package com.frame.pve.handler;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.frame.entity.User;
import com.frame.enums.GameType;
import com.frame.executor.DbExecutorHandler;
import com.frame.mobel.card.BetType;
import com.frame.model.Player;
import com.frame.model.game.BetInfo;
import com.frame.protobuf.TeenpattiMsg.DropCardType;
import com.frame.pve.handler.cmd.GameNoticeCmdHandler;
import com.frame.pve.manager.PlayerMgr;
import com.frame.pve.model.BetTypeWinlost;
import com.frame.pve.model.PlayerWinLost;
import com.frame.pve.model.PveGameDesk;
import com.frame.service.UserAccountsService;
import com.frame.service.UserService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Sanjie
 * @date 2020-11-4 
 * <p>Title: GameWinLostHandler.java</p>  
 * <p>Description: </p>  
 */
@Slf4j
public class GameCalcuHandler {
	private static Map<Integer, GameCalcuHandler> instanceMap = new ConcurrentHashMap<Integer, GameCalcuHandler>();
	public static GameCalcuHandler getInstance(GameType gameType) {
		GameCalcuHandler instance = instanceMap.get(gameType.getType());
		if(instance == null) {
			instance = new GameCalcuHandler();
			instance.gameType = gameType;
			instanceMap.put(gameType.getType(), instance);
		}
		return instance;
	}
	private GameType gameType;
	//==================================
	private List<BetType> betList_dt = Arrays.asList(BetType.DT_DRAGON_WIN, BetType.DT_TIGER_WIN, BetType.DT_TIE);
	/**
	 * 玩家弃牌
	 * @param pid
	 * @param desk
	 * @param dropType {@link DropCardType}
	 */
	public void gameCalcu(PveGameDesk desk) {
		List<BetType> betList = null;
		if(desk.getConfig().getGameType() == GameType.DT.getType()) {
			betList = betList_dt;
		}
		for (Entry<Long, List<BetInfo>> entry : desk.getPlayerBetInfo().entrySet()) {
			long pid = entry.getKey();
			PlayerWinLost  playerWinLost  = PlayerWinLost.build(desk.getRoundId(), desk.getConfig().getGameType(), desk.getDeskId(), pid);
			for (BetType betType : betList) {
				long betAmount = desk.getAllBetAmountByPlayBet(pid, betType.getValue());
				if(betAmount != 0) {
					BetTypeWinlost betTypeWinlost = new BetTypeWinlost();
					betTypeWinlost.setBetType(betType.getValue());
					betTypeWinlost.setBetAmount(betAmount);
					
					if(desk.getResluts().contains(betType)) {
						long winAmount = (long) (betType.getRate() * betAmount / 100);
						betTypeWinlost.setWinlost(winAmount);
					}
					playerWinLost.getBetTypeWinlostMap().put(betType.getValue(), betTypeWinlost);
				}
				desk.getWinLostInfoMap().put(pid, playerWinLost);
			}
		}
		
		//异步存储数据
		DbExecutorHandler.getInstance().execute(desk.getDeskId(), this::saveWinLostInfoToDb, desk, "saveWinLostInfoToDb");
	}

	/**
	 * 保存到数据库
	 * @param info
	 */
	public void saveWinLostInfoToDb(PveGameDesk desk) {
		for (PlayerWinLost info : desk.getWinLostInfoMap().values()) {
			if(info.isToDb()) {
				continue;
			}
			saveUserRecord(desk, info);
		}

		//推送结果给前端
		GameNoticeCmdHandler.getInstance(gameType).noticeWinlost(desk.getDeskId(), desk.getWinLostInfoMap().values());
	}

	/**
	 * 玩家数据入库
	 * @param desk
	 * @param playerInfo
	 */
	private void saveUserRecord(PveGameDesk desk, PlayerWinLost playerInfo) {
		playerInfo.setToDb(true);
		long pid = playerInfo.getUserId();
		PlayerWinLost info = desk.getWinLostInfoMap().get(pid);
		Player player = PlayerMgr.getInstance(gameType).getPlayer(pid);
		User user = null;
		if(player != null) {
			user = player.getUser();
		}
		else {
			user = UserService.getInstance().findUser(pid);
		}
		
		//结算入库 TODO
		if(!player.getUser().isRobot()) {
			long balance = UserAccountsService.getInstance().upDataUserAccount(pid, info.getWinAmount());
			user.setBalance(balance);
		}
		else {
			user.setBalance(user.getBalance() + info.getWinAmount());
		}
		
		info.setUser(user);
		info.setBalance(user.getBalance());
		
		//bill and settlement
	}

}
