package com.sy599.game.qipai.csmj.robot.btlibrary;

import com.sy599.game.qipai.csmj.bean.CsMjDisAction;
import com.sy599.game.qipai.csmj.bean.CsMjPlayer;
import com.sy599.game.qipai.csmj.constant.CsMjAction;
import com.sy599.game.qipai.csmj.robot.jbtAI.MajiangAITool;
import com.sy599.game.qipai.csmj.rule.CsMj;
import jbt.execution.core.*;
import jbt.model.core.ModelTask;

import java.util.List;

/**
 * 机器人动作处理结果
 * 2020年7月10日 15:14:44
 * butao
 */
public class CsmjRobotActionResult {
    private static boolean CanHu = false;
    private static boolean CanMingBu = false;
    private static boolean CanChi = false;
    private static boolean CanDa = false;
    private static boolean CanMingGang = false;
    private static boolean CanGuo = false;
    private static boolean CanPeng = false;
    private static boolean CanXiaoHu = false;
    private static boolean CanZiMo = false;
    private static boolean CanBuZhangAn = false;
    private static boolean CanAnGang = false;
    ;

    /**
     * @param actionList 0胡 1碰 2明刚 3暗杠(暗杠后来不需要了 暗杠也用3标记)4吃 5补张(6缺一色 7板板胡 8大四喜 9六六顺 10节节高 11三同
     *                   12一枝花 13中途四喜 14中途六六顺)
     * @return
     */
    private static void ActionDeal(List<Integer> actionList, boolean isDa) {

        if (actionList.get(CsMjAction.HU) == 1) {
            CsmjRobotActionResult.CanHu = true;
        }
        if (actionList.get(CsMjAction.ZIMO) == 1) {
            CsmjRobotActionResult.CanZiMo = true;
        }
        if (actionList.get(CsMjAction.BUZHANG) == 1) {
            CsmjRobotActionResult.CanMingBu = true;
        }
        if (actionList.get(CsMjAction.BUZHANG_AN) == 1) {
            CsmjRobotActionResult.CanBuZhangAn = true;
        }
        if (actionList.get(CsMjAction.CHI) == 1) {
            CsmjRobotActionResult.CanChi = true;
        }
        if (isDa) {
            CsmjRobotActionResult.CanDa = true;
            CsmjRobotActionResult.CanGuo = false;
        } else {
            CsmjRobotActionResult.CanDa = false;
            CsmjRobotActionResult.CanGuo = true;
        }
        if (actionList.get(CsMjAction.MINGGANG) == 1) {
            CsmjRobotActionResult.CanMingGang = true;
        }
        if (actionList.get(CsMjAction.ANGANG) == 1) {
            CsmjRobotActionResult.CanAnGang = true;
        }
        if (actionList.get(CsMjAction.PENG) == 1) {
            CsmjRobotActionResult.CanPeng = true;
        }
        if (CsMjAction.getFirstXiaoHu(actionList) != -1) {
            CsmjRobotActionResult.CanXiaoHu = true;
        }

    }

    private static void setDefault(boolean isDa) {
        CanHu = false;
        CanMingBu = false;
        CanChi = false;
        CanDa = false;
        CanGuo = false;
        CanPeng = false;
        CanXiaoHu = false;
        CanZiMo = false;
        CanBuZhangAn = false;
        CanMingGang = false;
        CanAnGang = false;
        if (isDa) {
            CsmjRobotActionResult.CanDa = true;
            CsmjRobotActionResult.CanGuo = false;
        } else {
            CsmjRobotActionResult.CanDa = false;
            CsmjRobotActionResult.CanGuo = true;
        }
    }

    /**
     * 根据入参获取最后动作结果
     *
     * @param actionMap
     * @param isDa
     * @param userName
     * @return
     */
    public static int RoBotAIBehavior(List<Integer> actionMap, boolean isDa, CsMjPlayer player,int OnlyDaHu,int quanqiurJiang ) {

        /* First of all, we create the BT library. */
        IBTLibrary btLibrary = new MajiangBTLibrary();
        /* Then we create the initial context that the tree will use. */
        IContext context = ContextFactory.createContext(btLibrary);
        /*
         * Now we are assuming that the marine that is going to be *
         * controlled has an id of "terranMarine1"
         */
        context.setVariable("CurrentEntityID", "terranMarine1");

        /* Now we get the Model BT to run. */
        ModelTask terranMarineTree = btLibrary.getBT("majiang");

        /* action 赋值 */
        context.setVariable("hu", CanChi);
        context.setVariable("mingbu", CanMingBu);
        context.setVariable("chi", CanChi);
        context.setVariable("da", CanDa);
        context.setVariable("minggang", CanMingGang);
        context.setVariable("guo", CanGuo);
        context.setVariable("peng", CanPeng);
        context.setVariable("xiaohu", CanXiaoHu);
        context.setVariable("zimo", CanZiMo);
        context.setVariable("anbu", CanBuZhangAn);
        context.setVariable("angang", CanAnGang);
        context.setVariable("userName", player.getName());


        /* Then we create the BT Executor to run the tree. */
        IBTExecutor btExecutor = BTExecutorFactory.createBTExecutor(terranMarineTree, context);
        btExecutor.tick();
        int step = 1;
        /* And finally we run the tree through the BT Executor. */
        do {
            System.out.println("--------------Step = " + step);
            btExecutor.tick();
            step++;
        } while (btExecutor.getStatus() == ExecutionTask.Status.RUNNING);

        String actionResult = "";
        if (null == context.getVariable("action")) {
            if (isDa) {
                actionResult = "da";
            } else {
                actionResult = "guo";
            }
        } else {
            actionResult = (String) context.getVariable("action");
        }
        int resutCommand = getDisActionCommand(actionResult);
        return resutCommand;
    }

    private static int getDisActionCommand(String actionResult) {
        int result = -1;
        switch (actionResult) {
            case "da":
                result = CsMjDisAction.action_chupai;
                break;
            case "hu":
                result = CsMjDisAction.action_hu;
                break;
            case "peng":
                result = CsMjDisAction.action_peng;
                break;
            case "minggang":
                result = CsMjDisAction.action_minggang;
                break;
            case "angang":
                result = CsMjDisAction.action_angang;
                break;
            case "guo":
                result = CsMjDisAction.action_pass;
                break;
            case "chi":
                result = CsMjDisAction.action_chi;
                break;
            case "mingbu":
                result = CsMjDisAction.action_buzhang;
                break;
            case "xiaohu":
                result = CsMjDisAction.action_xiaohu;
                break;//1-8
            case "anbu":
                result = CsMjDisAction.action_buzhang_an;//14
                break;
            case "zimo":
                result = CsMjDisAction.action_hu;
                break;
            default:
                result = -1;
                break;

        }
        System.out.println("Command:" + actionResult + "=================IntCommand:" + result);
        return result;
    }
}
