package com.sy.sanguo.game.pdkuai.game;

import org.apache.commons.lang.StringUtils;

import com.sy.sanguo.common.util.JacksonUtil;
import com.sy.sanguo.game.msg.MonitorMsg;
import com.sy.sanguo.game.pdkuai.action.BaseAction;
import com.sy.sanguo.game.service.SysInfManager;

/**
 * 封神榜
 * 
 * @author wujun
 * 
 */
public class MonitorAction extends BaseAction {

	@Override
	public String execute() throws Exception {

		int functype = this.getInt("funcType");
		switch (functype) {
		case 1:
			refreshMonitor();
			break;
		default:
			break;
		}
		return result;
	}

	private void refreshMonitor() {
		String monitor = this.getString("monitor");
		if (!StringUtils.isBlank(monitor)) {
			MonitorMsg msg = JacksonUtil.readValue(monitor, MonitorMsg.class);
			SysInfManager.getInstance().refreshMonitor(msg);
		}
		writeMsg(0, null);
	}
}
