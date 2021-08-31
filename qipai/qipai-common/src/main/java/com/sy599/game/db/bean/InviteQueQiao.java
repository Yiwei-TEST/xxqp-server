package com.sy599.game.db.bean;

import java.util.Date;

public class InviteQueQiao {
    private int id;
    private long sendId;
    private long acceptId;
    private long sendTime;
    private int isAllow;
    private int isRead;

    public InviteQueQiao() {
    }

    public InviteQueQiao(int id, long sendId, long acceptId, long sendTime, int isAllow, int isRead) {
        this.id = id;
        this.sendId = sendId;
        this.acceptId = acceptId;
        this.sendTime = sendTime;
        this.isAllow = isAllow;
        this.isRead = isRead;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getSendId() {
        return sendId;
    }

    public void setSendId(int sendId) {
        this.sendId = sendId;
    }

    public long getAcceptId() {
        return acceptId;
    }

    public void setAcceptId(int acceptId) {
        this.acceptId = acceptId;
    }

    public long getSendTime() {
        return sendTime;
    }

    public void setSendTime(long sendTime) {
        this.sendTime = sendTime;
    }

    public int getIsAllow() {
        return isAllow;
    }

    public void setIsAllow(int isAllow) {
        this.isAllow = isAllow;
    }

    public int getIsRead() {
        return isRead;
    }

    public void setIsRead(int isRead) {
        this.isRead = isRead;
    }
}
