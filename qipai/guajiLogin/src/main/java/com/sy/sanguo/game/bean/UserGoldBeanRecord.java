package com.sy.sanguo.game.bean;

import java.util.Date;

/**
 * 玩家房卡来源日志
 */
public class UserGoldBeanRecord {

    /**
     * 自增ID
     */
    private long id;

    /**
     * 玩家ID
     */
    private long userId;

    private long goldBean;

    private long addGoldBean;

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

    public UserGoldBeanRecord() {
    }

    public UserGoldBeanRecord(long userId, long goldBean,  long addGoldBean, int type) {
        this.recordType = (addGoldBean > 0) ? 1 : 0;
        this.userId = userId;
        this.goldBean = goldBean;
        this.addGoldBean = addGoldBean;
        this.playType = 0;
        this.sourceType = type;
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

    public long getGoldBean() {
        return goldBean;
    }

    public void setGoldBean(long goldBean) {
        this.goldBean = goldBean;
    }

    public long getAddGoldBean() {
        return addGoldBean;
    }

    public void setAddGoldBean(long addGoldBean) {
        this.addGoldBean = addGoldBean;
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
}
