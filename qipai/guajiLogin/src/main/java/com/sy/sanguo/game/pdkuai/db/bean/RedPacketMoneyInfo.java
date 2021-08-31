package com.sy.sanguo.game.pdkuai.db.bean;
/**
 * 红包金额表
 * @author Ysy
 *
 */
public class RedPacketMoneyInfo {
	private Long userid;//用户ID
	private double shengMoney;//未领取红包金额
	private double totalMoney;//红包总金额
	private int prizeFlag;//领奖状态
	public Long getUserid() {
		return userid;
	}
	public void setUserid(Long userid) {
		this.userid = userid;
	}
	public double getShengMoney() {
		return shengMoney;
	}
	public void setShengMoney(double shengMoney) {
		this.shengMoney = shengMoney;
	}
	public double getTotalMoney() {
		return totalMoney;
	}
	public void setTotalMoney(double totalMoney) {
		this.totalMoney = totalMoney;
	}
	public int getPrizeFlag() {
		return prizeFlag;
	}
	public void setPrizeFlag(int prizeFlag) {
		this.prizeFlag = prizeFlag;
	}
	
	
}
