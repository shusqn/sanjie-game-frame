package com.frame.service;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * UserAccountsService.java
 * @author Sanjie
 * @date 2021-09-09 18:14
 * @version 1.0.0
 */
@Service("RobotService")
@Slf4j
public class RobotService {
	@Getter
	private static RobotService instance;
	
	@PostConstruct
	private void init() { 
		instance = this;
	}
}
