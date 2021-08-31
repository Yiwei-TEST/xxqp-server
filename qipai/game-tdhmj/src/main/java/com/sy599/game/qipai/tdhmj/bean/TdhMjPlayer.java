package com.sy599.game.qipai.tdhmj.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.protobuf.GeneratedMessage;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.FirstmythConstants;
import com.sy599.game.common.constant.SharedConstants.player_state;
import com.sy599.game.db.bean.group.GroupUser;
import com.sy599.game.db.dao.group.GroupDao;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.TableMjResMsg.ClosingMjPlayerInfoRes;
import com.sy599.game.msg.serverPacket.TablePhzResMsg.PhzHuCards;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes;
import com.sy599.game.qipai.tdhmj.command.TdhMjCommandProcessor;
import com.sy599.game.qipai.tdhmj.constant.TdhMjAction;
import com.sy599.game.qipai.tdhmj.constant.TdhMjConstants;
import com.sy599.game.qipai.tdhmj.rule.TdhMj;
import com.sy599.game.qipai.tdhmj.rule.TdhMjHelper;
import com.sy599.game.qipai.tdhmj.tool.TdhMjEnumHelper;
import com.sy599.game.qipai.tdhmj.tool.TdhMjQipaiTool;
import com.sy599.game.qipai.tdhmj.tool.TdhMjTool;
import com.sy599.game.util.GameUtil;
import com.sy599.game.util.JacksonUtil;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.util.StringUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

public class TdhMjPlayer extends Player {
	// 座位id
	private int seat;
	// 状态
	private player_state state;// 1进入 2已准备 3正在玩 4已结束
	private int isEntryTable;
	private List<TdhMj> handPais;
	private List<TdhMj> outPais;
	private List<TdhMj> peng;
	private List<TdhMj> aGang;
	private List<TdhMj> mGang;
	private List<TdhMj> chi;
	private List<TdhMj> buzhang;
	private List<TdhMj> buzhangAn;
	private int winCount;
	private int lostCount;
	private int lostPoint;
	private int gangPoint;// 要胡牌杠才算分
	private int point;
	/** 0点炮1杠2明杠3暗杠4被碰5被杠6胡7自摸 **/
	private int[] actionArr = new int[9];
	/** 0自摸1点炮2暗杠3明杠4大胡5小胡 **/
	private int[] actionTotalArr = new int[6];
	private List<Integer> dahu;
	private int passMajiangVal;
	private List<Integer> passGangValList;
	private List<TdhMjCardDisType> cardTypes;
	
	 private long lastPassTime;
	
	
    /**
     * [类型]=分值
     */
    private int[] pointArr = new int[4];
	
	/*** 胡的牌*/
	private List<Integer> huMjIds;
	/**
	 * 飘分
	 * 未开始抛分时该值为-1 表示还未进行抛分
	 * 牌局开始前玩家抛出的分数(相当于押注分) 抛出的分数在结算的时候  单方结算的分数=对方抛出的分数+自己抛出的分数+胡的基础分数
	 */
	private int piaoPoint = -1;

	/*** 过胡了的牌，过手后清空**/
	private List<Integer> passHuValList;
	
	
    private volatile boolean autoPlay = false;//是否进入托管状态
    private volatile boolean autoPlaySelf = false;//托管
    private volatile long lastCheckTime = 0;//最后检查时间
    private volatile long autoPlayTime = 0;//自动操作时间
    private volatile boolean checkAutoPlay = false; //是否是牌桌上的焦点
    private volatile long sendAutoTime = 0;//发送倒计时间
	
	
	private int autoMD=0;//自动摸打

	public TdhMjPlayer() {
		handPais = new ArrayList<TdhMj>();
		outPais = new ArrayList<TdhMj>();
		peng = new ArrayList<>();
		aGang = new ArrayList<>();
		mGang = new ArrayList<>();
		chi = new ArrayList<>();
		buzhang = new ArrayList<>();
		buzhangAn = new ArrayList<>();
		passGangValList = new ArrayList<>();
		dahu = new ArrayList<>();
		cardTypes = new ArrayList<>();
		piaoPoint = -1;
		passHuValList = new ArrayList<>();
		huMjIds = new ArrayList<>();
		
		autoPlaySelf = false;
		autoPlay = false;
		autoPlayTime = 0;
		checkAutoPlay = false;
		lastCheckTime = System.currentTimeMillis();
		autoMD = 0;
	}

	public int getSeat() {
		return seat;
	}

	public void setSeat(int seat) {
		this.seat = seat;
	}

	public void initPais(List<TdhMj> hand, List<TdhMj> out) {
		if (hand != null) {
			this.handPais = hand;

		}
		if (out != null) {
			this.outPais = out;

		}
	}

	public void dealHandPais(List<TdhMj> pais) {
		this.handPais = pais;
		getPlayingTable().changeCards(seat);
	}

	public boolean isGangshangHua() {
		return dahu.contains(7);
	}

	public List<TdhMj> getHandMajiang() {
		return handPais;
	}


	public List<Integer> getHandPais() {
		return TdhMjHelper.toMajiangIds(handPais);
	}

	public List<Integer> getOutPais() {
		return TdhMjHelper.toMajiangIds(outPais);
	}

	public List<TdhMj> getOutMajing() {
		return outPais;
	}

	public void moMajiang(TdhMj majiang) {
		setPassMajiangVal(0);
		handPais.add(majiang);
		getPlayingTable().changeCards(seat);
	}

	public void addOutPais(List<TdhMj> cards, int action, int disSeat) {
		handPais.removeAll(cards);
		if (action == 0) {
			outPais.addAll(cards);
		} else {
			if (action == TdhMjDisAction.action_buzhang) {
				buzhang.addAll(cards);
				TdhMj pengMajiang = cards.get(0);
				Iterator<TdhMj> iterator = peng.iterator();
				while (iterator.hasNext()) {
					TdhMj majiang = iterator.next();
					if (majiang.getVal() == pengMajiang.getVal()) {
						buzhang.add(majiang);
						iterator.remove();
					}
				}
			} else if (action == TdhMjDisAction.action_chi) {
				chi.addAll(cards);

			} else if (action == TdhMjDisAction.action_peng) {
				peng.addAll(cards);
				// changeAction(0, 1);
			} else if (action == TdhMjDisAction.action_minggang) {
				myExtend.setMjFengshen(FirstmythConstants.firstmyth_index12, 1);
				mGang.addAll(cards);
				if (cards.size() != 1) {
					changeAction(1, 1);
				} else {
					TdhMj pengMajiang = cards.get(0);
					Iterator<TdhMj> iterator = peng.iterator();
					while (iterator.hasNext()) {
						TdhMj majiang = iterator.next();
						if (majiang.getVal() == pengMajiang.getVal()) {
							mGang.add(majiang);
							iterator.remove();
						}
					}
					changeAction(2, 1);
				}

			} else if (action == TdhMjDisAction.action_angang) {
				myExtend.setMjFengshen(FirstmythConstants.firstmyth_index12, 1);
				aGang.addAll(cards);
				changeAction(3, 1);
			}
			else if (action == TdhMjDisAction.action_buzhang_an) {
				myExtend.setMjFengshen(FirstmythConstants.firstmyth_index12, 1);
				buzhangAn.addAll(cards);
				changeAction(8, 1);
			}
			getPlayingTable().changeExtend();
			addCardType(action, cards, disSeat,0);
		}

		getPlayingTable().changeCards(seat);
	}

	public void addCardType(int action, List<TdhMj> disCardList, int disSeat, int layEggId) {
		if (action != 0) {
			if (action == TdhMjDisAction.action_buzhang && disCardList.size() == 1) {
				TdhMj majiang = disCardList.get(0);
				for (TdhMjCardDisType disType : cardTypes) {
					if (disType.getAction() == TdhMjDisAction.action_peng) {
						if (disType.isHasCardVal(majiang)) {
							disType.setAction(TdhMjDisAction.action_buzhang);
							disType.addCardId(majiang.getId());
							disType.setDisSeat(seat);
							break;
						}
					}
				}
			} else if (action == TdhMjDisAction.action_minggang && disCardList.size() == 1) {
				TdhMj majiang = disCardList.get(0);
				for (TdhMjCardDisType disType : cardTypes) {
					if (disType.getAction() == TdhMjDisAction.action_peng) {
						if (disType.isHasCardVal(majiang)) {
							disType.setAction(TdhMjDisAction.action_minggang);
							disType.addCardId(majiang.getId());
							disType.setDisSeat(seat);
							break;
						}
					}
				}
			} else {
				TdhMjCardDisType type = new TdhMjCardDisType();
				type.setAction(action);
				type.setCardIds(TdhMjHelper.toMajiangIds(disCardList));
				type.setHux(layEggId);
				type.setDisSeat(disSeat);
				cardTypes.add(type);
			}
		}
	}

	/**
	 * 已经摸过牌了
	 *
	 * @return
	 */
	public boolean isAlreadyMoMajiang() {
		return !handPais.isEmpty() && handPais.size() % 3 == 2;
	}

	public void removeOutPais(List<TdhMj> cards, int action) {
		boolean remove = outPais.removeAll(cards);
		if (remove) {
			if (action == TdhMjDisAction.action_peng) {
				changeAction(4, 1);
			} else if (action == TdhMjDisAction.action_minggang) {
				changeAction(5, 1);
			}
			getPlayingTable().changeCards(seat);

		}
	}

	public String toExtendStr() {
		StringBuffer sb = new StringBuffer();
		sb.append(TdhMjHelper.implodeMajiang(peng, ",")).append("|");
		sb.append(TdhMjHelper.implodeMajiang(aGang, ",")).append("|");
		sb.append(TdhMjHelper.implodeMajiang(mGang, ",")).append("|");
		sb.append(StringUtil.implode(actionArr)).append("|");
		sb.append(StringUtil.implode(actionTotalArr)).append("|");
		sb.append(StringUtil.implode(passGangValList, ",")).append("|");
		sb.append(TdhMjHelper.implodeMajiang(chi, ",")).append("|");
		sb.append(TdhMjHelper.implodeMajiang(buzhang, ",")).append("|");
		sb.append(StringUtil.implode(dahu, ",")).append("|");
		sb.append(StringUtil.implode(passHuValList, ",")).append("|");
		sb.append(TdhMjHelper.implodeMajiang(buzhangAn, ",")).append("|");
		
		
		return sb.toString();

	}

	public void initExtend(String info) {
		if (StringUtils.isBlank(info)) {
			return;
		}
		int i = 0;
		String[] values = info.split("\\|");
		String val1 = StringUtil.getValue(values, i++);
		peng = TdhMjHelper.explodeMajiang(val1, ",");
		String val2 = StringUtil.getValue(values, i++);
		aGang = TdhMjHelper.explodeMajiang(val2, ",");
		String val3 = StringUtil.getValue(values, i++);
		mGang = TdhMjHelper.explodeMajiang(val3, ",");
		String val4 = StringUtil.getValue(values, i++);
		if (!StringUtils.isBlank(val4)) {
			actionArr = StringUtil.explodeToIntArray(val4);
		}
		String val5 = StringUtil.getValue(values, i++);
		if (!StringUtils.isBlank(val5)) {
			actionTotalArr = StringUtil.explodeToIntArray(val5);
		}
		String val6 = StringUtil.getValue(values, i++);
		if (!StringUtils.isBlank(val6)) {
			passGangValList = StringUtil.explodeToIntList(val6);
		}
		String val7 = StringUtil.getValue(values, i++);
		chi = TdhMjHelper.explodeMajiang(val7, ",");
		String val8 = StringUtil.getValue(values, i++);
		buzhang = TdhMjHelper.explodeMajiang(val8, ",");
		String val9 = StringUtil.getValue(values, i++);
		if (!StringUtils.isBlank(val9)) {
			dahu = StringUtil.explodeToIntList(val9);
		}
		String val10 = StringUtil.getValue(values, i++);
		String val11 = StringUtil.getValue(values, i++);
		String val12 = StringUtil.getValue(values, i++);
		if (!StringUtils.isBlank(val12)) {
			passHuValList = StringUtil.explodeToIntList(val12);
		}
		String val13 = StringUtil.getValue(values, i++);
		buzhangAn = TdhMjHelper.explodeMajiang(val13, ",");
		
	}

	public String toInfoStr() {
		StringBuffer sb = new StringBuffer();
		sb.append(getUserId()).append(",");
		sb.append(seat).append(",");
		int stateVal = 0;
		if (state != null) {
			stateVal = state.getId();
		}
		sb.append(stateVal).append(",");
		sb.append(isEntryTable).append(",");
		sb.append(winCount).append(",");
		sb.append(lostCount).append(",");
		sb.append(point).append(",");
		sb.append(loadScore()).append(",");
		sb.append(lostPoint).append(",");
		sb.append(passMajiangVal).append(",");
		sb.append(gangPoint).append(",");
		sb.append(piaoPoint).append(",");
		
        sb.append(autoPlay ? 1 : 0).append(",");
        sb.append(autoPlaySelf ? 1 : 0).append(",");
        sb.append(autoPlayTime).append(",");
        sb.append(lastCheckTime).append(",");
        sb.append(autoMD).append(",");
        
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
			this.state = TdhMjEnumHelper.getPlayerState(stateVal);
			this.isEntryTable = StringUtil.getIntValue(values, i++);
			this.winCount = StringUtil.getIntValue(values, i++);
			this.lostCount = StringUtil.getIntValue(values, i++);
			this.point = StringUtil.getIntValue(values, i++);
			setTotalPoint(StringUtil.getIntValue(values, i++));
			this.lostPoint = StringUtil.getIntValue(values, i++);
			this.passMajiangVal = StringUtil.getIntValue(values, i++);
			this.gangPoint = StringUtil.getIntValue(values, i++);
			this.piaoPoint = StringUtil.getIntValue(values, i++);
			this.autoPlay = StringUtil.getIntValue(values, i++) == 1;
            this.autoPlaySelf = StringUtil.getIntValue(values, i++) == 1;
            this.autoPlayTime = StringUtil.getLongValue(values, i++);
            this.lastCheckTime = StringUtil.getLongValue(values, i++);
            this.autoMD = StringUtil.getIntValue(values, i++);
            
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
		changeTbaleInfo();
	}

	public PlayerInTableRes.Builder buildPlayInTableInfo() {
		return buildPlayInTableInfo(false);
	}

	/**
	 * 吃碰杠过的牌
	 *
	 * @return
	 */
	public List<Integer> getMoldIds() {
		List<Integer> list = new ArrayList<>();
		if (!peng.isEmpty()) {
			list.addAll(TdhMjHelper.toMajiangIds(peng));
		}
		if (!mGang.isEmpty()) {
			list.addAll(TdhMjHelper.toMajiangIds(mGang));
		}
		if (!aGang.isEmpty()) {
			list.addAll(TdhMjHelper.toMajiangIds(aGang));
		}
		if (!buzhang.isEmpty()) {
			list.addAll(TdhMjHelper.toMajiangIds(buzhang));
		}
		if (!buzhangAn.isEmpty()) {
			list.addAll(TdhMjHelper.toMajiangIds(buzhangAn));
		}
		if (!chi.isEmpty()) {
			list.addAll(TdhMjHelper.toMajiangIds(chi));
		}

		return list;
	}

	public List<PhzHuCards> buildDisCards(long lookUid) {
		return buildDisCards(lookUid, true);
	}

	public List<PhzHuCards> buildDisCards(long lookUid, boolean hide) {
		List<PhzHuCards> list = new ArrayList<>();
		for (TdhMjCardDisType type : cardTypes) {
			if (hide && lookUid != this.userId && type.getAction() == TdhMjDisAction.action_angang) {
				// 不是本人并且是栽
				list.add(type.buildMsg(true).build());
			} else {
				list.add(type.buildMsg().build());
			}

		}
		return list;
	}

	/**
	 * @param isrecover
	 *            是否重连
	 * @return
	 */
	public PlayerInTableRes.Builder buildPlayInTableInfo(boolean isrecover) {
		PlayerInTableRes.Builder res = PlayerInTableRes.newBuilder();
		res.setUserId(userId + "");
		if (!StringUtils.isBlank(ip)) {
			res.setIp(ip);
		} else {
			res.setIp("");
		}
		res.setName(name);
		res.setSeat(seat);
		res.setSex(sex);
		res.setPoint(loadScore());
		res.addAllOutedIds(getOutPais());
		res.addAllMoldIds(getMoldIds());
		res.addAllAngangIds(TdhMjHelper.toMajiangIds(aGang));
		res.addAllAngangIds(TdhMjHelper.toMajiangIds(buzhangAn));
		res.addAllMoldCards(buildDisCards(userId));
		List<TdhMj> gangList = getGang();
		// 是否杠过
		res.addExt(gangList.isEmpty() ? 0 : 1);
		
		
		TdhMjTable table = getPlayingTable(TdhMjTable.class);
		// 现在是否自己摸的牌
		res.addExt(isAlreadyMoMajiang() ? 1 : 0);
		res.addExt(handPais != null ? handPais.size() : 0);
		res.addExt(piaoPoint);// 飘分
        res.addExt(autoPlay ? 1 : 0);//4
        res.addExt(autoPlaySelf ? 1 : 0);//5
        res.addExt(autoMD);//5
        
		
		
		res.addExt(Integer.valueOf(getPayBindId()+"")); //绑定邀请码
		if (!StringUtils.isBlank(getHeadimgurl())) {
			res.setIcon(getHeadimgurl());
		} else {
			res.setIcon("");
		}
		if (state == player_state.ready || state == player_state.play) {
			// 玩家装备已经准备和正在玩的状态时通知前台已准备
			res.setStatus(1);
		} else {
			res.setStatus(0);
		}
		
		 if (table.isCreditTable()) {
	            GroupUser gu = getGroupUser();
	            String groupId = table.loadGroupId();
	            if (gu == null || !groupId.equals(gu.getGroupId() + "")) {
	                gu = GroupDao.getInstance().loadGroupUser(getUserId(), groupId);
	            }
	            res.setCredit(gu != null ? gu.getCredit() : 0);
	        }

		if (isrecover) {
			// 0是否要的起 1是否报单 2是否暂离(1暂离0在线)
			List<Integer> recover = new ArrayList<>();
			recover.add(isEntryTable);
			res.addAllRecover(recover);
		}
		return buildPlayInTableInfo1(res);
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

	// public void calcLost(int lostCount, int point) {
	// this.lostCount += lostCount;
	// changePoint(point);
	//
	// }
	//
	public void calcWin() {
		// changeLostPoint(gangPoint);
	}

	public int getPoint() {
		return point;
	}

	public void setPoint(int point) {
		this.point = point;
	}

	public void setGangPoint(int gangPoint) {
		this.gangPoint = gangPoint;
	}

	public void setLostPoint(int lostPoint) {
		this.lostPoint = lostPoint;
		changeTbaleInfo();
	}

	public void changeLostPoint(int point) {
		this.lostPoint += point;
		changeTbaleInfo();
	}

	public void changeGangPoint(int point) {
		this.gangPoint += point;
		changeTbaleInfo();
	}

	public int getGangPoint() {
		return gangPoint;
	}

	public int getLostPoint() {
		return lostPoint;
	}

	public void setPiaoPoint(int piaoPoint) {
		this.piaoPoint = piaoPoint;
		changeTableInfo();
	}

	public int getPiaoPoint() {
		return piaoPoint;
	}

	
	
	
	public int getAutoMD() {
		return autoMD;
	}

	public void setAutoMD(int autoMD) {
		this.autoMD = autoMD;
		changeTbaleInfo();
	}

	public void changePoint(int point) {
		this.point += point;
		myExtend.changePoint(getPlayingTable().getPlayType(), point);
		myExtend.setMjFengshen(FirstmythConstants.firstmyth_index7, point);
		changeTotalPoint(point);
		if (point > getMaxPoint()) {
			setMaxPoint(point);
		}
		changeTbaleInfo();
	}

	public void clearTableInfo() {
		BaseTable table = getPlayingTable();
		boolean isCompetition = false;
		if (table != null && table.isCompetition()) {
			isCompetition = true;
			endCompetition();
		}
		setIsEntryTable(0);
		changeIsLeave(0);
		getHandMajiang().clear();
		getOutMajing().clear();
		changeState(null);
		actionArr = new int[9];
		actionTotalArr = new int[6];
		dahu = new ArrayList<>();
		peng.clear();
		aGang.clear();
		mGang.clear();
		chi.clear();
		buzhang.clear();
		buzhangAn.clear();
		cardTypes.clear();
		setPassMajiangVal(0);
		clearPassGangVal();
		setWinCount(0);
		setLostCount(0);
		setPoint(0);
		setGangPoint(0);
		setLostPoint(0);
		setTotalPoint(0);
		setSeat(0);
		setPiaoPoint(-1);
		if (!isCompetition) {
			setPlayingTableId(0);
		}
		huMjIds = new ArrayList<>();
		saveBaseInfo();
		
		autoPlaySelf = false;
		autoPlay = false;
		lastCheckTime = System.currentTimeMillis();
		checkAutoPlay = false;
        pointArr = new int[4];
        setAutoMD(0);
	}

	/**
	 * 单局详情
	 *
	 * @return
	 */
	public ClosingMjPlayerInfoRes.Builder buildOneClosingPlayerInfoRes() {
		ClosingMjPlayerInfoRes.Builder res = ClosingMjPlayerInfoRes.newBuilder();
		res.setUserId(userId + "");
		res.setName(name);
		res.setPoint(point);
		res.setTotalPoint(getTotalPoint());
		res.setSeat(seat);
		if (!StringUtils.isBlank(getHeadimgurl())) {
			res.setIcon(getHeadimgurl());
		} else {
			res.setIcon("");
		}
		 res.addAllPointArr(Arrays.asList(ArrayUtils.toObject(pointArr)));
		res.setSex(sex);
		res.addAllHandPais(getHandPais());
		List<PhzHuCards> list = new ArrayList<>();
		for (TdhMjCardDisType type : cardTypes) {
			list.add(type.buildMsg().build());
		}
		res.addAllMoldPais(list);
		res.addAllDahus(dahu);
		//res.setFanPao(actionArr[0]==1?1:0);

		res.addExt(getPiaoPoint());
		return res;
	}

	public int[] buildActionArr() {
		// 点炮 接炮 自摸 中鸟
		int[] result = new int[3];
		if (actionArr[0] != 0) {
			result[0] = actionArr[0];
		}
		if (actionArr[6] == 1) {
			result[1] = 1;
		}
		if (actionArr[7] == 1) {
			result[2] = 1;
		}
		return result;
	}

	/**
	 * 总局详情
	 *
	 * @return
	 */
	public ClosingMjPlayerInfoRes.Builder buildTotalClosingPlayerInfoRes() {

		ClosingMjPlayerInfoRes.Builder res = ClosingMjPlayerInfoRes.newBuilder();
		res.setUserId(userId + "");
		res.setName(name);
		res.setPoint(point);
		res.addAllActionCount(Arrays.asList(ArrayUtils.toObject(actionTotalArr)));
		res.setTotalPoint(getTotalPoint());
		 res.addAllPointArr(Arrays.asList(ArrayUtils.toObject(pointArr)));
		res.setSeat(seat);
		if (!StringUtils.isBlank(getHeadimgurl())) {
			res.setIcon(getHeadimgurl());
		} else {
			res.setIcon("");
		}
		res.setSex(sex);
		res.addAllHandPais(getHandPais());
		List<PhzHuCards> list = new ArrayList<>();
		for (TdhMjCardDisType type : cardTypes) {
			list.add(type.buildMsg().build());
		}
		
		res.addAllMoldPais(list);
		res.addAllDahus(dahu);
		return res;
	}

	public void changeTbaleInfo() {
		BaseTable table = getPlayingTable();
		if (table != null)
			table.changePlayers();
	}

	@Override
	public void initNext() {
		getHandMajiang().clear();
		getOutMajing().clear();
		setPoint(0);
		setGangPoint(0);
		setPassMajiangVal(0);
		setLostPoint(0);
		clearPassGangVal();
		peng.clear();
		aGang.clear();
		mGang.clear();
		chi.clear();
		buzhang.clear();
		buzhangAn.clear();
		cardTypes.clear();
		actionArr = new int[9];
		dahu = new ArrayList<>();
		getPlayingTable().changeExtend();
		getPlayingTable().changeCards(seat);
		changeState(player_state.entry);
		setPiaoPoint(-1);
		clearPassHu();
		huMjIds = new ArrayList<>();
		if (autoPlaySelf) {
			autoPlaySelf = false;
			autoPlay = false;
			checkAutoPlay = false;
			lastCheckTime = System.currentTimeMillis();
		}
		if (!autoPlay) {
			checkAutoPlay = false;
			lastCheckTime = System.currentTimeMillis();
		}
		setAutoMD(0);
        pointArr = new int[4];
	}

	@Override
	public void initPais(String handPai, String outPai) {
		if (!StringUtils.isBlank(handPai)) {
			List<Integer> list = StringUtil.explodeToIntList(handPai);
			this.handPais = TdhMjHelper.toMajiang(list);
		}

		if (!StringUtils.isBlank(outPai)) {
			String[] values = outPai.split(";");
			int i = -1;
			for (String value : values) {
				i++;
				if (i == 0) {
					List<Integer> list = StringUtil.explodeToIntList(value);
					this.outPais = TdhMjHelper.toMajiang(list);
				} else {
					TdhMjCardDisType type = new TdhMjCardDisType();
					type.init(value);
					cardTypes.add(type);

					List<TdhMj> majiangs = TdhMjHelper.toMajiang(type.getCardIds());
					if (type.getAction() == TdhMjDisAction.action_angang) {
						aGang.addAll(majiangs);
					} else if (type.getAction() == TdhMjDisAction.action_minggang) {
						mGang.addAll(majiangs);
					} else if (type.getAction() == TdhMjDisAction.action_chi) {
						chi.addAll(majiangs);
					} else if (type.getAction() == TdhMjDisAction.action_peng) {
						peng.addAll(majiangs);
					} else if (type.getAction() == TdhMjDisAction.action_buzhang) {
						buzhang.addAll(majiangs);
					}else if (type.getAction() == TdhMjDisAction.action_buzhang_an) {
						buzhangAn.addAll(majiangs);
					}
				}
			}
		}

//		if (!StringUtils.isBlank(outPai)) {
//			List<Integer> list = StringUtil.explodeToIntList(outPai);
//			this.outPais = MajiangHelper.toMajiang(list);
//
//		}
	}

	/**
	 * 可以吃这张麻将的牌
	 *
	 * @param disMajiang
	 * @return
	 */
	public List<TdhMj> getCanChiMajiangs(TdhMj disMajiang) {
		return TdhMjTool.checkChi(handPais, disMajiang);
	}

	/**
	 * 是否听牌
	 * @param val
	 * @return
	 */
	public boolean isTingPai(int val) {
		List<TdhMj> copy = new ArrayList<>(handPais);
		List<TdhMj> gangList = getGang();
		gangList.addAll(TdhMjQipaiTool.dropVal(copy, val));

		List<TdhMj> copyPeng = new ArrayList<>(peng);
		if (!peng.isEmpty()) {
			gangList.addAll(TdhMjQipaiTool.dropVal(copyPeng, val));
		}

		if (copy.size() % 3 != 2) {
			copy.add(TdhMj.getMajang(201));
		}
		List<TdhMj> bzCopy = new ArrayList<>(buzhang);
		bzCopy.addAll(buzhangAn);
		TdhMjTable table = getPlayingTable(TdhMjTable.class);
		TdhMjiangHu huBean = TdhMjTool.isHuTuiDaoHu(copy, table, this, null, true);
		
		if(huBean.isHu()) {
			if(getChi().size()>0&&!huBean.isQingyiseHu() ) {
				huBean.setHu(false);
			}
		}
		
		return huBean.isHu();
	}

	/**
	 * 出牌
	 *
	 * @return
	 */
	public String buildOutPaiStr() {
		StringBuffer sb = new StringBuffer();
		List<Integer> outPais = getOutPais();
		sb.append(StringUtil.implode(outPais)).append(";");
		for (TdhMjCardDisType huxi : cardTypes) {
			sb.append(huxi.toStr()).append(";");
		}

		return sb.toString();
	}

	/**
	 * 别人出牌可做的操作
	 *
	 * @param majiang
	 * @return
	 */
	public List<Integer> checkDisMajiang(TdhMj majiang) {
		return checkDisMaj(majiang,true);
	}

	public List<Integer> checkDisMaj(TdhMj majiang,boolean need258) {
		List<Integer> list = new ArrayList<>();
		TdhMjAction actData = new TdhMjAction();
		TdhMjTable table = getPlayingTable(TdhMjTable.class);
		// 没有出现漏炮的情况
		// if (passMajiangVal != majiang.getVal()) {
		if ((!table.moHu() || table.getDisCardSeat() == seat) && passMajiangVal == 0) {
			List<TdhMj> copy = new ArrayList<>(handPais);
			copy.add(majiang);
			List<TdhMj> bzCopy = new ArrayList<>(buzhang);
			bzCopy.addAll(buzhangAn);
			boolean  zimo =table.getMoMajiangSeat() == seat;
			if(need258) {
				zimo = false;
			}
			TdhMjiangHu hu = TdhMjTool.isHuTuiDaoHu(copy, table, this, majiang, zimo);
			if (hu.isHu()) {
				if(!need258) {
					if(chi.size()==0 ||  chi.get(0).getColourVal()== majiang.getColourVal()) {
						actData.addHu();
					}
					
				}else if(hu.isDahu()){
					actData.addHu();
				}
			}
		}
		List<TdhMj> gangList = getGang();
		if (table.getDisCardSeat() != seat) {
			// 现在出牌的人不是自己
			int count = TdhMjHelper.getMajiangCount(handPais, majiang.getVal());
			if (count == 3) {
				if (canPengGang(table, majiang)) {
					boolean isTing = isTingPai(majiang.getVal());
					if (isTing) {
						actData.addMingGang();
						actData.addBuZhang();
					}else{
						actData.addBuZhang();
					}
				}
			}
			if (gangList.isEmpty()) {
				if (count >= 2) {
					if (canPengGang(table, majiang)) {
						actData.addPeng();
					}
				}
				if (table.calcNextSeat(table.getDisCardSeat()) == seat) {
					// List<TdhMj> chi = TdhMjTool.checkChi(handPais, majiang);
					List<TdhMj> chi = TdhMjTool.checkChi(handPais, majiang);
					if (!chi.isEmpty()&&table.getQingyiseChi() == 1 && canChi(majiang)) {
						actData.addChi();
					}
				}

			}
		} else {
			// 出牌的人是自己 (杠后补张)
			int count = TdhMjHelper.getMajiangCount(handPais, majiang.getVal());
			if (count == 3) {
				if (canPengGang(table, majiang)) {
					boolean isTing = isTingPai(majiang.getVal());
					if (isTing) {
						actData.addAnGang();
						actData.addBuZhangAn();
					}else{
						actData.addBuZhangAn();
					}
				}
			}
			Map<Integer, Integer> pengMap = TdhMjHelper.toMajiangValMap(peng);
			if (pengMap.containsKey(majiang.getVal())) {
				if (canPengGang(table, majiang)) {
					boolean isTing = isTingPai(majiang.getVal());
					if (isTing) {
						actData.addMingGang();
						actData.addBuZhang();
					}else{
						actData.addBuZhang();
					}
				}
			}
		}
		int [] arr = actData.getArr();
		for (int val : arr) {
			list.add(val);
		}
		if (list.contains(1)) {
			return list;
		} else {
			return Collections.EMPTY_LIST;
		}
	}

	public TdhMj getLastMoMajiang() {
		if (handPais.isEmpty()) {
			return null;

		} else {
			return handPais.get(handPais.size() - 1);

		}
	}


	public void checkDahu() {
	}

	public int getDahuCount() {
		return dahu.size();
	}

	public int getDahuPointCount(){
		return TdhMjiangHu.getDaHuPointCount(dahu);
	}
	
	public List<Integer> getDahuPointCount2(){
		return TdhMjiangHu.getDaHuPointCount2(dahu);
	}

	public List<Integer> getDahu() {
		return dahu;
	}

	public void setDahu(List<Integer> dahu) {
		this.dahu = dahu;
		getPlayingTable().changeExtend();
	}

	/**
	 * 胡牌
	 *
	 * @param disMajiang
	 * @param isbegin
	 * @return
	 */
	public TdhMjiangHu checkHu(TdhMj disMajiang, boolean isbegin) {
		List<TdhMj> copy = new ArrayList<>(handPais);
		if (disMajiang != null) {
			copy.add(disMajiang);
		}
		List<TdhMj> bzCopy = new ArrayList<>(buzhang);
		bzCopy.addAll(buzhangAn);
		TdhMjTable table = getPlayingTable(TdhMjTable.class);
		
		boolean zimo = false;
		boolean isMoMjF = isAlreadyMoMajiang();
		
		if(!isMoMjF&&table.isHasGangAction(seat)){
			isMoMjF = true;
		}
		
		if(isMoMjF&&table.getMoMajiangSeat()==seat){
			zimo = true;
		}
		
		TdhMjiangHu hu = TdhMjTool.isHuTuiDaoHu(copy, table, this, disMajiang,zimo );
		return hu;
	}
	
	
	public boolean isChiPengGang(){
		if(chi.size() >0 || mGang.size() >0 || peng.size()>0 ||buzhang.size()>0) {
			return true;
		}
		return false;
	}

	/**
	 * 自己出牌可做的操作
	 *
	 * @param majiang
	 *            null 时自己碰或者吃,不能胡牌
	 * @param isBegin
	 *            起牌是第一次出牌
	 * @return
	 */
	public List<Integer> checkMo(TdhMj majiang, boolean isBegin) {
		List<Integer> list = new ArrayList<>();
		TdhMjAction actData = new TdhMjAction();
		TdhMjTable table = getPlayingTable(TdhMjTable.class);
		if (isAlreadyMoMajiang() || majiang != null || isBegin) {
			// 碰的时候判断不能胡牌
			List<TdhMj> copy = new ArrayList<>(handPais);
			TdhMjiangHu hu = TdhMjTool.isHuTuiDaoHu(copy, table, this, majiang, true);
			if (hu.isHu()) {
				if(chi.size()==0 ||  (majiang!=null &&chi.get(0).getColourVal()== majiang.getColourVal())) {
					actData.addZiMo();
				}	
			}
		}
		if (isAlreadyMoMajiang()) {
			List<TdhMj> gangList = getGang();
			Map<Integer, Integer> pengMap = TdhMjHelper.toMajiangValMap(peng);

			for (TdhMj handMajiang : handPais) {
				if (pengMap.containsKey(handMajiang.getVal())) {
					// 有碰过
					boolean isTing = isTingPai(handMajiang.getVal());
					if (isTing) {
						actData.addMingGang();// 可以杠
						if(table.getGangBubu()!=1||!isPassGang(handMajiang)) {
							actData.addBuZhang();// 可以补张
						}
						break;
					}else{
							if(table.getGangBubu()==1&&isPassGang(handMajiang)) {
								continue;
							}
							actData.addBuZhang();// 可以补张
					}
				}
			}
			Map<Integer, Integer> handMap = TdhMjHelper.toMajiangValMap(handPais);
			if (handMap.containsValue(4)) {
				for (Map.Entry<Integer, Integer> entry : handMap.entrySet()) {
					if (entry.getValue() == 4) {
						if (canPengGang(table, TdhMjHelper.findMajiangByVal(handPais, entry.getKey()))) {
							boolean isTing = isTingPai(entry.getKey());
							if (isTing) {
								actData.addAnGang();// 可以杠
								actData.addBuZhangAn(); // 可以补张
								break;
							}else{
								actData.addBuZhangAn(); // 可以补张
							}
						}
					}
				}
			}

//			// 只能杠抓上来的那张
//			if (majiang != null && pengMap.containsKey(majiang.getVal())) {
//				boolean isTing = isTingPai(majiang.getVal());
//				if (isTing) {
//					actData.addMingGang();// 可以杠
//				}
//				actData.addBuZhang();// 可以补张
//			}
		}
        int [] arr = actData.getArr();
		for (int val : arr) {
			list.add(val);
		}
		if (list.contains(1)) {
			return list;
		} else {
			return Collections.EMPTY_LIST;
		}
	}

	/**
	 * 操作
	 *
	 * @param index
	 *            0点炮1点杠2明杠3暗杠4被碰5被杠6胡7自摸,8暗杠补张
	 * @param val
	 */
	public void changeAction(int index, int val) {
		actionArr[index] += val;
		getPlayingTable().changeExtend();
	}

	public void changeActionTotal(int index , int val){
		actionTotalArr[index] += val;
		getPlayingTable().changeExtend();
	}

	public List<TdhMj> getPeng() {
		return peng;
	}

	public List<TdhMj> getaGang() {
		return aGang;
	}

	public List<TdhMj> getmGang() {
		return mGang;
	}

	public List<TdhMj> getGang() {
		List<TdhMj> gang = new ArrayList<>();
		gang.addAll(aGang);
		gang.addAll(mGang);
		return gang;
	}

	public int getPassMajiangVal() {
		return passMajiangVal;
	}

	/**
	 * 漏炮
	 *
	 * @param passMajiangVal
	 */
	public void setPassMajiangVal(int passMajiangVal) {
		if (this.passMajiangVal != passMajiangVal) {
			this.passMajiangVal = passMajiangVal;
			changeTbaleInfo();
		}

	}

	/**
	 * 可以碰可以杠的牌 选择了碰 再杠不算分
	 *
	 * @param majiang
	 * @return
	 */
	public boolean isPassGang(TdhMj majiang) {
		return passGangValList.contains(majiang.getVal());
	}
	
	public boolean isPassGang2(int val) {
		return passGangValList.contains(val);
	}

	public List<Integer> getPassGangValList() {
		return passGangValList;
	}

	/**
	 * 可以碰可以杠的牌 选择了碰 再杠不算分
	 *
	 * @param passGangVal
	 */
	public void addPassGangVal(int passGangVal) {
		if (!this.passGangValList.contains(passGangVal)) {
			this.passGangValList.add(passGangVal);
			getPlayingTable().changeExtend();
		}
	}

	public void clearPassGangVal() {
		this.passGangValList.clear();
		BaseTable table = getPlayingTable();
		if (table != null) {
			table.changeExtend();
		}
	}

	public static void main(String[] args) {
		List<Integer> l = new ArrayList<>(4);
		l.set(2, 0);
		System.out.println(JacksonUtil.writeValueAsString(l));
	}

	@Override
	public void endCompetition1() {

	}

	public static final List<Integer> wanfaList = Arrays.asList(GameUtil.game_type_TdhMj);

	public static void loadWanfaPlayers(Class<? extends Player> cls){
		for (Integer integer:wanfaList){
			PlayerManager.wanfaPlayerTypesPut(integer,cls, TdhMjCommandProcessor.getInstance());
		}
	}
	
	
    /**
     * 检查托管
     *
     * @param autoType  0打牌，1准备，2胡牌
     * @param forceSend 立即推送倒计时，用于断线重连
     * @return
     */
    public boolean checkAutoPlay(int autoType, boolean forceSend) {
    	TdhMjTable table = getPlayingTable(TdhMjTable.class);
        long now = System.currentTimeMillis();
        boolean auto = isAutoPlay();
        if (!auto && table.getIsAutoPlay() > 0) {
            //检查玩家是否进入系统托管状态
            if (!checkAutoPlay && table.getTableStatus()!= TdhMjConstants.TABLE_STATUS_PIAO) {
                if (isAlreadyMoMajiang() || table.getActionSeatMap().containsKey(seat)) {
                    setCheckAutoPlay(true);
                } else {
                    setCheckAutoPlay(false);
                    return false;
                }
            }

            int timeOut = (int) (now - getLastCheckTime()) / 1000;
            if (timeOut >= table.getIsAutoPlay()) {
                //进入托管状态
                auto = true;
                setAutoPlay(true, false);
                setAutoPlayCheckedTime(table.getIsAutoPlay()); //进入过托管就启用防恶意托管
            } else {
                if (sendAutoTime == 0) {
                    sendAutoTime = now;
                    setLastCheckTime(now);
                    timeOut = (int) (now - getLastCheckTime()) / 1000;
                }
                if (timeOut >= 10) {
                    addAutoPlayCheckedTime(1);
                }
                int autoTimeOut = table.getIsAutoPlay();
                if (getAutoPlayCheckedTime() >= table.getIsAutoPlay()) {
                    //进入防恶意托管
                    if (timeOut >= table.getIsAutoPlay()) {
                        auto = true;
                        setAutoPlay(true, false);
                    }
                    autoTimeOut = table.getIsAutoPlay();
                }
                if ((timeOut % 3 == 0 && isOnline()) || forceSend) {
                    int timeSecond = autoTimeOut - timeOut;
                    //推送即将进入托管状态的倒计时
                    ComMsg.ComRes.Builder res = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_tuoguan, seat, (autoPlay ? 1 : 0), timeSecond, autoPlaySelf ? 1 : 0);
                    GeneratedMessage msg = res.build();
                    table.broadMsg(msg);
                }
            }
        }
        if (auto) {
            if (getAutoPlayTime() == 0L) {
                setAutoPlayTime(now);
                if (autoType != 1) {
                    return true;
                }
            } else {
                int timeOut = (int) (now - getAutoPlayTime()) / 1000;
                if (autoType == 1) {
                    if (timeOut >= TdhMjConstants.AUTO_READY_TIME) {
                        setAutoPlayTime(0);
                        return true;
                    }
                } else if (autoType == 2) {
                    if (timeOut >= TdhMjConstants.AUTO_HU_TIME) {
                        setAutoPlayTime(0);
                        return true;
                    }
                } else {
                    if (timeOut >=TdhMjConstants.AUTO_PLAY_TIME) {
                        setAutoPlayTime(0);
                        return true;
                    }
                }
            }
        }
        return false;
    }
    

    public boolean isAutoPlaySelf() {
        return autoPlaySelf;
    }

    public boolean isAutoPlay() {
        return autoPlay;
    }
    
    
    public void setAutoPlay(boolean autoPlay, boolean isSelf) {
        TdhMjTable table = getPlayingTable(TdhMjTable.class);
        if (this.autoPlay != autoPlay || autoPlaySelf != isSelf) {
            ComMsg.ComRes.Builder res = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_tuoguan, seat, autoPlay ? 1 : 0, 0, isSelf ? 1 : 0);
            GeneratedMessage msg = res.build();
            if (table != null) {
                table.broadMsgToAll(msg);
            }
        	if (!getHandPais().isEmpty()) {
            table.addPlayLog(table.getDisCardRound() + "_" +getSeat() + "_" + TdhMjConstants.action_tuoguan + "_" +(autoPlay?1:0));
        	}

            StringBuilder sb = new StringBuilder("TdhMj");
            if (table != null) {
                sb.append("|").append(table.getId());
                sb.append("|").append(table.getPlayBureau());
                sb.append("|").append(table.getIsAutoPlay());
            }
            sb.append("|").append(getUserId());
            sb.append("|").append(getSeat());
            sb.append("|").append("setAutoPlay");
            sb.append("|").append(autoPlay ? 1 : 0);
            sb.append("|").append(isSelf ? 1 : 0);
            LogUtil.msg(sb.toString());
        }
        this.autoPlay = autoPlay;
        this.autoPlaySelf = autoPlay && isSelf;
        this.checkAutoPlay = autoPlay;
        setLastCheckTime(System.currentTimeMillis());
        if (table != null) {
            table.changeExtend();
        }
    }
    

    public long getLastCheckTime() {
        return lastCheckTime;
    }

    public void setLastCheckTime(long lastCheckTime) {
        this.lastCheckTime = lastCheckTime;
        if (getPlayingTable() != null) {
            getPlayingTable().changeExtend();
        }
    }

    public long getAutoPlayTime() {
        return autoPlayTime;
    }

    public void setAutoPlayTime(long autoPlayTime) {
        this.autoPlayTime = autoPlayTime;
//		getPlayingTable().changeExtend();
    }

    public boolean isCheckAutoPlay() {
        return checkAutoPlay;
    }

    public void setCheckAutoPlay(boolean checkAutoPlay) {
        this.checkAutoPlay = checkAutoPlay;
        this.lastCheckTime = System.currentTimeMillis();
        this.sendAutoTime = 0;
        if (getPlayingTable() != null) {
            getPlayingTable().changeExtend();
        }
    }
    
	

	/**
	 * 漏胡
	 *
	 * @param mjVal
	 */
	public void passHu(int mjVal) {
		if (null == passHuValList) {
			passHuValList = new ArrayList<>();
		}
		passHuValList.add(mjVal);
		getPlayingTable().changeExtend();
	}

	/**
	 * 过手，清空漏胡
	 */
	public void clearPassHu() {
		if (null != passHuValList) {
			passHuValList.clear();
		} else {
			passHuValList = new ArrayList<>();
		}
	}

	public void addHuMjId(int mjId) {
		if (huMjIds == null) {
			huMjIds = new ArrayList<>();
		}
		huMjIds.add(mjId);
	}

	public List<Integer> getHuMjIds(){
		return huMjIds;
	}


	public List<TdhMj> getChi() {
		return chi;
	}
	
	
	public long getLastPassTime() {
		return lastPassTime;
	}

	public void setLastPassTime(long lastPassTime) {
		this.lastPassTime = lastPassTime;
	}

	//TODO:优化
	public boolean canChi(TdhMj mj) {
		
		HashMap<Integer,Integer> colors = new HashMap<>();
		if(peng.size()>0) {
			for(TdhMj m:peng) {
				
				Integer value = colors.get(m.getColourVal());
				if(value==null) {
					value =0;
				}
				colors.put(m.getColourVal(), value+1);
			}
		}
		
		if(chi.size()>0) {
			for(TdhMj m:chi) {
				Integer value = colors.get(m.getColourVal());
				if(value==null) {
					value =0;
				}
				colors.put(m.getColourVal(), value+1);
			}
		}
		
		if(colors.size()>1){
			return false;
		}
		
		List<TdhMj> gang = getGang();
		if(gang.size()>0) {
			for(TdhMj m:gang) {
				Integer value = colors.get(m.getColourVal());
				if(value==null) {
					value =0;
				}
				colors.put(m.getColourVal(), value+1);
			}
		}
		
		List<TdhMj> buzhang = getBuzhang();
		if(buzhang.size()>0) {
			for(TdhMj m:buzhang) {
				Integer value = colors.get(m.getColourVal());
				if(value==null) {
					value =0;
				}
				colors.put(m.getColourVal(), value+1);
			}
		}
		
		
		if(colors.size()>1){
			return false;
		}
		if(colors.size()==1 && !colors.keySet().contains(mj.getColourVal())) {
			return false;
		}
			
			
		for(TdhMj m:handPais) {
			Integer value = colors.get(m.getColourVal());
			if(value==null) {
				value =0;
			}
			colors.put(m.getColourVal(), value+1);
		}
		
		for(Map.Entry<Integer,Integer> entry: colors.entrySet()) {
			int value = entry.getValue();
			if(value>=7) {
				int num=0;
				if(getGang().size()>0 ||getBuzhang().size()>0) {
					num = getGang().size()/4 + getBuzhang().size()/4;
				}
				value -=num;
				if(value >=7&& entry.getKey()==mj.getColourVal()) {
					return true;
				}
			}
		}
		
		return false;
		
	}
	
	private boolean canPengGang(TdhMjTable table,TdhMj majiang){
		if(majiang==null){
			return false;
		}
		if(table.getQingyiseChi() == 1 && chi.size()>0 &&chi.get(0).getColourVal()!=majiang.getColourVal()) {
			return false;
		}
		return true;
	}
	
	public List<TdhMj> getBuzhang() {
		List<TdhMj> bzCopy = new ArrayList<>(buzhang);
		bzCopy.addAll(buzhangAn);
		return bzCopy;
	}
	public void removeGangMj(TdhMj mj){
		buzhang.remove(mj);
		mGang.remove(mj);
		for (TdhMjCardDisType type : cardTypes) {
			if(type.getCardIds()!=null) {
				int size = type.getCardIds().size();
				for(int i=0;i<size;i++) {
					Integer id = type.getCardIds().get(i);
					if(id ==mj.getId()) {
						type.getCardIds().remove(i);
					}
				}
			}
		}
	}
	
    /**
     * 记录得分详情
     *
     * @param index 0胡牌分，1鸟分，2杠分，3飘分
     * @param point
     */
    public void changePointArr(int index, int point) {
        if (pointArr.length > index) {
            pointArr[index] += point;
        }
    }

    public int[] getPointArr() {
        return pointArr;
    }
	
}
