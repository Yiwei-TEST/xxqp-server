package com.sy599.game.db.dao;

import com.sy599.game.db.bean.RedBagInfo;
import com.sy599.game.util.LogUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class RedBagInfoDao extends BaseDao {

	private static RedBagInfoDao _inst = new RedBagInfoDao();

	public static RedBagInfoDao getInstance() {
		return _inst;
	}

	public List<RedBagInfo> getUserRedBagInfos(long userId) {
		try {
			return (List<RedBagInfo>)getSqlLoginClient().queryForList("redbagInfo.getUserRedBagInfos", userId);
		}catch (Exception e) {
			LogUtil.errorLog.error("redbagInfo.getUserRedBagInfos Exception:" + e.getMessage(), e);
		}
		return null;
	}

	public List<RedBagInfo> getUserRedBagInfosBySourceType(long userId, int sourceType) {
		try {
			HashMap<String, Object> paramMap = new HashMap<>();
			paramMap.put("userId", userId);
			paramMap.put("sourceType", sourceType);
			return (List<RedBagInfo>)getSqlLoginClient().queryForList("redbagInfo.getUserRedBagInfosBySourceType", paramMap);
		}catch (Exception e) {
			LogUtil.errorLog.error("redbagInfo.getUserRedBagInfosBySourceType Exception:" + e.getMessage(), e);
		}
		return null;
	}

	public int getRedbagNum(float redbag, Date startDate, Date endDate) {
		try {
			HashMap<String, Object> paramMap = new HashMap<>();
			paramMap.put("redbag", redbag);
			paramMap.put("startDate", startDate);
			paramMap.put("endDate", endDate);
			return (int)getSqlLoginClient().queryForObject("redbagInfo.getRedbagNum", paramMap);
		}catch (Exception e) {
			LogUtil.errorLog.error("redbagInfo.getRedbagNum Exception:" + e.getMessage(), e);
		}
		return 0;
	}

	public int getBiggerRedbagNum(Date startDate, Date endDate) {
		try {
			HashMap<String, Object> paramMap = new HashMap<>();
			paramMap.put("startDate", startDate);
			paramMap.put("endDate", endDate);
			return (int)getSqlLoginClient().queryForObject("redbagInfo.getBiggerRedbagNum", paramMap);
		}catch (Exception e) {
			LogUtil.errorLog.error("redbagInfo.getBiggerRedbagNum Exception:" + e.getMessage(), e);
		}
		return 0;
	}

	public void saveRedBagInfo(RedBagInfo redBagInfo) {
		try {
			getSqlLoginClient().update("redbagInfo.saveRedBagInfo", redBagInfo);
		} catch (Exception e) {
			LogUtil.e("redbagInfo.saveRedBagInfo err", e);
		}
	}
}
