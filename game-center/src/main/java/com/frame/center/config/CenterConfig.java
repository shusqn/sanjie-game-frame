package com.frame.center.config;

import javax.annotation.PostConstruct;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * GateWayConfig.java
 * @author Sanjie
 * @date 2021-09-23 16:22
 * @version 1.0.0
 */
@Component
@Slf4j
@Lazy
public class CenterConfig {
	@Getter
	private static CenterConfig instance;
	
	@PostConstruct
	private void init() {
		instance = this;
		log.info("CenterConfig init success !");
	}
}
