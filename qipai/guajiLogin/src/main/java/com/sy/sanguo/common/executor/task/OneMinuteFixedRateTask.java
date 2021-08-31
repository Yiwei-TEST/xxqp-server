package com.sy.sanguo.common.executor.task;

import com.sy.sanguo.game.pdkuai.util.LogUtil;
import com.sy599.sanguo.util.GroupConfigUtil;
import com.sy599.sanguo.util.ResourcesConfigsUtil;
import com.sy599.sanguo.util.SysPartitionUtil;

import java.util.Calendar;
import java.util.Date;
import java.util.TimerTask;

public class OneMinuteFixedRateTask extends TimerTask {

    public static final Date loadFirstExecuteDate() {
        Calendar calendar = Calendar.getInstance();
        int m = calendar.get(Calendar.SECOND);
        calendar.add(Calendar.SECOND, 60 - (m % 60));
        return calendar.getTime();
    }

    @Override
    public void run() {
        try {
            //加载数据库资源配置
            ResourcesConfigsUtil.initResourcesConfigs();
        } catch (Exception e) {
            LogUtil.e("Exception:" + e.getMessage(), e);
        }

        try {
            // 加载俱乐部等级配置
            GroupConfigUtil.initGroupConfig();
        } catch (Exception e) {
            LogUtil.e("Exception:" + e.getMessage(), e);
        }

        try {
            // 加载分表功能配置
            SysPartitionUtil.refreshConfig();
        } catch (Exception e) {
            LogUtil.e("Exception:" + e.getMessage(), e);
        }
    }
}