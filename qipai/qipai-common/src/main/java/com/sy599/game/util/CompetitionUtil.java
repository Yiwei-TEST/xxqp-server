package com.sy599.game.util;

import com.alibaba.fastjson.JSONObject;
import com.sy599.game.GameServerConfig;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.executor.TaskExecutor;
import com.sy599.game.db.bean.Server;
import com.sy599.game.db.bean.competition.CompetitionPlaying;
import com.sy599.game.db.bean.competition.CompetitionRoom;
import com.sy599.game.db.bean.competition.CompetitionRoomConfig;
import com.sy599.game.db.bean.competition.CompetitionRoomUser;
import com.sy599.game.db.bean.competition.param.CompetitionClearingModelReq;
import com.sy599.game.db.bean.competition.param.CompetitionClearingModelRes;
import com.sy599.game.db.bean.competition.param.CompetitionClearingPlay;
import com.sy599.game.db.dao.CompetitionDao;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.msg.serverPacket.ComMsg.ComRes;
import com.sy599.game.websocket.MyWebSocket;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;

public final class CompetitionUtil {
	/**结算推送重试次数*/
	private static final Integer CLEARING_GET_TIME_OUT_RETRY = 3;

	/**次局自动准备超时时间*/
	public static final Integer NO_FIRST_AUTO_READY_TIME_OUT = 3000;

	private static final List<CompetitionBigStageTask> bigStageTasks = Arrays.asList(CompetitionBigStageMission.builder().build());

	public static final Map<Long, CompetitionRoomConfig> ALL_MAP = new HashMap<>();

	public static final Map<Integer, List<CompetitionRoomConfig>> TYPE_MAP = new HashMap<>();

	public static void init() {
		initCompetitionRoomConfig();
	}


	public static void initCompetitionRoomConfig() {
		try {
			List<CompetitionRoomConfig> all = CompetitionDao.getInstance().loadAllRoomConfig();
			if (all == null || all.size() == 0) {
				return;
			}
			Set<Integer> cleared = new HashSet<>();
			for (CompetitionRoomConfig config : all) {
				ALL_MAP.put(config.getKeyId(), config);
				List<CompetitionRoomConfig> list = TYPE_MAP.get(config.getSoloType());
				if (list == null) {
					list = new ArrayList<>();
					TYPE_MAP.put(config.getSoloType(), list);
				}
				else {
					if (!cleared.contains(config.getSoloType())) {
						list.clear();
						cleared.add(config.getSoloType());
					}
				}
				list.add(config);
			}

			LogUtil.msgLog.info("competitionUtil|initCompetitionRoomConfig|" + ALL_MAP.size());
		}
		catch (Exception e) {
			LogUtil.errorLog.error("CompetitionUtil|initCompetitionRoomConfig|error" + e.getMessage(), e);
		}
	}

	public static List<CompetitionRoomConfig> getCompetitionRoomConfigList(int soloType) {
		return TYPE_MAP.get(soloType);
	}


	public static CompetitionRoomConfig getCompetitionRoomConfig(long keyId) {
		return getCompetitionRoomConfig(keyId, true);
	}


	public static CompetitionRoomConfig getCompetitionRoomConfig(long keyId, boolean isValid) {
		CompetitionRoomConfig config = ALL_MAP.get(keyId);
		if (config == null) {
			return null;
		}
		if (isValid && !config.isValid()) {
			return null;
		}
		return config;
	}

	/**
	 *@description 赛事结算界面展示
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/6
	 */
	public static void showClearingInfo(CompetitionClearingModelRes clearingResModel) {
		LogUtil.msgLog.info("全部结算:" + clearingResModel.getPlayingId() + "|" + clearingResModel.getCurStep() + "|" + clearingResModel.getCurRound() + "|" + clearingResModel);

		LogUtil.msgLog.info("CompetitionUtil showClearingInfo player {}", clearingResModel);

		if (clearingResModel == null) {
			LogUtil.msgLog.error("CompetitionUtil showClearingInfo player return param is null{}", clearingResModel);
			return;
		}
		// 下发当前的轮次标题,回合数,牌桌底分,淘汰分数,玩家当前排名,剩余总人数
		// 通知客户端等待其他玩家完成比赛
		Iterator<CompetitionClearingPlay> iterator = clearingResModel.getPlays().iterator();
		while (iterator.hasNext()) {
			CompetitionClearingPlay next = iterator.next();
			Player player = PlayerManager.getInstance().getPlayer(next.getUserId());
			if (player == null) {
				LogUtil.msgLog.info("CompetitionUtil showClearingInfo player offline pid:{}, res:{}", next.getUserId(), clearingResModel);
				continue;
			}
			try {
				showClearingInfo(clearingResModel, next, player.getMyWebSocket());
			}
			catch (Exception e) {
				LogUtil.msgLog.info("CompetitionUtil showClearingInfo error {}", e);
			}
		}
	}

	/**
	 *@description
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/7/28
	 */
	public static void showClearingInfo(CompetitionClearingModelRes clearingResModel , CompetitionClearingPlay next, MyWebSocket socket) {
		if (socket == null || clearingResModel == null) return;
		socket.send(ComRes.newBuilder().setCode(WebSocketMsgType.competition_msg_clearing)
					//赛事结果: 1等待其他玩家完成比赛
					.addParams(clearingResModel.getResult())
					//是否晋级: 1晋级
					.addParams(next.getIsOver())
					//当前排名
					.addParams(next.getRank())
					//总人数
					.addParams(clearingResModel.getCurStepTotalHuman())
					//奖励
					.addParams(next.getAwardId())
					.addParams(next.getAwardVal())
					//当前轮次
					.addParams(next.isNextStep() ? 1 : 0)
					//标题
					.addStrParams(clearingResModel.getPlayingTitle())
					//大回合晋级人数
					.addStrParams(JSONObject.toJSONString(clearingResModel.getStepUpgradeDetails()))
					.build());
	}

	/**
	 *@description 仅更新排名
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/7/8
	 */
	public static void pushOnlyRefreshRankMsg(List<Player> seatMap, Map<Long, CompetitionRoomUser> roomPlayers, CompetitionRoom room) {
		try {
			Iterator<Player> iterator = seatMap.iterator();
			while (iterator.hasNext()) {
				Player next = iterator.next();
				roomPlayers.get(Long.valueOf(next.getUserId())).setClearingScore(next.getTotalPoint());
			}
			//玩家
			Collection<CompetitionRoomUser> players = roomPlayers.values();
			//
			CompetitionClearingModelRes createModelRes = room.getCompetitionClearingModelRes();
			List<CompetitionClearingPlay> users = new ArrayList<>();
			//每桌分数最高的为赢家
			players = players.stream().filter(v -> v.getClearingScore() != null).sorted(Comparator.comparing(CompetitionRoomUser::getClearingScore).reversed()).collect(Collectors.toList());
			//桌内排名
			sortPlay(players, users);

			String arg = toJson(
					CompetitionClearingModelReq.builder()
							.playingId(room.getPlayingId())
							.curRound(createModelRes.getCurRound())
							.curStep(createModelRes.getCurStep())
							.users(users)
							.competitionRoomKeyId(room.getKeyId())
							.curTableCount(-1)
							.build());
			long timestamp = System.currentTimeMillis();

			String sign = checkSign(new HashMap<String, String>() {
				{
					this.put("arg", arg);
					this.put("timestamp", "" + timestamp);
				}
			});

			int timeOut = 10 * 1000 * CLEARING_GET_TIME_OUT_RETRY;

			String url =  "arg=" + arg + "&sign=" + sign + "&timestamp=" + timestamp;
			new HttpUtils(createModelRes.getLoginCallBackUrl() + "/" + createModelRes.getLoginCallBackOnlyRefreshRank()).post(url, timeOut, timeOut);
			LogUtil.msg("CompetitionRoom|pushOnlyRefreshRankMsg|" + createModelRes.getPlayingId() + "|" + createModelRes.getCurStep() + "|" + createModelRes.getCurRound() + "|" + room.getKeyId() + "|users|" + users + "|players|" + players);
		} catch (Exception e) {
		}
	}

	private static void sortPlay(Collection<CompetitionRoomUser> players, List<CompetitionClearingPlay> users) {
		int rank = 0;
		Iterator<CompetitionRoomUser> iterator = players.iterator();
		while (iterator.hasNext()) {
			CompetitionRoomUser next = iterator.next();
			users.add(CompetitionClearingPlay.builder()
					.userId(next.getUserId())
					.score(next.getClearingScoreNotNull())
					.tableRank(++rank)
					.build());
		}
	}

	/**
	 *@description 结算推送
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/4
	 */
	public static CompetitionClearingModelRes pushClearingMsg(List<Long> winnerUserIds, Collection<CompetitionRoomUser> players, CompetitionRoom room) {
		LogUtil.msg("CompetitionRoom|pushClearing|" + room.getCompetitionClearingModelRes().getPlayingId() + "|" + room.getCompetitionClearingModelRes().getCurStep() + "|" + room.getCompetitionClearingModelRes().getCurRound() + "|" + room.getKeyId());

		if(CollectionUtils.isEmpty(players)){
			return null;
		}

		int retry = CLEARING_GET_TIME_OUT_RETRY;

		try {
			CompetitionClearingModelRes createModelRes = room.getCompetitionClearingModelRes();

			boolean normalTable = players.stream().noneMatch(v -> v.getClearingScore() == null);
			//每桌分数最高的为赢家
			if(normalTable){
				players = players.stream().filter(v -> v.getClearingScore() != null).sorted(Comparator.comparing(CompetitionRoomUser::getClearingScore).reversed()).collect(Collectors.toList());
			} else {
				players = players.stream().sorted(Comparator.comparing(CompetitionRoomUser::getInitScore).reversed()).collect(Collectors.toList());
			}

			List<CompetitionClearingPlay> users = new ArrayList<>();
			//桌内排名
			sortPlay(players, users);

			LogUtil.msg("CompetitionRoom|pushClearing|" + createModelRes.getPlayingId() + "|" + createModelRes.getCurStep() + "|" + createModelRes.getCurRound() + "|" + room.getKeyId() + "|users|" + users + "|players|" + players);

			//减少现有房间数
			long curTableCount = -1;

			if (normalTable && room != null && players.size() == room.getMaxCount() && room.getCurrentState() == CompetitionRoom.STATE_NORMAL_OVER) {
				curTableCount = CompetitionDao.getInstance().addCompetitionCurPlayingTableCount(room.getPlayingId(), -1);
				LogUtil.msg("CompetitionRoom|pushClearing|curTableCount|" + room.getCompetitionClearingModelRes().getPlayingId() + "|" + room.getCompetitionClearingModelRes().getCurStep() + "|" + room.getCompetitionClearingModelRes().getCurRound() + "|" + room.getKeyId() + "|" + curTableCount);
			}

			CompetitionClearingModelRes competitionClearingModelRes = null;

			String arg = toJson(
					CompetitionClearingModelReq.builder()
							.playingId(room.getPlayingId())
							.curRound(createModelRes.getCurRound())
							.curStep(createModelRes.getCurStep())
							.users(users)
							.competitionRoomKeyId(room.getKeyId())
							.curTableCount(curTableCount)
							.currentMills(createModelRes.getCurrentMills())
							//结算时间点,以loginServer时间为基准,这里给出差值
							.currentClearingMills(createModelRes.getCurrentMills() + (new Date().getTime() - room.getCreatedTime().getTime()))
							.build()
			);

			long timestamp = System.currentTimeMillis();
			String sign = checkSign(new HashMap<String, String>() {
				{
					this.put("arg", arg);
					this.put("timestamp", "" + timestamp);
				}
			});

			String url =  "timestamp=" + timestamp + "&sign=" + sign + "&arg=" + arg;
			HttpUtils httpUtils = new HttpUtils(createModelRes.getLoginCallBackUrl() + "/" + createModelRes.getLoginCallBackClearing());

			int timeOut = 10 * 1000 * CLEARING_GET_TIME_OUT_RETRY;
			//请求重试
			for (; retry > 0; retry--) {
				try {
					long t = System.currentTimeMillis();
					String s = httpUtils.post(url, timeOut, timeOut);
					//这里loginServer正常情况下一定会返回,重启loginServer不能强行kill-9,loginServer依次关闭启动后,再关闭启动
					//返回为null或者状态码-1时,业务逻辑出错了,不做处理了
					//返回使用域名,不要使用ip,动态切换,
					competitionClearingModelRes = toCompetitionClearingModelRes(s);
					//结算出现意外
					if (competitionClearingModelRes == null) {
						break;
					}
					LogUtil.msg("CompetitionRoom|pushClearing|" + createModelRes.getPlayingId() + "|" + createModelRes.getCurStep() + "|" + createModelRes.getCurRound() + "|" + room.getKeyId() + "|" + retry + "|" + (System.currentTimeMillis() - t) + "ms" + "|" + url + "|" + competitionClearingModelRes + "|" + s);
				}
				catch (java.net.ConnectException e1) {
					Thread.sleep(10 * 1000);
				}
				catch (Exception e) {
					LogUtil.e("CompetitionRoom|pushClearing|error" + createModelRes.getPlayingId() + "|" + createModelRes.getCurStep() + "|" + createModelRes.getCurRound() + "|" + room.getKeyId() + "|" + retry + " url:" + url + "  ", e);
					Thread.sleep(10 * 1000);
				}

				if (competitionClearingModelRes != null) {
					break;
				}
			}

			return competitionClearingModelRes;
		}
		catch (Exception e) {
			LogUtil.e("CompetitionUtil pushClearingMsg error players:" + players + "room:" + room, e);
		}

		return null;
	}

	public static String checkSign(Map<String, String> map) {
		String[] objs = map.keySet().toArray(new String[0]);
		Arrays.sort(objs);

		StringBuilder stringBuilder = new StringBuilder();
		for (String obj : objs) {
			stringBuilder.append("&").append(obj).append("=").append(map.get(obj));
		}
		stringBuilder.append("&key=").append("043a528a8fc7195876fc4d4c3eaa7d2e");

		try {
			return MD5Util.getMD5String(String.valueOf(stringBuilder).getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 *@description 退赛
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/4
	 */
	public static void pushCancelMsg(Long playingId, Long userId, String callBackUrl, String actionName) {
		try {
			long timestamp = System.currentTimeMillis();

			String sign = checkSign(new HashMap<String, String>() {
				{
					this.put("playingId", ""+playingId);
					this.put("userId", ""+userId);
					this.put("timestamp", "" + timestamp);
				}
			});

			new HttpUtils(callBackUrl + "/" + actionName).post("playingId=" + playingId + "&userId=" + userId + "&sign=" + sign + "&timestamp=" + timestamp);
		}
		catch (Exception e) {
		}
	}

	/**
	 *@description 回到报名界面
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/4
	 */
	public static void pushBackSign(Player player, MyWebSocket socket) {
		try {
			if (true) {
				return;
			}
			if(player == null){
				return;
			}
			long userId = player.getUserId();
			long timestamp = System.currentTimeMillis();

			String sign = checkSign(new HashMap<String, String>() {
				{
					this.put("userId", "" + userId);
					this.put("timestamp", "" + timestamp);
				}
			});

			String arg = new HttpUtils(GameServerConfig.LOGIN_SERVER_ADDRESS + "/" + GameServerConfig.PLAYING_BACK_SIGN_ADDRESS).post("userId=" + userId + "&sign=" + sign + "&timestamp=" + timestamp);

			if(!StringUtil.isBlank(arg)){
				JSONObject jsonObject = JSONObject.parseObject(arg);
				if(jsonObject.getIntValue("code") > 0){
					socket.sendComMessage(WebSocketMsgType.competition_msg_back_sign, jsonObject.getString("message"));
				}
			}
		} catch (Exception e) {
		}
	}

	/**
	 *@description 等待晋级界面
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/4
	 */
	public static void pushPlayCheck(Player player, MyWebSocket socket) {
		try {
			if (true) {
				return;
			}
			if(player == null){
				return;
			}

			long userId = player.getUserId();
			long timestamp = System.currentTimeMillis();

			String sign = checkSign(new HashMap<String, String>() {
				{
					this.put("userId", "" + userId);
					this.put("timestamp", "" + timestamp);
				}
			});

			String arg = new HttpUtils(GameServerConfig.LOGIN_SERVER_ADDRESS + "/" + GameServerConfig.PLAYING_PLAY_CHECK_ADDRESS).post("userId=" + userId + "&sign=" + sign + "&timestamp=" + timestamp);
			CompetitionClearingModelRes competitionClearingModelRes = toCompetitionClearingModelRes(arg);

			if (competitionClearingModelRes != null) {
				int tableId = CompetitionDao.getInstance().queryPlayingTableByRoomUser(competitionClearingModelRes.getPlayingId(), player.getUserId());
				if (tableId > 0) {
					player.setPlayingTableId(tableId);
					return;
				}
			}
			showClearingInfo(competitionClearingModelRes, competitionClearingModelRes.getPlays().get(0), socket);
		} catch (Exception e) {
		}
	}

	/**
	 *@description 通知客户端切服
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/10
	 */
	public static boolean changePlayServer(Long userId, Server server) throws Exception {
		Player player = PlayerManager.getInstance().getPlayer(userId);
		if (player != null && player.getMyWebSocket() != null && player.getEnterServer() != server.getId()) {
			try {
				player.setEnterServer(server.getId());
				player.saveBaseInfo();
				Map<String, Object> result = new HashMap<>();
				Map<String, Object> serverMap = new HashMap<>();
				serverMap.put("serverId", server.getId());
				serverMap.put("connectHost", server.getHost());
				result.put("server", serverMap);
				result.put("code", 0);
				player.writeComMessage(WebSocketMsgType.res_code_getserverid, JacksonUtil.writeValueAsString(result));
				return true;
			}
			catch (Exception e) {
				throw e;
			}
		}

		return false;
	}

	public static CompetitionClearingModelRes getClearingResModel(Long userId, Long playingId) {
		CompetitionClearingModelRes res = null;
		try {
			CompetitionPlaying playing = CompetitionDao.getInstance().loadCompetitionPlayingById(playingId);
			//开赛中或匹配中状态 com.sy.sanguo.game.competition.model.enums.CompetitionPlayingStatusEnum
			if (playing.getStatus() != 2 && playing.getStatus() != 7) {
				Player player = PlayerManager.getInstance().getPlayer(playingId);
				if (player != null) player.writeErrMsg("比赛已经完结");
				return null;
			}

			if (!StringUtils.isBlank(playing.getExt()) && !"Null".equalsIgnoreCase(playing.getExt())) {
				res = JSONObject.parseObject(playing.getExt(), CompetitionClearingModelRes.class);
				res.setPlays(Arrays.asList(CompetitionClearingPlay.builder().userId(userId).score(playing.getInitScore()).build()));
			}
		}
		catch (Exception e) {
			LogUtil.e("CompetitionUtil getClearingResModel error", e);
		}

		return res;
	}

	public static void dissTable(BaseTable table, List<Long> winnerUserIds, CompetitionRoom room, Map<Long, CompetitionRoomUser> users) {
		//比赛场结算
		if (table.isCompetitionRoom() && room != null && room.getCurrentState() >= CompetitionRoom.STATE_PLAYING) {
//			Player winner = table.getSeatMap().get(table.getLastWinSeat());
			TaskExecutor.getInstance().submitTask(() -> {
				long time = System.currentTimeMillis();
				LogUtil.monitor_i("CompetitionServiceClearing....Start..." + Thread.currentThread().getId() + ": roomId:" + room.getKeyId() + " playingId:" + room.getPlayingId() + " room:" + room + " users:" + users);
				CompetitionUtil.showClearingInfo(CompetitionUtil.pushClearingMsg(winnerUserIds, users.values(), room));
				LogUtil.monitor_i("CompetitionServiceClearing....End..." + Thread.currentThread().getId() + " t:" + (System.currentTimeMillis() - time) + "ms" + " : roomId:" + room.getKeyId() + " playingId:" + room.getPlayingId());
			});
		}
	}

	/**
	 *@description
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/7/16
	 */
	public static void taskInvoke(List<Long> users) {
		TaskExecutor.getInstance().submitTask(() -> {
			bigStageTasks.stream().forEach(v -> {
				try {
					v.invoke(Collections.emptyList(),
							users.stream().map(v1 -> CompetitionRoomUser.builder().userId(v1).build()).collect(Collectors.toList()),
							null);
				} catch (Exception e) {
					LogUtil.e("CompetitionRoom|bigStageTask|error|{}", e);
				}
			});
		});
	}

	public static CompetitionClearingModelRes fillUrl(CompetitionClearingModelRes competitionClearingModelRes) {
		if (competitionClearingModelRes != null) {
			competitionClearingModelRes.setLoginCallBackUrl(GameServerConfig.LOGIN_SERVER_ADDRESS);
			competitionClearingModelRes.setLoginCallBackClearing(GameServerConfig.PLAYING_CLEARING_ADDRESS);
			competitionClearingModelRes.setLoginCallBackCancel(GameServerConfig.PLAYING_CANCEL_ADDRESS);
			competitionClearingModelRes.setLoginCallBackOnlyRefreshRank(GameServerConfig.PLAYING_ONLY_REFRESH_RANK_ADDRESS);
		}
		return competitionClearingModelRes;
	}

	public static String toJson(CompetitionClearingModelReq req) {
		return JSONObject.toJSONString(req);
	}

	public static CompetitionClearingModelRes toCompetitionClearingModelRes(String arg) {
		JSONObject jsonObject = JSONObject.parseObject(arg);
		if (jsonObject == null || jsonObject.getIntValue("code") < 0) {
			return null;
		}
		return JSONObject.parseObject(jsonObject.getString("message"), CompetitionClearingModelRes.class);
	}
}
