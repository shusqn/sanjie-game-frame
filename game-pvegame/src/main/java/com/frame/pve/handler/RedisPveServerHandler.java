package com.frame.pve.handler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.frame.enums.GameType;
import com.frame.handler.BaseRedisServerHandler;
import com.frame.router.IDestroyHandler;

/**
 * RedisServerHandler.java
 * @author Sanjie
 * @date 2021-09-27 16:44
 * @version 1.0.0
 */
public class RedisPveServerHandler extends BaseRedisServerHandler implements IDestroyHandler{
	private static Map<Integer, RedisPveServerHandler> instanceMap = new ConcurrentHashMap<Integer, RedisPveServerHandler>();
	public static RedisPveServerHandler getInstance(GameType gameType) {
		RedisPveServerHandler instance = instanceMap.get(gameType.getType());
		if(instance == null) {
			instance = new RedisPveServerHandler();
			instanceMap.put(gameType.getType(), instance);
		}
		return instance;
	}
	//==================================
}
