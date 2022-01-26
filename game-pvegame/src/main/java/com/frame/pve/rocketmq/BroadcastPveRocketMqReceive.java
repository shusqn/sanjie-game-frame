package com.frame.pve.rocketmq;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.frame.enums.GameType;
import com.frame.mobel.mq.MqTransmissionData;
import com.frame.pve.router.MqPveDataRouter;
import com.frame.rocketmq.base.AbstractConsumerService;

import lombok.extern.slf4j.Slf4j;

/**
 * BroadcastRocketMqReceive.java
 * @author Sanjie
 * @date 2021-09-27 12:22
 * @version 1.0.0
 */
@Slf4j
public final class BroadcastPveRocketMqReceive extends AbstractConsumerService{
	private static Map<Integer, BroadcastPveRocketMqReceive> instanceMap = new ConcurrentHashMap<Integer, BroadcastPveRocketMqReceive>();
	public static BroadcastPveRocketMqReceive getInstance(GameType gameType) {
		BroadcastPveRocketMqReceive instance = instanceMap.get(gameType.getType());
		if(instance == null) {
			instance = new BroadcastPveRocketMqReceive();
			instanceMap.put(gameType.getType(), instance);
		}
		return instance;
	}
	//==================================
	
	@Override
	protected void onMessage(List<String> messageList) {
		for (String message : messageList) {
			MqTransmissionData data = gson.fromJson(message, MqTransmissionData.class);
			MqPveDataRouter.getInstance().executeHandler((int) data.getId(), data);
		}
	}
}
