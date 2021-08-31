package com.sy599.game.db.bean;

import java.util.Date;

/**
 * 玩家龙虎斗日志 用户玩家列表信息查询
 */
public class LHDRecord {
    /**
     * 自增id
     */
    private long id;
    /**
     * 下注玩家ID
     */
    private long userId;
    /**
     * 下注信息Map<Integer, Integer>
     */
    private String betInfo;
    /**
     * 总下注金币数
     */
    private int betGold;
    /**
     * 本局龙虎斗结果 1龙赢 2虎赢 3和赢
     */
    private int result;
    /**
     * 获胜赢得金币数
     */
    private int winGold;
    /**
     * 记录时间
     */
    private Date createTime;

    public LHDRecord(long userId, String betInfo, int betGold, int result, int winGold, Date createTime) {
        this.userId = userId;
        this.betInfo = betInfo;
        this.betGold = betGold;
        this.result = result;
        this.winGold = winGold;
        this.createTime = createTime;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getBetInfo() {
        return betInfo;
    }

    public void setBetInfo(String betInfo) {
        this.betInfo = betInfo;
    }

    public int getBetGold() {
        return betGold;
    }

    public void setBetGold(int betGold) {
        this.betGold = betGold;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public int getWinGold() {
        return winGold;
    }

    public void setWinGold(int winGold) {
        this.winGold = winGold;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
