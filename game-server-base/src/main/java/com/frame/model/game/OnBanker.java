package com.frame.model.game;

import lombok.Data;

/**
 * TODO
 * @author Sanjie
 * @date 2020-02-14 15:55
 * @version 1.0
 */
@Data
public class OnBanker {
	public int isRobot = 0;
	/**
	 * 庄家用户ID
	 */
	private long pid;
	/**
	 * 昵称
	 */
	private String nickName;
	/**
	 * 本金
	 */
	private long principalAmount;
	/**
	 * 余额
	 */
	private long freeBalance;
	/**
	 * 金额 暂时未使用
	 */
	private long amount;
	/**
	 * 占成
	 */
	private float rate;
	/**
	 * 头像类型
	 */
	private int faceType;
	/**头像路径**/
	public String headPic;
}
