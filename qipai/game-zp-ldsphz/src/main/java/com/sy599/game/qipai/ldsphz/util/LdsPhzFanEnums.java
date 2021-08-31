package com.sy599.game.qipai.ldsphz.util;

public enum LdsPhzFanEnums {
    WANG_DIAO(4),
    WANG_DIAO_WANG(8),
    WANG_CHUANG(8),
    WANG_CHUANG_WANG(16),
    WANG_ZHA(16),
    WANG_ZHA_WANG(32),
    DIAN_HU(3),
    HONG_HU(2),
    HEI_HU(4),
    ALL_HONG(4),
    HONG_TO_DIAN(32),
    HONG_TO_HEI(42),
    SELF(2),
    TIAN_HU(2),
    DI_HU(2),
    HU(1);

    private int fan;

    LdsPhzFanEnums(int fan){
        this.fan=fan;
    }

    public int getFan() {
        return fan;
    }

    @Override
    public String toString() {
        return name()+" "+fan;
    }
}
