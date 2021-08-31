package com.sy599.game.db.bean;

import com.sy599.game.db.enums.CardSourceType;

import java.util.Date;

/**
 * 玩家房卡来源日志
 */
public class UserCardRecordInfo {

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
    private long freeCard;

    /**
     * 玩家当前房卡数
     */
    private long cards;

    /**
     * 玩家本次操作(消耗/获得)免费房卡数
     */
    private int addFreeCard;

    /**
     * 玩家本次操作(消耗/获得)免费房卡数
     */
    private int addCard;

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
     * 操作来源名
     */
    private String sourceName;

    /**
     * 操作时间
     */
    private Date createTime;

    public UserCardRecordInfo() {
    }

    public UserCardRecordInfo(long userId, long freeCard, long cards, int addFreeCard, int addCard, int playType, CardSourceType type) {
        this.recordType = (addFreeCard + addCard > 0) ? 0 : 1;
        this.userId = userId;
        this.freeCard = freeCard;
        this.cards = cards;
        this.addFreeCard = addFreeCard;
        this.addCard = addCard;
        this.playType = playType;
        this.sourceType = type.getSourceType();
        this.sourceName = type.getSourceName();
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

    public long getFreeCard() {
        return freeCard;
    }

    public void setFreeCard(long freeCard) {
        this.freeCard = freeCard;
    }

    public long getCards() {
        return cards;
    }

    public void setCards(long cards) {
        this.cards = cards;
    }

    public int getAddFreeCard() {
        return addFreeCard;
    }

    public void setAddFreeCard(int addFreeCard) {
        this.addFreeCard = addFreeCard;
    }

    public int getAddCard() {
        return addCard;
    }

    public void setAddCard(int addCard) {
        this.addCard = addCard;
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

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
