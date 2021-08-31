package com.sy599.game.common.executor.task;

import com.sy599.game.util.LogUtil;

/**
 * 时间任务
 * @author lc
 *
 */
public abstract class BaseTask {
	protected int hour;
	protected int minute;

	public BaseTask(int hour, int minute) {
		this.hour = hour;
		this.minute = minute;

	}

	public abstract void run();

	/**
	 * 是否当前小时和分钟数执行该任务
	 * 
	 * @param hour
	 * @param minute
	 * @return
	 */
	public boolean isRun(int hour, int minute) {
		if (hour == this.hour && minute == this.minute) {
			LogUtil.monitorLog.info("-----" + hour + ":" + minute + " task begin---");
			run();
			LogUtil.monitorLog.info("-----" + hour + ":" + minute + " task end---");
			return true;
		}
		return false;
	}
}
