package com.frame.executor;

import com.frame.executor.BaseExecutor;

import lombok.Getter;

/**
 * TODO
 * @author Sanjie
 * @date 2020-01-20 11:10
 * @version 1.0
 */
public final class RobotExecutor extends BaseExecutor{
	private RobotExecutor(int nthreads, String threadName) {
		super(nthreads, threadName);
	}
	@Getter
	private static RobotExecutor instance = new RobotExecutor(10, "RobotExecutor");
}
