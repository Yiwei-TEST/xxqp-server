package com.sy599.game.db.dao.group;

import com.alibaba.fastjson.JSON;
import com.sy599.game.db.bean.group.GroupWarn;
import com.sy599.game.db.dao.BaseDao;
import com.sy599.game.util.LogUtil;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupWarnDao extends BaseDao {
	private static GroupWarnDao groupWarnDao = new GroupWarnDao();

	public static GroupWarnDao getInstance() {
		return groupWarnDao;
	}



	public List<Map<String, Object>> selectGroupWarn(long groupId, int promoterLevel, long promoterId, String keyWord, int pageNo, int pageSize) {
		Map<String, Object> map = new HashMap<>(8);
		map.put("groupId", groupId);
		map.put("promoterId", promoterId);
		map.put("andSql", " AND promoterId" + promoterLevel + " = " + promoterId);
		map.put("groupByKey", " promoterId" + (promoterLevel + 1));
		if (StringUtils.isNotEmpty(keyWord)) {
			map.put("targetUserId", keyWord);
		}
		map.put("startNo", (pageNo - 1) * pageSize);
		map.put("pageSize", pageSize);
		try {
			return ( List<Map<String, Object>>)this.getSqlLoginClient().queryForList("groupWarn.groupWarnList", map);
		} catch (Exception e) {
			LogUtil.dbLog.error("groupWarn.groupWarnList:", e);
			return null;
		}
	}

	public List<GroupWarn> getGroupWarnByUserIdAndGroupId(long userId, long groupId)  {

		Map<String, Object> map = new HashMap<>();
		map.put("userId", userId);
		map.put("groupId", groupId);
		try {
			return (List<GroupWarn>) this.getSqlLoginClient().queryForList("groupWarn.getGroupWarnByUserIdAndGroupId", map);
		}catch (Exception e) {
			LogUtil.dbLog.error("groupWarn.getGroupWarnByUserIdAndGroupId：", e);
			return null;
		}

	}

}
