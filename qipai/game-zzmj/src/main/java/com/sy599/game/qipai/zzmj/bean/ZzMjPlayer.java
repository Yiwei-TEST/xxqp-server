package com.sy599.game.qipai.zzmj.bean;

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
import com.sy599.game.qipai.zzmj.command.ZzMjCommandProcessor;
import com.sy599.game.qipai.zzmj.constant.ZzMjConstants;
import com.sy599.game.qipai.zzmj.rule.ZzMj;
import com.sy599.game.qipai.zzmj.tool.ZzMjHelper;
import com.sy599.game.qipai.zzmj.tool.ZzMjQipaiTool;
import com.sy599.game.qipai.zzmj.tool.ZzMjTool;
import com.sy599.game.util.GameUtil;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.util.StringUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

public class ZzMjPlayer extends Player {
    // 座位id
    private int seat;
    // 状态
    private player_state state;// 1进入 2已准备 3正在玩 4已结束
    /**
     * 牌局是否在线 1离线 2在线
     */
    private int isEntryTable;
    private List<ZzMj> handPais;
    private List<ZzMj> outPais;
    private List<ZzMj> peng;
    private List<ZzMj> aGang;
    private List<ZzMj> mGang;
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
    private List<ZzMjCardDisType> cardTypes;
    //胡牌类型
    private List<Integer> huType;

    private volatile boolean autoPlay = false;//托管
    private volatile boolean autoPlaySelf = false;//托管
    private volatile long lastCheckTime = 0;//最后检查时间
    private volatile long autoPlayTime = 0;//自动操作时间
    private volatile boolean checkAutoPlay = false; //是否是牌桌上的焦点
    private volatile long sendAutoTime = 0;//发送倒计时间
    private int huNum=0;//胡牌次数
    private int dianPaoNum=0;//点炮次数
    private int gongGangNum=0;//公杠次数
    private int anGangNum=0;//暗杠次数
    private int isJiePao=0;//是否已经接炮
    /**
     * [类型]=分值 0胡牌分，1鸟分，2杠分,3庄闲
     */
    private int[] pointArr = new int[4];

    private int piaoFen=-1;
    //输赢飘分
    private int winLostPiaoFen=0;
    //过操作计时
    private long lastPassTime;
    public ZzMjPlayer() {
        handPais = new ArrayList<ZzMj>();
        outPais = new ArrayList<ZzMj>();
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

    public int getIsJiePao() {
        return isJiePao;
    }

    public void setIsJiePao(int isJiePao) {
        this.isJiePao = isJiePao;
    }

    public int getWinLostPiaoFen() {
        return winLostPiaoFen;
    }

    public void setWinLostPiaoFen(int winLostPiaoFen) {
        this.winLostPiaoFen = winLostPiaoFen;
    }

    public long getLastPassTime() {
        return lastPassTime;
    }

    public void setLastPassTime(long lastPassTime) {
        this.lastPassTime = lastPassTime;
    }

    public void dealHandPais(List<ZzMj> pais) {
        this.handPais = pais;
        getPlayingTable().changeCards(seat);
    }

    public List<ZzMj> getHandMajiang() {
        return handPais;
    }

    public boolean haveHongzhong() {
        for (ZzMj majiang : handPais) {
            if (majiang.isHongzhong()) {
                return true;
            }
        }
        return false;
    }

    public List<Integer> getHandPais() {
        return ZzMjHelper.toMajiangIds(handPais);
    }

    public List<Integer> getOutPais() {
        return ZzMjHelper.toMajiangIds(outPais);
    }

    public List<ZzMj> getOutMajing() {
        return outPais;
    }

    public void moMajiang(ZzMj majiang) {
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
    public void addOutPais(List<ZzMj> cards, int action, int disSeat) {
        handPais.removeAll(cards);
        if (action == ZzMjDisAction.action_chupai) {
            outPais.addAll(cards);
        } else {
            if (action == ZzMjDisAction.action_peng) {
                peng.addAll(cards);
            } else if (action == ZzMjDisAction.action_minggang) {
                mGang.addAll(cards);
                if (cards.size() != 1) {
                } else {
                    ZzMj pengMajiang = cards.get(0);
                    Iterator<ZzMj> iterator = peng.iterator();
                    while (iterator.hasNext()) {
                        ZzMj majiang = iterator.next();
                        if (majiang.getVal() == pengMajiang.getVal()) {
                            mGang.add(majiang);
                            iterator.remove();
                        }
                    }
                }
                changeAction(ZzMjConstants.ACTION_COUNT_INDEX_MINGGANG, 1);
            } else if (action == ZzMjDisAction.action_angang) {
                aGang.addAll(cards);
                changeAction(ZzMjConstants.ACTION_COUNT_INDEX_ANGANG, 1);
            }
            getPlayingTable().changeExtend();
            addCardType(action, cards, disSeat, 0);
        }
        getPlayingTable().changeCards(seat);
    }

    public void qGangUpdateOutPais(ZzMj card) {
        Iterator<ZzMj> iterator = mGang.iterator();
        while (iterator.hasNext()) {
            ZzMj majiang = iterator.next();
            if (majiang.getVal() == card.getVal()) {
                peng.add(majiang);
                iterator.remove();
            }
        }
        for (ZzMjCardDisType disType : cardTypes) {
            if (disType.getAction() == ZzMjDisAction.action_minggang) {
                if (disType.isHasCardVal(card)) {
                    disType.setAction(ZzMjDisAction.action_peng);
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
    public void addCardType(int action, List<ZzMj> disCardList, int disSeat, int disStatus) {
        if (action != 0) {
            if (action == ZzMjDisAction.action_minggang && disCardList.size() == 1) {
                ZzMj majiang = disCardList.get(0);
                for (ZzMjCardDisType disType : cardTypes) {
                    if (disType.getAction() == ZzMjDisAction.action_peng) {
                        if (disType.isHasCardVal(majiang)) {
                            disType.setAction(ZzMjDisAction.action_minggang);
                            disType.addCardId(majiang.getId());
                            disType.setDisSeat(seat);
                            break;
                        }
                    }
                }
            } else {
                ZzMjCardDisType type = new ZzMjCardDisType();
                type.setAction(action);
                type.setCardIds(ZzMjQipaiTool.toMajiangIds(disCardList));
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

    public void removeOutPais(List<ZzMj> cards, int action) {
        boolean remove = outPais.removeAll(cards);
        if (remove) {
//			if (action == ZzMjDisAction.action_peng) {
//				changeAction(4, 1);
//			} else if (action == ZzMjDisAction.action_minggang) {
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
        sb.append(StringUtil.implode(pointArr)).append("|");
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
        String val8 = StringUtil.getValue(values, i++);
        if (!StringUtils.isBlank(val8)) {
            pointArr = StringUtil.explodeToIntArray(val8);
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
        sb.append(piaoFen).append(",");
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
            this.piaoFen = StringUtil.getIntValue(values, i++);
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
            list.addAll(ZzMjHelper.toMajiangIds(peng));
        }
        if (!mGang.isEmpty()) {
            list.addAll(ZzMjHelper.toMajiangIds(mGang));
        }
        if (!aGang.isEmpty()) {
            list.addAll(ZzMjHelper.toMajiangIds(aGang));
        }
        return list;
    }

    public List<PhzHuCards> buildDisCards(long lookUid) {
        return buildDisCards(lookUid, true);
    }

    public List<PhzHuCards> buildDisCards(long lookUid, boolean hide) {
        List<PhzHuCards> list = new ArrayList<>();
        for (ZzMjCardDisType type : cardTypes) {
            if (hide && lookUid != this.userId && type.getAction() == ZzMjDisAction.action_angang) {
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
        res.addAllAngangIds(ZzMjHelper.toMajiangIds(aGang));
        res.addAllMoldCards(buildDisCards(userId));
        List<ZzMj> gangList = getGang();
        // 是否杠过
        ZzMjTable table = getPlayingTable(ZzMjTable.class);
        // 现在是否自己摸的牌
        res.addExt(gangList.isEmpty() ? 0 : 1);
        res.addExt((isAlreadyMoMajiang()) ? 1 : 0);
        res.addExt(handPais != null ? handPais.size() : 0);
        res.addExt(autoPlay ? 1 : 0);
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
        if (table.isCreditTable()) {
            GroupUser gu = getGroupUser();
            String groupId = table.loadGroupId();
            if (gu == null || !groupId.equals(gu.getGroupId() + "") || gu.getTempCredit() > 0) {
                gu = GroupDao.getInstance().loadGroupUser(getUserId(), groupId);
                setGroupUser(gu);
            }
            res.setCredit(gu != null ? gu.getCredit() : 0);
        }
        return buildPlayInTableInfo1(res);
    }

    public List<ZzMj> getGang() {
        List<ZzMj> gang = new ArrayList<>();
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
        huNum=0;//胡牌次数
        dianPaoNum=0;//点炮次数
        gongGangNum=0;//公杠次数
        anGangNum=0;//暗杠次数
        pointArr = new int[4];
        saveBaseInfo();
        setPiaoFen(-1);
        setWinLostPiaoFen(0);
        setWinGold(0);
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
        for (ZzMjCardDisType type : cardTypes) {
            list.add(type.buildMsg().build());
        }
        res.addAllMoldPais(list);
        res.addAllDahus(huType);
        res.setGoldFlag(getGoldResult());
        res.addAllPointArr(Arrays.asList(ArrayUtils.toObject(pointArr)));
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
        for (ZzMjCardDisType type : cardTypes) {
            list.add(type.buildMsg().build());
        }
        res.addAllMoldPais(list);
        res.addAllDahus(huType);
        res.setActionCount(0,huNum);
        res.setActionCount(1,dianPaoNum);
        res.setActionCount(2,gongGangNum);
        res.setActionCount(3,anGangNum);
        res.setGoldFlag(getGoldResult());
        res.addAllPointArr(Arrays.asList(ArrayUtils.toObject(pointArr)));
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
        pointArr = new int[4];
        ZzMjTable t=(ZzMjTable)table;
        if(t.getPiaoFenType()!=5)
            setPiaoFen(-1);
        setWinLostPiaoFen(0);
    }

    @Override
    public void initPais(String handPai, String outPai) {
        if (!StringUtils.isBlank(handPai)) {
            List<Integer> list = StringUtil.explodeToIntList(handPai);
            this.handPais = ZzMjHelper.toMajiang(list);
        }
        if (!StringUtils.isBlank(outPai)) {
            String[] values = outPai.split(";");
            int i = -1;
            for (String value : values) {
                i++;
                if (i == 0) {
                    List<Integer> list = StringUtil.explodeToIntList(value);
                    this.outPais = ZzMjHelper.toMajiang(list);
                } else {
                    ZzMjCardDisType type = new ZzMjCardDisType();
                    type.init(value);
                    cardTypes.add(type);
                    List<ZzMj> majiangs = ZzMjHelper.toMajiang(type.getCardIds());
                    if (type.getAction() == ZzMjDisAction.action_angang) {
                        aGang.addAll(majiangs);
                    } else if (type.getAction() == ZzMjDisAction.action_minggang) {
                        mGang.addAll(majiangs);
                    } else if (type.getAction() == ZzMjDisAction.action_peng) {
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
        for (ZzMjCardDisType huxi : cardTypes) {
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
    public List<Integer> checkDisMajiang(ZzMj majiang, boolean canCheckHu) {
        List<Integer> list = new ArrayList<>();
        int[] arr = new int[6];
        // 转转麻将别人出牌可以胡
        ZzMjTable table = getPlayingTable(ZzMjTable.class);
        if (canCheckHu && passMajiangVal != majiang.getVal()) {
            // 没有出现漏炮的情况
            List<ZzMj> copy = new ArrayList<>(handPais);
            copy.add(majiang);
          
            boolean hu = ZzMjTool.isHu(copy, table.isHu7dui());
            if (hu) {
                arr[ZzMjConstants.ACTION_INDEX_HU] = 1;
            }
        }
        int count = ZzMjHelper.getMajiangCount(handPais, majiang.getVal());
        if (count >= 2) {
            // 除了红中外必须要3张以上的牌
            //if (getExceptHzMajiangCount() >= 3) {
            arr[ZzMjConstants.ACTION_INDEX_PENG] = 1;// 可以碰
            //}
        }
        
        
        if (table.calcNextSeat(table.getDisCardSeat()) == seat) {
        	//&& table.getBuChi()==1
	        if(table.getKeChi()==1) {
	        	List<ZzMj> chi = ZzMjTool.checkChi(handPais, majiang);
				if (!chi.isEmpty()) {
					 arr[ZzMjConstants.ACTION_INDEX_CHI] = 1;
					//actData.addChi();
				}
	        }
		}
        
        if (count == 3&&!passGangValList.contains(majiang.getVal())) {
            arr[ZzMjConstants.ACTION_INDEX_MINGGANG] = 1;// 可以杠
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

    public ZzMj getLastMoMajiang() {
        if (handPais.isEmpty()) {
            return null;

        } else {
            return handPais.get(handPais.size() - 1);

        }
    }

    /**
     * 自己出牌可做的操作
     *
     * @return 0胡 1碰 2明刚 3暗杠
     */
    public List<Integer> checkMo() {
        List<Integer> list = new ArrayList<>();
        int[] arr = new int[6];
        ZzMjTable table = getPlayingTable(ZzMjTable.class);
        if (ZzMjTool.isHu(handPais, table.isHu7dui())) {
            arr[ZzMjConstants.ACTION_INDEX_ZIMO] = 1;
        }
        if (isAlreadyMoMajiang()) {
            Map<Integer, Integer> pengMap = ZzMjHelper.toMajiangValMap(peng);
            for (ZzMj handMajiang : handPais) {
                if (pengMap.containsKey(handMajiang.getVal())&&!passGangValList.contains(handMajiang.getVal())) {
                    // 有碰过
                    arr[ZzMjConstants.ACTION_INDEX_MINGGANG] = 1;// 可以杠
                    break;
                }
            }
            List<ZzMj> copy = new ArrayList<>(handPais);
            Map<Integer, Integer> handMap = ZzMjHelper.toMajiangValMap(copy);
            if (handMap.containsValue(4)) {
                arr[ZzMjConstants.ACTION_INDEX_ANGANG] = 1;// 可以暗杠
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
     * 自己出牌可做的操作
     * @return 0胡 1碰 2明刚 3暗杠
     */
    public List<Integer> checkAnGang() {
        List<Integer> list = new ArrayList<>();
        int[] arr = new int[6];
        List<ZzMj> copy = new ArrayList<>(handPais);
        Map<Integer, Integer> handMap = ZzMjHelper.toMajiangValMap(copy);
        if (handMap.containsValue(4)) {
            arr[ZzMjConstants.ACTION_INDEX_ANGANG] = 1;// 可以暗杠
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
     * @param index 0点炮1点杠2明杠3暗杠4被碰5被杠6胡7自摸
     * @param val
     */
    public void changeAction(int index, int val) {
        actionArr[index] += val;
        actionTotalArr[index] += val;
        getPlayingTable().changeExtend();
    }

    public List<ZzMj> getPeng() {
        return peng;
    }

    public List<ZzMj> getaGang() {
        return aGang;
    }

    public List<ZzMj> getmGang() {
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
    public boolean isPassGang(ZzMj majiang) {
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

    public static final List<Integer> wanfaList = Arrays.asList(GameUtil.game_type_zzmj);

    public static void loadWanfaPlayers(Class<? extends Player> cls) {
        for (Integer integer : wanfaList) {
            PlayerManager.wanfaPlayerTypesPut(integer, cls, ZzMjCommandProcessor.getInstance());
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
        ZzMjTable table = getPlayingTable(ZzMjTable.class);
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
            //ZzMjConstants.AUTO_TIMEOUT + ZzMjConstants.AUTO_CHECK_TIMEOUT
            if (timeOut >=table.getIsAutoPlay()) {
                //进入托管状态
                auto = true;
                setAutoPlay(true, false);
            } else if (timeOut >= ZzMjConstants.AUTO_CHECK_TIMEOUT) {
                if (sendAutoTime == 0) {
                    sendAutoTime = now;
                    setLastCheckTime(now - ZzMjConstants.AUTO_CHECK_TIMEOUT * 1000);
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
                    if (timeOut >= ZzMjConstants.AUTO_HU_TIME) {
                        setAutoPlayTime(0);
                        return true;
                    }
                } else {
                    if (timeOut >= ZzMjConstants.AUTO_PLAY_TIME) {
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
        ZzMjTable table = getPlayingTable(ZzMjTable.class);
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
            } else if (timeOut >= ZzMjConstants.AUTO_CHECK_TIMEOUT) {
                if (sendAutoTime == 0) {
                    sendAutoTime = now;
                    setLastCheckTime(now - ZzMjConstants.AUTO_CHECK_TIMEOUT * 1000);
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

    public void setAutoPlay(boolean autoPlay, boolean isSelf) {
        ZzMjTable table = getPlayingTable(ZzMjTable.class);
        if (this.autoPlay != autoPlay || autoPlaySelf != isSelf) {
            ComMsg.ComRes.Builder res = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_tuoguan, seat, autoPlay ? 1 : 0, 0, isSelf ? 1 : 0);
            GeneratedMessage msg = res.build();
            if (table != null) {
                table.broadMsgToAll(msg);
            }
        	if (!getHandPais().isEmpty()) {
            table.addPlayLog(table.getDisCardRound() + "_" +getSeat() + "_" + ZzMjConstants.action_tuoguan + "_" +(autoPlay?1:0)+ getExtraPlayLog());
        	}

            StringBuilder sb = new StringBuilder("ZzMj");
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

    public long getLastCheckTime() {
        return lastCheckTime;
    }

    public void setLastCheckTime(long lastCheckTime) {
        this.lastCheckTime = lastCheckTime;
        if (getPlayingTable() != null) {
            getPlayingTable().changeExtend();
        }
    }

    public int getHuNum() {
        return huNum;
    }

    public void addHuNum(int num) {
        this.huNum +=num ;
    }

    public int getDianPaoNum() {
        return dianPaoNum;
    }

    public void addDianPaoNum(int num) {
        this.dianPaoNum += num;
    }

    public int getGongGangNum() {
        return gongGangNum;
    }

    public void addGongGangNum(int num) {
        this.gongGangNum +=1 ;
    }

    public int getAnGangNum() {
        return anGangNum;
    }

    public void addAnGangNum(int num) {
        this.anGangNum += num;
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
     * 记录得分详情
     *
     * @param index 0胡牌分，1鸟分，2杠分，3庄闲
     * @param point
     */
    public void changePointArr(int index, int point) {
        if (pointArr.length > index) {
            pointArr[index] += point;
        }
    }

    public int sumPointArr(){
        int sum=0;
        for (int i = 0; i < pointArr.length; i++) {
            sum+=pointArr[i];
        }
        return sum;
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
