package com.sy.sanguo.game.bean;

import com.sy.sanguo.game.bean.enums.SourceType;

import java.io.Serializable;

public class GoldDataStatistics implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long keyId;
    private Long dataDate;
    private Integer dataType;
    private Long userId;
    private Integer dataCount;
    private Long dataValue;

    public GoldDataStatistics(Long dataDate, int sourceType, Long userId, Integer dataCount, Long dataValue) {
        this.dataDate = dataDate;
        this.dataType = sourceType;
        this.userId = userId;
        this.dataCount = dataCount;
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

    public Integer getDataType() {
        return dataType;
    }

    public void setDataType(Integer dataType) {
        this.dataType = dataType;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getDataCount() {
        return dataCount;
    }

    public void setDataCount(Integer dataCount) {
        this.dataCount = dataCount;
    }

    public Long getDataValue() {
        return dataValue;
    }

    public void setDataValue(Long dataValue) {
        this.dataValue = dataValue;
    }
}
