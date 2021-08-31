package com.sy599.game.qipai.yzlc.bean;

import com.sy599.game.msg.serverPacket.TablePhzResMsg.PhzHuCards;
import com.sy599.game.msg.serverPacket.TablePhzResMsg.PhzHuCards.Builder;
import com.sy599.game.qipai.yzlc.constant.PaohzCard;
import com.sy599.game.qipai.yzlc.tool.PaohuziTool;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.StringUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 *@description 牌的组合, 每个组合可得胡息
 *@param
 *@return
 *@author Guang.OuYang
 *@date 2019/9/2
 */
@Data
@lombok.Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardTypeHuxi {
    //PaohzDisAction
    private int action;
    //组成该操作的卡组
	private List<Integer> cardIds = new ArrayList<>();
    //该操作的胡息
    private int hux;

    public boolean isHasCard(PaohzCard card) {
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
        }
    }

    public String toStr() {
        StringBuilder sb = new StringBuilder();
        sb.append(action).append("_");
        sb.append(StringUtil.implode(cardIds)).append("_");
        sb.append(hux).append("_");
        return sb.toString();
    }

    public Builder buildMsg() {
        return buildMsg(false);
    }

    public Builder buildMsg(boolean hideCards) {
        Builder msg = PhzHuCards.newBuilder();
        if (hideCards) {
            msg.addAllCards(PaohuziTool.toPhzCardZeroIds(cardIds));
        } else {
            msg.addAllCards(cardIds);
        }
        msg.setAction(action);
        msg.setHuxi(hux);

//		LogUtil.printDebug("牌型:{},{},{}",msg.getCardsList(), msg.getHuxi(), msg.getAction());
        return msg;
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

	public CardTypeHuxi huXiIncrease() {
    	++hux;
    	return this;
	}

}
