package com.sy599.game.db.bean;

import com.alibaba.fastjson.TypeReference;
import com.sy599.game.util.JacksonUtil;
import com.sy599.game.util.MathUtil;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 现金红包活动系统数据
 */
public class RedBagSystemInfo {

    /**
     * 奖金池创建时间
     */
    private Date createdTime;

    /**
     * 现金红包奖池(每日凌晨0点重置)
     */
    private float dayPoolNum;

    /**
     * 现金红包领取记录
     */
    private String receiveRecords;

    public RedBagSystemInfo() {
    }

    public RedBagSystemInfo(int dayPoolNum) {
        this.createdTime = new Date();
        this.dayPoolNum = dayPoolNum;
        this.receiveRecords = "";
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public float getDayPoolNum() {
        return dayPoolNum;
    }

    public void setDayPoolNum(float dayPoolNum) {
        this.dayPoolNum = dayPoolNum;
    }

    /**
     * 现金红包奖池更新
     * @param subNum
     * @return
     */
    public float subDayPoolNum(float subNum) {
        //因为我们现在5000领不完，玩家会认为我们没有人气，每次抽取金额，随机减1.5-2.5倍的系数
        float randomSubNum = (float) MathUtil.random(1.5, 2.5) * subNum;
        DecimalFormat fnum = new DecimalFormat("#0.00");
        float realSubNum = Float.parseFloat(fnum.format(randomSubNum));
        if(dayPoolNum > realSubNum) {
            this.dayPoolNum -= realSubNum;
            return realSubNum;
        } else {
            this.dayPoolNum = 0;
            return realSubNum;
        }
    }

    public String getReceiveRecords() {
        if (this.receiveRecords == null || this.receiveRecords.isEmpty()) {
            return "";
        }
        return receiveRecords;
    }

    public LinkedBlockingQueue<RedBagReceiveRecord> getReceiveRecordList() {
        if (this.receiveRecords == null || this.receiveRecords.isEmpty()) {
            return new LinkedBlockingQueue<>();
        }
        return JacksonUtil.readValue(receiveRecords, new TypeReference<LinkedBlockingQueue<RedBagReceiveRecord>>() {});
    }

    public void setReceiveRecords(String receiveRecords) {
        this.receiveRecords = receiveRecords;
    }
}
