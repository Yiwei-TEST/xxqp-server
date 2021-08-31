package com.sy599.game.msg;

import java.util.List;

public class UserPlayTableMsg {
	private long id;
	private long tableId;
	private int playType;
	private int playCount;
	private String time;
	private int isWin;
	private List<UserPlayMsg> playerMsg;
	private List<String> resList;
	private String closingMsg;
	private String play;
	private int totalCount;
	private int groupScoreA;
	private int groupScoreB;
	private int groupScoreC;
	private List<Integer> cutCardList;

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getTableId() {
		return tableId;
	}

	public void setTableId(long tableId) {
		this.tableId = tableId;
	}

	public int getPlayCount() {
		return playCount;
	}

	public void setPlayCount(int playCount) {
		this.playCount = playCount;
	}

	public int getIsWin() {
		return isWin;
	}

	public void setIsWin(int isWin) {
		this.isWin = isWin;
	}

	public List<UserPlayMsg> getPlayerMsg() {
		return playerMsg;
	}

	public void setPlayerMsg(List<UserPlayMsg> playerMsg) {
		this.playerMsg = playerMsg;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public int getPlayType() {
		return playType;
	}

	public void setPlayType(int playType) {
		this.playType = playType;
	}

	public List<String> getResList() {
		return resList;
	}

	public void setResList(List<String> resList) {
		this.resList = resList;
	}

	public String getPlay() {
		return play;
	}

	public void setPlay(String play) {
		this.play = play;
	}

	public String getClosingMsg() {
		return closingMsg;
	}

	public void setClosingMsg(String closingMsg) {
		this.closingMsg = closingMsg;
	}


	public int getGroupScoreA() {
		return groupScoreA;
	}

	public void setGroupScoreA(int groupScoreA) {
		this.groupScoreA = groupScoreA;
	}

	public int getGroupScoreB() {
		return groupScoreB;
	}

	public void setGroupScoreB(int groupScoreB) {
		this.groupScoreB = groupScoreB;
	}

	public int getGroupScoreC() {
		return groupScoreC;
	}

	public void setGroupScoreC(int groupScoreC) {
		this.groupScoreC = groupScoreC;
	}
	
	public List<Integer> getCutCardList() {
		return cutCardList;
	}

	public void setCutCardList(List<Integer> cutCardList) {
		this.cutCardList = cutCardList;
	}
}
