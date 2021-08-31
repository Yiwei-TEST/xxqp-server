package com.sy599.game.websocket.model;

import com.sy599.game.util.StringUtil;

public class ChatContentMsg {
	private static String spitStr = "@-:";
	private String content;
	private int hongbaoId;
	private int hongbao;
	private long userId;
	private long talkId;
	private int systemtype;

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		if (content.contains(spitStr)) {
			content = content.replace(spitStr, "");
		}
		this.content = content;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public void setTalkId(long talkId) {
		this.talkId = talkId;
	}

	public long getTalkId() {
		return talkId;
	}

	public void setSystemtype(int systemtype) {
		this.systemtype = systemtype;
	}

	//
	// public ChatInfoMsg buildChatInfoMsg() {
	//
	// return msg;
	//
	// }

	public int getHongbaoId() {
		return hongbaoId;
	}

	public void setHongbaoId(int hongbaoId) {
		this.hongbaoId = hongbaoId;
	}

	public int getHongbao() {
		return hongbao;
	}

	public void setHongbao(int hongbao) {
		this.hongbao = hongbao;
	}

	public void initData(String data) {
		String[] arr = data.split(spitStr);
		int i = 0;
		userId = StringUtil.getLongValue(arr, i++);
		content = StringUtil.getValue(arr, i++);
		talkId = StringUtil.getLongValue(arr, i++);
		hongbaoId = StringUtil.getIntValue(arr, i++);
		hongbao = StringUtil.getIntValue(arr, i++);
		systemtype = StringUtil.getIntValue(arr, i++);
	}

	public String toStr() {
		StringBuffer sb = new StringBuffer();
		sb.append(userId).append(spitStr);
		sb.append(content).append(spitStr);
		sb.append(talkId).append(spitStr);
		sb.append(hongbaoId).append(spitStr);
		sb.append(hongbao).append(spitStr);
		sb.append(systemtype);
		return sb.toString();
	}
}
