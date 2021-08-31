package com.sy.sanguo.game.pdkuai.db.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.sy.sanguo.game.pdkuai.db.bean.UserMessage;
import com.sy.sanguo.game.pdkuai.util.LogUtil;

public class UserMessageDao extends BaseDao {
	private static UserMessageDao _inst = new UserMessageDao();

	public static UserMessageDao getInstance() {
		return _inst;
	}

	public long saveUserMessage(UserMessage info) {
		try {
			return (Long) getSql().insert("message.saveUserMessage", info);
		} catch (SQLException e) {
			LogUtil.e("#message.saveUserMessage:" + info.getUserId(), e);
		}
		return 0;
	}

	public void delete(long id) {
		try {
			getSql().delete("message.delete", id);
		} catch (SQLException e) {
			LogUtil.e("#message.delete:" + id, e);
		}
	}

	public int deleteMessageByDay() {
		try {
			int deleate = getSql().delete("message.deleteMessageByDay");
			return deleate;
		} catch (SQLException e) {
			LogUtil.e("#message.deleteMessageByDay:", e);
		}
		return 0;
	}

	public void deleteByDate(long userId) {
		try {
			getSql().delete("message.deleteByDate", userId);
		} catch (SQLException e) {
			LogUtil.e("#message.deleteByDate:" + userId, e);
		}
	}

	@SuppressWarnings("unchecked")
	public List<UserMessage> selectUserMessage(long userId) {
		List<UserMessage> result = new ArrayList<UserMessage>();
		try {
			result = (List<UserMessage>) getSql().queryForList("message.selectUserMessage", userId);
		} catch (SQLException e) {
			LogUtil.e("#message.selectUserMessage:", e);
		}
		return result;
	}
}
