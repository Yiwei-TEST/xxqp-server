package com.sy599.game.db.bean.serverSign;

/**
 * 七日签到奖励信息
 */
public class SevenSignInfo {

    /**
     * 档次1-7
     */
    private int grade;
    /**
     * 档次状态  0未领取 1可领取 2已领取
     */
    private int state;
    /**
     * 奖励信息 1-6档金币数  7为宝箱奖励配置
     */
    private String reward;

    public SevenSignInfo(int grade, int state, String reward) {
        this.grade = grade;
        this.state = state;
        this.reward = reward;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getReward() {
        return reward;
    }

    public void setReward(String reward) {
        this.reward = reward;
    }
}
