package com.sy599.game.qipai.dtz.tool;

import com.sy599.game.util.LogUtil;

/**
 * 打筒子日志发送
 * @author zhouhj
 *
 */
public class DtzSendLog {
	
	/**
	 * 发送牌局通用日志
	 * @param tableId	房间号
	 * @param playId	玩家id	
	 * @param playName	玩家昵称
	 * @param logType	日志类型
	 * @param param		参数列表
	 */
	public static void sendCardLog(long tableId,long playId,String playName,String logType,String... param){
		StringBuilder info = new StringBuilder();
		info.append(logType+",");
		info.append("房间号="+tableId+",");
		info.append("玩家="+playId+"["+playName+"]"+",");
		info.append("参数={");
		for(String l:param){
			info.append(l+",");
		}
		info.append("}");
		LogUtil.msgLog.info(info.toString());
	}
}
