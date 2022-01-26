package com.frame.teenpatti.handler;

import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import com.alibaba.fastjson.JSON;
import com.frame.entity.User;
import com.frame.executor.DbExecutorHandler;
import com.frame.executor.DeskExecutorHandler;
import com.frame.mobel.Tupple3;
import com.frame.mobel.card.CardsUtils;
import com.frame.model.Player;
import com.frame.protobuf.TeenpattiMsg.DropCardType;
import com.frame.service.UserAccountsService;
import com.frame.service.UserService;
import com.frame.teenpatti.handler.cmd.GameNoticeCmdHandler;
import com.frame.teenpatti.handler.cmd.GameReqCmdHandler;
import com.frame.teenpatti.manager.PlayerMgr;
import com.frame.teenpatti.model.PlayerWinLost;
import com.frame.teenpatti.model.TeenpattiDesk;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Sanjie
 * @date 2020-11-4 
 * <p>Title: GameWinLostHandler.java</p>  
 * <p>Description: </p>  
 */
@Slf4j
public class GameCalcuHandler {

	@Getter
	private static GameCalcuHandler instance = new GameCalcuHandler();
	//===========================================================
	/**
	 * 玩家弃牌
	 * @param pid
	 * @param desk
	 * @param dropType {@link DropCardType}
	 */
	public void playerDropCard(long pid, TeenpattiDesk desk, int dropType) {
		long lostAmount = desk.getAllBetAmount(pid);

		Player player = PlayerMgr.getInstance().getPlayer(pid);
		User user = player.getUser();
		if(user.getBalance() - lostAmount < 0) {
			lostAmount = user.getBalance();
		}

		PlayerWinLost info = PlayerWinLost.build(desk.getRoundId(), desk.getConfig().getGameType(), desk.getDeskId(), pid, false, desk.getPlayerCardsInfo().get(pid), true, dropType);
		info.setWinAmount(0 - lostAmount);
		if(desk.getGamingPlayerInfo().size() == 2 && dropType == DropCardType.COMPARE_VALUE) {
			info.setDropCard(false);
		}
		desk.getWinLostInfoMap().put(pid, info);
		log.info("保存投降userId {}, 结算信息 {}", pid, JSON.toJSONString(desk.getWinLostInfoMap().get(pid)));
		//异步存储数据
		DbExecutorHandler.getInstance().execute(desk.getDeskId(), this::saveDropToDb, new Tupple3<TeenpattiDesk, PlayerWinLost, Integer>(desk, info, dropType), "saveDropToDb");
	}

	/**
	 * @param desk
	 */
	public void saveDropToDb(Tupple3<TeenpattiDesk, PlayerWinLost, Integer> tupple3) {
		final TeenpattiDesk desk = tupple3.first;
		final PlayerWinLost info = tupple3.second;
		final int dropType  =  tupple3.third;

		long pid = info.getUserId();

		saveUserRecord(desk, info);
		//广播玩家余额
		GameNoticeCmdHandler.getInstance().noticePlayerBalance(desk.getDeskId(), pid, info.getBalance());

		DeskExecutorHandler.getInstance().execute(desk.getDeskId(), ()->{
			desk.getGamingPlayerInfo().remove(pid);
			//广播玩家弃牌
			GameNoticeCmdHandler.getInstance().noticePlayerDropCard(desk.getDeskId(), pid, info.getWinAmount(), info.getBalance(), dropType);
			if(dropType == DropCardType.STAND_UP_VALUE) {
				desk.getDeskHandler().standUp(pid);
			}
			else if(dropType == DropCardType.MUST_LEAVE_VALUE) {
				PlayerOfflineHandler.getInstance().offlineHandler(pid);
			}
			else if(dropType == DropCardType.SHIFT_DESK_VALUE) {
				Player player = PlayerMgr.getInstance().getPlayer(pid);

				PlayerOfflineHandler.getInstance().offlineHandler(pid);
				GameReqCmdHandler.getInstance().doSitDown(player.getPb(), desk.getConfig().getRoomId(), 0, player.getUser(), true, desk.getDeskId());
			}
			if(pid == desk.getCurrentPid() || pid==desk.getWaitProtocol().getPid() || dropType == DropCardType.COMPARE_VALUE || desk.getGamingPlayerInfo().size() == 1) {
				//通知下个玩家摸牌
				desk.getDeskHandler().noticePlayerTurn(false);
			}
		});
	}

	/**
	 * 计算本局结果
	 * @param desk
	 */
	public void produceWinLostInfo(TeenpattiDesk desk) {
		Set<Long> winPids = new TreeSet<>();
		if(desk.getGamingPlayerInfo().size() != 1) {
			long maxPid = 0;
			for (Entry<Long, Long> entry : desk.getGamingPlayerInfo().entrySet()) {
				Long pid = entry.getKey();
				if(maxPid != 0) {
					int b = CardsUtils.getCompareJinHuaCardsResult(desk.getPlayerCardsInfo().get(pid), desk.getPlayerCardsInfo().get(maxPid));
					if(b == 1) {
						maxPid = pid;
						winPids.clear();
						winPids.add(pid);
					}
					else if(b == -1) {
						winPids.add(pid);
					}
				}
				else {
					maxPid = pid;
					winPids.add(pid);
				}
			}
		}
		else {
			winPids.add(desk.getWinnerPid());
		}
		for (Entry<Long, Long> entry : desk.getGamingPlayerInfo().entrySet()) {
			Long pid = entry.getKey();
			if(winPids.contains(Long.valueOf(pid))) {
				long winAmount = desk.getTotalBetWithoutWiner(winPids) / winPids.size();
				PlayerWinLost winnerInfo = PlayerWinLost.build(desk.getRoundId(), desk.getConfig().getGameType(), desk.getDeskId(), pid, true, desk.getPlayerCardsInfo().get(pid), false, 0);
				winnerInfo.setWinAmount(winAmount);

				long chouShui = winAmount * desk.getConfig().getTaxRate() / 100;
				winnerInfo.setChoushui((int) chouShui);
				winnerInfo.setWinAmount(winAmount - chouShui);
				desk.getWinLostInfoMap().put(winnerInfo.getUserId(), winnerInfo);
			}
			else {
				long lostAmount = desk.getAllBetAmount(pid);
				PlayerWinLost info = PlayerWinLost.build(desk.getRoundId(), desk.getConfig().getGameType(), desk.getDeskId(), pid, false, desk.getPlayerCardsInfo().get(pid), false, 0);
				info.setWinAmount(0 - lostAmount);
				desk.getWinLostInfoMap().put(info.getUserId(), info);
			}
		}

		//异步存储数据
		DbExecutorHandler.getInstance().execute(desk.getDeskId(), this::saveWinLostInfoToDb, desk, "saveWinLostInfoToDb");
	}

	/**
	 * 保存到数据库
	 * @param info
	 */
	public void saveWinLostInfoToDb(TeenpattiDesk desk) {
		for (PlayerWinLost info : desk.getWinLostInfoMap().values()) {
			if(info.isToDb()) {
				continue;
			}
			saveUserRecord(desk, info);
		}

		//推送结果给前端
		GameNoticeCmdHandler.getInstance().noticeWinlost(desk, desk.getWinLostInfoMap().values());
		DeskExecutorHandler.getInstance().execute(desk.getDeskId(), ()->{
			desk.getDeskHandler().showPlayersCardsAndWinLost();
		});
	}

	/**
	 * 玩家数据入库
	 * @param desk
	 * @param playerInfo
	 */
	private void saveUserRecord(TeenpattiDesk desk, PlayerWinLost playerInfo) {
		playerInfo.setToDb(true);
		long pid = playerInfo.getUserId();
		PlayerWinLost info = desk.getWinLostInfoMap().get(pid);
		Player player = PlayerMgr.getInstance().getPlayer(pid);
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
