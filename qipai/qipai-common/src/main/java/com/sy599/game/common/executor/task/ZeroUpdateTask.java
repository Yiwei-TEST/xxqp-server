package com.sy599.game.common.executor.task;

import com.sy599.game.character.Player;
import com.sy599.game.db.bean.Activity;
import com.sy599.game.db.dao.UserTwinRewardDao;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.util.ActivityUtil;

import java.util.Calendar;
import java.util.Date;

/**
 * 零点刷新部分数据，考虑到误差不一定设定为零点，具体时间可能会往后推迟数分钟。 考虑到多台服务器，每天服务器都会执行一遍代码，不适合sql执行全表操作
 */
public class ZeroUpdateTask implements Runnable {
	@Override
	public void run() {
		ActivityUtil.init();
		for (Player player : PlayerManager.playerMap.values()) {
			player.getMission().rectifyData();
			player.updateUserTwinReward();
		}
		// 清理累计胜利奖励表中数据
		UserTwinRewardDao.getInstance().deleteUserTwinReward();
	}

	/**
	 * 设置零点时间，延时了10秒
	 * 
	 * @return
	 */
	public static int getZeroDelay() {
		Calendar ca = Calendar.getInstance();
		ca.setTime(new Date());
		ca.set(Calendar.HOUR_OF_DAY, 0);
		ca.set(Calendar.MINUTE, 0);
		ca.set(Calendar.SECOND, 10);
		ca.set(Calendar.SECOND, 0);
		ca.add(Calendar.DATE, +1);
		int delay = (int) (ca.getTimeInMillis() - System.currentTimeMillis());
		return delay;
	}
}
