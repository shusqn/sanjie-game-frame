package com.frame.dao;


import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author fanyou
 *
 */
@Service
@Slf4j
public final class RedisDao{
	@Autowired
	@Getter
	protected StringRedisTemplate template; 
	@Getter
	private static RedisDao instance;
	
	@PostConstruct
	private void init() {
		log.info("RedisDao init success !");
		instance = this;
		template.setEnableTransactionSupport(true);
	}
	
	/**
	 * 单例线程池
	 */
	@Getter
	private static ScheduledThreadPoolExecutor redisExecutor  = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
		@Override
		public Thread newThread(Runnable r) {
			Thread thread = new Thread(r, "RedisSingleExecutor");
			thread.setDaemon(true);
			return thread;
		}
	});
}
