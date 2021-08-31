package com.sy599.game.db.bean;

import java.io.Serializable;
import java.util.Date;

public class UserPlaylog implements Serializable{
	private long id;
	private long userId;
	private long logId;
	private long tableId;
	private int count;
	private Date time;
	private String res;
	private int totalCount;
	private int startseat;
	private String outCards;
	private String extend;
	/** 最大房间人数**/
	private int maxPlayerCount;

	private int type;

	private String generalExt;

	public int getMaxPlayerCount() {
		return maxPlayerCount;
	}

	public void setMaxPlayerCount(int maxPlayerCount) {
		this.maxPlayerCount = maxPlayerCount;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public long getLogId() {
		return logId;
	}

	public void setLogId(long logId) {
		this.logId = logId;
	}

	public long getTableId() {
		return tableId;
	}

	public void setTableId(long tableId) {
		this.tableId = tableId;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public String getRes() {
		return res;
	}

	public void setRes(String res) {
		this.res = res;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	public int getStartseat() {
		return startseat;
	}

	public void setStartseat(int startseat) {
		this.startseat = startseat;
	}

	public String getOutCards() {
		return outCards;
	}

	public void setOutCards(String outCards) {
		this.outCards = outCards;
	}

	public String getExtend() {
		return extend;
	}

	public void setExtend(String extend) {
		this.extend = extend;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

    public String getGeneralExt() {
        return generalExt;
    }

    public void setGeneralExt(String generalExt) {
        this.generalExt = generalExt;
    }
}
