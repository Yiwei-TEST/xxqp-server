package com.sy.sanguo.game.bean.enums;


public enum SourceType {
    // 1-10000 给server机
    // 10001 - 20000 给登录机
    // 20001 - 30000 给代理后台
    unknown(-1, "未知来源"),
    share_award(10001, "分享领金币"),
    bind_phone(10002, "绑定手机"),

    award_user(20001, "后台奖励"),
    ;
    private int type;

    private String name;

    SourceType(int type, String name) {
        this.type = type;
        this.name = name;
    }

    public int type() {
        return type;
    }

    public String getSourceName() {
        return name;
    }

}
