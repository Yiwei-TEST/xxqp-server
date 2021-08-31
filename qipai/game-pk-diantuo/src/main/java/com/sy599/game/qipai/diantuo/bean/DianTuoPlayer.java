package com.sy599.game.qipai.diantuo.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

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
import com.sy599.game.db.dao.group.GroupDao;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.TableRes.ClosingPlayerInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes;
import com.sy599.game.qipai.diantuo.command.DianTuoCommandProcessor;
import com.sy599.game.qipai.diantuo.constant.DianTuoConstants;
import com.sy599.game.qipai.diantuo.util.CardUtils;
import com.sy599.game.util.GameUtil;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.util.StringUtil;
import com.sy599.game.util.TimeUtil;

public class DianTuoPlayer extends Player {
	// private long tableId;
	// private long roomId;
	// 座位id
	private volatile int seat;
	// 状态
	private volatile player_state state;// 1进入 2已准备 3正在玩 4已结束
	private int isEntryTable;
	private List<Integer> handPais;
	private List<List<Integer>> outPais;
	private int winCount;
	private int lostCount;
	private int point;
	private int playPoint;
	private int cutCard;// 是否需要切牌
//	/**叫分阶段叫的分 -1：还没叫 0不叫 */
//	private int jiaofen=-1;

	/**0：喜分；1：炸弹个数2：510K个数*/
	private int[] infoArr = new int[4];
	private volatile boolean autoPlay = false;//托管
	private volatile long lastOperateTime = 0;//最后操作时间
	private volatile long lastCheckTime = 0;//最后检查时间
	private volatile long nextAutoDisCardTime = 0;

	private volatile long autoPlayTime = 0;//自动操作时间

	private int currentLs;//当前连胜
	private int maxLs;//最大连胜
	//飘分分数
	private int piaoFen=-1;
	//喜分
	private int xifen=0;
	//吃分
	//private int chifen=0;

	//游戏分
	private int gameFen=0;
	
	/**1不要，2要不起*/
	private int actionS=-1;
	/***/
	private int duzhan=-1;
	private int rank;//名次
	
	private int faFen;//罚分
	
	
	
	private List<Integer> chiFenCards = new ArrayList<>();
	
	public long getNextAutoDisCardTime() {
		return nextAutoDisCardTime;
	}

	public void setNextAutoDisCardTime(long nextAutoDisCardTime) {
		this.nextAutoDisCardTime = nextAutoDisCardTime;
	}

	@Override
	protected void loadFromDB0(RegInfo info) {
		if (UserDatasDao.getInstance().exists()){
			String val = UserDatasDao.getInstance().selectUserDataValue(String.valueOf(userId),DianTuoTable.GAME_CODE,"all","currentLs");
			if (CommonUtil.isPureNumber(val)){
				currentLs = Integer.parseInt(val);
			}else{
				UserDatasDao.getInstance().saveUserDatas(String.valueOf(userId),DianTuoTable.GAME_CODE,"all","currentLs","0");
			}

			val = UserDatasDao.getInstance().selectUserDataValue(String.valueOf(userId),DianTuoTable.GAME_CODE,"all","maxLs");
			if (CommonUtil.isPureNumber(val)){
				maxLs = Integer.parseInt(val);
			}else{
				UserDatasDao.getInstance().saveUserDatas(String.valueOf(userId),DianTuoTable.GAME_CODE,"all","maxLs","0");
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
			DianTuoTable table2 = getPlayingTable(DianTuoTable.class);
			ArrayList<Integer> val = new ArrayList<>();
			if(autoPlay){
				val.add(1);
			}else {
				val.add(0);
			}
        	table2.addPlayLog(table2.addSandhPlayLog(getSeat(), DianTuoConstants.action_tuoguan, val, false, 0, null, 0));
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


	public DianTuoPlayer() {
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

	public void dealHandPais(List<Integer> pais,DianTuoTable table) {
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
	
	public boolean addHandPais(List<Integer> list) {
		 handPais.addAll(list);
		 return true;
	}
	
	public boolean removeHandPais(List<Integer> list) {
		 handPais.removeAll(list);
		 return true;
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

	public void addOutPais(List<Integer> cards ,DianTuoTable table) {
		this.table = table;
		//handPais.removeAll(cards);
		
		for(Integer id: cards){
			handPais.remove(id);
		}
		outPais.add(cards);
		table.changeCards(seat);
	}
	
	public void initExtend(String info) {
		  if (StringUtils.isBlank(info)) {
	            return;
	        }
	        int i = 0;
	        String[] values = info.split("\\|");
	        String val4 = StringUtil.getValue(values, i++);
	        if (!StringUtils.isBlank(val4)) {
	        	infoArr = StringUtil.explodeToIntArray(val4);
	        }
	}

	public String toExtendStr() {
		StringBuffer sb = new StringBuffer();
		sb.append(StringUtil.implode(infoArr)).append("|");
		return sb.toString();

	}

	public String toInfoStr() {
		// private int boomCount;
		// private int winCount;
		// private int lostCount;
		// private int point;
		StringBuilder sb = new StringBuilder();
		sb.append(getUserId()).append(",");
		sb.append(seat).append(",");
		int stateVal = 0;
		if (state != null) {
			stateVal = state.getId();
		}
		sb.append(stateVal).append(",");
		sb.append(isEntryTable).append(",");
		sb.append(getTotalBoom()).append(",");
		sb.append(winCount).append(",");
		sb.append(lostCount).append(",");
		sb.append(point).append(",");
		sb.append(loadScore()).append(",");
		sb.append(playPoint).append(",");
		sb.append(piaoFen).append(",");
		sb.append(xifen).append(",");
		sb.append(actionS).append(",");
		sb.append(gameFen).append(",");
		sb.append(duzhan).append(",");
		sb.append(rank).append(",");
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
			setTotalBoom(StringUtil.getIntValue(values, i++));
			this.winCount = StringUtil.getIntValue(values, i++);
			this.lostCount = StringUtil.getIntValue(values, i++);
			this.point = StringUtil.getIntValue(values, i++);
			setTotalPoint(StringUtil.getIntValue(values, i++));
			this.playPoint = StringUtil.getIntValue(values, i++);
			if (playPoint == 0 && this.point != 0) {
				playPoint = point;
			}
			this.piaoFen = StringUtil.getIntValue(values, i++);
			this.xifen = StringUtil.getIntValue(values, i++);
			this.actionS =  StringUtil.getIntValue(values, i++);
			gameFen=  StringUtil.getIntValue(values, i++);
			duzhan=  StringUtil.getIntValue(values, i++);
			rank=  StringUtil.getIntValue(values, i++);
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
		
		
		DianTuoTable table = getPlayingTable(DianTuoTable.class);
		if (table == null) {
			LogUtil.e("userId="+this.userId+"table null-->" + getPlayingTableId());
			return null;
		}
		
		List<Integer> outCards = getCurOutCard();
		if(outCards!=null) {
			if(!table.getTurnCards().isEmpty()){
				List<Integer> cards2 =new ArrayList<>(table.getNowDisCardIds());
				cards2.removeAll(outCards);
				if(cards2.isEmpty()){
					res.addAllOutCardIds(outCards);
				}
			}
			
		}
		
		
		if(!table.getTurnCards().isEmpty()&&actionS!=-1){
			if(table.getNowDisCardIds().size()==1&&!handPais.isEmpty()){
				for(Integer id: handPais){
					if(id==501||id==502){
						res.addOutedIds(id);
					}
				}
			}
		}
		res.setStatus(state.getId()-1);
		//res.setShiZhongCard(table.getTurnFirstSeat()==seat?1:0);

		if (isrecover) {
		}
		List<Integer> recover = new ArrayList<>();
		recover.add(isEntryTable);
		res.addAllRecover(recover);
		List<Integer> extList = new ArrayList<>();
		if(table.getMasterId() == userId){
			extList.add(1);//0
		}else{
			extList.add(0);
		}
		int totalScore = CardUtils.loadCardScore(chiFenCards);
		res.addAllScoreCard(chiFenCards);
		extList.add(getXifen(0));
		//吃分
		extList.add(totalScore);
		extList.add(isAutoPlay() && !isRobot() ? 1 : 0);
		extList.add(getGameFen());
		extList.add(actionS<0?0:actionS);// 5
		if(table.getTeamSeat().isEmpty()){
			extList.add(0);
		}else {
			extList.add(table.getTeamSeat().contains(getSeat())?1:2);
		}
		extList.add(rank);
		extList.add(handPais.size());// 8

		int tuoGuanCountDown = table.getAutoTimeOut();
		if (nextAutoDisCardTime > 0) {
			tuoGuanCountDown = (int) (nextAutoDisCardTime - TimeUtil.currentTimeMillis());
			if (tuoGuanCountDown < 0) {
				tuoGuanCountDown = 0;
			}
		}
		extList.add(0);//12 托管倒计时
		extList.add(autoPlay ? 1 : 0);//13 是否托管状态
		extList.add(piaoFen);         //15 飘分
		res.addAllExt(extList);

		//信用分
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
	
	/**
	 * 获取当前轮的牌
	 * @param turnNum
	 * @return
	 */
	public List<Integer> getCurOutCard(){
		if(outPais==null||outPais.isEmpty()){
			return null;
		}
		return outPais.get(outPais.size()-1);
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

//	public void calcLost(int lostCount, int point) {
//		this.lostCount += lostCount;
//		changePlayPoint(point);
//		changePoint(this.playPoint,table);
//	}
//
//	public void calcWin(int winCount, int point) {
//		this.winCount += winCount;
//		changePlayPoint(point);
//		changePoint(this.playPoint,table);
//	}

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

    public void changeAction(int index, int val) {
        infoArr[index] += val;
        if(index==0){
        	addXifen(val);
        }
        getPlayingTable().changeExtend();
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
		changeState(null);
		setTotalBoom(0);
		setWinCount(0);
		setLostCount(0);
		setPoint(0);
		setTotalPoint(0);
		setCutCard(0);
		xifen=0;
		gameFen=0;
		rank=0;
		setFaFen(0);
		chiFenCards= new ArrayList<>();
		duzhan=-1;
		actionS=-1;
		infoArr = new int[4];
		// getPlayingTable().changePlayers();
		setLastCheckTime(0);
		if(table.isAutoPlay() && this.autoPlay) {
			this.autoPlay = false;
			setAutoPlayCheckedTimeAdded(false);
		}
		if (save) {
			saveBaseInfo();
		}
		setPiaoFen(-1);
	}

	public void clearTableInfo() {
		clearTableInfo(getPlayingTable(),true);
	}

	/**
	 * 单局详情
	 *
	 * @return
	 */
	public ClosingPlayerInfoRes.Builder bulidOneClosingPlayerInfoRes() {
		ClosingPlayerInfoRes.Builder res = ClosingPlayerInfoRes.newBuilder();
		res.setUserId(userId + "");
		res.setName(name);
		res.setPoint(getFaFen()+getGameFen());
		res.setLeftCardNum(handPais.size());
		res.addAllCards(handPais);
		res.setTotalPoint(getPlayPoint());
		res.setSeat(seat);
		DianTuoTable table = getPlayingTable(DianTuoTable.class);
		res.setBoom(table.getBanker()==seat?1:0);
		if (!StringUtils.isBlank(getHeadimgurl())) {
			res.setIcon(getHeadimgurl());
		} else {
			res.setIcon("");
		}

		res.setSex(sex);
		
		
		res.addExt(xifen+"");
		res.addExt(faFen+"");
		res.addExt(gameFen+"");
		res.addExt(rank+"");
		res.addExt(table.getTeamSeat().contains(getSeat())?"1":"2");
		res.addExt(getTotalPoint()+"");
		
		return res;
	}

	/**
	 * 总局详情
	 *
	 * @return
	 */
	public ClosingPlayerInfoRes.Builder bulidTotalClosingPlayerInfoRes() {
		ClosingPlayerInfoRes.Builder res = ClosingPlayerInfoRes.newBuilder();
		res.setUserId(userId + "");
		res.setName(name);
		res.setPoint(point);
		res.setLeftCardNum(handPais.size());
		res.setMaxPoint(getMaxPoint());
		res.setTotalBoom(getTotalBoom());
		res.setWinCount(getWinCount());
		res.setLostCount(getLostCount());
		res.setTotalPoint(getPlayPoint());
	
		res.addAllActionCounts(	Arrays.asList(ArrayUtils.toObject(infoArr)));
		
		res.addAllCards(handPais);
		res.setSeat(seat);
		if (!StringUtils.isBlank(getHeadimgurl())) {
			res.setIcon(getHeadimgurl());
		} else {
			res.setIcon("");
		}
		DianTuoTable table = getPlayingTable(DianTuoTable.class);
		res.setBoom(table.getBanker()==seat?1:0);

		res.setSex(sex);
		
		
		
		
		res.addExt(xifen+"");
		res.addExt(faFen+"");
		res.addExt(gameFen+"");
		res.addExt(rank+"");
		res.addExt(table.getTeamSeat().contains(getSeat())?"1":"2");
		res.addExt(getTotalPoint()+"");
		
		return res;
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
		changeState(player_state.entry);
		changeSeat();
		setPiaoFen(-1);
		xifen=0;
		gameFen=0;
		actionS = 0;
		duzhan = -1;
		rank = 0;
		faFen=0;
		chiFenCards= new ArrayList<>();
	}

	public void changeSeat() {
		BaseTable table = getPlayingTable();
		if (table != null)
			table.changeCards(seat);
	}
	
	
	
	public int getXifen(int simpl) {
		if(simpl==0){
			return xifen;
		}
		return infoArr[0];
	}

	public void addXifen(int fen) {
		this.xifen += fen;
		changeTableInfo();
	}
	

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
		changeTableInfo();
	}

	public int getGameFen() {
		return gameFen;
	}

	public void addGameFen(int fen) {
		DianTuoTable table2 = getPlayingTable(DianTuoTable.class);
		if(table2==null ||table2.getBanker()!=0){
			return;
		}
		this.gameFen += fen;
		changeTableInfo();
	}

	public void setGameFen(int fen) {
		this.gameFen = fen;
		changeTableInfo();
	}
	public int getActionS() {
		return actionS;
	}

	public void setActionS(int actionS) {
		this.actionS = actionS;
		changeTableInfo();
	}

	
	public int getDuzhan() {
		return duzhan;
	}

	public void setDuzhan(int duzhan) {
		this.duzhan = duzhan;
	}

	@Override
	public void endCompetition1() {
		// TODO Auto-generated method stub

	}
	
	

	public int getFaFen() {
		return faFen;
	}

	public void setFaFen(int faFen) {
		this.faFen = faFen;
	}

	public List<Integer> getChiFenCards() {
		return chiFenCards;
	}

	public void addChiFenCards(List<Integer> cards) {
		this.chiFenCards.addAll(cards);
		changeTableInfo();
	}

	public int getCutCard() {
		return cutCard;
	}

	public void setCutCard(int cutCard) {
		this.cutCard = cutCard;
	}

	public static final List<Integer> wanfaList = Arrays.asList(GameUtil.game_type_DATUO);

	public static void loadWanfaPlayers(Class<? extends Player> cls){
		for (Integer integer:wanfaList){
			PlayerManager.wanfaPlayerTypesPut(integer,cls, DianTuoCommandProcessor.getInstance());
		}
	}

}
