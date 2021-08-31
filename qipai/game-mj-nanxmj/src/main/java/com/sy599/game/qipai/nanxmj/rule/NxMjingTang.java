package com.sy599.game.qipai.nanxmj.rule;

import com.sy599.game.qipai.nanxmj.bean.MjCardDisType;
import com.sy599.game.qipai.nanxmj.bean.MjDisAction;
import com.sy599.game.qipai.nanxmj.bean.NanxMjPlayer;
import com.sy599.game.qipai.nanxmj.bean.NanxMjTable;
import com.sy599.game.qipai.nanxmj.constant.NxMj;
import com.sy599.game.qipai.nanxmj.tool.MjHelper;

import java.util.*;

public class NxMjingTang {

    private static List<Integer> val258= Arrays.asList(2, 5, 8);

    public static final int MINGTANG_MQ = 1;             //门清
    public static final int MINGTANG_PINGHU = 2;         //平胡
    public static final int MINGTANG_PPH = 3;            //碰碰胡
    public static final int MINGTANG_JJH = 4;            //将将胡
    public static final int MINGTANG_QYS = 5;            //清一色
    public static final int MINGTANG_0QXD = 6;           //七小对
    public static final int MINGTANG_1QXD = 7;           //豪华七小对
    public static final int MINGTANG_2QXD = 8;           //双豪华七小对
    public static final int MINGTANG_3QXD = 9;           //三豪华七小对
    public static final int MINGTANG_BT = 10;            //报听
    public static final int MINGTANG_QQR = 11;           //全球人

    public static final int MINGTANG_GSH = 13;           //杠上花
    public static final int MINGTANG_HDL = 14;           //海底捞
    public static final int MINGTANG_QGH = 15;           //抢杠胡
    public static final int MINGTANG_HDZN = 16;          //海底中鸟




    public static List<Integer> getHuType(List<NxMj> mjs, NanxMjPlayer player){
        List<Integer> huTypes=new ArrayList<>();
        List<MjCardDisType> disTypes = player.getCardTypes();
        List<Integer> list = MjHelper.toMajiangIds(mjs);
        if(isJiangJiangHu(disTypes,list))
            huTypes.add(MINGTANG_JJH);
        if(isQingYiSe(disTypes,list))
            huTypes.add(MINGTANG_QYS);
        if(isQuanQR(list))
            huTypes.add(MINGTANG_QQR);
        //检测七小对
        boolean haveQXD = checkQXD(list, huTypes);
        if(!haveQXD){
            if(isPengPengHu(list))
                huTypes.add(MINGTANG_PPH);
            if(huTypes.size()==0)
                huTypes.add(MINGTANG_PINGHU);
            if(isMenQing(disTypes))
                huTypes.add(MINGTANG_MQ);
        }
        if(player.getBaoTing()==1)
            huTypes.add(MINGTANG_BT);
        Collections.sort(huTypes,Collections.reverseOrder());
        return huTypes;
    }

    public static List<Integer> getGshHdl(NanxMjTable table,boolean self){
        List<Integer> huTypes=new ArrayList<>();
        if(table.getGangSeat()!=0){
            if(self)
                huTypes.add(MINGTANG_GSH);
            else
                huTypes.add(MINGTANG_QGH);
        }
        if(table.getLeftMajiangCount()<table.getMaxPlayerCount())
            huTypes.add(MINGTANG_HDL);
        return huTypes;
    }

    public static List<Integer> getDaHuType(List<Integer> ids, NanxMjPlayer player){
        List<Integer> huTypes=new ArrayList<>();
        List<MjCardDisType> disTypes = player.getCardTypes();
        List<Integer> copy = new ArrayList<>(ids);
        if(isJiangJiangHu(disTypes,copy))
            huTypes.add(MINGTANG_JJH);
        if(isQingYiSe(disTypes,copy))
            huTypes.add(MINGTANG_QYS);
        if(isQuanQR(copy))
            huTypes.add(MINGTANG_QQR);
        //检测七小对
        boolean haveQXD = checkQXD(copy, huTypes);
        if(!haveQXD){
            if(isPengPengHu(copy))
                huTypes.add(MINGTANG_PPH);
        }
        Collections.sort(huTypes,Collections.reverseOrder());
        return huTypes;
    }

    public static boolean isMenQing(List<MjCardDisType> disTypes){
        for (MjCardDisType type:disTypes) {
            if(type.getAction()!= MjDisAction.action_angang){
                return false;
            }
        }
        return true;
    }

    public static boolean isPengPengHu(List<Integer> handIds){
        Map<Integer,Integer> valAndNum=new HashMap<>();
        for (Integer id:handIds) {
            int val = NxMj.getMajang(id).getVal();
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
        if(jiangNum==1)
            return true;
        return false;
    }

    public static boolean isQingYiSe(List<MjCardDisType> disTypes, List<Integer> handIds){
        int clas=0;
        for (Integer id:handIds) {
            int chu=NxMj.getMajang(id).getVal()/10;
            if(clas==0){
                clas=chu;
            }else {
                if(chu!=clas)
                    return false;
            }
        }

        for (MjCardDisType type:disTypes) {
            for (Integer id:type.getCardIds()) {
                NxMj mj = NxMj.getMajang(id);
                int chu= mj.getVal()/10;
                if(chu!=clas)
                    return false;
            }
        }
        return true;
    }

    public static boolean isQuanQR(List<Integer> handIds){
        if(handIds.size()==2)
            return true;
        return false;
    }

    /**
     * @param handIds
     * @return
     */
    public static boolean isJiangJiangHu(List<MjCardDisType> disTypes,List<Integer> handIds){
        for (Integer id:handIds) {
            int yu=NxMj.getMajang(id).getVal()%10;
            if(!val258.contains(yu))
                return false;
        }
        for (MjCardDisType type:disTypes) {
            for (Integer id:type.getCardIds()) {
                int yu=NxMj.getMajang(id).getVal()%10;
                if(!val258.contains(yu))
                    return false;
            }
        }
        return true;
    }

    public static boolean checkQXD(List<Integer> ids, List<Integer> huTypes){
        if(ids.size()!=14)
            return false;
        Map<Integer,Integer> valAndNum=new HashMap<>();
        for (Integer id:ids) {
            int val = NxMj.getMajang(id).getVal();
            if(valAndNum.containsKey(val)){
                valAndNum.put(val,valAndNum.get(val)+1);
            }else {
                valAndNum.put(val,1);
            }
        }

        int size4Num=0;
        for (Map.Entry<Integer,Integer> entry:valAndNum.entrySet()) {
            if(entry.getValue()%2!=0)
                return false;
            if(entry.getValue()==4)
                size4Num++;
        }
        switch (size4Num){
            case 0:
                huTypes.add(MINGTANG_0QXD);
                break;
            case 1:
                huTypes.add(MINGTANG_1QXD);
                break;
            case 2:
                huTypes.add(MINGTANG_2QXD);
                break;
            case 3:
                huTypes.add(MINGTANG_3QXD);
                break;
        }
        return true;
    }


    /**
     * 杠上花和海底捞的额外分不在此方法中计算
     * @param mts
     * @return
     */
    public static int getMingTangFen1(List<Integer> mts){
        if(mts==null||mts.size()==0)
            return 1;
        int fan=0;
        int pow=0;
        if(mts.contains(MINGTANG_PINGHU))
            fan=2;
        else if(mts.contains(MINGTANG_MQ))
            fan=8;
        else
            fan=5;
        List<Integer> copy=new ArrayList<>(mts);
        if(copy.contains(MINGTANG_JJH)){
            copy.remove((Integer) MINGTANG_MQ);
        }
        for (Integer mt:copy) {
            switch (mt){
                case MINGTANG_PPH:
                    pow++;
                    break;
                case MINGTANG_JJH:
                    pow++;
                    break;
                case MINGTANG_QYS:
                    pow++;
                    break;
                case MINGTANG_BT:
                    pow++;
                    break;
                case MINGTANG_QQR:
                    pow++;
                    break;
                case MINGTANG_0QXD:
                    fan=8;
                    break;
                case MINGTANG_1QXD:
                    fan=16;
                    break;
                case MINGTANG_2QXD:
                    fan=32;
                    break;
                case MINGTANG_3QXD:
                    fan=64;
                    break;
            }
        }
        if(pow>=1){
            if(mts.contains(MINGTANG_0QXD)||mts.contains(MINGTANG_1QXD)||mts.contains(MINGTANG_2QXD)||mts.contains(MINGTANG_3QXD)){

            }else
                pow--;
        }

        int fen=fan*(int)Math.pow(2, pow);
        return fen;
    }

    public static int getMingTangFen2(List<Integer> mts,int mtFen,NanxMjTable table){
        if(mts==null||mts.size()==0)
            return mtFen;
        if(table.getGangHuAdd2()==1&&mts.contains(MINGTANG_GSH))
            mtFen+=2;
        if(table.getHaiDi2Fen()==1&&mts.contains(MINGTANG_HDL))
            mtFen+=2;
        return mtFen;
    }
}
