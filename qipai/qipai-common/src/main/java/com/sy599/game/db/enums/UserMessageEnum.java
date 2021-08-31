package com.sy599.game.db.enums;

public enum UserMessageEnum {
    /**
     * 通用
     */
    TYPE0(0),
    /**
     * 通用
     */
    TYPE1(1),
    /**
     * 砸金蛋
     */
    TYPE2(2),
    /**
     * 比赛场
     */
    TYPE3(3),
    /**
     * 礼券兑换商品
     */
    TYPE4(4);

    UserMessageEnum(int type){
        this.type = type;
    }

    private int type;

    public int type(){
        return this.type;
    }
}
