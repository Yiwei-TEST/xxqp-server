package com.sy599.game.qipai.cqxzmj.bean;

import java.util.*;

import com.sy599.game.qipai.cqxzmj.tool.*;
import com.sy599.game.qipai.cqxzmj.tool.huTool.HuTool;
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
import com.sy599.game.msg.serverPacket.TableMjResMsg.*;
import com.sy599.game.msg.serverPacket.TablePhzResMsg.PhzHuCards;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes;
import com.sy599.game.qipai.cqxzmj.command.MjCommandProcessor;
import com.sy599.game.qipai.cqxzmj.constant.CqxzMjConstants;
import com.sy599.game.qipai.cqxzmj.constant.CqxzMj;
import com.sy599.game.util.GameUtil;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.util.StringUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

public class CqxzMjPlayer extends Player {
    // 座位id
    private int seat;
    // 状态
    private player_state state;// 1进入 2已准备 3正在玩 4已结束
    /**
     * 牌局是否在线 1离线 2在线
     */
    private int isEntryTable;
    private List<CqxzMj> handPais;
    private List<Integer> outPais;
    private List<CqxzMj> peng;
    private List<CqxzMj> aGang;
    private List<CqxzMj> mGang;
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
    private int[] actionArr = new int[5];
    private int[] actionTotalArr = new int[5];
    private List<Integer> passMajiangVal;
    //过杠操作
    private List<Integer> passGangValList;
    /**
     * 出牌对应操作信息
     */
    private List<MjCardDisType> cardTypes;
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
    private int moNum=0;//摸排数
    private int anGangNum=0;//暗杠数
    private int mingGangNum=0;
    private int chaJiaoNum=0;


    private int piaoFen=-1;
    //过胡之后，直到自己的回合为止都不能胡。
    private int passHu=0;
    //可碰
    private boolean canPeng=true;
    //可杠牌
    private List<Integer> canGang=new ArrayList<>();
    //定缺花色 1条 2筒 3万
    private volatile int dingQue=0;
    //系统默认换张
    List<Integer> huanZhang=new ArrayList<>();
    //系统默认定缺
    private int systemDq=0;
    //计分板
    private CqxzMjBoard board=new CqxzMjBoard(seat);
    //胡牌顺序
    private int huIndex=0;

    public CqxzMjPlayer() {
        handPais = new ArrayList<CqxzMj>();
        outPais = new ArrayList<Integer>();
        peng = new ArrayList<>();
        aGang = new ArrayList<>();
        mGang = new ArrayList<>();
        passGangValList = new ArrayList<>();
        passMajiangVal = new ArrayList<>();
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
        sb.append(autoPlay ? 1 : 0).append(",");
        sb.append(autoPlaySelf ? 1 : 0).append(",");
        sb.append(autoPlayTime).append(",");
        sb.append(lastCheckTime).append(",");
        sb.append(piaoFen).append(",");
        sb.append(ziMoNum).append(",");
        sb.append(jiePaoNum).append(",");
        sb.append(dianPaoNum).append(",");
        sb.append(canPeng?1:0).append(",");
        sb.append(StringUtil.implode(canGang, "_")).append(",");
        sb.append(anGangNum).append(",");
        sb.append(mingGangNum).append(",");
        sb.append(dingQue).append(",");
        sb.append(board.toStr()).append(",");
        sb.append(huIndex).append(",");
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
            this.autoPlay = StringUtil.getIntValue(values, i++) == 1;
            this.autoPlaySelf = StringUtil.getIntValue(values, i++) == 1;
            this.autoPlayTime = StringUtil.getLongValue(values, i++);
            this.lastCheckTime = StringUtil.getLongValue(values, i++);
            this.piaoFen = StringUtil.getIntValue(values, i++);
            this.ziMoNum = StringUtil.getIntValue(values, i++);
            this.jiePaoNum = StringUtil.getIntValue(values, i++);
            this.dianPaoNum = StringUtil.getIntValue(values, i++);
            this.canPeng = StringUtil.getIntValue(values, i++)==1?true:false;
            String virtualGangStr = StringUtil.getValue(values, i++);
            if (!StringUtils.isBlank(virtualGangStr)) {
                this.canGang = StringUtil.explodeToIntList(virtualGangStr, "_");
            }
            this.anGangNum = StringUtil.getIntValue(values, i++);
            this.mingGangNum = StringUtil.getIntValue(values, i++);
            this.dingQue = StringUtil.getIntValue(values, i++);
            String boardStr = StringUtil.getValue(values, i++);
            board.init(boardStr);
            huIndex = StringUtil.getIntValue(values, i++);
        }
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





    public void setPassHu(int passHu) {
        this.passHu = passHu;
    }

    public int getPassHu() {
        return passHu;
    }

    public boolean isCanPeng() {
        return canPeng;
    }

    public void setCanPeng(boolean canPeng) {
        this.canPeng = canPeng;
    }

    public CqxzMjBoard getBoard() {
        return board;
    }

    public void addPointAct(int actId,int act, CqxzMjPlayer player, int point){
        board.addActOperator(actId,act,player.getUserId(),player.getSeat(),point);
        changeTbaleInfo();
        //每次计分板改动，更新前端数据
        writeSocket(board.getBuild());
    }

    public void addPointAct(int act, CqxzMjPlayer player, int point){
        addPointAct(0,act,player,point);
    }

    public void dealHandPais(List<CqxzMj> pais) {
        this.handPais = pais;
        getPlayingTable().changeCards(seat);
    }

    public List<CqxzMj> getHandMajiang() {
        return handPais;
    }

    public boolean haveHongzhong() {
        for (CqxzMj majiang : handPais) {
            if (majiang.isHongzhong()) {
                return true;
            }
        }
        return false;
    }

    public List<Integer> getHandPais() {
        return MjHelper.toMajiangIds(handPais);
    }

    public List<Integer> getHandPaisWhithOutMo() {
        List<Integer> ids = new ArrayList<>(getHandPais());
        if(ids.size()%3==2&&huType.size()>0)
            ids.remove(ids.size()-1);
        return ids;
    }

    public List<Integer> getOutPais() {
        return outPais;
    }

    public List<Integer> getAngangZero() {
        List<Integer> zero=new ArrayList<>(aGang.size());
        for (CqxzMj mj:aGang) {
            zero.add(0);
        }
        return zero;
    }

    public List<Integer> getOutPaisNoZero() {
        List<Integer> noZero=new ArrayList<>();
        for (Integer id:outPais) {
            if(id!=0)
                noZero.add(id);
        }
        return noZero;
    }

    public void moMajiang(CqxzMj majiang) {
        passMajiangVal.clear();
        handPais.add(majiang);
        getPlayingTable().changeCards(seat);
    }

    /**
     * 增加玩家明面上的牌
     *
     * @param action
     * @param disSeat 碰或杠的出牌人
     */
    public void addOutPais(List<CqxzMj> cards, int action, int disSeat) {
        handPais.removeAll(cards);
        if (action == MjDisAction.action_chupai) {
            outPais.addAll(MjHelper.toMajiangIds(cards));
        } else {
            if (action == MjDisAction.action_peng) {
                peng.addAll(cards);
            } else if (action == MjDisAction.action_minggang) {
                mGang.addAll(cards);
                if (cards.size() != 1) {
                } else {
                    CqxzMj pengMajiang = cards.get(0);
                    Iterator<CqxzMj> iterator = peng.iterator();
                    while (iterator.hasNext()) {
                        CqxzMj majiang = iterator.next();
                        if (majiang.getVal() == pengMajiang.getVal()) {
                            mGang.add(majiang);
                            iterator.remove();
                        }
                    }
                }
                changeAction(CqxzMjConstants.ACTION_COUNT_INDEX_MINGGANG, 1);
            } else if (action == MjDisAction.action_angang) {
                aGang.addAll(cards);
                changeAction(CqxzMjConstants.ACTION_COUNT_INDEX_ANGANG, 1);
            }
            getPlayingTable().changeExtend();
            setPassHu(0);
            addCardType(action, cards, disSeat, 0);
        }
        getPlayingTable().changeCards(seat);
    }

    /**
     * 添加牌型，例如将碰变成明杠等
     *
     * @param action
     * @param disCardList
     */
    public void addCardType(int action, List<CqxzMj> disCardList, int disSeat, int disStatus) {
        if (action != 0) {
            if (action == MjDisAction.action_minggang && disCardList.size() == 1) {
                CqxzMj majiang = disCardList.get(0);
                for (MjCardDisType disType : cardTypes) {
                    if (disType.getAction() == MjDisAction.action_peng) {
                        if (disType.isHasCardVal(majiang)) {
                            disType.setAction(MjDisAction.action_minggang);
                            disType.addCardId(majiang.getId());
                            disType.setDisSeat(0);//用于前端显示，把出牌标记清除
                            break;
                        }
                    }
                }
            } else {
                MjCardDisType type = new MjCardDisType();
                type.setAction(action);
                type.setCardIds(MjQipaiTool.toMajiangIds(disCardList));
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

    public void removeOutPais(List<CqxzMj> cards) {
        boolean remove = outPais.removeAll(MjHelper.toMajiangIds(cards));
        if (remove) {
            getPlayingTable().changeCards(seat);
        }
    }

    public String toExtendStr() {
        StringBuffer sb = new StringBuffer();
        sb.append(StringUtil.implode(actionArr)).append("|");
        sb.append(StringUtil.implode(actionTotalArr)).append("|");
        sb.append(StringUtil.implode(passGangValList, ",")).append("|");
        sb.append(StringUtil.implode(huType, ",")).append("|");
        sb.append(StringUtil.implode(passMajiangVal, ",")).append("|");
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
            passMajiangVal = StringUtil.explodeToIntList(val8);
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
            list.addAll(MjHelper.toMajiangIds(peng));
        }
        if (!mGang.isEmpty()) {
            list.addAll(MjHelper.toMajiangIds(mGang));
        }
        if (!aGang.isEmpty()) {
            list.addAll(MjHelper.toMajiangIds(aGang));
        }
        return list;
    }



    public List<PhzHuCards> buildDisCards(long lookUid) {
        return buildDisCards(lookUid, true);
    }

    public List<PhzHuCards> buildDisCards(long lookUid, boolean hide) {
        List<PhzHuCards> list = new ArrayList<>();
        CqxzMjTable table = getPlayingTable(CqxzMjTable.class);
        for (MjCardDisType type : cardTypes) {
            if ((lookUid != this.userId && type.getAction() == MjDisAction.action_angang)) {
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
        CqxzMjTable table = getPlayingTable(CqxzMjTable.class);

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
        res.addAllAngangIds(MjHelper.toMajiangIds(aGang));
        res.addAllMoldCards(buildDisCards(userId));
        List<CqxzMj> gangList = getGang();
        // 是否杠过
        res.addExt(gangList.isEmpty() ? 0 : 1);    //0
        // 现在是否自己摸的牌
        res.addExt((isAlreadyMoMajiang()) ? 1 : 0);//1
        res.addExt(handPais != null ? handPais.size() : 0);//2
        res.addExt(autoPlay ? 1 : 0);              //3
        res.addExt(autoPlaySelf ? 1 : 0);          //4
        res.addExt(piaoFen);                       //5
        res.addExt(0);                             //6
        res.addExt(0);                             //7
        res.addExt(table.getSeatAndIds().containsKey(seat)?1:0);                   //8补花数量
        res.addExt(table.getFinishFapai()==3?dingQue:0);                       //9定缺
        if(table.getFinishFapai()==2&&huanZhang.size()==0)
            res.addExt(1);                                                     //10换张中
        else
            res.addExt(0);                                                     //10 不现实换张中
        res.addExt(huIndex);                                                   //11 胡
        if (!StringUtils.isBlank(getHeadimgurl())) {
            res.setIcon(getHeadimgurl());
        } else {
            res.setIcon("");
        }
        if (state == player_state.ready || state == player_state.play)
            // 玩家装备已经准备和正在玩的状态时通知前台已准备
            res.setStatus(SharedConstants.state_player_ready);
        else
            res.setStatus(0);

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

    public List<CqxzMj> getGang() {
        List<CqxzMj> gang = new ArrayList<>();
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

    public void setHuIndex(int huIndex) {
        this.huIndex = huIndex;
    }

    public int getHuIndex() {
        return huIndex;
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
        getOutPais().clear();
        changeState(null);
        actionArr = new int[5];
        actionTotalArr = new int[5];
        peng.clear();
        aGang.clear();
        mGang.clear();
        cardTypes.clear();
        huType.clear();
        passMajiangVal.clear();
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
        moNum=0;
        canPeng=true;
        passHu=0;
        anGangNum=0;
        mingGangNum=0;
        chaJiaoNum=0;
        huanZhang=new ArrayList<>();
        systemDq=0;
        dingQue=0;
        board.clear();
        huIndex=0;
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
        CqxzMjTable table = getPlayingTable(CqxzMjTable.class);
        res.addAllHandPais(getHandPaisWhithOutMo());
        List<PhzHuCards> list = new ArrayList<>();
        for (MjCardDisType type : cardTypes) {
            list.add(type.buildMsg().build());
        }
        res.addAllMoldPais(list);
        res.addAllDahus(huType);
        res.addExt(huIndex);
        res.addAllPointArr(Arrays.asList(ArrayUtils.toObject(board.getOverHuMsg(table))));
        res.addAllPointArr(Arrays.asList(ArrayUtils.toObject(board.getOverGangMsg())));
        res.addAllPointArr(Arrays.asList(ArrayUtils.toObject(board.getOverZYTS())));
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
        res.setTotalPoint(getTotalPoint());
        res.setSeat(seat);
        if (!StringUtils.isBlank(getHeadimgurl())) {
            res.setIcon(getHeadimgurl());
        } else {
            res.setIcon("");
        }
        res.setSex(sex);
        CqxzMjTable table = getPlayingTable(CqxzMjTable.class);
        res.addAllHandPais(getHandPaisWhithOutMo());
        List<PhzHuCards> list = new ArrayList<>();
        for (MjCardDisType type : cardTypes) {
            list.add(type.buildMsg().build());
        }
        res.addAllMoldPais(list);
        res.addAllDahus(huType);
        res.addExt(huIndex);
        res.addActionCount(ziMoNum);
        res.addActionCount(jiePaoNum);
        res.addActionCount(dianPaoNum);
        res.addActionCount(anGangNum);
        res.addActionCount(mingGangNum);
        res.addActionCount(chaJiaoNum);
        res.addAllPointArr(Arrays.asList(ArrayUtils.toObject(board.getOverHuMsg(table))));
        res.addAllPointArr(Arrays.asList(ArrayUtils.toObject(board.getOverGangMsg())));
        res.addAllPointArr(Arrays.asList(ArrayUtils.toObject(board.getOverZYTS())));
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
        getOutPais().clear();
        setPoint(0);
        passMajiangVal.clear();
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
        CqxzMjTable table = getPlayingTable(CqxzMjTable.class);
        if(table.getPiaoFenType()!=2)
            setPiaoFen(-1);
        moNum=0;
        canPeng=true;
        passHu=0;
        huanZhang=new ArrayList<>();
        systemDq=0;
        dingQue=0;
        mingGangNum+=board.getMingGangNum();
        chaJiaoNum+=board.haveChaJiao()?1:0;
        board.clear();
        huIndex=0;
    }

    @Override
    public void initPais(String handPai, String outPai) {
        if (!StringUtils.isBlank(handPai)) {
            List<Integer> list = StringUtil.explodeToIntList(handPai);
            this.handPais = MjHelper.toMajiang(list);
        }
        if (!StringUtils.isBlank(outPai)) {
            String[] values = outPai.split(";");
            int i = -1;
            for (String value : values) {
                i++;
                if (i == 0) {
                    List<Integer> list = StringUtil.explodeToIntList(value);
                    if(list!=null)
                        this.outPais = list;
                }else {
                    MjCardDisType type = new MjCardDisType();
                    type.init(value);
                    cardTypes.add(type);
                    List<CqxzMj> majiangs = MjHelper.toMajiang(type.getCardIds());
                    if (type.getAction() == MjDisAction.action_angang) {
                        aGang.addAll(majiangs);
                    } else if (type.getAction() == MjDisAction.action_minggang) {
                        mGang.addAll(majiangs);
                    } else if (type.getAction() == MjDisAction.action_peng) {
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
        sb.append(StringUtil.implode(getOutPais())).append(";");
        for (MjCardDisType huxi : cardTypes) {
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
    public List<Integer> checkDisMajiang(CqxzMj majiang, boolean canCheckHu) {
        List<Integer> list = new ArrayList<>();
        if(majiang.getVal()/10==dingQue)
            return list;
        int[] arr = new int[6];
        CqxzMjTable table = getPlayingTable(CqxzMjTable.class);
        // 转转麻将别人出牌可以胡
        if (passHu==0&&canCheckHu && !passMajiangVal.contains(majiang.getVal())) {
            // 没有出现漏炮的情况
            List<CqxzMj> copy = new ArrayList<>(handPais);
            copy.add(majiang);
            boolean hu = HuTool.isHu(MjHelper.toMajiangIds(copy),dingQue);
            if (hu) {
                arr[CqxzMjConstants.ACTION_INDEX_HU] = 1;
            }
        }
        int count = MjHelper.getMajiangCount(handPais, majiang.getVal());
        if (count >= 2&&isCanPeng()) {
            arr[CqxzMjConstants.ACTION_INDEX_PENG] = 1;// 可以碰
        }
        if (count == 3) {
            arr[CqxzMjConstants.ACTION_INDEX_MINGGANG] = 1;// 可以杠
            canGang.addAll(MjHelper.find(new ArrayList<>(handPais),majiang.getVal()));
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

    public CqxzMj getLastMoMajiang() {
        if (handPais.isEmpty()) {
            return null;
        } else {
            return handPais.get(handPais.size() - 1);
        }
    }

    /**
     * 自己出牌可做的操作
     * @return 0胡 1碰 2明刚 3暗杠
     */
    public List<Integer> checkMo() {
        List<Integer> list = new ArrayList<>();
        int[] arr = new int[6];
        CqxzMjTable table=getPlayingTable(CqxzMjTable.class);
        if (HuTool.isHu(MjHelper.toMajiangIds(handPais),dingQue)) {
            arr[CqxzMjConstants.ACTION_INDEX_HU] = 1;
        }
        if (isAlreadyMoMajiang()) {
            canGang.clear();
            Map<Integer, Integer> pengMap = MjHelper.toMajiangValMap(peng);
            for (CqxzMj handMajiang : handPais) {
                if (pengMap.containsKey(handMajiang.getVal())) {
                    // 有碰过
                    arr[CqxzMjConstants.ACTION_INDEX_MINGGANG] = 1;// 可以杠
                    canGang.add(handMajiang.getId());
                }
            }
            List<CqxzMj> copy = new ArrayList<>(handPais);
            Map<Integer, Integer> handMap = MjHelper.toMajiangValMap(copy);
            if (handMap.containsValue(4)) {
                arr[CqxzMjConstants.ACTION_INDEX_ANGANG] = 1;// 可以暗杠
                List<Integer> anGangVal=new ArrayList<>();
                for (Map.Entry<Integer,Integer> entry:handMap.entrySet()) {
                    if(entry.getValue()==4){
                        anGangVal.add(entry.getKey());
                    }
                }
                List<CqxzMj> mjs = MjHelper.find(MjHelper.toMajiangIds(copy), anGangVal);
                canGang.addAll(MjHelper.toMajiangIds(mjs));
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
        if(canGang.isEmpty())
            return;
        StringBuilder sb=new StringBuilder();
        for (Integer id:canGang) {
            if(sb.length()>0)
                sb.append(";");
            List<CqxzMj> mjs = MjHelper.getGangMajiangList(handPais, CqxzMj.getMajang(id).getVal());
            if(!mjs.isEmpty())
                sb.append(StringUtil.implode(MjHelper.toMajiangIds(mjs), ","));
            if(!MjHelper.toMajiangIds(handPais).contains(id)){
                if(sb.length()>0)
                    sb.append(",");
                sb.append(id);
            }
        }
        if(sb.length()>0)
            writeComMessage(WebSocketMsgType.res_code_cxmj_gangMsg, sb.toString());
    }


    public List<Integer> removeGangId(List<CqxzMj> mjs, int removeVal){
        List<Integer> ids=new ArrayList<>();
        for (CqxzMj mj:mjs) {
            if(mj.getVal()!=removeVal)
                ids.add(mj.getId());
        }
        return ids;
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

    public List<CqxzMj> getPeng() {
        return peng;
    }

    public List<CqxzMj> getaGang() {
        return aGang;
    }

    public List<CqxzMj> getmGang() {
        return mGang;
    }

    public void setPassGangValList(List<Integer> passGangValList) {
        this.passGangValList = passGangValList;
    }

    public void addAnGangNum() {
        this.anGangNum++;
    }

    /**
     * 漏炮
     *
     * @param passVal
     */
    public void addPassMajiangVal(int passVal) {
        if (!passMajiangVal.contains(passVal)) {
            passMajiangVal.add(passVal);
            changeTbaleInfo();
        }
    }

    /**
     * 可以碰可以杠的牌 选择了碰 再杠不算分
     *
     * @param majiang
     * @return
     */
    public boolean isPassGang(CqxzMj majiang) {
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

    public static final List<Integer> wanfaList = Arrays.asList(GameUtil.game_type_cqxzmj);

    public static void loadWanfaPlayers(Class<? extends Player> cls) {
        for (Integer integer : wanfaList) {
            PlayerManager.wanfaPlayerTypesPut(integer, cls, MjCommandProcessor.getInstance());
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
        CqxzMjTable table = getPlayingTable(CqxzMjTable.class);
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
            } else if (timeOut >= CqxzMjConstants.AUTO_CHECK_TIMEOUT) {
                if (sendAutoTime == 0) {
                    sendAutoTime = now;
                    setLastCheckTime(now - CqxzMjConstants.AUTO_CHECK_TIMEOUT * 1000);
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
                    if (timeOut >= CqxzMjConstants.AUTO_HU_TIME) {
                        setAutoPlayTime(0);
                        return true;
                    }
                } else {
                    if (timeOut >= CqxzMjConstants.AUTO_PLAY_TIME) {
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
        CqxzMjTable table = getPlayingTable(CqxzMjTable.class);
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
            } else if (timeOut >= CqxzMjConstants.AUTO_CHECK_TIMEOUT) {
                if (sendAutoTime == 0) {
                    sendAutoTime = now;
                    setLastCheckTime(now - CqxzMjConstants.AUTO_CHECK_TIMEOUT * 1000);
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
        CqxzMjTable table = getPlayingTable(CqxzMjTable.class);
        if (this.autoPlay != autoPlay || autoPlaySelf != isSelf) {
            ComMsg.ComRes.Builder res = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_tuoguan, seat, autoPlay ? 1 : 0, 0, isSelf ? 1 : 0);
            GeneratedMessage msg = res.build();
            if (table != null) {
                table.broadMsgToAll(msg);
            }
        }
        this.autoPlay = autoPlay;
        this.autoPlaySelf = autoPlay && isSelf;
        this.checkAutoPlay = autoPlay;
        setLastCheckTime(System.currentTimeMillis());
        if (table != null) {
            table.changeExtend(); 
        }
        LogUtil.msg("setAutoPlay|" + (table==null?null:table.getIsAutoPlay()) + "|" + getSeat() + "|" + getUserId() + "|" + (autoPlay ? 1 : 0) + "|" + (isSelf ? 1 : 0));
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

    /**
     * 玩家换张
     * @param huanNum
     * @param ids
     * @return
     */
    public boolean userHuangZhang(int huanNum,List<Integer> ids){
        synchronized (handPais){
            if(handPais.size()<=11||ids==null)
                return false;
            if(ids.size()!=huanNum)
                return false;
            int clas=0;
            for(Integer id:ids){
                int chu=CqxzMj.getMajang(id).getVal()/10;
                if(clas==0){
                    clas=chu;
                }else {
                    if(clas!=chu)
                        return false;
                }
            }
            for(Integer id:ids){
                handPais.remove(CqxzMj.getMajang(id));
            }
            CqxzMjTable table = getPlayingTable(CqxzMjTable.class);
            huanZhang=ids;
            table.getSeatAndIds().put(seat,ids);
            table.changeCards(seat);
            return true;
        }
    }

    /**
     * 系统换张
     * @param huanNum
     * @return
     */
    public boolean systemHuangZhang(int huanNum){
        synchronized (handPais){
            if(handPais.size()<=11)
                return false;
            List<Integer> ids = getLessMjs(huanNum);
            for (Integer id:ids){
                handPais.remove(CqxzMj.getMajang(id));
            }
            getPlayingTable().changeCards(seat);
            return true;
        }
    }



    public List<Integer> getLessMjs(int huanNum){
        if(huanZhang!=null&&huanZhang.size()==huanNum)
            return huanZhang;
        Map<Integer,List<CqxzMj>> clasAndMj=getClases();
        int clas=0;
        int clasNum=0;
        for(Map.Entry<Integer, List<CqxzMj>> entry:clasAndMj.entrySet()){
            int size = entry.getValue().size();
            if(size>=huanNum){
                if(clas==0){
                    clas=entry.getKey();
                    clasNum=size;
                } else {
                    if(size<clasNum){
                        clas=entry.getKey();
                        clasNum=size;
                    }
                }
            }
        }
        List<CqxzMj> aClasMjs = clasAndMj.get(clas);
        List<CqxzMj> huanMj = getLeastMj(aClasMjs, huanNum);
        huanZhang = MjHelper.toMajiangIds(huanMj);
        return huanZhang;
    }

    public List<CqxzMj> getLeastMj(List<CqxzMj> aClasMjs,int huangNum){
        List<CqxzMj> copy=new ArrayList<>(aClasMjs);
        List<CqxzMj> result=new ArrayList<>();
        for (int i = 1; i <=9; i++) {
            if(result.size()<huangNum)
                result.addAll(removeLeastVal(copy,i,huangNum-result.size()));
            else
                break;
        }
        return result;
    }

    public  List<CqxzMj> removeLeastVal(List<CqxzMj> mjs,int val,int remainNum){
        List<CqxzMj> removeMjs=new ArrayList<>();
        for(CqxzMj mj:mjs){
            if(mj.getVal()%10==val){
                if(removeMjs.size()<remainNum)
                    removeMjs.add(mj);
            }
        }
        return removeMjs;
    }

    public List<Integer> getHuanZhang() {
        return huanZhang;
    }


    /**
     * 可能存在多个相同最少牌数量的花色，需要同时推荐给前端，所以返回List
     * @return
     */
    public List<Integer> getLessClasList(){
        List<Integer> list=new ArrayList<>();
        Map<Integer,Integer> clasAndNum=getClasAndNum(getPlayingTable(CqxzMjTable.class).getMaxPlayerCount());
        int clas=0;
        int clasNum=0;
        for(Map.Entry<Integer, Integer> entry:clasAndNum.entrySet()){
            int size = entry.getValue();
            if(clas==0){
                clas=entry.getKey();
                clasNum=size;
            } else {
                if(size<clasNum){
                    clas=entry.getKey();
                    clasNum=size;
                }
            }
        }
        systemDq=clas;
        for (Map.Entry<Integer, Integer> entry:clasAndNum.entrySet()){
            if(entry.getValue()==clasNum)
                list.add(entry.getKey());
        }
        return list;
    }

    public int getLessClas(){
        if(systemDq!=0)
            return systemDq;
        else
            return getLessClasList().get(0);
    }

    private Map<Integer,List<CqxzMj>> getClases(){
        Map<Integer,List<CqxzMj>> clasAndMj=new HashMap<>();
        for (CqxzMj mj:handPais) {
            int chu=mj.getVal()/10;
            List<CqxzMj> clas = clasAndMj.getOrDefault(chu, new ArrayList<>());
            clas.add(mj);
            clasAndMj.putIfAbsent(chu,clas);
        }
        return clasAndMj;
    }

    private Map<Integer,Integer> getClasAndNum(int playerCount){
        Map<Integer,Integer> clasAndNum=new HashMap<>();
        if(playerCount==2){
            for (int i = 1; i <= 2; i++) {
                clasAndNum.put(i,0);
            }
            for (CqxzMj mj:handPais) {
                int chu=mj.getVal()/10;
                if(chu!=3)
                    clasAndNum.put(chu,clasAndNum.get(chu)+1);
            }
        }else {
            for (int i = 1; i <= 3; i++) {
                clasAndNum.put(i,0);
            }
            for (CqxzMj mj:handPais) {
                int chu=mj.getVal()/10;
                clasAndNum.put(chu,clasAndNum.get(chu)+1);
            }
        }

        return clasAndNum;
    }












    public int getDianPaoNum() {
        return dianPaoNum;
    }

    public int getMoNum() {
        return moNum;
    }

    public void setMoNum(int moNum) {
        this.moNum = moNum;
    }

    public List<MjCardDisType> getCardTypes() {
        return cardTypes;
    }

    public void addDianPaoNum(int num) {
        this.dianPaoNum += num;
    }

    public void addJiePaoNum(int num) {
        this.jiePaoNum += num;
    }

    public void addZiMoNum() {
        this.ziMoNum++;
    }

    public int getDingQue() {
        return dingQue;
    }

    public synchronized void setDingQue(int dingQue) {
        this.dingQue = dingQue;
        changeTbaleInfo();
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
     * 回放日志的额外信息
     *
     * @return
     */
    public String getExtraPlayLog() {
        return "_" + (isAutoPlay() ? 1 : 0) + "," + (isAutoPlaySelf() ? 1 : 0);
    }
}
