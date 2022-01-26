package com.frame.pve.router;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.frame.enums.GameType;
import com.frame.mobel.ProtoBuf;
import com.frame.router.BaseRouterHander;

/**
 * NettyCmdRouter.java
 * @author Sanjie
 * @date 2021-09-10 10:26
 * @version 1.0.0
 */
public final class MqPbCmdRouter extends BaseRouterHander<Integer, ProtoBuf>{
	private static Map<Integer, MqPbCmdRouter> instanceMap = new ConcurrentHashMap<Integer, MqPbCmdRouter>();
	public static MqPbCmdRouter getInstance(GameType gameType) {
		MqPbCmdRouter instance = instanceMap.get(gameType.getType());
		if(instance == null) {
			instance = new MqPbCmdRouter();
			instanceMap.put(gameType.getType(), instance);
		}
		return instance;
	}
	//==================================
}
