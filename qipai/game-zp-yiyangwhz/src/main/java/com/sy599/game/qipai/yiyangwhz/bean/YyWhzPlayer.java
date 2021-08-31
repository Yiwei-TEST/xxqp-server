package com.sy599.game.qipai.yiyangwhz.bean;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.GeneratedMessage;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.SharedConstants.player_state;
import com.sy599.game.db.bean.group.GroupUser;
import com.sy599.game.db.dao.group.GroupDao;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.TableGhzResMsg;
import com.sy599.game.msg.serverPacket.TablePhzResMsg.PhzHuCards;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes;
import com.sy599.game.qipai.yiyangwhz.command.YyWhzCommandProcessor;
import com.sy599.game.qipai.yiyangwhz.constant.YyWhzCard;
import com.sy599.game.qipai.yiyangwhz.constant.YyWhzConstant;
import com.sy599.game.qipai.yiyangwhz.constant.YyWhzEnumHelper;
import com.sy599.game.qipai.yiyangwhz.rule.YyWhzCardIndexArr;
import com.sy599.game.qipai.yiyangwhz.rule.YyWhzIndex;
import com.sy599.game.qipai.yiyangwhz.rule.YyWhzMenzi;
import com.sy599.game.qipai.yiyangwhz.rule.YyWhzMingTangRule;
import com.sy599.game.qipai.yiyangwhz.tool.YyWhzHuLack;
import com.sy599.game.qipai.yiyangwhz.tool.YyWhzTool;
import com.sy599.game.util.DataMapUtil;
import com.sy599.game.util.GameUtil;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.util.StringUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 鬼胡子玩家信息
 */
public class YyWhzPlayer extends Player {
    /**
     * 座位id
     */
    private int seat;
    /**
     * 状态 1进入 2已准备 3正在玩 4已结束
     */
    private player_state state;
    /**
     * 是否已进牌桌
     */
    private int isEntryTable;
    /**
     * 当前手牌
     */
    private List<YyWhzCard> handPais;
    /**
     * 已打出的牌
     */
    private List<YyWhzCard> outPais;
    /**
     * 碰的牌
     */
    private List<YyWhzCard> peng;
    /**
     * 吃的牌
     */
    private List<YyWhzCard> chi;
    /**
     * 偎的牌
     */
    private List<YyWhzCard> wei;
    /**
     * 漂的牌
     */
    private List<YyWhzCard> piao;
    /**
     * 溜的牌
     */
    private List<YyWhzCard> liu;
    /**
     * 臭碰
     */
    private List<Integer> passPeng;
    /**
     * 臭吃
     */
    private List<Integer> passChi;
    /**
     * 臭门子
     */
    private List<YyWhzMenzi> passMenzi;

    /**
     * 是否有起手溜 1是 0否
     */
    private int firstLiu;
    /**
     * 胜场
     */
    private int winCount;
    /**
     * 负场
     */
    private int lostCount;
    private int lostPoint;
    private int point;
    /**
     * 胡牌信息
     */
    private YyWhzHuLack hu;
    /**
     * 出牌对应操作信息
     */
    private List<YyWhzCardTypeHuxi> cardTypes;
    /**
     * 是否第一次出牌
     */
    private boolean isFristDisCard;

    /**
     * 是否出过牌，用于检查是否报听胡，庄家起手出牌不算
     */
    private boolean isDisCardForBaoTingHu;
    /**
     * 初始手牌
     */
    private List<Integer> firstPais;

    /**
     * 小局结算 大胡列表
     * 0项项息 1无息平 2对子胡 3黑胡 4黑对子胡 5一点朱 6十三火 7十四火 8十五火 9九对半 10大字胡 11小字胡 12海底 13天胡 14报听 15内圆 16外圆
     */
    private List<Integer> dahu;

    private int wailiufen = 0;
    private int jichufen = 0;
    
    /**
     * 总结算
     * 大胡次数  0项项息 1无息平 2对子胡 3黑胡 4黑对子胡 5一点朱 6十三火 7十四火 8十五火 9九对半 10大字胡 11小字胡 12海底 13天胡 14报听 15内圆 16外圆
     */
    private int[] dahuCounts;


    private volatile boolean autoPlay = false;//托管
    private volatile long lastOperateTime = 0;//最后操作时间
    private volatile long lastCheckTime = 0;//最后检查时间

    private volatile long autoPlayTime = 0;//自动操作时间
    private volatile boolean isCheckAuto = false; //开始托管合计时

    /**
     * 过吃临时操作：用于记录过吃后还要取消过吃
     */
    private List<Integer> passChiTemp;
    /**
     * 臭门子
     */
    private List<YyWhzMenzi> passMenziTemp;

    /**
     * 玩家死守，只能摸打，不能有其他操作
     */
    private boolean isSiShou;
    /**
     * 玩家死守，只能摸打，不能有其他操作
     */
    private boolean isSiShouTemp;
    /**
     * 死守的牌
     */
    private Set<Integer> siShouPais;
    /**
     * 最大胡息
     */
    private int maxHuxi;
    
    private List<Integer> tingpai;//听牌的值

    public YyWhzPlayer() {
        handPais = new ArrayList<>();
        outPais = new ArrayList<>();
        peng = new ArrayList<>();
        passPeng = new ArrayList<>();
        passChi = new ArrayList<>();
        passMenzi = new ArrayList<>();
        chi = new ArrayList<>();
        wei = new ArrayList<>();
        piao = new ArrayList<>();
        liu = new ArrayList<>();
        cardTypes = new ArrayList<>();
        firstPais = new ArrayList<>();
        dahu = new ArrayList<>();
        dahuCounts = new int[18];
        firstLiu = 0;
        maxHuxi = 0;
        passChiTemp = new ArrayList<>();
        passMenziTemp = new ArrayList<>();
        siShouPais = new HashSet<>();
        tingpai = new ArrayList<>();
    }


    /**
     * 获得初始手牌
     *
     * @return
     */
    public List<Integer> getFirstPais() {
        return firstPais;
    }

    public int getDahuCount() {
        return dahu.size();
    }

    public List<Integer> getDahu() {
        return dahu;
    }

    public void setDahu(List<Integer> dahuList, Map<Integer, Integer> yuanMap) {
        this.dahu.clear();
        this.dahu = dahuList;
        for (int dahu : dahuList) {
            changeDahuCounts(dahu, 1);
        }
        if (yuanMap != null) {
            if (yuanMap.get(1) > 0) {
                changeDahuCounts(15, yuanMap.get(1));
            }
            if (yuanMap.get(2) > 0) {
                changeDahuCounts(16, yuanMap.get(2));
            }
        }
        getPlayingTable().changeExtend();
    }

    /**
     * 总结算 大胡次数累计统计
     *
     * @param index 大胡次数  0项项息 1无息平 2对子胡 3黑胡 4黑对子胡 5一点朱 6十三火 7十四火 8十五火 9九对半 10大字胡 11小字胡 12海底 13天胡 14报听 15内圆 16外圆
     * @param val   次数
     */
    public void changeDahuCounts(int index, int val) {
        if(index >= 100){
        	return;
        }
    	dahuCounts[index] += val;
        getPlayingTable().changeExtend();
    }

    /**
     * 移掉出的牌
     */
    void removeOutPais(YyWhzCard card) {
        outPais.remove(card);
    }

    /**
     * 摸牌
     */
    public void moCard(YyWhzCard card) {
        this.outPais.add(card);
        setFristDisCard(false);
    }

    public void tiLong(YyWhzCard card) {
        this.handPais.add(card);
        compensateCard();
        changeSeat(seat);
    }

    public void disCard(int action, List<YyWhzCard> disCardList) {
        for (YyWhzCard disCard : disCardList) {
            handPais.remove(disCard);// 手牌里面去掉
        }
        if (action == YyWhzDisAction.action_piao && YyWhzTool.isHasCardVal(peng, disCardList.get(0).getVal())) {// 碰 => 漂
            List<YyWhzCard> removePengs = YyWhzTool.findGhzByVal(peng, disCardList.get(0).getVal());
            for (YyWhzCard removeCard : removePengs) {
                peng.remove(removeCard);
            }
            Iterator<YyWhzCardTypeHuxi> iterator = cardTypes.iterator();
            while (iterator.hasNext()) {
                YyWhzCardTypeHuxi type = iterator.next();
                if (type.getAction() == YyWhzDisAction.action_peng && type.isHasCard(disCardList.get(1))) {
                    iterator.remove();
                }
            }
        } else if ((action == YyWhzDisAction.action_liu || action == YyWhzDisAction.action_weiHouLiu) && YyWhzTool.isHasCardVal(wei, disCardList.get(0).getVal())) {// 偎 => 溜
            List<YyWhzCard> removeWeis = YyWhzTool.findGhzByVal(wei, disCardList.get(0).getVal());
            for (YyWhzCard removeCard : removeWeis) {
                wei.remove(removeCard);
            }
            Iterator<YyWhzCardTypeHuxi> iterator = cardTypes.iterator();
            while (iterator.hasNext()) {
                YyWhzCardTypeHuxi type = iterator.next();
                if (type.getAction() == YyWhzDisAction.action_wei && type.isHasCard(disCardList.get(1))) {
                    iterator.remove();
                }
            }
            action = YyWhzDisAction.action_weiHouLiu;
        }
        if (action == YyWhzDisAction.action_liu || action == YyWhzDisAction.action_weiHouLiu) {
            liu.addAll(disCardList);
        } else if (action == YyWhzDisAction.action_piao) {
            piao.addAll(disCardList);
        } else if (action == YyWhzDisAction.action_wei) {
            wei.addAll(disCardList);
            this.addSiShouPai(disCardList.get(0).getVal());
        } else if (action == YyWhzDisAction.action_peng) {
            peng.addAll(disCardList);
            this.addSiShouPai(disCardList.get(0).getVal());
        } else if (action == YyWhzDisAction.action_chi) {
            chi.addAll(disCardList);
            this.addSiShouPai(disCardList.get(0).getVal());
        } else {
            outPais.addAll(disCardList);
            int passVal = disCardList.get(0).getVal();
            if (!passChi.contains(passVal))
                passChi.add(passVal);
            if (!passPeng.contains(passVal))
                passPeng.add(passVal);
        }
        setFristDisCard(false);
        if (disCardList.size() > 3 && disCardList.size() % 3 == 0) {
            for (int i = 0; i < disCardList.size() / 3; i++) {
                List<YyWhzCard> list = disCardList.subList(i * 3, i * 3 + 3);
                addCardType(action, list);
            }
        } else {
            addCardType(action, disCardList);
        }
        changeSeat(seat);
        changeTableInfo();
    }

    public void addCardType(int action, List<YyWhzCard> disCardList) {
        YyWhzTable table = getPlayingTable(YyWhzTable.class);
        int huxi = YyWhzTool.getYingHuxi(action, disCardList);
        if (action != 0) {
            YyWhzCardTypeHuxi type = new YyWhzCardTypeHuxi();
            type.setAction(action);
            type.setCardIds(YyWhzTool.toGhzCardIds(disCardList));
            type.setHux(huxi);
            boolean isSelfMo = table.isMoFlag() && table.getMoSeat() == seat;
            if (action == YyWhzDisAction.action_liu || action == YyWhzDisAction.action_weiHouLiu) {
                isSelfMo = true;
            }
            if (action == YyWhzDisAction.action_piao) {
                isSelfMo = false;
            }
            type.setSelfMo(isSelfMo);
            cardTypes.add(type);
        }
    }

    public List<YyWhzCardTypeHuxi> getCardTypes() {
        return cardTypes;
    }

    public List<YyWhzCard> getPeng() {
        return peng;
    }

    public List<YyWhzCard> getWei() {
        return wei;
    }

    /**
     * 是否需要出牌
     *
     * @return
     */
    public boolean isNeedDisCard(int action) {
        if (action == YyWhzDisAction.action_piao || action == YyWhzDisAction.action_liu || action == YyWhzDisAction.action_weiHouLiu) {
            return false;
        }
        return true;
//		return isFristDisCard();
    }

    /**
     *
     */
    public void compensateCard() {
//		oweCardCount++;
        changeTableInfo();
    }

    /**
     * 臭吃或者臭碰
     *
     * @param passAction 过掉的操作
     * @param passCard   过掉的牌
     * @param nowAction  当前执行的操作
     */
    public void pass(int passAction, YyWhzCard passCard, int nowAction) {
        if (passAction == YyWhzDisAction.action_chi) {
            // -------- 过吃 -----------
            if (!passChi.contains(passCard.getVal())) {
                passChi.add(passCard.getVal());
                if (nowAction == YyWhzDisAction.action_pass) {
                    passChiTemp.add(passCard.getVal());
                }
            }
            // 过吃、出牌，需要臭门子
            boolean isChouMenZi = nowAction == YyWhzDisAction.action_pass || nowAction == YyWhzDisAction.action_chuPai;
            if (isChouMenZi) {
                List<YyWhzCard> copyHandPais = new ArrayList<>(handPais);
                YyWhzCardIndexArr valArr = YyWhzTool.getMax(copyHandPais);
                List<YyWhzMenzi> menZis = valArr.getMenzis(false);
                for (YyWhzMenzi menZi : menZis) {
                    YyWhzMenzi menZiTemp = new YyWhzMenzi(menZi.getMenzi(), 0);
                    List<YyWhzCard> menZiCards = YyWhzTool.findGhzCards(copyHandPais, menZi.getMenzi());
                    List<YyWhzCard> chiList = YyWhzTool.checkChi(menZiCards, passCard);
                    if (chiList.size() == menZiCards.size() && !passMenzi.contains(menZiTemp)) {
                        passMenzi.add(menZiTemp);
                        if (nowAction == YyWhzDisAction.action_pass) {
                            passMenziTemp.add(menZiTemp);
                        }
                    }
                }
            }
        } else if (passAction == YyWhzDisAction.action_peng || passAction == YyWhzDisAction.action_wei) {
            // -------- 过碰 -----------
            if (!passPeng.contains(passCard.getVal())) {
                passPeng.add(passCard.getVal());
            }
        }
        changeTableInfo();
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
            this.state = YyWhzEnumHelper.getPlayerState(stateVal);
            this.isEntryTable = StringUtil.getIntValue(values, i++);

            String passPengStr = StringUtil.getValue(values, i++);
            if (!StringUtils.isBlank(passPengStr)) {
                passPeng = StringUtil.explodeToIntList(passPengStr, "_");
            }
            String passChiStr = StringUtil.getValue(values, i++);
            if (!StringUtils.isBlank(passChiStr)) {
                passChi = StringUtil.explodeToIntList(passChiStr, "_");
            }

            this.winCount = StringUtil.getIntValue(values, i++);
            this.lostCount = StringUtil.getIntValue(values, i++);
            this.lostPoint = StringUtil.getIntValue(values, i++);
            this.point = StringUtil.getIntValue(values, i++);
            setTotalPoint(StringUtil.getIntValue(values, i++));
            this.isFristDisCard = StringUtil.getIntValue(values, i++) == 1;
            setMaxPoint(StringUtil.getIntValue(values, i++));
            String firstPaisStr = StringUtil.getValue(values, i++);
            if (!StringUtils.isBlank(firstPaisStr)) {
                firstPais = StringUtil.explodeToIntList(firstPaisStr, "_");
            }
            String dahuStr = StringUtil.getValue(values, i++);
            if (!StringUtils.isBlank(dahuStr)) {
                dahu = StringUtil.explodeToIntList(dahuStr, "_");
            }
            String dahuCountStr = StringUtil.getValue(values, i++);
            if (!StringUtils.isBlank(dahuCountStr)) {
                dahuCounts = StringUtil.explodeToIntArray(dahuCountStr, "_");
            }

            passMenzi.clear();
            String passMenziStr = StringUtil.getValue(values, i++);
            if (!StringUtils.isBlank(passMenziStr)) {
                String[] str = StringUtil.explodeToStringArray(passMenziStr, "_");
                if (str != null && str.length > 0) {
                    for (String menziStr : str) {
                        List<Integer> menziIds = new ArrayList<>();
                        String[] ids = menziStr.split("&");
                        menziIds.add(Integer.parseInt(ids[0]));
                        menziIds.add(Integer.parseInt(ids[1]));
                        passMenzi.add(new YyWhzMenzi(menziIds, 0));
                    }
                }
            }
            this.firstLiu = StringUtil.getIntValue(values, i++);

            String passChiStrTemp = StringUtil.getValue(values, i++);
            if (!StringUtils.isBlank(passChiStrTemp)) {
                passChiTemp = StringUtil.explodeToIntList(passChiStrTemp, "_");
            }

            String passMenziStrTemp = StringUtil.getValue(values, i++);
            if (!StringUtils.isBlank(passMenziStrTemp)) {
                String[] str = StringUtil.explodeToStringArray(passMenziStrTemp, "_");
                if (str != null && str.length > 0) {
                    for (String menziStr : str) {
                        List<Integer> menziIds = new ArrayList<>();
                        String[] ids = menziStr.split("&");
                        menziIds.add(Integer.parseInt(ids[0]));
                        menziIds.add(Integer.parseInt(ids[1]));
                        passMenziTemp.add(new YyWhzMenzi(menziIds, 0));
                    }
                }
            }
            this.isSiShou = StringUtil.getIntValue(values, i++,0) == 1;
            this.isSiShouTemp = StringUtil.getIntValue(values, i++,0) == 1;

            String siShouPaisStr = StringUtil.getValue(values, i++);
            if (!StringUtils.isBlank(siShouPaisStr)) {
                siShouPais = new HashSet<>(StringUtil.explodeToIntList(siShouPaisStr, "_"));
            }
            this.isDisCardForBaoTingHu = StringUtil.getIntValue(values, i++) == 1;
            String tingpaiStr = StringUtil.getValue(values, i++);
            if (!StringUtils.isBlank(tingpaiStr)) {
            	tingpai = new ArrayList<>(StringUtil.explodeToIntList(tingpaiStr, "_"));
            }
            this.maxHuxi = StringUtil.getIntValue(values, i++);
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
        dahu = new ArrayList<>();
        dahuCounts = new int[18];
        peng.clear();
        chi.clear();
        wei.clear();
        piao.clear();
        liu.clear();
        passChi.clear();
        passPeng.clear();
        passMenzi.clear();
        cardTypes.clear();
        tingpai.clear();
        maxHuxi = 0;
        setHu(null);
        setWinCount(0);
        setLostCount(0);
        setPoint(0);
        setLostPoint(0);
        setTotalPoint(0);
        setMaxPoint(0);
        setSeat(0);
        setFirstLiu(0);
        if (table.isAutoPlay() && autoPlay) {
            setAutoPlay(false, table);
        }
        if (!isCompetition) {
            setPlayingTableId(0);
        }
        autoPlay = false;
        setLastCheckTime(0);
        this.setSiShou(false);
        this.setSiShouTemp(false);
        this.setSiShouPais(new HashSet<>());
        setDisCardForBaoTingHu(false);
        saveBaseInfo();
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

    boolean isCanDisCard(List<Integer> disCards, YyWhzCard nowDisCard) {
        if (disCards != null) {
            if (nowDisCard != null && disCards.contains(nowDisCard.getId())) {
                List<Integer> copy = new ArrayList<>(disCards);
                // 排除掉在桌面上已经出的牌
                copy.remove((Integer) nowDisCard.getId());
                if (!(getHandPais().containsAll(copy) || YyWhzTool.toGhzCardIds(getWei()).containsAll(copy))) {
                    LogUtil.errorLog.info("找不到牌:" + disCards);
                    return false;
                }
            } else {
                if (!getHandPais().containsAll(disCards)) {
                    LogUtil.errorLog.info("找不到牌:" + disCards);
                    return false;
                }
            }
        }
        return true;
    }

    public YyWhzHuLack checkHu(YyWhzCard card, boolean isSelfMo) {
        List<YyWhzCard> handCopy = new ArrayList<>(handPais);
        boolean hasWei = true;
//        if (wei != null && wei.size() > 0) {
//            hasWei = true;
//        }
        int outYingxiCount = getOutYingXiCount();
        YyWhzTable table = getPlayingTable(YyWhzTable.class);
        boolean isHaiDi = (table.getLeftCards().size() == 0) ? true : false;
//        boolean canDiaoDiaoShou = table.isDiaodiaoshou();
        YyWhzHuLack lack = YyWhzTool.isHu(cardTypes, handCopy, card, isSelfMo, outYingxiCount, hasWei, false, false, false,table.getQihuxi());
        return lack;
    }

    /**
   *	溜：四张相同，4息
	飘：四张相同，1息
	歪：三张相同，4息
	坎：三张相同，3息
	碰：三张相同，1
	2710：大小字都是 1息

     * @return
     */
    public int getOutYingXiCount() {
        int xi2710 = 0;
        if (chi != null && !chi.isEmpty()) {
            for (int i = 0; i < chi.size(); i += 3) {
                List<YyWhzCard> list = chi.subList(i, i + 3);
                int redNum = YyWhzTool.findRedGhzs(list).size();
                if (redNum == 3) {
                    xi2710++;
                }
            }
        }
        return wei.size() / 3 * 4 + peng.size() / 3 + liu.size() / 4 * 4 + piao.size() / 4 + xi2710;
    }

    /**
     * 摸牌或出牌时 检查玩家可以做的操作
     *
     * @param card
     * @param isSelfMo
     * @param isFirstCard
     * @return
     */
    public YyWhzCheckCardBean checkCard(YyWhzCard card, boolean isSelfMo, boolean isFirstCard) {
        return checkCard(card, isSelfMo, false, false, isFirstCard, false);
    }

    /**
     * 庄家起手可做的操作
     *
     * @param card
     * @param isSelfMo
     * @param isBegin
     * @param isFirstCard
     * @return
     */
    public YyWhzCheckCardBean checkCard(YyWhzCard card, boolean isSelfMo, boolean isBegin, boolean isFirstCard) {
        return checkCard(card, isSelfMo, false, isBegin, isFirstCard, false);
    }

    /**
     * 检查牌  可做的操作
     *
     * @param card     出的牌或者是摸的牌
     * @param isSelfMo 是否是自己摸的牌
     */
    public YyWhzCheckCardBean checkCard(YyWhzCard card, boolean isSelfMo, boolean isPassHu, boolean isBegin, boolean isFirstCard, boolean isPass) {
        YyWhzCheckCardBean check = new YyWhzCheckCardBean(seat, card);
        if(isSiShou){
            check.buildActionList();
            return check;
        }
        List<YyWhzCard> copy = new ArrayList<>(handPais);
        List<YyWhzCard> handPais = new ArrayList<>(getHandGhzs());// 手牌
        YyWhzCardIndexArr valArr = YyWhzTool.getMax(handPais);
        boolean isCanChiPeng = true;
        if (handPais.size() < 4) {// 必须要出牌但是可以可操作的牌已经不够了
            isCanChiPeng = false;
        }
        YyWhzTable table = getPlayingTable(YyWhzTable.class);
        YyWhzIndex index3 = valArr.getPaohzCardIndex(3);// 四张的牌
        YyWhzIndex index2 = valArr.getPaohzCardIndex(2);// 三张的牌
        YyWhzIndex index1 = valArr.getPaohzCardIndex(1);// 两张的牌
        // ---------------------------检查溜 偎 漂
        if (isSelfMo) {
            if (index3 != null && card == null && isBegin) {// 手里有四张
                check.setLiu(true);
            }
            if (card != null && index2 != null) {// 手里有三张 可偎 可溜
                for (int val : index2.getPaohzValMap().keySet()) {
                    if (val == card.getVal()) {
                        check.setLiu(true);
                        check.setWei(true);
                        break;
                    }
                }
            }
            if (card != null && containsCard(wei, card.getVal())) {// 偎牌后再摸了一张
                check.setLiu(true);
            }
            if (card != null && index1 != null) {// 手里有两张 可偎
                for (int val : index1.getPaohzValMap().keySet()) {
                    if (val == card.getVal() && !passPeng.contains(card.getVal())) {// 臭碰的牌不能再偎了
                        check.setWei(true);
                    }
                }
            }
            if (card != null && containsCard(peng, card.getVal()) && table.isKepiao()) {
                check.setPiao(true);
            }
        }else{
        	if (index2 != null && card != null ) {// 手里有3张，别人摸、出或碰后自己摸起一个。可选【飘】
           	 for (int val : index2.getPaohzValMap().keySet()) {
                    if (val == card.getVal()) {
                   	 check.setPiao(true);
                        break;
                    }
                }
           }
           
        }
        
        // --------------------------检查胡
        boolean isMoFlag = table.isMoFlag();
        if (isMoFlag && card != null) {
            YyWhzHuLack lack = checkHu(card, isSelfMo);// 检查摸到的牌是否能胡
            if (lack.isHu()) {
                check.setHu(true);
            }
        } else {
            if (isBegin == true) {// 天胡判断
                YyWhzHuLack lack = checkHu(card, isSelfMo);
                if (lack.isHu()) {
                    check.setHu(true);
                }
            }
        }
        // --------------------------检查碰
        if (card != null) {
            List<YyWhzCard> sameList = YyWhzTool.getSameCards(copy, card);
            if (sameList.size() >= 2 && !isSelfMo) {// 手里有两张 可碰
                if (isCanChiPeng && !passPeng.contains(card.getVal())) {// 检查碰
                    check.setPeng(true);
                }
            }
            // ----------------------检查吃
            // 是否能检测能吃牌
            boolean isCheckChi = false;
            if (isMoFlag && table.getMoSeat() == seat) {// 如果这张牌是摸的,摸上来的人也能检测
                isCheckChi = true;
            } 
            
            if (!isCheckChi && table.calcNextSeat(table.getDisCardSeat()) == seat) {// 摸牌打出的下家是一定要检测的
                isCheckChi = true;
            }
            int hasChiSize = getChiSizeExcept2710();
            if (hasChiSize >= 2 && !YyWhzTool.c2710List.contains(card.getPai())) {
                isCheckChi = false;
            }
            if(!isMoFlag && table.isChidisCard()){
            	isCheckChi = false;
            }
            if (isCanChiPeng && isCheckChi && !isPass) {
                List<YyWhzMenzi> menzis = valArr.getMenzis(false);
                boolean canChi = false;
                for (YyWhzMenzi menzi : menzis) {
                    List<YyWhzCard> menziCards = YyWhzTool.findGhzCards(handPais, menzi.getMenzi());
                    List<YyWhzCard> chiList = YyWhzTool.checkChi(menziCards, card);
                    if (chiList.size() == menziCards.size()) {
                        if (!passChi.contains(card.getVal()) && !passMenzi.contains(menzi)) {
                            if (hasChiSize >= 2) {
                                chiList.add(card);
                                List<Integer> cardPais = YyWhzTool.toGhzCardVals(chiList, false);
                                if (YyWhzTool.c2710List.containsAll(cardPais)) {
                                    check.setChi(true);
                                    canChi = true;
                                    break;
                                }
                            } else {
                                check.setChi(true);
                                canChi = true;
                                break;
                            }
                        }
                    }
                }
                if (!canChi && !passChi.contains(card.getVal())) {// 不能吃后就再也不能吃了
                    passChi.add(card.getVal());
                    changeTableInfo();
                }
            }
        } else {
            if (!isPassHu) {
                YyWhzHuLack lack = checkHu(card, isSelfMo);
                check.setHu(lack.isHu());
            }
        }
        check.buildActionList();
        return check;
    }

    /**
     * 检测溜（在要出牌时检查是否可溜）
     */
    public YyWhzCheckCardBean checkLiu() {
        YyWhzCheckCardBean check = new YyWhzCheckCardBean(seat, null);
        List<YyWhzCard> handPais = new ArrayList<>(getHandGhzs());// 手牌
        YyWhzCardIndexArr valArr = YyWhzTool.getMax(handPais);
        YyWhzIndex index3 = valArr.getPaohzCardIndex(3);// 四张的牌
        YyWhzIndex index0 = valArr.getPaohzCardIndex(0);// 单张的牌
        // 手里有四张的牌 或者包含一张已经偎过的牌
        if (index3 != null) {// 手里有四张
            check.setLiu(true);
        } else {
            if (index0 != null) {// 手里有三张 可偎 可溜
                for (int val : index0.getPaohzValMap().keySet()) {
                    if (containsCard(wei, val)) {
                        check.setLiu(true);
                        break;
                    }
                    if (containsCard(peng, val)) {
                    	check.setPiao(true);
                    	break;
                    }
                }
            }
        }
        check.buildActionList();
        return check;
    }

    public List<YyWhzCard> getPengOrWeiList(YyWhzCard card, List<YyWhzCard> cardList) {
        if (cardList != null && !cardList.isEmpty()) {// 吃牌不符合规则
            List<YyWhzCard> gameList = YyWhzTool.getSameCards(cardList, card);
            if (gameList == null || gameList.isEmpty()) {
                return null;
            }
            if (gameList.size() != cardList.size()) {
                return null;
            }
        } else {
            cardList = YyWhzTool.getSameCards(handPais, card);
            if (cardList.size() >= 3) {
                cardList.remove(0);
            }
            if (cardList == null || cardList.size() < 2) {
                return null;
            }
        }
        return cardList;
    }

    public List<YyWhzCard> getChiList(YyWhzCard card, List<YyWhzCard> cardList) {
        if (cardList != null) {
            List<YyWhzCard> sameList = YyWhzTool.findGhzByVal(cardList, card.getVal());
            List<YyWhzCard> copy = new ArrayList<>(cardList);
            // 吃牌不符合规则
            if (copy.contains(card)) {
                copy.remove(card);
            }
            List<YyWhzCard> chiList = YyWhzTool.checkChi(copy, card);
            if (chiList == null || chiList.isEmpty()) {
                return null;
            }
            // 找出有没有要吃的相同的牌
            if (sameList == null || sameList.isEmpty()) {
                // 如果没有其他相同的牌 直接吃
                return cardList;
            } else {
                // 有相同的牌
                List<YyWhzCard> allChi = new ArrayList<>();
                allChi.addAll(chiList);
                //
                copy.removeAll(chiList);
                for (YyWhzCard sameCard : sameList) {
                    if (!copy.contains(sameCard)) {
                        continue;
                    }
                    // 相同的牌还能不能继续吃
                    List<YyWhzCard> samechiList = YyWhzTool.checkChi(copy, sameCard);
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
//			cardList = YjGhzTool.checkChi(handCard.getOperateCards(), card);
//			if (cardList == null || cardList.isEmpty()) {
//				return null;
//			}
        }
        List<YyWhzCard> allChi = new ArrayList<>();
        return allChi;
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
        cardTypes.clear();
        dahu = new ArrayList<>();
        handPais.clear();
        outPais.clear();
        peng.clear();
        chi.clear();
        piao.clear();
        liu.clear();
        wei.clear();
        passChi.clear();
        passPeng.clear();
        passMenzi.clear();
        setFirstLiu(0);
        getPlayingTable().changeExtend();
        getPlayingTable().changeCards(seat);
        changeState(player_state.entry);
        this.setSiShou(false);
        this.setSiShouTemp(false);
        this.setSiShouPais(new HashSet<>());
        clearPassChiTemp();
        setDisCardForBaoTingHu(false);
    }

    public List<YyWhzCard> getHandGhzs() {
        return handPais;
    }

    @Override
    public List<Integer> getHandPais() {
        return YyWhzTool.toGhzCardIds(handPais);
    }

    public List<Integer> getOutPais() {
        return YyWhzTool.toGhzCardIds(outPais);
    }

    public List<YyWhzCard> getOutPaisCard() {
        return outPais;
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
        for (YyWhzCardTypeHuxi huxi : cardTypes) {
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
        StringBuffer sb = new StringBuffer();
        List<Integer> outPais = getHandPais();
        List<Integer> piaoPais = YyWhzTool.toGhzCardIds(piao);
        List<Integer> liuPais = YyWhzTool.toGhzCardIds(liu);
        List<Integer> weiPais = YyWhzTool.toGhzCardIds(wei);
        List<Integer> pengPais = YyWhzTool.toGhzCardIds(peng);
        List<Integer> chiPais = YyWhzTool.toGhzCardIds(chi);
        sb.append(StringUtil.implode(outPais)).append(";");
        sb.append(StringUtil.implode(piaoPais)).append(";");
        sb.append(StringUtil.implode(liuPais)).append(";");
        sb.append(StringUtil.implode(weiPais)).append(";");
        sb.append(StringUtil.implode(pengPais)).append(";");
        sb.append(StringUtil.implode(chiPais)).append(";");
        return sb.toString();
    }

    @Override
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
        sb.append(StringUtil.implode(passPeng, "_")).append(",");
        sb.append(StringUtil.implode(passChi, "_")).append(",");
        sb.append(winCount).append(",");
        sb.append(lostCount).append(",");
        sb.append(lostPoint).append(",");
        sb.append(point).append(",");
        sb.append(getTotalPoint()).append(",");
        sb.append(isFristDisCard ? 1 : 0).append(",");
        sb.append(getMaxPoint()).append(",");
        sb.append(StringUtil.implode(firstPais, "_")).append(",");
        sb.append(StringUtil.implode(dahu, "_")).append(",");
        sb.append(StringUtil.implode(dahuCounts, "_")).append(",");
        sb.append(StringUtil.implode(passMenzi, "_")).append(",");
        sb.append(firstLiu).append(",");
        sb.append(StringUtil.implode(passChiTemp, "_")).append(",");
        sb.append(StringUtil.implode(passMenziTemp, "_")).append(",");
        sb.append(isSiShou ? 1 : 0).append(",");
        sb.append(isSiShouTemp ? 1 : 0).append(",");
        sb.append(StringUtil.implode(new ArrayList<>(siShouPais), "_")).append(",");
        sb.append(isDisCardForBaoTingHu ? 1 : 0).append(",");
        sb.append(StringUtil.implode(new ArrayList<>(tingpai), "_")).append(",");
        sb.append(maxHuxi).append(",");
        return sb.toString();
    }

    /**
     * 单局详情
     */
    public TableGhzResMsg.ClosingGhzPlayerInfoRes.Builder bulidOneClosingPlayerInfoRes() {
        TableGhzResMsg.ClosingGhzPlayerInfoRes.Builder res = TableGhzResMsg.ClosingGhzPlayerInfoRes.newBuilder();
        res.setUserId(userId + "");
        res.addAllCards(getHandPais());
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
        if (hu != null && hu.getYuanMap() != null) {
//        	YyWhzTable table = getPlayingTable(YyWhzTable.class);
//        	// --------------------------内圆--------------------------------
//            if (hu.getYuanMap().get(1) > 0) {
////            	res.setNeiYuanNum((table.isDazhuo()?40:30)*hu.getYuanMap().get(1));
//            }
//
//            // --------------------------外圆--------------------------------
//            if (hu.getYuanMap().get(2) > 0) {
////            	res.setWaiYuanNum((table.isDazhuo()?30:20)*hu.getYuanMap().get(2));
//            }
        	res.setNeiYuanNum(hu.getYuanMap().get(1));//内豪
            res.setWaiYuanNum(hu.getYuanMap().get(2));//外豪
        }
        res.setWaiHao(wailiufen);//歪溜分
        res.setQingHao(jichufen);//基础分
        res.addAllDahus(dahu);//牌型
        res.addAllMcards(buildGhzHuCards());
        return res;
    }

    /**
     * 总局详情
     *
     * @return
     */
    public TableGhzResMsg.ClosingGhzPlayerInfoRes.Builder bulidTotalClosingPlayerInfoRes(int point) {
        TableGhzResMsg.ClosingGhzPlayerInfoRes.Builder res = bulidOneClosingPlayerInfoRes();
        res.setLostCount(lostCount);
        res.setWinCount(winCount);
        res.setMaxPoint(getMaxPoint());//最高积分
        res.setZhuaHao(maxHuxi);//最大胡息
        res.setBopiPoint(point);
        res.addAllDahuCounts(DataMapUtil.toList(dahuCounts));
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
        res.setName(name);
        res.setSeat(seat);
        res.setSex(sex);
        res.setPoint(getTotalPoint());
        // 当前出的牌
        YyWhzTable table = getPlayingTable(YyWhzTable.class);
        List<YyWhzCard> nowDisCard = table.getNowDisCardIds();// 当前出的牌有玩家可操作
        List<Integer> outPais = new ArrayList<>(getOutPais());
        if (nowDisCard != null && !outPais.isEmpty() && outPais.contains(nowDisCard.get(0).getId()) && !table.getActionSeatMap().isEmpty()) {// 当牌桌有可做操作时 客户端不显示打出的牌
            outPais.remove((Integer) nowDisCard.get(0).getId());
        }
        res.addAllOutedIds(outPais);
        res.addAllMoldCards(buildGhzCards(lookUid));
        // ////////////////////////////////
        if (table.getDisCardSeat() == seat) {
            if (nowDisCard != null && !nowDisCard.isEmpty()) {
                int selfMo = 0;
                if (table.isSelfMo(this)) {
                    selfMo = 1;
                }
                res.addOutCardIds(selfMo);
                YyWhzCard beremoveCard = table.getBeRemoveCard();
                if (beremoveCard != null && nowDisCard.contains(beremoveCard)) {
                    // 被移掉的牌
                } else {
                    res.addAllOutCardIds(YyWhzTool.toGhzCardIds(nowDisCard));
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
            res.setStatus(YyWhzConstant.state_player_ready);
        } else {
            res.setStatus(0);
        }

        res.addExt(isAutoPlay() ? 1 : 0);// 0
        res.addExt(getAutoPlayCheckedTime() > table.getAutoTimeOut() ? 1 : 0);//1

        if (isrecover) {
            // 0是否要的起 1是否报单 2是否暂离(1暂离0在线)
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

    @Override
    public void initPais(String handPai, String outPai) {
        if (!StringUtils.isBlank(outPai)) {
            String[] values = outPai.split(";");
            int i = -1;
            for (String value : values) {
                i++;
                if (i == 0) {
                    this.outPais = YyWhzTool.explodeGhz(value, ",");
                } else {
                    YyWhzCardTypeHuxi type = new YyWhzCardTypeHuxi();
                    type.init(value);
                    cardTypes.add(type);
                }
            }
        }
        if (!StringUtils.isBlank(handPai)) {
            String[] values = handPai.split(";");
            int i = 0;
            String outPaiStr = StringUtil.getValue(values, i++);
            String piaoPaiStr = StringUtil.getValue(values, i++);
            String liuPaiStr = StringUtil.getValue(values, i++);
            String weiPaiStr = StringUtil.getValue(values, i++);
            String pengPaiStr = StringUtil.getValue(values, i++);
            String chiPaiStr = StringUtil.getValue(values, i++);
            this.handPais = YyWhzTool.explodeGhz(outPaiStr, ",");
            this.piao = YyWhzTool.explodeGhz(piaoPaiStr, ",");
            this.liu = YyWhzTool.explodeGhz(liuPaiStr, ",");
            this.wei = YyWhzTool.explodeGhz(weiPaiStr, ",");
            this.peng = YyWhzTool.explodeGhz(pengPaiStr, ",");
            this.chi = YyWhzTool.explodeGhz(chiPaiStr, ",");
        }
    }

    @Override
    public void endCompetition1() {

    }

    public void dealHandPais(List<YyWhzCard> list) {
        this.handPais = list;
        setFristDisCard(true);
        getPlayingTable().changeCards(seat);
    }

    public int getWinCount() {
        return winCount;
    }

    @Override
    public int getLostCount() {
        return lostCount;
    }

    public void setWinCount(int winCount) {
        this.winCount = winCount;
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

    public int getFirstLiu() {
        return firstLiu;
    }

    public void setFirstLiu(int firstLiu) {
        this.firstLiu = firstLiu;
        changeTableInfo();
    }

    public void calcResult(int count, int point, boolean huangzhuang) {
        if (!huangzhuang) {
            if (point > 0) {
                this.winCount += count;
            } else {
                this.lostCount += count;
            }
        }

        changePoint(point);

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

    /**
     * 总胡息
     *
     * @return
     */
    public int getTotalHu() {
        return 0;
    }

    public List<PhzHuCards> buildGhzHuCards() {
        List<PhzHuCards> list = new ArrayList<>();
        for (YyWhzCardTypeHuxi type : cardTypes) {
            list.add(type.buildMsg().build());
        }
        if (hu != null && hu.getGhzHuCards() != null) {
            YyWhzCardTypeHuxi huCardType = null;
            YyWhzCardTypeHuxi twoCardType = null;
            for (YyWhzCardTypeHuxi type : hu.getGhzHuCards()) {
                if (hu.getCheckCard() != null && type.getCardIds().contains(hu.getCheckCard().getId())) {// 胡的牌放到最后面
                    huCardType = type;
                } else if (type.getCardIds().size() == 2) {
                    twoCardType = type;
                } else {
                    list.add(type.buildMsg().build());
                }
            }
            if (huCardType != null)
                list.add(huCardType.buildMsg().build());
            if (twoCardType != null)
                list.add(twoCardType.buildMsg().build());
        } else {
            YyWhzCardTypeHuxi handPaisType = new YyWhzCardTypeHuxi(-1, getHandPais(), 0, true);
            list.add(handPaisType.buildMsg().build());
        }
        return list;
    }

    public List<PhzHuCards> buildGhzCards(long lookUid) {
        List<PhzHuCards> list = new ArrayList<>();
        for (YyWhzCardTypeHuxi type : cardTypes) {
            list.add(type.buildMsg().build());
        }
        return list;
    }

    /**
     * 检查听牌
     * @param table
     * @param disPlayer
     * @return
     */
//	public boolean isTing(YjGhzTable table, YjGhzPlayer disPlayer){
//		List<YjGhzCard> checkCards = new ArrayList<>(table.getLeftCards());
//		for(Player player : table.getPlayerMap().values()){
//			if(player.getUserId() != disPlayer.getUserId()){
//				YjGhzPlayer ghzPlayer = (YjGhzPlayer) player;
//				List<YjGhzCard> cards = YjGhzTool.toGhzCards(ghzPlayer.getHandPais());  //玩家手牌
//				for(YjGhzCardTypeHuxi huxi : ghzPlayer.cardTypes){
//					if(huxi.getAction() == YjGhzDisAction.action_liu){
//						cards.addAll(YjGhzTool.toGhzCards(huxi.getCardIds()));  //玩家直接溜的牌
//					}
//				}
//				checkCards.addAll(cards);
//			}
//		}
//		if(tingCards != null && tingCards.size() > 0){
//			tingCards.clear();
//		}
//		List<Integer> cheked = new ArrayList<>();  //已检查牌值集
//		for(YjGhzCard card : checkCards){
//			if(!cheked.contains(card.getVal())){
//				YjGhzHuLack lack = disPlayer.checkHu(card,true);
//				if(lack.isHu())
//				{
//					if(!tingCards.contains(card.getVal())){
//						tingCards.add(card.getVal());
//					}
//				}
//				cheked.add(card.getVal());
//			}
//		}
//		if(tingCards != null && tingCards.size() > 0){
//			return true;
//		}
//		return false;
//	}

    /**
     * 胡牌时初始化大胡(名堂)列表
     *
     * @return
     */
    public Map<Integer, Integer> initDahuList(YyWhzTable nxGhzTable,List<Integer> mtList, boolean isBegin, boolean isWeiHouHu,YyWhzCard nowDisCard) {
        List<YyWhzCard> allCards = new ArrayList<>();
        List<YyWhzCardTypeHuxi> allHuxiCards = new ArrayList<>();
        for (YyWhzCardTypeHuxi type : cardTypes) {
            allCards.addAll(YyWhzTool.toGhzCards(type.getCardIds()));
            allHuxiCards.add(type);
        }
        if (hu != null && hu.getGhzHuCards() != null) {
            allHuxiCards.addAll(hu.getGhzHuCards());
            for (YyWhzCardTypeHuxi type : hu.getGhzHuCards()) {
                allCards.addAll(YyWhzTool.toGhzCards(type.getCardIds()));
            }
        }
        if (allCards.isEmpty()) {
            allCards.addAll(getHandGhzs());
        }
        Map<Integer, Integer> yuanMap = YyWhzMingTangRule.calcMingTang(nxGhzTable,mtList, this, hu, allCards, allHuxiCards, isBegin,isWeiHouHu,nowDisCard);
        if (isWeiHouHu) {
            // 偎后胡，不算吊吊手
            mtList.remove(new Integer(YyWhzMingTangRule.quanqiuren));
        }
        setDahu(mtList, yuanMap);
        return yuanMap;
    }

    public int getTableHuxi() {
        int baseCount = wei.size() / 3 + liu.size() / 4;
        return baseCount * 5;
    }

    /**
     * 算分初始化
     * 先算基础分 溜 偎 坎算5分
     * 再算加分  大胡列表加分
     *
     * @param dahuList 大胡列表
     * @param yuanMap  内圆 外圆个数
     * @return
     */
    public int initHuxiPoint(List<Integer> dahuList, Map<Integer, Integer> yuanMap, int maxhuxi,boolean isSelfMo,int allhuxi) {
        // --------------基础底分 -------------------------------------
        int baseCount = allhuxi;
        
//        if(dahuList.contains(YyWhzMingTangRule.quanqiuren)){
//        	if(isSelfMo){
//        		allhuxi +=4;
//        	}else{
//        		allhuxi +=1;
//        	}
//        }
        if(baseCount > this.maxHuxi){
        	this.maxHuxi = baseCount;
        }
//        System.out.println("基础胡息："+baseCount);
        this.setJichufen(baseCount);
        int basePoint = baseCount;
        // ------------------大胡------------------------------
        /**
         * 1 对子息：对子息：胡牌全部为碰或歪或溜或飘的牌，最后计分结算分数时x8
         * 2 大字胡：胡牌时全部为大字，最后计分结算分数时x8
         * 3 小子胡：胡牌时全部为小字，最后计分结算分数时x8
         * 4 火火翻：胡牌时有10个红字，最后计分结算分数时x2；11个红字，最后计分结算分数时x4；12个红字，最后计分结算分数时x8；以此类推，多一个红字翻一翻。
         * 5 行行息：没有顺子，句句是息，最后计分结算分数时x4
         * 6 黑胡子：胡牌时没有一个红字，最后计分结算分数时x8
         * 7 黑对子胡：胡牌时全部为碰或歪或溜，且没有一个红字，胡牌算对子息x黑胡子（8x4=32）。
         * 8 天地胡：庄家起手胡牌，算天胡；闲家胡庄家打出的第一张牌，算地胡；天地胡胡牌最后计算分数时x4.
         * 9 一点红：胡牌时，手上只有一个红字，最后计算分数x4.
         * 10 海底捞：海底捞胡牌后x4（勾选）
         * 11 报听：庄家起手打出一张牌后听牌，闲家起手听牌，胡牌后计算分数时x4.（飘了不算报听）
         * 12 神腰：玩家胡牌后，如果胡牌的那张牌的下一张翻开后和第一局牌的亮牌都是同为单数或者双数，最后计分结算分数时x2。海底时海底牌为神牌。
         * 13 单调：手里剩一张后胡牌（客户端不管这个）
         */
        int multiple = 1;
        for (int daHu : dahuList) {
            switch (daHu) {
	            case YyWhzMingTangRule.haiDi:
	        	case YyWhzMingTangRule.yiDianZhu:
	        	case YyWhzMingTangRule.xiangXiangXi:
	        	case YyWhzMingTangRule.tianHu:
	        	case YyWhzMingTangRule.baoTing:
	        		multiple*= 4;
	        		break;
	        	case YyWhzMingTangRule.heiHu:
	        	case YyWhzMingTangRule.duiZiHu:
	            case YyWhzMingTangRule.daZiHu:
	            case YyWhzMingTangRule.xiaoZiHu:
	            	multiple*= 8;
	            	break;
	            case YyWhzMingTangRule.heiDuiZiHu:
	            case YyWhzMingTangRule.huahuzi:
	            	multiple*= 64;
	            	break;
	            case YyWhzMingTangRule.shiSanHuo:
	            case YyWhzMingTangRule.danpiao:
	            case YyWhzMingTangRule.shuangpiao:
	            case YyWhzMingTangRule.yin:
	            	multiple*= 2;
	            	break;
            }
            if(daHu > 100){
//            	multiple += (daHu - 100) * 2; //10红以上
            	multiple /=2;
            	multiple *= (int)(Math.pow(2, (daHu - 100+1)));
            }
        }
//        System.out.println("翻数："+multiple);
        basePoint *=multiple;

        // --------------------------内圆--------------------------------
        if (yuanMap.get(1) > 0) {
//            basePoint *= Math.pow(4, yuanMap.get(1));
        	basePoint *= 4*yuanMap.get(1);
//        	System.out.println("内元："+4*yuanMap.get(1));
        }
        // --------------------------外圆--------------------------------
        if (yuanMap.get(2) > 0) {
//            basePoint *= Math.pow(2, yuanMap.get(2));
        	basePoint *= 2*yuanMap.get(2);
//        	System.out.println("外元："+2*yuanMap.get(2));
        }
        if(dahuList.contains(YyWhzMingTangRule.shenyao)){//神腰
        	basePoint *= 2;
        }
//        System.out.println("神腰："+2);
        
        YyWhzTable table = getPlayingTable(YyWhzTable.class);
        if (table != null) {
            StringBuilder sb = new StringBuilder("YyWhz");
            sb.append("|").append(table.getId());
            sb.append("|").append(table.getPlayBureau());
            sb.append("|").append(this.getUserId());
            sb.append("|").append(this.getSeat());
            sb.append("|").append(this.isAutoPlay() ? 1 : 0);
            sb.append("|").append("initHuxiPoint");
            sb.append("|").append(basePoint);
            sb.append("|").append(maxhuxi);
            sb.append("|").append(dahuList);
            LogUtil.msgLog.info(sb.toString());
        }
        if (basePoint > maxhuxi && maxhuxi >0) {
            return maxhuxi;
        } else {
            return basePoint;
        }
    }
    
    public List<YyWhzCard> getAllCards() {
        List<YyWhzCard> cards = new ArrayList<>();
        cards.addAll(piao);
        cards.addAll(liu);
        cards.addAll(wei);
        cards.addAll(peng);
        cards.addAll(chi);
        cards.addAll(handPais);
        return cards;
    }

    /**
     * cardVal牌是否在containCards其中
     *
     * @param containCards
     * @param cardVal
     * @return
     */
    public boolean containsCard(List<YyWhzCard> containCards, int cardVal) {
        if (containCards != null) {
            for (YyWhzCard card : containCards) {
                if (card.getVal() == cardVal)
                    return true;
            }
        }
        return false;
    }

    public YyWhzHuLack getHu() {
        return hu;
    }

    public void setHu(YyWhzHuLack hu) {
        this.hu = hu;
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

    public boolean isDisCardForBaoTingHu() {
        return isDisCardForBaoTingHu;
    }

    public void setDisCardForBaoTingHu(boolean disCardForBaoTingHu) {
        isDisCardForBaoTingHu = disCardForBaoTingHu;
        changeTableInfo();
    }

    public List<Integer> getPassChi() {
        return passChi;
    }

    public List<Integer> getPassPeng() {
        return passPeng;
    }

    /**
     * 获取玩家已吃牌的组数（除去2710）
     *
     * @return
     */
    public int getChiSizeExcept2710() {
        if (chi.isEmpty())
            return 0;
        else {
            int chiSize = chi.size() / 3;
            for (int i = 0; i < chi.size(); i += 3) {
                List<YyWhzCard> chiCards = chi.subList(i + 0, i + 3);
                List<Integer> cardPais = YyWhzTool.toGhzCardVals(chiCards, false);
                if (YyWhzTool.c2710List.containsAll(cardPais)) {
                    chiSize--;
                }
            }
            return chiSize;
        }
    }

    /**
     * 剔除吃过的牌的门子后 是否能吃
     *
     * @param preChiMenzi 预吃的门子
     * @param chiCard     吃的牌
     * @return
     */
    public boolean getHasChiMenzi(List<YyWhzCard> preChiMenzi, YyWhzCard chiCard) {
        List<List<YyWhzCard>> hasChiMenziList = new ArrayList<>();
        if (!chi.isEmpty()) {
            for (int i = 0; i < chi.size(); i += 3) {
                hasChiMenziList.add(chi.subList(i + 1, i + 3));
            }
        }
        if (preChiMenzi != null && !YyWhzTool.isSameCard(preChiMenzi))// 加上准备吃的门子
            hasChiMenziList.add(preChiMenzi);
        if (!hasChiMenziList.isEmpty()) {
            for (List<YyWhzCard> menzi : hasChiMenziList) {
                if (YyWhzTool.checkChi(menzi, chiCard).size() == menzi.size()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取碰过或者偎过的牌
     * @return
     */
    /**
     * @param preCard 预碰的牌
     * @return
     */
    public List<Integer> getHasPengOrWeiPais(YyWhzCard preCard) {
        List<Integer> hasPengOrWeiList = new ArrayList<>();
        if (!peng.isEmpty()) {
            for (int i = 0; i < peng.size(); i++) {
                if (i % 3 == 0) {
                    hasPengOrWeiList.add(peng.get(i).getVal());
                }
            }
        }
        if (!wei.isEmpty()) {
            for (int i = 0; i < wei.size(); i++) {
                if (i % 3 == 0) {
                    hasPengOrWeiList.add(wei.get(i).getVal());
                }
            }
        }
        if (preCard != null)
            hasPengOrWeiList.add(preCard.getVal());
        return hasPengOrWeiList;
    }

    public void removePassChi(int val) {
        if (passChi.contains(val)) {
            int index = passChi.indexOf(val);
            passChi.remove(index);
            changeTableInfo();
        }
    }

    public List<YyWhzMenzi> getPassMenzi() {
        return passMenzi;
    }

    /**
     * 检查玩家是否还能出牌  (偎 碰 吃)
     *
     * @param operateCards 当前操作的牌
     * @param action
     * @return
     */
    public boolean checkCanDiscard(List<YyWhzCard> operateCards, int action) {
        List<YyWhzCard> copyHandPais = new ArrayList<>(handPais);
        for (YyWhzCard operateCard : operateCards) {
            copyHandPais.remove(operateCard);
        }
        YyWhzCard prePengCard = null;
        List<YyWhzCard> preChiMenzi = null;
        if (action == YyWhzDisAction.action_chi) {
            preChiMenzi = operateCards.subList(1, 3);
        } else if (action == YyWhzDisAction.action_peng) {
            prePengCard = operateCards.get(0);
        }else if (action == YyWhzDisAction.action_wei) {
            prePengCard = operateCards.get(0);
        }
        for (YyWhzCard operateCard : copyHandPais) {
            if (!getHasPengOrWeiPais(prePengCard).contains((Integer) operateCard.getVal()) && !getHasChiMenzi(preChiMenzi, operateCard)) {// 已吃过的门子 或者碰过的牌 不能打出去了
                return true;
            }
        }
        this.setSiShouTemp(true);
        return false;
    }

    public static final List<Integer> wanfaList = Arrays.asList(GameUtil.play_type_yiyangwhz);

    public static void loadWanfaPlayers(Class<? extends Player> cls) {
        for (Integer integer : wanfaList) {
            PlayerManager.wanfaPlayerTypesPut(integer, cls, YyWhzCommandProcessor.getInstance());
        }
    }

    public boolean isAutoPlay() {
        return autoPlay;
    }

    public void setAutoPlay(boolean autoPlay, BaseTable table) {
        if (this.autoPlay != autoPlay) {
            ComMsg.ComRes.Builder res = SendMsgUtil.buildComRes(132, seat, autoPlay ? 1 : 0, (int) userId);
            GeneratedMessage msg = res.build();
            for (Map.Entry<Long, Player> kv : table.getPlayerMap().entrySet()) {
                Player player = kv.getValue();
                if (player.getIsOnline() == 0) {
                    continue;
                }
                player.writeSocket(msg);
            }
            if (!getHandPais().isEmpty()) {
                table.addPlayLog(getSeat(), YyWhzConstant.action_tuoguan + "", (autoPlay ? 1 : 0) + "");
            }
        }
        this.autoPlay = autoPlay;
        this.setCheckAuto(false);
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

    public boolean isCheckAuto() {
        return isCheckAuto;
    }

    public void setCheckAuto(boolean checkAuto) {
        isCheckAuto = checkAuto;
    }

    public boolean isAlreadyMo() {
        int count = handPais.size();
        count += outPais.size();
        count += chi.size();
        count += peng.size();
        count += wei.size();
        count += liu.size();
        count += piao.size();
        return count == 21;
    }

    public void clearPassChiTemp() {
        passChiTemp.clear();
        passMenziTemp.clear();
        changeTableInfo();
    }
    public boolean hasPassChiTemp(){
        return passChiTemp != null && passChiTemp.size() > 0;
    }

    public void resetPassChi() {
        boolean needSend = false;
        if (passChiTemp.size() > 0) {
            if (passChi.contains(passChiTemp.get(0))) {
                passChi.remove(Integer.valueOf(passChiTemp.get(0)));
                needSend = true;
            }
        }
        if (passMenziTemp.size() > 0) {
            for (YyWhzMenzi mz : passMenziTemp) {
                passMenzi.remove(mz);
            }
            needSend = true;
        }

        if (needSend) {
            List<Integer> intExts = new ArrayList<>();
            if (!this.passMenzi.isEmpty()) {
                for (YyWhzMenzi mz : this.passMenzi) {
                    List<Integer> mzIds = YyWhzTool.toGhzCardIds(YyWhzTool.findByVals(getHandGhzs(), mz.getMenzi()));
                    if (mzIds.size() >= 2) {
                        intExts.addAll(mzIds);
                    }
                }
            }
            writeComMessage(WebSocketMsgType.res_code_yiyangwhz_resetGuoChi, JSON.toJSONString(intExts));
        }
    }

    public boolean isSiShou() {
        return isSiShou;
    }

    public void setSiShou(boolean siShou) {
        isSiShou = siShou;
        changeTableInfo();
    }

    public boolean isSiShouTemp() {
        return isSiShouTemp;
    }

    public void setSiShouTemp(boolean siShouTemp) {
        isSiShouTemp = siShouTemp;
        changeTableInfo();
    }

    public Set<Integer> getSiShouPais() {
        return siShouPais;
    }

    public void setSiShouPais(Set<Integer> siShouPais) {
        this.siShouPais = siShouPais;
        changeTableInfo();
    }

    public void addSiShouPai(Integer val){
        siShouPais.add(val);
        changeTableInfo();
    }
    public void addTingpai(Integer val){
    	if(tingpai.contains(val)){
    		return;
    	}
    	tingpai.add(val);
    	changeTableInfo();
    }

    public List<Integer> getTingpais(){
    	return tingpai;
    }
    
    public void clearTingpai(){
    	tingpai.clear();
    	changeTableInfo();
    	
    }

	public int getWailiufen() {
		return wailiufen;
	}


	public void setWailiufen(int wailiufen) {
		this.wailiufen = wailiufen;
	}


	public int getJichufen() {
		return jichufen;
	}


	public void setJichufen(int jichufen) {
		this.jichufen = jichufen;
	}
    
}
