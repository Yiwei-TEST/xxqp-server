package com.sy599.game.common.executor.task;

import com.sy599.game.robot.RobotManager;
import com.sy599.game.util.GoldRoomUtil;
import com.sy599.game.util.GroupConfigUtil;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.ResourcesConfigsUtil;
import com.sy599.game.util.SoloRoomUtil;
import com.sy599.game.util.SysPartitionUtil;

import java.util.Calendar;
import java.util.Date;
import java.util.TimerTask;

/**
 * 每分钟定时任务
 */
public class OneMinuteFixedRateTask extends TimerTask {

    public static final Date loadFirstExecuteDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.MINUTE, 1);
        return calendar.getTime();
    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();

        try {
            GroupConfigUtil.initGroupConfig();
        } catch (Throwable t) {
            LogUtil.errorLog.error("OneMinuteFixedRateTask|error|", t);
        }
        LogUtil.monitorLog.info("OneMinuteFixedRateTask|initGroupConfig|" + (System.currentTimeMillis() - start));

        try {
            //加载数据库资源配置
            ResourcesConfigsUtil.initResourcesConfigs();
        } catch (Throwable t) {
            LogUtil.errorLog.error("OneMinuteFixedRateTask|error|", t);
        }

        try {
            //加载数据库资源配置
            SysPartitionUtil.refreshConfig();
        } catch (Throwable t) {
            LogUtil.errorLog.error("OneMinuteFixedRateTask|error|", t);
        }

        try {
            //加载数据库资源配置
            GoldRoomUtil.init();
        } catch (Throwable t) {
            LogUtil.errorLog.error("OneMinuteFixedRateTask|error|", t);
        }

        try {
            //加载数据库资源配置
            SoloRoomUtil.init();
        } catch (Throwable t) {
            LogUtil.errorLog.error("OneMinuteFixedRateTask|error|", t);
        }

        try {
            //加载数据库资源配置
            RobotManager.init();
        } catch (Throwable t) {
            LogUtil.errorLog.error("OneMinuteFixedRateTask|error|", t);
        }

        LogUtil.monitorLog.info("OneMinuteFixedRateTask|initResourcesConfigs|" + (System.currentTimeMillis() - start));
    }

}
