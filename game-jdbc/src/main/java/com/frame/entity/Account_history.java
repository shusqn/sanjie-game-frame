package com.frame.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(name = "account_history")
public class Account_history{
	@Id
	private long id;
	/**最近账单id**/
	private long billId;
	private long balance;
	
}
