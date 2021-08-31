package com.sy599.game.qipai.ldsphz.bean;

import com.google.protobuf.GeneratedMessage;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.db.bean.group.GroupUser;
import com.sy599.game.db.dao.gold.GoldDao;
import com.sy599.game.db.dao.group.GroupDao;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.TablePhzResMsg.ClosingPhzPlayerInfoRes;
import com.sy599.game.msg.serverPacket.TablePhzResMsg.PhzHuCards;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes;
import com.sy599.game.qipai.ldsphz.command.LdsPhzCommandProcessor;
import com.sy599.game.qipai.ldsphz.util.*;
import com.sy599.game.util.GameUtil;
import com.sy599.game.util.JsonWrapper;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.util.StringUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.*;

public class LdsPhzPlayer extends Player {
    // 座位id
    private int seat;
    // 状态
    private SharedConstants.player_state state;// 1进入 2已准备 3正在玩 4已结束
    private int isEntryTable;
    private int outHuXi;// 需要记下吃的胡息
    private int zaiHuxi;
    private int huxi;
    private int winCount;
    private int lostCount;
    private int lostPoint;
    private int point;
    private int oweCardCount;// 每出4张牌一次 就欠一张牌
    //	private PaohuziHuLack hu;
    private List<LdsPhzCardTypeHuxi> cardTypes;

    public List<LdsPhzCardTypeHuxi> getCardTypes() {
        return cardTypes;
    }

    private boolean isFristDisCard;
    private int action;
    //	private List<Integer> firstPais = new ArrayList<>();//初始手牌
    private LdsPhzHandCards handCards = new LdsPhzHandCards();
    private LdsPhzCardResult huResult;
    private List<Integer> pointList = new ArrayList<>();
    private int canHuState = 1;//1可以胡牌0死守
    
	//0胡1自摸2提3跑
	private int[] actionTotalArr = new int[4];

    private volatile boolean autoPlay = false;//托管
    private volatile long lastOperateTime = 0;//最后操作时间
    private volatile long lastCheckTime = 0;//最后检查时间

    private volatile long autoPlayTime = 0;//自动操作时间
    
    private int passHuVal;

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
//        this.autoPlay = autoPlay;
//
//        //广播托管消息
//        if (table==null){
//            table=getPlayingTable();
//        }
//        if (table!=null){
//            ComMsg.ComRes.Builder res = SendMsgUtil.buildComRes(autoPlay? WebSocketMsgType.req_com_code_cccanceltrusteeship:WebSocketMsgType.req_com_code_canceltrusteeship, seat, (int)userId);
//            GeneratedMessage msg = res.build();
//            for (Map.Entry<Long,Player> kv:table.getPlayerMap().entrySet()){
//                Player player=kv.getValue();
//                if (player.getIsOnline() == 0) {
//                    continue;
//                }
//                player.writeSocket(msg);
//            }
//        }
        
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
			
       		 table.addPlayLog(getSeat(), LdsPhzConstants.action_tuoguan + "",(autoPlay?1:0) + "");
		}
		this.autoPlay = autoPlay;
		//this.setCheckAuto(false);
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
    }

    public int getCanHuState() {
        return canHuState;
    }

    public void setCanHuState(int canHuState) {
        if (this.canHuState != canHuState && canHuState == 0) {
            LogUtil.monitorLog.warn("sishou:tableId=" + getPlayingTableId() + ",userId=" + userId);
        }
        this.canHuState = canHuState;
        changeExtend();
    }

    public int getPassHuVal() {
    	if(passHuVal==0){
    		return 0;
    	}
		return passHuVal/100;
	}
    
    
    public boolean checkCanhuPass(int passVal,int zimo) {
    	int passHuValzi = getPassHuValZimo();
    	if(passVal==getPassHuVal()){
    		//过掉了自摸的牌
    		if(passHuValzi==1){
    			return false;
    		}
    		else if(passHuValzi==0){
    			if(zimo==1){
    				return true;
    			}
    			return false;
    		}
    	}
		return true;
	}
    
    
    public int getPassHuValZimo() {
    	if(passHuVal==0){
    		return 0;
    	}
		return passHuVal%100;
	}

	public void setPassHuVal(int passHuVal,int zimo) {
		if(passHuVal>0){
			int c = passHuVal*100+zimo;
			this.passHuVal = c;
		}else{
			this.passHuVal = passHuVal;
		}
	}

	public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public LdsPhzPlayer() {
        cardTypes = new ArrayList<>();
        autoPlay = false;
		autoPlayTime = 0;
	//	lastCheckTime = System.currentTimeMillis();
    }

    public LdsPhzHandCards getHandCards() {
        return handCards;
    }

    /**
     * 移掉出的牌
     *
     * @param id
     */
    public void removeOutPais(Integer id) {
        handCards.OUTS.remove(id);
    }

    /**
     * 摸牌
     *
     * @param id
     */
    public void outCardOfMo(Integer id, LdsPhzTable table) {
        if (LdsPhzCardUtils.commonCard(id)){
            handCards.OUTS.add(id);

            int val = LdsPhzCardUtils.loadCardVal(id);
            for(Player player:table.getPlayerMap().values()){
                LdsPhzPlayer player1=(LdsPhzPlayer) player;
                player1.getHandCards().NOT_PENG.add(val);
                if (table.getDisCardSeat()==player1.getSeat()||table.getNowDisCardSeat()==player1.getSeat()){
                    player1.passVal(LdsPhzConstants.action_chi,val);
                }
                player1.changeExtend();
            }

            changeSeat(seat);
            changeTableInfo();
        }
    }

    public void moWang(Integer id) {
        handCards.WANGS.add(id);
        changeSeat(seat);
    }

    public void disCard(int action, List<Integer> disCardList) {
//		for (Integer disCard : disCardList) {
//			if (!handCards.INS.remove(disCard)) {
//				int val=LdsPhzCardUtils.loadCardVal(disCard);
//				if (handCards.KAN.remove(val)==null)
//				if (handCards.WEI.remove(val)==null) {
//					if (handCards.WEI_CHOU.remove(val)==null)
//						if (handCards.PENG.remove(val)==null) {
//						if (handCards.PAO.remove(val)==null) {
//						}
//					}
//				}
//			}
//		}

        if (disCardList.size() > 1) {
            Integer card = disCardList.get(1);
            Iterator<LdsPhzCardTypeHuxi> iterator = cardTypes.iterator();
            while (iterator.hasNext()) {
                LdsPhzCardTypeHuxi type = iterator.next();
                if (type.isHasCard(card)) {
                    if (type.getAction() == LdsPhzConstants.action_zai) {
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

        List<Integer> copy = new ArrayList<>(disCardList);
        int val = LdsPhzCardUtils.loadCardVal(disCardList.get(0));
        if (action == LdsPhzConstants.action_ti) {
            handCards.TI.put(val, copy);
            handCards.KAN.remove(val);
            handCards.WEI.remove(val);
        } else if (action == LdsPhzConstants.action_zai) {
            handCards.WEI.put(val, copy);
            handCards.INS.removeAll(copy);
        } else if (action == LdsPhzConstants.action_chouzai) {
            handCards.WEI_CHOU.put(val, copy);
            handCards.INS.removeAll(copy);
        } else if (action == LdsPhzConstants.action_peng) {
            handCards.PENG.put(val, copy);
            handCards.INS.removeAll(copy);
        } else if (action == LdsPhzConstants.action_chi) {
            if (copy.size() % 3 == 0) {
                for (int i = 0; i < copy.size() / 3; i++) {
                    List<Integer> list = new ArrayList<>(copy.subList(i * 3, i * 3 + 3));
                    if (LdsPhzCardUtils.loadCardCount(list).size() == 3) {
                        handCards.CHI_COMMON.add(list);
                    } else {
                        handCards.CHI_JIAO.add(list);
                    }
                }
            }
            handCards.INS.removeAll(copy);
        } else if (action == LdsPhzConstants.action_pao) {
            handCards.PAO.put(val, copy);
            handCards.KAN.remove(val);
            handCards.WEI.remove(val);
            handCards.PENG.remove(val);
        } else {
            handCards.OUTS.addAll(copy);
            handCards.INS.removeAll(copy);
            handCards.NOT_PENG.add(val);
            handCards.NOT_CHI.add(val);
        }

        if (disCardList.size() > 3 && disCardList.size() % 3 == 0) {
            for (int i = 0; i < disCardList.size() / 3; i++) {
                List<Integer> list = disCardList.subList(i * 3, i * 3 + 3);
                addCardType(action, list);
            }

        } else {
            addCardType(action, disCardList);
        }
        changeSeat(seat);
        changeTableInfo();
    }

    public void addCardType(int action, List<Integer> disCardList) {
        int huxi = LdsPhzCardUtils.countHuxi0(new ArrayList<>(disCardList));
        if (action != 0) {
            LdsPhzCardTypeHuxi type = new LdsPhzCardTypeHuxi();
            type.setAction(action);
            type.setCardIds(disCardList);
            type.setHux(huxi);
            cardTypes.add(type);

            if (action == LdsPhzConstants.action_zai || action == LdsPhzConstants.action_chouzai || action == LdsPhzConstants.action_ti) {
                changeZaihu(huxi);
            } else {
                changeOutCardHuxi(huxi);
            }
        }
    }

    /**
     * 是否需要出牌
     *
     * @return
     */
    public boolean isNeedDisCard(int action) {
        int ct = (handCards.INS.size() + handCards.WANGS.size()) % 3;
        if (handCards.hasFourSameCard()) {
            return ct == 2;
        } else {
            return ct == 0;
        }
    }

    /**
     *
     */
    public void compensateCard() {
        oweCardCount++;
        changeTableInfo();
    }

    public void passVal(int action, int val) {
        if (LdsPhzCardUtils.commonCardByVal(val)){
            if (action == LdsPhzConstants.action_chi) {
                handCards.NOT_CHI.add(val);
                changeExtend();
                changeTableInfo();
            } else if (action == LdsPhzConstants.action_peng) {
                handCards.NOT_PENG.add(val);
                changeExtend();
                changeTableInfo();
            }
        }
    }

//    public LdsPhzCheckCardBean checkTi() {
//        LdsPhzCheckCardBean check = new LdsPhzCheckCardBean();
//        check.setSeat(seat);
//        if (handCards.TI.size() + handCards.PAO.size() > 0) {
////            List<Integer> list=new ArrayList<>();
////            for (List<Integer> temp:handCards.TI.values()){
////                list.addAll(temp);
////            }
////            for (List<Integer> temp:handCards.PAO.values()){
////                list.addAll(temp);
////            }
////            check.setAuto(LdsPhzConstants.action_ti, list);
////            check.setTi(true);
//        }
//        check.buildActionList();
//        return check;
//    }

    public List<Integer> getCalcOverCards() {
        List<Integer> list = new ArrayList<>();
        if (huResult != null && huResult.isCanHu()) {
            for (LdsPhzCardMessage temp : huResult.getCardMessageList()) {
                list.addAll(temp.getCards());
            }
        }

        return list;
    }

    @Override
    public void initPlayInfo(String data) {
        try {
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
                this.outHuXi = StringUtil.getIntValue(values, i++);
                this.huxi = StringUtil.getIntValue(values, i++);
                this.winCount = StringUtil.getIntValue(values, i++);
                this.lostCount = StringUtil.getIntValue(values, i++);
                this.lostPoint = StringUtil.getIntValue(values, i++);
                this.point = StringUtil.getIntValue(values, i++);
                setTotalPoint(StringUtil.getIntValue(values, i++));
                this.oweCardCount = StringUtil.getIntValue(values, i++);
                this.isFristDisCard = StringUtil.getIntValue(values, i++) == 1;
                setMaxPoint(StringUtil.getIntValue(values, i++));
                this.zaiHuxi = StringUtil.getIntValue(values, i++);
                this.pointList = str2IntList(StringUtil.getValue(values, i++), "_");
                this.canHuState = values.length > i ? Integer.parseInt(values[i++]) : 1;
                
            	String actionTotalArrStr = StringUtil.getValue(values, i++);
                if (!StringUtils.isBlank(actionTotalArrStr)) {
    				this.actionTotalArr = StringUtil.explodeToIntArray(actionTotalArrStr, "_");
    			}
                
                this.passHuVal = StringUtil.getIntValue(values, i++);
                
                
            }
        } catch (Exception e) {
            LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
        }
    }

    @Override
    public void changeState(SharedConstants.player_state state) {
        this.state = state;
        changeTableInfo();
    }

    @Override
    public void clearTableInfo() {
        BaseTable table = getPlayingTable();
        boolean isCompetition = false;
        if (table != null && table.isCompetition()) {
            isCompetition = true;
            endCompetition();
        }
        handCards = new LdsPhzHandCards();
        setIsEntryTable(0);
        changeIsLeave(0);
        changeState(null);
        cardTypes.clear();
        setZaiHuxi(0);
        setHuResult(null);
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
        actionTotalArr = new int[4];
        pointList.clear();
        setAutoPlay(false,table);
        setCanHuState(1);
        saveBaseInfo();
        setPassHuVal(0,0);
    }

    
	public int[] getActionTotalArr() {
		return actionTotalArr;
	}
    @Override
    public SharedConstants.player_state getState() {
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

    public boolean isCanDisCard(List<Integer> disCards, Integer nowDisCard, int action) {
        if (disCards != null) {
            if (action == LdsPhzConstants.action_zai || action == LdsPhzConstants.action_chouzai) {
                return disCards.size() == 2 && handCards.INS.containsAll(disCards);
            } else if (action == LdsPhzConstants.action_peng) {
                int val = LdsPhzCardUtils.loadCardVal(nowDisCard);
                return LdsPhzCardUtils.loadIdsByVal(handCards.INS, val).size() == 2;
            } else if (action == LdsPhzConstants.action_ti) {
                int val = LdsPhzCardUtils.loadCardVal(nowDisCard);
                return disCards.size() == 3 && (handCards.KAN.containsKey(val) || handCards.WEI.containsKey(val));
            } else if (action == LdsPhzConstants.action_pao) {
                int val = LdsPhzCardUtils.loadCardVal(nowDisCard);
                return disCards.size() == 3 && (handCards.KAN.containsKey(val) || handCards.WEI.containsKey(val) || handCards.PENG.containsKey(val));
            } else if (action == LdsPhzConstants.action_chi) {
                if (disCards.contains(nowDisCard)) {
                    List<Integer> list = new ArrayList<>(disCards);
                    list.remove(nowDisCard);
                    return handCards.INS.containsAll(list);
                }
                return handCards.INS.containsAll(disCards);
            } else {
                return handCards.INS.containsAll(disCards) || handCards.OUTS.containsAll(disCards);
            }
        }
        return true;
    }

//	/**
//	 * 不加入能跑的牌是否能胡
//	 *
//	 * @param card
//	 * @param isSelfMo
//	 * @return
//	 */
//	public PaohuziHuLack checkPaoHu(PaohzCard card, boolean isSelfMo, boolean canDiHu) {
//		int addhuxi = 0;
//		int action = 0;
//
//		List<PaohzCard> handCopy = new ArrayList<>(handPais);
//
//		if (card != null) {
//
//			LdsPhzTable table = getPlayingTable(LdsPhzTable.class);
//			boolean isMoFlag = table.isMoFlag();
//			List<PaohzCard> sameList = PaohuziTool.getSameCards(handPais, card);
//
//			if (isSelfMo) {
//				sameList.remove(card);
//			}
//
//			if (sameList.size() >= 3) {
//				if (isSelfMo) {
//					action = LdsPhzConstants.action_ti;
//				} else {
//					action = LdsPhzConstants.action_pao;
//				}
//			}
//
//			List<Integer> zaiVals = PaohuziTool.toPhzCardVals(zai, true);
//			if (isMoFlag && !peng.contains(card) && PaohuziTool.isHasCardVal(peng, card.getVal())) {
//				// 检查是否碰过的牌 跑过的牌能跑的话必须要是摸的牌
//
//				if (card.isBig()) {
//					// 大的9 小的6 碰大的3 小的1
//					addhuxi = 9 - 3;
//				} else {
//					addhuxi = 6 - 1;
//				}
//				action = LdsPhzConstants.action_pao;
//			} else if (isMoFlag && !zai.contains(card) && zaiVals.contains(card.getVal())) {
//				// 如果是栽过的牌别人打的或者摸的能提或者跑
//				// /////////////////////////////////////////////////////
//
//				if (isSelfMo) {
//					// 如果是栽过的牌自己再摸一张上来就是提
//					// 大的12 小的9 栽大的6 小的3
//					action = LdsPhzConstants.action_ti;
//					if (card.isBig()) {
//						addhuxi = 12 - 6;
//					} else {
//						addhuxi = 9 - 3;
//					}
//
//				} else {
//					// 别人打的或摸的就是跑
//					action = LdsPhzConstants.action_pao;
//					if (card.isBig()) {
//						// 大的9 小的6 栽大的6 小的3
//						addhuxi = 9 - 6;
//					} else {
//						addhuxi = 6 - 3;
//					}
//				}
//
//			} else if (isMoFlag || canDiHu) {
//				// 在手上的牌
//				List<PaohzCard> sameCard = PaohuziTool.getSameCards(handCopy, card);
//				if (sameCard.size() >= 3) {
//					if (card.isBig()) {
//						// 大的9 小的6
//						addhuxi = 9;
//					} else {
//						addhuxi = 6;
//					}
//					handCopy.removeAll(sameCard);
//				}
//			}
//		}
//
//		int totalHuxi = outHuXi + zaiHuxi + addhuxi;
//
//		PaohuziHuLack lack = PaohuziTool.isHu_3(PaohuziTool.getPaohuziHandCardBean(handCopy), null, isSelfMo, totalHuxi, true, true, this);
//		if (lack.isHu()) {
//			if (action != 0) {
//				lack.setPaohuAction(action);
//				List<PaohzCard> paohuList = new ArrayList<>();
//				paohuList.add(card);
//				lack.setPaohuList(paohuList);
//			}
//
//			if (lack.getHuxi() + totalHuxi < 15) {
//				// 15胡息能胡
//				lack.setHu(false);
//			} else if (action == 0) {
//				lack.setHu(false);
//			} else {
//				setHuxi(lack.getHuxi());
//			}
//
//		}
//
//		return lack;
//	}

//	public PaohuziHuLack checkHu(PaohzCard card, boolean isSelfMo) {
//		List<PaohzCard> handCopy = new ArrayList<>(handPais);
//		boolean isPaoHu = true;
//		if (card != null && !handCopy.contains(card)) {
//			handCopy.add(card);
//			isPaoHu = false;
//
//		}
//		PaohuziHuLack lack = PaohuziTool.isHu_3(PaohuziTool.getPaohuziHandCardBean(handCopy), card, isSelfMo, (outHuXi + zaiHuxi), isNeedDui(), isPaoHu, this);
//		if (lack.isHu()) {
//			if (lack.getHuxi() + outHuXi + zaiHuxi < 15) {
//				// 15胡息能胡
//				lack.setHu(false);
//			} else {
//				setHuxi(lack.getHuxi());
//			}
//
//		}
//		return lack;
//	}
//
//	public PaohuziHuLack checkWangDiaoOrChuang(PaohzCard disCard, boolean before) {
//		List<PaohzCard> handCopy = new ArrayList<>(handPais);
//		if (disCard != null && !handCopy.contains(disCard) && !before) {
//			handCopy.add(disCard);
//		}
//		PaohuziHuLack lack = null;
//
//		int outCardHuxi = outHuXi + zaiHuxi;
//		PaohuziHandCard handCardBean = PaohuziTool.getPaohuziHandCardBean(handCopy);
//
//		boolean isSelfMo = true;
//
//		boolean needDui = isNeedDui();
//		boolean isPaoHu = false;
//
//		lack = PaohuziTool.checkWangDiaoOrChuang(handCardBean, disCard, isSelfMo, outCardHuxi, needDui, isPaoHu, before);
//
//		if (lack.isHu()) {
//			int minHuxi = 15;
//			if (before && lack.getDiaoOrChuang() == 2) {
//				minHuxi = 12;
//			}
//			if (lack.getHuxi() + outHuXi + zaiHuxi < minHuxi) {
//				// 15胡息能胡
//				lack.setHu(false);
//				lack.setDiaoOrChuang(0);
//			} else {
//				setHuxi(lack.getHuxi());
//			}
//
//		}
//		return lack;
//	}


//	public LdsPhzCheckCardBean checkPaoHu() {
//		LdsPhzCheckCardBean check = new LdsPhzCheckCardBean();
//		check.setSeat(seat);
//
//		LdsPhzTable table = getPlayingTable(LdsPhzTable.class);
//		PaohuziHuLack lack = checkHu(null, table.isSelfMo(this));
//
//		// PaohuziHuLack lack = checkHu(null, false);
//		if (lack.isHu()) {
//			check.setLack(lack);
//			check.setHu(true);
//		}
//		check.buildActionList();
//		return check;
//	}

    public void tiLong(Integer card) {
        Integer val = LdsPhzCardUtils.loadCardVal(card);
        List<Integer> list = handCards.TI.get(val);
        if (list == null) {
            list = handCards.PAO.get(val);
        }
        if (list == null) {
            list = handCards.KAN.remove(val);
            if (list == null) {
                list = handCards.WEI.remove(val);
                if (list == null) {
                    list = handCards.PENG.remove(val);
                    if (list != null) {
                        list.add(card);
                        handCards.PAO.put(val, list);
                    }
                } else {
                    list.add(card);
                    handCards.PAO.put(val, list);
                }
            } else {
                list.add(card);
                handCards.PAO.put(val, list);
            }
        }

        compensateCard();
        changeSeat(seat);
    }

    /**
     * 检查跑、跑后胡
     *
     * @param card0
     * @param isSelfMo
     * @return
     */
    public LdsPhzCheckCardBean checkPaoHu(Integer card0, boolean isSelfMo, boolean checkHu) {
        LdsPhzCheckCardBean paohuziCheckCardBean = new LdsPhzCheckCardBean();
        paohuziCheckCardBean.setSeat(seat);

        int card = card0 == null ? 0 : card0.intValue();
        if (LdsPhzCardUtils.commonCard(card)) {
            List<Integer> list;
            int val = LdsPhzCardUtils.loadCardVal(card);
            LdsPhzTable table = getPlayingTable(LdsPhzTable.class);
            boolean isMoFlag = table.isMoFlag();
            //&& isMoFlag
            if (isSelfMo ) {
                list = handCards.PENG.get(val);
                if (list != null&&!list.contains(card)) {
//                    result[0] = true;
                    paohuziCheckCardBean.setPao(true);
                    LdsPhzHandCards handCards1 = handCards.copy();
                    list = handCards1.PENG.remove(val);
                    list.add(card);
                    handCards1.PAO.put(val, list);
                    if (canHuState == 1 && checkHu) {
                        LdsPhzCardResult cardResult = LdsPhzHuPaiUtils.huPai(handCards1, 0, table, isSelfMo, userId, table.loadXingCard(false));
                        if (cardResult.isCanHu() && !cardResult.isWangZha() && !cardResult.isWangChuang() && !cardResult.isWangDiao()) {
//                            result[1] = true;
                            paohuziCheckCardBean.setHu(true);
                            paohuziCheckCardBean.setCardResult(cardResult);
                        }
                    }
                }
            } else if (!isSelfMo) {
                list = handCards.KAN.get(val);
                if (list != null&&!list.contains(card)) {
//                    result[0] = true;
                    paohuziCheckCardBean.setPao(true);
                    LdsPhzHandCards handCards1 = handCards.copy();
                    list = handCards1.KAN.remove(val);
                    list.add(card);
                    handCards1.PAO.put(val, list);
                    if (canHuState == 1 && checkHu) {
                        LdsPhzCardResult cardResult = LdsPhzHuPaiUtils.huPai(handCards1, 0, table, isSelfMo, userId, table.loadXingCard(false));
                        if (cardResult.isCanHu() && !cardResult.isWangZha() && !cardResult.isWangChuang() && !cardResult.isWangDiao()) {
//                            result[1] = true;
                            paohuziCheckCardBean.setHu(true);
                            paohuziCheckCardBean.setCardResult(cardResult);
                        }
                    }
                } else {
                    list = handCards.WEI.get(val);
                    if (list != null&&!list.contains(card)) {
//                        result[0] = true;
                        paohuziCheckCardBean.setPao(true);
                        LdsPhzHandCards handCards1 = handCards.copy();
                        list = handCards1.WEI.remove(val);
                        list.add(card);
                        handCards1.PAO.put(val, list);
                        if (canHuState == 1 && checkHu) {
                            LdsPhzCardResult cardResult = LdsPhzHuPaiUtils.huPai(handCards1, 0, table, isSelfMo, userId, table.loadXingCard(false));
                            if (cardResult.isCanHu() && !cardResult.isWangZha() && !cardResult.isWangChuang() && !cardResult.isWangDiao()) {
//                                result[1] = true;
                                paohuziCheckCardBean.setHu(true);
                                paohuziCheckCardBean.setCardResult(cardResult);
                            }
                        }
                    } else {
                        list = handCards.PENG.get(val);
                        if (list != null && isMoFlag&&!list.contains(card)) {
//                            result[0] = true;
                            paohuziCheckCardBean.setPao(true);
                            LdsPhzHandCards handCards1 = handCards.copy();
                            list = handCards1.PENG.remove(val);
                            list.add(card);
                            handCards1.PAO.put(val, list);
                            if (canHuState == 1 && checkHu) {
                                LdsPhzCardResult cardResult = LdsPhzHuPaiUtils.huPai(handCards1, 0, table, isSelfMo, userId, table.loadXingCard(false));
                                if (cardResult.isCanHu() && !cardResult.isWangZha() && !cardResult.isWangChuang() && !cardResult.isWangDiao()) {
//                                    result[1] = true;
                                    paohuziCheckCardBean.setHu(true);
                                    paohuziCheckCardBean.setCardResult(cardResult);
                                }
                            }
                        }
                    }
                }
            }
        }

        return paohuziCheckCardBean;
    }
    
    
    
    
    
    
    
    
    
    
    
    public LdsPhzCheckCardBean checkPaoHu2(LdsPhzHandCards mHandCards,Integer card0, boolean isSelfMo, boolean checkHu) {
        LdsPhzCheckCardBean paohuziCheckCardBean = new LdsPhzCheckCardBean();
        paohuziCheckCardBean.setSeat(seat);

        int card = card0 == null ? 0 : card0.intValue();
        if (LdsPhzCardUtils.commonCard(card)) {
            int val = LdsPhzCardUtils.loadCardVal(card);
            LdsPhzTable table = getPlayingTable(LdsPhzTable.class);
            //&& isMoFlag
            List<Integer> list;
            if (isSelfMo ) {
            	 list = handCards.KAN.get(val);
                 if (list != null&&!list.contains(card)) {
//                     result[0] = true;
                     paohuziCheckCardBean.setPao(true);
                     LdsPhzHandCards handCards1 = mHandCards.copy();
                     list = handCards1.KAN.remove(val);
                     list.add(card);
                     handCards1.PAO.put(val, list);
                     if (canHuState == 1 && checkHu) {
                         LdsPhzCardResult cardResult = LdsPhzHuPaiUtils.huPai(handCards1, 0, table, isSelfMo, userId, table.loadXingCard(false));
                         if (cardResult.isCanHu() && !cardResult.isWangZha() && !cardResult.isWangChuang() && !cardResult.isWangDiao()) {
//                             result[1] = true;
                             paohuziCheckCardBean.setHu(true);
                             paohuziCheckCardBean.setCardResult(cardResult);
                         }
                     }
                 } else {
            	 list = handCards.WEI.get(val);
                 if (list != null&&!list.contains(card)) {
//                     result[0] = true;
                     paohuziCheckCardBean.setPao(true);
                     LdsPhzHandCards handCards1 = mHandCards.copy();
                     list = handCards1.WEI.remove(val);
                     list.add(card);
                     handCards1.PAO.put(val, list);
                     if (canHuState == 1 && checkHu) {
                         LdsPhzCardResult cardResult = LdsPhzHuPaiUtils.huPai(handCards1, 0, table, isSelfMo, userId, table.loadXingCard(false));
                         if (cardResult.isCanHu() && !cardResult.isWangZha() && !cardResult.isWangChuang() && !cardResult.isWangDiao()) {
//                             result[1] = true;
                             paohuziCheckCardBean.setHu(true);
                             paohuziCheckCardBean.setCardResult(cardResult);
                         }
                     }
                     
                 	}else {
                 		  list = handCards.PENG.get(val);
                          if (list != null &&!list.contains(card)) {
//                              result[0] = true;
                              paohuziCheckCardBean.setPao(true);
                              LdsPhzHandCards handCards1 = mHandCards.copy();
                              list = handCards1.PENG.remove(val);
                              list.add(card);
                              handCards1.PAO.put(val, list);
                              if (canHuState == 1 && checkHu) {
                                  LdsPhzCardResult cardResult = LdsPhzHuPaiUtils.huPai(handCards1, 0, table, isSelfMo, userId, table.loadXingCard(false));
                                  if (cardResult.isCanHu() && !cardResult.isWangZha() && !cardResult.isWangChuang() && !cardResult.isWangDiao()) {
//                                      result[1] = true;
                                      paohuziCheckCardBean.setHu(true);
                                      paohuziCheckCardBean.setCardResult(cardResult);
                                  }
                              }
                          }
                 	}
                 } 

            } 
        }

        return paohuziCheckCardBean;
    }
    
    
    
    
    

    public LdsPhzCheckCardBean checkCard(Integer card, boolean isSelfMo, boolean isPassHu, boolean isBegin, boolean isFirstCard, boolean isPass) {
        return checkCard(card, isSelfMo, isPassHu, isBegin, isFirstCard, isPass, false, 0, false, -1);
    }

    public LdsPhzCheckCardBean checkCard(Integer card0, boolean isSelfMo, boolean isPassHu, boolean isBegin, boolean isFirstCard, boolean isPass, int huResult) {
        return checkCard(card0, isSelfMo, isPassHu, isBegin, isFirstCard, isPass, false, 0, false, huResult);
    }

    public LdsPhzCheckCardBean checkCard(Integer card0, boolean isSelfMo, boolean isPassHu, boolean isBegin, boolean isFirstCard, boolean isPass, boolean wangPass) {
        return checkCard(card0, isSelfMo, isPassHu, isBegin, isFirstCard, isPass, false, 0, wangPass, -1);
    }

    public LdsPhzCheckCardBean checkCard(Integer card0, boolean isSelfMo, boolean isPassHu, boolean isBegin, boolean isFirstCard, boolean isPass, boolean tiqian, Integer preCard) {
        return checkCard(card0, isSelfMo, isPassHu, isBegin, isFirstCard, isPass, tiqian, preCard, false, -1);
    }

    private int replaceRedCount(LdsPhzCardResult cardResult) {
        int count = 0;
        for (String str : cardResult.getReplaceCards()) {
            String[] strs = str.split("\\|");
            for (String s : strs) {
                if (s.length() > 0) {
                    if (LdsPhzCardUtils.redCard(Integer.parseInt(s))) {
                        count++;
                        break;
                    }
                }
            }
        }
        return count;
    }

    private boolean replaceRed(LdsPhzCardResult cardResult) {
        List<String> list = cardResult.getReplaceCards();
        if (list.size() >= 2 && list.get(list.size() - 2).equals(list.get(list.size() - 1))) {
            String[] strs = list.get(list.size() - 2).split("\\|");
            for (String s : strs) {
                if (s.length() > 0) {
                    if (LdsPhzCardUtils.redCard(Integer.parseInt(s))) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * 检查牌
     *
     * @param card0    出的牌或者是摸的牌
     * @param isSelfMo 是否是自己摸的牌
     * @return
     */
    public LdsPhzCheckCardBean checkCard(Integer card0, boolean isSelfMo, boolean isPassHu, boolean isBegin, boolean isFirstCard, boolean isPass, boolean tiqian, Integer preCard, boolean wangPass, int huResult) {
        int card = card0 == null ? 0 : card0.intValue();
        LdsPhzCheckCardBean check = new LdsPhzCheckCardBean();
        check.setSeat(seat);
        check.setDisCard(card);

        if (isSelfMo && (huResult == 0 || huResult == 1)) {
            isPassHu = true;
        }

        LdsPhzTable table = getPlayingTable(LdsPhzTable.class);
        int nextSeat = table.getNextDisCardSeat();
        boolean isMoFlag = table.isMoFlag();
        int val = LdsPhzCardUtils.loadCardVal(card);
        if (tiqian) {
            if (canHuState == 1) {
//				if (preCard!=null&&LdsPhzCardUtils.commonCard(preCard)){
//					int val=LdsPhzCardUtils.loadCardVal(preCard);
//					if(nextSeat==seat){
//						if (val<200&&!isPass&&canChi0(preCard)&&handCards.INS.size()>2){
//							if (isMoFlag){
//								check.setChi(true);
//							}else{
//								List<Integer> list=new ArrayList<>(((LdsPhzPlayer)table.getSeatMap().get(table.calcFrontSeat(seat))).getHandCards().OUTS);
//                                list.remove(Integer.valueOf(table.getDisposeCard()));
//								boolean bl=true;
//								for (Integer integer:list){
//								    if (val==LdsPhzCardUtils.loadCardVal(integer)){
//                                        bl=false;
//                                        break;
//                                    }
//                                }
//                                if (bl){
//                                    check.setChi(true);
//                                }
//							}
//						}
//					}
//
//					if (!isPass&&!handCards.NOT_PENG.contains(val)&&LdsPhzCardUtils.canPeng(handCards.INS,preCard)&&handCards.INS.size()>2){
//						check.setPeng(true);
//					}
//				}

                LdsPhzCardResult cardResult = LdsPhzHuPaiUtils.huPai(handCards, card, table, isSelfMo, userId, table.loadXingCard(tiqian));
                if (cardResult.isCanHu()) {
                    check.setHu(true);
                    check.setCardResult(cardResult);
                    boolean checkHu = true;
                    if ((card > 0 && !LdsPhzCardUtils.smallCard(card)) && (cardResult.isWangZha() || cardResult.isWangChuang())) {
                        int huxi = cardResult.getTotalHuxi(false);
                        if (cardResult.isWangZha()) {
                            LdsPhzCardMessage cardMessage = null;
                            for (LdsPhzCardMessage cm : cardResult.getCardMessageList()) {
                                if (cm.getHuXiEnum() == LdsPhzHuXiEnums.TI && LdsPhzCardUtils.hasCard(cm.getCards(), val) && LdsPhzCardUtils.countCard(cm.getCards(), 201) >= 3) {
                                    cardMessage = cm;
                                    break;
                                }
                            }

                            if (cardMessage == null) {
                                LogUtil.errorLog.warn("tiqianhu error:card=" + card + ",cards=" + cardResult.getCardMessageList());
                            }

                            if (cardMessage != null && huxi - cardMessage.loadHuxi() + 9 < table.loadQihuxi()) {
                                checkHu = false;

                                if ((card > 0 && !LdsPhzCardUtils.smallCard(card)) && cardResult.isWangChuang()) {
                                    huxi = cardResult.getChuangCardResult().getTotalHuxi(false);
                                    cardMessage = null;
                                    for (LdsPhzCardMessage cm : cardResult.getChuangCardResult().getCardMessageList()) {
                                        if ((cm.getHuXiEnum() == LdsPhzHuXiEnums.WEI || cm.getHuXiEnum() == LdsPhzHuXiEnums.KAN) && LdsPhzCardUtils.hasCard(cm.getCards(), val) && LdsPhzCardUtils.countCard(cm.getCards(), 201) >= 2) {
                                            cardMessage = cm;
                                            break;
                                        }
                                    }

                                    if (cardMessage == null) {
                                        LogUtil.errorLog.warn("tiqianhu error:card=" + card + ",cards=" + cardResult.getChuangCardResult().getCardMessageList());
                                    }

                                    if (cardMessage != null && huxi - cardMessage.loadHuxi() + 3 < table.loadQihuxi()) {
                                        check.setWangChuang(false);
                                    } else {
                                        if (handCards.WANGS.size() >= 4 && table.loadHuMode() == 2 && !LdsPhzCardUtils.wangCard(card) && cardResult.getChuangCardResult().getTotalFan() > 1) {
                                            int redCount = cardResult.getChuangCardResult().getRedTotal();
                                            int baseCount = cardResult.getChuangCardResult().getRedCount();
                                            int red0 = replaceRedCount(cardResult.getChuangCardResult());
                                            int red1 = LdsPhzCardUtils.redCard(card) ? red0 - 2 : red0;

                                            if (redCount <= 1 || (baseCount + red1 < table.loadRedCount() && redCount >= table.loadRedCount() + red1)) {
                                                check.setWangChuang(false);
                                            } else {
                                                check.setWangChuang(true);
                                            }
                                        } else {
                                            check.setWangChuang(true);
                                        }
                                    }
                                } else {
                                    if (cardResult.isWangChuang() && handCards.WANGS.size() >= 4 && table.loadHuMode() == 2 && !LdsPhzCardUtils.wangCard(card) && cardResult.getChuangCardResult().getTotalFan() > 1) {
                                        int redCount = cardResult.getChuangCardResult().getRedTotal();
                                        int baseCount = cardResult.getChuangCardResult().getRedCount();
                                        int red0 = replaceRedCount(cardResult.getChuangCardResult());
                                        int red1 = LdsPhzCardUtils.redCard(card) ? red0 - 2 : red0;

                                        if (redCount <= 1 || (baseCount + red1 < table.loadRedCount() && redCount >= table.loadRedCount() + red1)) {
                                            check.setWangChuang(false);
                                        } else {
                                            check.setWangChuang(true);
                                        }
                                    } else {
                                        check.setWangChuang(cardResult.isWangChuang());
                                    }
                                }

                                if (cardResult.isWangDiao() && table.loadHuMode() == 2 && cardResult.getDiaoCardResult().getTotalFan() > 1) {
                                    int redCount = cardResult.getDiaoCardResult().getRedTotal();
                                    int baseCount = cardResult.getDiaoCardResult().getRedCount();
                                    int red0 = replaceRedCount(cardResult.getDiaoCardResult());
                                    int red1;

                                    if (LdsPhzCardUtils.wangCard(card)) {
                                        if (red0 >= 3) {
                                            red1 = red0 - 2;
                                        } else {
                                            red1 = replaceRed(cardResult.getDiaoCardResult()) ? red0 - 2 : red0;
                                        }
                                    } else {
                                        red1 = LdsPhzCardUtils.redCard(card) ? red0 - 1 : red0;
                                    }
                                    switch (handCards.WANGS.size()) {
                                        case 3:
                                            if (redCount + red1 <= 1 || (baseCount + red1 < table.loadRedCount() && redCount >= table.loadRedCount() + red1)) {
                                                check.setWangDiao(false);
                                            } else {
                                                check.setWangDiao(true);
                                            }
                                            break;
                                        case 4:
                                            if (redCount + red1 <= 0 || (table.checkRed2Black() && baseCount + red1 < table.loadRed2BlackCount() && redCount >= table.loadRed2BlackCount() + red1)) {
                                                check.setWangDiao(false);
                                            } else {
                                                check.setWangDiao(true);
                                            }
                                            break;
                                        default:
                                            check.setWangDiao(true);
                                            break;
                                    }
                                } else {
                                    check.setWangDiao(cardResult.isWangDiao());
                                }

                                if (check.isWangChuang() || check.isWangDiao()) {
//									check.setHu(false);
                                    check.setCardResult(cardResult);
                                }
                            }
                        } else if (cardResult.isWangChuang()) {
                            LdsPhzCardMessage cardMessage = null;
                            for (LdsPhzCardMessage cm : cardResult.getCardMessageList()) {
                                if ((cm.getHuXiEnum() == LdsPhzHuXiEnums.WEI || cm.getHuXiEnum() == LdsPhzHuXiEnums.KAN) && LdsPhzCardUtils.hasCard(cm.getCards(), val) && LdsPhzCardUtils.countCard(cm.getCards(), 201) >= 2) {
                                    cardMessage = cm;
                                    break;
                                }
                            }

                            if (cardMessage == null) {
                                LogUtil.errorLog.warn("tiqianhu error:card=" + card + ",cards=" + cardResult.getCardMessageList());
                            }

                            if (cardMessage != null && huxi - cardMessage.loadHuxi() + 3 < table.loadQihuxi()) {
                                checkHu = false;

                                if (handCards.WANGS.size() >= 3 && cardResult.isWangDiao() && table.loadHuMode() == 2 && cardResult.getDiaoCardResult().getTotalFan() > 1) {
                                    int redCount = cardResult.getDiaoCardResult().getRedTotal();
                                    int baseCount = cardResult.getDiaoCardResult().getRedCount();
                                    int red0 = replaceRedCount(cardResult.getDiaoCardResult());
                                    int red1;

                                    if (LdsPhzCardUtils.wangCard(card)) {
                                        if (red0 >= 3) {
                                            red1 = red0 - 2;
                                        } else {
                                            red1 = replaceRed(cardResult.getDiaoCardResult()) ? red0 - 2 : red0;
                                        }
                                    } else {
                                        red1 = LdsPhzCardUtils.redCard(card) ? red0 - 1 : red0;
                                    }
                                    switch (handCards.WANGS.size()) {
                                        case 3:
                                            if (redCount + red1 <= 1 || (baseCount + red1 < table.loadRedCount() && redCount >= table.loadRedCount() + red1)) {
                                                check.setWangDiao(false);
                                            } else {
                                                check.setWangDiao(true);
                                            }
                                            break;
                                        case 4:
                                            if (redCount + red1 <= 0 || (table.checkRed2Black() && baseCount + red1 < table.loadRed2BlackCount() && redCount >= table.loadRed2BlackCount() + red1)) {
                                                check.setWangDiao(false);
                                            } else {
                                                check.setWangDiao(true);
                                            }
                                            break;
                                        default:
                                            check.setWangDiao(true);
                                            break;
                                    }
                                } else {
                                    check.setWangDiao(cardResult.isWangDiao());
                                }

                                if (check.isWangDiao()) {
//									check.setHu(false);
                                    check.setCardResult(cardResult);
                                }
                            }
                        }
                    }
                    if (checkHu) {
                        check.setWangZha(cardResult.isWangZha());
                        if (cardResult.isWangZha()) {
                            if ((card > 0 && !LdsPhzCardUtils.smallCard(card)) && cardResult.isWangChuang()) {
                                int huxi = cardResult.getChuangCardResult().getTotalHuxi(false);
                                LdsPhzCardMessage cardMessage = null;
                                for (LdsPhzCardMessage cm : cardResult.getChuangCardResult().getCardMessageList()) {
                                    if ((cm.getHuXiEnum() == LdsPhzHuXiEnums.WEI || cm.getHuXiEnum() == LdsPhzHuXiEnums.KAN) && LdsPhzCardUtils.hasCard(cm.getCards(), val) && LdsPhzCardUtils.countCard(cm.getCards(), 201) >= 2) {
                                        cardMessage = cm;
                                        break;
                                    }
                                }

                                if (cardMessage == null) {
                                    LogUtil.errorLog.warn("tiqianhu error:card=" + card + ",cards=" + cardResult.getChuangCardResult().getCardMessageList());
                                }

                                if (cardMessage != null && huxi - cardMessage.loadHuxi() + 3 < table.loadQihuxi()) {
                                    checkHu = false;
                                }
                                check.setWangChuang(checkHu);
                            } else {
                                check.setWangChuang(cardResult.isWangChuang());
                            }
                            if (check.isWangChuang()) {
                                if (handCards.WANGS.size() >= 4 && !LdsPhzCardUtils.wangCard(card) && table.loadHuMode() == 2 && cardResult.getChuangCardResult().getTotalFan() > 1) {
                                    int redCount = cardResult.getChuangCardResult().getRedTotal();
                                    int baseCount = cardResult.getChuangCardResult().getRedCount();
                                    int red0 = replaceRedCount(cardResult.getChuangCardResult());
                                    int red1 = LdsPhzCardUtils.redCard(card) ? red0 - 2 : red0;

                                    if (redCount <= 1 || (baseCount + red1 < table.loadRedCount() && redCount >= table.loadRedCount() + red1)) {
                                        check.setWangChuang(false);
                                    } else {
                                        check.setWangChuang(true);
                                    }
                                } else {
                                    check.setWangChuang(true);
                                }
                            }
                        } else {
                            if (cardResult.isWangChuang()) {
                                if (handCards.WANGS.size() >= 4 && !LdsPhzCardUtils.wangCard(card) && table.loadHuMode() == 2 && cardResult.getChuangCardResult().getTotalFan() > 1) {
                                    int redCount = cardResult.getChuangCardResult().getRedTotal();
                                    int baseCount = cardResult.getChuangCardResult().getRedCount();
                                    int red0 = replaceRedCount(cardResult.getChuangCardResult());
                                    int red1 = LdsPhzCardUtils.redCard(card) ? red0 - 2 : red0;

                                    if (redCount <= 1 || (baseCount + red1 < table.loadRedCount() && redCount >= table.loadRedCount() + red1)) {
                                        check.setWangChuang(false);
                                    } else {
                                        check.setWangChuang(true);
                                    }
                                } else {
                                    check.setWangChuang(true);
                                }
                            }
                        }

                        if (handCards.WANGS.size() >= 3 && cardResult.isWangDiao() && table.loadHuMode() == 2 && cardResult.getDiaoCardResult().getTotalFan() > 1) {
                            int redCount = cardResult.getDiaoCardResult().getRedTotal();
                            int baseCount = cardResult.getDiaoCardResult().getRedCount();
                            int red0 = replaceRedCount(cardResult.getDiaoCardResult());
                            int red1;

                            if (LdsPhzCardUtils.wangCard(card)) {
                                if (red0 >= 3) {
                                    red1 = red0 - 2;
                                } else {
                                    red1 = replaceRed(cardResult.getDiaoCardResult()) ? red0 - 2 : red0;
                                }
                            } else {
                                red1 = LdsPhzCardUtils.redCard(card) ? red0 - 1 : red0;
                            }
                            switch (handCards.WANGS.size()) {
                                case 3:
                                    if (redCount + red1 <= 1 || (baseCount + red1 < table.loadRedCount() && redCount >= table.loadRedCount() + red1)) {
                                        check.setWangDiao(false);
                                    } else {
                                        check.setWangDiao(true);
                                    }
                                    break;
                                case 4:
                                    if (redCount + red1 <= 0 || (table.checkRed2Black() && baseCount + red1 < table.loadRed2BlackCount() && redCount >= table.loadRed2BlackCount() + red1)) {
                                        check.setWangDiao(false);
                                    } else {
                                        check.setWangDiao(true);
                                    }
                                    break;
                                default:
                                    check.setWangDiao(true);
                                    break;
                            }
                        } else {
                            check.setWangDiao(cardResult.isWangDiao());
                        }
                    }
                }
            }
        } else {
            if (val > 0) {
                if (isSelfMo) {
                    if (isMoFlag) {
                        List<Integer> temp = handCards.KAN.get(val);
                        if (temp != null&&!temp.contains(card)) {
                            check.setTi(true);
                            check.setAuto(LdsPhzConstants.action_ti, new ArrayList<>(temp));
                        } else {
                            temp = handCards.WEI.get(val);
                            if (temp != null&&!temp.contains(card)) {
                                check.setTi(true);
                                check.setAuto(LdsPhzConstants.action_ti, new ArrayList<>(temp));
                            } else {
                                temp = handCards.PENG.get(val);
                                if (temp != null&&!temp.contains(card)) {
//                                    check.setPao(true);
//                                    check.setAuto(LdsPhzConstants.action_pao,new ArrayList<>(temp));
//
                                    if (canHuState == 1 && !isPassHu) {
                                        LdsPhzCardResult cardResult = LdsPhzHuPaiUtils.huPai(handCards, card, table, isSelfMo, userId, table.loadXingCard(tiqian));
                                        if (!wangPass) {
                                            if (cardResult.isCanHu()) {
                                                check.setHu(true);
                                                check.setCardResult(cardResult);
                                            }
                                        } else {
                                            if (cardResult.isCanHu() && !cardResult.isWangZha() && !cardResult.isWangChuang() && !cardResult.isWangDiao()) {
                                                check.setHu(true);
                                                check.setCardResult(cardResult);
                                            }
                                        }
                                    }
                                } else {
                                    temp = LdsPhzCardUtils.loadIdsByVal(handCards.INS, val);
                                    if (temp != null && temp.size() == 2&&!temp.contains(card)) {
                                        if (!handCards.NOT_PENG.contains(val) && !handCards.NOT_CHI.contains(val)) {
                                            check.setZai(true);
                                            check.setAuto(LdsPhzConstants.action_zai, new ArrayList<>(temp));
                                        } else {
                                            check.setChouZai(true);
                                            check.setAuto(LdsPhzConstants.action_chouzai, new ArrayList<>(temp));
                                        }
                                    } else {
                                        if (val < 200 && !isPass && canChi0(card) && handCards.INS.size() > 2) {
                                            if (isMoFlag) {
                                                check.setChi(true);
                                            } else {
                                                List<Integer> list = new ArrayList<>(((LdsPhzPlayer) table.getSeatMap().get(table.calcFrontSeat(seat))).getHandCards().OUTS);
                                                list.remove(Integer.valueOf(table.getDisposeCard()));
                                                boolean bl = true;
                                                for (Integer integer : list) {
                                                    if (val == LdsPhzCardUtils.loadCardVal(integer)) {
                                                        bl = false;
                                                        break;
                                                    }
                                                }
                                                if (bl) {
                                                    check.setChi(true);
                                                }
                                            }
                                        }
                                        if (!isPassHu) {
                                            if (canHuState == 1) {
                                                LdsPhzCardResult cardResult = LdsPhzHuPaiUtils.huPai(handCards, card, table, isSelfMo, userId, table.loadXingCard(tiqian));
                                                if (!wangPass) {
                                                    if (cardResult.isCanHu()) {
                                                        check.setHu(true);
                                                        check.setCardResult(cardResult);
                                                    }
                                                } else {
                                                    if (cardResult.isCanHu() && !cardResult.isWangZha() && !cardResult.isWangChuang() && !cardResult.isWangDiao()) {
                                                        check.setHu(true);
                                                        check.setCardResult(cardResult);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    List<Integer> temp = handCards.KAN.get(val);
                    if (temp != null&&!temp.contains(card)) {
//                        check.setPao(true);
//                        check.setAuto(LdsPhzConstants.action_pao,new ArrayList<>(temp));
//
                        if (canHuState == 1 && !isPassHu) {
                            LdsPhzCardResult cardResult = LdsPhzHuPaiUtils.huPai(handCards, card, table, isSelfMo, userId, table.loadXingCard(tiqian));
                            if (!wangPass) {
                                if (cardResult.isCanHu()) {
                                    check.setHu(true);
                                    check.setCardResult(cardResult);
                                }
                            } else {
                                if (cardResult.isCanHu() && !cardResult.isWangZha() && !cardResult.isWangChuang() && !cardResult.isWangDiao()) {
                                    check.setHu(true);
                                    check.setCardResult(cardResult);
                                }
                            }
                        }
                    } else {
                        temp = handCards.WEI.get(val);
                        if (temp != null&&!temp.contains(card)) {
//                            check.setPao(true);
//                            check.setAuto(LdsPhzConstants.action_pao,new ArrayList<>(temp));
//
                            if (canHuState == 1 && !isPassHu) {
                                LdsPhzCardResult cardResult = LdsPhzHuPaiUtils.huPai(handCards, card, table, isSelfMo, userId, table.loadXingCard(tiqian));
                                if (!wangPass) {
                                    if (cardResult.isCanHu()) {
                                        check.setHu(true);
                                        check.setCardResult(cardResult);
                                    }
                                } else {
                                    if (cardResult.isCanHu() && !cardResult.isWangZha() && !cardResult.isWangChuang() && !cardResult.isWangDiao()) {
                                        check.setHu(true);
                                        check.setCardResult(cardResult);
                                    }
                                }
                            }
                        } else {
                            temp = handCards.PENG.get(val);
                            if (temp != null && isMoFlag&&!temp.contains(card)) {
//                                check.setPao(true);
//                                check.setAuto(LdsPhzConstants.action_pao,new ArrayList<>(temp));
//
                                if (canHuState == 1 && !isPassHu) {
                                    LdsPhzCardResult cardResult = LdsPhzHuPaiUtils.huPai(handCards, card, table, isSelfMo, userId, table.loadXingCard(tiqian));
                                    if (!wangPass) {
                                        if (cardResult.isCanHu()) {
                                            check.setHu(true);
                                            check.setCardResult(cardResult);
                                        }
                                    } else {
                                        if (cardResult.isCanHu() && !cardResult.isWangZha() && !cardResult.isWangChuang() && !cardResult.isWangDiao()) {
                                            check.setHu(true);
                                            check.setCardResult(cardResult);
                                        }
                                    }
                                }
                            } else {
                                if (nextSeat == seat) {
                                    if (val < 200 && !isPass && canChi0(card) && handCards.INS.size() > 2) {
                                        if (isMoFlag) {
                                            check.setChi(true);
                                        } else {
                                            List<Integer> list = new ArrayList<>(((LdsPhzPlayer) table.getSeatMap().get(table.calcFrontSeat(seat))).getHandCards().OUTS);
                                            list.remove(Integer.valueOf(table.getDisposeCard()));
                                            boolean bl = true;
                                            for (Integer integer : list) {
                                                if (val == LdsPhzCardUtils.loadCardVal(integer)) {
                                                    bl = false;
                                                    break;
                                                }
                                            }
                                            if (bl) {
                                                check.setChi(true);
                                            }
                                        }
                                    }
                                }

                                if (!isPass && !handCards.NOT_PENG.contains(val) && LdsPhzCardUtils.canPeng(handCards.INS, card) && handCards.INS.size() > 2) {
                                    check.setPeng(true);
                                }
                                if (!isPassHu && isMoFlag) {
                                    if (canHuState == 1) {
                                        LdsPhzCardResult cardResult = LdsPhzHuPaiUtils.huPai(handCards, card, table, isSelfMo, userId, table.loadXingCard(tiqian));
                                        if (!wangPass) {
                                            if (cardResult.isCanHu()) {
                                                check.setHu(true);
                                                check.setCardResult(cardResult);
                                            }
                                        } else {
                                            if (cardResult.isCanHu() && !cardResult.isWangZha() && !cardResult.isWangChuang() && !cardResult.isWangDiao()) {
                                                check.setHu(true);
                                                check.setCardResult(cardResult);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                if (!isPassHu || isBegin) {
                    if (canHuState == 1 && isMoFlag) {
                        LdsPhzCardResult cardResult = LdsPhzHuPaiUtils.huPai(handCards, card, table, isSelfMo, userId, table.loadXingCard(tiqian));
//						System.out.println("weihu :"+isSelfMo+"="+cardResult.isCanHu());
                        if (!wangPass) {
                            if (cardResult.isCanHu()) {
                                check.setHu(true);
                                check.setCardResult(cardResult);
                            }
                        } else {
                            if (cardResult.isCanHu() && !cardResult.isWangZha() && !cardResult.isWangChuang() && !cardResult.isWangDiao()) {
                                check.setHu(true);
                                check.setCardResult(cardResult);
                            }
                        }
                    }
                } else {
//				if (!isPassHu&&isMoFlag){
//					LdsPhzCardResult cardResult=LdsPhzHuPaiUtils.huPai(handCards,card,15,isSelfMo);
//					if (cardResult.isCanHu()){
//						if(nextSeat==seat){
//							if (cardResult.isWangZha()){
//								check.setWangZha(true);
//							}else if (cardResult.isWangChuang()){
//								check.setWangChuang(true);
//							}else if (cardResult.isWangDiao()){
//								check.setWangDiao(true);
//							}
//						}
//						check.setHu(true);
//					}
//				}
                }

            }
        }
        

        if (isPassHu && huResult == 1) {
            check.setHu(true);
        }
        
        if(!check.isHu()){
        	 LdsPhzCardResult cardResult = LdsPhzHuPaiUtils.huPai(handCards, card, table, isSelfMo, userId, table.loadXingCard(tiqian));
                 if (cardResult.isCanHu()) {
                     check.setHu(true);
                     table.setHuCard(card);
                     check.setCardResult(cardResult);
                 }
        }
        
        
        
        if(getCanHuState()==0||!checkCanhuPass(val, isSelfMo?1:0)) {
        	check.setHu(false);
        }
        
		if (check.isHu()) {

//			if ((table.getHuBiHu() == 2&&handCards.WANGS.size()==0) || (table.getHuBiHu() == 1 && !table.isMoFlag())) {
//				check.setAuto(LdsPhzConstants.action_hu, new ArrayList<>());
//			}
		}
        

//		List<Integer> list=
        check.buildActionList();
//		LogUtil.msgLog.info("dongzuo list===tableId="+table.getId()+",userId="+userId+",actions="+list);

        return check;
    }

    @Override
    public void initNext() {
        setHuResult(null);
        setPoint(0);
        setZaiHuxi(0);
        handCards = new LdsPhzHandCards();
        cardTypes.clear();
        setOutHuxi(0);
        setHuxi(0);
        setOweCardCount(0);
        getPlayingTable().changeExtend();
        getPlayingTable().changeCards(seat);
        changeState(SharedConstants.player_state.entry);
        setCanHuState(1);
        setPassHuVal(0,0);
    }

    @Override
    public List<Integer> getHandPais() {
        return handCards.SRC;
    }
    
    public List<Integer> getLefHandPais() {
    	List<Integer> letf = new ArrayList<>(handCards.INS);
    	letf.addAll(handCards.WANGS);
        return letf;
    }

    public Set<Integer> getOutPais() {
        return getOutPaisCard();
    }

    public Set<Integer> getOutPaisCard() {
        return handCards.OUTS;
    }

    List<Integer> str2IntList(String str, String split) {
        List<Integer> list = new ArrayList<>();
        String[] strs = str.split(split);
        for (String temp : strs) {
            if (StringUtils.isNotBlank(temp)) {
                list.add(Integer.valueOf(temp));
            }
        }
        return list;
    }

    String intList2Str(List<Integer> list, String split) {
        if (list == null) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (Integer temp : list) {
            stringBuilder.append(split).append(temp);
        }
        return stringBuilder.length() > 0 ? stringBuilder.substring(1) : "";
    }

    /**
     * 出牌
     *
     * @return
     */
    public String buildOutPaiStr() {
        JsonWrapper json = new JsonWrapper("{}");

        StringBuilder strBuilder = new StringBuilder();
        for (Integer val : handCards.OUTS) {
            strBuilder.append(",").append(val);
        }
        json.putString("out", strBuilder.length() > 0 ? strBuilder.substring(1) : "");

        strBuilder.setLength(0);
        for (Integer val : handCards.SRC) {
            strBuilder.append(",").append(val);
        }
        json.putString("src", strBuilder.length() > 0 ? strBuilder.substring(1) : "");

        strBuilder.setLength(0);
        for (LdsPhzCardTypeHuxi huxi : cardTypes) {
            strBuilder.append(";").append(huxi.toStr());
        }
        json.putString("cards", strBuilder.length() > 0 ? strBuilder.substring(1) : "");

        return json.toString();
    }

    /**
     * 手牌
     *
     * @return
     */
    public String buildHandPaiStr() {
        JsonWrapper json = new JsonWrapper("{}");
        StringBuilder strBuilder = new StringBuilder();
        for (Map.Entry<Integer, List<Integer>> kv : handCards.TI.entrySet()) {
            strBuilder.append(";");
            int i = 0;
            for (Integer val : kv.getValue()) {
                if (i > 0) {
                    strBuilder.append(",");
                }
                strBuilder.append(val);
                i++;
            }
        }
        json.putString("t4", strBuilder.length() > 0 ? strBuilder.substring(1) : "");

        strBuilder.setLength(0);
        for (Map.Entry<Integer, List<Integer>> kv : handCards.PAO.entrySet()) {
            strBuilder.append(";");
            int i = 0;
            for (Integer val : kv.getValue()) {
                if (i > 0) {
                    strBuilder.append(",");
                }
                strBuilder.append(val);
                i++;
            }
        }
        json.putString("p4", strBuilder.length() > 0 ? strBuilder.substring(1) : "");

        strBuilder.setLength(0);
        for (Map.Entry<Integer, List<Integer>> kv : handCards.KAN.entrySet()) {
            strBuilder.append(";");
            int i = 0;
            for (Integer val : kv.getValue()) {
                if (i > 0) {
                    strBuilder.append(",");
                }
                strBuilder.append(val);
                i++;
            }
        }
        json.putString("k3", strBuilder.length() > 0 ? strBuilder.substring(1) : "");

        strBuilder.setLength(0);
        for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI.entrySet()) {
            strBuilder.append(";");
            int i = 0;
            for (Integer val : kv.getValue()) {
                if (i > 0) {
                    strBuilder.append(",");
                }
                strBuilder.append(val);
                i++;
            }
        }
        json.putString("w3", strBuilder.length() > 0 ? strBuilder.substring(1) : "");

        strBuilder.setLength(0);
        for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI_CHOU.entrySet()) {
            strBuilder.append(";");
            int i = 0;
            for (Integer val : kv.getValue()) {
                if (i > 0) {
                    strBuilder.append(",");
                }
                strBuilder.append(val);
                i++;
            }
        }
        json.putString("wc", strBuilder.length() > 0 ? strBuilder.substring(1) : "");

        strBuilder.setLength(0);
        for (Map.Entry<Integer, List<Integer>> kv : handCards.PENG.entrySet()) {
            strBuilder.append(";");
            int i = 0;
            for (Integer val : kv.getValue()) {
                if (i > 0) {
                    strBuilder.append(",");
                }
                strBuilder.append(val);
                i++;
            }
        }
        json.putString("p3", strBuilder.length() > 0 ? strBuilder.substring(1) : "");

        strBuilder.setLength(0);
        for (List<Integer> list : handCards.CHI_JIAO) {
            strBuilder.append(";");
            int i = 0;
            for (Integer val : list) {
                if (i > 0) {
                    strBuilder.append(",");
                }
                strBuilder.append(val);
                i++;
            }
        }
        json.putString("j3", strBuilder.length() > 0 ? strBuilder.substring(1) : "");

        strBuilder.setLength(0);
        for (List<Integer> list : handCards.CHI_COMMON) {
            strBuilder.append(";");
            int i = 0;
            for (Integer val : list) {
                if (i > 0) {
                    strBuilder.append(",");
                }
                strBuilder.append(val);
                i++;
            }
        }
        json.putString("c3", strBuilder.length() > 0 ? strBuilder.substring(1) : "");

        strBuilder.setLength(0);
        for (Integer val : handCards.INS) {
            strBuilder.append(",").append(val);
        }
        json.putString("ins", strBuilder.length() > 0 ? strBuilder.substring(1) : "");

        strBuilder.setLength(0);
        for (Integer val : handCards.WANGS) {
            strBuilder.append(",").append(val);
        }
        json.putString("wang", strBuilder.length() > 0 ? strBuilder.substring(1) : "");

        strBuilder.setLength(0);
        for (Integer val : handCards.NOT_CHI) {
            strBuilder.append(",").append(val);
        }
        json.putString("nc", strBuilder.length() > 0 ? strBuilder.substring(1) : "");

        strBuilder.setLength(0);
        for (Integer val : handCards.NOT_PENG) {
            strBuilder.append(",").append(val);
        }
        json.putString("np", strBuilder.length() > 0 ? strBuilder.substring(1) : "");

        return json.toString();
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
        sb.append(outHuXi).append(",");
        sb.append(huxi).append(",");
        sb.append(winCount).append(",");
        sb.append(lostCount).append(",");
        sb.append(lostPoint).append(",");
        sb.append(point).append(",");
        sb.append(loadScore()).append(",");
        sb.append(oweCardCount).append(",");
        sb.append(isFristDisCard ? 1 : 0).append(",");
        sb.append(getMaxPoint()).append(",");
        sb.append(zaiHuxi).append(",");
        sb.append(intList2Str(pointList, "_")).append(",");
        sb.append(canHuState).append(",");
        sb.append(StringUtil.implode(actionTotalArr, "_")).append(",");
        
        sb.append(passHuVal).append(",");
        
        return sb.toString();
    }

    /**
     * 单局详情
     *
     * @return
     */
    public ClosingPhzPlayerInfoRes.Builder bulidOneClosingPlayerInfoRes(BaseTable table,int onePoint2, boolean isOver) {
        ClosingPhzPlayerInfoRes.Builder res = ClosingPhzPlayerInfoRes.newBuilder();
        res.setUserId(userId + "");
        res.addAllCards(loadHandCards());
        res.setName(name);
        res.setPoint(point);
        res.setBopiPoint(onePoint2);
//        res.setTotalPoint(table.isGoldRoom()?(int)loadAllGolds():isOver && isBoPi ? randNumber(loadScore()) : loadScore());
        res.setTotalPoint(loadScore());
        res.setSeat(seat);
        if (!StringUtils.isBlank(getHeadimgurl())) {
            res.setIcon(getHeadimgurl());
        } else {
            res.setIcon("");
        }
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
    public ClosingPhzPlayerInfoRes.Builder bulidTotalClosingPlayerInfoRes(BaseTable table,int isBoPi, boolean isOver) {
        ClosingPhzPlayerInfoRes.Builder res = bulidOneClosingPlayerInfoRes(table, isBoPi, isOver);
        res.setLostCount(lostCount);
        res.setWinCount(winCount);
        res.setMaxPoint(getMaxPoint());
       // res.setBopiPoint(bopiPoint);
       // res.addStrExt(intList2Str(pointList, ","));
        setBaseCount();
        return res;
    }

    /**
     * TODO 设置大结算的胜负次数
     */
    public void setBaseCount() {
//		this.winCnt=winCount;
//		this.loseCnt=lostCount;
    }

    @Override
    public PlayerInTableRes.Builder buildPlayInTableInfo() {
        return buildPlayInTableInfo(getPlayingTable(LdsPhzTable.class),0, false);
    }

    public PlayerInTableRes.Builder buildPlayInTableInfo(LdsPhzTable table, long lookUid, boolean isrecover) {
        PlayerInTableRes.Builder res = PlayerInTableRes.newBuilder();
        res.setUserId(userId + "");
        if (!StringUtils.isBlank(ip)) {
            res.setIp(ip);
        } else {
            res.setIp("");
        }

        int outHuxi = handCards.loadPPChuxi();
        if (lookUid == userId) {
            outHuxi += loadTWhuxi();
        }
        res.addExt(outHuxi);
        res.setName(name);
        res.setSeat(seat);
        res.setSex(sex);
        res.setPoint(table.isGoldRoom()?(int)loadAllGolds():loadScore());
        // 当前出的牌
        res.addAllOutedIds(getOutPais());
        res.addAllMoldCards(buildPhzCards(lookUid));

//        List<Integer> nowDisCard = table.getNowDisCardIds();
        if (table.getDisCardSeat() == seat) {
            int lastCard=table.getLastCard();
            if (lastCard>0&&!table.isTianHu()) {
                int selfMo = table.isSelfMo(this)?1:0;
                res.addOutCardIds(selfMo);
                Integer beremoveCard = table.getBeRemoveCard();
                if (beremoveCard != null && beremoveCard.intValue() == lastCard) {
                    // 被移掉的牌
                } else {
                    res.addOutCardIds(lastCard);
                }
            }

        }

        if (!StringUtils.isBlank(getHeadimgurl())) {
            res.setIcon(getHeadimgurl());

        } else {
            res.setIcon("");

        }

        if (state == SharedConstants.player_state.ready || state == SharedConstants.player_state.play) {
            // 玩家装备已经准备和正在玩的状态时通知前台已准备
            res.setStatus(1);
        } else {
            res.setStatus(0);
        }
        
        
        
		res.addExt(0);// 1
		res.addExt((table.getMasterId() == this.userId) ? 1 : 0);// 2
    	res.addExt(0);// 3
    	res.addExt(0);// 4
    	res.addExt(table.isGoldRoom()?(int)loadAllGolds():0);// 5
    	res.addExt(isAutoPlay() ? 1 : 0);// 6
    	res.addExt( 0);//7
        //getAutoPlayCheckedTime() > table.getAutoTimeOut() ? 1 :
      //信用分
        if(table.isCreditTable()) {
            GroupUser gu = getGroupUser();
            String groupId = table.loadGroupId();
            if (gu == null || !groupId.equals(gu.getGroupId() + "")) {
                gu = GroupDao.getInstance().loadGroupUser(getUserId(), groupId);
            }
            res.setCredit(gu != null ? gu.getCredit() : 0);
        }

//		if (isrecover) {
        // 1在房间0暂离
//			List<Integer> recover = new ArrayList<>();
//			recover.add(isEntryTable);
//			res.addAllRecover(recover);
        res.addRecover(getIsOnline() == 1 ? isEntryTable : 0);
//		}
        return buildPlayInTableInfo1(res);

    }

    public List<Integer> loadFan() {
        List<Integer> list = new ArrayList<>();
        if (huResult != null && huResult.isCanHu()) {
            for (LdsPhzFanEnums fanEnums : huResult.getFans()) {
                switch (fanEnums) {
                    case WANG_DIAO:
                        list.add(1);
                        break;
                    case WANG_DIAO_WANG:
                        list.add(2);
                        break;
                    case WANG_CHUANG:
                        list.add(3);
                        break;
                    case WANG_CHUANG_WANG:
                        list.add(4);
                        break;
                    case WANG_ZHA:
                        list.add(5);
                        break;
                    case WANG_ZHA_WANG:
                        list.add(5);
                        break;
                    case DIAN_HU:
                        list.add(6);
                        break;
                    case HONG_HU:
                        list.add(7);
                        break;
                    case HEI_HU:
                        list.add(8);
                        break;
                    case ALL_HONG:
                        list.add(9);
                        break;
                        
                    case TIAN_HU:
                        list.add(11);
                        break;
                        
                    case DI_HU:
                        list.add(12);
                        break;
                        
                    case HONG_TO_HEI:
                       // list.add(25);
                        break;
                    case SELF:
                        list.add(10);
                        break;
                }
            }
        }
        return list;
    }

    public int getTotalFan() {
        if (huResult != null && huResult.isCanHu()) {
           return LdsPhzHuPaiUtils.loadMaxFanCount(huResult.getFans());
        }
        return 1;
    }

    @Override
    public void initPais(String handPai, String outPai) {
        try {
            if (!StringUtils.isBlank(handPai)) {
                JsonWrapper json = new JsonWrapper(handPai);
                String tis = json.getString("t4");
                String paos = json.getString("p4");
                String kans = json.getString("k3");
                String weis = json.getString("w3");
                String weichous = json.getString("wc");
                String pengs = json.getString("p3");

                String jiaos = json.getString("j3");
                String chis = json.getString("c3");

                String ins = json.getString("ins");
                String wangs = json.getString("wang");
                String ncs = json.getString("nc");
                String nps = json.getString("np");

                if (StringUtils.isNotBlank(tis)) {
                    String strs[] = tis.split(";");
                    for (String str : strs) {
                        String temps[] = str.split(",");
                        if (temps.length > 1) {
                            List<Integer> list = new ArrayList<>();
                            for (String temp : temps) {
                                int val = NumberUtils.toInt(temp, -1);
                                if (val > 0) {
                                    list.add(val);
                                }
                            }
                            if (list.size() > 0) {
                                handCards.TI.put(LdsPhzCardUtils.loadCardVal(list.get(0)), list);
                            }
                        }
                    }
                }

                if (StringUtils.isNotBlank(paos)) {
                    String strs[] = paos.split(";");
                    for (String str : strs) {
                        String temps[] = str.split(",");
                        if (temps.length > 1) {
                            List<Integer> list = new ArrayList<>();
                            for (String temp : temps) {
                                int val = NumberUtils.toInt(temp, -1);
                                if (val > 0) {
                                    list.add(val);
                                }
                            }
                            if (list.size() > 0) {
                                handCards.PAO.put(LdsPhzCardUtils.loadCardVal(list.get(0)), list);
                            }
                        }
                    }
                }

                if (StringUtils.isNotBlank(kans)) {
                    String strs[] = kans.split(";");
                    for (String str : strs) {
                        String temps[] = str.split(",");
                        if (temps.length > 1) {
                            List<Integer> list = new ArrayList<>();
                            for (String temp : temps) {
                                int val = NumberUtils.toInt(temp, -1);
                                if (val > 0) {
                                    list.add(val);
                                }
                            }
                            if (list.size() > 0) {
                                handCards.KAN.put(LdsPhzCardUtils.loadCardVal(list.get(0)), list);
                            }
                        }
                    }
                }

                if (StringUtils.isNotBlank(weis)) {
                    String strs[] = weis.split(";");
                    for (String str : strs) {
                        String temps[] = str.split(",");
                        if (temps.length > 1) {
                            List<Integer> list = new ArrayList<>();
                            for (String temp : temps) {
                                int val = NumberUtils.toInt(temp, -1);
                                if (val > 0) {
                                    list.add(val);
                                }
                            }
                            if (list.size() > 0) {
                                handCards.WEI.put(LdsPhzCardUtils.loadCardVal(list.get(0)), list);
                            }
                        }
                    }
                }

                if (StringUtils.isNotBlank(weichous)) {
                    String strs[] = weichous.split(";");
                    for (String str : strs) {
                        String temps[] = str.split(",");
                        if (temps.length > 1) {
                            List<Integer> list = new ArrayList<>();
                            for (String temp : temps) {
                                int val = NumberUtils.toInt(temp, -1);
                                if (val > 0) {
                                    list.add(val);
                                }
                            }
                            if (list.size() > 0) {
                                handCards.WEI_CHOU.put(LdsPhzCardUtils.loadCardVal(list.get(0)), list);
                            }
                        }
                    }
                }

                if (StringUtils.isNotBlank(pengs)) {
                    String strs[] = pengs.split(";");
                    for (String str : strs) {
                        String temps[] = str.split(",");
                        if (temps.length > 1) {
                            List<Integer> list = new ArrayList<>();
                            for (String temp : temps) {
                                int val = NumberUtils.toInt(temp, -1);
                                if (val > 0) {
                                    list.add(val);
                                }
                            }
                            if (list.size() > 0) {
                                handCards.PENG.put(LdsPhzCardUtils.loadCardVal(list.get(0)), list);
                            }
                        }
                    }
                }

                if (StringUtils.isNotBlank(jiaos)) {
                    String strs[] = jiaos.split(";");
                    for (String str : strs) {
                        String temps[] = str.split(",");
                        if (temps.length > 1) {
                            List<Integer> list = new ArrayList<>();
                            for (String temp : temps) {
                                int val = NumberUtils.toInt(temp, -1);
                                if (val > 0) {
                                    list.add(val);
                                }
                            }
                            if (list.size() > 0) {
                                handCards.CHI_JIAO.add(list);
                            }
                        }
                    }
                }

                if (StringUtils.isNotBlank(chis)) {
                    String strs[] = chis.split(";");
                    for (String str : strs) {
                        String temps[] = str.split(",");
                        if (temps.length > 1) {
                            List<Integer> list = new ArrayList<>();
                            for (String temp : temps) {
                                int val = NumberUtils.toInt(temp, -1);
                                if (val > 0) {
                                    list.add(val);
                                }
                            }
                            if (list.size() > 0) {
                                handCards.CHI_COMMON.add(list);
                            }
                        }
                    }
                }

                if (StringUtils.isNotBlank(ins)) {
                    String strs[] = ins.split(",");
                    for (String temp : strs) {
                        int val = NumberUtils.toInt(temp, -1);
                        if (val > 0) {
                            handCards.INS.add(val);
                        }
                    }
                }

                if (StringUtils.isNotBlank(wangs)) {
                    String strs[] = wangs.split(",");
                    for (String temp : strs) {
                        int val = NumberUtils.toInt(temp, -1);
                        if (val > 0) {
                            handCards.WANGS.add(val);
                        }
                    }
                }

                if (StringUtils.isNotBlank(ncs)) {
                    String strs[] = ncs.split(",");
                    for (String temp : strs) {
                        int val = NumberUtils.toInt(temp, -1);
                        if (val > 0) {
                            handCards.NOT_CHI.add(val);
                        }
                    }
                }

                if (StringUtils.isNotBlank(nps)) {
                    String strs[] = nps.split(",");
                    for (String temp : strs) {
                        int val = NumberUtils.toInt(temp, -1);
                        if (val > 0) {
                            handCards.NOT_PENG.add(val);
                        }
                    }
                }
            }

            if (!StringUtils.isBlank(outPai)) {
                JsonWrapper json = new JsonWrapper(outPai);
                String out = json.getString("out");
                String srcs = json.getString("src");
                String cards = json.getString("cards");
                //todo
                if (StringUtils.isNotBlank(out)) {
                    String strs[] = out.split(",");
                    for (String temp : strs) {
                        int val = NumberUtils.toInt(temp, -1);
                        if (val > 0) {
                            handCards.OUTS.add(val);
                        }
                    }
                }

                if (StringUtils.isNotBlank(srcs)) {
                    String strs[] = srcs.split(",");
                    for (String temp : strs) {
                        int val = NumberUtils.toInt(temp, -1);
                        if (val > 0) {
                            handCards.SRC.add(val);
                        }
                    }
                }
                if (StringUtils.isNotBlank(cards)) {
                    String strs[] = cards.split(";");
                    for (String temp : strs) {
                        if (StringUtils.isNotBlank(temp)) {
                            LdsPhzCardTypeHuxi type = new LdsPhzCardTypeHuxi();
                            type.init(temp);
                            cardTypes.add(type);
                        }
                    }
                }

            }
        } catch (Exception e) {
            LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
        }
    }

    @Override
    public void endCompetition1() {

    }

    public void dealHandPais(List<Integer> list) {
        handCards.init(list);
        getPlayingTable().changeCards(seat);
    }

    public void reBuildHandCards() {
        handCards = new LdsPhzHandCards();
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

    public void calcResult(int count, int point, boolean huangzhuang) {

    	LdsPhzTable table2 = getPlayingTable(LdsPhzTable.class);
        if (!huangzhuang) {
            if (point > 0) {
                this.winCount += count;
            } else {
            	  this.lostCount += count;
//            	  if(table2.getLimitScore()>0) {
//                  	if(table2.getLimitScore()==1){
//                  		if(lostCount<-300) {
//                  			lostCount =-300;
//                  		}
//                  	}else if(table2.getLimitScore()==2) {
//                  		if(lostCount<-600) {
//                  			lostCount =-600;
//                  		}
//                  	}
//                  }
            }
        }

        if (table == null){
            table = getPlayingTable();
        }

        if (table!=null&&table.isGoldRoom()){
            getGoldPlayer().changePlayCount();
            if (point>0){
                getGoldPlayer().changeWinCount();
                if (isRobot()){

                }else
                GoldDao.getInstance().updateGoldUserCount(userId,1,0,0,1);
            }else if (point<0){
                getGoldPlayer().changeLoseCount();
                if (isRobot()){

                }else
                GoldDao.getInstance().updateGoldUserCount(userId,0,1,0,1);
            }else{
                getGoldPlayer().changePlayCountEven();
                if (isRobot()){

                }else
                GoldDao.getInstance().updateGoldUserCount(userId,0,0,1,1);
            }
        }
        

        changePoint(point);

        pointList.add(point);
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

    public int getOweCardCount() {
        return oweCardCount;
    }

    public void setOweCardCount(int oweCardCount) {
        this.oweCardCount = oweCardCount;
    }

    public int loadTWhuxi() {
        int total = 0;
//		if (handCards.TI.size()>0){
//			for(Map.Entry<Integer,List<Integer>> kv:handCards.TI.entrySet()){
//				total+=(kv.getKey()>100? LdsPhzHuXiEnums.TI.getBig(): LdsPhzHuXiEnums.TI.getSmall());
//			}
//		}
//		if (handCards.KAN.size()>0){
//			for(Map.Entry<Integer,List<Integer>> kv:handCards.KAN.entrySet()){
//				total+=(kv.getKey()>100? LdsPhzHuXiEnums.KAN.getBig(): LdsPhzHuXiEnums.KAN.getSmall());
//			}
//		}

        if (cardTypes.size() > 0) {
            for (LdsPhzCardTypeHuxi cth : cardTypes) {
                if (cth.getAction() == 4) {
                    total += cth.getHux();
                }
            }
        }

        if (handCards.WEI.size() > 0) {
            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI.entrySet()) {
                total += (kv.getKey() > 100 ? LdsPhzHuXiEnums.WEI.getBig() : LdsPhzHuXiEnums.WEI.getSmall());
            }
        }
        return total;
    }

    public List<Integer> loadHandCards() {
        List<Integer> list = new ArrayList<>();
        list.addAll(handCards.INS);
        for (List<Integer> temp : handCards.TI.values()) {
            list.addAll(temp);
        }
        for (List<Integer> temp : handCards.KAN.values()) {
            list.addAll(temp);
        }
        list.addAll(handCards.WANGS);
        for (LdsPhzCardTypeHuxi cth : cardTypes) {
            if (cth.getAction() == LdsPhzConstants.action_ti) {
                list.removeAll(cth.getCardIds());
            }
        }
        return list;
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

    public LdsPhzCardTypeHuxi convertCardType(List<Integer> list) {
        LdsPhzCardTypeHuxi cth = new LdsPhzCardTypeHuxi();
        LdsPhzCardUtils.sort(list);
        cth.setCardIds(list);
        if (list.size() == 3) {
            int val1 = LdsPhzCardUtils.loadCardVal(list.get(0));
            int val2 = LdsPhzCardUtils.loadCardVal(list.get(1));
            int val3 = LdsPhzCardUtils.loadCardVal(list.get(2));
            List<Integer> list1 = LdsPhzCardUtils.loadIdsByVal(list, 201);
            if (list1.size() == 0) {
                if (val1 == val2 && val1 == val3) {
                    cth.setAction(LdsPhzConstants.action_zai);
                    cth.setHux(val1 > 100 ? 6 : 3);
                } else if (val1 == 1 && val2 == 2 && val3 == 3) {
                    cth.setAction(LdsPhzConstants.action_chi);
                    cth.setHux(3);
                } else if (val1 == 101 && val2 == 102 && val3 == 103) {
                    cth.setAction(LdsPhzConstants.action_chi);
                    cth.setHux(6);
                } else if (val1 == 2 && val2 == 7 && val3 == 10) {
                    cth.setAction(LdsPhzConstants.action_chi);
                    cth.setHux(3);
                } else if (val1 == 102 && val2 == 107 && val3 == 110) {
                    cth.setAction(LdsPhzConstants.action_chi);
                    cth.setHux(6);
                } else {
                    cth.setAction(LdsPhzConstants.action_chi);
                    cth.setHux(0);
                }
            } else {
                if (val1 == 201) {
                    cth.setAction(LdsPhzConstants.action_zai);
                    cth.setHux(6);
                } else if (val2 == 201) {
                    cth.setAction(LdsPhzConstants.action_zai);
                    cth.setHux(val1 > 100 ? 6 : 3);
                } else {
                    if (val1 == val2) {
                        cth.setAction(LdsPhzConstants.action_zai);
                        cth.setHux(val1 > 100 ? 6 : 3);
                    } else if (LdsPhzCardUtils.sameType(list.get(0), list.get(1))) {
                        cth.setAction(LdsPhzConstants.action_chi);
                        if (val1 == 1 || (val1 == 2 && val2 == 3)) {
                            cth.setHux(3);
                        } else if (val1 == 101 || (val1 == 102 && val2 == 103)) {
                            cth.setHux(6);
                        } else if (LdsPhzCardUtils.redCard(list.get(0)) && LdsPhzCardUtils.redCard(list.get(1))) {
                            cth.setHux(val1 > 100 ? 6 : 3);
                        } else {
                            cth.setHux(0);
                        }

                    } else {
                        cth.setAction(LdsPhzConstants.action_chi);
                        cth.setHux(0);
                    }
                }
            }
        } else if (list.size() == 4) {
            cth.setAction(LdsPhzConstants.action_ti);
            cth.setHux(LdsPhzCardUtils.loadCardVal(list.get(0)) > 100 ? 12 : 9);
        }
        return cth;
    }

    public int loadWangHu(LdsPhzCardResult cardResult) {
        LdsPhzCardResult temp = cardResult != null ? cardResult : huResult;
        int wangHu = -1;
        if (temp.isCanHu()) {
            wangHu = 0;
            for (LdsPhzFanEnums fanEnums : temp.getFans()) {
                if (fanEnums.name().startsWith("WANG_DIAO")) {
                    wangHu = 1;
                } else if (fanEnums.name().startsWith("WANG_CHUANG")) {
                    wangHu = 2;
                } else if (fanEnums.name().startsWith("WANG_ZHA")) {
                    wangHu = 3;
                }
            }
        }
        return wangHu;
    }

    public int loadValCount(LdsPhzCardResult cardResult, int val) {
        LdsPhzCardResult temp = cardResult != null ? cardResult : huResult;
        return temp.loadValCount(val);
    }

    public List<Integer> loadReplaceCards0() {
        return huResult.loadReplaceCards0();
    }

    public List<Integer> loadReplaceCards(int val) {
        return huResult.loadReplaceCards(val);
    }


    /**
     * 总胡息
     *
     * @return
     */
    public int getTotalHu() {
        return huResult != null ? huResult.getTotalHuxi(false) : 0;
    }

    public List<PhzHuCards> buildPhzHuCards() {
        List<PhzHuCards> list = new ArrayList<>();
        if (huResult != null && huResult.isCanHu()) {
            for (LdsPhzCardMessage cm : huResult.getCardMessageList()) {
                list.add(cm.build().build());
            }
//			System.out.println(huResult.getCardMessageList());
        }

        return list;
    }

    public String buildReplaceCards(int val) {
        StringBuffer sb = new StringBuffer();

        List<Integer> replaceCards = loadReplaceCards(val);
        if (replaceCards != null && replaceCards.size() > 0) {
            for (int temp : replaceCards) {
                sb.append(temp);
                sb.append(";");
            }
        }

        String str = "";
        if (sb.length() > 0) {
            str = sb.substring(0, sb.length() - 1);
        }

        return str;
    }

    public List<PhzHuCards> buildPhzCards(long lookUid) {
        List<PhzHuCards> list = new ArrayList<>();
        for (LdsPhzCardTypeHuxi type : cardTypes) {
            if (lookUid != this.userId && type.getAction() == LdsPhzConstants.action_zai) {
                // 不是本人并且是栽
                list.add(type.buildMsg(true).build());
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
    public int calcHuPoint(LdsPhzTable table) {
        int total = getTotalHu();

        if (total < table.loadQihuxi()) {
            return 0;
        }

        return table.loadScoreTun(total);
    }

    /**
     * 是否栽过的牌跑
     *
     * @param val
     * @return
     */
    public boolean isZaiPao(int val) {
        return handCards.WEI.containsKey(val);
    }

    public LdsPhzCardResult getHuResult() {
        return huResult;
    }

    public void setHuResult(LdsPhzCardResult huResult) {
        this.huResult = huResult;
    }

    public Set<Integer> getPassChi() {
        return handCards.NOT_CHI;
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

//    public boolean canChi(int id) {
//        int val = LdsPhzCardUtils.loadCardVal(id);
//        return !LdsPhzCardUtils.hasCard(handCards.OUTS, val) && !handCards.NOT_CHI.contains(val) && LdsPhzCardUtils.canChi(handCards.INS, id).size() > 0;
//    }

    public boolean canChi0(int id) {
        int val = LdsPhzCardUtils.loadCardVal(id);

        LdsPhzTable table = getPlayingTable(LdsPhzTable.class);
        if (table != null) {
            LdsPhzPlayer player = (LdsPhzPlayer) table.getSeatMap().get(table.calcFrontSeat(seat));
            Set<Integer> set = new HashSet<>(player.getHandCards().OUTS);
            set.remove(Integer.valueOf(id));
            if (LdsPhzCardUtils.hasCard(set, val)) {
                return false;
            }
        }

        return !LdsPhzCardUtils.hasCard(handCards.OUTS, val) && !handCards.NOT_CHI.contains(val) && LdsPhzCardUtils.canChi(handCards.INS, id).size() > 0;
    }

//    public void removePassChi(Integer val) {
//        if (handCards.NOT_CHI.contains(val)) {
//            handCards.NOT_CHI.remove(val);
//            changeTableInfo();
//        }
//    }
    public static final List<Integer> wanfaList = Arrays.asList(
            GameUtil.play_type_LDSPHZ
    );

    public static void loadWanfaPlayers(Class<? extends Player> cls){
        for (Integer integer:wanfaList){
            PlayerManager.wanfaPlayerTypesPut(integer,cls, LdsPhzCommandProcessor.getInstance());
        }
    }
}
