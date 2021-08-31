package com.sy599.game.qipai.nxghz.bean;

import com.sy599.game.qipai.nxghz.constant.NxGhzCard;
import com.sy599.game.qipai.nxghz.rule.NxGhzCardIndexArr;

import java.util.List;

public class NxGhzHuBean {
    private int huxi;
    private boolean ishu;
    private List<NxGhzCard> handCards;
    private NxGhzCardIndexArr valArr;
    private List<NxGhzCard> operateCards;

    public List<NxGhzCard> getHandCards() {
        return handCards;
    }

    public void setHandCards(List<NxGhzCard> handCards) {
        this.handCards = handCards;
    }

    public NxGhzCardIndexArr getValArr() {
        return valArr;
    }

    public void setValArr(NxGhzCardIndexArr valArr) {
        this.valArr = valArr;
    }

    public List<NxGhzCard> getOperateCards() {
        return operateCards;
    }

    public void setOperateCards(List<NxGhzCard> operateCards) {
        this.operateCards = operateCards;
    }

    // private
    public int getHuxi() {
        return huxi;
    }

    public void setHuxi(int huxi) {
        this.huxi = huxi;
    }

    public boolean isIshu() {
        return ishu;
    }

    public void setIshu(boolean ishu) {
        this.ishu = ishu;
    }

}
