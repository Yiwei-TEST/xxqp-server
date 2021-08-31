package com.sy599.game.manager;

import com.sy599.game.GameServerConfig;
import com.sy599.game.msg.MonitorMsg;
import com.sy599.game.util.MemoryUtil;
import com.sy599.game.websocket.WebSocketManager;

public class MonitorManger {
	private static MonitorManger _inst = new MonitorManger();

	public static MonitorManger getInst() {
		return _inst;
	}

	/**
	 * 监控Msg
	 * 
	 * @return
	 */
	public MonitorMsg buildMonitorMsg() {
		int count = PlayerManager.getInstance().getPlayerCount();
		int tableCount = TableManager.getInstance().getTableCount();
		int onlineCount = WebSocketManager.webSocketMap.size();
		int freeMem = MemoryUtil.getFreeMemoryMB();
		int totalMem = MemoryUtil.getTotalMemoryMB();
		int maxMem = MemoryUtil.getMaxMemoryMB();

		MonitorMsg msg = new MonitorMsg();
		msg.setServerId(GameServerConfig.SERVER_ID);
		msg.setCount(count);
		msg.setTableCount(tableCount);
		msg.setOnlineCount(onlineCount);
		msg.setFreeMem(freeMem);
		msg.setMaxMem(maxMem);
		msg.setTotalMem(totalMem);
		return msg;
	}

//	public void sendMonitor() {
//		new Thread(new monitorTask()).start();
//	}
//
//	class monitorTask implements Runnable {
//		@Override
//		public void run() {
//			Map<String, String> params = new HashMap<>();
//			params.put("monitor", JacksonUtil.writeValueAsString(buildMonitorMsg()));
//			LoginHelper.postToLogin(5, 1, params);
//		}
//
//	}
}
