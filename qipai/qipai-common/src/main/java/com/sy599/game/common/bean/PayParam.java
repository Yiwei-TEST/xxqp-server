package com.sy599.game.common.bean;

public class PayParam {

    private int mode;
    private int playType;
    private int payType;
    private int playerCount;
    private int bureauCount;
    private Object ext;
    private int needCards = -1;
    private int costType = -1;
    private int costValue = -1;

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getPlayType() {
        return playType;
    }

    public void setPlayType(int playType) {
        this.playType = playType;
    }

    public int getPayType() {
        return payType;
    }

    public void setPayType(int payType) {
        this.payType = payType;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public void setPlayerCount(int playerCount) {
        this.playerCount = playerCount;
    }

    public int getBureauCount() {
        return bureauCount;
    }

    public void setBureauCount(int bureauCount) {
        this.bureauCount = bureauCount;
    }

    public Object getExt() {
        return ext;
    }

    public void setExt(Object ext) {
        this.ext = ext;
    }

    public int getNeedCards() {
        return needCards;
    }

    public void setNeedCards(int needCards) {
        this.needCards = needCards;
    }

    public int getCostType() {
        return costType;
    }

    public void setCostType(int costType) {
        this.costType = costType;
    }

    public int getCostValue() {
        return costValue;
    }

    public void setCostValue(int costValue) {
        this.costValue = costValue;
    }

    @Override
    public String toString() {
        return "PayParam{" +
                "playType=" + playType +
                ", payType=" + payType +
                ", playerCount=" + playerCount +
                ", bureauCount=" + bureauCount +
                ", ext=" + ext +
                ", needCards=" + needCards +
                '}';
    }
}
