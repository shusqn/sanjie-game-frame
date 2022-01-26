package com.frame.config;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@Configuration
@Getter
public class AppConfig {
	@Value("${spring.application.port}")
	private int port;
	@Value("${spring.application.serverId}")
	private int serverId;
	@Value("${spring.application.getBetMuiltResultServerId}")
	private int getBetMuiltResultServerId;
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
