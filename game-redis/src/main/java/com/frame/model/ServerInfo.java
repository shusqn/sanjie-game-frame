package com.frame.model;

import org.springframework.stereotype.Service;

import lombok.Getter;
import lombok.ToString;

/**
 * ServerInfo.java
 * @author Sanjie
 * @date 2021-09-27 11:33
 * @version 1.0.0
 */
@Getter
@Service
@ToString
public class ServerInfo {
	/**
	 * 服务器id
	 */
	private int sid;
	/**
	 * 服务器类型
	 */
	private int stype;
	/**
	 * 在线人数
	 */
	private int onlineUsers;
	/**
	 * 端口
	 */
	private Integer port;
	/**
	 * 域名
	 */
	private String hostname;
	/**
	 * 游戏类型
	 */
	private int gameType;
}
