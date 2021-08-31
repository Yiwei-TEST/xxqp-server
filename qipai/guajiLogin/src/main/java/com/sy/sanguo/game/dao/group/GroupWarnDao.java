package com.sy.sanguo.game.dao.group;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.sy.sanguo.common.parent.impl.CommonDaoImpl;
import com.sy.sanguo.game.bean.Activity;
import com.sy.sanguo.game.bean.group.GroupWarn;
import com.sy.sanguo.game.pdkuai.db.dao.BaseDao;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupWarnDao extends CommonDaoImpl {
	private static GroupWarnDao _inst = new GroupWarnDao();

	public static GroupWarnDao getInstance() {
		return _inst;
	}



	public List<Map<String, Object>> selectGroupWarn(long groupId, int promoterLevel, long promoterId, String keyWord, int pageNo, int pageSize) throws SQLException {
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
		return ( List<Map<String, Object>>)this.getSqlMapClient().queryForList("groupWarn.groupWarnList", map);
	}

	public List<GroupWarn> getGroupWarnByUserIdAndGroupId(long userId, int groupId) throws Exception   {
		Map<String, Object> map = new HashMap<>();
		map.put("userId", userId);
		map.put("groupId", groupId);
		return (List<GroupWarn>) this.getSqlMapClient().queryForList("groupWarn.getGroupWarnByUserIdAndGroupId", map);
	}


	public void insertGroupWarn(GroupWarn groupWarn) throws Exception{
		this.getSqlMapClient().insert("groupWarn.insertGroupWarn", groupWarn);
	}

	public void deleteGroupWarn(int groupId,long userId) throws Exception  {
		Map<String, Object> map = new HashMap<>();
		map.put("groupId", groupId);
		map.put("userId", userId);
		this.getSqlMapClient().delete("groupWarn.deleteGroupWarn", map);
	}

	public void updateGroupWarn(int groupId,long userId, int warnScore,int warnSwitch) throws Exception  {
		Map<String, Object> map = new HashMap<>();
		map.put("groupId", groupId);
		map.put("userId", userId);
		map.put("warnScore", warnScore);
		map.put("warnSwitch", warnSwitch);
		map.put("updateTime", new Date());
		this.getSqlMapClient().update("groupWarn.updateGroupWarn", map);
	}

}
