package com.frame.config;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@Configuration
@Getter
public class AppConfig {
	@Value("${rocketmq.nameServer}")
	private String nameServer;
	
	@Value("${servers-groupId}")
	private int serversGroupId;
	
	@Value("${start-all.login}")
	private boolean startLogin;
	@Value("${start-all.gateway}")
	private boolean startGateway;
	@Value("${start-all.center}")
	private boolean startCenter;
	@Value("${start-all.teenpatti}")
	private boolean startTeenpatti;
	@Value("${start-all.dt}")
	private boolean startDt;
	
	@Getter
	private static AppConfig instance; 
	
	@PostConstruct
	public void init() {
		instance = this;
	}
}
