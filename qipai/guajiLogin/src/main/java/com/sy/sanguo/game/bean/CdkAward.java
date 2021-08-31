package com.sy.sanguo.game.bean;

public class CdkAward {

	private int id;
	
	private String name;
	
	private String platform;
	
	private int cdkcount;
	
	private int awardId;// 房卡
	
	private int type;
	
	private int cdkType;
	
	private String cdk;
	
	private String cdkKey;
	
	private String regTime;
	
	private String endTime;

	public String getRegTime() {
		return regTime;
	}

	public void setRegTime(String regTime) {
		this.regTime = regTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public int getCdkcount() {
		return cdkcount;
	}

	public void setCdkcount(int cdkcount) {
		this.cdkcount = cdkcount;
	}

	public int getAwardId() {
		return awardId;
	}

	public void setAwardId(int awardId) {
		this.awardId = awardId;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getCdkType() {
		return cdkType;
	}

	public void setCdkType(int cdkType) {
		this.cdkType = cdkType;
	}

	public String getCdk() {
		return cdk;
	}

	public void setCdk(String cdk) {
		this.cdk = cdk;
	}

	public String getCdkKey() {
		return cdkKey;
	}

	public void setCdkKey(String cdkKey) {
		this.cdkKey = cdkKey;
	}
	
}
