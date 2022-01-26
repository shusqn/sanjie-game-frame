package com.frame.teenpatti.manager;

import com.frame.manager.BasePlayerMgr;

import lombok.Getter;

public class PlayerMgr extends BasePlayerMgr{
	@Getter
	private static PlayerMgr instance = new PlayerMgr();
}
