package com.frame.teenpatti.utils;

import com.frame.mobel.ProtoBuf;
import com.frame.teenpatti.rocketmq.RocketMqSender;

public class MsgUtils {

	/**
	 * @param pbvo
	 */
	public static void pushToGateway(ProtoBuf pbvo) {
		RocketMqSender.getInstance().pushPb2Gateway(pbvo);
	}
	
	/**
	 * @param pbvo
	 */
	public static void noticeToGateway(ProtoBuf pbvo) {
		RocketMqSender.getInstance().noticePb2Gateway(pbvo);
	}
}
