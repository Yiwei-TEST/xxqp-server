package com.sy599.game.staticdata.model;

/**
 * @author liuping
 * 开服牌局返利活动配置
 */
public class GameReBate {
	
	/**
	 * 玩法ID
	 */
	private int wanfa;/**
	 * 活动开启时间
	 */
	private String openServerDate;

	/**
	 * 活动时间(天)
	 */
	private int rebateRangeTime;
	/**
	 * 牌局返利基础奖励局数
	 */
	private int baseBureau;

	public int getWanfa() {
		return wanfa;
	}

	public void setWanfa(int wanfa) {
		this.wanfa = wanfa;
	}

	public String getOpenServerDate() {
		return openServerDate;
	}

	public void setOpenServerDate(String openServerDate) {
		this.openServerDate = openServerDate;
	}

	public int getRebateRangeTime() {
		return rebateRangeTime;
	}

	public void setRebateRangeTime(int rebateRangeTime) {
		this.rebateRangeTime = rebateRangeTime;
	}

	/**
	 * @return the baseBureau
	 */
	public int getBaseBureau() {
		return baseBureau;
	}

	/**
	 * @param baseBureau the baseBureau to set
	 */
	public void setBaseBureau(int baseBureau) {
		this.baseBureau = baseBureau;
	}
}
