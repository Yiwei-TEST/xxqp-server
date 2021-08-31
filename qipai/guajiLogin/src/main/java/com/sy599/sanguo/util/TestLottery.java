package com.sy599.sanguo.util;

import com.sy.sanguo.game.bean.Lottery;
import com.sy.sanguo.game.service.SysInfManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestLottery {

    static final int TIME = 20000;

    public static void iteratorMap(Map<Integer, Integer> map, List<Double> list,List<String> prize) {
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            int index = entry.getKey();
            int time = entry.getValue();
            LotteryResult result = new LotteryResult(index, TIME, time, list.get(index),prize.get(index));
            System.out.println(result);
        }
    }

    public static void main(String[] args) {
        //创建奖品
        List<String> prize = new ArrayList<String>();
        List<Double> list = new ArrayList<Double>();
        List<Lottery> lotteries= SysInfManager.getInstance().initLottery();
        //读取奖品概率集合
        for( Lottery lottery:lotteries){
            list.add(lottery.getChance());
            prize.add(lottery.getName());
        }
        LotteryUtil ll = new LotteryUtil(list);
        double sumProbability = ll.getMaxElement();

        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        for (int i = 0; i < TIME; i++) {
            int index = ll.randomColunmIndex();
            if (map.containsKey(index)) {
                map.put(index, map.get(index) + 1);
            } else {
                map.put(index, 1);
            }
        }
        for (int i = 0; i < list.size(); i++) {
            double probability = list.get(i) / sumProbability;
            list.set(i, probability);
        }
        iteratorMap(map, list,prize);

    }


}