package com.sy599.game.qipai.ahmj.tool.ting;

import com.sy599.game.qipai.ahmj.constant.Ahmj;
import com.sy599.game.qipai.ahmj.constant.AhmjConstants;
import com.sy599.game.qipai.ahmj.tool.AhmjHelper;
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

    /**
     * 去除给定的王牌
     * @param cardIds
     * @param bossVal
     * @return
     */
    public static int  dropBossById(List<Integer> cardIds,List<Integer> bossVal){
        int bossNum=0;
        if(bossVal!=null){
            Iterator<Integer> it = cardIds.iterator();
            while (it.hasNext()){
                Integer val = Ahmj.getMajang(it.next()).getVal();
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
            list.add(Ahmj.getMajang((id)).getVal());
        }
        return list;
    }

    public static List<Integer> mjsToVals(List<Ahmj> ids){
        List<Integer> list=new ArrayList<>(ids.size());
        if(ids==null)
            return list;
        for (Ahmj mj:ids) {
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
    public static boolean isHuAll(List<Integer> ids,List<Integer> bossVals,boolean nead258){
        if(isHu7dui(ids,bossVals))
            return true;
        if(isHu(ids,bossVals,nead258))
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
    public static boolean isHu(List<Integer> ids,List<Integer> bossVals,boolean nead258){
        if(ids.size()%3!=2)
            return false;
        List<Integer> vals = idsToVals(ids);
        int bossNum = dropBoss(vals, bossVals);
        int[] yus = getValToInts(vals, bossNum);
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

    /**
     * 清一色不需要258做将
     * @param allMj
     * @param bossVals
     * @return
     */
    public static boolean nead258(List<Ahmj> allMj,List<Integer> bossVals){
        int yu=-1;
        for (Ahmj mj:allMj){
            int val = mj.getVal();
            if(!bossVals.contains(val)){
                if(yu==-1){
                    yu=val/10;
                }else {
                    if(yu!=val/10)
                        return true;
                }
            }
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

    public static List<Integer> getTing(List<Integer> ids,List<Integer> bossVals,List<Ahmj> allMj){
        List<Integer> tings=new ArrayList<>();
        if(ids.size()%3!=1)
            return tings;
        Ahmj[] fullMj=AhmjConstants.fullMj;
        for (int i = 0; i < fullMj.length; i++) {
            ids.add(fullMj[i].getId());
            List<Ahmj> allCopy=new ArrayList<>(allMj);
            allCopy.addAll(AhmjHelper.toMajiang(ids));
            if(isHuAll(ids,bossVals,nead258(allCopy,bossVals))){
                tings.add(fullMj[i].getId());
            }
            ids.remove(ids.size()-1);
        }
        return tings;
    }

    public static Map<Integer, List<Integer>> getDaTing(List<Integer> ids,List<Integer> bossVals,List<Ahmj> withoutHand){
        Map<Integer, List<Integer>> daTings=new HashMap<>();
        if(ids.size()%3!=2)
            return daTings;
        for (int i = 0; i < ids.size(); i++) {
            List<Integer> clone=new ArrayList<>(ids);
            clone.remove(i);
            List<Ahmj> copyAll=new ArrayList<>(withoutHand);
            copyAll.addAll(AhmjHelper.toMajiang(clone));
            List<Integer> ting = getTing(clone, bossVals,copyAll);
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
