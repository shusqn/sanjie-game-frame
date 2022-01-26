package com.frame.center.hander.cmd;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.frame.center.hander.PlayerOfflineHandler;
import com.frame.center.hander.RedisCenterServerHandler;
import com.frame.center.manager.PlayerMgr;
import com.frame.center.rocketmq.RocketMqReceive;
import com.frame.center.router.MpPbCmdRouter;
import com.frame.entity.User;
import com.frame.mobel.ProtoBuf;
import com.frame.mobel.mq.ChanelClose;
import com.frame.model.ServerInfo;
import com.frame.protobuf.CenterMsg;
import com.frame.protobuf.CommonMsg;
import com.frame.service.UserService;
import com.google.gson.Gson;
import com.google.protobuf.InvalidProtocolBufferException;

import lombok.extern.slf4j.Slf4j;

/**
 * SlotGameReqHandler.java
 * @author Sanjie
 * @date 2021-09-10 09:48
 * @version 1.0.0
 */
@Slf4j
@Component
public final class CenterReqCmdHandler {

	@PostConstruct
	private void init(){
		log.info("CenterReqCmdHandler init success !");
		//监听mq收到的ProtoBuf
		RocketMqReceive.getMqRouterHandler().registHandler(ProtoBuf.class.getSimpleName(), this::receiveMqData);
		//玩家断线
		RocketMqReceive.getMqRouterHandler().registHandler(ChanelClose.class.getSimpleName(), this::chanelClose);


		MpPbCmdRouter.getInstance().registHandler(CenterMsg.SubCmd.Cmd_ReqLoginCenter_VALUE, this::reqLoginCenter);
		MpPbCmdRouter.getInstance().registHandler(CenterMsg.SubCmd.Cmd_ReqGameServerId_VALUE, this::reqGameServerId);
	}

	private Gson gson = new Gson();
	/**
	 * @param pbvodata
	 */
	private void receiveMqData(String pbvodata) {
		try {
			ProtoBuf pbvo = gson.fromJson(pbvodata, ProtoBuf.class);
			MpPbCmdRouter.getInstance().executeHandler(pbvo.getCmd(), pbvo);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	/**
	 * @param pbvodata
	 */
	private void chanelClose(String pbvodata) {
		try {
			ChanelClose pbvo = gson.fromJson(pbvodata, ChanelClose.class);
			PlayerOfflineHandler.getInstance().offlineHandler(pbvo.getPid());
			log.info("pid:{} 离线", pbvo.getPid());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * 请求登录大厅
	 * @param pbvo
	 */
	private void reqLoginCenter(ProtoBuf pbvo) {
		long pid = pbvo.getPid();

		PlayerMgr.getInstance().put(pid, pbvo);
		//刷新在线人数
		RedisCenterServerHandler.getInstance().updateOnlineUsers(PlayerMgr.getInstance().values().size());

		User user = UserService.getInstance().findUser(pid);
		if (user == null) {
			CenterPushCmdHandler.getInstance().pushErrorMessage(pbvo, CommonMsg.ErrorCode.SYSTEM_ERR_VALUE, "玩家信息不存在");
			return;
		}
		ServerInfo serverInfo = null;
		int serverId = RedisCenterServerHandler.getInstance().getUserGameServerId(pid);

		if(serverId != 0) {
			serverInfo = RedisCenterServerHandler.getInstance().getServer(serverId);
		}
		CenterPushCmdHandler.getInstance().pushLoginCenter(pid, user, serverInfo);
	}

	/**
	 * 请求获取gameServerId
	 * @param pbvo
	 */
	private void reqGameServerId(ProtoBuf pbvo) {
		long pid = pbvo.getPid();

		CenterMsg.ReqGameServerId message = null;
		try {
			message = CenterMsg.ReqGameServerId.parseFrom(pbvo.getBody());
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}

		int serverType = message.getServerType();

		ServerInfo serverInfo = RedisCenterServerHandler.getInstance().getMinUsersServer(serverType);

		if(serverInfo == null) {
			CenterPushCmdHandler.getInstance().pushErrorMessage(pbvo, CommonMsg.ErrorCode.SYSTEM_ERR_VALUE, "找不到相对应的服务器:"+serverType);
			return;
		}
		CenterPushCmdHandler.getInstance().pushGameServerId(pid, serverInfo);
	}

}




