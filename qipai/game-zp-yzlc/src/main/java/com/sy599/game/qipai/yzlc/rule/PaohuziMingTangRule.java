package com.sy599.game.qipai.yzlc.rule;

import com.sy599.game.qipai.yzlc.bean.PaohzDisAction;
import com.sy599.game.qipai.yzlc.bean.YzLcPaohuziPlayer;
import com.sy599.game.qipai.yzlc.bean.YzLcPaohuziTable;
import com.sy599.game.qipai.yzlc.constant.PaohzCard;
import com.sy599.game.qipai.yzlc.tool.PaohuziTool;
import com.sy599.game.qipai.yzlc.bean.CardTypeHuxi;
import com.sy599.game.qipai.yzlc.bean.GameModel;
import com.sy599.game.util.LogUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.lang.NullArgumentException;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public class PaohuziMingTangRule {

    public static final int LOUDI_MINGTANG_HeiHu = 1;//4.5 黑胡：胡牌牌型红字牌张数=0;计分翻倍。
    public static final int LOUDI_MINGTANG_HongHu = 2;//红戳：胡牌时所有戳子全部是红色牌张。由于一和壹是固定戳子，在没有其他黑色牌张的情况下计算为红戳。计分为戳子数目乘3，番戳时为戳子数目乘6。如果勾选红戳4番，则计分为戳子数目乘4，番戳时为戳子数目乘8。
    public static final int LOUDI_MINGTANG_JianHongJiaFen = 3;//见红加分：每张红颜色的字牌都会加分，其中每张“二贰七柒”加一分，每张“十拾”加2分。
    public static final int LOUDI_MINGTANG_HongChuoJiaFan = 4;//红戳4番，则计分为戳子数目乘4，番戳时为戳子数目乘8。
    public static final int LOUDI_MINGTANG_FanChuo = 5;//番戳：2人和3人场当戳子数大于等于18戳时（4人场为15戳时）称为“番戳”，基本分大为提高。18戳记基本分14分，每多1戳多2分。19戳记基本分16分，20戳记基本分18分，以此类推。定戳时不可选番戳。

    /**
     * @description
     * @param
     * @return
     * @author Guang.OuYang
     * @date 2019/9/4
     */
    //大六八
    public static final BigMap<Integer, String, int[]> SCORE_CALC = new BigMap<Integer, String, int[]>(30) {
        {
			//1.黑戳：胡牌时所有戳子全部是黑色牌张，即没有二七十和贰柒拾。计分为戳子数目乘2，番戳时为戳子数目乘4
            put(LOUDI_MINGTANG_HeiHu, "*", new int[] { 2, 4 });
            put(LOUDI_MINGTANG_HongHu, "*", new int[] { 3, 6 });
            put(LOUDI_MINGTANG_JianHongJiaFen, "+", new int[] { 0, 0 });
			put(LOUDI_MINGTANG_HongChuoJiaFan, "*", new int[] { 4, 8 });
			put(LOUDI_MINGTANG_FanChuo, "+", new int[] { 0, 0 });

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
		private final int DEFAULT_CAPACITY = 16;

		private HashMap<K , BigMapEntry<K, E, V>> bigMap ;

		BigMap() {
			bigMap = new HashMap<>(DEFAULT_CAPACITY);
		}

		BigMap(int capacity) {
			bigMap = new HashMap<>(capacity);
		}

		public void put(K k, E e, V v) {
			if (bigMap == null) {
				throw new NullArgumentException("BigMap don't init..");
			}
			this.bigMap.put(k, new BigMapEntry<>(k, e, v));
		}

		public void merge(K k, E e, V v, BiFunction<? super BigMapEntry<K, E, V>, ? super BigMapEntry<K, E, V>, ? extends BigMapEntry<K, E, V>> remappingFunction) {
			bigMap.merge(k, new BigMapEntry<>(k,e,v), remappingFunction);
		}

		public BigMapEntry<K, E, V> get(K k) {
			return bigMap.get(k);
		}

		public Iterator<BigMapEntry<K, E, V>> iterable(){
			return this.bigMap.values().iterator();
		}

		public String toString() {
			StringBuilder sb = new StringBuilder();
			Iterator<BigMapEntry<K, E, V>> iterable = iterable();
			while (iterable.hasNext()) {
				BigMapEntry<K, E, V> next = iterable.next();
				sb.append("," + next.toString());
			}
			return sb.length() > 0 ? sb.deleteCharAt(0).toString() : sb.toString();
		}
//        final int reHash(Object key) {
//            int hash = key.hashCode() % bigMap.length;
//            int reHash = hash == 0 ? key.hashCode() == 0 ? 0 : (key.hashCode() + hash - 1) : hash - 1;
//            return reHash;
//        }
	}

	@Data
	@AllArgsConstructor
	public static class BigMapEntry<K, E, V> {
		private K k;
		private E e;
		private V v;

		BigMapEntry put(K k, E e, V v) {
			this.e = e;
			this.k = k;
			this.v = v;
			return this;
		}

		public K getK(){return this.k;}
		public E getE(){return this.e;}
		public V getV(){return this.v;}

		@Override
		public String toString() {
			return "[K=" + k + " , E=" + e + " , V=" + v + "]";
		}
	}
    /**
     * 名堂
     *
     * @param player 胡牌玩家
     * @return  map k->名堂 v->额外加分这个分数的运算取决于名堂内的分数是增加还是翻番
     */
    public static Map<Integer,Integer> calcMingTang(YzLcPaohuziPlayer player, YzLcPaohuziTable table) {
        Map<Integer,Integer> mtList = new HashMap<>();
        List<PaohzCard> allCards = new ArrayList<>();
        for (CardTypeHuxi type : player.getCardTypes()) {
            allCards.addAll(PaohuziTool.toPhzCards(type.getCardIds()));
        }

		//红牌数量
		int redCardCount = Optional.ofNullable(PaohuziTool.findRedPhzs(allCards)).orElse(Collections.emptyList()).size();

        //2.红戳：胡牌时所有戳子全部是红色牌张。由于一和壹是固定戳子，在没有其他黑色牌张的情况下计算为红戳。计分为戳子数目乘3，番戳时为戳子数目乘6。如果勾选红戳4番，则计分为戳子数目乘4，番戳时为戳子数目乘8。
		redCardCount = (allCards.size() - redCardCount == player.getCardTypes().get(0).getCardIds().size()) ? redCardCount + player.getCardTypes().get(0).getCardIds().size() : redCardCount;

	//        //小牌数量
//        int smallCardsSize = Optional.ofNullable(PaohuziTool.findSmallPhzs(allCards)).orElse(Collections.emptyList()).size();

		LogUtil.printDebug("红牌数量:{} ", redCardCount);
		if (table.getGameModel().getSpecialPlay().isRedBlackHu() && redCardCount == 0) {
			mtList.put(LOUDI_MINGTANG_HeiHu, 0);
			LogUtil.printDebug("1.黑戳：胡牌时所有戳子全部是黑色牌张，即没有二七十和贰柒拾。计分为戳子数目乘2，番戳时为戳子数目乘4。");
		} else if (table.getGameModel().getSpecialPlay().getRedChuoNFan() > 0 && redCardCount == allCards.size()) {
			mtList.put(LOUDI_MINGTANG_HongChuoJiaFan, 0/*table.getGameModel().getSpecialPlay().getRedChuoNFan()*/);
			LogUtil.printDebug("红戳4番，则计分为戳子数目乘4，番戳时为戳子数目乘8。");
		} else if (table.getGameModel().getSpecialPlay().isRedBlackHu() && redCardCount == allCards.size()) {
			mtList.put(LOUDI_MINGTANG_HongHu, 0);
			LogUtil.printDebug("2.红戳：胡牌时所有戳子全部是红色牌张。由于一和壹是固定戳子，在没有其他黑色牌张的情况下计算为红戳。计分为戳子数目乘3，番戳时为戳子数目乘6。如果勾选红戳4番，则计分为戳子数目乘4，番戳时为戳子数目乘8。");
		}else if (table.getGameModel().getSpecialPlay().isRedAddScore() && redCardCount > 0) {
			mtList.put(LOUDI_MINGTANG_JianHongJiaFen, allCards.stream().mapToInt(v -> (v.getPai() == 2 || v.getPai() == 7) ? 1 : v.getPai() == 10 ? 2 : 0).sum());
			LogUtil.printDebug("见红加分：每张红颜色的字牌都会加分，其中每张“二贰七柒”加一分，每张“十拾”加2分。");
		}

		if (table.getGameModel().getSpecialPlay().isDoubleChuo() && (player.getHuxi() >= table.getGameModel().getDoubleChuoHuXi())) {
			mtList.put(LOUDI_MINGTANG_FanChuo, 0);
			LogUtil.printDebug("番戳：2人和3人场当戳子数大于等于18戳时（4人场为15戳时）称为“番戳”，基本分大为提高。18戳记基本分14分，每多1戳多2分。19戳记基本分16分，20戳记基本分18分，以此类推。定戳时不可选番戳。");
		}

		return mtList;
    }

    private static boolean isPPHu(YzLcPaohuziPlayer player) {
        long t = System.currentTimeMillis();
        //不能有吃
        boolean ppHu = !player.getCardTypes().stream().anyMatch(v -> v.getAction() == PaohzDisAction.action_small_face);

        //检测手牌中是否有顺子
        if (ppHu && player.getHandPais().size() > 3) {
            List<PaohzCard> handCards = new ArrayList<>();
            handCards.addAll(player.getHandPhzs());
            handCards.addAll(player.getTi());
            handCards.addAll(player.getZai());
            handCards.addAll(player.getChi());
            handCards.addAll(player.getPeng());
            handCards.addAll(player.getPao());

            //除非天胡否则把最后摸出来的牌算入
            if (!player.isFirstDisSingleCard() && !handCards.contains(player.getPlayingTable(YzLcPaohuziTable.class).getNowDisCardIds().get(0))) {
                //胡牌时把最后张底牌算进去,这张牌的组合不确定所以把玩家所有牌都算入
                handCards.add(player.getPlayingTable(YzLcPaohuziTable.class).getNowDisCardIds().get(0));
            }

            //找到所有牌组中3个以上的组合值val
            Map<Integer, Integer> countGeTwo = findCountGeTwo(handCards);

            //除掉手牌中的3个和4个
            countGeTwo.entrySet().stream().filter(v -> v.getValue() >= 2).forEach(v -> {
                handCards.removeIf(v1 -> v1.getVal() == v.getKey());
            });

//            //除掉手牌中的3个和4个
//            handCards.removeIf(v -> threeOrFourCountCards.contains(v.getVal()));

//            //匹配找到所有组合
//            Integer[][] allCombo = comboAllSerial(player.getPlayingTable(XtPaohuziTable.class).getGameModel());

            //匹配所有顺子组合, 匹配到了则不能组成碰碰胡
//            ppHu = !new FindAnyCombo(allCombo).anyComboMath(handCards.stream().map(v -> v.getVal()));
            ppHu = handCards.size() == 0;
        }

        LogUtil.printDebug("碰碰胡匹配:{}ms", (System.currentTimeMillis() - t));
        return ppHu;
    }

    /**
     *@description 找到牌组中所有大于3张的牌组
     *@param
     *@return
     *@author Guang.OuYang
     *@date 2019/9/26
     */
    public static Map<Integer, Integer> findCountGeTwo(List<PaohzCard> cards) {
        Map<Integer, Integer> res = new HashMap<Integer, Integer>();
        cards.sort((v1, v2) -> Integer.valueOf(v1.getPai()).compareTo(Integer.valueOf(v2.getPai())));

        //最低匹配数量
        int minMathCount = 2;
        int prePai=0;   //上一个牌
        int repeated=0; //当前计算的数量
        ArrayList<Integer> paohzCards = new ArrayList<>();

        Iterator<PaohzCard> iterator = cards.iterator();
        while (iterator.hasNext()) {
            PaohzCard next = iterator.next();
            if (prePai > 0 && prePai > 0 && prePai != next.getVal()) {
                repeated = 0;
                prePai = 0;
            }
            if (prePai == 0 || prePai == next.getVal()) {
                prePai = next.getVal();
                repeated += 1;
            }
            if (repeated == minMathCount) {
                paohzCards.add(next.getVal());
            }

            res.merge(next.getVal(), repeated, (oV, nV) -> nV);
        }
        return res;
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
     * @param
     * @return
     * @description 计算名堂分, 番数, 分数=囤数*番数(名堂)。 囤数=胡息/3 , 这里直接计算胡息
     * @author Guang.OuYang
     * @date 2019/9/2
     */
    public static int calcMingTangFen(int totalTun, YzLcPaohuziTable table, Map<Integer,Integer> mt) {
//        //总囤
        int totalTunNumber = totalTun;
		int totalFan = 0;  //总番数
        //额外增加的胡息
        int addHuXi = 0;
        //默认没有大六八小六八区分
		int doubleChuo = table.getGameModel().getSpecialPlay().isDoubleChuo() && totalTun >= table.getGameModel().getDoubleChuoHuXi() ? 1: 0;

        //不叠加翻倍的番数仅生效一次, 叠加可重复算
        int repeatedIndex = (table).getGameModel().getSpecialPlay().isRepeatedEffect() ? 99999 : 1;

		Iterator<Entry<Integer, Integer>> iterator = mt.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<Integer, Integer> kv = iterator.next();
			BigMapEntry<Integer, String, int[]> currentCalc = SCORE_CALC.get(kv.getKey());
            if (currentCalc != null && currentCalc.getK() != LOUDI_MINGTANG_JianHongJiaFen) {
                //名堂分不叠加
				if (currentCalc.getE().equals("*") && --repeatedIndex >= 0) {           //*代表乘积
					totalFan += (currentCalc.getV()[currentCalc.getV().length > doubleChuo ? doubleChuo : 0] + kv.getValue());
					LogUtil.printDebug("名堂分翻倍 : {},{}, {} ,{}", currentCalc.getV(),doubleChuo, currentCalc.getK(), totalFan);
				}else if (currentCalc.getE().equals("+")) {    //+代表直接增加胡息
					addHuXi += (currentCalc.getV()[currentCalc.getV().length > doubleChuo ? doubleChuo : 0] + kv.getValue());
					LogUtil.printDebug("名堂分增加 :{},{},  {}, {}", currentCalc.getV(),doubleChuo, currentCalc.getK(), addHuXi);
				}
            }
        }
//
		int res = totalTunNumber + addHuXi;

		res *= Math.max(totalFan, 1);

//		if (table.getGameModel().getTopScore() > 0 && table.getGameModel().getTopScore() < res) {
//			res = table.getGameModel().getTopScore();
//		}

		return res;

//		return 0;
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

//        int arraySize = max - min;

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

}
