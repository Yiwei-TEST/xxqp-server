package com.sy.sanguo.game.bean;


public class GroupServer {
	private int id;
	private String name;
	private String serverIds;
	private String content;
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
	public String getServerIds() {
		return serverIds;
	}
	public void setServerIds(String serverIds) {
		this.serverIds = serverIds;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
