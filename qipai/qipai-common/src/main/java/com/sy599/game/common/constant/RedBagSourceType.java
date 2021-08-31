package com.sy599.game.common.constant;

/**
 * 红包来源类型
 */
public enum RedBagSourceType {

    guafen_redbag(1, "瓜分现金红包活动"),// 比如小甘现金红包互动
    lucky_redbag(2, "转盘抽奖活动"),  // 比如王者千分转盘抽奖活动
    hupenghuanyou_redbag(3, "呼朋唤友互动"),
    oldBackGift_redbag(4, "回归礼包活动"),
    newPlayerGift_redbag(5, "新人有礼活动"),
    gameBureauStaticActivity(6, "牌局统计活动"),
    ;
    private int type;

    private String name;

    RedBagSourceType(int type, String name) {
        this.type = type;
        this.name = name;
    }

    public static RedBagSourceType getRedbagSourceType(int sourceType) {
        for(RedBagSourceType type : RedBagSourceType.values()) {
            if(type.getType() == sourceType) {
                return type;
            }
        }
        return null;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
