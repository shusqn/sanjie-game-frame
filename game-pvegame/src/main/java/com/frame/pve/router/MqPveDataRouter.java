package com.frame.pve.router;

import com.frame.mobel.mq.MqTransmissionData;
import com.frame.router.BaseRouterHander;

import lombok.Getter;

public class MqPveDataRouter extends BaseRouterHander<Integer, MqTransmissionData>{
	@Getter
	private static MqPveDataRouter instance = new MqPveDataRouter();
}
