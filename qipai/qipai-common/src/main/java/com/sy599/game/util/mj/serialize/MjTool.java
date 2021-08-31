package com.sy599.game.util.mj.serialize;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MjTool {
    static List<Integer> cards = new ArrayList<>();
    static List<Integer> bossVal = new ArrayList<>();
    static List<Integer> val258 = new ArrayList<>();

    static {
        cards.add(1);
        cards.add(2);
        cards.add(3);
        cards.add(4);
        cards.add(5);
        cards.add(6);
        cards.add(7);
        cards.add(8);
        cards.add(3);
        cards.add(4);
        cards.add(5);
        cards.add(6);
        cards.add(7);
        cards.add(8);

        bossVal.add(11);


        val258.add(2);
        val258.add(5);
        val258.add(8);

    }


    public static void main(String[] args) {
        long l1 = System.currentTimeMillis();
        isHu(cards, bossVal);
        long l2 = System.currentTimeMillis();
        System.out.println(l2 - l1);
    }

    public static void changeIn(Integer i) {
        i++;
    }

    public static boolean isHu(List<Integer> vals, List<Integer> bossVal) {
        if (cards == null || cards.size() % 3 != 2)
            return false;
        int bossNum = dropBoss(cards, bossVal);
        List<List<Integer>> classfy = classifyCard(cards);
        HuLack lack = new HuLack();
        splitClassfy(lack, classfy, bossNum);
        System.out.println(classfy.toString());
        return false;
    }

    /**
     * 去除给定的王牌
     *
     * @param cardVals
     * @param bossVal
     * @return
     */
    public static int dropBoss(List<Integer> cardVals, List<Integer> bossVal) {
        //默认加入一张王牌，将牌最后处理
        int bossNum = 1;
        if (bossVal != null) {
            Iterator<Integer> it = cardVals.iterator();
            while (it.hasNext()) {
                Integer val = it.next();
                if (bossVal.contains(val)) {
                    it.remove();
                    bossNum++;
                }
            }
        }
        return bossNum;
    }

    /**
     * 按花色分类（不带风牌和红中）
     *
     * @param cardVals
     * @return
     */
    public static List<List<Integer>> classifyCard(List<Integer> cardVals) {
        List<List<Integer>> classfy = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            List<Integer> l = new ArrayList<>();
            classfy.add(l);
        }

        for (Integer val : cardVals) {
            int clas = val / 10 - 1;
            int yu = val % 10;
            List<Integer> l = classfy.get(clas);
            l.add(yu);
        }
        return classfy;
    }


    /**
     * @param lack
     * @param classfy
     * @param bossNum
     * @return
     */
    public static boolean splitClassfy(HuLack lack, List<List<Integer>> classfy, int bossNum) {
        for (List<Integer> clas : classfy) {
            CardType ct = new CardType(bossNum);
            List<CardType> cts = new ArrayList<>();
            splitCards(clas, ct, cts);
            ct = findUseLess(cts);
            if (ct != null) {
                lack.addCardType(ct);
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * @param vals
     * @param ct
     * @return
     */
    public static void splitCards(List<Integer> vals, CardType ct, List<CardType> cts) {
        if (vals.size() == 0)
            return;
        int val = vals.get(0);
        List<int[]> paiZus = MjConstant.getPaiZu(val);
        for (int[] paiZu : paiZus) {
            List<Integer> normal = new ArrayList<>(vals);
            CardType ctCopy = ct.clone();
            if (!removeVals(normal, paiZu, ctCopy))
                continue;
            if (normal.size() == 0) {
                cts.add(ctCopy);
            } else {
                splitCards(normal, ctCopy, cts);
            }
        }
    }


    /**
     * 删除normal中paiZu的值，添加到ct中
     *
     * @param normal
     * @param paiZu
     * @return
     */
    public static boolean removeVals(List<Integer> normal, int[] paiZu, CardType ct) {
        List<Integer> copyN = new ArrayList<>(normal);
        List<Integer> rmList = new ArrayList<>(paiZu.length);
        for (Integer val : paiZu) {
            for (int i = 0; i < copyN.size(); i++) {
                Integer yu = copyN.get(i);
                if (yu == val) {
                    rmList.add(yu);
                    copyN.remove(i);
                    break;
                }
            }
        }
        if (rmList.size() + ct.getRemainBoss() < paiZu.length) {
            //boss数不够组合用
            return false;
        }

        if (isSameCard(paiZu)) {
            ct.addPengNum();
            if (val258.contains(rmList.get(0)))
                ct.setIs2582(true);
        }
        normal.clear();
        normal.addAll(copyN);
        ct.minusBoss(3 - rmList.size());
        ct.addAllVal(rmList);
        return true;
    }

    public static boolean isSameCard(int[] ids) {
        int val = 0;
        for (int i = 0; i < ids.length; i++) {
            if (val == 0) {
                val = ids[i];
                continue;
            }
            if (val != ids[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * 找出使用boss数最少的组合
     *
     * @param cts
     * @return
     */
    public static CardType findUseLess(List<CardType> cts) {
        if (cts == null || cts.size() == 0)
            return null;
        int more = 0;
        CardType lesCt = null;
        for (CardType ct : cts) {
            int remainBoss = ct.getRemainBoss();
            if (remainBoss > more) {
                more = remainBoss;
                lesCt = ct;
            } else if (remainBoss == more) {
                //消耗同样的boss数量，则去碰较多的组合
                if (lesCt == null) {
                    lesCt = ct;
                } else if (ct.getPengNum() > lesCt.getPengNum()) {
                    lesCt = ct;
                }
            }
        }
        return lesCt;
    }

}
