package com.sy599.game.db.dao;

import com.sy599.game.db.bean.RedBagSystemInfo;
import com.sy599.game.util.LogUtil;

import java.util.HashMap;
import java.util.Map;

public class RedBagSystemInfoDao extends BaseDao {
	
	private static RedBagSystemInfoDao _inst = new RedBagSystemInfoDao();

	public static RedBagSystemInfoDao getInstance() {
		return _inst;
	}

	public RedBagSystemInfo getRedBagSystemInfo() {
		try {
			Map<String,Object> map = new HashMap<>();
			return (RedBagSystemInfo)getSqlLoginClient().queryForObject("redBagSystem.getRedBagSystemInfo");
		}catch (Exception e) {
			LogUtil.e("redBagSystem.getRedBagSystemInfo Exception:" + e.getMessage(), e);
		}
		return null;
	}

	public void saveRedBagSystemInfo(RedBagSystemInfo redBagSystemInfo) {
		try {
			getSqlLoginClient().update("redBagSystem.saveRedBagSystemInfo", redBagSystemInfo);
		} catch (Exception e) {
			LogUtil.e("redBagSystem.saveRedBagSystemInfo err", e);
		}
	}

	public void updateRedBagSystemInfo(RedBagSystemInfo redBagSystemInfo) {
		try {
			getSqlLoginClient().update("redBagSystem.updateRedBagSystemInfo", redBagSystemInfo);
		} catch (Exception e) {
			LogUtil.e("redBagSystem.updateRedBagSystemInfo err", e);
		}
	}
}
