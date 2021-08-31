package com.sy599.game.db.dao.group;


import com.sy599.game.db.bean.group.SysGroupLevelConfig;
import com.sy599.game.db.bean.group.SysGroupUserLevelConfig;
import com.sy599.game.db.dao.BaseDao;

import java.util.List;

public class GroupConfigDao extends BaseDao {

    private static GroupConfigDao inst = new GroupConfigDao();

    public static GroupConfigDao getInstance() {
        return inst;
    }

    public List<SysGroupLevelConfig> loadAllGroupLevelConfig() throws Exception {
        return (List<SysGroupLevelConfig>) this.getSqlLoginClient().queryForList("groupConfig.loadAllGroupLevelConfig");
    }

    public List<SysGroupUserLevelConfig> loadAllGroupUserLevelConfig() throws Exception {
        return (List<SysGroupUserLevelConfig>) this.getSqlLoginClient().queryForList("groupConfig.loadAllGroupUserLevelConfig");
    }
}
