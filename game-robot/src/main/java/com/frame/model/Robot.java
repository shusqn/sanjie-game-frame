package com.frame.model;

import com.frame.enums.ServerType;
import com.frame.executor.RobotExecutor;
import com.frame.handler.RobotNettyGatewayClientHandler;
import com.frame.mobel.mq.AskRobotLogin;
import com.frame.netty.client.NettyWebSocketClient;

import io.netty.channel.Channel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class Robot {
	private RobotUserInfo robotUserInfo;
	private String uid;
	private String token;
	private int port;
	private String ip;
	private int centerSid;
	private int gatewaySid;
	private int targetGameSid;
	
	private long balance;
	
	private long loginOutMillTime = Long.MAX_VALUE;
	
	@Setter(value = AccessLevel.NONE)
	private ServerType targetGameServerType;
	
	private Channel channel;

	public Robot() {
	}
	
	private AskRobotLogin askRobotLogin;
	private NettyWebSocketClient nettyWebSocketClient;
	/**
	 * @param robotUserInfo
	 * @param serverType
	 * @param serverId
	 * @param gatewaySid
	 */
	public Robot(RobotUserInfo robotUserInfo, ServerInfo gateWayServerInfo, AskRobotLogin askRobotLogin) {
		this.robotUserInfo = robotUserInfo;
		this.uid = robotUserInfo.getUserId() + "";
		this.gatewaySid = gateWayServerInfo.getSid();
		
		this.askRobotLogin = askRobotLogin;
		targetGameServerType = ServerType.valueOf(askRobotLogin.getServerType());
		
		nettyWebSocketClient = new NettyWebSocketClient(gateWayServerInfo.getHostname(), gateWayServerInfo.getPort(), new RobotNettyGatewayClientHandler(this));
		nettyWebSocketClient.setReConnect(true);
	}
	
	public void initNettyWebSocketClient(ServerType targetGameServerType) {
		if(nettyWebSocketClient != null) {
			return;
		}
		this.targetGameServerType = targetGameServerType;
		nettyWebSocketClient = new NettyWebSocketClient(ip, port, new RobotNettyGatewayClientHandler(this));
		nettyWebSocketClient.setReConnect(true);
	}

	/**
	 * @param url
	 * @param port
	 */
	public void connectGateWayServer() {
		RobotExecutor.getInstance().execute(Long.valueOf(uid), nettyWebSocketClient);
	}
}
