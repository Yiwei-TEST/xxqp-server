package com.sy599.game.qipai.hsphz.bean;

import com.sy599.game.util.DataMapUtil;
import com.sy599.game.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class PaohzDisAction {
	/** 胡 **/
	public static final int action_hu = 1;
	/** 碰 **/
	public static final int action_peng = 2;
	/** 栽/偎 **/
	public static final int action_zai = 3;
	/** 提 **/
	public static final int action_ti = 4;
	/** pass **/
	public static final int action_pass = 5;
	/** 吃 **/
	public static final int action_chi = 6;
	/** 跑 **/
	public static final int action_pao = 7;
	/** 坎 **/
	public static final int action_kan = 8;
	/** 摸牌 **/
	public static final int action_mo = 9;
	/** 臭栽/偎 **/
	public static final int action_chouzai = 10;
	/** 提龙 **/
	public static final int action_tilong = 11;
	/** 有动作 **/
	public static final int action_hasaction = 12;
	/** 刷新 **/
	public static final int action_refreshaction = 13;

    /** 提龙补牌**/
    public static final int action_buPai= 15;
	
	/** 优先list ------提，栽，胡，跑，碰，吃 **/
	public static final List<Integer> priority_action = Arrays.asList(action_ti, action_zai, action_chouzai, action_hu, action_pao, action_peng, action_chi);
	// 0胡 1碰 2栽 3提 4吃 5跑
	// ////////////////////////////////////////////////////////
	/** 动作 **/
	public static final int action_type_action = 0;
	/** 摸 **/
	public static final int action_type_mo = 1;
	/** 出牌 **/
	public static final int action_type_dis = 2;
	/** 托管出牌 **/
	public static final int action_type_autoplaydis = 4;

	public static int getMaxPriorityAction(List<Integer> actions) {
		for (int pri_ation : priority_action) {
			if(actions.contains(pri_ation))
				return pri_ation;
		}
		return action_chi;
	}
	/**
	 * 优先的动作
	 */
	public static List<Integer> findPriorityAction(int action) {
		List<Integer> findActionList = new ArrayList<>();
		for (int pri_ation : priority_action) {
			if (action == pri_ation) {
				break;
			}
			findActionList.add(pri_ation);
		}
		return findActionList;
	}

	public static List<Integer> getPriorityAction(Map<Integer, List<Integer>> actionMap, int nowSeat) {
		for (Entry<Integer, List<Integer>> entry : actionMap.entrySet()) {
			List<Integer> list = parseToDisActionList(entry.getValue());
		}
		return null;
	}

	// public static List<Integer>

	public static List<Integer> parseToDisActionList(List<Integer> actionList) {
		List<Integer> disActionList = new ArrayList<>();
		// 0胡 1碰 2明刚 3暗杠(暗杠后来不需要了 暗杠也用3标记)4吃5补张
		if (actionList == null || actionList.isEmpty()) {
			return disActionList;
		}
		if (actionList.get(0) == 1) {
			disActionList.add(action_hu);
		}
		if (actionList.get(1) == 1) {
			disActionList.add(action_peng);
		}
		if (actionList.get(2) == 1) {
			disActionList.add(action_zai);
		}
		if (actionList.get(3) == 1) {
			disActionList.add(action_ti);
		}
		if (actionList.get(4) == 1) {
			disActionList.add(action_chi);
		}
		if (actionList.get(5) == 1) {
			disActionList.add(action_pao);
		}
		if (DataMapUtil.getIntValue(actionList, 6) == 1) {
			disActionList.add(action_chouzai);
		}
		return disActionList;
	}

	public static boolean canDis(List<Integer> stopActionList, List<Integer> actionList) {
		if (stopActionList == null) {
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


    private static final Map<Integer,String> actionNameMap = new HashMap<>();
    static {
        actionNameMap.put(action_hu, "hu");
        actionNameMap.put(action_peng, "peng");
        actionNameMap.put(action_zai, "zai");
        actionNameMap.put(action_ti, "ti");
        actionNameMap.put(action_pass, "guo");
        actionNameMap.put(action_chi, "chi");
        actionNameMap.put(action_pao, "pao");
        actionNameMap.put(action_kan, "kan");
        actionNameMap.put(action_mo, "mo");
        actionNameMap.put(action_chouzai, "chouzai");
        actionNameMap.put(action_tilong, "tilong");
        actionNameMap.put(action_hasaction, "hasaction");
        actionNameMap.put(action_refreshaction, "refreshaction");
        actionNameMap.put(0, "chuPai");
    }
    public static String getActionName(int action) {
        String name = actionNameMap.get(action);
        if (StringUtil.isBlank(name)) {
            name = String.valueOf(action);
        }
        return name;
    }

}
