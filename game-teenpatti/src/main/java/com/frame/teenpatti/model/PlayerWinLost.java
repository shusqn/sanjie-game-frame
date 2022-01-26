package com.frame.teenpatti.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Id;
import javax.persistence.Transient;

import com.frame.entity.User;
import com.frame.entity.User;
import com.frame.mobel.card.Card;
import com.frame.teenpatti.TeenpattiMrg;

import lombok.Getter;
import lombok.Setter;



/**
 * 拉米玩家结算信息
 * @author Sanjie
 * @date 2020-11-5 
 * <p>Description: </p>  
 */
@Getter
@Setter
public class PlayerWinLost {
	@Id
	private long uuId;
	
	/**
	 * 游戏id
	 */
	private int gameId;
	/**
	 *当局的局号
	 */
	private long roundId;
	/**
	 * 
	 */
	private int deskId;
	
	/**
	 * 玩家userId
	 */
	private long userId;
	/**
	 * 是否是赢家
	 */
	private boolean winner;
	
	/**
	 *输赢的筹码
	 */
	private long winAmount;
	/**
	 * 剩余的筹码
	 */
	private long balance;
	
	/**
	 * 是否弃牌
	 */
	private boolean dropCard = false;
	
	/**
	 * 因为什么弃牌
	 */
	private int dropCardType;
	
	/**
	 * 是否入库了
	 */
	private boolean toDb;
	/**
	 * 抽水
	 */
	private int choushui;
	/**
	 * 最后结算时的牌型
	 */
	private String cardsListJson;
	
	@Transient
	private List<Integer> cardsList;
	
	@Transient
	private List<Card> cardsListCard;
	@Transient
	private User user;
	
	public static PlayerWinLost build(long roundId, int gameId, int deskId, long userId, boolean winner, List<Card> cardsList, boolean dropCard, int dropCardType) {
		PlayerWinLost info = new PlayerWinLost();
		info.uuId = TeenpattiMrg.getInstance().getIdBuilder().nextId();
		info.roundId = roundId;
		info.userId = userId;
		info.winner = winner;
		info.cardsList = new ArrayList<>();
		info.cardsListCard = cardsList;
		info.gameId = gameId;
		info.deskId = deskId;
		info.dropCard = dropCard;
		info.dropCardType = dropCardType;
		info.toDb = false;
		cardsList = cardsList == null ? new ArrayList<Card>() : cardsList;
		for (Card card : cardsList) {
			info.cardsList.add(card.getData());
		}
		return info;
	}
}
