package com.frame.pve.handler.cmd;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.frame.enums.GameType;
import com.frame.mobel.ProtoBuf;
import com.frame.mobel.card.BetType;
import com.frame.mobel.card.Card;
import com.frame.mobel.card.CardsResult;
import com.frame.mobel.card.CardsUtils;
import com.frame.model.Player;
import com.frame.model.game.BetInfo;
import com.frame.protobuf.GatewayMsg;
import com.frame.protobuf.PveGameMsg.Desk;
import com.frame.protobuf.PveGameMsg.PlayerCards;
import com.frame.protobuf.PveGameMsg.PushSitDown;
import com.frame.protobuf.PveGameMsg.SubCmd;
import com.frame.pve.manager.PlayerMgr;
import com.frame.pve.model.PveGameDesk;
import com.frame.pve.rocketmq.RocketMqSender;
import com.frame.utils.ArrayUtils;
import com.google.protobuf.Message;

import lombok.extern.slf4j.Slf4j;


/**
 * @author Sanjie
 * @date 2020-11-4 
 */
@Slf4j
public final class GamePushCmdHandler {
	private static Map<Integer, GamePushCmdHandler> instanceMap = new ConcurrentHashMap<Integer, GamePushCmdHandler>();
	public static GamePushCmdHandler getInstance(GameType gameType) {
		GamePushCmdHandler instance = instanceMap.get(gameType.getType());
		if(instance == null) {
			instance = new GamePushCmdHandler();
			instance.gameType = gameType;
			instanceMap.put(gameType.getType(), instance);
		}
		return instance;
	}
	private GameType gameType;
	//==================================

	/**
	 * 发送错误信息
	 * @param pid
	 * @param errorCode
	 * @param args
	 */
	public void pushErrorMessage(long pid, int  errorCode, String... args) {
		for (String s : args) {
			log.error(s);
		}
		pushMessage(pid, GatewayMsg.SubCmd.Cmd_PushErrorMsg_VALUE, buildError(errorCode, args));
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
	 *    
	Cmd_PushSitDown             = 2003;               //请求坐下 返回
message PushSitDown{
    optional int64 currentTimeMills=1; //服务器当前时间戳用来和客户端做时间矫正
    optional Desk desk=2;
    optional PushInitHand initHand=3; //断线重连时玩家的手牌详情
}
    message Desk{
        optional int32 maxNum=1;//容纳人数
        optional int32 deskId=2;//桌子id
        repeated Player players=3;//玩家们
        optional int32 state=4;//桌子当前状态
        optional int64 expireTimeMillis=5;//桌子当前状态的到期时间戳
    }
	 * @param pid
	 * @param desk
	 */
	public void pushSitDown(long pid, PveGameDesk desk) {
		PushSitDown.Builder builder = PushSitDown.newBuilder();
		
		Desk.Builder deskBuilder = Desk.newBuilder();
		deskBuilder.setCurrentTimeMills(System.currentTimeMillis())
		.setDeskId(desk.getDeskId())
		.setExpireTimeMillis(desk.getWaitProtocol().getExpireTimeMillis())
		.setState(desk.getState());
		
		if(desk.getPlayerCards().size() > 0) {
			for (Entry<Integer, List<Card>> entry : desk.getPlayerCards().entrySet()) {
				deskBuilder.addPlayerCards(PlayerCards.newBuilder()
						.setPlayerType(entry.getKey())
						.addAllCards(CardsUtils.getCardDataList(entry.getValue())));
			}
		}
		
		if(desk.getCardsResult().size() > 0) {
			for (Entry<Integer, CardsResult> entry : desk.getCardsResult().entrySet()) {
				deskBuilder.addPlayerCardsResults(com.frame.protobuf.PveGameMsg.PlayerCardsResult.newBuilder()
						.setPlayerType(entry.getKey())
						.setCardsResult(entry.getValue().getValue())
						.build()
						);
			}
		}
		
		if(desk.getResluts().size() > 0) {
			for (BetType betType : desk.getResluts()) {
				deskBuilder.addBetTypes(betType.getValue());
			}
		}
		
		Collection<Long> playerList = ArrayUtils.copeCollection(desk.getDeskSeatPlayerInfo().values());
		playerList.add(pid);

		for (Long playerId : playerList) {
			Player player = PlayerMgr.getInstance(gameType).getPlayer(playerId);
			int offlineStatus = desk.getOfflinePlayerMap().get(player.getPid()) != null ? 2: 1;
			if(player != null) {
				List<BetInfo> betList = desk.getPlayerBetInfo().get(playerId);
				
				com.frame.protobuf.PveGameMsg.Player.Builder playerBuilder = com.frame.protobuf.PveGameMsg.Player.newBuilder()
				.setBalance(player.getUser().getBalance() - desk.getAllBetAmount(pid))
				.setHeadImg(player.getUser().getHeadPic() == null ? "": player.getUser().getHeadPic())
				.setHeadImgType(player.getUser().getHeadPicType())
				.setName(player.getUser().getNickName() == null ? "" : player.getUser().getNickName())
				.setOnlineStatus(offlineStatus)
				.setSeatId(desk.getSeatByPid(playerId))
				.setSex(player.getUser().getSex())
				.setLevel(player.getUser().getLevel())
				.setUid(playerId + "");
				
				if(betList != null) {
					for (BetInfo betInfo : betList) {
						playerBuilder.addBetInfoList(com.frame.protobuf.PveGameMsg.BetInfo.newBuilder()
								.setBetAmount(betInfo.getAmont())
								.setBetType(betInfo.getType())
								.build());
					}
				}
				;

				builder.addPlayers(playerBuilder.build());
			}
		}
		

		builder.setDesk(deskBuilder.build());

		int subCmd = SubCmd.Cmd_PushSitDown_VALUE;
		pushMessage(pid, subCmd, builder.build());
	}

	/**
	 * @param userId
	 * @param subCmd
	 * @param message
	 */
	public void pushMessage(long pid, int subCmd, Message message) {
		Player player = PlayerMgr.getInstance(gameType).getPlayer(pid);
		if(player != null){
			pushMessage(player.getPb(), subCmd, message);
		}
	}
	
	/**
	 * @param pb
	 * @param subCmd
	 * @param message
	 */
	private void pushMessage(ProtoBuf pb, int subCmd, Message message) {
		log.info("pushMessage cmd:{}  pid:{} data:{}" , subCmd, pb.getPid(), message == null ? "" : message.toBuilder().toString());
		RocketMqSender.getInstance(gameType).pushPb2Gateway(pb.buildNew(subCmd, message.toByteArray()));
	}
}
