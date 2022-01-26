package com.frame.gateway.utils;

import com.frame.mobel.ProtoBuf;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

/**
 * MsgUtils.java
 * @author Sanjie
 * @date 2022-01-04 10:39
 * @version 1.0.0
 */
@Slf4j
public class MsgUtils {
	/**
	 * @param buf
	 * @return
	 */
	public static ProtoBuf read(ByteBuf buf) {
		int cmd = buf.readShort();
		int sid = buf.readUnsignedShort();
		byte[] bytes = new byte[buf.readableBytes()];
		buf.readBytes(bytes);
		
		return ProtoBuf.build(cmd, bytes, sid);
	}
	
	/**
	 * @param pb
	 * @return
	 */
	private static BinaryWebSocketFrame writeFrame(ProtoBuf pb) {
		BinaryWebSocketFrame frame = new BinaryWebSocketFrame();
		frame.content().writeShort(pb.getCmd());
		frame.content().writeShort(pb.getSid());
		if(pb.getBody() != null) {
			frame.content().writeBytes(pb.getBody());
		}
		return frame;
	}
	
	/**
	 * 发送到websocket 到客户端
	 * @param channel
	 * @param pbvo
	 */
	public static void sendToWsMsg(Channel channel, ProtoBuf pbvo){
		if(channel==null){
			log.error("pid:{} 不在线无法发送消息{}", pbvo.getPid(), pbvo.toString());
			return;
		}
		channel.writeAndFlush(writeFrame(pbvo));
	}
}
