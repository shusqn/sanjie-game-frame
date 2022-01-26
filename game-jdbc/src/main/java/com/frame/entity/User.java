package com.frame.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Sanjie
 * @date 2019-09-17 14:44
 * @version 1.0
 */
@Getter
@Setter
@Entity(name = "user")
public class User {
	@Id
	private long uid;
	private String name;
	private String mobile;
	private String nickName;
	private int sex;
	private String psw;
	private String bankPsw;
	private int level;
	private String qq;
	private String wechat;
	private String google;
	private String y2b;
	private String facebook;
	private String ip;
	private String city;
	private int headPicType;
	private String headPic;
	private String sign;
	private String imei;
	private String reMark;
	private int state;

	@Transient
	private long balance;
	
	public boolean isRobot() {
		return false;
	}
}
