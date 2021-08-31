package com.sy599.game.common.executor.task;

import java.util.concurrent.atomic.AtomicLong;

import com.sy599.game.manager.TableManager;
import com.sy599.game.util.LogUtil;

public class CompetitionTask implements Runnable {
    private static final AtomicLong ATOMIC_LONG=new AtomicLong(0);

    @Override
    public void run() {
        try {
            long curNo = ATOMIC_LONG.addAndGet(1);
            long time1 = System.currentTimeMillis();
            boolean record = curNo % 60 == 0;
            if (record) {
                LogUtil.monitorLog.info("checkCompetitionTask start::curNo={}",curNo);
            }

            //自动完成任务
            try {
                TableManager.getInstance().checkCompetitionTask();
            } catch (Throwable e) {
                LogUtil.errorLog.error("CompetitionTask err1:" + e.getMessage(), e);
            }

            long time2 = System.currentTimeMillis();

            if (record) {
                LogUtil.monitorLog.info("checkCompetitionTask end::time(ms):{} ,curNo={}" ,(time2 - time1), curNo);
            }

        }catch (Throwable t){
            LogUtil.errorLog.error("CompetitionTask err1:" + t.getMessage(), t);
        }
    }

}
