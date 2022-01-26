package com.frame.entity;

import java.sql.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@ToString
@Table(name="room")
public class Room{
	@Id
	private int roomId;
	/**
	 * 游戏类型
	 */
	private int gameType;
	/**
	 * 房间类型
	 */
	private int roomType;
	/**
	 * 底注
	 */
	private long ante;
	/**
	 * 最小进入金额
	 */
	private long minJoin;
	/**
	 * 最小下注
	 */
	private long minBet;
	/**
	 * 最大下注
	 */
	private long maxBet;
	/**
	 * 抽水比例
	 */
	private int taxRate;
	/**
	 * 座位数
	 */
	private int siteCount;
	/**
	 * 最大容纳桌台数
	 */
	private int maxDeskCount;
	/**
	 * 最小开始的玩家数
	 */
	private int minPlayersCount;
	/**
	 * 最大限制备用
	 */
	private int maxLimit = 0;
	/**
	 * 其他配置json
	 */
	private String conf;
	/**
	 * 备注
	 */
	private String reMark;
    /**
     * 创建时间
     */
    @CreatedDate
    private Date createTime;
    
    /**
     * 修改时间
     */
    @LastModifiedDate
    private Date updateTime;
}
