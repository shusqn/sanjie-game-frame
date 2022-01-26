package com.frame;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;

import com.frame.enums.ServerType;
import com.frame.model.Robot;
import com.frame.service.ApiService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@EntityScan("com.frame")  //用于扫描JPA实体类 @Entity
@ComponentScan(basePackages = {"com.frame"}) //用于扫描@Controller @Service
@SpringBootApplication(exclude= {DataSourceAutoConfiguration.class, MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
public class RobotTestApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(RobotTestApplication.class).web(WebApplicationType.NONE).run(args);
        robotTest();
    }

    private static void robotTest() {
    	log.info("robotTest start");
    	List<Robot> robotList = new ArrayList<>();
		String url = "http://sanjie.games:8080/login";
    	//String url = "http://localhost:8080/login";
 		for (int i = 0; i < 1; i++) {
			String udid = "testUser:"+ i;
			Robot robot = ApiService.getInstance().getRobot(udid, url);
			robotList.add(robot);
			log.info(robot.toString());
		}
 		for (Robot robot : robotList) {
 			try {
				Thread.sleep(3);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
 			
 			robot.initNettyWebSocketClient(ServerType.DT_BET_GAME);
 			robot.connectGateWayServer();
		}
    }
    
}

