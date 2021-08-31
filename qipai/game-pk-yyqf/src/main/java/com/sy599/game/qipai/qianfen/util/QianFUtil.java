package com.sy599.game.qipai.qianfen.util;

import java.util.*;

public class QianFUtil {

    public static List<List<Integer>> fapai(List<Integer> leftList, List<Integer> useCard,List<Integer> cutList,int playerCount) {
        List<List<Integer>> paiList = new ArrayList<>();
        int count = 0;
        leftList.clear();
        for (int i = 0; i < 6; i++) {
            leftList.add(useCard.get(i));
            count++;
        }
        int pais = (useCard.size() - count) / 3;

        paiList.clear();
        Map<Integer, Integer> map = new HashMap<>();
        for (int m = 0; m < playerCount; m++) {
            List<Integer> temp = new ArrayList<>();
            for (int i = 0; i < pais; i++) {
                temp.add(useCard.get(count));
                count++;
            }
            paiList.add(temp);
            Map<Integer, Integer> countMap = CardUtils.countValue(CardUtils.loadCards(temp));
            for (Map.Entry<Integer, Integer> kv : countMap.entrySet()) {
                if (kv.getValue().intValue() >= 7) {
                    map.put(kv.getValue(), map.getOrDefault(kv.getValue(), 0) + 1);
                }
            }
        }

        cutList.clear();
        for (; count < useCard.size(); count++) {
            cutList.add(useCard.get(count));
        }
        return paiList;
    }

    public static List<List<Integer>> fapaiControl(List<Integer> leftList, List<Integer> useCard,List<Integer> cutList,int playerCount,int time){
        List<List<Integer>> list = fapai(leftList, useCard, cutList, playerCount);
        if(time>=20)
            return list;
        List<Integer> countOver7=new ArrayList<>();
        for (List<Integer> one:list) {
            Map<Integer,Integer> valAndCount= countVal(one);
            findCountOver7(countOver7,valAndCount);
        }

        int probability = getProbability(countOver7);

        Random rand=new Random();
        if(rand.nextInt(100)<probability){
            Collections.shuffle(useCard);
            return fapaiControl(leftList, useCard, cutList, playerCount,time+1);
        }
        return list;
    }

    private static int getProbability(List<Integer> countOver7){
        Collections.sort(countOver7,Collections.reverseOrder());
        for (Integer count:countOver7) {
            switch (count) {
                case 7:
                    return 50;
                case 8:
                    return 60;
                case 9:
                    return 70;
                case 10:
                    return 80;
                case 11:
                case 12:
                    return 100;
            }
        }
        return 0;
    }

    private static Map<Integer,Integer> countVal(List<Integer> vals){
        Map<Integer,Integer> valAndCount=new HashMap<>();
        for (Integer val:vals) {
            int yu=val%100;
            valAndCount.put(yu,valAndCount.getOrDefault(yu,0)+1);
        }
        return valAndCount;
    }

    private static void findCountOver7(List<Integer> countOver7,Map<Integer,Integer> valAndCount){
        for (Integer count:valAndCount.values()) {
            if(count>=7&&!countOver7.contains(count)){
                countOver7.add(count);
            }
        }
    }

    private static volatile int[] cutCardVals = new int[]{3, 4,6,7};

    public static void main(String[] args) {
        int[] num=new int[6];
        for (int i = 0; i < 1000000; i++) {
            List<Integer> leftCardList=new ArrayList<>();
            List<Integer> cutCardList=new ArrayList<>();
            List<List<Integer>> list = fapaiControl(leftCardList, CardUtils.loadCards(3, cutCardVals), cutCardList, 2,1);
            List<Integer> countOver7 = new ArrayList<>();
            for (List<Integer> one:list) {
                Map<Integer, Integer> valAndCount = countVal(one);
                findCountOver7(countOver7,valAndCount);
            }

            for (Integer count:countOver7) {
                num[count-7]++;
            }
        }

        for (int i = 0; i < num.length; i++) {
            System.out.println((i+7)+"炸出现："+num[i]);
        }
    }
}
