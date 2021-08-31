package com.sy599.game.qipai;

import com.sy599.game.character.Player;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.ObjectUtil;
import com.sy599.game.websocket.netty.coder.MessageUnit;

import java.util.Map;

public abstract class AbstractCommandProcessor<T extends Player> extends AbstractBaseCommandProcessor<T> {

    public abstract Map<Short, Class<? extends BaseCommand<T>>> loadCommands();

    public void process(T player, MessageUnit message) {
        try {
            Class<? extends BaseCommand<T>> cls = loadCommands().get(message.getMsgType());
            if (cls == null) {
                LogUtil.msgLog.warn("command is not exists:msgType={},processor={},userId={},playerClass={}"
                        , message.getMsgType(), getClass().getName(), player == null ? "null" : player.getUserId(), player == null ? "unknown" : player.getClass().getName());
                return;
            }
            BaseCommand<T> action = ObjectUtil.newInstance(cls);
            action.setPlayer(player);
            action.execute(player, message);
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder("CommandProcessor");
            sb.append("|").append(getClass().getSimpleName());
            sb.append("|error");
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
