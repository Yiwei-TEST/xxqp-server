package com.sy.sanguo.game.bean.group;

import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;
import java.util.Date;

/**
 * 俱乐部房间信息
 */
public class GroupTable implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long keyId;
    /**
     * 俱乐部ID
     */
    private Long groupId;
    /**
     * 牌局配置id
     */
    private Long configId;
    /**
     * 房间id
     */
    private Integer tableId;
    /**
     * 房间信息
     */
    private String tableMsg;
    /**
     * 服ID
     */
    private String serverId;
    /**
     * 创建时间
     */
    private Date createdTime;
    /**
     * 牌局状态（0：未开始，1：已开始，2已结束）
     */
    private String currentState;
    /**
     * 当前人数
     */
    private Integer currentCount;
    /**
     * 人数上限
     */
    private Integer maxCount;
    /**
     * 实际打的局数
     */
    private Integer playedBureau;
    /**
     * 玩家的名称
     */
    private String players;
    /**
     * 结束时间
     */
    private Date overTime;
    /**
     * 房主ID
     */
    private String userId;
    /**
     * 发牌次数
     */
    private Integer dealCount;
    /**
     * 房间名
     */
    private String tableName;

    public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public Integer getDealCount() {
        return dealCount;
    }

    public void setDealCount(Integer dealCount) {
        this.dealCount = dealCount;
    }

    public Date getOverTime() {
        return overTime;
    }

    public void setOverTime(Date overTime) {
        this.overTime = overTime;
    }

    public Integer getPlayedBureau() {
        return playedBureau;
    }

    public void setPlayedBureau(Integer playedBureau) {
        this.playedBureau = playedBureau;
    }

    public String getPlayers() {
        return players;
    }

    public void setPlayers(String players) {
        this.players = players;
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

    public Long getConfigId() {
        return configId;
    }

    public void setConfigId(Long configId) {
        this.configId = configId;
    }

    public Integer getTableId() {
        return tableId;
    }

    public void setTableId(Integer tableId) {
        this.tableId = tableId;
    }

    public String getTableMsg() {
        return tableMsg;
    }

    public void setTableMsg(String tableMsg) {
        this.tableMsg = tableMsg;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getCurrentState() {
        return currentState;
    }

    public void setCurrentState(String currentState) {
        this.currentState = currentState;
    }

    public Integer getCurrentCount() {
        return currentCount;
    }

    public void setCurrentCount(Integer currentCount) {
        this.currentCount = currentCount;
    }

    public Integer getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(Integer maxCount) {
        this.maxCount = maxCount;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public JSONObject getJsonObj() {
        JSONObject jo = new JSONObject();
        jo.put("configId", configId);// 房间索引号
        jo.put("tableId", tableId);// 房间号
        jo.put("tableMsg", tableMsg);// 玩法信息
        jo.put("serverId", serverId);// 服务器ID
        jo.put("createdTime", createdTime);// 创建时间
        jo.put("currentState", currentState);// 当前房间状态 0：未开始，1：已开始，2已结束
        jo.put("maxCount", maxCount);// 人数上限
        jo.put("playedBureau", playedBureau);// 实际打的局数
        jo.put("userId", userId);// 房主ID
        return jo;
    }
}
