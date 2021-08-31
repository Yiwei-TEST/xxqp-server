package com.sy599.game.common;

/**
 * 玩家资源
 */
public enum UserResourceType {
    /**
     * 体力
     */
    TILI(100, "体力"),
    /**
     * 房卡/钻石
     */
    CARD(101, "钻石"),
    /**
     * 金币
     */
    GOLD(102, "金币"),
    /**
     * 积分（芒果跑得快）
     */
    JIFEN(103, "积分"),
    /**
     * 礼券
     */
    TICKET(104, "礼券");


    private int type;

    private String name;

    UserResourceType(int type, String name){
        this.type = type;
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
