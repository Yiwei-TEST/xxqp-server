package com.sy599.game.qipai.yiyangwhz.bean;

import com.sy599.game.qipai.yiyangwhz.constant.YyWhzCard;
import com.sy599.game.qipai.yiyangwhz.rule.YyWhzCardIndexArr;

import java.util.List;

public class YyWhzHuBean {
    private int huxi;
    private boolean ishu;
    private List<YyWhzCard> handCards;
    private YyWhzCardIndexArr valArr;
    private List<YyWhzCard> operateCards;

    public List<YyWhzCard> getHandCards() {
        return handCards;
    }

    public void setHandCards(List<YyWhzCard> handCards) {
        this.handCards = handCards;
    }

    public YyWhzCardIndexArr getValArr() {
        return valArr;
    }

    public void setValArr(YyWhzCardIndexArr valArr) {
        this.valArr = valArr;
    }

    public List<YyWhzCard> getOperateCards() {
        return operateCards;
    }

    public void setOperateCards(List<YyWhzCard> operateCards) {
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
