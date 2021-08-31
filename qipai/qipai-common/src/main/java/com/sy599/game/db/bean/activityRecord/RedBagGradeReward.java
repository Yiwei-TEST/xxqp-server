package com.sy599.game.db.bean.activityRecord;

import org.omg.CORBA.INTERNAL;

public class RedBagGradeReward {

    private int day;
    private float minGrade;
    private float maxGrade;

    public RedBagGradeReward(String gradeRewards) {
        String [] arr = gradeRewards.split(":");
        this.day = Integer.parseInt(arr[0]);
        this.minGrade = Float.parseFloat(arr[1]);
        this.maxGrade = Float.parseFloat(arr[2]);
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public float getMinGrade() {
        return minGrade;
    }

    public void setMinGrade(float minGrade) {
        this.minGrade = minGrade;
    }

    public float getMaxGrade() {
        return maxGrade;
    }

    public void setMaxGrade(float maxGrade) {
        this.maxGrade = maxGrade;
    }
}
