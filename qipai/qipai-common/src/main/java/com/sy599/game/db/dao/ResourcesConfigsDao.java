package com.sy599.game.db.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sy599.game.db.bean.MissionConfig;
import com.sy599.game.db.bean.ResourcesConfigs;
import com.sy599.game.db.bean.SevenSignConfig;
import com.sy599.game.db.bean.TWinReward;

public class ResourcesConfigsDao extends BaseDao {

    private static ResourcesConfigsDao resourcesConfigsDao = new ResourcesConfigsDao();

    public static ResourcesConfigsDao getInstance() {
        return resourcesConfigsDao;
    }


    public List<ResourcesConfigs> loadAllConfigs() throws Exception {
        return (List<ResourcesConfigs>) this.getSqlLoginClient().queryForList("resources_configs.load_all_configs");
    }

    public ResourcesConfigs loadOneConfig(String msgType, String msgKey) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("msgType", msgType);
        map.put("msgKey", msgKey);
        List<ResourcesConfigs> list = this.getSqlLoginClient().queryForList("resources_configs.loadOneConfig", map);
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    public List<SevenSignConfig> loadSevenSignConfig() throws Exception {
        return (List<SevenSignConfig>)this.getSqlLoginClient().queryForList("resources_configs.loadSevenSign");
    }

    public List<MissionConfig> loadMissionConfig() throws Exception {
        return (List<MissionConfig>)this.getSqlLoginClient().queryForList("resources_configs.loadMissionConfig");
    }
    public List<TWinReward> queryAllTwinReward() throws Exception {
        List<TWinReward> queryForList = (List<TWinReward>)this.getSqlLoginClient().queryForList("resources_configs.queryAllTwinReward");
		return queryForList;
    }
}
