package com.sy.sanguo.game.pdkuai.db.bean;

import java.sql.Timestamp;

public class GameSite {
	
	private int id;
	
	private String gameName;
	
	private int gameType;
	
	private int gameIcon;
	
	private int gameNumber;
	
	private int gameMaxNumber;
	
	private String configRound;
	
	private String configBout;
	
	private String configTimes;
	
	private Timestamp applyTime;
	
	private Timestamp beginTime;
	
	private int durationTime;
	
	private int gameCondition;
	
	private int needPropCount;
	
	private int inviteCode;
	
	private String gameReward;
	
	private int serverId;
	
	private int applyMaxNumber;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getGameName() {
		return gameName;
	}

	public void setGameName(String gameName) {
		this.gameName = gameName;
	}

	public int getGameType() {
		return gameType;
	}

	public void setGameType(int gameType) {
		this.gameType = gameType;
	}

	public int getGameIcon() {
		return gameIcon;
	}

	public void setGameIcon(int gameIcon) {
		this.gameIcon = gameIcon;
	}

	public int getGameNumber() {
		return gameNumber;
	}

	public void setGameNumber(int gameNumber) {
		this.gameNumber = gameNumber;
	}

	public Timestamp getApplyTime() {
		return applyTime;
	}

	public void setApplyTime(Timestamp applyTime) {
		this.applyTime = applyTime;
	}

	public Timestamp getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(Timestamp beginTime) {
		this.beginTime = beginTime;
	}

	public int getGameCondition() {
		return gameCondition;
	}

	public void setGameCondition(int gameCondition) {
		this.gameCondition = gameCondition;
	}

	public int getInviteCode() {
		return inviteCode;
	}

	public void setInviteCode(int inviteCode) {
		this.inviteCode = inviteCode;
	}

	public String getGameReward() {
		return gameReward;
	}

	public void setGameReward(String gameReward) {
		this.gameReward = gameReward;
	}

	public int getDurationTime() {
		return durationTime;
	}

	public void setDurationTime(int durationTime) {
		this.durationTime = durationTime;
	}

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public int getNeedPropCount() {
		return needPropCount;
	}

	public void setNeedPropCount(int needPropCount) {
		this.needPropCount = needPropCount;
	}

	public String getConfigRound() {
		return configRound;
	}

	public void setConfigRound(String configRound) {
		this.configRound = configRound;
	}

	public int getGameMaxNumber() {
		return gameMaxNumber;
	}

	public void setGameMaxNumber(int gameMaxNumber) {
		this.gameMaxNumber = gameMaxNumber;
	}

	public String getConfigBout() {
		return configBout;
	}

	public void setConfigBout(String configBout) {
		this.configBout = configBout;
	}

	public String getConfigTimes() {
		return configTimes;
	}

	public void setConfigTimes(String configTimes) {
		this.configTimes = configTimes;
	}

	public int getApplyMaxNumber() {
		return applyMaxNumber;
	}

	public void setApplyMaxNumber(int applyMaxNumber) {
		this.applyMaxNumber = applyMaxNumber;
	}
	
}
