package com.sy.sanguo.game.pdkuai.db.bean;

import java.util.Date;

public class PdkTableLog {
	private long tableId;
	private String players;
	private String outCards1;
	private String outCards2;
	private String outCards3;
	private String handCards1;
	private String handCards2;
	private String handCards3;
	private int playType;
	private int enterServer;
	private long createId;
	private long winId;
	private int totalCount;
	private int count;
	private int startSeat;
	private Date createTime;
	private Date closingTime;
	private String closingRes;
	private String extend;

	public long getTableId() {
		return tableId;
	}

	public void setTableId(long tableId) {
		this.tableId = tableId;
	}

	public String getPlayers() {
		return players;
	}

	public void setPlayers(String players) {
		this.players = players;
	}

	public String getOutCards1() {
		return outCards1;
	}

	public void setOutCards1(String outCards1) {
		this.outCards1 = outCards1;
	}

	public String getOutCards2() {
		return outCards2;
	}

	public void setOutCards2(String outCards2) {
		this.outCards2 = outCards2;
	}

	public String getOutCards3() {
		return outCards3;
	}

	public void setOutCards3(String outCards3) {
		this.outCards3 = outCards3;
	}

	public String getHandCards1() {
		return handCards1;
	}

	public void setHandCards1(String handCards1) {
		this.handCards1 = handCards1;
	}

	public String getHandCards2() {
		return handCards2;
	}

	public void setHandCards2(String handCards2) {
		this.handCards2 = handCards2;
	}

	public String getHandCards3() {
		return handCards3;
	}

	public void setHandCards3(String handCards3) {
		this.handCards3 = handCards3;
	}

	public int getPlayType() {
		return playType;
	}

	public void setPlayType(int playType) {
		this.playType = playType;
	}

	public int getEnterServer() {
		return enterServer;
	}

	public void setEnterServer(int enterServer) {
		this.enterServer = enterServer;
	}

	public long getCreateId() {
		return createId;
	}

	public void setCreateId(long createId) {
		this.createId = createId;
	}

	public long getWinId() {
		return winId;
	}

	public void setWinId(long winId) {
		this.winId = winId;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getExtend() {
		return extend;
	}

	public void setExtend(String extend) {
		this.extend = extend;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String getClosingRes() {
		return closingRes;
	}

	public void setClosingRes(String closingRes) {
		this.closingRes = closingRes;
	}

	public Date getClosingTime() {
		return closingTime;
	}

	public void setClosingTime(Date closingTime) {
		this.closingTime = closingTime;
	}

	public int getStartSeat() {
		return startSeat;
	}

	public void setStartSeat(int startSeat) {
		this.startSeat = startSeat;
	}

}
