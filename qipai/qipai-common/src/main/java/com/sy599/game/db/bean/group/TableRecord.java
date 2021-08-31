package com.sy599.game.db.bean.group;

import java.io.Serializable;
import java.util.Date;

public class TableRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long keyId;
    private Long groupId;
    private Long tableNo;
    private String modeMsg;
    private Integer tableId;
    private Date createdTime;
    private Integer playNo;
    private Integer recordType;
    private String initMsg;
    private String resultMsg;
    private String logId;

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public Long getKeyId() {
        return keyId;
    }

    public void setKeyId(Long keyId) {
        this.keyId = keyId;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getTableNo() {
        return tableNo;
    }

    public void setTableNo(Long tableNo) {
        this.tableNo = tableNo;
    }

    public String getModeMsg() {
        return modeMsg;
    }

    public void setModeMsg(String modeMsg) {
        this.modeMsg = modeMsg;
    }

    public Integer getTableId() {
        return tableId;
    }

    public void setTableId(Integer tableId) {
        this.tableId = tableId;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
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

    public String getInitMsg() {
        return initMsg;
    }

    public void setInitMsg(String initMsg) {
        this.initMsg = initMsg;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }
}
