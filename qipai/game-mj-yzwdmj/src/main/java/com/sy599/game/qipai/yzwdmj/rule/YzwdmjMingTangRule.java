package com.sy599.game.qipai.yzwdmj.rule;

import com.sy599.game.qipai.yzwdmj.bean.YzwdmjPlayer;
import com.sy599.game.qipai.yzwdmj.bean.YzwdmjTable;
import com.sy599.game.qipai.yzwdmj.tool.YzwdmjQipaiTool;
import com.sy599.game.qipai.yzwdmj.tool.YzwdmjTool;
import com.sy599.game.qipai.yzwdmj.tool.hulib.util.HuUtil;

import java.util.*;

public class YzwdmjMingTangRule {
    public static final int LOUDI_MINGTANG_ZIMO = 1;//自摸 2番
    public static final int LOUDI_MINGTANG_LIANGPIAN = 2;//两片 没有红中 2番
    public static final int LOUDI_MINGTANG_WAMGDIAO = 3;//王钓 2番
    public static final int LOUDI_MINGTANG_WAMGDIAOWANG = 4;//王钓王 4番
    public static final int LOUDI_MINGTANG_WAMGCHUANG = 5;//王闯 4番
    public static final int LOUDI_MINGTANG_WAMGCHUANGWANG = 6;//王闯王 8番
    public static final int LOUDI_MINGTANG_QINGYISE = 7;//清一色  4番
    public static final int LOUDI_MINGTANG_QIQIAODUI = 8;//七巧对  2番
    public static final int LOUDI_MINGTANG_LONGQIDUI = 9;//龙七巧  4番
    public static final int LOUDI_MINGTANG_QIANGGANGHU = 10;//抢杠胡  2番
    public static final int LOUDI_MINGTANG_PENGPENGHU = 11;//碰碰胡  2番
    public static final int LOUDI_MINGTANG_ZHUANGXIAN = 12;//庄闲  2番


    public static List<Integer> calcMingTang(YzwdmjTable table,YzwdmjPlayer player,List<Integer> mt){
        List<Yzwdmj> handCards = new ArrayList<>(player.getHandMajiang());
        if(mt.contains(LOUDI_MINGTANG_QIANGGANGHU)){
            handCards.add(table.getMoGang());
        }
        if(table.getQingYiSe2Bei()==1&&isQingYiSe(handCards,player))
            mt.add(LOUDI_MINGTANG_QINGYISE);
        List<Integer> wangAct = player.getWangAct();
        //手动胡王钓王闯
        if(wangAct!=null&&wangAct.contains(1)){
//            removeOtherWang(wangAct,mt);
            if(wangAct.get(6)==1){//王闯
                //碰碰胡
                checkPengPengWangChuang(table,handCards,mt);
                checkWangChuang(handCards,mt,table.getLastMo());
            }else {//王钓
                //七对
                if(checkQiQiaoDui(table,handCards,mt)==1)
                    checkPengPengWangDiao(table,handCards,mt);
                checkWangDiao(handCards,mt,true,table.getLastMo());
            }
        }else {
            //点胡系统找出最大胡牌方式
            if(table.getLianPian()==1&&isLiangPian(handCards)){
                mt.add(LOUDI_MINGTANG_LIANGPIAN);
                if(table.getPengPengHu()==1&&isPengPengHu(handCards,true))
                    mt.add(LOUDI_MINGTANG_PENGPENGHU);
                checkQiQiaoDui(table,handCards,mt);
            }else {
                mt=getMaxMt(table,handCards,mt);
            }

        }
        return mt;
    }

    public static List<Integer> getMaxMt(YzwdmjTable table,List<Yzwdmj> handCards,List<Integer> mts){
        List<Integer> copy=new ArrayList<>(mts);
        List<Integer> copy1=new ArrayList<>(mts);
        List<Integer> copy2=new ArrayList<>(mts);
        int bei=checkQiDuiWangDiao(table,handCards,copy1);
        if(bei>1)
            copy=copy1;
        if(chekcPengPengWangChuang(table,handCards,copy2)>bei){
            copy=copy2;
        }
        if(bei==1){
            //不是王闯王钓
            if(table.getPengPengHu()==1&&isPengPengHu(handCards,true))
                copy.add(LOUDI_MINGTANG_PENGPENGHU);
            checkQiQiaoDui(table,handCards,copy);
        }
        return copy;
    }


    public static int checkQiDuiWangDiao(YzwdmjTable table,List<Yzwdmj> handCards,List<Integer> mts){
        int bei=1;
        bei*=checkQiQiaoDui(table,handCards,mts);
        if(bei==1&&checkPengPengWangDiao(table,handCards,mts))
            bei*=2;
        bei*=checkWangDiao(handCards,mts,table.isHu7dui(),table.getLastMo());
        return bei;
    }

    public static int checkWangDiao(List<Yzwdmj> handCards,List<Integer> mts,boolean hu7dui,Integer moCard){
        List<Yzwdmj> cards=new ArrayList<>(handCards);
        Yzwdmj moC=null;
        if(moCard!=null&&moCard!=0)
            moC=Yzwdmj.getMajang(moCard);
        if(cards.contains(moC)){
            cards.remove(moC);
        }
        if(cards.size()%3!=1)
            return 1;
        int hzCount = YzwdmjTool.dropHongzhong(cards).size();
        int[] cardArr = HuUtil.toCardArray(cards);
        List<Yzwdmj> lackPaiList = YzwdmjTool.getLackList(cardArr, hzCount, hu7dui);
        if ((lackPaiList.size() == 1 && null == lackPaiList.get(0))||lackPaiList.size()>=26) {
            if (moCard != null && moCard >= 201) {
                mts.add(LOUDI_MINGTANG_WAMGDIAOWANG);
                return 4;
            } else {
                mts.add(LOUDI_MINGTANG_WAMGDIAO);
                return 2;
            }
        }
        return 1;
    }


    public static int chekcPengPengWangChuang(YzwdmjTable table,List<Yzwdmj> handCards,List<Integer> mts){
        int bei=1;
        if(checkPengPengWangChuang(table,handCards,mts))
            bei*=2;
        bei*=checkWangChuang(handCards,mts,table.getLastMo());
        return bei;
    }

    public static int checkWangChuang(List<Yzwdmj> handCards,List<Integer> mts,Integer moCard){
        List<Yzwdmj> cards=new ArrayList<>(handCards);
        Yzwdmj moC=null;
        if(moCard!=null&&moCard!=0)
            moC=Yzwdmj.getMajang(moCard);
        if(cards.contains(moC)){
            cards.remove(moC);
        }
        if(cards.size()%3!=1)
            return 1;
        int hzCount = YzwdmjTool.dropHongzhong(cards).size();
        if(hzCount<2)
            return 1;
        int[] cardArr = HuUtil.toCardArray(cards);
        List<Yzwdmj> lackPaiList = YzwdmjTool.getLackList(cardArr, hzCount, false);
        if ((lackPaiList.size() == 1 && null == lackPaiList.get(0))||lackPaiList.size()>=26) {
            if(isWangChuang(cards,hzCount)){
                if(moCard!=null&&moCard>=201){
                    mts.add(LOUDI_MINGTANG_WAMGCHUANGWANG);
                    return 8;
                }else {
                    mts.add(LOUDI_MINGTANG_WAMGCHUANG);
                    return 4;
                }
            }
        }
        return 1;
    }




    /**
     * 计算番数
     * @param mt
     * @return
     */
    public static int countFan(List<Integer> mt) {
        int fan=1;
        for (int i = 0; i < mt.size(); i++) {
            switch (mt.get(i)){
                case LOUDI_MINGTANG_ZIMO:
                case LOUDI_MINGTANG_LIANGPIAN:
                case LOUDI_MINGTANG_WAMGDIAO:
                case LOUDI_MINGTANG_QIANGGANGHU:
                case LOUDI_MINGTANG_PENGPENGHU:
                case LOUDI_MINGTANG_ZHUANGXIAN:
                case LOUDI_MINGTANG_QIQIAODUI:
                case LOUDI_MINGTANG_QINGYISE:
                    fan*=2;
                    break;
                case LOUDI_MINGTANG_WAMGDIAOWANG:
                case LOUDI_MINGTANG_WAMGCHUANG:
                case LOUDI_MINGTANG_LONGQIDUI:
                    fan*=4;
                    break;
                case LOUDI_MINGTANG_WAMGCHUANGWANG:
                    fan*=8;
                    break;
            }
        }

        return fan;
    }


    public static boolean isLiangPian(List<Yzwdmj> handCards){
        for (Yzwdmj card:handCards) {
            if(card.getVal()==201)
                return false;
        }
        return true;
    }


    public static int checkQiQiaoDui(YzwdmjTable table,List<Yzwdmj> handCards,List<Integer> mt){
        if(!table.isHu7dui()||table.getQiDui2Bei()!=1)
            return 1;
        if(handCards.size()!=14)
            return 1;
        List<Yzwdmj> copy1=new ArrayList<>(handCards);
        List<Yzwdmj> hongzhongList = YzwdmjTool.dropHongzhong(copy1);
        YzwdmjIndexArr card_index = new YzwdmjIndexArr();
        YzwdmjQipaiTool.getMax(card_index, copy1);
        boolean qidui = YzwdmjTool.check7duizi(copy1, card_index, hongzhongList.size());
        if (!qidui)
            return 1;
        int[] ints = HuUtil.toCardArray(copy1);
        for (int i = 0; i < ints.length; i++) {
            if(i!=31&&ints[i]==4){
                mt.add(LOUDI_MINGTANG_LONGQIDUI);
                return 4;
            }
        }
        if(!mt.contains(LOUDI_MINGTANG_LONGQIDUI))
            mt.add(LOUDI_MINGTANG_QIQIAODUI);
        return 2;
    }




    public static boolean isWangChuang(List<Yzwdmj> handCards,int hzCount){
        if(hzCount>=2){
            for (int i = 0; i < hzCount-2; i++) {
                handCards.add(Yzwdmj.getMajang(201+i));
            }
            return YzwdmjTool.isHu(handCards, false);
        }else {
            return false;
        }
    }

    public static boolean checkWang(List<Yzwdmj> cards,Integer moCard,List<Integer> mt,boolean hu7dui){
        List<Yzwdmj> handCards=new ArrayList<>(cards);
        Yzwdmj moC=null;
        if(moCard!=null&&moCard!=0)
            moC=Yzwdmj.getMajang(moCard);
        if(handCards.contains(moC)){
            handCards.remove(moC);
        }
        if(handCards.size()%3!=1)
            return false;
        int hzCount = YzwdmjTool.dropHongzhong(handCards).size();
        int[] cardArr = HuUtil.toCardArray(handCards);
        List<Yzwdmj> lackPaiList = YzwdmjTool.getLackList(cardArr, hzCount, hu7dui);
        if ((lackPaiList.size() == 1 && null == lackPaiList.get(0))||lackPaiList.size()>=26) {
            if(moCard!=null&&moCard==201){
                mt.add(LOUDI_MINGTANG_WAMGDIAOWANG);
            }else {
                mt.add(LOUDI_MINGTANG_WAMGDIAO);
            }
            if(isWangChuang(handCards,hzCount)){
                if(moCard!=null&&moCard==201){
                    mt.add(LOUDI_MINGTANG_WAMGCHUANGWANG);
                }else {
                    mt.add(LOUDI_MINGTANG_WAMGCHUANG);
                }
            }
            return true;
        }
        return false;
    }

    /**
     * 确认王闯的情况下，是否能碰碰胡
     * @param table
     * @param handCards
     * @param mt
     * @return
     */
    public static boolean checkPengPengWangChuang(YzwdmjTable table,List<Yzwdmj> handCards,List<Integer> mt){
        if(table.getPengPengHu()==0)
            return false;
        List<Yzwdmj>copy=new ArrayList<>(handCards.subList(0,handCards.size()-1));
        Iterator<Yzwdmj> it = copy.iterator();
        int num=0;
        while (it.hasNext()){
            if(it.next().getVal()==201){
                it.remove();
                num++;
            }
            if(num==2)
                break;
        }
        if(isPengPengHu(copy,true)){
            mt.add(LOUDI_MINGTANG_PENGPENGHU);
            return true;
        }
        return false;
    }

    /**
     * 确认王闯的情况下，是否能碰碰胡
     * @param table
     * @param handCards
     * @param mt
     * @return
     */
    public static boolean checkPengPengWangDiao(YzwdmjTable table,List<Yzwdmj> handCards,List<Integer> mt){
        if(table.getPengPengHu()==0)
            return false;
        List<Yzwdmj>copy=new ArrayList<>(handCards.subList(0,handCards.size()-1));
        Iterator<Yzwdmj> it = copy.iterator();
        int num=0;
        while (it.hasNext()){
            if(it.next().getVal()==201){
                it.remove();
                num++;
            }
            if(num==1)
                break;
        }
        if(isPengPengHu(copy,false)){
            mt.add(LOUDI_MINGTANG_PENGPENGHU);
            return true;
        }
        return false;
    }

    public static boolean isPengPengHu(List<Yzwdmj> mjs,boolean needJiang){
        Map<Integer,Integer> valAndNum=new HashMap<>();
        for (Yzwdmj mj:mjs) {
            int val = mj.getVal();
            if(valAndNum.containsKey(val)){
                valAndNum.put(val,valAndNum.get(val)+1);
            }else {
                valAndNum.put(val,1);
            }
        }
        int needWang=0;
        for (Integer val:valAndNum.keySet()) {
            Integer num = valAndNum.get(val);
            if(val==201||num==3)
                continue;
            if(num==2){
                if(needJiang){
                    needJiang=false;
                }else {
                    needWang++;
                }
            }else if((num==1||num==4)){
                if(needJiang){
                    needWang+=1;
                    needJiang=false;
                }else {
                    needWang+=2;
                }
            }
        }
        if(needJiang)
            needWang+=2;
        Integer bossNum = valAndNum.get(201);
        if(needWang==0||(bossNum!=null&&bossNum>=needWang))
            return true;
        return false;
    }

    public static boolean isQingYiSe(List<Yzwdmj> handCards,YzwdmjPlayer player){
        List<Yzwdmj> copy=new ArrayList<>(handCards);
        copy.addAll(player.getPeng());
        copy.addAll(player.getGang());
        int[] nums= new int[4];
        for (Yzwdmj card:copy) {
            int x = card.getVal() / 10;
            switch (x){
                case 1:
                    nums[0]++;
                    break;
                case 2:
                    nums[1]++;
                    break;
                case 3:
                    nums[2]++;
                    break;
                    default:
                        nums[3]++;
            }
        }
        for (int i = 0; i < nums.length-1; i++) {
            if((nums[i]+nums[3])==copy.size())
                return true;
        }
        return false;
    }

}
