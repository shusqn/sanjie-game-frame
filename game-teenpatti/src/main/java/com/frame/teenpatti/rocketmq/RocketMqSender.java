package com.frame.teenpatti.rocketmq;

import com.frame.constant.MqTopic;
import com.frame.mobel.ProtoBuf;
import com.frame.mobel.mq.MqTransmissionData;
import com.frame.rocketmq.base.AbstractProducerService;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * TODO
 * @author Sanjie
 * @date 2021-09-07 16:46
 * @version 1.0
 */
@Slf4j
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
	 * @param <T>
	 * @param askRobotData
	 * @return
	 */
	public <T> boolean pushAskRobotData2RobotServer(T askRobotData){
		MqTransmissionData data = MqTransmissionData.build(askRobotData.getClass().getSimpleName(), gson.toJson(askRobotData));
		log.info(data.toString());
		return this.sendMessage(MqTopic.BROADCAST_TOPIC_ROBOT_SERVER, data, askRobotData.hashCode());
	}
	
}
