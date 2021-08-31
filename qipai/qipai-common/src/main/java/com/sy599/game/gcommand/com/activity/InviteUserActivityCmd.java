package com.sy599.game.gcommand.com.activity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.staticdata.bean.ActivityConfig;
import com.sy599.game.staticdata.bean.InviteUserActivityConfig;
import com.sy599.game.staticdata.model.InviteRewardActivityBean;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.netty.coder.MessageUnit;

/**
 * 打筒子邀请好友送钻活动
 */
public class InviteUserActivityCmd extends BaseCommand {

	@Override
	public void execute(Player player, MessageUnit message) throws Exception {
		ComReq req = (ComReq) this.recognize(ComReq.class, message);
	    List<Integer> reqParams = req.getParamsList();
	    int requestType = reqParams.get(0);// params参数第0位 表示请求的操作类型 0打开 1领取
	    if(requestType != 0 && requestType != 1){
	    	player.writeErrMsg(LangMsg.code_3);
	    	return;
	    }
	    InviteUserActivityConfig config = (InviteUserActivityConfig) ActivityConfig.getActivityConfigInfo(ActivityConfig.activity_invite_user_zuan);
	    Date startTime = config.getStartDate();// 活动开始统计时间
	    Date endTime = config.getEndDate();
	    int maxShowNum = config.getMaxShowNum();//最大显示人数
	    List<InviteRewardActivityBean> rewardDiam = config.getRewardDiam();//奖励配置
	    MyActivity myactivity = player.getMyActivity();
	    UserActivityRecord record = myactivity.getUserActivityRecord();//已领取记录
	    List<Map> inviteActivityUser = UserDao.getInstance().getInviteUserActivity(startTime, endTime, player.getUserId(), record.getInviteUserPage(), maxShowNum);
	    int InviteNum = inviteActivityUser.size();//本页邀请人数
	    if(requestType == 1){//领奖励
	    	int rewardIndex = reqParams.get(1);//领取奖励index 从0开始
	    	if(rewardIndex>=rewardDiam.size() || rewardIndex<0){
	    		player.writeErrMsg(LangMsg.code_3);
	    		return;
	    	}
	    	if(record.getInviteUserReward().contains(rewardIndex)){
	    		player.writeErrMsg("已领取过奖励！");
	    		return;
	    	}
	    	InviteRewardActivityBean inviteRewardActivityBean = rewardDiam.get(rewardIndex);
	    	if(InviteNum < inviteRewardActivityBean.getFriendNum()){
	    		player.writeErrMsg("条件不满足，领取失败！"); 
	    		return;
	    	}
	    	LogUtil.msgLog.info(player.getName() + "玩家领取打筒子邀请领钻活动 rewardIndex"+rewardIndex+" 奖励" + inviteRewardActivityBean.getRewardNum());
	    	ActivityReward activityReward = new ActivityReward();
	    	activityReward.setActivityId(ActivityConfig.activity_invite_user_zuan);
	    	activityReward.setUserId(player.getUserId());
	    	activityReward.setType(1);//钻石
	    	activityReward.setState(1);
	    	activityReward.setRewardIndex(rewardIndex);
	    	activityReward.setRewardDate(new Date());
	    	activityReward.setRewardNum(inviteRewardActivityBean.getRewardNum());
	    	player.changeCards(inviteRewardActivityBean.getRewardNum(), 0, true, CardSourceType.activity_inviteUser);
    		activityReward.setReward("钻石X"+inviteRewardActivityBean.getRewardNum());
    		ActivityDao.getInstance().addActivityReward(activityReward);
    		record.getInviteUserReward().add(rewardIndex);
    		myactivity.updateActivityRecord(record);
	    }
	    List<Integer> recordState = new ArrayList<>();
	    int i = 0,rewardNum = 0;
	    for(InviteRewardActivityBean bean : rewardDiam){
	    	rewardNum += bean.getRewardNum();
	    	if(bean.getFriendNum() > InviteNum){
	    		recordState.add(0);
	    	}else if(record.getInviteUserReward().contains(i)){
	    		recordState.add(1);
	    	}else{
	    		recordState.add(2);
	    	}
	    	i++;
	    }
	    JSONObject userJsonInfo = new JSONObject();
	    userJsonInfo.put("rewardNum", rewardNum);
	    userJsonInfo.put("maxShowNum", maxShowNum);
	    userJsonInfo.put("inviteActivityUser", inviteActivityUser);
	    userJsonInfo.put("rewardDiam", rewardDiam);
	    userJsonInfo.put("recordState", recordState);
	    userJsonInfo.put("requestType", requestType);
	    ActivityCommand.sendActivityInfo(player, config, userJsonInfo);
	    if(requestType == 1 && record.getInviteUserReward().size() == rewardDiam.size()){
	    	record.getInviteUserReward().clear();
	    	record.setInviteUserPage(record.getInviteUserPage() + 1);
	    	myactivity.updateActivityRecord(record);
	    }
	}

	@Override
	public void setMsgTypeMap() {
	}

}
