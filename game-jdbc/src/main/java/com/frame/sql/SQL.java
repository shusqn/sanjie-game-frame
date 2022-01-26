package com.frame.sql;

public class SQL {
	/**
	 * 查询玩家余额 SELECT uid, balance FROM `game-teenpatti`.account;
	 */
	public final static String S_ACCOUNT_BALANCE= "SELECT balance FROM account WHERE uid = ?";
	/**
	 * 更新玩家余额
	 */
	public final static String U_ACCOUNT_BALANCE = "update account set balance=balance+? where uid = ?";
	
	/**
	 * 插入
	 */
	public final static String I_ACCOUNT = "INSERT INTO account (uid, balance) VALUES(?, 0)";
	
	;

	
}
