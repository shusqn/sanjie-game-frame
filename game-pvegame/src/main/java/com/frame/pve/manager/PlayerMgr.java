package com.frame.pve.manager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.frame.enums.GameType;
import com.frame.manager.BasePlayerMgr;

public class PlayerMgr extends BasePlayerMgr{
	private static Map<Integer, PlayerMgr> instanceMap = new ConcurrentHashMap<Integer, PlayerMgr>();
	public static PlayerMgr getInstance(GameType gameType) {
		PlayerMgr instance = instanceMap.get(gameType.getType());
		if(instance == null) {
			instance = new PlayerMgr();
			instanceMap.put(gameType.getType(), instance);
		}
		return instance;
	}
	//==================================
}
