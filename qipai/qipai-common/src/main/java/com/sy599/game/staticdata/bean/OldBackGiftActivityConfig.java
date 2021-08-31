package com.sy599.game.staticdata.bean;


import com.sy599.game.staticdata.model.OldBackGiftActivityBean;

import java.security.SecureRandom;
import java.util.*;

public class OldBackGiftActivityConfig extends ActivityConfigInfo
{
    private int unLofinDay;  //回归玩家判断条件【离开天数】
    private int continuousDay;  //活动持续天数
    private int dayMax;  //最大有奖励的登录天数
    private int payCount;  //要求对局数
    private List<OldBackGiftActivityBean> rewardDiam = new ArrayList<>(); //奖励配置

    private Map<Integer, float[]> grades = new HashMap<>();  //红包等级配置
    private int totalRatio;  //总比率
    private int maxGrade; //最大等级

    @Override
    public void configParamsAndRewards() {
//        String param = "15;10;7;20";
//        String reard = "1_1_10;2_1_20;3_1_30;4_1_30;5_1_30;6_1_30;7_2_0;1_2_1.88_200;2_2_2.88_200;3_2_3.88_200;4_2_5.88_150;5_2_6.88_100;6_2_7.88_100;7_2_8.88_50";
        String[] activityParams = params.split(";");
        unLofinDay = Integer.parseInt(activityParams[0]);
        continuousDay =  Integer.parseInt(activityParams[1]);
        dayMax =  Integer.parseInt(activityParams[2]);
        payCount = Integer.parseInt(activityParams[3]);
        maxGrade = 0;
        totalRatio = 0;
        String[] strRewades = rewards.split("\\|");
        for(String strRewade : strRewades)
        {
            String[] strs = strRewade.split(";");
            for(String str : strs)  //奖励
            {
                String[] temps = str.split("_");
                if(temps.length == 3)  //奖励配置
                {
                    OldBackGiftActivityBean oldBackGiftActivityBean = new OldBackGiftActivityBean();
                    oldBackGiftActivityBean.setDayCount(Integer.parseInt(temps[0]));  //需要登录天数
                    oldBackGiftActivityBean.setRewardType(Integer.parseInt(temps[1]));  //奖励类型
                    oldBackGiftActivityBean.setReward(Integer.parseInt(temps[2]));  //奖励额
                    rewardDiam.add(oldBackGiftActivityBean);  //奖励配置
                }

                if(temps.length == 4)  //红包档次配置
                {
                    float[] arr = new float[5];
                    arr[0] = Float.parseFloat(temps[0]);// 奖励档次ID
                    arr[1] = Float.parseFloat(temps[1]);// 奖励类型  1钻石 2现金红包
                    arr[2] = Float.parseFloat(temps[2]);// 金额
                    float ratio = Float.parseFloat(temps[3]); // 概率
                    arr[3] = ratio;
                    totalRatio += ratio;  //总比率
                    arr[4] = totalRatio;
                    int grade = (int) arr[0];
                    grades.put(grade, arr);  //红包等级
                    if(grade > maxGrade)
                    {
                        maxGrade = grade;  //最大等级
                    }
                }
            }
        }
    }

    //获取随机红包等级
    public int randomGrade()
    {
        Random random = new SecureRandom();
        int value = random.nextInt(totalRatio) + 1;
        for(int grade = 1; grade <= maxGrade; grade++)
        {
            float[] curGrades = grades.get(grade);
            if(value <= curGrades[4]){
                return grade;
            }
        }
        return 1;
    }

    public int getPayCount() {
        return payCount;
    }

    public void setPayCount(int payCount) {
        this.payCount = payCount;
    }

    public int getUnLofinDay() {
        return unLofinDay;
    }

    public void setUnLofinDay(int unLofinDay) {
        this.unLofinDay = unLofinDay;
    }

    public int getDayMax() {
        return dayMax;
    }

    public void setDayMax(int dayMax) {
        this.dayMax = dayMax;
    }

    public List<OldBackGiftActivityBean> getRewardDiam() {
        return rewardDiam;
    }

    public void setRewardDiam(List<OldBackGiftActivityBean> rewardDiam) {
        this.rewardDiam = rewardDiam;
    }

    public int getContinuousDay() {
        return continuousDay;
    }

    public void setContinuousDay(int continuousDay) {
        this.continuousDay = continuousDay;
    }

    public Map<Integer, float[]> getGrades() {
        return grades;
    }

    public void setGrades(Map<Integer, float[]> grades) {
        this.grades = grades;
    }

    public int getTotalRatio() {
        return totalRatio;
    }

    public void setTotalRatio(int totalRatio) {
        this.totalRatio = totalRatio;
    }

    public int getMaxGrade() {
        return maxGrade;
    }

    public void setMaxGrade(int maxGrade) {
        this.maxGrade = maxGrade;
    }
}
