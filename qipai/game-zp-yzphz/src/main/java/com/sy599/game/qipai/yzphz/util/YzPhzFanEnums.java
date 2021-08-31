package com.sy599.game.qipai.yzphz.util;

import java.util.HashSet;
import java.util.Set;

public enum YzPhzFanEnums {
    WANG_DIAO(4),
    WANG_DIAO_WANG(8),
    WANG_CHUANG(8),
    WANG_CHUANG_WANG(16),
    WANG_ZHA(16),
    WANG_ZHA_WANG(32),
    DIAN_HU(3),
    HONG_HU(2),
    HEI_HU(4),
    HONG_TO_DIAN(3),
    HONG_TO_HEI(4),
    SELF(2),
    TIAN_HU(1),
    HU(1);

    private int fan;

    YzPhzFanEnums(int fan){
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
