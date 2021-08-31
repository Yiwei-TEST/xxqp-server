package com.sy599.game.qipai.yywhz.bean;

import com.google.protobuf.GeneratedMessage;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.SharedConstants.player_state;
import com.sy599.game.db.bean.group.GroupUser;
import com.sy599.game.db.dao.group.GroupDao;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.TablePhzResMsg;
import com.sy599.game.msg.serverPacket.TablePhzResMsg.PhzHuCards;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes;
import com.sy599.game.qipai.yywhz.command.WaihuziCommandProcessor;
import com.sy599.game.qipai.yywhz.constant.EnumHelper;
import com.sy599.game.qipai.yywhz.constant.GuihzCard;
import com.sy599.game.qipai.yywhz.constant.WaihuziConstant;
import com.sy599.game.qipai.yywhz.rule.GuihuziIndex;
import com.sy599.game.qipai.yywhz.rule.GuihuziMenzi;
import com.sy599.game.qipai.yywhz.rule.GuihuziMingTangRule;
import com.sy599.game.qipai.yywhz.rule.GuihzCardIndexArr;
import com.sy599.game.qipai.yywhz.tool.GuihuziHuLack;
import com.sy599.game.qipai.yywhz.tool.GuihuziTool;
import com.sy599.game.util.DataMapUtil;
import com.sy599.game.util.GameUtil;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author liuping
 * 鬼胡子玩家信息
 */
public class WaihuziPlayer extends Player {
	/**
	 * 座位id
	 */
	private int seat;
	/**
	 * 状态 1进入 2已准备 3正在玩 4已结束
	 */
	private player_state state;
	/**
	 * 是否已进牌桌
	 */
	private int isEntryTable;
	/**
	 * 当前手牌
	 */
	private List<GuihzCard> handPais;
	/**
	 * 已打出的牌
	 */
	private List<GuihzCard> outPais;
	/**
	 * 碰的牌
	 */
	private List<GuihzCard> peng;
	/**
	 * 吃的牌
	 */
	private List<GuihzCard> chi;
	/**
	 * 偎的牌
	 */
	private List<GuihzCard> wei;
	/**
	 * 漂的牌
	 */
	private List<GuihzCard> piao;
	/**
	 * 溜的牌
	 */
	private List<GuihzCard> liu;
	/**
	 * 臭碰
	 */
	private List<Integer> passPeng;
	/**
	 * 臭吃
	 */
	private List<Integer> passChi;
	/**
	 * 臭门子
	 */
	private List<GuihuziMenzi> passMenzi;

	/**
	 * 是否有起手溜 1是 0否
	 */
	private int firstLiu;
	/**
	 * 胜场
	 */
	private int winCount;
	/**
	 * 负场
	 */
	private int lostCount;
	private int lostPoint;
	private int point;
	
	private int totalMingt;
	/**
	 * 胡牌信息
	 */
	private GuihuziHuLack hu;
	/**
	 * 出牌对应操作信息
	 */
	private List<CardTypeHuxi> cardTypes;
	/**
	 * 是否第一次出牌
	 */
	private boolean isFristDisCard;
	/**
	 * 初始手牌
	 */
	private List<Integer> firstPais;
	
	/**
	 * 小局结算 大胡列表
	 * 0项项息 1无息平 2对子胡 3黑胡 4黑对子胡 5一点朱 6十三火 7十四火 8十五火 9九对半 10大字胡 11小字胡 12海底 13天胡 14报听 15内圆 16外圆
	 */
	private List<Integer> dahu;
	
	/**
	 * 总结算
	 * 大胡次数  0项项息 1无息平 2对子胡 3黑胡 4黑对子胡 5一点朱 6十三火 7十四火 8十五火 9九对半 10大字胡 11小字胡 12海底 13天胡 14报听 15内圆 16外圆
	 */
	private int[] dahuCounts;
	
	
	private volatile boolean autoPlay = false;//托管
	private volatile long lastOperateTime = 0;//最后操作时间
	private volatile long lastCheckTime = 0;//最后检查时间

	private volatile long autoPlayTime = 0;//自动操作时间
    private volatile boolean isCheckAuto = false; //开始托管合计时
    
    private int totalPiao;
	/**
	 * 飘分
	 * 未开始抛分时该值为-1 表示还未进行抛分
	 * 牌局开始前玩家抛出的分数(相当于押注分) 抛出的分数在结算的时候  单方结算的分数=对方抛出的分数+自己抛出的分数+胡的基础分数
	 */
	private int piaoPoint = -1;
	
	 private int allHuxi;
	

	/**
	 * 听值牌集
	 */
//	private List<Integer> tingCards;

	public WaihuziPlayer() {
		handPais = new ArrayList<>();
		outPais = new ArrayList<>();
		peng = new ArrayList<>();
		passPeng = new ArrayList<>();
		passChi = new ArrayList<>();
		passMenzi = new ArrayList<>();
		chi = new ArrayList<>();
		wei = new ArrayList<>();
		piao = new ArrayList<>();
		liu = new ArrayList<>();
		cardTypes = new ArrayList<>();
		firstPais = new ArrayList<>();
		dahu = new ArrayList<>();
		dahuCounts = new int[19];
		firstLiu = 0;
		piaoPoint = -1;
		
		autoPlay = false;
		autoPlayTime = 0;
		lastCheckTime = System.currentTimeMillis();
//		tingCards = new ArrayList<>();
	}

//	public List<Integer> getTingCards() {
//		return tingCards;
//	}
//
//	public void setTingCards(List<Integer> tingCards) {
//		this.tingCards = tingCards;
//	}
	
	/**
	 * 获得初始手牌
	 * @return
	 */
	public List<Integer> getFirstPais() {
		return firstPais;
	}
	
	public int getDahuCount() {
		return dahu.size();
	}

	public List<Integer> getDahu() {
		return dahu;
	}

	public void setDahu(List<Integer> dahuList, Map<Integer, Integer> yuanMap) {
		this.dahu.clear();
		this.dahu = dahuList;
		for(int dahu : dahuList) {
			changeDahuCounts(dahu, 1);
		}
		if(yuanMap != null) {
			if(yuanMap.get(1) > 0) {
				changeDahuCounts(15, yuanMap.get(1));
			}
			if(yuanMap.get(2) > 0) {
				changeDahuCounts(16, yuanMap.get(2));
			}
			
			if(yuanMap.get(3) > 0) {
				changeDahuCounts(17, yuanMap.get(3));
			}
			if(yuanMap.get(4) > 0) {
				changeDahuCounts(18, yuanMap.get(4));
			}
		}
		getPlayingTable().changeExtend();
	}
	
	/**
	 * 总结算 大胡次数累计统计 
	 * @param index 大胡次数  0项项息 1无息平 2对子胡 3黑胡 4黑对子胡 5一点朱 6十三火 7十四火 8十五火 9九对半 10大字胡 11小字胡 12海底 13天胡 14报听 15内圆 16外圆
	 * @param val 次数
	 */
	public void changeDahuCounts(int index, int val) {
		int addVal = 0;
		if(index>=100) {
			addVal =index-100;
			dahuCounts[5] += addVal;
			return;
		}
		dahuCounts[index] += val;
		getPlayingTable().changeExtend();
	}
	
	public List<Integer> getdahuList(){
		return DataMapUtil.toList(dahuCounts);
	}
	
	public int getMingtangCount(){
		int total = 0;
		for(int i=0;i< dahuCounts.length;i++ ) {
			int value = dahuCounts[i];
			if(value>0 && i <=13){
				total +=1;
			}
		}
		return total;
	}
	
	/**
	 * 移掉出的牌
	 */
    void removeOutPais(GuihzCard card) {
		outPais.remove(card);
	}

	/**
	 * 摸牌
	 */
	public void moCard(GuihzCard card) {
		this.outPais.add(card);
		setFristDisCard(false);
	}

	public void tiLong(GuihzCard card) {
		this.handPais.add(card);
		compensateCard();
		changeSeat(seat);
	}

	public void disCard(int action, List<GuihzCard> disCardList) {
		for (GuihzCard disCard : disCardList) {
			handPais.remove(disCard);// 手牌里面去掉
		}
		if (action == WaihzDisAction.action_piao && GuihuziTool.isHasCardVal(peng, disCardList.get(0).getVal())) {// 碰 => 漂
			List<GuihzCard> removePengs = GuihuziTool.findGhzByVal(peng, disCardList.get(0).getVal());
			for(GuihzCard removeCard : removePengs) {
				peng.remove(removeCard);
			}
			Iterator<CardTypeHuxi> iterator = cardTypes.iterator();
			while (iterator.hasNext()) {
				CardTypeHuxi type = iterator.next();
				if (type.getAction() == WaihzDisAction.action_peng && type.isHasCard(disCardList.get(1))) {
					iterator.remove();
				}
			}
		} else if((action == WaihzDisAction.action_liu || action == WaihzDisAction.action_weiHouLiu) && GuihuziTool.isHasCardVal(wei, disCardList.get(0).getVal())) {// 偎 => 溜
			List<GuihzCard> removeWeis = GuihuziTool.findGhzByVal(wei, disCardList.get(0).getVal());
			for(GuihzCard removeCard : removeWeis) {
				wei.remove(removeCard);
			}
			Iterator<CardTypeHuxi> iterator = cardTypes.iterator();
			while (iterator.hasNext()) {
				CardTypeHuxi type = iterator.next();
				if (type.getAction() == WaihzDisAction.action_wei && type.isHasCard(disCardList.get(1))) {
					iterator.remove();
				}
			}
			action = WaihzDisAction.action_weiHouLiu;
		}
		if(action == WaihzDisAction.action_liu || action == WaihzDisAction.action_weiHouLiu|| action == WaihzDisAction.action_qishouLiu) {
			liu.addAll(disCardList);
		} else if(action == WaihzDisAction.action_piao) {
			piao.addAll(disCardList);
		} else if(action == WaihzDisAction.action_wei) {
			wei.addAll(disCardList);
		} else if (action == WaihzDisAction.action_peng) {
			peng.addAll(disCardList);
		} else if (action == WaihzDisAction.action_chi) {
			chi.addAll(disCardList);
		} else {
			outPais.addAll(disCardList);
			int passVal = disCardList.get(0).getVal();
			if(!passChi.contains(passVal))
				passChi.add(passVal);
			if(!passPeng.contains(passVal))
				passPeng.add(passVal);
		}
		setFristDisCard(false);
		if (disCardList.size() > 3 && disCardList.size() % 3 == 0) {
			for (int i = 0; i < disCardList.size() / 3; i++) {
				List<GuihzCard> list = disCardList.subList(i * 3, i * 3 + 3);
				addCardType(action, list);
			}
		} else {
			addCardType(action, disCardList);
		}
		changeSeat(seat);
		changeTableInfo();
	}

	public void addCardType(int action, List<GuihzCard> disCardList) {
		WaihuziTable table = getPlayingTable(WaihuziTable.class);
		int huxi = GuihuziTool.getYingHuxi(action, disCardList);
		if (action != 0) {
			CardTypeHuxi type = new CardTypeHuxi();
			type.setAction(action);
			type.setCardIds(GuihuziTool.toGhzCardIds(disCardList));
			type.setHux(huxi);
			boolean isSelfMo = table.isMoFlag() && table.getMoSeat() == seat;
			if(action == WaihzDisAction.action_liu || action == WaihzDisAction.action_weiHouLiu || action == WaihzDisAction.action_weiHouLiu) {
				isSelfMo = true;
			}
//			if(action == WaihzDisAction.action_piao) {
//				isSelfMo = false;
//			}
			type.setSelfMo(isSelfMo);
			cardTypes.add(type);
		}
	}
	
	public List<CardTypeHuxi> getCardTypes() {
		return cardTypes;
	}
	
	public List<GuihzCard> getPeng() {
		return peng;
	}
	
	public List<GuihzCard> getWei() {
		return wei;
	}

	/**
	 * 是否需要出牌
	 * 
	 * @return
	 */
	public boolean isNeedDisCard(int action) {
		if(action == WaihzDisAction.action_piao || action == WaihzDisAction.action_liu || action == WaihzDisAction.action_weiHouLiu || action == WaihzDisAction.action_qishouLiu) {
			return false;
		}
		return true;
//		return isFristDisCard();
	}

	/**
	 * 
	 */
	public void compensateCard() {
//		oweCardCount++;
		changeTableInfo();
	}

	/**
	 * 臭吃或者臭碰
	 * @param action
	 * @param passCard
	 */
	public void pass(int action, GuihzCard passCard, boolean isPass) {
		if (action == WaihzDisAction.action_chi) {
			if(!passChi.contains(passCard.getVal()))
				passChi.add(passCard.getVal());
			if(isPass) {// 只有吃的牌选择过才臭门子
				List<GuihzCard> copyHandPais = new ArrayList<>(handPais);
				GuihzCardIndexArr valArr = GuihuziTool.getMax(copyHandPais);
				List<GuihuziMenzi> menzis = valArr.getMenzis(false);
				for(GuihuziMenzi menzi : menzis) {
					GuihuziMenzi menziTemp = new GuihuziMenzi(menzi.getMenzi(), 0);
					List<GuihzCard> menziCards = GuihuziTool.findGhzCards(copyHandPais, menzi.getMenzi());
					List<GuihzCard> chiList = GuihuziTool.checkChi(menziCards, passCard);
					if(chiList.size() == menziCards.size() && !passMenzi.contains(menziTemp)) {
						passMenzi.add(menziTemp);//臭门子
					}
				}
			}
		} else if (action == WaihzDisAction.action_peng || action == WaihzDisAction.action_wei) {// 没偎的牌也不能再碰了
			if(!passPeng.contains(passCard.getVal()))
				passPeng.add(passCard.getVal());
		} else
			return;
		changeTableInfo();
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
			this.state = EnumHelper.getPlayerState(stateVal);
			this.isEntryTable = StringUtil.getIntValue(values, i++);

			String passPengStr = StringUtil.getValue(values, i++);
			if (!StringUtils.isBlank(passPengStr)) {
				passPeng = StringUtil.explodeToIntList(passPengStr, "_");
			}
			String passChiStr = StringUtil.getValue(values, i++);
			if (!StringUtils.isBlank(passChiStr)) {
				passChi = StringUtil.explodeToIntList(passChiStr, "_");
			}
			
			this.winCount = StringUtil.getIntValue(values, i++);
			this.lostCount = StringUtil.getIntValue(values, i++);
			this.lostPoint = StringUtil.getIntValue(values, i++);
			this.point = StringUtil.getIntValue(values, i++);
			this.piaoPoint = StringUtil.getIntValue(values, i++);
			
			setTotalPoint(StringUtil.getIntValue(values, i++));
			this.isFristDisCard = StringUtil.getIntValue(values, i++) == 1;
			setMaxPoint(StringUtil.getIntValue(values, i++));
			String firstPaisStr = StringUtil.getValue(values, i++);
			if (!StringUtils.isBlank(firstPaisStr)) {
				firstPais = StringUtil.explodeToIntList(firstPaisStr, "_");
			}
			String dahuStr = StringUtil.getValue(values, i++);
			if (!StringUtils.isBlank(dahuStr)) {
				dahu = StringUtil.explodeToIntList(dahuStr, "_");
			}
			String dahuCountStr = StringUtil.getValue(values, i++);
			if (!StringUtils.isBlank(dahuCountStr)) {
				dahuCounts = StringUtil.explodeToIntArray(dahuCountStr, "_");
			}
			
			passMenzi.clear();
			String passMenziStr = StringUtil.getValue(values, i++);
			if (!StringUtils.isBlank(passMenziStr)) {
				String[] str = StringUtil.explodeToStringArray(passMenziStr, "_");
				if(str != null && str.length > 0) {
					for(String menziStr : str) {
						List<Integer> menziIds = new ArrayList<>();
						String[] ids = menziStr.split("&");
						menziIds.add(Integer.parseInt(ids[0]));
						menziIds.add(Integer.parseInt(ids[1]));
						passMenzi.add(new GuihuziMenzi(menziIds, 0));
					}
				}
			}
			this.firstLiu = StringUtil.getIntValue(values, i++);
		}
	}

	@Override
	public void clearTableInfo() {
		BaseTable table = getPlayingTable();
		boolean isCompetition = false;
		if (table != null && table.isCompetition()) {
			isCompetition = true;
			endCompetition();
		}
		setIsEntryTable(0);
		changeIsLeave(0);
		handPais.clear();
		outPais.clear();
		changeState(null);
		dahu = new ArrayList<>();
		dahuCounts = new int[19];
		peng.clear();
		chi.clear();
		wei.clear();
		piao.clear();
		liu.clear();
		passChi.clear();
		passPeng.clear();
		passMenzi.clear();
		cardTypes.clear();
		setHu(null);
		setWinCount(0);
		setLostCount(0);
		setPoint(0);
		setLostPoint(0);
		setTotalPoint(0);
		setMaxPoint(0);
		setSeat(0);
		setPiaoPoint(-1);
		setFirstLiu(0);
		if (!isCompetition) {
			setPlayingTableId(0);
		}
		if(table.isAutoPlay() && autoPlay){
		    setAutoPlay(false,table);
        }
		
		autoPlay = false;
		lastCheckTime = 0;
		totalMingt =0;
		totalPiao = 0;
		 allHuxi=0;
		saveBaseInfo();
	}

	@Override
	public player_state getState() {
		return state;
	}

	@Override
	public int getIsEntryTable() {
		return isEntryTable;
	}

	@Override
	public void setIsEntryTable(int tableOnline) {
		this.isEntryTable = tableOnline;
		changeTableInfo();

	}

	@Override
	public int getSeat() {
		return seat;
	}

	@Override
	public void setSeat(int randomSeat) {
		seat = randomSeat;
	}

	boolean isCanDisCard(List<Integer> disCards, GuihzCard nowDisCard) {
		if (disCards != null) {
			if (nowDisCard != null && disCards.contains(nowDisCard.getId())) {
				List<Integer> copy = new ArrayList<>(disCards);
				// 排除掉在桌面上已经出的牌
				copy.remove((Integer) nowDisCard.getId());
				if (!(getHandPais().containsAll(copy) || GuihuziTool.toGhzCardIds(getWei()).containsAll(copy))) {
					LogUtil.errorLog.info("找不到牌:" + disCards);
					return false;
				}
			} else {
				if (!getHandPais().containsAll(disCards)) {
					LogUtil.errorLog.info("找不到牌:" + disCards);
					return false;
				}
			}
		}
		return true;
	}

	public GuihuziHuLack checkHu(GuihzCard card, boolean isSelfMo) {
		List<GuihzCard> handCopy = new ArrayList<>(handPais);
		boolean hasWei = false;
		if(wei != null && wei.size() > 0)
			hasWei = true;
		int outYingxiCount = getOutYingXiCount();		
		WaihuziTable table = getPlayingTable(WaihuziTable.class);
		GuihuziHuLack lack = GuihuziTool.isHu(table, cardTypes, handCopy, card, isSelfMo, outYingxiCount, hasWei, table.getNowDisCardIds()==null);
		return lack;
	}
	
	/**
	 * 获取玩家盘桌上的牌的硬息数 
	 * 溜（四张）：1息
	 * 飘（四张）：1息
	 * 偎（三张）：1息
	 * 坎（手牌三张相同）：1息
	 * 碰（三张）：1息
	 * 二七十	     ：1息
	 * 贰柒拾        ：1息
	 * @return
	 */
	public int getOutYingXiCount() {
		int xi2710 = 0;
		if(chi != null && !chi.isEmpty()) {
			for(int i = 0; i < chi.size(); i += 3) {
				List<GuihzCard> list = chi.subList(i, i + 3);
				int redNum = GuihuziTool.findRedGhzs(list).size();
				if(redNum == 3) {
					xi2710 ++;
				}
			}
		}
		return (wei.size() / 3)*4 + (peng.size() / 3) + (liu.size() / 4 )*5  + xi2710;
	}

	/**
	 * 摸牌或出牌时 检查玩家可以做的操作
	 * @param card
	 * @param isSelfMo
	 * @param isFirstCard
	 * @return
	 */
	public WaihuziCheckCardBean checkCard(GuihzCard card, boolean isSelfMo, boolean isFirstCard) {
		return checkCard(card, isSelfMo, false, false, isFirstCard, false);
	}

	/**
	 * 庄家起手可做的操作
	 * @param card
	 * @param isSelfMo
	 * @param isBegin
	 * @param isFirstCard
	 * @return
	 */
	public WaihuziCheckCardBean checkCard(GuihzCard card, boolean isSelfMo, boolean isBegin, boolean isFirstCard) {
		return checkCard(card, isSelfMo, false, isBegin, isFirstCard, false);
	}
	
	/**
	 * 检查牌  可做的操作
	 * @param card
	 *            出的牌或者是摸的牌
	 * @param isSelfMo
	 *            是否是自己摸的牌
	 */
	public WaihuziCheckCardBean checkCard(GuihzCard card, boolean isSelfMo, boolean isPassHu, boolean isBegin, boolean isFirstCard, boolean isPass) {
		List<GuihzCard> copy = new ArrayList<>(handPais);
		WaihuziCheckCardBean check = new WaihuziCheckCardBean(seat, card);
		List<GuihzCard> handPais = new ArrayList<>(getHandGhzs());// 手牌
		GuihzCardIndexArr valArr = GuihuziTool.getMax(handPais);
		boolean isCanChiPeng = true;
		if (handPais.size() < 4) {// 必须要出牌但是可以可操作的牌已经不够了
			isCanChiPeng = false;
		}
		WaihuziTable table = getPlayingTable(WaihuziTable.class);
		GuihuziIndex index3 = valArr.getPaohzCardIndex(3);// 四张的牌
		GuihuziIndex index2 = valArr.getPaohzCardIndex(2);// 三张的牌
		GuihuziIndex index1 = valArr.getPaohzCardIndex(1);// 两张的牌
		GuihuziIndex index0 = valArr.getPaohzCardIndex(0);// 两张的牌
		// ---------------------------检查溜 偎 漂
		if (isSelfMo) {
			if (index3 != null && card == null && isBegin) {// 手里有四张
				check.setLiu(true);
			}
			if(index0!=null&& card == null) {
				for (int val : index0.getPaohzValMap().keySet()) {
					if (containsCard(wei, val)) {// 手里有1张
						check.setLiu(true);
						break;
					}
				}	
			
				
			}
			
			if(card != null && index2 != null) {// 手里有三张 可偎 可溜
				for (int val : index2.getPaohzValMap().keySet()) {
					if (val == card.getVal()) {
						check.setLiu(true);
						check.setWei(true);
						break;
					}
				}
			}
			if(card != null && containsCard(wei, card.getVal())) {// 偎牌后再摸了一张
				check.setLiu(true);
			}
			if(card != null && index1 != null) {// 手里有两张 可偎
				for (int val : index1.getPaohzValMap().keySet()) {
					if (val == card.getVal() ) {// 臭碰的牌不能再偎了&& !passPeng.contains(card.getVal())
						check.setWei(true);
					}
				}
			}
//			if(card != null && containsCard(peng, card.getVal()) && table.isKepiao()) {
//				check.setPiao(true);
//			}
		}
		// --------------------------检查胡
		boolean isMoFlag = table.isMoFlag();
		if(isMoFlag && card != null) {
			GuihuziHuLack lack = checkHu(card, isSelfMo);// 检查摸到的牌是否能胡
			if (lack.isHu()) {
				check.setHu(true);
			}
		} else {
			if(isBegin == true) {// 天胡判断
				GuihuziHuLack lack = checkHu(card, isSelfMo);
				if (lack.isHu()) {
					check.setHu(true);
				}
			}
		}
		// --------------------------检查碰
		if (card != null) {
			List<GuihzCard> sameList = GuihuziTool.getSameCards(copy, card);
			if (sameList.size() >= 2 && !isSelfMo) {// 手里有两张 可碰	
				if (isCanChiPeng && !passPeng.contains(card.getVal())) {// 检查碰
					check.setPeng(true);
				}
			}
			// ----------------------检查吃
			// 是否能检测能吃牌
			boolean isCheckChi = false;
			if (isMoFlag && table.getMoSeat() == seat) {// 如果这张牌是摸的,摸上来的人也能检测
				isCheckChi = true;
			}
			if (!isCheckChi && table.calcNextSeat(table.getDisCardSeat()) == seat) {// 摸牌打出的下家是一定要检测的
				isCheckChi = true;
			}
//			int hasChiSize = getChiSizeExcept2710();
//			if (hasChiSize >= 2 && !GuihuziTool.c2710List.contains(card.getPai())) {
//				isCheckChi = false;
//			}
			if (isCanChiPeng && isCheckChi && !isPass) {
				List<GuihuziMenzi> menzis = valArr.getMenzis(false);
				boolean canChi = false;
				for(GuihuziMenzi menzi : menzis) {
					List<GuihzCard> menziCards = GuihuziTool.findGhzCards(handPais, menzi.getMenzi());
					List<GuihzCard> chiList = GuihuziTool.checkChi(menziCards, card);
//					if(!checkCanDiscard(menziCards, true)) {
//						continue;
//					}
					if(chiList.size() == menziCards.size()) {
						if(!passChi.contains(card.getVal()) && !passMenzi.contains(menzi)) {
//							if(hasChiSize >= 2) {
//								chiList.add(card);
//								List<Integer> cardPais = GuihuziTool.toGhzCardVals(chiList, false);
//								if(GuihuziTool.c2710List.containsAll(cardPais)) {
//									check.setChi(true);
//									canChi = true;
//									break;
//								}
//							} else {
								check.setChi(true);
								canChi = true;
								break;
						}
					}
				}
				if(!canChi && !passChi.contains(card.getVal())) {// 不能吃后就再也不能吃了
					passChi.add(card.getVal());
					changeTableInfo();
				}
			}
		} else {
			if (!isPassHu) {
				GuihuziHuLack lack = checkHu(card, isSelfMo);
				check.setHu(lack.isHu());
			}
		}
		check.buildActionList();
		return check;
	}
	
	/**
	 * 检测溜（在要出牌时检查是否可溜）
	 */
	public WaihuziCheckCardBean checkLiu() {
		WaihuziCheckCardBean check = new WaihuziCheckCardBean(seat, null);
		List<GuihzCard> handPais = new ArrayList<>(getHandGhzs());// 手牌
		GuihzCardIndexArr valArr = GuihuziTool.getMax(handPais);
		GuihuziIndex index3 = valArr.getPaohzCardIndex(3);// 四张的牌
		GuihuziIndex index0 = valArr.getPaohzCardIndex(0);// 单张的牌
		// 手里有四张的牌 或者包含一张已经偎过的牌
		if (index3 != null) {// 手里有四张
			check.setLiu(true);
		} else {
			if(index0 != null) {// 手里有三张 可偎 可溜
				for (int val : index0.getPaohzValMap().keySet()) {
					if (containsCard(wei, val)) {
						check.setLiu(true);
						break;
					}
				}
			}
		}
		check.buildActionList();
		return check;
	}

	public List<GuihzCard> getPengOrWeiList(GuihzCard card, List<GuihzCard> cardList) {
		if (cardList != null && !cardList.isEmpty()) {// 吃牌不符合规则
			List<GuihzCard> gameList = GuihuziTool.getSameCards(cardList, card);
			if (gameList == null || gameList.isEmpty()) {
				return null;
			}
			if (gameList.size() != cardList.size()) {
				return null;
			}
		} else {
			cardList = GuihuziTool.getSameCards(handPais, card);
			if(cardList.size() >= 3) {
				cardList.remove(0);
			}
			if (cardList == null || cardList.size() < 2) {
				return null;	
			}
		}
		return cardList;
	}

	public List<GuihzCard> getChiList(GuihzCard card, List<GuihzCard> cardList) {
		if (cardList != null) {
			List<GuihzCard> sameList = GuihuziTool.findGhzByVal(cardList, card.getVal());
			List<GuihzCard> copy = new ArrayList<>(cardList);
			// 吃牌不符合规则
			if (copy.contains(card)) {
				copy.remove(card);
			}
			List<GuihzCard> chiList = GuihuziTool.checkChi(copy, card);
			if (chiList == null || chiList.isEmpty()) {
				return null;
			}
			// 找出有没有要吃的相同的牌
			if (sameList == null || sameList.isEmpty()) {
				// 如果没有其他相同的牌 直接吃
				return cardList;
			} else {
				// 有相同的牌
				List<GuihzCard> allChi = new ArrayList<>();
				allChi.addAll(chiList);
				//
				copy.removeAll(chiList);
				for (GuihzCard sameCard : sameList) {
					if (!copy.contains(sameCard)) {
						continue;
					}
					// 相同的牌还能不能继续吃
					List<GuihzCard> samechiList = GuihuziTool.checkChi(copy, sameCard);
					if (samechiList == null || samechiList.isEmpty()) {
						// 如果不能吃 则这个牌不能吃
						return null;
					}

					// 添加相同的牌 的吃
					copy.removeAll(samechiList);
					samechiList.add(0, sameCard);
					allChi.addAll(samechiList);
				}
				return cardList;
			}
		} else {
//			cardList = GuihuziTool.checkChi(handCard.getOperateCards(), card);
//			if (cardList == null || cardList.isEmpty()) {
//				return null;
//			}
		}
		List<GuihzCard> allChi = new ArrayList<>();
		return allChi;
	}

	@Override
	public void changeState(player_state state) {
		this.state = state;
		changeTableInfo();

	}

	@Override
	public void initNext() {
		setHu(null);
		setPoint(0);
		setFristDisCard(false);
		cardTypes.clear();
		dahu = new ArrayList<>();
		handPais.clear();
		outPais.clear();
		peng.clear();
		chi.clear();
		piao.clear();
		liu.clear();
		wei.clear();
		setPiaoPoint(-1);
		passChi.clear();
		passPeng.clear();
		passMenzi.clear();
		dahuCounts = new int[19];
		setFirstLiu(0);
		getPlayingTable().changeExtend();
		getPlayingTable().changeCards(seat);
		changeState(player_state.entry);
	}

	public List<GuihzCard> getHandGhzs() {
		return handPais;
	}

	@Override
	public List<Integer> getHandPais() {
		return GuihuziTool.toGhzCardIds(handPais);
	}

	public List<Integer> getOutPais() {
		return GuihuziTool.toGhzCardIds(outPais);
	}

	public List<GuihzCard> getOutPaisCard() {
		return outPais;
	}

	/**
	 * 出牌
	 * @return
	 */
	public String buildOutPaiStr() {
		StringBuffer sb = new StringBuffer();
		List<Integer> outPais = getOutPais();
		sb.append(StringUtil.implode(outPais)).append(";");
		for (CardTypeHuxi huxi : cardTypes) {
			sb.append(huxi.toStr()).append(";");
		}
		return sb.toString();
	}

	/**
	 * 手牌
	 * @return
	 */
	public String buildHandPaiStr() {
		StringBuffer sb = new StringBuffer();
		List<Integer> outPais = getHandPais();
		List<Integer> piaoPais = GuihuziTool.toGhzCardIds(piao);
		List<Integer> liuPais = GuihuziTool.toGhzCardIds(liu);
		List<Integer> weiPais = GuihuziTool.toGhzCardIds(wei);
		List<Integer> pengPais = GuihuziTool.toGhzCardIds(peng);
		List<Integer> chiPais = GuihuziTool.toGhzCardIds(chi);
		sb.append(StringUtil.implode(outPais)).append(";");
		sb.append(StringUtil.implode(piaoPais)).append(";");
		sb.append(StringUtil.implode(liuPais)).append(";");
		sb.append(StringUtil.implode(weiPais)).append(";");
		sb.append(StringUtil.implode(pengPais)).append(";");
		sb.append(StringUtil.implode(chiPais)).append(";");
		return sb.toString();
	}

	@Override
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
		sb.append(StringUtil.implode(passPeng, "_")).append(",");
		sb.append(StringUtil.implode(passChi, "_")).append(",");
		sb.append(winCount).append(",");
		sb.append(lostCount).append(",");
		sb.append(lostPoint).append(",");
		sb.append(point).append(",");
		sb.append(piaoPoint).append(",");
		
		sb.append(getTotalPoint()).append(",");
		sb.append(isFristDisCard ? 1 : 0).append(",");
		sb.append(getMaxPoint()).append(",");
		sb.append(StringUtil.implode(firstPais, "_")).append(",");
		sb.append(StringUtil.implode(dahu, "_")).append(",");
		sb.append(StringUtil.implode(dahuCounts, "_")).append(",");
		sb.append(StringUtil.implode(passMenzi, "_")).append(",");
		sb.append(firstLiu).append(",");
		return sb.toString();
	}

	/**
	 * 单局详情
	 */
	public TablePhzResMsg.ClosingPhzPlayerInfoRes.Builder bulidOneClosingPlayerInfoRes() {
		TablePhzResMsg.ClosingPhzPlayerInfoRes.Builder res = TablePhzResMsg.ClosingPhzPlayerInfoRes.newBuilder();
		res.setUserId(userId + "");
		res.addAllCards(getHandPais());
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
		//res.addStrExt(piaoPoint+"");
		res.setFinalPoint(piaoPoint);
		
		res.addStrExt(0+"");
		res.addStrExt(0+"");
//		if(hu != null && hu.getYuanMap() != null) {
//			res.setNeiYuanNum(hu.getYuanMap().get(1));
//			res.setWaiYuanNum(hu.getYuanMap().get(2));
//		}
//		res.addAllDahus(dahu);
//		res.addAllMcards(buildGhzHuCards());
		return res;
	}

	/**
	 * 总局详情
	 * @return
	 */
	public TablePhzResMsg.ClosingPhzPlayerInfoRes.Builder bulidTotalClosingPlayerInfoRes(int point) {
		TablePhzResMsg.ClosingPhzPlayerInfoRes.Builder res = bulidOneClosingPlayerInfoRes();
		res.setLostCount(lostCount);
		res.setWinCount(winCount);
		res.setMaxPoint(getMaxPoint());
		res.setBopiPoint(point);
		res.addStrExt(getMingtCount()+"");
		res.addStrExt(getTotalPiao()+"");
		res.setAllHuxi(getAllHuxi());
		//res.addAllDahuCounts(DataMapUtil.toList(dahuCounts));
		return res;
	}

	@Override
	public PlayerInTableRes.Builder buildPlayInTableInfo() {
		return buildPlayInTableInfo(0, false);
	}

	public PlayerInTableRes.Builder buildPlayInTableInfo(long lookUid, boolean isrecover) {
		PlayerInTableRes.Builder res = PlayerInTableRes.newBuilder();
		res.setUserId(userId + "");
		if (!StringUtils.isBlank(ip)) {
			res.setIp(ip);
		} else {
			res.setIp("");
		}
		
		// 当前出的牌
		WaihuziTable table = getPlayingTable(WaihuziTable.class);
		res.setName(name);
		res.setSeat(seat);
		res.setSex(sex);
		res.setPoint(getTotalPoint());
		
		
		res.addExt(getOutYingXiCount());// 0
		res.addExt(0);// 1
		res.addExt((table.getMasterId() == this.userId) ? 1 : 0);// 2
    	res.addExt(0);// 3
    	res.addExt(0);// 4
    	res.addExt(table.isGoldRoom()?(int)loadAllGolds():0);// 5
    	res.addExt(isAutoPlay() ? 1 : 0);// 6
    	res.addExt(getAutoPlayCheckedTime() > table.getAutoTimeOut() ? 1 : 0);//7
    	
    	if(table.getPiaoFen()>0) {
    		res.addExt(piaoPoint);// 飘分8
    	}else {
    		res.addExt(0);
    	}
		
	
		
		
		List<GuihzCard> nowDisCard = table.getNowDisCardIds();// 当前出的牌有玩家可操作
		List<Integer> outPais = new ArrayList<>(getOutPais());
		if(nowDisCard != null && !outPais.isEmpty() && outPais.contains(nowDisCard.get(0).getId()) && !table.getActionSeatMap().isEmpty()) {// 当牌桌有可做操作时 客户端不显示打出的牌
			outPais.remove((Integer)nowDisCard.get(0).getId());
		}
		res.addAllOutedIds(outPais);
		res.addAllMoldCards(buildGhzCards(lookUid));
		// ////////////////////////////////
		if (table.getDisCardSeat() == seat) {
			if (nowDisCard != null && !nowDisCard.isEmpty()) {
				int selfMo = 0;
				if (table.isSelfMo(this)) {
					selfMo = 1;
				}
				res.addOutCardIds(selfMo);
				GuihzCard beremoveCard = table.getBeRemoveCard();
				if (beremoveCard != null && nowDisCard.contains(beremoveCard)) {
					// 被移掉的牌
				} else {
					res.addAllOutCardIds(GuihuziTool.toGhzCardIds(nowDisCard));
				}
			}
		}
		if (!StringUtils.isBlank(getHeadimgurl())) {
			res.setIcon(getHeadimgurl());
		} else {
			res.setIcon("");
		}
		if (state == player_state.ready || state == player_state.play) {
			// 玩家装备已经准备和正在玩的状态时通知前台已准备
			res.setStatus(WaihuziConstant.state_player_ready);
		} else {
			res.setStatus(0);
		}
		if (isrecover) {
			// 0是否要的起 1是否报单 2是否暂离(1暂离0在线)
			List<Integer> recover = new ArrayList<>();
			recover.add(isEntryTable);
			res.addAllRecover(recover);
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

	@Override
	public void initPais(String handPai, String outPai) {
		if (!StringUtils.isBlank(outPai)) {
			String[] values = outPai.split(";");
			int i = -1;
			for (String value : values) {
				i++;
				if (i == 0) {
					this.outPais = GuihuziTool.explodeGhz(value, ",");
				} else {
					CardTypeHuxi type = new CardTypeHuxi();
					type.init(value);
					cardTypes.add(type);
				}
			}
		}
		if (!StringUtils.isBlank(handPai)) {
			String[] values = handPai.split(";");
			int i = 0;
			String outPaiStr = StringUtil.getValue(values, i++);
			String piaoPaiStr = StringUtil.getValue(values, i++);
			String liuPaiStr = StringUtil.getValue(values, i++);
			String weiPaiStr = StringUtil.getValue(values, i++);
			String pengPaiStr = StringUtil.getValue(values, i++);
			String chiPaiStr = StringUtil.getValue(values, i++);
			this.handPais = GuihuziTool.explodeGhz(outPaiStr, ",");
			this.piao = GuihuziTool.explodeGhz(piaoPaiStr, ",");
			this.liu = GuihuziTool.explodeGhz(liuPaiStr, ",");
			this.wei = GuihuziTool.explodeGhz(weiPaiStr, ",");
			this.peng = GuihuziTool.explodeGhz(pengPaiStr, ",");
			this.chi = GuihuziTool.explodeGhz(chiPaiStr, ",");
		}
	}

	@Override
	public void endCompetition1() {

	}

	public void dealHandPais(List<GuihzCard> list) {
		this.handPais = list;
		setFristDisCard(true);
		getPlayingTable().changeCards(seat);
	}

	public int getWinCount() {
		return winCount;
	}

	@Override
	public int getLostCount() {
		return lostCount;
	}

	public void setWinCount(int winCount) {
		this.winCount = winCount;
	}

	public void setLostCount(int lostCount) {
		this.lostCount = lostCount;
	}

	public int getLostPoint() {
		return lostPoint;
	}

	public void setLostPoint(int lostPoint) {
		this.lostPoint = lostPoint;
	}

	public int getPoint() {
		return point;
	}

	public int getFirstLiu() {
		return firstLiu;
	}

	public void setFirstLiu(int firstLiu) {
		this.firstLiu = firstLiu;
		changeTableInfo();
	}

	public void calcResult(int count, int point, boolean huangzhuang) {
		if (!huangzhuang) {
			if (point > 0) {
				this.winCount += count;
			} else {
				this.lostCount += count;
			}
		}

		changePoint(point);

	}

	public void setPoint(int point) {
		this.point = point;
	}

	public void changePoint(int point) {
		this.point += point;
		myExtend.changePoint(getPlayingTable().getPlayType(), point);
		changeTotalPoint(point);
		if (point > getMaxPoint()) {
			setMaxPoint(point);
		}
		changeTableInfo();
	}
	
	/**
	 * 总胡息
	 * 
	 * @return
	 */
	public int getTotalHu() {
		return 0;
	}

	public List<PhzHuCards> buildGhzHuCards() {
		List<PhzHuCards> list = new ArrayList<>();
		for (CardTypeHuxi type : cardTypes) {
			list.add(type.buildMsg().build());
		}
		if (hu != null && hu.getGhzHuCards() != null) {
			CardTypeHuxi huCardType = null;
			CardTypeHuxi twoCardType = null;
			for (CardTypeHuxi type : hu.getGhzHuCards()) {
				if(type.getAction()==-1) {
					list.add(type.buildMsg().build());
					continue;
				}
				if(hu.getCheckCard() != null && type.getCardIds().contains(hu.getCheckCard().getId())) {// 胡的牌放到最后面
					huCardType = type;
				} else if(type.getCardIds().size() == 2) {
					twoCardType = type;
				} else {
					list.add(type.buildMsg().build());
				}
				
			}
			if(huCardType != null)
				list.add(huCardType.buildMsg().build());
			if(twoCardType != null)
				list.add(twoCardType.buildMsg().build());
		} else {
			CardTypeHuxi handPaisType =  new CardTypeHuxi(-1, getHandPais(), 0, true);
			list.add(handPaisType.buildMsg().build());
		}
		return list;
	}

	public List<PhzHuCards> buildGhzCards(long lookUid) {
		List<PhzHuCards> list = new ArrayList<>();
		for (CardTypeHuxi type : cardTypes) {
			list.add(type.buildMsg().build());
		}
		return list;
	}

	/**
	 * 检查听牌
	 * @param table
	 * @param disPlayer
	 * @return
	 */
//	public boolean isTing(WaihuziTable table, WaihuziPlayer disPlayer){
//		List<GuihzCard> checkCards = new ArrayList<>(table.getLeftCards());
//		for(Player player : table.getPlayerMap().values()){
//			if(player.getUserId() != disPlayer.getUserId()){
//				WaihuziPlayer ghzPlayer = (WaihuziPlayer) player;
//				List<GuihzCard> cards = GuihuziTool.toGhzCards(ghzPlayer.getHandPais());  //玩家手牌
//				for(CardTypeHuxi huxi : ghzPlayer.cardTypes){
//					if(huxi.getAction() == WaihzDisAction.action_liu){
//						cards.addAll(GuihuziTool.toGhzCards(huxi.getCardIds()));  //玩家直接溜的牌
//					}
//				}
//				checkCards.addAll(cards);
//			}
//		}
//		if(tingCards != null && tingCards.size() > 0){
//			tingCards.clear();
//		}
//		List<Integer> cheked = new ArrayList<>();  //已检查牌值集
//		for(GuihzCard card : checkCards){
//			if(!cheked.contains(card.getVal())){
//				GuihuziHuLack lack = disPlayer.checkHu(card,true);
//				if(lack.isHu())
//				{
//					if(!tingCards.contains(card.getVal())){
//						tingCards.add(card.getVal());
//					}
//				}
//				cheked.add(card.getVal());
//			}
//		}
//		if(tingCards != null && tingCards.size() > 0){
//			return true;
//		}
//		return false;
//	}

	/**
	 * 胡牌时初始化大胡(名堂)列表
	 * @return
	 */
	public Map<Integer, Integer> initDahuList(List<Integer> mtList, boolean isBegin) {
		List<GuihzCard> allCards = new ArrayList<>();
		List<CardTypeHuxi> allHuxiCards = new ArrayList<>();
		for (CardTypeHuxi type : cardTypes) {
			allCards.addAll(GuihuziTool.toGhzCards(type.getCardIds()));
			allHuxiCards.add(type);
		}
		if (hu != null && hu.getGhzHuCards() != null) {
			allHuxiCards.addAll(hu.getGhzHuCards());
			for (CardTypeHuxi type : hu.getGhzHuCards()) {
				allCards.addAll(GuihuziTool.toGhzCards(type.getCardIds()));
			}
		}
		if(allCards.isEmpty()) {
			allCards.addAll(getHandGhzs());
		}
		Map<Integer, Integer> yuanMap =  GuihuziMingTangRule.calcMingTang(mtList, this, hu, allCards, allHuxiCards, isBegin);
		
		
		WaihuziTable table = getPlayingTable(WaihuziTable.class);
	
		if(table.getStartLeftCards().size()-table.getLeftCards().size() ==1&&!table.isOpreAc()) {
			mtList.add(11);
			mtList.remove((Integer)12);
		}
		
		setDahu(mtList, yuanMap);
		return yuanMap;
	}
	
	public int getTableHuxi() {
		int baseCount = wei.size() / 3 + liu.size() / 4;
		return baseCount * 5;
	}
	
	/**
	 * 算分初始化
	 * 先算基础分 溜 5偎4 坎3  碰1 二七十1
	 * 再算加分  大胡列表加分

	 * @param dahuList 大胡列表
	 * @param yuanMap 
	 * @return
	 */
	public int initHuxiPoint(List<Integer> dahuList, Map<Integer, Integer> yuanMap, int maxhuxi) {
		int basePoint = getYingxi(dahuList);
		WaihuziTable table = getPlayingTable(WaihuziTable.class);
		for(int dahu : dahuList) {
			if(dahu<100 ) {
				if(table.getMingtang()==2) {
					basePoint += 20;
				}
			}
			if(dahu == 3 || dahu == 7 || dahu == 8 || dahu == 2 || dahu == 10 || dahu==11 ||dahu==1) {
				basePoint += 100;
			} else if(dahu == 0 || dahu == 13 || dahu == 4 || dahu == 12) {
				basePoint += 60;
			} else if(dahu == 9) {
				basePoint += 30;
			} else if(dahu == 6 ) {
				basePoint += 120;
			}else if(dahu== 5) {
				basePoint += 80;
			}else if(dahu >100) {//红字叠加
				int addvalue = dahu-100;
				basePoint +=(10*addvalue);
			}
		}
		
		if(yuanMap.get(1) > 0) {
			int value = 20;
			if(table.getHaoFen()==2) {
				value+=10;
			}
			basePoint +=	(value*yuanMap.get(1));
		}
		if(yuanMap.get(2) > 0) {
			int value = 30;
			if(table.getHaoFen()==2) {
				value+=10;
			}
			basePoint +=	(value*yuanMap.get(2));
		}
		if(yuanMap.get(3) > 0) {
			int value = 40;
			if(table.getHaoFen()==2) {
				value+=10;
			}
			basePoint +=	(value*yuanMap.get(3));
		}
		if(yuanMap.get(4) > 0) {
			int value = 10;
			if(table.getHaoFen()==2) {
				value+=10;
			}
			basePoint +=	(value*yuanMap.get(4));
		}
		
			return basePoint;
	}

	public int getYingxi(List<Integer> dahuList) {
		int baseCount = 1;// 基础底分1分
	//	int kangOrCount = wei.size() / 3 + liu.size() / 4 + hu.getKangNum();
		if(!dahuList.isEmpty()) {// 存在大胡（海底 天胡 报听除外）时 取消基础底分
			for(int dahu : dahuList) {
				if(dahu >= 0 && dahu <= 13) {
					baseCount = 0;
					break;
				}
			}
		}
		
		List<PhzHuCards> huCards = buildGhzHuCards();
		int huxi= 0;
		for(PhzHuCards card: huCards ) {
			if(!dahuList.isEmpty() && (card.getAction() == WaihzDisAction.action_peng||card.getAction() == WaihzDisAction.action_chi ||card.getAction() == WaihzDisAction.action_shun||card.getAction() == WaihzDisAction.action_jiang||card.getAction() == WaihzDisAction.action_men)) {
				continue;
			}
			huxi +=card.getHuxi();
			
		}
		int basePoint = huxi + baseCount;
		return basePoint;
	}

	public List<GuihzCard> getAllCards() {
		List<GuihzCard> cards = new ArrayList<>();
		cards.addAll(piao);
		cards.addAll(liu);
		cards.addAll(wei);
		cards.addAll(peng);
		cards.addAll(chi);
		cards.addAll(handPais);
		return cards;
	}
	
	/**
	 * cardVal牌是否在containCards其中
	 * @param containCards
	 * @param cardVal
	 * @return
	 */
	public boolean containsCard(List<GuihzCard> containCards, int cardVal) {
		if(containCards != null) {
			for(GuihzCard card : containCards) {
				if(card.getVal() == cardVal)
					return true;
			}
		}
		return false;
	}

	public GuihuziHuLack getHu() {
		return hu;
	}

	public void setHu(GuihuziHuLack hu) {
		this.hu = hu;
	}

	public static void main(String[] args) {
		List<Integer> list1 = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6));
		System.out.println(list1.subList(0, 3));
		System.out.println(list1);
	}

	public boolean isFristDisCard() {
		return isFristDisCard;
	}

	public void setFristDisCard(boolean isFristDisCard) {
		if (this.isFristDisCard != isFristDisCard) {
			this.isFristDisCard = isFristDisCard;
			changeTableInfo();
		}
	}
	
	public List<Integer> getPassChi() {
		return passChi;
	}
	
	public List<Integer> getPassPeng() {
		return passPeng;
	}
	
	/**
	 * 获取玩家已吃牌的组数（除去2710）
	 * @return
	 */
	public int getChiSizeExcept2710() {
		if(chi.isEmpty())
			return 0;
		else {
			int chiSize = chi.size() / 3;
			for(int i = 0; i < chi.size(); i += 3) {
				List<GuihzCard> chiCards = chi.subList(i + 0, i + 3);
				List<Integer> cardPais = GuihuziTool.toGhzCardVals(chiCards, false);
				if(GuihuziTool.c2710List.containsAll(cardPais)) {
					chiSize --;
				}
			}
			return chiSize;
		}
	}
	
	/**
	 * 剔除吃过的牌的门子后 是否能吃
	 * @param preChiMenzi  预吃的门子
	 * @param chiCard 吃的牌
	 * @return
	 */
	public boolean getHasChiMenzi(List<GuihzCard> preChiMenzi, GuihzCard chiCard) {
		List<List<GuihzCard>> hasChiMenziList = new ArrayList<>();
		if(!chi.isEmpty()) {
			for(int i = 0; i < chi.size(); i += 3) {
				hasChiMenziList.add(chi.subList(i + 1, i + 3));
			}
		}
		if(preChiMenzi != null && !GuihuziTool.isSameCard(preChiMenzi))// 加上准备吃的门子
			hasChiMenziList.add(preChiMenzi);
		if(!hasChiMenziList.isEmpty()) {
			for(List<GuihzCard> menzi : hasChiMenziList) {
				if(GuihuziTool.checkChi(menzi, chiCard).size() == menzi.size()) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * 获取碰过或者偎过的牌
	 * @return
	 */
	/**
	 * @param preCard 预碰的牌
	 * @return
	 */
	public List<Integer> getHasPengOrWeiPais(GuihzCard preCard) {
		List<Integer> hasPengOrWeiList = new ArrayList<>();
		if(!peng.isEmpty()) {
			for(int i = 0; i < peng.size(); i++) {
				if(i % 3 == 0) {
					hasPengOrWeiList.add(peng.get(i).getVal());
				}
			}
		}
		if(!wei.isEmpty()) {
			for(int i = 0; i < wei.size(); i++) {
				if(i % 3 == 0) {
					hasPengOrWeiList.add(wei.get(i).getVal());
				}
			}
		}
		if(preCard != null)
			hasPengOrWeiList.add(preCard.getVal());
		return hasPengOrWeiList;
	}

	public void removePassChi(int val) {
		if (passChi.contains(val)) {
			int index = passChi.indexOf(val);
			passChi.remove(index);
			changeTableInfo();
		}
	}
	
	public List<GuihuziMenzi> getPassMenzi() {
		return passMenzi;
	}
	
	
    public boolean isCheckAuto() {
        return isCheckAuto;
    }

    public void setCheckAuto(boolean checkAuto) {
        isCheckAuto = checkAuto;
    }
	public boolean isAutoPlay() {
		return autoPlay;
	}

	public void setAutoPlay(boolean autoPlay,BaseTable table) {
		if (this.autoPlay != autoPlay){
			ComMsg.ComRes.Builder res = SendMsgUtil.buildComRes(132, seat,autoPlay?1:0, (int)userId);
			GeneratedMessage msg = res.build();
			for (Map.Entry<Long,Player> kv:table.getPlayerMap().entrySet()){
				Player player=kv.getValue();
				if (player.getIsOnline() == 0) {
					continue;
				}
				player.writeSocket(msg);
			}
			  if (!getHandPais().isEmpty()) {
			  table.addPlayLog(getSeat(), WaihuziConstant.action_tuoguan + "",(autoPlay?1:0) + "");
			  }
		}
		this.autoPlay = autoPlay;
		this.setCheckAuto(false);
		if(!autoPlay){
		    setAutoPlayCheckedTimeAdded(false);
        }
	}
	public long getLastOperateTime() {
		return lastOperateTime;
	}

	public void setLastOperateTime(long lastOperateTime) {
		this.lastCheckTime = 0;
		this.lastOperateTime = lastOperateTime;
		this.autoPlayTime = 0;
		this.setCheckAuto(false);
	}

	public long getLastCheckTime() {
		return lastCheckTime;
	}

	public void setLastCheckTime(long lastCheckTime) {
		this.lastCheckTime = lastCheckTime;
	}

	public long getAutoPlayTime() {
		return autoPlayTime;
	}

	public void setAutoPlayTime(long autoPlayTime) {
		this.autoPlayTime = autoPlayTime;
	}


	public void setPiaoPoint(int piaoPoint) {
		this.piaoPoint = piaoPoint;
		changeTableInfo();
	}

	public int getPiaoPoint() {
		return piaoPoint;
	}
	
	
	
	public void addMingtCount() {
		totalMingt +=getMingtangCount();
	}
	
	public int getMingtCount() {
		return totalMingt;
	}
	
	
	
	
	public int getAllHuxi() {
		return allHuxi;
	}

	public void addHuXi(int huxi) {
		this.allHuxi += huxi;
	}

	public int getTotalPiao() {
		return totalPiao;
	}

	public void addPiaoPoint(int piaoPoint) {
		this.totalPiao += piaoPoint;
	}

	/**
	 * 检查玩家是否还能出牌  (偎 碰 吃)
	 * @param operateCards  当前操作的牌
	 * @param isPengOrChi  是否碰或者吃
	 * @return
	 */
	public boolean checkCanDiscard(List<GuihzCard> operateCards, boolean isPengOrChi) {
		List<GuihzCard> copyHandPais = new ArrayList<>();
		copyHandPais.addAll(handPais);
		for(GuihzCard operateCard : operateCards) {
			copyHandPais.remove(operateCard);
		}
		boolean isPeng = GuihuziTool.isSameCard(operateCards);
		GuihzCard prePengCard = null;
		List<GuihzCard> preChiMenzi = null;
		if(isPengOrChi) {
			if(isPeng)
				prePengCard = operateCards.get(0);
			else
				preChiMenzi = operateCards.subList(1, 3);
		}
		for(GuihzCard operateCard : copyHandPais) {
			if(!getHasPengOrWeiPais(prePengCard).contains((Integer)operateCard.getVal()) && !getHasChiMenzi(preChiMenzi, operateCard)) {// 已吃过的门子 或者碰过的牌 不能打出去了
				return true;
			}
		}
		return false;
	}

	public static final List<Integer> wanfaList = Arrays.asList(GameUtil.game_type_YyWaiHZ);

	public static void loadWanfaPlayers(Class<? extends Player> cls){
		for (Integer integer:wanfaList){
			PlayerManager.wanfaPlayerTypesPut(integer,cls, WaihuziCommandProcessor.getInstance());
		}
	}

}
