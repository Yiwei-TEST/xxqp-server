package com.sy599.game.qipai.bsmj.bean;

import java.util.ArrayList;
import java.util.List;

import com.sy599.game.qipai.bsmj.rule.BsMj;

/**
 * 检查是否胡牌的参数bean对象
 * @author liuping
 */
public class BsMjCheckHuParam {
	
	/**
	 * 手牌0
	 */
	private List<BsMj> mjOnHand;
	/**
	 * 暗杠的牌 
	 */
	private List<BsMj> mjOnAGang;
	/**
	 * 明杠的牌 
	 */
	private List<BsMj> mjOnMGang;
	/**
	 * 接杠的牌 
	 */
	private List<BsMj> mjOnJGang;
	/**
	 * 碰的牌
	 */
	private List<BsMj> mjOnPeng;
	/**
	 * 补张了的牌
	 */
	private List<BsMj> mjOnBuZhang;
	/**
	 * 吃的牌
	 */
	private List<BsMj> mjOnChi;

	/**
	 * 出的这张牌 如果是吃碰胡，就是前台发过来的第一张牌
	 */
	private BsMj majiang;
	
	public BsMjCheckHuParam() {
	}

	public BsMjCheckHuParam(List<BsMj> mjOnHand,
                            List<BsMj> mjOnAGang, List<BsMj> mjOnMGang, List<BsMj> mjOnJGang, List<BsMj> mjOnPeng,
                            List<BsMj> mjOnBuZhang, List<BsMj> mjOnChi, BsMj majiang) {
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

	public List<BsMj> getMjOnHand() {
		return mjOnHand;
	}

	public void setMjOnHand(List<BsMj> mjOnHand) {
		this.mjOnHand = mjOnHand;
	}

	public List<BsMj> getMjOnAGang() {
		return mjOnAGang;
	}

	public void setMjOnAGang(List<BsMj> mjOnAGang) {
		this.mjOnAGang = mjOnAGang;
	}
	
	public List<BsMj> getMjOnMGang() {
		return mjOnMGang;
	}

	public void setMjOnMGang(List<BsMj> mjOnMGang) {
		this.mjOnMGang = mjOnMGang;
	}

	public List<BsMj> getMjOnJGang() {
		return mjOnJGang;
	}

	public void setMjOnJGang(List<BsMj> mjOnJGang) {
		this.mjOnJGang = mjOnJGang;
	}

	public List<BsMj> getMjOnPeng() {
		return mjOnPeng;
	}

	public void setMjOnPeng(List<BsMj> mjOnPeng) {
		this.mjOnPeng = mjOnPeng;
	}

	public List<BsMj> getMjOnBuZhang() {
		return mjOnBuZhang;
	}

	public void setMjOnBuZhang(List<BsMj> mjOnBuZhang) {
		this.mjOnBuZhang = mjOnBuZhang;
	}

	public List<BsMj> getMjOnChi() {
		return mjOnChi;
	}

	public void setMjOnChi(List<BsMj> mjOnChi) {
		this.mjOnChi = mjOnChi;
	}

	public BsMj getMajiang() {
		return majiang;
	}

	public void setMajiang(BsMj majiang) {
		this.majiang = majiang;
	}
	
	public List<BsMj> getAllCards() {
		List<BsMj> allCards = new ArrayList<>();
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

	public List<BsMj> getGangCards() {
		List<BsMj> result = new ArrayList<>();
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
