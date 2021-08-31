package com.sy599.game.qipai.zhz.been;

import com.google.protobuf.GeneratedMessage;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.SharedConstants.player_state;
import com.sy599.game.db.bean.group.GroupUser;
import com.sy599.game.db.dao.gold.GoldDao;
import com.sy599.game.db.dao.group.GroupDao;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.TablePhzResMsg.ClosingPhzPlayerInfoRes;
import com.sy599.game.msg.serverPacket.TablePhzResMsg.PhzHuCards;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes;
import com.sy599.game.qipai.zhz.command.PaohuziCommandProcessor;
import com.sy599.game.qipai.zhz.constant.EnumHelper;
import com.sy599.game.qipai.zhz.constant.PaohuziConstant;
import com.sy599.game.qipai.zhz.constant.PaohzCard;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.DaPaiTingPaiInfo;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.DaPaiTingPaiRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.TingPaiRes;
import com.sy599.game.qipai.zhz.rule.PaohuziMingTangRule;
import com.sy599.game.qipai.zhz.tool.PaohuziHuLack;
import com.sy599.game.qipai.zhz.tool.PaohuziTool;
import com.sy599.game.util.GameUtil;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.Map.Entry;

public class ZhzPlayer extends Player {
    // 座位id
    private int seat;
    // 状态
    private player_state state;// 1进入 2已准备 3正在玩 4已结束
    private int isEntryTable;
    private List<PaohzCard> handPais;
    private List<PaohzCard> outPais;
    private List<PaohzCard> peng;
    private List<PaohzCard> chi;
    private List<PaohzCard> pao;
    private List<Integer> passPeng;
    private List<Integer> passChi;
    private List<PaohzCard> zai;
    private List<PaohzCard> ti;
    private int outHuXi;// 需要记下吃的胡息
    private int huxi;
    private int winCount;
    private int lostCount;
    private int lostPoint;
    private int point;
    private PaohuziHuLack hu;
    private List<CardTypeHuxi> cardTypes;
    private boolean isFristDisCard;
    private List<PaohzCard> chouZai;
    private int zaiHuxi;
    private int fangZhao;
    private List<Integer> firstPais;//初始手牌
    private int winLossPoint;//输赢分用于统计排名 bopi
    //0胡1自摸2提3跑
    private int[] actionTotalArr = new int[4];

    private volatile boolean autoPlay = false;//托管
    private volatile long lastOperateTime = 0;//最后操作时间
    private volatile long lastCheckTime = 0;//最后检查时间

    private volatile long autoPlayTime = 0;//自动操作时间
    private volatile boolean isCheckAuto = false; //开始托管合计时
    private boolean siShou=false;//死守，大小相公，不能胡牌


    //是否可以栽胡,提胡,跑胡，可以的话可能会在当回合胡牌时成为死守状态，而不能胡牌，用该状态标记
    private volatile boolean zaiTiHu=false;
    //出牌数量，只计算吃碰跑提栽之后的单出牌
    private int disNum=0;
    //摸排数量
    private int moNum=0;
    //吃完牌后，相同的牌禁止出
    private List<Integer> forbidDis=new ArrayList<>();
    //特殊名堂list
    private List<Integer> mt=new ArrayList<>();
    //大胡次数
    private int daHuNum=0;
    //小胡次数
    private int xiaoHuNum=0;
    //听牌map
    Map<Integer, List<Integer>> idAndTing=new HashMap<>();
    //听牌list
    List<Integer> ting=new ArrayList<>();
    public ZhzPlayer() {
        handPais = new ArrayList<>();
        outPais = new ArrayList<>();
        peng = new ArrayList<>();
        passPeng = new ArrayList<>();
        passChi = new ArrayList<>();
        chi = new ArrayList<>();
        zai = new ArrayList<>();
        pao = new ArrayList<>();
        ti = new ArrayList<>();
        chouZai = new ArrayList<>();
        cardTypes = new ArrayList<>();
        firstPais = new ArrayList<>();
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

            String passPengStr = StringUtil.getValue(values, i++);
            if (!StringUtils.isBlank(passPengStr)) {
                passPeng = StringUtil.explodeToIntList(passPengStr, "_");
            }
            String passChiStr = StringUtil.getValue(values, i++);
            if (!StringUtils.isBlank(passChiStr)) {
                passChi = StringUtil.explodeToIntList(passChiStr, "_");
            }

            this.outHuXi = StringUtil.getIntValue(values, i++);
            this.huxi = StringUtil.getIntValue(values, i++);
            this.winCount = StringUtil.getIntValue(values, i++);
            this.lostCount = StringUtil.getIntValue(values, i++);
            this.lostPoint = StringUtil.getIntValue(values, i++);
            this.point = StringUtil.getIntValue(values, i++);
            setTotalPoint(StringUtil.getIntValue(values, i++));
            this.isFristDisCard = StringUtil.getIntValue(values, i++) == 1;
            setMaxPoint(StringUtil.getIntValue(values, i++));
            this.zaiHuxi = StringUtil.getIntValue(values, i++);
            this.fangZhao = StringUtil.getIntValue(values, i++);
            String firstPaisStr = StringUtil.getValue(values, i++);
            if (!StringUtils.isBlank(firstPaisStr)) {
                firstPais = StringUtil.explodeToIntList(firstPaisStr, "_");
            }
            String actionTotalArrStr = StringUtil.getValue(values, i++);
            if (!StringUtils.isBlank(actionTotalArrStr)) {
                this.actionTotalArr = StringUtil.explodeToIntArray(actionTotalArrStr, "_");
            }
            this.zaiTiHu=StringUtil.getIntValue(values,i++) == 1;
            this.siShou=StringUtil.getIntValue(values,i++) == 1;
            this.disNum=StringUtil.getIntValue(values,i++);

            String forbidDisStr = StringUtil.getValue(values, i++);
            if (!StringUtils.isBlank(forbidDisStr)) {
                forbidDis = StringUtil.explodeToIntList(forbidDisStr, "_");
            }
            String mtStr = StringUtil.getValue(values, i++);
            if (!StringUtils.isBlank(mtStr)) {
                mt = StringUtil.explodeToIntList(mtStr, "_");
            }
            this.daHuNum=StringUtil.getIntValue(values,i++);
            this.xiaoHuNum=StringUtil.getIntValue(values,i++);

            String tingStr = StringUtil.getValue(values, i++);
            if (!StringUtils.isBlank(tingStr)) {
                ting = StringUtil.explodeToIntList(tingStr, "_");
            }
            this.moNum=StringUtil.getIntValue(values,i++);
        }
    }

    @Override
    public String toInfoStr() {
        StringBuilder sb = new StringBuilder();
        sb.append(getUserId()).append(",");
        sb.append(seat).append(",");
        int stateVal = 0;
        if (state != null) {
            stateVal = state.getId();
        }
        sb.append(stateVal).append(",");
        sb.append(isEntryTable).append(",");
        sb.append(StringUtil.implode(passPeng, "_")).append(",");
        sb.append(StringUtil.implode(passChi, "_")).append(",");
        sb.append(outHuXi).append(",");
        sb.append(huxi).append(",");
        sb.append(winCount).append(",");
        sb.append(lostCount).append(",");
        sb.append(lostPoint).append(",");
        sb.append(point).append(",");
        sb.append(getTotalPoint()).append(",");
        sb.append(isFristDisCard ? 1 : 0).append(",");
        sb.append(getMaxPoint()).append(",");
        sb.append(zaiHuxi).append(",");
        sb.append(fangZhao).append(",");
        sb.append(StringUtil.implode(firstPais, "_")).append(",");
        sb.append(StringUtil.implode(actionTotalArr, "_")).append(",");
        sb.append(zaiTiHu?1:0).append(",");
        sb.append(siShou?1:0).append(",");
        sb.append(disNum).append(",");
        sb.append(StringUtil.implode(forbidDis, "_")).append(",");
        sb.append(StringUtil.implode(mt, "_")).append(",");
        sb.append(daHuNum).append(",");
        sb.append(xiaoHuNum).append(",");
        sb.append(StringUtil.implode(ting, "_")).append(",");
        sb.append(moNum).append(",");
        return sb.toString();
    }

    @Override
    public void clearTableInfo() {
        BaseTable table = getPlayingTable();
        boolean isCompetition = false;
        if (table != null && table.isCompetition()) {
            isCompetition = true;
            endCompetition();
        }
        setIsEntryTable(0);
        changeIsLeave(0);
        handPais.clear();
        outPais.clear();
        changeState(null);
        peng.clear();
        chi.clear();
        pao.clear();
        zai.clear();
        chouZai.clear();
        ti.clear();
        passChi.clear();
        passPeng.clear();
        cardTypes.clear();
        chouZai.clear();
        setZaiHuxi(0);
        setHu(null);
        setOutHuxi(0);
        setHuxi(0);
        setWinCount(0);
        setLostCount(0);
        setPoint(0);
        setLostPoint(0);
        setTotalPoint(0);
        setMaxPoint(0);
        setSeat(0);
        if (!isCompetition) {
            setPlayingTableId(0);
        }
        actionTotalArr = new int[4];
//        if(table.isAutoPlay() && autoPlay){
//            setAutoPlay(false,table);
//        }
        this.autoPlay = false;
        this.setCheckAuto(false);
        if(!autoPlay){
            setAutoPlayCheckedTimeAdded(false);
        }
        
        
        saveBaseInfo();
        setSiShou(false);
        setDisNum(0);
        forbidDis.clear();
        mt.clear();
        daHuNum=0;
        xiaoHuNum=0;
        ting=new ArrayList<>();
        idAndTing=new HashMap<>();
        moNum=0;
    }

    public int getDisNum() {
        return disNum;
    }

    public int getMoNum() {
        return moNum;
    }

    public void setDisNum(int disNum) {
        this.disNum = disNum;
    }

    public boolean isAutoPlay() {
        return autoPlay;
    }

    public void setAutoPlay(boolean autoPlay,BaseTable table) {
        if (this.autoPlay != autoPlay){
            ComMsg.ComRes.Builder res = SendMsgUtil.buildComRes(132, seat,autoPlay?1:0, (int)userId);
            GeneratedMessage msg = res.build();
            for (Map.Entry<Long,Player> kv:table.getPlayerMap().entrySet()){
                Player player=kv.getValue();
                if (player.getIsOnline() == 0) {
                    continue;
                }
                player.writeSocket(msg);
            }
            table.addPlayLog(getSeat(), PaohzDisAction.action_tuoguan + "",(autoPlay?1:0) + "");
        }
        this.autoPlay = autoPlay;
        this.setCheckAuto(false);
        if(!autoPlay){
            setAutoPlayCheckedTimeAdded(false);
        }
    }

    public long getLastOperateTime() {
        return lastOperateTime;
    }

    public void setLastOperateTime(long lastOperateTime) {
        this.lastCheckTime = 0;
        this.lastOperateTime = lastOperateTime;
        this.autoPlayTime = 0;
        this.setCheckAuto(false);
    }

    public long getLastCheckTime() {
        return lastCheckTime;
    }

    public void setLastCheckTime(long lastCheckTime) {
        this.lastCheckTime = lastCheckTime;
    }

    public long getAutoPlayTime() {
        return autoPlayTime;
    }

    public void setAutoPlayTime(long autoPlayTime) {
        this.autoPlayTime = autoPlayTime;
    }

    public int[] getActionTotalArr() {
        return actionTotalArr;
    }


    /**
     * 操作
     * @param index 0胡1自摸2提3跑
     * @param val
     */
    public void changeAction(int index, int val) {
        actionTotalArr[index] += val;
        getPlayingTable().changeExtend();
    }
    public List<CardTypeHuxi> getCardTypes() {
        return cardTypes;
    }


    /**
     * 获得初始手牌
     * @return
     */
    public List<Integer> getFirstPais() {
        return firstPais;
    }



    /**
     * 移掉出的牌
     */
    void removeOutPais(PaohzCard card) {
        outPais.remove(card);
    }

    /**
     * 摸牌
     */
    public void moCard(PaohzCard card) {
        if(card.getVal()==0)
            return;
        this.outPais.add(card);
        ZhzTable t=(ZhzTable)table;
        t.getOutCards().add(card.getId());
        setFristDisCard(false);
    }

    public void moBoss(PaohzCard card) {
        this.handPais.add(card);
        changeSeat(seat);
    }

    void disCard(int action, List<PaohzCard> disCardList) {
        for (PaohzCard disCard : disCardList) {
            if (!handPais.remove(disCard)) {
                if (!zai.remove(disCard)) {
                    if (!peng.remove(disCard)) {
                        pao.remove(disCard);
                    }
                }
            }
        }

        if (disCardList.size() > 1) {
            PaohzCard card = disCardList.get(1);
            Iterator<CardTypeHuxi> iterator = cardTypes.iterator();
            while (iterator.hasNext()) {
                CardTypeHuxi type = iterator.next();
                if (type.isHasCard(card)) {
                    iterator.remove();
                }

            }
        }
        if(ting!=null)
            ting.clear();
        if (action == PaohzDisAction.action_ti) {
            ti.addAll(disCardList);
        } else if (action == PaohzDisAction.action_zai) {
            zai.addAll(disCardList);
        } else if (action == PaohzDisAction.action_chouzai) {
            chouZai.addAll(disCardList);
        } else if (action == PaohzDisAction.action_peng) {
            peng.addAll(disCardList);
        } else if (action == PaohzDisAction.action_chi) {
            chi.addAll(disCardList);
        } else if (action == PaohzDisAction.action_pao) {
            pao.addAll(disCardList);
            // oweCardCount--;
        } else {
            outPais.addAll(disCardList);
        }
        addCardType(action, disCardList);
        ZhzTable t=(ZhzTable)table;
        t.getOutCards().addAll(PaohuziTool.toPhzCardIds(disCardList));
        changeSeat(seat);
        changeTableInfo();
    }

    public void addCardType(int action, List<PaohzCard> disCardList) {
        if (action !=0) {
            CardTypeHuxi type = new CardTypeHuxi();
            type.setAction(action);
            type.setCardIds(PaohuziTool.toPhzCardIds(disCardList));
            cardTypes.add(type);
        }
    }

    public void pass(int action, int val, boolean addPassChi) {
        if (action == PaohzDisAction.action_chi) {
            if (addPassChi) {
                passChi.add(val);
            }
        } else if (action == PaohzDisAction.action_peng) {
            passPeng.add(val);
        } else {
            return;
        }
        changeTableInfo();
    }

    public void passPeng(int val) {
        passPeng.add(val);
        changeTableInfo();
    }

    public void disPass(int val){
        passChi.add(val);
        passPeng.add(val);
    }




    @Override
    public player_state getState() {
        return state;
    }

    @Override
    public int getIsEntryTable() {
        return isEntryTable;
    }

    @Override
    public void setIsEntryTable(int tableOnline) {
        this.isEntryTable = tableOnline;
        changeTableInfo();

    }

    @Override
    public int getSeat() {
        return seat;
    }

    @Override
    public void setSeat(int randomSeat) {
        seat = randomSeat;
    }

    boolean isCanDisCard(List<Integer> disCards, PaohzCard nowDisCard) {
        if (disCards != null) {
            if (nowDisCard != null && disCards.contains(nowDisCard.getId())) {
                List<Integer> copy = new ArrayList<>(disCards);
                // 排除掉在桌面上已经出的牌
                copy.remove((Integer) nowDisCard.getId());
                if (!getHandPais().containsAll(copy)) {
                    writeErrMsg("找不到牌:" + disCards);
                    return false;
                }
            } else {
                if (!getHandPais().containsAll(disCards)) {
                    writeErrMsg("找不到牌:" + disCards);
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isHu(PaohzCard card, ZhzTable table) {
        if(isSiShou())
            return false;
        return PaohuziTool.isHu(PaohuziTool.toPhzCardIds(handPais), card, this,table,true,false);
    }



    public List<PaohuziHuLack> checkHu(PaohzCard card,ZhzTable table) {
        if(isSiShou())
            return null;
        return PaohuziTool.getAllHu(PaohuziTool.toPhzCardIds(handPais), card, this, table);
    }
    /**
     * 得到可以操作的牌 4张和3张是不能操作的
     */
    PaohuziHandCard getPaohuziHandCard() {
        return PaohuziTool.getPaohuziHandCardBean(handPais);
    }

    /**
     * 拿到可以操作的牌
     *
     * @return
     */
    public List<PaohzCard> getOperateCards() {
        List<PaohzCard> copy = new ArrayList<>();
        Iterator<PaohzCard> iterator = handPais.iterator();
        while (iterator.hasNext()){
            PaohzCard card= iterator.next();
            if(!forbidDis.contains(card.getId())&&card.getVal()!=0){
                copy.add(card);
            }
        }
        return copy;
    }

    public PaohuziCheckCardBean checkCard(PaohzCard card, boolean isSelfMo, boolean isFirstCard) {
        return checkCard(card, isSelfMo, false, false, isFirstCard, false);
    }

    public PaohuziCheckCardBean checkCard(PaohzCard card, boolean isSelfMo, boolean isBegin, boolean isFirstCard) {
        return checkCard(card, isSelfMo, false, isBegin, isFirstCard, false);
    }

    public boolean canFangZhao(PaohzCard card) {

        if (!zai.contains(card)) {
            List<Integer> zaiVals = PaohuziTool.toPhzCardVals(zai, true);
            return zaiVals.contains(card.getVal());
        }

        return false;
    }

    /**
     * 检查牌
     * @param card
     *            出的牌或者是摸的牌
     * @param isSelfMo
     *            是否是自己摸的牌
     */
    public PaohuziCheckCardBean  checkCard(PaohzCard card, boolean isSelfMo, boolean isPassHu, boolean isBegin, boolean isFirstCard, boolean isPass) {
        List<PaohzCard> copy = new ArrayList<>(handPais);
        PaohuziCheckCardBean check = new PaohuziCheckCardBean();
        check.setSeat(seat);
        PaohuziHandCard cardBean = getPaohuziHandCard();
        boolean isCanChiPeng = true;
        int operateCount = cardBean.getOperateCards().size();
        if (operateCount ==0) {
            // 必须要出牌但是可以可操作的牌已经不够了
            isCanChiPeng = false;
        }
        ZhzTable table = getPlayingTable(ZhzTable.class);
        boolean isMoFlag = table.isMoFlag();
        if (card != null) {
            List<PaohzCard> sameList = PaohuziTool.getSameCards(copy, card);
            if (sameList.size() >= 2&&isCanChiPeng && !passPeng.contains(card.getVal())) {
                // 检查碰
                check.setPeng(true);
            }


            // 是否能检测能吃牌
            boolean isCheckChi = false;
            if (isMoFlag && table.getMoSeat() == seat) {
                // 如果这张牌是摸的,摸上来的人也能检测
                isCheckChi = true;
            }
            if (!isCheckChi && table.calcNextSeat(table.getDisCardSeat()) == seat) {
                // 不管怎么样，摸牌打出的下家是一定要检测的
                isCheckChi = true;
            }
            if (isCanChiPeng && isCheckChi && !isPass&&!passChi.contains(card.getVal())) {
                if (isCanChi(card)&&getOperateCards().size()>=2) {
                    check.setChi(true);
                }
            }

            //检测是否能胡
            if (!isPassHu&&table.getDisOrMo()==2) {
                // 检查胡
                boolean flag = isHu(card, table);
                if (flag) {
                    check.setHu(true);
                }
            }
            if (check.isPeng()) {
                List<PaohzCard> sameCards = getPengList(card, null);
                if (sameCards == null) {
                    System.out.println("check peng-->" + card);
                }
            }
        } else {
            // if (!isPassHu && !check.isZai() && !check.isTi()) {
            if (!isPassHu) {
                check.setHu(isHu(card, table));
            }
        }
        check.buildActionList();
        return check;
    }

    public PaohuziCheckCardBean checkBoss(PaohzCard card) {
        PaohuziCheckCardBean check = new PaohuziCheckCardBean();
        check.setSeat(seat);
        // 检查胡
        boolean flag = isHu(card, getPlayingTable(ZhzTable.class));
        if (flag) {
            check.setHu(true);
        }

        check.buildActionList();
        return check;
    }

    /**
     * 检测起手蝴蝶飞
     */
    public boolean checkHuDieFei(ZhzTable table){
        List<Integer> allCards=new ArrayList<>();
        allCards.addAll(PaohuziTool.toPhzCardIds(handPais));
        if(PaohuziMingTangRule.isHuDieFei(allCards)){
            table.setHuDieSeat(getSeat());
            PaohuziCheckCardBean check = new PaohuziCheckCardBean();
            check.setHu(true);
            check.buildActionList();
            check.setSeat(getSeat());
            table.checkPaohuziCheckCard(check);
            return true;
        }
        return false;
    }

    List<PaohzCard> getPengList(PaohzCard card, List<PaohzCard> cardList) {
        if (cardList != null) {
            // 吃牌不符合规则
            List<PaohzCard> gameList = PaohuziTool.getSameCards(cardList, card);
            if (gameList == null || gameList.isEmpty()) {
                return null;
            }
            if (gameList.size() != cardList.size()) {
                return null;
            }
        } else {
            cardList = PaohuziTool.getSameCards(PaohuziTool.toPhzCards(getHandPais()), card);
            if (cardList == null || cardList.size() < 2) {
                return null;
            }
        }
        return cardList.subList(0,2);
    }

    public boolean isCanChi(PaohzCard card,List<PaohzCard> cardList){
        List<PaohzCard> copy=new ArrayList<>(cardList);
        Iterator<PaohzCard> it = copy.iterator();
        while (it.hasNext()){
            PaohzCard next = it.next();
            if(next.getId()==card.getId())
                it.remove();
        }
        return getHandPais().containsAll(PaohuziTool.toPhzCardIds(copy));
    }

    public boolean isCanChi(PaohzCard otherCard){
        List<int[]> paiZus = PaohuziConstant.getChiZu(otherCard.getVal());
        if(paiZus==null)
            return false;
        for (int[] paiZu:paiZus) {
            int count=0;
            for (int val:paiZu) {
                for (PaohzCard card:handPais) {
                    if(card.getVal()==val&&card.getVal()!=otherCard.getVal()){
                        count++;
                        break;
                    }

                }
            }
            if(count==2)
                return true;
        }
        return false;
    }

    /**
     * 获取所有打听
     * @param table
     */
    public void getAllDisTing(ZhzTable table){
        if (table.getToPlayCardFlag()==1&&table.getNowDisCardSeat()==seat)
            idAndTing = PaohuziTool.checkDisTing(PaohuziTool.toPhzCardIds(handPais), this, table);
    }

    /**
     * 起手闲家听牌
     * @param table
     */
    public void getQishouTing(ZhzTable table){
        List<Integer> ting = PaohuziTool.checkTing(PaohuziTool.toPhzCardIds(handPais), this, table);
        if(ting!=null)
            this.ting=ting;
    }

    public void sendAllDisTing(){
        if(idAndTing!=null&&idAndTing.size()>0){
            DaPaiTingPaiRes.Builder tingInfo = DaPaiTingPaiRes.newBuilder();
            for (Map.Entry<Integer,List<Integer>> entry:idAndTing.entrySet()) {
                DaPaiTingPaiInfo.Builder t = DaPaiTingPaiInfo.newBuilder();
                t.setMajiangId(entry.getKey());
                for (Integer id:entry.getValue()){
                    t.addTingMajiangIds(id);
                }
                tingInfo.addInfo(t.build());
            }
            writeSocket(tingInfo.build());
        }
    }

    public void sendTing(){
        if(ting!=null&&ting.size()>0){
            TingPaiRes.Builder ting = TingPaiRes.newBuilder();
            for (Integer id : this.ting) {
                ting.addMajiangIds(id);
            }
            writeSocket(ting.build());
            ZhzTable t = getPlayingTable(ZhzTable.class);
            if(t.getBossOut()>0)
                t.setBossType(1);
        }
    }

    public List<Integer> getTing() {
        return ting;
    }

    public void setTing(List<Integer> ting) {
        this.ting = ting;
    }

    /**
     * 出完一张牌后听的牌
     * @param id
     */
    public void disTing(Integer id){
        if(idAndTing!=null){
            ting = idAndTing.get(id);
        }
        idAndTing.clear();
        sendTing();
    }

    public List<PaohzCard> getChiList(PaohzCard card, List<PaohzCard> cardList) {
        return null;
    }

    @Override
    public void changeState(player_state state) {
        this.state = state;
        changeTableInfo();

    }

    @Override
    public void initNext() {
        setHu(null);
        setPoint(0);
        setFristDisCard(false);
        setZaiHuxi(0);
        cardTypes.clear();
        handPais.clear();
        outPais.clear();
        peng.clear();
        chi.clear();
        pao.clear();
        zai.clear();
        chouZai.clear();
        ti.clear();
        passChi.clear();
        passPeng.clear();
        setOutHuxi(0);
        setHuxi(0);
        getPlayingTable().changeExtend();
        getPlayingTable().changeCards(seat);
        changeState(player_state.entry);
        setSiShou(false);
        setDisNum(0);
        forbidDis.clear();
        mt.clear();
        ting=new ArrayList<>();
        idAndTing=new HashMap<>();
        moNum=0;
    }

    public List<PaohzCard> getHandPhzs() {
        return handPais;
    }

    @Override
    public List<Integer> getHandPais() {
        return PaohuziTool.toPhzCardIds(handPais);
    }

    public List<Integer> getOutPais() {
        return PaohuziTool.toPhzCardIds(outPais);
    }

    public List<PaohzCard> getOutPaisCard() {
        return outPais;
    }


    /**
     * 出牌
     *
     * @return
     */
    public String buildOutPaiStr() {
        StringBuilder sb = new StringBuilder();
        List<Integer> outPais = getOutPais();
        sb.append(StringUtil.implode(outPais)).append(";");
        for (CardTypeHuxi huxi : cardTypes) {
            sb.append(huxi.toStr()).append(";");
        }

        return sb.toString();
    }

    /**
     * 手牌
     *
     * @return
     */
    public String buildHandPaiStr() {
        StringBuilder sb = new StringBuilder();
        List<Integer> outPais = getHandPais();
        List<Integer> tiPais = PaohuziTool.toPhzCardIds(ti);
        List<Integer> paoPais = PaohuziTool.toPhzCardIds(pao);
        List<Integer> zaiPais = PaohuziTool.toPhzCardIds(zai);
        List<Integer> pengPais = PaohuziTool.toPhzCardIds(peng);
        List<Integer> chiPais = PaohuziTool.toPhzCardIds(chi);
        List<Integer> chouZaiPais = PaohuziTool.toPhzCardIds(chouZai);

        sb.append(StringUtil.implode(outPais)).append(";");
        sb.append(StringUtil.implode(tiPais)).append(";");
        sb.append(StringUtil.implode(paoPais)).append(";");
        sb.append(StringUtil.implode(zaiPais)).append(";");
        sb.append(StringUtil.implode(pengPais)).append(";");
        sb.append(StringUtil.implode(chiPais)).append(";");
        sb.append(StringUtil.implode(chouZaiPais)).append(";");
        return sb.toString();
    }



    /**
     * 单局详情
     */
    public ClosingPhzPlayerInfoRes.Builder bulidOneClosingPlayerInfoRes() {
        ClosingPhzPlayerInfoRes.Builder res = ClosingPhzPlayerInfoRes.newBuilder();
        res.setUserId(userId + "");
        res.addAllCards(getHandPais());
        res.setName(name);
        res.setPoint(point);
        res.setTotalPoint(getTotalPoint());
//        res.setFinalPoint(getFinalPoint());
        res.setSeat(seat);
        res.setBopiPoint(0);
        if (!StringUtils.isBlank(getHeadimgurl())) {
            res.setIcon(getHeadimgurl());
        } else {
            res.setIcon("");
        }

        res.setSex(sex);
        return res;
    }

    public void addDisNum(){
        disNum++;
    }

    public void addMoNum(){
        moNum++;
    }

    /**
     * 总局详情
     *
     * @return
     */
    public ClosingPhzPlayerInfoRes.Builder bulidTotalClosingPlayerInfoRes() {
        ClosingPhzPlayerInfoRes.Builder res = bulidOneClosingPlayerInfoRes();
        res.setLostCount(lostCount);
        res.setWinCount(winCount);
        res.setMaxPoint(getMaxPoint());
        return res;
    }

    @Override
    public PlayerInTableRes.Builder buildPlayInTableInfo() {
        return buildPlayInTableInfo(0, false);
    }

    public PlayerInTableRes.Builder buildPlayInTableInfo(long lookUid, boolean isrecover) {
        PlayerInTableRes.Builder res = PlayerInTableRes.newBuilder();
        res.setUserId(userId + "");
        if (!StringUtils.isBlank(ip)) {
            res.setIp(ip);
        } else {
            res.setIp("");
        }

        ZhzTable table = getPlayingTable(ZhzTable.class);

        if (table == null) {
            LogUtil.e("userId="+this.userId+"table null-->" + getPlayingTableId());
            return null;
        }

        res.setPoint(getTotalPoint());
        // 当前出的牌
        res.addAllOutedIds(getOutPais());

        int outHuxi = getOutHuxi();
        outHuxi += zaiHuxi;
        res.addExt(outHuxi);// 0
        res.addExt(fangZhao);// 1
        res.addExt((table.getMasterId() == this.userId) ? 1 : 0);// 2
        res.addExt(0);// 3
        res.addExt(0);// 4
        res.addExt(table.isGoldRoom()?(int)loadAllGolds():0);// 5
        res.addExt(isAutoPlay() ? 1 : 0);// 6
        res.addExt(getAutoPlayCheckedTime() > table.getAutoTimeOut() ? 1 : 0);//7
        res.addExt(-1);//8
        res.addExt(0);//9
        res.addExt(0).addExt(0);
        res.addExt(-1);//12飘分
        res.setName(name);
        res.setSeat(seat);
        res.setSex(sex);

        res.addAllMoldCards(buildPhzCards(table,lookUid));

        List<PaohzCard> nowDisCard = table.getNowDisCardIds();
        if (table.getDisCardSeat() == seat) {
            if (nowDisCard != null && !nowDisCard.isEmpty()) {
                int selfMo = 0;
                if (table.isSelfMo(this)) {
                    selfMo = 1;

                }
                res.addOutCardIds(selfMo);
                PaohzCard zaiCard = table.getZaiCard();
                PaohzCard beremoveCard = table.getBeRemoveCard();
                if (zaiCard != null && nowDisCard.contains(zaiCard)) {
                    // 如果栽了牌
                } else if (beremoveCard != null && nowDisCard.contains(beremoveCard)) {
                    // 被移掉的牌
                } else {
                    res.addAllOutCardIds(PaohuziTool.toPhzCardIds(nowDisCard));

                }
            }

        }

        if (!StringUtils.isBlank(getHeadimgurl())) {
            res.setIcon(getHeadimgurl());

        } else {
            res.setIcon("");

        }

        if (state == player_state.ready || state == player_state.play) {
            // 玩家装备已经准备和正在玩的状态时通知前台已准备
            res.setStatus(PaohuziConstant.state_player_ready);
        } else {
            res.setStatus(0);
        }

        if (isrecover) {
            // 0是否要的起 1是否报单 2是否暂离(1暂离0在线)
            List<Integer> recover = new ArrayList<>();
            recover.add(isEntryTable);
            res.addAllRecover(recover);
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

    @Override
    public void initPais(String handPai, String outPai) {
        if (!StringUtils.isBlank(outPai)) {
            String[] values = outPai.split(";");
            int i = -1;

            for (String value : values) {
                i++;
                if (i == 0) {
                    this.outPais = PaohuziTool.explodePhz(value, ",");
                } else {
                    CardTypeHuxi type = new CardTypeHuxi();
                    type.init(value);
                    cardTypes.add(type);
                }
            }
        }
        if (!StringUtils.isBlank(handPai)) {
            String[] values = handPai.split(";");
            int i = 0;
            String outPaiStr = StringUtil.getValue(values, i++);
            String tiPaiStr = StringUtil.getValue(values, i++);
            String paoPaiStr = StringUtil.getValue(values, i++);
            String zaiPaiStr = StringUtil.getValue(values, i++);
            String pengPaiStr = StringUtil.getValue(values, i++);
            String chiPaiStr = StringUtil.getValue(values, i++);
            String chouZaiPaiStr = StringUtil.getValue(values, i++);
            this.handPais = PaohuziTool.explodePhz(outPaiStr, ",");
            this.ti = PaohuziTool.explodePhz(tiPaiStr, ",");
            this.pao = PaohuziTool.explodePhz(paoPaiStr, ",");
            this.zai = PaohuziTool.explodePhz(zaiPaiStr, ",");
            this.peng = PaohuziTool.explodePhz(pengPaiStr, ",");
            this.chi = PaohuziTool.explodePhz(chiPaiStr, ",");
            this.chouZai = PaohuziTool.explodePhz(chouZaiPaiStr, ",");

        }
    }

    @Override
    public void endCompetition1() {

    }

    public void dealHandPais(List<PaohzCard> list) {
        this.handPais = list;
        setFristDisCard(true);
        getPlayingTable().changeCards(seat);
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

    public int getLostPoint() {
        return lostPoint;
    }

    public void setLostPoint(int lostPoint) {
        this.lostPoint = lostPoint;
    }

    public List<Integer> getForbidDis() {
        return forbidDis;
    }

    public void setForbidDis(List<Integer> forbidDis) {
        this.forbidDis = forbidDis;
    }

    public int getPoint() {
        return point;
    }

    public void calcResult(BaseTable table,int count, int point, boolean huangzhuang) {
        if (!huangzhuang) {
            if (point > 0) {
                this.winCount += count;
            } else {
                this.lostCount += count;
            }
        }
        changePoint(point);
        if (table!=null&&table.isGoldRoom()){
            getGoldPlayer().changePlayCount();
            if(point > 0){
                getGoldPlayer().changeWinCount();
                if(!isRobot()){
                    GoldDao.getInstance().updateGoldUserCount(userId,1,0,0,1);
                }
            }else{
                getGoldPlayer().changeLoseCount();
                if(!isRobot()){
                    GoldDao.getInstance().updateGoldUserCount(userId,0,1,0,1);
                }
            }
        }
    }

    public void setPoint(int point) {
        this.point = point;
    }

    public void changePoint(int point) {
        this.point += point;
        myExtend.changePoint(getPlayingTable().getPlayType(), point);
        changeTotalPoint(point);
        if (point > getMaxPoint()) {
            setMaxPoint(point);
        }
        changeTableInfo();
    }

    public void addHuNum(List<Integer> mt){
        if(mt!=null&&mt.size()>0){
            setDaHuNum(getDaHuNum()+1);
        }else {
            setXiaoHuNum(getXiaoHuNum()+1);
        }
    }

    public int getDaHuNum() {
        return daHuNum;
    }

    public void setDaHuNum(int daHuNum) {
        this.daHuNum = daHuNum;
    }

    public int getXiaoHuNum() {
        return xiaoHuNum;
    }

    public void setXiaoHuNum(int xiaoHuNum) {
        this.xiaoHuNum = xiaoHuNum;
    }

    public int getOutHuxi() {
        return outHuXi;
    }

    public void setOutHuxi(int chiHuxi) {
        this.outHuXi = chiHuxi;
        changeTableInfo();
    }

    public void changeOutCardHuxi(int huxi) {
        if (huxi != 0) {
            this.outHuXi += huxi;
            changeTableInfo();
            // writeErrMsg("出的牌总胡息:" + this.outHuXi + "加的胡息:" + huxi);
        }
    }

    public int getZaiHuxi() {
        return zaiHuxi;
    }

    public void setZaiHuxi(int zaiHuxi) {
        this.zaiHuxi = zaiHuxi;
    }

    public void changeZaihu(int zaiHuxi) {
        this.zaiHuxi += zaiHuxi;
        changeTableInfo();
    }

    public boolean isSiShou() {
        return siShou;
    }

    public void setSiShou(boolean siShou) {
        this.siShou = siShou;
    }


    public int getHuxi() {
        return huxi;
    }

    public void setHuxi(int huxi) {
        if (this.huxi != huxi) {
            this.huxi = huxi;
        }
    }

    public List<Integer> getMt() {
        return mt;
    }

    public void setMt(List<Integer> mt) {
        this.mt = mt;
    }

    /**
     * 总胡息
     *
     * @return
     */
    public int getTotalHu() {
        return outHuXi + huxi + zaiHuxi;
    }

    public List<PhzHuCards> buildPhzHuCards() {
        List<PhzHuCards> list = new ArrayList<>();
        for (CardTypeHuxi type : cardTypes) {
            list.add(type.buildMsg(true).build());
        }
        if (hu != null && hu.getPhzHuCards() != null) {
            for (CardTypeHuxi type : hu.getPhzHuCards()) {
                list.add(type.buildMsg(false).build());
            }
        }

        return list;
    }

    public List<PhzHuCards> buildPhzCards(ZhzTable table,long lookUid) {
        List<PhzHuCards> list = new ArrayList<>();
        for (CardTypeHuxi type : cardTypes) {
            list.add(type.buildMsg(true).build());
        }

        return list;
    }

    public PaohuziHuLack getHu() {
        return hu;
    }

    public void setHu(PaohuziHuLack hu) {
        this.hu = hu;
    }

    public static void main(String[] args) {
        List<Integer> list1 = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6));
        System.out.println(list1.subList(0, 3));
        System.out.println(list1);
    }

    public boolean isFristDisCard() {
        return isFristDisCard;
    }

    public void setFristDisCard(boolean isFristDisCard) {
        if (this.isFristDisCard != isFristDisCard) {
            this.isFristDisCard = isFristDisCard;
            changeTableInfo();
        }
    }

    public List<Integer> getPassChi() {
        return passChi;
    }

    public void removePassChi(int val) {
        if (passChi.contains(val)) {
            passChi.remove(Integer.valueOf(val));
            changeTableInfo();
        }
    }
    @Override
    public int loadScore(){
        ZhzTable table = getPlayingTable(ZhzTable.class);
        if(table.isGoldRoom()){
            return getPoint();
        }else{
            return getTotalPoint();
        }
    }

    public boolean isCheckAuto() {
        return isCheckAuto;
    }

    public void setCheckAuto(boolean checkAuto) {
        isCheckAuto = checkAuto;
    }

    public static final List<Integer> wanfaList = Arrays.asList(GameUtil.game_type_zhz);

    public static void loadWanfaPlayers(Class<? extends Player> cls){
        for (Integer integer:wanfaList){
            PlayerManager.wanfaPlayerTypesPut(integer,cls, PaohuziCommandProcessor.getInstance());
        }
    }

}
