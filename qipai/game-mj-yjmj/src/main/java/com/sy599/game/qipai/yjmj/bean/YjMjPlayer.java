package com.sy599.game.qipai.yjmj.bean;

import com.google.protobuf.GeneratedMessage;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.FirstmythConstants;
import com.sy599.game.common.constant.SharedConstants.player_state;
import com.sy599.game.db.bean.group.GroupUser;
import com.sy599.game.db.dao.group.GroupDao;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.TablePhzResMsg.PhzHuCards;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes;
import com.sy599.game.msg.serverPacket.TableRes.YjClosingPlayerInfoRes;
import com.sy599.game.qipai.yjmj.command.YjMjCommandProcessor;
import com.sy599.game.qipai.yjmj.constant.EnumHelper;
import com.sy599.game.qipai.yjmj.constant.YjMjConstants;
import com.sy599.game.qipai.yjmj.rule.MajiangHelper;
import com.sy599.game.qipai.yjmj.rule.YjMjIndexArr;
import com.sy599.game.qipai.yjmj.rule.YjMj;
import com.sy599.game.qipai.yjmj.tool.YjMjTool;
import com.sy599.game.qipai.yjmj.tool.YjMjQipaiTool;
import com.sy599.game.util.DataMapUtil;
import com.sy599.game.util.GameUtil;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.util.StringUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class YjMjPlayer extends Player {
    /**
     * 座位id
     */
    private int seat;
    /**
     * 状态 1进入 2已准备 3正在玩 4已结束
     */
    private player_state state;
    /**
     * 牌局是否在线 1离线 2在线
     */
    private int isEntryTable;
    /**
     * 手上的牌
     */
    private List<YjMj> handPais;
    /**
     * 海底牌
     */
    private int haidiMajiang;
    /**
     * 打出去的牌
     */
    private List<YjMj> outPais;
    /**
     * 已碰的牌
     */
    private List<YjMj> peng;
    /**
     * 暗杠的牌
     */
    private List<YjMj> aGang;
    /**
     * 明杠的牌
     */
    private List<YjMj> mGang;
    /**
     * 接杠的牌
     */
    private List<YjMj> jgang;
    /**
     * 已吃的牌
     */
    private List<YjMj> chi;
    /**
     * 胜局数(无用)
     */
    private int winCount;
    /**
     * 负局数(无用)
     */
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
     * 0自摸 1接炮 2放炮  总统计
     */
    private int[] actionTotalArr = new int[3];
    /**
     * 小局结算 大胡列表
     * 0碰碰胡  1将将胡 2清一色 3七小队 4豪华七小队 5双豪华七小队 6三豪华七小队 7杠爆 8抢杠胡 9海底捞 10一条龙 11门清 12天胡 13一字翘 14报听
     */
    private List<Integer> dahu;
    private int passMajiangVal;
    private int passMengZi;
    private List<Integer> passGangValList;
    /**
     * 出牌对应操作信息
     */
    private List<CardDisType> cardTypes;
    /**
     * 杠相关信息    0暗杠次数 1摸杠次数 2接杠次数 3放杠次数
     */
    private int[] gangInfos;
    /**
     * 总结算
     * 大胡次数  0碰碰胡  1将将胡 2清一色 3七小队 4豪华七小队 5双豪华七小队 6三豪华七小队 7杠爆 8抢杠胡 9海底捞 10一条龙 11门清 12天胡 13一字翘 14报听
     */
    private int[] dahuCounts;
    /**
     * 明杠(摸杠)只能第一时间杠
     * 玩家碰的牌有三张相同的牌  再摸到这张牌后选择了过或者碰  玩家在可以摸牌生成操作时初始化  玩家选择过或者碰时则删除
     */
    private List<Integer> uncheckmGangs;

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

    public YjMjPlayer() {
        handPais = new ArrayList<YjMj>();
        outPais = new ArrayList<YjMj>();
        peng = new ArrayList<>();
        aGang = new ArrayList<>();
        mGang = new ArrayList<>();
        chi = new ArrayList<>();
        jgang = new ArrayList<>();
        passGangValList = new ArrayList<>();
        dahu = new ArrayList<>();
        cardTypes = new ArrayList<>();
        gangInfos = new int[4];
        dahuCounts = new int[YjMjHu.daHuCount];
        uncheckmGangs = new ArrayList<>();
    }

    public int getSeat() {
        return seat;
    }

    public void setSeat(int seat) {
        this.seat = seat;
    }

    public void initPais(List<YjMj> hand, List<YjMj> out) {
        if (hand != null) {
            this.handPais = hand;
        }
        if (out != null) {
            this.outPais = out;
        }
    }

    public void dealHandPais(List<YjMj> pais) {
        this.handPais = pais;
        getPlayingTable().changeCards(seat);
    }

    /**
     * 是否杠爆
     *
     * @return
     */
    public boolean isGangshangHua() {
        return dahu.contains(7);
    }

    public List<YjMj> getHandMajiang() {
        return handPais;
    }

    public List<Integer> getHandPais() {
        return MajiangHelper.toMajiangIds(handPais);
    }

    public List<Integer> getOutPais() {
        return MajiangHelper.toMajiangIds(outPais);
    }

    public List<YjMj> getOutMajing() {
        return outPais;
    }

    public void moMajiang(YjMj majiang) {
        setPassMajiangVal(0);
        handPais.add(majiang);
        getPlayingTable().changeCards(seat);
    }

    /**
     * @param cards
     * @param action
     * @param outGangSeat 放杠人的座位号
     */
    public void addOutPais(List<YjMj> cards, int action, int outGangSeat) {
        handPais.removeAll(cards);
        if (action == 0) {
            outPais.addAll(cards);
        } else {
            if (action == YjMjDisAction.action_peng || action == YjMjDisAction.action_gangPeng) {
                peng.addAll(cards);
            } else if (action == YjMjDisAction.action_minggang) {
                myExtend.setMjFengshen(FirstmythConstants.firstmyth_index12, 1);
                mGang.addAll(cards);
                if (cards.size() == 1) {
                    YjMj pengMajiang = cards.get(0);
                    Iterator<YjMj> iterator = peng.iterator();
                    while (iterator.hasNext()) {
                        YjMj majiang = iterator.next();
                        if (majiang.getVal() == pengMajiang.getVal()) {
                            mGang.add(majiang);
                            iterator.remove();
                        }
                    }
                }
            } else if (action == YjMjDisAction.action_angang) {
                myExtend.setMjFengshen(FirstmythConstants.firstmyth_index12, 1);
                aGang.addAll(cards);
            } else if (action == YjMjDisAction.action_jiegang) {// 沅江麻将补张就是杠
                myExtend.setMjFengshen(FirstmythConstants.firstmyth_index12, 1);
                jgang.addAll(cards);
                if (cards.size() == 1) {
                    YjMj pengMajiang = cards.get(0);
                    Iterator<YjMj> iterator = peng.iterator();
                    while (iterator.hasNext()) {
                        YjMj majiang = iterator.next();
                        if (majiang.getVal() == pengMajiang.getVal()) {
                            jgang.add(majiang);
                            iterator.remove();
                        }
                    }
                }
            } else if (action == -1) {// 抢杠胡的杠变成碰
                YjMj qGangMajiang = cards.get(0);
                Iterator<YjMj> iterator = mGang.iterator();
                List<YjMj> pengs = new ArrayList<>();
                while (iterator.hasNext()) {
                    YjMj majiang = iterator.next();
                    if (majiang.getVal() == qGangMajiang.getVal()) {
                        if (pengs.size() < 3) {
                            pengs.add(majiang);
                        }
                        iterator.remove();
                    }
                }
                peng.addAll(pengs);
                outPais.addAll(cards);
            }
            getPlayingTable().changeExtend();
            if (action == -1) {
                action = 0;
            }
            addCardType(action, cards, outGangSeat);
        }
        getPlayingTable().changeCards(seat);
    }

    /**
     * @param action
     * @param disCardList
     * @param outGangSeat 放杠人的座位号
     */
    public void addCardType(int action, List<YjMj> disCardList, int outGangSeat) {
        if (action != 0) {
            if (action == YjMjDisAction.action_minggang && disCardList.size() == 1) {
                YjMj majiang = disCardList.get(0);
                for (CardDisType disType : cardTypes) {
                    if (disType.getAction() == YjMjDisAction.action_peng) {
                        if (disType.isHasCardVal(majiang)) {
                            disType.setAction(YjMjDisAction.action_minggang);
                            disType.addCardId(majiang.getId());
                            break;
                        }
                    }
                }
            } else {
                CardDisType type = new CardDisType();
                type.setAction(action);
                type.setCardIds(MajiangHelper.toMajiangIds(disCardList));
                type.setHux(outGangSeat);
                cardTypes.add(type);
            }
        }
    }

    /**
     * 所有摆在外面的牌都是将
     *
     * @return
     */
    public boolean allOutMjIsJiang() {
        if (cardTypes == null || cardTypes.size() == 0) {
            return true;
        }
        for (CardDisType type : cardTypes) {
            List<Integer> cardIds = type.getCardIds();
            if (cardIds == null || cardIds.size() == 0) {
                continue;
            }
            for (int mjId : cardIds) {
                YjMj mj = YjMj.getMajang(mjId);
                if (mj != null && !mj.isJiang()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 所有摆在外面的牌都是码
     *
     * @return
     */
    public boolean allOutMjIsMa() {
        if (cardTypes == null || cardTypes.size() == 0) {
            return true;
        }
        for (CardDisType type : cardTypes) {
            List<Integer> cardIds = type.getCardIds();
            if (cardIds == null || cardIds.size() == 0) {
                continue;
            }
            for (int mjId : cardIds) {
                YjMj mj = YjMj.getMajang(mjId);
                if (mj != null && !mj.isMa()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 已经摸过牌了
     *
     * @return
     */
    public boolean isAlreadyMoMajiang() {
        return !handPais.isEmpty() && handPais.size() % 3 == 2;
    }

    public void removeOutPais(List<YjMj> cards, int action) {
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
        sb.append(MajiangHelper.implodeMajiang(chi, ",")).append("|");
        sb.append(MajiangHelper.implodeMajiang(jgang, ",")).append("|");
        sb.append(StringUtil.implode(dahu, ",")).append("|");
        sb.append(StringUtil.implode(gangInfos, ",")).append("|");
        sb.append(StringUtil.implode(dahuCounts, ",")).append("|");
        sb.append(StringUtil.implode(uncheckmGangs, ",")).append("|");
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
        String val8 = StringUtil.getValue(values, i++);
        chi = MajiangHelper.explodeMajiang(val8, ",");
        String val9 = StringUtil.getValue(values, i++);
        jgang = MajiangHelper.explodeMajiang(val9, ",");
        String val10 = StringUtil.getValue(values, i++);
        if (!StringUtils.isBlank(val10)) {
            dahu = StringUtil.explodeToIntList(val10);
        }
        String val11 = StringUtil.getValue(values, i++);
        if (!StringUtils.isBlank(val11)) {
            gangInfos = StringUtil.explodeToIntArray(val11);
        }
        String val12 = StringUtil.getValue(values, i++);
        if (!StringUtils.isBlank(val12)) {
            dahuCounts = StringUtil.explodeToIntArray(val12);
        }
        String val13 = StringUtil.getValue(values, i++);
        if (!StringUtils.isBlank(val13)) {
            uncheckmGangs = StringUtil.explodeToIntList(val13);
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
        sb.append(haidiMajiang).append(",");
        sb.append(passMengZi).append(",");
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
            this.state = EnumHelper.getPlayerState(stateVal);
            this.isEntryTable = StringUtil.getIntValue(values, i++);
            this.winCount = StringUtil.getIntValue(values, i++);
            this.lostCount = StringUtil.getIntValue(values, i++);
            this.point = StringUtil.getIntValue(values, i++);
            setTotalPoint(StringUtil.getIntValue(values, i++));
            this.lostPoint = StringUtil.getIntValue(values, i++);
            this.passMajiangVal = StringUtil.getIntValue(values, i++);
            this.haidiMajiang = StringUtil.getIntValue(values, i++);
            this.passMengZi = StringUtil.getIntValue(values, i++);
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
            list.addAll(MajiangHelper.toMajiangIds(peng));
        }
        if (!mGang.isEmpty()) {
            list.addAll(MajiangHelper.toMajiangIds(mGang));
        }
        if (!aGang.isEmpty()) {
            list.addAll(MajiangHelper.toMajiangIds(aGang));
        }
        if (!jgang.isEmpty()) {
            list.addAll(MajiangHelper.toMajiangIds(jgang));
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
        for (CardDisType type : cardTypes) {
            if (hide && lookUid != this.userId && type.getAction() == YjMjDisAction.action_angang) {
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
        res.setPoint(getTotalPoint());
        res.addAllOutedIds(getOutPais());
        res.addAllMoldIds(getMoldIds());
        res.addAllAngangIds(MajiangHelper.toMajiangIds(aGang));
        res.addAllMoldCards(buildDisCards(userId));
        List<YjMj> gangList = getGang();
        // 是否杠过
        res.addExt(gangList.isEmpty() ? 0 : 1); // 0
        YjMjTable table = getPlayingTable(YjMjTable.class);
        res.addExt(table.getMoMajiangSeat() == seat ? 1 : 0); // 1 牌桌上当前最后摸牌的人是否自己
        res.addExt(table.getBaotingSeat().containsKey(seat) ? 1 : 0);// 2玩家是否报听
        res.addExt(handPais != null ? handPais.size() : 0);// 3玩家手牌数量
        res.addExt(autoPlay ? 1 : 0); //4
        res.addExt(autoPlaySelf ? 1 : 0); //5
//		List<Integer> selfActions = table.getActionSeatMap().get(seat);
//		int alreadyLast4MoMajiang = 0;
//		if(table.getDisCardRound() > 0 && table.getLeftMajiangCount() < 4 && isAlreadyMoMajiang() && (selfActions == null || selfActions.isEmpty())) {
//			alreadyLast4MoMajiang = 1;
//		}
//		res.addExt(alreadyLast4MoMajiang);// 海底捞时玩家重连是否显示没胡 0已发过没胡 1没发过没胡 需要发送过  2显示没胡
        if (!StringUtils.isBlank(getHeadimgurl())) {
            res.setIcon(getHeadimgurl());
        } else {
            res.setIcon("");
        }
        if (state == player_state.ready || state == player_state.play) {// 玩家装备已经准备和正在玩的状态时通知前台已准备
            res.setStatus(1);
        } else {
            res.setStatus(0);
        }
        if (isrecover) {// 0是否要的起 1是否报单 2是否暂离(1暂离0在线)
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

    /**
     * 检查发送小胡
     */
    public void checkSendActionRes() {
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

    public void calcWin() {
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
        actionTotalArr = new int[3];
        dahuCounts = new int[YjMjHu.daHuCount];
        dahu = new ArrayList<>();
        gangInfos = new int[4];
        uncheckmGangs = new ArrayList<>();
        peng.clear();
        aGang.clear();
        mGang.clear();
        jgang.clear();
        chi.clear();
        cardTypes.clear();
        setPassMajiangVal(0);
        setHaidiMajiang(0);
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
        this.passMajiangVal = 0;
        this.passMengZi = 0;
        saveBaseInfo();
    }

    /**
     * 单局详情
     *
     * @return
     */
    public YjClosingPlayerInfoRes.Builder bulidOneClosingPlayerInfoRes() {
        YjClosingPlayerInfoRes.Builder res = YjClosingPlayerInfoRes.newBuilder();
        res.setUserId(userId + "");
        res.setName(name);
        res.setPoint(point);// 局分
        res.setLeftCardNum(0);
        res.setTotalPoint(getTotalPoint());// 总分
        List<Integer> allCards = new ArrayList<>();
        List<Integer> handPais = getHandPais();
        List<Integer> copyHandPais = new ArrayList<>(handPais);
        if (haidiMajiang != 0 && dahu != null && dahu.contains(8)) {// 海底时  胡抢杠胡
            if (copyHandPais.contains(haidiMajiang)) {
                copyHandPais.remove((Integer) haidiMajiang);
            }
        }
        allCards.addAll(copyHandPais);
        res.addAllCards(allCards);
        res.addAllMcards(buildDisCards(0, false));
        res.setSeat(seat);
        res.addAllDahus(dahu);
        res.addAllAGang(MajiangHelper.toGangCardList(aGang));//暗杠
        res.addAllMGang(MajiangHelper.toGangCardList(mGang));//明杠
        res.addAllJGang(MajiangHelper.toGangCardList(jgang));//接杠
        res.addAllPengs(MajiangHelper.toMajiangIds(peng));
        if (!StringUtils.isBlank(getHeadimgurl())) {
            res.setIcon(getHeadimgurl());
        } else {
            res.setIcon("");
        }
        res.setSex(sex);
        res.addAllGangInfos(DataMapUtil.toList(gangInfos));
        return res;
    }

    /**
     * 总局详情
     *
     * @return
     */
    public YjClosingPlayerInfoRes.Builder bulidTotalClosingPlayerInfoRes() {
        YjClosingPlayerInfoRes.Builder res = YjClosingPlayerInfoRes.newBuilder();
        res.setUserId(userId + "");
        res.setName(name);
        res.setPoint(point);
        res.setLeftCardNum(0);
        res.setZimoCount(actionTotalArr[0]);
        res.setJiePaoCount(actionTotalArr[1]);
        res.setFangPaoCount(actionTotalArr[2]);
        res.setTotalPoint(getTotalPoint());
        res.addAllDahus(dahu);
        List<Integer> allCards = new ArrayList<>();
        List<Integer> handPais = getHandPais();
        List<Integer> copyHandPais = new ArrayList<>(handPais);
        if (haidiMajiang != 0 && dahu != null && dahu.contains(8)) {// 海底时  胡抢杠胡
            if (copyHandPais.contains(haidiMajiang)) {
                copyHandPais.remove((Integer) haidiMajiang);
            }
        }
        allCards.addAll(copyHandPais);
        res.addAllCards(allCards);
        res.addAllAGang(MajiangHelper.toGangCardList(aGang));//暗杠
        res.addAllMGang(MajiangHelper.toGangCardList(mGang));//明杠
        res.addAllJGang(MajiangHelper.toGangCardList(jgang));//接杠
        res.addAllPengs(MajiangHelper.toMajiangIds(peng));
        res.setSeat(seat);
        if (!StringUtils.isBlank(getHeadimgurl())) {
            res.setIcon(getHeadimgurl());
        } else {
            res.setIcon("");
        }
        res.setSex(sex);
        res.addAllGangInfos(DataMapUtil.toList(gangInfos));
        res.addAllMcards(buildDisCards(0, false));
        res.addAllDahuCounts(DataMapUtil.toList(dahuCounts));
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
        this.passMajiangVal = 0;
        this.passMengZi = 0;
        setHaidiMajiang(0);
        setLostPoint(0);
        clearPassGangVal();
        peng.clear();
        aGang.clear();
        mGang.clear();
        chi.clear();
        jgang.clear();
        cardTypes.clear();
        dahu = new ArrayList<>();
        gangInfos = new int[4];
        getPlayingTable().changeExtend();
        getPlayingTable().changeCards(seat);
        changeState(player_state.entry);
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
                    this.outPais = MajiangHelper.toMajiang(list);
                } else {
                    CardDisType type = new CardDisType();
                    type.init(value);
                    cardTypes.add(type);
                    List<YjMj> majiangs = MajiangHelper.toMajiang(type.getCardIds());
                    if (type.getAction() == YjMjDisAction.action_angang) {
                        aGang.addAll(majiangs);
                    } else if (type.getAction() == YjMjDisAction.action_minggang) {
                        mGang.addAll(majiangs);
                    } else if (type.getAction() == YjMjDisAction.action_chi) {
                        chi.addAll(majiangs);
                    } else if (type.getAction() == YjMjDisAction.action_peng || type.getAction() == YjMjDisAction.action_gangPeng) {
                        peng.addAll(majiangs);
                    } else if (type.getAction() == YjMjDisAction.action_jiegang) {
                        jgang.addAll(majiangs);
                    }
                }
            }
        }
    }

    /**
     * 可以吃这张麻将的牌
     *
     * @param disMajiang
     * @return
     */
    public List<YjMj> getCanChiMajiangs(YjMj disMajiang) {
        return YjMjTool.checkChi(handPais, disMajiang);
    }

//	public boolean canHu(YjMj majiang) {
//		List<YjMj> copy = new ArrayList<>(handPais);
//		copy.add(majiang);
//		YjMjHu huBean = YjMjTool.isHuYuanjiang(copy, getGang(), peng, chi, buzhang, false);
//		return huBean.isHu();
//	}

    /**
     * 是否听牌
     *
     * @param val
     * @return
     */
    public boolean isTingPai(int val) {
        List<YjMj> copy = new ArrayList<>(handPais);
        List<YjMj> gangList = getGang();
        gangList.addAll(YjMjQipaiTool.dropVal(copy, val));
        List<YjMj> copyPeng = new ArrayList<>(peng);
        if (!peng.isEmpty()) {
            gangList.addAll(YjMjQipaiTool.dropVal(copyPeng, val));
        }
        if (copy.size() % 3 != 2) {
            copy.add(YjMj.getMajang(201));// 加上万能牌
        }
        List<YjMj> aGangs = new ArrayList<>(aGang);
        YjMjTable table = getPlayingTable(YjMjTable.class);
        boolean isMenQing = table.canMenQing();
        boolean isMaMaHu = table.canMaMaHu();
        YjMjHu huBean = YjMjTool.isHuYuanjiang(copy, gangList, aGangs, copyPeng, false, false, isMenQing, isMaMaHu);
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
        for (CardDisType huxi : cardTypes) {
            sb.append(huxi.toStr()).append(";");
        }
        return sb.toString();
    }

    /**
     * 别人出牌可做的操作
     *
     * @param majiang
     */
    public List<Integer> checkDisMajiangOld(YjMj majiang) {
        List<Integer> list = new ArrayList<>();
        int[] arr = new int[YjMjConstants.ACTION_INDEX_SIZE];
        YjMjTable table = getPlayingTable(YjMjTable.class);
        boolean isMenQing = table.canMenQing();
        boolean isMaMaHu = table.canMaMaHu();
        // 没有出现漏炮的情况
        if (table.getLeftMajiangCount() >= table.getMaxPlayerCount()) {// 只提示是否能胡
            if ((table.getDisCardSeat() != seat) && passMajiangVal == 0 &&
                    (!table.getBaotingSeat().containsKey(seat) || (table.getBaotingSeat().containsKey(seat) && table.getBaotingSeat().get(seat) == 0) // 报听第一次炮没胡 之后都不能胡炮
                            && !table.getHuPassSeat().contains(seat))) {// 过手胡 如果在没过自己手时 某一方打牌没胡 则不能再接炮
                List<YjMj> copy = new ArrayList<>(handPais);
                copy.add(majiang);
                YjMjHu huBean = YjMjTool.isHuYuanjiang(copy, getGang(), aGang, peng, false, false, isMenQing, isMaMaHu);
                if (huBean.isHu()) {
                    boolean canHu;
                    if (huBean.isPingHu()) {// 可胡平胡
                        if (huBean.isDahu()) {// 有大胡
                            YjMjIndexArr card_index = new YjMjIndexArr();
                            YjMjQipaiTool.getMax(card_index, handPais);
                            if (handPais.size() == 4 && card_index.getDuiziNum() == 2 && table.getKaqiao() > 0 && huBean.isPengpengHu() && huBean.getDahuList().size() == 1) {// 卡撬
                                canHu = false;
                            } else {
                                if (huBean.isJiangjiangHu()) { // 是否可以将将胡接炮
                                    if (!huBean.isMengQing() && !table.getBaotingSeat().containsKey(seat) && huBean.getDahuList().size() >= 2) {
                                        canHu = true;
                                    } else {
                                        canHu = false;
                                    }
                                } else if (huBean.isMaMaHu()) {// 是否可以码码胡接炮
                                    if (!huBean.isMengQing() && !table.getBaotingSeat().containsKey(seat) && huBean.getDahuList().size() >= 2) {
                                        canHu = true;
                                    } else {
                                        canHu = false;
                                    }
                                } else {
                                    canHu = true;
                                }
                            }
                        } else {// 抢杠胡、报听平胡可以接炮
                            if (table.getBaotingSeat().containsKey(seat) || table.getMoGangHuSeats().contains(seat)) {
                                canHu = true;
                            } else {
                                canHu = false;
                            }
                        }
                    } else {// 大胡 将将胡 小7对特殊牌型
                        if (huBean.isJiangjiangHu()) {// 是否可以将将胡接炮
                            if (!huBean.isMengQing() && !table.getBaotingSeat().containsKey(seat) && huBean.getDahuList().size() >= 2) {
                                canHu = true;
                            } else {
                                canHu = false;
                            }
                        } else if (huBean.isMaMaHu()) {// 是否可以码码胡接炮
                            if (!huBean.isMengQing() && !table.getBaotingSeat().containsKey(seat) && huBean.getDahuList().size() >= 2) {
                                canHu = true;
                            } else {
                                canHu = false;
                            }
                        } else {
                            canHu = true;
                        }
                    }
                    if (canHu) {
                        arr[YjMjConstants.ACTION_INDEX_HU] = 1;
                    }
                }
            }
            // 报听判断
            if (seat == table.getLastWinSeat()
                    && table.hadNotMoMj()
                    && table.getDisCardSeat() == table.getLastWinSeat()
                    && getHandPais().size() >= 13
                    && isTingPai(0)) {// 庄家打第一张牌 是否可报听
                arr[YjMjConstants.ACTION_INDEX_BAOTING] = 1;
            }
            // 杠判断
            if (!table.getBaotingSeat().containsKey(seat)) {// 报听后不能接杠
                if (table.getDisCardSeat() != seat) {// 现在出牌的人不是自己
                    int count = MajiangHelper.getMajiangCount(handPais, majiang.getVal());
                    if (count == 3) {// 接杠
                        arr[YjMjConstants.ACTION_INDEX_JIEGANG] = 1;// 可以补张
                    }
                    if (count >= 2 && !table.inPengPassSeat(seat, majiang.getVal())) {// 是否已经碰过(未过手)
                        arr[YjMjConstants.ACTION_INDEX_PENG] = 1;// 可以碰
                    }
                } else {// 出牌的人是自己  明杠
                    Map<Integer, Integer> pengMap = MajiangHelper.toMajiangValMap(peng);
                    if (pengMap.containsKey(majiang.getVal())) {
                        arr[YjMjConstants.ACTION_INDEX_MINGGANG] = 1;// 可以补张
                    }
                }
            }
        } else {// 海底捞四张只能胡或者过  不能碰
            if ((!table.moHu() || table.getDisCardSeat() == seat) && passMajiangVal == 0) {
                List<YjMj> copy = new ArrayList<>(handPais);
                copy.add(majiang);
                YjMjHu huBean = YjMjTool.isHuYuanjiang(copy, getGang(), aGang, peng, false, false, isMenQing, isMaMaHu);
                if (huBean.isHu()) {
                    if (huBean.isPingHu()) {
                        if (table.getDisCardSeat() == seat) {// 沅江麻将平胡只能自摸
                            arr[YjMjConstants.ACTION_INDEX_HU] = 1;
                        }
                    } else {
                        if (huBean.isJiangjiangHu() && table.getDisCardSeat() != seat) {// 将将胡不可接炮
                        } else {
                            arr[YjMjConstants.ACTION_INDEX_HU] = 1;
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
            return Collections.emptyList();
        }
    }

    /**
     * 别人出牌可做的操作
     *
     * @param mj
     */
    public List<Integer> checkDisMj(YjMj mj, boolean isQiangGang) {

        int[] arr = new int[YjMjConstants.ACTION_INDEX_SIZE];
        YjMjTable table = getPlayingTable(YjMjTable.class);
        boolean canJiePao = false;
        YjMjHu huBean = null;
        boolean needCheckJiePao = true;
//        if (passMajiangVal == 0) {
//            needCheckJiePao = true;
//        } else {
//            if (passMajiangVal != mj.getVal()) { // 同一张牌过胡了，不需要检查接炮
//                needCheckJiePao = true;
//            }
//        }
        if (needCheckJiePao) {
            boolean canMenQing = table.canMenQing();
            boolean canMaMaHu = table.canMaMaHu();
            List<YjMj> copy = new ArrayList<>(handPais);
            copy.add(mj);
            huBean = YjMjTool.isHuYuanjiang(copy, getGang(), aGang, peng, false, false, canMenQing, canMaMaHu);
            if (huBean.isHu()) {
                if (isQiangGang) {
                    huBean.setQiangGangHu(true);
                }
                if (huBean.isJiangjiangHu()) { // 将将胡
                    if (huBean.isXiao7duiHu() || huBean.isPengpengHu() || huBean.isQiangGangHu()) {
                        canJiePao = true;
                    }
                } else if (huBean.isMaMaHu()) { // 码码胡
                    if (huBean.isXiao7duiHu() || huBean.isPengpengHu() || huBean.isQiangGangHu()) {
                        canJiePao = true;
                    }
                } else if (huBean.isPengpengHu()) { // 碰碰胡
                    YjMjIndexArr card_index = new YjMjIndexArr();
                    YjMjQipaiTool.getMax(card_index, handPais);
                    if (table.getKaqiao() > 0 && handPais.size() == 4 && card_index.getDuiziNum() == 2 && huBean.isPengpengHu() && huBean.getDahuList().size() == 1) {
                        // 卡撬
                        canJiePao = false;
                    } else {
                        if (table.getBaotingSeat().containsKey(getSeat())) {
                            canJiePao = true;
                        } else {
                            if (huBean.isXiao7duiHu() || huBean.isPengpengHu() || huBean.isQingyiseHu() || huBean.isYiTiaoLong() || huBean.isQiangGangHu()) {
                                canJiePao = true;
                            }
                        }
                    }
                } else {
                    if (table.getBaotingSeat().containsKey(getSeat())) {
                        canJiePao = true;
                    } else {
                        if (huBean.isXiao7duiHu() || huBean.isPengpengHu() || huBean.isQingyiseHu() || huBean.isYiTiaoLong() || huBean.isQiangGangHu()) {
                            canJiePao = true;
                        }
                    }
                }
                // 可以接炮
                if (canJiePao) {
                    arr[YjMjConstants.ACTION_INDEX_HU] = 1;
                }
            }
        }

        // 报听后不能吃碰杠
        // 检查抢杠胡时不需要检查吃碰杠
        if (!isQiangGang && !table.getBaotingSeat().containsKey(seat)) {
            if (table.getDisCardSeat() != seat) { // 现在出牌的人不是自己
                int count = MajiangHelper.getMajiangCount(handPais, mj.getVal());
                if (count == 3) {// 接杠
                    arr[YjMjConstants.ACTION_INDEX_JIEGANG] = 1;// 可以补张
                }
                if (count >= 2 && !table.inPengPassSeat(seat, mj.getVal())) {// 是否已经碰过(未过手)
                    arr[YjMjConstants.ACTION_INDEX_PENG] = 1; // 可以碰
                }
            }
        }


        if (canJiePao) {
            if (table.getBaotingSeat().containsKey(getSeat())) {
                huBean.setBaoting(true);
            }
            huBean.initDahuList();
            int newMengZi = 1;
            if (huBean.isDahu()) {
                newMengZi = 1 + huBean.getDahuPoint();
            }
            // 检查过胡
            if (getPassMajiangVal() > 0) {
                if (this.passMengZi > 0 && newMengZi <= this.passMengZi) {
                    // 不能接炮了
                    arr[YjMjConstants.ACTION_INDEX_HU] = 0;
                }
            }
            setPassMengZi(newMengZi);
        }


        List<Integer> list = new ArrayList<>();
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
     * 抢杠胡时可做的操作
     *
     * @param majiang 杠牌
     * @return 0胡 1碰 2明杠 3暗杠 4接杠 5杠爆 6报听
     */
    public List<Integer> checkQGangHu(YjMj majiang) {
        List<Integer> list = new ArrayList<>();
        int[] arr = new int[8];
        YjMjTable table = getPlayingTable(YjMjTable.class);
        boolean isMenQing = table.canMenQing();
        boolean isMaMaHu = table.canMaMaHu();
        // 没有出现漏炮的情况
        if (passMajiangVal == 0) {// 过手胡 如果在没过自己手时 某一方打牌没胡 则不能再接炮
            List<YjMj> copy = new ArrayList<>(handPais);
            copy.add(majiang);
//			if(table.getLastMajiang() != null && copy.size() % 3 != 2) {// 如果玩家摸过海底牌了 胡抢杠胡
//				copy.remove(YjMj.getMajang(getHaidiMajiang()));
//			}
            YjMjHu huBean = YjMjTool.isHuYuanjiang(copy, getGang(), aGang, peng, false, false, isMenQing, isMaMaHu);
            if (huBean.isHu()) {
                boolean canHu;
                if (huBean.isPingHu()) {// 可胡平胡
                    if (huBean.isDahu()) {// 有大胡
                        YjMjIndexArr card_index = new YjMjIndexArr();
                        YjMjQipaiTool.getMax(card_index, handPais);
                        if (handPais.size() == 4 && card_index.getDuiziNum() == 2 && table.getKaqiao() > 0 && huBean.isPengpengHu() && huBean.getDahuList().size() == 1) {// 卡撬
                            canHu = false;
                        } else {
                            canHu = true;
                        }
                    } else {// 抢杠胡、报听平胡可以接炮
                        canHu = true;
                    }
                } else {// 大胡 将将胡 小7对特殊牌型
                    canHu = true;
                }
                if (canHu)
                    arr[0] = 1;
            }
        }
        // 杠判断
        if (!table.getBaotingSeat().containsKey(seat)) {// 报听后不能接杠
            if (table.getDisCardSeat() != seat) {// 现在出牌的人不是自己
                int count = MajiangHelper.getMajiangCount(handPais, majiang.getVal());
                if (count == 3) {// 接杠
                    arr[4] = 1;// 可以补张
                }
                if (count >= 2 && !table.inPengPassSeat(seat, majiang.getVal())) {// 是否已经碰过(未过手)
                    arr[1] = 1;// 可以碰
                }
            } else {// 出牌的人是自己  明杠
                Map<Integer, Integer> pengMap = MajiangHelper.toMajiangValMap(peng);
                if (pengMap.containsKey(majiang.getVal())) {
                    arr[2] = 1;// 可以补张
                }
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

    public YjMj getLastMoMajiang() {
        if (handPais.isEmpty()) {
            return null;
        } else {
            return handPais.get(handPais.size() - 1);
        }
    }

    public int getDahuCount() {
        return dahu.size();
    }

    public List<Integer> getDahu() {
        return dahu;
    }

    public void setDahu(List<Integer> dahuList) {
        this.dahu = dahuList;
        for (int dahu : dahuList) {
            changeDahuCounts(dahu, 1);
        }
        getPlayingTable().changeExtend();
    }

    public String getDaHuNames() {
        return YjMjHu.getDahuNames(getDahu());
    }


    public List<Integer> getUncheckmGangs() {
        return uncheckmGangs;
    }

    public void addUncheckmGangs(Integer mGangId) {
        uncheckmGangs.add(mGangId);
        getPlayingTable().changeExtend();
    }

    public void removeUncheckmGang(Integer mGangId) {
        uncheckmGangs.remove(mGangId);
        getPlayingTable().changeExtend();
    }

    /**
     * 胡牌
     *
     * @param disMajiang
     * @param isbegin
     * @return
     */
    public YjMjHu checkHu(YjMj disMajiang, boolean isbegin) {
        List<YjMj> copy = new ArrayList<>(handPais);
        if (disMajiang != null) {
            copy.add(disMajiang);
        }
        YjMjTable table = getPlayingTable(YjMjTable.class);
        if (table.getLastMajiang() != null && copy.size() % 3 != 2) {// 如果玩家摸过海底牌了 胡抢杠胡
            copy.remove(YjMj.getMajang(getHaidiMajiang()));
        }
        boolean isMenQing = table.canMenQing();
        boolean isMaMaHu = table.canMaMaHu();
        YjMjHu hu = YjMjTool.isHuYuanjiang(copy, getGang(), aGang, peng, isbegin, false, isMenQing, isMaMaHu);
        return hu;
    }

    /**
     * 自己出牌可做的操作
     *
     * @param mj       null 时自己碰或者吃,不能胡牌
     * @param isBegin  起牌是第一次出牌
     * @param isGangMo 是否杠后摸牌(杠爆胡)
     */
    public List<Integer> checkMo(YjMj mj, boolean isBegin, boolean isGangMo) {
        List<Integer> list = new ArrayList<>();
        int[] arr = new int[YjMjConstants.ACTION_INDEX_SIZE];
        YjMjTable table = getPlayingTable(YjMjTable.class);
        if (mj != null || isBegin) {// 碰的时候判断不能胡牌
            YjMjHu hu = YjMjTool.isHuYuanjiang(this, isBegin, isGangMo);
            if (hu.isHu()) {
                if (isGangMo) {
                    arr[YjMjConstants.ACTION_INDEX_GANGBAO] = 1;// 杠爆
                    hu.setGangBao(true);
                }
                hu.initDahuList();
                int newMengZi = 1;
                if (hu.isDahu()) {
                    newMengZi = 1 + hu.getDahuPoint();
                }
                setPassMengZi(newMengZi);
                arr[YjMjConstants.ACTION_INDEX_HU] = 1;
            }
            if (seat != table.getLastWinSeat() && isBegin && isTingPai(0)) {// 闲家起手听牌 报听
                arr[YjMjConstants.ACTION_INDEX_BAOTING] = 1;
            }
        }
        if (isAlreadyMoMajiang()) {
            Map<Integer, Integer> pengMap = MajiangHelper.toMajiangValMap(peng);
            if (pengMap != null && !pengMap.isEmpty()) {
                for (YjMj handMajiang : handPais) {// 手上就有一张杠牌
                    if (pengMap.containsKey(handMajiang.getVal())) {
                        if (uncheckmGangs.contains(handMajiang.getVal())) {
                            arr[YjMjConstants.ACTION_INDEX_MINGGANG] = 1;// 明杠
                            break;
                        }
                    }
                }
            }
            Map<Integer, Integer> handMap = MajiangHelper.toMajiangValMap(handPais);
            if (table.getBaotingSeat().containsKey(seat)) {// 如果玩家报听
                YjMjHu hu = YjMjTool.isHuYuanjiang(this, isBegin, isGangMo);
                if (hu.isHu() && hu.isJiangjiangHu() && handMap.containsValue(4)) {// 将将胡报听可以暗杠
                    arr[YjMjConstants.ACTION_INDEX_ANGANG] = 1;
                } else {// 平胡去掉暗杠的牌 还能报听 则可以暗杠
                    for (int majiangValue : handMap.keySet()) {
                        if (handMap.get(majiangValue) == 4) {
                            List<YjMj> copy = new ArrayList<>(this.getHandMajiang());
                            List<YjMj> aGangs = YjMjTool.dropMjValue(copy, majiangValue);
                            List<YjMj> pengs = new ArrayList<>(peng);
                            copy.add(YjMj.mj201);// 补上红中
                            boolean isMenQing = table.canMenQing();
                            boolean isMaMaHu = table.canMaMaHu();
                            hu = YjMjTool.isHuYuanjiang(copy, aGangs, aGangs, pengs, isBegin, isGangMo, isMenQing, isMaMaHu);
                            if (hu.isHu()) {
                                arr[YjMjConstants.ACTION_INDEX_ANGANG] = 1;
                                break;
                            }
                        }
                    }
                }
            } else {
                if (handMap.containsValue(4)) {
                    arr[YjMjConstants.ACTION_INDEX_ANGANG] = 1;
                }
            }
            if (mj != null && pengMap.containsKey(mj.getVal())) {// 明杠  摸上来的麻将可以杠
                if (!uncheckmGangs.contains(mj.getVal())) {
                    addUncheckmGangs(mj.getVal());
                    arr[YjMjConstants.ACTION_INDEX_MINGGANG] = 1;// 明杠
                }
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
     * 结算统计操作
     *
     * @param index 0自摸 1接炮 2放炮 总统计
     * @param val   次数
     */
    public void changeAction(int index, int val) {
        actionTotalArr[index] += val;
        getPlayingTable().changeExtend();
    }

    /**
     * 总结算 大胡次数累计统计
     *
     * @param index 大胡次数 0碰碰胡 1将将胡 2清一色 3七小队 4豪华七小队 5双豪华七小队 6三豪华七小队 7杠爆 8抢杠胡 9海底捞 10一条龙 11门清 12天胡 13一字翘
     * @param val   次数
     */
    public void changeDahuCounts(int index, int val) {
        dahuCounts[index] += val;
        getPlayingTable().changeExtend();
    }

    public List<YjMj> getPeng() {
        return peng;
    }

    public List<YjMj> getaGang() {
        return aGang;
    }

    public List<YjMj> getmGang() {
        return mGang;
    }


    public List<YjMj> getGang() {
        List<YjMj> gang = new ArrayList<>();
        gang.addAll(aGang);
        gang.addAll(mGang);
        gang.addAll(jgang);
        return gang;
    }

    public int getPassMajiangVal() {
        return passMajiangVal;
    }

    /**
     * 漏炮
     *
     * @param passMjVal
     */
    public void setPassMajiangVal(int passMjVal) {
        YjMjTable table = getPlayingTable(YjMjTable.class);
        if (passMjVal == 0 && table != null && table.getBaotingSeat().containsKey(getSeat())) {
            return;
        }
        if (this.passMajiangVal != passMjVal) {
            this.passMajiangVal = passMjVal;
            if (passMjVal == 0) {
                this.passMengZi = 0;
            }
            changeTbaleInfo();
        }
    }

    public void setPassMengZi(int mengZi) {
        if (this.passMengZi != mengZi) {
            this.passMengZi = mengZi;
            changeTbaleInfo();
        }
    }

    public void setHaidiMajiang(int haidiMajiang) {
        this.haidiMajiang = haidiMajiang;
        changeTbaleInfo();
    }

    public int getHaidiMajiang() {
        return this.haidiMajiang;
    }

    public boolean hadMoHaiDi() {
        return haidiMajiang != 0;
    }

    /**
     * 可以碰可以杠的牌 选择了碰 再杠不算分
     *
     * @param majiang
     * @return
     */
    public boolean isPassGang(YjMj majiang) {
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

    public int[] getGangInfos() {
        return gangInfos;
    }

    /**
     * 更新杠次数信息
     * 0暗杠次数 1摸杠次数 2接杠次数 3放杠次数
     */
    public void updateGangInfo(int index, int num) {
        int value = gangInfos[index];
        value += num;
        gangInfos[index] = value;
        getPlayingTable().changeExtend();
    }

    public static void main(String[] args) {
        List<Integer> l = new ArrayList<>(4);
        l.set(2, 0);
    }

    @Override
    public void endCompetition1() {

    }

    /**
     * 检查托管
     *
     * @param autoType  0打牌，1准备，2胡牌
     * @param forceSend 立即推送倒计时，用于断线重连
     * @return
     */
    public boolean checkAutoPlay(int autoType, boolean forceSend) {
        YjMjTable table = getPlayingTable(YjMjTable.class);
        long now = System.currentTimeMillis();
        boolean auto = isAutoPlay();
        if (!auto && table.isAutoPlay()) {
            //检查玩家是否进入系统托管状态
            if (!checkAutoPlay) {
                if (isAlreadyMoMajiang() || table.getActionSeatMap().containsKey(seat)) {
                    setCheckAutoPlay(true);
                } else {
                    setCheckAutoPlay(false);
                    return false;
                }
            }

            int autoTimeOut = table.getAutoTime();
            int timeOut = (int) (now - getLastCheckTime()) / 1000;
            if (timeOut >= autoTimeOut) {
                //进入托管状态
                auto = true;
                setAutoPlay(true, false);
                setAutoPlayCheckedTime(autoTimeOut); //进入过托管就启用防恶意托管
            } else {
                if (sendAutoTime == 0) {
                    sendAutoTime = now;
                    setLastCheckTime(now);
                    timeOut = (int) (now - getLastCheckTime()) / 1000;
                }
                if (timeOut >= 10) {
                    addAutoPlayCheckedTime(1);
                }
                if (getAutoPlayCheckedTime() >= table.getAutoTime()) {
                    //进入防恶意托管
                    if (timeOut >= autoTimeOut) {
                        auto = true;
                        setAutoPlay(true, false);
                    }
                    autoTimeOut = autoTimeOut;
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
                    if (timeOut >= YjMjConstants.AUTO_READY_TIME) {
                        setAutoPlayTime(0);
                        return true;
                    }
                } else if (autoType == 2) {
                    if (timeOut >= YjMjConstants.AUTO_HU_TIME) {
                        setAutoPlayTime(0);
                        return true;
                    }
                } else {
                    if (timeOut >= YjMjConstants.AUTO_PLAY_TIME) {
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
        YjMjTable table = getPlayingTable(YjMjTable.class);
        boolean needLog = false;
        if (this.autoPlay != autoPlay || autoPlaySelf != isSelf) {
            ComMsg.ComRes.Builder res = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_tuoguan, seat, autoPlay ? 1 : 0, 0, isSelf ? 1 : 0);
            GeneratedMessage msg = res.build();
            if (table != null) {
                table.broadMsgToAll(msg);
            }
            if (!getHandPais().isEmpty()) {
                table.addPlayLog(table.getDisCardRound() + "_" + getSeat() + "_" + YjMjDisAction.action_tuoguan + "_" + (autoPlay ? 1 : 0) + getExtraPlayLog());
            }
            needLog = true;
        }
        this.autoPlay = autoPlay;
        this.autoPlaySelf = autoPlay && isSelf;
        this.checkAutoPlay = autoPlay;
        setLastCheckTime(System.currentTimeMillis());
        if (table != null) {
            table.changeExtend();
            if (needLog) {
                StringBuilder sb = new StringBuilder();
                sb.append("YjMj");
                sb.append("|").append(table.getId());
                sb.append("|").append(table.getPlayBureau());
                sb.append("|").append(this.getUserId());
                sb.append("|").append(this.getSeat());
                sb.append("|").append("setAutoPlay");
                sb.append("|").append((autoPlay ? 1 : 0));
                sb.append("|").append((isSelf ? 1 : 0));
                LogUtil.msg(sb.toString());
            }
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

    public static final List<Integer> wanfaList = Arrays.asList(GameUtil.play_type_yuanjiang);

    public static void loadWanfaPlayers(Class<? extends Player> cls) {
        for (Integer integer : wanfaList) {
            PlayerManager.wanfaPlayerTypesPut(integer, cls, YjMjCommandProcessor.getInstance());
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

    public void setPiaoPoint(int piaoPoint) {
        this.piaoPoint = piaoPoint;
        changeTableInfo();
    }

    public int getPiaoPoint() {
        return piaoPoint;
    }
}
