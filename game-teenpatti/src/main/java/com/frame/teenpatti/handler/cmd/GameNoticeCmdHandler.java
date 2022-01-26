package com.frame.teenpatti.handler.cmd;

import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import com.frame.mobel.card.Card;
import com.frame.mobel.card.CardsUtils;
import com.frame.model.Player;
import com.frame.protobuf.TeenpattiMsg;
import com.frame.protobuf.TeenpattiMsg.NoticeBankerInfo;
import com.frame.protobuf.TeenpattiMsg.NoticeDeskStateChange;
import com.frame.protobuf.TeenpattiMsg.NoticePlayerBalance;
import com.frame.protobuf.TeenpattiMsg.NoticePlayerBetAnte;
import com.frame.protobuf.TeenpattiMsg.NoticePlayerCall;
import com.frame.protobuf.TeenpattiMsg.NoticePlayerDropCard;
import com.frame.protobuf.TeenpattiMsg.NoticePlayerRaise;
import com.frame.protobuf.TeenpattiMsg.NoticePlayerSeeCard;
import com.frame.protobuf.TeenpattiMsg.NoticePlayerShow;
import com.frame.protobuf.TeenpattiMsg.NoticePlayerShowResult;
import com.frame.protobuf.TeenpattiMsg.NoticePlayerStatus;
import com.frame.protobuf.TeenpattiMsg.NoticePlayerTurn;
import com.frame.protobuf.TeenpattiMsg.NoticeRefuseOrAgreeShow;
import com.frame.protobuf.TeenpattiMsg.NoticeSitUp;
import com.frame.protobuf.TeenpattiMsg.NoticeUserEnter;
import com.frame.protobuf.TeenpattiMsg.NoticeUserLeave;
import com.frame.protobuf.TeenpattiMsg.NoticeUserOffline;
import com.frame.protobuf.TeenpattiMsg.NoticeUserOnline;
import com.frame.protobuf.TeenpattiMsg.NoticeWinlost;
import com.frame.protobuf.TeenpattiMsg.SubCmd;
import com.frame.protobuf.TeenpattiMsg.WinlostInfo;
import com.frame.teenpatti.manager.DeskMgr;
import com.frame.teenpatti.manager.PlayerMgr;
import com.frame.teenpatti.model.PlayerWinLost;
import com.frame.teenpatti.model.TeenpattiDesk;
import com.frame.teenpatti.utils.MsgUtils;
import com.google.protobuf.Message;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Sanjie
 * @date 2020-11-4 
 */
@Slf4j
public class GameNoticeCmdHandler {
	@Getter
	private static GameNoticeCmdHandler instance = new GameNoticeCmdHandler();

	/**
	 * 	//广播玩家下底注
	message NoticePlayerBetAnte{
	    optional int32 userId=1; //玩家id;
	    optional int32 ante=2;   //底注
	}
	 * @param deskId
	 * @param userId
	 * @param ante
	 */
	public void noticePlayerBetAnte(int deskId,  long userId, long ante) {
		NoticePlayerBetAnte.Builder builder = NoticePlayerBetAnte.newBuilder();
		builder.setUserId(userId + "");
		builder.setAnte(ante);
		int subCmd = SubCmd.Cmd_NoticePlayerBetAnte_VALUE;
		noticeAllRoomPlayer(subCmd, builder.build(), deskId);
	}

	/**
	 *     Cmd_NoticePlayerShowResult  = 1028;               //广播比牌结果
    message NoticePlayerShowResult{
      optional int32 winnerId=1; //赢家id
      optional int32 lostId=2; //输家id
   }  
	 * @param deskId
	 * @param winnerId
	 * @param lostId
	 */
	public void noticePlayerShowResult(int deskId,  long winnerId, long lostId) {
		NoticePlayerShowResult.Builder builder = NoticePlayerShowResult.newBuilder();
		builder.setWinnerId(winnerId + "");
		builder.setLostId(lostId + "");
		int subCmd = SubCmd.Cmd_NoticePlayerShowResult_VALUE;
		noticeAllRoomPlayer(subCmd, builder.build(), deskId);
	}

	/**
	 *     Cmd_NoticeRefuseShow        = 1025;               //广播玩家拒绝比牌
    message NoticeRefuseShow{
      optional int32 userId=1; //玩家id
      optional bool refuse = 2;  //是否拒绝
   }  
	 * @param deskId
	 * @param pid
	 * @param refuse
	 */
	public void noticeRefuseOrAgreeShow(int deskId,  long pid, Boolean agree) {
		NoticeRefuseOrAgreeShow.Builder builder = NoticeRefuseOrAgreeShow.newBuilder();
		builder.setUserId(pid + "");
		builder.setAgree(agree);
		int subCmd = SubCmd.Cmd_NoticeRefuseOrAgreeShow_VALUE;
		noticeAllRoomPlayer(subCmd, builder.build(), deskId);
	}

	/**
	 *     Cmd_NoticePlayerShow        = 1026;               //广播玩家请求和谁比牌
    message NoticePlayerShow{
      optional int32 userId=1; //玩家id
      optional int32 compareUserId=2;  //被比牌玩家id
   }  
	 * @param deskId
	 * @param pid
	 * @param compareUserId
	 */
	public void noticePlayerShow(int deskId,  long pid, long lastPid, long betAmount, long expireTimeMillis) {
		NoticePlayerShow.Builder builder = NoticePlayerShow.newBuilder();
		builder.setUserId(pid + "");
		builder.setCompareUserId(lastPid + "");
		builder.setBetAmount(betAmount);
		builder.setExpireTimeMillis(expireTimeMillis);

		int subCmd = SubCmd.Cmd_NoticePlayerShow_VALUE;
		noticeAllRoomPlayer(subCmd, builder.build(), deskId);
	}

	/**
	 *     Cmd_NoticePlayerRaise       = 1022;               //广播玩家加注
    message NoticePlayerRaise{
      optional int32 userId=1; //玩家id
      optional int32 raiseCount=2; //加注金额
   }
	 * @param deskId
	 * @param pid
	 * @param raiseCount
	 */
	public void noticePlayerRaise(int deskId,  long pid, long raiseAmount, boolean seeCard) {
		NoticePlayerRaise.Builder builder = NoticePlayerRaise.newBuilder();
		builder.setUserId(pid + "");
		builder.setRaiseAmount(raiseAmount);
		builder.setSeeCard(seeCard);
		int subCmd = SubCmd.Cmd_NoticePlayerRaise_VALUE;
		noticeAllRoomPlayer(subCmd, builder.build(), deskId);
	}

	/**
	 * 	Cmd_NoticePlayerCall        = 1020;               //广播玩家跟注
	 message NoticePlayerCall{
	   optional int32 userId=1; //玩家id
	   optional int32 callCount=2; //跟注金额
	} 
	 */
	public void noticePlayerCall(int deskId,  long pid, long callAmount, boolean seeCard) {
		NoticePlayerCall.Builder builder = NoticePlayerCall.newBuilder();
		builder.setUserId(pid + "");
		builder.setCallAmount(callAmount);
		builder.setSeeCard(seeCard);
		int subCmd = SubCmd.Cmd_NoticePlayerCall_VALUE;
		noticeAllRoomPlayer(subCmd, builder.build(), deskId);
	}

	/**
	 *     Cmd_NoticePlayerSeeCard     = 1015;               //广播有玩家看牌
    message NoticePlayerSeeCard{
       optional int32 userId=1; //玩家id
   }
	 * @param deskId
	 * @param pid
	 */
	public void noticePlayerSeeCard(int deskId,  long pid) {
		NoticePlayerSeeCard.Builder builder = NoticePlayerSeeCard.newBuilder();
		builder.setUserId(pid + "");
		int subCmd = SubCmd.Cmd_NoticePlayerSeeCard_VALUE;
		noticeAllRoomPlayer(subCmd, builder.build(), deskId);
	}

	/**
	 * 	Cmd_NoticePlayerTurn        = 1013;               //广播该轮到哪个玩家操作了
	 message NoticePlayerTurn{
	   optional int32 userId=1; //玩家id
	    optional int64 expireTimeMillis=2; //有效操作到期时间戳
        optional int32 countdownTime=3; //倒计时秒数
	}
	 * @param deskId
	 * @param pid
	 */
	public void noticePlayerTurn(TeenpattiDesk desk,  long pid,  long expireTimeMillis) {
		NoticePlayerTurn.Builder builder = NoticePlayerTurn.newBuilder();
		builder.setUserId(pid + "");
		builder.setExpireTimeMillis(expireTimeMillis);
		builder.setAnte(desk.getNowAnte());
		int subCmd = SubCmd.Cmd_NoticePlayerTurn_VALUE;
		noticeAllRoomPlayer(subCmd, builder.build(), desk.getDeskId());
	}

	/**
	 *     Cmd_NoticeReady             = 993;                //广播玩家已经准备好了
    message NoticeReady{
        optional int32 userId=1; //玩家id;
    }
	 * @param deskId
	 * @param pid
	 */
	public void noticePlayerStatus(TeenpattiDesk desk,  long pid) {
		NoticePlayerStatus.Builder builder = NoticePlayerStatus.newBuilder();
		builder.setUserId(pid + "");
		builder.setGaming(desk.getGamingPlayerInfo().get(pid) == null ? false : true);
		builder.setReady(desk.getReadyPlayerInfo().get(pid) == null ? false : true);
		int subCmd = SubCmd.Cmd_NoticePlayerStatus_VALUE;
		noticeAllRoomPlayer(subCmd, builder.build(), desk.getDeskId());
	}

	/**
	 *  Cmd_NoticePlayerDropCard    = 1018;               //广播有玩家弃牌了
	 *   message NoticePlayerDropCard{
    optional int32 userId=1; //玩家id
    optional int64 winLostGold = 2; //输赢的筹码
    optional int64 balance = 3;//剩余的筹码
} 
	 */
	public void noticePlayerDropCard(int deskId,  long pid, long winLostGold, long balance, int dropCardType) {
		NoticePlayerDropCard.Builder builder = NoticePlayerDropCard.newBuilder();

		builder.setUserId(pid + "");
		builder.setWinLostGold(winLostGold);
		builder.setBalance(balance);
		builder.setDropCardType(dropCardType);

		int subCmd = SubCmd.Cmd_NoticePlayerDropCard_VALUE;
		noticeAllRoomPlayer(subCmd, builder.build(), deskId);
	}

	/**
	 * 	Cmd_NoticePlayerBalance     = 1026;               //广播玩家金币改变
	 * @param deskId
	 * @param pid
	 * @param balance
	 */
	public void noticePlayerBalance(int deskId,  long pid, long balance) {
		NoticePlayerBalance.Builder builder = NoticePlayerBalance.newBuilder();
		builder.setUserId(pid + "");
		builder.setBalance(balance);
		int subCmd = SubCmd.Cmd_NoticePlayerBalance_VALUE;
		noticeAllRoomPlayer(subCmd, builder.build(), deskId);
	}

	/**
	 * @param deskId
	 * @param pid
	 */
	public void noticeSitUp(int deskId,  long pid) {
		NoticeSitUp.Builder builder = NoticeSitUp.newBuilder();
		builder.setUserId(pid + "");
		int subCmd = SubCmd.Cmd_NoticeSitUp_VALUE;
		noticeAllRoomPlayer(subCmd, builder.build(), deskId);
	}

	/**
	 * @param deskId
	 * @param pid
	 */
	public void noticeUserOnline(int deskId,  long pid) {
		NoticeUserOnline.Builder builder = NoticeUserOnline.newBuilder();
		builder.setUserId(pid + "");

		int subCmd = SubCmd.Cmd_NoticeUserOnline_VALUE;
		noticeAllRoomPlayer(subCmd, builder.build(), deskId);
	}

	/**
	 * @param roomId
	 * @param pid
	 */
	public void noticeUserOffline(int deskId,  long pid) {
		NoticeUserOffline.Builder builder = NoticeUserOffline.newBuilder();
		builder.setUserId(pid + "");

		int subCmd = SubCmd.Cmd_NoticeUserOffline_VALUE;
		noticeAllRoomPlayer(subCmd, builder.build(), deskId);
	}

	/**
	 * @param roomId
	 * @param infoList
	 */
	public void noticeWinlost(TeenpattiDesk desk,  Collection<PlayerWinLost> infoList) {
		NoticeWinlost.Builder builder = NoticeWinlost.newBuilder();

		Collection<Long> playerList = new TreeSet<Long>();
		for (PlayerWinLost info : infoList) {
			if(info.isDropCard()) {
				continue;
			}
			playerList.add(info.getUserId());
			
			List<Card> cards = desk.getPlayerCardsInfo().get(info.getUserId());

			builder.addWinlostInfoList(WinlostInfo.newBuilder()
					.setWinAmount(info.getWinAmount())
					.setBetAmount(desk.getAllBetAmount(info.getUserId()))
					.setBalance(info.getBalance())
					.setUserId(info.getUserId() + "")
					.setWinner(info.isWinner())
					.addAllCards(info.getCardsList())
					.setName(info.getUser().getNickName())
					.setHeadImg(info.getUser().getHeadPic())
					.setHeadImgType(info.getUser().getHeadPicType())
					.setCardResult((cards== null || cards.size() == 0) ? 0 : CardsUtils.getJinhuaCardsResult(cards).getValue())
					.build()
					);
		}

		int subCmd = SubCmd.Cmd_NoticeWinlost_VALUE;
		noticeAllRoomPlayer(subCmd, builder.build(), desk.getDeskId());
		//noticeAllRoomPlayer(subCmd, builder.build(), playerList);
	}

	/**
	 * @param roomId
	 * @param pid
	 */
	public void noticeUserLeave(int deskId, long pid) {
		NoticeUserLeave.Builder builder = NoticeUserLeave.newBuilder();
		builder.setUserId(pid + "");

		int subCmd = SubCmd.Cmd_NoticeUserLeave_VALUE;
		noticeAllRoomPlayer(subCmd, builder.build(), deskId);
	}

	/**
	 * @param roomId
	 * @param seatId
	 * @param pid
	 */
	public void noticeUserEnter(TeenpattiDesk desk, int seatId, long pid, boolean enterDesk) {
		NoticeUserEnter.Builder builder = NoticeUserEnter.newBuilder();

		Player player = PlayerMgr.getInstance().getPlayer(pid);

		TeenpattiMsg.Player.Builder playerBuilder = TeenpattiMsg.Player.newBuilder();
		playerBuilder.setBalance(player.getUser().getBalance())
		.setHeadPic(player.getUser().getHeadPic() == null ? "":player.getUser().getHeadPic())
		.setHeadPicType(player.getUser().getHeadPicType())
		.setName(player.getUser().getNickName() == null ? "": player.getUser().getNickName())
		.setOnlineStatus(2)
		.setSeatId(desk.getSeatByPid(pid))
		.setSex(player.getUser().getSex())
		.setUserId(player.getPid() + "")
		.setGaming(desk.getGamingPlayerInfo().get(pid) == null ? false :  true)
		.setReady(desk.getReadyPlayerInfo().get(pid) == null ? false : true)
		.setBlindsCount(desk.getPlayerBlindsInfo().computeIfAbsent(pid, k -> 0))
		.setVipLevel(player.getUser().getLevel());

		builder.setPlayer(playerBuilder.build());

		int subCmd = SubCmd.Cmd_NoticeUserEnter_VALUE;
		if(enterDesk) {
			noticeAllRoomPlayerExceptSelf(subCmd, builder.build(), desk.getDeskId(), pid);
		}
		else {
			noticeAllRoomPlayer(subCmd, builder.build(), desk.getDeskId());
		}
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
	public void noticeDeskStateChange(int roomId, int state, long expireTimeMillis) {
		NoticeDeskStateChange.Builder builder = NoticeDeskStateChange.newBuilder();
		builder.setState(state);
		builder.setExpireTimeMillis(expireTimeMillis);
		int subCmd = SubCmd.Cmd_NoticeDeskStateChange_VALUE;
		noticeAllRoomPlayer(subCmd, builder.build(), roomId);
	}

	/**
	 *     Cmd_NoticeBankerInfo        = 1008;               //广播谁是庄家 <p>
    message NoticeBankerInfo{<br/>
        optional int32 userId=1;//玩家id;<br/>
    }<p>
	 * @param roomId
	 * @param pid
	 */
	public void noticeBankerInfo(int roomId, long pid) {
		NoticeBankerInfo.Builder builder = NoticeBankerInfo.newBuilder();
		builder.setUserId(pid + "");

		int subCmd = SubCmd.Cmd_NoticeBankerInfo_VALUE;
		noticeAllRoomPlayer(subCmd, builder.build(), roomId);
	}
	
	
	/**
	 * 群发
	 * @param subCmd
	 * @param message
	 * @param roomId
	 */
	public void noticeAllRoomPlayer(int subCmd, Message message, int deskId) {
		TeenpattiDesk desk = DeskMgr.getInstance().getDesk(deskId);
		if (desk == null) return;

		Collection<Long> playerList = desk.getAllPids();

		if (playerList == null || playerList.size() == 0) return;
		noticeAllRoomPlayer(subCmd, message, playerList);
	}

	/**
	 * @param subCmd
	 * @param message
	 * @param roomId
	 * @param selfPid
	 */
	public void noticeAllRoomPlayerExceptSelf(int subCmd, Message message, int deskId, long selfPid) {
		TeenpattiDesk desk = DeskMgr.getInstance().getDesk(deskId);
		if (desk == null) return;

		Collection<Long> playerList = desk.getAllPids();
		if (playerList == null || playerList.size() == 0) return;
		playerList.remove(Long.valueOf(selfPid));
		
		noticeAllRoomPlayer(subCmd, message, playerList);
	}

	/**
	 * @param subCmd
	 * @param message
	 * @param playerList
	 */
	public void noticeAllRoomPlayer(int subCmd, Message message, Collection<Long> playerList) {
		for (Long pid : playerList) {
			Player player = PlayerMgr.getInstance().getPlayer(pid);
			if(player == null) {
				continue;
			}
			pushMessage(pid, subCmd, message);
		}
		if(playerList.size() > 0) {
			log.info("noticeAllRoomPlayer： cmd:{}  pids:{} data:{}" , subCmd, playerList , message.toBuilder().toString());
		}
	}
	
	/**
	 * @param userId
	 * @param subCmd
	 * @param message
	 */
	private void pushMessage(long pid, int subCmd, Message message) {
		Player player = PlayerMgr.getInstance().getPlayer(pid);
		if(player != null){
			MsgUtils.pushToGateway(player.getPb().buildNew(subCmd, message.toByteArray()));
		}
	}

}
