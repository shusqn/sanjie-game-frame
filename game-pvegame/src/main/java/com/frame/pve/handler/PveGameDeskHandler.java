package com.frame.pve.handler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import com.frame.enums.GameType;
import com.frame.executor.TimerExecutorHandler;
import com.frame.executor.VoidFunction;
import com.frame.mobel.mq.AskRobotLogin;
import com.frame.mobel.mq.AskRobotLoginOut;
import com.frame.mobel.mq.MqTransmissionData;
import com.frame.model.Player;
import com.frame.model.mq.GameCardsResult;
import com.frame.model.mq.GamePlayerCards;
import com.frame.model.mq.GameReslut;
import com.frame.model.mq.PVERoomState;
import com.frame.protobuf.PveGameMsg;
import com.frame.pve.PveGameMrg;
import com.frame.pve.handler.cmd.GameNoticeCmdHandler;
import com.frame.pve.manager.DeskMgr;
import com.frame.pve.manager.PlayerMgr;
import com.frame.pve.model.PveGameDesk;
import com.frame.pve.rocketmq.RocketMqSender;
import com.google.gson.Gson;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PveGameDeskHandler  {
	private static Map<Integer, PveGameDeskHandler> instanceMap = new ConcurrentHashMap<Integer, PveGameDeskHandler>();
	public static PveGameDeskHandler getInstance(GameType gameType) {
		PveGameDeskHandler instance = instanceMap.get(gameType.getType());
		if(instance == null) {
			instance = new PveGameDeskHandler();
			instance.gameType = gameType;
			instanceMap.put(gameType.getType(), instance);
			
			instance.init();
		}
		return instance;
	}
	private GameType gameType;
	//==================================
	
	@Getter
	private  Map<String, Consumer<MqTransmissionData>> nameMap = new ConcurrentHashMap<>();
	
	private Gson gson = new Gson();
	/**
	 * 
	 */
	private void init() {
		nameMap.put(PVERoomState.class.getSimpleName(), this::getPVERoomState);
		nameMap.put(GamePlayerCards.class.getSimpleName(), this::getGamePlayerCards);
		nameMap.put(GameCardsResult.class.getSimpleName(), this::getGameCardsResult);
		nameMap.put(GameReslut.class.getSimpleName(), this::getGameReslut);
	}
	
	/**
	 * @param desk
	 */
	public void robotManager(PveGameDesk desk) {
		for (Long pid : desk.getLookOnPlayerInfo().values()) {
			Player player = PlayerMgr.getInstance(gameType).getPlayer(pid);
			if(player != null && player.getUser().isRobot()) {
				log.info("deskId:{} clean lookonPlayers:{}", desk.getDeskId(), player.getPid());
				RocketMqSender.getInstance(gameType).pushAskRobotData2RobotServer(
						AskRobotLoginOut.builder()
						.uid(player.getPid())
						.serverType(gameType.getServerType().getType())
						.sid(PveGameMrg.getInstance(gameType).getServerId())
						.build());
			}
		}
		
		int count = desk.getSeats().length - desk.getDeskSeatPlayerInfo().size() - 1;
		if(count < 0) {
			for (Long pid : desk.getDeskSeatPlayerInfo().values()) {
				Player player = PlayerMgr.getInstance(gameType).getPlayer(pid);
				if(player != null && player.getUser().isRobot()) {
					RocketMqSender.getInstance(gameType).pushAskRobotData2RobotServer(
							AskRobotLoginOut.builder()
							.uid(player.getPid())
							.serverType(gameType.getServerType().getType())
							.sid(PveGameMrg.getInstance(gameType).getServerId())
							.build());
					break;
				}
			}
		}
		else if(count >=1){
			TimerExecutorHandler.getInstance().executeDelay((long) (Math.random() * 1500), 0, new VoidFunction() {
				@Override
				public void handle() {
					RocketMqSender.getInstance(gameType).pushAskRobotData2RobotServer(
							AskRobotLogin.builder()
							.roomId(desk.getConfig().getRoomId())
							.serverType(gameType.getServerType().getType())
							.deskId(desk.getDeskId())
							.sid(PveGameMrg.getInstance(gameType).getServerId())
							.build());
				}
			}, "pushAskRobotData2RobotServer");
		}
	}
	
	
	/**
	 * @param data
	 */
	private void getPVERoomState(MqTransmissionData data) {
		List<PveGameDesk> list = DeskMgr.getInstance(gameType).getDeskList(Integer.valueOf(data.getId()+""));
		if(list == null) {
			return;
		}
		PVERoomState roomState = gson.fromJson(data.getResult(), PVERoomState.class);
		for (PveGameDesk desk : list) {
			desk.setState(roomState.getState());
			desk.setWaitProtocol(roomState.getWaitProtocol());
			desk.setRoundId(roomState.getRoundId());
			
			//广播游戏状态
			GameNoticeCmdHandler.getInstance(gameType).noticeDeskStateChange(desk.getDeskId(), desk.getState(), desk.getWaitProtocol().getExpireTimeMillis());
			
			if(roomState.getState() == PveGameMsg.DeskState.START_COUNTDOWN_VALUE) {
				PlayerOfflineHandler.getInstance(gameType).cleanLowBalancePlayer(desk);
				PlayerOfflineHandler.getInstance(gameType).cleanOfflinePlayer(desk);
				
				desk.reset();
				
				robotManager(desk);
			}
		}
	}
	
	/**
	 * @param data
	 */
	private void getGamePlayerCards(MqTransmissionData data) {
		List<PveGameDesk> list = DeskMgr.getInstance(gameType).getDeskList(Integer.valueOf(data.getId()+""));
		if(list == null) {
			return;
		}
		GamePlayerCards playerCards = gson.fromJson(data.getResult(), GamePlayerCards.class);
		for (PveGameDesk desk : list) {
			desk.setPlayerCards(playerCards.getPlayerCards());
			
			//广播玩家手牌
			GameNoticeCmdHandler.getInstance(gameType).noticePlayerCards(desk);
		}
	}
	
	/**
	 * @param data
	 */
	private void getGameCardsResult(MqTransmissionData data) {
		List<PveGameDesk> list = DeskMgr.getInstance(gameType).getDeskList(Integer.valueOf(data.getId()+""));
		if(list == null) {
			return;
		}
		GameCardsResult cardsResult = gson.fromJson(data.getResult(), GameCardsResult.class);
		for (PveGameDesk desk : list) {
			desk.setCardsResult(cardsResult.getCardsResult());
			
			GameNoticeCmdHandler.getInstance(gameType).noticeCardsResult(desk);
		}
	}
	
	/**
	 * @param data
	 */
	private void getGameReslut(MqTransmissionData data) {
		List<PveGameDesk> list = DeskMgr.getInstance(gameType).getDeskList(Integer.valueOf(data.getId()+""));
		if(list == null) {
			return;
		}
		GameReslut gameReslut = gson.fromJson(data.getResult(), GameReslut.class);
		for (PveGameDesk desk : list) {
			desk.setResluts(gameReslut.getResluts());
			
			GameNoticeCmdHandler.getInstance(gameType).noticeGameReslut(desk);
			
			GameCalcuHandler.getInstance(gameType).gameCalcu(desk);
		}
	}
	
}
