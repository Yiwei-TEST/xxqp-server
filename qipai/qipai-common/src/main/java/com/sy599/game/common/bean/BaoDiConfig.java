package com.sy599.game.common.bean;

import com.sy599.game.util.LogUtil;
import org.apache.commons.lang3.StringUtils;

public class BaoDiConfig {
    private long start;
    private long end;
    private long baoDi;

    public BaoDiConfig(long start, long end, long baoDi) {
        this.start = start;
        this.end = end;
        this.baoDi = baoDi;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public long getBaoDi() {
        return baoDi;
    }

    public void setBaoDi(long baoDi) {
        this.baoDi = baoDi;
    }

    public static BaoDiConfig parseString(String configStr) {
        try {
            if (StringUtils.isBlank(configStr)) {
                return null;
            }
            String[] splits = configStr.split("[|]");
            return new BaoDiConfig(Long.valueOf(splits[0]), Long.valueOf(splits[1]), Long.valueOf(splits[2]));
        } catch (Exception e) {
            LogUtil.errorLog.error("BaoDiConfig|parseString|" + configStr, e);
        }
        return null;
    }

    public static String toDBString(BaoDiConfig config) {
        if (config == null) {
            return "";
        }
        return config.getStart() + "|" + config.getEnd() + "|" + config.getBaoDi();
    }

}
