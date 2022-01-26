package com.frame.service;

import javax.annotation.PostConstruct;

import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;

import com.frame.dao.JdbcDao;
import com.frame.sql.SQL;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * UserAccountsService.java
 * @author Sanjie
 * @date 2021-09-09 18:14
 * @version 1.0.0
 */
@Service
@Slf4j
public class UserAccountsService {
	@Getter
	private static UserAccountsService instance;

	@PostConstruct
	private void init() { 
		instance = this;
	}

	/**
	 * 更新玩家余额
	 * @param uid
	 * @param changeAmount
	 * @return
	 */
	public long upDataUserAccount(long uid, long changeAmount) {
		long balance = getBalance(uid);
		if(balance + changeAmount < 0) {
			throw new RuntimeException("余额不足");
		}
		JdbcDao.getInstance().getJdbcTemplate().update(SQL.U_ACCOUNT_BALANCE, changeAmount, uid);
		return balance + changeAmount;
	}

	/**
	 * 更新玩家余额
	 * @param uid
	 * @param changeAmount
	 * @return
	 */
	public boolean insertUserAccount(long uid) {
		JdbcDao.getInstance().getJdbcTemplate().update(SQL.I_ACCOUNT, uid);
		return true;
	}


	/**
	 * 获取玩家游戏币余额
	 * @param uid
	 * @return
	 */
	public long getBalance(long uid) {
		SqlRowSet rowset = JdbcDao.getInstance().getJdbcTemplate().queryForRowSet(SQL.S_ACCOUNT_BALANCE, uid);
		if(rowset.next()) {
			return rowset.getLong("balance");
		}
		return 0;
	}
}
