package com.sy599.game.qipai.yzlc.bean;

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
	/** 单牌 **/
	public static final int action_single_card = 2;
	/** 对牌 **/
	public static final int action_double_card = 3;
	/** 大面 **/
	public static final int action_big_face = 4;
	/** pass **/
	public static final int action_pass = 5;
	/** 小面 **/
	public static final int action_small_face = 6;
	/** 食盒 **/
	public static final int action_eat_box = 7;
	/** 坎 **/
	public static final int action_kan = 8;
	/** 摸牌 **/
	public static final int action_mo = 9;
	/** 龙 **/
	public static final int action_dragon = 10;
	/** 顺子 **/
	public static final int action_straight = 11;
	/** 有动作 **/
	public static final int action_hasaction = 12;
	/** 刷新 **/
	public static final int action_refreshaction = 13;

    /** 提龙补牌**/
    public static final int action_buPai= 15;
	/** 过，用于日志记录**/
	public static final int action_guo= 101;
	/** 优先list ------提，栽，胡，跑，碰，吃 **/
	public static final List<Integer> priority_action = Arrays.asList(action_big_face, action_double_card, action_dragon, action_hu, action_eat_box, action_single_card, action_small_face);
	// 0胡 1碰 2栽 3提 4吃 5跑
	// ////////////////////////////////////////////////////////
	/** 动作 **/
	public static final int action_type_action = 0;
	/** 摸 **/
	public static final int action_type_mo = 1;
	/** 出牌 **/
	public static final int action_type_dis = 2;

	//要不起
	public static final int action_type_min = 20;

	//初始戳子
	public static final int action_type_init_combo = 21;


	/** 托管出牌 **/
	public static final int action_type_autoplaydis = 4;

	public static int getMaxPriorityAction(List<Integer> actions) {
		for (int pri_ation : priority_action) {
			if(actions.contains(pri_ation))
				return pri_ation;
		}
		return action_small_face;
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
		// 0胡,1单牌,2对子,3大面,4小面,5食盒,6坎,7龙,8顺子
		if (actionList == null || actionList.isEmpty()) {
			return disActionList;
		}
		if (actionList.get(0) == 1) {
			disActionList.add(action_hu);
		}
		if (actionList.get(1) == 1) {
			disActionList.add(action_single_card);
		}
		if (actionList.get(2) == 1) {
			disActionList.add(action_double_card);
		}
		if (actionList.get(3) == 1) {
			disActionList.add(action_big_face);
		}
		if (actionList.get(4) == 1) {
			disActionList.add(action_small_face);
		}
		if (actionList.get(5) == 1) {
			disActionList.add(action_eat_box);
		}
		if (DataMapUtil.getIntValue(actionList, 6) == 1) {
			disActionList.add(action_kan);
		}
		if (DataMapUtil.getIntValue(actionList, 7) == 1) {
			disActionList.add(action_dragon);
		}
		if (DataMapUtil.getIntValue(actionList, 8) == 1) {
			disActionList.add(action_straight);
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
        actionNameMap.put(action_single_card, "peng");
        actionNameMap.put(action_double_card, "zai");
        actionNameMap.put(action_big_face, "ti");
        actionNameMap.put(action_pass, "guo");
        actionNameMap.put(action_small_face, "chi");
        actionNameMap.put(action_eat_box, "pao");
        actionNameMap.put(action_kan, "kan");
        actionNameMap.put(action_mo, "mo");
        actionNameMap.put(action_dragon, "chouzai");
        actionNameMap.put(action_straight, "tilong");
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
