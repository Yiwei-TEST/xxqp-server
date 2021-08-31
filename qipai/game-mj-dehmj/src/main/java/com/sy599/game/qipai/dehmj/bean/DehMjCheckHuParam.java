package com.sy599.game.qipai.dehmj.bean;

import java.util.ArrayList;
import java.util.List;

import com.sy599.game.qipai.dehmj.rule.DehMj;

/**
 * 检查是否胡牌的参数bean对象
 * @author liuping
 */
public class DehMjCheckHuParam {
	
	/**
	 * 手牌0
	 */
	private List<DehMj> mjOnHand;
	/**
	 * 暗杠的牌 
	 */
	private List<DehMj> mjOnAGang;
	/**
	 * 明杠的牌 
	 */
	private List<DehMj> mjOnMGang;
	/**
	 * 接杠的牌 
	 */
	private List<DehMj> mjOnJGang;
	/**
	 * 碰的牌
	 */
	private List<DehMj> mjOnPeng;
	/**
	 * 补张了的牌
	 */
	private List<DehMj> mjOnBuZhang;
	/**
	 * 吃的牌
	 */
	private List<DehMj> mjOnChi;

	/**
	 * 出的这张牌 如果是吃碰胡，就是前台发过来的第一张牌
	 */
	private DehMj majiang;
	
	public DehMjCheckHuParam() {
	}

	public DehMjCheckHuParam(List<DehMj> mjOnHand,
                            List<DehMj> mjOnAGang, List<DehMj> mjOnMGang, List<DehMj> mjOnJGang, List<DehMj> mjOnPeng,
                            List<DehMj> mjOnBuZhang, List<DehMj> mjOnChi, DehMj majiang) {
		super();
		this.mjOnHand = mjOnHand;
		this.mjOnAGang = mjOnAGang;
		this.mjOnMGang = mjOnMGang;
		this.mjOnJGang = mjOnJGang;
		this.mjOnPeng = mjOnPeng;
		this.mjOnBuZhang = mjOnBuZhang;
		this.mjOnChi = mjOnChi;
		this.majiang = majiang;
	}

	public List<DehMj> getMjOnHand() {
		return mjOnHand;
	}

	public void setMjOnHand(List<DehMj> mjOnHand) {
		this.mjOnHand = mjOnHand;
	}

	public List<DehMj> getMjOnAGang() {
		return mjOnAGang;
	}

	public void setMjOnAGang(List<DehMj> mjOnAGang) {
		this.mjOnAGang = mjOnAGang;
	}
	
	public List<DehMj> getMjOnMGang() {
		return mjOnMGang;
	}

	public void setMjOnMGang(List<DehMj> mjOnMGang) {
		this.mjOnMGang = mjOnMGang;
	}

	public List<DehMj> getMjOnJGang() {
		return mjOnJGang;
	}

	public void setMjOnJGang(List<DehMj> mjOnJGang) {
		this.mjOnJGang = mjOnJGang;
	}

	public List<DehMj> getMjOnPeng() {
		return mjOnPeng;
	}

	public void setMjOnPeng(List<DehMj> mjOnPeng) {
		this.mjOnPeng = mjOnPeng;
	}

	public List<DehMj> getMjOnBuZhang() {
		return mjOnBuZhang;
	}

	public void setMjOnBuZhang(List<DehMj> mjOnBuZhang) {
		this.mjOnBuZhang = mjOnBuZhang;
	}

	public List<DehMj> getMjOnChi() {
		return mjOnChi;
	}

	public void setMjOnChi(List<DehMj> mjOnChi) {
		this.mjOnChi = mjOnChi;
	}

	public DehMj getMajiang() {
		return majiang;
	}

	public void setMajiang(DehMj majiang) {
		this.majiang = majiang;
	}
	
	public List<DehMj> getAllCards() {
		List<DehMj> allCards = new ArrayList<>();
		if(mjOnHand != null)
			allCards.addAll(mjOnHand);
		if(majiang != null && !mjOnHand.contains(majiang)) //手牌里不包括胡的牌
			allCards.add(majiang);
		if(mjOnAGang != null)
			allCards.addAll(mjOnAGang);
		if(mjOnMGang != null)
			allCards.addAll(mjOnMGang);
		if(mjOnJGang != null)
			allCards.addAll(mjOnJGang);
		if(mjOnPeng != null)
			allCards.addAll(mjOnPeng);
		if(mjOnBuZhang != null)
			allCards.addAll(mjOnBuZhang);
		if(mjOnChi != null)
			allCards.addAll(mjOnChi);
		return allCards;
	}

	public List<DehMj> getGangCards() {
		List<DehMj> result = new ArrayList<>();
		if(mjOnAGang != null)
			result.addAll(mjOnAGang);
		if(mjOnMGang != null)
			result.addAll(mjOnMGang);
		if(mjOnJGang != null)
			result.addAll(mjOnJGang);
		return result;
	}

	/**
	 * 玩家是否有碰过或者杠过
	 * @return
	 */
	public boolean isPengOrGang() {
		if(!mjOnPeng.isEmpty() || !mjOnAGang.isEmpty() || !mjOnMGang.isEmpty() || !mjOnJGang.isEmpty()) {
			return true;
		} else
			return false;
	}
}
