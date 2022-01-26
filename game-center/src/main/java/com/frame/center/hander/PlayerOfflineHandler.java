package com.frame.center.hander;

import com.frame.center.manager.PlayerMgr;

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
		PlayerMgr.getInstance().remove(pid);
		//刷新在线人数
		RedisCenterServerHandler.getInstance().updateOnlineUsers(PlayerMgr.getInstance().values().size());
	}
	
}
