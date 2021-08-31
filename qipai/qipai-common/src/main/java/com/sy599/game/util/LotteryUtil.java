package com.sy599.game.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 定义一个连续集合
 * 集合中元素x满足:(minElement,maxElement]
 * 数学表达式为：minElement < x <= maxElement
 *
 */
public class LotteryUtil {
public class ContinuousList {

    private double minElement;
    private double maxElement;

    public ContinuousList(double minElement, double maxElement){
        if(minElement > maxElement){
            throw new IllegalArgumentException("区间不合理，minElement不能大于maxElement！");
        }
        this.minElement = minElement;
        this.maxElement = maxElement;
    }

    /**
     * 判断当前集合是否包含特定元素
     * @param element
     * @return
     */
    public boolean isContainKey(double element){
        boolean flag = false;
        if(element > minElement && element <= maxElement){
            flag = true;
        }
        return flag;
    }

}

    private List<ContinuousList> lotteryList;   //概率连续集合
    private double maxElement;                  //这里只需要最大值，最小值默认为0.0

    /**
     * 构造抽奖集合
     * @param list 为奖品的概率
     */
    public LotteryUtil(List<Double> list){
        lotteryList = new ArrayList<ContinuousList>();
        if(list.size() == 0){
            throw new IllegalArgumentException("抽奖集合不能为空！");
        }
        double minElement = 0d;
        ContinuousList continuousList = null;
        for(Double d : list){
            minElement = maxElement;
            maxElement = maxElement + d;
            continuousList = new ContinuousList(minElement, maxElement);
            lotteryList.add(continuousList);
        }
    }

    /**
     * 进行抽奖操作
     * 返回：奖品的概率list集合中的下标
     */
    public int randomColunmIndex(){
        int index = -1;
        Random r = new Random();
        double d = r.nextDouble() * maxElement;  //生成0-1间的随机数
        if(d == 0d){
            d = r.nextDouble() * maxElement;     //防止生成0.0
        }
        int size = lotteryList.size();
        for(int i = 0; i < size; i++){
            ContinuousList cl = lotteryList.get(i);
            if(cl.isContainKey(d)){
                index = i;
                break;
            }
        }
        if(index == -1){
            throw new IllegalArgumentException("概率集合设置不合理！");
        }
        return index;

    }

    public double getMaxElement() {
        return maxElement;
    }

    public List<ContinuousList> getLotteryList() {
        return lotteryList;
    }
    public void setLotteryList(List<ContinuousList> lotteryList) {
        this.lotteryList = lotteryList;
    }


}