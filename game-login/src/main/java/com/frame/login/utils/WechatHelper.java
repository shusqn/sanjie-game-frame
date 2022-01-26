package com.frame.login.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.frame.login.config.LoginConfig;
import com.frame.login.model.wechat.AccessTokenInfo;
import com.frame.login.model.wechat.ModelWechat;
import com.frame.login.model.wechat.WeChatUserInfo;
import com.frame.utils.Ggson;

import cn.frame.utils.HttpUtil;

/**
 * TODO
 * @author Sanjie
 * @date 2019-09-17 17:05
 * @version 1.0
 */
public class WechatHelper {

	private final static ConcurrentHashMap<String, ModelWechat> WechatInfoCache = new ConcurrentHashMap<String, ModelWechat>();
	//=============================================================
	/**
	 * 获取微信唯一id
	 * @param wechatCode 前端上传的验证码
	 * @return
	 */
	public static WeChatUserInfo getWechatUnionid(String wechatCode){
		String URL = "https://api.weixin.qq.com/sns/oauth2/access_token";

		ModelWechat modelWechat = getModelWechat(wechatCode);
		AccessTokenInfo accessTokenInfo = null;
		String backJson;

		if(modelWechat != null){
			accessTokenInfo = new AccessTokenInfo();
			accessTokenInfo.setAccess_token(modelWechat.getAccess_token());
			accessTokenInfo.setOpenid(modelWechat.getOpenid());
		}
		else{
			String appid = LoginConfig.getInstance().getAppid();
			String secret = LoginConfig.getInstance().getSecret();

			String grant_type = "authorization_code";

			URL +=("?appid="+appid+"&"+"secret="+secret+"&"+"code="+wechatCode+"&"+"grant_type="+grant_type);
			backJson = HttpUtil.GetHttpWebURL(URL);

			accessTokenInfo = Ggson.gson.fromJson(backJson, AccessTokenInfo.class);

			URL = "";
			URL += "https://api.weixin.qq.com/sns/oauth2/refresh_token?appid="
					+ ""+appid+"&grant_type=refresh_token&refresh_token="+accessTokenInfo.getRefresh_token();
			backJson = HttpUtil.GetHttpWebURL(URL);

			accessTokenInfo = Ggson.gson.fromJson(backJson, AccessTokenInfo.class);

			if(accessTokenInfo.getAccess_token() != null){
				modelWechat = new ModelWechat();
				modelWechat.setAccess_token(accessTokenInfo.getAccess_token());
				modelWechat.setOpenid(accessTokenInfo.getOpenid());

				addModelWechat(wechatCode, modelWechat);
			}
		}

		if(accessTokenInfo.getAccess_token() != null){
			URL = "https://api.weixin.qq.com/sns/userinfo";
			Map<String,Object> dataBack = new HashMap<String, Object>();
			dataBack.put("access_token", accessTokenInfo.getAccess_token());
			dataBack.put("openid", accessTokenInfo.getOpenid());
			backJson = HttpUtil.PostHttpWebURL(URL, dataBack);

			WeChatUserInfo weChatUserInfo = Ggson.gson.fromJson(backJson, WeChatUserInfo.class);

			System.err.println("backJson:" + backJson);

			if(weChatUserInfo.getUnionid() == null){
				weChatUserInfo = null;
				removeModelWechat(wechatCode);
			}
			return weChatUserInfo;
		}
		
		return null;
	}

	private static void addModelWechat(String wechatCode, ModelWechat modelWechat) {
		WechatInfoCache.put(wechatCode, modelWechat);
	}
	
	private static ModelWechat getModelWechat(String wechatCode) {
		return WechatInfoCache.get(wechatCode);
	}

	private static void removeModelWechat(String wechatCode) {
		WechatInfoCache.remove(wechatCode);
	}
}
