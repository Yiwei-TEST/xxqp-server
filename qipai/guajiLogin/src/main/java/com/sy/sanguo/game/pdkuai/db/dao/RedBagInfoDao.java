package com.sy.sanguo.game.pdkuai.db.dao;

import com.sy.sanguo.game.bean.redbag.RedBagInfo;
import com.sy.sanguo.game.pdkuai.util.LogUtil;
import java.util.List;

public class RedBagInfoDao extends BaseDao {
	
	private static RedBagInfoDao _inst = new RedBagInfoDao();

	public static RedBagInfoDao getInstance() {
		return _inst;
	}

	public List<RedBagInfo> getUserRedBagInfos(long userId) {
		try {
			return (List<RedBagInfo>)getSql().queryForList("redbagInfo.getUserRedBagInfos", userId);
		}catch (Exception e) {
			LogUtil.e("redbagInfo.getUserRedBagInfos Exception:" + e.getMessage(), e);
		}
		return null;
	}

	public float getTodayUserRedBagNum() {
		try {
			return (float)getSql().queryForObject("redbagInfo.getTodayUserRedBagNum");
		}catch (Exception e) {
			LogUtil.e("redbagInfo.getTodayUserRedBagNum Exception:" + e.getMessage(), e);
		}
		return 0.0f;
	}

	public void saveRedBagInfo(RedBagInfo redBagInfo) {
		try {
			getSql().update("redbagInfo.saveRedBagInfo", redBagInfo);
		} catch (Exception e) {
			LogUtil.e("redbagInfo.saveRedBagInfo err", e);
		}
	}
}
