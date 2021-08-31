package com.sy599.game.gcommand.com.activity;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sy599.game.activity.MyActivity;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.db.bean.UserMessage;
import com.sy599.game.db.bean.activityRecord.UserActivityRecord;
import com.sy599.game.db.dao.UserDao;
import com.sy599.game.db.dao.UserMessageDao;
import com.sy599.game.db.enums.CardSourceType;
import com.sy599.game.db.enums.UserMessageEnum;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.gcommand.com.ActivityCommand;
import com.sy599.game.message.MessageUtil;
import com.sy599.game.util.LangHelp;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.staticdata.bean.ActivityConfig;
import com.sy599.game.staticdata.bean.Lottery;
import com.sy599.game.staticdata.bean.SmashEggAcitivityConfig;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.Probability;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author liuping
 * 砸金蛋活动处理类
 */
public class SmashEggActivityCmd extends BaseCommand {

	/**
	 * 每日最多可砸蛋的次数
	 */
	private static int max_smash_egg_times = 3;
	
	@Override
	public void execute(Player player, MessageUnit message) throws Exception {
		ComReq req = (ComReq) this.recognize(ComReq.class, message);
	    List<Integer> reqParams = req.getParamsList();
        MyActivity myactivity = player.getMyActivity();
        long userId = player.getUserId();
        int playType = 0;// 玩法ID
		UserActivityRecord record = myactivity.getUserActivityRecord();
		SmashEggAcitivityConfig config = (SmashEggAcitivityConfig) ActivityConfig.getActivityConfigInfo(ActivityConfig.activity_smash_egg);
		int requestType = reqParams.get(0);// params参数第0位 表示请求的操作类型 0打开 1领取
		JSONObject userJsonObj = getUserJson(config, record);
		if(requestType == 1) {
			if(record.getSmashEggTimes() >= max_smash_egg_times) {
				player.writeErrMsg(LangHelp.getMsg(LangMsg.code_237));
    			return;
			}			
			int canSmashEggTimes = getCanSmashEggTimes(config, record);
			if(canSmashEggTimes <= 0) {
				player.writeErrMsg(LangHelp.getMsg(LangMsg.code_238));
    			return;
			}
			Map<Integer, Lottery> lotteries = config.getLotteries();
			int lotteryIndex = randomLotteryIndex(lotteries);
			Lottery lottery = lotteries.get(lotteryIndex);
			Map<String, Object> userMap = new HashMap<>();
			int cards = lottery.getRoomCard();
			userMap.put("cards", cards);
			UserDao.getInstance().addPrize(userId, lottery.getName(), lotteryIndex);
			if (cards > 0) {
				player.changeCards(cards, 0, false, playType, CardSourceType.activity_smashEgg);
			}
			LogUtil.msgLog.info(player.getName() + "砸蛋获得:" + lottery.getName());

			MessageUtil.sendMessage(false,true, UserMessageEnum.TYPE2,player,"砸蛋获得:" + lottery.getName(),player.getName());
			record.alterSmashEggTimes(1);// 增加一次已砸蛋次数 
			myactivity.updateActivityRecord(record);
			userJsonObj.put("lottyIndex", lotteryIndex);
			ActivityCommand.sendActivityInfo(player, config, userJsonObj);
		} else
			ActivityCommand.sendActivityInfo(player, config, userJsonObj);
	}
	
	/**
	 * 抽奖
	 * @param lotteries
	 * @return
	 */
	private int randomLotteryIndex(Map<Integer, Lottery> lotteries) {
		Probability<Lottery> prop = new Probability<Lottery>();
		for (Lottery lottery : lotteries.values()) {
			prop.add((int)lottery.getChance(), lottery);
		}
		int ordinaryLottyIndex = 8;
		prop.setProbabilityRangeByCount();
		Lottery lottery = prop.random();// 随机抽奖
		int curPrizeNum = 0;
		int prizeSum = UserDao.getInstance().getSmashEggPrizeSum();
		int lotteryIndex = lottery.getIndex();
		if(lottery.getMaxNum() > 0) {
			curPrizeNum = UserDao.getInstance().getPrizeSum(lotteryIndex);// 有限制抽奖次数的档次ID
			if(curPrizeNum >= lottery.getMaxNum()) {
				lotteryIndex = ordinaryLottyIndex;
			}
		}
		if ((prizeSum + 1) % 300 == 0)
			lotteryIndex = ordinaryLottyIndex;
		if ((prizeSum + 1) % 500 == 0)
			lotteryIndex = ordinaryLottyIndex;
		if (lotteries.get(lotteryIndex).getState() == 0) {
			lotteryIndex = ordinaryLottyIndex;
		}
		return lotteryIndex;
	}
	
	/**
	 * 获取玩家用户数据
	 * @param config
	 * @param record
	 * @return
	 */
	public JSONObject getUserJson(SmashEggAcitivityConfig config, UserActivityRecord record) {
		JSONObject userJsonObj = new JSONObject();
		Map<Integer, Integer> smashEggTasks = record.getSmashEggTasks();
		userJsonObj.put("smashEggTimes", getCanSmashEggTimes(config, record));// 当前可砸蛋次数
		userJsonObj.put("smashEggTasks", smashEggTasks);// 当前活动任务达成情况
		List<UserMessage> userMessages = UserMessageDao.getInstance().selectUserMessageByType(2, 30);// 最近30条记录
		JSONArray ja = new JSONArray();
		if(!userMessages.isEmpty()) {
			for(UserMessage msg : userMessages) {
				JSONObject jo = new JSONObject();
				jo.put("userName", msg.getAward());
				jo.put("content", msg.getContent());
				ja.add(jo);
			}
		}
		userJsonObj.put("smashEggRecords", ja);// 当前活动任务达成情况
		return userJsonObj;
	}
	
	private int getCanSmashEggTimes(SmashEggAcitivityConfig config, UserActivityRecord record) {
		Map<Integer, Integer> conditions = config.getReachConditions();
		Map<Integer, Integer> smashEggTasks = record.getSmashEggTasks();
		int smashEggTimes = record.getSmashEggTimes();
		int canReceiveTimes = 0;
		for(int taskId : conditions.keySet()) {
			if(smashEggTasks.containsKey(taskId) && smashEggTasks.get(taskId) >= conditions.get(taskId)) {
				canReceiveTimes ++;
			}
		}
		canReceiveTimes = canReceiveTimes - smashEggTimes;
		if(canReceiveTimes > max_smash_egg_times) {
			canReceiveTimes = max_smash_egg_times;
		}
		return canReceiveTimes;
	}
	
	@Override
	public void setMsgTypeMap() {	
	}
}
