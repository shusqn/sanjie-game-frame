package com.frame;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;

import com.frame.config.RobotConfig;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@EntityScan("com.frame")  //用于扫描JPA实体类 @Entity
@ComponentScan(basePackages = {"com.frame"}) //用于扫描@Controller @Service
@SpringBootApplication(exclude= {DataSourceAutoConfiguration.class, MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
public class RobotApplication  {

    public static void main(String[] args) {
        new SpringApplicationBuilder(RobotApplication.class).web(WebApplicationType.NONE).run(args);
    	RobotMrg.getInstance().start(RobotConfig.getInstance().getNameServer(), RobotConfig.getInstance().getServersGroupId());
    	
    	 log.info("RobotApplication start !!!");
    }
}

