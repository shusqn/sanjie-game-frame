package com.frame.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Sanjie
 * @date 2019-09-17 14:44
 * @version 1.0
 */
@Getter
@Setter
public class RobotUser extends User{
	private long robotId;
	
	@Override
	public boolean isRobot() {
		return true;
	}
}
