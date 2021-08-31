package com.sy.sanguo.game.pdkuai.action;

import com.alibaba.fastjson.JSON;
import com.sy.sanguo.common.struts.StringResultType;
import com.sy.sanguo.common.util.JacksonUtil;
import com.sy.sanguo.common.util.MathUtil;
import com.sy.sanguo.game.action.UserAction;
import com.sy.sanguo.game.bean.RegInfo;
import com.sy.sanguo.game.bean.enums.CardSourceType;
import com.sy.sanguo.game.bean.redbag.*;
import com.sy.sanguo.game.dao.UserDaoImpl;
import com.sy.sanguo.game.pdkuai.manager.RedBagManager;
import com.sy.sanguo.game.pdkuai.util.LogUtil;
import com.sy599.sanguo.util.ResourcesConfigsUtil;
import com.sy599.sanguo.util.TimeUtil;
import com.sy599.sanguo.util.TimeUtil1;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.*;

/**
 * 小甘领取现金红包活动
 */
public class RedBagAction {

	/**
	 * @param userInfo
	 * @param requestType 请求类型 0打开领取红包界面 1领取红包 2获取玩家红包领取记录 3红包提现(钻石 满5元提现到微信公众号发红包口令体现)
	 * @return
	 */
	public static Map<String, Object> execute(UserDaoImpl userDao, RegInfo userInfo, int requestType, int redBagType) throws Exception {
		Map<String, Object> result = new HashMap<>();
		result.put("requestType", requestType);
		result.put("code", 0);
		long userId = userInfo.getUserId();
		String redBagConfigStr = ResourcesConfigsUtil.loadServerPropertyValue("red_bag_config");
		if (redBagConfigStr == null) {//10000,2018-7-15,2018-7-22,2018-7-23,2018-7-26,1、活动时间7.15-7.22，提现时间23日00:00至25日24:00点;2、每日登陆可转动转盘领取红包一个，红包累计金额满5元才能提现;3、每日玩牌4局以上（含4局）可再领取红包一个;
			result.put("msg", "活动配置异常");
			result.put("receive", false);
			return result;
		}
		String redBagRewardDiamondGrades = ResourcesConfigsUtil.loadServerPropertyValue("red_bag_reward_diamond_grade");// 1:30,6:20,3:90,4:120;5:160
		if (redBagRewardDiamondGrades == null) {
			result.put("msg", "活动配置异常");
			result.put("receive", false);
			return result;
		}
		RedBagConfig config = new RedBagConfig(redBagConfigStr, redBagRewardDiamondGrades);
		Date nowDate = TimeUtil1.now();
		Date startDate = new Date(TimeUtil1.parseTimeInMillis(config.getStartDate() + " 00:00:00"));
		Date endDate = new Date(TimeUtil1.parseTimeInMillis(config.getEndDate() + " 23:59:59"));
        Date drawStartDate = new Date(TimeUtil1.parseTimeInMillis(config.getDrowStartDate() + " 00:00:00"));
        Date drawEndDate = new Date(TimeUtil1.parseTimeInMillis(config.getDrowEndDate() + " 00:00:00"));
		if (nowDate.before(startDate)) {// 在活动时间内
			result.put("msg", "领取时间未到，请晚点再来哦！");
			result.put("receive", false);
			return result;
		}
		if (nowDate.after(drawEndDate)) {
			result.put("msg", "活动已结束，请下次再来哦！");
			result.put("receive", false);
			return result;
		}
		LogUtil.i(userId + "请求小甘领取现金红包活动:" + requestType);
		if (requestType == 0) {// 打开活动界面
			result.put("msg", sendOpenRedBagInfo(userId, config));
			result.put("receive", true);
		} else if (requestType == 1) {// 领取红包
			if (nowDate.after(endDate)) {
				result.put("msg", "领取活动已结束，可以提现了哦！");
				result.put("receive", false);
				return result;
			}
			Date receiveStartDate = new Date(TimeUtil1.parseTimeInMillis(TimeUtil1.formatDayTime2(nowDate) + " " + config.getReceiveStartTime()));
			Date receiveEndDate = new Date(TimeUtil1.parseTimeInMillis(TimeUtil1.formatDayTime2(nowDate) + " " + config.getReceiveEndTime()));
			if (nowDate.before(receiveStartDate) || nowDate.after(receiveEndDate)) {
				result.put("msg", "领取时间未到，请晚点再来哦！");
				result.put("receive", false);
				return result;
			}
			receiveRedBag(result, config, userInfo, startDate, redBagType);
			return result;
		} else if (requestType == 2) {// 获取玩家红包领取记录
			result.put("msg", JSON.toJSONString(getSelfRedBagReceiveRecords(userId)));
		} else if (requestType == 3) {// 提现
			if (nowDate.before(drawStartDate) || nowDate.after(drawEndDate)) {
				result.put("msg", "当前还不在提现时间内！");
				return result;
			}
			float accRedBagNum = RedBagManager.getInstance().getUserCanReceiveRedBag(userId);
			if(accRedBagNum <= 0) {
                result.put("msg", "您已提现过了哦！");
                return result;
            }
			if (accRedBagNum >= 5.0f) {
				result.put("msg", "超过5元红包需要扫码公众号体现！");
				return result;
			} else {
				int receiveDiamond = getDiamondFromRedBag(accRedBagNum, redBagRewardDiamondGrades);
				LOGGER.info(userInfo.getName() + "提现金额：" + accRedBagNum + "--获得钻石：" + receiveDiamond);
				userDao.addUserCards(userInfo, 0, receiveDiamond, CardSourceType.activity_RedBag);
				List<UserRedBagRecord> records = RedBagManager.getInstance().getUserRedBagReords(userId);
				for (UserRedBagRecord record : records) {// 把所有
					boolean update = false;
					for (SelfRedBagReceiveRecord receiveRecord : record.getReceiveRecordList()) {
						if (!receiveRecord.isWithDraw()) {
							receiveRecord.setWithDraw(true);
							update = true;
						}
					}
					if (update == true)
						RedBagManager.getInstance().updateUserRedBagRecord(record);
				}
				result.put("msg", "您在本次领取现金红包活动中获得钻石x" + receiveDiamond);
			}
		}
		return result;
    }

    private static void receiveRedBag(Map<String, Object> result, RedBagConfig config, RegInfo userInfo, Date startDate, int redBagType) throws Exception{
        String redBagGradeRewards = ResourcesConfigsUtil.loadServerPropertyValue("red_bag_reward_grades");// red_bag_config
        String redBagLoginRewardGradeStr = ResourcesConfigsUtil.loadServerPropertyValue("red_bag_reward_grade_login");// red_bag_config
        String redBagGameRewardGradeStr = ResourcesConfigsUtil.loadServerPropertyValue("red_bag_reward_grade_game");// red_bag_config
        if (redBagLoginRewardGradeStr == null || redBagGameRewardGradeStr == null || redBagGradeRewards == null) {//10000,2018-4-1,2018-5-8,00:00:00,23:59:59,1、活动时间：2018年07月30日-2018年07月30日;2、登录一次即可抽奖一次，玩一局还能再抽一次;3、七天内累计金额达到5元即可提现,每日12:00:00-23:59:59领取10000元红包
            result.put("msg", "数据库未配置RewardGarde");
            result.put("receive", false);
            return;
        }
		RedBagManager manager = RedBagManager.getInstance();
        synchronized (manager) {
			RedBagSystemInfo systemInfo = manager.getRedBagSystemInfo(config);
			if (systemInfo.getDayPoolNum() <= 0) {
				result.put("msg", "活动太火爆了，今日红包已经领取完毕！");
				result.put("receive", false);
				return;
			}
			UserRedBagRecord userRedBagRecord = manager.getTodayUserRedBagRecord(userInfo.getUserId());// 当天活动数据
			if ((redBagType == 0 && userRedBagRecord.getLoginRedBag() > 0) || (redBagType == 1 && userRedBagRecord.getGameRedBag() > 0)) {
				result.put("msg", "您已领取该红包！");
				result.put("receive", false);
				return;
			}
			if(redBagType == 1 && userRedBagRecord.getGameNum() < 4) {
				result.put("msg", "完成4小局游戏才能领取哦！");
				result.put("receive", false);
				return;
			}
			RedBagGradeReward gradeReward = null;
			int apartDay = TimeUtil.apartDays(startDate, new Date());
			if (redBagType == 0) {
				boolean firstLoginRedBag = manager.isReceiveFirstLoginRedBag(userInfo.getUserId());
				if (firstLoginRedBag) {// 领取首次登陆红包
					gradeReward = getRedBagGrades(1, redBagLoginRewardGradeStr);
					System.out.println(userInfo.getName() + "领取首次登陆红包!");
					LOGGER.info(userInfo.getName() + "领取首次登陆红包!");
				} else {
					gradeReward = getRedBagGrades(apartDay + 1, redBagLoginRewardGradeStr);
					System.out.println(userInfo.getName() + "领取第" + (apartDay + 1) + "天登陆红包!" + gradeReward.getMinGrade() + "-" + gradeReward.getMaxGrade());
					LOGGER.info(userInfo.getName() + "领取第" + (apartDay + 1) + "天登陆红包!");
				}
			} else if (redBagType == 1) {
				boolean firstGameRedBag = manager.isReceiveFirstGameRedBag(userInfo.getUserId());
				if (firstGameRedBag) {
					gradeReward = getRedBagGrades(1, redBagGameRewardGradeStr);
					System.out.println(userInfo.getName() + "领取首次打牌红包!");
					LOGGER.info(userInfo.getName() + "领取首次打牌红包!");
				} else {
					gradeReward = getRedBagGrades(apartDay + 1, redBagGameRewardGradeStr);
					System.out.println(userInfo.getName() + "领取第" + (apartDay + 1) + "天打牌红包!" + gradeReward.getMinGrade() + "-" + gradeReward.getMaxGrade());
					LOGGER.info(userInfo.getName() + "领取第" + (apartDay + 1) + "天打牌红包!");
				}
			}
			float receiveRedBag = 0.0f;// 新红包抽取规则
			if(redBagType == 1 && userInfo.getTotalCount() >= 100 && MathUtil.mt_rand(1, 100) <= 1) {// 打牌红包 牌局数100局以上的用户 有1%的概率抽到1.58-8.88之间的金额
				receiveRedBag = randomRedBag(1.58f, 8.88f);
			} else {
				receiveRedBag = randomRedBag(gradeReward.getMinGrade(), gradeReward.getMaxGrade());
			}
			userRedBagRecord.updateRecord(receiveRedBag, redBagType);
			manager.updateUserRedBagRecord(userRedBagRecord);
			manager.subPoolRedBag(receiveRedBag, config);
			if (receiveRedBag >= 0.8f) {// 超过1元才广播
				RedBagReceiveRecord record = new RedBagReceiveRecord(userInfo.getName(), receiveRedBag);
				manager.addRedBagReceiveRecord(record, config);
			}
			result.put("msg", String.valueOf(receiveRedBag));
			result.put("receive", true);
		}
        return;
    }

    private static String sendOpenRedBagInfo(long userId, RedBagConfig config) {
        RedBagSystemInfo systemInfo = RedBagManager.getInstance().getRedBagSystemInfo(config);
        float accRedBagNum = RedBagManager.getInstance().getUserCanReceiveRedBag(userId);
        DecimalFormat fnum = new DecimalFormat("#0.00");
        float sendAccRedBagNum = Float.parseFloat(fnum.format(accRedBagNum));
        Date drawStartDate = new Date(TimeUtil1.parseTimeInMillis(config.getDrowStartDate() + " 00:00:00"));
        Date drawEndDate = new Date(TimeUtil1.parseTimeInMillis(config.getDrowEndDate() + " 00:00:00"));
        boolean canDraw = false;
        Date nowDate = new Date();
        if(nowDate.after(drawStartDate) && nowDate.before(drawEndDate)) {
            canDraw = true;
        }
        UserRedBagRecord userRedBagRecord = RedBagManager.getInstance().getTodayUserRedBagRecord(userId);// 当天活动数据
        RedBagSendInfo sendInfo = new RedBagSendInfo(userId, config, userRedBagRecord, systemInfo, sendAccRedBagNum, canDraw);
        return JSON.toJSONString(sendInfo);
    }

	public static List<SelfRedBagReceiveRecord> getSelfRedBagReceiveRecords(long userId) {
		List<SelfRedBagReceiveRecord> selfRecords = RedBagManager.getInstance().getUserAllReceiveRecords(userId);
		Collections.sort(selfRecords, new Comparator<SelfRedBagReceiveRecord>() {
			@Override
			public int compare(SelfRedBagReceiveRecord o1, SelfRedBagReceiveRecord o2) {
				try {
					Date date1 = TimeUtil1.getDateByString(o1.getReceiveTime(), "yyyy-MM-dd HH:mm:ss");
					Date date2 = TimeUtil1.getDateByString(o2.getReceiveTime(), "yyyy-MM-dd HH:mm:ss");
					if (date1.getTime() > date1.getTime()) {
						return 1;
					}
					return 0;
				} catch (Exception e) {
					return 0;
				}
			}
		});
		return selfRecords;
	}

	private static RedBagGradeReward getRedBagGrades(int day, String rewardGradeStr) {
        if(day == 0)
            day = 1;
		Map<Integer, RedBagGradeReward> map = new HashMap<>();
		String [] arr = rewardGradeStr.split(",");
		for(String rewardGrade : arr) {
			RedBagGradeReward reward = new RedBagGradeReward(rewardGrade);
			if(reward.getDay() == day) {
				return reward;
			}
		}
		return null;
	}

	private static float randomRedBag(float minGrade, float maxGrade) {
		int minValue = (int)(minGrade * 100);
		int maxValue = (int)(maxGrade * 100);
		int randomValue =  MathUtil.mt_rand(minValue, maxValue);
		float randomRedbag = randomValue * 1.0f / 100;
		DecimalFormat fnum = new DecimalFormat("#0.00");
		float receiveRedBag = Float.parseFloat(fnum.format(randomRedbag));
		return receiveRedBag;
	}

	/**
	 * 未满1元赠送30钻，1元及以上未满2元赠送60钻，2元及以上未满3元赠送90钻，3元及以上未满4元赠送120钻；4元以上未满5元赠送150钻；
	 * @param redBag
	 * @return
	 */
	private static int getDiamondFromRedBag(float redBag, String redBagRewardDiamondGrades) {
		String[] arr = redBagRewardDiamondGrades.split(",");//1:30,6:20,3:90,4:120;5:160
		for(String temp : arr) {
			String[] gradeArr = temp.split(":");
			int money = Integer.parseInt(gradeArr[0]);
			int diamond = Integer.parseInt(gradeArr[1]);
			if(redBag < money) {
				return diamond;
			}
		}
		LogUtil.e("数据库配置钻石提现档次错误");
    	return 0;
	}

    private static final Logger LOGGER = LoggerFactory.getLogger(RedBagAction.class);

    /**
     * 是否活动开启
     * @return
     */
	public static boolean isRedBagOpen() {
	    try {
            String redBagConfigStr = ResourcesConfigsUtil.loadServerPropertyValue("red_bag_config");
            if (redBagConfigStr == null) {
                return false;
            }
            RedBagConfig config = new RedBagConfig(redBagConfigStr, null);
            Date nowDate = TimeUtil1.now();
            Date startDate = new Date(TimeUtil1.parseTimeInMillis(config.getStartDate() + " 00:00:00"));
            Date drawEndDate = new Date(TimeUtil1.parseTimeInMillis(config.getDrowEndDate() + " 00:00:00"));
            if (nowDate.before(startDate) || nowDate.after(drawEndDate)) {
                return false;
            } else
                return true;
        } catch(Exception e) {
            LOGGER.error("小甘领取现金红包活动是否开启异常：" + e.toString());
            return false;
        }
    }
}
