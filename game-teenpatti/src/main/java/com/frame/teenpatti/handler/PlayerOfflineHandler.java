package com.frame.teenpatti.handler;

import java.util.Map.Entry;

import com.frame.model.Player;
import com.frame.protobuf.TeenpattiMsg.DropCardType;
import com.frame.teenpatti.handler.cmd.GameNoticeCmdHandler;
import com.frame.teenpatti.manager.DeskMgr;
import com.frame.teenpatti.manager.PlayerMgr;
import com.frame.teenpatti.model.TeenpattiDesk;

import lombok.Getter;
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
	@Getter
	private static PlayerOfflineHandler instance = new PlayerOfflineHandler();

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
		Player player = PlayerMgr.getInstance().getPlayer(pid);
		if(player == null) {
			log.warn("player == null pid:{}", pid);
			return;
		}
		TeenpattiDesk desk = DeskMgr.getInstance().getDesk(player.getDeskId());
		//直接离开
		if(desk.getGamingPlayerInfo().get(player.getPid()) == null || desk.isCanLeaveDesk()) {
			log.info("pid {} 直接离开", pid);
			playerLeaveDesk(desk, pid);
			desk.getDeskHandler().playerLeaveDesk();
		}
		//在游戏中无法移除
		else {
			if(mustLeave) {
				log.info("pid {} 强制离开", pid);
				desk.getDeskHandler().dropCardHandler(pid, DropCardType.MUST_LEAVE_VALUE);
			}
			desk.getOfflinePlayerMap().put(pid, pid);
			GameNoticeCmdHandler.getInstance().noticeUserOffline(desk.getDeskId(), pid);
		}
	}

	/**
	 * 清理离线玩家
	 * @param desk
	 */
	public void cleanOfflinePlayer(TeenpattiDesk desk) {
		for (Entry<Long, Long> entry : desk.getOfflinePlayerMap().entrySet()) {
			playerLeaveDesk(desk, entry.getKey());
		}
		desk.getOfflinePlayerMap().clear();
	}

	/**
	 * 玩家离开
	 */
	public void playerLeaveDesk(TeenpattiDesk desk, long pid){
		GameNoticeCmdHandler.getInstance().noticeUserLeave(desk.getDeskId(), pid);

		desk.removeSeatByPid(pid);
		desk.getLookOnPlayerInfo().remove(pid);
		desk.getOfflinePlayerMap().remove(pid);
		desk.getGamingPlayerInfo().remove(pid);
		desk.getReadyPlayerInfo().remove(pid);
		PlayerMgr.getInstance().removePlayer(pid);
		//移除redis的所在游戏信息
		RedisTeenpattiServerHandler.getInstance().removeUserGameServerId(pid);

		//刷新在线人数
		RedisTeenpattiServerHandler.getInstance().updateOnlineUsers(PlayerMgr.getInstance().getAll().size());
	}

	/**清理金币不足玩家
	 * @param desk
	 */
	public void cleanLowBalancePlayer(TeenpattiDesk desk) {
		for (Entry<Integer, Long> entry : desk.getDeskSeatPlayerInfo().entrySet()) {
			Long pid = entry.getValue();
			Player player = PlayerMgr.getInstance().getPlayer(pid);
			if(player.getUser().getBalance() < desk.getConfig().getMinJoin()) {
				log.info("pid:{}金币不足，主动站起",  pid);
				desk.getDeskHandler().standUp(pid);
			}
		}
	}
}
