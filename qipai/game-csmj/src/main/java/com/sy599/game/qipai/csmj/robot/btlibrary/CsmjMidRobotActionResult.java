package com.sy599.game.qipai.csmj.robot.btlibrary;

import com.sy599.game.qipai.csmj.bean.CsMjDisAction;
import com.sy599.game.qipai.csmj.bean.CsMjPlayer;
import com.sy599.game.qipai.csmj.bean.CsMjTable;
import com.sy599.game.qipai.csmj.constant.CsMjAction;
import com.sy599.game.qipai.csmj.robot.jbtAI.MajiangAITool;
import com.sy599.game.qipai.csmj.rule.CsMj;
import com.sy599.game.qipai.csmj.tool.CsMjQipaiTool;
import com.sy599.game.util.LogUtil;
import jbt.execution.core.*;
import jbt.model.core.ModelTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 机器人动作处理结果
 * 2020年7月10日 15:14:44
 * butao
 */
public class CsmjMidRobotActionResult {
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
    private static void ActionDeal(List<Integer> actionList, boolean jiepai) {


        if (actionList.get(CsMjAction.HU) == 1) {
            CsmjMidRobotActionResult.CanHu = true;
        }
        if (actionList.get(CsMjAction.ZIMO) == 1) {
            CsmjMidRobotActionResult.CanZiMo = true;
        }
        if (actionList.get(CsMjAction.BUZHANG) == 1) {
            CsmjMidRobotActionResult.CanMingBu = true;
        }
        if (actionList.get(CsMjAction.BUZHANG_AN) == 1) {
            CsmjMidRobotActionResult.CanBuZhangAn = true;
        }
        if (actionList.get(CsMjAction.CHI) == 1) {
            CsmjMidRobotActionResult.CanChi = true;
        }

        if (jiepai) {
            CsmjMidRobotActionResult.CanDa = false;
            CsmjMidRobotActionResult.CanGuo = true;
        } else {
            CsmjMidRobotActionResult.CanDa = true;
            CsmjMidRobotActionResult.CanGuo = false;
        }
        if (actionList.get(CsMjAction.MINGGANG) == 1) {
            CsmjMidRobotActionResult.CanMingGang = true;
        }
        if (actionList.get(CsMjAction.ANGANG) == 1) {
            CsmjMidRobotActionResult.CanAnGang = true;
        }
        if (actionList.get(CsMjAction.PENG) == 1) {
            CsmjMidRobotActionResult.CanPeng = true;
        }
        if (CsMjAction.getFirstXiaoHu(actionList) > -1) {
            CsmjMidRobotActionResult.CanXiaoHu = true;
        } else {
            CsmjMidRobotActionResult.CanXiaoHu = false;
        }

    }

    private static void setDefault() {
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
            CsmjMidRobotActionResult.CanDa = true;
            CsmjMidRobotActionResult.CanGuo = false;
        } else {
            CsmjMidRobotActionResult.CanDa = false;
            CsmjMidRobotActionResult.CanGuo = true;
        }
    }

    /**
     * 根据入参获取最后动作结果
     *
     * @param actionMap
     * @param isDa
     * @param
     * @return
     */
    public static  HashMap<String, Object> RoBotAIBehavior(  IBTExecutor robotAI ,Map<Integer, List<Integer>> actionSeatMap,  CsMjPlayer player, int OnlyDaHu, int quanqiurJiang, List<CsMj> leftMajiang, CsMjTable ta
    ) {

        boolean jiepai =false;
        List<Integer> actionMap = actionSeatMap.get(player.getSeat());
        if(null==player.getPlayingTable()  ){
            LogUtil.printDebug("======牌局已经结束");
            return null;
        }
        if( null==actionMap && ta.getNowDisCardSeat()!=player.getSeat()){
            LogUtil.printDebug("机器人无动作且不为出牌人");
            return null;
        }
        //其他玩家起手小胡或者有小胡的情况下。不做判断return
        //System.out.println("actionSeatMap::::::::::::::::::::::::::::");
        //System.out.println(actionSeatMap);
        LogUtil.printDebug("actionSeatMap:"+actionSeatMap);
        for (int i = 1; i <= ta.getMaxPlayerCount(); i++) {
            if (i != player.getSeat()) {
                List<Integer> other_action = actionSeatMap.get(i);
                if (null != other_action && other_action.contains(1)) {
                    if (CsMjAction.getFirstXiaoHu(other_action) > 0) {
                        //其他玩家起手小胡 先让非机器人动作做完
                        LogUtil.printDebug("其他玩家起手小胡 先让非机器人动作做完: " );
                        return null;
                    }
                }
            }
        }
        if (null!=actionMap && actionMap.contains(1)) {
            //先处理动作序列
            if(!player.isAlreadyMoMajiang()){
                jiepai = true;
            }else{
                jiepai = false;
            }
        }else{
            if (ta.getNowDisCardSeat() == player.getSeat() && null== actionMap) {
                jiepai  = false;
            }else{
                return null;
            }
        }

        IContext context=robotAI.getRootContext();
        context.setVariable("CurrentEntityID", "terranMarine1");
        ModelTask terranMarineTree =  robotAI.getBehaviourTree();// btLibrary.getBT("midMajiang");

        /* action 赋值 */
        context.setVariable("hu", CanHu);
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
        // AI行为赋值
        if (null == actionMap || actionMap.size() == 0) {
            setDefault(jiepai);
            context.setVariable("IsWuCaoZuo", true);
        } else {
            setDefault();
            ActionDeal(actionMap, jiepai);
            context.setVariable("IsWuCaoZuo", false);
        }
        //重新赋值动作
        context.setVariable("hu", CanHu);
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

        CsMj mj3;
        if (null == ta.getNowDisCardIds() || ta.getNowDisCardIds().size() == 0) {
            //起手摸排大四喜情况 没人出牌
            mj3 = null;
            jiepai = false;
        } else {
            mj3 = ta.getNowDisCardIds().get(0);
        }

        if (CanXiaoHu) {
            int xiaohuType = CsMjAction.getFirstXiaoHu(actionMap);
            context.setVariable("xiaohuType", xiaohuType);
            //优先胡小胡
            if (xiaohuType > -1) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("action", CsMjDisAction.action_xiaohu);
                map.put("xiaohuType", xiaohuType);
                List<CsMj> xiaohulist = player.showXiaoHuMajiangs(xiaohuType, false);
                ta.huXiaoHu(player, xiaohulist, xiaohuType, CsMjDisAction.action_xiaohu);
                setDefault();

                return null;
            }
        }
//        //特殊情况处理。上家出牌 下家吃后自摸胡 先处理吃的动作
//        if(CanChi && CanZiMo ){
//            CanChi= false;
//            context.setVariable("chi", CanChi);
//        }
        //是否听牌
//        result.get("dismj")
        HashMap<String, Object>   tingmap = MajiangAITool.isTingPai(player, OnlyDaHu, quanqiurJiang, leftMajiang);

        if (null != tingmap ) {
            List<CsMj> tingmj = (List<CsMj>) tingmap.get("tingmj");
            if(null!=tingmj && tingmj.size()>0){
                context.setVariable("IsTinPai", true);
            }else{
                context.setVariable("IsTinPai", false);
            }

            CsMj dismj=(CsMj) tingmap.get("dismj");
            if(null!= dismj){
                context.setVariable("IsTinPaiCard", dismj);
            }else{
                context.setVariable("IsTinPaiCard", null);
            }
        } else {
            context.setVariable("IsTinPai", false);
            context.setVariable("IsTinPaiCard", null);
        }


        if (jiepai) {
            context.setVariable("jiepai", jiepai);
            context.setVariable("mopai", false);
            context.setVariable("canPengMj", new ArrayList<>(player.getHandMajiang()));
            context.setVariable("canChiMj", new ArrayList<>(player.getHandMajiang()));
        } else {
            context.setVariable("jiepai", false);
            context.setVariable("mopai", true);
        }

        List<CsMj> hands = new ArrayList<>(player.getHandMajiang());

        //移除牌型
        LogUtil.printDebug("===当前手牌："+hands);
        List<CsMj> sypx = MajiangAITool.removePaiXing(hands);
        LogUtil.printDebug("===移除后手牌手牌："+hands);
          LogUtil.printDebug("================================================");
        if (null != sypx && sypx.size() > 0) {
            context.setVariable("YichuPaixing", true);
            context.setVariable("YichuPaixingCard", sypx);
        } else {
            context.setVariable("YichuPaixing", false);
            context.setVariable("YichuPaixingCard", null);
        }

         LogUtil.printDebug("CanChi=" + CanChi);
         LogUtil.printDebug("CanPeng=" + CanPeng);
          LogUtil.printDebug("CanMingGang=" + CanMingGang);
          LogUtil.printDebug("CanAnGang=" + CanAnGang);
          LogUtil.printDebug("CanBuZhangAn=" + CanBuZhangAn);
          LogUtil.printDebug("CanMingBu=" + CanMingBu);
          LogUtil.printDebug("CaZimo=" + CanZiMo);
          LogUtil.printDebug("Cahu=" + CanHu);

        if (jiepai) {
            if (null != mj3) {
                context.setVariable("deskpai", ta.getNowDisCardIds().get(0));
                //无动作接牌 确保桌面有出的牌 deskpai！=null
                boolean chup = MajiangAITool.isJiang(ta.getNowDisCardIds().get(0));
                context.setVariable("isJiang", chup);
                  LogUtil.printDebug("接牌是否为将" + chup + " ==" + ta.getNowDisCardIds().get(0));

                //移除顺砍
                List<CsMj> sydui = MajiangAITool.removeShunKan(hands);
                context.setVariable("YichuShunKan", sydui);
                  LogUtil.printDebug("移除顺坎后：" + sydui);

                //多将对
                List<CsMj> isDuoJiangDui = MajiangAITool.isDuoDuiJiang(sydui);
                context.setVariable("isDuoJiangDui", isDuoJiangDui);
                  LogUtil.printDebug("是否多将对：" + isDuoJiangDui);
            }
        }

        if (CanChi) {
            //重新判定移除牌型后能不能吃。
            if (ta.getNowDisCardIds().size() == 1) {
                boolean re = MajiangAITool.IsZuyijuhua(sypx, ta.getNowDisCardIds().get(0));
                context.setVariable("chi", re);
            }
        }

        //备份chi peng 判断手牌。
        if (jiepai) {
            //重置数据
            context.setVariable("resetMj", new ArrayList<>(player.getHandMajiang()));
            // isChiPai isPengPai 判断
            context.setVariable("canPengMj", new ArrayList<>(player.getHandMajiang()));
            context.setVariable("canChiMj", new ArrayList<>(player.getHandMajiang()));
        }


        /* Then we create the BT Executor to run the tree. */
        IBTExecutor btExecutor = BTExecutorFactory.createBTExecutor(terranMarineTree, context);
        btExecutor.tick();
        int step = 1;
        /* And finally we run the tree through the BT Executor. */
        if (leftMajiang.size() == 1) {
            //海底判断
            ta.moLastMajiang(player, CsMjDisAction.action_passmo);
        }
        do {
              LogUtil.printDebug("--------------Step = " + step);
            btExecutor.tick();
            step++;
        } while (btExecutor.getStatus() == ExecutionTask.Status.RUNNING);


        String actionResult = "";
        if (null == context.getVariable("action")) {
            if (jiepai) {
                actionResult = "guo";
            } else {
                actionResult = "da";
            }
        } else {
            actionResult = (String) context.getVariable("action");
        }
        int resutCommand = getDisActionCommand(actionResult);
        HashMap<String, Object> map = new HashMap<>();
        map.put("action", resutCommand);
        map.put("chup", context.getVariable("chup"));
        map.put("act",actionResult);
        map.put("chi",context.getVariable("chiCards"));
        LogUtil.printDebug("机器人返回数据=========动作：" + actionResult);
        LogUtil.printDebug(map.toString());
        int action = (int) map.get("action");
        CsMj mj = (CsMj) map.get("chup");
        if (action == CsMjDisAction.action_pass) {
            ta.playCommand(player, new ArrayList<CsMj>(), CsMjDisAction.action_pass);
        }
        if (action == CsMjDisAction.action_chupai && null != mj) {
            if (!player.getGang().isEmpty()) {
                // 已经杠过了牌 长沙麻将摸啥打啥
                List<CsMj> a = new ArrayList<>();
                a.add(player.getLastMoMajiang());
                ta.playCommand(player,a, action);
            }
            List<CsMj> disMjiang = new ArrayList<>();
            disMjiang.add(mj);
            ta.playCommand(player, disMjiang, action);
        } else {
            List<CsMj> list = new ArrayList<>();
            if (!ta.getNowDisCardIds().isEmpty()) {
                list = CsMjQipaiTool.getVal(player.getHandMajiang(), ta.getNowDisCardIds().get(0).getVal());
            }
            if (action == CsMjDisAction.action_hu) {
                // 胡
                ta.playCommand(player, new ArrayList<CsMj>(), CsMjDisAction.action_hu);
                ta.getActionSeatMap().clear();
                setDefault();
            } else if (action == CsMjDisAction.action_angang) {
                CsMj mj1 = player.getLastMoMajiang();
                list = CsMjQipaiTool.getVal(player.getHandMajiang(), mj1.getVal());
                List<CsMj> list2 = CsMjQipaiTool.getVal(player.getPeng(), mj1.getVal());
                if(list.size()+list2.size()==4){
                    ta.playCommand(player, list, CsMjDisAction.action_angang);
                }else{
                    List<CsMj> templist = new ArrayList<>(player.getHandMajiang());
                    for(CsMj tmj:templist){
                        List<CsMj> list3 = CsMjQipaiTool.getVal(player.getHandMajiang(), tmj.getVal());
                        if(list3.size()==4){
                            ta.playCommand(player, list3, CsMjDisAction.action_angang);
                            break;
                        }
                    }
                }
                //ta.playCommand(player, list, CsMjDisAction.action_angang);
            } else if (action == CsMjDisAction.action_minggang) {
                if (player.isAlreadyMoMajiang()) {
                    CsMj lastmo_ = player.getLastMoMajiang();
                    list = CsMjQipaiTool.getVal(player.getHandMajiang(), lastmo_.getVal());
                }
                ta.playCommand(player, list, CsMjDisAction.action_minggang);

            }
            else if (action == CsMjDisAction.action_peng) {
                List<CsMj> penglist;
                if(ta.isHasGangAction()){
                    //非机器人杠后开牌能碰
                    for (CsMj gangdismj:ta.getNowDisCardIds()  ) {
                        boolean pengresult = MajiangAITool.CanPeng(player.getHandMajiang(),gangdismj);
                        if(pengresult){
                            penglist =MajiangAITool.getPengMjList(player.getHandMajiang(),gangdismj);
                            ta.playCommand(player, penglist, CsMjDisAction.action_peng);
                            break;
                        }
                    }
                }else{
                    ta.playCommand(player, list, CsMjDisAction.action_peng);
                }

            }
            else if (action == CsMjDisAction.action_buzhang) {
                if (player.isAlreadyMoMajiang()) {
                    CsMj lastmo_ = player.getLastMoMajiang();
                    list = CsMjQipaiTool.getVal(player.getHandMajiang(), lastmo_.getVal());
                }
                ta.playCommand(player, list, CsMjDisAction.action_buzhang);

            }
            else if (action == CsMjDisAction.action_buzhang_an) {
                CsMj lastmo_ = player.getLastMoMajiang();
                list = CsMjQipaiTool.getVal(player.getHandMajiang(), lastmo_.getVal());
                List<CsMj> list2 = CsMjQipaiTool.getVal(player.getPeng(), lastmo_.getVal());
                if(list.size()+list2.size()==4){
                    ta.playCommand(player, list, CsMjDisAction.action_buzhang_an);
                }else{
                    List<CsMj> templist = new ArrayList<>(player.getHandMajiang());
                    for(CsMj tmj:templist){
                        List<CsMj> list3 = CsMjQipaiTool.getVal(player.getHandMajiang(), tmj.getVal());
                        if(list3.size()==4){
                            ta.playCommand(player, list3, CsMjDisAction.action_buzhang_an);
                            break;
                        }
                    }
                }

            } else if (action == CsMjDisAction.action_chi) {
                   List<CsMj> chilist;
                   if(ta.isHasGangAction()){
                       //非机器人杠后开拍能吃
                       for (CsMj gangdismj:ta.getNowDisCardIds()  ) {
                           chilist = player.getCanChiMajiangs(gangdismj);
                           boolean chiresult = MajiangAITool.IsZuyijuhua(chilist,gangdismj);
                           if(chiresult){
                                chilist.add(gangdismj);
                                ta.playCommand(player, chilist, CsMjDisAction.action_chi);
                           }
                       }
                   }else{
                       List<CsMj> chicards = (List<CsMj>) context.getVariable("chiCards");
                       if(null!=chicards && chicards.size()>0){
                           ta.playCommand(player,chicards, CsMjDisAction.action_chi);
                       }else{
                           ta.playCommand(player, player.getCanChiMajiangs(ta.getNowDisCardIds().get(0)), CsMjDisAction.action_chi);

                       }
                  }
            }
        }
        setDefault();
        return map;
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
        return result;
    }

}
