package com.sy599.game.qipai.yjghz.tool;

import com.sy599.game.qipai.yjghz.bean.YjGhzCardTypeHuxi;
import com.sy599.game.qipai.yjghz.bean.YjGhzDisAction;
import com.sy599.game.qipai.yjghz.constant.YjGhzCard;
import com.sy599.game.util.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author liuping
 * 鬼胡子胡牌检查相关信息
 */
public class YjGhzHuLack implements Cloneable {
    /**
     *
     */
    private List<Integer> lackVal;
    /**
     * 红中个数
     */
    private int hongzhongNum;
    /**
     * 是否胡
     */
    private boolean isHu;
    /**
     * 硬息数
     */
    private int huxi;
    /**
     * 检查二七十失败的牌值
     */
    private List<Integer> fail2710Val;
    /**
     * 胡牌时的牌组
     */
    private List<YjGhzCardTypeHuxi> ghzHuCards;
    /**
     * 胡牌时检查的牌
     */
    private YjGhzCard checkCard;
    /**
     * 是否是自己摸的牌
     */
    private boolean isSelfMo;
    /**
     * 是否有无平息胡
     */
    private boolean hasWupingXi;
    /**
     * 胡算分
     */
    private int point;
    /**
     * 内圆 外圆个数
     */
    private Map<Integer, Integer> yuanMap;

    public YjGhzCard getCheckCard() {
        return checkCard;
    }

    public void setCheckCard(YjGhzCard checkCard) {
        this.checkCard = checkCard;
    }

    public boolean isSelfMo() {
        return isSelfMo;
    }

    public void setSelfMo(boolean isSelfMo) {
        this.isSelfMo = isSelfMo;
    }

    public YjGhzHuLack(int hongzhongNum) {
        lackVal = new ArrayList<>();
        this.hongzhongNum = hongzhongNum;
    }

    public void addLack(int val) {
        lackVal.add(val);
    }

    public void addAllLack(List<Integer> vallist) {
        lackVal.addAll(vallist);
    }

    public List<Integer> getLackVal() {
        return lackVal;
    }

    public void setLackVal(List<Integer> lackVal) {
        this.lackVal = lackVal;
    }

    public int getHongzhongNum() {
        return hongzhongNum;
    }

    public void changeHongzhong(int count) {
        hongzhongNum += count;
    }

    public void setHongzhongNum(int hongzhongNum) {
        this.hongzhongNum = hongzhongNum;
    }

    public boolean isHu() {
        return isHu;
    }

    public void setHu(boolean isHu) {
        this.isHu = isHu;
    }

    public int getHuxi() {
        return huxi;
    }

    public void changeHuxi(int huxi) {
        this.huxi += huxi;
    }

    public void setHuxi(int huxi) {
        this.huxi = huxi;
    }

    public List<Integer> getFail2710Val() {
        return fail2710Val;
    }

    public boolean isHasFail2710Val(int val) {
        if (val % 100 != 2) {
            return true;
        }
        if (fail2710Val == null) {
            return false;
        }
        return fail2710Val.contains(val);
    }

    public void addFail2710Val(int fail2710Val) {
        if (this.fail2710Val == null) {
            this.fail2710Val = new ArrayList<>();
        }
        this.fail2710Val.add(fail2710Val);
    }

    public List<YjGhzCardTypeHuxi> getGhzHuCards() {
        return ghzHuCards;
    }

    /**
     * 胡牌时手牌坎的数量
     *
     * @return
     */
    public int getKangNum() {
        int kangNum = 0;
        if (ghzHuCards == null) {
            ghzHuCards = new ArrayList<>();
        }
        List<YjGhzCardTypeHuxi> allHuxiCards = new ArrayList<YjGhzCardTypeHuxi>(ghzHuCards);
        for (YjGhzCardTypeHuxi huxi : allHuxiCards) {
            if (huxi.getAction() == YjGhzDisAction.action_kang || huxi.getAction() == YjGhzDisAction.action_wei) {
                List<YjGhzCard> cards = YjGhzTool.toGhzCards(huxi.getCardIds());
                if (cards.size() == 2 && huxi.isSelfMo() == true && cards.contains(checkCard)) {
                    kangNum++;
                }
                if (cards.size() == 3 && huxi.isSelfMo() == true) {
                    kangNum++;
                }
            }
        }
        return kangNum;
    }

    /**
     * 是否对子胡
     *
     * @return
     */
    public boolean isAllDuizi() {
        for (YjGhzCardTypeHuxi cardType : ghzHuCards) {
            if (!YjGhzTool.isSameCard(YjGhzTool.toGhzCards(cardType.getCardIds()))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 是否有2710
     *
     * @return
     */
    public boolean contains2710() {
        for (YjGhzCardTypeHuxi cardType : ghzHuCards) {
            List<YjGhzCard> cards = YjGhzTool.toGhzCards(cardType.getCardIds());
            List<Integer> cardPais = YjGhzTool.toGhzCardVals(cards, false);
            if (cardPais.contains(2) && cardPais.contains(7) && cardPais.contains(10)) {
                return true;
            } else {
                if (!YjGhzTool.isSameCard(cards) && YjGhzTool.c2710List.containsAll(cardPais)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setPhzHuCards(List<YjGhzCardTypeHuxi> phzHuCards) {
        this.ghzHuCards = phzHuCards;
    }

    /**
     * 手牌加入胡息牌组
     *
     * @param action
     * @param cards
     * @param huxi
     */
    public void addPhzHuCards(int action, List<Integer> cards, int huxi) {
        if (this.ghzHuCards == null) {
            this.ghzHuCards = new ArrayList<>();
        }
        YjGhzCardTypeHuxi type = new YjGhzCardTypeHuxi(action, cards, huxi, true);
        boolean selfMo = true;
        if (checkCard != null && cards.contains(new Integer(checkCard.getId())) && !isSelfMo) {// 当牌组包括胡牌时检查的牌  并且该牌不是自己摸的
            selfMo = false;
        }
        if (action == YjGhzDisAction.action_kang && selfMo == false) {
            type.setAction(YjGhzDisAction.action_peng);//胡的牌成坎 不是自己摸的
        }
        if (action == YjGhzDisAction.action_kang && selfMo == true) {
            if (checkCard != null && cards.contains(new Integer(checkCard.getId())))
                type.setAction(YjGhzDisAction.action_wei);//胡的牌成坎 并且是自己摸的
        }
        type.setSelfMo(selfMo);// 自己手上的牌
        this.ghzHuCards.add(type);
    }

    public void refreshSelfMoCard(boolean isSelfMo, YjGhzCard moCard) {
        for (YjGhzCardTypeHuxi huxiCard : ghzHuCards) {
            if (isSelfMo == false && moCard != null && huxiCard.getCardIds().contains(moCard.getId())) {
                huxiCard.getCardIds().remove(new Integer(moCard.getId()));
                huxiCard.getCardIds().add(0, moCard.getId());
                huxiCard.setSelfMo(false);
                break;
            }
        }
    }

    /**
     * 切换成偎胡
     */
    public void refreshWeiHuCard() {
        YjGhzCardTypeHuxi hucardType = null;
        YjGhzCardTypeHuxi kangType = null;
        if (isSelfMo) {// 自己摸的牌
            for (YjGhzCardTypeHuxi huxiCard : ghzHuCards) {
                if (huxiCard.getAction() == YjGhzDisAction.action_shun) {
                    List<YjGhzCard> cards = YjGhzTool.toGhzCards(huxiCard.getCardIds());
                    if (checkCard != null && cards.contains(checkCard))
                        hucardType = huxiCard;
                } else if (huxiCard.getAction() == YjGhzDisAction.action_kang) {
                    List<YjGhzCard> cards = YjGhzTool.toGhzCards(huxiCard.getCardIds());
                    if (checkCard != null && YjGhzTool.isHasCardVal(cards, checkCard.getVal())) {
                        kangType = huxiCard;
                    }
                }
            }
            if (hucardType != null && kangType != null) {
                int removeId = kangType.getCardIds().remove(0);
                kangType.getCardIds().add(0, checkCard.getId());
                kangType.setAction(YjGhzDisAction.action_wei);// 坎换成偎
                hucardType.getCardIds().remove((Integer) checkCard.getId());
                hucardType.getCardIds().add(removeId);//放到最后
                hucardType.setAction(YjGhzDisAction.action_shunChi);
            }
        }
    }

    public void setFail2710Val(List<Integer> fail2710Val) {
        this.fail2710Val = fail2710Val;
    }

    public int getPoint() {
        return point;
    }

    public void setPoint(int point) {
        this.point = point;
    }

    public boolean isHasWupingXi() {
        return hasWupingXi;
    }

    public void setHasWupingXi(boolean hasWupingXi) {
        this.hasWupingXi = hasWupingXi;
    }

    public Map<Integer, Integer> getYuanMap() {
        return yuanMap;
    }

    public void setYuanMap(Map<Integer, Integer> yuanMap) {
        this.yuanMap = yuanMap;
    }

    public void copy(YjGhzHuLack copy) {
        setHuxi(copy.getHuxi());
        setPhzHuCards(copy.getGhzHuCards());
        setCheckCard(copy.getCheckCard());
        setFail2710Val(copy.getFail2710Val());
        setSelfMo(copy.isSelfMo());
    }

    protected YjGhzHuLack clone() {
        YjGhzHuLack o = null;
        try {
            o = (YjGhzHuLack) super.clone();
            if (ghzHuCards != null) {
                o.setPhzHuCards(new ArrayList<>(ghzHuCards));
            }
            if (fail2710Val != null) {
                o.setFail2710Val(new ArrayList<>(fail2710Val));
            }
            if (checkCard != null) {
                o.setCheckCard(checkCard);
            }
            o.setHuxi(huxi);
            o.setSelfMo(isSelfMo);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e("PaohuziHuLack clone err", e);
        }
        return o;
    }
}
