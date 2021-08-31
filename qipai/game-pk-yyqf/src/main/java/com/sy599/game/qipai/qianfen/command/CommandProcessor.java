package com.sy599.game.qipai.qianfen.command;

import com.sy599.game.qipai.AbstractCommandProcessor;
import com.sy599.game.qipai.qianfen.bean.QianfenPlayer;
import com.sy599.game.util.ObjectUtil;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.qipai.qianfen.command.com.ComCommand;
import com.sy599.game.qipai.qianfen.command.play.PlayCommand;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

import java.util.HashMap;
import java.util.Map;

public class CommandProcessor extends AbstractCommandProcessor<QianfenPlayer> {
    private static CommandProcessor processor = new CommandProcessor();
    private static Map<Short, Class<? extends BaseCommand<QianfenPlayer>>> commandMap = new HashMap<>();
    private static Map<Class<?>, Short> msgClassToMsgTypeMap = new HashMap<Class<?>, Short>();

    static {
        commandMap.put(WebSocketMsgType.cs_com, ComCommand.class);
        commandMap.put(WebSocketMsgType.cs_play, PlayCommand.class);

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

    public static CommandProcessor getInstance() {
        return processor;
    }

    public short getMsgType(Class<?> clazz) {
        return msgClassToMsgTypeMap.getOrDefault(clazz, (short) 0);
    }

    @Override
    public Map<Short, Class<? extends BaseCommand<QianfenPlayer>>> loadCommands() {
        return commandMap;
    }
}
