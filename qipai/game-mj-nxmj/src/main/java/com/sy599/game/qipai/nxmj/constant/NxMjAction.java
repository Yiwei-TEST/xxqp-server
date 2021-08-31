package com.sy599.game.qipai.nxmj.constant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NxMjAction {

    public static final int ACTION_SIZE = 20;

    /*** 接炮*/
    public static final int HU = 0;
    /*** 碰*/
    public static final int PENG = 1;
    /*** 明杠*/
    public static final int MINGGANG = 2;
    /*** 暗杠*/
    public static final int ANGANG = 3;
    /*** 吃*/
    public static final int CHI = 4;
    /*** 补张*/
    public static final int BUZHANG = 5;
    /*** 缺一色*/
    public static final int QUEYISE = 6;
    /*** 板板胡*/
    public static final int BANBANHU = 7;
    /*** 一枝花*/
    public static final int YIZHIHUA = 8;
    /*** 六六顺*/
    public static final int LIULIUSHUN = 9;
    /*** 大四喜*/
    public static final int DASIXI = 10;
    /*** 金童玉女*/
    public static final int JINGTONGYUNU = 11;
    /*** 节节高*/
    public static final int JIEJIEGAO = 12;
    /*** 三同*/
    public static final int SANTONG = 13;
    /*** 中途四喜*/
    public static final int ZHONGTUSIXI = 14;
    /*** 中途六六顺*/
    public static final int ZHONGTULIULIUSHUN = 15;
    /*** 暗杠补张*/
    public static final int BUZHANG_AN = 16;
    /*** 自摸*/
    public static final int ZIMO = 17;
    /*** 报听*/
    public static final int BAOTING = 18;
    /*** 一点红*/
    public static final int YIDIANHONG = 19;

    /*** 大胡自摸*/
    public static final int ACTION_COUNT_DAHU_ZIMO = 0 ;
    /*** 小胡自摸*/
    public static final int ACTION_COUNT_XIAOHU_ZIMO = 1;
    /*** 大胡点炮*/
    public static final int ACTION_COUNT_DAHU_DIANPAO = 2;
    /*** 小胡点炮*/
    public static final int ACTION_COUNT_XIAOHU_DIANPAO = 3;
    /*** 大胡接炮*/
    public static final int ACTION_COUNT_DAHU_JIEPAO = 4;
    /*** 小胡接炮*/
    public static final int ACTION_COUNT_XIAOHU_JIEPAO = 5;


    /** 小胡展示的优先级*/
    public static final List<Integer> xiaoHuPriority = Arrays.asList(DASIXI,LIULIUSHUN,JIEJIEGAO,SANTONG,JINGTONGYUNU,BANBANHU,QUEYISE,YIZHIHUA,YIDIANHONG,ZHONGTUSIXI,ZHONGTULIULIUSHUN);

    private int [] arr;

    public NxMjAction(){
        this.arr = new int[ACTION_SIZE];
    }

    public int[] getArr(){
        return arr;
    }

    public void addHu() {
        setVal(HU);
    }

    public void addPeng() {
        setVal(PENG);
    }
    public void addMingGang() {
        setVal(MINGGANG);
    }
    public void addAnGang() {
        setVal(ANGANG);
    }
    public void addBaoTing() {
    	setVal(BAOTING);
    }
    public void addChi() {
        setVal(CHI);
    }
    public void addBuZhang() {
        setVal(BUZHANG);
    }
    public void addQueYiSe() {
        setVal(QUEYISE);
    }
    public void addBanBanHu() {
        setVal(BANBANHU);
    }
    public void addYiZhiHua() {
        setVal(YIZHIHUA);
    }
    public void addYiDianHong() {
    	setVal(YIDIANHONG);
    }
    public void addLiuLiuShun() {
        setVal(LIULIUSHUN);
    }
    public void addDaSiXi() {
        setVal(DASIXI);
    }
    public void addJingTongYuNu() {
        setVal(JINGTONGYUNU);
    }
    public void addJieJieGao() {
        setVal(JIEJIEGAO);
    }
    public void addSanTong() {
        setVal(SANTONG);
    }
    public void addBuZhangAn(){
        setVal(BUZHANG_AN);
    }
    public void addZhongTuLiuLiuShun() {
        setVal(ZHONGTULIULIUSHUN);
    }
    public void addZhongTuSiXi() {
        setVal(ZHONGTUSIXI);
    }
    public void addZiMo(){
        setVal(ZIMO);
    }

    public void setVal(int action){
        arr[action] = 1 ;
    }

    /**
     * 从可操作列表中提出第一个小胡
     * @param actList
     * @return
     */
    public static int getFirstXiaoHu(List<Integer> actList){
        for(int index : xiaoHuPriority){
            if(actList.get(index) == 1){
                return index;
            }
        }
        return -1;
    }

    /**
     * 是否有胡操作
     *
     * @param actionList
     * @return
     */
    public static boolean hasHu(List<Integer> actionList) {
        return hasAction(actionList, NxMjAction.HU) || hasAction(actionList,ZIMO);
    }
    
    
    /**
     * 是否有胡操作
     *
     * @param actionList
     * @return
     */
    public static boolean hasGang(List<Integer> actionList) {
        return hasAction(actionList, NxMjAction.MINGGANG);
    }
    
    
    
    /**
     * 是否有操作
     *
     * @param actionList
     * @param action     操作
     * @return
     */
    private static boolean hasAction(List<Integer> actionList, int action) {
        if (actionList != null && actionList.size() > action) {
            return actionList.get(action) == 1;
        }
        return false;
    }

    /**
     * 只保留胡操作
     *
     * @param actionList
     * @return
     */
    public static List<Integer> keepHu(List<Integer> actionList) {
        if (actionList == null || actionList.size() < HU + 1) {
            return Collections.EMPTY_LIST;
        }
        List<Integer> res = new ArrayList<>();
        for (int i = 0; i < actionList.size(); i++) {
            if (i == HU) {
                res.add(actionList.get(i));
            } else {
                res.add(0);
            }
        }
        return res;
    }

    /**
     * 是否是起手小胡
     * @param xiaoHu
     * @return
     */
    public static boolean isQiShouXiaoHu(int xiaoHu){
        if(xiaoHu == ZHONGTUSIXI || xiaoHu == ZHONGTULIULIUSHUN){
            return false;
        }
        return true;
    }

}
