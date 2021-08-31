package com.sy599.game.qipai.yymj.command;

import com.google.protobuf.GeneratedMessage;
import com.sy.general.PackageHelper;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.ObjectUtil;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import lombok.ToString;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Optional;

/**
 * Code指令执行, 该执行执行器子类可托管覆盖掉命令集
 *
 * @author Guang.OuYang
 * @date 2019/9/2-16:48
 */
public abstract class AbsCodeCommandExecutor<T extends BaseTable, P extends Player> {


    /**
     * @description 指令操作, 这里是公共的全局指令集, 按目前结构为 Type->Code 分为公共指令(进入房间之前)与 玩家指令(进入房间之后)
     * * @param
     * @return
     * @author Guang.OuYang
     * @date 2019/9/2
     */
    private static Optional<HashMap<GlobalCommonIndex, HashMap<Integer, AbsCodeCommandExecutor>>> GLOBAL_COMMON_ACTION_CODE = Optional.ofNullable(new HashMap<GlobalCommonIndex, HashMap<Integer, AbsCodeCommandExecutor>>() {
        {
            Optional.ofNullable(PackageHelper.getClasses(AbsCodeCommandExecutor.class.getPackage().getName(), true, AbsCodeCommandExecutor.class)).ifPresent(v -> {
                v.stream().filter(v1 -> AbsCodeCommandExecutor.class.isAssignableFrom(v1)).map(v2 -> ((Class<AbsCodeCommandExecutor>) v2)).forEach(clazz -> {
                    try {
                        AbsCodeCommandExecutor absCodeCommandExecutor = newInstance(clazz);

                        this.putIfAbsent(absCodeCommandExecutor.globalCommonIndex(), new HashMap<>());

                        this.get(absCodeCommandExecutor.globalCommonIndex()).putIfAbsent(absCodeCommandExecutor.actionCode(), absCodeCommandExecutor);
                    } catch (Exception e) {
                    }
                });
            });
        }
    });

    private Class<T> tableType = getTClass(0);
    private Class<T> playerType = getTClass(1);

    /**
     * @return int
     * @description 初始化指令操作
     * @author Guang.OuYang
     * @date 2019/9/3
     */
    public static int initGlobalCommonActionCodeCache() {
        return GLOBAL_COMMON_ACTION_CODE.orElseGet(HashMap::new).size();
    }

    /**
     * @param
     * @return
     * @description
     * @author Guang.OuYang
     * @date 2019/9/2
     */
    public static <R extends AbsCodeCommandExecutor> Optional<R> getGlobalActionCode(GlobalCommonIndex globalCommonIndex, Integer actionCode) {
        return !GLOBAL_COMMON_ACTION_CODE.isPresent() ? Optional.empty() : Optional.ofNullable((R) GLOBAL_COMMON_ACTION_CODE.get().getOrDefault(globalCommonIndex, new HashMap<>()).get(actionCode));
    }

    /**
     * @param
     * @return
     * @description
     * @author Guang.OuYang
     * @date 2019/9/2
     */
    public static Optional<AbsCodeCommandExecutor> getGlobalActionCodeInstance(GlobalCommonIndex globalCommonIndex, Integer actionCode) throws Exception {
        Optional<AbsCodeCommandExecutor> command = getGlobalActionCode(globalCommonIndex, actionCode);
        return !command.isPresent() ? Optional.empty() : Optional.ofNullable(command.get());
    }

    /**
     * @param
     * @return
     * @description
     * @author Guang.OuYang
     * @date 2019/9/2
     */
    private static <R extends AbsCodeCommandExecutor> R newInstance(Class<? extends AbsCodeCommandExecutor> clazz) throws Exception {
        return (R) ObjectUtil.newInstance(clazz);
    }

    /**
     * @param
     * @return
     * @description 泛型处理类
     * @author Guang.OuYang
     * @date 2019/9/2
     */
    public Class<T> getTClass(int index) {
        Class<T> tClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[index];
        return tClass;
    }

    /**
     * @param player
     * @param messageUnit
     * @param baseCommand
     * @return generatedMessage 经过上游解码的消息
     * @description
     * @author Guang.OuYang
     * @date 2019/9/2
     */
    public final void execute0(P player, MessageUnit messageUnit, BaseCommand baseCommand, GeneratedMessage generatedMessage) {
        if (tableType == null || playerType == null) {
            LogUtil.errorLog.info("Not find 'T' Class '{}' , {}, {}", this.getClass(), tableType, playerType);
            return;
        }

        //具体的玩法桌子
        T table;

        if ((table = player.getPlayingTable(playerType)) == null) {
            return;
        }

        execute(table, player, new CarryMessage(baseCommand, messageUnit, generatedMessage));
    }

    /**
     * @param
     * @return
     * @description
     * @author Guang.OuYang
     * @date 2019/9/2
     */
    public abstract void execute(T table, P player, CarryMessage carryMessage);

    /**
     * @return int
     * @description 消息类型
     * @author Guang.OuYang
     * @date 2019/9/2
     */
    public abstract Integer actionCode();

    /**
     * @return int
     * @description 消息类型
     * @author Guang.OuYang
     * @date 2019/9/2
     */
    public abstract GlobalCommonIndex globalCommonIndex();


    /**
     * @param
     * @author Guang.OuYang
     * @description 变更Executor为无状态
     * @return
     * @date 2019/9/3
     */
    @ToString
    public static class CarryMessage {
        /**
         * 包头内MsgType的执行区
         */
        private BaseCommand baseCommand;
        /**
         * 一个完整的Packet包头包体信息,包含包体的原始字节码
         */
        private MessageUnit messageUnit;
        /**
         * 经过上游解码的消息
         */
        private GeneratedMessage generatedMessage;

        public CarryMessage(BaseCommand baseCommand, MessageUnit messageUnit, GeneratedMessage generatedMessage) {
            this.baseCommand = baseCommand;
            this.messageUnit = messageUnit;
            this.generatedMessage = generatedMessage;
        }

        public BaseCommand getBaseCommand() {
            return baseCommand;
        }

        public MessageUnit getMessageUnit() {
            return messageUnit;
        }

        public GeneratedMessage getGeneratedMessage() {
            return generatedMessage;
        }

        /**
         * @param
         * @return
         * @description Decoder
         * @author Guang.OuYang
         * @date 2019/9/2
         */
        public <R extends GeneratedMessage> R parseFrom(Class<? extends GeneratedMessage> clazz) {
            return (R) (generatedMessage != null && generatedMessage.getClass().isAssignableFrom(clazz) ? generatedMessage : baseCommand.recognize(clazz, messageUnit));
        }
    }


    public static enum GlobalCommonIndex {
        /**公共指令集msgType1002*/
        COMMAND_INDEX,       //公共指令集
        /**玩家指令集msgType1003*/
        PLAY_INDEX;         //玩家指令集
    }
}
