package com.frame.dao;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * JdbcDao.java
 * @author Sanjie
 * @date 2021-09-10 14:11
 * @version 1.0.0
 */
@Service
@Slf4j
public class JdbcDao {
	@Getter
	@Resource(name = "jdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	
	@Getter
	private static JdbcDao instance ;

	@PostConstruct
	public void init() {
		log.info("JdbcDao init success !");
		instance = this;
	}
	
	/*@Autowired
	private Environment env;
	@PostConstruct
	public void init() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setUrl(env.getProperty("spring.datasource.url"));
		dataSource.setDriverClassName(env.getProperty("spring.datasource.driver-class-name"));
		dataSource.setUsername(env.getProperty("spring.datasource.username"));
		dataSource.setPassword(env.getProperty("spring.datasource.password"));
		dao = new JdbcTemplate(dataSource);
	}*/

	/*
	List<User> list = Dao.jdbc.query("SELECT * FROM gamedb.UserInfo where userId = ?",new Object[]{100000}, new BeanPropertyRowMapper(User.class));
	(User) Dao.jdbc.queryForObject("SELECT * FROM gamedb.UserInfo where userId = ?",new Object[]{100000},new BeanPropertyRowMapper(User.class));
	List<Map<String, Object>> users = jdbcTemplate.queryForList(sql, 1L);
	String sql = "insert into user (user_name,password) VALUES (?, ?)";
	jdbcTemplate.update(sql, "赵孤鸿", "123456"); // 插入数据

	conn = jdbcTemplate.getDataSource().getConnection();
	if(conn != null)
	{
		conn.setAutoCommit(false);
		conn.commit();
		conn.rollback();
		conn.setAutoCommit(true);
	}
	//事务
	@Transactional
    public int insertOnePerson(String name, int age) {
        int result = 0;
        int count = jdbcTemplate.update(DELETE_ONE_PERSON, new Object[]{name});
        if(count >= 0)                                                                    
        {
            result = jdbcTemplate.update(INSERT_ONE_PERSON, new Object[]{name,"l123a"});
        }
        return result    ;
    }
	 */

}
