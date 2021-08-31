package com.sy599.game.qipai.yjghz.bean;

import com.sy599.game.msg.serverPacket.TablePhzResMsg.PhzHuCards;
import com.sy599.game.msg.serverPacket.TablePhzResMsg.PhzHuCards.Builder;
import com.sy599.game.qipai.yjghz.constant.YjGhzCard;
import com.sy599.game.qipai.yjghz.tool.YjGhzTool;
import com.sy599.game.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class YjGhzCardTypeHuxi {
    private int action;
    private List<Integer> cardIds;
    private int hux;
    /**
     * 是否自己摸的牌 用于判断内圆 外圆
     */
    private boolean selfMo;

    public YjGhzCardTypeHuxi() {
    }

    public YjGhzCardTypeHuxi(int action, List<Integer> cardIds, int hux, boolean selfMo) {
        this.action = action;
        this.cardIds = cardIds;
        this.hux = hux;
        this.selfMo = selfMo;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public List<Integer> getCardIds() {
        return cardIds;
    }

    public void setCardIds(List<Integer> cardIds) {
        this.cardIds = cardIds;
    }

    public int getHux() {
        return hux;
    }

    public void setHux(int hux) {
        this.hux = hux;
    }

    /**
     * 是否自己摸的牌 用于判断内圆 外圆
     *
     * @return
     */
    public boolean isSelfMo() {
        return selfMo;
    }

    public void setSelfMo(boolean selfMo) {
        this.selfMo = selfMo;
    }

    public boolean isHasCard(YjGhzCard card) {
        return cardIds != null && cardIds.contains(card.getId());
    }

    public void init(String data) {
        if (!StringUtils.isBlank(data)) {
            String[] values = data.split("_");
            action = StringUtil.getIntValue(values, 0);
            String cards = StringUtil.getValue(values, 1);
            if (!StringUtils.isBlank(cards)) {
                cardIds = StringUtil.explodeToIntList(cards);
            }
            hux = StringUtil.getIntValue(values, 2);
            selfMo = StringUtil.getIntValue(values, 3) == 1 ? true : false;
        }
    }

    public String toStr() {
        StringBuffer sb = new StringBuffer();
        sb.append(action).append("_");
        sb.append(StringUtil.implode(cardIds)).append("_");
        sb.append(hux).append("_");
        sb.append(selfMo == true ? 1 : 0);
        return sb.toString();
    }

    public Builder buildMsg() {
        return buildMsg(false);
    }

    public Builder buildMsg(boolean hideCards) {
        PhzHuCards.Builder msg = PhzHuCards.newBuilder();
        if (hideCards) {
            msg.addAllCards(YjGhzTool.toGhzCardZeroIds(cardIds));
        } else {
            if (cardIds == null)
                cardIds = new ArrayList<>();
            msg.addAllCards(cardIds);
        }
        msg.setAction(action);
        msg.setHuxi(hux);
        return msg;
    }
}
