package com.sy.sanguo.game.pdkuai.game;

//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import com.sy.sanguo.game.bean.Server;
//import com.sy.sanguo.game.msg.MonitorMsg;
import com.sy.sanguo.game.pdkuai.action.BaseAction;
//import com.sy.sanguo.game.service.SysInfManager;

public class ToServerAction extends BaseAction {

	@Override
	public String execute() throws Exception {
		
		int funcType = getInt("funcType");
		switch (funcType) {
		case 1:
//			getServerData();
			break;
		case 2:
//			getMonitorMapData();
			break;
		case 3:
//			getSpareIpMapData();
			break;
		default:
			break;
		}
		
		return result;
	}
		
//	private void getServerData() {
//
//		Map<String, Object> result = new HashMap<String, Object>();
//
//		List<Server> servers = SysInfManager.getInstance().getServers();
//		if (servers != null) {
//			result.put("info", servers);
//			this.writeMsg(0, result);
//		} else {
//			result.put("info", "error");
//			this.writeMsg(-1, result);
//		}
//
//	}
	
//	private void getMonitorMapData() {
//
//		Map<String, Object> result = new HashMap<String, Object>();
//
//		Map<Integer, MonitorMsg> monitorMap = SysInfManager.getInstance().getMonitorMap();
//		if (monitorMap != null) {
//			result.put("info", monitorMap);
//			this.writeMsg(0, result);
//		} else {
//			result.put("info", "error");
//			this.writeMsg(-1, result);
//		}
//	}
	
//	private void getSpareIpMapData() {
//
//		Map<String, Object> result = new HashMap<String, Object>();
//		Map<Integer, String> spareIpMap = SysInfManager.getInstance().getSpareIpMap();
//
//		if (spareIpMap != null) {
//			result.put("info", spareIpMap);
//			this.writeMsg(0, result);
//		} else {
//			result.put("info", "error");
//			this.writeMsg(-1, result);
//		}
//	}

}
