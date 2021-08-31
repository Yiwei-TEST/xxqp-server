package com.sy.sanguo.common.util;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * 异步任务执行器
 * 
 * @author taohuiliang
 * @date 2012-10-5
 * @version v1.0
 */
public class TaskExecutor {
	private static final ScheduledExecutorService scheExecutor = Executors.newScheduledThreadPool(2,createThreadFactory("_scheduleExecutor_thread_pool"));
	private static final TaskExecutor _inst = new TaskExecutor();
	public static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();
	private TaskExecutor() {

	}

	private static final ThreadFactory createThreadFactory(final String threadName) {
		return new ThreadFactory() {
			final ThreadFactory defaultFactory = Executors.defaultThreadFactory();

			public Thread newThread(final Runnable r) {
				Thread thread = defaultFactory.newThread(r);
				thread.setName("qipaiLogin" + threadName);
				return thread;
			}
		};
	}

	public static TaskExecutor getInstance() {
		return _inst;
	}

	/** 提交任务，立马执行 */
	public void submitTask(Runnable task) {
		if (task != null) {
			EXECUTOR_SERVICE.submit(task);
		}
	}

	/** 顺序提交任务，立马执行,将用户提交的task与thread绑定 */
	public void submitOrderTask(long userId, Runnable task) {
		if (task != null) {
			EXECUTOR_SERVICE.submit(task);
		}
	}

	/**
	 * 固定周期地执行某个任务
	 * 
	 * @param task
	 *            具体任务
	 * @param initDelay
	 *            初次执行的延迟时间，以ms为单位
	 * @param period
	 *            多长周期执行一次任务，以ms为单位
	 */
	public void submitSchTask(Runnable task, long initDelay, long period) {
		scheExecutor.scheduleAtFixedRate(task, initDelay, period, TimeUnit.MILLISECONDS);
	}

	private static final Timer TIMER = new Timer("Scheduled-Timer");

	public void scheduleWithFixedRate(TimerTask timerTask, Date firstDate, long period){
		TIMER.scheduleAtFixedRate(timerTask,firstDate,period);
	}
	/**
	 * 延时执行某个任务
	 * 
	 * @param task
	 *            具体任务
	 * @param initDelay
	 *            初次执行的延迟时间，以ms为单位
	 */
	public void submitSchTask(Runnable task, long initDelay) {
		scheExecutor.schedule(task, initDelay, TimeUnit.MILLISECONDS);
	}

	/** 等待所有执行器将未完成任务执行完再关闭 */
	public void shutDown() {
		EXECUTOR_SERVICE.shutdown();
		scheExecutor.shutdown();
	}

}
