package com.sy599.game.qipai.xtbp.bean;

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
import com.sy599.game.qipai.xtbp.command.XtbpCommandProcessor;
import com.sy599.game.qipai.xtbp.constant.XtbpConstants;
import com.sy599.game.qipai.xtbp.util.CardUtils;
import com.sy599.game.util.*;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class XtbpPlayer extends Player {
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
	/**叫分阶段叫的分 -1：还没叫 0不叫 */
	private int jiaofen=-1;


	private volatile boolean autoPlay = false;//托管
	private volatile long lastOperateTime = 0;//最后操作时间
	private volatile long lastCheckTime = 0;//最后检查时间
	private volatile long nextAutoDisCardTime = 0;

	private volatile long autoPlayTime = 0;//自动操作时间
	private volatile long piaofenCheckTime = 0;//飘分托管操作时间

	private int currentLs;//当前连胜
	private int maxLs;//最大连胜

	//报副
	private int baofu=0;
	//留守
	private int liushou=0;

	//投降 1：发起2：同意3：拒绝
	private int touXiang=0;

	private int piaofen =-1;

	public long getNextAutoDisCardTime() {
		return nextAutoDisCardTime;
	}

	public void setNextAutoDisCardTime(long nextAutoDisCardTime) {
		this.nextAutoDisCardTime = nextAutoDisCardTime;
	}

	@Override
	protected void loadFromDB0(RegInfo info) {
		if (UserDatasDao.getInstance().exists()){
			String val = UserDatasDao.getInstance().selectUserDataValue(String.valueOf(userId),XtbpTable.GAME_CODE,"all","currentLs");
			if (CommonUtil.isPureNumber(val)){
				currentLs = Integer.parseInt(val);
			}else{
				UserDatasDao.getInstance().saveUserDatas(String.valueOf(userId),XtbpTable.GAME_CODE,"all","currentLs","0");
			}

			val = UserDatasDao.getInstance().selectUserDataValue(String.valueOf(userId),XtbpTable.GAME_CODE,"all","maxLs");
			if (CommonUtil.isPureNumber(val)){
				maxLs = Integer.parseInt(val);
			}else{
				UserDatasDao.getInstance().saveUserDatas(String.valueOf(userId),XtbpTable.GAME_CODE,"all","maxLs","0");
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
			XtbpTable table2 = getPlayingTable(XtbpTable.class);
			ArrayList<Integer> val = new ArrayList<>();
			if(autoPlay){
				val.add(1);
			}else {
				val.add(0);
			}
			if(null == table2){
				return;
			}else{
				table2.addPlayLog(table2.addSandhPlayLog(getSeat(), XtbpConstants.action_tuoguan, val, false, 0, null, 0));
			}
        	// table.addPlayLog(getSeat(), XtbpConstants.action_tuoguan + "",(autoPlay?1:0) + "");
		}
		boolean addLog = this.autoPlay != autoPlay;
		this.autoPlay = autoPlay;
		if(!autoPlay){
			setAutoPlayCheckedTimeAdded(false);
		}

		if(addLog) {
			StringBuilder sb = new StringBuilder("HeTianBaoPai");
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


	public XtbpPlayer() {
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

	public void dealHandPais(List<Integer> pais,XtbpTable table) {
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

	public void addOutPais(List<Integer> cards ,XtbpTable table) {
		this.table = table;
		//handPais.removeAll(cards);
		
		for(Integer id: cards){
			handPais.remove(id);
		}
		outPais.add(cards);
		table.changeCards(seat);
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
		sb.append(jiaofen).append(",");
		sb.append(baofu).append(",");
		sb.append(liushou).append(",");
		sb.append(touXiang).append(",");
		sb.append(piaofen).append(",");
		sb.append(piaofenCheckTime).append(",");
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
			this.jiaofen = StringUtil.getIntValue(values, i++);
			this.baofu = StringUtil.getIntValue(values, i++);
			this.liushou =  StringUtil.getIntValue(values, i++);
			touXiang=  StringUtil.getIntValue(values, i++);
			piaofen=  StringUtil.getIntValue(values, i++);
			piaofenCheckTime = StringUtil.getLongValue(values, i++);
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
		
		
		XtbpTable table = getPlayingTable(XtbpTable.class);
		if (table == null) {
			LogUtil.e("userId="+this.userId+"table null-->" + getPlayingTableId());
			return null;
		}
		
		List<Integer> outCards = getCurOutCard(table.getTurnNum());
		if(outCards!=null) {
			res.addAllOutCardIds(outCards);
		}
		
		res.setStatus(state.getId()-1);
		res.setShiZhongCard(table.getTurnFirstSeat()==seat?1:0);

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

		extList.add(jiaofen);//1
		extList.add(getBaofu());
		extList.add(isAutoPlay() && !isRobot() ? 1 : 0);
		extList.add(getLiushou());
		extList.add(getTouXiang());
		extList.add(getPiaofen());//6
		extList.add(0);//7
		extList.add(0);
		extList.add(0);// 11
		int tuoGuanCountDown = table.getAutoTimeOut();
		if (nextAutoDisCardTime > 0) {
			tuoGuanCountDown = (int) (nextAutoDisCardTime - TimeUtil.currentTimeMillis());
			if (tuoGuanCountDown < 0) {
				tuoGuanCountDown = 0;
			}
		}
		extList.add(tuoGuanCountDown);//12 托管倒计时
		extList.add(autoPlay ? 1 : 0);//13 是否托管状态
		extList.add(piaofen);         //15 飘分
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
	public List<Integer> getCurOutCard(int turnNum){
		if(turnNum>outPais.size()||outPais.size()==0 ||turnNum==0){
			return null;
		}
		return outPais.get(turnNum-1);
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

	public void calcLost(int lostCount, int point) {
		this.lostCount += lostCount;
		changePlayPoint(point);
		changePoint(this.playPoint,table);
	}

	public void calcWin(int winCount, int point) {
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
		baofu=0;
		liushou=0;
		touXiang=0;
		// getPlayingTable().changePlayers();
		setLastCheckTime(0);
		if(table.isAutoPlay() && this.autoPlay) {
			setAutoPlay(false, table);
		}
		if (save) {
			saveBaseInfo();
		}
		setPiaofen(-1);
		setJiaofen(-1);
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
		res.setPoint(point);
		res.setLeftCardNum(handPais.size());
		res.addAllCards(handPais);
		res.setTotalPoint(loadScore());
		res.setSeat(seat);
		XtbpTable table = getPlayingTable(XtbpTable.class);
		res.setBoom(table.getBanker()==seat?1:0);
		if (!StringUtils.isBlank(getHeadimgurl())) {
			res.setIcon(getHeadimgurl());
		} else {
			res.setIcon("");
		}

		res.setSex(sex);
		res.addExt(getPiaofen()+"");//0
		res.addExt(winCount+"");//1
		res.addExt(lostCount+"");//2
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
		res.setTotalPoint(loadScore());
		res.addAllCards(handPais);
		res.setSeat(seat);
		if (!StringUtils.isBlank(getHeadimgurl())) {
			res.setIcon(getHeadimgurl());
		} else {
			res.setIcon("");
		}
		XtbpTable table = getPlayingTable(XtbpTable.class);
		res.setBoom(table.getBanker()==seat?1:0);

		res.setSex(sex);
		res.addExt(getPiaofen()+"");//0
		res.addExt(winCount+"");//1
		res.addExt(lostCount+"");//2
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
		setBaofu(0);
		setJiaofen(-1);
		setLiushou(0);
		setTouXiang(0);
		setPiaofen(-1);
	}

	public void changeSeat() {
		BaseTable table = getPlayingTable();
		if (table != null)
			table.changeCards(seat);
	}
	
	

	public int getBaofu() {
		return baofu;
	}

	public void setBaofu(int baofu) {
		this.baofu = baofu;
		changeTableInfo();
	}

	
	
	public int getLiushou() {
		return liushou;
	}

	public void setLiushou(int liushou) {
		this.liushou = liushou;
		changeTableInfo();
	}
	
	

	public int getTouXiang() {
		return touXiang;
	}

	public void setTouXiang(int touXiang) {
		this.touXiang = touXiang;
		changeTableInfo();
	}

	@Override
	public void endCompetition1() {


	}


	

	public int getJiaofen() {
		return jiaofen;
	}

	public void setJiaofen(int jiaofen) {
		this.jiaofen = jiaofen;
		changeTableInfo();
	}

	public int getCutCard() {
		return cutCard;
	}

	public void setCutCard(int cutCard) {
		this.cutCard = cutCard;
	}

	public int getPiaofen() {
		return piaofen;
	}

	public void setPiaofen(int piaofen) {
		this.piaofen = piaofen;
	}

	public long getPiaofenCheckTime() {
		return piaofenCheckTime;
	}

	public void setPiaofenCheckTime(long piaofenCheckTime) {
		this.piaofenCheckTime = piaofenCheckTime;
	}

	public static final List<Integer> wanfaList = Arrays.asList(GameUtil.play_type_pk_xtbp);

	public static void loadWanfaPlayers(Class<? extends Player> cls){
		for (Integer integer:wanfaList){
			PlayerManager.wanfaPlayerTypesPut(integer,cls, XtbpCommandProcessor.getInstance());
		}
	}

}
