package com.sy599.game.qipai.nxkwmj.rule;

import com.sy599.game.qipai.nxkwmj.bean.KwMjPlayer;
import com.sy599.game.qipai.nxkwmj.bean.KwMjTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KwMingTang {

    public static final int MINGTANG_PINGHU = 1;         //平胡 小胡
    public static final int MINGTANG_QSH_WWWJ = 2;       //起手胡 无王无将
    public static final int MINGTANG_QSH_QYS = 3;        //起手胡 缺一色
    public static final int MINGTANG_QSH_YZH = 4;        //起手胡 一枝花
    public static final int MINGTANG_DH_TIANHU = 5;      //大胡 天胡
    public static final int MINGTANG_DH_PPH = 6;         //大胡 碰碰胡
    public static final int MINGTANG_DH_JJH = 7;         //大胡 将将胡
    public static final int MINGTANG_DH_QXD = 8;         //大胡 七小对
    public static final int MINGTANG_DH_QYS = 9;         //大胡 清一色
    public static final int MINGTANG_DH_HDLY = 10;       //大胡 海底捞月
    public static final int MINGTANG_DH_HDP = 11;        //大胡 海底炮
    public static final int MINGTANG_DH_GSH = 12;        //大胡 杠上花
    public static final int MINGTANG_DH_QGH = 13;        //大胡 抢杠胡
    public static final int MINGTANG_DH_GSP = 14;        //大胡 杠上炮
    public static final int MINGTANG_DH_BT = 15;         //大胡 报听
    public static final int MINGTANG_0W = 16;            //无王
    public static final int MINGTANG_4W = 17;            //四王
    public static final int MINGTANG_6W = 18;            //六王
    public static final int MINGTANG_7W = 19;            //七王  2番
    public static final int MINGTANG_8W = 20;            //八王  3番
    public static final int MINGTANG_HTH = 21;           //黑天胡
    public static final int MINGTANG_DH_LQXD = 22;       //大胡 龙七小对 2番



    public static void addOddHuType(KwMjTable table, KwMjPlayer player,boolean zimo){
        Map<Integer, List<Integer>> idMingTang = player.getIdMingTang();
        if(idMingTang.isEmpty())
            return;
        for (Map.Entry<Integer,List<Integer>> entry:idMingTang.entrySet()){
            List<Integer> huType = entry.getValue();
            if(huType.size()==0)
                continue;
            //检查海底
            checkHaiDi(table,player,huType);
            //检查杠
            checkGangHu(table,player,huType);
            //报听
            if(player.getBaoTing()==1)
                huType.add(MINGTANG_DH_BT);
        }

        for (List<Integer> mts:idMingTang.values()){
            boolean dahu=false;
            for (Integer mt:mts){
                if(mt!=MINGTANG_PINGHU)
                    dahu=true;
            }
            if(dahu&&mts.contains(MINGTANG_PINGHU))
                mts.remove((Integer) MINGTANG_PINGHU);
        }
    }

    public static void checkHaiDi(KwMjTable table, KwMjPlayer player, List<Integer> huType){
        if(table.getLeftMajiangCount()!=0)
            return;
        if(table.getAskSeat()==player.getSeat()){
            huType.add(MINGTANG_DH_HDLY);
        }else {
            huType.add(MINGTANG_DH_HDP);
        }
    }

    public static void checkGangHu(KwMjTable table, KwMjPlayer player , List<Integer> huType){
        if(table.getBuIds().size()==0)
            return;
        int gangSeat = table.getGangSeat();
        if(gangSeat==0)
            return;
        if(gangSeat==player.getSeat()){
            huType.add(MINGTANG_DH_GSH);
        }else {
            if(table.getBuIds().size()==0)
                huType.add(MINGTANG_DH_QGH);
            else
                huType.add(MINGTANG_DH_GSP);
        }

    }



    public static int  getMingTangFen(Map<Integer,List<Integer>> idMingTang,boolean celing){
        if(idMingTang==null||idMingTang.size()==0)
            return 0;
        int fen=0;
        for (List<Integer> huType:idMingTang.values()) {
            int bei=0;
            if(huType.contains(MINGTANG_PINGHU)){
                fen=2;
            }else {
                for (Integer mt:huType) {
                    if(mt==MINGTANG_7W||mt==MINGTANG_DH_LQXD){
                        bei+=2;
                    }else if(mt==MINGTANG_8W){
                        bei+=3;
                    }else {
                        bei+=1;
                    }
                }
                if(celing&&bei>3)
                    bei=3;
                fen=bei*7;
            }
        }
        return fen;
    }

    public static int  getMingTangFen(List<Integer> huType,boolean celing){
        if(huType==null||huType.size()==0)
            return 0;
        int fen=0;
        int bei=0;
        if(huType.contains(MINGTANG_PINGHU)){
            fen=2;
        }else {
            for (Integer mt:huType) {
                if(mt==MINGTANG_7W||mt==MINGTANG_DH_LQXD){
                    bei+=2;
                }else if(mt==MINGTANG_8W){
                    bei+=3;
                }else {
                    bei+=1;
                }
            }
            if(celing&&bei>3)
                bei=3;
            fen=bei*7;
        }
        return fen;
    }

    public static void removeLess(Map<Integer,List<Integer>> idMingTang,KwMjTable table){
        if(idMingTang.size()<2)
            return;
        Integer maxKey=0;
        int maxFen=0;
        for (Map.Entry<Integer,List<Integer>> entry:idMingTang.entrySet()){
            int bei=0;
            int fen=0;
            if(entry.getValue().contains(MINGTANG_PINGHU)){
                fen=2;
            }else {
                for (Integer mt:entry.getValue()) {
                    if(mt==MINGTANG_7W){
                        bei+=2;
                    }else if(mt==MINGTANG_8W){
                        bei+=3;
                    }else {
                        bei+=1;
                    }
                }
                fen=bei*7;
            }
            if(maxFen<fen){
                maxFen=fen;
                maxKey=entry.getKey();
            }

        }
        table.setLastId(maxKey);
        List<Integer> removeKey=new ArrayList<>();
        for (Map.Entry<Integer,List<Integer>> entry:idMingTang.entrySet()){
            if(entry.getKey()!=maxKey)
                removeKey.add(entry.getKey());
        }
        for (Integer key:removeKey) {
            idMingTang.remove(key);
        }
    }

}
