package com.sy.sanguo.game.competition.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sy.mainland.util.HttpUtil;
import com.sy.sanguo.game.bean.RegInfo;
import com.sy.sanguo.game.bean.ServerConfig;
import com.sy.sanguo.game.competition.dao.CompetitionApplyDao;
import com.sy.sanguo.game.competition.job.CompetitionJobSchedulerThreadPool;
import com.sy.sanguo.game.competition.model.db.CompetitionApplyDB;
import com.sy.sanguo.game.competition.model.db.CompetitionPlayingDB;
import com.sy.sanguo.game.competition.model.db.CompetitionScoreDetailDB;
import com.sy.sanguo.game.competition.model.enums.CompetitionApplyStatusEnum;
import com.sy.sanguo.game.competition.model.param.CompetitionApplyParam;
import com.sy.sanguo.game.competition.model.param.CompetitionClearingModelRes;
import com.sy.sanguo.game.competition.model.param.CompetitionRefreshQueueModel;
import com.sy.sanguo.game.competition.util.BigMap;
import com.sy.sanguo.game.dao.ServerDaoImpl;
import com.sy.sanguo.game.dao.UserDao;
import com.sy.sanguo.game.pdkuai.util.LogUtil;
import com.sy599.game.util.MD5Util;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 消息推送
 * @author Guang.OuYang
 * @date 2020/5/20-16:29
 */
@Service
public class CompetitionPushService {
	/**申请消息,推送人数刷新*/
	private final static ConcurrentLinkedDeque<CompetitionRefreshQueueModel> APPLY_HUMAN_MSG = new ConcurrentLinkedDeque<>();
	/**URL PRIVATE KEY*/
	private final static String APP_KEY = "qweh#$*(_~)lpslot;589*/-+.-8&^%$#@!";

	/**刷新当前人数入口*/
	private final static String REFRESH_HUMAN_ACTION_NAME = "playingRefreshNotify",
	/**通知客户端切服入口*/
	CHANGE_SERVER_ACTION_NAME = "playingChangePlayerServer",
	/**批量玩家匹配入口*/
	BATCH_MATCH_ACTION_NAME = "playingMatchPushBatchInnerRoom",
	/**比赛结算界面入口*/
	SHOW_CLEARING_INFO_ACTION_NAME = "playingShowClearingInfo",
	/**手动清理房间*/
	CLEARING_INVALID_ROOM = "clearingInvalidRoom",
	/**开赛通知6002*/
	BEGIN_PLAYING_NOTIFY = "beginPlayingNotify",
	/**退赛通知*/
	CANCEL_PLAYING_NOTIFY = "cancelPlayingNotify",
	/**结赛通知*/
	END_PLAYING_NOTIFY = "endPlayingNotify",
	/**扣除金币*/
	NOTIFY_AND_CHANGE_GOLDS = "notifyAndChangeGolds",
	//开赛6003
	BEGIN_PLAYING_PUSH = "beginPlayingPush",
	//获取比赛服ID
	GET_SERVER_AND_COMPETITION_ROOM_ID = "getServerAndCompetitionRoomId",
	//推动榜单6004
	PLAYING_RANK_PUSH = "playingRankPush",
	//批量创建房间
	PLAYING_BATCH_CREATE_ROOM = "playingBatchCreateRoom",
	//批量加入房间
	PLAYING_BATCH_RANDOM_JOIN_ROOM = "playingBatchRandomJoinRoom",
	//批量开局
	PLAYING_BATCH_STAT_ROOM_GAME = "playingBatchStatRoomGame"
	;
//	/**获得一个比赛房可用的服务器Id*/
//	GET_COMPETITION_SERVER = "getCompetitionServer";


	@Value("${competition.push.delay:1000}")
	private int pushDelay;
	@Autowired
	private CompetitionJobSchedulerThreadPool competitionJobSchedulerThreadPool;
	@Autowired
	private CompetitionPlayingService competitionPlayingService;
	@Autowired
	private CompetitionRankService competitionRankService;
	@Autowired
	private CompetitionApplyService competitionApplyService;
	@Autowired
	private CompetitionApplyDao competitionApplyDao;
	@Autowired
	private CompetitionRankRefreshService competitionRankRefreshService;
	@Autowired
	private CompetitionMatchService competitionMatchService;

	private UserDao userDao = UserDao.getInstance();

	/**
	 *@description 初始化赛场人数推送调度器
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/5/25
	 */
	public void initPushMsg() {
		//固定间隔刷新
		competitionJobSchedulerThreadPool.push_scheduler.scheduleWithFixedDelay(this::pushRepeated, 0, pushDelay, TimeUnit.MILLISECONDS);

		competitionJobSchedulerThreadPool.getClearingMainPool().scheduleWithFixedDelay(competitionRankRefreshService::pushRankRefresh, 0, 5000, TimeUnit.MILLISECONDS);

		competitionJobSchedulerThreadPool.getMatchMainPool().scheduleWithFixedDelay(competitionMatchService::repeatedMatch, 0, 3000, TimeUnit.MILLISECONDS);

//		competitionJobSchedulerThreadPool.getTotalRankRefreshPool().scheduleAtFixedRate(competitionRankRefreshService::refreshTotalRank, 0, 10000, TimeUnit.MILLISECONDS);
	}

	/**
	 *@description 推送, 当前人数
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/5/25
	 */
	public void pushApplyCurHumanRefresh(CompetitionRefreshQueueModel queueModel) {
		if (!APPLY_HUMAN_MSG.contains(queueModel))
			APPLY_HUMAN_MSG.offer(queueModel);
	}


	public void pushRepeated() {
		try {
			pushApplyCurHumanRefresh();
		} catch (Exception e) {
			LogUtil.e("competition|push|error|"+e);
		}
	}

	/**
	 *@description 申请人数刷新, 给所有报名该场活动的玩家推送人数
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/5/21
	 */
	private void pushApplyCurHumanRefresh() {
		if (!APPLY_HUMAN_MSG.isEmpty()) {
			LogUtil.i("competition|push|refresh apply human|begin");

			long t = System.currentTimeMillis();
			int count = 0;

			CompetitionRefreshQueueModel m = APPLY_HUMAN_MSG.removeFirst();

			Optional<CompetitionPlayingDB> playingOptional = competitionPlayingService.getPlaying(m.getPlayingId());

			int curHuman = playingOptional.get().getCurHuman();

			long playingId = playingOptional.get().getId();

			//当前全部报名人
			Optional<List<CompetitionApplyDB>> optionalCompetitionApplyDB = competitionApplyDao.queryForList(CompetitionApplyDB.builder().playingId(playingId).status(CompetitionApplyStatusEnum.NORMAL).build());

			if (optionalCompetitionApplyDB.isPresent() && optionalCompetitionApplyDB.get().size()>0) {

				List<Integer> params = new ArrayList<>();
				List<Long> users = new ArrayList<>();
				CompetitionCommonPushModel competitionCommonPushModel = new CompetitionCommonPushModel()
						.addParam("type", REFRESH_HUMAN_ACTION_NAME)
						.addParam("params", params)
						.addParam("user", users);

				//参数列表0消息id,1当前人数
				params.add((int) playingOptional.get().getId());
				params.add(1);

				Iterator<CompetitionApplyDB> iterator = optionalCompetitionApplyDB.get().iterator();
				while (iterator.hasNext()) {
					CompetitionApplyDB next = iterator.next();
					//当前赛场内全部玩家
					users.add(next.getUserId());
					//替换人数
					params.set(1, Math.max(0, curHuman));
					count++;
				}

				try {
					pushToGameServer(competitionCommonPushModel);
				}
				catch (Exception e) {
					LogUtil.e("competition|push|Push queue error : ", e);
				}
			}

			LogUtil.i("competition|push|refresh apply human|end|" + (System.currentTimeMillis() - t) + "ms|count|" + count + "|" + curHuman);
		}
	}

	/**
	 *@description 切换玩家服务器
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/4
	 */
	public String cgePlayServerPush(List<Long> userIds, Long configId, Long bindServerId) {
		try {
			LogUtil.i("competition|push|cgePlayServerPush|server|" + bindServerId + " " + "|userIds|" + Optional.ofNullable(userIds).orElse(Collections.emptyList()).size());
			CompetitionCommonPushModel arg = new CompetitionCommonPushModel()
					.addParam("type", CHANGE_SERVER_ACTION_NAME)
					.addParam("bindServerId", bindServerId)
					.addParam("playingConfigId", configId)
					.addParam("user", new ArrayList<>(userIds));
			arg.setNotFilterOffline(true);

			arg.setTimeout(90);

			return pushToGameServer(arg);
		}
		catch (Exception e) {
			LogUtil.e("competition|push|cgePlayServerPush error : ", e);
		}

		return "";
	}

	/**
	 *@description 玩家匹配, 玩家批量合并推送
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/4
	 */
	public String matchPushBatchInnerRoom(CompetitionPushModel args) {
		try {
			if (CollectionUtils.isEmpty(args.getUserIds())) {
				return null;
			}

			return clearingModelPush(BATCH_MATCH_ACTION_NAME, args);
		}
		catch (Exception e) {
			LogUtil.e("competition|push|matchPushBatchInnerRoom error : ", e);
		}

		return null;
	}

	/**
	 *@description 玩家匹配, 玩家批量合并推送
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/4
	 */
	public String playingBatchCreateRoom( int tableCount, long bindServerId, CompetitionClearingModelRes clearingModelRes) {
		try {
			CompetitionCommonPushModel pushArgs = null;
			try {
				//默认一批比赛中的玩家一定是一个服务器
				pushArgs = new CompetitionCommonPushModel()
						.addParam("type", PLAYING_BATCH_CREATE_ROOM)
						.addParam("tableCount", tableCount)
						.addParam("bindServerId", bindServerId)
						.addParam("clearingModel", JSONObject.toJSONString(clearingModelRes))
						.notFilterOffline(true);
//						.offlineCancelPlaying(args.getPlayingId());

				pushArgs.setTimeout(5);

				return push(ServerDaoImpl.getInstance().queryServer((int) bindServerId), pushArgs, Collections.emptyList());
			} catch (Exception e) {
				LogUtil.e("competition|playingBatchCreateRoom|" + pushArgs.getParams().get("type") + "|error|" + e + "|" + pushArgs, e);
			}
		} catch (Exception e) {
			LogUtil.e("competition|push|matchPushBatchInnerRoom error : ", e);
		}

		return null;
	}

	/**
	 *@description 玩家匹配, 玩家批量合并推送
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/4
	 */
	public String playingBatchRandomJoinRoom(List<Long> userIds, long bindServerId, CompetitionClearingModelRes clearingModelRes) {

		try {
			if (CollectionUtils.isEmpty(userIds)) {
				return null;
			}
			CompetitionCommonPushModel pushArgs = null;
			try {
				//默认一批比赛中的玩家一定是一个服务器
				pushArgs = new CompetitionCommonPushModel()
						.addParam("type", PLAYING_BATCH_RANDOM_JOIN_ROOM)
						.addParam("user", new ArrayList<>(userIds))
						.addParam("bindServerId", bindServerId)
						.addParam("clearingModel", JSONObject.toJSONString(clearingModelRes))
						.notFilterOffline(true);
//						.offlineCancelPlaying(args.getPlayingId())

				if (clearingModelRes.isFirstMatch()) {
					pushArgs.setTimeout(5);
				}

				return push(ServerDaoImpl.getInstance().queryServer((int) bindServerId), pushArgs, Collections.emptyList());
			}
			catch (Exception e) {
				LogUtil.e("competition|playingBatchRandomJoinRoom|" + pushArgs.getParams().get("type") + "|error|" + e + "|" + pushArgs, e);
			}
		}
		catch (Exception e) {
			LogUtil.e("competition|push|matchPushBatchInnerRoom error : ", e);
		}

		return null;
	}

	/**
	 *@description 玩家匹配, 批量开局
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/4
	 */
	public String playingBatchStatRoomGame(long bindServerId, CompetitionClearingModelRes clearingModelRes) {
		try {
			CompetitionCommonPushModel pushArgs = null;
			try {
				//默认一批比赛中的玩家一定是一个服务器
				pushArgs = new CompetitionCommonPushModel()
						.addParam("type", PLAYING_BATCH_STAT_ROOM_GAME)
						.addParam("bindServerId", bindServerId)
						.addParam("clearingModel", JSONObject.toJSONString(clearingModelRes))
						.notFilterOffline(true);
//						.offlineCancelPlaying(args.getPlayingId())

				pushArgs.setTimeout(5);

				return push(ServerDaoImpl.getInstance().queryServer((int) bindServerId), pushArgs, Collections.emptyList());
			}
			catch (Exception e) {
				LogUtil.e("competition|playingBatchRandomJoinRoom|" + pushArgs.getParams().get("type") + "|error|" + e + "|" + pushArgs, e);
			}
		}
		catch (Exception e) {
			LogUtil.e("competition|push|matchPushBatchInnerRoom error : ", e);
		}

		return null;
	}

	/**
	 *@description 玩家结算界面等待其他牌桌结束后推送, 玩家批量合并推送
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/4
	 */
	public String showClearingInfo(CompetitionPushModel args) {
		try {
			if (CollectionUtils.isEmpty(args.getUserIds())) {
				return null;
			}

			return clearingModelPush(SHOW_CLEARING_INFO_ACTION_NAME, args);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 *@description 玩家结算界面等待其他牌桌结束后推送, 玩家批量合并推送
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/4
	 */
	public String clearInvalidRoom(CompetitionPushModel args, boolean force, int curStep, int curRound) {
		try {
			if (CollectionUtils.isEmpty(args.getUserIds())) {
				return null;
			}

			return clearInvalidRoomlPush(CLEARING_INVALID_ROOM, args, force, curStep, curRound);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 *@description 结算模型统一推送
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/8
	 */
	public String clearingModelPush(String type, CompetitionPushModel args) {
		CompetitionCommonPushModel pushArgs = null;
		try {
			//默认一批比赛中的玩家一定是一个服务器
			pushArgs = new CompetitionCommonPushModel()
					.addParam("type", type)
					.addParam("user", new ArrayList<>(args.getUserIds()))
					.addParam("bindServerId", args.getBindServerId())
					.addParam("clearingModel", JSONObject.toJSONString(args.getClearModelRes()))
					.notFilterOffline(true)
					.offlineCancelPlaying(args.getPlayingId());

			String res = pushToGameServer(pushArgs);

			LogUtil.i("competition|push|" + type + "|" + Optional.ofNullable(args.getUserIds()).orElse(Collections.emptyList()).size() + "|" + pushArgs);

			return res;
		}
		catch (Exception e) {
			LogUtil.e("competition|push|" + type + "|error|" + e + "|" + pushArgs, e);
		}
		return null;
	}

	/**
	 *@description 结算模型统一推送
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/8
	 */
	public String clearInvalidRoomlPush(String type, CompetitionPushModel args, boolean force, int curStep, int curRound) {
		CompetitionCommonPushModel pushArgs = null;
		try {
			//默认一批比赛中的玩家一定是一个服务器
			pushArgs = new CompetitionCommonPushModel()
					.addParam("type", type)
					.addParam("bindServerId", args.getBindServerId())
					.addParam("user", new ArrayList<>(args.getUserIds()))
					.addParam("playingId", args.getPlayingId())
					.notFilterOffline(true)
					.offlineCancelPlaying(args.getPlayingId());
			if (force) {
				pushArgs.addParam("force", true);
			}
			if (curStep != 0 && curRound != 0) {
				pushArgs.addParam("curStep", curStep);
				pushArgs.addParam("curRound", curRound);
			}

			String res = pushToGameServer(pushArgs);

			LogUtil.i("competition|push|" + type + "|" + Optional.ofNullable(args.getUserIds()).orElse(Collections.emptyList()).size() + "|" + pushArgs);

			return res;
		}
		catch (Exception e) {
			LogUtil.e("competition|push|" + type + "|error|" + e + "|" + pushArgs, e);
		}
		return null;
	}

	/**
	 *@description 开赛推送
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/8
	 */
	public String beginPlayingPush(CompetitionPushModel args, CompetitionPlayingDB playing) {
		String type = BEGIN_PLAYING_PUSH;
		CompetitionCommonPushModel pushArgs = null;
		try {
			//默认一批比赛中的玩家一定是一个服务器
			pushArgs = new CompetitionCommonPushModel()
					.addParam("type", type)
					.addParam("bindServerId", args.getBindServerId())
					.addParam("user", new ArrayList<>(args.getUserIds()))
					.addParam("playing", playing)
					.notFilterOffline(false)
					.offlineCancelPlaying(args.getPlayingId());
			pushArgs.setOfflineCancelPlaying(true);

			String res = pushToGameServer(pushArgs);

			LogUtil.i("competition|push|BEGIN_PLAYING_PUSH|" + Optional.ofNullable(args.getUserIds()).orElse(Collections.emptyList()).size() + "|" + pushArgs);

			return res;
		}
		catch (Exception e) {
			LogUtil.e("competition|push|" + type + "|error|" + e + "|" + pushArgs, e);
		}
		return null;
	}

	/**
	 *@description 开赛通知
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/8
	 */
	public String beginPlayingNotifyPush(List<Long> users, List<String> content) {
		return playingNotifyPush(BEGIN_PLAYING_NOTIFY, users, content);
	}

	/**
	 *@description 退赛通知
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/8
	 */
	public String cancelPlayingNotifyPush(List<Long> users, List<String> content) {
		return playingNotifyPush(CANCEL_PLAYING_NOTIFY, users, content);
	}

	/**
	 *@description 结束比赛通知
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/8
	 */
	public String endPlayingNotifyPush(List<Long> users, List<String> content) {
		return playingNotifyPush(END_PLAYING_NOTIFY, users, content);
	}

	/**
	 *@description 开赛/退赛/结赛通知
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/8
	 */
	public String playingNotifyPush(String type, List<Long> users, List<String> content) {
		if (CollectionUtils.isEmpty(users)) {
			return null;
		}
		CompetitionCommonPushModel pushArgs = null;
		try {
			//默认一批比赛中的玩家一定是一个服务器
			pushArgs = new CompetitionCommonPushModel()
					.addParam("type", type)
					.addParam("user", users)
					.addParam("args", JSONArray.toJSONString(content))
					.notFilterOffline(false);
			String res = pushToGameServer(pushArgs);

			LogUtil.i("competition|push|" + type + "|" + Optional.ofNullable(users).orElse(Collections.emptyList()).size() + "|" + pushArgs);

			return res;
		}
		catch (Exception e) {
			LogUtil.e("competitionPushService|" + type + "|error|" + e + "|" + pushArgs, e);
		}
		return null;
	}

	/**
	 *@description 通用推送
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/8
	 */
	public String cttConNotify(int code, List<Long> users, List<String> content) {
		if (CollectionUtils.isEmpty(users)) {
			return null;
		}
		CompetitionCommonPushModel pushArgs = null;
		try {
			//默认一批比赛中的玩家一定是一个服务器
			pushArgs = new CompetitionCommonPushModel()
					.addParam("type", "cttConNotify")
					.addParam("user", users)
					.addParam("args", JSONArray.toJSONString(content))
					.addParam("code", code)
					.notFilterOffline(false);
			String res = pushToGameServer(pushArgs);

			LogUtil.i("competition|push|cttConNotify|"+code+"|" + Optional.ofNullable(users).orElse(Collections.emptyList()).size() + "|" + pushArgs);

			return res;
		}
		catch (Exception e) {
			LogUtil.e("competitionPushService|cttConNotify|"+code+"|error|" + e + "|" + pushArgs, e);
		}
		return null;
	}

	/**
	 *@description 开赛/退赛/结赛通知
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/8
	 */
	public String playingRankPush(List<Long> users, Map<Integer, String> ranks) {
		if (CollectionUtils.isEmpty(users)) {
			return null;
		}
		String type = PLAYING_RANK_PUSH;
		CompetitionCommonPushModel pushArgs = null;
		try {
			//默认一批比赛中的玩家一定是一个服务器
			pushArgs = new CompetitionCommonPushModel()
					.addParam("type", type)
					.addParam("user", users)
					.addParam("arg", JSONObject.toJSONString(ranks))
					.notFilterOffline(false);
			String res = pushToGameServer(pushArgs);

			LogUtil.i("competition|push|" + type + "|" + Optional.ofNullable(users).orElse(Collections.emptyList()).size() + "|" + pushArgs);

			return res;
		} catch (Exception e) {
			LogUtil.e("competitionPushService|" + type + "|error|" + e + "|" + pushArgs, e);
		}
		return null;
	}

	/**
	 *@description 扣除资源
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/8
	 */
	public String changeGoldPush(List<Long> users, int playType, long consumerVal, int sourceType) {
		if (CollectionUtils.isEmpty(users)) {
			return null;
		}
		CompetitionCommonPushModel pushArgs = null;
		String type = NOTIFY_AND_CHANGE_GOLDS;
		try {
			//默认一批比赛中的玩家一定是一个服务器
			pushArgs = new CompetitionCommonPushModel()
					.addParam("type", type)
					.addParam("user", users)
					.addParam("freeNum", 0)
					.addParam("num", consumerVal)
					.addParam("playType", playType)
					.addParam("sourceType", sourceType)
					.notFilterOffline(true);
			String res = pushToGameServer(pushArgs);

			LogUtil.i("competition|push|" + type + "|" + sourceType + "|" + Optional.ofNullable(users).orElse(Collections.emptyList()).size() + "|" + pushArgs);

			return res.substring(1);
		}
		catch (Exception e) {
			LogUtil.e("competitionPushService|" + type + "|error|" + e + "|" + pushArgs, e);
		}
		return null;
	}


	/**
	 *@description 获取一个房间和服务器
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/8
	 */
	public HashMap<String, String> getServerAndCompetitionRoomId(Long users, int playType, long playingId, long roomConfigId, long serverId) {
		if (users == null) {
			return null;
		}
		HashMap<String, String> resMap = new HashMap<>();
		CompetitionCommonPushModel pushArgs = null;
		String type = GET_SERVER_AND_COMPETITION_ROOM_ID;
		try {
			//默认一批比赛中的玩家一定是一个服务器
			pushArgs = new CompetitionCommonPushModel()
					.addParam("type", type)
					.addParam("bindServerId", serverId)
					.addParam("user", Arrays.asList(users))
					.addParam("playingId", playingId)
					.addParam("playType", playType)
					.addParam("roomConfigId", roomConfigId)
					.notFilterOffline(true);
			String res = pushToGameServer(pushArgs);

			LogUtil.i("competition|push|" + type + "|" + Optional.ofNullable(users).orElse(0l) + "|" + pushArgs + "|" + res);

//			Map<String, Object> result = new HashMap<>();
//			Map<String, Object> serverMap = new HashMap<>();
//			serverMap.put("serverId", server.getId());
//			serverMap.put("connectHost", server.getChathost());
//			result.put("server", serverMap);
//			if (room != null) {
//				result.put("playingRoomId", room.getKeyId());
//			} else {
//				result.put("playingRoomId", 0);
//			}
//			result.put("code", 0);

			if (!StringUtils.isBlank(res)) {
				HashMap hashMap = JSONObject.parseObject(res.substring(1), HashMap.class);
				resMap.put("playingRoomId", String.valueOf(hashMap.get("playingRoomId")));
				resMap.put("serverId", String.valueOf(((JSONObject) hashMap.get("server")).get("serverId")));
			}
			return resMap;
		}
		catch (Exception e) {
			LogUtil.e("competitionPushService|" + type + "|error|" + e + "|" + pushArgs, e);
		}
		return null;
	}

	/**
	 *@description GameServer推送消息, 以单个活动做批量推送
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/5/21
	 */
	public String pushToGameServer(CompetitionCommonPushModel args) throws Exception {
		//区服ID,区服,用户
		BigMap<Integer, ServerConfig, List<RegInfo>> serverUserMapper = new BigMap<Integer, ServerConfig, List<RegInfo>>();

		//机器人直接分到赛事绑定服
		Long bindServerId = args.getParams().containsKey("bindServerId") && args.getParams().get("bindServerId") != null ? (Long) (args.getParams().get("bindServerId")) : null;

		if (bindServerId == null || bindServerId == 0) {
			args.getParams().remove("bindServerId");
		}else{
			serverUserMapper.put(bindServerId.intValue(),
					ServerDaoImpl.getInstance().queryServer(bindServerId.intValue()),
					new ArrayList<>())
					;
		}

		boolean isChangeServer = args.getParams().get("type").equals(CHANGE_SERVER_ACTION_NAME);
		boolean isMatch = args.getParams().get("type").equals(BATCH_MATCH_ACTION_NAME);
		//切换指定服
//		boolean isAssignServer = args.getUserIdServerId().size() > 0;

		//用户列表
		if (!CollectionUtils.isEmpty((List<Long>) args.getParams().get("user"))) {
			//批量分类,玩家在线推送,不在线通知
			List<RegInfo> users = userDao.getUserForList((List<Long>) args.getParams().get("user"), true);

			Iterator<RegInfo> iterator = users.iterator();
			while (iterator.hasNext()) {
				RegInfo user = iterator.next();
				//过滤掉没有登录的玩家
				if (user == null || user.getEnterServer() <= 0) continue;
				//有ID没有实际配置server,该服玩家全部过滤
				if (serverUserMapper.containsKey(user.getEnterServer()) && serverUserMapper.get(user.getEnterServer()).getE() == null)
					continue;
				//离线不做处理
				if (user.getIsOnLine() == 0 && !args.isNotFilterOffline()) {
//					//离线不在线的,直接做退赛处理
//					if (args.isOfflineCancelPlaying()) {
//						sysCancelSign(args, user);
//					}
					continue;
				}

				//切服,玩家登入gameServer与活动绑定gameServer一致,不做切换
				if (isChangeServer && user.getEnterServer() == Optional.ofNullable(bindServerId).orElse(0l)) {
					continue;
				}

				//匹配, 直接推送到绑定服
				if(bindServerId != null && isMatch) {
					//这里已经验证过server,不做重复校验
					if (serverUserMapper.containsKey(bindServerId.intValue())) {
						serverUserMapper.get(bindServerId.intValue()).getV().add(user);
					} else {    //验证server是否存在
						serverUserMapper.put(bindServerId.intValue(),
								ServerDaoImpl.getInstance().queryServer(bindServerId.intValue()),
								new ArrayList<RegInfo>() {{
									this.add(user);
								}});
					}
				}else {
					//这里已经验证过server,不做重复校验
					if (serverUserMapper.containsKey(user.getEnterServer())) {
						serverUserMapper.get(user.getEnterServer()).getV().add(user);
					} else {    //验证server是否存在
						serverUserMapper.put(user.getEnterServer(),
								ServerDaoImpl.getInstance().queryServer(user.getEnterServer()),
								new ArrayList<RegInfo>() {{
									this.add(user);
								}});
					}
				}
			}

			//所属服所有玩家都不带, 可能仅存在机器人了, 这里直接一波推送到绑定服
			if (serverUserMapper.size() == 0 && bindServerId != null) {
				serverUserMapper.put(bindServerId.intValue(),
						ServerDaoImpl.getInstance().queryServer(bindServerId.intValue()),
						Collections.emptyList());
			}

			StringBuilder resBuild = new StringBuilder();
			//批量发送消息
			serverUserMapper.iterable().forEachRemaining(v -> resBuild.append("|" + push(v.getE(), args, v.getV())));

			LogUtil.i("competition|push|pushToGameServer|resBuild:"+args+"|"+resBuild);
			return resBuild.toString();
		}

		return "success";
	}

	private void sysCancelSign(CompetitionCommonPushModel args, RegInfo user) {
		//异步退赛
		competitionJobSchedulerThreadPool.nSynBus(() -> {
			try {
				//查询自己的榜单

				CompetitionScoreDetailDB rankForSelf = competitionRankService.getDetailRankForSelf(CompetitionScoreDetailDB.builder().playingId(args.getPlayingId()).userId(user.getUserId()).build());
				//没有参与过一轮的玩家给予退赛
				if (rankForSelf.getLastStep() == 0) {
					competitionApplyService.cancel(CompetitionApplyParam.builder()
							.playingId(args.getPlayingId())
							.userId(user.getUserId())
							.build(), true);
				}
			}
			catch (Exception e) {
				LogUtil.e("competition|push|cancel|error|" + e.getMessage());
			}
		});
	}

	/**
	 *@description 单个推送
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/5/25
	 */
	public String push(ServerConfig serverConfig, CompetitionCommonPushModel params, List<RegInfo> regInfos) {
		if (serverConfig == null || params == null/* || CollectionUtils.isEmpty(regInfos)*/) {
			return null;
		}
		try {
			return push(serverConfig, paramsToStr(params, regInfos), params.getTimeout());
		} catch (Exception e) {
			throw e;
		}
	}

	public String push(ServerConfig serverConfig, String params, long timeout) {
		String url = serverConfig.getIntranet();
		if (StringUtils.isBlank(url)) {
			url = serverConfig.getHost();
		}

		if (StringUtils.isNotBlank(url)) {
			int idx = url.indexOf(".");
			if (idx > 0) {
				idx = url.indexOf("/", idx);
				if (idx > 0) {
					url = url.substring(0, idx);
				}
				url += "/online/notice.do?" + params;
				long t = System.currentTimeMillis();
				String noticeRet = HttpUtil.getUrlReturnValue(url, "utf-8", "POST", (int) (timeout == 0 ? 30 : timeout));
				LogUtil.i("competition|push|params|" + url + "|return|" + noticeRet + " " + (System.currentTimeMillis() - t) + " ms");

				return noticeRet;
			}
		}
		return null;
	}

	/**
	 *@description 批量处理
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/5/25
	 */
	public String paramsToStr(CompetitionCommonPushModel params, List<RegInfo> regInfos) {
		StringBuilder httpParams = new StringBuilder();
		String message = "";
		String userIdStr = JSONArray.toJSONString(regInfos.stream().map(v -> v.getUserId()).collect(Collectors.toList()));

		String timestamp = String.valueOf(System.currentTimeMillis());

		//签名
		httpParams.append("sign=");
		httpParams.append(MD5Util.getStringMD5(APP_KEY + params.getParams().get("type") + userIdStr + message + timestamp));
		//消息
		httpParams.append("&message=");
		httpParams.append(message);
		//时间戳
		httpParams.append("&timestamp=");
		httpParams.append(timestamp);
		httpParams.append("&");
		//用户参数
		httpParams.append(params.toParam());
		return httpParams.toString();
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class CompetitionCommonPushModel {
		private long timeout;//秒

		//离线的给退赛处理
		private boolean offlineCancelPlaying;

		//退赛参数
		private long playingId;

		//false仅仅给在线玩家发消息
		private boolean notFilterOffline;

		//额外参数
		private HashMap<String, Object> params = new HashMap<>();

		private HashMap<Long, Long> userIdServerId = new HashMap<>();

		public void addUser(Long userId) {
			params.merge("user", new ArrayList<Long>() {{
				this.add(userId);
			}}, (oV, nV) -> {
				((List<Long>) oV).addAll((List<Long>) nV);
				return oV;
			});
		}

		public CompetitionCommonPushModel notFilterOffline(boolean notFilterOffline) {
			this.notFilterOffline = notFilterOffline;
			return this;
		}

		public CompetitionCommonPushModel offlineCancelPlaying(Long playingId) {
			if (playingId != null && playingId > 0) {
				this.playingId = playingId;
			}
			return this;
		}

		public CompetitionCommonPushModel addParam(String paramName, Object paramVal) {
			params.put(paramName, paramVal);
			return this;
		}

		public String toParam() {
			StringBuilder val = new StringBuilder();
			Iterator<Entry<String, Object>> iterator = params.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, Object> next = iterator.next();
				val.append("&");
				val.append(next.getKey());
				val.append("=");
				try {
					/**
					 * +    URL 中+号表示空格                                 %2B
					 * 空格 URL中的空格可以用+号或者编码           %20
					 * /   分隔目录和子目录                                     %2F
					 * ?    分隔实际的URL和参数                             %3F
					 * %    指定特殊字符                                          %25
					 * #    表示书签                                                  %23
					 * &    URL 中指定的参数间的分隔符                  %26
					 * =    URL 中指定参数的值                                %3D
					 * */
					if (!isBaseType(next.getValue().getClass(), true)/*next.getValue().getClass().isAssignableFrom(ArrayList.class)*/) {
						if(next.getValue().getClass().isAssignableFrom(ArrayList.class)){
							val.append(JSONArray.toJSONString(next.getValue()));
						}else{
							val.append(JSONObject.toJSONString(next.getValue()));
						}
					}
					else {
						val.append(String.valueOf(next.getValue())
								.replace("%", "%23")
								.replace("+", "%2B")
								.replace(" ", "%20")
								.replace("/", "%2F")
								.replace("?", "%3F")
								.replace("&", "%26")
								.replace("=", "%3D")
						);
//						val.append(next.getValue());
					}
				}
				catch (Exception e) {
					LogUtil.e("competition|pushModel|toParam|" + next.getKey() + "|" + e.getMessage());
					throw e;
				}
			}

			return val.toString().trim().substring(1);
		}
	}

	public static boolean isBaseType(Class className, boolean incString) {
		if (incString && className.equals(String.class)) {
			return true;
		}
		return className.equals(Integer.class) ||
				className.equals(int.class) ||
				className.equals(Byte.class) ||
				className.equals(byte.class) ||
				className.equals(Long.class) ||
				className.equals(long.class) ||
				className.equals(Double.class) ||
				className.equals(double.class) ||
				className.equals(Float.class) ||
				className.equals(float.class) ||
				className.equals(Character.class) ||
				className.equals(char.class) ||
				className.equals(Short.class) ||
				className.equals(short.class) ||
				className.equals(Boolean.class) ||
				className.equals(boolean.class);
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class CompetitionPushModel {
		private List<Long> userIds; 	//用户IDS
		private Long playingId;			//赛场ID
		private Long roomConfigId; 		//房间配置ID
		private Long bindServerId; 		//房间配置ID

		private CompetitionClearingModelRes clearModelRes;
	}

}


