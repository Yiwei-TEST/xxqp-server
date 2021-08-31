package com.sy599.game.db.bean.gold;

import java.util.Date;

public class GoldRoomUser {

    private Long keyId;
    private Long roomId;
    private Long groupId;
    private Long userId;
    private Date createdTime;
    private Long gameResult;
    private String logIds;

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

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Long getGameResult() {
        return gameResult;
    }

    public void setGameResult(Long gameResult) {
        this.gameResult = gameResult;
    }

    public String getLogIds() {
        return logIds;
    }

    public void setLogIds(String logIds) {
        this.logIds = logIds;
    }
}
