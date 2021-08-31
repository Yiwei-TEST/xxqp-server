package com.sy599.game.qipai.pdkuai.bean;

import com.google.protobuf.GeneratedMessage;
import com.sy.mainland.util.CommonUtil;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.FirstmythConstants;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.common.constant.SharedConstants.player_state;
import com.sy599.game.db.bean.RegInfo;
import com.sy599.game.db.bean.group.GroupUser;
import com.sy599.game.db.dao.UserDatasDao;
import com.sy599.game.db.dao.gold.GoldRoomDao;
import com.sy599.game.db.dao.group.GroupDao;
import com.sy599.game.jjs.bean.MatchBean;
import com.sy599.game.jjs.util.JjsUtil;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.TableRes.ClosingPlayerInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes;
import com.sy599.game.qipai.pdkuai.command.PdkCommandProcessor;
import com.sy599.game.qipai.pdkuai.constant.PdkConstants;
import com.sy599.game.qipai.pdkuai.tool.CardTypeTool;
import com.sy599.game.qipai.pdkuai.util.CardUtils;
import com.sy599.game.robot.RobotManager;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.util.StringUtil;
import com.sy599.game.util.TimeUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class PdkPlayer extends Player {
	// private long tableId;
	// private long roomId;
	// 座位id
	private volatile int seat;
	// 状态
	private volatile player_state state;// 1进入 2已准备 3正在玩 4已结束
	private int isEntryTable;
	private List<Integer> handPais;
	private List<List<Integer>> outPais;
	private int boomCount;
	private int winCount;
	private int lostCount;
	private int point;
	private int playPoint;
	private int playBoomPoint;
	private int isNoLet;
	private int cutCard;// 是否需要切牌
	private int redTenPai;//0没红10 1有红10


	private volatile boolean autoPlay = false;//托管
	private volatile long lastOperateTime = 0;//最后操作时间
	private volatile long lastCheckTime = 0;//最后检查时间
	private volatile long nextAutoDisCardTime = 0;

	private volatile long autoPlayTime = 0;//自动操作时间

	private int currentLs;//当前连胜
	private int maxLs;//最大连胜
	private int niaoFen = -1; //打鸟分：-1未选择，0不打鸟，>0选择的打鸟分
	//飘分分数
	private int piaoFen=-1;
	//是否已经点击飘分
	private boolean alreadyPiaoFen=false;
	//飘分结算输赢分
	private int winLostPiaoFen=0;
	
	private int currentLshu;//当前连输
	//如果可以出，系统帮助出最后一手牌
	private List<Integer> autoFinalCards=new ArrayList<>();

	//首局洗牌的选择  -1 未选, 0不洗, 1洗
	private int firstXipai=-1;

	public long getNextAutoDisCardTime() {
		return nextAutoDisCardTime;
	}

	public void setNextAutoDisCardTime(long nextAutoDisCardTime) {
		this.nextAutoDisCardTime = nextAutoDisCardTime;
	}

	@Override
	protected void loadFromDB0(RegInfo info) {
		if (UserDatasDao.getInstance().exists()){
			String val = UserDatasDao.getInstance().selectUserDataValue(String.valueOf(userId),PdkTable.GAME_CODE,"all","currentLs");
			if (CommonUtil.isPureNumber(val)){
				currentLs = Integer.parseInt(val);
			}else{
				UserDatasDao.getInstance().saveUserDatas(String.valueOf(userId),PdkTable.GAME_CODE,"all","currentLs","0");
			}

			val = UserDatasDao.getInstance().selectUserDataValue(String.valueOf(userId),PdkTable.GAME_CODE,"all","maxLs");
			if (CommonUtil.isPureNumber(val)){
				maxLs = Integer.parseInt(val);
			}else{
				UserDatasDao.getInstance().saveUserDatas(String.valueOf(userId),PdkTable.GAME_CODE,"all","maxLs","0");
			}
		}
	}

	public int getCurrentLs() {
		return currentLs;
	}

	public void setCurrentLs(int currentLs) {
		this.currentLs = currentLs;
	}

	public int getMaxLs() {
		return maxLs;
	}

	public void setMaxLs(int maxLs) {
		this.maxLs = maxLs;
	}

	public long getAutoPlayTime() {
		return autoPlayTime;
	}

	public void setAutoPlayTime(long autoPlayTime) {
		this.autoPlayTime = autoPlayTime;
	}

	public long getLastCheckTime() {
		return lastCheckTime;
	}

	public void setLastCheckTime(long lastCheckTime) {
		this.lastCheckTime = lastCheckTime;
	}

	public boolean isAutoPlay() {
		return autoPlay;
	}

	public void setAutoPlay(boolean autoPlay,BaseTable table) {
		if (this.autoPlay != autoPlay && !isRobot()){
			ComMsg.ComRes.Builder res = SendMsgUtil.buildComRes(132, seat,autoPlay?1:0, (int)userId);
			GeneratedMessage msg = res.build();
			for (Map.Entry<Long,Player> kv:table.getPlayerMap().entrySet()){
				Player player=kv.getValue();
				if (player.getIsOnline() == 0) {
					continue;
				}
				player.writeSocket(msg);
			}
			if(!getHandPais().isEmpty()){
				table.addPlayLog(getSeat(), PdkConstants.action_tuoguan + "",(autoPlay?1:0) + "");
			}
		}
		boolean addLog = this.autoPlay != autoPlay;
		this.autoPlay = autoPlay;
		if(!autoPlay){
			setAutoPlayCheckedTimeAdded(false);
		}

		if(addLog) {
			StringBuilder sb = new StringBuilder("Pdk");
			if (table != null) {
				sb.append("|").append(table.getId());
				sb.append("|").append(table.getPlayBureau());
			} else {
				sb.append("|").append(-1);
			}
			sb.append("|").append(this.getUserId());
			sb.append("|").append(this.getSeat());
			sb.append("|").append(this.isAutoPlay() ? 1 : 0);
			sb.append("|").append("setAutoPlay");
			sb.append("|").append(autoPlay);
			LogUtil.msgLog.info(sb.toString());
		}
	}

	public long getLastOperateTime() {
		return lastOperateTime;
	}

	public void setLastOperateTime(long lastOperateTime) {
		this.lastCheckTime = 0;
		this.lastOperateTime = lastOperateTime;
		this.autoPlayTime = 0;
	}

	public int getWinLostPiaoFen() {
		return winLostPiaoFen;
	}

	public void setWinLostPiaoFen(int winLostPiaoFen) {
		this.winLostPiaoFen = winLostPiaoFen;
	}

	public int getRedTenPai() {
		return redTenPai;
	}

	public void setRedTenPai(int redTenPai) {
		this.redTenPai = redTenPai;
	}

	public PdkPlayer() {
		// this.tableId = tableId;
		// this.userId = userId;
		// this.roomId = roomId;
		handPais = new ArrayList<Integer>();
		outPais = new ArrayList<List<Integer>>();
	}

	public int getSeat() {
		return seat;
	}

	public void setSeat(int seat) {
		this.seat = seat;
	}

	public void initPais(String hand, String out) {
		if (!StringUtils.isBlank(hand)) {
			this.handPais = StringUtil.explodeToIntList(hand);

		}
		if (!StringUtils.isBlank(out)) {
			this.outPais = StringUtil.explodeToLists(out);

		}
	}

	public void initPais(List<Integer> hand, List<List<Integer>> out) {
		if (hand != null) {
			this.handPais = hand;

		}
		if (out != null) {
			this.outPais = out;

		}
	}

	public void dealHandPais(List<Integer> pais,PdkTable table) {
		this.table = table;
		this.handPais = pais;

		Collections.sort(pais, new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return CardUtils.loadCardValue(o2) - CardUtils.loadCardValue(o1);
			}
		});

		table.changeCards(seat);
	}

	public List<Integer> getHandPais() {
		return handPais;
	}

	public List<List<Integer>> getOutPais() {
		return outPais;
	}

	/**
	 * 是否出过牌用于结算
	 *
	 * @return
	 */
	public boolean isOutCards() {
		boolean isOut = false;
		for (List<Integer> list : outPais) {
			if (!list.isEmpty() && !list.contains(0)) {
				isOut = true;
			}

		}
		return isOut;
	}
	
//	public boolean isRobot() {
//		return getFlatId().endsWith("zd1000");
//	}

	public void addOutPais(List<Integer> cards ,PdkTable table) {
		this.table = table;
		handPais.removeAll(cards);
		outPais.add(cards);
		table.changeCards(seat);
	}

	public String toInfoStr() {
		StringBuilder sb = new StringBuilder();
		sb.append(getUserId()).append(",");
		sb.append(seat).append(",");
		int stateVal = 0;
		if (state != null) {
			stateVal = state.getId();
		}
		sb.append(stateVal).append(",");
		sb.append(isEntryTable).append(",");
		sb.append(boomCount).append(",");
		sb.append(getTotalBoom()).append(",");
		sb.append(winCount).append(",");
		sb.append(lostCount).append(",");
		sb.append(point).append(",");
		sb.append(loadScore()).append(",");
		sb.append(isNoLet).append(",");
		sb.append(playPoint).append(",");
		sb.append(cutCard).append(",");
		sb.append(playBoomPoint).append(",");
		sb.append(redTenPai).append(",");
		sb.append(niaoFen).append(",");
		sb.append(piaoFen).append(",");
		sb.append(alreadyPiaoFen?1:0).append(",");
		sb.append(currentLshu).append(",");
		sb.append(StringUtil.implode(autoFinalCards, "_")).append(",");
		sb.append(firstXipai).append(",");
		return sb.toString();
	}

	@Override
	public void initPlayInfo(String data) {
		if (!StringUtils.isBlank(data)) {
			int i = 0;
			String[] values = data.split(",");
			long duserId = StringUtil.getLongValue(values, i++);
			if (duserId != getUserId()) {
				return;
			}
			this.seat = StringUtil.getIntValue(values, i++);
			int stateVal = StringUtil.getIntValue(values, i++);
			this.state = SharedConstants.getPlayerState(stateVal);
			this.isEntryTable = StringUtil.getIntValue(values, i++);
			this.boomCount = StringUtil.getIntValue(values, i++);
			setTotalBoom(StringUtil.getIntValue(values, i++));
			this.winCount = StringUtil.getIntValue(values, i++);
			this.lostCount = StringUtil.getIntValue(values, i++);
			this.point = StringUtil.getIntValue(values, i++);
			setTotalPoint(StringUtil.getIntValue(values, i++));
			this.isNoLet = StringUtil.getIntValue(values, i++);
			this.playPoint = StringUtil.getIntValue(values, i++);
			if (playPoint == 0 && this.point != 0) {
				playPoint = point;
			}
			this.cutCard = StringUtil.getIntValue(values, i++);
			this.playBoomPoint = StringUtil.getIntValue(values, i++);
			this.redTenPai = StringUtil.getIntValue(values, i++);
			this.niaoFen = StringUtil.getIntValue(values, i++);
			this.piaoFen = StringUtil.getIntValue(values, i++);
			this.alreadyPiaoFen = StringUtil.getIntValue(values, i++)==1;
			this.currentLshu = StringUtil.getIntValue(values, i++);
			String str = StringUtil.getValue(values, i++);
			if (!StringUtils.isBlank(str)) {
				this.autoFinalCards = StringUtil.explodeToIntList(str, "_");
			}
			this.firstXipai=StringUtil.getIntValue(values,i++);
		}
	}

	public player_state getState() {
		return state;
	}

	public void changeState(player_state state) {
		this.state = state;
		changeTableInfo();
	}

	public int getIsEntryTable() {
		return isEntryTable;
	}

	public void setIsEntryTable(int isEntryTable) {
		this.isEntryTable = isEntryTable;
		changeTableInfo();
	}

	public PlayerInTableRes.Builder buildPlayInTableInfo() {
		return buildPlayInTableInfo(0, false);
	}

	/**
	 * @param isrecover
	 *            是否重连
	 * @return
	 */
	public PlayerInTableRes.Builder buildPlayInTableInfo(long recUserId, boolean isrecover) {
		PlayerInTableRes.Builder res = PlayerInTableRes.newBuilder();
		res.setUserId(this.userId + "");
		if (!StringUtils.isBlank(ip)) {
			res.setIp(ip);

		} else {
			res.setIp("");
		}
		res.setName(name);
		res.setSeat(seat);
		res.setSex(sex);
		res.setPoint(loadScore());
		if (!StringUtils.isBlank(getHeadimgurl())) {
			res.setIcon(getHeadimgurl());

		} else {
			res.setIcon("");
		}
		if (state == player_state.ready || state == player_state.play) {
			// 玩家装备已经准备和正在玩的状态时通知前台已准备
			res.setStatus(SharedConstants.state_player_ready);
		} else {
			res.setStatus(0);

		}
		PdkTable table = getPlayingTable(PdkTable.class);
		if (table == null) {
			LogUtil.e("userId="+this.userId+"table null-->" + getPlayingTableId());
			return null;
		}

		if (isrecover) {
			// 0是否要的起 1是否报单 2是否暂离(1暂离0在线)
			List<Integer> recover = new ArrayList<>();

			int nowDisSeat = table.getNextDisCardSeat();
			if (recUserId == this.userId && table.getDisCardSeat() != seat && nowDisSeat == seat && table.getNowDisCardIds().size()>0) {
				// 自己重连,现在轮到自己出牌,去打别人出的牌
				List<Integer> list = CardTypeTool.canPlay(handPais, table.getNowDisCardIds(),false,this,table);
				if (list==null||list.size()==0) {
					recover.add(0);
				} else {
					CardUtils.Result result = CardUtils.calcCardValue(CardUtils.loadCards(list),table.getSiDai(),table.getIsFirstCardType32()==1,table.getAAAZha()==1);
					int temp = 1;

					if (result.getType()==3||result.getType()==33){
						if (list.size()%5!=0){
							if (list.size()==handPais.size()){
								if (table.getCard3Eq()==1){
									temp=0;
								}
							}else{
								temp=0;
							}
						}
					}

					recover.add(temp);
				}
			} else {
				if (isNoLet == 1) {
					// 是否要不起
					recover.add(0);
				} else {
					// 1要的起
					recover.add(1);
				}
			}

			if (handPais.size() == 1) {
				recover.add(1);
			} else {
				recover.add(0);
			}
			recover.add(isEntryTable);
			res.addAllRecover(recover);
		}
		List<Integer> extList = new ArrayList<>();
		extList.add(cutCard);
		int cardNumber = this.getHandPais().size();
		if (0 == cardNumber) {
			cardNumber = table.getPlayType();
		}
		extList.add(cardNumber);
		if(table.getMasterId() == userId){
			extList.add(1);
		}else{
			extList.add(0);
		}

		MatchBean matchBean = table.isMatchRoom()?JjsUtil.loadMatch(table.getMatchId()):null;
		if (matchBean!=null){
			extList.add(matchBean.loadUserScore(userId));//3
			int gameNo = JjsUtil.loadMatchCurrentGameNo(matchBean);
			extList.add(gameNo);//4
			extList.add(JjsUtil.loadMatchCurrentGameWinCount(matchBean,String.valueOf(gameNo)));//5
			if (gameNo>0){
				extList.add(JjsUtil.loadMatchCurrentGameWinCount(matchBean,String.valueOf(gameNo-1)));
				extList.add(table.getPlayBureau());
			}else{
				extList.add(JjsUtil.loadMaxRestUserCount(matchBean,String.valueOf(gameNo)));
				extList.add(GoldRoomDao.getInstance().loadUserRoomCount(table.getModeId(),userId));
			}
			extList.add(isAutoPlay()&&!isRobot()?1:0);//8
			extList.add(matchBean.loadUserScore(userId));//9
			int[] rank = matchBean.loadUserRank(gameNo,String.valueOf(userId));
			extList.add(rank[0]);//10
			extList.add(rank[1]);//11
		}else{
			extList.add(table.isGoldRoom()?(int)loadAllGolds():0);//3
			extList.add(0);
			extList.add(0);
			extList.add(0);
			extList.add(0);
			extList.add(isAutoPlay()&&!isRobot()?1:0);//8
			extList.add(0);//9
			extList.add(0);
			extList.add(0);//11
		}

		int tuoGuanCountDown = table.getAutoTimeOut();
		if (nextAutoDisCardTime > 0) {
			tuoGuanCountDown = (int) (nextAutoDisCardTime - TimeUtil.currentTimeMillis());
			if (tuoGuanCountDown < 0) {
				tuoGuanCountDown = 0;
			}
		}
		extList.add(tuoGuanCountDown);//12 托管倒计时
		extList.add(autoPlay ? 1 : 0);//13 是否托管状态
		extList.add(niaoFen);         //14 鸟分
		extList.add(piaoFen);         //15 飘分
		extList.add(firstXipai);	//16首局洗牌的选择
		res.addAllExt(extList);

		//信用分
		if(table.isCreditTable()) {
			GroupUser gu = getGroupUser();
			String groupId = table.loadGroupId();
			if (gu == null || !groupId.equals(gu.getGroupId() + "") || gu.getTempCredit() > 0) {
				gu = GroupDao.getInstance().loadGroupUser(getUserId(), groupId);
				setGroupUser(gu);
			}
			res.setCredit(gu != null ? gu.getCredit() : 0);
		}
		return buildPlayInTableInfo1(res);
	}

	public int getBoomCount() {
		return boomCount;
	}

	public void setBoomCount(int boomCount) {
		this.boomCount = boomCount;
	}

	public void changeBoomCount(int boomCount) {
		this.boomCount += boomCount;
		myExtend.setPdkFengshen(FirstmythConstants.firstmyth_index3, boomCount);
		changeTotalBoom(boomCount);
		changeTableInfo();
	}

	public int getWinCount() {
		return winCount;
	}

	public void setWinCount(int winCount) {
		this.winCount = winCount;
	}

	public int getLostCount() {
		return lostCount;
	}

	public void setLostCount(int lostCount) {
		this.lostCount = lostCount;
	}

	public void calcLost(BaseTable table,int lostCount, int point) {
        this.lostCount += lostCount;
        changePlayPoint(point);
        changePoint(this.playPoint, table);
	}

	public void calcWin(BaseTable table,int winCount, int point) {
		this.winCount += winCount;
		changePlayPoint(point);
		changePoint(this.playPoint,table);
	}

	public int getPoint() {
		return point;
	}

	public void setPoint(int point) {
		this.point = point;
	}

	public int getPlayPoint() {
		return playPoint;
	}

	public void setPlayPoint(int playPoint) {
		this.playPoint = playPoint;
	}

	public void changePlayPoint(int playPoint) {
		this.playPoint += playPoint;
		changeTableInfo();
	}

	public int getPiaoFen() {
		return piaoFen;
	}

	public void setPiaoFen(int piaoFen) {
		this.piaoFen = piaoFen;
	}

	public boolean isAlreadyPiaoFen() {
		return alreadyPiaoFen;
	}

	public void setAlreadyPiaoFen(boolean alreadyPiaoFen) {
		this.alreadyPiaoFen = alreadyPiaoFen;
	}

	public int getPlayBoomPoint() {
		return playBoomPoint;
	}

	public void setPlayBoomPoint(int playBoomPoint) {
		this.playBoomPoint = playBoomPoint;
	}

	public void changePlayBoomPoint(int playBoomPoint) {
		this.playBoomPoint += playBoomPoint;
		changeTableInfo();
	}


	public void changePoint(int point,BaseTable table) {
		this.point += point;
		myExtend.changePoint(table.getPlayType(), point);
		myExtend.setPdkFengshen(FirstmythConstants.firstmyth_index0, point);
		changeTotalPoint(point);
		if (point > getMaxPoint()) {
			setMaxPoint(point);
		}
		changeTableInfo();
	}

	public void changeCutCard(int cutCard) {
		this.cutCard = cutCard;
		changeTableInfo();
	}

	public void clearTableInfo(BaseTable table,boolean save){
		boolean isCompetition = false;
		if (table != null && table.isCompetition()) {
			isCompetition = true;
			endCompetition();
		}
		setSeat(0);
		if (!isCompetition) {
			setPlayingTableId(0);
		}
		setIsEntryTable(0);
		changeIsLeave(0);
		getHandPais().clear();
		getOutPais().clear();
		setMaxPoint(0);
		setPlayPoint(0);
		setPlayBoomPoint(0);
		changeState(null);
		setBoomCount(0);
		setTotalBoom(0);
		setWinCount(0);
		setLostCount(0);
		setPoint(0);
		setTotalPoint(0);
		setCutCard(0);
		// getPlayingTable().changePlayers();

		setRedTenPai(0);
		setNiaoFen(-1);
		setFirstXipai(-1);
		setLastCheckTime(0);
		if(table.isAutoPlay() && this.autoPlay) {
			setAutoPlay(false, table);
		}
		if (save) {
			saveBaseInfo();
		}
		setAlreadyPiaoFen(false);
		setPiaoFen(-1);
		currentLshu = 0;
		setCurrentLs(0);
		setWinLostPiaoFen(0);
		autoFinalCards.clear();
        setWinGold(0);
	}

	public void clearTableInfo() {
		clearTableInfo(getPlayingTable(),true);
	}

	/**
	 * 单局详情
	 *
	 * @return
	 */
	public ClosingPlayerInfoRes.Builder bulidOneClosingPlayerInfoRes(BaseTable table) {
		ClosingPlayerInfoRes.Builder res = ClosingPlayerInfoRes.newBuilder();
		res.setUserId(userId + "");
		res.setName(name);
		res.setPoint(point);
		res.setLeftCardNum(handPais.size());
		res.addAllCards(handPais);
//        res.setTotalPoint(table.isMatchRoom()?JjsUtil.loadMatch(table.getMatchId()).loadUserScore(userId):(table.isGoldRoom()?(int)loadAllGolds():loadScore()));
        res.setTotalPoint(table.isGoldRoom() ? (int) getWinGold() : loadScore());
		res.setSeat(seat);
		res.setBoom(boomCount);
		if (!StringUtils.isBlank(getHeadimgurl())) {
			res.setIcon(getHeadimgurl());
		} else {
			res.setIcon("");
		}
		res.setGoldFlag(getGoldResult());
		res.setSex(sex);
		return res;
	}

	/**
	 * 总局详情
	 *
	 * @return
	 */
	public ClosingPlayerInfoRes.Builder bulidTotalClosingPlayerInfoRes(BaseTable table) {
		ClosingPlayerInfoRes.Builder res = ClosingPlayerInfoRes.newBuilder();
		res.setUserId(userId + "");
		res.setName(name);
		res.setPoint(point);
		res.setLeftCardNum(handPais.size());
		res.setMaxPoint(getMaxPoint());
		res.setTotalBoom(getTotalBoom());
		res.setWinCount(getWinCount());
		res.setLostCount(getLostCount());
//		res.setTotalPoint(table.isMatchRoom()?JjsUtil.loadMatch(table.getMatchId()).loadUserScore(userId):(table.isGoldRoom()?(int)loadAllGolds():loadScore()));
        res.setTotalPoint(table.isGoldRoom() ? (int) getWinGold() : loadScore());
		res.addAllCards(handPais);
		res.setBoom(boomCount);
		res.setSeat(seat);
		if (!StringUtils.isBlank(getHeadimgurl())) {
			res.setIcon(getHeadimgurl());
		} else {
			res.setIcon("");
		}
		res.setGoldFlag(getGoldResult());
		res.setSex(sex);
		return res;
	}

	public int getIsNoLet() {
		return isNoLet;
	}

	/**
	 * 用于断线重连---
	 *
	 * @param isNoLet
	 *            1要不起0要的起
	 */
	public void setIsNoLet(int isNoLet) {
		this.isNoLet = isNoLet;
		changeTableInfo();
	}

	public void changeTableInfo() {
		BaseTable table = getPlayingTable();
		if (table != null)
			table.changePlayers();
	}

	@Override
	public void initNext() {
		getHandPais().clear();
		getOutPais().clear();
		setPoint(0);
		setPlayPoint(0);
		setPlayBoomPoint(0);
		setBoomCount(0);
		setIsNoLet(0);
		setRedTenPai(0);
		changeState(player_state.entry);
		changeSeat();
		setAlreadyPiaoFen(false);
		setPiaoFen(-1);
		setWinLostPiaoFen(0);
		autoFinalCards.clear();
	}

	public void changeSeat() {
		BaseTable table = getPlayingTable();
		if (table != null)
			table.changeCards(seat);
	}

	@Override
	public void endCompetition1() {
		// TODO Auto-generated method stub

	}

	/**
	 * 开始下一局准备
	 *
	 * @return
	 */
//	public boolean isStartNextReady() {
//		// 前台版本号在213以下的直接准备(因为没有切牌功能)
//		if (myExtend.getVersions() <= 213) {
//			return true;
//		}
//		return getCutCard() == 0;
//	}

	public int getCutCard() {
		return cutCard;
	}

	public void setCutCard(int cutCard) {
		this.cutCard = cutCard;
	}

	public static final List<Integer> wanfaList = Arrays.asList(15,16);

    public static void loadWanfaPlayers(Class<? extends Player> cls) {
        for (Integer playType : wanfaList) {
            PlayerManager.wanfaPlayerTypesPut(playType, cls, PdkCommandProcessor.getInstance());
            RobotManager.regAIGenerator(playType, PdkRobotAIGenerator.getInst());
        }
    }
	
	

	public int getCurrentLshu() {
		return currentLshu;
	}

	public void setCurrentLshu(int currentLshu) {
		this.currentLshu = currentLshu;
		changeTableInfo();
	}

	public int getNiaoFen() {
		return niaoFen;
	}

	public void setNiaoFen(int niaoFen) {
		this.niaoFen = niaoFen;
		changeTableInfo();
	}

	public int getFirstXipai() {
		return firstXipai;
	}

	public void setFirstXipai(int firstXipai) {
		this.firstXipai = firstXipai;
		changeTableInfo();
	}

	public List<Integer> getAutoFinalCards() {
		return autoFinalCards;
	}

	public void setAutoFinalCards(List<Integer> autoFinalCards) {
		if(autoFinalCards!=null)
			this.autoFinalCards = autoFinalCards;
		else
			this.autoFinalCards=new ArrayList<>();
	}
}
