package com.sy599.game.qipai.sypaohuzi.command;

import com.sy599.game.character.Player;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.util.ObjectUtil;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import com.sy599.game.qipai.AbstractBaseCommandProcessor;
import com.sy599.game.qipai.sypaohuzi.bean.SyPaohuziPlayer;
import com.sy599.game.qipai.sypaohuzi.command.com.PaohuziComCommand;
import com.sy599.game.qipai.sypaohuzi.command.play.PlayCommand;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

import java.util.HashMap;
import java.util.Map;

public class PaohuziCommandProcessor extends AbstractBaseCommandProcessor {
	private static PaohuziCommandProcessor processor = new PaohuziCommandProcessor();
	private static Map<Short, Class<? extends BaseCommand>> commandMap = new HashMap<Short, Class<? extends BaseCommand>>();
	private static Map<Class<?>, Short> msgClassToMsgTypeMap = new HashMap<Class<?>, Short>();

	static {
		commandMap.put(WebSocketMsgType.cs_com, PaohuziComCommand.class);
		commandMap.put(WebSocketMsgType.cs_play, PlayCommand.class);

	}

	public static PaohuziCommandProcessor getInstance() {
		return processor;
	}

	public void process(SyPaohuziPlayer player, MessageUnit message) {
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
			SyPaohuziPlayer pdkPlayer = player.getPlayer(processor);
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
