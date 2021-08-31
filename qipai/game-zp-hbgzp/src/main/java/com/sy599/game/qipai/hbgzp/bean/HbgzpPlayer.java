package com.sy599.game.qipai.hbgzp.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.google.protobuf.GeneratedMessage;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.common.constant.SharedConstants.player_state;
import com.sy599.game.db.bean.group.GroupUser;
import com.sy599.game.db.dao.gold.GoldDao;
import com.sy599.game.db.dao.group.GroupDao;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.TablePhzResMsg.ClosingPhzPlayerInfoRes;
import com.sy599.game.msg.serverPacket.TablePhzResMsg.PhzHuCards;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes;
import com.sy599.game.qipai.hbgzp.command.HbgzpCommandProcessor;
import com.sy599.game.qipai.hbgzp.constant.HbgzpConstants;
import com.sy599.game.qipai.hbgzp.rule.Hbgzp;
import com.sy599.game.qipai.hbgzp.tool.HbgzpHelper;
import com.sy599.game.qipai.hbgzp.tool.HbgzpHuLack;
import com.sy599.game.qipai.hbgzp.tool.HbgzpTool;
import com.sy599.game.util.GameUtil;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.util.StringUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

public class HbgzpPlayer extends Player {
	// 座位id
	private int seat;
	// 状态
	private player_state state;// 1进入 2已准备 3正在玩 4已结束
	/**
	 * 牌局是否在线 1离线 2在线
	 */
	private int isEntryTable;
	private List<Hbgzp> handPais;
	private List<Hbgzp> firstCards;
	private List<Hbgzp> outPais;
	private List<Hbgzp> peng;
	private List<Hbgzp> aGang;
	private List<Hbgzp> mGang;
	private List<Hbgzp> chi;
	private List<Hbgzp> hua;
	private Set<Integer> zhaVal;//扎过得val(list中包含多个 则表示不确定扎过哪一个)
	private Integer zhaCount;
	private Integer kaizhaoVal;//开招的牌，开中了才可以滑
	private Integer chiVal;//刚吃的牌 要隔一轮才可以打出去
	private Integer singleMaxHuxi;//单局最大胡息
	
	private int outHuXi;// 需要记下吃的胡息
    private int huxi;
    private HbgzpHuLack hu;
	private int winCount;
	private int lostCount;
	private int piaoFen=-1;
	private int dPiaofen = 0;
	/**
	 * 局分
	 */
	private int lostPoint;
	/**
	 * 大局分
	 */
	private int point;
	/** ZZMajiangConstants.ACTION_COUNT_INDEX_ **/
	private int[] actionArr = new int[6];
	/** ZZMajiangConstants.ACTION_COUNT_INDEX_ **/
	private int[] actionTotalArr = new int[6];
	private int passMajiangVal;
	private List<Integer> passGangValList;
	/**
	 * 出牌对应操作信息
	 */
	private List<HbgzpCardDisType> cardTypes;
	//大胡
	private List<Integer> dahu;
	private int chui = -1;//-1未选择，1：锤1分 2：锤2分
	//开始托管合计时
    private volatile boolean isCheckAuto = false;
	private volatile boolean autoPlay = false;//托管
	private volatile boolean autoPlaySelf = false;//托管
	private volatile long lastCheckTime = 0;//最后检查时间
	private volatile long autoPlayTime = 0;//自动操作时间
	private volatile boolean checkAutoPlay = false; //是否是牌桌上的焦点
	private volatile long sendAutoTime=0;//发送倒计时间
	//最后操作时间
    private volatile long lastOperateTime = 0;

	public HbgzpPlayer() {
		handPais = new ArrayList<Hbgzp>();
		firstCards = new ArrayList<Hbgzp>();
		outPais = new ArrayList<Hbgzp>();
		peng = new ArrayList<>();
		aGang = new ArrayList<>();
		zhaVal = new HashSet<>();
		zhaCount=0;
		kaizhaoVal=0;
		chiVal =0;
		singleMaxHuxi = 0;
		mGang = new ArrayList<>();
		chi = new ArrayList<>();
		hua = new ArrayList<>();
		passGangValList = new ArrayList<>();
		cardTypes = new ArrayList<>();
		dahu = new ArrayList<>();
		autoPlaySelf = false;
		autoPlay = false;
		autoPlayTime = 0;
		checkAutoPlay = false;
		lastCheckTime = System.currentTimeMillis();
	}
	
	public int getChui() {
		return chui;
	}

	public void setChui(int chui) {
		this.chui = chui;
	}
	
	public List<Integer> getDahu() {
		return dahu;
	}

	public void setDahu(List<Integer> dahu) {
		this.dahu = dahu;
	}

	public int getSeat() {
		return seat;
	}

	public void setSeat(int seat) {
		this.seat = seat;
	}

	public void dealHandPais(List<Hbgzp> pais) {
		this.handPais = pais;
		this.firstCards = pais;
		getPlayingTable().changeCards(seat);
	}

	public List<Hbgzp> getHandMajiang() {
		return handPais;
	}
	/**
     * 拿到可以操作的牌的值
     *
     * @return
     */
    public List<Integer> getOperateCardVals() {
    	List<Hbgzp> copy = new ArrayList<>(handPais);
    	return HbgzpTool.toPhzRepeatVals(copy);
    	
    }
	public List<Integer> getHandPais() {
		return HbgzpHelper.toMajiangIds(handPais);
	}
	public List<Integer> getFirstCards() {
		return HbgzpHelper.toMajiangIds(firstCards);
	}

	public List<Integer> getOutPais() {
		return HbgzpHelper.toMajiangIds(outPais);
	}

	public List<Hbgzp> getOutMajing() {
		return outPais;
	}

	public void moMajiang(Hbgzp majiang) {
		setPassMajiangVal(0);
		handPais.add(majiang);
		getPlayingTable().changeCards(seat);
	}

	/**
     * 增加玩家明面上的牌
     *
     * @param action
     * @param disSeat 碰或杠的出牌人
     */
	public void addOutPais(List<Hbgzp> cards, int action, int disSeat) {
		if(action != HbgzpDisAction.action_chi && action != HbgzpDisAction.action_angang){
			handPais.removeAll(cards);
		}
		if (action == HbgzpDisAction.action_chupai) {
			outPais.addAll(cards);
		} else {
			if(action == HbgzpDisAction.action_chi){
				chi.add(cards.get(0));//捡一张牌
				chiVal=cards.get(0).getVal();
			}else 
				if (action == HbgzpDisAction.action_peng) {
				peng.addAll(cards);
				// changeAction(0, 1);
			} else if (action == HbgzpDisAction.action_minggang) {
				//myExtend.setMjFengshen(FirstmythConstants.firstmyth_index12, 1);
				mGang.addAll(cards);
				kaizhaoVal = cards.get(0).getVal();
				if (cards.size() != 1) {
					//changeAction(1, 1);
				} else {
					Hbgzp pengMajiang = cards.get(0);
					Iterator<Hbgzp> iterator = peng.iterator();
					while (iterator.hasNext()) {
						Hbgzp majiang = iterator.next();
						if (majiang.getVal() == pengMajiang.getVal()) {
							mGang.add(majiang);
							iterator.remove();
						}
					}
					//changeAction(2, 1);
				}
				changeAction(HbgzpConstants.ACTION_COUNT_INDEX_MINGGANG, 1);
			}else if (action == HbgzpDisAction.action_hua) {
				hua.addAll(cards);
				if (cards.size() != 1) {
					Hbgzp pengMajiang = cards.get(0);
					Iterator<Hbgzp> iterator = aGang.iterator();
					while (iterator.hasNext()) {
						Hbgzp majiang = iterator.next();
						if (majiang.getVal() == pengMajiang.getVal()) {
//							hua.add(majiang);
							iterator.remove();
						}
					}
					
//					if(zhaVal.contains(cards.get(0).getVal())){
////	            		int canZhaCount = zhaCount - zhaVal.size();//已经扎过的次数 - 扎过的牌 =不知道之前不知道扎得哪张牌的次数
////	            		if(canZhaCount <= 0){
////	            			zhaCount--;
////	            		}
//						zhaCount--;
//	            	}
				} else {
					Hbgzp pengMajiang = cards.get(0);
					Iterator<Hbgzp> iterator = mGang.iterator();
					while (iterator.hasNext()) {
						Hbgzp majiang = iterator.next();
						if (majiang.getVal() == pengMajiang.getVal()) {
							hua.add(majiang);
							iterator.remove();
						}
					}
				}
				changeAction(HbgzpConstants.ACTION_COUNT_INDEX_HUA, 1);
			}
			else if (action == HbgzpDisAction.action_angang) {
//				aGang.addAll(cards); //暗杠（扎） 放在手上
				Map<Integer, Integer> canZhaMap = getCanZhaMap();
				if(canZhaMap.size()>0){
					Set<Integer> copyZhaVal = new HashSet<>(zhaVal);
					for (HbgzpCardDisType disType : cardTypes){
						if (disType.getAction() == HbgzpDisAction.action_hua){
							Hbgzp gzp =	Hbgzp.getPaohzCard(disType.getCardIds().get(0));
							if(copyZhaVal.contains(gzp.getVal())){
								copyZhaVal.remove(gzp.getVal());
							}
						}
					}
					int zhaValCount = 0;
					for (int key : canZhaMap.keySet()) {
						if(copyZhaVal.contains(key)){
							zhaValCount ++;
							copyZhaVal.remove(key);
						}
					}
					zhaValCount += copyZhaVal.size();
					int canZhaCount = zhaCount - zhaValCount;
					int cc = zhaCount-zhaValCount+1;
//					int canZhaCount = zhaCount - zhaVal.size();//已经扎过的次数 - 扎过的牌 =不知道之前不知道扎得哪张牌的次数
					if(canZhaMap.size()-zhaValCount <= zhaCount-zhaValCount+1){
						zhaVal.addAll(canZhaMap.keySet());
					}
				}
				zhaCount+=1;
				changeAction(HbgzpConstants.ACTION_COUNT_INDEX_ANGANG, 1);
			}
			getPlayingTable().changeExtend();
			if(action != HbgzpDisAction.action_angang && action != HbgzpDisAction.action_chi){
				addCardType(action, cards, disSeat, 0);
			}
		}
		getPlayingTable().changeCards(seat);
	}
	
	public Map<Integer, Integer>  getCanZhaMap(){
		Map<Integer, Integer> canZhaMap = new HashMap<>();
		List<Hbgzp> copy = new ArrayList<>(handPais);
		Map<Integer, Integer> handMap = HbgzpHelper.toMajiangValMap(this,copy);
		if (handMap.containsValue(4) || handMap.containsValue(5)) {
			for (Entry<Integer, Integer> entry:handMap.entrySet()) {
				int count = HbgzpHelper.getMajiangCount(this,handPais, entry.getKey());
//				if(count >= 4 && entry.getValue() >=4 && !zhaVal.contains(entry.getKey())){//没扎过的牌(或者说是不确定扎没扎过的牌)
				if(count >= 4 && entry.getValue() >=4 ){//没扎过的牌(或者说是不确定扎没扎过的牌)
					canZhaMap.put(entry.getKey(), entry.getValue());
				}
			}
		}
		return canZhaMap;
	}
	
	public void qGangUpdateOutPais(Hbgzp card){
		Iterator<Hbgzp> mGangIterator = mGang.iterator();
		while (mGangIterator.hasNext()){
			Hbgzp mj = mGangIterator.next();
			if(mj.getVal() == card.getVal()){
				peng.add(mj);
				mGangIterator.remove();
			}
		}
		Iterator<Hbgzp> aGangIterator = aGang.iterator();
		int added = 0;
		while (aGangIterator.hasNext()){
			Hbgzp mj = aGangIterator.next();
			if(mj.getVal() == card.getVal()){
				if(++added < 4){
					handPais.add(mj);
				}
				aGangIterator.remove();
			}
		}
		HbgzpCardDisType delDisType = null;
		for (HbgzpCardDisType disType : cardTypes){
			if (disType.getAction() == HbgzpDisAction.action_minggang){
				if (disType.isHasCardVal(card)){
					disType.setAction(HbgzpDisAction.action_peng);
					disType.getCardIds().remove((Integer)card.getId());
					//disType.setDisSeat(seat);
                    break;
				}
			}
			if (disType.getAction() == HbgzpDisAction.action_angang){
				if (disType.isHasCardVal(card)){
					delDisType = disType;
					break;
				}
			}
		}
		if(delDisType != null){
			cardTypes.remove(delDisType);
		}
    }
	/**
     * 添加牌型，例如将碰变成明杠等
     *
     * @param action
     * @param disCardList
     */
    public void addCardType(int action, List<Hbgzp> disCardList, int disSeat, int disStatus) {
    	if (action != 0) {
    		int huxi = HbgzpTool.getOutCardHuxi(action, disCardList);
            if (action == HbgzpDisAction.action_minggang && disCardList.size() == 1) {
                Hbgzp majiang = disCardList.get(0);
                for (HbgzpCardDisType disType : cardTypes) {
                    if (disType.getAction() == HbgzpDisAction.action_peng) {
                        if (disType.isHasCardVal(majiang)) {
                            disType.setAction(HbgzpDisAction.action_minggang);
                            disType.addCardId(majiang.getId());
                            disType.setDisSeat(seat);
                            disType.setHuxi(huxi);
                            break;
                        }
                    }
                }
            }if (action == HbgzpDisAction.action_hua && disCardList.size() == 1) {
                Hbgzp majiang = disCardList.get(0);
                for (HbgzpCardDisType disType : cardTypes) {
                    if (disType.getAction() == HbgzpDisAction.action_minggang) {
                        if (disType.isHasCardVal(majiang)) {
                            disType.setAction(HbgzpDisAction.action_hua);
                            disType.addCardId(majiang.getId());
                            disType.setDisSeat(seat);
                            disType.setHuxi(huxi);
                            break;
                        }
                    }
                    if (disType.getAction() == HbgzpDisAction.action_angang) {
                        if (disType.isHasCardVal(majiang)) {
                        	disType.setAction(HbgzpDisAction.action_hua);
                            disType.addCardId(majiang.getId());
                            disType.setDisSeat(seat);
                            disType.setHuxi(huxi);
                            break;
                        }
                    }
                }
            } else {
                HbgzpCardDisType type = new HbgzpCardDisType();
                type.setAction(action);
                type.setCardIds(HbgzpTool.toMajiangIds(disCardList));
                type.setDisSeat(disSeat);
                type.setDisStatus(disStatus);
                type.setHuxi(huxi);
                cardTypes.add(type);
            }
//            changeOutCardHuxi(huxi);
        }
    }
	/**
	 * 已经摸过牌了
	 * 
	 * @return
	 */
	public boolean isAlreadyMoMajiang() {
		//判断扎
//		if(zhaVal.size()%3 ==1){
//			return !handPais.isEmpty() && handPais.size() % 3 == 0;
//		}else if(zhaVal.size()%3 ==2){
//			return !handPais.isEmpty() && handPais.size() % 3 == 1;
//		}
//		return !handPais.isEmpty() && handPais.size() % 3 == 2;
		if(zhaCount%3 ==1){
			return !handPais.isEmpty() && handPais.size() % 3 == 0;
		}else if(zhaCount%3 ==2){
			return !handPais.isEmpty() && handPais.size() % 3 == 1;
		}
		return !handPais.isEmpty() && handPais.size() % 3 == 2;
	}

	public void removeOutPais(List<Hbgzp> cards, int action) {
		boolean remove = outPais.removeAll(cards);
		if (remove) {
//			if (action == HbgzpDisAction.action_peng) {
//				changeAction(4, 1);
//			} else if (action == HbgzpDisAction.action_minggang) {
//				changeAction(5, 1);
//			}
			getPlayingTable().changeCards(seat);
		}
	}

	public String toExtendStr() {
		StringBuffer sb = new StringBuffer();
		sb.append(StringUtil.implode(actionArr)).append("|");
		sb.append(StringUtil.implode(actionTotalArr)).append("|");
		sb.append(StringUtil.implode(passGangValList, ",")).append("|");
		sb.append(StringUtil.implode(dahu, ",")).append("|");
		return sb.toString();

	}

	public void initExtend(String info) {
		if (StringUtils.isBlank(info)) {
			return;
		}
		int i = 0;
		String[] values = info.split("\\|");
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
		if (!StringUtils.isBlank(val7)) {
			dahu = StringUtil.explodeToIntList(val7);
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
		sb.append(getTotalPoint()).append(",");
		sb.append(lostPoint).append(",");
		sb.append(passMajiangVal).append(",");
		sb.append(chui).append(",");
		sb.append(autoPlay?1:0).append(",");
		sb.append(autoPlaySelf?1:0).append(",");
		sb.append(autoPlayTime).append(",");
		sb.append(lastCheckTime).append(",");
		sb.append(outHuXi).append(",");
        sb.append(huxi).append(",");
        sb.append(zhaCount).append(",");
        sb.append(piaoFen).append(",");
        sb.append(dPiaofen).append(",");
        String zhaValStr = "";
        if(zhaVal != null && zhaVal.size() > 0){
        	for (Integer zv : zhaVal) {
				if("".equals(zhaValStr)){
					zhaValStr = zv+"";
				}else{
					zhaValStr = zhaValStr+"-"+zv;
				}
			}
        }
        sb.append(zhaValStr).append(",");
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
			this.winCount = StringUtil.getIntValue(values, i++);
			this.lostCount = StringUtil.getIntValue(values, i++);
			this.point = StringUtil.getIntValue(values, i++);
			setTotalPoint(StringUtil.getIntValue(values, i++));
			this.lostPoint = StringUtil.getIntValue(values, i++);
			this.passMajiangVal = StringUtil.getIntValue(values, i++);
			this.chui = StringUtil.getIntValue(values, i++);
			this.autoPlay = StringUtil.getIntValue(values, i++)==1;
			this.autoPlaySelf = StringUtil.getIntValue(values, i++)==1;
			this.autoPlayTime = StringUtil.getLongValue(values, i++);
			this.lastCheckTime = StringUtil.getLongValue(values, i++);
			this.outHuXi = StringUtil.getIntValue(values, i++);
            this.huxi = StringUtil.getIntValue(values, i++);
            this.zhaCount = StringUtil.getIntValue(values, i++);
            this.piaoFen = StringUtil.getIntValue(values, i++);
            this.dPiaofen = StringUtil.getIntValue(values, i++);
            String zhaValStr = StringUtil.getValue(values, i++);
            if(!"".equals(zhaValStr)){
            	String[] zvs = zhaValStr.split("-");
            	this.zhaVal =new HashSet<>();
            	for (int j = 0; j < zvs.length; j++) {
            		zhaVal.add(Integer.parseInt(zvs[j]));
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
			list.addAll(HbgzpHelper.toMajiangIds(peng));
		}
		if (!mGang.isEmpty()) {
			list.addAll(HbgzpHelper.toMajiangIds(mGang));
		}
		if (!hua.isEmpty()) {
			list.addAll(HbgzpHelper.toMajiangIds(hua));
		}
//		if (!aGang.isEmpty()) {
//			list.addAll(HbgzpHelper.toMajiangIds(aGang));
//		}
		if (!chi.isEmpty()) {
			list.addAll(HbgzpHelper.toMajiangIds(chi));
		}
		return list;
	}
	
	public List<PhzHuCards> buildDisCards(long lookUid) {
		return buildDisCards(lookUid, true);
	}
	public List<PhzHuCards> buildDisCards(long lookUid, boolean hide) {
		List<PhzHuCards> list = new ArrayList<>();
		for (HbgzpCardDisType type : cardTypes) {
			if(type.getAction() == HbgzpDisAction.action_angang){
				continue;
			}
			if (hide && lookUid != this.userId ) {
				// 不是本人并且是栽
				list.add(type.buildMsg(true).build());
			} else {
				list.add(type.buildMsg().build());
			}
		}
		if(chi != null && chi.size() > 0){
			HbgzpCardDisType type = new HbgzpCardDisType();
			type.setAction(HbgzpDisAction.action_chi);
			for (Hbgzp gzp : chi) {
				type.addCardId(gzp.getId());
			}
			list.add(type.buildMsg().build());
		}
		return list;
	}
	public List<PhzHuCards> buildDisCards(long lookUid, boolean hide,long userId) {
		List<PhzHuCards> list = new ArrayList<>();
		for (HbgzpCardDisType type : cardTypes) {
			if (hide && lookUid != this.userId && (type.getAction() == HbgzpDisAction.action_angang || 
					type.getAction() == HbgzpDisAction.action_chi)) {
				// 不是本人并且是栽
				//list.add(type.buildMsg(true).build());
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
		res.setPoint(getTotalPoint() + getLostPoint());
		res.addAllOutedIds(getOutPais());
		res.addAllMoldIds(getMoldIds());
		res.addAllAngangIds(HbgzpHelper.toMajiangIds(aGang));
		res.addAllMoldCards(buildDisCards(userId));
		List<Hbgzp> gangList = getGang();
		// 是否杠过
		HbgzpTable table = getPlayingTable(HbgzpTable.class);
		res.addExt(gangList.isEmpty() ? 0 : 1);
		// 现在是否自己摸的牌
        res.addExt(isAlreadyMoMajiang() ? 1 : 0);
		res.addExt(handPais != null ? handPais.size() : 0);
		res.addExt(getChui());
		res.addExt(autoPlay ? 1 : 0);
		res.addExt(autoPlaySelf ? 1 : 0);
		res.addExt(zhaCount);
		res.addExt(getOutCardHuxi());
		res.addExt(piaoFen);
		res.addExt(dPiaofen);
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
		if (isrecover) {
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

	public List<Hbgzp> getGang() {
		List<Hbgzp> gang = new ArrayList<>();
		gang.addAll(aGang);
		gang.addAll(mGang);
		return gang;
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
		changePoint(point);

	}

	public void calcWin(int winCount, int point) {
		this.winCount += winCount;
		changePoint(point);

	}

	public int getPoint() {
		return point;
	}

	public void setPoint(int point) {
		this.point = point;
	}

	public void setLostPoint(int lostPoint) {
		this.lostPoint = lostPoint;
	}

	public void changeLostPoint(int point) {
		this.lostPoint += point;
		changeTbaleInfo();
	}

	public int getLostPoint() {
		return lostPoint;
	}
	
	public Integer getSingleMaxHuxi() {
		return singleMaxHuxi;
	}

	public void setSingleMaxHuxi(Integer singleMaxHuxi) {
		this.singleMaxHuxi = singleMaxHuxi;
	}

	public void changePoint(int point) {
		this.point += point;
		myExtend.changePoint(getPlayingTable().getPlayType(), point);
		//myExtend.setMjFengshen(FirstmythConstants.firstmyth_index7, point);
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
		actionArr = new int[6];
		actionTotalArr = new int[6];
		peng.clear();
		aGang.clear();
		zhaVal.clear();
		zhaCount = 0;
		kaizhaoVal =0;
		chiVal = 0;
		singleMaxHuxi=0;
		mGang.clear();
		chi.clear();
		cardTypes.clear();
		dahu.clear();
		setOutHuxi(0);
	    setHuxi(0);
	    setHu(null);
	    setPassMajiangVal(0);
		clearPassGangVal();
		setWinCount(0);
		setLostCount(0);
		setPoint(0);
		setLostPoint(0);
		setTotalPoint(0);
		setSeat(0);
		 setPiaoFen(-1);
		 setdPiaofen(-1);
		if (!isCompetition) {
			setPlayingTableId(0);

		}
		setChui(-1);
		autoPlaySelf = false;
		autoPlay = false;
		lastCheckTime = System.currentTimeMillis();
		checkAutoPlay = false;
		saveBaseInfo();
	}

	/**
	 * 单局详情
	 * 
	 * @return
	 */
	public ClosingPhzPlayerInfoRes.Builder bulidOneClosingPlayerInfoRes() {
		ClosingPhzPlayerInfoRes.Builder res = ClosingPhzPlayerInfoRes.newBuilder();
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
		res.addAllCards(getHandPais());
        List<PhzHuCards> list = new ArrayList<>();
        for (HbgzpCardDisType type : cardTypes) {
            list.add(type.buildMsg().build());
        }
//        res.addAllMoldPais(list);
		return res;
	}

	/**
	 * 总局详情
	 * 
	 * @return
	 */
	public ClosingPhzPlayerInfoRes.Builder bulidTotalClosingPlayerInfoRes() {
		ClosingPhzPlayerInfoRes.Builder res = ClosingPhzPlayerInfoRes.newBuilder();
		res.setUserId(userId + "");
		res.setName(name);
		res.setPoint(point);
//		res.addAllActionCount(Arrays.asList(ArrayUtils.toObject(actionTotalArr)));
        res.setTotalPoint(getTotalPoint());
        res.setSeat(seat);
        if (!StringUtils.isBlank(getHeadimgurl())) {
            res.setIcon(getHeadimgurl());
        } else {
            res.setIcon("");
        }
        res.setSex(sex);
		res.addAllCards(getHandPais());
        List<PhzHuCards> list = new ArrayList<>();
        for (HbgzpCardDisType type : cardTypes) {
            list.add(type.buildMsg().build());
        }
        res.addAllFirstCards(getFirstCards());
//        res.addAllMoldPais(list);
//        res.addAllDahus(filterDaHu(dahu));
//
//        res.addExt(chui);
//        res.addExt(piaoFen);
		return res;
	}
	/**
	 * TODO 设置大结算的胜负次数
	 */
//	public void setBaseCount(){
//		this.winCnt=winCount;
//		this.loseCnt=lostCount;
//	}

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
		setHu(null);
		setPassMajiangVal(0);
		setLostPoint(0);
		clearPassGangVal();
		peng.clear();
		aGang.clear();
		mGang.clear();
		zhaVal.clear();
		zhaCount=0;
		chiVal=0;
		kaizhaoVal=0;
		chi.clear();
		cardTypes.clear();
		actionArr = new int[6];
		dahu.clear();
		setOutHuxi(0);
	    setHuxi(0);
	    if(!autoPlay){
	    	setPiaoFen(-1);
	    }
		getPlayingTable().changeExtend();
		getPlayingTable().changeCards(seat);
		changeState(player_state.entry);
		if(autoPlaySelf){
			autoPlaySelf = false;
			autoPlay = false;
			checkAutoPlay = false;
			lastCheckTime = System.currentTimeMillis();
		}
		if(!autoPlay){
			checkAutoPlay =false;
			lastCheckTime = System.currentTimeMillis();
		}
	}

	@Override
	public void initPais(String handPai, String outPai) {
		if (!StringUtils.isBlank(handPai)) {
			List<Integer> list = StringUtil.explodeToIntList(handPai);
			this.handPais = HbgzpHelper.toMajiang(list);
		}
		if (!StringUtils.isBlank(outPai)) {
			String[] values = outPai.split(";");
			int i = -1;
			for (String value : values) {
				i++;
				if (i == 0) {
					List<Integer> list = StringUtil.explodeToIntList(value);
					this.outPais = HbgzpHelper.toMajiang(list);
				} else {
					HbgzpCardDisType type = new HbgzpCardDisType();
					type.init(value);
					cardTypes.add(type);
					List<Hbgzp> majiangs = HbgzpHelper.toMajiang(type.getCardIds());
					if (type.getAction() == HbgzpDisAction.action_angang) {
						aGang.addAll(majiangs);
					} else if (type.getAction() == HbgzpDisAction.action_minggang) {
						mGang.addAll(majiangs);
					}else if (type.getAction() == HbgzpDisAction.action_peng) {
						peng.addAll(majiangs);
					}else if (type.getAction() == HbgzpDisAction.action_chi) {
						chi.addAll(majiangs);
					}
				}
			}
		}
	}
	/**
	 * 出牌
	 * @return
	 */
	public String buildOutPaiStr() {
		StringBuffer sb = new StringBuffer();
		List<Integer> outPais = getOutPais();
		sb.append(StringUtil.implode(outPais)).append(";");
		for (HbgzpCardDisType huxi : cardTypes) {
			sb.append(huxi.toStr()).append(";");
		}
		return sb.toString();
	}
	/**
	 * 别人出牌可做的操作
	 * 
	 * @param majiang
	 * @param canCheckHu 是否能点炮 或 抢杠胡
	 * @return 0胡 1碰 2明刚 3暗杠(暗杠后来不需要了 暗杠也用3标记)
	 */
	public List<Integer> checkDisMajiang(Hbgzp majiang, boolean canCheckHu,boolean canChi) {
		List<Integer> list = new ArrayList<>();
		int[] arr = new int[HbgzpConstants.ACTION_INDEX_LENGTH];
		HbgzpTable table = getPlayingTable(HbgzpTable.class);
		// 转转麻将别人出牌可以胡
//		if (canCheckHu && passMajiangVal != majiang.getVal()) {
			// 没有出现漏炮的情况
//			HbgzpHu hu = HbgzpTool.isHu(this, majiang);
		HbgzpHuLack lack = checkHuNew(majiang, false);
		if (lack.isHu()) {
			arr[HbgzpConstants.ACTION_INDEX_HU] = 1;
		}	
//		}
		int count = HbgzpHelper.getMajiangCount(this,handPais, majiang.getVal());
		if (count >= 2) {
			if(!zhaVal.contains(majiang.getVal())){
				arr[HbgzpConstants.ACTION_INDEX_PENG] = 1;// 可以碰
			}
		}
		if (count >= 3) {
			if(!zhaVal.contains(majiang.getVal())){
				arr[HbgzpConstants.ACTION_INDEX_MINGGANG] = 1;// 可以招
			}
		}
		if (count == 4) {
			arr[HbgzpConstants.ACTION_INDEX_HUA] = 1;// 可以滑
		}
		if(canChi&&table.calcNextSeat(table.getDisCardSeat())  == getSeat()){
//			List<Hbgzp> chi = HbgzpTool.checkChi(handPais, majiang);
//			if(!chi.isEmpty()){
//				arr[HbgzpConstants.ACTION_INDEX_CHI] = 1;
//			}
			//可以必捡
			arr[HbgzpConstants.ACTION_INDEX_CHI] = 1;
		}
		for (int val : arr) {
			list.add(val);
		}
		if (list.contains(1)) {
			return list;
		} else {
			return Collections.emptyList();
		}
	}

	public Hbgzp getLastMoMajiang() {
		if (handPais.isEmpty()) {
			return null;

		} else {
			return handPais.get(handPais.size() - 1);

		}
	}

	/**
	 * 自己出牌可做的操作
	 * 
	 * @param majiang
	 * @return 0胡 1碰 2明刚 3暗杠
	 */
	public List<Integer> checkMo(Hbgzp majiang) {
		List<Integer> list = new ArrayList<>();
		int[] arr = new int[HbgzpConstants.ACTION_INDEX_LENGTH];
//		HbgzpHu hu = HbgzpTool.isHu(this);
		HbgzpHuLack lack = checkHuNew(majiang, true);
		if (lack.isHu()) {
			arr[HbgzpConstants.ACTION_INDEX_ZIMO] = 1;
		}
		if (isAlreadyMoMajiang()) {
//			Map<Integer, Integer> pengMap = HbgzpHelper.toMajiangValMap(this,peng);
//			for (Hbgzp handMajiang : handPais) {
//				if (pengMap.containsKey(handMajiang.getVal())) {
//					// 有碰过
//					arr[HbgzpConstants.ACTION_INDEX_ANGANG] = 1;// 可以杠
//					break;
//				}
//			}
			List<Hbgzp> copy = new ArrayList<>(handPais);
			Map<Integer, Integer> handMap = HbgzpHelper.toMajiangValMap(this,copy);
			if (handMap.containsValue(4) || handMap.containsValue(5)) {
				Map<Integer, Integer> canZhaMap = getCanZhaMap();
				if(canZhaMap.size()>0){
					int zhaValCount = 0;
					for (int key : canZhaMap.keySet()) {
						if(zhaVal.contains(key)){
							zhaValCount ++;
						}
					}
//					int canZhaCount = zhaCount - zhaValCount;//已经扎过的次数 - 扎过的牌 =不知道之前不知道扎得哪张牌的次数
//					if(canZhaCount >=0 && canZhaMap.size()> canZhaCount){
					if(canZhaMap.size()> zhaValCount){
						arr[HbgzpConstants.ACTION_INDEX_ANGANG] = 1;// 可以暗杠
					}
				}
				
//				for (Entry<Integer, Integer> entry:handMap.entrySet()) {
//					if(entry.getValue() >= 4 && !zhaVal.contains(entry.getKey())){
//						
//					}
//					if(entry.getValue() == 5){
//						arr[HbgzpConstants.ACTION_INDEX_HUA] = 1;// 可以滑
//					}
//				}
			}
			Map<Integer, Integer> pengMap = HbgzpHelper.toMajiangValMap(this,mGang);
			for (Hbgzp handMajiang : handPais) {
				if (pengMap.containsKey(handMajiang.getVal()) && kaizhaoVal == handMajiang.getVal()) {
					// 有招过
					arr[HbgzpConstants.ACTION_INDEX_HUA] = 1;// 可以杠
					break;
				}
			}
			if (handMap.containsValue(5)) {//要判断这个牌没有捡上来的
//				for (Entry<Integer, Integer> enrty :handMap.entrySet()) {
//					int count = HbgzpHelper.getMajiangCount(this,handPais, enrty.getKey());
//					if(count >=5){
						arr[HbgzpConstants.ACTION_INDEX_HUA] = 1;// 可以滑
//					}
//				}
			}
		}
		for (int val : arr) {
			list.add(val);
		}
		//摸完牌设置清理开招
		kaizhaoVal =0;
		if (list.contains(1)) {
			return list;
		} else {
			return Collections.emptyList();
		}
	}
	public List<Integer> checkMoQishou(Hbgzp majiang) {
		List<Integer> list = new ArrayList<>();
		int[] arr = new int[HbgzpConstants.ACTION_INDEX_LENGTH];
//		HbgzpHu hu = HbgzpTool.isHu(this);
//		HbgzpHuLack lack = checkHuNew(majiang, true);
//		if (lack.isHu()) {
//			arr[HbgzpConstants.ACTION_INDEX_ZIMO] = 1;
//		}

//		Map<Integer, Integer> pengMap = HbgzpHelper.toMajiangValMap(this,peng);
//		for (Hbgzp handMajiang : handPais) {
//			if (pengMap.containsKey(handMajiang.getVal())) {
//				// 有碰过
//				arr[HbgzpConstants.ACTION_INDEX_ANGANG] = 1;// 可以杠
//				break;
//			}
//		}
		List<Hbgzp> copy = new ArrayList<>(handPais);
		Map<Integer, Integer> handMap = HbgzpHelper.toMajiangValMap(this,copy);
		if (handMap.containsValue(4) || handMap.containsValue(5)) {
			Map<Integer, Integer> canZhaMap = getCanZhaMap();
			if(canZhaMap.size()>0){
				int zhaValCount = 0;
				for (int key : canZhaMap.keySet()) {
					if(zhaVal.contains(key)){
						zhaValCount ++;
					}
				}
//				int canZhaCount = zhaCount - zhaValCount;//已经扎过的次数 - 扎过的牌 =不知道之前不知道扎得哪张牌的次数
//				if(canZhaCount >=0 && canZhaMap.size()> canZhaCount){
				if(canZhaMap.size()> zhaValCount){
					arr[HbgzpConstants.ACTION_INDEX_ANGANG] = 1;// 可以暗杠
				}
			}
			
//			for (Entry<Integer, Integer> entry:handMap.entrySet()) {
//				if(entry.getValue() >= 4 && !zhaVal.contains(entry.getKey())){
//					
//				}
//				if(entry.getValue() == 5){
//					arr[HbgzpConstants.ACTION_INDEX_HUA] = 1;// 可以滑
//				}
//			}
		}
		Map<Integer, Integer> pengMap = HbgzpHelper.toMajiangValMap(this,mGang);
		for (Hbgzp handMajiang : handPais) {
			if (pengMap.containsKey(handMajiang.getVal()) && kaizhaoVal == handMajiang.getVal()) {
				// 有招过
				arr[HbgzpConstants.ACTION_INDEX_HUA] = 1;// 可以杠
				break;
			}
		}
		if (handMap.containsValue(5)) {//要判断这个牌没有捡上来的
//			for (Entry<Integer, Integer> enrty :handMap.entrySet()) {
//				int count = HbgzpHelper.getMajiangCount(this,handPais, enrty.getKey());
//				if(count >=5){
			arr[HbgzpConstants.ACTION_INDEX_HUA] = 1;// 可以滑
//				}
//			}
		}
	
		for (int val : arr) {
			list.add(val);
		}
		//摸完牌设置清理开招
		kaizhaoVal =0;
		if (list.contains(1)) {
			return list;
		} else {
			return Collections.emptyList();
		}
	}
	/**
	 * 捡牌可做的操作
	 * 
	 * @param majiang
	 * @return 0胡 1碰 2明刚 3暗杠
	 */
	public List<Integer> checkJianpai() {
		List<Integer> list = new ArrayList<>();
		int[] arr = new int[HbgzpConstants.ACTION_INDEX_LENGTH];

		List<Hbgzp> copy = new ArrayList<>(handPais);
		Map<Integer, Integer> handMap = HbgzpHelper.toMajiangValMap(this,copy);
		if (handMap.containsValue(4) || handMap.containsValue(5)) {
			Map<Integer, Integer> canZhaMap = getCanZhaMap();
			if(canZhaMap.size()>0){
				int zhaValCount = 0;
				for (int key : canZhaMap.keySet()) {
					if(zhaVal.contains(key)){
						zhaValCount ++;
					}
				}
//				int canZhaCount = zhaCount - zhaValCount;//已经扎过的次数 - 扎过的牌 =不知道之前不知道扎得哪张牌的次数
//				int canZhaCount = zhaCount - zhaVal.size();//已经扎过的次数 - 扎过的牌 =不知道之前不知道扎得哪张牌的次数
				if(canZhaMap.size()> zhaValCount){
					arr[HbgzpConstants.ACTION_INDEX_ANGANG] = 1;// 可以暗杠
				}
			}
			
		}
		Map<Integer, Integer> pengMap = HbgzpHelper.toMajiangValMap(this,mGang);
		for (Hbgzp handMajiang : handPais) {
			if (pengMap.containsKey(handMajiang.getVal()) && kaizhaoVal == handMajiang.getVal()) {
				// 有招过
				arr[HbgzpConstants.ACTION_INDEX_HUA] = 1;// 可以杠
				break;
			}
		}
		if (handMap.containsValue(5)) {//要判断这个牌没有捡上来的
			arr[HbgzpConstants.ACTION_INDEX_HUA] = 1;// 可以滑
		}
	
		for (int val : arr) {
			list.add(val);
		}
		if (list.contains(1)) {
			return list;
		} else {
			return Collections.emptyList();
		}
	}

	/**
	 * 操作
	 * 
	 * @param index
	 *            0点炮1点杠2明杠3暗杠4被碰5被杠6胡7自摸
	 * @param val
	 */
	public void changeAction(int index, int val) {
		actionArr[index] += val;
		actionTotalArr[index] += val;
		getPlayingTable().changeExtend();
	}

	public List<Hbgzp> getPeng() {
		return peng;
	}

	public List<Hbgzp> getaGang() {
		return aGang;
	}

	public Set<Integer> getZhaVal(){
		return zhaVal;
	}
	public Integer getZhaCount() {
		return zhaCount;
	}
	public void reduceZhaCount() {
		this.zhaCount -= 1;
	}
	public void setZhaCount(Integer zhaCount) {
		this.zhaCount = zhaCount;
	}

	public List<Hbgzp> getmGang() {
		return mGang;
	}
	
	public List<Hbgzp> getChi() {
		return chi;
	}

	/**
	 * 吃过的牌 ID集合  ---捡
	 * @return
	 */
	public List<Integer> getChiIds(){
		if(chi == null || chi.size() <= 0){
			return new ArrayList<>();
		}
		List<Integer> list = new ArrayList<>();
		for (Hbgzp gzp : chi) {
			list.add(gzp.getId());
		}
		return list;
	}
	
	/**
	 * 是否吃过这张牌
	 * @param card
	 * @return
	 */
	public boolean isChiCard(Hbgzp card){
		if(chi == null || chi.size() <= 0){
			return false;
		}
		for (Hbgzp gzp : chi) {
			if(gzp.getId()  == card.getId()){
				return true;
			}
		}
		return false;
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
	public boolean isPassGang(Hbgzp majiang) {
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
		if (table != null)
			table.changeExtend();
	}

	@Override
	public void endCompetition1() {
		// TODO Auto-generated method stub

	}
	
	public static final List<Integer> wanfaList = Arrays.asList(
            GameUtil.play_type_hubai_gezipai);

    public static void loadWanfaPlayers(Class<? extends Player> cls){
        for (Integer integer:wanfaList){
            PlayerManager.wanfaPlayerTypesPut(integer,cls, HbgzpCommandProcessor.getInstance());
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
		HbgzpTable table = getPlayingTable(HbgzpTable.class);
		long now = System.currentTimeMillis();
		boolean auto = isAutoPlay();
		if (!auto && table.getIsAutoPlay() >= 1) {
			//检查玩家是否进入系统托管状态
			if (!checkAutoPlay&& table.getTableStatus()!= HbgzpConstants.TABLE_STATUS_CHUI) {
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
			} else if (timeOut >= HbgzpConstants.AUTO_CHECK_TIMEOUT) {
				if (sendAutoTime == 0) {
					sendAutoTime = now;
					setLastCheckTime(now - HbgzpConstants.AUTO_CHECK_TIMEOUT * 1000);
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
					if (timeOut >= HbgzpConstants.AUTO_READY_TIME) {
						setAutoPlayTime(0);
						return true;
					}
				} else if (autoType == 2) {
					if (timeOut >= HbgzpConstants.AUTO_HU_TIME) {
						setAutoPlayTime(0);
						return true;
					}
				} else {
					if (timeOut >= HbgzpConstants.AUTO_PLAY_TIME) {
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
		HbgzpTable table = getPlayingTable(HbgzpTable.class);
		if (this.autoPlay != autoPlay || autoPlaySelf != isSelf) {
			ComMsg.ComRes.Builder res = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_tuoguan, seat, autoPlay ? 1 : 0, 0, isSelf ? 1 : 0);
			GeneratedMessage msg = res.build();
			if(table != null) {
				table.broadMsgToAll(msg);
			}
			if (!getHandPais().isEmpty()) {
            table.addPlayLog(table.getDisCardRound() + "_" +getSeat() + "_" + HbgzpConstants.action_tuoguan + "_" +(autoPlay?1:0)+ getExtraPlayLog());
			}

            LogUtil.msg("setAutoPlay|" + (table == null ? -1 : table.getIsAutoPlay()) + "|" + getSeat() + "|" + getUserId() + "|" + (autoPlay ? 1 : 0) + "|" + (isSelf ? 1 : 0));
		}
			
		this.autoPlay = autoPlay;
		this.autoPlaySelf = autoPlay && isSelf;
		this.checkAutoPlay = autoPlay;
		setLastCheckTime(System.currentTimeMillis());
		if(table != null) {
			table.changeExtend();
		}
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
	            table.addPlayLog(getSeat(), HbgzpConstants.action_tuoguan + "",(autoPlay?1:0) + "");
	        }
	        this.autoPlay = autoPlay;
	        this.setCheckAuto(false);
	        if(!autoPlay){
	            setAutoPlayCheckedTimeAdded(false);
	        }
	    }
	 public boolean isCheckAuto() {
	        return isCheckAuto;
	    }
	 public void setCheckAuto(boolean checkAuto) {
	        isCheckAuto = checkAuto;
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
		if(getPlayingTable() != null){
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
		if(getPlayingTable() != null){
			getPlayingTable().changeExtend();
		}
	}

	/**
	 * 回放日志的额外信息
	 * @return
	 */
	public String getExtraPlayLog() {
		return "_" + (isAutoPlay() ? 1 : 0) + "," + (isAutoPlaySelf() ? 1 : 0);
	}

    /**
     * 过滤不需要给前端显示的大胡
     * @param daHuList
     * @return
     */
    public static List<Integer> filterDaHu(List<Integer> daHuList) {
	    List<Integer> daHu = new ArrayList<>();
        if (daHuList != null && !daHuList.isEmpty()) {
            if(daHuList.contains(HbgzpConstants.HU_PINGHU)){
                daHu.add(HbgzpConstants.HU_PINGHU);
            }
        }
        return daHu;
    }
    /**
     * 获取所有牌本身的子
     * @return
     */
    public int getOutPaiSuanZi(){
    	int num = 0;
    	for (HbgzpCardDisType type:getCardTypes()){
    		for(Integer id:type.getCardIds()){
    			Hbgzp gzp = Hbgzp.getPaohzCard(id);
    			num+=gzp.getSuanzi();
    		}
    	}
    	return num;
    }
    /**
     * 获取所有牌本身的子
     * @return
     */
    public int getAllPaiSuanZi(){
    	int num = 0;
    	for (HbgzpCardDisType type:getCardTypes()){
            for(Integer id:type.getCardIds()){
            	Hbgzp gzp = Hbgzp.getPaohzCard(id);
            	num+=gzp.getSuanzi();
            }
        }
        List<Hbgzp> handCards = HbgzpTool.toPhzCards(getHandPais());
        for (Hbgzp paohzCard:handCards) {
        	num+=paohzCard.getSuanzi();
        }
        return num;
    }
    /**
     * 获取所有牌本身的子
     * @return
     */
    public int getAllPaiSuanZi(List<Hbgzp> handCards){
    	int num = 0;
    	for (HbgzpCardDisType type:getCardTypes()){
    		for(Integer id:type.getCardIds()){
    			Hbgzp gzp = Hbgzp.getPaohzCard(id);
    			num+=gzp.getSuanzi();
    		}
    	}
    	if(handCards != null && handCards.size() > 0){
    		for (Hbgzp paohzCard:handCards) {
    			num+=paohzCard.getSuanzi();
    		}
    	}
    	return num;
    }
    public HbgzpHuLack checkHuNew(Hbgzp card, boolean isSelfMo) {
        HbgzpHuLack lack=new HbgzpHuLack();
        List<Hbgzp> handCopy = new ArrayList<>(handPais);
        if (card != null && !handCopy.contains(card)) {
            handCopy.add(card);
        }
        HbgzpTable table = getPlayingTable(HbgzpTable.class);
        List<Integer> chiList = new ArrayList<>(getChiIds());
        if(!isSelfMo){
        	chiList.add(card.getId());
        }
        List<HbgzpHuLack> list = HbgzpTool.isHuNew1(HbgzpTool.getPaohuziHandCardBean(handCopy,chiList), card, this,isSelfMo);
        if (list!=null&&!list.isEmpty()){
            for(HbgzpHuLack hu:list){
                hu.setHuxi(hu.calcHuxi());
//                int allHuxi = hu.getHuxi() + getOutCardsHuxi() + getAllPaiSuanZi()+(card==null?0:card.getSuanzi());
                int allHuxi = hu.getHuxi() + getOutCardsHuxi() + getAllPaiSuanZi(handCopy);
                if (allHuxi >= table.getHupaizi()){
                    //此处不需要找出最大胡息，只需告诉前端可以胡牌即可
                    return hu;
                }
            }
        }
        return lack;
    }
    public HbgzpHuLack checkHuNewTing(Hbgzp card, boolean isSelfMo,List<Hbgzp> handCopy) {
    	HbgzpHuLack lack=new HbgzpHuLack();
    	if (card != null && !handCopy.contains(card)) {
    		handCopy.add(card);
    	}
    	HbgzpTable table = getPlayingTable(HbgzpTable.class);
    	List<Integer> chiList = new ArrayList<>(getChiIds());
//    	if(!isSelfMo){
//    		chiList.add(card.getId());
//    	}
    	List<HbgzpHuLack> list = HbgzpTool.isHuNew1(HbgzpTool.getPaohuziHandCardBean(handCopy,chiList), card, this,isSelfMo);
    	if (list!=null&&!list.isEmpty()){
    		for(HbgzpHuLack hu:list){
    			hu.setHuxi(hu.calcHuxi());
//    			int allHuxi = hu.getHuxi() + getOutCardsHuxi() + getAllPaiSuanZi()+(card==null?0:card.getSuanzi());
    			int allHuxi = hu.getHuxi() + getOutCardsHuxi() + getAllPaiSuanZi(handCopy);
    			if (allHuxi >= table.getHupaizi()){
    				//此处不需要找出最大胡息，只需告诉前端可以胡牌即可
    				return hu;
    			}
    		}
    	}
    	return lack;
    }
    
    public List<HbgzpHuLack> checkHu1(Hbgzp card, boolean isSelfMo) {
        List<Hbgzp> handCopy = new ArrayList<>(handPais);
        if (card != null && !handCopy.contains(card)) {
            handCopy.add(card);
        }
        List<Integer> chiList = new ArrayList<>(getChiIds());
        if(!isSelfMo){
        	chiList.add(card.getId());
        }
        List<HbgzpHuLack> list = HbgzpTool.isHuNew1(HbgzpTool.getPaohuziHandCardBean(handCopy,chiList), card,this, isSelfMo);
        if(list!=null&&!list.isEmpty()){
            for (HbgzpHuLack lack:list){
                lack.setHuxi(lack.calcHuxi());
            }
        }
        return list;
    }
    
    public int getOutHuxi() {
//        return outHuXi;
    	return getOutCardsHuxi();
    }

    public void setOutHuxi(int chiHuxi) {
        this.outHuXi = chiHuxi;
        changeTableInfo();
    }

    public int getOutCardHuxi() {
    	
    	int zi = 0;
    	int huxi = 0;
    	int heipengCount = 0;
    	for (HbgzpCardDisType type:getCardTypes()){
    		for(Integer id:type.getCardIds()){
    			Hbgzp gzp = Hbgzp.getPaohzCard(id);
    			zi+=gzp.getSuanzi();
    		}
    		huxi += type.getHuxi();
    		if(type.getAction() == HbgzpDisAction.action_peng){
    			Hbgzp gzp = Hbgzp.getPaohzCard(type.getCardIds().get(0));
    			if(!Hbgzp.isHongpai(gzp.getVal())){
    				heipengCount++;
    			}
    		}
    	}
    	
    	return zi+huxi + heipengCount/3;
    }
    
    public int getOutCardsHuxi() {
    	
    	int huxi = 0;
    	int heipengCount = 0;
    	for (HbgzpCardDisType type:getCardTypes()){
    		huxi += type.getHuxi();
    		if(type.getAction() == HbgzpDisAction.action_peng){
    			Hbgzp gzp = Hbgzp.getPaohzCard(type.getCardIds().get(0));
    			if(!Hbgzp.isHongpai(gzp.getVal())){
    				heipengCount++;
    			}
    		}
    	}
    	return huxi +heipengCount/3;
    }
    
    public void changeOutCardHuxi(int huxi) {
        if (huxi != 0) {
            this.outHuXi += huxi;
            changeTableInfo();
        }
    }
    public int getHuxi() {
        return huxi;
    }

    public void setHuxi(int huxi) {
        if (this.huxi != huxi) {
            this.huxi = huxi;
        }
    }

    public List<PhzHuCards> buildNormalPhzHuCards() {
        List<PhzHuCards> list = new ArrayList<>();
        for (HbgzpCardDisType type : cardTypes) {
            list.add(type.buildMsg().build());
        }

        if (hu != null && hu.getPhzHuCards() != null) {
            for (HbgzpCardDisType type : hu.getPhzHuCards()) {
                list.add(type.buildMsg().build());
            }
        } else {
            PhzHuCards.Builder msg = PhzHuCards.newBuilder();
            msg.addAllCards(handPais.stream().map(v -> v.getId()).collect(Collectors.toList()));
            msg.setAction(0);
            msg.setHuxi(0);
            list.add(msg.build());
        }


        return list;
    }
    public List<PhzHuCards> buildPhzHuCards() {
        List<PhzHuCards> list = new ArrayList<>();
        for (HbgzpCardDisType type : cardTypes) {
            list.add(type.buildMsg().build());
        }
        if (hu != null && hu.getPhzHuCards() != null) {
            for (HbgzpCardDisType type : hu.getPhzHuCards()) {
            	list.add(type.buildMsg().build());
            }
        }

        return list;
    }
    public HbgzpHuLack getHu() {
        return hu;
    }

    public void setHu(HbgzpHuLack hu) {
        this.hu = hu;
    }
    /**
     * 总胡息
     *
     * @return
     */
    public int getTotalHu() {
        return getOutCardsHuxi() + huxi + getAllPaiSuanZi();
    }

	public List<HbgzpCardDisType> getCardTypes() {
		return cardTypes;
	}

	public void setKaizhaoVal(Integer kaizhaoVal) {
		this.kaizhaoVal = kaizhaoVal;
	}

	public Integer getChiVal() {
		return chiVal;
	}
	public void clearChiVal() {
		this.chiVal = 0;
	}
	 public int[] getActionTotalArr() {
	        return actionTotalArr;
	    }
	 
	 public int getPiaoFen() {
	        return piaoFen;
	    }

	    public void setPiaoFen(int piaoFen) {
	        this.piaoFen = piaoFen;
	    }
	    
	    
	    public int getdPiaofen() {
			return dPiaofen;
		}

		public void setdPiaofen(int dPiaofen) {
			this.dPiaofen = dPiaofen;
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
	        if (table!=null&&table.isGoldRoom()){
	            getGoldPlayer().changePlayCount();
	            if(point > 0){
	                getGoldPlayer().changeWinCount();
	                if(!isRobot()){
	                    GoldDao.getInstance().updateGoldUserCount(userId,1,0,0,1);
	                }
	            }else{
	                getGoldPlayer().changeLoseCount();
	                if(!isRobot()){
	                    GoldDao.getInstance().updateGoldUserCount(userId,0,1,0,1);
	                }
	            }
	        }
	    }	

}
