package com.sy.sanguo.game.pdkuai.db.bean;

import java.util.Date;
import java.util.List;

import com.sy.sanguo.game.msg.UserPlayMsg;

public class DaikaiTable {

	private long tableId;
	
	private long daikaiId;
	
	private int serverId;
	
	private int playType;
	
	private int needCard;
	
	private int state;
	
	private int createFlag;
	
	private String createPara;
	
	private String createStrPara;
	
	private Date createTime;
	
	private Date daikaiTime;
	
	private int returnFlag;
	
	private String playerInfo;
	
	private List<UserPlayMsg> playerMsg;
	
	public List<UserPlayMsg> getPlayerMsg() {
		return playerMsg;
	}

	public void setPlayerMsg(List<UserPlayMsg> playerMsg) {
		this.playerMsg = playerMsg;
	}

	public String getPlayerInfo() {
		return playerInfo;
	}

	public void setPlayerInfo(String playerInfo) {
		this.playerInfo = playerInfo;
	}

	private String extend;

	public long getTableId() {
		return tableId;
	}

	public void setTableId(long tableId) {
		this.tableId = tableId;
	}

	public long getDaikaiId() {
		return daikaiId;
	}

	public void setDaikaiId(long daikaiId) {
		this.daikaiId = daikaiId;
	}

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public int getPlayType() {
		return playType;
	}

	public void setPlayType(int playType) {
		this.playType = playType;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getCreatePara() {
		return createPara;
	}

	public void setCreatePara(String createPara) {
		this.createPara = createPara;
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

	public String getCreateStrPara() {
		return createStrPara;
	}

	public void setCreateStrPara(String createStrPara) {
		this.createStrPara = createStrPara;
	}

	public int getNeedCard() {
		return needCard;
	}

	public void setNeedCard(int needCard) {
		this.needCard = needCard;
	}

	public int getCreateFlag() {
		return createFlag;
	}

	public void setCreateFlag(int createFlag) {
		this.createFlag = createFlag;
	}

	public Date getDaikaiTime() {
		return daikaiTime;
	}

	public void setDaikaiTime(Date daikaiTime) {
		this.daikaiTime = daikaiTime;
	}

	public int getReturnFlag() {
		return returnFlag;
	}

	public void setReturnFlag(int returnFlag) {
		this.returnFlag = returnFlag;
	}
	
}
