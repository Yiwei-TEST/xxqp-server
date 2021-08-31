package com.sy599.game.db.bean;

import java.util.Date;

public class SoloRoomConfig {

    public static final int STATE_VALID = 1;
    public static final int STATE_UNVALID = 2;

    public static final int TYPE_GOLD = 1;

    private Long keyId;
    private Integer soloType;
    private Integer state;
    private Integer playType;
    private String name;
    private Integer playerCount;
    private Integer totalBureau;
    private String tableMsg;
    private Integer order;
    private Date createdTime;
    private Date lastUpTime;

    public Long getKeyId() {
        return keyId;
    }

    public void setKeyId(Long keyId) {
        this.keyId = keyId;
    }

    public Integer getSoloType() {
        return soloType;
    }

    public void setSoloType(Integer soloType) {
        this.soloType = soloType;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Integer getPlayType() {
        return playType;
    }

    public void setPlayType(Integer playType) {
        this.playType = playType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPlayerCount() {
        return playerCount;
    }

    public void setPlayerCount(Integer playerCount) {
        this.playerCount = playerCount;
    }

    public Integer getTotalBureau() {
        return totalBureau;
    }

    public void setTotalBureau(Integer totalBureau) {
        this.totalBureau = totalBureau;
    }

    public String getTableMsg() {
        return tableMsg;
    }

    public void setTableMsg(String tableMsg) {
        this.tableMsg = tableMsg;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getLastUpTime() {
        return lastUpTime;
    }

    public void setLastUpTime(Date lastUpTime) {
        this.lastUpTime = lastUpTime;
    }

    public boolean isValid(){
        return state == STATE_VALID;
    }
}
