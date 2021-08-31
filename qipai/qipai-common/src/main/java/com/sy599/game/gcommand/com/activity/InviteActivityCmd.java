package com.sy599.game.gcommand.com.activity;

import com.alibaba.fastjson.JSONObject;
import com.sy599.game.activity.MyActivity;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.db.bean.activityRecord.ActivityReward;
import com.sy599.game.db.bean.activityRecord.UserActivityRecord;
import com.sy599.game.db.dao.ActivityDao;
import com.sy599.game.db.dao.UserDao;
import com.sy599.game.db.enums.CardSourceType;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.gcommand.com.ActivityCommand;
import com.sy599.game.util.LangHelp;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.staticdata.bean.ActivityConfig;
import com.sy599.game.staticdata.bean.InviteActivityConfig;
import com.sy599.game.staticdata.model.InviteRewardActivityBean;
import com.sy599.game.util.LogUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 打筒子邀请活动
 * @author admin
 */
public class InviteActivityCmd extends BaseCommand {

	@Override
	public void execute(Player player, MessageUnit message) throws Exception {
        ComReq req = (ComReq) this.recognize(ComReq.class, message);
	    List<Integer> reqParams = req.getParamsList();
	    int requestType = reqParams.get(0);// params参数第0位 表示请求的操作类型 0打开 1领取
	    if(requestType != 0 && requestType != 1){
	    	player.writeErrMsg(LangHelp.getMsg(LangMsg.code_3));
	    	return;
	    }
	    InviteActivityConfig config = (InviteActivityConfig) ActivityConfig.getActivityConfigInfo(ActivityConfig.activity_invite_user);
	    Date startTime = config.getStartDate();// 活动开始统计时间
	    Date endTime = config.getEndDate();
	    int maxInviteNum = config.getMaxInviteNum();//最大邀请人数
	    int playNum = config.getPlayNum();//要玩的盘数
	    List<InviteRewardActivityBean> rewardDiam = config.getRewardDiam();//奖励配置
	    List<Map> inviteActivityUser = UserDao.getInstance().getInviteActivityUser(startTime, endTime, player.getUserId(), maxInviteNum, playNum);
	    int InviteNum = inviteActivityUser.size();//邀请人数
	    MyActivity myactivity = player.getMyActivity();
	    UserActivityRecord record = myactivity.getUserActivityRecord();//已领取记录
	    if(requestType == 1){//领奖励
	    	int rewardIndex = reqParams.get(1);//领取奖励index 从0开始
	    	if(rewardIndex>=rewardDiam.size() || rewardIndex<0){
	    		player.writeErrMsg(LangHelp.getMsg(LangMsg.code_3));
	    		return;
	    	}
	    	if(record.getInviteReward().contains(rewardIndex)){
	    		player.writeErrMsg(LangHelp.getMsg(LangMsg.code_219));
	    		return;
	    	}
	    	InviteRewardActivityBean inviteRewardActivityBean = rewardDiam.get(rewardIndex);
	    	if(InviteNum < inviteRewardActivityBean.getFriendNum()){
	    		player.writeErrMsg(LangHelp.getMsg(LangMsg.code_220));
	    		return;
	    	}
	    	LogUtil.msgLog.info(player.getName() + "玩家领取打筒子邀请活动*类型" + inviteRewardActivityBean.getType() + " rewardIndex"+rewardIndex+" 奖励" + inviteRewardActivityBean.getRewardNum());
	    	ActivityReward activityReward = new ActivityReward();
	    	activityReward.setActivityId(ActivityConfig.activity_invite_user);
	    	activityReward.setUserId(player.getUserId());
	    	activityReward.setType(inviteRewardActivityBean.getType());
	    	activityReward.setState(1);
	    	activityReward.setRewardIndex(rewardIndex);
	    	activityReward.setRewardDate(new Date());
	    	activityReward.setRewardNum(inviteRewardActivityBean.getRewardNum());
	    	if(inviteRewardActivityBean.getType()==1){//1钻石2现金
	    		player.changeCards(inviteRewardActivityBean.getRewardNum(), 0, true, CardSourceType.activity_invite);
	    		activityReward.setReward("钻石X"+inviteRewardActivityBean.getRewardNum());
	    	}else{
	    		activityReward.setReward("现金X"+inviteRewardActivityBean.getRewardNum());
	    	}
	    	ActivityDao.getInstance().addActivityReward(activityReward);
	    	record.getInviteReward().add(rewardIndex);
	    	myactivity.updateActivityRecord(record);
	    }
	    List<Integer> recordState = new ArrayList<>();
	    int i = 0;
	    for(InviteRewardActivityBean bean : rewardDiam){
	    	if(bean.getFriendNum() > InviteNum){
	    		recordState.add(0);
	    	}else if(record.getInviteReward().contains(i)){
	    		recordState.add(1);
	    	}else{
	    		recordState.add(2);
	    	}
	    	i++;
	    }
	    JSONObject userJsonInfo = new JSONObject();
	    userJsonInfo.put("maxInviteNum", maxInviteNum);
	    userJsonInfo.put("playNum", playNum);
	    userJsonInfo.put("inviteActivityUser", inviteActivityUser);
	    userJsonInfo.put("rewardDiam", rewardDiam);
	    userJsonInfo.put("recordState", recordState);
	    userJsonInfo.put("requestType", requestType);
	    ActivityCommand.sendActivityInfo(player, config, userJsonInfo);
	}

	@Override
	public void setMsgTypeMap() {
	}

}
