package com.sy599.game.staticdata.bean;


import com.sy599.game.staticdata.model.RewardBean;
import com.sy599.game.util.LogUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GameBureauActivityConfig extends ActivityConfigInfo {
    private List<RewardBean> rewardBeans = new ArrayList<>(); //奖励配置
    private Date activeEndDate = null;
    private Date activeStartDate = null;

    @Override
    public void configParamsAndRewards() {
        try {
            String[] paramArr = params.split(";");
            activeStartDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(paramArr[0]);
            activeEndDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(paramArr[1]);

            String[] rewardStrArr = rewards.split(";");
            for (String rewardStr : rewardStrArr) {
                String[] temps = rewardStr.split("_");
                if (temps.length == 3) {
                    RewardBean rewardBean = new RewardBean();
                    rewardBean.setData(Integer.parseInt(temps[0]));  //需要完成
                    rewardBean.setType(Integer.parseInt(temps[1]));  //奖励类型
                    rewardBean.setValue(Float.parseFloat(temps[2]));  //奖励额
                    rewardBeans.add(rewardBean);  //奖励配置
                }
            }
        } catch (Exception e) {
            LogUtil.errorLog.error("GameBureauStaticActivityConfig|error|" + e.getMessage(), e);
        }
    }

    @Override
    public boolean isActive() {
        long now = System.currentTimeMillis();
        return activeStartDate != null && now > activeStartDate.getTime() && activeEndDate != null && now < activeEndDate.getTime();
    }

    public Date getActiveEndDate() {
        return activeEndDate;
    }

    public List<RewardBean> getRewardBeans() {
        return rewardBeans;
    }
}
