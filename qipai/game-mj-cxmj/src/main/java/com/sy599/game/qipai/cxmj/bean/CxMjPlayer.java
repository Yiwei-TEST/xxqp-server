package com.sy599.game.qipai.cxmj.bean;

import java.util.*;

import com.sy599.game.qipai.cxmj.tool.CxMjTool;
import com.sy599.game.qipai.cxmj.tool.ting.TingTool;
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
import com.sy599.game.qipai.cxmj.command.CxMjCommandProcessor;
import com.sy599.game.qipai.cxmj.constant.CxMjConstants;
import com.sy599.game.qipai.cxmj.constant.CxMj;
import com.sy599.game.qipai.cxmj.tool.CxMjHelper;
import com.sy599.game.qipai.cxmj.tool.CxMjQipaiTool;
import com.sy599.game.util.GameUtil;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.util.StringUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

public class CxMjPlayer extends Player {
    // 座位id
    private int seat;
    // 状态
    private player_state state;// 1进入 2已准备 3正在玩 4已结束
    /**
     * 牌局是否在线 1离线 2在线
     */
    private int isEntryTable;
    private List<CxMj> handPais;
    private List<CxMj> outPais;
    private List<CxMj> peng;
    private List<CxMj> aGang;
    private List<CxMj> mGang;
    private int winCount;
    private int lostCount;
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
    private int[] actionArr = new int[5];
    /**
     * ZZMajiangConstants.ACTION_COUNT_INDEX_
     **/
    private int[] actionTotalArr = new int[5];
    private int passMajiangVal;
    //过杠操作
    private List<Integer> passGangValList;
    /**
     * 出牌对应操作信息
     */
    private List<CxMjCardDisType> cardTypes;
    //胡牌类型
    private List<Integer> huType;

    private volatile boolean autoPlay = false;//托管
    private volatile boolean autoPlaySelf = false;//托管
    private volatile long lastCheckTime = 0;//最后检查时间
    private volatile long autoPlayTime = 0;//自动操作时间
    private volatile boolean checkAutoPlay = false; //是否是牌桌上的焦点
    private volatile long sendAutoTime = 0;//发送倒计时间
    private int ziMoNum=0;//自摸次数
    private int jiePaoNum=0;//接炮次数
    private int dianPaoNum=0;//点炮次数

    private int piaoFen=-1;
    //输赢飘分
    private int winLostPiaoFen=0;
    //虚拟杠牌
    private List<Integer> virtualGang=new ArrayList<>();
    //虚拟胡牌
    private int virtualHu=0;
    //杠牌次数，最多连杠两次
    private int gangNum=0;
    //杠补牌Id
    private int buId=0;

    public CxMjPlayer() {
        handPais = new ArrayList<CxMj>();
        outPais = new ArrayList<CxMj>();
        peng = new ArrayList<>();
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
        sb.append(piaoFen).append(",");
        sb.append(StringUtil.implode(virtualGang, "_")).append(",");
        sb.append(virtualHu).append(",");
        sb.append(ziMoNum).append(",");
        sb.append(jiePaoNum).append(",");
        sb.append(dianPaoNum).append(",");
        sb.append(gangNum).append(",");
        sb.append(buId).append(",");
        return sb.toString();
    }

    public List<Integer> getHuType() {
        return huType;
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

    public int getWinLostPiaoFen() {
        return winLostPiaoFen;
    }

    public void setWinLostPiaoFen(int winLostPiaoFen) {
        this.winLostPiaoFen = winLostPiaoFen;
    }

    public void dealHandPais(List<CxMj> pais) {
        this.handPais = pais;
        getPlayingTable().changeCards(seat);
    }

    public List<CxMj> getHandMajiang() {
        return handPais;
    }

    public boolean haveHongzhong() {
        for (CxMj majiang : handPais) {
            if (majiang.isHongzhong()) {
                return true;
            }
        }
        return false;
    }

    public List<Integer> getHandPais() {
        return CxMjHelper.toMajiangIds(handPais);
    }

    public List<Integer> getOutPais() {
        return CxMjHelper.toMajiangIds(outPais);
    }

    public List<CxMj> getOutMajing() {
        return outPais;
    }

    public void moMajiang(CxMj majiang) {
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
    public void addOutPais(List<CxMj> cards, int action, int disSeat) {
        handPais.removeAll(cards);
        if (action == CxMjDisAction.action_chupai) {
            outPais.addAll(cards);
        } else {
            if (action == CxMjDisAction.action_peng) {
                peng.addAll(cards);
            } else if (action == CxMjDisAction.action_minggang) {
                mGang.addAll(cards);
                if (cards.size() != 1) {
                } else {
                    CxMj pengMajiang = cards.get(0);
                    Iterator<CxMj> iterator = peng.iterator();
                    while (iterator.hasNext()) {
                        CxMj majiang = iterator.next();
                        if (majiang.getVal() == pengMajiang.getVal()) {
                            mGang.add(majiang);
                            iterator.remove();
                        }
                    }
                }
                changeAction(CxMjConstants.ACTION_COUNT_INDEX_MINGGANG, 1);
            } else if (action == CxMjDisAction.action_angang) {
                aGang.addAll(cards);
                changeAction(CxMjConstants.ACTION_COUNT_INDEX_ANGANG, 1);
            }
            getPlayingTable().changeExtend();
            addCardType(action, cards, disSeat, 0);
        }
        getPlayingTable().changeCards(seat);
    }

    public void qGangUpdateOutPais(CxMj card) {
        Iterator<CxMj> iterator = mGang.iterator();
        while (iterator.hasNext()) {
            CxMj majiang = iterator.next();
            if (majiang.getVal() == card.getVal()) {
                peng.add(majiang);
                iterator.remove();
            }
        }
        for (CxMjCardDisType disType : cardTypes) {
            if (disType.getAction() == CxMjDisAction.action_minggang) {
                if (disType.isHasCardVal(card)) {
                    disType.setAction(CxMjDisAction.action_peng);
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
    public void addCardType(int action, List<CxMj> disCardList, int disSeat, int disStatus) {
        if (action != 0) {
            if (action == CxMjDisAction.action_minggang && disCardList.size() == 1) {
                CxMj majiang = disCardList.get(0);
                for (CxMjCardDisType disType : cardTypes) {
                    if (disType.getAction() == CxMjDisAction.action_peng) {
                        if (disType.isHasCardVal(majiang)) {
                            disType.setAction(CxMjDisAction.action_minggang);
                            disType.addCardId(majiang.getId());
                            disType.setDisSeat(seat);
                            break;
                        }
                    }
                }
            } else {
                CxMjCardDisType type = new CxMjCardDisType();
                type.setAction(action);
                type.setCardIds(CxMjQipaiTool.toMajiangIds(disCardList));
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

    public void removeOutPais(List<CxMj> cards, int action) {
        boolean remove = outPais.removeAll(cards);
        if (remove) {
//			if (action == CxMjDisAction.action_peng) {
//				changeAction(4, 1);
//			} else if (action == CxMjDisAction.action_minggang) {
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
            this.piaoFen = StringUtil.getIntValue(values, i++);
            String virtualGangStr = StringUtil.getValue(values, i++);
            if (!StringUtils.isBlank(virtualGangStr)) {
                this.virtualGang = StringUtil.explodeToIntList(virtualGangStr, "_");
            }
            this.virtualHu = StringUtil.getIntValue(values, i++);
            this.ziMoNum = StringUtil.getIntValue(values, i++);
            this.jiePaoNum = StringUtil.getIntValue(values, i++);
            this.dianPaoNum = StringUtil.getIntValue(values, i++);
            this.gangNum = StringUtil.getIntValue(values, i++);
            this.buId = StringUtil.getIntValue(values, i++);
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
            list.addAll(CxMjHelper.toMajiangIds(peng));
        }
        if (!mGang.isEmpty()) {
            list.addAll(CxMjHelper.toMajiangIds(mGang));
        }
        if (!aGang.isEmpty()) {
            list.addAll(CxMjHelper.toMajiangIds(aGang));
        }
        return list;
    }



    public List<PhzHuCards> buildDisCards(long lookUid) {
        return buildDisCards(lookUid, true);
    }

    public List<PhzHuCards> buildDisCards(long lookUid, boolean hide) {
        List<PhzHuCards> list = new ArrayList<>();
        for (CxMjCardDisType type : cardTypes) {
            if (hide && lookUid != this.userId && type.getAction() == CxMjDisAction.action_angang) {
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
        res.addAllAngangIds(CxMjHelper.toMajiangIds(aGang));
        res.addAllMoldCards(buildDisCards(userId));
        List<CxMj> gangList = getGang();
        // 是否杠过
        CxMjTable table = getPlayingTable(CxMjTable.class);
        // 现在是否自己摸的牌
        res.addExt(gangList.isEmpty() ? 0 : 1);    //0
        res.addExt((isAlreadyMoMajiang()) ? 1 : 0);//1
        res.addExt(handPais != null ? handPais.size() : 0);//2
        res.addExt(autoPlay ? 1 : 0);              //3
        res.addExt(autoPlaySelf ? 1 : 0);
        res.addExt(piaoFen);
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

    public List<CxMj> getGang() {
        List<CxMj> gang = new ArrayList<>();
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

    public void addGangNum(){
        gangNum++;
        changeTbaleInfo();
    }

    public int getGangNum() {
        return gangNum;
    }

    public int getPoint() {
        return point;
    }

    public void setPoint(int point) {
        this.point = point;
    }

    public void setLostPoint(int lostPoint) {
        this.lostPoint = lostPoint;
        changeTbaleInfo();
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
        actionArr = new int[5];
        actionTotalArr = new int[5];
        peng.clear();
        aGang.clear();
        mGang.clear();
        cardTypes.clear();
        huType.clear();
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
        ziMoNum=0;//胡牌次数
        jiePaoNum=0;
        dianPaoNum=0;//点炮次数
        saveBaseInfo();
        setPiaoFen(-1);
        setWinLostPiaoFen(0);
        virtualHu=0;
        virtualGang.clear();
        gangNum=0;
        buId=0;
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
        for (CxMjCardDisType type : cardTypes) {
            list.add(type.buildMsg().build());
        }
        res.addAllMoldPais(list);
        res.addAllDahus(huType);
        res.addExt(piaoFen);
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
        if (!StringUtils.isBlank(getHeadimgurl())) {
            res.setIcon(getHeadimgurl());
        } else {
            res.setIcon("");
        }
        res.setSex(sex);
        res.addAllHandPais(getHandPais());
        List<PhzHuCards> list = new ArrayList<>();
        for (CxMjCardDisType type : cardTypes) {
            list.add(type.buildMsg().build());
        }
        res.addAllMoldPais(list);
        res.addAllDahus(huType);
        res.setActionCount(0,ziMoNum);
        res.setActionCount(1,jiePaoNum);
        res.setActionCount(2,dianPaoNum);
        res.addExt(piaoFen);
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
        cardTypes.clear();
        actionArr = new int[5];
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
        CxMjTable t=(CxMjTable)table;
        if(t.getPiaoFenType()!=5)
            setPiaoFen(-1);
        setWinLostPiaoFen(0);
        virtualHu=0;
        virtualGang.clear();
        gangNum=0;
        buId=0;
    }

    @Override
    public void initPais(String handPai, String outPai) {
        if (!StringUtils.isBlank(handPai)) {
            List<Integer> list = StringUtil.explodeToIntList(handPai);
            this.handPais = CxMjHelper.toMajiang(list);
        }
        if (!StringUtils.isBlank(outPai)) {
            String[] values = outPai.split(";");
            int i = -1;
            for (String value : values) {
                i++;
                if (i == 0) {
                    List<Integer> list = StringUtil.explodeToIntList(value);
                    this.outPais = CxMjHelper.toMajiang(list);
                } else {
                    CxMjCardDisType type = new CxMjCardDisType();
                    type.init(value);
                    cardTypes.add(type);
                    List<CxMj> majiangs = CxMjHelper.toMajiang(type.getCardIds());
                    if (type.getAction() == CxMjDisAction.action_angang) {
                        aGang.addAll(majiangs);
                    } else if (type.getAction() == CxMjDisAction.action_minggang) {
                        mGang.addAll(majiangs);
                    } else if (type.getAction() == CxMjDisAction.action_peng) {
                        peng.addAll(majiangs);
                    }
                }
            }
        }
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
        for (CxMjCardDisType huxi : cardTypes) {
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
    public List<Integer> checkDisMajiang(CxMj majiang, boolean canCheckHu) {
        List<Integer> list = new ArrayList<>();
        int[] arr = new int[6];
        // 转转麻将别人出牌可以胡
        List<CxMj> copy = new ArrayList<>(handPais);
        copy.add(majiang);
        if (canCheckHu && passMajiangVal != majiang.getVal()) {
            boolean hu = TingTool.isHu(CxMjHelper.toMajiangIds(copy));
            if (hu) {
                arr[CxMjConstants.ACTION_INDEX_HU] = 1;
            }
        }
        int count = CxMjHelper.getMajiangCount(handPais, majiang.getVal());
        if (count >= 2) {
            arr[CxMjConstants.ACTION_INDEX_PENG] = 1;// 可以碰
        }
        if (count == 3&&!passGangValList.contains(majiang.getVal())) {
            List<Integer> l = CxMjHelper.dropVal(copy, majiang.getVal());
            l.add(1004);
            Map<Integer,List<Integer>> gangM1 = CxMjTool.checkGang(1, l, CxMjHelper.toMajiangValMap(peng),1004);
            l.remove(l.size()-1);
            l.add(1005);
            Map<Integer,List<Integer>> gangM2 = CxMjTool.checkGang(1, l, CxMjHelper.toMajiangValMap(peng),1005);
            if(gangM1.size()>0||gangM2.size()>0){
                virtualGang.clear();
                virtualGang.add(majiang.getId());
                arr[CxMjConstants.ACTION_INDEX_MINGGANG] = 1;// 可以杠
                sendGangMsg();
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


    public List<Integer> checkGangHu(List<Integer> ids) {
        List<Integer> l=new ArrayList<>();
        ids.add(1004);
        if(TingTool.isHu(ids)){
            virtualHu=1004;
            l.add(1004);
        }
        ids.remove(ids.size()-1);
        ids.add(1005);
        if(TingTool.isHu(ids)){
            virtualHu=1005;
            l.add(1005);
        }
        return l;
    }

    /**
     * 除了红中之外还有多少张牌
     *
     * @return
     */
    public int getExceptHzMajiangCount() {
        int count = 0;
        for (CxMj majiang : handPais) {
            if (!majiang.isHongzhong()) {
                count++;
            }
        }
        return count;
    }

    public CxMj getLastMoMajiang() {
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
    public List<Integer> checkMo(CxMj majiang) {
        List<Integer> list = new ArrayList<>();
        int[] arr = new int[6];
        if (TingTool.isHu(CxMjHelper.toMajiangIds(handPais))) {
            arr[CxMjConstants.ACTION_INDEX_HU] = 1;
        }
        if (isAlreadyMoMajiang()) {
            int buId=0;
            if(gangNum==1){
                buId=handPais.get(handPais.size()-1).getId();
            }
            Map<Integer,List<Integer>> gangM = CxMjTool.checkGang(gangNum, CxMjHelper.toMajiangIds(handPais), CxMjHelper.toMajiangValMap(peng),buId);
            if(gangM.size()>0)
                virtualGang.clear();
            for(Map.Entry<Integer,List<Integer>> entry:gangM.entrySet()){
                if(entry.getKey()!=0){
                    for (Integer id:entry.getValue()){
                        if(!virtualGang.contains(id))
                            virtualGang.add(id);
                        //明杠和暗杠都发明杠消息
                        arr[CxMjConstants.ACTION_INDEX_MINGGANG] = 1;// 可以杠
                    }
                }
            }
            sendGangMsg();
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
     * 获取可以杠的数组
     */
    public void sendGangMsg(){
        if(virtualGang.isEmpty())
            return;
        StringBuilder sb=new StringBuilder();
        for (Integer id:virtualGang) {
            if(sb.length()>0)
                sb.append(";");
            List<CxMj> mjs = CxMjHelper.getGangMajiangList(handPais, CxMj.getMajang(id).getVal());
            if(!mjs.isEmpty())
                sb.append(StringUtil.implode(CxMjHelper.toMajiangIds(mjs), ","));
            if(!CxMjHelper.toMajiangIds(handPais).contains(id)){
                if(sb.length()>0)
                    sb.append(",");
                sb.append(id);
            }
        }
        if(sb.length()>0)
            writeComMessage(WebSocketMsgType.res_code_cxmj_gangMsg, sb.toString());
    }

    private boolean checkGangHu(List<Integer> handCards,List<Integer> gangList){
        List<Integer> copy=new ArrayList<>(handCards);
        copy.removeAll(gangList);
        copy.add(1004);
        if(TingTool.isHu(copy)){
            return true;
        }else {
            copy.remove(copy.size()-1);
            copy.add(1005);
            if(TingTool.isHu(copy)){
                return true;
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
    public void changeAction(int index, int val) {
        actionArr[index] += val;
        actionTotalArr[index] += val;
        getPlayingTable().changeExtend();
    }

    public List<CxMj> getPeng() {
        return peng;
    }

    public List<CxMj> getaGang() {
        return aGang;
    }

    public List<CxMj> getmGang() {
        return mGang;
    }

    public void setPassGangValList(List<Integer> passGangValList) {
        this.passGangValList = passGangValList;
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
    public boolean isPassGang(CxMj majiang) {
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

    public static final List<Integer> wanfaList = Arrays.asList(GameUtil.game_type_cxmj);

    public static void loadWanfaPlayers(Class<? extends Player> cls) {
        for (Integer integer : wanfaList) {
            PlayerManager.wanfaPlayerTypesPut(integer, cls, CxMjCommandProcessor.getInstance());
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
        CxMjTable table = getPlayingTable(CxMjTable.class);
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
            //CxMjConstants.AUTO_TIMEOUT + CxMjConstants.AUTO_CHECK_TIMEOUT
            if (timeOut >=table.getIsAutoPlay()) {
                //进入托管状态
                auto = true;
                setAutoPlay(true, false);
            } else if (timeOut >= CxMjConstants.AUTO_CHECK_TIMEOUT) {
                if (sendAutoTime == 0) {
                    sendAutoTime = now;
                    setLastCheckTime(now - CxMjConstants.AUTO_CHECK_TIMEOUT * 1000);
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
                    if (timeOut >= CxMjConstants.AUTO_HU_TIME) {
                        setAutoPlayTime(0);
                        return true;
                    }
                } else {
                    if (timeOut >= CxMjConstants.AUTO_PLAY_TIME) {
                        setAutoPlayTime(0);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 检查托管(飘分)
     * @return
     */
    public boolean checkAutoPiaoFen() {
        CxMjTable table = getPlayingTable(CxMjTable.class);
        long now = System.currentTimeMillis();
        boolean auto = isAutoPlay();
        if (!auto && table.getIsAutoPlay() >= 1) {
            //检查玩家是否进入系统托管状态
            if (!checkAutoPlay) {
                setCheckAutoPlay(true);
            }

            int timeOut = (int) (now - getLastCheckTime()) / 1000;
            if (timeOut >=table.getIsAutoPlay()) {
                //进入托管状态
                auto = true;
                setAutoPlay(true, false);
            } else if (timeOut >= CxMjConstants.AUTO_CHECK_TIMEOUT) {
                if (sendAutoTime == 0) {
                    sendAutoTime = now;
                    setLastCheckTime(now - CxMjConstants.AUTO_CHECK_TIMEOUT * 1000);
                    timeOut = (int) (now - getLastCheckTime()) / 1000;
                }
                int timeSecond = table.getIsAutoPlay() - timeOut;
                if ((timeOut % 5 == 0 && isOnline())) {
                    //推送即将进入托管状态的倒计时
                    ComMsg.ComRes.Builder res = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_tuoguan, seat, (autoPlay ? 1 : 0), timeSecond, autoPlaySelf ? 1 : 0);
                    GeneratedMessage msg = res.build();
                    table.broadMsg(msg);
                }
            }
        }
        if (auto) {
            setAutoPlayTime(now);
            return true;
        }
        return false;
    }

    public boolean isAutoPlaySelf() {
        return autoPlaySelf;
    }

    public boolean isAutoPlay() {
        return autoPlay;
    }

    public int getPiaoFen() {
        return piaoFen;
    }

    public void setPiaoFen(int piaoFen) {
        this.piaoFen = piaoFen;
    }

    public List<Integer> getVirtualGang() {
        return virtualGang;
    }

    public int getVirtualHu() {
        return virtualHu;
    }

    public void setVirtualHu(int virtualHu) {
        this.virtualHu = virtualHu;
    }

    public void setAutoPlay(boolean autoPlay, boolean isSelf) {
        CxMjTable table = getPlayingTable(CxMjTable.class);
        if (this.autoPlay != autoPlay || autoPlaySelf != isSelf) {
            ComMsg.ComRes.Builder res = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_tuoguan, seat, autoPlay ? 1 : 0, 0, isSelf ? 1 : 0);
            GeneratedMessage msg = res.build();
            if (table != null) {
                table.broadMsgToAll(msg);
            }
        	if (!getHandPais().isEmpty()) {
            table.addPlayLog(table.getDisCardRound() + "_" +getSeat() + "_" + CxMjConstants.action_tuoguan + "_" +(autoPlay?1:0)+ getExtraPlayLog());
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

    public int getDianPaoNum() {
        return dianPaoNum;
    }

    public List<CxMjCardDisType> getCardTypes() {
        return cardTypes;
    }

    public void addDianPaoNum(int num) {
        this.dianPaoNum += num;
    }

    public void addJiePaoNum(int num) {
        this.jiePaoNum += num;
    }

    public void addZiMoNum(int num) {
        this.ziMoNum += num;
    }

    public long getAutoPlayTime() {
        return autoPlayTime;
    }

    public void setAutoPlayTime(long autoPlayTime) {
        this.autoPlayTime = autoPlayTime;
//		getPlayingTable().changeExtend();
    }

    public int getBuId() {
        return buId;
    }

    public void setBuId(int buId) {
        this.buId = buId;
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
     * 回放日志的额外信息
     *
     * @return
     */
    public String getExtraPlayLog() {
        return "_" + (isAutoPlay() ? 1 : 0) + "," + (isAutoPlaySelf() ? 1 : 0);
    }
}
