package com.frame.dt.builder;

import java.util.List;

import com.frame.entity.Room;
import com.frame.enums.GameType;
import com.frame.service.ConfigService;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PveGameBuilderMgr {
	@Getter
	private final static PveGameBuilderMgr instance = new PveGameBuilderMgr();
	/**
	 * 初始化
	 */
	public void init() {
		List<Room> configList = ConfigService.getInstance().findRoomsByGameType(GameType.DT.getType());
		for (Room config : configList) {
			new PveGameBuilder(config);
			log.info("start DragonTigerBuilder :{}", config.toString());
		}
	}
}
