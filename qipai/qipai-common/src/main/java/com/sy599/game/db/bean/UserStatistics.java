package com.sy599.game.db.bean;

import java.text.SimpleDateFormat;
import java.util.Date;

public class UserStatistics {
    private long keyId;

    private String userId;
    private int winCount;
    private int loseCount;
    private int drawCount;
    private int userScore;
    private String roomType;//common,gold
    private long currentDate;//yyyyMMdd
    private int gameType;
    private int gameCount0;//小局总数
    private int gameCount1;//大局总数

    public UserStatistics() {

    }

    public UserStatistics(String userId, int userScore, String roomType, int gameType, int gameCount0) {
        this.userId = userId;
        this.userScore = userScore;

        if (userScore > 0) {
            this.winCount = 1;
        } else if (userScore < 0) {
            this.loseCount = 1;
        } else {
            this.drawCount = 1;
        }

        this.roomType = roomType;
        this.gameType = gameType;
        this.gameCount0 = gameCount0;
        this.gameCount1 = 1;
        this.currentDate = Long.parseLong(new SimpleDateFormat("yyyyMMdd").format(new Date()));
    }

    public long getKeyId() {
        return keyId;
    }

    public void setKeyId(long keyId) {
        this.keyId = keyId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getWinCount() {
        return winCount;
    }

    public void setWinCount(int winCount) {
        this.winCount = winCount;
    }

    public int getLoseCount() {
        return loseCount;
    }

    public void setLoseCount(int loseCount) {
        this.loseCount = loseCount;
    }

    public int getDrawCount() {
        return drawCount;
    }

    public void setDrawCount(int drawCount) {
        this.drawCount = drawCount;
    }

    public int getUserScore() {
        return userScore;
    }

    public void setUserScore(int userScore) {
        this.userScore = userScore;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public long getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(long currentDate) {
        this.currentDate = currentDate;
    }

    public int getGameType() {
        return gameType;
    }

    public void setGameType(int gameType) {
        this.gameType = gameType;
    }

    public int getGameCount0() {
        return gameCount0;
    }

    public void setGameCount0(int gameCount0) {
        this.gameCount0 = gameCount0;
    }

    public int getGameCount1() {
        return gameCount1;
    }

    public void setGameCount1(int gameCount1) {
        this.gameCount1 = gameCount1;
    }
}
