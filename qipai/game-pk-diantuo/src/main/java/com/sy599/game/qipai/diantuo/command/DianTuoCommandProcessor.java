package com.sy599.game.qipai.diantuo.command;

import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.qipai.AbstractCommandProcessor;
import com.sy599.game.qipai.diantuo.bean.DianTuoPlayer;
import com.sy599.game.qipai.diantuo.command.play.PlayCommand;
import com.sy599.game.qipai.diantuo.command.play.DianTuoComCommand;
import com.sy599.game.util.ObjectUtil;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

import java.util.HashMap;
import java.util.Map;

public class DianTuoCommandProcessor extends AbstractCommandProcessor<DianTuoPlayer> {
	private static DianTuoCommandProcessor processor = new DianTuoCommandProcessor();
	private static Map<Short, Class<? extends BaseCommand<DianTuoPlayer>>> commandMap = new HashMap<Short, Class<? extends BaseCommand<DianTuoPlayer>>>();
	private static Map<Class<?>, Short> msgClassToMsgTypeMap = new HashMap<Class<?>, Short>();

	static {
		// commandMap.put(WebSocketMsgType.cs_com, DianTuoComCommand.class);
		commandMap.put(WebSocketMsgType.cs_play, PlayCommand.class);
		commandMap.put(WebSocketMsgType.cs_com, DianTuoComCommand.class);
//		commandMap.put(WebSocketMsgType.cs_com, PdkComCommand.class);

		try {
			for (Short type : commandMap.keySet()) {
				Class<? extends BaseCommand> cl = commandMap.get(type);
				BaseCommand action = ObjectUtil.newInstance(cl);
				Map<Class<?>, Short> msgTypeMap = action.getMsgTypeMap();
				if (msgTypeMap != null && !msgTypeMap.isEmpty()) {
					for (Class<?> msgClass : msgTypeMap.keySet()) {
						if (msgClassToMsgTypeMap.containsKey(msgClass)) {
							throw new Exception("msgClassToMsgTypeMap err!!!!");

						} else {
							msgClassToMsgTypeMap.put(msgClass, msgTypeMap.get(msgClass));

						}
					}
				}
			}

		} catch (Exception e) {
			LogUtil.e("SocketAcitonProcessor err:", e);

		}

	}

	public static DianTuoCommandProcessor getInstance() {
		return processor;
	}


	public short getMsgType(Class<?> clazz) {
		if (msgClassToMsgTypeMap.containsKey(clazz)) {
			return msgClassToMsgTypeMap.get(clazz);
		}
		return 0;
	}

	@Override
	public Map<Short, Class<? extends BaseCommand<DianTuoPlayer>>> loadCommands() {
		return commandMap;
	}
}
