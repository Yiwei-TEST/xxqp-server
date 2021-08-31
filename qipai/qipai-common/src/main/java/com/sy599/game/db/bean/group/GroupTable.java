package com.sy599.game.db.bean.group;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sy599.game.db.bean.RankInfo;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Date;

public class GroupTable implements Serializable, Comparable<GroupTable>{

    private static final long serialVersionUID = 1L;

    /*** 类型：普通房****/
    public static final int type_normal = 1;
    /*** 类型：信用房****/
    public static final int type_credit = 2;
    /*** 类型：金币房****/
    public static final int type_gold = 3;

    private Long keyId;
    private Long groupId;
    private Long configId;
    private Integer tableId;
    private String tableName;
    private String tableMsg;
    private String serverId;
    private Date createdTime;
    private String currentState;
    private Integer currentCount;
    private Integer maxCount;
    private Integer playedBureau = 0;
    private String players;
    private Date overTime;
    private String userId;
    private Integer type;
    private Integer dealCount = 0;
    private String creditMsg;
    private Integer playType;
    private String payMsg;
    private Integer isPrivate;
    private String goldMsg;

    private JSONObject tableJson;

    public Integer getDealCount() {
        return dealCount;
    }

    public void setDealCount(Integer dealCount) {
        this.dealCount = dealCount;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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
        if (StringUtils.isNotBlank(tableMsg)&&tableMsg.startsWith("{")&&tableMsg.endsWith("}")){
            this.tableJson = JSON.parseObject(tableMsg);
        }
    }

    /**
     * 获取包厢ID
     * @return
     */
    public int loadGroupRoom(){
        return tableJson!=null?tableJson.getIntValue("room"):0;
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

    public Date getOverTime() {
        return overTime;
    }

    public void setOverTime(Date overTime) {
        this.overTime = overTime;
    }

    public Integer getCurrentCount() {
        return currentCount;
    }

    public void setCurrentCount(Integer currentCount) {
        this.currentCount = currentCount;
    }

    public int changeCurrentCount(int count){
        synchronized (this){
            if (this.currentCount == null){
                this.currentCount = count;
            }else{
                this.currentCount += count;
            }
        }
        return this.currentCount;
    }

    public Integer getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(Integer maxCount) {
        this.maxCount = maxCount;
    }

    public boolean isNotStart() {
        return "0".equals(currentState);
    }

    public boolean isPlaying() {
        return "1".equals(currentState);
    }

    public boolean isNormalOver() {
        return "2".equals(currentState);
    }

    public boolean isZeroOver() {
        return "3".equals(currentState);
    }

    public boolean isMidwayOver() {
        return "4".equals(currentState);
    }

    public boolean isOver() {
        return isNormalOver() || isZeroOver() || isMidwayOver();
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getCreditMsg() {
        return creditMsg;
    }

    public void setCreditMsg(String creditMsg) {
        this.creditMsg = creditMsg;
    }

    public Integer getPlayType() {
        return playType;
    }

    public void setPlayType(Integer playType) {
        this.playType = playType;
    }

    public String getPayMsg() {
        return payMsg;
    }

    public void setPayMsg(String payMsg) {
        this.payMsg = payMsg;
    }

    public Integer getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(Integer isPrivate) {
        this.isPrivate = isPrivate;
    }

    public String getGoldMsg() {
        return goldMsg;
    }

    public void setGoldMsg(String goldMsg) {
        this.goldMsg = goldMsg;
    }

    @Override
    public int compareTo(GroupTable o) {
        int order1 = 0;
        int order2 = 0;

        if (this.currentCount < this.maxCount && this.currentCount > 0) {
            order1 = 3;
        } else if (this.currentCount == 0) {
            order1 = 1;
        } else {
            order1 = 2;
        }
        if (o.currentCount < o.maxCount && o.currentCount > 0) {
            order2 = 3;
        } else if (o.currentCount == 0) {
            order2 = 1;
        } else {
            order2 = 2;
        }
        if (order1 > order2)
            return -1;
        else {
            return 1;
        }

    }
}
