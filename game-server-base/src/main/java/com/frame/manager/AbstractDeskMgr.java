package com.frame.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import com.frame.executor.TimerExecutorHandler;
import com.frame.model.game.BaseDesk;

import lombok.extern.slf4j.Slf4j;

/**
 * 游戏桌台管理工具
 * @author Sanjie
 * @date 2020-11-4 
 * <p>Title: PlayerMgr.java</p>  
 * <p>Description: </p>  
 */
@Slf4j
public abstract class AbstractDeskMgr <T extends BaseDesk>{
	
	public abstract void initAllDesk();
	/**
	 * key deskId
	 */
	private final ConcurrentMap<Integer, T> deskCache = new ConcurrentHashMap<Integer, T>();
	/**
	 * key roomId
	 */
	private final ConcurrentMap<Integer, List<T>> roomDeskCache = new ConcurrentHashMap<Integer, List<T>>();

	/**
	 * @param roomId
	 * @return
	 */
	public T getDesk(int deskId) {
		return deskCache.get(deskId);
	}
	
	/**
	 * @param roomId
	 * @return
	 */
	public List<T> getDeskList(int roomId) {
		return roomDeskCache.get(roomId);
	}
	
	/**
	 * @return
	 */
	public Collection<Integer> getAllRooms() {
		return roomDeskCache.keySet();
	}
	
	/**
	 * @return
	 */
	public int getDeskCount() {
		return deskCache.size();
	}

	/**
	 * @param room
	 * @return
	 */
	public T addDesk(T desk) {
		List<T> list = roomDeskCache.computeIfAbsent(desk.getConfig().getRoomId(), (v->new ArrayList<T>()));
		list.add(desk);
		return deskCache.putIfAbsent(desk.getDeskId(), desk);
	}
	
	/**
	 * 
	 */
	protected void timeDoSortDesk() {
		try {
			Collection<Integer> rooms = getAllRooms();
			for (Integer roomId : rooms) {
				List<T> list = getDeskList(roomId);
				sortDesk(list);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	/**
	 * 根据点数倒序排序 从大到小
	 * @param list
	 * @return
	 */
	protected List<T> sortDesk(List<T> list){
		//实现排序
		Collections.sort(list, new Comparator<T>() {
			@Override
			public int compare(T c1, T c2) {
				if(c1.getSortWeight() > c2.getSortWeight()) {
					return -1;
				}else if (c1.getSortWeight() == c2.getSortWeight()) {
					return 0;
				}else {
					return 1;
				}
			}
		});
		return list;
	}

}
