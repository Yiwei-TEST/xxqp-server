package com.sy599.game.qipai.csmj.bean;

import com.sy599.game.qipai.csmj.robot.btlibrary.MidMajiangBTLibrary;
import com.sy599.game.robot.RobotAIGenerator;
import jbt.execution.core.*;
import jbt.model.core.ModelTask;

public class CsMjRobotAIGenerator extends RobotAIGenerator {

    private static final CsMjRobotAIGenerator INST = new CsMjRobotAIGenerator();

    private CsMjRobotAIGenerator() {
    }

    public static CsMjRobotAIGenerator getInst() {
        return INST;
    }

    @Override
    public IBTExecutor generateRobotAI(int level) {
        IBTLibrary btLibrary = new MidMajiangBTLibrary();
        IContext context = ContextFactory.createContext(btLibrary);
        context.setVariable("CurrentEntityID", "terranMarine1");
        ModelTask terranMarineTree = btLibrary.getBT("midMajiang");
        IBTExecutor robotAI = BTExecutorFactory.createBTExecutor(terranMarineTree, context);
        return robotAI;
    }

}
