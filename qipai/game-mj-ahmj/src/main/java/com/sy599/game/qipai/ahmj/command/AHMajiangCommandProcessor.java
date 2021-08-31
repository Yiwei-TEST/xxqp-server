package com.sy599.game.qipai.ahmj.command;

import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.qipai.AbstractCommandProcessor;
import com.sy599.game.qipai.ahmj.bean.AhmjPlayer;
import com.sy599.game.qipai.ahmj.command.com.AHMajiangComCommand;
import com.sy599.game.qipai.ahmj.command.play.PlayCommand;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.ObjectUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

import java.util.HashMap;
import java.util.Map;

public class AHMajiangCommandProcessor extends AbstractCommandProcessor<AhmjPlayer> {
	private static AHMajiangCommandProcessor processor = new AHMajiangCommandProcessor();
	private static Map<Short, Class<? extends BaseCommand<AhmjPlayer>>> commandMap = new HashMap<>();
	private static Map<Class<?>, Short> msgClassToMsgTypeMap = new HashMap<Class<?>, Short>();

	static {
		commandMap.put(WebSocketMsgType.cs_com, AHMajiangComCommand.class);
		commandMap.put(WebSocketMsgType.cs_play, PlayCommand.class);

		try {
			for (Short type : commandMap.keySet()) {
				Class<? extends BaseCommand<AhmjPlayer>> cl = commandMap.get(type);
				BaseCommand<AhmjPlayer> action = ObjectUtil.newInstance(cl);
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

			// msgClassToMsgTypeMap.putAll(LoginSocketAction.class.newInstance().getMsgTypeMap());
		} catch (Exception e) {
			LogUtil.e("SocketAcitonProcessor err:", e);

		}

	}

	public static AHMajiangCommandProcessor getInstance() {
		return processor;
	}

	@Override
	public Map<Short, Class<? extends BaseCommand<AhmjPlayer>>> loadCommands() {
		return commandMap;
	}

	public short getMsgType(Class<?> clazz) {
		if (msgClassToMsgTypeMap.containsKey(clazz)) {
			return msgClassToMsgTypeMap.get(clazz);
		}
		return 0;
	}

}
