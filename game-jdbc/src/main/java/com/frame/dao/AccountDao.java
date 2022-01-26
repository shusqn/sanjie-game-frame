package com.frame.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.frame.entity.Account;

@Repository
public interface AccountDao extends JpaRepository<Account,Long> {
	
}
