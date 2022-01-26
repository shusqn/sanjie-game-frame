package com.frame.model.game;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import com.frame.entity.Room;
import com.frame.mobel.WaitProtocol;
import com.frame.mobel.card.Card;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BaseDesk {
	public BaseDesk(int deskId, Room config) {
		this.deskId = deskId;
		this.config = config;
		this.seats = new int[getConfig().getSiteCount()];
		for (int i = 0; i < getConfig().getSiteCount(); i++) {
			this.seats[i] = i;
		}
	}
	/**
	 * 桌号
	 */
	private final int deskId;
	/**
	 * 桌台配置id
	 */
	private final  Room config;
	/**
	 * 座位序号
	 */
	private final  int[] seats;
	/**
	 * 局号
	 */
	private long roundId = 0;
	/**
	 * 开局时间
	 */
	private long startTime;
	/**
	 * 桌台当前状态
	 */
	private int state;
	/**
	 * 牌库大小（多少副）
	 */
	private final Vector<Card> cardsRepository = new Vector<>();
	/**
	 * 离线玩家
	 */
	private final Map<Long, Long> offlinePlayerMap = new ConcurrentHashMap<Long, Long>();
	
	/**
	 * 旁观的玩家
	 * key pid, value pid
	 */
	private final Map<Long, Long> lookOnPlayerInfo = new ConcurrentHashMap<Long, Long>();
	/**
	 * 坐下的玩家信息 key seat, value pid
	 */
	private final Map<Integer, Long> deskSeatPlayerInfo = new ConcurrentHashMap<Integer, Long>();
	/**
	 * 玩家下注详情
	 */
	private Map<Long, List<BetInfo>> playerBetInfo = new ConcurrentHashMap<Long, List<BetInfo>>();
	
	/**
	 * 获取玩家下注总额
	 * @param pid
	 * @return
	 */
	public long getAllBetAmount(long pid) {
		long amount = 0;
		Collection<BetInfo> list = getPlayerBetInfo().get(pid);
		if(list != null) {
			for (BetInfo betInfo : list) {
				amount += betInfo.getAmont();
			}
		}
		return amount;
	}
	
	/**
	 * 获取玩家某个下注总额
	 * @param pid
	 * @param playBet
	 * @return
	 */
	public long getAllBetAmountByPlayBet(long pid, int playBet) {
		long amount = 0;
		Collection<BetInfo> list = getPlayerBetInfo().get(pid);
		if(list != null) {
			for (BetInfo betInfo : list) {
				if(betInfo.getType() == playBet) {
					amount += betInfo.getAmont();
				}
			}
		}
		return amount;
	}
	
	/**
	 * @param pid
	 * @return
	 */
	public int getSeatByPid(long pid){
		for (Entry<Integer, Long> entry : getDeskSeatPlayerInfo().entrySet()) {
			int seat = entry.getKey();
			if(pid == entry.getValue()) {
				return seat;
			}
		}
		return -1;
	}

	/**
	 * @param pid
	 */
	public void removeSeatByPid(long pid){
		for (Entry<Integer, Long> entry : getDeskSeatPlayerInfo().entrySet()) {
			int seat = entry.getKey();
			if(pid == entry.getValue()) {
				getDeskSeatPlayerInfo().remove(seat);
				break;
			}
		}
	}
	
	/**
	 * @param pid
	 * @param canSitIndex
	 */
	public void sitdown(long pid, int canSitIndex) {
		getDeskSeatPlayerInfo().put(canSitIndex, pid);
	}
	
	/**
	 * 获取当前可坐下的座位号 -1 为没有座位可以坐下，排队等待
	 * @return
	 */
	public int getCanSitIndex() {
		for (int seatIndex : getSeats()) {
			if(this.getDeskSeatPlayerInfo().get(seatIndex) == null) {
				return seatIndex;
			}
		}
		return -1;
	}
	
	/**
	 * @return
	 */
	public Collection<Long> getAllPids(){
		Collection<Long> playerList = new TreeSet<>();
		playerList.addAll(getLookOnPlayerInfo().values());
		playerList.addAll(getDeskSeatPlayerInfo().values());
		return playerList;
	}

	/**
	 * 如果在游戏中，表示当前等候玩家操作的信息
	 */
	private WaitProtocol waitProtocol;
	
	public abstract void reset();
	
	public abstract int getSortWeight();
}
