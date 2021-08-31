package com.sy599.game.qipai.bsmj.bean;

import com.sy599.game.msg.serverPacket.TablePhzResMsg.PhzHuCards;
import com.sy599.game.msg.serverPacket.TablePhzResMsg.PhzHuCards.Builder;
import com.sy599.game.qipai.bsmj.rule.BsMj;
import com.sy599.game.qipai.bsmj.rule.BsMjHelper;
import com.sy599.game.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BsMjCardDisType {
	private int action;
	private List<Integer> cardIds;
	private int hux;
	private int disSeat;

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

	public void addCardId(int id) {
		if (this.cardIds == null) {
			this.cardIds = new ArrayList<>();
		}
		this.cardIds.add(id);
	}

	public int getHux() {
		return hux;
	}

	public void setHux(int hux) {
		this.hux = hux;
	}

	public int getDisSeat() {
		return disSeat;
	}

	public void setDisSeat(int disSeat) {
		this.disSeat = disSeat;
	}

	//	public boolean isHasCard(PaohzCard card) {
//		return cardIds != null && cardIds.contains(card.getId());
//	}

	public boolean isHasCard(BsMj card) {
		return cardIds != null && cardIds.contains(card.getId());
	}

	public boolean isHasCardVal(BsMj card) {
		if (cardIds != null) {
			List<BsMj> majiangs = BsMjHelper.toMajiang(cardIds);
			List<Integer> vals = BsMjHelper.toMajiangVals(majiangs);
			return vals.contains(card.getVal());
		}
		return false;

	}

	public boolean isHasCardVal(int val) {
		if (cardIds != null) {
			List<BsMj> majiangs = BsMjHelper.toMajiang(cardIds);
			List<Integer> vals = BsMjHelper.toMajiangVals(majiangs);
			return vals.contains(val);
		}
		return false;

	}

	/**
	 * 删除某个值
	 * @param val
	 * @return
	 */
	public int removeCardVal(int val) {
		if (cardIds != null) {
			Iterator<Integer> iterator = cardIds.iterator();
			while (iterator.hasNext()) {
				int id = iterator.next();
				BsMj majiang = BsMj.getMajang(id);
				if (majiang != null && majiang.getVal() == val) {
					iterator.remove();
					return id;
				}
			}
		}
		return 0;
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
			disSeat = StringUtil.getIntValue(values, 3);
		}
	}

	public String toStr() {
		StringBuffer sb = new StringBuffer();
		sb.append(action).append("_");
		sb.append(StringUtil.implode(cardIds)).append("_");
		sb.append(hux).append("_");
		sb.append(disSeat).append("_");
		return sb.toString();
	}

	public Builder buildMsg() {
		return buildMsg(false);
	}

	public Builder buildMsg(boolean hideCards) {
		Builder msg = PhzHuCards.newBuilder();
		msg.addAllCards(cardIds);
		msg.setAction(action);
		msg.setHuxi(disSeat);
		return msg;
	}
}
