package com.sy599.game.db.bean;

import java.sql.Timestamp;

public class UserGameSite {

	private long userId;
	
	private int gameSiteId;
	
	private int integral;
	
	private int roundNum;
	
	private int playGame;
	
	private int turnBlank;
	
	private long tableId;
	
	private int serverId;
	
	private Timestamp applyTime;
	
	private String passInviteCode;
	
	private String name;

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public int getGameSiteId() {
		return gameSiteId;
	}

	public void setGameSiteId(int gameSiteId) {
		this.gameSiteId = gameSiteId;
	}

	public int getIntegral() {
		return integral;
	}

	public void setIntegral(int integral) {
		this.integral = integral;
	}

	public int getRoundNum() {
		return roundNum;
	}

	public void setRoundNum(int roundNum) {
		this.roundNum = roundNum;
	}

	public long getTableId() {
		return tableId;
	}

	public void setTableId(long tableId) {
		this.tableId = tableId;
	}

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public Timestamp getApplyTime() {
		return applyTime;
	}

	public void setApplyTime(Timestamp applyTime) {
		this.applyTime = applyTime;
	}

	public String getPassInviteCode() {
		return passInviteCode;
	}

	public void setPassInviteCode(String passInviteCode) {
		this.passInviteCode = passInviteCode;
	}

	public int getPlayGame() {
		return playGame;
	}

	public void setPlayGame(int playGame) {
		this.playGame = playGame;
	}

	public int getTurnBlank() {
		return turnBlank;
	}

	public void setTurnBlank(int turnBlank) {
		this.turnBlank = turnBlank;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
