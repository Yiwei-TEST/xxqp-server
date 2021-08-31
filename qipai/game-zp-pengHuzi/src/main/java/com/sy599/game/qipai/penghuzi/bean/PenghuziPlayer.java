package com.sy599.game.qipai.penghuzi.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.google.protobuf.GeneratedMessage;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.SharedConstants.player_state;
import com.sy599.game.db.bean.group.GroupUser;
import com.sy599.game.db.dao.group.GroupDao;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.TablePhzResMsg.ClosingPhzPlayerInfoRes;
import com.sy599.game.msg.serverPacket.TablePhzResMsg.PhzHuCards;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes;
import com.sy599.game.qipai.penghuzi.command.PaohuziCommandProcessor;
import com.sy599.game.qipai.penghuzi.constant.EnumHelper;
import com.sy599.game.qipai.penghuzi.constant.PengHZMingTang;
import com.sy599.game.qipai.penghuzi.constant.PenghuziConstant;
import com.sy599.game.qipai.penghuzi.constant.PenghzCard;
import com.sy599.game.qipai.penghuzi.rule.PenghuziIndex;
import com.sy599.game.qipai.penghuzi.rule.PenghuziMingTangRule;
import com.sy599.game.qipai.penghuzi.rule.PenghzCardIndexArr;
import com.sy599.game.qipai.penghuzi.tool.PenghuziHuLack;
import com.sy599.game.qipai.penghuzi.tool.PenghuziTool;
import com.sy599.game.util.DataMapUtil;
import com.sy599.game.util.GameUtil;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.util.StringUtil;

public class PenghuziPlayer extends Player {
	// 座位id
	private int seat;
	// 状态
	private player_state state;// 1进入 2已准备 3正在玩 4已结束
	private int isEntryTable;
	private List<PenghzCard> handPais;
	private List<PenghzCard> outPais;
	private List<PenghzCard> peng;
	private List<PenghzCard> chi;
	private List<PenghzCard> pao;
	private List<Integer> passPeng;
	private List<Integer> passChi;
	private List<PenghzCard> zai;
	private List<PenghzCard> ti;
	private int outHuXi;// 需要记下吃的胡息
	private int huxi;
	private int winCount;
	private int lostCount;
	private int lostPoint;
	private int point;
	private int oweCardCount;// 每出4张牌一次 就欠一张牌
	private PenghuziHuLack hu;
	private List<CardTypeHuxi> cardTypes;
	private List<PenghzCard> chouZai;
	private int zaiHuxi;
	private List<Integer> firstPais;//初始手牌
	//0胡1自摸2提3跑
	private int[] actionTotalArr = new int[4];
	
	private volatile boolean autoPlay = false;//托管
	private volatile long lastOperateTime = 0;//最后操作时间
	private volatile long lastCheckTime = 0;//最后检查时间

	private volatile long autoPlayTime = 0;//自动操作时间
    private volatile boolean isCheckAuto = false; //开始托管合计时
    
    private Map<Integer,Integer> pengMap = new HashMap<Integer,Integer>();
    
    
    private List<Integer> disOutPais = new ArrayList<Integer>();
    
    
    private int lianZhuangC;
    private int wufuBaoj=-1;
    private int siShou=0;//死守
    
    
	/**
	 * 打鸟分
	 * 未开始抛分时该值为-1 表示还未进行抛分
	 * 牌局开始前玩家抛出的分数(相当于押注分) 抛出的分数在结算的时候  单方结算的分数=对方抛出的分数+自己抛出的分数+胡的基础分数
	 */
	private int daNiaoPoint = -1;
	
	private boolean hasNoDui;
    

	public PenghuziPlayer() {
		handPais = new ArrayList<>();
		outPais = new ArrayList<>();
		peng = new ArrayList<>();
		passPeng = new ArrayList<>();
		passChi = new ArrayList<>();
		chi = new ArrayList<>();
		zai = new ArrayList<>();
		pao = new ArrayList<>();
		ti = new ArrayList<>();
		chouZai = new ArrayList<>();
		cardTypes = new ArrayList<>();
		firstPais = new ArrayList<>();
		disOutPais.clear();
//		daNiaoPoint = -1;
		
		setWufuBaoj(-1);
		setDaNiaoPoint(-1);
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
			 table.addPlayLog(getSeat(), PenghzDisAction.action_tuoguan + "",(autoPlay?1:0) + "");
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

	public int getSiShou() {
		return siShou;
	}

	public void setSiShou(int siShou) {
		this.siShou = siShou;
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

	public int[] getActionTotalArr() {
		return actionTotalArr;
	}



    /**
	 * 操作
	 * @param index 0胡1自摸2提3跑
	 * @param val
	 */
	public void changeAction(int index, int val) {
		actionTotalArr[index] += val;
		getPlayingTable().changeExtend();
	}
	public List<CardTypeHuxi> getCardTypes() {
		return cardTypes;
	}
	
	public int getLianZhuangC() {
		return lianZhuangC;
	}

	public void setLianZhuangC(int lianZhuangC) {
		this.lianZhuangC = lianZhuangC;
		changeTableInfo();
	}

	/**
	 * 获得初始手牌
	 * @return
	 */
	public List<Integer> getFirstPais() {
		return firstPais;
	}
	
	
	
	/**
	 * 移掉出的牌
	 */
    void removeOutPais(PenghzCard card) {
		outPais.remove(card);
	}

	/**
	 * 摸牌
	 */
	public void moCard(PenghzCard card) {
		this.outPais.add(card);
	}

	public void tiLong(PenghzCard card) {
		this.handPais.add(card);
		compensateCard();
		changeSeat(seat);
	}

	void disCard(int action, List<PenghzCard> disCardList) {
		for (PenghzCard disCard : disCardList) {
			if (!handPais.remove(disCard)) {
				if (!zai.remove(disCard)) {
					if (!peng.remove(disCard)) {
						pao.remove(disCard);
					}
				}
			}
		}

		if (disCardList.size() > 1) {
			PenghzCard card = disCardList.get(1);
			Iterator<CardTypeHuxi> iterator = cardTypes.iterator();
			while (iterator.hasNext()) {
				CardTypeHuxi type = iterator.next();
				if (type.isHasCard(card)) {
					if (type.getAction() == PenghzDisAction.action_zai) {
					    // 去掉栽的息
						changeZaihu(-type.getHux());

					} else {
						changeOutCardHuxi(-type.getHux());

					}
					iterator.remove();
				}

			}
		}

		if (disCardList.size() == 4) {
			oweCardCount--;
		}

		if (action == PenghzDisAction.action_ti) {
			ti.addAll(disCardList);

		} else if (action == PenghzDisAction.action_zai) {
			zai.addAll(disCardList);
		} else if (action == PenghzDisAction.action_chouzai) {
			chouZai.addAll(disCardList);
		} else if (action == PenghzDisAction.action_peng) {
			peng.addAll(disCardList);
		} else if (action == PenghzDisAction.action_chi) {
			chi.addAll(disCardList);
		} else if (action == PenghzDisAction.action_pao) {
			pao.addAll(disCardList);
			// oweCardCount--;
		} else {
			
			outPais.addAll(disCardList);
			int passVal = disCardList.get(0).getVal();
			passChi.add(passVal);
			passPeng.add(passVal);
			addDisOutPais(passVal);
		}


		if (disCardList.size() > 3 && disCardList.size() % 3 == 0) {
			for (int i = 0; i < disCardList.size() / 3; i++) {
				List<PenghzCard> list = disCardList.subList(i * 3, i * 3 + 3);
				addCardType(action, list);
			}

		} else {
			addCardType(action, disCardList);
		}
		changeSeat(seat);
		changeTableInfo();
	}
	
	
	
	public int getPWPTCount(List<CardTypeHuxi> phzHuCards){
		HashSet<Integer> pwpt = new HashSet<Integer>();
		
		for(PenghzCard card : peng){
			int val = card.getVal();
			pwpt.add(val);
		}
		for(PenghzCard card : pao){
			int val = card.getVal();
			pwpt.add(val);
		}
		for(PenghzCard card : zai){
			int val = card.getVal();
			pwpt.add(val);
		}
		for(PenghzCard card : ti){
			int val = card.getVal();
			pwpt.add(val);
		}
		
		for(PenghzCard card : chouZai){
			int val = card.getVal();
			pwpt.add(val);
		}
		
		int add=0;
		if(phzHuCards!=null&&!phzHuCards.isEmpty()){
			for(CardTypeHuxi ct: phzHuCards){
				if(ct.getAction()==2){
					add++;
				}
			}
		}
		
		return pwpt.size()+add;
	}
	
	
	
	public int getHuCardCPZT(int val2,List<CardTypeHuxi> phzHuCards){
		
		if(phzHuCards!=null&&!phzHuCards.isEmpty()){
			for(CardTypeHuxi ct: phzHuCards){
				if(ct.getAction()==2){
					return 1;
				}else {
					 List<Integer> cids = 	ct.getCardIds();
					 for(Integer id: cids){
						 PenghzCard pc =	 PenghzCard.getPaohzCard(id);
						 if(pc!=null && pc.getVal()==val2){
							 return 0;
						 }
					 }
				}
			}
		}
		
		for(PenghzCard card : peng){
			int val = card.getVal();
			if(val2==val){
				return 1;
			}
		}
		for(PenghzCard card : pao){
			int val = card.getVal();
			if(val2==val){
				return 3;
			}
		}
		for(PenghzCard card : zai){
			int val = card.getVal();
			if(val2==val){
				return 2;
			}
		}
		for(PenghzCard card : ti){
			int val = card.getVal();
			if(val2==val){
				return 4;
			}
		}
		
	
		
		
		
		return 0;
	}
	

	public void addCardType(int action, List<PenghzCard> disCardList) {
		int huxi = PenghuziTool.getOutCardHuxi(action, disCardList);
		if (action != 0) {
			CardTypeHuxi type = new CardTypeHuxi();
			type.setAction(action);
			type.setCardIds(PenghuziTool.toPhzCardIds(disCardList));
			type.setHux(huxi);
			cardTypes.add(type);
		}
		if (action == PenghzDisAction.action_zai) {
			changeZaihu(huxi);
		} else {
			changeOutCardHuxi(huxi);

		}
	}

	public boolean isCanExec(int action, List<PenghzCard> cards) {
		if (action == PenghzDisAction.action_pao) {

		}
		return true;
	}

	public List<PenghzCard> getSameCards(PenghzCard card) {
		List<PenghzCard> list = new ArrayList<>();
		List<PenghzCard> zaiList = PenghuziTool.findPhzByVal(zai, card.getVal());
		list.addAll(zaiList);
		List<PenghzCard> paoList = PenghuziTool.findPhzByVal(handPais, card.getVal());
		list.addAll(paoList);
		List<PenghzCard> pengList = PenghuziTool.findPhzByVal(peng, card.getVal());
		list.addAll(pengList);
		return list;
	}

	/**
	 * 是否需要出牌
	 * 
	 * @return
	 */
	public boolean isNeedDisCard(int action) {
		// boolean isFrist
//		int ct = isFristDisCard() ? handPais.size() % 3 : handPais.size()+1 % 3;
//        if (!ti.isEmpty() || !pao.isEmpty()) {
//            return ct == 2;
//        } else {
//            return ct == 0;
//        }
		return oweCardCount >= -1;
	}

	/**
	 * 
	 */
	public void compensateCard() {
		oweCardCount++;
		changeTableInfo();
	}
	
	public void pass(int action, int val) {
		pass(action, val, false);
	}

	public void pass(int action, int val, boolean addPassChi) {
		if (action == PenghzDisAction.action_chi) {
			if (addPassChi) {				
				passChi.add(val);
			}
		} else if (action == PenghzDisAction.action_peng) {
			passPeng.add(val);
		} else {
			return;
		}
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

			this.outHuXi = StringUtil.getIntValue(values, i++);
			this.huxi = StringUtil.getIntValue(values, i++);
			this.winCount = StringUtil.getIntValue(values, i++);
			this.lostCount = StringUtil.getIntValue(values, i++);
			this.lostPoint = StringUtil.getIntValue(values, i++);
			this.point = StringUtil.getIntValue(values, i++);
			setTotalPoint(StringUtil.getIntValue(values, i++));
			this.oweCardCount = StringUtil.getIntValue(values, i++);
			setMaxPoint(StringUtil.getIntValue(values, i++));
			this.zaiHuxi = StringUtil.getIntValue(values, i++);
			String firstPaisStr = StringUtil.getValue(values, i++);
			if (!StringUtils.isBlank(firstPaisStr)) {
				firstPais = StringUtil.explodeToIntList(firstPaisStr, "_");
			}
			String actionTotalArrStr = StringUtil.getValue(values, i++);
			if (!StringUtils.isBlank(actionTotalArrStr)) {
				this.actionTotalArr = StringUtil.explodeToIntArray(actionTotalArrStr, "_");
			}
			
			String pengStr = StringUtil.getValue(values, i++);
			if (!StringUtils.isBlank(pengStr)) {
				if(!pengStr.equals("null")&&!pengStr.contains("_")){
					pengMap.put(Integer.valueOf(pengStr), StringUtil.getIntValue(values, i++));
				}else{
					pengMap = DataMapUtil.implode(pengStr,"_","#");
				}
			}
			this.lianZhuangC = StringUtil.getIntValue(values, i++);
			this.wufuBaoj = StringUtil.getIntValue(values, i++);
			
			 int pwp =  getPWPTCount(null);
			if(getHandPais().size()!=2||pwp!=4||!PenghuziTool.isDuiZi(getHandPais())){
				wufuBaoj =-1;
			}
			
			daNiaoPoint = StringUtil.getIntValue(values, i++);
			hasNoDui = StringUtil.getIntValue(values, i++)==1?true:false;
			
			
			String disOutStr = StringUtil.getValue(values, i++);
			if (!StringUtils.isBlank(disOutStr)) {
				disOutPais = StringUtil.explodeToIntList(disOutStr, "_");
			}
			
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
		peng.clear();
		chi.clear();
		pao.clear();
		zai.clear();
		chouZai.clear();
		ti.clear();
		passChi.clear();
		disOutPais.clear();
		passPeng.clear();
		cardTypes.clear();
		chouZai.clear();
		setZaiHuxi(0);
		setHu(null);
		setOutHuxi(0);
		setHuxi(0);
		setOweCardCount(0);
		setWinCount(0);
		setLostCount(0);
		setPoint(0);
		setLostPoint(0);
		setTotalPoint(0);
		setMaxPoint(0);
		setSeat(0);
		if (!isCompetition) {
			setPlayingTableId(0);
		}
		pengMap.clear();
		actionTotalArr = new int[4];

		if(table.isAutoPlay() && autoPlay){
		    setAutoPlay(false,table);
        }
		setLianZhuangC(0);
		saveBaseInfo();
		setLastCheckTime(0);
		setSiShou(0);
		setWufuBaoj(-1);
		setDaNiaoPoint(-1);
		setHasNoDui(false);
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

	boolean isCanDisCard(List<Integer> disCards, PenghzCard nowDisCard) {
		if (disCards != null) {
			if (nowDisCard != null && disCards.contains(nowDisCard.getId())) {
				List<Integer> copy = new ArrayList<>(disCards);
				// 排除掉在桌面上已经出的牌
				copy.remove((Integer) nowDisCard.getId());
				if (!getHandPais().containsAll(copy)) {
					writeErrMsg("找不到牌:" + disCards);
					return false;
				}
			} else {
				if (!getHandPais().containsAll(disCards)) {
					writeErrMsg("找不到牌:" + disCards);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 不加入能跑的牌是否能胡
	 */
	public PenghuziHuLack checkPaoHu(PenghzCard card, boolean isSelfMo, boolean canDiHu) {
//		int addhuxi = 0;
		
		//boolean isNeedDui = isNeedDui();
		List<PenghzCard> handCopy = new ArrayList<>(handPais);
		PenghuziHuLack lack = checkPaoHu2(card, isSelfMo, canDiHu, handCopy,false);
		return lack;
	}

	public PenghuziHuLack checkPaoHu2(PenghzCard card, boolean isSelfMo, boolean canDiHu, 
			List<PenghzCard> handCopy,boolean tingpai) {
		int action = 0;
		if (card != null) {
			PenghuziTable table = getPlayingTable(PenghuziTable.class);
			boolean isMoFlag = table.isMoFlag();
			if(tingpai){
				isMoFlag = true;
			}
			List<PenghzCard> sameList = PenghuziTool.getSameCards(handPais, card);
			if (isSelfMo) {
				sameList.remove(card);
			}
			if (sameList.size() >= 3) {
				if (isSelfMo) {
					action = PenghzDisAction.action_ti;
				} else {
					action = PenghzDisAction.action_pao;
				}
			}
			List<Integer> zaiVals = PenghuziTool.toPhzCardVals(zai, true);
			if (isMoFlag && !peng.contains(card) && PenghuziTool.isHasCardVal(peng, card.getVal())) {
				// 检查是否碰过的牌 跑过的牌能跑的话必须要是摸的牌
				//isNeedDui = true;
				action = PenghzDisAction.action_pao;
			} else if (!zai.contains(card) && zaiVals.contains(card.getVal())) {
				// 如果是栽过的牌别人打的或者摸的能提或者跑
				// /////////////////////////////////////////////////////
			//	isNeedDui = true;
				if (isSelfMo) {
					// 如果是栽过的牌自己再摸一张上来就是提
					// 大的12 小的9 栽大的6 小的3
					action = PenghzDisAction.action_ti;
				
				} else {
					// 别人打的或摸的就是跑
					action = PenghzDisAction.action_pao;
				
				}

			} else if (isMoFlag || canDiHu) {
				// 在手上的牌
				List<PenghzCard> sameCard = PenghuziTool.getSameCards(handCopy, card);
				if (sameCard.size() >= 3) {
					handCopy.removeAll(sameCard);
				}
			}
		}
//		int totalHuxi = outHuXi + zaiHuxi + addhuxi;
		if(getSiShou()==1)
			return new PenghuziHuLack(0);
		PenghuziTable table=getPlayingTable(PenghuziTable.class);
		PenghuziHuLack lack = PenghuziTool.isHuNew(PenghuziTool.getPaohuziHandCardBean(handCopy), null, isSelfMo, 0, true, true,table.getFirstCard());
		if (lack.isHu()) {
			if (action != 0) {
				lack.setPaohuAction(action);
				List<PenghzCard> paohuList = new ArrayList<>();
				paohuList.add(card);
				lack.setPaohuList(paohuList);
			}else{
				lack.setHu(false);
			}
				if(action==PenghzDisAction.action_pao)
					table.setPaoHu(1);
		}
		return lack;
	}

	private boolean isNeedDui() {
		return !ti.isEmpty() || !pao.isEmpty();
	}

	public PenghuziHuLack checkHu(PenghzCard card, boolean isSelfMo) {
		if(getSiShou()==1)
			return new PenghuziHuLack(0);
		List<PenghzCard> handCopy = new ArrayList<>(handPais);
		PenghuziHuLack lack = checkHuCard2(card, isSelfMo, handCopy,false);
		return lack;
	}

	public PenghuziHuLack checkHuCard2(PenghzCard card, boolean isSelfMo, List<PenghzCard> handCopy,boolean isTing) {
		boolean isPaoHu = true;
		if (card != null && !handCopy.contains(card)) {
			handCopy.add(card);
			isPaoHu = false;
		}
		PenghuziTable table = getPlayingTable(PenghuziTable.class);
		PenghuziHuLack lack = PenghuziTool.isHuNew(PenghuziTool.getPaohuziHandCardBean(handCopy), card, isSelfMo, outHuXi, isNeedDui(), isPaoHu,isTing?false:table.getFirstCard());
		
		if(lack.isHu()){
			int pwp =  getPWPTCount(lack.getPhzHuCards());
	        if(getWufuBaoj()==1&&pwp!=5){
	        	int val = getHandPhzs().get(0).getVal();
	        	if(card!=null && val!=card.getVal()){
	        		lack.setHu(false);
	        	}
	        } 
	        
	        if(lack.getDaHu()==PengHZMingTang.XIAO_QI_DUI){
	        	if((isTing&&!table.getFirstCard())||!table.getFirstCard()){
	        		lack.setHu(false);
	        	}
	        }
	        
	        List<Integer> disOutCardVals  = getDisOutPais();
	        if(disOutCardVals!=null&&!disOutCardVals.isEmpty()&&card!=null){
	        	for(Integer pc : disOutCardVals){
	        		if(pc==card.getVal()){
	        			lack.setHu(false);
	        		}
	        	}
	        }
		}
		
		if (lack.isHu()) {
			int dahu = 0;
			if(card==null&&table!=null&&table.getNowDisCardIds()!=null&&!table.getNowDisCardIds().isEmpty()){
				card = table.getNowDisCardIds().get(0);
			}
			if (card != null) {
				int pwpCount = getPWPTCount(lack.getPhzHuCards());
				
				int huC = getHuCardCPZT(card.getVal(),lack.getPhzHuCards());
				if (huC > 0) {
					table.logGameAcMsg(this, 0, 10, "pwpCount|"+pwpCount+"|baoJingS"+getWufuBaoj());
					if (huC == 1) {
						dahu = PengHZMingTang.PENG_HU;
						if (pwpCount >= 3) {
							dahu = pwpCount - 2;
							if(getWufuBaoj()!=1&&dahu==PengHZMingTang.WU_FU){
								dahu = PengHZMingTang.PENG_HU;
							}
						}
						
					} else if (huC == 2) {
						dahu = PengHZMingTang.SAO_HU;
						if (pwpCount >= 3) {
							if (pwpCount == 3) {
								dahu = PengHZMingTang.SAO_SAN_DA;
							} else if (pwpCount == 4) {
								dahu = PengHZMingTang.SAO_SI_QING;
							} else {
								if(getWufuBaoj()==1){
									dahu = PengHZMingTang.WU_FU;
								}
							}
						}
					} else if (huC == 3) {
						dahu = PengHZMingTang.PAO_HU;

					} else if (huC == 4) {
						dahu = PengHZMingTang.TI_LONG_HU;
					}

				}
				
			}
			
		
			
			if(table!=null){
				
				if(lack.getDaHu()==PengHZMingTang.XIAO_QI_DUI){
					dahu = PengHZMingTang.XIAO_QI_DUI;
				}else{
					if(table.getFirstCard()){
						dahu = PengHZMingTang.TIAN_HU;
					}
				}
				
				
			}
			
			lack.setDaHu(dahu);
			
		}
		return lack;
	}

	/**
	 * 得到可以操作的牌 4张和3张是不能操作的
	 */
    PenghuziHandCard getPaohuziHandCard() {
		return PenghuziTool.getPaohuziHandCardBean(handPais);
	}

	/**
	 * 拿到可以操作的牌
	 * 
	 * @return
	 */
	public List<PenghzCard> getOperateCards() {
		List<PenghzCard> copy = new ArrayList<>(handPais);
		Map<Integer, Integer> valMap = PenghuziTool.toPhzValMap(copy);
		for (Entry<Integer, Integer> entry : valMap.entrySet()) {
			if (entry.getValue() >= 3) {
				PenghuziTool.removePhzByVal(copy, entry.getKey());
			}
		}
		return copy;

	}

	public PenghuziCheckCardBean checkCard(PenghzCard card, boolean isSelfMo, boolean isFirstCard) {
		return checkCard(card, isSelfMo, false, false, isFirstCard, false);
	}

	public PenghuziCheckCardBean checkCard(PenghzCard card, boolean isSelfMo, boolean isBegin, boolean isFirstCard) {
		return checkCard(card, isSelfMo, false, isBegin, isFirstCard, false);
	}

	public PenghuziCheckCardBean checkPaoHu() {
		PenghuziCheckCardBean check = new PenghuziCheckCardBean();
		check.setSeat(seat);
		PenghuziHuLack lack = checkHu(null, false);
		if (lack.isHu()) {
			check.setLack(lack);
			check.setHu(true);
			PenghuziTable table = getPlayingTable(PenghuziTable.class);
			if(table!=null&&table.getQiangzhiHu()==1){
				check.setAuto(PenghzDisAction.action_hu, null);
			}
		}
		check.buildActionList();
		return check;
	}

	/**
	 * 检查提
	 * 
	 * @return
	 */
	public PenghuziCheckCardBean checkTi() {
		PenghuziCheckCardBean check = new PenghuziCheckCardBean();
		check.setSeat(seat);
		PenghuziHandCard cardBean = getPaohuziHandCard();
		PenghzCardIndexArr valArr = cardBean.getIndexArr();
		PenghuziIndex index3 = valArr.getPaohzCardIndex(3);
		if (index3 != null) {
			check.setAuto(PenghzDisAction.action_ti, index3.getPaohzList());
			check.setTi(true);
		}
		check.buildActionList();
		return check;
	}

	/**
	 * 已经提，跑，栽固定了的出了的牌值
	 * 
	 * @return
	 */
	public List<Integer> getFixedOutVals() {
		// 提，跑，栽
		List<PenghzCard> phzs = new ArrayList<>();
		phzs.addAll(chouZai);
		phzs.addAll(zai);
		phzs.addAll(pao);
		phzs.addAll(ti);
		return PenghuziTool.toPhzRepeatVals(phzs);
	}

	public boolean canFangZhao(PenghzCard card) {

		if (!zai.contains(card)) {
			List<Integer> zaiVals = PenghuziTool.toPhzCardVals(zai, true);
			if (zaiVals.contains(card.getVal())) {
				return true;
			}
		}

		return false;
	}

	/**
	 * 检查牌
	 * 
	 * @param card
	 *            出的牌或者是摸的牌
	 * @param isSelfMo
	 *            是否是自己摸的牌
	 */
	public PenghuziCheckCardBean checkCard(PenghzCard card, boolean isSelfMo, boolean isPassHu, boolean isBegin, boolean isFirstCard, boolean isPass) {
		List<PenghzCard> copy = new ArrayList<>(handPais);
		PenghuziCheckCardBean check = new PenghuziCheckCardBean();
		check.setSeat(seat);
		check.setDisCard(card);

		PenghuziHandCard cardBean = getPaohuziHandCard();
		boolean isCanChiPeng = true;
		int operateCount = cardBean.getOperateCards().size();
		if (operateCount < 4 && oweCardCount >= -1) {
			// 必须要出牌但是可以可操作的牌已经不够了
			isCanChiPeng = false;
		}

		// ///////////////////////////////////////////////////////////////
		PenghzCardIndexArr valArr = cardBean.getIndexArr();
		PenghuziIndex index3 = valArr.getPaohzCardIndex(3);
		if (isSelfMo) {
			// 检查提
			boolean isMoTi = false;
			if (index3 != null && card != null) {
				for (int val : index3.getPaohzValMap().keySet()) {
					if (val == card.getVal()) {
						isMoTi = true;
						break;
					}
				}

			}
			
			PenghuziIndex index2 = valArr.getPaohzCardIndex(2);
			if (index3 != null && (isBegin || isMoTi)) {
				check.setAuto(PenghzDisAction.action_ti, index3.getPaohzList());
				check.setTi(true);
			}else if(index2 != null){
				 List<PenghzCard> cards = index2.getPaohzList();
				 List<PenghzCard> weiC = new ArrayList<>(); 
				 for(PenghzCard ca : cards){
					 if(weiC.isEmpty()){
						 weiC.add(ca);
					 }else {
						 if(weiC.get(0).getVal()==ca.getVal()){
							 weiC.add(ca);
						 }
						 
						 if(weiC.size()==3){
							 break;
						 }
						 
					 }
				 }
				
				check.setAuto(PenghzDisAction.action_zai,weiC);
				check.setZai(true);
			}
			
			
		}
		PenghuziTable table = getPlayingTable(PenghuziTable.class);
		boolean isMoFlag = table.isMoFlag();
		if (card != null) {
			List<Integer> fixdOutVals = getFixedOutVals();
			// 有没有一样的牌
			List<PenghzCard> sameList = PenghuziTool.getSameCards(copy, card);
			if (isSelfMo) {
				sameList.remove(card);
			}
			if (sameList != null) {
				if (sameList.size() == 3) {
					if (isSelfMo) {
						// !!!!不可能出现这种情况
						// 检查栽 如果从墩上摸上来的那张牌 自己可以偎时，这张牌是不能让其他人看到的
						check.setAuto(PenghzDisAction.action_ti, sameList);
						check.setTi(true);

					} else {
						// 在手里面不管是摸的还是出的牌都能跑
						// 检查跑
						check.setAuto(PenghzDisAction.action_pao, sameList);
						check.setPao(true);
					}

				} else if (sameList.size() == 2) {
					if (isSelfMo) {
						// 玩家手中有一对牌，当自已从墩上摸起一只相同的牌，玩家必须将手中的一对连同摸起的牌放置于桌面，且不能明示给其它玩家，称为栽/偎
						// 看看是否是臭栽
						if (passPeng.contains(card.getVal())) {
							check.setAuto(PenghzDisAction.action_chouzai, sameList);
							check.setChouZai(true);
						} else {
							check.setAuto(PenghzDisAction.action_zai, sameList);
							check.setZai(true);
						}

					} else {
						if (isCanChiPeng && !passPeng.contains(card.getVal()) ) {
							// 检查碰
							check.setPeng(true);
						}

					}
				}
			}
			// 去掉4张和3张
			PenghuziIndex index2 = valArr.getPaohzCardIndex(2);
			if (index3 != null) {
				copy.removeAll(index3.getPaohzList());
			}
			if (index2 != null) {
				copy.removeAll(index2.getPaohzList());
			}
			// ////////////////////////////////////
			// 检查是否碰过的牌 跑过的牌能跑的话必须要是摸的牌
			if (isMoFlag && PenghuziTool.isHasCardVal(peng, card.getVal())) {
				check.setAuto(PenghzDisAction.action_pao, new ArrayList<>(Arrays.asList(card)));
				check.setPao(true);

			}

			// /////////////////////////////////////////////////////
			if (!zai.contains(card)) {
				List<Integer> zaiVals = PenghuziTool.toPhzCardVals(zai, true);
				if (zaiVals.contains(card.getVal())) {
					if (isSelfMo) {
						// 如果是栽过的牌自己再摸一张上来就是提
						check.setAuto(PenghzDisAction.action_ti, new ArrayList<>(Arrays.asList(card)));
						check.setTi(true);
					} else {
						// 别人打的或摸的就是跑
						check.setAuto(PenghzDisAction.action_pao, new ArrayList<>(Arrays.asList(card)));
						check.setPao(true);
					}

				}
			}

			// //////////////////////////////////////////////////// 

			// 是否能检测能吃牌
			boolean isCheckChi = false;

			// 不是已经提跑栽过的牌
			if (!fixdOutVals.contains(card.getVal())) {
				if (isMoFlag && table.getMoSeat() == seat) {
					// 如果这张牌是摸的,摸上来的人也能检测
					isCheckChi = true;
				}
				if (!isCheckChi && table.calcNextSeat(table.getDisCardSeat()) == seat) {
					// 不管怎么样，摸牌打出的下家是一定要检测的
					isCheckChi = true;
				}
			}


			// 必须在不能跑的情况下
			if (isCanChiPeng && !check.isTi() && !check.isPao() && isCheckChi && !isPass) {
				
				PenghuziPlayer frontPlayer = (PenghuziPlayer) table.getPlayerBySeat(table.calcFrontSeat(seat));
				List<PenghzCard> frontOutPaisCard = frontPlayer.getOutPaisCard();
				List<Integer> frontOutPais = PenghuziTool.toPhzCardVals(frontOutPaisCard, true);
				List<Integer> selfOutVals = PenghuziTool.toPhzCardVals(outPais, true);
				// System.out.println("前面过的牌:" + frontOutPais + "自己过的牌:" + selfOutVals + ",名字->" + name);
				
				List<Integer> passChiList = new ArrayList<>();
				if (isMoFlag) {
					if (isSelfMo && !selfOutVals.isEmpty()) {// 自己摸的牌
						selfOutVals.remove((int) (selfOutVals.size() - 1));
					} else {// 别人摸的牌
						frontOutPais.remove((int) (frontOutPais.size() - 1));
					}
				} else {// 出的牌
					frontOutPais.remove((int) (frontOutPais.size() - 1));
					if(!frontOutPais.isEmpty()){
						List<Integer> pengIds= new ArrayList<Integer>();
						//被碰掉的牌删除掉
						 removePengCardVal(table, frontOutPais, pengIds);
						frontOutPais.removeAll(pengIds);
					}
				}
				passChiList.addAll(frontOutPais);
				passChiList.addAll(selfOutVals);
				// System.out.println(name + "所有过掉的牌:" + passChiList);
				if (!passChiList.contains(card.getVal()) && !passChi.contains(card.getVal())) {
					List<PenghzCard> chiList = getChiList(card, null);
					if (chiList != null && !chiList.isEmpty()) {
						check.setChi(true);
					} else{
						// 不能吃后就再也不能吃了
						if (!check.isPeng() && !check.isZai() && !check.isChouZai()) {
							// pass(PenghzDisAction.action_chi, card.getVal());
							passChi.add(card.getVal());
						}
					}
				}
				
			}

			if (isMoFlag) {
				if (!isPassHu&&getSiShou()==0) {
					// if (!check.isZai() && !check.isTi()) {
					if (!check.isChouZai() && !check.isZai() && !check.isTi()) {
						// 检查胡
						PenghuziHuLack lack = checkHu(card, isSelfMo);
						if (lack.isHu()) {
							check.setHu(true);
						}
					}

					// 如果是摸出来的牌并且能跑，检测手上的牌是否能胡
					if ((check.isPao())) {
						PenghuziHuLack lack = checkPaoHu(card, isSelfMo, isFirstCard);
						if (lack.isHu()) {
							// 能跑胡也能平胡，显示胡但是不显示跑，在点胡的按钮的时候再去确定是否能先跑再胡
							check.setHu(true);
						}
					}

					if (check.isHu()) {
						check.setPao(false);
					}
				}
			} else  {
				if (!isPassHu) {
					PenghuziHuLack lack = checkHu(card, false);
					if (lack.isHu()) {
						check.setHu(true);
						if (check.isPao()) {
							check.setPao(false);
						}
					} else {
						lack = checkPaoHu(card, false, true);
						if (lack.isHu()) {
							check.setHu(true);
							check.setPao(false);
						}
					}
				}
			}

			if (check.isPeng()) {
				List<PenghzCard> sameCards = getPengList(card, null);
				if (sameCards == null) {
					System.out.println("check peng-->" + card);

				}
			}

			// }

		} else {
			// if (!isPassHu && !check.isZai() && !check.isTi()) {
			if (!isPassHu && !check.isChouZai() && !check.isZai() && !check.isTi()) {
				PenghuziHuLack lack = checkHu(card, isSelfMo);
				check.setHu(lack.isHu());
			}

		}
		
		
		
		if(check.isHu()&&table.getQiangzhiHu()==1){
			check.setAuto(PenghzDisAction.action_hu, null);
			check.setHu(true);
		}
		
		

		check.buildActionList();
		return check;
	}

	private void removePengCardVal(PenghuziTable table, List<Integer> frontOutPais, List<Integer> pengIds) {
		for(Integer pcard: frontOutPais){
				for (PenghuziPlayer robotPlayer : table.getSeatMap().values()) { 
//				 if(robotPlayer.getSeat() == seat){
//					 continue;
//				 }
					Map<Integer, Integer> map = robotPlayer.getPengMap();
					if(map.isEmpty()){
						continue;
					}
					Integer seatD = map.get(pcard);
				// int val2= card.getVal();
				 if(seatD!=null){
					 pengIds.add(pcard);
					 break;
				 }
			}
		 }
	}

	List<PenghzCard> getPengList(PenghzCard card, List<PenghzCard> cardList) {
		PenghuziHandCard handCard = getPaohuziHandCard();
		if (!handCard.isCanoperateCard(card)) {
			return null;
		}
		if (cardList != null) {
			// 吃牌不符合规则
			List<PenghzCard> gameList = PenghuziTool.getSameCards(cardList, card);
			if (gameList == null || gameList.isEmpty()) {
				return null;
			}
			if (gameList.size() != cardList.size()) {
				return null;
			}
		} else {
			cardList = PenghuziTool.getSameCards(handCard.getOperateCards(), card);
			if (cardList == null || cardList.size() != 2) {
				return null;
			}
		}
		return cardList;

	}

	public List<PenghzCard> getChiList(PenghzCard card, List<PenghzCard> cardList) {
		PenghuziHandCard handCard = getPaohuziHandCard();
		if (!handCard.isCanoperateCard(card)) {
			return null;
		}
		if (cardList != null) {
			List<PenghzCard> sameList = PenghuziTool.findPhzByVal(cardList, card.getVal());
			List<PenghzCard> handSameList = PenghuziTool.findPhzByVal(handCard.getOperateCards(), card.getVal());
			if (!handSameList.isEmpty() && !sameList.containsAll(handSameList)) {
				return null;
			}
			List<PenghzCard> copy = new ArrayList<>(cardList);
			// 吃牌不符合规则
			if (copy.contains(card)) {
				copy.remove(card);
			}

			List<PenghzCard> chiList = PenghuziTool.checkChi(copy, card);
			if (chiList == null || chiList.isEmpty()) {
				return null;
			}
			// 找出有没有要吃的相同的牌
			if (sameList == null || sameList.isEmpty()) {
				// 如果没有其他相同的牌 直接吃
				return cardList;
			} else {
				// 有相同的牌
				List<PenghzCard> allChi = new ArrayList<>();
				allChi.addAll(chiList);
				//
				copy.removeAll(chiList);
				for (PenghzCard sameCard : sameList) {
					if (!copy.contains(sameCard)) {
						continue;
					}
					// 相同的牌还能不能继续吃
					List<PenghzCard> samechiList = PenghuziTool.checkChi(copy, sameCard);
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
			cardList = PenghuziTool.checkChi(handCard.getOperateCards(), card);
			if (cardList == null || cardList.isEmpty()) {
				return null;
			}
		}

		if (cardList != null) {
			if (cardList.contains(card)) {
				cardList.remove(card);
			}
			handCard.getOperateCards().removeAll(cardList);
		}

		List<PenghzCard> sameList = PenghuziTool.findCountByVal(handCard.getOperateCards(), card, true);
		// 找出有没有要吃的相同的牌
		if (sameList == null || sameList.isEmpty()) {
			// 如果没有其他相同的牌 直接吃
			return cardList;
		} else {
			// 有相同的牌
			List<PenghzCard> allChi = new ArrayList<>();
			allChi.addAll(cardList);
			//
			handCard.getOperateCards().removeAll(cardList);
			for (PenghzCard sameCard : sameList) {
				if (!handCard.getOperateCards().contains(sameCard)) {
					continue;
				}
				// 相同的牌还能不能继续吃
				List<PenghzCard> samechiList = PenghuziTool.checkChi(handCard.getOperateCards(), sameCard);
				if (samechiList == null || samechiList.isEmpty()) {
					// 如果不能吃 则这个牌不能吃
					return null;
				}

				// 添加相同的牌 的吃
				handCard.getOperateCards().removeAll(samechiList);
				samechiList.add(0, sameCard);
				allChi.addAll(samechiList);
			}
			return allChi;
		}

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
		setZaiHuxi(0);
		cardTypes.clear();
		handPais.clear();
		outPais.clear();
		peng.clear();
		chi.clear();
		pao.clear();
		zai.clear();
		chouZai.clear();
		ti.clear();
		passChi.clear();
		disOutPais.clear();
		passPeng.clear();
		setOutHuxi(0);
		setHuxi(0);
		setOweCardCount(0);
		getPlayingTable().changeExtend();
		getPlayingTable().changeCards(seat);
		changeState(player_state.entry);
		setSiShou(0);
		setWufuBaoj(-1);
		setDaNiaoPoint(-1);
		setHasNoDui(false);
		pengMap.clear();
	}

	public List<PenghzCard> getHandPhzs() {
		return handPais;
	}

	@Override
	public List<Integer> getHandPais() {
		return PenghuziTool.toPhzCardIds(handPais);
	}

	public List<Integer> getOutPais() {
		return PenghuziTool.toPhzCardIds(outPais);
	}

	public List<PenghzCard> getOutPaisCard() {
		return outPais;
	}

	/**
	 * 出牌
	 * 
	 * @return
	 */
	public String buildOutPaiStr() {
		StringBuilder sb = new StringBuilder();
		List<Integer> outPais = getOutPais();
		sb.append(StringUtil.implode(outPais)).append(";");
		for (CardTypeHuxi huxi : cardTypes) {
			sb.append(huxi.toStr()).append(";");
		}

		return sb.toString();
	}

	/**
	 * 手牌
	 * 
	 * @return
	 */
	public String buildHandPaiStr() {
		StringBuilder sb = new StringBuilder();
		List<Integer> outPais = getHandPais();
		List<Integer> tiPais = PenghuziTool.toPhzCardIds(ti);
		List<Integer> paoPais = PenghuziTool.toPhzCardIds(pao);
		List<Integer> zaiPais = PenghuziTool.toPhzCardIds(zai);
		List<Integer> pengPais = PenghuziTool.toPhzCardIds(peng);
		List<Integer> chiPais = PenghuziTool.toPhzCardIds(chi);
		List<Integer> chouZaiPais = PenghuziTool.toPhzCardIds(chouZai);

		sb.append(StringUtil.implode(outPais)).append(";");
		sb.append(StringUtil.implode(tiPais)).append(";");
		sb.append(StringUtil.implode(paoPais)).append(";");
		sb.append(StringUtil.implode(zaiPais)).append(";");
		sb.append(StringUtil.implode(pengPais)).append(";");
		sb.append(StringUtil.implode(chiPais)).append(";");
		sb.append(StringUtil.implode(chouZaiPais)).append(";");
		return sb.toString();
	}

	@Override
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
		sb.append(StringUtil.implode(passPeng, "_")).append(",");
		sb.append(StringUtil.implode(passChi, "_")).append(",");
		sb.append(outHuXi).append(",");
		sb.append(huxi).append(",");
		sb.append(winCount).append(",");
		sb.append(lostCount).append(",");
		sb.append(lostPoint).append(",");
		sb.append(point).append(",");
		sb.append(getTotalPoint()).append(",");
		sb.append(oweCardCount).append(",");
		sb.append(getMaxPoint()).append(",");
		sb.append(zaiHuxi).append(",");
		sb.append(StringUtil.implode(firstPais, "_")).append(",");
		sb.append(StringUtil.implode(actionTotalArr, "_")).append(",");
		sb.append(DataMapUtil.explode(pengMap,"_","#")).append(",");
		
		sb.append(lianZhuangC).append(",");
		sb.append(wufuBaoj).append(",");
		sb.append(daNiaoPoint).append(",");
		if(hasNoDui){
			sb.append(1).append(",");
		}else {
			sb.append(0).append(",");
		}
		
		sb.append(StringUtil.implode(disOutPais, "_")).append(",");
		return sb.toString();
	}

	/**
	 * 单局详情
	 */
	public ClosingPhzPlayerInfoRes.Builder bulidOneClosingPlayerInfoRes(boolean isOver) {
		ClosingPhzPlayerInfoRes.Builder res = ClosingPhzPlayerInfoRes.newBuilder();
		res.setUserId(userId + "");
		res.addAllCards(getHandPais());
		res.setName(name);
		res.setPoint(point);
		res.setTotalPoint( getTotalPoint());
		res.setSeat(seat);
		PenghuziTable table = getPlayingTable(PenghuziTable.class);
		if(table!=null){
			res.addAllMoldCards(buildPhzCards(table,userId));
		}
//		res.setBopiPoint(bopiPoint);
		if (!StringUtils.isBlank(getHeadimgurl())) {
			res.setIcon(getHeadimgurl());
		} else {
			res.setIcon("");
		}
		res.setBopiPoint(getDaNiaoPoint());
		res.setGoldFlag(getGoldResult());
		res.setSex(sex);
		return res;
	}

	public int randNumber(int number) {
		int ret = 0;
		if (number > 0) {
			ret = (number + 5) / 10 * 10;
		} else if (number < 0) {
			ret = (number - 5) / 10 * 10;
		}

		return ret;
	}

	/**
	 * 总局详情
	 * 
	 * @return
	 */
	public ClosingPhzPlayerInfoRes.Builder bulidTotalClosingPlayerInfoRes( boolean isOver) {
		ClosingPhzPlayerInfoRes.Builder res = bulidOneClosingPlayerInfoRes(isOver);
		res.setLostCount(lostCount);
		res.setWinCount(winCount);
		res.setMaxPoint(getMaxPoint());
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

		PenghuziTable table = getPlayingTable(PenghuziTable.class);

		if (table == null) {
			LogUtil.e("userId="+this.userId+"table null-->" + getPlayingTableId());
			return null;
		}

		res.setPoint(getTotalPoint());
		// 当前出的牌
		res.addAllOutedIds(getOutPais());

		res.addExt(getPoint());// 0
		res.addExt((table.getMasterId() == this.userId) ? 1 : 0);// 1

        res.addExt(getLianZhuangC());// 2
//        res.addExt(0);// 4
//		res.addExt(table.isGoldRoom()?(int)loadAllGolds():0);// 5
        res.addExt(isAutoPlay() ? 1 : 0);// 3
        res.addExt(getWufuBaoj());// 4
        
        res.addExt(daNiaoPoint);// 飘分5
        res.addExt(hasNoDui?1:0);// 无对6
        
        res.addExt(getAutoPlayCheckedTime() > table.getAutoTimeOut() ? 1 : 0);//7
		res.setName(name);
		res.setSeat(seat);
		res.setSex(sex);
		
		List<Integer> pengIds = new ArrayList<Integer>();
		 
		
		
		
		 if(!outPais.isEmpty()){
				 for(PenghzCard card: outPais){
					for (PenghuziPlayer robotPlayer : table.getSeatMap().values()) { 
					 if(robotPlayer.getSeat() == seat){
						 continue;
					 }
						Map<Integer, Integer> map = robotPlayer.getPengMap();
						if(map.isEmpty()){
							continue;
						}
						Integer seatD = map.get(card.getVal());
					// int val2= card.getVal();
					 if(seatD!=null){
						 pengIds.add(card.getId());
						 break;
					 }
				}
			 }
		 }
		 
		res.addAllAngangIds(pengIds);
		res.addAllMoldCards(buildPhzCards(table,lookUid));

		List<PenghzCard> nowDisCard = table.getNowDisCardIds();
		if (table.getDisCardSeat() == seat) {
			if (nowDisCard != null && !nowDisCard.isEmpty()) {
				int selfMo = 0;
				if (table.isSelfMo(this)) {
					selfMo = 1;
				}
				res.addOutCardIds(selfMo);
				PenghzCard zaiCard = table.getZaiCard();
				PenghzCard beremoveCard = table.getBeRemoveCard();
				if (zaiCard != null && nowDisCard.contains(zaiCard)) {
					// 如果栽了牌
				} else if (beremoveCard != null && nowDisCard.contains(beremoveCard)) {
					// 被移掉的牌
				} else {
					res.addAllOutCardIds(PenghuziTool.toPhzCardIds(nowDisCard));

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
			res.setStatus(PenghuziConstant.state_player_ready);
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

	public boolean isAlreadyMo() {
		int count = handPais.size();
		count += outPais.size();
		count += ti.size();
		count += chi.size();
		count += peng.size();
		count += zai.size();
		count += chouZai.size();
		return count == 21;

	}

	@Override
	public void initPais(String handPai, String outPai) {
		if (!StringUtils.isBlank(outPai)) {
			String[] values = outPai.split(";");
			int i = -1;

			for (String value : values) {
				i++;
				if (i == 0) {
					this.outPais = PenghuziTool.explodePhz(value, ",");
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
			String tiPaiStr = StringUtil.getValue(values, i++);
			String paoPaiStr = StringUtil.getValue(values, i++);
			String zaiPaiStr = StringUtil.getValue(values, i++);
			String pengPaiStr = StringUtil.getValue(values, i++);
			String chiPaiStr = StringUtil.getValue(values, i++);
			String chouZaiPaiStr = StringUtil.getValue(values, i++);
			this.handPais = PenghuziTool.explodePhz(outPaiStr, ",");
			this.ti = PenghuziTool.explodePhz(tiPaiStr, ",");
			this.pao = PenghuziTool.explodePhz(paoPaiStr, ",");
			this.zai = PenghuziTool.explodePhz(zaiPaiStr, ",");
			this.peng = PenghuziTool.explodePhz(pengPaiStr, ",");
			this.chi = PenghuziTool.explodePhz(chiPaiStr, ",");
			this.chouZai = PenghuziTool.explodePhz(chouZaiPaiStr, ",");

		}
	}

	@Override
	public void endCompetition1() {

	}

	public void dealHandPais(List<PenghzCard> list) {
		this.handPais = list;
		getPlayingTable().changeCards(seat);
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

	public int getLostPoint() {
		return lostPoint;
	}

	public void setLostPoint(int lostPoint) {
		this.lostPoint = lostPoint;
	}

	public int getPoint() {
		return point;
	}

	public void calcResult(BaseTable table,int count, int point, boolean huangzhuang) {
		if (!huangzhuang) {
			if (point > 0) {
				this.winCount += count;
			} else {
				this.lostCount += count;
			}
		}
		changePoint(point);
//		changeTotalPoint(point);
	}

	public void setPoint(int point) {
		this.point = point;
	}

	public void changePoint(int point) {
		this.point += point;
		myExtend.changePoint(getPlayingTable().getPlayType(), point);
		if (point > getMaxPoint()) {
			setMaxPoint(point);
		}
		changeTableInfo();
	}

	public int getOweCardCount() {
		return oweCardCount;
	}

	public void setOweCardCount(int oweCardCount) {
		this.oweCardCount = oweCardCount;
	}

	public int getOutHuxi() {
		return outHuXi;
	}

	public void setOutHuxi(int chiHuxi) {
		this.outHuXi = chiHuxi;
		changeTableInfo();
	}

	public void changeOutCardHuxi(int huxi) {
		if (huxi != 0) {
			this.outHuXi += huxi;
			changeTableInfo();
			// writeErrMsg("出的牌总胡息:" + this.outHuXi + "加的胡息:" + huxi);
		}
	}

	public int getZaiHuxi() {
		return zaiHuxi;
	}

	public void setZaiHuxi(int zaiHuxi) {
		this.zaiHuxi = zaiHuxi;
	}

	public void changeZaihu(int zaiHuxi) {
		this.zaiHuxi += zaiHuxi;
		changeTableInfo();
	}

	public int getHuxi() {
		return huxi;
	}

	public void setHuxi(int huxi) {
		if (this.huxi != huxi) {
			this.huxi = huxi;

		}
	}

	/**
	 * 总胡息
	 * 
	 * @return
	 */
	public int getTotalHu() {
		return outHuXi + huxi + zaiHuxi;
	}

	public List<PhzHuCards> buildPhzHuCards() {
		List<PhzHuCards> list = new ArrayList<>();
		for (CardTypeHuxi type : cardTypes) {
			list.add(type.buildMsg().build());
		}
		if (hu != null && hu.getPhzHuCards() != null) {
			for (CardTypeHuxi type : hu.getPhzHuCards()) {
				list.add(type.buildMsg().build());
			}
		}

		return list;
	}

	public List<PhzHuCards> buildPhzCards(PenghuziTable table,long lookUid) {
		List<PhzHuCards> list = new ArrayList<>();
		for (CardTypeHuxi type : cardTypes) {
			if (type.getAction() == PenghzDisAction.action_zai) {
				// 不是本人并且是栽
					if (lookUid != this.userId){
						list.add(type.buildMsg(true).build());
					}else{
						list.add(type.buildMsg().build());
					}
				
			} else {
				list.add(type.buildMsg().build());
			}
		}

		return list;
	}

	/**
	 * 胡牌得到的积分
	 * 
	 * @return
	 */
	public int calcHuPoint(int total, int xiToTun) {
		if(xiToTun == 0){
			return 0;
		}
		return total / xiToTun;
	}

	/**
	 * 跑胡子名堂
	 * 
	 * @return
	 */
	public List<Integer> getMt() {
		List<PenghzCard> allCards = new ArrayList<>();
		for (CardTypeHuxi type : cardTypes) {
			allCards.addAll(PenghuziTool.toPhzCards(type.getCardIds()));
		}

		if (hu != null && hu.getPhzHuCards() != null) {
			for (CardTypeHuxi type : hu.getPhzHuCards()) {
				allCards.addAll(PenghuziTool.toPhzCards(type.getCardIds()));
			}
		}

		List<Integer> mtList = PenghuziMingTangRule.calcMingTang(allCards);
		// calcMingTang
		// 自摸算两分
		return mtList;
	}

	public List<PenghzCard> getAllCards() {
		List<PenghzCard> cards = new ArrayList<>();
		cards.addAll(ti);
		cards.addAll(pao);
		cards.addAll(zai);
		cards.addAll(chouZai);
		cards.addAll(peng);
		cards.addAll(chi);
		cards.addAll(handPais);
		return cards;
	}
	
	public int getPaoTi(){
		if(pao!=null &&!pao.isEmpty()){
			return 1;
		}
		
		if(ti!=null &&!ti.isEmpty()){
			return 1;
		}
		return 0;
	}

	/**
	 * 是否栽过的牌跑
	 */
    boolean isZaiPao(int val) {
		return PenghuziTool.isHasCardVal(zai, val);
	}

	/**
	 * 得到要提的栽的牌
	 */
	List<PenghzCard> getTiCard(PenghzCard card) {
		List<PenghzCard> list = PenghuziTool.getSameCards(zai, card);
		if (list == null || list.isEmpty()) {
			list = PenghuziTool.getSameCards(handPais, card);
		}
		return list;
	}

	public PenghuziHuLack getHu() {
		return hu;
	}

	public void setHu(PenghuziHuLack hu) {
		this.hu = hu;
	}

	public static void main(String[] args) {
		List<Integer> list1 = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6));
		System.out.println(list1.subList(0, 3));
		System.out.println(list1);
	}



	public List<Integer> getPassChi() {
		return passChi;
	}

	public void removePassChi(int val) {
		if (passChi.contains(val)) {
			int index = passChi.indexOf(val);
			passChi.remove(index);
			changeTableInfo();
		}
	}
	@Override
	public int loadScore(){
		PenghuziTable table = getPlayingTable(PenghuziTable.class);
		return getTotalPoint();
	}

    public boolean isCheckAuto() {
        return isCheckAuto;
    }

    public void setCheckAuto(boolean checkAuto) {
        isCheckAuto = checkAuto;
    }

    public static final List<Integer> wanfaList = Arrays.asList(GameUtil.play_type_penghuzi);

	public static void loadWanfaPlayers(Class<? extends Player> cls){
		for (Integer integer:wanfaList){
			PlayerManager.wanfaPlayerTypesPut(integer,cls, PaohuziCommandProcessor.getInstance());
		}
	}
	

	public int getDaNiaoPoint() {
		return daNiaoPoint;
	}

	public void setDaNiaoPoint(int daNiaoPoint) {
		this.daNiaoPoint = daNiaoPoint;
		changeTableInfo();
	}

	public Map<Integer, Integer> getPengMap() {
		return pengMap;
	}

	public void addPlayerPengVal(int val,int seat) {
		this.pengMap.put(val, seat);
	}

	public int getWufuBaoj() {
//		if(wufuBaoj>=0){
//			 int pwp =  getPWPTCount(null);
//				if(getHandPais().size()!=2||pwp!=4||!PenghuziTool.isDuiZi(getHandPais())){
//					wufuBaoj =-1;
//				}
//		}
		return wufuBaoj;
	}

	public void setWufuBaoj(int wufuBaoj) {
		this.wufuBaoj = wufuBaoj;
		changeTableInfo();
	}

	public boolean isHasNoDui() {
		return hasNoDui;
	}

	public void setHasNoDui(boolean hasDui) {
		this.hasNoDui = hasDui;
		changeTableInfo();
	}

	public List<Integer> getDisOutPais() {
		return disOutPais;
	}

	public void addDisOutPais(int cardId) {
		if(!disOutPais.contains(cardId))
		this.disOutPais.add(cardId);
	}
    
    
	

}
