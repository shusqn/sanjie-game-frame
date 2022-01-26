package com.frame.teenpatti.manager;

import java.util.List;

import com.frame.entity.Room;
import com.frame.executor.TimerExecutorHandler;
import com.frame.id.DeskIdBuilder;
import com.frame.manager.AbstractDeskMgr;
import com.frame.protobuf.CommonMsg.GameType;
import com.frame.service.ConfigService;
import com.frame.teenpatti.TeenpattiMrg;
import com.frame.teenpatti.model.TeenpattiDesk;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * DeskMgr.java
 * @author Sanjie
 * @date 2021-09-30 09:53
 * @version 1.0.0
 */
@Slf4j
public class DeskMgr extends AbstractDeskMgr<TeenpattiDesk> {
	@Getter
	private static DeskMgr instance = new DeskMgr();
	
	@Override
	public void initAllDesk() {
		List<Room> configList = ConfigService.getInstance().findRoomsByGameType(GameType.GAME_TEENPATTI_VALUE);
		for (Room config : configList) {
			ConfigMgr.getInstance().put(config.getRoomId(), config);
			buildDesk(config.getRoomId());
			log.info("start game desk :{}", config.toString());
		}
		
		TimerExecutorHandler.getInstance().scheduleAtFixedRate(0, this::timeDoSortDesk, 60);
	}
	
	/**
	 * @param roomId
	 * @param exceptDeskId
	 * @param isRobot
	 * @return
	 */
	public TeenpattiDesk faskGetDesk(int roomId, int exceptDeskId, boolean isRobot) {
		List<TeenpattiDesk> list = getDeskList(roomId);
		if(list != null) {
			for (int i = 0; i < list.size(); i++) {
				TeenpattiDesk desk = list.get(i);
				if(desk.getDeskId() != exceptDeskId && desk.getAllPids().size() < desk.getSeats().length) {
					return desk;
				}
			}
		}
		return buildDesk(roomId);
	}
	
	/**
	 * @param roomId
	 * @return
	 */
	private TeenpattiDesk buildDesk(int roomId) {
		Room config = ConfigMgr.getInstance().get(roomId);
		if(config == null) {
			log.error("找不到config:{}的配置文件", roomId);
			return null;
		}
		List<TeenpattiDesk> list = getDeskList(roomId);
		int deskIndex = (list == null) ? 0 : list.size();
		log.info("deskIndex:{}", deskIndex);
		int deskId = DeskIdBuilder.getDeskId(TeenpattiMrg.getInstance().getServerId(), config.getRoomType(), deskIndex);
		if(getDesk(deskId) == null) {
			TeenpattiDesk desk = new TeenpattiDesk(deskId, config);
			addDesk(desk);
			log.info("创建游戏桌台gameType:{} deskId：{} roomId: {} ",config.getGameType(), deskId, config.getRoomId());
			return desk;
		}
		return null;
	}
	
}
