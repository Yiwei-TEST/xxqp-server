package com.sy599.game.manager;

import com.sy599.game.character.CommonPlayer;
import com.sy599.game.character.Player;
import com.sy599.game.common.executor.TaskExecutor;
import com.sy599.game.db.bean.*;
import com.sy599.game.db.bean.activityRecord.UserActivityRecord;
import com.sy599.game.db.dao.*;
import com.sy599.game.db.dao.LogDao;
import com.sy599.game.db.dao.UserCardRecordDao;
import com.sy599.game.db.dao.UserCoinRecordDao;
import com.sy599.game.db.dao.UserDao;
import com.sy599.game.db.dao.UserGoldRecordDao;
import com.sy599.game.db.dao.gold.GoldDao;
import com.sy599.game.qipai.AbstractBaseCommandProcessor;
import com.sy599.game.staticdata.bean.ActivityConfig;
import com.sy599.game.util.*;
import com.sy599.game.websocket.MyWebSocket;
import com.sy599.game.websocket.WebSocketManager;
import com.sy599.game.websocket.netty.WebSocketServerHandler;
import com.sy599.game.websocket.netty.coder.MessageUnit;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class PlayerManager {
	public static final Map<Integer, Class<? extends Player>> wanfaPlayerTypes = new HashMap<>();
	public static final Map<Class<? extends Player>, AbstractBaseCommandProcessor> commandTypes = new HashMap<>();
	public static final Map<AbstractBaseCommandProcessor, Class<? extends Player>> playerTypes = new HashMap<>();
//	public static final Map<Integer,Map<Integer,Class<?>>>
	private static final PlayerManager _inst = new PlayerManager();
//	public static final Map<Long, Player> playerMap = new ConcurrentHashMap<>();
	public static  Map<Long, Player> playerMap = new ConcurrentHashMap<>();
	public static final Map<String, Player> fPlayerMap = new ConcurrentHashMap<>();
	private AtomicInteger consumeFreeCards = new AtomicInteger(0);
	private AtomicInteger consumeCards = new AtomicInteger(0);
    private AtomicInteger consumeFreeGold = new AtomicInteger(0);
    private AtomicInteger consumeGold = new AtomicInteger(0);
	private static final Map<Integer, Integer> playTypeMap = new ConcurrentHashMap();
    private static final Map<Integer, Integer> playTypeGoldMap = new ConcurrentHashMap();
	private static final List<Integer> playTypeList = new ArrayList<>();
	public static int robotId = -100;
	public static volatile Date syncTime;
	/**
	 * 玩家房卡(钻石)消耗/获得日志记录
	 */
	private static final List<UserCardRecordInfo> userCardRecords = new ArrayList<>();
	private static final Map<Long,MissionAbout> userMission = new ConcurrentHashMap<>();
	private static AtomicInteger num=new AtomicInteger(0);

    /**
     * 玩家房卡(钻石)消耗/获得日志记录
     */
    private static final List<UserGoldRecord> userGoldRecords = new ArrayList<>();

    /**
     * 玩家金币消耗/获得日志记录
     */
    private static final List<Map<String,Object>> userCoinRecord = new ArrayList<>();
	/**
	 * player对象可能在创房和解散中变更，导致sign对象丢失，在此处保存
	 */
	private static final Map<Long, SevenGoldSign> userSign = new ConcurrentHashMap();

	public static boolean wanfaPlayerTypesPut(Integer gameType,Class<? extends Player> playerClass,AbstractBaseCommandProcessor processor){
        if (wanfaPlayerTypes.containsKey(gameType)) {
            StringBuilder sb = new StringBuilder("PlayerManager|wanfaPlayerTypesPut|error|hasSameWanFa");
            sb.append("|").append(gameType);
            sb.append("|").append(playerClass.getName());
            sb.append("|").append(processor.getClass().getName());
            sb.append("|").append(wanfaPlayerTypes.get(gameType).getName());
            throw new RuntimeException(sb.toString());
        }
		if (GameConfigUtil.hasGame(gameType)){
			wanfaPlayerTypes.put(gameType,playerClass);
			playTypeList.add(gameType);
			commandTypes.put(playerClass,processor);
			playerTypes.put(processor,playerClass);
			return true;
		}
		return false;
	}

	private PlayerManager() {
	}

	public void loadFromDB() {
	}

	/**
	 * 获取player信息，但是不放入集合中
	 * @param userId
	 * @param playType
	 * @return
	 */
	public Player loadPlayer0(long userId, int playType) {
		Player player = playType>0?PlayerManager.getInstance().getInstancePlayer(playType):new CommonPlayer();
		RegInfo info = UserDao.getInstance().selectUserByUserId(userId);
		if (info == null) {
			return null;
		}
		player.loadFromDB(info);
		return player;
	}

	public Player loadPlayer(long userId, int playType) {
		Player player = PlayerManager.getInstance().getInstancePlayer(playType);
		RegInfo info = UserDao.getInstance().selectUserByUserId(userId);
		if (info == null) {
			return null;
		}
		player.loadFromDB(info);
		Player addPlayer = addPlayer(player);
		if (addPlayer!=player){
			addPlayer.loadFromDB(info);
		}
		LogUtil.monitor_i("loadPlayer-->" + userId + " playType:" + playType);
		return addPlayer;
	}

	public Class<? extends Player> getPlayer(AbstractBaseCommandProcessor processor) {
		return playerTypes.get(processor);
	}

	public Player getInstancePlayer(int playType) {
		Class<? extends Player> cls = wanfaPlayerTypes.get(playType);
		Player player = null;
		if (cls == null){
			LogUtil.errorLog.error("getInstancePlayer error:player is not exists:playType="+playType);
		}else{
			try {
				player = ObjectUtil.newInstance(cls);
			}catch (Exception e){
				LogUtil.errorLog.error("getInstancePlayer err:"+e.getMessage(), e);
			}
		}
		
		if(player == null){
			player = new CommonPlayer();
		}
		return player;
	}
	
	public void process(Player player, MessageUnit message) {
		AbstractBaseCommandProcessor prossce = commandTypes.get(player.getClass());
		if (prossce != null) {

			if(LogUtil.msgLog.isDebugEnabled())
				System.err.println(String.format("Request After TableCmd... UserId:[%s] Type:[%s -> %s -> %s],CheckCode:[%s],Length:[%s],ByteLength:[%s] ", (player!=null?player.getUserId():-1), message.getMsgType(), prossce.getClass(), message.getMessage()!=null?message.getMessage().getClass().getSimpleName():"NULL", message.getCheckCode(),message.getLength(),message.getContent().length));

			if (Commands.contains(prossce.getClass(),message.getMsgType())){
				prossce.process(player, message);
			}else{
				LogUtil.errorLog.warn("Unknown msgType,CommandProcessor:name={},msgType={}",prossce.getClass().getName(),message.getMsgType());
			}
		}
	}

	public static PlayerManager getInstance() {
		return _inst;
	}

	public Player getPlayer(Long userId) {
		return playerMap.get(userId);
	}

	public int getPlayerCount() {
		return playerMap.size();
	}
	
	public void changeConsume(Player player, int cards, int freeCards, int playType) {
		if (cards < 0) {
			consumeCards.addAndGet(cards);
		}
		if (freeCards < 0) {
			consumeFreeCards.addAndGet(freeCards);
		}

		int totalCards = cards + freeCards;
		if (totalCards < 0) {
			synchronized (this) {
				Integer temp = playTypeMap.get(playType);
				playTypeMap.put(playType, temp == null ? totalCards : (temp.intValue() + totalCards));
			}

			if (!player.isRobot())
				LogDao.getInstance().insertCardsConsumeCards(player.getUserId(), player.getRegBindId(), player.getUsedCards(), 0, player.getReginTime());
			if(ActivityConfig.isActivityOpen(ActivityConfig.activity_comsume_diam, playType)) {// 开房送钻活动 累计消耗钻石统计
				UserActivityRecord record = player.getMyActivity().getUserActivityRecord();
				record.alterComsumeDiam(-totalCards);
				player.getMyActivity().updateActivityRecord(record);
			}
		}
	}

	public  Map<Integer, Integer> loadCurrentDbMap(Map<Integer, Integer> map){
		// copy 一份map
		Map<Integer, Integer> tempMap = new HashMap<>();
		synchronized(this){
			Iterator<Entry<Integer, Integer>> it = map.entrySet().iterator();
			while (it.hasNext()){
				Entry<Integer, Integer> kv=it.next();
				tempMap.put(kv.getKey(),kv.getValue());
				it.remove();
			}
		}
		return tempMap;
	}

	public void saveConsumeDatas(){
		try {
			Map<Integer, Integer> copy = getPlayTypeConsumeMap(1);
			if (consumeCards.get() != 0 || consumeFreeCards.get() != 0 || copy.size()>0) {
				int logDate = TimeUtil.getIntSimpleDay();
				int copy_consumeCards = consumeCards.getAndSet(0);
				int copy_consumeFreeCards = consumeFreeCards.getAndSet(0);
				LogDao.getInstance().updateConsumeCards(logDate, copy_consumeCards, copy_consumeFreeCards, copy);
			}
		}catch (Throwable t){
			LogUtil.errorLog.error("Throwable:"+t.getMessage(),t);
		}

		try {
			Map<Integer, Integer> copy = getPlayTypeConsumeMap(2);
			if (consumeGold.get() != 0 || consumeFreeGold.get() != 0 || copy.size()>0) {
				int logDate = TimeUtil.getIntSimpleDay();
				int copy_consumeGold = consumeGold.getAndSet(0);
				int copy_consumeFreeGold = consumeFreeGold.getAndSet(0);
				GoldDao.getInstance().updateConsumeGold(logDate, copy_consumeGold, copy_consumeFreeGold, copy);
			}
		}catch (Throwable t){
			LogUtil.errorLog.error("Throwable:"+t.getMessage(),t);
		}
	}

	public void saveDB(boolean asyn) {
		syncTime = TimeUtil.now();
		int count = playerMap.size();
		if (count > 0){
			if (asyn){
				TaskExecutor.SINGLE_EXECUTOR_SERVICE_USER.execute(new Runnable() {
					@Override
					public void run() {
						List<Map<String,Object>> list = saveDB0(true);
						if (list!=null&&list.size()>0){
							UserDao.getInstance().batchUpdate(list);
						}
						insertUserCards();
                        insertUserCoin();
                        updateUserMission();
						num.incrementAndGet();
                        batchInsertUserGoldRecord();
					}
				});
			}else{
				List<Map<String,Object>> list = saveDB0(asyn);
				if (list!=null&&list.size()>0){
					UserDao.getInstance().batchUpdate(list);
				}
				insertUserCards();
                insertUserCoin();
				updateUserMission();
                batchInsertUserGoldRecord();
			}
		}
	}

	private final static List<Map<String,Object>> saveDB0(boolean asyn){
		int count = playerMap.size();
		if (count>0){
			List<Map<String,Object>> list = new ArrayList<>(count);
			if (WebSocketServerHandler.isOpen){
				for (Map.Entry<Long,Player> kv : playerMap.entrySet()) {
					Map<String,Object> map = kv.getValue().saveDB(asyn);
					if (map != null && map.size() > 0){
						list.add(map);
					}
				}
			}else{
				for (Map.Entry<Long,Player> kv : playerMap.entrySet()) {
					Player player = kv.getValue();
					player.setLogoutTime(TimeUtil.now());
					player.setIsOnline(0);
					Map<String,Object> map = player.saveDB(asyn);
					if (map != null && map.size() > 0){
						list.add(map);
					}
				}
			}
			return list;
		}
		return null;
	}

    private Map<Integer, Integer> getPlayTypeConsumeMap(int mode) {
        Map<Integer, Integer> copy;
	    if (mode == 1) {
            copy = loadCurrentDbMap(playTypeMap);
        } else if (mode == 2){
            copy = loadCurrentDbMap(playTypeGoldMap);
        } else {
            return new HashMap<>();
        }
        return copy;
    }


    public boolean checkPlayer(int playType, Player player) {
		Class<? extends Player> cls = wanfaPlayerTypes.get(playType);
		return cls!=null&&cls==player.getClass();
	}

	public Player getRobot(long robotId, int playType) {
		Player robot = getInstancePlayer(playType);
		robot.setUserId(robotId);
		robot.setFlatId(String.valueOf(robotId));
		robot.setPf("robot");
		robot.setName(DataLoaderUtil.loadRandomRobotName());
		return robot;
	}

	public Player getPlayer(String flatId,String pf) {
		return fPlayerMap.get(flatId+pf);
	}

	public Player addPlayer(Player player) {
		return addPlayer(player,false);
	}

    public Player addPlayer(Player player, boolean force) {
        if (force) {
            playerMap.put(player.getUserId(), player);
            fPlayerMap.put(new StringBuilder(64).append(player.getFlatId()).append(player.getPf()).toString(), player);
            return player;
        } else {
            Player player1 = playerMap.putIfAbsent(player.getUserId(), player);
            if (player1 == null) {
                player1 = player;
            }
            fPlayerMap.put(new StringBuilder(64).append(player1.getFlatId()).append(player1.getPf()).toString(), player1);
            return player1;
        }
    }

	public void removePlayer(Player player){
		if (player != null){
			WebSocketManager.removeWebSocket(player);
			playerMap.remove(player.getUserId());
			fPlayerMap.remove(new StringBuilder(64).append(player.getFlatId()).append(player.getPf()).toString());
		}
	}

	/**
	 * 转化player
	 * 
	 * @param oldPlayer
	 * @param cl
	 * @return
	 */
	public Player changePlayer(Player oldPlayer, Class<? extends Player> cl) {
		if (oldPlayer.getClass() == cl) {
			return oldPlayer;
		}
		try {
			// 先保存数据
			oldPlayer.saveBaseInfo();
			// 保存当前的长连接
			MyWebSocket myWebSocket = oldPlayer.getMyWebSocket();
			Player player = ObjectUtil.newInstance(cl);
			RegInfo info = UserDao.getInstance().selectUserByUserId(oldPlayer.getUserId());
			if (info == null) {
				return null;
			}
			player.loadFromDB(info);

			Player player1 = addPlayer(player);
			if (player1.getClass()!=player.getClass()){
				player1 = addPlayer(player,true);
			}

			if (player1!=player){
				player1.loadFromDB(info);
			}

			player1.setMyWebSocket(myWebSocket);
			if (myWebSocket != null) {
				myWebSocket.setPlayer(player1);
			}
			if (oldPlayer.getGroupUser()!=null){
				player1.loadGroupUser(oldPlayer.getGroupUser().getGroupId().toString());
			}
			player1.setLastSaveDbTime(oldPlayer.getLastSaveDbTime());
			player1.setMsgCheckCode(oldPlayer.getMsgCheckCode0());
			player1.setPropertiesCache(oldPlayer.getPropertiesCache());
			player1.setUserTili(oldPlayer.getUserTili());
			player1.setLoginTime(oldPlayer.getLoginTime());
			player1.setRobotAILevel(oldPlayer.getRobotAILevel());
			player1.setRobot(oldPlayer.isRobot());
			//将玩家添加到playerMap和fPlayerMap中

			//监听日志
			LogUtil.monitor_i("changePlayer-->" + oldPlayer.getUserId() + " cl:" + cl.getName() + " tId:" + oldPlayer.getPlayingTableId());
			return player1;

		} catch (Exception e) {
			//错误日志
			LogUtil.e("changePlayer err:", e);
		}
		return null;
	}

    public void changeConsumeGold(Player player, int gold, int freeGold, int playType) {
		if(GameUtil.isPlayBaiRenWanfa(playType) && player.isRobot()) {
			return;// 百人玩法机器人不记录到消耗中
		}
		if (gold < 0) {
			consumeGold.getAndAdd(gold);
		}
		if (freeGold < 0) {
			consumeFreeGold.getAndAdd(freeGold);
		}
		int totalGold = gold + freeGold;
		if (totalGold < 0) {
			synchronized (this){
				Integer temp = playTypeGoldMap.get(playType);
				playTypeGoldMap.put(playType,temp==null?totalGold:(temp+totalGold));
			}
			if (!player.isRobot())
				LogDao.getInstance().insertGoldConsumeCards(player.getUserId(), player.getRegBindId(), player.getGoldPlayer().getUsedGold(), player.getReginTime());
		}
    }

    public void addUserCardRecord(UserCardRecordInfo info) {
		synchronized (userCardRecords) {
			userCardRecords.add(info);
		}
	}

	public void insertUserCards() {
		List<UserCardRecordInfo> list = null;
		synchronized (userCardRecords) {
			if (!userCardRecords.isEmpty()) {
				list = new ArrayList<>(userCardRecords);
				userCardRecords.clear();
			}
		}
		if (list != null){
			UserCardRecordDao.getInstance().batchInsert(list);
		}
	}

    public void addUserCoinRecord(Map<String,Object> info) {
        synchronized (userCoinRecord) {
            userCoinRecord.add(info);
        }
    }

    public void insertUserCoin() {
        List<Map<String,Object>> list = null;
        synchronized (userCoinRecord) {
            if (!userCoinRecord.isEmpty()) {
                list = new ArrayList<>(userCoinRecord);
                userCoinRecord.clear();
            }
        }
        if (list != null){
            UserCoinRecordDao.getInstance().batchInsert(list);
        }
    }

	public void addUpdateUserMission(Long userId,MissionAbout info) {
		synchronized (userMission) {
			userMission.put(userId,info);
		}
	}

	public void updateUserMission() {
		Map<Long,MissionAbout> map = null;
		synchronized (userMission) {
			if (!userMission.isEmpty()) {
				map = new ConcurrentHashMap<>(userMission);
				userMission.clear();
			}
		}
		if (map != null){
			MissionAboutDao.getInstance().batchUpdate(map);
		}
	}

	public void updateUserMissionNow(Long userId){
		MissionAbout ma = null;
		synchronized (userMission) {
			if (!userMission.isEmpty()&&userMission.containsKey(userId)) {
				ma =userMission.get(userId);
				userMission.remove(userId);
			}
		}
		if (ma != null){
			MissionAboutDao.getInstance().update(ma);
		}
	}

    public void addUserGoldRecord(UserGoldRecord info) {
        synchronized (userGoldRecords) {
            userGoldRecords.add(info);
        }
    }

    public void batchInsertUserGoldRecord() {
        try {
            List<UserGoldRecord> list = null;
            synchronized (userGoldRecords) {
                if (!userGoldRecords.isEmpty()) {
                    list = new ArrayList<>(userGoldRecords);
                    userGoldRecords.clear();
                }
            }
            if (list != null) {
                UserGoldRecordDao.getInstance().batchInsert(list);
            }
        } catch (Exception e) {
            LogUtil.errorLog.error("insertUserGoldRecord|error|" + e.getMessage(), e);
        }
    }

	public void addUserSign(Long userId,SevenGoldSign sign) {
		synchronized (userSign) {
			userSign.put(userId,sign);
		}
	}

	public void removeUserSign(Long userId) {
		synchronized (userSign) {
			userSign.remove(userId);
		}
	}

	public SevenGoldSign getUserSign(Long userId){
		SevenGoldSign sign = userSign.get(userId);
		if(sign==null){
			List<SevenGoldSign> signs = SevenGoldSignDao.getInstance().getGoldSign(userId);
			if(signs!=null&&signs.size()>0)
				sign=signs.get(0);
			else{
				sign=new SevenGoldSign(userId,new Date(0));
				SevenGoldSignDao.getInstance().insert(sign);
			}
			addUserSign(userId,sign);
		}
		return sign;
	}

}
