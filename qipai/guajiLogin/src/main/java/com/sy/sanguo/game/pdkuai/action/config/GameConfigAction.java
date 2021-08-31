package com.sy.sanguo.game.pdkuai.action.config;

import java.util.HashMap;
import java.util.Map;

import com.sy.sanguo.game.pdkuai.action.BaseAction;
public class GameConfigAction extends BaseAction {

	@Override
	public String execute() {
		int funcType = this.getInt("funcType");
		switch (funcType) {
		case 1:
			config();
			break;

		default:
			break;
		}
		return result;
	}

	private void config() {
		long userId = this.getLong("userId");
		String config = this.getString("config");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("config", config);
		paramMap.put("userId", userId);
		int update = gameUserDao.save(paramMap);
		int code = 0;
		if (update == 0) {
			code = 1;
		}
		writeMsg(code, "");
	}
}
