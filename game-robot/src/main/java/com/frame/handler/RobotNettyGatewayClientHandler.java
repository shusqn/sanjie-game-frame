package com.frame.handler;

import java.util.concurrent.atomic.AtomicInteger;

import com.frame.enums.ServerType;
import com.frame.executor.RobotExecutor;
import com.frame.handler.games.RobotPveHandler;
import com.frame.handler.games.RobotTeenpattiHandler;
import com.frame.manager.RobotUserInfoConfigMrg;
import com.frame.mobel.ProtoBuf;
import com.frame.mobel.mq.AskRobotLogin;
import com.frame.model.Robot;
import com.frame.netty.handler.AbstrackClientWebSocketFrameHandler;
import com.frame.protobuf.CenterMsg;
import com.frame.protobuf.GatewayMsg;
import com.frame.protobuf.RobotMsg;
import com.frame.router.RobotCmdRouterHandler;
import com.frame.utils.MsgUtils;
import com.frame.utils.UuidUtils;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RobotNettyGatewayClientHandler extends AbstrackClientWebSocketFrameHandler{
	private Robot robot;
	private final AtomicInteger seqCount = new AtomicInteger();
	private RobotCmdRouterHandler cmdRouter = new RobotCmdRouterHandler();
	private RobotTeenpattiHandler teenpattiHandler;
	private RobotPveHandler dtHandler;

	public RobotNettyGatewayClientHandler(Robot robot) {
		this.robot = robot;
		teenpattiHandler = new RobotTeenpattiHandler(robot);
		dtHandler = new RobotPveHandler(robot);
		
		cmdRouter.registHandler(GatewayMsg.SubCmd.Cmd_PushIdentify_VALUE, this::pushLogin);
		cmdRouter.registHandler(GatewayMsg.SubCmd.Cmd_PushHeartBeat_VALUE, this::pushHeartBeat);
		cmdRouter.registHandler(CenterMsg.SubCmd.Cmd_PushLoginCenter_VALUE, this::pushLoginCenter);
		cmdRouter.registHandler(CenterMsg.SubCmd.Cmd_PushGameServerId_VALUE, this::pushGameServerId);
		
		cmdRouter.registHandler(RobotMsg.SubCmd.Cmd_PushRobotLogin_VALUE, this::pushRobotLogin);
		cmdRouter.registHandler(GatewayMsg.SubCmd.Cmd_PushErrorMsg_VALUE, this::pushErrorMsg);
	}
	
	private void pushErrorMsg(ProtoBuf pbvo) {
		try {
			GatewayMsg.PushErrorMsg message = GatewayMsg.PushErrorMsg.parseFrom(pbvo.getBody());
			log.info(message.toString());
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param pbvo
	 */
	private void pushRobotLogin(ProtoBuf pbvo) {
		log.info("机器人 {}登录gateway 成功!", robot.getUid());
		RobotUserInfoConfigMrg.getInstance().getRunningRobotMap().put(robot.getRobotUserInfo().getUserId(), robot);
		
		robot.setLoginOutMillTime((long) (1000 * 60 * 30 * Math.random() + 1000 * 60 * 30 + System.currentTimeMillis()));
		
		RobotExecutor.getInstance().scheduleAtFixedRate(0, this::reqHeartBeat, 10);
		
		reqHeartBeat();
		
		reqRobotLoginGame(robot.getAskRobotLogin());
	}
	
	/**
	 * 登录对应游戏服务器
	 */
	public void reqRobotLoginGame(AskRobotLogin askRobotLogin) {
		robot.setTargetGameSid(askRobotLogin.getSid());
		
		//登录
		sendToWsMsg(RobotMsg.SubCmd.Cmd_ReqRobotLoginGame_VALUE, askRobotLogin.getSid() ,
				RobotMsg.ReqRobotLoginGame.newBuilder()
				.setHeadPic(robot.getRobotUserInfo().getHeadPic())
				.setHeadPicType(robot.getRobotUserInfo().getHeadPicType())
				.setName(robot.getRobotUserInfo().getName())
				.setUserId(robot.getRobotUserInfo().getUserId()+"")
				.setLevel(robot.getRobotUserInfo().getLevel())
				.setBalance(robot.getRobotUserInfo().getBalance())
				.setRoomId(askRobotLogin.getRoomId())
				.setDeskId(askRobotLogin.getDeskId())
				.build());
	}

	/**
	 * @param pbvo
	 */
	private void pushHeartBeat(ProtoBuf pbvo) {
		try {
			if(robot.getAskRobotLogin() != null) {
				if(robot.getAskRobotLogin().getServerType() == ServerType.TEENPATTI.getType()) {
					teenpattiHandler.pushHeartBeat();
				}
				else if(robot.getAskRobotLogin().getServerType() == ServerType.DT_BET_GAME.getType()) {
					dtHandler.pushHeartBeat();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 登录成功
	 * @param pbvo
	 */
	private void pushLogin(ProtoBuf pbvo) {
		try {
			GatewayMsg.PushIdentify message = GatewayMsg.PushIdentify.parseFrom(pbvo.getBody());
			robot.setCenterSid(message.getCentreSid());
			RobotExecutor.getInstance().scheduleAtFixedRate(0, this::reqHeartBeat, 10);
			reqHeartBeat();
			loginCenter();
			log.info("gateway 登录成功 {}", message.toString());
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 登录成功
	 * @param pbvo
	 */
	private void pushLoginCenter(ProtoBuf pbvo) {
		try {
			CenterMsg.PushLoginCenter message = CenterMsg.PushLoginCenter.parseFrom(pbvo.getBody());
			reqGameServerId();
			log.info("center登录成功 {}", message.toString());
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	private void reqGameServerId(){
		try {
			sendToWsMsg( CenterMsg.SubCmd.Cmd_ReqGameServerId_VALUE, robot.getCenterSid(), CenterMsg.ReqGameServerId.newBuilder()
					.setServerType(robot.getTargetGameServerType().getType())
					.build());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * @param pbvo
	 */
	private void pushGameServerId(ProtoBuf pbvo) {
		try {
			CenterMsg.PushGameServerId message = CenterMsg.PushGameServerId.parseFrom(pbvo.getBody());
			robot.setTargetGameSid(message.getServerId());
			if(ServerType.TEENPATTI.getType() == robot.getTargetGameServerType().getType()) {
				teenpattiHandler.reqSitDown(10);
			}
			else if(ServerType.DT_BET_GAME.getType() == robot.getTargetGameServerType().getType()) {
				dtHandler.reqSitDown(20);
			}
			
			log.info("pushGameServerId {}", message.toString());
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 登录大厅
	 */
	private void loginCenter(){
		try {
			sendToWsMsg( CenterMsg.SubCmd.Cmd_ReqLoginCenter_VALUE, robot.getCenterSid(), 
					CenterMsg.ReqLoginCenter.newBuilder()
					.build());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * 心跳
	 */
	private void reqHeartBeat(){
		try {
			sendToWsMsg( GatewayMsg.SubCmd.Cmd_ReqHeartBeat_VALUE, robot.getGatewaySid(), GatewayMsg.ReqHeartBeat.newBuilder().build());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public void readCmdData(Channel channel, ByteBuf buf) {
		ProtoBuf pbvo = MsgUtils.read(buf);
		pbvo.getSid();
		if(ServerType.TEENPATTI.getType() == robot.getTargetGameServerType().getType() 
				&& robot.getTargetGameSid() == pbvo.getSid()) {
			teenpattiHandler.teenpattiRouterHanlder(pbvo);
		}
		else if(ServerType.DT_BET_GAME.getType() == robot.getTargetGameServerType().getType()
				&& robot.getTargetGameSid() == pbvo.getSid()) {
			dtHandler.routerHanlder(pbvo);
		}
		else {
			cmdRouter.executeHandler(pbvo.getCmd(), pbvo);
		}
	}
	
	@Override
	public void connectClose(Channel channel) {
		RobotUserInfoConfigMrg.getInstance().getRunningRobotMap().remove(Long.valueOf( robot.getUid() ));
	}
	
	@Override
	public void connectSuccess(Channel channel) {
		robot.setChannel(channel);
		teenpattiHandler.setChannel(channel);
		seqCount.set(0);
		
		if(robot.getRobotUserInfo() == null) {
			//登录
			sendToWsMsg( GatewayMsg.SubCmd.Cmd_ReqIdentify_VALUE, robot.getGatewaySid() ,
					GatewayMsg.ReqIdentify.newBuilder()
					.setPid(robot.getUid() + "")
					.setToken(robot.getToken())
					.build());
		}
		else {
			robot.setToken(UuidUtils.getId());
			RedisRobotServerHandler.getInstance().setRobotToken( Long.valueOf(robot.getUid()), robot.getToken());
			//机器人登录
			sendToWsMsg( RobotMsg.SubCmd.Cmd_ReqRobotLogin_VALUE, robot.getGatewaySid() ,
					RobotMsg.ReqRobotLogin.newBuilder()
					.setPid(robot.getUid() + "")
					.setToken(robot.getToken())
					.build());
		}
		
	}

	/**
	 * 发送到websocket 到客户端
	 * @param channel
	 * @param cmd
	 * @param body
	 */
	public void sendToWsMsg(int cmd,  int targetSid, Message message){
		MsgUtils.sendToWsMsg(robot.getChannel(), ProtoBuf.build(cmd, message.toByteArray(), targetSid));
	}
}
