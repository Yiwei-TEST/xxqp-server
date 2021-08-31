package com.sy599.game.db.bean.group;

import java.io.Serializable;
import java.util.Date;

public class TableUser implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long keyId;
    private Long groupId;
    private Long tableNo;
    private Integer tableId;
    private Long userId;
    private Integer playResult;
    private Date createdTime;
    private String gpSeq;

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

    public Integer getTableId() {
        return tableId;
    }

    public void setTableId(Integer tableId) {
        this.tableId = tableId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getPlayResult() {
        return playResult;
    }

    public void setPlayResult(Integer playResult) {
        this.playResult = playResult;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getGpSeq() {
        return gpSeq;
    }

    public void setGpSeq(String gpSeq) {
        this.gpSeq = gpSeq;
    }
}
