package com.frame.dt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.frame.dt.config.DtConfig;
import com.frame.utils.DestroyUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * StatAllApplication.java
 * @author Sanjie
 * @date 2021-09-23 15:46
 * @version 1.0.0
 */
@SpringBootApplication
@EntityScan("com.frame")  //用于扫描JPA实体类 @Entity
@EnableJpaRepositories(basePackages = "com.frame") //用于扫描Dao @Repository
@ComponentScan(basePackages = {"com.frame"}) //用于扫描@Controller @Service
@Slf4j
public class DtBuilderApplication{

	public static void main(String[] args) {
		Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
			e.printStackTrace();
			log.error(t.getName() + " Exception", e);
		});
		
		ApplicationContext applicationContext = new SpringApplicationBuilder(DtBuilderApplication.class).web(WebApplicationType.NONE).run(args);
		DtMrg.getInsance().start(applicationContext.getBean(DtBuilderApplication.class).nameServer);
		log.info("DtBuilderApplication start success");
	}
	
	@Value("${rocketmq.nameServer}")
	private String nameServer;

}