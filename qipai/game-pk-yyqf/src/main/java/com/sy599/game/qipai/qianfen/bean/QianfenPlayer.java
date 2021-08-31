package com.sy599.game.qipai.qianfen.bean;

import com.google.protobuf.GeneratedMessage;
import com.sy599.game.base.BaseGame;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.common.constant.SharedConstants.player_state;
import com.sy599.game.db.bean.group.GroupUser;
import com.sy599.game.db.dao.group.GroupDao;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.TableRes.ClosingPlayerInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes;
import com.sy599.game.qipai.qianfen.command.CommandProcessor;
import com.sy599.game.qipai.qianfen.util.CardUtils;
import com.sy599.game.qipai.qianfen.util.CardValue;
import com.sy599.game.util.GameUtil;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class QianfenPlayer extends Player implements BaseGame{
	// 座位id
	private volatile int seat;
	// 状态
	private volatile player_state state = player_state.entry;// 1进入 2已准备 3正在玩 4已结束
	private volatile int isEntryTable;
	private volatile List<CardValue> handPais;
	private volatile List<List<Integer>> outPais;
	/**
	 * 第几局，牌面分，炸弹分，奖惩分，名次、牌局结束奖励分
	 */
	private volatile List<List<Integer>> results=new ArrayList<>();

	public List<List<Integer>> getResults() {
		return results;
	}

	private volatile boolean autoPlay = false;//托管
	private volatile long lastOperateTime = 0;//最后操作时间
	private volatile long lastCheckTime = 0;//最后检查时间
	private volatile long nextAutoDisCardTime = 0;

	private volatile long autoPlayTime = 0;//自动操作时间

	public int calcCurrentAllscore(){
		int total = 0;
		int idx = getResults().size() - 1;
		if (idx >= 0){
			List<Integer> list = getResults().get(idx);
			total+=list.get(1).intValue();
			total+=list.get(2).intValue();
			total+=list.get(3).intValue();
			total+=list.get(5).intValue();
		}
		return total;
	}

	public int calcCardAndRankScore(){
		int total = 0;
		for (List<Integer> list:getResults()){
			total+=list.get(1).intValue();
			total+=list.get(3).intValue();
		}
		return total;
	}

	public int calcCardAndRankAndGiveScore(){
		int total = 0;
		for (List<Integer> list:getResults()){
			total+=list.get(1).intValue();
			total+=list.get(3).intValue();
			total+=list.get(5).intValue();
		}
		return total;
	}

	public int calcBoomScore(){
		int total = 0;
		for (List<Integer> list:getResults()){
			total+=list.get(2).intValue();
		}
		return total;
	}

	public int getLastBoomScore(){
		int total = 0;
		if(getResults().size()>=1){
			for (int i = 0; i < getResults().size()-1; i++) {
				total+=getResults().get(i).get(2).intValue();
			}
		}
		return total;
	}

	public QianfenPlayer() {
		handPais = new ArrayList<>();
		outPais = new ArrayList<>();
	}

	public int getSeat() {
		return seat;
	}

	public void setSeat(int seat) {
		this.seat = seat;
	}

	public void initPais(String hand, String out) {
		if (!StringUtils.isBlank(hand)) {
			this.handPais = CardUtils.loadCards(StringUtil.explodeToIntList(hand));
		}
		if (!StringUtils.isBlank(out)) {
			this.outPais = StringUtil.explodeToLists(out);
		}
	}

	public void initPais(List<Integer> hand, List<List<Integer>> out) {
		if (hand != null) {
			this.handPais = CardUtils.loadCards(hand);
		}
		if (out != null) {
			this.outPais = out;
		}
	}

	public void dealHandPais(List<Integer> pais) {
		this.handPais = CardUtils.loadCards(pais);
		getPlayingTable().changeCards(seat);
	}

	public List<Integer> getHandPais() {
		return CardUtils.loadCardIds(handPais);
	}

	public boolean isOver(){
		return handPais==null||handPais.size()==0;
	}

	public List<CardValue> getHandPais0() {
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

	public boolean addOutPais(List<CardValue> cards) {
		boolean ok=true;
		synchronized (this){
            List<CardValue> tempList=new ArrayList<>(handPais);
            for (CardValue cv:cards){
            	if (!tempList.remove(cv)){
					ok=false;
					break;
				}
			}
			if (ok){
				handPais = tempList;
				outPais.add(CardUtils.loadCardIds(cards));
			}
		}
		if (ok){
			getPlayingTable().changeCards(seat);
		}

		return ok;
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

		for (int m=0;m<results.size();m++){
			List<Integer> list = results.get(m);
			if (m>0){
				sb.append("|");
			}
			for (int n=0;n<list.size();n++){
				Integer integer=list.get(n);
				if (n>0){
					sb.append("_");
				}
				sb.append(integer);
		}
		}
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
			String rets=StringUtil.getValue(values, i++);
			if (StringUtils.isNotBlank(rets)&&rets.contains("_")){
				String[] results=rets.split("\\|");
				for (String result:results){
					if (StringUtils.isNotBlank(result)){
						this.results.add(StringUtil.explodeToIntList(result,"_"));
					}
				}
			}
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
		}
		boolean addLog = this.autoPlay != autoPlay;
		this.autoPlay = autoPlay;
		if(!autoPlay){
			setAutoPlayCheckedTimeAdded(false);
		}

		if(addLog) {
			StringBuilder sb = new StringBuilder("QF.tuoguan");
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
		QianfenTable table = getPlayingTable(QianfenTable.class);
		if (table == null) {
			LogUtil.e("userId="+this.userId+"table null-->" + getPlayingTableId());
			return null;
		}

		int mark = 0;
		if (isrecover) {
			// 0是否要的起 1是否报单 2是否暂离(1暂离0在线)
			List<Integer> recover = new ArrayList<>();

			int nowDisSeat = table.getNextDisCardSeat();
			if (recUserId == this.userId && table.getDisCardSeat() != seat && !table.getNowDisCardIds().isEmpty() && nowDisSeat == seat) {
				// 自己重连,现在轮到自己出牌,去打别人出的牌
				if (CardUtils.searchBiggerCardValues(handPais,table.getNowDisCardIds(),table.getCutCardVals()).size()>0) {
					recover.add(1);
					mark=1;
				} else {
					recover.add(0);
				}
			} else {
				//1要的起
				recover.add(0);
			}

			recover.add(handPais.size());
			recover.add(getIsOnline() == 1 ? isEntryTable : 0);
			res.addAllRecover(recover);
		}
		List<Integer> extList = new ArrayList<>();
		int cardNumber = handPais.size();
		extList.add(cardNumber);//0
		if(table == null || table.getMasterId() == userId){
			extList.add(1);
		}else{
			extList.add(0);
		}

		//2
		extList.add(table.getDisCardRound()==0?(table.getLastWinSeat()==seat?1:0):(table.getNowDisCardSeat()==seat&&mark!=1?1:0));
		//3名次
		extList.add(results.size()>0?results.get(results.size()-1).get(4):0);

		int score=0;

		//4历史总分
		int playedCount=table.getPlayedBureau();
		if (table.isOver()){
			playedCount-=1;
		}
		if (playedCount>0){
			int i=0;
			for (List<Integer> list:results){
				score+=list.get(1).intValue();
//				score+=list.get(2).intValue();
				score+=list.get(3).intValue();
				i++;
				if (i>=playedCount){
					break;
				}
			}
		}
		extList.add(score);

		score=0;
		//5本轮总分,6本轮牌面分，7炸弹分,8奖励分
		int len=results.size();
		if (len>0){
			score+=results.get(len-1).get(1).intValue();
//			score+=results.get(len-1).get(2).intValue();
			score+=results.get(len-1).get(3).intValue();

			extList.add(score);
			extList.add(results.get(len-1).get(1).intValue());
			extList.add(results.get(len-1).get(2).intValue());
			extList.add(results.get(len-1).get(5).intValue());
		}else{
			extList.add(0);
			extList.add(0);
			extList.add(0);
			extList.add(0);
		}
        //9总喜分
		extList.add(calcBoomScore());
		extList.add(autoPlay?1:0);//10托管
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

	public int getCountPoint(){
		int playedCount=table.getPlayedBureau();
		int score=0;
		if (playedCount>0){
			int i=0;
			for (List<Integer> list:results){
				score+=list.get(1).intValue();
				score+=list.get(3).intValue();
				i++;
				if (i>=playedCount){
					break;
				}
			}
		}
		return score;
	}

	public List<Integer> initResult(int... rets){
		List<Integer> list = new ArrayList<>(rets.length);
		for (int ret:rets){
			list.add(ret);
		}
		return list;
	}

	public void clearTableInfo() {
		handPais = new ArrayList<>();
		outPais = new ArrayList<>();
		results = new ArrayList<>();
		BaseTable table = getPlayingTable();
		boolean isCompetition = false;
		if (table != null && table.isCompetition()) {
			isCompetition = true;
			endCompetition();
		}
		setIsEntryTable(0);
		changeIsLeave(0);
//		getHandPais().clear();
//		getOutPais().clear();
		changeState(player_state.entry);
		setTotalBoom(0);
		setTotalPoint(0);
		setSeat(0);
		if (!isCompetition) {
			setPlayingTableId(0);
		}
		this.autoPlay = false;
		saveBaseInfo();
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
		res.setLeftCardNum(handPais.size());
		res.addAllCards(CardUtils.loadCardIds(handPais));
		res.setSeat(seat);
		if (!StringUtils.isBlank(getHeadimgurl())) {
			res.setIcon(getHeadimgurl());
		} else {
			res.setIcon("");
		}

		res.setSex(sex);
//		res.setTotalPoint(loadScore());

		res.setPoint(calcCurrentAllscore());
		res.setTotalPoint(getCountPoint());

		StringBuilder sb=new StringBuilder();
		for (List<Integer> pai:outPais){
			sb.append(";").append(StringUtil.implode(pai,","));
		}
		res.addExt(sb.length()>0?sb.substring(1):"");

		sb=new StringBuilder();
		for (List<Integer> result:results){
			sb.append(";").append(StringUtil.implode(result,","));
		}
		res.addExt(sb.length()>0?sb.substring(1):"");

		res.addExt(results.size()>table.getPlayedBureau()?results.get(table.getPlayedBureau()).get(4).toString():"0");
		res.addExt(""+getLastBoomScore());
		return res;
	}

	/**
	 * 总局详情
	 * 
	 * @return
	 */
	public ClosingPlayerInfoRes.Builder bulidTotalClosingPlayerInfoRes() {
		return bulidOneClosingPlayerInfoRes().setTotalPoint(loadScore());
	}

	public void changeTableInfo() {
		BaseTable table = getPlayingTable();
		if (table != null)
			table.changePlayers();
	}

	@Override
	public void initNext() {
		handPais = new ArrayList<>();
		outPais = new ArrayList<>();
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

	public static final List<Integer> wanfaList = Arrays.asList(GameUtil.game_type_qf);

	public static void loadWanfaPlayers(Class<? extends Player> cls){
		for (Integer integer:wanfaList){
			PlayerManager.wanfaPlayerTypesPut(integer,cls, CommandProcessor.getInstance());
		}
	}
}
