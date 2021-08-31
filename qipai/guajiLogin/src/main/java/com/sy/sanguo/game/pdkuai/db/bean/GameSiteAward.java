package com.sy.sanguo.game.pdkuai.db.bean;

import java.sql.Timestamp;

public class GameSiteAward {

	private long id;
	
	private long userId;
	
	private String gameSiteName;
	
	private int gameSiteRank;
	
	private Timestamp gameSiteTime;
	
	private int awardId;
	
	private int awardCnt;
	
	private String awardName;

	private Timestamp receiveTime;
	
	private int awardStatus;
	
	private String telephone;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getGameSiteName() {
		return gameSiteName;
	}

	public void setGameSiteName(String gameSiteName) {
		this.gameSiteName = gameSiteName;
	}

	public int getGameSiteRank() {
		return gameSiteRank;
	}

	public void setGameSiteRank(int gameSiteRank) {
		this.gameSiteRank = gameSiteRank;
	}

	public Timestamp getGameSiteTime() {
		return gameSiteTime;
	}

	public void setGameSiteTime(Timestamp gameSiteTime) {
		this.gameSiteTime = gameSiteTime;
	}

	public int getAwardId() {
		return awardId;
	}

	public void setAwardId(int awardId) {
		this.awardId = awardId;
	}

	public String getAwardName() {
		return awardName;
	}

	public void setAwardName(String awardName) {
		this.awardName = awardName;
	}

	public Timestamp getReceiveTime() {
		return receiveTime;
	}

	public void setReceiveTime(Timestamp receiveTime) {
		this.receiveTime = receiveTime;
	}

	public int getAwardStatus() {
		return awardStatus;
	}

	public void setAwardStatus(int awardStatus) {
		this.awardStatus = awardStatus;
	}

	public int getAwardCnt() {
		return awardCnt;
	}

	public void setAwardCnt(int awardCnt) {
		this.awardCnt = awardCnt;
	}

	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}
	
}
