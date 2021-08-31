package com.sy.sanguo.game.competition.job;

import java.util.concurrent.ThreadFactory;

/**
 * @author Guang.OuYang
 * @date 2020/5/20-10:21
 */
public class ThreadFactoryReWrite implements ThreadFactory {

	private String threadName;

	public ThreadFactoryReWrite(String threadName) {
		this.threadName = threadName;
	}

	@Override
	public Thread newThread(Runnable r) {
		return new Thread(r, threadName);
	}
}
