package com.sy.sanguo.game.pdkuai.db.bean;

import java.sql.Timestamp;

public class MatchInviteCode {

	private long id;
	
	private String inviteCode;
	
	private int gameSiteId;
	
	private int useFlag;
	
	private long useUserId;
	
	private int useServer;
	
	private Timestamp useTime;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getInviteCode() {
		return inviteCode;
	}

	public void setInviteCode(String inviteCode) {
		this.inviteCode = inviteCode;
	}

	public int getGameSiteId() {
		return gameSiteId;
	}

	public void setGameSiteId(int gameSiteId) {
		this.gameSiteId = gameSiteId;
	}

	public long getUseUserId() {
		return useUserId;
	}

	public void setUseUserId(long useUserId) {
		this.useUserId = useUserId;
	}

	public Timestamp getUseTime() {
		return useTime;
	}

	public void setUseTime(Timestamp useTime) {
		this.useTime = useTime;
	}

	public int getUseFlag() {
		return useFlag;
	}

	public void setUseFlag(int useFlag) {
		this.useFlag = useFlag;
	}

	public int getUseServer() {
		return useServer;
	}

	public void setUseServer(int useServer) {
		this.useServer = useServer;
	}
	
}
