package com.sy599.game.qipai.ldsphz.util.zphu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 跑胡子
 */
public class ZpHuBean {
    // 是否自摸
    private boolean isSelfMo;
    // 不包含癞子的牌组
    private int[] cardArr;
    // 癞子数
    private int laiZiNum;
    // disCardVal
    private int disCardVal;
    // 是否已经使用了disCardd
    private boolean usedDisCard;
    // 所有胡牌列表
    private List<ZpHuLack> huList;
    // 最大胡息列表
    private List<ZpHuLack> maxHuList;
    // 听牌信息
    private Map<Integer, List<Integer>> tingInfo;

    public boolean isSelfMo() {
        return isSelfMo;
    }

    public void setSelfMo(boolean selfMo) {
        isSelfMo = selfMo;
    }


    public int[] getCardArr() {
        return cardArr;
    }

    public void setCardArr(int[] cardArr) {
        this.cardArr = cardArr;
    }

    public int getDisCardVal() {
        return disCardVal;
    }

    public void setDisCardVal(int disCardVal) {
        this.disCardVal = disCardVal;
    }

    public int getLaiZiNum() {
        return laiZiNum;
    }

    public void setLaiZiNum(int laiZiNum) {
        this.laiZiNum = laiZiNum;
    }

    public boolean isUsedDisCard() {
        return usedDisCard;
    }

    public void setUsedDisCard(boolean usedDisCard) {
        this.usedDisCard = usedDisCard;
    }

    public List<ZpHuLack> getHuList() {
        return huList;
    }

    public void setHuList(List<ZpHuLack> huList) {
        this.huList = huList;
    }

    public List<ZpHuLack> getMaxHuList() {
        return maxHuList;
    }

    public void setMaxHuList(List<ZpHuLack> maxHuList) {
        this.maxHuList = maxHuList;
    }

    public Map<Integer, List<Integer>> getTingInfo() {
        return tingInfo;
    }

    public void setTingInfo(Map<Integer, List<Integer>> tingInfo) {
        this.tingInfo = tingInfo;
    }

    public ZpHuBean(int[] cardArr, int disCardVal, int laiZiNum, boolean isSelfMo) {
        this.isSelfMo = isSelfMo;
        this.cardArr = ZpConstant.copyArr(cardArr);
        this.disCardVal = disCardVal;
        if (disCardVal > 0) {
            this.cardArr[disCardVal]++;
        }
        this.laiZiNum = laiZiNum;
        this.huList = new ArrayList<>();
        this.maxHuList = new ArrayList<>();
    }

    public ZpHuBean(int[] cardArr, int laiZiNum) {
        this.isSelfMo = true;
        this.cardArr = ZpConstant.copyArr(cardArr);
        this.disCardVal = 0;
        if (disCardVal > 0) {
            this.cardArr[disCardVal]++;
        }
        this.laiZiNum = laiZiNum;
        this.huList = new ArrayList<>();
        this.maxHuList = new ArrayList<>();
    }

    /**
     * 拆对
     *
     * @param curLack
     */
    public void chaiDui(ZpHuLack curLack) {

        if (curLack.getHasPaiNum() == 0) {
            if (curLack.isNeedDui() && !curLack.isHasDui()) {
                // 没牌了，需要对，还没有对
                return;
            }
            curLack.setHu(true);
            this.huList.add(curLack);
            return;
        } else if (curLack.getHasPaiNum() + curLack.getLzNum() == 1) {
            return;
        }
        if (curLack.isNeedDui() && !curLack.isHasDui()) {
            // 拆对
            int[] cardArr = curLack.getCardArr();
            for (int val = 1; val <= 20; val++) {
                if (cardArr[val] == 2) {
                    // 两张作对
                    ZpHuLack newLack = curLack.clone();
                    newLack.removeVals(new int[]{val, val});
                    newLack.setDui(ZpShun.newDui(new int[]{val, val}, 0));
                    newLack.setNeedDui(false);
                    newLack.setHasDui(true);
                    chaiShun(newLack);
                }
            }
            if (curLack.getLzNum() > 0) {
                for (int val = 1; val <= 20; val++) {
                    // 单张+癞子补对
                    if (cardArr[val] == 0 || cardArr[val] == 3) {
                        continue;
                    }
                    ZpHuLack newLack = curLack.clone();
                    newLack.removeVals(new int[]{val});
                    ZpShun dui = ZpShun.newDui(new int[]{val, val}, 0);
                    dui.setLzNum(1);
                    dui.setLzVal(new int[]{val});
                    newLack.setDui(dui);
                    newLack.setNeedDui(false);
                    newLack.addLzNum(-1);
                    newLack.setHasDui(true);
                    newLack.addLzVal(new int[]{val});
                    chaiShun(newLack);
                }
                if (curLack.getLzNum() >= 2) {
                    // 癞子补对
                    ZpHuLack newLack = curLack.clone();
                    ZpShun dui = ZpShun.newDui(new int[]{ZpConstant.laiZiVal, ZpConstant.laiZiVal}, 0);
                    dui.setLzVal(new int[]{ZpConstant.laiZiVal, ZpConstant.laiZiVal});
                    dui.setLzNum(2);
                    newLack.setDui(dui);
                    newLack.setNeedDui(false);
                    newLack.addLzNum(-2);
                    newLack.setHasDui(true);
                    chaiShun(newLack);
                }
            }
        } else {
            chaiShun(curLack);
        }
    }

    /**
     * 拆顺
     *
     * @param curLack
     */
    public void chaiShun(ZpHuLack curLack) {
        if (curLack.getHasPaiNum() + curLack.getLzNum() == 1) {
            return;
        }
        if (curLack.getHasPaiNum() == 0) {
            curLack.setHu(true);
            this.huList.add(curLack);
            return;
        }
        //拆顺
        int val;
        boolean hasDisCard = false;
        if (!isSelfMo && !usedDisCard) {
            val = disCardVal;
            hasDisCard = true;
            usedDisCard = true;
        } else {
            val = curLack.getCurVal();
        }
        int[][] paiZus = ZpConstant.getPaiZu(val);
        int[] rmValArr;
        int[] lzValArr;
        ZpHuLack newLack;
        for (int[] paiZu : paiZus) {
            newLack = curLack.clone();
            int pzLen = paiZu.length - 1;
            rmValArr = new int[pzLen];
            lzValArr = new int[pzLen];
            int rmNum = newLack.removeVals(paiZu, rmValArr, lzValArr);
            if (rmNum == pzLen) {
                // 不需要使用癞子
                ZpShun shun = ZpShun.newShun(rmValArr, paiZu[pzLen]);
                shun.setHasDisCard(hasDisCard);
                newLack.addShun(shun);
                if (pzLen == 4 && !newLack.isHasDui()) {
                    newLack.setNeedDui(true);
                    chaiDui(newLack);
                } else {
                    chaiShun(newLack);
                }
            } else {
                if (pzLen - rmNum > newLack.getLzNum()) {
                    // 癞子不够
                    continue;
                } else if (rmNum == 0) {
                    continue;
                } else {
                    newLack.addLzNum(rmNum - pzLen);
                    ZpShun shun = ZpShun.newShun(ZpConstant.subArr1(paiZu), paiZu[pzLen]);
                    shun.setLzNum(pzLen - rmNum);
                    shun.setLzVal(lzValArr);
                    shun.setHasDisCard(hasDisCard);
                    newLack.addShun(shun);
                    newLack.addLzVal(lzValArr);
                    if (pzLen == 4 && !newLack.isHasDui()) {
                        newLack.setNeedDui(true);
                        chaiDui(newLack);
                    } else {
                        chaiShun(newLack);
                    }
                }
            }
        }
    }

    /**
     * 计算胡息
     */
    public void calcHx() {
        if (isSelfMo) {
            // 自摸
            for (ZpHuLack hu : huList) {
                if (maxHuList.size() == 0) {
                    maxHuList.add(hu);
                } else if (hu.getHuXi() == maxHuList.get(0).getHuXi()) {
                    maxHuList.add(hu);
                } else if (hu.getHuXi() > maxHuList.get(0).getHuXi()) {
                    maxHuList.clear();
                    maxHuList.add(hu);
                }
            }
            return;
        }
        // 非自摸
        int deltaHuXi = -3;
        for (ZpHuLack hu : huList) {
            ZpShun disCardShun = hu.getDisCardShun();
            disCardShun.setHuXi(disCardShun.getHuXi() + deltaHuXi);
            hu.setHuXi(hu.getHuXi() + deltaHuXi);
            if (maxHuList.size() == 0) {
                maxHuList.add(hu);
            } else if (hu.getHuXi() == maxHuList.get(0).getHuXi()) {
                maxHuList.add(hu);
            } else if (hu.getHuXi() > maxHuList.get(0).getHuXi()) {
                maxHuList.clear();
                maxHuList.add(hu);
            }
        }
    }

    /**
     * 计算胡
     * @return
     */
    public List<ZpHuLack> calcHu() {
        // 提
        ZpHuLack lack = new ZpHuLack(this.cardArr, this.laiZiNum);
        for (int i = 1; i < lack.getCardArr().length; i++) {
            if (lack.getCardArr()[i] == 4) {
                if (isSelfMo) {
                    lack.getCardArr()[i] = 0;
                    lack.addShun(ZpShun.newTi(new int[]{i, i, i, i}, 0));
                    lack.setNeedDui(true);
                } else {
                    if (disCardVal > 0 && disCardVal != i) {
                        lack.getCardArr()[i] = 0;
                        lack.addShun(ZpShun.newTi(new int[]{i, i, i, i}, 0));
                        lack.setNeedDui(true);
                    }
                }
            }
        }

        if (!lack.isNeedDui()) {
            // 防止后期拆到四张牌后，需要补对，先拆对
            ZpHuLack clone = lack.clone();
            clone.setNeedDui(true);
            chaiDui(clone);
        }
        chaiDui(lack);

        if (huList.size() > 0) {
            calcHx();
        }
        return huList;
    }

    /**
     * 计算听牌
     *
     * @return
     */
    public Map<Integer, List<Integer>> calcTing() {
        if (tingInfo == null) {
            tingInfo = new HashMap<>();
        }
        for (int val = 1; val <= 20; val++) {
            if (cardArr[val] == 0) {
                continue;
            }
            if (val == ZpConstant.laiZiVal) {
                continue;
            }
            cardArr[val] -= 1;
            int[] newCardArr = ZpConstant.copyArr(cardArr);
            List<ZpHuLack> huList = new ZpHuBean(newCardArr, laiZiNum + 1).calcHu();
            if (huList.size() > 0) {
                List<Integer> tingList = new ArrayList<>();
                int[] ting = new int[21];
                for (ZpHuLack hu : huList) {
                    int[] lzVal = hu.getLzVal();
                    for (int i = 1; i <= 20; i++) {
                        if (lzVal[i] == 1) {
                            ting[i] = 1;
                        }
                    }
                }
                for (int i = 1; i <= 20; i++) {
                    if (ting[i] == 1) {
                        tingList.add(i);
                    }
                }
                tingInfo.put(val, tingList);
            }
        }
        return tingInfo;
    }

}
