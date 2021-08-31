package com.sy599.game.db.dao;

import com.sy599.game.db.bean.Authority;
import com.sy599.game.db.bean.UserSign;
import com.sy599.game.util.LogUtil;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuthorityDao extends BaseDao {
	private static AuthorityDao _inst = new AuthorityDao();

	public static AuthorityDao getInstance() {
		return _inst;
	}

	public List<Authority> getAuthorityByQxId(long userId, int quanxianId,int indexBegin) {
		try {
			Map<String, Object> map = new HashMap<>();
			map.put("quanxianId", quanxianId);
			map.put("indexBegin", indexBegin);
			if(userId > 0){
				map.put("userId", userId);
				return (List<Authority>) this.getSqlLoginClient().queryForList("authority.getAuthorityByQxIdUserId", map);
			}else{
				return (List<Authority>) this.getSqlLoginClient().queryForList("authority.getAuthorityByQxId", map);
			}
		} catch (SQLException e) {
			LogUtil.e("getAuthorityByQxId err", e);
		}
		return null;
	}

	public void insertAuthority(Authority authority) {
		try {
			this.getSqlLoginClient().insert("authority.insertAuthority", authority);
		} catch (SQLException e) {
			LogUtil.e("insertAuthority err", e);
		}
	}

	public void deleteAuthority(long userId, int quanxianId) {
		try {
			Map<String, Object> map = new HashMap<>();
			map.put("quanxianId", quanxianId);
			map.put("userId", userId);
			this.getSqlLoginClient().delete("authority.deleteAuthority", map);
		} catch (SQLException e) {
			LogUtil.e("deleteAuthority err", e);
		}
	}

}
