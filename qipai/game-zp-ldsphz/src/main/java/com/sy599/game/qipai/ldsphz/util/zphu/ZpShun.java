package com.sy599.game.qipai.ldsphz.util.zphu;

public class ZpShun {
    public static final int dui = 1;
    public static final int shun = 2;
    public static final int chi = 3;
    public static final int peng = 4;
    public static final int pao = 5;
    public static final int ti = 6;
    public static final int wei = 7;

    private int action;
    private int[] vals;
    private int huXi;
    private int lzNum;
    private int[] lzVal;
    private boolean hasDisCard;

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public int[] getVals() {
        return vals;
    }

    public void setVals(int[] vals) {
        this.vals = vals;
    }

    public int getHuXi() {
        return huXi;
    }

    public void setHuXi(int huXi) {
        this.huXi = huXi;
    }

    public int getLzNum() {
        return lzNum;
    }

    public void setLzNum(int lzNum) {
        this.lzNum = lzNum;
    }

    public int[] getLzVal() {
        return lzVal;
    }

    public void setLzVal(int[] lzVal) {
        this.lzVal = lzVal;
    }

    public boolean isHasDisCard() {
        return hasDisCard;
    }

    public void setHasDisCard(boolean hasDisCard) {
        this.hasDisCard = hasDisCard;
    }

    private ZpShun() {
    }

    public static ZpShun newDui(int[] vals, int huXi) {
        ZpShun res = new ZpShun();
        res.setVals(vals);
        res.setHuXi(huXi);
        res.setAction(dui);
        return res;
    }

    public static ZpShun newShun(int[] vals, int huXi) {
        ZpShun res = new ZpShun();
        res.setVals(vals);
        res.setHuXi(huXi);
        res.setAction(shun);
        return res;
    }

    public static ZpShun newChi(int[] vals, int huXi) {
        ZpShun res = new ZpShun();
        res.setVals(vals);
        res.setHuXi(huXi);
        res.setAction(chi);
        return res;
    }

    public static ZpShun newPeng(int[] vals, int huXi) {
        ZpShun res = new ZpShun();
        res.setVals(vals);
        res.setHuXi(huXi);
        res.setAction(peng);
        return res;
    }

    public static ZpShun newPao(int[] vals, int huXi) {
        ZpShun res = new ZpShun();
        res.setVals(vals);
        res.setHuXi(huXi);
        res.setAction(pao);
        return res;
    }

    public static ZpShun newTi(int[] vals, int huXi) {
        ZpShun res = new ZpShun();
        res.setVals(vals);
        res.setHuXi(huXi);
        res.setAction(ti);
        return res;
    }

    public static ZpShun newWei(int[] vals, int huXi) {
        ZpShun res = new ZpShun();
        res.setVals(vals);
        res.setHuXi(huXi);
        res.setAction(wei);
        return res;
    }

    public ZpShun clone() {
        ZpShun res = new ZpShun();
        res.setAction(this.action);
        res.setHuXi(this.huXi);
        res.setLzNum(this.lzNum);
        res.setHasDisCard(this.hasDisCard);
        res.setVals(ZpConstant.copyArr(this.vals));
        if (lzVal != null) {
            res.setLzVal(ZpConstant.copyArr(this.lzVal));
        }
        return res;
    }

}
