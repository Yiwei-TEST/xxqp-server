package com.sy599.game.db.bean;

import java.util.Date;

public class SevenGoldSign {
	private long userId;
	private Date lastSignTime;
	private String sevenSign="";

	public SevenGoldSign() {
	}

	public SevenGoldSign(long userId, Date lastSignTime) {
		this.userId = userId;
		this.lastSignTime = lastSignTime;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public Date getLastSignTime() {
		return lastSignTime;
	}

	public void setLastSignTime(Date lastSignTime) {
		this.lastSignTime = lastSignTime;
	}

	public String getSevenSign() {
		return sevenSign;
	}

	public void setSevenSign(String sevenSign) {
		this.sevenSign = sevenSign;
	}
}
