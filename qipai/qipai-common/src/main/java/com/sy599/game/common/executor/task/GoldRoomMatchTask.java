package com.sy599.game.common.executor.task;

import com.sy599.game.util.GoldRoomMatchUtil;
import com.sy599.game.util.LogUtil;

public class GoldRoomMatchTask implements Runnable {

    @Override
    public void run() {
        try {
            long start = System.currentTimeMillis();
            try {
                GoldRoomMatchUtil.doMatch();
            } catch (Throwable e) {
                LogUtil.errorLog.error("GoldRoomMatchTask|error|" + e.getMessage(), e);
            }
            long timeUse = System.currentTimeMillis() - start;
            if (timeUse > 100) {
                LogUtil.monitorLog.info("GoldRoomMatchTask|" + timeUse);
            }
        } catch (Throwable t) {
            LogUtil.errorLog.error("GoldRoomMatchTask|" + t.getMessage(), t);
        }
    }
}
