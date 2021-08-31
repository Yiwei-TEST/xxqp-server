package com.sy599.game.qipai.ldsphz.util;

public enum LdsPhzHuXiEnums {

    TI(4, 12, 9),
    PAO(7, 9, 6),
    KAN(8, 6, 3),
    WEI(3, 6, 3),
    WEI_CHOU(10, 6, 3),
    PENG(2, 3, 1),
    CHI123(6, 6, 3),
    CHI2710(6, 6, 3),
    CHI(6, 0, 0),
    JIAO(6, 0, 0),
    DUI(0, 0, 0),
    WANG_SMALL_KAN(3, 3, 3),
    WANG_SMALL_TI(4, 9, 9);

    private final int big;
    private final int small;
    private final int action;

    LdsPhzHuXiEnums(int action, int big, int small) {
        this.action = action;
        this.big = big;
        this.small = small;
    }

    /**
     * CHI123,CHI2710,CHI
     *
     * @param huXiEnums
     * @return
     */
    public final static boolean isChi(LdsPhzHuXiEnums huXiEnums) {
        return huXiEnums == CHI123 || huXiEnums == CHI2710 || huXiEnums == CHI;
    }

    /**
     * CHI123,CHI2710,CHI,JIAO
     *
     * @param huXiEnums
     * @return
     */
    public final static boolean isChi0(LdsPhzHuXiEnums huXiEnums) {
        return huXiEnums == CHI123 || huXiEnums == CHI2710 || huXiEnums == CHI || huXiEnums == JIAO;
    }

    public int getBig() {
        return big;
    }

    public int getSmall() {
        return small;
    }

    public int getAction() {
        return action;
    }
}
