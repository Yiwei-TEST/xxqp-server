package com.sy599.game.qipai.tcdpmj.rule;

import com.sy599.game.qipai.tcdpmj.bean.TcdpMjPlayer;
import com.sy599.game.qipai.tcdpmj.bean.TcdpMjTable;
import com.sy599.game.qipai.tcdpmj.constant.TcdpMj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TcdpMjingTang {

    public static final int MINGTANG_PINGHU = 1;         //平胡
    public static final int MINGTANG_0QXD = 2;           //七小对
    public static final int MINGTANG_1QXD = 3;           //豪华七小对
    public static final int MINGTANG_2QXD = 4;           //超豪华七小对
    public static final int MINGTANG_3QXD = 5;           //超超豪华七小对
    public static final int MINGTANG_QGH = 6;            //抢杠胡


    public static List<Integer> getHuType(List<TcdpMj> mjs,boolean qgh){
        List<Integer> huTypes=new ArrayList<>();
        checkQXD(mjs,huTypes);
        if(qgh){
            huTypes.add(MINGTANG_QGH);
        }
        if(huTypes.size()==0)
            huTypes.add(MINGTANG_PINGHU);
        return huTypes;
    }

    public static void checkQXD(List<TcdpMj> mjs,List<Integer> huTypes){
        if(mjs.size()!=14)
            return;
        Map<Integer,Integer> valAndNum=new HashMap<>();
        for (TcdpMj mj:mjs) {
            int val = mj.getVal();
            if(valAndNum.containsKey(val)){
                valAndNum.put(val,valAndNum.get(val)+1);
            }else {
                valAndNum.put(val,1);
            }
        }

        int size4Num=0;
        for (Map.Entry<Integer,Integer> entry:valAndNum.entrySet()) {
            if(entry.getValue()%2!=0)
                return;
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
    }


    public static int getMingTangFen(List<Integer> mts,int playerCount){
        if(mts==null||mts.size()==0)
            return 1;
        int fen=0;
        for (Integer mt:mts) {
            switch (mt){
                case MINGTANG_PINGHU:
                    fen=1;
                    break;
                case MINGTANG_0QXD:
                    fen=2;
                    break;
                case MINGTANG_1QXD:
                    fen=4;
                    break;
                case MINGTANG_2QXD:
                    fen=8;
                    break;
                case MINGTANG_3QXD:
                    fen=16;
                    break;
                case MINGTANG_QGH:
                    fen=playerCount-1;
                    break;
            }
        }
        return fen;
    }
}
