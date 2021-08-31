package com.sy.sanguo.game.bean.redbag;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 推送给前端的现金红包信息
 */
public class RedBagSendInfo {

    /**
     * 活动开启时间
     */
    private String startDate;

    /**
     * 活动结束时间
     */
    private String endDate;

    /**
     * 提现开始时间
     */
    private String drowStartDate;

    /**
     * 提现结束时间
     */
    private String drowEndDate;

    /**
     * 领取开始时间   12:00:00
     */
    private String receiveStartTime;

    /**
     * 领取结束时间   23:59:59
     */
    private String receiveEndTime;

    /**
     * 玩家ID
     */
    private long userId;

    /**
     * 当天可领取次数
     */
    private int canReceiveNum;

    /**
     * 当天已累计领取的红包金额
     */
    private float redBagNum;

    /**
     * 奖金池
     */
    private int maxPoolNum;

    /**
     * 当前奖金池
     */
    private float curPoolNum;

    /**
     * 玩家红包领取记录
     */
    private List<RedBagReceiveRecord> receiveRecords;

    /**
     * 活动规则(前端显示)
     */
    private String rule;

    /**
     * 提现钻石领取规则
     */
    private String withDrawDiamond;

    /**
     * 领取红包时间段
     */
    private String timeRange;

    /**
     * 是否能提现
     */
    private boolean canDraw;

    /**
     * 今日已领取的登陆红包金额
     */
    private float todayLoginRedBag;

    /**
     * 今日已领取的打牌红包金额
     */
    private float todayGameRedBag;

    public RedBagSendInfo(long userId, RedBagConfig config, UserRedBagRecord userRedBagRecord, RedBagSystemInfo systemInfo, float accRedBagNum, boolean canDraw) {
        this.startDate = config.getStartDate();
        this.endDate = config.getEndDate();
        this.drowStartDate = config.getDrowStartDate();
        this.drowEndDate = config.getDrowEndDate();
        this.receiveStartTime = config.getReceiveStartTime();
        this.receiveEndTime = config.getReceiveEndTime();
        this.userId = userId;
        this.canReceiveNum = userRedBagRecord.getCanReceiveNum();
        this.redBagNum = accRedBagNum ;
        this.maxPoolNum = config.getPoolNum();
        DecimalFormat fnum = new DecimalFormat("#0.00");
        float sendCurPoolNum = Float.parseFloat(fnum.format(systemInfo.getDayPoolNum()));
        this.curPoolNum = sendCurPoolNum;
        this.receiveRecords = new ArrayList<>();
        List<RedBagReceiveRecord> all = new ArrayList<>(systemInfo.getReceiveRecordList());
        for(RedBagReceiveRecord item : all) {
            receiveRecords.add(item);
        }
        this.rule = config.getRule();
        this.withDrawDiamond = config.getWithDrawDiamond();
        this.timeRange = config.getTimeRange();
        this.canDraw = canDraw;
        this.todayLoginRedBag = userRedBagRecord.getLoginRedBag();
        this.todayGameRedBag = userRedBagRecord.getGameRedBag();
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getDrowStartDate() {
        return drowStartDate;
    }

    public void setDrowStartDate(String drowStartDate) {
        this.drowStartDate = drowStartDate;
    }

    public String getDrowEndDate() {
        return drowEndDate;
    }

    public void setDrowEndDate(String drowEndDate) {
        this.drowEndDate = drowEndDate;
    }

    public String getReceiveStartTime() {
        return receiveStartTime;
    }

    public void setReceiveStartTime(String receiveStartTime) {
        this.receiveStartTime = receiveStartTime;
    }

    public String getReceiveEndTime() {
        return receiveEndTime;
    }

    public void setReceiveEndTime(String receiveEndTime) {
        this.receiveEndTime = receiveEndTime;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public float getRedBagNum() {
        return redBagNum;
    }

    public void setRedBagNum(float redBagNum) {
        this.redBagNum = redBagNum;
    }

    public int getMaxPoolNum() {
        return maxPoolNum;
    }

    public void setMaxPoolNum(int maxPoolNum) {
        this.maxPoolNum = maxPoolNum;
    }

    public float getCurPoolNum() {
        return curPoolNum;
    }

    public void setCurPoolNum(float curPoolNum) {
        this.curPoolNum = curPoolNum;
    }

    public List<RedBagReceiveRecord> getReceiveRecords() {
        return receiveRecords;
    }

    public void setReceiveRecords(List<RedBagReceiveRecord> receiveRecords) {
        this.receiveRecords = receiveRecords;
    }

    public int getCanReceiveNum() {
        return canReceiveNum;
    }

    public void setCanReceiveNum(int canReceiveNum) {
        this.canReceiveNum = canReceiveNum;
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    public String getWithDrawDiamond() {
        return withDrawDiamond;
    }

    public void setWithDrawDiamond(String withDrawDiamond) {
        this.withDrawDiamond = withDrawDiamond;
    }

    public boolean isCanDraw() {
        return canDraw;
    }

    public void setCanDraw(boolean canDraw) {
        this.canDraw = canDraw;
    }

    public String getTimeRange() {
        return timeRange;
    }

    public void setTimeRange(String timeRange) {
        this.timeRange = timeRange;
    }

    public float getTodayLoginRedBag() {
        return todayLoginRedBag;
    }

    public void setTodayLoginRedBag(float todayLoginRedBag) {
        this.todayLoginRedBag = todayLoginRedBag;
    }

    public float getTodayGameRedBag() {
        return todayGameRedBag;
    }

    public void setTodayGameRedBag(float todayGameRedBag) {
        this.todayGameRedBag = todayGameRedBag;
    }
}
