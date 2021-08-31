package com.sy599.game.gcommand.login.base;

import com.sy599.game.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PwErrorMsg {

    private static final int limit_second = 10 * 60 * 1000;


    private List<Long> timeList;
    private Long now;

    public List<Long> getTimeList() {
        return timeList;
    }

    public void setTimeList(List<Long> timeList) {
        this.timeList = timeList;
    }

    public Long getNow() {
        return now;
    }

    public void setNow(Long now) {
        this.now = now;
    }

    public static PwErrorMsg init(String pwErrorMsg) {
        long now = System.currentTimeMillis();
        List<Long> timeList = new ArrayList<>();
        if (StringUtils.isNotBlank(pwErrorMsg)) {
            timeList = StringUtil.explodeToLongList(pwErrorMsg);
            if (timeList.size() < 3 || now - timeList.get(2) > limit_second) {
                Iterator<Long> iterator = timeList.iterator();
                while (iterator.hasNext()) {
                    long time = iterator.next();
                    if (now - time >= 60 * 1000) {
                        iterator.remove();
                    }
                }
            }
        }
        PwErrorMsg bean = new PwErrorMsg();
        bean.setTimeList(timeList);
        bean.setNow(now);
        return bean;
    }

    public String limitMsg() {
        if (timeList.size() < 3) {
            return null;
        }
        int second = (int) ((limit_second - (now - timeList.get(2))) / 1000);
        return second + "";
    }

    public void add() {
        timeList.add(now);
    }

    public String toString() {
        if (timeList.size() > 0) {
            return StringUtil.implodeLongToStr(timeList, ",");
        }
        return "";
    }

}
