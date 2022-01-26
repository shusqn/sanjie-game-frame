package com.frame.gateway.netty;

import com.frame.netty.AbstractSessionMgr;

import lombok.Getter;

/**
 * TODO
 * @author Sanjie
 * @date 2019-08-29 12:04
 * @version 1.0
 */
public class SessionMgr extends AbstractSessionMgr{
	@Getter
	private static SessionMgr instance = new SessionMgr();
}
