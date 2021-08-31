package com.sy599.game.qipai.cxmj.tool.ting;

import com.sy599.game.qipai.cxmj.constant.CxMj;
import com.sy599.game.util.TingResouce;

import java.util.*;

public class TingTool {


    private static List<Integer> val258= Arrays.asList(2, 5, 8);

    /**
     * 按花色分类（不带风牌和红中）
     * @param cardVals
     * @return
     */
    public static List<int[]> classifyCard(List<Integer> cardVals){
        List<int[]> classfy=new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            int[] l=new int[10];
            classfy.add(l);
        }

        for (Integer val: cardVals) {
            int clas=val/10-1;
            int yu=val%10;
            int[] l = classfy.get(clas);
            l[yu]++;
        }
        return classfy;
    }

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
            list.add(CxMj.getMajang((id)).getVal());
        }
        return list;
    }

    public static List<Integer> mjsToVals(List<CxMj> ids){
        List<Integer> list=new ArrayList<>(ids.size());
        if(ids==null)
            return list;
        for (CxMj mj:ids) {
            list.add(mj.getVal());
        }
        return list;
    }



    public static int isZu(List<Integer> clas,int bossNum){
        int yu=clas.size()%3;
        if(yu!=0&&bossNum<3-yu)
            return -1;
        int useBoss=3-yu;
        for (int i = 0; i < bossNum / 3; i++) {
            if(bossNum>=useBoss+3){
                useBoss+=3;
            }else {
                break;
            }
        }

        int[] obj=new int[10];
        for (int i = 1; i < clas.size(); i++) {
            obj[clas.get(i)]++;
        }
        obj[0]=useBoss;

        int code=0;
        for (int i = 0; i < obj.length; i++) {
            code=code*10+obj[i];
        }

        int remainBossNum = TingResouce.getRemainBossNum(useBoss, code);
        if(remainBossNum==-1)
            return -1;
        return bossNum-useBoss+remainBossNum;
    }

    /**
     * 不带王胡牌
     * @param ids
     * @return
     */
    public static boolean isHu(List<Integer> ids){
        if(isHu7dui(ids,new ArrayList<>()))
            return true;
        if(isHu(ids,new ArrayList<>()))
            return true;
        return false;
    }


    public static boolean isHu7dui(List<Integer> ids,List<Integer> bossVals){
        if(ids.size()!=14)
            return false;
        List<Integer> vals = idsToVals(ids);
        int bossNum = dropBoss(vals, bossVals);
        int[] yus = getValToInts(vals, bossNum);
        for (int i = 1; i < yus.length; i++) {
            if(yus[i]%2!=0){
                yus[i]++;
                bossNum--;
            }
        }
        if(bossNum>=0)
            return true;
        return false;
    }


    /**
     * 去掉将牌，然后判断是否能胡牌
     * @param ids
     * @param bossVals
     * @return
     */
    public static boolean isHu(List<Integer> ids,List<Integer> bossVals){
        if(ids.size()%3!=2)
            return false;
        List<Integer> vals = idsToVals(ids);
        int bossNum = dropBoss(vals, bossVals);
        int[] yus = getValToInts(vals, bossNum);
        for (int i = 1; i < yus.length; i++) {
            //找到将牌
            int[] clone = yus.clone();
            if(clone[i]>=2){
                clone[i]-=2;
            }else {
                //不够则用王补
                int num=clone[i];
                if(clone[0]>=(2-num)){
                    clone[0]-=(2-num);
                    clone[i]=0;
                }else {
                    //王不够则跳过
                    continue;
                }
            }
            if(isHu(clone))
                return true;
        }
        return false;
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

    public static List<Integer> getTing(List<Integer> ids){
        List<Integer> tings=new ArrayList<>();
        if(ids.size()%3!=1)
            return tings;
        CxMj[] fullMj= CxMj.fullMj;
        for (int i = 0; i < fullMj.length; i++) {
            ids.add(fullMj[i].getId());
            if(isHu(ids)){
                tings.add(fullMj[i].getId());
            }
            ids.remove(ids.size()-1);
        }
        return tings;
    }

    public static Map<Integer, List<Integer>> getDaTing(List<Integer> ids){
        Map<Integer, List<Integer>> daTings=new HashMap<>();
        if(ids.size()%3!=2)
            return daTings;
        for (int i = 0; i < ids.size(); i++) {
            List<Integer> clone=new ArrayList<>(ids);
            clone.remove(i);
            List<Integer> ting = getTing(clone);
            if(ting.size()>0)
                daTings.put(ids.get(i),ting);
        }
        return daTings;
    }

    public static void main(String[] args) {
        int[] yus=new int[28];
        for (int i = 0; i < 28; i++) {
            yus[i]=i;
        }
        getClassify(yus);
    }


}
