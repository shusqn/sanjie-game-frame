package com.frame.executor;

import com.frame.executor.BaseExecutor;

import lombok.Getter;

/**
 * TODO
 * @author Sanjie
 * @date 2020-01-20 11:10
 * @version 1.0
 */
public  final class DeskExecutorHandler extends BaseExecutor{
	private DeskExecutorHandler(int nthreads, String threadName) {
		super(nthreads, threadName);
	}

	@Getter
	private static DeskExecutorHandler instance = new DeskExecutorHandler(5, "DeskExecutor");
}
