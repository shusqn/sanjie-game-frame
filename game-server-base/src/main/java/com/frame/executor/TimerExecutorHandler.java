package com.frame.executor;

import lombok.Getter;

/**
 * TODO
 * @author Sanjie
 * @date 2020-01-20 11:10
 * @version 1.0
 */
public final class TimerExecutorHandler extends BaseExecutor{
	private TimerExecutorHandler(int nthreads, String threadName) {
		super(nthreads, threadName);
	}

	@Getter
	private static TimerExecutorHandler instance = new TimerExecutorHandler(1, "TimerExecutor");

}
