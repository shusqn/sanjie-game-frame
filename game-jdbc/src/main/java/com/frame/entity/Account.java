package com.frame.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(name = "account")
public class Account{
	@Id
	private long uid;
	private long balance;
}
