package com.rooollerslot.config;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@Configuration
@Getter
public class AppConfig {
	private String apiServer;
	@Value("${spring.application.serverId}")
	private int serverId;
	@Value("${rocketmq.nameServer}")
	private String nameServer;
	@Value("${rocketmq.producer.group}")
	private String producerGroupName;
	
	@Getter
	private static AppConfig instance; 
	
	@PostConstruct
	public void init() {
		instance = this;
	}
}
