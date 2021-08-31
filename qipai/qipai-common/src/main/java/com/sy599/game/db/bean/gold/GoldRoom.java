package com.sy599.game.db.bean.gold;

import com.sy599.game.util.LogUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

public class GoldRoom {

    /*** 状态：新建***/
    public static final int STATE_NEW = 0;
    /*** 状态：准备好，可以开始匹配**/
    public static final int STATE_READY = 1;
    /*** 状态：已开局***/
    public static final int STATE_PLAYING = 2;
    /*** 状态：正常结束***/
    public static final int STATE_NORMAL_OVER = 3;


    private Long keyId;
    private Long tableId;
    private Integer groupIdLimit;
    private Long configId;
    private String modeId;
    private Integer currentCount;
    private Integer maxCount;
    private Integer serverId;
    private Integer currentState;
    private Integer gameCount;
    private String tableMsg;
    private String goldMsg;
    private String tableName;
    private Date createdTime;
    private Date modifiedTime;

    /*** 金币场：门票 ***/
    private long ticket = 0;
    /*** 金币场：加入分数限制 ***/
    private long joinLimit = 0;
    /*** 金币场：倍率 ***/
    private long rate = 1;

    public Long getKeyId() {
        return keyId;
    }

    public void setKeyId(Long keyId) {
        this.keyId = keyId;
    }

    public Long getTableId() {
        return tableId;
    }

    public void setTableId(Long tableId) {
        this.tableId = tableId;
    }

    public Integer getGroupIdLimit() {
        return groupIdLimit;
    }

    public void setGroupIdLimit(Integer groupIdLimit) {
        this.groupIdLimit = groupIdLimit;
    }

    public void setConfigId(Long configId) {
        this.configId = configId;
    }

    public Long getConfigId() {
        return configId;
    }

    public String getModeId() {
        return modeId;
    }

    public void setModeId(String modeId) {
        this.modeId = modeId;
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

    public Integer getServerId() {
        return serverId;
    }

    public void setServerId(Integer serverId) {
        this.serverId = serverId;
    }

    public Integer getCurrentState() {
        return currentState;
    }

    public void setCurrentState(Integer currentState) {
        this.currentState = currentState;
    }

    public String getTableMsg() {
        return tableMsg;
    }

    public void setTableMsg(String tableMsg) {
        this.tableMsg = tableMsg;
    }

    public String getGoldMsg() {
        return goldMsg;
    }

    public void setGoldMsg(String goldMsg) {
        this.goldMsg = goldMsg;
        initGoldMsg();
    }

    public Integer getGameCount() {
        return gameCount;
    }

    public void setGameCount(Integer gameCount) {
        this.gameCount = gameCount;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Date modifiedTime) {
        this.modifiedTime = modifiedTime;
    }


    // 以下与db无关
    public boolean isFull() {
        return currentCount >= maxCount;
    }

    public boolean isNotStart() {
        return currentState == STATE_NEW || currentState == STATE_READY;
    }

    public boolean canStart() {
        return isFull() && isNotStart();
    }

    public boolean isPlaying() {
        return currentState == STATE_PLAYING;
    }

    public boolean isNormalOver() {
        return currentState == STATE_NORMAL_OVER;
    }


    public boolean isOver() {
        return isNormalOver();
    }


    public int loadMatchRatio() {
        return 0;
    }

    public long getTicket() {
        return ticket;
    }

    public long getJoinLimit() {
        return joinLimit;
    }

    public long getRate() {
        return rate;
    }

    public void initGoldMsg() {
        try {
            if (StringUtils.isNotBlank(goldMsg)) {
                String[] splits = goldMsg.split(",");
                ticket = Long.valueOf(splits[0]);
                joinLimit = Long.valueOf(splits[1]);
                rate = Long.valueOf(splits[2]);
            }
        } catch (Exception e) {
            LogUtil.errorLog.error("" + keyId, e);
        }
    }
}
