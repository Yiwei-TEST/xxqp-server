package com.sy599.game.qipai.lyzp.been;

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
import com.sy599.game.qipai.lyzp.command.PaohuziCommandProcessor;
import com.sy599.game.qipai.lyzp.constant.EnumHelper;
import com.sy599.game.qipai.lyzp.constant.PaohuziConstant;
import com.sy599.game.qipai.lyzp.constant.PaohzCard;
import com.sy599.game.qipai.lyzp.rule.PaohuziIndex;
import com.sy599.game.qipai.lyzp.rule.PaohuziMingTangRule;
import com.sy599.game.qipai.lyzp.rule.PaohzCardIndexArr;
import com.sy599.game.qipai.lyzp.tool.PaohuziHuLack;
import com.sy599.game.qipai.lyzp.tool.PaohuziTool;
import com.sy599.game.util.GameUtil;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class LyzpPlayer extends Player {
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
    private int oweCardCount;// 每出4张牌一次 就欠一张牌
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
    //提额外分
    private int tiFen=0;
    //结算时输赢提分
    private int winLoseTiFen=0;
    //出牌数量，只计算吃碰跑提栽之后的单出牌
    private int disNum=0;
    //吃边打边，限制吃（吃红打黑只能胡红黑堂）
    private List<Integer> limitChiCardIds=new ArrayList<>();
    //吃边打边,不能吃黑打黑
    private List<Integer> forbidChiCardIds=new ArrayList<>();
    //出发吃边打边，只能胡红黑堂
    private int onlyHht=0;
    //过胡分数
    private int passHuNum=0;
    //标记吃遍打边时的出牌数量
    private int cbdbNum=0;
    public LyzpPlayer() {
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
            this.oweCardCount = StringUtil.getIntValue(values, i++);
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
            String  forbidS= StringUtil.getValue(values, i++);
            if (!StringUtils.isBlank(firstPaisStr)) {
                forbidChiCardIds = StringUtil.explodeToIntList(forbidS, "_");
                if(forbidChiCardIds==null)
                    forbidChiCardIds=new ArrayList<>();
            }
            String limitS = StringUtil.getValue(values, i++);
            if (!StringUtils.isBlank(firstPaisStr)) {
                limitChiCardIds = StringUtil.explodeToIntList(limitS, "_");
                if(limitChiCardIds==null)
                    limitChiCardIds=new ArrayList<>();
            }
            onlyHht=StringUtil.getIntValue(values,i++);
            disNum=StringUtil.getIntValue(values,i++);
            tiFen=StringUtil.getIntValue(values,i++);
            passHuNum=StringUtil.getIntValue(values,i++);
            cbdbNum=StringUtil.getIntValue(values,i++);
        }
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
        setOweCardCount(0);
        setWinCount(0);
        setLostCount(0);
        setPoint(0);
        setLostPoint(0);
        setTotalPoint(0);
        setMaxPoint(0);
        setSeat(0);
        setFangZhao(0);
        if (!isCompetition) {
            setPlayingTableId(0);
        }
        actionTotalArr = new int[4];

        if(table.isAutoPlay() && autoPlay){
            setAutoPlay(false,table);
        }
        saveBaseInfo();
        setSiShou(false);
        setDisNum(0);
        setTiFen(0);
        clearCbdb();
        setOnlyHht(0);
        setWinLoseTiFen(0);
        setPassHuNum(0);
        setCbdbNum(0);
        setZaiTiHu(false);
    }

    public void clearCbdb(){
        forbidChiCardIds.clear();
        limitChiCardIds.clear();
    }

    /**
     * 找出所有可能吃边打边的牌
     * @param card
     */
    public void chiBianDaBian(PaohzCard card,List<PaohzCard> list){
        if(list.size()%3!=0)
            return;
        boolean flag=list.size()>3;
        for (int i = 0; i < list.size() / 3; i++) {
            List<PaohzCard> l = list.subList(0 + i * 3, (i + 1) * 3);
            List<Integer> cbdbChi = new ArrayList<>();
            for (int j = 0; j < l.size(); j++) {
                cbdbChi.add(l.get(j).getId());
            }
            int type=findCbdbType(cbdbChi);
            addCbdbCards(type,card,cbdbChi,flag);
        }
    }

    private void addCbdbCards(int type,PaohzCard card,List<Integer> cbdbChi,boolean over3){
        int val=card.getVal();
        List<PaohzCard> cards;
        if(type==0){
            //大小吃
            int val1=0;
            if(val>10){
                val1=val-100;
            }else {
                val1=val+100;
            }
            cards = PaohzCard.getPaohzCardsByVal(val1);
            for (int i = 0; i < cards.size(); i++) {
                forbidChiCardIds.add(cards.get(i).getId());
            }
        }else if(type==1){
            //顺子吃
            if(PaohzCard.getPaohzCard(cbdbChi.get(0)).getVal()==val){
                //吃的牌在第一个
                if(val%100==1)
                    return;
                int val3=val+3;
                boolean flag=false;
                if((val<=10&&val3<=10)||(val>100&&val3<=110)){
                    cards=PaohzCard.getPaohzCardsByVal(val3);
                    switch (val%100){
                        case 2:
                        case 4:
                            flag=true;
                            break;
                    }
                    if (flag||over3){
                        for (int i = 0; i < cards.size(); i++) {
                            limitChiCardIds.add(cards.get(i).getId());
                        }
                    }else {
                        for (int i = 0; i < cards.size(); i++) {
                            forbidChiCardIds.add(cards.get(i).getId());
                        }
                    }
                }
            }else if(PaohzCard.getPaohzCard(cbdbChi.get(2)).getVal()==val){
                //吃的牌在第三个
                int val2=val-3;
                boolean flag=false;
                if((val<=10&&val2>0)||(val>100&&val2>100)){
                    cards=PaohzCard.getPaohzCardsByVal(val2);
                    switch (val%100){
                        case 4:
                        case 5:
                        case 7:
                            flag=true;
                            break;
                    }
                    if (flag||over3){
                        for (int i = 0; i < cards.size(); i++) {
                            limitChiCardIds.add(cards.get(i).getId());
                        }
                    }else {
                        for (int i = 0; i < cards.size(); i++) {
                            forbidChiCardIds.add(cards.get(i).getId());
                        }
                    }
                }
            }
        }
    }

    private int findCbdbType(List<Integer> cbdbChi){
        //先进行排序
        for (int i = 0; i < cbdbChi.size(); ++i) {
            // 提前退出冒泡循环的标志位,即一次比较中没有交换任何元素，这个数组就已经是有序的了
            boolean flag = false;
            for (int j = 0; j < cbdbChi.size() - i - 1; ++j) {        //此处你可能会疑问的j<n-i-1，因为冒泡是把每轮循环中较大的数飘到后面，
                // 数组下标又是从0开始的，i下标后面已经排序的个数就得多减1，总结就是i增多少，j的循环位置减多少
                if ((cbdbChi.get(j)%10 > cbdbChi.get(j+1)%10)||cbdbChi.get(j)%10==0) {        //即这两个相邻的数是逆序的，交换
                    int temp = cbdbChi.get(j);
                    cbdbChi.set(j,cbdbChi.get(j+1));
                    cbdbChi.set(j+1,temp);
                    flag = true;
                }
            }
            if (!flag) break;//没有数据交换，数组已经有序，退出排序
        }

        //0为大小吃，1为顺子吃，-1有误
        List<PaohzCard> list=new ArrayList<>();
        for (int i = 0; i <cbdbChi.size(); i++) {
            list.add(PaohzCard.getPaohzCard(cbdbChi.get(i)));
        }
        int type=cbdbChi.get(1)%10-cbdbChi.get(0)%10;
        int n=cbdbChi.get(2)%10;
        if(n==0)
            n=10;
        if(type==n-cbdbChi.get(1)%10){
            return type;
        }else {
            return -1;
        }
    }

//    private List<Integer> findCbdbShun(int id,List<PaohzCard> list){
//        List<Integer> shun=new ArrayList<>();
//        boolean flag=false;
//        for (int i = 0; i < list.size() / 3; i++) {
//            if(!flag){
//                List<PaohzCard> l = list.subList(i * 3, i * 3 + 3);
//                for (int j = 0; j < 3; j++) {
//                    PaohzCard card = l.get(j);
//                    shun.add(card.getId());
//                    if(card.getId()==id){
//                        flag=true;
//                    }
//                }
//                if(!flag){
//                    shun.clear();
//                }else {
//                    break;
//                }
//
//            }
//        }
//        return shun;
//    }



    public int getDisNum() {
        return disNum;
    }

    public void setDisNum(int disNum) {
        this.disNum = disNum;
    }

    public List<Integer> getLimitChiCardIds() {
        return limitChiCardIds;
    }

    public void setLimitChiCardIds(List<Integer> limitChiCardIds) {
        this.limitChiCardIds = limitChiCardIds;
    }

    public List<Integer> getForbidChiCardIds() {
        return forbidChiCardIds;
    }

    public void setForbidChiCardIds(List<Integer> forbidChiCardIds) {
        this.forbidChiCardIds = forbidChiCardIds;
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

    public int getTiFen() {
        return tiFen;
    }

    public void setTiFen(int tiFen) {
        this.tiFen = tiFen;
    }

    public int getWinLoseTiFen() {
        return winLoseTiFen;
    }

    public void setWinLoseTiFen(int winLoseTiFen) {
        this.winLoseTiFen = winLoseTiFen;
    }

    public int getPassHuNum() {
        return passHuNum;
    }

    public void setPassHuNum(int passHuNum) {
        this.passHuNum = passHuNum;
    }

    public int getCbdbNum() {
        return cbdbNum;
    }

    public void setCbdbNum(int cbdbNum) {
        this.cbdbNum = cbdbNum;
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
        this.outPais.add(card);
        setFristDisCard(false);
    }

    public void tiLong(PaohzCard card) {
        this.handPais.add(card);
        compensateCard();
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
                    if (type.getAction() == PaohzDisAction.action_zai) {
                        // 去掉栽的息
                        changeZaihu(-type.getHux());

                    } else {
                        changeOutCardHuxi(-type.getHux());

                    }
                    iterator.remove();
                }

            }
        }

        if (disCardList.size() == 4) {
            oweCardCount--;
        }

        if (action == PaohzDisAction.action_ti) {
            ti.addAll(disCardList);
            //耒阳字牌提分
            addTiFen(disCardList);
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
            int passVal = disCardList.get(0).getVal();
            passChi.add(passVal);
            passPeng.add(passVal);
        }
        //这行代码和重跑有关，保证重跑不出错
        if (action != PaohzDisAction.action_ti) {
            setFristDisCard(false);
        }
        if (disCardList.size() > 3 && disCardList.size() % 3 == 0) {
            for (int i = 0; i < disCardList.size() / 3; i++) {
                List<PaohzCard> list = disCardList.subList(i * 3, i * 3 + 3);
                addCardType(action, list);
            }

        } else {
            addCardType(action, disCardList);
        }
        changeSeat(seat);
        changeTableInfo();
    }

    public void addTiFen(List<PaohzCard> disCardList){
        if (disCardList.size()!=4){
            return;
        }
        int val=disCardList.get(0).getVal();
        if(val>100&&val<=110){
            tiFen+=2;
        }else if(val>0&&val<=10){
            tiFen++;
        }
    }

    public void addCardType(int action, List<PaohzCard> disCardList) {
        int huxi = PaohuziTool.getOutCardHuxi(action, disCardList);
        if (action != 0) {
            CardTypeHuxi type = new CardTypeHuxi();
            type.setAction(action);
            type.setCardIds(PaohuziTool.toPhzCardIds(disCardList));
            type.setHux(huxi);
            cardTypes.add(type);
        }
        if (action == PaohzDisAction.action_zai) {
            changeZaihu(huxi);
        } else {
            changeOutCardHuxi(huxi);

        }
    }

    public boolean isCanExec(int action, List<PaohzCard> cards) {
        if (action == PaohzDisAction.action_pao) {

        }
        return true;
    }

    public List<PaohzCard> getSameCards(PaohzCard card) {
        List<PaohzCard> list = new ArrayList<>();
        List<PaohzCard> zaiList = PaohuziTool.findPhzByVal(zai, card.getVal());
        list.addAll(zaiList);
        List<PaohzCard> paoList = PaohuziTool.findPhzByVal(handPais, card.getVal());
        list.addAll(paoList);
        List<PaohzCard> pengList = PaohuziTool.findPhzByVal(peng, card.getVal());
        list.addAll(pengList);
        return list;
    }

    /**
     * 是否需要出牌
     *
     * @return
     */
    public boolean isNeedDisCard(int action) {
        // boolean isFrist
//		int ct = isFristDisCard() ? handPais.size() % 3 : handPais.size()+1 % 3;
//        if (!ti.isEmpty() || !pao.isEmpty()) {
//            return ct == 2;
//        } else {
//            return ct == 0;
//        }
        return isFristDisCard() || oweCardCount >= -1;
    }

    /**
     *
     */
    public void compensateCard() {
        oweCardCount++;
        changeTableInfo();
    }

    public void pass(int action, int val) {
        pass(action, val, false);
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

    public void removeLastPassChi(int val){
        int size = passChi.size();
        if(size>=1&&passChi.get(size-1)==val){
            passChi.remove(size-1);
            changeTableInfo();
        }
    }

    public int getTiSize(){
        return (ti.size()+pao.size())/4;
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


    /**
     * 不加入能跑的牌是否能胡
     */
    public List<PaohuziHuLack> checkPaoHu1(PaohzCard card, boolean isSelfMo, boolean canDiHu) {
        int addhuxi = 0;
        int action = 0;
        boolean isNeedDui = isNeedDui();
//        if(!isNeedDui)
//            return null;
        LyzpTable table = getPlayingTable(LyzpTable.class);
        //这个起手提需要关注一下，改错了
        boolean isQishouTi=disNum==0&&isTiContian(card);
        PaohzCard disCard=null;
        if(isQishouTi)
            disCard=card;

        List<PaohzCard> removeList=new ArrayList<>();
        List<PaohzCard> handCopy = new ArrayList<>(handPais);
        if (card != null) {
            boolean isMoFlag = table.isMoFlag();
            List<PaohzCard> sameList = PaohuziTool.getSameCards(handPais, card);
            if (isSelfMo) {
                sameList.remove(card);
            }
            if (sameList.size() >= 3) {
                if (isSelfMo) {
                    action = PaohzDisAction.action_ti;
                } else {
                    action = PaohzDisAction.action_pao;
                }
            }else if(sameList.size()>0&&sameList.size()<3)
                return null;
            List<Integer> zaiVals = PaohuziTool.toPhzCardVals(zai, true);
            if (isMoFlag && !peng.contains(card) && PaohuziTool.isHasCardVal(peng, card.getVal())) {
                // 检查是否碰过的牌 跑过的牌能跑的话必须要是摸的牌
                isNeedDui = true;
                if (card.isBig()) {
                    // 大的9 小的6 碰大的3 小的1
                    addhuxi = 9 - 3;
                } else {
                    addhuxi = 6 - 1;
                }
                action = PaohzDisAction.action_pao;
            } else if (zaiVals.contains(card.getVal())) {
                // 如果是栽过的牌别人打的或者摸的能提或者跑
                isNeedDui = true;
                if(zai.contains(card))
                    return null;
                if (isSelfMo) {
                    // 如果是栽过的牌自己再摸一张上来就是提
                    // 大的12 小的9 栽大的6 小的3
                    action = PaohzDisAction.action_ti;
                    if (card.isBig()) {
                        addhuxi = 12 - 6;
                    } else {
                        addhuxi = 9 - 3;
                    }

                } else {
                    // 别人打的或摸的就是跑
                    action = PaohzDisAction.action_pao;
                    if (card.isBig()) {
                        // 大的9 小的6 栽大的6 小的3
                        addhuxi = 9 - 6;
                    } else {
                        addhuxi = 6 - 3;
                    }
                }
            } else if (isMoFlag || canDiHu) {
                // 在手上的牌
                List<PaohzCard> sameCard = PaohuziTool.getSameCards(handCopy, card);
                if (sameCard.size() >= 3) {
                    if (card.isBig()) {
                        // 大的9 小的6
                        addhuxi = 9;
                    } else {
                        addhuxi = 6;
                    }
                    handCopy.removeAll(sameCard);
                    removeList.addAll(sameCard);
                    removeList.add(card);
                    disCard=null;
                }
            }
        }
        if (action == PaohzDisAction.action_ti||action== PaohzDisAction.action_pao)
            isNeedDui=true;
        List<PaohuziHuLack> list = PaohuziTool.isHuNew1(PaohuziTool.getPaohuziHandCardBean(handCopy), disCard, isSelfMo,  isNeedDui, true);
        if(list==null)
            return null;

        Iterator<PaohuziHuLack> it = list.iterator();
        while(it.hasNext()){
            PaohuziHuLack lack = it.next();
            if (action != 0||card==null) {
                lack.setPaohuAction(action);
                List<PaohzCard> paohuList = new ArrayList<>();
                if(card!=null){
                    paohuList.add(card);
                    lack.setPaohuList(paohuList);
                    List<PaohzCard> paoHuCards = getSameCards(card);
                    paoHuCards.addAll(paoHuCards);
                    if(paoHuCards.size() != 4){
                        paoHuCards.add(card);
                    }
                    lack.setPaoHuCards(paoHuCards);
                }
                if(removeList.size()>0){
                    lack.addPhzHuCards(action,PaohuziTool.toPhzCardIds(removeList),0);
                }
                lack.setHuxi(lack.calcHuxi()+addhuxi);
                if(action==PaohzDisAction.action_pao)
                    table.setPaoHu(1);
            }else {
                it.remove();
            }
        }
        return list;
    }

    public boolean isTiContian(PaohzCard card){
        if (ti==null||ti.isEmpty())
            return false;
        if (ti.contains(card))
            return true;
        return false;
    }

    private boolean isNeedDui() {
        return !ti.isEmpty() || !pao.isEmpty();
    }

    public PaohuziHuLack checkHuNew(PaohzCard card, boolean isSelfMo) {
        PaohuziHuLack lack=new PaohuziHuLack();
        if(isSiShou())
            return lack;
        List<PaohzCard> handCopy = new ArrayList<>(handPais);
        boolean isPaoHu = true;
        if (card != null && !handCopy.contains(card)) {
            handCopy.add(card);
            isPaoHu = false;
        }
        List<PaohuziHuLack> list = PaohuziTool.isHuNew1(PaohuziTool.getPaohuziHandCardBean(handCopy), card, isSelfMo, isNeedDui(), isPaoHu);
        for (PaohuziHuLack h:list) {
            h.setHuxi(h.calcHuxi());
        }
        LyzpTable table = getPlayingTable(LyzpTable.class);
        PaohuziHuLack hu = table.filtrateHu(this, null, list);
        if(hu!=null)
            return hu;
//        if (list!=null&&!list.isEmpty()){
//            for(PaohuziHuLack hu:list){
//                hu.setHuxi(hu.calcHuxi());
//                int allHuxi = hu.getHuxi() + outHuXi + zaiHuxi;
//                if(onlyHht==0){
//                    if (allHuxi >= table.getFloorValue()||(table.getNoWuHu()==0&&allHuxi==0))
//                        return hu;
//                }else{
//                    List<PaohzCard> allCards=new ArrayList<>();
//                    allCards.addAll(handPais);
//                    if(card!=null)
//                        allCards.add(card);
//                    for (CardTypeHuxi type :getCardTypes()) {
//                        allCards.addAll(PaohuziTool.toPhzCards(type.getCardIds()));
//                    }
//                    if(PaohuziMingTangRule.calcSimpleHht(allCards,table,allHuxi)){
//                        setHuxi(hu.getHuxi());
//                        return hu;
//                    }
//                }
//            }
//        }
        return lack;
    }



    public List<PaohuziHuLack> checkHu1(PaohzCard card, boolean isSelfMo) {
        List<PaohzCard> handCopy = new ArrayList<>(handPais);
        boolean isPaoHu = true;
        if (card != null && !handCopy.contains(card)) {
            handCopy.add(card);
            isPaoHu = false;
        }
        List<PaohuziHuLack> list = PaohuziTool.isHuNew1(PaohuziTool.getPaohuziHandCardBean(handCopy), card, isSelfMo, isNeedDui(), isPaoHu);
        if(list!=null&&!list.isEmpty()){
            for (PaohuziHuLack lack:list){
                lack.setHuxi(lack.calcHuxi());
            }
        }
        return list;
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
        List<PaohzCard> copy = new ArrayList<>(handPais);
        Map<Integer, Integer> valMap = PaohuziTool.toPhzValMap(copy);
        for (Entry<Integer, Integer> entry : valMap.entrySet()) {
            if (entry.getValue() >= 3) {
                PaohuziTool.removePhzByVal(copy, entry.getKey());
            }
        }
        return copy;

    }

    public List<PhzHuCards> buildNormalPhzHuCards() {
        List<PhzHuCards> list = new ArrayList<>();
        for (CardTypeHuxi type : cardTypes) {
            list.add(type.buildMsg().build());
        }

        if (hu != null && hu.getPhzHuCards() != null) {
            for (CardTypeHuxi type : hu.getPhzHuCards()) {
                list.add(type.buildMsg().build());
            }
        } else {
            PhzHuCards.Builder msg = PhzHuCards.newBuilder();
            msg.addAllCards(handPais.stream().map(v -> v.getId()).collect(Collectors.toList()));
            msg.setAction(0);
            msg.setHuxi(0);
            list.add(msg.build());
        }
        return list;
    }

    public PaohuziCheckCardBean checkCard(PaohzCard card, boolean isSelfMo, boolean isFirstCard) {
        return checkCard(card, isSelfMo, false, false, isFirstCard, false);
    }

    public PaohuziCheckCardBean checkCard(PaohzCard card, boolean isSelfMo, boolean isBegin, boolean isFirstCard) {
        return checkCard(card, isSelfMo, false, isBegin, isFirstCard, false);
    }

    public PaohuziCheckCardBean checkPaoHu(int action) {
        PaohuziCheckCardBean check = new PaohuziCheckCardBean();
        check.setSeat(seat);
        PaohuziHuLack lack = checkHuNew(null, false);
        if (lack.isHu()) {
            check.setLack(lack);
            check.setHu(true);
            setZaiTiHu(true);
        }
        check.buildActionList();
        return check;
    }

    /**
     * 检查提
     *
     * @return
     */
    public PaohuziCheckCardBean checkTi() {
        PaohuziCheckCardBean check = new PaohuziCheckCardBean();
        check.setSeat(seat);
        PaohuziHandCard cardBean = getPaohuziHandCard();
        PaohzCardIndexArr valArr = cardBean.getIndexArr();
        PaohuziIndex index3 = valArr.getPaohzCardIndex(3);
        if (index3 != null) {
            check.setAuto(PaohzDisAction.action_ti, index3.getPaohzList());
            check.setTi(true);
        }
        check.buildActionList();
        return check;
    }

    /**
     * 已经提，跑，栽固定了的出了的牌值
     *
     * @return
     */
    public List<Integer> getFixedOutVals() {
        // 提，跑，栽
        List<PaohzCard> phzs = new ArrayList<>();
        phzs.addAll(chouZai);
        phzs.addAll(zai);
        phzs.addAll(pao);
        phzs.addAll(ti);
        return PaohuziTool.toPhzRepeatVals(phzs);
    }

    public boolean canFangZhao(PaohzCard card) {

        if (!zai.contains(card)) {
            List<Integer> zaiVals = PaohuziTool.toPhzCardVals(zai, true);
            if (zaiVals.contains(card.getVal())) {
                return true;
            }
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
        check.setDisCard(card);
        PaohuziHandCard cardBean = getPaohuziHandCard();
        boolean isCanChiPeng = true;
        int operateCount = cardBean.getOperateCards().size();
        if (operateCount < 4 && oweCardCount >= -1) {
            // 必须要出牌但是可以可操作的牌已经不够了
            isCanChiPeng = false;
        }

        PaohzCardIndexArr valArr = cardBean.getIndexArr();
        PaohuziIndex index3 = valArr.getPaohzCardIndex(3);
        if (isSelfMo) {
            // 检查提
            boolean isMoTi = false;
            if (index3 != null && card != null) {
                for (int val : index3.getPaohzValMap().keySet()) {
                    if (val == card.getVal()) {
                        isMoTi = true;
                        break;
                    }
                }

            }
            if (index3 != null && (isBegin || isMoTi)) {
                check.setAuto(PaohzDisAction.action_ti, index3.getPaohzList());
                check.setTi(true);
            }
        }
        LyzpTable table = getPlayingTable(LyzpTable.class);
        boolean isMoFlag = table.isMoFlag();
        if (card != null) {
            List<Integer> fixdOutVals = getFixedOutVals();
            // 有没有一样的牌
            List<PaohzCard> sameList = PaohuziTool.getSameCards(copy, card);
            if (isSelfMo) {
                sameList.remove(card);
            }
            if (sameList != null) {
                if (sameList.size() == 3) {
                    if (isSelfMo) {
                        // !!!!不可能出现这种情况
                        // 检查栽 如果从墩上摸上来的那张牌 自己可以偎时，这张牌是不能让其他人看到的
                        check.setAuto(PaohzDisAction.action_ti, sameList);
                        check.setTi(true);

                    } else {
                        // 在手里面不管是摸的还是出的牌都能跑
                        // 检查跑
                        check.setAuto(PaohzDisAction.action_pao, sameList);
                        check.setPao(true);
                    }

                } else if (sameList.size() == 2) {
                    if (isSelfMo) {
                        // 玩家手中有一对牌，当自已从墩上摸起一只相同的牌，玩家必须将手中的一对连同摸起的牌放置于桌面，且不能明示给其它玩家，称为栽/偎
                        // 看看是否是臭栽
                        if (passPeng.contains(card.getVal())) {
                            check.setAuto(PaohzDisAction.action_chouzai, sameList);
                            check.setChouZai(true);
                        } else {
                            check.setAuto(PaohzDisAction.action_zai, sameList);
                            check.setZai(true);
                        }

                    } else {
                        if (isCanChiPeng && !passPeng.contains(card.getVal()) && !isFangZhao()) {
                            // 检查碰
                            check.setPeng(true);
                        }

                    }
                }
            }
            // 去掉4张和3张
            PaohuziIndex index2 = valArr.getPaohzCardIndex(2);
            if (index3 != null) {
                copy.removeAll(index3.getPaohzList());
            }
            if (index2 != null) {
                copy.removeAll(index2.getPaohzList());
            }
            // 检查是否碰过的牌 跑过的牌能跑的话必须要是摸的牌
            if (isMoFlag && PaohuziTool.isHasCardVal(peng, card.getVal())) {
                check.setAuto(PaohzDisAction.action_pao, new ArrayList<>(Arrays.asList(card)));
                check.setPao(true);

            }

            if (!zai.contains(card)) {
                List<Integer> zaiVals = PaohuziTool.toPhzCardVals(zai, true);
                if (zaiVals.contains(card.getVal())) {
                    if (isSelfMo) {
                        // 如果是栽过的牌自己再摸一张上来就是提
                        check.setAuto(PaohzDisAction.action_ti, new ArrayList<>(Arrays.asList(card)));
                        check.setTi(true);
                    } else {
                        // 别人打的或摸的就是跑
                        check.setAuto(PaohzDisAction.action_pao, new ArrayList<>(Arrays.asList(card)));
                        check.setPao(true);
                    }

                }
            }


            // 是否能检测能吃牌
            boolean isCheckChi = false;

            // 不是已经提跑栽过的牌
            if (!fixdOutVals.contains(card.getVal())) {
                if (isMoFlag && table.getMoSeat() == seat) {
                    // 如果这张牌是摸的,摸上来的人也能检测
                    isCheckChi = true;
                }
                if (!isCheckChi && table.calcNextSeat(table.getDisCardSeat()) == seat) {
                    // 不管怎么样，摸牌打出的下家是一定要检测的
                    isCheckChi = true;
                }
            }

            // 放招之后不能吃碰
            if (isFangZhao()) {
                isCheckChi = false;
            }

            // 必须在不能跑的情况下
            if (isCanChiPeng && !check.isTi() && !check.isPao() && isCheckChi && !isPass) {

                LyzpPlayer frontPlayer = (LyzpPlayer) table.getPlayerBySeat(table.calcFrontSeat(seat));
                List<PaohzCard> frontOutPaisCard = frontPlayer.getOutPaisCard();
                List<Integer> frontOutPais = PaohuziTool.toPhzCardVals(frontOutPaisCard, true);
                List<Integer> selfOutVals = PaohuziTool.toPhzCardVals(outPais, true);
                // System.out.println("前面过的牌:" + frontOutPais + "自己过的牌:" + selfOutVals + ",名字->" + name);

                List<Integer> passChiList = new ArrayList<>();
                if (isMoFlag) {
                    if (isSelfMo && !selfOutVals.isEmpty()) {// 自己摸的牌
                        selfOutVals.remove((int) (selfOutVals.size() - 1));
                    } else {// 别人摸的牌
                        frontOutPais.remove((int) (frontOutPais.size() - 1));
                    }
                } else {// 出的牌
                    frontOutPais.remove((int) (frontOutPais.size() - 1));
                }
                passChiList.addAll(frontOutPais);
                passChiList.addAll(selfOutVals);
                // System.out.println(name + "所有过掉的牌:" + passChiList);
                if (!passChiList.contains(card.getVal()) && !passChi.contains(card.getVal())) {
                    List<PaohzCard> chiList = getChiList(card, null);
                    if (chiList != null && !chiList.isEmpty()) {
                        check.setChi(true);
                    } else{
                        // 不能吃后就再也不能吃了
                        if (!check.isPeng() && !check.isZai() && !check.isChouZai()) {
                            // pass(PaohzDisAction.action_chi, card.getVal());
                            passChi.add(card.getVal());
                        }
                    }
                }

            }

            List<PaohuziHuLack> pingHu=null;
            List<PaohuziHuLack> paoHu=null;
            // 检查胡
            if (!check.isChouZai() && !check.isZai() && !check.isTi()) {
                // 检查胡
                pingHu = checkHu1(card, isSelfMo);
            }

            if(!siShou){
                // 检测手上的牌是否能跑胡
                if (check.isPao()) {
                    paoHu = checkPaoHu1(card, isSelfMo, isFirstCard);
                }
                if(check.isHu()) {
                    check.setPao(false);
                }
                PaohuziHuLack hu = table.filtrateHu(this, paoHu, pingHu);
                int passNum=0;
                if(hu!=null){
                    passNum=hu.getFinallyPoint();
                }
                if(!isPassHu){
                    if(hu!=null){
                        if(hu.isHu()){
                            check.setHu(true);
                            check.setPao(false);
                        }
                    }
                    //是否存在栽提
                    boolean flag=check.isTi()||check.isChouZai()||check.isZai();
                    if(!check.isHu()&&flag&&(getOperateCards()==null||getOperateCards().size()==0))
                        setSiShou(true);
                }else {
                    if(passHuNum<passNum)
                        setPassHuNum(passNum);
                }
            }
        } else {
            if (!isPassHu && !check.isChouZai() && !check.isZai() && !check.isTi()) {
                PaohuziHuLack lack = checkHuNew(card, isSelfMo);
                check.setHu(lack.isHu());
            }else if(isPassHu){
                PaohuziHuLack lack = checkHuNew(card, isSelfMo);
                if(lack.isHu()&&lack.getFinallyPoint()>passHuNum)
                    setPassHuNum(lack.getFinallyPoint());
            }
        }
        check.buildActionList();
        return check;
    }

    List<PaohzCard> getPengList(PaohzCard card, List<PaohzCard> cardList) {
        PaohuziHandCard handCard = getPaohuziHandCard();
        if (!handCard.isCanoperateCard(card)) {
            return null;
        }
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
            cardList = PaohuziTool.getSameCards(handCard.getOperateCards(), card);
            if (cardList == null || cardList.size() != 2) {
                return null;
            }
        }
        return cardList;

    }

    public List<PaohzCard> getChiList(PaohzCard card, List<PaohzCard> cardList) {
        PaohuziHandCard handCard = getPaohuziHandCard();
        if (!handCard.isCanoperateCard(card)) {
            return null;
        }
        if (cardList != null) {
            List<PaohzCard> sameList = PaohuziTool.findPhzByVal(cardList, card.getVal());
            List<PaohzCard> handSameList = PaohuziTool.findPhzByVal(handCard.getOperateCards(), card.getVal());
            if (!handSameList.isEmpty() && !sameList.containsAll(handSameList)) {
                return null;
            }
            List<PaohzCard> copy = new ArrayList<>(cardList);
            // 吃牌不符合规则
            if (copy.contains(card)) {
                copy.remove(card);
            }

            List<PaohzCard> chiList = PaohuziTool.checkChi(copy, card);
            if (chiList == null || chiList.isEmpty()) {
                return null;
            }
            // 找出有没有要吃的相同的牌
            if (sameList == null || sameList.isEmpty()) {
                // 如果没有其他相同的牌 直接吃
                return cardList;
            } else {
                // 有相同的牌
                List<PaohzCard> allChi = new ArrayList<>();
                allChi.addAll(chiList);
                //
                copy.removeAll(chiList);
                for (PaohzCard sameCard : sameList) {
                    if (!copy.contains(sameCard)) {
                        continue;
                    }
                    // 相同的牌还能不能继续吃
                    List<PaohzCard> samechiList = PaohuziTool.checkChi(copy, sameCard);
                    if (samechiList == null || samechiList.isEmpty()) {
                        // 如果不能吃 则这个牌不能吃
                        return null;
                    }

                    // 添加相同的牌 的吃
                    copy.removeAll(samechiList);
                    samechiList.add(0, sameCard);
                    allChi.addAll(samechiList);
                }
                return cardList;
            }
        } else {
            cardList = PaohuziTool.checkChi(handCard.getOperateCards(), card);
            if (cardList == null || cardList.isEmpty()) {
                return null;
            }
        }

        if (cardList != null) {
            if (cardList.contains(card)) {
                cardList.remove(card);
            }
            handCard.getOperateCards().removeAll(cardList);
        }

        List<PaohzCard> sameList = PaohuziTool.findCountByVal(handCard.getOperateCards(), card, true);
        // 找出有没有要吃的相同的牌
        if (sameList == null || sameList.isEmpty()) {
            // 如果没有其他相同的牌 直接吃
            return cardList;
        } else {
            // 有相同的牌
            List<PaohzCard> allChi = new ArrayList<>();
            allChi.addAll(cardList);
            //
            handCard.getOperateCards().removeAll(cardList);
            for (PaohzCard sameCard : sameList) {
                if (!handCard.getOperateCards().contains(sameCard)) {
                    continue;
                }
                // 相同的牌还能不能继续吃
                List<PaohzCard> samechiList = PaohuziTool.checkChi(handCard.getOperateCards(), sameCard);
                if (samechiList == null || samechiList.isEmpty()) {
                    // 如果不能吃 则这个牌不能吃
                    return null;
                }

                // 添加相同的牌 的吃
                handCard.getOperateCards().removeAll(samechiList);
                samechiList.add(0, sameCard);
                allChi.addAll(samechiList);
            }
            return allChi;
        }

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
        setOweCardCount(0);
        setFangZhao(0);
        getPlayingTable().changeExtend();
        getPlayingTable().changeCards(seat);
        changeState(player_state.entry);
        setSiShou(false);
        setZaiTiHu(false);
        setTiFen(0);
        clearCbdb();
        setOnlyHht(0);
        setDisNum(0);
        setWinLoseTiFen(0);
        setPassHuNum(0);
        setCbdbNum(0);
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
        sb.append(oweCardCount).append(",");
        sb.append(isFristDisCard ? 1 : 0).append(",");
        sb.append(getMaxPoint()).append(",");
        sb.append(zaiHuxi).append(",");
        sb.append(fangZhao).append(",");
        sb.append(StringUtil.implode(firstPais, "_")).append(",");
        sb.append(StringUtil.implode(actionTotalArr, "_")).append(",");
        sb.append(zaiTiHu?1:0).append(",");
        sb.append(siShou?1:0).append(",");
        sb.append(StringUtil.implode(forbidChiCardIds, "_")).append(",");
        sb.append(StringUtil.implode(limitChiCardIds, "_")).append(",");
        sb.append(onlyHht).append(",");
        sb.append(disNum).append(",");
        sb.append(tiFen).append(",");
        sb.append(passHuNum).append(",");
        sb.append(cbdbNum).append(",");
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
        res.setBopiPoint(getWinLoseTiFen());
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

    public int randNumber(int number) {
        int ret = 0;
        if (number > 0) {
            ret = (number + 5) / 10 * 10;
        } else if (number < 0) {
            ret = (number - 5) / 10 * 10;
        }

        return ret;
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

        LyzpTable table = getPlayingTable(LyzpTable.class);

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
        res.addExt(onlyHht);//13是否只能胡红黑堂（用作前端听牌提示）
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

    public boolean isAlreadyMo() {
        int count = handPais.size();
        count += outPais.size();
        count += ti.size();
        count += chi.size();
        count += peng.size();
        count += zai.size();
        count += chouZai.size();
        return count == 21;

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

    public int getOweCardCount() {
        return oweCardCount;
    }

    public void setOweCardCount(int oweCardCount) {
        this.oweCardCount = oweCardCount;
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
            list.add(type.buildMsg().build());
        }
        if (hu != null && hu.getPhzHuCards() != null) {
            for (CardTypeHuxi type : hu.getPhzHuCards()) {
                list.add(type.buildMsg().build());
            }
        }

        return list;
    }

    public List<PhzHuCards> buildPhzCards(LyzpTable table,long lookUid) {
        List<PhzHuCards> list = new ArrayList<>();
        for (CardTypeHuxi type : cardTypes) {
            if (type.getAction() == PaohzDisAction.action_zai) {
                // 不是本人并且是栽
                if (lookUid != this.userId){
                    list.add(type.buildMsg(true).build());
                }else{
                    list.add(type.buildMsg().build());
                }
            } else {
                list.add(type.buildMsg().build());
            }
        }

        return list;
    }



    /**
     * 是否栽过的牌跑
     */
    boolean isZaiPao(int val) {
        return PaohuziTool.isHasCardVal(zai, val);
    }

    /**
     * 得到要提的栽的牌
     */
    List<PaohzCard> getTiCard(PaohzCard card) {
        List<PaohzCard> list = PaohuziTool.getSameCards(zai, card);
        if (list == null || list.isEmpty()) {
            list = PaohuziTool.getSameCards(handPais, card);
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

    public boolean isZaiTiHu() {
        return zaiTiHu;
    }

    public void setZaiTiHu(boolean zaiTiHu) {
        this.zaiTiHu = zaiTiHu;
    }

    public int getFangZhao() {
        return fangZhao;
    }

    public void setFangZhao(int fangZhao) {
        this.fangZhao = fangZhao;
        changeTableInfo();
    }

    public boolean isFangZhao() {
        return 1 == fangZhao;
    }

    public int getOnlyHht() {
        return onlyHht;
    }

    public void setOnlyHht(int onlyHht) {
        this.onlyHht = onlyHht;
        changeTableInfo();
    }
    public boolean isOnlyHht() {
        return 1 == onlyHht;
    }

    public List<Integer> getPassChi() {
        return passChi;
    }

    public void removePassChi(int val) {
        if (passChi.contains(val)) {
            int index = passChi.indexOf(val);
            passChi.remove(index);
            changeTableInfo();
        }
    }
    @Override
    public int loadScore(){
        LyzpTable table = getPlayingTable(LyzpTable.class);
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

    public static final List<Integer> wanfaList = Arrays.asList(GameUtil.game_type_lyzp);

    public static void loadWanfaPlayers(Class<? extends Player> cls){
        for (Integer integer:wanfaList){
            PlayerManager.wanfaPlayerTypesPut(integer,cls, PaohuziCommandProcessor.getInstance());
        }
    }

}
