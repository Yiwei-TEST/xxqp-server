package com.sy599.game.staticdata.bean;

import com.sy599.game.util.LogUtil;
import com.sy599.game.util.StringUtil;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author liuping
 * 砸金蛋活动配置
 */
public class SmashEggAcitivityConfig extends ActivityConfigInfo {

	/*--------------------砸金蛋活动达成任务类型----------------------*/
	/**
	 * 每日首次分享朋友（0/1）
	 */
	public static int type_daily_share = 1;
	/**
	 * 军团中进行3场牌局（0/3)
	 */
	public static int type_group_game = 2;
	/**
	 * 进行4场4人或8人牌局（0/4）
	 */
	public static int type_game_count = 3;
	/**
	 * 活动3次大赢家（0/3）
	 */
	public static int type_big_win = 4;
	
	/**
	 * 砸金蛋活动需要达成的任务 次数
	 */
	private Map<Integer, Integer> reachConditions;
	
	/**
     * 砸蛋奖励以及概率配置
     */
    private Map<Integer, Lottery> lotteries = new HashMap<>();
    
	@Override
	public void configParamsAndRewards() {
		reachConditions = new HashMap<>();
		if(!StringUtil.isBlank(params)) {
			String [] arr = params.split("\\|");
			for(String temp : arr) {
				String []tempArr = temp.split("&");
				int type = Integer.parseInt(tempArr[0]);
				int num = Integer.parseInt(tempArr[1]);
				reachConditions.put(type, num);
			}
		}
		List<String> list = StringUtil.explodeToStringList(rewards, ";");
		lotteries.clear();
		for (String valueStrs : list) {
			String[] values = valueStrs.split("&");
			Lottery lottery = new Lottery();
			int i = 0;
			lottery.setIndex(StringUtil.getIntValue(values, i++));
			lottery.setName(getValue(values, i++));
			lottery.setChance(StringUtil.getFloatValue(values, i++));
			lottery.setRoomCard(StringUtil.getIntValue(values, i++));
			lottery.setMaxNum(StringUtil.getIntValue(values, i++));
			String state = StringUtil.getValue(values, i++);
			lottery.setState(NumberUtils.isDigits(state) ? Integer.parseInt(state) : 1);
			lotteries.put(lottery.getIndex(), lottery);
		}
		if(reachConditions == null || reachConditions.isEmpty() || lotteries == null || lotteries.isEmpty()) {
			LogUtil.errorLog.info("砸金蛋活动配置异常");
		}
	}

	/**
     * getValue读取csv
     * @param values
     * @param index
     * @return
     */
    private static String getValue(String[] values, int index) {
        if (index >= values.length) {
            return "";
        }
        return values[index];
    }

	public Map<Integer, Integer> getReachConditions() {
		return reachConditions;
	}
	
	/**
	 * 是否已达成条件
	 * @param smashEggType 砸金蛋条件类型
	 * @param reachNum 已达成的次数
	 * @return
	 */
	public boolean isReach(int smashEggType, int reachNum) {
		if(!reachConditions.containsKey(smashEggType)) {
			return false;
		}
		int needNum = reachConditions.get(smashEggType);
		if(needNum >= reachNum) {
			return true;
		} else
			return false;
	}

	public Map<Integer, Lottery> getLotteries() {
		return lotteries;
	}

	public void setLotteries(Map<Integer, Lottery> lotteries) {
		this.lotteries = lotteries;
	}
	
}
