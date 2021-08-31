package com.sy599.game.qipai.yjghz.bean;

import com.sy599.game.qipai.yjghz.constant.YjGhzCard;
import com.sy599.game.qipai.yjghz.rule.YjGhzCardIndexArr;

import java.util.List;

public class YjGhzHuBean {
    private int huxi;
    private boolean ishu;
    private List<YjGhzCard> handCards;
    private YjGhzCardIndexArr valArr;
    private List<YjGhzCard> operateCards;

    public List<YjGhzCard> getHandCards() {
        return handCards;
    }

    public void setHandCards(List<YjGhzCard> handCards) {
        this.handCards = handCards;
    }

    public YjGhzCardIndexArr getValArr() {
        return valArr;
    }

    public void setValArr(YjGhzCardIndexArr valArr) {
        this.valArr = valArr;
    }

    public List<YjGhzCard> getOperateCards() {
        return operateCards;
    }

    public void setOperateCards(List<YjGhzCard> operateCards) {
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
