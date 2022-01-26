package com.frame.center.utils;

import com.frame.center.rocketmq.RocketMqSender;
import com.frame.mobel.ProtoBuf;

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
