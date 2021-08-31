package com.sy.sanguo.game.bean;

import java.util.Date;

public class SystemBlack {
	private String flatId;
	private String ip;
	private String mac;
	private String deviceCode;
	private Date time;
	private int isLoad;

	public String getFlatId() {
		return flatId;
	}

	public void setFlatId(String flatId) {
		this.flatId = flatId;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public String getDeviceCode() {
		return deviceCode;
	}

	public void setDeviceCode(String deviceCode) {
		this.deviceCode = deviceCode;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public int getIsLoad() {
		return isLoad;
	}

	public void setIsLoad(int isLoad) {
		this.isLoad = isLoad;
	}

}
