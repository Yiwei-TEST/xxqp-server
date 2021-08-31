package com.sy599.game.db.dao;

import com.sy599.game.db.bean.UserMessage;
import com.sy599.game.util.LogUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserMessageDao extends BaseDao {
	private static UserMessageDao _inst = new UserMessageDao();

	public static UserMessageDao getInstance() {
		return _inst;
	}

	public long saveUserMessage(UserMessage info) {
		try {
			return (Long) getSqlLoginClient().insert("message.saveUserMessage", info);
		} catch (SQLException e) {
			LogUtil.dbLog.error("#message.saveUserMessage:" + info.getUserId(), e);
		}
		return 0;
	}

	public void delete(long id) {
		try {
			getSqlLoginClient().delete("message.delete", id);
		} catch (SQLException e) {
			LogUtil.dbLog.error("#message.delete:" + id, e);
		}
	}

	@SuppressWarnings("unchecked")
	public List<UserMessage> selectUserMessage(long userId) {
//		Map<String, Object> map = new HashMap<String, Object>();
//		map.put("userId", userId);
		List<UserMessage> result = new ArrayList<UserMessage>();
		try {
			result = (List<UserMessage>) getSqlLoginClient().queryForList("message.selectUserMessage", userId);
		} catch (SQLException e) {
			LogUtil.dbLog.error("#message.selectUserMessage:", e);
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public List<UserMessage> selectUserMessageByType(int type, int limitNum) {
		List<UserMessage> result = new ArrayList<UserMessage>();
		try {
			result = (List<UserMessage>) getSqlLoginClient().queryForList("message.selectUserMessageByType", type, limitNum);
		} catch (SQLException e) {
			LogUtil.dbLog.error("#message.selectUserMessage:", e);
		}
		return result;
	}
}
