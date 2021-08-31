package com.sy.sanguo.game.competition.util;

import com.sy.sanguo.game.competition.dao.CompetitionCommonDao;
import com.sy.sanguo.game.competition.util.ParamCheck.ConsumerC;
import com.sy.sanguo.game.pdkuai.util.LogUtil;

/**
 * @author Guang.OuYang
 * @date 2020/5/22-17:33
 */
public class LockSharing {
	/**分片接管默认标记*/
	public static String DEFAULT_SHARING_FLAG = "0";

	public static String DEFAULT_SHARING_FLAG_TO = "1";

	public static String getDefaultSharingFlagTo() {
		if (DEFAULT_SHARING_FLAG_TO.equals("1")) {
			try {
				DEFAULT_SHARING_FLAG_TO = NetworkUtils.getInet4Address() + ":" + NetworkUtils.getHttpPort();
				LogUtil.i("LockSharing:" + DEFAULT_SHARING_FLAG_TO);
			} catch (Exception e) {
				LogUtil.e("getDefaultSharingFlagTo",e);
			}
		}

		return DEFAULT_SHARING_FLAG_TO;
	}

	public static void releaseLock(CompetitionCommonDao dao) {
		dao.releaseShardingTakeOverLock(getDefaultSharingFlagTo(), DEFAULT_SHARING_FLAG);
	}

	/**
	 *@description 多进程同时处理一段业务逻辑时, 会产生竞争, 这里暂且用数据库事务锁代替
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/5/22
	 */
	public static <T, R> Boolean lock(CompetitionCommonDao dao, long id, Runnable r) {
		try {
			return lock(dao, id, () -> {
				r.run();
				return true;
			}) != null;
		} catch (Exception e) {
			LogUtil.e("competition|lockSharing|error|",e);
		}
		return false;
	}

	public static <T, R> R lock(CompetitionCommonDao dao, long id, ConsumerC<R> r) {
		boolean shareMaster = false;
		try {
			//锁定处理分片
			if (shareMaster = dao.updateShardingTakeOver(id, DEFAULT_SHARING_FLAG, getDefaultSharingFlagTo()) > 0) {
				return r.invoke();
			}
		} finally {
			if (shareMaster) {
				//解锁
				dao.updateShardingTakeOver(id, getDefaultSharingFlagTo(), DEFAULT_SHARING_FLAG);
			}
		}

		return null;
	}
}
