package com.frame.dt;

import com.frame.constant.MqProducer;
import com.frame.dt.builder.PveGameBuilderMgr;
import com.frame.dt.handler.RedisPveServerHandler;
import com.frame.dt.rocketmq.RocketMqSender;
import com.frame.enums.ServerType;
import com.frame.id.DeskIdBuilder;
import com.frame.id.IdBuilder;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * GatewayMrg.java
 * @author Sanjie
 * @date 2021-09-24 12:24
 * @version 1.0.0
 */
@Slf4j
public class DtMrg {
	@Getter
	private static DtMrg insance = new DtMrg();
	
	@Getter
	private IdBuilder idBuilder = null; 
	@Getter
	private int serverId;
	/**
	 * 初始化gateway 服务器相关配置
	 */
	public void start(String nameServerAddr) {
		try {
			int serverType = ServerType.DT_BUILDER.getType();
			serverId = DeskIdBuilder.getServerId(0, serverType);
			//初始化id生成器
			idBuilder = new IdBuilder(serverId);
			
			//初始化mq
			RocketMqSender.getInstance().start(nameServerAddr, MqProducer.PRODUCER_GROUP+serverType, 16, null);
			//注册服务器信息到redis
			RedisPveServerHandler.getInstance().register2Redis(serverId, serverType);
			
			//初始化游戏桌台
			PveGameBuilderMgr.getInstance().init();
			
			log.info("DtMrg {} start success", serverId);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			System.exit(0);
		}
	}
	
}
