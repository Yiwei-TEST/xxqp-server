package com.sy599.game.db.bean;

import com.sy599.game.db.enums.SourceType;

import java.util.Date;

/**
 * 玩家房卡来源日志
 */
public class UserGoldRecord {

    /**
     * 自增ID
     */
    private long id;

    /**
     * 玩家ID
     */
    private long userId;

    /**
     * 玩家当前免费房卡数
     */
    private long freeGold;

    /**
     * 玩家当前房卡数
     */
    private long gold;

    /**
     * 玩家本次操作(消耗/获得)免费房卡数
     */
    private long addFreeGold;

    /**
     * 玩家本次操作(消耗/获得)免费房卡数
     */
    private long addGold;

    /**
     * 操作类型(1消耗  0获得)
     */
    private int recordType;

    /**
     * 操作所属玩法ID 0表示不属于玩法类操作
     */
    private int playType;

    /**
     * 操作来源
     */
    private int sourceType;

    /**
     * 操作时间
     */
    private Date createTime;

    public UserGoldRecord() {
    }

    public UserGoldRecord(long userId, long freeGold, long gold, long addFreeGold, long addGold, int playType, SourceType type) {
        this.recordType = (addFreeGold + addGold > 0) ? 1 : 0;
        this.userId = userId;
        this.freeGold = freeGold;
        this.gold = gold;
        this.addFreeGold = addFreeGold;
        this.addGold = addGold;
        this.playType = playType;
        this.sourceType = type.type();
        this.createTime = new Date();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getFreeGold() {
        return freeGold;
    }

    public void setFreeGold(long freeGold) {
        this.freeGold = freeGold;
    }

    public long getGold() {
        return gold;
    }

    public void setGold(long gold) {
        this.gold = gold;
    }

    public long getAddFreeGold() {
        return addFreeGold;
    }

    public void setAddFreeGold(long addFreeGold) {
        this.addFreeGold = addFreeGold;
    }

    public long getAddGold() {
        return addGold;
    }

    public void setAddGold(long addGold) {
        this.addGold = addGold;
    }

    public int getRecordType() {
        return recordType;
    }

    public void setRecordType(int recordType) {
        this.recordType = recordType;
    }

    public int getPlayType() {
        return playType;
    }

    public void setPlayType(int playType) {
        this.playType = playType;
    }

    public int getSourceType() {
        return sourceType;
    }

    public void setSourceType(int sourceType) {
        this.sourceType = sourceType;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }


    // --------------以下为非数据库字段
    private String userSeq;
    public String getUserSeq() {
        return userSeq;
    }

    public void setUserSeq(String userSeq) {
        this.userSeq = userSeq;
    }
}
