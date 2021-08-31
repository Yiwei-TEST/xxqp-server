package com.sy.sanguo.game.pdkuai.db.dao;
import com.sy.sanguo.game.bean.MGauthorization;
import com.sy.sanguo.game.bean.redbag.UserRedBagRecord;
import com.sy.sanguo.game.pdkuai.util.LogUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MGauthorizationDao extends BaseDao {
	
	private static MGauthorizationDao _inst = new MGauthorizationDao();

	public static MGauthorizationDao getInstance() {
		return _inst;
	}

	public MGauthorization getMGauthorization(String unionId) {
		try {
			Map<String, String> sqlMap = new HashMap<String, String>();
			sqlMap.put("unionId", unionId);
			return (MGauthorization)getSql().queryForObject("mgauthorization.getMGauthorization", sqlMap);
		}catch (Exception e) {
			LogUtil.e("mgauthorization.getMGauthorization Exception:" + e.getMessage(), e);
		}
		return null;
	}
}
