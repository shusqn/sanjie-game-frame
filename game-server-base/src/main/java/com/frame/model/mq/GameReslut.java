package com.frame.model.mq;

import java.util.Vector;

import com.frame.mobel.card.BetType;
import com.frame.mongodto.BaseCollection;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameReslut extends BaseCollection{
	private Vector<BetType> resluts = new Vector<BetType>();
}
