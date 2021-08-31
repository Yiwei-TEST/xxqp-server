package com.sy.sanguo.game.msg;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;


public class GameSiteMsg {

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
	
	private Map<Integer, String> gameRewardList = new HashMap<Integer, String>();
	
	private int applyNumber;
	
	private int applyStatus = 0;// 1代表已报名
	
	private int verifyStatus = 0;// 1代表需要验证
	
	private Timestamp currTime;
	
	private int serverId;
	
	private int applyMaxNumber;
	
	private Map<Integer, Integer> roundMap = new HashMap<>();
	
	private Map<Integer, Integer> boutMap = new HashMap<>();
	
	private Map<Integer, Integer> timesMap = new HashMap<>();

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

	public int getDurationTime() {
		return durationTime;
	}

	public void setDurationTime(int durationTime) {
		this.durationTime = durationTime;
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

	public int getApplyStatus() {
		return applyStatus;
	}

	public void setApplyStatus(int applyStatus) {
		this.applyStatus = applyStatus;
	}

	public int getApplyNumber() {
		return applyNumber;
	}

	public void setApplyNumber(int applyNumber) {
		this.applyNumber = applyNumber;
	}

	public int getVerifyStatus() {
		return verifyStatus;
	}

	public void setVerifyStatus(int verifyStatus) {
		this.verifyStatus = verifyStatus;
	}

	public Timestamp getCurrTime() {
		return currTime;
	}

	public void setCurrTime(Timestamp currTime) {
		this.currTime = currTime;
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

	public Map<Integer, String> getGameRewardList() {
		return gameRewardList;
	}

	public void setGameRewardList(Map<Integer, String> gameRewardList) {
		this.gameRewardList = gameRewardList;
	}

	public Map<Integer, Integer> getRoundMap() {
		return roundMap;
	}

	public void setRoundMap(Map<Integer, Integer> roundMap) {
		this.roundMap = roundMap;
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

	public Map<Integer, Integer> getBoutMap() {
		return boutMap;
	}

	public void setBoutMap(Map<Integer, Integer> boutMap) {
		this.boutMap = boutMap;
	}

	public String getConfigTimes() {
		return configTimes;
	}

	public void setConfigTimes(String configTimes) {
		this.configTimes = configTimes;
	}

	public Map<Integer, Integer> getTimesMap() {
		return timesMap;
	}

	public void setTimesMap(Map<Integer, Integer> timesMap) {
		this.timesMap = timesMap;
	}

	public int getApplyMaxNumber() {
		return applyMaxNumber;
	}

	public void setApplyMaxNumber(int applyMaxNumber) {
		this.applyMaxNumber = applyMaxNumber;
	}

}
