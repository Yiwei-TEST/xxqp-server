package com.sy599.game.staticdata.model;

public class InviteRewardActivityBean {

	private int friendNum;//邀请人数
	private int type;//奖励类型 1钻石2现金
	private int rewardNum;//奖励数
	
	public int getFriendNum() {
		return friendNum;
	}
	public void setFriendNum(int friendNum) {
		this.friendNum = friendNum;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getRewardNum() {
		return rewardNum;
	}
	public void setRewardNum(int rewardNum) {
		this.rewardNum = rewardNum;
	}
}
