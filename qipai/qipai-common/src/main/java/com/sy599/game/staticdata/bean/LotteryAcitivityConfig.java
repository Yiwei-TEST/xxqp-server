package com.sy599.game.staticdata.bean;

import com.sy599.game.util.StringUtil;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * @author liuping
 * 幸运转盘活动配置
 */
public class LotteryAcitivityConfig extends ActivityConfigInfo {

	/**
	 * 开启军团是否增加一次转动次数  改成了玩家累计消耗钻石数送一次抽奖次数
	 */
	private int openGroupReward;
	
    /**
     * 幸运转盘 转盘奖励以及概率配置
     */
    private List<Lottery> lotteries = new ArrayList<>();

	@Override
	public void configParamsAndRewards() {
		openGroupReward = Integer.parseInt(params);
		List<String> list = StringUtil.explodeToStringList(rewards, ";");
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
			lotteries.add(lottery);
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

	
	public int getOpenGroupReward() {
		return openGroupReward;
	}

	public void setOpenGroupReward(int openGroupReward) {
		this.openGroupReward = openGroupReward;
	}

	public List<Lottery> getLotteries() {
		return lotteries;
	}

	public void setLotteries(List<Lottery> lotteries) {
		this.lotteries = lotteries;
	}
}
