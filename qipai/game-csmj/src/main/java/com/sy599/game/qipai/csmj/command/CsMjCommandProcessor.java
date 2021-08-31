package com.sy599.game.qipai.csmj.command;

import com.sy599.game.character.Player;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.qipai.AbstractBaseCommandProcessor;
import com.sy599.game.qipai.csmj.bean.CsMjPlayer;
import com.sy599.game.qipai.csmj.command.com.CsMjComCommand;
import com.sy599.game.qipai.csmj.command.play.CsMjPlayCommand;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

import java.util.HashMap;
import java.util.Map;

public class CsMjCommandProcessor extends AbstractBaseCommandProcessor {
    private static CsMjCommandProcessor processor = new CsMjCommandProcessor();
    private static Map<Short, Class<? extends BaseCommand>> commandMap = new HashMap<>();
    private static Map<Class<?>, Short> msgClassToMsgTypeMap = new HashMap<Class<?>, Short>();

    static {
        commandMap.put(WebSocketMsgType.cs_com, CsMjComCommand.class);
        commandMap.put(WebSocketMsgType.cs_play, CsMjPlayCommand.class);

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

    public static CsMjCommandProcessor getInstance() {
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
        int code = 0;
        try {
            if (player == null || !(player instanceof CsMjPlayer)) {
                return;
            }
            CsMjPlayer csMajiangPlayer = player.getPlayer(processor);
            BaseCommand action = commandMap.get((short) message.getMsgType()).newInstance();
            action.setPlayer(csMajiangPlayer);
            action.execute(csMajiangPlayer, message);
        } catch (Exception e) {
            LogUtil.e("socket err: " + player.getUserId() + " " + message.getMsgType(), e);
            code = -1;
        } finally {
            if (code != 0) {
            }
        }
    }
}
