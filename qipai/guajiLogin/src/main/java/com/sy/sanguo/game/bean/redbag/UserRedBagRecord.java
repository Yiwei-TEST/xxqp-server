package com.sy.sanguo.game.bean.redbag;

import com.alibaba.fastjson.TypeReference;
import com.sy.sanguo.common.util.JacksonUtil;
import com.sy.sanguo.common.util.StringUtil;
import com.sy599.sanguo.util.TimeUtil;
import com.sy599.sanguo.util.TimeUtil1;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    /**
     * 登陆红包领取金额
     */
    private float loginRedBag;

    /**
     * 打牌红包领取金额
     */
    private float gameRedBag;

    /**
     * 最后领取时间
     */
    private Date lastReceiveTime;

    public UserRedBagRecord() {
    }

    public UserRedBagRecord(long userId, String receiveDate, int gameNum, int receiveNum) {
        this.userId = userId;
        this.receiveDate = receiveDate;
        this.gameNum = gameNum;
        this.receiveNum = receiveNum;
        this.receiveRecords = new ArrayList<>();
        this.loginRedBag = 0;
        this.gameRedBag = 0;
        this.lastReceiveTime = null;
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
        SelfRedBagReceiveRecord selfReceiveRecord = new SelfRedBagReceiveRecord(TimeUtil1.formatDayTime2(receiveDate), redBag);
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
        if (!StringUtils.isBlank(records)) {
            this.receiveRecords = JacksonUtil.readValue(records,
                    new TypeReference<List<SelfRedBagReceiveRecord>>() {
                    });
        } else {
            this.receiveRecords = new ArrayList<>();
        }
    }

    public void updateRecord(float redBag, int redbagType) {
        setReceiveNum(receiveNum + 1);
        addReceiveRecord(redBag);
        if(redbagType == 0) {
            loginRedBag = redBag;
        } else {
            gameRedBag = redBag;
        }
        lastReceiveTime = new Date();
    }

    /**
     * 获取玩家剩余可领取次数
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

    public float getLoginRedBag() {
        return loginRedBag;
    }

    public void setLoginRedBag(float loginRedBag) {
        this.loginRedBag = loginRedBag;
    }

    public float getGameRedBag() {
        return gameRedBag;
    }

    public void setGameRedBag(float gameRedBag) {
        this.gameRedBag = gameRedBag;
    }

    public Date getLastReceiveTime() {
        return lastReceiveTime;
    }

    public void setLastReceiveTime(Date lastReceiveTime) {
        this.lastReceiveTime = lastReceiveTime;
    }
}
