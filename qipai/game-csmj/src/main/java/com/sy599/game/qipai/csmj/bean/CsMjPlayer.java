package com.sy599.game.qipai.csmj.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.sy599.game.robot.RobotManager;
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
import com.sy599.game.qipai.csmj.command.CsMjCommandProcessor;
import com.sy599.game.qipai.csmj.constant.CsMjAction;
import com.sy599.game.qipai.csmj.constant.CsMjConstants;
import com.sy599.game.qipai.csmj.rule.CsMj;
import com.sy599.game.qipai.csmj.rule.CsMjHelper;
import com.sy599.game.qipai.csmj.rule.CsMjRule;
import com.sy599.game.qipai.csmj.tool.CsMjEnumHelper;
import com.sy599.game.qipai.csmj.tool.CsMjQipaiTool;
import com.sy599.game.qipai.csmj.tool.CsMjTool;
import com.sy599.game.util.DataMapUtil;
import com.sy599.game.util.GameUtil;
import com.sy599.game.util.JacksonUtil;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.util.StringUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

public class CsMjPlayer extends Player {
	// 座位id
	private int seat;
	// 状态
	private player_state state;// 1进入 2已准备 3正在玩 4已结束
	private int isEntryTable;
	private List<CsMj> handPais;
	private List<CsMj> outPais;
	private List<CsMj> peng;
	private List<CsMj> aGang;
	private List<CsMj> mGang;
	private List<CsMj> chi;
	private List<CsMj> buzhang;
	private List<CsMj> buzhangAn;
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
	private List<CsMjCardDisType> cardTypes;
	/*** 过小胡的记录*/
	private List<Integer> passXiaoHuList;
	
	/*** 过小胡的记录，出牌后清空*/
	private List<Integer> passXiaoHuList2= new ArrayList<>();
	
	 private long lastPassTime;
		
	
	 private int disCount;
	
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
	private List<CsMj> xiaoHuMjList = new ArrayList<>();
	
	private List<Integer> gangCGangMjs = new ArrayList<>();

	private String robotLastActionMap="";
	private int robotLastActionRepeatCount=1;
	public CsMjPlayer() {
		handPais = new ArrayList<CsMj>();
		outPais = new ArrayList<CsMj>();
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
	}

	public int getSeat() {
		return seat;
	}

	public void setSeat(int seat) {
		this.seat = seat;
	}

	public void initPais(List<CsMj> hand, List<CsMj> out) {
		if (hand != null) {
			this.handPais = hand;

		}
		if (out != null) {
			this.outPais = out;

		}
	}


	public void dealHandPais(List<CsMj> pais) {
		this.handPais = pais;
		getPlayingTable().changeCards(seat);
	}

	public boolean isGangshangHua() {
		return dahu.contains(7);
	}

	public List<CsMj> getHandMajiang() {
		return handPais;
	}

	// 0缺一色 1板板胡 2大四喜 3六六顺
	public List<CsMj> showXiaoHuMajiangs(int xiaohu,boolean connect) {
		CsMjTable table = getPlayingTable(CsMjTable.class);
        if (connect && !xiaoHuMjList.isEmpty()) {
            if (xiaohu == CsMjAction.QUEYISE || xiaohu == CsMjAction.BANBANHU || xiaohu == CsMjAction.YIZHIHUA) {
                // 缺一色 板板胡
                return handPais;
            } else {
                return xiaoHuMjList;
            }
        } else {
            CsMjiangHu hu = new CsMjiangHu();
            List<CsMj> copy2 = new ArrayList<>(getHandMajiang());
            CsMjRule.checkXiaoHu2(hu, copy2, true, table);

            HashMap<Integer, Map<Integer, List<CsMj>>> map = hu.getXiaohuMap();
            Map<Integer, List<CsMj>> cardMap = map.get(xiaohu);
            if (cardMap == null) {
                if (xiaohu == CsMjAction.ZHONGTUSIXI) {
                    xiaohu = 10;
                } else if (xiaohu == CsMjAction.ZHONGTULIULIUSHUN) {
                    xiaohu = 9;
                }
                cardMap = map.get(xiaohu);
            }
            if (xiaohu == 6) {// 缺一色
                return handPais;
            } else if (xiaohu == 7) {// 板板胡
                return handPais;
            } else if (xiaohu == 10 || xiaohu == CsMjAction.ZHONGTUSIXI) {// 大四喜|| xiaohu==

                return getShowCards(cardMap, connect, xiaohu);
            } else if (xiaohu == 9 || xiaohu == CsMjAction.ZHONGTULIULIUSHUN) {// 六六顺
                return getShowCards(cardMap, connect, xiaohu);
            } else if (xiaohu == CsMjAction.SANTONG) {// 三同
                return getShowCards(cardMap, connect, xiaohu);
            } else if (xiaohu == CsMjAction.JINGTONGYUNU) {// 金童玉女
                return getShowCards(cardMap, connect, xiaohu);
            } else if (xiaohu == CsMjAction.JIEJIEGAO) {// 节节高
                return getShowCards(cardMap, connect, xiaohu);
            } else {

                return handPais;
            }
        }
	}

	private List<CsMj> getShowCards(Map<Integer, List<CsMj>> cardMap,boolean connect,int xiaohuType) {
		if(cardMap==null) {
			return handPais;
		}
		List<CsMj> list =null;
		for (Entry<Integer, List<CsMj>> entry : cardMap.entrySet()) {
			
//			if(xiaohuType==CsMjAction.LIULIUSHUN||xiaohuType==CsMjAction.ZHONGTULIULIUSHUN){
//				
//				
//				
//			}else{
//				if (huXiaohuCards.contains(entry.getKey())) {
//						list=entry.getValue();
//				}
//			}
			if(xiaohuType==CsMjAction.DASIXI){
				xiaohuType =CsMjAction.ZHONGTUSIXI;
			}else if(xiaohuType==CsMjAction.LIULIUSHUN){
				xiaohuType =CsMjAction.ZHONGTULIULIUSHUN;
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
		return CsMjHelper.toMajiangIds(handPais);
	}

	public List<Integer> getOutPais() {
		return CsMjHelper.toMajiangIds(outPais);
	}

	public List<CsMj> getOutMajing() {
		return outPais;
	}

	public void moMajiang(CsMj majiang) {
		setPassMajiangVal(0);
		handPais.add(majiang);
		getPlayingTable().changeCards(seat);
		clearPassXiaoHu2();
	}

	public void addOutPais(List<CsMj> cards, int action, int disSeat) {
		handPais.removeAll(cards);
		if (action == 0) {
			outPais.addAll(cards);
		} else {
			if (action == CsMjDisAction.action_buzhang) {
				buzhang.addAll(cards);
				CsMj pengMajiang = cards.get(0);
				Iterator<CsMj> iterator = peng.iterator();
				while (iterator.hasNext()) {
					CsMj majiang = iterator.next();
					if (majiang.getVal() == pengMajiang.getVal()) {
						buzhang.add(majiang);
						iterator.remove();
					}
				}
			} else if (action == CsMjDisAction.action_chi) {
				chi.addAll(cards);

			} else if (action == CsMjDisAction.action_peng) {
				peng.addAll(cards);
				// changeAction(0, 1);
			} else if (action == CsMjDisAction.action_minggang) {
				myExtend.setMjFengshen(FirstmythConstants.firstmyth_index12, 1);
				mGang.addAll(cards);
				if (cards.size() != 1) {
					changeAction(1, 1);
				} else {
					CsMj pengMajiang = cards.get(0);
					Iterator<CsMj> iterator = peng.iterator();
					while (iterator.hasNext()) {
						CsMj majiang = iterator.next();
						if (majiang.getVal() == pengMajiang.getVal()) {
							mGang.add(majiang);
							iterator.remove();
						}
					}
					changeAction(2, 1);
				}

			} else if (action == CsMjDisAction.action_angang) {
				myExtend.setMjFengshen(FirstmythConstants.firstmyth_index12, 1);
				aGang.addAll(cards);
				changeAction(3, 1);
			}
			else if (action == CsMjDisAction.action_buzhang_an) {
				myExtend.setMjFengshen(FirstmythConstants.firstmyth_index12, 1);
				buzhangAn.addAll(cards);
				changeAction(8, 1);
			}
			getPlayingTable().changeExtend();
			addCardType(action, cards, disSeat,0);
		}

		getPlayingTable().changeCards(seat);
	}

	public void addCardType(int action, List<CsMj> disCardList, int disSeat, int layEggId) {
		if (action != 0) {
			if (action == CsMjDisAction.action_buzhang && disCardList.size() == 1) {
				CsMj majiang = disCardList.get(0);
				for (CsMjCardDisType disType : cardTypes) {
					if (disType.getAction() == CsMjDisAction.action_peng) {
						if (disType.isHasCardVal(majiang)) {
							disType.setAction(CsMjDisAction.action_buzhang);
							disType.addCardId(majiang.getId());
							disType.setDisSeat(seat);
							break;
						}
					}
				}
			} else if (action == CsMjDisAction.action_minggang && disCardList.size() == 1) {
				CsMj majiang = disCardList.get(0);
				for (CsMjCardDisType disType : cardTypes) {
					if (disType.getAction() == CsMjDisAction.action_peng) {
						if (disType.isHasCardVal(majiang)) {
							disType.setAction(CsMjDisAction.action_minggang);
							disType.addCardId(majiang.getId());
							disType.setDisSeat(seat);
							break;
						}
					}
				}
			} else {
				CsMjCardDisType type = new CsMjCardDisType();
				type.setAction(action);
				type.setCardIds(CsMjHelper.toMajiangIds(disCardList));
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

	public void removeOutPais(List<CsMj> cards, int action) {
		boolean remove = outPais.removeAll(cards);
		if (remove) {
			if (action == CsMjDisAction.action_peng) {
				changeAction(4, 1);
			} else if (action == CsMjDisAction.action_minggang) {
				changeAction(5, 1);
			}
			getPlayingTable().changeCards(seat);

		}
	}

	public String toExtendStr() {
		StringBuffer sb = new StringBuffer();
		sb.append(CsMjHelper.implodeMajiang(peng, ",")).append("|");
		sb.append(CsMjHelper.implodeMajiang(aGang, ",")).append("|");
		sb.append(CsMjHelper.implodeMajiang(mGang, ",")).append("|");
		sb.append(StringUtil.implode(actionArr)).append("|");
		sb.append(StringUtil.implode(actionTotalArr)).append("|");
		sb.append(StringUtil.implode(passGangValList, ",")).append("|");
		sb.append(CsMjHelper.implodeMajiang(chi, ",")).append("|");
		sb.append(CsMjHelper.implodeMajiang(buzhang, ",")).append("|");
		sb.append(StringUtil.implode(dahu, ",")).append("|");
		sb.append(StringUtil.implode(huXiaohu, ",")).append("|");
		sb.append(StringUtil.implode(passXiaoHuList, ",")).append("|");
		sb.append(StringUtil.implode(passHuValList, ",")).append("|");
		sb.append(CsMjHelper.implodeMajiang(buzhangAn, ",")).append("|");
		sb.append(StringUtil.implode(huXiaohuCards, ",")).append("|");

		String huXiaoHuCardsV = DataMapUtil.explodeListMap(huxiaoHuCardVal);
		sb.append(huXiaoHuCardsV).append("|");

		List<Integer> idList = new ArrayList<>();
		for(CsMj mj : xiaoHuMjList){
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
		peng = CsMjHelper.explodeMajiang(val1, ",");
		String val2 = StringUtil.getValue(values, i++);
		aGang = CsMjHelper.explodeMajiang(val2, ",");
		String val3 = StringUtil.getValue(values, i++);
		mGang = CsMjHelper.explodeMajiang(val3, ",");
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
		chi = CsMjHelper.explodeMajiang(val7, ",");
		String val8 = StringUtil.getValue(values, i++);
		buzhang = CsMjHelper.explodeMajiang(val8, ",");
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
		buzhangAn = CsMjHelper.explodeMajiang(val13, ",");
		
		
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
                xiaoHuMjList.add(CsMj.getMajang(id));
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
        sb.append(disCount).append(",");
        
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
			this.state = CsMjEnumHelper.getPlayerState(stateVal);
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
            disCount= StringUtil.getIntValue(values, i++);
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
			list.addAll(CsMjHelper.toMajiangIds(peng));
		}
		if (!mGang.isEmpty()) {
			list.addAll(CsMjHelper.toMajiangIds(mGang));
		}
		if (!aGang.isEmpty()) {
			list.addAll(CsMjHelper.toMajiangIds(aGang));
		}
		if (!buzhang.isEmpty()) {
			list.addAll(CsMjHelper.toMajiangIds(buzhang));
		}
		if (!buzhangAn.isEmpty()) {
			list.addAll(CsMjHelper.toMajiangIds(buzhangAn));
		}
		if (!chi.isEmpty()) {
			list.addAll(CsMjHelper.toMajiangIds(chi));
		}

		return list;
	}

	public List<PhzHuCards> buildDisCards(long lookUid) {
		return buildDisCards(lookUid, true);
	}

	public List<PhzHuCards> buildDisCards(long lookUid, boolean hide) {
		List<PhzHuCards> list = new ArrayList<>();
		for (CsMjCardDisType type : cardTypes) {
			if (hide && lookUid != this.userId && type.getAction() == CsMjDisAction.action_angang) {
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
		res.addAllAngangIds(CsMjHelper.toMajiangIds(aGang));
		res.addAllAngangIds(CsMjHelper.toMajiangIds(buzhangAn));
		res.addAllMoldCards(buildDisCards(userId));
		List<CsMj> gangList = getGang();
		// 是否杠过
		res.addExt(gangList.isEmpty() ? 0 : 1);
		
		
		CsMjTable table = getPlayingTable(CsMjTable.class);
		// 现在是否自己摸的牌
		res.addExt(isAlreadyMoMajiang() ? 1 : 0);
		res.addExt(handPais != null ? handPais.size() : 0);
		res.addExt(piaoPoint);// 飘分
        res.addExt(autoPlay ? 1 : 0);//4
        res.addExt(autoPlaySelf ? 1 : 0);//5
		
		
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
			 if (gu == null || !groupId.equals(gu.getGroupId() + "") || gu.getTempCredit() > 0) {
				 gu = GroupDao.getInstance().loadGroupUser(getUserId(), groupId);
				 setGroupUser(gu);
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
		setDisCount(0);
		autoPlaySelf = false;
		autoPlay = false;
		lastCheckTime = System.currentTimeMillis();
		checkAutoPlay = false;
		huXiaohuCards.clear();
		huxiaoHuCardVal.clear();
		xiaoHuMjList.clear();
		gangCGangMjs.clear();
        setWinGold(0);
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
		for (CsMjCardDisType type : cardTypes) {
			list.add(type.buildMsg().build());
		}
		res.addAllMoldPais(list);
		res.addAllDahus(dahu);
		res.addAllXiaohus(huXiaohu);
		res.setGoldFlag(getGoldResult());
		//res.setFanPao(actionArr[0]==1?1:0);

		res.addExt(getPiaoPoint());
		res.addExt(gangPoint);
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
		for (CsMjCardDisType type : cardTypes) {
			list.add(type.buildMsg().build());
		}
		
		res.setGoldFlag(getGoldResult());
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
		setDisCount(0);
		huXiaohuCards.clear();
		huxiaoHuCardVal.clear();
		gangCGangMjs.clear();
	}

	@Override
	public void initPais(String handPai, String outPai) {
		if (!StringUtils.isBlank(handPai)) {
			List<Integer> list = StringUtil.explodeToIntList(handPai);
			this.handPais = CsMjHelper.toMajiang(list);
		}

		if (!StringUtils.isBlank(outPai)) {
			String[] values = outPai.split(";");
			int i = -1;
			for (String value : values) {
				i++;
				if (i == 0) {
					List<Integer> list = StringUtil.explodeToIntList(value);
					this.outPais = CsMjHelper.toMajiang(list);
				} else {
					CsMjCardDisType type = new CsMjCardDisType();
					type.init(value);
					cardTypes.add(type);

					List<CsMj> majiangs = CsMjHelper.toMajiang(type.getCardIds());
					if (type.getAction() == CsMjDisAction.action_angang) {
						aGang.addAll(majiangs);
					} else if (type.getAction() == CsMjDisAction.action_minggang) {
						mGang.addAll(majiangs);
					} else if (type.getAction() == CsMjDisAction.action_chi) {
						chi.addAll(majiangs);
					} else if (type.getAction() == CsMjDisAction.action_peng) {
						peng.addAll(majiangs);
					} else if (type.getAction() == CsMjDisAction.action_buzhang) {
						buzhang.addAll(majiangs);
					}else if (type.getAction() == CsMjDisAction.action_buzhang_an) {
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
	public List<CsMj> getCanChiMajiangs(CsMj disMajiang) {
		return CsMjTool.checkChi(handPais, disMajiang);
	}

	/**
	 * 是否听牌
	 * @param val
	 * @return
	 */
	public boolean isTingPai(int val) {
		List<CsMj> copy = new ArrayList<>(handPais);
		List<CsMj> gangList = getGang();
		gangList.addAll(CsMjQipaiTool.dropVal(copy, val));

		List<CsMj> copyPeng = new ArrayList<>(peng);
		if (!peng.isEmpty()) {
			gangList.addAll(CsMjQipaiTool.dropVal(copyPeng, val));
		}

		if (copy.size() % 3 != 2) {
			copy.add(CsMj.getMajang(201));
		}
		List<CsMj> bzCopy = new ArrayList<>(buzhang);
		bzCopy.addAll(buzhangAn);
		boolean jiang258 = true;
		CsMjTable table = getPlayingTable(CsMjTable.class);
		//开杠
		if(table.getJiajianghu()==1) {
			jiang258 =false;
		}
		CsMjiangHu huBean = CsMjTool.isChangshaHu2(copy, gangList, copyPeng, chi, bzCopy,false,jiang258,table,handPais.get(0));
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
		for (CsMjCardDisType huxi : cardTypes) {
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
	public List<Integer> checkDisMajiang(CsMj majiang) {
		return checkDisMaj(majiang,true);
	}

	public List<Integer> checkDisMaj(CsMj majiang,boolean need258) {
		List<Integer> list = new ArrayList<>();
		CsMjAction actData = new CsMjAction();
		CsMjTable table = getPlayingTable(CsMjTable.class);
		// 没有出现漏炮的情况
		// if (passMajiangVal != majiang.getVal()) {
		if ((!table.moHu() || table.getDisCardSeat() == seat) && passMajiangVal == 0) {
			List<CsMj> copy = new ArrayList<>(handPais);
			copy.add(majiang);
			List<CsMj> bzCopy = new ArrayList<>(buzhang);
			bzCopy.addAll(buzhangAn);
			boolean jiang258 = true;
			
			if(table.getJiajianghu()==1) {
				// 没出牌就有人胡了，天胡
		    	CsMjPlayer csplayer = table.getPlayerBySeat(table.getLastWinSeat());
				if(table.getLastWinSeat()==table.getDisCardSeat()&& csplayer!=null&&csplayer.getDisCount()==1) {
					jiang258 = false;
				}else if(table.getLeftMajiangCount()==0) {
					jiang258 = false;
				}else if(!need258) {
					jiang258 = false;
				}
			}
			CsMjiangHu hu = CsMjTool.isChangshaHu2(copy, getGang(), peng, chi, bzCopy,false,jiang258,table,majiang);
			if (hu.isHu()) {
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
		List<CsMj> gangList = getGang();
		if (table.getDisCardSeat() != seat) {
			// 现在出牌的人不是自己
			int count = CsMjHelper.getMajiangCount(handPais, majiang.getVal());
			if (count == 3) {
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
			if(gangList.isEmpty()) {
				if (count >= 2) {
					actData.addPeng();
				}
				if (table.calcNextSeat(table.getDisCardSeat()) == seat) {
			        if(!(table.getMaxPlayerCount()==2 && table.getBuChi()==1)) {
			        	List<CsMj> chi = CsMjTool.checkChi(handPais, majiang);
						if (!chi.isEmpty()) {
							actData.addChi();
						}
			        }
				}
			}
		} else {
			// 出牌的人是自己 (杠后补张)
			int count = CsMjHelper.getMajiangCount(handPais, majiang.getVal());
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
			Map<Integer, Integer> pengMap = CsMjHelper.toMajiangValMap(peng);
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

	public CsMj getLastMoMajiang() {
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
			if(xiaoHuType == CsMjAction.ZHONGTUSIXI ||xiaoHuType == CsMjAction.DASIXI)
			this.huXiaohuCards.add(val);
		}
		changeTbaleInfo();
	}
	
	
	
	public void addLiuLiuShunHu2(int xiaoHuType,List<Integer> mjs) {
		this.huXiaohu.add(xiaoHuType);
		
		if(xiaoHuType==CsMjAction.DASIXI){
			xiaoHuType =CsMjAction.ZHONGTUSIXI;
		}else if(xiaoHuType==CsMjAction.LIULIUSHUN){
			xiaoHuType =CsMjAction.ZHONGTULIULIUSHUN;
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

    public void addXiaoHuMjList(List<CsMj> mjs) {
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
		return CsMjiangHu.getDaHuPointCount(dahu);
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
	public CsMjiangHu checkHu(CsMj disMajiang, boolean isbegin) {
		List<CsMj> copy = new ArrayList<>(handPais);
		if (disMajiang != null) {
			copy.add(disMajiang);
		}
		List<CsMj> bzCopy = new ArrayList<>(buzhang);
		bzCopy.addAll(buzhangAn);
		boolean jiang258 = true;
		CsMjTable table = getPlayingTable(CsMjTable.class);
		
		if(table.getJiajianghu()==1) {
			if( seat !=table.getMoMajiangSeat()) {
				
				CsMjPlayer csplayer = table.getPlayerBySeat(table.getLastWinSeat());
				if( table.getDisCardRound()==1||(table.getLastWinSeat()==table.getDisCardSeat()&&csplayer.getDisCount()==1)) {
					 jiang258 = false;
				}
				CsMj mj  = table.getGangHuMajiang(seat,disMajiang);
				if(mj!=null) {
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
		
		CsMjiangHu hu = CsMjTool.isChangshaHu2(copy, getGang(), peng, chi, bzCopy, isbegin,jiang258,table,disMajiang);
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
	public List<Integer> checkMo(CsMj majiang, boolean isBegin) {
		List<Integer> list = new ArrayList<>();
		CsMjAction actData = new CsMjAction();
		CsMjTable table = getPlayingTable(CsMjTable.class);
		boolean canXiaohu = false;
//		int xiaoHuType = 0;
		if (isAlreadyMoMajiang() || majiang != null || isBegin) {
			// 碰的时候判断不能胡牌
			List<CsMj> bzCopy = new ArrayList<>(buzhang);
			bzCopy.addAll(buzhangAn);
			boolean jiang258 = true;
			if(table.getJiajianghu()==1) {
				if (table.getDisCardRound()==0) {
					jiang258=false;
				}
			}
			List<CsMj> copy = new ArrayList<>(handPais);
			CsMjiangHu hu = CsMjTool.isChangshaHu2(copy, getGang(), peng, chi, bzCopy, isBegin,jiang258,table,majiang);
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
				
				HashMap<Integer, Map<Integer, List<CsMj>>> xiaohuMap = hu.getXiaohuMap();
				for (int index : CsMjAction.xiaoHuPriority) {
					
					Map<Integer, List<CsMj>> xiaoHuCardMap = xiaohuMap.get(index);
					if(xiaoHuCardMap ==null) {
						continue;
					}
					List<Integer> keys = new ArrayList<Integer>();
					if(xiaoHuCardMap.size()==0) {
						keys.add(0);
					}else{
						keys.addAll(xiaoHuCardMap.keySet());
					}
					
					
					if(index == CsMjAction.ZHONGTULIULIUSHUN){
						boolean canLiu = true;
						
						List<Integer> cardVals = huxiaoHuCardVal.get(index);
						
						List<Integer> cardVals2 = huxiaoHuCardVal.get(CsMjAction.LIULIUSHUN);
						
						List<Integer> liuliuShunVals = new ArrayList<Integer>();
						
						if(cardVals!=null){
							liuliuShunVals.addAll(cardVals);
						}
						
						if(cardVals2!=null){
							liuliuShunVals.addAll(cardVals2);
						}
						List<CsMj>  mjs = xiaoHuCardMap.get(keys.get(0));
						if(mjs!=null){
							for(CsMj mj: mjs){
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
							actData = new CsMjAction();
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
		if (isAlreadyMoMajiang() && !canXiaohu) {
			List<CsMj> gangList = getGang();
			Map<Integer, Integer> pengMap = CsMjHelper.toMajiangValMap(peng);

			for (CsMj handMajiang : handPais) {
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
			Map<Integer, Integer> handMap = CsMjHelper.toMajiangValMap(handPais);
			if (handMap.containsValue(4)) {
				for (Map.Entry<Integer, Integer> entry : handMap.entrySet()) {
					if (entry.getValue() == 4) {
						boolean isTing = isTingPai(entry.getKey());
						//杠过之后不能再暗杠了
						if (isTing&&gangList.isEmpty()) {
							actData.addAnGang();// 可以杠
							actData.addBuZhangAn(); // 可以补张
							break;
						}else{
							if(gangList.isEmpty()){
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
		
//		if(table.getXiaoHuAuto()==1&&xiaoHuType>0){
//			table.addActionSeat(getSeat(), list);
//			List<CsMj> majiangs = new ArrayList<>(showXiaoHuMajiangs(xiaoHuType,false));
//			table.huXiaoHu(this, majiangs, xiaoHuType, CsMjDisAction.action_xiaohu);
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

	public List<CsMj> getPeng() {
		return peng;
	}

	public List<CsMj> getaGang() {
		return aGang;
	}

	public List<CsMj> getmGang() {
		return mGang;
	}

	public List<CsMj> getGang() {
		List<CsMj> gang = new ArrayList<>();
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
	

	public int getDisCount() {
		return disCount;
	}

	public void setDisCount(int disCount) {
		this.disCount = disCount;
		changeTbaleInfo();
	}

	/**
	 * 可以碰可以杠的牌 选择了碰 再杠不算分
	 *
	 * @param majiang
	 * @return
	 */
	public boolean isPassGang(CsMj majiang) {
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

	public static final List<Integer> wanfaList = Arrays.asList(GameUtil.game_type_csmj);

	public static void loadWanfaPlayers(Class<? extends Player> cls){
		for (Integer integer:wanfaList){
			PlayerManager.wanfaPlayerTypesPut(integer,cls, CsMjCommandProcessor.getInstance());
			 RobotManager.regAIGenerator(integer, CsMjRobotAIGenerator.getInst());
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
    	CsMjTable table = getPlayingTable(CsMjTable.class);
        long now = System.currentTimeMillis();
        boolean auto = isAutoPlay();
        if (!auto && table.getIsAutoPlay() > 0) {
            //检查玩家是否进入系统托管状态
            if (!checkAutoPlay && table.getTableStatus()!= CsMjConstants.TABLE_STATUS_PIAO) {
                if (isAlreadyMoMajiang() || table.getActionSeatMap().containsKey(seat)||seat==table.getAskLastMajaingSeat()) {
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
                    if (timeOut >= CsMjConstants.AUTO_READY_TIME) {
                        setAutoPlayTime(0);
                        return true;
                    }
                } else if (autoType == 2) {
                    if (timeOut >= CsMjConstants.AUTO_HU_TIME) {
                        setAutoPlayTime(0);
                        return true;
                    }
                } else {
                    if (timeOut >=CsMjConstants.AUTO_PLAY_TIME) {
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
        CsMjTable table = getPlayingTable(CsMjTable.class);
        if (this.autoPlay != autoPlay || autoPlaySelf != isSelf) {
            ComMsg.ComRes.Builder res = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_tuoguan, seat, autoPlay ? 1 : 0, 0, isSelf ? 1 : 0);
            GeneratedMessage msg = res.build();
            if (table != null) {
                table.broadMsgToAll(msg);
            }
            if (!getHandPais().isEmpty()) {
            table.addPlayLog(table.getDisCardRound() + "_" +getSeat() + "_" + CsMjConstants.action_tuoguan + "_" +(autoPlay?1:0));
            }

            StringBuilder sb = new StringBuilder("CsMj");
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
        boolean needLog = this.autoPlay != autoPlay || this.autoPlaySelf != isSelf;
        this.autoPlay = autoPlay;
        this.autoPlaySelf = autoPlay && isSelf;
        this.checkAutoPlay = autoPlay;
        setLastCheckTime(System.currentTimeMillis());
        if (needLog && table != null) {
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
		if (action == CsMjAction.ZHONGTUSIXI && huXiaohu.contains(CsMjAction.DASIXI)) {
			return false;
		} else if (action == CsMjAction.ZHONGTULIULIUSHUN && huXiaohu.contains(CsMjAction.LIULIUSHUN)) {
			return false;
		}
		return true;
	}
	
	
	public boolean canHuXiaoHu2(int action,int val) {
		int key = action;
		if(key==CsMjAction.DASIXI){
			key =CsMjAction.ZHONGTUSIXI;
		}else if(key==CsMjAction.LIULIUSHUN){
			key =CsMjAction.ZHONGTULIULIUSHUN;
		}
		List<Integer> vals = huxiaoHuCardVal.get(key);
		if(vals ==null){
			vals = new ArrayList<Integer>();
		}
		
		if (huXiaohu.contains(action)) {
			if(action == CsMjAction.YIZHIHUA || action == CsMjAction.QUEYISE ||action == CsMjAction.JINGTONGYUNU || action == CsMjAction.BANBANHU) {
				return false;
			}
//			if(action == CsMjAction.ZHONGTULIULIUSHUN||action == CsMjAction.LIULIUSHUN){
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
		
		if (action == CsMjAction.ZHONGTUSIXI && huXiaohu.contains(CsMjAction.DASIXI)&& vals.contains(val)) {
			return false;
		}
		
		else if (action == CsMjAction.ZHONGTULIULIUSHUN && huXiaohu.contains(CsMjAction.LIULIUSHUN)&& vals.contains(val)) {
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
			if(xiaoHu != CsMjAction.ZHONGTUSIXI && xiaoHu != CsMjAction.DASIXI && xiaoHu !=CsMjAction.ZHONGTULIULIUSHUN && xiaoHu !=CsMjAction.LIULIUSHUN) {
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
	
	public List<Integer> isPassHu(){
		return passHuValList;
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
			passXiaoHuList2.add(CsMjAction.ZHONGTUSIXI);
		}else if(xiaoHu==9) {
			passXiaoHuList2.add(CsMjAction.ZHONGTULIULIUSHUN);
		}
		
	}

	public List<Integer> getHuXiaohuCards() {
		return huXiaohuCards;
	}
	

	public List<CsMj> getChi() {
		return chi;
	}
	
	public List<CsMj> getBuzhang() {
		List<CsMj> bzCopy = new ArrayList<>(buzhang);
		bzCopy.addAll(buzhangAn);
		return bzCopy;
	}
	
	public void removeGangMj(CsMj mj){
		buzhang.remove(mj);
		mGang.remove(mj);
		for (CsMjCardDisType type : cardTypes) {
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

	public String getRobotLastActionMap() {
		return robotLastActionMap;
	}

	public void setRobotLastActionMap(String RobotLastActionMap) {
		robotLastActionMap = RobotLastActionMap;
	}

	public int getRobotLastActionRepeatCount() {
		return robotLastActionRepeatCount;
	}

	public void setRobotLastActionRepeatCount(int robotLastActionRepeatCount) {
		this.robotLastActionRepeatCount = robotLastActionRepeatCount;
	}


}
