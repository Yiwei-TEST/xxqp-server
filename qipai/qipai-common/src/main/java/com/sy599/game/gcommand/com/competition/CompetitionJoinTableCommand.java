package com.sy599.game.gcommand.com.competition;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.GeneratedMessage;
import com.sy599.game.GameServerConfig;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.common.executor.TaskExecutor;
import com.sy599.game.db.bean.Server;
import com.sy599.game.db.bean.competition.CompetitionRoom;
import com.sy599.game.db.bean.competition.CompetitionRoomConfig;
import com.sy599.game.db.bean.competition.CompetitionRoomUser;
import com.sy599.game.db.bean.competition.param.CompetitionClearingModelRes;
import com.sy599.game.db.bean.competition.param.CompetitionClearingPlay;
import com.sy599.game.db.dao.CompetitionDao;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.manager.ServerManager;
import com.sy599.game.manager.TableManager;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.msg.serverPacket.TableRes;
import com.sy599.game.util.CompetitionUtil;
import com.sy599.game.util.LangHelp;
import com.sy599.game.util.LogUtil;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 比赛场房间处理
 * @author Guang.OuYang
 * @date 2020/6/2-17:52
 */
public class CompetitionJoinTableCommand {

	public static final Map<Long, Player> ROBOT_CACHE = new ConcurrentHashMap<>();

	/**
	 *@description 开始匹配
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/9
	 */
	public static List<Long> action(List<Long> user, CompetitionClearingModelRes competitionJoinRoomParam) {
		LogUtil.msgLog.info("Competition|joinTable|param|{}|{}|{}|{}|{}", competitionJoinRoomParam.getPlayingId(), competitionJoinRoomParam.getCurStep(), competitionJoinRoomParam.getCurRound(), user, competitionJoinRoomParam);
		CompetitionUtil.fillUrl(competitionJoinRoomParam);

		CompetitionRoomConfig roomConfig = CompetitionUtil.getCompetitionRoomConfig(competitionJoinRoomParam.getRoomConfigId());

		List<Long> res = new ArrayList<>();
		//扰乱top3进入同一桌
		List<Long> newUsers = shuffle(user, competitionJoinRoomParam, roomConfig);

		int i = newUsers.size() - 1;
		for (; i >= 0; i--) {
			try {
				Player player;
				long userId = newUsers.get(i);
				if (userId < 0) {
					player = getRobot(userId, roomConfig.getPlayType());
				} else if((player = PlayerManager.getInstance().getPlayer(userId)) == null) {
					//离线退赛
					if (competitionJoinRoomParam.isOfflinePlayCancelSign()) {
						//玩家离线处理退赛
						boolean cancel = competitionJoinRoomParam.isFirstMatch();
						if (cancel) {
							//处理退赛逻辑
							CompetitionUtil.pushCancelMsg(competitionJoinRoomParam.getPlayingId(), userId, competitionJoinRoomParam.getLoginCallBackUrl(), competitionJoinRoomParam.getLoginCallBackCancel());
						}
						LogUtil.msgLog.info("CompetitionRoom|batchInnerRoom|notFindPlayCache|" + userId + "|" + cancel + "|" + competitionJoinRoomParam.getPlayingId());
						continue;
					} else {
						//离线也加入房间挂机
						player = PlayerManager.getInstance().loadPlayer(userId, roomConfig.getPlayType());
					}
				}

				if(player == null) {
					continue;
				}

				LogUtil.msgLog.info("competition|action|singleJoin|{}|{}|{}|{}|", competitionJoinRoomParam.getPlayingId(), competitionJoinRoomParam.getCurStep(), competitionJoinRoomParam.getCurRound(), userId);

				if(CompetitionJoinTableCommand.process(player, competitionJoinRoomParam)){
					res.add(player.getUserId());
				}
			} catch (Exception e) {
				LogUtil.msgLog.error("CompetitionRoom|joinTable|error|", e);
			}
		}

		//任务相关
		if(competitionJoinRoomParam.isFirstMatch()){
			CompetitionUtil.taskInvoke(user);
		}

		return res;
	}

	/**
	 *@description 打乱前3的顺序,最大化规避前三进了同一个桌子
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/7/8
	 */
	private static List<Long> shuffle(List<Long> user, CompetitionClearingModelRes competitionJoinRoomParam, CompetitionRoomConfig roomConfig) {
		int middleCount = roomConfig.getPlayerCount();

		List<Long> newUsers = user;

		if (!competitionJoinRoomParam.isNoSplitUserOrder() && user.size() > middleCount) {
			//排序
			List<CompetitionClearingPlay> plays = new ArrayList<>(competitionJoinRoomParam.getPlays()).stream().sorted(Comparator.comparing(CompetitionClearingPlay::getScore).reversed()).collect(Collectors.toList());
			//分桌前三
			//取中间段向下取整
			int middle = Math.max(plays.size() / middleCount - 1, roomConfig.getPlayerCount()-1);
			//前三
			ArrayList<Long> topThree = new ArrayList<>(Arrays.asList(plays.remove(0).getUserId(), plays.remove(0).getUserId(), plays.remove(0).getUserId()));
			//其他玩家打乱顺序
			Collections.shuffle(plays);
			//最终排名
			newUsers = new ArrayList<>();

			int j = 0;

			Iterator<CompetitionClearingPlay> iterator = plays.iterator();
			while (iterator.hasNext()) {
				CompetitionClearingPlay next = iterator.next();

				if (++j % middle == 0 && topThree.size() > 0) {
					newUsers.add(topThree.remove(0));
				}

				newUsers.add(next.getUserId());
			}

			if(newUsers.isEmpty() || newUsers.size() != user.size()) {
				Collections.shuffle(user);

				Iterator<Long> iterator1 = user.iterator();
				while (iterator1.hasNext()) {
					Long next = iterator1.next();
					if (!newUsers.contains(next)) {
						newUsers.add(next);
					}
				}
			}
		}

		return newUsers.size() != user.size() ? user : newUsers;
	}

	/**
	 *@description 客户端申请加入房间
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/7/1
	 */
	public static void process(Player player, ComReq req) {
		LogUtil.msgLog.info("competition|process|userId:{},{}|req:{}", player.getUserId(), player.getName(), req);

		List<String> strParamsList = req.getStrParamsList();
		Long playingId = Long.valueOf(strParamsList.get(1));
		try {
			CompetitionClearingModelRes competitionClearingModelRes = CompetitionUtil.fillUrl(CompetitionUtil.getClearingResModel(player.getUserId(), playingId));
			if (competitionClearingModelRes == null) {
				LogUtil.msgLog.info("competition|process|pramsIsNull" + player.getUserId() + "|" + req);
				return;
			}
			process(player, competitionClearingModelRes);
		} catch (Exception e) {
			LogUtil.msgLog.error("competition|joinTable|process|error|" + e);
		}
	}

	/**
	 *@description 加入比赛房统一处理
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/7/1
	 */
	public static boolean process(Player player, CompetitionClearingModelRes param) throws Exception {
		if (param == null) {
			return false;
		}

//		LogUtil.printDebug("申请加入房间~~~~~~~~~~{},{},{}", player.getName(), player.getUserId(), param.getPlayingId(), param.getCurStep(), param.getCurRound());

		LogUtil.msgLog.info("competition|joinTable|process|" + param.getPlayingId() + "|" + param.getCurStep() + "|" + param.getCurRound() + "|" + player.getUserId() + "|" + param);
		//判断玩家是否有正在玩的房间
		BaseTable table = player.getPlayingTable();
		if (table != null) {
			if(table.isCompetitionRoom()){
				player.writeSocket(table.buildCreateTableRes(player.getUserId(), true, false));
				table.broadOnlineStateMsg();
			}
			return false;
		}

		//可能该房间不在当前服务器
		if (player.getPlayingTableId() > 0) {
			//不是比赛房,或者不是这一场比赛
			if ((table == null || (!table.isCompetitionRoom() || !table.getCompetitionRoom().getPlayingId().equals(param.getPlayingId()))) && param.getCurStep() == 1 && param.getCurRound() == 1) {
				CompetitionUtil.pushCancelMsg(param.getPlayingId(), player.getUserId(), param.getLoginCallBackUrl(), param.getLoginCallBackCancel());
				LogUtil.msgLog.info("competition|joinTable|process|cancel|" + param);
			}
			LogUtil.msgLog.info("competition|joinTable|process|tableIsNotNull|" + param.getPlayingId() + "|" + param.getCurStep() + "|" + param.getCurRound() + "|" + player.getUserId());
			return false;
		}

		//获取当前比赛房的详细创房参数配置
		CompetitionRoomConfig config = CompetitionJoinTableCommand.getRoomConfig(param.getRoomConfigId(), false);

		synchronized (config) {
			if (config == null || !config.isValid()) {
				player.writeErrMsg("玩法不存在或暂未开放" + param.getRoomConfigId());
				return false;
			}

			Server server = ServerManager.loadServer(GameServerConfig.SERVER_ID);
			if (!ServerManager.isValid(server, Server.SERVER_TYPE_COMPETITION_ROOM, config.getPlayType())) {
				player.writeErrMsg("玩法配置错误：请联系系统管理员");
				return false;
			}
			CompetitionRoom randomRoom = null;

//			long roomId = param.getPlayingRoomId();
//			if (roomId > 0) {
//				randomRoom = TableManager.getInstance().getCompetitionRoom(roomId);
//				if (randomRoom == null) {
//					randomRoom = CompetitionDao.getInstance().loadCompetitionRoomForceMaster(roomId);
//					if (randomRoom != null && randomRoom.getServerId() != GameServerConfig.SERVER_ID) {
//						player.writeErrMsg("当前牌桌服务器错误" + randomRoom.getServerId() + "," + GameServerConfig.SERVER_ID);
//						return;
//					}
//				}
//			}

			randomRoom = CompetitionDao.getInstance().randomCompetitionRoom(config.getKeyId(), param.getPlayingId());

			if (param.isNoCreateTable() && (randomRoom == null || randomRoom.getMaxCount() <= randomRoom.getCurrentCount())) {
				LogUtil.msgLog.info("copetition|joinTable|noCreateTable|" + param.getPlayingId() + "|" + param.getCurStep() + "|" + param.getCurRound() + "|" + player.getUserId() + "|" + player.getName() + "|IsCreateRoom:" + (randomRoom == null) + "|Config:" + config.getKeyId() + "|createRoom:" + randomRoom);
				return false;
			}

			CompetitionRoom createRoom = joinOrCreateCompetitionRoom(player, config, randomRoom, param);

			LogUtil.msgLog.info("competition|joinTable|process|finish|" + param.getPlayingId() + "|" + param.getCurStep() + "|" + param.getCurRound() + "|" + player.getUserId() + "|" + player.getName() + "|IsCreateRoom:" + (randomRoom == null) + "|Config:" + config.getKeyId() + "|createRoom:" + createRoom);

			player.saveDB(true);

			return true;
		}
	}

	/**
	 *@description 加入或创建房间
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/9
	 */
	public static CompetitionRoom joinOrCreateCompetitionRoom(Player player, CompetitionRoomConfig config, CompetitionRoom room, CompetitionClearingModelRes param) throws Exception {
		//这里先按userId剥离出来玩家
		Map<Long, CompetitionClearingPlay> plays = new HashMap<>();
		param.getPlays().stream().forEach(v -> plays.put(v.getUserId(), v));

		//创建需要等待加入房间的玩家, 这里防止多次加入, 使用unique索引做同步
		CompetitionRoomUser roomUser = null;
		try {
			roomUser = newRoomUser(player, room, plays, param);
			Long keyId = CompetitionDao.getInstance().saveRoomUser(roomUser);
			if (keyId == null || keyId.longValue() <= 0) {
				return null;
			}
		} catch (java.sql.SQLIntegrityConstraintViolationException e) {
			return null;
		}

		BaseTable table = createTable(player, config, room, param);
		room = table.getCompetitionRoom();

		synchronized (room) {
			//人满
			while (room.isFull()) {
				// 重新走创建流程
				table = createTable(player, config, null, param);
				room = table.getCompetitionRoom();
			}

			int updateCount = CompetitionDao.getInstance().updateCompetitionRoomUserRoomId(player.getUserId(), param.getPlayingId(), room.getKeyId());
			if (updateCount <= 0) {
				player.writeErrMsg(LangHelp.getMsg(LangMsg.code_221));
				return null;
			}
			int ret = CompetitionDao.getInstance().addCompetitionRoomPlayerCount(room.getKeyId(), 1);
			if (ret <= 0) {
				CompetitionDao.getInstance().deleteCompetitionRoomUser(room.getKeyId(), player.getUserId(), param.getPlayingId());
				return null;
			}
			room.setCurrentCount(room.getCurrentCount() + 1);
			table.addCompetitionRoomUser(roomUser);
			player.setPlayingTableId(table.getId());
//			player.setEnterServer(room.getServerId());
			player.saveBaseInfo();

			StringBuilder sb = new StringBuilder("CompetitionRoom|join");
			sb.append("|").append(room.getKeyId());
			sb.append("|").append(room.getModeId());
			sb.append("|").append(player.getUserId());
			LogUtil.msgLog.info(sb.toString());

			if (!room.canStart()) {
				// 等待开始游戏, 推送玩家加入房间
				player.writeSocket(table.buildCreateTableRes(player.getUserId()));
				return room;
			} else {
				//推送玩家加入房间
				player.writeSocket(table.buildCreateTableRes(player.getUserId()));
				//开局
				startCompetitionRoom(room);
			}
		}
		return room;
	}

	public static void batchStartGame(CompetitionClearingModelRes clearingArg) {
		try {
			int threadCount = Runtime.getRuntime().availableProcessors();
			long s = System.currentTimeMillis();
			AtomicInteger synWaitLock = new AtomicInteger();
			List<CompetitionRoom> competitionRooms = CompetitionDao.getInstance().loadAllCompetitionRoom(clearingArg.getPlayingId(), GameServerConfig.SERVER_ID);

			for (int i = 0; i < competitionRooms.size(); i++) {
				int j = i;
				synWaitLock.incrementAndGet();
				TaskExecutor.getInstance().submitTask(() -> {
					CompetitionRoom competitionRoom = competitionRooms.get(j);
					try {
						if(competitionRoom.isFull()){
							startCompetitionRoom(competitionRoom);
							LogUtil.msgLog.info("competition|batchStartGame|roomId|" + competitionRoom.getKeyId());
						}
					} catch (Exception e){
						LogUtil.msgLog.error("competition|batchStartGameError|",e);
					} finally {
						synWaitLock.decrementAndGet();
					}
				});

				while (synWaitLock.get() >= threadCount) {
					sleep(50);
				}
			}

			while (synWaitLock.get() > 0) {
				sleep(50);
			}

			LogUtil.msgLog.info("competition|batchJoinTable:" + competitionRooms.size() + " " + (System.currentTimeMillis() - s) + "ms");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static List<Long> batchJoinTable(CompetitionRoomConfig roomConfigArg, CompetitionClearingModelRes clearingArg) {
		int threadCount = Runtime.getRuntime().availableProcessors();
		List<Long>  res = new ArrayList<>();
		synchronized (roomConfigArg){
			try {
				List<CompetitionRoomUser> competitionRoomUsers = Optional.ofNullable(CompetitionDao.getInstance().loadCompetitionUser(clearingArg.getPlayingId(), GameServerConfig.SERVER_ID)).orElse(Collections.emptyList());

				Map<Long, List<CompetitionRoomUser>> groupByUserId = competitionRoomUsers.stream().collect(Collectors.groupingBy(CompetitionRoomUser::getUserId));

				ArrayList<Long> userIds = new ArrayList<>(groupByUserId.keySet());

				clearingArg.setPlays(competitionRoomUsers.stream().map(v->
						CompetitionClearingPlay.builder()
								.userId(v.getUserId())
								.score(v.getInitScore())
								.build()).collect(Collectors.toList()));

				shuffle(userIds, clearingArg, CompetitionUtil.getCompetitionRoomConfig(clearingArg.getRoomConfigId()));

				long s = System.currentTimeMillis();
				AtomicInteger synWaitLock = new AtomicInteger();
				for (int i = 0; i < userIds.size(); i++) {
					int j = i;
					synWaitLock.incrementAndGet();
					TaskExecutor.getInstance().submitTask(() -> {
						try {
							Long userId = userIds.get(j);
							LogUtil.msgLog.info("CompetitionRoom|batchJoinTable|" + userId);
							if(batchJoinTable(groupByUserId.get(userId).get(0), roomConfigArg, clearingArg)){
								res.add(userId);
								LogUtil.msgLog.info("CompetitionRoom|batchJoinTable|" + userId + "|join|" + clearingArg.getPlayingId() + "|" + clearingArg.getCurStep() + "|" + clearingArg.getCurRound());
							}else{
								LogUtil.msgLog.info("CompetitionRoom|batchJoinTable|" + userId + "|noJoin|" + clearingArg.getPlayingId() + "|" + clearingArg.getCurStep() + "|" + clearingArg.getCurRound());
							}
						} catch (Exception e){
							LogUtil.msgLog.error("batchJoinTable,error {}",e);
						} finally {
							synWaitLock.decrementAndGet();
						}
					});

					while (synWaitLock.get() >= threadCount) {
						sleep(50);
					}
				}

				while (synWaitLock.get() > 0) {
					sleep(50);
				}

                //任务相关
                if(clearingArg.isFirstMatch()){
                    CompetitionUtil.taskInvoke(userIds);
                }

				LogUtil.msgLog.info("competition|batchJoinTable:" + competitionRoomUsers.size() + " " + (System.currentTimeMillis() - s) + "ms");

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return res;
	}

	public static boolean batchJoinTable(CompetitionRoomUser roomUserArg, CompetitionRoomConfig roomConfigArg, CompetitionClearingModelRes clearingArg) {
		int addPlayCount = 0;
		long roomId = 0l;
		try {
			Player player;
			if(!roomUserArg.isNotRobot()){
				player = createRobot(roomUserArg.getUserId(), roomConfigArg.getPlayType());
			} else {
				player = PlayerManager.getInstance().getPlayer(roomUserArg.getUserId());
				if(player == null){
					player = PlayerManager.getInstance().loadPlayer(roomUserArg.getUserId(), roomConfigArg.getPlayType());
				}
			}

			CompetitionRoom room;
			if (roomUserArg.getRoomId() > 0) {
				room = CompetitionDao.getInstance().loadCompetitionRoom(roomUserArg.getRoomId());
			} else {
				//仅随机加入房
				room = CompetitionDao.getInstance().randomCompetitionRoom(roomConfigArg.getKeyId(), clearingArg.getPlayingId(), (long) GameServerConfig.SERVER_ID, clearingArg.getCurStep(), clearingArg.getCurRound());
			}

			BaseTable bt;
			//玩家已经加入,重复检测
			if (room != null && roomUserArg.getRoomId() > 0 && room.getCurrentState() < 2 && ((bt = TableManager.getInstance().getTable(room.getTableId())) == null || bt.getCompetitionRoomUserMap().containsKey(player.getUserId()))) {
				LogUtil.msgLog.info("CompetitionRoom|batchJoinTable|userUsedJoinTable|" + roomUserArg.getUserId());
				return false;
			}

			if(room == null || room.getCurrentState() >= CompetitionRoom.STATE_PLAYING){
				LogUtil.msgLog.info("CompetitionRoom|batchJoinTable|repeatedCheck|" + roomUserArg.getUserId() + "|" + room);
				return false;
			}

			if(player.getPlayingTableId() > 0){
//				LogUtil.i("CompetitionRoom|batchJoinTable|userInRoom|" + player.getUserId() + "|" + player.getPlayingTableId() + "|" + roomUserArg.getRoomId());
				BaseTable table = player.getPlayingTable();
				if (table != null && table.isCompetitionRoom()) {
					player.writeSocket(table.buildCreateTableRes(player.getUserId(), true, false));
					table.broadOnlineStateMsg();
				}
				LogUtil.msgLog.warn("competition|joinTable|process|tableIsNotNull|" + clearingArg.getPlayingId() + "|" + clearingArg.getCurStep() + "|" + clearingArg.getCurRound() + "|" + player.getUserId() + "|" + player.getPlayingTableId());
				return false;
			}

			roomId = room.getKeyId();

			//满人重新随机
			if(roomUserArg.getRoomId() <= 0) {
				while ((room.isFull() || (addPlayCount = CompetitionDao.getInstance().addCompetitionRoomPlayerCount(room.getKeyId(), 1)) <= 0)) {
					if ((room = CompetitionDao.getInstance().randomCompetitionRoom(roomConfigArg.getKeyId(), clearingArg.getPlayingId(), (long) GameServerConfig.SERVER_ID, clearingArg.getCurStep(), clearingArg.getCurRound())) == null) {
						LogUtil.i("CompetitionRoom|batchJoinTable|noFindFreeRoom|" + roomUserArg.getUserId());
						return false;
					}
					roomId = room.getKeyId();
				}
			}

			synchronized (room) {
				BaseTable table = TableManager.getInstance().getTable(room.getTableId());
				table.setCompetitionRoom(room);
				roomUserArg.setRoomId(roomId);
				table.addCompetitionRoomUser(roomUserArg);
				TableManager.getInstance().addCompetitionTable(table);
				room.setCurrentCount(CompetitionDao.getInstance().loadRoomCurrentCount(room.getKeyId()));
				//更新玩家的房间,这里可能存在,已经被分配过
				CompetitionDao.getInstance().updateCompetitionRoomUserRoomId(player.getUserId(), room.getPlayingId(), room.getKeyId());
				player.setPlayingTableId(table.getId());
				player.saveBaseInfo();
				// 等待开始游戏, 推送玩家加入房间
				player.writeSocket(table.buildCreateTableRes(player.getUserId()));

				LogUtil.i("CompetitionRoom|batchJoinTable|" + room.getPlayingId() + "|" + room.getCurStep() + "|" + room.getCurRound() + "|" + player.getUserId() + "|" + roomId + "|" + player.getPlayingTableId() + "|" + table.getId() + "|" + table.getCompetitionRoomUserMap().size() + "|" + room.getCurrentCount() + "|" + table.getCompetitionRoomUserMap().values());
			}
			addPlayCount = 0 ;
			return true;
		} catch (Exception e) {
			LogUtil.msgLog.error("batchJoinTableErr,{}",e);
			if (addPlayCount > 0) {
				CompetitionDao.getInstance().addCompetitionRoomPlayerCount(roomId, -1);
			}
		}
		return false;
	}

	 public static void tCreate(int tableCount, CompetitionRoomConfig roomConfigArg, CompetitionClearingModelRes clearingParamArg) {
		 int threadCount = Runtime.getRuntime().availableProcessors();
		 long s = System.currentTimeMillis();
		 AtomicInteger synWaitLock = new AtomicInteger();
		 for (int i = 0; i < tableCount; i++) {
			 synWaitLock.incrementAndGet();
			 TaskExecutor.getInstance().submitTask(() -> {
				 try {
					 createTable(createRobot(-1l, roomConfigArg.getPlayType()), roomConfigArg, null, clearingParamArg);
				 } finally {
					 synWaitLock.decrementAndGet();
				 }
			 });
			 while (synWaitLock.get() >= threadCount) {
				 sleep(200);
			 }
		 }

		 while (synWaitLock.get() > 0) {
			 sleep(200);
		 }

		 LogUtil.msg("competition|tCreateTable:" + tableCount + " " + (System.currentTimeMillis() - s) + "ms");
	 }

	public static void sleep(long t) {
		try {
			Thread.sleep(t);
		}catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 *@description 创建一个比赛场牌桌
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/7/1
	 */
	public static BaseTable createTable(Player player, CompetitionRoomConfig config, CompetitionRoom room, CompetitionClearingModelRes param) {
		BaseTable table = null;
		try {
			if (room == null) {
				room = newRoom(config, param);
				Long roomId = CompetitionDao.getInstance().saveRoom(room);
				if (roomId == null || roomId.longValue() <= 0) {
					return null;
				}
				table = TableManager.getInstance().createCompetitionRoomTable(player, config, room);
				if (table == null || !table.isCompetitionRoom()) {
					CompetitionDao.getInstance().deleteCompetitionRoomByKeyId(room.getKeyId());
					return null;
				}

				TableManager.getInstance().addCompetitionTable(table);

				StringBuilder sb = new StringBuilder("CompetitionRoom|create");
				sb.append("|").append(room.getKeyId());
				sb.append("|").append(room.getModeId());
				sb.append("|").append(player.getUserId());
				LogUtil.msgLog.info(sb.toString());
			} else {
				table = TableManager.getInstance().getTable(room.getTableId());
				if (table == null) {
					CompetitionDao.getInstance().deleteCompetitionRoomByKeyId(room.getKeyId());
					CompetitionDao.getInstance().deleteCompetitionRoomUserByRoomId(room.getKeyId());
					return null;
				}
			}
		} catch (Exception e) {
			LogUtil.e("competition|joinTable|createTable|error|"+e);
		}


		return table;
	}

	/**
	 *@description 游戏开局
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/6/5
	 */
	public static void startCompetitionRoom(CompetitionRoom room) throws Exception {
		synchronized (room) {
			if (room == null || !room.canStart()) {
				LogUtil.msgLog.info("CompetitionRoom|start|error|configIsNull|" + (room != null ? JSON.toJSONString(room) : ""));
				return;
			}
			CompetitionRoomConfig config = getRoomConfig(room.getConfigId());
			if (config == null) {
				LogUtil.msgLog.info("CompetitionRoom|start|error|configIsNull|" + room.getKeyId());
				return;
			}

			BaseTable table = TableManager.getInstance().getTable(room.getTableId());
			if (table == null) {
				LogUtil.msgLog.info("CompetitionRoom|start|error|tableIsNull|" + room.getKeyId());
				return;
			}

			//开始游戏
			if(CompetitionDao.getInstance().updateCompetitionRoomCurrentState(room.getKeyId(), CompetitionRoom.STATE_PLAYING)<=0){
				//已经开局
				return;
			}

			Map<Long, CompetitionRoomUser> playingUserMap = table.getCompetitionRoomUserMap();

			if (playingUserMap.size() != room.getMaxCount()) {
				List<CompetitionRoomUser> competitionRoomUsers = CompetitionDao.getInstance().loadAllCompetitionRoomUser(room.getKeyId());
				playingUserMap.clear();
				competitionRoomUsers.forEach(v -> playingUserMap.put(v.getUserId(), v));
			}

			List<Player> playerList = new ArrayList<>(playingUserMap.size());
			for (CompetitionRoomUser gu : playingUserMap.values()) {
				Player tempPlayer = PlayerManager.getInstance().getPlayer(gu.getUserId());
				if(!gu.isNotRobot()) {
					tempPlayer = getRobot(gu.getUserId(), config.getPlayType());
				} else if (tempPlayer == null) {
					tempPlayer = PlayerManager.getInstance().loadPlayer(gu.getUserId(), config.getPlayType());
					tempPlayer.setIsOnline(0);
					tempPlayer.setIsEntryTable(SharedConstants.table_offline);
					LogUtil.msgLog.info("CompetitionRoom|userOnline|0|" + gu.getUserId());
				}
				playerList.add(tempPlayer);
			}

			// -----------随机房主-------------------
			Player master = playerList.get(new SecureRandom().nextInt(playerList.size()));
//			playerList.add(0, master);

			// -----------牌桌进入准备阶段-------------------
			table.changeTableState(SharedConstants.table_state.ready);
			if (TableManager.getInstance().addTable(table) == 1) {
				table.saveSimpleTable();
			}

			table.setMasterId(master.getUserId());
			table.changeExtend();
			StringBuilder sb;
			List<Player> tablePlayerList = new ArrayList<>(playerList.size());
			for (Player player1 : playerList) {
				Player player2 = PlayerManager.getInstance().changePlayer(player1, table.getPlayerClass());

				// 加入牌桌
				if (!table.joinPlayer(player2)) {
					sb = new StringBuilder("CompetitionRoom|error|join");
					sb.append("|").append(table.getId());
					sb.append("|").append(room.getKeyId());
					sb.append("|").append(room.getModeId());
					sb.append("|").append(player1.getUserId());
					LogUtil.msgLog.info(sb.toString());
					continue;
				} else {
					player2.changeTotalPoint(table.getCompetitionRoomUser(player1.getUserId()).getInitScore());
					tablePlayerList.add(player2);
				}

				sb = new StringBuilder("CompetitionRoom|Start");
				sb.append("|").append(table.getId());
				sb.append("|").append(room.getKeyId());
				sb.append("|").append(room.getModeId());
				sb.append("|").append(player1.getUserId());
				sb.append("|").append(player1.getTotalPoint());
				LogUtil.msgLog.info(sb.toString());

				table.ready(player2);

				//XXX 比赛场新增推送, 定庄家之后刷新玩家所在房间桌面
				player2.writeSocket(table.buildCreateTableRes(player2.getUserId()));
			}

			Map<Long, GeneratedMessage> msgMap = new HashMap<>();

			// 给玩家推送加入房间消息时，先发自己加入房间的消息
			for (Player p : tablePlayerList) {
				GeneratedMessage msg = buildJoinTableRes(table, p);
				msgMap.put(p.getUserId(), msg);
				p.writeSocket(msg);
			}

			// 给玩家推送加入房间消息时，发别人的加入房间的消息
			tablePlayerList.forEach(p -> msgMap.entrySet().stream().filter(v -> p.getUserId() != v.getKey().longValue()).map(v -> v.getValue()).forEach(p::writeSocket));

			//推送在线消息
			table.broadOnlineStateMsg();

			room.setCurrentState(CompetitionRoom.STATE_PLAYING);

			int delay = 0;
			if (delay > 0) {
				TaskExecutor.delayExecutor.schedule(() -> {
					table.checkDeal();
					table.startNext();

					if(table.isPlaying()){
						//增加现有房间数
						long l = CompetitionDao.getInstance().addCompetitionCurPlayingTableCount(room.getPlayingId(), 1);
						LogUtil.msgLog.info("competition|startCompetitionRoom|{}|{}|{}|{}|{}", room.getPlayingId(), room.getCurStep(), room.getCurRound(), l, room);
					}
				}, delay, TimeUnit.MILLISECONDS);
			} else {
				try {
					table.checkDeal();
					table.startNext();
				}finally{
					if(table.isPlaying()) {
						//增加现有房间数
						long l = CompetitionDao.getInstance().addCompetitionCurPlayingTableCount(room.getPlayingId(), 1);
						LogUtil.msgLog.info("competition|startCompetitionRoom|{}|{}|{}|{}|{}", room.getPlayingId(), room.getCurStep(), room.getCurRound(), l, room);
					}else {
						try {
							if (table.getCompetitionRoom() != null) {
								table.getCompetitionRoom().setCurrentState(CompetitionRoom.STATE_NORMAL_OVER);
							}
							CompetitionDao.getInstance().updateCompetitionRoomCurrentState(room.getKeyId(), CompetitionRoom.STATE_NORMAL_OVER);
							CompetitionDao.getInstance().deleteCompetitionRoomUserByRoomId(room.getKeyId());
							CompetitionDao.getInstance().deleteCompetitionRoomByKeyId(room.getKeyId());
						} catch (Exception e) {
							LogUtil.errorLog.error("competition|clearingData|joinTable|Exception:" + e.getMessage(), e);
						}

						try {
							Iterator<Player> iterator = Optional.ofNullable(table.getPlayerMap()).orElse(Collections.emptyMap()).values().iterator();
							while (iterator.hasNext()) {
								Player next = iterator.next();
								next.setPlayingTableId(0);
								next.saveBaseInfo();
							}
						} catch (Exception e) {
							LogUtil.errorLog.error("competition|clearingPlayData|joinTable|Exception:" + e.getMessage(), e);
						}

						LogUtil.msgLog.info("competition|startCompetitionRoom|tableNoPlaying|{}|{}|{}|{}|{}", room.getPlayingId(), room.getCurStep(), room.getCurRound(), table.getId(), table.getCompetitionRoomUserMap(), room);
						table.setCompetitionRoom(null);
					}
				}

			}
		}
	}

	/**
	 *@description
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/7/1
	 */
	private static GeneratedMessage buildJoinTableRes(BaseTable table, Player player2) {
		TableRes.JoinTableRes.Builder joinRes = TableRes.JoinTableRes.newBuilder();
		joinRes.setPlayer(player2.buildPlayInTableInfo());
		joinRes.setWanfa(table.getPlayType());
		return joinRes.build();
	}


	/**
	 *@description 获取房间配置信息
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/7/1
	 */
	public static CompetitionRoomConfig getRoomConfig(long keyId) {
		return getRoomConfig(keyId, true);
	}

	/**
	 * 比赛场配置
	 *
	 * @param keyId   t_competition_room_config.keyId
	 * @param isValid 是否必须是有效的
	 * @return
	 */
	public static CompetitionRoomConfig getRoomConfig(long keyId, boolean isValid) {
		CompetitionRoomConfig config = CompetitionUtil.ALL_MAP.get(keyId);
		if (config == null) {
			return null;
		}
		if (isValid && !config.isValid()) {
			return null;
		}
		return config;
	}


	/**
	 *@description 创建新比赛房玩家
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/7/1
	 */
	private final static CompetitionRoomUser newRoomUser(Player player, CompetitionRoom room, Map<Long, CompetitionClearingPlay> plays, CompetitionClearingModelRes param) {
		return CompetitionRoomUser.builder()
				.createdTime(new Date())
				.gameResult(0L)
				.logIds("")
//				.roomId(room.getKeyId())
				.roomId(0l)
				.userId(player.getUserId())
//				.status(CompetitionRoomUser.MATCH_SUCCESS)
				.initScore(plays.get(player.getUserId()).getScore())
				.playingId(param.getPlayingId())
				.build();
	}

	private final static CompetitionRoom newRoom(CompetitionRoomConfig config, CompetitionClearingModelRes param) {
		CompetitionRoom room = new CompetitionRoom();
		room.setConfigId(config.getKeyId());
		room.setPlayingId(param.getPlayingId());
		room.setCurStep(param.getCurStep());
		room.setCurRound(param.getCurRound());
		room.setModeId(config.getKeyId().toString());
		room.setServerId(GameServerConfig.SERVER_ID);
		room.setCurrentCount(0);
		room.setCurrentState(CompetitionRoom.STATE_NEW);
		room.setGameCount(config.getTotalBureau());
		room.setMaxCount(config.getPlayerCount());
		room.setTableMsg(config.getTableMsg());
		room.setExt(param.getBaseScore() + CompetitionRoom.RATIO_PREX + config.getName() + CompetitionRoom.RATIO_PREX + param.getWeedOutScore() + CompetitionRoom.RATIO_PREX + param.getCurStep() + CompetitionRoom.RATIO_PREX + param.getCurRound());
		room.setTableName(String.format(config.getName().replace("{p}","%s"), param.getBaseScore()) /*+ "&" + config.getName()*/);
		room.setCreatedTime(new Date());
		room.setModifiedTime(room.getCreatedTime());
		//仅保留头部基础数据
		room.setCompetitionClearingModelRes(param.copyBasicOption());
		return room;
	}

	public static Player createRobot(Long userId, int payType) {
		Player player  = PlayerManager.getInstance().getInstancePlayer(payType);
		player.setUserId(userId);
		player.setName(""+userId);
		player.setIsOnline(1);
		player.setRobot(true);
//		try {
//			player.setAutoPlayCheckedTime(1);
//			player.setAutoPlayCheckedTimeAdded(true);
//			player.setAutoPlayChecked(true);
//			Field autoPlay = player.getClass().getDeclaredField("autoPlay");
//			if (autoPlay != null) {
//				autoPlay.setAccessible(true);
//				autoPlay.set(player, true);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		return player;
	}

	public static void addRobot(Player player) {
		ROBOT_CACHE.put(player.getUserId(), player);
	}

	public static Player getRobot(Long userId, int playType) {
//		Player player = ROBOT_CACHE.get(userId);
//		if (player == null) {
//			addRobot(createRobot(userId, playType));
//			return getRobot(userId, playType);
//		}
//
//		try {
//			player.setAutoPlayCheckedTime(1);
//			player.setAutoPlayCheckedTimeAdded(true);
//			player.setAutoPlayChecked(true);
//			Field autoPlay = player.getClass().getDeclaredField("autoPlay");
//			if (autoPlay != null) {
//				autoPlay.setAccessible(true);
//				autoPlay.set(player, true);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

		return createRobot(userId, playType);
	}

	/**
	 *@description 清理比赛房
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/7/1
	 */
	public static void clearInvalidTable(long playingId, boolean onlyClearInvalid, boolean force, String curStep, String curRound) {
		TableManager.getInstance().clearInvalidCompetitionRoom(playingId, onlyClearInvalid, force, curStep, curRound);
	}
}
