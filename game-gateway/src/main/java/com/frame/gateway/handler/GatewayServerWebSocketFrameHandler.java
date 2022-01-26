package com.frame.gateway.handler;

import com.frame.enums.ServerType;
import com.frame.gateway.GatewayMrg;
import com.frame.gateway.netty.SessionData;
import com.frame.gateway.netty.SessionMgr;
import com.frame.gateway.rocketmq.RocketMqSender;
import com.frame.gateway.utils.MsgUtils;
import com.frame.mobel.ProtoBuf;
import com.frame.model.ServerInfo;
import com.frame.netty.handler.AbstrackWebSocketFrameHandlerV3;
import com.frame.netty.handler.ChannelIpData;
import com.frame.protobuf.CommonMsg;
import com.frame.protobuf.CommonMsg.ErrorCode;
import com.frame.protobuf.GatewayMsg;
import com.frame.protobuf.RobotMsg;
import com.google.gson.Gson;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GatewayServerWebSocketFrameHandler extends AbstrackWebSocketFrameHandlerV3 {

	@Override
	public void connectClose(Channel  channel) {
		SessionData sessionData = SessionData.getChanelData(channel);
		if(sessionData == null) {
			log.info("连接断开：ip:{} channelId:{}", ChannelIpData.getChanelDataIP(channel) ,channel.id().asShortText());
			return;
		}
		long pid = sessionData.getPid();
		Channel oldChannel = SessionMgr.getInstance().get(pid);
		if(oldChannel == null || !oldChannel.id().asShortText().equals(channel.id().asShortText())) {
			return;
		}
		SessionMgr.getInstance().remove(pid);
		RocketMqSender.getInstance().noticeChanelClose2Server(pid);
		//刷新在线人数
		RedisGatewayServerHandler.getInstance().updateOnlineUsers(SessionMgr.getInstance().getSize());
		log.info("ip:{} pid {} channelId:{} 连接断开",  ChannelIpData.getChanelDataIP(channel), pid, channel.id().asShortText());
	}

	@Override
	public void readCmdData(Channel channel, ByteBuf buf) {
		ProtoBuf pbvo = null;
		try {
			pbvo = MsgUtils.read(buf);
			//处理登录
			if(pbvo.getCmd() == GatewayMsg.SubCmd.Cmd_ReqIdentify_VALUE) {
				identify(channel, pbvo, false);
				return;
			}
			else if(pbvo.getCmd() == RobotMsg.SubCmd.Cmd_ReqRobotLogin_VALUE) {
				identify(channel, pbvo, true);
				return;
			}
			SessionData sessionData = SessionData.getChanelData(channel);
			if(sessionData == null){
				log.error("非法的通信请求ddd cmd:{} ip:{}------", pbvo.getCmd(), ChannelIpData.getChanelDataIP(channel) );
				channel.close();
				return;
			}
			//心跳
			if(pbvo.getCmd() == GatewayMsg.SubCmd.Cmd_ReqHeartBeat_VALUE) {
				GatewayMsg.PushHeartBeat.Builder builder = GatewayMsg.PushHeartBeat.newBuilder();
				MsgUtils.sendToWsMsg(channel, pbvo.buildNew(GatewayMsg.SubCmd.Cmd_PushHeartBeat_VALUE, 
						builder.setServerTime(System.currentTimeMillis()).build().toByteArray()));
				
				//RocketMqSender.getInstance().noticeHeartBeat2Server(sessionData.getPid());
				return;
			}
			pbvo.bindPidAndGid(sessionData.getPid(), GatewayMrg.getInstance().getServerId());
			// 转发到其他服务器
			int sid = pbvo.getSid();
			ServerInfo serverInfo = RedisGatewayServerHandler.getInstance().getServer(sid);
			if(serverInfo == null) {
				log.error("找不到可用的 sid:{} 的服务器", sid);
				return;
			}
			if(serverInfo.getStype() == ServerType.GATEWAY.getType()) {
				log.error("不可知的通信数据 {}", pbvo.toString());
				channel.close();
				return;
			}
			RocketMqSender.getInstance().pushPb2Server(sid, pbvo);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public void connectSuccess(Channel channel) {
		//log.info("channelId:{} connect success---", ((InetSocketAddress)channel.remoteAddress()).getAddress().getHostAddress(), channel.id().asShortText());
	}

	private static Gson gson = new Gson();
	/**
	 * 认证登录合法性
	 * @param channel
	 * @param pbvo
	 */
	private void identify(Channel channel, ProtoBuf pbvo, boolean robot) {
		String token = null;
		long pid = 0;
		try {
			if(!robot) {
				GatewayMsg.ReqIdentify message;
				message = GatewayMsg.ReqIdentify.parseFrom(pbvo.getBody());
				token = message.getToken();
				String pidString = message.getPid();
				pid = Long.valueOf(pidString);
				log.info(message.toString());
			}
			else {
				RobotMsg.ReqRobotLogin message;
				message = RobotMsg.ReqRobotLogin.parseFrom(pbvo.getBody());
				token = message.getToken();
				String pidString = message.getPid();
				pid = Long.valueOf(pidString);
				log.info("机器人登录：{}", message.toString());
			}
			
			if(token == null || !token.equals(RedisGatewayServerHandler.getInstance().getToken(pid, robot))) {
				log.error("pid: {} token 错误", pid);
				pushErrorMessage(channel, pbvo, CommonMsg.ErrorCode.TOKEN_ERR_VALUE, " token 错误");
				channel.close();
				return;
			}

			Channel other = SessionMgr.getInstance().get(pid);
			if(other != null) {
				log.info("pid:{} channelId:{} 你的账号在其他地方登录", pid, other.id().asShortText());
				pushErrorMessage(channel, pbvo, CommonMsg.ErrorCode.OTHER_LOGIN_ERR_VALUE, " 你的账号在其他地方登录");
				other.close();
			}
			SessionData.setChannelData(channel, pid);
			//刷新在线人数
			RedisGatewayServerHandler.getInstance().updateOnlineUsers(SessionMgr.getInstance().getSize());
			RedisGatewayServerHandler.getInstance().delToken(pid);

			ServerInfo centreServerInfo = RedisGatewayServerHandler.getInstance().getMinUsersServer(ServerType.CENTER.getType());
			if(centreServerInfo == null) {
				throw new RuntimeException("没有可用的大厅服务器");
			}
			
			if(robot) {
				MsgUtils.sendToWsMsg(channel, 
						pbvo.buildNew(RobotMsg.SubCmd.Cmd_PushRobotLogin_VALUE, 
								RobotMsg.PushRobotLogin.newBuilder()
								.build().toByteArray()));
			}
			else {
				MsgUtils.sendToWsMsg(channel, 
						pbvo.buildNew(GatewayMsg.SubCmd.Cmd_PushIdentify_VALUE, 
								GatewayMsg.PushIdentify.newBuilder()
								.setCentreSid(centreServerInfo.getSid())
								.build().toByteArray()));
			}
			log.info("玩家pid:{} 认证通过", pid);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			pushErrorMessage(channel, pbvo, ErrorCode.SYSTEM_ERR_VALUE, e.getMessage());
		}
	}
	
	/**
	 * @param channel
	 * @param pbvo
	 * @param errCode
	 */
	private void pushErrorMessage(Channel channel, ProtoBuf pbvo, int errCode, String... args){
		GatewayMsg.PushErrorMsg.Builder builder = GatewayMsg.PushErrorMsg.newBuilder();
		builder.setCode(errCode);
		for (String arg : args) {
			builder.addArgs(arg);
		}
		MsgUtils.sendToWsMsg(channel, pbvo.buildNew(GatewayMsg.SubCmd.Cmd_PushErrorMsg_VALUE, builder.build().toByteArray()));
	}

	/**
	 * @param message
	 */
	public static void receiveMqData(String pbvodata) {
		try {
			ProtoBuf pbvo = gson.fromJson(pbvodata, ProtoBuf.class);
			Channel channel = SessionMgr.getInstance().get(pbvo.getPid());
			MsgUtils.sendToWsMsg(channel, pbvo);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

}
