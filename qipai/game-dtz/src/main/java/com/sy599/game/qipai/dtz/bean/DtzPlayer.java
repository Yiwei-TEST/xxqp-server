package com.sy599.game.qipai.dtz.bean;

import com.sy.mainland.util.CommonUtil;
import com.sy599.game.base.BaseGame;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.FirstmythConstants;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.common.constant.SharedConstants.player_state;
import com.sy599.game.db.bean.RegInfo;
import com.sy599.game.db.bean.group.GroupUser;
import com.sy599.game.db.dao.UserDatasDao;
import com.sy599.game.db.dao.gold.GoldDao;
import com.sy599.game.db.dao.gold.GoldRoomDao;
import com.sy599.game.db.dao.group.GroupDao;
import com.sy599.game.jjs.bean.MatchBean;
import com.sy599.game.jjs.util.JjsUtil;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.msg.serverPacket.TableRes.ClosingPlayerInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes;
import com.sy599.game.qipai.dtz.command.DtzCommandProcessor;
import com.sy599.game.qipai.dtz.constant.DtzzConstants;
import com.sy599.game.qipai.dtz.tool.CardToolDtz;
import com.sy599.game.qipai.dtz.tool.CardTypeToolDtz;
import com.sy599.game.util.GameUtil;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DtzPlayer extends Player implements Comparable<DtzPlayer>, BaseGame {
	// 座位id,1、2、3、4
	private volatile int seat;
	// 状态
	private player_state state;// 1进入 2已准备 3正在玩 4已结束
	private int isEntryTable;
	private List<Integer> handPais;
	private List<List<Integer>> outPais;
	private int boomCount;
	private int winCount;
	private int lostCount;
	//玩家头像显示当前分数，玩家个人牌面分，不包括筒子地炸等
	private int point; 
	private int playPoint;
	//当前玩家是否要的起，1要不起0要的起
	private int isNoLet;
	private int cutCard;// 是否需要切牌
	//玩家本轮的地炸筒子分  是本局的
	private int roundScore;
	
	private int dtzTotalPoint; //dtz的大结算分数
	
	private int winLossPoint;//输赢分用于统计排名
	
	private int autoPlay;//自动托管 1开启  重启不保存

	private int currentLs;//当前连胜
	private int maxLs;//最大连胜

	public int getAutoPlay() {
		return autoPlay;
	}
	public void setAutoPlay(int autoPlay) {
		this.autoPlay = autoPlay;
	}
	public int getWinLossPoint() {
		return winLossPoint;
	}
	public void setWinLossPoint(int winLossPoint) {
		this.winLossPoint = winLossPoint;
	}

	public DtzPlayer() {
		// this.tableId = tableId;
		// this.userId = userId;
		// this.roomId = roomId;
		handPais = new ArrayList<Integer>();
		outPais = new ArrayList<List<Integer>>();
	}

	@Override
	protected void loadFromDB0(RegInfo info) {
		if (UserDatasDao.getInstance().exists()){
			String val = UserDatasDao.getInstance().selectUserDataValue(String.valueOf(userId),DtzTable.GAME_CODE,"all","currentLs");
			if (CommonUtil.isPureNumber(val)){
				currentLs = Integer.parseInt(val);
			}else{
				UserDatasDao.getInstance().saveUserDatas(String.valueOf(userId),DtzTable.GAME_CODE,"all","currentLs","0");
			}

			val = UserDatasDao.getInstance().selectUserDataValue(String.valueOf(userId),DtzTable.GAME_CODE,"all","maxLs");
			if (CommonUtil.isPureNumber(val)){
				maxLs = Integer.parseInt(val);
			}else{
				UserDatasDao.getInstance().saveUserDatas(String.valueOf(userId),DtzTable.GAME_CODE,"all","maxLs","0");
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

	public int getSeat() {
		return seat;
	}

	/**
	 * 打筒子本局以前历史牌面分
	 */
	@Override
	public int loadAggregateScore() {
		if (getPlayingTable() == null) {
			LogUtil.e("loadAggregateScore-->table is null:"+getPlayingTableId()+",userId:"+userId);
			return 0;
		}
		DtzTable table1 = (DtzTable) getPlayingTable();
		int score;
		int group = table1.getGroup(seat);
		if (table1.isFourPlayer()) {
			score =  table1.getGroupScore().get(group) - point;
			List<DtzPlayer> sameGroupList = table1.getGroupMap().get(group);
			if (sameGroupList.contains(this)) {
				for (DtzPlayer player : sameGroupList) {
					if (player.getUserId() == userId) {
						continue;
					}
					score -= player.getPoint();
				}
			}
		} else {
			score = table1.getGroupScore().get(group) - point;
		}
		//getDtzTotalPoint()本局以前全部得分含筒子分          getPoint()本局所得牌面分                    getRoundScore()本局所得筒子分
		return score;
	}

	/**
	 * 本局以前的筒子分
	 */
	@Override
	public int loadTzScore() {
		return getDtzTotalPoint() - loadAggregateScore();
	}

	public int getRoundScore() {
		return roundScore;
	}

	public void setRoundScore(int roundScore) {
		this.roundScore = roundScore;
	}

	public int getDtzTotalPoint() {
		return dtzTotalPoint;
	}

	public void setDtzTotalPoint(int dtzTotalPoint) {
		this.dtzTotalPoint = dtzTotalPoint;
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

	@Override
	public int loadScore() {
		return getWinLossPoint();
	}

	public void dealHandPais(List<Integer> pais) {
		this.handPais = pais;
		BaseTable basetable = getPlayingTable();
		basetable.changeCards(seat);
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

	public void addOutPais(List<Integer> cards) {
//		handPais.removeAll(cards);
		handPais = CardToolDtz.removeAll(cards, this.handPais);
		outPais.add(new ArrayList<Integer>(cards));
		getPlayingTable().changeCards(seat);
	}

	/**
	 * 存到数据库的
	 */
	public String toInfoStr() {
		// private int boomCount;
		// private int winCount;
		// private int lostCount;
		// private int point;
		StringBuffer sb = new StringBuffer();
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
		sb.append(getTotalPoint()).append(",");
		sb.append(isNoLet).append(",");
		sb.append(playPoint).append(",");
		sb.append(cutCard).append(",");
		/**
		 * 			if (player.getClass() == DtzPlayer.class) {
				DtzPlayer pdkplayer = (DtzPlayer)player;
				pdkplayer.setRoundScore(wrapper.getInt("roundScore", 0));
				pdkplayer.setDtzTotalPoint(wrapper.getInt("dtzTotalPoint", 0));
			}
		 */
		sb.append(roundScore).append(",");
		sb.append(dtzTotalPoint).append(",");
		sb.append(getWinLoseCredit()).append(",");
		sb.append(getCommissionCredit()).append(",");
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
//			if (playPoint == 0 && this.point != 0) {
//				playPoint = point;
//				System.out.println("playPoint:"+playPoint+",point:"+point);
//			}
			this.cutCard = StringUtil.getIntValue(values, i++);
			this.roundScore = StringUtil.getIntValue(values, i ++);
			this.dtzTotalPoint = StringUtil.getIntValue(values, i ++);

			setWinLoseCredit(StringUtil.getIntValue(values, i++));
			setCommissionCredit(StringUtil.getIntValue(values, i++));
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
		return buildPlayInTableInfo(getPlayingTable(DtzTable.class),0, false);
	}

	/**
	 * @param isrecover
	 *            是否重连
	 * @return
	 */
	public PlayerInTableRes.Builder buildPlayInTableInfo(DtzTable table,long recUserId, boolean isrecover) {
		if (table == null) {
			LogUtil.e("userId="+this.userId+"table null-->" + getPlayingTableId());
			return null;
		}
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
		res.setPoint(getTotalPoint());
		if (!StringUtils.isBlank(getHeadimgurl())) {
			res.setIcon(getHeadimgurl());

		} else {
			res.setIcon("");
		}

		//|| state == player_state.play
//		LogUtil.msg("table:" + table.getId() + " name " + getName() + " player State " + state);
		if (state == player_state.ready) {
			// 玩家装备已经准备和正在玩的状态时通知前台已准备
			res.setStatus(DtzzConstants.state_player_ready);

		} 
		else if (state == player_state.play) {
			res.setStatus(DtzzConstants.state_player_ready);
		}
		else {
			res.setStatus(0);

		}

		if (isrecover) {
			// 0是否要的起 1是否报单 2是否暂离(1暂离0在线)
			List<Integer> recover = new ArrayList<>();

			int nowDisSeat = table.getNextDisCardSeat();
			if (recUserId == this.userId && table.getDisCardSeat() != seat && !table.getNowDisCardIds().isEmpty() && nowDisSeat == seat) {
				// 自己重连,现在轮到自己出牌,去打别人出的牌
				if (CardTypeToolDtz.isCanPlay(handPais, table.getNowDisCardIds(), table) != 1) {
					recover.add(0);
				} else {
					recover.add(1);
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
//		List<Integer> extList = new ArrayList<>();
//		extList.add(cutCard);
		int cardNumber = this.getHandPais().size();
		if (0 == cardNumber && (table.getState()==null||table.getState().getId() < SharedConstants.table_state.play.getId())) {
			switch(table.getPlayType()){
				case DtzzConstants.play_type_3POK:
					cardNumber = table.getKouType() == 1 ? 31 : 33;
					break;
				case DtzzConstants.play_type_4POK:
					cardNumber = table.getKouType() == 1 ? 44 : 46;
					break;	
				case DtzzConstants.play_type_3PERSON_3POK:
					cardNumber = 41;
					break;	
				case DtzzConstants.play_type_3PERSON_4POK:
					cardNumber = 44;
					break;	
				case DtzzConstants.play_type_2PERSON_3POK:
					cardNumber = 33;
					break;	
				case DtzzConstants.play_type_2PERSON_4POK:
					cardNumber = 44;
					break;	
			}
        }
		/*
		optional int32 curPoint = 23; //这个玩家的分数
     optional PlayerPointDetail bars = 24; //我的积分 详情
     optional bool isMaster = 25; //是不是房主
     optional int32 group = 26; //dtz分组
     optional int32 mingci = 27;  //他的名次   这句比赛的
     optional int32 isFirstOut =  28;  //重连  和 下一局 需要看是不是要显示按钮
		 */
//		extList.add(cardNumber);
//		res.addAllExt(extList);

		res.setPoint(getPoint());
//		res.addExt(getPoint());
// 		res.setGroup(table.findGroupInit(this));  //这个玩家分组  1

		int group = table.findGroupInit(this);
		if (group == 0 && seat>0 && table.getMaxPlayerCount()<=3){
			table.groupPlayer(seat,this);
			group = seat;
		}

		res.addExt(group);

//		res.setMingci(table.findPlayerMingCi(this));  //这个玩家的名次  2
		res.addExt(table.findPlayerMingCi(this));

//		res.setCurPoint(getDtzTotalPoint()); //这个玩家的分数  3
		res.addExt(getDtzTotalPoint());

        if (table.dtzRound == 1 && table.getNowDisCardSeat() == getSeat() && (table.getNowDisCardIds() == null ||table.getNowDisCardIds().isEmpty())) {
//            res.setIsFirstOut(1); // 4
			res.addExt(1);
		}
		else {
    		if ((table.lastWin != 0 && table.lastWin == this.seat) || (table.isDoneRound1().getValue1() != null && table.isDoneRound1().getValue1().equals(this))) {
//    			res.setIsFirstOut(1); // 4
				res.addExt(1);
    		}
    		else {
//				res.setIsFirstOut(0); //4
				res.addExt(0);
			}
        }
//		res.setIsMaster(getPlayingTable().getMasterId() == this.userId); // 5
		res.addExt((table.getMasterId() == this.userId) ? 1 : 0);
		res.addExt(table.getFirstCardType().getValue1().intValue());

		// 7 剩牌数量
		if (table.getShowCardNumber() == 1) {
			res.addExt(cardNumber);
		} else {
			res.addExt(0);
		}
		res.addExt(getAutoPlay());//8

		MatchBean matchBean = table.isMatchRoom()? JjsUtil.loadMatch(table.getMatchId()):null;
		if (matchBean!=null){
			res.addExt(matchBean.loadUserScore(userId));//9
			int gameNo = JjsUtil.loadMatchCurrentGameNo(matchBean);
			res.addExt(gameNo);//10
			res.addExt(JjsUtil.loadMatchCurrentGameWinCount(matchBean,String.valueOf(gameNo)));//11
			if (gameNo>0){
				res.addExt(JjsUtil.loadMatchCurrentGameWinCount(matchBean,String.valueOf(gameNo-1)));//12
				res.addExt(table.getPlayBureau());//13
			}else{
				res.addExt(JjsUtil.loadMaxRestUserCount(matchBean,String.valueOf(gameNo)));
				res.addExt(GoldRoomDao.getInstance().loadUserRoomCount(table.getModeId(),userId));
			}
			res.addExt(matchBean.loadUserScore(userId));//14
			int[] rank = matchBean.loadUserRank(gameNo,String.valueOf(userId));
			res.addExt(rank[0]);//15
			res.addExt(rank[1]);//16
		}else{
			res.addExt(table.isGoldRoom()?(int)loadAllGolds():0);//9
			res.addExt(0);//10
			res.addExt(0);//11
			res.addExt(0);//12
			res.addExt(0);//13
			res.addExt(0);//14
			res.addExt(0);//15
			res.addExt(0);//16
		}
		if(table.isCreditTable()) {
			GroupUser gu = getGroupUser();
			String groupId = table.loadGroupId();
			if (gu == null || !groupId.equals(gu.getGroupId() + "")) {
				gu = GroupDao.getInstance().loadGroupUser(getUserId(), groupId);
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

	public void calcResult(DtzTable table,int point) {
	    if (point>0) {
	        this.winCount++;
        } else {
            this.lostCount ++;
        }
        this.table = table;
	    myExtend.changeWinLossCount(table.getPlayType(), point>0);
		changeTotalCount();
		changeExtend();
		changeTableInfo();
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

	public void changePoint(int point) {
		this.point += point;
		myExtend.changePoint(getPlayingTable().getPlayType(), point);
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

    public void clearTableInfo() {
        clearTableInfo(getPlayingTable(),true);
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
		changeState(null);
		setBoomCount(0);
		setTotalBoom(0);
		setWinCount(0);
		setLostCount(0);
		setPoint(0);
		setTotalPoint(0);
		setCutCard(0);
		// getPlayingTable().changePlayers();
		setAutoPlay(0);
		setWinGold(0);
		setDtzTotalPoint(0);
		setRoundScore(0);
		setWinLossPoint(0);
        if (save) {
            saveBaseInfo();
        }
	}

	/**
	 * 单局详情
	 * 
	 * @return
	 */
	public ClosingPlayerInfoRes.Builder bulidOneClosingPlayerInfoRes(DtzTable table) {
		ClosingPlayerInfoRes.Builder res = ClosingPlayerInfoRes.newBuilder();
		res.setUserId(userId + "");
		res.setName(name);
		res.setPoint(point);
		res.setLeftCardNum(handPais.size());
		res.addAllCards(handPais);
		res.setTotalPoint(getTotalPoint());
//		res.setTotalPoint(table.isGoldRoom()?(int)loadAllGolds():getTotalPoint());
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
	public ClosingPlayerInfoRes.Builder bulidTotalClosingPlayerInfoRes(DtzTable table) {
		ClosingPlayerInfoRes.Builder res = ClosingPlayerInfoRes.newBuilder();
		res.setUserId(userId + "");
		res.setName(name);
		res.setPoint(point);
		res.setLeftCardNum(handPais.size());
		res.setMaxPoint(getMaxPoint());
		res.setTotalBoom(getTotalBoom());
		res.setWinCount(getWinCount());
		res.setLostCount(getLostCount());
		res.setTotalPoint(table.isGoldRoom()?(int)loadAllGolds():getTotalPoint());
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
	 * 设置玩家当前是否要的起
	 * @param isNoLet 1要不起0要的起
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
//		setPoint(0);
		setPlayPoint(0);
		setBoomCount(0);
		setIsNoLet(0);
		changeState(player_state.entry);
		changeSeat();
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


	public int getCutCard() {
		return cutCard;
	}

	public void setCutCard(int cutCard) {
		this.cutCard = cutCard;
	}
	
	
	public boolean  equals(Object obj) {
		if (obj == this) return true;
		return ((DtzPlayer)obj).userId == userId;
	}
	
	public int hashCode() {
		return (int)userId;
	}
	
	public int compareTo(DtzPlayer obj) {
		return 0;
	}

    public static final List<Integer> wanfaList = Arrays.asList(
            GameUtil.play_type_3POK,
            GameUtil.play_type_4POK,
            GameUtil.play_type_3PERSON_3POK,
            GameUtil.play_type_3PERSON_4POK,
            GameUtil.play_type_2PERSON_3POK,
            GameUtil.play_type_2PERSON_4POK,
            GameUtil.play_type_2PERSON_4Xi,
            GameUtil.play_type_3PERSON_4Xi,
            GameUtil.play_type_4PERSON_4Xi);

	public static void loadWanfaPlayers(Class<? extends Player> cls){
		for (Integer integer:wanfaList){
			PlayerManager.wanfaPlayerTypesPut(integer,cls, DtzCommandProcessor.getInstance());
		}
	}

	
}
