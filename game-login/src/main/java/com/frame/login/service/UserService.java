package com.frame.login.service;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.frame.dao.UserDao;
import com.frame.entity.User;
import com.frame.enums.HeadPicType;
import com.frame.enums.SystemHeadPic;
import com.frame.login.config.LoginConfig;
import com.frame.login.model.wechat.WeChatUserInfo;
import com.frame.service.UserAccountsService;

import lombok.Getter;


/**
 * TODO
 * @author Sanjie
 * @date 2019-09-17 17:36
 * @version 1.0
 */
@Component
public class UserService {
	@Getter
	private static UserService instance;
	
	@Autowired
	private UserDao userDao;

	@PostConstruct
	private void init() { 
		instance = this;
	}
	
	/**
	 * 根据微信返回的唯一id获取用户信息
	 * @param unionid
	 * @return
	 */
	public User getUser(long uid) {
		return userDao.getOne(uid);
	}
	
	/**
	 * 根据微信返回的唯一id获取用户信息
	 * @param unionid
	 * @return
	 */
	public User getUserByWechatUnionid(String unionid) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * @param mobileNum
	 * @return
	 */
	public User getUserByPsw(String name, String psw) { 
		return null;
	}
	
	/**
	 * 根据手机号获取用户
	 * @param mobileNum
	 * @return
	 */
	public User getUserBymobile(String mobileNum) { 
		return null;
	}

	/**
	 * 根据手机号创建用户
	 * @param mobileNum
	 * @param ip 
	 * @return
	 */
	public User buildUserBymobile(String mobileNum, String ip) { 
		return null;
	}

	public User buildUserByWechatCode(WeChatUserInfo weChatUserInfo, String ip) {
		return null;
	}

	public User getUserByIMEI(String imei) {
		return userDao.getUserByIMEI(imei);
	}

	/**
	 *根据IME创建用户
	 * @param imei
	 * @param ip
	 * @return
	 */
	public User buildUserByIMEI(String imei, String ip, long uid) {
		User user = new User();
		user.setUid(uid);
		user.setImei(imei);
		user.setIp(ip);
		user.setHeadPicType(HeadPicType.SYSTEM.getType());
		user.setHeadPic(SystemHeadPic.getRandomOne().getType() + "");
		user.setNickName("u"+uid);
		
		UserAccountsService.getInstance().insertUserAccount(user.getUid());
		UserAccountsService.getInstance().upDataUserAccount(user.getUid(), LoginConfig.getInstance().getInitBalance());
		
		return saveUser(user);
	}
	
	private User saveUser(User user) {
		return userDao.save(user);
	}

}
