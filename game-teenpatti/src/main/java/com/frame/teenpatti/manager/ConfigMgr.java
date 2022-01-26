package com.frame.teenpatti.manager;

import java.util.concurrent.ConcurrentHashMap;

import com.frame.entity.Room;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConfigMgr extends ConcurrentHashMap<Integer, Room>{
	private static final long serialVersionUID = 1L;
	@Getter
	private static ConfigMgr instance = new ConfigMgr();
	
	@Deprecated
	public Room remove(Integer key) {
		log.error("禁止删除操作");
    	return null;
    }
	
	@Override
	@Deprecated
	public void clear() {
		log.error("禁止clear操作");
	}
}
