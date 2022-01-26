package com.frame.login.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.frame.enums.LoginType;
import com.frame.enums.SystemCode;
import com.frame.login.model.push.PushLoginInfo;
import com.frame.login.model.req.ReqLogin;
import com.frame.login.service.LoginService;
import com.frame.mobel.ApiResult;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * TODO
 * @author Sanjie
 * @date 2019-09-16 19:33
 * @version 1.0
 */
@Controller
@Slf4j
public class LoginServlet {
    @Autowired
    LoginService loginService;

    @ApiOperation("登录")
    @RequestMapping(value = "/login", method = {RequestMethod. POST, RequestMethod.GET })
    @ResponseBody
    public ApiResult<PushLoginInfo> login(HttpServletRequest req, HttpServletResponse response, ReqLogin request){
        log.info(request.toString());
        LoginType loginType = LoginType.valueOf(request.getLoginType());
        if(loginType == null) {
            return ApiResult.build(SystemCode.PARMS_TRANS_ERROR.getValue(), SystemCode.PARMS_TRANS_ERROR.getName());
        }
        ApiResult<PushLoginInfo> rs = null;
        switch (loginType) {
        case IMEI:
        	rs= loginService.touristLogin(request.getIMEI(), req);
        	break;
        case SYS_USERNAME:
        	rs=  loginService.userLogin(request.getName(),  request.getPsw(), req);
        	break;
        case MOBILE:
        	rs=  loginService.mobileLogin(request.getMobileNum(),  request.getMobileCode(), req);
        	break;
        case WECHAT:
        	rs= loginService.wechatLogin(request.getWechatCode(), req);
        	break;
        default:
        	rs= ApiResult.build(SystemCode.PARMS_TRANS_ERROR.getValue(), SystemCode.PARMS_TRANS_ERROR.getName());
        	break;
        }
        log.info(rs.toString());
        return rs;
    }

}
