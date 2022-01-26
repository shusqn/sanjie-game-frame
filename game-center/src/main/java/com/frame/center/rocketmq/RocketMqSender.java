package com.frame.center.rocketmq;

import com.frame.constant.MqTopic;
import com.frame.mobel.ProtoBuf;
import com.frame.mobel.mq.MqTransmissionData;
import com.frame.rocketmq.base.AbstractProducerService;
import com.frame.utils.Ggson;

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
	 * @param pid
	 * @param targetSid
	 * @param pb
	 * @return
	 */
	public boolean pushPb2Gateway(ProtoBuf pb){
		MqTransmissionData data = MqTransmissionData.build(ProtoBuf.class.getSimpleName(), gson.toJson(pb));
		return this.sendMessage(MqTopic.PUSH_TOPIC_GATEWAY + pb.getGsid(), data, pb.getPid());
	}
	
	/**
	 * @param pb
	 * @return
	 */
	public boolean noticePb2Gateway(ProtoBuf pb){
		MqTransmissionData data = MqTransmissionData.build(ProtoBuf.class.getSimpleName(), gson.toJson(pb));
		return this.sendMessage(MqTopic.BROADCAST_TOPIC_GATEWAY, data, pb.getPid());
	}
}
