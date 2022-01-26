package com.frame.model.game;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import com.frame.entity.Room;
import com.frame.mobel.card.BetType;
import com.frame.mobel.card.Card;
import com.frame.mobel.card.CardsResult;

import lombok.Getter;
import lombok.Setter;

/**
 * BasePvEDesk.java
 * @author Sanjie
 * @date 2021-10-13 16:10
 * @version 1.0.0
 */
@Getter
@Setter
public abstract class BasePvEDesk extends BaseDesk{
	public BasePvEDesk(int deskId, Room config) {
		super(deskId, config);
	}
	/**
	 * 手牌详情
	 */
	private ConcurrentHashMap<Integer, List<Card>> playerCards = new ConcurrentHashMap<>();
	/**
	 * key {@link PlayerType} 牌型结果
	 */
	private ConcurrentHashMap<Integer, CardsResult> cardsResult = new ConcurrentHashMap<>();
	/**
	 * 游戏结果
	 */
	private Vector<BetType> resluts = new Vector<BetType>();
}
