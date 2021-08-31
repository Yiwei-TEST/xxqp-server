package com.sy.sanguo.game.pdkuai.user;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sy.sanguo.game.bean.enums.CardSourceType;
import com.sy599.sanguo.util.ResourcesConfigsUtil;
import org.apache.commons.lang3.StringUtils;

import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.util.JacksonUtil;
import com.sy.sanguo.common.util.JsonWrapper;
import com.sy.sanguo.game.bean.CdkAward;
import com.sy.sanguo.game.bean.RegInfo;
import com.sy.sanguo.game.bean.Room;
import com.sy.sanguo.game.bean.SystemCdk;
import com.sy.sanguo.game.bean.UserLotteryStatistics;
import com.sy.sanguo.game.bean.UserLucky;
import com.sy.sanguo.game.dao.LuckyDaoImpl;
import com.sy.sanguo.game.dao.RoomDaoImpl;
import com.sy.sanguo.game.dao.UserDao;
import com.sy.sanguo.game.dao.UserDaoImpl;
import com.sy.sanguo.game.dao.UserLotteryStatisticsDaoImpl;
import com.sy.sanguo.game.msg.UserPlayMsg;
import com.sy.sanguo.game.msg.UserPlayTableMsg;
import com.sy.sanguo.game.pdkuai.constants.ActivityConstant;
import com.sy.sanguo.game.pdkuai.db.bean.UserPlaylog;
import com.sy.sanguo.game.pdkuai.staticdata.StaticDataManager;
import com.sy.sanguo.game.pdkuai.staticdata.bean.ActivityBean;
import com.sy599.sanguo.util.TimeUtil;
import com.alibaba.fastjson.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Manager {

    private static final Logger LOGIN_LOGGER = LoggerFactory.getLogger("login");

	private static Manager _inst = new Manager();

	public static Manager getInstance() {
		return _inst;
	}

	/** 玩家最小的id **/
	public static long min_player_id = 101000;//默认最小userId为101000，甘肃最小值为120000
	//是否过滤userId
	public static boolean withFilterUserId = false;

	public static long base_server_id = 100000;

	static {

	}

	public void buildBaseUser(RegInfo regInfo, String platform, long maxId) {
		regInfo.setPf(platform);
		regInfo.setUserId(maxId);
		regInfo.setLoginDays(1);
        int giveRoomCards = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "giveRoomCards", 0);
		regInfo.setFreeCards(giveRoomCards);
		regInfo.setPlayedSid("[]");
		regInfo.setConfig("1,1");
	}

	/**
	 * 检查玩家局数
	 * @param regInfo
	 * @return
	 */
	public int checkTotalCount(RegInfo regInfo) {
		if (!StringUtils.isBlank(regInfo.getExtend())) {
			JsonWrapper wrapper = new JsonWrapper(regInfo.getExtend());
			String val5 = wrapper.getString(5);
			String val6 = wrapper.getString(6);
			int total5 = 0;
			if (!StringUtils.isBlank(val5)) {
				total5 = split(val5);
			}
			int total6 = 0;
			if (!StringUtils.isBlank(val6)) {
				total6 = split(val6);
			}
			return total5 + total6;
		}
		return 0;

	}

	/**
	 * 分解出局数
	 * @param val
	 * @return
	 */
	private static int split(String val) {
		int total = 0;
		String[] values = val.split(";");
		for (String value : values) {
			String[] _values = value.split(",");
			if (_values.length < 2) {
				continue;
			}
			int valInt = Integer.parseInt(_values[1]);
			total += valInt;
		}
		return total;
	}

	public synchronized long generatePlayerId(UserDaoImpl userDao) throws Exception {
		long maxId = userDao.getMaxId();
		if (maxId < min_player_id) {
			maxId = min_player_id;
		}
		maxId++;
		if(withFilterUserId){
			while (filterUserId(maxId)){
				maxId++;
			}
		}
		return maxId;
	}

    /**
     * 新方法：随机生成
     *
     * @return
     * @throws Exception
     */
    public static long generatePlayerId() throws Exception {
        long start = System.currentTimeMillis();
        Random rnd = new Random();
        long userId = randomUserId(rnd);
        int count = 1;
        while (!isUserIdOK(userId)) {
            userId = randomUserId(rnd);
            count++;
        }
        LOGIN_LOGGER.info("generatePlayerId|" + userId + "|" + (System.currentTimeMillis() - start) + "|" + start + "|" + count);
        return userId;
    }

    private static final int minRandomId = 120000;
    private static final int maxRandomId = 9880000;

    private static long randomUserId(Random rnd) {
        return minRandomId + rnd.nextInt(maxRandomId);
    }

    private static boolean isUserIdOK(long userId) throws Exception {
        RegInfo regInfo = UserDao.getInstance().getUser(userId);
        if (regInfo != null) {
            return false;
        }
        return true;
    }

	/**
	 * 过滤(2017.04.11添加)<br/>
	 * 4A/5A/6A，如：102222、85555<br/>
	   ABCD/ABCDE/ABCDEF，如：201234、56789<br/>
	   3A/3B，如：111222<br/>
	 * @param userId
	 * @return
	 */
	private static boolean filterUserId(long userId){
		String userIdStr=String.valueOf(userId);
		if (userIdStr.length()>=4){
			int count=1;
			int temp=userIdStr.charAt(userIdStr.length()-1)-userIdStr.charAt(userIdStr.length()-2);

			switch (temp){
				case 0:
					boolean isAAABBB=false;
					for (int i=userIdStr.length()-2;i>=1;i--){
						if (userIdStr.charAt(i)-userIdStr.charAt(i-1)==0){
							count++;
							if (count>=3||(isAAABBB&&count>=2)){
								return true;
							}
						}else{
							if(count>=2){
								count=0;
								isAAABBB=true;
							}else{
								return false;
							}
						}
					}
					break;
				case 1:
					for (int i=userIdStr.length()-2;i>=1;i--){
						if (userIdStr.charAt(i)-userIdStr.charAt(i-1)==1){
							count++;
							if (count>=3){
								return true;
							}
						}else{
							return false;
						}
					}
					break;
				case -1:
					for (int i=userIdStr.length()-2;i>=1;i--){
						if (userIdStr.charAt(i)-userIdStr.charAt(i-1)==-1){
							count++;
							if (count>=3){
								return true;
							}
						}else{
							return false;
						}
					}
					break;
				default:
					return false;
			}

		}
		return false;
	}

	public int getServerId(long roomId) {
		int serverId = 0;
		try {
			Room room = RoomDaoImpl.getInstance().queryRoom(roomId);
			if (room != null) {
				serverId = room.getServerId();
			}
		} catch (SQLException e) {
			GameBackLogger.SYS_LOG.error(" getServerId", e);
		}

		// if (serverId == 0) {
		// serverId = (int) (roomId / Manager.base_server_id);
		//
		// }
		return serverId;

	}

	public List<UserPlayTableMsg> buildUserPlayTbaleMsg(long logId, List<UserPlaylog> list, long viewUserId) {
		List<UserPlayTableMsg> result = new ArrayList<UserPlayTableMsg>();
		for (UserPlaylog log : list) {
			if (logId != 0 && StringUtils.isBlank(log.getOutCards())) {
				// 查询详细牌局信息 但是没有打牌记录
				continue;
			}
			UserPlayTableMsg msg = new UserPlayTableMsg();
			msg.setId(log.getId());
			msg.setPlayType((int) log.getLogId());
			msg.setPlayCount(log.getCount());
			msg.setTotalCount(log.getTotalCount());
			msg.setTableId(log.getTableId());
			msg.setTime(TimeUtil.formatTime(log.getTime()));
			msg.setClosingMsg(log.getExtend());
			msg.setGeneralExt(log.getGeneralExt());
			//打筒子战绩单独处理
			if ((log.getLogId()>=113&&log.getLogId()<=118) || ( log.getLogId() >= 210 && log.getLogId() <= 212 ) ){
				JSONObject jsonObject = null;
				if (!StringUtils.isEmpty(log.getExtend())) {
					try{
						jsonObject = JSONObject.parseObject(log.getExtend());
						msg.setGroupScoreA((jsonObject.containsKey("aScore") ? jsonObject.getIntValue("aScore") : 0));
						msg.setGroupScoreB((jsonObject.containsKey("bScore") ? jsonObject.getIntValue("bScore") : 0));
						msg.setGroupScoreC((jsonObject.containsKey("cScore") ? jsonObject.getIntValue("cScore") : 0));
						if(jsonObject.containsKey("cutCardList")){
							List<Integer> loadKou8CardList = new ArrayList<Integer>();
							if (jsonObject.getString("cutCardList") != null && !jsonObject.getString("cutCardList").isEmpty()){
								JSONArray jsonArray = JSONArray.parseArray(jsonObject.getString("cutCardList"));
								for (Object val : jsonArray) {
									loadKou8CardList.add(Integer.valueOf(val.toString()));
								}
							}
							msg.setCutCardList(loadKou8CardList);
						}else{
							msg.setCutCardList(new ArrayList<Integer>());
						}
					}catch (Exception e){e.printStackTrace();}
				}
				String res = log.getRes();
				List<String> resList = JacksonUtil.readValue(res, new TypeReference<List<String>>() {});
				msg.setResList(resList);
				msg.setPlay(log.getOutCards());
				List<UserPlayMsg> playerMsgList = new ArrayList<UserPlayMsg>();
				for (String resValue : resList) {
					JsonWrapper resMap = new JsonWrapper(resValue);
					long userId = resMap.getLong("userId", 0);
					int leftCardNum = resMap.getInt("leftCardNum", 0);
					int isHu = resMap.getInt("isHu", 0);
					int point = resMap.getInt("point", 0);
					if (userId == viewUserId) {
						if (log.getLogId() == 15 || log.getLogId() == 16) {
							if (leftCardNum == 0) {
								msg.setIsWin(1);

							}
						} else if (log.getLogId() < 10) {
							if (isHu > 0) {
								msg.setIsWin(1);
							}
						} else {
							if (point > 0) {
								msg.setIsWin(1);
							}
						}
					}

					UserPlayMsg playerMsg = new UserPlayMsg();
					playerMsg.setName(resMap.getString("name"));
					playerMsg.setPoint(resMap.getInt("point", 0));
					playerMsg.setTotalPoint(resMap.getInt("totalPoint", 0));
					playerMsg.setWinLossPoint(resMap.getInt("boom", 0));

						String ext = resMap.getString("ext");
						if (!StringUtils.isEmpty(ext)) {
							JSONArray jsonArray = JSONArray.parseArray(ext);
							playerMsg.setGroup(jsonArray.getIntValue(12));
							playerMsg.setMingci(jsonArray.getIntValue(13));
							playerMsg.setWinGroup(jsonArray.size() >= 15 ? jsonArray.getIntValue(14) : 0);
							playerMsg.setDtzOrXiScore(jsonArray.size() >= 16 ? jsonArray.getIntValue(15) : 0);
						}
						playerMsg.setJiangli((jsonObject == null) ? 0 : jsonObject.getIntValue("jiangli"));
					playerMsg.setSex(resMap.getInt("sex", 0));
					if (resMap.isHas("bopiPoint")) {
						playerMsg.setBopiPoint(resMap.getInt("bopiPoint", 0));
					}
					playerMsg.setUserId(userId);
					if (userId == viewUserId) {
						playerMsgList.add(0, playerMsg);
					} else {
						playerMsgList.add(playerMsg);

					}
				}
				msg.setPlayerMsg(playerMsgList);
			}else{
				String res = log.getRes();
				List<String> resList = JacksonUtil.readValue(res, new TypeReference<List<String>>() {
				});
				msg.setResList(resList);
				msg.setPlay(log.getOutCards());
				msg.setMaxPlayerCount(log.getMaxPlayerCount());
				List<UserPlayMsg> playerMsgList = new ArrayList<UserPlayMsg>();
				for (String resValue : resList) {
					JsonWrapper resMap = new JsonWrapper(resValue);
					long userId = resMap.getLong("userId", 0);
					int leftCardNum = resMap.getInt("leftCardNum", 0);
					int isHu = resMap.getInt("isHu", 0);
					int point = resMap.getInt("point", 0);
					if (userId == viewUserId) {
						if (log.getLogId() == 15 || log.getLogId() == 16) {
							if (leftCardNum == 0) {
								msg.setIsWin(1);

							}
						} else if (log.getLogId() < 10) {
							if (isHu > 0) {
								msg.setIsWin(1);
							}
						} else {
							if (point > 0) {
								msg.setIsWin(1);
							}
						}
					}

					UserPlayMsg playerMsg = new UserPlayMsg();
					playerMsg.setSex(resMap.getInt("sex", 0));
					playerMsg.setName(resMap.getString("name"));
					playerMsg.setIcon(resMap.getString("icon"));
					playerMsg.setPoint(resMap.getInt("point", 0));
					playerMsg.setTotalPoint(resMap.getInt("totalPoint", 0));
					if (resMap.isHas("bopiPoint")) {
						playerMsg.setBopiPoint(resMap.getInt("bopiPoint", 0));
					}
					if(resMap.isHas("allHuxi")){
						playerMsg.setAllHuxi(resMap.getInt("allHuxi", 0));
					}
					playerMsg.setUserId(userId);
					if (resMap.isHas("ext") && !StringUtils.isEmpty(resMap.getString("ext"))){
						playerMsg.setExt(resMap.getString("ext").replaceAll("\\\\", ""));
					}
					if (userId == viewUserId) {
						playerMsgList.add(0, playerMsg);
					} else {
						playerMsgList.add(playerMsg);

					}
				}
				msg.setPlayerMsg(playerMsgList);
			}
			result.add(msg);
		}
		return result;
	}

	/**
	 * 活动期间新用户注册成功后执行的方法
	 * 
	 * @return
	 * @throws Exception
	 */
	public static void invitationSuccess(RegInfo regInfo, Long invitorId) throws Exception {

		// 如果invitorId不等于0，则参与拉新福袋活动
		ActivityBean activity = StaticDataManager.getActivityBean(ActivityConstant.activity_fudai);
		if (activity == null) {
			return;
		}

		// userId, username, sex, inviteeCount, invitorId, feedbackCount,
		// openCount, activityStartTime, prizeFlag

		if (invitorId != 0) {
			UserLucky otherLucky = (UserLucky) LuckyDaoImpl.getInstance().getUserLucky(invitorId);
			// 查看该邀请者是否参加过拉新福袋活动，如果以前没有参加过，也需将其添加到拉新福袋活动表
			if (otherLucky == null) {
				RegInfo otherInfo = UserDao.getInstance().getUser(invitorId);
				otherLucky = new UserLucky();
				otherLucky.setUserId(invitorId);
				otherLucky.setInvitorId(0);
				otherLucky.setUsername(otherInfo.getName());
				otherLucky.setSex(otherInfo.getSex());
				otherLucky.setInviteeCount(1);
				otherLucky.setActivityStartTime(activity.getStartDateTime());
				LuckyDaoImpl.getInstance().insertUserLucky(otherLucky);
			} else {
				// 如果该邀请者以前参加过拉新福袋活动，则邀请的人数需要+1
//				if (otherLucky.getInviteeCount() >= 30) {
//					return;
//				}
				Map<String, Object> paraMap = new HashMap<>();
				paraMap.put("inviteeCount", otherLucky.getInviteeCount() + 1);
				LuckyDaoImpl.getInstance().updateUserLucky(invitorId, paraMap);

			}

			UserLucky selfLucky = (UserLucky) LuckyDaoImpl.getInstance().getUserLucky(regInfo.getUserId());
			if (selfLucky == null) {
				// 将该用户加入到拉新福袋活动表中
				UserLucky userLucky = new UserLucky();
				userLucky.setUserId(regInfo.getUserId());
				userLucky.setInvitorId(invitorId);
				userLucky.setUsername(regInfo.getName());
				userLucky.setSex(regInfo.getSex());
				userLucky.setActivityStartTime(activity.getStartDateTime());
				LuckyDaoImpl.getInstance().insertUserLucky(userLucky);
			} else {
				Map<String, Object> paraMap = new HashMap<>();
				paraMap.put("username", regInfo.getName());
				paraMap.put("sex", regInfo.getSex());
				paraMap.put("inviteeCount", 0);
				paraMap.put("invitorId", invitorId);
				paraMap.put("prizeFlag", 0);
				paraMap.put("openCount", 0);
				paraMap.put("feedbackCount", 0);
				paraMap.put("activityStartTime", activity.getStartDateTime());
				LuckyDaoImpl.getInstance().updateUserLucky(regInfo.getUserId(), paraMap);
			}
		}
	}

	/**
	 * 当被邀请的新用户的每消耗5张房卡,feedbackCount数更新
	 * 
	 * @return
	 * @throws SQLException
	 */
	// public void perUsedFiveCards(long userId, long invitorId, int
	// usedCardCount) throws SQLException {
	// /*
	// * //用户的id long userId = getLong("userId"); //邀请人的id long invitorId =
	// * getLong("invitorId"); //用户消耗的房卡数 int usedCardCount =
	// * getInt("usedCards");
	// */
	//
	// ActivityBean activity =
	// StaticDataManager.getActivityBean(ActivityConstant.activity_fudai);
	//
	// if (activity == null) {
	// return;
	// }
	//
	// // 如果邀请人 id不等于0则为被邀请的新用户
	// if (invitorId != 0) {
	// // 每消耗5张房卡
	// if (usedCardCount % 5 == 0 && usedCardCount != 0) {
	// // 则反馈福袋数+1
	// UserLucky userLucky = (UserLucky)
	// LuckyDaoImpl.getInstance().getUserLucky(userId);
	// userLucky.setFeedbackCount(usedCardCount);
	// LuckyDaoImpl.getInstance().updateUserLucky(userLucky);
	// }
	// }
	//
	// }

	/**
	 * 领取cdk
	 * 
	 * @param userDao
	 *            userDao
	 * @param cdkid
	 *            cdkId
	 * @param flag
	 *            cdkFlag
	 * @param user
	 *            玩家
	 * @param checkCdkUnique
	 *            是否检查cdkId唯一
	 * @param checkTime
	 *            是否检查cdk时间过期
	 * @param checkCdkTypeUnique
	 *            是否检查cdkType 唯一
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getCdk(UserDaoImpl userDao, UserLotteryStatisticsDaoImpl userLotteryStatisticsDao, String cdkid, CdkAward flag, RegInfo user, boolean checkCdkUnique, boolean checkTime, boolean checkCdkTypeUnique) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		// 用户注册时间大于CDK有效开始时间
		if (checkTime) {
			if (user.getRegTime().getTime() < TimeUtil.ParseTime(flag.getRegTime()).getTime()) {
				result.put("code", 10);
				result.put("msg", "没有领取资格");
				return result;
			}

			// 用户领取时间在CDK失效时间内
			if (new Date().getTime() > TimeUtil.ParseTime(flag.getEndTime()).getTime()) {
				result.put("code", 11);
				result.put("msg", "CDK已失效");
				return result;
			}
		}

		if (checkCdkUnique) {
			SystemCdk systemCdk = userDao.getSystemCdk(cdkid);
			if (systemCdk == null) {
				result.put("code", 2);
				result.put("msg", "兑换码错误");
				return result;
			}
			if (!StringUtils.isBlank(systemCdk.getFlatid())) {
				result.put("code", -100);
				result.put("msg", "该兑换码已领取");
				return result;
			}
		}

		// 是否领取过新注册的CDK
		if (checkCdkTypeUnique) {
			String cdkIds = userDao.getUserExtendinfByUid(user.getUserId());
			if (!StringUtils.isBlank(cdkIds)) {
				String[] idArr = cdkIds.split(",");
				String idParm = "";
				boolean temp = false;
				for (int i = 0; i < idArr.length; i++) {
					idParm += idArr[i] + ",";
					if (Integer.parseInt(idArr[i]) == flag.getId()) {
						temp = true;
					}
				}
				if (temp) {
					result.put("code", 12);
					result.put("msg", "已领取过奖励");
					return result;
				} else {
					int update = userDao.updateUserCdk(user.getUserId(), idParm + flag.getId() + ",");
					if (update == 0) {
						result.put("code", 9);
						result.put("msg", "发送房卡失败");
						return result;
					}
				}
			} else {
				if (cdkIds == null) {
					userDao.insertUserExtendinf(user.getUserId(), flag.getId() + ",", 0);
				} else {
					int update = userDao.updateUserCdk(user.getUserId(), flag.getId() + ",");
					if (update == 0) {
						result.put("code", 9);
						result.put("msg", "发送房卡失败");
						return result;
					}
				}
			}
		}
		if (checkCdkUnique) {
			SystemCdk systemCdk = userDao.getSystemCdk(cdkid);
			if (systemCdk == null) {
				result.put("code", 2);
				result.put("msg", "兑换码错误");
				return result;
			}
			if (!StringUtils.isBlank(systemCdk.getFlatid())) {
				result.put("code", -100);
				result.put("msg", "该兑换码已领取");
				return result;
			}
			userDao.updateSystemCdk(user.getFlatId(), Integer.parseInt("1"), flag.getType(), cdkid);
		}
		int update = userDao.addUserCards(user, 0, flag.getAwardId(), 0, CardSourceType.receive_cdk);
		if (update == 0) {
			result.put("code", 9);
			result.put("msg", "发送房卡失败");
			return result;
		}

		int day = Integer.parseInt(new SimpleDateFormat("yyyyMMdd").format(new Date()));
		userLotteryStatisticsDao.saveUserLotteryStatistics(new UserLotteryStatistics(day, user.getUserId(), user.getName(), 3, 1));
		result.put("code", 0);
		result.put("cardNumber", flag.getAwardId());
		result.put("msg", "领取成功");
		return result;
	}

	public void check(RegInfo info) {
		// GameBackLogger.SYS_LOG.info("login----9" + info.getUserId());
		// TableLogDao.getInstance().delete(info.getUserId());
		// UserMessageDao.getInstance().deleteByDate(info.getUserId());
		// GameBackLogger.SYS_LOG.info("login----10" + info.getUserId());
	}


    /**
     * 新方法：随机生成
     *
     * @return
     * @throws Exception
     */
    public static long generateRobotPlayerId() throws Exception {
        long start = System.currentTimeMillis();
        Random rnd = new Random();
        long userId = randomRobotPlayerId(rnd);
        int count = 1;
        while (!isUserIdOK(userId)) {
            userId = randomRobotPlayerId(rnd);
            count++;
        }
        LOGIN_LOGGER.info("generateRobotPlayerId|" + userId + "|" + (System.currentTimeMillis() - start) + "|" + start + "|" + count);
        return userId;
    }

    private static final int robot_minRandomId = 110000;
    private static final int robot_maxRandomId = 10000;

    private static long randomRobotPlayerId(Random rnd) {
        return robot_minRandomId + rnd.nextInt(robot_maxRandomId);
    }
}
