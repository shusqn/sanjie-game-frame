package com.frame.teenpatti.handler.cmd;

import java.util.Collection;
import java.util.List;

import com.frame.mobel.ProtoBuf;
import com.frame.mobel.card.Card;
import com.frame.mobel.card.CardsUtils;
import com.frame.model.Player;
import com.frame.model.game.BetInfo;
import com.frame.protobuf.GatewayMsg;
import com.frame.protobuf.TeenpattiMsg.CardResult;
import com.frame.protobuf.TeenpattiMsg.Desk;
import com.frame.protobuf.TeenpattiMsg.PushSeeCard;
import com.frame.protobuf.TeenpattiMsg.PushShowCardResult;
import com.frame.protobuf.TeenpattiMsg.PushSitDown;
import com.frame.protobuf.TeenpattiMsg.SubCmd;
import com.frame.teenpatti.manager.PlayerMgr;
import com.frame.teenpatti.model.TeenpattiDesk;
import com.frame.teenpatti.utils.MsgUtils;
import com.frame.utils.ArrayUtils;
import com.google.protobuf.Message;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;


/**
 * @author Sanjie
 * @date 2020-11-4 
 */
@Slf4j
public class GamePushCmdHandler {
	@Getter
	private static GamePushCmdHandler instance = new GamePushCmdHandler();

	/**
	 *     Cmd_PushShowCardResult      = 1027;               //推送双方的比牌牌型
    message PushShowCardResult{
    	   repeated CardResult cardResultList=1; //比牌牌型
    	} 
    message CardResult{
    	   optional int32 userId=1; //玩家id
    	   repeated int32 cards = 2;//玩家手牌详情
    	   optional int32 cardResult=3; //牌型
    	}  
	 * @param pid
	 * @param otherPid
	 */
	public void pushShowCardResult(TeenpattiDesk desk, long pid, long otherPid) {
		PushShowCardResult.Builder builder = PushShowCardResult.newBuilder();
		long[] arr = {pid, otherPid};
		List<Card> cards = desk.getPlayerCardsInfo().get(pid);
		for (long ppid : arr) {
			if(desk.getPlayerSeeCardstInfo().get(ppid) != null) {
				CardResult.Builder cardResultBuilder = CardResult.newBuilder();
				cardResultBuilder.setUserId(ppid + "");
				cardResultBuilder.setCardResult(CardsUtils.getJinhuaCardsResult(cards).getValue());
				for (Card card : desk.getPlayerCardsInfo().get(ppid)) {
					cardResultBuilder.addCards(card.getData());
				}
				builder.addCardResultList(cardResultBuilder.build());
			}
		}
		int subCmd = SubCmd.Cmd_PushShowCardResult_VALUE;
		for (long ppid : arr) {
			if(desk.getPlayerSeeCardstInfo().get(ppid) != null) {
				pushMessage(ppid, subCmd, builder.build());
			}
		}
	}

	/**
	 *     Cmd_PushSeeCard             = 1016;               //推送玩家具体看到的牌
    message PushSeeCard{
        repeated int32 cards = 1;//玩家手牌详情
    }
	 * @param pid
	 * @param cards
	 */
	public void pushSeeCard(long pid, Collection<Card> cards) {
		PushSeeCard.Builder builder = PushSeeCard.newBuilder();
		if(cards == null) {
			return;
		}
		for (Card card : cards) {
			builder.addCards(card.getData());
		}
		int subCmd = SubCmd.Cmd_PushSeeCard_VALUE;
		pushMessage(pid, subCmd, builder.build());
	}

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
	public void pushSitDown(long pid, TeenpattiDesk desk) {
		PushSitDown.Builder builder = PushSitDown.newBuilder();

		Desk.Builder deskBuilder = Desk.newBuilder();
		
		deskBuilder
		.setDeskId(desk.getDeskId())
		.setState(desk.getState())
		.setUserId(desk.getCurrentPid() + "")
		.setExpireTimeMillis(desk.getWaitProtocol().getExpireTimeMillis())
		.setNowAnte(desk.getNowAnte())
		.setBankerUserId(desk.getBankerPid() + "")
		.setBetTotalAmount(desk.getTotalBet())
		.setShowCardTipStaus(desk.getWaitProtocol().isHaveCode(SubCmd.Cmd_ReqRefuseOrAgreeShow_VALUE) ? true : false)
		.setShowCardPid(desk.getWaitProtocol().getPid() + "")
		;
		
		Collection<Long> playerList = ArrayUtils.copeCollection(desk.getDeskSeatPlayerInfo().values());
		playerList.add(pid);

		for (Long playerId : playerList) {
			Player player = PlayerMgr.getInstance().getPlayer(playerId);
			int offlineStatus = desk.getOfflinePlayerMap().get(player.getPid()) != null ? 2: 1;
			if(player != null) {
				List<BetInfo> betList = desk.getPlayerBetInfo().get(playerId);
				
				com.frame.protobuf.TeenpattiMsg.Player.Builder playerBuilder = com.frame.protobuf.TeenpattiMsg.Player.newBuilder()
				.setBalance(player.getUser().getBalance() - desk.getAllBetAmount(pid))
				.setHeadPic(player.getUser().getHeadPic() == null ? "": player.getUser().getHeadPic())
				.setHeadPicType(player.getUser().getHeadPicType())
				.setName(player.getUser().getNickName() == null ? "" : player.getUser().getNickName())
				.setOnlineStatus(offlineStatus)
				.setSeatId(desk.getSeatByPid(playerId))
				.setSex(player.getUser().getSex())
				.setUserId(playerId + "")
				.setReady(desk.getReadyPlayerInfo().get(player.getPid()) == null ? false : true)
				.setGaming(desk.getGamingPlayerInfo().get(playerId) == null ? false : true)
				.setBetAmount(desk.getAllBetAmount(pid))
				.setIsSeeCard(desk.getPlayerSeeCardstInfo().get(playerId) == null ? false : true)
				.setLastBetAmount(betList == null ? 0 : betList.get(betList.size() - 1).getAmont())
				.setBlindsCount(desk.getPlayerBlindsInfo().computeIfAbsent(pid, k -> 0))
				;

				deskBuilder.addPlayers(playerBuilder.build());
			}
		}
		
		if(desk.getPlayerSeeCardstInfo().get(pid) != null) {
			for (Card card : desk.getPlayerCardsInfo().get(pid)) {
				builder.addHandCards(card.getData());
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
		Player player = PlayerMgr.getInstance().getPlayer(pid);
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
		MsgUtils.pushToGateway(pb.buildNew(subCmd, message.toByteArray()));
	}
}
