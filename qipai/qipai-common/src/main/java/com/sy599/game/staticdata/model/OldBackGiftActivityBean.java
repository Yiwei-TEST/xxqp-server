package com.sy599.game.staticdata.model;

public class OldBackGiftActivityBean
{
    private int dayCount; //领取奖励需要登录天数
    private int rewardType;  //奖励类型 1钻石 2现金红包
    private int reward; //奖励额
    public int getDayCount() {
        return dayCount;
    }

    public void setDayCount(int dayCount) {
        this.dayCount = dayCount;
    }

    public int getRewardType() {
        return rewardType;
    }

    public void setRewardType(int rewardType) {
        this.rewardType = rewardType;
    }

    public int getReward() {
        return reward;
    }

    public void setReward(int reward) {
        this.reward = reward;
    }
}
