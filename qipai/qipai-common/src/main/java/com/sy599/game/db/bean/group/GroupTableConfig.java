package com.sy599.game.db.bean.group;

import java.io.Serializable;
import java.util.Date;

public class GroupTableConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long keyId;
    private Long parentGroup;
    private Long groupId;
    private String tableName;
    private String tableMode;
    private String modeMsg;
    private Integer gameType;
    private Integer payType;
    private Integer gameCount;
    private Integer playerCount;
    private String descMsg;
    private String configState;
    private Integer tableOrder;
    private Long playCount;
    private Date createdTime;
    private String creditMsg;
    private String goldMsg;

    public Long getParentGroup() {
        return parentGroup;
    }

    public void setParentGroup(Long parentGroup) {
        this.parentGroup = parentGroup;
    }

    public String getConfigState() {
        return configState;
    }

    public void setConfigState(String configState) {
        this.configState = configState;
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

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTableMode() {
        return tableMode;
    }

    public void setTableMode(String tableMode) {
        this.tableMode = tableMode;
    }

    public String getModeMsg() {
        return modeMsg;
    }

    public void setModeMsg(String modeMsg) {
        this.modeMsg = modeMsg;
    }

    public Integer getGameType() {
        return gameType;
    }

    public void setGameType(Integer gameType) {
        this.gameType = gameType;
    }

    public Integer getPayType() {
        return payType;
    }

    public void setPayType(Integer payType) {
        this.payType = payType;
    }

    public Integer getGameCount() {
        return gameCount;
    }

    public void setGameCount(Integer gameCount) {
        this.gameCount = gameCount;
    }

    public Integer getPlayerCount() {
        return playerCount;
    }

    public void setPlayerCount(Integer playerCount) {
        this.playerCount = playerCount;
    }

    public String getDescMsg() {
        return descMsg;
    }

    public void setDescMsg(String descMsg) {
        this.descMsg = descMsg;
    }

    public Integer getTableOrder() {
        return tableOrder;
    }

    public void setTableOrder(Integer tableOrder) {
        this.tableOrder = tableOrder;
    }

    public Long getPlayCount() {
        return playCount;
    }

    public void setPlayCount(Long playCount) {
        this.playCount = playCount;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getCreditMsg() {
        return creditMsg;
    }

    public void setCreditMsg(String creditMsg) {
        this.creditMsg = creditMsg;
    }

    public String getGoldMsg() {
        return goldMsg;
    }

    public void setGoldMsg(String goldMsg) {
        this.goldMsg = goldMsg;
    }
}
