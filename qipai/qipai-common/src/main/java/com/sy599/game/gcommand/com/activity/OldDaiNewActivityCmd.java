package com.sy599.game.gcommand.com.activity;

import com.alibaba.fastjson.JSONObject;
import com.sy599.game.activity.MyActivity;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.db.bean.activityRecord.UserActivityRecord;
import com.sy599.game.db.dao.UserDao;
import com.sy599.game.db.enums.CardSourceType;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.gcommand.com.ActivityCommand;
import com.sy599.game.util.LangHelp;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.staticdata.bean.ActivityConfig;
import com.sy599.game.staticdata.bean.OldDaiNewAcitivityConfig;
import com.sy599.game.util.LogUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author liuping
 * 老带新活动处理类
 */
public class OldDaiNewActivityCmd extends BaseCommand {

	@Override
	public void execute(Player player, MessageUnit message) throws Exception {
		ComReq req = (ComReq) this.recognize(ComReq.class, message);
	    List<Integer> reqParams = req.getParamsList();
	    int requestType = reqParams.get(0);// params参数第0位 表示请求的操作类型 0打开 1领取
	    OldDaiNewAcitivityConfig config = (OldDaiNewAcitivityConfig) ActivityConfig.getActivityConfigInfo(ActivityConfig.activity_old_dai_new);
		Date startTime = config.getStartTime();// 活动开始时间
		int rewardDiam = config.getRewardDiam();// 单次下载获得的钻石数
		int maxRewardDiam = config.getMaxRewardDiam();// 每日奖励钻石上限
		int maxCount = maxRewardDiam / rewardDiam;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd");
		String todayZeroTimeStr = dataFormat.format(new Date()) + " 00:00:00";
		Date todayZeroTime = sdf.parse(todayZeroTimeStr);
		List<Map> todayDownloads = UserDao.getInstance().getDownloadData(todayZeroTime, new Date(), player.getUserId());// 获得今日累计下载
		List<Map> totalDownloads = UserDao.getInstance().getDownloadData(startTime, new Date(), player.getUserId());// 获得累计下载
		MyActivity myactivity = player.getMyActivity();
		UserActivityRecord record = myactivity.getUserActivityRecord();
        int todayPay = (todayDownloads.size() > maxCount  ? maxCount : todayDownloads.size()) * rewardDiam;// 今日钻石  
        // 当天邀请下载登录的新玩家人数乘以钻石数量（100钻），当日有效钻石数量上限1000钻，超过上限的不计入到累加钻石里面，每日的都累积加到一起。
        Map<String, Integer> dayDownLoads = new HashMap<>();
        int accCount = 0;// 累计钻石的下载次数
        for (Object map : totalDownloads) {
            Date date = (Date) ((Map) map).get("regTime");
            String dateStr = dataFormat.format(date);
            if(dayDownLoads.containsKey(dateStr)) {
            	int curCount = dayDownLoads.get(dateStr);
            	if(curCount < maxCount) {// 每天最多领取下载钻石次数 10次
            		accCount ++;
            		dayDownLoads.put(dateStr, curCount + 1);
            	}
            } else {
            	dayDownLoads.put(dateStr, 1);
            	accCount ++;
            }
        }
        int accDiam = accCount * rewardDiam;// 累计钻石
        int payCount = record.getOldDaiNewReceiveDiam();// 玩家已领取钻石数
        int payRest = accDiam - payCount;// 可领取钻石  = 累计钻石 - 玩家已领取钻石  
    	if(requestType == 1) {// 领取玩家开房送钻档次
			if (payRest <= 0) { 
				player.writeErrMsg(LangHelp.getMsg(LangMsg.code_231));
			    return;
			}
			LogUtil.msgLog.info(player.getName() + "玩家领取老带新活动钻石*" + payRest);
			record.setOldDaiNewReceiveDiam(record.getOldDaiNewReceiveDiam() + payRest);
			player.changeCards(payRest, 0, true, CardSourceType.activity_oldDaiNew);
			payRest = 0;
			myactivity.updateActivityRecord(record);
    	}
    	JSONObject userJsonInfo = new JSONObject();
    	userJsonInfo.put("todayDownLoad", todayDownloads.size());// 今日下载
    	userJsonInfo.put("totalDownLoad", totalDownloads.size());// 累计下载
    	userJsonInfo.put("canReceiveDiam", payRest);// 可领取钻石
    	userJsonInfo.put("todayDiam", todayPay);// 今日钻石
    	userJsonInfo.put("accDiam", accDiam);// 累计钻石
        ActivityCommand.sendActivityInfo(player, config, userJsonInfo);
	}
	
	@Override
	public void setMsgTypeMap() {
	}
}
