package com.sy599.game.qipai.xtpaohuzi.bean;

import com.alibaba.fastjson.annotation.JSONField;
import com.sy599.game.qipai.xtpaohuzi.constant.HuType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Guang.OuYang
 * @date 2019/9/4-14:08
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameModel {
    //游戏局数 5/6/8/10
    private int gameFinishRound;
    //游戏遇到最大胡息总结算 解散房间
    private int gameFinishMaxHuXi;
    //游戏最大人数 2/3
    private int gameMaxHuman;
    //支持方式 1AA 2房主
    private int payType;
    //抽取底牌数量 0/10/20
    private int discardHoleCards;
    //单局最低胡息起胡 10/15
    private int roundFinishLowestHuXi;
    //胡息转换为囤的计算方式 1息1囤(逢3加1)/1息1囤/3息1囤
    private int converHuXiToTunRatio;
    //支持的特殊玩法 1自摸+3胡,2一五十,3 30胡翻倍,4黄庄5胡,5一点红,6天地胡,7大小字,8碰碰胡,9红黑胡,10名堂叠加,11吃边打边
    private SpecialPlay specialPlay = new SpecialPlay();
    //0随机 1先进房坐庄
    private int changeBankerWay;
//    //自动托管0无, 60s后, 120s后, 180s后, 300s后
//    private int autoOutCard;
    //加倍0不加倍,1加倍
    private int doubleChip;
    //加倍生效分, 1小于10分时加倍
    private int doubleChipLeChip;
    //加倍倍数1翻两倍 3翻三倍 4翻四倍
    private int doubleRatio;
    /*
    6.3 打坨：建房三选一选项：不打坨/坨对坨3番/坨对坨4番
    6.4 建房选择不打坨：全部准备直接发牌开始没有打坨/不打坨选项。
    6.5 建房时选择坨对坨3番：全部准备后出现选项打坨/不打坨，情况1：两家选择不打坨，结算时两家胡息相减后得到的积分不变；情况2：一家选择打坨一家选择不打坨，结算积分*2。情况3：两家都选择打坨，结算积分*3。
    6.6 建房时选择坨对坨4番：全部准备后出现选项打坨/不打坨，情况1：两家选择不打坨，结算时两家胡息相减后得到的积分不变；情况2：一家选择打坨一家选择不打坨，结算积分*2。情况3：两家都选择打坨，结算积分*4。
    0不坨,3.3番,4.4番
    */
    private int tuo;
    //选坨超时时间
    private long tuoTimeOut;
    private long nowTime;
    private boolean repeatedSendTuo;

    //计分规则
    //1建房选择囤数计分：分数=（(胡息数-12或7)/3向下取整）*番数
    //2建房选择胡息计分：分数=（胡息数*番数-12或7）/3向下取整。
    private int tunOrHuXi;

    //最大胡息限制,0平胡,1自摸/接炮
    private int[] maxHuXiLimit;

    //低于多少号分阈值
    private int lowScoreLimit;
    //低于多少分+多少分
    private int lowScoreAdd;


    @JSONField(serialize = false)
    public void resetTuo() {
        this.nowTime = System.currentTimeMillis();
        this.repeatedSendTuo = false;
    }

    /**
     *@description 踩到胡牌种类的胡息上限
     *@param huType 胡牌种类, 1平胡,2自摸,3接炮,4黄庄
     *@return  默认未上限
     *@author Guang.OuYang
     *@date 2019/9/7
     */
    public int limitUpper(HuType huType, int huxi) {
        if (maxHuXiLimit != null && huType != null && huType.ordinal() <= maxHuXiLimit.length) {
            return maxHuXiLimit[huType.ordinal()] >= huxi ? huxi : maxHuXiLimit[huType.ordinal()];
        }
        return huxi;
    }

    /**
     * @param currentScore 当前分
     * @return 1不加倍, 其他加倍
     * @description 检测和返回当前可用的倍数,
     * @author Guang.OuYang
     * @date 2019/9/4
     */
    @JSONField(serialize = false)
    public int doubleChipEffect(int currentScore) {
        return Math.max(isDoubleChip() && Math.abs(currentScore) < doubleChipLeChip ? doubleRatio : 1, 1);
    }

    @JSONField(serialize = false)
    public int lowScoreEffect(int currentScore) {
        return lowScoreLimit > 0 && Math.abs(currentScore) < lowScoreLimit ? currentScore > 0 ? lowScoreAdd : -lowScoreAdd : 0;
    }

    @JSONField(serialize = false)
    public boolean isDoubleChip() {
        return doubleChip == 1;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpecialPlay {
    	private boolean zzCaclScore;//株洲计分
        //自摸翻倍
        private boolean sinceTouchDoubleScore;
        //自摸+3胡
        private boolean sinceTouchAdd3Score;
        //一五十
        private boolean oneFiveTen;
        //4.9 30胡息：胡牌时胡息≥30，胡息翻倍(先翻倍再加自摸放炮10分)。
        //4.10 30胡息（十红）：30胡息以上，且红字数量>=10,算100胡。
        private boolean thirtyHuXiDoubleScore;
        //30胡息阈值
        private int thirtyHuXiDoubleScoreHu;
        //黄庄庄家扣10胡
        private int noneHuCardSubHuXi;
        //一点红
        private boolean aSmallRed;
        //天地胡
        private boolean skyFloorHu;
        //大小字
        private boolean maxMinChar;
        //碰碰胡
        private boolean ppHu;
        //红黑胡
        private boolean redBlackHu;
        //红胡数10or13张
        private int redHuCount;
        //名堂叠加,数量
        private int repeatedEffect;
        //吃边打边,吃大打小,吃小打大
        private boolean eatSideDozenEdge;
        //点炮
        private boolean ignite;
        //点炮必胡
        private boolean igniteMustHu;
        //偎牌 true明偎,0暗偎
        private boolean weiWay;

        public void optionNoneHuCardFiveHuXi(boolean flag) {
            if (!flag) {
                noneHuCardSubHuXi = 0;
            }
        }
    }
}
