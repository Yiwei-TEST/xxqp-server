package com.sy.sanguo.game.pdkuai.db.dao;

import com.sy.sanguo.game.bean.group.SysGroupLevelConfig;
import com.sy.sanguo.game.bean.group.SysGroupUserLevelConfig;

import java.util.List;

public class GroupConfigDao extends BaseDao {

    private static GroupConfigDao inst = new GroupConfigDao();

    public static GroupConfigDao getInstance() {
        return inst;
    }

    public List<SysGroupLevelConfig> loadAllGroupLevelConfig() throws Exception {
        return (List<SysGroupLevelConfig>) this.getSql().queryForList("groupConfig.loadAllGroupLevelConfig");
    }

    public List<SysGroupUserLevelConfig> loadAllGroupUserLevelConfig() throws Exception {
        return (List<SysGroupUserLevelConfig>) this.getSql().queryForList("groupConfig.loadAllGroupUserLevelConfig");
    }
}
