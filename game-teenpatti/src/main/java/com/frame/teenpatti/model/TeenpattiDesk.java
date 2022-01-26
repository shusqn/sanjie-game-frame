package com.frame.teenpatti.model;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import com.frame.entity.Room;
import com.frame.mobel.game.Operate;
import com.frame.model.game.BasePvPDesk;
import com.frame.model.game.BetInfo;
import com.frame.protobuf.TeenpattiMsg.DeskState;
import com.frame.teenpatti.handler.GameDeskHandler;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeenpattiDesk extends BasePvPDesk{
	public TeenpattiDesk(int deskId, Room config) {
		super(deskId, config);
	}

	/**
	 * 赢家pid
	 */
	private long winnerPid;
	
	/**
	 * 当前底注
	 */
	private long nowAnte;

	/**
	 * 玩家总的输赢情况 key 为pid
	 */
	private final Map<Long, PlayerWinLost> winLostInfoMap = new ConcurrentHashMap<Long, PlayerWinLost>();
	
	/**
	 *已经看牌的玩家信息
	 */
	private final Map<Long, Long> playerSeeCardstInfo = new ConcurrentHashMap<>();
	/**
	 *拒绝比牌的次数
	 */
	private final Map<Long, Integer> playerRefuseInfo = new ConcurrentHashMap<>();
	/**
	 *暗柱次数
	 */
	private final Map<Long, Integer> playerBlindsInfo = new ConcurrentHashMap<>();
	/**
	 * 玩家的操作列表
	 */
	private final  Vector<Operate> playerOperateList = new Vector<>();
	
	/**
	 * 主要游戏桌面操作对象
	 */
	private final GameDeskHandler deskHandler = new GameDeskHandler(this);
	
	//=======================================
	/**
	 * 是否超出最大底池金币数
	 * @param winnerPid
	 * @return
	 */
	public boolean isOverMaxGameChip() {
		long totalAmount = 0;
		for (Entry<Long, List<BetInfo>> entry : getPlayerBetInfo().entrySet()) {
			Long pid = entry.getKey();
			totalAmount += getAllBetAmount(pid);
		}
		if(totalAmount >= getConfig().getMaxBet()) {
			return true;
		}
		return false;
	}
	
	/**
	 * @return
	 */
	public long getTotalBet() {
		return  getTotalBetWithoutWiner(new TreeSet<>());
	}
	
	/**
	 * 除开赢家所有玩家的下注总额
	 * @param winnerPid
	 * @return
	 */
	public long getTotalBetWithoutWiner(Set<Long> winPids) {
		int totalAmount = 0;
		for (Entry<Long, List<BetInfo>> entry : getPlayerBetInfo().entrySet()) {
			Long pid = entry.getKey();
			if(!winPids.contains(Long.valueOf(pid))) {
				totalAmount += getAllBetAmount(pid);
			}
		}
		return totalAmount;
	}
	
	
	/**
	 * 重置
	 */
	@Override
	public void reset() {
		getPlayerCardsInfo().clear();
		getPlayerSeeCardstInfo().clear();
		getPlayerBetInfo().clear();
		getWinLostInfoMap().clear();
		getPlayerRefuseInfo().clear();
		getPlayerBlindsInfo().clear();
		getPlayerOperateList().clear();
		
		setNowAnte(getAnte());
	}
	
	/**
	 * 是否可以立即离开桌子
	 * @return
	 */
	public boolean isCanLeaveDesk() {
		if(getState()  == DeskState.RESLUT_SHOW_VALUE || getState()  == DeskState.START_COUNTDOWN_VALUE  || 
				getState()  == DeskState.WAIT_VALUE) {
			return true;
		}
		return false;
	}
	
	/**
	 * 获取排序权重
	 * @return
	 */
	public int getSortWeight() {
		if(this.getDeskSeatPlayerInfo().size() >= getSeats().length) {
			return (0 - getAllPids().size());
		}
		if(this.getDeskSeatPlayerInfo().size() < getSeats().length) {
			if(this.getDeskSeatPlayerInfo().size() != 0 && (getState() == DeskState.WAIT_VALUE || 
					getState() == DeskState.START_COUNTDOWN_VALUE || getState()  == DeskState.RESLUT_SHOW_VALUE) ) {
				return (getSeats().length + this.getDeskSeatPlayerInfo().size());
			}
			return this.getDeskSeatPlayerInfo().size();
		}
		return  0;
	}
}
