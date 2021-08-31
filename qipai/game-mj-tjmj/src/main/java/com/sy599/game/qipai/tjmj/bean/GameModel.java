package com.sy599.game.qipai.tjmj.bean;

import com.alibaba.fastjson.annotation.JSONField;
import com.sy599.game.qipai.tjmj.constant.MjAction;
import com.sy599.game.qipai.tjmj.rule.Mj;
import com.sy599.game.util.LogUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    //游戏最大人数 2/3
    private int gameMaxHuman;
    //支持方式 1AA 2房主
    private int payType;
    //抽取底牌数量 0/10/20
    private int discardHoleCards;
    //支持的特殊玩法
    private SpecialPlay specialPlay = new SpecialPlay();
    //0随机 1先进房坐庄
    private int changeBankerWay;
    //加倍0不加倍,1加倍
    private int doubleChip;
    //加倍生效分, 1小于10分时加倍
    private int doubleChipLeChip;
    //加倍倍数1翻两倍 3翻三倍 4翻四倍
    private int doubleRatio;
    //底分翻倍
    private int basicRatio;

    private long tuoTimeOut;
    private long nowTime;
    private boolean repeatedSendTuo;

    //报听, 有人触发报听 -1全部选择完毕
    private int signTing;

    /**
     * 低于多少号分阈值
     */
    private int lowScoreLimit;
    /**
     * 低于多少分+多少分
     */
    private int lowScoreAdd;
    /**
     * 计算庄闲
     */
    private boolean calcBanker;
    /**
     * 分数限制
     */
    private int topFen;

    /**8王玩法*/
    private boolean eightKing;

    @JSONField(serialize = false)
    public boolean signTingAllOver() {
        return !getSpecialPlay().isSignTing() || signTing == -1;
    }

    @JSONField(serialize = false)
    public void incSignTingFlag(){
        signTing += 1;
    }

    @JSONField(serialize = false)
    public void decSignTingFlag(){
        signTing -= 1;
    }

    @JSONField(serialize = false)
    public void resetTuo() {
        this.nowTime = System.currentTimeMillis();
        this.repeatedSendTuo = false;
    }

    @JSONField(serialize = false)
    public boolean topFen() {
        return topFen > 0;
    }

    @JSONField(serialize = false)
    public int topFenCalc(int score) {
        return topFen() ? Math.abs(score) > topFen ? score > 0 ? topFen : -topFen : score : score;
    }

    public int calcBasicRatio(int basic) {
        return basic * Math.max(basicRatio, 1);
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

    /**
     *@description 每个座位的鸟
     *@param
     *@return
     *@author Guang.OuYang
     *@date 2019/10/18
     */
    public Integer[] birdSeat(int seat) {
        int birdNumber[];
        //4人玩法
        if (gameMaxHuman == 4) {
            birdNumber = new int[]{159, 26, 37, 48};
        } else if (gameMaxHuman == 3) {
            birdNumber = new int[]{147, 258, 369};
        } else {
            birdNumber = new int[]{159, 37};
        }

        int birdAll = birdNumber[seat - 1];
        String birdAllStr = birdAll + "";
        Integer birdCombo[] = new Integer[(birdAllStr).length()];
        for (int i = 0; i < birdCombo.length; i++) {
            birdCombo[i] = Character.getNumericValue(birdAllStr.charAt(i));
        }
        return birdCombo;
    }

    /**
     *@description
     *@param seat 座位号
     *@param bird 当前抓出来的鸟
     *@return 中几个鸟
     *@author Guang.OuYang
     *@date 2019/10/18
     */
    public int birdIsThisSeat(int seat ,int realSeat, Integer[] bird, List<Integer> birdSeat, List<Integer> birdId) {
        Integer[] birdCombo = birdSeat(seat);
        int birdNum = 0;
        for (int i = 0; i < birdCombo.length; i++) {
            for (int j = 0; j < bird.length; j++) {
                Mj birdMj = Mj.getMajang(bird[j]);
                if (birdMj.getPai() == birdCombo[i]) {
                    birdNum += 1;
                    birdSeat.add(realSeat);
                    birdId.add(birdMj.getId());
                }
                LogUtil.printDebug("中鸟检测:seat:{}, mj:{}, bird:{}, num:{}", realSeat, birdMj, birdCombo[i], birdNum);
            }
        }
        return birdNum;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpecialPlay {
        /**补牌开关*/
        private boolean canBuCard;
        /**7小对听牌能否杠*/
        private boolean xiaoDuiGang;
        /**门子叠加*/
        private boolean repeatedEffect;
        /**
         * 是否飘分 1飘分 0不飘分
         */
        private int kePiao;
        /*** GPS预警 */
        private boolean gpsWarn;
        /*** 缺一色 */
        private boolean queYiSe;
        /*11.黑天胡 庄家起手14张牌、闲家起手13张牌+摸第一张牌后，手牌无“王”无将无顺子无刻子。则可胡牌（不能炮胡）*/
        private boolean blackSkyHu;
        /*** 一枝花 */
        private boolean yiZhiHua;
        /*** 六六顺 */
        private boolean liuliuShun;
        /*** 大四喜 */
        private boolean daSiXi;
        /*** 金童玉女 */
        private boolean jinTongYuNv;
        /*** 节节高 */
        private boolean jieJieGao;
        /*** 三同 */
        private boolean sanTong;
        /*** 中途六六顺 */
        private boolean zhongTuLiuLiuShun;
        /*** 中途四喜 */
        private boolean zhongTuSiXi;
        // 特殊状态 1飘分
        private int tableStatus;
        /**
         * 抓鸟
         */
        private int birdNum;
        /**
         * 计算鸟的算法 1：中鸟+1 2：中鸟翻倍 3：中鸟加倍
         */
        private int calcBird;
        /**
         * 三人：1 159中鸟, 2鸟必中,3单数中鸟
         */
        private int birdOption;
        /**
         * （二人选）1：不能吃
         */
        private boolean buChi;
        /**
         * （二人选）只能大胡
         */
        private boolean onlyDaHu;
        /**
         * （二人选）小胡自摸
         */
        private boolean xiaohuZiMo;
        /**
         * （二人选）缺一门
         */
        private boolean queYiMen;
        /**
         * 假将胡: 没有258能胡
         */
        private boolean jiajianghu;
        /**
         * 门清
         */
        private boolean menqing;
        /**
         * 杠摸4张
         */
        private int gangMoNum;
        /**
         * 起手鸟分1:不算鸟分
         */
        private boolean qiShouNiaoFen;
        /**
         * 杠补算分
         */
        private boolean gangBuF;
        /**
         * 全求人吊将
         */
        private boolean quanQiuRenJiang;

        private boolean haiDiLaoYue;
        private boolean haiDiPao;

        /**自摸胡*/
        private boolean selfMoHu;
        /**报听*/
        private boolean signTing;
        /**留底牌张数*/
        private int stayLastCard;
        /*1.碰碰胡 任意一对，加四组刻子。（可碰杠、可炮胡）*/
        private boolean ppHu;
        /*2.将将胡 14张牌全为2、5、8组成，不需要满足胡牌牌型。（可碰杠、可炮胡）*/
        private boolean jjHu;
        /*3.七小对 玩家手牌为七个对子组成。（可炮胡）*/
        private boolean sevenPairs;
        /* 4.清一色 玩家手牌为筒条万其中一种花色组成，需要满足胡牌类型（可吃碰杠、可炮胡）*/
        private boolean allOfTheSameColor;
        /* 5.豪华七小对 玩家在七对的基础上有四张一样的牌，不能用王代替*/
        private boolean superSevenPairs;
        /*6.超豪华七小对 玩家在七对的基础上有两组四张一样的牌，不能用王代替*/
        private boolean specialSuperSevenPairs;
        /*7.超超豪华七小对（不做该门子，如出现此牌型，按超豪华算分）玩家在七对的基础上有三组四张一样的牌，不能用王代替*/
        private boolean specialSSuperSevenPairs;
        /*8.杠上开花 玩家在手牌满足听牌，才可以进行杠操作，杠后补的牌，满足胡牌则算杠上花。*/
        private boolean gangUpFlower;
        /*9.报听 闲家起手听牌，可选择报听，报听摸不能换手牌，不影响手牌可以杠。*/
        private boolean entryListen;
        /*10.抢杠胡 玩家发生弯杠时，其他玩家满足听牌且可以胡这个杠牌。*/
        private boolean robGangHu;
        /**抢暗杠胡*/
        private boolean robAnGangHu;
        /**抢暗杠胡听牌数量必须>=该值*/
        private int robAnGangHuTingCardSizeMust;
        /**天胡抢杠胡*/
        private boolean skyHuRobGangHu;
        /**地胡开关*/
        private int floorHuNum;
        /**王牌天胡开关*/
        private int kingHuNum;
        /*12. 手中有3张“王”自摸胡牌（需要满足胡牌类型），可以和其他牌型叠加如天胡带平2+1，天胡+胡牌番型（炮胡不算天胡*/
//        private boolean skyHu;
        /**倒底胡 庄家开局14张就可以胡牌，闲家没有倒底胡*/
        private boolean openingHu;
        /*13.杠上炮 杠后补的牌，导致其他玩家胡牌，则为杠上炮。*/
        private boolean gangUpGun;
        /**
         * 大进大出,杠上炮,算分算点炮(被抢杠)玩家牌型
         * 大进大出:杠后胡了,属于大胡,别人胡他杠上的牌也属于大胡,他大胡要进多少,他没胡到就要出多少
         * */
        private boolean maxInMaxOut;

        /**一炮多响开关*/
        private boolean oneGunMultiRing;

        /**炮胡底分*/
        private int gunHu;
        /**自摸底分*/
        private int selfMo;

        /**过胡限制*/
        private boolean passHuLimit;

        @JSONField(serialize = false)
        public boolean isGangMoNum() {
            return gangMoNum > 0;
        }

        @JSONField(serialize = false)
        public boolean isFloorHu() {
            return floorHuNum > 0;
        }
        @JSONField(serialize = false)
        public boolean isSkyHu() {
            return kingHuNum > 0;
        }

        /**
         *@description 炮胡3自摸2,炮胡2自摸3
         *@param
         *@return
         *@author Guang.OuYang
         *@date 2019/10/16
         */
        @JSONField(serialize = false)
        public void setPaoHuGangHu(int v) {
            if (v == 1) {
                gunHu = 3;
                selfMo = 2;
            } else {
                gunHu = 2;
                selfMo = 3;
            }
        }

        public boolean selfMoEqGunHu() {
            return selfMo > gunHu ? true : false;
        }

        @JSONField(serialize = false)
        public int getPaoHuGungHuBasicScore(boolean isSelfMo){
            return isSelfMo ? selfMo : gunHu;
        }

        /**报听后不允许的操作*/
        @JSONField(serialize = false)
        public boolean signTingNotToDoAction(int i) {
            return i != MjAction.HU && i != MjAction.ZIMO && i != MjAction.MINGGANG && i != MjAction.ANGANG;
        }

        /**报听后不允许的操作*/
        @JSONField(serialize = false)
        public boolean signTingNotToDoActionDis(int i) {
            return i != MjDisAction.action_hu && i != MjDisAction.action_minggang && i != MjDisAction.action_angang;
        }
    }
}
