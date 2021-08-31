package com.sy599.game.qipai.cqxzmj.rule;

import com.sy599.game.qipai.cqxzmj.bean.CqxzMjPlayer;
import com.sy599.game.qipai.cqxzmj.bean.CqxzMjTable;
import com.sy599.game.qipai.cqxzmj.bean.MjCardDisType;
import com.sy599.game.qipai.cqxzmj.bean.MjDisAction;
import com.sy599.game.qipai.cqxzmj.constant.CqxzMj;
import com.sy599.game.qipai.cqxzmj.tool.MjHelper;

import java.util.*;

public class CqxzMjMingTang {

    private static List<Integer> val258= Arrays.asList(2, 5, 8);
    private static List<Integer> val19= Arrays.asList(1,9);

    public static final int MINGTANG_PINGHU = 1;         //平胡         0番
    public static final int MINGTANG_DDH = 2;            //对对胡       1番
    public static final int MINGTANG_JIANGD = 3;         //将对         3番
    public static final int MINGTANG_QYS = 4;            //清一色       2番
    public static final int MINGTANG_QD = 5;             //七对         2番
    public static final int MINGTANG_LQD = 6;            //龙七对       3番
    public static final int MINGTANG_JQD = 7;            //将七对       4番
    public static final int MINGTANG_QJY = 8;            //全九幺       2番
    public static final int MINGTANG_ZZ = 9;             //中张         1番
    public static final int MINGTANG_MQ = 10;            //门清         1番
    public static final int MINGTANG_GSH = 11;           //杠上花       1番
    public static final int MINGTANG_GSP = 12;           //杠上炮       1番
    public static final int MINGTANG_QGH = 13;           //抢杠胡       1番
    public static final int MINGTANG_HDH = 14;           //海底胡       1番
    public static final int MINGTANG_HDP = 15;           //海底炮       1番
    public static final int MINGTANG_JGH = 16;           //金钩胡       1番
    public static final int MINGTANG_TH = 17;            //天胡         3番
    public static final int MINGTANG_DH = 18;            //地胡         2番

    public static final int MINGTANG_1G = 19;            //1根          1番
    public static final int MINGTANG_2G = 20;            //2根          2番
    public static final int MINGTANG_3G = 21;            //3根          3番
    public static final int MINGTANG_4G = 22;            //4根          4番

    public static final int MINGTANG_ZIMO = 23;          //自摸


    public static int getMingTangFen(List<Integer> mts,int celling,int ziMoType){
        if(mts==null||mts.size()==0)
            return 1;
        int fan=0;
        for (Integer mt:mts) {
            switch (mt){
                case MINGTANG_DDH:
                case MINGTANG_ZZ:
                case MINGTANG_MQ:
                case MINGTANG_GSH:
                case MINGTANG_GSP:
                case MINGTANG_QGH:
                case MINGTANG_HDH:
                case MINGTANG_HDP:
                case MINGTANG_JGH:
                case MINGTANG_1G:
                    fan++;
                    break;
                case MINGTANG_ZIMO:
                    if(ziMoType==1)
                        fan++;
                    break;
                case MINGTANG_QYS:
                case MINGTANG_QD:
                case MINGTANG_QJY:
                case MINGTANG_DH:
                case MINGTANG_2G:
                    fan+=2;
                    break;
                case MINGTANG_JIANGD:
                case MINGTANG_LQD:
                case MINGTANG_TH:
                case MINGTANG_3G:
                    fan+=3;
                    break;
                case MINGTANG_JQD:
                case MINGTANG_4G:
                    fan+=4;
                    break;
            }
        }
        celling=celling>0&&celling<5?celling:5;
        fan=fan>=celling?celling:fan;
        int fen=(int)Math.pow(2, fan);
        if(mts.contains(MINGTANG_ZIMO)&&ziMoType==0)
            fen++;
        return fen;
    }

    public static int getMingTangFan(List<Integer> mts){
        if(mts==null||mts.size()==0)
            return 0;
        int fan=0;
        for (Integer mt:mts) {
            switch (mt){
                case MINGTANG_DDH:
                case MINGTANG_ZZ:
                case MINGTANG_MQ:
                case MINGTANG_GSH:
                case MINGTANG_GSP:
                case MINGTANG_QGH:
                case MINGTANG_HDH:
                case MINGTANG_HDP:
                case MINGTANG_JGH:
                case MINGTANG_1G:
                    fan++;
                    break;
                case MINGTANG_QYS:
                case MINGTANG_QD:
                case MINGTANG_QJY:
                case MINGTANG_DH:
                case MINGTANG_2G:
                    fan+=2;
                    break;
                case MINGTANG_JIANGD:
                case MINGTANG_LQD:
                case MINGTANG_TH:
                case MINGTANG_3G:
                    fan+=3;
                    break;
                case MINGTANG_JQD:
                case MINGTANG_4G:
                    fan+=4;
                    break;
            }
        }
        return fan;
    }




    public static List<Integer> getHuType(CqxzMjTable table,CqxzMjPlayer player, List<CqxzMj> handCards,boolean selfMo,boolean chajiao){
        List<CqxzMj> mjs=new ArrayList<>(handCards);
        List<Integer> huTypes=new ArrayList<>();
        List<MjCardDisType> disCards = player.getCardTypes();
        if(isQingYiSe(disCards,mjs))
            huTypes.add(MINGTANG_QYS);

        if(table.getYjjd()==1&&isQJY(disCards,mjs))
            huTypes.add(MINGTANG_QJY);

        if(table.getMqzz()==1){
            if(isZhongZhang(disCards,mjs))
                huTypes.add(MINGTANG_ZZ);
            if(!chajiao&&isMenQing(disCards))
                huTypes.add(MINGTANG_MQ);
        }


        boolean qd=checkQXD(mjs,huTypes);
        if(!qd){
            checkPPH(mjs,disCards,huTypes,table.getYjjd()==1);
        }

        if(table.getTdh()==1){
            if(table.getDisNum()==0)
                huTypes.add(MINGTANG_TH);
            else if(table.getMoNum()==0&&table.getDisNum()==1)
                huTypes.add(MINGTANG_DH);
        }

        if(!chajiao){
            if(table.getGangSeat()!=0){
                if(selfMo){
                    huTypes.add(MINGTANG_GSH);
                }else {
                    if(table.getGangDisNum()==table.getDisNum())
                        huTypes.add(MINGTANG_QGH);
                    else
                        huTypes.add(MINGTANG_GSP);
                }
            }

            if(table.getLeftMajiangCount()==0){
                if(selfMo)
                    huTypes.add(MINGTANG_HDH);
                else
                    huTypes.add(MINGTANG_HDP);
            }
        }


        if(mjs.size()<=2)
            huTypes.add(MINGTANG_JGH);

        if(huTypes.size()==0)
            huTypes.add(MINGTANG_PINGHU);

        checkGeng(disCards,mjs,huTypes);
        return huTypes;
    }

    public static void checkGeng(List<MjCardDisType> disTypes,List<CqxzMj> handCards,List<Integer> huTypes){
        List<CqxzMj> mjs=new ArrayList<>(handCards);
        for (MjCardDisType type:disTypes) {
            mjs.addAll(MjHelper.toMajiang(type.getCardIds()));
        }

        Map<Integer,Integer> valAndNum=new HashMap<>();
        for (CqxzMj mj:mjs) {
            int val = mj.getVal();
            if(valAndNum.containsKey(val)){
                valAndNum.put(val,valAndNum.get(val)+1);
            }else {
                valAndNum.put(val,1);
            }
        }

        int gengNum=0;
        for (Map.Entry<Integer,Integer> entry:valAndNum.entrySet()) {
            if(entry.getValue()==4)
                gengNum++;
        }

        switch (gengNum){
            case 1:
                huTypes.add(MINGTANG_1G);
                break;
            case 2:
                huTypes.add(MINGTANG_2G);
                break;
            case 3:
                huTypes.add(MINGTANG_3G);
                break;
            case 4:
                huTypes.add(MINGTANG_4G);
                break;
        }
    }

    public static boolean isMenQing(List<MjCardDisType> disTypes){
        for (MjCardDisType type:disTypes) {
            if(type.getAction()!= MjDisAction.action_angang){
                return false;
            }
        }
        return true;
    }

    public static boolean isZhongZhang(List<MjCardDisType> disTypes, List<CqxzMj> mjs){
        for (CqxzMj mj:mjs) {
            int yu=mj.getVal()%10;
            if(val19.contains(yu))
                return false;
        }

        for (MjCardDisType type:disTypes) {
            for (Integer id:type.getCardIds()) {
                CqxzMj mj = CqxzMj.getMajang(id);
                int yu=mj.getVal()%10;
                if(val19.contains(yu))
                    return false;
            }
        }
        return true;
    }

    public static boolean checkPPH(List<CqxzMj> mjs, List<MjCardDisType> disCards, List<Integer> huTypes, boolean jangDui){
        Map<Integer,Integer> valAndNum=new HashMap<>();
        for (CqxzMj mj:mjs) {
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
        if(jiangNum==1){
            huTypes.add(MINGTANG_DDH);
            if(jangDui&&isJiangJiang(mjs,disCards))
                huTypes.add(MINGTANG_JIANGD);
            return true;
        }
        return false;
    }

    public static boolean isQingYiSe(List<MjCardDisType> disTypes, List<CqxzMj> mjs){
        int clas=0;
        for (CqxzMj mj:mjs) {
            int chu=mj.getVal()/10;
            if(clas==0){
                clas=chu;
            }else {
                if(chu!=clas)
                    return false;
            }
        }

        for (MjCardDisType type:disTypes) {
            for (Integer id:type.getCardIds()) {
                CqxzMj mj = CqxzMj.getMajang(id);
                int chu= mj.getVal()/10;
                if(chu!=clas)
                    return false;
            }
        }
        return true;
    }

    public static boolean checkQXD(List<CqxzMj> mjs, List<Integer> huTypes){
        if(mjs.size()!=14)
            return false;
        Map<Integer,Integer> valAndNum=new HashMap<>();
        for (CqxzMj mj:mjs) {
            int val = mj.getVal();
            if(valAndNum.containsKey(val)){
                valAndNum.put(val,valAndNum.get(val)+1);
            }else {
                valAndNum.put(val,1);
            }
        }

        boolean longqd=false;
        for (Map.Entry<Integer,Integer> entry:valAndNum.entrySet()) {
            if(entry.getValue()%2!=0)
                return false;
            if(entry.getValue()==4)
                longqd=true;
        }

        if(longqd){
            if(isJiangJiang(mjs,new ArrayList<>()))
                huTypes.add(MINGTANG_JQD);
            else
                huTypes.add(MINGTANG_LQD);
        }else
            huTypes.add(MINGTANG_QD);
        return true;
    }


    public static boolean isJiangJiang(List<CqxzMj> mjs ,List<MjCardDisType> disCards){
        for (CqxzMj mj:mjs) {
            int yu=mj.getVal()%10;
            if(!val258.contains(yu))
                return false;
        }

        for (MjCardDisType type:disCards) {
            for (Integer id:type.getCardIds()) {
                int yu=CqxzMj.getMajang(id).getVal()%10;
                if(!val258.contains(yu))
                    return false;
            }
        }
        return true;
    }


    /**
     * 顺子只需要包括19就行
     * @param disTypes
     * @param mjs
     * @return
     */
    public static boolean isQJY(List<MjCardDisType> disTypes, List<CqxzMj> mjs){
        for (MjCardDisType type:disTypes) {
            for (Integer id:type.getCardIds()) {
                CqxzMj mj = CqxzMj.getMajang(id);
                int yu=mj.getVal()%10;
                if(!val19.contains(yu))
                    return false;
            }
        }

        return isHu19(mjs);
    }

    public static boolean isHu19(List<CqxzMj> mjs){
        Map<Integer,Integer> valAndNum=new HashMap<>();
        for (CqxzMj mj:mjs) {
            int val = mj.getVal();
            if(valAndNum.containsKey(val)){
                valAndNum.put(val,valAndNum.get(val)+1);
            }else {
                valAndNum.put(val,1);
            }
        }

        for (Map.Entry<Integer,Integer> entry:valAndNum.entrySet()) {
            if(entry.getValue()>=2&&val19.contains(entry.getKey()%10)){
                Map<Integer,Integer> copy=new HashMap<>(valAndNum);
                copy.put(entry.getKey(),entry.getValue()-2);
                if(isHu19(copy))
                    return true;
            }
        }
        return false;
    }

    public static boolean isHu19(Map<Integer,Integer> valAndNum){
        List<Integer> code19 = CqxzMj.getCode19();
        for (int i = 1; i <= 3; i++) {
            int code = findCodeByClas(valAndNum, i);
            if(code!=0&&!code19.contains(code))
                return false;
        }
        return true;
    }

    private static int findCodeByClas(Map<Integer,Integer> valAndNum,int clas){
        int [] vals=new int[10];
        for (Map.Entry<Integer,Integer> entry:valAndNum.entrySet()) {
            if(entry.getKey()/10==clas){
                vals[entry.getKey()%10]+=entry.getValue();
            }
        }

        int code=0;
        for (int i = 1; i < vals.length; i++) {
            code=code*10+vals[i];
        }
        return code;
    }

}
