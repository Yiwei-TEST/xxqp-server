package com.sy.sanguo.common.executor.task;

import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.game.pdkuai.db.dao.UserMessageDao;
import com.sy.sanguo.game.pdkuai.util.LogUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ClearLogTask extends BaseTask {
	public ClearLogTask(int hour, int minute) {
		super(hour, minute);
	}

	public void run() {
		LogUtil.i("start:"+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		int delCount = UserMessageDao.getInstance().deleteMessageByDay();
		LogUtil.i("end:"+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		GameBackLogger.SYS_LOG.info("deleteMessageByDay count-->" + delCount);

	}

}
