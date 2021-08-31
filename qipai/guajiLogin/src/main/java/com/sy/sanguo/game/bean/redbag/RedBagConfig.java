package com.sy.sanguo.game.bean.redbag;

public class RedBagConfig {

    private int poolNum;

    private String startDate;

    private String endDate;

    private String drowStartDate;

    private String drowEndDate;

    private String receiveStartTime;

    private String receiveEndTime;

    private String rule;

    private String withDrawDiamond;

    private String timeRange;

    public RedBagConfig() {
    }

    public RedBagConfig(String redBagConfigStr, String withDrawDiamond) {
        String[] configs = redBagConfigStr.split(",");
        this.poolNum = Integer.parseInt(configs[0]);
        this.startDate = configs[1];
        this.endDate = configs[2];
        this.drowStartDate = configs[3];
        this.drowEndDate = configs[4];
        this.receiveStartTime = configs[5];
        this.receiveEndTime = configs[6];
        this.timeRange = configs[7];
        this.rule = configs[8];
        this.withDrawDiamond = withDrawDiamond;
    }

    public int getPoolNum() {
        return poolNum;
    }

    public void setPoolNum(int poolNum) {
        this.poolNum = poolNum;
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

    public String getTimeRange() {
        return timeRange;
    }

    public void setTimeRange(String timeRange) {
        this.timeRange = timeRange;
    }
}
