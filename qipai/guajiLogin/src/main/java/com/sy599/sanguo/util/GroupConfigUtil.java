package com.sy599.sanguo.util;

import com.sy.sanguo.game.bean.group.SysGroupLevelConfig;
import com.sy.sanguo.game.bean.group.SysGroupUserLevelConfig;
import com.sy.sanguo.game.pdkuai.db.dao.GroupConfigDao;
import com.sy.sanguo.game.pdkuai.util.LogUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupConfigUtil {
    private static Map<Integer, SysGroupLevelConfig> groupLevelConfigMap = new HashMap<>();
    private static Map<Integer, SysGroupUserLevelConfig> groupUserLevelConfigMap = new HashMap<>();


    public final static void initGroupConfig() {
        try {
            List<SysGroupLevelConfig> sysGroupLevelConfigs = GroupConfigDao.getInstance().loadAllGroupLevelConfig();
            Map<Integer, SysGroupLevelConfig> gTemp = new HashMap<>();
            for (SysGroupLevelConfig config : sysGroupLevelConfigs) {
                gTemp.put(config.getLevel(), config);
//                LogUtil.i("initGroupConfig|groupLevelConfig|" + JSON.toJSONString(config));
            }
            groupLevelConfigMap = gTemp;

            List<SysGroupUserLevelConfig> sysGroupUserLevelConfigs = GroupConfigDao.getInstance().loadAllGroupUserLevelConfig();
            Map<Integer, SysGroupUserLevelConfig> guTemp = new HashMap<>();
            for (SysGroupUserLevelConfig config : sysGroupUserLevelConfigs) {
                guTemp.put(config.getLevel(), config);
//                LogUtil.i("initGroupConfig|groupUserLevelConfig|" + JSON.toJSONString(config));
            }
            groupUserLevelConfigMap = guTemp;
        } catch (Exception e) {
            LogUtil.e("initGroupConfig|error|", e);
        }
    }


    public static SysGroupLevelConfig getGroupLevelConfig(int level) {
        SysGroupLevelConfig config = groupLevelConfigMap.get(level);
        if (config == null) {
            throw new RuntimeException("getGroupLevelConfig|error|" + level);
        }
        return config;
    }

    public static SysGroupUserLevelConfig getGroupUserLevelConfig(int level) {
        SysGroupUserLevelConfig config = groupUserLevelConfigMap.get(level);
        if (config == null) {
            throw new RuntimeException("getGroupUserLevelConfig|error|" + level);
        }
        return config;
    }

    public static List<SysGroupLevelConfig> getGroupLevelConfigList() {
        return new ArrayList<>(groupLevelConfigMap.values());
    }

    public static List<SysGroupUserLevelConfig> getGroupUserLevelConfigList() {
        return new ArrayList<>(groupUserLevelConfigMap.values());
    }
}
