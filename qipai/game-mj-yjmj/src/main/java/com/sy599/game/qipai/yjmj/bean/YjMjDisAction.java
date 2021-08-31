package com.sy599.game.qipai.yjmj.bean;

import com.sy599.game.qipai.yjmj.constant.YjMjConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class YjMjDisAction {
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
     * 接杠
     **/
    public static final int action_jiegang = 7;
    /**
     * 小胡
     **/
    public static final int action_xiaohu = 8;
    /**
     * 摸麻将
     **/
    public static final int action_moMjiang = 9;
    /**
     * 海底捞时摸麻将
     **/
    public static final int action_moLastMjiang = 10;
    /**
     * 杠摸麻将
     **/
    public static final int action_moGangMjiang = 11;
    /**
     * 打牌时候有动作
     **/
    public static final int action_hasAction = 12;
    /**
     * 海底捞pass
     **/
    public static final int action_haodilaoPass = 13;
    /**
     * 报听
     **/
    public static final int action_baoting = 14;
    /**
     * 杠碰
     **/
    public static final int action_gangPeng = 15;
    /**
     * 海底捞暗杠
     **/
    public static final int action_haidi_angang = 16;
    /**
     * 下蛋
     **/
    public static final int action_layEgg = 20;
    /**
     * 听牌摸牌
     **/
    public static final int action_ting = 21;
    /**
     * 下蛋时有动作
     **/
    public static final int action_layEggActive = 22;
    /**
     * 摸宝
     **/
    public static final int action_moTreasure = 23;
    /**
     * 看宝
     **/
    public static final int action_seeTreasure = 24;
    public static final int action_mo = 1;
    public static final int action_passmo = 2;

    /**
     * 托管
     **/
    public static final int action_tuoguan = 100;

    /**
     * 优先list
     **/
    public static final List<Integer> priority_action = Arrays.asList(action_hu,action_baoting, action_angang, action_minggang, action_jiegang, action_peng, action_layEgg, action_chi);

    public static final List<Integer> auto_priority_action = Arrays.asList(action_peng);

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
        // 0胡 1碰 2明杠 3暗杠 4接杠 5杠爆 6报听
        if (actionList.isEmpty()) {
            return disActionList;
        }
        if (actionList.get(YjMjConstants.ACTION_INDEX_HU) == 1) {
            disActionList.add(action_hu);
        }
        if (actionList.get(YjMjConstants.ACTION_INDEX_PENG) == 1) {
            disActionList.add(action_peng);
        }
        if (actionList.get(YjMjConstants.ACTION_INDEX_MINGGANG) == 1) {
            disActionList.add(action_minggang);
        }
        if (actionList.get(YjMjConstants.ACTION_INDEX_ANGANG) == 1) {
            disActionList.add(action_angang);
        }
        if (actionList.get(YjMjConstants.ACTION_INDEX_JIEGANG) == 1) {
            disActionList.add(action_jiegang);
        }
        if (actionList.get(YjMjConstants.ACTION_INDEX_GANGBAO) == 1) {
            disActionList.add(0);
        }
        if (actionList.get(YjMjConstants.ACTION_INDEX_BAOTING) == 1) {
            disActionList.add(action_baoting);
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


    public static int getAutoMaxPriorityAction(List<Integer> actions) {
        for (int pri_ation : auto_priority_action) {
            if (actions.contains(pri_ation))
                return pri_ation;
        }
        return action_chi;
    }


}
