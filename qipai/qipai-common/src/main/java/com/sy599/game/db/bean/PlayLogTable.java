package com.sy599.game.db.bean;

import java.io.Serializable;
import java.util.Date;

public class PlayLogTable implements Serializable {

    private long keyId;
    private long tableId;
    private long creatorId;
    private long groupId;
    private long groupMasterId;
    private int playerCount;
    private String players;
    private String scores;
    private Integer totalCount;
    private Integer finishCount;
    private String tableMsg;
    private Date overTime;
    private Date createTime;

    public long getKeyId() {
        return keyId;
    }

    public void setKeyId(long keyId) {
        this.keyId = keyId;
    }

    public long getTableId() {
        return tableId;
    }

    public void setTableId(long tableId) {
        this.tableId = tableId;
    }

    public long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(long creatorId) {
        this.creatorId = creatorId;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public long getGroupMasterId() {
        return groupMasterId;
    }

    public void setGroupMasterId(long groupMasterId) {
        this.groupMasterId = groupMasterId;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public void setPlayerCount(int playerCount) {
        this.playerCount = playerCount;
    }

    public String getPlayers() {
        return players;
    }

    public void setPlayers(String players) {
        this.players = players;
    }

    public String getScores() {
        return scores;
    }

    public void setScores(String scores) {
        this.scores = scores;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getFinishCount() {
        return finishCount;
    }

    public void setFinishCount(Integer finishCount) {
        this.finishCount = finishCount;
    }

    public String getTableMsg() {
        return tableMsg;
    }

    public void setTableMsg(String tableMsg) {
        this.tableMsg = tableMsg;
    }

    public Date getOverTime() {
        return overTime;
    }

    public void setOverTime(Date overTime) {
        this.overTime = overTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
