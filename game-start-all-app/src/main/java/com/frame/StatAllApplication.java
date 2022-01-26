package com.frame;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.frame.center.CenterMrg;
import com.frame.center.config.CenterConfig;
import com.frame.config.AppConfig;
import com.frame.dt.DtMrg;
import com.frame.dt.config.DtConfig;
import com.frame.enums.GameType;
import com.frame.executor.ComExecutor;
import com.frame.gateway.GatewayMrg;
import com.frame.gateway.config.GatewayConfig;
import com.frame.log.Log;
import com.frame.login.LoginMrg;
import com.frame.login.config.LoginConfig;
import com.frame.pve.PveGameMrg;
import com.frame.teenpatti.TeenpattiMrg;
import com.frame.teenpatti.config.TeenpattiConfig;
import com.frame.utils.DestroyUtils;
import com.frame.utils.FileUtil;

import lombok.extern.slf4j.Slf4j;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

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
@EnableSwagger2   //http://192.168.10.97:10086/swagger-ui.html#
@Slf4j
public class StatAllApplication{
	public static void main(String[] args) {
		Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
			e.printStackTrace();
			log.error(t.getName() + " Exception", e);
		});

		ApplicationContext applicationContext = new SpringApplicationBuilder(StatAllApplication.class).web(WebApplicationType.SERVLET).run(args);
		startAll(applicationContext);
	}

	/**
	 * @param applicationContext
	 */
	private static void startAll(ApplicationContext applicationContext) {
		String mqNameServer = AppConfig.getInstance().getNameServer();
		int serversGroupId = AppConfig.getInstance().getServersGroupId();
		if(AppConfig.getInstance().isStartLogin()) {
			applicationContext.getBean(LoginConfig.class);
			//初始化login
			LoginMrg.getInsance().start(mqNameServer, serversGroupId);
		}
		if(AppConfig.getInstance().isStartGateway()) {
			applicationContext.getBean(GatewayConfig.class);
			//初始化gateway
			GatewayMrg.getInstance().start(mqNameServer, serversGroupId);
		}
		if(AppConfig.getInstance().isStartCenter()) {
			applicationContext.getBean(CenterConfig.class);
			//初始化center
			CenterMrg.getInstance().start(mqNameServer, serversGroupId);
		}
		if(AppConfig.getInstance().isStartTeenpatti()) {
			applicationContext.getBean(TeenpattiConfig.class);
			//初始化teenpatti
			TeenpattiMrg.getInstance().start(mqNameServer, serversGroupId);
		}
		if(AppConfig.getInstance().isStartDt()) {
			applicationContext.getBean(DtConfig.class);
			//初始化dt
			DtMrg.getInsance().start(mqNameServer);
			
			PveGameMrg dtGameMrg = new PveGameMrg(GameType.DT);
			dtGameMrg.start(mqNameServer, serversGroupId);
		}

		log.info("StatAllApplication start success");
		log.info(Log.test, FileUtil.getfozhu());
	}
}