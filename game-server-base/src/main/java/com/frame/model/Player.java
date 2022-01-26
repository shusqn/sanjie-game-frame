package com.frame.model;

import com.frame.entity.User;
import com.frame.mobel.ProtoBuf;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Player{
	public Player(User user) {
		this.user = user;
	}
	private User user;
	/**
     * 桌台id
     */
    private int deskId;
    /**
     * login pb
     */
    private ProtoBuf pb;
    
	public long getPid() {
		return user.getUid();
	}
	
}
