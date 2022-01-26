package com.frame.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.frame.entity.Game;

@Repository
public interface GamesDao extends JpaRepository<Game,Integer> {
}
