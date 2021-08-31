package com.sy.sanguo.common.executor.task;

import java.util.Calendar;
import java.util.Date;
import java.util.TimerTask;

/**
 * Created by pc on 2017/5/19.
 */
public class TenMinuteFixedRateTask extends TimerTask {


    public static final Date loadFirstExecuteDate() {
        Calendar calendar = Calendar.getInstance();
        int m = calendar.get(Calendar.MINUTE);
        calendar.add(Calendar.MINUTE, 10 - (m % 10));
        return calendar.getTime();
    }

    @Override
    public void run() {
    }
}
