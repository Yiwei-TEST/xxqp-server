package com.sy599.game.qipai.tdhmj.constant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TdhMjAction {

    public static final int ACTION_SIZE = 18;

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
   
    /*** 暗杠补张*/
    public static final int BUZHANG_AN = 16;
    /*** 自摸*/
    public static final int ZIMO = 17;

    /*** 自摸*/
    public static final int ZIMO_COUNT = 0 ;
    /*** 点炮*/
    public static final int DIANPAO_COUNT = 1;
    /**暗杠次数*/
    public static final int ANGANG_COUNT = 2;
    /*** 明杠次数*/
    public static final int MING_GANG_COUNT = 3;
    public static final int DAHU_COUNT = 4;
    public static final int XIAOHU_COUNT = 5;


    private int [] arr;

    public TdhMjAction(){
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
    public void addChi() {
        setVal(CHI);
    }
    public void addBuZhang() {
        setVal(BUZHANG);
    }
    public void addBuZhangAn(){
        setVal(BUZHANG_AN);
    }
    public void addZiMo(){
        setVal(ZIMO);
    }

    public void setVal(int action){
        arr[action] = 1 ;
    }


    /**
     * 是否有胡操作
     *
     * @param actionList
     * @return
     */
    public static boolean hasHu(List<Integer> actionList) {
        return hasAction(actionList, TdhMjAction.HU) || hasAction(actionList,ZIMO);
    }
    
    
    /**
     * 明杠还是暗杠
     *
     * @param actionList
     * @return
     */
    public static int hasGang(List<Integer> actionList) {
    	if(hasAction(actionList, TdhMjAction.MINGGANG)) {
    		return 1;
    	}else if(hasAction(actionList, TdhMjAction.ANGANG)){
    		return 2;
    	}
        return  0;
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

}
