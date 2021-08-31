package com.sy599.game.staticdata.bean;

/**
 * @author liuping
 * 老带新活动配置
 */
public class OldDaiNewAcitivityConfig extends ActivityConfigInfo {

	private int rewardDiam;// 邀请并下载登陆玩家一个获得的钻石数
	
	private int maxRewardDiam;// 每日奖励钻石上限
	
	@Override
	public void configParamsAndRewards() {
		rewardDiam = Integer.parseInt(params);
		maxRewardDiam = Integer.parseInt(rewards);
	}

	public int getRewardDiam() {
		return rewardDiam;
	}

	public void setRewardDiam(int rewardDiam) {
		this.rewardDiam = rewardDiam;
	}

	public int getMaxRewardDiam() {
		return maxRewardDiam;
	}

	public void setMaxRewardDiam(int maxRewardDiam) {
		this.maxRewardDiam = maxRewardDiam;
	}
}
