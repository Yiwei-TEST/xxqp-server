package com.sy599.game.qipai.ldfpf.command;

import com.sy599.game.character.Player;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.qipai.AbstractBaseCommandProcessor;
import com.sy599.game.qipai.ldfpf.been.LdfpfPlayer;
import com.sy599.game.qipai.ldfpf.command.com.PaohuziComCommand;
import com.sy599.game.qipai.ldfpf.command.play.PlayCommand;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.ObjectUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

import java.util.HashMap;
import java.util.Map;

public class PaohuziCommandProcessor extends AbstractBaseCommandProcessor {
	private static PaohuziCommandProcessor processor = new PaohuziCommandProcessor();
	private static Map<Short, Class<? extends BaseCommand>> commandMap = new HashMap<Short, Class<? extends BaseCommand>>();
	private static Map<Class<?>, Short> msgClassToMsgTypeMap = new HashMap<Class<?>, Short>();

	static {
		commandMap.put(WebSocketMsgType.cs_com, PaohuziComCommand.class);
		commandMap.put(WebSocketMsgType.cs_play, PlayCommand.class);

		try {
			for (Short type : commandMap.keySet()) {
				Class<? extends BaseCommand> cl = commandMap.get(type);
				BaseCommand action = cl.newInstance();
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

	public static PaohuziCommandProcessor getInstance() {
		return processor;
	}

	public void process(LdfpfPlayer player, MessageUnit message) {
		int code = 0;
		try {
			BaseCommand action = ObjectUtil.newInstance(commandMap.get(message.getMsgType()));
			action.setPlayer(player);
			action.execute(player, message);
		} catch (Exception e) {
			LogUtil.e("socket err: " + player.getUserId() + " " + message.getMsgType() + " " + LogUtil.printlnLog(message.getMessage()), e);
			code = -1;
		} finally {
			if (code != 0) {
			}
		}
	}

	public short getMsgType(Class<?> clazz) {
		if (msgClassToMsgTypeMap.containsKey(clazz)) {
			return msgClassToMsgTypeMap.get(clazz);
		}
		return 0;
	}

	@Override
	public void process(Player player, MessageUnit message) {
		// TODO Auto-generated method stub
		int code = 0;
		try {
			LdfpfPlayer pdkPlayer = player.getPlayer(processor);
			BaseCommand action = ObjectUtil.newInstance(commandMap.get(message.getMsgType()));
			action.setPlayer(player);
			action.execute(pdkPlayer, message);
		} catch (Exception e) {
			LogUtil.e("socket err: " + player.getUserId() + " " + message.getMsgType() + " " + LogUtil.printlnLog(message.getMessage()), e);
			code = -1;
		} finally {
			if (code != 0) {
			}
		}
	}

}
