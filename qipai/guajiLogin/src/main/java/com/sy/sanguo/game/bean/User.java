package com.sy.sanguo.game.bean;

import java.util.List;

import com.sy.sanguo.game.msg.ActivityMsg;

public class User {
	private long userId;
	private String username;
	private String pf;
	private String name;
	private String headimgurl;
	private int sex;
	private long cards;
	private String connectHost;
	private String playedSid;
	private String sessCode;
	private long playTableId;
	private long payBindId;
	private long regBindId;
	private int isNewReg;
	private int isShowRankActivity;
	private int isShowButton;
	private int serverId;
	private List<ActivityMsg> activityList;
	private long totalCount;
	private GoldUserInfo goldUserInfo;
	private boolean hasPay = false;
	private String connectHost1;
	private String connectHost2;
	private String phoneNum ;
    private long inviterPayBindId;

	public String getConnectHost1() {
		return connectHost1;
	}

	public void setConnectHost1(String connectHost1) {
		this.connectHost1 = connectHost1;
	}

	public String getConnectHost2() {
		return connectHost2;
	}

	public void setConnectHost2(String connectHost2) {
		this.connectHost2 = connectHost2;
	}

	public boolean isHasPay() {
		return hasPay;
	}

	public void setHasPay(boolean hasPay) {
		this.hasPay = hasPay;
	}

	public GoldUserInfo getGoldUserInfo() {
		return goldUserInfo;
	}

	public void setGoldUserInfo(GoldUserInfo goldUserInfo) {
		this.goldUserInfo = goldUserInfo;
	}

	public int getIsShowButton() {
		return isShowButton;
	}

	public void setIsShowButton(int isShowButton) {
		this.isShowButton = isShowButton;
	}

	public String getPf() {
		return pf;
	}

	public void setPf(String pf) {
		this.pf = pf;
	}

	public String getSessCode() {
		return sessCode;
	}

	public void setSessCode(String sessCode) {
		this.sessCode = sessCode;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPlayedSid() {
		return playedSid;
	}

	public void setPlayedSid(String playedSid) {
		this.playedSid = playedSid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getSex() {
		return sex;
	}

	public void setSex(int sex) {
		this.sex = sex;
	}

	public long getCards() {
		return cards;
	}

	public void setCards(long cards) {
		this.cards = cards;
	}

	public String getConnectHost() {
		return connectHost;
	}

	public void setConnectHost(String connectHost) {
		this.connectHost = connectHost;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getHeadimgurl() {
		return headimgurl;
	}

	public void setHeadimgurl(String headimgurl) {
		this.headimgurl = headimgurl;
	}

	public long getPlayTableId() {
		return playTableId;
	}

	public void setPlayTableId(long playTableId) {
		this.playTableId = playTableId;
	}

	public long getPayBindId() {
		return payBindId;
	}

	public void setPayBindId(long payBindId) {
		this.payBindId = payBindId;
	}

	public int getIsNewReg() {
		return isNewReg;
	}

	public void setIsNewReg(int isNewReg) {
		this.isNewReg = isNewReg;
	}

	public int getIsShowRankActivity() {
		return isShowRankActivity;
	}

	public void setIsShowRankActivity(int isShowRankActivity) {
		this.isShowRankActivity = isShowRankActivity;
	}

	public long getRegBindId() {
		return regBindId;
	}

	public void setRegBindId(long regBindId) {
		this.regBindId = regBindId;
	}

	public List<ActivityMsg> getActivityList() {
		return activityList;
	}

	public void setActivityList(List<ActivityMsg> activityList) {
		this.activityList = activityList;
	}

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public long getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(long totalCount) {
		this.totalCount = totalCount;
	}

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public long getInviterPayBindId() {
        return inviterPayBindId;
    }

    public void setInviterPayBindId(long inviterPayBindId) {
        this.inviterPayBindId = inviterPayBindId;
    }
}
