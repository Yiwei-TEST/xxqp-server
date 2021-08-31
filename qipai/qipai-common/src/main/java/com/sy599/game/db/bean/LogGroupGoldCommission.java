package com.sy599.game.db.bean;

import java.io.Serializable;

public class LogGroupGoldCommission implements Serializable {

    private static final long serialVersionUID = 1L;

    private long keyId;
    private long dataDate;
    private long groupId;
    private long userId;

    private long gold;
    private long commission;
    private int commissionCount;
    private int zjsCount;

    private long selfWin;
    private long selfCommission;
    private int selfCommissionCount;
    private int selfZjsCount;


    public LogGroupGoldCommission(Long dataDate, long groupId, long userId) {
        this.dataDate = dataDate;
        this.groupId = groupId;
        this.userId = userId;
    }

    public LogGroupGoldCommission clone() {
        LogGroupGoldCommission log = new LogGroupGoldCommission(dataDate, groupId, userId);

        log.setGold(gold);
        log.setCommission(commission);
        log.setCommissionCount(commissionCount);
        log.setZjsCount(zjsCount);

        log.setSelfWin(selfWin);
        log.setSelfCommission(selfCommission);
        log.setSelfCommissionCount(selfCommissionCount);
        log.setSelfZjsCount(selfZjsCount);
        return log;
    }

    public void addProp(LogGroupGoldCommission other) {

        this.setGold(this.getGold() + other.getGold());
        this.setCommission(this.getCommission() + other.getCommission());
        this.setCommissionCount(this.getCommissionCount() + other.getCommissionCount());
        this.setZjsCount(this.getZjsCount() + other.getZjsCount());

        this.setSelfWin(this.getSelfWin() + other.getSelfWin());
        this.setSelfCommission(this.getSelfCommission() + other.getSelfCommission());
        this.setSelfCommissionCount(this.getSelfCommissionCount() + other.getSelfCommissionCount());
        this.setSelfZjsCount(this.getSelfZjsCount() + other.getSelfZjsCount());
    }

    public long getKeyId() {
        return keyId;
    }

    public void setKeyId(long keyId) {
        this.keyId = keyId;
    }

    public long getDataDate() {
        return dataDate;
    }

    public void setDataDate(long dataDate) {
        this.dataDate = dataDate;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getGold() {
        return gold;
    }

    public void setGold(long gold) {
        this.gold = gold;
    }

    public long getCommission() {
        return commission;
    }

    public void setCommission(long commission) {
        this.commission = commission;
    }

    public int getCommissionCount() {
        return commissionCount;
    }

    public void setCommissionCount(int commissionCount) {
        this.commissionCount = commissionCount;
    }

    public int getZjsCount() {
        return zjsCount;
    }

    public void setZjsCount(int zjsCount) {
        this.zjsCount = zjsCount;
    }

    public long getSelfWin() {
        return selfWin;
    }

    public void setSelfWin(long selfWin) {
        this.selfWin = selfWin;
    }

    public long getSelfCommission() {
        return selfCommission;
    }

    public void setSelfCommission(long selfCommission) {
        this.selfCommission = selfCommission;
    }

    public int getSelfCommissionCount() {
        return selfCommissionCount;
    }

    public void setSelfCommissionCount(int selfCommissionCount) {
        this.selfCommissionCount = selfCommissionCount;
    }

    public int getSelfZjsCount() {
        return selfZjsCount;
    }

    public void setSelfZjsCount(int selfZjsCount) {
        this.selfZjsCount = selfZjsCount;
    }
}
