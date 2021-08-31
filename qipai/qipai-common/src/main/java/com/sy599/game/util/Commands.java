package com.sy599.game.util;

import com.sy599.game.qipai.AbstractBaseCommandProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 指令集合
 */
public final class Commands {
    /**
     * 所有支持的msgType
     */
    public static final Map<Number, Integer> COMMANDS = new ConcurrentHashMap<>();

    /**
     * 每个CommandProcessor所支持的msgType
     */
    public static final Map<Class<? extends AbstractBaseCommandProcessor>, List<Number>> MSG_TYPE_COMMANDS = new ConcurrentHashMap<>();

    static {
        COMMANDS.put((short)1000, 1);//长连接登陆
        COMMANDS.put((short)1001, 1);//普通登陆(重连)
        COMMANDS.put((short)1002, 1);//通用消息
    }

    /**
     * 所有支持的msgType中是否包含当前值
     */
    public static boolean contains(Number msgType) {
        return COMMANDS.containsKey(msgType);
    }

    /**
     * ICommandProssce所支持的msgType中是否包含当前值
     */
    public static boolean contains(Class<? extends AbstractBaseCommandProcessor> commandClass, Number msgType) {
        List<Number> list = MSG_TYPE_COMMANDS.get(commandClass);
        return list != null && list.contains(msgType);
    }

    /**
     * 指令注册
     *
     * @param commandClass
     * @param msgType
     */
    public static void registCommand(Class<? extends AbstractBaseCommandProcessor> commandClass, Number msgType) {
        int current = COMMANDS.getOrDefault(msgType, 0);
        current++;
        COMMANDS.put(msgType, current);

        List<Number> list = MSG_TYPE_COMMANDS.get(commandClass);
        if (list == null) {
            list = new ArrayList<>();
            list.add(msgType);
            MSG_TYPE_COMMANDS.put(commandClass, list);
        } else {
            list.add(msgType);
        }
    }
}
