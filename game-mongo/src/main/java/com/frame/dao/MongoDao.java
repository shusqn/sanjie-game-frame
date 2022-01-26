package com.frame.dao;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * TODO
 * @author Sanjie
 * @date 2021-09-07 16:57
 * @version 1.0
 */
@Slf4j
@Service
public class MongoDao {
	@Getter
	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Getter
	private static MongoDao instance ;
	
	@PostConstruct
	public void init() {
		log.info("MongoDao init success !");
		instance = this;
	}
	
	public <T> T findOne(String collectionName,Class<T> t,String idName,Object idVaule){
		Query query = new Query(Criteria.where(idName).is(idVaule));
		return mongoTemplate.findOne(query, t, collectionName);
	}
	public <T> List<T>  find(String collectionName,Class<T> t,Query query)	{
		return mongoTemplate.find(query, t, collectionName);
	}
	public <T> List<T> findPage(String collectionName,Class<T> t,Query query,int start,int size)	{
		query.skip(start);
		query.limit(size);
		return mongoTemplate.find(query, t, collectionName);
	}
	public <T> T findAndModify(String collectionName,Class<T> t,String idName,Object idVaule,Update update)	{
		Query query = new Query(Criteria.where(idName).is(idVaule));
		return mongoTemplate.findAndModify(query, update, t, collectionName);
	}
	public Object findAndRemove(String collectionName, String id, Object vaule)	{
		Query query = new Query(Criteria.where(id).is(vaule));
		return mongoTemplate.findAndRemove(query, Object.class, collectionName);
	}
	public <T> T findAndRemove(String collectionName,Class<T> t, Object id)	{
		Query query = new Query(Criteria.where("_id").is(id));
		return mongoTemplate.findAndRemove(query, t, collectionName);
	}
	public void insert(String collectionName,Object objectToSave)	{
		mongoTemplate.insert(objectToSave,collectionName);
	}
	public void dropCollection(String collectionName)	{
		mongoTemplate.dropCollection(collectionName);
	}
}
