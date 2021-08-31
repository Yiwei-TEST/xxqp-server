package com.sy599.game.util;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.GeneratedMessage;
import com.sy599.game.GameServerConfig;
import com.sy599.game.msg.serverPacket.TableMjResMsg.ClosingMjInfoRes;
import com.sy599.game.msg.serverPacket.TableMjResMsg.ClosingMjPlayerInfoRes;
import com.sy599.game.msg.serverPacket.TablePhzResMsg;
import com.sy599.game.msg.serverPacket.TablePhzResMsg.ClosingPhzInfoRes;
import com.sy599.game.msg.serverPacket.TablePhzResMsg.ClosingPhzPlayerInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.ClosingInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.ClosingPlayerInfoRes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author taohuiliang
 * @date 2013-3-19
 * @version v1.0
 */
public class LogUtil {

	/** 用于记录聊天的日志 **/
	public static Logger chat = LoggerFactory.getLogger("chat");
	/** 用于记录游戏业务逻辑的日志 **/
	public static Logger msgLog = LoggerFactory.getLogger("msg");
	/** 用于记录数据库操作的日志 **/
	public static Logger dbLog = LoggerFactory.getLogger("db");
	/** 用于记录监控性能的日志 **/
	public static Logger monitorLog = LoggerFactory.getLogger("monitor");
	/** 用于记录错误日志 **/
	public static Logger errorLog = LoggerFactory.getLogger("error");
    /** 用于记录监控机器人的日志 **/
    public static Logger robot = LoggerFactory.getLogger("robot");

	/** 用于记录异常的日志 **/
	// public static Logger err = Logger.getLogger("msg");
	/** 用于记录战斗日志 **/
	/*
	 * public static Logger batLog = Logger.getLogger("batlog");
	 *//** 用于记录支付业务逻辑的日志 **/
	/*
	 * public static Logger payLog = Logger.getLogger("pay");
	 */

	public static void monitor_i(String info) {
		monitorLog.info(info);
	}

	public static void i(Class<?> c, String info) {
		i(c.getName() + "--> " + info);
	}

	/**
	 * debug 平台sysout日志
	 * 
	 * @param info
	 */
	public static void i(String info) {
		if (GameServerConfig.isDebug()) {
			System.out.println(info);

		}
	}

	public static void msg(String info) {
		LogUtil.msgLog.info(info);
	}

	public static void i(String format, Object... args) {
		System.out.printf(format, args);
	}

	public static void e(String info) {
		LogUtil.errorLog.error(info);
	}

	public static void e(String info, Exception e) {
		LogUtil.errorLog.error(info, e);
	}

	public static void e(Class<?> c, String info, Exception e) {
		errorLog.error(c.getName() + ":" + info, e);
	}

	/**
	 * debug 平台info日志
	 * 
	 * @param info
	 */
	public static void d_msg(String info) {
		if (GameServerConfig.isDebug()) {
			msgLog.info(info);

		}
	}
	
	public static List<String> buildMJClosingInfoResLog(ClosingMjInfoRes res) {
		List<String> list = new ArrayList<>();
		for (ClosingMjPlayerInfoRes info : res.getClosingPlayersList()) {
			Map<String, Object> map = new HashMap<>();
			for (Entry<FieldDescriptor, Object> entry : info.getAllFields().entrySet()) {
				if (entry.getValue() instanceof List) {
					List<Object> l = new ArrayList<>();
					for (Object o : (List) entry.getValue()) {
						if (o instanceof String) {
							l = (List) entry.getValue();
							break;
						} else if (o instanceof Integer) {
							l = (List) entry.getValue();
							break;
						} else if (o instanceof Long) {
							l = (List) entry.getValue();
							break;
						} else if (o instanceof GeneratedMessage) {
							l.add(buildClosingInfoResOtherLog((GeneratedMessage) o));
						}

					}
					map.put(entry.getKey().getName(), l);

				} else {
					map.put(entry.getKey().getName(), entry.getValue());
				}
			}
			list.add(JacksonUtil.writeValueAsString(map));
		}
		return list;
	}

	public static List<String> buildClosingInfoResLog(ClosingInfoRes res) {
		List<String> list = new ArrayList<>();
		for (ClosingPlayerInfoRes info : res.getClosingPlayersList()) {
			Map<String, Object> map = new HashMap<>();
			for (Entry<FieldDescriptor, Object> entry : info.getAllFields().entrySet()) {
				if (entry.getValue() instanceof List) {
					List<Object> l = new ArrayList<>();
					for (Object o : (List) entry.getValue()) {
						if (o instanceof String) {
							l = (List) entry.getValue();
							break;
						} else if (o instanceof Integer) {
							l = (List) entry.getValue();
							break;
						} else if (o instanceof Long) {
							l = (List) entry.getValue();
							break;
						} else if (o instanceof GeneratedMessage) {
							l.add(buildClosingInfoResOtherLog((GeneratedMessage) o));
						}

					}
					map.put(entry.getKey().getName(), l);

				} else {
					map.put(entry.getKey().getName(), entry.getValue());
				}
			}
			list.add(JacksonUtil.writeValueAsString(map));
		}
		return list;
	}

	public static List<String> buildClosingInfoResLog(TablePhzResMsg.ClosingAhPhzInfoRes res) {
		List<String> list = new ArrayList<>();
		for (TablePhzResMsg.ClosingAhPhzPlayerInfoRes info : res.getClosingPlayersList()) {
			Map<String, Object> map = new HashMap<>();
			for (Entry<FieldDescriptor, Object> entry : info.getAllFields().entrySet()) {
				map.put(entry.getKey().getName(), entry.getValue());
			}
			list.add(JacksonUtil.writeValueAsString(map));
		}
		return list;
	}

	public static List<String> buildClosingInfoResLog(ClosingPhzInfoRes res) {
		List<String> list = new ArrayList<>();
		for (ClosingPhzPlayerInfoRes info : res.getClosingPlayersList()) {
			Map<String, Object> map = new HashMap<>();
			for (Entry<FieldDescriptor, Object> entry : info.getAllFields().entrySet()) {
				Object obj = entry.getValue();
				if(entry.getKey().getName().equals("moldCards")){
					if (entry.getValue() instanceof List) {
						List<Object> l = new ArrayList<>();
						for (Object o : (List) entry.getValue()) {
							l.add(buildClosingInfoResOtherLog((GeneratedMessage) o));
						}
						obj = l;
					}

				}
				map.put(entry.getKey().getName(), obj);
			}
			list.add(JacksonUtil.writeValueAsString(map));
		}
		return list;
	}
	
	public static Map<String, Object> buildClosingInfoResOtherLog(GeneratedMessage res) {
		Map<String, Object> map = new HashMap<>();
		for (Entry<FieldDescriptor, Object> entry : res.getAllFields().entrySet()) {
			String name = entry.getKey().getName();
			if (!name.equals("closingPlayers")) {
				if (entry.getValue() instanceof List) {
					List<Object> l = new ArrayList<>();
					for (Object o : (List) entry.getValue()) {
						if (o instanceof String) {
							l = (List) entry.getValue();
							break;
						} else if (o instanceof Integer) {
							l = (List) entry.getValue();
							break;
						} else if (o instanceof Long) {
							l = (List) entry.getValue();
							break;
						} else if (o instanceof GeneratedMessage) {
							l.add(buildClosingInfoResOtherLog((GeneratedMessage) o));
						}

					}
					map.put(entry.getKey().getName(), l);

				} else {
					map.put(entry.getKey().getName(), entry.getValue());
				}
			}
		}
		return map;
	}

	/**
	 * 打印日志
	 * 
	 * @param message
	 * @return
	 */
	public static Map<String, Object> buildLog(GeneratedMessage message) {
		Map<String, Object> map = new HashMap<>();
		for (Entry<FieldDescriptor, Object> entry : message.getAllFields().entrySet()) {
			String name = entry.getKey().getName();
			map.put(name, entry.getValue());
		}
		return map;
	}

	/**
	 * 打印日志
	 * 
	 * @param message
	 * @return
	 */
	public static String printlnLog(GeneratedMessage message) {
		try {
			if (message == null) {
				return null;
			}
			Map<String, Object> map = buildLog(message);
			if (map == null) {
				return null;
			}
			return JacksonUtil.writeValueAsString(map);
		} catch (Exception e) {
			LogUtil.e("printlnLog err:", e);
		}
		return null;
	}

	public static void printDebug(String msg, Object...param) {
		if (msgLog.isDebugEnabled()) {
			msgLog.debug(msg, param);
		}
	}

	public static void main(String[] args) {
		ClosingPlayerInfoRes.Builder info = ClosingPlayerInfoRes.newBuilder();
		info.setBoom(3);
		for (Entry<FieldDescriptor, Object> entry : info.getAllFields().entrySet()) {
			System.out.println(entry.getKey().getName() + "  " + entry.getValue());
		}
	}
}
