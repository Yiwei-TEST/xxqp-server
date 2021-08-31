package com.sy599.game.staticdata.bean;

import com.sy599.game.staticdata.model.NewPlayerGiftActivityBean;
import com.sy599.game.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewPlayerGiftActivityConfig extends ActivityConfigInfo
{
    /*--------------------新人有礼活动达成任务类型----------------------*/
    /**
     * 每日完成牌局数（0/2）
     */
    public static int type_game_count = 1;
    /**
     * 每日首次分享（0/1）
     */
    public static int type_daily_share = 2;
    /**
     * 大赢家次数（0/3）
     */
    public static int type_big_win = 3;
    /**
     * 商城充值（0/1）
     */
    public static int type_mall_topUp = 4;
    /**
     * 加入亲友圈（0/1）
     */
    public static int type_join_fc = 5;

    /**
     * 新人有礼活动需要达成的任务配置  任务id - 需要完成次数
     */
    private Map<Integer, Integer> reachConditions;

    /**
     * 新人有礼活动达成任务的奖励
     * 【活跃度】配置  任务id - 活跃度
     */
    private Map<Integer, Integer> livenessReward;

    private int activityDay;  //活动时间

    private List<NewPlayerGiftActivityBean> rewardDiam = new ArrayList<>();  //奖励配置

    @Override
    public void configParamsAndRewards() {
        //1;2|2;1|3;3|4;1|5;1&1;10|2;10|3;20|4;20|5;20&7
        String[] paramsStr = params.split("&");
        reachConditions = new HashMap<>();
        if(!StringUtil.isBlank(paramsStr[0]))
        {
            String[] reachs = paramsStr[0].split("\\|");
            for(String reach : reachs)
            {
                String[] tempArr = reach.split(";");
                int type = Integer.parseInt(tempArr[0]);
                int count = Integer.parseInt(tempArr[1]);
                reachConditions.put(type, count);    //任务表
            }
        }
        livenessReward = new HashMap<>();
        if(!StringUtil.isBlank(paramsStr[1]))
        {
            String[] liveness = paramsStr[1].split("\\|");
            for(String livenStr : liveness)
            {
                String[] tempArr = livenStr.split(";");
                int type = Integer.parseInt(tempArr[0]);
                int liven = Integer.parseInt(tempArr[1]);
                livenessReward.put(type, liven);  //活跃度配置
            }
        }

        if(!StringUtil.isBlank(paramsStr[2]))
        {
            activityDay = Integer.parseInt(paramsStr[2]);
        }
        //"20_1_30;50_1_60;100_1_100;160_2_10"
        String[] strRewards = rewards.split(";");
        for(String strReward : strRewards)
        {
            String[] temps = strReward.split("_");
            if(temps.length == 3)
            {
                NewPlayerGiftActivityBean newPlayerGiftActivityBean = new NewPlayerGiftActivityBean();
                newPlayerGiftActivityBean.setLiveness(Integer.parseInt(temps[0]));
                newPlayerGiftActivityBean.setRewardType(Integer.parseInt(temps[1]));
                newPlayerGiftActivityBean.setReward(Integer.parseInt(temps[2]));
                rewardDiam.add(newPlayerGiftActivityBean);
            }
        }
    }

    public int getActivityDay() {
        return activityDay;
    }

    public void setActivityDay(int activityDay) {
        this.activityDay = activityDay;
    }

    public List<NewPlayerGiftActivityBean> getRewardDiam() {
        return rewardDiam;
    }

    public void setRewardDiam(List<NewPlayerGiftActivityBean> rewardDiam) {
        this.rewardDiam = rewardDiam;
    }

    public Map<Integer, Integer> getReachConditions() {
        return reachConditions;
    }

    public void setReachConditions(Map<Integer, Integer> reachConditions) {
        this.reachConditions = reachConditions;
    }

    public Map<Integer, Integer> getLivenessReward() {
        return livenessReward;
    }

    public void setLivenessReward(Map<Integer, Integer> livenessReward) {
        this.livenessReward = livenessReward;
    }
}
