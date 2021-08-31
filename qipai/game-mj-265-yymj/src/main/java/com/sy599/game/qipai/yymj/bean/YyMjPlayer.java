package com.sy599.game.qipai.yymj.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.fastjson.annotation.JSONField;
import com.sy599.game.qipai.yymj.rule.Mj;
import com.sy599.game.qipai.yymj.rule.MjHelper;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
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
import com.sy599.game.qipai.yymj.command.MjCommandProcessor;
import com.sy599.game.qipai.yymj.constant.MjAction;
import com.sy599.game.qipai.yymj.constant.MjConstants;
import com.sy599.game.qipai.yymj.tool.MjEnumHelper;
import com.sy599.game.qipai.yymj.tool.MjQipaiTool;
import com.sy599.game.qipai.yymj.tool.MjTool;
import com.sy599.game.util.GameUtil;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.util.StringUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

@Data
public class YyMjPlayer extends Player {
    public static final List<Integer> wanfaList = Arrays.asList(GameUtil.play_type265_yymj);

    // 座位id
    private int seat;
    // 状态
    private player_state state;// 1进入 2已准备 3正在玩 4已结束
    private int isEntryTable;
    private List<Mj> handPais;
    private List<Mj> outPais;
    private List<Mj> peng;
    private List<Mj> aGang;
    private List<Mj> mGang;
    private List<Mj> chi;
    private List<Mj> buzhang;
    private List<Mj> buzhangAn;
//    //杠是哪个座位放的, 自己摸得与暗杠没有映射关系
//    //麻将id->明杠>1000|暗杠>1000|seat
    private Map<Integer, Integer> gangSeat = new HashMap<>();

    private int winLossPoint;

    private int winCount;
    private int lostCount;
    private int lostPoint;
    private int gangPoint;// 要胡牌杠才算分
    private int point;
    //明杠得分
    private int mGangScore;
    //暗杠得分
    private int aGangScore;
    //放杠得分
    private int fGangScore;
    //明杠得分,马跟杠
    private int mGangScoreHorseAndGang;
    //暗杠得分,马跟杠
    private int aGangScoreHorseAndGang;
    //放杠得分,马跟杠
    private int fGangScoreHorseAndGang;
    //马跟杠分
    private int horseAndGangScore;
    /**
     * 0点炮1杠2明杠3暗杠4被碰5被杠6胡7自摸
     **/
    private int[] actionArr = new int[9];
    /**
     * 0大胡自摸1小胡自摸2大胡点炮3小胡点炮4大胡接炮5小胡接炮
     **/
    private int[] actionTotalArr = new int[9];
    private List<Integer> huXiaohu;
    private List<Integer> dahu;
    private int passMajiangVal;
    private List<Integer> passGangValList;
    private List<MjCardDisType> cardTypes;
    /*** 过小胡的记录*/
    private List<Integer> passXiaoHuList;

    /*** 过小胡的记录，出牌后清空*/
    private List<Integer> passXiaoHuList2 = new ArrayList<>();
    /**
     * 小胡的牌
     */
    private List<Integer> huXiaohuCards = new ArrayList<>();
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

    //玩家选择报听 0初始化, 1选择报听, 2报听, 3非报听
    private int signTing;
    //报听第一次炮胡没有接炮，后面只能自摸
    private int signTingPao;
    //漏胡：玩家有胡点过后，过手后才能炮胡，需要自己摸牌才算过手
    private int takeCardFlag;

    //玩家是首次出牌
    private boolean firstDisCard;

    //开局王牌的数量 , 天胡
    private int kingCardNumHuFlag;
    //开局底牌的数量, 地胡
    private int floorCardNumHuFlag;
    //喜分
    private int xiScore;
    private int faScore;
    //报听状态是否自动出牌
    private boolean signCanAutoDisCard;

    //死胡, 已经胡不了牌了
    private boolean deathHu;

    //开局买的马
    private List<Integer> buyHorses = new ArrayList<>();

    public YyMjPlayer() {
        handPais = new ArrayList<Mj>();
        outPais = new ArrayList<Mj>();
        peng = new ArrayList<>();
        aGang = new ArrayList<>();
        mGang = new ArrayList<>();
        chi = new ArrayList<>();
        buzhang = new ArrayList<>();
        buzhangAn = new ArrayList<>();
        gangSeat = new HashMap<>();
        buyHorses = new ArrayList<>();
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
    }

    public int getSeat() {
        return seat;
    }

    public void setSeat(int seat) {
        this.seat = seat;
    }

    public void initPais(List<Mj> hand, List<Mj> out) {
        if (hand != null) {
            this.handPais = hand;

        }
        if (out != null) {
            this.outPais = out;
        }
    }

    @JSONField(serialize = false)
    public boolean isSignTing() {
        return signTing == 2;
    }

    public void dealHandPais(List<Mj> pais) {
        this.handPais = pais;
        getPlayingTable().changeCards(seat);
    }

    public boolean isGangshangHua() {
        return dahu.contains(6);
    }

    public List<Mj> getHandMajiang() {
        return handPais;
    }

    // 0缺一色 1板板胡 2大四喜 3六六顺
    public List<Mj> showXiaoHuMajiangs(int xiaohu, boolean connect) {
		return handPais;
    }

    private List<Mj> getShowCards(Map<Integer, List<Mj>> cardMap, boolean connect) {
        if (cardMap == null) {
            return handPais;
        }
        List<Mj> list = null;
        for (Entry<Integer, List<Mj>> entry : cardMap.entrySet()) {
            if (huXiaohuCards.contains(entry.getKey())) {
                list = entry.getValue();
            } else {
                if (!connect) {
                    return entry.getValue();
                }
            }
        }
        return list;
    }

    public List<Integer> getHandPais() {
        return MjHelper.toMajiangIds(handPais);
    }

    public List<Integer> getOutPais() {
        return MjHelper.toMajiangIds(outPais);
    }

    public List<Mj> getOutMajing() {
        return outPais;
    }

    public void moMajiang(Mj majiang) {
        setPassMajiangVal(0);
        handPais.add(majiang);
        getPlayingTable().changeCards(seat);
        clearPassXiaoHu2();
    }

    public void addOutPais(List<Mj> cards, int action, int disSeat) {
        handPais.removeAll(cards);
        if (action == 0) {
            outPais.addAll(cards);
        } else {
            if (action == MjDisAction.action_buzhang) {
                buzhang.addAll(cards);
                Mj pengMajiang = cards.get(0);
                Iterator<Mj> iterator = peng.iterator();
                while (iterator.hasNext()) {
                    Mj majiang = iterator.next();
                    if (majiang.getVal() == pengMajiang.getVal()) {
                        buzhang.add(majiang);
                        iterator.remove();
                    }
                }
            } else if (action == MjDisAction.action_chi) {
                chi.addAll(cards);

            } else if (action == MjDisAction.action_peng) {
                peng.addAll(cards);
                // changeAction(0, 1);
            } else if (action == MjDisAction.action_minggang) {
                myExtend.setMjFengshen(FirstmythConstants.firstmyth_index12, 1);
                mGang.addAll(cards);
                if (cards.size() != 1) {
                    changeAction(1, 1);
                } else {
                    Mj pengMajiang = cards.get(0);
                    Iterator<Mj> iterator = peng.iterator();
                    while (iterator.hasNext()) {
                        Mj majiang = iterator.next();
                        if (majiang.getVal() == pengMajiang.getVal()) {
                            mGang.add(majiang);
                            iterator.remove();
                        }
                    }
                    changeAction(2, 1);
                }

            } else if (action == MjDisAction.action_angang) {
                myExtend.setMjFengshen(FirstmythConstants.firstmyth_index12, 1);
                aGang.addAll(cards);
                changeAction(3, 1);
            } else if (action == MjDisAction.action_buzhang_an) {
                myExtend.setMjFengshen(FirstmythConstants.firstmyth_index12, 1);
                buzhangAn.addAll(cards);
                changeAction(8, 1);
            }
            getPlayingTable().changeExtend();
            addCardType(action, cards, disSeat, 0);
        }

        getPlayingTable().changeCards(seat);
        LogUtil.printDebug("{}, 22hand:{}  , {}", getName(), handPais, cards);
    }

    public void addCardType(int action, List<Mj> disCardList, int disSeat, int layEggId) {
        if (action != 0) {
            if (action == MjDisAction.action_buzhang && disCardList.size() == 1) {
                Mj majiang = disCardList.get(0);
                for (MjCardDisType disType : cardTypes) {
                    if (disType.getAction() == MjDisAction.action_peng) {
                        if (disType.isHasCardVal(majiang)) {
                            disType.setAction(MjDisAction.action_buzhang);
                            disType.addCardId(majiang.getId());
                            disType.setDisSeat(seat);
                            break;
                        }
                    }
                }
            } else if (action == MjDisAction.action_minggang && disCardList.size() == 1) {
                Mj majiang = disCardList.get(0);
                for (MjCardDisType disType : cardTypes) {
                    if (disType.getAction() == MjDisAction.action_peng) {
                        if (disType.isHasCardVal(majiang)) {
                            disType.setAction(MjDisAction.action_minggang);
                            disType.addCardId(majiang.getId());
                            disType.setDisSeat(seat);
                            break;
                        }
                    }
                }
            } else {
                MjCardDisType type = new MjCardDisType();
                type.setAction(action);
                type.setCardIds(MjHelper.toMajiangIds(disCardList));
                type.setHux(layEggId);
                type.setDisSeat(disSeat);
                cardTypes.add(type);
            }
        }
    }

    /**
     * 牌是否14张
     * true不需要摸牌了
     *
     * @return
     */
    public boolean noNeedMoCard() {
        return !handPais.isEmpty() && handPais.size() % 3 == 2;
    }

    /**
     * @param
     * @return
     * @description 把牌移出手牌
     * @author Guang.OuYang
     * @date 2019/10/21
     */
    public void removeOutPais(List<Mj> cards, int action) {
        boolean remove = outPais.removeAll(cards);
        if (remove) {
            if (action == MjDisAction.action_peng) {
                changeAction(4, 1);
            } else if (action == MjDisAction.action_minggang) {
                changeAction(5, 1);
            }
            getPlayingTable().changeCards(seat);

        }
    }

    public String toExtendStr() {
        StringBuffer sb = new StringBuffer();
        sb.append(MjHelper.implodeMajiang(peng, ",")).append("|");
        sb.append(MjHelper.implodeMajiang(aGang, ",")).append("|");
        sb.append(MjHelper.implodeMajiang(mGang, ",")).append("|");
        sb.append(StringUtil.implode(actionArr)).append("|");
        sb.append(StringUtil.implode(actionTotalArr)).append("|");
        sb.append(StringUtil.implode(passGangValList, ",")).append("|");
        sb.append(MjHelper.implodeMajiang(chi, ",")).append("|");
        sb.append(MjHelper.implodeMajiang(buzhang, ",")).append("|");
        sb.append(StringUtil.implode(dahu, ",")).append("|");
        sb.append(StringUtil.implode(huXiaohu, ",")).append("|");
        sb.append(StringUtil.implode(passXiaoHuList, ",")).append("|");
        sb.append(StringUtil.implode(passHuValList, ",")).append("|");
        sb.append(MjHelper.implodeMajiang(buzhangAn, ",")).append("|");

        sb.append(StringUtil.implode(huXiaohuCards, ",")).append("|");

        sb.append(MjHelper.implodeMap(gangSeat, ",")).append("|");
        sb.append(StringUtil.implode(buyHorses, ",")).append("|");

        return sb.toString();

    }

    public void initExtend(String info) {
        if (StringUtils.isBlank(info)) {
            return;
        }
        int i = 0;
        String[] values = info.split("\\|");
        String val1 = StringUtil.getValue(values, i++);
        peng = MjHelper.explodeMajiang(val1, ",");
        String val2 = StringUtil.getValue(values, i++);
        aGang = MjHelper.explodeMajiang(val2, ",");
        String val3 = StringUtil.getValue(values, i++);
        mGang = MjHelper.explodeMajiang(val3, ",");
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
        chi = MjHelper.explodeMajiang(val7, ",");
        String val8 = StringUtil.getValue(values, i++);
        buzhang = MjHelper.explodeMajiang(val8, ",");
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
        buzhangAn = MjHelper.explodeMajiang(val13, ",");


        String val14 = StringUtil.getValue(values, i++);
        if (!StringUtils.isBlank(val14)) {
            huXiaohuCards = StringUtil.explodeToIntList(val14);
        }

        String val16 = StringUtil.getValue(values, i++);
        if (!StringUtils.isBlank(val16)) {
            gangSeat = MjHelper.explodeToMap(val16, ",");
        }

        String val15 = StringUtil.getValue(values, i++);
        if (!StringUtils.isBlank(val15)) {
            buyHorses = StringUtil.explodeToIntList(val15);
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
        sb.append(signTing).append(",");
        sb.append(signTingPao).append(",");
        sb.append(takeCardFlag).append(",");
        sb.append(firstDisCard ? 1 : 0).append(",");
        sb.append(kingCardNumHuFlag).append(",");
        sb.append(floorCardNumHuFlag).append(",");
        sb.append(deathHu ? 1 : 0).append(",");
        sb.append(mGangScore).append(",");
        sb.append(aGangScore).append(",");
        sb.append(fGangScore).append(",");
        sb.append(horseAndGangScore).append(",");
        sb.append(xiScore).append(",");
        sb.append(faScore).append(",");
        sb.append(signCanAutoDisCard?1:0).append(",");
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
            this.state = MjEnumHelper.getPlayerState(stateVal);
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
            this.signTing = StringUtil.getIntValue(values, i++);
            this.signTingPao = StringUtil.getIntValue(values, i++);
            this.takeCardFlag = StringUtil.getIntValue(values, i++);
            this.firstDisCard = StringUtil.getLongValue(values, i++) > 0;
            this.kingCardNumHuFlag = StringUtil.getIntValue(values, i++);
            this.floorCardNumHuFlag = StringUtil.getIntValue(values, i++);
            this.deathHu = StringUtil.getIntValue(values, i++) > 0;
            this.mGangScore = StringUtil.getIntValue(values, i++);
            this.aGangScore = StringUtil.getIntValue(values, i++);
            this.fGangScore = StringUtil.getIntValue(values, i++);
            this.horseAndGangScore = StringUtil.getIntValue(values, i++);
            this.xiScore = StringUtil.getIntValue(values, i++);
            this.faScore = StringUtil.getIntValue(values, i++);
            this.signCanAutoDisCard = StringUtil.getIntValue(values, i++)>0;
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
        if (!buzhang.isEmpty()) {
            list.addAll(MjHelper.toMajiangIds(buzhang));
        }
        if (!buzhangAn.isEmpty()) {
            list.addAll(MjHelper.toMajiangIds(buzhangAn));
        }
        if (!chi.isEmpty()) {
            list.addAll(MjHelper.toMajiangIds(chi));
        }

        return list;
    }

    public List<PhzHuCards> buildDisCards(long lookUid) {
        return buildDisCards(lookUid, true);
    }

    public List<PhzHuCards> buildDisCards(long lookUid, boolean hide) {
        List<PhzHuCards> list = new ArrayList<>();
        for (MjCardDisType type : cardTypes) {
            if (hide && lookUid != this.userId && type.getAction() == MjDisAction.action_angang) {
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
    public PlayerInTableRes.Builder
    buildPlayInTableInfo(boolean isrecover) {
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
        res.addAllAngangIds(MjHelper.toMajiangIds(aGang));
        res.addAllAngangIds(MjHelper.toMajiangIds(buzhangAn));
        res.addAllMoldCards(buildDisCards(userId));
        List<Mj> gangList = getGang();
        // 是否杠过
        res.addExt(gangList.isEmpty() ? 0 : 1);

        YyMjTable table = getPlayingTable(YyMjTable.class);
        // 现在是否自己摸的牌
        res.addExt(noNeedMoCard() ? 1 : 0);
        res.addExt(handPais != null ? handPais.size() : 0);
        res.addExt(piaoPoint);// 飘分
        res.addExt(autoPlay ? 1 : 0);//4
        res.addExt(autoPlaySelf ? 1 : 0);//5
        res.addExt(Integer.valueOf(getPayBindId() + "")); //绑定邀请码
        res.addExt(isSignTing() ? 1 : 0); //听牌状态

        int totalLen = 100;
        StringBuffer buyHorse = new StringBuffer("1");
        for (int i = 0; i < getBuyHorses().size(); i++) {
            int j = getBuyHorses().get(i);
            if (j < totalLen) {
                //补位
                for (int k = 0; k < String.valueOf(totalLen).length() - String.valueOf(j).length(); k++) {
                    buyHorse.append("0");
                }
            }
            buyHorse.append(j);
        }

        res.addExt(Integer.valueOf(buyHorse.toString()));

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
            if (gu == null || !groupId.equals(gu.getGroupId() + "")) {
                gu = GroupDao.getInstance().loadGroupUser(getUserId(), groupId);
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
        actionTotalArr = new int[7];
        dahu = new ArrayList<>();
        huXiaohu = new ArrayList<>();
        peng.clear();
        aGang.clear();
        mGang.clear();
        chi.clear();
        buzhang.clear();
        buzhangAn.clear();
        cardTypes.clear();
        buyHorses.clear();
        gangSeat.clear();
        setPassMajiangVal(0);
        clearPassGangVal();
        clearPassXiaoHu();
        setWinCount(0);
        setLostCount(0);
        setPoint(0);
        setGangPoint(0);
        setMGangScore(0);
        setAGangScore(0);
        setFGangScore(0);
        setHorseAndGangScore(0);
        setLostPoint(0);
        setTotalPoint(0);
        setSeat(0);
        setPiaoPoint(-1);

        clearPlayerOverInfo();

        if (!isCompetition) {
            setPlayingTableId(0);
        }
        huMjIds = new ArrayList<>();
        saveBaseInfo();

        autoPlaySelf = false;
        autoPlay = false;
        lastCheckTime = System.currentTimeMillis();
        checkAutoPlay = false;
        huXiaohuCards.clear();
    }


    public void clearPlayerOverInfo() {
        setFirstDisCard(true);
        setKingCardNumHuFlag(0);
        setFloorCardNumHuFlag(0);
        setSignTing(0);
        setSignTingPao(0);
        setTakeCardFlag(0);
        setDeathHu(false);
        changeExtend();
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
        for (MjCardDisType type : cardTypes) {
            list.add(type.buildMsg().build());
        }
        res.addAllMoldPais(list);
        res.addAllDahus(dahu);
        res.addAllXiaohus(huXiaohu);
        //res.setFanPao(actionArr[0]==1?1:0);

        addCloseingExt(res);

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
        for (MjCardDisType type : cardTypes) {
            list.add(type.buildMsg().build());
        }

        res.addAllMoldPais(list);
        res.addAllDahus(dahu);
        res.addAllXiaohus(huXiaohu);

        addCloseingExt(res);
        return res;
    }

    private void addCloseingExt(ClosingMjPlayerInfoRes.Builder res) {
        res.addExt(getPiaoPoint());
        res.addExt(gangPoint);
        res.addExt(getPlayingTable(YyMjTable.class).getReappointment()); //连庄 0~9 - 1~10
        res.addExt(mGangScore); //明杠
        res.addExt(aGangScore); //暗杠
        res.addExt(fGangScore); //放杠
//        res.addExt(horseAndGangScore); //马和杠  0~6
        res.addExt(0); //马和杠  0~6
        res.addExt(mGangScoreHorseAndGang); //马和杠  0~7
        res.addExt(aGangScoreHorseAndGang); //马和杠  0~8
        res.addExt(fGangScoreHorseAndGang); //马和杠  0~9
        res.addExt(xiScore); //喜分  0~11
        res.addExt(faScore); //罚分  0~12
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
        setMGangScore(0);
        setAGangScore(0);
        setFGangScore(0);
        setHorseAndGangScore(0);
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
        huXiaohuCards.clear();
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
                    this.outPais = MjHelper.toMajiang(list);
                } else {
                    MjCardDisType type = new MjCardDisType();
                    type.init(value);
                    cardTypes.add(type);

                    List<Mj> majiangs = MjHelper.toMajiang(type.getCardIds());
                    if (type.getAction() == MjDisAction.action_angang) {
                        aGang.addAll(majiangs);
                    } else if (type.getAction() == MjDisAction.action_minggang) {
                        mGang.addAll(majiangs);
                    } else if (type.getAction() == MjDisAction.action_chi) {
                        chi.addAll(majiangs);
                    } else if (type.getAction() == MjDisAction.action_peng) {
                        peng.addAll(majiangs);
                    } else if (type.getAction() == MjDisAction.action_buzhang) {
                        buzhang.addAll(majiangs);
                    } else if (type.getAction() == MjDisAction.action_buzhang_an) {
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
    public List<Mj> getCanChiMajiangs(Mj disMajiang) {
        return MjTool.checkChi(handPais, disMajiang, getPlayingTable(YyMjTable.class));
    }

    public boolean isTingPai(int val, boolean needRemoveThisVal) {
        return isTingPai(val, false, needRemoveThisVal);
    }

    /**
     * 是否听牌
     *
     * @param val
     * @return
     */
    public boolean isTingPai(int val, boolean onlySevenDuiNoHu, boolean needRemoveThisVal) {
        List<Mj> copy = new ArrayList<>(handPais);
        List<Mj> gangList = getGang();

        List<Mj> copyPeng = new ArrayList<>(peng);

        if (needRemoveThisVal) {
            gangList.addAll(MjQipaiTool.dropVal(copy, val));
            if (!peng.isEmpty()) {
                gangList.addAll(MjQipaiTool.dropVal(copyPeng, val));
            }
        }

        if (copy.size() % 3 != 2 ) {
            if (getPlayingTable(YyMjTable.class).getKingCard() != null) {
                copy.add(Mj.getMajang(getPlayingTable(YyMjTable.class).getKingCard().getId()));
            }else {
				List<Mj> tingMjs = MjTool.getTingMjs(copy, getGang(), peng, chi, buzhang, getPlayingTable(YyMjTable.class).getGameModel().isNeed258(), true, 0, this.getPlayingTable(YyMjTable.class), this);
				if (!CollectionUtils.isEmpty(tingMjs)) {
					copy.add(tingMjs.get(0));
				}else {
					copy.add(copy.get(copy.size() - 1));
				}
            }
        }
        List<Mj> bzCopy = new ArrayList<>(buzhang);
        bzCopy.addAll(buzhangAn);
        boolean jiang258 = true;
        YyMjTable table = getPlayingTable(YyMjTable.class);
        //开杠
        if (table.getGameModel().getSpecialPlay().isJiajianghu()) {
            jiang258 = false;
        }
        MjiangHu huBean = MjTool.isHu(copy, gangList, copyPeng, chi, bzCopy, false, jiang258, table, handPais.get(0), this, false);
        //听牌, 这里计算假设杠了之后的听牌类型, 依然是7小对, 则不能听
        if (onlySevenDuiNoHu && !table.getGameModel().getSpecialPlay().isXiaoDuiGang() && ((huBean.isXiaodui() || huBean.isHao7xiaodui() || huBean.isShuang7xiaodui()))) {
            return false;
        }
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
        for (MjCardDisType huxi : cardTypes) {
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
    public List<Integer> checkDisCard(Mj majiang) {
        return checkDisCard(majiang, true, false, getPlayingTable(YyMjTable.class).getKingCard() != null && majiang != null && getPlayingTable(YyMjTable.class).getKingCard().getVal() == majiang.getVal());
    }

    /**
     * @param majiang
     * @param need258
     * @param gangHouMo        是否杠后摸打出来的牌
     * @param onlyYingZhuangHu 是否只能硬庄,意味着王牌不能变了
     * @return
     * @description
     * @author Guang.OuYang
     * @date 2019/10/28
     */
    public List<Integer> checkDisCard(Mj majiang, boolean need258, boolean gangHouMo, boolean onlyYingZhuangHu) {
        List<Integer> list = new ArrayList<>();
        MjAction actData = new MjAction();
        YyMjTable table = getPlayingTable(YyMjTable.class);
        // 没有出现漏炮的情况
        // if (passMajiangVal != majiang.getVal()) {
        boolean canGang = true;
        if ((!table.moHu() || table.getDisCardSeat() == seat) /*&& passMajiangVal != majiang.getVal()*/) {
            List<Mj> copy = new ArrayList<>(handPais);
            copy.add(majiang);
            List<Mj> bzCopy = new ArrayList<>(buzhang);
            bzCopy.addAll(buzhangAn);
            boolean jiang258 = true;

            if (table.getGameModel().getSpecialPlay().isJiajianghu()) {
                if (table.getDisCardRound() == 1) {
                    jiang258 = false;
                } else if (table.getLeftMajiangCount() == 0) {
                    jiang258 = false;
                } else if (!need258) {
                    jiang258 = false;
                }
            }
            MjiangHu hu = MjTool.isHu(copy, getGang(), peng, chi, bzCopy, false, jiang258, table, majiang, this, onlyYingZhuangHu);

            if (hu.isHu()) {
                //二人麻将
                if (table.getMaxPlayerCount() == 2) {
                    if (table.getGameModel().getSpecialPlay().isOnlyDaHu()) {
                        if (hu.getDahuList().size() > 0 || !need258) {
                            actData.addHu();
                            //抢杠胡
                        } else if (table.getGameModel().getSpecialPlay().isMenqing() && !isChiPengGang()) {
                            actData.addHu();
                        }
                    } else {
                        if (table.getGameModel().getSpecialPlay().isXiaohuZiMo()) {
                            if (table.getGameModel().getSpecialPlay().isMenqing() && !isChiPengGang()) {
                                actData.addHu();
                            } else if (hu.getDahuList().size() > 0 || !need258) {
                                actData.addHu();
                            }
                        } else {
                            actData.addHu();
                        }
                    }
                } else {
                    actData.addHu();
                }

                //抢杠胡
                if (gangHouMo && hu.isHu()) {
                    actData.addQiangGangHu();
                }

                //黑天胡, 特殊操作, 这里做限制用
                if (hu.isHeitianhu()) {
                    actData.addBlackSkyHu();
                }

                //没有大胡门子算平胡, 特殊操作, 这里做限制用
                if (hu.isHu() && hu.getDahuCount() == 0) {
                    actData.addPingHu();
                }

                //平胡特殊操作, 这里做限制用
                if (hu.isYingZhuang()) {
                    actData.addYingZhuang();
                }

//                if (hu.isXiaodui() || hu.isHao7xiaodui() || hu.isShuang7xiaodui()) {
//                    actData.addXiaoDui();
//                    canGang = false;
//                }
            }
        }
        List<Mj> gangList = getGang();
        if (table.getDisCardSeat() != seat) {
            // 现在出牌的人不是自己
            int count = MjHelper.getMajiangCount(handPais, majiang.getVal());
            if (count == 3) {
            	//报听不能接杠
				boolean isTing = this.isSignTing() ? false : table.getGameModel().isNeedTingGang() ? isTingPai(majiang.getVal(), true) : true;
                if (isTing) {
                    if (canGang)
                        actData.addMingGang();
                    if (table.getGameModel().getSpecialPlay().isCanBuCard())
                        actData.addBuZhang();
                } else {
                    if (gangList.isEmpty() && table.getGameModel().getSpecialPlay().isCanBuCard()) {
                        actData.addBuZhang();
                    }
                }
            }
            if (gangList.isEmpty() || !table.getGameModel().getSpecialPlay().isGangMoNum()) {
                if (count >= 2) {
                    actData.addPeng();
                }
                if (table.calcNextSeat(table.getDisCardSeat()) == seat) {
                    if (!(/*table.getMaxPlayerCount() == 2 &&*/ table.getGameModel().getSpecialPlay().isBuChi())) {
                        List<Mj> chi = MjTool.checkChi(handPais, majiang, getPlayingTable(YyMjTable.class));
                        if (!chi.isEmpty()) {
                            actData.addChi();
                        }
                    }
                }
            }
        } else {
            // 出牌的人是自己 (杠后补张)
            int count = MjHelper.getMajiangCount(handPais, majiang.getVal());
            if (count == 3 || MjHelper.toMajiangValMap(peng).containsKey(majiang.getVal())) {
                boolean isTing = table.getGameModel().isNeedTingGang() || this.isSignTing()  ? isTingPai(majiang.getVal(), true):true;
                if (isTing) {
                    if (canGang)
                        actData.addAnGang();
                    if (table.getGameModel().getSpecialPlay().isCanBuCard())
                        actData.addBuZhangAn();
                } else {
                    if (gangList.isEmpty() && table.getGameModel().getSpecialPlay().isCanBuCard()) {
                        actData.addBuZhangAn();
                    }
                }
            }
//            Map<Integer, Integer> pengMap = MjHelper.toMajiangValMap(peng);
//            if (pengMap.containsKey(majiang.getVal())) {
//                boolean isTing = table.getGameModel().isNeedTingGang() ? isTingPai(majiang.getVal(), true) : true;
//                if (isTing) {
//                    if (canGang)
//                        actData.addMingGang();
//                    if (table.getGameModel().getSpecialPlay().isCanBuCard())
//                        actData.addBuZhang();
//                } else {
//                    if (gangList.isEmpty() && table.getGameModel().getSpecialPlay().isCanBuCard()) {
//                        actData.addBuZhang();
//                    }
//                }
//            }
        }
        int[] arr = actData.getArr();
        for (int val : arr) {
            list.add(val);
        }
        if (list.contains(1)) {
            return list;
        } else {
            return Collections.EMPTY_LIST;
        }

    }

    public Mj getLastMoMajiang() {
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

    public void addXiaoHu2(int xiaoHuType, int val) {
        this.huXiaohu.add(xiaoHuType);

        if (val > 0 && !huXiaohuCards.contains(val)) {

            this.huXiaohuCards.add(val);
        }
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
        return MjiangHu.calcDaHuCount(dahu);
    }

    public int getDahuScore() {
        return MjiangHu.calcMenZiScore(getPlayingTable(YyMjTable.class), getDahu());
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
    public MjiangHu checkHu(Mj disMajiang, boolean isbegin) {
        List<Mj> copy = new ArrayList<>(handPais);
        if (disMajiang != null) {
            copy.add(disMajiang);
        }
        List<Mj> bzCopy = new ArrayList<>(buzhang);
        bzCopy.addAll(buzhangAn);
        boolean jiang258 = true;
        YyMjTable table = getPlayingTable(YyMjTable.class);

        if (table.getGameModel().getSpecialPlay().isJiajianghu()) {
            if (seat != table.getMoMajiangSeat()) {
                if (table.getDisCardRound() == 1) {
                    jiang258 = false;
                }
                Mj mj = table.getGangHuMajiang(seat);
                if (mj != null && disMajiang != null && mj.getVal() == disMajiang.getVal()) {
                    jiang258 = false;//杠上炮
                }
                if (table.getLeftMajiangCount() == 0) {
                    jiang258 = false;
                }
            } else if (table.getDisCardRound() == 0) {
                jiang258 = false;
            } else if (table.getLeftMajiangCount() == 0) {
                jiang258 = false;
            } else if (getGang().size() > 0) {
                jiang258 = false;
            }

        }

        MjiangHu hu = MjTool.isHu(copy, getGang(), peng, chi, bzCopy, isbegin, jiang258, table, disMajiang, this, false);
        if (hu.isHu()) {
//            if (table.getGameModel().getSpecialPlay().isMenqing() && !isChiPengGang()) {
//                hu.setMenqing(true);
//            }
            hu.setBaoTing(isSignTing());
            //跟庄
//            hu.setGenZhuang(getPlayingTable(ChaosMjTable.class).isFollowMaster());
            hu.initDahuList();
            hu.setDahuList(hu.buildDahuList());
        }
        return hu;
    }


    public boolean isChiPengGang() {
        if (chi.size() > 0 || mGang.size() > 0 || peng.size() > 0 || buzhang.size() > 0) {
            return true;
        }
        return false;
    }

    /**
     * 自己出牌可做的操作
     *
     * @param majiang null 时自己碰或者吃,不能胡牌
     * @param isBegin 起牌是第一次出牌
     * @return 接炮0;碰1;明杠2;暗杠3;吃4;补张5;缺一色6;板板胡7;一枝花 8;六六顺9;大四喜10;金童玉女11;节节高12;三同13;中途四喜14;中途六六顺15;暗杠补张16;自摸17;
     */
    public List<Integer> checkMoCard(Mj majiang, boolean isBegin) {
        List<Integer> list = new ArrayList<>();
        MjAction actData = new MjAction();
        YyMjTable table = getPlayingTable(YyMjTable.class);
        boolean canXiaohu = false;
        boolean canGang = true;
        if (noNeedMoCard() || majiang != null || isBegin) {
            // 碰的时候判断不能胡牌
            List<Mj> bzCopy = new ArrayList<>(buzhang);
            bzCopy.addAll(buzhangAn);
            boolean jiang258 = true;
            if (table.getGameModel().getSpecialPlay().isJiajianghu()) {
                if (table.getDisCardRound() == 0) {
                    jiang258 = false;
                }
            }
            List<Mj> copy = new ArrayList<>(handPais);
            MjiangHu hu = MjTool.isHu(copy, getGang(), peng, chi, bzCopy, isBegin, jiang258, table, majiang, this, false);

//            canGang = hu.isHu() && !((!table.getGameModel().getSpecialPlay().isXiaoDuiGang() && (hu.isXiaodui() || hu.isHao7xiaodui() || hu.isShuang7xiaodui())) && hu.getDahuCount() == 1);

            //自摸控制
            if (hu.isHu() && getPlayingTable(YyMjTable.class).getGameModel().getSpecialPlay().isSelfMoHu()) {
                //二人麻将
                if (table.getMaxPlayerCount() == 2) {
                    if (table.getGameModel().getSpecialPlay().isOnlyDaHu()) {
                        if (table.getGameModel().getSpecialPlay().isMenqing() && !isChiPengGang()) {
                            actData.addZiMo();
                        }
                        if (hu.getDahuList().size() > 0) {
                            actData.addZiMo();
                        }
                    } else {
                        actData.addZiMo();
                    }
                } else {
                    actData.addZiMo();
                }
            }
        }
        checkGang(actData, table, canXiaohu, canGang);
        int[] arr = actData.getArr();
        for (int val : arr) {
            list.add(val);
        }

        table.checkMustHu(list);

        if (list.contains(1)) {
            return list;
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    private void checkGang(MjAction actData, YyMjTable table, boolean canXiaohu, boolean canGang) {
        if ((noNeedMoCard() && !canXiaohu) || !table.getGameModel().isNeedTingGang()) {
            List<Mj> gangList = getGang();
            Map<Integer, Integer> pengMap = MjHelper.toMajiangValMap(peng);

            for (Mj handMajiang : handPais) {
                if (pengMap.containsKey(handMajiang.getVal())) {
                    // 有碰过
					boolean isTing = this.isSignTing() ? false : table.getGameModel().isNeedTingGang() ? isTingPai(handMajiang.getVal(), true) : true;
                    if (isTing && !isPassGang(handMajiang)) {
                        if (canGang)
                            actData.addMingGang();// 可以杠
                        if (table.getGameModel().getSpecialPlay().isCanBuCard())
                            actData.addBuZhang();// 可以补张
                        break;
                    } else {
                        if (table.getGameModel().getSpecialPlay().isCanBuCard() && gangList.isEmpty()) {
                            actData.addBuZhang();// 可以补张
                        }
                    }
                }
            }

            Map<Integer, Integer> handMap = MjHelper.toMajiangValMap(handPais);
            if (handMap.containsValue(4)) {
                for (Entry<Integer, Integer> entry : handMap.entrySet()) {
                    if (entry.getValue() == 4) {
                    	//报听需要验证听牌
                        boolean isTing = table.getGameModel().isNeedTingGang() || this.isSignTing() ? isTingPai(entry.getKey(), true) : true;
                        if (isTing) {
                            if (canGang)
                                actData.addAnGang();// 可以杠
                            if (table.getGameModel().getSpecialPlay().isCanBuCard())
                                actData.addBuZhangAn(); // 可以补张
                            break;
                        } else {
                            if (table.getGameModel().getSpecialPlay().isCanBuCard() && gangList.isEmpty()) {
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
    }

    /**
     * 操作
     *
     * @param index 0点炮1点杠2明杠3暗杠4被碰5被杠6胡7自摸,8暗杠补张
     * @param val
     */
    public void changeAction(int index, int val) {
        actionArr[index] += val;
        getPlayingTable().changeExtend();
    }

    public void changeActionTotal(int index, int val) {
        actionTotalArr[index] += val;
        getPlayingTable().changeExtend();
    }

    public List<Mj> getPeng() {
        return peng;
    }

    public List<Mj> getaGang() {
        return aGang;
    }

    public List<Mj> getmGang() {
        return mGang;
    }

    public List<Mj> getGang() {
        List<Mj> gang = new ArrayList<>();
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

    /**
     * 可以碰可以杠的牌 选择了碰 再杠不算分
     *
     * @param majiang
     * @return
     */
    public boolean isPassGang(Mj majiang) {
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

    @Override
    public void endCompetition1() {

    }


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
        YyMjTable table = getPlayingTable(YyMjTable.class);
        long now = System.currentTimeMillis();
        boolean auto = isAutoPlay();
        if (!auto && table.getIsAutoPlay() > 0) {
            //检查玩家是否进入系统托管状态
            if (!checkAutoPlay && table.getGameModel().getSpecialPlay().getTableStatus() != MjConstants.TABLE_STATUS_PIAO) {
                if (noNeedMoCard() || table.getActionSeatMap().containsKey(seat)) {
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
                    if (timeOut >= MjConstants.AUTO_READY_TIME) {
                        setAutoPlayTime(0);
                        return true;
                    }
                } else if (autoType == 2) {
                    if (timeOut >= MjConstants.AUTO_HU_TIME) {
                        setAutoPlayTime(0);
                        return true;
                    }
                } else {
                    if (timeOut >= MjConstants.AUTO_PLAY_TIME) {
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
        YyMjTable table = getPlayingTable(YyMjTable.class);
        if (this.autoPlay != autoPlay || autoPlaySelf != isSelf) {
            ComMsg.ComRes.Builder res = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_tuoguan, seat, autoPlay ? 1 : 0, 0, isSelf ? 1 : 0);
            GeneratedMessage msg = res.build();
            if (table != null) {
                table.broadMsgToAll(msg);
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


    /**
     * 是否能小胡
     *
     * @param action
     * @return
     */
    public boolean canHuXiaoHu(int action) {
        if (huXiaohu.contains(action)) {
            return false;
        }
        if (passXiaoHuList.contains(action)) {
            return false;
        }
        if (action == MjAction.ZHONGTUSIXI && huXiaohu.contains(MjAction.DASIXI)) {
            return false;
        } else if (action == MjAction.ZHONGTULIULIUSHUN && huXiaohu.contains(MjAction.LIULIUSHUN)) {
            return false;
        }
        return true;
    }


    public boolean canHuXiaoHu2(int action, int val) {
        if (huXiaohu.contains(action)) {
            if (action == MjAction.YIZHIHUA || action == MjAction.QUEYISE || action == MjAction.JINGTONGYUNU || action == MjAction.BANBANHU) {
                return false;
            }
            if (val != 0 && huXiaohuCards.contains(val)) {
                return false;
            }
        }
        if (passXiaoHuList.contains(action)) {
            return false;
        }
        if (action == MjAction.ZHONGTUSIXI && huXiaohu.contains(MjAction.DASIXI) && huXiaohuCards.contains(val)) {
            return false;
        } else if (action == MjAction.ZHONGTULIULIUSHUN && huXiaohu.contains(MjAction.LIULIUSHUN) && huXiaohuCards.contains(val)) {
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
            if (xiaoHu != MjAction.ZHONGTUSIXI && xiaoHu != MjAction.DASIXI && xiaoHu != MjAction.ZHONGTULIULIUSHUN && xiaoHu != MjAction.LIULIUSHUN) {
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

    public List<Integer> getHuMjIds() {
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
        if (xiaoHu == 10) {
            passXiaoHuList2.add(MjAction.ZHONGTUSIXI);
        } else if (xiaoHu == 9) {
            passXiaoHuList2.add(MjAction.ZHONGTULIULIUSHUN);
        }

    }

    public List<Integer> getHuXiaohuCards() {
        return huXiaohuCards;
    }

    public void addHuXiaohuCards2(int val) {
        huXiaohuCards.add(val);
    }

    public List<Mj> getChi() {
        return chi;
    }

    public List<Mj> getBuzhang() {
        List<Mj> bzCopy = new ArrayList<>(buzhang);
        bzCopy.addAll(buzhangAn);
        return bzCopy;
    }

    public List<Mj> removeGangMj(Mj mj) {
        buzhang.remove(mj);
        mGang.remove(mj);
        List<Mj> indx = new ArrayList<>();
        for (MjCardDisType type : cardTypes) {
            if (type.getCardIds() != null) {
                int size = type.getCardIds().size();
                int j = 0;
                for (int i = 0; i < size; i++) {
                    Integer id = type.getCardIds().get(j);
                    if (Mj.getMajang(id).getVal() == mj.getVal() && MjDisAction.action_peng != type.getAction()) {
                        type.getCardIds().remove(j);
                        indx.add(Mj.getMajang(id));
                        break;
                    } else {
                        j++;
                    }
                }
            }
        }
        return indx;
    }


}
