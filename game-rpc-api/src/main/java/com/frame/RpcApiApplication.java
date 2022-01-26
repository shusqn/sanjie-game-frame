package com.frame;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.frame.config.AppConfig;
import com.frame.id.IDGenerator;
import com.frame.rocketmq.rpc.RocketMqRpcService;

import lombok.extern.slf4j.Slf4j;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableScheduling
@EnableSwagger2   //http://192.168.10.97:10086/swagger-ui.html#
@ComponentScan(basePackages = {"com.rooollerslot"}) //用于扫描@Controller @Service
@Slf4j
public class RpcApiApplication implements CommandLineRunner {
	public static void main(String[] args) {
		try {
			new SpringApplicationBuilder(RpcApiApplication.class).web(WebApplicationType.SERVLET).run(args);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public void run(String... args) throws Exception {
		try {
			//初始化id生成器
			IDGenerator.register(AppConfig.getInstance().getServerId());
			RocketMqRpcService.init(AppConfig.getInstance().getNameServer(), AppConfig.getInstance().getServerId());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			System.exit(0);
		}
		log.info("RpcApiApplication start success");
	}

}