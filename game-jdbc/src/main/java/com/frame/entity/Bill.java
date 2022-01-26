package com.frame.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Getter;

@Getter
@Entity(name = "bill")
public class Bill{
	@Id
	private long billId;
	private long orderId;
	private int orderType;
	private long uid;
	private long changeBefore;
	private int changeCount;
	private long changeAfter;
	private int billType;
}
