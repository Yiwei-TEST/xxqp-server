package com.sy599.game.qipai.yzwdmj.bean;

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
import com.sy599.game.qipai.yzwdmj.command.YzwdmjCommandProcessor;
import com.sy599.game.qipai.yzwdmj.constant.YzwdmjConstants;
import com.sy599.game.qipai.yzwdmj.rule.Yzwdmj;
import com.sy599.game.qipai.yzwdmj.rule.YzwdmjMingTangRule;
import com.sy599.game.qipai.yzwdmj.tool.YzwdmjHelper;
import com.sy599.game.qipai.yzwdmj.tool.YzwdmjQipaiTool;
import com.sy599.game.qipai.yzwdmj.tool.YzwdmjTool;
import com.sy599.game.util.GameUtil;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.util.StringUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

public class YzwdmjPlayer extends Player {
    // 座位id
    private int seat;
    // 状态
    private player_state state;// 1进入 2已准备 3正在玩 4已结束
    /**
     * 牌局是否在线 1离线 2在线
     */
    private int isEntryTable;
    private List<Yzwdmj> handPais;
    private List<Yzwdmj> outPais;
    private List<Yzwdmj> peng;
    private List<Yzwdmj> aGang;
    private List<Yzwdmj> mGang;
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
    private List<YzwdmjCardDisType> cardTypes;
    //胡牌类型
    private List<Integer> huType;

    private volatile boolean autoPlay = false;//托管
    private volatile boolean autoPlaySelf = false;//托管
    private volatile long lastCheckTime = 0;//最后检查时间
    private volatile long autoPlayTime = 0;//自动操作时间
    private volatile boolean checkAutoPlay = false; //是否是牌桌上的焦点
    private volatile long sendAutoTime = 0;//发送倒计时间
    private int ziMoNum=0;//胡牌次数
    private int qiangHuNum=0;//点炮次数
    private int gongGangNum=0;//公杠次数
    private int anGangNum=0;//暗杠次数
    private int isJiePao=0;//是否已经接炮
    /**
     * [类型]=分值 0码分，1杠分，2飘分,3跟庄分
     */
    private int[] pointArr = new int[4];
    private int piaoFen=-1;
    //过王闯王钓
    private int passWang=0;
    //是否可胡王闯王钓
    private List<Integer> wangAct=new ArrayList<>();
    //输赢飘分
    private int winLostPiaoFen=0;
    public String toInfoStr() {
        StringBuffer sb = new StringBuffer();
        sb.append(getUserId()).append(",");//1
        sb.append(seat).append(",");//2
        int stateVal = 0;
        if (state != null) {
            stateVal = state.getId();
        }
        sb.append(stateVal).append(",");//3
        sb.append(isEntryTable).append(",");//4
        sb.append(winCount).append(",");//5
        sb.append(lostCount).append(",");//6
        sb.append(point).append(",");//7
        sb.append(getTotalPoint()).append(",");//8
        sb.append(lostPoint).append(",");//9
        sb.append(passMajiangVal).append(",");//10
        sb.append(autoPlay ? 1 : 0).append(",");//11
        sb.append(autoPlaySelf ? 1 : 0).append(",");//12
        sb.append(autoPlayTime).append(",");//13
        sb.append(lastCheckTime).append(",");//14
        sb.append(piaoFen).append(",");//15
        sb.append(passWang).append(",");//16
        sb.append(StringUtil.implode(pointArr, "_")).append(",");//17
        sb.append(StringUtil.implode(wangAct, "_")).append(",");//18
        return sb.toString();
    }

    @Override
    public void initPlayInfo(String data) {
        if (!StringUtils.isBlank(data)) {
            int i = 0;
            String[] values = data.split(",");
            long duserId = StringUtil.getLongValue(values, i++);//1
            if (duserId != getUserId()) {
                return;
            }
            this.seat = StringUtil.getIntValue(values, i++);//2
            int stateVal = StringUtil.getIntValue(values, i++);//3
            this.state = SharedConstants.getPlayerState(stateVal);

            this.isEntryTable = StringUtil.getIntValue(values, i++);//4
            this.winCount = StringUtil.getIntValue(values, i++);//5
            this.lostCount = StringUtil.getIntValue(values, i++);//6
            this.point = StringUtil.getIntValue(values, i++);//7
            setTotalPoint(StringUtil.getIntValue(values, i++));//8
            this.lostPoint = StringUtil.getIntValue(values, i++);//9
            this.passMajiangVal = StringUtil.getIntValue(values, i++);//10
            this.autoPlay = StringUtil.getIntValue(values, i++) == 1;//11
            this.autoPlaySelf = StringUtil.getIntValue(values, i++) == 1;//12
            this.autoPlayTime = StringUtil.getLongValue(values, i++);//13
            this.lastCheckTime = StringUtil.getLongValue(values, i++);//14
            this.piaoFen = StringUtil.getIntValue(values, i++);//15
            this.passWang = StringUtil.getIntValue(values, i++);//16
            String pointArr = StringUtil.getValue(values, i++);//17
            if (!StringUtils.isBlank(pointArr)) {
                this.pointArr=StringUtil.explodeToIntArray(pointArr,"_");
            }
            String wangAct = StringUtil.getValue(values, i++);//18
            if (!StringUtils.isBlank(wangAct)) {
                this.wangAct=StringUtil.explodeToIntList(wangAct,"_");
            }
        }
    }








    public YzwdmjPlayer() {
        handPais = new ArrayList<Yzwdmj>();
        outPais = new ArrayList<Yzwdmj>();
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

    public void dealHandPais(List<Yzwdmj> pais) {
        this.handPais = pais;
        getPlayingTable().changeCards(seat);
    }

    public int getPiaoFen() {
        return piaoFen;
    }

    public void setPiaoFen(int piaoFen) {
        this.piaoFen = piaoFen;
    }

    public int getWinLostPiaoFen() {
        return winLostPiaoFen;
    }

    public void setWinLostPiaoFen(int winLostPiaoFen) {
        this.winLostPiaoFen = winLostPiaoFen;
    }

    public List<Yzwdmj> getHandMajiang() {
        return handPais;
    }

    public boolean haveHongzhong() {
        for (Yzwdmj majiang : handPais) {
            if (majiang.isHongzhong()) {
                return true;
            }
        }
        return false;
    }

    public List<Integer> getHandPais() {
        return YzwdmjHelper.toMajiangIds(handPais);
    }

    public List<Integer> getOutPais() {
        return YzwdmjHelper.toMajiangIds(outPais);
    }

    public List<Yzwdmj> getOutMajing() {
        return outPais;
    }

    public void moMajiang(Yzwdmj majiang) {
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
    public void addOutPais(List<Yzwdmj> cards, int action, int disSeat) {
        handPais.removeAll(cards);
        if (action == YzwdmjDisAction.action_chupai) {
            outPais.addAll(cards);
        } else {
            if (action == YzwdmjDisAction.action_peng) {
                peng.addAll(cards);
            } else if (action == YzwdmjDisAction.action_minggang) {
                mGang.addAll(cards);
                if (cards.size() != 1) {
                } else {
                    Yzwdmj pengMajiang = cards.get(0);
                    Iterator<Yzwdmj> iterator = peng.iterator();
                    while (iterator.hasNext()) {
                        Yzwdmj majiang = iterator.next();
                        if (majiang.getVal() == pengMajiang.getVal()) {
                            mGang.add(majiang);
                            iterator.remove();
                        }
                    }
                }
                changeAction(YzwdmjConstants.ACTION_COUNT_INDEX_MINGGANG, 1);
            } else if (action == YzwdmjDisAction.action_angang) {
                aGang.addAll(cards);
                changeAction(YzwdmjConstants.ACTION_COUNT_INDEX_ANGANG, 1);
            }
            getPlayingTable().changeExtend();
            addCardType(action, cards, disSeat, 0);
        }
        getPlayingTable().changeCards(seat);
    }

    public void qGangUpdateOutPais(Yzwdmj card) {
        Iterator<Yzwdmj> iterator = mGang.iterator();
        while (iterator.hasNext()) {
            Yzwdmj majiang = iterator.next();
            if (majiang.getVal() == card.getVal()) {
                peng.add(majiang);
                iterator.remove();
            }
        }
        for (YzwdmjCardDisType disType : cardTypes) {
            if (disType.getAction() == YzwdmjDisAction.action_minggang) {
                if (disType.isHasCardVal(card)) {
                    disType.setAction(YzwdmjDisAction.action_peng);
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
    public void addCardType(int action, List<Yzwdmj> disCardList, int disSeat, int disStatus) {
        if (action != 0) {
            if (action == YzwdmjDisAction.action_minggang && disCardList.size() == 1) {
                Yzwdmj majiang = disCardList.get(0);
                for (YzwdmjCardDisType disType : cardTypes) {
                    if (disType.getAction() == YzwdmjDisAction.action_peng) {
                        if (disType.isHasCardVal(majiang)) {
                            disType.setAction(YzwdmjDisAction.action_minggang);
                            disType.addCardId(majiang.getId());
                            disType.setDisSeat(seat);
                            break;
                        }
                    }
                }
            } else {
                YzwdmjCardDisType type = new YzwdmjCardDisType();
                type.setAction(action);
                type.setCardIds(YzwdmjQipaiTool.toMajiangIds(disCardList));
                type.setDisSeat(disSeat);
                type.setDisStatus(disStatus);
                cardTypes.add(type);
            }

        }
    }

    public List<Integer> checkWang(boolean hu7dui){
        List<Yzwdmj> handCards=new ArrayList<>(getHandMajiang());
        int []act=new int[8];
        List<Integer> mt = new ArrayList<>();
        YzwdmjMingTangRule.checkWang(handCards,null,mt,hu7dui);
        if(mt.contains(YzwdmjMingTangRule.LOUDI_MINGTANG_WAMGCHUANG)){
            act[6]=1;
        }
        if(mt.contains(YzwdmjMingTangRule.LOUDI_MINGTANG_WAMGDIAO)){
            act[7]=1;
        }
        List<Integer> list=new ArrayList<>();
        for (int val : act) {
            list.add(val);
        }
        setWangAct(list);
        return list;
    }


    /**
     * 已经摸过牌了
     *
     * @return
     */
    public boolean isAlreadyMoMajiang() {
        return !handPais.isEmpty() && handPais.size() % 3 == 2;
    }

    public void removeOutPais(List<Yzwdmj> cards, int action) {
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
            list.addAll(YzwdmjHelper.toMajiangIds(peng));
        }
        if (!mGang.isEmpty()) {
            list.addAll(YzwdmjHelper.toMajiangIds(mGang));
        }
        if (!aGang.isEmpty()) {
            list.addAll(YzwdmjHelper.toMajiangIds(aGang));
        }
        return list;
    }

    public List<PhzHuCards> buildDisCards(long lookUid) {
        return buildDisCards(lookUid, true);
    }

    public List<PhzHuCards> buildDisCards(long lookUid, boolean hide) {
        List<PhzHuCards> list = new ArrayList<>();
        for (YzwdmjCardDisType type : cardTypes) {
            if (hide && lookUid != this.userId && type.getAction() == YzwdmjDisAction.action_angang) {
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
        res.addAllAngangIds(YzwdmjHelper.toMajiangIds(aGang));
        res.addAllMoldCards(buildDisCards(userId));
        List<Yzwdmj> gangList = getGang();
        // 是否杠过
        YzwdmjTable table = getPlayingTable(YzwdmjTable.class);
        // 现在是否自己摸的牌
        res.addExt(gangList.isEmpty() ? 0 : 1);
        res.addExt((isAlreadyMoMajiang()) ? 1 : 0);
        res.addExt(handPais != null ? handPais.size() : 0);
        res.addExt(autoPlay ? 1 : 0);
        res.addExt(autoPlaySelf ? 1 : 0);
        res.addExt(piaoFen);//5
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

    public List<Yzwdmj> getGang() {
        List<Yzwdmj> gang = new ArrayList<>();
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

    public int getPassWang() {
        return passWang;
    }

    public void setPassWang(int passWang) {
        this.passWang = passWang;
    }
    public void passWang() {
        passWang = 0;
        wangAct.clear();
        changeExtend();
    }

    public List<Integer> getWangAct() {
        return wangAct;
    }

    public void removeWangAct(Integer action){
        if(action==YzwdmjDisAction.action_wangchuang){
            wangAct.set(7,0);
        }else if(action==YzwdmjDisAction.action_wangdiao){
            wangAct.set(6,0);
        }
        changeExtend();
    }

    public void setWangAct(List<Integer> wangAct) {
        this.wangAct = wangAct;
        changeExtend();
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
        ziMoNum=0;//自摸次数
        qiangHuNum=0;//抢杠胡次数
        gongGangNum=0;//公杠次数
        anGangNum=0;//暗杠次数
        pointArr = new int[4];
        saveBaseInfo();
        wangAct.clear();
        passWang=0;
        piaoFen=-1;
        setWinLostPiaoFen(0);
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
        for (YzwdmjCardDisType type : cardTypes) {
            list.add(type.buildMsg().build());
        }
        res.addAllMoldPais(list);
        res.addAllDahus(huType);
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
        for (YzwdmjCardDisType type : cardTypes) {
            list.add(type.buildMsg().build());
        }
        res.addAllMoldPais(list);
        res.addAllDahus(huType);
        res.setActionCount(0,ziMoNum);
        res.setActionCount(1,qiangHuNum);
        res.setActionCount(2,anGangNum);
        res.setActionCount(3,gongGangNum);
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
        wangAct.clear();
        passWang=0;
        YzwdmjTable table = getPlayingTable(YzwdmjTable.class);
        if(table.getPiaoFenType()!=5)
            piaoFen=-1;
        setWinLostPiaoFen(0);
    }

    @Override
    public void initPais(String handPai, String outPai) {
        if (!StringUtils.isBlank(handPai)) {
            List<Integer> list = StringUtil.explodeToIntList(handPai);
            this.handPais = YzwdmjHelper.toMajiang(list);
        }
        if (!StringUtils.isBlank(outPai)) {
            String[] values = outPai.split(";");
            int i = -1;
            for (String value : values) {
                i++;
                if (i == 0) {
                    List<Integer> list = StringUtil.explodeToIntList(value);
                    this.outPais = YzwdmjHelper.toMajiang(list);
                } else {
                    YzwdmjCardDisType type = new YzwdmjCardDisType();
                    type.init(value);
                    cardTypes.add(type);
                    List<Yzwdmj> majiangs = YzwdmjHelper.toMajiang(type.getCardIds());
                    if (type.getAction() == YzwdmjDisAction.action_angang) {
                        aGang.addAll(majiangs);
                    } else if (type.getAction() == YzwdmjDisAction.action_minggang) {
                        mGang.addAll(majiangs);
                    } else if (type.getAction() == YzwdmjDisAction.action_peng) {
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
        for (YzwdmjCardDisType huxi : cardTypes) {
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
    public List<Integer> checkDisMajiang(Yzwdmj majiang, boolean canCheckHu) {
        List<Integer> list = new ArrayList<>();
        int[] arr = new int[6];
        // 转转麻将别人出牌可以胡
        if (canCheckHu && passMajiangVal != majiang.getVal()) {
            // 没有出现漏炮的情况
            List<Yzwdmj> copy = new ArrayList<>(handPais);
            copy.add(majiang);
            YzwdmjTable table = getPlayingTable(YzwdmjTable.class);
            boolean hu = YzwdmjTool.isHu(copy, table.isHu7dui());
            if (hu) {
                arr[YzwdmjConstants.ACTION_INDEX_HU] = 1;
            }
        }
        int count = YzwdmjHelper.getMajiangCount(handPais, majiang.getVal());
        if (count >= 2) {
            // 除了红中外必须要3张以上的牌
            //if (getExceptHzMajiangCount() >= 3) {
            arr[YzwdmjConstants.ACTION_INDEX_PENG] = 1;// 可以碰
            //}
        }
        if (count == 3&&!passGangValList.contains(majiang.getVal())) {
            arr[YzwdmjConstants.ACTION_INDEX_MINGGANG] = 1;// 可以杠
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
     * 除了红中之外还有多少张牌
     *
     * @return
     */
    public int getExceptHzMajiangCount() {
        int count = 0;
        for (Yzwdmj majiang : handPais) {
            if (!majiang.isHongzhong()) {
                count++;
            }
        }
        return count;
    }

    public Yzwdmj getLastMoMajiang() {
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
    public List<Integer> checkMo(Yzwdmj majiang) {
        List<Integer> list = new ArrayList<>();
        int[] arr = new int[6];
        YzwdmjTable table = getPlayingTable(YzwdmjTable.class);
        if (YzwdmjTool.isHu(handPais, table.isHu7dui())) {
            arr[YzwdmjConstants.ACTION_INDEX_ZIMO] = 1;
        }
        if (isAlreadyMoMajiang()) {
            // if (majiang != null) {
            Map<Integer, Integer> pengMap = YzwdmjHelper.toMajiangValMap(peng);
            for (Yzwdmj handMajiang : handPais) {
                if (pengMap.containsKey(handMajiang.getVal())) {
                    // 有碰过
                    arr[YzwdmjConstants.ACTION_INDEX_MINGGANG] = 1;// 可以杠
                    break;
                }
            }
            List<Yzwdmj> copy = new ArrayList<>(handPais);
            //ZzMjTool.dropHongzhong(copy);红中麻将需要做下判断
            Map<Integer, Integer> handMap = YzwdmjHelper.toMajiangValMap(copy);
            if (handMap.containsValue(4)) {
                arr[YzwdmjConstants.ACTION_INDEX_ANGANG] = 1;// 可以暗杠
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
     * @param index 0点炮1点杠2明杠3暗杠4被碰5被杠6胡7自摸
     * @param val
     */
    public void changeAction(int index, int val) {
        actionArr[index] += val;
        actionTotalArr[index] += val;
        getPlayingTable().changeExtend();
    }

    public List<Yzwdmj> getPeng() {
        return peng;
    }

    public List<Yzwdmj> getaGang() {
        return aGang;
    }

    public List<Yzwdmj> getmGang() {
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
    public boolean isPassGang(Yzwdmj majiang) {
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

    public static final List<Integer> wanfaList = Arrays.asList(GameUtil.game_type_yzwdmj);

    public static void loadWanfaPlayers(Class<? extends Player> cls) {
        for (Integer integer : wanfaList) {
            PlayerManager.wanfaPlayerTypesPut(integer, cls, YzwdmjCommandProcessor.getInstance());
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
        YzwdmjTable table = getPlayingTable(YzwdmjTable.class);
        long now = System.currentTimeMillis();
        boolean auto = isAutoPlay();
        if (!auto && table.getIsAutoPlay() >= 1) {
            //检查玩家是否进入系统托管状态
            if (!checkAutoPlay) {
                if (isAlreadyMoMajiang() || table.getActionSeatMap().containsKey(seat)||wangAct.contains(1)) {
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
            } else if (timeOut >= YzwdmjConstants.AUTO_CHECK_TIMEOUT) {
                if (sendAutoTime == 0) {
                    sendAutoTime = now;
                    setLastCheckTime(now - YzwdmjConstants.AUTO_CHECK_TIMEOUT * 1000);
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
                    if (timeOut >= YzwdmjConstants.AUTO_HU_TIME) {
                        setAutoPlayTime(0);
                        return true;
                    }
                } else {
                    if (timeOut >= YzwdmjConstants.AUTO_PLAY_TIME) {
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
        YzwdmjTable table = getPlayingTable(YzwdmjTable.class);
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
            } else if (timeOut >= YzwdmjConstants.AUTO_CHECK_TIMEOUT) {
                if (sendAutoTime == 0) {
                    sendAutoTime = now;
                    setLastCheckTime(now - YzwdmjConstants.AUTO_CHECK_TIMEOUT * 1000);
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

    public void setAutoPlay(boolean autoPlay, boolean isSelf) {
        YzwdmjTable table = getPlayingTable(YzwdmjTable.class);
        if (this.autoPlay != autoPlay || autoPlaySelf != isSelf) {
            ComMsg.ComRes.Builder res = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_tuoguan, seat, autoPlay ? 1 : 0, 0, isSelf ? 1 : 0);
            GeneratedMessage msg = res.build();
            if (table != null) {
                table.broadMsgToAll(msg);
            }
        	if (!getHandPais().isEmpty()) {
            table.addPlayLog(table.getDisCardRound() + "_" +getSeat() + "_" + YzwdmjConstants.action_tuoguan + "_" +(autoPlay?1:0)+ getExtraPlayLog());
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

    public int getZiMoNum() {
        return ziMoNum;
    }

    public void addZiMoNum() {
        this.ziMoNum++;
    }

    public int getQiangHuNum() {
        return qiangHuNum;
    }

    public void addQiangHuNum() {
        this.qiangHuNum++;
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
     * @param index 0码分，1杠分，2飘分
     * @param point
     */
    public void changePointArr(int index, int point) {
        if (pointArr.length > index) {
            pointArr[index] += point;
        }
    }

    public int[] getPointArr() {
        return pointArr;
    }

    public void clearPointArr(){
        pointArr=new int[4];
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
