package com.frame.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.frame.enums.HeadPicType;
import com.frame.enums.ServerType;
import com.frame.enums.SystemHeadPic;
import com.frame.id.DeskIdBuilder;
import com.frame.id.IdBuilder;
import com.frame.mobel.mq.AskRobotLogin;
import com.frame.model.Robot;
import com.frame.model.RobotUserInfo;
import com.frame.utils.FileUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RobotUserInfoConfigMrg {
	@Getter
	private static RobotUserInfoConfigMrg instance = new RobotUserInfoConfigMrg();

	private List<RobotUserInfo> robotUserInfoList;

	@Getter
	private Map<Long, Robot> runningRobotMap = new ConcurrentHashMap<Long, Robot>();

	public static String url = "conf/robot.json";

	private Gson gson = new Gson();
	/**
	 * @return
	 */
	public synchronized List<RobotUserInfo> getRobotUserInfoList() {
		if(robotUserInfoList == null) {
			String json = FileUtil.getFile(url);
			robotUserInfoList = gson.fromJson(json, new TypeToken<List<RobotUserInfo>>(){}.getType());
		}
		return robotUserInfoList;
	}

	/**
	 * @return
	 */
	public synchronized RobotUserInfo getOneRobot(AskRobotLogin askRobotLogin) {
		int index = 0;
		for (Robot robotUserInfo : runningRobotMap.values()) {
			if(robotUserInfo.getAskRobotLogin().getRoomId() == askRobotLogin.getRoomId()) {
				index ++;
			}
		}
		if(index >= 15) {
			log.error("roomId:{} 机器人满了", askRobotLogin.getRoomId());
			return null;
		}
		while(true) {
			index ++;
			if(index >= 1000) {
				return null;
			}
			RobotUserInfo one = getRobotUserInfoList().get((int) (Math.random() * robotUserInfoList.size()));
			if(runningRobotMap.get(one.getUserId()) == null) {
				return one;
			}
		}
	}

	public static void main(String[] args) {
		int serverId = DeskIdBuilder.getServerId(0, ServerType.ROBOT.getType());
		//初始化id生成器
		IdBuilder idBuilder = new IdBuilder(serverId);

		List<RobotUserInfo> list = new ArrayList<RobotUserInfo>();
		for (int i = 0; i < 500; i++) {
			list.add(RobotUserInfo.builder()
					.balance((long) (1000000 + 5000000*Math.random()))
					.headPic(SystemHeadPic.getRandomOne().getType()+"")
					.headPicType(HeadPicType.SYSTEM.getType())
					.name("Robot_"+i)
					.userId(idBuilder.nextId())
					.level((int) (10 * Math.random()))
					.build());
		}
		FileUtil.CreateJsonCode(list, url);
		RobotUserInfoConfigMrg.getInstance().getRobotUserInfoList();
	}
}
