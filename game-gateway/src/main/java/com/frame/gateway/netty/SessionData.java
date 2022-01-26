package com.frame.gateway.netty;

import java.util.concurrent.atomic.AtomicInteger;

import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.Getter;

/**
 * @Title: NettyChannel.java
 * @Package socket.netty
 * @Description: TODO(用一句话描述该文件做什么)
 * @author sanjie
 * @date 2018年7月25日 上午10:39:33
 * @version V1.0
 */
@Getter
public class SessionData {
	private static final AttributeKey<SessionData> NETTY_CHANNEL_KEY = AttributeKey.valueOf("netty.channel.player");
	private SessionData() {}
	
	private long pid;
	private AtomicInteger seqCount = new AtomicInteger();
	
	public static void setChannelData(Channel channel, long pid){
		Attribute<SessionData> attr = channel.attr(NETTY_CHANNEL_KEY);
        SessionData data = new SessionData();
        data.pid = pid;
        attr.setIfAbsent(data);
        SessionMgr.getInstance().put(pid, channel);
	}
	public static SessionData getChanelData(Channel channel){
		Attribute<SessionData> attr = channel.attr(SessionData.NETTY_CHANNEL_KEY);
		if(attr == null){
			return null;
		}
		return attr.get();
	}
}
