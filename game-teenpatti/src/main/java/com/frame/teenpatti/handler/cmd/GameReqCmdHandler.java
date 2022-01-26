package com.frame.teenpatti.handler.cmd;

import java.util.function.Consumer;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import com.frame.entity.RobotUser;
import com.frame.entity.User;
import com.frame.executor.DeskExecutorHandler;
import com.frame.mobel.ProtoBuf;
import com.frame.mobel.Tupple3;
import com.frame.mobel.mq.ChanelClose;
import com.frame.model.Player;
import com.frame.protobuf.CommonMsg;
import com.frame.protobuf.CommonMsg.ErrorCode;
import com.frame.protobuf.RobotMsg;
import com.frame.protobuf.RobotMsg.ReqRobotLoginGame;
import com.frame.protobuf.TeenpattiMsg;
import com.frame.protobuf.TeenpattiMsg.DeskState;
import com.frame.protobuf.TeenpattiMsg.DropCardType;
import com.frame.protobuf.TeenpattiMsg.ReqRefuseOrAgreeShow;
import com.frame.protobuf.TeenpattiMsg.ReqSitDown;
import com.frame.protobuf.TeenpattiMsg.SubCmd;
import com.frame.service.UserService;
import com.frame.teenpatti.TeenpattiMrg;
import com.frame.teenpatti.handler.PlayerOfflineHandler;
import com.frame.teenpatti.handler.RedisTeenpattiServerHandler;
import com.frame.teenpatti.manager.DeskMgr;
import com.frame.teenpatti.manager.PlayerMgr;
import com.frame.teenpatti.model.TeenpattiDesk;
import com.frame.teenpatti.rocketmq.RocketMqReceive;
import com.frame.teenpatti.router.MpPbCmdRouter;
import com.frame.utils.Ggson;
import com.google.protobuf.InvalidProtocolBufferException;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 对接客户端req请求相关操作
 */
@Service
@Slf4j
public class GameReqCmdHandler {
	@Getter
	private static GameReqCmdHandler instance;
	@PostConstruct
	private void init(){
		log.info("TeenpattiReqCmdHandler init success !");
		//监听mq收到的ProtoBuf
		RocketMqReceive.getMqRouterHandler().registHandler(ProtoBuf.class.getSimpleName(), this::receiveMqData);
		//玩家断线
		RocketMqReceive.getMqRouterHandler().registHandler(ChanelClose.class.getSimpleName(), this::chanelClose);
		//机器人登录请求
		MpPbCmdRouter.getInstance().registHandler(RobotMsg.SubCmd.Cmd_ReqRobotLoginGame_VALUE, this::reqRobotLoginGame);

		MpPbCmdRouter.getInstance().registHandler(TeenpattiMsg.SubCmd.Cmd_ReqSitDown_VALUE, this::reqSitDown);
		MpPbCmdRouter.getInstance().registHandler(TeenpattiMsg.SubCmd.Cmd_ReqSitUp_VALUE, this::reqSitUp);
		MpPbCmdRouter.getInstance().registHandler(TeenpattiMsg.SubCmd.Cmd_ReqRefuseOrAgreeShow_VALUE, this::reqRefuseOrAgreeShow);
		MpPbCmdRouter.getInstance().registHandler(TeenpattiMsg.SubCmd.Cmd_ReqShow_VALUE, this::reqShow);

		MpPbCmdRouter.getInstance().registHandler(TeenpattiMsg.SubCmd.Cmd_ReqRaise_VALUE, this::reqRaise);
		MpPbCmdRouter.getInstance().registHandler(TeenpattiMsg.SubCmd.Cmd_ReqCall_VALUE, this::reqCall);

		MpPbCmdRouter.getInstance().registHandler(TeenpattiMsg.SubCmd.Cmd_ReqSeeCard_VALUE, this::reqSeeCard);

		MpPbCmdRouter.getInstance().registHandler(TeenpattiMsg.SubCmd.Cmd_ReqReady_VALUE, this::reqReady);
		MpPbCmdRouter.getInstance().registHandler(TeenpattiMsg.SubCmd.Cmd_ReqShiftGame_VALUE, this::reqShiftGame);

		MpPbCmdRouter.getInstance().registHandler(TeenpattiMsg.SubCmd.Cmd_ReqGetNewData_VALUE, this::reqGetNewData);

		MpPbCmdRouter.getInstance().registHandler(TeenpattiMsg.SubCmd.Cmd_ReqDropCard_VALUE, this::reqDropCard);
		MpPbCmdRouter.getInstance().registHandler(TeenpattiMsg.SubCmd.Cmd_ReqLeave_VALUE, this::reqLeave);
	}
	
	/**
	 * @param pbvodata
	 */
	private void reqRobotLoginGame(ProtoBuf pbvo) {
		ReqRobotLoginGame message = null;
		try {
			message = ReqRobotLoginGame.parseFrom(pbvo.getBody());
			log.info("机器人{} 登录teenpatti游戏服务器", pbvo.getPid());
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
	 * @param pbvodata
	 */
	private void chanelClose(String pbvodata) {
		try {
			ChanelClose pbvo = Ggson.gson.fromJson(pbvodata, ChanelClose.class);
			Player player = PlayerMgr.getInstance().getPlayer(pbvo.getPid());
			if(player != null) {
				int deskId = player.getDeskId();
				DeskExecutorHandler.getInstance().execute(deskId, PlayerOfflineHandler.getInstance()::offlineHandler, pbvo.getPid(), "PlayerOfflineHandler.getInstance().offlineHandler");
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
			ProtoBuf pbvo = Ggson.gson.fromJson(pbvodata, ProtoBuf.class);
			Consumer<ProtoBuf> handler = MpPbCmdRouter.getInstance().getHandler(pbvo.getCmd());
			if(handler != null) {
				Player player = PlayerMgr.getInstance().getPlayer(pbvo.getPid());
				if(player != null) {
					int deskId = player.getDeskId();
					DeskExecutorHandler.getInstance().execute(deskId, handler, pbvo, "cmd:" + pbvo.getCmd() +" handler");
				}
				//玩家或机器人登录
				else if(pbvo.getCmd() == TeenpattiMsg.SubCmd.Cmd_ReqSitDown_VALUE || 
						pbvo.getCmd() == RobotMsg.SubCmd.Cmd_ReqRobotLoginGame_VALUE) {
					MpPbCmdRouter.getInstance().executeHandler(pbvo.getCmd(), pbvo);
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
	 *  Cmd_ReqSitUp = 889; 
    玩家请求站起
	 * @param pbvo
	 */
	private void reqSitUp(ProtoBuf pbvo) {
		long pid= pbvo.getPid();
		Player player = PlayerMgr.getInstance().getPlayer(pid);
		TeenpattiDesk desk = DeskMgr.getInstance().getDesk(player.getDeskId());
		if(!desk.isCanLeaveDesk() && desk.getGamingPlayerInfo().get(pid) != null){
			log.info("玩家pid:{}站起，直接弃牌", pid);
			desk.getDeskHandler().dropCardHandler(pid, DropCardType.STAND_UP_VALUE);
			return;
		}

		desk.getDeskHandler().standUp(pid);
		desk.getDeskHandler().playerLeaveDesk();
	}

	/**
	 * Cmd_ReqRefuseShow           = 1024;               //玩家拒绝比牌
	 *  message ReqRefuseShow{
   optional bool refuse = 1;  //是否拒绝
}    
	 * @param pbvo
	 */
	private void reqRefuseOrAgreeShow(ProtoBuf pbvo) {
		long pid= pbvo.getPid();
		Player player = PlayerMgr.getInstance().getPlayer(pid);
		ReqRefuseOrAgreeShow message = null;
		try {
			message = ReqRefuseOrAgreeShow.parseFrom(pbvo.getBody());
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}

		boolean agree = message.getAgree();
		TeenpattiDesk desk = DeskMgr.getInstance().getDesk(player.getDeskId());
		desk.getDeskHandler().playerAgreeShow(pid, SubCmd.Cmd_ReqRefuseOrAgreeShow_VALUE, agree);
	}

	/**
	 * Cmd_ReqShow                 = 1023;               //玩家请求和谁比牌
	 *  message ReqShow{
   optional int32 compareUserId = 1;//被比牌玩家id
}  
	 * @param pbvo
	 */
	private void reqShow(ProtoBuf pbvo) {
		long pid= pbvo.getPid();
		Player player = PlayerMgr.getInstance().getPlayer(pid);
		TeenpattiDesk desk = DeskMgr.getInstance().getDesk(player.getDeskId());
		desk.getDeskHandler().playerShow(pid, SubCmd.Cmd_ReqShow_VALUE);
	}


	/**
	 *  Cmd_ReqRaise                = 1021;               //玩家请求加注
	 * @param pbvo
	 */
	private void reqRaise(ProtoBuf pbvo) {
		long pid= pbvo.getPid();
		Player player = PlayerMgr.getInstance().getPlayer(pid);
		TeenpattiDesk desk = DeskMgr.getInstance().getDesk(player.getDeskId());
		desk.getDeskHandler().playerRaise(pid, SubCmd.Cmd_ReqRaise_VALUE);
	}

	/**
	 *     Cmd_ReqCall                 = 1019;               //玩家请求跟注
	 * @param pbvo
	 */
	private void reqCall(ProtoBuf pbvo) {
		long pid= pbvo.getPid();
		Player player = PlayerMgr.getInstance().getPlayer(pid);
		TeenpattiDesk desk = DeskMgr.getInstance().getDesk(player.getDeskId());
		desk.getDeskHandler().playerCall(pid, SubCmd.Cmd_ReqCall_VALUE);
	}

	/**
	 *   Cmd_ReqSeeCard              = 1014;               //玩家请求看牌
	 * @param pbvo
	 */
	private void reqSeeCard(ProtoBuf pbvo) {
		long pid= pbvo.getPid();
		Player player = PlayerMgr.getInstance().getPlayer(pid);
		TeenpattiDesk desk = DeskMgr.getInstance().getDesk(player.getDeskId());
		desk.getDeskHandler().seeCard(pid);
	}

	/**
	 *  Cmd_ReqDropGame             = 997;                //投降
	 * @param pbvo
	 */
	private void reqReady(ProtoBuf pbvo) {
		long pid= pbvo.getPid();
		Player player = PlayerMgr.getInstance().getPlayer(pid);
		TeenpattiDesk desk = DeskMgr.getInstance().getDesk(player.getDeskId());
		log.info("pid:{}  gold:{} minlimit:{}", pid, player.getUser().getBalance(), desk.getConfig().getMinBet());
		if(player.getUser().getBalance() < desk.getConfig().getMinJoin()) {
			GamePushCmdHandler.getInstance().pushErrorMessage(pid, CommonMsg.ErrorCode.BALANCE_NOT_ENOUGH_VALUE, "");
			return;
		}
		if(desk.getReadyPlayerInfo().get(pid) != null || desk.getGamingPlayerInfo().get(pid) != null) {
			log.error("玩家pid:{}已经准备了", pid);
			return;
		}
		if(desk.getState() != DeskState.WAIT_VALUE && desk.getState() != DeskState.START_COUNTDOWN_VALUE ) {
			int seat = desk.getCanSitIndex();
			if(seat == -1) {
				log.error("pid:{} 没有座位可以坐下", pid);
				GamePushCmdHandler.getInstance().pushErrorMessage(pid, ErrorCode.SYSTEM_ERR_VALUE, "没有座位可以坐下");
				return;
			}

			desk.sitdown(pid, seat);
			desk.getReadyPlayerInfo().put(pid, pid);
			desk.getLookOnPlayerInfo().remove(player.getPid());
			GameNoticeCmdHandler.getInstance().noticeUserEnter(desk, seat, pid, false);
			return;
		}
		boolean isLookOnReady = false;
		if(desk.getSeatByPid(pid) == -1 && desk.getLookOnPlayerInfo().get(player.getPid()) != null) {
			int seat = desk.getCanSitIndex();
			if(seat == -1) {
				log.error("pid:{} 没有座位可以坐下", pid);
				GamePushCmdHandler.getInstance().pushErrorMessage(pid, ErrorCode.SYSTEM_ERR_VALUE, "没有座位可以坐下");
				return;
			}
			desk.getDeskSeatPlayerInfo().put(seat, pid);
			desk.getReadyPlayerInfo().put(pid, pid);
			desk.getLookOnPlayerInfo().remove(player.getPid());
			GameNoticeCmdHandler.getInstance().noticeUserEnter(desk, seat, pid, false);

			isLookOnReady = true;
			log.info("pid:{}旁观的玩家坐下", pid);
		}
		if(!isLookOnReady) {
			log.info("pid:{} 玩家准备", pid);
			desk.getReadyPlayerInfo().put(pid, pid);
			GameNoticeCmdHandler.getInstance().noticePlayerStatus(desk, pid);
		}
		desk.getDeskHandler().playerJoinDesk();
	}

	/**
	 * 玩家弃牌
	 * @param pbvo
	 */
	private void reqDropCard(ProtoBuf pbvo) {
		long pid= pbvo.getPid();
		Player player = PlayerMgr.getInstance().getPlayer(pid);
		TeenpattiDesk desk = DeskMgr.getInstance().getDesk(player.getDeskId());
		desk.getDeskHandler().playerDropCard(pid, SubCmd.Cmd_ReqDropCard_VALUE);
	}

	/**
	 * 换桌
	 * @param pbvo
	 */
	private void reqShiftGame(ProtoBuf pbvo) {
		long pid= pbvo.getPid();
		Player player = PlayerMgr.getInstance().getPlayer(pid);
		TeenpattiDesk desk = DeskMgr.getInstance().getDesk(player.getDeskId());
		if(!desk.isCanLeaveDesk() && desk.getGamingPlayerInfo().get(pid) != null){
			log.info("玩家pid:{}换桌，直接弃牌", pid);
			desk.getDeskHandler().dropCardHandler(pid, DropCardType.SHIFT_DESK_VALUE);
			return;
		}

		log.info("pid:{} 离开房间 deskId:{}", pid, desk.getDeskId());
		PlayerOfflineHandler.getInstance().offlineHandler(pid);
		
		doSitDown(pbvo, desk.getConfig().getRoomId(), 0, player.getUser(), true, desk.getDeskId());
	}

	/**
	 * @param pbvo
	 */
	private void reqLeave(ProtoBuf pbvo) {
		long pid= pbvo.getPid();
		Player player = PlayerMgr.getInstance().getPlayer(pid);
		log.info("pid:{} 点击退出房间 deskId:{}", pid, player.getDeskId());
		PlayerOfflineHandler.getInstance().offlineHandler(pid, true);
	}

	/**
	 * 坐下
	 * @param pbvo
	 */
	public void reqGetNewData(ProtoBuf pbvo) {
		long pid = pbvo.getPid();
		Player player = PlayerMgr.getInstance().getPlayer(pid);
		final TeenpattiDesk desk = DeskMgr.getInstance().getDesk(player.getDeskId());
		if(desk == null) {
			log.error("玩家:{} 不在桌台上", pid);
			return;
		}

		DeskExecutorHandler.getInstance().execute(desk.getDeskId(), ()->{
			GamePushCmdHandler.getInstance().pushSitDown(pid, desk);
			log.info("pid: {} 获取最新数据！", pid);
		});
	}

	/**
	 * 坐下
	 * @param pbvo
	 */
	public void reqSitDown(ProtoBuf pbvo) {
		long pid = pbvo.getPid();

		ReqSitDown message = null;
		try {
			message = ReqSitDown.parseFrom(pbvo.getBody());
		} catch (Exception e) {
			e.printStackTrace();
		}
		int serverId = RedisTeenpattiServerHandler.getInstance().getUserGameServerId(pid);

		if(serverId != 0 && serverId != TeenpattiMrg.getInstance().getServerId()) {
			GamePushCmdHandler.getInstance().pushErrorMessage(pbvo, CommonMsg.ErrorCode.SYSTEM_ERR_VALUE, "玩家在其他游戏未完结");
			return;
		}

		User user = UserService.getInstance().findUser(pid);
		if (user == null) {
			GamePushCmdHandler.getInstance().pushErrorMessage(pbvo, CommonMsg.ErrorCode.SYSTEM_ERR_VALUE, "玩家信息不存在");
			return;
		}

		try {
			doSitDown(pbvo, message.getRoomId(), message.getDeskId(), user, false, 0);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			GamePushCmdHandler.getInstance().pushErrorMessage(pbvo, CommonMsg.ErrorCode.SYSTEM_ERR_VALUE, e.getMessage());
		}
	}

	/**
	 * @param roomId
	 * @param deskId
	 * @param pid
	 * @param player
	 * @param user
	 * @param shiftDesk
	 * @param exceptDeskId
	 */
	public void doSitDown(ProtoBuf pb, int roomId, int deskId, User user, boolean shiftDesk, int exceptDeskId) {
		long pid = pb.getPid();
		Player player = PlayerMgr.getInstance().getPlayer(pid);

		//属于断线重连，直接放入先前的座位
		if(player != null && !shiftDesk) {
			final TeenpattiDesk fdesk = DeskMgr.getInstance().getDesk(player.getDeskId());
			DeskExecutorHandler.getInstance().execute(fdesk.getDeskId(), ()->{
				fdesk.getOfflinePlayerMap().remove(pid);
				GamePushCmdHandler.getInstance().pushSitDown(pid, fdesk);
				GameNoticeCmdHandler.getInstance().noticeUserOnline(fdesk.getDeskId(), pid);
				log.info("pid: {} 断线重连！", pid);
			});
			return;
		}
		
		if(player == null) {
			player = new Player(user);
		}
		player.setPb(pb);
		
		TeenpattiDesk desk = DeskMgr.getInstance().faskGetDesk(roomId, exceptDeskId, user.isRobot());
		if(desk.getConfig().getMinJoin() > user.getBalance()) {
			GamePushCmdHandler.getInstance().pushErrorMessage(pb, CommonMsg.ErrorCode.BALANCE_NOT_ENOUGH_VALUE);
			return;
		}
		
		DeskExecutorHandler.getInstance().execute(desk.getDeskId(), this::joinDesk,
				new Tupple3<User, Player, TeenpattiDesk>(user, player, desk), "joinDesk");
	}

	/**
	 * @param tupple3
	 */
	private void joinDesk(Tupple3<User, Player, TeenpattiDesk> tupple3) {
		User user = tupple3.first;
		Player player = tupple3.second;
		TeenpattiDesk desk = tupple3.third;

		final Player fplayer =  buildPlayerByUser(user, player, desk);
		long pid = fplayer.getPid();
		
		GamePushCmdHandler.getInstance().pushSitDown(pid, desk);
		int playerSeat = desk.getSeatByPid(pid);
		if(playerSeat != -1) {
			GameNoticeCmdHandler.getInstance().noticeUserEnter(desk, playerSeat, pid, true);
			desk.getDeskHandler().playerJoinDesk();
		}

		//加入redis的所在游戏信息
		RedisTeenpattiServerHandler.getInstance().setUserGameServerId(pid, TeenpattiMrg.getInstance().getServerId());
		//刷新在线人数
		RedisTeenpattiServerHandler.getInstance().updateOnlineUsers(PlayerMgr.getInstance().getAll().size());
	}

	/**
	 * @param user
	 * @return
	 */
	private Player buildPlayerByUser(User user, Player player, TeenpattiDesk desk) {
		long pid = user.getUid();
		PlayerMgr.getInstance().addPlayer(player);
		player.setDeskId(desk.getDeskId());

		int canSitIndex = desk.getCanSitIndex();

		if(canSitIndex != -1) {
			desk.sitdown(pid, canSitIndex);
			if(desk.getState() == DeskState.WAIT_VALUE || desk.getState() == DeskState.START_COUNTDOWN_VALUE) {
				desk.getReadyPlayerInfo().put(pid, pid);
			}
		}
		else {
			desk.getLookOnPlayerInfo().put(pid, pid);
		}

		return player;
	}

}
