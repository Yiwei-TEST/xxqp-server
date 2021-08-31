package com.sy599.game.qipai.nxmj.bean;

import java.util.ArrayList;
import java.util.List;

import com.sy599.game.qipai.nxmj.rule.NxMj;

/**
 * 检查是否胡牌的参数bean对象
 * @author liuping
 */
public class NxMjCheckHuParam {
	
	/**
	 * 手牌0
	 */
	private List<NxMj> mjOnHand;
	/**
	 * 暗杠的牌 
	 */
	private List<NxMj> mjOnAGang;
	/**
	 * 明杠的牌 
	 */
	private List<NxMj> mjOnMGang;
	/**
	 * 接杠的牌 
	 */
	private List<NxMj> mjOnJGang;
	/**
	 * 碰的牌
	 */
	private List<NxMj> mjOnPeng;
	/**
	 * 补张了的牌
	 */
	private List<NxMj> mjOnBuZhang;
	/**
	 * 吃的牌
	 */
	private List<NxMj> mjOnChi;

	/**
	 * 出的这张牌 如果是吃碰胡，就是前台发过来的第一张牌
	 */
	private NxMj majiang;
	
	public NxMjCheckHuParam() {
	}

	public NxMjCheckHuParam(List<NxMj> mjOnHand,
                            List<NxMj> mjOnAGang, List<NxMj> mjOnMGang, List<NxMj> mjOnJGang, List<NxMj> mjOnPeng,
                            List<NxMj> mjOnBuZhang, List<NxMj> mjOnChi, NxMj majiang) {
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

	public List<NxMj> getMjOnHand() {
		return mjOnHand;
	}

	public void setMjOnHand(List<NxMj> mjOnHand) {
		this.mjOnHand = mjOnHand;
	}

	public List<NxMj> getMjOnAGang() {
		return mjOnAGang;
	}

	public void setMjOnAGang(List<NxMj> mjOnAGang) {
		this.mjOnAGang = mjOnAGang;
	}
	
	public List<NxMj> getMjOnMGang() {
		return mjOnMGang;
	}

	public void setMjOnMGang(List<NxMj> mjOnMGang) {
		this.mjOnMGang = mjOnMGang;
	}

	public List<NxMj> getMjOnJGang() {
		return mjOnJGang;
	}

	public void setMjOnJGang(List<NxMj> mjOnJGang) {
		this.mjOnJGang = mjOnJGang;
	}

	public List<NxMj> getMjOnPeng() {
		return mjOnPeng;
	}

	public void setMjOnPeng(List<NxMj> mjOnPeng) {
		this.mjOnPeng = mjOnPeng;
	}

	public List<NxMj> getMjOnBuZhang() {
		return mjOnBuZhang;
	}

	public void setMjOnBuZhang(List<NxMj> mjOnBuZhang) {
		this.mjOnBuZhang = mjOnBuZhang;
	}

	public List<NxMj> getMjOnChi() {
		return mjOnChi;
	}

	public void setMjOnChi(List<NxMj> mjOnChi) {
		this.mjOnChi = mjOnChi;
	}

	public NxMj getMajiang() {
		return majiang;
	}

	public void setMajiang(NxMj majiang) {
		this.majiang = majiang;
	}
	
	public List<NxMj> getAllCards() {
		List<NxMj> allCards = new ArrayList<>();
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

	public List<NxMj> getGangCards() {
		List<NxMj> result = new ArrayList<>();
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
