package com.sy599.game.staticdata.bean;

import com.sy599.game.staticdata.model.InviteRewardActivityBean;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.StringUtil;
import com.sy599.game.util.TimeUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 打筒子邀请活动配置
 * @author admin
 */
public class InviteActivityConfig extends ActivityConfigInfo {
	
	private int maxInviteNum;//最大邀请人数
	private int playNum;//要玩的盘数
	private Date startDate; 
	private Date endDate;
	private List<InviteRewardActivityBean> rewardDiam = new ArrayList<>();//奖励

	@Override
	public void configParamsAndRewards() {
		List<String> param = StringUtil.explodeToStringList(params, "#");
		if(param == null || param.isEmpty() || param.size() != 4){
			LogUtil.errorLog.info("打筒子邀请活动配置异常");
		}
		this.maxInviteNum = Integer.parseInt(param.get(0));
		this.playNum = Integer.parseInt(param.get(1));
		this.startDate = new Date(TimeUtil.parseTimeInMillis(param.get(2)));
		this.endDate = new Date(TimeUtil.parseTimeInMillis(param.get(3)));
		String[] strArray = StringUtil.explodeToStringArray(rewards,"&");
		if(strArray == null || strArray.length == 0){
			LogUtil.errorLog.info("打筒子邀请活动配置异常");
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
	
	public int getMaxInviteNum() {
		return maxInviteNum;
	}

	public int getPlayNum() {
		return playNum;
	}

	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public List<InviteRewardActivityBean> getRewardDiam() {
		return rewardDiam;
	}

}
