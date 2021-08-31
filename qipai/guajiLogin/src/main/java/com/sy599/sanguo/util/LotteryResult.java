package com.sy599.sanguo.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class LotteryResult {
    private int index;
    private int sumTime;
    private int time;
    private double probability;

    public String getPrizeName() {
        return prizeName;
    }

    public void setPrizeName(String prizeName) {
        this.prizeName = prizeName;
    }

    private double realProbability;
    private String prizeName;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getSumTime() {
        return sumTime;
    }

    public void setSumTime(int sumTime) {
        this.sumTime = sumTime;
    }

    public double getProbability() {
        return probability;
    }

    public double getRealProbability() {
        return realProbability;
    }

    public void setRealProbability(double realProbability) {
        this.realProbability = realProbability;
    }

    public LotteryResult() {

    }

    public LotteryResult(int index, int sumTime, int time, double realProbability, String prizeName) {
        this.setIndex(index);
        this.setTime(time);
        this.setSumTime(sumTime);
        this.setRealProbability(realProbability);
        this.setPrizeName(prizeName);

    }

    public String toString() {
        return "奖品名称：" + prizeName + "，索引值：" + index + "，抽奖总数：" + sumTime + "，抽中次数：" + time + "，概率："
                + realProbability + "，实际概率：" + (double) time / sumTime;
    }
}

