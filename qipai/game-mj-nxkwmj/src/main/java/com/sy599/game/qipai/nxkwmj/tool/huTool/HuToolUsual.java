package com.sy599.game.qipai.nxkwmj.tool.huTool;

import com.sy599.game.qipai.nxkwmj.bean.KwMjPlayer;
import com.sy599.game.qipai.nxkwmj.bean.KwMjTable;
import com.sy599.game.qipai.nxkwmj.bean.MjCardDisType;
import com.sy599.game.qipai.nxkwmj.bean.MjDisAction;
import com.sy599.game.qipai.nxkwmj.constant.KwMj;
import com.sy599.game.qipai.nxkwmj.rule.KwMingTang;
import com.sy599.game.qipai.nxkwmj.tool.MjHelper;
import com.sy599.game.util.TingResouce;

import java.util.*;

public class HuToolUsual {

//    private static List<Integer> val258= Arrays.asList(2, 5, 8);
//    private static List<Integer> allJVals= Arrays.asList(12,15,18,22,25,28,32,35,38);
//
//    /**
//     * 去除给定的王牌
//     * @param cardVals
//     * @param bossVal
//     * @return
//     */
//    public static int  dropBoss(List<Integer> cardVals,List<Integer> bossVal){
//        //默认加入一张王牌，将牌最后处理
//        int bossNum=0;
//        if(bossVal!=null){
//            Iterator<Integer> it = cardVals.iterator();
//            while (it.hasNext()){
//                Integer val = it.next();
//                if(bossVal.contains(val)){
//                    it.remove();
//                    bossNum++;
//                }
//            }
//        }
//        return bossNum;
//    }
//
//    public static List<Integer> idsToVals(List<Integer> ids){
//        List<Integer> list=new ArrayList<>(ids.size());
//        if(ids==null)
//            return list;
//        for (Integer id:ids) {
//            list.add(KwMj.getMajang((id)).getVal());
//        }
//        return list;
//    }
//
//    public static List<Integer> mjsToVals(List<KwMj> mjs){
//        List<Integer> list=new ArrayList<>(mjs.size());
//        if(mjs==null)
//            return list;
//        for (KwMj mj:mjs) {
//            list.add((mj).getVal());
//        }
//        return list;
//    }
//
//    public static int[] getValToInts(List<Integer> vals,int bossNum){
//        int [] yus=new int[28];
//        for (int i = 0; i < vals.size(); i++) {
//            Integer val = vals.get(i);
//            int yu=val%10;
//            int chu=val/10;
//            yus[(chu-1)*9+yu]++;
//        }
//        yus[0]=bossNum;
//        return yus;
//    }
//
//
//    public static boolean isHu(int[]yus){
//        List<int[]> classify = getClassify(yus);
//        int bossNum=yus[0];
//        for (int[] cals:classify) {
//            bossNum=isZu(cals,bossNum);
//            if(bossNum==-1)
//                return false;
//        }
//        return true;
//    }
//
//
//    /**
//     * 将已经按条筒万分类的数组转化为code查询是否组成牌组
//     * 牌数不够则用王补，王不够则返回-1,表示不能组成牌组
//     * @param clas
//     * @param bossNum
//     * @return
//     */
//    public static int isZu(int[] clas,int bossNum){
//        int size=0;
//        for (int num:clas) {
//            size+=num;
//        }
//        if(size==0)
//            return bossNum;
//        int yu=size%3;
//        if(yu!=0&&bossNum<3-yu)
//            return -1;
//        int useBoss=0;
//        if(yu!=0)
//            useBoss=3-yu;
//        for (int i = 0; i < bossNum / 3; i++) {
//            if(bossNum>=useBoss+3){
//                useBoss+=3;
//            }else {
//                break;
//            }
//        }
//
//        long code=useBoss;
//        for (int i = 0; i < clas.length; i++) {
//            code=code*10+clas[i];
//        }
//
//        int remainBossNum = TingResouce.getRemainBossNum(useBoss, code);
//        if(remainBossNum==-1)
//            return -1;
//        return bossNum-useBoss+remainBossNum;
//    }
//
//
//    /**
//     * 按条筒万分类返回
//     * @param yus
//     * @return
//     */
//    private static List<int[]> getClassify(int[]yus){
//        List<int[]> classfy=new ArrayList<>();
//        classfy.add(Arrays.copyOfRange(yus,1,10));
//        classfy.add(Arrays.copyOfRange(yus,10,19));
//        classfy.add(Arrays.copyOfRange(yus,19,28));
//        return classfy;
//    }
//
//    /**
//     * 去掉将牌，然后判断是否能胡牌
//     * @param ids
//     * @param bossVals
//     * @return
//     */
//    public static boolean isHu(List<Integer> ids,List<Integer> bossVals,boolean nead258){
//        if(ids.size()%3!=2)
//            return false;
//        List<Integer> vals = idsToVals(ids);
//        int bossNum = dropBoss(vals, bossVals);
//        int[] yus = getValToInts(vals, bossNum);
//        for (int i = 0; i < yus.length; i++) {
//            //找到将牌
//            if((nead258&&val258.contains(i%9))||!nead258){
//                int[] clone = yus.clone();
//                if(clone[i]>=2){
//                    clone[i]-=2;
//                }else {
//                    //不够则用王补
//                    int num=clone[i];
//                    clone[i]=0;
//                    if(clone[0]>=(2-num)){
//                        clone[0]-=(2-num);
//                    }else {
//                        //王不够则跳过
//                        continue;
//                    }
//                }
//                if(isHu(clone))
//                    return true;
//            }
//        }
//        return false;
//    }
//
//
//    /**
//     * 只判断能不能胡，当检测到可胡时，立即返回
//     * @param ids
//     * @param chunWang
//     * @return
//     */
//    public static boolean isHu(List<Integer> ids,List<Integer> chunWang,int zhengWang,List<MjCardDisType> disTypes,boolean baoTing){
//        if(isXiaoHu(ids,chunWang))
//            return true;
//        if(isDaHu(ids,chunWang,zhengWang,disTypes,baoTing))
//            return true;
//        return false;
//    }
//
//
//
//    /**
//     *
//     * @param ids
//     * @param bossVals
//     * @return
//     */
//    public static boolean isXiaoHu(List<Integer> ids,List<Integer> bossVals){
//        return isHu(ids,bossVals,true);
//    }
//
//    /**
//     * 只计算是否能胡，胡哪些大胡另算
//     * 部分大胡需要258做将，已在{@link HuToolUsual#isXiaoHu(List, List)}该方法中计算。
//     * @param ids
//     * @param chunWang
//     * @param zhengWang
//     * @return
//     */
//    public static boolean isDaHu(List<Integer> ids,List<Integer> chunWang,int zhengWang,List<MjCardDisType> disTypes,boolean baoTing){
//        //不需要成牌型的大胡
//        if(isHu7dui(ids,chunWang))
//            return true;
//        if(!baoTing){
//            if(isWangHu(ids,chunWang,zhengWang))
//                return true;
//            if(isJJHNoZu(disTypes,MjHelper.toMajiang(ids),chunWang))
//                return true;
//        }
//        //需要成牌型的大胡
//        if(isHu(ids,chunWang,false)){
//            //碰碰胡
//            if(isPengPengHu(disTypes,ids,chunWang))
//                return true;
//            //清一色
//            if(isQingYiSe(disTypes,ids,chunWang))
//                return true;
//        }
//        return false;
//    }
//
//    public static boolean isHu7dui(List<Integer> ids,List<Integer> bossVals){
//        if(ids.size()!=14)
//            return false;
//        List<Integer> vals = idsToVals(ids);
//        int bossNum = dropBoss(vals, bossVals);
//        int[] yus = getValToInts(vals, bossNum);
//        for (int i = 1; i < yus.length; i++) {
//            if(yus[i]%2!=0){
//                yus[i]++;
//                bossNum--;
//            }
//        }
//        if(bossNum>=0)
//            return true;
//        return false;
//    }
//
//    public static boolean isHu7duiByVal(List<Integer> vals,List<Integer> bossVals){
//        if(vals.size()!=14)
//            return false;
//        List<Integer> copy=new ArrayList<>(vals);
//        int bossNum = dropBoss(copy, bossVals);
//        int[] yus = getValToInts(copy, bossNum);
//        for (int i = 1; i < yus.length; i++) {
//            if(yus[i]%2!=0){
//                yus[i]++;
//                bossNum--;
//            }
//        }
//        if(bossNum>=0)
//            return true;
//        return false;
//    }
//
//    /**
//     * 黑天胡，四王，六王
//     * @param ids
//     * @param chunWang
//     * @param zhengWang
//     * @return
//     */
//    public static boolean isWangHu(List<Integer> ids,List<Integer> chunWang,int zhengWang){
//        if(ids.size()%3!=2)
//            return false;
//        List<Integer> vals = idsToVals(ids);
//        int chunNum = dropBoss(vals, chunWang);
//        if(chunNum>=4)
//            return true;
//        List<Integer> zWang=new ArrayList<>();
//        zWang.add(zhengWang);
//        int zhengNum = dropBoss(vals, zWang);
//        if(zhengNum>=3)
//            return true;
//        return false;
//    }
//
//    public static boolean isPengPengHu(List<MjCardDisType> disTypes, List<Integer> handIds,List<Integer> chunWang){
//        for (MjCardDisType type:disTypes) {
//            int action = type.getAction();
//            if(action== MjDisAction.action_chi)
//                return false;
//        }
//        int[] intZu = getIntZu(handIds,chunWang);
//        int needWang=0;
//        for (int i = 1; i < intZu.length; i++) {
//            int count=intZu[i];
//            if(count==3||count==0){
//                continue;
//            }else if(count==2){
//                needWang++;
//            }else {
//                needWang+=2;
//            }
//        }
//        if(needWang-1>intZu[0])
//            return false;
//        return true;
//    }
//
//    public static int[] getIntZu(List<Integer> handCards,List<Integer> chunWang){
//        int [] count=new int[28];
//        for (Integer id:handCards) {
//            int val = KwMj.getMajang(id).getVal();
//            if(chunWang.contains(val)){
//                count[0]++;
//            }else {
//                int yu=val%10;
//                int chu=val/10;
//                count[(chu-1)*9+yu]++;
//            }
//        }
//        return count;
//    }
//
//    public static boolean isQingYiSe(List<MjCardDisType> disTypes, List<Integer> handIds,List<Integer> chunWang){
//        int clas=0;
//        for (Integer id:handIds) {
//            KwMj mj = KwMj.getMajang(id);
//            if(chunWang.contains(mj.getVal()))
//                continue;
//            int chu=KwMj.getMajang(id).getVal()/10;
//            if(clas==0){
//                clas=chu;
//            }else {
//                if(chu!=clas)
//                    return false;
//            }
//        }
//
//        for (MjCardDisType type:disTypes) {
//            for (Integer id:type.getCardIds()) {
//                KwMj mj = KwMj.getMajang(id);
//                if(chunWang.contains(mj.getVal()))
//                    continue;
//                int chu= mj.getVal()/10;
//                if(chu!=clas)
//                    return false;
//            }
//        }
//        return true;
//    }
//
//    public static boolean isQingYiSeByVal(List<MjCardDisType> disTypes, List<Integer> vals,List<Integer> chunWang){
//        int clas=0;
//        for (Integer val:vals) {
//            if(chunWang.contains(val)||val==1000)
//                continue;
//            int chu=val/10;
//            if(clas==0){
//                clas=chu;
//            }else {
//                if(chu!=clas)
//                    return false;
//            }
//        }
//
//        for (MjCardDisType type:disTypes) {
//            for (Integer id:type.getCardIds()) {
//                KwMj mj = KwMj.getMajang(id);
//                if(chunWang.contains(mj.getVal()))
//                    continue;
//                int chu= mj.getVal()/10;
//                if(chu!=clas)
//                    return false;
//            }
//        }
//        return true;
//    }
//
//
//    /**
//     * 起手胡
//     * @param mjs
//     * @return
//     */
//    public static List<Integer> isQiShouHu(List<KwMj> mjs,KwMjTable table) {
//        List<Integer> hu=new ArrayList<>();
//        int zhengWang;
//        if(table.getZhengWang()!=0){
//            zhengWang=table.getZhengWang();
//        }else {
//            zhengWang=table.getChunWang().get(0);
//        }
//        if(isWWWJ(mjs,table))
//            hu.add(KwMingTang.MINGTANG_QSH_WWWJ);
//        if(isQueYS(mjs,zhengWang))
//            hu.add(KwMingTang.MINGTANG_QSH_QYS);
//        if(isYZH(mjs,zhengWang))
//            hu.add(KwMingTang.MINGTANG_QSH_YZH);
//        checkBoss(hu,mjs,table.getZhengWang(),table.getChunWang());
//        return hu;
//    }
//
//    /**
//     * 无王无将
//     * @param mjs
//     * @param table
//     * @return
//     */
//    private static boolean isWWWJ(List<KwMj> mjs,KwMjTable table){
//        List<Integer> vals = new ArrayList<>(table.getChunWang());
//        if(table.getZhengWang()!=0)
//            vals.add(table.getZhengWang());
//        for (KwMj mj:mjs){
//            int val=mj.getVal();
//            if(vals.contains(val))
//                return false;
//            int yu=val%10;
//            if(val258.contains(yu))
//                return false;
//        }
//        return true;
//    }
//
//    /**
//     * 缺一色
//     * @param mjs
//     * @param kingVal
//     * @return
//     */
//    private static boolean isQueYS(List<KwMj> mjs,int kingVal){
//        for (KwMj mj:mjs){
//            int chu=kingVal/10;
//            if(mj.getVal()/10==chu)
//                return false;
//        }
//        return true;
//    }
//
//    /**
//     * 一枝花
//     * @param mjs
//     * @param kingVal
//     * @return
//     */
//    private static boolean isYZH(List<KwMj> mjs,int kingVal){
//        int count=0;
//        for (KwMj mj:mjs){
//            if(mj.getVal()==kingVal){
//                count++;
//                continue;
//            }
//
//            int chu=kingVal/10;
//            if(mj.getVal()/10==chu)
//                return false;
//        }
//        if(count==1)
//            return true;
//        return false;
//    }
//
//
//    //-------------------------------------------------以下为找出所有胡牌牌型相关逻辑---------------------------------------------------------//
//
//    public static List<Integer> getHuType(List<Integer> ids, List<Integer> chunWang, KwMjPlayer player, KwMjTable table){
//        //先判断起手胡
//        List<Integer> qsType=new ArrayList<>();
//        if(table.getDisNum()==0)
//            qsType = isQiShouHu(MjHelper.toMajiang(ids), table);
//        List<Integer> huType=getDaHu(ids,chunWang,player,table);
//        for (Integer type:qsType) {
//            if(!huType.contains(type))
//                huType.add(type);
//        }
//        if(huType.size()==0&&isXiaoHu(ids,chunWang)){
//            if(table.getDisNum()==0){
//                huType.add(KwMingTang.MINGTANG_DH_TIANHU);
//            }else {
//                huType.add(KwMingTang.MINGTANG_PINGHU);
//            }
//        }
//        return huType;
//    }
//
//    /**
//     * 该方法当王牌变为某张牌后，不可再变为其他牌，因需求更改，已弃用
//     * @param ids
//     * @param chunWang
//     * @param player
//     * @param table
//     * @return
//     */
//    public static List<Integer> getDaHu(List<Integer> ids, List<Integer> chunWang, KwMjPlayer player, KwMjTable table){
//        List<Integer> huType;
//        //需要王代替目标牌型的大胡
//        List<ReplaceLack> daHu = getAllJJH(player.getCardTypes(), player.getHandMajiang(), chunWang);
//        find7dui(daHu,ids,chunWang);
//        findQYS(daHu,player.getCardTypes(),ids,chunWang);
//        if(isHu(ids,chunWang,false)){
//            List<Integer> vals = idsToVals(ids);
//            List<ReplaceLack> huList1 = getAllHu(vals, chunWang);
//            List<ReplaceLack> huList2 = getDaHu(huList1);
//            if(huList2.size()>0){
//                //由于getAllHu中只验算手牌是否为大胡，还需要分别验算出牌堆是否符合碰碰胡、将将胡、清一色
//                List<MjCardDisType> cardTypes = player.getCardTypes();
//                huList2=isPPHWithOut(huList2,cardTypes);
//                huList2=isQYSWithOut(huList2,cardTypes);
//                huList2=isJJHNeedZu(huList2,cardTypes);
//            }
//            daHu.addAll(huList2);
//        }
//        ReplaceLack maxHu = getMaxHu(daHu);
//
//        //不需要成牌型的大胡
//        huType = maxHu.getDaHu();
//        //王相关胡
//        checkBoss(huType,player.getHandMajiang(),table.getZhengWang(),chunWang);
//        return huType;
//    }
//
//
//
//    private static List<ReplaceLack> getAllJJH(List<MjCardDisType> cardTypes, List<KwMj> handMajiang,List<Integer> chunWang){
//        List<ReplaceLack> list=new ArrayList<>();
//        if(!isJJHNoZu(cardTypes,handMajiang,chunWang))
//            return list;
//        List<Integer> vals = mjsToVals(handMajiang);
//        int bossNum = dropBoss(vals, chunWang);
//        getJJH(list,vals,bossNum);
//        return list;
//    }
//
//    private static void getJJH(List<ReplaceLack> allJJH,List<Integer> vals,int bossNum){
//        List<int[]> allCombo=new ArrayList<>();
//        int []obj=new int[allJVals.size()];
//        createCombo2(allCombo,obj,bossNum,0);
//        for (int [] combo:allCombo) {
//            ReplacelValType rvt=new ReplacelValType(vals,bossNum);
//            List<Integer> copyV=new ArrayList<>(vals);
//            for (int k:combo) {
//                copyV.add(k);
//            }
//            rvt.setReplace(copyV);
//            ReplaceLack lack=new ReplaceLack();
//            lack.addReplaceType(rvt);
//            lack.getDaHu().add(KwMingTang.MINGTANG_DH_JJH);
//            allJJH.add(lack);
//        }
//    }
//
//    private static void createCombo2(List<int[]> allCombo,int []obj,int surplus,int cur) {
//        if(cur>=obj.length)
//            return;
//        if (surplus==0) {
//            allCombo.add(obj);
//            return;
//        }
//
//        //游标是否还可以增长
//        boolean curAdd=true;
//        if(cur+1==obj.length){
//            curAdd=false;
//            if(surplus>4)
//                return;
//        }
//
//        if(curAdd){
//            createCombo2(allCombo,obj.clone(),surplus,cur+1);
//        }
//
//        int[] clone = obj.clone();
//        clone[cur]++;
//        createCombo2(allCombo,clone,surplus-1,cur);
//    }
//
//    private static void find7dui(List<ReplaceLack> daHu,List<Integer> ids,List<Integer> chunWang){
//        if(!isHu7dui(ids,chunWang))
//            return;
//        boolean jjhAnd7dui=false;
//        for (ReplaceLack lack:daHu) {
//            //将将胡没有牌型，全都存在lack.getReplace()的Index0中
//            ReplacelValType replacelValType = lack.getReplace().get(0);
//            if(isHu7duiByVal(replacelValType.getReplace(),chunWang)){
//                lack.getDaHu().add(KwMingTang.MINGTANG_DH_QXD);
//                jjhAnd7dui=true;
//                //目前只有将将胡和七小对比较特殊，找出2者是否关联即可跳出循环，后续有其他牌型，则可能需要找出全部可能验证其他牌型
//                break;
//            }
//        }
//        if(!jjhAnd7dui){
//            get7dui(daHu,ids,chunWang);
//        }
//    }
//
//    /**
//     * 目前七对之后的验证牌型除了必须代替的，不关心王可代替任意牌，统一设定值为1000
//     * @param ids
//     * @param chunWang
//     */
//    private static void get7dui(List<ReplaceLack> daHu,List<Integer> ids,List<Integer> chunWang){
//        List<Integer> vals = idsToVals(ids);
//        int bossNum = dropBoss(vals, chunWang);
//        ReplacelValType rvt=new ReplacelValType(vals,bossNum);
//        Map<Integer,Integer> valAndNum=new HashMap<>();
//        for (Integer val:vals) {
//            if(valAndNum.containsKey(val)){
//                valAndNum.put(val,valAndNum.get(val)+1);
//            }else {
//                valAndNum.put(val,1);
//            }
//        }
//        List<Integer> vls=new ArrayList<>();
//        for (Map.Entry<Integer,Integer> entry:valAndNum.entrySet()) {
//            if(entry.getValue()%2!=0&&bossNum>0){
//                entry.setValue(entry.getValue()+1);
//                bossNum--;
//            }
//            for (int i = 0; i < entry.getValue(); i++) {
//                vls.add(entry.getKey());
//            }
//        }
//        for (int i = 0; i < bossNum; i++) {
//            vls.add(1000);
//        }
//        rvt.setReplace(vls);
//        ReplaceLack lack=new ReplaceLack();
//        lack.addReplaceType(rvt);
//        lack.getDaHu().add(KwMingTang.MINGTANG_DH_QXD);
//        daHu.add(lack);
//    }
//
//    private static void findQYS(List<ReplaceLack> allHu,List<MjCardDisType> disTypes, List<Integer> ids,List<Integer> chunWang){
//        if(!isQingYiSe(disTypes, ids,chunWang))
//            return;
//        boolean odd7dui=false;
//        for (ReplaceLack lack:allHu) {
//            //没有牌型，全都存在lack.getReplace()的Index0中
//            ReplacelValType replacelValType = lack.getReplace().get(0);
//            if(isQingYiSeByVal(disTypes,replacelValType.getReplace(),chunWang)){
//                lack.getDaHu().add(KwMingTang.MINGTANG_DH_QYS);
//                odd7dui=true;
//            }
//        }
//        if(!odd7dui){
//            ReplaceLack lack = new ReplaceLack();
//            lack.getDaHu().add(KwMingTang.MINGTANG_DH_QYS);
//            allHu.add(lack);
//        }
//    }
//
//    private static ReplaceLack getMaxHu(List<ReplaceLack> allHu){
//        ReplaceLack max=null;
//        for (ReplaceLack lack:allHu) {
//            if(max==null){
//                max=lack;
//            }else {
//                if(lack.getDaHu().size()>max.getDaHu().size()){
//                    max=lack;
//                }
//            }
//        }
//        if(max==null)
//            max=new ReplaceLack();
//        return max;
//    }
//
//    private static void checkBoss(List<Integer> huType,List<KwMj> mjs,int zhengWang,List<Integer> chunWang){
//        int chunNum1=getMjNum(mjs,chunWang.get(0));
//        int chunNum2=0;
//        if(chunWang.size()==2){
//            chunNum2=getMjNum(mjs,chunWang.get(1));
//        }
//        int zhengNum=getMjNum(mjs,zhengWang);
//        if(zhengNum==3&&!huType.contains(KwMingTang.MINGTANG_HTH))
//            huType.add(KwMingTang.MINGTANG_HTH);
//        if(chunNum1+chunNum2>=6){
//            switch (chunNum1+chunNum2){
//                case 6:
//                    huType.add(KwMingTang.MINGTANG_6W);
//                    break;
//                case 7:
//                    huType.add(KwMingTang.MINGTANG_7W);
//                    break;
//                case 8:
//                    huType.add(KwMingTang.MINGTANG_8W);
//                    break;
//            }
//        }else if(chunNum1==4||chunNum2==4)
//            huType.add(KwMingTang.MINGTANG_4W);
//        if(chunNum1+chunNum2==0&&!huType.contains(KwMingTang.MINGTANG_0W))
//            huType.add(KwMingTang.MINGTANG_0W);
//    }
//
//    private static int getMjNum(List<KwMj> mjs,int val){
//        int count=0;
//        for (KwMj mj:mjs) {
//            if(mj.getVal()==val)
//                count++;
//        }
//        return count;
//    }
//
//
//
//
//    //---------------------------------------------以下为找出所有王牌替代牌能组成最大牌型------------------------------------------------------//
//    public static List<ReplaceLack> getAllHu(List<Integer> vals, List<Integer> bossVal){
//        if(vals.size()%3!=2)
//            return new ArrayList<>();
//        int bossNum = dropBoss(vals, bossVal);
//        List<ReplaceLack> allHu=new ArrayList<>();
//        if(bossNum>=2){
//            //癞子超过两个，可以自己组成任意将
//            ReplacelValType rt=new ReplacelValType(new int[]{1000,1000},new ArrayList<>(),2);
//            ReplaceLack lack=new ReplaceLack();
//            lack.addReplaceType(rt);
//            spilitGetAll(vals,bossNum-2,allHu,lack);
//        }
//        for (Integer val:vals) {
//            List<Integer> copyV=new ArrayList<>(vals);
//            Iterator<Integer> it = copyV.iterator();
//            List<Integer> rmList=new ArrayList<>();
//            while (it.hasNext()){
//                if(rmList.size()==2)
//                    break;
//                if(it.next()==val){
//                    rmList.add(val);
//                    it.remove();
//                }
//            }
//            ReplacelValType rt=null;
//            ReplaceLack lack=new ReplaceLack();
//            int [] jiang=new int[]{val,val};
//            if(rmList.size()==2){
//                rt=new ReplacelValType(jiang,rmList,0);
//            }else if(rmList.size()+bossNum>=2){
//                rt=new ReplacelValType(jiang,rmList,1);
//                bossNum--;
//            }
//            if(rt!=null){
//                lack.addReplaceType(rt);
//                spilitGetAll(copyV,bossNum,allHu,lack);
//            }
//        }
//        return allHu;
//    }
//
//
//
//    public static void spilitGetAll(List<Integer> vals,int bossNum,List<ReplaceLack> allHu,ReplaceLack rl){
//        if(vals.size()==0)
//            return;
//        int val=vals.get(0);
//        List<int[]> paiZus = KwMj.getPaiZu(val);
//        for (int[] paiZu:paiZus) {
//            List<Integer> valsCopy = new ArrayList<>(vals);
//            ReplaceLack newLack = rl.clone();
//            int bossCopy;
//            int surplus = removeVals(valsCopy, paiZu, bossNum, newLack);
//            if (surplus==-1) {
//                continue;
//            }else {
//                bossCopy=surplus;
//            }
//            if (valsCopy.size()==0&&(bossCopy==3||bossCopy==6)){
//                //当最后剩3张王牌的时候需要组成所有的牌型
//                addAllReplaceBoss(newLack,allHu,bossCopy);
//            }else if (valsCopy.size() +bossCopy== 0) {
//                allHu.add(newLack);
//                continue;
//            } else {
//                spilitGetAll(valsCopy, bossCopy, allHu,newLack);
//            }
//        }
//
//    }
//
//    private static void addAllReplaceBoss(ReplaceLack lack,List<ReplaceLack> allHu,int bossNum) {
//        Map<Integer, List<int[]>> paiZuMap = KwMj.getPaiZuMap();
//        for (Map.Entry<Integer,List<int[]>> entry:paiZuMap.entrySet()) {
//            List<int[]> aCardGroup = entry.getValue();
//            for (int[] aGroup:aCardGroup) {
//                ReplaceLack clone = lack.clone();
//                clone.addReplaceType(new ReplacelValType(aGroup,new ArrayList<>(),3));
//                allHu.add(clone);
//                if(bossNum==6)
//                    addAllReplaceBoss(clone,allHu,3);
//            }
//        }
//    }
//
//    public static int removeVals(List<Integer> vals, int[] zus, int bossNum,ReplaceLack lack) {
//        List<Integer> copyVal=new ArrayList<>(vals);
//        List<Integer> rmList=new ArrayList<>();
//        for (Integer val : zus) {
//            Iterator<Integer> iterator = copyVal.iterator();
//            while (iterator.hasNext()){
//                Integer next = iterator.next();
//                if(next==val){
//                    iterator.remove();
//                    rmList.add(next);
//                    break;
//                }
//            }
//        }
//        if (rmList.size()+bossNum< zus.length) {
//            return -1;
//        }
//        ReplacelValType rt=new ReplacelValType(zus,rmList,3-rmList.size());
//        lack.addReplaceType(rt);
//        vals.clear();
//        vals.addAll(copyVal);
//        return bossNum-3+rmList.size();
//    }
//
//    /**
//     * 找出所有含，碰碰胡、将将胡、清一色的牌，由外部验证是否出牌堆依然符合该大胡
//     * @param allHu
//     * @return
//     */
//    public static List<ReplaceLack> getDaHu(List<ReplaceLack> allHu){
//        List<ReplaceLack> list=new ArrayList<>();
//        for (ReplaceLack lack:allHu) {
//            if(lack.countDaHu().size()>0)
//                list.add(lack);
//        }
//        return list;
//    }
//
//    //-----------------------------如果手牌可以组成碰碰胡、将将胡、清一色则验证出出去的牌是否符合相应牌型---------------------------------//
//
//    private static List<ReplaceLack> isPPHWithOut(List<ReplaceLack> allHu, List<MjCardDisType> cardTypes){
//        if(allHu.size()==0)
//            return allHu;
//        boolean flag=true;
//        for (MjCardDisType type:cardTypes) {
//            int action = type.getAction();
//            if(action==MjDisAction.action_chi)
//                flag=false;
//        }
//        if(!flag){
//            for (ReplaceLack lack:allHu) {
//                if(lack.getDaHu().contains(KwMingTang.MINGTANG_DH_PPH)){
//                    lack.getDaHu().remove((Integer)KwMingTang.MINGTANG_DH_PPH);
//                    if(lack.getDaHu().size()==0)
//                        lack.getDaHu().add(KwMingTang.MINGTANG_PINGHU);
//                }
//            }
//        }
//        return allHu;
//    }
//
//    private static List<ReplaceLack> isJJHNeedZu(List<ReplaceLack> allHu, List<MjCardDisType> cardTypes) {
//        if(allHu.size()==0)
//            return allHu;
//        boolean flag=true;
//        for (MjCardDisType type:cardTypes) {
//            for (Integer id:type.getCardIds()) {
//                int yu=KwMj.getMajang(id).getVal()%10;
//                if(yu!=2&&yu!=5&&yu!=8)
//                    flag=false;
//            }
//        }
//        if(!flag){
//            for (ReplaceLack lack:allHu) {
//                if(lack.getDaHu().contains(KwMingTang.MINGTANG_DH_JJH)){
//                    lack.getDaHu().remove((Integer)KwMingTang.MINGTANG_DH_JJH);
//                    if(lack.getDaHu().size()==0)
//                        lack.getDaHu().add(KwMingTang.MINGTANG_PINGHU);
//                }
//            }
//        }
//        return allHu;
//    }
//
//    private static boolean isJJHNoZu(List<MjCardDisType> cardTypes, List<KwMj> handMajiang,List<Integer> chunWang) {
//        for (KwMj mj:handMajiang) {
//            int val = mj.getVal()%10;
//            if(!chunWang.contains(mj.getVal())&&!val258.contains(val))
//                return false;
//        }
//        for (MjCardDisType type:cardTypes) {
//            for (Integer id:type.getCardIds()){
//                int val = KwMj.getMajang(id).getVal()%10;
//                if(!chunWang.contains(KwMj.getMajang(id).getVal())&&!val258.contains(val))
//                    return false;
//            }
//        }
//        return true;
//    }
//
//    private static List<ReplaceLack> isQYSWithOut(List<ReplaceLack> allHu, List<MjCardDisType> cardTypes) {
//        if(allHu.size()==0)
//            return allHu;
//        for (ReplaceLack lack:allHu) {
//            if(!lack.getDaHu().contains(KwMingTang.MINGTANG_DH_QYS))
//                continue;
//            boolean flag=true;
//            int chu=lack.getReplace().get(0).getReplace().get(0)/10;
//            for (MjCardDisType type:cardTypes){
//                for (Integer id:type.getCardIds()) {
//                    int val=KwMj.getMajang(id).getVal();
//                    if(val/10!=chu)
//                        flag=false;
//                }
//            }
//            if(!flag){
//                lack.getDaHu().remove((Integer)KwMingTang.MINGTANG_DH_QYS);
//                if(lack.getDaHu().size()==0)
//                    lack.getDaHu().add(KwMingTang.MINGTANG_PINGHU);
//            }
//        }
//        return allHu;
//    }
}
