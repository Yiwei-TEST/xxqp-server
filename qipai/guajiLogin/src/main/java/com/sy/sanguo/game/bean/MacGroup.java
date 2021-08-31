package com.sy.sanguo.game.bean;

public class MacGroup {
	private String mac;
	private int count;
	

	public MacGroup() {
		super();
		// TODO Auto-generated constructor stub
	}
	public MacGroup(String mac, int count) {
		super();
		this.mac = mac;
		this.count = count;
	}
	public String getMac() {
		return mac;
	}
	public void setMac(String mac) {
		this.mac = mac;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
}
