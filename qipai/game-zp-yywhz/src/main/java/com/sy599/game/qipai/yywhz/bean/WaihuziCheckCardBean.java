package com.sy599.game.qipai.yywhz.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sy599.game.qipai.yywhz.constant.GuihzCard;
import com.sy599.game.qipai.yywhz.tool.GuihuziHuLack;

/**
 * @author liuping
 * 鬼胡子摸牌或出牌可以做的操作  溜  > 漂 > 偎 ＞ 胡 ＞ 碰 ＞ 吃 
 */
public class WaihuziCheckCardBean {
	// 当前seat
	private int seat;
	/**
	 * 溜
	 */
	private boolean liu;
	/**
	 * 漂
	 */
	private boolean piao;
	/**
	 * 偎
	 */
	private boolean wei;
	/**
	 * 胡
	 */
	private boolean hu;
	/**
	 * 碰
	 */
	private boolean peng;
	/**
	 * 吃
	 */
	private boolean chi;
	/**
	 * 天胡或九对半胡
	 */
	private boolean tianHu;
	/**
	 * 出的牌或者是摸的牌
	 */
	private GuihzCard disCard;
	/**
	 * 可做的操作列表
	 */
	private List<Integer> actionList;
	/**
	 * 是否正在摸牌
	 */
	private boolean isMoPaiIng;
	/**
	 * 胡牌时相关信息
	 */
	private GuihuziHuLack lack;
	/**
	 * 过掉胡
	 */
	private boolean isPassHu;
	
	public WaihuziCheckCardBean(){
	}
	
	public WaihuziCheckCardBean(int seat, GuihzCard disCard) {
		this.seat = seat;
		this.disCard = disCard;
	}

	public List<Integer> getActionList() {
		return actionList;
	}

	public void setActionList(List<Integer> actionList) {
		this.actionList = actionList;
	}

	public GuihzCard getDisCard() {
		return disCard;
	}

	public void setDisCard(GuihzCard disCard) {
		this.disCard = disCard;
	}

	public int getSeat() {
		return seat;
	}

	public void setSeat(int seat) {
		this.seat = seat;
	}

	public boolean isLiu() {
		return liu;
	}

	public void setLiu(boolean liu) {
		this.liu = liu;
	}

	public boolean isPiao() {
		return piao;
	}

	public void setPiao(boolean piao) {
		this.piao = piao;
	}

	public boolean isWei() {
		return wei;
	}

	public void setWei(boolean wei) {
		this.wei = wei;
	}

	public boolean isHu() {
		return hu;
	}

	public void setHu(boolean hu) {
		this.hu = hu;
	}

	public boolean isPeng() {
		return peng;
	}

	public void setPeng(boolean peng) {
		this.peng = peng;
	}

	public boolean isChi() {
		return chi;
	}

	public void setChi(boolean chi) {
		this.chi = chi;
	}

	public boolean isTianHu() {
		return tianHu;
	}

	public void setTianHu(boolean tianHu) {
		this.tianHu = tianHu;
	}

	/**
	 * 0溜  1漂 2偎 3胡 4 碰 5吃 6天胡(九对半胡)
	 * @return
	 */
	public List<Integer> buildActionList() {
		int[] arr = new int[7];
		if (liu) {
			arr[3] = 1;
		}
//		if (piao) {
//			arr[1] = 1;
//		}
		if (wei) {
			arr[2] = 1;
		}
		if (hu) {
			arr[0] = 1;
		}
		if (peng) {
			arr[1] = 1;
		}
		if (chi) {
			arr[4] = 1;
		}
		if (tianHu) {
			arr[3] = 1;
		}
		List<Integer> list = new ArrayList<>();
		for (int val : arr) {
			list.add(val);
		}
		if (list.contains(1)) {
			actionList = list;
		} else {
			actionList = Collections.emptyList();
		}
		return actionList;
	}

	public boolean isMoPaiIng() {
		return isMoPaiIng;
	}

	public void setMoPaiIng(boolean isMoPaiIng) {
		this.isMoPaiIng = isMoPaiIng;
	}

//	public void initAutoDisData(String data) {
//		if (!StringUtils.isBlank(data)) {
//			int i = 0;
//			String[] values = data.split(";");
//			seat = StringUtil.getIntValue(values, i++);
//			this.isMoPaiIng = StringUtil.getIntValue(values, i++) == 1;
//			this.isPassHu = StringUtil.getIntValue(values, i++) == 1;
//		}
//	}
//
//	public String buildAutoDisStr() {
//		StringBuffer sb = new StringBuffer();
//		sb.append(seat).append(";");
//		sb.append(isMoPaiIng ? 1 : 0).append(";");
//		sb.append(isPassHu ? 1 : 0).append(";");
//		return sb.toString();
//	}

	public GuihuziHuLack getLack() {
		return lack;
	}

	public void setLack(GuihuziHuLack lack) {
		this.lack = lack;
	}
	
	public boolean isPassHu() {
		return isPassHu;
	}

	public void setPassHu(boolean isPassHu) {
		this.isPassHu = isPassHu;
	}
}
