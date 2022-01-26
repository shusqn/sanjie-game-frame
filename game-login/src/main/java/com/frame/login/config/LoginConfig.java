package com.frame.login.config;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Component
@Getter
@Lazy
public class LoginConfig {
	@Value("${login.appid}")
	private String appid;
	@Value("${login.secret}")
	private String secret;
	@Value("${login.initBalance}")
	private long initBalance;
	
	@Getter
	private static LoginConfig instance;
	@PostConstruct
	private void init(){
		instance = this;
	}
}
