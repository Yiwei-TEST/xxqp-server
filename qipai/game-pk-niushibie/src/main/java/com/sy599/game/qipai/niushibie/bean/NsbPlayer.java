package com.sy599.game.qipai.niushibie.bean;

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
import com.sy599.game.qipai.niushibie.command.NsbCommandProcessor;
import com.sy599.game.qipai.niushibie.constant.NsbConstants;
import com.sy599.game.qipai.niushibie.util.NsbSfNew;
import com.sy599.game.qipai.niushibie.util.NsbUtil;
import com.sy599.game.util.*;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class NsbPlayer extends Player {
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
	private int[] infoArr = new int[4];
	private volatile boolean autoPlay = false;//托管
	private volatile long lastOperateTime = 0;//最后操作时间
	private volatile long lastCheckTime = 0;//最后检查时间
	private volatile long nextAutoDisCardTime = 0;
	private volatile long autoPlayTime = 0;//自动操作时间
	private int currentLs;//当前连胜
	private int maxLs;//最大连胜
	//吃分
	private int chifen=0;
	//烧分
	private int shaofen=0;
	//排名奖励分
	private int pmjlfen=0;
	//游戏分
	private int gameFen=0;

	private int wsknum=0;
	private int boomnum=0;
	private int thsnum =0;

	/**1不要，2要不起*/
	private int actionS=-1;
	/***/
	private int rank;//名次
//	全局总拿分
	private int zongfen=0;
	//是否明牌0=否 1=是
	private int isShowCards = 0;

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
			String val = UserDatasDao.getInstance().selectUserDataValue(String.valueOf(userId),NsbTable.GAME_CODE,"all","currentLs");
			if (CommonUtil.isPureNumber(val)){
				currentLs = Integer.parseInt(val);
			}else{
				UserDatasDao.getInstance().saveUserDatas(String.valueOf(userId),NsbTable.GAME_CODE,"all","currentLs","0");
			}

			val = UserDatasDao.getInstance().selectUserDataValue(String.valueOf(userId),NsbTable.GAME_CODE,"all","maxLs");
			if (CommonUtil.isPureNumber(val)){
				maxLs = Integer.parseInt(val);
			}else{
				UserDatasDao.getInstance().saveUserDatas(String.valueOf(userId),NsbTable.GAME_CODE,"all","maxLs","0");
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
			NsbTable table2 = getPlayingTable(NsbTable.class);
			ArrayList<Integer> val = new ArrayList<>();
			if(autoPlay){
				val.add(1);
			}else {
				val.add(0);
			}
        	table2.addPlayLog(table2.addSandhPlayLog(getSeat(), NsbConstants.action_tuoguan, val, false, 0, null, 0));
		}
		boolean addLog = this.autoPlay != autoPlay;
		this.autoPlay = autoPlay;
		if(!autoPlay){
			setAutoPlayCheckedTimeAdded(false);
		}

		if(addLog) {
			StringBuilder sb = new StringBuilder("NSB");
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


	public NsbPlayer() {
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

	public void dealHandPais(List<Integer> pais,NsbTable table) {
		this.table = table;
		this.handPais = pais;

		Collections.sort(pais, new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return NsbUtil.loadCardValue(o2) - NsbUtil.loadCardValue(o1);
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

	public void addOutPais(List<Integer> cards ,NsbTable table) {
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
		sb.append(chifen).append(",");
		sb.append(shaofen).append(",");
		sb.append(zongfen).append(",");
		sb.append(actionS).append(",");
		sb.append(gameFen).append(",");
		sb.append(rank).append(",");
		sb.append(isShowCards).append(",");
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
			chifen = StringUtil.getIntValue(values, i++);
			shaofen =StringUtil.getIntValue(values, i++);
			zongfen =StringUtil.getIntValue(values, i++);
			this.actionS =  StringUtil.getIntValue(values, i++);
			gameFen=  StringUtil.getIntValue(values, i++);
			rank=  StringUtil.getIntValue(values, i++);
			isShowCards=  StringUtil.getIntValue(values, i++);
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
		res.setShiZhongCard(isShowCards);//是否明牌 1是  0 否
		res.setPoint(getPlayPoint());//积分。
		if (!StringUtils.isBlank(getHeadimgurl())) {
			res.setIcon(getHeadimgurl());
		} else {
			res.setIcon("");
		}
		NsbTable table = getPlayingTable(NsbTable.class);
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
		int totalScore = NsbUtil.getCpfen(NsbSfNew.intCardToStringCard(chiFenCards));
		res.addAllScoreCard(chiFenCards);
		extList.add(0);//1 弃用
		extList.add(getChifen());//2
		extList.add(isAutoPlay() && !isRobot() ? 1 : 0);//3
		extList.add(getGameFen());//4
		extList.add(actionS<0?0:actionS);// 5
		//6 队伍
		if(table.getTeamSeat().isEmpty()){
			extList.add(0);
		}else {
			extList.add(table.getTeamSeat().contains(getSeat())?1:2);
		}
		extList.add(rank);// 7排名
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
	 * @param
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


    public void changeAction(int index, int val) {
        infoArr[index] += val;
//        if(index==0){
//        	addXifen(val);
//        }
        getPlayingTable().changeExtend();
    }
	public int getActionValue(int index){
		return infoArr[index];
	}
	public int[] getInfoArr(){
		return infoArr;
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
		setChifen(0);
		setShaofen(0);
		setPmjlfen(0);
		setRank(0);
		setZongfen(0);
		isShowCards=0;
		chifen=0;
		shaofen=0;
		pmjlfen=0;
		gameFen=0;
		rank=0;
		chiFenCards= new ArrayList<>();
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
//		res.setPoint(getFaFen()+getGameFen());
		res.setPoint(point);
		res.setLeftCardNum(handPais.size());
		res.addAllCards(handPais);
		res.setTotalPoint(getPlayPoint());
		res.setSeat(seat);
		NsbTable table = getPlayingTable(NsbTable.class);
		res.setBoom(table.getBanker()==seat?1:0);
		if (!StringUtils.isBlank(getHeadimgurl())) {
			res.setIcon(getHeadimgurl());
		} else {
			res.setIcon("");
		}

		res.setSex(sex);
		res.addExt(shaofen+"");//0
		res.addExt(pmjlfen+"");//1
		res.addExt(gameFen+"");//2
		res.addExt(rank+"");//3 排名
		res.addExt(table.getTeamSeat().contains(getSeat())?"1":"2");//4 队伍
		res.addExt(zongfen+"");//5 总分
		res.addExt(chifen+"");//6 单局个人吃分
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
//		res.setTotalPoint(getTotalPoint());
		res.addAllActionCounts(	Arrays.asList(ArrayUtils.toObject(infoArr)));
		res.addAllCards(handPais);
		res.setSeat(seat);
		if (!StringUtils.isBlank(getHeadimgurl())) {
			res.setIcon(getHeadimgurl());
		} else {
			res.setIcon("");
		}
		NsbTable table = getPlayingTable(NsbTable.class);
		res.setBoom(table.getBanker()==seat?1:0);
		res.setSex(sex);
		res.addExt(shaofen+"");//0
		res.addExt(pmjlfen+"");//1
		res.addExt(gameFen+"");//2
		res.addExt(rank+"");//3 排名
		res.addExt(table.getTeamSeat().contains(getSeat())?"1":"2");//4 队伍
		res.addExt(zongfen+"");//5 总分
		res.addExt(chifen+"");//6 单局个人吃分
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
//		setPlayPoint(0);
		changeState(player_state.entry);
		changeSeat();
		chifen=0;
		gameFen=0;
		actionS = 0;
		rank = 0;
		shaofen=0;
		isShowCards=0;
		pmjlfen =0;
		chiFenCards= new ArrayList<>();
	}

	public void changeSeat() {
		BaseTable table = getPlayingTable();
		if (table != null)
			table.changeCards(seat);
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

	public void setGameFen(int gameFen) {
		this.gameFen = gameFen;
	}

	public void addGameFen(int fen) {
		NsbTable table2 = getPlayingTable(NsbTable.class);
//		if(table2==null ||table2.getBanker()!=0){
//			return;
//		}
		if(table2==null){
			return;
		}
		this.gameFen += fen;
		changeTableInfo();
	}

	
	public int getActionS() {
		return actionS;
	}

	public void setActionS(int actionS) {
		this.actionS = actionS;
		changeTableInfo();
	}

	@Override
	public void endCompetition1() {

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

	public static final List<Integer> wanfaList = Arrays.asList(GameUtil.play_type_pk_nsb);

	public int getChifen() {
		return chifen;
	}

	public void setChifen(int chifen) {
		this.chifen = chifen;
		changeTableInfo();
	}

	public static void loadWanfaPlayers(Class<? extends Player> cls){
		for (Integer integer:wanfaList){
			PlayerManager.wanfaPlayerTypesPut(integer,cls, NsbCommandProcessor.getInstance());
		}
	}

	public int getShaofen() {
		return shaofen;
	}

	public void setShaofen(int shaofen) {
		this.shaofen = shaofen;
		changeTableInfo();
	}

	public int getPmjlfen() {
		return pmjlfen;
	}

	public void setPmjlfen(int pmjlfen) {
		this.pmjlfen = pmjlfen;
	}

	public int getWsknum() {
		return wsknum;
	}

	public void setWsknum(int wsknum) {
		this.wsknum = wsknum;
	}

	public int getBoomnum() {
		return boomnum;
	}

	public void setBoomnum(int boomnum) {
		this.boomnum = boomnum;
	}

	public int getThsnum() {
		return thsnum;
	}

	public void setThsnum(int thsnum) {
		this.thsnum = thsnum;
	}

	public int getZongfen() {
		return zongfen;
	}
	public void setZongfen(int zongfen) {
		this.zongfen = zongfen;
		changeTableInfo();
	}

	public int isShowCards() {
		return isShowCards;
	}

	public void setShowCards(int showCards) {
		isShowCards = showCards;
		changeTableInfo();
	}
}
