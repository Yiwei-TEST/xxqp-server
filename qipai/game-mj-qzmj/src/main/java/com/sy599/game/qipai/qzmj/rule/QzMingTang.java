package com.sy599.game.qipai.qzmj.rule;


import com.sy599.game.qipai.qzmj.bean.QzMjPlayer;
import com.sy599.game.qipai.qzmj.bean.QzMjTable;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class QzMingTang {

    public static final int MINGTANG_PINGHU = 1;         //平胡 小胡
    public static final int MINGTANG_DH_TIANHU = 2;      //大胡 天胡
    public static final int MINGTANG_DH_DIHU = 3;      //大胡 地胡
    public static final int MINGTANG_DH_PPH = 4;         //大胡 碰碰胡
    public static final int MINGTANG_DH_QXD = 5;         //大胡 七小对
    public static final int MINGTANG_DH_QYS = 6;         //大胡 清一色
    public static final int MINGTANG_DH_HYS = 7;         //大胡 混一色
    public static final int MINGTANG_DH_ZYS = 8;         //大胡 字一色
    public static final int MINGTANG_DH_DSY = 9;         //大胡 大三元
    public static final int MINGTANG_DH_LP = 10;         //烂牌
    public static final int MINGTANG_DH_QXGW = 11;         //七星归位
    public static final int MINGTANG_DH_ZY = 12;         //抓鱼
    public static final int MINGTANG_DH_GSH = 13;        //大胡 杠上花
    public static final int MINGTANG_DH_QGH = 14;        //大胡 抢杠胡
    public static final int MINGTANG_DH_GSP = 15;        //大胡 杠上炮
    public static final int MINGTANG_DH_BT = 16;         //大胡 报听
    public static final int MINGTANG_0W = 17;            //无王
    public static final int MINGTANG_4W = 18;            //四王


    private static final List<Integer> wufenList = Arrays.asList(MINGTANG_4W,MINGTANG_DH_TIANHU,MINGTANG_DH_DIHU);

    public static void addOddHuType(QzMjTable table, QzMjPlayer player, boolean zimo){
        Map<Integer, List<Integer>> idMingTang = player.getIdMingTang();
        if(idMingTang.isEmpty())
            return;
        for (Map.Entry<Integer,List<Integer>> entry:idMingTang.entrySet()){
            List<Integer> huType = entry.getValue();
            if(huType.size()==0)
                continue;
            //检查杠
            checkGangHu(table,player,huType,zimo);
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
//            if(dahu&&mts.contains(MINGTANG_PINGHU))
//                mts.remove((Integer) MINGTANG_PINGHU);
        }
    }


    public static void checkGangHu(QzMjTable table, QzMjPlayer player , List<Integer> huType,boolean zimo){
        if(zimo && player.getGangStatus() == 1){
            huType.add(MINGTANG_DH_GSH);
        }else if(!zimo){
            QzMjPlayer losePlayer = (QzMjPlayer) table.getSeatMap().get(table.getDisCardSeat());
            if(losePlayer != null){
                if(losePlayer.getGangStatus() == 2){
                    huType.add(MINGTANG_DH_GSP);
                } else if (losePlayer.getGangStatus() == 3) {
                    huType.add(MINGTANG_DH_QGH);
                }
            }
        }

    }



    public static int  getMingTangFen(Map<Integer,List<Integer>> idMingTang){
        if(idMingTang==null||idMingTang.size()==0)
            return 0;
        int fen=0;
        List<Integer>  huType = null;
        for(Map.Entry<Integer,List<Integer>> entry:idMingTang.entrySet()){
            huType = entry.getValue();
        }
        if(huType == null || huType.size() <= 0){
            return 0;
        }

        for (Integer mingtId : huType){
            switch (mingtId){
                case MINGTANG_PINGHU:
                case MINGTANG_DH_PPH:
                case MINGTANG_DH_HYS:
                case MINGTANG_0W:
                case MINGTANG_DH_QGH:
                        fen += 1;
                        break;
//                case MINGTANG_DH_TIANHU:
//                case MINGTANG_DH_DIHU:
//                case MINGTANG_4W:
//                        fen += 5;
//                        break;
                case MINGTANG_DH_QXD:
                case MINGTANG_DH_QXGW:
                        fen += 3;
                        break;
                case MINGTANG_DH_QYS:
                case MINGTANG_DH_ZYS:
                case MINGTANG_DH_DSY:
                case MINGTANG_DH_LP:
                case MINGTANG_DH_ZY:
                case MINGTANG_DH_GSP:
                case MINGTANG_DH_GSH:
                case MINGTANG_DH_BT:
                        fen += 2;
                default:
                    continue;
            }
        }


        for (int mingtId : wufenList){
            if(huType.contains(mingtId)){
                if(fen < 5){
                    huType.clear();
                    huType.add(mingtId);
                    fen = 5;
                }else{
                    huType.remove((Integer) mingtId);
                }
            }
        }
        return fen;
    }

    public static void removeLess(Map<Integer,List<Integer>> idMingTang,QzMjTable table){
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
                        bei+=1;
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
