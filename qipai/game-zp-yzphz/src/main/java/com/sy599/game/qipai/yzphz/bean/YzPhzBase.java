package com.sy599.game.qipai.yzphz.bean;

/**
 * 跑胡子
 */
public interface YzPhzBase {
    /**
     * 起胡息
     * @return
     */
    int loadQihuxi();

    /**
     * 红胡，最小红牌数量
     * @return
     */
    int loadRedCount();

    /**
     * 是否红转黑
     * @return
     */
    boolean checkRed2Black();
    /**
     * 是否红转点
     * @return
     */
    boolean checkRed2Dian();

    /**
     * 红转黑，红牌数量
     * @return
     */
    int loadRed2BlackCount();

    /**
     *红转点，红牌数量
     * @return
     */
    int loadRed2DianCount();

    /**
     * 胡牌起始屯数
     * @return
     */
    int loadBaseTun();

    /**
     * 额外胡息屯数
     * @return
     */
    int loadCommonTun(int totalHuxi);

    /**
     * 胡息总屯数屯数（loadBaseTun()+loadCommonTun(int totalHuxi)）
     * @see #loadBaseTun()
     * @see #loadCommonTun(int)
     * @return
     */
    int loadScoreTun(int totalHuxi);

    /**
     *醒模式，0跟醒1翻醒
     * @return
     */
    int loadXingMode();

    /**
     *醒屯数
     * @return
     */
    int loadXingTun(int xingCount);

    /**
     *醒牌
     * @return
     */
    int loadXingCard(boolean tiqian);
    /**
     *胡牌模式 0，有王必须自摸，1按王限胡，2按番限胡
     */
    int loadHuMode();

    /**
     * 是否天胡
     *
     * @return
     */
    boolean isTianHu();

}
