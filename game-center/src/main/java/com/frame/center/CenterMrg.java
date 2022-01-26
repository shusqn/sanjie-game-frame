package com.frame.center;

import java.util.List;

import com.frame.center.hander.RedisCenterServerHandler;
import com.frame.center.rocketmq.BroadcastRocketMqReceive;
import com.frame.center.rocketmq.RocketMqReceive;
import com.frame.center.rocketmq.RocketMqSender;
import com.frame.constant.MqConsumerGroup;
import com.frame.constant.MqProducer;
import com.frame.constant.MqTopic;
import com.frame.entity.Room;
import com.frame.enums.ServerType;
import com.frame.id.DeskIdBuilder;
import com.frame.id.IdBuilder;
import com.frame.service.ConfigService;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * GatewayMrg.java
 * @author Sanjie
 * @date 2021-09-24 12:24
 * @version 1.0.0
 */
@Slf4j
public class CenterMrg {
	@Getter
	private static CenterMrg instance = new CenterMrg();
	
	@Getter
	private IdBuilder idGenerator = null; 
	@Getter
	private int serverId;
	@Getter
	private List<Room> configList;
	/**
	 * 初始化gateway 服务器相关配置
	 */
	public void start(String nameServerAddr, int serversGroupId) {
		try {
			int serverType = ServerType.CENTER.getType();
			serverId = DeskIdBuilder.getServerId(serversGroupId, serverType);
			
			//初始化id生成器
			idGenerator = new IdBuilder(serverId);
			//初始化mq
			RocketMqSender.getInstance().start(nameServerAddr, MqProducer.PRODUCER_GROUP + serverType, 16, null);
			RocketMqReceive.getInstance().start(
					nameServerAddr, 
					MqTopic.PUSH_TOPIC_SERVER +serverId, 
					MqConsumerGroup.CONSUMER_PUSH_SERVER+serverId,
					null);
			BroadcastRocketMqReceive.getInstance().start(
					nameServerAddr, 
					MqTopic.BROADCAST_TOPIC_SERVER, 
					MqConsumerGroup.CONSUMER_BROADCAST_SERVER + serverId, 
					null);
			
			configList = ConfigService.getInstance().findAll();
			
			//注册服务器信息到redis
			RedisCenterServerHandler.getInstance().register2Redis(serverId, serverType);
			
			log.info("CenterMrg {} start success", serverId);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			System.exit(0);
		}
	}
	
}
