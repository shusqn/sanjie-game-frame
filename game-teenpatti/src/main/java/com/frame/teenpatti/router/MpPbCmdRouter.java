package com.frame.teenpatti.router;

import com.frame.mobel.ProtoBuf;
import com.frame.router.BaseRouterHander;

import lombok.Getter;

/**
 * NettyCmdRouter.java
 * @author Sanjie
 * @date 2021-09-10 10:26
 * @version 1.0.0
 */
public final class MpPbCmdRouter extends BaseRouterHander<Integer, ProtoBuf>{
	@Getter
	private static MpPbCmdRouter instance = new MpPbCmdRouter();
}
