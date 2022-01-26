package com.frame.pve.manager;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import com.frame.constant.RedisKey;
import com.frame.dao.RedisDao;
import com.frame.entity.Room;
import com.frame.enums.GameType;
import com.frame.id.DeskIdBuilder;
import com.frame.manager.AbstractDeskMgr;
import com.frame.mobel.mq.MqTransmissionData;
import com.frame.model.mq.GameCardsResult;
import com.frame.model.mq.GamePlayerCards;
import com.frame.model.mq.GameReslut;
import com.frame.model.mq.PVERoomState;
import com.frame.pve.PveGameMrg;
import com.frame.pve.handler.PveGameDeskHandler;
import com.frame.pve.model.PveGameDesk;
import com.frame.pve.router.MqPveDataRouter;
import com.frame.service.ConfigService;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DeskMgr extends AbstractDeskMgr<PveGameDesk> {
	private static Map<Integer, DeskMgr> instanceMap = new ConcurrentHashMap<Integer, DeskMgr>();
	public static DeskMgr getInstance(GameType gameType) {
		DeskMgr instance = instanceMap.get(gameType.getType());
		if(instance == null) {
			instance = new DeskMgr();
			instance.gameType = gameType;
			instanceMap.put(gameType.getType(), instance);
		}
		return instance;
	}
	private GameType gameType;
	//==================================
	
	@Override
	public void initAllDesk() {
		List<Room> configList = ConfigService.getInstance().findRoomsByGameType(gameType.getType());
		for (Room config : configList) {
			ConfigMgr.getInstance().put(config.getRoomId(), config);
			MqPveDataRouter.getInstance().registHandler(config.getRoomId(), this::updataFromMQ);
			
			buildDesk(config.getRoomId());
		}
	}
	
	/**
	 * @param data
	 */
	private void updataFromMQ(MqTransmissionData data) {
		log.info("data name:{} result:{}", data.getName(), data.getResult());
		Consumer<MqTransmissionData> handler = PveGameDeskHandler.getInstance(gameType).getNameMap().get(data.getName());
		if(handler == null) {
			log.error("找不到处理{}的handler", data.getName());
			return;
		}
		handler.accept(data);
	}
	
	/**
	 * @param roomId
	 * @param exceptDeskId
	 * @param isRobot
	 * @return
	 */
	public PveGameDesk faskGetDesk(int roomId, int exceptDeskId, boolean isRobot) {
		List<PveGameDesk> list = getDeskList(roomId);
		if(list != null) {
			for (int i = 0; i < list.size(); i++) {
				PveGameDesk desk = list.get(i);
				if(desk.getDeskId() != exceptDeskId && desk.getAllPids().size() < desk.getSeats().length ) {
					return desk;
				}
			}
		}
		return buildDesk(roomId);
	}
	
	private static Gson gson = new Gson();
	/**
	 * @param roomId
	 * @return
	 */
	private PveGameDesk buildDesk(int roomId) {
		Room config = ConfigMgr.getInstance().get(roomId);
		if(config == null) {
			log.error("找不到config:{}的配置文件", roomId);
			return null;
		}
		
		List<PveGameDesk> list = getDeskList(roomId);
		int deskIndex = (list == null) ? 0 : list.size();
		log.info("deskIndex:{}", deskIndex);
		int deskId = DeskIdBuilder.getDeskId(PveGameMrg.getInstance(gameType).getServerId(), config.getRoomType(), deskIndex);
		
		if(getDesk(deskId) == null) {
			PveGameDesk desk = new PveGameDesk(deskId, config);
			String  pveRoomStateData = (String) RedisDao.getInstance().getTemplate().opsForHash().get(RedisKey.ROOM_MAP_KEY + config.getRoomId(), 
					PVERoomState.class.getSimpleName());
			if(pveRoomStateData != null) {
				PVERoomState  pveRoomState = gson.fromJson(pveRoomStateData, PVERoomState.class);
				desk.setWaitProtocol(pveRoomState.getWaitProtocol());
				desk.setState(pveRoomState.getState());
				desk.setRoundId(pveRoomState.getRoundId());
			}
			String gamePlayerCardsdata= (String) RedisDao.getInstance().getTemplate().opsForHash().get(RedisKey.ROOM_MAP_KEY + config.getRoomId(), 
					GamePlayerCards.class.getSimpleName());
			if(gamePlayerCardsdata != null) {
				GamePlayerCards  gamePlayerCards = gson.fromJson(gamePlayerCardsdata, GamePlayerCards.class);
				desk.setPlayerCards(gamePlayerCards.getPlayerCards());
			}
			String gameCardsResultData = (String) RedisDao.getInstance().getTemplate().opsForHash().get(RedisKey.ROOM_MAP_KEY + config.getRoomId(), 
					GameCardsResult.class.getSimpleName());
			if(gameCardsResultData != null) {
				GameCardsResult  gameCardsResult = gson.fromJson(gameCardsResultData, GameCardsResult.class);
				desk.setCardsResult(gameCardsResult.getCardsResult());
			}
			String  gameReslutData = (String) RedisDao.getInstance().getTemplate().opsForHash().get(RedisKey.ROOM_MAP_KEY + config.getRoomId(), 
					GameReslut.class.getSimpleName());
			if(gameReslutData != null) {
				GameReslut  gameReslut = gson.fromJson(gameReslutData, GameReslut.class);
				desk.setResluts(gameReslut.getResluts());
			}
			
			addDesk(desk);
			log.info("创建游戏桌台 deskId：{} roomId: {} ", deskId, config.getRoomId());
			return desk;
		}
		return null;
	}
	
}
