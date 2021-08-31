package com.sy599.game.qipai.zjmj.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import com.sy599.game.qipai.zjmj.command.ZjMjCommandProcessor;
import com.sy599.game.qipai.zjmj.constant.ZjMjConstants;
import com.sy599.game.qipai.zjmj.rule.ZjMj;
import com.sy599.game.qipai.zjmj.tool.ZjMjHelper;
import com.sy599.game.qipai.zjmj.tool.ZjMjQipaiTool;
import com.sy599.game.qipai.zjmj.tool.ZjMjTool;
import com.sy599.game.qipai.zjmj.tool.hulib.util.HuUtil;
import com.sy599.game.util.GameUtil;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.util.StringUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

public class ZjMjPlayer extends Player {
    // 座位id
    private int seat;
    // 状态
    private player_state state;// 1进入 2已准备 3正在玩 4已结束
   
    private boolean isTing;//是否听牌
    private boolean yaoGang;//是否已经摇过杠
    
    private List<Integer> gangCGangMjs = new ArrayList<>();
    
    private List<Integer> tingpai;
    private List<Integer> yaogangTingpai;
    /**
     * 牌局是否在线 1离线 2在线
     */
    private int isEntryTable;
    private List<ZjMj> handPais;
    private List<ZjMj> outPais;
    private List<ZjMj> peng;
    private List<ZjMj> aGang;
    private List<ZjMj> mGang;
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
    private List<ZjMjCardDisType> cardTypes;
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

    /**
     * [类型]=分值
     */
    private int[] pointArr = new int[4];

    public ZjMjPlayer() {
        handPais = new ArrayList<ZjMj>();
        outPais = new ArrayList<ZjMj>();
        peng = new ArrayList<>();
        aGang = new ArrayList<>();
        mGang = new ArrayList<>();
        passGangValList = new ArrayList<>();
        cardTypes = new ArrayList<>();
        huType = new ArrayList<>();
        yaogangTingpai = new ArrayList<>();
        tingpai = new ArrayList<>();
        autoPlaySelf = false;
        autoPlay = false;
        autoPlayTime = 0;
        checkAutoPlay = false;
        lastCheckTime = System.currentTimeMillis();
        piaoPoint = -1;
        isTing = false;
        yaoGang = false;
        gangCGangMjs  = new ArrayList<>();
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

    public void dealHandPais(List<ZjMj> pais) {
        this.handPais = pais;
        getPlayingTable().changeCards(seat);
    }

    public List<ZjMj> getHandMajiang() {
        return handPais;
    }

    public boolean haveHongzhong() {
        for (ZjMj majiang : handPais) {
            if (majiang.isHongzhong()) {
                return true;
            }
        }
        return false;
    }

    public List<Integer> getHandPais() {
        return ZjMjHelper.toMajiangIds(handPais);
    }

    public List<Integer> getOutPais() {
        return ZjMjHelper.toMajiangIds(outPais);
    }

    public List<ZjMj> getOutMajing() {
        return outPais;
    }

    public void moMajiang(ZjMj majiang) {
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
    public void addOutPais(List<ZjMj> cards, int action, int disSeat) {
        handPais.removeAll(cards);
        if (action == ZjMjDisAction.action_chupai) {
            outPais.addAll(cards);
        } else {
            if (action == ZjMjDisAction.action_peng) {
                peng.addAll(cards);
                // changeAction(0, 1);
            } else if (action == ZjMjDisAction.action_minggang || action == ZjMjDisAction.action_yaogang) {
                //myExtend.setMjFengshen(FirstmythConstants.firstmyth_index12, 1);
                mGang.addAll(cards);
                if (cards.size() != 1) {
                    //changeAction(1, 1);
                } else {
                    ZjMj pengMajiang = cards.get(0);
                    Iterator<ZjMj> iterator = peng.iterator();
                    while (iterator.hasNext()) {
                        ZjMj majiang = iterator.next();
                        if (majiang.getVal() == pengMajiang.getVal()) {
                            mGang.add(majiang);
                            iterator.remove();
                        }
                    }
                    //changeAction(2, 1);
                }
                changeAction(ZjMjConstants.ACTION_COUNT_INDEX_MINGGANG, 1);
            } else if (action == ZjMjDisAction.action_angang || action == ZjMjDisAction.action_yaoangang) {
                //myExtend.setMjFengshen(FirstmythConstants.firstmyth_index12, 1);
                aGang.addAll(cards);
                changeAction(ZjMjConstants.ACTION_COUNT_INDEX_ANGANG, 1);
            }
            getPlayingTable().changeExtend();
            addCardType(action, cards, disSeat, 0);
        }
        getPlayingTable().changeCards(seat);
    }

    public void qGangUpdateOutPais(ZjMj card) {
        Iterator<ZjMj> iterator = mGang.iterator();
        while (iterator.hasNext()) {
            ZjMj majiang = iterator.next();
            if (majiang.getVal() == card.getVal()) {
                peng.add(majiang);
                iterator.remove();
            }
        }
        for (ZjMjCardDisType disType : cardTypes) {
            if (disType.getAction() == ZjMjDisAction.action_minggang) {
                if (disType.isHasCardVal(card)) {
                    disType.setAction(ZjMjDisAction.action_peng);
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
    public void addCardType(int action, List<ZjMj> disCardList, int disSeat, int disStatus) {
        if (action != 0) {
            if (action == ZjMjDisAction.action_minggang && disCardList.size() == 1) {
                ZjMj majiang = disCardList.get(0);
                for (ZjMjCardDisType disType : cardTypes) {
                    if (disType.getAction() == ZjMjDisAction.action_peng) {
                        if (disType.isHasCardVal(majiang)) {
                            disType.setAction(ZjMjDisAction.action_minggang);
                            disType.addCardId(majiang.getId());
                            disType.setDisSeat(seat);
                            break;
                        }
                    }
                }
            } else {
                ZjMjCardDisType type = new ZjMjCardDisType();
                type.setAction(action);
                type.setCardIds(ZjMjQipaiTool.toMajiangIds(disCardList));
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

    public void removeOutPais(List<ZjMj> cards, int action) {
        boolean remove = outPais.removeAll(cards);
        if (remove) {
//			if (action == HzMjDisAction.action_peng) {
//				changeAction(4, 1);
//			} else if (action == HzMjDisAction.action_minggang) {
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
            list.addAll(ZjMjHelper.toMajiangIds(peng));
        }
        if (!mGang.isEmpty()) {
            list.addAll(ZjMjHelper.toMajiangIds(mGang));
        }
        if (!aGang.isEmpty()) {
            list.addAll(ZjMjHelper.toMajiangIds(aGang));
        }
        return list;
    }

    public List<PhzHuCards> buildDisCards(long lookUid) {
        return buildDisCards(lookUid, true);
    }

    public List<PhzHuCards> buildDisCards(long lookUid, boolean hide) {
        List<PhzHuCards> list = new ArrayList<>();
        for (ZjMjCardDisType type : cardTypes) {
            if (hide && lookUid != this.userId && type.getAction() == ZjMjDisAction.action_angang) {
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
        res.addAllAngangIds(ZjMjHelper.toMajiangIds(aGang));
        res.addAllMoldCards(buildDisCards(userId));
        List<ZjMj> gangList = getGang();
        // 是否杠过
        res.addExt(gangList.isEmpty() ? 0 : 1); //0
        ZjMjTable table = getPlayingTable(ZjMjTable.class);
        // 现在是否自己摸的牌
        res.addExt((isAlreadyMoMajiang()) ? 1 : 0); //1
        res.addExt(handPais != null ? handPais.size() : 0); //2
        res.addExt(piaoPoint);//3
        res.addExt(autoPlay ? 1 : 0);//4
        res.addExt(autoPlaySelf ? 1 : 0);//5
        res.addExt(isYaoGang() ? 1:0);
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

    public List<ZjMj> getGang() {
        List<ZjMj> gang = new ArrayList<>();
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
        getTingpai().clear();
        getYaogangTingpai().clear();
        changeState(null);
        yaoGang = false;
        isTing = false;
        actionArr = new int[6];
        actionTotalArr = new int[6];
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
        gangCGangMjs.clear();
        autoPlaySelf = false;
        autoPlay = false;
        lastCheckTime = System.currentTimeMillis();
        checkAutoPlay = false;
        setPiaoPoint(-1);
        saveBaseInfo();
        pointArr = new int[4];
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
        for (ZjMjCardDisType type : cardTypes) {
            list.add(type.buildMsg().build());
        }
        res.addAllMoldPais(list);
        res.addAllDahus(huType);

        res.addExt(piaoPoint);
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
        for (ZjMjCardDisType type : cardTypes) {
            list.add(type.buildMsg().build());
        }
        res.addAllMoldPais(list);
        res.addAllDahus(huType);

        res.addExt(piaoPoint);
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
        getTingpai().clear();
        getYaogangTingpai().clear();
        peng.clear();
        aGang.clear();
        mGang.clear();
        cardTypes.clear();
        isTing = false;
        yaoGang = false;
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
        ZjMjTable table = getPlayingTable(ZjMjTable.class);
        if(table.getKePiao()!=2){
        	  setPiaoPoint(-1);
        }
      
        pointArr = new int[4];
        gangCGangMjs.clear();
    }

    @Override
    public void initPais(String handPai, String outPai) {
        if (!StringUtils.isBlank(handPai)) {
            List<Integer> list = StringUtil.explodeToIntList(handPai);
            this.handPais = ZjMjHelper.toMajiang(list);
        }
        if (!StringUtils.isBlank(outPai)) {
            String[] values = outPai.split(";");
            int i = -1;
            for (String value : values) {
                i++;
                if (i == 0) {
                    List<Integer> list = StringUtil.explodeToIntList(value);
                    this.outPais = ZjMjHelper.toMajiang(list);
                } else {
                    ZjMjCardDisType type = new ZjMjCardDisType();
                    type.init(value);
                    cardTypes.add(type);
                    List<ZjMj> majiangs = ZjMjHelper.toMajiang(type.getCardIds());
                    if (type.getAction() == ZjMjDisAction.action_angang) {
                        aGang.addAll(majiangs);
                    } else if (type.getAction() == ZjMjDisAction.action_minggang) {
                        mGang.addAll(majiangs);
                    } else if (type.getAction() == ZjMjDisAction.action_peng) {
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
        for (ZjMjCardDisType huxi : cardTypes) {
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
    public List<Integer> checkDisMajiang(ZjMj majiang, boolean canCheckHu) {
      
    	if(majiang.isHongzhong()){
    		return Collections.emptyList();
    	}
    	List<Integer> list = new ArrayList<>();
        int[] arr = new int[7];
        // 转转麻将别人出牌可以胡
        if (canCheckHu && passMajiangVal != majiang.getVal()) {
            // 没有出现漏炮的情况
            List<ZjMj> copy = new ArrayList<>(handPais);
            copy.add(majiang);
            ZjMjTable table = getPlayingTable(ZjMjTable.class);
            boolean hu = ZjMjTool.isHu(copy, table,getPeng(),getGang());
            if (hu) {
                arr[ZjMjConstants.ACTION_INDEX_HU] = 1;
            }
        }
        int count = ZjMjHelper.getMajiangCount(handPais, majiang.getVal());
        if(!isYaoGang()){
            if (count >= 2) {
                // 除了红中外必须要3张以上的牌
                if (getExceptHzMajiangCount() >= 3) {
                    arr[ZjMjConstants.ACTION_INDEX_PENG] = 1;// 可以碰
                }
            }
        }
        if (count == 3) {
        	if(!isYaoGang()){
        		 arr[ZjMjConstants.ACTION_INDEX_MINGGANG] = 1;// 可以杠
        	}
            if(isCanYaoGang(majiang,false)){
           	  arr[ZjMjConstants.ACTION_INDEX_YAOGANG] = 1;// 可以摇杠
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

    public List<Integer> getTingInfo(List<ZjMj> cards) {
    	ZjMjTable table = getPlayingTable(ZjMjTable.class);
    	List<Integer> tingInfo = new ArrayList<>();
    	int hzCount = ZjMjTool.dropHongzhong(cards).size();
		int[] cardArr = HuUtil.toCardArray(cards);
		List<ZjMj> penggang = new ArrayList<>();
		penggang.addAll(getPeng());
		penggang.addAll(getGang());
		
		List<ZjMj> lackPaiList = ZjMjTool.getLackList(cardArr, hzCount, true,penggang,table.isJJhu());
		if (lackPaiList == null || lackPaiList.size() == 0) {
			return tingInfo;
		}
		if (lackPaiList.size() == 1 && null == lackPaiList.get(0)) {
			//听所有
			tingInfo.add(ZjMj.mj201.getVal());
		} else {
			for (ZjMj lackPai : lackPaiList) {
				tingInfo.add(lackPai.getVal());
			}
			
			if(table.isHzlz()){
				tingInfo.add(ZjMj.mj201.getVal());
			}
		}
		return tingInfo;
    }
    
    /**
     * 除了红中之外还有多少张牌
     *
     * @return
     */
    public int getExceptHzMajiangCount() {
        int count = 0;
        for (ZjMj majiang : handPais) {
            if (!majiang.isHongzhong()) {
                count++;
            }
        }
        return count;
    }

    public ZjMj getLastMoMajiang() {
        if (handPais.isEmpty()) {
            return null;

        } else {
            return handPais.get(handPais.size() - 1);

        }
    }
    
    private boolean isBbhu(){
    	if(handPais.isEmpty()){
    		return false;
    	}
    	for (ZjMj mj : handPais) {
			if(mj.isJiang()){
				return false;
			}
		}
    	return true;
    }

    
    /**
     * 自己出牌可做的操作
     *
     * @param majiang
     * @return 0胡 1碰 2明刚 3暗杠
     */
    public List<Integer> checkMo(ZjMj majiang) {
        List<Integer> list = new ArrayList<>();
        int[] arr = new int[7];
        ZjMjTable table = getPlayingTable(ZjMjTable.class);
        int hongZhongCount = ZjMjQipaiTool.getMajiangCount(getHandMajiang(), ZjMj.getHongZhongVal());
        
        if(hongZhongCount >=4 &&table.isSiBaHZ()) {
        	  arr[ZjMjConstants.ACTION_INDEX_ZIMO] = 1;
        }
        if(table.isBanBanHu() && isBbhu() && getHandMajiang().size() == 14) {
        	arr[ZjMjConstants.ACTION_INDEX_ZIMO] = 1;
        }
        if (ZjMjTool.isHu(handPais, table,getPeng(),getGang())) {
            arr[ZjMjConstants.ACTION_INDEX_ZIMO] = 1;
        }
        if (isAlreadyMoMajiang()) {
            // if (majiang != null) {
            Map<Integer, Integer> pengMap = ZjMjHelper.toMajiangValMap(peng);
//            for (ZjMj handMajiang : handPais) {
//                if (pengMap.containsKey(handMajiang.getVal())) {
//                    // 有碰过
//                    arr[ZjMjConstants.ACTION_INDEX_MINGGANG] = 1;// 可以杠
//                    break;
//                }
//            }
            // 摸上来的麻将有碰过，则可以杠
            if (majiang != null && pengMap.containsKey(majiang.getVal())) {
				if(!isYaoGang()){
					arr[ZjMjConstants.ACTION_INDEX_MINGGANG] = 1;// 可以杠
				}  
			    if(isCanYaoGang(majiang,false)){
			 	   arr[ZjMjConstants.ACTION_INDEX_YAOGANG] = 1;// 可以暗杠
			    }
            }
            List<ZjMj> copy = new ArrayList<>(handPais);
            ZjMjTool.dropHongzhong(copy);//红中麻将需要做下判断
            Map<Integer, Integer> handMap = ZjMjHelper.toMajiangValMap(copy);
            if (handMap.containsValue(4)) {
            	if(!isYaoGang()){
            		arr[ZjMjConstants.ACTION_INDEX_ANGANG] = 1;// 可以暗杠
            	}
                for (Entry<Integer, Integer> entry : handMap.entrySet()) {
					if(entry.getValue() == 4){
						 if(isCanYaoGang(ZjMjHelper.getMajiangByVal(copy, entry.getKey()),true)){
		                	 arr[ZjMjConstants.ACTION_INDEX_YAOGANG] = 1;// 可以摇杠
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
     * 是否可以摇杠
     * @return
     */
    public boolean isCanYaoGang(ZjMj majiang,boolean isAg){
    	ZjMjTable table = getPlayingTable(ZjMjTable.class); 
    	if(table.getLeftMajiangCount() <= 0){
    		return false;
    	}
    	List<ZjMj> copyList = new ArrayList<>(handPais);
    	copyList = ZjMjHelper.dropVal(copyList, majiang.getVal());
    	List<Integer> tingList = getTingInfo(copyList);
     	if(tingList != null && tingList.size() > 0){
     		if(getYaogangTingpai().isEmpty()){
     			return true;
     		}
     		if(tingList.size() == getYaogangTingpai().size() && tingList.containsAll(getYaogangTingpai())){
     			return true;
     		}
     	}
    	return false;
    }
    /**
     * 是否可以摇杠
     * @return
     */
    public boolean isCanYaoGangold(ZjMj majiang,boolean isAg){
    	ZjMjTable table = getPlayingTable(ZjMjTable.class); 
    	if(table.getLeftMajiangCount() <= 0){
    		return false;
    	}
    	List<ZjMj> copyList = new ArrayList<>(handPais);
    	if (isAlreadyMoMajiang()) {
    		if(isAg){
    			Map<Integer, Integer> handMap = ZjMjHelper.toMajiangValMap(copyList);
    			for (Entry<Integer, Integer> entry : handMap.entrySet()) {
    				if(entry.getValue() == 4){
    					List<ZjMj>  ccopyList = ZjMjHelper.dropVal(copyList, entry.getKey());
    					List<Integer> tingList = getTingInfo(ccopyList);
    					if(tingList != null && tingList.size() == getYaogangTingpai().size() && tingList.containsAll(getYaogangTingpai())){
    						return true;
    					}
    				}
    			}
    		}
    	}else{
    		List<Integer> tingCards = getTingInfo(copyList);
    		if(!tingCards.isEmpty()){
    			if(getYaogangTingpai().isEmpty()){
    				return true;
    			}else{
    				//去掉这张牌，看看 听什么牌，如果不改听，则可以继续摇
    				copyList = ZjMjHelper.dropVal(copyList, majiang.getVal());
    				List<Integer> tingList = getTingInfo(copyList);
    				if(tingList != null && tingList.size() == getYaogangTingpai().size() && tingList.containsAll(getYaogangTingpai())){
    					return true;
    				}
    			}
    		}
    	}
    	
    	return false;
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

    public List<ZjMj> getPeng() {
        return peng;
    }

    public List<ZjMj> getaGang() {
        return aGang;
    }

    public List<ZjMj> getmGang() {
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
    public boolean isPassGang(ZjMj majiang) {
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
            GameUtil.game_type_zjmj);

    public static void loadWanfaPlayers(Class<? extends Player> cls) {
        for (Integer integer : wanfaList) {
            PlayerManager.wanfaPlayerTypesPut(integer, cls, ZjMjCommandProcessor.getInstance());
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
        ZjMjTable table = getPlayingTable(ZjMjTable.class);
        long now = System.currentTimeMillis();
        boolean auto = isAutoPlay();
        if (!auto && table.getIsAutoPlay() > 0) {
            //检查玩家是否进入系统托管状态
        	 if (!checkAutoPlay && table.getTableStatus()!= ZjMjConstants.TABLE_STATUS_PIAO) {
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
                    if (timeOut >= ZjMjConstants.AUTO_READY_TIME) {
                        setAutoPlayTime(0);
                        return true;
                    }
                } else if (autoType == 2) {
                    if (timeOut >= ZjMjConstants.AUTO_HU_TIME) {
                        setAutoPlayTime(0);
                        return true;
                    }
                } else {
                    if (timeOut >= ZjMjConstants.AUTO_PLAY_TIME) {
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
        ZjMjTable table = getPlayingTable(ZjMjTable.class);
        if (this.autoPlay != autoPlay || autoPlaySelf != isSelf) {
            ComMsg.ComRes.Builder res = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_tuoguan, seat, autoPlay ? 1 : 0, 0, isSelf ? 1 : 0);
            GeneratedMessage msg = res.build();
            if (table != null) {
                table.broadMsgToAll(msg);
            }
            if(!getHandPais().isEmpty()){
            	table.addPlayLog(table.getDisCardRound() + "_" +getSeat() + "_" + ZjMjDisAction.action_tuoguan + "_" +(autoPlay?1:0)+ getExtraPlayLog());
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
    	 ZjMjTable table = getPlayingTable(ZjMjTable.class);
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

	public boolean isTing() {
		return isTing;
	}

	public void setTing(boolean isTing) {
		this.isTing = isTing;
	}

	public boolean isYaoGang() {
		return yaoGang;
	}

	public void setYaoGang(boolean isYaoGang) {
		this.yaoGang = isYaoGang;
	}

	public List<Integer> getTingpai() {
		return tingpai;
	}

	public void setTingpai(List<Integer> tingpai) {
		this.tingpai = tingpai;
	}

	public List<Integer> getYaogangTingpai() {
		return yaogangTingpai;
	}

	public void setYaogangTingpai(List<Integer> yaogangTingpai) {
		if(yaogangTingpai.isEmpty()){
			this.yaogangTingpai = yaogangTingpai;
		}
	}
    
}
