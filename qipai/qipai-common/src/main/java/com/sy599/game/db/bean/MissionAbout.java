package com.sy599.game.db.bean;

import java.util.Date;

public class MissionAbout {
	private long userId;
	private String dayMissionState="";
	private String otherMissionState="";
	private int brokeAward=0;
	private int brokeShare=0;
	private Date dayTime=new Date();
	private String ext="";

	public MissionAbout() {
	}

	public MissionAbout(long userId) {
		this.userId = userId;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getDayMissionState() {
		return dayMissionState;
	}

	public void setDayMissionState(String dayMissionState) {
		this.dayMissionState = dayMissionState;
	}

	public String getOtherMissionState() {
		return otherMissionState;
	}

	public void setOtherMissionState(String otherMissionState) {
		this.otherMissionState = otherMissionState;
	}

	public int getBrokeAward() {
		return brokeAward;
	}

	public void setBrokeAward(int brokeAward) {
		this.brokeAward = brokeAward;
	}

	public int getBrokeShare() {
		return brokeShare;
	}

	public void setBrokeShare(int brokeShare) {
		this.brokeShare = brokeShare;
	}

	public Date getDayTime() {
		return dayTime;
	}

	public void setDayTime(Date dayTime) {
		this.dayTime = dayTime;
	}

	public String getExt() {
		return ext;
	}

	public void setExt(String ext) {
		this.ext = ext;
	}
}
