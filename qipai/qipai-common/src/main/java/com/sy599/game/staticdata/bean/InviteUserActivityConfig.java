package com.sy599.game.staticdata.bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.sy599.game.staticdata.model.InviteRewardActivityBean;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.StringUtil;
import com.sy599.game.util.TimeUtil;

/**
 * 打筒子邀请好友送钻活动配置
 * @author
 */
public class InviteUserActivityConfig extends ActivityConfigInfo {

	private Date startDate; 
	private Date endDate;
	private int maxShowNum;
	private List<InviteRewardActivityBean> rewardDiam = new ArrayList<>();//奖励
	
	@Override
	public void configParamsAndRewards() {
		List<String> param = StringUtil.explodeToStringList(params, "#");
		if(param == null || param.isEmpty() || param.size() != 3){
			LogUtil.errorLog.info("打筒子邀请好友送钻活动配置异常");
		}
		this.startDate = new Date(TimeUtil.parseTimeInMillis(param.get(0)));
		this.endDate = new Date(TimeUtil.parseTimeInMillis(param.get(1)));
		this.maxShowNum = Integer.parseInt(param.get(2));
		String[] strArray = StringUtil.explodeToStringArray(rewards,"&");
		if(strArray == null || strArray.length == 0){
			LogUtil.errorLog.info("打筒子邀请好友送钻活动配置");
		}
		for(String str : strArray){
			List<Integer> reward = StringUtil.explodeToIntList(str, "#");
			if(reward == null || reward.isEmpty() || reward.size() != 3){
				continue;
			}
			InviteRewardActivityBean bean = new InviteRewardActivityBean();
			bean.setFriendNum(reward.get(0));
			bean.setType(reward.get(1));
			bean.setRewardNum(reward.get(2));
			getRewardDiam().add(bean);
		}
	}

	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public int getMaxShowNum() {
		return maxShowNum;
	}

	public List<InviteRewardActivityBean> getRewardDiam() {
		return rewardDiam;
	}

}
