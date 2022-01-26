package com.frame.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.frame.entity.Room;

@Repository
public interface RoomDao extends JpaRepository<Room,Integer> {
	List<Room> findRoomsByGameType(Integer gameType);
}
