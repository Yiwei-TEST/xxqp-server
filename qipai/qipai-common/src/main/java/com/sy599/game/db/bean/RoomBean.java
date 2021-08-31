package com.sy599.game.db.bean;

import java.io.Serializable;
import java.util.Date;

public class RoomBean implements Serializable {

	private static final long serialVersionUID = 4840434212131406714L;

	private int id;

	private long roomId;

	private long userId;

	private int serverId;

	private String players;

	private Date createTime;

	private int type;

	private int used;

	private String extend;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public long getRoomId() {
		return roomId;
	}

	public void setRoomId(long roomId) {
		this.roomId = roomId;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public String getPlayers() {
		return players;
	}

	public void setPlayers(String players) {
		this.players = players;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getUsed() {
		return used;
	}

	public void setUsed(int used) {
		this.used = used;
	}

	public String getExtend() {
		return extend;
	}

	public void setExtend(String extend) {
		this.extend = extend;
	}

	
}
