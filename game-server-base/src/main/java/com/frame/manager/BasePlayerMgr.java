package com.frame.manager;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.frame.model.Player;

/**
 * 玩家管理工具
 * @author Sanjie
 * 
 * @date 2020-11-4 
 * <p>Title: PlayerMgr.java</p>  
 * <p>Description: </p>  
 */
public abstract class BasePlayerMgr {
	private ConcurrentMap<Long, Player> playerCache = new ConcurrentHashMap<Long, Player>();
	
	public Collection<Player> getAll() {
		return playerCache.values();
	}
	
	/**
	 * @param roomId
	 * @return
	 */
	public Player getPlayer(long pid) {
		return playerCache.get(pid);
	}
	
	/**
	 * @param room
	 * @return
	 */
	public Player addPlayer(Player player) {
		return playerCache.put(player.getPid(), player);
	}
	
	/**
	 * @param room
	 * @return
	 */
	public Player removePlayer(Player player) {
		return playerCache.remove(player.getPid());
	}
	
	/**
	 * @param roomId
	 * @return
	 */
	public Player removePlayer(long pid) {
		return playerCache.remove(pid);
	}
}
