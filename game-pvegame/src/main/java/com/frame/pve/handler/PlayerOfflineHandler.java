package com.frame.pve.handler;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.frame.enums.GameType;
import com.frame.model.Player;
import com.frame.pve.handler.cmd.GameNoticeCmdHandler;
import com.frame.pve.manager.DeskMgr;
import com.frame.pve.manager.PlayerMgr;
import com.frame.pve.model.PveGameDesk;

import lombok.extern.slf4j.Slf4j;


/**
 * 玩家掉线操作
 * @author Sanjie
 * @date 2020-11-5 
 * <p>Title: PlayerOfflineHandler.java</p>  
 * <p>Description: </p>  
 */
@Slf4j
public class PlayerOfflineHandler {
	private static Map<Integer, PlayerOfflineHandler> instanceMap = new ConcurrentHashMap<Integer, PlayerOfflineHandler>();
	public static PlayerOfflineHandler getInstance(GameType gameType) {
		PlayerOfflineHandler instance = instanceMap.get(gameType.getType());
		if(instance == null) {
			instance = new PlayerOfflineHandler();
			instance.gameType = gameType;
			instanceMap.put(gameType.getType(), instance);
		}
		return instance;
	}
	private GameType gameType;

	/**
	 * @param pid
	 */
	public void offlineHandler(long pid) {
		offlineHandler(pid, false);
	}

	/**
	 * 离线操作
	 * @param pid
	 * @param mustLeave 强制离开
	 */
	public void offlineHandler(long pid, boolean mustLeave) {
		Player player = PlayerMgr.getInstance(gameType).getPlayer(pid);
		if(player == null) {
			log.warn("player == null pid:{}", pid);
			return;
		}
		PveGameDesk desk = DeskMgr.getInstance(gameType).getDesk(player.getDeskId());
		//直接离开
		if(desk.getPlayerBetInfo().get(player.getPid()) == null) {
			log.info("pid {} 直接离开", pid);
			playerLeaveDesk(desk, pid);
		}
		//在游戏中无法移除
		else {
			desk.getOfflinePlayerMap().put(pid, pid);
		}
	}

	/**
	 * 清理离线玩家
	 * @param desk
	 */
	public void cleanOfflinePlayer(PveGameDesk desk) {
		for (Entry<Long, Long> entry : desk.getOfflinePlayerMap().entrySet()) {
			playerLeaveDesk(desk, entry.getKey());
		}

		desk.getOfflinePlayerMap().clear();
	}

	/**
	 * 玩家离开
	 */
	public void playerLeaveDesk(PveGameDesk desk, long pid){
		GameNoticeCmdHandler.getInstance(gameType).noticeUserLeave(desk.getDeskId(), pid);

		desk.removeSeatByPid(pid);
		desk.getLookOnPlayerInfo().remove(pid);
		desk.getOfflinePlayerMap().remove(pid);
		PlayerMgr.getInstance(gameType).removePlayer(pid);
		//移除redis的所在游戏信息
		RedisPveServerHandler.getInstance(gameType).removeUserGameServerId(pid);

		//刷新在线人数
		RedisPveServerHandler.getInstance(gameType).updateOnlineUsers(PlayerMgr.getInstance(gameType).getAll().size());
	}

	/**清理金币不足玩家
	 * @param desk
	 */
	public void cleanLowBalancePlayer(PveGameDesk desk) {
		for (Entry<Integer, Long> entry : desk.getDeskSeatPlayerInfo().entrySet()) {
			Long pid = entry.getValue();
			Player player = PlayerMgr.getInstance(gameType).getPlayer(pid);
			if(player != null && player.getUser().getBalance() < desk.getConfig().getMinJoin()) {
				log.info("pid:{}金币不足，主动站起",  pid);
			}
		}
	}
}
