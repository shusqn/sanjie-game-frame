package com.frame.pve.rocketmq;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import com.frame.enums.GameType;
import com.frame.mobel.mq.MqTransmissionData;
import com.frame.pve.router.MqGameDataRouter;
import com.frame.rocketmq.base.AbstractConsumerService;

import lombok.extern.slf4j.Slf4j;

/**
 * TODO
 * @author Sanjie
 * @date 2021-09-07 16:58
 * @version 1.0
 */
@Slf4j
public final class RocketMqReceive extends AbstractConsumerService {
	private static Map<Integer, RocketMqReceive> instanceMap = new ConcurrentHashMap<Integer, RocketMqReceive>();
	public static RocketMqReceive getInstance(GameType gameType) {
		RocketMqReceive instance = instanceMap.get(gameType.getType());
		if(instance == null) {
			instance = new RocketMqReceive();
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
			receiveMqData(message);
		}
	}
	
	/**
	 * @param message
	 */
	public void receiveMqData(String message) {
		log.info("receiveMqData:{}", message);
		MqTransmissionData mqdata;
		try {
			mqdata = gson.fromJson(message, MqTransmissionData.class);
			String name = mqdata.getName();
			String jsondata = mqdata.getResult();
			Consumer<String> handler = MqGameDataRouter.getInstance(gameType).getHandler(name);
			if(handler != null) {
				handler.accept(jsondata);
			}
			else {
				log.error("cmd:{} handler 未注册", name);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

}
