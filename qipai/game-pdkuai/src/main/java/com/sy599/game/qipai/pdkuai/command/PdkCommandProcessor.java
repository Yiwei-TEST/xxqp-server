package com.sy599.game.qipai.pdkuai.command;

import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.qipai.AbstractCommandProcessor;
import com.sy599.game.util.ObjectUtil;
import com.sy599.game.qipai.pdkuai.bean.PdkPlayer;
import com.sy599.game.qipai.pdkuai.command.play.CutCardCommand;
import com.sy599.game.qipai.pdkuai.command.play.PlayCommand;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

import java.util.HashMap;
import java.util.Map;

public class PdkCommandProcessor extends AbstractCommandProcessor<PdkPlayer> {
	private static PdkCommandProcessor processor = new PdkCommandProcessor();
	private static Map<Short, Class<? extends BaseCommand<PdkPlayer>>> commandMap = new HashMap<Short, Class<? extends BaseCommand<PdkPlayer>>>();
	private static Map<Class<?>, Short> msgClassToMsgTypeMap = new HashMap<Class<?>, Short>();

	static {
		// commandMap.put(WebSocketMsgType.cs_com, ComCommand.class);
		commandMap.put(WebSocketMsgType.cs_play, PlayCommand.class);
		commandMap.put(WebSocketMsgType.cs_com, CutCardCommand.class);
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

	public static PdkCommandProcessor getInstance() {
		return processor;
	}


	public short getMsgType(Class<?> clazz) {
		if (msgClassToMsgTypeMap.containsKey(clazz)) {
			return msgClassToMsgTypeMap.get(clazz);
		}
		return 0;
	}

	@Override
	public Map<Short, Class<? extends BaseCommand<PdkPlayer>>> loadCommands() {
		return commandMap;
	}
}
