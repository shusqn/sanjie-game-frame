package com.frame.handler.games;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.lang3.RandomUtils;

import com.frame.enums.ServerType;
import com.frame.executor.RobotExecutor;
import com.frame.mobel.ProtoBuf;
import com.frame.mobel.card.BetType;
import com.frame.model.Robot;
import com.frame.protobuf.GatewayMsg;
import com.frame.protobuf.PveGameMsg;
import com.frame.protobuf.PveGameMsg.DeskState;
import com.frame.protobuf.PveGameMsg.Player;
import com.frame.router.RobotCmdRouterHandler;
import com.frame.utils.MsgUtils;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;

import lombok.extern.slf4j.Slf4j;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * RobotTeenpattiHandler.java
 * @author Sanjie
 * @date 2021-10-18 10:30
 * @version 1.0.0
 */
@Slf4j
public class RobotPveHandler {
	private Robot robot;
	private int deskState;
	private RobotCmdRouterHandler cmdRouter = new RobotCmdRouterHandler();
	
	private List<Tuple2<Integer, Integer>> betAmoutList_dt = Arrays.asList(Tuples.of(100, 40), Tuples.of(500, 30), Tuples.of(1000, 20), Tuples.of(2000, 7), Tuples.of(5000, 3));
	private List<Tuple2<BetType, Integer>> betTypeList_dt = Arrays.asList(Tuples.of(BetType.DT_DRAGON_WIN, 46), Tuples.of(BetType.DT_TIGER_WIN, 46), Tuples.of(BetType.DT_TIE, 8));
	
	/**
	 * @param robot
	 */
	public RobotPveHandler(Robot robot) {
		 this.robot = robot;
		 cmdRouter.registHandler(PveGameMsg.SubCmd.Cmd_PushSitDown_VALUE, this::pushSitDown);
		 cmdRouter.registHandler(PveGameMsg.SubCmd.Cmd_NoticeDeskStateChange_VALUE, this::noticeDeskStateChange);
		 cmdRouter.registHandler(PveGameMsg.SubCmd.Cmd_NoticeBet_VALUE, this::noticeBet);
		 
		 cmdRouter.registHandler(GatewayMsg.SubCmd.Cmd_PushErrorMsg_VALUE, this::pushErrorMsg);
	}
	
	private void noticeBet(ProtoBuf pbvo) {
		try {
			PveGameMsg.NoticeBet message = PveGameMsg.NoticeBet.parseFrom(pbvo.getBody());
			if(message.getPid().equals(robot.getUid())) {
				log.info(message.toString());
			}
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}
	
	private void pushErrorMsg(ProtoBuf pbvo) {
		try {
			GatewayMsg.PushErrorMsg message = GatewayMsg.PushErrorMsg.parseFrom(pbvo.getBody());
			log.error("{}, {}", message.getCode(), message.getArgs(0));
			//robot.getNettyWebSocketClient().closeChannel();
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}
	
	public void pushHeartBeat() {
		if(System.currentTimeMillis() > robot.getLoginOutMillTime() && deskState == DeskState.START_COUNTDOWN_VALUE) {
			log.info("机器人 {}存活时间到,退出游戏", robot.getUid());
			robot.getNettyWebSocketClient().closeChannel();
		}
	}
	
	/**
	 * @param cmd
	 * @param pbvo
	 */
	public void routerHanlder(ProtoBuf pbvo) {
		Consumer<ProtoBuf> handler = cmdRouter.getHandler(pbvo.getCmd());
		if(handler != null) {
			handler.accept(pbvo);
		}
	}
	
	private void reset() {
		
	}
	
	public void reqSitDown(int roomId) {
		//登录
		sendToWsMsg( PveGameMsg.SubCmd.Cmd_ReqSitDown_VALUE,
				PveGameMsg.ReqSitDown.newBuilder()
				.setRoomId(roomId)
				.build());
		log.info("robot {} 登录房间 {} gameServerId:{}", robot.getUid(), roomId, robot.getTargetGameSid());
	}
	
	private void noticeDeskStateChange(ProtoBuf pbvo) {
		try {
			PveGameMsg.NoticeDeskStateChange message = PveGameMsg.NoticeDeskStateChange.parseFrom(pbvo.getBody());
			deskState = message.getState();
			if(deskState == DeskState.START_COUNTDOWN_VALUE) {
				reset();
			}
			else if(deskState == DeskState.BET_VALUE) {
				if(robot.getTargetGameServerType().getType() == ServerType.DT_BET_GAME.getType()) {
					reqBet();
				}
			}
			log.info(message.toString());
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}
	
	private int getBetAmount() {
		int random = RandomUtils.nextInt(0, 100);
		List<Tuple2<Integer, Integer>> betAmoutList = null;
		if(robot.getTargetGameServerType().getType() == ServerType.DT_BET_GAME.getType()) {
			betAmoutList = betAmoutList_dt;
		}
		for (Tuple2<Integer, Integer> tuple2 : betAmoutList) {
			random -= tuple2.getT2();
			if(random <= 0) {
				return tuple2.getT1();
			}
		}
		return 0;
	}
	
	private int getBetType() {
		int random = RandomUtils.nextInt(0, 100);
		List<Tuple2<BetType, Integer>> betTypeList = null;
		if(robot.getTargetGameServerType().getType() == ServerType.DT_BET_GAME.getType()) {
			betTypeList = betTypeList_dt;
		}
		for (Tuple2<BetType, Integer> tuple2 : betTypeList) {
			random -= tuple2.getT2();
			if(random <= 0) {
				return tuple2.getT1().getValue();
			}
		}
		return 0;
	}
	
	private void reqBet() {
		int betType = getBetType();
		int betAmount = getBetAmount();
		
		RobotExecutor.getInstance().executeDelay(RandomUtils.nextLong(1200, 13000), robot.getUid().hashCode(), this::reqBet, Tuples.of(betType, betAmount),"reqBet");
	}
	
	/**
	 * 
	 */
	private void reqBet(Tuple2<Integer, Integer> tuple2) {
		int betType = tuple2.getT1();
		int betAmount = tuple2.getT2();
		//下注
		sendToWsMsg( PveGameMsg.SubCmd.Cmd_ReqBet_VALUE,
				PveGameMsg.ReqBet.newBuilder()
				.setBetType(betType)
				.setBetAmount(betAmount)
				.build());
	}
	
	/**
	 * @param pbvo
	 */
	private void pushSitDown(ProtoBuf pbvo) {
		try {
			PveGameMsg.PushSitDown message = PveGameMsg.PushSitDown.parseFrom(pbvo.getBody());
			deskState = message.getDesk().getState();
			
			for (Player player : message.getPlayersList()) {
				if(player.getUid().equals(robot.getUid())) {
					robot.setBalance(player.getBalance());
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
	public void sendToWsMsg( int cmd, Message message){
		RobotExecutor.getInstance().executeDelay((long) (Math.random()*5000 + 2000), Long.valueOf(robot.getUid()), ()->{
			MsgUtils.sendToWsMsg(robot.getChannel(), ProtoBuf.build(cmd, message.toByteArray(), robot.getTargetGameSid()));
		}, cmd+":handler");
	}
}
