package com.frame.service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.frame.config.AppConfig;
import com.frame.id.IDGenerator;
import com.frame.rocketmq.rpc.MqRpcData;
import com.frame.rocketmq.rpc.RocketMqRpcService;
import com.frame.utils.Ggson;
import com.google.gson.Gson;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * TODO
 * @author Sanjie
 * @date 2019-09-16 19:33
 * @version 1.0
 */
@Controller
@Slf4j
public class SlotApiServlet {

	private static Map<Long, AsyncContext> respMap = new ConcurrentHashMap<Long, AsyncContext>();
	/**
	 * @param data
	 */
	private void rpcRespHandler(MqRpcData data) {
		AsyncContext response = respMap.get(data.getMsgId());
		if(response != null) {
			try {
				response.getResponse().getOutputStream().write(data.getResult().getBytes());
				response.complete();
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
		}
	}
	
	private final Gson gson = new Gson();
	
	/**
	 * @param request
	 * @param response
	 * @param gameId
	 * @param roomLevel
	 * @param safetyModel
	 * @param betTimes
	 */
	@ApiOperation("获取多次下注结果")
	@RequestMapping(value = "/getBetMuiltResult", method = {RequestMethod. POST, RequestMethod.GET })
	public void getBetMuiltResult(HttpServletRequest request, HttpServletResponse response, int gameId,  int roomLevel, int safetyModel, int betTimes) {
		log.info("betTimes:{} , roomLevel:{} safetyModel:{} gameId:{}" , betTimes, roomLevel, safetyModel, gameId);

		buildAsyncContext(request, (msgId)->{
			Map<String, Object> map = new ConcurrentHashMap<String, Object>();
			map.put("gameId", gameId);
			map.put("roomLevel", roomLevel);
			map.put("safetyModel", safetyModel);
			map.put("betTimes", betTimes);
			RocketMqRpcService.sendAndReceiveRpcMsg(this::rpcRespHandler, msgId, 0, AppConfig.getInstance().getGetBetMuiltResultServerId(), gson.toJson(map));
		});
	}

	/**
	 * @param request
	 * @param sendMsgFunc
	 */
	private void buildAsyncContext(HttpServletRequest request, Consumer<Long> sendMsgFunc) {
		AsyncContext asyncContext = request.startAsync();

		//设置超时时间
		asyncContext.setTimeout(60 * 1000 * 3);
		asyncContext.getResponse().setContentType("text/html;charset=utf-8");
		long msgId = IDGenerator.getId();
		respMap.put(msgId, asyncContext);

		asyncContext.addListener(new AsyncListener() {
			@Override
			public void onTimeout(AsyncEvent event) throws IOException {
				asyncContext.getResponse().getWriter().println("请求超时");
				asyncContext.complete();
			}
			@Override
			public void onStartAsync(AsyncEvent event) throws IOException {
				System.out.println("线程开始");
			}
			@Override
			public void onError(AsyncEvent event) throws IOException {
				System.out.println("发生错误："+event.getThrowable());
			}
			@Override
			public void onComplete(AsyncEvent event) throws IOException {
				System.out.println("执行完成");
				respMap.remove(msgId);
			}
		});

		asyncContext.start(()->{
			sendMsgFunc.accept(msgId);
		});
	}



	@Bean
	public Docket demoApi() {
		return new Docket(DocumentationType.SWAGGER_2)
				.select()
				.apis(RequestHandlerSelectors.any())
				.paths(PathSelectors.regex("(?!/error.*).*"))
				.build();
	}
}
