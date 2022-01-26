package com.frame;

import com.frame.constant.MqConsumerGroup;
import com.frame.constant.MqTopic;
import com.frame.enums.ServerType;
import com.frame.handler.RedisRobotServerHandler;
import com.frame.id.DeskIdBuilder;
import com.frame.id.IdBuilder;
import com.frame.manager.RobotUserInfoConfigMrg;
import com.frame.mobel.mq.AskRobotLogin;
import com.frame.mobel.mq.AskRobotLoginOut;
import com.frame.model.Robot;
import com.frame.model.RobotUserInfo;
import com.frame.model.ServerInfo;
import com.frame.rocketmq.RocketMqReceive;
import com.google.gson.Gson;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * GatewayMrg.java
 * @author Sanjie
 * @date 2021-09-24 12:24
 * @version 1.0.0
 */
@Slf4j
public class RobotMrg {
	@Getter
	private static RobotMrg instance = new RobotMrg();
	
	@Getter
	private IdBuilder idBuilder = null; 
	@Getter
	private int serverId;
	/**
	 * 初始化gateway 服务器相关配置
	 */
	public void start(String nameServerAddr, int serversGoupId) {
		try {	
			int serverType = ServerType.ROBOT.getType();
			serverId = DeskIdBuilder.getServerId(serversGoupId, serverType);
			//初始化id生成器
			idBuilder = new IdBuilder(serverId);
			//注册服务器信息到redis
			RedisRobotServerHandler.getInstance().register2Redis(serverId, serverType);
			
			RocketMqReceive.getInstance().start(
					nameServerAddr, 
					MqTopic.BROADCAST_TOPIC_ROBOT_SERVER, 
					MqConsumerGroup.CONSUMER_BROADCAST_ROBOT+serverId,
					null, true);
			
			RocketMqReceive.getInstance().getMqRouterHandler().registHandler(AskRobotLogin.class.getSimpleName(), this::askRobotLogin);
			RocketMqReceive.getInstance().getMqRouterHandler().registHandler(AskRobotLoginOut.class.getSimpleName(), this::askRobotLoginOut);
			
			log.info("RobotMrg {} start success", serverId);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			System.exit(0);
		}
	}
	
	/**
	 * @return
	 */
	private ServerInfo getGateWayServer() {
		for (ServerInfo serverInfo : RedisRobotServerHandler.getInstance().getAllServer()) {
			if(serverInfo.getStype() == ServerType.GATEWAY.getType()) {
				return serverInfo;
			}
		}
		log.error("找不到可用的gateway服务器");
		return null;
	}
	
	
	/**
	 * @param gateWayServerInfo
	 * @param serverType
	 * @param serverId
	 */
	private void robotLogin(ServerInfo gateWayServerInfo, AskRobotLogin askRobotLogin) {
		RobotUserInfo robotUserInfo = RobotUserInfoConfigMrg.getInstance().getOneRobot(askRobotLogin);
		if(robotUserInfo != null) {
			Robot robot = new Robot(robotUserInfo, gateWayServerInfo, askRobotLogin);
			robot.connectGateWayServer();
		}
	}
	
	private Gson gson = new Gson();
	/**
	 * @param message
	 */
	private void askRobotLogin(String pbvodata) {
		try {
			AskRobotLogin askRobotLogin = gson.fromJson(pbvodata, AskRobotLogin.class);
			ServerInfo gateWayServerInfo = getGateWayServer();
			robotLogin(gateWayServerInfo, askRobotLogin);
			log.info("deskId:{} serverId:{} {}服务器要求上线",  askRobotLogin.getDeskId(), askRobotLogin.getSid(), ServerType.valueOf(askRobotLogin.getServerType()));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	/**
	 * @param message
	 */
	private void askRobotLoginOut(String pbvodata) {
		try {
			AskRobotLoginOut askRobotLoginOut = gson.fromJson(pbvodata, AskRobotLoginOut.class);
			Robot robot = RobotUserInfoConfigMrg.getInstance().getRunningRobotMap().get(askRobotLoginOut.getUid());
			if(robot != null) {
				log.info("robot {} 被服务器要求下线",  robot.getUid());
				robot.getNettyWebSocketClient().closeChannel();
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
}
