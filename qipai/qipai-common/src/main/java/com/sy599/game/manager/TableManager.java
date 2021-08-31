package com.sy599.game.manager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.GeneratedMessage;
import com.sy.mainland.util.redis.Redis;
import com.sy.mainland.util.redis.RedisUtil;
import com.sy599.game.GameServerConfig;
import com.sy599.game.assistant.AssisServlet;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.character.ProtoPlayer;
import com.sy599.game.common.bean.CardConsume;
import com.sy599.game.common.bean.CreateTableInfo;
import com.sy599.game.common.bean.PayParam;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.LogConstants;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.common.executor.TaskExecutor;
import com.sy599.game.db.bean.RegInfo;
import com.sy599.game.db.bean.Server;
import com.sy599.game.db.bean.SoloRoomConfig;
import com.sy599.game.db.bean.TableInf;
import com.sy599.game.db.bean.competition.CompetitionRoom;
import com.sy599.game.db.bean.competition.CompetitionRoomConfig;
import com.sy599.game.db.bean.competition.CompetitionRoomUser;
import com.sy599.game.db.bean.gold.GoldRoom;
import com.sy599.game.db.bean.gold.GoldRoomConfig;
import com.sy599.game.db.bean.gold.GoldRoomUser;
import com.sy599.game.db.bean.group.GroupInfo;
import com.sy599.game.db.bean.group.GroupTable;
import com.sy599.game.db.bean.group.GroupTableConfig;
import com.sy599.game.db.bean.group.GroupUser;
import com.sy599.game.db.dao.CompetitionDao;
import com.sy599.game.db.dao.TableDao;
import com.sy599.game.db.dao.UserDao;
import com.sy599.game.db.dao.gold.GoldRoomDao;
import com.sy599.game.db.dao.group.GroupDao;
import com.sy599.game.db.enums.CardSourceType;
import com.sy599.game.db.enums.UserMessageEnum;
import com.sy599.game.jjs.bean.MatchBean;
import com.sy599.game.jjs.util.JjsUtil;
import com.sy599.game.message.MessageUtil;
import com.sy599.game.msg.serverPacket.BaiRenTableMsg.BaiRenTableRes;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.msg.serverPacket.TableRes;
import com.sy599.game.msg.serverPacket.TableRes.CreateTableRes;
import com.sy599.game.msg.serverPacket.TableRes.JoinTableRes;
import com.sy599.game.shutdown.ShutDownAction;
import com.sy599.game.util.*;
import com.sy599.game.util.constants.GroupConstants;
import com.sy599.game.util.gold.constants.GoldConstans;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.WebSocketServerHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TableManager {
    private static TableManager _inst = new TableManager();
    private static final Map<Long, BaseTable> tableMap = new ConcurrentHashMap<>();
    private static final Map<Long, GoldRoom> goldRoomMap = new ConcurrentHashMap<>();
    private static final Map<Long, CompetitionRoom> competitionRoomMap = new ConcurrentHashMap<>();
    //比赛场房间
	private static final Map<Long, BaseTable> competitionTableMap = new ConcurrentHashMap<>();

	//金币场房间
    private static final Map<Long, BaseTable> goldTableMap = new ConcurrentHashMap<>();
    //金币场房间
    private static final Map<Long, BaseTable> scoreTableMap = new ConcurrentHashMap<>();

    public static final Map<Integer, Class<? extends BaseTable>> wanfaTableTypes = new HashMap<>();
    private static final List<Long> tableIdList = new ArrayList<>();
    //记录特殊消息需要转换的player信息
    public static Map<Integer, Class<? extends Player>> msgPlayerTypes = new HashMap<>();
    /**
     * 俱乐部牌桌
     */
    private static final Map<String, List<BaseTable>> serverTypeMap = new ConcurrentHashMap<>();

    /**
     * 自动完成的任务超时时间
     */
    public static final int AUTO_TASK_TIMEOUT = 3;//3s

    public static boolean wanfaTableTypesPut(Integer gameType, Class<? extends BaseTable> tableClass) {
        if (wanfaTableTypes.containsKey(gameType)) {
            StringBuilder sb = new StringBuilder("TableManager|wanfaTableTypesPut|error|hasSameWanFa");
            sb.append("|").append(gameType);
            sb.append("|").append(tableClass.getName());
            sb.append("|").append(wanfaTableTypes.get(gameType).getName());
            throw new RuntimeException(sb.toString());
        }
        if (GameConfigUtil.hasGame(gameType)) {
            wanfaTableTypes.put(gameType, tableClass);
            return true;
        }
        return false;
    }

    public static TableManager getInstance() {
        return _inst;
    }

    private TableManager() {
    }

    /**
     * 检查玩法和table是否匹配
     *
     * @param playType
     * @param tableClass
     * @return
     */
    public static boolean checkTableType(Integer playType, Class<? extends BaseTable> tableClass) {
        return tableClass == wanfaTableTypes.get(playType);
    }

    public void initData() {
        JjsUtil.loadMatchData();
        loadFromDB();
    }


    private void loadFromDB() {
        long time1 = TimeUtil.currentTimeMillis();
        int totalCount = TableDao.getInstance().selectCount();
        LogUtil.monitor_i("loadTable count-->" + totalCount);
        int start = 0;
        long time2 = TimeUtil.currentTimeMillis();
        LogUtil.monitor_i("time2:" + (time2 - time1));

        List<Long> deleteds = new ArrayList<>();
        while (start < totalCount) {
            long time3 = TimeUtil.currentTimeMillis();
            List<TableInf> list = TableDao.getInstance().selectAll(start, 1000);
            if (list == null || list.size() == 0) {
                break;
            }
            long time4 = TimeUtil.currentTimeMillis();
            LogUtil.monitor_i("time3:" + (time4 - time3) + " table size:" + list.size());
            for (TableInf info : list) {
                BaseTable table = getInstanceTable(info.getPlayType());
                if (table == null) {
                    continue;
                }
                table.loadFromDB(info);

                if (NumberUtils.isDigits(table.getServerKey())) {
                    try {
                        GroupTable groupTable = GroupDao.getInstance().loadGroupTableByKeyId(table.getServerKey());
                        if (groupTable != null) {
                            GroupTableConfig groupTableConfig = GroupDao.getInstance().loadGroupTableConfig(groupTable.getConfigId());
                            if (groupTableConfig != null) {
                                table.setCheckPay(groupTableConfig.getPayType().intValue() == 1);
                            }
                        }
                    } catch (Exception e) {
                        LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
                    }
                }

                boolean reload = true;
                if (table.isGroupRoom() && table.getServerKey().contains("_")) {
                    try {
                        String[] temps = table.getServerKey().split("_");
                        if (temps.length >= 2) {
                            GroupTable groupTable = GroupDao.getInstance().loadGroupTableByKeyId(temps[1]);
                            if (groupTable == null || groupTable.isOver()) {
                                reload = false;

                                if (Redis.isConnected()) {
                                    RedisUtil.zrem(GroupRoomUtil.loadGroupKey(temps[0].substring(5), temps.length >= 3 ? Integer.parseInt(temps[2]) : 0), temps[1]);
                                    RedisUtil.hdel(GroupRoomUtil.loadGroupTableKey(temps[0].substring(5), temps.length >= 3 ? Integer.parseInt(temps[2]) : 0), temps[1]);
                                }

                                LogUtil.errorLog.error("reload table error:tableId=" + table.getId() + ",serverKey=" + table.getServerKey());
                            }
                        }
                    } catch (Throwable t) {
                        LogUtil.errorLog.error("Throwable:" + t.getMessage(), t);
                    }
                }
                table.initGroupInfo();
                if (!table.initGoldRoom()) {
                    reload = false;
                }
                table.initGroupInfo();
                table.initCompetitionRoom();
                if (reload) {
                    addTable(table);
                    LogUtil.monitorLog.info("init table:tableId=" + info.getTableId());
                    if (table.getServerType() == 0) {
                        if (StringUtils.isNotBlank(table.getServerKey()) && !NumberUtils.isDigits(table.getServerKey())) {
                            if (table.getState() == table_state.ready && table.getPlayBureau() <= 1) {
                                List<BaseTable> list1 = serverTypeMap.get(table.getServerKey());
                                if (list1 == null) {
                                    list1 = new ArrayList<>();
                                    list1.add(table);
                                    serverTypeMap.put(table.getServerKey(), list1);
                                } else {
                                    if (table.getPlayerCount() < table.getMaxPlayerCount()) {
                                        list1.add(0, table);
                                    } else {
                                        list1.add(table);
                                    }
                                }
                            }
                        }
                    }
                } else {
                    deleteds.add(info.getTableId());
                }
            }
            long time5 = TimeUtil.currentTimeMillis();
            LogUtil.monitorLog.info("time4:" + (time5 - time4));
            start += 1000;
        }

        if (deleteds.size() > 0) {
            for (Long tableId : deleteds) {
                int ret = TableDao.getInstance().delete(tableId.longValue());
                LogUtil.monitorLog.info("init table datas:delete from table_inf where tableId=" + tableId + ",ret=" + ret);
            }
            LogUtil.monitorLog.info("init table datas:delete table count=" + deleteds.size());
        }

        for (BaseTable table : tableMap.values()) {
            for (Player player : table.getPlayerMap().values()) {
                player.setSyncTime(new Date());
            }
        }
    }

    public BaseTable getInstanceTable(int playType) {
        Class<? extends BaseTable> cls = wanfaTableTypes.get(playType);
        if (cls == null) {
            LogUtil.errorLog.error("getInstanceTable error:table is not exists:playType=" + playType);
        } else {
            try {
                BaseTable table = ObjectUtil.newInstance(cls);
                table.setPlayType(playType);
                return table;
            } catch (Exception e) {
                LogUtil.errorLog.error("getInstanceTable err:" + e.getMessage(), e);
            }
        }
        return null;
    }

    /**
     * 获取房间id
     *
     * @param createId
     * @param type
     * @param tableType 房间类型 0普通开房，1军团开房，2代开房
     * @return
     */
    public long generateId(long createId, int type, int tableType) {
        return generateId(createId, type, tableType, GameServerConfig.SERVER_ID);
    }

    public synchronized long generateId(long createId, int type, int tableType, int serverId) {
//		if (GameServerConfig.isMemcache) {
//			// 从redis中获取房间id
//			Long roomId = getRoomIdToCached();
//			if (roomId != null) {
//				return roomId;
//			}
//		}
        long tableId = TableDao.getInstance().callProCreateRoom(createId, serverId, type, tableType);
        return tableId;
    }

    public static Map<String, List<BaseTable>> getServerTypeMap() {
        return serverTypeMap;
    }

    public int addTable(BaseTable table) {
        if (table.getId() > 0) {
            synchronized (this) {
                BaseTable table0 = tableMap.put(table.getId(), table);
                tableIdList.add(table.getId());
				addCompetitionTable(table);
                if (table.isGoldRoom()) {
                    goldRoomMap.put(table.getGoldRoom().getKeyId(), table.getGoldRoom());
				} else if (table.isCompetitionRoom() && table.getCompetitionRoom() != null) {
					competitionRoomMap.put(table.getCompetitionRoomId(), table.getCompetitionRoom());
                }
                return table0 == null ? 1 : 2;
            }
        }
        return 0;
    }

    /**
     * 添加金币场房间
     *
     * @param table
     */
    public int addGoldTable(BaseTable table) {
        if (table.isGoldRoom()) {
            BaseTable table1 = goldTableMap.put(table.getId(), table);
            if (table1 != null && table1.getPlayerCount() > 0) {
                goldTableMap.put(table.getId(), table1);
                return 2;
            } else {
                return 1;
            }
        }
        return 0;
    }

    /**
     * 添加比赛场房间
     *
     * @param table
     */
    public int addCompetitionTable(BaseTable table) {
        if (table.isCompetitionRoom() && !competitionTableMap.containsKey(table.getId())) {
            BaseTable table1 = competitionTableMap.put(table.getId(), table);
            if (table1 != null && table1.getPlayerCount() > 0) {
                competitionTableMap.put(table.getId(), table1);
                return 2;
            } else {
                return 1;
            }
        }
        return 0;
    }

    /**
     * 如果deleted==true，则只从内存中删除，否则也要删除数据库中的数据
     *
     * @param table
     * @param deleted 是否已被删除
     * @return
     */
    public int delTable(BaseTable table, boolean deleted) {
        int result = 0;
        BaseTable tempTable = tableMap.remove(table.getId());

        if (table.isGoldRoom()) {
            goldTableMap.remove(table.getId());
        }

        if(table.isCompetitionRoom()){
        	competitionTableMap.remove(table.getId());
			competitionRoomMap.remove(table.getId());
		}

        int reset = 0;
        int del;
        if (!deleted) {
            del = TableDao.getInstance().delete(table.getId());
            if (del > 0) {
                reset = TableDao.getInstance().clearRoom(table.getId());
                GotyeChatManager.getInstance().deleteGotyeRoomId(table.getGotyeRoomId());
                if (table.isDaikaiTable()) {
                    result = table.dissDaikaiTable();
                } else {
                    result = del;
                }
            }
        } else {
            TableDao.getInstance().delete(table.getId());
            del = 0;
        }

        LogUtil.msgLog.info("delTable|" + (tempTable == null ? "null" : "deleted") + "|" + table.getId() + "|" + table.getPlayBureau() + "|" + table.getServerKey() + "|" + del + "|" + reset + "|" + deleted);

        if (tempTable != null) {
            tableIdList.remove(Long.valueOf(table.getId()));
            if (StringUtils.isNotBlank(tempTable.getServerKey())) {
                List<BaseTable> list = serverTypeMap.get(tempTable.getServerKey());
                if (list != null) {
                    boolean ret = list.remove(tempTable);
                    LogUtil.msgLog.info("delTable from serverTypeMap:tableId=" + table.getId() + ",serverType=" + table.getServerType() + ",serverKey=" + table.getServerKey() + ",result=" + ret);
                }
            }
        }

        if (!deleted) {
            String tableKeyId = table.getServerKey();
            if (NumberUtils.isDigits(tableKeyId)) {
                try {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("keyId", tableKeyId);
                    map.put("currentState", table.getDissCurrentState());

                    GroupDao.getInstance().updateGroupTableByKeyId(map);
                } catch (Exception e) {
                    LogUtil.errorLog.error("Group Exception:" + e.getMessage(), e);
                }
            }
        }

        return result;
    }

    public static boolean removeUnavailableTable(BaseTable table) {
        if (StringUtils.isNotBlank(table.getServerKey())) {
            List<BaseTable> list = serverTypeMap.get(table.getServerKey());
            if (list != null) {
                boolean ret = list.remove(table);
                LogUtil.msgLog.info("removeUnavailableTable from serverTypeMap:tableId=" + table.getId() + ",serverType=" + table.getServerType() + ",serverKey=" + table.getServerKey() + ",state=" + table.getState() + ",result=" + ret);
                return ret;
            }
        }
        return false;
    }

    public BaseTable getTable(long id) {
        if (id > 0)
            return tableMap.get(id);
        else
            return null;
    }

    public GoldRoom getGoldRoom(long id) {
        if (id > 0)
            return goldRoomMap.get(id);
        else
            return null;
    }

    public CompetitionRoom getCompetitionRoom(long id) {
        if (id > 0)
            return competitionRoomMap.get(id);
        else
            return null;
    }

    public int getTableCount() {
        return tableMap.size();
    }

    public void saveDB(boolean asyn) {

        int count = tableMap.size();
        if (count > 0) {
            if (asyn) {
                TaskExecutor.SINGLE_EXECUTOR_SERVICE_TABLE.execute(new Runnable() {
                    @Override
                    public void run() {
                        List<Map<String, Object>> list = saveDB0(true);
                        if (list != null && list.size() > 0) {
                            TableDao.getInstance().batchUpdate(list);
                        }

                    }
                });
            } else {
                List<Map<String, Object>> list = saveDB0(asyn);
                if (list != null && list.size() > 0) {
                    TableDao.getInstance().batchUpdate(list);
                }
            }
        }
    }

    private final static List<Map<String, Object>> saveDB0(boolean asyn) {
        int count = tableMap.size();
        if (!WebSocketServerHandler.isOpen) {
            LogUtil.msgLog.info("TableManager|saveDB0|start|" + count);
        }
        if (count > 0) {
            List<Map<String, Object>> list = new ArrayList<>(count);
            for (Map.Entry<Long, BaseTable> kv : tableMap.entrySet()) {
                if (!WebSocketServerHandler.isOpen) {
                    LogUtil.msgLog.info("TableManager|saveDB0|save|" + kv.getValue().getId());
                }
                Map<String, Object> map = kv.getValue().saveDB(asyn);
                if (map != null && map.size() > 0) {
                    list.add(map);
                }
            }
            if (!WebSocketServerHandler.isOpen) {
                LogUtil.msgLog.info("TableManager|saveDB0|end|" + count);
            }
            return list;
        }
        return null;
    }

    private static void execute(final CountDownLatch countDownLatch, final BaseTable table) {
        TaskExecutor.EXECUTOR_SERVICE.execute(new Runnable() {
            @Override
            public void run() {
                int referenceCount;
                if ((referenceCount = table.getReferenceCounter().get()) >= 1 || referenceCount < 0) {
                    countDownLatch.countDown();
                    LogUtil.errorLog.warn("table is dead ? tableId={},referenceCount={},players={}", table.getId(), referenceCount, table.getPlayerMap().keySet());
                } else {
                    table.getReferenceCounter().addAndGet(1);

                    if (table.getMaxPlayerCount() == table.getPlayerCount() || table.getPlayedBureau() > 0) {
                        try {
                            table.checkCompetitionPlay();
                            table.checkRobotPlay();
                        } catch (Throwable e) {
                            LogUtil.errorLog.error("Exception|" + table.getId() + "|" + e.getMessage(), e);
                        }
                    }

                    try {
                        boolean isdiss = table.checkRoomDiss();
                        if (isdiss) {
                            LogUtil.monitor_i("apply diss table timeout-->id:" + table.getId() + " time:" + table.getCreateTime().toString() + " createId:" + table.getMasterId() + ",result=1");
                        } else {
                            isdiss = table.checkDissByDate();
                            if (isdiss) {
                                LogUtil.msgLog.info("BaseTable|dissReason|execute|1|" + table.getId() + "|" + table.getPlayBureau());
                                int ret = table.diss();
                                LogUtil.monitor_i("auto deltable-->id:" + table.getId() + " time:" + table.getCreateTime().toString() + " createId:" + table.getMasterId() + ",result=" + ret);

                                // 智能补房
                                GameUtil.autoCreateGroupTableNew(table);
                            }
                        }
                    } catch (Throwable t) {
                        LogUtil.errorLog.error("Throwable:" + t.getMessage(), t);
                    }

                    table.getReferenceCounter().addAndGet(-1);
                    countDownLatch.countDown();
                }
            }
        });
    }

    public void checkCompetitionTask() {
        try {
            BaseTable[] tables = tableMap.values().toArray(new BaseTable[0]);
            int count = tables.length;
            if (count > 0) {
                long startTime = System.currentTimeMillis();
                CountDownLatch countDownLatch = new CountDownLatch(count);
                for (BaseTable table : tables) {
                    execute(countDownLatch, table);
                }
                boolean isOk = false;
                try {
                    isOk = countDownLatch.await(AUTO_TASK_TIMEOUT, TimeUnit.SECONDS);
                } catch (Throwable e) {
                    LogUtil.errorLog.error("Exception:count=" + countDownLatch.getCount() + ",msg=" + e.getMessage(), e);
                } finally {
                    if (System.currentTimeMillis() - startTime > 50) {
                        LogUtil.msgLog.info("auto play:table count=" + count + ",time(ms)=" + (System.currentTimeMillis() - startTime) + ",ok=" + isOk);
                    }
                }
            }

            long currentTime = System.currentTimeMillis();
            //金币场房间检查
            for (BaseTable table : goldTableMap.values()) {
                if (table.getReferenceCounter().get() >= 1) {
                    continue;
                }

                if (table.isGoldRoom() && table.allowRobotJoin() && table.getPlayerCount() < table.getMaxPlayerCount()) {
                    if (table.getLastCheckTime() == 0L) {
                        table.setLastCheckTime(currentTime);
                        continue;
                    } else if (currentTime - table.getLastCheckTime() < GoldConstans.loadRobotJoinTime()) {
                        continue;
                    }

                    synchronized (Constants.GOLD_LOCK) {
                        if (table.getPlayerCount() < table.getMaxPlayerCount()) {
                            GoldRoom goldRoom = GoldRoomDao.getInstance().loadGoldRoom(table.getId());
                            if (goldRoom != null && goldRoom.isNotStart() && goldRoom.getCurrentCount().intValue() > 0 && goldRoom.getCurrentCount().intValue() < goldRoom.getMaxCount().intValue()) {

                                List<HashMap<String, Object>> list = GoldRoomDao.getInstance().loadGoldRoomUserIds(goldRoom.getKeyId());
                                List<Player> playerList = new ArrayList<>(list.size());
                                for (HashMap<String, Object> map : list) {
                                    long tempUserId = Long.parseLong(map.get("userId").toString());
                                    Player tempPlayer = PlayerManager.getInstance().getPlayer(tempUserId);
                                    if (tempPlayer == null) {
                                        tempPlayer = PlayerManager.getInstance().loadPlayer(tempUserId, table.getPlayType());
                                        tempPlayer.setIsOnline(0);
                                        tempPlayer.setIsEntryTable(SharedConstants.table_offline);
                                        LogUtil.msgLog.info("gold user online?false:userId=" + tempUserId);
                                    }
                                    playerList.add(tempPlayer);
                                }
                                int c = table.getMaxPlayerCount() - playerList.size();
                                while (c > 0) {
                                    Player tempPlayer = ObjectUtil.newInstance(table.getPlayerClass());
                                    tempPlayer.setUserId(-c);
                                    tempPlayer.setFlatId(String.valueOf(tempPlayer.getUserId()));
                                    tempPlayer.setPf("robot");
                                    tempPlayer.setName(DataLoaderUtil.loadRandomRobotName());
                                    playerList.add(tempPlayer);
                                    c--;
                                }

                                //开始游戏
                                GoldRoomDao.getInstance().updateGoldRoom0(goldRoom.getKeyId(), goldRoom.getMaxCount(), "1");

                                //随机房主
                                Player master = playerList.remove(new SecureRandom().nextInt(playerList.size()));

                                JsonWrapper json = new JsonWrapper(goldRoom.getTableMsg());
                                BaseTable table1 = TableManager.getInstance().createSimpleTable(master.isRobot() ? master : playerList.get(playerList.size() - 1), StringUtil.explodeToIntList(json.getString("ints"), ",")
                                        , StringUtil.explodeToStringList(json.getString("strs"), ","), goldRoom, JjsUtil.loadMatch(goldRoom));

                                if (table1 != null) {
                                    table1.setMasterId(master.getUserId());
                                    table1.saveSimpleTable();
                                    playerList.add(0, master);

                                    // 牌桌进入准备阶段
                                    table1.changeTableState(table_state.ready);
                                    TableManager.getInstance().addTable(table1);

                                    master.sendActionLog(LogConstants.reason_createtable, "tableId:" + table1.getId());

                                    List<Player> tablePlayers = new ArrayList<>(playerList.size());
                                    for (Player player1 : playerList) {
                                        Player player2 = PlayerManager.getInstance().changePlayer(player1, table1.getPlayerClass());
//							if(player2.getUserId() == table1.getMasterId()) {
                                        table1.ready(player2);
//							}

                                        // 加入牌桌
                                        if (!table1.joinPlayer(player2)) {
                                            continue;
                                        } else {
                                            tablePlayers.add(player2);
                                        }
                                        player2.writeComMessage(WebSocketMsgType.GOLD_JOIN_SUCCESS);
                                    }

                                    Map<Long, GeneratedMessage> msgMap = new HashMap<>();
                                    //先发给自己
                                    for (Player player2 : tablePlayers) {
                                        JoinTableRes.Builder joinRes = JoinTableRes.newBuilder();
                                        joinRes.setPlayer(player2.buildPlayInTableInfo());
                                        //玩法
                                        joinRes.setWanfa(table1.getPlayType());

                                        GeneratedMessage msg = joinRes.build();
                                        msgMap.put(player2.getUserId(), msg);
                                        player2.writeSocket(msg);
                                    }

                                    //后发给其他人
                                    for (Player player2 : tablePlayers) {
                                        for (Map.Entry<Long, GeneratedMessage> kv : msgMap.entrySet()) {
                                            if (player2.getUserId() != kv.getKey().longValue()) {
                                                player2.writeSocket(kv.getValue());
                                            }
                                        }
                                    }

                                    //房主以外的其他玩家选座发送消息
                                    for (Player player2 : tablePlayers) {
                                        if (player2.getUserId() != master.getUserId()) {
                                            table1.sendPlayerStatusMsg();
                                        }
                                    }

                                    table1.checkDeal();
                                }
                            }
                        }
                    }
                }
            }

			//-----------------比赛房检查
			Iterator<BaseTable> iterator = competitionTableMap.values().iterator();
			while (iterator.hasNext()) {
				BaseTable table = iterator.next();
				if(table.isCompetitionRoom() && table.getCompetitionRoom()!=null){

					if(table.getState() == table_state.ready && table.getPlayedBureau() < table.getTotalBureau() && table.getMaxPlayerCount() <= table.getPlayerCount()){
						if (table.getCompetitionRoom().getLastCheckTime() == 0L) {
							table.getCompetitionRoom().setLastCheckTime(currentTime);
							continue;
						} else if (currentTime - table.getCompetitionRoom().getLastCheckTime() < CompetitionUtil.NO_FIRST_AUTO_READY_TIME_OUT) {
							continue;
						}

						//准备状态
						table.getSeatMap().values().forEach(table::autoReady);
					} else {
						table.getCompetitionRoom().setLastCheckTime(0);
					}

					//超过5分钟未开局的房间直接清理掉
					clearInvalidCompetitionRoom(table.getCompetitionRoom().getPlayingId(), false, false, null, null);
				}
			}

        } catch (Throwable e) {
            LogUtil.errorLog.error("Throwable:" + e.getMessage(), e);
        }
    }

    // 代开牌桌
    public void daikaiTable(Player player, ComReq req) {
        List<Integer> params = req.getParamsList();
        List<String> strParams = req.getStrParamsList();

        int playType = StringUtil.getIntValue(params, 1, 0);
        int allowGroupMember = 0;
        if (GameUtil.isPlayDn(playType)) {
            if (!SharedConstants.isKingOfBull()) {
                allowGroupMember = StringUtil.getIntValue(params, 18, 0);
            } else {
                allowGroupMember = StringUtil.getIntValue(params, 17, 0);
            }
        }
        if (allowGroupMember > 0 && player.getIsGroup() <= 0) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_52));
            return;
        }
        if (playType == GameUtil.play_type_3POK || playType == GameUtil.play_type_4POK) {//临时屏蔽四人打筒子代开
            return;
        }

        int bureauCount = StringUtil.getIntValue(params, 0, 10);// 局数
        if (!GameUtil.isPlayAhGame() && GameUtil.isPlayBopi(playType)) {
            bureauCount = 50;
        }
        int playerCount = StringUtil.getIntValue(params, 7, 0);// 比赛人数
        if (GameUtil.isPlayDzbp(playType)) {
            bureauCount = 50;
            playerCount = 2;
        }
        BaseTable table = TableManager.getInstance().getInstanceTable(playType);

        if (table == null) {
            player.writeErrMsg(LangMsg.code_239, playType);
            return;
        }

        int payType = 2;

        if (GameUtil.isPlayAhDdz(playType)) {
            if (playerCount == 0) {
                playerCount = 3;
            } else if (playerCount != 2 && playerCount != 3) {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_3));
                return;
            }
            payType = StringUtil.getIntValue(params, 9, 2);
            if (payType == 1) {
                payType = 2;
            }
        } else if (GameUtil.isPlayAhPdk(playType)) {
            if (playerCount == 0) {
                playerCount = 3;
            } else if (playerCount != 2 && playerCount != 3) {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_3));
                return;
            }
            payType = StringUtil.getIntValue(params, 10, 2);
        } else if (GameUtil.isPlayAhMj(playType)) {
            playerCount = StringUtil.getIntValue(params, 8, 4);// 人数

            if (playerCount < 2 || playerCount > 4) {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_3));
                return;
            }
            payType = StringUtil.getIntValue(params, 10, 2);
        } else if (GameUtil.isPlayAhPhz(playType)) {
            if (playerCount == 0) {
                playerCount = 3;
            }
            payType = StringUtil.getIntValue(params, 10, 2);
        } else if (GameUtil.isPlayAhCsMj(playType) || GameUtil.isPlayAhZzOrHzMj(playType)) {
            if (playerCount == 0) {
                playerCount = 4;
            }
            payType = StringUtil.getIntValue(params, 10, 2);
        } else if (GameUtil.isPlayDn(playType) || GameUtil.isPlayYjMj(playType)) {
            payType = StringUtil.getIntValue(params, 10, 2);
        } else if (GameUtil.isPlayTenthirty(playType) || GameUtil.isPlayPdk(playType) || GameUtil.isPlayYjPdk(playType)) {
            payType = StringUtil.getIntValue(params, 9, 2);
        } else if (GameUtil.isPlayDtz(playType)) {
            bureauCount = bureauCount > 0 ? bureauCount : 30;
            payType = StringUtil.getIntValue(params, 2, 2);
        } else if (GameUtil.isPlaySyPhz(playType)) {
            if (GameUtil.isPlayBopi(playType)) {
                bureauCount = 50;
            }
            payType = StringUtil.getIntValue(params, 9, 2);
        } else if (GameUtil.isPlayDzbp(playType)) {
            bureauCount = 50;
            payType = StringUtil.getIntValue(params, 9, 2);
        } else if (GameUtil.isPlayXtPhz(playType) || GameUtil.isPlaySmPhz(playType) || GameUtil.isPlayCdPhz(playType)) {
//            if (GameUtil.isPlayBopi(playType)) {
//                bureauCount = 50;
//            }
            payType = StringUtil.getIntValue(params, 9, 2);
        } else if (GameUtil.isPlayNxPhz(playType)) {
            payType = StringUtil.getIntValue(params, 9, 2);
        } else if (GameUtil.isPlayHsPhz(playType)) {
            payType = StringUtil.getIntValue(params, 9, 2);
        } else if (GameUtil.isPlayHbgzp(playType)) {
            payType = StringUtil.getIntValue(params, 9, 2);
        } else if (GameUtil.isPlayXxGhz(playType)) {
//                if (GameUtil.isPlayBopi(play)) {
//                    bureauCount = 50;
//                }
            payType = StringUtil.getIntValue(params, 9, 2);
        } else if (GameUtil.isPlayXxPhz(playType)) {
            payType = StringUtil.getIntValue(params, 9, 2);
        } else if (GameUtil.isPlayAHPhzNew(playType)) {
            payType = StringUtil.getIntValue(params, 9, 2);
        } else if (GameUtil.isPlaySp(playType) || GameUtil.isPlayBbtz(playType) || GameUtil.isPlayYjGhz(playType)
                || GameUtil.isNxghz(playType)|| GameUtil.isPlayXpLp(playType) || GameUtil.isPlayHGWPhz(playType) || GameUtil.isYiYangWhz(playType)) {

            payType = StringUtil.getIntValue(params, 2, 2);
        } else if (GameUtil.isPlayQianFen(playType)) {
            payType = StringUtil.getIntValue(params, 2, 2);
            bureauCount = 100;
        } else {
            payType = StringUtil.getIntValue(params, 10, 2);
        }
//        boolean isAAdaikai = false;
//        if(GameUtil.isPlayDn(playType) || GameUtil.isPlayPdk(playType) || GameUtil.isPlaySyPhz(playType) || GameUtil.isPlayDtz(playType)){
//        	isAAdaikai = true;
//        }
        int needCard;
        if (payType == 1) {
            needCard = 0;
        } else {
            if (GameUtil.isPlayDtz(playType)) {
                int score_max = StringUtil.getIntValue(params, 3, 600);
                bureauCount = bureauCount > 0 ? bureauCount : 30;
                needCard = PayConfigUtil.get(playType, bureauCount, table.calcPlayerCount(playerCount), 1, score_max);
            } else {
                needCard = PayConfigUtil.get(playType, bureauCount, table.calcPlayerCount(playerCount), 1);
            }
        }

        if (needCard < 0 || needCard > 0 && player.getFreeCards() + player.getCards() < needCard) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_diamond_err));
            return;
        }

        long userId = player.getUserId();
        String createPara = StringUtil.implode(params, ",");
        String createStrPara = StringUtil.implode(strParams, "#");

        long tableId;
        boolean existFlag;
        Server server = ServerManager.loadServer(playType, 1);
        int serverId = server != null ? server.getId() : GameServerConfig.SERVER_ID;
        do {
            tableId = generateId(userId, playType, 2, serverId);
            if (tableId <= 0) {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_0));
                return;
            }
            existFlag = TableDao.getInstance().checkTableIdExist(tableId);
        } while (existFlag);

        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("tableId", tableId);
        paramMap.put("daikaiId", userId);
        paramMap.put("serverId", serverId);
        paramMap.put("playType", playType);
        paramMap.put("needCard", needCard);
        paramMap.put("state", 0);
        paramMap.put("createFlag", 0);
        paramMap.put("createPara", createPara);
        paramMap.put("createStrPara", createStrPara);
        paramMap.put("createTime", null);
        paramMap.put("daikaiTime", new Date());
        paramMap.put("returnFlag", 0);
        paramMap.put("playerInfo", "");
        paramMap.put("extend", null);

        try {
            TableDao.getInstance().daikaiTable(paramMap);
        } catch (SQLException e) {
            LogUtil.e("daikaiTable err:", e);
            return;
        }

        if (needCard != 0) {
            player.changeCards(0, -needCard, true, playType, false, CardSourceType.daikaiTable_FZ);
        }

        LogUtil.msgLog.info("daikai msg={},payType={}", paramMap, payType);
        player.writeComMessage(WebSocketMsgType.res_com_code_daikaitable, 1, playType);
    }

    public BaseTable createBaiRenTable(Player player, List<Integer> params, List<String> strParams) throws Exception {
        int play = StringUtil.getIntValue(params, 0, 1);// 玩法
        if (params.size() < 1) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_3));
            return null;
        }
        BaseTable table = TableManager.getInstance().getInstanceTable(play);
        if (table == null) {
            player.writeErrMsg(LangMsg.code_239, play);
            return null;
        }
        player = PlayerManager.getInstance().changePlayer(player, table.getPlayerClass());
        if (player == null) {
            player.writeErrMsg(LangMsg.code_239, play);
            return null;
        }
        long tableId = new Long(play);
        if (!tableMap.containsKey(tableId)) {// 房间ID为2表示龙虎斗房间
            table.createBaiRenTable(player, play, params, strParams);
            table.joinPlayer(player);// 加入房间
            addTable(table);
        } else {// 已经有房间时
            table = tableMap.get(tableId);
            if (!table.getPlayerMap().containsKey(player.getUserId())) {
                table.joinPlayer(player);// 加入房间
            }
        }
        table.getPlayerMap().put(player.getUserId(), player);
        if (player.getPlayingTableId() != play) {
            player.setPlayingTableId(play);
            player.saveBaseInfo();
        }
        return table;
    }

    public BaseTable createSimpleTable(Player player, List<Integer> params, List<String> strParams, GoldRoom goldRoom, MatchBean matchBean) throws Exception {
        int bureauCount;
        String modeId;
        if (matchBean != null) {
            bureauCount = JjsUtil.loadMatchCurrentGameCount(matchBean);
            modeId = "match" + matchBean.getKeyId().toString();
        } else {
            modeId = goldRoom.getModeId();
            bureauCount = StringUtil.getIntValue(params, 0, 10);// 局数
        }
        int play = StringUtil.getIntValue(params, 1, 1);// 玩法
        if (params.size() <= 0) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_3));
            return null;
        }

        BaseTable table = TableManager.getInstance().getInstanceTable(play);
        if (table == null) {
            player.writeErrMsg(LangMsg.code_239, play);
            return null;
        }
        player = PlayerManager.getInstance().changePlayer(player, table.getPlayerClass());
        table.setDaikaiTableId(goldRoom.getKeyId());
        if ("2".equals(ResourcesConfigsUtil.loadServerPropertyValue("matchType", "1"))) {
            table.createTable(player, play, bureauCount, params, strParams);
            if (table.getId() <= 0) {
                return null;
            }
            if (table.isGoldRoom()) {
                try {
                    table.setModeId(goldRoom.getModeId());
                } catch (Exception e) {
                }
            }
            return table;
        } else {
            Long matchId = matchBean == null ? null : matchBean.getKeyId();
            table.setModeId(modeId);
            table.setMatchId(matchId);
            table.setMatchRatio(goldRoom.loadMatchRatio());
            table.changeExtend();
            if (table.createSimpleTable(player, play, bureauCount, params, strParams, false)) {
                table.setModeId(modeId);
                table.setMatchId(matchId);
                table.setMatchRatio(goldRoom.loadMatchRatio());
                table.changeExtend();
                player.writeSocket(table.buildCreateTableRes(player.getUserId()));
                return table;
            } else {
                return null;
            }
        }
    }

    public BaseTable createTable(Player player, List<Integer> params, List<String> strParams, long daikaiTableId, long creatorId, Map<String, Object> properties) throws Exception {
        return createTable(player, params, strParams, daikaiTableId, creatorId, true, properties);
    }

    public BaseTable createTable(Player player, List<Integer> params, List<String> strParams, long daikaiTableId, long creatorId, boolean checkPay, Map<String, Object> properties) throws Exception {
        return createTable(player, params, strParams, daikaiTableId, creatorId, checkPay, properties, null, null);
    }

    // 创建牌桌
    public BaseTable createTable(Player player, List<Integer> params, List<String> strParams, long daikaiTableId, long creatorId, boolean checkPay, Map<String, Object> properties, GroupTable groupTable, GroupTableConfig groupTableConfig) throws Exception {
        // 是比赛场
        if (player.isMatching()) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_240));
            return null;
        }

        BaseTable table = player.getPlayingTable();
        boolean autoCreate = properties != null && properties.containsKey("autoCreate"); // 智能补房
        boolean recreate = properties != null && properties.containsKey("recreate"); // 一次创建多个房间或智能补房，只是创建了GroupTable，实际未生成Table

        // 牌桌不为空
        if (table != null && !autoCreate) {
            // 重连牌桌
            reconnect(table, player);
            return null;
        }

        if (player.isPlayingMatch()) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_240));
            return null;
        }

        // 重置玩家俱乐部成员数据
        player.setGroupUser(null);
        player.setIsGroup(0);

        String groupId = strParams.size() > 0 && NumberUtils.isDigits(strParams.get(0)) && Long.parseLong(strParams.get(0)) > 0L ? strParams.get(0) : null;
        int tableCount = NumberUtils.toInt(strParams.size() > 1 ? strParams.get(1) : "1", 1);
        String tableVisible = strParams.size() > 2 ? strParams.get(2) : "1";//0私密1可见
        String groupTableConfigKeyId = strParams.size() > 3 ? strParams.get(3) : "";//创房模式ID
        boolean isPrivate = strParams.size() > 4 ? "1".equals(strParams.get(4)) : false; //私密房

        int play = StringUtil.getIntValue(params, 1, 1);// 玩法

        if (GameUtil.isPlayWzq(play)) { // 五子棋玩法特殊处理
            isPrivate = true;
            groupTableConfigKeyId = "0";
            tableCount = 1;
            checkPay = false;
        }

        //俱乐部包厢ID
        int groupRoom = 0;
        Long modeVal = null;
        tableCount = 1;
        int maxTableCount = tableCount;

        int pay0 = 0;
        GroupTableConfig gtc = null;
        GroupInfo groupInfo = null;
        // <<<<<   处理参数----------开始
        if (groupTableConfigKeyId.length() > 0 && NumberUtils.isDigits(groupTableConfigKeyId) && Integer.parseInt(groupTableConfigKeyId) > 0 && !recreate) {
            GroupTableConfig groupTableConfig0 = groupTableConfig != null ? groupTableConfig : GroupDao.getInstance().loadGroupTableConfig(Long.parseLong(groupTableConfigKeyId));
            if (groupTableConfig0 == null) {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_48));
                return null;
            }

            gtc = groupTableConfig0;
            groupId = gtc.getParentGroup().toString();
            groupInfo = GroupDao.getInstance().loadGroupInfo(gtc.getParentGroup());

            if(groupInfo == null){
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_48));
                return null;
            }
            if (groupTableConfig0.getParentGroup().longValue() == 0) {
                groupRoom = 0;
                if (!groupTableConfig0.getGroupId().toString().equals(groupId)) {
                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_48));
                    return null;
                }
            } else {
                groupRoom = groupTableConfig0.getGroupId().intValue();
                if (!groupTableConfig0.getParentGroup().toString().equals(groupId)) {
                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_48));
                    return null;
                }
            }

            modeVal = groupTableConfig0.getKeyId();

            JsonWrapper json;
            List<Integer> intsList = null;
            List<String> strsList = null;
            try {
                json = new JsonWrapper(groupTableConfig0.getModeMsg());
                intsList = GameConfigUtil.string2IntList(json.getString("ints"));
                strsList = GameConfigUtil.string2List(json.getString("strs"));
            } catch (Throwable th) {
            } finally {
                if ((intsList == null || intsList.size() == 0) && (strsList == null || strsList.size() == 0)) {
                    intsList = GameConfigUtil.string2IntList(groupTableConfig0.getModeMsg());
                    strsList = Collections.emptyList();
                }
            }
            params = intsList;
            if (!strsList.isEmpty()) {
                strParams = strsList;
            }
        }
        if (params.size() <= 0) {
            if (groupId != null) {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_241));
            } else {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_3));
            }
            return null;
        }
        // <<<<<   处理参数-----------结束-----

        play = StringUtil.getIntValue(params, 1, 1);
        int bureauCount = StringUtil.getIntValue(params, 0, 10);// 局数
        int playerCount = StringUtil.getIntValue(params, 7, 0);// 比赛人数

        if (isPrivate) {
            // 是否可以 创建亲密房间
            if(groupInfo == null) {
                groupInfo = GroupDao.getInstance().loadGroupInfo(groupId);
            }
            if (!GameUtil.isPlayWzq(play) && !canCreatePrivateRoom(groupInfo)) {
                player.writeErrMsg(LangMsg.code_73);
                return null;
            }
        }

        if (StringUtils.isNotBlank(groupId)) {
            // 亲友圈禁止开房
            if(groupInfo == null){
                groupInfo = GroupDao.getInstance().loadGroupInfo(groupId);
            }
            if (isStopCreateGroupRoom(groupInfo)) {
                player.writeErrMsg(LangMsg.code_67);
                return null;
            }
            if (GameUtil.isPlayWzq(play) && !isPrivate) {
                player.writeErrMsg(LangMsg.code_267);
                return null;
            }
            if (StringUtils.isNotBlank(groupId)) {
                if (player.getGroupUser() == null || !groupId.equals(player.getGroupUser().getGroupId().toString())) {
                    player.loadGroupUser(groupId);
                }
            }
        } else {
            if (GameUtil.isPlayWzq(play)) {
                player.writeErrMsg(LangMsg.code_268);
                return null;
            }
        }

        if(groupTable != null){
            groupId = groupTable.getGroupId().toString();
            if(groupInfo == null){
                groupInfo = GroupDao.getInstance().loadGroupInfo(groupTable.getGroupId());
            }
            if(gtc == null){
                groupTableConfigKeyId = groupTable.getConfigId().toString();
                gtc = GroupDao.getInstance().loadGroupTableConfig(groupTable.getConfigId());
            }
        }

        if (maxTableCount == 1 && !autoCreate) {
            // 强制玩法在指定服
            if ("1".equals(ResourcesConfigsUtil.loadServerPropertyValue("force_game_type"))) {
                if (!ServerManager.hasPlayType(GameServerConfig.SERVER_ID, play)) {
                    LogUtil.errorLog.info("PlayTypeNotFound|" + play);
                    player.writeErrMsg(LangMsg.code_251, play);
                    return null;
                }
            }
        }

        table = TableManager.getInstance().getInstanceTable(play);
        if (table == null) {
            player.writeErrMsg(LangMsg.code_239, play);
            return null;
        }

        boolean isGroupGoldRoom = false;
        if (groupInfo != null) {
            isGroupGoldRoom = groupInfo.getIsCredit() == GroupInfo.isCredit_gold;
        }

        table.setCheckPay(checkPay);
        table.setCreatorId(player.getUserId());

        // <<<<<   计算耗钻----------开始--------------------------------------------------------------------------------
        int needCard = 0;
        int payMark = -1;  // 1、AA支付，2、房主支付，3、群主支付，4、群主支付，群主当前不在本服或未登录
        Player payPlayer = null;
        int payType = 1;// 1：AA，2：房主，3：群主 4: 金币AA
        PayParam payParam = new PayParam();
        payParam.setPlayType(play);
        payParam.setBureauCount(bureauCount);
        payParam.setExt(null);
        payParam.setPlayerCount(playerCount);
        payParam.setPayType(payType);
        payParam.setNeedCards(-1);
        payParam.setCostType(-1);
        payParam.setCostValue(-1);

        // 处理各玩玩法的支付参数
        processPayParam(table, params, payParam);
        if (payParam.getNeedCards() == -1) {
            player.writeErrMsg(LangMsg.code_76, (play + "-" + payType));
            return null;
        }
        payType = payParam.getPayType();
        bureauCount = payParam.getBureauCount();
        playerCount = payParam.getPlayerCount();
        needCard = payParam.getNeedCards();

        // 如果玩家的钻石小于玩一局需要的钻石，则返回
        if (table.isCheckPay()) {
            if (StringUtils.isNotBlank(groupId)) {
                // 俱乐部游戏免费检测
                boolean groupFree = GameConfigUtil.freeGameOfGroup(play, groupId);
                if (groupFree) {
                    needCard = 0;
                    pay0 = 0;
                }
                if (groupInfo == null) {
                    groupInfo = GroupDao.getInstance().loadGroupInfo(Long.parseLong(groupId), 0);
                }
                if (groupInfo == null) {
                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_242));
                    return null;
                }
                GroupUser groupUser0 = player.loadGroupUser(groupId);
                if (groupUser0 == null) {
                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_62));
                    return null;
                } else if (GroupConstants.isForbidden(groupUser0)) {
                    // 玩家已被亲友圈禁止游戏
                    player.writeErrMsg(LangMsg.code_258);
                    return null;
                }
                // 开启群主支会后，不能再使用AA支付或房间主支付
                if (groupInfo.getExtMsg() != null && !recreate) {
                    if ((payType == PayConfigUtil.PayType_Client_AA || payType == PayConfigUtil.PayType_Client_TableMaster) && groupInfo.getExtMsg().contains("+p3")) {
                        player.writeErrMsg(LangHelp.getMsg(LangMsg.code_244));
                        return null;
                    }
                }

                if (payType == PayConfigUtil.PayType_Client_AA) {
                    // AA支付
                    if (checkNeedCards(player, needCard, play)) {
                        player.writeErrMsg(LangHelp.getMsg(LangMsg.code_diamond_err));
                        return null;
                    }
                    pay0 = needCard;
                    payMark = 1;
                } else if (payType == PayConfigUtil.PayType_Client_TableMaster && !recreate) {
                    // 房主支付，recreate之前已支付过
                    payMark = 2;
                    pay0 = needCard;
                    if (checkNeedCards(player, needCard, play)) {
                        player.writeErrMsg(LangHelp.getMsg(LangMsg.code_diamond_err));
                        return null;
                    }
                } else if (payType == PayConfigUtil.PayType_Client_GroupMaster && !recreate) {
                    // 群主支付，recreate之前已支付过
                    payMark = 3;

                    // 检查是否开启群主支付
                    if (groupInfo.getExtMsg() == null || !groupInfo.getExtMsg().contains("+p3")) {
                        player.writeErrMsg(LangHelp.getMsg(LangMsg.code_246));
                        return null;
                    }

                    pay0 = needCard;
                    GroupUser master = GroupDao.getInstance().loadGroupMaster(groupId);
                    if (master != null) {
                        if ("1".equals(ResourcesConfigsUtil.loadServerPropertyValue("group_table_more_wanfa"))) {
                            creatorId = player.getUserId();
                        } else {
                            creatorId = master.getUserId();
                        }
                        if (needCard < 0) {
                            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_50));
                            LogUtil.errorLog.error("code50|TableManager|1|" + needCard + "|" + play + "|" + tableCount + "|" + bureauCount);
                            return null;
                        }
                        Player player1 = PlayerManager.getInstance().getPlayer(master.getUserId());
                        if (player1 != null) {
                            if (needCard > 0 && player1.getCards() + player1.getFreeCards() < needCard) {
                                if (autoCreate) {
                                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_245));
                                } else {
                                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_50));
                                    LogUtil.errorLog.error("code50|TableManager|2|" + needCard + "|" + play + "|" + tableCount + "|" + bureauCount + "|" + player1.getUserId() + "|" + player1.getCards() + "|" + player1.getFreeCards());
                                }
                                return null;
                            } else {
                                payPlayer = player1;
                            }
                        } else {
                            RegInfo user = UserDao.getInstance().selectUserByUserId(master.getUserId());
                            if (user == null) {
                                if (autoCreate) {
                                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_245));
                                } else {
                                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_50));
                                    LogUtil.errorLog.error("code50|TableManager|3|" + needCard + "|" + play + "|" + tableCount + "|" + bureauCount + "|" + master.getUserId());
                                }
                                return null;
                            }
                            if (needCard > 0 && user.getCards() + user.getFreeCards() < needCard) {
                                if (autoCreate) {
                                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_245));
                                } else {
                                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_50));
                                    LogUtil.errorLog.error("code50|TableManager|4|" + needCard + "|" + play + "|" + tableCount + "|" + bureauCount + "|" + user.getUserId() + "|" + user.getCards() + "|" + user.getFreeCards());
                                }
                                return null;
                            } else {
                                Player player2 = ObjectUtil.newInstance(player.getClass());
                                player2.loadFromDB(user);
                                payPlayer = player2;
                                payMark = 4; // 群主当前不在本服或未登录
                            }
                        }
                    } else {
                        if (autoCreate) {
                            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_245));
                        } else {
                            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_50));
                            LogUtil.errorLog.error("code50|TableManager|5|" + needCard + "|" + play + "|" + tableCount + "|" + bureauCount);
                        }
                        return null;
                    }
                } else if (payType == PayConfigUtil.PayType_Client_AA_Gold && !recreate) {
                    if (player.loadAllGolds() < needCard) {
                        player.writeErrMsg(LangHelp.getMsg(LangMsg.code_diamond_err));
                        return null;
                    }
                }
            } else {
                if (needCard < 0 || needCard > 0 && (!GameConfigUtil.freeGame(play, player.getUserId())) && player.loadAllCards() < needCard) {
                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_diamond_err));
                    return null;
                }
            }
        }
        // <<<<<   计算耗钻----------结束--------------------------------------------------------------------------------


        if (!autoCreate) {
            // 智能补房，不强制
            player = PlayerManager.getInstance().changePlayer(player, table.getPlayerClass());
        }

        if (properties != null) {
            Object serverType = properties.get("serverType");
            Object serverKey = properties.get("serverKey");
            if (serverType != null) {
                table.setServerType((serverType instanceof Number) ? ((Number) serverType).intValue() : Integer.parseInt(String.valueOf(serverType)));
            }
            if (serverKey != null) {
                table.setServerKey(String.valueOf(serverKey));
            }
            table.changeExtend();
        }

        if (groupTableConfig != null) {
            table.setGroupTableConfig(groupTableConfig);
            table.changeExtend();
        }

        if (groupId != null) {
            table.setServerType(1);
            table.setServerKey("group" + groupId);
            table.changeExtend();
        }

        table.setGroupTable(groupTable);

        if (!recreate && groupId != null) {
            if ((player.getGroupUser() == null || !GroupConstants.isHuiZhang(player.getGroupUser().getUserRole()))) {
                Integer count = GroupDao.getInstance().loadMyGroupTableCount(groupId, player.getUserId());
                if (count != null && count.intValue() > 0 && !("1".equals(ResourcesConfigsUtil.loadServerPropertyValue("group_table_more_wanfa")))) {
                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_247));
                    return null;
                }
            } else if (GroupConstants.isForbidden(player.getGroupUser())) {
                player.writeErrMsg(LangMsg.code_258);
                return null;
            }

            Integer count = GroupDao.getInstance().loadGroupTableCount(groupId);
            if (count != null) {
                int tempCount = ResourcesConfigsUtil.getGroupTableCountLimit() - count.intValue();
                if (tempCount <= 0) {
                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_248));
                    return null;
                } else {
                    if (tableCount > tempCount) {
                        tableCount = tempCount;
                    }
                }
            }
        }

        // <<<<<   验证包间是否关闭或删除------开始------------------------------------------------------------------------

        if (groupTable != null) {
            gtc = GroupDao.getInstance().loadGroupTableConfig(groupTable.getConfigId());
            if (gtc != null) {
                GroupInfo g = GroupDao.getInstance().loadGroupInfo(gtc.getGroupId(), gtc.getParentGroup());
                if (g == null || "0".equals(g.getGroupState()) || "-1".equals(g.getGroupState())) {
                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_68));
                    return null;
                }
            }
        }
        if (groupId != null && !recreate) {
            if (groupTableConfigKeyId.length() > 0 && NumberUtils.isDigits(groupTableConfigKeyId) && Integer.parseInt(groupTableConfigKeyId) > 0 && !recreate) {
                gtc = groupTableConfig != null ? groupTableConfig : GroupDao.getInstance().loadGroupTableConfig(Long.parseLong(groupTableConfigKeyId));
                if (gtc != null) {
                    GroupInfo g = GroupDao.getInstance().loadGroupInfo(gtc.getGroupId(), gtc.getParentGroup());
                    if (g == null || "0".equals(g.getGroupState()) || "-1".equals(g.getGroupState())) {
                        player.writeErrMsg(LangHelp.getMsg(LangMsg.code_68));
                        return null;
                    }
                }
            }
        }
        if (gtc != null) {
            // 非自动补房和私密房先检查玩家信用分是否足够进入房间
            if (!autoCreate || isPrivate) {
                BaseTable bt = TableManager.getInstance().getInstanceTable(play);
                if (bt != null) {
                    GroupUser gu = GroupDao.getInstance().loadGroupUserForceMaster(player.getUserId(), String.valueOf(gtc.getParentGroup()));
                    if (gu != null) {
                        bt.initCreditMsg(gtc.getCreditMsg());
                        // 比赛分是否足够进房间
                        if (bt.getCreditMode() == 1 && gu.getCredit() < bt.getCreditJoinLimit()) {
                            player.writeErrMsg(LangMsg.code_64, MathUtil.formatCredit(bt.getCreditJoinLimit()));
                            return null;
                        }
                        if(!bt.checkGroupWarn(player,groupId)){
                            return null;
                        }
                    }
                }
            }
        }
        // <<<<<   验证包间是否关闭或删除------结束------------------------------------------------------------------------


        // <<<<<   扣钻------------开始----------------------------------------------------------------------------------
        List<Player> playerList = new ArrayList<>(table.getMaxPlayerCount());
        playerList.add(player);
        CardConsume consume = new CardConsume();
        if (!recreate) { // recreate状态下，不需要支付，因为创建groupTable时已经扣钻
            if (payMark != 1) {
                consume.setFreeCards(0);
                consume.setCards(-needCard);
                consume.setWrite(true);
                consume.setPlayType(play);
                consume.setRecord(false);
                consume.setSourceType(table.getCardSourceType(payType));
                if (payMark == 2) {
                    // -----房主支付---
                    needCard = pay0 * tableCount;
                    consume.setCards(-needCard);
                    if (!player.changeCards(consume)) {
                        player.writeErrMsg(LangHelp.getMsg(LangMsg.code_diamond_err));
                        return null;
                    }
                    table.setCreatorId(player.getUserId());
                } else if (payMark == 3 || payMark == 4) {
                    //-----群主支付----
                    needCard = pay0 * tableCount;
                    consume.setCards(-needCard);
                    if (!payPlayer.changeCards(consume)) {
                        LogUtil.errorLog.error("code50|TableManager|6|" + needCard + "|" + play + "|" + tableCount + "|" + bureauCount + "|" + payPlayer.getUserId());
                        return null;
                    }
                    table.setCreatorId(payPlayer.getUserId());
                }
                if (payMark == 4) {
                    // 群主当前不在本服或未登录
                    int c1 = 0, c2 = 0;
                    if (payPlayer.getFreeCards() > 0) {
                        if (payPlayer.getFreeCards() >= needCard) {
                            c1 = needCard;
                        } else {
                            c1 = (int) payPlayer.getFreeCards();
                        }
                    }
                    if (c1 < needCard) {
                        c2 = needCard - c1;
                    }
                    if (payPlayer.getEnterServer() > 0) {
                        ServerUtil.notifyPlayerCards(payPlayer.getEnterServer(), payPlayer.getUserId(), -c2, -c1);
                    }
                }
            }
        }
        // <<<<<   扣钻------------结束----------------------------------------------------------------------------------

        List<BaseTable> tableList = new ArrayList<>(tableCount);
        if (autoCreate || maxTableCount > 1) {
            // 智能补房或一次创建多个
            for (int i = 0; i < tableCount; i++) {
                try {
                    BaseTable baseTable = TableManager.getInstance().getInstanceTable(play);
                    Server server = ServerManager.loadServer(play, 1);
                    int serverId = server != null ? server.getId() : GameServerConfig.SERVER_ID;

                    long tableId = generateId(player.getUserId(), play, 1, serverId);

                    if (tableId > 0) {
                        baseTable.setId(tableId);
                        baseTable.setServerId(serverId);
                        tableList.add(baseTable);
                    }
                } catch (Throwable t) {
                    LogUtil.errorLog.error("Exception:" + t.getMessage(), t);
                }
            }
        } else {
            // 正常创房
            table.createTable(player, play, bureauCount, params, strParams);
            if (table.getId() <= 0) {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_0));
                return null;
            }
            table.setIntParams(params);
            table.setStrParams(strParams);
            tableList.add(table);
            if (groupTable != null) {
                int room = groupTable.loadGroupRoom();
                if (room > 0) {
                    table.setServerKey("group" + groupTable.getGroupId() + "_" + groupTable.getKeyId() + "_" + room);
                } else {
                    table.setServerKey("group" + groupTable.getGroupId() + "_" + groupTable.getKeyId());
                }
                table.changeExtend();
            }
        }

        if (StringUtils.isNotBlank(groupId)) {
            if (table.getAllowGroupMember() > 0) {
                if (player.getGroupUser() != null && groupId.equals(player.getGroupUser().getGroupId().toString())) {
                } else {
                    player.loadGroupUser(groupId);
                }
                table.setAllowGroupMember(Integer.parseInt(groupId));
                table.changeExtend();
            }
        }

        if (creatorId > 0) {
            table.setCreatorId(creatorId);
        }
        if (GameServerConfig.isDebug()) {
            if (ShutDownAction.testWanFa == play) {
                table.setZp(ShutDownAction.testPai);
            }
        }

        if (maxTableCount == 1) {
            StringBuilder sb = new StringBuilder("createTable");
            sb.append("|").append(table.getId());
            sb.append("|").append(table.getMasterId());
            sb.append("|").append(player.getUserId());
            sb.append("|").append(creatorId);
            LogUtil.msgLog.info(sb.toString());
        }

        if (groupTable != null) {
            if (!recreate) {
                table.setTableType(BaseTable.TABLE_TYPE_GROUP);
                table.setRoomName(groupTableConfig.getTableName());
                table.initGroupConfig(groupTable.getGroupId());
                if(isGroupGoldRoom) {
                    table.initGroupTableGoldMsg(groupTableConfig.getGoldMsg());
                }else{
                    table.initCreditMsg(groupTableConfig.getCreditMsg());
                }
                groupTable.setConfigId(groupTableConfig.getKeyId());
                groupTable.setCreatedTime(new Date());
                groupTable.setCurrentCount(0);
                groupTable.setCurrentState("0");
                groupTable.setGroupId(groupTableConfig.getParentGroup().longValue() == 0 ? groupTableConfig.getGroupId() : groupTableConfig.getParentGroup());
                groupTable.setTableName(groupTableConfig.getTableName());
                groupTable.setMaxCount(groupTableConfig.getPlayerCount());
                groupTable.setServerId(String.valueOf(GameServerConfig.SERVER_ID));
                groupTable.setTableId(Long.valueOf(table.getId()).intValue());
                groupTable.setTableMsg(groupTableConfig.getModeMsg());
                if(isGroupGoldRoom){
                    groupTable.setType(GroupTable.type_gold);
                    groupTable.setGoldMsg(groupTableConfig.getGoldMsg());
                    groupTable.setCreditMsg("");
                }else{
                    groupTable.setType(table.getCreditMode() == 1 ? GroupTable.type_credit : GroupTable.type_normal);
                    groupTable.setCreditMsg(groupTableConfig.getCreditMsg());
                    groupTable.setGoldMsg("");
                }
                groupTable.setPlayType(play);
                groupTable.setPayMsg(getPayMsg(consume));
                groupTable.setIsPrivate(isPrivate ? 1 : 0);
                groupTable.setGoldMsg(groupTableConfig.getGoldMsg());
                Long tableKeyId = GroupDao.getInstance().createGroupTable(groupTable);
                groupTable.setKeyId(tableKeyId);

                table.setGroupTable(groupTable);
                table.setServerKey(tableKeyId.toString());
                table.setGroupTableConfig(groupTableConfig);
                table.changeExtend();
            } else if (recreate) {
                table.setTableType(BaseTable.TABLE_TYPE_GROUP);
                if(isGroupGoldRoom){
                    table.initGroupTableGoldMsg(groupTable.getGoldMsg());
                }else{
                    table.initCreditMsg(groupTable.getCreditMsg());
                }
                table.setRoomName(groupTable.getTableName());
                table.initGroupConfig(groupTable.getGroupId());
                table.setGroupTable(groupTable);
                table.initGroupInfo();
                String tableMsg = groupTable.getTableMsg();
                if (StringUtils.isNotBlank(tableMsg)) {
                    JsonWrapper jsonWrapper = new JsonWrapper(tableMsg);
                    String strs = jsonWrapper.getString("strs");
                    if (StringUtils.isNotBlank(strs)) {
                        String[] tempStrs = strs.split(";")[0].split("_");
                        if (tempStrs.length >= 4) {
                            if ("2".equals(tempStrs[0]) || "3".equals(tempStrs[0])) {
                                table.setCreatorId(Long.valueOf(tempStrs[2]));
                            } else {
                                table.setCreatorId(NumberUtils.toLong(groupTable.getUserId(), 0));
                            }
                        }
                    }
                }
            }
        }

        if (maxTableCount == 1 && !autoCreate) {
            TableManager.getInstance().addTable(table);
        }

        if (groupId != null && !recreate) {
            List<GroupTable> gtList = new ArrayList<>(tableList.size());
            for (BaseTable baseTable : tableList) {
                GroupTable gt = new GroupTable();
                gt.setUserId(String.valueOf(creatorId > 0L ? creatorId : player.getUserId()));
                gt.setCurrentCount(0);
                gt.setConfigId(modeVal == null ? 0L : modeVal);
                gt.setCreatedTime(new Date());
                gt.setCurrentState("0");
                gt.setGroupId(NumberUtils.toLong(groupId));
                gt.setTableName(gtc != null ? gtc.getTableName() : "");
                gt.setMaxCount(baseTable.calcPlayerCount(playerCount));
                gt.setServerId(String.valueOf(baseTable.getServerId()));
                gt.setTableId((int) baseTable.getId());
                gt.setPlayedBureau(0);
                gt.setIsPrivate(isPrivate ? 1 : 0);

                JsonWrapper jsonWrapper = new JsonWrapper("");
                jsonWrapper.putString("ints", StringUtil.implode(params, ","));
                jsonWrapper.putString("strs", new StringBuilder().append(payType).append("_").append(player.getUserId()).append("_").append(payPlayer == null ? player.getUserId() : payPlayer.getUserId()).append("_").append(pay0).append(";").append(StringUtil.implode(strParams, ",")).toString());
                jsonWrapper.putString("props", tableVisible);
                jsonWrapper.putInt("type", baseTable.getPlayType());
                if (groupRoom > 0) {
                    jsonWrapper.putInt("room", groupRoom);
                }

                gt.setTableMsg(jsonWrapper.toString());
                if(isGroupGoldRoom){
                    baseTable.initGroupTableGoldMsg(gtc.getGoldMsg());
                    gt.setType(GroupTable.type_gold);
                    gt.setCreditMsg("");
                    gt.setGoldMsg(gtc.getGoldMsg());
                }else{
                    baseTable.initCreditMsg(gtc != null ? gtc.getCreditMsg():"");
                    gt.setType(baseTable.getCreditMode() == 1 ? GroupTable.type_credit : GroupTable.type_gold);
                    gt.setCreditMsg(gtc != null ? gtc.getCreditMsg():"");
                    gt.setGoldMsg("");
                }
                gt.setPlayType(play);
                gt.setPayMsg(getPayMsg(consume));
                Long groupKey = GroupDao.getInstance().createGroupTable(gt);
                gt.setKeyId(groupKey);
                gtList.add(gt);

                if (groupRoom > 0) {
                    baseTable.setServerKey("group" + groupId + "_" + groupKey + "_" + groupRoom);
                } else {
                    baseTable.setServerKey("group" + groupId + "_" + groupKey);
                }
                baseTable.setTableType(BaseTable.TABLE_TYPE_GROUP);
                baseTable.setGroupTable(gt);
                baseTable.setRoomName(gtc != null ? gtc.getTableName():"");
                baseTable.initGroupConfig(NumberUtils.toLong(groupId));
                baseTable.initGroupInfo();
                baseTable.changeExtend();

                LogUtil.msgLog.info("create group table:userId={},msg={}", player.getUserId(), JacksonUtil.writeValueAsString(gt));
            }

            if (Redis.isConnected() && gtList.size() > 0) {
                Map<String, Double> gtMap = new HashMap<>();
                Map<String, String> gtMap0 = new HashMap<>();
                for (GroupTable gt : gtList) {
                    if (gt.getKeyId() != null) {
                        gtMap.put(gt.getKeyId().toString(), Double.valueOf(GroupRoomUtil.loadWeight(gt.getCurrentState(), gt.getCurrentCount(), gt.getCreatedTime())));
                        gtMap0.put(gt.getKeyId().toString(), JSON.toJSONString(gt));
                    }
                }
                if (gtMap.size() > 0) {
                    RedisUtil.zadd(GroupRoomUtil.loadGroupKey(groupId, groupRoom), gtMap);
                    Map<String, String> groupRooms = RedisUtil.hgetAll(GroupRoomUtil.loadGroupKey(groupId, 0));
                    if (groupRooms == null) {
                        groupRooms = new HashMap<>();
                    }
                    groupRooms.put(groupRoom + "", "0");
                    //存储开了房的包厢id
                    RedisUtil.hmset(GroupRoomUtil.loadGroupTableKey(groupId, 0), groupRooms);
                    RedisUtil.hmset(GroupRoomUtil.loadGroupTableKey(groupId, groupRoom), gtMap0);
                }
            }
            if (maxTableCount > 1 || autoCreate) {
                return null;
            }
        }

        //设置语音房间ID
        long gotyeRoomId = GotyeChatManager.getInstance().loadGotyeRoomId(table.getId(), table.getPlayType(), player.getOs(), player.getVc());
        if (gotyeRoomId > 0) {
            table.setGotyeRoomId(gotyeRoomId);
            table.changeExtend();
        }

        // 牌桌进入准备阶段
        table.changeTableState(table_state.ready);

        // 是否启新金币系统
        if (table.isGroupRoom()) {
            if (groupInfo == null) {
                groupInfo = GroupDao.getInstance().loadGroupInfo(table.loadGroupId());
            }
            if (groupInfo != null) {
                table.setSwitchCoin(groupInfo.getSwitchCoin());
                table.setCreditRate(groupInfo.getCreditRate());
            }
        }

        for (int i = 0, len = playerList.size(); i < len; i++) {
            Player player1 = playerList.get(i);
            Player player2 = PlayerManager.getInstance().changePlayer(player1, table.getPlayerClass());
            if (player1 != player2) {
                playerList.set(i, player2);
            }
            if (!table.joinPlayer(player2)) {
                table.updateDaikaiTableInfo();
                LogUtil.errorLog.info("createTable joinPlayer fail:tableId={},userId={}", table.getId(), player2.getUserId());
                return null;
            }
        }

        // 房主创建房间自动准备
        if (table.isGroupRoom()) {
            if (playerList.size() == 1) {
                if (table.autoReadyForFirstPlayerOfGroup()) {
                    table.ready(playerList.get(0));
                } else if (GameUtil.isPlayWzq(play)) {
                    table.ready(playerList.get(0));
                }
            } else {
                if (!table.allowChooseSeat()) {
                    for (Player player1 : playerList) {
                        table.ready(player1);
                    }
                }
            }
        } else {
            if (playerList.size() == 1) {
                if (table.autoReadyForFirstPlayerOfCommon()) {
                    table.ready(playerList.get(0));
                }
            } else {
                if (!table.allowChooseSeat()) {
                    for (Player player1 : playerList) {
                        table.ready(player1);
                    }
                }
            }
        }

        table.updateDaikaiTableInfo();

        if (table.isGoldRoom()) {
            player.writeComMessage(WebSocketMsgType.GOLD_JOIN_SUCCESS);
        }

        for (Player player1 : playerList) {
            player1.writeSocket(table.buildCreateTableRes(player1.getUserId()));
        }

        for (Map.Entry<Long, Player> kv : table.getRoomPlayerMap().entrySet()) {
            long currentUserId = kv.getKey().longValue();
            if (currentUserId != player.getUserId() && !table.getPlayerMap().containsKey(kv.getKey()) && kv.getValue().getSeat() > 0) {
                TableRes.JoinTableRes.Builder joinRes = TableRes.JoinTableRes.newBuilder();
                TableRes.PlayerInTableRes.Builder builder = kv.getValue().buildPlayInTableInfo();
                if (builder == null) {
                    continue;
                }
                joinRes.setPlayer(builder);
                //玩法
                joinRes.setWanfa(table.getPlayType());
                GeneratedMessage msg1 = joinRes.build();
                for (Player tablePlayer : table.getSeatMap().values()) {
                    //如果是要加入的人，则推送创建房间消息,如果是其他人，推送加入房间消息
                    tablePlayer.writeSocket(msg1);
                }

                for (Player player0 : table.getRoomPlayerMap().values()) {
                    if (player0.getUserId() != currentUserId) {
                        if ((GameUtil.isPlayThreeMonkeys(table.getPlayType()) || GameUtil.isPlayTenthirty(table.getPlayType())) && player.getUserId() != player0.getUserId()) {
                            player0.writeSocket(table.buildCreateTableRes(player0.getUserId()));
                        } else {
                            player0.writeSocket(msg1);
                        }
                    }
                }
            }
        }

        player.sendActionLog(LogConstants.reason_createtable, "tableId:" + table.getId());

        if (table.isTest()) {
            // 加入牌桌
            int playerCountId = PlayerManager.robotId - table.getMaxPlayerCount() + 1;
            for (int i = PlayerManager.robotId - 1; i >= playerCountId; i--) {
                Player robot1 = PlayerManager.getInstance().getRobot(i, play);
                table.joinPlayer(robot1);
                table.ready(robot1);

                JoinTableRes.Builder joinRes = JoinTableRes.newBuilder();
                TableRes.PlayerInTableRes.Builder builder = robot1.buildPlayInTableInfo();
                if (builder == null) {
                    continue;
                }
                joinRes.setPlayer(builder);
                joinRes.setWanfa(table.getPlayType());
                player.writeSocket(joinRes.build());
                // ////////////////////////////////////////////////////////////
            }
            table.setRuning(true);
            // 检查所有人是否都准备完毕,如果准备完毕,改变牌桌状态并开始发牌
            table.ready();
            table.checkDeal();

        } else {
            boolean isRun = playerList.size() == table.getMaxPlayerCount();
            table.setRuning(isRun);
            if (isRun) {
                table.ready();
                table.checkDeal();
            }
        }
        table.sendPlayerStatusMsg();
        if (table.isKaiYiJu()) {
            AssisServlet.sendRoomStatus(table, "0");
        }
        GameUtil.autoCreateGroupTableNew(table);
        return table;
    }

    public static Class<? extends Player> getPlayerByCode(int code) {
        return msgPlayerTypes.get(Integer.valueOf(code));
    }

    public BaseTable reconnect(BaseTable table, Player player) {
		if (table.isGoldRoom()) {
			return reconnectGoldRoom(table, player);
		}
		else if (table.isCompetitionRoom()) {
			return reconnectCompetitionRoom(table, player);
		}
		else {
			return reconnectNormalRoom(table, player);
		}
	}

    public BaseTable reconnectNormalRoom(BaseTable table , Player player){
        synchronized (table) {
            if (table.getPlayerMap().isEmpty() && table.getRoomPlayerMap().isEmpty()) {
                LogUtil.msgLog.info("BaseTable|dissReason|reconnect|1|" + table.getId() + "|" + table.getPlayBureau() + "|" + player.getUserId());
                table.diss();
                LogUtil.msgLog.info("playingTableId0|6|" + player.getUserId() + "|" + player.getEnterServer() + "|" + player.getPlayingTableId());
                player.setPlayingTableId(0);
                player.saveBaseInfo();
                player.writeComMessage(WebSocketMsgType.res_code_err, LangHelp.getMsg(LangMsg.code_8, table.getId()), WebSocketMsgType.sc_code_err_table);
                return null;
            }
            if (!table.checkPlayer(player)) {
                LogUtil.e("check player err:" + player.getUserId());
                player.writeComMessage(WebSocketMsgType.res_code_err, LangHelp.getMsg(LangMsg.code_15, player.getPlayingTableId()), WebSocketMsgType.sc_code_err_table);
                return null;
            }

            table.ready();

            table.checkDeal();
            table.broadIsOnlineMsg(player, SharedConstants.table_online);

            if (GameUtil.isPlayBaiRenWanfa(table.getWanFa())) {
                BaiRenTableRes res = table.buildBaiRenTableRes(player.getUserId(), true, false);
                player.writeSocket(res);
            } else {
                CreateTableRes res = table.buildCreateTableRes(player.getUserId(), true, false);
                player.writeSocket(res);
            }
            LogUtil.msgLog.info("BaseTable|reconnect|" + table.getId() + "|" + table.getPlayBureau() + "|" + player.getUserId());
            table.sendZpLeftCards(player.getUserId());
            table.checkDiss();
            table.checkReconnect(player);
            table.checkSendDissMsg(player);
            table.sendPlayerStatusMsg();
            table.broadOnlineStateMsg();
            return null;
        }
    }

	public BaseTable reconnectGoldRoom(BaseTable table, Player player) {
        try {
            GoldRoomUser goldRoomUser = table.getGoldRoomUser(player.getUserId());
            if (goldRoomUser == null) {
                player.setPlayingTableId(0);
                player.saveBaseInfo();
            } else {
                GoldRoom goldRoom = table.getGoldRoom();
                if (goldRoom == null || goldRoom.isOver()) {
                    player.setPlayingTableId(0);
                    player.saveBaseInfo();
                } else if (goldRoom.isPlaying()) {
                    //游戏正在进行中
                    player.writeComMessage(WebSocketMsgType.GOLD_JOIN_SUCCESS);
                    player.writeSocket(table.buildCreateTableRes(player.getUserId(),true,false));
                    if (table.getState() == table_state.ready) {
                        table.checkDeal(player.getUserId());
                    }
                    return null;
                } else if (goldRoom.isNotStart() && goldRoom.canStart()) {
                    // 可开局
                    GoldRoomUtil.startGoldRoom(goldRoom);
                } else {
                    //等待开始游戏
                    player.writeComMessage(WebSocketMsgType.GOLD_JOIN_WAIT);
                    player.writeSocket(table.buildCreateTableRes(player.getUserId()));
                }
            }
        } catch (Exception e) {
            LogUtil.errorLog.error("reconnectGoldRoom|error", e);
        }
        return null;
    }

	public BaseTable reconnectCompetitionRoom(BaseTable table, Player player) {
        try {
            CompetitionRoomUser roomUser = table.getCompetitionRoomUser(player.getUserId());
            if (roomUser == null) {
                player.setPlayingTableId(0);
                player.saveBaseInfo();
            } else {
                CompetitionRoom room = table.getCompetitionRoom();
                if (room == null || room.isOver()) {
                    player.setPlayingTableId(0);
                    player.saveBaseInfo();
                } else if (room.isPlaying()) {
                    //游戏正在进行中
                    player.writeSocket(table.buildCreateTableRes(player.getUserId()));
                    if (table.getState() == table_state.ready) {
                        table.checkDeal(player.getUserId());
                    }
                    return null;
                } else if (room.isNotStart() && room.canStart()) {
                    // 可开局
//					CompetitionJoinTableCommand.startCompetitionRoom(room);
                    player.writeSocket(table.buildCreateTableRes(player.getUserId()));
                } else {
                    //等待开始游戏
                    player.writeSocket(table.buildCreateTableRes(player.getUserId()));
                }
            }
        } catch (Exception e) {
            LogUtil.errorLog.error("reconnectCompetitionRoom|error", e);
        }
        return null;
    }

    public void checkAutoQuit() {
        if (!SharedConstants.isAutoQuit()) {
//            return;
        }
        BaseTable[] tables = tableMap.values().toArray(new BaseTable[0]);
        int count = tables.length;
        if (count > 0) {
            long startTime = System.currentTimeMillis();
            CountDownLatch countDownLatch = new CountDownLatch(count);
            for (BaseTable table : tables) {
                executeAutoQuit(countDownLatch, table);
            }
            boolean isOk = false;
            try {
                isOk = countDownLatch.await(AUTO_TASK_TIMEOUT, TimeUnit.SECONDS);
            } catch (Throwable e) {
                LogUtil.errorLog.error("Exception:count=" + countDownLatch.getCount() + ",msg=" + e.getMessage(), e);
            } finally {
                if (System.currentTimeMillis() - startTime > 50) {
                    LogUtil.msgLog.info("checkAutoQuit|" + count + "|" + (System.currentTimeMillis() - startTime) + "|" + isOk);
                }
            }
        }
    }

    private static void executeAutoQuit(final CountDownLatch countDownLatch, final BaseTable table) {
        TaskExecutor.EXECUTOR_SERVICE.execute(new Runnable() {
            @Override
            public void run() {
                int referenceCount;
                if ((referenceCount = table.getReferenceCounter().get()) >= 1 || referenceCount < 0) {
                    countDownLatch.countDown();
                    LogUtil.errorLog.warn("table is dead ? tableId={},referenceCount={},players={}", table.getId(), referenceCount, table.getPlayerMap().keySet());
                } else {
                    table.getReferenceCounter().addAndGet(1);
                    try {
                        table.checkAutoQuit();
                    } catch (Throwable e) {
                        LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
                    }
                    table.getReferenceCounter().addAndGet(-1);
                    countDownLatch.countDown();
                }
            }
        });
    }

    /**
     * 俱乐部暂停开房
     *
     * @return
     */
    public static boolean isStopCreateGroupRoom(String groupId) {
        try {
            GroupInfo group = GroupDao.getInstance().loadGroupInfo(Long.valueOf(groupId));
            if (isStopCreateGroupRoom(group)) {
                return true;
            }
        } catch (Exception e) {
            LogUtil.errorLog.error("isStopCreateGroupRoom|error|" + e.getMessage(), e);
        }
        return false;
    }

    /**
     * 俱乐部暂停开房
     *
     * @return
     */
    public static boolean isStopCreateGroupRoom(GroupInfo group) {
        if (group != null && StringUtils.isNotBlank(group.getExtMsg())) {
            JSONObject json = JSONObject.parseObject(group.getExtMsg());
            String stopCreate = json.getString(GroupConstants.groupExtKey_stopCreate);
            if ("1".equals(stopCreate)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否可以创建俱乐部私密房间
     *
     * @return
     */
    public static boolean canCreatePrivateRoom(GroupInfo group) {
        if (group != null && StringUtils.isNotBlank(group.getExtMsg())) {
            JSONObject json = JSONObject.parseObject(group.getExtMsg());
            String stopCreate = json.getString(GroupConstants.groupExtKey_privateRoom);
            if ("1".equals(stopCreate)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 单人支付时，记录玩家付费信息
     *
     * @param consume
     * @return userId, cards, freeCards
     */
    public static String getPayMsg(CardConsume consume) {
        if (consume == null || consume.getPlayer() == null || !consume.isOK()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(consume.getPlayer().getUserId());
        sb.append(",").append(consume.getCards1());
        sb.append(",").append(consume.getFreeCards1());
        return sb.toString();
    }

    /**
     * 退钻
     *
     * @param table
     * @param player
     * @param gt
     * @return
     */
    public static boolean repay(BaseTable table, Player player, GroupTable gt) {
        if (gt == null) {
            return false;
        }
        return repay(table, player, gt.getGroupId(), gt.getTableId(), gt.getPayMsg());
    }

    /**
     * 退钻
     *
     * @param table
     * @param player
     * @param groupId
     * @param tableId
     * @param payMsg
     * @return
     */
    public static boolean repay(BaseTable table, Player player, long groupId, long tableId, String payMsg) {
        try {
            LogUtil.msgLog.info("TableManager|repay|1|" + groupId + "|" + tableId + "|" + payMsg);
            if (StringUtils.isBlank(payMsg)) {
                return false;
            }
            String[] splits = payMsg.split(",");
            long userId = Long.valueOf(splits[0]);
            long cards = -Long.valueOf(splits[1]);
            long freeCards = -Long.valueOf(splits[2]);
            if (cards == 0 && freeCards == 0) {
                return true;
            }
            Player payPlayer = PlayerManager.getInstance().getPlayer(userId);
            if (payPlayer != null) {
                payPlayer.changeCards(freeCards, cards, true, true, CardSourceType.groupTable_diss_FZ);
            } else {
                RegInfo user = UserDao.getInstance().selectUserByUserId(userId);
                if (user != null) {
                    if (table != null) {
                        payPlayer = ObjectUtil.newInstance(table.getPlayerClass());
                    } else if (player != null) {
                        payPlayer = ObjectUtil.newInstance(player.getClass());
                    } else {
                        payPlayer = new ProtoPlayer();
                    }
                    payPlayer.loadFromDB(user);
                    payPlayer.changeCards(freeCards, cards, true, CardSourceType.groupTable_diss_FZ);
                    if (user.getIsOnLine() == 1 && user.getEnterServer() > 0) {
                        ServerUtil.notifyPlayerCards(user.getEnterServer(), user.getUserId(), cards, freeCards);
                    }
                }
            }
//            MessageUtil.sendMessage(true, true, UserMessageEnum.TYPE0, userId, "军团房间【" + tableId + "】未开局被解散，获得钻石x" + (cards + freeCards), null);
            LogUtil.msgLog.info("TableManager|repay|2|" + groupId + "|" + tableId + "|" + payMsg + "|" + userId + "|" + cards + "|" + freeCards);
        } catch (Exception e) {
            LogUtil.errorLog.error("repay|error|" + tableId + "|" + groupId + "|" + e.getMessage(), e);
            return false;
        }
        return true;
    }


    /**
     * 由于各玩法创房参数不一样，所以需要单独处理
     *
     * @param table
     * @param params
     * @param payParam
     */
    public void processPayParam(BaseTable table, List<Integer> params, PayParam payParam) {

        int playType = payParam.getPlayType();
        int playerCount = payParam.getPlayerCount();
        int payType;
        int bureauCount = payParam.getBureauCount();
        int needCard = payParam.getNeedCards();
        Object ext = payParam.getExt();

        if (GameUtil.isPlayAhDdz(playType)) {
            if (playerCount == 0) {
                playerCount = 3;
            }
            payType = StringUtil.getIntValue(params, 9, 2);
            if (payType == 1) {
                payType = 2;
            }
        } else if (GameUtil.isPlayAhPdk(playType)) {
            if (playerCount == 0) {
                playerCount = 3;
            }
            payType = StringUtil.getIntValue(params, 10, 2);
        } else if (GameUtil.isPlayAhMj(playType)) {
            playerCount = StringUtil.getIntValue(params, 8, 4);// 人数
            payType = StringUtil.getIntValue(params, 10, 2);
        } else if (GameUtil.isPlayAhPhz(playType)) {
            payType = StringUtil.getIntValue(params, 10, 2);
            if (playerCount == 0) {
                playerCount = 3;
            }
        } else if (GameUtil.isPlayAhCsMj(playType) || GameUtil.isPlayAhZzOrHzMj(playType)) {
            payType = StringUtil.getIntValue(params, 10, 2);
            if (playerCount == 0) {
                playerCount = 4;
            }
        } else if (GameUtil.isPlayBSMj(playType) || GameUtil.isPlayDehMj(playType)) {
            playerCount = StringUtil.getIntValue(params, 3, 4);// 人数
            payType = StringUtil.getIntValue(params, 2, 1);
            if (playerCount == 0) {
                playerCount = 4;
            }
        } else if (GameUtil.isPlayDn(playType)) {
            payType = StringUtil.getIntValue(params, 10, 1);
        } else if (GameUtil.isPlayTenthirty(playType) || GameUtil.isPlayThreeMonkeys(playType)) {
            payType = StringUtil.getIntValue(params, 9, 1);
        } else if (GameUtil.isPlayDdz(playType)) {
            payType = StringUtil.getIntValue(params, 9, 1);
        } else if (GameUtil.isPlayGSMajiang(playType)) {
            payType = StringUtil.getIntValue(params, 10, 1);
        } else if (GameUtil.isPlayCCMajiang(playType)) {
            payType = StringUtil.getIntValue(params, 10, 2);
        } else if (GameUtil.isPlaySp(playType) || GameUtil.isPlayMajiang(playType) || GameUtil.isPlayBbtz(playType)) {
            payType = StringUtil.getIntValue(params, 2, 1);
        } else if (GameUtil.isQianFen(playType) || GameUtil.isPlayWzq(playType)) {
            payType = StringUtil.getIntValue(params, 2, 1);
        } else if (GameUtil.isPlayPdk(playType) || GameUtil.isPlayYzPdk(playType)) {
            payType = StringUtil.getIntValue(params, 9, 1);
        } else if (GameUtil.isPlaySyPhz(playType)) {
            if (GameUtil.isPlayBopi(playType)) {
                if (bureauCount != 1) { // 单局玩法不强制写50局
                    bureauCount = 50;
                }
            }
            payType = StringUtil.getIntValue(params, 9, 1);
        } else if (GameUtil.isPlayDzbp(playType)) {
            bureauCount = 50;
            payType = StringUtil.getIntValue(params, 9, 1);
        } else if (GameUtil.isPlayXtPhz(playType) || GameUtil.isPlaySmPhz(playType)
                || GameUtil.isPlayCdPhz(playType) || GameUtil.isPlayYzLCPhz(playType)
                || GameUtil.isPlayWcphz(playType)
        ) {
            payType = StringUtil.getIntValue(params, 9, 1);
        } else if (GameUtil.isPlayNxPhz(playType)) {
            payType = StringUtil.getIntValue(params, 9, 1);
        } else if (GameUtil.isPlayHsPhz(playType)) {
            payType = StringUtil.getIntValue(params, 9, 1);
        } else if (GameUtil.isPlayHbgzp(playType)) {
            payType = StringUtil.getIntValue(params, 2, 1);
        } else if (GameUtil.isPlayXxGhz(playType)) {
            payType = StringUtil.getIntValue(params, 9, 1);
        } else if (GameUtil.isPlayXxPhz(playType)) {
            payType = StringUtil.getIntValue(params, 9, 1);
        } else if (GameUtil.isPlayAHPhzNew(playType)) {
            payType = StringUtil.getIntValue(params, 9, 1);
        } else if (GameUtil.isLdfpf(playType) || GameUtil.isZzzp(playType) || GameUtil.isLyzp(playType)
                || GameUtil.isZhz(playType) || GameUtil.isHylhq(playType) || GameUtil.isHyshk(playType)
                || GameUtil.isGlphz(playType) || GameUtil.isLszp(playType) || GameUtil.isXx2710(playType)

        ) {
            payType = StringUtil.getIntValue(params, 9, 1);
        } else if (GameUtil.isYzwdmj(playType) || GameUtil.isPlayAhMajiang(playType) || GameUtil.isPlayCxMj(playType)
                || GameUtil.isPlayNxkwMj(playType) || GameUtil.isPlayTcpfMj(playType) || GameUtil.isPlayTcdpMj(playType)
                || GameUtil.isPlayNanxMj(playType) || GameUtil.isPlayCqxzMj(playType) || GameUtil.isPlayQzMj(playType)) {
            payType = StringUtil.getIntValue(params, 2, 1);
        } else if (GameUtil.isPlayZzMj(playType) || GameUtil.isPlayCsMj(playType) || GameUtil.isPlayHzMj(playType)
                || GameUtil.isPlaySyMj(playType) || GameUtil.isPlayTdhMj(playType) || GameUtil.isPlayYyWhz(playType)
                || GameUtil.isPlayLDSPhz(playType) || GameUtil.isPlayTjMj(playType) || GameUtil.isPlayChaosMj(playType)
                || GameUtil.isPlayNxMj(playType) || GameUtil.isPlayTcMj(playType) || GameUtil.isPlayDzMj(playType)
                || GameUtil.isPlayNyMj(playType) || GameUtil.isPlay265YyMj(playType) || GameUtil.isPlayZjMj(playType)
                || GameUtil.isPlayXpLp(playType)
        ) {
            payType = StringUtil.getIntValue(params, 2, 2);
        } else if (GameUtil.isPlaySandh(playType) || GameUtil.isPlayDaTuo(playType) || GameUtil.isPlayPengHuzi(playType)) {
            payType = StringUtil.getIntValue(params, 2, 2);
        } else if (GameUtil.isPlayNiuShiBie(playType) || GameUtil.isPlayXTBP(playType) || GameUtil.isPlay2renDdz(playType)
                || GameUtil.isPlayYYBS(playType) || GameUtil.isPlayTcgd(playType) || GameUtil.isPlayHsth(playType) || GameUtil.isPlay2renDdz(playType) || GameUtil.isPlayJingZhouMJ(playType)
                || GameUtil.isPlayCDTLJ(playType)) {
            payType = StringUtil.getIntValue(params, 2, 2);
        } else if (GameUtil.isPlayDtz(playType)) {
            payType = StringUtil.getIntValue(params, 2, 1);
            int score_max = StringUtil.getIntValue(params, 3, 600);
            bureauCount = bureauCount > 0 ? bureauCount : 30;
            ext = Integer.valueOf(score_max);
        } else if (GameUtil.isPlayYjMj(playType)) {
            payType = StringUtil.getIntValue(params, 10, 1);

        } else if (GameUtil.isPlayYjGhz(playType) || GameUtil.isNxghz(playType) || GameUtil.isPlayHGWPhz(playType) || GameUtil.isYiYangWhz(playType)) {

            payType = StringUtil.getIntValue(params, 2, 1);
        } else {
            payType = StringUtil.getIntValue(params, 10, 1);
            playerCount = table.calcPlayerCount(playerCount);
        }

        if (payType == PayConfigUtil.PayType_Client_AA) {
            needCard = PayConfigUtil.get(playType, bureauCount, playerCount, PayConfigUtil.PayType_Server_AA, ext);
        } else if (payType == PayConfigUtil.PayType_Client_TableMaster) {
            needCard = (!table.isCheckPay()) ? 0 : PayConfigUtil.get(playType, bureauCount, playerCount, PayConfigUtil.PayType_Server_TableMaster, ext);
        } else if (payType == PayConfigUtil.PayType_Client_GroupMaster) {
            needCard = (!table.isCheckPay()) ? 0 : PayConfigUtil.get(playType, bureauCount, playerCount, PayConfigUtil.PayType_Server_GroupMaster, ext);
        } else if (payType == PayConfigUtil.PayType_Client_AA_Gold) {
            needCard = (!table.isCheckPay()) ? 0 : PayConfigUtil.get(playType, bureauCount, playerCount, PayConfigUtil.PayType_Server_AA_Gold, ext);
        }

        payParam.setPayType(payType);
        payParam.setPlayerCount(playerCount);
        payParam.setBureauCount(bureauCount);
        payParam.setNeedCards(needCard);
        payParam.setExt(ext);
    }

    public boolean checkNeedCards(Player player, int needCard, int playType) {
        return (needCard < 0 || needCard > 0 && player.getFreeCards() + player.getCards() < needCard) && !GameConfigUtil.freeGame(playType, player.getUserId());
    }

    public BaseTable createGoldRoomTable(Player player, GoldRoomConfig config, GoldRoom goldRoom) throws Exception {
        BaseTable table = TableManager.getInstance().getInstanceTable(config.getPlayType());
        if (table == null) {
            player.writeErrMsg(LangMsg.code_239, config.getPlayType());
            return null;
        }

        CreateTableInfo createTableInfo = new CreateTableInfo(player);
        createTableInfo.setTableType(BaseTable.TABLE_TYPE_GOLD);
        createTableInfo.setPlayType(config.getPlayType());
        createTableInfo.setBureauCount(config.getTotalBureau());
        createTableInfo.setIntParams(StringUtil.explodeToIntList(config.getTableMsg()));
        createTableInfo.setStrParams(null);
        createTableInfo.setSaveDb(true);
        if (table.createTable(createTableInfo)) {
            table.setTotalBureau(1);
            table.setGoldRoomId(goldRoom.getKeyId());
            table.setGoldRoom(goldRoom);
            table.setModeId(config.getKeyId().toString());
            table.setRoomName(config.getName());
            table.setMatchRatio(goldRoom.loadMatchRatio());
            table.setIntParams(StringUtil.explodeToIntList(config.getTableMsg()));
            table.setStrParams(StringUtil.explodeToStringList(config.getGoldMsg(), ","));
            table.changeExtend();

            Map<String, Object> modify = new HashMap<>(8);
            modify.put("keyId", goldRoom.getKeyId());
            modify.put("tableId", table.getId());
            modify.put("currentState", GoldRoom.STATE_READY);
            GoldRoomDao.getInstance().updateGoldRoomByKeyId(modify);

            goldRoom.setTableId(table.getId());
            goldRoom.setCurrentState(GoldRoom.STATE_READY);
            addTable(table);
            return table;
        }
        return null;
    }

    public BaseTable createCompetitionRoomTable(Player player, CompetitionRoomConfig config, CompetitionRoom room) throws Exception {
        String modeId = config.getKeyId().toString();
        int playType = config.getPlayType(); // 玩法
        BaseTable table = TableManager.getInstance().getInstanceTable(playType);
        if (table == null) {
            player.writeErrMsg(LangMsg.code_239, playType);
            return null;
        }
//        player = PlayerManager.getInstance().changePlayer(player, table.getPlayerClass());

        CreateTableInfo createTableInfo = new CreateTableInfo(player);
        createTableInfo.setTableType(BaseTable.TABLE_TYPE_COMPETITION_PLAYING);
        createTableInfo.setPlayType(config.getPlayType());
        createTableInfo.setBureauCount(config.getTotalBureau());
        createTableInfo.setIntParams(StringUtil.explodeToIntList(config.getTableMsg()));
        createTableInfo.setStrParams(null);
        createTableInfo.setSaveDb(true);
        if (table.createTable(createTableInfo)) {
            table.setTotalBureau(config.getTotalBureau());
            table.setCompetitionRoomId(room.getKeyId());
            table.setCompetitionRoom(room);
            table.setModeId(modeId);
            table.setRoomName(room.getTableName()/*config.getName()*/);
            table.setMatchRatio(room.loadMatchRatio());
            table.setIntParams(StringUtil.explodeToIntList(config.getTableMsg()));
            table.setStrParams(StringUtil.explodeToStringList(config.getRatioMsg(), ","));
            table.changeExtend();

            Map<String, Object> modify = new HashMap<>(8);
            modify.put("keyId", room.getKeyId());
            modify.put("tableId", table.getId());
            modify.put("currentState", CompetitionRoom.STATE_READY);
            CompetitionDao.getInstance().updateCompetitionRoomByKeyId(modify);

            room.setTableId(table.getId());
            room.setCurrentState(CompetitionRoom.STATE_READY);
            addTable(table);
            return table;
        }
        return null;
    }


    public BaseTable createSoloRoom(Player player, List<String> strParams) throws Exception {

        int strSize = strParams.size();
        if (strSize < 2) {
            player.writeErrMsg(LangMsg.code_3);
            return null;
        }
        Long configId = Long.valueOf(strParams.get(0));
        Long soloRoomValue = Long.valueOf(strParams.get(1));
        SoloRoomConfig config = SoloRoomUtil.getSoloRoomConfig(configId);
        if (config == null) {
            player.writeErrMsg("挑战：该玩法不存在或已关闭");
            return null;
        }
        Server server = ServerManager.loadServer(GameServerConfig.SERVER_ID);
        if (!ServerManager.isValid(server, Server.SERVER_TYPE_GOLD_ROOM, config.getPlayType())) {
            player.writeErrMsg("挑战：玩法配置错误，请联系系统管理员");
            return null;
        }

        if (soloRoomValue < 10000) {
            player.writeErrMsg("挑战：设置的值错误，应该大于10000");
            return null;
        }
        int playType = config.getPlayType(); // 玩法
        BaseTable table = TableManager.getInstance().getInstanceTable(playType);
        if (table == null) {
            player.writeErrMsg(LangMsg.code_239, playType);
            return null;
        }

        CreateTableInfo createTableInfo = new CreateTableInfo(player);
        createTableInfo.setTableType(BaseTable.TABLE_TYPE_SOLO);
        createTableInfo.setPlayType(config.getPlayType());
        createTableInfo.setBureauCount(config.getTotalBureau());
        createTableInfo.setIntParams(StringUtil.explodeToIntList(config.getTableMsg()));
        createTableInfo.setStrParams(strParams);
        createTableInfo.setSaveDb(true);

        if (table.createTable(createTableInfo)) {
            table.setTotalBureau(1);
            table.setSoloRoomType(config.getSoloType());
            table.setSoloRoomValue(soloRoomValue);
            table.setRoomName(config.getName());
            table.setIntParams(StringUtil.explodeToIntList(config.getTableMsg()));
            table.setStrParams(strParams);
            table.changeExtend();
            table.changeTableState(table_state.ready);
            addTable(table);
            player = PlayerManager.getInstance().changePlayer(player, table.getPlayerClass());
            if (table.joinPlayer(player)) {
                player.setPlayingTableId(table.getId());
                player.setEnterServer(table.getServerId());
                player.saveBaseInfo();
                player.writeSocket(table.buildCreateTableRes(player.getUserId()));
                return table;
            } else {
                StringBuilder sb = new StringBuilder("SoloRoom|joinPlayerFail");
                sb.append("|").append(player.getUserId());
                sb.append("|").append(table.getId());
                sb.append("|").append(JSON.toJSONString(config));
                LogUtil.errorLog.info(sb.toString());
                table.diss();
            }
        }
        return null;
    }

	/**
     *@description 检查比赛场的无效房间
     *@param playingId
     *@param onlyClearInvalid 仅清理无效的房间
     *@param force 强制解散不结算
     *@return
     *@author Guang.OuYang
     *@date 2020/6/10
     */
	public void clearInvalidCompetitionRoom(long playingId, boolean onlyClearInvalid, boolean force, String curStep, String curRound) {
        ArrayList<BaseTable> baseTables = new ArrayList<>(tableMap.values());
        baseTables.addAll(competitionTableMap.values());
        Iterator<BaseTable> iterator = baseTables.iterator();
		while (iterator.hasNext()) {
			BaseTable next = iterator.next();
			if(!next.isCompetitionRoom())continue;
			//单个赛事检查
			if (playingId != -1 && next.getCompetitionRoom() != null && next.getCompetitionRoom().getPlayingId() != playingId) continue;
            if (!StringUtil.isBlank(curStep) && !StringUtil.isBlank(curRound) && next.getCompetitionRoom() != null
                    && (!next.getCompetitionRoom().getCurStep().equals(Integer.valueOf(curStep)) || !next.getCompetitionRoom().getCurRound().equals(Integer.valueOf(curRound)))) continue;
			//未开局的房间超过n分钟
            if (next.getCompetitionRoom() == null
                    || ((next.getCompetitionRoom().getCurrentState() < CompetitionRoom.STATE_PLAYING || !next.isPlaying()) && ((System.currentTimeMillis() - next.getCompetitionRoom().getCreatedTime().getTime()) >= 120 * 60 * 1000))
					//强制解除
					|| force || onlyClearInvalid) {

				LogUtil.msgLog.info("clearInvalidCompetitionRoom|success|{}|{}|{}|{}|{}", next.getCompetitionRoomUserMap().keySet().stream().map(v -> String.valueOf(v)).collect(Collectors.joining(",")), playingId, onlyClearInvalid, force, next.getCompetitionRoom());

				//强制解除的房间不做结算
				if(force) {
					next.setCompetitionRoom(null);
				}

				next.diss();

				try {
					competitionRoomMap.remove(next.getCompetitionRoomId());
                    competitionTableMap.remove(next.getId());
					CompetitionDao.getInstance().deleteCompetitionRoomByKeyId(next.getCompetitionRoomId());
					CompetitionDao.getInstance().deleteCompetitionRoomUserByRoomId(next.getCompetitionRoomId());
				} catch (Exception e) {
					e.printStackTrace();
				}

				//在线的玩家移出至大厅
				if(next.getCompetitionRoomUserMap() != null){
					Iterator<Long> iterator1 = next.getCompetitionRoomUserMap().keySet().iterator();
					while (iterator1.hasNext()) {
						Long userId = iterator1.next();
						Player player = PlayerManager.getInstance().getPlayer(userId);
						if(player == null)continue;
						try {
							reconnectCompetitionRoom(next, player);
							player.writeSocket(SendMsgUtil.buildComRes(WebSocketMsgType.res_code_tablequit, String.valueOf(player.getUserId()), String.valueOf(0), 0, 1, 0, 0, 0).build());
						}catch (Exception e) {
						}
					}
				}
			}
		}

	}

    public BaseTable createGoldRoomTableForMatch(Player player, GoldRoomConfig config, GoldRoom goldRoom) throws Exception {
        String modeId = config.getKeyId().toString();
        int playType = config.getPlayType(); // 玩法
        BaseTable table = TableManager.getInstance().getInstanceTable(playType);
        if (table == null) {
            return null;
        }

        CreateTableInfo createTableInfo = new CreateTableInfo(player);
        createTableInfo.setTableType(BaseTable.TABLE_TYPE_GOLD);
        createTableInfo.setPlayType(config.getPlayType());
        createTableInfo.setBureauCount(config.getTotalBureau());
        createTableInfo.setIntParams(StringUtil.explodeToIntList(config.getTableMsg()));
        createTableInfo.setStrParams(null);
        createTableInfo.setSaveDb(true);
        if (table.createTable(createTableInfo)) {
            table.setTotalBureau(1);
            table.setGoldRoomId(goldRoom.getKeyId());
            table.setGoldRoom(goldRoom);
            table.setModeId(modeId);
            table.setRoomName(config.getName());
            table.setMatchRatio(goldRoom.loadMatchRatio());
            table.setIntParams(StringUtil.explodeToIntList(config.getTableMsg()));
            table.setStrParams(StringUtil.explodeToStringList(config.getGoldMsg(), ","));
            table.changeExtend();

            Map<String, Object> modify = new HashMap<>(8);
            modify.put("keyId", goldRoom.getKeyId());
            modify.put("tableId", table.getId());
            modify.put("currentState", GoldRoom.STATE_READY);
            GoldRoomDao.getInstance().updateGoldRoomByKeyId(modify);

            goldRoom.setTableId(table.getId());
            goldRoom.setCurrentState(GoldRoom.STATE_READY);
            addTable(table);
            return table;
        }
        return null;
    }

}
