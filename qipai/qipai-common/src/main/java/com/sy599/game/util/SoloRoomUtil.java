package com.sy599.game.util;

import com.sy599.game.db.bean.SoloRoomConfig;
import com.sy599.game.db.dao.SoloRoomDao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class SoloRoomUtil {

    public static final Map<Long, SoloRoomConfig> ALL_MAP = new HashMap<>();
    public static final Map<Integer, List<SoloRoomConfig>> TYPE_MAP = new HashMap<>();

    public static void init() {
        initSoloRoomConfig();
    }

    public static void initSoloRoomConfig() {
        try {
            List<SoloRoomConfig> all = SoloRoomDao.getInstance().loadAllGoldRoomConfig();
            if (all == null || all.size() == 0) {
                return;
            }
            Set<Integer> cleared = new HashSet<>();
            for (SoloRoomConfig config : all) {
                ALL_MAP.put(config.getKeyId(), config);
                List<SoloRoomConfig> list = TYPE_MAP.get(config.getSoloType());
                if (list == null) {
                    list = new ArrayList<>();
                    TYPE_MAP.put(config.getSoloType(), list);
                } else {
                    if (!cleared.contains(config.getSoloType())) {
                        list.clear();
                        cleared.add(config.getSoloType());
                    }
                }
                list.add(config);
            }
        } catch (Exception e) {
            LogUtil.errorLog.error("GoldRoomUtil|initSoloRoomConfig|error" + e.getMessage(), e);
        }
    }

    public static List<SoloRoomConfig> getSoloRoomConfigList(int soloType) {
        return TYPE_MAP.get(soloType);
    }


    public static SoloRoomConfig getSoloRoomConfig(long keyId) {
        return getSoloRoomConfig(keyId, true);
    }


    /**
     * 金币场配置
     *
     * @param keyId   t_gold_room_config.keyId
     * @param isValid 是否必须是有效的
     * @return
     */
    public static SoloRoomConfig getSoloRoomConfig(long keyId, boolean isValid) {
        SoloRoomConfig config = ALL_MAP.get(keyId);
        if (config == null) {
            return null;
        }
        if (isValid && !config.isValid()) {
            return null;
        }
        return config;
    }

}
