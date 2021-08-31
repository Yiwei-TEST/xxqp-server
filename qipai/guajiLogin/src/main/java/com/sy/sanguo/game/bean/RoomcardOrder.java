package com.sy.sanguo.game.bean;

import java.util.Date;

public class RoomcardOrder {
	private long roleId;
	private String orderId;
	private int registerBindAgencyId;
	private int rechargeBindAgencyId;
	private int isFirstPayBindId;
	private int isFirstPayAmount;
	private int commonCards;
	private int freeCards;
	private int orderStatus;
	private Date createTime;
	private int isDirectRecharge;
	private String rechargeWay;
	private int rechargeAgencyId;
	private String remark;

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public int getIsFirstPayAmount() {
		return isFirstPayAmount;
	}

	public void setIsFirstPayAmount(int isFirstPayAmount) {
		this.isFirstPayAmount = isFirstPayAmount;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public int getRegisterBindAgencyId() {
		return registerBindAgencyId;
	}

	public void setRegisterBindAgencyId(int registerBindAgencyId) {
		this.registerBindAgencyId = registerBindAgencyId;
	}

	public int getRechargeBindAgencyId() {
		return rechargeBindAgencyId;
	}

	public void setRechargeBindAgencyId(int rechargeBindAgencyId) {
		this.rechargeBindAgencyId = rechargeBindAgencyId;
	}

	public int getCommonCards() {
		return commonCards;
	}

	public void setCommonCards(int commonCards) {
		this.commonCards = commonCards;
	}

	public int getFreeCards() {
		return freeCards;
	}

	public void setFreeCards(int freeCards) {
		this.freeCards = freeCards;
	}

	public int getOrderStatus() {
		return orderStatus;
	}

	public void setOrderStatus(int orderStatus) {
		this.orderStatus = orderStatus;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public long getRoleId() {
		return roleId;
	}

	public void setRoleId(long roleId) {
		this.roleId = roleId;
	}

	public int getIsDirectRecharge() {
		return isDirectRecharge;
	}

	public void setIsDirectRecharge(int isDirectRecharge) {
		this.isDirectRecharge = isDirectRecharge;
	}

	public String getRechargeWay() {
		return rechargeWay;
	}

	public void setRechargeWay(String rechargeWay) {
		this.rechargeWay = rechargeWay;
	}

	public int getRechargeAgencyId() {
		return rechargeAgencyId;
	}

	public void setRechargeAgencyId(int rechargeAgencyId) {
		this.rechargeAgencyId = rechargeAgencyId;
	}

	public int getIsFirstPayBindId() {
		return isFirstPayBindId;
	}

	public void setIsFirstPayBindId(int isFirstPayBindId) {
		this.isFirstPayBindId = isFirstPayBindId;
	}

}
