package com.sy599.game.db.bean.task;

import java.util.*;

public class UserTaskInfo {

    /**
     * 芒果个数
     */
    private int mangGuo;
    /**
     * 历史最高金币数
     */
    private long maxGold;
    /**
     * 玩家每日任务刷新时间
     */
    private long dailyRefreshTime;
    /**
     * 玩家每日金币场次数
     */
    private int dailyGoldGameNum;
    /**
     * 玩家每日金币场连胜次数
     */
    private int dailyWinGameNum;
    /**
     * 玩家每日比赛场次数
     */
    private int dailyMatchGameNum;
    /**
     * 已达成的任务列表
     */
    private Map<Integer, Set<Integer>> taskInfos = new HashMap<>();

    public UserTaskInfo() {
    }

    public UserTaskInfo(int mangGuo, long maxGold, Map<Integer, Set<Integer>> taskInfos) {
        this.mangGuo = mangGuo;
        this.maxGold = maxGold;
        this.taskInfos = taskInfos;
        this.dailyGoldGameNum = 0;
        this.dailyWinGameNum = 0;
        this.dailyMatchGameNum = 0;
        this.dailyRefreshTime = 0;
    }

    public int getMangGuo() {
        return mangGuo;
    }

    public void setMangGuo(int mangGuo) {
        this.mangGuo = mangGuo;
    }

    public void addMangGuo(int mangGuo) {
        this.mangGuo += mangGuo;
    }

    public long getMaxGold() {
        return maxGold;
    }

    public void setMaxGold(long maxGold) {
        this.maxGold = maxGold;
    }

    public int getDailyGoldGameNum() {
        return dailyGoldGameNum;
    }

    public void setDailyGoldGameNum(int dailyGoldGameNum) {
        this.dailyGoldGameNum = dailyGoldGameNum;
    }

    public void alterDailyGoldGameNum() {
        this.dailyGoldGameNum ++;
    }

    public int getDailyWinGameNum() {
        return dailyWinGameNum;
    }

    public void setDailyWinGameNum(int dailyWinGameNum) {
        this.dailyWinGameNum = dailyWinGameNum;
    }

    public void alterDailyWinGameNum() {
        this.dailyWinGameNum ++;
    }

    public int getDailyMatchGameNum() {
        return dailyMatchGameNum;
    }

    public void setDailyMatchGameNum(int dailyMatchGameNum) {
        this.dailyMatchGameNum = dailyMatchGameNum;
    }

    public void alterDailyMatchGameNum() {
        this.dailyMatchGameNum ++;
    }

    public long getDailyRefreshTime() {
        return dailyRefreshTime;
    }

    public void setDailyRefreshTime(long dailyRefreshTime) {
        this.dailyRefreshTime = dailyRefreshTime;
    }

    public Map<Integer, Set<Integer>> getTaskInfos() {
        return taskInfos;
    }

    public void setTaskInfos(Map<Integer, Set<Integer>> taskInfos) {
        this.taskInfos = taskInfos;
    }

    public Set<Integer> getUserTaskInfos(int taskType) {
        return taskInfos.get(taskType);
    }

    public void updateTaskInfos(int taskType, Set<Integer> tasks) {
        taskInfos.put(taskType, tasks);
    }
}
