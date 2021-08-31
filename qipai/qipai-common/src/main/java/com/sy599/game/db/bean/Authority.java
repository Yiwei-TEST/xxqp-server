package com.sy599.game.db.bean;

import java.util.Date;

/**
 * 权限表
 */
public class Authority {
	private int id;
	private long userId;
	private int quanxianId;
	private long createTime;
	private String name;

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getQuanxianId() {
		return quanxianId;
	}

	public void setQuanxianId(int quanxianId) {
		this.quanxianId = quanxianId;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
