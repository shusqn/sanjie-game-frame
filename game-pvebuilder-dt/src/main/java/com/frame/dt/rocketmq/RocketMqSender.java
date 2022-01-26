package com.frame.dt.rocketmq;

import com.frame.constant.MqTopic;
import com.frame.mobel.mq.MqTransmissionData;
import com.frame.mongodto.BaseCollection;
import com.frame.rocketmq.base.AbstractProducerService;

import lombok.Getter;

/**
 * TODO
 * @author Sanjie
 * @date 2021-09-07 16:46
 * @version 1.0
 */
public final class RocketMqSender  extends AbstractProducerService{
	@Getter
	private static RocketMqSender instance = new RocketMqSender();
	private RocketMqSender() {}
	
	/**
	 * @param <T>
	 * @param mqdata
	 * @param roomId
	 * @return
	 */
	public <T extends BaseCollection>boolean noticePveGames(T mqdata, int roomId){
		MqTransmissionData data = MqTransmissionData.build(mqdata.getClass().getSimpleName(), gson.toJson(mqdata));
	    data.setId(roomId);
		return this.sendMessage(MqTopic.BROADCAST_TOPIC_PVE , data, roomId);
	}
}
