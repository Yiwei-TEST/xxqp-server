package com.sy599.game.qipai.ahmj.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.protobuf.GeneratedMessage;
import com.sy599.game.base.AhGame;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.FirstmythConstants;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.db.bean.group.GroupUser;
import com.sy599.game.db.dao.group.GroupDao;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.PlayMajiangRes;
import com.sy599.game.msg.serverPacket.TableMjResMsg.ClosingMjPlayerInfoRes;
import com.sy599.game.msg.serverPacket.TablePhzResMsg.PhzHuCards;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes;
import com.sy599.game.qipai.ahmj.command.AHMajiangCommandProcessor;
import com.sy599.game.qipai.ahmj.constant.Ahmj;
import com.sy599.game.qipai.ahmj.constant.AhmjConstants;
import com.sy599.game.qipai.ahmj.rule.MajiangIndex;
import com.sy599.game.qipai.ahmj.rule.MajiangIndexArr;
import com.sy599.game.qipai.ahmj.tool.AhMajiangTool;
import com.sy599.game.qipai.ahmj.tool.AhmjHelper;
import com.sy599.game.qipai.ahmj.tool.AhmjTool;
import com.sy599.game.qipai.ahmj.tool.MajiangHelper;
import com.sy599.game.qipai.ahmj.tool.MajiangResTool;
import com.sy599.game.qipai.ahmj.tool.QipaiTool;
import com.sy599.game.util.DataMapUtil;
import com.sy599.game.util.JacksonUtil;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.util.StringUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

public class AhmjPlayer extends Player{
	// 座位id
	private int seat;
	// 状态
	private SharedConstants.player_state state;// 1进入 2已准备 3正在玩 4已结束
	private int isEntryTable;
	private List<Ahmj> handPais;
	private List<Ahmj> outPais;
	private List<Ahmj> peng;
	private List<Ahmj> aGang;
	private List<Ahmj> mGang;
	private List<Ahmj> chi;
	private List<Ahmj> buzhang;
	private int winCount;
	private int lostCount;
	private int lostPoint;
	private int gangPoint;// 要胡牌杠才算分
	private int point;
	/** 0自摸1接炮2放炮 5中鸟 **/
	private int[] actionTotalArr = new int[6];
	private List<Integer> xiaohu;
	private List<Integer> huXiaohu;
	private List<Integer> dahu;
	private List<Integer> passMajiangVal;
	private List<Integer> passGangValList;
	private boolean isAlreadyDisCard;
	private volatile boolean autoPlay = false;//托管
	private volatile boolean autoPlaySelf = false;//托管
	private volatile long lastCheckTime = 0;//最后检查时间
	private volatile long autoPlayTime = 0;//自动操作时间
	private volatile boolean checkAutoPlay = false; //是否是牌桌上的焦点
	private volatile long sendAutoTime = 0;//发送倒计时间
	//胡牌类型
	private List<Integer> huType=new ArrayList<>();
	/**
	 * 出牌对应操作信息
	 */
	private List<AhmjCardDisType> cardTypes=new ArrayList<>();

	/**
	 * [类型]=分值 0中鸟数，1庄闲分
	 */
	private int[] pointArr = new int[2];


	public AhmjPlayer() {
		handPais = new ArrayList<Ahmj>();
		outPais = new ArrayList<Ahmj>();
		peng = new ArrayList<>();
		aGang = new ArrayList<>();
		mGang = new ArrayList<>();
		chi = new ArrayList<>();
		buzhang = new ArrayList<>();
		passGangValList = new ArrayList<>();
		xiaohu = new ArrayList<>();
		huXiaohu = new ArrayList<>();
		dahu = new ArrayList<>();
		passMajiangVal = new ArrayList<>();
		autoPlaySelf = false;
		autoPlay = false;
		autoPlayTime = 0;
		checkAutoPlay = false;
		lastCheckTime = System.currentTimeMillis();
	}

	public int getSeat() {
		return seat;
	}

	public void setSeat(int seat) {
		this.seat = seat;
	}

	public void initPais(List<Ahmj> hand, List<Ahmj> out) {
		if (hand != null) {
			this.handPais = hand;

		}
		if (out != null) {
			this.outPais = out;

		}
	}

	public void dealHandPais(List<Ahmj> pais) {
		this.handPais = pais;
		getPlayingTable().changeCards(seat);
	}

	public boolean isGangshangHua() {
		return dahu.contains(3);
	}

	public boolean isGangshangPao() {
		return dahu.contains(5);
	}

	public List<Ahmj> getHandMajiang() {
		return handPais;
	}

	// 0缺一色 1板板胡 2大四喜 3六六顺
	public List<Ahmj> showXiaoHuMajiangs(int xiaohu) {
		if (xiaohu == 0) {
			return handPais;

		} else if (xiaohu == 1) {
			return handPais;

		} else if (xiaohu == 2) {
			MajiangIndexArr card_index = new MajiangIndexArr();
			QipaiTool.getMax(card_index, handPais);

			return card_index.getMajiangIndex(3).getMajiangs();

		} else if (xiaohu == 3) {
			MajiangIndexArr card_index = new MajiangIndexArr();
			QipaiTool.getMax(card_index, handPais);
			MajiangIndex index = card_index.getMajiangIndex(2);
			return index.getMajiangs();

		} else {
			return handPais;
		}

	}

	public List<Integer> getHandPais() {
		return MajiangHelper.toMajiangIds(handPais);
	}

	public List<Integer> getOutPais() {
		return MajiangHelper.toMajiangIds(outPais);
	}

	public List<Ahmj> getOutMajing() {
		return outPais;
	}

	public void moMajiang(Ahmj majiang) {
		setPassMajiangVal(0);
		handPais.add(majiang);
		getPlayingTable().changeCards(seat);
	}

	public void addOutPais(List<Ahmj> cards, int action) {
		handPais.removeAll(cards);
		if (action == 0) {
			outPais.addAll(cards);
		} else {
			if (action == MjDisAction.action_buzhang) {
				buzhang.addAll(cards);

			} else if (action == MjDisAction.action_chi) {
				chi.addAll(cards);

			} else if (action == MjDisAction.action_peng) {
				peng.addAll(cards);
				// changeAction(0, 1);
			} else if (action == MjDisAction.action_minggang) {
				myExtend.setMjFengshen(FirstmythConstants.firstmyth_index12, 1);
				mGang.addAll(cards);
				if (cards.size() != 1) {
				} else {
					Ahmj pengMajiang = cards.get(0);
					Iterator<Ahmj> iterator = peng.iterator();
					while (iterator.hasNext()) {
						Ahmj majiang = iterator.next();
						if (majiang.getVal() == pengMajiang.getVal()) {
							mGang.add(majiang);
							iterator.remove();
						}
					}
				}

			} else if (action == MjDisAction.action_angang) {
				myExtend.setMjFengshen(FirstmythConstants.firstmyth_index12, 1);
				aGang.addAll(cards);
			}
			getPlayingTable().changeExtend();
			addCardType(action, cards, seat, 0);
		}

		getPlayingTable().changeCards(seat);
	}

	public void addCardType(int action, List<Ahmj> disCardList, int disSeat, int disStatus) {
		if (action != 0) {
			if (action == MjDisAction.action_minggang && disCardList.size() == 1) {
				Ahmj majiang = disCardList.get(0);
				for (AhmjCardDisType disType : cardTypes) {
					if (disType.getAction() == MjDisAction.action_peng) {
						if (disType.isHasCardVal(majiang)) {
							disType.setAction(MjDisAction.action_minggang);
							disType.addCardId(majiang.getId());
							disType.setDisSeat(seat);
							break;
						}
					}
				}
			} else {
				AhmjCardDisType type = new AhmjCardDisType();
				type.setAction(action);
				type.setCardIds(AhmjHelper.toMajiangIds(disCardList));
				type.setDisSeat(disSeat);
				type.setDisStatus(disStatus);
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

	public void removeOutPais(List<Ahmj> cards, int action) {
		boolean remove = outPais.removeAll(cards);
		if (remove) {
			getPlayingTable().changeCards(seat);

		}
	}

	public String toExtendStr() {
		StringBuffer sb = new StringBuffer();
		sb.append(MajiangHelper.implodeMajiang(peng, ",")).append("|");
		sb.append(MajiangHelper.implodeMajiang(aGang, ",")).append("|");
		sb.append(MajiangHelper.implodeMajiang(mGang, ",")).append("|");
		sb.append(StringUtil.implode(actionTotalArr)).append("|");
		sb.append(StringUtil.implode(passGangValList, ",")).append("|");
		sb.append(StringUtil.implode(xiaohu, ",")).append("|");
		sb.append(MajiangHelper.implodeMajiang(chi, ",")).append("|");
		sb.append(MajiangHelper.implodeMajiang(buzhang, ",")).append("|");
		sb.append(StringUtil.implode(dahu, ",")).append("|");
		sb.append(StringUtil.implode(huXiaohu, ",")).append("|");
		return sb.toString();

	}

	public void initExtend(String info) {
		if (StringUtils.isBlank(info)) {
			return;
		}
		int i = 0;
		String[] values = info.split("\\|");
		String val1 = StringUtil.getValue(values, i++);
		peng = MajiangHelper.explodeMajiang(val1, ",");
		String val2 = StringUtil.getValue(values, i++);
		aGang = MajiangHelper.explodeMajiang(val2, ",");
		String val3 = StringUtil.getValue(values, i++);
		mGang = MajiangHelper.explodeMajiang(val3, ",");
		String val5 = StringUtil.getValue(values, i++);
		if (!StringUtils.isBlank(val5)) {
			actionTotalArr = StringUtil.explodeToIntArray(val5);
		}
		String val6 = StringUtil.getValue(values, i++);
		if (!StringUtils.isBlank(val6)) {
			passGangValList = StringUtil.explodeToIntList(val6);
		}
		String val7 = StringUtil.getValue(values, i++);
		if (!StringUtils.isBlank(val7)) {
			xiaohu = StringUtil.explodeToIntList(val7);
		}
		String val8 = StringUtil.getValue(values, i++);
		chi = MajiangHelper.explodeMajiang(val8, ",");
		String val9 = StringUtil.getValue(values, i++);
		buzhang = MajiangHelper.explodeMajiang(val9, ",");
		String val10 = StringUtil.getValue(values, i++);
		if (!StringUtils.isBlank(val10)) {
			dahu = StringUtil.explodeToIntList(val10);
		}
		String val11 = StringUtil.getValue(values, i++);
		if (!StringUtils.isBlank(val11)) {
			huXiaohu = StringUtil.explodeToIntList(val11);
		}
		pointArr=new int[2];
	}

	public String toInfoStr() {
		StringBuffer sb = new StringBuffer();
		sb.append(getUserId()).append(",");//1
		sb.append(seat).append(",");//2
		int stateVal = 0;
		if (state != null) {
			stateVal = state.getId();
		}
		sb.append(stateVal).append(",");//3
		sb.append(isEntryTable).append(",");//4
		sb.append(winCount).append(",");//5
		sb.append(lostCount).append(",");//6
		sb.append(point).append(",");//7
		sb.append(getTotalPoint()).append(",");//8
		sb.append(lostPoint).append(",");//9
		sb.append(StringUtil.implode(passMajiangVal, "_")).append(",");//10
		sb.append(gangPoint).append(",");//11
		sb.append(autoPlay?1:0).append(",");//12
		sb.append(lastCheckTime).append(",");//13
		sb.append(StringUtil.implode(pointArr, "_")).append(",");//14
		return sb.toString();
	}

	@Override
	public void initPlayInfo(String data) {
		if (!StringUtils.isBlank(data)) {
			int i = 0;
			String[] values = data.split(",");
			long duserId = StringUtil.getLongValue(values, i++);//1
			if (duserId != getUserId()) {
				return;
			}
			this.seat = StringUtil.getIntValue(values, i++);//2
			int stateVal = StringUtil.getIntValue(values, i++);//3
			this.state = SharedConstants.getPlayerState(stateVal);
			this.isEntryTable = StringUtil.getIntValue(values, i++);//4
			this.winCount = StringUtil.getIntValue(values, i++);//5
			this.lostCount = StringUtil.getIntValue(values, i++);//6
			this.point = StringUtil.getIntValue(values, i++);//7
			setTotalPoint(StringUtil.getIntValue(values, i++));//8
			this.lostPoint = StringUtil.getIntValue(values, i++);//9

			String passMajiangValStr = StringUtil.getValue(values, i++);//10
			if (!StringUtils.isBlank(passMajiangValStr)) {
				this.passMajiangVal = StringUtil.explodeToIntList(passMajiangValStr, "_");
			}
			this.gangPoint = StringUtil.getIntValue(values, i++);//11
			this.autoPlay = StringUtil.getIntValue(values, i++) == 1;//12
			this.lastCheckTime = StringUtil.getLongValue(values, i++);//13
			String pointArr = StringUtil.getValue(values, i++);//14
			if (!StringUtils.isBlank(pointArr)) {
				this.pointArr=StringUtil.explodeToIntArray(pointArr,"_");
			}
		}
	}

	public SharedConstants.player_state getState() {
		return state;
	}

	public void changeState(SharedConstants.player_state state) {
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
		return buildPlayInTableInfo(false);
	}


	public void setAutoPlay(boolean autoPlay, boolean isSelf) {
		AhmjTable table = getPlayingTable(AhmjTable.class);
		if (this.autoPlay != autoPlay || autoPlaySelf != isSelf) {
			ComMsg.ComRes.Builder res = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_tuoguan, seat, autoPlay ? 1 : 0, 0, isSelf ? 1 : 0);
			GeneratedMessage msg = res.build();
			if (table != null) {
				table.broadMsgToAll(msg);
			}
            table.addPlayLog(table.getDisCardRound() + "_" +getSeat() + "_" + AhmjConstants.action_tuoguan + "_" +(autoPlay?1:0));

            LogUtil.msg("setAutoPlay|" + (table == null ? -1 : table.getIsAutoPlay()) + "|" + getSeat() + "|" + getUserId() + "|" + (autoPlay ? 1 : 0) + "|" + (isSelf ? 1 : 0));

		}
		this.autoPlay = autoPlay;
		this.autoPlaySelf = autoPlay && isSelf;
		this.checkAutoPlay = autoPlay;
		setLastCheckTime(System.currentTimeMillis());
		if (table != null) {
			table.changeExtend();
		}
	}

	public void setLastCheckTime(long lastCheckTime) {
		this.lastCheckTime = lastCheckTime;
		if (getPlayingTable() != null) {
			getPlayingTable().changeExtend();
		}
	}


	/**
	 * 吃碰杠过的牌
	 * 
	 * @return
	 */
	public List<Integer> getMoldIds() {
		List<Integer> list = new ArrayList<>();
		if (!peng.isEmpty()) {
			list.addAll(MajiangHelper.toMajiangIds(peng));
		}
		if (!mGang.isEmpty()) {
			list.addAll(MajiangHelper.toMajiangIds(mGang));
		}
		if (!aGang.isEmpty()) {
			list.addAll(MajiangHelper.toMajiangIds(aGang));
		}
		if (!buzhang.isEmpty()) {
			list.addAll(MajiangHelper.toMajiangIds(buzhang));
		}
		if (!chi.isEmpty()) {
			list.addAll(MajiangHelper.toMajiangIds(chi));
		}

		return list;
	}

	public List<PhzHuCards> buildDisCards(long lookUid) {
		return buildDisCards(lookUid, true);
	}

	public List<PhzHuCards> buildDisCards(long lookUid, boolean hide) {
		List<PhzHuCards> list = new ArrayList<>();
		for (AhmjCardDisType type : cardTypes) {
			if (hide && lookUid != this.userId && type.getAction() == MjDisAction.action_angang) {
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
		res.setPoint(getTotalPoint());
		res.addAllOutedIds(getOutPais());
		res.addAllMoldIds(getMoldIds());
		res.addAllAngangIds(MajiangHelper.toMajiangIds(aGang));
		res.addAllMoldCards(buildDisCards(userId));
		List<Ahmj> gangList = getGang();
		// 是否杠过
		res.addExt(gangList.isEmpty() ? 0 : 1);
		res.addExt((isAlreadyMoMajiang()) ? 1 : 0);
		res.addExt(handPais != null ? handPais.size() : 0);
		res.addExt(autoPlay ? 1 : 0);
		AhmjTable table = getPlayingTable(AhmjTable.class);
		// 现在是否自己摸的牌
		if (table != null)
			res.addExt(table.getMoMajiangSeat() == seat ? 1 : 0);

		if (!StringUtils.isBlank(getHeadimgurl())) {
			res.setIcon(getHeadimgurl());

		} else {
			res.setIcon("");

		}

		if (state == SharedConstants.player_state.ready || state == SharedConstants.player_state.play) {
			// 玩家装备已经准备和正在玩的状态时通知前台已准备
			res.setStatus(SharedConstants.state_player_ready);
		} else {
			res.setStatus(0);
		}
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
	 * 检查发送小胡
	 */
	public void checkSendActionRes() {
		if (canXiaoHu()) {
			PlayMajiangRes.Builder builder = MajiangResTool.buildActionRes(this, xiaohu);
			writeSocket(builder.build());
		}

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
		changeTableInfo();
	}

	public void changeLostPoint(int point) {
		this.lostPoint += point;
		changeTableInfo();
	}

	public void changeGangPoint(int point) {
		this.gangPoint += point;
		changeTableInfo();
	}

	public int getGangPoint() {
		return gangPoint;
	}

	public int getLostPoint() {
		return lostPoint;
	}

	public void changePoint(int point) {
		this.point += point;
		myExtend.changePoint(getPlayingTable().getPlayType(), point);
		myExtend.setMjFengshen(FirstmythConstants.firstmyth_index7, point);
		changeTotalPoint(point);
		if (point > getMaxPoint()) {
			setMaxPoint(point);
		}
		changeTableInfo();
	}

	public void clearTableInfo() {
		BaseTable table = getPlayingTable();
		boolean isCompetition = false;
		if (table != null && table.isCompetition()) {
			isCompetition = true;
			endCompetition();
		}
		setAlreadyDisCard(false);
		setIsEntryTable(0);
		changeIsLeave(0);
		getHandMajiang().clear();
		getOutMajing().clear();
		changeState(null);
		actionTotalArr = new int[6];
		xiaohu = new ArrayList<>();
		dahu = new ArrayList<>();
		huXiaohu = new ArrayList<>();
		peng.clear();
		aGang.clear();
		mGang.clear();
		chi.clear();
		buzhang.clear();
		setPassMajiangVal(0);
		clearPassGangVal();
		setWinCount(0);
		setLostCount(0);
		setPoint(0);
		setGangPoint(0);
		setLostPoint(0);
		setTotalPoint(0);
		setSeat(0);
		if (!isCompetition) {
			setPlayingTableId(0);
		}
		saveBaseInfo();
		huType.clear();
		autoPlaySelf = false;
		autoPlay = false;
		lastCheckTime = System.currentTimeMillis();
		checkAutoPlay = false;
		cardTypes.clear();
		pointArr=new int[2];
	}

	/**
	 * 单局详情
	 * 
	 * @return
	 */
	public ClosingMjPlayerInfoRes.Builder bulidOneClosingPlayerInfoRes() {
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
		res.setSex(sex);
		res.addAllHandPais(getHandPais());
		List<PhzHuCards> list = new ArrayList<>();
		for (AhmjCardDisType type : cardTypes) {
			list.add(type.buildMsg().build());
		}
		res.addAllMoldPais(list);
		res.addAllXiaohus(getDahu());
		res.addAllDahus(getDahu());
		res.addAllPointArr(Arrays.asList(ArrayUtils.toObject(pointArr)));
		return res;
	}

	public int[] buildActionArr() {
		// 点炮 接炮 自摸 中鸟
		int[] result = new int[3];
		return result;
	}

	public void changeTableInfo() {
		BaseTable table = getPlayingTable();
		if (table != null)
			table.changePlayers();
	}

	@Override
	public void initNext() {
		setAlreadyDisCard(false);
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
		huXiaohu = new ArrayList<>();
		chi.clear();
		buzhang.clear();
		xiaohu = new ArrayList<>();
		dahu = new ArrayList<>();
		getPlayingTable().changeExtend();
		getPlayingTable().changeCards(seat);
		changeState(SharedConstants.player_state.entry);
		huType.clear();
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
		cardTypes.clear();
		pointArr=new int[2];
	}

	@Override
	public void initPais(String handPai, String outPai) {
		if (!StringUtils.isBlank(handPai)) {
			List<Integer> list = StringUtil.explodeToIntList(handPai);
			this.handPais = MajiangHelper.toMajiang(list);
		}
		if (!StringUtils.isBlank(outPai)) {
			String[] values = outPai.split(";");
			int i = -1;
			for (String value : values) {
				i++;
				if (i == 0) {
					List<Integer> list = StringUtil.explodeToIntList(value);
					this.outPais = AhmjHelper.toMajiang(list);
				} else {
					AhmjCardDisType type = new AhmjCardDisType();
					type.init(value);
					cardTypes.add(type);
					List<Ahmj> majiangs = AhmjHelper.toMajiang(type.getCardIds());
					if (type.getAction() == MjDisAction.action_angang) {
						aGang.addAll(majiangs);
					} else if (type.getAction() == MjDisAction.action_minggang) {
						mGang.addAll(majiangs);
					} else if (type.getAction() == MjDisAction.action_peng) {
						peng.addAll(majiangs);
					}
				}
			}
		}
	}

	public String buildOutPaiStr() {
		StringBuffer sb = new StringBuffer();
		List<Integer> outPais = getOutPais();
		sb.append(StringUtil.implode(outPais)).append(";");
		for (AhmjCardDisType huxi : cardTypes) {
			sb.append(huxi.toStr()).append(";");
		}
		return sb.toString();
	}
	/**
	 * 可以吃这张麻将的牌
	 * 
	 * @param disMajiang
	 * @return
	 */
	public List<Ahmj> getCanChiMajiangs(Ahmj disMajiang) {
		AhmjTable table = getPlayingTable(AhmjTable.class);
		List<Ahmj> chi = AhmjTool.checkChi(handPais, disMajiang, table.getWangValList());
		if (chi.isEmpty()) {
			return chi;
		}
		boolean check = table.checkWang(chi);
		return check ? new ArrayList<Ahmj>() : chi;
	}

	/**
	 * 除了王之外还有多少张牌
	 * 
	 * @param wangValList
	 * @return
	 */
	public int getExceptWangMajiangCount(List<Integer> wangValList) {
		int count = 0;
		for (Ahmj majiang : handPais) {
			if (!wangValList.contains(majiang.getVal())) {
				count++;
			}
		}
		return count;
	}

	public long getLastCheckTime() {
		return lastCheckTime;
	}

	public long getAutoPlayTime() {
		return autoPlayTime;
	}

	public void setAutoPlayTime(long autoPlayTime) {
		this.autoPlayTime = autoPlayTime;
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

	public void changePointArr(int index, int point) {
		if (pointArr.length > index) {
			pointArr[index] += point;
		}
	}

	public int[] getPointArr() {
		return pointArr;
	}

	public long getSendAutoTime() {
		return sendAutoTime;
	}

	public void setSendAutoTime(long sendAutoTime) {
		this.sendAutoTime = sendAutoTime;
	}

	/**
	 * 检查托管
	 *
	 * @param autoType  0打牌，1准备，2胡牌
	 * @param forceSend 立即推送倒计时，用于断线重连
	 * @return
	 */
	public boolean checkAutoPlay(int autoType, boolean forceSend) {
		AhmjTable table = getPlayingTable(AhmjTable.class);
		long now = System.currentTimeMillis();
		boolean auto = isAutoPlay();
		if (!auto && table.getIsAutoPlay() >= 1) {
			//检查玩家是否进入系统托管状态
			if (!checkAutoPlay) {
				if (isAlreadyMoMajiang() || table.getActionSeatMap().containsKey(seat)) {
					setCheckAutoPlay(true);
				} else {
					setCheckAutoPlay(false);
					return false;
				}
			}

			int timeOut = (int) (now - getLastCheckTime()) / 1000;
			//ZzMjConstants.AUTO_TIMEOUT + ZzMjConstants.AUTO_CHECK_TIMEOUT
			if (timeOut >=table.getIsAutoPlay()) {
				//进入托管状态
				auto = true;
				setAutoPlay(true, false);
			} else if (timeOut >= AhmjConstants.AUTO_CHECK_TIMEOUT) {
				if (sendAutoTime == 0) {
					sendAutoTime = now;
					setLastCheckTime(now - AhmjConstants.AUTO_CHECK_TIMEOUT * 1000);
					timeOut = (int) (now - getLastCheckTime()) / 1000;
				}
				int timeSecond = table.getIsAutoPlay() - timeOut;
				if ((timeOut % 5 == 0 && isOnline()) || forceSend) {
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
			} else {
				int timeOut = (int) (now - getAutoPlayTime()) / 1000;
				if (autoType == 1) {
					if (timeOut >= table.getIsAutoPlay()) {
						setAutoPlayTime(0);
						return true;
					}
				} else if (autoType == 2) {
					if (timeOut >= AhmjConstants.AUTO_HU_TIME) {
						setAutoPlayTime(0);
						return true;
					}
				} else {
					if (timeOut >= AhmjConstants.AUTO_PLAY_TIME) {
						setAutoPlayTime(0);
						return true;
					}
				}
			}
		}
		return false;
	}



	public List<Integer> checkDisMajiang(Ahmj majiang) {
		return checkDisMajiang(majiang, false, false);
	}

	/**
	 * 别人出牌可做的操作
	 * 
	 * @param majiang
	 * @param isCheckGang
	 *            是否检查杠上花和杠上炮(是的话不能吃碰杠,只能胡)
	 * @param isQGang
	 *            是否抢杠胡和杠上炮
	 * @return 0胡 1碰 2明刚 3暗杠(暗杠后来不需要了 暗杠也用3标记)4吃5补张(安化麻将不能补张)
	 */
	public List<Integer> checkDisMajiang(Ahmj majiang, boolean isCheckGang, boolean isQGang) {
		return checkDisMajiang(majiang, isCheckGang, isQGang, false);
	}

	/**
	 * 别人出牌可做的操作
	 * 
	 * @param majiang
	 * @param isCheckGang
	 *            是否检查杠上花和杠上炮(是的话不能吃碰杠,只能胡)
	 * @param isQGang
	 *            是否抢杠胡
	 * @return 0胡 1碰 2明刚 3暗杠(暗杠后来不需要了 暗杠也用3标记)4吃5补张(安化麻将不能补张)
	 */
	public List<Integer> checkDisMajiang(Ahmj majiang, boolean isCheckGang, boolean isQGang, boolean gangshanghua) {
		List<Integer> list = new ArrayList<>();
		int[] arr = new int[6];
		AhmjTable table = getPlayingTable(AhmjTable.class);
		// 没有出现漏炮的情况
		// if (passMajiangVal != majiang.getVal()) {
		List<Ahmj> copy = new ArrayList<>(handPais);
		if ((!table.moHu() || table.getDisCardSeat() == seat) && (isQGang || !passMajiangVal.contains(majiang.getVal()))) {
			copy.add(majiang);
			AhmjHu huBean = AhMajiangTool.isHuAHMajiang(copy, getGang(), peng, chi, buzhang, Ahmj.getMajang(table.getFirstId()), table.isFourWang(), false, false, isQGang, gangshanghua);
			if (huBean.isHu()) {
				arr[0] = 1;
			}

			if (!huBean.isHu() && table.isYzWang() && huBean.getWangDahuNum() == 0) {
				// 三王以下
				huBean = AhMajiangTool.isHuAHMajiang(copy, getGang(), peng, chi, buzhang, null, table.isFourWang(), false, false, isQGang, gangshanghua);
				if (huBean.isHu()) {
					arr[0] = 1;
				}
			}
		}

		if (!isCheckGang) {
			List<Ahmj> gangList = getGang();
			if (table.getDisCardSeat() != seat) {
				// 现在出牌的人不是自己
				int count = MajiangHelper.getMajiangCount(handPais, majiang.getVal());
				// 如果已经杠过了牌 那只能胡牌(不能换牌)
				if (gangList.isEmpty()) {
					// 剩下的牌有4张才能杠
					if (count == 3) {
						if (table.getLeftMajiangCount() >= 4) {
							if (checkGang(copy, Ahmj.getMajang(table.getFirstId()), table.isFourWang(), null, majiang.getVal())) {
								arr[2] = 1;// 可以杠

							}
						}
						// 不能补张
						// arr[5] = 1;// 可以补张
					}

					if (count >= 2) {
						// 必须要3张及以上不是王的牌才能碰，不然没牌打咯
						if (getExceptWangMajiangCount(table.getWangValList()) >= 3) {
							arr[1] = 1;// 可以碰
						}

					}

					if (table.calcNextSeat(table.getDisCardSeat()) == seat) {
						List<Ahmj> chi = AhmjTool.checkChi(handPais, majiang, table.getWangValList());
						if (!chi.isEmpty()) {
							// if (!table.checkWang(chi)) {
							// 必须要3张及以上不是王的牌才能吃，不然没牌打咯
							if (getExceptWangMajiangCount(table.getWangValList()) >= 3) {
								arr[4] = 1; // 可以吃
							}
							// }

						}
					}
				}

			} else {
				// 出牌的人是自己 (杠后补张)
				if (!gangList.isEmpty()) {
					Map<Integer, Integer> pengMap = MajiangHelper.toMajiangValMap(peng);
					if (pengMap.containsKey(majiang.getVal())) {
						arr[2] = 1;// 可以杠
						// 不能补张
						// arr[5] = 1;// 可以补张
					}
				}

			}

		}

		for (int val : arr) {
			list.add(val);
		}
		if (list.contains(1)) {
			return list;
		} else {
			return Collections.EMPTY_LIST;
		}
	}

	public Ahmj getLastMoMajiang() {
		if (handPais.isEmpty()) {
			return null;

		} else {
			return handPais.get(handPais.size() - 1);

		}
	}

	public int getXiaoHuCount(int xiaoIndex) {
		int count = 0;
		for (int val : huXiaohu) {
			if (val == xiaoIndex) {
				count++;
			}
		}
		return count;
	}

	/**
	 * 检查起牌小胡
	 * 
	 * @return
	 */
	public List<Integer> initXiaoHu(List<Integer> xiaohu) {
		List<Integer> temp = new ArrayList<>();
		for (int i = 0; i < xiaohu.size(); i++) {
			if (xiaohu.get(i) != 0) {
				temp.add(1);
			} else {
				temp.add(0);
			}
		}
		this.xiaohu = temp;
		this.huXiaohu = DataMapUtil.indexToValCountList(xiaohu);
		getPlayingTable().changeExtend();
		return xiaohu;

	}

	public void checkDahu() {

	}

	public void huXiaohu(int index) {
		xiaohu.set(index, 2);
		getPlayingTable().changeExtend();
	}

	public boolean canXiaoHu() {
		return xiaohu.contains(1);
	}

	/**
	 * 0缺一色 1板板胡 2大四喜 3六六顺
	 * 
	 * @return
	 */
	public List<Integer> getXiaohu() {
		return xiaohu;
	}

	public int getDahuCount() {
		return dahu.size();
	}

	public List<Integer> getDahu() {
		return dahu;
	}

	public void setDahu(List<Integer> dahu) {
		this.dahu = dahu;
		getPlayingTable().changeExtend();
	}

	public AhmjHu checkHu(Ahmj disMajiang, Ahmj wangMajiang, boolean isFourWang, boolean isbegin, boolean qGangHu) {
		return checkHu(disMajiang, wangMajiang, isFourWang, isbegin, qGangHu, false);
	}

	/**
	 * 胡牌
	 * 
	 * @param disMajiang
	 * @param isbegin
	 * @return
	 */
	public AhmjHu checkHu(Ahmj disMajiang, Ahmj wangMajiang, boolean isFourWang, boolean isbegin, boolean qGangHu, boolean gangshanghua) {
		List<Ahmj> copy = new ArrayList<>(handPais);
		if (disMajiang != null) {
			copy.add(disMajiang);
		}
		AhmjHu hu = AhMajiangTool.isHuAHMajiang(copy, getGang(), peng, chi, buzhang, wangMajiang, isFourWang, isbegin, disMajiang == null, qGangHu, gangshanghua);
		return hu;
	}

	/**
	 * 自己出牌可做的操作
	 * 
	 * @param majiang
	 *            null 时自己碰或者吃,不能胡牌
	 * @param isBegin
	 *            起牌是第一次出牌
	 * @return 0胡 1碰 2明刚 3暗杠
	 */
	public List<Integer> checkMo(Ahmj majiang, boolean isBegin) {
		List<Integer> list = new ArrayList<>();
		int[] arr = new int[6];
		AhmjTable table = getPlayingTable(AhmjTable.class);
		if (majiang != null || isBegin) {
			// 碰的时候判断不能胡牌
			AhmjHu hu = AhMajiangTool.isHuAHMajiang(handPais, getGang(), peng, chi, buzhang, Ahmj.getMajang(table.getFirstId()), table.isFourWang(), isBegin, true, false);
			if (hu.isHu()) {
				arr[0] = 1;
			}
		}


		if (isAlreadyMoMajiang()) {
			List<Ahmj> gangList = getGang();
			Map<Integer, Integer> pengMap = MajiangHelper.toMajiangValMap(peng);
			if (table.getNowDisCardSeat()==getSeat()) {
				for (Ahmj handMajiang : handPais) {
					if (pengMap.containsKey(handMajiang.getVal())) {
						// 有碰过
						if (table.getLeftMajiangCount() >= 4) {
							if (checkGang(handPais, Ahmj.getMajang(table.getFirstId()), table.isFourWang(), null, handMajiang.getVal())) {
								arr[2] = 1;// 可以杠
								break;
							}
						}
					}
				}
				Map<Integer, Integer> handMap = MajiangHelper.toMajiangValMap(handPais);
				// if (handMap.containsValue(4)) {
				if (table.getLeftMajiangCount() >= 4) {
					if (checkGang(handPais, Ahmj.getMajang(table.getFirstId()), table.isFourWang(), table.getWangValList(), handMap)) {
						arr[2] = 1;// 可以杠
					}
				}

				// 只能杠抓上来的那张
				if (majiang!=null&&pengMap.containsKey(majiang.getVal())) {
					if (table.getLeftMajiangCount() >= 4) {
						if (checkGang(handPais, Ahmj.getMajang(table.getFirstId()), table.isFourWang(), null, majiang.getVal())) {
							arr[2] = 1;// 可以杠
						}
					}
				}
			}
		}


		for (int val : arr) {
			list.add(val);
		}
		if (list.contains(1)) {
			return list;
		} else {
			return Collections.EMPTY_LIST;
		}
	}

	public boolean checkGang(List<Ahmj> handPais, Ahmj wangMajiang, boolean isFourWang, List<Integer> wangValList, Map<Integer, Integer> handMap) {
		for (Entry<Integer, Integer> entry : handMap.entrySet()) {
			if (wangValList != null && wangValList.contains(entry.getKey())) {
				continue;
			}
			if (entry.getValue() == 4) {
				boolean check = checkGang(handPais, wangMajiang, isFourWang, null, entry.getKey());
				if (check) {
					return check;
				}
			}
		}
		return false;

	}

	/**
	 * 操作
	 *
	 * @param index 0点炮1点杠2明杠3暗杠4被碰5被杠6胡7自摸
	 * @param val
	 */
	public void changeActionNum(int index, int val) {
		actionTotalArr[index] += val;
		getPlayingTable().changeExtend();
	}

	/**
	 * 检查是否能杠
	 * 
	 * @param handPais
	 *            手牌
	 * @param wangMajiang
	 *            王
	 * @param disMajiang
	 *            出牌
	 * @param gangPaiVal
	 *            杠出的牌val
	 * @return
	 */
	public boolean checkGang(List<Ahmj> handPais, Ahmj wangMajiang, boolean fourWang, Ahmj disMajiang, int gangPaiVal) {
		// 检查杠了牌之后是否已经听牌不然不能杠
		List<Ahmj> copy = new ArrayList<>(handPais);
		List<Ahmj> dropValList = QipaiTool.dropVal(copy, gangPaiVal);
		List<Ahmj> gangList = getGang();
		if (dropValList != null) {
			gangList.addAll(dropValList);
		}
		// 去掉杠的牌后 补一张万能牌
		if (fourWang) {
			copy.add(Ahmj.mj1000);
		} else {
			copy.add(wangMajiang);

		}
		AhmjHu hu = AhMajiangTool.isHuAHMajiang(copy, gangList, peng, chi, buzhang, wangMajiang, fourWang, false, true, false);
		return hu.isHu();
	}

	public boolean isAutoPlay() {
		return autoPlay;
	}

	public boolean isAutoPlaySelf() {
		return autoPlaySelf;
	}

	public List<Ahmj> getPeng() {
		return peng;
	}
	
	public List<Ahmj> getChi() {
		return chi;
	}
	
	public List<Ahmj> getBuzhang() {
		return buzhang;
	}

	public List<Ahmj> getaGang() {
		return aGang;
	}

	public List<Ahmj> getmGang() {
		return mGang;
	}

	public List<Ahmj> getGang() {
		List<Ahmj> gang = new ArrayList<>();
		gang.addAll(aGang);
		gang.addAll(mGang);
		return gang;
	}

	public List<Integer> getPassMajiangVal() {
		return passMajiangVal;
	}

	/**
	 * 总局详情
	 *
	 * @return
	 */
	public ClosingMjPlayerInfoRes.Builder bulidTotalClosingPlayerInfoRes() {
		ClosingMjPlayerInfoRes.Builder res = ClosingMjPlayerInfoRes.newBuilder();
		res.setUserId(userId + "");
		res.setName(name);
		res.setPoint(point);
		res.addAllActionCount(Arrays.asList(ArrayUtils.toObject(actionTotalArr)));
		res.setTotalPoint(getTotalPoint());
		res.setSeat(seat);
		if (!StringUtils.isBlank(getHeadimgurl())) {
			res.setIcon(getHeadimgurl());
		} else {
			res.setIcon("");
		}
		res.setSex(sex);
		res.addAllHandPais(getHandPais());
		List<PhzHuCards> list = new ArrayList<>();
		for (AhmjCardDisType type : cardTypes) {
			list.add(type.buildMsg().build());
		}
		res.addAllMoldPais(list);
		res.addAllDahus(huType);
		res.addAllPointArr(Arrays.asList(ArrayUtils.toObject(pointArr)));
		return res;
	}


	/**
	 * 漏炮
	 * 
	 * @param passMajiangVal
	 */
	public void setPassMajiangVal(int passMajiangVal) {
		if (passMajiangVal == 0) {
			this.passMajiangVal.clear();
			changeTableInfo();
		} else if (!this.passMajiangVal.contains(passMajiangVal)) {
			this.passMajiangVal.add(passMajiangVal);
			changeTableInfo();
		}

	}

	/**
	 * 可以碰可以杠的牌 选择了碰 再杠不算分
	 * 
	 * @param majiang
	 * @return
	 */
	public boolean isPassGang(Ahmj majiang) {
		return passGangValList.contains(majiang.getVal());
	}

	public List<Integer> getPassGangValList() {
		return passGangValList;
	}

	public void clearPassGangVal() {
		this.passGangValList.clear();
		BaseTable table = getPlayingTable();
		if (table != null) {
			table.changeExtend();
		}
	}

	public List<Ahmj> getAllMjWithoutHand(){
		List<Ahmj> allmj=new ArrayList<>();
		allmj.addAll(peng);
		allmj.addAll(aGang);
		allmj.addAll(mGang);
		allmj.addAll(chi);
		return allmj;
	}

	@Override
	public void endCompetition1() {
		// TODO Auto-generated method stub

	}

	public boolean isAlreadyDisCard() {
		return isAlreadyDisCard;
	}

	public void setAlreadyDisCard(boolean isAlreadyDisCard) {
		this.isAlreadyDisCard = isAlreadyDisCard;
		changeTableInfo();
	}

	public static final List<Integer> wanfaList = Arrays.asList(4);

	public static void loadWanfaPlayers(Class<? extends Player> cls){
		for (Integer integer:wanfaList){
			PlayerManager.wanfaPlayerTypesPut(integer,cls, AHMajiangCommandProcessor.getInstance());
		}
	}
}
