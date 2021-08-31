package com.sy.sanguo.game.pdkuai.util;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.sy.sanguo.game.pdkuai.constants.SharedConstants;
import com.sy.sanguo.game.pdkuai.db.bean.UserPlaylog;
import com.sy599.sanguo.util.TimeUtil;

public class PlayLogTool {
	/**
	 * 筛选
	 * 
	 * @param logList
	 * @param logType
	 * @param wanfaIds 查固定玩法战绩ID
	 */
	public static void screen(List<UserPlaylog> logList, int logType, List<Integer> wanfaIds) {
		Iterator<UserPlaylog> iterator = logList.iterator();
		Date now = TimeUtil.now();
		while (iterator.hasNext()) {
			UserPlaylog log = iterator.next();
			int apartHours = TimeUtil.apartHours(now, log.getTime());
			if (apartHours > 24) {
				iterator.remove();
				continue;
			}
			if(!wanfaIds.isEmpty() && !wanfaIds.contains((int)log.getLogId())) {// 查固定玩法战绩
				iterator.remove();
				continue;
			}
			if (wanfaIds.isEmpty() && logType != 0 && SharedConstants.getType((int) log.getLogId()) != logType) {
				iterator.remove();
				continue;
			}
			if (log.getExtend() != null && log.getExtend().contains("gold")) {
				iterator.remove();
			}
		}
	}
}
