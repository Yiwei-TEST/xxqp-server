package com.sy599.game.db.bean.activityRecord;

/**
 * 玩家红包领取记录
 */
public class SelfRedBagReceiveRecord {

    /**
     * 领取时间格式yyyy-mm-ddd  00:00:00
     */
    private String receiveTime;
    /**
     * 领取金额
     */
    private float receiveNum;
    /**
     * 是否已提现
     */
    private boolean withDraw;

    public SelfRedBagReceiveRecord() {
    }

    public SelfRedBagReceiveRecord(String receiveTime, float receiveNum) {
        this.receiveTime = receiveTime;
        this.receiveNum = receiveNum;
        this.withDraw = false;
    }

    public String getReceiveTime() {
        return receiveTime;
    }

    public void setReceiveTime(String receiveTime) {
        this.receiveTime = receiveTime;
    }

    public float getReceiveNum() {
        return receiveNum;
    }

    public void setReceiveNum(float receiveNum) {
        this.receiveNum = receiveNum;
    }

    public boolean isWithDraw() {
        return withDraw;
    }

    public void setWithDraw(boolean withDraw) {
        this.withDraw = withDraw;
    }
}
