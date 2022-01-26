package com.frame.service;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.frame.dao.RoomDao;
import com.frame.entity.Room;

import lombok.Getter;


/**
 * TODO
 * @author Sanjie
 * @date 2019-09-17 17:36
 * @version 1.0
 */
@Service
public class ConfigService {
	@Getter
	private static ConfigService instance;
	
	@PostConstruct
	private void init() {
		instance = this;
	}
	
	@Autowired
	private RoomDao roomDao;
	/**
	 * @param roomId
	 * @return
	 */
	public Room getConfig(int roomId) {
		return roomDao.getOne(roomId);
	}
	
	/**
	 * @return
	 */
	public List<Room> findAll() {
		return roomDao.findAll();
	}

	/**
	 * @param gameType
	 * @return
	 */
	public List<Room> findRoomsByGameType(int gameType) {
		return roomDao.findRoomsByGameType(gameType);
	}

}
