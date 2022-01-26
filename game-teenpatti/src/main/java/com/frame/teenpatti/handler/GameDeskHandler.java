package com.frame.teenpatti.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSON;
import com.frame.enums.ServerType;
import com.frame.executor.DeskExecutorHandler;
import com.frame.executor.TimerExecutorHandler;
import com.frame.executor.VoidFunction;
import com.frame.mobel.WaitProtocol;
import com.frame.mobel.card.BetType;
import com.frame.mobel.card.Card;
import com.frame.mobel.card.CardRankType;
import com.frame.mobel.card.CardsUtils;
import com.frame.mobel.game.Operate;
import com.frame.mobel.mq.AskRobotLogin;
import com.frame.mobel.mq.AskRobotLoginOut;
import com.frame.model.Player;
import com.frame.model.game.BetInfo;
import com.frame.protobuf.CommonMsg;
import com.frame.protobuf.TeenpattiMsg.DeskState;
import com.frame.protobuf.TeenpattiMsg.DropCardType;
import com.frame.protobuf.TeenpattiMsg.SubCmd;
import com.frame.teenpatti.TeenpattiMrg;
import com.frame.teenpatti.handler.cmd.GameNoticeCmdHandler;
import com.frame.teenpatti.handler.cmd.GamePushCmdHandler;
import com.frame.teenpatti.manager.PlayerMgr;
import com.frame.teenpatti.model.TeenpattiDesk;
import com.frame.teenpatti.rocketmq.RocketMqSender;
import com.frame.utils.ArrayUtils;

import lombok.extern.slf4j.Slf4j;


/**
 * 拉米桌台操作相关
 * @author Sanjie
 * @date 2020-01-20 18:05
 * @version 1.0
 */
@Slf4j
public class GameDeskHandler{
	private final TeenpattiDesk desk;
	public GameDeskHandler(TeenpattiDesk desk) {
		this.desk = desk;
		waitStart(null);
	}
	
	/**
	 * @param desk
	 */
	public void robotManager() {
		for (Long pid : desk.getLookOnPlayerInfo().values()) {
			Player player = PlayerMgr.getInstance().getPlayer(pid);
			if(player != null && player.getUser().isRobot()) {
				RocketMqSender.getInstance().pushAskRobotData2RobotServer(
						AskRobotLoginOut.builder()
						.uid(player.getPid())
						.serverType(ServerType.TEENPATTI.getType())
						.sid(TeenpattiMrg.getInstance().getServerId())
						.build());
			}
		}
		
		int count = desk.getSeats().length - desk.getDeskSeatPlayerInfo().size() - 1;
		if(count < 0) {
			for (Long pid : desk.getDeskSeatPlayerInfo().values()) {
				Player player = PlayerMgr.getInstance().getPlayer(pid);
				if(player != null && player.getUser().isRobot()) {
					RocketMqSender.getInstance().pushAskRobotData2RobotServer(
							AskRobotLoginOut.builder()
							.uid(player.getPid())
							.serverType(ServerType.TEENPATTI.getType())
							.sid(TeenpattiMrg.getInstance().getServerId())
							.build());
					break;
				}
			}
		}
		else if(count >=1){
			TimerExecutorHandler.getInstance().executeDelay((long) (Math.random() * 1500), 0, new VoidFunction() {
				@Override
				public void handle() {
					RocketMqSender.getInstance().pushAskRobotData2RobotServer(
							AskRobotLogin.builder()
							.roomId(desk.getConfig().getRoomId())
							.serverType(ServerType.TEENPATTI.getType())
							.sid(TeenpattiMrg.getInstance().getServerId())
							.build());
				}
			}, "pushAskRobotData2RobotServer");
		}
	}
	
	/**
	 * 玩家加入游戏桌
	 */
	public void playerJoinDesk() {
		log.info("waitPlayerJoin readySize {}", desk.getReadyPlayerInfo().size() );
	    robotManager();
		if(desk.getReadyPlayerInfo().size() >= desk.getConfig().getMinPlayersCount() && desk.getState() == DeskState.WAIT_VALUE && !isAllRobot()) {
			//等待倒计时后开始游戏
			int waitSeconds = 5;
			desk.setState(DeskState.START_COUNTDOWN_VALUE);
			desk.setWaitProtocol(WaitProtocol.build(TimeUnit.SECONDS.toMillis(waitSeconds)));
			DeskExecutorHandler.getInstance().executeDelay(TimeUnit.SECONDS.toMillis(waitSeconds), desk.getDeskId(), this::betBootVaule, desk.getWaitProtocol(), "betBootVaule");

			//推送给客户端
			noticeDeskStateChange();
		}
	}

	/**
	 * 玩家离开游戏桌
	 */
	public void playerLeaveDesk() {
		log.info("playerLeftDesk");
		if(desk.getState() == DeskState.START_COUNTDOWN_VALUE && (desk.getReadyPlayerInfo().size() < 2 || isAllRobot())) {
			desk.setState(DeskState.WAIT_VALUE);
			desk.setWaitProtocol(WaitProtocol.build(TimeUnit.SECONDS.toMillis(0)));

			//推送给客户端
			noticeDeskStateChange();
		}
	}

	/**
	 * 下底注
	 */
	private void betBootVaule(WaitProtocol vo) {
		if(vo.getUuid() != desk.getWaitProtocol().getUuid()) {
			return;
		}
		if(isAllRobot()) {
			waitStart(null);
			log.info("isAllRobot chooseBanker fail");
			return;
		}

		//站起所有未准备玩家
		standUpNoReady();

		//=============================================================
		//初始化桌台数据
		desk.setRoundId(TeenpattiMrg.getInstance().getIdBuilder().nextId());
		desk.setStartTime(System.currentTimeMillis());

		//后台提前洗牌
		desk.getCardsRepository().clear();
		desk.getCardsRepository().addAll(CardsUtils.shuffleCards(false, false));
		desk.getPlayerCardsInfo().clear();

		log.info("sendCards size:{}", desk.getCardsRepository().size());
		for (Entry<Integer, Long> entry : desk.getDeskSeatPlayerInfo().entrySet()) {
			Long pid = entry.getValue();
			if(desk.getGamingPlayerInfo().get(pid) == null) {
				continue;
			}
			List<Card> cardList = new ArrayList<Card>();
			desk.getPlayerCardsInfo().put(pid, cardList);
			for (int i = 0; i < 3; i++) {
				cardList.add(desk.getCardsRepository().remove(0));
			}
			CardsUtils.sortCardsByValue(cardList, CardRankType.JIN_HUA);
		}

		//=======================下底注===================================
		int waitSeconds = 2;
		desk.setState(DeskState.BET_BOOTVALUE_VALUE);
		desk.setWaitProtocol(WaitProtocol.build(TimeUnit.SECONDS.toMillis(waitSeconds)));
		DeskExecutorHandler.getInstance().executeDelay(TimeUnit.SECONDS.toMillis(waitSeconds), desk.getDeskId(), this::chooseBanker, desk.getWaitProtocol(), "chooseBanker");
		//推送给客户端
		noticeDeskStateChange();

		//所有人下底注
		for (Long pid : desk.getGamingPlayerInfo().keySet()) {
			desk.getPlayerBetInfo().computeIfAbsent(pid, k-> new ArrayList<BetInfo>()).add(new BetInfo(BetType.ANTE.getValue(), desk.getAnte()));
			GameNoticeCmdHandler.getInstance().noticePlayerBetAnte(desk.getDeskId(), pid, desk.getAnte());

			//记录玩家行为
			addPlayerOperate(Operate.build(pid, Operate.ANTE, desk.getAnte()));
		}
	}

	/**
	 * 选庄
	 * @param vo
	 */
	private void chooseBanker(WaitProtocol vo) {
		if(vo.getUuid() != desk.getWaitProtocol().getUuid()) {
			log.info("chooseBanker fail");
			return;
		}

		//向前端发送选庄详情
		int waitSeconds = 1;

		desk.setCurrentPid(desk.chooseBankerPid()); 
		desk.setBankerPid(desk.getCurrentPid());

		desk.setState(DeskState.CHOOSE_BANKER_VALUE);
		desk.setWaitProtocol(WaitProtocol.build(TimeUnit.SECONDS.toMillis(waitSeconds)));
		DeskExecutorHandler.getInstance().executeDelay(TimeUnit.SECONDS.toMillis(waitSeconds), desk.getDeskId(), this::sendCards, desk.getWaitProtocol(), "sendCards");
		//推送给客户端
		noticeDeskStateChange();
		GameNoticeCmdHandler.getInstance().noticeBankerInfo(desk.getDeskId(), desk.getBankerPid());
	}

	/**
	 * 发牌
	 */
	private void sendCards(WaitProtocol vo) {
		if(vo.getUuid() != desk.getWaitProtocol().getUuid()) {
			return;
		}
		desk.setState(DeskState.SEND_CARDS_VALUE);
		int waitSeconds = 2;
		desk.setWaitProtocol(WaitProtocol.build(TimeUnit.SECONDS.toMillis(waitSeconds)));
		DeskExecutorHandler.getInstance().executeDelay(TimeUnit.SECONDS.toMillis(waitSeconds), desk.getDeskId(), this::startGame, desk.getWaitProtocol(), "startGame");
		//推送给客户端
		noticeDeskStateChange();
	}

	/**
	 * 开始游戏
	 * @param vo
	 */
	private void startGame(WaitProtocol vo) {
		if(vo.getUuid() != desk.getWaitProtocol().getUuid()) {
			return;
		}

		desk.setState(DeskState.GAMEING_VALUE);
		desk.setWaitProtocol(WaitProtocol.build());
		//推送给客户端
		noticeDeskStateChange();
		//通知下个玩家操作
		noticePlayerTurn(true);
	}

	/**
	 * @param pid
	 */
	public void seeCard(long pid) {
		if(desk.getGamingPlayerInfo().get(pid) == null) {
			log.error("pid:{} 游戏未开始，不能看牌", pid);
			return;
		}
		if(desk.getPlayerSeeCardstInfo().get(pid) != null) {
			log.error("玩家pid:{}已经看过牌了", pid);
			return;
		}
		log.info("pid:{} see card", pid);
		//标注玩家已看牌
		desk.getPlayerSeeCardstInfo().put(pid, pid);
		//广播玩家看了牌
		GameNoticeCmdHandler.getInstance().noticePlayerSeeCard(this.desk.getDeskId(), pid);
		//推送玩家具体看的牌
		GamePushCmdHandler.getInstance().pushSeeCard(pid, desk.getPlayerCardsInfo().get(pid));

		//记录玩家行为
		addPlayerOperate(Operate.build(pid, Operate.SEE_CARD));
	}

	/**
	 * 玩家弃牌
	 * @param pid
	 * @param waitCode
	 */
	public void playerDropCard(long pid, int waitCode) {
		WaitProtocol waitProtocol = desk.getWaitProtocol();
		//		if(waitProtocol.getPid() != pid || !waitProtocol.isHaveCode(waitCode)) {
		//			log.error("当前无法弃牌pid:{}", pid);
		//			return;
		//		}
		if(desk.getGamingPlayerInfo().get(pid) == null) {
			log.error("玩家不再游戏中pid:{}", pid);
			return;
		}
		log.info("pid:{} drop card", pid);
		dropCardHandler(pid, DropCardType.REQ_DROP_CARD_VALUE);

		//记录玩家行为
		addPlayerOperate(Operate.build(pid, Operate.DROP_CARD_REQ));
	}

	/**
	 * 通知下个玩家操作
	 */
	public void noticePlayerTurn(boolean start) {
		if(desk.getGamingPlayerInfo().size() == 1) {
			//当前只有一个玩家了，进入计算模式
			log.info("desk.getReadyPlayerInfo().size() == 1");
			desk.setWinnerPid(desk.getGamingPlayerInfo().keySet().stream().findFirst().get());
			calcuRoundResout();
			return;
		}
		if(desk.isOverMaxGameChip()) {
			//下注量超出底池最大限制量直接进入结算
			log.info("desk.isOverMaxGameChip()");
			calcuRoundResout();
			return;
		}
		long nextPid = desk.getNextPid();
		if(start) {
			nextPid = desk.getCurrentPid();
		}
		log.info("该谁操作了pid {}", nextPid);

		desk.setCurrentPid(nextPid); 

		int waitSeconds = 30;
		desk.setWaitProtocol(WaitProtocol.build(TimeUnit.SECONDS.toMillis(waitSeconds), desk.getCurrentPid(), SubCmd.Cmd_ReqSeeCard_VALUE,
				SubCmd.Cmd_ReqCall_VALUE, SubCmd.Cmd_ReqRaise_VALUE, SubCmd.Cmd_ReqDropCard_VALUE, SubCmd.Cmd_ReqShow_VALUE));

		DeskExecutorHandler.getInstance().executeDelay(TimeUnit.SECONDS.toMillis(waitSeconds), desk.getDeskId(), this::playerWithoutHandle, desk.getWaitProtocol(), "playerWithoutHandle");

		//广播该轮到哪个玩家摸牌了
		GameNoticeCmdHandler.getInstance().noticePlayerTurn(desk, desk.getCurrentPid(), desk.getWaitProtocol().getExpireTimeMillis());
	}

	/**
	 * 在规定时间内，玩家无操作直接弃牌
	 */
	private void playerWithoutHandle(WaitProtocol vo) {
		if(vo.getUuid() != desk.getWaitProtocol().getUuid()) {
			return;
		}
		long pid = vo.getPid();
		log.info("玩家pid:{}操作超时，直接弃牌", pid);
		dropCardHandler(pid, DropCardType.TIME_OUT_VALUE);

		//记录玩家行为
		addPlayerOperate(Operate.build(pid, Operate.DROP_CARD_TIME_OUT));
	}

	/**
	 * 处理玩家弃牌
	 * @param pid
	 * @param dropType {@link DropCardType}
	 */
	public void dropCardHandler(long pid, int dropType) {
		if(pid == desk.getCurrentPid()) {
			//防止桌台线程继续执行
			desk.setWaitProtocol(WaitProtocol.build());
		}
		log.info("dropCardHandler pid:{} dropType:{}", pid, dropType);
		GameCalcuHandler.getInstance().playerDropCard(pid, desk, dropType);
	}

	/**
	 * 玩家跟注
	 * @param pid
	 * @param cmdReqcallValue
	 */
	public void playerCall(long pid, int waitCode) {
		WaitProtocol waitProtocol = desk.getWaitProtocol();
		if(waitProtocol.getPid() != pid || !waitProtocol.isHaveCode(waitCode)) {
			log.warn("等候状态过时 pid:{}", pid);
			return;
		}
		if(desk.getGamingPlayerInfo().get(pid) == null) {
			log.error("玩家不再游戏中pid:{}", pid);
			return;
		}
		log.info("pid:{} 跟注", pid);
		boolean seeCard = desk.getPlayerSeeCardstInfo().get(pid) == null ? false : true;
		if(!seeCard) {
			desk.getPlayerBlindsInfo().merge(pid, 1, Integer::sum);
			if(desk.getPlayerBlindsInfo().get(pid) > desk.getConfig().getMaxLimit()) {
				//GamePushCmdHandler.getInstance().pushErrStatusMsg(pid, ClientStatus.E801);
				//直接看牌
				seeCard(pid);
				return;
			}
		}
		long betAmount = seeCard ? desk.getNowAnte() * 2 : desk.getNowAnte();
		//betAmount = (int) Math.min(betAmount, desk.getConfig().getMinChip());

		desk.getPlayerBetInfo().computeIfAbsent(pid, k-> new ArrayList<BetInfo>()).add(new BetInfo(BetType.CALL.getValue(), betAmount));
		GameNoticeCmdHandler.getInstance().noticePlayerCall(desk.getDeskId(), pid, betAmount, seeCard);
		noticePlayerTurn(false);

		//记录玩家行为
		addPlayerOperate(Operate.build(pid, Operate.CALL, betAmount));
	}

	/**
	 * 玩家加注
	 * @param pid
	 * @param waitCode
	 */
	public void playerRaise(long pid, int waitCode) {
		WaitProtocol waitProtocol = desk.getWaitProtocol();
		if(waitProtocol.getPid() != pid || !waitProtocol.isHaveCode(waitCode)) {
			log.error("当前无法加注pid:{}", pid);
			return;
		}
		if(desk.getGamingPlayerInfo().get(pid) == null) {
			log.error("玩家不再游戏中pid:{}", pid);
			return;
		}
		log.info("pid:{} 加注", pid);
		boolean seeCard = desk.getPlayerSeeCardstInfo().get(pid) == null ? false : true;
		if(!seeCard) {
			desk.getPlayerBlindsInfo().merge(pid, 1, Integer::sum);
			if(desk.getPlayerBlindsInfo().get(pid) > desk.getConfig().getMaxLimit()) {
				//直接看牌
				seeCard(pid);
				return;
			}
		}
		long betAmount = seeCard ? desk.getNowAnte() * 2  : desk.getNowAnte();
		betAmount = betAmount * 2;
		long maxBetAmount = desk.getConfig().getMaxBet();
		if(!seeCard) {
			maxBetAmount = maxBetAmount / 2;
		}
		//超出个人下注上限
		if(betAmount > maxBetAmount) {
			GamePushCmdHandler.getInstance().pushErrorMessage(pid, CommonMsg.ErrorCode.EXCEEDS_LIMIT_VALUE);
			return;
		}
		desk.setNowAnte(desk.getNowAnte() * 2);

		desk.getPlayerBetInfo().computeIfAbsent(pid, k-> new ArrayList<BetInfo>()).add(new BetInfo(BetType.RAISE.getValue(), betAmount));
		GameNoticeCmdHandler.getInstance().noticePlayerRaise(desk.getDeskId(), pid, betAmount, seeCard);

		noticePlayerTurn(false);

		//记录玩家行为
		addPlayerOperate(Operate.build(pid, Operate.RAISE, betAmount));
	}

	/**
	 * 玩家请求比牌
	 * @param pid
	 * @param cmdReqshowValue
	 */
	public void playerShow(long pid, int waitCode) {
		WaitProtocol waitProtocol = desk.getWaitProtocol();
		if(waitProtocol.getPid() != pid || !waitProtocol.isHaveCode(waitCode)) {
			log.error("当前请求比牌pid:{}", pid);
			return;
		}

		long lastPid = desk.getLastPid();
		log.info("pid:{}  lastPid:{}请求比牌", pid, lastPid);
		if(pid == lastPid) {
			log.error("出错了currentPid{}, currentSeat {}, data{}", desk.getCurrentPid(), desk.getCurrentSeat(), JSON.toJSONString(desk.getDeskSeatPlayerInfo()));
		}
		boolean seeCard = desk.getPlayerSeeCardstInfo().get(pid) == null ? false : true;
		long betAmount = seeCard ? desk.getNowAnte() * 2 : desk.getNowAnte();
		desk.getPlayerBetInfo().computeIfAbsent(pid, k-> new ArrayList<BetInfo>()).add(new BetInfo(BetType.COMPARE.getValue(), betAmount));

		//等待接受比牌时间
		int waitSeconds = 6;
		desk.setWaitProtocol(WaitProtocol.build(TimeUnit.SECONDS.toMillis(waitSeconds), lastPid, SubCmd.Cmd_ReqRefuseOrAgreeShow_VALUE));
		DeskExecutorHandler.getInstance().executeDelay(TimeUnit.SECONDS.toMillis(waitSeconds), desk.getDeskId(),
				this::playerWithoutShowHandle, desk.getWaitProtocol(), "playerWithoutShowHandle");
		//广播比牌消息
		GameNoticeCmdHandler.getInstance().noticePlayerShow(desk.getDeskId(), pid, lastPid, betAmount, desk.getWaitProtocol().getExpireTimeMillis());

		//记录玩家行为
		addPlayerOperate(Operate.buildCompare(pid, Operate.COMPARE, betAmount, lastPid));

		if(desk.getGamingPlayerInfo().size() == 2) {
			//只剩两个玩家直接比牌
			compareCards(lastPid);
			return;
		}

	}


	/**
	 * @param pid
	 * @param cmdReqrefuseshowValue
	 */
	public void playerAgreeShow(long pid, int waitCode, boolean agree) {
		WaitProtocol waitProtocol = desk.getWaitProtocol();
		if(waitProtocol.getPid() != pid || !waitProtocol.isHaveCode(waitCode)) {
			log.warn("playerAgreeShow 等待协议状态错误 pid:{}", pid);
			return;
		}

		log.info("pid:{} 是否同意比牌 {}", pid, agree);
		//广播是否同意比牌
		GameNoticeCmdHandler.getInstance().noticeRefuseOrAgreeShow(desk.getDeskId(), pid, agree);
		if(!agree) {
			//desk.getPlayerRefuseInfo().merge(pid, 1, Integer::sum);
			noticePlayerTurn(false);

			//记录玩家行为
			addPlayerOperate(Operate.buildCompare(pid, Operate.REFUSE, 0, desk.getCurrentPid()));
		}
		else {
			compareCards(pid);

			//记录玩家行为
			addPlayerOperate(Operate.buildCompare(pid, Operate.AGREE, 0, desk.getCurrentPid()));
		}

	}

	/**
	 *  比牌
	 * @param pid
	 */
	private void compareCards(long pid) {
		desk.setWaitProtocol(WaitProtocol.build());

		long winPid = desk.getCurrentPid();
		long lostPid = pid;
		//GameRobotHandler.getInstance().isChangeCard(desk, winPid, lostPid);
		boolean isWin = CardsUtils.compareJinHuaCardsResult(desk.getPlayerCardsInfo().get(desk.getCurrentPid()), desk.getPlayerCardsInfo().get(pid));
		if(isWin) {
			winPid = desk.getCurrentPid();
			lostPid = pid;
		}
		else {
			winPid = pid;
			lostPid = desk.getCurrentPid();
		}

		GamePushCmdHandler.getInstance().pushShowCardResult(this.desk, winPid, lostPid);
		GameNoticeCmdHandler.getInstance().noticePlayerShowResult(this.desk.getDeskId(), winPid, lostPid);
		//输家弃牌
		dropCardHandler(lostPid, DropCardType.COMPARE_VALUE);

		//记录玩家行为
		addPlayerOperate(Operate.build(pid, Operate.DROP_CARD_COMPARE));
	}

	/**
	 * 被比牌玩家没有回复是否比牌
	 */
	private void playerWithoutShowHandle(WaitProtocol vo) {
		if(vo.getUuid() != desk.getWaitProtocol().getUuid()) {
			return;
		}
		long pid =  vo.getPid();
		log.info("pid:{} playerWithoutShowHandle", pid);

		//		if(desk.getPlayerRefuseInfo().computeIfAbsent(pid, k-> 0)  > desk.getConfig().getMaxScore()){
		//			//超过拒绝次数直接比牌
		//			compareCards(pid);
		//			return;
		//		}
		//广播拒绝
		GameNoticeCmdHandler.getInstance().noticeRefuseOrAgreeShow(desk.getDeskId(), pid, false);

		noticePlayerTurn(false);

		//记录玩家行为
		addPlayerOperate(Operate.buildCompare(pid, Operate.REFUSE, 0, desk.getCurrentPid()));
	}

	/**
	 * 计算本局结果
	 */
	private void calcuRoundResout() {
		//计算积分
		desk.setState(DeskState.CALCULATING_VALUE);
		desk.setWaitProtocol(WaitProtocol.build());
		//推送给客户端
		noticeDeskStateChange();

		GameCalcuHandler.getInstance().produceWinLostInfo(desk);
	}

	/**
	 * 展示结算界面
	 */
	public void showPlayersCardsAndWinLost() {
		desk.setState(DeskState.RESLUT_SHOW_VALUE);
		int waitSeconds = 6;
		desk.setWaitProtocol(WaitProtocol.build(TimeUnit.SECONDS.toMillis(waitSeconds)));
		DeskExecutorHandler.getInstance().executeDelay(TimeUnit.SECONDS.toMillis(waitSeconds), desk.getDeskId(), this::waitStart, desk.getWaitProtocol(), "waitStart");
		//推送给客户端
		noticeDeskStateChange();
	}

	/**
	 * 重新开始新的一局
	 * @param vo
	 */
	private void waitStart(WaitProtocol vo) {
		//清除离线玩家
		PlayerOfflineHandler.getInstance().cleanOfflinePlayer(desk);

		//清除金币不足的玩家
		PlayerOfflineHandler.getInstance().cleanLowBalancePlayer(desk);

		//重置玩家在游戏中的状态
		resetPlayersStatus();

		//重置
		desk.reset();

		desk.setState(DeskState.WAIT_VALUE);
		desk.setWaitProtocol(WaitProtocol.build());
		noticeDeskStateChange();

		playerJoinDesk();
	}

	/**
	 * 重置玩家在游戏中的状态
	 */
	private void resetPlayersStatus() {
		Collection<Long> pids = ArrayUtils.copeCollection(desk.getGamingPlayerInfo().values());
		//清除所有在游戏中的玩家
		desk.getGamingPlayerInfo().clear();

		for (Long pid : pids) {
			GameNoticeCmdHandler.getInstance().noticePlayerStatus(desk, pid);
		}
	}

	/**
	 * @return
	 */
	public boolean isAllRobot() {
		for (Long pid : desk.getDeskSeatPlayerInfo().values()) {
			Player player = PlayerMgr.getInstance().getPlayer(pid);
			if(player != null && !player.getUser().isRobot()) {
				return false;
			}
		}
		for (Long pid : desk.getLookOnPlayerInfo().values()) {
			Player player = PlayerMgr.getInstance().getPlayer(pid);
			if(player != null && !player.getUser().isRobot()) {
				return false;
			}
		}
		return true;
	}

	/**
	 *让没有准备的玩家站起
	 */
	private void standUpNoReady() {
		for (Entry<Integer, Long> entry : desk.getDeskSeatPlayerInfo().entrySet()) {
			Long pid = entry.getValue();
			if(desk.getReadyPlayerInfo().get(pid) == null) {
				standUp(pid);
			}
			else {
				desk.getGamingPlayerInfo().put(pid, pid);
				log.info("玩家pid:{} seat:{} 在desk:{} 开始游戏", pid, entry.getKey(), desk.getDeskId());
			}
		}

		Collection<Long> pids = ArrayUtils.copeCollection(desk.getGamingPlayerInfo().values());
		//清除所有玩家的准备状态
		desk.getReadyPlayerInfo().clear();

		for (Long pid : pids) {
			GameNoticeCmdHandler.getInstance().noticePlayerStatus(desk, pid);
		}
	}

	/**
	 * 站起
	 * @param pid
	 */
	public void standUp(long pid) {
		for (Entry<Integer, Long> entry : desk.getDeskSeatPlayerInfo().entrySet()) {
			if(entry.getValue() == pid) {
				desk.getDeskSeatPlayerInfo().remove(entry.getKey());
			}
		}
		desk.getLookOnPlayerInfo().put(pid, pid);
		desk.getReadyPlayerInfo().remove(pid);
		desk.getGamingPlayerInfo().remove(pid);
		GameNoticeCmdHandler.getInstance().noticeSitUp(desk.getDeskId(), pid);

		Player player = PlayerMgr.getInstance().getPlayer(pid);
		if(player == null) {
			log.error("player is null");
			return;
		}
	}

	/**
	 * @param operate
	 */
	private void addPlayerOperate(Operate operate){
		desk.getPlayerOperateList().add(operate);
	}

	private void noticeDeskStateChange() {
		//推送给客户端
		GameNoticeCmdHandler.getInstance().noticeDeskStateChange(desk.getDeskId(), desk.getState(), desk.getWaitProtocol().getExpireTimeMillis());
	}

}

