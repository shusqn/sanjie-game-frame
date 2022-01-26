package com.frame.handler;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.frame.router.IDestroyHandler;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * RedisServerHandler.java
 * @author Sanjie
 * @date 2021-09-27 16:44
 * @version 1.0.0
 */
@Slf4j
@Component
public class RedisRobotServerHandler extends BaseRedisServerHandler implements IDestroyHandler{
	@Getter
	private static RedisRobotServerHandler instance;

	@PostConstruct
	private void init() {
		instance = this;
	}
}
