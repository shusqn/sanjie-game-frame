package com.frame.pve.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.Id;
import javax.persistence.Transient;

import com.frame.entity.User;
import com.frame.enums.GameType;
import com.frame.pve.PveGameMrg;

import lombok.Getter;
import lombok.Setter;



/**
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
	private int gameType;
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
	 * 剩余的筹码
	 */
	private long balance;
	
	/**
	 * 是否入库了
	 */
	private boolean toDb;
	/**
	 * 抽水
	 */
	private int choushui;
	
	/**
	 * 每个玩法的输赢情况
	 */
	private Map<Integer, BetTypeWinlost> betTypeWinlostMap = new ConcurrentHashMap<Integer, BetTypeWinlost>();
	
	@Transient
	private User user;
	
	public static PlayerWinLost build(long roundId, int gameType, int deskId, long userId) {
		PlayerWinLost info = new PlayerWinLost();
		info.uuId = PveGameMrg.getInstance(GameType.valueOf(gameType)).getIdBuilder().nextId();
		info.roundId = roundId;
		info.userId = userId;
		info.gameType = gameType;
		info.deskId = deskId;
		info.toDb = false;
		return info;
	}

	/**
	 * @return
	 */
	public boolean isWinner() {
		for (BetTypeWinlost betTypeWinlost : betTypeWinlostMap.values()) {
			if(betTypeWinlost.getBetAmount() > 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return
	 */
	public long getWinAmount() {
		long winAmount = 0;
		for (BetTypeWinlost betTypeWinlost : betTypeWinlostMap.values()) {
			if(betTypeWinlost.getBetAmount() > 0) {
				winAmount += betTypeWinlost.getBetAmount();
			}
		}
		return winAmount;
	}
}
