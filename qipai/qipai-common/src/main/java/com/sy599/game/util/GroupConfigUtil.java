package com.sy599.game.util;


import com.sy599.game.db.bean.group.SysGroupLevelConfig;
import com.sy599.game.db.bean.group.SysGroupUserLevelConfig;
import com.sy599.game.db.dao.group.GroupConfigDao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupConfigUtil {
    private static Map<Integer, SysGroupLevelConfig> groupLevelConfigMap = new HashMap<>();
    private static Map<Integer, SysGroupUserLevelConfig> groupUserLevelConfigMap = new HashMap<>();


    public final static void initGroupConfig() {
        try {
            List<SysGroupLevelConfig> sysGroupLevelConfigs = GroupConfigDao.getInstance().loadAllGroupLevelConfig();
            for (SysGroupLevelConfig config : sysGroupLevelConfigs) {
                groupLevelConfigMap.put(config.getLevel(), config);
            }

            List<SysGroupUserLevelConfig> sysGroupUserLevelConfigs = GroupConfigDao.getInstance().loadAllGroupUserLevelConfig();
            for (SysGroupUserLevelConfig config : sysGroupUserLevelConfigs) {
                groupUserLevelConfigMap.put(config.getLevel(), config);
            }
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
}
