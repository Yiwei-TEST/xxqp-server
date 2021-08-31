package com.sy599.game.qipai.dtz.command;

import com.sy599.game.character.Player;
import com.sy599.game.util.ObjectUtil;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import com.sy599.game.qipai.AbstractBaseCommandProcessor;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.qipai.dtz.bean.DtzPlayer;
import com.sy599.game.qipai.dtz.command.com.ComCommand;
import com.sy599.game.qipai.dtz.command.play.PlayCommand;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

import java.util.HashMap;
import java.util.Map;

public class DtzCommandProcessor extends AbstractBaseCommandProcessor {
	private static DtzCommandProcessor processor = new DtzCommandProcessor();
	private static Map<Short, Class<? extends BaseCommand>> commandMap = new HashMap<>();
	private static Map<Class<?>, Short> msgClassToMsgTypeMap = new HashMap<Class<?>, Short>();

	static {
		 commandMap.put(WebSocketMsgType.cs_com, ComCommand.class);
		commandMap.put(WebSocketMsgType.cs_play, PlayCommand.class);
//		commandMap.put(WebSocketMsgType.cs_com, CutCardCommand.class);

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

	public static DtzCommandProcessor getInstance() {
		return processor;
	}

	public void process(DtzPlayer player, MessageUnit message) {}

	public short getMsgType(Class<?> clazz) {
		if (msgClassToMsgTypeMap.containsKey(clazz)) {
			return msgClassToMsgTypeMap.get(clazz);
		}
		return 0;
	}

	@Override
	public void process(Player player, MessageUnit message) {
		try {
			DtzPlayer pdkPlayer = player.getPlayer(processor);
			BaseCommand action = ObjectUtil.newInstance(commandMap.get(message.getMsgType()));
			action.setPlayer(player);
			action.execute(pdkPlayer, message);
		} catch (Exception e) {
            StringBuilder sb = new StringBuilder("CommandProcessor|Dtz|error");
            if (player != null) {
                sb.append("|").append(player.getUserId());
                sb.append("|").append(player.getSeat());
                sb.append("|").append(player.getPlayingTableId());
            }
            if (message != null) {
                sb.append("|").append(message.getMsgType());
                sb.append("|").append(LogUtil.printlnLog(message.getMessage()));
            }
            sb.append("|").append(e.getMessage());
            LogUtil.errorLog.error(sb.toString(), e);
		}
	}

}
