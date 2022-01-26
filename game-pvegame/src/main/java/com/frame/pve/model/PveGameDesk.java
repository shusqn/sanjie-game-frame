package com.frame.pve.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.frame.entity.Room;
import com.frame.model.game.BasePvEDesk;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class PveGameDesk extends BasePvEDesk {
	public PveGameDesk(int deskId, Room config) {
		super(deskId, config);
	}
	
	@Override
	public void reset() {
		getPlayerBetInfo().clear();
		getCardsResult().clear();
		getResluts().clear();
		getPlayerCards().clear();
		getWinLostInfoMap().clear();
	}
	
	/**
	 * 玩家总的输赢情况 key 为pid
	 */
	private Map<Long, PlayerWinLost> winLostInfoMap = new ConcurrentHashMap<Long, PlayerWinLost>();
	
	@Override
	public int getSortWeight() {
		if(getAllPids().size() >= getSeats().length) {
			return 0;
		}
		return getAllPids().size();
	}
}
