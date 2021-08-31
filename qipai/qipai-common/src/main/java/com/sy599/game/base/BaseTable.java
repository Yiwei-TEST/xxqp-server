package com.sy599.game.base;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.GeneratedMessage;
import com.sy.mainland.util.redis.Redis;
import com.sy.mainland.util.redis.RedisUtil;
import com.sy599.game.GameServerConfig;
import com.sy599.game.activity.ActivityConstant;
import com.sy599.game.assistant.AssisServlet;
import com.sy599.game.character.GoldPlayer;
import com.sy599.game.character.Player;
import com.sy599.game.common.UserResourceType;
import com.sy599.game.common.bean.BaoDiConfig;
import com.sy599.game.common.bean.Consume;
import com.sy599.game.common.bean.CreateTableInfo;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.LogConstants;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.common.constant.SharedConstants.player_state;
import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.common.executor.TaskExecutor;
import com.sy599.game.credit.CreditCommission;
import com.sy599.game.db.bean.*;
import com.sy599.game.db.bean.competition.CompetitionRoom;
import com.sy599.game.db.bean.competition.CompetitionRoomUser;
import com.sy599.game.db.bean.competition.CompetitionTmpPlayer;
import com.sy599.game.db.bean.gold.GoldRoom;
import com.sy599.game.db.bean.gold.GoldRoomActivityUserItem;
import com.sy599.game.db.bean.gold.GoldRoomConfig;
import com.sy599.game.db.bean.gold.GoldRoomUser;
import com.sy599.game.db.bean.group.*;
import com.sy599.game.db.dao.*;
import com.sy599.game.db.dao.gold.GoldDao;
import com.sy599.game.db.dao.gold.GoldRoomActivityDao;
import com.sy599.game.db.dao.gold.GoldRoomDao;
import com.sy599.game.db.dao.group.GroupDao;
import com.sy599.game.db.dao.group.GroupWarnDao;
import com.sy599.game.db.enums.CardSourceType;
import com.sy599.game.db.enums.CoinSourceType;
import com.sy599.game.db.enums.SourceType;
import com.sy599.game.db.enums.UserMessageEnum;
import com.sy599.game.gcommand.com.LuckyRedbagCommand;
import com.sy599.game.gcommand.com.activity.NewPlayerGiftActivityCmd;
import com.sy599.game.gcommand.login.util.BjdUtil;
import com.sy599.game.gcommand.login.util.LoginUtil;
import com.sy599.game.gold.GoldRoomTableRecord;
import com.sy599.game.gold.GroupGoldLog;
import com.sy599.game.gold.SoloRoomTableRecord;
import com.sy599.game.jjs.bean.MatchBean;
import com.sy599.game.jjs.dao.MatchDao;
import com.sy599.game.jjs.util.JjsUtil;
import com.sy599.game.manager.MarqueeManager;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.manager.RedBagManager;
import com.sy599.game.manager.TableManager;
import com.sy599.game.message.MessageUtil;
import com.sy599.game.msg.serverPacket.BaiRenTableMsg.BaiRenTableRes;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.ComMsg.ComRes;
import com.sy599.game.msg.serverPacket.TableRes;
import com.sy599.game.msg.serverPacket.TableRes.CreateTableRes;
import com.sy599.game.robot.RobotManager;
import com.sy599.game.staticdata.StaticDataManager;
import com.sy599.game.staticdata.bean.ActivityConfig;
import com.sy599.game.staticdata.bean.ActivityConfigInfo;
import com.sy599.game.staticdata.model.ActivityBean;
import com.sy599.game.staticdata.model.GameReBate;
import com.sy599.game.util.*;
import com.sy599.game.util.constants.GroupConstants;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public abstract class BaseTable {
	protected static final int JSON_TAG = 1;

	/*** 类型：普通***/
	public static final int TABLE_TYPE_NORMAL = 0;
	/*** 类型：军团***/
	public static final int TABLE_TYPE_GROUP = 1;
	/*** 类型：练习场***/
	public static final int TABLE_TYPE_PRACTICE = 2;
	/*** 类型：金币场***/
	public static final int TABLE_TYPE_GOLD = 3;
	/*** 类型：solo挑战***/
	public static final int TABLE_TYPE_SOLO = 4;
	/*** 类型：比赛场***/
	public static final int TABLE_TYPE_COMPETITION_PLAYING = 5;

	/*** 牌桌类型：0普通，1金币, 5比赛房***/
	protected int tableType;
	/*** 金币场随机id****/
	protected long goldRoomId;
	/**比赛场房间ID**/
	protected long competitionRoomId;
	/*** 玩法类型***/
	protected volatile int playType;
	/*** 当前牌桌状态 */
	protected volatile table_state state;
	/*** 牌桌Id */
	protected volatile long id;
	/*** 房间id(暂定) */
	protected volatile int roomId;
	/*** 创建人*/
	protected volatile long creatorId;
	/*** 房主 */
	protected volatile long masterId;
	/*** 创建时间 */
	protected Date createTime;
	/*** 总局数 */
	protected volatile int totalBureau;
	/*** 当前玩的局数 */
	protected volatile int playBureau;
	/*** 当前完成局数 */
	protected volatile int finishBureau=0;
	/*** 出牌的轮数 */
	protected volatile int disCardRound;
	/*** 当前要出牌的座位 */
	protected volatile int nowDisCardSeat;
	/*** 当前出牌人的座位 */
	protected volatile int disCardSeat;
	/*** 应答解散请求玩家的答案 */
	protected Map<Integer, Integer> answerDissMap = new LinkedHashMap<>();
	protected volatile String playLog = "";
	protected Map<String, Object> dbParamMap = new ConcurrentHashMap<>();
	// public static final int max_player_count = 4;
	protected volatile int lastWinSeat = 0;
	protected long gotyeRoomId;
	protected boolean isRuning;
	protected List<Integer> config;
	private long sendDissTime;
	/*** 做出玩家动作的时间 动作超时用 */
	protected volatile long lastActionTime;
	protected int isCompetition;
	protected TableInf tableInf;
	protected List<List<Integer>> zp;
	/**
	 * 玩家摸牌做牌
	 */
	protected Map<Long, Integer> zpMap = new HashMap<>();
	protected volatile long daikaiTableId;// 代开房间ID
	protected volatile boolean tiqianDiss = false;

	protected volatile boolean autoPlayDiss = false;
	protected int specialDiss = 0;
	private ReentrantLock lock;
	/**
	 * 房费支付方式
	 **/
	protected volatile int payType;

	//是否检查支付，代开房和军团房不需要
	protected volatile boolean checkPay = true;

	/**
	 * 是否允许群组牌友加入的桌子
	 **/
	protected int allowGroupMember = 0;
	/**
	 * 群助手创房编号
	 **/
	protected String assisCreateNo = "";
	/**
	 * 群助手群编号
	 **/
	protected String assisGroupNo = "";
	/**
	 * 桌子是否能搓牌
	 **/
	protected int isShuffling = 0;

	/*** 金币场模式id*/
	protected volatile String modeId = "0";//金币场

	private volatile boolean deleted = false;

	protected long lastCheckTime = 0L;//定时任务最后检查该房间的时间

	/**
	 * 已结算的小局数
	 **/
	protected volatile int playedBureau = 0;
	/**
	 * 比赛场ID
	 **/
	protected long matchId = 0L;
	protected long matchRatio = 1L;  //比赛场倍率

	/**
	 * 是否信用分房间
	 */
	protected int creditMode = 0;
	/**
	 * 进房最低信用分限制
	 */
	protected long creditJoinLimit = 0;
	/**
	 * 解散最低信用分限制
	 */
	protected long creditDissLimit = 0;
	/**
	 * 信用分低分:
	 */
	protected long creditDifen = 0;
	/**
	 * 佣金：送给群主的信用分
	 */
	protected long creditCommission = 0;
	/**
	 * 佣金模式：1：固定赠送，2：比例赠送: 百分制
	 */
	protected int creditCommissionMode1 = 0;
	/**
	 * 佣金模式：1：大赢家，2：全部赢家
	 */
	protected int creditCommissionMode2 = 0;
	/**
	 * 佣金模式:佣金限制分,当玩家赢得分大于或等于此分时,才计算佣金
	 */
	protected long creditCommissionLimit = 0;
	/**
	 * 信用分是否是100模式
	 */
	protected long credit100 = 0;
	/**
	 * 保低增送分
	 */
	protected long creditCommissionBaoDi = 0;

	/**
	 * 保低增送分配置
	 */
	protected String baoDiConfigStr = "";

	/**
	 * 保低增送分配置
	 */
	protected List<BaoDiConfig> baoDiConfigList = new ArrayList<>();

	/**
	 * 是否产生了保低抽水
	 */
	protected boolean isBaoDiCommission = false;
	/**
	 * 是不是可以洗牌
	 */
	protected boolean isXipai = false;
	/**
	 * 洗牌需要的分数
	 */
	protected int xipaiScoure = 0;
	/**
	 * 洗牌的人名
	 */
	protected List<String> xipaiName=new ArrayList();

	/**
	 * AA赠送勾选项
	 */
	protected int AAScoure = 0;

	/**
	 * 是否开启托管
	 */
	protected boolean autoPlay = false;

	/**
	 * 上次autoPlay时间
	 */
	protected long lastAutoPlayTime = 0;

	/**
	 * 创房int参数列表
	 */
	protected List<Integer> intParams;

	/**
	 * 创房str参数列表
	 */
	protected List<String> strParams;


	protected long tablePayStartTime;  //牌桌游戏开始时间

	/**
	 * 包间名字
	 */
	protected String roomName;

	private int chatConfig;  //牌桌内聊天配置

	private int autoQuitTimeOut = 0; //长时间未准备，踢出房间：单位秒,0表示关闭此功能

	/*** 是否因为信用分不够解散房间 **/
	private boolean isDissByCreditLimit = false;

	/** 信用分不够的玩家 **/
	private String creditLimitPlayerNames;

	/** 是否打开新金币系统开关 0否，1是***/
	private int switchCoin = 0;
	/** 俱乐部信用分比例**/
	private int creditRate = 1;

	/**
	 * 比赛开始前信用分
	 */
	public HashMap<Integer,Long> creditMap =new HashMap<>() ;

	protected Map<Long,GroupUser> guMap = new HashMap<>();

	protected List<CreditCommission> commList = new ArrayList<>();
	protected List<GroupGoldLog> goldCommList = new ArrayList<>();

	protected int dyjCount = 0; // 临时变量

	/*** 同ip禁止进入***/
	protected boolean sameIpLimit = false;
	/*** 不打开GPS禁止进入***/
	protected boolean openGpsLimit = false;
	/*** 距离太近禁止进入***/
	protected boolean distanceLimit = false;
	/*** 是否允许信用负分***/
	protected boolean negativeCredit = false;

	/*** 最后点击开始下一局的玩家***/
	protected long lastStartNextUser = 0;
	/*** 推送给最后点开始的人发牌消息 ***/
	protected GeneratedMessage lastDealMsg = null;

	protected GoldRoom goldRoom;
	protected Map<Long, GoldRoomUser> goldRoomUserMap = new ConcurrentHashMap<>();

	protected CompetitionRoom competitionRoom;
	protected Map<Long, CompetitionRoomUser> competitionRoomUserMap = new ConcurrentHashMap<>();

	/*** soloRoom类型：SoloRoomUtil***/
	protected int soloRoomType;
	/*** soloRoom值***/
	protected long soloRoomValue;

	protected String masterName;

    /**
     * 是否亲友圈金币房间
     */
    protected int gtgMode = 0;
    protected int gtgJoinLimit = 0;
    protected int gtgDissLimit = 0;
    protected int gtgDifen = 0;


    public int getTableType() {
        return tableType;
    }

	public void setTableType(int tableType) {
		this.tableType = tableType;
		dbParamMap.put("tableType",tableType);
	}

	public long getLastAutoPlayTime() {
		return lastAutoPlayTime;
	}

	public void setLastAutoPlayTime(long lastAutoPlayTime) {
		this.lastAutoPlayTime = lastAutoPlayTime;
	}

	public void setCompetitionRoomId(long competitionRoomId) {
    	this.competitionRoomId=competitionRoomId;
	}

	public long getCompetitionRoomId() {
    	return this.competitionRoomId;
	}

	/**引用计数器**/
	protected final AtomicInteger referenceCounter = new AtomicInteger(0);

	public AtomicInteger getReferenceCounter() {
		return referenceCounter;
	}

	public void setMatchId(Long matchId) {
		if (matchId != null) {
			if (this.matchId != matchId.longValue()) {
				this.matchId = matchId.longValue();
				changeExtend();
			}
		} else if (this.matchId != 0L) {
			this.matchId = 0L;
			changeExtend();
		}
	}

	public long getTablePayStartTime() {
		return tablePayStartTime;
	}

	public void setTablePayStartTime(long tablePayStartTime) {
		this.tablePayStartTime = tablePayStartTime;
	}

	public GroupTableConfig getGroupTableConfig() {
		return groupTableConfig;
	}

	public void setMatchRatio(long matchRatio) {
		this.matchRatio = matchRatio;
	}

	public String getModeId() {
		return modeId;
	}

	public void setModeId(String modeId) {
		this.modeId = modeId;
	}

	public int getSpecialDiss() {
		return specialDiss;
	}

	public void setSpecialDiss(int specialDiss) {
		this.specialDiss = specialDiss;
	}

	/**
	 * 是否群主解散
	 */
	public boolean isGroupMasterDiss() {
		return getSpecialDiss() == 1;
	}

	/**
	 * 已结算的小局数
	 **/
	public int getPlayedBureau() {
		return playedBureau;
	}

	public void setLastCheckTime(long lastCheckTime) {
		this.lastCheckTime = lastCheckTime;
	}

	public long getLastCheckTime() {
		return lastCheckTime;
	}

	public int getIsShuffling() {
		return isShuffling;
	}

	public void setIsShuffling(int isShuffling) {
		this.isShuffling = isShuffling;
	}

	public String getAssisCreateNo() {
		return assisCreateNo;
	}

	public void setAssisCreateNo(String assisCreateNo) {
		this.assisCreateNo = assisCreateNo;
		changeExtend();
	}

	public String getAssisGroupNo() {
		return assisGroupNo;
	}

	public void setAssisGroupNo(String assisGroupNo) {
		this.assisGroupNo = assisGroupNo;
		changeExtend();
	}

	public int getAllowGroupMember() {
		return allowGroupMember;
	}

	public void setAllowGroupMember(int allowGroupMember) {
		this.allowGroupMember = allowGroupMember;
	}

	public boolean isCheckPay() {
		return checkPay;
	}

	public void setCheckPay(boolean checkPay) {
		this.checkPay = checkPay;
		changeExtend();
	}

	/**
	 * 房间模式0固定，1可观战，2可中途加入，3可下注
	 */
	protected Map<String, String> roomModeMap = new ConcurrentHashMap<>();

	/**
	 * 记录房间人员信息
	 */
	protected Map<Long, Player> roomPlayerMap = new ConcurrentHashMap<>();

	/**
	 * 是否是AA开房(所有)
	 */
	protected boolean isAAConsume = Boolean.parseBoolean(ResourcesConfigsUtil.loadServerPropertyValue("table.isAAConsume", "false"));

	protected int serverId = GameServerConfig.SERVER_ID;

	protected volatile int serverType = 1;//游戏服类型0练习场1普通场

	protected volatile String serverKey = "";
	protected volatile String groupIdStr = null;
	protected volatile long groupId = 0;
	protected volatile boolean isGroupRoom = false;
	protected volatile String groupTableKeyId = null;
	protected volatile long groupMasterId;

	protected GroupTableConfig groupTableConfig = null;
	protected GroupTable groupTable = null;

	public GroupTable getGroupTable() {
		return groupTable;
	}

	public void setGroupTable(GroupTable groupTable) {
		this.groupTable = groupTable;
	}

	public String getServerKey() {
		return serverKey;
	}

	public void setServerKey(String serverKey) {
		this.serverKey = serverKey;
	}

	public int getServerType() {
		return serverType;
	}

	public void setServerType(int serverType) {
		this.serverType = serverType;
	}

	/**
	 * 记录房间人员信息
	 */
	public Map<Long, Player> getRoomPlayerMap() {
		return roomPlayerMap;
	}

	public long getCreatorId() {
		return creatorId;
	}

	public void setCreatorId(long creatorId) {
		this.creatorId = creatorId;
	}

	public int getPayType() {
		return payType;
	}

	public void setPayType(int payType) {
		this.payType = payType;
	}

	public BaseTable() {
		lock = new ReentrantLock();
	}

	/**
	 * 房间模式0固定，1可观战，2可中途加入，3可下注
	 */
	public Map<String, String> getRoomModeMap() {
		return roomModeMap;
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

	public void loadFromDB(TableInf info) {
		if (info.getServerId() > 0) {
			serverId = info.getServerId();
		}
		this.tableInf = info;
		this.id = info.getTableId();
		this.tableType = info.getTableType();
		this.playType = info.getPlayType();
		this.roomId = info.getRoomId();
		this.masterId = info.getMasterId();
		this.createTime = info.getCreateTime();
		this.totalBureau = info.getTotalBureau();
		this.playBureau = info.getPlayBureau();
		this.state = SharedConstants.getTableState(info.getState());
		this.lastActionTime = info.getLastActionTime();
		this.isCompetition = info.getIsCompetition();
		this.disCardRound = info.getDisCardRound();
		this.nowDisCardSeat = info.getNowDisCardSeat();
		this.disCardSeat = info.getDisCardSeat();
		this.gotyeRoomId = info.getGotyeRoomId();
		this.daikaiTableId = info.getDaikaiTableId();
		this.finishBureau=info.getFinishBureau();
		String answer = info.getAnswerDiss();
		if (!StringUtils.isBlank(answer)) {
			String[] answerArr = answer.split("_");
			answerDissMap = DataMapUtil.implode(StringUtil.getValue(answerArr, 0));
			sendDissTime = StringUtil.getLongValue(answerArr, 1);

		}

		if (!StringUtils.isBlank(info.getConfig())) {
			config = StringUtil.explodeToIntList(info.getConfig());
		}
		this.lastWinSeat = info.getLastWinSeat();
		if (!StringUtils.isBlank(info.getPlayLog())) {
			this.playLog = info.getPlayLog();
		} else {
			this.playLog = "";
		}
		initPlayers();
		if (!checkPlayer(null)) {
			LogUtil.msgLog.info("BaseTable|dissReason|loadFromDB|1|" + getId() + "|" + getPlayBureau());
			diss();
			return;
		}
		initExtend(info.getExtend());
		if (!StringUtils.isBlank(info.getNowAction())) {
			initNowAction(info.getNowAction());
		}
		loadFromDB1(info);

	}

	public int initPlayers(Long mUserId, Player mPlayer) {
		String playerInfos = tableInf.getPlayers();
		if (!StringUtils.isBlank(playerInfos)) {
			int count = 0;
			Set<Integer> seatSet = new HashSet<>();
			if (StringUtils.isNotBlank(tableInf.getHandPai1()) || StringUtils.isNotBlank(tableInf.getOutPai1())) {
				seatSet.add(1);
			}
			if (StringUtils.isNotBlank(tableInf.getHandPai2()) || StringUtils.isNotBlank(tableInf.getOutPai2())) {
				seatSet.add(2);
			}
			if (StringUtils.isNotBlank(tableInf.getHandPai3()) || StringUtils.isNotBlank(tableInf.getOutPai3())) {
				seatSet.add(3);
			}
			if (StringUtils.isNotBlank(tableInf.getHandPai4()) || StringUtils.isNotBlank(tableInf.getOutPai4())) {
				seatSet.add(4);
			}
			if (StringUtils.isNotBlank(tableInf.getHandPai5()) || StringUtils.isNotBlank(tableInf.getOutPai5())) {
				seatSet.add(5);
			}
			if (StringUtils.isNotBlank(tableInf.getHandPai6()) || StringUtils.isNotBlank(tableInf.getOutPai6())) {
				seatSet.add(6);
			}
			if (StringUtils.isNotBlank(tableInf.getHandPai7()) || StringUtils.isNotBlank(tableInf.getOutPai7())) {
				seatSet.add(7);
			}
			if (StringUtils.isNotBlank(tableInf.getHandPai8()) || StringUtils.isNotBlank(tableInf.getOutPai8())) {
				seatSet.add(8);
			}
			if (StringUtils.isNotBlank(tableInf.getHandPai9()) || StringUtils.isNotBlank(tableInf.getOutPai9())) {
				seatSet.add(9);
			}
			if (StringUtils.isNotBlank(tableInf.getHandPai10()) || StringUtils.isNotBlank(tableInf.getOutPai10())) {
				seatSet.add(10);
			}

			List<Player> playerList0 = new ArrayList<>();
			List<Player> playerList1 = new ArrayList<>();

			String[] playerArr = playerInfos.split(";");
			for (String str : playerArr) {
				if (StringUtils.isBlank(str) || str.equals("null")) {
					continue;
				}
				count++;

				String[] values = str.split(",");
				long userId = StringUtil.getLongValue(values, 0);

				if (mUserId != null && mUserId.longValue() != userId) {
					continue;
				}

				Player player;
				if (mPlayer == null) {

					if (userId < 0) {
						player = PlayerManager.getInstance().getRobot(userId, playType);
					} else {
						player = PlayerManager.getInstance().getPlayer(userId);
					}
					if (player == null) {
						player = PlayerManager.getInstance().loadPlayer(userId, playType);
					}
				} else {
					player = mPlayer;
				}

				if (player == null) {
					continue;
				}

				if (!PlayerManager.getInstance().checkPlayer(playType, player)) {
					player = PlayerManager.getInstance().changePlayer(player, getPlayerClass());
				}

				if (player.getPlayingTableId() == 0) {
					player.setPlayingTableId(id);
					player.saveBaseInfo();
				}
				if (player.getPlayingTableId() != id) {
					continue;
				}
				player.initPlayInfo(str);
				player.initXipaiDataPlayInfo(str);//洗牌数据，最后3位
				player.setJoinTime(System.currentTimeMillis());
				int seat = player.getSeat();
				if (seat != 0) {
					playerList0.add(player);
					initPlayerCards(tableInf, seat, player);
					getPlayerMap().put(player.getUserId(), player);
					getSeatMap().put(seat, player);
				} else {
					playerList1.add(player);
					LogUtil.errorLog.warn("table user seat error0:userId={},tableId={},seat={}", player.getUserId(), id, seat);
				}
			}

			if ((mUserId == null && mPlayer == null) && seatSet.size() > 0 && playerList1.size() == 1 && (count == playerList1.size() + playerList0.size())) {
				int maxSeat = 0;
				for (Player player : playerList0) {
					seatSet.remove(Integer.valueOf(player.getSeat()));

					if (player.getSeat() > maxSeat) {
						maxSeat = player.getSeat();
					}
				}
				boolean recoverPlayer = false;
				int size = seatSet.size();
				if (size == 1) {
					playerList1.get(0).setSeat(seatSet.iterator().next());
					recoverPlayer = true;
				} else if (size == 0) {
					List<Integer> seatList = new ArrayList<>(maxSeat > 0 ? maxSeat : 1);
					for (int i = 1; i <= maxSeat; i++) {
						seatList.add(i);
					}
					for (Player player : playerList0) {
						seatList.remove(Integer.valueOf(player.getSeat()));
					}
					if (seatList.size() == 1) {
						playerList1.get(0).setSeat(seatList.get(0));
						recoverPlayer = true;
					} else if (seatList.size() == 0) {
						playerList1.get(0).setSeat(maxSeat + 1);
						recoverPlayer = true;
					}
				}
				if (recoverPlayer) {
					Player player = playerList1.get(0);
					int seat = player.getSeat();
					initPlayerCards(tableInf, seat, player);
					getPlayerMap().put(player.getUserId(), player);
					getSeatMap().put(seat, player);

					changePlayers();
				}
			}

			return playerList0.size();
		} else {
			return 0;
		}
	}

	private static void initPlayerCards(TableInf tableInf, int seat, Player player) {
		if (seat == 1) {
			player.initPais(tableInf.getHandPai1(), tableInf.getOutPai1());
		} else if (seat == 2) {
			player.initPais(tableInf.getHandPai2(), tableInf.getOutPai2());
		} else if (seat == 3) {
			player.initPais(tableInf.getHandPai3(), tableInf.getOutPai3());
		} else if (seat == 4) {
			player.initPais(tableInf.getHandPai4(), tableInf.getOutPai4());
		} else if (seat == 5) {
			player.initPais(tableInf.getHandPai5(), tableInf.getOutPai5());
		} else if (seat == 6) {
			player.initPais(tableInf.getHandPai6(), tableInf.getOutPai6());
		} else if (seat == 7) {
			player.initPais(tableInf.getHandPai7(), tableInf.getOutPai7());
		} else if (seat == 8) {
			player.initPais(tableInf.getHandPai8(), tableInf.getOutPai8());
		} else if (seat == 9) {
			player.initPais(tableInf.getHandPai9(), tableInf.getOutPai9());
		} else if (seat == 10) {
			player.initPais(tableInf.getHandPai10(), tableInf.getOutPai10());
		}
	}

	public int initPlayers() {
		return initPlayers(null, null);
	}

	public abstract void initExtend0(JsonWrapper extend);

	public final void initExtend(String extend) {
		JsonWrapper wrapper = new JsonWrapper(extend);

		playedBureau = wrapper.getInt("playedBureau", playBureau);

		serverType = wrapper.getInt("-1", 1);
		String tempServerKey = wrapper.getString("-2");
		if (StringUtils.isNotBlank(tempServerKey)) {
			serverKey = tempServerKey;
		}

		String str = wrapper.getString(0);
		if (StringUtils.isNotBlank(str)) {
			String[] temps = str.split("\\;");
			for (String temp : temps) {
				int idx = temp.indexOf(":");
				if (idx > 0) {
					roomModeMap.put(temp.substring(0, idx), temp.substring(idx + 1));
				}
			}
		}

		str = wrapper.getString("-3");
		if (StringUtils.isNotBlank(str)) {
			String[] temps = str.split("\\;");
			try {
				for (String temp : temps) {
					if (NumberUtils.isDigits(temp)) {
						long userId = Long.parseLong(temp);
						if (!getPlayerMap().containsKey(userId)) {
							Player player = ObjectUtil.newInstance(getPlayerClass());

							RegInfo user = UserDao.getInstance().selectUserByUserId(userId);
							player.loadFromDB(user);
							if (player.getState() == null) {
								player.changeState(player_state.entry);
							}
							player.setIsOnline(0);

							Player addPlayer = PlayerManager.getInstance().addPlayer(player);
							if (addPlayer != player) {
								addPlayer.loadFromDB(user);
							}
							roomPlayerMap.put(userId, addPlayer);
						}
					}
				}
			} catch (Exception e) {
				LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
			}
		}

		payType = wrapper.getInt("payType", -1);

		creatorId = wrapper.getLong("creatorId", 0);
		allowGroupMember = wrapper.getInt("allowGroupMember", 0);
		if (SharedConstants.isAssisOpen() && StringUtils.isNotBlank(wrapper.getString("assisCreateNo"))) {
			assisCreateNo = wrapper.getString("assisCreateNo");
			assisGroupNo = wrapper.getString("assisGroupNo");
		}
		isShuffling = wrapper.getInt("isShuffling", 0);

		checkPay = wrapper.getInt("checkPay", 1) == 1;

		matchId = wrapper.getLong("matchId", 0);
		matchRatio = wrapper.getLong("matchRatio", 1);
		String modeId = wrapper.getString("modeId");
		if (StringUtils.isNotBlank(modeId)) {
			this.modeId = modeId;
		}

		//信用房信息
		creditMode = wrapper.getInt("creditMode", 0);
		creditJoinLimit = wrapper.getLong("creditJoinLimit", 0);
		creditDissLimit = wrapper.getLong("creditDissLimit", 0);
		creditDifen = wrapper.getLong("creditDifen", 0);
		creditCommission = wrapper.getLong("creditCommission", 0);
		creditCommissionMode1 = wrapper.getInt("creditCommissionMode1", 0);
		creditCommissionMode2 = wrapper.getInt("creditCommissionMode2", 0);
		creditCommissionLimit = wrapper.getLong("creditCommissionLimit", 0);
		credit100 = wrapper.getLong("credit100", 0);
		isXipai = wrapper.getInt("isXipai", 0) == 1 ? true:false;
		xipaiScoure = wrapper.getInt("xipaiScoure", 0);
		AAScoure = wrapper.getInt("AAScoure", 0);
		creditCommissionBaoDi = wrapper.getLong("creditCommissionBaoDi", 0);
		initCredit100();
		autoPlay = wrapper.getInt("autoPlay", 0) == 1;

		String intParamsStr = wrapper.getString("intParams");
		if (!StringUtils.isBlank(intParamsStr)) {
			intParams = StringUtil.explodeToIntList(intParamsStr);
		}

		String strParamsStr = wrapper.getString("strParams");
		if (!StringUtils.isBlank(strParamsStr)) {
			strParams = StringUtil.explodeToStringList(strParamsStr,",");
		}

		roomName = wrapper.getString("roomName");
		chatConfig = wrapper.getInt("chatConfig",0);
		autoQuitTimeOut = wrapper.getInt("autoQuit",0);
		switchCoin = wrapper.getInt("switchCoin",0);
		creditRate = wrapper.getInt("creditRate",1);

		sameIpLimit = wrapper.getInt("sameIpLimit", 0) == 1;
		openGpsLimit = wrapper.getInt("openGpsLimit", 0) == 1;
		distanceLimit = wrapper.getInt("distanceLimit", 0) == 1;
		negativeCredit = wrapper.getInt("negativeCredit", 0) == 1;
		String baoDiConfigStr = wrapper.getString("baoDiConfig");
		if(StringUtils.isNotBlank(baoDiConfigStr)){
			this.baoDiConfigStr = baoDiConfigStr;
			initBaoDiConfig();
		}
		goldRoomId = wrapper.getLong("goldRoomId", 0);
		soloRoomType = wrapper.getInt("soloRoomType", 0);
		soloRoomValue = wrapper.getLong("soloRoomValue", 0);
		competitionRoomId = wrapper.getLong("competitionRoomId", 0);
		gtgMode = wrapper.getInt("gtgMode", 0);
		if (gtgMode == 1) {
			gtgDifen = wrapper.getInt("gtgDifen", 0);
			gtgJoinLimit = wrapper.getInt("gtgJoinLimit", 0);
			gtgDissLimit = wrapper.getInt("gtgDissLimit", 0);
		}
        initExtend0(wrapper);
    }

	public abstract Map<String, Object> saveDB(boolean asyn);

	public abstract JsonWrapper buildExtend0(JsonWrapper extend);

	public final String buildExtend() {
		JsonWrapper wrapper = new JsonWrapper("");

		wrapper.putInt("playedBureau", playedBureau);

		if (roomModeMap != null && roomModeMap.size() > 0) {
			StringBuilder strBuilder = new StringBuilder();
			for (Map.Entry<String, String> kv : roomModeMap.entrySet()) {
				strBuilder.append(";").append(kv.getKey()).append(":").append(kv.getValue());
			}
			wrapper.putString(0, strBuilder.substring(1));
		}

		wrapper.putInt("-1", serverType);
		if (StringUtils.isNotBlank(serverKey)) {
			wrapper.putString("-2", serverKey);
		}

		if (roomPlayerMap != null && roomPlayerMap.size() > 0) {
			StringBuilder strBuilder = new StringBuilder();
			for (Map.Entry<Long, Player> kv : roomPlayerMap.entrySet()) {
				strBuilder.append(";").append(kv.getKey().toString());
			}
			wrapper.putString("-3", strBuilder.substring(1));
		}

		wrapper.putInt("payType", payType);

		wrapper.putLong("creatorId", creatorId);
		if (allowGroupMember != 0)
			wrapper.putInt("allowGroupMember", allowGroupMember);
		if (isKaiYiJu()) {
			if (StringUtils.isNotBlank(assisCreateNo))
				wrapper.putString("assisCreateNo", assisCreateNo);
			if (StringUtils.isNotBlank(assisGroupNo))
				wrapper.putString("assisGroupNo", assisGroupNo);
		}
		if (isShuffling != 0)
			wrapper.putInt("isShuffling", isShuffling);

		wrapper.putInt("checkPay", checkPay ? 1 : 0);
		if (matchId != 0L)
			wrapper.putLong("matchId", matchId);
		if (matchRatio != 1L)
			wrapper.putLong("matchRatio", matchRatio);
		if (StringUtils.isNotBlank(modeId) && !"0".equals(modeId)) {
			wrapper.putString("modeId", modeId);
		}

		//信用房信息
		wrapper.putInt("creditMode", creditMode);
		wrapper.putLong("creditJoinLimit", creditJoinLimit);
		wrapper.putLong("creditDissLimit", creditDissLimit);
		wrapper.putLong("creditDifen", creditDifen);
		wrapper.putLong("creditCommission", creditCommission);
		wrapper.putInt("creditCommissionMode1", creditCommissionMode1);
		wrapper.putInt("creditCommissionMode2", creditCommissionMode2);
		wrapper.putLong("creditCommissionLimit", creditCommissionLimit);
		wrapper.putLong("credit100", credit100);
		wrapper.putLong("creditCommissionBaoDi", creditCommissionBaoDi);

		wrapper.putInt("autoPlay", autoPlay ? 1 : 0);

		wrapper.putString("intParams", StringUtil.implode(intParams, ","));
		wrapper.putString("strParams", StringUtil.implode(strParams, ","));

		wrapper.putString("roomName",roomName);
		wrapper.putInt("chatConfig",chatConfig);
		wrapper.putInt("autoQuit",autoQuitTimeOut);
		wrapper.putInt("switchCoin",switchCoin);
		wrapper.putInt("creditRate",creditRate);
		wrapper.putInt("AAScoure",AAScoure);
		if (sameIpLimit) {
			wrapper.putInt("sameIpLimit", sameIpLimit ? 1 : 0);
		}
		if (openGpsLimit) {
			wrapper.putInt("openGpsLimit", openGpsLimit ? 1 : 0);
		}
		if (distanceLimit) {
			wrapper.putInt("distanceLimit", distanceLimit ? 1 : 0);
		}
		if (negativeCredit) {
			wrapper.putInt("negativeCredit", negativeCredit ? 1 : 0);
		}

		if (isXipai) {
			wrapper.putInt("isXipai", isXipai ? 1 : 0);
		}
		if (xipaiScoure > 0) {
			wrapper.putInt("xipaiScoure", xipaiScoure);
		}
		if(baoDiConfigList.size() > 0){
			wrapper.putString("baoDiConfig", baoDiConfigStr);
		}
		if (goldRoomId > 0) {
			wrapper.putLong("goldRoomId", goldRoomId);
		}
		if (soloRoomType > 0) {
			wrapper.putInt("soloRoomType", soloRoomType);
		}
		if (soloRoomValue > 0) {
			wrapper.putLong("soloRoomValue", soloRoomValue);
		}
		if (competitionRoomId > 0) {
			wrapper.putLong("competitionRoomId", competitionRoomId);
		}
		if (gtgMode == 1) {
			wrapper.putInt("gtgMode", gtgMode);
			wrapper.putLong("gtgDifen", gtgDifen);
			wrapper.putLong("gtgJoinLimit", gtgJoinLimit);
			wrapper.putLong("gtgDissLimit", gtgDissLimit);
		}
		return buildExtend0(wrapper).toString();
	}

	/**
	 * 获取游戏code
	 *
	 * @return
	 */
	public String loadGameCode() {
		return "gameCode" + playType;
	}

	/**
	 * 是否比赛场
	 *
	 * @return
	 */
	public boolean isMatchRoom() {
		return matchId > 0L;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public abstract <T> T getPlayer(long id, Class<T> cl);

	/**
	 * 玩家退出
	 *
	 * @param player
	 */
	public boolean quitPlayer(Player player) {
		synchronized (this) {
			if (!getPlayerMap().containsKey(player.getUserId())) {
				return false;
			}

			if (!canQuit(player)) {
				return false;
			}

			if (getMasterId() == player.getUserId()) {
				if (isGoldRoom() || isSoloRoom() || isDaikaiTable() || StringUtils.isNotBlank(serverKey) || isGroupRoom()) {
				}else {
					player.writeErrMsg(LangHelp.getMsg(LangMsg.code_11));
					return false;
				}
			}
			int seat = player.getSeat();
			quitPlayer1(player);
			getSeatMap().remove(player.getSeat());
			getPlayerMap().remove(player.getUserId());
			player.clearTableInfo();
			player.cleanXipaiData();
			if (isKaiYiJu()) {
				String nStatus = getPlayerCount() == getMaxPlayerCount() ? "2" : "0";
				AssisServlet.sendRoomStatus(this, nStatus);
			}
			changePlayers();
			LogUtil.msgLog.info("BaseTable|quitPlayer|" + getId() + "|" + getPlayBureau() + "|" + player.getUserId() + "|" + seat);
			return true;
		}
	}

	public void onPlayerQuitSuccess(Player player) {
		onPlayerQuitSuccess(player, 0, true);
	}

	/**
	 * @param reason 1主动退出 2被踢出
	 * @param callme 是否给自己发退出消息
	 */
	public void onPlayerQuitSuccess(Player player, int reason, boolean callme) {
		try {
			//是否发生房主变动
			boolean change = false;
			String tableKeyId = getServerKey();
			if (getMasterId() == player.getUserId()) {
				change = makeOverMasterId(player);
			}
			LogUtil.msgLog.info("onPlayerQuitSuccess|" + getId() + "|" + getPlayBureau() + "|" + player.getUserId() + "|" + player.getSeat() + "|" + tableKeyId);

			boolean checkDiss = true;
			if (org.apache.commons.lang3.math.NumberUtils.isDigits(tableKeyId)) {
				int ret = GroupDao.getInstance().deleteTableUser(tableKeyId, String.valueOf(player.getUserId()),loadGroupIdLong());
				if (ret > 0) {
					HashMap<String, Object> map = new HashMap<>();
					map.put("keyId", tableKeyId);
					map.put("count", -1);
					if (groupTable != null) {
						groupTable.changeCurrentCount(-1);
					}
					GroupDao.getInstance().updateGroupTableByKeyId(map);
				}
			} else if (tableKeyId != null && tableKeyId.startsWith("group") && tableKeyId.contains("_")) {

				String tableKey = tableKeyId.split("_")[1];

				HashMap<String, Object> map = new HashMap<>();
				map.put("count", -1);
				map.put("keyId", tableKey);
				if (GroupDao.getInstance().updateGroupTableByKeyId(map) > 0) {
					if (Redis.isConnected() && NumberUtils.isDigits(tableKey)) {
						groupTable = GroupDao.getInstance().loadGroupTableByKeyId(tableKey);
						RedisUtil.zadd(GroupRoomUtil.loadGroupKey(groupTable.getGroupId().toString(), groupTable.loadGroupRoom()), GroupRoomUtil.loadWeight(groupTable.getCurrentState(), getPlayerCount(), groupTable.getCreatedTime()), tableKey);
						groupTable.setCurrentCount(getPlayerCount());
						RedisUtil.hset(GroupRoomUtil.loadGroupTableKey(groupTable.getGroupId().toString(), groupTable.loadGroupRoom()), tableKey, JSON.toJSONString(groupTable));
					}
				}

				GroupDao.getInstance().deleteTableUser(tableKey, String.valueOf(player.getUserId()),loadGroupIdLong());
				checkDiss = false;
			}

			int groupId = 0;
			if (isGroupRoom()) {
				groupId = Integer.parseInt(loadGroupId());
			}

			ComRes.Builder com= SendMsgUtil.buildComRes(WebSocketMsgType.res_code_tablequit, String.valueOf(player.getUserId()),String.valueOf(calcTableType()), getPlayType(), reason, 0, groupId, groupId);

			GeneratedMessage msg = com.build();
			if (callme) {
				player.writeSocket(msg);
			}

			broadMsg(msg);
			if (change) {
				Player player1 = getPlayerMap().get(getMasterId());
				if (player1 != null) {
					//代开房间新生成的房主id消息
					ComRes.Builder com2 = SendMsgUtil.buildComRes(WebSocketMsgType.res_com_code_daikaimasterid, "" + player1.getUserId());
					//将新房主id消息发给房主一个人
					GeneratedMessage msg2 = com2.build();

					for (Player tableplayer : getSeatMap().values()) {
						tableplayer.writeSocket(msg2);
					}
					for (Player tableplayer : getRoomPlayerMap().values()) {
						tableplayer.writeSocket(msg2);
					}
				}
			}

			if (checkDiss && getPlayerCount() <= 0 && StringUtils.isNotBlank(tableKeyId)) {

				// 解散前发送解散消息
				ComRes.Builder com1 = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_disstable,String.valueOf(calcTableType()), playType, groupId, playedBureau);

				GeneratedMessage msg1 = com1.build();
				for (Player player0 : getSeatMap().values()) {
					if (player0.getUserId() != player.getUserId())
						player0.writeSocket(msg1);
				}

				for (Player player0 : roomPlayerMap.values()) {
					if (player0.getUserId() != player.getUserId())
						player0.writeSocket(msg1);
				}
				LogUtil.msgLog.info("BaseTable|dissReason|onPlayerQuitSuccess|1|" + getId() + "|" + getPlayBureau() + "|" + player.getUserId());
				diss();
			}

			checkDissOnQuit(player);
		} catch (Exception e) {
			LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
		}
	}

	public boolean ready(Player player) {
		StringBuilder sb=new StringBuilder("BaseTable|ready");
		sb.append("|").append(getId());
		sb.append("|").append(getPlayBureau());
		sb.append("|").append(player.getUserId());
		LogUtil.msgLog.info(sb.toString());
		player.changeState(player_state.ready);
		return true;
	}



	/**
	 * 加入桌子
	 *
	 * @param player
	 */
	public final boolean joinPlayer(Player player) {
		synchronized (this) {
			if (deleted) {
				LogUtil.msgLog.error("BaseTable|joinPlayer|error|deleted|" + getId() + "|" + getPlayBureau());
				return false;
			}
			if (!isCanJoin(player, false)) {
				if (!getPlayerMap().containsKey(player.getUserId())) {
					player.setSeat(0);
					player.saveBaseInfo();
				}
				return false;
			}

			int seat = randomSeat();
			if (seat <= 0) {
				return false;
			} else {
				int seat0 = player.getSeat();
				if (seat0 > 0 && seat0 <= getMaxPlayerCount()) {
					if (getSeatMap().containsKey(seat0)) {
						player.setSeat(seat);
					} else {
						player.setSeat(seat0);
						seat = seat0;
					}
				} else {
					player.setSeat(seat);
				}
			}

			player.setPlayingTableId(id);
			player.setTable(this);
			player.setLastActionBureau(getPlayBureau() <= 1 ? 0 : getPlayBureau());
			if (player_state.ready != player.getState()) {
				player.changeState(player_state.entry);
			}
			player.getMyExtend().setLatitudeLongitude("");
			player.changeIsLeave(0);
			player.setTotalPoint(0);
			player.setMaxPoint(0);
			player.setTotalBoom(0);
			player.setIsEntryTable(SharedConstants.table_online);

			player.saveBaseInfo();

			if (isDaikaiTable() && getPlayerCount() < 1) {
				setMasterId(player.getUserId());
			} else if (StringUtils.isNotBlank(serverKey) && (getPlayerCount() == 0 || getMasterId() <= 0)) {
				setMasterId(player.getUserId());
			}

			player.setSeat(seat);
			getPlayerMap().put(player.getUserId(), player);
			getSeatMap().put(seat, player);

			roomPlayerMap.remove(player.getUserId());
			player.getMyExtend().getPlayerStateMap().remove("1");
			player.getMyExtend().getPlayerStateMap().remove("seat");
			player.getMyExtend().getPlayerStateMap().put("0", "1");
			if (getPlayBureau() > 1 || (getPlayBureau() == 1 && state != table_state.ready && masterId != player.getUserId())) {
				player.getMyExtend().getPlayerStateMap().put("2", "1");
				player.getMyExtend().getPlayerStateMap().put("cur", getPlayBureau() + "_0");
			}


			player.newRecord();

			updateDaikaiTablePlayer();
			updateRoomPlayers();

			joinPlayer1(player);
			changePlayers();

			if (groupTable == null && NumberUtils.isDigits(serverKey)) {
				try {
					groupTable = GroupDao.getInstance().loadGroupTableByKeyIdMaster(serverKey);
				} catch (Throwable t) {
					LogUtil.errorLog.error("Throwable:" + t.getMessage(), t);
				}
			}

			logJoinPlayer(player,0);

			if (groupTable != null) {
				try {
					HashMap<String, Object> map = new HashMap<>();
					map.put("keyId", groupTable.getKeyId().toString());
					map.put("count", 1);
					GroupDao.getInstance().updateGroupTableByKeyId(map);

					groupTable.changeCurrentCount(1);

					TableUser tableUser = new TableUser();
					tableUser.setCreatedTime(new Date());
					tableUser.setGroupId(groupTable.getGroupId());
					tableUser.setTableId(groupTable.getTableId());
					tableUser.setPlayResult(0);
					tableUser.setTableNo(groupTable.getKeyId());
					tableUser.setUserId(player.getUserId());

					GroupDao.getInstance().createTableUser(tableUser);
				} catch (Throwable t) {
					LogUtil.errorLog.error("Throwable:" + t.getMessage(), t);
				}
			} else if (isGroupRoom()) {
				String[] msgs = serverKey.split("_");
				logJoinPlayer(player,1);
				if (msgs.length >= 2) {
					try {
						GroupTable gt = GroupDao.getInstance().loadGroupTableByKeyIdMaster(msgs[1]);

						logJoinPlayer(player,2);

						if (gt != null) {
							gt.setCurrentCount(getPlayerCount());
							groupTable = gt;
							if (Redis.isConnected()) {
								RedisUtil.zadd(GroupRoomUtil.loadGroupKey(gt.getGroupId().toString(), gt.loadGroupRoom()), GroupRoomUtil.loadWeight(gt.getCurrentState(), gt.getCurrentCount(), gt.getCreatedTime()), gt.getKeyId().toString());
								RedisUtil.hset(GroupRoomUtil.loadGroupTableKey(gt.getGroupId().toString(), gt.loadGroupRoom()), msgs[1], JSON.toJSONString(gt));
							}

							HashMap<String, Object> map = new HashMap<>();
							map.put("count", 1);
							map.put("keyId", gt.getKeyId().toString());
							GroupDao.getInstance().updateGroupTableByKeyId(map);

							TableUser tableUser = new TableUser();
							tableUser.setCreatedTime(new Date());
							tableUser.setGroupId(gt.getGroupId());
							tableUser.setTableId(gt.getTableId());
							tableUser.setPlayResult(0);
							tableUser.setTableNo(gt.getKeyId());
							tableUser.setUserId(player.getUserId());

							GroupDao.getInstance().createTableUser(tableUser);

							logJoinPlayer(player,3);
						}
					} catch (Throwable t) {
						LogUtil.errorLog.error("Throwable:" + t.getMessage(), t);
					}
				}
			}
			player.setJoinTime(System.currentTimeMillis());
			player.changeExtend();
			if (isKaiYiJu()) {
				String nStatus = getPlayerCount() == getMaxPlayerCount() ? "2" : "0";
				AssisServlet.sendRoomStatus(this, nStatus);
			}
			return true;
		}
	}

	public void logJoinPlayer(Player player, int seq) {
		StringBuilder sb = new StringBuilder("BaseTable|joinPlayer");
		sb.append("|").append(seq);
		sb.append("|").append(getId());
		sb.append("|").append(player.getUserId());
		sb.append("|").append(player.getSeat());
		sb.append("|").append(serverKey);
		if (groupTable == null) {
			sb.append("|null");
		}
		LogUtil.msgLog.info(sb.toString());
	}

	public int updateRoomPlayers() {
		if (!isGoldRoom()) {
			HashMap<String, Object> paramMap = new HashMap<String, Object>(4);
			List<Long> idList = new ArrayList<>(getPlayerMap().keySet());
			paramMap.put("players", StringUtil.implode(idList));
			return TableDao.getInstance().updateRoom(id, paramMap);
		}
		return -1;
	}

	public Map<String, Object> loadCurrentDbMap() {
		// copy 一份map
		Map<String, Object> tempMap = new HashMap<>();
		if (deleted) {
			return tempMap;
		}
		synchronized (this) {
			if (deleted) {
				return tempMap;
			}
			Iterator<Entry<String, Object>> it = dbParamMap.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, Object> kv = it.next();
				tempMap.put(kv.getKey(), kv.getValue());
				it.remove();
			}
		}
		return tempMap;
	}

	protected abstract void initNowAction(String nowAction);

	protected abstract String buildNowAction();

	protected abstract boolean quitPlayer1(Player player);

	public int randomSeat() {
		List<Integer> list = new ArrayList<>();
		for (int i = 1; i <= getMaxPlayerCount(); i++) {
			list.add(i);
		}

		List<Integer> seatlist = new ArrayList<>(getSeatMap().keySet());
		list.removeAll(seatlist);
		if (list.isEmpty()) {
			return 0;
		}

		int index = MathUtil.mt_rand(0, list.size() - 1);
		return list.get(index);
	}

	protected abstract boolean joinPlayer1(Player player);

	/**
	 * 解散
	 */
	public int diss() {

		synchronized (this) {

			if (deleted) {
				LogUtil.msgLog.info("BaseTable|diss|deleted|" + getId() + "|" + getPlayBureau());
				return 0;
			}

            Long dataDate = Long.valueOf(new SimpleDateFormat("yyyyMMdd").format(new Date()));
			final Collection<Player> players = new ArrayList<>(getPlayerMap().values());
			LogUtil.msgLog.info("BaseTable|diss|" + getId() + "|" + getPlayBureau() + "|" + JacksonUtil.writeValueAsString(groupTable));
			try {
				if (playedBureau > 0) {
					if (GameServerConfig.checkDataStatistics()) {
						try {
							calcDataStatistics2();
							calcDataStatisticsBjd();
							savePlayLogTable();
							BjdUtil.share2XianLiaoGroup(this);
						} catch (Exception e) {
							LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
						}
					}
				}

				if (isKaiYiJu()) {
					if (playedBureau > 0) {
						AssisServlet.sendRoomResult(this);
					}
					AssisServlet.sendRoomStatus(this, "1");
				}
				try {
					GroupTable groupTable = null;
					if (NumberUtils.isDigits(serverKey)) {
						groupTable = GroupDao.getInstance().loadGroupTableByKeyIdMaster(getServerKey());

						if (groupTable != null && !groupTable.isOver()) {
							HashMap<String, Object> tableMap = new HashMap<>();
							tableMap.put("keyId", getServerKey());
							tableMap.put("currentState", getDissCurrentState());
							tableMap.put("playedBureau", getPlayedBureau());
							tableMap.put("players", getPlayerNameString());
							GroupDao.getInstance().updateGroupTableByKeyId(tableMap);

							if (playBureau > 1) {
								HashMap<String, Object> map = new HashMap<>();
								map.put("keyId", groupTable.getConfigId().toString());
								map.put("count", 1);
								GroupDao.getInstance().updateGroupTableConfigByKeyId(map);
							} else {
								int ret1 = GroupDao.getInstance().deleteTableUser(getServerKey(),groupTable.getGroupId());
								int ret2 = GroupDao.getInstance().deleteGroupTableByKeyId(groupTable.getKeyId());

								LogUtil.msgLog.info("group table diss:deleteTableUser=" + ret1 + ",deleteGroupTable=" + ret2 + ",groupTable=" + JacksonUtil.writeValueAsString(groupTable));

								//退还钻石/房卡
								boolean repay = TableManager.repay(this, null, groupTable);
								if(!repay){
									GroupTableConfig groupTableConfig = GroupDao.getInstance().loadGroupTableConfig(groupTable.getConfigId());
									if (groupTableConfig != null && groupTableConfig.getPayType().intValue() != 1) {
										int payValue = PayConfigUtil.get(groupTableConfig.getGameType(), groupTableConfig.getGameCount(), groupTableConfig.getPlayerCount(), 1);
										GroupUser groupUser = GroupDao.getInstance().loadGroupMaster(groupTableConfig.getParentGroup().longValue() == 0 ? groupTableConfig.getGroupId().toString() : groupTableConfig.getParentGroup().toString());
										if (groupUser != null && payValue > 0) {
											Player player1 = PlayerManager.getInstance().getPlayer(groupUser.getUserId());
											if (player1 != null) {
												player1.changeCards(payValue, 0, true, true, CardSourceType.groupTable_diss_FZ);
											} else {
												RegInfo user = UserDao.getInstance().selectUserByUserId(groupUser.getUserId());
												if (user != null) {
													UserDao.getInstance().updateUserCards(user.getUserId(), user.getFlatId(), user.getPf(), 0, payValue);

													if (user.getIsOnLine() == 1 && user.getEnterServer() > 0) {
														ServerUtil.notifyPlayerCards(user.getEnterServer(), user.getUserId(), 0, payValue);
													}
												}
											}

//											MessageUtil.sendMessage(true, true, UserMessageEnum.TYPE0, player1 != null ? player1 : groupUser.getUserId(), "军团房间【" + groupTable.getTableId() + "】未开局被解散，获得钻石x" + payValue, null);
										}
									}
								}
								groupTable = null;
							}
						} else {
							deleted = true;
						}
					} else if (isGroupRoom()) {
						String[] msgs = serverKey.split("_");
						if (msgs.length >= 2) {
							if (Redis.isConnected()) {
								RedisUtil.zrem(GroupRoomUtil.loadGroupKey(loadGroupId(), msgs.length >= 3 ? Integer.parseInt(msgs[2]) : 0), msgs[1]);
								RedisUtil.hdel(GroupRoomUtil.loadGroupTableKey(loadGroupId(), msgs.length >= 3 ? Integer.parseInt(msgs[2]) : 0), msgs[1]);
							}
                            transferTempCredit();
							groupTable = GroupDao.getInstance().loadGroupTableByKeyIdMaster(msgs[1]);
							if (groupTable != null && !groupTable.isOver()) {
								HashMap<String, Object> map = new HashMap<>();
								map.put("currentState", getDissCurrentState());
								map.put("keyId", msgs[1]);
								map.put("playedBureau", getPlayedBureau());
								map.put("players", getPlayerNameString());
								GroupDao.getInstance().updateGroupTableByKeyId(map);
								LogUtil.msgLog.info("diss group table success:msg=" + JacksonUtil.writeValueAsString(groupTable));

								if (groupTable.isNotStart() || isGroupRoomReturnConsume()) {
									//根据payType返回钻石currentState
									boolean repay = TableManager.repay(this, null, groupTable);
									if (!repay) {
										String[] tempMsgs = new JsonWrapper(groupTable.getTableMsg()).getString("strs").split(";")[0].split("_");
										String payType = tempMsgs[0];
										if (tempMsgs.length >= 4) {
											int tempPay = Integer.parseInt(tempMsgs[3]);
											if (("2".equals(payType) || "3".equals(payType)) && tempPay > 0) {
												CardSourceType sourceType = getCardSourceType(Integer.parseInt(payType));
												Player payPlayer = PlayerManager.getInstance().getPlayer(Long.valueOf(tempMsgs[2]));
												if (payPlayer != null) {
													payPlayer.changeCards(tempPay, 0, true, sourceType);
												} else {
													RegInfo user = UserDao.getInstance().selectUserByUserId(Long.valueOf(tempMsgs[2]));
													payPlayer = ObjectUtil.newInstance(getPlayerClass());
													payPlayer.loadFromDB(user);
													payPlayer.changeCards(tempPay, 0, true, sourceType);

													if (payPlayer.getEnterServer() > 0 && user.getIsOnLine() == 1) {
														ServerUtil.notifyPlayerCards(payPlayer.getEnterServer(), payPlayer.getUserId(), 0, Long.valueOf(tempMsgs[3]));
													}
												}
											}
										}
									}
								}
							} else {
								deleted = true;
							}
						}
					}

					int maxPoint = 0;
					for (Player player : players) {
						if (player.loadScore() > maxPoint) {
							maxPoint = player.loadScore();
						}
					}

					//统计系统打牌数据
					if (playedBureau > 0) {
						UserStatistics userStatistics0 = new UserStatistics("system", 0, isMatchRoom() ? "match" : isCompetitionRoom() ? "competition" : (isGoldRoom() ? "gold" : (isGroupRoom() ? "group" : "common")), playType, playedBureau);
						UserDao.getInstance().saveUserStatistics(userStatistics0);
					}

					List<Player> bigWin = new ArrayList<>();
					int bigWinPoint = 0;
					for (Player player : players) {
						if (groupTable != null && getPlayedBureau() > 0) {
							HashMap<String, Object> map = new HashMap<>();
							map.put("groupId", groupTable.getGroupId().toString());
							map.put("userId", String.valueOf(player.getUserId()));
							map.put("count2", 1);
							GroupDao.getInstance().updateGroupUser(map);

							updatePlayerScore(player, player.loadScore() == maxPoint ? 1 : -1);
						}

						String timeRemoveBindStr = ResourcesConfigsUtil.loadServerPropertyValue("periodRemoveBind");
						if (this.playBureau > 1 && !StringUtil.isBlank(timeRemoveBindStr) && !"0".equals(timeRemoveBindStr)) {
							// 刷新最后游戏时间
							player.setLastPlayTime(TimeUtil.now());
						}

						//统计用户输赢数据
						UserStatistics userStatistics = player.isRobot() ? loadRobotUserStatistics(player) : loadPlayerUserStatistics(player);

						if (userStatistics != null) {
							UserDao.getInstance().saveUserStatistics(userStatistics);
						}
						if (player.loadScore() > bigWinPoint) {
							bigWin.clear();
							bigWin.add(player);
							bigWinPoint = player.loadScore();
						} else if (player.loadScore() > 0 && player.loadScore() == bigWinPoint) {
							bigWin.add(player);
						}

                        if (player.isRobot() && isGoldRoom()) {
                            if (player.getWinGold() < 0) {
                                GoldDataStatistics data = new GoldDataStatistics(dataDate, SourceType.goldRoom_robot_total_lose, 0l, 1, player.getWinGold());
                                DataStatisticsDao.getInstance().saveOrUpdateGoldDataStatistics(data);

                                GoldDataStatistics data1 = new GoldDataStatistics(dataDate, SourceType.goldRoom_robot_playType_lose, Integer.valueOf(playType).longValue(), 1, player.getWinGold());
                                DataStatisticsDao.getInstance().saveOrUpdateGoldDataStatistics(data1);
                            } else if (player.getWinGold() > 0) {
                                GoldDataStatistics data = new GoldDataStatistics(dataDate, SourceType.goldRoom_robot_total_win, 0l, 1, player.getWinGold());
                                DataStatisticsDao.getInstance().saveOrUpdateGoldDataStatistics(data);

                                GoldDataStatistics data1 = new GoldDataStatistics(dataDate, SourceType.goldRoom_robot_playType_win, Integer.valueOf(playType).longValue(), 1, player.getWinGold());
                                DataStatisticsDao.getInstance().saveOrUpdateGoldDataStatistics(data1);
                            }
                        }
					}
					if (playedBureau == totalBureau) {
						if (!bigWin.isEmpty()) {// 统计玩家大赢家次数
							for (Player player : bigWin) {
								NewPlayerGiftActivityCmd.updateBigWinNum(player);  //大赢家次数更新
							}
						}
					} else {
						//打筒子
						if (playedBureau > 0 && !bigWin.isEmpty() && "1".equals(ResourcesConfigsUtil.loadServerPropertyValue("isDtzApp"))) {
							for (Player player : bigWin) {
								NewPlayerGiftActivityCmd.updateBigWinNum(player);  //大赢家次数更新
							}
						}
					}
					for (Player player : players) {
						if (player.isRobot()) {
							PlayerManager.getInstance().removePlayer(player);
						}

						UserFirstmyth bean = player.getMyExtend().buildFrstmyth(getWanFa(), isGroupRoom() ? Integer.parseInt(loadGroupId()) : 0);
						if (bean != null) {
							UserFirstmythDao.getInstance().saveUserFirstmyth(bean);
						}

						// 牌局统计活动
						if (isNormalOver() && !isCompetitionRoom() && !isGoldRoom() && !isMatchRoom() && ActivityConfig.isActivityActive(ActivityConfig.activity_game_bureau)) {
							UserGameBureauDao.getInstance().saveUserGameBureau(new UserGameBureau(player.getUserId(), player.getName(), 0, "", 0));
						}
					}
				} catch (Exception e) {
					LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
				}

				if (this.playBureau > 1) {
					// 新年红包
					try {
						calcActivity(ActivityConstant.activity_xn_hb, isTiqianDiss() ? 1 : 0);
						recordUserGameRebate();
					} catch (Exception e) {
						LogUtil.msgLog.error("{}",e);
					}
				}

				if (isGoldRoom()) {
					try {
						goldRoom.setCurrentState(GoldRoom.STATE_NORMAL_OVER);
						GoldRoomDao.getInstance().updateGoldRoomCurrentState(goldRoomId, GoldRoom.STATE_NORMAL_OVER);
						GoldRoomDao.getInstance().freezeGoldRoom(goldRoomId);
						GoldRoomDao.getInstance().freezeGoldRoomUser(goldRoomId);
						GoldRoomDao.getInstance().deleteGoldRoomUserByRoomId(goldRoomId);
						GoldRoomDao.getInstance().deleteGoldRoomByKeyId(goldRoomId);
					} catch (Exception e) {
						LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
					}
				}

			} finally {
				long winner = 0;
				int ret;
				try {
					//赢家的id
					winner = getSeatMap().get(getLastWinSeat()) != null ? getSeatMap().get(getLastWinSeat()).getUserId() : 0;

					Collection<Player> players1 = new ArrayList<>(roomPlayerMap.values());
					getPlayerMap().clear();
					getSeatMap().clear();
					roomPlayerMap.clear();
					roomModeMap.clear();

					for (Player player : players) {
						LogUtil.msgLog.info("BaseTable|diss|" + getId() + "|" + getPlayBureau() + "|" + player.getUserId() + "|" + player.getSeat() + "|" + player.loadScore() + "|" + player.getTotalPoint() + "|" + player.getWinLoseCredit() + "|" + player.getCommissionCredit());

						//记录结算分
						if (isCompetitionRoom() && getCompetitionRoomUser(player.getUserId()) != null) {
							getCompetitionRoomUser(player.getUserId()).setClearingScore(player.getTotalPoint());
						}

						player.setAutoPlayCheckedTime(0);
						player.setAutoPlayChecked(false);
						player.setAutoPlayCheckedTimeAdded(false);
						player.clearTableInfo(this, false);
						player.setDissCount(0);
						player.getMyExtend().getPlayerStateMap().clear();
						player.changeExtend();
						player.clearTingMsg();
						player.saveBaseInfo(false);
						player.setGoldResult(0);

						if (player.isRobot()) {
							RobotManager.recycleRobot(player.getUserId());
							player.resetRobotActionCounter();
						}
					}
					for (Player player : players1) {
						player.clearTableInfo(this, false);
						player.getMyExtend().getPlayerStateMap().clear();
						player.changeExtend();
						player.saveBaseInfo(false);
					}
					LogUtil.msgLog.info("TableManager|delTable|diss|1|" + getId() + "|" + getPlayBureau() + "|" + getServerKey());
					ret = TableManager.getInstance().delTable(this, deleted);

					deleted = true;

					if (isMatchRoom()) {
						final MatchBean matchBean = JjsUtil.loadMatch(matchId);
						if (matchBean != null) {
							final long matchRatio = getMatchRatio();
							TaskExecutor.EXECUTOR_SERVICE.execute(new Runnable() {
								@Override
								public void run() {
									JjsUtil.doMatch(players, matchRatio, matchBean);
								}
							});
						} else {
							LogUtil.msgLog.info("current match is null: matchId={}", matchId);
						}
					}
				} finally {
					try {
						if (isCompetitionRoom()) {
							try {
								if (getCompetitionRoom() != null) {
									getCompetitionRoom().setCurrentState(CompetitionRoom.STATE_NORMAL_OVER);
								}
								CompetitionDao.getInstance().updateCompetitionRoomCurrentState(competitionRoomId, CompetitionRoom.STATE_NORMAL_OVER);
								CompetitionDao.getInstance().deleteCompetitionRoomUserByRoomId(competitionRoomId);
								CompetitionDao.getInstance().deleteCompetitionRoomByKeyId(competitionRoomId);
							} catch (Exception e) {
								LogUtil.errorLog.error("competition|clearingData|Exception:" + e.getMessage(), e);
							}
						}

						//解散比赛场
						CompetitionUtil.dissTable(this, Arrays.asList(winner), getCompetitionRoom(), getCompetitionRoomUserMap());
					} catch (Exception e) {
						LogUtil.msgLog.error("competition|dissTable|Exception|{}", isCompetitionRoom(), e);
					}
				}


				return ret;

			}
		}

	}

	public long getMatchId() {
		return matchId;
	}

	public long getMatchRatio() {
		return matchRatio;
	}

	/**
	 * 更新比赛场中玩家的分数信息
	 *
	 * @param userScores
	 */
	public void changeMatchData(Map<Long, Integer> userScores) {
		if (isMatchRoom()) {
			MatchBean matchBean = JjsUtil.loadMatch(matchId);
			if (matchBean != null) {
				synchronized (matchBean) {
					int currentNo = JjsUtil.loadMatchCurrentGameNo(matchBean);
					for (Map.Entry<Long, Integer> kv : userScores.entrySet()) {
						int temp = (int) (kv.getValue().intValue() * matchRatio);
						int score = matchBean.addUserMsg(kv.getKey().longValue(), currentNo, temp, -2, true);

						HashMap<String, Object> map0 = new HashMap<>();
						map0.put("currentScore", score);
						MatchDao.getInstance().updateMatchUser(matchBean.getKeyId(), kv.getKey().longValue(), map0);
					}
					matchBean.sort();

					//推送排名
					JjsUtil.sendRank(matchBean);
				}

				for (Map.Entry<Long, Player> kv : getPlayerMap().entrySet()) {
					kv.getValue().getMyExtend().getUserTaskInfo().alterDailyMatchGameNum();
				}
			}
		}
	}

	public boolean isGroupRoomReturnConsume() {
		return playBureau <= 1 && playedBureau == 0;
	}

	public boolean isAAConsume0() {
		return payType == 1;
	}

	/**
	 * 一局结束
	 */
	public abstract void calcOver();

	public void calcOver0() {
		synchronized (this) {
			try {
				for (Player player : getPlayerMap().values()) {
					String cur = player.getMyExtend().getPlayerStateMap().get("cur");
					if (cur != null && cur.endsWith("_0")) {
						String str = cur.substring(0, cur.length() - 2);
						int temp = Integer.parseInt(str);

						cur = temp + "_1";
						player.getMyExtend().getPlayerStateMap().put("cur", cur);

						if (consumeCards() && isAAConsume0()) {
							int needCards = PayConfigUtil.get(playType, totalBureau, getMaxPlayerCount(), 0);

							if (needCards <= 0) {
								continue;
							}

							if (temp > (getTotalBureau() / 2)) {
								needCards = needCards / 2;
							}

							player.changeCards(0, -needCards, true, playType, getCardSourceType(payType));
							player.saveBaseInfo();
						}
					}
					String gameTime = TimeUtil.formatDayTime2(TimeUtil.now());
					if (!isGoldRoom()) {
						// 非金币场房间局数统计
						player.changeTotalBureau();
					}

//                    if(ActivityConfig.isActivityOpen(ActivityConfig.activity_game_bureau_static,playType)) {
//                        UserGameBureauDao.getInstance().saveUserGameBureau(new UserGameBureau(player.getUserId(), player.getName(), playType, gameTime, (int) player.getPayBindId()));
//                    }

					RedBagManager.getInstance().updateTodayRedBagGameNum(player.getUserId());
				}

			} catch (Exception e) {
				LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
			}

		}
	}

	/**
	 * 小局数据统计
	 */
	public void calcDataStatistics1() {

	}

	/**
	 * 大局数据统计
	 */
	public void calcDataStatistics2() {
		//俱乐部房间 单大局大赢家、单大局大负豪、总小局数、单大局赢最多、单大局输最多 数据统计
		if (isGroupRoom()) {
			String groupId = loadGroupId();
			int maxPoint = 0;
			int minPoint = 0;
			Long dataDate = Long.valueOf(new SimpleDateFormat("yyyyMMdd").format(new Date()));
			//Long dataDate, String dataCode, String userId, String gameType, String dataType, int dataValue
			for (Player player : getPlayerMap().values()) {
				//总小局数
				DataStatistics dataStatistics1 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "xjsCount", playedBureau);
				DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics1, 3);
				//总大局数
				DataStatistics dataStatistics5 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "djsCount", 1);
				DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics5, 3);
				//总积分
				DataStatistics dataStatistics6 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "zjfCount", player.loadScore());
				DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics6, 3);

				if (player.loadScore() > 0) {
					if (player.loadScore() > maxPoint) {
						maxPoint = player.loadScore();
					}
					//单大局赢最多
					DataStatistics dataStatistics2 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "winMaxScore", player.loadScore());
					DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics2, 4);
				} else if (player.loadScore() < 0) {
					if (player.loadScore() < minPoint) {
						minPoint = player.loadScore();
					}
					//单大局输最多
					DataStatistics dataStatistics3 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "loseMaxScore", player.loadScore());
					DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics3, 5);
				}
			}

			for (Player player : getPlayerMap().values()) {
				if (maxPoint > 0 && maxPoint == player.loadScore()) {
					//单大局大赢家
					DataStatistics dataStatistics4 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "dyjCount", 1);
					DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics4, 1);
				} else if (minPoint < 0 && minPoint == player.loadScore()) {
					//单大局大负豪
					DataStatistics dataStatistics5 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "dfhCount", 1);
					DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics5, 2);
				}
			}

			calcDataStatistics3(groupId);
		}
	}

	/**
	 * 自定义的数据统计
	 *
	 * @see #calcDataStatistics2()
	 */
	public void calcDataStatistics3(String groupId) {
		//俱乐部活动总大局数
		if (ActivityConfig.isActivityOpen(ActivityConfig.activity_group_conquest) && loadGroupRoomPay() > 0 && isCommonOver()) {
			try {
				DataStatistics dataStatistics7 = new DataStatistics(Long.valueOf(ActivityConfig.activity_group_conquest), "group" + groupId, groupId, String.valueOf(playType), "jlbCount", 1);
				DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics7, 3);
			} catch (Exception e) {
				LogUtil.e("calcDataStatistics7 err-->groupId:" + groupId + ",tableId:" + getId(), e);
			}
		}

		if (ActivityConfig.isActivityOpen(ActivityConfig.activity_group_megabucks) && loadGroupRoomPay() > 0) {
			// 百万大奖活动
			try {
				DataStatistics dataStatistics8 = new DataStatistics(Long.valueOf(ActivityConfig.activity_group_megabucks), "group" + groupId, groupId, String.valueOf(playType), "jlbCount", 1);
				DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics8, 3);
			} catch (Exception e) {
				LogUtil.e("calcDataStatistics8 err-->groupId:" + groupId + ",tableId:" + getId(), e);
			}
		}
		//俱乐部总大局数统计
		try {
			Long dataDate = Long.valueOf(new SimpleDateFormat("yyyyMMdd").format(new Date()));
			DataStatistics jlbDjs = new DataStatistics(dataDate, "group" + groupId, groupId, "1", "jlbDjs", 1);
			DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(jlbDjs, 3);
		} catch (Exception e) {
			LogUtil.e("calcDataStatistics7 err-->groupId:" + groupId + ",tableId:" + getId(), e);
		}


		//俱乐部总消耗统计
		try {
			LogUtil.i("calcDataStatistics decDiamond -->groupId:" + groupId + ",tableId:" + getId()+ " "+loadPayConfig());
			Long dataDate = Long.valueOf(new SimpleDateFormat("yyyyMMdd").format(new Date()));
			DataStatistics decDiamond = new DataStatistics(dataDate, "group" + groupId, groupId, "1", "decDiamond", loadPayConfig());
			DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(decDiamond, 3);
		} catch (Exception e) {
			LogUtil.e("calcDataStatistics decDiamond err-->groupId:" + groupId + ",tableId:" + getId(), e);
		}
	}

	/**
	 * 是否开一局桌子
	 */
	public boolean isKaiYiJu() {
		return SharedConstants.isAssisOpen() && !StringUtil.isBlank(getAssisCreateNo());
	}

	public void calcOver1() {
		if(playBureau == totalBureau && totalBureau == 1){
			consume();
		}

		calcOver0();
		if (GameServerConfig.checkDataStatistics()) {
			try {
				calcDataStatistics1();
			} catch (Exception e) {
				LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
			}
		}
		try {
			GroupTable groupTable = null;
			if (NumberUtils.isDigits(getServerKey())) {
				groupTable = GroupDao.getInstance().loadGroupTableByKeyId(getServerKey());
			}

			for (Player player : getPlayerMap().values()) {
				if (groupTable != null) {
					HashMap<String, Object> map = new HashMap<>();
					map.put("groupId", groupTable.getGroupId().toString());
					map.put("userId", String.valueOf(player.getUserId()));
					map.put("count1", 1);
					GroupDao.getInstance().updateGroupUser(map);
				}
			}
		} catch (Exception e) {
			LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
		}

		//比赛场更新榜单,大结算不重复更新榜单
		if(isCompetitionRoom() && playedBureau < totalBureau){
			try {
				CompetitionUtil.pushOnlyRefreshRankMsg(new ArrayList<>(getSeatMap().values()), getCompetitionRoomUserMap(), getCompetitionRoom());
			}catch (Exception e) {
			}
		}

		// 幸运红包
		calcActivity(ActivityConstant.activity_luck_hb, isTiqianDiss() ? 1 : 0);
		// calcActivity(ActivityConstant.activity_xn_hb);
	}

	public UserStatistics loadRobotUserStatistics(Player player) {
		return null;
	}

	public UserStatistics loadPlayerUserStatistics(Player player) {
		if (playedBureau > 0) {
			return new UserStatistics(String.valueOf(player.getUserId()), player.loadScore(), isMatchRoom() ? "match" : (isCompetitionRoom()) ? "competition" : (isGoldRoom() ? "gold" : (isGroupRoom() ? "group" : "common")), playType, playedBureau);
		} else {
			return null;
		}
	}

	/**
	 * 是否允许机器人加入
	 *
	 * @return
	 */
	public boolean allowRobotJoin() {
		return false;
	}

	/**
	 * 大结算
	 */
	public void calcOver2() {
		for (Player player : getPlayerMap().values()) {
			if(player.isRobot()) continue;

			if (masterId == player.getUserId() && playBureau == totalBureau) {
				player.endCreateBigResultBureau(playBureau);
			}
			LuckyRedbagCommand.setInnings(player);
			NewPlayerGiftActivityCmd.updateMatchNum(player);
		}

		// 新年红包
		// calcActivity(ActivityConstant.activity_xn_hb);
	}

    /**
     * 结算亲友圈信用房
     * 结算亲友圈金币房
     */
    public void calcOver3() {
        calcCreditNew();
        calcGroupTableGoldRoomOver();
    }

    // 随机红包
    public static List<Double> getHongBaoList(double total, int count) {
        double sheng = total;
        int cnt = count - 1;

        double min = 0.1;
        double max = sheng - min * cnt;
        List<Double> list = new ArrayList<>();
        double money = 0;
        DecimalFormat df = new DecimalFormat("#.##");
        String moneyStr = "";
        for (int i = 0; i < count; i++) {
            if (0 == cnt) {
                money = sheng;
            } else {
                money = MathUtil.random(min, max);
                cnt--;
            }
            moneyStr = df.format(money);
            money = Double.parseDouble(moneyStr);
            sheng = sheng - money;
            max = sheng - min * cnt;
            list.add(money);
        }

        return list;
    }

    /**
     * 新年活动
     */
    public void calcActivity(int activityType, Object... para) {
        if (ActivityConstant.activity_xn_hb == activityType) {// 新年红包
            ActivityBean xnAcitvity = StaticDataManager.getActivityBean(ActivityConstant.activity_xn_hb);
            if (xnAcitvity == null) {
//                LogUtil.d_msg("activity is null-->" + activityType);
                return;
            }

            float xnHbMoney = xnAcitvity.shakeXnHbMoney();
            if (xnHbMoney <= 0) {
                return;
            }

            // double xnHbMoney = 0.5;
            // 总红包金额=平均金额*牌桌人数*（0.5+本局消耗房卡数量*0.5）
            // 红包金额最小值0.1 最大值为总红包金额-0.1*（总人数-1）

            int needCards = PayConfigUtil.get(playType, totalBureau, getMaxPlayerCount(), 0);

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("tableId", id);
            paramMap.put("hbType", 1);
            paramMap.put("createTime", new Date());

            int playerCount = getPlayerCount();
            double totalMoney = xnHbMoney * playerCount * (0.5f + needCards * 0.5f);
            List<Double> hbList = getHongBaoList(totalMoney, playerCount);
            double money = 0;
            List<Map<String, Object>> paramMaps = new ArrayList<>();

            int index = 0;
            for (Player player : getPlayerMap().values()) {
                money = hbList.get(index++);
                paramMap.put("userId", player.getUserId());
                paramMap.put("userName", player.getName());
                paramMap.put("money", money);
                ActivityDao.getInstance().addHbFafangRecord(paramMap);
                ActivityDao.getInstance().insertUserTotalMoney(player, money);
                paramMaps.add(new HashMap<>(paramMap));
            }

            for (Player player : getPlayerMap().values()) {
                player.writeComMessage(WebSocketMsgType.res_com_code_hb, activityType, (int) para[0], JacksonUtil.writeValueAsString(paramMaps));
            }

        } else if (ActivityConstant.activity_luck_hb == activityType) {// 幸运红包
            ActivityBean luckAcitvity = StaticDataManager.getActivityBean(ActivityConstant.activity_luck_hb);
            if (luckAcitvity == null) {
//                LogUtil.d_msg("activity is null-->" + activityType);
                return;
            }

            int money = 0;
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("tableId", 0);
            paramMap.put("hbType", 2);
            paramMap.put("createTime", new Date());
            List<Map<String, Object>> paramMaps = null;

            for (Player player : getPlayerMap().values()) {
                // 统计小局数
                player.getMyActivity().changeTimeFlag(activityType, luckAcitvity.getStartTime(), 1);
                int curPlayCount = player.getMyActivity().getTimeFlag(activityType, luckAcitvity.getStartTime());
                money = luckAcitvity.drawLuckMoney(curPlayCount);
                if (money <= 0) {
                    continue;
                }
                // 触发后，当前局数清为0
                player.getMyActivity().clearTimeFlag(activityType, luckAcitvity.getStartTime());

                paramMap.put("userId", player.getUserId());
                paramMap.put("userName", player.getName());
                paramMap.put("money", money);
                paramMaps = new ArrayList<>();
                paramMaps.add(paramMap);
                player.writeComMessage(WebSocketMsgType.res_com_code_hb, activityType, isTiqianDiss() ? 1 : 0, JacksonUtil.writeValueAsString(paramMaps));
                ActivityDao.getInstance().addHbFafangRecord(paramMap);
                ActivityDao.getInstance().insertUserTotalMoney(player, money);
                String content = "恭喜" + player.getName() + "获得" + money + "元幸运红包，在1月27日至2月2日19：00~23:00，参与牌局，都有概率获得幸运红包哦！";
                int round = 1;
                MarqueeManager.getInstance().sendMarquee(content, round);
                // writeMessage("发送:" + content + " 轮数:" + round);
                LogUtil.monitor_i("幸运红包:userId" + player.getUserId() + ",获得红包:" + money + ", 牌桌号:" + this.id);
            }

        } else if (ActivityConstant.activity_fudai == activityType) {// 福袋
            ActivityBean fudaiAcitvity = StaticDataManager.getActivityBean(ActivityConstant.activity_fudai);
            if (fudaiAcitvity == null) {
//                LogUtil.d_msg("activity is null-->" + activityType);
                return;
            }

            ActivityDao.getInstance().updateFudai((Player) para[0], (int) para[1]);
        }

    }

    /**
     * 牌局返利活动统计
     */
    private void recordUserGameRebate() {
        GameReBate gameRebateBean = StaticDataManager.getGameRebate(playType);
        if (gameRebateBean != null) {// 暂时没做配置表
            int baseBureau = gameRebateBean.getBaseBureau();// 牌局返利基础奖励局数
            Date openServerDate = new Date(TimeUtil.parseTimeInMillis(gameRebateBean.getOpenServerDate()));
            Date endDate = DateUtils.addDays(openServerDate, gameRebateBean.getRebateRangeTime());
            Date curDate = new Date();
            if (playBureau >= baseBureau && curDate.before(endDate)) {
                int rewardBureau = (int) (playBureau / baseBureau);// 获得的基础点数
                for (Player player : getPlayerMap().values()) {
                    UserGameRebateDao.getInstance().saveUserGameRebate(new UserGameRebate(player.getUserId(), player.getName(), this.playType, rewardBureau, new Date(), (int) player.getPayBindId()));
                }
            }
        }
        if (ActivityConfig.isActivityOpen(ActivityConfig.activity_new_payBind_bureau_static, playType)) {
            int baseBureau = 4;
            if (playBureau >= baseBureau) {
                int rewardBureau = (int) (playBureau / baseBureau);// 获得的基础局数
                String gameTime = TimeUtil.formatDayTime2(TimeUtil.now());
                ActivityConfigInfo config = ActivityConfig.getActivityConfigInfo(ActivityConfig.activity_new_payBind_bureau_static);
                for (Player player : getPlayerMap().values()) {
                    if (player.getPayBindId() > 0 && player.getPayBindTime().after(config.getStartTime()) && player.getPayBindTime().before(config.getEndTime())) {// 玩家已绑码 并且是在活动期间绑码
                        UserBindGameBureau record = new UserBindGameBureau(player.getUserId(), player.getName(), this.playType, gameTime, rewardBureau, (int) player.getPayBindId());
                        UserBindGameBureauDao.getInstance().saveUserBindGameBureau(record);
                    }
                }
            }
        }
    }

    // 是否AA制消耗
    public boolean isAAConsume() {
        return isAAConsume;
    }

    public boolean setAAConsume(boolean isAA) {
        return isAAConsume = isAA;
    }

    /**
     * 获取支付配置
     *
     * @return
     */
    public int loadPayConfig(int payType) {
        int serverPayType = -1;
        switch (payType) {
            case PayConfigUtil.PayType_Client_AA:
                serverPayType = PayConfigUtil.PayType_Server_AA;
                break;
            case PayConfigUtil.PayType_Client_TableMaster:
                serverPayType = PayConfigUtil.PayType_Server_TableMaster;
                break;
            case PayConfigUtil.PayType_Client_GroupMaster:
                serverPayType = PayConfigUtil.PayType_Server_GroupMaster;
                break;
            case PayConfigUtil.PayType_Client_AA_Gold:
                serverPayType = PayConfigUtil.PayType_Server_AA_Gold;
                break;
        }
        if (serverPayType == -1) {
            return -1;
        }
        return PayConfigUtil.get(playType, totalBureau, getMaxPlayerCount(), serverPayType);
    }

    /**
     * 获取支付配置
     *
     * @return
     */
    public int loadPayConfig() {
        return loadPayConfig(payType);
    }

    public void initNext() {
        initNext(playBureau >= totalBureau);
    }

    public void initNext(boolean over) {
        boolean requriedCard = false;
        if (isFirstBureauOverConsume()) {
            requriedCard = consume();
        }

        // 如果为第一局，则添加table_playlog

        if (over) {
            return;
        }
        for (Player player : getSeatMap().values()) {
            player.initNext();
            player.clearTingMsg();
            player.setGoldResult(0);
            player.resetRobotActionCounter();
        }

//        setCreateTime(TimeUtil.now());
        clearPlayLog();
        changePlayBureau(1);
        setDisCardRound(0);
        setDisCardSeat(0);
        setNowDisCardSeat(0);
		zpMap.clear();

        changeTableState(table_state.ready);

        if (isGroupRoom()) {
            try {
                String[] msgs = serverKey.split("_");
                if (msgs.length >= 2) {
                    if (groupTable != null) {
                        groupTable.setPlayedBureau(getPlayedBureau());
                    }

                    if (Redis.isConnected()) {
                        String str = RedisUtil.hget(GroupRoomUtil.loadGroupTableKey(loadGroupId(), msgs.length >= 3 ? Integer.parseInt(msgs[2]) : 0), msgs[1]);
                        if (StringUtils.isNotBlank(str)) {
                            JSONObject jsonObject = JSONObject.parseObject(str);
                            jsonObject.put("playedBureau", getPlayedBureau());
                            RedisUtil.hset(GroupRoomUtil.loadGroupTableKey(loadGroupId(), msgs.length >= 3 ? Integer.parseInt(msgs[2]) : 0), msgs[1], jsonObject.toString());
                        }
                    }
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("keyId", msgs[1]);
                    map.put("playedBureau", getPlayedBureau());
                    GroupDao.getInstance().updateGroupTableByKeyId(map);
                }
            } catch (Exception e) {
                LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
            }
        }
        setLastStartNextUser(0);
        autoPlayDiss = false;
        initNext1();
        initNext0(requriedCard);
        this.dyjCount = 0;
        changePlayers();
    }

    /**
     * 俱乐部已打完局数入库（小甘）
     */
    public void grouplogBureauStorage() {
        if (isGroupRoom()) {
            try {
                String[] msgs = serverKey.split("_");
                if (msgs.length >= 2) {
                    if (groupTable != null) {
                        groupTable.setPlayedBureau(getPlayedBureau());
                    }

                    if (Redis.isConnected()) {
                        String str = RedisUtil.hget(GroupRoomUtil.loadGroupTableKey(loadGroupId(), msgs.length >= 3 ? Integer.parseInt(msgs[2]) : 0), msgs[1]);
                        if (StringUtils.isNotBlank(str)) {
                            JSONObject jsonObject = JSONObject.parseObject(str);
                            jsonObject.put("playedBureau", getPlayedBureau());
                            RedisUtil.hset(GroupRoomUtil.loadGroupTableKey(loadGroupId(), msgs.length >= 3 ? Integer.parseInt(msgs[2]) : 0), msgs[1], jsonObject.toString());
                        }
                    }
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("keyId", msgs[1]);
                    map.put("playedBureau", getPlayedBureau());
                    GroupDao.getInstance().updateGroupTableByKeyId(map);
                }
            } catch (Exception e) {
                LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
            }
        }
    }

    public boolean isFirstBureauOverConsume() {
        return true;
    }


    public final int loadGroupRoomPay() {
        if (isGroupRoom()) {
            try {
                String[] msgs = serverKey.split("_");
                if (msgs.length >= 2) {
                    if (groupTable == null || groupTable.getKeyId() == null || !groupTable.getKeyId().toString().equals(msgs[1])) {
                        groupTable = GroupDao.getInstance().loadGroupTableByKeyId(msgs[1]);
                    }

                    String[] tempMsgs = new JsonWrapper(groupTable.getTableMsg()).getString("strs").split(";")[0].split("_");
//                    String payType = tempMsgs[0];

                    if (tempMsgs.length >= 4) {
//                        if (!"1".equals(payType)){
                        return Integer.parseInt(tempMsgs[3]);
//                        }else{
//                            //老数据AA扣钻数据错误,兼容处理
//                            String date = ResourcesConfigsUtil.loadServerPropertyValue("group_room_aa_date");
//                            if (StringUtils.isNotBlank(date)&&groupTable.getCreatedTime()!=null&&groupTable.getCreatedTime().after(com.sy.general.GeneralHelper.str2Date(date,"yyyy-MM-dd HH:mm:ss"))){
//                                return Integer.parseInt(tempMsgs[3]);
//                            }
//                        }
                    }
                }
            } catch (Exception e) {
                LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
            }
        }
        return -1;
    }

    protected boolean consume() {
        boolean requriedCard = false;

        // 如果是第一句扣掉房卡
        if (playBureau == 1 && consumeCards() && !isGoldRoom() && !isCompetitionRoom()) {
            if(payType == PayConfigUtil.PayType_Client_AA
                || payType == PayConfigUtil.PayType_Client_TableMaster
                || payType == PayConfigUtil.PayType_Client_GroupMaster
            ) {
                int needCards = 0;
                if(isGroupRoom()){
					needCards = loadGroupRoomPay();
				}else{
					needCards = loadPayConfig();
				}
                if (needCards > 0) {
                    CardSourceType sourceType = getCardSourceType(payType);
                    if (isAAConsume() || isAAConsume0()) {
                        if (needCards < 0) {
                            needCards = loadPayConfig();
                        }
                        if (needCards <= 0) {
                            return requriedCard;
                        }

                        for (Player player : getPlayerMap().values()) {
                            if (!GameConfigUtil.freeGame(playType, player.getUserId())) {
                                // 如果是AA开房每人扣一张
                                if (PayConfigUtil.loadPayResourceType(playType) == UserResourceType.TILI) {
                                    player.changeTili(-needCards, true);
                                } else {
                                    player.changeCards(0, -needCards, true, playType, sourceType);
                                    player.saveBaseInfo();
                                    calcActivity(ActivityConstant.activity_fudai, player, needCards);
                                }

                                requriedCard = true;
                            }
                        }
                    } else {
                        if (NumberUtils.isDigits(getServerKey()) || isGroupRoom()) {
                            LogUtil.msgLog.info("group master pay:group table keyId=" + serverKey + ",tableId=" + getId());
                            changeConsume(needCards);
                        } else {
                            if (needCards < 0) {
                                needCards = loadPayConfig();
                            }
                            if (needCards <= 0) {
                                return requriedCard;
                            }
                            Player player = getPlayerMap().get(masterId);
                            if (player != null && !GameConfigUtil.freeGame(playType, player.getUserId())) {
                                if (PayConfigUtil.loadPayResourceType(playType) == UserResourceType.TILI) {
                                    player.changeTili(-needCards, true);
                                } else {
                                    player.changeCards(0, -needCards, true, playType, sourceType);
                                    player.saveBaseInfo();
                                    calcActivity(ActivityConstant.activity_fudai, player, needCards);
                                }
                            } else {
                                if (player == null) {
                                    RegInfo user = UserDao.getInstance().selectUserByUserId(masterId);
                                    if (user != null && !GameConfigUtil.freeGame(playType, user.getUserId())) {
                                        try {
                                            player = ObjectUtil.newInstance(getPlayerClass());
                                            player.loadFromDB(user);
                                            if (PayConfigUtil.loadPayResourceType(playType) == UserResourceType.TILI) {
                                                player.changeTili(-needCards, true);
                                            } else {
                                                player.changeCards(0, -needCards, true, playType, sourceType);
                                                player.saveBaseInfo();
                                                calcActivity(ActivityConstant.activity_fudai, player, needCards);
                                            }
                                        } catch (Exception e) {
                                            LogUtil.errorLog.error("consume err-->Exception:" + e.getMessage(), e);
                                        }
                                    } else {
                                        LogUtil.e("consume err-->tableId:" + id + ",masterId:" + masterId);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else if (playBureau == 1 && (NumberUtils.isDigits(getServerKey()) || isGroupRoom() || isDaikaiTable())) {
            int needCards = loadGroupRoomPay();
            if (needCards == 0) {
            } else {
                changeConsume(needCards);
            }
        }
        return requriedCard;
    }

    protected void changeConsume() {
        if (SharedConstants.consumecards && playBureau == 1) {
            changeConsume(loadPayConfig());
        }
    }

    public CardSourceType getCardSourceType(int payType) {
        if (isDaikaiTable()) {
            if (payType == 1) {
                return CardSourceType.daikaiTable_AA;
            } else if (payType == 2) {
                return CardSourceType.daikaiTable_FZ;
            }
        } else if (isGroupRoom()) {
            if (payType == 1) {
                return CardSourceType.groupTable_AA;
            } else if (payType == 2) {
                return CardSourceType.groupTable_FZ;
            } else if (payType == 3) {
                return CardSourceType.groupTable_QZ;
            }
        } else {
            if (payType == 1) {
                return CardSourceType.commonTable_AA;
            } else if (payType == 2) {
                return CardSourceType.commonTable_FZ;
            }
        }
        return CardSourceType.unknown;
    }

    /**
     * 军团长支付，房主代开需要第一局打完后统计消耗
     */
    protected void changeConsume(int needCards) {
        if (SharedConstants.consumecards && playBureau == 1) {
            if (needCards < 0) {
                needCards = loadPayConfig();
            }

            if (needCards <= 0) {
                return;
            }
            Player creator = PlayerManager.getInstance().getPlayer(creatorId);
            boolean count = false;
            if (creator == null) {
                RegInfo user = UserDao.getInstance().selectUserByUserId(creatorId);
                if (user != null) {
                    try {
                        creator = ObjectUtil.newInstance(getPlayerClass());
                        creator.loadFromDB(user);
                        count = true;
                    } catch (Exception e) {
                        LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
                    }
                } else {
                    LogUtil.e("changeConsume err-->tableId:" + id + ",creatorId:" + creatorId);
                }
            } else {
                count = true;
            }
            if (count) {
                PlayerManager.getInstance().changeConsume(creator, -needCards, 0, playType);
                creator.changeUsedCards(-needCards);
            }
        }
    }

    public void initNext0(boolean requriedCard) {
    }

    public int calcPlayerCount(int playerCount) {
        return playerCount > 0 ? playerCount : 6;
    }

    protected String buildPlayersInfo() {
        StringBuilder sb = new StringBuilder();
        for (Player player : getPlayerMap().values()) {
            sb.append(player.toInfoStr()).append(player.toXipaiDatatoInfoStr()).append(";");
        }
        // playerInfos = sb.toString();
        return sb.toString();
    }

    public void setLastActionTime(long lastActionTime) {
        this.lastActionTime = lastActionTime;
        dbParamMap.put("lastActionTime", lastActionTime);
    }

    public void changePlayers() {
        dbParamMap.put("players", JSON_TAG);
    }

    public void changeCards(int seat) {
        dbParamMap.put("outPai" + seat, JSON_TAG);
        dbParamMap.put("handPai" + seat, JSON_TAG);
    }

    public void changeExtend() {
        dbParamMap.put("extend", JSON_TAG);
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
        dbParamMap.put("createTime", createTime);
    }

    public int getTotalBureau() {
        return totalBureau;
    }

    public void setTotalBureau(int totalBureau) {
        this.totalBureau = totalBureau;
        dbParamMap.put("totalBureau", totalBureau);
    }

    public int getPlayBureau() {
        return playBureau;
    }

    public void changePlayBureau(int playBureau) {
        this.playBureau += playBureau;
        dbParamMap.put("playBureau", this.playBureau);
    }

    public void changeFinishBureau(int finishBureau) {
        this.finishBureau += finishBureau;
        dbParamMap.put("finishBureau", this.finishBureau);
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
        dbParamMap.put("roomId", roomId);
    }

    public long getMasterId() {
        return masterId;
    }

    public void setMasterId(long masterId) {
        this.masterId = masterId;
        dbParamMap.put("masterId", masterId);
    }

    /**
     * 每小局结算之后调用
     */
    protected void calcAfter() {
        this.playedBureau = playBureau;
        transferTempCredit();
        changeFinishBureau(1);
        changeExtend();
        if (getSeatMap() != null) {
            StringBuilder sb = new StringBuilder("BaseTable|calcAfter");
            sb.append("|").append(getId());
            sb.append("|").append(getPlayBureau());
            for (Player player : getSeatMap().values()) {
                if (player == null) {
                    continue;
                }
                sb.append("[").append(player.getUserId());
                sb.append(",").append(player.loadScore());
                sb.append(",").append(player.getTotalPoint());
                sb.append("]").append(player.getWinLoseCredit());
                sb.append("]").append(player.getWinGold());
            }
            LogUtil.msgLog.info(sb.toString());
        }
    }

    public void transferTempCredit() {
        for (Player player : getAllPlayerMap().values()) {
            player.settleTempCredit();
        }
    }

    /**
     * 房间内所有玩家 包括观战者
     * @return
     */
    public Map<Long, Player> getAllPlayerMap() {
        Map<Long, Player> result = new HashMap<>();
        result.putAll(getPlayerMap());
        result.putAll(getRoomPlayerMap());
        return result;
    }


    /**
     * 已经加入房间的不能再次加入
     * <br/>房间已满不能再次加入
     *
     * @param player
     * @return
     */
    public final boolean isCanJoin(Player player) {
        return isCanJoin(player, true);
    }

    public final boolean isCanJoin(Player player, boolean syn) {
        if (syn) {
            synchronized (this) {
                if (player.isRobot()) {
                    return true;
                }
                if (getPlayerMap().containsKey(player.getUserId())) {
                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_4));
                    return false;
                }

                if (getPlayerCount() >= getMaxPlayerCount()) {
                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_5));
                    return false;
                }

                if(isGoldRoom() || isSoloRoom() || isCompetitionRoom()){
                    return true;
                }

                if (getAllowGroupMember() > 0 && (player.getGroupUser() == null || player.getGroupUser().getGroupId().intValue() != getAllowGroupMember())) {
                    if (isGroupRoom()) {
                        String groupId = loadGroupId();
                        GroupUser groupUser = GroupDao.getInstance().loadGroupUser(player.getUserId(), groupId);
                        if (groupUser != null && groupUser.getGroupId().intValue() == allowGroupMember) {
                            player.setGroupUser(groupUser);
                        } else {
                            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_53));
                            return false;
                        }
                    } else {
                        player.loadGroupUser(String.valueOf(getAllowGroupMember()));
                        if ((player.getGroupUser() == null || player.getGroupUser().getGroupId().intValue() != getAllowGroupMember())) {
                            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_53));
                            return false;
                        }
                    }
                }

                if (SharedConstants.isRestrictOpen() && isKaiYiJu()) {
                    if (!AssisServlet.chatRoomUserCheck(this, player)) {
                        player.writeErrMsg(LangHelp.getMsg(LangMsg.code_54));
                        return false;
                    }
                }

                if (isGroupRoom()) {
                    String groupId = loadGroupId();
                    GroupInfo group = null;
                    try {
                        // 进房间金币限制
                        group = GroupDao.getInstance().loadGroupInfo(groupId);
                        if(group == null){
                            player.writeErrMsg(LangMsg.code_262,groupId);
                            return false;
                        }
                    } catch (Exception e) {
                        LogUtil.errorLog.error("Exception:",e);
                    }
                    //信用房：未达到最低信用分时不可以进入房间
                    if (creditMode == 1) {
                        GroupUser gu = GroupDao.getInstance().loadGroupUserForceMaster(player.getUserId(),groupId);
                        if (gu == null || gu.getCredit() < creditJoinLimit) {
                            player.writeErrMsg(LangMsg.code_64, MathUtil.formatCredit(creditJoinLimit));
                            return false;
                        }
                        if(gu != null){
                            player.setGroupUser(gu);
                        }
                        if(gu.getCreditLock() == 1){
                            // 20191015不限制
//                            player.writeErrMsg(LangMsg.code_71, MathUtil.formatCredit(creditJoinLimit), MathUtil.formatCredit(gu.getCredit()));
//                            return false;
                        }
                        // 进房间金币限制
                        if(group != null && group.getSwitchCoin() == 1){
                            if(player.loadAllCoin() <= creditJoinLimit * group.getCreditRate()){
                                player.writeErrMsg(LangMsg.code_72, creditJoinLimit * group.getCreditRate(), player.getCoin());
                                return false;
                            }
                        }
                        if(!checkGroupWarn(player,groupId)){
                        	return false;
						}
                    }

                    if(gtgMode == 1){
                        if(player.loadAllGolds() < gtgJoinLimit){
                            player.writeErrMsg(LangMsg.code_256);
                            return false;
                        }
                    }

                    if(!checkIpOrGps(player)){
                        return false;
                    }

                    if(!checkGroupUserReject(player)){
                        return false;
                    }

                    if(TableManager.isStopCreateGroupRoom(group)){
                        player.writeErrMsg(LangHelp.getMsg(LangMsg.code_67));
                        return false;
                    }

                    if (GameUtil.isPlayWzq(playType) && !isCanJoinFOrWzQ(player)) {
                        player.writeErrMsg(LangMsg.code_263);
                        return false;
                    }

                    //////   验证是否开房间免费   ////////////////
                    //////   这一步必须放在最后   ////////////////
                    if (GameConfigUtil.freeGameOfGroup(playType, groupId)) {
                        return true;
                    }
                }

                return isCanJoin0(player);
            }
        } else {
            if (player.isRobot()) {
                return true;
            }
            if (getPlayerMap().containsKey(player.getUserId())) {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_4));
                return false;

            }
            if (getPlayerCount() >= getMaxPlayerCount()) {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_5));
                return false;
            }

            if(isGoldRoom() || isSoloRoom()){
                return true;
            }

            if (getAllowGroupMember() > 0 && (player.getGroupUser() == null || player.getGroupUser().getGroupId().intValue() != getAllowGroupMember())) {
                if (isGroupRoom()) {
                    String groupId = loadGroupId();
                    GroupUser groupUser = GroupDao.getInstance().loadGroupUser(player.getUserId(), groupId);
                    if (groupUser != null && groupUser.getGroupId().intValue() == allowGroupMember) {
                        player.setGroupUser(groupUser);
                    } else {
                        player.writeErrMsg(LangHelp.getMsg(LangMsg.code_53));
                        return false;
                    }
                } else {
                    player.loadGroupUser(String.valueOf(getAllowGroupMember()));
                    if ((player.getGroupUser() == null || player.getGroupUser().getGroupId().intValue() != getAllowGroupMember())) {
                        player.writeErrMsg(LangHelp.getMsg(LangMsg.code_53));
                        return false;
                    }
                }
            }

            if (SharedConstants.isRestrictOpen() && isKaiYiJu()) {
                if (!AssisServlet.chatRoomUserCheck(this, player)) {
                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_54));
                    return false;
                }
            }

            if (isGroupRoom()) {
                String groupId = loadGroupId();
                GroupInfo group = null;
                try {
                    // 进房间金币限制
                    group = GroupDao.getInstance().loadGroupInfo(groupId);
                    if(group == null){
                        player.writeErrMsg(LangMsg.code_262,groupId);
                        return false;
                    }
                } catch (Exception e) {
                    LogUtil.errorLog.error("Exception:",e);
                }
                //信用房：未达到最低信用分时不可以进入房间
                if (creditMode == 1) {
                    GroupUser gu = GroupDao.getInstance().loadGroupUserForceMaster(player.getUserId(),groupId);
                    if (gu == null || gu.getCredit() < creditJoinLimit) {
                        player.writeErrMsg(LangMsg.code_64, MathUtil.formatCredit(creditJoinLimit));
                        return false;
                    }
                    if(gu != null){
                        player.setGroupUser(gu);
                    }
                    if(gu.getCreditLock() == 1){
                        // 20191015不限制
//                        player.writeErrMsg(LangMsg.code_71, MathUtil.formatCredit(creditJoinLimit), MathUtil.formatCredit(gu.getCredit()));
//                        return false;
                    }
                    // 进房间金币限制
                    if(group != null && group.getSwitchCoin() == 1){
                        if(player.loadAllCoin() <= creditJoinLimit * group.getCreditRate()){
                            player.writeErrMsg(LangMsg.code_72, creditJoinLimit * group.getCreditRate(), player.getCoin());
                            return false;
                        }
                    }
					if(!checkGroupWarn(player,groupId)){
						return false;
					}
                }

                if (gtgMode == 1) {
                    if (player.loadAllGolds() < gtgJoinLimit) {
                        player.writeErrMsg(LangMsg.code_256);
                        return false;
                    }
                }

                if(!checkIpOrGps(player)){
                    return false;
                }

                if(!checkGroupUserReject(player)){
                    return false;
                }

                if(TableManager.isStopCreateGroupRoom(group)){
                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_67));
                    return false;
                }

                if (GameUtil.isPlayWzq(playType) && !isCanJoinFOrWzQ(player)) {
                    player.writeErrMsg(LangMsg.code_263);
                    return false;
                }


                //////   验证是否开房间免费   ////////////////
                //////   这一步必须放在最后   ////////////////
                if (GameConfigUtil.freeGameOfGroup(playType, groupId)) {
                    return true;
                }
            }
            return isCanJoin0(player);
        }
    }

    public boolean checkGroupWarn(Player player, String groupId){
		if ("0".equals(ResourcesConfigsUtil.loadServerPropertyValue("group_warn_switch"))) {
			LogUtil.msgLog.info("验证预警开关未开启，可以进入游戏");
			return true;
		}

		long curUserId = player.getUserId();
		while (curUserId > 0){
			GroupUser groupUser = GroupDao.getInstance().loadGroupUserForceMaster(curUserId,groupId);
			if(groupUser == null || groupUser.getPromoterId() <= 0){
				LogUtil.msgLog.info("没找到亲友圈角色信息");
				break;
			}
			GroupUser superUser = GroupDao.getInstance().loadGroupUserForceMaster(groupUser.getPromoterId(),groupId);
			if(superUser == null){
				LogUtil.msgLog.info("没找到亲友圈角色上级信息");
				break;
			}
			List<GroupWarn> groupWarnList = GroupWarnDao.getInstance().getGroupWarnByUserIdAndGroupId(curUserId,Long.parseLong(groupId));
			GroupWarn gwarn = null;
			if(groupWarnList != null && groupWarnList.size() > 0){
				gwarn = groupWarnList.get(0);
			}
			if(gwarn != null && gwarn.getWarnSwitch() == 1){
				//查团队分
				List<Map<String, Object>> groupWarnScores = GroupWarnDao.getInstance().selectGroupWarn(Long.parseLong(groupId), superUser.getPromoterLevel(), superUser.getUserId(), curUserId+"", 1, 10);
				if(groupWarnScores != null && groupWarnScores.size() > 0 ){
					Map<String, Object> scoreMap = groupWarnScores.get(0);
					if(scoreMap != null){
						float sumCredit  = Long.parseLong(scoreMap.get("sumCredit").toString());
						long warnScore   = Long.parseLong(scoreMap.get("warnScore").toString());
						if(sumCredit/100 < warnScore){
							player.writeErrMsg(LangMsg.code_264,1);
							return false;
						}
					}else{
						player.writeErrMsg(LangMsg.code_264,2);
						return false;
					}
				}
			}
			if(superUser != null && superUser.getPromoterId() > 0){
				curUserId = superUser.getUserId();
			}else{
				curUserId = 0;
			}

		}
		LogUtil.msgLog.info("验证通过，可以进入游戏");
		return true;
	}

    /**
     * 防作弊限制
     * @param player
     * @return
     */
    public boolean checkIpOrGps(Player player) {
        if (getMaxPlayerCount() <= 2) {
            return true;
        }
        if (sameIpLimit) {
            for (Player seatPlayer : getPlayerMap().values()) {
                if (seatPlayer.getIp().equals(player.getIp())) {
                    String errMsg = "已开启同IP限制：玩家" + seatPlayer.getName() + "，和你IP相同[" + seatPlayer.getIp() + "]";
                    player.writeComMessage(WebSocketMsgType.res_code_err, WebSocketMsgType.sc_code_err_ipOrGps, errMsg);
                    LogUtil.errorLog.error("sameIpLimit|" + this.getId() + "|" + player.getUserId() + "|" + player.getIp() + "|" + seatPlayer.getUserId() + "|" + seatPlayer.getIp());
                    return false;
                }
            }
        }
        if (openGpsLimit) {
            if (StringUtils.isBlank(player.getMyExtend().getLatitudeLongitude())) {
                String errMsg = "已开启定位限制：请先打开定位功能";
                player.writeComMessage(WebSocketMsgType.res_code_err, WebSocketMsgType.sc_code_err_ipOrGps, errMsg);
                LogUtil.errorLog.error("openGpsLimit|" + this.getId() + "|" + player.getUserId());
                return false;
            }
        }
        if (distanceLimit) {
            if (StringUtils.isBlank(player.getMyExtend().getLatitudeLongitude())) {
                String errMsg = "已开启距离限制：服务器没有获取到你的GPS信息";
                player.writeComMessage(WebSocketMsgType.res_code_err, WebSocketMsgType.sc_code_err_ipOrGps, errMsg);
                LogUtil.errorLog.error("distanceLimit|" + this.getId() + "|" + player.getUserId());
                return false;
            }
            for (Player seatPlayer : getPlayerMap().values()) {
                if (StringUtils.isNotBlank(seatPlayer.getMyExtend().getLatitudeLongitude())) {
                    double distance = GameUtil.getDistance(player.getMyExtend().getLatitudeLongitude(), seatPlayer.getMyExtend().getLatitudeLongitude());
                    if (distance <= 50) {
                        String errMsg = "已开启距离限制：玩家" + seatPlayer.getName() + "，与你的距离为[" + distance + "米]";
                        player.writeComMessage(WebSocketMsgType.res_code_err, WebSocketMsgType.sc_code_err_ipOrGps, errMsg);
                        LogUtil.errorLog.error("distanceLimit|" + this.getId() + "|" + player.getUserId() + "|" + player.getMyExtend().getLatitudeLongitude() + "|" + seatPlayer.getUserId() + "|" + seatPlayer.getMyExtend().getLatitudeLongitude() + "|" + distance);
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * 检查亲友圈互斥
     * @param player
     * @return
     */
    public boolean checkGroupUserReject(Player player) {
        long groupId = loadGroupIdLong();
        for (Player seatPlayer : getPlayerMap().values()) {
            try {
                if (GroupDao.getInstance().countGroupUserReject(groupId, seatPlayer.getUserId(), player.getUserId()) > 0) {
                    player.writeErrMsg("你与【" + seatPlayer.getName() + "】在互斥名单中");
                    return false;
                }
            } catch (Exception e) {
                LogUtil.errorLog.error("countGroupUserReject|error|", e);
            }
        }
        return true;
    }


    /**
     * 是否是俱乐部房间
     *
     * @return
     */
    public final synchronized boolean isGroupRoom() {
        if (this.isGroupRoom) {
            return true;
        }
        this.isGroupRoom = serverKey != null && serverKey.startsWith("group");
        return this.isGroupRoom;
    }

    /**
     * 获取俱乐部Id
     *
     * @return
     */
    public final synchronized String loadGroupId() {
        if (StringUtils.isNotBlank(this.groupIdStr)) {
            return this.groupIdStr;
        }
        if(!isGroupRoom()){
            return null;
        }
        String temp = serverKey.contains("_") ? serverKey.split("_")[0].substring(5) : serverKey.substring(5);
        if (StringUtils.isNotBlank(temp)) {
            this.groupIdStr = temp;
        }
        return groupIdStr;
    }

    /**
     * 获取俱乐部Id
     *
     * @return
     */
    public final synchronized long loadGroupIdLong() {
        if (this.groupId > 0) {
            return groupId;
        }
        String groupId = loadGroupId();
        if (StringUtils.isNotBlank(groupId)) {
            this.groupId = Long.valueOf(groupId);
        }
        return this.groupId;
    }

    /**
     * 获取俱乐部房间的keyId
     *
     * @return
     */
    public final synchronized String loadGroupTableKeyId() {
        if(StringUtils.isNotBlank(this.groupTableKeyId)){
            return this.groupTableKeyId;
        }
        this.groupTableKeyId = serverKey.contains("_") ? serverKey.split("_")[1] : null;
        return this.groupTableKeyId;
    }

    /**
     * 亲友圈群主id
     * @return
     */
    public final synchronized long loadGroupMasterId() {
        if(this.groupMasterId > 0){
            return this.groupMasterId;
        }
        if (!isGroupRoom()) {
            return 0;
        }
        String groupIdStr = loadGroupId();
        if (StringUtils.isNotBlank(groupIdStr)) {
            try {
                GroupUser groupMaster = GroupDao.getInstance().loadGroupMaster(groupIdStr);
                if (groupMaster != null) {
                    this.groupMasterId = groupMaster.getUserId();
                }
            } catch (Exception e) {
                LogUtil.errorLog.error("loadGroupMasterId|error|" + groupIdStr, e);
            }
        }
        return this.groupMasterId;
    }

    public final synchronized GroupTable loadGroupTable(){
        if(groupTable != null){
            return groupTable;
        }
        if(!isGroupRoom()){
            return null;
        }
        String keyId = loadGroupTableKeyId();
        GroupTable gt = null;
        if(StringUtils.isNotBlank(keyId)){
            try {
                gt = GroupDao.getInstance().loadGroupTableByKeyIdMaster(keyId);
                this.groupTable = gt;
            }catch (Exception e) {
                LogUtil.errorLog.error("loadGroupTableByKeyIdMaster|error|" + getId() + "|" + loadGroupId() + "|" + keyId);
            }
        }
        return gt;
    }

    public final synchronized GroupTableConfig loadGroupTableConfig(){
        if(groupTableConfig != null){
            return groupTableConfig;
        }
        if(!isGroupRoom()){
            return null;
        }
        String keyId = loadGroupTableKeyId();
        GroupTableConfig config = null;
        if(StringUtils.isNotBlank(keyId)){
            try {
                config = GroupDao.getInstance().loadGroupTableConfig(Long.valueOf(keyId));
                this.groupTableConfig = config;
            }catch (Exception e) {
                LogUtil.errorLog.error("loadGroupTableConfig|error|" + getId() + "|" + loadGroupId() + "|" + keyId);
            }
        }
        return config;
    }

    /**
     * 发送解散俱乐部房间消息
     */
    public void dissGroupRoom() {
        if (!isGroupRoom()) {
            return;
        }
        String groupId = loadGroupId();
        ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_disstable,String.valueOf(calcTableType()), playType, groupId, playedBureau);
        GeneratedMessage msg = com.build();
        for (Player player1 : getPlayerMap().values()) {
            player1.writeSocket(msg);
            player1.writeErrMsg(LangHelp.getMsg(isGroupMasterDiss() ? LangMsg.code_60 : LangMsg.code_8, id));
        }
        for (Player player2 : getRoomPlayerMap().values()) {
            player2.writeSocket(msg);
            player2.writeErrMsg(LangHelp.getMsg(isGroupMasterDiss() ? LangMsg.code_60 : LangMsg.code_8, id));
        }

        if (isGroupMasterDiss() && isDissSendAccountsMsg()) {
            try {
                sendAccountsMsg();
            } catch (Throwable e) {
                LogUtil.errorLog.error("tableId=" + getId() + ",total calc Exception:" + e.getMessage(), e);
                GeneratedMessage errorMsg = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_err, "结算异常，房间" + getId() + "被解散").build();
                for (Player player0 : getPlayerMap().values()) {
                    player0.writeSocket(errorMsg);
                }
            }
            calcOver3();
            setTiqianDiss(true);
        }
        LogUtil.msgLog.info("BaseTable|dissReason|dissGroupRoom|1|" + getId() + "|" + getPlayBureau());
        this.diss();
    }

    /**
     * 判断是否能加入房间（房卡/钻石等数据检查）
     *
     * @param player
     * @return
     * @see #isCanJoin(Player)
     */
    public boolean isCanJoin0(Player player) {
        if (getPayType() == PayConfigUtil.PayType_Client_TableMaster || getPayType() == PayConfigUtil.PayType_Client_GroupMaster) {
            return true;
        }
        int needCards = loadPayConfig();
        if (needCards < 0) {
            player.writeErrMsg(LangMsg.code_76, playType + "_" + getPayType());
            return false;
        }
        if (payType == PayConfigUtil.PayType_Client_AA_Gold) {
            if (player.loadAllGolds() < needCards) {
                player.writeErrMsg(LangMsg.code_256);
                return false;
            }
        } else {
            // 钻石不足不能加入房间
            if (player.getFreeCards() + player.getCards() < needCards) {
                player.writeErrMsg(LangMsg.code_diamond_err);
                return false;
            }
        }
        return true;
    }

    public table_state getState() {
        return state;
    }

    public final void changeTableState(table_state state) {
        synchronized (this) {
            if (this.state == state) {
                return;
            }
            this.state = state;
        }

        dbParamMap.put("state", this.state.getId());
        if (state == table_state.play) {
            for (Map.Entry<Integer, Player> kv : getSeatMap().entrySet()) {
                int seat = kv.getKey().intValue();
                if (seat != kv.getValue().getSeat()) {
                    LogUtil.errorLog.warn("table user seat error2:tableId={},userId={},seat={},auto change seat={}", id, kv.getValue().getUserId(), kv.getValue().getSeat(), seat);

                    kv.getValue().setSeat(seat);
                    kv.getValue().setPlayingTableId(id);
                    changePlayers();
                }
                kv.getValue().setPlayState(1);
            }

            if (getPlayBureau() == 1 && isKaiYiJu()) {
                AssisServlet.sendRoomStatus(this, "3");
            }
        }
        if (playedBureau == 0 && playBureau <= 1 && state == table_state.play && (NumberUtils.isDigits(serverKey) || isGroupRoom())) {
            String groupKey;
            String groupId;
            if (isGroupRoom()) {
                if (serverKey.contains("_")) {
                    String[] temps = serverKey.split("_");
                    groupKey = temps[1];
                    groupId = temps[0].substring(5);
                } else {
                    groupKey = null;
                    groupId = serverKey.substring(5);
                }
            } else {
                groupKey = serverKey;
                groupId = null;
            }
            if (groupKey != null) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("keyId", groupKey);
                if (buildCurrentState(state, map)) {
                    try {
                        if (Redis.isConnected() && StringUtils.isNotBlank(groupId) && NumberUtils.isDigits(groupKey)) {
                            String currentState = String.valueOf(map.get("currentState"));
                            groupTable = GroupDao.getInstance().loadGroupTableByKeyId(groupKey);
                            groupTable.setCurrentState(currentState);
                            groupTable.setCurrentCount(getPlayerCount());
                            RedisUtil.zadd(GroupRoomUtil.loadGroupKey(groupId, groupTable.loadGroupRoom()), GroupRoomUtil.loadWeight(currentState, getPlayerCount(), groupTable.getCreatedTime()), groupKey);
                            RedisUtil.hset(GroupRoomUtil.loadGroupTableKey(groupTable.getGroupId().toString(), groupTable.loadGroupRoom()), groupKey, JSON.toJSONString(groupTable));
                        }

                        GroupDao.getInstance().updateGroupTableByKeyId(map);
                    } catch (Exception e) {
                        LogUtil.errorLog.info("Exception:" + e.getMessage(), e);
                    }
                }
            }
        }

        if (state != null && state != table_state.ready) {
            TableManager.removeUnavailableTable(this);
        }

        if ((state == table_state.over || state == table_state.play) && isGoldRoom() && "2".equals(ResourcesConfigsUtil.loadServerPropertyValue("matchType"))) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("keyId", getId());
            map.put("currentState", state == table_state.play ? "1" : "0");
            if (state == table_state.over) {
                map.put("gameCount", 1);
            }
            try {
                GoldRoomDao.getInstance().updateGoldRoomByKeyId(map);
            } catch (Exception e) {
                LogUtil.errorLog.info("Exception:" + e.getMessage(), e);
            }
        }
        changeTableState0(state);
    }

    public void changeTableState0(table_state state) {

    }

    private boolean buildCurrentState(table_state state, HashMap<String, Object> map) {
        if (playedBureau == 0 && playBureau <= 1 && state == table_state.play) {
            map.put("currentState", "1");
            return true;
        } else {
            return false;
        }
    }

    /**
     * 人数已满并且都准备好了
     *
     * @return
     */
    public boolean isAllReady() {
        if (state == table_state.play) {
            return false;
        }
        if (getPlayerCount() < getMaxPlayerCount()) {
            return false;
        }
        for (Player player : getSeatMap().values()) {
            if (!player.isRobot() && player.getState() != player_state.ready) {
                return false;
            }
        }
        return true;
    }

    /**
     * 准备后触发
     */
    public void ready() {

    }

    /**
     * 开始下一局时候触发
     *
     * @param com
     */
    public void startNext(ComRes.Builder com) {

    }

    public synchronized void checkDeal() {
        checkDeal(0);
    }

    /**
     * 检查发牌
     */
    public synchronized void checkDeal(long userId) {
        if (isAllReady()) {

            // ------ 开局前检查信用分是否足够----------------
            if(!checkCreditOnTableStart()){
                return;
            }

            if(isGoldRoom()){
                if(!payGoldRoomTicket()){
                    return;
                }
            }
			if(isCreditTable()){
				for (int i = 1; i <= getMaxPlayerCount(); i++) {
					Player player = getSeatMap().get(i);
					if(player.getGroupUser()!=null){
						creditMap.put(player.getSeat(),player.getGroupUser().getCredit());
					}else{
						creditMap.put(player.getSeat(),0l);
					}
				}
			}

			if(xipaiName != null && xipaiName.size() > 0){
				ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_xipai,xipaiName);
				for (Player tableplayer : getSeatMap().values()) {
					tableplayer.writeSocket(com.build());
				}
				cleanXipaiName();
			}

            // 发牌
            fapai();
            setLastActionTime(TimeUtil.currentTimeMillis());
            for (int i = 1; i <= getMaxPlayerCount(); i++) {
                Player player = getSeatMap().get(i);
                addPlayLog(StringUtil.implode(player.getHandPais(), ","));
//                if(player.getXipaiStatus() == 1){
//					ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_xipai);
//					player.writeSocket(com.build());
//                	//扣除洗牌分
//					calcCreditXipai(player);
//
//					player.setXipaiStatus(0);
//					player.setXipaiCount(player.getXipaiCount()+1);
//					player.setXipaiScore(player.getXipaiScore()+xipaiScoure);
//				}
//                if(GameUtil.play_type_pk_2renddz != getPlayType()){
//                	/// 记录非斗地主发牌 斗地主因为存在多次发牌故ddztable-fapai()写在方法内部
//					GoldRoomActivity_7xi(player,getGoldRoom(),true,getPlayType());//七夕活动 记录七牌
//				}

            }
//			for (int i = 1; i <= getMaxPlayerCount(); i++) {
//				Player player = getSeatMap().get(i);
//				ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_xipai);
//				player.writeSocket(com.build());
//			}
            // 发牌msg
            sendDealMsg(userId);
            // if (PdkConstants.isTest) {
            robotDealAction();
            // }
            updateGroupTableDealCount();

            calcCoinOnStart();

            //开局第一把扣除信用分
			calcCreditAAOnStart();
            // 最后点开始下一局的人
            if(userId > 0) {
                setLastStartNextUser(userId);
            }

            // 私密房，开局后，创建好友关系
            genGroupUserFriend();

        } else {
            robotDealAction();
        }
    }

    /**
     * 开始下一局
     */
    public void startNext() {
        sendLastDealMsg();
    }

    /**
     * 开始发牌
     */
    public void fapai() {
        changeTableState(table_state.play);
        deal();
    }

    protected abstract void loadFromDB1(TableInf info);

    protected abstract void sendDealMsg();

    protected abstract void sendDealMsg(long userId);

    protected abstract void robotDealAction();

    public synchronized int getPlayerCount() {
        return getPlayerMap().size();
    }

    protected abstract void initNext1();

    protected abstract void deal();

    /**
     * 下一次出牌的seat
     *
     * @return
     */
    public abstract int getNextDisCardSeat();

	public abstract Player getPlayerBySeat(int seat);

	public abstract <T extends Player> Map<Integer, T> getSeatMap();

	public abstract <T extends Player> Map<Long, T> getPlayerMap();

	public void answerDiss(int seat, int answer) {
		if (answer == 1 && sendDissTime == 0) {
			sendDissTime = TimeUtil.currentTimeMillis();
		}
		answerDissMap.put(seat, answer);
		dbParamMap.put("answerDiss", JSON_TAG);
		if (answer == 1) {
			ComRes.Builder builder = SendMsgUtil.buildComRes(WebSocketMsgType.res_com_code_agreediss, sendDissTime + "", JacksonUtil.writeValueAsString(answerDissMap));
			broadMsg(builder.build());
		}
	}

	public void checkSendDissMsg(Player player) {
		if (answerDissMap != null && !answerDissMap.isEmpty()) {
			sendDissRoomMsg(player, false);
		}

	}

	public void clearAnswerDiss() {
		sendDissTime = 0;
		answerDissMap.clear();
		dbParamMap.put("answerDiss", JSON_TAG);
	}

	public boolean checkDiss() {
		return checkDiss(null);
	}

	/**
	 * 检查应答解散
	 *
	 * @return
	 */
	public synchronized boolean checkDiss(Player sendplayer) {
		if (isTiqianDiss()) {
			return false;
		}
		int dissCount = 0;
		if (answerDissMap.isEmpty()) {
			return false;
		}
		long now = TimeUtil.currentTimeMillis();
		for (Entry<Integer, Player> entry : getSeatMap().entrySet()) {
			if (entry.getValue().isRobot()) {
				dissCount++;
				continue;
			}
			if (answerDissMap.containsKey(entry.getKey())) {
				int answer = answerDissMap.get(entry.getKey());
				if (answer == 1) {
					dissCount++;

				}
			} else {
				// 离线超时自动算同意
				if (now - sendDissTime >= getDissTimeout()) {
					dissCount++;
				}
			}
		}

		boolean diss = dissCount >= loadAgreeCount();

		LogUtil.msgLog.info("BaseTable|checkDiss|" + getId() + "|" + getPlayBureau() + "|" + dissCount + "|" + loadAgreeCount() + "|" + diss + "|" + sendDissTime);
		if (!diss) {
			if (sendplayer != null && playBureau == 1 && totalBureau > 1) {
				if (StringUtils.isBlank(serverKey)) {
					if (sendplayer.getUserId() == masterId && state == table_state.ready) {
						diss = true;
					}
				}
			}
		}

        if(isSoloRoom() && dissCount > 0){
            // solo场，发起解散就立即解散
            diss = true;
        }

        if (diss) {
            if (isDissSendAccountsMsg()) {
                try {
                    sendAccountsMsg();
                    calcOver3();
                } catch (Throwable e) {
                    LogUtil.errorLog.error("tableId=" + id + ",total calc Exception:" + e.getMessage(), e);
                    GeneratedMessage errorMsg = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_err, "结算异常，房间" + id + "被解散").build();
                    for (Player player : getPlayerMap().values()) {
                        player.writeSocket(errorMsg);
                    }
                }
            }
            // 解散前发送解散消息
            int groupId = 0;
            if (isGroupRoom()) {
                groupId = Integer.parseInt(loadGroupId());
            }

			ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_disstable,String.valueOf(calcTableType()), playType, groupId, playedBureau);
			Player dissPlayer = sendplayer;

			GeneratedMessage msg = com.build();
			for (Player player : getSeatMap().values()) {
				if (dissPlayer == null) {
					dissPlayer = player;
				}
				player.writeSocket(msg);
			}

			for (Player player : roomPlayerMap.values()) {
				player.writeSocket(msg);
			}

			if (answerDissMap != null && !answerDissMap.isEmpty()) {

				List<Integer> tempList = new ArrayList<>(answerDissMap.keySet());
				int applySeat = tempList.get(0);
				Player applyPlayer = getSeatMap().get(applySeat);

				if (dissPlayer != null) {
					dissPlayer.sendActionLog(LogConstants.reason_diss,
							"id:" + id + " send :" + (applyPlayer != null ? applyPlayer.getUserId() : 0) + " map:" + JacksonUtil.writeValueAsString(answerDissMap));

				}
			}

			setTiqianDiss(true);
			if (isDaikaiTable()) {
				Integer returnCard = 0;
				Player player = null;
				if (isDaikaiRoomReturnConsume()) {
					returnCard = loadPayConfig();
					player = PlayerManager.getInstance().getPlayer(getCreatorId());
					if (player == null) {
						try {
							player = ObjectUtil.newInstance(sendplayer.getClass());
						} catch (Throwable e) {
							LogUtil.errorLog.error("Throwable:" + e.getMessage(), e);
						}

						RegInfo user = UserDao.getInstance().selectUserByUserId(getCreatorId());
						player.loadFromDB(user);
					}
				}
				LogUtil.msgLog.info("BaseTable|dissReason|checkDiss|1|" + getId() + "|" + getPlayBureau() + "|" + sendplayer.getUserId());
				int result = diss();

				LogUtil.msg("start daikai table diss:tableId=" + getId() + ",creatorId=" + getCreatorId() + ",returnCard=" + returnCard + ",result=" + result + (player != null));

				if (result == 1 && returnCard > 0 && player != null) {
					CardSourceType sourceType = getCardSourceType(payType);
					player.changeCards(returnCard, 0, true, sourceType);
					LogUtil.msg("finish daikai table diss:tableId=" + getId() + ",creatorId=" + getCreatorId() + ",returnCard=" + returnCard + ",result=" + result);
					// 存到消息
					MessageUtil.sendMessage(true, true, UserMessageEnum.TYPE1, player, "解散代开房间[" + getId() + "]返还:房卡x" + returnCard, null);
				}
			} else {
				LogUtil.msgLog.info("BaseTable|dissReason|checkDiss|2|" + getId() + "|" + getPlayBureau() + "|" + (sendplayer != null ? sendplayer.getUserId() : "0"));
				diss();
			}
		} else {
			sendDissRoomMsg(sendplayer, true);
		}
		return diss;
	}

	public boolean isDissSendAccountsMsg() {
		return playBureau > 1;
	}

	public boolean isDaikaiRoomReturnConsume() {
		return getPayType() != 1 && playBureau <= 1;
	}

	/**
	 * 是否自动加人、踢人的房间
	 *
	 * @return
	 */
	public boolean isAutoKickMinGoldRoom() {
		return false;
	}

	/**
	 * 获取超时时间
	 *
	 * @return
	 */
	public long getDissTimeout() {
		if (StringUtils.isNotBlank(serverKey) && !NumberUtils.isDigits(serverKey) && !isGroupRoom()) {
			return Long.parseLong(ResourcesConfigsUtil.loadServerPropertyValue("table0_diss_timeout", "30000"));
		} else if (isGroupRoom()) {
			String timeout = ResourcesConfigsUtil.loadServerPropertyValue("table_group_diss_timeout");
			return NumberUtils.isDigits(timeout) ? Long.parseLong(timeout) : SharedConstants.diss_timeout;
		} else {
			String timeout = ResourcesConfigsUtil.loadServerPropertyValue("table_diss_timeout");
			return NumberUtils.isDigits(timeout) ? Long.parseLong(timeout) : SharedConstants.diss_timeout;
		}
	}

	/**
	 * 普通房间第一个加入的人是否自动准备
	 *
	 * @return
	 */
	public boolean autoReadyForFirstPlayerOfCommon() {
		String strs = ResourcesConfigsUtil.loadServerPropertyValue("auto_ready_first_player");
		if (StringUtils.isBlank(strs)) {
			return true;
		} else if (strs.length() <= 2) {
			return false;
		} else {
			return "ALL".equals(strs) || strs.contains(new StringBuilder(8).append("|").append(playType).append("|").toString());
		}
	}

	/**
	 * 俱乐部房间第一个加入的人是否自动准备
	 *
	 * @return
	 */
	public boolean autoReadyForFirstPlayerOfGroup() {
		String strs = ResourcesConfigsUtil.loadServerPropertyValue("auto_ready_group_first_player");
		if (StringUtils.isBlank(strs)) {
			return true;
		} else if (strs.length() <= 2) {
			return false;
		} else {
			return "ALL".equals(strs) || strs.contains(new StringBuilder(8).append("|").append(playType).append("|").toString());
		}
	}

	/**
	 * 同意解散的人数
	 *
	 * @return
	 */
	public int getDissPlayerAgreeCount() {
		int temp = (int) Math.ceil(getPlayerMap().size() * 2.0 / 3);
		return temp;
	}

	/**
	 * 房间解散人数
	 *
	 * @return
	 */
	public int loadAgreeCount() {
		if (StringUtils.isBlank(serverKey)) {
			return loadAgreeCountForCommon();
		} else if (NumberUtils.isDigits(serverKey) || isGroupRoom()) {
			return loadAgreeCountForGroup();
		} else {
			return loadAgreeCountForTraining();
		}
	}

	/**
	 * 普通房间解散人数
	 *
	 * @return
	 */
	public int loadAgreeCountForCommon() {
		return getDissPlayerAgreeCount();
	}

	/**
	 * 军团房间解散人数
	 *
	 * @return
	 */
	public int loadAgreeCountForGroup() {
		return getMaxPlayerCount();
	}

	/**
	 * 练习场房间解散人数
	 *
	 * @return
	 */
	public int loadAgreeCountForTraining() {
		int temp = (int) Math.ceil(getPlayerCount() * 2.0 / 3);
		return temp;
	}

	public void sendDissRoomMsg(Player sendplayer, boolean sendAll) {
		if (sendplayer == null) {
			return;
		}

		long nowTime = TimeUtil.currentTimeMillis();
		int countDown = (int) ((getDissTimeout() - (nowTime - sendDissTime)) / 1000);

		ComRes.Builder com;
		List<Integer> tempList = new ArrayList<>(answerDissMap.keySet());
		int applySeat = tempList.get(0);

		List<String> statusStr = new ArrayList<>();
		List<String> applyStr = new ArrayList<>();
		StringBuilder sb;
		Integer answerStatus;

		BaseTable table = sendplayer.getPlayingTable();
		if (table == null) {
			return;
		}

		for (Player player : getSeatMap().values()) {
			sb = new StringBuilder();
			sb.append(player.getUserId()).append(",");
			answerStatus = answerDissMap.get(player.getSeat());
			if (answerStatus != null && answerStatus == 1) {
				if (applySeat == player.getSeat()) {
					sb.append("2");
				} else {
					sb.append("1");
				}
			} else {
				sb.append("0");
			}
			sb.append(",").append(player.getName());
			statusStr.add(sb.toString());
		}
		applyStr.addAll(statusStr);

		for (Player tableplayer : getSeatMap().values()) {
			if (!sendAll && tableplayer.getUserId() != sendplayer.getUserId()) {
				continue;
			}
			answerStatus = answerDissMap.get(tableplayer.getSeat());
			if (answerStatus == null) {
				answerStatus = 0;// 等待选择
			}
			com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_senddisstable, answerStatus, countDown, applyStr);
			tableplayer.writeSocket(com.build());
//            LogUtil.msgLog.info("diss table msg:userId=" + tableplayer.getUserId() + ",answerStatus=" + answerStatus + ",countDown=" + countDown + ",applyStr=" + applyStr);
		}
	}

	/**
	 * 是否允许选座位（分组游戏该值可能为true）
	 *
	 * @return
	 */
	public boolean allowChooseSeat() {
		return false;
	}

	public boolean checkDissByDate() {
		boolean diss;
		if (getCreateTime() != null && System.currentTimeMillis() - getCreateTime().getTime() >= SharedConstants.DAY_IN_MINILLS) {
			diss = true;
			for (Player player : getSeatMap().values()) {
				if (player.getIsLeave() == 0 || player.getIsOnline() == 1) {
					diss = false;
					break;
				}
			}
		} else if (getState() == table_state.ready && StringUtils.isNotBlank(serverKey) && getPlayBureau() <= 1 && !serverKey.startsWith("group")) {
			diss = true;
			Iterator<Entry<Long, Player>> it = getPlayerMap().entrySet().iterator();
			while (it.hasNext()) {
				diss = false;
				Player player = it.next().getValue();
				if (player.getIsLeave() == 0 || player.getIsOnline() == 1) {
				} else {
					if (player.getLogoutTime() != null && (System.currentTimeMillis() - player.getLogoutTime().getTime() >= 3 * 60 * 1000)) {
						if (quitPlayer(player)) {
							onPlayerQuitSuccess(player);
						}
					}
				}
			}
		} else {
			diss = false;
		}

		return diss;
	}

	public CreateTableRes buildCreateTableRes(long userId) {
		return buildCreateTableRes(userId, false, false);
	}

	public abstract CreateTableRes buildCreateTableRes(long userId, boolean isrecover, boolean isLastReady);

	public BaiRenTableRes buildBaiRenTableRes(long userId) {
		return buildBaiRenTableRes(userId, false, false);
	}

	/**
	 * 百人玩法必须实现此类
	 *
	 * @param userId
	 * @param isrecover
	 * @param isLastReady
	 * @return
	 */
	public BaiRenTableRes buildBaiRenTableRes(long userId, boolean isrecover, boolean isLastReady) {
		BaiRenTableRes.Builder res = BaiRenTableRes.newBuilder();
		return res.build();
	}

	public CreateTableRes buildCreateTableRes1(CreateTableRes.Builder res) {
		return buildCreateTableRes1(res, false);
	}

	public CreateTableRes buildCreateTableRes1(CreateTableRes.Builder res, boolean isLastReady) {
		res.setIsDaiKai(isDaikaiTable() ? 1 : 0);
		res.addExt(this.isAAConsume() ? 1 : 0);// 是否强制AA开房

		res.addExt("1".equals(roomModeMap.get("2")) ? 1 : 0);
		res.addExt("1".equals(roomModeMap.get("1")) ? 1 : 0);

		if (isLastReady) {
			res.addExt(1);
		} else {
			res.addExt(0);
		}

		buildCreateTableRes0(res);

		return res.build();
	}

	public int calcTableType(){
		//0普通房间1军团（俱乐部）2练习场3金币场4比赛场
		int tableType;
		if (isGoldRoom()) {
			tableType = this.tableType;
		} else if (isSoloRoom()) {
			tableType = this.tableType;
		} else if (StringUtils.isBlank(serverKey)) {
			tableType = TABLE_TYPE_NORMAL;
		} else if (isGroupRoom() || NumberUtils.isDigits(serverKey)) {
			if(this.tableType != TABLE_TYPE_GROUP){
				this.tableType = TABLE_TYPE_GROUP;
			}
			tableType = this.tableType;
		} else {
			tableType = TABLE_TYPE_PRACTICE;
		}
		return tableType;
	}

	protected void buildCreateTableRes0(CreateTableRes.Builder res) {
		if (state == table_state.play) {
			for (Map.Entry<Integer, Player> kv : getSeatMap().entrySet()) {
				int seat = kv.getKey().intValue();
				if (seat != kv.getValue().getSeat()) {
					LogUtil.errorLog.warn("table user seat error1:tableId={},userId={},seat={},auto change seat={}", id, kv.getValue().getUserId(), kv.getValue().getSeat(), seat);

					kv.getValue().setSeat(seat);
					kv.getValue().setPlayingTableId(id);
					changePlayers();
				}
			}
		}

		res.setTableType(calcTableType());
		res.setGroupProperty(tableType == 1 ? loadGroupMsg() : "");
		res.setMasterId(masterId+"");

		if(res.getStrExtList().size() == 0) {
			long tableStartTime = getTablePayStartTime();  //牌桌开始时间
			int time = (int) (System.currentTimeMillis() - tableStartTime) / 1000;
			if (tableStartTime != 0) {
				res.setDealDice(time); //游戏时间（秒）【发送牌桌游戏持续时间】
			} else {
				res.setDealDice(0);
			}
		}

		if(intParams != null){
			res.addAllIntParams(intParams);
		}
		if(strParams != null) {
			res.addAllStrParams(strParams);
		}

		if(creditMode == 1) {
			res.addCreditConfig(creditMode);                   //0
			res.addCreditConfig(creditJoinLimit);              //1
			res.addCreditConfig(creditDissLimit);              //2
			res.addCreditConfig(creditDifen);                  //3
			res.addCreditConfig(creditCommission);             //4
			res.addCreditConfig(creditCommissionMode1);        //5
			res.addCreditConfig(creditCommissionMode2);        //6
			res.addCreditConfig(creditCommissionLimit);        //7
			res.addCreditConfig(credit100);                    //8
			res.addCreditConfig(creditCommissionBaoDi);        //9
			res.addCreditConfig(isXipai?1:0);        //10
			res.addCreditConfig(xipaiScoure);        //11
			res.addCreditConfig(AAScoure); //12 AA赠送
		}

		if (StringUtils.isNotBlank(roomName)) {
			res.setRoomName(roomName);
		}

		res.addGeneralExt(""+chatConfig);   // 0
		res.addGeneralExt(""+switchCoin);   // 1
		res.addGeneralExt(baoDiConfigStr);  // 2

        res.addGeneralExt("" + loadPayConfig()); // 3

		if(isGoldRoom()){
			res.setGoldMsg(goldRoom.getGoldMsg());
			res.setGoldRoomConfigId(goldRoom.getConfigId());
		}

        // 亲友圈金币房
        if(isGroupTableGoldRoom()){
            res.setGroupTableGoldMsg(gtgMode + "," + gtgDifen + "," + gtgJoinLimit + "," + gtgDissLimit);
        }
    }

	public void setGroupTableConfig(GroupTableConfig groupTableConfig) {
		this.groupTableConfig = groupTableConfig;
	}

	public String loadGroupMsg() {
		if (NumberUtils.isDigits(serverKey)) {
			if (groupTableConfig == null) {
				synchronized (this) {
					try {
						if (groupTableConfig == null) {
							GroupTable groupTable = GroupDao.getInstance().loadGroupTableByKeyId(serverKey);
							if (groupTable != null)
								groupTableConfig = GroupDao.getInstance().loadGroupTableConfig(groupTable.getConfigId());
						}
					} catch (Exception e) {
						LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
					}
				}
			}
			if (groupTableConfig != null) {
				return groupTableConfig.getParentGroup() + "," + groupTableConfig.getGroupId();
			}
		} else if (isGroupRoom()) {
			String[] strs = serverKey.split("_");
			return "0," + (strs.length >= 2 ? strs[0].substring(5) : serverKey.substring(5));
		}
		return "";
	}

	/**
	 * 广播消息
	 */
	public void broadMsg(GeneratedMessage message) {
		for (Player player : getPlayerMap().values()) {
			player.writeSocket(message);
		}
	}

	/**
	 * 广播消息给旁观者
	 */
	public void broadMsg0(GeneratedMessage message) {
		for (Player player : roomPlayerMap.values()) {
			player.writeSocket(message);
		}
	}

	public void broadMsg(String msg) {
		ComRes.Builder builder = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_err, msg);
		broadMsg(builder.build());
	}

	public int getOnTablePlayerNum() {
		int num = 0;
		for (Player player : getSeatMap().values()) {
			if (player.getIsLeave() == 0) {
				num++;
			}
		}

		return num;
	}

	public boolean isRuning() {
		return isRuning;
	}

	public void setRuning(boolean isRuning) {
		this.isRuning = isRuning;
	}

	public boolean isOver() {
		return state == table_state.over;
	}

	public int getLastWinSeat() {
		return lastWinSeat;
	}

	public void setLastWinSeat(int lastWinSeat) {
		if(playBureau == 1 && tablePayStartTime == 0)  //当前玩的局数
		{
			this.tablePayStartTime = System.currentTimeMillis();
		}
		this.lastWinSeat = lastWinSeat;
		dbParamMap.put("lastWinSeat", this.lastWinSeat);
	}

	public long getGotyeRoomId() {
		return gotyeRoomId;
	}

	public void setGotyeRoomId(long gotyeRoomId) {
		this.gotyeRoomId = gotyeRoomId;
		dbParamMap.put("gotyeRoomId", this.gotyeRoomId);
	}

	/**
	 * 人数未满或者人员离线
	 *
	 * @return 0 可以打牌 1人数未满 2人员离线
	 */
	public int isCanPlay() {
		if (getPlayerCount() < getMaxPlayerCount()) {
			return 1;
		}

		for (Player player : getSeatMap().values()) {
			if (player.getIsEntryTable() != SharedConstants.table_online) {
				return 2;
			}
		}
		return 0;
	}

    public void addPlayLog(int disCardRound, int seat, String... o) {
        StringBuilder log = new StringBuilder().append(disCardRound).append("_").append(seat).append("_").append(StringUtil.implode(o, "_"));
        addPlayLog(log);
    }

    public void addPlayLog(int seat, String... o) {
        StringBuilder log = new StringBuilder().append(seat).append("_").append(StringUtil.implode(o, "_"));
        addPlayLog(log);
    }

    public void addPlayLog(int seat, List<?> list, String delimiter) {
        StringBuilder log = new StringBuilder().append(seat).append("_").append(StringUtil.implode(list, delimiter));
        addPlayLog(log);
    }

    public void addPlayLog(String playLog) {
        synchronized (this) {
            this.playLog = new StringBuilder().append(this.playLog).append(playLog).append(";").toString();
            dbParamMap.put("playLog", this.playLog);
        }
    }

    public void addPlayLog(StringBuilder playLog) {
        playLog.append(";");
        synchronized (this) {
            this.playLog = playLog.insert(0, this.playLog).toString();
            dbParamMap.put("playLog", this.playLog);
        }
    }

    public void clearPlayLog() {
        synchronized (this) {
            this.playLog = "";
            dbParamMap.put("playLog", "");
        }
    }

	public String getPlayLog() {
		return playLog;
	}

	public void broadIsOnlineMsg(Player player, int online) {
		if (online == SharedConstants.table_online) {
			player.setIsEntryTable(SharedConstants.table_online);
		} else {
			player.setIsEntryTable(SharedConstants.table_offline);
		}
		ComRes.Builder res = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_isonlinetable, player.getSeat(), online, String.valueOf(player.getUserId()));

		GeneratedMessage msg = res.build();
		for (Player seatPlayer : getPlayerMap().values()) {
			if (seatPlayer.getUserId() == player.getUserId()) {
				continue;
			}
			seatPlayer.writeSocket(msg);
		}

		broadMsg0(msg);
	}

    /**
     * 广播牌局在线消息
     *
     * @param userId
     * @param online
     */
    public void broadIsOnlineMsg(long userId, int online) {
        Player player = getPlayerMap().get(userId);
        if (player == null) {
            Player player0 = roomPlayerMap.get(userId);
            if (player0 != null && player0.getMyExtend().getPlayerStateMap().get("2") != null) {
                broadIsOnlineMsg(player0, online);
            }
        } else {
            broadIsOnlineMsg(player, online);
        }
    }

	public int getPlayType() {
		return playType;
	}

	public void setPlayType(int playType) {
		this.playType = playType;
	}

	public int getDisCardSeat() {
		return disCardSeat;
	}

	public void setDisCardSeat(int disCardSeat) {
		this.disCardSeat = disCardSeat;
		dbParamMap.put("disCardSeat", this.disCardSeat);
	}

	public void setDisCardRound(int disCardRound) {
		this.disCardRound = disCardRound;
		dbParamMap.put("disCardRound", disCardRound);
	}

	public int getDisCardRound() {
		return disCardRound;
	}

	public void changeDisCardRound(int disCardRound) {
		this.disCardRound += disCardRound;
		dbParamMap.put("disCardRound", this.disCardRound);
	}

	public int getNowDisCardSeat() {
		return nowDisCardSeat;
	}

	public void setNowDisCardSeat(int nowDisCardSeat) {
		this.nowDisCardSeat = nowDisCardSeat;
		dbParamMap.put("nowDisCardSeat", nowDisCardSeat);
	}

	public abstract void setConfig(int index, int val);

	/**
	 * 配置cofnig
	 *
	 * @param index
	 * @return
	 */
	public int getConifg(int index) {
		if (config == null || config.size() <= index) {
			return 0;
		}
		return config.get(index);
	}

	public List<Integer> getConfig() {
		return config;
	}

	/**
	 * 发送结算消息
	 */
	public abstract void sendAccountsMsg();

	/**
	 * 牌桌最大人数
	 *
	 * @return
	 */
	public abstract int getMaxPlayerCount();

	/**
	 * 保存Log日志
	 *
	 * @return
	 */
	public abstract void saveLog(boolean over, long winId, Object res);

    /**
     * 保存Log日志之后调用
     *
     * @param logId
     * @param over
     * @param playNo
     */
    public final void saveTableRecord(long logId, boolean over, int playNo) {
        if (NumberUtils.isDigits(serverKey) || isGroupRoom()) {
            try {
                String keyId = serverKey.contains("_") ? serverKey.split("_")[1] : serverKey;
                GroupTable groupTable;
                if (this.groupTable != null && keyId.equals(String.valueOf(this.groupTable.getKeyId()))) {
                    groupTable = this.groupTable;
                } else {
                    groupTable = GroupDao.getInstance().loadGroupTableByKeyId(keyId);
                }

                if (groupTable != null) {
                    TableRecord tableRecord = new TableRecord();
                    tableRecord.setCreatedTime(new Date());
                    tableRecord.setGroupId(groupTable.getGroupId());
                    tableRecord.setInitMsg("");
                    tableRecord.setLogId(String.valueOf(logId));
                    tableRecord.setModeMsg(groupTable.getTableMsg());
                    tableRecord.setPlayNo(playNo);
                    tableRecord.setRecordType(over ? 1 : 0);
                    tableRecord.setResultMsg(over ? saveRecordResultMsg() : "");
                    tableRecord.setTableId(groupTable.getTableId());
                    tableRecord.setTableNo(groupTable.getKeyId());

                    GroupDao.getInstance().createTableRecord(tableRecord);
                }
            } catch (Exception e) {
                LogUtil.errorLog.error("saveTableRecord|error|" + getId(), e);
            }
        } else if (isGoldRoom()) {
            Long dataDate = Long.valueOf(new SimpleDateFormat("yyyyMMdd").format(new Date()));
            try {
                List<LogGroupGoldCommission> logList = new ArrayList<>();
                for (Player player : getPlayerMap().values()) {
                    if (player.getWinGold() != 0) {
                        GoldRoomDao.getInstance().updateGoldRoomUser(goldRoomId, player.getUserId(), player.getWinGold(), String.valueOf(logId));

                        LogGroupGoldCommission log = new LogGroupGoldCommission(dataDate, getGoldRoomUser(player.getUserId()).getGroupId(), player.getUserId());
                        log.setSelfWin(player.getWinGold());
                        logList.add(log);
                    }
                }
                LogDao.getInstance().saveLogGroupGoldCommission(logList);
            } catch (Exception e) {
                LogUtil.errorLog.error("saveTableRecord|error|" + getId(), e);
            }

            try {
                GoldRoomTableRecord tableRecord = new GoldRoomTableRecord();
                tableRecord.setGoldRoomId(goldRoomId);
                tableRecord.setCreatedTime(new Date());
                tableRecord.setLogId(logId);
                tableRecord.setPlayNo(playNo);
                tableRecord.setRecordType(over ? 1 : 0);
                tableRecord.setResultMsg(over ? saveRecordResultMsg() : "");
                tableRecord.setTableId(getId());
                GoldRoomDao.getInstance().insertGoldRoomTableRecord(tableRecord);
            } catch (Exception e) {
                LogUtil.errorLog.error("saveTableRecord|error|" + getId(), e);
            }
        }else if(isSoloRoom()){
            try{
                long winnerId = 0;
                long loserId = 0;
                for(Player player : getSeatMap().values()){
                    if(player.isSoloWinner()){
                        winnerId = player.getUserId();
                    }else{
                        loserId = player.getUserId();
                    }
                }
                if (winnerId > 0) {
                    SoloRoomTableRecord tableRecord = new SoloRoomTableRecord();
                    tableRecord.setWinnerId(winnerId);
                    tableRecord.setLoserId(loserId);
                    tableRecord.setGold(soloRoomValue);
                    tableRecord.setPlayType(playType);
                    tableRecord.setTableId(getId());
                    tableRecord.setLogId(logId);
                    tableRecord.setCreatedTime(new Date());
                    tableRecord.setTableId(getId());
                    GoldRoomDao.getInstance().insertSoloRoomTableRecord(tableRecord);
                }
            }catch (Exception e){
                LogUtil.errorLog.error("saveTableRecord|error|" + getId(), e);
            }
        }
    }

    public String saveRecordResultMsg() {
        JSONObject json = new JSONObject();
        json.put("createTime", getCreateTime() == null ? "" : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(getCreateTime()));

        if (getSpecialDiss() == 1) {
            json.put("dissState", "1");//群主解散
        } else {
            if (answerDissMap != null && !answerDissMap.isEmpty()) {
                json.put("dissState", "2");//玩家申请解散
                StringBuilder str = new StringBuilder();
                for (Entry<Integer, Integer> entry : answerDissMap.entrySet()) {
                    Player player0 = getSeatMap().get(entry.getKey());
                    if (player0 != null) {
                        str.append(player0.getName()).append(",");
                    }
                }
                if (str.length() > 0) {
                    str.deleteCharAt(str.length() - 1);
                }
                json.put("dissPlayer", str.toString());
            } else {
            	if(autoPlayDiss) {
            		json.put("dissState", "3");//托管解散
            	}else {
            		json.put("dissState", "0");//正常打完
            	}
            }
        }
        if (isDissByCreditLimit) {
            json.put("dissState", "4");
            json.put("dissPlayer", creditLimitPlayerNames);
            if (creditMode == 1) {
                json.put("creditDissLimit", creditDissLimit);
            }
            if (gtgMode == 1) {
                json.put("creditDissLimit", gtgDissLimit);
            }
        }
        return json.toString();
    }

    /**
     * 大结算后调用
     *
     * @param player
     */
    public final void updatePlayerScore(Player player, int isWinner) {
        int userGroup = 0;
        long winLoseCredit = player.getWinLoseCredit();
        long commissionCredit = player.getCommissionCredit();
        if (isGroupTableGoldRoom()) {
            winLoseCredit = player.getWinGold();
            commissionCredit = 0;
        }
        if (NumberUtils.isDigits(serverKey)) {
            try {
                GroupDao.getInstance().updateTableUserScore(player.loadScore(), player.getUserId(), Long.parseLong(serverKey), isWinner, winLoseCredit, commissionCredit, userGroup, loadGroupIdLong());
            } catch (Exception e) {
                LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
            }
        } else if (isGroupRoom() && serverKey.contains("_")) {
            try {
                GroupDao.getInstance().updateTableUserScore(player.loadScore(), player.getUserId(), Long.parseLong(serverKey.split("_")[1]), isWinner, winLoseCredit, commissionCredit, userGroup, loadGroupIdLong());
            } catch (Exception e) {
                LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
            }
        }
    }

    /**
     * 创建一个牌桌
     *
     * @param player
     * @param play
     * @param bureauCount
     * @param objects
     * @throws Exception
     */
    public abstract void createTable(Player player, int play, int bureauCount, Object... objects) throws Exception;

    /**
     * 创建一个牌桌
     *
     * @param player
     * @param play
     * @param bureauCount
     * @param objects
     * @throws Exception
     */
    public abstract void createTable(Player player, int play, int bureauCount, List<Integer> params, List<String> strParams, Object... objects) throws Exception;

    /**
     * 创建一个牌桌
     *
     * @param player
     * @param play
     * @param bureauCount
     * @param params
     * @param strParams
     * @param saveDb
     * @throws Exception
     */
    public boolean createSimpleTable(Player player, int play, int bureauCount, List<Integer> params, List<String> strParams, boolean saveDb) throws Exception {
        return false;
    }

    public boolean createTable(CreateTableInfo info) throws Exception {
        return false;
    }


    public boolean createBaiRenTable(Player player, int play, List<Integer> params, List<String> strParams) throws Exception {
        return false;
    }

    public boolean saveSimpleTable() throws Exception {
        return false;
    }

    public abstract int getWanFa();

    public boolean isTest() {
        return "1".equals(ResourcesConfigsUtil.loadServerPropertyValue("test"));
    }

    public abstract void checkReconnect(Player player);

    public boolean isCompetition() {
        return isCompetition > 0;
    }

    /**
     * 托管
     */
    public synchronized void checkCompetitionPlay() {
        checkAutoPlay();
    }

    public synchronized void checkRobotPlay(){}

    public abstract void checkAutoPlay();

    public abstract Class<? extends Player> getPlayerClass();

    public String buildDissInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append(DataMapUtil.explode(answerDissMap));
        sb.append("_");
        sb.append(sendDissTime);
        return sb.toString();
    }

    /**
     * 重连和加入牌桌时检查玩家是否合法
     *
     * @param player
     * @return
     */
    public boolean checkPlayer(Player player) {
        Map<Long, Player> playerMap = new HashMap<>(getPlayerMap());
        if (player != null && !playerMap.containsKey(player.getUserId()) && !roomPlayerMap.containsKey(player.getUserId())) {
            // 房间内没有找到玩家
            if (player.getPlayingTableId() == id) {
                // 玩家身上标记在这个房间
                LogUtil.msgLog.info("playingTableId0|9|" + player.getUserId() + "|" + player.getEnterServer() + "|" + player.getPlayingTableId());
                player.setPlayingTableId(0);
                player.saveBaseInfo();
            }
            return false;
        }

        for (Player p : playerMap.values()) {
            if (!p.getClass().getName().equals(getPlayerClass().getName())) {
                if (p.isRobot() && p.getPlayingTableId() == 0) {
                    LogUtil.msgLog.info("BaseTable|dissReason|checkPlayer|1|" + getId() + "|" + getPlayBureau() + "|" + p.getUserId());
                    diss();
                    return false;
                }
                if (playBureau == 1 && !isDaikaiTable()) {
                    // 还没有开始打牌
                    LogUtil.msgLog.info("BaseTable|dissReason|checkPlayer|2|" + getId() + "|" + getPlayBureau() + "|" + p.getUserId());
                    diss();
                }
                LogUtil.e("table checkplayer err-->" + playType + " not majiangplayer:" + p.getUserId());
                LogUtil.e("checkPlayer|error|" + getId() + "|" + p.getUserId() + "|" + playType + "|" + p.getClass().getName() + "|" + getPlayerClass().getName());
                return false;
            }
        }

        return true;
    }

    public int getAnswerDissCount() {
        return answerDissMap.size();
    }

    /**
     * 消耗房卡
     *
     * @return
     */
    public boolean consumeCards() {
        return SharedConstants.consumecards && checkPay;
    }

	public List<List<Integer>> getZp() {
		return zp;
	}

	public void setZp(List<List<Integer>> zp) {
		this.zp = zp;
	}

	public boolean setZpMap(long zpUser, int zpValue) {
		zpMap.put(zpUser, zpValue);
		return true;
	}

	public void sendZpLeftCards(long userId) {

	}

	/**
	 * 出牌离自己是最近的
	 */
	public int getNearSeat(int nowSeat, List<Integer> seatList) {
		if (seatList.contains(nowSeat)) {
			// 出牌离自己是最近的
			return nowSeat;
		}
		for (int i = 0; i < getPlayerCount() - 1; i++) {
			int seat = calcNextSeat(nowSeat);
			if (seatList.contains(seat)) {
				return seat;
			}
			nowSeat = seat;
		}
		return 0;
	}

	/**
	 * 计算seat右边的座位
	 *
	 * @param seat
	 * @return
	 */
	public int calcNextSeat(int seat) {
		return seat + 1 > getMaxPlayerCount() ? 1 : seat + 1;
	}

	public int getNextSeat(int seat) {
		List<Integer> seatList = new ArrayList<>(getSeatMap().keySet());
		if (seatList.isEmpty()) {
			return 0;
		}
		Collections.sort(seatList);
		int findIndex = seatList.indexOf(seat);
		if (findIndex != -1) {
			int index = findIndex + 1 > seatList.size() - 1 ? 0 : findIndex + 1;
			return seatList.get(index);
		} else {
			return 0;
		}

	}

	public int getNextSeat(int seat, List<Integer> seatList) {
		if (seatList.isEmpty()) {
			return 0;
		}
		Collections.sort(seatList);
		int findIndex = seatList.indexOf(seat);
		if (findIndex != -1) {
			int index = findIndex + 1 > seatList.size() - 1 ? 0 : findIndex + 1;
			return seatList.get(index);
		} else {
			return 0;
		}

	}

    /**
     * 检查是否能解散房间
     */
    public synchronized boolean checkRoomDiss() {
        if (isTiqianDiss()) {
            return false;
        }
        long nowTime = TimeUtil.currentTimeMillis();
        Player applyPlayer = null;
        int applySeat = 0;
        List<Integer> tempList = null;
        if (sendDissTime > 0 && nowTime - sendDissTime >= getDissTimeout() && !isCompetitionRoom()) {
            tempList = new ArrayList<>(answerDissMap.keySet());
            if (tempList == null || tempList.size() <= 0) {
                return false;
            }
            applySeat = tempList.get(0);
            applyPlayer = getSeatMap().get(applySeat);
            if (applyPlayer == null) {
                return false;
            }

            // 解散前发送解散消息
            int groupId = 0;
            if (isGroupRoom()) {
                groupId = Integer.parseInt(loadGroupId());
            }
            ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_disstable,String.valueOf(calcTableType()),String.valueOf(calcTableType()), playType, groupId, playedBureau);
            for (Player temp : getPlayerMap().values()) {
                if (temp != null) {
                    temp.writeSocket(com.build());
                }
            }
            LogUtil.monitor_i("table diss:" + getId() + " apply player:" + applyPlayer.getUserId() + " play:" + getPlayType() + " pb" + getPlayBureau() + " timeout 5 minute");
            try {
                if (isDissSendAccountsMsg()) {
                    sendAccountsMsg();
                    calcOver3();
                }
            } catch (Throwable e) {
                LogUtil.errorLog.error("tableId=" + id + ",total calc Exception:" + e.getMessage(), e);
                GeneratedMessage errorMsg = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_err, "结算异常，房间" + id + "被解散").build();
                for (Player player : getPlayerMap().values()) {
                    player.writeSocket(errorMsg);
                }
            } finally {
                setTiqianDiss(true);
                LogUtil.msgLog.info("BaseTable|dissReason|checkRoomDiss|1|" + getId() + "|" + getPlayBureau());
                return diss() > 0;
            }
        }
        return false;
    }

    // 是否为代开房间
    public boolean isDaikaiTable() {
        return daikaiTableId > 0 && GoldRoomUtil.isNotGoldRoom(daikaiTableId);
    }

	/**
	 * 是否是无房号金币场房间
	 *
	 * @return
	 */
	public boolean isGoldRoom() {
		return this.tableType == TABLE_TYPE_GOLD ;
	}

	/**
	 * 是否是无房号比赛场房间
	 *
	 * @return
	 */
	public boolean isCompetitionRoom() {
		return this.tableType == TABLE_TYPE_COMPETITION_PLAYING ;
	}

	/**
	 *  是否是solo场
	 * @return
	 */
	public boolean isSoloRoom() {
		return this.tableType == TABLE_TYPE_SOLO && getMaxPlayerCount() == 2;
	}

	public long getCreateTableId(long userId, int playType) {
		long tableId;
		if (groupTable != null && groupTable.getTableId() != null && groupTable.getTableId().intValue() > 0) {
			tableId = groupTable.getTableId().intValue();
		} else {
			int tableType;
			if (groupTableConfig != null || groupTable != null || allowGroupMember > 0 || isGroupRoom()) {
				tableType = 1;
			} else {
				tableType = 0;
			}
			tableId = TableManager.getInstance().generateId(userId, playType, tableType);
		}

		return tableId;
	}

	public long getDaikaiTableId() {
		return daikaiTableId;
	}

	public void setDaikaiTableId(long daikaiTableId) {
		this.daikaiTableId = daikaiTableId;
		dbParamMap.put("daikaiTableId", daikaiTableId);
	}

    // 代开房间,没开局是不能解散的
    public synchronized boolean canDissTable(Player player) {
        if (isDaikaiTable() && state == table_state.ready && this.playBureau < 2) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_204));
            return false;
        }
        if (isGroupRoom()) {
            try {
                String[] strs = serverKey.split("_");
//                int groupId = Integer.parseInt(strs.length >= 2 ? strs[0].substring(5) : serverKey.substring(5));
                String groupTableKey = strs.length >= 2 ? strs[1] : null;

                GroupTable gt = groupTableKey == null ? GroupDao.getInstance().loadGroupTable(player.getUserId(), id) : GroupDao.getInstance().loadGroupTableByKeyId(groupTableKey);
                if (gt != null) {
                    String[] tempMsgs = new JsonWrapper(gt.getTableMsg()).getString("strs").split(";")[0].split("_");
//                    String payType = tempMsgs[0];
                    String userId = tempMsgs[1];
                    if (userId.equals(String.valueOf(player.getUserId())) && gt.getCurrentState().equals("0") && playedBureau <= 0 && (gt.getCurrentCount().intValue() <= 0 || (getPlayerMap().containsKey(player.getUserId())))) {
                        if(isPlaying()){
                            // 开局后，不能直接解散，必须走解散流程
                            return true;
                        }
                        // 未开局，族长直接解散
                        ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_disstable,String.valueOf(calcTableType()), getPlayType(), gt.getGroupId().intValue(), playedBureau);
                        GeneratedMessage msg = com.build();
                        for (Player player0 : getSeatMap().values()) {
                            player0.writeSocket(msg);
                        }
                        for (Player player0 : getRoomPlayerMap().values()) {
                            player0.writeSocket(msg);
                        }
                        LogUtil.msgLog.info("BaseTable|dissReason|canDissTable|1|" + getId() + "|" + getPlayBureau() + "|" + player.getUserId());
                        diss();
                    } else if ((gt.isPlaying() || gt.isOver() || isPlaying()) && getPlayerMap().containsKey(player.getUserId())) {
                        return true;
                    } else {
                        player.writeErrMsg(LangHelp.getMsg(LangMsg.code_43));
                    }
                }
            } catch (Exception e) {
                LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
            }
            return false;
        }

		return true;
	}

	// build代开房间玩家的信息
	public String buildDaikaiPlayerInfo() {
		StringBuilder userInfo = new StringBuilder();
		for (Player player : getSeatMap().values()) {
			// userInfo.append(player.getUserId());
			// userInfo.append(",");
			userInfo.append(player.getName());
			userInfo.append(",");
			userInfo.append(player.getSex());
			userInfo.append(";");
		}

		return userInfo.toString();
	}

	// 修改代开房间的信息
	public void updateDaikaiTableInfo() {
		if (isDaikaiTable()) {
			HashMap<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("tableId", daikaiTableId);
			paramMap.put("createTime", new Date());
			paramMap.put("state", 1);
			paramMap.put("createFlag", 1);
			paramMap.put("playerInfo", buildDaikaiPlayerInfo());
			paramMap.put("extend", GameServerConfig.SERVER_ID);
			TableDao.getInstance().updateDaikaiTable(paramMap);
		}
	}

	// 修改代开房间玩家的信息
	public int updateDaikaiTablePlayer() {
		if (isDaikaiTable()) {
			HashMap<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("tableId", daikaiTableId);
			paramMap.put("playerInfo", buildDaikaiPlayerInfo());
			return TableDao.getInstance().updateDaikaiTable(paramMap);
		}
		return 0;
	}

    public void afterMakeOverMasterId(Player player) {

    }

    // 转让代开房间房主
    public boolean makeOverMasterId(Player player) {
        if (getPlayerMap().size() < 1) {
            setMasterId(0);
            return false;
        }

        List<Long> userIds = new ArrayList<>();
        for (Player temp : getPlayerMap().values()) {
            if (temp.getUserId() == player.getUserId()) {
                continue;
            }
            userIds.add(temp.getUserId());
        }
        if (userIds.size() > 0) {
            Long masterId2 = userIds.get(RandomUtils.nextInt(userIds.size()));
            setMasterId(masterId2);
            afterMakeOverMasterId(getPlayerMap().get(masterId2));
            return true;
        } else {
            setMasterId(0);
        }
        return false;
    }

    // 解散代开房间
    public int dissDaikaiTable() {
        if (!isDaikaiTable()) {
            return 0;
        }

        boolean needReturn = true;
        if (getPlayBureau() > 1) {
            needReturn = false;
        } else {
            if (isTiqianDiss()) {
                needReturn = false;
            }
        }
//		boolean needReturn = false;

        return TableDao.getInstance().dissDaikaiTable(getId(), needReturn);
    }

    public boolean isTiqianDiss() {
        return tiqianDiss;
    }

    public void setTiqianDiss(boolean tiqianDiss) {
        this.tiqianDiss = tiqianDiss;
    }

	public boolean isCanReady() {
		return true;
	}

	public long getSendDissTime() {
		return sendDissTime;
	}

	public boolean  isNeedFromOverPop(){
		return true;
	}

    /**
     * 发送玩家状态信息
     */
    public void sendPlayerStatusMsg() {
        for (Entry<Long, Player> kv : getPlayerMap().entrySet()) {
            broadIsOnlineMsg(kv.getValue(), kv.getValue().getIsOnline() == 0 ? SharedConstants.table_offline : SharedConstants.table_online);
        }
    }

    public String getPlayerNameString() {
        StringBuilder names = new StringBuilder();
        Player master;
        if (masterId > 0 && (master = getPlayerMap().get(masterId)) != null) {
            names.append(",").append(master.getName());
            for (Player player : getPlayerMap().values()) {
                if (player.getUserId() != masterId) {
                    names.append(",").append(player.getName());
                }
            }
        } else {
            for (Player player : getPlayerMap().values()) {
                names.append(",").append(player.getName());
            }
        }

        if (names.length() > 0) {
            return names.substring(1);
        } else {
            return "";
        }
    }

    /**
     * 获得房间解散的状态（2：正常结束解散 3：未开局被解散 4：中途解散 5：托管提前解散）
     */
    public String getDissCurrentState() {
        String currentState;
        if (playedBureau == 0) {
            currentState = "3";
        } else if (isCommonOver()) {
            if (autoPlayDiss) {
                currentState = "5";
            } else {
                currentState = "2";
            }
        } else {
            currentState = "4";
        }
        return currentState;
    }

    /**
     * 房间是否正常打完结束
     */
    public boolean isCommonOver() {
        return playedBureau == totalBureau || !tiqianDiss;
    }

    /**
     * 房间是否托管提前结束
     */
    public boolean isAutoOver() {
        return playedBureau < totalBureau && autoPlay && !tiqianDiss;
    }


    /**
     * 房间是否正常打完结束
     */
    public boolean isNormalOver() {
        return playedBureau == totalBureau && !tiqianDiss;
    }

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public boolean joinWaitNext() {
		return true;
	}

	/**
	 * 广播玩家在线状态消息
	 */
	public void broadOnlineStateMsg() {
		for (Player player1 : getPlayerMap().values()) {
			if (player1.getIsOnline() == 0) {
				broadIsOnlineMsg(player1, SharedConstants.table_offline);
			} else {
				broadIsOnlineMsg(player1, SharedConstants.table_online);
			}
		}
	}

	/**
	 * 所有人可以开始游戏
	 */
	public boolean anyOneStart() {
		return "1".equals(ResourcesConfigsUtil.loadServerPropertyValue("anyOneStart", ""));
	}


	/**
	 * 是否可以退出
	 *
	 * @param player
	 * @return
	 */
	public boolean canQuit(Player player) {
		if (state == table_state.play || playedBureau > 0 || isMatchRoom() || isGoldRoom()) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 获取牌桌正常结束条件的最终值
	 *
	 * @return
	 */
	public int loadOverValue() {
		return totalBureau;
	}

	/**
	 * 获取牌桌正常结束条件的当前值
	 *
	 * @return
	 */
	public int loadOverCurrentValue() {
		return playedBureau;
	}

	/**
	 * 签名key
	 *
	 * @return
	 */
	public String loadSignKey() {
		return LoginUtil.DEFAULT_KEY;
	}

	/**
	 * 加载玩法配置，子类必须重写
	 * todo
	 */
	public static void loadWanfaTables(Class<? extends BaseTable> cls) {
	}

//    public int getStartType(){
//        return 0;
//    }

	public int getCreditMode() {
		return creditMode;
	}

	public void setCreditMode(int creditMode) {
		this.creditMode = creditMode;
	}

	public long getCreditJoinLimit() {
		return creditJoinLimit;
	}

	public void setCreditJoinLimit(long creditJoinLimit) {
		this.creditJoinLimit = creditJoinLimit;
	}

	public long getCreditDissLimit() {
		return creditDissLimit;
	}

	public void setCreditDissLimit(long creditDissLimit) {
		this.creditDissLimit = creditDissLimit;
	}

	public long getCreditDifen() {
		return creditDifen;
	}

	public void setCreditDifen(long creditDifen) {
		this.creditDifen = creditDifen;
	}

	public long getCreditCommission() {
		return creditCommission;
	}

	public void setCreditCommission(int creditCommission) {
		this.creditCommission = creditCommission;
	}

	public int getCreditCommissionMode1() {
		return creditCommissionMode1;
	}

	public void setCreditCommissionMode1(int creditCommissionMode1) {
		this.creditCommissionMode1 = creditCommissionMode1;
	}

	public int getCreditCommissionMode2() {
		return creditCommissionMode2;
	}

	public void setCreditCommissionMode2(int creditCommissionMode2) {
		this.creditCommissionMode2 = creditCommissionMode2;
	}

	/**
	 * 是否是信用房间
	 *
	 * @return
	 */
	public boolean isCreditTable() {
		return isGroupRoom() && creditMode == 1;
	}

	/**
	 * 是否是信用房间
	 * ---根据玩法配置数据判断，各玩法自行实现
	 *
	 * @param params
	 * @return
	 */
	public boolean isCreditTable(List<Integer> params) {
		return false;
	}


	/**
	 * 更新玩家信用分
	 *
	 * @param groupId
	 * @param userId
	 * @param seat
	 * @param credit
	 * @return
	 */
	public int updateGroupCredit(String groupId, long userId, int seat, long credit) {
		int updateResult = 0;
		try {
			updateResult = GroupDao.getInstance().updateGroupCredit(groupId, userId, credit);
		} catch (Exception e) {
			LogUtil.errorLog.error("updateGroupCredit|error|2|" + groupId + "|" + getId() + "|" + userId + "|" + seat + "|" + credit, e);
		}
		if (updateResult == 0) {
			LogUtil.errorLog.error("updateGroupCredit|error|3|" + groupId + "|" + getId() + "|" + userId + "|" + seat + "|" + credit);
		} else {
			LogUtil.msgLog.info("updateGroupCredit|succ|" + groupId + "|" + getId() + "|" + userId + "|" + seat + "|" + credit);
		}
		return updateResult;
	}

	/**
	 * 更新玩家零钱包信用分
	 *
	 * @param groupId
	 * @param userId
	 * @param seat
	 * @param credit
	 * @return
	 */
	public int updateGroupCreditPurse(String groupId, long userId, int seat, long credit) {
		int updateResult = 0;
		try {
			updateResult = GroupDao.getInstance().updateGroupCreditPurse(groupId, userId, credit);
		} catch (Exception e) {
			LogUtil.errorLog.error("updateGroupCreditPurse|error|2|" + groupId + "|" + getId() + "|" + userId + "|" + seat + "|" + credit, e);
		}
		if (updateResult == 0) {
			LogUtil.errorLog.error("updateGroupCreditPurse|error|3|" + groupId + "|" + getId() + "|" + userId + "|" + seat + "|" + credit);
		} else {
			LogUtil.msgLog.info("updateGroupCreditPurse|succ|" + groupId + "|" + getId() + "|" + userId + "|" + seat + "|" + credit);
		}
		return updateResult;
	}


    /**
     * 计算佣金
     *
     * @param player
     * @param dyjCredit 大赢家
     */
    public void calcCommissionCredit(Player player, long dyjCredit) {
        player.setCommissionCredit(0);
		if(AAScoure==100){
			//2020年11月30日 AA赠送开局发牌已扣 就不扣了；
			return;
		}
        long credit = player.getWinLoseCredit();
        long preCredit = credit;
        if (credit <= 0) {
            return;
        }
        if (creditCommissionMode2 == 1 && credit < dyjCredit) {
            return;
        }
        int tmpCount = 0;
        if (this.dyjCount == 0) {
            for (Player p : getSeatMap().values()) {
                if (p.getWinLoseCredit() == dyjCredit) {
                    tmpCount++;
                }
            }
            this.dyjCount = tmpCount;
        }
        long commissionCredit = 0;
        isBaoDiCommission = false;
        if (credit <= creditCommissionLimit) {
            // 保底抽水
            long baoDiCredit = calcBaoDi(credit);
            if (baoDiCredit <= 0) {
                // 不抽
                return;
            }
            commissionCredit = credit > baoDiCredit ? baoDiCredit : credit;
            isBaoDiCommission = true;
        }else {
            //佣金
            if (creditCommissionMode1 == 1) {
                //固定数量佣金
                if (creditCommissionMode2 == 1) {
                    //大赢家
                    if (credit >= dyjCredit && dyjCredit > 0) {
                        if (credit >= creditCommission) {
                            commissionCredit = creditCommission;
                        } else {
                            commissionCredit = credit;
                        }
                    }
                } else {
                    //全部赢家
                    if (credit > 0) {
                        if (credit >= creditCommission) {
                            commissionCredit = creditCommission;
                        } else {
                            commissionCredit = credit;
                        }
                    }
                }
            } else {
                //按比例交佣金
                if (creditCommissionMode2 == 1) {
                    //大赢家
                    if (credit >= dyjCredit && dyjCredit > 0) {
                        long commission = credit * creditCommission / 100;
                        if (credit >= commission) {
                            commissionCredit = commission;
                        } else {
                            commissionCredit = credit;
                        }
                    }
                } else {
                    //全部赢家
                    if (credit > 0) {
                        long commission = credit * creditCommission / 100;
                        if (credit >= commission) {
                            commissionCredit = commission;
                        } else {
                            commissionCredit = credit;
                        }
                    }
                }
            }
        }
        if (preCredit == dyjCredit && dyjCount > 1) {
            commissionCredit = (long) Math.ceil((commissionCredit * 1d) / (this.dyjCount * 1d));
        }
        credit = credit > commissionCredit ? credit - commissionCredit : 0;
        player.setCommissionCredit(commissionCredit);
        player.setWinLoseCredit(credit);
    }

    /**
     * 开始下一局前检查是否可以解散
     * 发牌前检查玩家身上信用分是否满足最低信用分需求
     *
     * @return
     */
    public synchronized boolean checkCreditOnStartNext() {
        if (playBureau == 1) {
            return true;
        }
        if (!isCreditTable()) {
            return true;
        }
        try {
        	// 重新查询亲友圈用户数据
        	initGroupUser();
            String disPlayerNames = "";
            StringBuilder sb = new StringBuilder("checkCreditOnStartNext");
            sb.append("|").append(getId());
            sb.append("|").append(getPlayBureau());
            sb.append("|").append(creditDissLimit);
            sb.append("|");
            for (Player player : getSeatMap().values()) {
            	if(player.getWinLoseCredit() < 0){
					GroupUser gu = getGroupUser(player.getUserId());
					if (gu == null || gu.getCredit() + player.getWinLoseCredit() < creditDissLimit) {
						disPlayerNames += player.getName() + ",";
						sb.append(gu.getUserId()).append(",").append(gu.getCredit()).append(",").append(player.getWinLoseCredit()).append(";");
					}
				}
            }
            if (!"".equals(disPlayerNames)) {
                LogUtil.msgLog.info(sb.toString());
                isDissByCreditLimit = true;
                creditLimitPlayerNames = disPlayerNames;
                disPlayerNames = disPlayerNames.substring(0, disPlayerNames.length() - 1);
                for (Player player : getSeatMap().values()) {
                    player.writeErrMsg(LangMsg.code_65, disPlayerNames, MathUtil.formatCredit(creditDissLimit));
                }
                for (Player player : getRoomPlayerMap().values()) {
                    player.writeErrMsg(LangMsg.code_65, disPlayerNames, MathUtil.formatCredit(creditDissLimit));
                }
                try {
                    sendAccountsMsg();
                } catch (Throwable e) {
                    LogUtil.errorLog.error("tableId=" + id + ",total calc Exception:" + e.getMessage(), e);
                    GeneratedMessage errorMsg = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_err, "结算异常，房间" + id + "被解散").build();
                    for (Player player : getPlayerMap().values()) {
                        player.writeSocket(errorMsg);
                    }
                }
                setSpecialDiss(1);
                setTiqianDiss(true);
                calcOver3();
                LogUtil.msgLog.info("BaseTable|dissReason|checkCreditOnStartNext|1|" + getId() + "|" + getPlayBureau() + "|" + disPlayerNames);
                diss();
                return false;
            }
        } catch (Exception e) {
            LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
        }
        return true;
    }

	/**
	 * 检查洗牌之后信用分是否够下局游戏
	 */
	public synchronized boolean checkXipaiCreditOnStartNext(Player player) {
		if (!isCreditTable()) {
			return false;
		}
		try {
			String groupIdStr = loadGroupId();
			GroupUser gu = GroupDao.getInstance().loadGroupUserForceMaster(player.getUserId(), groupIdStr);
			if (gu == null || gu.getCredit() + player.getWinLoseCredit() - xipaiScoure < creditDissLimit) {
				return false;
			}
			return true;
		} catch (Exception e) {
			LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
		}
		return false;
	}


    /**
     * 计算出现负分的情况：以输家可以输的总分，分给赢家（从赢分最大的开始，依次来分发）
     * 各子类按实现业务需求重写
     */
    public void calcNegativeCredit() {
        if (!isCreditTable()) {
            return;
        }
        if(canNegativeCredit()){
            return;
        }
        initGroupUser();
        calcWinCreditLimit();
        //计算负分
        int totalHave = 0;
        List<Player> winList = new ArrayList<>();
        for (Player player : getSeatMap().values()) {
            GroupUser gu = getGroupUser(player.getUserId());
            long haveCredit = gu.getCredit();
            if (player.getWinLoseCredit() < 0) {
                if (haveCredit <= 0) {
                    totalHave += 0;
                    player.setWinLoseCredit(0);
                } else if (haveCredit + player.getWinLoseCredit() < 0) {
                    totalHave += haveCredit;
                    player.setWinLoseCredit(-haveCredit);
                } else {
                    totalHave += -player.getWinLoseCredit();
                }
            } else {
                winList.add(player);
            }
        }

        if (winList.size() == 0) {
            return;
        }
        // 赢分从大到小
        Collections.sort(winList, new Comparator<Player>() {
            @Override
            public int compare(Player o1, Player o2) {
                if(o2.getWinLoseCredit() > o1.getWinLoseCredit()){
                    return 1;
                }else if(o2.getWinLoseCredit() == o1.getWinLoseCredit()){
                    return 0;
                }else{
                    return -1;
                }
            }
        });

        for (Player player : winList) {
            if (player.getWinLoseCredit() < totalHave) {
                totalHave -= player.getWinLoseCredit();
            } else {
                player.setWinLoseCredit(totalHave);
                totalHave = 0;
            }
        }
    }

    /**
     * 比赛房赢分不能超过玩家本身进房间所带的比赛分
     */
    public void calcWinCreditLimit() {
        if (!isCreditTable()) {
            return;
        }
        int totalWin = 0;// 赢分的玩家总分
        int totalLose = 0;// 输分玩家的总分
        int losePlayerCount = 0; // 输分玩家数
        long maxLoseCredit = 0;  // 最大输分数

        List<Player> loseList = new ArrayList<>();
        for (Player player : getSeatMap().values()) {
            if (player.getWinLoseCredit() > 0) {
                GroupUser gu = getGroupUser(player.getUserId());
                long haveCredit = gu.getCredit();
                if (player.getWinLoseCredit() > haveCredit) {
                    // 比赛房赢分不能超过玩家本身进房间所带的比赛分
                    player.setWinLoseCredit(haveCredit);
                }
                totalWin += player.getWinLoseCredit();
            } else {
                totalLose += player.getWinLoseCredit();
                losePlayerCount++;
                maxLoseCredit = player.getWinLoseCredit() < maxLoseCredit ? player.getWinLoseCredit() : maxLoseCredit;
                loseList.add(player);
            }
        }

        //赢的总分与输的总分不一至时 退还输分玩家多输的分数
        if (Math.abs(totalLose) > totalWin) {
            // 输分从小到大
            Collections.sort(loseList, new Comparator<Player>() {
                @Override
                public int compare(Player o1, Player o2) {
                    if(o2.getWinLoseCredit() > o1.getWinLoseCredit()){
                        return 1;
                    }else if(o2.getWinLoseCredit() == o1.getWinLoseCredit()){
                        return 0;
                    }else{
                        return -1;
                    }
                }
            });

            int credit = totalWin / losePlayerCount;
            int leftCredit = totalWin % losePlayerCount;
            // 即若一个输家就都是退给他，如果是两个输家就相当于用赢分平均分摊给两个输家，前提为输家本身输的分>均摊分，若小于则只输本身输分，其他都退给另外一个输家
            for (Player player : loseList) {
                if (Math.abs(player.getWinLoseCredit()) < credit) {
                    totalWin -= Math.abs(player.getWinLoseCredit());
                    losePlayerCount--;
                    credit = totalWin / losePlayerCount;
                    leftCredit = totalWin % losePlayerCount;
                } else {
                    player.setWinLoseCredit(-credit);
                }
            }
            if (leftCredit > 0) {
                Player maxLoser = loseList.get(loseList.size() - 1);
                maxLoser.setWinLoseCredit(maxLoser.getWinLoseCredit() - leftCredit);
            }
        }
    }

    public boolean isAutoPlay() {
        return autoPlay;
    }


    public long saveUserGroupPlaylog() {
        if(!needSaveUserGroupPlayLog()){
            return 0;
        }
        UserGroupPlaylog userGroupLog = new UserGroupPlaylog();
        userGroupLog.setTableid(id);
        userGroupLog.setUserid(creatorId);
        userGroupLog.setCount(playBureau);
        String players = "";
        String score = "";
        String diFenScore = "";
        for (Player player : getSeatMap().values()) {
            players += player.getUserId() + ",";
            score += player.getTotalPoint() + ",";
            diFenScore += player.getTotalPoint() + ",";
        }
        userGroupLog.setPlayers(players.length() > 0 ? players.substring(0, players.length() - 1) : "");
        userGroupLog.setScore(score.length() > 0 ? score.substring(0, score.length() - 1) : "");
        userGroupLog.setDiFenScore(diFenScore.length() > 0 ? diFenScore.substring(0, diFenScore.length() - 1) : "");
        userGroupLog.setDiFen("");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        userGroupLog.setCreattime(sdf.format(createTime));
        userGroupLog.setOvertime(sdf.format(new Date()));
        userGroupLog.setPlayercount(getMaxPlayerCount());
        userGroupLog.setGroupid(Long.parseLong(loadGroupId()));
        userGroupLog.setGamename(getGameName());
        userGroupLog.setTotalCount(totalBureau);
        return TableLogDao.getInstance().saveGroupPlayLog(userGroupLog);
    }

    public String getGameName() {
        return "";
    }

    /**
     * 广播消息给所有人
     */
    public void broadMsgToAll(GeneratedMessage message) {
        broadMsg(message, 0);
        broadMsgRoomPlayer(message);
    }

    /**
     * 广播消息
     */
    public void broadMsg(GeneratedMessage message, long userId) {
        for (Player player : getPlayerMap().values()) {
            if (player.getIsOnline() == 0 || userId == player.getUserId()) {
                continue;
            }
            player.writeSocket(message);
        }
    }

    /**
     * 广播消息给旁观者
     */
    public void broadMsgRoomPlayer(GeneratedMessage message) {
        for (Player player : roomPlayerMap.values()) {
            if (player.getIsOnline() == 0) {
                continue;
            }
            player.writeSocket(message);
        }
    }

    /**
     * 默认加入房间不分配位置
     * @return
     */
    public boolean isJoinPlayerAllotSeat(){
        return false;
    }

    /**
     * 初始化信息房间配置
     * @param creditMsg
     */
    public void initCreditMsg(String creditMsg) {
        if (StringUtils.isBlank(creditMsg)) {
            return;
        }
        String[] params = creditMsg.split(",");
        if (params.length < 8) {
            return;
        }
        this.creditMode = StringUtil.getIntValue(params, 0, 0);
        this.creditJoinLimit = StringUtil.getIntValue(params, 1, 0);
        this.creditDissLimit = StringUtil.getIntValue(params, 2, 0);
        this.creditDifen = StringUtil.getIntValue(params, 3, 0);
        this.creditCommission = StringUtil.getIntValue(params, 4, 0);
        this.creditCommissionMode1 = StringUtil.getIntValue(params, 5, 1);
        this.creditCommissionMode2 = StringUtil.getIntValue(params, 6, 1);
        this.creditCommissionLimit = StringUtil.getIntValue(params, 7, 100);
        this.credit100 = StringUtil.getIntValue(params, 8, 0);
        this.creditCommissionBaoDi = StringUtil.getIntValue(params, 9, 0);
        String baoDiConfigStr = StringUtil.getValue(params, 10);
        if (StringUtils.isNotBlank(baoDiConfigStr)) {
            this.baoDiConfigStr = baoDiConfigStr;
            initBaoDiConfig();
        }
        if(params.length >=13){
			this.isXipai = StringUtil.getIntValue(params, 11, 0) == 1?true:false;
			this.xipaiScoure = StringUtil.getIntValue(params, 12, 0);
		}
		if(params.length>=14){
			this.AAScoure = StringUtil.getIntValue(params, 13, 0);// 100
		}else{
			this.AAScoure = 0;
		}
        initCredit100();
        changeExtend();
    }

    public void initBaoDiConfig() {
        if (StringUtils.isNotBlank(baoDiConfigStr)) {
            String splits1[] = baoDiConfigStr.split("#");
            for (String split1 : splits1) {
                BaoDiConfig con = BaoDiConfig.parseString(split1);
                if (con != null) {
                    this.baoDiConfigList.add(con);
                }
            }
            if (this.baoDiConfigList.size() > 0) {
                Collections.sort(baoDiConfigList, new Comparator<BaoDiConfig>() {
                    @Override
                    public int compare(BaoDiConfig o1, BaoDiConfig o2) {
                        if (o1.getStart() > o2.getStart()) {
                            return 1;
                        } else if (o1.getStart() == o2.getStart()) {
                            return 0;
                        } else {
                            return -1;
                        }
                    }
                });
            }
        }
    }

    public String buildBaoDiConfigString() {
        StringJoiner res = new StringJoiner("#");
        if (baoDiConfigList != null && baoDiConfigList.size() > 0) {
            for (BaoDiConfig config : baoDiConfigList) {
                res.add(BaoDiConfig.toDBString(config));
            }
        }
        return res.toString();
    }

    public void updateGroupTableDealCount(){
        if (isGroupRoom()) {
            int keyId = Integer.parseInt(loadGroupTableKeyId());
            GroupDao.getInstance().addGroupTableDealCount(keyId);
        }
    }

    public List<Integer> getIntParams() {
        return intParams;
    }

    public void setIntParams(List<Integer> intParams) {
        this.intParams = intParams;
    }

    public List<String> getStrParams() {
        return strParams;
    }

    public void setStrParams(List<String> strParams) {
        this.strParams = strParams;
    }

    public void setReplenishParams(Player player,List<Integer> intParams,List<String> strParams){

    }

    public synchronized void checkAutoQuit() {
        if(getPlayedBureau() > 0){
            return;
        }
        if (this.getState() != table_state.ready) {
            return;
        }
        if (!this.isGroupRoom()) {
            return;
        }
        if(autoQuitTimeOut == 0){
            return;
        }
        for (Player player : getSeatMap().values()) {
            if (player.getState() != player_state.entry) {
                continue;
            }
            if ((System.currentTimeMillis() - player.getJoinTime())/1000 < autoQuitTimeOut) {
                continue;
            }
            boolean quit = this.quitPlayer(player);
            if (quit) {
                this.onPlayerQuitSuccess(player);
                // 修改代开房间玩家的信息记录
                this.updateDaikaiTablePlayer();
                // 修改room表
                this.updateRoomPlayers();
                player.writeErrMsg(LangMsg.code_66);
            }
        }
    }

    public void calcDataStatisticsBjd() {
        if (!isGroupRoom()) {
            return;
        }
        String groupIdStr = loadGroupId();
        Long groupId = Long.valueOf(groupIdStr);
        //白金岛有效局数活动
        Long dataDate = Long.valueOf(new SimpleDateFormat("yyyyMMdd").format(new Date()));
        try {
            boolean bjdNewerActivity = "1".equals(ResourcesConfigsUtil.loadServerPropertyValue("switch_bjdNewerActivity", SharedConstants.SWITCH_DEFAULT_OFF));
            boolean needProcess = bjdNewerActivity && isCommonOver() && !SharedConstants.testPlayTypes.contains(playType);
            // 有效玩家人数
            int validCount = 0;
            for (Player player : getPlayerMap().values()) {
                GroupUser groupUser = getGroupUser(player.getUserId());
                if (groupUser == null) {
                    continue;
                }
                boolean isValid = false;
                long bindedGroupId = GroupDao.getInstance().loadIsNewBjdBindGroup(player.getUserId());
                if (bindedGroupId > 0) {
                    isValid = groupIdStr.equals(String.valueOf(bindedGroupId));
                } else {
                    DataStatistics data = DataStatisticsDao.getInstance().loadMaxWzjsOfUser(player.getUserId());
                    if (data == null || data.getDataValue() < 19) {
                        // 验证玩家是否是该俱乐部的新人
                        isValid = true;
                    } else if (data.getDataValue() >= 19 && groupIdStr.equals(data.getDataCode())) {
                        isValid = true;
                        if (data.getDataValue() == 19) {
                            // 将自己写成绑定用户
                            GroupDao.getInstance().bindIsNewBjd(groupIdStr, player.getUserId());
                        }
                    }
                }
                if (isValid) {
                    validCount++;
                    if (needProcess) {
                        DataStatistics dataStatistics = new DataStatistics(dataDate, groupIdStr, String.valueOf(player.getUserId()), "0", "wzjsCount", 1);
                        DataStatisticsDao.getInstance().saveOrUpdateDataStatisticsBjd(dataStatistics);
                    }

                    try {
                        GroupDao.getInstance().updateTableUserIsNewPlayer(player.getUserId(), Long.parseLong(serverKey.split("_")[1]), 1,groupId);
                    } catch (Exception e) {
                        LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
                    }
                }

            }
            if (needProcess) {
                // 牌局中新玩家数量大于等于总人数的一半时该牌局算成有效局数，即2人有1名新玩家，3人有2名新玩家，4人有2名新玩家
                boolean isValid = validCount * 1f / getMaxPlayerCount() * 1f >= 0.5f;
                if (isValid) {
                    DataStatistics dataStatistics = new DataStatistics(dataDate, groupIdStr, groupIdStr, "0", "jlbwzjsCount", 1);
                    DataStatisticsDao.getInstance().saveOrUpdateDataStatisticsBjd(dataStatistics);
                }
            }
        } catch (Exception e) {
            LogUtil.e("calcDataStatistics|bjd|error|" + groupIdStr + "|" + getId(), e);
        }
        int maxPoint = 0 ;
        for (Player player : getPlayerMap().values()) {
            if (player.loadScore() > 0 && player.loadScore() > maxPoint) {
                maxPoint = player.loadScore();
            }
        }

        saveLogGroupTable(groupId, dataDate, maxPoint);

        saveLogGroupCommission(groupId, dataDate, maxPoint);
    }

    public void saveLogGroupTable(long groupId, long dataDate, int maxPoint) {
        // 牌桌统计数据
        try {
            List<LogGroupTable> logList = new ArrayList<>();
            for (Player player : getPlayerMap().values()) {
                GroupUser groupUser = getGroupUser(player.getUserId());
                if (groupUser == null) {
                    continue;
                }
                LogGroupTable log = new LogGroupTable(dataDate, Long.valueOf(groupId), getPlayType(), getLogGroupTableBureau());
                boolean saveLog = false;
                if (getMaxPlayerCount() == 2) {
                    if (isCommonOver()) {
                        log.setPlayer2Count1(1);
                    } else {
                        log.setPlayer2Count2(1);
                    }
                    log.setPlayer2Count3(getPlayedBureau());
                    saveLog = true;
                } else if (getMaxPlayerCount() == 3) {
                    if (isCommonOver()) {
                        log.setPlayer3Count1(1);
                    } else {
                        log.setPlayer3Count2(1);
                    }
                    log.setPlayer3Count3(getPlayedBureau());
                    saveLog = true;
                } else if (getMaxPlayerCount() == 4) {
                    if (isCommonOver()) {
                        log.setPlayer4Count1(1);
                    } else {
                        log.setPlayer4Count2(1);
                    }
                    log.setPlayer4Count3(getPlayedBureau());
                    saveLog = true;
                }
                if (maxPoint > 0 && maxPoint == player.loadScore()) {
                    // 大赢家
                    log.setDyjCount(1);
                    saveLog = true;
                }

                if (saveLog) {
                    log.setUserGroup(Long.valueOf(groupUser.getUserGroup()));
                    log.setUserId(player.getUserId());
                    logList.add(log);
                }
            }
            if(logList.size() > 0) {
                LogDao.getInstance().saveLogGroupTable(logList);
            }
        } catch (Exception e) {
            LogUtil.e("calcDataStatistics|logGroupTable|error|" + groupId + "|" + getId(), e);
        }
    }

    /**
     * 抽水统计
     *
     * @param groupId
     * @param dataDate
     * @param maxPoint
     */
    public void saveLogGroupCommission(long groupId, long dataDate, int maxPoint) {
        try {
            Set<Long> dyjSet = new HashSet<>();
            int needCards = loadPayConfig();
            if(needCards < 0){
                LogUtil.errorLog.error("getNeedCardError|" + getId() + "|" + groupId + "|" + payType + "|" + totalBureau + "|" + payType);
                needCards = 0;
            }
            int payCards = needCards * 100;
            int tmpCards = 0; // 除不尽的钻石数，给群主
            if (payType != 0) {
                payCards = needCards * 100 / getMaxPlayerCount();
                tmpCards = needCards * 100 % getMaxPlayerCount();
            }
            HashMap<Long, LogGroupCommission> map = new HashMap();
            List<LogGroupCommission> list = new ArrayList<>();
            boolean hasCommissionCredit = false;
            for (Player player : getPlayerMap().values()) {
                if (player.getCommissionCredit() > 0) {
                    hasCommissionCredit = true;
                }
                if (maxPoint > 0 && player.loadScore() == maxPoint) {
                    dyjSet.add(player.getUserId());
                }
            }
            for (Player player : getPlayerMap().values()) {
                GroupUser groupUser = getGroupUser(player.getUserId());

                LogGroupCommission tmp = new LogGroupCommission(dataDate, Long.valueOf(groupId), player.getUserId());

                LogGroupCommission logSelf = new LogGroupCommission(dataDate, Long.valueOf(groupId), player.getUserId());

                if (player.getCommissionCredit() > 0) {
                    tmp.setCommissionCredit(player.getCommissionCredit());
                    logSelf.setSelfCommissionCredit(player.getCommissionCredit());
					LogUtil.msg("saveLogGroupCommission|setSelfCommissionCredit|" + groupId + "|" + getId() + "|" + player.getUserId() + "|" + player.getCommissionCredit());
                }
                if (dyjSet.contains(player.getUserId())) {
                    tmp.setDyjCount(1);
                    logSelf.setSelfDyjCount(1);
                }
                tmp.setTotalPay(payCards);
                tmp.setZjsCount(1);

                logSelf.setSelfTotalPay(payCards);
                logSelf.setSelfZjsCount(1);
                if (hasCommissionCredit) {
                    logSelf.setSelfCommissionCount(1);
                }
                logSelf.setSelfWinCredit(player.getWinLoseCredit());
                list.add(logSelf);
                LogGroupCommission log = null;
                if (groupUser.getPromoterId1() > 0) {
                    log = tmp.clone();
                    log.setUserId(groupUser.getPromoterId1());
                    if (tmpCards > 0) {
                        log.setTotalPay(tmp.getTotalPay() + tmpCards);
                        log.setSelfTotalPay(tmp.getSelfTotalPay() + tmpCards);
                        tmpCards = 0;
                    }
                    list.add(log);
                } else {
                    continue;
                }
                if (groupUser.getPromoterId2() > 0) {
                    log = tmp.clone();
                    log.setUserId(groupUser.getPromoterId2());
                    list.add(log);
                } else {
                    continue;
                }
                if (groupUser.getPromoterId3() > 0) {
                    log = tmp.clone();
                    log.setUserId(groupUser.getPromoterId3());
                    list.add(log);
                } else {
                    continue;
                }
                if (groupUser.getPromoterId4() > 0) {
                    log = tmp.clone();
                    log.setUserId(groupUser.getPromoterId4());
                    list.add(log);
                } else {
                    continue;
                }
                if (groupUser.getPromoterId5() > 0) {
                    log = tmp.clone();
                    log.setUserId(groupUser.getPromoterId5());
                    list.add(log);
                } else {
                    continue;
                }
                if (groupUser.getPromoterId6() > 0) {
                    log = tmp.clone();
                    log.setUserId(groupUser.getPromoterId6());
                    list.add(log);
                } else {
                    continue;
                }
                if (groupUser.getPromoterId7() > 0) {
                    log = tmp.clone();
                    log.setUserId(groupUser.getPromoterId7());
                    list.add(log);
                } else {
                    continue;
                }
                if (groupUser.getPromoterId8() > 0) {
                    log = tmp.clone();
                    log.setUserId(groupUser.getPromoterId8());
                    list.add(log);
                } else {
                    continue;
                }
                if (groupUser.getPromoterId9() > 0) {
                    log = tmp.clone();
                    log.setUserId(groupUser.getPromoterId9());
                    list.add(log);
                } else {
                    continue;
                }
                if (groupUser.getPromoterId10() > 0) {
                    log = tmp.clone();
                    log.setUserId(groupUser.getPromoterId10());
                    list.add(log);
                } else {
                    continue;
                }
            }
            for (CreditCommission comm : commList) {
                LogGroupCommission log = new LogGroupCommission(dataDate, Long.valueOf(groupId), comm.getDestUserId());
                log.setCredit(comm.getCredit());
                if (comm.isAddCount()) {
                    log.setCommissionCount(1);
                }
                list.add(log);
            }

            for (LogGroupCommission tmp : list) {
                LogGroupCommission log = map.get(tmp.getUserId());
                if (log == null) {
                    map.put(tmp.getUserId(), tmp);
                } else {
                    log.addProp(tmp);
                }
            }
            LogDao.getInstance().saveLogGroupCommission(new ArrayList<>(map.values()));

        } catch (Exception e) {
            LogUtil.e("saveLogGroupCommission|error|" + groupId + "|" + getId(), e);
        }
    }

    public int getLogGroupTableBureau(){
        return totalBureau;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
        changeExtend();
    }

    public JsonWrapper  buildGeneralExtForPlaylog(){
        JsonWrapper json = new JsonWrapper("");
        json.putString("roomName",getRoomName());
        json.putString("intParams",StringUtil.implode(intParams, ","));
        json.putString("strParams",StringUtil.implode(strParams, ","));
        json.putString("creditMsg", StringUtil.implode(Arrays.asList(creditMode, creditJoinLimit, creditDissLimit, creditDifen, creditCommission, creditCommissionMode1, creditCommissionMode2, creditCommissionLimit, credit100,creditCommissionBaoDi,AAScoure), ","));
        json.putString("gtgMsg", StringUtil.implode(Arrays.asList(gtgMode, gtgJoinLimit, gtgDissLimit, gtgDifen), ","));
        return json;
    }

    /**
     * 初始化亲友圈的一些配置
     * @param groupId
     */
    public void initGroupConfig(long groupId) {
        try {
            GroupInfo group = GroupDao.getInstance().loadGroupInfo(groupId);
            if(group != null && StringUtils.isNotBlank(group.getExtMsg())){

                JSONObject json = JSONObject.parseObject(group.getExtMsg());
                String chatStr  = json.getString("chat");
                if(StringUtils.isNotBlank(chatStr)){
                    this.chatConfig = Integer.valueOf(chatStr);
                }
                String autoQuitStr  = json.getString("autoQuit");
                if(StringUtils.isNotBlank(autoQuitStr)){
                    this.autoQuitTimeOut = Integer.valueOf(autoQuitStr);
                }

                this.sameIpLimit = json.getIntValue(GroupConstants.groupExtKey_sameIpLimit) == 1;
                this.openGpsLimit = json.getIntValue(GroupConstants.groupExtKey_openGpsLimit) == 1;
                this.distanceLimit = json.getIntValue(GroupConstants.groupExtKey_distanceLimit) == 1;
                this.negativeCredit = json.getIntValue(GroupConstants.groupExtKey_negativeCredit) == 1;
            }
        } catch (Exception e) {
            LogUtil.errorLog.error("initGroupConfig|error|" + e.getMessage(), e);
        }
    }

    /**
     * 是否允许信用负分
     *
     * @return
     */
    public boolean canNegativeCredit() {
        return negativeCredit;
    }


    /**
     * 信用分结算前，加载玩家俱乐部的信息
     */
	public void initGroupUser() {
		if (guMap == null) {
			guMap = new HashMap<>();
		}
		List<Long> userIdList = new ArrayList<>();
		for (Player player : getSeatMap().values()) {
			userIdList.add(player.getUserId());
		}
		List<GroupUser> list = GroupDao.getInstance().loadGroupUserListForceMaster(userIdList, loadGroupIdLong());
		if (list != null && list.size() > 0) {
			for (GroupUser gu : list) {
				guMap.put(gu.getUserId(), gu);
			}
		}
	}

    public GroupUser getGroupUser(long userId){
        if(guMap == null || guMap.size() == 0){
            initGroupUser();
        }
        return guMap != null ? guMap.get(userId):null;
    }

    /**
     * 玩家离开房间，检查是否解散房间
     */
    public synchronized void checkDissOnQuit(Player player) {
        boolean diss = false;
        if (getRoomPlayerMap().size() == 0 && getSeatMap().size() == 0 && isGroupRoom()) {
            GroupTable gt = loadGroupTable();
            if (gt != null && gt.getIsPrivate() == 1) {
                // 私密房，没人就解散
                diss = true;
            } else {
                int count = GameUtil.loadGroupRoomEmptyTableCount(this);
                if (count > 1) {
                    diss = true;
                }
            }
        }
        if (GameUtil.isPlayWzq(playType)) {
            // 五子棋创房者离开房间后房间解散
            if (player != null && player.getUserId() == creatorId) {
                diss = true;
            }
        }

        if (isSoloRoom()) {
            // solo场，没人就解散
            if (getRoomPlayerMap().size() == 0 && getSeatMap().size() == 0) {
                diss = true;
            }
        }
        if (diss) {
            LogUtil.msgLog.info("BaseTable|dissReason|checkDissOnQuit|1|" + getId() + "|" + getPlayBureau());

            ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_disstable,String.valueOf(calcTableType()), playType, (int)loadGroupIdLong());
            GeneratedMessage msg = com.build();
            for (Player player0 : getSeatMap().values()) {
                player0.writeSocket(msg);
                player0.writeErrMsg(LangHelp.getMsg(LangMsg.code_8, getId()));
            }
            for (Player player0 : getRoomPlayerMap().values()) {
                player0.writeSocket(msg);
                player0.writeErrMsg(LangHelp.getMsg(LangMsg.code_8, getId()));
            }

            diss();
        }
    }

    /**
     * 是否已开局
     * @return
     */
    public boolean isPlaying() {
        return playedBureau > 0 || playBureau > 1 || state != table_state.ready;
    }


    public void autoReady(Player player){
        if (playBureau > 1) {
            if (player.getState() != player_state.entry && player.getState() != player_state.over) {
                return;
            }

            // 检查信用分
            if (!checkCanStartNext()) {
                return;
            }

            ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_state, player.getSeat(), SharedConstants.state_player_ready);
            GeneratedMessage playerReadyMsg = com.build();
            this.ready(player);
            for (Player seatPlayer : getSeatMap().values()) {
                if (seatPlayer.getUserId() == player.getUserId()) {
                    continue;
                }
                seatPlayer.writeSocket(playerReadyMsg);
            }
            for (Player roomPlayer : this.getRoomPlayerMap().values()) {
                roomPlayer.writeSocket(playerReadyMsg);
            }
            player.writeComMessage(WebSocketMsgType.res_code_isstartnext);
            if (this.isTest()) {
                for (Player tableplayer : getSeatMap().values()) {
                    if (tableplayer.isRobot()) {
                        this.ready(tableplayer);
                    }
                }
            }

            ready();
            checkDeal(player.getUserId());

            // 如果是最后一个点继续的人需要告诉前台
            boolean isLastStart = false;
            if (player.getState() == player_state.play) {
                isLastStart = true;
            }

            TableRes.CreateTableRes.Builder msg = buildCreateTableRes(player.getUserId(), true, isLastStart).toBuilder();
            if (getState() == SharedConstants.table_state.play) {
                //点下一局，触发发牌时设置为1，前端用来判断是否播放发牌动作
                msg.setFromOverPop(1);
            }
            player.writeSocket(msg.build());
            for (Player roomPlayer : getRoomPlayerMap().values()) {
                TableRes.CreateTableRes.Builder msg0 = buildCreateTableRes(roomPlayer.getUserId(), true, isLastStart).toBuilder();
                msg0.setFromOverPop(0);
                roomPlayer.writeSocket(msg0.build());
            }

            this.startNext();

            broadIsOnlineMsg(player, player.getIsOnline() == 0? SharedConstants.table_offline:SharedConstants.table_online);
        }
    }

    public boolean needSaveUserGroupPlayLog() {
        return ResourcesConfigsUtil.isSwitchOn(ResourcesConfigsUtil.KEY_SWITCH_SAVE_USER_GROUP_PLAYLOG);
    }



    /**
     * 各子类存在差异，自行实现该方法
     *
     * @param logTable
     */
    public void setDataForPlayLogTable(PlayLogTable logTable) {
        StringJoiner players = new StringJoiner(",");
        StringJoiner scores = new StringJoiner(",");
        for (int seat = 1, length = getSeatMap().size(); seat <= length; seat++) {
            Player player = getSeatMap().get(seat);
            players.add(String.valueOf(player.getUserId()));
            scores.add(String.valueOf(player.getTotalPoint()));
        }
        logTable.setPlayers(players.toString());
        logTable.setScores(scores.toString());
    }

    /**
     * 所有玩法固定显示内容：
     * 游戏名称、包厢名称、人数、局数（无局数不写）、托管规则（时间、单局/整局）
     * @return
     */
    public String getTableMsg(){
        return "";
    }

    /**
     * 保存牌桌结算分数
     *
     * @return
     */
    public int savePlayLogTable() {
    	if(true){
    		// 20201118关闭
    		return 0;
		}
        if (isCreditTable()) {
            return 0;
        }
        Date now = new Date();
        PlayLogTable logTable = new PlayLogTable();
        logTable.setTableId(id);
        logTable.setGroupId(0);
        logTable.setGroupMasterId(0);
        if (isGroupRoom()) {
            long groupId = loadGroupIdLong();
            logTable.setGroupId(groupId);
            try {
                GroupUser groupMaster = GroupDao.getInstance().loadGroupMaster(String.valueOf(groupId));
                if (groupMaster != null) {
                    logTable.setGroupMasterId(groupMaster.getUserId());
                }
            } catch (Exception e) {
                LogUtil.errorLog.error("loadGroupMaster|error|" + e.getMessage(), e);
            }
        }
        logTable.setPlayerCount(getPlayerCount());
        logTable.setCreatorId(creatorId);
        logTable.setFinishCount(playedBureau);
        setDataForPlayLogTable(logTable);
        logTable.setCreateTime(createTime);
        logTable.setOverTime(now);
        logTable.setTableMsg(getTableMsg());
        logTable.setTotalCount(totalBureau);
        long keyId = TableLogDao.getInstance().savePlayLogTable(logTable);
        for (Player player : getSeatMap().values()) {
            PlayLogUser logUser = new PlayLogUser();
            logUser.setUserId(player.getUserId());
            logUser.setCreateTime(now);
            logUser.setLogId(keyId);
            TableLogDao.getInstance().savePlayLogUser(logUser);
        }
        return 1;
    }

    public long getCredit100() {
        return credit100;
    }


    public void initCredit100() {
        if (this.creditMode == 1 && this.credit100 == 0) {
            // 当没有设置该值的时候，强制相当于设置过该值
            this.credit100 = 1;
            this.creditJoinLimit *= 100;
            this.creditDissLimit *= 100;
            this.creditCommission *= 100;
            this.creditCommissionLimit *= 100;
            this.creditDifen *= 100;
            this.creditCommissionBaoDi *= 100;
        }
    }

    /**
     * 所有玩法固定显示内容：
     * 【房间号】X/XX局
     * ——————————
     * 【包厢名】【玩法名称】【时间】
     * ——————————
     * 昵称【ID】，是否大赢家
     * 正负分
     * ——————————
     * 昵称【ID】，是否大赢家
     * 正负分
     *
     * 本接口适用最终结算分player.loadScore()的玩法，如果存在差异，请玩法内自行实现
     *
     * @return
     */
    public String getTableMsgForXianLiao() {
        StringBuilder sb = new StringBuilder();
        sb.append("【").append(getId()).append("】").append(finishBureau).append("/").append(totalBureau).append("局").append("\n");
        sb.append("————————————————").append("\n");
        sb.append("【").append(getRoomName()).append("】").append("\n");
        sb.append("【").append(getGameName()).append("】").append("\n");
        sb.append("【").append(TimeUtil.formatTime(new Date())).append("】").append("\n");
        int maxPoint = -999999999;
        List<Player> players = new ArrayList<>();
        for (Player player : getSeatMap().values()) {
            if (player.loadScore() > maxPoint) {
                maxPoint = player.loadScore();
            }
            players.add(player);
        }
        Collections.sort(players, new Comparator<Player>() {
            @Override
            public int compare(Player o1, Player o2) {
                return o2.loadScore() - o1.loadScore();
            }
        });
        for (Player player : players) {
            sb.append("————————————————").append("\n");
            int point = player.loadScore();
            sb.append(StringUtil.cutHanZi(player.getName(), 5)).append("【").append(player.getUserId()).append("】").append(point == maxPoint ? "，大赢家" : "").append("\n");
            sb.append(point > 0 ? "+" : point == 0 ? "" : "-").append(Math.abs(point)).append("\n");
        }
        return sb.toString();
    }


    /**
     * 计算信用分,
     */
    public void calcCreditNew() {
        if (!isCreditTable()) {
            return;
        }

        LogUtil.msgLog.info("calcCreditNew|start|" + getId() + "|" + getPlayBureau());
        Date now = new Date();
        String groupId = loadGroupId();
        int totalCommissionCredit = 0;
        List<Player> dyjPlayers = new ArrayList<>();
        long dyjCredit = 0;
        try {

            String keyId = serverKey.contains("_") ? serverKey.split("_")[1] : serverKey;
            GroupTable groupTable;
            if (this.groupTable != null && keyId.equals(String.valueOf(this.groupTable.getKeyId()))) {
                groupTable = this.groupTable;
            } else {
                groupTable = GroupDao.getInstance().loadGroupTableByKeyId(keyId);
            }

            // 保存玩家牌桌内输赢的信用分
            Map<Long, GroupUser> guMap = new HashMap<>();
            for (Player player : getSeatMap().values()) {
                GroupUser groupUser = getGroupUser(player.getUserId());
                if (groupUser == null) {
                    continue;
                }
                guMap.put(player.getUserId(), groupUser);
                int updateResult = 1;
                if (player.getWinLoseCredit() != 0) {
                    updateResult = updateGroupCredit(groupId, player.getUserId(), player.getSeat(), player.getWinLoseCredit());
                }
                HashMap<String, Object> log = new HashMap<>();
                log.put("groupId", groupId);
                log.put("userId", player.getUserId());
                log.put("optUserId", player.getUserId());
                log.put("tableId", getId());
                log.put("credit", player.getWinLoseCredit());
                log.put("type", Constants.CREDIT_LOG_TYPE_TABLE);
                log.put("flag", updateResult);
                log.put("promoterId1", groupUser.getPromoterId1());
                log.put("promoterId2", groupUser.getPromoterId2());
                log.put("promoterId3", groupUser.getPromoterId3());
                log.put("promoterId4", groupUser.getPromoterId4());
                log.put("promoterId5", groupUser.getPromoterId5());
                log.put("promoterId6", groupUser.getPromoterId6());
                log.put("promoterId7", groupUser.getPromoterId7());
                log.put("promoterId8", groupUser.getPromoterId8());
                log.put("promoterId9", groupUser.getPromoterId9());
                log.put("promoterId10", groupUser.getPromoterId10());
                log.put("roomName", StringUtils.isNotBlank(roomName) ? roomName : "");
                log.put("createdTime", now);
                log.put("groupTableId", groupTable != null ? groupTable.getKeyId() : 0);
                GroupDao.getInstance().insertGroupCreditLog(log);

                totalCommissionCredit += player.getCommissionCredit();
                if (player.getWinLoseCredit() > dyjCredit) {
                    dyjCredit = player.getWinLoseCredit();
                    dyjPlayers.clear();
                    dyjPlayers.add(player);
                } else if (dyjCredit > 0 && player.getWinLoseCredit() == dyjCredit) {
                    dyjPlayers.add(player);
                }
            }
            Long dataDate = Long.valueOf(new SimpleDateFormat("yyyyMMdd").format(now));
            for (Player player : dyjPlayers) {
                //信用分大赢家
                DataStatistics dataStatistics4 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "dyjCountCredit", 1);
                DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics4, 1);
            }
            GroupInfo groupInfo = GroupDao.getInstance().loadGroupInfo(Long.valueOf(groupId), 0);
            if (totalCommissionCredit > 0) {
				// 信用分分成 creditAllotMode 1大赢家分成，2参与分成
				int creditAllotMode = 1;
				int creditRate = 100;
				if (groupInfo != null) {
					creditAllotMode = groupInfo.getCreditAllotMode();
					creditRate = groupInfo.getCreditRate();
				}
				long masterId = 0;
				GroupUser master = GroupDao.getInstance().loadGroupMaster(groupId);
				if (master != null) {
					masterId = master.getUserId();
				}

				List<CreditCommission> commList = new ArrayList<>();
				if (dyjCount == 1 || creditAllotMode == 1) {
					for (Player player : getSeatMap().values()) {
						LogUtil.msgLog.info("calcCreditNew1|" + getId() + "|" + player.getUserId() + "|" + player.getWinLoseCredit() + "|" + player.getCommissionCredit());
						long commissionCredit = player.getCommissionCredit();
						if (commissionCredit <= 0) {
							continue;
						}
						GroupUser groupUser = getGroupUser(player.getUserId());
						if (groupUser == null) {
							continue;
						}
						if (isBaoDiCommission) {
							// 保低抽，只给群主
							commList.add(new CreditCommission(groupUser, masterId, commissionCredit));
						} else {
							GroupCommissionConfig sysConfig = GroupConstants.getSysCommssionConfig(creditRate, commissionCredit);
							if (creditAllotMode == 1) { // 大赢家分成
								if (sysConfig == null) {
									// 给群主
									commList.add(new CreditCommission(groupUser, masterId, commissionCredit));
									continue;
								}
								commList.addAll(calcCommissionNew(groupUser, masterId, sysConfig.getSeq(), commissionCredit, creditAllotMode, getPlayerCount()));
							} else { // 参与分成
								long tmp = commissionCredit % getPlayerCount();
								if (tmp > 0) { // 除不尽的分给群主
									CreditCommission comm = new CreditCommission(groupUser, masterId, tmp);
									comm.setAddCount(false);
									commList.add(new CreditCommission(groupUser, masterId, tmp));
								}
								commissionCredit = commissionCredit / getPlayerCount();
								for (Player player0 : getSeatMap().values()) {
									groupUser = getGroupUser(player0.getUserId());
									if (groupUser == null) {
										continue;
									}
									if (sysConfig == null) {
										// 给群主
										commList.add(new CreditCommission(groupUser, masterId, commissionCredit));
										continue;
									}
									commList.addAll(calcCommissionNew(groupUser, masterId, sysConfig.getSeq(), commissionCredit, creditAllotMode, getPlayerCount()));
								}
							}
						}

						//信用分贡献榜
						int tmpCredit = Long.valueOf(player.getCommissionCredit()).intValue();
						DataStatistics dataStatistics4 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "creditCommisionCount", tmpCredit);
						DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics4, 1);
					}
				} else {
					GroupUser groupUser = null;
					if (isBaoDiCommission) {
						// 保低抽，只给群主
						for (Player player : getSeatMap().values()) {
							if (player.getCommissionCredit() <= 0) {
								continue;
							}
							groupUser = getGroupUser(player.getUserId());
							if (groupUser == null) {
								continue;
							}
							commList.add(new CreditCommission(groupUser, masterId, player.getCommissionCredit()));
						}
					} else {
						long commissionCredit = totalCommissionCredit;
						long tmp = commissionCredit % getPlayerCount();
						if (tmp > 0) { // 除不尽的分给群主
							for (Player player : getSeatMap().values()) {
								if (player.getCommissionCredit() > 0) {
									groupUser = getGroupUser(player.getUserId());
								}
							}
							commList.add(new CreditCommission(groupUser, masterId, tmp));
						}
						GroupCommissionConfig sysConfig = GroupConstants.getSysCommssionConfig(creditRate, commissionCredit);
						commissionCredit = commissionCredit / getPlayerCount();
						for (Player player0 : getSeatMap().values()) {
							groupUser = getGroupUser(player0.getUserId());
							if (groupUser == null) {
								continue;
							}
							if (sysConfig == null) {
								// 给群主
								commList.add(new CreditCommission(groupUser, masterId, commissionCredit));
								continue;
							}
							commList.addAll(calcCommissionNew(groupUser, masterId, sysConfig.getSeq(), commissionCredit, creditAllotMode, getPlayerCount()));
						}
					}

					for (Player player : getSeatMap().values()) {
						LogUtil.msgLog.info("calcCreditNew2|" + getId() + "|" + player.getUserId() + "|" + player.getWinLoseCredit() + "|" + player.getCommissionCredit());
						if (player.getCommissionCredit() <= 0) {
							continue;
						}
						//信用分贡献榜
						int tmpCredit = Long.valueOf(player.getCommissionCredit()).intValue();
						DataStatistics dataStatistics4 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "creditCommisionCount", tmpCredit);
						DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics4, 1);
					}
				}
				doCommission(now, groupId, groupTable, commList);
				this.commList = commList;
			}
            LogUtil.msgLog.info("calcCreditNew|over|" + getId() + "|" + getPlayBureau());
            calcCoinOnOver(null);
        } catch (Exception e) {
            LogUtil.errorLog.error("calcCreditNew|error|" + getId() + "|" + groupId, e);
        }
    }

	/**
	 * 佣金到账并且记录log
	 * @param now
	 * @param groupId
	 * @param groupTable
	 * @param commList
	 */
	private void doCommission(Date now, String groupId, GroupTable groupTable, List<CreditCommission> commList) {
		try {
			TaskExecutor.EXECUTOR_SERVICE_STATISTICS.execute(new Runnable() {
				@Override
				public void run() {
					for (CreditCommission comm : commList) {
						int updateResult = updateGroupCreditPurse(String.valueOf(groupId), comm.getDestUserId(), -1, comm.getCredit());
						HashMap<String, Object> log = new HashMap<>();
						GroupUser groupUser = comm.getGroupUser();
						log.put("groupId", groupId);
						log.put("optUserId", groupUser.getUserId());
						log.put("userId", comm.getDestUserId());
						log.put("credit", comm.getCredit());
						log.put("type", Constants.CREDIT_LOG_TYPE_COMMSION);
						log.put("flag", updateResult);
						log.put("tableId", getId());
						log.put("promoterId1", groupUser.getPromoterId1());
						log.put("promoterId2", groupUser.getPromoterId2());
						log.put("promoterId3", groupUser.getPromoterId3());
						log.put("promoterId4", groupUser.getPromoterId4());
						log.put("promoterId5", groupUser.getPromoterId5());
						log.put("promoterId6", groupUser.getPromoterId6());
						log.put("promoterId7", groupUser.getPromoterId7());
						log.put("promoterId8", groupUser.getPromoterId8());
						log.put("promoterId9", groupUser.getPromoterId9());
						log.put("promoterId10", groupUser.getPromoterId10());
						log.put("roomName", StringUtils.isNotBlank(roomName) ? roomName : "");
						log.put("createdTime", now);
						log.put("groupTableId", groupTable != null ? groupTable.getKeyId() : 0);
						GroupDao.getInstance().insertGroupCreditLogSync(log);
					}
				}
			});
		} catch (Exception e) {
			LogUtil.errorLog.error("doCommission|error|" + groupTable.getTableId(), e);
		}
	}

	/**
	 * 计算洗牌信用分,
	 */
	public void calcCreditXipai(Player player) {
		if (!isCreditTable() || xipaiScoure <= 0) {
			return;
		}
		LogUtil.msgLog.info("calcCreditXipai|start|" + getId() + "|" + getPlayBureau());
		Date now = new Date();
		String groupId = loadGroupId();
		List<Player> dyjPlayers = new ArrayList<>();
		long dyjCredit = 0;
		try {

			String keyId = serverKey.contains("_") ? serverKey.split("_")[1] : serverKey;
			GroupTable groupTable;
			if (this.groupTable != null && keyId.equals(String.valueOf(this.groupTable.getKeyId()))) {
				groupTable = this.groupTable;
			} else {
				groupTable = GroupDao.getInstance().loadGroupTableByKeyId(keyId);
			}

			Map<Long, GroupUser> guMap = new HashMap<>();
			GroupUser groupUser = getGroupUser(player.getUserId());
			if (groupUser == null) {
				return;
			}
			guMap.put(player.getUserId(), groupUser);
			int updateResult = updateGroupCredit(groupId, player.getUserId(), player.getSeat(), -xipaiScoure);
			player.notifyCreditUpdate(Long.parseLong(groupId));
			HashMap<String, Object> log = new HashMap<>();
			log.put("groupId", groupId);
			log.put("userId", player.getUserId());
			log.put("optUserId", player.getUserId());
			log.put("tableId", getId());
			log.put("credit", -xipaiScoure);
			log.put("type", Constants.CREDIT_LOG_TYPE_XIPAI);
			log.put("flag", updateResult);
			log.put("promoterId1", groupUser.getPromoterId1());
			log.put("promoterId2", groupUser.getPromoterId2());
			log.put("promoterId3", groupUser.getPromoterId3());
			log.put("promoterId4", groupUser.getPromoterId4());
			log.put("promoterId5", groupUser.getPromoterId5());
			log.put("promoterId6", groupUser.getPromoterId6());
			log.put("promoterId7", groupUser.getPromoterId7());
			log.put("promoterId8", groupUser.getPromoterId8());
			log.put("promoterId9", groupUser.getPromoterId9());
			log.put("promoterId10", groupUser.getPromoterId10());
			log.put("roomName", StringUtils.isNotBlank(roomName) ? roomName : "");
			log.put("createdTime", now);
			log.put("groupTableId", groupTable != null ? groupTable.getKeyId() : 0);
			GroupDao.getInstance().insertGroupCreditLog(log);


			long promoterId = 0;
			promoterId = loadXipaiPromoterId(GroupDao.getInstance().loadGroupMaster(groupId),groupUser);
			updateResult = updateGroupCreditPurse(groupId, promoterId, player.getSeat(), xipaiScoure);
			log = new HashMap<>();
			log.put("groupId", groupId);
			log.put("optUserId", player.getUserId());
			log.put("userId", promoterId);
			log.put("credit", xipaiScoure);
			log.put("type", Constants.CREDIT_LOG_TYPE_XIPAI);
			log.put("flag", updateResult);
			log.put("tableId", getId());
			log.put("promoterId1", groupUser.getPromoterId1());
			log.put("promoterId2", groupUser.getPromoterId2());
			log.put("promoterId3", groupUser.getPromoterId3());
			log.put("promoterId4", groupUser.getPromoterId4());
			log.put("promoterId5", groupUser.getPromoterId5());
			log.put("promoterId6", groupUser.getPromoterId6());
			log.put("promoterId7", groupUser.getPromoterId7());
			log.put("promoterId8", groupUser.getPromoterId8());
			log.put("promoterId9", groupUser.getPromoterId9());
			log.put("promoterId10", groupUser.getPromoterId10());
			log.put("roomName", StringUtils.isNotBlank(roomName) ? roomName : "");
			log.put("createdTime", now);
			log.put("groupTableId", groupTable != null ? groupTable.getKeyId() : 0);
			GroupDao.getInstance().insertGroupCreditLog(log);

			LogUtil.msgLog.info("calcCreditXipai|over|" + getId() + "|" + getPlayBureau());
			calcCoinOnOver(null);
		} catch (Exception e) {
			LogUtil.errorLog.error("calcCreditXipai|error|" + getId() + "|" + groupId, e);
		}
	}


	/**
	 * 获得洗牌分归属人
	 * @param currUser
	 * @param fromUser
	 * @return
	 */
	public long loadXipaiPromoterId(GroupUser currUser, GroupUser fromUser) {
        long promoterId = 0;
        boolean isNextChengyuan = false;
        if (GroupConstants.isZhuGuan(currUser.getUserRole())) {
            if (currUser.getExtJson() != null && currUser.getExtJson().getIntValue(GroupConstants.extKey_xipaiConfig) == 1) {
                promoterId = GroupConstants.getNextId(fromUser, currUser.getPromoterLevel());
                if(promoterId == 0) {
                    isNextChengyuan = true;
                }
            }
        } else if (GroupConstants.isHuiZhang(currUser.getUserRole())) {
            promoterId = GroupConstants.getNextId(fromUser, currUser.getPromoterLevel());
            if(promoterId == 0) {
                isNextChengyuan = true;
            }
        }
        if (promoterId == 0) {
            if (isNextChengyuan) {           //返回currUserId
                return currUser.getUserId();
            } else {                    //返回上级id
                return GroupConstants.getUpId(fromUser, currUser.getPromoterLevel());
            }
        } else {
            GroupUser nextUser = GroupDao.getInstance().loadGroupUser(promoterId, groupId);
            return loadXipaiPromoterId(nextUser, fromUser);
        }
    }

	/**
	 * 2020年11月30日  AA信用分 第一局发牌扣除
	 */
	public void calcCreditAAOnStart() {
		if (AAScoure == 100 && getPlayBureau() == 1 && isCreditTable()) {
				calcCreditAA();
		}
	}
	/**
	 *  AA信用分 扣除
	 */
	public void calcCreditAA ( ){
		if (!isCreditTable()) {
			return;
		}
		LogUtil.msgLog.info("BaseTable|calcCreditAA|start|" + getId() + "|" + getPlayBureau());
		Date now = new Date();
		String groupId = loadGroupId();
		int totalCommissionCredit = 0;
		List<Player> dyjPlayers = new ArrayList<>();
		long dyjCredit = 0;
		try {

			String keyId = serverKey.contains("_") ? serverKey.split("_")[1] : serverKey;
			GroupTable groupTable;
			if (this.groupTable != null && keyId.equals(String.valueOf(this.groupTable.getKeyId()))) {
				groupTable = this.groupTable;
			} else {
				groupTable = GroupDao.getInstance().loadGroupTableByKeyId(keyId);
			}

			// 保存玩家 AA抽成信用分 creditCommission
			Map<Long, GroupUser> guMap = new HashMap<>();
			for (Player player : getSeatMap().values()) {
				GroupUser groupUser = getGroupUser(player.getUserId());
				player.setCommissionCredit(creditCommission);
				if (groupUser == null) {
					continue;
				}
				guMap.put(player.getUserId(), groupUser);
				int  updateResult = updateGroupCredit(groupId, player.getUserId(), player.getSeat(), -creditCommission);
				HashMap<String, Object> log = new HashMap<>();
				log.put("groupId", groupId);
				log.put("userId", player.getUserId());
				log.put("optUserId", player.getUserId());
				log.put("tableId", getId());
				log.put("credit", -creditCommission);
				log.put("type", Constants.CREDIT_LOG_TYPE_AA);
				log.put("flag", updateResult);
				log.put("promoterId1", groupUser.getPromoterId1());
				log.put("promoterId2", groupUser.getPromoterId2());
				log.put("promoterId3", groupUser.getPromoterId3());
				log.put("promoterId4", groupUser.getPromoterId4());
				log.put("promoterId5", groupUser.getPromoterId5());
				log.put("promoterId6", groupUser.getPromoterId6());
				log.put("promoterId7", groupUser.getPromoterId7());
				log.put("promoterId8", groupUser.getPromoterId8());
				log.put("promoterId9", groupUser.getPromoterId9());
				log.put("promoterId10", groupUser.getPromoterId10());
				log.put("roomName", StringUtils.isNotBlank(roomName) ? roomName : "");
				log.put("createdTime", now);
				log.put("groupTableId", groupTable != null ? groupTable.getKeyId() : 0);
				GroupDao.getInstance().insertGroupCreditLog(log);

				totalCommissionCredit += creditCommission;
//				if (player.getWinLoseCredit() > dyjCredit) {
//					dyjCredit = player.getWinLoseCredit();
//					dyjPlayers.clear();
//					dyjPlayers.add(player);
//				} else if (dyjCredit > 0 && player.getWinLoseCredit() == dyjCredit) {
//					dyjPlayers.add(player);
//				}
			}
			Long dataDate = Long.valueOf(new SimpleDateFormat("yyyyMMdd").format(now));
//			for (Player player : dyjPlayers) {
//				//信用分大赢家
//				DataStatistics dataStatistics4 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "dyjCountCredit", 1);
//				DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics4, 1);
//			}
			GroupInfo groupInfo = GroupDao.getInstance().loadGroupInfo(Long.valueOf(groupId), 0);
			if (totalCommissionCredit > 0) {
				// 信用分分成 creditAllotMode 1大赢家分成，2参与分成
				int creditAllotMode = 2;
				int creditRate = 100;
				if (groupInfo != null) {
					//creditAllotMode = groupInfo.getCreditAllotMode();
					creditRate = groupInfo.getCreditRate();
				}
				long masterId = 0;
				GroupUser master = GroupDao.getInstance().loadGroupMaster(groupId);
				if (master != null) {
					masterId = master.getUserId();
				}
				// aa分成无大赢家模式
				List<CreditCommission> commList = new ArrayList<>();
				//if(dyjCount == 1 || creditAllotMode == 1) {
//					for (Player player : getSeatMap().values()) {
//						LogUtil.msgLog.info("calcCreditNew1|" + getId() + "|" + player.getUserId() + "|" + player.getWinLoseCredit() + "|" + player.getCommissionCredit());
//						long commissionCredit = player.getCommissionCredit();
//						if (commissionCredit <= 0) {
//							continue;
//						}
//						GroupUser groupUser = getGroupUser(player.getUserId());
//						if (groupUser == null) {
//							continue;
//						}
//						if (isBaoDiCommission) {
//							// 保低抽，只给群主
//							commList.add(new CreditCommission(groupUser, masterId, commissionCredit));
//						} else {
//							GroupCommissionConfig sysConfig = GroupConstants.getSysCommssionConfig(creditRate, commissionCredit);
//							if (creditAllotMode == 1) { // 大赢家分成
//								if (sysConfig == null) {
//									// 给群主
//									commList.add(new CreditCommission(groupUser, masterId, commissionCredit));
//									continue;
//								}
//								commList.addAll(calcCommissionNew(groupUser, masterId, sysConfig.getSeq(), commissionCredit, creditAllotMode, getPlayerCount()));
//							} else { // 参与分成
//								long tmp = commissionCredit % getPlayerCount();
//								if (tmp > 0) { // 除不尽的分给群主
//									CreditCommission comm = new CreditCommission(groupUser, masterId, tmp);
//									comm.setAddCount(false);
//									commList.add(new CreditCommission(groupUser, masterId, tmp));
//								}
//								commissionCredit = commissionCredit / getPlayerCount();
//								for (Player player0 : getSeatMap().values()) {
//									groupUser = getGroupUser(player0.getUserId());
//									if (groupUser == null) {
//										continue;
//									}
//									if (sysConfig == null) {
//										// 给群主
//										commList.add(new CreditCommission(groupUser, masterId, commissionCredit));
//										continue;
//									}
//									commList.addAll(calcCommissionNew(groupUser, masterId, sysConfig.getSeq(), commissionCredit, creditAllotMode, getPlayerCount()));
//								}
//							}
//						}
//
//						//信用分贡献榜
//						int tmpCredit = Long.valueOf(player.getCommissionCredit()).intValue();
//						DataStatistics dataStatistics4 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "creditCommisionCount", tmpCredit);
//						DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics4, 1);
//					}
				//}else
				{
					GroupUser groupUser = null;
					if (isBaoDiCommission) {
						// 保低抽，只给群主
						for (Player player : getSeatMap().values()) {
							if (player.getCommissionCredit() <= 0) {
								continue;
							}
							groupUser = getGroupUser(player.getUserId());
							if (groupUser == null) {
								continue;
							}
							commList.add(new CreditCommission(groupUser, masterId, player.getCommissionCredit()));
						}
					} else {
						long commissionCredit = totalCommissionCredit;
						long tmp = commissionCredit % getPlayerCount();
						if (tmp > 0) { // 除不尽的分给群主
							for (Player player : getSeatMap().values()) {
								if (player.getCommissionCredit() > 0) {
									groupUser = getGroupUser(player.getUserId());
								}
							}
							commList.add(new CreditCommission(groupUser, masterId, tmp));
						}
						GroupCommissionConfig sysConfig = GroupConstants.getSysCommssionConfig(creditRate, commissionCredit);
						commissionCredit = commissionCredit / getPlayerCount();
						for (Player player0 : getSeatMap().values()) {
							groupUser = getGroupUser(player0.getUserId());
							if (groupUser == null) {
								continue;
							}
							if (sysConfig == null) {
								// 给群主
								commList.add(new CreditCommission(groupUser, masterId, commissionCredit));
								continue;
							}
							commList.addAll(calcCommissionNew(groupUser, masterId, sysConfig.getSeq(), commissionCredit, creditAllotMode, getPlayerCount()));
						}
					}

					for (Player player : getSeatMap().values()) {
						LogUtil.msgLog.info("BaseTable|calcCreditAA|" + getId() + "|" + player.getUserId() + "|" + player.getCommissionCredit());
						if (player.getCommissionCredit() <= 0) {
							continue;
						}
						//信用分贡献榜
						int tmpCredit = Long.valueOf(player.getCommissionCredit()).intValue();
						DataStatistics dataStatistics4 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "creditCommisionCount", tmpCredit);
						DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics4, 1);
					}
				}
				doCommission(now, groupId, groupTable, commList);
				this.commList = commList;
			}
			LogUtil.msgLog.info("BaseTable|calcCreditAA|over|" + getId() + "|" + getPlayBureau());
			//calcCoinOnOver(null);
		} catch (Exception e) {
			LogUtil.errorLog.error("BaseTable|calcCreditAA||error|" + getId() + "|" + groupId, e);
		}
	}


	/**
     * 计算信用分分成
     *
     * @param groupUser
     * @param commissionCredit
     * @return
     * @throws Exception
     */
    private List<CreditCommission> calcCommissionNew(GroupUser groupUser, long masterId, int seq, long commissionCredit, int allotMode, int playerCount) throws Exception {

        List<CreditCommission> commList = new ArrayList<>();
        if (groupUser == null) {
            return commList;
        }
        String groupId = groupUser.getGroupId().toString();
        GroupUser master = GroupDao.getInstance().loadGroupMaster(groupId);
        calcCommissionToUser(groupId, groupUser, allotMode, playerCount, seq, master, commissionCredit, commList);
        return commList;
    }

    /**
     * 计算抽水
     *
     * @param groupId          俱乐部id
     * @param fromUser         计算抽水的提供方
     * @param allotMode        抽水方式：1大赢家分成，2参与分成
     * @param playerCount      牌桌人数
     * @param seq              抽水档序号
     * @param curUser          计算抽水收益方
     * @param commissionCredit 抽水分
     * @param commList
     * @throws Exception
     */
    public void calcCommissionToUser(String groupId, GroupUser fromUser, int allotMode, int playerCount, int seq, GroupUser curUser, long commissionCredit, List<CreditCommission> commList) throws Exception {
        long curUserId = curUser.getUserId();
        long nextUserId = GroupConstants.getNextId(fromUser,curUser.getPromoterLevel());
        if (nextUserId == 0) {
            commList.add(new CreditCommission(fromUser, curUserId, commissionCredit));
            return;
        }
        GroupUser nextUser = GroupDao.getInstance().loadGroupUser(nextUserId, groupId);
        if (nextUser == null) {
            // 给群主
            commList.add(new CreditCommission(fromUser, curUserId, commissionCredit));
            return;
        }
        nextUserId = nextUser.getUserId();
        GroupCommissionConfig config = GroupDao.getInstance().loadCommissionConfigBySeq(groupId, nextUserId, seq);
        long nowCredit = 0;
        if (config == null) {
            //全部给自己
            commList.add(new CreditCommission(fromUser, curUserId, commissionCredit));
            return;
        } else {
            nowCredit = config.getCredit();
            if (allotMode == 2) {
                nowCredit = nowCredit / playerCount;
            }
            if (nowCredit > 0) {
                if (commissionCredit < nowCredit) {
                    nowCredit = commissionCredit;
                }
                commList.add(new CreditCommission(fromUser, curUserId, nowCredit));
            }
        }
        long leftCredit = commissionCredit - nowCredit;
        if (leftCredit <= 0) {
            // 无剩余分数直接返回
            return;
        }
        calcCommissionToUser(groupId, fromUser, allotMode, playerCount, seq, nextUser, leftCredit, commList);
    }



    public int getSwitchCoin() {
        return switchCoin;
    }

    public void setSwitchCoin(int switchCoin) {
        this.switchCoin = switchCoin;
        this.changeExtend();
    }

    public int getCreditRate() {
        return creditRate;
    }

    public void setCreditRate(int creditRate) {
        this.creditRate = creditRate;
        this.changeExtend();
    }

    /**
     * 牌局开始前计算玩家金币
     */
    public void calcCoinOnStart() {
        try {
            if (switchCoin == 0) {
                return;
            }
            if (!isCreditTable()) {
                return;
            }
            String groupIdStr = loadGroupId();
            GroupInfo group = GroupDao.getInstance().loadGroupInfo(groupIdStr, "0");
            if (group == null) {
                LogUtil.errorLog.info("calcCoinOnStart|error|" + getId() + "|groupIsNull");
                return;
            }
            for (Player player : getSeatMap().values()) {
                // -------扣除玩家金币-------------
                long credit = creditCommission;
                credit = (credit * 100) / group.getCreditRate();
                long coin = GroupConstants.getSysCoinConsume(Math.abs(credit));
                if (coin == 0) {
                    continue;
                }
                player.changeUserCoin(0, -coin, true, playType, CoinSourceType.onTableStart);
            }
            writeCoinMsgOnStart();
        } catch (Exception e) {
            LogUtil.errorLog.info("calcCoinOnStart|error|" + getId(), e);
        }
    }

    private void writeCoinMsgOnStart() {
        List<Map<String,String>> dataList = new ArrayList<>();
        for (Player player : getSeatMap().values()) {
            Map<String,String> data = new HashMap<>();
            data.put("userId",String.valueOf(player.getUserId()));
            data.put("coin",String.valueOf(player.loadAllCoin()));
            dataList.add(data);
        }
        ComRes.Builder res = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_coin_table_start, JSON.toJSONString(dataList));
        broadMsg(res.build());
    }

    /**
     * 牌局大结算时计算金币
     * 结算比赛分时计算金币
     * 计算玩家经验值
     * 计算亲友圈经验值
     */
    public void calcCoinOnOver(GroupInfo group) {
        try {
            if (switchCoin == 0) {
                return;
            }
            if (!isCreditTable()) {
                return;
            }
            LogUtil.msgLog.info("calcCoinOnOver|start|" + getId() + "|" + getPlayBureau());
            if (group == null) {
                group = GroupDao.getInstance().loadGroupInfo(loadGroupIdLong());
                if (group == null) {
                    return;
                }
            }

            writeCoinMsgOnOver();

            Date now = new Date();
            for (Player player : getSeatMap().values()) {

                // -------计算亲友圈玩家牌局输赢金币-------------
                long coin = player.getWinLoseCoin();
                if (coin != 0) {
                    player.changeUserCoin(0, coin, true, playType, CoinSourceType.onTableOver);
                }

                // -------计算亲友圈玩家经验值----------
                GroupUser gu = getGroupUser(player.getUserId());
                int oldLevel = gu.getLevel();
                SysGroupUserLevelConfig guConfig = GroupConfigUtil.getGroupUserLevelConfig(gu.getLevel());
                if (guConfig.getExp() > 0) {
                    long exp = guConfig.getPlayExp();
                    GroupDao.getInstance().addGroupUserExp(gu.getKeyId(), exp);

                    // ------计算亲友圈玩家等级--------------
                    GroupDao.getInstance().calcGroupUserLevel(gu.getKeyId());

                    SysGroupUserLevelConfig next = GroupConfigUtil.getGroupUserLevelConfig(guConfig.getLevel() + 1);
                    if (next != null && next.getExp() == 0) {
                        GroupDao.getInstance().calcGroupUserExp(gu.getKeyId());
                    }

                    HashMap<String, Object> guLogMap = new HashMap<>();
                    guLogMap.put("groupId", gu.getGroupId());
                    guLogMap.put("userId", gu.getUserId());
                    guLogMap.put("optUserId", 0);
                    guLogMap.put("tableId", getId());
                    guLogMap.put("credit", 0);
                    guLogMap.put("exp", exp);
                    guLogMap.put("createdTime", now);
                    GroupDao.getInstance().insertLogGroupUserExp(guLogMap);

                    gu = GroupDao.getInstance().loadGroupUser(player.getUserId(), gu.getGroupId().toString());
                    if (gu.getLevel() > oldLevel) {
                        // 升级记录
                        HashMap<String, Object> guLevelMap = new HashMap<>();
                        guLevelMap.put("groupId", gu.getGroupId());
                        guLevelMap.put("userId", gu.getUserId());
                        guLevelMap.put("level", gu.getLevel());
                        guLevelMap.put("stat", 1);
                        guLevelMap.put("createdTime", now);
                        guLevelMap.put("lastUpTime", now);
                        GroupDao.getInstance().insertLogGroupUserLevel(guLevelMap);
                        // 推送消息级前端
                        player.notifyGroupUserLevelUp(gu.getGroupId().toString(), gu.getLevel().toString());
                    }
                }
            }

            // -------计算亲友圈的经验-----
            SysGroupLevelConfig gConfig = GroupConfigUtil.getGroupLevelConfig(group.getLevel());
            if (gConfig.getExp() > 0) {
                long exp = gConfig.getPlayExp();
                GroupDao.getInstance().addGroupExp(group.getKeyId(), exp);

                // -------计算亲友圈的等级-----
                GroupDao.getInstance().calcGroupLevel(group.getKeyId());

                SysGroupLevelConfig next = GroupConfigUtil.getGroupLevelConfig(gConfig.getLevel() + 1);
                if (next != null && next.getExp() == 0) {
                    GroupDao.getInstance().calcGroupExp(group.getKeyId());
                }

                HashMap<String, Object> guLogMap = new HashMap<>();
                guLogMap.put("groupId", group.getGroupId());
                guLogMap.put("userId", 0);
                guLogMap.put("optUserId", 0);
                guLogMap.put("tableId", getId());
                guLogMap.put("credit", 0);
                guLogMap.put("exp", exp);
                guLogMap.put("createdTime", now);
                GroupDao.getInstance().insertLogGroupExp(guLogMap);
            }
            LogUtil.msgLog.info("calcCoinOnOver|over|" + getId() + "|" + getPlayBureau());
        } catch (Exception e) {
            LogUtil.errorLog.info("calcCoinOnOver|error|" + getId(), e);
        }
    }

    private void writeCoinMsgOnOver() {
        List<Map<String, String>> dataList = new ArrayList<>();
        for (Player player : getSeatMap().values()) {
            Map<String, String> data = new HashMap<>();
            data.put("userId", String.valueOf(player.getUserId()));
            data.put("coin", String.valueOf(player.getWinLoseCoin()));
            dataList.add(data);
        }
        String dataStr = JSON.toJSONString(dataList);
        ComRes.Builder res = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_coin_table_over, dataStr);
        broadMsg(res.build());
        LogUtil.msgLog.info("writeCoinMsgOnOver|" + getId() + "|" + getPlayBureau() + "|" + dataStr);
    }

    /**
     * 第一局发牌前检查
     * 发牌前检查玩家身上信用分是否满足最低信用分需求
     *
     * @return
     */
    public synchronized boolean checkCreditOnTableStart() {

        if (playBureau != 1) {
            return true;
        }
        if (!isCreditTable()) {
            return true;
        }
        try {
			// 重新查询亲友圈用户数据
			initGroupUser();
            String disPlayerNames = "";
            String groupIdStr = loadGroupId();
            StringBuilder sb = new StringBuilder("checkCreditOnTableStart");
            sb.append("|").append(getId());
            sb.append("|").append(getPlayBureau());
            sb.append("|").append(creditJoinLimit);
            sb.append("|");
            for (Player player : getSeatMap().values()) {
                GroupUser gu = GroupDao.getInstance().loadGroupUserForceMaster(player.getUserId(), groupIdStr);
                if (gu == null || gu.getCredit() < creditJoinLimit) {
                    disPlayerNames += player.getName() + ",";
                    sb.append(gu.getUserId()).append(",").append(gu.getCredit()).append(";");
                }
            }
            if (!"".equals(disPlayerNames)) {
                LogUtil.msgLog.info(sb.toString());
                isDissByCreditLimit = true;
                creditLimitPlayerNames = disPlayerNames;
                disPlayerNames = disPlayerNames.substring(0, disPlayerNames.length() - 1);
                for (Player player : getSeatMap().values()) {
                    player.writeErrMsg(LangMsg.code_65, disPlayerNames, MathUtil.formatCredit(creditJoinLimit));
                }
                for (Player player : getRoomPlayerMap().values()) {
                    player.writeErrMsg(LangMsg.code_65, disPlayerNames, MathUtil.formatCredit(creditJoinLimit));
                }

                // 推送解散消息
                int groupId = Integer.parseInt(groupIdStr);
                ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_disstable,String.valueOf(calcTableType()), playType, groupId, playedBureau);
                GeneratedMessage msg = com.build();
                for (Player player : getSeatMap().values()) {
                    player.writeSocket(msg);
                }
                for (Player player : roomPlayerMap.values()) {
                    player.writeSocket(msg);
                }

                setSpecialDiss(1);
                setTiqianDiss(true);
                LogUtil.msgLog.info("BaseTable|dissReason|checkCreditOnTableStart|1|" + getId() + "|" + getPlayBureau() + "|" + disPlayerNames);
                diss();
                return false;
            }
        } catch (Exception e) {
            LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
        }
        return true;
    }

    /**
     * 是否全服关闭托管
     *
     * @return
     */
    public boolean isAutoPlayOff() {
        return ResourcesConfigsUtil.isAutoPlayOff();
    }

    public long getLastStartNextUser() {
        return lastStartNextUser;
    }

    public void setLastStartNextUser(long lastStartNextUser) {
        this.lastStartNextUser = lastStartNextUser;
    }


    /**
     * 计算保底
     *
     * @param credit
     * @return
     */
    public long calcBaoDi(long credit) {
        if (baoDiConfigList.size() > 0) {
            for (BaoDiConfig config : baoDiConfigList) {
                if (credit >= config.getStart() && credit <= config.getEnd()) {
                    return config.getBaoDi();
                }
            }
            return 0;
        } else {
            return creditCommissionBaoDi;
        }
    }

    /**
     * 初始始化牌桌的亲友圈信息
     */
    public void initGroupInfo() {
        if (isGroupRoom()) {
            this.loadGroupId();
            this.loadGroupIdLong();
            this.loadGroupTableKeyId();
            this.loadGroupTable();
            this.loadGroupMasterId();
            this.loadGroupTableConfig();
        }
    }

    /**
     * 最后一个点开始下一局的玩家，最后推送发牌消息
     */
    public void sendLastDealMsg() {
        if (lastStartNextUser == 0) {
            return;
        }
        try {
            Map<Long, Player> playerMap = getPlayerMap();
            if (playerMap == null) {
                return;
            }
            Player player = playerMap.get(lastStartNextUser);
            if (player == null) {
                return;
            }
            if(lastDealMsg != null){
                player.writeSocket(lastDealMsg);
            }
            sendTingInfo(lastStartNextUser);
        } catch (Exception e) {
            LogUtil.errorLog.error("sendLastDealMsg|error|" + lastStartNextUser, e);
        } finally {
            lastStartNextUser = 0;
            lastDealMsg = null;
        }
    }

    /**
     * 推送听牌消息
     * @param userId
     */
    public void sendTingInfo(long userId) {

    }

    /**
     * 创建亲友圈好友列表
     * 私密房开局后，牌局内所有玩家创建好友关系
     */
    protected void genGroupUserFriend() {
        try {
            TaskExecutor.SINGLE_EXECUTOR_SERVICE_USER.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (getPlayBureau() != 1) {
                            return;
                        }
                        if (!isGroupRoom()) {
                            return;
                        }
                        GroupTable gt = loadGroupTable();
                        if (gt == null || gt.getIsPrivate() == null || gt.getIsPrivate() == 0) {
                            return;
                        }
                        Set<String> sets = new HashSet<>();
                        for (Player player1 : getSeatMap().values()) {
                            for (Player player2 : getSeatMap().values()) {
                                if (player1.getUserId() == player2.getUserId()) {
                                    continue;
                                }
                                String userIdKeyStr = GroupConstants.genGroupUserFriendKeyStr(player1.getUserId(), player2.getUserId());
                                if (sets.contains(userIdKeyStr)) {
                                    continue;
                                }
                                if (GroupDao.getInstance().countGroupUserFriend(gt.getGroupId(), player1.getUserId(), player2.getUserId()) == 0) {
                                    Map<String, Object> map = new HashMap();
                                    map.put("groupId", gt.getGroupId());
                                    map.put("userId1", MathUtil.smallOne(player1.getUserId(), player2.getUserId()));
                                    map.put("userId2", MathUtil.bigOne(player1.getUserId(), player2.getUserId()));
                                    map.put("createdTime", new Date());
                                    map.put("userIdKeyStr", userIdKeyStr);
                                    GroupDao.getInstance().createGroupUserFriend(map);
                                    sets.add(userIdKeyStr);
                                }
                            }
                        }
                    } catch (Exception e) {
                        LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
                    }
                }
            });
        } catch (Exception e) {
            LogUtil.errorLog.error("genGroupUserFriend|error|" + e.getMessage(), e);
        }
    }

    /**
     * 检查亲友圈是否是上下级关系
     *
     * @param player
     * @return
     */
    public boolean isCanJoinFOrWzQ(Player player) {
        String gid = loadGroupId();
        GroupUser selfGu = player.loadGroupUser(gid);
        if (selfGu == null) {
            return false;
        }
        for (Player p : getSeatMap().values()) {
            GroupUser otherGu = p.loadGroupUser(gid);
            if (otherGu == null) {
                return false;
            }
            if (!GroupConstants.isCanPlayWzq(selfGu, otherGu)) {
                return false;
            }
        }
        return true;
    }

    public GoldRoom getGoldRoom() {
        return goldRoom;
    }

    public void setGoldRoom(GoldRoom goldRoom) {
        this.goldRoom = goldRoom;
    }

    public synchronized void addGoldRoomUser(GoldRoomUser goldRoomUser) {
        if (goldRoomUserMap == null) {
            goldRoomUserMap = new HashMap<>();
        }
        goldRoomUserMap.put(goldRoomUser.getUserId(), goldRoomUser);
    }

    public Map<Long, GoldRoomUser> getGoldRoomUserMap() {
        return goldRoomUserMap;
    }

    public GoldRoomUser getGoldRoomUser(long userId){
        return goldRoomUserMap.get(userId);
    }

    public CompetitionRoom getCompetitionRoom() {
        return competitionRoom;
    }

    public void setCompetitionRoom(CompetitionRoom competitionRoom) {
        this.competitionRoom = competitionRoom;
    }

    public synchronized void addCompetitionRoomUser(CompetitionRoomUser competitionRoomUser) {
        if (competitionRoomUserMap == null) {
			competitionRoomUserMap = new ConcurrentHashMap<>();
        }
		competitionRoomUserMap.put(competitionRoomUser.getUserId(), competitionRoomUser);
    }

    public Map<Long, CompetitionRoomUser> getCompetitionRoomUserMap() {
        return competitionRoomUserMap;
    }

    public CompetitionRoomUser getCompetitionRoomUser(long userId){
        return competitionRoomUserMap.get(userId);
    }

    /**
     * 初始始化牌桌的金币房信息
     */
    public boolean initGoldRoom() {
        if (!isGoldRoom()) {
            return true;
        }
        try {
            GoldRoom goldRoom = GoldRoomDao.getInstance().loadGoldRoom(goldRoomId);
            if(goldRoom == null || goldRoom.isOver()){
                return false;
            }
            setGoldRoom(goldRoom);
            List<GoldRoomUser> userList = GoldRoomDao.getInstance().loadAllGoldRoomUser(goldRoom.getKeyId());
            if(userList == null || userList.size() == 0){
                return false;
            }
            for (GoldRoomUser user : userList) {
                goldRoomUserMap.put(user.getUserId(), user);
            }
        } catch (Exception e) {
            LogUtil.errorLog.error("initGoldRoom|error|" + id, e);
        }
        return true;
    }

	/**
	 * 初始始化牌桌的比赛房信息
	 */
	public void initCompetitionRoom() {
		if (!isCompetitionRoom()) {
			return;
		}
		try {
			CompetitionRoom room = CompetitionDao.getInstance().loadCompetitionRoom(competitionRoomId);
			if (room != null) {
				setCompetitionRoom(room);
				List<CompetitionRoomUser> userList = CompetitionDao.getInstance().loadAllCompetitionRoomUser(room.getKeyId());
				if (userList != null && userList.size() > 0) {
					for (CompetitionRoomUser user : userList) {
						competitionRoomUserMap.put(user.getUserId(), user);
					}
				}
			}

		} catch (Exception e) {
			LogUtil.errorLog.error("initCompetitionRoom|error|" + id, e);
		}
	}

	/**
	 * 新金币场，开局前扣玩家金币
	 *
	 * @return
	 */
	public boolean payGoldRoomTicket() {
		try {
			// --------------------门票--------------------
			long ticket = goldRoom.getTicket();
			for (Player player : getSeatMap().values()) {
				player.changeGold(-ticket, playType, SourceType.table_ticket);
				GoldRoomUser goldRoomUser = getGoldRoomUser(player.getUserId());
				if (goldRoomUser != null && goldRoomUser.getGroupId() > 0) {
					GroupUser groupUser = GroupDao.getInstance().loadGroupUser(player.getUserId(), goldRoomUser.getGroupId().toString());
					if (groupUser != null) {
						guMap.put(player.getUserId(), groupUser);
					}
				}
			}
			// -----------抽水分成-----------------------
			calcGoldRoomCommission();
			return true;
		} catch (Exception e) {
			LogUtil.errorLog.error("payGoldRoomTicket|error|" + getId(), e);
		}
		return true;
	}

    /**
     *  金币场抽水
     */
    private void calcGoldRoomCommission(){
        try{
            long ticket = goldRoom.getTicket();
            Long dataDate = Long.valueOf(new SimpleDateFormat("yyyyMMdd").format(new Date()));
            List<GroupGoldLog> goldCommList = new ArrayList<>();
            List<LogGroupGoldTable> logGroupGoldTableList = new ArrayList<>();
            List<LogGroupGoldCommission> logGroupGoldCommissionList = new ArrayList<>();
            for (Player player : getSeatMap().values()) {
                LogGroupGoldTable logGroupGoldTable = new LogGroupGoldTable(dataDate, 0, goldRoom.getConfigId(), player.getUserId());
                logGroupGoldTable.setCommission(0);
                logGroupGoldTable.setTicket(ticket);
                logGroupGoldTable.setTicketCount(1);
                logGroupGoldTable.setPlayType(playType);
                logGroupGoldTableList.add(logGroupGoldTable);

                GoldRoomUser goldRoomUser = getGoldRoomUser(player.getUserId());
                if (goldRoomUser == null || goldRoomUser.getGroupId() <= 0) {
                    continue;
                }
                GroupInfo groupInfo = GroupDao.getInstance().loadGroupInfo(goldRoomUser.getGroupId());
                if (groupInfo == null || groupInfo.getGoldRoomRate() < 0) {
                    continue;
                }
                long commissionValue = 0; // 给亲友圈分成
                long toSysRate = groupInfo.getGoldRoomRate();
                if (toSysRate >= 100 || toSysRate < 0) {
                    commissionValue = 0;
                } else {
                    commissionValue = ticket * (100 - toSysRate) / 100;
                    if (commissionValue <= 0) {
                        commissionValue = 0;
                    }
                }
                if (commissionValue <= 0) {
                    continue;
                }
                GroupGoldCommissionConfig sysConfig = GroupConstants.getSysGoldCommissionConfig(creditRate, commissionValue);
                if (sysConfig == null) {
                    continue;
                }
                GroupUser groupUser = guMap.get(player.getUserId());
                if (groupUser == null) {
                    continue;
                }
                boolean onlyMaster = ticket < 1000; // 只分到群主
                goldCommList.addAll(calcGoldCommission(groupUser, sysConfig.getSeq(), commissionValue,onlyMaster));

                logGroupGoldTable.setGroupId(goldRoomUser.getGroupId());
                logGroupGoldTable.setCommission(commissionValue);
                logGroupGoldTable.setCommissionCount(1);

                logGroupGoldCommissionList.addAll(calcLogGroupGoldCommission(dataDate, player, groupUser, commissionValue));
            }

            this.goldCommList = goldCommList;

            // --------------抽水记录-----------------------------------
            saveGroupGoldLog(goldCommList);

            // --------------------抽水统计-----------------------------
            if (goldCommList.size() > 0) {
                for (GroupGoldLog comm : goldCommList) {
                    LogGroupGoldCommission log = new LogGroupGoldCommission(dataDate, comm.getGroupUser().getGroupId(), comm.getDestUserId());
                    log.setGold(comm.getValue());
                    log.setCommissionCount(1);
                    logGroupGoldCommissionList.add(log);
                }
                Map<Long, LogGroupGoldCommission> map = new HashMap<>();
                for (LogGroupGoldCommission tmp : logGroupGoldCommissionList) {
                    LogGroupGoldCommission log = map.get(tmp.getUserId());
                    if (log == null) {
                        map.put(tmp.getUserId(), tmp);
                    } else {
                        log.addProp(tmp);
                    }
                }
                if(map.size() > 0) {
                    LogDao.getInstance().saveLogGroupGoldCommission(new ArrayList<>(map.values()));
                }
            }

            // -------------------------------------------------
            LogDao.getInstance().saveLogGroupGoldTable(logGroupGoldTableList);
        }catch (Exception e){
            LogUtil.errorLog.error("calcGoldRoomCommission|error|" + getId());
        }
    }

    public void saveGroupGoldLog(List<GroupGoldLog> goldLogList) {
        Date now = new Date();
        for (GroupGoldLog goldLog : goldLogList) {
            int updateResult = 1;
            Player player = PlayerManager.getInstance().getPlayer(goldLog.getDestUserId());
            if (player != null) {
                player.changeGold(goldLog.getValue(), playType, SourceType.group_commission);
            } else {
                GoldPlayer goldPlayer = GoldDao.getInstance().selectGoldUserByUserId(goldLog.getDestUserId());
                if (goldPlayer == null) {
                    updateResult = GoldDao.getInstance().changeUserGoldDirect(goldLog.getDestUserId(), goldLog.getValue(), 0);
                    PlayerManager.getInstance().addUserGoldRecord(new UserGoldRecord(goldLog.getDestUserId(), 0, 0, 0, (int) goldLog.getValue(), playType, SourceType.group_commission));
                } else {
                    Consume consume = new Consume();
                    consume.setPlayType(getPlayType());
                    consume.setValue(goldLog.getValue());
                    consume.setSourceType(SourceType.group_commission);
                    goldPlayer.changeGold(consume);
                }
            }
            HashMap<String, Object> log = new HashMap<>();
            log.put("optUserId", goldLog.getFromUserId());
            log.put("userId", goldLog.getDestUserId());
            log.put("gold", goldLog.getValue());
            log.put("type", goldLog.getType());
            log.put("flag", updateResult);
            log.put("tableId", getId());
            GroupUser groupUser = goldLog.getGroupUser();
            log.put("groupId", groupUser.getGroupId());
            log.put("promoterId1", groupUser.getPromoterId1());
            log.put("promoterId2", groupUser.getPromoterId2());
            log.put("promoterId3", groupUser.getPromoterId3());
            log.put("promoterId4", groupUser.getPromoterId4());
            log.put("promoterId5", groupUser.getPromoterId5());
            log.put("promoterId6", groupUser.getPromoterId6());
            log.put("promoterId7", groupUser.getPromoterId7());
            log.put("promoterId8", groupUser.getPromoterId8());
            log.put("promoterId9", groupUser.getPromoterId9());
            log.put("promoterId10", groupUser.getPromoterId10());
            log.put("roomName", StringUtils.isNotBlank(roomName) ? roomName : "");
            log.put("createdTime", now);
            GroupDao.getInstance().insertGroupGoldLog(log);
        }
    }


    /**
     * 计算信用分分成
     *
     * @param groupUser
     * @param seq
     * @param value
     * @param onlyMaster 分给群主，不再往下分
     * @return
     * @throws Exception
     */
    private List<GroupGoldLog> calcGoldCommission(GroupUser groupUser, int seq, long value, boolean onlyMaster) throws Exception {
        List<GroupGoldLog> commList = new ArrayList<>();
        if (groupUser == null) {
            return commList;
        }
        long groupId = groupUser.getGroupId();
        GroupUser master = GroupDao.getInstance().loadGroupMaster(String.valueOf(groupId));
        if (onlyMaster) {
            commList.add(new GroupGoldLog(Constants.GOLD_LOG_TYPE_COMMISSION, groupUser.getUserId(), groupUser, master.getUserId(), value));
        } else {
            calcGoldCommissionToUser(groupId, groupUser, seq, master, value, commList);
        }
        return commList;
    }

    /**
     * 计算抽水
     *
     * @param groupId  俱乐部id
     * @param fromUser 计算抽水的提供方
     * @param seq      抽水档序号
     * @param curUser  计算抽水收益方
     * @param value    抽水分
     * @param commList
     * @throws Exception
     */
    public void calcGoldCommissionToUser(long groupId, GroupUser fromUser, int seq, GroupUser curUser, long value, List<GroupGoldLog> commList) throws Exception {
        long curUserId = curUser.getUserId();
        long nextUserId = GroupConstants.getNextId(fromUser, curUser.getPromoterLevel());
        if (nextUserId == 0) {
            commList.add(new GroupGoldLog(Constants.GOLD_LOG_TYPE_COMMISSION, fromUser.getUserId(), fromUser, curUserId, value));
            return;
        }
        GroupUser nextUser = GroupDao.getInstance().loadGroupUser(nextUserId, groupId);
        if (nextUser == null) {
            // 给自己
            commList.add(new GroupGoldLog(Constants.GOLD_LOG_TYPE_COMMISSION, fromUser.getUserId(), fromUser, curUserId, value));
            return;
        }
        nextUserId = nextUser.getUserId();
        GroupGoldCommissionConfig config = GroupDao.getInstance().loadGoldCommissionConfigBySeq(groupId, nextUserId, seq);
        long nowValue = 0;
        if (config == null) {
            //全部给自己
            commList.add(new GroupGoldLog(Constants.GOLD_LOG_TYPE_COMMISSION, fromUser.getUserId(), fromUser, curUserId, value));
            return;
        } else {
            nowValue = config.getValue();
            if (nowValue > 0) {
                if (value < nowValue) {
                    nowValue = value;
                }
                commList.add(new GroupGoldLog(Constants.GOLD_LOG_TYPE_COMMISSION, fromUser.getUserId(), fromUser, curUserId, nowValue));
            }
        }
        long leftValue = value - nowValue;
        if (leftValue <= 0) {
            // 无剩余分数直接返回
            return;
        }
        calcGoldCommissionToUser(groupId, fromUser, seq, nextUser, leftValue, commList);
    }

    public void addGoldRoomBureau(long polyploidNum){
        for (Player p:getPlayerMap().values()) {
            if(p!=null){
                p.getMission().addDayPlayNum(polyploidNum);
                calcGoldRoomActivityGiftCert(p,getGoldRoom());
				GoldRoomActivity_7xi(p,getGoldRoom(),false,getPlayType());//七夕活动
            }
        }
		sendWatchAdsMsg();
    }


    /**
     * 金币场端午活动2020年6月22日
     * @param player
     * @param goldRoom
     */
    protected void calcGoldRoomActivity(Player player, GoldRoom goldRoom) {
        if (isGroupTableGoldRoom() || !isGoldRoom()) {
			LogUtil.msgLog.error("GoldRoomActivity|not gold room |UserId|"+player.getUserId());
            return;
        }
        try {
            List<GoldRoomActivityUserItem> result = GoldRoomActivityDao.getInstance().loadItemByUserId(player.getUserId());
            String datestr = DateFormatUtils.format(new Date(), "yyyyMMdd");
            String cur_keyid = String.valueOf(goldRoom.getConfigId());
            HashMap<String, Object> map = (HashMap<String, Object>) ResourcesConfigsUtil.getGoldRoomActivityConfig(cur_keyid);
            int bean = (int)map.get("bean");
            int rule = (int)map.get("rule");
            String room_lv = (String)map.get("roomlv");
            if (bean == 0 || rule == 0 ||"".equals(room_lv)) {
				LogUtil.msgLog.error("GoldRoomActivity|config error|cur_keyid="+cur_keyid+"|bean="+bean+"|rule="+rule);
                return;
            }
            List<Activity> ac = ActivityUtil.getAllActivityByThem(ActivityUtil.themZongZi);//
            if(null ==ac || ac.size()==0){
				LogUtil.msgLog.error("GoldRoomActivity|activit 102 config is null|"+cur_keyid);
                return;
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            Date beginDate = ac.get(0).getBeginTime();
            Date endDate = ac.get(0).getEndTime();
            Date now = new Date();
            if (now.compareTo(beginDate) >= 0 && now.compareTo(endDate) <= 0) {

            } else {
				LogUtil.msgLog.error("GoldRoomActivity|DateOutTime|gameConfig|now=" + sdf.format(now) + "|ActivityDateBetween[" + beginDate + "-" + endDate + "]");
                return;
            }

            if (null == result || result.size() == 0) {
                GoldRoomActivityUserItem item = new GoldRoomActivityUserItem();
                JSONObject json = new JSONObject();
                json.put("senior",0);
                json.put("novice",0);
                json.put("primary",0);
                json.put("mediate",0);
                json.put(room_lv,1);
                item.setDaterecord(datestr);
                item.setActivityBureau(json.toJSONString());
                item.setActivityItemNum(1);
                item.setActivityDesc("粽子");
                item.setUserid(player.getUserId());
                GoldRoomActivityDao.getInstance().saveGoldRoomActivityUserItem(item);
                player.duanwu_glodRoomActivityNoticeMsg(0,bean);
            } else {
                GoldRoomActivityUserItem item = result.get(0);

                String bureau = item.getActivityBureau();
                JSONObject j = (JSONObject) JSONObject.parse(bureau);
                int bareaunum = Integer.valueOf(j.get(room_lv).toString());
                bareaunum++;
                j.put(room_lv,bareaunum);
                boolean dayfisrtBureau = !item.getDaterecord().contains(datestr);
                item.setActivityBureau(j.toJSONString());
                if (dayfisrtBureau) {
                    //每日首局奖励粽子 根据当前场次
                    item.setActivityItemNum(item.getActivityItemNum() + bean);
                    item.setDaterecord(item.getDaterecord() + "," + datestr);
                    player.duanwu_glodRoomActivityNoticeMsg(0,bean);
                }
                if (bareaunum % rule == 0) {
                    //场级
                    long beannum = item.getActivityItemNum() + bean;
                    item.setActivityItemNum(beannum);
                    player.duanwu_glodRoomActivityNoticeMsg(0,bean);
                }
                GoldRoomActivityDao.getInstance().updateItem(item);
            }
        } catch (Exception e) {
            LogUtil.errorLog.error("GoldRoomActivity|error|",e);
        }
    }

	/**
	 *金币场礼券投放
	 *
	 * @param player
	 * @param goldRoom
	 */
	protected void calcGoldRoomActivityGiftCert(Player player, GoldRoom goldRoom) {
		if (isGroupTableGoldRoom() || !isGoldRoom()) {
			LogUtil.msgLog.error("GoldRoomGiftCertificateActivity|not gold room |UserId|"+player.getUserId());
			return;
		}
		try {
			HashMap<String,Object> userThem = new HashMap<>();
			userThem.put("userid",player.getUserId());
			userThem.put("activityDesc",ActivityUtil.themGiftCertificate);
			List<GoldRoomActivityUserItem> result = GoldRoomActivityDao.getInstance().loadItemByUserIdByThem(userThem);
			String datestr = DateFormatUtils.format(new Date(), "yyyyMMdd");
			String cur_keyid = String.valueOf(goldRoom.getConfigId());
			HashMap<String, Object> map = (HashMap<String, Object>) ResourcesConfigsUtil.getGoldRoomActivityConfig(cur_keyid);
			int bean = (int)map.get("bean");
			int rule = (int)map.get("rule");
			int limitnum = (int)map.get("limitnum");
			String room_lv = (String)map.get("roomlv");
			if (bean == 0 || rule == 0 ||"".equals(room_lv)) {
				//player.writeErrMsg("玩法未配置礼券奖励: 场次:"+room_lv+" cofigId = "+cur_keyid);
				LogUtil.msgLog.error("GoldRoomGiftCertificateActivity|config error|cur_keyid="+cur_keyid+"|bean="+bean+"|rule="+rule+"|room_lv="+room_lv);
				return;
			}
			List<Activity> ac = ActivityUtil.getAllActivityByThem(ActivityUtil.themGiftCertificate);//
			if(null ==ac || ac.size()==0){
				LogUtil.msgLog.error("GoldRoomGiftCertificateActivity|activit 103 config is null|"+cur_keyid);
				return;
			}
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			Date beginDate = ac.get(0).getBeginTime();
			Date endDate = ac.get(0).getEndTime();
			Date now = new Date();
			if (now.compareTo(beginDate) >= 0 && now.compareTo(endDate) <= 0) {
			} else {
				LogUtil.msgLog.error("GoldRoomGiftCertificateActivity|DateOutTime|gameConfig|now=" + sdf.format(now) + "|ActivityDateBetween[" + beginDate + "-" + endDate + "]");
				return;
			}

			if (null == result || result.size() == 0) {
				GoldRoomActivityUserItem item = new GoldRoomActivityUserItem();
				JSONObject json = new JSONObject();
				json.put("senior",0);
				json.put("novice",0);
				json.put("primary",0);
				json.put("mediate",0);
				json.put(room_lv,1);//新增一场数据
				item.setDaterecord(datestr);
				item.setActivityBureau(json.toJSONString());
				item.setActivityItemNum(0);
				item.setEverydayLimit(0);
				item.setActivityDesc(""+ActivityUtil.themGiftCertificate);
				item.setUserid(player.getUserId());
				GoldRoomActivityDao.getInstance().saveGoldRoomActivityUserItem(item);
			} else {
				GoldRoomActivityUserItem item = result.get(0);
				String bureau = item.getActivityBureau();
				JSONObject j = (JSONObject) JSONObject.parse(bureau);
				int bareaunum = Integer.valueOf(j.get(room_lv).toString());
				bareaunum++;
				j.put(room_lv,bareaunum);
				boolean dayfisrtBureau = !item.getDaterecord().contains(datestr);
				item.setActivityBureau(j.toJSONString());
				if (dayfisrtBureau) {
					//每日首局
					item.setDaterecord(datestr);
					// 清空当日获取数量
					item.setEverydayLimit(0);
				}
				if(item.getEverydayLimit()>=limitnum){
					//System.out.println("===============已达上限");
					return;
				}
				if (bareaunum % rule == 0) {
					//场级
					long beannum = item.getActivityItemNum() + bean;
					//每日 日获取数量限定
					int itemlimit = item.getEverydayLimit()+bean;
					if(itemlimit<=limitnum){
						item.setActivityItemNum(beannum);//活动物品总数量
						item.setEverydayLimit(itemlimit);
					}else{
						//礼券溢出处理。
						int yichunum = limitnum-bean;
						bean =yichunum;
						item.setActivityItemNum(item.getActivityItemNum() + bean);//活动物品总数量
						item.setEverydayLimit(limitnum);
					}
					player.changeGiftCert(bean,SourceType.goldRoomGiftCert_award );
					player.duanwu_glodRoomActivityNoticeMsg(0,bean);
				}
				GoldRoomActivityDao.getInstance().updateItem(item);
			}
		} catch (Exception e) {
			LogUtil.errorLog.error("GoldRoomGiftCertificateActivity|error|",e);
		}
	}

	/**
	 * 七夕活动
	 */
	protected  void GoldRoomActivity_7xi(Player player, GoldRoom goldRoom, boolean isCreateRoom,int roomplayType) {
		if (isGroupTableGoldRoom() || !isGoldRoom()) {
			return;
		}
		int themid = ActivityUtil.them7xi;
		if (!checkActivityIsBegin(themid)) {
			return;
		}
		try {
			HashMap<String, Object> userThem = new HashMap<>();
			userThem.put("userid", player.getUserId());
			userThem.put("activityDesc", themid);
			String datestr = DateFormatUtils.format(new Date(), "yyyyMMdd");
			GoldRoomConfig gconfig = GoldRoomUtil.getGoldRoomConfig(goldRoom.getConfigId());
			int rate =  gconfig.getLevel();// 1234 新初中高
			if (rate == 0 ) {
				//player.writeErrMsg("玩法未配置奖励: 场次:"+room_lv+" cofigId = "+cur_keyid+"|roomplayType="+roomplayType);
				LogUtil.msgLog.error("GoldRoomActivity_7xi|config error|cur_keyid=" + gconfig.getKeyId()  +"|roomplayType="+roomplayType);
				return;
			}
			  int cards7 = 0;
			  List<Integer> listcards7 = GoldRoomActivity_7xiCards(new ArrayList<>(player.getHandPais()),roomplayType);
			  cards7 = listcards7.size();
			  List<GoldRoomActivityUserItem> result = GoldRoomActivityDao.getInstance().loadItemByUserIdByThem(userThem);
			  System.out.println(player.getUserId()+" 77777777777777nnumfapai="+cards7+"   "+listcards7.toString());
			  int tongjinum =0;
			  if (isCreateRoom) {
				//新建房间 插入将要获得的7数据
				if (null == result || result.size() == 0) {
					//新增用户数据
					GoldRoomActivityUserItem item = new GoldRoomActivityUserItem();
					JSONObject json = new JSONObject();
					json.put("num", cards7);
					item.setActivityBureau(json.toJSONString());
					item.setActivityItemNum(0);
					item.setActivityDesc(themid + "");
					item.setUserid(player.getUserId());
					GoldRoomActivityDao.getInstance().saveGoldRoomActivityUserItem(item);
				} else {
					//修改用户数据
					GoldRoomActivityUserItem item = result.get(0);
					JSONObject json = new JSONObject();
					json.put("num", cards7);
					item.setActivityBureau(json.toJSONString());
					GoldRoomActivityDao.getInstance().updateItem(item);
				}
			} else {
				//结算运行，
				  GoldRoomActivityUserItem item = result.get(0);
				  String bureau = item.getActivityBureau();
				  JSONObject j = (JSONObject) JSONObject.parse(bureau);
				  int num7 = (int) j.get("num");
				  int total = rate * num7;
				  long oldnum = item.getActivityItemNum();
				  JSONObject json = new JSONObject();
				  json.put("num", 0);
				  item.setActivityItemNum(item.getActivityItemNum() + total);
				  item.setActivityBureau(json.toJSONString());
				  GoldRoomActivityDao.getInstance().updateItem(item);
				  LogUtil.msgLog.info("GoldRoomActivity_7xi|updateItem|succe|" + player.getUserId() + "|oldnum=" + oldnum + "|getnum=" + total + "|totalnum=" + item.getActivityItemNum());
				  //推送获得
				  System.out.println(player.getName() + " rate=" + rate + " num7=" + num7 + " cur_keyid=" + gconfig.getKeyId() + " playrtpe=" + roomplayType);
				  ComRes.Builder builder2 = SendMsgUtil.buildComRes(WebSocketMsgType.cs_7xi_gold_room_calcpushmsg, total);
				  player.writeSocket(builder2.build());
				  //统计
				  player.change7xiItem(total);
			}

		} catch (Exception e) {

		}
	}

	protected  List<Integer> GoldRoomActivity_7xiCards(List<Integer> pais, int roomplayType){
		//玩法ID 117 210
		List mj = new ArrayList();
		mj.add(220);mj.add(221);mj.add(222);

		List pk = new ArrayList();
		pk.add(15);pk.add(16);pk.add(264);

		List phz = new ArrayList();
		phz.add(33);phz.add(53);phz.add(194);
		phz.add(198);phz.add(199);phz.add(226);
		phz.add(229);phz.add(230);phz.add(235);phz.add(250);

		List special = new ArrayList();
		special.add(117) ;special.add(210);
		System.out.println( pais);
		List cards7= new ArrayList();
		if(mj.contains(roomplayType)){
			for (int c :pais) {
				//条7 筒16 万25
				System.out.println();
				if (c==7 ||c==34||c==61||c==88||
						c==16 ||c==43||c==70||c==97 ||
						c==25 ||c==52||c==79||c==106){
					System.out.println("mj:"+c);
					cards7.add(c);
				}
			}
		}

		if(pk.contains(roomplayType)){
			for (int c :pais) {
				if (c % 100 ==7){
					System.out.println("pk:"+c);
					cards7.add(c);
				}
			}
		}
		if(phz.contains(roomplayType)){
			for (int c :pais) {
				if (c % 10 ==7){
					System.out.println("phz:"+c);
					cards7.add(c);
				}
			}
		}
		//打筒子牌 选
		if(special.contains(roomplayType)){
			for (int c :pais) {
				if (c ==307){
					System.out.println("dtz:"+c);
					cards7.add(c);
				}
			}
		}
		return cards7;
	};

	protected boolean checkActivityIsBegin(int activityid){
		List<Activity> ac = ActivityUtil.getAllActivityByThem(activityid);//
		if(null ==ac || ac.size()==0){
			return false;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		Date beginDate = ac.get(0).getBeginTime();
		Date endDate = ac.get(0).getEndTime();
		Date now = new Date();
		if (now.compareTo(beginDate) < 0 || now.compareTo(endDate) > 0) {
			LogUtil.msgLog.error("checkActivityIsBegin|activityid="+activityid+"|now=" + sdf.format(now) + "|ActivityDateBetween[" + beginDate + "-" + endDate + "]");
			return false;
		}
		return true;
	}

	/**
	 *娱乐场单局赢输1000以上。 可观看广告 领取白金都 每日5次
	 *
	 */
	public void sendWatchAdsMsg() {
		if (isGroupTableGoldRoom() || !isGoldRoom()) {
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
		String datestr = sdf.format(now);
		if (now.compareTo(beginDate) >= 0 && now.compareTo(endDate) <= 0) {
		} else {
			LogUtil.msgLog.error("GoldRoomWatchAdsActivity|DateOutTime|now=" + sdf.format(now) + "|ActivityDateBetween[" + beginDate + "-" + endDate + "]");
			return;
		}
		try {
			for (Player player : getPlayerMap().values()) {
				long point = Math.abs(player.getWinGold());
				int param = 0;
				int param2 = 0;
				if (point >= 1000 && point <= 20000) {
					param = 2000;
				} else if (point > 20000) {
					param = 4000;
				} else {
					continue;
				}
				if (player.getWinGold() > 0) {
					param2 = 2;
				} else if (player.getWinGold() < 0) {
					param2 = 3;
				}
				HashMap<String, Object> userThem = new HashMap<>();
				userThem.put("userid", player.getUserId());
				userThem.put("activityDesc", ActivityUtil.themGooldRoomWatchAdsReword);
				List<GoldRoomActivityUserItem> result = GoldRoomActivityDao.getInstance().loadItemByUserIdByThem(userThem);
				if (null == result || result.size() == 0 || !result.get(0).getDaterecord().contains(datestr)) {
					ComRes.Builder builder = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_goldRoomSendWatchAdsMsg, param, param2);
					System.out.println(builder.toString());
					player.writeSocket(builder.build());
					LogUtil.msgLog.info("");
				} else {
					GoldRoomActivityUserItem item = result.get(0);
					String str = item.getActivityBureau();
					JSONObject json = JSONObject.parseObject(str);
					if (player.getWinGold() > 0) {
						int winnum = json.getIntValue("win");
						if (winnum >= 5) {
							continue;
						}
					} else if (player.getWinGold() < 0) {
						int winnum = json.getIntValue("lose");
						if (winnum >= 5) {
							continue;
						}
					}
					ComRes.Builder builder = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_goldRoomSendWatchAdsMsg, param, param2);
					System.out.println(builder.toString());
					player.writeSocket(builder.build());
				}
			}
		} catch (Exception e) {
			LogUtil.errorLog.error("GoldRoomSendWatchAdsMsg|error|", e);
		}
	}

	protected void calcGoldRoom() {
        try {
            if (!isGoldRoom()) {
                return;
            }
            long totalWin = 0;
            // 算最多赢多少分
            List<Player> winList = new ArrayList<>();
            for (Player player : getSeatMap().values()) {
                if (player.getWinGold() > 0) {
                    winList.add(player);
                    //刷新累计赢次数
                    player.updateTwinRewardCount();
                }
            }

            int loseGoldCount = getSeatMap().size()-winList.size();
            long maxTotalWin = 0;
            long maxTotalLose = 0;
            for (Player player : getSeatMap().values()) {
                // 金币场倍率
                long winGold = player.getWinGold()* getGoldRoom().getRate();
                long havingGold = player.loadAllGolds();
                if (winGold > 0) {
                	if (havingGold*loseGoldCount <= winGold) {
                		winGold = havingGold*loseGoldCount;
//                		fengDing = true;
                		player.setGoldResult(2);// 封顶
                	}
                	maxTotalWin+=winGold;
                }else{
                	if (havingGold < -winGold) {
                		winGold = -havingGold;
                	}
                	maxTotalLose +=winGold;
                }
            }

            long loseFengDing = 0;
            //输的分比赢的多，那就少输,赢的封顶 , 如果输的分比赢的分少，下面会优先算输的总共多少分，再分给赢的人
            if(maxTotalWin<=-maxTotalLose){
            	loseFengDing =maxTotalWin/loseGoldCount;
            }
            for (Player player : getSeatMap().values()) {
                // 金币场倍率
                player.setWinGold(player.getWinGold() * getGoldRoom().getRate());
                long winGold = player.getWinGold();
                if (winGold < 0) {
                    long havingGold = player.loadAllGolds();
                  //实际输的最大，应该是赢最多的平均数
                	if(loseFengDing>0&&loseFengDing<=-winGold){
                		winGold =-loseFengDing;
                	}
                    if (winGold + havingGold < 0) {
                        winGold = -havingGold;
                        player.setWinGold(winGold);
                        player.setGoldResult(1);//破产
                    }else{
                    	 player.setWinGold(winGold);
                    }
                    totalWin += -winGold;
                    player.setTotalPoint((int) player.getWinGold());
                    player.changeGold(winGold, playType, SourceType.table_win);
                    player.calcGoldResult(winGold);
                }

            }
            Collections.sort(winList, new Comparator<Player>() {
                @Override
                public int compare(Player o1, Player o2) {
                    return Long.valueOf(o2.getWinGold() - o1.getWinGold()).intValue();
                }
            });

            // 算赢家
			for (Player player : winList) {
				long winGold = player.getWinGold();
				if (winGold > 0 && totalWin > 0) {
					if (totalWin > winGold) {
						totalWin -= winGold;
					} else {
						winGold = totalWin;
						totalWin = 0;
					}

					setGoldWinPoint(player, winGold);
				}else{
					setGoldWinPoint(player, 0);
				}
			}
            addGoldRoomBureau(getGoldRoom().getRate());

        } catch (Exception e) {
            LogUtil.errorLog.error("calcGoldRoom|error|" + getId(), e);
        }
    }



    private void setGoldWinPoint(Player player, long winGold) {
		player.setWinGold(winGold);
		player.setTotalPoint((int) player.getWinGold());
		player.changeGold(winGold, playType, SourceType.table_win);
		player.calcGoldResult(winGold);
	}

	
	
	

    public List<LogGroupGoldCommission> calcLogGroupGoldCommission(Long dataDate, Player player, GroupUser groupUser, Long commission) {
        List<LogGroupGoldCommission> list = new ArrayList<>();
        LogGroupGoldCommission tmp = new LogGroupGoldCommission(dataDate, groupUser.getGroupId(), player.getUserId());
        LogGroupGoldCommission logSelf = new LogGroupGoldCommission(dataDate, groupUser.getGroupId(), player.getUserId());

        if (commission > 0) {
            tmp.setCommission(commission);
            logSelf.setSelfCommission(commission);
        }
        tmp.setZjsCount(1);

        logSelf.setSelfZjsCount(1);
        logSelf.setSelfCommissionCount(1);
        list.add(logSelf);
        LogGroupGoldCommission log = null;
        if (groupUser.getPromoterId1() > 0) {
            log = tmp.clone();
            log.setUserId(groupUser.getPromoterId1());
            list.add(log);
        } else {
            return list;
        }
        if (groupUser.getPromoterId2() > 0) {
            log = tmp.clone();
            log.setUserId(groupUser.getPromoterId2());
            list.add(log);
        } else {
            return list;
        }
        if (groupUser.getPromoterId3() > 0) {
            log = tmp.clone();
            log.setUserId(groupUser.getPromoterId3());
            list.add(log);
        } else {
            return list;
        }
        if (groupUser.getPromoterId4() > 0) {
            log = tmp.clone();
            log.setUserId(groupUser.getPromoterId4());
            list.add(log);
        } else {
            return list;
        }
        if (groupUser.getPromoterId5() > 0) {
            log = tmp.clone();
            log.setUserId(groupUser.getPromoterId5());
            list.add(log);
        } else {
            return list;
        }
        if (groupUser.getPromoterId6() > 0) {
            log = tmp.clone();
            log.setUserId(groupUser.getPromoterId6());
            list.add(log);
        } else {
            return list;
        }
        if (groupUser.getPromoterId7() > 0) {
            log = tmp.clone();
            log.setUserId(groupUser.getPromoterId7());
            list.add(log);
        } else {
            return list;
        }
        if (groupUser.getPromoterId8() > 0) {
            log = tmp.clone();
            log.setUserId(groupUser.getPromoterId8());
            list.add(log);
        } else {
            return list;
        }
        if (groupUser.getPromoterId9() > 0) {
            log = tmp.clone();
            log.setUserId(groupUser.getPromoterId9());
            list.add(log);
        } else {
            return list;
        }
        if (groupUser.getPromoterId10() > 0) {
            log = tmp.clone();
            log.setUserId(groupUser.getPromoterId10());
            list.add(log);
        }
        return list;
    }

    public long getGoldRoomId() {
        return goldRoomId;
    }

    public void setGoldRoomId(long goldRoomId) {
        this.goldRoomId = goldRoomId;
    }

    public int getSoloRoomType() {
        return soloRoomType;
    }

    public void setSoloRoomType(int soloRoomType) {
        this.soloRoomType = soloRoomType;
    }

    public long getSoloRoomValue() {
        return soloRoomValue;
    }

    public void setSoloRoomValue(long soloRoomValue) {
        this.soloRoomValue = soloRoomValue;
    }

    protected void calcSoloRoom() {
        try {
            if (!isSoloRoom()) {
                return;
            }
            // 先算输家
            boolean lostOk = false;
            for (Player player : getSeatMap().values()) {
                if (!player.isSoloWinner()) {
                    lostOk = player.changeGold(-soloRoomValue, playType, SourceType.solo_room);
                    if (lostOk) {
                        player.setTotalPoint((int) -soloRoomValue);
                    } else {
                        player.setTotalPoint(0);
                    }
                    break;
                }
            }
            // 再算赢家
            for (Player player : getSeatMap().values()) {
                if (player.isSoloWinner()) {
                    if (lostOk) {
                        player.changeGold(soloRoomValue, playType, SourceType.solo_room);
                        player.setTotalPoint((int) soloRoomValue);
                    } else {
                        player.setTotalPoint(0);
                        player.setSoloWinner(false);
                    }
                    break;
                }
            }
        } catch (Exception e) {
            LogUtil.errorLog.error("calcSoloRoom|error|" + getId(), e);
        }
    }

    public String getMasterName() {
        if(StringUtils.isNotBlank(masterName)){
            return masterName;
        }
        long userId = masterId;
        if (isGroupRoom()) {
            userId = creatorId;
        }
        Player master = PlayerManager.getInstance().getPlayer(userId);
        if (master == null) {
            RegInfo info = UserDao.getInstance().selectUserByUserId(userId);
            if (info != null) {
                masterName = info.getName();
            }
        } else {
            masterName = master.getName();
        }
        return masterName;
    }

    /**
     * 初始化金币房配置
     * @param goldMsg
     */
    public void initGroupTableGoldMsg(String goldMsg) {
        if (StringUtils.isBlank(goldMsg)) {
            return;
        }
        String[] params = goldMsg.split(",");
        if (params.length < 4) {
            return;
        }
        this.gtgMode = StringUtil.getIntValue(params, 0, 0);
        this.gtgJoinLimit = StringUtil.getIntValue(params, 1, 0);
        this.gtgDissLimit = StringUtil.getIntValue(params, 2, 0);
        this.gtgDifen = StringUtil.getIntValue(params,3, 0);
        changeExtend();
    }

    /**
     * 是否是亲友圈金币房
     *
     * @return
     */
    public boolean isGroupTableGoldRoom() {
        return isGroupRoom() && gtgMode == 1;
    }

    /**
     * 赢分不能超过玩家本身进房间所带的比赛分
     */
    public void calcGroupTableGoldRoomWinLimit() {
        if (!isGroupTableGoldRoom()) {
            return;
        }

        // 亲友圈金币房收门票
        consumeForGroupGoldRoom();

        int totalWin = 0;// 赢分的玩家总分
        int totalLose = 0;// 输分玩家的总分
        int losePlayerCount = 0; // 输分玩家数
        long maxLose = 0;  // 最大输分数

        List<Player> loseList = new ArrayList<>();
        for (Player player : getSeatMap().values()) {
            if (player.getWinGold() > 0) {
                long haveValue = player.loadAllGolds();
                if (player.getWinGold() > haveValue) {
                    player.setWinGold(haveValue);
                }
                totalWin += player.getWinGold();
            } else {
                totalLose += player.getWinGold();
                losePlayerCount++;
                maxLose = player.getWinGold() < maxLose ? player.getWinGold() : maxLose;
                loseList.add(player);
            }
        }

        //赢的总分与输的总分不一至时 退还输分玩家多输的分数
        if (Math.abs(totalLose) > totalWin) {
            // 输分从小到大
            Collections.sort(loseList, new Comparator<Player>() {
                @Override
                public int compare(Player o1, Player o2) {
                    if(o2.getWinGold() > o1.getWinGold()){
                        return 1;
                    }else if(o2.getWinGold() == o1.getWinGold()){
                        return 0;
                    }else{
                        return -1;
                    }
                }
            });

            int value = totalWin / losePlayerCount;
            int leftValue = totalWin % losePlayerCount;
            // 即若一个输家就都是退给他，如果是两个输家就相当于用赢分平均分摊给两个输家，前提为输家本身输的分>均摊分，若小于则只输本身输分，其他都退给另外一个输家
            for (Player player : loseList) {
                if (Math.abs(player.getWinGold()) < value) {
                    totalWin -= Math.abs(player.getWinGold());
                    losePlayerCount--;
                    value = totalWin / losePlayerCount;
                    leftValue = totalWin % losePlayerCount;
                } else {
                    player.setWinGold(-value);
                }
            }
            if (leftValue > 0) {
                Player maxLoser = loseList.get(loseList.size() - 1);
                maxLoser.setWinGold(maxLoser.getWinGold() - leftValue);
            }
        }
    }

    public void calcGroupTableGoldRoomOver() {
        try {
            if (!isGroupTableGoldRoom()) {
                return;
            }
            long totalWin = 0;
            // 算输家
            List<Player> winList = new ArrayList<>();
            for (Player player : getSeatMap().values()) {
                // 金币场倍率
                player.setWinGold(player.getWinGold());
                long loseGold = player.getWinGold();
                if (loseGold < 0) {
                    boolean ret = player.changeGold(loseGold, playType, SourceType.groupTableGoldRoom);
                    while (!ret) {
                        player.refreshGoldFromDb();
                        loseGold = -player.loadAllGolds();
                        ret = player.changeGold(loseGold, playType, SourceType.groupTableGoldRoom);
                        player.setWinGold(loseGold);
                    }
                    totalWin += -loseGold;
                } else {
                    winList.add(player);
                }
            }
            Collections.sort(winList, new Comparator<Player>() {
                @Override
                public int compare(Player o1, Player o2) {
                    return Long.valueOf(o1.getWinGold() - o2.getWinGold()).intValue();
                }
            });

            // 算赢家
            for (Player player : winList) {
                long winGold = player.getWinGold();
                if (winGold > 0 && totalWin > 0) {
                    if (totalWin > winGold) {
                        totalWin -= winGold;
                    } else {
                        winGold = totalWin;
                        totalWin = 0;
                    }
                    player.changeGold(winGold, playType, SourceType.groupTableGoldRoom);
                    player.setWinGold(winGold);
                }
            }

            Long dataDate = Long.valueOf(new SimpleDateFormat("yyyyMMdd").format(new Date()));
            long groupId = loadGroupIdLong();
            List<LogGroupGoldWin> logList = new ArrayList<>();
            for (Player player : getSeatMap().values()) {
                logList.add(new LogGroupGoldWin(dataDate, groupId, player.getUserId(), player.getWinGold()));
            }
            LogDao.getInstance().saveLogGroupGoldWin(logList);

        } catch (Exception e) {
            LogUtil.errorLog.error("calcGoldRoom|error|" + getId(), e);
        }
    }

    /**
     * 开始下一局前检查是否可以解散
     * 发牌前检查玩家身上信用分是否满足最低信用分需求
     *
     * @return
     */
    public synchronized boolean checkGoldOnStartNext() {
        if (playBureau == 1) {
            return true;
        }
        if(!isGroupTableGoldRoom()){
            return true;
        }
        try {
            String disPlayerNames = "";
            StringBuilder sb = new StringBuilder("checkGoldOnStartNext");
            sb.append("|").append(getId());
            sb.append("|").append(getPlayBureau());
            sb.append("|").append(gtgDissLimit);
            sb.append("|");
            for (Player player : getSeatMap().values()) {
                if (player.loadAllGolds() < creditDissLimit) {
                    disPlayerNames += player.getName() + ",";
                    sb.append(player.getUserId()).append(",").append(player.loadAllGolds()).append(",").append(player.getWinGold()).append(";");
                }
            }
            if (!"".equals(disPlayerNames)) {
                LogUtil.msgLog.info(sb.toString());
                isDissByCreditLimit = true;
                creditLimitPlayerNames = disPlayerNames;
                disPlayerNames = disPlayerNames.substring(0, disPlayerNames.length() - 1);
                for (Player player : getSeatMap().values()) {
                    player.writeErrMsg(LangMsg.code_75, disPlayerNames, MathUtil.formatCredit(gtgDissLimit));
                }
                for (Player player : getRoomPlayerMap().values()) {
                    player.writeErrMsg(LangMsg.code_75, disPlayerNames, MathUtil.formatCredit(gtgDissLimit));
                }
                try {
                    sendAccountsMsg();
                } catch (Throwable e) {
                    LogUtil.errorLog.error("tableId=" + id + ",total calc Exception:" + e.getMessage(), e);
                    GeneratedMessage errorMsg = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_err, "结算异常，房间" + id + "被解散").build();
                    for (Player player : getPlayerMap().values()) {
                        player.writeSocket(errorMsg);
                    }
                }
                setSpecialDiss(1);
                setTiqianDiss(true);
                calcOver3();
                LogUtil.msgLog.info("BaseTable|dissReason|checkGoldOnStartNext|1|" + getId() + "|" + getPlayBureau() + "|" + disPlayerNames);
                diss();
                return false;
            }
        } catch (Exception e) {
            LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
        }
        return true;
    }

    public boolean checkCanStartNext() {
        // 检查信用分
        if (!checkCreditOnStartNext()) {
            return false;
        }
        // 亲友圈金币房模式，检查金币
        if (!checkGoldOnStartNext()) {
            return false;
        }
        return true;
    }

    public boolean consumeForGroupGoldRoom() {
        if (!isGroupTableGoldRoom()) {
            return true;
        }
        if (playBureau != 1) {
            return true;
        }
        if (payType == PayConfigUtil.PayType_Client_AA_Gold) {
            int needGold = loadPayConfig();
            if (needGold > 0) {
                List<Player> consumed = new ArrayList<>();
                for (Player player : getPlayerMap().values()) {
                    boolean res = player.changeGold(-needGold, playType, SourceType.AA_Gold);
                    if (!res) {
                        for (Player player1 : consumed) {
//                            player1.changeGold(needGold, playType, SourceType.AA_Gold_Return);
                        }
                        return false;
                    } else {
                        consumed.add(player);
                    }
                }
            }
        }
        return true;
    }

	public CompetitionTmpPlayer competitionPointConvertRate(CompetitionTmpPlayer next) {
		int pointTemp = next.getPoint();
		next.setPoint(next.getPoint() * (int) getCompetitionRoom().getRate());
		//把上面扣除或增加的先补回来
		next.setTotalPoint(next.getTotalPoint() - pointTemp);
		//换算倍率后再扣除
		next.setTotalPoint(next.getTotalPoint() + next.getPoint());
		return next;
	}


	public boolean isXipai() {
		return isXipai;
	}

	public int getXipaiScoure() {
		return xipaiScoure;
	}

	public List<String> getXipaiName() {
		return xipaiName;
	}

	public void cleanXipaiName() {
		this.xipaiName.clear();
	}

	public void addXipaiName(String name){
    	if(xipaiName == null){
			xipaiName =new ArrayList<>();
		}
    	xipaiName.add(name);
	}

	/**2020年11月10日  添加回放日志 额外需求*/
	public String extendLogDeal(String res){
		if(!isCreditTable()){
			return  res;
		}
		JSONArray array = JSONArray.parseArray(res);
		for (int i=0;i<array.size();i++  ) {
			JSONObject object =  JSONObject.parseObject(array.get(i).toString());
			int seat2 =  object.getInteger("seat");
			if(null!=creditMap.get(seat2)){
				object.put("credit",creditMap.get(seat2));//添加牌局开始前信用分记录
			}else{
				object.put("credit",0l);
			}
			array.set(i,object.toJSONString());
		}
		String returnStr = array.toJSONString();
		creditMap.clear();
		return returnStr;
	}

}
