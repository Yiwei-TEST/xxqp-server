package com.sy.sanguo.game.bean;

public class UcSdkBean {
	//cp编号
	int cpId=0;
	//游戏编号
	int gameId=0;
	//游戏发行id
	String channelId="";
	//游戏服务器id
	int serverId=0;
	
	public int getCpId() {
		return cpId;
	}
	/**
	 * 设置cp编号
	 * @param cpId cp编号
	 */
	public void setCpId(int cpId) {
		this.cpId = cpId;
	}
	public int getGameId() {
		return gameId;
	}
	/**
	 * 设置游戏编号。
	 * @param gameId 游戏编号
	 */
	public void setGameId(int gameId) {
		this.gameId = gameId;
	}
	public String getChannelId() {
		return channelId;
	}
	/**
	 * 游戏发行渠道编号。
	 * @param channelId 游戏发行渠道编号
	 */
	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}
	public int getServerId() {
		return serverId;
	}
	public void setServerId(int serverId) {
		this.serverId = serverId;
	} 
}
