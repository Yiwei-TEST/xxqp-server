package com.sy599.game.qipai.xtpaohuzi.rule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Stream;

import com.sy599.game.qipai.xtpaohuzi.bean.CardTypeHuxi;
import com.sy599.game.qipai.xtpaohuzi.bean.GameModel;
import com.sy599.game.qipai.xtpaohuzi.bean.PaohuziHandCard;
import com.sy599.game.qipai.xtpaohuzi.bean.PaohzDisAction;
import com.sy599.game.qipai.xtpaohuzi.bean.XtPaohuziPlayer;
import com.sy599.game.qipai.xtpaohuzi.bean.XtPaohuziTable;
import com.sy599.game.qipai.xtpaohuzi.constant.HuType;
import com.sy599.game.qipai.xtpaohuzi.constant.PaohzCard;
import com.sy599.game.qipai.xtpaohuzi.tool.PaohuziTool;
import com.sy599.game.util.LogUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.lang.NullArgumentException;

public class PaohuziMingTangRule {

    public static final int LOUDI_MINGTANG_TIANHU = 1;//4.2 天胡: 庄家起手发牌结束后直接胡牌;计分翻倍。
    public static final int LOUDI_MINGTANG_DIHU = 15; //4.3 地胡：手牌未变动，胡牌算地胡;计分翻倍。
    public static final int LOUDI_MINGTANG_ZIMO = 3;//4.1 自摸翻倍
    public static final int LOUDI_MINGTANG_YIDIANZHU = 4;//4.7 一点红: 胡牌时红牌张数=1;计分翻倍。
    public static final int LOUDI_MINGTANG_DAHONGHU = 5;//红胡: 胡牌牌型红字张数≥10或13张（选项）;计分翻倍。
    public static final int LOUDI_MINGTANG_WUHU = 7;//4.5 黑胡：胡牌牌型红字牌张数=0;计分翻倍。
    public static final int LOUDI_MINGTANG_DAZIHU = 8;//4.8 大字胡：胡牌时大字张数≥18张;计分翻倍。
    public static final int LOUDI_MINGTANG_XIAOZIHU = 9;//4.9 小字胡：胡牌时小字张数≥18张;计分翻倍。
    public static final int LOUDI_MINGTANG_PENGPENGHU = 10;//4.6 碰碰胡: 胡牌时7门子全部都是跑、提、偎、坎、碰、将，玩家手上和桌子上不能有一句话，桌子上不能有吃的牌;计分翻倍。
    public static final int LOUDI_MINGTANG_SANSHIHUXI_FANBEI = 14; //4.9 30胡息：胡牌时胡息≥30，胡息翻倍(先翻倍再加自摸放炮10分)。

    public static final int LOUDI_MINGTANG_HONGWU = 6;//十三红4番
    public static final int LOUDI_MINGTANG_ZIMO_JIAHU = 2;// 自摸加3胡
    public static final int LOUDI_MINGTANG_DIAOPAO = 11; // 放炮：所胡那张牌是别人打出来的，胡息加10。
    public static final int LOUDI_MINGTANG_SANSHIHUXI = 12; //30胡息（十红）：30胡息以上，且红字数量>=10,算100胡。
    public static final int LOUDI_MINGTANG_JIEPAO = 13; //4.3 放炮：所胡那张牌是别人打出来的，胡息加10。


    /**
     * @description
     * @param
     * @return
     * @author Guang.OuYang
     * @date 2019/9/4
     */
    public static final BigMap<Integer, String, int[]> SCORE_CALC = new BigMap<Integer, String, int[]>(15) {
		{
			put(LOUDI_MINGTANG_ZIMO, "*", new int[] { 1, 2 });        //加分直接都计入胡息
			put(LOUDI_MINGTANG_DAHONGHU, "*", new int[] { 1, 0 });
			put(LOUDI_MINGTANG_TIANHU, "*", new int[] { 1, 2 });
			put(LOUDI_MINGTANG_DIHU, "*", new int[] { 1, 2 });
			put(LOUDI_MINGTANG_WUHU, "*", new int[] { 1, 5 });
			put(LOUDI_MINGTANG_YIDIANZHU, "*", new int[] { 1, 3 });
			put(LOUDI_MINGTANG_DAZIHU, "*", new int[] { 1, 2 });
			put(LOUDI_MINGTANG_XIAOZIHU, "*", new int[] { 1, 2 });
			put(LOUDI_MINGTANG_PENGPENGHU, "*", new int[] { 1, 2 });
			put(LOUDI_MINGTANG_SANSHIHUXI_FANBEI, "*", new int[] { 1, 2 });

			put(LOUDI_MINGTANG_ZIMO_JIAHU, "+", new int[] { 0, 3 });

//			put(LOUDI_MINGTANG_DIAOPAO, "+", 0);
//			put(LOUDI_MINGTANG_SANSHIHUXI, "+", 0);
//			put(LOUDI_MINGTANG_JIEPAO, "+", 0);
//			put(LOUDI_MINGTANG_ZIMO_JIAHU, "+", 0);
		}
    };

    /**
     * @param
     * @author Guang.OuYang
     * @description
     * @return
     * @date 2019/9/4
     */
    @AllArgsConstructor
    @Getter
    public static class BigMap<K, E, V> {
        private BigMap[] bigMap;
        private E e;
        private K k;
        private V v;

        BigMap() {
        }

        BigMap(int size) {
            bigMap = new BigMap[size];
        }

        BigMap putAll(K k, E e, V v) {
            this.e = e;
            this.k = k;
            this.v = v;
            return this;
        }

        void put(K k, E e, V v) {
            if (bigMap == null) {
                throw new NullArgumentException("BigMap don't init..");
            }
            int hash = k.hashCode() % bigMap.length;
            int reHash = hash == 0 ? (k.hashCode() + hash - 1) : hash - 1;
            bigMap[reHash] = new BigMap<K, E, V>().putAll(k, e, v);
        }

        public BigMap<K, E, V> get(K k) {
            int hash = k.hashCode() % bigMap.length;
            int reHash = hash == 0 ? (k.hashCode() + hash - 1) : hash - 1;
            return hash < bigMap.length ? bigMap[reHash] : null;
        }
    }

    /**
     * 名堂
     *
     * @param player 胡牌玩家
     * @return
     */
	public static Map<Integer, Integer> calcMingTang(XtPaohuziPlayer player) {

        /*  4.1 自摸：自摸加3胡息（该3胡息不进行翻倍）。
            4.2 天胡: 庄家起手发牌结束后直接胡牌;计分翻倍。
            4.3 地胡：手牌未变动，胡牌算地胡;计分翻倍。
            4.4 红胡: 胡牌牌型红字张数≥10或13张（选项）;计分翻倍。
            4.5 黑胡：胡牌牌型红字牌张数=0;计分翻倍。
            4.6 碰碰胡: 胡牌时7门子全部都是跑、提、偎、坎、碰、将，玩家手上和桌子上不能有一句话，桌子上不能有吃的牌;计分翻倍。
            4.7 一点红: 胡牌时红牌张数=1;计分翻倍。
            4.8 大字胡：胡牌时大字张数≥18张;计分翻倍。
            4.9 小字胡：胡牌时小字张数≥18张;计分翻倍。
            4.10 计分：同时出现多名堂时,累乘算分。
            */

		Map<Integer, Integer> mtList = new HashMap<>();
        List<PaohzCard> allCards = new ArrayList<>();
        for (CardTypeHuxi type : player.getCardTypes()) {
            allCards.addAll(PaohuziTool.toPhzCards(type.getCardIds()));
        }

        if (player.getHu() != null && player.getHu().getPhzHuCards() != null) {
            for (CardTypeHuxi type : player.getHu().getPhzHuCards()) {
                allCards.addAll(PaohuziTool.toPhzCards(type.getCardIds()));
            }
        }

        XtPaohuziTable table = player.getPlayingTable(XtPaohuziTable.class);

        //红牌数量
        int redCardCount = Optional.ofNullable(PaohuziTool.findRedPhzs(allCards)).orElse(Collections.emptyList()).size();
        //小牌数量
        int smallCardsSize = Optional.ofNullable(PaohuziTool.findSmallPhzs(allCards)).orElse(Collections.emptyList()).size();
        //4.9 30胡息：胡牌时胡息≥30，胡息翻倍(先翻倍再加自摸放炮10分)。
        if (table.getGameModel().getSpecialPlay().isThirtyHuXiDoubleScore() && player.getTotalHu() >= table.getGameModel().getSpecialPlay().getThirtyHuXiDoubleScoreHu()) {
            mtList.put(LOUDI_MINGTANG_SANSHIHUXI_FANBEI, 0);
            LogUtil.printDebug("30胡息：胡牌时胡息≥" + table.getGameModel().getSpecialPlay().getThirtyHuXiDoubleScoreHu() + "，胡息翻倍(先翻倍再加自摸放炮10分)。");
        }

        HuType huType = ((XtPaohuziTable) player.getPlayingTable()).calcHuType();
        //天胡
        if (table.getGameModel().getSpecialPlay().isSkyFloorHu() && table.isFirstCard() && table.getLastWinSeat() == player.getSeat()) {
            mtList.put(LOUDI_MINGTANG_TIANHU, 0);
            LogUtil.printDebug("4.2 天胡: 庄家起手发牌结束后直接胡牌;计分翻倍。");
        } /*else if ( table.getGameModel().getSpecialPlay().isSkyFloorHu() && table.getLastWinSeat() != player.getSeat() && table.isFirstCard()) {
            mtList.add(LOUDI_MINGTANG_DIHU);
            LogUtil.printDebug("4.7 地胡: 胡庄家打出的第一张牌，胡牌算110胡。");
        } */else if (table.getGameModel().getSpecialPlay().isSkyFloorHu() && table.getLastWinSeat() != player.getSeat() && player.isFirstDisSingleCard()) {
            mtList.put(LOUDI_MINGTANG_DIHU, 0);
            LogUtil.printDebug("4.7 地胡: 手牌未变动，胡牌胡息翻倍；");
        } else

        //自摸,庄家天胡也算自摸,(天胡不算自摸20200411)
        if (table.getGameModel().getSpecialPlay().isSinceTouchDoubleScore() && huType == HuType.ZI_MO) {
            mtList.put(LOUDI_MINGTANG_ZIMO, 0);
            LogUtil.printDebug("4.1 自摸自摸翻倍。");
        }

        //自摸,庄家天胡也算自摸,(天胡不算自摸20200411)
        if (table.getGameModel().getSpecialPlay().isSinceTouchAdd3Score() && huType == HuType.ZI_MO) {
            mtList.put(LOUDI_MINGTANG_ZIMO_JIAHU, 0);
            LogUtil.printDebug("4.1 自摸：自摸加3胡息（该3胡息不进行翻倍）。");
        }

        //碰碰胡: 胡牌时7门子全部都是跑、提、偎、坎、碰、将，玩家手上和桌子上不能有一句话，桌子上不能有吃的牌;计分翻倍。
        if (table.getGameModel().getSpecialPlay().isPpHu() && isPPHu(player)) {
            mtList.put(LOUDI_MINGTANG_PENGPENGHU, 0);
            LogUtil.printDebug("胡牌时7门子全部都是跑、提、偎、坎、碰、将，玩家手上和桌子上不能有一句话，桌子上不能有吃的牌;计分翻倍。");

        }

        //互斥名堂
        if (table.getGameModel().getSpecialPlay().isRedBlackHu() && redCardCount >= table.getGameModel().getSpecialPlay().getRedHuCount()) {
            //红胡
			//株洲计分, 13红 4番 10红 2番, 默认计分:根据规则
			mtList.put(LOUDI_MINGTANG_DAHONGHU, table.getGameModel().getSpecialPlay().isZzCaclScore() ? redCardCount >= 13 ? 4 : 2 : 0);
            LogUtil.printDebug("4.4 红胡: 胡牌牌型红字张数≥" + table.getGameModel().getSpecialPlay().getRedHuCount() + ";计分翻倍。");
        } else if (table.getGameModel().getSpecialPlay().isASmallRed() && redCardCount == 1) {
            //一点红
            mtList.put(LOUDI_MINGTANG_YIDIANZHU, 0);
            LogUtil.printDebug("4.7 一点红: 胡牌时红牌张数=1;计分翻倍。");
        } else if (table.getGameModel().getSpecialPlay().isRedBlackHu() && redCardCount == 0) {
            //黑胡
            mtList.put(LOUDI_MINGTANG_WUHU, 0);
            LogUtil.printDebug("4.5 黑胡：胡牌牌型红字牌张数=0;计分翻倍。");
        }

        //互斥名堂, 大小字
        if (table.getGameModel().getSpecialPlay().isMaxMinChar())
            if (smallCardsSize >= 18) {
                //小字胡
                mtList.put(LOUDI_MINGTANG_XIAOZIHU, 0);
                LogUtil.printDebug("4.9 小字胡：胡牌时小字张数≥18张;计分翻倍。");
            } else if (allCards.size() - smallCardsSize >= 18) {
                //大字胡
                mtList.put(LOUDI_MINGTANG_DAZIHU, 0);
                LogUtil.printDebug("胡牌时大字张数≥18张;计分翻倍。");
            }

        if (table.getGameModel().getSpecialPlay().isIgnite() && table.getHuType() == HuType.DIAN_PAO) {
            mtList.put(LOUDI_MINGTANG_JIEPAO, 0);
            LogUtil.printDebug("放炮：所胡那张牌是别人打出来的，胡息加10。");
        }

//        List<Integer> mtListRes = new ArrayList<>();

//        boolean removeRedHu = false;
//        if (mtList.contains(LOUDI_MINGTANG_DAHONGHU)) {
//            Iterator<Integer> iterator = mtList.iterator();
//            while (iterator.hasNext()) {
//                Integer mt = iterator.next();
//                boolean isAdd = false;
//                //地胡+红胡 合并名堂
//                if (mt == LOUDI_MINGTANG_DIHU) {
//                    mtListRes.add(LOUDI_MINGTANG_ZIMO_JIAHU);
//                    isAdd = removeRedHu = true;
//                } else if (mt == LOUDI_MINGTANG_SANSHIHUXI_FANBEI) { //三十胡息+红胡 合并名堂
//                    mtListRes.add(LOUDI_MINGTANG_SANSHIHUXI);
//                    isAdd = removeRedHu = true;
//                }
//
//                if (!isAdd)
//                    mtListRes.add(mt);
//            }
//        }

//        if (removeRedHu) {
//            mtListRes.removeIf(v -> v == LOUDI_MINGTANG_DAHONGHU);
//        }

//        return CollectionUtils.isEmpty(mtListRes) ? mtList : mtListRes;

		return mtList;
    }

    /**
     * @param
     * @return
     * @description 碰碰胡: 胡牌时7门子全部都是跑、提、偎、坎、碰、将，玩家手上和桌子上不能有一句话，桌子上不能有吃的牌;计分翻倍
     * @author Guang.OuYang
     * @date 2019/9/5
     */
    private static boolean isPPHu(XtPaohuziPlayer player) {
		long t = System.currentTimeMillis();
		//不能有吃
		boolean ppHu = !player.getCardTypes().stream().anyMatch(v -> v.getAction() == PaohzDisAction.action_chi);

		//检测手牌中是否有顺子
		if (ppHu && player.getHandPais().size() >= 3) {
			List<PaohzCard> handCards = new ArrayList<>();
			handCards.addAll(player.getHandPhzs());
			handCards.addAll(player.getTi());
			handCards.addAll(player.getZai());
			handCards.addAll(player.getChi());
			handCards.addAll(player.getPeng());
			handCards.addAll(player.getPao());

			//除非天胡否则把最后摸出来的牌算入
			if (!player.isFirstDisSingleCard() && !handCards.contains(player.getPlayingTable(XtPaohuziTable.class).getNowDisCardIds().get(0))) {
				//胡牌时把最后张底牌算进去,这张牌的组合不确定所以把玩家所有牌都算入
				handCards.add(player.getPlayingTable(XtPaohuziTable.class).getNowDisCardIds().get(0));
			}

			PaohuziHandCard paohuziHandCardBean = PaohuziTool.getPaohuziHandCardBean(handCards);

			PaohuziIndex paohzCardIndex = paohuziHandCardBean.getGroupByNumberToArray().getPaohzCardIndex(0);
			PaohuziIndex paohzCardIndex2 = paohuziHandCardBean.getGroupByNumberToArray().getPaohzCardIndex(1);
			ppHu = (paohzCardIndex == null || paohzCardIndex.getLength() <= 0) && (paohzCardIndex2 == null || paohzCardIndex2.getPaohzValMap().size() == 1);
		}

		LogUtil.printDebug("碰碰胡匹配:{}ms", (System.currentTimeMillis() - t));
		return ppHu;
	}



    private static Integer[][] defaultArrays;

    static {
        defaultArrays = comboAllSerial(null);
    }

    /**
     *@description 所有顺子组合 123,234,345,...
     *@param
     *@return
     *@author Guang.OuYang
     *@date 2019/9/26
     */
    public static Integer[][] comboAllSerial(GameModel gameModel) {
        if (defaultArrays != null) {
            Integer[][] clone = defaultArrays.clone();
            if (!(gameModel != null && gameModel.getSpecialPlay().isOneFiveTen())) {
                clone[clone.length - 1] = null;
                clone[clone.length - 2] = null;
            }

            return clone;
        }


        int index = -1;
        //找出所有能组成顺子的组合123,234,345...
        //最大组合(8+2)*2
        int maxSerial = 10;
        //最大顺子组合8,最大吃组合为10,额外组合2710+1510+大于8的吃牌如9,109,109但是不能组成9,10,11
        Integer[][] arrays = new Integer[((maxSerial - 2) * 4) + 2 * 4][3];
        for (int i = 1; i <= maxSerial; i++) {
            if (i <= 8) {
                arrays[++index] = new Integer[]{i, i + 1, i + 2};
                arrays[++index] = new Integer[]{i + 100, i + 1 + 100, i + 2 + 100};
            }
            arrays[++index] = new Integer[]{i, i + 100, i + 100};
            arrays[++index] = new Integer[]{i, i, i + 100};
        }

        //特殊组合2710
        arrays[++index] = new Integer[]{2, 7, 10};
        arrays[++index] = new Integer[]{102, 107, 110};
        //特殊组合1510
        arrays[++index] = new Integer[]{1, 5, 10};
        arrays[++index] = new Integer[]{101, 105, 110};
        return arrays;
    }

    /**
     *@description 找到牌组中所有大于3张的牌组
     *@param
     *@return
     *@author Guang.OuYang
     *@date 2019/9/26
     */
//    private static List<Integer> findCountGeTwo(List<PaohzCard> cards) {
//        cards.sort((v1, v2) -> Integer.valueOf(v1.getPai()).compareTo(Integer.valueOf(v2.getPai())));
//
//        //最低匹配数量
//        int minMathCount = 2;
//        int prePai=0;   //上一个牌
//        int repeated=0; //当前计算的数量
//        ArrayList<Integer> paohzCards = new ArrayList<>();
//
//        Iterator<PaohzCard> iterator = cards.iterator();
//        while (iterator.hasNext()) {
//            PaohzCard next = iterator.next();
//            if (prePai > 0 && prePai > 0 && prePai != next.getVal()) {
//                repeated = 0;
//                prePai = 0;
//            }
//            if (prePai == 0 || prePai == next.getVal()) {
//                prePai = next.getVal();
//                repeated += 1;
//            }
//            if (repeated == minMathCount) {
//                paohzCards.add(next.getVal());
//            }
//        }
//        return paohzCards;
//    }

	/**
	 *@description 找到牌组中所有大于3张的牌组
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2019/9/26
	 */
	public static Map<Integer, Integer> findCountGeTwo(List<PaohzCard> cards) {
		Map<Integer, Integer> res = new HashMap<Integer, Integer>();
		Iterator<PaohzCard> iterator = cards.iterator();
		while (iterator.hasNext()) {
			PaohzCard next = iterator.next();
			res.merge(next.getVal(), 1, (oV, nV) -> nV + oV);
		}
		return res;
	}

    /**
     * @param
     * @return
     * @description 计算名堂分, 番数, 分数=囤数*番数(名堂)。 囤数=胡息/3 , 这里直接计算胡息
     * @author Guang.OuYang
     * @date 2019/9/2
     */
    public static int calcMingTangFen(int totalTun, XtPaohuziTable table, Map<Integer, Integer> mt) {
        //总囤
        int totalTunNumber = totalTun;

        int totalRatio = 1;

        //额外增加的胡息
        int addHuXi = 0;

        //株洲计分
        boolean zzCalcScore = table.getGameModel().getSpecialPlay().isZzCaclScore();

        //不叠加翻倍的番数仅生效一次, 叠加可重复算
        int repeatedIndex = (table).getGameModel().getSpecialPlay().getRepeatedEffect() > 0 ? (table).getGameModel().getSpecialPlay().getRepeatedEffect() : 1;

		Iterator<Entry<Integer, Integer>> iterator = mt.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<Integer, Integer> next = iterator.next();
			BigMap<Integer, String, int[]> currentCalc = SCORE_CALC.get(next.getKey());
            if (currentCalc != null) {
				//名堂分不叠加
				if (!zzCalcScore && currentCalc.getE().equals("*") && --repeatedIndex >= 0) {           //*代表乘积
					totalRatio += currentCalc.getV()[0];
					LogUtil.printDebug("名堂分翻倍 : {}, {} ,{}", currentCalc.getV(), currentCalc.getK(), totalRatio);
				} else if (!zzCalcScore && currentCalc.getE().equals("+")) {    //+代表直接增加胡息
					addHuXi += currentCalc.getV()[0];
					LogUtil.printDebug("名堂分增加 :{},  {}, {}", currentCalc.getV(), currentCalc.getK(), addHuXi);
				} else if (zzCalcScore && currentCalc.getE().equals("*") && --repeatedIndex >= 0) {    //株洲计分
					totalRatio *= (currentCalc.getV()[1] + next.getValue());
					LogUtil.printDebug("名堂分翻倍 : {}, {} ,{}", currentCalc.getV(), currentCalc.getK(), totalRatio);
				}else if (zzCalcScore && currentCalc.getE().equals("+")) {    //+代表直接增加胡息
					addHuXi += currentCalc.getV()[1];
					LogUtil.printDebug("名堂分增加 :{},  {}, {}", currentCalc.getV(), currentCalc.getK(), addHuXi);
				}
			}
        }

        int res = totalTunNumber + (addHuXi > 0 ? addHuXi / table.getGameModel().getConverHuXiToTunRatio() : addHuXi);

		totalTunNumber = res * totalRatio;

		LogUtil.printDebug("翻倍分:{},{}->{}", totalTun, res, totalTunNumber);

        return totalTunNumber;
    }


    /**
     * @param
     * @author Guang.OuYang
     * @description 特定数字组合寻找[时间复杂度O(n - m)], 数据源200, 循环10w次, 结果:全部false,平均1ms/100次,结果:全部true,50ms/10w
     * @return
     * @date 2019/9/19
     */
    public static class FindAnyCombo {
        private List<FindAny> dataSrc = new ArrayList<>();

        /**
         * @param
         * @author Guang.OuYang
         * @description 搜索容器, 给出指定数字
         * @return
         * @date 2019/9/19
         */
        @Data
        public class FindAny {
            //结果匹配值,不重复, 匹配值->结果
            private HashMap<Integer, Boolean> src = new HashMap<>();
            //匹配值,不重复,  匹配值->出现次数
            private HashMap<Integer, Integer> srcCount = new HashMap<>();
            //初始匹配值,不重复,  匹配值->出现次数
            private HashMap<Integer, Integer> initSrcCount = new HashMap<>();
            //匹配值的重复值
            private List<Integer> repeatedSrcKey = new ArrayList<>();

            public FindAny(Integer... val) {
                for (int i = 0; i < val.length; i++) {
                    src.put(val[i], false);
                    if (srcCount.get(val[i]) == null) {
                        srcCount.put(val[i], 1);
                    } else {
                        srcCount.put(val[i], srcCount.get(val[i]) + 1);
                    }

                    initSrcCount.putAll(srcCount);
                    repeatedSrcKey.add(val[i]);
                }

            }

            public boolean in(Integer number) {
                if (src.containsKey(number)) {
                    int v = srcCount.get(number) - 1;
                    srcCount.put(number, v);
                    if (v <= 0) {
                        src.put(number, true);
                    }
                    return true;
                }
                return false;
            }

            public boolean checkAllIn() {
                Iterator<Boolean> iterator = src.values().iterator();
                while (iterator.hasNext()) {
                    Boolean next = iterator.next();
                    if (!next) {
                        return false;
                    }
                }
                return true;
//                return src.values().stream().noneMatch(v -> !v.booleanValue());
            }

            public boolean clearMark() {
                srcCount.putAll(initSrcCount);
                srcCount.keySet().forEach(k -> src.put(k, false));
                return true;
            }
        }

        public FindAnyCombo(Integer[]... val) {
            for (int i = 0; i < val.length; i++) {
                if (val[i] == null) continue;
                dataSrc.add(new FindAny(val[i]));
            }
        }

        /**
         * @param
         * @return
         * @description 多个组合中匹配任意1个
         * @author Guang.OuYang
         * @date 2019/9/20
         */
        public FindAnyComboResult anyComboMath(Stream<Integer> stream) {
            return assignComboMath(stream, 1, true);
        }

        public FindAnyComboResult anyComboMathSizeNonEquals(Stream<Integer> stream) {return assignComboMath(stream, 1, false);}

        /**
         * @param
         * @return
         * @description 多个组合中匹配全部组合
         * @author Guang.OuYang
         * @date 2019/9/20
         */
        public FindAnyComboResult allComboMath(Stream<Integer> stream) {return assignComboMath(stream, dataSrc.size(), false); }
        public FindAnyComboResult allComboMathSizeEquals(Stream<Integer> stream) {return assignComboMath(stream, dataSrc.size(), true);}

        /**
         * @param stream         需要做筛选的数据集
         * @param matchComboSize 匹配组合数量为1时, 匹配给定的组合内任意一组
         * @param jump 如果所有组合的数量一致时开启它
         * @return
         * @description 特定数字组合寻找, 数据源200, 循环10w次, 结果:全部false,平均1ms/100次,结果:全部true,50ms/10w
         * @author Guang.OuYang
         * @date 2019/9/19
         */
        private FindAnyComboResult assignComboMath(Stream<Integer> stream, int matchComboSize, boolean jump) {
            FindAnyComboResult findAnyComboResult = new FindAnyComboResult();
            int i = 0;
            int iniSize = matchComboSize;//dataSrc.size();
            Iterator<Integer> iterator = stream.iterator();

            boolean find = false;
            int findCount = 0;
            co:
            while (iterator.hasNext()) {
                i++;
                Integer next = iterator.next();
                Iterator<FindAny> iterator1 = dataSrc.iterator();
                while (iterator1.hasNext()) {
                    FindAny findAny = iterator1.next();//2710
                    findAny.in(next);
                    //这里循环校验避免过多的check
                    find = (!jump || i % findAny.src.size() == 0) && findAny.checkAllIn();

                    //命中的组合
                    if (find) {
                        findAnyComboResult.getValues().addAll(findAny.getRepeatedSrcKey());
                        findAnyComboResult.getFindCombos().add(findAny);
                        ++findCount;
                        iterator1.remove();
                    }

                    //数量足够了
                    if (find = findCount == iniSize) {
                        break co;
                    }
                }
            }
            findAnyComboResult.setFind(find);
            return findAnyComboResult;
        }


//        /**
//         * @param stream         需要做筛选的数据集
//         * @param matchComboSize 匹配组合数量为1时, 匹配给定的组合内任意一组
//         * @return
//         * @description 字典查找,当多次查找在同个大量数据堆搜索大量数据可以减少约10倍搜索时间
//         * @author Guang.OuYang
//         * @date 2019/9/19
//         */
//        private boolean groupAssignComboMath(Stream<Integer> stream, int matchComboSize) {
//            int i = 0;
//            int iniSize = matchComboSize;//dataSrc.size();
//            List<List<Integer>> res = new ArrayList<>();
//            Iterator<Integer> iterator = stream.iterator();
//            while (iterator.hasNext()) {
//                Integer val = iterator.next();
//
//                int groupId = val / 10 + 1;
//
//                //寻找组
//                List<Integer> group = res.get(groupId);
//
//                if (group == null) {
//                    group = new ArrayList<>(10);
//                    res.add(group);
//                }
//                group.add(val);
//            }
//
//            return false;
//        }
    }

    @Data
    public static class FindAnyComboResult{
        private boolean find;
        private List<FindAnyCombo.FindAny> findCombos = new ArrayList<>();
        private List<Integer> values=new ArrayList<>();
    }

    /**
     * @param
     * @param removeRepeatedMath 移除重复数字后做匹配
     * @return
     * @description 判断数组内的数字是否连续, 时间复杂度O(n) 测试数据10w测试次数100,数组大小100,总平均160ms
     * @author Guang.OuYang
     * @date 2019/9/20
     */
    public static boolean isSerialNumber(int[] array, boolean removeRepeatedMath) {
        int max = Integer.MIN_VALUE;
        int min = Integer.MAX_VALUE;

        HashSet hashSet = new HashSet<>(array.length);

        int arraySum = 0;

        //剔除重复数字
        for (int i = 0; i < array.length; i++) {
            //不计入重复数字
            if (!hashSet.contains(array[i]))
                arraySum += array[i];

            hashSet.add(array[i]);

            if (!(hashSet.size() > i)) {
                if (!removeRepeatedMath) {
//                    System.out.println("找到重复的数字, 不做匹配");
                    return false;
                }
            }

            //找出最大最小数
            if (array[i] < min) {
                min = array[i];
            }

            if (array[i] > max) {
                max = array[i];
            }
        }

        if (max == min) {
//            System.out.println("组合内数量为1,顺序匹配不够");
            return false;
        }

        int arraySize = max - min;

        //10-1=9 10-0=11
        int arrayReSize = max + 1 - min;//min == 0 || min == 1 ? arraySize + 1 : arraySize;

        //最大数大于整体数组, 意味着这一串数字并不连续
        if (hashSet.size() != arrayReSize) {
//            System.out.println(min + "~" + max + "长度应为:" + arrayReSize + ",实际"+(removeRepeatedMath?"剔除重复后":"")+"长度:" + arrRveRepeated.length);
            return false;
        }

        //算出最小与最大数正确的和
        long sum = 0;
        for (int i = min; i <= max; i++) {
            sum += i;
        }

//        System.out.println("正确的和:" + sum + ",实际:" + arraySum);
        return arraySum == sum;
    }
}
