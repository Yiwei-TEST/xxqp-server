package com.sy.sanguo.game.bean;

public class SystemChatMaskWord {

	private int id;
	private int isLoad;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	private String maskWord;

	public String getMaskWord() {
		return maskWord;
	}

	public void setMaskWord(String maskWord) {
		this.maskWord = maskWord;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public int getIsLoad() {
		return isLoad;
	}

	public void setIsLoad(int isLoad) {
		this.isLoad = isLoad;
	}

	private String userName;
	private String time;

}
