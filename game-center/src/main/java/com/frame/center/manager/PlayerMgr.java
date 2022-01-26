package com.frame.center.manager;

import com.frame.manager.BaseMgr;
import com.frame.mobel.ProtoBuf;

import lombok.Getter;

/**
 * PlayerMgr.java
 * @author Sanjie
 * @date 2021-10-15 15:57
 * @version 1.0.0
 */
public class PlayerMgr extends BaseMgr<Long, ProtoBuf>{
	private static final long serialVersionUID = 1L;
	@Getter
	private static PlayerMgr instance = new PlayerMgr();
}
