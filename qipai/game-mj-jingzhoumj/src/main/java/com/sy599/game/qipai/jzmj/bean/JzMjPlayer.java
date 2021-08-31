package com.sy599.game.qipai.jzmj.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import com.sy599.game.qipai.jzmj.command.JzMjCommandProcessor;
import com.sy599.game.qipai.jzmj.constant.JzMjAction;
import com.sy599.game.qipai.jzmj.constant.JzMjConstants;
import com.sy599.game.qipai.jzmj.rule.JzMj;
import com.sy599.game.qipai.jzmj.rule.JzMjHelper;
import com.sy599.game.qipai.jzmj.rule.JzMjRule;
import com.sy599.game.qipai.jzmj.tool.JzMjEnumHelper;
import com.sy599.game.qipai.jzmj.tool.JzMjQipaiTool;
import com.sy599.game.qipai.jzmj.tool.JzMjTool;
import com.sy599.game.util.DataMapUtil;
import com.sy599.game.util.GameUtil;
import com.sy599.game.util.JacksonUtil;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.util.StringUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

public class JzMjPlayer extends Player {
	// 座位id
	private int seat;
	// 状态
	private player_state state;// 1进入 2已准备 3正在玩 4已结束
	private int isEntryTable;
	private List<JzMj> handPais;
	private List<JzMj> outPais;
	private List<JzMj> peng;
	private List<JzMj> aGang;
	private List<JzMj> mGang;
	private List<JzMj> chi;
	private List<JzMj> buzhang;
	private List<JzMj> buzhangAn;
	private int winCount;
	private int lostCount;
	private int lostPoint;
	private int gangPoint;// 要胡牌杠才算分
	private int point;
	/** 0点炮1杠2明杠3暗杠4被碰5被杠6胡7自摸 **/
	private int[] actionArr = new int[9];
	/** 0点炮1杠2明杠3暗杠4被碰5被杠6胡7自摸 **/
	private int[] actionTotalArr = new int[9];
	private List<Integer> huXiaohu;
	private List<Integer> dahu;
	private int passMajiangVal;
	private List<Integer> passGangValList;
	private List<JzMjCardDisType> cardTypes;
	/*** 过小胡的记录*/
	private List<Integer> passXiaoHuList;

	/*** 过小胡的记录，出牌后清空*/
	private List<Integer> passXiaoHuList2= new ArrayList<>();

	private long lastPassTime;
	/**闲上庄=1 */
	private int isxsz ;

	/**出牌次数*/
	private int cpnum;

	/**   1小胡自摸  2小胡接炮 3大胡自摸 	4大胡接炮*/
	private int xjsHuType;
	/**明杠分*/
	private int xjsMingGangPoint;
	/**暗杠分*/
	private int xjsAnGangPoint;
	/**直杠*/
	private int xjsZhiGangPoint;

	/**报听：不能换牌了,胡了或放炮都算大胡*/
	private int baotingStatus = 0;

	/**小胡的牌*/
	private List<Integer> huXiaohuCards= new ArrayList<>();


	/**小胡六六顺的牌*/
	private HashMap<Integer,List<Integer>> huxiaoHuCardVal= new HashMap<Integer,List<Integer>>();
	//private List<Integer> passXiaoHuCards= new ArrayList<>();




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

	/** 小胡所有的牌**/
	private List<JzMj> xiaoHuMjList = new ArrayList<>();

	private List<Integer> gangCGangMjs = new ArrayList<>();

	public JzMjPlayer() {
		handPais = new ArrayList<JzMj>();
		outPais = new ArrayList<JzMj>();
		peng = new ArrayList<>();
		aGang = new ArrayList<>();
		mGang = new ArrayList<>();
		chi = new ArrayList<>();
		buzhang = new ArrayList<>();
		buzhangAn = new ArrayList<>();
		passGangValList = new ArrayList<>();
		huXiaohu = new ArrayList<>();
		dahu = new ArrayList<>();
		cardTypes = new ArrayList<>();
		piaoPoint = -1;
		passXiaoHuList = new ArrayList<>();
		passXiaoHuList2.clear();
		passHuValList = new ArrayList<>();
		huMjIds = new ArrayList<>();

		autoPlaySelf = false;
		autoPlay = false;
		autoPlayTime = 0;
		checkAutoPlay = false;
		lastCheckTime = System.currentTimeMillis();
		xiaoHuMjList = new ArrayList<>();
		gangCGangMjs  = new ArrayList<>();
		cpnum=0;
		baotingStatus=0;
	}

	public int getSeat() {
		return seat;
	}

	public void setSeat(int seat) {
		this.seat = seat;
	}

	public void initPais(List<JzMj> hand, List<JzMj> out) {
		if (hand != null) {
			this.handPais = hand;

		}
		if (out != null) {
			this.outPais = out;

		}
	}

	public void dealHandPais(List<JzMj> pais) {
		this.handPais = pais;
		getPlayingTable().changeCards(seat);
	}

	public boolean isGangshangHua() {
		return dahu.contains(7);
	}

	public List<JzMj> getHandMajiang() {
		return handPais;
	}

	// 0缺一色 1板板胡 2大四喜 3六六顺
	public List<JzMj> showXiaoHuMajiangs(int xiaohu,boolean connect) {
		JzMjTable table = getPlayingTable(JzMjTable.class);
		if (connect && !xiaoHuMjList.isEmpty()) {
			if (xiaohu == JzMjAction.QUEYISE || xiaohu == JzMjAction.BANBANHU || xiaohu == JzMjAction.YIZHIHUA) {
				// 缺一色 板板胡
				return handPais;
			} else {
				return xiaoHuMjList;
			}
		} else {
			JzMjiangHu hu = new JzMjiangHu();
			List<JzMj> copy2 = new ArrayList<>(getHandMajiang());
			JzMjRule.checkXiaoHu2(hu, copy2, true, table);

			HashMap<Integer, Map<Integer, List<JzMj>>> map = hu.getXiaohuMap();
			Map<Integer, List<JzMj>> cardMap = map.get(xiaohu);
			if (cardMap == null) {
				if (xiaohu == JzMjAction.ZHONGTUSIXI) {
					xiaohu = 10;
				} else if (xiaohu == JzMjAction.ZHONGTULIULIUSHUN) {
					xiaohu = 9;
				}
				cardMap = map.get(xiaohu);
			}
			if (xiaohu == 6) {// 缺一色
				return handPais;
			} else if (xiaohu == 7) {// 板板胡
				return handPais;
			} else if (xiaohu == 10 || xiaohu == JzMjAction.ZHONGTUSIXI) {// 大四喜|| xiaohu==

				return getShowCards(cardMap, connect, xiaohu);
			} else if (xiaohu == 9 || xiaohu == JzMjAction.ZHONGTULIULIUSHUN) {// 六六顺
				return getShowCards(cardMap, connect, xiaohu);
			} else if (xiaohu == JzMjAction.SANTONG) {// 三同
				return getShowCards(cardMap, connect, xiaohu);
			} else if (xiaohu == JzMjAction.JINGTONGYUNU) {// 金童玉女
				return getShowCards(cardMap, connect, xiaohu);
			} else if (xiaohu == JzMjAction.JIEJIEGAO) {// 节节高
				return getShowCards(cardMap, connect, xiaohu);
			} else {

				return handPais;
			}
		}
	}

	private List<JzMj> getShowCards(Map<Integer, List<JzMj>> cardMap,boolean connect,int xiaohuType) {
		if(cardMap==null) {
			return handPais;
		}
		List<JzMj> list =null;
		for (Entry<Integer, List<JzMj>> entry : cardMap.entrySet()) {

//			if(xiaohuType==JzMjAction.LIULIUSHUN||xiaohuType==JzMjAction.ZHONGTULIULIUSHUN){
//				
//				
//				
//			}else{
//				if (huXiaohuCards.contains(entry.getKey())) {
//						list=entry.getValue();
//				}
//			}
			if(xiaohuType==JzMjAction.DASIXI){
				xiaohuType =JzMjAction.ZHONGTUSIXI;
			}else if(xiaohuType==JzMjAction.LIULIUSHUN){
				xiaohuType =JzMjAction.ZHONGTULIULIUSHUN;
			}
			List<Integer> cardVuls = huxiaoHuCardVal.get(xiaohuType);
			if (cardVuls!=null&& cardVuls.contains(entry.getKey())) {
				list=entry.getValue();
			}
			else {
				if(!connect){
					return entry.getValue();
				}
			}
		}
		return list;
	}

	public List<Integer> getHandPais() {
		return JzMjHelper.toMajiangIds(handPais);
	}

	public List<Integer> getOutPais() {
		return JzMjHelper.toMajiangIds(outPais);
	}

	public List<JzMj> getOutMajing() {
		return outPais;
	}

	public void moMajiang(JzMj majiang) {
		setPassMajiangVal(0);
		handPais.add(majiang);
		getPlayingTable().changeCards(seat);
		clearPassXiaoHu2();
	}

	public void addOutPais(List<JzMj> cards, int action, int disSeat) {
		handPais.removeAll(cards);
		if (action == 0) {
			outPais.addAll(cards);
		} else {
			if (action == JzMjDisAction.action_buzhang) {
				buzhang.addAll(cards);
				JzMj pengMajiang = cards.get(0);
				Iterator<JzMj> iterator = peng.iterator();
				while (iterator.hasNext()) {
					JzMj majiang = iterator.next();
					if (majiang.getVal() == pengMajiang.getVal()) {
						buzhang.add(majiang);
						iterator.remove();
					}
				}
			} else if (action == JzMjDisAction.action_chi) {
				chi.addAll(cards);

			} else if (action == JzMjDisAction.action_peng) {
				peng.addAll(cards);
				// changeAction(0, 1);
			} else if (action == JzMjDisAction.action_minggang) {
				myExtend.setMjFengshen(FirstmythConstants.firstmyth_index12, 1);
				mGang.addAll(cards);
				if (cards.size() != 1) {
					changeAction(1, 1);
				} else {
					JzMj pengMajiang = cards.get(0);
					Iterator<JzMj> iterator = peng.iterator();
					while (iterator.hasNext()) {
						JzMj majiang = iterator.next();
						if (majiang.getVal() == pengMajiang.getVal()) {
							mGang.add(majiang);
							iterator.remove();
						}
					}
					changeAction(2, 1);
				}

			} else if (action == JzMjDisAction.action_angang) {
				myExtend.setMjFengshen(FirstmythConstants.firstmyth_index12, 1);
				aGang.addAll(cards);
				changeAction(3, 1);
			}
			else if (action == JzMjDisAction.action_buzhang_an) {
				myExtend.setMjFengshen(FirstmythConstants.firstmyth_index12, 1);
				buzhangAn.addAll(cards);
				changeAction(8, 1);
			}
			getPlayingTable().changeExtend();
			addCardType(action, cards, disSeat,0);
		}

		getPlayingTable().changeCards(seat);
	}

	public void addCardType(int action, List<JzMj> disCardList, int disSeat, int layEggId) {
		if (action != 0) {
			if (action == JzMjDisAction.action_buzhang && disCardList.size() == 1) {
				JzMj majiang = disCardList.get(0);
				for (JzMjCardDisType disType : cardTypes) {
					if (disType.getAction() == JzMjDisAction.action_peng) {
						if (disType.isHasCardVal(majiang)) {
							disType.setAction(JzMjDisAction.action_buzhang);
							disType.addCardId(majiang.getId());
							disType.setDisSeat(seat);
							break;
						}
					}
				}
			} else if (action == JzMjDisAction.action_minggang && disCardList.size() == 1) {
				JzMj majiang = disCardList.get(0);
				for (JzMjCardDisType disType : cardTypes) {
					if (disType.getAction() == JzMjDisAction.action_peng) {
						if (disType.isHasCardVal(majiang)) {
							disType.setAction(JzMjDisAction.action_minggang);
							disType.addCardId(majiang.getId());
							disType.setDisSeat(seat);
							break;
						}
					}
				}
			} else {
				JzMjCardDisType type = new JzMjCardDisType();
				type.setAction(action);
				type.setCardIds(JzMjHelper.toMajiangIds(disCardList));
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

	public void removeOutPais(List<JzMj> cards, int action) {
		boolean remove = outPais.removeAll(cards);
		if (remove) {
			if (action == JzMjDisAction.action_peng) {
				changeAction(4, 1);
			} else if (action == JzMjDisAction.action_minggang) {
				changeAction(5, 1);
			}
			getPlayingTable().changeCards(seat);

		}
	}

	public String toExtendStr() {
		StringBuffer sb = new StringBuffer();
		sb.append(JzMjHelper.implodeMajiang(peng, ",")).append("|");
		sb.append(JzMjHelper.implodeMajiang(aGang, ",")).append("|");
		sb.append(JzMjHelper.implodeMajiang(mGang, ",")).append("|");
		sb.append(StringUtil.implode(actionArr)).append("|");
		sb.append(StringUtil.implode(actionTotalArr)).append("|");
		sb.append(StringUtil.implode(passGangValList, ",")).append("|");
		sb.append(JzMjHelper.implodeMajiang(chi, ",")).append("|");
		sb.append(JzMjHelper.implodeMajiang(buzhang, ",")).append("|");
		sb.append(StringUtil.implode(dahu, ",")).append("|");
		sb.append(StringUtil.implode(huXiaohu, ",")).append("|");
		sb.append(StringUtil.implode(passXiaoHuList, ",")).append("|");
		sb.append(StringUtil.implode(passHuValList, ",")).append("|");
		sb.append(JzMjHelper.implodeMajiang(buzhangAn, ",")).append("|");
		sb.append(StringUtil.implode(huXiaohuCards, ",")).append("|");

		String huXiaoHuCardsV = DataMapUtil.explodeListMap(huxiaoHuCardVal);
		sb.append(huXiaoHuCardsV).append("|");

		List<Integer> idList = new ArrayList<>();
		for(JzMj mj : xiaoHuMjList){
			idList.add(mj.getId());
		}
		sb.append(StringUtil.implode(idList, ",")).append("|");


		sb.append(StringUtil.implode(gangCGangMjs, ",")).append("|");


		return sb.toString();

	}

	public void initExtend(String info) {
		if (StringUtils.isBlank(info)) {
			return;
		}
		int i = 0;
		String[] values = info.split("\\|");
		String val1 = StringUtil.getValue(values, i++);
		peng = JzMjHelper.explodeMajiang(val1, ",");
		String val2 = StringUtil.getValue(values, i++);
		aGang = JzMjHelper.explodeMajiang(val2, ",");
		String val3 = StringUtil.getValue(values, i++);
		mGang = JzMjHelper.explodeMajiang(val3, ",");
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
		chi = JzMjHelper.explodeMajiang(val7, ",");
		String val8 = StringUtil.getValue(values, i++);
		buzhang = JzMjHelper.explodeMajiang(val8, ",");
		String val9 = StringUtil.getValue(values, i++);
		if (!StringUtils.isBlank(val9)) {
			dahu = StringUtil.explodeToIntList(val9);
		}
		String val10 = StringUtil.getValue(values, i++);
		if (!StringUtils.isBlank(val10)) {
			huXiaohu = StringUtil.explodeToIntList(val10);
		}
		String val11 = StringUtil.getValue(values, i++);
		if (!StringUtils.isBlank(val11)) {
			passXiaoHuList = StringUtil.explodeToIntList(val11);
		}
		String val12 = StringUtil.getValue(values, i++);
		if (!StringUtils.isBlank(val12)) {
			passHuValList = StringUtil.explodeToIntList(val12);
		}
		String val13 = StringUtil.getValue(values, i++);
		buzhangAn = JzMjHelper.explodeMajiang(val13, ",");


		String val14 = StringUtil.getValue(values, i++);
		if (!StringUtils.isBlank(val14)) {
			huXiaohuCards = StringUtil.explodeToIntList(val14);
		}

		String val15 = StringUtil.getValue(values, i++);
		if (!StringUtils.isBlank(val15)) {
			huxiaoHuCardVal.putAll(DataMapUtil.toListMap(val15));
//			huxiaoHuCardVal = StringUtil.explodeToIntList(val15);
		}

		String val16 = StringUtil.getValue(values, i++);
		if (!StringUtils.isBlank(val16)) {
			List<Integer> idList = StringUtil.explodeToIntList(val16);
			for(Integer id : idList){
				xiaoHuMjList.add(JzMj.getMajang(id));
			}
		}


		String val17 = StringUtil.getValue(values, i++);
		if (!StringUtils.isBlank(val17)) {
			gangCGangMjs = StringUtil.explodeToIntList(val17);
		}

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
		sb.append(isxsz).append(",");
		sb.append(cpnum).append(",");
		sb.append(baotingStatus).append(",");
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
			this.state = JzMjEnumHelper.getPlayerState(stateVal);
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
			this.isxsz = StringUtil.getIntValue(values, i++);
			this.cpnum = StringUtil.getIntValue(values, i++);
			this.baotingStatus = StringUtil.getIntValue(values, i++);

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
			list.addAll(JzMjHelper.toMajiangIds(peng));
		}
		if (!mGang.isEmpty()) {
			list.addAll(JzMjHelper.toMajiangIds(mGang));
		}
		if (!aGang.isEmpty()) {
			list.addAll(JzMjHelper.toMajiangIds(aGang));
		}
		if (!buzhang.isEmpty()) {
			list.addAll(JzMjHelper.toMajiangIds(buzhang));
		}
		if (!buzhangAn.isEmpty()) {
			list.addAll(JzMjHelper.toMajiangIds(buzhangAn));
		}
		if (!chi.isEmpty()) {
			list.addAll(JzMjHelper.toMajiangIds(chi));
		}

		return list;
	}

	public List<PhzHuCards> buildDisCards(long lookUid) {
		return buildDisCards(lookUid, true);
	}

	public List<PhzHuCards> buildDisCards(long lookUid, boolean hide) {
		List<PhzHuCards> list = new ArrayList<>();
		for (JzMjCardDisType type : cardTypes) {
			if (hide && lookUid != this.userId && type.getAction() == JzMjDisAction.action_angang) {
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
		res.addAllAngangIds(JzMjHelper.toMajiangIds(aGang));
		res.addAllAngangIds(JzMjHelper.toMajiangIds(buzhangAn));
		res.addAllMoldCards(buildDisCards(userId));
		List<JzMj> gangList = getGang();
		// 是否杠过
		res.addExt(gangList.isEmpty() ? 0 : 1);


		JzMjTable table = getPlayingTable(JzMjTable.class);
		// 现在是否自己摸的牌
		res.addExt(isAlreadyMoMajiang() ? 1 : 0);
		res.addExt(handPais != null ? handPais.size() : 0);
		res.addExt(piaoPoint);// 飘分
		res.addExt(autoPlay ? 1 : 0);//4
		res.addExt(autoPlaySelf ? 1 : 0);//5


		res.addExt(Integer.valueOf(getPayBindId()+"")); //6绑定邀请码
		res.addExt(baotingStatus);//7
		res.addExt(isxsz);//8
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
		changeTbaleInfo();
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
		huXiaohu = new ArrayList<>();
		peng.clear();
		aGang.clear();
		mGang.clear();
		chi.clear();
		buzhang.clear();
		buzhangAn.clear();
		cardTypes.clear();
		setPassMajiangVal(0);
		clearPassGangVal();
		clearPassXiaoHu();
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
		huXiaohuCards.clear();
		huxiaoHuCardVal.clear();
		xiaoHuMjList.clear();
		gangCGangMjs.clear();
		xjsHuType=0;
		xjsAnGangPoint=0;
		xjsMingGangPoint=0;
		xjsZhiGangPoint=0;
		setBaotingStatus(0);
		setXjsHuType(0);
		setXjsAnGangPoint(0);
		setXjsMingGangPoint(0);
		setXjsZhiGangPoint(0);
		cpnum=0;
		setIsxsz(0);
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
		res.setSex(sex);
		res.addAllHandPais(getHandPais());
		List<PhzHuCards> list = new ArrayList<>();
		for (JzMjCardDisType type : cardTypes) {
			list.add(type.buildMsg().build());
		}
		res.addAllMoldPais(list);

		if(isBaoting() && !dahu.contains(15)){
			//输家报听设置xjs面板报听
			dahu.add(15);
		}
		res.addAllDahus(dahu);
		res.addAllXiaohus(huXiaohu);
		res.setFanPao(actionArr[0]==1?1:0);

		res.addExt(getPiaoPoint());
		res.addExt(gangPoint);//1 总杠分
		res.addExt(isxsz);// 2  闲上庄
		res.addExt(xjsHuType);// 3   1小胡自摸  2小胡接炮 3大胡自摸 	4大胡接炮  5 小胡放炮  6 大胡放炮
		res.addExt(xjsMingGangPoint);// 4  明杠分
		res.addExt(xjsAnGangPoint);// 5 暗杠分
		res.addExt(xjsZhiGangPoint);// 6 直杠

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
		res.setSeat(seat);
		if (!StringUtils.isBlank(getHeadimgurl())) {
			res.setIcon(getHeadimgurl());
		} else {
			res.setIcon("");
		}
		res.setSex(sex);
		res.addAllHandPais(getHandPais());
		List<PhzHuCards> list = new ArrayList<>();
		for (JzMjCardDisType type : cardTypes) {
			list.add(type.buildMsg().build());
		}

		res.addAllMoldPais(list);
		res.addAllDahus(dahu);
		res.addAllXiaohus(huXiaohu);
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
		huXiaohu = new ArrayList<>();
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
		clearPassXiaoHu();
		passXiaoHuList2.clear();
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
		huXiaohuCards.clear();
		huxiaoHuCardVal.clear();
		gangCGangMjs.clear();
		cpnum=0;
		setBaotingStatus(0);
		xjsHuType=0;
		xjsAnGangPoint=0;
		xjsMingGangPoint=0;
		xjsZhiGangPoint=0;

	}

	@Override
	public void initPais(String handPai, String outPai) {
		if (!StringUtils.isBlank(handPai)) {
			List<Integer> list = StringUtil.explodeToIntList(handPai);
			this.handPais = JzMjHelper.toMajiang(list);
		}

		if (!StringUtils.isBlank(outPai)) {
			String[] values = outPai.split(";");
			int i = -1;
			for (String value : values) {
				i++;
				if (i == 0) {
					List<Integer> list = StringUtil.explodeToIntList(value);
					this.outPais = JzMjHelper.toMajiang(list);
				} else {
					JzMjCardDisType type = new JzMjCardDisType();
					type.init(value);
					cardTypes.add(type);

					List<JzMj> majiangs = JzMjHelper.toMajiang(type.getCardIds());
					if (type.getAction() == JzMjDisAction.action_angang) {
						aGang.addAll(majiangs);
					} else if (type.getAction() == JzMjDisAction.action_minggang) {
						mGang.addAll(majiangs);
					} else if (type.getAction() == JzMjDisAction.action_chi) {
						chi.addAll(majiangs);
					} else if (type.getAction() == JzMjDisAction.action_peng) {
						peng.addAll(majiangs);
					} else if (type.getAction() == JzMjDisAction.action_buzhang) {
						buzhang.addAll(majiangs);
					}else if (type.getAction() == JzMjDisAction.action_buzhang_an) {
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
	public List<JzMj> getCanChiMajiangs(JzMj disMajiang) {
		return JzMjTool.checkChi(handPais, disMajiang);
	}
	/**
	 * 闲家是否起手听牌
	 * @param
	 * @return
	 */
	public boolean isQishouTingPai() {
		List<JzMj> copy = new ArrayList<>(handPais);
		List<JzMj> gangList = getGang();

		List<JzMj> copyPeng = new ArrayList<>(peng);

		if (copy.size() % 3 != 2) {
			copy.add(JzMj.getMajang(201));
		}
		List<JzMj> bzCopy = new ArrayList<>(buzhang);
		bzCopy.addAll(buzhangAn);
		boolean jiang258 = true;
		JzMjTable table = getPlayingTable(JzMjTable.class);
		//开杠
		if(table.getJiajianghu()==1) {
			jiang258 =false;
		}
		JzMjiangHu huBean = JzMjTool.isChangshaHu2(copy, gangList, copyPeng, chi, bzCopy,false,jiang258,table,handPais.get(0));
		return huBean.isHu();
	}
	/**
	 * 是否第一次出完牌就听牌了
	 * @param
	 * @return
	 */
	public boolean isFirstOutTingPai() {
		List<JzMj> copy = new ArrayList<>(handPais);
    	List<JzMj> gangList = getGang();
//		gangList.addAll(JzMjQipaiTool.dropVal(copy, val));
		List<JzMj> copyPeng = new ArrayList<>(peng);
//		if (!peng.isEmpty()) {
//			gangList.addAll(JzMjQipaiTool.dropVal(copyPeng, val));
//		}

		if (copy.size() % 3 != 2) {
			copy.add(JzMj.getMajang(201));
		}
		List<JzMj> bzCopy = new ArrayList<>(buzhang);
		bzCopy.addAll(buzhangAn);
		boolean jiang258 = true;
		JzMjTable table = getPlayingTable(JzMjTable.class);
		//开杠
		if(table.getJiajianghu()==1) {
			jiang258 =false;
		}
		JzMjiangHu huBean = JzMjTool.isChangshaHu2(copy, gangList, copyPeng, chi, bzCopy,false,jiang258,table,handPais.get(0));
		return huBean.isHu();
	}
	/**
	 * 是否听牌
	 * @param val
	 * @return
	 */
	public boolean isTingPai(int val) {
		List<JzMj> copy = new ArrayList<>(handPais);
		List<JzMj> gangList = getGang();
		gangList.addAll(JzMjQipaiTool.dropVal(copy, val));

		List<JzMj> copyPeng = new ArrayList<>(peng);
		if (!peng.isEmpty()) {
			gangList.addAll(JzMjQipaiTool.dropVal(copyPeng, val));
		}

		if (copy.size() % 3 != 2) {
			copy.add(JzMj.getMajang(201));
		}
		List<JzMj> bzCopy = new ArrayList<>(buzhang);
		bzCopy.addAll(buzhangAn);
		boolean jiang258 = true;
		JzMjTable table = getPlayingTable(JzMjTable.class);
		//开杠
		if(table.getJiajianghu()==1) {
			jiang258 =false;
		}
		JzMjiangHu huBean = JzMjTool.isChangshaHu2(copy, gangList, copyPeng, chi, bzCopy,false,jiang258,table,handPais.get(0));
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
		for (JzMjCardDisType huxi : cardTypes) {
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
	public List<Integer> checkDisMajiang(JzMj majiang) {
		return checkDisMaj(majiang,true);
	}

	public List<Integer> checkDisMaj(JzMj majiang,boolean need258) {
		List<Integer> list = new ArrayList<>();
		JzMjAction actData = new JzMjAction();
		JzMjTable table = getPlayingTable(JzMjTable.class);
		// 没有出现漏炮的情况
		// if (passMajiangVal != majiang.getVal()) {
		if ((!table.moHu() || table.getDisCardSeat() == seat) && passMajiangVal == 0) {
			List<JzMj> copy = new ArrayList<>(handPais);
			copy.add(majiang);
			List<JzMj> bzCopy = new ArrayList<>(buzhang);
			bzCopy.addAll(buzhangAn);
			boolean jiang258 = true;

			if(table.getJiajianghu()==1) {
				if(table.getDisCardRound()==1) {
					jiang258 = false;
				}else if(table.getLeftMajiangCount()==0) {
					jiang258 = false;
				}else if(!need258) {
					jiang258 = false;
				}
			}
			JzMjiangHu hu = JzMjTool.isChangshaHu2(copy, getGang(), peng, chi, bzCopy,false,jiang258,table,majiang);

			if(!hu.isHu() && isBaoting()){
				return Collections.EMPTY_LIST;
			}

			if (hu.isHu() ||  isBaoting()) {
				//二人麻将
				if(table.getMaxPlayerCount()==2) {
					if(table.getOnlyDaHu()==1) {
						if(hu.getDahuList().size()>0 || !need258) {
							actData.addHu();
							//抢杠胡
						}else if(table.getMenqing()==1 &&!isChiPengGang()) {
							actData.addHu();
						}
					}else {
						if(table.getXiaohuZiMo() ==1 ) {
							if(table.getMenqing()==1 &&!isChiPengGang()) {
								actData.addHu();
							}
							else if(hu.getDahuList().size()>0||!need258||(table.getDisCardRound()==1&&getSeat()!= table.getMoMajiangSeat())) {
								actData.addHu();
							}

						}else{
							actData.addHu();
						}
					}

				}else {
					actData.addHu();
				}


			}
		}
		List<JzMj> gangList = getGang();
		if (table.getDisCardSeat() != seat) {
			// 现在出牌的人不是自己
			int count = JzMjHelper.getMajiangCount(handPais, majiang.getVal());
			if (count == 3) {
                actData.addMingGang();
//				boolean isTing = isTingPai(majiang.getVal());
//				if (isTing) {
//					actData.addMingGang();
//					actData.addBuZhang();
//				}else{
//					if(gangList.isEmpty()){
//						actData.addBuZhang();
//					}
//				}
			}
			//if(gangList.isEmpty()) {
			//杠后不能碰吃
				if (count >= 2) {
					actData.addPeng();
				}
				if (table.calcNextSeat(table.getDisCardSeat()) == seat) {
					if(!(table.getMaxPlayerCount()==2 && table.getBuChi()==1)) {
						List<JzMj> chi = JzMjTool.checkChi(handPais, majiang);
						if (!chi.isEmpty()) {
							actData.addChi();
						}
					}
				}
			//}
		} else {
			// 出牌的人是自己 (杠后补张)
			int count = JzMjHelper.getMajiangCount(handPais, majiang.getVal());
			if (count == 3) {
				boolean isTing = isTingPai(majiang.getVal());
				if (isTing) {
					actData.addAnGang();
					actData.addBuZhangAn();
				}else{
					if(gangList.isEmpty()){
						actData.addBuZhangAn();
					}
				}
			}
			Map<Integer, Integer> pengMap = JzMjHelper.toMajiangValMap(peng);
			if (pengMap.containsKey(majiang.getVal())) {
				boolean isTing = isTingPai(majiang.getVal());
				if (isTing) {
					actData.addMingGang();
					actData.addBuZhang();
				}else{
					if(gangList.isEmpty()){
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

	public JzMj getLastMoMajiang() {
		if (handPais.isEmpty()) {
			return null;

		} else {
			return handPais.get(handPais.size() - 1);

		}
	}

	public List<Integer> getHuXiaohu() {
		return huXiaohu;
	}

	public void setHuXiaohu(List<Integer> huXiaohu) {
		this.huXiaohu = huXiaohu;
	}

	public void addXiaoHu(int xiaoHuType) {
		this.huXiaohu.add(xiaoHuType);
		changeTbaleInfo();
	}

	public void addXiaoHu2(int xiaoHuType,int val) {
		this.huXiaohu.add(xiaoHuType);

		if(val>0 &&!huXiaohuCards.contains(val)) {
			if(xiaoHuType == JzMjAction.ZHONGTUSIXI ||xiaoHuType == JzMjAction.DASIXI)
				this.huXiaohuCards.add(val);
		}
		changeTbaleInfo();
	}



	public void addLiuLiuShunHu2(int xiaoHuType,List<Integer> mjs) {
		this.huXiaohu.add(xiaoHuType);

		if(xiaoHuType==JzMjAction.DASIXI){
			xiaoHuType =JzMjAction.ZHONGTUSIXI;
		}else if(xiaoHuType==JzMjAction.LIULIUSHUN){
			xiaoHuType =JzMjAction.ZHONGTULIULIUSHUN;
		}
		List<Integer> valus = huxiaoHuCardVal.get(xiaoHuType);
		if(valus==null){
			valus = new ArrayList<Integer>();
			huxiaoHuCardVal.put(xiaoHuType, valus);
		}

		if(!mjs.isEmpty()){
			for(Integer id : mjs){
				if(!valus.contains(id))
					valus.add(id);
			}

		}

		changeTbaleInfo();
	}

	public void addXiaoHuMjList(List<JzMj> mjs) {
		xiaoHuMjList.clear();
		xiaoHuMjList.addAll(mjs);
		changeTbaleInfo();
	}

	public void clearXiaoHuMjList(){
		xiaoHuMjList.clear();
		changeTableInfo();
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


	public void checkDahu() {
	}

	public int getDahuCount() {
		return dahu.size();
	}

	public int getDahuPointCount(){
		return JzMjiangHu.getDaHuPointCount(dahu);
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
	public JzMjiangHu checkHu(JzMj disMajiang, boolean isbegin) {
		List<JzMj> copy = new ArrayList<>(handPais);
		if (disMajiang != null) {
			copy.add(disMajiang);
		}
		List<JzMj> bzCopy = new ArrayList<>(buzhang);
		bzCopy.addAll(buzhangAn);
		boolean jiang258 = true;
		JzMjTable table = getPlayingTable(JzMjTable.class);

		if(table.getJiajianghu()==1) {
			if( seat !=table.getMoMajiangSeat()) {
				if( table.getDisCardRound()==1) {
					jiang258 = false;
				}
				JzMj mj  = table.getGangHuMajiang(seat);
				if(mj!=null && disMajiang!=null && mj.getVal()==disMajiang.getVal()) {
					jiang258 = false;//杠上炮
				}
				if(table.getLeftMajiangCount()==0){
					jiang258 = false;
				}
			}else if(table.getDisCardRound()==0){
				jiang258 = false;
			}else if(table.getLeftMajiangCount()==0) {
				jiang258 = false;
			}else if(getGang().size()>0) {
				jiang258 = false;
			}

		}

		JzMjiangHu hu = JzMjTool.isChangshaHu2(copy, getGang(), peng, chi, bzCopy, isbegin,jiang258,table,disMajiang);
		if(hu.isHu()) {
			if(table.getMenqing()==1 &&!isChiPengGang()) {
				hu.setMenqing(true);
				hu.initDahuList();
			}
		}
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
	public List<Integer> checkMo(JzMj majiang, boolean isBegin) {
		List<Integer> list = new ArrayList<>();
		JzMjAction actData = new JzMjAction();
		JzMjTable table = getPlayingTable(JzMjTable.class);
		boolean canXiaohu = false;
//		int xiaoHuType = 0;
		if (isAlreadyMoMajiang() || majiang != null || isBegin) {
			// 碰的时候判断不能胡牌
			List<JzMj> bzCopy = new ArrayList<>(buzhang);
			bzCopy.addAll(buzhangAn);
			boolean jiang258 = true;
			if(table.getJiajianghu()==1) {
				if (table.getDisCardRound()==0) {
					jiang258=false;
				}
			}
			List<JzMj> copy = new ArrayList<>(handPais);
			JzMjiangHu hu = JzMjTool.isChangshaHu2(copy, getGang(), peng, chi, bzCopy, isBegin,jiang258,table,majiang);
			if (hu.isHu()) {
				//二人麻将
				if(table.getMaxPlayerCount()==2) {
					if(table.getOnlyDaHu()==1) {
						if(table.getMenqing()==1 &&!isChiPengGang()) {
							actData.addZiMo();
						}

						if(table.getMenqingZM()==1 &&!isChiPengGang()) {
							actData.addHu();
						}
						if(hu.getDahuList().size()>0) {
							actData.addZiMo();
						}
					}else {
						actData.addZiMo();
					}
				}else {
					actData.addZiMo();
				}
			}
			if (hu.isXiaohu()) {// 小胡检测
				List<Integer> xiaohuList = hu.getXiaohuList();

				HashMap<Integer, Map<Integer, List<JzMj>>> xiaohuMap = hu.getXiaohuMap();
				for (int index : JzMjAction.xiaoHuPriority) {

					Map<Integer, List<JzMj>> xiaoHuCardMap = xiaohuMap.get(index);
					if(xiaoHuCardMap ==null) {
						continue;
					}
					List<Integer> keys = new ArrayList<Integer>();
					if(xiaoHuCardMap.size()==0) {
						keys.add(0);
					}else{
						keys.addAll(xiaoHuCardMap.keySet());
					}


					if(index == JzMjAction.ZHONGTULIULIUSHUN){
						boolean canLiu = true;

						List<Integer> cardVals = huxiaoHuCardVal.get(index);

						List<Integer> cardVals2 = huxiaoHuCardVal.get(JzMjAction.LIULIUSHUN);

						List<Integer> liuliuShunVals = new ArrayList<Integer>();

						if(cardVals!=null){
							liuliuShunVals.addAll(cardVals);
						}

						if(cardVals2!=null){
							liuliuShunVals.addAll(cardVals2);
						}
						List<JzMj>  mjs = xiaoHuCardMap.get(keys.get(0));
						if(mjs!=null){
							for(JzMj mj: mjs){
								if(mj.getVal()!=0&& liuliuShunVals.contains(mj.getVal())) {
									canLiu =  false;
								}
							}
						}
						if(!canLiu){
							continue;
						}
					}


					for(Integer key: keys) {
						//&& xiaohuList.get(index) == 1
						if (table.canXiaoHu(index) && canHuXiaoHu2(index,key) ) {
							//过了的不能胡
							if(getPassXiaoHuList2().contains(index)) {
								continue;
							}
							// 如果玩家有多个小胡 则一个个胡
							actData = new JzMjAction();
							actData.setVal(index);
							canXiaohu = true;
							break;
						}
					}

					if(canXiaohu) {
						break;
					}
				}

			}
		}
		if (!isAlreadyMoMajiang() && !canXiaohu && isBegin) {
			boolean isTing = isQishouTingPai();
			if(isTing){
				//addCpnum(1);
				actData.addBaoTing();// 可以报听
			}
		}
		if (isAlreadyMoMajiang() && !canXiaohu) {
			List<JzMj> gangList = getGang();
			Map<Integer, Integer> pengMap = JzMjHelper.toMajiangValMap(peng);

			for (JzMj handMajiang : handPais) {
				if (pengMap.containsKey(handMajiang.getVal())) {
					// 有碰过
					boolean isTing = isTingPai(handMajiang.getVal());
					if (isTing) {
						actData.addMingGang();// 可以杠
						actData.addBuZhang();// 可以补张
						break;
					}else{
						if(gangList.isEmpty()){
							actData.addBuZhang();// 可以补张
						}
					}
				}
			}
			Map<Integer, Integer> handMap = JzMjHelper.toMajiangValMap(handPais);
			if (handMap.containsValue(4)) {
				for (Map.Entry<Integer, Integer> entry : handMap.entrySet()) {
					if (entry.getValue() == 4) {
						actData.addBuZhangAn();
//						boolean isTing = isTingPai(entry.getKey());
//						//杠过之后不能再暗杠了
//						if (isTing&&gangList.isEmpty()) {
//							actData.addAnGang();// 可以杠
//							actData.addBuZhangAn(); // 可以补张
//							break;
//						}else{
//							if(gangList.isEmpty()){
//								actData.addBuZhangAn(); // 可以补张
//							}
//						}
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

//		if(table.getXiaoHuAuto()==1&&xiaoHuType>0){
//			table.addActionSeat(getSeat(), list);
//			List<JzMj> majiangs = new ArrayList<>(showXiaoHuMajiangs(xiaoHuType,false));
//			table.huXiaoHu(this, majiangs, xiaoHuType, JzMjDisAction.action_xiaohu);
//		}

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

	public List<JzMj> getPeng() {
		return peng;
	}

	public List<JzMj> getaGang() {
		return aGang;
	}

	public List<JzMj> getmGang() {
		return mGang;
	}

	public List<JzMj> getGang() {
		List<JzMj> gang = new ArrayList<>();
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
	public boolean isPassGang(JzMj majiang) {
		return passGangValList.contains(majiang.getVal());
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

	public static final List<Integer> wanfaList = Arrays.asList(GameUtil.play_type_mj_jzmj);

	public static void loadWanfaPlayers(Class<? extends Player> cls){
		for (Integer integer:wanfaList){
			PlayerManager.wanfaPlayerTypesPut(integer,cls, JzMjCommandProcessor.getInstance());
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
		JzMjTable table = getPlayingTable(JzMjTable.class);
		long now = System.currentTimeMillis();
		boolean auto = isAutoPlay();
		if (!auto && table.getIsAutoPlay() > 0) {
			//检查玩家是否进入系统托管状态
			if (!checkAutoPlay && table.getTableStatus()!= JzMjConstants.TABLE_STATUS_PIAO) {
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
					if (timeOut >= JzMjConstants.AUTO_READY_TIME) {
						setAutoPlayTime(0);
						return true;
					}
				} else if (autoType == 2) {
					if (timeOut >= JzMjConstants.AUTO_HU_TIME) {
						setAutoPlayTime(0);
						return true;
					}
				} else {
					if (timeOut >=JzMjConstants.AUTO_PLAY_TIME) {
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
		JzMjTable table = getPlayingTable(JzMjTable.class);
		if (this.autoPlay != autoPlay || autoPlaySelf != isSelf) {
			ComMsg.ComRes.Builder res = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_tuoguan, seat, autoPlay ? 1 : 0, 0, isSelf ? 1 : 0);
			GeneratedMessage msg = res.build();
			if (table != null) {
				table.broadMsgToAll(msg);
			}
			if (!getHandPais().isEmpty()) {
				table.addPlayLog(table.getDisCardRound() + "_" +getSeat() + "_" + JzMjConstants.action_tuoguan + "_" +(autoPlay?1:0));
			}
		}
		boolean needLog = this.autoPlay != autoPlay || this.autoPlaySelf != isSelf;
		this.autoPlay = autoPlay;
		this.autoPlaySelf = autoPlay && isSelf;
		this.checkAutoPlay = autoPlay;
		setLastCheckTime(System.currentTimeMillis());
		if (needLog && table != null) {
			table.changeExtend();
			LogUtil.msg("setAutoPlay|" + table.getIsAutoPlay() + "|" + getSeat() + "|" + getUserId() + "|" + (autoPlay ? 1 : 0) + "|" + (isSelf ? 1 : 0));
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
	 * 是否能小胡
	 * @param action
	 * @return
	 */
	public boolean canHuXiaoHu(int action) {
		if (huXiaohu.contains(action)) {
			return false;
		}
		if(passXiaoHuList.contains(action)){
			return false;
		}
		if (action == JzMjAction.ZHONGTUSIXI && huXiaohu.contains(JzMjAction.DASIXI)) {
			return false;
		} else if (action == JzMjAction.ZHONGTULIULIUSHUN && huXiaohu.contains(JzMjAction.LIULIUSHUN)) {
			return false;
		}
		return true;
	}


	public boolean canHuXiaoHu2(int action,int val) {
		int key = action;
		if(key==JzMjAction.DASIXI){
			key =JzMjAction.ZHONGTUSIXI;
		}else if(key==JzMjAction.LIULIUSHUN){
			key =JzMjAction.ZHONGTULIULIUSHUN;
		}
		List<Integer> vals = huxiaoHuCardVal.get(key);
		if(vals ==null){
			vals = new ArrayList<Integer>();
		}

		if (huXiaohu.contains(action)) {
			if(action == JzMjAction.YIZHIHUA || action == JzMjAction.QUEYISE ||action == JzMjAction.JINGTONGYUNU || action == JzMjAction.BANBANHU) {
				return false;
			}
//			if(action == JzMjAction.ZHONGTULIULIUSHUN||action == JzMjAction.LIULIUSHUN){
//				
//			}else {
//				if(val!=0&& huXiaohuCards.contains(val)) {
//					return false;
//				}
//			}
			if(val!=0&& vals.contains(val)) {
				return false;
			}

		}
		if (passXiaoHuList.contains(action)){
			return false;
		}

		if (action == JzMjAction.ZHONGTUSIXI && huXiaohu.contains(JzMjAction.DASIXI)&& vals.contains(val)) {
			return false;
		}

		else if (action == JzMjAction.ZHONGTULIULIUSHUN && huXiaohu.contains(JzMjAction.LIULIUSHUN)&& vals.contains(val)) {
			return false;
		}
		return true;
	}


	/**
	 * 可以碰可以杠的牌 选择了碰 再杠不算分
	 *
	 * @param xiaoHu
	 */
	public void addPassXiaoHu(int xiaoHu) {
		if (!this.passXiaoHuList.contains(xiaoHu)) {
			if(xiaoHu != JzMjAction.ZHONGTUSIXI && xiaoHu != JzMjAction.DASIXI && xiaoHu !=JzMjAction.ZHONGTULIULIUSHUN && xiaoHu !=JzMjAction.LIULIUSHUN) {
				this.passXiaoHuList.add(xiaoHu);
			}
			//	
			getPlayingTable().changeExtend();
		}
	}

	public void clearPassXiaoHu() {
		this.passXiaoHuList.clear();
		BaseTable table = getPlayingTable();
		if (table != null) {
			table.changeExtend();
		}
		this.passXiaoHuList2.clear();
	}

	public void clearPassXiaoHu2() {
		this.passXiaoHuList2.clear();

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

	public void addGangcGangMj(int id){
		if(gangCGangMjs.contains(id)){
			return;
		}
		gangCGangMjs.add(id);
	}


	public List<Integer> getGangCGangMjs() {
		return gangCGangMjs;
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

	public List<Integer> getPassXiaoHuList2() {
		return passXiaoHuList2;
	}

	public void setPassXiaoHuList2(List<Integer> passXiaoHuList2) {
		this.passXiaoHuList2 = passXiaoHuList2;
	}


	public void addPassXiaoHuList2(int xiaoHu) {
		passXiaoHuList2.add(xiaoHu);
		if(xiaoHu==10) {
			passXiaoHuList2.add(JzMjAction.ZHONGTUSIXI);
		}else if(xiaoHu==9) {
			passXiaoHuList2.add(JzMjAction.ZHONGTULIULIUSHUN);
		}

	}

	public List<Integer> getHuXiaohuCards() {
		return huXiaohuCards;
	}


	public List<JzMj> getChi() {
		return chi;
	}

	public List<JzMj> getBuzhang() {
		List<JzMj> bzCopy = new ArrayList<>(buzhang);
		bzCopy.addAll(buzhangAn);
		return bzCopy;
	}

	public void removeGangMj(JzMj mj){
		buzhang.remove(mj);
		mGang.remove(mj);
		for (JzMjCardDisType type : cardTypes) {
			if(type.getCardIds()!=null) {
				int size = type.getCardIds().size();
				for(int i=0;i<size;i++) {
					Integer id = type.getCardIds().get(i);
					if(id ==mj.getId()) {
						type.getCardIds().remove(i);
						break;
					}
				}
			}
		}
	}

	public HashMap<Integer, List<Integer>> getHuxiaoHuCardVal() {
		return huxiaoHuCardVal;
	}

	public long getLastPassTime() {
		return lastPassTime;
	}

	public void setLastPassTime(long lastPassTime) {
		this.lastPassTime = lastPassTime;
	}

	public int getIsxsz() {
		return isxsz;
	}

	public void setIsxsz(int isxsz) {
		this.isxsz = isxsz;
	}

	public int getBaotingStatus() {
		return baotingStatus;
	}

	public void setBaotingStatus(int baotingStatus) {
		this.baotingStatus = baotingStatus;
		changeExtend();
	}

	public boolean isBaoting(){
		return baotingStatus == 1;
	}

	public int getCpnum() {
		return cpnum;
	}

	public void setCpnum(int cpnum) {
		this.cpnum = cpnum;
	}
	public void addCpnum(int cpnum) {
		this.cpnum ++;
	}

	public void setState(player_state state) {
		this.state = state;
	}

	public int getXjsHuType() {
		return xjsHuType;
	}

	public void setXjsHuType(int xjsHuType) {
		this.xjsHuType = xjsHuType;
	}

	public int getXjsMingGangPoint() {
		return xjsMingGangPoint;
	}

	public void setXjsMingGangPoint(int xjsMingGangPoint) {
		this.xjsMingGangPoint = xjsMingGangPoint;
	}
	public void addXjsMingGangPoint(int xjsMingGangPoint) {
		this.xjsMingGangPoint += xjsMingGangPoint;
	}
	public int getXjsAnGangPoint() {
		return xjsAnGangPoint;
	}

	public void setXjsAnGangPoint(int xjsAnGangPoint) {
		this.xjsAnGangPoint = xjsAnGangPoint;
	}
	public void addXjsAnGangPoint(int xjsAnGangPoint) {
		this.xjsAnGangPoint += xjsAnGangPoint;
	}

	public int getXjsZhiGangPoint() {
		return xjsZhiGangPoint;
	}

	public void setXjsZhiGangPoint(int xjsZhiGangPoint) {
		this.xjsZhiGangPoint = xjsZhiGangPoint;
	}
	public void addXjsZhiGangPoint(int xjsZhiGangPoint) {
		this.xjsZhiGangPoint += xjsZhiGangPoint;
	}
}
