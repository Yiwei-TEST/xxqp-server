package com.sy599.game.qipai.yjghz.command;

import com.sy599.game.character.Player;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.qipai.yjghz.bean.YjGhzPlayer;
import com.sy599.game.util.ObjectUtil;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import com.sy599.game.qipai.AbstractBaseCommandProcessor;
import com.sy599.game.qipai.yjghz.command.com.YjGhzComCommand;
import com.sy599.game.qipai.yjghz.command.play.YjGhzPlayCommand;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

import java.util.HashMap;
import java.util.Map;

public class YjGhzCommandProcessor extends AbstractBaseCommandProcessor {
    private static YjGhzCommandProcessor processor = new YjGhzCommandProcessor();
    private static Map<Short, Class<? extends BaseCommand>> commandMap = new HashMap<Short, Class<? extends BaseCommand>>();
    private static Map<Class<?>, Short> msgClassToMsgTypeMap = new HashMap<Class<?>, Short>();

    static {
        commandMap.put(WebSocketMsgType.cs_com, YjGhzComCommand.class);
        commandMap.put(WebSocketMsgType.cs_play, YjGhzPlayCommand.class);
    }

    public void process(YjGhzPlayer player, MessageUnit message) {
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
        try {
            if (player == null || !(player instanceof YjGhzPlayer)) {
                return;
            }
            YjGhzPlayer yjGhzPlayer = (YjGhzPlayer) player;
            BaseCommand action = ObjectUtil.newInstance(commandMap.get(message.getMsgType()));
            action.setPlayer(player);
            action.execute(yjGhzPlayer, message);
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder("CommandProcessor|YjGhz|error");
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

    public static AbstractBaseCommandProcessor getInstance() {
        return processor;
    }

}
