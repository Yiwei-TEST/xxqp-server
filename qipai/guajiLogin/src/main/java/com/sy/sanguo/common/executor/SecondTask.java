package com.sy.sanguo.common.executor;

import com.sy.sanguo.game.pdkuai.helper.FakeTableHepler;
import com.sy.sanguo.game.pdkuai.util.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecondTask implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(SecondTask.class);

	/** 分钟数量 **/
	private long secondCount = 0;

	@Override
	public void run() {
		secondCount++;
		if (secondCount % 5 == 0) {	//每5秒检测下假桌子的刷新
			long time = System.currentTimeMillis();
			FakeTableHepler.checkFakeTableRefresh();
			LogUtil.i((new StringBuilder(256)).append("checkFakeTableRefresh").append(" time(ms):").append(System.currentTimeMillis() - time).toString());
		}
	}

}
