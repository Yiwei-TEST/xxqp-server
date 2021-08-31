package com.sy599.game.qipai.daozmj.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.protobuf.GeneratedMessage;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.common.constant.SharedConstants.player_state;
import com.sy599.game.db.bean.group.GroupUser;
import com.sy599.game.db.dao.group.GroupDao;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.TableMjResMsg.ClosingMjPlayerInfoRes;
import com.sy599.game.msg.serverPacket.TablePhzResMsg.PhzHuCards;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes;
import com.sy599.game.qipai.daozmj.command.DaozMjCommandProcessor;
import com.sy599.game.qipai.daozmj.constant.DaozMjConstants;
import com.sy599.game.qipai.daozmj.rule.DzMj;
import com.sy599.game.qipai.daozmj.tool.DaozMjHelper;
import com.sy599.game.qipai.daozmj.tool.DaozMjQipaiTool;
import com.sy599.game.qipai.daozmj.tool.DaozMjTool;
import com.sy599.game.util.GameUtil;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.util.StringUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

public class DaozMjPlayer extends Player {
    // 座位id
    private int seat;
    // 状态
    private player_state state;// 1进入 2已准备 3正在玩 4已结束
    /**
     * 牌局是否在线 1离线 2在线
     */
    private int isEntryTable;
    private List<DzMj> handPais;
    private List<DzMj> outPais;
    private List<DzMj> peng;
    private List<DzMj> aGang;
	private List<DzMj> chi;
    private List<DzMj> mGang;
    private int winCount;
    private int lostCount;
    
    
    private int wangzState;
    /**
     * 局分
     */
    private int lostPoint;
    /**
     * 大局分
     */
    private int point;
    /**
     * ZZMajiangConstants.ACTION_COUNT_INDEX_
     **/
    private int[] actionArr = new int[6];
    /**
     * ZZMajiangConstants.ACTION_COUNT_INDEX_
     **/
    private int[] actionTotalArr = new int[6];
    private int passMajiangVal;
    private List<Integer> passGangValList;
    /**
     * 出牌对应操作信息
     */
    private List<DaozMjCardDisType> cardTypes;
    //胡牌类型
    private List<Integer> huType;

    private volatile boolean autoPlay = false;//是否进入托管状态
    private volatile boolean autoPlaySelf = false;//托管
    private volatile long lastCheckTime = 0;//最后检查时间
    private volatile long autoPlayTime = 0;//自动操作时间
    private volatile boolean checkAutoPlay = false; //是否是牌桌上的焦点
    private volatile long sendAutoTime = 0;//发送倒计时间

    /**
     * 飘分
     * 未开始抛分时该值为-1 表示还未进行飘分
     * 牌局开始前玩家飘的分数(相当于押注分) 抛出的分数在结算的时候  单方结算的分数=对方抛出的分数+自己抛出的分数+胡的基础分数
     */
    private int piaoPoint = -1;
    
    
    private int waiPiaoPoint = -1;

    /**
     * [类型]=分值
     */
    private int[] pointArr = new int[5];

    public DaozMjPlayer() {
        handPais = new ArrayList<DzMj>();
        outPais = new ArrayList<DzMj>();
        peng = new ArrayList<>();
        chi = new ArrayList<>();
        aGang = new ArrayList<>();
        mGang = new ArrayList<>();
        passGangValList = new ArrayList<>();
        cardTypes = new ArrayList<>();
        huType = new ArrayList<>();
        autoPlaySelf = false;
        autoPlay = false;
        autoPlayTime = 0;
        checkAutoPlay = false;
        lastCheckTime = System.currentTimeMillis();
        piaoPoint = -1;
        waiPiaoPoint = -1;
        wangzState = 0;
    }

    public List<Integer> getHuType() {
        return huType;
    }
	public List<DzMj> getChi() {
		return chi;
	}
    public void setHuType(List<Integer> huType) {
        this.huType = huType;
    }

    public int getSeat() {
        return seat;
    }

    public void setSeat(int seat) {
        this.seat = seat;
    }

    public void dealHandPais(List<DzMj> pais) {
        this.handPais = pais;
        getPlayingTable().changeCards(seat);
    }

    public List<DzMj> getHandMajiang() {
        return handPais;
    }


    public List<Integer> getHandPais() {
        return DaozMjHelper.toMajiangIds(handPais);
    }

    public List<Integer> getOutPais() {
        return DaozMjHelper.toMajiangIds(outPais);
    }

    public List<DzMj> getOutMajing() {
        return outPais;
    }

    public void moMajiang(DzMj majiang) {
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
    public void addOutPais(List<DzMj> cards, int action, int disSeat) {
        handPais.removeAll(cards);
        if (action == DaozMjDisAction.action_chupai) {
            outPais.addAll(cards);
        } else {
            if (action == DaozMjDisAction.action_peng) {
                peng.addAll(cards);
                // changeAction(0, 1);
            }else if (action == DaozMjDisAction.action_chi) {
				chi.addAll(cards);

			} else if (action == DaozMjDisAction.action_minggang) {
                //myExtend.setMjFengshen(FirstmythConstants.firstmyth_index12, 1);
                mGang.addAll(cards);
                if (cards.size() != 1) {
                    //changeAction(1, 1);
                } else {
                    DzMj pengMajiang = cards.get(0);
                    Iterator<DzMj> iterator = peng.iterator();
                    while (iterator.hasNext()) {
                        DzMj majiang = iterator.next();
                        if (majiang.getVal() == pengMajiang.getVal()) {
                            mGang.add(majiang);
                            iterator.remove();
                        }
                    }
                    //changeAction(2, 1);
                }
                changeAction(DaozMjConstants.ACTION_COUNT_INDEX_MINGGANG, 1);
            } else if (action == DaozMjDisAction.action_angang) {
                //myExtend.setMjFengshen(FirstmythConstants.firstmyth_index12, 1);
                aGang.addAll(cards);
                changeAction(DaozMjConstants.ACTION_COUNT_INDEX_ANGANG, 1);
            }
            getPlayingTable().changeExtend();
            addCardType(action, cards, disSeat, 0);
        }
        getPlayingTable().changeCards(seat);
    }

    public void qGangUpdateOutPais(DzMj card) {
        Iterator<DzMj> iterator = mGang.iterator();
        while (iterator.hasNext()) {
            DzMj majiang = iterator.next();
            if (majiang.getVal() == card.getVal()) {
                peng.add(majiang);
                iterator.remove();
            }
        }
        for (DaozMjCardDisType disType : cardTypes) {
            if (disType.getAction() == DaozMjDisAction.action_minggang) {
                if (disType.isHasCardVal(card)) {
                    disType.setAction(DaozMjDisAction.action_peng);
                    disType.getCardIds().remove((Integer) card.getId());
                    //disType.setDisSeat(seat);
                    break;
                }
            }
        }
    }

    /**
     * 添加牌型，例如将碰变成明杠等
     *
     * @param action
     * @param disCardList
     */
    public void addCardType(int action, List<DzMj> disCardList, int disSeat, int disStatus) {
        if (action != 0) {
            if (action == DaozMjDisAction.action_minggang && disCardList.size() == 1) {
                DzMj majiang = disCardList.get(0);
                for (DaozMjCardDisType disType : cardTypes) {
                    if (disType.getAction() == DaozMjDisAction.action_peng) {
                        if (disType.isHasCardVal(majiang)) {
                            disType.setAction(DaozMjDisAction.action_minggang);
                            disType.addCardId(majiang.getId());
                            disType.setDisSeat(seat);
                            break;
                        }
                    }
                }
            } else {
                DaozMjCardDisType type = new DaozMjCardDisType();
                type.setAction(action);
                type.setCardIds(DaozMjQipaiTool.toMajiangIds(disCardList));
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

    public void removeOutPais(List<DzMj> cards, int action) {
        boolean remove = outPais.removeAll(cards);
        if (remove) {
//			if (action == DaozMjDisAction.action_peng) {
//				changeAction(4, 1);
//			} else if (action == DaozMjDisAction.action_minggang) {
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
        sb.append(StringUtil.implode(huType, ",")).append("|");
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
            huType = StringUtil.explodeToIntList(val7);
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
        sb.append(autoPlay ? 1 : 0).append(",");
        sb.append(autoPlaySelf ? 1 : 0).append(",");
        sb.append(autoPlayTime).append(",");
        sb.append(lastCheckTime).append(",");
        sb.append(piaoPoint).append(",");
        sb.append(wangzState).append(",");
        sb.append(waiPiaoPoint).append(",");
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
            this.autoPlay = StringUtil.getIntValue(values, i++) == 1;
            this.autoPlaySelf = StringUtil.getIntValue(values, i++) == 1;
            this.autoPlayTime = StringUtil.getLongValue(values, i++);
            this.lastCheckTime = StringUtil.getLongValue(values, i++);
            this.piaoPoint = StringUtil.getIntValue(values, i++);
            this. wangzState = StringUtil.getIntValue(values, i++);
            
            
           this.waiPiaoPoint  = StringUtil.getIntValue(values, i++);
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
            list.addAll(DaozMjHelper.toMajiangIds(peng));
        }
        if (!mGang.isEmpty()) {
            list.addAll(DaozMjHelper.toMajiangIds(mGang));
        }
        if (!aGang.isEmpty()) {
            list.addAll(DaozMjHelper.toMajiangIds(aGang));
        }
        if (!chi.isEmpty()) {
			list.addAll(DaozMjHelper.toMajiangIds(chi));
		}
        return list;
    }

    public List<PhzHuCards> buildDisCards(long lookUid) {
        return buildDisCards(lookUid, true);
    }

    public List<PhzHuCards> buildDisCards(long lookUid, boolean hide) {
        List<PhzHuCards> list = new ArrayList<>();
        for (DaozMjCardDisType type : cardTypes) {
            if (hide && lookUid != this.userId && type.getAction() == DaozMjDisAction.action_angang) {
                // 不是本人并且是栽
                list.add(type.buildMsg(true).build());
            } else {
                list.add(type.buildMsg().build());
            }
        }
        return list;
    }

    /**
     * @param isrecover 是否重连
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
        res.addAllAngangIds(DaozMjHelper.toMajiangIds(aGang));
        res.addAllMoldCards(buildDisCards(userId));
        List<DzMj> gangList = getGang();
        // 是否杠过
        res.addExt(gangList.isEmpty() ? 0 : 1); //0
        DaozMjTable table = getPlayingTable(DaozMjTable.class);
        // 现在是否自己摸的牌
        res.addExt((isAlreadyMoMajiang()) ? 1 : 0); //1
        res.addExt(handPais != null ? handPais.size() : 0); //2
        res.addExt(waiPiaoPoint);//3
        res.addExt(autoPlay ? 1 : 0);//4
        res.addExt(autoPlaySelf ? 1 : 0);//5
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

    public List<DzMj> getGang() {
        List<DzMj> gang = new ArrayList<>();
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
        mGang.clear();
        cardTypes.clear();
        huType.clear();
        chi.clear();
        setPassMajiangVal(0);
        clearPassGangVal();
        setWinCount(0);
        setLostCount(0);
        setPoint(0);
        setLostPoint(0);
        setTotalPoint(0);
        setSeat(0);
        if (!isCompetition) {
            setPlayingTableId(0);

        }
        autoPlaySelf = false;
        autoPlay = false;
        lastCheckTime = System.currentTimeMillis();
        checkAutoPlay = false;
        setPiaoPoint(-1);
        setWaiPiaoPoint(-1);
        saveBaseInfo();
        pointArr = new int[5];
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
        res.addAllPointArr(Arrays.asList(ArrayUtils.toObject(pointArr)));
        if (!StringUtils.isBlank(getHeadimgurl())) {
            res.setIcon(getHeadimgurl());
        } else {
            res.setIcon("");
        }
        res.setSex(sex);
        res.addAllHandPais(getHandPais());
        List<PhzHuCards> list = new ArrayList<>();
        for (DaozMjCardDisType type : cardTypes) {
            list.add(type.buildMsg().build());
        }
        res.addAllMoldPais(list);
        res.addAllDahus(huType);

        res.addExt(waiPiaoPoint);
        return res;
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
        res.addAllPointArr(Arrays.asList(ArrayUtils.toObject(pointArr)));
        if (!StringUtils.isBlank(getHeadimgurl())) {
            res.setIcon(getHeadimgurl());
        } else {
            res.setIcon("");
        }
        res.setSex(sex);
        res.addAllHandPais(getHandPais());
        List<PhzHuCards> list = new ArrayList<>();
        for (DaozMjCardDisType type : cardTypes) {
            list.add(type.buildMsg().build());
        }
        res.addAllMoldPais(list);
        res.addAllDahus(huType);

        res.addExt(waiPiaoPoint);
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
        setPassMajiangVal(0);
        setLostPoint(0);
        clearPassGangVal();
        peng.clear();
        aGang.clear();
        mGang.clear();
        chi.clear();
        cardTypes.clear();
        actionArr = new int[6];
        huType.clear();
        getPlayingTable().changeExtend();
        getPlayingTable().changeCards(seat);
        changeState(player_state.entry);
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
        DaozMjTable table = getPlayingTable(DaozMjTable.class);
        if(table.getWaiPiao()>0){
        	  setWaiPiaoPoint(-1);
        }
        setWangzState(0);
      
        pointArr = new int[5];
    }

    @Override
    public void initPais(String handPai, String outPai) {
        if (!StringUtils.isBlank(handPai)) {
            List<Integer> list = StringUtil.explodeToIntList(handPai);
            this.handPais = DaozMjHelper.toMajiang(list);
        }
        if (!StringUtils.isBlank(outPai)) {
            String[] values = outPai.split(";");
            int i = -1;
            for (String value : values) {
                i++;
                if (i == 0) {
                    List<Integer> list = StringUtil.explodeToIntList(value);
                    this.outPais = DaozMjHelper.toMajiang(list);
                } else {
                    DaozMjCardDisType type = new DaozMjCardDisType();
                    type.init(value);
                    cardTypes.add(type);
                    List<DzMj> majiangs = DaozMjHelper.toMajiang(type.getCardIds());
                    if (type.getAction() == DaozMjDisAction.action_angang) {
                        aGang.addAll(majiangs);
                    } else if (type.getAction() == DaozMjDisAction.action_minggang) {
                        mGang.addAll(majiangs);
                    } else if (type.getAction() == DaozMjDisAction.action_peng) {
                        peng.addAll(majiangs);
                    }else if (type.getAction() == DaozMjDisAction.action_chi) {
                        chi.addAll(majiangs);
                    }
                    
                    
                    
                    
                    
                }
            }
        }
    }
    
    
    

    public List<DaozMjCardDisType> getCardTypesByAction(int action) {
    	List<DaozMjCardDisType> types = new ArrayList<>();
    	  for (DaozMjCardDisType type : cardTypes) {
    		  if(type.getAction()==action){
    			  types.add(type);
    		  }
          }
    	
		return types;
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
        for (DaozMjCardDisType huxi : cardTypes) {
            sb.append(huxi.toStr()).append(";");
        }
        return sb.toString();
    }
	public boolean isChiPengGang(){
		if(chi.size() >0 || mGang.size() >0 || peng.size()>0 || aGang.size() >0) {
			return true;
		}
		
		return false;
	}
	
	public boolean isChiPengGangNoAgang(){
		if(chi.size() >0 || mGang.size() >0 || peng.size()>0 ) {
			return true;
		}
		
		return false;
	}

    /**
     * 别人出牌可做的操作
     *
     * @param majiang
     * @param canCheckHu 是否能点炮 或 抢杠胡
     * @return 0胡 1碰 2明刚 3暗杠(暗杠后来不需要了 暗杠也用3标记)
     */
    public List<Integer> checkDisMajiang(DzMj majiang, boolean canCheckHu) {
        List<Integer> list = new ArrayList<>();
        int[] arr = new int[6];
        // 转转麻将别人出牌可以胡
        DaozMjTable table = getPlayingTable(DaozMjTable.class);
        if (canCheckHu && passMajiangVal != majiang.getVal()) {
        	
        	List<DzMj>  hangWangs = DaozMjTool.getSameMajiang(handPais, majiang, 1);
        	boolean canHu = checkWangChiHu(majiang, table, hangWangs);
            // 没有出现漏炮的情况
            List<DzMj> copy = new ArrayList<>(handPais);
            copy.add(majiang);
            boolean hu =canHu;
            if(hu){
            	hu =	DaozMjTool.disIsHu(copy, table,majiang.getVal()==table.getWangMjVal());
            }
            if (hu) {
            	if(getWangzState()!=1)
                arr[DaozMjConstants.ACTION_INDEX_HU] = 1;
            }else{
           	 //十三浪
                if(!isChiPengGang()&&DaozMjTool.shiSanLang(copy)>0){ 
                	 arr[DaozMjConstants.ACTION_INDEX_HU] = 1;
                }
            }
        }
        int count = DaozMjHelper.getMajiangCount(handPais, majiang.getVal());
        if (count >= 2&&majiang.getVal()!=table.getWangMjVal()) {
            // 除了红中外必须要3张以上的牌
//            if (getExceptHzMajiangCount() >= 3) {
                arr[DaozMjConstants.ACTION_INDEX_PENG] = 1;// 可以碰
//            }
        }
        if (count == 3&&majiang.getVal()!=table.getWangMjVal()) {
            arr[DaozMjConstants.ACTION_INDEX_MINGGANG] = 1;// 可以杠
        }
        
        
        if (table.calcNextSeat(table.getDisCardSeat()) == seat&&table.getKeChi()==1) {
        	  List<DzMj> chiMj = DaozMjTool.checkChi(handPais, majiang,table.getWangMjVal());
        	  if (!chiMj.isEmpty()&&majiang.getVal()!=table.getWangMjVal()) {
        		  arr[DaozMjConstants.ACTION_INDEX_CHI] = 1;
        	  }
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

	public boolean checkWangChiHu(DzMj majiang, DaozMjTable table, List<DzMj> hangWangs) {
		boolean canHu = true;
		if(majiang.getVal()==table.getWangMjVal()&&!hangWangs.isEmpty()){
			List<DzMj> hcopy = new ArrayList<>(handPais);
			 List<DzMj> chiList =DaozMjTool.addChiList(hcopy, majiang.getVal());
			 if(!chiList.isEmpty()){
				 hcopy.remove(hangWangs.get(0));
				 for(DzMj mj:chiList){
					 hcopy.remove(mj);
					 boolean hu = DaozMjTool.disIsHu(hcopy, table,false);
					 if(hu){
						 canHu = false;
						 break;
					 }else{
						 hcopy.add(mj);
					 }
				 }
			 }
		}
		return canHu;
	}

    /**
     * 除了红中之外还有多少张牌
     *
     * @return
     */
//    public int getExceptHzMajiangCount() {
//        int count = 0;
//        for (DzMj majiang : handPais) {
//            if (!majiang.isHongzhong()) {
//                count++;
//            }
//        }
//        return count;
//    }

    public DzMj getLastMoMajiang() {
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
    public List<Integer> checkMo(DzMj majiang) {
        List<Integer> list = new ArrayList<>();
        int[] arr = new int[6];
        DaozMjTable table = getPlayingTable(DaozMjTable.class);
        int hongZhongCount = DaozMjQipaiTool.getMajiangCount(getHandMajiang(), DzMj.getHongZhongVal());
        
//        if(hongZhongCount >=4 &&table.isSiBaHZ()) {
//        	  arr[DaozMjConstants.ACTION_INDEX_ZIMO] = 1;
//        }
        	 if (DaozMjTool.isHu(handPais, table)) {
                 arr[DaozMjConstants.ACTION_INDEX_ZIMO] = 1;
             }else{
            	 //十三浪
                 if(!isChiPengGang()&&DaozMjTool.shiSanLang(getHandMajiang())>0){
                 	 arr[DaozMjConstants.ACTION_INDEX_ZIMO] = 1;
                 }
             }
		
       
        if (isAlreadyMoMajiang()) {
            // if (majiang != null) {
            Map<Integer, Integer> pengMap = DaozMjHelper.toMajiangValMap(peng);
            for (DzMj handMajiang : handPais) {
                if (pengMap.containsKey(handMajiang.getVal())) {
                    // 有碰过
                    arr[DaozMjConstants.ACTION_INDEX_MINGGANG] = 1;// 可以杠
                    break;
                }
            }
            List<DzMj> copy = new ArrayList<>(handPais);
           // DaozMjTool.dropWangBa(copy,table.getWangMjVal());//道州麻将需要做下判断
            Map<Integer, Integer> handMap = DaozMjHelper.toMajiangValMap(copy);
            if (handMap.containsValue(4)) {
                arr[DaozMjConstants.ACTION_INDEX_ANGANG] = 1;// 可以暗杠
            }
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
     * @param val
     */
    public void changeAction(int index, int val) {
        actionArr[index] += val;
        actionTotalArr[index] += val;
        getPlayingTable().changeExtend();
    }

    public List<DzMj> getPeng() {
        return peng;
    }

    public List<DzMj> getaGang() {
        return aGang;
    }

    public List<DzMj> getmGang() {
        return mGang;
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
    public boolean isPassGang(DzMj majiang) {
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
            GameUtil.game_type_daozmj);

    public static void loadWanfaPlayers(Class<? extends Player> cls) {
        for (Integer integer : wanfaList) {
            PlayerManager.wanfaPlayerTypesPut(integer, cls, DaozMjCommandProcessor.getInstance());
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
        DaozMjTable table = getPlayingTable(DaozMjTable.class);
        long now = System.currentTimeMillis();
        boolean auto = isAutoPlay();
        if (!auto && table.getIsAutoPlay() > 0) {
            //检查玩家是否进入系统托管状态
        	 if (!checkAutoPlay && table.getTableStatus()!= DaozMjConstants.TABLE_STATUS_PIAO) {
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
                    if (timeOut >= DaozMjConstants.AUTO_READY_TIME) {
                        setAutoPlayTime(0);
                        return true;
                    }
                } else if (autoType == 2) {
                    if (timeOut >= DaozMjConstants.AUTO_HU_TIME) {
                        setAutoPlayTime(0);
                        return true;
                    }
                } else {
                    if (timeOut >= DaozMjConstants.AUTO_PLAY_TIME) {
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
        DaozMjTable table = getPlayingTable(DaozMjTable.class);
        if (this.autoPlay != autoPlay || autoPlaySelf != isSelf) {
            ComMsg.ComRes.Builder res = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_tuoguan, seat, autoPlay ? 1 : 0, 0, isSelf ? 1 : 0);
            GeneratedMessage msg = res.build();
            if (table != null) {
                table.broadMsgToAll(msg);
            }
            if(!getHandPais().isEmpty()){
            	table.addPlayLog(table.getDisCardRound() + "_" +getSeat() + "_" + DaozMjDisAction.action_tuoguan + "_" +(autoPlay?1:0)+ getExtraPlayLog());
            }

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

    public void setPiaoPoint(int piaoPoint) {
        this.piaoPoint = piaoPoint;
        changeTableInfo();
    }

    public int getPiaoPoint() {
        return piaoPoint;
    }
    
    

    public int getWaiPiaoPoint() {
		return waiPiaoPoint;
	}

	public void setWaiPiaoPoint(int waiPiaoPoint) {
		this.waiPiaoPoint = waiPiaoPoint;
		 changeTableInfo();
	}

	public int getWangzState() {
		return wangzState;
	}

	public void setWangzState(int wangzState) {
		this.wangzState = wangzState;
	}

	/**
     * 回放日志的额外信息
     *
     * @return
     */
    public String getExtraPlayLog() {
        return "_" + (isAutoPlay() ? 1 : 0) + "," + (isAutoPlaySelf() ? 1 : 0);
    }

    /**
     * 记录得分详情
     *
     * @param index 0胡牌分，1鸟分，2杠分，3飘分
     * @param point
     */
    public void changePointArr(int index, int point) {
    	 DaozMjTable table = getPlayingTable(DaozMjTable.class);
        if (pointArr.length > index) {
        	if(table!=null&&index==2){
        		point =point/table.getDiFen();
        	}
            pointArr[index] += point;
        }
    }

    public int[] getPointArr() {
        return pointArr;
    }
}
