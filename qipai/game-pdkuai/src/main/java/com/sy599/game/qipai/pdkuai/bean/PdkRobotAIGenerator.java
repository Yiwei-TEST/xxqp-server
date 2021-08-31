package com.sy599.game.qipai.pdkuai.bean;

import com.sy599.game.robot.RobotAIGenerator;
import com.sy599.game.qipai.pdkuai.robot.btlibrary.PaodekuaiBTLibrary;
import jbt.execution.core.BTExecutorFactory;
import jbt.execution.core.ContextFactory;
import jbt.execution.core.IBTExecutor;
import jbt.execution.core.IBTLibrary;
import jbt.execution.core.IContext;
import jbt.model.core.ModelTask;

public class PdkRobotAIGenerator extends RobotAIGenerator {

    private static final PdkRobotAIGenerator INST = new PdkRobotAIGenerator();

    private PdkRobotAIGenerator() {
    }

    public static PdkRobotAIGenerator getInst() {
        return INST;
    }

    @Override
    public IBTExecutor generateRobotAI(int level) {
        IBTLibrary btLibrary = new PaodekuaiBTLibrary();
        IContext context = ContextFactory.createContext(btLibrary);
        context.setVariable("CurrentEntityID", "terranMarine1");
        ModelTask terranMarineTree = btLibrary.getBT("pdk");
        IBTExecutor robotAI = BTExecutorFactory.createBTExecutor(terranMarineTree, context);
        return robotAI;
    }

}
