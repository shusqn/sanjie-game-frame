package com.frame.gateway;

import com.frame.constant.MqConsumerGroup;
import com.frame.constant.MqProducer;
import com.frame.constant.MqTopic;
import com.frame.enums.ServerType;
import com.frame.gateway.config.GatewayConfig;
import com.frame.gateway.handler.GatewayServerWebSocketFrameHandler;
import com.frame.gateway.handler.RedisGatewayServerHandler;
import com.frame.gateway.rocketmq.BroadcastRocketMqReceive;
import com.frame.gateway.rocketmq.RocketMqReceive;
import com.frame.gateway.rocketmq.RocketMqSender;
import com.frame.id.DeskIdBuilder;
import com.frame.id.IdBuilder;
import com.frame.mobel.ProtoBuf;
import com.frame.netty.server.NettyWebSocketServerV3;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * GatewayMrg.java
 * @author Sanjie
 * @date 2021-09-24 12:24
 * @version 1.0.0
 */
@Slf4j
public class GatewayMrg {
	@Getter
	private static GatewayMrg instance = new GatewayMrg();
	
	@Getter
	private IdBuilder idGenerator = null; 
	@Getter
	private int serverId;
	/**
	 * 初始化gateway 服务器相关配置
	 */
	public void start(String nameServerAddr, int serversGroupId) {
		try {
			int serverType = ServerType.GATEWAY.getType();
			serverId = DeskIdBuilder.getServerId(serversGroupId, serverType);
			//初始化id生成器
			idGenerator = new IdBuilder(serverId);
			//初始化websocket服务器
			new NettyWebSocketServerV3<GatewayServerWebSocketFrameHandler>(GatewayConfig.getInstance().getPort(), GatewayServerWebSocketFrameHandler.class).start();
			
			//初始化mq
			RocketMqSender.getInstance().start(nameServerAddr, MqProducer.PRODUCER_GROUP+serverType, 16, null);
			RocketMqReceive.getInstance().start(
					nameServerAddr, 
					MqTopic.PUSH_TOPIC_GATEWAY +serverId, 
					MqConsumerGroup.CONSUMER_PUSH_GATEWAY+serverId,
					null);
			BroadcastRocketMqReceive.getInstance().start(
					nameServerAddr, 
					MqTopic.BROADCAST_TOPIC_GATEWAY, 
					MqConsumerGroup.CONSUMER_BROADCAST_GATEWAY + serverId, 
					null);
			
			//监听mq收到的ProtoBuf
			RocketMqReceive.getMqRouterHandler().registHandler(ProtoBuf.class.getSimpleName(), GatewayServerWebSocketFrameHandler::receiveMqData);
			
			//注册服务器信息到redis
			RedisGatewayServerHandler.getInstance().register2Redis(serverId, serverType, GatewayConfig.getInstance().getHostname(), GatewayConfig.getInstance().getPort());
			
			log.info("GatewayMrg {} start success", serverId);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			System.exit(0);
		}
	}
	
}
