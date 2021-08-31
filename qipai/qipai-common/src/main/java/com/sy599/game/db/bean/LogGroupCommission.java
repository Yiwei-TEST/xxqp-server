package com.sy599.game.db.bean;

import java.io.Serializable;

public class LogGroupCommission implements Serializable {

    private static final long serialVersionUID = 1L;

    private long keyId;
    private long dataDate;
    private long groupId;
    private long userId;

    private long credit;
    private long commissionCredit;
    private int commissionCount;
    private int dyjCount;
    private int zjsCount;
    private int totalPay;

    private long selfWinCredit;
    private long selfCommissionCredit;
    private int selfCommissionCount;
    private int selfDyjCount;
    private int selfZjsCount;
    private int selfTotalPay;


    public LogGroupCommission(Long dataDate, long groupId, long userId) {
        this.dataDate = dataDate;
        this.groupId = groupId;
        this.userId = userId;
    }

    public LogGroupCommission clone() {
        LogGroupCommission log = new LogGroupCommission(dataDate, groupId, userId);

        log.setCredit(credit);
        log.setCommissionCredit(commissionCredit);
        log.setCommissionCount(commissionCount);
        log.setDyjCount(dyjCount);
        log.setZjsCount(zjsCount);
        log.setTotalPay(totalPay);

        log.setSelfWinCredit(selfWinCredit);
        log.setSelfCommissionCredit(selfCommissionCredit);
        log.setSelfCommissionCount(selfCommissionCount);
        log.setSelfDyjCount(selfDyjCount);
        log.setSelfZjsCount(selfZjsCount);
        log.setSelfTotalPay(selfTotalPay);
        return log;
    }

    public void addProp(LogGroupCommission other) {

        this.setCredit(this.getCredit() + other.getCredit());
        this.setCommissionCredit(this.getCommissionCredit() + other.getCommissionCredit());
        this.setCommissionCount(this.getCommissionCount() + other.getCommissionCount());
        this.setZjsCount(this.getZjsCount() + other.getZjsCount());
        this.setDyjCount(this.getDyjCount() + other.getDyjCount());
        this.setTotalPay(this.getTotalPay() + other.getTotalPay());

        this.setSelfWinCredit(this.getSelfWinCredit() + other.getSelfWinCredit());
        this.setSelfCommissionCredit(this.getSelfCommissionCredit() + other.getSelfCommissionCredit());
        this.setSelfCommissionCount(this.getSelfCommissionCount() + other.getSelfCommissionCount());
        this.setSelfZjsCount(this.getSelfZjsCount() + other.getSelfZjsCount());
        this.setSelfDyjCount(this.getSelfDyjCount() + other.getSelfDyjCount());
        this.setSelfTotalPay(this.getSelfTotalPay() + other.getSelfTotalPay());
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

    public long getCredit() {
        return credit;
    }

    public void setCredit(long credit) {
        this.credit = credit;
    }

    public long getCommissionCredit() {
        return commissionCredit;
    }

    public void setCommissionCredit(long commissionCredit) {
        this.commissionCredit = commissionCredit;
    }

    public int getCommissionCount() {
        return commissionCount;
    }

    public void setCommissionCount(int commissionCount) {
        this.commissionCount = commissionCount;
    }

    public int getDyjCount() {
        return dyjCount;
    }

    public void setDyjCount(int dyjCount) {
        this.dyjCount = dyjCount;
    }

    public int getZjsCount() {
        return zjsCount;
    }

    public void setZjsCount(int zjsCount) {
        this.zjsCount = zjsCount;
    }

    public int getTotalPay() {
        return totalPay;
    }

    public void setTotalPay(int totalPay) {
        this.totalPay = totalPay;
    }

    public long getSelfWinCredit() {
        return selfWinCredit;
    }

    public void setSelfWinCredit(long selfWinCredit) {
        this.selfWinCredit = selfWinCredit;
    }

    public long getSelfCommissionCredit() {
        return selfCommissionCredit;
    }

    public void setSelfCommissionCredit(long selfCommissionCredit) {
        this.selfCommissionCredit = selfCommissionCredit;
    }

    public int getSelfCommissionCount() {
        return selfCommissionCount;
    }

    public void setSelfCommissionCount(int selfCommissionCount) {
        this.selfCommissionCount = selfCommissionCount;
    }

    public int getSelfDyjCount() {
        return selfDyjCount;
    }

    public void setSelfDyjCount(int selfDyjCount) {
        this.selfDyjCount = selfDyjCount;
    }

    public int getSelfZjsCount() {
        return selfZjsCount;
    }

    public void setSelfZjsCount(int selfZjsCount) {
        this.selfZjsCount = selfZjsCount;
    }

    public int getSelfTotalPay() {
        return selfTotalPay;
    }

    public void setSelfTotalPay(int selfTotalPay) {
        this.selfTotalPay = selfTotalPay;
    }
}
