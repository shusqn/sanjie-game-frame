package com.frame.gateway.config;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
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
@Getter
@Slf4j
@Lazy
public class GatewayConfig {
	@Getter
	private static GatewayConfig instance;
	
	@PostConstruct
	private void init() {
		instance = this;
		log.info("GatewayConfig init success !");
	}
	
	@Value("${gateway.port}")
    private int port;
	@Value("${gateway.hostname}")
    private String hostname;
	
}
