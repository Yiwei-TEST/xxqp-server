package com.sy599.game.qipai.tdhmj.bean;

import java.util.ArrayList;
import java.util.List;

import com.sy599.game.qipai.tdhmj.rule.TdhMj;

/**
 * 检查是否胡牌的参数bean对象
 * @author liuping
 */
public class TdhMjCheckHuParam {
	
	/**
	 * 手牌0
	 */
	private List<TdhMj> mjOnHand;
	/**
	 * 暗杠的牌 
	 */
	private List<TdhMj> mjOnAGang;
	/**
	 * 明杠的牌 
	 */
	private List<TdhMj> mjOnMGang;
	/**
	 * 接杠的牌 
	 */
	private List<TdhMj> mjOnJGang;
	/**
	 * 碰的牌
	 */
	private List<TdhMj> mjOnPeng;
	/**
	 * 补张了的牌
	 */
	private List<TdhMj> mjOnBuZhang;
	/**
	 * 吃的牌
	 */
	private List<TdhMj> mjOnChi;

	/**
	 * 出的这张牌 如果是吃碰胡，就是前台发过来的第一张牌
	 */
	private TdhMj majiang;
	
	public TdhMjCheckHuParam() {
	}

	public TdhMjCheckHuParam(List<TdhMj> mjOnHand,
                            List<TdhMj> mjOnAGang, List<TdhMj> mjOnMGang, List<TdhMj> mjOnJGang, List<TdhMj> mjOnPeng,
                            List<TdhMj> mjOnBuZhang, List<TdhMj> mjOnChi, TdhMj majiang) {
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

	public List<TdhMj> getMjOnHand() {
		return mjOnHand;
	}

	public void setMjOnHand(List<TdhMj> mjOnHand) {
		this.mjOnHand = mjOnHand;
	}

	public List<TdhMj> getMjOnAGang() {
		return mjOnAGang;
	}

	public void setMjOnAGang(List<TdhMj> mjOnAGang) {
		this.mjOnAGang = mjOnAGang;
	}
	
	public List<TdhMj> getMjOnMGang() {
		return mjOnMGang;
	}

	public void setMjOnMGang(List<TdhMj> mjOnMGang) {
		this.mjOnMGang = mjOnMGang;
	}

	public List<TdhMj> getMjOnJGang() {
		return mjOnJGang;
	}

	public void setMjOnJGang(List<TdhMj> mjOnJGang) {
		this.mjOnJGang = mjOnJGang;
	}

	public List<TdhMj> getMjOnPeng() {
		return mjOnPeng;
	}

	public void setMjOnPeng(List<TdhMj> mjOnPeng) {
		this.mjOnPeng = mjOnPeng;
	}

	public List<TdhMj> getMjOnBuZhang() {
		return mjOnBuZhang;
	}

	public void setMjOnBuZhang(List<TdhMj> mjOnBuZhang) {
		this.mjOnBuZhang = mjOnBuZhang;
	}

	public List<TdhMj> getMjOnChi() {
		return mjOnChi;
	}

	public void setMjOnChi(List<TdhMj> mjOnChi) {
		this.mjOnChi = mjOnChi;
	}

	public TdhMj getMajiang() {
		return majiang;
	}

	public void setMajiang(TdhMj majiang) {
		this.majiang = majiang;
	}
	
	public List<TdhMj> getAllCards() {
		List<TdhMj> allCards = new ArrayList<>();
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

	public List<TdhMj> getGangCards() {
		List<TdhMj> result = new ArrayList<>();
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
