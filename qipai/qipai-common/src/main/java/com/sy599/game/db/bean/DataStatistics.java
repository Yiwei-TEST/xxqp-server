package com.sy599.game.db.bean;

import java.io.Serializable;

public class DataStatistics implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long keyId;
    private Long dataDate;
    private String dataCode;
    private String userId;
    private String gameType;
    private String dataType;
    private Integer dataValue;

    public DataStatistics() {

    }

    public DataStatistics(Long dataDate, String dataCode, String userId, String gameType, String dataType, int dataValue) {
        this.dataDate = dataDate;
        this.dataCode = dataCode;
        this.userId = userId;
        this.gameType = gameType;
        this.dataType = dataType;
        this.dataValue = dataValue;
    }

    public Long getKeyId() {
        return keyId;
    }

    public void setKeyId(Long keyId) {
        this.keyId = keyId;
    }

    public Long getDataDate() {
        return dataDate;
    }

    public void setDataDate(Long dataDate) {
        this.dataDate = dataDate;
    }

    public String getDataCode() {
        return dataCode;
    }

    public void setDataCode(String dataCode) {
        this.dataCode = dataCode;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGameType() {
        return gameType;
    }

    public void setGameType(String gameType) {
        this.gameType = gameType;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public Integer getDataValue() {
        return dataValue;
    }

    public void setDataValue(Integer dataValue) {
        this.dataValue = dataValue;
    }
}
