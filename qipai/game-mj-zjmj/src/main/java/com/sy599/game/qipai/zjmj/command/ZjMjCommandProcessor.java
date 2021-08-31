package com.sy599.game.qipai.zjmj.command;

import java.util.HashMap;
import java.util.Map;

import com.sy599.game.character.Player;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.qipai.AbstractBaseCommandProcessor;
import com.sy599.game.qipai.zjmj.bean.ZjMjPlayer;
import com.sy599.game.qipai.zjmj.command.com.ZjMjComCommand;
import com.sy599.game.qipai.zjmj.command.play.ZjMjPlayCommand;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

public class ZjMjCommandProcessor extends AbstractBaseCommandProcessor {
    private static ZjMjCommandProcessor processor = new ZjMjCommandProcessor();
    private static Map<Short, Class<? extends BaseCommand>> commandMap = new HashMap<>();
    private static Map<Class<?>, Short> msgClassToMsgTypeMap = new HashMap<Class<?>, Short>();

    static {
        commandMap.put(WebSocketMsgType.cs_com, ZjMjComCommand.class);
        commandMap.put(WebSocketMsgType.cs_play, ZjMjPlayCommand.class);

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

    public static ZjMjCommandProcessor getInstance() {
        return processor;
    }

    @Override
    public void process(Player player, MessageUnit message) {
        int code = 0;
        try {
            ZjMjPlayer _player = player.getPlayer(processor);
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
