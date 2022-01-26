package com.frame.datasource;
import javax.sql.DataSource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class DataSourceConfig {
	@Bean(name = "dataSource")
	@Primary
	@ConfigurationProperties(prefix="spring.datasource")
	public DataSource getDataSource() {
		log.info("DataSourceConfig dataSource");
		return DataSourceBuilder.create().build();
	}
	
	@Bean(name = "dataSource2")
	@ConfigurationProperties(prefix="spring.datasource2")
	public DataSource getDataSource2() {
		log.info("DataSourceConfig dataSource2");
		return DataSourceBuilder.create().build();
	}
}