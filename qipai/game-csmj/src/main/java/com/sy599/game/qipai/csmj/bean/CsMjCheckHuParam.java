package com.sy599.game.qipai.csmj.bean;

import java.util.ArrayList;
import java.util.List;

import com.sy599.game.qipai.csmj.rule.CsMj;

/**
 * 检查是否胡牌的参数bean对象
 * @author liuping
 */
public class CsMjCheckHuParam {
	
	/**
	 * 手牌0
	 */
	private List<CsMj> mjOnHand;
	/**
	 * 暗杠的牌 
	 */
	private List<CsMj> mjOnAGang;
	/**
	 * 明杠的牌 
	 */
	private List<CsMj> mjOnMGang;
	/**
	 * 接杠的牌 
	 */
	private List<CsMj> mjOnJGang;
	/**
	 * 碰的牌
	 */
	private List<CsMj> mjOnPeng;
	/**
	 * 补张了的牌
	 */
	private List<CsMj> mjOnBuZhang;
	/**
	 * 吃的牌
	 */
	private List<CsMj> mjOnChi;

	/**
	 * 出的这张牌 如果是吃碰胡，就是前台发过来的第一张牌
	 */
	private CsMj majiang;
	
	public CsMjCheckHuParam() {
	}

	public CsMjCheckHuParam(List<CsMj> mjOnHand,
                            List<CsMj> mjOnAGang, List<CsMj> mjOnMGang, List<CsMj> mjOnJGang, List<CsMj> mjOnPeng,
                            List<CsMj> mjOnBuZhang, List<CsMj> mjOnChi, CsMj majiang) {
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

	public List<CsMj> getMjOnHand() {
		return mjOnHand;
	}

	public void setMjOnHand(List<CsMj> mjOnHand) {
		this.mjOnHand = mjOnHand;
	}

	public List<CsMj> getMjOnAGang() {
		return mjOnAGang;
	}

	public void setMjOnAGang(List<CsMj> mjOnAGang) {
		this.mjOnAGang = mjOnAGang;
	}
	
	public List<CsMj> getMjOnMGang() {
		return mjOnMGang;
	}

	public void setMjOnMGang(List<CsMj> mjOnMGang) {
		this.mjOnMGang = mjOnMGang;
	}

	public List<CsMj> getMjOnJGang() {
		return mjOnJGang;
	}

	public void setMjOnJGang(List<CsMj> mjOnJGang) {
		this.mjOnJGang = mjOnJGang;
	}

	public List<CsMj> getMjOnPeng() {
		return mjOnPeng;
	}

	public void setMjOnPeng(List<CsMj> mjOnPeng) {
		this.mjOnPeng = mjOnPeng;
	}

	public List<CsMj> getMjOnBuZhang() {
		return mjOnBuZhang;
	}

	public void setMjOnBuZhang(List<CsMj> mjOnBuZhang) {
		this.mjOnBuZhang = mjOnBuZhang;
	}

	public List<CsMj> getMjOnChi() {
		return mjOnChi;
	}

	public void setMjOnChi(List<CsMj> mjOnChi) {
		this.mjOnChi = mjOnChi;
	}

	public CsMj getMajiang() {
		return majiang;
	}

	public void setMajiang(CsMj majiang) {
		this.majiang = majiang;
	}
	
	public List<CsMj> getAllCards() {
		List<CsMj> allCards = new ArrayList<>();
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

	public List<CsMj> getGangCards() {
		List<CsMj> result = new ArrayList<>();
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
