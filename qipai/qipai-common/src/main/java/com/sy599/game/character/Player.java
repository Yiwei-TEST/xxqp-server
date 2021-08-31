package com.sy599.game.character;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.GeneratedMessage;
import com.sy599.game.GameServerConfig;
import com.sy599.game.activity.ActivityConstant;
import com.sy599.game.activity.ActivityQueQiao;
import com.sy599.game.activity.MyActivity;
import com.sy599.game.base.BaseTable;
import com.sy599.game.common.UserResourceType;
import com.sy599.game.common.bean.CardConsume;
import com.sy599.game.common.bean.Consume;
import com.sy599.game.common.bean.MissionState;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.LogConstants;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.common.constant.SharedConstants.player_state;
import com.sy599.game.common.executor.TaskExecutor;
import com.sy599.game.common.service.FuncConsumeStatics;
import com.sy599.game.db.bean.*;
import com.sy599.game.db.bean.gold.GoldAcitivityRankResult;
import com.sy599.game.db.bean.gold.GoldRoomActivityUserItem;
import com.sy599.game.db.bean.group.GroupUser;
import com.sy599.game.db.bean.sendDiamonds.SendDiamondsLog;
import com.sy599.game.db.bean.sendDiamonds.SendDiamondsPermission;
import com.sy599.game.db.dao.*;
import com.sy599.game.db.dao.gold.GoldDao;
import com.sy599.game.db.dao.gold.GoldRoomActivityDao;
import com.sy599.game.db.dao.group.GroupCreditDao;
import com.sy599.game.db.dao.group.GroupDao;
import com.sy599.game.db.enums.CardSourceType;
import com.sy599.game.db.enums.CoinSourceType;
import com.sy599.game.db.enums.SourceType;
import com.sy599.game.extend.MyExtend;
import com.sy599.game.gcommand.login.base.msg.User;
import com.sy599.game.gcommand.login.util.LoginUtil;
import com.sy599.game.manager.MarqueeManager;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.manager.TableManager;
import com.sy599.game.message.MyMessage;
import com.sy599.game.msg.MarqueeMsg;
import com.sy599.game.msg.serverPacket.ComMsg.ComRes;
import com.sy599.game.msg.serverPacket.ConfigMsg;
import com.sy599.game.msg.serverPacket.PlayCardResMsg;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes;
import com.sy599.game.msg.serverPacket.TaskMsg.ATagMissionRes;
import com.sy599.game.msg.serverPacket.TaskMsg.MissionBoardRes;
import com.sy599.game.msg.serverPacket.TaskMsg.MissionRes;
import com.sy599.game.qipai.AbstractBaseCommandProcessor;
import com.sy599.game.staticdata.bean.GradeExpConfig;
import com.sy599.game.staticdata.bean.GradeExpConfigInfo;
import com.sy599.game.udplog.UdpLogger;
import com.sy599.game.util.*;
import com.sy599.game.util.gold.constants.GoldConstans;
import com.sy599.game.websocket.MyWebSocket;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.constant.WebSocketMsgType.TipsEnum;
import com.sy599.game.websocket.netty.WebSocketServerHandler;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import org.apache.commons.httpclient.util.DateUtil;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public abstract class Player {
	private static final int JSON_TAG = 1;
	private AtomicInteger msgCheckCode = new AtomicInteger(1);
	protected volatile long userId;
	protected String flatId;
	protected String name;
	protected String rawName;
	protected String ip;
	protected String deviceCode;
	private int maxPoint;
	private int totalPoint;
	private int totalBoom;
	private String headimgurl;
	private int enterServer;
	private String pf;
	protected int sex;
	private String identity;
	private volatile long freeCards;
	private volatile long cards;
	private volatile long usedCards;
	private long synUsedCards;
	private Date syncTime;
	private Date reginTime=new Date();
	private volatile Date loginTime;
	private Date logoutTime;
	private Date lastPlayTime;
	private Date payBindTime;
	private String channel;
	private int loginDays;
	private String pay;
	private String config;
	private volatile String sessionId;
	private ReentrantLock lock;
	private MyWebSocket myWebSocket;
	private MessageUnit messageUnit;
	private long actionTime;
	private int isLeave;
	private int drawLottery;
	private volatile int isOnline;
	private volatile List<List<Long>> record;
	protected volatile long playingTableId;
	private FuncConsumeStatics funcConsume;
	private MyActivity myActivity;
	private MyMessage myMessage;
	protected MyExtend myExtend;
	private GeneratedMessage recMsg;
	private boolean isLoad;
	private int actionCount;
	private long loginActionTime;
	private int regBindId;
	private String os;
	private String vc;
	private long payBindId;
	/** 玩家当月已签到的集合 **/
	private List<Integer> signs;
	private Integer userState;//玩家状态：0禁止登陆，1正常，2红名
	protected String loginExtend;
	protected int totalCount;
	protected int totalBureau;// 非金币场房间局数统计
    protected long goldRoomGroupId;

	protected volatile BaseTable table;

	protected GroupUser groupUser;

	protected GoldPlayer goldPlayer;

	/*** 中途退出房间标识*/
	private int quitTable = 0;

	private volatile String matchId;//比赛场标识

	/*** 是否牌友群成员标识*/
	private Integer isGroup;// 0非牌友群成员，1牌友群成员

	private String version;

	private volatile int userTili = 0;//体力

	private List<String> ymdSigns = new ArrayList<>();

	/**
	 * 保存在内存中，不需要持久化的有限数据
	 */
	protected Map<String,Long> propertiesCache = new ConcurrentHashMap<>();

	private Map<String, Object> dbParamMap = new ConcurrentHashMap<>();

	protected volatile long lastSaveDbTime = 0;//最后保存db的时间

	private String errerMsg;//错误消息

	/**
	 * 赢或输的信用分：正数为赢分，负数为负分
	 */
	private long winLoseCredit = 0;
	/**
	 * 洗牌次数
	 */
	private int xipaiCount = 0;
	/**
	 * 洗牌用的分
	 */
	private int xipaiScore = 0;
	/**
	 * 洗牌开始状态
	 */
	private int xipaiStatus = 0;
	/**
	 * 信用分佣金
	 */
	private long commissionCredit = 0;

	/**
	 * 最近手动操作的小局
	 */
	protected int lastActionBureau;

    /**
     * 托管累计计时:时间毫秒
     */
	private int autoPlayCheckedTime;
    /**
     * 托管累计计时辅助值
     */
    private boolean autoPlayCheckedTimeAdded;
    /**
     * 托管累计计时辅助值
     */
    private boolean autoPlayChecked;

    private long joinTime;
    
    
	//牌桌状态：1开局，0未开局或不在房间内
	private int playState;
	//用该字段保存累计分(郴州字牌)
	private int finalPoint;


	//
	private int dissCount;

    private volatile long coin;
    private volatile long freeCoin;
    private volatile long usedCoin;
	private int isCreateGroup;
    
    private int goldResult;

    private int isSpAdmin;//是否超级管理员，可以配置权限

    /** 输赢金币**/
    private long winLoseCoin;

    /*** 是否需要重新检查听牌消息***/
    private boolean needCheckTing = true;
    /*** 打牌听牌信息**/
    PlayCardResMsg.DaPaiTingPaiRes daTingMsg = null;
    /*** 听牌信息**/
    PlayCardResMsg.TingPaiRes tingMsg = null;

	Mission mission=new Mission();

    /*** 输赢金币***/
    private long winGold = 0;

    /*** 就否在solo胜利***/
    private boolean isSoloWinner = false;


    /**
     * 累计胜利次数
     */
//    private int twinCount;

    /**
     * 已领取的胜利奖励id
     */
//    private List<Integer> tWinIds = new ArrayList<>();

    private UserTwinReward utreward;

    /**
     * 是否是机器人
     */
    private boolean isRobot = false;

    private int robotActionCounter;
    private int robotActionRND;

    private int robotAILevel = -1;

    private ActivityQueQiao aqq=null;

    public Player() {
		record = new ArrayList<>();
		myActivity = new MyActivity(this);
		myMessage = new MyMessage(this);
		myExtend = new MyExtend(this);
		joinTime = System.currentTimeMillis();
	}
	private long creditLogData =0l;
    public long getCoin() {
        return coin;
    }

    public void setCoin(long coin) {
        this.coin = coin;
    }

    public long getFreeCoin() {
        return freeCoin;
    }

    public void setFreeCoin(long freeCoin) {
        this.freeCoin = freeCoin;
    }

    public long getUsedCoin() {
        return usedCoin;
    }

    public void setUsedCoin(long usedCoin) {
        this.usedCoin = usedCoin;
    }

    public String getErrerMsg() {
		return errerMsg;
	}

	public void setErrerMsg(String errerMsg) {
		this.errerMsg = errerMsg;
	}

	public String getMatchId() {
		return matchId;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getVersion() {
		return version;
	}

	public void setMsgCheckCode(AtomicInteger msgCheckCode) {
		this.msgCheckCode = msgCheckCode;
	}

	public void setLastSaveDbTime(long lastSaveDbTime) {
		this.lastSaveDbTime = lastSaveDbTime;
	}

	public long getLastSaveDbTime() {
		return lastSaveDbTime;
	}

    public ActivityQueQiao getAqq() {
        return aqq;
    }

	/**
	 * 获取totalPoint
	 * @see #getTotalPoint()
	 * @return
	 */
	public int loadScore(){
		return getTotalPoint();
	}

	public Date getPayBindTime() {
		return payBindTime;
	}

	public void setPayBindTime(Date payBindTime) {
		this.payBindTime = payBindTime;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public Date getLastPlayTime() {
		return lastPlayTime;
	}

	public void setLastPlayTime(Date lastPlayTime) {
		this.lastPlayTime = lastPlayTime;
		this.dbParamMap.put("lastPlayTime", this.lastPlayTime);
	}

//	public int getFinalPoint() {
//		return finalPoint;
//	}
//
//	public void setFinalPoint(int finalPoint) {
//		this.finalPoint = finalPoint;
//	}

	/**
	 * 变更用户体力
	 * @param tili
	 * @return
	 */
	public int changeTili(int tili){
		return changeTili(tili,false);
	}

	public int changeTili(int tili,boolean db){
		synchronized (this){
			userTili += tili;
		}

		if (db){
			UserDao.getInstance().updateUserExtendIntValue(String.valueOf(userId),UserResourceType.TILI,tili);
			writeUserResourceMessage(UserResourceType.TILI,tili,userTili);
		}
		return userTili;
	}

	public int getUserTili() {
		return userTili;
	}

	public void setUserTili(int userTili) {
		this.userTili = userTili;
	}

	public List<String> getYmdSigns() {
		return ymdSigns;
	}

	public String getRawName() {
		return rawName;
	}

	public void setRawName(String rawName) {
		this.rawName = rawName;
	}

	public int getIsGroup() {
		return isGroup;
	}

	public int getIsCreateGroup() {
		return isCreateGroup;
	}

	public void setIsCreateGroup(int isCreateGroup) {
		this.isCreateGroup = isCreateGroup;
	}

	public boolean IsSpAdmin() {
		return isSpAdmin == 1;
	}
	public int getIsSpAdmin() {
		return isSpAdmin;
	}
	public void setIsSpAdmin(int isSpAdmin) {
		this.isSpAdmin = isSpAdmin;
	}

	public void setIsGroup(int isGroup) {
		this.isGroup = isGroup;
	}

	public int getQuitTable() {
		return quitTable;
	}

	public void setQuitTable(int quitTable) {
		this.quitTable = quitTable;
	}

	public GoldPlayer getGoldPlayer() {
		return goldPlayer;
	}

	public void setGoldPlayer(GoldPlayer goldPlayer) {
		this.goldPlayer = goldPlayer;
	}

	public GroupUser getGroupUser() {
		return groupUser;
	}

	public void setGroupUser(GroupUser groupUser) {
		this.groupUser = groupUser;
	}

	public Map<String, Long> getPropertiesCache() {
		return propertiesCache;
	}

	public void setPropertiesCache(Map<String, Long> propertiesCache) {
		this.propertiesCache = propertiesCache;
	}

	public Integer getUserState() {
		return userState;
	}

	public void setUserState(Integer userState) {
		this.userState = userState;
	}

	public void changeUserState(Integer userState) {
		setUserState(userState);

		if (isForbidLogin()){
			forbid();
		}
	}

	/**
	 * 是否被禁止登陆
	 * @return
	 */
	public boolean isForbidLogin(){
		return userState!=null&&userState.intValue()==0;
	}

	/**
	 * 是否红名
	 * @return
	 */
	public boolean isRedPlayer(){
		return userState!=null&&userState.intValue()==2;
	}

	/**
	 * 是否是正常玩家
	 * @return
	 */
	public boolean isOkPlayer(){
		return userState==null||userState.intValue()==1;
	}

	public final void loadFromDB(RegInfo info) {
		this.messageUnit = null;
		this.loginExtend = info.getLoginExtend();
		this.headimgurl = info.getHeadimgurl();
		this.userId = info.getUserId();
		this.flatId = info.getFlatId();
		this.rawName = info.getName();
		this.name = info.getName();
		if (org.apache.commons.lang3.StringUtils.isNotBlank(this.name)){
			this.name = this.name.replaceAll("\\,|\\;|\\_|\\||\\*","");
		}
		this.pf = info.getPf();
		this.sex = info.getSex();
		this.sessionId = info.getSessCode();
		this.cards = info.getCards();
		// this.syncCards = info.getSyncCards();
		this.loginDays = info.getLoginDays();
		this.syncTime = info.getSyncTime();
		this.reginTime = info.getRegTime();
		this.loginTime = info.getLogTime();
		this.logoutTime = info.getLogoutTime();
		this.playingTableId = info.getPlayingTableId();
		this.payBindTime = info.getPayBindTime();
		this.channel = info.getChannel();
		this.enterServer = info.getEnterServer();
		this.config = info.getConfig();
		this.isCreateGroup = info.getIsCreateGroup();
		this.isSpAdmin = info.getIsSpAdmin();
		this.ip = info.getIp();
		this.freeCards = info.getFreeCards();
		this.usedCards = info.getUsedCards();
		this.drawLottery = info.getDrawLottery();
		this.isOnline = info.getIsOnLine();
		this.myActivity.loadFromDB(info.getActivity());
		this.myExtend.initData(info.getExtend());
		this.os = info.getOs();
		this.vc = info.getSyvc();
		this.regBindId = info.getRegBindId();
		this.payBindId = info.getPayBindId();
		this.identity = info.getIdentity();
		if (!StringUtils.isBlank(info.getRecord())) {
			this.record = StringUtil.explodeToLongLists(info.getRecord());
		}
		this.userState=info.getUserState();
		if (this.userState==null){
			this.userState=1;
		}
		this.playState = info.getPlayState();
		this.coin = info.getCoin();
		this.freeCoin = info.getFreeCoin();
		this.usedCoin = info.getUsedCoin();
		this.goldRoomGroupId = info.getGoldRoomGroupId();

		//改为从extend计算
//		this.totalCount=info.getTotalCount();

		if (playingTableId>0){
			getPlayingTable();
		} else {
			settleTempCredit();
		}

		// 加载是否牌友群成员标识
		loadGroupUser();

		// 加载金币玩家身份
		loadGoldPlayer(true);

		matchId = loadMatchId();

		loadFromDB0(info);
		List<MissionAbout> l = MissionAboutDao.getInstance().selectMissionStateByUserId(userId);
		if(l!=null&&l.size()>0)
			mission.initDate(l.get(0));
		else
			MissionAboutDao.getInstance().insert(new MissionAbout(userId));
		utreward = UserTwinRewardDao.getInstance().getUserTwinReward(userId);
		if(aqq==null)
			aqq=new ActivityQueQiao(this);

		// 初始化机器人
        if(info.getIsRobot() != null && info.getIsRobot() == 1){
            this.isRobot = true;
            initRobotFromDB(info.getRobotInfo());
        }
	}

	protected void loadFromDB0(RegInfo info){

	}

	/**
	 * 自动解绑
	 */
	public int autoRemoveBind(Long id){
		int res = UserDao.getInstance().removeBindById(id);
		if (res == 1) {
			UserDao.getInstance().addRBRecordByPlayer(this);
		}
		return res;
	}

	/**
	 * 加载军团数据
	 */
	public GroupUser loadGroupUser(String groupId){
		isGroup = 0;
		GroupUser groupUser = null;
		if ("1".equals(ResourcesConfigsUtil.loadServerPropertyValue("isGroupUser_load"))||(groupId!=null&&groupId.length()>=4)) {
			groupUser = GroupDao.getInstance().loadGroupUser(this.getUserId(),groupId);
			if (groupUser!=null) {
				setIsGroup(1);
			} else {
				setIsGroup(0);
			}
		} else {
			setIsGroup(0);
		}
		setGroupUser(groupUser);
		return groupUser;
	}
	/**
	 * 加载军团数据
	 */
	public void loadGroupUser(){
		loadGroupUser(null);
	}

	public void changeTotalCount(){
		this.totalCount++;
		dbParamMap.put("totalCount", JSON_TAG);
	}

	public void changeTotalBureau() {
		this.totalBureau++;
		dbParamMap.put("totalBureau", JSON_TAG);
	}

	public boolean refreshPlayer() {
		RegInfo info = UserDao.getInstance().selectUserByUserId(userId);
		if (info != null) {
			loadFromDB(info);
			return true;
		}
		return false;
	}

	public abstract void initPlayInfo(String data);

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	public int getTotalBureau() {
		return totalBureau;
	}

	public void setTotalBureau(int totalBureau) {
		this.totalBureau = totalBureau;
	}

	public long getPayBindId() {
		return payBindId;
	}

	public void setPayBindId(long payBindId) {
		this.payBindId = payBindId;
	}

	public List<Integer> getSigns() {
		return signs;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public MessageUnit getMessageUnit() {
		return messageUnit;
	}

	public int getMsgCheckCode() {
		return msgCheckCode.get();
	}

	public AtomicInteger getMsgCheckCode0() {
		return msgCheckCode;
	}

	public void initMsgCheckCode() {
		this.msgCheckCode.set(1+new SecureRandom().nextInt(100000));
	}

	public int incrementAndGetMsgCheckCode() {
		return msgCheckCode.addAndGet(1);
	}

	public void setMessageUnit(MessageUnit msgUnit) {
		this.messageUnit = msgUnit;
	}

	public MyWebSocket getMyWebSocket() {
		return myWebSocket;
	}

	public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
		return this.lock.tryLock(timeout, unit);
	}

	public boolean tryLock() {
		return this.lock.tryLock();
	}

	public void unLock() {
		this.lock.unlock();
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
		this.dbParamMap.put("sessionId", this.sessionId);
	}

	public int getWinCount(){
		return 0;
	}
	public int getLostCount(){
		return 0;
	}

	public long getActionTime() {
		return actionTime;
	}

	public void setActionTime(long actionTime) {
		this.actionTime = actionTime;
	}

	public String getFlatId() {
		return flatId;
	}

	public void setFlatId(String flatId) {
		this.flatId = flatId;
		this.dbParamMap.put("flatId", this.flatId);
	}

	public void setMyWebSocket(MyWebSocket myWebSocket) {
		this.myWebSocket = myWebSocket;
	}

	public void changeConsumeNum() {

	}

	public FuncConsumeStatics getFuncConsume() {
		return funcConsume;
	}

	public void changeRefreshTime() {

	}

	public void changeExtend() {
		dbParamMap.put("extend", JSON_TAG);
	}

	public String getIp() {
		return ip;
	}

	public String getDeviceCode() {
		return deviceCode;
	}

	public void setIp(String ip) {
		setIp(ip,true);
	}

	public void setIp(String ip,boolean save) {
		this.ip = ip;
		if (save){
			this.dbParamMap.put("ip", this.ip);
		}
	}

    public void exit(String channelId) {
        LogUtil.monitorLog.info("login|Player|exit|1|" + getUserId() + "|" + getPlayingTableId() + "|" + channelId + "|" + (myWebSocket != null ? myWebSocket.getCtx().channel().id().asShortText() : "") + "|" + ip + "|" + Thread.currentThread().getName());
        if (myWebSocket != null && !channelId.equals(myWebSocket.getCtx().channel().id().asShortText())) {
            return;
        }
        setLogoutTime(TimeUtil.now());
        setIsOnline(0,false);

        if (WebSocketServerHandler.isOpen) {
            saveBaseInfo();
            BaseTable table = getPlayingTable();
            if (table != null) {
                table.broadIsOnlineMsg(this, SharedConstants.table_offline);
            }
            sendActionLog(LogConstants.reason_logout, "");
        }

        PlayerManager.getInstance().removePlayer(this);
        if (myWebSocket != null) {
            myWebSocket.setPlayer(null);
            setMyWebSocket(null);
        }

        dbParamMap.remove("isOnline");
        UserDao.getInstance().saveOffLine(GameServerConfig.SERVER_ID, userId);
        PlayerManager.getInstance().updateUserMissionNow(userId);
		PlayerManager.getInstance().removeUserSign(userId);
        LogUtil.monitorLog.info("login|Player|exit|2|" + getUserId()+ "|" + getPlayingTableId() + "|" + (myWebSocket != null ? myWebSocket.getCtx().channel().id().asShortText() : "") + "|" + ip + "|" + Thread.currentThread().getName());
    }

	public void setTable(BaseTable table) {
		this.table = table;
	}

	public Map<String,Object> loadCurrentDbMap(){
		// copy 一份map
		Map<String, Object> tempMap = new HashMap<>();
		synchronized (this) {
			Iterator<Map.Entry<String, Object>> it = dbParamMap.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, Object> kv = it.next();
				tempMap.put(kv.getKey(), kv.getValue());
				it.remove();
			}
		}
		return tempMap;
	}

	private final void forbid(){
		try{
			setIsOnline(0);
			LogUtil.e(userId + " 您已被禁止登陆");
			writeComMessage(WebSocketMsgType.res_code_err,"您已被禁止登陆");
			if (myWebSocket!=null){
				PlayerManager.getInstance().removePlayer(this);
				myWebSocket.close();
			}
		}catch (Exception e){
			LogUtil.errorLog.error("Exception:"+e.getMessage(),e);
		}
	}

	public final void saveBaseInfo(){
		saveBaseInfo(false);
	}

	public final void saveBaseInfo(boolean asyn) {
		if (userId < 0) {
			return;
		}

		if (isForbidLogin()){
			forbid();
		}

		if (asyn && WebSocketServerHandler.isOpen){
			TaskExecutor.coreExecutor.execute(new Runnable() {
				public void run() {
					// copy 一份map
					Map<String, Object> tempMap = loadCurrentDbMap();
					synUsedCards = 0;
					int count = tempMap.size();
					if (count > 0) {
						Object syncTime;
						if (count==1&&(syncTime=tempMap.get("syncTime"))!=null&&(System.currentTimeMillis()-lastSaveDbTime)<10*60*1000){
							dbParamMap.put("syncTime",syncTime);
						}else{
							buildBaseSaveMap(tempMap);
							UserDao.getInstance().save(flatId, pf, tempMap);
						}
					}
				}
			});
		}else{
			// copy 一份map
			Map<String, Object> tempMap = loadCurrentDbMap();

			synUsedCards = 0;
			if (!tempMap.isEmpty()) {
				buildBaseSaveMap(tempMap);
				UserDao.getInstance().save(flatId, pf, tempMap);
			}
		}
	}

	public final Map<String, Object> saveDB(boolean asyn) {
		actionCount = 0;
		if (userId < 0) {
			return null;
		}

		if (isForbidLogin()){
			forbid();
		}

		// copy 一份map
		Map<String, Object> tempMap = loadCurrentDbMap();
		synUsedCards = 0;

		int count = tempMap.size();
		if (count > 0) {
			Object syncTime;
			if (asyn && count == 1 && (syncTime = tempMap.get("syncTime")) != null&&(System.currentTimeMillis()-lastSaveDbTime)<10*60*1000){
				dbParamMap.put("syncTime",syncTime);
				return null;
			}else{
				buildBaseSaveMap(tempMap);
			}
			return tempMap;
		}else{
			return null;
		}
	}

	private void buildBaseSaveMap(Map<String, Object> tempMap) {
		tempMap.put("userId", userId);
		if (tempMap.containsKey("refreshTime")) {
			tempMap.put("refreshTime", "");
		}
		if (tempMap.containsKey("activity")&&myActivity!=null) {
			tempMap.put("activity", myActivity.toJson());
		}
		if (tempMap.containsKey("record")) {
			tempMap.put("record", StringUtil.implodeLongLists(record));
		}
		if (tempMap.containsKey("extend")&&myExtend!=null) {
			tempMap.put("extend", myExtend.toJson());
		}
		if (tempMap.containsKey("totalCount")) {
			tempMap.put("totalCount", this.totalCount);
		}
		if (tempMap.containsKey("totalBureau")) {
			tempMap.put("totalBureau", this.totalBureau);
		}
		if (tempMap.containsKey("enterServer")) {
			tempMap.put("enterServer", this.enterServer);
		}
		if (tempMap.containsKey("playingTableId")) {
			tempMap.put("playingTableId", this.playingTableId);
		}
		if (tempMap.containsKey("playState")) {
			tempMap.put("playState", this.playState);
		}
		
		// if (PlayerManager.syncTime == null) {
		// PlayerManager.syncTime = TimeUtil.now();
		// }
		// tempMap.put("syncTime", PlayerManager.syncTime);

		lastSaveDbTime = System.currentTimeMillis();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		this.dbParamMap.put("name", this.name);
	}

	public void writeSocket(MessageUnit message) {
		if (myWebSocket != null) {
			myWebSocket.send(message);
		}
	}

	/**
	 * 推送消息给前台
	 *
	 * @param message
	 */
	public void writeSocket(GeneratedMessage message) {
		if (myWebSocket != null) {
			myWebSocket.send(message);
//            LogUtil.msgLog.info("writeSocket|" + getUserId() + "|" + message);
		}/* else {
			LogUtil.e("myWebSocket is null-->send message error");
		}*/
	}

	public String getPf() {
		return pf;
	}

	public void setPf(String pf) {
		this.pf = pf;
		this.dbParamMap.put("pf", this.pf);
	}

	public int getSex() {
		return sex;
	}

	public void setSex(int sex) {
		this.sex = sex;
		this.dbParamMap.put("sex", this.sex);
	}

	public String getIdentity() {
		return identity;
	}

	public void setIdentity(String identity) {
		this.identity = identity;
		this.dbParamMap.put("identity", this.identity);
	}

	public long getCards() {
		return cards;
	}

	public void setCards(long cards) {
		this.cards += cards;
		this.dbParamMap.put("cards", cards);
	}

	/**
	 * 如果距离上一次请求有25秒并且socket已经断开 判断离线
	 *
	 * @return
	 */
	public boolean isOnline() {
		long lastIntervalTime = 0;
		Date syncTime = getSyncTime();
		if (syncTime != null) {
			lastIntervalTime = TimeUtil.currentTimeMillis() - syncTime.getTime();
		}

		if (lastIntervalTime == 0) {
			return myWebSocket != null;
		}

		if (lastIntervalTime != 0 && lastIntervalTime > SharedConstants.SENCOND_IN_MINILLS * 25) {
			// 距离上一次请求有25秒 并且socket已经断开
			if (myWebSocket == null) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 前台最后一次请求的时间
	 *
	 * @return
	 */
	public Date getSyncTime() {
		return syncTime;
	}

	public void setSyncTime(Date syncTime) {
		this.syncTime = syncTime;
		this.dbParamMap.put("syncTime", this.syncTime);
	}

	public Date getReginTime() {
		return reginTime;
	}

	public void setReginTime(Date reginTime) {
		this.reginTime = reginTime;
		this.dbParamMap.put("reginTime", this.reginTime);
	}

	public Date getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(Date loginTime) {
		this.loginTime = loginTime;
	}

	public Date getLogoutTime() {
		return logoutTime;
	}

	public void setLogoutTime(Date logoutTime) {
		this.logoutTime = logoutTime;
		this.dbParamMap.put("logoutTime", this.logoutTime);
	}

	public String getPay() {
		return pay;
	}

	public void setPay(String pay) {
		this.pay = pay;
	}

	public String getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = config;
		this.dbParamMap.put("config", this.config);
	}

	public long getPlayingTableId() {
		return playingTableId;
	}

	public void setPlayingTableId(long playingTableId) {
        if (this.playingTableId != playingTableId) {
            StringBuilder sb = new StringBuilder("Player|playingTableId");
            sb.append("|").append(getUserId());
            sb.append("|").append(getEnterServer());
            sb.append("|").append(this.playingTableId);
            sb.append("|").append(playingTableId);
            LogUtil.monitorLog.info(sb.toString());
        }
		this.playingTableId = playingTableId;

		if (playingTableId<=0){
			table = null;
		}
		if(playingTableId==0) {
			playState=0;
			changePlayState();
		}

		dbParamMap.put("playingTableId", playingTableId);
	}

	public boolean isHasCards(int needCard) {
		return this.freeCards + this.cards >= needCard;

	}

	public <T> T getPlayer(AbstractBaseCommandProcessor processor) {
		return getClass() == PlayerManager.getInstance().getPlayer(processor) ? ((T) this) : null;
	}

	public int getTotalPoint() {
		return totalPoint;
	}

	public void setTotalPoint(int totalPoint) {
		this.totalPoint = totalPoint;
	}

	public void changeTotalPoint(int totalPoint) {
		this.totalPoint += totalPoint;
	}

	public int getMaxPoint() {
		return maxPoint;
	}

	public void setMaxPoint(int maxPoint) {
		this.maxPoint = maxPoint;
	}

	public int getTotalBoom() {
		return totalBoom;
	}

	public void setTotalBoom(int totalBoom) {
		this.totalBoom = totalBoom;
	}

	public void changeTotalBoom(int totalBoom) {
		this.totalBoom += totalBoom;
	}

	public int getIsLeave() {
		if (isLeave == 0 && !isOnline()) {
			return 1;
		}
		return isLeave;
	}

	public void changeIsLeave(int isLeave) {
		this.isLeave = isLeave;
	}

	/**
	 * 发送通用消息
	 *
	 * @param code
	 * @param notCheckCode
	 *            不需要检查checkCode
	 * @param params
	 */
	public void writeOutSyncComMessage(int code, boolean notCheckCode, Object... params) {
		MessageUnit messageUnit = new MessageUnit();
		messageUnit.setNotCheckCode(notCheckCode);
		ComRes.Builder res = SendMsgUtil.buildComRes(code, params);
		messageUnit.setMessage(res.build());
		writeSocket(messageUnit);

	}

	/**
	 * 发送通用消息
	 *
	 * @param code
	 * @param params
	 */
	public void writeComMessage(int code, Object... params) {
		if(!isRobot()){
			ComRes.Builder res = SendMsgUtil.buildComRes(code, params);
			writeSocket(res.build());
		}
	}

	/**
	 * 推送前台房卡更新
	 *
	 * @param cards
	 */
	public void writeCardsMessage(int cards) {
		ComRes.Builder res = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_cards, cards,String.valueOf(loadAllCards()),String.valueOf(loadAllGolds()));
		writeSocket(res.build());
//		writeOutSyncComMessage(WebSocketMsgType.res_code_cards,true,cards,String.valueOf(loadAllCards()),String.valueOf(loadAllGolds()));
	}

	/**
	 * 推送前台金币更新
	 */
	public void writeGoldMessage(long changeGols) {
		ComRes.Builder res = SendMsgUtil.buildComRes(WebSocketMsgType.res_com_gold, String.valueOf(changeGols), getGoldPlayer().getShowGold());
		writeSocket(res.build());
	}

    /**
     * 推送前台金币更新
     */
    public void writeGoldMessage(long curGold , long changeGold , long allGold) {
        ComRes.Builder res = SendMsgUtil.buildComRes(WebSocketMsgType.res_com_gold, String.valueOf(changeGold), String.valueOf(allGold), String.valueOf(curGold));
        writeSocket(res.build());
    }

	/**
	 * 推送前台段位更新
	 */
	public void writeGradeMessage(int grade, String gradeDesc) {
		ComRes.Builder res = SendMsgUtil.buildComRes(WebSocketMsgType.res_com_sendGrade, String.valueOf(grade), gradeDesc);
		writeSocket(res.build());
	}

	/**
	 * 推送前台用户资源更新
	 */
	public void writeUserResourceMessage(UserResourceType type,long count,long total) {
		ComRes.Builder res = SendMsgUtil.buildComRes(WebSocketMsgType.res_com_resource,type.getType(), type.name(), String.valueOf(count), String.valueOf(total));
		writeSocket(res.build());
	}

    public <T extends BaseTable> T getPlayingTable() {
        if (table == null || table.getId() != playingTableId) {
            table = TableManager.getInstance().getTable(playingTableId);

            if (table == null && playingTableId > 0 && GoldRoomUtil.isNotGoldRoom(playingTableId) && !GameUtil.isPlayBaiRenWanfa((int) playingTableId)) {
                LogUtil.errorLog.error("player table not found:userId=" + userId + ",tableId=" + playingTableId);
//				setPlayingTableId(0);
//				saveBaseInfo();
            }
        }
        return (T) table;
    }

	public <T extends BaseTable> T getPlayingTable(Class<T> clazz) {
		return (T) getPlayingTable();
	}

	public void writeErrMsg(String errMsg) {
		if (errMsg!=null&&errMsg.startsWith("code_")){
			writeComMessage(WebSocketMsgType.res_code_err, LangHelp.getMsg(errMsg));
			setErrerMsg(LangHelp.getMsg(errMsg));
		}else{
			writeComMessage(WebSocketMsgType.res_code_err, errMsg);
			setErrerMsg(errMsg);
		}
	}

	public void writeErrMsgs(Object... msgs) {
		writeComMessage(WebSocketMsgType.res_code_err, msgs);
	}

	public void writeErrMsg(String langKey, Object... o) {
		writeComMessage(WebSocketMsgType.res_code_err, LangHelp.getMsg(langKey, o));
	}

	// public void writeFormMsg(String errMsg) {
	// writeComMessage(WebSocketMsgType.sc_code_shutdown, errMsg);
	// }

	public String getHeadimgurl() {
		return headimgurl;
	}

	public void setHeadimgurl(String headimgurl) {
		this.headimgurl = headimgurl;
	}

	public int getEnterServer() {
		return enterServer;
	}

	public void setEnterServer(int enterServer) {
		this.enterServer = enterServer;
		dbParamMap.put("enterServer", enterServer);
	}

	public long getFreeCards() {
		return freeCards;
	}


    public boolean changeCards(long freeCards, long cards, boolean isWrite, int playType){
        CardConsume consume = new CardConsume();
        consume.setFreeCards(freeCards);
        consume.setCards(cards);
        consume.setWrite(isWrite);
        consume.setPlayType(0);
        consume.setRecord(true);
        consume.setSourceType(CardSourceType.unknown);
        return changeCards(consume,1);
    }

    /**
     * @param freeCards
     *            免费房卡(增加免费房卡)
     * @param cards
     *            收费房卡(消耗房卡，会优化消耗免费房卡)
     * @param isWrite
     *            是否推送给前台
     */
    public boolean changeCards(long freeCards, long cards, boolean isWrite, CardSourceType sourceType) {
        CardConsume consume = new CardConsume();
        consume.setFreeCards(freeCards);
        consume.setCards(cards);
        consume.setWrite(isWrite);
        consume.setPlayType(0);
        consume.setRecord(true);
        consume.setSourceType(sourceType);
        return changeCards(consume,1);
    }

    // 消耗房卡过渡方法
    public boolean changeCards(long freeCards, long cards, boolean isWrite, int playType, CardSourceType sourceType) {
        CardConsume consume = new CardConsume();
        consume.setFreeCards(freeCards);
        consume.setCards(cards);
        consume.setWrite(isWrite);
        consume.setPlayType(playType);
        consume.setRecord(true);
        consume.setSourceType(sourceType);
        return changeCards(consume,1);
    }

    public boolean changeCards(final long freeCards, final long cards, boolean isWrite, int playType, boolean isRecord, CardSourceType sourceType) {
        CardConsume consume = new CardConsume();
        consume.setFreeCards(freeCards);
        consume.setCards(cards);
        consume.setWrite(isWrite);
        consume.setPlayType(playType);
        consume.setRecord(isRecord);
        consume.setSourceType(sourceType);
        return changeCards(consume,1);
    }

    public boolean changeCards(CardConsume consume) {
        return changeCards(consume, 1);
    }


    /**
     * 钻石变动
     */
    private boolean changeCards(CardConsume consume, int tryCount) {
        consume.setPlayer(this);
        if (isRobot()) {
            consume.setOK(true);
            return true;
        }

        long freeCards = consume.getFreeCards();
        long cards = consume.getCards();
        boolean isWrite = consume.isWrite();
        int playType = consume.getPlayType();
        boolean isRecord = consume.isRecord();
        CardSourceType sourceType = consume.getSourceType();
        long temp1 = 0; // 减少的免费钻 freeCards
        long temp2 = 0; // 减少的充值钻 cards
        synchronized (this) {
            if (cards < 0) { //扣钻，只允许会用该值
                // temp等于绑定房卡 + cards
                long temp = this.freeCards + cards;
                if (temp >= 0) {
                    // 房卡足够
                    this.freeCards = temp;
                    temp2 = 0;
                    temp1 = -cards;
                } else {
                    // 房卡不足，先用完绑定房卡，再用普通房卡
                    this.freeCards = 0;
                    temp2 = -temp;
                    temp1 = (-cards) - temp2;
                }
                temp1 += -freeCards;

                this.cards -= temp2;
                this.freeCards += freeCards;
            } else {
                temp1 = -freeCards;
                temp2 = -cards;

                this.cards += cards;
                this.freeCards += freeCards;
            }

            if (temp1 > 0 && isRecord) {
                changeUsedCards(-temp1);
            }
            if (temp2 > 0 && isRecord) {
                changeUsedCards(-temp2);
            }

            Map<String, Object> log = new HashMap<>();
            log.put("freeCards", -temp1);
            log.put("cards", -temp2);
            log.put("playType", playType);
            log.put("isRecord", isRecord ? 1 : 0);

//            LogUtil.msgLog.info("statistics:userId=" + userId + ",tableId=" + playingTableId + ",isRecord=" + isRecord + " ,playType=" + playType + ",freeCards=" + freeCards + ",cards=" + cards + ",rest freeCards=" + this.freeCards + ",rest cards=" + this.cards + ",isWrite=" + isWrite + ",temp1=" + temp1 + ",temp2=" + temp2);

            if (temp2 != 0 || temp1 != 0) {
                if (UserDao.getInstance().updateUserCards(userId, flatId, pf, -temp2, -temp1) > 0) {
                    if (isWrite) {
                        writeCardsMessage((int) (-temp1 - temp2));
                    }
                    consume.setOK(true);
                    consume.setFreeCards1(-temp1);
                    consume.setCards1(-temp2);
                } else {
                    long[] cs = UserDao.getInstance().loadUserCards(String.valueOf(userId));
                    LogUtil.errorLog.warn("updateUserCards fail:userId={},cards={},freeCards={},cards(current)={},freeCards(current)={},cards(db)={},freeCards(db)={},tryCount={}", userId, -temp2, -temp1, this.cards, this.freeCards, cs[0], cs[1], tryCount);
                    this.cards = cs[0];
                    this.freeCards = cs[1];
                    tryCount++;
                    if (tryCount > 4) {
                        return false;
                    }
                    return changeCards(consume, tryCount);
                }
            }

            if (isRecord && (temp2 > 0 || temp1 > 0)) {
                PlayerManager.getInstance().changeConsume(this, (int) -temp2, (int) -temp1, playType);
            }

            sendActionLog(LogConstants.reason_consumecards, JacksonUtil.writeValueAsString(log));
            PlayerManager.getInstance().addUserCardRecord(new UserCardRecordInfo(userId, this.freeCards, this.cards, (int) -temp1, (int) -temp2, playType, sourceType));

            StringBuilder sb = new StringBuilder("changeCards");
            sb.append("|").append(userId);
            sb.append("|").append(playingTableId);
            sb.append("|").append(isRecord);
            sb.append("|").append(playType);
            sb.append("|").append(freeCards);
            sb.append("|").append(cards);
            sb.append("|").append(this.freeCards);
            sb.append("|").append(this.cards);
            sb.append("|").append(isWrite);
            sb.append("|").append(temp1);
            sb.append("|").append(temp2);
            sb.append("|").append(tryCount);
            LogUtil.msgLog.info(sb.toString());
        }

        return true;
    }

	/**
	 * 改变房卡数量（消耗房卡不能使用该方法）
	 * @param freeCards
	 * @param commonCards
	 * @param saveDb
	 * @param sendClient
	 */
	public void changeCards(long freeCards,long commonCards,boolean saveDb,boolean sendClient, CardSourceType sourceType){
		synchronized (this) {
			this.freeCards += freeCards;
			this.cards += commonCards;
		}
		if (saveDb){
			UserDao.getInstance().updateUserCards(userId, flatId, pf, commonCards, freeCards);
            PlayerManager.getInstance().addUserCardRecord(new UserCardRecordInfo(userId, this.freeCards, this.cards, (int) freeCards, (int) commonCards, 0, sourceType));
		}
		if (sendClient){
			writeCardsMessage((int) (freeCards+commonCards));
		}
	}

	public void updatePayCards(int cards, int freeCards) {
		synchronized (this) {
			RegInfo info = UserDao.getInstance().selectUserByUserId(userId);
			if (info != null) {
				this.cards = info.getCards();
				this.freeCards = info.getFreeCards();
			}
		}
		Map<String, Object> log = new HashMap<>();
		log.put("freeCards", freeCards);
		log.put("cards", cards);
		sendActionLog(LogConstants.reason_pay, JacksonUtil.writeValueAsString(log));
	}

	public void updatePayGold(int gold, int freeGold) {
		GoldPlayer goldPlayer = getGoldPlayer();
		if (goldPlayer == null) {
			LogUtil.e("updatePayGold err-->goldPlayer is null,userId:"+userId);
			return;
		}
		GoldPlayer goldPlayer0;
		try {
			synchronized (this) {
				goldPlayer0 = GoldDao.getInstance().selectGoldUserByUserId(userId);
				if (goldPlayer0 != null) {
					goldPlayer.setGold(goldPlayer0.getGold());
					goldPlayer.setFreeGold(goldPlayer0.getFreeGold());
					getMyExtend().updateUserMaxGold();
				}
			}
			Map<String, Object> log = new HashMap<>();
			log.put("freeGold", freeGold);
			log.put("gold", gold);

			sendActionLog(LogConstants.reason_pay, JacksonUtil.writeValueAsString(log));
		} catch (Exception e) {
			LogUtil.e("updatePayGold err-->", e);
		}
	}

	public long getUsedCards() {
		return usedCards;
	}

	public void newRecord(){
		synchronized (this) {
			if (record.size() == 0) {
				List<Long> list0 = new ArrayList<>();
				list0.add(0L);
				record.add(list0);
				dbParamMap.put("record", JSON_TAG);
			} else {
				List<Long> list = record.get(record.size() - 1);
				if (!(list.size() == 0 || (list.size() == 1 && list.get(0) == 0L))) {
					List<Long> list0 = new ArrayList<>();
					list0.add(0L);
					record.add(list0);
					dbParamMap.put("record", JSON_TAG);
				}
			}
		}
	}

	public List<List<Long>> getRecord() {
		return record;
	}

	public void sendActionLog(LogConstants reason, String properties) {
		UdpLogger.getInstance().sendActionLog(this, reason, properties);
	}

	public void addRecord(long logId, int playCount) {
		List<Long> recordList;
		//调整为加入房间时判断
		/**playCount == 1 || **/

		synchronized(this) {
			if (record.isEmpty()) {
				recordList = new ArrayList<>();
				recordList.add(0L);
				record.add(recordList);

			} else {
				recordList = record.get(record.size() - 1);
			}
			recordList.add(logId);
            int saveCount = ResourcesConfigsUtil.loadIntegerValue("ServerConfig","savePlayLogCount",10);
			while (record.size() > saveCount) {
				record.remove(0);
			}
		}
		dbParamMap.put("record", JSON_TAG);
	}

	public void changeUsedCards(long usedCards) {
		synchronized (this) {
			this.usedCards += usedCards;
			this.synUsedCards += usedCards;
		}
		dbParamMap.put("usedCards", synUsedCards);
	}

	public int getDrawLottery() {
		return drawLottery;
	}

	public void changeDrawLottery(int drawLottery) {
		this.drawLottery += drawLottery;
		dbParamMap.put("drawLottery", this.drawLottery);
	}

	public void changeActivity() {
		dbParamMap.put("activity", JSON_TAG);
	}
	
	public void changePlayState() {
		dbParamMap.put("playState", JSON_TAG);
	}
	

	public MyActivity getMyActivity() {
		return myActivity;
	}

	public int getLoginDays() {
		return loginDays;
	}

	public MyMessage getMyMessage() {
		return myMessage;
	}

	public boolean isLoad() {
		return isLoad;
	}

	public void setLoad(boolean isLoad) {
		this.isLoad = isLoad;
	}

	public int getIsOnline() {
		return isOnline;
	}

	public void setIsOnline(int isOnline) {
		setIsOnline(isOnline,true);
	}

	public void setIsOnline(int isOnline,boolean save) {
		this.isOnline = isOnline;
		if (save){
			dbParamMap.put("isOnline", isOnline);
		}
	}

	public GeneratedMessage getRecMsg() {
		return recMsg;
	}

	public void setRecMsg(GeneratedMessage recMsg) {
		this.recMsg = recMsg;
	}


	public boolean isRobot() {
		return isRobot;
	}

    public void setRobot(boolean robot) {
        isRobot = robot;
    }

    public abstract void clearTableInfo();

	public void clearTableInfo(BaseTable table,boolean save){
	    setWinLoseCredit(0L);
	    setCommissionCredit(0L);
		clearTableInfo();
		cleanXipaiData();
	}

	public abstract player_state getState();

	public abstract int getIsEntryTable();

	public abstract void setIsEntryTable(int tableOnline);

	public abstract int getSeat();

	public abstract void setSeat(int randomSeat);

	public abstract void changeState(player_state entry);

	public void changeTableInfo() {
		BaseTable table = getPlayingTable();
		if (table != null)
			table.changePlayers();
	}

	public void changeSeat(int seat) {
		BaseTable table = getPlayingTable();
		if (table != null)
			table.changeCards(seat);
	}

	/**
	 * 初始化下一局
	 */
	public abstract void initNext();

	/**
	 * 手牌
	 *
	 * @return
	 */
	public abstract List<Integer> getHandPais();

	/**
	 * 打牌saveDBInfo
	 *
	 * @return
	 */
	public abstract String toInfoStr();

	/**
	 * 玩家在房间内的res
	 *
	 * @return
	 */
	public abstract PlayerInTableRes.Builder buildPlayInTableInfo();

	/**
	 * 获取称号
	 * @return
	 */
	public int loadDesignation(){
		int name;
		if (totalCount>=180000){
			name = 21;
		}else if(totalCount>=165000){
			name = 20;
		}else if(totalCount>=150000){
			name = 19;
		}else if(totalCount>=135000){
			name = 18;
		}else if(totalCount>=120000){
			name = 17;
		}else if(totalCount>=105000){
			name = 16;
		}else if(totalCount>=95000){
			name = 15;
		}else if(totalCount>=85000){
			name = 14;
		}else if(totalCount>=75000){
			name = 13;
		}else if(totalCount>=65000){
			name = 12;
		}else if(totalCount>=55000){
			name = 11;
		}else if(totalCount>=45000){
			name = 10;
		}else if(totalCount>=36000){
			name = 9;
		}else if(totalCount>=28000){
			name = 8;
		}else if(totalCount>=21000){
			name = 7;
		}else if(totalCount>=15000){
			name = 6;
		}else if(totalCount>=10000){
			name = 5;
		}else if(totalCount>=5000){
			name = 4;
		}else if(totalCount>=2000){
			name = 3;
		}else if(totalCount>=500){
			name = 2;
		}else if(totalCount>=100){
			name = 1;
		} else {
			name = 0;
		}
		return name;
	}


	public PlayerInTableRes.Builder buildPlayInTableInfo1(PlayerInTableRes.Builder res) {
		boolean bl;
		if (GameUtil.isPlayAhGame()){
			String version = ResourcesConfigsUtil.loadServerPropertyValue("gps_version","");
			if (StringUtils.isNotBlank(version)&&StringUtils.isNotBlank(this.version)&&!LoginUtil.checkVersion(version,1,this.version,1)){
				bl = true;
			}else{
				bl = false;
			}
		}else{
			bl = true;
		}

		if (bl){
			if (!StringUtils.isBlank(myExtend.getLatitudeLongitude())) {
				res.setGps(myExtend.getLatitudeLongitude());
			}
			if (this.userState!=null){
				res.setUserSate(this.userState);
			}else{
				res.setUserSate(1);
			}

			res.setDesignation(loadDesignation());
		}
        res.setCoin(loadAllCoin());
		GroupUser groupUser = getGroupUser();
		if(groupUser != null){
		    res.setFrameId(groupUser.getFrameId());
        }
        res.setGold(String.valueOf(loadAllGolds()));
		return res;
	}

	public abstract void initPais(String handPai, String outPai);

	/**
	 * 比赛场结束之后
	 */
	public void endCompetition() {
		endCompetition1();
	}

	/**
	 * 创建者大结算后的处理
	 */
	public void endCreateBigResultBureau(int playBureau) {
		if (playBureau > 10) {
			playBureau = 2;
		} else {
			playBureau = 1;
		}
		if (!isRobot())
			LogDao.getInstance().insertCardsConsumeCards(userId, regBindId, usedCards, playBureau, reginTime);
	}

	/**
	 * 比赛场结束之后
	 */
	public abstract void endCompetition1();

	public int getActionCount() {
		return actionCount;
	}

	public void changeActionCount(int actionCount) {
		this.actionCount += actionCount;
	}

	public long getLoginActionTime() {
		return loginActionTime;
	}

	public void setLoginActionTime(long loginActionTime) {
		this.loginActionTime = loginActionTime;
	}

	public MyExtend getMyExtend() {
		return myExtend;
	}

	public int getRegBindId() {
		return regBindId;
	}

	public void setRegBindId(int regBindId) {
		this.regBindId = regBindId;
	}

	public String getVc() {
		return vc;
	}

	public String getOs() {
		return os;
	}

	/**
	 * 开始下一局准备
	 *
	 * @return
	 */
	public boolean isStartNextReady() {
		return true;
	}

	/**
	 * 可以抽奖的大转盘次数
	 *
	 * @return
	 */
	public int getCanDrawCount() {
		int drawCount = (int) (-usedCards / 15);
		drawCount = drawCount - getDrawLottery();
		return drawCount;
	}

	/**
	 * 登陆检查
	 *
	 * @return
	 */
	public void checkLogin() {
		Date noticeDate = NoticeDao.getInstance().selectNewNoticeTime();

		List<Integer> tips = new ArrayList<>();
		if (noticeDate != null && noticeDate.getTime() > myExtend.getNoticeTime()) {
			// 发送消息提示
			tips.add(TipsEnum.message.getId());
		}

		boolean canGetAward = myActivity.isCanGetAward(ActivityConstant.activity_logindays, getLoginDays());
		if (getCanDrawCount() > 0 || canGetAward) {
			tips.add(TipsEnum.draw.getId());
		}
		writeOutSyncComMessage(WebSocketMsgType.res_code_tips, true, tips);

		// 检查跑马灯
		List<MarqueeMsg> marqueeMsgs = MarqueeManager.getInstance().getMarquee();
		writeOutSyncComMessage(WebSocketMsgType.res_code_marquee, true, JacksonUtil.writeValueAsString(marqueeMsgs));

	}

	public boolean isLc() {
		return GameServerConfig.isDeveloper() && (flatId.startsWith("lc") || flatId.startsWith("cc") || flatId.equals("bd1001"));
	}

	/**
	 * 是否在打比赛场
	 *
	 * @return
	 */
	public boolean isMatching() {
		return false;
	}

	public void loadSigns() {
		Calendar ca = Calendar.getInstance();
		int year = ca.get(Calendar.YEAR);
		int month = ca.get(Calendar.MONTH) + 1;
		ca.set(year, month, 1);
		ca.add(Calendar.DATE, -1);
		int endDay = ca.get(Calendar.DATE);
		String begin = year + "-" + (month>=10?month:("0"+month)) + "-01 00:00:00";
		String end = year + "-" + (month>=10?month:("0"+month)) + "-" + endDay+" 23:59:59";
		loadSigns(begin,end,1);
	}

	public String loadGoldSigns() {
		SevenGoldSign sign=PlayerManager.getInstance().getUserSign(userId);
		if(sign.getLastSignTime()==null)
			sign.setLastSignTime(new Date(0));
		int x = TimeUtil.countTwoDay(sign.getLastSignTime(), new Date());
		if(x==0)
			return "0";
		if((x>1&&sign.getLastSignTime().getTime()!=0)||(x==1&&sign.getSevenSign().endsWith("7"))){
			sign.setSevenSign("0");
			sign.setLastSignTime(new Date(0));
			SevenGoldSignDao.getInstance().updateGoldSign(sign);
		}
		return "1";
	}

	public synchronized void sign(int signDay,SourceType sourceType){
	    try {
	    	SevenGoldSign sign=PlayerManager.getInstance().getUserSign(userId);
            int x = TimeUtil.countTwoDay(sign.getLastSignTime(), new Date());
            if (x==0) {
                writeErrMsg(LangHelp.getMsg(LangMsg.code_39));
                return;
            }else if(x>1&&signDay!=1){
                writeErrMsg("时间已刷新请重新打开签到界面");
                return;
            }
            Map<Integer, Integer> sevenDayConfig = MissionConfigUtil.getSevenDayConfig();
            String sevenSign = sign.getSevenSign();
            int goldNum=0;
            int yesterday=0;
            if(sevenSign.length()>0){
                String lastDay = sevenSign.substring(sevenSign.length()-1);
                yesterday = Integer.parseInt(lastDay);
            }
            if(yesterday>=7){
                return;
            }else {
                sevenSign=sevenSign+","+(yesterday+1);
            }
			sign.setSevenSign(sevenSign);
			sign.setLastSignTime(new Date());
            goldNum=sevenDayConfig.get(yesterday+1);
            if(sourceType==SourceType.video_sign)
            	goldNum*=2;
            SevenGoldSignDao.getInstance().updateGoldSign(sign);
            // 领取金币
            changeGold(goldNum, sourceType);
			mission.addSignMission();
            // 发送签到成功的
            writeComMessage(WebSocketMsgType.res_code_goldsign,yesterday+1,goldNum);
            LogUtil.msgLog.info("sign|"+userId+"|"+signDay);
        }catch (Exception e){
            LogUtil.errorLog.error("#sign:", e);
        }
	}

	public void writeGoldSignInfo() {
	    try{
			SevenGoldSign sign=PlayerManager.getInstance().getUserSign(userId);
			if(sign.getLastSignTime()==null)
				sign.setLastSignTime(new Date(0));
            int x = TimeUtil.countTwoDay(sign.getLastSignTime(), new Date());
            String isOut="0";
            if(x>0) {
                isOut = "1";
            }
            String lastDay="0";
            String s = sign.getSevenSign();
            if(s.length()>0){
                if(x==0)
                    lastDay= s.substring(s.length()-1);
                else if(x==1){
                    lastDay= s.substring(s.length()-1);
                    if(lastDay.equals("7")){
                        lastDay="0";
						sign.setSevenSign(lastDay);
                    }
                }else {
					sign.setSevenSign("0");
                }
            }
            writeComMessage(WebSocketMsgType.res_code_goldsigninfo, isOut, "0", lastDay,"1");
        }catch (Exception e){
            LogUtil.errorLog.error("#writeGoldSignInfo:", e);
        }
	}

	public void writeGoldSignInfo(String isOut, String isInRoom) {
		SevenGoldSign sign=PlayerManager.getInstance().getUserSign(userId);
		String lastDay="0";
		String s = sign.getSevenSign();
		if(s.length()>0)
			lastDay= s.substring(s.length()-1);
		writeComMessage(WebSocketMsgType.res_code_goldsigninfo, isOut, isInRoom, lastDay,"0");
	}

	/**
	 * 获得玩家的当月已签到情况
	 * @param begin 开始时间
	 * @param end 结束时间
	 * @param type 按天1，年月日2
	 */
	public void loadSigns(String begin,String end,int type) {
		synchronized (this){
			Calendar ca = Calendar.getInstance();
			List<UserSign> lists = UserSignDao.getInstance().getUserSign(userId, begin, end);
			if (signs == null) {
				signs = new ArrayList<>();
			} else {
				signs.clear();
			}
			ymdSigns.clear();

			if (lists != null && lists.size() != 0) {
				if (type==1){
					for (UserSign sign : lists) {
						Calendar now = Calendar.getInstance();
						ca.setTime(sign.getSignTime());
						// 日期正确
						if (ca.get(Calendar.DATE) <= now.get(Calendar.DATE)) {
							// 避免同一天的重复记录
							if (!signs.contains(ca.get(Calendar.DATE))) {
								signs.add(ca.get(Calendar.DATE));
							}
						}
					}
				}else{
					SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");
					for (UserSign sign : lists) {
						if (sign.getSignTime()!=null){
							String ymd=sdf.format(sign.getSignTime());
							if (!ymdSigns.contains(ymd)){
								ymdSigns.add(ymd);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * 发送签到信息
	 * @param isOut 是否弹出
	 * @param isInRoom
	 * @param type 按天1，年月日2
	 */
	public void writeSignInfo(String isOut, String isInRoom,int type) {
		// 发送签到的消息
		Calendar ca = Calendar.getInstance();

		if (type==1){
			int year = ca.get(Calendar.YEAR);
			int month = ca.get(Calendar.MONTH) + 1;
			int date = ca.get(Calendar.DATE);
			ca.set(Calendar.DATE, 1);
			int week = ca.get(Calendar.DAY_OF_WEEK) - 1 == 0 ? 7 : ca.get(Calendar.DAY_OF_WEEK) - 1;
			int days = ca.getActualMaximum(Calendar.DATE);
			writeComMessage(WebSocketMsgType.res_code_signinfo, signs, ""+ year, ""+ month, ""+ date, ""+ week, ""+ days, isOut, isInRoom);
		}else{
			SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");
			String ymdDate=sdf.format(ca.getTime());
			int today=0;
			int num=1;

			try {
				int len = ymdSigns.size();
				if (len > 0) {
					if (ymdSigns.contains(ymdDate)) {
						today = 1;
					}

					SimpleDateFormat sdf0 = new SimpleDateFormat("yyyy-MM-dd");
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(ca.getTime());
					calendar.add(Calendar.DAY_OF_YEAR, -1);
					String ymdDate0 = sdf0.format(calendar.getTime());
					List<UserSign> userSigns = UserSignDao.getInstance().getUserSign(getUserId(),ymdDate0+" 00:00:00",ymdDate0+" 23:59:59");

					if (userSigns==null || userSigns.size()==0){
					}else {
						String[] msgs = userSigns.get(0).getExtend().split(",");
						if (msgs.length >= 3 && NumberUtils.isDigits(msgs[2])) {
							int signDays = NumberUtils.toInt(ResourcesConfigsUtil.loadServerPropertyValue("signDays"), 7);
							int cur = NumberUtils.toInt(msgs[2]);
							num = cur >= signDays ? 1 : (cur + 1);
						} else {
							if (len == 1) {
								ca.add(Calendar.DAY_OF_YEAR, -1);
								if (ymdSigns.get(0).equals(sdf.format(ca.getTime()))) {
									num = 2;
								}
							} else {
								len = len - 1;
								ca.set(Calendar.HOUR_OF_DAY, 12);

								boolean bl = true;
								if (Integer.parseInt(ymdSigns.get(len)) >= Integer.parseInt(ymdDate)) {
									today = 1;
								}else{
									today = 0;
									ca.add(Calendar.DAY_OF_YEAR, -1);
									if (!ymdSigns.get(len).equals(sdf.format(ca.getTime()))){
										num = 1;
										bl = false;
									}
								}

								if (bl){
									int series = today == 1 ? 0 : 1;
									for (int i = len; i > 0; i--) {
										ca.setTime(sdf.parse(ymdSigns.get(i)));
										ca.add(Calendar.DAY_OF_YEAR, -1);
										if (!ymdSigns.get(i - 1).equals(sdf.format(ca.getTime()))) {
											break;
										} else {
											series++;
										}
									}

									num = series + 1;
								}
							}
						}
					}
				}
			}catch (Exception e){
				LogUtil.errorLog.error("Exception:"+e.getMessage(),e);
			}

			writeComMessage(WebSocketMsgType.res_code_signinfo,today,num,isOut,isInRoom,ymdDate,ymdSigns);
		}
	}


	/**
	 * 签到
	 */
	public void sign(int date, Date now, int diamond, int gold,int index) {
		// 在已签集合里加入今天
		if (date>10000000){
			ymdSigns.add(String.valueOf(date));
		}else{
			signs.add(date);
		}

		UserSign userSign = new UserSign();
		userSign.setUserId(userId);
		userSign.setSignTime(now);
		StringBuilder sb = new StringBuilder();
		if (diamond>0) {
			// 更新钻石
			changeCards(diamond, 0, false, CardSourceType.sign);
		}
		sb.append(diamond).append(",");
		if (gold>0) {
			// 更新金币
			changeGold(gold,0, 998);
		}
		sb.append(gold).append(",");
		sb.append(index);
		userSign.setExtend(sb.toString());
		// 更新签到记录
		UserSignDao.getInstance().sign(userSign);
	}

	/**
	 * 获取玩家所有房卡或钻石
	 * @return
	 */
	public long loadAllCards(){
		return freeCards + cards;
	}

    /**
     * 获取玩家所有金币
     * @return
     */
    public long loadAllCoin(){
        return freeCoin + coin;
    }

	/**
	 * 获取玩家所有金币
	 * @return
	 */
	public long loadAllGolds(){
		if (goldPlayer == null){
			return 0;
		}else{
			return goldPlayer.getAllGold();
		}
	}

	/**
	 * 加载金币玩家身份
	 */
    public void loadGoldPlayer(boolean canCreate) {
        GoldPlayer goldInfo;
        if (GoldConstans.isGoldSiteOpen()) {
            try {
                goldInfo = GoldDao.getInstance().selectGoldUserByUserId(userId);
                int drawRemedyCount = GoldDao.getInstance().selectDrawRemedyCount(TimeUtil.formatDayTime2(TimeUtil.now()), userId);
                if (goldInfo == null) {
                    goldInfo = new GoldPlayer();
                    goldInfo.setUserId(getUserId());
                    goldInfo.setUserName(getName());
                    goldInfo.setUserNickName(getName());
                    goldInfo.setHeadimgurl(getHeadimgurl());
                    goldInfo.setSex(getSex());


                    long give = GoldRoomUtil.giveGoldOnNew;

                    goldInfo.setFreeGold(give);
                    goldInfo.setRegTime(new Date());
                    goldInfo.setLastLoginTime(goldInfo.getRegTime());
                    GoldDao.getInstance().createGoldUser(goldInfo);

                    setGoldPlayer(goldInfo);
                    writeGoldMessage(goldInfo.getFreeGold());
                } else {
                    goldInfo.setLastLoginTime(new Date());
                    if (canCreate) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("lastLoginTime", goldInfo.getLastLoginTime());
                        map.put("userId", userId);
                        GoldDao.getInstance().updateGoldUser(map);
                    }
                    goldInfo.setDrawRemedyCount(drawRemedyCount);
                    setGoldPlayer(goldInfo);
                }
            } catch (Exception e) {
                LogUtil.e("loadGoldPlayer err-->", e);
            }
        } else {
            goldInfo = new GoldPlayer();
            goldInfo.setUserId(userId);
            setGoldPlayer(goldInfo);
        }
    }

	@Deprecated
	public void changeGold(int num, int playType) {
		changeGold(0, num, true, playType, true);
	}
    @Deprecated
	public void changeGold(int freeNum, int num, int playType) {
		changeGold(freeNum, num, true, playType, true);
	}

    @Deprecated
    public void changeGold(int freeNum, int num, boolean isRecord, int playType, boolean isWrite) {
        changeGold(Long.valueOf(freeNum),Long.valueOf(num),isRecord,playType,isWrite,null);
    }

    /**
     * 刷新金币
     */
    public void refreshGoldFromDb(){
        GoldPlayer goldPlayer = getGoldPlayer();
        goldPlayer.refreshGoldFromDb();
    }

    public boolean changeGold(long num, int playType,SourceType sourceType) {
        return changeGold(0, num, true, playType, true,sourceType);
    }

    public boolean changeGold(long freeNum, long num, int playType,SourceType sourceType) {
        return changeGold(freeNum, num, true, playType, true,sourceType);
    }

	public boolean changeGold(long freeNum,SourceType sourceType) {
		return changeGold(freeNum, 0, true, 0, true,sourceType);
	}

    /**
     * 改变金币
     * 扣金币时，使用-num
     *
     * @param freeNum
     * @param num
     * @param playType
     * @param isRecord
     * @param isWrite  是否推送
     * @param sourceType
     */
    public boolean changeGold(long freeNum, long num, boolean isRecord, int playType, boolean isWrite,SourceType sourceType) {
        GoldPlayer goldPlayer = getGoldPlayer();
        Consume consume = new Consume();
        consume.setSourceType(sourceType);
        consume.setFreeValue(freeNum);
        consume.setValue(num);
        consume.setRecord(isRecord);
        consume.setPlayType(playType);
        consume.setWrite(isWrite);
        consume.setPlayer(this);
        return goldPlayer.changeGold(consume);
    }

	/**
	 * 更新玩家的金币
	 */
	private void changeGold(long freeGold, long gold) {
		GoldPlayer player = getGoldPlayer();
		player.setFreeGold(freeGold);
		player.setGold(gold);
		getMyExtend().updateUserMaxGold();
	}

	/**
	 * 改变玩家消耗的金币
	 */
	private void changeUsedGold(long gold) {
		GoldPlayer player = getGoldPlayer();
		player.changeUsedGold(gold);
	}

	/**
	 * 增加金币场总局数
	 */
	protected void changeGoldPlayCount() {
		getGoldPlayer().changePlayCount();
		getMyExtend().getUserTaskInfo().alterDailyGoldGameNum();
		changeExtend();
	}

	/**
	 * 增加金币场胜局数
	 */
	protected void changeGoldWinCount() {
		getGoldPlayer().changeWinCount();
		getMyExtend().getUserTaskInfo().alterDailyWinGameNum();
		changeExtend();
	}

	/**
	 * 增加金币场败局数
	 */
	protected void changeGoldLoseCount() {
		getGoldPlayer().changeLoseCount();
	}

	/**
	 * 芒果跑得快增加积分
	 * @param addNum 获取积分数
	 * @param sourceType 积分来源  需约定来源类型(1金币场2比赛场)
	 */
	public void addJiFen(int addNum, int sourceType){
		try {
			Date currentDate = new Date();
			UserExtend userExtend = new UserExtend();
			userExtend.setUserId(String.valueOf(userId));
			userExtend.setCreatedTime(currentDate);
			userExtend.setModifiedTime(currentDate);
			userExtend.setMsgDesc(UserResourceType.JIFEN.getName());
			userExtend.setMsgKey(UserResourceType.JIFEN.name());
			userExtend.setMsgType(UserResourceType.JIFEN.getType());
			userExtend.setMsgState("1");
			userExtend.setMsgValue(String.valueOf(addNum));
			UserDao.getInstance().saveOrUpdateUserExtend(userExtend);
			// 积分获取增加操作日志
			JiFenRecordLogDao.getInstance().saveJiFenRecordLog(new JiFenRecordLog(userId, addNum, sourceType, currentDate));
			upgradeExp(addNum);
		}catch (Exception e){
			LogUtil.errorLog.error("Exception:"+e.getMessage(),e);
		}
	}

	/**
	 * 芒果跑得快更新段位积分
	 * @param addExp
	 * @return
	 */
	protected boolean upgradeExp(int addExp) {
		if (addExp < 1) {
			return false;
		}
		GoldPlayer goldPlayer = getGoldPlayer();
		int curGrade = goldPlayer.getGrade();
		int curGradeExp = goldPlayer.getGradeExp();
		GradeExpConfigInfo curConfigInfo = GradeExpConfig.getGradeExpConfigInfo(curGrade);
		if (curGrade == GradeExpConfig.maxGrade) {// 已满级
			if (curGradeExp == curConfigInfo.getNeedExp()) {//满经验
				return false;
			}
			int exp = curGradeExp + addExp;
			if (exp > curConfigInfo.getNeedExp()) {
				exp = curConfigInfo.getNeedExp();
			}
			goldPlayer.setGradeExp(exp);
			// 推送前端经验值更新消息
			return false;
		}
		// 未满级
		int exp = curGradeExp + addExp;
		boolean upgrade = false;
		while (exp >= curConfigInfo.getNeedExp()) {
			upgrade = true;
			exp = exp - curConfigInfo.getNeedExp();
			goldPlayer.changeGrade();
			//下一级配置
			curConfigInfo = GradeExpConfig.getGradeExpConfigInfo(goldPlayer.getGrade());
			if (goldPlayer.getGrade() == GradeExpConfig.maxGrade
					&& exp >= GradeExpConfig.getGradeExpConfigInfo(GradeExpConfig.maxGrade).getNeedExp()) {
				exp = curConfigInfo.getNeedExp();
				break;
			}
		}
		goldPlayer.setGradeExp(exp);
		if (upgrade) {// 段位升级
			// 推送前端经验值更新消息
			GradeExpConfigInfo gradeExpConfigInfo = GradeExpConfig.getGradeExpConfigInfo(goldPlayer.getGrade());
			String gradeDesc = (gradeExpConfigInfo != null ) ? gradeExpConfigInfo.getDesc() : "";
			this.writeGradeMessage(goldPlayer.getGrade(), gradeDesc);
		}
		GoldDao.getInstance().updateGoldUserGrade(userId,goldPlayer.getGrade(),goldPlayer.getGradeExp());
		return upgrade;
	}

	public void writeRemoveBindMessage() {
		writeComMessage(WebSocketMsgType.res_code_removebind);
	}

	/**
	 * 获取金币场机器人
	 *
	 * @return
	 */
	public GoldPlayer loadGoldRobot(){
		if (goldPlayer == null){
			goldPlayer = new GoldPlayer();
			table = getPlayingTable();

			if (table!=null){
				List<Integer> list = GameConfigUtil.getIntsList(2, table.getModeId());
				if (list!=null&&list.size()>0){
					goldPlayer.setFreeGold(list.get(0).longValue());
				}
			}

			goldPlayer.setUserId(userId);
		}
		return goldPlayer;
	}

	/**
	 * 获取累计分
	 *
	 * @return
	 */
	public int loadAggregateScore(){
		return totalPoint;
	}

	public int loadTzScore() {
		return 0;
	}

    /**
     * 是否在打比赛场
     *
     * @return
     */
    public boolean isPlayingMatch() {
        if (StringUtil.isBlank(loginExtend)) {
            return false;
        }
        JSONObject jsonObject = JSON.parseObject(loginExtend);
        if (jsonObject.containsKey("grmId")) {
            return true;
        }
        return false;
    }

	/**
	 * 获取玩家当前比赛场ID
	 * @return
	 */
	public String loadMatchId(){
		return StringUtils.isNotBlank(loginExtend)?JSON.parseObject(loginExtend).getString("match"):null;
	}

	public boolean joinMatch(String configId,boolean force){
		try {
			synchronized (this) {
				if (org.apache.commons.lang3.StringUtils.isBlank(loginExtend)) {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("match", configId);
					String loginExt = jsonObject.toString();
					Map<String, Object> map = new HashMap<>();
					map.put("loginExtend", loginExt);

					this.loginExtend = loginExt;
					this.matchId = configId;
					UserDao.getInstance().updateUser(String.valueOf(userId), map);
				} else {
					JSONObject jsonObject = JSON.parseObject(loginExtend);
					if (force || org.apache.commons.lang3.StringUtils.isBlank(jsonObject.getString("match"))) {
						jsonObject.put("match", configId);
						String loginExt = jsonObject.toString();
						Map<String, Object> map = new HashMap<>();
						map.put("loginExtend", loginExt);
						this.loginExtend = loginExt;
						this.matchId = configId;

						UserDao.getInstance().updateUser(String.valueOf(userId), map);
					}
				}
			}
		}catch (Exception e){
			LogUtil.errorLog.error("Exception:"+e.getMessage(),e);
		}
		return false;
	}

	/**
	 *加入比赛场
	 * @return
	 */
	public boolean joinMatch(String configId){
		return joinMatch(configId,false);
	}

	/**
	 *退出比赛场
	 * @return
	 */
	public boolean quitMatch(){
		synchronized (this){
			if (org.apache.commons.lang3.StringUtils.isNotBlank(loginExtend)) {
				JSONObject jsonObject = JSON.parseObject(loginExtend);
				if (jsonObject.remove("match")!=null) {
					String loginExt = jsonObject.toString();
					Map<String, Object> map = new HashMap<>();
					map.put("loginExtend", loginExt);

					try {
						this.loginExtend = loginExt;
						this.matchId = null;
						UserDao.getInstance().updateUser(String.valueOf(userId), map);
					}catch (Exception e){
						LogUtil.errorLog.error("Exception:"+e.getMessage(),e);

						return false;
					}
				}else{
					this.matchId = null;
				}
			}else{
				this.matchId = null;
			}
		}
		return true;
	}

	/**
	 * 把tempCredit转移到credit上
	 *
	 * @return
	 */
	public int settleTempCredit() {
		int updateResult = 0;
		try {
			updateResult = GroupDao.getInstance().settleTableTempCredit(userId);
		} catch (Exception e) {
			LogUtil.errorLog.error("settleTempCredit|error|1|" + userId , e);
		}
		if (updateResult > 0) {
			LogUtil.msgLog.info("settleTempCredit|succ|" + userId);
		}
		return updateResult;
	}

	/**
	 * 加载玩法配置，子类必须重写
	 * todo
	 */
	public static void loadWanfaPlayers(Class<? extends Player> cls){
	}

    public long getWinLoseCredit() {
        return winLoseCredit;
    }

    public void setWinLoseCredit(long winLoseCredit) {
        int rate = 1;
        BaseTable table = this.getPlayingTable();
	    if(table != null && table.getCredit100() == 0){
	        rate = 100;
        }
        this.winLoseCredit = winLoseCredit * rate;
        long credit = (getWinLoseCredit() + getCommissionCredit());
        long coin = 0;
        if (table != null) {
            coin = credit / table.getCreditRate();
        } else {
            coin = credit / 1;
        }
        setWinLoseCoin(coin);
    }

    public long getWinLoseCoin() {
        return winLoseCoin;
    }

    public void setWinLoseCoin(long winLoseCoin) {
        this.winLoseCoin = winLoseCoin;
    }

    public long getCommissionCredit() {
        return commissionCredit;
    }

    public void setCommissionCredit(long commissionCredit) {
        int rate = 1;
        BaseTable table = this.getPlayingTable();
        if(table != null && table.getCredit100() == 0){
            rate = 100;
        }
        this.commissionCredit = commissionCredit * rate;

        long credit = (getWinLoseCredit() + getCommissionCredit());
        long coin = 0;
        if (table != null) {
            coin = credit / table.getCreditRate();
        } else {
            coin = credit / 1;
        }
        setWinLoseCoin(coin);
    }

    public void setLastActionBureau(int lastActionBureau) {
		if(lastActionBureau < 0)
			lastActionBureau = 0;
		this.lastActionBureau = lastActionBureau;
	}

	public int getLastActionBureau() {
		return lastActionBureau;
	}

	public int getAutoPlayCheckedTime() {
        return autoPlayCheckedTime;
    }

    public void setAutoPlayCheckedTime(int autoPlayCheckedTime) {
        this.autoPlayCheckedTime = autoPlayCheckedTime;
    }

    public void addAutoPlayCheckedTime(int time){
        this.autoPlayCheckedTime += time;
    }

    public boolean isAutoPlayCheckedTimeAdded() {
        return autoPlayCheckedTimeAdded;
    }

    public void setAutoPlayCheckedTimeAdded(boolean autoPlayCheckedTimeAdded) {
        this.autoPlayCheckedTimeAdded = autoPlayCheckedTimeAdded;
    }

    public boolean isAutoPlayChecked() {
        return autoPlayChecked;
    }

    public void setAutoPlayChecked(boolean autoPlayChecked) {
        this.autoPlayChecked = autoPlayChecked;
    }
    
    

    public int getPlayState() {
		return playState;
	}

	public void setPlayState(int playState) {
		this.playState = playState;
		if(playState==1) {
			changePlayState();
		}
	}

	/**
     * 改变房卡数量（消耗房卡不能使用该方法）
     *
     * @param cards
     * @param freeCards
     */
    public void notifyChangeCards(long cards, long freeCards, boolean saveRecord, CardSourceType sourceType) {
        synchronized (this) {
            RegInfo info = UserDao.getInstance().selectUserByUserId(userId);
            if (info != null) {
                this.cards = info.getCards();
                this.freeCards = info.getFreeCards();
            }
        }
        writeCardsMessage((int) (freeCards + cards));
        if (saveRecord) {
            PlayerManager.getInstance().addUserCardRecord(new UserCardRecordInfo(userId, this.freeCards, this.cards, (int) freeCards, (int) cards, 0, sourceType));
        }
    }

    public long getJoinTime() {
        return joinTime;
    }

    public void setJoinTime(long joinTime) {
        this.joinTime = joinTime;
    }

	public int getDissCount() {
		return dissCount;
	}


	public void addDissCount() {
		this.dissCount += 1;
	}
	public void setDissCount(int dissCount) {
		this.dissCount = dissCount;
	}



    public int getGoldResult() {
		return goldResult;
	}

	public void setGoldResult(int goldResult) {
		this.goldResult = goldResult;
	}

	/**
     * 推送信用分变更
     *
     * @param groupId
     */
    public void notifyCreditUpdate(long groupId) {
//        long curCredit = GroupCreditDao.getInstance().loadGroupUserCredit(groupId, getUserId());
        GroupUser groupUser = GroupDao.getInstance().loadGroupUser(getUserId(), groupId);
        writeCreditMessage(groupId, groupUser.getCredit());

        if(groupUser != null && groupUser.getGroupId() == groupId){
            setGroupUser(groupUser);
        }
    }

    /**
     * 推送信用分变更
     *
     * @param credit
     */
    public void writeCreditMessage(long groupId, long credit) {
        ComRes.Builder res = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_credit_update, String.valueOf(groupId), String.valueOf(credit));
        writeSocket(res.build());
    }


    /**
     * 修改玩家金币
     * 扣金币，使用coin参数
     * @param freeCoin
     * @param coin
     * @param isWrite
     * @param playType
     * @param sourceType
     * @return
     */
    public synchronized boolean changeUserCoin(final long freeCoin, final long coin, boolean isWrite, int playType, CoinSourceType sourceType) {
        if (freeCoin == 0 && coin == 0) {
            return true;
        }
        if (freeCoin < 0) {
            throw new RuntimeException("changeUserCoin|error|" + freeCoin + "|" + coin);
        }
        long coin1 = 0;// 免费金币
        long coin2 = 0;// 非免费金币
        if (coin < 0) {
            long temp = this.freeCoin + coin;
            if (temp >= 0) {
                // 免费金币足够
                this.freeCoin = temp;
                coin2 = 0;
                coin1 = -coin;
            } else {
                //
                this.freeCoin = 0;
                coin2 = -temp;
                coin1 = (-coin) - coin2;
            }
            this.coin -= coin2;
            this.usedCoin += -coin;
            dbParamMap.put("usedCoin", usedCoin);
        } else {
            this.coin += coin;
            coin2 += -coin;
        }

        this.freeCoin += freeCoin;
        coin1 += -freeCoin;

        StringBuilder sb = new StringBuilder("changeUserCoin|start|");
        sb.append("|").append(userId);
        sb.append("|").append(playingTableId);
        sb.append("|").append(isWrite);
        sb.append("|").append(playType);
        sb.append("|").append(freeCoin);
        sb.append("|").append(coin);
        sb.append("|").append(this.freeCoin);
        sb.append("|").append(this.coin);
        sb.append("|").append(coin1);
        sb.append("|").append(coin2);
        LogUtil.msgLog.info(sb.toString());

        if (coin2 != 0 || coin1 != 0) {
            if (UserDao.getInstance().changeUserCoin(userId, -coin2, -coin1) > 0) {
                if (isWrite) {
                    writeCoinMessage();
                }
                LogUtil.msgLog.info(sb.toString().replace("start", "end"));
            } else {
                LogUtil.errorLog.error(sb.toString().replace("start", "error"));
            }
        }
        boolean saveLog = true;
        if(saveLog) {
            Map<String, Object> record = new HashMap<>();
            record.put("userId", userId);
            record.put("freeCoin", this.freeCoin);
            record.put("coin", this.coin);
            record.put("addFreeCoin", -coin1);
            record.put("addCoin", -coin2);
            record.put("playType", playType);
            record.put("sourceType", sourceType.getSourceType());
            record.put("sourceName", sourceType.getSourceName());
            record.put("createTime", new Date());
            PlayerManager.getInstance().addUserCoinRecord(record);
        }
        return true;
    }

    /**
     * 推送前台房卡更新
     */
    public void writeCoinMessage() {
        ComRes.Builder res = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_coin, String.valueOf(loadAllCoin()));
        writeSocket(res.build());
    }

    /**
     * 亲友圈玩家升级
     */
    public void notifyGroupUserLevelUp(String groupId, String level) {
        // 推送消息级前端
        String addCoin = "2000";
        writeComMessage(WebSocketMsgType.res_com_group_user_level_up, level, addCoin, groupId);
    }

    public boolean isNeedCheckTing() {
        return needCheckTing;
    }

    public void setNeedCheckTing(boolean needCheckTing) {
        this.needCheckTing = needCheckTing;
        if(needCheckTing){
            clearTingMsg();
        }
    }

    public PlayCardResMsg.DaPaiTingPaiRes getDaTingMsg() {
        return daTingMsg;
    }

    public void setDaTingMsg(PlayCardResMsg.DaPaiTingPaiRes daTingMsg) {
        this.daTingMsg = daTingMsg;
    }

    public PlayCardResMsg.TingPaiRes getTingMsg() {
        return tingMsg;
    }

    public void setTingMsg(PlayCardResMsg.TingPaiRes tingMsg) {
        this.tingMsg = tingMsg;
    }

    public void clearTingMsg(){
        daTingMsg = null;
        tingMsg = null;
    }

	/**
	 * 金币场端午活动排行榜查询
	 */
	public void queryGoldRoomActivityRankList() {
		try {
			List<GoldAcitivityRankResult> ranklist =null ;
			long nowtime = System.currentTimeMillis();
			long nextQueryTime = ActivityUtil.getDuanWu_GoldRoomActivityLastQueryTime()+60*1000;
			if(ActivityUtil.getDuanWu_GoldRoomActivityLastQueryTime()==0 || nowtime>=nextQueryTime || null == ActivityUtil.getDuanWu_GoldRoomActivityRankList() ){
				ranklist = GoldRoomActivityDao.getInstance().queryActivityRankMap();
				ActivityUtil.setDuanWu_GoldRoomActivityLastQueryTime(System.currentTimeMillis());
				ActivityUtil.setDuanWu_GoldRoomActivityRankList(ranklist);
			}else{
				ranklist =ActivityUtil.getDuanWu_GoldRoomActivityRankList();
			}
			boolean myRankInTop50 = false;
			long myRankInTop50no =0;
			HashMap<String,Object> map = new HashMap<>();
			map.put("userid",getUserId());
			List<GoldAcitivityRankResult>  myranklist = GoldRoomActivityDao.getInstance().queryActivityRankMapByUserId(map);
			GoldAcitivityRankResult myrankNo =null;
			JSONObject json = new JSONObject();
			List<GoldAcitivityRankResult> top50 =null;
			if(ranklist.size()<50){
				top50 = ranklist;
			}else {
				top50 = ranklist.subList(0,50);
			}
			for (GoldAcitivityRankResult result:ranklist ) {
				if(result.getUserid()==getUserId()){
					myRankInTop50 =true;
					myRankInTop50no =result.getRowno();
					break;
				}
			}
			if(null!=myranklist && myranklist.size()!=0){
				myrankNo = myranklist.get(0);
			}else{
				myrankNo = new GoldAcitivityRankResult();
				myrankNo.setRowno(0);
				myrankNo.setName(getName());
				myrankNo.setUserid(getUserId());
				myrankNo.setHeadimgurl(getHeadimgurl());
				myrankNo.setActivityItemNum(0);
			}
			if(myRankInTop50){
				//前50显示名次
				myrankNo.setRowno(myRankInTop50no);
			}else{
				//50+名次
				myrankNo.setRowno(0);
			}

			List<Activity> ac = ActivityUtil.getAllActivityByThem(ActivityUtil.themZongZi);//
			if(null ==ac || ac.size()==0){
				return;
			}
			Date endDate = ac.get(0).getEndTime();
			Date now = new Date();
			boolean timeFlag = false;
			if (now.compareTo(endDate) > 0) {
				timeFlag = true;
			}
			boolean meInTop10 =myRankInTop50no>0 && myRankInTop50no<=10 && timeFlag ;
			json.put("ranklist",top50);
			json.put("myrank",myrankNo);
			int intParam = 0;
			if(meInTop10){
				intParam =1;//有奖励
				List<GoldRoomActivityUserItem> rewardItem = 	GoldRoomActivityDao.getInstance().loadItemByUserId(getUserId());
				if(null!=rewardItem && rewardItem.size()>0){
					GoldRoomActivityUserItem me = rewardItem.get(0);
					if(me.getIsReward()>0){
						intParam = 0;
					}
				}
			}

			ComRes.Builder builder2 = SendMsgUtil.buildComRes(WebSocketMsgType.cs_duanwu_gold_room_acitiviyt,intParam,json.toJSONString());
			writeSocket(builder2.build());
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.msgLog.error("DuanWu queryGoldRoomActivityRankList|error"+e.getMessage());
		}

	}


	/**
	 * 金币场活动获得奖励
	 */
	public synchronized void getReWard() {
		try {
			//活动时间。
			List<Activity> ac = ActivityUtil.getAllActivityByThem(ActivityUtil.themZongZi);//
			if(null ==ac || ac.size()==0){
				return;
			}
			Date endDate = ac.get(0).getEndTime();
			Date now = new Date();
			if (now.compareTo(endDate) > 0 ) {
				//领取奖励时间
				List<GoldAcitivityRankResult> ranklist = GoldRoomActivityDao.getInstance().queryActivityRankMap();
				long rankno = 0;
				GoldAcitivityRankResult rankItem = null;
				for (int i = 0; i < ranklist.size(); i++) {
					GoldAcitivityRankResult item = ranklist.get(i);
					if (item.getUserid() == getUserId() && item.getRowno()<=20 ) {
						//排名前20且没领取奖励
						rankno = item.getRowno();
						rankItem = item;
						break;
					}
				}
				List<GoldRoomActivityUserItem> reslut =  GoldRoomActivityDao.getInstance().loadItemByUserId(getUserId());
				if(null==reslut || reslut.size()==0){
					writeErrMsg("您不在前20榜单内");
					return;
				}else{
					int isreward =reslut.get(0).getIsReward();
					if(isreward>0){
						writeErrMsg("您已经领取！");
						return;
					}
				}
				if (rankno > 0 && rankno <= 10) {
					//领取奖励。
					int bean = 0;
					if (rankno == 1) {
						bean = 100000;
					} else if (rankno == 2) {
						bean = 50000;
					} else if (rankno == 3) {
						bean = 20000;
					} else if (rankno == 4 || rankno == 5 || rankno == 6) {
						bean = 10000;
					}else if(rankno >=7){
						bean = 5000;
					}
					changeGold(bean, SourceType.DuanWuActivityGoldRoom);
					GoldRoomActivityUserItem myrankItem = reslut.get(0);
					myrankItem.setIsReward(1);
					GoldRoomActivityDao.getInstance().updateReward(myrankItem);
					duanwu_glodRoomActivityNoticeMsg(1,bean);
					//领取完奖励取消红点
					ComRes.Builder builder2 = SendMsgUtil.buildComRes(WebSocketMsgType.cs_duanwu_gold_room_acitiviyt,0);
					writeSocket(builder2.build());
					LogUtil.msgLog.info("DuanWuActivityReward_GoldRoom|" + userId + "|rankno=" + rankno + "|bean=" + bean);
				}
			} else {
				writeErrMsg("活动尚未结束~");
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.msgLog.error("DuanWu GoldRoomActivityGetReWard|error"+e.getMessage());
		}
	}

	/**
	 * 金币场7xi活动排行榜查询
	 */
	public void queryGoldRoom7xiActivityRankList() {
		try {
			List<GoldAcitivityRankResult> ranklist =null ;
			long nowtime = System.currentTimeMillis();
			long nextQueryTime = ActivityUtil.getGoldRoom7xiActivityLastQueryTime()+60*1000;
			if(ActivityUtil.getGoldRoom7xiActivityLastQueryTime()==0 || nowtime>=nextQueryTime || null == ActivityUtil.getDuanWu_GoldRoomActivityRankList() ){
				ranklist = GoldRoomActivityDao.getInstance().queryActivityRankMapByThemAndUserId(ActivityUtil.them7xi,null);
				ActivityUtil.setGoldRoom7xiActivityLastQueryTime(System.currentTimeMillis());
				ActivityUtil.setGoldRoom7xiActivityRankList(ranklist);
			}else{
				ranklist =ActivityUtil.getDuanWu_GoldRoomActivityRankList();
			}
			boolean myRankInTop50 = false;
			long myRankInTop50no =0;
			HashMap<String,Object> map = new HashMap<>();
			map.put("userid",getUserId());
			List<GoldAcitivityRankResult>  myranklist = GoldRoomActivityDao.getInstance().queryActivityRankMapByThemAndUserId(ActivityUtil.them7xi,String.valueOf(getUserId()));
			GoldAcitivityRankResult myrankNo =null;
			JSONObject json = new JSONObject();
			List<GoldAcitivityRankResult> top50 =null;
			if(ranklist.size()<50){
				top50 = ranklist;
			}else {
				top50 = ranklist.subList(0,50);
			}
			for (GoldAcitivityRankResult result:ranklist ) {
				if(result.getUserid()==getUserId()){
					myRankInTop50 =true;
					myRankInTop50no =result.getRowno();
					break;
				}
			}
			if(null!=myranklist && myranklist.size()!=0){
				myrankNo = myranklist.get(0);
			}else{
				myrankNo = new GoldAcitivityRankResult();
				myrankNo.setRowno(0);
				myrankNo.setName(getName());
				myrankNo.setUserid(getUserId());
				myrankNo.setHeadimgurl(getHeadimgurl());
				myrankNo.setActivityItemNum(0);
			}
			if(myRankInTop50){
				//前50显示名次
				myrankNo.setRowno(myRankInTop50no);
			}else{
				//50+名次
				myrankNo.setRowno(0);
			}

			List<Activity> ac = ActivityUtil.getAllActivityByThem(ActivityUtil.them7xi);//
			if(null ==ac || ac.size()==0){
				return;
			}
			Date endDate = ac.get(0).getEndTime();
			Date overDate = ac.get(0).getShowEndTime();//领奖终止时间
			Date now = new Date();
			boolean timeFlag = false;
			if(overDate.compareTo(now)<0){
				//领奖限制时间<现在时间 不能领取
				writeErrMsg("活动已结束");
				return;
			}
			if (now.compareTo(endDate) > 0 ) {
				timeFlag = true;
			}

			boolean meInTop10 =myRankInTop50no>0 && myRankInTop50no<=10 && timeFlag ;
			json.put("ranklist",top50);
			json.put("myrank",myrankNo);
			int intParam = 0;
			if(meInTop10){
				intParam =1;//有奖励
				List<GoldRoomActivityUserItem> rewardItem = GoldRoomActivityDao.getInstance().queryInfoByUseridAndThem(getUserId(),ActivityUtil.them7xi);
				if(null!=rewardItem && rewardItem.size()>0){
					GoldRoomActivityUserItem me = rewardItem.get(0);
					if(me.getIsReward()>0){
						intParam = 0;
					}
				}
			}

			ComRes.Builder builder2 = SendMsgUtil.buildComRes(WebSocketMsgType.cs_7xi_gold_room_acitiviyt,intParam,json.toJSONString());
			writeSocket(builder2.build());
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.msgLog.error("7xiqueryGoldRoomActivityRankList|error"+e.getMessage());
		}

	}


	/**
	 * 获取七夕 集7活动 奖励；排名前10获取对应白金逗
	 */
	public synchronized void getReWard7xiActivity() {
		try {
			//活动时间。
			List<Activity> ac = ActivityUtil.getAllActivityByThem(ActivityUtil.them7xi);//
			if(null ==ac || ac.size()==0){
				return;
			}
			Date endDate = ac.get(0).getEndTime();
			Date showendtime = ac.get(0).getShowEndTime();
			Date now = new Date();
			if (now.compareTo(endDate) > 0 && now.compareTo(showendtime)<0) {
				//领取奖励时间
				List<GoldAcitivityRankResult> ranklist = GoldRoomActivityDao.getInstance().queryActivityRankMapByThemAndUserId(ActivityUtil.them7xi,null);
				long rankno = 0;
				GoldAcitivityRankResult rankItem = null;
				for (int i = 0; i < ranklist.size(); i++) {
					GoldAcitivityRankResult item = ranklist.get(i);
					if (item.getUserid() == getUserId() && item.getRowno()<=10 ) {
						//排名前20且没领取奖励
						rankno = item.getRowno();
						break;
					}
				}
				List<GoldRoomActivityUserItem> reslut =  GoldRoomActivityDao.getInstance().queryInfoByUseridAndThem(getUserId(),ActivityUtil.them7xi);
				if(null==reslut || reslut.size()==0){
					writeErrMsg("您不在前10榜单内");
					return;
				}else{
					int isreward =reslut.get(0).getIsReward();
					if(isreward>0){
						writeErrMsg("您已经领取！");
						return;
					}
				}
				if (rankno > 0 && rankno <= 10) {
					//领取奖励。
					int bean = 0;
					if (rankno == 1) {
						bean = 100000;
					} else if (rankno == 2) {
						bean = 50000;
					} else if (rankno == 3) {
						bean = 20000;
					} else if (rankno == 4 || rankno == 5 || rankno == 6) {
						bean = 10000;
					}else if(rankno >=7){
						bean = 5000;
					}
					changeGold(bean, SourceType.Activity7xi );
					GoldRoomActivityUserItem myrankItem = reslut.get(0);
					myrankItem.setIsReward(1);
					GoldRoomActivityDao.getInstance().updateReward(myrankItem);
					//领取完奖励取消红点
					ComRes.Builder builder2 = SendMsgUtil.buildComRes(WebSocketMsgType.cs_7xi_gold_room_acitiviyt_reward,bean);
					writeSocket(builder2.build());
					LogUtil.msgLog.info("7xiActivityReward_GoldRoom|" + userId + "|rankno=" + rankno + "|bean=" + bean);
				}
			} else {
				writeErrMsg("活动尚未结束~");
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.msgLog.error("7xiGoldRoomActivityGetReWard|error"+e.getMessage());
		}
	}

	/**
	 * 金币场结算 输赢>1000，看广告获取白金豆
	 * @param roomid
	 * @param beans
	 */
	public synchronized void getWatchAdsReWard(long roomid, long beans) {
		HashMap<String, Object> userThem = new HashMap<>();
		userThem.put("userid", getUserId());
		userThem.put("activityDesc", ActivityUtil.themGooldRoomWatchAdsReword);
		int rewardBeans = 0;
		if (Math.abs(beans) >= 1000 && Math.abs(beans) <= 20000) {
			rewardBeans = 2000;
		} else if (Math.abs(beans) >= 20000) {
			rewardBeans = 4000;
		} else {
			return;
		}
		List<Activity> ac = ActivityUtil.getAllActivityByThem(ActivityUtil.themGooldRoomWatchAdsReword);
		if (null == ac || ac.size() == 0) {
			LogUtil.msgLog.error("GoldRoomWatchAdsActivity|activit 104 config is null|");
			return;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		Date beginDate = ac.get(0).getBeginTime();
		Date endDate = ac.get(0).getEndTime();
		Date now = new Date();
		if (now.compareTo(beginDate) >= 0 && now.compareTo(endDate) <= 0) {
		} else {
			LogUtil.msgLog.error("GoldRoomWatchAdsActivity|DateOutTime|now=" + sdf.format(now) + "|ActivityDateBetween[" + beginDate + "-" + endDate + "]");
			return;
		}

		try {
			List<GoldRoomActivityUserItem> result = GoldRoomActivityDao.getInstance().loadItemByUserIdByThem(userThem);
			if (null == result || result.size() == 0) {
				//新增
				GoldRoomActivityUserItem item = new GoldRoomActivityUserItem();
				item.setEverydayLimit(0);
				item.setUserid(getUserId());
				JSONObject json = new JSONObject();
				if (beans > 0) {
					json.put("win", 1);
					json.put("winRoomIds", roomid + "/" + rewardBeans + "|");
					json.put("lose", 0);
					json.put("loseRoomIds", "");
				} else {
					json.put("win", 0);
					json.put("lose", 1);
					json.put("winRoomIds", "");
					json.put("loseRoomIds", roomid + "/" + rewardBeans + "|");
				}
				item.setActivityBureau(json.toString());//
				item.setDaterecord(sdf.format(now));
				item.setActivityDesc(ActivityUtil.themGooldRoomWatchAdsReword + "");
				GoldRoomActivityDao.getInstance().saveGoldRoomActivityUserItem(item);
				LogUtil.msgLog.info("GoldRoomWatchAdsActivityReward|" + getUserId() + "roomid=" + roomid + "|getReward|" + item.toString());

				//更新统计
				changeGold(rewardBeans, SourceType.goldRoomXjsWatchAds_reward);
				ComRes.Builder builder = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_goldRoomWatchAdsReward, rewardBeans);
				writeSocket(builder.build());
			} else {
				String datestr = sdf.format(now);
				GoldRoomActivityUserItem item = result.get(0);
				if (!item.getDaterecord().contains(datestr)) {
					//每日重置
					item.setDaterecord(datestr);
					JSONObject json = new JSONObject();
					if (beans > 0) {
						json.put("win", 1);
						json.put("winRoomIds", roomid + "/" + rewardBeans + "|");
						json.put("lose", 0);
						json.put("loseRoomIds", "");
					} else {
						json.put("win", 0);
						json.put("lose", 1);
						json.put("winRoomIds", "");
						json.put("loseRoomIds", roomid + "/" + rewardBeans + "|");
					}
					item.setActivityBureau(json.toString());//
				} else {
					//更新
					String str = item.getActivityBureau();
					JSONObject json = JSONObject.parseObject(str);
					int winnum = json.getIntValue("win");
					int losenum = json.getIntValue("lose");
					String winRoomIdsstr = json.getString("winRoomIds");
					String loseRoomIdsstr = json.getString("loseRoomIds");
					if (winnum >= 5 && beans > 0) {
						return;
					}
					if (losenum >= 5 && beans < 0) {
						return;
					}
					if (winRoomIdsstr.contains(roomid + "/") || loseRoomIdsstr.contains(roomid + "-")) {
						writeErrMsg("此局观看奖励已获取");
						return;
					}
					if (beans > 0) {
						json.put("win", 1 + winnum);
						json.put("winRoomIds", winRoomIdsstr + roomid + "/" + rewardBeans + "|");
					} else {
						json.put("lose", 1 + losenum);
						json.put("loseRoomIds", loseRoomIdsstr + roomid + "/" + rewardBeans + "|");
						json.toJSONString();
					}
					item.setActivityBureau(json.toString());
				}

				//更新活动表
				GoldRoomActivityDao.getInstance().updateItem(item);

				//更新统计
				changeGold(rewardBeans, SourceType.goldRoomXjsWatchAds_reward);
				ComRes.Builder builder = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_goldRoomWatchAdsReward, rewardBeans);
				writeSocket(builder.build());
				LogUtil.msgLog.info("GoldRoomWatchAdsActivityReward|" + getUserId() + "roomid=" + roomid + "|getReward|" + item.toString());
			}
		} catch (Exception e) {
			LogUtil.errorLog.error("GoldRoomWatchAdsActivityReward|error|", e);
		}
	}

	/**
	 *
	 * @param type type=0 粽子，type=1 白金豆
	 * @param number 数量
	 */
	public void duanwu_glodRoomActivityNoticeMsg(int type,int number){
		ComRes.Builder builder2 = SendMsgUtil.buildComRes(WebSocketMsgType.cs_duanwu_gold_room_acitiviyt_reward,type,number );
		writeSocket(builder2.build());
	}

	/**
	 * 金币场变更礼卷信息
	 * @param bean
	 * @param goldRoomGiftCert_award
	 * @return
	 */
	public synchronized boolean changeGiftCert(int bean, SourceType goldRoomGiftCert_award) {
		Date now = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		Long dateDate = Long.valueOf(sdf.format(now));
		boolean re = false;
		try {
			UserDao.getInstance().updateGoldenBeans(getUserId(),bean);
			GoldDataStatistics goldDataStatistics = new GoldDataStatistics(dateDate, goldRoomGiftCert_award, getUserId(), 1, Long.valueOf(bean));
			DataStatisticsDao.getInstance().saveOrUpdateGoldDataStatistics(goldDataStatistics);
			GoldDataStatistics goldDataStatistics2 = new GoldDataStatistics(dateDate, goldRoomGiftCert_award, Long.valueOf(0), 1, Long.valueOf(bean));
			DataStatisticsDao.getInstance().saveOrUpdateGoldDataStatistics(goldDataStatistics2);
			re = true;
			StringBuilder sb = new StringBuilder("updateUserGiftCert|succ");
			sb.append("|").append(userId);
			sb.append("|").append(goldRoomGiftCert_award);
			sb.append("|").append(bean);
			LogUtil.monitorLog.info(sb.toString());
		} catch (Exception e) {
			re = false;
			LogUtil.errorLog.error("updateGiftCert|error|bean=" + bean + "|userid=" + getUserId(), e);
		}
		return re;
	}

	public synchronized boolean change7xiItem(int bean) {
		Date now = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		Long dateDate = Long.valueOf(sdf.format(now));
		boolean re = false;
		try {
			UserDao.getInstance().updateGoldenBeans(getUserId(),bean);
			GoldDataStatistics goldDataStatistics = new GoldDataStatistics(dateDate, SourceType.Activity7xiItem, getUserId(), 1, Long.valueOf(bean));
			DataStatisticsDao.getInstance().saveOrUpdateGoldDataStatistics(goldDataStatistics);
			GoldDataStatistics goldDataStatistics2 = new GoldDataStatistics(dateDate, SourceType.Activity7xiItem, Long.valueOf(0), 1, Long.valueOf(bean));
			DataStatisticsDao.getInstance().saveOrUpdateGoldDataStatistics(goldDataStatistics2);
			re = true;
			StringBuilder sb = new StringBuilder("updateUserActivity7xiItem|succ");
			sb.append("|").append(userId);
			sb.append("|").append(SourceType.Activity7xiItem);
			sb.append("|").append(bean);
			LogUtil.monitorLog.info(sb.toString());
		} catch (Exception e) {
			re = false;
			LogUtil.errorLog.error("updateUserActivity7xiItem|error|bean=" + bean + "|userid=" + getUserId(), e);
		}
		return re;
	}

	/**
	 * 大厅-赠送钻石 查看赠送记录权限 0隐藏
	 */
	public int isSendDiamondsPermission(){
		if(isSongZuanQuanxian()){
			return 1;
		}else{
			return 0;
		}
	}

	/**
	 * 是否有送钻权限
	 */
	public boolean isSongZuanQuanxian(){
		List<Authority> list = AuthorityDao.getInstance().getAuthorityByQxId(userId,1,0);
		if(null!=list && list.size() >0){
			return true;
		}
		return false;
	}
	/**
	 * 是否是业务员
	 */
	public boolean isYeWuYuan(){
		List<Authority> list = AuthorityDao.getInstance().getAuthorityByQxId(userId,2,0);
		if(null!=list && list.size() >0){
			return true;
		}
		return false;
	}
	/**
	 * 送钻功能
	 * @param userId
	 * @param acpUserid
	 * @param diamondsNum
	 */
	public synchronized void sendDiamonds(long userId, long acpUserid, long diamondsNum){
		try {
			if(diamondsNum<=0||userId<0 ){
				writeErrMsg(LangMsg.code_3);
				return ;
			}
			if(userId==acpUserid){
				writeErrMsg("不能赠送给自己!");
				return;
			}
			RegInfo regInfo = UserDao.getInstance().getUser(acpUserid);
			if(null==regInfo||regInfo.getIsRobot()==1){
				writeErrMsg("没有找到此玩家");
				return;
			}
			//SendDiamondsPermission permission = null;//PermissionDao.getInstance().queryPermissionBuUid(userId);
			if(isSongZuanQuanxian()){
				boolean result = changeCards(0,-diamondsNum,true,CardSourceType.jiangnan_sendCards);
				if(!result ){
					writeErrMsg(LangMsg.code_diamond_err);
				}else{
					String acp_flatid = regInfo.getFlatId();
					String acp_pf = regInfo.getPf();
					UserDao.getInstance().updateUserCards(acpUserid,acp_flatid,acp_pf,diamondsNum,0);
					//添加日志
					HashMap<String,Object>  map = new HashMap<>();
					map.put("sendUserid",userId);
					map.put("acceptUserid",acpUserid);
					map.put("sendTime", new Date());
					map.put("diamondNum",diamondsNum);
					PermissionDao.getInstance().insertSendDiamondsLog(map);
					writeComMessage(WebSocketMsgType.res_code_permission,2,"赠送"+acpUserid+" "+diamondsNum+"钻石成功!");
					LogUtil.msgLog.info("sendDiamonds|senduserid|"+getUserId()+"|acpuserid|"+acpUserid+"|"+diamondsNum);
				}
			}else{
				writeErrMsg("权限不足");
			}
			return ;
		}catch (Exception e){
			LogUtil.errorLog.error("sendDiamonds", e);
		}
	}

	/**
	 * 送钻记录查询
	 * @param userId
	 * @param pageno
	 * @param pagesize
	 * @param acpid
	 * @param begintime
	 * @param endtime
	 */
	public  void getSendDiamondsRecord(long userId, int pageno, long pagesize, long acpid,String begintime,String endtime){
		int pageno1= pageno;
		Calendar ca = Calendar.getInstance();
		ca.add(Calendar.DAY_OF_MONTH,-15);
		Date date =ca.getTime();
		String sendTime = DateUtil.formatDate(date,"yyyy-MM-dd")+" 00:00:00";
		HashMap<String,Object> map = new HashMap<>();
		map.put("pageno",(pageno>0?pageno-1:0)*pagesize);
		map.put("pagesize",pagesize);
		map.put("sendUserid",userId);
		map.put("acceptUserid",acpid<0?null:acpid);
		map.put("sendTime",sendTime);
		map.put("begintime",begintime+" 00:00:00");
		map.put("endtime",endtime+" 23:59:59");
		//int count = PermissionDao.getInstance().querySendDiamondsLogCount(map);
		List<SendDiamondsLog> ls = PermissionDao.getInstance().querySendDiamondsLog(map);
		writeComMessage(WebSocketMsgType.res_code_permission,3,pageno1,JSONObject.toJSON(ls).toString());
	};



	/**
	 * 任务内部类，任务相关属性及可以封装的代码。
	 */
	public class Mission {
		//当天累计对局数
		int dayPlayNum = 0;
		Map<Integer, MissionState> idAndState = new HashMap<>();
		//已完成未领取的id
		List<Integer> completeId = new ArrayList<>();

		int brokeAward = 0;
		int brokeShare = 0;
		Date dayTime=new Date();
		int lzGoldNum=0;
		int hasLookedVideo=0;
		int videoAwardNum=MissionConfigUtil.getSendConfig().getAloneConfig().getVideoWatchNum();
        int queQiaoGoldNum=0;
        int queQiaoMsRed=0;//鹊桥活动奖励完成红点
		int queQiaoInviteRed=0;//邀请列表红点

		/**
		 *
		 * 普通金币场对局数
		 * @param polyploidNum 房间倍率
		 */
		public void addDayPlayNum(long polyploidNum) {
			addDayPlayNum();
			List<Integer> list = MissionConfigUtil.getPlayMissionId();
			if(list!=null){
				for (Integer id : list) {
					MissionConfig config = MissionConfigUtil.getMissionIdAndConfig().get(id);
					if(completeId.contains(config))
						continue;
					switch (config.getTag()){
						case MissionConfigUtil.tag_dayMission:
							String  ext = config.getExt();
							if(ext!=null&&ext.length()>0){
								String[] split = ext.split(",");
								if(polyploidNum>=StringUtil.getLongValue(split,0)){
									if (addProgressBar(id))
										addCompleteId(id);
								}
							}else{
								if (addProgressBar(id))
									addCompleteId(id);
							}
							break;
						case MissionConfigUtil.tag_activeQueQiao:
							if (addProgressBar(id)){
								addCompleteId(id);
								changeQueQiaoMs(getIdAndState().get(id));
							}
							break;
					}
				}
			}
			changeMission();
		}

		public void addDayPlayNum(){
			dayPlayNum++;
			aqq.sendTeamateMsgChange(dayPlayNum);
		}

		/**
		 *
		 * 分享任务
		 */
		public void addShareMission() {
			List<Integer> list = MissionConfigUtil.getShareMissionId();
			if(list!=null){
				for (Integer id : list) {
					if (!completeId.contains(id)&&addProgressBar(id))
						addCompleteId(id);
				}
			}
			changeMission();
		}

		/**
		 *
		 * 签到任务
		 */
		public void addSignMission() {
			List<Integer> list = MissionConfigUtil.getSignMissionId();
			if(list!=null){
				for (Integer id : list) {
					if (!completeId.contains(id))
						addSignProgressBar(id);
				}
			}
			changeMission();
		}

		/**
		 *
		 * 当打完一局金币挑战赛时时调用
		 */
		public void addChallengeRound() {
			addDayPlayNum();
			List<Integer> list = MissionConfigUtil.getChallengeRoomId();
			if(list!=null){
				for (Integer id : list) {
					if (!completeId.contains(id)&&addProgressBar(id))
						addCompleteId(id);
				}
			}
			changeMission();
		}

		private boolean addProgressBar(Integer id){
			MissionState ms = idAndState.get(id);
			if(ms==null){
				ms=new MissionState(id);
				idAndState.put(id,ms);
			}
			return ms.addProgressBar();
		}

		private boolean addSignProgressBar(Integer id){
			MissionState ms = idAndState.get(id);
			if(ms==null){
				ms=new MissionState(id);
				idAndState.put(id,ms);
			}
			return ms.addSignProgressBar();
		}

		public void testComplete(int missionId) {
			MissionState ms = idAndState.get(missionId);
			if (ms == null) {
				ms = new MissionState(missionId);
				idAndState.put(missionId, ms);
				ms.setComplete(true);
				ms.setProgressBar(MissionConfigUtil.getMissionIdAndConfig().get(missionId).getFinishNum());
				if(!completeId.contains(missionId))
					addCompleteId(missionId);
			}
			changeMission();
		}


		public boolean receiveAward(int missionId) {
			boolean flag=idAndState.get(missionId).receiveAward();
			if(flag){//可以领取，并且已经修改了领取状态，接下来领取对应奖励
				removeCompleteId(missionId);
				changeMission();
				MissionConfig missionConfig = MissionConfigUtil.getMissionIdAndConfig().get(missionId);
				if(missionConfig.getAwardId()==1){//后续会配道具表，暂时==1
					SourceType goldType=SourceType.unknown;
					switch (missionConfig.getTag()){
						case MissionConfigUtil.tag_dayMission:
							goldType=SourceType.day_mission;
							break;
						case MissionConfigUtil.tag_activeQueQiao:
							goldType=SourceType.activeQueQiao;
							break;
					}
					changeGold(missionConfig.getAwardNum(), goldType);
				}
			}
			return flag;
		}

		public boolean receiveAwardVideo(int missionId,long goldNum,SourceType sourceType) {
			boolean flag=idAndState.get(missionId).receiveAward();
			if(flag){//可以领取，并且已经修改了领取状态，接下来领取对应奖励
				removeCompleteId(missionId);
				changeMission();
				MissionConfig missionConfig = MissionConfigUtil.getMissionIdAndConfig().get(missionId);
				if(missionConfig.getAwardId()==1){//后续会配道具表，暂时==1
					changeGold(goldNum, sourceType);
				}
			}
			return flag;
		}

		public void addCompleteId(int missionId){
			if(MissionConfigUtil.getQueQiaoSpecialId()==missionId){
				completeId.add(missionId);
			}else if(!completeId.contains(missionId)){
				completeId.add(missionId);
				writeMissionRedCom(missionId);
			}
		}

		public void writeMissionRedCom(int missionId){
			if(MissionConfigUtil.getShowMissionId().contains(missionId))
				writeComMessage(WebSocketMsgType.res_code_missionRedCom,1);
			if(MissionConfigUtil.getQueQiaoId().contains(missionId)&&aqq!=null){
				changeQueQiaoRed(1,null);
			}
		}

		public void changeQueQiaoRed(Integer msRed,Integer inviteRed){
			if(msRed!=null&&msRed!=queQiaoMsRed){
				queQiaoMsRed=msRed;
				writeComMessage(WebSocketMsgType.res_code_QueQiaoRedCom,queQiaoMsRed,queQiaoInviteRed);
			}
			if(inviteRed!=null&&inviteRed!=queQiaoInviteRed){
				queQiaoInviteRed=inviteRed;
				writeComMessage(WebSocketMsgType.res_code_QueQiaoRedCom,queQiaoMsRed,queQiaoInviteRed);
			}
		}

		public boolean isShowMissionComplete(){
			List<Integer> showList = MissionConfigUtil.getShowMissionId();
			for(Integer id:completeId){
				if(showList.contains(id))
					return true;
			}
			return false;
		}

		public void writeMissionRedCom(){
			if(isShowMissionComplete())
				writeComMessage(WebSocketMsgType.res_code_missionRedCom,1);
			if(aqq!=null)
            	writeComMessage(WebSocketMsgType.res_code_QueQiaoRedCom,aqq.getIsRead()==0?1:queQiaoMsRed,queQiaoInviteRed);
			else
				writeComMessage(WebSocketMsgType.res_code_QueQiaoRedCom,queQiaoMsRed,queQiaoInviteRed);
		}


		public void removeCompleteId(int missionId){
			if(completeId.contains(missionId)){
				completeId.remove((Integer) missionId);
				if(!isShowMissionComplete())
					writeComMessage(WebSocketMsgType.res_code_missionRedCom,0);
			}
		}

		public void addBrokeAward() {
			brokeAward++;
			changeMission();
		}

		public void addBrokeShare() {
			brokeShare++;
			changeMission();
		}

		public MissionAbout toDB() {
			MissionAbout ab=new MissionAbout();
            ab.setUserId(userId);
			ab.setBrokeShare(brokeShare);
			ab.setDayTime(dayTime);
			StringBuilder dayS = new StringBuilder();
			StringBuilder otherS = new StringBuilder();
            Map<Integer, List<Integer>> tagAndId = MissionConfigUtil.getTagAndId();
            if(tagAndId.size()>0){
				List<Integer> dayMissionId = MissionConfigUtil.getDayMissionId();
                for (Integer id : dayMissionId) {
					MissionState ms = idAndState.get(id);
					if(ms!=null)
						dayS.append(ms.toStr()).append("|");
                }
                ab.setDayMissionState(dayS.toString());
                for (Map.Entry<Integer, MissionState> entry : idAndState.entrySet()) {
                    if (!dayMissionId.contains(entry.getKey())) {
                        otherS.append(entry.getValue().toStr()).append("|");
                    }
                }
                ab.setOtherMissionState(otherS.toString());
            }
            ab.setBrokeAward( brokeAward);
			StringBuffer sb = new StringBuffer();
			sb.append(dayPlayNum).append(",");
			sb.append(lzGoldNum).append(",");
			sb.append(videoAwardNum).append(",");
            sb.append(queQiaoGoldNum).append(",");
			ab.setExt(sb.toString());
			return ab;
		}

		public void initDate(MissionAbout ma) {
			if (ma != null) {
				brokeAward = ma.getBrokeAward();
				brokeShare = ma.getBrokeShare();
				dayTime = ma.getDayTime();
				if(dayTime==null)
					dayTime=new Date(0);
				String[] split1 = ma.getDayMissionState().split("\\|");
				Map<Integer, MissionConfig> idAndConfig = MissionConfigUtil.getMissionIdAndConfig();
				for (int i = 0; i < split1.length; i++) {
					if("".equals(split1[i]))
						continue;
					MissionState ms = new MissionState();
					ms.init(split1[i]);
					if(idAndConfig.containsKey(ms.getId()))
						idAndState.put(ms.getId(), ms);
				}
				String[] split2 = ma.getOtherMissionState().split("\\|");
				for (int i = 0; i < split2.length; i++) {
					if("".equals(split2[i]))
						continue;
					MissionState ms = new MissionState();
					ms.init(split2[i]);
					if(idAndConfig.containsKey(ms.getId()))
						idAndState.put(ms.getId(), ms);
				}
				completeId.clear();
				for (MissionState ms:idAndState.values()) {
					if(ms.isComplete()&&!ms.isObtain())
						addCompleteId(ms.getId());
				}
				//下次提交可删除
				SevenGoldSign sign=PlayerManager.getInstance().getUserSign(userId);
				if(TimeUtil.countTwoDay(sign.getLastSignTime(),new Date())==0){
					List<Integer> list = MissionConfigUtil.getSignMissionId();
					for (Integer id:list) {
						MissionState ms = idAndState.get(id);
						if(ms!=null)
							ms.addSignProgressBar();
					}
				}

				String ext = ma.getExt();
				if(ext!=null&&!"".equals(ext)){
					int i=0;
					String[] values = ext.split(",");
					dayPlayNum = StringUtil.getIntValue(values, i++);
					lzGoldNum = StringUtil.getIntValue(values, i++);
					lzGoldNum=0;//活动已结束，字段清零
					videoAwardNum = StringUtil.getIntValue(values, i++);
                    queQiaoGoldNum = StringUtil.getIntValue(values, i++);
				}
				if(haveCompleteQueQiao())
				    queQiaoMsRed=1;
			}
			rectifyData();
		}

		/**
		 * 判断是否已经过了一天，如果过了，每日数据需要清零。
		 */
		public void rectifyData(){
			Date today = new Date();
			if(dayTime==null)
				dayTime=new Date(0);
			if(TimeUtil.countTwoDay(dayTime,today)>0){
				dayPlayNum = 0;
				brokeAward = 0;
				brokeShare = 0;
				lzGoldNum = 0;
				hasLookedVideo=0;
				videoAwardNum=MissionConfigUtil.getSendConfig().getAloneConfig().getVideoWatchNum();
				dayTime=today;
				queQiaoGoldNum=0;
				queQiaoMsRed=0;
				List<Integer> dayMissionId = MissionConfigUtil.getDayMissionId();
				if(dayMissionId!=null){
					completeId.removeAll(dayMissionId);
					for (Integer id : dayMissionId) {
						if(idAndState.containsKey(id))
							 idAndState.get(id).clear();
					}
				}
				if(aqq!=null)
					aqq.ZeroReset();
				startServerRedCom();
			}
			changeMission();
		}

		public Map<Integer, MissionState> getIdAndState() {
			rectifyData();
			return idAndState;
		}

		public boolean canBrokeAward(){
			return brokeAward<2;
		}

		public void changeMission(){
			PlayerManager.getInstance().addUpdateUserMission(userId,mission.toDB());
		}

		public void minusVideoAwardNum(){
			videoAwardNum--;
			changeMission();
		}

        public int getDayPlayNum() {
            return dayPlayNum;
        }

        public int getQueQiaoGoldNum() {
            return queQiaoGoldNum;
        }

        public boolean haveCompleteQueQiao(){
			List<Integer> queQiaoId = MissionConfigUtil.getQueQiaoId();
			for (Integer id:completeId) {
				if(queQiaoId.contains(id))
					return true;
			}
			return false;
		}
    }

	public Mission getMission() {
		return mission;
	}

	public void getMissionMsg(){
		Map<Integer, List<MissionConfig>> tagMissionMap = MissionConfigUtil.getTagMissionMap();
		MissionBoardRes.Builder mbr = MissionBoardRes.newBuilder();
		for (Map.Entry<Integer,List<MissionConfig>> entry:tagMissionMap.entrySet()) {
			Integer tag = entry.getKey();
			if(!MissionConfigUtil.show_mission.contains(tag))
				continue;
			ATagMissionRes.Builder atmr=ATagMissionRes.newBuilder();
			atmr.setTag(tag);
			List<MissionRes.Builder> l1=new ArrayList<>();//已完成没领取
			List<MissionRes.Builder> l2=new ArrayList<>();//未完成
			List<MissionRes.Builder> l3=new ArrayList<>();//已完成已领取
			for (MissionConfig mc:entry.getValue()) {
				MissionRes.Builder mr=MissionRes.newBuilder();
				mr.setId(mc.getId());
				mr.setTag(mc.getTag());
				mr.setMissionExplain(mc.getMissionExplain());
				mr.setAwardExplain(mc.getAwardExplain());
				mr.setType(mc.getType());
				mr.setFinishNum(mc.getFinishNum());
				mr.setAwardId(mc.getAwardId());
				mr.setAwardIcon(mc.getAwardIcon());
				mr.setAwardNum(mc.getAwardNum());
				Map<Integer, MissionState> idAndState = getMission().getIdAndState();
				if(idAndState!=null&&idAndState.containsKey(mr.getId())){
					MissionState ms = idAndState.get(mr.getId());
					mr.setProgressBar(ms.getProgressBar());
					mr.setIsComplete(ms.isComplete()?1:0);
					mr.setIsObtain(ms.isObtain()?1:0);
				}else {
					mr.setProgressBar(0);
					mr.setIsComplete(0);
					mr.setIsObtain(0);
				}
				if(mr.getIsComplete()==1&&mr.getIsObtain()==0){
					l1.add(mr);
				}else if(mr.getIsComplete()==0&&mr.getIsObtain()==0){
					l2.add(mr);
				}else {
					l3.add(mr);
				}
			}
			l1.addAll(l2);
			l1.addAll(l3);
			for (MissionRes.Builder mr:l1) {
				atmr.addMissionRes(mr);
			}
			mbr.addATag(atmr);
		}
		writeSocket(mbr.build());
	}

	public void getActivityQueQiaoMsg(){
		List<Activity> activity = ActivityUtil.getQueQiaoActive();
		if(activity.size()==0||!activity.get(0).show()){
			writeErrMsg("活动已结束");
			return;
		}
		writeSocket(aqq.getQueQiaoMsg(this));
		mission.changeQueQiaoRed(mission.haveCompleteQueQiao()?1:0,null);
		if(aqq.getIsRead()==0){
			ActivityDao.getInstance().updateReadQueQiaoMsg(userId,aqq.getTeamateId());
			aqq.setIsRead(1);
		}
	}

	public void getActivityQueQiaoInviteBoard(){
		List<Activity> activity = ActivityUtil.getQueQiaoActive();
		if(activity.size()==0||!activity.get(0).show()){
			writeErrMsg("活动已结束");
			return;
		}
		writeSocket(aqq.getInviteList());
		mission.changeQueQiaoRed(null,0);
	}

	public void startServerRedCom(){
		clearRedCom();
		if(mission!=null)
			mission.writeMissionRedCom();

	}

	public void videoSign(){
		mission.hasLookedVideo=1;
		LogUtil.msgLog.info("videoSign|"+userId+"|");
		sign(1,SourceType.video_sign);
	}

	public void videoAward(){
		if(mission.videoAwardNum>0){
			mission.minusVideoAwardNum();
			int videoAwardNum = MissionConfigUtil.getSendConfig().getAloneConfig().getVideoAwardNum();
			changeGold(videoAwardNum,SourceType.video_award);
			writeComMessage(WebSocketMsgType.req_code_videoAward);
		}
	}

	public void changeQueQiaoMs(MissionState ms){
		aqq.changeMissionState(this,ms);
	}

	public void getVideoAwardMsg(){
		writeComMessage(WebSocketMsgType.req_code_getVideoAwardMsg,mission.videoAwardNum);
	}

	public void clearRedCom(){
		writeComMessage(WebSocketMsgType.res_code_missionRedCom,0);
//		writeComMessage(WebSocketMsgType.res_code_lzRedCom,0);
	}



	public void getWinReard(){
		if(utreward==null){
			return;
		}
		LogUtil.msgLog.info("getWinReard|"+utreward.getTempVal()+"|"+getUserId()+"|"+utreward.gettWinCount());
		Set<Integer> ids = TWinRewardUtil.twinList.keySet();
		TWinReward reward0 = null;
		for(Integer id: ids){
			TWinReward reward = TWinRewardUtil.twinList.get(id);
			if(reward.getWinCount()<=utreward.gettWinCount()){
				if(utreward.getWinRewardList().contains(reward.getWinCount())){
					reward0 = reward;
					break;
				}
			}
		}

		// 领取金币
		if(reward0!=null&&utreward.getTempVal()>0){
			changeGold(utreward.getTempVal()*10, SourceType.twin_video_award);
			writeComMessage(WebSocketMsgType.res_code_getWinReward,2,utreward.getTempVal()*10);
			utreward.setTempVal(0);
			UserTwinRewardDao.getInstance().update(utreward);
		}
	}

	public void updateTwinRewardCount() {
		if (utreward == null) {
			utreward = UserTwinRewardDao.getInstance().getUserTwinReward(userId);
			if (utreward == null) {
				utreward = new UserTwinReward();
				utreward.settWinCount(1);
				utreward.setUserId(userId);
			}else{
				utreward.settWinCount(utreward.gettWinCount() + 1);
			}
		} else {
			utreward.settWinCount(utreward.gettWinCount() + 1);
		}
		// twinCount++;
		Set<Integer> ids = TWinRewardUtil.twinList.keySet();
		TWinReward reward0 = null;
		for (Integer id : ids) {
			TWinReward reward = TWinRewardUtil.twinList.get(id);
			if (reward.getWinCount() <= utreward.gettWinCount()) {
				if (!utreward.getWinRewardList().contains(reward.getWinCount())) {
					reward0 = reward;
					break;
				}
			}
		}

		// 领取金币
		int goldBean = 0;
		if (reward0 != null) {
			utreward.getWinRewardList().add(reward0.getWinCount());
			utreward.setWinRewardList(utreward.getWinRewardList());
//			reward0.getjBeanList().get(0);
			 goldBean = reward0.getjBeanList().get(0)
					+ RandomUtils.nextInt(reward0.getjBeanList().get(1) - reward0.getjBeanList().get(0));
			utreward.setTempVal(goldBean);
			changeGold(goldBean, SourceType.Twin_award);
		}
		UserTwinRewardDao.getInstance().update(utreward);
		writeComMessage(WebSocketMsgType.res_code_sendWinReward, utreward.gettWinCount(), reward0==null?0:2, reward0==null?0:goldBean);

	}

	public void updateUserTwinReward(){
		if(utreward!=null){
			utreward.settWinCount(0);
			utreward.getWinRewardList().clear();
			utreward.setWinRewardList(utreward.getWinRewardList());
		}
	}


	public void getWinReardInfo(){
//		if(utreward==null){
//			return;
//		}
		Set<Integer> ids = TWinRewardUtil.twinList.keySet();
		JSONObject json = new JSONObject();
		json.put("winCount",utreward==null?0:utreward.gettWinCount());
		json.put("rewards", utreward==null?null:utreward.getWinRewardList());
		json.put("getRewards", ids);


		writeComMessage(WebSocketMsgType.res_code_getWinRewardInfo,json.toString());
	}


	public synchronized void receiveMissionAward(int missionId){
		if(!MissionConfigUtil.getMissionIdAndConfig().containsKey(missionId)||!mission.getIdAndState().containsKey(missionId))
			writeComMessage(WebSocketMsgType.res_code_err,"任务不存在或者内容已变更");
		else if(!mission.getIdAndState().get(missionId).isComplete())
			writeComMessage(WebSocketMsgType.res_code_err,"任务未完成");
		else if(!mission.receiveAward(missionId))
			writeComMessage(WebSocketMsgType.res_code_err,"奖励已被领取");
		else {
			LogUtil.msgLog.info("completeMission|"+userId+"|"+missionId);
			writeComMessage(WebSocketMsgType.res_code_receiveaward,missionId);
		}

	}

	public synchronized void receiveActiveQueQiao(int missionId,String type){
		MissionConfig config = MissionConfigUtil.getMissionIdAndConfig().get(missionId);
		if(config==null||!mission.getIdAndState().containsKey(missionId))
			writeComMessage(WebSocketMsgType.res_code_err,"活动不存在或者内容已变更");
		else if(!mission.getIdAndState().get(missionId).isComplete())
			writeComMessage(WebSocketMsgType.res_code_err,"活动未完成");
		else {
			boolean flag = false;
			int awardNum = config.getAwardNum();
			if(StringUtil.isBlank(config.getExt())){
				if("normal".equals(type))
					flag=mission.receiveAward(missionId);
				else if("video".equals(type)){
					awardNum*=2;
					flag=mission.receiveAwardVideo(missionId,awardNum,SourceType.activeQueQiaoVideo);
				}
				if(!flag)
					writeComMessage(WebSocketMsgType.res_code_err,"奖励已被领取");
				else {
					mission.queQiaoGoldNum+=awardNum;
					writeComMessage(WebSocketMsgType.res_code_receiveawardQueQiao,config.getAwardNum());
					LogUtil.msgLog.info("receiveActiveQueQiao|"+userId+"|"+missionId);
					aqq.changeMissionState(this,mission.getIdAndState().get(missionId));
				}
			}else {
				if(aqq.getTeamatePlayNum()>=config.getFinishNum()){
					flag = mission.receiveAward(missionId);
					if(flag){
						changeGiftCert(Integer.parseInt(config.getExt()),SourceType.giftCertQueQiao);
						mission.queQiaoGoldNum+=awardNum;
						writeComMessage(WebSocketMsgType.res_code_receiveawardQueQiao,config.getAwardNum(),Integer.parseInt(config.getExt()));
						LogUtil.msgLog.info("receiveActiveQueQiao|"+userId+"|"+missionId);
						aqq.changeMissionState(this,mission.getIdAndState().get(missionId));
					}
				}else {
					writeComMessage(WebSocketMsgType.res_code_err,"约会对象尚未完成");
				}
			}
			if(flag)
				getActivityQueQiaoMsg();
			if(!mission.haveCompleteQueQiao())
				mission.changeQueQiaoRed(0,null);
		}
	}

	public void testMission(int missionId){
		mission.testComplete(missionId);
	}

	/**
	 * 破产补助
	 */
	public synchronized void brokeAward(SourceType sourceType){
	    try{
            ConfigMsg.AloneConfigRes aloneConfig = MissionConfigUtil.getSendConfig().getAloneConfig();
            if(loadAllGolds()<aloneConfig.getBrokeTigger()&&mission.brokeAward<2){
                mission.addBrokeAward();
                // 领取金币
				int goldNum=aloneConfig.getBroke();
				if(sourceType==SourceType.video_broke){
                    goldNum*=2;
                    writeComMessage(WebSocketMsgType.res_code_borkeawardVidea,goldNum);
                }else
                    writeComMessage(WebSocketMsgType.res_code_borkeaward);
                changeGold(goldNum, sourceType);
                LogUtil.msgLog.info("brokeAward|"+userId+"|"+sourceType);
            }
        }catch (Exception e){
            LogUtil.errorLog.error("#brokeAward:", e);
        }
	}

	public synchronized void deskShare(){
		if(mission!=null)
			mission.addShareMission();
		LogUtil.msgLog.info("deskShare|"+userId);
	}

	public synchronized void selectGoldenBeans(){
		Long goldenBeans = UserDao.getInstance().selectGoldenBeans(userId);
		LogUtil.msgLog.info("selectGoldenBeans|"+userId+"|"+goldenBeans);
		writeComMessage(WebSocketMsgType.res_code_getGoldenBeans,""+goldenBeans);
	}

	/**
	 * 发送破产补助信息
	 */
	public synchronized void sendBrokeAward(long joinLimit ){
		ConfigMsg.AloneConfigRes aloneConfig = MissionConfigUtil.getSendConfig().getAloneConfig();
		if(loadAllGolds()<aloneConfig.getBrokeTigger()&&mission.brokeAward<2)
			writeComMessage(WebSocketMsgType.getRes_code_sendbrokemsg,mission.brokeAward);
		else if(loadAllGolds()<joinLimit)
			writeComMessage(WebSocketMsgType.res_code_buygold);
	}

	/**
	 * 破产分享
	 */
	public synchronized void brokeShare(){
	    try {
            ConfigMsg.AloneConfigRes aloneConfig = MissionConfigUtil.getSendConfig().getAloneConfig();
            if(mission.brokeAward<2&&mission.brokeShare<2){
                writeComMessage(WebSocketMsgType.res_code_brokeshare);
                mission.addBrokeShare();
                // 领取金币
                changeGold(aloneConfig.getBrokeShare(), SourceType.broke_award);
                LogUtil.msgLog.info("brokeShare|"+userId);
            }
        }catch (Exception e){
            LogUtil.errorLog.error("#brokeShare:", e);
        }
	}
    public long getWinGold() {
        return winGold;
    }

    public void setWinGold(long winGold) {
        this.winGold = winGold;
    }

    public long getGoldRoomGroupId() {
        return goldRoomGroupId;
    }

	public void setGoldRoomGroupId(long goldRoomGroupId) {
        this.goldRoomGroupId = goldRoomGroupId;
    }

    public void calcGoldResult(long winGold) {
        getGoldPlayer().changePlayCount();

        if (winGold > 0) {
            getGoldPlayer().changeWinCount();
            GoldDao.getInstance().updateGoldUserCount(userId, 1, 0, 0, 1);
        } else {
            getGoldPlayer().changeLoseCount();
            GoldDao.getInstance().updateGoldUserCount(userId, 0, 1, 0, 1);
        }
    }

    public boolean isSoloWinner() {
        return isSoloWinner;
    }

    public void setSoloWinner(boolean soloWinner) {
        isSoloWinner = soloWinner;
    }

    /**
     * 推送头像变更
     */
    public void notifyHeadimgurl(String headimgurl) {
        ComRes.Builder res = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_headimgurl, headimgurl);
        writeSocket(res.build());
    }

    public String getLoginExtend() {
        return loginExtend;
    }

    public void setLoginExtend(String loginExtend) {
        this.loginExtend = loginExtend;
    }


    /**
     * 数据库加载robot
     */
    public void initRobotFromDB(String robotInfo){

    }

    public int getRobotAILevel() {
        return robotAILevel;
    }

    public void setRobotAILevel(int robotAILevel) {
        this.robotAILevel = robotAILevel;
    }

    public int getRobotActionCounter() {
        return robotActionCounter;
    }

    public void setRobotActionCounter(int robotActionCounter) {
        this.robotActionCounter = robotActionCounter;
    }

    public void resetRobotActionCounter() {
        this.robotActionCounter = 0;
        this.robotActionRND = 0;
    }

    public void addRobotActionCounter(){
        this.robotActionCounter++;
    }

    public int getRobotActionRND() {
        return robotActionRND;
    }

    public void setRobotActionRND(int robotActionRND) {
        this.robotActionRND = robotActionRND;
    }

    public boolean canRobotAction(){
        return robotActionCounter > robotActionRND;
    }

    public void calcRobotActionRND(int base, int rnd) {
        if (this.robotActionRND == 0) {
            this.robotActionRND = base + new Random().nextInt(rnd) + 1;
        }
    }

	public int getXipaiCount() {
		return xipaiCount;
	}

	public void setXipaiCount(int xipaiCount) {
		this.xipaiCount = xipaiCount;
	}

	public int getXipaiStatus() {
		return xipaiStatus;
	}

	public void setXipaiStatus(int xipaiStatus) {
		this.xipaiStatus = xipaiStatus;
	}

	public int getXipaiScore() {
		return xipaiScore;
	}

	public void setXipaiScore(int xipaiScore) {
		this.xipaiScore = xipaiScore;
	}

	/**
	 * 保存洗牌数据
	 * @return
	 */
	public String toXipaiDatatoInfoStr() {
		StringBuilder sb = new StringBuilder();
		sb.append(getXipaiCount()).append(",");
		sb.append(getXipaiScore()).append(",");
		sb.append(getXipaiStatus()).append(",");
		return sb.toString();
	}
	/**
	 * 解析洗牌数据
	 */
	public void initXipaiDataPlayInfo(String data) {
		if (!StringUtils.isBlank(data)) {
			int i = 0;
			String[] values = data.split(",");
			long duserId = StringUtil.getLongValue(values, i++);
			if (duserId != getUserId()) {
				return;
			}
			int end = values.length -1;
			this.xipaiStatus = StringUtil.getIntValue(values, end--);
			this.xipaiScore = StringUtil.getIntValue(values, end--);
			this.xipaiCount = StringUtil.getIntValue(values, end--);
			return;
		}
	}

	public void cleanXipaiData(){
		this.xipaiStatus =0;
		this.xipaiScore =0;
		this.xipaiCount = 0;
	}

	public long getCreditLogData() {
		return creditLogData;
	}

	public void setCreditLogData(long creditLogData) {
		this.creditLogData = creditLogData;
	}
}
