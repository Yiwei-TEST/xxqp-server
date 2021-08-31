package com.sy599.game.db.bean.sendDiamonds;

import java.util.Date;

/**
 * 送钻记录
 */
public class SendDiamondsLog {
    private int id;
    private long sendUserid;
    private long acceptUserid;
    private long diamondNum ;
    private Date sendTime ;
    private String sendName;
    private String acceptName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getSendUserid() {
        return sendUserid;
    }

    public void setSendUserid(long sendUserid) {
        this.sendUserid = sendUserid;
    }

    public long getAcceptUserid() {
        return acceptUserid;
    }

    public void setAcceptUserid(long acceptUserid) {
        this.acceptUserid = acceptUserid;
    }

    public long getDiamondNum() {
        return diamondNum;
    }

    public void setDiamondNum(long diamondNum) {
        this.diamondNum = diamondNum;
    }



    public String getSendName() {
        return sendName;
    }

    public void setSendName(String sendName) {
        this.sendName = sendName;
    }

    public String getAcceptName() {
        return acceptName;
    }

    public void setAcceptName(String acceptName) {
        this.acceptName = acceptName;
    }

    public Date getSendTime() {
        return sendTime;
    }

    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }

    @Override
    public String toString() {
        return "SendDiamondsLog{" +
                "sendUserid=" + sendUserid +
                ", acceptUserid=" + acceptUserid +
                ", diamondNum=" + diamondNum +
                ", sendTime=" + sendTime +
                ", sendName='" + sendName + '\'' +
                ", acceptName='" + acceptName + '\'' +
                '}';
    }
}
