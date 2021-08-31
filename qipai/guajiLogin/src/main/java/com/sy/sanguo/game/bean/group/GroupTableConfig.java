package com.sy.sanguo.game.bean.group;

import java.io.Serializable;
import java.util.Date;

/**
 * 俱乐部房间配置
 */
public class GroupTableConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键(空房间索引)
     */
    private Long keyId;
    /**
     * 上级id
     */
    private Long parentGroup;
    /**
     * 军团id
     */
    private Long groupId;
    /**
     * 房间名
     */
    private String tableName;
    /**
     * 牌局模式  1普通房间 2比赛房间
     */
    private String tableMode;
    /**
     * 房间信息
     */
    private String modeMsg;
    /**
     * 游戏玩法
     */
    private Integer gameType;
    /**
     * 付费方式
     */
    private Integer payType;
    /**
     * 牌局数
     */
    private Integer gameCount;
    /**
     * 牌局人数上限
     */
    private Integer playerCount;
    /**
     * 信息简介
     */
    private String descMsg;
    /**
     * 配置是否有效
     */
    private String configState;
    /**
     * 排序（越小越靠前）
     */
    private Integer tableOrder;
    /**
     * 玩的数量
     */
    private Long playCount;
    /**
     * 创建时间
     */
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
