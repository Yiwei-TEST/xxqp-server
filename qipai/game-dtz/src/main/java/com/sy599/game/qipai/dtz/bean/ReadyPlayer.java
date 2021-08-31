package com.sy599.game.qipai.dtz.bean;

public class ReadyPlayer {
	//角色ID
	private long userId;
	//座位号，0为未选座
	private int seat;
	//是否准备，0未准备，1已准备
	private int ready;
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public int getSeat() {
		return seat;
	}
	public void setSeat(int seat) {
		this.seat = seat;
	}
	public int getReady() {
		return ready;
	}
	public void setReady(int ready) {
		this.ready = ready;
	}

	
}
