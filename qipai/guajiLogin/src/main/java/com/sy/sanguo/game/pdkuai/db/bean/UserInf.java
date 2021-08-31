package com.sy.sanguo.game.pdkuai.db.bean;

import java.util.Date;

public class UserInf {
	private long userId;
	private String flatId;
	private String pf;
	private String name;
	private int sex;
	private String sessionId;
	private String identity;
	private long cards;
	private long syncCards;
	private Date syncTime;
	private Date reginTime;
	private Date loginTime;
	private Date logoutTime;
	private String pay;
	private String paiInfo;
	private long playingTableId;
	private String config;
	private String extend;
	private int loginDays;
	private String activity;
	private String record;
	public String getPf() {
		return pf;
	}
	public void setPf(String pf) {
		this.pf = pf;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public String getFlatId() {
		return flatId;
	}
	public void setFlatId(String flatId) {
		this.flatId = flatId;
	}
	public String getIdentity() {
		return identity;
	}
	public void setIdentity(String identity) {
		this.identity = identity;
	}
	public long getCards() {
		return cards;
	}
	public void setCards(long cards) {
		this.cards = cards;
	}
	public long getSyncCards() {
		return syncCards;
	}
	public void setSyncCards(long syncCards) {
		this.syncCards = syncCards;
	}
	public Date getSyncTime() {
		return syncTime;
	}
	public void setSyncTime(Date syncTime) {
		this.syncTime = syncTime;
	}
	public Date getReginTime() {
		return reginTime;
	}
	public void setReginTime(Date reginTime) {
		this.reginTime = reginTime;
	}
	public Date getLoginTime() {
		return loginTime;
	}
	public void setLoginTime(Date loginTime) {
		this.loginTime = loginTime;
	}
	public Date getLogoutTime() {
		return logoutTime;
	}
	public void setLogoutTime(Date logoutTime) {
		this.logoutTime = logoutTime;
	}
	public String getPay() {
		return pay;
	}
	public void setPay(String pay) {
		this.pay = pay;
	}
	public String getPaiInfo() {
		return paiInfo;
	}
	public void setPaiInfo(String paiInfo) {
		this.paiInfo = paiInfo;
	}
	public String getConfig() {
		return config;
	}
	public void setConfig(String config) {
		this.config = config;
	}
	public String getExtend() {
		return extend;
	}
	public void setExtend(String extend) {
		this.extend = extend;
	}
	public int getSex() {
		return sex;
	}
	public void setSex(int sex) {
		this.sex = sex;
	}
	public long getPlayingTableId() {
		return playingTableId;
	}
	public void setPlayingTableId(long playingTableId) {
		this.playingTableId = playingTableId;
	}
	public int getLoginDays() {
		return loginDays;
	}
	public void setLoginDays(int loginDays) {
		this.loginDays = loginDays;
	}
	public String getActivity() {
		return activity;
	}
	public void setActivity(String activity) {
		this.activity = activity;
	}
	public String getRecord() {
		return record;
	}
	public void setRecord(String record) {
		this.record = record;
	}
	
	
	
}
