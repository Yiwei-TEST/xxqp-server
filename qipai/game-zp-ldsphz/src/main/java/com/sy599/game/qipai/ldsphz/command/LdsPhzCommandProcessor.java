package com.sy599.game.qipai.ldsphz.command;

import java.util.HashMap;
import java.util.Map;

import com.sy599.game.character.Player;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.qipai.AbstractBaseCommandProcessor;
import com.sy599.game.qipai.ldsphz.command.com.PaohuziComCommand;
import com.sy599.game.qipai.ldsphz.command.play.LdsPhzPlayCommand;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.ObjectUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

public class LdsPhzCommandProcessor extends AbstractBaseCommandProcessor {
	private static LdsPhzCommandProcessor processor = new LdsPhzCommandProcessor();
	private static Map<Short, Class<? extends BaseCommand>> commandMap = new HashMap<>();
	private static Map<Class<?>, Short> msgClassToMsgTypeMap = new HashMap<Class<?>, Short>();

	static {
		commandMap.put(WebSocketMsgType.cs_com, PaohuziComCommand.class);
		commandMap.put(WebSocketMsgType.cs_play, LdsPhzPlayCommand.class);

	}

	public static LdsPhzCommandProcessor getInstance() {
		return processor;
	}

	@Override
	public void process(Player player, MessageUnit message) {
		try {
			BaseCommand action = ObjectUtil.newInstance(commandMap.get(message.getMsgType()));
			action.setPlayer(player);
			action.execute(player, message);
		} catch (Exception e) {
            StringBuilder sb = new StringBuilder("CommandProcessor|LdsPhz|error");
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

	public short getMsgType(Class<?> clazz) {
		if (msgClassToMsgTypeMap.containsKey(clazz)) {
			return msgClassToMsgTypeMap.get(clazz);
		}
		return 0;
	}

}
