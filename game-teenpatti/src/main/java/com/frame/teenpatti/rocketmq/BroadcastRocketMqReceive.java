package com.frame.teenpatti.rocketmq;

import java.util.List;

import com.frame.rocketmq.base.AbstractConsumerService;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * BroadcastRocketMqReceive.java
 * @author Sanjie
 * @date 2021-09-27 12:22
 * @version 1.0.0
 */
@Slf4j
public final class BroadcastRocketMqReceive extends AbstractConsumerService{
	@Getter
	private static BroadcastRocketMqReceive instance = new BroadcastRocketMqReceive();
	private BroadcastRocketMqReceive() {}
	
	@Override
	protected void onMessage(List<String> messageList) {
		for (String message : messageList) {
			RocketMqReceive.getInstance().receiveMqData(message);
		}
	}
}
