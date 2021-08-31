package com.sy599.game.db.bean;

import java.util.Date;

public class TableInf {
	private long tableId;
	private int tableType;
	private int roomId;
	private int serverId;
	private long masterId;
	private String players;
	private int totalBureau;
	private int playBureau;
	private String outPai1;
	private String outPai2;
	private String outPai3;
	private String outPai4;
	private String outPai5;
	private String outPai6;
	private String outPai7;
	private String outPai8;
	private String outPai9;
	private String outPai10;
	private String handPai1;
	private String handPai2;
	private String handPai3;
	private String handPai4;
	private String handPai5;
	private String handPai6;
	private String handPai7;
	private String handPai8;
	private String handPai9;
	private String handPai10;
	private Date createTime;
	private int state;
	private String answerDiss;
	private int nowDisCardSeat;
	private String nowDisCardIds;
	private int disCardRound;
	private int disCardSeat;
	private int lastWinSeat;
	private String nowAction;
	private int playType;
	private String leftPais;
	private String extend;
	private String playLog;
	private String config;
	private long gotyeRoomId;
	private long lastActionTime;
	private int isCompetition;
	private long daikaiTableId;
	private int finishBureau;

    public int getTableType() {
        return tableType;
    }

    public void setTableType(int tableType) {
        this.tableType = tableType;
    }

    public String getOutPai9() {
		return outPai9;
	}

	public void setOutPai9(String outPai9) {
		this.outPai9 = outPai9;
	}

	public String getOutPai10() {
		return outPai10;
	}

	public void setOutPai10(String outPai10) {
		this.outPai10 = outPai10;
	}

	public String getHandPai9() {
		return handPai9;
	}

	public void setHandPai9(String handPai9) {
		this.handPai9 = handPai9;
	}

	public String getHandPai10() {
		return handPai10;
	}

	public void setHandPai10(String handPai10) {
		this.handPai10 = handPai10;
	}

	public String getOutPai6() {
		return outPai6;
	}

	public void setOutPai6(String outPai6) {
		this.outPai6 = outPai6;
	}

	public String getOutPai7() {
		return outPai7;
	}

	public void setOutPai7(String outPai7) {
		this.outPai7 = outPai7;
	}

	public String getOutPai8() {
		return outPai8;
	}

	public void setOutPai8(String outPai8) {
		this.outPai8 = outPai8;
	}

	public String getHandPai6() {
		return handPai6;
	}

	public void setHandPai6(String handPai6) {
		this.handPai6 = handPai6;
	}

	public String getHandPai7() {
		return handPai7;
	}

	public void setHandPai7(String handPai7) {
		this.handPai7 = handPai7;
	}

	public String getHandPai8() {
		return handPai8;
	}

	public void setHandPai8(String handPai8) {
		this.handPai8 = handPai8;
	}

	public int getDisCardRound() {
		return disCardRound;
	}

	public void setDisCardRound(int disCardRound) {
		this.disCardRound = disCardRound;
	}

	public int getDisCardSeat() {
		return disCardSeat;
	}

	public void setDisCardSeat(int disCardSeat) {
		this.disCardSeat = disCardSeat;
	}

	public long getTableId() {
		return tableId;
	}

	public void setTableId(long tableId) {
		this.tableId = tableId;
	}

	public int getRoomId() {
		return roomId;
	}

	public void setRoomId(int roomId) {
		this.roomId = roomId;
	}

	public String getPlayers() {
		return players;
	}

	public void setPlayers(String players) {
		this.players = players;
	}

	public int getTotalBureau() {
		return totalBureau;
	}

	public void setTotalBureau(int totalBureau) {
		this.totalBureau = totalBureau;
	}

	public int getPlayBureau() {
		return playBureau;
	}

	public void setPlayBureau(int playBureau) {
		this.playBureau = playBureau;
	}

	public String getOutPai1() {
		return outPai1;
	}

	public void setOutPai1(String outPai1) {
		this.outPai1 = outPai1;
	}

	public String getOutPai2() {
		return outPai2;
	}

	public void setOutPai2(String outPai2) {
		this.outPai2 = outPai2;
	}

	public String getOutPai3() {
		return outPai3;
	}

	public void setOutPai3(String outPai3) {
		this.outPai3 = outPai3;
	}

	public String getHandPai1() {
		return handPai1;
	}

	public void setHandPai1(String handPai1) {
		this.handPai1 = handPai1;
	}

	public String getHandPai2() {
		return handPai2;
	}

	public void setHandPai2(String handPai2) {
		this.handPai2 = handPai2;
	}

	public String getHandPai3() {
		return handPai3;
	}

	public void setHandPai3(String handPai3) {
		this.handPai3 = handPai3;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public long getMasterId() {
		return masterId;
	}

	public void setMasterId(long masterId) {
		this.masterId = masterId;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getAnswerDiss() {
		return answerDiss;
	}

	public void setAnswerDiss(String answerDiss) {
		this.answerDiss = answerDiss;
	}

	public String getNowDisCardIds() {
		return nowDisCardIds;
	}

	public void setNowDisCardIds(String nowDisCardIds) {
		this.nowDisCardIds = nowDisCardIds;
	}

	public int getNowDisCardSeat() {
		return nowDisCardSeat;
	}

	public void setNowDisCardSeat(int nowDisCardSeat) {
		this.nowDisCardSeat = nowDisCardSeat;
	}

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public int getLastWinSeat() {
		return lastWinSeat;
	}

	public void setLastWinSeat(int lastWinSeat) {
		this.lastWinSeat = lastWinSeat;
	}

	public String getExtend() {
		return extend;
	}

	public void setExtend(String extend) {
		this.extend = extend;
	}

	public int getPlayType() {
		return playType;
	}

	public void setPlayType(int playType) {
		this.playType = playType;
	}

	public String getNowAction() {
		return nowAction;
	}

	public void setNowAction(String nowAction) {
		this.nowAction = nowAction;
	}

	public String getOutPai4() {
		return outPai4;
	}

	public void setOutPai4(String outPai4) {
		this.outPai4 = outPai4;
	}

	public String getHandPai4() {
		return handPai4;
	}

	public void setHandPai4(String handPai4) {
		this.handPai4 = handPai4;
	}

	public String getLeftPais() {
		return leftPais;
	}

	public void setLeftPais(String leftPais) {
		this.leftPais = leftPais;
	}

	public String getPlayLog() {
		return playLog;
	}

	public void setPlayLog(String playLog) {
		this.playLog = playLog;
	}

	public String getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = config;
	}

	public long getGotyeRoomId() {
		return gotyeRoomId;
	}

	public void setGotyeRoomId(long gotyeRoomId) {
		this.gotyeRoomId = gotyeRoomId;
	}

	public long getLastActionTime() {
		return lastActionTime;
	}

	public void setLastActionTime(long lastActionTime) {
		this.lastActionTime = lastActionTime;
	}

	public int getIsCompetition() {
		return isCompetition;
	}

	public void setIsCompetition(int isCompetition) {
		this.isCompetition = isCompetition;
	}

	public String getOutPai5() {
		return outPai5;
	}

	public void setOutPai5(String outPai5) {
		this.outPai5 = outPai5;
	}

	public String getHandPai5() {
		return handPai5;
	}

	public void setHandPai5(String handPai5) {
		this.handPai5 = handPai5;
	}

	public long getDaikaiTableId() {
		return daikaiTableId;
	}

	public void setDaikaiTableId(long daikaiTableId) {
		this.daikaiTableId = daikaiTableId;
	}

	public int getFinishBureau() {
		return finishBureau;
	}

	public void setFinishBureau(int finishBureau) {
		this.finishBureau = finishBureau;
	}
}
