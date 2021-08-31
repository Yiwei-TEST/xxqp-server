package com.sy599.game.db.enums;

/**
 * 金币来源
 */
public enum CoinSourceType {
    unknown(0, "未知来源"),
    onTableStart(1, "开局扣除玩家金币"),
    onTableOver(2, "大结算计算玩家输赢金币"),
    onLevelUp(3, "玩家升级"),
    xipai(5, "洗牌"),
    ;

    private int sourceType;
    private String sourceName;

    CoinSourceType(int sourceType, String sourceName) {
        this.sourceType = sourceType;
        this.sourceName = sourceName;
    }

    public int getSourceType() {
        return sourceType;
    }

    public String getSourceName() {
        return sourceName;
    }
}
