package com.frame.pve;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.frame.constant.MqConsumerGroup;
import com.frame.constant.MqProducer;
import com.frame.constant.MqTopic;
import com.frame.enums.GameType;
import com.frame.enums.ServerType;
import com.frame.id.DeskIdBuilder;
import com.frame.id.IdBuilder;
import com.frame.pve.handler.RedisPveServerHandler;
import com.frame.pve.handler.cmd.GameReqCmdHandler;
import com.frame.pve.manager.DeskMgr;
import com.frame.pve.rocketmq.BroadcastPveRocketMqReceive;
import com.frame.pve.rocketmq.BroadcastRocketMqReceive;
import com.frame.pve.rocketmq.RocketMqReceive;
import com.frame.pve.rocketmq.RocketMqSender;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * GatewayMrg.java
 * @author Sanjie
 * @date 2021-09-24 12:24
 * @version 1.0.0
 */
@Slf4j
public class PveGameMrg {
	private static Map<Integer, PveGameMrg> instanceMap = new ConcurrentHashMap<Integer, PveGameMrg>();
	public static PveGameMrg getInstance(GameType gameType) {
		return instanceMap.get(gameType.getType());
	}
	private GameType gameType;
	public PveGameMrg(GameType gameType) {
		this.gameType =  gameType;
		instanceMap.put(gameType.getType(), this);
	}
	//=============================================

	@Getter
	private IdBuilder idBuilder = null; 
	@Getter
	private int serverId;

	/**
	 * 初始化gateway 服务器相关配置
	 */
	public void start(String nameServerAddr, int serversGroupId) {
		try {
			int serverType = gameType.getServerType().getType();
			
			serverId = DeskIdBuilder.getServerId(serversGroupId, serverType);
			//初始化id生成器
			idBuilder = new IdBuilder(serverId);
			
			//初始化桌台
			DeskMgr.getInstance(gameType).initAllDesk();
			
			//初始化pb 接收handler
			GameReqCmdHandler.getInstance(gameType);
			
			//初始化mq
			RocketMqSender.getInstance(gameType).start(nameServerAddr, MqProducer.PRODUCER_GROUP+serverType, 16, null);
			RocketMqReceive.getInstance(gameType).start(
					nameServerAddr, 
					MqTopic.PUSH_TOPIC_SERVER +serverId, 
					MqConsumerGroup.CONSUMER_PUSH_SERVER+serverId,
					null);
			BroadcastRocketMqReceive.getInstance(gameType).start(
					nameServerAddr, 
					MqTopic.BROADCAST_TOPIC_SERVER, 
					MqConsumerGroup.CONSUMER_BROADCAST_SERVER + serverId, 
					null);
			BroadcastPveRocketMqReceive.getInstance(gameType).start(
					nameServerAddr, 
					MqTopic.BROADCAST_TOPIC_PVE, 
					MqConsumerGroup.CONSUMER_BROADCAST_PVE + serverId, 
					null);
			
			//注册服务器信息到redis
			RedisPveServerHandler.getInstance(gameType).register2Redis(serverId, serverType);
			log.info("PveGameMrg {} game:{} start success", serverId, gameType.toString());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			System.exit(0);
		}
	}

}
