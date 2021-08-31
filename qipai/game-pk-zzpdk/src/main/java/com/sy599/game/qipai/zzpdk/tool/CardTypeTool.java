package com.sy599.game.qipai.zzpdk.tool;

import com.sy599.game.qipai.zzpdk.bean.ZZPdkPlayer;
import com.sy599.game.qipai.zzpdk.bean.ZZPdkTable;
import com.sy599.game.qipai.zzpdk.util.CardUtils;
import com.sy599.game.qipai.zzpdk.util.CardUtils.Result;
import com.sy599.game.qipai.zzpdk.util.CardValue;

import java.util.*;

/**
 * 规则 查询牌型 等等
 *
 * @author lc
 */
public class CardTypeTool {

    /**
     * @param from 是自己已有的牌
     * @param oppo 对手出的牌
     * @return
     */
    public static List<Integer> canPlay(List<Integer> from, List<Integer> oppo, boolean isDisCards, ZZPdkPlayer player, ZZPdkTable table,Result result) {
        return CardTypeTool.getBestAI2(from, oppo, false, table,result);
    }

    /**
     * 接牌或出牌
     *
     * @param curList 当前手牌
     * @param oppo    要去接的牌，为空时表示自己第一个出牌
     * @param nextDan 下家是否报单
     * @param table   牌桌
     * @return
     */
    public static List<Integer> getBestAI2(List<Integer> curList, List<Integer> oppo, boolean nextDan, ZZPdkTable table, Result result) {
        if (curList == null || curList.size() == 0) {
            return Collections.emptyList();
        }

        List<Integer> retList = new ArrayList<>();
        Map<Integer, Integer> valAndNum = CardTool.loadCards(curList);
        int val = 0;
        int count = valAndNum.size();

        if (oppo == null || oppo.size() == 0) {
            if (count == 1) {
                retList.addAll(curList);
                return retList;
            }

            int size = curList.size();
            switch (size) {
                case 2:
                    if (nextDan) {
                        for (Map.Entry<Integer, Integer> kv : valAndNum.entrySet()) {
                            val = kv.getKey().intValue();
                        }
                        retList.add(CardTool.loadCards(curList, val).get(0));
                    } else {
                        for (Map.Entry<Integer, Integer> kv : valAndNum.entrySet()) {
                            val = kv.getKey().intValue();
                            break;
                        }
                        retList.add(CardTool.loadCards(curList, val).get(0));
                    }
                    break;
                case 3:
                    for (Map.Entry<Integer, Integer> kv : valAndNum.entrySet()) {
                        if (kv.getValue().intValue() == 3) {
                            retList.addAll(curList);
                            break;
                        }
                    }
                    for (Map.Entry<Integer, Integer> kv : valAndNum.entrySet()) {
                        if (kv.getValue().intValue() == 2) {
                            val = kv.getKey().intValue();
                            retList.addAll(CardTool.loadCards(curList, val));
                            break;
                        }
                    }
                    if (retList.size() == 0) {
                        if (nextDan) {
                            for (Map.Entry<Integer, Integer> kv : valAndNum.entrySet()) {
                                val = kv.getKey().intValue();
                            }
                            retList.add(CardTool.loadCards(curList, val).get(0));
                        } else {
                            for (Map.Entry<Integer, Integer> kv : valAndNum.entrySet()) {
                                val = kv.getKey().intValue();
                                break;
                            }
                            retList.add(CardTool.loadCards(curList, val).get(0));
                        }
                    }
                    break;
                case 4:
                    for (Map.Entry<Integer, Integer> kv : valAndNum.entrySet()) {
                        if (kv.getValue().intValue() == 3) {
                            if (kv.getKey().intValue() == 14 && table.getAAAZha() == 1) {
                                if (nextDan) {
                                    val = kv.getKey().intValue();
                                    retList.addAll(CardTool.loadCards(curList, val));
                                } else {
                                    for (Map.Entry<Integer, Integer> tmp : valAndNum.entrySet()) {
                                        if (tmp.getKey().intValue() != 14) {
                                            val = tmp.getKey().intValue();
                                            break;
                                        }
                                    }
                                    retList.addAll(CardTool.loadCards(curList, val));
                                }
                            } else {
                                retList.addAll(curList);
                                break;
                            }
                        }
                    }
                    if (retList.size() == 0) {
                        for (Map.Entry<Integer, Integer> kv : valAndNum.entrySet()) {
                            if (kv.getValue().intValue() == 2) {
                                val = kv.getKey().intValue();
                                retList.addAll(CardTool.loadCards(curList, val));
                                break;
                            }
                        }
                        if (retList.size() == 0) {
                            if (nextDan) {
                                for (Map.Entry<Integer, Integer> kv : valAndNum.entrySet()) {
                                    val = kv.getKey().intValue();
                                }
                                retList.add(CardTool.loadCards(curList, val).get(0));
                            } else {
                                for (Map.Entry<Integer, Integer> kv : valAndNum.entrySet()) {
                                    val = kv.getKey().intValue();
                                    break;
                                }
                                retList.add(CardTool.loadCards(curList, val).get(0));
                            }
                        }
                    }
                    break;
                case 5:
                    if (count == 2 || count == 3) {
                        for (Map.Entry<Integer, Integer> kv : valAndNum.entrySet()) {
                            if (kv.getValue().intValue() == 3) {
                                if (kv.getKey().intValue() == 14 && table.getAAAZha() == 1) {
                                    if (nextDan) {
                                        val = kv.getKey().intValue();
                                        retList.addAll(CardTool.loadCards(curList, val));
                                    } else {
                                        for (Map.Entry<Integer, Integer> tmp : valAndNum.entrySet()) {
                                            if (tmp.getKey().intValue() != 14) {
                                                val = tmp.getKey().intValue();
                                                break;
                                            }
                                        }
                                        retList.addAll(CardTool.loadCards(curList, val));
                                    }
                                } else {
                                    retList.addAll(curList);
                                    break;
                                }
                            } else if (kv.getValue().intValue() == 4) {
                                if (nextDan) {
                                    val = kv.getKey().intValue();
                                    retList.addAll(CardTool.loadCards(curList, val));
                                } else {
                                    for (Map.Entry<Integer, Integer> tmp : valAndNum.entrySet()) {
                                        if (tmp.getKey().intValue() != kv.getKey().intValue()) {
                                            val = tmp.getKey().intValue();
                                            break;
                                        }
                                    }
                                    retList.addAll(CardTool.loadCards(curList, val));
                                }
                                break;
                            }
                        }
                        if (retList.size() == 0) {
                            if (nextDan) {
                                int maxVal = 0;
                                for (Map.Entry<Integer, Integer> kv : valAndNum.entrySet()) {
                                    val = kv.getKey().intValue();
                                    if (kv.getValue() == 2) {
                                        retList.addAll(CardTool.loadCards(curList, val));
                                        return retList;
                                    }
                                    if (val > maxVal)
                                        maxVal = val;
                                }
                                val = maxVal;
                            } else {
                                for (Map.Entry<Integer, Integer> kv : valAndNum.entrySet()) {
                                    val = kv.getKey().intValue();
                                    break;
                                }
                            }
                            retList.add(CardTool.loadCards(curList, val).get(0));
                        }
                    } else if (count == 5) {
                        boolean isShun = true;
                        int pre = 0;
                        int current = 0;
                        for (Map.Entry<Integer, Integer> kv : valAndNum.entrySet()) {
                            val = kv.getKey().intValue();
                            if (current == 0) {
                                current = val;
                            } else {
                                if (pre == 0) {
                                    pre = current;
                                }
                                current = val;
                                if (current >= 15 || current - pre != 1) {
                                    isShun = false;
                                    break;
                                }
                            }
                        }

                        if (isShun) {
                            retList.addAll(curList);
                        } else {
                            for (Map.Entry<Integer, Integer> kv : valAndNum.entrySet()) {
                                if (kv.getValue().intValue() == 2) {
                                    val = kv.getKey().intValue();
                                    retList.addAll(CardTool.loadCards(curList, val));
                                    break;
                                }
                            }
                            if (retList.size() == 0) {
                                if (nextDan) {
                                    for (Map.Entry<Integer, Integer> kv : valAndNum.entrySet()) {
                                        val = kv.getKey().intValue();
                                    }
                                    retList.add(CardTool.loadCards(curList, val).get(0));
                                } else {
                                    for (Map.Entry<Integer, Integer> kv : valAndNum.entrySet()) {
                                        val = kv.getKey().intValue();
                                        break;
                                    }
                                    retList.add(CardTool.loadCards(curList, val).get(0));
                                }
                            }
                        }
                    } else {
                        for (Map.Entry<Integer, Integer> kv : valAndNum.entrySet()) {
                            if (kv.getValue().intValue() == 2) {
                                val = kv.getKey().intValue();
                                retList.addAll(CardTool.loadCards(curList, val));
                                break;
                            }
                        }
                        if (retList.size() == 0) {
                            if (nextDan) {
                                for (Map.Entry<Integer, Integer> kv : valAndNum.entrySet()) {
                                    val = kv.getKey().intValue();
                                }
                                retList.add(CardTool.loadCards(curList, val).get(0));
                            } else {
                                for (Map.Entry<Integer, Integer> kv : valAndNum.entrySet()) {
                                    val = kv.getKey().intValue();
                                    break;
                                }
                                retList.add(CardTool.loadCards(curList, val).get(0));
                            }
                        }
                    }
                    break;
                default:
                    if (count == size) {
                        boolean isShun = true;
                        int pre = 0;
                        int current = 0;
                        for (Map.Entry<Integer, Integer> kv : valAndNum.entrySet()) {
                            val = kv.getKey().intValue();
                            if (current == 0) {
                                current = val;
                            } else {
                                if (pre == 0) {
                                    pre = current;
                                }
                                current = val;
                                if (current >= 15 || current - pre != 1) {
                                    isShun = false;
                                    break;
                                }
                            }
                        }
                        if (isShun) {
                            retList.addAll(curList);
                        }
                    } else if (count * 2 == size) {
                        boolean isShun = true;
                        int pre = 0;
                        int current = 0;
                        for (Map.Entry<Integer, Integer> kv : valAndNum.entrySet()) {
                            if (kv.getValue().intValue() != 2) {
                                isShun = false;
                                break;
                            }
                            val = kv.getKey().intValue();
                            if (current == 0) {
                                current = val;
                            } else {
                                if (pre == 0)
                                    pre = current;
                                current = val;
                                if (current >= 15 || current - pre != 1) {
                                    isShun = false;
                                    break;
                                }
                            }
                        }
                        if (isShun) {
                            retList.addAll(curList);
                        }
                    }

                    val = 0;
                    if (retList.size() == 0) {
                        if(table.haveSanDai())
                            for (Map.Entry<Integer, Integer> kv : valAndNum.entrySet()) {
                                if (kv.getValue().intValue() == 3) {
                                    if (kv.getKey().intValue() == 14) {
                                        if (table.getAAAZha() != 1) {
                                            val = kv.getKey().intValue();
                                            break;
                                        }
                                    } else {
                                        val = kv.getKey().intValue();
                                        break;
                                    }
                                }
                            }
                        List<CardValue> cardValues = CardUtils.loadCards(curList);
                        List<CardValue> find=new ArrayList<>();
                        if (val > 0) {
                            Map<Integer,Integer> copy=new TreeMap<>(valAndNum);
                            copy.remove(val);
                            if(table.getSandai2()==1){
                                searchDan(cardValues,copy,2,0,find);
                            }
                            if(find.size()<2&&table.getSandaidui()==1){
                                find.clear();
                                searchDanAndDui(cardValues,copy,2,1,0,find,true);
                            }
                            if(find.size()<2&&table.getSandai1()==1){
                                find.clear();
                                searchDanAndDui(cardValues,copy,1,1,0,find,true);
                            }
                            if(find.size()==0&&table.getSanzhang()==1){
                                retList.addAll(CardTool.loadCards(curList, val));
                            }
                        }

                        if (retList.size() == 0) {
                            searchDanAndDui(cardValues,valAndNum,2,1,0,find,false);
                            if (find.size() == 2) {
                                retList.addAll(CardUtils.loadCardIds(find));
                            } else {
                                if (nextDan) {
                                    for (Map.Entry<Integer, Integer> kv : valAndNum.entrySet()) {
                                        if (kv.getValue().intValue() == 4) {
                                            val = kv.getKey().intValue();
                                            retList.addAll(CardTool.loadCards(curList, val));
                                            break;
                                        } else {
                                            val = kv.getKey().intValue();
                                        }
                                    }
                                    if (retList.size() == 0) {
                                        retList.add(CardTool.loadCards(curList, val).get(0));
                                    }
                                } else {
                                    //优先不拆炸弹
                                    for (Map.Entry<Integer, Integer> kv : valAndNum.entrySet()) {
                                        if (kv.getValue() != 4 && !(kv.getKey() == 14 && kv.getValue() == 3 && table.getAAAZha() == 1)) {
                                            if (val == 0) {
                                                val = kv.getKey().intValue();
                                            } else if (val > kv.getKey().intValue()) {
                                                val = kv.getKey().intValue();
                                            }
                                        }
                                    }
                                    if (val == 0) {
                                        for (Map.Entry<Integer, Integer> kv : valAndNum.entrySet()) {
                                            val = kv.getKey().intValue();
                                            break;
                                        }
                                    }
                                    retList.add(CardTool.loadCards(curList, val).get(0));
                                }
                            }
                        }
                    }
            }
        } else {
            if (nextDan && oppo.size() == 1) {
                for (Map.Entry<Integer, Integer> kv : valAndNum.entrySet()) {
                    if (kv.getValue().intValue() == 4) {
                        val = kv.getKey().intValue();
                        retList.addAll(CardTool.loadCards(curList, val));
                        break;
                    } else if (kv.getValue().intValue() == 3 && kv.getKey().intValue() == 14 && table.getAAAZha() == 1) {
                        val = kv.getKey().intValue();
                        retList.addAll(CardTool.loadCards(curList, val));
                        break;
                    } else {
                        val = kv.getKey().intValue();
                    }
                }
                if (retList.size() == 0 && val > CardTool.loadCardValue(oppo.get(0))) {
                    retList.add(CardTool.loadCards(curList, val).get(0));
                }
            } else {
                if (result.getType() > 0) {
                    List<CardValue> cardValueList = CardUtils.searchBiggerCardValues(CardUtils.loadCards(curList), result, table);
                    if (cardValueList != null && cardValueList.size() > 0) {
                        result = CardUtils.calcCardValue(cardValueList, table,false);
                        if ((result.isShunZi() || result.isLianDui() || result.isFeiJi()) && result.getMax() >= 15) {
                            return retList;
                        } else {
                            retList = CardUtils.loadCardIds(cardValueList);
                        }
                    }
                }
            }
        }
        return retList;
    }

    /**
     *
     * @param src          手牌，用于找出需要的牌
     * @param valAndNum    已经排除了三带本身三张
     * @param addSrcType   递归查询控制变量
     * @param searchCount  查询总数
     * @param findVal      查找牌大于findVal
     * @param result       查找结果
     * @return
     */
    private static void searchDanAndDui(List<CardValue> src,Map<Integer,Integer> valAndNum,
                                     int addSrcType,int searchCount, int findVal,List<CardValue> result,boolean sanDai){
        if(addSrcType>3)
            return;
        if(sanDai&&addSrcType==3&&result.size()==0)//如果递归到三张，而没有找到单牌，则不拆三带，直接返回
            return;
        List<Integer> srcVal=new ArrayList<>();
        boolean breakFlag=false;
        for (Map.Entry<Integer, Integer> kv : valAndNum.entrySet()) {
            if (kv.getValue().intValue() == addSrcType && kv.getKey().intValue() > findVal) {
                for (int i = 0; i < kv.getValue(); i++) {
                    srcVal.add(kv.getKey());
                    if(srcVal.size()==searchCount){
                        breakFlag=true;
                        break;
                    }
                }
            }
            if(breakFlag)
                break;
        }
        if(srcVal.size()>=searchCount){
            for (Integer val:srcVal){
                result.addAll(CardUtils.searchCardValues(src, val, 1));
            }
        }else {
            searchDanAndDui(src, valAndNum,addSrcType+1, searchCount - srcVal.size(), findVal,result,sanDai);
        }
    }


    private static void searchDan(List<CardValue> src,Map<Integer,Integer> valAndNum, int searchCount, int findVal,List<CardValue> result){
        List<Integer> srcVal=new ArrayList<>();
        for (Map.Entry<Integer, Integer> kv : valAndNum.entrySet()) {
            if (kv.getValue().intValue() >=1 && kv.getKey().intValue() > findVal) {
                srcVal.add(kv.getKey());
                if(srcVal.size()==searchCount)
                    break;
            }
        }
        if(srcVal.size()>=searchCount){
            for (Integer val:srcVal){
                result.addAll(CardUtils.searchCardValues(src, val, 1));
            }
        }
    }

}



