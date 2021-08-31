package com.sy599.game.qipai.cxmj.rule;

import com.sy599.game.qipai.cxmj.bean.CxMjCardDisType;
import com.sy599.game.qipai.cxmj.bean.CxMjDisAction;
import com.sy599.game.qipai.cxmj.bean.CxMjTable;
import com.sy599.game.qipai.cxmj.constant.CxMj;
import com.sy599.game.qipai.cxmj.tool.CxMjHelper;
import com.sy599.game.qipai.cxmj.tool.ting.TingTool;

import java.util.ArrayList;
import java.util.List;

public class MingTang {

    public static final int MINGTANG_ZIMO = 1;//自摸 0番
    public static final int MINGTANG_MENQING = 2;//门清 1番
    public static final int MINGTANG_PENGPENG = 3;//碰碰胡 1番
    public static final int MINGTANG_QINGYISE = 4;//清一色  1番
    public static final int MINGTANG_XIAOQIDUI = 5;//小七对 2番
    public static final int MINGTANG_LONGAOQIDUI = 6;//龙七对 3番
    public static final int MINGTANG_SLONGAOQIDUI = 7;//双龙七对 4番
    public static final int MINGTANG_GANGSHANGHUA = 8;//杠上花 1番
    public static final int MINGTANG_SGANGSHANGHUA = 9;//双杠上花 2番
    public static final int MINGTANG_WUMEIHUA = 10;//五梅花 2番
    public static final int MINGTANG_SGANGWUMEIHUA = 11;//双杠五梅花 3番

    public static List<Integer> get(List<CxMjCardDisType> disTypes, List<CxMj> handCards, CxMjTable table){
        List<Integer> mts=new ArrayList<>();
        checkQiDui(handCards,mts);
        if(mts.size()==0&&menQing(disTypes))
            mts.add(MINGTANG_MENQING);
        if(pengPengHu(disTypes,handCards))
            mts.add(MINGTANG_PENGPENG);
        if(qingYiSe(disTypes,handCards))
            mts.add(MINGTANG_QINGYISE);

        checkGang(disTypes,mts,handCards,table.getNian5());
        return mts;
    }

    public static boolean menQing(List<CxMjCardDisType> disTypes){
        if(disTypes==null||disTypes.size()==0)
            return true;
        for (CxMjCardDisType type:disTypes) {
            int action = type.getAction();
            if(action!=CxMjDisAction.action_angang)
                return false;
        }
        return true;
    }

    public static boolean pengPengHu(List<CxMjCardDisType> disTypes,List<CxMj> handCards){
        for (CxMjCardDisType type:disTypes) {
            int action = type.getAction();
            if(action==CxMjDisAction.action_chi)
                return false;
        }
        int[] intZu = getIntZu(handCards);
        int duiNum=0;
        for (int i = 0; i < intZu.length; i++) {
            int count=intZu[i];
            if(count==3||count==0){
                continue;
            }else if(count==2){
                duiNum++;
                if(duiNum>1)
                    return false;
            }else {
                return false;
            }
        }
        return true;
    }

    public static boolean qingYiSe(List<CxMjCardDisType> disTypes,List<CxMj> handCards){
        int clas=0;
        for (CxMj mj:handCards) {
            int chu=mj.getVal()/10;
            if(clas==0){
                clas=chu;
            }else {
                if(chu!=clas)
                    return false;
            }
        }

        for (CxMjCardDisType type:disTypes) {
            for (Integer id:type.getCardIds()) {
                int chu=CxMj.getMajang(id).getVal()/10;
                if(chu!=clas)
                    return false;
            }
        }
        return true;
    }

    public static void checkQiDui(List<CxMj> handCards,List<Integer> mts){
        if(handCards.size()!=14)
            return;
        int[] intZu = getIntZu(handCards);
        int count4=0;
        for (Integer count:intZu) {
            if (count == 2) {
                continue;
            } else if (count == 4) {
                count4++;
            } else if (count != 0) {
                return;
            }
        }
        if(count4>=2){
            mts.add(MINGTANG_SLONGAOQIDUI);
        }else if(count4==1){
            mts.add(MINGTANG_LONGAOQIDUI);
        }else {
            mts.add(MINGTANG_XIAOQIDUI);
        }
    }

    public static void checkGang(List<CxMjCardDisType> disTypes,List<Integer> mts,List<CxMj> handCards,int nian5){
        int gangNum=0;
        for (CxMjCardDisType type:disTypes) {
            if(type.getCardIds().size()==4)
                gangNum++;
        }

        if(gangNum>0){
            if(isMeiHua(handCards,nian5)){
                if(gangNum>=2){
                    mts.add(MINGTANG_SGANGWUMEIHUA);
                }else {
                    mts.add(MINGTANG_WUMEIHUA);
                }
            }else {
                if(gangNum>=2){
                    mts.add(MINGTANG_SGANGSHANGHUA);
                }else {
                    mts.add(MINGTANG_GANGSHANGHUA);
                }
            }
        }
    }

    public static boolean isMeiHua(List<CxMj> handCards,int nian5){
        CxMj huCard=handCards.get(handCards.size()-1);
        if(huCard.getVal()!=25)
            return false;
        if(nian5==1)
            return true;
        List<Integer> ids = CxMjHelper.toMajiangIds(handCards);
        if(ids.size()%3!=2)
            return false;
        List<Integer> vals = TingTool.idsToVals(ids);
        int[] yus = TingTool.getValToInts(vals, 0);
        return isHuWmh1(yus)||isHuWmh2(yus);
    }


    public static boolean isHuWmh1(int[] yus){
        if(yus[14]>=2){
            int[] copy=yus.clone();
            copy[14]-=2;
            return TingTool.isHu(copy);
        }
        return false;
    }

    public static boolean isHuWmh2(int[] yus){
        if(yus[13]>0&&yus[14]>0&&yus[15]>0){
            int[] copy=yus.clone();
            copy[13]--;
            copy[14]--;
            copy[15]--;
            for (int i = 0; i < copy.length; i++) {
                if(copy[i]>=2){
                    int[] clone = copy.clone();
                    clone[i]-=2;
                    if(TingTool.isHu(clone))
                        return true;
                }
            }
        }
        return false;
    }


    public static int[] getIntZu(List<CxMj> handCards){
        int [] count=new int[28];
        for (CxMj mj:handCards) {
            int val = mj.getVal();
            int yu=val%10;
            int chu=val/10;
            count[(chu-1)*9+yu]++;
        }
        return count;
    }




    public static int getMingTangFen(List<Integer> mts,int fen){
        if(mts==null||mts.size()==0)
            return fen;
        int fan=0;
        for (Integer mt:mts) {
            switch (mt){
                case MINGTANG_MENQING:
                    fan+=1;
                    break;
                case MINGTANG_PENGPENG:
                    fan+=1;
                    break;
                case MINGTANG_QINGYISE:
                    fan+=1;
                    break;
                case MINGTANG_XIAOQIDUI:
                    fan+=2;
                    break;
                case MINGTANG_LONGAOQIDUI:
                    fan+=3;
                    break;
                case MINGTANG_SLONGAOQIDUI:
                    fan+=4;
                    break;
                case MINGTANG_GANGSHANGHUA:
                    fan+=1;
                    break;
                case MINGTANG_SGANGSHANGHUA:
                    fan+=2;
                    break;
                case MINGTANG_WUMEIHUA:
                    fan+=2;
                    break;
                case MINGTANG_SGANGWUMEIHUA:
                    fan+=3;
                    break;
            }
        }
        if(fan> 4)
            fan=4;
        return fen*((int)Math.pow(2,fan));
    }

}
