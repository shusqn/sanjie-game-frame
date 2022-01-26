package com.frame.model.game;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.frame.entity.Room;
import com.frame.mobel.card.Card;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * BasePvPDesk.java
 * @author Sanjie
 * @date 2021-09-24 15:41
 * @version 1.0.0
 */
@Getter
@Setter
public abstract class BasePvPDesk extends BaseDesk{
	public BasePvPDesk(int deskId, Room config) {
		super(deskId, config);
		ante = getConfig().getAnte();
	}

	@Setter(AccessLevel.NONE)
	private int currentSeat;
	/**
	 * 当前desk 状态
	 */
	private long bankerPid;
	
	/**
	 * 底注
	 */
	private final long ante;
	
	/**
	 * 当前操作的玩家
	 */
	private long currentPid;
	
	public void setCurrentPid(long currentPid) {
		this.currentPid = currentPid;
		currentSeat = getSeatByPid(currentPid);
	}
	
	/**
	 *准备状态 key pid value pid
	 */
	private final Map<Long, Long> readyPlayerInfo = new ConcurrentHashMap<Long, Long>();
	
	/**
	 *游戏中的玩家信息 key pid value pid
	 */
	private final Map<Long, Long> gamingPlayerInfo = new ConcurrentHashMap<Long, Long>();
	
	/**
	 * 玩家手牌详情
	 */
	private final Map<Long, List<Card>> playerCardsInfo = new ConcurrentHashMap<>();
	
	/**
	 * 下一轮pid
	 * @return
	 */
	public long getNextPid() {
		if(getDeskSeatPlayerInfo().size() == 0) {
			return 0;
		}
		for (int i = 0; i < getSeats().length; i++) {
			int nextSet = i + getCurrentSeat() +1;
			nextSet = nextSet % getSeats().length;
			Long nextPid = getDeskSeatPlayerInfo().get(nextSet);
			if(nextPid != null && getGamingPlayerInfo().get(nextPid) != null) {
				return nextPid;
			}
		}
		return 0;
	}
	
	/**
	 * 获取上一家pid
	 * @return
	 */
	public long getLastPid() {
		if(getDeskSeatPlayerInfo().size() == 0) {
			return 0;
		}
		for (int i = 0; i < getSeats().length; i++) {
			int lastSeat = getCurrentSeat() - i - 1 + getSeats().length;
			lastSeat = lastSeat % getSeats().length;
			Long lastPid = getDeskSeatPlayerInfo().get(lastSeat);
			if(lastPid != null && getGamingPlayerInfo().get(lastPid) != null) {
				return lastPid;
			}
		}
		return 0;
	}
	
	/**
	 * 随机一个banker
	 * @return
	 */
	public long chooseBankerPid() {
		for (int i = 0; i < getSeats().length; i++) {
			int nextSet = i + getSeatByPid(getBankerPid()) +1;
			nextSet = nextSet % getSeats().length;
			Long nextPid = getDeskSeatPlayerInfo().get(nextSet);
			if( nextPid != null && getGamingPlayerInfo().get(nextPid) != null) {
				return nextPid;
			}
		}
		return 0;
	}
}
