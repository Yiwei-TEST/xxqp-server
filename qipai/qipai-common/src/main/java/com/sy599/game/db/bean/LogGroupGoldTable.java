package com.sy599.game.db.bean;

import java.io.Serializable;

public class LogGroupGoldTable implements Serializable {

    private static final long serialVersionUID = 1L;

    private long keyId;
    private long dataDate;
    private long groupId;
    private long configId;
    private long userId;
    private int playType;
    private long ticket;
    private int ticketCount;
    private long commission;
    private int commissionCount;

    public LogGroupGoldTable(Long dataDate, long groupId,long configId , long userId) {
        this.dataDate = dataDate;
        this.groupId = groupId;
        this.configId = configId;
        this.userId = userId;
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

    public long getConfigId() {
        return configId;
    }

    public void setConfigId(long configId) {
        this.configId = configId;
    }

    public int getPlayType() {
        return playType;
    }

    public void setPlayType(int playType) {
        this.playType = playType;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getTicket() {
        return ticket;
    }

    public void setTicket(long ticket) {
        this.ticket = ticket;
    }

    public int getTicketCount() {
        return ticketCount;
    }

    public void setTicketCount(int ticketCount) {
        this.ticketCount = ticketCount;
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
}
