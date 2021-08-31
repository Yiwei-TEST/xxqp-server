package com.sy599.game.qipai.dehmj.bean;

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
import com.sy599.game.qipai.dehmj.command.DehMjCommandProcessor;
import com.sy599.game.qipai.dehmj.constant.DehMjAction;
import com.sy599.game.qipai.dehmj.constant.DehMjConstants;
import com.sy599.game.qipai.dehmj.rule.DehMj;
import com.sy599.game.qipai.dehmj.rule.DehMjHelper;
import com.sy599.game.qipai.dehmj.rule.DehMjIndex;
import com.sy599.game.qipai.dehmj.rule.DehMjIndexArr;
import com.sy599.game.qipai.dehmj.tool.DehMjEnumHelper;
import com.sy599.game.qipai.dehmj.tool.DehMjQipaiTool;
import com.sy599.game.qipai.dehmj.tool.DehMjTool;
import com.sy599.game.util.GameUtil;
import com.sy599.game.util.JacksonUtil;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.util.StringUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

public class DehMjPlayer extends Player {
	// 座位id
	private int seat;
	// 状态
	private player_state state;// 1进入 2已准备 3正在玩 4已结束
	private int isEntryTable;
	private List<DehMj> handPais;
	private List<DehMj> outPais;
	private List<DehMj> peng;
	private List<DehMj> aGang;
	private List<DehMj> mGang;
	private List<DehMj> chi;
	private List<DehMj> buzhang;
	private List<DehMj> buzhangAn;
	private int winCount;
	private int lostCount;
	private int lostPoint;
	private int gangPoint;// 要胡牌杠才算分
	private int point;
	/** 0点炮1杠2明杠3暗杠4被碰5被杠6胡7自摸 **/
	private int[] actionArr = new int[10];
	/** 0点炮1杠2明杠3暗杠4被碰5被杠6胡7自摸 **/
	private int[] actionTotalArr = new int[9];
	private List<Integer> huXiaohu;
	private List<Integer> dahu;
	private int passMajiangVal;
	private List<Integer> passGangValList;
	private List<DehMjCardDisType> cardTypes;
	/*** 过小胡的记录，出牌后清空*/
	private List<Integer> passXiaoHuList;
	/*** 胡的牌*/
	private List<Integer> huMjIds;
	
	
    private volatile boolean autoPlay = false;//是否进入托管状态
    private volatile boolean autoPlaySelf = false;//托管
    private volatile long lastCheckTime = 0;//最后检查时间
    private volatile long autoPlayTime = 0;//自动操作时间
    private volatile boolean checkAutoPlay = false; //是否是牌桌上的焦点
    private volatile long sendAutoTime = 0;//发送倒计时间
	
    /**
     * [类型]=分值
     */
    private int[] pointArr = new int[4];
	
    
    private int[] gangRemainArr = new int[2];
    
    
    private int passPengMajVal;
    
    
	/**
	 * 
	 * 
	 * 买点数
	 */
	private int buyPoint = -1;
	/**大胡的分*/
	private int dahuFan = 0;
	/**连续*/
	private int conGangNum;
	
	/**报听*/
	private int baoTingS = -1;
	

	/*** 过胡了的牌，过手后清空**/
	private List<Integer> passHuValList;

	public DehMjPlayer() {
		handPais = new ArrayList<DehMj>();
		outPais = new ArrayList<DehMj>();
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
		buyPoint = -1;
		passXiaoHuList = new ArrayList<>();
		passHuValList = new ArrayList<>();
		huMjIds = new ArrayList<>();
		baoTingS = -1;
	}

	public int getSeat() {
		return seat;
	}

	public void setSeat(int seat) {
		this.seat = seat;
	}

	public void initPais(List<DehMj> hand, List<DehMj> out) {
		if (hand != null) {
			this.handPais = hand;

		}
		if (out != null) {
			this.outPais = out;

		}
	}

	public void dealHandPais(List<DehMj> pais) {
		this.handPais = pais;
		getPlayingTable().changeCards(seat);
	}

	public boolean isGangshangHua() {
		return dahu.contains(7);
	}

	public List<DehMj> getHandMajiang() {
		return handPais;
	}

	// 0缺一色 1板板胡 2大四喜 3六六顺
	public List<DehMj> showXiaoHuMajiangs(int xiaohu) {
		if (xiaohu == 0) {// 缺一色
			return handPais;
		} else if (xiaohu == 1) {// 板板胡
			return handPais;
		} else if (xiaohu == 2) {// 大四喜
			DehMjIndexArr card_index = new DehMjIndexArr();
			DehMjQipaiTool.getMax(card_index, handPais);
			return card_index.getMajiangIndex(3).getMajiangs();
		} else if (xiaohu == 3) {// 六六顺
			DehMjIndexArr card_index = new DehMjIndexArr();
			DehMjQipaiTool.getMax(card_index, handPais);
			DehMjIndex index = card_index.getMajiangIndex(2);
			return index.getMajiangs();
		} else {

			return handPais;
		}
	}

	public List<Integer> getHandPais() {
		return DehMjHelper.toMajiangIds(handPais);
	}

	public List<Integer> getOutPais() {
		return DehMjHelper.toMajiangIds(outPais);
	}

	public List<DehMj> getOutMajing() {
		return outPais;
	}

	public void moMajiang(DehMj majiang) {
		setPassMajiangVal(0);
		handPais.add(majiang);
		getPlayingTable().changeCards(seat);
	}

	public void addOutPais(List<DehMj> cards, int action, int disSeat) {
		handPais.removeAll(cards);
		if (action == 0) {
			outPais.addAll(cards);
		} else {
			if (action == DehMjDisAction.action_buzhang) {
				buzhang.addAll(cards);
				DehMj pengMajiang = cards.get(0);
				Iterator<DehMj> iterator = peng.iterator();
				while (iterator.hasNext()) {
					DehMj majiang = iterator.next();
					if (majiang.getVal() == pengMajiang.getVal()) {
						buzhang.add(majiang);
						iterator.remove();
					}
				}
			} else if (action == DehMjDisAction.action_chi) {
				chi.addAll(cards);

			} else if (action == DehMjDisAction.action_peng) {
				peng.addAll(cards);
				// changeAction(0, 1);
			} else if (action == DehMjDisAction.action_minggang) {
				myExtend.setMjFengshen(FirstmythConstants.firstmyth_index12, 1);
				mGang.addAll(cards);
				if (cards.size() != 1) {
					changeAction(1, 1);
				} else {
					DehMj pengMajiang = cards.get(0);
					Iterator<DehMj> iterator = peng.iterator();
					while (iterator.hasNext()) {
						DehMj majiang = iterator.next();
						if (majiang.getVal() == pengMajiang.getVal()) {
							mGang.add(majiang);
							iterator.remove();
						}
					}
					changeAction(2, 1);
				}
				changeActionTotal(2, 1);

			} else if (action == DehMjDisAction.action_angang) {
				myExtend.setMjFengshen(FirstmythConstants.firstmyth_index12, 1);
				aGang.addAll(cards);
				changeAction(3, 1);
				changeActionTotal(3, 1);
			}
			else if (action == DehMjDisAction.action_buzhang_an) {
				myExtend.setMjFengshen(FirstmythConstants.firstmyth_index12, 1);
				buzhangAn.addAll(cards);
				changeAction(8, 1);
				
			}
			getPlayingTable().changeExtend();
			addCardType(action, cards, disSeat,0);
		}

		getPlayingTable().changeCards(seat);
	}

	public void addCardType(int action, List<DehMj> disCardList, int disSeat, int layEggId) {
		if (action != 0) {
			if (action == DehMjDisAction.action_buzhang && disCardList.size() == 1) {
				DehMj majiang = disCardList.get(0);
				for (DehMjCardDisType disType : cardTypes) {
					if (disType.getAction() == DehMjDisAction.action_peng) {
						if (disType.isHasCardVal(majiang)) {
							disType.setAction(DehMjDisAction.action_buzhang);
							disType.addCardId(majiang.getId());
							disType.setDisSeat(seat);
							break;
						}
					}
				}
			} else if (action == DehMjDisAction.action_minggang && disCardList.size() == 1) {
				DehMj majiang = disCardList.get(0);
				for (DehMjCardDisType disType : cardTypes) {
					if (disType.getAction() == DehMjDisAction.action_peng) {
						if (disType.isHasCardVal(majiang)) {
							disType.setAction(DehMjDisAction.action_minggang);
							disType.addCardId(majiang.getId());
							disType.setDisSeat(seat);
							break;
						}
					}
				}
			} else {
				DehMjCardDisType type = new DehMjCardDisType();
				type.setAction(action);
				type.setCardIds(DehMjHelper.toMajiangIds(disCardList));
				type.setHux(layEggId);
				type.setDisSeat(disSeat);
				cardTypes.add(type);
			}
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
        DehMjTable table = getPlayingTable(DehMjTable.class);
        long now = System.currentTimeMillis();
        boolean auto = isAutoPlay();
        if (!auto && table.getIsAutoPlay() > 1) {
            //检查玩家是否进入系统托管状态
            if (!checkAutoPlay) {
                if (isAlreadyMoMajiang() || table.getActionSeatMap().containsKey(seat)||getBaoTingS()==0) {
                    setCheckAutoPlay(true);
                } else {
                    setCheckAutoPlay(false);
                    return false;
                }
            }

            int timeOut = (int) (now - getLastCheckTime()) / 1000;
            if (timeOut >=  table.getIsAutoPlay()) {
                //进入托管状态
                auto = true;
                setAutoPlay(true, false);
                setAutoPlayCheckedTime( table.getIsAutoPlay()); //进入过托管就启用防恶意托管
            } else {
                if (sendAutoTime == 0) {
                    sendAutoTime = now;
                    setLastCheckTime(now);
                    timeOut = (int) (now - getLastCheckTime()) / 1000;
                }
//                if (timeOut >= 10) {
//                    addAutoPlayCheckedTime(1);
//                }
                int autoTimeOut =  table.getIsAutoPlay();
//                if (getAutoPlayCheckedTime() >=  table.getIsAutoPlay()) {
//                    //进入防恶意托管
//                    if (timeOut >=  table.getIsAutoPlay()) {
//                        auto = true;
//                        setAutoPlay(true, false);
//                    }
//                    autoTimeOut =  table.getIsAutoPlay();
//                }
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
                    if (timeOut >= DehMjConstants.AUTO_READY_TIME) {
                        setAutoPlayTime(0);
                        return true;
                    }
                } else if (autoType == 2) {
                    if (timeOut >= DehMjConstants.AUTO_HU_TIME) {
                        setAutoPlayTime(0);
                        return true;
                    }
                } else {
                    if (timeOut >= DehMjConstants.AUTO_PLAY_TIME) {
                        setAutoPlayTime(0);
                        return true;
                    }
                }
            }
        }
        return false;
    }
	

	/**
	 * 已经摸过牌了
	 *
	 * @return
	 */
	public boolean isAlreadyMoMajiang() {
		return !handPais.isEmpty() && handPais.size() % 3 == 2;
	}

	public void removeOutPais(List<DehMj> cards, int action) {
		boolean remove = outPais.removeAll(cards);
		if (remove) {
			if (action == DehMjDisAction.action_peng) {
				changeAction(4, 1);
			} else if (action == DehMjDisAction.action_minggang) {
				changeAction(5, 1);
			}
			getPlayingTable().changeCards(seat);

		}
	}

	public String toExtendStr() {
		StringBuffer sb = new StringBuffer();
		sb.append(DehMjHelper.implodeMajiang(peng, ",")).append("|");
		sb.append(DehMjHelper.implodeMajiang(aGang, ",")).append("|");
		sb.append(DehMjHelper.implodeMajiang(mGang, ",")).append("|");
		sb.append(StringUtil.implode(actionArr)).append("|");
		sb.append(StringUtil.implode(actionTotalArr)).append("|");
		sb.append(StringUtil.implode(passGangValList, ",")).append("|");
		sb.append(DehMjHelper.implodeMajiang(chi, ",")).append("|");
		sb.append(DehMjHelper.implodeMajiang(buzhang, ",")).append("|");
		sb.append(StringUtil.implode(dahu, ",")).append("|");
		sb.append(StringUtil.implode(huXiaohu, ",")).append("|");
		sb.append(StringUtil.implode(passXiaoHuList, ",")).append("|");
		sb.append(StringUtil.implode(passHuValList, ",")).append("|");
		sb.append(DehMjHelper.implodeMajiang(buzhangAn, ",")).append("|");
		
		sb.append(StringUtil.implode(gangRemainArr)).append("|");
		return sb.toString();

	}

	public void initExtend(String info) {
		if (StringUtils.isBlank(info)) {
			return;
		}
		int i = 0;
		String[] values = info.split("\\|");
		String val1 = StringUtil.getValue(values, i++);
		peng = DehMjHelper.explodeMajiang(val1, ",");
		String val2 = StringUtil.getValue(values, i++);
		aGang = DehMjHelper.explodeMajiang(val2, ",");
		String val3 = StringUtil.getValue(values, i++);
		mGang = DehMjHelper.explodeMajiang(val3, ",");
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
		chi = DehMjHelper.explodeMajiang(val7, ",");
		String val8 = StringUtil.getValue(values, i++);
		buzhang = DehMjHelper.explodeMajiang(val8, ",");
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
		buzhangAn = DehMjHelper.explodeMajiang(val13, ",");
		
		
		String val14 = StringUtil.getValue(values, i++);
		if (!StringUtils.isBlank(val14)) {
			gangRemainArr = StringUtil.explodeToIntArray(val14);
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
		sb.append(buyPoint).append(",");
		sb.append(passPengMajVal).append(",");
		
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
			this.state = DehMjEnumHelper.getPlayerState(stateVal);
			this.isEntryTable = StringUtil.getIntValue(values, i++);
			this.winCount = StringUtil.getIntValue(values, i++);
			this.lostCount = StringUtil.getIntValue(values, i++);
			this.point = StringUtil.getIntValue(values, i++);
			setTotalPoint(StringUtil.getIntValue(values, i++));
			this.lostPoint = StringUtil.getIntValue(values, i++);
			this.passMajiangVal = StringUtil.getIntValue(values, i++);
			this.gangPoint = StringUtil.getIntValue(values, i++);
			this.buyPoint = StringUtil.getIntValue(values, i++);
			passPengMajVal= StringUtil.getIntValue(values, i++);
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
			list.addAll(DehMjHelper.toMajiangIds(peng));
		}
		if (!mGang.isEmpty()) {
			list.addAll(DehMjHelper.toMajiangIds(mGang));
		}
		if (!aGang.isEmpty()) {
			list.addAll(DehMjHelper.toMajiangIds(aGang));
		}
		if (!buzhang.isEmpty()) {
			list.addAll(DehMjHelper.toMajiangIds(buzhang));
		}
		if (!buzhangAn.isEmpty()) {
			list.addAll(DehMjHelper.toMajiangIds(buzhangAn));
		}
		if (!chi.isEmpty()) {
			list.addAll(DehMjHelper.toMajiangIds(chi));
		}

		return list;
	}
	
	
	
	public boolean isChiPengGang(){
		if(chi.size() >0 || mGang.size() >0 || peng.size()>0) {
			return true;
		}
		return false;
	}
	
	public int getGenNum(){
		HashMap<Integer,Integer> map = new HashMap<Integer,Integer>();
		getMjNum(map,getHandMajiang());
		if(chi.size() >0){
			getMjNum(map,chi);
		}
		
		if(mGang.size() >0){
			getMjNum(map,mGang);
		}
		
		if(aGang.size() >0){
			getMjNum(map,aGang);
		}
		
		if(peng.size() >0){
			getMjNum(map,peng);
		}
		
		
		DehMjTable table = getPlayingTable(DehMjTable.class);
		
		if(table!=null&&!isAlreadyMoMajiang()){
			DehMj mj = null;
			if(!huMjIds.isEmpty()){
				mj = DehMj.getMajang(huMjIds.get(0));
			}else{
				if(!table.getNowDisCardIds().isEmpty()){
					mj = table.getNowDisCardIds().get(0);
				}
			}
			if(mj!=null){
				Integer count  = map.get(mj.getVal());
				if(count==null){
					map.put(mj.getVal(), 1);
				}else{
					map.put(mj.getVal(), count+1);
				}
			}
		}
		
		int genNum = 0;
		for(Entry<Integer, Integer> entry: map.entrySet()){
			int val = entry.getValue();
			if(val>=4){
				genNum++;
			}
		}
		return genNum;
		
		
	}

	private void getMjNum(HashMap<Integer, Integer> map,List<DehMj> mjs) {
		for(DehMj mj : mjs){
			Integer num = map.get(mj.getVal());
			if(num==null){
				map.put(mj.getVal(), 1);
			}else{
				map.put(mj.getVal(), num+1);
			}
		}
	}

	public List<PhzHuCards> buildDisCards(long lookUid) {
		return buildDisCards(lookUid, true);
	}

	public List<PhzHuCards> buildDisCards(long lookUid, boolean hide) {
		List<PhzHuCards> list = new ArrayList<>();
		for (DehMjCardDisType type : cardTypes) {
			if (hide && lookUid != this.userId && type.getAction() == DehMjDisAction.action_angang) {
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
		res.addAllAngangIds(DehMjHelper.toMajiangIds(aGang));
		res.addAllAngangIds(DehMjHelper.toMajiangIds(buzhangAn));
		res.addAllMoldCards(buildDisCards(userId));
		List<DehMj> gangList = getGang();
		// 是否杠过
		res.addExt(gangList.isEmpty() ? 0 : 1);
		DehMjTable table = getPlayingTable(DehMjTable.class);
		// 现在是否自己摸的牌
		res.addExt(table != null ? table.getMoMajiangSeat() == seat ? 1 : 0 : 0);
		res.addExt(handPais != null ? handPais.size() : 0);
		res.addExt(buyPoint);// 买点
		//res.addExt(Integer.valueOf(getPayBindId()+"")); //绑定邀请码
		
		res.addExt(autoPlay?1:0); //托管
		res.addExt(baoTingS); //报听
		
		
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

		if (isrecover) {
			// 0是否要的起 1是否报单 2是否暂离(1暂离0在线)
			List<Integer> recover = new ArrayList<>();
			recover.add(isEntryTable);
			res.addAllRecover(recover);
		}
		
		
		
		 if (table.isCreditTable()) {
	            GroupUser gu = getGroupUser();
	            String groupId = table.loadGroupId();
	            if (gu == null || !groupId.equals(gu.getGroupId() + "")) {
	                gu = GroupDao.getInstance().loadGroupUser(getUserId(), groupId);
	            }
	            res.setCredit(gu != null ? gu.getCredit() : 0);
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
		this.buyPoint = piaoPoint;
		changeTableInfo();
	}

	public int getPiaoPoint() {
		return buyPoint;
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
		actionArr = new int[10];
		actionTotalArr = new int[9];
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
        autoPlaySelf = false;
        autoPlay = false;
        lastCheckTime = System.currentTimeMillis();
        checkAutoPlay = false;
        
		huMjIds = new ArrayList<>();
		saveBaseInfo();
		 pointArr = new int[4];
		 setBaoTingS(-1);
		 gangRemainArr = new int[2];
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
		for (DehMjCardDisType type : cardTypes) {
			list.add(type.buildMsg().build());
		}
		res.addAllMoldPais(list);
		res.addAllDahus(dahu);
		res.addAllXiaohus(huXiaohu);

		res.addExt(getPiaoPoint());
		res.addExt(getGangPoint());
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
		
		 res.addAllPointArr(Arrays.asList(ArrayUtils.toObject(pointArr)));
		if (!StringUtils.isBlank(getHeadimgurl())) {
			res.setIcon(getHeadimgurl());
		} else {
			res.setIcon("");
		}
		res.setWinCount(winCount);
		res.setSex(sex);
		res.addAllHandPais(getHandPais());
		List<PhzHuCards> list = new ArrayList<>();
		for (DehMjCardDisType type : cardTypes) {
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
		actionArr = new int[10];
		dahu = new ArrayList<>();
		dahuFan=0;
		conGangNum=0;
		getPlayingTable().changeExtend();
		getPlayingTable().changeCards(seat);
		changeState(player_state.entry);
		//setPiaoPoint(-1);
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
        pointArr = new int[4];
        gangRemainArr= new int[2];
        setBaoTingS(-1);
	}

	@Override
	public void initPais(String handPai, String outPai) {
		if (!StringUtils.isBlank(handPai)) {
			List<Integer> list = StringUtil.explodeToIntList(handPai);
			this.handPais = DehMjHelper.toMajiang(list);
		}

		if (!StringUtils.isBlank(outPai)) {
			String[] values = outPai.split(";");
			int i = -1;
			for (String value : values) {
				i++;
				if (i == 0) {
					List<Integer> list = StringUtil.explodeToIntList(value);
					this.outPais = DehMjHelper.toMajiang(list);
				} else {
					DehMjCardDisType type = new DehMjCardDisType();
					type.init(value);
					cardTypes.add(type);

					List<DehMj> majiangs = DehMjHelper.toMajiang(type.getCardIds());
					if (type.getAction() == DehMjDisAction.action_angang) {
						aGang.addAll(majiangs);
					} else if (type.getAction() == DehMjDisAction.action_minggang) {
						mGang.addAll(majiangs);
					} else if (type.getAction() == DehMjDisAction.action_chi) {
						chi.addAll(majiangs);
					} else if (type.getAction() == DehMjDisAction.action_peng) {
						peng.addAll(majiangs);
					} else if (type.getAction() == DehMjDisAction.action_buzhang) {
						buzhang.addAll(majiangs);
					}else if (type.getAction() == DehMjDisAction.action_buzhang_an) {
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
	public List<DehMj> getCanChiMajiangs(DehMj disMajiang) {
		return DehMjTool.checkChi(handPais, disMajiang);
	}

	/**
	 * 是否听牌
	 * @param val
	 * @return
	 */
	public boolean isTingPai(DehMj mj) {
		List<DehMj> copy = new ArrayList<>(handPais);
		List<DehMj> gangList = getGang();
		gangList.addAll(DehMjQipaiTool.dropVal(copy, mj.getVal()));

		List<DehMj> copyPeng = new ArrayList<>(peng);
		if (!peng.isEmpty()) {
			gangList.addAll(DehMjQipaiTool.dropVal(copyPeng, mj.getVal()));
		}

		if (copy.size() % 3 != 2) {
			copy.add(DehMj.getMajang(201));
		}
		List<DehMj> bzCopy = new ArrayList<>(buzhang);
		bzCopy.addAll(buzhangAn);
		DehMjTable table2 = getPlayingTable(DehMjTable.class);
		DehMjiangHu huBean = DehMjTool.isHuBaoShan(copy,  table2,this,mj,false);
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
		for (DehMjCardDisType huxi : cardTypes) {
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
	public List<Integer> checkDisMajiang(DehMj majiang) {
		List<Integer> list = new ArrayList<>();
		DehMjAction actData = new DehMjAction();
		DehMjTable table = getPlayingTable(DehMjTable.class);
		// 没有出现漏炮的情况
		// if (passMajiangVal != majiang.getVal()) {
		if ((!table.moHu() || table.getDisCardSeat() == seat) && passMajiangVal == 0) {
			List<DehMj> copy = new ArrayList<>(handPais);
			copy.add(majiang);
			List<DehMj> bzCopy = new ArrayList<>(buzhang);
			bzCopy.addAll(buzhangAn);
			DehMjiangHu hu = DehMjTool.isHuBaoShan(copy,table,this,majiang,false);
			if (hu.isHu()) {
				actData.addHu();
			}
		}
		List<DehMj> gangList = getGang();
		if (table.getDisCardSeat() != seat) {
			// 现在出牌的人不是自己
			int count = DehMjHelper.getMajiangCount(handPais, majiang.getVal());
			if (count == 3) {
				//boolean isTing = isTingPai(majiang.getVal());
				actData.addMingGang();
//				if (isTing) {
//					actData.addMingGang();
//					actData.addBuZhang();
//				}else{
//					if(gangList.isEmpty()){
//						actData.addBuZhang();
//					}
//				}
			}
//			if(gangList.isEmpty()) {
				if (count >= 2&& (passPengMajVal != majiang.getVal())) {
					actData.addPeng();
				}
//				if (table.calcNextSeat(table.getDisCardSeat()) == seat) {
//					List<DehMj> chi = DehMjTool.checkChi(handPais, majiang);
//					if (!chi.isEmpty()) {
//						actData.addChi();
//					}
//				}
//			}
		} else {
			// 出牌的人是自己 (杠后补张)
			int count = DehMjHelper.getMajiangCount(handPais, majiang.getVal());
			if (count == 3) {
				actData.addAnGang();
//				boolean isTing = isTingPai(majiang.getVal());
//				if (isTing) {
//				
//					actData.addBuZhangAn();
//				}else{
//					if(gangList.isEmpty()){
//						actData.addBuZhangAn();
//					}
//				}
			}
			Map<Integer, Integer> pengMap = DehMjHelper.toMajiangValMap(peng);
			if (pengMap.containsKey(majiang.getVal())) {
				//boolean isTing = isTingPai(majiang.getVal());
				actData.addMingGang();
//				if (isTing) {
//					
//					actData.addBuZhang();
//				}else{
//					if(gangList.isEmpty()){
//						actData.addBuZhang();
//					}
//				}
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

	public DehMj getLastMoMajiang() {
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

	public int getDahuFan(){
		if(dahu == null || dahu.size()==0){
			return 0;
		}
		return dahuFan;
	}

	public List<Integer> getDahu() {
		return dahu;
	}

	public void setDahu(List<Integer> dahu,int fan) {
		this.dahu = dahu;
		this.dahuFan = fan;
		getPlayingTable().changeExtend();
	}

	/**
	 * 胡牌
	 *
	 * @param disMajiang
	 * @param isbegin
	 * @return
	 */
	public DehMjiangHu checkHu(DehMj disMajiang, boolean isbegin) {
		List<DehMj> copy = new ArrayList<>(handPais);
		if (disMajiang != null) {
			copy.add(disMajiang);
		}
		List<DehMj> bzCopy = new ArrayList<>(buzhang);
		bzCopy.addAll(buzhangAn);
		
		if(disMajiang == null) {
			if(huMjIds.size()>0) {
				disMajiang = DehMj.getMajang(huMjIds.get(0));
			}
			if(disMajiang == null) {
				disMajiang = getLastMoMajiang();
			}
		}
		
		DehMjiangHu hu = null;
		if(!isChiPengGang() && getActionNum(8)>=10) {
			hu = new DehMjiangHu();
			hu.setShilaotou(true);
			hu.setHu(true);
			hu.initDahuList();
			return hu;
		}
		DehMjTable table2 = getPlayingTable(DehMjTable.class);
		hu = DehMjTool.isHuBaoShan(copy,table2,this,disMajiang,false);
		
		return hu;
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
	public List<Integer> checkMo(DehMj majiang, boolean isBegin) {
		List<Integer> list = new ArrayList<>();
		DehMjAction actData = new DehMjAction();
		DehMjTable table = getPlayingTable(DehMjTable.class);
		boolean canXiaohu = false;
		if (isAlreadyMoMajiang() || majiang != null || isBegin) {
			// 碰的时候判断不能胡牌
			List<DehMj> bzCopy = new ArrayList<>(buzhang);
			bzCopy.addAll(buzhangAn);
			DehMjTable table2 = getPlayingTable(DehMjTable.class);
			DehMjiangHu hu = DehMjTool.isHuBaoShan(handPais, table2,this,majiang,false);
			if (hu.isHu()) {
                actData.addZiMo();
			}
			
		}
		if (isAlreadyMoMajiang() && !canXiaohu) {
			List<DehMj> gangList = getGang();
			Map<Integer, Integer> pengMap = DehMjHelper.toMajiangValMap(peng);

			for (DehMj handMajiang : handPais) {
				if (pengMap.containsKey(handMajiang.getVal())) {
					// 有碰过
					//boolean isTing = isTingPai(handMajiang.getVal());
					actData.addMingGang();// 可以杠
					
//					if (isTing) {
//						
//						actData.addBuZhang();// 可以补张
//						break;
//					}else{
//						if(gangList.isEmpty()){
//							actData.addBuZhang();// 可以补张
//						}
//					}
				}
			}
			Map<Integer, Integer> handMap = DehMjHelper.toMajiangValMap(handPais);
			if (handMap.containsValue(4)) {
				for (Map.Entry<Integer, Integer> entry : handMap.entrySet()) {
					if (entry.getValue() == 4) {
						//boolean isTing = isTingPai(entry.getKey());
						actData.addAnGang();// 可以杠
//						if (isTing) {
//							
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
	 *            0点炮1点杠2明杠3暗杠4被碰5被杠6胡7自摸,8打出风牌
	 * @param val
	 */
	public void changeAction(int index, int val) {
		actionArr[index] += val;
		getPlayingTable().changeExtend();
	}
	
	
	public int getBaoTingS() {
		return baoTingS;
	}

	public void setBaoTingS(int baoTingS) {
		this.baoTingS = baoTingS;
		changeExtend();
	}

	public void setAction(int index,int value) {
		actionArr[index] = 0;
		getPlayingTable().changeExtend();
	}
	
	public int getActionNum(int index) {
		return actionArr[index];
	}

	public void changeActionTotal(int index , int val){
		actionTotalArr[index] += val;
		getPlayingTable().changeExtend();
	}

	public List<DehMj> getPeng() {
		return peng;
	}

	public List<DehMj> getaGang() {
		return aGang;
	}

	public List<DehMj> getmGang() {
		return mGang;
	}

	public List<DehMj> getGang() {
		List<DehMj> gang = new ArrayList<>();
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
		if(passMajiangVal==0){
			setPassPengMajVal(0);
		}
	}
	
	public void setPassPengMajVal(int passPengMajVal) {
		if (this.passPengMajVal != passPengMajVal) {
			this.passPengMajVal = passPengMajVal;
			changeTbaleInfo();
		}

	}
	

	/**
	 * 可以碰可以杠的牌 选择了碰 再杠不算分
	 *
	 * @param majiang
	 * @return
	 */
	public boolean isPassGang(DehMj majiang) {
		return passGangValList.contains(majiang.getVal());
	}
	
	public int getFangPengSeat(int val){
		if(cardTypes==null||cardTypes.isEmpty()){
			return 0;
		}
		for (DehMjCardDisType disType : cardTypes) {
			if (disType.getAction() == DehMjDisAction.action_peng) {
				List<Integer> ids = disType.getCardIds();
				DehMj mj = DehMj.getMajang(ids.get(0));
				if(mj.getVal()==val){
					if(disType.getDisSeat()!=seat){
						return disType.getDisSeat();
					}
						return 0;
					
				}
			}
		}
		return 0;
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

	public static final List<Integer> wanfaList = Arrays.asList(GameUtil.game_type_dehmj);



	/**
	 * 是否能小胡
	 * @param action
	 * @return
	 */
	public boolean canHuXiaoHu(int action) {
//		if (huXiaohu.contains(action)) {
//			return false;
//		}
//		if(passXiaoHuList.contains(action)){
//			return false;
//		}
//		if (action == DehMjAction.ZHONGTUSIXI && huXiaohu.contains(DehMjAction.DASIXI)) {
//			return false;
//		} else if (action == DehMjAction.ZHONGTULIULIUSHUN && huXiaohu.contains(DehMjAction.LIULIUSHUN)) {
//			return false;
//		}
		return false;
	}

	/**
	 * 可以碰可以杠的牌 选择了碰 再杠不算分
	 *
	 * @param xiaoHu
	 */
	public void addPassXiaoHu(int xiaoHu) {
		if (!this.passXiaoHuList.contains(xiaoHu)) {
			this.passXiaoHuList.add(xiaoHu);
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
	
	
	

	public int getConGangNum() {
		return conGangNum;
	}

	public void setConGangNum(int conGangNum) {
		this.conGangNum = conGangNum;
	}
	
	public void addConGangNum(int conGangNum) {
		this.conGangNum += conGangNum;
	}

    /**
     * 记录得分详情
     *
     * @param index 0胡牌分，1鸟分，2杠分，3飘分
     * @param point
     */
    public void changePointArr(int index, int point) {
    	 DehMjTable table = getPlayingTable(DehMjTable.class);
        if (pointArr.length > index) {
//        	if(table!=null&&index==2){
//        		point =point;
//        	}
            pointArr[index] += point;
            
        }
    }
    
    
    /**
     * 记录杠操作
     *
     * @param index 杠操作，0杠操作，1放杠人座位
     * @param point
     */
    public void changeGangRemainArr(int action, int seat) {
  
    	gangRemainArr[0] = action;
    	gangRemainArr[1] = seat;
    	changeExtend();
    }
    
    
    public int[] getGangRemainArr() {
    	return	gangRemainArr;
    }
    
    

	public int[] getPointArr() {
		return pointArr;
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
    public boolean isAutoPlay() {
        return autoPlay;
    }
    
    public boolean isAutoPlaySelf() {
        return autoPlaySelf;
    }
    
    public void setAutoPlay(boolean autoPlay, boolean isSelf) {
        DehMjTable table = getPlayingTable(DehMjTable.class);
        if (this.autoPlay != autoPlay || autoPlaySelf != isSelf) {
            ComMsg.ComRes.Builder res = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_tuoguan, seat, autoPlay ? 1 : 0, 0, isSelf ? 1 : 0);
            GeneratedMessage msg = res.build();
            if (table != null) {
                table.broadMsgToAll(msg);
            }

            StringBuilder sb = new StringBuilder("DehMj");
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
    public void setLastCheckTime(long lastCheckTime) {
        this.lastCheckTime = lastCheckTime;
        if (getPlayingTable() != null) {
            getPlayingTable().changeExtend();
        }
    }
    public long getLastCheckTime() {
        return lastCheckTime;
    }
    public long getAutoPlayTime() {
        return autoPlayTime;
    }

    public void setCheckAutoPlay(boolean checkAutoPlay) {
        this.checkAutoPlay = checkAutoPlay;
        this.lastCheckTime = System.currentTimeMillis();
        this.sendAutoTime = 0;
        if (getPlayingTable() != null) {
            getPlayingTable().changeExtend();
        }
    }
    public void setAutoPlayTime(long autoPlayTime) {
        this.autoPlayTime = autoPlayTime;
//		getPlayingTable().changeExtend();
    }

	public static void loadWanfaPlayers(Class<? extends Player> cls){
		for (Integer integer:wanfaList){
			PlayerManager.wanfaPlayerTypesPut(integer,cls, DehMjCommandProcessor.getInstance());
		}
	}
}
