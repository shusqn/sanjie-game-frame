package com.rooollerslot.rocketmq;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.frame.mobel.mq.MqTransmissionData;
import com.frame.rocketmq.base.AbstractConsumerService;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * TODO
 * @author Sanjie
 * @date 2019-09-10 14:31
 * @version 1.0
 */
//@RocketMQMessageListener(
//		consumeMode = ConsumeMode.ORDERLY, 
//		selectorType = SelectorType.TAG,
//		messageModel = org.apache.rocketmq.spring.annotation.MessageModel.CLUSTERING, 
//		consumerGroup = "consumer_exchange_mongo_record", 
//		consumeThreadMax = 5,
//		topic = "sequence_exchange_mongo_record")
@Slf4j
public class RocketMqReceive extends AbstractConsumerService{
	private static final Map<String, Class<?>> nameMap = new ConcurrentHashMap<>();

	static{
	}
	
	@Getter
	private static RocketMqReceive instance = new RocketMqReceive();
	private RocketMqReceive() {}
	
	private void onMessage(String message) {
		MqTransmissionData mqdata;
		try {
			mqdata = gson.fromJson(message, MqTransmissionData.class);
			Class<?> clazz = nameMap.get(mqdata.getName());
			if(clazz != null) {
				log.info("name:{} jsondata:{}", clazz.getSimpleName(), mqdata.getResult());
				//MongoDao.getInstance().getMongoTemplate().save(gson.fromJson(jsondata, clazz), clazz.getSimpleName());
			}
			else {
				log.error("type:{} data:{}不可知数据, 没有对应的实体", mqdata.getName(), message);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	protected void onMessage(List<String> messageList) {
		for (String message : messageList) {
			onMessage(message);
		}		
	}

}

