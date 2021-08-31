package com.sy599.game.db.bean;

import java.util.Date;

public class DBNotice {
	private int id;
	private String name;
	private String serverIds;
	private String pfs;
	private String content;
	private int isuse;
	private Date updatetime;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public Date getUpdatetime() {
		return updatetime;
	}
	public void setUpdatetime(Date updatetime) {
		this.updatetime = updatetime;
	}
	public String getServerIds() {
		return serverIds;
	}
	public void setServerIds(String serverIds) {
		this.serverIds = serverIds;
	}
	public int getIsuse() {
		return isuse;
	}
	public void setIsuse(int isuse) {
		this.isuse = isuse;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPfs() {
		return pfs;
	}
	public void setPfs(String pfs) {
		this.pfs = pfs;
	}
}
