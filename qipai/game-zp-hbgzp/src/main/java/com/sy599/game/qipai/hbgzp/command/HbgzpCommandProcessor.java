package com.sy599.game.qipai.hbgzp.command;

import java.util.HashMap;
import java.util.Map;

import com.sy599.game.character.Player;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.qipai.AbstractBaseCommandProcessor;
import com.sy599.game.qipai.hbgzp.command.com.HbgzpComCommand;
import com.sy599.game.qipai.hbgzp.command.play.HbgzpPlayCommand;
import com.sy599.game.qipai.hbgzp.bean.HbgzpPlayer;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

public class HbgzpCommandProcessor extends AbstractBaseCommandProcessor {
    private static HbgzpCommandProcessor processor = new HbgzpCommandProcessor();
    private static Map<Short, Class<? extends BaseCommand>> commandMap = new HashMap<>();
    private static Map<Class<?>, Short> msgClassToMsgTypeMap = new HashMap<Class<?>, Short>();

    static {
        commandMap.put(WebSocketMsgType.cs_com, HbgzpComCommand.class);
        commandMap.put(WebSocketMsgType.cs_play, HbgzpPlayCommand.class);

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

    public static HbgzpCommandProcessor getInstance() {
        return processor;
    }

    @Override
    public void process(Player player, MessageUnit message) {
        int code = 0;
        try {
            HbgzpPlayer _player = player.getPlayer(processor);
            BaseCommand action = commandMap.get((short) message.getMsgType()).newInstance();
            action.setPlayer(player);
            action.execute(_player, message);
        } catch (Exception e) {
            LogUtil.e("socket err: " + player.getUserId() + " " + message.getMsgType(), e);
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

}
