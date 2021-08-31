package com.sy.sanguo.game.pdkuai.game;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.sy.sanguo.common.util.JsonWrapper;
import com.sy.sanguo.common.util.MD5Util;
import com.sy.sanguo.game.bean.RegInfo;
import com.sy.sanguo.game.bean.enums.CardSourceType;
import com.sy.sanguo.game.pdkuai.action.BaseAction;
import com.sy.sanguo.game.pdkuai.constants.SharedConstants;
import com.sy.sanguo.game.service.pfs.weixin.util.WeixinUtil;
import com.sy599.sanguo.util.TimeUtil;

public class GameAction extends BaseAction {

	@Override
	public String execute() throws Exception {
		switch (this.getInt("funcType")) {
		case 1:
			getDiDiAward();
			break;
		case 2:
			getUser();
			break;
		default:
			break;
		}
		return result;
	}

	public void getUser() throws SQLException {
		String code = this.getString("code");
		RegInfo regInfo = WeixinUtil.getInfoByCode(code);
		if (regInfo == null) {
			writeMsg(1, result);
			return;
		}
		long time = TimeUtil.currentTimeMillis();
		String md5 = MD5Util.getStringMD5(time + "" + regInfo.getUserId() + "7HGO4K61M8N2D9LARSPU");
		Map<String, Object> result = new HashMap<>();
		result.put("headimgurl", regInfo.getHeadimgurl());
		result.put("sytime", time);
		result.put("sysign", md5);
		result.put("name", regInfo.getName());
		result.put("userId", regInfo.getUserId());
		writeMsg(0, result);
	}

	private void getDiDiAward() throws Exception {
		String sytime = this.getString("sytime");
		String sysign = this.getString("sysign");
		long userId = this.getLong("userId", 0);
		String md5 = MD5Util.getStringMD5(sytime + userId + "7HGO4K61M8N2D9LARSPU");
		if (!md5.equals(sysign)) {
			writeMsg(3, result);
			return;
		}

		RegInfo regInfo = userDao.getUser(userId);
		if (regInfo == null) {
			writeMsg(1, null);
			return;
		}

		JsonWrapper wrapper = new JsonWrapper(regInfo.getLoginExtend());
		int getAward = wrapper.getInt(SharedConstants.extend_getaward_dididache, 0);
		if (getAward == 1) {
			writeMsg(2, null);
			return;
		}
		wrapper.putInt(SharedConstants.extend_getaward_dididache, 1);
		Map<String, Object> modify = new HashMap<>();
		modify.put("loginExtend", wrapper.toString());
		wrapper.putInt(SharedConstants.extend_getaward_dididache, 1);
		userDao.addUserCards(regInfo, 0, 3, modify, CardSourceType.activity_dididache);
		writeMsg(0, null);
	}
}
