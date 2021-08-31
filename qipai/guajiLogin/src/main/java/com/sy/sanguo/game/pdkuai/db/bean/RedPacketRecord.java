package com.sy.sanguo.game.pdkuai.db.bean;

import java.util.Date;

/**
 * 红包记录表
 * @author Ysy
 *
 */
public class RedPacketRecord {
	private long id;//红包ID
	private long tableId;//牌桌ID
	private int hbType;//红包类型：1.牌局红包 2.幸运红包
	private Long userId;//用户Id
	private String userName;//用户名称
	private double money;//拥抱金额
	private Date createTime;//创建时间
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
	public int getHbType() {
		return hbType;
	}
	public void setHbType(int hbType) {
		this.hbType = hbType;
	}
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
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
