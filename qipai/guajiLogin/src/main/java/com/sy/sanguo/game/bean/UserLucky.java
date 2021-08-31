package com.sy.sanguo.game.bean;

import java.util.Date;

public class UserLucky {
	
	private long userId;
	
	private String username;
	
	private int sex;
	
	private int inviteeCount;
	
	private long invitorId;
	
	private int feedbackCount;
	
	private int openCount;
	
	private Date activityStartTime;
	
	private int prizeFlag;

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public int getSex() {
		return sex;
	}

	public void setSex(int sex) {
		this.sex = sex;
	}

	public int getInviteeCount() {
		return inviteeCount;
	}

	public void setInviteeCount(int inviteeCount) {
		this.inviteeCount = inviteeCount;
	}

	public long getInvitorId() {
		return invitorId;
	}

	public void setInvitorId(long invitorId) {
		this.invitorId = invitorId;
	}

	public int getFeedbackCount() {
		return feedbackCount;
	}

	public void setFeedbackCount(int feedbackCount) {
		this.feedbackCount = feedbackCount;
	}

	public int getOpenCount() {
		return openCount;
	}

	public void setOpenCount(int openCount) {
		this.openCount = openCount;
	}

	public Date getActivityStartTime() {
		return activityStartTime;
	}

	public void setActivityStartTime(Date activityStartTime) {
		this.activityStartTime = activityStartTime;
	}

	public int getPrizeFlag() {
		return prizeFlag;
	}

	public void setPrizeFlag(int prizeFlag) {
		this.prizeFlag = prizeFlag;
	}

}
