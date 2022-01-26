package com.frame.teenpatti.rocketmq;

import java.util.List;

import com.frame.mobel.mq.MqTransmissionData;
import com.frame.rocketmq.base.AbstractConsumerService;
import com.frame.router.BaseRouterHander;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * TODO
 * @author Sanjie
 * @date 2021-09-07 16:58
 * @version 1.0
 */
@Slf4j
public final class RocketMqReceive extends AbstractConsumerService {
	@Getter
	private static RocketMqReceive instance = new RocketMqReceive();
	@Getter
	private static BaseRouterHander<String, String> mqRouterHandler = new BaseRouterHander<String, String>() {};
	
	private RocketMqReceive() {}
	
	@Override
	protected void onMessage(List<String> messageList) {
		for (String message : messageList) {
			receiveMqData(message);
		}
	}
	
	/**
	 * @param message
	 */
	public void receiveMqData(String message) {
		MqTransmissionData mqdata;
		try {
			mqdata = gson.fromJson(message, MqTransmissionData.class);
			String name = mqdata.getName();
			String jsondata = mqdata.getResult();
			mqRouterHandler.executeHandler(name, jsondata);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

}
