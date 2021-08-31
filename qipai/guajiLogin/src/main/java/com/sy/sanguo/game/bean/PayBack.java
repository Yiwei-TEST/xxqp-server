package com.sy.sanguo.game.bean;

import java.util.Date;
public class PayBack {
	
	private String flatid;
	private int payamout;
	private int serverid;
	private int issent;
	private Date selltime;
	
	public String getFlatid() {
		return flatid;
	}
	public void setFlatid(String flatid) {
		this.flatid = flatid;
	}
	public int getPayamout() {
		return payamout;
	}
	public void setPayamout(int payamout) {
		this.payamout = payamout;
	}
	public int getServerid() {
		return serverid;
	}
	public void setServerid(int serverid) {
		this.serverid = serverid;
	}
	public int getIssent() {
		return issent;
	}
	public void setIssent(int issent) {
		this.issent = issent;
	}
	public Date getSelltime() {
		return selltime;
	}
	public void setSelltime(Date selltime) {
		this.selltime = selltime;
	}
	
}
