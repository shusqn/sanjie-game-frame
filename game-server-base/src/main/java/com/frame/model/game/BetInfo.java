package com.frame.model.game;

import lombok.Getter;

/**
 * @author Sanjie
 * @date 2020-12-14 
 * <p>Title: BetInfo.java</p>  
 * <p>Description: </p>  
 */
@Getter
public class BetInfo {
	private int type;
	private long amont;
	public BetInfo(int type, long amont) {
		this.type = type;
		this.amont = amont;
	}
}
