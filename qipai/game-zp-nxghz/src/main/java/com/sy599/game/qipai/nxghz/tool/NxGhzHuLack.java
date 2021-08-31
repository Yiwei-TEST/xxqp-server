package com.sy599.game.qipai.nxghz.tool;

import com.sy599.game.qipai.nxghz.bean.NxGhzCardTypeHuxi;
import com.sy599.game.qipai.nxghz.bean.NxGhzDisAction;
import com.sy599.game.qipai.nxghz.constant.NxGhzCard;
import com.sy599.game.util.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author liuping
 * 鬼胡子胡牌检查相关信息
 */
public class NxGhzHuLack implements Cloneable {
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
    private List<NxGhzCardTypeHuxi> ghzHuCards;
    /**
     * 胡牌时检查的牌
     */
    private NxGhzCard checkCard;
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

    public NxGhzCard getCheckCard() {
        return checkCard;
    }

    public void setCheckCard(NxGhzCard checkCard) {
        this.checkCard = checkCard;
    }

    public boolean isSelfMo() {
        return isSelfMo;
    }

    public void setSelfMo(boolean isSelfMo) {
        this.isSelfMo = isSelfMo;
    }

    public NxGhzHuLack(int hongzhongNum) {
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

    public List<NxGhzCardTypeHuxi> getGhzHuCards() {
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
        List<NxGhzCardTypeHuxi> allHuxiCards = new ArrayList<NxGhzCardTypeHuxi>(ghzHuCards);
        for (NxGhzCardTypeHuxi huxi : allHuxiCards) {
            if (huxi.getAction() == NxGhzDisAction.action_kang || huxi.getAction() == NxGhzDisAction.action_wei) {
                List<NxGhzCard> cards = NxGhzTool.toGhzCards(huxi.getCardIds());
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
        for (NxGhzCardTypeHuxi cardType : ghzHuCards) {
            if (!NxGhzTool.isSameCard(NxGhzTool.toGhzCards(cardType.getCardIds()))) {
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
        for (NxGhzCardTypeHuxi cardType : ghzHuCards) {
            List<NxGhzCard> cards = NxGhzTool.toGhzCards(cardType.getCardIds());
            List<Integer> cardPais = NxGhzTool.toGhzCardVals(cards, false);
            if (cardPais.contains(2) && cardPais.contains(7) && cardPais.contains(10)) {
                return true;
            } else {
                if (!NxGhzTool.isSameCard(cards) && NxGhzTool.c2710List.containsAll(cardPais)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setPhzHuCards(List<NxGhzCardTypeHuxi> phzHuCards) {
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
        NxGhzCardTypeHuxi type = new NxGhzCardTypeHuxi(action, cards, huxi, true);
        boolean selfMo = true;
        if (checkCard != null && cards.contains(new Integer(checkCard.getId())) && !isSelfMo) {// 当牌组包括胡牌时检查的牌  并且该牌不是自己摸的
            selfMo = false;
        }
        if (action == NxGhzDisAction.action_kang && selfMo == false) {
            type.setAction(NxGhzDisAction.action_peng);//胡的牌成坎 不是自己摸的
        }
        if (action == NxGhzDisAction.action_kang && selfMo == true) {
            if (checkCard != null && cards.contains(new Integer(checkCard.getId())))
                type.setAction(NxGhzDisAction.action_wei);//胡的牌成坎 并且是自己摸的
        }
        type.setSelfMo(selfMo);// 自己手上的牌
        this.ghzHuCards.add(type);
    }

    public void refreshSelfMoCard(boolean isSelfMo, NxGhzCard moCard) {
        for (NxGhzCardTypeHuxi huxiCard : ghzHuCards) {
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
        NxGhzCardTypeHuxi hucardType = null;
        NxGhzCardTypeHuxi kangType = null;
        if (isSelfMo) {// 自己摸的牌
            for (NxGhzCardTypeHuxi huxiCard : ghzHuCards) {
                if (huxiCard.getAction() == NxGhzDisAction.action_shun) {
                    List<NxGhzCard> cards = NxGhzTool.toGhzCards(huxiCard.getCardIds());
                    if (checkCard != null && cards.contains(checkCard))
                        hucardType = huxiCard;
                } else if (huxiCard.getAction() == NxGhzDisAction.action_kang) {
                    List<NxGhzCard> cards = NxGhzTool.toGhzCards(huxiCard.getCardIds());
                    if (checkCard != null && NxGhzTool.isHasCardVal(cards, checkCard.getVal())) {
                        kangType = huxiCard;
                    }
                }
            }
            if (hucardType != null && kangType != null) {
                int removeId = kangType.getCardIds().remove(0);
                kangType.getCardIds().add(0, checkCard.getId());
                kangType.setAction(NxGhzDisAction.action_wei);// 坎换成偎
                hucardType.getCardIds().remove((Integer) checkCard.getId());
                hucardType.getCardIds().add(removeId);//放到最后
                hucardType.setAction(NxGhzDisAction.action_shunChi);
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

    public void copy(NxGhzHuLack copy) {
        setHuxi(copy.getHuxi());
        setPhzHuCards(copy.getGhzHuCards());
        setCheckCard(copy.getCheckCard());
        setFail2710Val(copy.getFail2710Val());
        setSelfMo(copy.isSelfMo());
    }

    protected NxGhzHuLack clone() {
        NxGhzHuLack o = null;
        try {
            o = (NxGhzHuLack) super.clone();
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
