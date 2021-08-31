package com.sy.sanguo.game.bean.gold;

import java.util.Date;

public class GoldRoomUser {
    private Long keyId;
    private Long roomId;
    private String userId;
    private Date createdTime;
    private Integer gameResult;
    private String logIds;

    public Long getKeyId() {
        return keyId;
    }

    public void setKeyId(Long keyId) {
        this.keyId = keyId;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Integer getGameResult() {
        return gameResult;
    }

    public void setGameResult(Integer gameResult) {
        this.gameResult = gameResult;
    }

    public String getLogIds() {
        return logIds;
    }

    public void setLogIds(String logIds) {
        this.logIds = logIds;
    }
}
