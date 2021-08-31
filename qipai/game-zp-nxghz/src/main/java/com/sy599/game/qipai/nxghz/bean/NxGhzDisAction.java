package com.sy599.game.qipai.nxghz.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 鬼胡子操作action  1溜 2飘3偎  4胡 5碰  6吃
 */
public class NxGhzDisAction {
    /*** 出牌**/
    public static final int action_chuPai = 0;
    // 操作优先级：溜 > 飘  > 偎  > 胡 > 碰  > 吃
    /**
     * 溜 1起手四张同牌，下牌不亮牌，称溜，算牌时叫内元;2偎后再摸同张牌，也称溜（也可以吃），算牌时称内元;3起手三张同牌，再自摸一张，亦称溜，亮一张，算内元
     */
    public static final int action_liu = 1;
    /**
     * 飘 (碰后再摸到同张字牌)
     */
    public static final int action_piao = 2;
    /**
     * 偎 (起手有两张，再自己摸一张相同的牌叫偎 偎牌亮一张)
     */
    public static final int action_wei = 3;
    /**
     * 胡
     **/
    public static final int action_hu = 4;
    /**
     * 碰 (别人打出的牌 或摸到的牌可碰)
     **/
    public static final int action_peng = 5;
    /**
     * 吃 (上家打出或摸到不要的牌  或者摸到的牌)
     **/
    public static final int action_chi = 6;
    /**
     * 过
     **/
    public static final int action_pass = 7;

    /**
     * 跑
     **/
    public static final int action_pao = 8;
    /**
     * 摸牌
     **/
    public static final int action_mo = 9;
    /**
     * 偎后溜
     */
    public static final int action_weiHouLiu = 11;
    /**
     * 有动作
     **/
    public static final int action_hasaction = 12;
    /**
     * 刷新
     **/
    public static final int action_refreshaction = 13;
    /**
     * 坎(主要用于结算牌型显示 和内圆 外圆计算)
     **/
    public static final int action_kang = 14;
    /**
     * 顺子 一句话(主要用于结算牌型显示 和内圆 外圆计算)
     **/
    public static final int action_shun = 15;
    /**
     * 鬼胡子内圆特殊处理
     **/
    public static final int action_shunChi = 16;
    /**
     * 鬼胡子九对半胡
     **/
    public static final int action_jiuduiban_hu = 17;

    /**
     * 优先list ------天胡 > 溜 > 飘  > 偎  > 胡 > 碰  > 吃
     **/
    public static final List<Integer> priority_action = Arrays.asList(action_jiuduiban_hu, action_liu, action_piao, action_wei, action_hu, action_peng, action_chi);

    // ////////////////////////////////////////////////////////
    /**
     * 动作
     **/
    public static final int action_type_action = 0;
    /**
     * 摸
     **/
    public static final int action_type_mo = 1;
    /**
     * 出牌
     **/
    public static final int action_type_dis = 2;

    /**
     * 托管
     **/
    public static final int action_tuoguan = 100;


    /**
     * 听牌
     */
    //public static final int action_type_ting = 3;
    public static int getMaxPriorityAction(List<Integer> actions) {
        for (int pri_ation : priority_action) {
            if (actions.contains(pri_ation))
                return pri_ation;
        }
        return action_chi;
    }

    /**
     * 优先的动作
     */
    public static List<Integer> findPriorityAction(int action) {
        List<Integer> findActionList = new ArrayList<>();
        for (int pri_ation : priority_action) {
            if (action == pri_ation) {
                break;
            }
            findActionList.add(pri_ation);
        }
        return findActionList;
    }


    /**
     * 转化action成操作
     *
     * @param actionList
     * @return
     */
    public static List<Integer> parseToDisActionList(List<Integer> actionList) {
        List<Integer> disActionList = new ArrayList<>();
        // 天胡 > 溜 > 飘  > 偎  > 胡 > 碰  > 吃
        if (actionList == null || actionList.isEmpty()) {
            return disActionList;
        }
        if (actionList.get(0) == 1) {
            disActionList.add(action_liu);
        }
        if (actionList.get(1) == 1) {
            disActionList.add(action_piao);
        }
        if (actionList.get(2) == 1) {
            disActionList.add(action_wei);
        }
        if (actionList.get(3) == 1) {
            disActionList.add(action_hu);
        }
        if (actionList.get(4) == 1) {
            disActionList.add(action_peng);
        }
        if (actionList.get(5) == 1) {
            disActionList.add(action_chi);
        }
        if (actionList.get(6) == 1) {
            disActionList.add(action_jiuduiban_hu);
        }
        return disActionList;
    }

    public static boolean canDis(List<Integer> stopActionList, List<Integer> actionList) {
        if (stopActionList == null) {
            return true;
        }
        List<Integer> disActionList = parseToDisActionList(actionList);
        for (int stop : stopActionList) {
            if (disActionList.contains(stop)) {
                return false;
            }
        }
        return true;
    }

    public static String getActionName(int action) {
        switch (action) {
            case action_chuPai:
                return "chuPai"; //出牌
            case action_liu:
                return "liu"; //溜
            case action_piao:
                return "piao"; //漂
            case action_wei:
                return "wei"; //偎
            case action_hu:
                return "hu"; //胡
            case action_peng:
                return "peng"; //碰
            case action_chi:
                return "chi"; //吃
            case action_pass:
                return "guo"; //过
            case action_mo:
                return "moPai"; //摸牌
            case action_weiHouLiu:
                return "weiHouLiu"; //偎后溜
            case action_jiuduiban_hu:
                return "jiuDuiBanHu"; //天胡
        }
        return "未定义-" + action;
    }


    /**
     * 优先的动作
     */
    public static int findPriorityAction2(int action) {
        for (int i = 0; i < priority_action.size(); i++) {
            if (priority_action.get(i) == action) {
                return i;
            }
        }
        return 0;
    }

    public static int getMaxPriorityActionWithoutHu(List<Integer> actList) {
        for (int act : priority_action) {
            if (act != action_hu && act != action_jiuduiban_hu && actList.contains(act)) {
                return act;
            }
        }
        return action_chi;
    }

    public static boolean isMax(int act1, int act2) {
        return priority_action.indexOf(act1) < priority_action.lastIndexOf(act2);
    }

}
