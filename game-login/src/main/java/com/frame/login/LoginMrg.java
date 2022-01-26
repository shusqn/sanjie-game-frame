package com.frame.login;

import com.frame.enums.ServerType;
import com.frame.id.DeskIdBuilder;
import com.frame.id.IdBuilder;
import com.frame.login.handler.RedisLoginServerHandler;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * GatewayMrg.java
 * @author Sanjie
 * @date 2021-09-24 12:24
 * @version 1.0.0
 */
@Slf4j
public class LoginMrg {
	@Getter
	private static LoginMrg insance = new LoginMrg();
	
	@Getter
	private IdBuilder idGenerator = null; 
	@Getter
	private int serverId;
	/**
	 * 初始化gateway 服务器相关配置
	 */
	public void start(String nameServerAddr, int serversGroupId) {
		try {
			int serverType = ServerType.LOGIN.getType();
			serverId = DeskIdBuilder.getServerId(serversGroupId, serverType);
			//初始化id生成器	
			idGenerator = new IdBuilder(serverId);
			//注册服务器信息到redis
			RedisLoginServerHandler.getInstance().register2Redis(serverId, serverType);
			
			log.info("LoginMrg {} start success", serverId);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			System.exit(0);
		}
	}
	
}
