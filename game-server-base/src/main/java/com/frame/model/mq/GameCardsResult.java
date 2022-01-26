package com.frame.model.mq;

import java.util.concurrent.ConcurrentHashMap;

import com.frame.mobel.card.CardsResult;
import com.frame.mobel.card.PlayerType;
import com.frame.mongodto.BaseCollection;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameCardsResult extends BaseCollection{
	/**
	 * key {@link PlayerType}
	 */
	private ConcurrentHashMap<Integer, CardsResult> cardsResult = new ConcurrentHashMap<>();
}
