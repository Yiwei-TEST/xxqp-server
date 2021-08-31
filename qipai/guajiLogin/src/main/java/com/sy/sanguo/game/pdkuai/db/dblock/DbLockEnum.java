package com.sy.sanguo.game.pdkuai.db.dblock;

public enum DbLockEnum {
    GROUP_USER_MODIFY_RELATION("group_user_modify_relation_", 60),
    CLEAR_TABLE_DATA("clear_table_data", 600),
    GROUP_GOLD_STATISTICS("group_gold_statistics", 600),
    ;
    String key;
    int overTimeSecond; // 时间,单位秒

    DbLockEnum(String key, int overTimeSecond) {
        this.key = key;
        this.overTimeSecond = overTimeSecond;
    }

    public String getKey() {
        return this.key;
    }

    public int getOverTimeSecond() {
        return this.overTimeSecond;
    }

    public String getLockKey(String identity) {
        return this.key + identity;
    }


}
