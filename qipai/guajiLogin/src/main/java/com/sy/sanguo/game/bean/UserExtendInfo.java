package com.sy.sanguo.game.bean;

import java.util.Date;

public class UserExtendInfo {

    private long userId;
    private String cdk;
    private String extend;
    private String myConsume;
    private double shengMoney;
    private int prizeFlag;
    private String name;
    private double totalMoney;
    private int bindSongCard;
    private Date lastUpNameTime;
    private Date lastUpHeadimgTime;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getCdk() {
        return cdk;
    }

    public void setCdk(String cdk) {
        this.cdk = cdk;
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend;
    }

    public String getMyConsume() {
        return myConsume;
    }

    public void setMyConsume(String myConsume) {
        this.myConsume = myConsume;
    }

    public double getShengMoney() {
        return shengMoney;
    }

    public void setShengMoney(double shengMoney) {
        this.shengMoney = shengMoney;
    }

    public int getPrizeFlag() {
        return prizeFlag;
    }

    public void setPrizeFlag(int prizeFlag) {
        this.prizeFlag = prizeFlag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getTotalMoney() {
        return totalMoney;
    }

    public void setTotalMoney(double totalMoney) {
        this.totalMoney = totalMoney;
    }

    public int getBindSongCard() {
        return bindSongCard;
    }

    public void setBindSongCard(int bindSongCard) {
        this.bindSongCard = bindSongCard;
    }

    public Date getLastUpNameTime() {
        return lastUpNameTime;
    }

    public void setLastUpNameTime(Date lastUpNameTime) {
        this.lastUpNameTime = lastUpNameTime;
    }

    public Date getLastUpHeadimgTime() {
        return lastUpHeadimgTime;
    }

    public void setLastUpHeadimgTime(Date lastUpHeadimgTime) {
        this.lastUpHeadimgTime = lastUpHeadimgTime;
    }
}
