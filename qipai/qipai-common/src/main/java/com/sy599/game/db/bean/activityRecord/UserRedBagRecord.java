package com.sy599.game.db.bean.activityRecord;

import com.alibaba.fastjson.TypeReference;
import com.sy599.game.util.JacksonUtil;
import com.sy599.game.util.StringUtil;
import com.sy599.game.util.TimeUtil;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 现金红包活动记录  KEY==>玩家ID_领取红包时间
 */
public class UserRedBagRecord {

    /**
     * 玩家ID
     */
    private long userId;

    /**
     * 领取红包时间
     */
    private String receiveDate;

    /**
     * 今日玩牌局数
     */
    private int gameNum;

    /**
     * 当天已领取红包次数
     */
    private int receiveNum;

    /**
     * 玩家当天红包领取记录
     */
    private List<SelfRedBagReceiveRecord> receiveRecords;

    public UserRedBagRecord() {
    }

    public UserRedBagRecord(long userId, String receiveDate, int gameNum, int receiveNum) {
        this.userId = userId;
        this.receiveDate = receiveDate;
        this.gameNum = gameNum;
        this.receiveNum = receiveNum;
        this.receiveRecords = new ArrayList<>();
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getReceiveDate() {
        return receiveDate;
    }

    public void setReceiveDate(String receiveDate) {
        this.receiveDate = receiveDate;
    }

    public int getGameNum() {
        return gameNum;
    }

    public void setGameNum(int gameNum) {
        this.gameNum = gameNum;
    }

    public void alterGameNum(int num) {
        this.gameNum += num;
    }

    public int getReceiveNum() {
        return receiveNum;
    }

    public void setReceiveNum(int receiveNum) {
        this.receiveNum = receiveNum;
    }

    public void addReceiveRecord(Float redBag) {
        Date receiveDate = new Date();
        SelfRedBagReceiveRecord selfReceiveRecord = new SelfRedBagReceiveRecord(TimeUtil.formatDayTime2(receiveDate), redBag);
        receiveRecords.add(selfReceiveRecord);
    }


    public String getReceiveRecords() {
        if (this.receiveRecords == null || this.receiveRecords.isEmpty()) {
            return "";
        }
        return JacksonUtil.writeValueAsString(this.receiveRecords);
    }

    public List<SelfRedBagReceiveRecord> getReceiveRecordList() {
        if (this.receiveRecords == null || this.receiveRecords.isEmpty()) {
            this.receiveRecords = new ArrayList<>();
        }
        return receiveRecords;
    }

    public void setReceiveRecords(List<SelfRedBagReceiveRecord> receiveRecords) {
        this.receiveRecords = receiveRecords;
    }

    public void setReceiveRecords(String records) {
        if (!StringUtil.isBlank(records)) {
            this.receiveRecords = JacksonUtil.readValue(records,
                    new TypeReference<List<SelfRedBagReceiveRecord>>() {
                    });
        } else {
            this.receiveRecords = new ArrayList<>();
        }
    }

    public void updateRecord(float redBag) {
        setReceiveNum(receiveNum + 1);
        addReceiveRecord(redBag);
    }

    /**
     * 获取玩家剩余可领取次数
     * @param userRedBagRecord
     * @return
     */
    public int getCanReceiveNum() {
        int maxNum = 2;
        int canReceiveNum = 0;
        if(receiveNum > 0) {// 已领取登陆奖励
            if(receiveNum == 1 && gameNum >= 4)
                canReceiveNum = 1;
            if(receiveNum == 2) {
                canReceiveNum = 0;
            }
        } else {
            canReceiveNum ++;
            if(gameNum >= 4)
                canReceiveNum ++;

        }
        return canReceiveNum;
//        return 1;
    }
}
