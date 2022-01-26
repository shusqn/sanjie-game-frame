package com.frame.pve.handler.cmd;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import com.frame.entity.RobotUser;
import com.frame.entity.User;
import com.frame.enums.GameType;
import com.frame.executor.DeskExecutorHandler;
import com.frame.mobel.ProtoBuf;
import com.frame.mobel.Tupple3;
import com.frame.mobel.mq.ChanelClose;
import com.frame.model.Player;
import com.frame.model.game.BetInfo;
import com.frame.protobuf.CommonMsg;
import com.frame.protobuf.PveGameMsg;
import com.frame.protobuf.RobotMsg;
import com.frame.protobuf.RobotMsg.ReqRobotLoginGame;
import com.frame.pve.PveGameMrg;
import com.frame.pve.handler.PlayerOfflineHandler;
import com.frame.pve.handler.RedisPveServerHandler;
import com.frame.pve.manager.DeskMgr;
import com.frame.pve.manager.PlayerMgr;
import com.frame.pve.model.PveGameDesk;
import com.frame.pve.rocketmq.RocketMqReceive;
import com.frame.pve.router.MqGameDataRouter;
import com.frame.pve.router.MqPbCmdRouter;
import com.frame.service.UserService;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

/**
 * 对接客户端req请求相关操作
 */
@Slf4j
public final class GameReqCmdHandler {
	private static Map<Integer, GameReqCmdHandler> instanceMap = new ConcurrentHashMap<Integer, GameReqCmdHandler>();
	public static GameReqCmdHandler getInstance(GameType gameType) {
		GameReqCmdHandler instance = instanceMap.get(gameType.getType());
		if(instance == null) {
			instance = new GameReqCmdHandler();
			instance.gameType = gameType;
			instanceMap.put(gameType.getType(), instance);
			//
			instance.init();
		}
		return instance;
	}
	private GameType gameType;
	//==================================
	private Gson gson = new Gson();
	private void init(){
		log.info("GameReqCmdHandler init success !");
		//监听mq收到的ProtoBuf
		MqGameDataRouter.getInstance(gameType).registHandler(ProtoBuf.class.getSimpleName(), this::receiveMqData);
		//监听mq收到的ChanelClose
		MqGameDataRouter.getInstance(gameType).registHandler(ChanelClose.class.getSimpleName(), this::chanelClose);

		//机器人登录请求
		MqPbCmdRouter.getInstance(gameType).registHandler(RobotMsg.SubCmd.Cmd_ReqRobotLoginGame_VALUE, this::reqRobotLoginGame);

		MqPbCmdRouter.getInstance(gameType).registHandler(PveGameMsg.SubCmd.Cmd_ReqSitDown_VALUE, this::reqSitDown);

		MqPbCmdRouter.getInstance(gameType).registHandler(PveGameMsg.SubCmd.Cmd_ReqGetNewData_VALUE, this::reqGetNewData);

		MqPbCmdRouter.getInstance(gameType).registHandler(PveGameMsg.SubCmd.Cmd_ReqLeave_VALUE, this::reqLeave);

		MqPbCmdRouter.getInstance(gameType).registHandler(PveGameMsg.SubCmd.Cmd_ReqBet_VALUE, this::reqBet);
	}

	/**
	 * @param pbvodata
	 */
	private void chanelClose(String pbvodata) {
		try {
			ChanelClose pbvo = gson.fromJson(pbvodata, ChanelClose.class);
			Player player = PlayerMgr.getInstance(gameType).getPlayer(pbvo.getPid());
			if(player != null) {
				int deskId = player.getDeskId();
				DeskExecutorHandler.getInstance().execute(deskId, PlayerOfflineHandler.getInstance(gameType)::offlineHandler, pbvo.getPid(), "PlayerOfflineHandler.getInstance().offlineHandler");
				log.info("pid:{} 离线", pbvo.getPid());
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * @param pbvodata
	 */
	private void receiveMqData(String pbvodata) {
		try {
			ProtoBuf pbvo = gson.fromJson(pbvodata, ProtoBuf.class);
			Consumer<ProtoBuf> handler = MqPbCmdRouter.getInstance(gameType).getHandler(pbvo.getCmd());
			if(handler != null) {
				Player player = PlayerMgr.getInstance(gameType).getPlayer(pbvo.getPid());
				if(player != null) {
					int deskId = player.getDeskId();
					DeskExecutorHandler.getInstance().execute(deskId, handler, pbvo, "cmd:" + pbvo.getCmd() +"handler");
				}
				//玩家或机器人登录
				else if(pbvo.getCmd() == PveGameMsg.SubCmd.Cmd_ReqSitDown_VALUE || 
						pbvo.getCmd() == RobotMsg.SubCmd.Cmd_ReqRobotLoginGame_VALUE) {
					MqPbCmdRouter.getInstance(gameType).executeHandler(pbvo.getCmd(), pbvo);
				}
				else {
					log.error("玩家还没登录就发送其他消息了:{}", pbvo.toString());
				}
			}
			else {
				log.error("找不到cmd={}的处理方法", pbvo.getCmd());
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	/**
	 * @param pbvodata
	 */
	private void reqRobotLoginGame(ProtoBuf pbvo) {
		ReqRobotLoginGame message = null;
		try {
			message = ReqRobotLoginGame.parseFrom(pbvo.getBody());
			log.info("机器人{} 登录{}游戏服务器", pbvo.getPid(), gameType.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		RobotUser robotUser = new RobotUser();
		robotUser.setBalance(message.getBalance());
		robotUser.setHeadPic(message.getHeadPic());
		robotUser.setHeadPicType(message.getHeadPicType());
		robotUser.setLevel(message.getLevel());
		robotUser.setNickName(message.getName());
		robotUser.setUid(Long.valueOf(message.getUserId()));
		
		doSitDown(pbvo, message.getRoomId(), message.getDeskId(), robotUser, false, 0);
	}

	/**
	 * 下注
	 * @param pbvo
	 */
	private void reqBet(ProtoBuf pbvo) {
		long pid= pbvo.getPid();
		Player player = PlayerMgr.getInstance(gameType).getPlayer(pid);
		if(player == null) {
			log.warn("找不到玩家 pid:{}", pid);
			return;
		}
		final PveGameDesk desk = DeskMgr.getInstance(gameType).getDesk(player.getDeskId());
		if(desk == null) {
			log.error("玩家:{} 不在桌台上", pid);
			return;
		}
		if(desk.getState() != PveGameMsg.DeskState.BET_VALUE) {
			GamePushCmdHandler.getInstance(gameType).pushErrorMessage(pid, CommonMsg.ErrorCode.SYSTEM_ERR_VALUE, "当前状态不允许下注");
			return;
		}
		PveGameMsg.ReqBet message = null;
		try {
			message = PveGameMsg.ReqBet.parseFrom(pbvo.getBody());
			log.info("玩家{} 登录 roomId:{}, deskId:{}", pid, message.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(message.getBetAmount() > player.getUser().getBalance()) {
			GamePushCmdHandler.getInstance(gameType).pushErrorMessage(pid, CommonMsg.ErrorCode.BALANCE_NOT_ENOUGH_VALUE);
			return;
		}
		if(message.getBetAmount() < desk.getConfig().getMinBet()) {
			GamePushCmdHandler.getInstance(gameType).pushErrorMessage(pid, CommonMsg.ErrorCode.EXCEEDS_LIMIT_VALUE);
			return;
		}

		desk.getPlayerBetInfo().computeIfAbsent(pid, k-> new ArrayList<BetInfo>()).add(new BetInfo(message.getBetType(), message.getBetAmount()));
		GameNoticeCmdHandler.getInstance(gameType).noticeBet(desk.getDeskId(), pid, message.getBetType(), message.getBetAmount());
	}

	/**
	 * @param pbvo
	 */
	private void reqLeave(ProtoBuf pbvo) {
		long pid= pbvo.getPid();
		Player player = PlayerMgr.getInstance(gameType).getPlayer(pid);
		if(player == null) {
			log.warn("找不到玩家 pid:{}", pid);
			return;
		}

		log.info("pid:{} 点击退出房间 deskId:{}", pid, player.getDeskId());
		PlayerOfflineHandler.getInstance(gameType).offlineHandler(pid, true);
	}

	/**
	 * 坐下
	 * @param pbvo
	 */
	public void reqGetNewData(ProtoBuf pbvo) {
		long pid = pbvo.getPid();
		Player player = PlayerMgr.getInstance(gameType).getPlayer(pid);
		if(player == null) {
			log.warn("找不到玩家 pid:{}", pid);
			return;
		}
		final PveGameDesk desk = DeskMgr.getInstance(gameType).getDesk(player.getDeskId());
		if(desk == null) {
			log.error("玩家:{} 不在桌台上", pid);
			return;
		}

		DeskExecutorHandler.getInstance().execute(desk.getDeskId(), ()->{
			GamePushCmdHandler.getInstance(gameType).pushSitDown(pid, desk);
			log.info("pid: {} 获取最新数据！", pid);
		});
	}

	/**
	 * 坐下
	 * @param pbvo
	 */
	public void reqSitDown(ProtoBuf pbvo) {
		long pid = pbvo.getPid();

		PveGameMsg.ReqSitDown message = null;
		try {
			message = PveGameMsg.ReqSitDown.parseFrom(pbvo.getBody());
			log.info("玩家{} 登录 roomId:{}, deskId:{}", pid, message.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		int serverId = RedisPveServerHandler.getInstance(gameType).getUserGameServerId(pid);

		if(serverId != 0 && serverId != PveGameMrg.getInstance(gameType).getServerId()) {
			GamePushCmdHandler.getInstance(gameType).pushErrorMessage(pid, CommonMsg.ErrorCode.SYSTEM_ERR_VALUE, "玩家在其他游戏未完结");
			return;
		}

		User user = UserService.getInstance().findUser(pid);
		if (user == null) {
			GamePushCmdHandler.getInstance(gameType).pushErrorMessage(pid, CommonMsg.ErrorCode.SYSTEM_ERR_VALUE, "玩家信息不存在");
			return;
		}

		int roomId = message.getRoomId();
		int deskId = message.getDeskId();
		doSitDown(pbvo, roomId, deskId, user, false, 0);
	}

	/**
	 * @param gameType
	 * @param roomType
	 * @param deskId
	 * @param pid
	 * @param player
	 * @param user
	 * @param shiftDesk
	 * @param exceptDeskId
	 */
	public void doSitDown(ProtoBuf pb, int roomId, int deskId,User user, boolean shiftDesk, int exceptDeskId) {
		long pid = pb.getPid();
		Player player = PlayerMgr.getInstance(gameType).getPlayer(pid);
		//属于断线重连，直接放入先前的座位
		if(player != null && !shiftDesk) {
			final PveGameDesk fdesk = DeskMgr.getInstance(gameType).getDesk(player.getDeskId());
			DeskExecutorHandler.getInstance().execute(fdesk.getDeskId(), ()->{
				fdesk.getOfflinePlayerMap().remove(pid);
				GamePushCmdHandler.getInstance(gameType).pushSitDown(pid, fdesk);
				log.info("pid: {} 断线重连！", pid);
			});
			return;
		}
		if(player == null) {
			player = new Player(user);
		}
		player.setPb(pb);

		PveGameDesk desk = DeskMgr.getInstance(gameType).faskGetDesk(roomId, exceptDeskId, user.isRobot());
		DeskExecutorHandler.getInstance().execute(desk.getDeskId(), this::joinDesk,
				new Tupple3<User, Player, PveGameDesk>(user, player, desk), "joinDesk");
	}

	/**
	 * @param tupple3
	 */
	private void joinDesk(Tupple3<User, Player, PveGameDesk> tupple3) {
		User user = tupple3.first;
		Player player = tupple3.second;
		PveGameDesk desk = tupple3.third;

		final Player fplayer =  buildPlayerByUser(user, player, desk);
		long pid = fplayer.getPid();
		GamePushCmdHandler.getInstance(gameType).pushSitDown(pid, desk);
		int playerSeat = desk.getSeatByPid(pid);
		if(playerSeat != -1) {
		}
		else {
			if(desk.getConfig().getMinJoin() > user.getBalance()) {
				GamePushCmdHandler.getInstance(gameType).pushErrorMessage(pid, CommonMsg.ErrorCode.BALANCE_NOT_ENOUGH_VALUE);
			}
		}

		//加入redis的所在游戏信息
		RedisPveServerHandler.getInstance(gameType).setUserGameServerId(pid, PveGameMrg.getInstance(gameType).getServerId());
		//刷新在线人数
		RedisPveServerHandler.getInstance(gameType).updateOnlineUsers(PlayerMgr.getInstance(gameType).getAll().size());
	}

	/**
	 * @param user
	 * @return
	 */
	private Player buildPlayerByUser(User user, Player player, PveGameDesk desk) {
		long pid = user.getUid();
		if(player == null) {
			player = new Player(user);
		}
		PlayerMgr.getInstance(gameType).addPlayer(player);
		player.setDeskId(desk.getDeskId());

		int canSitIndex = desk.getCanSitIndex();

		if(desk.getConfig().getMinJoin() > user.getBalance()) {
			canSitIndex = -1;
		}

		if(canSitIndex != -1) {
			desk.sitdown(pid, canSitIndex);
		}
		else {
			desk.getLookOnPlayerInfo().put(pid, pid);
		}

		return player;
	}

}
