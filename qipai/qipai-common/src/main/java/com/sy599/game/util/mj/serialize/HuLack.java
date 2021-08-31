package com.sy599.game.util.mj.serialize;


import java.util.ArrayList;
import java.util.List;

public class HuLack implements Cloneable {
    private boolean isHu;
    // 胡牌时的牌组
    private List<CardType> allcards = new ArrayList<>();

    public HuLack() {
    }


    public boolean isHu() {
        return isHu;
    }

    public void setHu(boolean isHu) {
        this.isHu = isHu;
    }

    public List<CardType> getAllcards() {
        return allcards;
    }

    public void setAllcards(List<CardType> allcards) {
        this.allcards = allcards;
    }


    public HuLack clone() {
        HuLack o = null;
        try {
            o = (HuLack) super.clone();
            if (allcards != null) {
                List<CardType> l = new ArrayList<>(allcards.size());
                for (CardType type : allcards) {
                    l.add(type.clone());
                }
                o.setAllcards(l);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return o;
    }


    public void addCardType(CardType ct) {
        allcards.add(ct);
    }


}
