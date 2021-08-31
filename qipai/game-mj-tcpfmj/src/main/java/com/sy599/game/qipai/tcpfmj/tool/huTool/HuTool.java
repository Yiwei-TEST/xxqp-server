package com.sy599.game.qipai.tcpfmj.tool.huTool;

import com.sy599.game.qipai.tcpfmj.bean.MjCardDisType;
import com.sy599.game.qipai.tcpfmj.constant.TcpfMj;
import com.sy599.game.qipai.tcpfmj.rule.TcpfMjingTang;
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
            list.add(TcpfMj.getMajang((id)).getVal());
        }
        return list;
    }

    public static List<Integer> mjsToVals(List<TcpfMj> mjs){
        List<Integer> list=new ArrayList<>(mjs.size());
        if(mjs==null)
            return list;
        for (TcpfMj mj:mjs) {
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

    public static boolean isTingHu(List<Integer> ids, int bossVal, int replaceVal,int buVal, List<MjCardDisType> cardTypes,int checkId){
        //全九幺
        if(isQuanJiuYao(ids,cardTypes,bossVal))
            return true;
        //十三烂
        if(isShiSanLang(ids,bossVal))
            return true;
        if(TcpfMj.getMajang(checkId).getVal()==buVal)
            return false;
        if(isXiaoHu(ids,bossVal,replaceVal,buVal))
            return true;
        return false;
    }


    public static boolean isHuNew(List<Integer> ids, int bossVal, int replaceVal, int buVal,List<MjCardDisType> cardTypes){
        //全九幺
         if(isQuanJiuYao(ids,cardTypes,bossVal))
            return true;
        //十三烂
        if(isShiSanLang(ids,bossVal))
            return true;
        if(isXiaoHu(ids,bossVal,replaceVal,buVal))
            return true;
        return false;
    }

    public static List<Integer> getHuType(List<Integer> ids,int bossVal,int replaceVal, int buVal,List<MjCardDisType> cardTypes){
        List<Integer> huType=new ArrayList<>();
        if(isQuanJiuYao(ids, cardTypes,bossVal)){
            //全九幺
            huType.add(TcpfMjingTang.MINGTANG_QUANJIUYAO);
        }else if(isShiSanLang(ids,bossVal)){
            //十三烂
            huType.add(TcpfMjingTang.MINGTANG_SHISANLANG);
        }else {
            checkXiaoHu(ids,bossVal,replaceVal,buVal,huType);
        }
        return huType;
    }

    public static boolean isXiaoHu(List<Integer> ids,int bossVal,int replaceVal,int buVal){
        List<Integer> copy=new ArrayList<>(ids);
        int bossNum = dropValByIds(copy, bossVal);
        int replaceNum = dropValByIds(copy, replaceVal);

        TcpfMj boss = TcpfMj.getMajiangByValue(bossVal);
        TcpfMj replace=null;
        if(replaceVal!=0)
            replace= TcpfMj.getMajiangByValue(replaceVal);
        for (int i = 0; i <= replaceNum; i++) {
            List<Integer> copy1=new ArrayList<>(copy);
            for (int j = 0; j < replaceNum; j++) {
                if(j<i){
                    copy1.add(boss.getId());
                }else if(replace!=null){
                    copy1.add(replace.getId());
                }
            }
            //先检测能不能胡七对
            if(isHu7dui(copy1,bossNum,buVal))
                return true;
            //再看是否能正常胡牌，抽出风牌单独考虑
            List<TcpfMj> fengPai = findFengPai(copy1);
            if(isHuContainFeng(fengPai,copy1,bossNum,buVal))
                return true;
        }
        return false;
    }

    public static void checkXiaoHu(List<Integer> ids,int bossVal,int replaceVal,int buVal,List<Integer> huType){
        List<Integer> copy=new ArrayList<>(ids);
        int bossNum = dropValByIds(copy, bossVal);
        int replaceNum = dropValByIds(copy, replaceVal);

        TcpfMj boss = TcpfMj.getMajiangByValue(bossVal);
        TcpfMj replace=null;
        if(replaceVal!=0)
            replace= TcpfMj.getMajiangByValue(replaceVal);
        for (int i = 0; i <= replaceNum; i++) {
            List<Integer> copy1=new ArrayList<>(copy);
            for (int j = 0; j < replaceNum; j++) {
                if(j<i){
                    if(boss==null)
                        System.out.println("有问题的牌："+ids);
                    copy.add(boss.getId());
                }else if(replace!=null){
                    copy.add(replace.getId());
                }

            }
            //先检测能不能胡七对
            if(isHu7dui(copy1,bossNum,buVal)){
                huType.add(TcpfMjingTang.MINGTANG_QXD);
                return;
            }

            //再看是否能正常胡牌，抽出风牌单独考虑
            List<TcpfMj> fengPai = findFengPai(copy1);
            if(isHuContainFeng(fengPai,copy1,bossNum,buVal)){

            }
            checkPPH(fengPai,copy1,bossNum,huType);
        }
    }

    public static void checkPPH(List<TcpfMj> mjs,List<Integer> ids,int bossNum,List<Integer> huType){
        if((mjs.size()+ids.size()+bossNum)%3!=2)
            return;
        Map<Integer,Integer> valAndNum=new HashMap<>();
        for (TcpfMj mj:mjs) {
            int val = mj.getVal();
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
        if(bossNum>needWang&&isHu(ids,bossNum-needWang,true)){
            if(isPengPengHu(ids,bossNum-needWang,true)){
                huType.add(TcpfMjingTang.MINGTANG_PPH);
                return;
            }

        }
        if(needWang>0&&isHu(ids,bossNum-needWang+1,false)){
            if(isPengPengHu(ids,bossNum-needWang+1,false)){
                huType.add(TcpfMjingTang.MINGTANG_PPH);
                return;
            }
        }
        huType.add(TcpfMjingTang.MINGTANG_PINGHU);
    }

    public static boolean isPengPengHu(List<Integer> handIds,int bossNum,boolean needJiang){
        Map<Integer,Integer> valAndNum=new HashMap<>();
        for (Integer id:handIds) {
            int val = TcpfMj.getMajang(id).getVal();
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

        if(needJiang){
            if(bossNum>needWang-1)
                return true;
        }else {
            if(bossNum>needWang)
                return true;
        }
        return false;
    }


    /**
     * 检查风牌是否可组成牌组
     * @param mjs
     * @return
     */
    public static boolean isHuContainFeng(List<TcpfMj> mjs,List<Integer> ids,int bossNum,int buVal){
        if((mjs.size()+ids.size()+bossNum)%3!=2)
            return false;
        Map<Integer,Integer> valAndNum=new HashMap<>();
        for (TcpfMj mj:mjs) {
            int val = mj.getVal();
            if(valAndNum.containsKey(val)){
                valAndNum.put(val,valAndNum.get(val)+1);
            }else {
                valAndNum.put(val,1);
            }
        }
        //正常胡牌不能有补花牌
        if(valAndNum.get(buVal)!=null)
            return false;
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
        if(needWang==0&&isHu(ids,bossNum,true))
            return true;
        else if(needWang>0){
            if(bossNum>=needWang&&isHu(ids,bossNum-needWang,true))
                return true;
            if(bossNum+1>=needWang&&isHu(ids,bossNum-needWang+1,false))
                return true;
        }
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


    public static int dropValByIds(List<Integer> ids,int val){
        int num=0;
        Iterator<Integer> it=ids.iterator();
        while (it.hasNext()){
            Integer next = it.next();
            if(TcpfMj.getMajang(next).getVal()==val){
                it.remove();
                num++;
            }
        }
        return num;
    }

    /**
     * 找到风牌的同时从ids中移除
     * @return
     */
    public static List<TcpfMj> findFengPai(List<Integer> ids){
        List<TcpfMj> fengMj=new ArrayList<>();
        Iterator<Integer> it=ids.iterator();
        while (it.hasNext()){
            TcpfMj mj = TcpfMj.getMajang(it.next());
            if(fengVal.contains(mj.getVal())){
                it.remove();
                fengMj.add(mj);
            }
        }
        return fengMj;
    }

    public static boolean isHu7dui(List<Integer> ids,int bossNum,int buVal){
        if(ids.size()+bossNum!=14)
            return false;
        Map<Integer,Integer> valAndNum=new HashMap<>();
        for (Integer id:ids) {
            int val = TcpfMj.getMajang(id).getVal();
            if(valAndNum.containsKey(val)){
                valAndNum.put(val,valAndNum.get(val)+1);
            }else {
                valAndNum.put(val,1);
            }
        }
        if(valAndNum.get(buVal)!=null)
            return false;
        for (Integer num:valAndNum.values()) {
            if(num%2!=0)
                bossNum--;
        }
        if(bossNum>=0)
            return true;
        return false;
    }

    /**
     * 全九幺
     * @param ids
     * @param cardTypes
     * @return
     */
    public static boolean isQuanJiuYao(List<Integer> ids, List<MjCardDisType> cardTypes,int bossVal){
        if(ids.size()%3!=2)
            return false;
        List<Integer> copy=new ArrayList<>(ids);
        dropValByIds(copy,bossVal);
        findFengPai(copy);
        for (Integer id:copy) {
            int yu=TcpfMj.getMajang(id).getVal()%10;
            if(yu!=1&&yu!=9)
                return false;
        }

        for (MjCardDisType type:cardTypes) {
            for (Integer id:type.getCardIds()) {
                int yu=TcpfMj.getMajang(id).getVal()%10;
                if(yu!=1&&yu!=9)
                    return false;
            }
        }
        return true;
    }

    /**
     * 十三烂
     * @param ids
     * @return
     */
    public static boolean isShiSanLang(List<Integer> ids,int bossVal){
        if(ids.size()!=14)
            return false;
        List<Integer> copy=new ArrayList<>(ids);
        dropValByIds(copy, bossVal);
        Map<Integer,Integer> valAanNum=new HashMap<>();
        for (Integer id:copy) {
            int val = TcpfMj.getMajang(id).getVal();
            if(valAanNum.containsKey(val))
                return false;
            else
                valAanNum.put(val,1);
        }
        findFengPai(copy);
        List<List<Integer>> classify = getClassify(copy);
        for (List<Integer> clas:classify) {
            if(clas.size()>3)
                return false;
            int x=0;
            for (Integer yu:clas) {
                if(x==0){
                    x=yu;
                    continue;
                }
                if(yu-x<=2)
                        return false;
                x=yu;
            }
        }
        return true;
    }


    public static List<List<Integer>> getClassify(List<Integer> ids){
        List<List<Integer>> classify=new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            classify.add(new ArrayList<>());
        }

        for (Integer id:ids) {
            int val=TcpfMj.getMajang(id).getVal();
            int chu=val/10;
            int yu=val%10;
            classify.get(chu-1).add(yu);
        }

        for (List<Integer> clas:classify) {
            Collections.sort(clas);
        }

        return classify;
    }

}
