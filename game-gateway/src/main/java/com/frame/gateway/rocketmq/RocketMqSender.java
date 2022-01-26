package com.frame.gateway.rocketmq;

import com.frame.constant.MqTopic;
import com.frame.mobel.ProtoBuf;
import com.frame.mobel.mq.ChanelClose;
import com.frame.mobel.mq.HeartBeat;
import com.frame.mobel.mq.MqTransmissionData;
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
	 * @param pid
	 * @param targetSid
	 * @param pb
	 * @return
	 */
	public boolean pushPb2Server(int targetSid, ProtoBuf pb){
		MqTransmissionData data = MqTransmissionData.build(ProtoBuf.class.getSimpleName(), gson.toJson(pb));
		return this.sendMessage(MqTopic.PUSH_TOPIC_SERVER + targetSid, data, pb.getPid());
	}
	
	/**
	 * @param targetSid
	 * @param pb
	 * @return
	 */
	public boolean noticeChanelClose2Server(long pid){
		MqTransmissionData data = MqTransmissionData.build(ChanelClose.class.getSimpleName(), gson.toJson(ChanelClose.build(pid)));
		return this.sendMessage(MqTopic.BROADCAST_TOPIC_SERVER, data, pid);
	}
	
	/**
	 * @param targetSid
	 * @param pb
	 * @return
	 */
	public boolean noticeHeartBeat2Server(long pid){
		MqTransmissionData data = MqTransmissionData.build(HeartBeat.class.getSimpleName(), gson.toJson(HeartBeat.build(pid)));
		return this.sendMessage(MqTopic.BROADCAST_TOPIC_SERVER, data, pid);
	}

}
