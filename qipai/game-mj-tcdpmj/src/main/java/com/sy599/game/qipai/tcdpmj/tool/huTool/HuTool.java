package com.sy599.game.qipai.tcdpmj.tool.huTool;

import com.sy599.game.qipai.tcdpmj.bean.MjCardDisType;
import com.sy599.game.qipai.tcdpmj.constant.TcdpMj;
import com.sy599.game.qipai.tcdpmj.rule.TcdpMjingTang;
import com.sy599.game.util.TingResouce;

import java.util.*;

public class HuTool {

    private static List<Integer> val258= Arrays.asList(2, 5, 8);
    public static List<Integer> fengVal=Arrays.asList(301,311,321,331,201,211,221);
    /**
     * 去除给定的王牌
     * @param cardVals
     * @param bossVal
     * @return
     */
    public static int  dropBoss(List<Integer> cardVals,List<Integer> bossVal){
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

    public static List<Integer> idsToVals(List<Integer> ids){
        List<Integer> list=new ArrayList<>(ids.size());
        if(ids==null)
            return list;
        for (Integer id:ids) {
            list.add(TcdpMj.getMajang((id)).getVal());
        }
        return list;
    }

    public static List<Integer> mjsToVals(List<TcdpMj> mjs){
        List<Integer> list=new ArrayList<>(mjs.size());
        if(mjs==null)
            return list;
        for (TcdpMj mj:mjs) {
            list.add((mj).getVal());
        }
        return list;
    }

    public static int[] getValToInts(List<Integer> vals,int bossNum){
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


    public static boolean isHu(int[]yus){
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
    public static int isZu(int[] clas,int bossNum){
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


    public static boolean isHuNew(List<Integer> ids,int buVal){
        for (Integer id:ids) {
            if(TcdpMj.getMajang(id).getVal()==buVal)
                return false;
        }
        if(isXiaoHu(ids))
            return true;
        return false;
    }

    public static boolean isXiaoHu(List<Integer> ids){
        List<Integer> copy=new ArrayList<>(ids);
        //先检测能不能胡七对
        if(isHu7dui(copy))
            return true;
        //再看是否能正常胡牌，抽出风牌单独考虑
        List<TcdpMj> fengPai = findFengPai(copy);
        if(isHuContainFeng(fengPai,copy))
            return true;

        return false;
    }



    /**
     * 检查风牌是否可组成牌组
     * @param mjs
     * @return
     */
    public static boolean isHuContainFeng(List<TcdpMj> mjs, List<Integer> ids){
        if((mjs.size()+ids.size())%3!=2)
            return false;
        Map<Integer,Integer> valAndNum=new HashMap<>();
        for (TcdpMj mj:mjs) {
            int val = mj.getVal();
            if(valAndNum.containsKey(val)){
                valAndNum.put(val,valAndNum.get(val)+1);
            }else {
                valAndNum.put(val,1);
            }
        }
        int jiangNum=0;
        for (Map.Entry<Integer,Integer> entry:valAndNum.entrySet()) {
            switch (entry.getValue()){
                case 1:
                case 4:
                    return false;
                case 2:
                    jiangNum++;
                    break;
            }
        }
        if (jiangNum==0)
            return isHu(ids,0,true);
        else if (jiangNum==1)
            return isHu(ids,0,false);
        return false;
    }

    public static boolean isHu(List<Integer> ids,int  bossNum,boolean needJiang){
        if(needJiang&&(ids.size()+bossNum)%3!=2)
            return false;
        else if(!needJiang&&(ids.size()+bossNum)%3!=0)
            return false;
        List<Integer> vals = idsToVals(ids);
        int[] yus = getValToInts(vals, bossNum);
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
     * 找到风牌的同时从ids中移除
     * @return
     */
    public static List<TcdpMj> findFengPai(List<Integer> ids){
        List<TcdpMj> fengMj=new ArrayList<>();
        Iterator<Integer> it=ids.iterator();
        while (it.hasNext()){
            TcdpMj mj = TcdpMj.getMajang(it.next());
            if(fengVal.contains(mj.getVal())){
                it.remove();
                fengMj.add(mj);
            }
        }
        return fengMj;
    }

    public static boolean isHu7dui(List<Integer> ids){
        if(ids.size()!=14)
            return false;
        Map<Integer,Integer> valAndNum=new HashMap<>();
        for (Integer id:ids) {
            int val = TcdpMj.getMajang(id).getVal();
            if(valAndNum.containsKey(val)){
                valAndNum.put(val,valAndNum.get(val)+1);
            }else {
                valAndNum.put(val,1);
            }
        }
        for (Integer num:valAndNum.values()) {
            if(num%2!=0)
                return false;
        }
        return true;
    }


}
