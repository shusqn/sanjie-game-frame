package com.frame.center.service;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.frame.dao.GamesDao;
import com.frame.entity.Game;

import lombok.Getter;


/**
 * TODO
 * @author Sanjie
 * @date 2019-09-17 17:36
 * @version 1.0
 */
@Service
public class GameService {
	@Getter
	private static GameService instance;
	
	private List<Game> gameList = null;
	
	@PostConstruct
	private void init() {
		instance = this;
	}
	
	@Autowired
	private GamesDao gamesDao;
	
	/**
	 * @return
	 */
	public synchronized List<Game> findAll() {
		if(gameList == null) {
			gameList = gamesDao.findAll();
		}
		return gameList;
	}
}
