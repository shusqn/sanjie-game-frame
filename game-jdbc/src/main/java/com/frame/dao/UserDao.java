package com.frame.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.frame.entity.User;

@Repository
public interface UserDao extends JpaRepository<User,Long> {

	Optional<User> findUserByImei(String imei);
	
	default User getUserByIMEI(String imei) {
		return findUserByImei(imei).orElse(null);
	}
}
