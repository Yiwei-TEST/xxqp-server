package com.sy599.game.udplog;

import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LogConstants;


public interface IPlayerLogger {
	
	/**连接到服务器*/
	void connectToServer();
	
	/**判断是否连接正常*/
	boolean isConnected();
	
	/**发送行为日志*/
    void sendActionLog(Player player, LogConstants reason/**具体操作*/, String consumeParam/**消耗参数*/);
	
	/**
	 * 发送玩家快照
	 * @param player
	 * @param sign
	 * @throws
	 */
	void sendSnapshotLog(Player player, int sign);
}
