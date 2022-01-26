package com.frame.executor;

import com.frame.executor.BaseExecutor;

import lombok.Getter;

/**
 * TODO
 * @author Sanjie
 * @date 2020-01-20 11:10
 * @version 1.0
 */
public final class LoginExecutorHandler extends BaseExecutor{
	private LoginExecutorHandler(int nthreads, String threadName) {
		super(nthreads, threadName);
	}

	@Getter
	private static LoginExecutorHandler instance = new LoginExecutorHandler(1, "CmdExecutor");

}
