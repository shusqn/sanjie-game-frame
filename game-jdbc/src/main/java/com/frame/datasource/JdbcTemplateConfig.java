package com.frame.datasource;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
 
@Configuration
public class JdbcTemplateConfig {
    @Bean
    JdbcTemplate jdbcTemplate(@Qualifier("dataSource") DataSource dataSource){
        return new JdbcTemplate(dataSource);
    }
 
    @Bean
    JdbcTemplate jdbcTemplate2(@Qualifier("dataSource2") DataSource dataSource){
        return new JdbcTemplate(dataSource);
    }
}