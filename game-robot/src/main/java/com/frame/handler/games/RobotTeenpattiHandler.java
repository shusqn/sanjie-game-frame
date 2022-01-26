package com.frame.handler.games;

import java.util.function.Consumer;

import com.frame.executor.RobotExecutor;
import com.frame.mobel.ProtoBuf;
import com.frame.model.Robot;
import com.frame.protobuf.GatewayMsg;
import com.frame.protobuf.TeenpattiMsg;
import com.frame.protobuf.TeenpattiMsg.DeskState;
import com.frame.protobuf.TeenpattiMsg.Player;
import com.frame.protobuf.TeenpattiMsg.WinlostInfo;
import com.frame.router.RobotCmdRouterHandler;
import com.frame.utils.MsgUtils;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;

import io.netty.channel.Channel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * RobotTeenpattiHandler.java
 * @author Sanjie
 * @date 2021-10-18 10:30
 * @version 1.0.0
 */
@Slf4j
public class RobotTeenpattiHandler {
	@Setter
	private Channel channel;
	private Robot robot;
	private int deskState;
	private String currentPid;
	private boolean seeCard =false;
	private RobotCmdRouterHandler cmdTeenpattiRouter = new RobotCmdRouterHandler();
	
	/**
	 * @param robot
	 */
	public RobotTeenpattiHandler(Robot robot) {
		 this.robot = robot;
		 cmdTeenpattiRouter.registHandler(TeenpattiMsg.SubCmd.Cmd_PushSitDown_VALUE, this::pushSitDown);
		 cmdTeenpattiRouter.registHandler(TeenpattiMsg.SubCmd.Cmd_NoticeDeskStateChange_VALUE, this::noticeDeskStateChange);
		 cmdTeenpattiRouter.registHandler(TeenpattiMsg.SubCmd.Cmd_NoticePlayerTurn_VALUE, this::noticePlayerTurn);
		 cmdTeenpattiRouter.registHandler(TeenpattiMsg.SubCmd.Cmd_PushSeeCard_VALUE, this::pushSeeCard);
		 cmdTeenpattiRouter.registHandler(TeenpattiMsg.SubCmd.Cmd_NoticePlayerShow_VALUE, this::noticePlayerShow);
		 cmdTeenpattiRouter.registHandler(TeenpattiMsg.SubCmd.Cmd_NoticeWinlost_VALUE, this::noticeWinlost);
		 
		 cmdTeenpattiRouter.registHandler(TeenpattiMsg.SubCmd.Cmd_NoticeUserOnline_VALUE, this::voidFunc);
		 cmdTeenpattiRouter.registHandler(TeenpattiMsg.SubCmd.Cmd_NoticeUserEnter_VALUE, this::voidFunc);
		 cmdTeenpattiRouter.registHandler(TeenpattiMsg.SubCmd.Cmd_NoticeUserLeave_VALUE, this::voidFunc);
		 cmdTeenpattiRouter.registHandler(TeenpattiMsg.SubCmd.Cmd_NoticeBankerInfo_VALUE, this::voidFunc);
		 cmdTeenpattiRouter.registHandler(TeenpattiMsg.SubCmd.Cmd_NoticePlayerBetAnte_VALUE, this::voidFunc);
		 cmdTeenpattiRouter.registHandler(TeenpattiMsg.SubCmd.Cmd_NoticePlayerDropCard_VALUE, this::voidFunc);
		 cmdTeenpattiRouter.registHandler(TeenpattiMsg.SubCmd.Cmd_NoticePlayerBalance_VALUE, this::voidFunc);
		 cmdTeenpattiRouter.registHandler(TeenpattiMsg.SubCmd.Cmd_NoticePlayerCall_VALUE, this::voidFunc);
		 cmdTeenpattiRouter.registHandler(TeenpattiMsg.SubCmd.Cmd_NoticeRefuseOrAgreeShow_VALUE, this::voidFunc);
		 cmdTeenpattiRouter.registHandler(TeenpattiMsg.SubCmd.Cmd_NoticePlayerShowResult_VALUE, this::voidFunc);
		 cmdTeenpattiRouter.registHandler(TeenpattiMsg.SubCmd.Cmd_NoticePlayerStatus_VALUE, this::voidFunc);
		 cmdTeenpattiRouter.registHandler(TeenpattiMsg.SubCmd.Cmd_NoticePlayerSeeCard_VALUE, this::voidFunc);
		 
		 cmdTeenpattiRouter.registHandler(GatewayMsg.SubCmd.Cmd_PushErrorMsg_VALUE, this::pushErrorMsg);
	}
	
	private void voidFunc(ProtoBuf pbvo) {
	}
	private void pushErrorMsg(ProtoBuf pbvo) {
		try {
			GatewayMsg.PushErrorMsg message = GatewayMsg.PushErrorMsg.parseFrom(pbvo.getBody());
			log.error("{}, {}", message.getCode(), message.getArgs(0));
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}
	
	public void pushHeartBeat() {
		if(System.currentTimeMillis() > robot.getLoginOutMillTime() && deskState == DeskState.WAIT_VALUE) {
			log.info("机器人 {}存活时间到,退出游戏", robot.getUid());
			robot.getNettyWebSocketClient().closeChannel();
		}
	}
	
	/**
	 * @param cmd
	 * @param pbvo
	 */
	public void teenpattiRouterHanlder(ProtoBuf pbvo) {
		Consumer<ProtoBuf> handler = cmdTeenpattiRouter.getHandler(pbvo.getCmd());
		if(handler != null) {
			handler.accept(pbvo);
		}
	}
	
	private void reset() {
		seeCard = false;
	}
	
	public void reqSitDown(int roomId) {
		//登录
		sendToWsMsg(TeenpattiMsg.SubCmd.Cmd_ReqSitDown_VALUE ,
				TeenpattiMsg.ReqSitDown.newBuilder()
				.setRoomId(roomId)
				.build());
		log.info("robot {} 登录房间 {}", robot.getUid(),roomId);
	}
	
	private void reqReady() {
		sendToWsMsg(TeenpattiMsg.SubCmd.Cmd_ReqReady_VALUE, 
				TeenpattiMsg.ReqReady.newBuilder()
				.build());
	}
	
	/**
	 * @param pbvo
	 */
	private void noticeWinlost(ProtoBuf pbvo) {
		try {
			TeenpattiMsg.NoticeWinlost message = TeenpattiMsg.NoticeWinlost.parseFrom(pbvo.getBody());
			for (WinlostInfo wininfo : message.getWinlostInfoListList()) {
				if( robot.getUid().equals(wininfo.getUserId())) {
					robot.setBalance(wininfo.getBalance());
					log.info("robotId:{} balance:{} winlost:{}", robot.getUid(), robot.getBalance(), wininfo.getWinAmount());
				}
			}
		}catch (Exception e) {
			
		}
	}
	
	/**
	 * @param pbvo
	 */
	private void noticePlayerShow(ProtoBuf pbvo) {
		try {
			TeenpattiMsg.NoticePlayerShow message = TeenpattiMsg.NoticePlayerShow.parseFrom(pbvo.getBody());
			if(message.getCompareUserId().equals(robot.getUid() + "")){
				long index = (long) (Math.random() * 100);
				index -= 50;
				if(index < 0) {
					// 同意
					sendToWsMsg(TeenpattiMsg.SubCmd.Cmd_ReqRefuseOrAgreeShow_VALUE, 
							TeenpattiMsg.ReqRefuseOrAgreeShow.newBuilder()
							.setAgree(true)
							.build());
				}
				else {
					// 拒绝
					sendToWsMsg(TeenpattiMsg.SubCmd.Cmd_ReqRefuseOrAgreeShow_VALUE, 
							TeenpattiMsg.ReqRefuseOrAgreeShow.newBuilder()
							.setAgree(false)
							.build());
				}
			}
			log.info(message.toString());
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param pbvo
	 */
	private void noticePlayerTurn(ProtoBuf pbvo) {
		try {
			TeenpattiMsg.NoticePlayerTurn message = TeenpattiMsg.NoticePlayerTurn.parseFrom(pbvo.getBody());
			currentPid = message.getUserId();
			
			if(message.getUserId().equals(robot.getUid() + "")) {
				long index = (long) (Math.random() * 100);
				index -= 50;
				if(index < 0 && !seeCard) {
					reqSeeCard();
				}
				else {
					playerDoFunction();
				}
			}
			
			log.info(message.toString());
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}
	
	private void playerDoFunction() {
		long index = (long) (Math.random() * 100);
		index -= 35;
		if(index < 0) {
			reqCall();
			return;
		}
		index -= 35;
		if(index < 0) {
			reqRaise();
			return;
		}
		index -= 25;
		if(index < 0) {
			// 发起比牌
			sendToWsMsg(TeenpattiMsg.SubCmd.Cmd_ReqShow_VALUE, 
					TeenpattiMsg.ReqShow.newBuilder()
					.build());
			return;
		}
		else {
			reqDropCard();
		}
	}
	
	private void reqCall() {
		sendToWsMsg(TeenpattiMsg.SubCmd.Cmd_ReqCall_VALUE, 
				TeenpattiMsg.ReqCall.newBuilder()
				.build());
	}
	
	/**
	 * 
	 */
	private void reqRaise() {
		sendToWsMsg(TeenpattiMsg.SubCmd.Cmd_ReqRaise_VALUE, 
				TeenpattiMsg.ReqRaise.newBuilder()
				.build());
	}
	
	private void reqSeeCard() {
		sendToWsMsg(TeenpattiMsg.SubCmd.Cmd_ReqSeeCard_VALUE, 
				TeenpattiMsg.ReqSeeCard.newBuilder()
				.build());
	}
	
	private void reqDropCard() {
		sendToWsMsg(TeenpattiMsg.SubCmd.Cmd_ReqDropCard_VALUE, 
				TeenpattiMsg.ReqDropCard.newBuilder()
				.build());
	}
	
	private void noticeDeskStateChange(ProtoBuf pbvo) {
		try {
			TeenpattiMsg.NoticeDeskStateChange message = TeenpattiMsg.NoticeDeskStateChange.parseFrom(pbvo.getBody());
			deskState = message.getState();
			if(deskState == DeskState.WAIT_VALUE) {
				reset();
				reqReady();
			}
			log.info(message.toString());
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param pbvo
	 */
	private void pushSeeCard(ProtoBuf pbvo) {
		if(currentPid.equals(robot.getUid() + "")) {
			seeCard = true;
			playerDoFunction();
		}
	}
	
	
	/**
	 * @param pbvo
	 */
	private void pushSitDown(ProtoBuf pbvo) {
		try {
			TeenpattiMsg.PushSitDown message = TeenpattiMsg.PushSitDown.parseFrom(pbvo.getBody());
			deskState = message.getDesk().getState();
			
			for (Player player : message.getDesk().getPlayersList()) {
				if(player.getUserId().equals(robot.getUid())) {
					robot.setBalance(player.getBalance());
					seeCard = player.getIsSeeCard();
				}
			}
			log.info(message.toString());
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 发送到websocket 到客户端
	 * @param channel
	 * @param cmd
	 * @param body
	 */
	public void sendToWsMsg(int cmd, Message message){
		RobotExecutor.getInstance().executeDelay((long) (Math.random()*5000 + 2000), Long.valueOf(robot.getUid()), ()->{
			MsgUtils.sendToWsMsg(robot.getChannel(), ProtoBuf.build(cmd, message.toByteArray(), robot.getTargetGameSid()));
		}, cmd+":handler");
	}
}
