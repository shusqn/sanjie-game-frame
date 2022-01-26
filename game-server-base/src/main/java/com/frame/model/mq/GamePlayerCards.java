package com.frame.model.mq;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.frame.mobel.card.Card;
import com.frame.mobel.card.PlayerType;
import com.frame.mongodto.BaseCollection;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GamePlayerCards extends BaseCollection{
	/**
	 * key {@link PlayerType}
	 */
	private ConcurrentHashMap<Integer, List<Card>> playerCards = new ConcurrentHashMap<>();
}


