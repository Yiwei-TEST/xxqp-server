package com.sy.sanguo.game.bean;

public class PfSdkConfig {
	private String pf;
	private String mch_id;
	private String appId;
	private String appKey;
	private String payKey;
	private String reyunAppId;
	private boolean isLog;
	private String extStr;
	
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	public String getAppKey() {
		return appKey;
	}
	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}
	public boolean isLog() {
		return isLog;
	}
	public void setLog(boolean isLog) {
		this.isLog = isLog;
	}
	public void setReyunAppId(String reyunAppId) {
		this.reyunAppId = reyunAppId;
	}
	public String getReyunAppId() {
		return reyunAppId;
	}
	public void setPayKey(String payKey) {
		this.payKey = payKey;
	}
	public String getPayKey() {
		return payKey;
	}
	public void setPf(String pf) {
		this.pf = pf;
	}
	public String getPf() {
		return pf;
	}
	public String getMch_id() {
		return mch_id;
	}
	public void setMch_id(String mch_id) {
		this.mch_id = mch_id;
	}

	public String getExtStr() {
		return extStr;
	}

	public void setExtStr(String extStr) {
		this.extStr = extStr;
	}
}
