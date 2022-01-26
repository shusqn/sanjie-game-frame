package com.frame.teenpatti;

import com.frame.constant.MqConsumerGroup;
import com.frame.constant.MqProducer;
import com.frame.constant.MqTopic;
import com.frame.enums.ServerType;
import com.frame.id.DeskIdBuilder;
import com.frame.id.IdBuilder;
import com.frame.teenpatti.handler.RedisTeenpattiServerHandler;
import com.frame.teenpatti.manager.DeskMgr;
import com.frame.teenpatti.rocketmq.BroadcastRocketMqReceive;
import com.frame.teenpatti.rocketmq.RocketMqReceive;
import com.frame.teenpatti.rocketmq.RocketMqSender;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * GatewayMrg.java
 * @author Sanjie
 * @date 2021-09-24 12:24
 * @version 1.0.0
 */
@Slf4j
public class TeenpattiMrg {
	@Getter
	private static TeenpattiMrg instance = new TeenpattiMrg();
	
	@Getter
	private IdBuilder idBuilder = null; 
	@Getter
	private int serverId;
	/**
	 * 初始化gateway 服务器相关配置
	 */
	public void start(String nameServerAddr, int serversGoupId) {
		try {	
			int serverType = ServerType.TEENPATTI.getType();
			serverId = DeskIdBuilder.getServerId(serversGoupId, serverType);
			
			//初始化id生成器
			idBuilder = new IdBuilder(serverId);
			
			//初始化mq
			RocketMqSender.getInstance().start(nameServerAddr, MqProducer.PRODUCER_GROUP+serverType, 16, null);
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
			
			//注册服务器信息到redis
			RedisTeenpattiServerHandler.getInstance().register2Redis(serverId, serverType);
			
			//初始化游戏桌台
			DeskMgr.getInstance().initAllDesk();
			
			log.info("TeenpattiMrg {} start success", serverId);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			System.exit(0);
		}
	}
	
}
