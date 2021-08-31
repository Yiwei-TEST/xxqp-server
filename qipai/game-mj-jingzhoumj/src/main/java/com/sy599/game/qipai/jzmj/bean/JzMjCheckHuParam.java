package com.sy599.game.qipai.jzmj.bean;

import com.sy599.game.qipai.jzmj.rule.JzMj;

import java.util.ArrayList;
import java.util.List;

/**
 * 检查是否胡牌的参数bean对象
 * @author liuping
 */
public class JzMjCheckHuParam {
	
	/**
	 * 手牌0
	 */
	private List<JzMj> mjOnHand;
	/**
	 * 暗杠的牌 
	 */
	private List<JzMj> mjOnAGang;
	/**
	 * 明杠的牌 
	 */
	private List<JzMj> mjOnMGang;
	/**
	 * 接杠的牌 
	 */
	private List<JzMj> mjOnJGang;
	/**
	 * 碰的牌
	 */
	private List<JzMj> mjOnPeng;
	/**
	 * 补张了的牌
	 */
	private List<JzMj> mjOnBuZhang;
	/**
	 * 吃的牌
	 */
	private List<JzMj> mjOnChi;

	/**
	 * 出的这张牌 如果是吃碰胡，就是前台发过来的第一张牌
	 */
	private JzMj majiang;
	
	public JzMjCheckHuParam() {
	}

	public JzMjCheckHuParam(List<JzMj> mjOnHand,
							List<JzMj> mjOnAGang, List<JzMj> mjOnMGang, List<JzMj> mjOnJGang, List<JzMj> mjOnPeng,
							List<JzMj> mjOnBuZhang, List<JzMj> mjOnChi, JzMj majiang) {
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

	public List<JzMj> getMjOnHand() {
		return mjOnHand;
	}

	public void setMjOnHand(List<JzMj> mjOnHand) {
		this.mjOnHand = mjOnHand;
	}

	public List<JzMj> getMjOnAGang() {
		return mjOnAGang;
	}

	public void setMjOnAGang(List<JzMj> mjOnAGang) {
		this.mjOnAGang = mjOnAGang;
	}
	
	public List<JzMj> getMjOnMGang() {
		return mjOnMGang;
	}

	public void setMjOnMGang(List<JzMj> mjOnMGang) {
		this.mjOnMGang = mjOnMGang;
	}

	public List<JzMj> getMjOnJGang() {
		return mjOnJGang;
	}

	public void setMjOnJGang(List<JzMj> mjOnJGang) {
		this.mjOnJGang = mjOnJGang;
	}

	public List<JzMj> getMjOnPeng() {
		return mjOnPeng;
	}

	public void setMjOnPeng(List<JzMj> mjOnPeng) {
		this.mjOnPeng = mjOnPeng;
	}

	public List<JzMj> getMjOnBuZhang() {
		return mjOnBuZhang;
	}

	public void setMjOnBuZhang(List<JzMj> mjOnBuZhang) {
		this.mjOnBuZhang = mjOnBuZhang;
	}

	public List<JzMj> getMjOnChi() {
		return mjOnChi;
	}

	public void setMjOnChi(List<JzMj> mjOnChi) {
		this.mjOnChi = mjOnChi;
	}

	public JzMj getMajiang() {
		return majiang;
	}

	public void setMajiang(JzMj majiang) {
		this.majiang = majiang;
	}
	
	public List<JzMj> getAllCards() {
		List<JzMj> allCards = new ArrayList<>();
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

	public List<JzMj> getGangCards() {
		List<JzMj> result = new ArrayList<>();
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
