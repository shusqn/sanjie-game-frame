package com.frame.dao2;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * JdbcDao.java
 * @author Sanjie
 * @date 2021-09-10 14:11
 * @version 1.0.0
 */
@Service
@Slf4j
@Lazy
public class JdbcDao2 {
	@Getter
	@Resource(name = "jdbcTemplate2")
	private JdbcTemplate jdbcTemplate;
	
	@Getter
	private static JdbcDao2 instance ;

	@PostConstruct
	public void init() {
		log.info("JdbcDao2 init success !");
		instance = this;
	}
}
