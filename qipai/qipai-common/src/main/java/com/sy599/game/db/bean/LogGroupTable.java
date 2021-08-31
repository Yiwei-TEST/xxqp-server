package com.sy599.game.db.bean;

import java.io.Serializable;

public class LogGroupTable implements Serializable {

    private static final long serialVersionUID = 1L;

    private long keyId;
    private long dataDate;
    private long groupId;
    private long userGroup;
    private int gameType;
    private int bureau;
    private long userId;
    private int player2Count1;
    private int player2Count2;
    private int player2Count3;
    private int player3Count1;
    private int player3Count2;
    private int player3Count3;
    private int player4Count1;
    private int player4Count2;
    private int player4Count3;
    private int dyjCount;

    public LogGroupTable(Long dataDate, long groupId, int gameType, int bureau) {
        this.dataDate = dataDate;
        this.groupId = groupId;
        this.gameType = gameType;
        this.bureau = bureau;
    }

    public long getKeyId() {
        return keyId;
    }

    public void setKeyId(long keyId) {
        this.keyId = keyId;
    }

    public long getDataDate() {
        return dataDate;
    }

    public void setDataDate(long dataDate) {
        this.dataDate = dataDate;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public long getUserGroup() {
        return userGroup;
    }

    public void setUserGroup(long userGroup) {
        this.userGroup = userGroup;
    }

    public int getGameType() {
        return gameType;
    }

    public void setGameType(int gameType) {
        this.gameType = gameType;
    }

    public int getBureau() {
        return bureau;
    }

    public void setBureau(int bureau) {
        this.bureau = bureau;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getPlayer2Count1() {
        return player2Count1;
    }

    public void setPlayer2Count1(int player2Count1) {
        this.player2Count1 = player2Count1;
    }

    public int getPlayer2Count2() {
        return player2Count2;
    }

    public void setPlayer2Count2(int player2Count2) {
        this.player2Count2 = player2Count2;
    }

    public int getPlayer2Count3() {
        return player2Count3;
    }

    public void setPlayer2Count3(int player2Count3) {
        this.player2Count3 = player2Count3;
    }

    public int getPlayer3Count1() {
        return player3Count1;
    }

    public void setPlayer3Count1(int player3Count1) {
        this.player3Count1 = player3Count1;
    }

    public int getPlayer3Count2() {
        return player3Count2;
    }

    public void setPlayer3Count2(int player3Count2) {
        this.player3Count2 = player3Count2;
    }

    public int getPlayer3Count3() {
        return player3Count3;
    }

    public void setPlayer3Count3(int player3Count3) {
        this.player3Count3 = player3Count3;
    }

    public int getPlayer4Count1() {
        return player4Count1;
    }

    public void setPlayer4Count1(int player4Count1) {
        this.player4Count1 = player4Count1;
    }

    public int getPlayer4Count2() {
        return player4Count2;
    }

    public void setPlayer4Count2(int player4Count2) {
        this.player4Count2 = player4Count2;
    }

    public int getPlayer4Count3() {
        return player4Count3;
    }

    public void setPlayer4Count3(int player4Count3) {
        this.player4Count3 = player4Count3;
    }

    public int getDyjCount() {
        return dyjCount;
    }

    public void setDyjCount(int dyjCount) {
        this.dyjCount = dyjCount;
    }
}
