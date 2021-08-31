package com.sy599.game.staticdata.model;

public class NewPlayerGiftActivityBean
{
    private int liveness;  //领取奖励所需活跃度
    private int rewardType;  //奖励类型 1钻石 2现金红包
    private int reward;   //奖励额

    public int getLiveness() {
        return liveness;
    }

    public void setLiveness(int liveness) {
        this.liveness = liveness;
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
