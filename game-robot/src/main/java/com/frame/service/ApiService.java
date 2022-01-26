package com.frame.service;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import com.frame.mobel.ApiResult;
import com.frame.model.Robot;
import com.frame.utils.HttpUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ApiService {
	@Getter
	private static ApiService instance;

	@PostConstruct
	private void init() { 
		instance = this;
	}
	
	private Gson gson = new Gson();
	/**
	 *  游客登录
	 * @param udid
	 * @param url
	 * @return
	 */
	public Robot getRobot(String udid, String url ) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("loginType", 15);
		map.put("IMEI", udid);
		String  rs = HttpUtil.PostHttpWebURL(url, map);
		ApiResult<Robot> apiResult = gson.fromJson(rs, new TypeToken<ApiResult<Robot>>(){}.getType());
		if(apiResult.getCode() != 0) {
			return null;
		}
		Robot robot = apiResult.getResult();
		return robot;
	}

}
