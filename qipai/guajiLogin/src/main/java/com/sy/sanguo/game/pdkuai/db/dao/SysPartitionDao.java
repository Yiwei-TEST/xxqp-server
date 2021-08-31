package com.sy.sanguo.game.pdkuai.db.dao;


import com.sy.sanguo.game.pdkuai.db.bean.SysPartition;

import java.util.HashMap;
import java.util.List;

public class SysPartitionDao extends BaseDao {

    private static SysPartitionDao inst = new SysPartitionDao();

    public static SysPartitionDao getInst() {
        return inst;
    }


    public List<SysPartition> loadAllSysPartition() throws Exception {
        return (List<SysPartition>) this.getSql().queryForList("sys_partition.loadAllSysPartition");
    }

    public List<Long> loadAllGroupId() throws Exception {
        return (List<Long>) this.getSql().queryForList("sys_partition.loadAllGroupId");
    }

    public int insertTempGroupPartition(HashMap<String, Object> map) throws Exception {
        return (int)this.getSql().update("sys_partition.insertTempGroupPartition", map);
    }

}
