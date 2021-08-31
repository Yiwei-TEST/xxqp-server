package com.sy599.game.qipai.ldsphz.util.zphu;


import com.sy599.game.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

public class ZpHuLack implements Cloneable {
    // 当前不包含癞子的牌组
    private int[] cardArr;
    // 是否需要对子
    private boolean isNeedDui;
    // 是否已经有对子
    private boolean hasDui;
    // 当前癞子数
    private int lzNum;
    // 是否胡牌
    private boolean isHu;
    // 胡息
    private int huXi;
    // 胡牌的对子
    private ZpShun dui;
    // 胡牌时的牌组
    private List<ZpShun> shunList;
    // 包含disCard的顺
    private ZpShun disCardShun;
    // 跑胡或者提胡
    private int huAction;
    // 是否自摸
    private boolean isSelfMo;
    // 当前index
    private int curIndex;
    // 剩余的牌数量
    private int hasPaiNum;
    // 癞子替换的牌val
    private int[] lzVal;

    public int[] getCardArr() {
        return cardArr;
    }

    public void setCardArr(int[] cardArr) {
        this.cardArr = cardArr;
    }

    public boolean isNeedDui() {
        return isNeedDui;
    }

    public void setNeedDui(boolean needDui) {
        isNeedDui = needDui;
    }

    public boolean isHasDui() {
        return hasDui;
    }

    public void setHasDui(boolean hasDui) {
        this.hasDui = hasDui;
    }

    public int getLzNum() {
        return lzNum;
    }

    public void setLzNum(int lzNum) {
        this.lzNum = lzNum;
    }

    public boolean isHu() {
        return isHu;
    }

    public void setHu(boolean hu) {
        isHu = hu;
    }

    public int getHuXi() {
        return huXi;
    }

    public void setHuXi(int huXi) {
        this.huXi = huXi;
    }

    public ZpShun getDui() {
        return dui;
    }

    public void setDui(ZpShun dui) {
        this.dui = dui;
    }

    public List<ZpShun> getShunList() {
        return shunList;
    }

    public void setShunList(List<ZpShun> shunList) {
        this.shunList = shunList;
    }

    public int getHuAction() {
        return huAction;
    }

    public void setHuAction(int huAction) {
        this.huAction = huAction;
    }

    public boolean isSelfMo() {
        return isSelfMo;
    }

    public void setSelfMo(boolean selfMo) {
        isSelfMo = selfMo;
    }

    public int getCurIndex() {
        return curIndex;
    }

    public void setCurIndex(int curIndex) {
        this.curIndex = curIndex;
    }

    public int getHasPaiNum() {
        return hasPaiNum;
    }

    public void setHasPaiNum(int hasPaiNum) {
        this.hasPaiNum = hasPaiNum;
    }

    public ZpShun getDisCardShun() {
        return disCardShun;
    }

    public void setDisCardShun(ZpShun disCardShun) {
        this.disCardShun = disCardShun;
    }

    public int[] getLzVal() {
        return lzVal;
    }

    public void setLzVal(int[] lzVal) {
        this.lzVal = lzVal;
    }

    public void addShun(ZpShun shun) {
        this.shunList.add(shun);
        huXi += shun.getHuXi();
        if (shun.isHasDisCard()) {
            disCardShun = shun;
        }
    }

    public void addLzNum(int num) {
        this.lzNum += num;
    }

    public ZpHuLack(int[] cardArr, int laiZiNum) {
        this.cardArr = cardArr;
        this.lzNum = laiZiNum;
        for (int i = 1; i <= 20; i++) {
            if (this.cardArr[i] > 0) {
                this.hasPaiNum += this.cardArr[i];
                if (this.curIndex == 0) {
                    this.curIndex = i;
                }
            }
        }
        this.shunList = new ArrayList<>();
        if ((hasPaiNum + laiZiNum) % 3 == 2) {
            this.isNeedDui = true;
        }
        lzVal = new int[21];
    }

    public void addLzVal(int[] lzVal) {
        for (int val : lzVal) {
            this.lzVal[val] = 1;
        }
    }

    public int getCurVal() {
        int res = 0;
        for (int i = curIndex; i <= 20; i++) {
            if (cardArr[i] > 0) {
                curIndex = i;
                res = i;
                break;
            }
        }
        return res;
    }

    /**
     * 从cardArr里移除paiZu里的牌
     *
     * @param paiZu    需要移除的val数组
     * @param rmValArr 成功移除的val数组
     * @param lzValArr 不够移除的val数组
     * @return 成功移除的数量
     */
    public int removeVals(int[] paiZu, int[] rmValArr, int[] lzValArr) {
        int res = 0;
        boolean canChai3 = false;
        if (paiZu.length == 5) {
            // 4张一样的，可拆3张
            canChai3 = true;
        } else {
            if (paiZu[0] == paiZu[1] && paiZu[1] == paiZu[2]) {
                // 3张一样的，可拆3张
                canChai3 = true;
            }
        }
        for (int index = 0; index < paiZu.length - 1; index++) {
            if (cardArr[paiZu[index]] == 3 && !canChai3) {
                lzValArr[index] = paiZu[index];
                continue;
            }
            if (cardArr[paiZu[index]] > 0) {
                cardArr[paiZu[index]] -= 1;
                rmValArr[index] = paiZu[index];
                res++;
            } else {
                lzValArr[index] = paiZu[index];
            }
        }
        this.hasPaiNum -= res;
        return res;
    }

    /**
     * 从cardArr里移除paiZu里的牌
     *
     * @param paiZu 需要移除的val数组
     * @return 成功移除的数量
     */
    public int removeVals(int[] paiZu) {
        int res = 0;
        for (int index = 0; index < paiZu.length; index++) {
            if (cardArr[paiZu[index]] > 0) {
                cardArr[paiZu[index]] -= 1;
                res++;
            }
        }
        this.hasPaiNum -= res;
        return res;
    }

    public ZpHuLack clone() {
        ZpHuLack res = null;
        try {
            res = (ZpHuLack) super.clone();
            if (this.shunList != null) {
                List<ZpShun> list = new ArrayList<>();
                for (int i = 0; i < shunList.size(); i++) {
                    list.add(shunList.get(i).clone());
                }
                res.setShunList(list);
            }
            if (this.cardArr != null) {
                int[] newCardArr = ZpConstant.copyArr(this.cardArr);
                res.setCardArr(newCardArr);
            }
            res.setCurIndex(this.curIndex);
            res.setHasPaiNum(this.hasPaiNum);
            res.setHuXi(this.huXi);
            res.setHuAction(this.huAction);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e("PaohuziHuLack clone err", e);
        }

        return res;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("isHu:").append(isHu);
        sb.append(",huXi:").append(huXi);
        if (dui != null) {
            sb.append(",dui:[");
            int[] vals = dui.getVals();
            for (int i = 0; i < vals.length; i++) {
                sb.append(ZpConstant.getName(vals[i])).append(",");
            }
            sb.append("]");
        }
        sb.append(",shun:[");
        for (ZpShun hx : shunList) {
            int[] vals = hx.getVals();
            for (int i = 0; i < vals.length; i++) {
                sb.append(ZpConstant.getName(vals[i])).append(",");
            }
            int[] lzVal = hx.getLzVal();
            if (lzVal != null) {
                sb.append(":");
                for (int i = 0; i < lzVal.length; i++) {
                    if (lzVal[i] > 0) {
                        sb.append(ZpConstant.getName(lzVal[i])).append(",");
                    }
                }
            }
            sb.append("|");
        }
        sb.append(" ] ");
        return sb.toString();
    }
}
