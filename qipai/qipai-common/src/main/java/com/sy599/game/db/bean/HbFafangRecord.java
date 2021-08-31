package com.sy599.game.db.bean;

import java.util.Date;

public class HbFafangRecord {

	private long id;
	
	private long tableId;
	
	private long userId;
	
	private String userName;
	
	private int hbType;
	
	private double money;
	
	private Date createTime;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getTableId() {
		return tableId;
	}

	public void setTableId(long tableId) {
		this.tableId = tableId;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public int getHbType() {
		return hbType;
	}

	public void setHbType(int hbType) {
		this.hbType = hbType;
	}

	public double getMoney() {
		return money;
	}

	public void setMoney(double money) {
		this.money = money;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
}
