package com.frame.center.hander.cmd;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.frame.center.CenterMrg;
import com.frame.center.manager.PlayerMgr;
import com.frame.center.service.GameService;
import com.frame.center.utils.MsgUtils;
import com.frame.entity.Game;
import com.frame.entity.Room;
import com.frame.entity.User;
import com.frame.mobel.ProtoBuf;
import com.frame.model.ServerInfo;
import com.frame.protobuf.CenterMsg;
import com.frame.protobuf.CenterMsg.GameInfo;
import com.frame.protobuf.CenterMsg.GamingInfo;
import com.frame.protobuf.CenterMsg.PlayerBaseInfo;
import com.frame.protobuf.CenterMsg.PushGameServerId;
import com.frame.protobuf.CenterMsg.PushLoginCenter;
import com.frame.protobuf.CenterMsg.RoomInfo;
import com.frame.protobuf.GatewayMsg;
import com.google.protobuf.Message;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * SlotGameReqHandler.java
 * @author Sanjie
 * @date 2021-09-10 09:48
 * @version 1.0.0
 */
@Slf4j
@Component
public final class CenterPushCmdHandler {
	@Getter
	private static CenterPushCmdHandler instance;
	@PostConstruct
	private void init(){
		log.info("CenterPushCmdHandler init success !");
		instance = this;
	}

	/**
	 * @param pb
	 * @param errorCode
	 * @param args
	 */
	public void pushErrorMessage(ProtoBuf pb, int  errorCode, String... args) {
		for (String s : args) {
			log.error(s);
		}
		pushMessage(pb, GatewayMsg.SubCmd.Cmd_PushErrorMsg_VALUE, buildError(errorCode, args));
	}

	/**
	 * @param errorCode
	 * @param args
	 * @return
	 */
	public GatewayMsg.PushErrorMsg buildError(int  errorCode, String... args){
		GatewayMsg.PushErrorMsg.Builder builder = GatewayMsg.PushErrorMsg.newBuilder();
		builder.setCode(errorCode);
		for (String arg : args) {
			builder.addArgs(arg);
		}
		return builder.build();
	}

	/**
	 * //请求游戏服务器id 返回
message PushGameServerId{
	int32 serverType = 1;
	int32 serverId = 2;
	 * @param serverType
	 * @param serverId
	 */
	public void pushGameServerId(long pid, ServerInfo serverInfo) {
		PushGameServerId.Builder builder = PushGameServerId.newBuilder()
				.setServerId(serverInfo.getSid())
				.setServerType(serverInfo.getStype());

		int subCmd = CenterMsg.SubCmd.Cmd_PushGameServerId_VALUE;
		pushMessage(pid, subCmd, builder.build());
	}

	/**
	 * //请求登录大厅成功
message PushLoginCenter{
	repeated GameInfo gameList = 1;         //游戏列表
	PlayerBaseInfo playerBaseInfo = 2;          //玩家基本信息
	GamingInfo gamingInfo = 3;                  //正在游戏中的游戏信息
}
	 */
	public void pushLoginCenter(long pid, User user, ServerInfo serverInfo) {
		PushLoginCenter.Builder builder = PushLoginCenter.newBuilder();

		PlayerBaseInfo.Builder playerBaseInfoBuilder = PlayerBaseInfo.newBuilder();
		playerBaseInfoBuilder.setBalance(user.getBalance())
		.setHeadImg(user.getHeadPic() == null ? "": user.getHeadPic())
		.setHeadImgType(user.getHeadPicType())
		.setName(user.getNickName() == null ? "" : user.getNickName())
		.setPid(pid)
		.setSex(user.getSex())
		.setBalance(user.getBalance())
		.setVipLevel(user.getLevel());
		
		if(serverInfo != null) {
			GamingInfo.Builder gamingInfoBuilder = GamingInfo.newBuilder()
					.setGameType(serverInfo.getGameType())
					.setServerId(serverInfo.getSid())
					.setServerType(serverInfo.getStype());
			
			builder.setGamingInfo(gamingInfoBuilder.build());
		}
		
		List<Game> games = GameService.getInstance().findAll();
		
		
		for (Game game : games) {
			if(game.getOpen() == 0) {
				continue;
			}
			GameInfo.Builder gameInfoBuilder = GameInfo.newBuilder()
			.setGameType(game.getGameType())
			.setServerType(game.getServerType());
			
			for (Room room : CenterMrg.getInstance().getConfigList()) {
				if(room.getGameType() == game.getGameType()) {
					gameInfoBuilder.addRoomList(RoomInfo.newBuilder()
							.setAnte(room.getAnte())
							.setConf(room.getConf() == null ? "{}":room.getConf() )
							.setGameType(room.getGameType())
							.setMaxBet(room.getMaxBet())
							.setMinBet(room.getMinBet())
							.setMinJoin(room.getMinJoin())
							.setRoomId(room.getRoomId())
							.setRoomType(room.getRoomType())
							.setTaxRate(room.getTaxRate())
							.build());
				}
			}
					
			builder.addGameList(gameInfoBuilder.build());
		}
		builder.setPlayerBaseInfo(playerBaseInfoBuilder.build());

		int subCmd = CenterMsg.SubCmd.Cmd_PushLoginCenter_VALUE;
		pushMessage(pid, subCmd, builder.build());
	}

	/**
	 * @param userId
	 * @param subCmd
	 * @param message
	 */
	public static void pushMessage(long pid, int subCmd, Message message) {
		ProtoBuf pb = PlayerMgr.getInstance().get(pid);
		if(pb != null){
			pushMessage(pb, subCmd, message);
		}
	}

	/**
	 * @param pb
	 * @param subCmd
	 * @param message
	 */
	public static void pushMessage(ProtoBuf pb, int subCmd, Message message) {
		log.info("pushMessage cmd:{}  pid:{} data:{}" , subCmd, pb.getPid(), message == null ? "" : message.toBuilder().toString());
		MsgUtils.pushToGateway(pb.buildNew(subCmd, message.toByteArray()));
	}
}




