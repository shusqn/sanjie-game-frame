package com.frame.executor;

import lombok.Getter;

/**
 * TODO
 * @author Sanjie
 * @date 2020-01-20 11:10
 * @version 1.0
 */
public final class RoomExecutorHandler extends BaseExecutor{
	private RoomExecutorHandler(int nthreads, String threadName) {
		super(nthreads, threadName);
	}

	@Getter
	private static RoomExecutorHandler instance = new RoomExecutorHandler(1, "RoomExecutor");

}
