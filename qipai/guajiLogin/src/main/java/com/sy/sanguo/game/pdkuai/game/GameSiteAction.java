package com.sy.sanguo.game.pdkuai.game;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

import com.sy.sanguo.common.struts.StringResultType;
import com.sy.sanguo.game.bean.enums.CardSourceType;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import com.sy.sanguo.common.log.GameBackLogger;
//import com.sy.sanguo.common.server.Server;
import com.sy.sanguo.common.util.HttpUtil;
import com.sy.sanguo.common.util.MD5Util;
import com.sy.sanguo.game.bean.RegInfo;
import com.sy.sanguo.game.bean.Server;
import com.sy.sanguo.game.msg.GameSiteMsg;
import com.sy.sanguo.game.pdkuai.action.BaseAction;
import com.sy.sanguo.game.pdkuai.db.bean.GameSite;
import com.sy.sanguo.game.pdkuai.db.bean.GameSiteAward;
import com.sy.sanguo.game.pdkuai.db.bean.MatchInviteCode;
import com.sy.sanguo.game.pdkuai.db.bean.UserGameSite;
import com.sy.sanguo.game.pdkuai.db.dao.GameSiteDao;
import com.sy.sanguo.game.service.SysInfManager;

public class GameSiteAction extends BaseAction {

	@Override
	public String execute() {

		int functype = this.getInt("funcType");

		switch (functype) {
		case 1:
			gameSiteList();// 比赛场列表
			break;
		case 2:
			applyGame();// 比赛报名
			break;
		case 3:
			verifyInviteCode();// 验证邀请码
			break;
		case 4:
			cancelApplyGame();// 取消比赛报名
			break;
		case 5:
			awardList();// 获得奖品列表
			break;
		case 6:
			takeAward();// 领奖
			break;
		case 7:
			gameSiteState();// 比赛状态
			break;
		case 8:
			singleGameSite();// 单个比赛场
			break;
		case 9:
			createInviteCode();// 生成比赛场邀请码
			break;
		case 10:
			beforeDismissMatch();// 比赛场解散之前请求
			break;
		case 11:
			dismissMatch();// 比赛场解散请求
			break;
		default:
			break;
		}

		return result;
	}
	
	// 比赛场解散之前请求
	public String beforeDismissMatch() {
		this.result = "success";
		return StringResultType.RETURN_ATTRIBUTE_NAME;
	}
	
	// 比赛场解散请求
	public String dismissMatch() {
		String flag = "fail";
		int gameSiteId =  Integer.parseInt(this.getString("gameSiteId"));
		
		GameSite gameSiteTemp = GameSiteDao.getInstance().getGameSiteById(gameSiteId);
		if (gameSiteTemp == null) {
			this.result = flag;
			return StringResultType.RETURN_ATTRIBUTE_NAME;
		}
		
		int gameCondition = gameSiteTemp.getGameCondition();
		int needPropCount = gameSiteTemp.getNeedPropCount();
		
		if (1 != gameCondition || needPropCount <= 0) {
			this.result = "success";
			return StringResultType.RETURN_ATTRIBUTE_NAME;
		}

		List<UserGameSite> userIds = GameSiteDao.getInstance().gameSiteApplyUserId(gameSiteId);
		RegInfo user = null;
		int update = 0;
		
		for (UserGameSite temp : userIds) {
			try {
				user = userDao.getUser(temp.getUserId());
				if (user == null) {
					GameBackLogger.SYS_LOG.error("数据库取玩家ID:" + temp.getUserId() + "数据出错");
					continue;
				}
				update = userDao.addUserCards(user, 0, needPropCount, 0, CardSourceType.match_diss);
				if (update <= 0) {
					GameBackLogger.SYS_LOG.error("比赛场退还房卡失败,玩家ID:" + temp.getUserId());
				}
			} catch (Exception e) {
				GameBackLogger.SYS_LOG.error("比赛场退还房异常:", e);
			}
		}

		this.result = "success";
		return StringResultType.RETURN_ATTRIBUTE_NAME;
	}

	// 生成比赛场邀请码
	private void createInviteCode() {
		Map<String, Object> result = new HashMap<>();
		int code = 0;
		
 		int gameSiteId = this.getInt("gameSiteId");
 		int count = GameSiteDao.getInstance().oneMatchInviteCodeCnt(gameSiteId);
 		if (count > 0) {
 			result.put("msg", "添加失败，比赛场ID:" + gameSiteId + "已有邀请码库记录");
 			this.writeMsg(-1, result);
 			return;
 		}
 		
		int amount = this.getInt("amount");
		String inviteCode = null;
		
		Set<String> inviteCodes = new HashSet<String>();
		
		while (inviteCodes.size() < amount) {
			inviteCode = RandomStringUtils.randomAlphanumeric(10);
			inviteCodes.add(inviteCode);
		}

		for (String temp : inviteCodes) {
			GameSiteDao.getInstance().addMatchInviteCode(gameSiteId, temp);
		}
		
		result.put("msg", "比赛场ID:" + gameSiteId + "添加" + amount + "条邀请码库记录成功");
		this.writeMsg(code, result);
	}

	// 单个比赛场
	private void singleGameSite() {
		Map<String, Object> result = new HashMap<>();

		int gameSiteId = this.getInt("gameSiteId");
		GameSite gameSiteTemp = GameSiteDao.getInstance().getGameSiteById(gameSiteId);

		if (gameSiteTemp == null) {
			this.writeMsg(-1, result);
			return;
		}

		long userId = this.getLong("userId");
		UserGameSite userGameSite = GameSiteDao.getInstance().queryUserGameSite(userId);

		Server server = SysInfManager.getInstance().getServer(gameSiteTemp.getServerId());
		String chathost = "";
		if (server != null) {
			chathost = server.getChathost();
		} else {
			GameBackLogger.SYS_LOG.error("server is null -->id:" + userGameSite.getServerId());
		}

		long currTime = System.currentTimeMillis();
		long tenMinutes = 10 * 60 * 1000;
		int code = 0;
		int status = 1;

		if (currTime > gameSiteTemp.getBeginTime().getTime()) {
			if (userGameSite == null || userGameSite.getGameSiteId() != gameSiteId) {
				this.writeMsg(-1, result);
				return;
			}

			status = 2;
			long gameSiteTableId = userGameSite.getTableId();
			result.put("gameSiteTableId", gameSiteTableId);
			result.put("connectHost", chathost);

		} else if (currTime + tenMinutes > gameSiteTemp.getBeginTime().getTime()) {
			if (userGameSite == null || userGameSite.getGameSiteId() != gameSiteId) {
				this.writeMsg(-1, result);
				return;
			}
			status = 3;
			result.put("countDown", gameSiteTemp.getBeginTime().getTime() - currTime);
		}

		int gameMaxNumber = gameSiteTemp.getGameMaxNumber();
		int currNumber = GameSiteDao.getInstance().getApplyNumber(gameSiteId);

		if (currNumber >= gameMaxNumber) {
			result.put("applyNumber", currNumber);
		}

		result.put("status", status);
		this.writeMsg(code, result);
	}

	// 比赛状态
	private void gameSiteState() {
		Map<String, Object> result = new HashMap<>();

		long userId = this.getLong("userId");
		int gameSiteId = this.getInt("gameSiteId");

		GameSiteDao gameSiteDao = GameSiteDao.getInstance();

		List<UserGameSite> userGameSites = gameSiteDao.userGameSiteRank(gameSiteId, 0);

		int selfRank = 1;
		int integral = 0;
		for (UserGameSite temp : userGameSites) {

			if (temp.getUserId() == userId) {
				integral = temp.getIntegral();
				break;
			}
			selfRank++;
		}

		result.put("allRank", userGameSites);
		result.put("selfRank", selfRank);
		result.put("integral", integral);

		this.writeMsg(0, result);
	}

	// 领奖
	private void takeAward() {
		Map<String, Object> result = new HashMap<>();

		long userId = this.getLong("userId");
		long id = this.getLong("id");
		long telephone = 0;
		try {
			telephone = this.getLong("telephone");
		} catch (Exception e) {
			result.put("msg", "手机号码只能为数字");
			this.writeMsg(-5, result);
			return;
		}

		GameSiteDao gameSiteDao = GameSiteDao.getInstance();
		GameSiteAward gameSiteAward = gameSiteDao.takeAwardById(id);

		if (gameSiteAward == null) {
			result.put("msg", "没有该条奖品记录");
			this.writeMsg(-1, result);
			return;
		}

		if (gameSiteAward.getUserId() != userId) {
			result.put("msg", "不能领取别人的奖品");
			this.writeMsg(-2, result);
			return;
		}

		if (gameSiteAward.getAwardStatus() != 1) {
			result.put("msg", "该奖品不是");
			this.writeMsg(-3, result);
			return;
		}

		// TODO 要不要验证手机号码

		Map<String, Object> sqlMap = new HashMap<String, Object>();
		sqlMap.put("id", id);
		sqlMap.put("awardStatus", 2);
		sqlMap.put("telephone", telephone);
		int count = gameSiteDao.updateAwardStatus(sqlMap);

		if (count <= 0) {
			result.put("msg", "领取奖品失败");
			this.writeMsg(-4, result);
			return;
		}

		result.put("msg", "奖品成功申请成功");
		result.put("awardStatus", 2);
		result.put("id", id);
		this.writeMsg(0, result);
	}

	// 获得奖品列表
	private void awardList() {
		Map<String, Object> result = new HashMap<>();
		long userId = this.getLong("userId");

		GameSiteDao gameSiteDao = GameSiteDao.getInstance();
		List<GameSiteAward> awards = gameSiteDao.takeAwardList(userId);

		result.put("msg", awards);
		this.writeMsg(0, result);
	}

	// 取消比赛报名
	private void cancelApplyGame() {
		Map<String, Object> result = new HashMap<>();

		int gameSiteId = this.getInt("gameSiteId");
		long userId = this.getLong("userId");

		GameSiteDao gameSiteDao = GameSiteDao.getInstance();

		GameSite gameSite = gameSiteDao.getGameSiteById(gameSiteId);
		if (gameSite == null) {
			result.put("msg", "没有取到ID为:" + gameSiteId + "的比赛场！");
			this.writeMsg(-2, result);
			GameBackLogger.SYS_LOG.error("GameSiteAction error : 没有取到ID为:" + gameSiteId + "的比赛场！");
			return;
		}

		UserGameSite userGameSite = gameSiteDao.queryUserGameSite(userId);
		if (userGameSite == null) {

			result.put("msg", "没有取到玩家Id:" + userId + "的比赛场报名数据！");
			this.writeMsg(-3, result);
			GameBackLogger.SYS_LOG.error("GameSiteAction error : 没有取到玩家Id:" + userId + "的比赛场报名数据！");
			return;
		} else {

			if (gameSiteId != userGameSite.getGameSiteId()) {
				result.put("msg", "您没有报名该比赛场！");
				this.writeMsg(-4, result);
				GameBackLogger.SYS_LOG.error("GameSiteAction error : 您没有报名该比赛场！");
				return;
			} else {

				Map<String, Object> sqlMap = new HashMap<String, Object>();
				sqlMap.put("userId", userId);
				sqlMap.put("gameSiteId", 0);
				sqlMap.put("integral", 0);
				sqlMap.put("roundNum", 0);
				sqlMap.put("playGame", 0);
				sqlMap.put("turnBlank", 0);
				sqlMap.put("tableId", 0);
				sqlMap.put("serverId", 0);
				sqlMap.put("applyTime", null);

				int count = gameSiteDao.updateUserGameSite(sqlMap);

				if (count > 0) {
					if (gameSite.getGameCondition() > 0) {
						// TODO 退还玩家道具或房卡
					}

					result.put("msg", "取消报名成功！");
					this.writeMsg(0, result);
				} else {
					result.put("msg", "取消报名失败！");
					this.writeMsg(-1, result);
				}
			}
		}
	}

	// 验证邀请码
	public void verifyInviteCode() {
		Map<String, Object> result = new HashMap<>();

		int gameSiteId = this.getInt("gameSiteId");
		long userId = this.getLong("userId");

		String inviteCode = this.getString("inviteCode");
		GameSiteDao gameSiteDao = GameSiteDao.getInstance();

		GameSite gameSite = gameSiteDao.getGameSiteById(gameSiteId);
		if (gameSite == null) {
			result.put("msg", "没有取到ID为:" + gameSiteId + "的比赛场！");
			this.writeMsg(-1, result);
			return;
		}

		int gameSiteCode = gameSite.getInviteCode();
		if (gameSiteCode <= 0) {
			result.put("msg", "该比赛场不需要验证邀请码！");
			this.writeMsg(-2, result);
			return;
		}

		MatchInviteCode matchInviteCode = gameSiteDao.getMatchInviteCode(gameSiteId, inviteCode);
		if (matchInviteCode == null) {
			result.put("msg", "无效的邀请码！");
			this.writeMsg(-3, result);
			return;
		}
		
		if (matchInviteCode.getUseFlag() > 0 && matchInviteCode.getUseUserId() > 0) {
			result.put("msg", "该邀请码已被使用！");
			this.writeMsg(-6, result);
			return;
		}

		UserGameSite userGameSite = gameSiteDao.queryUserGameSite(userId);

		int code = 0;
		if (userGameSite == null) {

			Map<String, Object> sqlMap = new HashMap<String, Object>();
			sqlMap.put("userId", userId);
			sqlMap.put("gameSiteId", 0);
			sqlMap.put("integral", 0);
			sqlMap.put("roundNum", 0);
			sqlMap.put("playGame", 0);
			sqlMap.put("turnBlank", 0);
			sqlMap.put("tableId", 0);
			sqlMap.put("serverId", 0);
			sqlMap.put("passInviteCode", gameSiteId);

			try {
				gameSiteDao.addUserGameSite(sqlMap);
				gameSiteDao.updateMatchInviteCode(matchInviteCode.getId(), userId);
				result.put("msg", "激活成功！");
				result.put("verifyStatus", 1);
				result.put("gameSiteId", gameSiteId);
			} catch (Exception e) {
				code = -4;
				result.put("msg", "激活失败！");
			}

			this.writeMsg(code, result);
			return;
		} else {
			List<String> passGameId = new ArrayList<String>();
			String passInviteCode = userGameSite.getPassInviteCode();

			if (!StringUtils.isBlank(passInviteCode)) {
				String[] arr = passInviteCode.split(";");
				for (String str : arr) {
					passGameId.add(str);
				}
				passInviteCode += ";" + gameSiteId;
			} else {
				passInviteCode = "" + gameSiteId;
			}

			if (passGameId.contains(gameSiteId + "")) {
				code = -5;
				result.put("msg", "该比赛场已激活过！");
				this.writeMsg(code, result);
				return;
			}

			Map<String, Object> sqlMap = new HashMap<String, Object>();
			sqlMap.put("userId", userId);
			sqlMap.put("passInviteCode", passInviteCode);
			gameSiteDao.updateUserGameSite(sqlMap);
			gameSiteDao.updateMatchInviteCode(matchInviteCode.getId(), userId);
			result.put("msg", "激活成功！");
			result.put("verifyStatus", 1);
			result.put("gameSiteId", gameSiteId);
			this.writeMsg(code, result);
		}

	}

	// 比赛场报名
	public void applyGame() {
		Map<String, Object> result = new HashMap<>();

		long userId = this.getLong("userId");
		
		RegInfo regInfo = null;
		try {
			regInfo = userDao.getUser(userId);
			if (regInfo == null) {
				result.put("msg", "报名参数有误！userId:" + userId);
				this.writeMsg(-10, result);
				return;
			}
		} catch (SQLException e) {
			GameBackLogger.SYS_LOG.error("userDao.getUser userId = " + userId + " error::", e);
		}
		
		if (regInfo.getPlayingTableId() > 0) {
			result.put("msg", "你正在普通场打牌中,暂时不能报名比赛场,请解散房间");
			this.writeMsg(-11, result);
			return;
		}

		GameSiteDao gameSiteDao = GameSiteDao.getInstance();

		UserGameSite userGameSite = gameSiteDao.queryUserGameSite(userId);
		if (userGameSite != null && userGameSite.getGameSiteId() > 0) {
			result.put("msg", "你已经参加其它比赛场了，不能同时参加两个比赛");
			this.writeMsg(-1, result);
			return;
		}

		int gameSiteId = this.getInt("gameSiteId");
		GameSite gameSite = gameSiteDao.getGameSiteById(gameSiteId);

		if (gameSite == null) {
			result.put("msg", "该比赛场不存在！");
			this.writeMsg(-2, result);
			return;
		}

		// 邀请码的判断
		if (gameSite.getInviteCode() > 0) {
			if (userGameSite == null) {
				result.put("msg", "没有验证码，不能报名！");
				this.writeMsg(-3, result);
				return;
			}

			String passInviteCode = userGameSite.getPassInviteCode();
			if (StringUtils.isBlank(passInviteCode)) {
				result.put("msg", "没有验证码，不能报名！");
				this.writeMsg(-9, result);
				return;
			}

			List<String> passGameId = new ArrayList<String>();
			if (!StringUtils.isBlank(passInviteCode)) {
				String[] arr = passInviteCode.split(";");
				for (String str : arr) {
					passGameId.add(str);
				}
			}
			if (!passGameId.contains(gameSite.getId() + "")) {
				result.put("msg", "没有验证码，不能报名！");
				this.writeMsg(-5, result);
				return;
			}
		}

		Timestamp applyTime = gameSite.getApplyTime();
		Timestamp beginTime = gameSite.getBeginTime();
		long tenMinutes = 10 * 60 * 1000;
		long currTime = System.currentTimeMillis();

		if (currTime < applyTime.getTime()) {
			result.put("msg", "报名时间还没开始！");
			this.writeMsg(-8, result);
			return;
		}

		if (currTime >= (beginTime.getTime() - tenMinutes)) {
			result.put("msg", "报名时间已结束！");
			this.writeMsg(-6, result);
			return;
		}

		int gameMaxNumber = gameSite.getGameMaxNumber();
		int currNumber = gameSiteDao.getApplyNumber(gameSiteId);

		if (currNumber >= gameMaxNumber) {
			result.put("msg", "报名人数已满，请留意下次报名时间！");
			this.writeMsg(-4, result);
			return;
		}
		
		if (gameSite.getGameCondition() == 1) {
			
			int needPropCount = gameSite.getNeedPropCount();
			
			//try {
				// RegInfo regInfo = userDao.getUser(userId);
				int flag = userDao.consumeUserCards(regInfo, needPropCount);
				if (flag != 0) {
					if (flag == 4) {
						result.put("msg", "房卡不够！");
					} else {						
						result.put("msg", "报名扣除房卡失败！错误码:" + flag);
					}
					this.writeMsg(-7, result);
					return;
				}
			//} catch (SQLException e) {
			//	GameBackLogger.SYS_LOG.error("userDao.getUser userId = " + userId + " error::", e);
			//}
			
		} else if (gameSite.getGameCondition() > 1) {
			// TODO 其它条件，暂时不处理
		}

		Map<String, Object> sqlMap = new HashMap<String, Object>();
		sqlMap.put("userId", userId);
		sqlMap.put("gameSiteId", gameSiteId);
		sqlMap.put("integral", 1000);
		sqlMap.put("applyTime", new Timestamp(System.currentTimeMillis()));

		int code = 0;
		if (userGameSite == null) {
			sqlMap.put("roundNum", 0);
			sqlMap.put("playGame", 0);
			sqlMap.put("turnBlank", 0);
			sqlMap.put("tableId", 0);
			sqlMap.put("serverId", 0);
			sqlMap.put("passInviteCode", "");
			gameSiteDao.addUserGameSite(sqlMap);
		} else {
			gameSiteDao.updateUserGameSite(sqlMap);
		}

		result.put("msg", "报名成功");
		result.put("gameSiteId", gameSiteId);
		result.put("applyStatus", 1);
		result.put("applyNumber", currNumber + 1);

		if (currNumber + 1 > gameSite.getApplyMaxNumber()) {
			gameSiteDao.updateApplyMaxNumber(gameSiteId, currNumber + 1);
		}
		pushApplyNumber(gameSiteId, currNumber + 1);

		this.writeMsg(code, result);
	}

	public void pushApplyNumber(int gameSiteId, int applyNumber) {

		Collection<Server> servers = SysInfManager.loadServers();
		if (servers.size() <= 0) {
			return;
		}

		List<UserGameSite> userIds = GameSiteDao.getInstance().gameSiteApplyUserId(gameSiteId);
		if (userIds.size() <= 0) {
			return;
		}

		StringBuffer sb = new StringBuffer();

		for (UserGameSite temp : userIds) {
			sb.append(temp.getUserId());
			sb.append(",");
		}

		String userIdStr = sb.substring(0, sb.length() - 1);

		String host = "";
		HttpUtil http = null;
		Map<String, String> params = null;
		String sytime = String.valueOf(System.currentTimeMillis());
		String md5 = MD5Util.getStringMD5(sytime + "7HGO4K61M8N2D9LARSPU", "utf-8");

		try {
			for (Server server : servers) {
				host = server.getIntranet();
				if (StringUtils.isBlank(host)) {
					continue;
				}
				params = new HashMap<>();
				params.put("type", 3 + "");
				params.put("funcType", 2 + "");
				params.put("userIdStr", userIdStr);
				params.put("gameSiteId", gameSiteId + "");
				params.put("applyNumber", applyNumber + "");
				params.put("sytime", sytime);
				params.put("sysign", md5);
				http = new HttpUtil(host);
				http.post(params);
			}
		} catch (Exception e) {
			GameBackLogger.SYS_LOG.error("pushApplyNumber error::", e);
		}
	}

	// 获取比赛场列表
	public void gameSiteList() {
		Map<String, Object> result = new HashMap<>();

		long userId = this.getLong("userId");

		GameSiteDao gameSiteDao = GameSiteDao.getInstance();
		List<GameSite> gameSites = gameSiteDao.queryAllGameSite();

		UserGameSite userGameSite = gameSiteDao.queryUserGameSite(userId);

		List<String> passGameId = new ArrayList<String>();
		if (userGameSite != null) {
			String passInviteCode = userGameSite.getPassInviteCode();

			if (!StringUtils.isBlank(passInviteCode)) {
				String[] arr4 = passInviteCode.split(";");
				for (String str : arr4) {
					passGameId.add(str);
				}
			}
		}

		List<GameSiteMsg> gameSiteMsgs = new ArrayList<GameSiteMsg>();
		GameSiteMsg tempMsg;
		int applyNumber = 0;

		Timestamp currTime = new Timestamp(System.currentTimeMillis());
		int tempVal1, tempVal2, tempVal3;

		outer: for (GameSite gameSite : gameSites) {
			tempMsg = new GameSiteMsg();

			try {
				BeanUtils.copyProperties(tempMsg, gameSite);
			} catch (IllegalAccessException | InvocationTargetException e) {
				GameBackLogger.SYS_LOG.error("gameSiteList error::", e);
			}

			String gameReward = gameSite.getGameReward();
			String[] arr1 = gameReward.split("#");

			for (String str : arr1) {
				String[] arr2 = str.split(";");
				tempMsg.getGameRewardList().put(Integer.parseInt(arr2[0]), arr2[1]);
			}

			String configRound = gameSite.getConfigRound();
			if (StringUtils.isBlank(configRound)) {
				// 没有配置轮数,直接跳过
				System.out.println("没有配置轮数,直接跳过");
				continue;
			}

			arr1 = configRound.split(";");
			for (String str : arr1) {
				String[] arr2 = str.split(",");
				tempVal1 = Integer.parseInt(arr2[0]);
				tempVal2 = Integer.parseInt(arr2[1]);

				if (tempVal1 > 1) {
					tempVal3 = tempMsg.getRoundMap().get(tempVal1 - 1);
					if (tempVal2 > tempVal3) {
						System.out.println("后一轮的人数,不能大于前一轮的人数");
						break outer;
					}
				}

				tempMsg.getRoundMap().put(tempVal1, tempVal2);
			}

			if (tempMsg.getRoundMap().get(1) > tempMsg.getGameNumber()) {
				System.out.println("第一轮人数不能大于最少报名人数");
				continue;
			}

			String configBout = gameSite.getConfigBout();
			arr1 = configBout.split(";");
			for (String str : arr1) {
				String[] arr2 = str.split(",");
				tempMsg.getBoutMap().put(Integer.parseInt(arr2[0]), Integer.parseInt(arr2[1]));
			}

			if (tempMsg.getBoutMap().size() < tempMsg.getRoundMap().size()) {
				// 局数size小于配置轮数,直接跳过
				System.out.println("局数size小于配置轮数,直接跳过");
				continue;
			}

			String configTimes = gameSite.getConfigTimes();
			arr1 = configTimes.split(";");
			for (String str : arr1) {
				String[] arr2 = str.split(",");
				tempMsg.getTimesMap().put(Integer.parseInt(arr2[0]), Integer.parseInt(arr2[1]));
			}

			if (tempMsg.getTimesMap().size() < tempMsg.getRoundMap().size()) {
				// 底分倍数size小于配置轮数,直接跳过
				System.out.println("底分倍数size小于配置轮数,直接跳过");
				continue;
			}

			if (userGameSite != null && userGameSite.getGameSiteId() == gameSite.getId()) {
				tempMsg.setApplyStatus(1);
			}

			if (gameSite.getInviteCode() > 0 && (passGameId.contains(gameSite.getId() + ""))) {
				tempMsg.setVerifyStatus(1);
			}

			applyNumber = gameSiteDao.getApplyNumber(gameSite.getId());
			tempMsg.setApplyNumber(applyNumber);
			gameSiteMsgs.add(tempMsg);
		}

		int awardCnt = gameSiteDao.userCanAwardCnt(userId);

		result.put("canReward", awardCnt);
		result.put("msg", gameSiteMsgs);
		result.put("self", userGameSite);
		result.put("currTime", currTime);

		int gameType = this.getInt("gameType", 0);
		com.sy.sanguo.game.bean.Server server = null;
		if (gameType != 0) {
			// 创建房间的时候
			server = SysInfManager.loadServer(gameType,1,true);
		}
		Map<String, Object> serverMap = new HashMap<String, Object>();
		if (server != null) {
			serverMap.put("serverId", server.getId());
			serverMap.put("httpUrl", server.getHost());
			serverMap.put("connectHost", server.getChathost());
			result.put("server", serverMap);
		}
		this.writeMsg(0, result);
	}

}
