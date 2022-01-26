package com.frame.service;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.frame.dao.UserDao;
import com.frame.entity.User;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * UserAccountsService.java
 * @author Sanjie
 * @date 2021-09-09 18:14
 * @version 1.0.0
 */
@Service("BaseUserService")
@Slf4j
public class UserService {
	@Getter
	private static UserService instance;
	
	@Autowired
	private UserDao userDao;

	@PostConstruct
	private void init() { 
		instance = this;
	}
	
	/**
	 * @param pid
	 * @return
	 */
	public User findUser(long pid) {
		User user = userDao.findById(pid).orElse(null);
		if(user != null) {
			user.setBalance(UserAccountsService.getInstance().getBalance(pid));
		}
		return user;
	}

}
