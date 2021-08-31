package com.sy.sanguo.game.bean;

public class IpGroup {
	private String ip;
	private int count;
	

	public IpGroup() {
		super();
		// TODO Auto-generated constructor stub
	}


	public IpGroup(String ip, int count) {
		super();
		this.ip = ip;
		this.count = count;
	}


	public String getIp() {
		return ip;
	}


	public void setIp(String ip) {
		this.ip = ip;
	}


	public int getCount() {
		return count;
	}


	public void setCount(int count) {
		this.count = count;
	}

}
