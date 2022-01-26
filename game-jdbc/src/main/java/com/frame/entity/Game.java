package com.frame.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@ToString
@Table(name="game")
public class Game{
	/**
	 * 游戏类型
	 */
	@Id
	private int gameType;
	
	/**
	 * 服务器类型
	 */
	private int serverType;

	/**
	 * 是否开放
	 */
	private int open;
}
