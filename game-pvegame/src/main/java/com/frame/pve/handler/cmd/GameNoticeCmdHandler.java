package com.frame.pve.handler.cmd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import com.frame.enums.GameType;
import com.frame.mobel.card.BetType;
import com.frame.mobel.card.Card;
import com.frame.mobel.card.CardsResult;
import com.frame.model.Player;
import com.frame.protobuf.PveGameMsg;
import com.frame.protobuf.PveGameMsg.NoticeBet;
import com.frame.protobuf.PveGameMsg.NoticeCardsResult;
import com.frame.protobuf.PveGameMsg.NoticeDeskStateChange;
import com.frame.protobuf.PveGameMsg.NoticeGameResult;
import com.frame.protobuf.PveGameMsg.NoticePlayerCards;
import com.frame.protobuf.PveGameMsg.NoticeUserLeave;
import com.frame.protobuf.PveGameMsg.NoticeWinlost;
import com.frame.protobuf.PveGameMsg.PlayerCards;
import com.frame.protobuf.PveGameMsg.PlayerCardsResult;
import com.frame.protobuf.PveGameMsg.SubCmd;
import com.frame.protobuf.PveGameMsg.WinlostInfo;
import com.frame.pve.manager.DeskMgr;
import com.frame.pve.manager.PlayerMgr;
import com.frame.pve.model.BetTypeWinlost;
import com.frame.pve.model.PlayerWinLost;
import com.frame.pve.model.PveGameDesk;
import com.frame.pve.rocketmq.RocketMqSender;
import com.google.protobuf.Message;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Sanjie
 * @date 2020-11-4 
 */
@Slf4j
public final class GameNoticeCmdHandler {
	private static Map<Integer, GameNoticeCmdHandler> instanceMap = new ConcurrentHashMap<Integer, GameNoticeCmdHandler>();
	public static GameNoticeCmdHandler getInstance(GameType gameType) {
		GameNoticeCmdHandler instance = instanceMap.get(gameType.getType());
		if(instance == null) {
			instance = new GameNoticeCmdHandler();
			instance.gameType = gameType;
			instanceMap.put(gameType.getType(), instance);
		}
		return instance;
	}
	private GameType gameType;
	//==================================
	/**
	 * @param deskId
	 * @param pid
	 * @param betType
	 * @param betAmount
	 */
	public void noticeBet(int deskId, long pid, int betType, long betAmount) {
		NoticeBet.Builder builder = NoticeBet.newBuilder()
				.setBetAmount(betAmount)
				.setBetType(betType)
				.setPid(pid + "");

		int subCmd = SubCmd.Cmd_NoticeBet_VALUE;
		noticeAllRoomPlayer(subCmd, builder.build(), deskId);
	}

	/**
	 * @param desk
	 */
	public void noticeGameReslut(PveGameDesk desk) {
		NoticeGameResult.Builder builder = NoticeGameResult.newBuilder();

		for (BetType betType : desk.getResluts()) {
			builder.addBetTypes(betType.getValue());
		}

		int subCmd = SubCmd.Cmd_NoticeGameResult_VALUE;
		noticeAllRoomPlayer(subCmd, builder.build(), desk.getDeskId());
	}

	/**
	 * @param desk
	 */
	public void noticeCardsResult(PveGameDesk desk) {
		NoticeCardsResult.Builder builder = NoticeCardsResult.newBuilder();

		for (Entry<Integer, CardsResult> entry : desk.getCardsResult().entrySet()) {
			builder.addPlayerCardsResults(
					PlayerCardsResult.newBuilder()
					.setPlayerType(entry.getKey())
					.setCardsResult(entry.getValue().getValue())
					.build());
		}

		int subCmd = SubCmd.Cmd_NoticeCardsResult_VALUE;
		noticeAllRoomPlayer(subCmd, builder.build(), desk.getDeskId());
	}

	/**
	 * @param desk
	 */
	public void noticePlayerCards(PveGameDesk desk) {
		NoticePlayerCards.Builder builder = NoticePlayerCards.newBuilder();

		for (Entry<Integer, List<Card>> entry : desk.getPlayerCards().entrySet()) {
			PlayerCards.Builder playerCards = PlayerCards.newBuilder();
			playerCards.setPlayerType(entry.getKey());
			for (Card card : entry.getValue()) {
				playerCards.addCards(card.getData());
			}
		}

		int subCmd = SubCmd.Cmd_NoticePlayerCards_VALUE;
		noticeAllRoomPlayer(subCmd, builder.build(), desk.getDeskId());
	}
	/**
	 * @param roomId
	 * @param infoList
	 */
	public void noticeWinlost(int deskId,  Collection<PlayerWinLost> infoList) {
		NoticeWinlost.Builder builder = NoticeWinlost.newBuilder();

		Collection<Long> playerList = new TreeSet<Long>();
		for (PlayerWinLost info : infoList) {
			playerList.add(info.getUserId());

			List<PveGameMsg.BetTypeWinlost> betTypWinlostList = new ArrayList<>();
			for (BetTypeWinlost betTypeWinlost : info.getBetTypeWinlostMap().values()) {
				betTypWinlostList.add(
						PveGameMsg.BetTypeWinlost.newBuilder()
						.setBetAmount(betTypeWinlost.getBetAmount())
						.setBetType(betTypeWinlost.getBetType())
						.setWinlost(betTypeWinlost.getWinlost()).build()
						);
			}

			builder.addWinlostInfoList(WinlostInfo.newBuilder()
					.setBalance(info.getBalance())
					.setPid(info.getUserId() + "")
					.setWinner(info.isWinner())
					.setName(info.getUser().getNickName())
					.setHeadImg(info.getUser().getHeadPic())
					.setHeadImgType(info.getUser().getHeadPicType())
					.addAllBetTypeWinlosts(betTypWinlostList)
					.build()
					);
		}

		int subCmd = SubCmd.Cmd_NoticeWinlost_VALUE;
		noticeAllRoomPlayer(subCmd, builder.build(), deskId);
	}

	/**
	 * @param roomId
	 * @param pid
	 */
	public void noticeUserLeave(int deskId, long pid) {
		NoticeUserLeave.Builder builder = NoticeUserLeave.newBuilder();
		builder.setPid(pid + "");

		int subCmd = SubCmd.Cmd_NoticeUserLeave_VALUE;
		noticeAllRoomPlayer(subCmd, builder.build(), deskId);
	}

	/**
	 * Cmd_NoticeDeskStateChange   = 1001;               //推送房间状态改变<p>
    message NoticeDeskStateChange{<br/>
        optional int32 state=1;//房间状态<br/>
        optional int64 expireTimeMillis=2;//房间状态到期时间戳<br/>
    }<p>
	 * @param roomId
	 * @param state
	 * @param expireTimeMillis
	 */
	public void noticeDeskStateChange(int deskId, int state, long expireTimeMillis) {
		NoticeDeskStateChange.Builder builder = NoticeDeskStateChange.newBuilder();
		builder.setState(state);
		builder.setExpireTimeMillis(expireTimeMillis);
		int subCmd = SubCmd.Cmd_NoticeDeskStateChange_VALUE;
		noticeAllRoomPlayer(subCmd, builder.build(), deskId);
	}

	/**
	 * 群发
	 * @param subCmd
	 * @param message
	 * @param roomId
	 */
	public void noticeAllRoomPlayer(int subCmd, Message message, int deskId) {
		PveGameDesk desk = DeskMgr.getInstance(gameType).getDesk(deskId);
		if (desk == null) {
			return;
		}

		Collection<Long> playerList = desk.getAllPids();

		if (playerList == null || playerList.size() == 0) {
			return;
		}

		noticeAllRoomPlayer(subCmd, message, playerList, deskId);
	}

	/**
	 * @param subCmd
	 * @param message
	 * @param playerList
	 */
	public void noticeAllRoomPlayer(int subCmd, Message message, Collection<Long> playerList,  int deskId) {
		for (Long pid : playerList) {
			Player player = PlayerMgr.getInstance(gameType).getPlayer(pid);
			if(player == null) {
				continue;
			}
			pushMessage(pid, subCmd, message);
		}
		if(playerList.size() > 0) {
			log.info("deskId {} noticeAllRoomPlayer： cmd:{}  pids:{} data:{}" , deskId, subCmd, playerList , message.toString());
		}
	}

	/**
	 * @param userId
	 * @param subCmd
	 * @param message
	 */
	private void pushMessage(long pid, int subCmd, Message message) {
		Player player = PlayerMgr.getInstance(gameType).getPlayer(pid);
		if(player != null){
			RocketMqSender.getInstance(gameType).pushPb2Gateway(player.getPb().buildNew(subCmd, message.toByteArray()));
		}
	}

}
