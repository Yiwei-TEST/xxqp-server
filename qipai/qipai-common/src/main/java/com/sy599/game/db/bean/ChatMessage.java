package com.sy599.game.db.bean;

import java.io.Serializable;
import java.util.Date;

public class ChatMessage implements Serializable {
	private Long keyId;
	private Long groupId;
	private Long fromUser;
	private Long toUser;
	private String msgType;
	private String msgContent;
	private Date createdTime;

	public Long getKeyId() {
		return keyId;
	}

	public void setKeyId(Long keyId) {
		this.keyId = keyId;
	}

	public Long getGroupId() {
		return groupId;
	}

	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}

	public Long getFromUser() {
		return fromUser;
	}

	public void setFromUser(Long fromUser) {
		this.fromUser = fromUser;
	}

	public Long getToUser() {
		return toUser;
	}

	public void setToUser(Long toUser) {
		this.toUser = toUser;
	}

	public String getMsgType() {
		return msgType;
	}

	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}

	public String getMsgContent() {
		return msgContent;
	}

	public void setMsgContent(String msgContent) {
		this.msgContent = msgContent;
	}

	public Date getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
	}
}
