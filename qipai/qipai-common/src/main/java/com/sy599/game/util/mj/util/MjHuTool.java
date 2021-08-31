package com.sy599.game.util.mj.util;
import com.sy599.game.util.TingResouce;

import java.util.*;

/**
 * 该类可以计算所有麻将是否能够组成牌型，包括风牌（风牌只能碰或者做将，若是十三幺
 * 等特殊情况，需要外层另外处理），不能计算最后王牌参与的名堂
 */
public class MjHuTool {

    private static List<Integer> val258= Arrays.asList(2, 5, 8);
    public static List<Integer> fengVal=Arrays.asList(301,311,321,331,201,211,221);
    /**
     * 去除给定的王牌
     * @param cardVals
     * @param bossVal
     * @return
     */
    private static int  dropBoss(List<Integer> cardVals,List<Integer> bossVal){
        //默认加入一张王牌，将牌最后处理
        int bossNum=0;
        if(bossVal!=null){
            Iterator<Integer> it = cardVals.iterator();
            while (it.hasNext()){
                Integer val = it.next();
                if(bossVal.contains(val)){
                    it.remove();
                    bossNum++;
                }
            }
        }
        return bossNum;
    }


    private static int[] getValToInts(List<Integer> vals,int bossNum){
        int [] yus=new int[28];
        for (int i = 0; i < vals.size(); i++) {
            Integer val = vals.get(i);
            int yu=val%10;
            int chu=val/10;
            yus[(chu-1)*9+yu]++;
        }
        yus[0]=bossNum;
        return yus;
    }


    private static boolean isHu(int[]yus){
        List<int[]> classify = getClassify(yus);
        int bossNum=yus[0];
        for (int[] cals:classify) {
            bossNum=isZu(cals,bossNum);
            if(bossNum==-1)
                return false;
        }
        return true;
    }


    /**
     * 将已经按条筒万分类的数组转化为code查询是否组成牌组
     * 牌数不够则用王补，王不够则返回-1,表示不能组成牌组
     * @param clas
     * @param bossNum
     * @return
     */
    private static int isZu(int[] clas,int bossNum){
        int size=0;
        for (int num:clas) {
            size+=num;
        }
        if(size==0)
            return bossNum;
        int yu=size%3;
        if(yu!=0&&bossNum<3-yu)
            return -1;
        int useBoss=0;
        if(yu!=0)
            useBoss=3-yu;
        for (int i = 0; i < bossNum / 3; i++) {
            if(bossNum>=useBoss+3){
                useBoss+=3;
            }else {
                break;
            }
        }

        long code=useBoss;
        for (int i = 0; i < clas.length; i++) {
            code=code*10+clas[i];
        }

        int remainBossNum = TingResouce.getRemainBossNum(useBoss, code);
        if(remainBossNum==-1)
            return -1;
        return bossNum-useBoss+remainBossNum;
    }


    /**
     * 按条筒万分类返回
     * @param yus
     * @return
     */
    private static List<int[]> getClassify(int[]yus){
        List<int[]> classfy=new ArrayList<>();
        classfy.add(Arrays.copyOfRange(yus,1,10));
        classfy.add(Arrays.copyOfRange(yus,10,19));
        classfy.add(Arrays.copyOfRange(yus,19,28));
        return classfy;
    }


    /*--------------------------------------------------------------------------------------*/


    /**
     * 包括风牌是否可胡牌
     * @param vals        检测对象值
     * @param bossNumval  癞子牌的值
     * @param need258     是否需要258做将
     * @return
     */
    public static boolean isHuAll(List<Integer> vals,List<Integer> bossNumval,boolean need258){
        if(vals.size()%3!=2)
            return false;
        List<Integer> copy=new ArrayList<>(vals);
        int bossNum = dropBoss(copy, bossNumval);
        List<Integer> fengVals = findFengPai(copy);
        Map<Integer,Integer> valAndNum=new HashMap<>();
        for (Integer val:fengVals) {
            if(valAndNum.containsKey(val)){
                valAndNum.put(val,valAndNum.get(val)+1);
            }else {
                valAndNum.put(val,1);
            }
        }
        int needWang=0;
        for (Map.Entry<Integer,Integer> entry:valAndNum.entrySet()) {
            switch (entry.getValue()){
                case 1:
                case 4:
                    needWang+=2;
                    break;
                case 2:
                    needWang++;
                    break;
            }
        }
        if(need258){
            return isHuNeed258(copy,bossNum-needWang,need258);
        }else {
            if(needWang==0&&isHu(copy,bossNum,true))
                return true;
            else if(needWang>0){
                if(bossNum>=needWang&&isHu(copy,bossNum-needWang,true))
                    return true;
                if(bossNum+1>=needWang&&isHu(copy,bossNum-needWang+1,false))
                    return true;
            }
        }
        return false;
    }


    /**
     * 找到风牌的同时从ids中移除
     * @return
     */
    private static List<Integer> findFengPai(List<Integer> vals){
        List<Integer> fengVals=new ArrayList<>();
        Iterator<Integer> it=vals.iterator();
        while (it.hasNext()){
            Integer val = it.next();
            if(fengVal.contains(val)){
                it.remove();
                fengVals.add(val);
            }
        }
        return fengVals;
    }

    public static boolean isHu(List<Integer> normolVals,int  bossNum,boolean needJiang){
        if(needJiang&&(normolVals.size()+bossNum)%3!=2)
            return false;
        else if(!needJiang&&(normolVals.size()+bossNum)%3!=0)
            return false;
        if(bossNum<0)
            return false;
        int[] yus = getValToInts(normolVals, bossNum);
        if(needJiang){
            for (int i = 0; i < yus.length; i++) {
                //找到将牌
                int[] clone = yus.clone();
                if(clone[i]>=2){
                    clone[i]-=2;
                }else {
                    //不够则用王补
                    int num=clone[i];
                    clone[i]=0;
                    if(clone[0]>=(2-num)){
                        clone[0]-=(2-num);
                    }else {
                        //王不够则跳过
                        continue;
                    }
                }
                if(isHu(clone))
                    return true;
            }
        }else {
            if(isHu(yus.clone()))
                return true;
        }
        return false;
    }

    /**
     * 是否需要258做将
     * @param normolVal
     * @param bossNum
     * @param nead258
     * @return
     */
    public static boolean isHuNeed258(List<Integer> normolVal,int bossNum,boolean nead258){
        if((normolVal.size()+bossNum)%3!=2||bossNum<0)
            return false;
        int[] yus = getValToInts(normolVal, bossNum);
        for (int i = 0; i < yus.length; i++) {
            //找到将牌
            if((nead258&&val258.contains(i%9))||!nead258){
                int[] clone = yus.clone();
                if(clone[i]>=2){
                    clone[i]-=2;
                }else {
                    //不够则用王补
                    int num=clone[i];
                    clone[i]=0;
                    if(clone[0]>=(2-num)){
                        clone[0]-=(2-num);
                    }else {
                        //王不够则跳过
                        continue;
                    }
                }
                if(isHu(clone))
                    return true;
            }
        }
        return false;
    }

    public static boolean isHuAnd7dui(List<Integer> vals,List<Integer> bossNumval,boolean need258){
        if(isHu7dui(vals,bossNumval))
            return true;
        if(isHuAll(vals,bossNumval,need258))
            return true;
        return false;
    }

    public static boolean isHu7dui(List<Integer> vals,List<Integer> bossNumval){
        if(vals.size()!=14)
            return false;
        List<Integer> copy=new ArrayList<>(vals);
        int bossNum = dropBoss(copy, bossNumval);
        Map<Integer,Integer> valAndNum=new HashMap<>();
        for (Integer val:copy) {
            if(valAndNum.containsKey(val)){
                valAndNum.put(val,valAndNum.get(val)+1);
            }else {
                valAndNum.put(val,1);
            }
        }
        for (Integer num:valAndNum.values()) {
            if(num%2!=0)
                bossNum--;
        }
        if(bossNum>=0)
            return true;
        return false;
    }

}
