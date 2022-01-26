package com.frame.login.service;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;

import com.frame.entity.User;
import com.frame.enums.ServerType;
import com.frame.enums.SystemCode;
import com.frame.login.LoginMrg;
import com.frame.login.handler.RedisLoginServerHandler;
import com.frame.login.model.push.PushLoginInfo;
import com.frame.login.model.wechat.WeChatUserInfo;
import com.frame.login.utils.IPUtil;
import com.frame.login.utils.WechatHelper;
import com.frame.mobel.ApiResult;
import com.frame.model.ServerInfo;

/**
 * TODO
 * @author Sanjie
 * @date 2019-09-17 16:24
 * @version 1.0
 * 与业务流程有关系的类禁止使用静态方法
 */
@Service
public class LoginService {
    
    /**
     * 手机登录
     * @return
     */
    public ApiResult<PushLoginInfo> mobileLogin(String mobileNum, String mobileCode, HttpServletRequest req) {
        if(mobileNum == null || mobileCode == null) {
            return  ApiResult.build(SystemCode.PARMS_TRANS_ERROR.getValue(), SystemCode.PARMS_TRANS_ERROR.getName());
        }
        if(!checkmobileCode(mobileNum, mobileCode)){
            return  ApiResult.build(SystemCode.VERCODE_ERROR.getValue(), SystemCode.VERCODE_ERROR.getName());
        }
        //验证是否数据库里有该手机号
        User user = UserService.getInstance().getUserBymobile(mobileNum);
        if(user == null){
            String ip = IPUtil.getIpAddr(req);
            //创建用户相关信息
            user = UserService.getInstance().buildUserBymobile(mobileNum, ip);
        }
        return ApiResult.build(buildLoginSuccessVO(user));
    }
    
    /**
     * 检测验证码是否正确
     * @return
     */
    public boolean checkmobileCode(String mobileNum, String mobileCode) {
        return true;
    }

    /**
     * 用户名登录
     * @param name
     * @param psw
     * @param req
     * @return
     */
    public ApiResult<PushLoginInfo> userLogin(String name, String psw, HttpServletRequest req) {
        if(name == null || psw == null) {
        	 return  ApiResult.build(SystemCode.PARMS_TRANS_ERROR.getValue(), SystemCode.PARMS_TRANS_ERROR.getName());
        }
        //验证是否数据库里有该手机号
        User user = UserService.getInstance().getUserByPsw(name, psw);
        if(user == null){
        	 return  ApiResult.build(SystemCode.USER_OR_PASS_ERROR.getValue(), SystemCode.USER_OR_PASS_ERROR.getName());
        }
        return ApiResult.build(buildLoginSuccessVO(user));
    }
    
    /**
     * 微信登录
     * @param wechatCode
     * @param mobileCode
     * @param req
     * @return
     */
    public ApiResult<PushLoginInfo> wechatLogin(String wechatCode, HttpServletRequest req) {
        if(wechatCode == null) {
        	 return  ApiResult.build(SystemCode.PARMS_TRANS_ERROR.getValue(), SystemCode.PARMS_TRANS_ERROR.getName());
        }
        WeChatUserInfo weChatUserInfo = WechatHelper.getWechatUnionid(wechatCode);
        if(weChatUserInfo == null){
        	 return  ApiResult.build(SystemCode.PARMS_TRANS_ERROR.getValue(), SystemCode.PARMS_TRANS_ERROR.getName());
        }
        User  user = UserService.getInstance().getUserByWechatUnionid(weChatUserInfo.getUnionid());
        if(user == null){
            String ip = IPUtil.getIpAddr(req);
            //创建用户相关信息
            user = UserService.getInstance().buildUserByWechatCode(weChatUserInfo, ip);
        }
        return ApiResult.build(buildLoginSuccessVO(user));
    }

    /**
     * 游客登录
     * @param imei
     * @param req
     * @return
     */
    public ApiResult<PushLoginInfo> touristLogin(String IMEI, HttpServletRequest req) {
        if(IMEI == null){
        	 return  ApiResult.build(SystemCode.PARMS_TRANS_ERROR.getValue(), SystemCode.PARMS_TRANS_ERROR.getName());
        }
        User  user = UserService.getInstance().getUserByIMEI(IMEI);
        if(user == null){
            String ip = IPUtil.getIpAddr(req);
            //创建用户相关信息
            user = UserService.getInstance().buildUserByIMEI(IMEI, ip, LoginMrg.getInsance().getIdGenerator().nextId());
        }
        return ApiResult.build(buildLoginSuccessVO(user));
    }
    
    /**
     * @param user
     * @return
     */
    private PushLoginInfo buildLoginSuccessVO(User  user) {
        Long uid = user.getUid();
        String token = UUID.randomUUID().toString().replace("-", "");
        ServerInfo gatewayServerInfo = RedisLoginServerHandler.getInstance().getMinUsersServer(ServerType.GATEWAY.getType());
        RedisLoginServerHandler.getInstance().setToken(uid, token);
        return PushLoginInfo.buildLoginSuccess(uid, token, gatewayServerInfo.getHostname(), gatewayServerInfo.getPort(), gatewayServerInfo.getSid());
    }
    	
    
}
