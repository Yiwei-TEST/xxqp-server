package com.sy599.game.qipai.nymj.bean;

import java.util.Arrays;
import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;
import com.sy599.game.qipai.nymj.constant.MjAction;
import com.sy599.game.qipai.nymj.rule.Mj;
import com.sy599.game.util.LogUtil;
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
    /**
     * 游戏局数 10/16/24
     */
    private int gameFinishRound;
    /**
     * 游戏最大人数 2/3/4
     */
    private int gameMaxHuman;
    /**
     * 支持方式 1AA 2房主
     */
    private int payType;
    /**
     * 抽取底牌数量 0/10/20
     */
    private int discardHoleCards;
    /**
     * 支持的特殊玩法
     */
    private SpecialPlay specialPlay = new SpecialPlay();
    /**
     * 0随机 1先进房坐庄
     */
    private int changeBankerWay;
    /**
     * 加倍0不加倍,1加倍
     */
    private int doubleChip;
    /**
     * 加倍生效分, 1小于10分时加倍
     */
    private int doubleChipLeChip;
    /**
     * 加倍倍数1翻两倍 3翻三倍 4翻四倍
     */
    private int doubleRatio;
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
     * 分数限制 0没限制
     */
    private int topFen;

    /***点炮, true鸡胡可吃胡, false鸡胡不能吃胡
     //鸡胡可吃胡：鸡胡可接炮
     //鸡胡不能吃胡：鸡胡不能接炮*/
    private boolean ignite;

    /**
     * 多少分才能接炮
     */
    private int igniteScoreArrive;

    /**
     * 无字
     */
    private boolean noneChar;
    /**
     * 无风
     */
    private boolean noneWind;
    /**
     * 有胡必胡,不能过
     */
    private boolean mustHu;
    /**
     * 十倍不计分
     */
    private int nMulNotCalcScore;
    /**
     * 跟庄+n
     */
    private int followMaster;
    /**
     * 流局算杠分
     */
    private boolean flowRoundCalcGangScore;
    /**
     * 吃杠杠爆全包
     * 吃杠杠爆全包：吃杠补张杠爆后，所有进分由放杠者全包
     */
    private boolean eatGangBurstAllInCharge;
    /**
     * 连庄:玩家如果连续胡牌（点炮、自摸）两局或两局以上，视为连庄；即玩家在当前局胡牌后，下一局再次胡牌，则为连庄，需要在该局胡牌分上多加连庄分数。
     */
    private boolean reappointment;
    /**
     * 创建癞子牌
     * //八、翻鬼
     * //1.每局发完牌后从牌堆顺摸一张牌亮出，该张牌+1为鬼牌。
     * //2.鬼牌不能变杠、变碰。
     * //3.任何情况下摸到四鬼默认鸡胡自摸。
     * //4.鬼牌可以打出，无鬼牌胡牌翻倍。
     * //5.鬼牌没有天地胡。
     */
    private boolean createKingCard;

    /**
     * 马跟杠：中马手中牌型分和杠分各加一倍。非胡牌玩家之间也需要马跟杠，按庄家胡牌的马数来给他们私下算分。
     * 勾选后, 额外增加马数*杠数的分数
     */
    private boolean birdAndGang;

    /**
     * 需要听牌才能杠
     */
    private boolean needTingGang;
    /**
     * 杠后自动出牌
     */
    private boolean gangAfterAutoPlay;

    /**
     * 需要258做将
     */
    public boolean need258;
    /**
     * 基础分, 这是个倍数
     */
    public int basicScore;
    /**
     * 飘分,己方飘1对方飘1,赢家+2
     * 飘分类型,8抬飘, 9追飘,定飘1,2,3 飘分11,12,13
     * 全部选择检测状态 -1全部選擇完畢
     */
    private int flutterScore;
    /**
     * 选项
     * 飘分类型,8抬飘, 9追飘,定飘1,2,3 飘分11,12,13
     */
    public int flutterScoreType;

    /**金马翻倍,所有马都中, *2*/
    public boolean allBirdDouble;

    /** 可出王 */
	public boolean canOutKing;

    @JSONField(serialize = false)
    public boolean signFlutterAllOver() {
        return !isFlutterScore() || flutterScore == -1;
    }

    @JSONField(serialize = false)
    public void incFlutterScoreFlag() {
        flutterScore += 1;
    }

    @JSONField(serialize = false)
    public void decSignTingFlag() {
        flutterScore -= 1;
    }

    @JSONField(serialize = false)
    public boolean topFen() {
        return topFen > 0;
    }

//    @JSONField(serialize = false)
//    public boolean canIgnite(int nowScore) {
//        return nowScore >= igniteScoreArrive;
//    }

    @JSONField(serialize = false)
    public int topFenCalc(int score) {
        return score;//topFen() ? Math.abs(score) > topFen ? score > 0 ? topFen : -topFen : score : score;
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

    @JSONField(serialize = false)
    public boolean isFollowMaster() {
        return followMaster > 0;
    }

    /**
     * @param
     * @return
     * @description 每个座位的鸟
     * @author Guang.OuYang
     * @date 2019/10/18
     */
    public Integer[] birdSeat(int seat) {
//        if (getGameMaxHuman() == 4) {
//            //庄家：1、5、9、东、中
//            //下家：2、6、南、发
//            //对家：3、7、西、白
//            //上家：4、8、北
//            return Arrays.asList(new Integer[]{1, 5, 9, 301, 201}, new Integer[]{2, 6, 311, 211}, new Integer[]{3, 7, 321, 221}, new Integer[]{4, 8, 331}).get(seat - 1);
//        } else if (getGameMaxHuman() == 3) {
//            //庄家：1、4、7、东、北、白；
//            //下家：2、5、8、南、中；
//            //上家：3、6、9、西、发
//            return Arrays.asList(new Integer[]{1, 4, 7, 301, 331, 221}, new Integer[]{2, 5, 8, 311, 201}, new Integer[]{3, 6, 9, 321, 211}).get(seat - 1);
//        } else {
//            //1单2双,字从东开始
//            //201中(20) 211发财(21) 221白板(22)
//            //301东风(30) 311南风(31)  321西风(32) 331北风 1000万能牌(100)
//            return Arrays.asList(new Integer[]{1, 3, 5, 7, 9, 301, 321, 201, 221}, new Integer[]{2, 4, 6, 8, 311, 331, 211}).get(seat - 1);
//        }
		return new Integer[]{1,5,9};
    }

    /**
     * @param seat 座位号
     * @param bird 当前抓出来的鸟
     * @return 中几个鸟
     * @description
     * @author Guang.OuYang
     * @date 2019/10/18
     */
    public int birdIsThisSeat(int seat, int realSeat, int winnerRealSeat, Integer[] bird, List<Integer> birdSeat, List<Integer> birdId, MjiangHu.BigMap<Integer, Integer, Integer> seatBirdBigMap) {
        int birdNum = 0;

		if (this.getSpecialPlay().isOneBirdScore() && bird.length > 0) {
			Mj birdMj = Mj.getMajang(bird[0]);
			//一鸟全中, 1是10分, 红中特殊处理为1
			birdNum += this.getSpecialPlay().isOneBirdScore() ? (/*birdMj.getVal() == 201 ? 1 : */birdMj.getPai() == 1 ? 10 : birdMj.getPai()) : 1;
			birdSeat.add(realSeat);
			birdId.add(birdMj.getId());
			//这里一定存在鸟,不存在就抛异常
			seatBirdBigMap.get(birdMj.getId()).setE(winnerRealSeat);
			LogUtil.printDebug("中鸟啦~ {}->{}(id:{},v:{}) comboV:{}", seat, birdMj, birdMj.getId(), birdMj.getVal(), birdMj);
			return birdNum;
		}

        Integer[] birdCombo = birdSeat(seat);
        for (int i = 0; i < birdCombo.length; i++) {
            for (int j = 0; j < bird.length; j++) {
                Mj birdMj = Mj.getMajang(bird[j]);
				if (((birdCombo[i] < 201 && birdMj.getVal() < 201 && birdMj.getPai() == birdCombo[i]) || birdMj.getVal() == birdCombo[i])) {
					//一鸟全中, 1是10分
					birdNum += 1;
					birdSeat.add(realSeat);
					birdId.add(birdMj.getId());
					//这里一定存在鸟,不存在就抛异常
					seatBirdBigMap.get(birdMj.getId()).setE(winnerRealSeat);
					LogUtil.printDebug("中鸟啦~ {}->{}(id:{},v:{}) comboV:{}", seat, birdMj, birdMj.getId(), birdMj.getVal(), birdCombo[i]);
				}
            }
        }
        return birdNum;
    }


    @JSONField(serialize = false)
    public boolean isFlutterScore() {
        return flutterScoreType > 0;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpecialPlay {
        /**
         * 补牌开关
         */
        private boolean canBuCard;
        /**
         * 7小对听牌能否杠
         */
        private boolean xiaoDuiGang;
        /**
         * 门子叠加
         */
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
        /**
         * 3豪华7对
         */
        private boolean sanHaoHua7Dui;
        /**
         * 混一色
         */
        private boolean hunYiSe;
        /**
         * 字一色
         */
        private boolean ziYiSe;
        /**
         * 十八罗汉
         */
        private boolean shiBaLuoHan;
        /**
         * 一九胡
         */
        private boolean yiJiuHu;
        /**
         * 清一九
         */
        private boolean qingYiJiu;
        /**
         * 十三幺
         */
        private boolean shiSanYao;

        // 特殊状态 1飘分
        private int tableStatus;
        /**
         * 抓鸟
         * 三、奖马
         * 1.胡牌的人可以从剩余牌池里按顺序拿马（牌），如果抽中了对应的马，胡牌分数加倍（不包含杠牌的分数），中一个马加一倍，两个马加两倍
         * 2.一炮多响的情况，由放炮的人拿马，如果中对应的马，要赔更多，点炮所输分数加倍（不包含杠牌的分数），中一个马加一倍，两个马加两倍
         */
        private int birdNum;
        /**
         * 1.买马：开局每名玩家从牌库拿取1或2张马牌，游戏结束时翻开结算，中马规则：
         * 庄家：1、5、9、东、中
         * 下家：2、6、南、发
         * 对家：3、7、西、白
         * 上家：4、8、北
         * 一炮多响时从点炮玩家计算
         * 买马:开局每个人都有n个买的马,结算时根据赢家方位马中则不算马,没中则需要出赢家底分加上单个马的分数
         */
        private int buyBirdNum;
        /**
         * 计算鸟的算法 1：中鸟+1 2：中鸟翻倍 3：中鸟加倍
         */
        private int calcBird;
        /**
         * 三人：1 159中鸟, 2鸟必中,3单数中鸟
         */
        private int birdOption;
        /**一马全中, 中到几就是几分*/
        private boolean oneBirdScore;
        /**杠翻倍:杠分在胡到大胡或者硬胡分数翻倍*/
        private boolean gangDouble;
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
        /**
         * 海底捞月
         */
        private boolean haiDiLaoYue;
        /**
         * 海底炮
         */
        private boolean haiDiPao;

        /**
         * 自摸胡
         */
        private boolean selfMoHu;
        /**
         * 飘分
         */
//        private int flutterScore;
        /**
         * 留底牌张数
         */
        private int stayLastCard;
        /**
         * 最后一圈
         */
        private int finalOneCircleCard;
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
        //17. 杠爆：开杠后补张自摸。杠爆结算时牌型分翻倍。
        private boolean gangUpFlower;
        /*9.报听 闲家起手听牌，可选择报听，报听摸不能换手牌，不影响手牌可以杠。*/
        private boolean entryListen;
        /*10.抢杠胡 玩家发生弯杠时，其他玩家满足听牌且可以胡这个杠牌。*/
        private boolean robGangHu;
        /**
         * 天胡抢杠胡
         */
        private boolean skyHuRobGangHu;
        /**
         * 地胡开关
         */
        private int floorHuNum;
        /**
         * 王牌天胡开关
         */
        private int kingHuNum;
        /*12. 手中有3张“王”自摸胡牌（需要满足胡牌类型），可以和其他牌型叠加如天胡带平2+1，天胡+胡牌番型（炮胡不算天胡*/
        private boolean skyHu;
        /**
         * 倒底胡 庄家开局14张就可以胡牌，闲家没有倒底胡
         */
        private boolean openingHu;
        /*13.杠上炮 杠后补的牌，导致其他玩家胡牌，则为杠上炮。*/
        private boolean gangUpGun;
        /**
         * 大进大出,杠上炮,算分算点炮(被抢杠)玩家牌型
         * 大进大出:杠后胡了,属于大胡,别人胡他杠上的牌也属于大胡,他大胡要进多少,他没胡到就要出多少
         */
        private boolean maxInMaxOut;

        /**
         * 一炮多响开关
         */
        private boolean oneGunMultiRing;

        /**
         * 炮胡底分
         */
        private int gunHu;
        /**
         * 自摸底分
         */
        private int selfMo;

        /**
         * 过胡限制
         */
        private boolean passHuLimit;

        /**过杠*/
        private boolean passGang;

        @JSONField(serialize = false)
        public boolean isGangMoNum() {
            return gangMoNum > 0;
        }

        @JSONField(serialize = false)
        public boolean isFloorHu() {
            return floorHuNum > 0;
        }

        /**
         * @param
         * @return
         * @description 炮胡3自摸2, 炮胡2自摸3
         * @author Guang.OuYang
         * @date 2019/10/16
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

        @JSONField(serialize = false)
        public int getPaoHuGungHuBasicScore(boolean isSelfMo) {
            return isSelfMo ? selfMo : gunHu;
        }

        /**
         * 报听后不允许的操作
         */
        @JSONField(serialize = false)
        public boolean signTingNotToDoAction(int i) {
            return i != MjAction.HU && i != MjAction.ZIMO && i != MjAction.MINGGANG && i != MjAction.ANGANG;
        }

        /**
         * 报听后不允许的操作
         */
        @JSONField(serialize = false)
        public boolean signTingNotToDoActionDis(int i) {
            return i != MjDisAction.action_hu && i != MjDisAction.action_minggang && i != MjDisAction.action_angang;
        }

        @JSONField(serialize = false)
        public int getFinalOneCircleCardNum() {
            return finalOneCircleCard > 0 ? finalOneCircleCard - stayLastCard : stayLastCard;
        }
    }
}
