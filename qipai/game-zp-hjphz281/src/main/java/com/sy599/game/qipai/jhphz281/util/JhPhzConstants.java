package com.sy599.game.qipai.jhphz281.util;

import com.sy599.game.GameServerConfig;
import com.sy599.game.util.DataMapUtil;
import com.sy599.game.util.ResourcesConfigsUtil;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class JhPhzConstants {
    /** 胡 **/
    public static final int action_hu = 1;
    /** 碰 **/
    public static final int action_peng = 2;
    /** 栽 **/
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
    /** 臭栽 **/
    public static final int action_chouzai = 10;
    /** 提龙 **/
    public static final int action_tilong = 11;
    /** 有动作 **/
    public static final int action_hasaction = 12;
    /** 刷新 **/
    public static final int action_refreshaction = 13;
    /** 摸王 **/
    public static final int action_mowang = 14;
    /** 王钓 **/
    public static final int action_wangdiao = 15;
    /** 王闯 **/
    public static final int action_wangchuang = 16;
    /** 王钓王 **/
    public static final int action_wangdiaowang = 17;
    /** 王闯王 **/
    public static final int action_wangchuangwang = 18;
    /** 王炸 **/
    public static final int action_wangzha = 19;
    /** 王炸王 **/
    public static final int action_wangzhawang = 20;

    /** 动作 **/
    public static final int action_type_action = 0;
    /** 摸 **/
    public static final int action_type_mo = 1;
    /** 出牌 **/
    public static final int action_type_dis = 2;
    /**清除按钮**/
    public static final int action_type_clear = 3;

    /** 后台自动出牌 **/
    public static final int action_type_dis_auto = 4;
    
    
    /**托管**/
    public static final int action_tuoguan = 100;
    /** 过，用于日志记录**/
    public static final int action_guo= 101;
    /**
     * 胡牌次数
     */
    public static final int ACTION_COUNT_INDEX_HU = 0;
    /**
     * 自摸次数
     */
    public static final int ACTION_COUNT_INDEX_ZIMO = 1;
    /**
     * 提次数
     */
    public static final int ACTION_COUNT_INDEX_TI = 2;
    /**
     * 跑次数
     */
    public static final int ACTION_COUNT_INDEX_PAO = 3;

    /** 优先list ------提，栽，胡，跑，碰，吃 **/
    public static final List<Integer> priority_action = Arrays.asList(action_ti, action_zai, action_chouzai,action_hu,action_pao, action_peng, action_chi,action_wangdiao,action_wangchuang,action_wangzha);

    /** 永州扯胡子 **/
    public static final int play_type_3_2_yongzhou = 37;
    public static final int play_type_3_4_yongzhou = 38;
    public static final int play_type_4_2_yongzhou = 35;
    public static final int play_type_4_4_yongzhou = 36;
    public static final int play_type_3_3_yongzhou = 39;
    public static final int play_type_4_3_yongzhou = 40;
    public static final int play_type_2_2_yongzhou = 41;
    public static final int play_type_2_3_yongzhou = 42;
    public static final int play_type_2_4_yongzhou = 43;

    /** 祁阳六胡抢 **/
    public static final int play_type_2_0_six = 44;
    public static final int play_type_2_1_six = 45;
    public static final int play_type_2_2_six = 46;
    public static final int play_type_3_0_six = 47;
    public static final int play_type_3_1_six = 48;
    public static final int play_type_3_2_six = 49;
    public static final int play_type_4_0_six = 50;
    public static final int play_type_4_1_six = 51;
    public static final int play_type_4_2_six = 52;

    public static boolean isPlayYzPhz(int playType) {
        return playType >= 35 && playType <= 43;
    }

    public static boolean isPlaySixPhz(int playType) {
        return playType >= 44 && playType <= 52;
    }

    /**
     * 托管时间15s
     */
    public static long AUTO_TIME = NumberUtils.toLong(ResourcesConfigsUtil.loadServerPropertyValue("gold_robot_play"),15 * 1000);

    /**
     * 托管操作时间3s
     */
    public static long AUTO_PLAY_TIME = NumberUtils.toLong(ResourcesConfigsUtil.loadServerPropertyValue("gold_auto_play"),2 * 1000);

    /**
     * 牌转化为Id
     *
     * @param phzs
     * @return
     */
    public static List<Integer> toPhzCardZeroIds(List<Integer> phzs) {
        if (phzs == null || phzs.size()==0) {
            return Collections.emptyList();
        }
        List<Integer> ids = new ArrayList<>(phzs.size());
        for (int i = 0; i < phzs.size(); i++) {
            ids.add(0);
        }
        return ids;
    }

    /**
     * 优先的动作
     *
     * @param action
     * @return
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
    
	public static String renameAction(int action) {
		String str = "";
		switch (action) {
		case 0:
			str = "出";
			break;
		case action_hu:
			str = "胡";
			break;
		case action_peng:
			str = "碰";
			break;
		case action_zai:
			str = "摘";
			break;
		case action_ti:
			str = "提";
			break;
		case action_chi:
			str = "吃";
			break;
		case action_mo:
			str = "摸";
			break;
		case action_wangdiao:
		case action_wangdiaowang:
		case action_wangzha:
		case action_wangzhawang:
			str = "摸";
			break;

		default:
			break;
		}
		if(str==""){
			str=action+"";
		}
		return str;

	}
    


    public static List<Integer> parseToDisActionList(List<Integer> actionList,boolean isSelf) {
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
        if (actionList.get(7) == 1) {
            disActionList.add(action_wangdiao);
            if (!isSelf){
                disActionList.remove(Integer.valueOf(action_hu));
            }
        }
        if (actionList.get(8) == 1) {
            disActionList.add(action_wangchuang);
            if (!isSelf){
                disActionList.remove(Integer.valueOf(action_hu));
            }
        }
        if (actionList.get(9) == 1) {
//            disActionList.add(action_wangzha);
//            if (!isSelf){
//                disActionList.remove(Integer.valueOf(action_hu));
//            }
        }
        return disActionList;
    }

    public static boolean canDis(List<Integer> stopActionList, List<Integer> actionList,boolean isSelf) {
        if (stopActionList == null) {
            return true;
        }
        List<Integer> disActionList = parseToDisActionList(actionList,isSelf);
        for (int stop : stopActionList) {
            if (disActionList.contains(stop)) {
                return false;
            }
        }
        return true;
    }

}
