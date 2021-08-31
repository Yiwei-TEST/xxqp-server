package com.sy.sanguo.game.pdkuai.game;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sy.sanguo.common.util.JacksonUtil;
import com.sy.sanguo.game.bean.RegInfo;
import com.sy.sanguo.game.bean.UserLucky;
import com.sy.sanguo.game.bean.enums.CardSourceType;
import com.sy.sanguo.game.dao.LuckyDaoImpl;
import com.sy.sanguo.game.dao.UserDao;
import com.sy.sanguo.game.pdkuai.action.BaseAction;
import com.sy.sanguo.game.pdkuai.constants.ActivityConstant;
import com.sy.sanguo.game.pdkuai.staticdata.StaticDataManager;
import com.sy.sanguo.game.pdkuai.staticdata.bean.ActivityBean;
import com.sy.sanguo.game.pdkuai.user.Manager;

public class LuckyAction extends BaseAction {
	private ActivityBean fudai;
	private ActivityBean ranking_list;

	@Override
	public String execute() throws Exception {
		init();
		int funcType = getInt("funcType");
		switch (funcType) {
		case 1:
			getMyLucky();
			break;
		case 2:
			getRankingList();
			break;
		case 3:
			openLucky();
			break;
		case 4:
			getAward();
			break;
		}
		return result;
	}

	public void init() {
		fudai = StaticDataManager.getActivityBean(ActivityConstant.activity_fudai);
		ranking_list = StaticDataManager.getActivityBean(ActivityConstant.activity_hbaward);
	}

	/**
	 * 获得用户的福袋信息和邀请信息的方法
	 * 
	 * @return
	 * @throws Exception
	 */
	public void getMyLucky() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();

		// 判断当前时间是否为福袋活动时间
		// if (fudai == null) {
		// map.put("msg", "未到活动时间");
		// this.writeMsg(-1, map);
		// return;
		// }

		ActivityBean rankingBaseInfo = StaticDataManager.getSingleActivityBaseInfo(ActivityConstant.activity_fudai);
		if (rankingBaseInfo == null) {
			map.put("msg", "rankingBaseInfo is null, activity type = " + ActivityConstant.activity_fudai);
			this.writeMsg(-1, map);
			return;
		}

		// 读表获得活动开始时间
		// Date startTime = null;
		// 获得当前时间
		// Date now = TimeUtil.now();
		// 如果不在游戏活动期间，报活动错误
		// startTime = new SimpleDateFormat("yyyy-MM-dd").parse("2017-01-19");
		// String startTime = TimeUtil.formatTime(fd_s_time);

		// 获得活动期间的userId
		Long userId = getLong("userId", 0);

		// 根据userId获得我的福袋对象
		UserLucky userLucky = LuckyDaoImpl.getInstance().getUserLucky(userId);
		// 如果userLucky不为空，则参加过拉新福袋活动
		if (userLucky != null) {
			// 如果邀请人数为0，则剩余福袋数，已开启福袋数，邀请名单均为0
			if (userLucky.getInviteeCount() != 0) {
				List<UserLucky> list = LuckyDaoImpl.getInstance().getInviteeInfo(userId);
				int totalCount = 0;
				Map<String, Object> inviteeInf = null;
				List<Object> li = new ArrayList<Object>();
				for (UserLucky u : list) {
					// 获得福袋的总数
					totalCount += u.getFeedbackCount() / 5 + 1;
					// 获得邀请名单的各种信息
					inviteeInf = new HashMap<String, Object>();
					inviteeInf.put("name", u.getUsername());
					inviteeInf.put("sex", u.getSex());
					inviteeInf.put("feedbackCount", u.getFeedbackCount() / 5 + 1);
					li.add(inviteeInf);
				}
				map.put("num", totalCount - userLucky.getOpenCount());
				map.put("time", rankingBaseInfo.getShowContent());
				map.put("list", li);
				this.result = JacksonUtil.writeValueAsString(map);
				return;
			}
		}

		map.put("num", 0);
		map.put("time", rankingBaseInfo.getShowContent());
		map.put("list", new ArrayList<Object>());
		this.writeMsg(0, map);
	}

	/**
	 * 以获得的福袋数量生成排行榜，
	 * 
	 * @return
	 * @throws SQLException
	 */
	public void getRankingList() throws SQLException {
		// 取前50条记录生成排行榜
		int number = 50;
		// 用户的id
		long userId = getLong("userId");
		// 用来装个人排行信息
		Map<String, Object> map = new HashMap<String, Object>();
		// 获得前五十名的对象集合
		List<HashMap<String, Object>> list = LuckyDaoImpl.getInstance().getRankingList(number);
		UserLucky userLucky = LuckyDaoImpl.getInstance().getUserLucky(userId);

		ActivityBean rankingBaseInfo = StaticDataManager.getSingleActivityBaseInfo(ActivityConstant.activity_hbaward);
		if (rankingBaseInfo == null) {
			map.put("code", -1);
			map.put("msg", "rankingBaseInfo is null, activity type = " + ActivityConstant.activity_hbaward);
			this.result = JacksonUtil.writeValueAsString(map);
			return;
		}

		int rank = 0;
		int myRank = -1;
		int prizeFlag = 0;
		for (HashMap<String, Object> m : list) {
			rank = rank + 1;
			long uId = (long) m.get("invitorId");
			// String name = (String) m.get("name");
			m.remove("invitorId");
			m.remove("sumCards");
			// m.put("name", name);
			m.put("rank", rank);
			int load = rankingBaseInfo.getAward(rank);
			m.put("load", load);
			// 如果该invitorId包含该玩家的,即排行榜上有名
			if (uId == userId) {
				myRank = rank;
				if (ranking_list != null) {
					if (userLucky.getPrizeFlag() == 0) {
						prizeFlag = 1;
						// userLucky.setPrizeFlag(1);
						// Map<String, Object> paraMap = new HashMap<>();
						// paraMap.put("prizeFlag", 1);
						// LuckyDaoImpl.getInstance().updateUserLucky(userId,
						// paraMap);
					} else {
						prizeFlag = userLucky.getPrizeFlag();
					}
				} else {
					prizeFlag = userLucky.getPrizeFlag();
				}
			}
		}

		int myLucky = 0;
		if (userLucky != null) {
			List<UserLucky> milist = LuckyDaoImpl.getInstance().getInviteeInfo(userId);
			int totalCount = 0;
			for (UserLucky u : milist) {
				totalCount += u.getFeedbackCount() / 5 + 1;
			}
			// myLucky = totalCount - userLucky.getOpenCount();
			myLucky = totalCount;
		}
		map.put("prizeFlag", prizeFlag);
		map.put("num", myLucky);
		map.put("myRank", myRank);
		map.put("list", list);
		map.put("time", rankingBaseInfo.getShowContent());
		map.put("load", rankingBaseInfo.getAward(myRank));
		this.writeMsg(0, map);
	}

	/**
	 * 开启福袋时
	 * 
	 * @return
	 * @throws SQLException
	 */
	public void openLucky() throws SQLException {
		Map<String, Object> map = new HashMap<String, Object>();
		if (fudai == null) {
			map.put("msg", "不在活动时间内");
			this.writeMsg(-1, map);
			return;
		}

		long userId = getLong("userId");
		// 获得参数要开启的福袋数
		int number = 1;

		UserLucky userLucky = (UserLucky) LuckyDaoImpl.getInstance().getUserLucky(userId);
		if (userLucky != null) {
			if (userLucky.getInviteeCount() != 0) {
				List<UserLucky> list = LuckyDaoImpl.getInstance().getInviteeInfo(userId);
				int luckyCount = 0;
				int openCount = userLucky.getOpenCount();
				for (UserLucky uLucky : list) {
					luckyCount += uLucky.getFeedbackCount() / 5 + 1;
				}
				// 只有当拥有的福袋总数大于等于已开启的福袋数加上要开启的福袋数之和时，才能成功开启福袋
				if (luckyCount >= openCount + number) {
					// 更改开启的福袋数
					userLucky.setOpenCount(openCount + number);
					Map<String, Object> paraMap = new HashMap<>();
					paraMap.put("openCount", openCount + number);
					LuckyDaoImpl.getInstance().updateUserLucky(userId, paraMap);
					// 计算获得的房卡数
					int cards = (int) (number * (Math.random() * 3 + 1));
					map.put("num", luckyCount - openCount - number);
					map.put("awardRoomCards", cards);
					RegInfo user = userDao.getUser(userId);
					userDao.addUserCards(user, 0, cards, CardSourceType.openLucky);
				} else {
					map.put("msg", "没有福袋!");
					this.writeMsg(-1, map);
				}
				this.result = JacksonUtil.writeValueAsString(map);
			} else {
				map.put("msg", "没有福袋!");
				this.writeMsg(-1, map);
			}
		} else {
			map.put("msg", "没有福袋!");
			this.writeMsg(-1, map);
		}
	}

	/**
	 * 领取奖品的方法
	 * 
	 * @throws SQLException
	 */
	@SuppressWarnings("unused")
	public void getAward() throws SQLException {
		Map<String, Object> map = new HashMap<String, Object>();

		// 玩家的用户id
		long userId = this.getLong("userId", 0);

		// 判断当前时间是否为活动可领取奖励时间
		if (ranking_list == null) {
			map.put("prizeFlag", 0);
			this.result = JacksonUtil.writeValueAsString(map);
			return;
		}

		UserLucky userLucky = LuckyDaoImpl.getInstance().getUserLucky(userId);
		int prizeFlag = userLucky.getPrizeFlag();
		// 判断当前玩家的福袋活动信息是否有误
		if (userLucky == null) {
			map.put("msg", "未上榜");
			this.writeMsg(-1, map);
			return;
		}

		// 排行榜记录名次
		int number = 50;
		List<HashMap<String, Object>> list = LuckyDaoImpl.getInstance().getRankingList(number);

		int rank = 0;
		int myRank = -1;
		for (HashMap<String, Object> m : list) {
			rank = rank + 1;
			long uId = (long) m.get("invitorId");
			// 如果该invitorId包含该玩家的,即排行榜上有名
			if (uId == userId) {
				myRank = rank;
			}
		}

		if (myRank < 0) {
			map.put("msg", "不在排行榜内");
			this.writeMsg(-2, map);
			return;
		}

		// 对奖品信息处理成想要的格式
		// Map<Integer, Integer> awardMap = getAwardMap(award);
		// 用来返回结果的map
		// 获得系统当前时间
		// Date today = TimeUtil.now();
		// 获得当前用户的福袋活动的信息

		int load = ranking_list.getAward(myRank);
		// 信息确认无误后，判断玩家是否领取过奖励
		if (prizeFlag == 2) {
			map.put("msg", "已经领取过了");
			this.writeMsg(-2, map);
		} else if (prizeFlag == 0) {
			// 如果玩家未领取奖励
			map.put("load", load);
			map.put("prizeFlag", 2);
			// 更新到福袋表
			userLucky.setPrizeFlag(2);
			Map<String, Object> paraMap = new HashMap<>();
			paraMap.put("prizeFlag", 2);
			LuckyDaoImpl.getInstance().updateUserLucky(userId, paraMap);
			// 更新到用户信息表
			RegInfo user = userDao.getUser(userId);
			userDao.addUserCards(user, 0, load, CardSourceType.openLucky);
			writeMsg(0, map);

		} else {
			// 玩家领取过奖励
			map.put("msg", "领取状态不对");
			this.writeMsg(-2, map);
		}
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}
}
