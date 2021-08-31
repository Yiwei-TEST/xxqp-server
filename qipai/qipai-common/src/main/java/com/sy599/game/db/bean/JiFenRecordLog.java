package com.sy599.game.db.bean;

import java.util.Date;

/**
 * 芒果跑得快积分获取的操作日志
 */
public class JiFenRecordLog {

    /**
     * 日志自增ID
     */
    private long id;

    /**
     * 玩家id
     */
    private long userId;

    /**
     * 获得积分数
     */
    private int jifen;

    /**
     * 获得积分来源
     */
    private int sourceType;

    /**
     * 获得时间
     */
    private Date createTime;

    public JiFenRecordLog(long userId, int jifen, int sourceType, Date createTime) {
        this.userId = userId;
        this.jifen = jifen;
        this.sourceType = sourceType;
        this.createTime = createTime;
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

    public int getJifen() {
        return jifen;
    }

    public void setJifen(int jifen) {
        this.jifen = jifen;
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
}
