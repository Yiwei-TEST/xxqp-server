package com.sy599.game.qipai.tjmj.bean;

import java.util.ArrayList;
import java.util.List;

import com.sy599.game.qipai.tjmj.rule.Mj;

/**
 * 检查是否胡牌的参数bean对象
 * @author liuping
 */
public class MjCheckHuParam {
	
	/**
	 * 手牌0
	 */
	private List<Mj> mjOnHand;
	/**
	 * 暗杠的牌 
	 */
	private List<Mj> mjOnAGang;
	/**
	 * 明杠的牌 
	 */
	private List<Mj> mjOnMGang;
	/**
	 * 接杠的牌 
	 */
	private List<Mj> mjOnJGang;
	/**
	 * 碰的牌
	 */
	private List<Mj> mjOnPeng;
	/**
	 * 补张了的牌
	 */
	private List<Mj> mjOnBuZhang;
	/**
	 * 吃的牌
	 */
	private List<Mj> mjOnChi;

	/**
	 * 出的这张牌 如果是吃碰胡，就是前台发过来的第一张牌
	 */
	private Mj majiang;
	
	public MjCheckHuParam() {
	}

	public MjCheckHuParam(List<Mj> mjOnHand,
						  List<Mj> mjOnAGang, List<Mj> mjOnMGang, List<Mj> mjOnJGang, List<Mj> mjOnPeng,
						  List<Mj> mjOnBuZhang, List<Mj> mjOnChi, Mj majiang) {
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

	public List<Mj> getMjOnHand() {
		return mjOnHand;
	}

	public void setMjOnHand(List<Mj> mjOnHand) {
		this.mjOnHand = mjOnHand;
	}

	public List<Mj> getMjOnAGang() {
		return mjOnAGang;
	}

	public void setMjOnAGang(List<Mj> mjOnAGang) {
		this.mjOnAGang = mjOnAGang;
	}
	
	public List<Mj> getMjOnMGang() {
		return mjOnMGang;
	}

	public void setMjOnMGang(List<Mj> mjOnMGang) {
		this.mjOnMGang = mjOnMGang;
	}

	public List<Mj> getMjOnJGang() {
		return mjOnJGang;
	}

	public void setMjOnJGang(List<Mj> mjOnJGang) {
		this.mjOnJGang = mjOnJGang;
	}

	public List<Mj> getMjOnPeng() {
		return mjOnPeng;
	}

	public void setMjOnPeng(List<Mj> mjOnPeng) {
		this.mjOnPeng = mjOnPeng;
	}

	public List<Mj> getMjOnBuZhang() {
		return mjOnBuZhang;
	}

	public void setMjOnBuZhang(List<Mj> mjOnBuZhang) {
		this.mjOnBuZhang = mjOnBuZhang;
	}

	public List<Mj> getMjOnChi() {
		return mjOnChi;
	}

	public void setMjOnChi(List<Mj> mjOnChi) {
		this.mjOnChi = mjOnChi;
	}

	public Mj getMajiang() {
		return majiang;
	}

	public void setMajiang(Mj majiang) {
		this.majiang = majiang;
	}
	
	public List<Mj> getAllCards() {
		List<Mj> allCards = new ArrayList<>();
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

	public List<Mj> getGangCards() {
		List<Mj> result = new ArrayList<>();
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
