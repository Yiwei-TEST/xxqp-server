package com.sy.sanguo.game.pdkuai.db.dao;

import com.sy.sanguo.game.pdkuai.util.LogUtil;
import com.sy.sanguo.game.bean.redbag.RedBagSystemInfo;

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
			return (RedBagSystemInfo)getSql().queryForObject("redBagSystem.getRedBagSystemInfo");
		}catch (Exception e) {
			LogUtil.e("redBagSystem.getRedBagSystemInfo Exception:" + e.getMessage(), e);
		}
		return null;
	}

	public void saveRedBagSystemInfo(RedBagSystemInfo redBagSystemInfo) {
		try {
			getSql().update("redBagSystem.saveRedBagSystemInfo", redBagSystemInfo);
		} catch (Exception e) {
			LogUtil.e("redBagSystem.saveRedBagSystemInfo err", e);
		}
	}

	public void updateRedBagSystemInfo(RedBagSystemInfo redBagSystemInfo) {
		try {
			getSql().update("redBagSystem.updateRedBagSystemInfo", redBagSystemInfo);
		} catch (Exception e) {
			LogUtil.e("redBagSystem.updateRedBagSystemInfo err", e);
		}
	}
}
