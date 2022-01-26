package com.frame.pve.rocketmq;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.frame.enums.GameType;
import com.frame.pve.handler.cmd.GamePushCmdHandler;
import com.frame.rocketmq.base.AbstractConsumerService;

import lombok.extern.slf4j.Slf4j;

/**
 * BroadcastRocketMqReceive.java
 * @author Sanjie
 * @date 2021-09-27 12:22
 * @version 1.0.0
 */
@Slf4j
public final class BroadcastRocketMqReceive extends AbstractConsumerService{
	private static Map<Integer, BroadcastRocketMqReceive> instanceMap = new ConcurrentHashMap<Integer, BroadcastRocketMqReceive>();
	public static BroadcastRocketMqReceive getInstance(GameType gameType) {
		BroadcastRocketMqReceive instance = instanceMap.get(gameType.getType());
		if(instance == null) {
			instance = new BroadcastRocketMqReceive();
			instance.gameType = gameType;
			instanceMap.put(gameType.getType(), instance);
		}
		return instance;
	}
	private GameType gameType;
	//==================================
	
	@Override
	protected void onMessage(List<String> messageList) {
		for (String message : messageList) {
			RocketMqReceive.getInstance(gameType).receiveMqData(message);
		}
	}
}
