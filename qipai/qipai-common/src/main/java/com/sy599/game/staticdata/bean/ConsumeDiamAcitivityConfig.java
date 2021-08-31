/**
 * 
 */
package com.sy599.game.staticdata.bean;

import com.sy599.game.util.LogUtil;
import com.sy599.game.util.StringUtil;

import java.util.List;

/**
 * @author liuping
 * 开房送钻活动配置
 */
public class ConsumeDiamAcitivityConfig extends ActivityConfigInfo {

	/**
	 * 开房需要消耗的钻石数量档次（由低到高）
	 */
	private List<Integer> needConsumeDaimGrades;
	
	/**
	 * 开房获得的钻石档次（由低到高）
	 */
	private List<Integer> rewardDaimGrades;

	
	@Override
	public void configParamsAndRewards() {
		needConsumeDaimGrades = StringUtil.explodeToIntList(params, "&");
		rewardDaimGrades = StringUtil.explodeToIntList(rewards, "&");
		if(needConsumeDaimGrades == null || needConsumeDaimGrades.isEmpty() || rewardDaimGrades == null || rewardDaimGrades.isEmpty()) {
			LogUtil.errorLog.info("开房送钻活动配置异常");
		}
	}

	public List<Integer> getNeedConsumeDaimGrades() {
		return needConsumeDaimGrades;
	}
	
	/**
	 * 获取领取当前档次下需要已消耗的钻石数量
	 * @param curRewardGrade
	 * @return
	 */
	public int getNeedConsumeDaim(int curRewardGrade) {
		if(curRewardGrade == 0) {
			return needConsumeDaimGrades.get(0);
		} else {
			int index = rewardDaimGrades.indexOf(new Integer(curRewardGrade));
			return needConsumeDaimGrades.get(index);
		}
	}

	public List<Integer> getRewardDaimGrades() {
		return rewardDaimGrades;
	}
}
