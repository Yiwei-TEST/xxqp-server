package com.sy599.game.gold;

import java.io.Serializable;
import java.util.Date;

public class GoldRoomTableRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long keyId;
    private Long goldRoomId;
    private Long tableId;
    private Integer playNo;
    private Integer recordType;
    private String resultMsg;
    private Long logId;
    private Date createdTime;

    public Long getKeyId() {
        return keyId;
    }

    public void setKeyId(Long keyId) {
        this.keyId = keyId;
    }

    public Long getGoldRoomId() {
        return goldRoomId;
    }

    public void setGoldRoomId(Long goldRoomId) {
        this.goldRoomId = goldRoomId;
    }

    public Long getTableId() {
        return tableId;
    }

    public void setTableId(Long tableId) {
        this.tableId = tableId;
    }

    public Integer getPlayNo() {
        return playNo;
    }

    public void setPlayNo(Integer playNo) {
        this.playNo = playNo;
    }

    public Integer getRecordType() {
        return recordType;
    }

    public void setRecordType(Integer recordType) {
        this.recordType = recordType;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }

    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }
}
