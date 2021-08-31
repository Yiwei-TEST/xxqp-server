package com.sy599.game.qipai.yjmj.command;

import com.sy599.game.character.Player;
import com.sy599.game.util.ObjectUtil;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import com.sy599.game.qipai.AbstractBaseCommandProcessor;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.qipai.yjmj.bean.YjMjPlayer;
import com.sy599.game.qipai.yjmj.command.com.YjMjComCommand;
import com.sy599.game.qipai.yjmj.command.play.YjMjPlayCommand;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

import java.util.HashMap;
import java.util.Map;

public class YjMjCommandProcessor extends AbstractBaseCommandProcessor {
    private static YjMjCommandProcessor processor = new YjMjCommandProcessor();
    private static Map<Short, Class<? extends BaseCommand>> commandMap = new HashMap<Short, Class<? extends BaseCommand>>();
    private static Map<Class<?>, Short> msgClassToMsgTypeMap = new HashMap<Class<?>, Short>();

    static {
        commandMap.put(WebSocketMsgType.cs_com, YjMjComCommand.class);
        commandMap.put(WebSocketMsgType.cs_play, YjMjPlayCommand.class);
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

    public static YjMjCommandProcessor getInstance() {
        return processor;
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
            if (player == null || !(player instanceof YjMjPlayer)) {
                return;
            }
            YjMjPlayer yjMjPlayer = (YjMjPlayer) player;
            BaseCommand action = ObjectUtil.newInstance(commandMap.get(message.getMsgType()));
            action.setPlayer(player);
            action.execute(yjMjPlayer, message);
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder("CommandProcessor|YjMj|error");
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
