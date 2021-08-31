package com.sy599.game.db.dao;

import com.sy599.game.db.bean.SysPartition;

import java.util.List;

public class SysPartitionDao extends BaseDao {

    private static SysPartitionDao inst = new SysPartitionDao();

    public static SysPartitionDao getInst() {
        return inst;
    }


    public List<SysPartition> loadAllSysPartition() throws Exception {
        return (List<SysPartition>) this.getSqlLoginClient().queryForList("sys_partition.loadAllSysPartition");
    }


}
