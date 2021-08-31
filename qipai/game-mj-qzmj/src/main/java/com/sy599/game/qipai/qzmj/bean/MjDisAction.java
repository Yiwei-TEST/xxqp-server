package com.sy599.game.qipai.qzmj.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MjDisAction {
    /**
     * 出牌
     **/
    public static final int action_chupai = 0;
    /**
     * 胡
     **/
    public static final int action_hu = 1;
    /**
     * 碰
     **/
    public static final int action_peng = 2;
    /**
     * 杠
     **/
    public static final int action_minggang = 3;
    /**
     * 暗杠
     **/
    public static final int action_angang = 4;
    /**
     * pass
     **/
    public static final int action_pass = 5;
    /**
     * 吃
     **/
    public static final int action_chi = 6;
    /**
     * 补张
     **/
    public static final int action_buzhang = 7;
    /**
     * 小胡
     **/
    public static final int action_xiaohu = 8;
    /**
     * 摸麻将
     **/
    public static final int action_moMjiang = 9;
    /**
     * 打牌时候有动作
     **/
    public static final int action_hasAction = 12;
    /**
     * 摇骰子
     **/
    public static final int action_dice = 13;

    public static final int action_baoting = 98;
    /**
     * 优先list
     **/
    public static final List<Integer> priority_action = Arrays.asList(action_hu, action_angang, action_minggang, action_buzhang, action_peng, action_chi);

    public static final List<Integer> auto_priority_action = Arrays.asList(action_minggang,action_angang,action_peng);

    private int round;
    private int seat;
    private long userId;
    private int majiangId;

    public int getSeat() {
        return seat;
    }

    public void setSeat(int seat) {
        this.seat = seat;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getMajiangId() {
        return majiangId;
    }

    public void setMajiangId(int majiangId) {
        this.majiangId = majiangId;
    }

    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
    }

    public static int getMaxPriorityAction(List<Integer> actions) {
        for (int pri_ation : priority_action) {
            if (actions.contains(pri_ation))
                return pri_ation;
        }
        return action_chi;
    }

    public static int getAutoMaxPriorityAction(List<Integer> actions) {
        for (int pri_ation : auto_priority_action) {
            if (actions.contains(pri_ation))
                return pri_ation;
        }
        return action_chi;
    }

    /**
     * 优先的动作
     *
     * @param action
     * @return
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

    public static List<Integer> parseToDisActionList(List<Integer> actionList) {
        List<Integer> disActionList = new ArrayList<>();
        // 0胡 1碰 2明刚 3暗杠(暗杠后来不需要了 暗杠也用3标记)4吃5补张
        if (actionList.isEmpty()) {
            return disActionList;
        }
        if (actionList.get(0) == 1) {
            disActionList.add(action_hu);
        }
        if (actionList.get(2) == 1) {
            disActionList.add(action_minggang);
        }
        if (actionList.get(3) == 1) {
            disActionList.add(action_angang);
        }
        if (actionList.get(1) == 1) {
            disActionList.add(action_peng);
        }
        if (actionList.get(4) == 1) {
            disActionList.add(action_chi);
        }
        return disActionList;
    }

    public static boolean canDisMajiang(List<Integer> stopActionList, List<Integer> actionList) {
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

}
