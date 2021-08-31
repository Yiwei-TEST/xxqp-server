package com.sy599.game.util.mj.serialize;

import java.util.ArrayList;
import java.util.List;

public class CardType implements Cloneable {

    private boolean is2582 = false;
    private int bossNum = 0;
    private int remainBoss = 0;
    private int pengNum = 0;

    List<Integer> yuVal = new ArrayList<>();

    public CardType(int bossNum) {
        this.bossNum = bossNum;
        this.remainBoss = bossNum;
    }

    public int getPengNum() {
        return pengNum;
    }

    public void addPengNum() {
        this.pengNum++;
    }

    public int getBossNum() {
        return bossNum;
    }

    public boolean isIs2582() {
        return is2582;
    }

    public void setIs2582(boolean is2582) {
        this.is2582 = is2582;
    }

    public int getRemainBoss() {
        return remainBoss;
    }

    public void setRemainBoss(int remainBoss) {
        this.remainBoss = remainBoss;
    }

    public void minusBoss(int remainBoss) {
        this.remainBoss -= remainBoss;
    }

    public List<Integer> getYuVal() {
        return yuVal;
    }

    public void setYuVal(List<Integer> yuVal) {
        this.yuVal = yuVal;
    }

    public void addAllVal(List<Integer> rmList) {
        yuVal.addAll(rmList);
    }

    public CardType clone() {
        CardType o = null;
        try {
            o = (CardType) super.clone();
            if (o.getYuVal() != null) {
                o.setYuVal(new ArrayList<>(yuVal));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return o;
    }


}
