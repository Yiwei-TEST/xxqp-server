package com.sy599.game.staticdata.bean;

import java.util.Date;

public class ActivityCsvInfo {

	private int id;

	private int type;

	private Date startTime;

	private Date endTime;

	private String newYearHb;

	private String goodLuckHb;

	private String fuDai;

	private int minExchange;
	
	private String rankingList;
	
	private String showContent;

	public String getShowContent() {
		return showContent;
	}

	public void setShowContent(String showContent) {
		this.showContent = showContent;
	}

	public String getRankingList() {
		return rankingList;
	}

	public void setRankingList(String rankingList) {
		this.rankingList = rankingList;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public String getNewYearHb() {
		return newYearHb;
	}

	public void setNewYearHb(String newYearHb) {
		this.newYearHb = newYearHb;
	}

	public String getGoodLuckHb() {
		return goodLuckHb;
	}

	public void setGoodLuckHb(String goodLuckHb) {
		this.goodLuckHb = goodLuckHb;
	}

	public String getFuDai() {
		return fuDai;
	}

	public void setFuDai(String fuDai) {
		this.fuDai = fuDai;
	}

	public int getMinExchange() {
		return minExchange;
	}

	public void setMinExchange(int minExchange) {
		this.minExchange = minExchange;
	}

}
