package com.sy599.game.common.executor.task;

import com.sy599.game.manager.DBManager;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.TimeUtil;

public class HeartBeatTask implements Runnable {

	public void run() {
		
		long beginTime = TimeUtil.currentTimeMillis();
		try {
			DBManager.saveDB(true);
		} catch (Throwable e) {
			LogUtil.dbLog.error("---HB,saveDB----"+e.getMessage(), e);
		}
		if (TimeUtil.currentTimeMillis() - beginTime>1000){
			LogUtil.dbLog.info("---HB,2----tTime:" + (TimeUtil.currentTimeMillis() - beginTime));
		}
	}

}
