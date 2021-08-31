package com.sy.sanguo.game.pdkuai.util;

import com.sy.sanguo.common.log.GameBackLogger;

public class LogUtil {
//	/** 用于记录游戏业务逻辑的日志 **/
//	public static Logger msgLog = Logger.getLogger("msg");
//	/** 用于记录数据库操作的日志 **/
//	public static Logger dbLog = Logger.getLogger("db");
//	/** 用于记录监控性能的日志 **/
//	public static Logger monitorLog = Logger.getLogger("monitor");
//	/** 用于记录监控性能的日志 **/
//	public static Logger errorLog = Logger.getLogger("error");
	
	public static void e(Object message, Throwable t) {
		// errorLog.error(message, t);
		GameBackLogger.SYS_LOG.error(message, t);
	}

	public static void e(String message) {
		// errorLog.error(message, t);
		GameBackLogger.SYS_LOG.error(message);
	}

	public static void i(String message) {
		// errorLog.error(message, t);
		GameBackLogger.SYS_LOG.info(message);
	}
}
