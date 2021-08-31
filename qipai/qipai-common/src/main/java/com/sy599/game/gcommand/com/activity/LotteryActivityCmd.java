package com.sy599.game.gcommand.com.activity;

import com.alibaba.fastjson.JSONObject;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.db.bean.activityRecord.UserActivityRecord;
import com.sy599.game.db.dao.UserDao;
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
import com.sy599.game.staticdata.bean.LotteryAcitivityConfig;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.LotteryUtil;

import java.util.*;

/**
 * @author liuping
 * 幸运转盘活动处理类
 */
public class LotteryActivityCmd extends BaseCommand {

	@Override
	public void execute(Player player, MessageUnit message) throws Exception {
		ComReq req = (ComReq) this.recognize(ComReq.class, message);
	    List<Integer> reqParams = req.getParamsList();
	    int requestType = reqParams.get(0);// params参数第0位 表示请求的操作类型 0打开 1领取
	    LotteryAcitivityConfig configInfo = (LotteryAcitivityConfig) ActivityConfig.getActivityConfigInfo(ActivityConfig.activity_lucky_reward);
		long userId = player.getUserId();
		int playType = 0;// 玩法ID
		JSONObject userJsonObj = new JSONObject();
		UserActivityRecord activityRecord = player.getMyActivity().getUserActivityRecord();
		int lotteryNumber = getLottyNum(player, activityRecord, configInfo);
		if (requestType == 1) {
			if (lotteryNumber <= 0) {
				player.writeErrMsg(LangHelp.getMsg(LangMsg.code_224));
				return;
			}
			int lottyIndex = 1;
			// 创建奖品
			List<String> prize = new ArrayList<>();
			List<Double> list = new ArrayList<>();
			List<Lottery> lotteries = configInfo.getLotteries();
			// 读取奖品概率集合
			for (Lottery lottery : lotteries) {
				list.add(lottery.getChance());
				prize.add(lottery.getName());
			}
			LotteryUtil ll = new LotteryUtil(list);
			lottyIndex = ll.randomColunmIndex();
			int prizeSum = UserDao.getInstance().getPrizeSum();
			int firstPrizeSum = UserDao.getInstance().getFirstPrizeSum();
			int secondPrizeSum = UserDao.getInstance().getSecondPrizeSum();
			if ((prizeSum + 1) % 300 == 0)
				lottyIndex = 0;
			if ((prizeSum + 1) % 999 == 0)
				lottyIndex = 0;
			if (firstPrizeSum >= 999 && lottyIndex == 7)
				lottyIndex = 0;
			if (secondPrizeSum >= 9999 && lottyIndex == 3)
				lottyIndex = 0;
			if (lotteries.get(lottyIndex).getState() == 0) {
				lottyIndex = 0;
			}
			Lottery lottery = lotteries.get(lottyIndex);
			Map<String, Object> userMap = new HashMap<>();
			int cards = lottery.getRoomCard();
			userMap.put("cards", cards);
			int res1 = UserDao.getInstance().addPrize(userId,
					prize.get(lottyIndex), lottyIndex);
			String log = "getLottyResult-->userId:" + userId + ",res1:" + res1;
			if (cards > 0) {
				player.changeCards(cards, 0, false, playType, CardSourceType.activity_drawLottery);
				log += ",res2:" + 1;
			}
			LogUtil.msgLog.info(log);

			MessageUtil.sendMessage(false,true, UserMessageEnum.TYPE0,player,"恭喜通过幸运转盘获得:" + lottery.getName(),null);

			lotteryNumber --;
			userJsonObj.put("lottyIndex", lottyIndex);
			userJsonObj.put("lotteryNumber", lotteryNumber);
			ActivityCommand.sendActivityInfo(player, configInfo, userJsonObj);
			activityRecord.setUserdLotteryNum(activityRecord.getUserdLotteryNum() + 1);
			player.getMyActivity().updateActivityRecord(activityRecord);
		} else {
			userJsonObj.put("lotteryNumber", lotteryNumber);
			ActivityCommand.sendActivityInfo(player, configInfo, userJsonObj);
		}
	}
	
	 /**
	 * 获取玩家幸运转盘次数
	 * @param player
	 * @return
	 */
	public int getLottyNum(Player player, UserActivityRecord activityRecord, LotteryAcitivityConfig configInfo) {
        int number = 0;
    	long usedCards = player.getUsedCards();
    	Date regTime = player.getReginTime();
    	Date startTime = configInfo.getStartTime();
    	int accCostDiam = configInfo.getOpenGroupReward();
    	if(regTime.after(startTime)) {
    		number = 1;// 首次注册有一次抽取次数
        	int userdLotteryNum = activityRecord.getUserdLotteryNum();// 已抽取次数
        	number += (-usedCards < accCostDiam ? 0 : 1);// 累计消耗钻石有一次抽奖次数
            return number - userdLotteryNum;
    	} else 
    		return 0;
//        int usedLotteryNum = UserDao.getInstance().getUserdLotteryNum(player.getUserId());
//        number += UserShareDao.getInstance().countUserShare(player.getUserId(), TimeUtil.formatTime(startTime), TimeUtil.formatTime(endTime));       
//        if(openGroupReward == 1){
//            GroupUser groupUser = GroupDao.getInstance().loadGroupUser(player.getUserId(),null);
//            if (groupUser != null) {
//                number += 1;
//            }
//        }       
    }
	
	@Override
	public void setMsgTypeMap() {
	}
}
