package com.sy599.game.qipai.tjmj.bean;

import java.lang.reflect.Array;
import java.util.*;

import com.sy599.game.qipai.tjmj.rule.Mj;
import com.sy599.game.qipai.tjmj.rule.MjRule;
import com.sy599.game.qipai.tjmj.tool.MjTool;
import com.sy599.game.util.DataMapUtil;
import com.sy599.game.util.LogUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.NullArgumentException;

@Data
public class MjiangHu implements Cloneable{
    /**
     * @description
     * @param
     * @return
     * @author Guang.OuYang
     * @date 2019/9/4
     */
    public static final BigMap<Integer, String, Integer> SCORE_CALC = new BigMap<Integer, String, Integer>(30) {
        {
            // 1.碰碰胡
            this.put(1, "+", -1);
            //2.将将胡
            this.put(2, "+", -1);
            // 3.七小对
            this.put(3, "+", -1);
            //4.清一色
            this.put(4, "+", -1);
            //5.豪华七小对4
            this.put(5, "*", 2);
            //6.超豪华七小对
            this.put(6, "*", 4);
            //7.杠上开花
            this.put(7, "+", -1);
            //8.杠上炮
            this.put(8, "+", 0);
            //9.报听
            this.put(9, "+", -1);
            //10.抢杠胡
            this.put(10, "+", 0);
            //11.黑天胡
            this.put(11, "+", -1);
            //12.地胡
            this.put(12, "+", -1);
            //13.倒底胡
            this.put(13, "+", -1);
            //14.天胡
            this.put(14, "+", -1);
            //15天天胡4
            this.put(15, "+", 4);
            //16地地胡4
            this.put(16, "+", 4);
            //17平胡1
            this.put(17, "+", 1);
            //18硬庄
            this.put(18, "*", 2);
            //天天天胡
            this.put(19, "+", 6);
        }
    };

    /**
     * 是否平胡
     */
    private boolean pingHu;
    //------------------------------大胡----------------------------
    /**
     * 碰碰胡
     */
    private boolean pengpengHu;
    /**
     * 将将胡
     */
    private boolean jiangjiangHu;
    /**
     * 清一色
     */
    private boolean qingyiseHu;
    /**
     * 双豪华7小对
     */
    private boolean shuang7xiaodui;
    /**
     * 豪华7小对
     */
    private boolean hao7xiaodui;
    /**
     * 7小对
     */
    private boolean xiaodui;
    /**
     * 全求人
     */
    private boolean quanqiuren;
    /**
     * 抢杠胡
     */
    private boolean isQGangHu;
    /**
     * 杠上炮
     */
    private boolean isGangShangPao;
    /**
     * 杠上花
     */
    private boolean isGangShangHua;
    /**
     * 海底捞
     */
    private boolean haidilaoyue;
    /**
     * 海底炮
     */
    private boolean haidipao;
    /**
     * 天胡
	 * 8王①天胡：手中有 4 张“王”自摸胡牌（需要满足胡牌类型），可以和其他牌型叠加如天胡带 平 2+1，天胡+胡牌番型（炮胡不算天胡）
     */
    private boolean tianhu;
    /**
     * 天天胡：手中有4张“王”自摸胡牌（需要满足胡牌类型），可以和其他牌型叠加如天天胡带平4+1，天天胡+胡牌番型（炮胡不算天天胡）
	 * 8王②天天胡：手中有 5 张“王”自摸胡牌（需要满足胡牌类型），可以和其他牌型叠加如天天 胡带平 4+1，天胡+胡牌番型（炮胡不算天胡）
     * */
    private boolean tiantianhu;
    /**
	 * 8王③天天天胡：手中有 6 张“王”自摸胡牌（需要满足胡牌类型），可以和其他牌型叠加如天 天天胡带平 6+1，天胡+胡牌番型（炮胡不算天胡）
	 * */
    private boolean tiantiantianhu;
    /**
     * 地胡
     */
    private boolean dihu;

    /**
     * 抢杠胡
     */
    private boolean qiangGangHu;

    /**
     * 报听
     */
    private boolean baoTing;

    /**
     * 门清
     */
    private boolean menqing;

    //------------------------------------小胡-----------------------------
    /**
     * 大四喜
     */
    private boolean dasixi;
    /**
     * 黑天胡&板板胡
     */
    private boolean heitianhu;
    /**
     * 倒底胡: 庄家开局14张就可以胡牌，闲家没有倒底胡。
     * */
    private boolean daodihuh;
    /**
     * 地地胡：手中有4张“地牌”即可胡牌，若有其他牌型算地胡+胡牌牌型，（炮胡不算地胡）
     * */
    private boolean didihu;
    /**
     * 缺一色
     */
    private boolean queyise;
    /**
     * 六六顺
     */
    private boolean liuliushun;
    /**
     * 节节高
     */
    private boolean jiejiegao;
    /**
     * 三同
     */
    private boolean santong;
    /**
     * 一枝花
     */
    private boolean yizhihua;
    /**
     * 中途四喜
     */
    private boolean zhongtusixi;
    /**
     * 中途六六顺
     */
    private boolean zhongtuliuliushun;
    /**
     * 金童玉女
     */
    private boolean jinTongYuNu;

    /**
     * 是否胡
     */
    private boolean isHu;
    /**
     * 是否小胡
     */
    private boolean isXiaohu;
    /**
     * 是否大胡
     */
    private boolean isDahu;
    /**
     * 小胡列表
     */
    private List<Integer> xiaohuList;

    private HashMap<Integer, Map<Integer, List<Mj>>> xiaohuMap = new HashMap<>();

    /**
     * <p>  1.碰碰胡2.将将胡 3.七小对4.清一色 5.豪华七小对4 6.超豪华七小对  7.杠上开花 8.杠上炮 9.报听 10.抢杠胡 11.黑天胡 12.地胡 13.倒底胡 14.天胡 15天天胡4 16地地胡4 17平胡1 18硬庄 19天天天胡</p>
     */
    private List<Integer> dahuList;
    /**
     *
     */
    private List<Mj> showMajiangs;

    /**
     * 硬庄
     */
    private boolean yingZhuang;

    /**
     * @return
     * <p>  1.碰碰胡2.将将胡 3.七小对4.清一色 5.豪华七小对4 6.超豪华七小对  7.杠上开花 8.杠上炮 9.报听 10.抢杠胡 11.黑天胡 12.地胡 13.倒底胡 14.天胡 15天天胡4 16地地胡4 17平胡1 18硬庄 19天天天胡 </p>
     */
    public List<Integer> buildDahuList() {
        int[] arr = {
                boolConvertInt(pengpengHu),
                boolConvertInt(jiangjiangHu),
                boolConvertInt(xiaodui),
                boolConvertInt(qingyiseHu),
                boolConvertInt(hao7xiaodui),
                boolConvertInt(shuang7xiaodui),
                boolConvertInt(isGangShangHua),
                boolConvertInt(isGangShangPao),
				boolConvertInt(baoTing),
                boolConvertInt(qiangGangHu),
                boolConvertInt(heitianhu),
                boolConvertInt(dihu),
                boolConvertInt(daodihuh),
                boolConvertInt(tianhu),
                boolConvertInt(tiantianhu),
                boolConvertInt(didihu),
                boolConvertInt(pingHu),
                boolConvertInt(yingZhuang),
                boolConvertInt(tiantiantianhu),
        };

        List<Integer> dahu = DataMapUtil.indexToValList(arr);
        if (!dahu.isEmpty()) {
            return dahu;
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * @param
     * @param menZiSrc 得分门子
     * @return
     * @description 计算门子得分
     * @author Guang.OuYang
     * @date 2019/9/2
     */
    public static int calcMenZiScore(TjMjTable table, List<Integer> menZiSrc, boolean isSelfMo) {
        if (CollectionUtils.isEmpty(menZiSrc)) {
            return 0;
        }
        ArrayList<Integer> menZi = new ArrayList<>(menZiSrc);
        int basic = 0;
        //额外分,相乘的
        List<Integer> mult = new ArrayList<>();

        //平胡
        boolean isPingHu = menZi.contains(16);

        //有豪华七对或超豪华,增加7对底分加成
        if (menZi.contains(4) || menZi.contains(5)) {
            menZi.add(2);
        }

        //不叠加翻倍的番数仅生效一次, 叠加可重复算
        int repeatedIndex = (table).getGameModel().getSpecialPlay().isRepeatedEffect() ? 99999 : 1;

        Iterator<Integer> iterator = menZi.iterator();
        while (iterator.hasNext()) {
            Integer v = iterator.next();
            BigMap<Integer, String, Integer> currentCalc = SCORE_CALC.get(v + 1);
            if (currentCalc != null) {
                //名堂分不叠加
                if (currentCalc.getE().equals("*") && --repeatedIndex >= 0) {           //*代表乘积
                    mult.add(currentCalc.getV());
                    LogUtil.printDebug("门子翻倍 :基础倍{},索引{},{},倍数{}", currentCalc.getV(), currentCalc.getK(), MjTool.dahuListToString(Arrays.asList(currentCalc.getK() - 1)), mult);
                } else if (currentCalc.getE().equals("+")) {    //+代表直接增加
                    int bScore = currentCalc.getV();
                    //这里的底分都根据是自摸和点炮进行变动
                    if (/*!isPingHu && */bScore == -1) {
                        bScore = table.getGameModel().getSpecialPlay().getPaoHuGungHuBasicScore(isSelfMo);
                    } else {
                        //其他门子与平胡不共存, 有其他门子则不是平胡
                        bScore = Math.max(bScore, 0);
                    }
                    basic += table.getGameModel().calcBasicRatio(bScore);
                    LogUtil.printDebug("门子增加 :基础分{},索引{},{},总增加{}", bScore, currentCalc.getK(), MjTool.dahuListToString(Arrays.asList(currentCalc.getK()-1)), basic);
                }
            }
        }

        //倍率
        Iterator<Integer> iterator1 = mult.iterator();
        while (iterator1.hasNext()) {
            Integer multi = iterator1.next();
            basic *= multi;
        }

        return basic;
    }

	public boolean isSevenDui() {
    	return isXiaodui()||isHao7xiaodui()||isShuang7xiaodui();
	}

	public int boolConvertInt(boolean v) {
        return v ? 1 : 0;
    }


    public boolean isPingHu() {
        return pingHu;
    }


    public boolean isMenqing() {
        return menqing;
    }

    public void setMenqing(boolean menqing) {
        this.menqing = menqing;
    }

    public void setPingHu(boolean pingHu) {
        this.pingHu = pingHu;
    }


    public int getDahuCount() {
        return calcDaHuCount(buildDahuList());
    }

    public int getDahuCountAll() {
        return calcDaHuCountAll(buildDahuList());
    }

    /**
     *@description 对比能胡牌的门子
     *@param
     *@return
     *@author Guang.OuYang
     *@date 2019/12/21
     */
    public static boolean canHuMenZi(List<Integer> v1, List<Integer> v2){
        //
        Iterator<Integer> iterator = v1.iterator();
        while (iterator.hasNext()) {
            Integer next = iterator.next() + 1;
            if ((next == 1 || next == 2 || next == 3 || next == 4 || next == 5 || next == 6 || next == 11 || next == 13 || next == 17) && v2.contains(next - 1)) {
                return true;
            }
        }

        return false;
    }

    /**
     *@description 对比能胡牌的门子
     *@param
     *@return
     *@author Guang.OuYang
     *@date 2019/12/21
     */
    public static int canHuMenZi(List<Integer> v1) {
        //
        int c = 0;
        Iterator<Integer> iterator = v1.iterator();
        while (iterator.hasNext()) {
            Integer next = iterator.next() + 1;
            if ((next == 1 || next == 2 || next == 3 || next == 4 || next == 5 || next == 6 || next == 11 || next == 12 || next == 13  || next == 16 || next == 17)) {
                c += 1;
            }
        }

        return c;
    }

    /** <p>  1.碰碰胡2.将将胡 3.七小对4.清一色 5.豪华七小对4 6.超豪华七小对  7.杠上开花 8.杠上炮 9.报听 10.抢杠胡 11.黑天胡 12.地胡 13.倒底胡 14.天胡 15天天胡4 16地地胡4 17平胡1 18硬庄 </p>*/
    public static int calcDaHuCount(List<Integer> dahu) {
        if (CollectionUtils.isEmpty(dahu)) return 0;
        int res = 0;
        Iterator<Integer> iterator = dahu.iterator();
        while (iterator.hasNext()) {
            Integer next = iterator.next() + 1;
            //硬庄不算入门子,平胡不算入大胡,天胡不做胡牌牌型
            if (next == 18 || next == 17 || next == 16 || next == 12 || next  == 14 || next == 15 /*|| next == 8*/) continue;
            res += 1;
        }
        return res;
    }


    /** <p>  1.碰碰胡2.将将胡 3.七小对4.清一色 5.豪华七小对4 6.超豪华七小对  7.杠上开花 8.杠上炮 9.报听 10.抢杠胡 11.黑天胡 12.地胡 13.倒底胡 14.天胡 15天天胡4 16地地胡4 17平胡1 18硬庄 </p>*/
    public static int calcDaHuCountAll(List<Integer> dahu) {
        if (CollectionUtils.isEmpty(dahu)) return 0;
        int res = 0;
        Iterator<Integer> iterator = dahu.iterator();
        while (iterator.hasNext()) {
            Integer next = iterator.next() + 1;
            //硬庄不算入门子,平胡不算入大胡,天胡不做胡牌牌型
//            if (next == 18 || next == 17 || next == 16 || next == 12 || next == 14 || next == 15 /*|| next == 8*/) continue;
            res += 1;
        }
        return res;
    }

    public void initDahuList() {
        //有其他门子,不能算平胡
        if (getDahuCount() > 0) {
            setPingHu(false);
        }

        this.dahuList = buildDahuList();
        if (!this.dahuList.isEmpty()) {
            isDahu = true;
        }
    }

	public MjiangHu clone() {
		try {
			return (MjiangHu) super.clone();
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

    public void addToDahu(List<Integer> dahuList) {
        if (this.dahuList == null) {
            this.initDahuList();
        }
        for (int dahu : dahuList) {
            // 杠上花和杠上炮可以算两次
            this.dahuList.add(dahu);
        }
    }

    public static int getDaHuPointCount(List<Integer> daHuList) {
        if (daHuList == null || daHuList.size() == 0) {
            return 0;
        }
        int count = 0;
        for (int dahu : daHuList) {
            if (dahu == 11) {
                count += 3;
            } else if (dahu == 6) {
                count += 2;
            } else {
                //天胡地胡不算大胡20180803
                count += 1;
            }
        }
        return count;
    }


    public void addXiaoHu(int xiaohu, Map<Integer, List<Mj>> map) {
        xiaohuMap.put(xiaohu, map);
    }

    public HashMap<Integer, Map<Integer, List<Mj>>> getXiaohuMap() {
        return xiaohuMap;
    }

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
            int reHash = hash == 0 ? k.hashCode() == 0 ? 0 : (k.hashCode() + hash - 1) : hash - 1;
            bigMap[reHash] = new BigMap<K, E, V>().putAll(k, e, v);
        }

        public BigMap<K, E, V> get(K k) {
            int hash = k.hashCode() % bigMap.length;
            int reHash = hash == 0 ? k.hashCode() == 0 ? 0 : (k.hashCode() + hash - 1) : hash - 1;
            return hash < bigMap.length ? bigMap[reHash] : null;
        }
    }
}
