package com.sy599.game.qipai.hyshk.been;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 需要特别注意，不同字牌的动作号相同可能会互相影响，新增动作的时候需要关注一下其他字牌玩法的动作是否重叠
 */
public class PaohuziDisAction {
	/** 胡 **/
	public static final int action_hu = 1;
	/** 碰 **/
	public static final int action_peng = 2;
	/** 杠 **/
	public static final int action_minggang = 3;
	/** 暗杠 **/
	public static final int action_angang = 4;
	/** pass **/
	public static final int action_pass = 5;
	/** 吃 **/
	public static final int action_chi = 6;
	/** 补张 **/
	public static final int action_buzhang = 7;
	/** 小胡 **/
	public static final int action_xiaohu = 8;
	/** 摸麻将**/
	public static final int action_moMjiang = 9;
	/** 海底捞时摸麻将**/
	public static final int action_moLastMjiang = 10;
	/** 杠摸麻将**/
	public static final int action_moGangMjiang = 11;
	/** 打牌时候有动作**/
	public static final int action_hasAction= 12;

	public static final int action_mo=1;
	public static final int action_passmo=2;

	private int round;
	private int seat;
	private long userId;
	private int majiangId;

	public int getSeat() {
		return seat;
	}

	public void setSeat(int seat) {
		this.seat = seat;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public int getMajiangId() {
		return majiangId;
	}

	public void setMajiangId(int majiangId) {
		this.majiangId = majiangId;
	}

	public int getRound() {
		return round;
	}

	public void setRound(int round) {
		this.round = round;
	}

	/**
	 * 优先的动作
	 * @param action
	 * @return
	 */
	public static List<Integer> findPriorityAction(int action) {
		List<Integer> findActionList = null;
		if (action == PaohuziDisAction.action_chi) {
			findActionList = Arrays.asList(PaohuziDisAction.action_hu, PaohuziDisAction.action_minggang, PaohuziDisAction.action_buzhang, PaohuziDisAction.action_peng);

		} else if (action == PaohuziDisAction.action_peng) {
			findActionList = Arrays.asList(PaohuziDisAction.action_hu, PaohuziDisAction.action_minggang, PaohuziDisAction.action_buzhang);

		} else if (action == PaohuziDisAction.action_buzhang) {
			findActionList = Arrays.asList(PaohuziDisAction.action_hu, PaohuziDisAction.action_minggang);

		} else if (action == PaohuziDisAction.action_minggang) {
			findActionList = Arrays.asList(PaohuziDisAction.action_hu);

		}
		return findActionList;
	}

	public static List<Integer> parseToDisActionList(List<Integer> actionList) {
		List<Integer> disActionList = new ArrayList<>();
		// 0胡 1碰 2明刚 3暗杠(暗杠后来不需要了 暗杠也用3标记)4吃5补张
		if (actionList.isEmpty()) {
			return disActionList;
		}
		if (actionList.get(0) == 1) {
			disActionList.add(action_hu);
		}
		if (actionList.get(1) == 1) {
			disActionList.add(action_peng);
		}
		if (actionList.get(2) == 1) {
			disActionList.add(action_minggang);
		}
		if (actionList.get(3) == 1) {
			disActionList.add(action_angang);
		}
		if (actionList.get(4) == 1) {
			disActionList.add(action_chi);
		}
		if (actionList.get(5) == 1) {
			disActionList.add(action_buzhang);
		}
		return disActionList;
	}

	public static boolean canDisMajiang(List<Integer> stopActionList, List<Integer> actionList) {
		if(stopActionList==null){
			return true;
		}
		List<Integer> disActionList = parseToDisActionList(actionList);
		for (int stop : stopActionList) {
			if (disActionList.contains(stop)) {
				return false;
			}
		}
		return true;
	}

}
