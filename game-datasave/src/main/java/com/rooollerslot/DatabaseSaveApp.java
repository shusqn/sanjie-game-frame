package com.rooollerslot;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.frame.constant.MqConsumerGroup;
import com.frame.constant.MqTopic;
import com.rooollerslot.config.AppConfig;
import com.rooollerslot.rocketmq.RocketMqReceive;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {"com.rooollerslot"}) //用于扫描@Controller @Service
@Slf4j
public class DatabaseSaveApp implements CommandLineRunner {
	public static void main(String[] args) {
		Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
			e.printStackTrace();
			log.error(t.getName() + " Exception", e);
		}
				);
		new SpringApplicationBuilder(DatabaseSaveApp.class).web(WebApplicationType.NONE).run(args);
	}

	@Override
	public void run(String... args) throws InterruptedException {
		RocketMqReceive.getInstance().start(
				AppConfig.getInstance().getNameServer(), 
				MqTopic.PUSH_TOPIC_MONGO_DATA_RECORD,
				MqConsumerGroup.CONSUMER_MONGO_DATA_RECORD,
				null);
		log.info("DatabaseSaveApp start success");
	}

}
