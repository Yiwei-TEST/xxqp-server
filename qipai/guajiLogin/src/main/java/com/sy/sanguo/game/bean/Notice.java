package com.sy.sanguo.game.bean;

import java.util.Date;

public class Notice {
	private int id;
	private String title;
	private String content;
	private Date viewDate;
	private String pf;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public Date getViewDate() {
		return viewDate;
	}
	public void setViewDate(Date viewDate) {
		this.viewDate = viewDate;
	}
	public void setPf(String pf) {
		this.pf = pf;
	}
	public String getPf() {
		return pf;
	}
	
}
