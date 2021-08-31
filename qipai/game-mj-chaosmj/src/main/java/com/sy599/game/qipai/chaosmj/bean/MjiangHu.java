package com.sy599.game.qipai.chaosmj.bean;

import java.util.*;
import java.util.function.BiFunction;

import com.sy599.game.qipai.chaosmj.rule.Mj;
import com.sy599.game.qipai.chaosmj.tool.MjTool;
import com.sy599.game.util.DataMapUtil;
import com.sy599.game.util.LogUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.NullArgumentException;

@Data
public class MjiangHu {
    /**
     * @description
     * @param
     * @return
     * @author Guang.OuYang
     * @date 2019/9/4
     */
    public static final BigMap<Integer, String, Integer> SCORE_CALC = new BigMap<Integer, String, Integer>(40) {
        {
            // 1.碰碰胡
            this.put(1, "+", 2);
            //2.将将胡
            this.put(2, "+", -1);
            // 3.七小对
            this.put(3, "+", 3);
            //4.清一色
            this.put(4, "+", 3);
            //5.豪华七小对4
            this.put(5, "+", 5);
            //6.超豪华七小对8
            this.put(6, "+", 10);
            //7.杠上开花
            this.put(7, "*", 2);
            //8.杠上炮
            this.put(8, "+", 0);
            //9.报听
            this.put(9, "+", -1);
            //10.抢杠胡 *-1为浮动倍率
            this.put(10, "*", -1);
            //11.黑天胡
            this.put(11, "+", 0);
            //12.地胡
            this.put(12, "+", 20);
            //13.倒底胡
            this.put(13, "+", 0);
            //14.天胡
            this.put(14, "+", 20);
            //15天天胡4
            this.put(15, "+", 0);
            //16地地胡4
            this.put(16, "+", 0);
            //17平胡1
            this.put(17, "+", 1);
            //18硬庄
            this.put(18, "*", 2);
//            //跟庄+1
            this.put(19, "+", 0);
            //十三幺
            this.put(20, "+", 13);
            //9.三豪华七小对30分,手上七对，其中有六对一样的。12
            this.put(21, "+", 15);
            //10.混一色4分
            //清一色加字牌。
            this.put(22, "+", 2);
            //12.字一色20分
            //只有东南西北中发白。
            this.put(23, "+", 10);
            //13.十八罗汉36分
            //四条杠加一对将共18张牌胡牌。
            this.put(24, "+", 18);
            //14.一九胡10分
            //全是一九和字牌组成的刻子加一对将。
            this.put(25, "+", 5);
            //15.清一九20分
            //只有一九组成的刻子加一对将。
            this.put(26, "+", 10);
            //海底捞
            this.put(27, "*", 2);
            //吃杠杠爆全包
            this.put(28, "+", 0);
            //十倍不计分
            this.put(29, "+", 0);
            //死胡
            this.put(30, "+", 0);
            //自摸
            this.put(31, "*", 2);
            //点炮
            this.put(32, "*", 2);
            //连庄
            this.put(33, "+", 0);
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
     * 三豪华7小对
     */
    private boolean san7xiaodui;
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
     * 吃杠, 杠爆, 全包
     */
    private int eatGangBurstAllInCharge;
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
     */
    private boolean tianhu;
    /**
     * 天天胡：手中有4张“王”自摸胡牌（需要满足胡牌类型），可以和其他牌型叠加如天天胡带平4+1，天天胡+胡牌番型（炮胡不算天天胡）
     * */
    private boolean tiantianhu;
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
     * <p>  1.碰碰胡2.将将胡 3.七小对4.清一色 5.豪华七小对4 6.超豪华七小对  7.杠上开花 8.杠上炮 9.报听 10.抢杠胡 11.黑天胡 12.地胡 13.倒底胡 14.天胡 15天天胡4 16地地胡4 17平胡1 18硬庄 </p>
     */
    private List<Integer> dahuList;
    /***/
    private List<Mj> showMajiangs;
    /**硬庄 */
    private boolean yingZhuang;
    /**跟庄*/
    private boolean genZhuang;
    /**十三幺*/
    private boolean shiSanYao;
    /**混一色*/
    private boolean hunYiSe;
    /**字一色*/
    private boolean ziYiSe;
    /**十八罗汉*/
    private boolean shiBaLuoHan;
    /**一九胡*/
    private boolean yiJiuHu;
    /**清一九*/
    private boolean qingYiJiu;
    /**十倍不计分*/
    private boolean shiBeiBuJiFen;
    /**十倍不计分*/
    private boolean deathHu;
    /**自摸*/
    private boolean ziMo;
    /**点炮*/
    private boolean dianPao;
    /**连庄*/
    private boolean lianZhuang;

    /**
     * @return
     * <p>  1.碰碰胡2.将将胡 3.七小对4.清一色 5.豪华七小对4 6.超豪华七小对  7.杠上开花 8.杠上炮 9.报听 10.抢杠胡 11.黑天胡 12.地胡 13.倒底胡 14.天胡 15天天胡4 16地地胡4 17平胡1 18硬庄 19跟庄 20十三幺
     * 21三豪华7对 22混一色 23字一色 24十八罗汉 25一九胡 26清一九 27海底捞月 28吃杠杠爆全包 29十倍不计分 30死胡 31自摸 32点炮 33连庄</p>
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
                boolConvertInt(genZhuang),
                boolConvertInt(shiSanYao),
                boolConvertInt(san7xiaodui),
                boolConvertInt(hunYiSe),
                boolConvertInt(ziYiSe),
                boolConvertInt(shiBaLuoHan),
                boolConvertInt(yiJiuHu),
                boolConvertInt(qingYiJiu),
                boolConvertInt(haidilaoyue),
                boolConvertInt(isEatGangBurstAllInCharge()),
                boolConvertInt(shiBeiBuJiFen),
                boolConvertInt(deathHu),
                boolConvertInt(ziMo),
                boolConvertInt(dianPao),
                boolConvertInt(lianZhuang),
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
    public static int calcMenZiScore(ChaosMjTable table, List<Integer> menZiSrc) {
        return calcMenZiScore(table, menZiSrc, 0);
    }
    /**
     * @param
     * @param menZiSrc 得分门子
     * @return
     * @description 计算门子得分
     * @author Guang.OuYang
     * @date 2019/9/2
     */
    public static int calcMenZiScore(ChaosMjTable table, List<Integer> menZiSrc, int basic) {
        if (CollectionUtils.isEmpty(menZiSrc)) {
            return 0;
        }
        ArrayList<Integer> menZi = new ArrayList<>(menZiSrc);

        //额外分,相乘的
        List<Integer> mult = new ArrayList<>();
        //额外分,相乘的, 在限制之外的门子, 天胡->硬庄
        List<Integer> mult2 = new ArrayList<>();
        //启用额外分
        int mult2SkyFloor = 0;
        //自摸点炮
        int mult2SelfGun = 0;
        //天地胡
        boolean mult2SkyFloorTrue = menZiSrc.contains(11) || menZiSrc.contains(13);

        //不叠加翻倍的番数仅生效一次, 叠加可重复算
        int repeatedIndex = (table).getGameModel().getSpecialPlay().isRepeatedEffect() ? 99999 : 1;

        Iterator<Integer> iterator = menZi.iterator();
        while (iterator.hasNext()) {
            Integer v = iterator.next();
            BigMapEntry<Integer, String, Integer> currentCalc = SCORE_CALC.get(v + 1);
            if (currentCalc != null) {
                //名堂分不叠加
                if (currentCalc.getE().equals("*") && --repeatedIndex >= 0) {           //*代表乘积
                    int ratio = currentCalc.getV();
                    //浮动倍率,根据人数-1
                    if (ratio == -1) {        //-1浮动倍率
                        ratio = table.getGameModel().getGameMaxHuman() - 1;
                    }

                    //硬庄加入额外不限制行列
                    if (table.getGameModel().topFen() && mult2SkyFloorTrue && currentCalc.getK().equals(18)) { //存在天地胡额外->硬庄得分
                        mult2SkyFloor += ratio;
                    } else if (table.getGameModel().topFen() && (currentCalc.getK().equals(31) || currentCalc.getK().equals(32))) { //自摸点炮
                        mult2SelfGun += ratio;
                    } else {
                        mult.add(ratio);
                    }

                    LogUtil.printDebug("门子翻倍 :基础倍{},索引{},{},倍数{}", currentCalc.getV(), currentCalc.getK(), MjTool.dahuListToString(Arrays.asList(currentCalc.getK() - 1)), mult);
                } else if (currentCalc.getE().equals("+")) {    //+代表直接增加
                    int bScore = currentCalc.getV();
                    //其他门子与平胡不共存, 有其他门子则不是平胡
                    bScore = Math.max(bScore, 0);
                    basic += bScore;

                    LogUtil.printDebug("门子增加 :基础分{},索引{},{},总增加{}", bScore, currentCalc.getK(), MjTool.dahuListToString(Arrays.asList(currentCalc.getK() - 1)), basic);
                }
            }
        }

//        basic = table.getGameModel().topFenCalc(basic);

        //倍率
        Iterator<Integer> iterator1 = mult.iterator();
        while (iterator1.hasNext()) {
            Integer multi = iterator1.next();
            basic *= multi;
        }

        LogUtil.printDebug("基础+分门子封顶:{}->{}", basic, table.getGameModel().topFenCalc(basic));

        basic = table.getGameModel().topFenCalc(basic);

        //存在封顶设置
        if (table.getGameModel().topFen() && mult2SkyFloor > 1 && mult2SkyFloorTrue) {
            basic *= mult2SkyFloor;
        }

        LogUtil.printDebug("基础+天地胡+硬庄:{}->{}", basic, mult2SkyFloor);

        //存在封顶设置
        if (table.getGameModel().topFen() && mult2SelfGun > 1) {
            basic *= mult2SelfGun;
        }

        LogUtil.printDebug("基础+天地胡+自摸:{}->{}", basic, mult2SkyFloor);

        return basic;
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

    public boolean isEatGangBurstAllInCharge(){
        return eatGangBurstAllInCharge > 0;
    }

    public int getDahuCount() {
//        int count = 0;
//        if (dahuList == null || dahuList.size() == 0) {
//            return 0;
//        }
//        for (Integer id : dahuList) {
//            if (id > 0) {
//                count += 1;
//            }
//
//        }
        return calcDaHuCount(buildDahuList());
    }

    /** <p>  1.碰碰胡2.将将胡 3.七小对4.清一色 5.豪华七小对4 6.超豪华七小对  7.杠上开花 8.杠上炮 9.报听 10.抢杠胡 11.黑天胡 12.地胡 13.倒底胡 14.天胡 15天天胡4 16地地胡4 17平胡1 18硬庄 19跟庄
     * 20十三幺  21三豪华7对 22混一色 23字一色 24十八罗汉 25一九胡 26清一九 27海底捞月 28吃杠杠爆全包 29十倍不计分 30死胡 31自摸 32点炮 33连庄</p>*/
    public static int calcDaHuCount(List<Integer> dahu) {
        if (CollectionUtils.isEmpty(dahu)) return 0;
        int res = 0;
        Iterator<Integer> iterator = dahu.iterator();
        while (iterator.hasNext()) {
            Integer next = iterator.next() + 1;
            //硬庄不算入门子,平胡不算入大胡
            if (next == 18 || next == 17 || next == 7 || next == 8 || next == 9 || next == 10 || next == 19 || next == 27 || next == 28 || next == 29 || next == 30 || next == 31 || next == 32 || next == 33)
                continue;
            res += 1;
        }
        return res;
    }

    /**
     * 仅有基础得分门子
     * <p>  1.碰碰胡2.将将胡 3.七小对4.清一色 5.豪华七小对4 6.超豪华七小对  7.杠上开花 8.杠上炮 9.报听 10.抢杠胡 11.黑天胡 12.地胡 13.倒底胡 14.天胡 15天天胡4 16地地胡4 17平胡1
     * 18硬庄 19跟庄 20十三幺  21三豪华7对 22混一色 23字一色 24十八罗汉 25一九胡 26清一九 27海底捞月 28吃杠杠爆全包 29十倍不计分 30死胡 31自摸 32点炮</p>*/
    public static List<Integer> calcDaHuBasic(List<Integer> dahu) {
        if (CollectionUtils.isEmpty(dahu)) Collections.emptyList();
        List<Integer> res = new ArrayList<>();
        Iterator<Integer> iterator = dahu.iterator();
        while (iterator.hasNext()) {
            Integer next = iterator.next() + 1;
            //硬庄不算入门子
            if (/* next == 18 || next == 7 || next == 8 || */next == 9 || /*next == 10 || next == 13 || next == 28 */ next == 29 || next == 30) continue;
            res.add(next-1);
        }
        return res;
    }

     /**
      * 仅有基础门子
      * <p>  1.碰碰胡2.将将胡 3.七小对4.清一色 5.豪华七小对4 6.超豪华七小对  7.杠上开花 8.杠上炮 9.报听 10.抢杠胡 11.黑天胡 12.地胡 13.倒底胡 14.天胡 15天天胡4 16地地胡4 17平胡1
     * 18硬庄 19跟庄 20十三幺  21三豪华7对 22混一色 23字一色 24十八罗汉 25一九胡 26清一九 27海底捞月 28吃杠杠爆全包 29十倍不计分 30死胡 31自摸 32点炮</p>*/
    public static List<Integer> calcDaHuBasicBase(List<Integer> dahu) {
        if (CollectionUtils.isEmpty(dahu)) Collections.emptyList();
        List<Integer> res = new ArrayList<>();
        Iterator<Integer> iterator = dahu.iterator();
        while (iterator.hasNext()) {
            Integer next = iterator.next() + 1;
            //不算入门子,平胡与其他门子不共存
            if (next == 7
                    || next == 8
                    || next == 9
                    || next == 10
                    || next == 13
                    || next == 17
                    || next == 18
                    || next == 19
                    || next == 28
                    || next == 29
                    || next == 30
                    || next == 31
                    || next == 32)
                continue;
            res.add(next - 1);
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
}
