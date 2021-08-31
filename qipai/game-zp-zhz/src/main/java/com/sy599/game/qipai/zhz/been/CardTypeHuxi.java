package com.sy599.game.qipai.zhz.been;

import com.sy599.game.msg.serverPacket.TablePhzResMsg.PhzHuCards;
import com.sy599.game.msg.serverPacket.TablePhzResMsg.PhzHuCards.Builder;
import com.sy599.game.qipai.zhz.constant.PaohzCard;
import com.sy599.game.qipai.zhz.tool.PaohuziTool;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CardTypeHuxi implements Cloneable{
	private int action;
	private List<Integer> cardIds;
	//普通牌型该map存的是bossId及其代替的val,特殊牌型，存的是所有卡牌id及其对应的val
	private Map<Integer,Integer> idAndVals=new HashMap<>();

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

	public boolean isHasCard(PaohzCard card) {
		return cardIds != null && cardIds.contains(card.getId());
	}

	public Map<Integer, Integer> getIdAndVals() {
		return idAndVals;
	}

	public void setIdAndVals(Map<Integer, Integer> idAndVals) {
		this.idAndVals = idAndVals;
	}

	public void init(String data) {
		if (!StringUtils.isBlank(data)) {
			String[] values = data.split("_");
			action = StringUtil.getIntValue(values, 0);
			String cards = StringUtil.getValue(values, 1);
			if (!StringUtils.isBlank(cards)) {
				cardIds = StringUtil.explodeToIntList(cards);
			}
		}
	}

	public String toStr() {
		StringBuilder sb = new StringBuilder();
		sb.append(action).append("_");
		sb.append(StringUtil.implode(cardIds)).append("_");
		return sb.toString();
	}

	public Builder buildMsg(boolean buildAct) {
		return buildMsg(false,buildAct);
	}

	public Builder buildMsg(boolean hideCards,boolean buildAct) {
		Builder msg = PhzHuCards.newBuilder();
		if (hideCards) {
			msg.addAllCards(PaohuziTool.toPhzCardZeroIds(cardIds));
		} else {
			msg.addAllCards(cardIds);
		}
		if(buildAct){
            msg.setAction(action);
        } else{
            msg.setAction(0);
        }
		return msg;
	}

	public CardTypeHuxi clone() {
		CardTypeHuxi o = null;
		try {
			o = (CardTypeHuxi) super.clone();
			if(o.getCardIds()!=null){
				o.setCardIds(new ArrayList<>(cardIds));
			}
			if (idAndVals != null) {
				o.setIdAndVals(new HashMap<>(idAndVals));
			}
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.e("PaohuziHuLack clone err", e);
		}

		return o;
	}

}
