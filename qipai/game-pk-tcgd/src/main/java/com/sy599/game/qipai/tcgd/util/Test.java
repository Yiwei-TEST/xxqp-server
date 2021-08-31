package com.sy599.game.qipai.tcgd.util;

import com.alibaba.druid.support.json.JSONUtils;
import com.sy599.game.qipai.tcgd.bean.TcgdPlayer;
import com.sy599.game.qipai.tcgd.bean.TcgdTable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Test {
    public static void main(String[] args) {
//        List<Integer> ls = new ArrayList<>();
//        ls.add(1);
//        ls.add(2);
////        ls.add(3);
//        ls.add(4);
//
//        int curseat  = 2;
//        int nextseat = 0;
//        int index= 0;
//        for (int i=0;i<ls.size();i++){
//            if(ls.get(i)== curseat){
//                index = i;
//                index++;
//                if(index>=ls.size()){
//                    nextseat = ls.get(0);
//                }else{
//                    nextseat = ls.get(index);
//                }
//                break;
//            }
//        }
//        System.out.println( "cur seat:"+curseat +" ; next seat:"+nextseat );

        //------
//        Map<String,String > maap = new HashMap<>();\
//        maap.put("2","plaayer1231");
//        maap.put("1","plaayer2");
//        System.out.println(maap.get("2"));
        
        Map<String,Object> responsemap = new HashMap<>();
        responsemap.put("cmd","yrzb");
        responsemap.put("pos","3");
//        {"cmd":"xxx","card":"103"}
        String json = JSONUtils.toJSONString(responsemap);
//        System.out.println(json);
//        for (int i=0;i<100 ;i++){
//            System.out.print(new Random().nextInt(4)+1+" ");
//        }
//         List<Integer> cardList = new ArrayList<>(52);
//
//            for (int n = 0; n < 2; n++) {
//                for (int i = 1; i <= 4; i++) {
//                    for (int j = 3; j <= 15; j++) {
//                        //
//                        int card = i * 100 + j;
//                        cardList.add(card);
//                    }
//                }
//                cardList.add(501);//小王
//                cardList.add(502);//大王
//            }
//            System.out.println(cardList);
//        System.out.println(TcgdSfNew.intCardToStringCard(cardList));
//        System.out.println(TcgdSfNew.stringCardToIntCard(TcgdSfNew.intCardToStringCard(cardList)));
        TcgdTable TcgdTable = new TcgdTable();
        Map<Long, TcgdPlayer> playerMap = new ConcurrentHashMap<Long, TcgdPlayer>();
        Map<Integer, TcgdPlayer> seatMap = new ConcurrentHashMap<Integer, TcgdPlayer>();
        int n=1;
        long m=1;
        for(int i =1 ;i<=4;i++){
            TcgdPlayer p1 = new TcgdPlayer();
            p1.setSeat(i);
            p1.setUserId(m);
            p1.setName("no"+i+" play");
            playerMap.put(m,p1);
            seatMap.put(n,p1);
            n++;m++;
        }
        TcgdTable.setDuiwu(2);
        TcgdTable.setPlayerMap(playerMap);
        TcgdTable.setSeatMap(seatMap);
        TcgdTable.fapai();
        for (TcgdPlayer p: seatMap.values()  ) {
            System.out.println(p.getSeat() +" "+ p.getUserId()+" "+p.getHandPais());
        }
        TcgdTable.allocTeam(0);

    }


}
