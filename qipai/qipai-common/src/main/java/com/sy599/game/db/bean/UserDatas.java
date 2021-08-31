package com.sy599.game.db.bean;

import java.util.Date;

/**
 * 玩家数据（eg：最高连胜、当前连胜）
 */
public class UserDatas {
    private long keyId;
    private String userId;
    private String gameCode;
    private String roomType;
    private String dataCode;
    private String dataValue;
    private Date createdTime;

    public UserDatas() {

    }

    public UserDatas(long userId, String gameCode, String roomType, String dataCode, String dataValue) {
        this.userId = String.valueOf(userId);
        this.gameCode = gameCode;
        this.roomType = roomType;
        this.dataCode = dataCode;
        this.dataValue = dataValue;
        this.createdTime = new Date();
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

    public String getGameCode() {
        return gameCode;
    }

    public void setGameCode(String gameCode) {
        this.gameCode = gameCode;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public String getDataCode() {
        return dataCode;
    }

    public void setDataCode(String dataCode) {
        this.dataCode = dataCode;
    }

    public String getDataValue() {
        return dataValue;
    }

    public void setDataValue(String dataValue) {
        this.dataValue = dataValue;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }
}
