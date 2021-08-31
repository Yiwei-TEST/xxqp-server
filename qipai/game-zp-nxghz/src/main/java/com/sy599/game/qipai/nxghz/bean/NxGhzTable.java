package com.sy599.game.qipai.nxghz.bean;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.sy599.game.GameServerConfig;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.common.constant.SharedConstants.player_state;
import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.db.bean.TableInf;
import com.sy599.game.db.bean.UserGroupPlaylog;
import com.sy599.game.db.bean.UserPlaylog;
import com.sy599.game.db.dao.TableDao;
import com.sy599.game.db.dao.TableLogDao;
import com.sy599.game.manager.TableManager;
import com.sy599.game.msg.serverPacket.PlayCardResMsg;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.PlayPaohuziRes;
import com.sy599.game.msg.serverPacket.TableGhzResMsg.ClosingGhzInfoRes;
import com.sy599.game.msg.serverPacket.TableGhzResMsg.ClosingGhzPlayerInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.CreateTableRes;
import com.sy599.game.msg.serverPacket.TableRes.DealInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes;
import com.sy599.game.qipai.nxghz.constant.NxGhzCard;
import com.sy599.game.qipai.nxghz.constant.NxGhzConstant;
import com.sy599.game.qipai.nxghz.rule.NxGhzCardIndexArr;
import com.sy599.game.qipai.nxghz.rule.NxGhzIndex;
import com.sy599.game.qipai.nxghz.rule.NxGhzMenzi;
import com.sy599.game.qipai.nxghz.rule.NxGhzRobotAI;
import com.sy599.game.qipai.nxghz.tool.NxGhzHuLack;
import com.sy599.game.qipai.nxghz.tool.NxGhzResTool;
import com.sy599.game.qipai.nxghz.tool.NxGhzTool;
import com.sy599.game.staticdata.KeyValuePair;
import com.sy599.game.udplog.UdpLogger;
import com.sy599.game.util.DataMapUtil;
import com.sy599.game.util.GameUtil;
import com.sy599.game.util.JacksonUtil;
import com.sy599.game.util.JsonWrapper;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.PayConfigUtil;
import com.sy599.game.util.ResourcesConfigsUtil;
import com.sy599.game.util.StringUtil;
import com.sy599.game.util.TimeUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

/**
 * 南县鬼胡子
 */
public class NxGhzTable extends BaseTable {
    /**
     * 玩家map
     */
    private Map<Long, NxGhzPlayer> playerMap = new ConcurrentHashMap<>();
    /**
     * 座位对应的玩家
     */
    private Map<Integer, NxGhzPlayer> seatMap = new ConcurrentHashMap<>();
    /**
     * 0溜  1漂 2偎 3胡 4 碰 5吃 6九对半天胡
     */
    private Map<Integer, List<Integer>> actionSeatMap = new ConcurrentHashMap<>();

    /**
     * 复制
     */
    private Map<Integer, List<Integer>> actionSeatMapCopy = new ConcurrentHashMap<>();

    /**
     * 玩家位置对应临时操作
     * 当同时存在多个可做的操作时
     * 1当优先级最高的玩家最先操作 则清理所有操作提示 并执行该操作
     * 2当操作优先级低的玩家先选择操作，则玩家先将所做操作存入临时操作中 当玩家优先级最高的玩家操作后 在判断临时操作是否可执行并执行
     */
    private Map<Integer, NxGhzTempAction> tempActionMap = new ConcurrentHashMap<>();
    /**
     * 开局所有底牌
     */
    private List<Integer> startLeftCards = new ArrayList<>();
    /**
     * 当前桌面底牌
     */
    private List<NxGhzCard> leftCards = new ArrayList<>();
    /**
     * 当前打出的牌
     */
    private List<NxGhzCard> nowDisCardIds = new ArrayList<>();
    /**
     * 摸牌flag
     */
    private int moFlag;
    /**
     * 应该要打牌的flag
     */
    private int toPlayCardFlag;
    /**
     * 当前摸牌的座位号
     */
    private int moSeat;
    /**
     * 桌子上移除的牌
     */
    private NxGhzCard beRemoveCard;
    /**
     * 玩家人数
     */
    private int maxPlayerCount = 3;
    /**
     * 已确认胡的座位号
     */
    private List<Integer> huConfirmList = new ArrayList<>();
    /**
     * 摸牌时对应的座位
     */
    private KeyValuePair<Integer, Integer> moSeatPair;
    /**
     * 摸牌时对应的座位
     */
    private KeyValuePair<Integer, Integer> checkMoMark;
    /**
     * 是否有操作过
     */
    private boolean firstCard = true;
    /**
     * 封顶息数  100息 200息
     */
    private int maxhuxi;
    /**
     * true可漂 false不可漂
     */
    private boolean kepiao;
    /**
     * true有无息平 false没有无息平
     */
    private boolean wuxiping;
    /**
     * true有吊吊手 false没有
     */
    private boolean diaodiaoshou;

    public List<Integer> getStartLeftCards() {
        return startLeftCards;
    }

    private long groupPaylogId = 0;  //俱乐部战绩记录id

    /**
     * 托管时间
     */
    private volatile int autoTimeOut = Integer.MAX_VALUE;
    private volatile int autoTimeOut2 = Integer.MAX_VALUE;

    // 是否加倍：0否，1是
    private int jiaBei;
    // 加倍分数：低于xx分进行加倍
    private int jiaBeiFen;
    // 加倍倍数：翻几倍
    private int jiaBeiShu;

    /**
     * 托管1：单局，2：全局
     */
    private int autoPlayGlob;
    private int autoTableCount;

    private volatile int timeNum = 0;

    // 低于 below 加 belowAdd 分
    private int belowAdd = 0;
    private int below = 0;

    // 抽牌，0表示不抽，10，19表示抽牌的数量
    private int chouPai = 0;
    // 抽牌牌堆
    List<Integer> chouCards = new ArrayList<>();

    //大小卓  0：小卓  1：大卓
    private int daxiaoZhuo = 0;
    private int beikaobei = 0;
    private int shouqianshou = 0;
    
    /*** 临时开关，报听胡新标识，为true时，检查player.isDisCard状态判定报听胡，默认为false,新创建的房间为true***/
    private boolean baoTingHuSwitchTemp = false;

    @Override
    public void initExtend0(JsonWrapper wrapper) {
        String hu = wrapper.getString(1);
        if (!StringUtils.isBlank(hu)) {
            huConfirmList = StringUtil.explodeToIntList(hu);
        }
        moFlag = wrapper.getInt(2, 0);
        toPlayCardFlag = wrapper.getInt(3, 0);
        moSeat = wrapper.getInt(4, 0);
        String moSeatVal = wrapper.getString(5);
        if (!StringUtils.isBlank(moSeatVal)) {
            moSeatPair = new KeyValuePair<>();
            String[] values = moSeatVal.split("_");
            String idStr = StringUtil.getValue(values, 0);
            if (!StringUtil.isBlank(idStr)) {
                moSeatPair.setId(Integer.parseInt(idStr));
            }
            moSeatPair.setValue(StringUtil.getIntValue(values, 1));
        }
        firstCard = wrapper.getInt(6, 1) == 1 ? true : false;
        beRemoveCard = NxGhzCard.getPaohzCard(wrapper.getInt(7, 0));
        maxPlayerCount = wrapper.getInt(8, 3);
        maxhuxi = wrapper.getInt(9, 200);// 默认两百息
        kepiao = wrapper.getInt(10, 1) == 1 ? true : false;
        wuxiping = wrapper.getInt(11, 1) == 1 ? true : false;
        isAAConsume = Boolean.parseBoolean(wrapper.getString(12));
        diaodiaoshou = wrapper.getInt(13, 1) == 1 ? true : false;
        startLeftCards = loadStartLeftCards(wrapper.getString("startLeftCards"));
        tempActionMap = loadTempActionMap(wrapper.getString("tempActions"));
        groupPaylogId = wrapper.getLong(14, 0);
        autoPlayGlob = wrapper.getInt(15, 0);
        autoTimeOut = wrapper.getInt(16, 0);
        jiaBei = wrapper.getInt(17, 0);
        jiaBeiFen = wrapper.getInt(18, 0);
        jiaBeiShu = wrapper.getInt(19, 0);
        below = wrapper.getInt(20, 0);
        belowAdd = wrapper.getInt(21, 0);
        this.chouPai = wrapper.getInt(22, 0);
        String chouCardsStr = wrapper.getString(23);
        if (StringUtils.isNotBlank(chouCardsStr)) {
            this.chouCards = StringUtil.explodeToIntList(chouCardsStr);
        }
        this.baoTingHuSwitchTemp = wrapper.getInt(24, 0) == 1;
        daxiaoZhuo = wrapper.getInt(25, 0);
        beikaobei = wrapper.getInt(26, 0);
        shouqianshou = wrapper.getInt(27, 0);
    }

    private Map<Integer, NxGhzTempAction> loadTempActionMap(String json) {
        Map<Integer, NxGhzTempAction> map = new ConcurrentHashMap<>();
        if (json == null || json.isEmpty())
            return map;
        JSONArray jsonArray = JSONArray.parseArray(json);
        for (Object val : jsonArray) {
            String str = val.toString();
            NxGhzTempAction tempAction = new NxGhzTempAction();
            tempAction.initData(str);
            map.put(tempAction.getSeat(), tempAction);
        }
        return map;
    }

    private List<Integer> loadStartLeftCards(String json) {
        List<Integer> list = new ArrayList<>();
        if (json == null || json.isEmpty()) return list;
        JSONArray jsonArray = JSONArray.parseArray(json);
        for (Object val : jsonArray) {
            list.add(Integer.valueOf(val.toString()));
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getPlayer(long id, Class<T> cl) {
        return (T) playerMap.get(id);
    }

    @Override
    protected void initNowAction(String nowAction) {
        JsonWrapper wrapper = new JsonWrapper(nowAction);
        String val1 = wrapper.getString(1);
        if (!StringUtils.isBlank(val1)) {
            actionSeatMap = DataMapUtil.toListMap(val1);
        }
        String val2 = wrapper.getString(2);
        if (!StringUtils.isBlank(val2)) {
            actionSeatMapCopy = DataMapUtil.toListMap(val1);
        }
    }

    @Override
    protected String buildNowAction() {
        JsonWrapper wrapper = new JsonWrapper("");
        wrapper.putString(1, DataMapUtil.explodeListMap(actionSeatMap));
        wrapper.putString(2, DataMapUtil.explodeListMap(actionSeatMapCopy));
        return wrapper.toString();
    }

    @Override
    protected boolean quitPlayer1(Player player) {
        return false;
    }

    @Override
    protected boolean joinPlayer1(Player player) {
        return false;
    }

    @Override
    public void calcOver() {
    	if (getState() != table_state.play) {
            return; 
    	}
		if(getPlayBureau() >= getMaxPlayerCount()){
		  changeTableState(table_state.over);
		}
        boolean isHuangZhuang = false;
        List<Integer> winList = new ArrayList<>(huConfirmList);
        if (winList.size() == 0 && leftCards.size() == 0) {// 流局
            isHuangZhuang = true;
        }
        int maxFan = 1;
        List<Integer> mt = null;
        int winPoint = 0;
        for (int winSeat : winList) {// 赢的玩家
            NxGhzPlayer winPlayer = seatMap.get(winSeat);
            winPoint = winPlayer.getLostPoint();
            int getPoint = 0;
            for (int seat : seatMap.keySet()) {
                if (!winList.contains(seat)) {
                    NxGhzPlayer player = seatMap.get(seat);
                    getPoint += winPoint;
                    player.calcResult(1, -winPoint, isHuangZhuang);
                }
            }
            winPlayer.calcResult(1, getPoint, isHuangZhuang);
        }
        NxGhzPlayer winPlayer = null;
        boolean selfMo = false;
        if (!winList.isEmpty()) {
            winPlayer = seatMap.get(winList.get(0));
            selfMo = winPlayer.getSeat() == moSeat;
        }
        boolean isOver = playBureau >= totalBureau;
        if (autoPlayGlob > 0) {
            //是否解散
            boolean diss = false;
            if (autoPlayGlob == 1) {
                for (NxGhzPlayer seat : seatMap.values()) {
                    if (seat.isAutoPlay()) {
                        diss = true;
                        break;
                    }
                }
            } else if (autoPlayGlob == 3) {
                diss = checkAuto3();
            }
            if (diss) {
                autoPlayDiss = true;
                isOver = true;
            }
        }

        ClosingGhzInfoRes.Builder res = sendAccountsMsg(isOver, selfMo, winList, maxFan, mt, winPoint, false);
        saveLog(isOver, 0L, res.build());
        if (!winList.isEmpty()) {
            setLastWinSeat(winList.get(0));
        } else {
            int next = getNextSeat(lastWinSeat);
            setLastWinSeat(next);
        }
        calcAfter();
        if (isOver) {
            calcOver1();
            calcOver2();
            calcOver3();
            diss();
        } else {
            initNext();
            calcOver1();
        }
        for (Player player : seatMap.values()) {
            player.saveBaseInfo();
        }
    }

    private boolean checkAuto3() {
        boolean diss = false;
        boolean diss2 = false;
        for (NxGhzPlayer seat : seatMap.values()) {
            if (seat.isAutoPlay()) {
                diss2 = true;
                break;
            }
        }
        if (diss2) {
            autoTableCount += 1;
        } else {
            autoTableCount = 0;
        }
        if (autoTableCount == 3) {
            diss = true;
        }
        return diss;
    }

    public void saveLog(boolean over, long winId, Object resObject) {
        LogUtil.d_msg("tableId:" + id + " play:" + playBureau + " over:" + resObject);
        ClosingGhzInfoRes res = (ClosingGhzInfoRes) resObject;
        String logRes = JacksonUtil.writeValueAsString(NxGhzTool.buildClosingInfoResLog(res));
        String logOtherRes = JacksonUtil.writeValueAsString(LogUtil.buildClosingInfoResOtherLog(res));
        Date now = TimeUtil.now();
        UserPlaylog userLog = new UserPlaylog();
        userLog.setLogId(playType);
        userLog.setTableId(id);
        userLog.setRes(extendLogDeal(logRes));
        userLog.setTime(now);
        userLog.setTotalCount(totalBureau);
        userLog.setCount(playBureau);
        userLog.setStartseat(lastWinSeat);
        userLog.setOutCards(playLog);
        userLog.setUserId(creatorId);
        userLog.setExtend(logOtherRes);

        long logId = TableLogDao.getInstance().save(userLog);
        if (isGroupRoom()) {
            UserGroupPlaylog userGroupLog = new UserGroupPlaylog();
            userGroupLog.setTableid(id);
            userGroupLog.setUserid(creatorId);
            userGroupLog.setCount(playBureau);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            userGroupLog.setCreattime(sdf.format(createTime));
            String players = "";
            String score = "";
            String diFenScore = "";
            for (int i = 1; i <= seatMap.size(); i++) {
                if (i == seatMap.size()) {
                    players += seatMap.get(i).getUserId();
                    score += seatMap.get(i).getTotalPoint();
                    diFenScore += seatMap.get(i).getTotalPoint();
                } else {
                    players += seatMap.get(i).getUserId() + ",";
                    score += seatMap.get(i).getTotalPoint() + ",";
                    diFenScore += seatMap.get(i).getTotalPoint() + ",";
                }
            }
            userGroupLog.setPlayers(players);
            userGroupLog.setScore(score);
            userGroupLog.setDiFenScore(diFenScore);
            userGroupLog.setDiFen("1");
            userGroupLog.setOvertime(sdf.format(now));
            userGroupLog.setPlayercount(maxPlayerCount);
            String groupId = isGroupRoom() ? loadGroupId() : 0 + "";
            userGroupLog.setGroupid(Long.parseLong(groupId));
            userGroupLog.setGamename("五硬息");
            userGroupLog.setTotalCount(totalBureau);
            if (playBureau == 1) {
                groupPaylogId = TableLogDao.getInstance().saveGroupPlayLog(userGroupLog);
            } else if (playBureau > 1 && groupPaylogId != 0) {
                userGroupLog.setId(groupPaylogId);
                TableLogDao.getInstance().updateGroupPlayLog(userGroupLog);
            }
        }
        saveTableRecord(logId, over, playBureau);
        for (NxGhzPlayer player : playerMap.values()) {
            player.addRecord(logId, playBureau);
        }
        UdpLogger.getInstance().sendSnapshotLog(masterId, playLog, logRes);
    }

    @Override
    protected void loadFromDB1(TableInf info) {
        if (!StringUtils.isBlank(info.getNowDisCardIds())) {
            this.nowDisCardIds = NxGhzTool.explodeGhz(info.getNowDisCardIds(), ",");
        }
        if (!StringUtils.isBlank(info.getLeftPais())) {
            this.leftCards = NxGhzTool.explodeGhz(info.getLeftPais(), ",");
        }
    }

    @Override
    protected void sendDealMsg() {
        sendDealMsg(0);
    }

    @Override
    protected void sendDealMsg(long userId) {// 天胡或者暗杠
        int lastCardIndex = RandomUtils.nextInt(20);
        for (NxGhzPlayer player : seatMap.values()) {
            DealInfoRes.Builder res = DealInfoRes.newBuilder();
            res.addAllHandCardIds(player.getHandPais());
            res.setNextSeat(lastWinSeat);
            res.setGameType(getWanFa());
            res.setRemain(leftCards.size());
            res.setBanker(lastWinSeat);
            res.addXiaohu(seatMap.get(lastWinSeat).getHandPais().get(lastCardIndex));
            player.writeSocket(res.build());
            player.setNeedCheckTing(true);
            player.setDisCardForBaoTingHu(false);
            sendTingInfo(player);
        }
    }

    @Override
    public int getWanFa() {
        return GameUtil.play_type_nxghz;
    }

    @Override
    public void startNext() {
        // 最后一个点击开始一下局的人，需要重新推送听牌信息
        if (getLastStartNextUser() != 0) {
            for (NxGhzPlayer player : seatMap.values()) {
                if (player.getUserId() == lastStartNextUser) {
                    sendTingInfo(player);
                    break;
                }
            }
        }
        checkAction();
    }

    public void play(NxGhzPlayer player, List<Integer> cardIds, int action) {
        play(player, cardIds, action, false, false, false);
    }

    private void hu(NxGhzPlayer player, List<NxGhzCard> cardList, int action, NxGhzCard nowDisCard) {
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return;
        }
        if (huConfirmList.contains(player.getSeat())) {
            return;
        }
        if (!checkAction(player, cardList, nowDisCard, action)) {
            player.writeComMessage(WebSocketMsgType.res_com_code_yj_guihz_skip);
            return;
        }
        if (action == NxGhzDisAction.action_jiuduiban_hu) {
            action = NxGhzDisAction.action_hu;
        }
        List<Integer> actionList = actionSeatMap.get(player.getSeat());
        if (actionList.get(3) != 1 && actionList.get(6) != 1) {
            return;
        }
        NxGhzHuLack hu = null;
        if (checkJiuDuiBanHu(player,nowDisCard)) {// 九对半胡验证
            hu = new NxGhzHuLack(0);
            hu.setHu(true);
            List<NxGhzCardTypeHuxi> huxiList = new ArrayList<>();
            List<Integer> handCardsCopy = new ArrayList<>(player.getHandPais());// 手牌
            if(nowDisCard != null){
            	handCardsCopy.add(nowDisCard.getId());
            }
            NxGhzCardTypeHuxi handPaisType = new NxGhzCardTypeHuxi(-1, handCardsCopy, 0, true);
            huxiList.add(handPaisType);
            hu.setCheckCard(nowDisCard);
            hu.setPhzHuCards(huxiList);
        }
        boolean isWeiHouHu = false;
        if (hu == null && player.getHandPais().size() % 3 == 2) {
            // 偎后胡
            hu = player.checkHu(null, isSelfMo(player));
            hu.setCheckCard(nowDisCard);
            isWeiHouHu = true;
        }
        if (hu == null) {
            hu = player.checkHu(nowDisCard, isSelfMo(player));
            isWeiHouHu = false;
        }
        if (hu != null && hu.isHu()) {
            sendActionMsg(player, action, null, NxGhzDisAction.action_type_action);// 发送胡消息
            addPlayLog(player.getSeat(), action + "", NxGhzTool.implodeGhz(cardList, ","));
            boolean isBegin = (nowDisCard == null) ? true : false;
            List<Integer> dahus = new ArrayList<>();
            player.setHu(hu);
            Map<Integer, Integer> yuanMap = player.initDahuList(this,dahus, isBegin, isWeiHouHu);
            int totalHuPoint = player.initHuxiPoint(dahus, yuanMap, maxhuxi,isDazhuo());
            hu.setYuanMap(yuanMap);
            player.setHu(hu);
            player.setLostPoint(totalHuPoint);
            huConfirmList.add(player.getSeat());
            resetPassChi(player, nowDisCard, action);
            clearAction();
            calcOver();
        } else {
            broadMsg(player.getName() + " 不能胡牌");
        }

    }

    /**
     * 是否自摸
     *
     * @param player
     * @return
     */
    public boolean isSelfMo(NxGhzPlayer player) {
        if (moSeatPair != null) {
            return moSeatPair.getValue() == player.getSeat();
        }
        return false;
    }

    /**
     * 溜：起手四张同牌，下牌不亮牌，称溜，算牌时叫内元，偎后再摸同张牌，也称溜（也可以吃），算牌时称内元，起手三张同牌，再自摸一张，亦称溜，亮一张，算内元
     *
     * @param player
     * @param cardList   溜的牌
     * @param nowDisCard 当前打出的牌
     * @param action
     */
    private void liu(NxGhzPlayer player, List<NxGhzCard> cardList, NxGhzCard nowDisCard, int action) {
        if (cardList == null || cardList.size() == 0) {
            LogUtil.msgLog.info("NxGhz|liu|error|" + getId() + "|" + player.getUserId() + "|" + player.getSeat() + "|" + player.getHandGhzs());
            return;
        }
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return;
        }
        if (isMoByPlayer(player)) {// 如果当前牌是玩家自己摸的牌
            List<NxGhzCard> cards = NxGhzTool.getSameCards(player.getHandGhzs(), nowDisCard);
            if (cards.size() == 3 && nowDisCard.getVal() != cardList.get(0).getVal()) {// 并且摸到的牌自己手上有三个  并且前端传过来的溜的牌不是自己摸的牌 则强制溜当前摸的牌
                cardList = NxGhzTool.getSameCards(player.getHandGhzs(), nowDisCard);
            }
        }
        if (!checkAction(player, cardList, nowDisCard, action)) {
            player.writeComMessage(WebSocketMsgType.res_com_code_yj_guihz_skip);
            return;
        }
        if (NxGhzTool.isHasCardVal(player.getWei(), cardList.get(0).getVal())) {// 如果是偎后溜
            cardList = NxGhzTool.findGhzByVal(player.getWei(), cardList.get(0).getVal());
            if (nowDisCard.getVal() == cardList.get(0).getVal() && !cardList.contains(nowDisCard))
                cardList.add(nowDisCard);
            if (cardList.size() <= 3) {
                List<NxGhzCard> handCards = NxGhzTool.findGhzByVal(player.getHandGhzs(), cardList.get(0).getVal());
                cardList.addAll(handCards);
            }
            action = NxGhzDisAction.action_weiHouLiu;
        } else {// 手里有四个
            cardList = NxGhzTool.getSameCards(player.getHandGhzs(), cardList.get(0));
            if (player.getHandGhzs().size() % 3 != 2 && cardList.size() <= 3) {
                cardList.add(nowDisCard);
                action = NxGhzDisAction.action_weiHouLiu;
            }
            if (player.getHandGhzs().size() % 3 == 2 && cardList.size() == 4 && (nowDisCardIds == null || nowDisCardIds.isEmpty()) && player.getSeat() == lastWinSeat) {
                player.setFirstLiu(1);// 庄家起手溜牌 算报听 单独记录起手溜的牌
            }
        }
        if (cardList.size() != 4) {
            LogUtil.errorLog.info("溜不合法:" + cardList);
            return;
        }
        if (!NxGhzTool.isSameCard(cardList)) {
            LogUtil.errorLog.info("溜不合法:" + cardList);
            return;
        }
        addPlayLog(player.getSeat(), action + "", NxGhzTool.implodeGhz(cardList, ","));
        if (nowDisCard != null) {
            setBeRemoveCard(nowDisCard);
            getDisPlayer().removeOutPais(nowDisCard);
        }
        player.disCard(action, cardList);
        resetPassChi(player, nowDisCard, action);
        clearAction();
        boolean disCard = setDisPlayer(player, action, false);
        sendActionMsg(player, action, cardList, NxGhzDisAction.action_type_action,true);
        player.setNeedCheckTing(true);
        player.setSiShouTemp(false);
        if (!disCard) {
            checkMo();
        }
    }

    /**
     * 漂  碰后再摸到同张字牌叫飘，飘牌不打牌，算外元
     *
     * @param player
     * @param cardList   漂的牌 只传一张过来
     * @param nowDisCard 当前摸到的牌
     * @param action
     */
    private void piao(NxGhzPlayer player, List<NxGhzCard> cardList, NxGhzCard nowDisCard, int action) {
        if (!isKepiao()) {//牌桌为不可漂
            return;
        }
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return;
        }
        if (!checkAction(player, cardList, nowDisCard, action)) {
            player.writeComMessage(WebSocketMsgType.res_com_code_yj_guihz_skip);
            return;
        }
        if (cardList.get(0).getVal() != nowDisCard.getVal()) {// 碰的牌和漂的牌不同
            LogUtil.msgLog.info("漂不合法:" + cardList);
            return;
        }
        if (!cardList.contains(nowDisCard)) {
            cardList.add(nowDisCard);
        }
        List<NxGhzCard> peng = player.getPeng();
        cardList.addAll(NxGhzTool.getSameCards(peng, nowDisCard));
        if (cardList.size() != 4) {// 碰的牌中没有要漂的牌
            LogUtil.msgLog.info("漂不合法2:" + cardList + "nowDisCard:" + nowDisCard);
            return;
        }
        setBeRemoveCard(nowDisCard);
        getDisPlayer().removeOutPais(nowDisCard);
        addPlayLog(player.getSeat(), action + "", NxGhzTool.implodeGhz(cardList, ","));
        player.disCard(action, cardList);
        resetPassChi(player, nowDisCard, action);
        clearAction();
        boolean disCard = setDisPlayer(player, action, false);
        sendActionMsg(player, action, cardList, NxGhzDisAction.action_type_action);
        player.setNeedCheckTing(true);
        if (!disCard) {
            checkMo();
        }
    }

    /**
     * 偎
     *
     * @param player
     * @param cardList   要偎的牌
     * @param nowDisCard
     * @param action
     */
    private void wei(NxGhzPlayer player, List<NxGhzCard> cardList, NxGhzCard nowDisCard, int action) {
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return;
        }
        if (player.getPassPeng().contains(nowDisCard.getVal())) {
            LogUtil.errorLog.info("臭碰的牌不能偎");
            return;
        }
        if (!checkAction(player, cardList, nowDisCard, action)) {
            player.writeComMessage(WebSocketMsgType.res_com_code_yj_guihz_skip);
            return;
        }
        boolean isFristDisCard = player.isFristDisCard();
        cardList = player.getPengOrWeiList(nowDisCard, cardList);
        if (cardList == null) {
            LogUtil.errorLog.info("不能偎");
            return;
        }
        if (!cardList.contains(nowDisCard)) {
            cardList.add(0, nowDisCard);
        }
        List<NxGhzCard> copyCardList = new ArrayList<>();
        copyCardList.addAll(cardList);
        copyCardList.remove(nowDisCard);

        boolean isSiShou = !player.checkCanDiscard(cardList, action);

        setBeRemoveCard(nowDisCard);
        getDisPlayer().removeOutPais(nowDisCard);
        addPlayLog(player.getSeat(), action + "", NxGhzTool.implodeGhz(cardList, ","));
        player.disCard(action, cardList);

//        clearActionForWei(player);
        resetPassChi(player, nowDisCard, action);
        clearAction();
        boolean disCard = setDisPlayer(player, action, isFristDisCard);

        if(isSiShou){
            if(NxGhzCheckCardBean.hasLiu(actionSeatMap.get(player.getSeat()))) {
                isSiShou = false;
            }else if(NxGhzCheckCardBean.hasHu(actionSeatMap.get(player.getSeat()))){
                isSiShou = false;
            }
        }
        if(isSiShou){
            removeAction(player.getSeat());
            player.setSiShou(true);
            markMoSeat(player.getSeat(), action);
            setToPlayCardFlag(0);
            setNowDisCardSeat(calcNextSeat(player.getSeat()));
            disCard = false;
        }

        sendActionMsg(player, action, cardList, NxGhzDisAction.action_type_action,true);
        player.setNeedCheckTing(true);
        if (!disCard) {
            checkMo();
        }
    }

    /**
     * 出牌
     */
    private void chuPai(NxGhzPlayer player, List<NxGhzCard> cardList, int action) {
        if (!actionSeatMap.isEmpty()) {
            LogUtil.e("动作:" + JacksonUtil.writeValueAsString(actionSeatMap));
            return;
        }
        if (!tempActionMap.isEmpty()) {
            LogUtil.e(player.getName() + "出牌清理临时操作！");
            clearTempAction();
        }
        if (toPlayCardFlag != 1) {
            LogUtil.e(player.getName() + "错误 toPlayCardFlag:" + toPlayCardFlag + "出牌");
            checkMo();
            return;
        }
        NxGhzCard disCard = cardList.get(0);
        if (player.getHasPengOrWeiPais(null).contains(disCard.getVal())) {
            LogUtil.errorLog.info("碰过或偎过的牌不能打出去！");
            return;
        }
        if (player.getHasChiMenzi(null, disCard)) {
            LogUtil.errorLog.info("吃过的牌不能打出去！");
            return;
        }
        if (player.getSeat() != nowDisCardSeat) {
            LogUtil.errorLog.info("轮到:" + nowDisCardSeat + "出牌");
            return;
        }
        if (cardList.size() != 1) {
            LogUtil.errorLog.info("出牌数量不对:" + cardList);
            return;
        }
        if (player.isFristDisCard() && player.getSeat() == lastWinSeat) {
        } else {
            if (firstCard) {
                firstCard = false;
            }
        }
        if (player.getSeat() == lastWinSeat) {
            // 庄家起手出第一张牌不算出过牌
            if (nowDisCardIds != null && !nowDisCardIds.isEmpty()) {
                player.setDisCardForBaoTingHu(true);
            }
        } else {
            player.setDisCardForBaoTingHu(true);
        }

        player.pass(NxGhzDisAction.action_chi, cardList.get(0), action);
        player.pass(NxGhzDisAction.action_peng, cardList.get(0), action);
        addPlayLog(player.getSeat(), action + "", NxGhzTool.implodeGhz(cardList, ","));
        player.disCard(action, cardList);
        setMoFlag(0);
        markMoSeat(player.getSeat(), action);
        clearMoSeatPair();
        setToPlayCardFlag(0); // 应该要打牌的flag
        setDisCardSeat(player.getSeat());
        setNowDisCardIds(cardList);
        setNowDisCardSeat(getNextDisCardSeat());
        for (int seat : seatMap.keySet()) {
            if (seat == player.getSeat())
                continue;
            NxGhzCheckCardBean checkCard = seatMap.get(seat).checkCard(cardList.get(0), false, false);
            List<Integer> list = checkCard.getActionList();
            if (list != null && !list.isEmpty()) {
                addAction(checkCard.getSeat(), list);
            }
        }

        sendActionMsg(player, action, cardList, NxGhzDisAction.action_type_dis);
        player.setNeedCheckTing(true);
        checkAutoMo();
    }

    private void checkAutoMo() {
        if (isTest()) {// 机器人玩家自动摸牌
            checkMo();
        }
    }

    /**
     * 碰
     */
    private void peng(NxGhzPlayer player, List<NxGhzCard> cardList, NxGhzCard nowDisCard, int action) {
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return;
        }
        if (!checkAction(player, cardList, nowDisCard, action)) {
            player.writeComMessage(WebSocketMsgType.res_com_code_yj_guihz_skip);
            return;
        }
        cardList = player.getPengOrWeiList(nowDisCard, cardList);
        if (cardList == null) {
            LogUtil.errorLog.info("不能碰");
            return;
        }
        // 碰的牌放到第一个
        if (!cardList.contains(nowDisCard)) {
            cardList.add(0, nowDisCard);
        } else {
            cardList.remove(nowDisCard);
            cardList.add(0, nowDisCard);
        }

        boolean isSiShou = !player.checkCanDiscard(cardList, action);

        setBeRemoveCard(nowDisCard);
        getDisPlayer().removeOutPais(nowDisCard);
        addPlayLog(player.getSeat(), action + "", NxGhzTool.implodeGhz(cardList, ","));
        player.disCard(action, cardList);
        resetPassChi(player, nowDisCard, action);
        clearAction();
        boolean disCard = setDisPlayer(player, action, false);

        if(isSiShou && NxGhzCheckCardBean.hasLiu(actionSeatMap.get(player.getSeat()))){
            isSiShou = false;
        }
        if(isSiShou){
            removeAction(player.getSeat());
            player.setSiShou(true);
            markMoSeat(player.getSeat(), action);
            setToPlayCardFlag(0);
            setNowDisCardSeat(calcNextSeat(player.getSeat()));
            disCard = false;
        }

        sendActionMsg(player, action, cardList, NxGhzDisAction.action_type_action);
        player.setNeedCheckTing(true);
        if (!disCard) {
            checkMo();
        }
        // 碰的情况,把所有玩家的过牌去掉
        if (isMoFlag()) {
            for (NxGhzPlayer seatPlayer : seatMap.values()) {
                if (seatPlayer.getSeat() == player.getSeat()) {
                    continue;
                }
                seatPlayer.removePassChi(nowDisCard.getVal());
            }
        }
    }

    /**
     * 过
     */
    private void guo(NxGhzPlayer player, List<NxGhzCard> cardList, NxGhzCard nowDisCard, int action) {
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return;
        }
        // 选择过的时候判断  其他玩家有没有优先级更高的操作  有则加入临时操作中  没有则直接过 并且判断有没有临时操作 有则执行优先级高的临时操作
        List<Integer> actionList = actionSeatMap.get(player.getSeat());
        List<Integer> list = NxGhzDisAction.parseToDisActionList(actionList);
        if (player.getHandPais().size() % 3 == 2) {
            boolean isSiShou = false;
            if (player.isSiShouTemp()) {// 可溜选择了过
                if(NxGhzCheckCardBean.hasLiu(actionList)){
                    isSiShou = true;
                }else if(NxGhzCheckCardBean.hasHu(actionList)){
                    isSiShou = true;
                }
            }
            if(isSiShou){
                player.setSiShou(true);
            }else{
                setNowDisCardSeat(player.getSeat());
                setToPlayCardFlag(1);// 设置可出牌
            }
        }

        if (nowDisCard != null) {
            for (int passAction : list) {// 将pass的吃碰值添加到passChi或passPeng中
                player.pass(passAction, nowDisCard, action);
            }
        }
        removeAction(player.getSeat());
        //过溜过歪 再检测其他人可做操作
        if((NxGhzCheckCardBean.hasLiu(actionList) || NxGhzCheckCardBean.hasWei(actionList)) && nowDisCard!= null){
        	addPlayLog(player.getSeat(), action + "", NxGhzTool.implodeGhz(cardList, ","));
        	for (int seat : seatMap.keySet()) {
        		if(seat == player.getSeat()){
        			continue;
        		}
                NxGhzCheckCardBean checkCard = seatMap.get(seat).checkCard(nowDisCard, false, false);
                List<Integer> alist = checkCard.getActionList();
                if (alist != null && !alist.isEmpty()) {
                    addAction(checkCard.getSeat(), alist);
                }
            }
        	
          sendActionMsg(player, NxGhzDisAction.action_mo, new ArrayList<>(Arrays.asList(nowDisCard)), NxGhzDisAction.action_type_mo,false,true);
          if (this.leftCards != null && this.leftCards.size() == 0 && !isHasSpecialAction()) {
              calcOver();
              return;
          }
          if (NxGhzConstant.isAutoMo) {
              checkMo();
          } else {
              if (isTest()) {
                  checkMo();
              }
          }
        }else{
        	markMoSeat(player.getSeat(), action);
            cardList.clear();
            addPlayLog(player.getSeat(), action + "", NxGhzTool.implodeGhz(cardList, ","));
            sendActionMsg(player, action, cardList, NxGhzDisAction.action_type_action);
            refreshTempAction(player);// 先过 后执行临时可做操作里面优先级最高的玩家操作
            if (NxGhzConstant.isAutoMo) {
                checkMo();
            } else {
                if (isTest()) {
                    checkMo();
                }
            }
        }
        
        
        
    }

    /**
     * 吃
     */
    private void chi(NxGhzPlayer player, List<NxGhzCard> cardList, NxGhzCard nowDisCard, int action) {
        List<Integer> actionList = actionSeatMap.get(player.getSeat());
        if (actionList == null) {
            return;
        }
        if (cardList != null) {
            List<NxGhzCard> chiCards = NxGhzTool.checkChi(cardList, nowDisCard);
            if (cardList.size() % 3 != 0 || chiCards.isEmpty()) {
                LogUtil.errorLog.info("不能吃" + cardList);
                return;
            }
            if (!cardList.contains(nowDisCard)) {
                return;
            }
        }
        cardList = player.getChiList(nowDisCard, cardList);
        if (cardList == null) {
            LogUtil.errorLog.info("不能吃");
            return;
        }
        if (player.getHandPais().size() <= cardList.size()) {
            LogUtil.errorLog.info("您手上没有剩余的牌可打，不能吃");
            return;
        }
        if (cardList.size() > 3) {
            LogUtil.errorLog.info("不能吃：" + cardList);
            return;
        }
        if (NxGhzTool.isGuihuziRepeat(cardList)) {
            LogUtil.errorLog.info("不能吃");
            return;
        }
        List<Integer> cardPais = NxGhzTool.toGhzCardVals(cardList, false);
        if (player.getChiSizeExcept2710() >= 2 && !NxGhzTool.c2710List.containsAll(cardPais)) {
            LogUtil.errorLog.info("已经吃了两组非息牌，不能再吃了");
            return;
        }
        List<NxGhzCard> copyChis = new ArrayList<>(cardList);
        copyChis.remove(nowDisCard);
        NxGhzMenzi menzi = new NxGhzMenzi(NxGhzTool.toGhzCardVals(copyChis, true), 0);
        if (player.getPassMenzi().contains(menzi)) {
            LogUtil.errorLog.info("臭了的门子不能再吃牌：" + copyChis);
            return;
        }
        player.pass(NxGhzDisAction.action_peng, nowDisCard, action);// 吃过的牌不能再碰  臭碰
        if (!checkAction(player, cardList, nowDisCard, action)) {
            player.writeComMessage(WebSocketMsgType.res_com_code_yj_guihz_skip);
            return;
        }
        // 吃的牌放到第一个
        if (!cardList.contains(nowDisCard)) {
            cardList.add(0, nowDisCard);
        } else {
            cardList.remove(nowDisCard);
            cardList.add(0, nowDisCard);
        }

        boolean isSiShou = !player.checkCanDiscard(cardList, action);

        setBeRemoveCard(nowDisCard);
        getDisPlayer().removeOutPais(nowDisCard);
        addPlayLog(player.getSeat(), action + "", NxGhzTool.implodeGhz(cardList, ","));
        player.disCard(action, cardList);
        resetPassChi(player, nowDisCard, action);
        clearAction();
        boolean disCard = setDisPlayer(player, action, false);

        if(isSiShou && NxGhzCheckCardBean.hasLiu(actionSeatMap.get(player.getSeat()))){
            isSiShou = false;
        }
        if(isSiShou){
            removeAction(player.getSeat());
            player.setSiShou(true);
            markMoSeat(player.getSeat(), action);
            setToPlayCardFlag(0);
            setNowDisCardSeat(calcNextSeat(player.getSeat()));
            disCard = false;
        }

        sendActionMsg(player, action, cardList, NxGhzDisAction.action_type_action);
        player.setNeedCheckTing(true);
        if (!disCard) {
            checkMo();
        }
    }

    private NxGhzCheckCardBean checkLiu(NxGhzPlayer player) {
        NxGhzCheckCardBean checkBean = player.checkLiu();
        if (player.getHandPais().size() % 3 == 2 && checkBean.isLiu()) {// 推送给自己可溜

        } else {
            checkBean.setLiu(false);
        }
        return checkBean;
    }

    public synchronized void play(NxGhzPlayer player, List<Integer> cardIds, int action, boolean moPai, boolean isHu, boolean isPassHu) {
        if (state != table_state.play) {// 检查play状态
            return;
        }
        NxGhzCard nowDisCard = null;
        List<NxGhzCard> cardList = new ArrayList<>();
        // 非摸牌非过牌要检查能否出牌,并将要出的牌id集合变成鬼胡子牌
        if (action != NxGhzDisAction.action_mo) {
            if (nowDisCardIds != null && nowDisCardIds.size() == 1) {
                nowDisCard = nowDisCardIds.get(0);
            }
            if (action != NxGhzDisAction.action_pass) {
                if (!player.isCanDisCard(cardIds, nowDisCard)) {
                    return;
                }
            }
            if (cardIds != null && !cardIds.isEmpty()) {
                cardList = NxGhzTool.toGhzCards(cardIds);
            }
        }

        if (action != NxGhzDisAction.action_mo) {
            StringBuilder sb = new StringBuilder("NxGhz");
            sb.append("|").append(getId());
            sb.append("|").append(getPlayBureau());
            sb.append("|").append(player.getUserId());
            sb.append("|").append(player.getSeat());
            sb.append("|").append(player.isAutoPlay() ? 1 : 0);
            sb.append("|").append(NxGhzDisAction.getActionName(action));
            sb.append("|").append(cardList);
            sb.append("|").append(nowDisCard);
            if (actionSeatMap.containsKey(player.getSeat())) {
                sb.append("|").append(NxGhzCheckCardBean.actionListToString(actionSeatMap.get(player.getSeat())));
            }
            LogUtil.msgLog.info(sb.toString());
        }

        if (action == NxGhzDisAction.action_piao) {
            piao(player, cardList, nowDisCard, action);
        } else if (action == NxGhzDisAction.action_wei) {
            wei(player, cardList, nowDisCard, action);
        } else if (action == NxGhzDisAction.action_liu) {
            liu(player, cardList, nowDisCard, action);
        } else if (action == NxGhzDisAction.action_hu || action == NxGhzDisAction.action_jiuduiban_hu) {
            hu(player, cardList, action, nowDisCard);
        } else if (action == NxGhzDisAction.action_peng) {
            peng(player, cardList, nowDisCard, action);
        } else if (action == NxGhzDisAction.action_chi) {
            chi(player, cardList, nowDisCard, action);
        } else if (action == NxGhzDisAction.action_pass) {
            guo(player, cardList, nowDisCard, action);
        } else if (action == NxGhzDisAction.action_mo) {
            if (isTest()) {
                return;
            }
            if (checkMoMark != null) {
                int cAction = cardIds.get(0);
                if (checkMoMark.getId() == player.getSeat() && checkMoMark.getValue() == cAction) {
                    checkMo();
                }
            }
        } else {// 0直接出牌
            chuPai(player, cardList, action);
        }
        if (!moPai && !isHu) {// 摸牌的时候提不需要做操作
            robotDealAction();
        }
        sendTingInfo(player);
    }

    /**
     * 设置要出牌的玩家
     */
    private boolean setDisPlayer(NxGhzPlayer player, int action, boolean isHu) {
        boolean canDisCard = true;
        if (player.getHandGhzs().isEmpty()) {
            canDisCard = false;
        }
        if (canDisCard && player.isNeedDisCard(action)) {
            setNowDisCardSeat(player.getSeat());
            NxGhzCheckCardBean checkBean = checkLiu(player);
            if (action == NxGhzDisAction.action_wei) {
                NxGhzCheckCardBean huBean = checkHuAfterWei(player);
                if (huBean.isHu()) {
                    checkBean.setHu(true);
                }
            }
            if (checkBean.isLiu() || checkBean.isHu()) {
                checkBean.buildActionList();
                List<Integer> actionList = checkBean.getActionList();
                addAction(player.getSeat(), actionList);
//                sendPlayerActionMsg(player, action);
                return false;
            }
            if (this.leftCards.isEmpty()) {// 桌面已经没有牌了
                if (!isHu) {
                    calcOver();
                }
                return false;
            }
            setToPlayCardFlag(1);// 设置可出牌
            return true;
        } else {// 不需要出牌 下一家直接摸牌
            if (this.leftCards.isEmpty()) {// 桌面已经没有牌了
                if (!isHu) {
                    calcOver();
                }
                return false;
            }
            setToPlayCardFlag(0);
            player.compensateCard();
            int next = calcNextSeat(player.getSeat());
            setNowDisCardSeat(next);
            if (actionSeatMap.isEmpty()) {
                markMoSeat(player.getSeat(), action);
            }
            return false;
        }
    }

    /**
     * 检查是否能操作
     *
     * @param player
     * @param cardList
     * @param nowDisCard
     * @param action
     * @return
     */
    private boolean checkCanAction(NxGhzPlayer player, List<NxGhzCard> cardList, NxGhzCard nowDisCard, int action) {
        List<Integer> stopActionList = NxGhzDisAction.findPriorityAction(action);
        boolean canAction = true;
        for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
            if (player.getSeat() != entry.getKey()) {// 别人
                boolean can = NxGhzDisAction.canDis(stopActionList, entry.getValue());
                if (!can) {
                    canAction = false;
                    break;
                }
                List<Integer> disActionList = NxGhzDisAction.parseToDisActionList(entry.getValue());
                if (disActionList.contains(action)) {
                    // 同时拥有同一个事件 根据座位号来判断
                    int actionSeat = entry.getKey();
                    int nearSeat = getNearSeat(disCardSeat, Arrays.asList(player.getSeat(), actionSeat));
                    if (nearSeat != player.getSeat()) {
                        canAction = false;
                        break;
                    }
                }
            }
        }
        return canAction;
    }

    /**
     * 检查优先度，溜漂偎胡碰吃 如果同时出现一个事件，按出牌座位顺序优先
     */
    private boolean checkAction(NxGhzPlayer player, List<NxGhzCard> cardList, NxGhzCard nowDisCard, int action) {
        boolean canAction = checkCanAction(player, cardList, nowDisCard, action);
        updateTempAction(canAction, player, cardList, nowDisCard, action);
        return canAction;
    }

    private void updateTempAction(boolean canAction, NxGhzPlayer player, List<NxGhzCard> cardList, NxGhzCard nowDisCard, int action) {
        if (canAction == false) {// 不能操作时  存入临时操作
            int seat = player.getSeat();
            if (action == NxGhzDisAction.action_jiuduiban_hu) {// 九对半转化成0action
                action = 0;
            }
            NxGhzTempAction tempAction = new NxGhzTempAction(seat, action, cardList, nowDisCard);
            tempActionMap.put(seat, tempAction);
            if (tempActionMap.size() == actionSeatMap.size()) {// 全部进入临时actionMap时
                int maxAction = Integer.MAX_VALUE;
                int maxSeat = 0;
                Map<Integer, Integer> prioritySeats = new HashMap<>();
                int maxActionSize = 0;
                for (NxGhzTempAction temp : tempActionMap.values()) {
                    if (temp.getAction() < maxAction) {
                        maxAction = temp.getAction();
                        maxSeat = temp.getSeat();
                    }
                    prioritySeats.put(temp.getSeat(), temp.getAction());
                }
                Set<Integer> maxPrioritySeats = new HashSet<>();
                for (int mActionSet : prioritySeats.keySet()) {
                    if (prioritySeats.get(mActionSet) == maxAction) {
                        maxActionSize++;
                        maxPrioritySeats.add(mActionSet);
                    }
                }
                if (maxActionSize > 1) {
                    maxSeat = getNearSeat(disCardSeat, new ArrayList<>(maxPrioritySeats));
                    maxAction = prioritySeats.get(maxSeat);
                }
                NxGhzPlayer tempPlayer = seatMap.get(maxSeat);
                List<NxGhzCard> tempCardList = tempActionMap.get(maxSeat).getCardList();
                NxGhzCard tempNowDisCard = tempActionMap.get(maxSeat).getNowDisCard();
                for (int removeSeat : prioritySeats.keySet()) {
                    if (removeSeat != maxSeat) {
                        removeAction(removeSeat);
                    }
                }
                tempActionMap.clear();
                if (maxAction == 0) {
                    maxAction = NxGhzDisAction.action_jiuduiban_hu;
                }
                if (maxAction == NxGhzDisAction.action_piao) {
                    piao(tempPlayer, tempCardList, tempNowDisCard, maxAction);
                } else if (maxAction == NxGhzDisAction.action_wei) {
                    wei(tempPlayer, tempCardList, tempNowDisCard, maxAction);
                } else if (maxAction == NxGhzDisAction.action_liu) {
                    liu(tempPlayer, tempCardList, tempNowDisCard, maxAction);
                } else if (maxAction == NxGhzDisAction.action_hu || maxAction == NxGhzDisAction.action_jiuduiban_hu) {// 九对半天胡
                    hu(tempPlayer, tempCardList, maxAction, tempNowDisCard);
                } else if (maxAction == NxGhzDisAction.action_peng) {
                    peng(tempPlayer, tempCardList, tempNowDisCard, maxAction);
                } else if (maxAction == NxGhzDisAction.action_chi) {
                    chi(tempPlayer, tempCardList, tempNowDisCard, maxAction);
                } else if (maxAction == NxGhzDisAction.action_pass) {
                    guo(tempPlayer, tempCardList, tempNowDisCard, maxAction);
                }
            }
        } else {// 能操作 清理所有临时操作
            tempActionMap.clear();
        }
        changeExtend();
    }

    private void clearTempAction() {
        tempActionMap.clear();
        changeExtend();
    }

    /**
     * 执行可做操作里面优先级最高的玩家操作
     *
     * @param player
     */
    private void refreshTempAction(NxGhzPlayer player) {
        tempActionMap.remove(player.getSeat());
        Map<Integer, Integer> prioritySeats = new HashMap<>();
        for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
            int seat = entry.getKey();
            List<Integer> actionList = entry.getValue();
            List<Integer> list = NxGhzDisAction.parseToDisActionList(actionList);
            int priorityAction = NxGhzDisAction.getMaxPriorityAction(list);
            prioritySeats.put(seat, priorityAction);
        }
        int maxPriorityAction = Integer.MAX_VALUE;
        int maxPrioritySeat = 0;
        boolean isSame = true;
        for (int seat : prioritySeats.keySet()) {
            if (maxPrioritySeat != Integer.MAX_VALUE && maxPrioritySeat != prioritySeats.get(seat)) {
                isSame = false;
            }
            if (prioritySeats.get(seat) < maxPriorityAction) {
                maxPriorityAction = prioritySeats.get(seat);
                maxPrioritySeat = seat;
            }
        }
        if (isSame) {
            maxPrioritySeat = getNearSeat(disCardSeat, new ArrayList<>(prioritySeats.keySet()));
        }
        Iterator<NxGhzTempAction> iterator = tempActionMap.values().iterator();
        while (iterator.hasNext()) {
            NxGhzTempAction tempAction = iterator.next();
            if (tempAction.getSeat() == maxPrioritySeat) {
                int action = tempAction.getAction();
                List<NxGhzCard> cardList = tempAction.getCardList();
                NxGhzCard nowDisCard = tempAction.getNowDisCard();
                iterator.remove();
                NxGhzPlayer tempPlayer = seatMap.get(tempAction.getSeat());
                if (action == 0) {
                    action = NxGhzDisAction.action_jiuduiban_hu;
                }
                if (action == NxGhzDisAction.action_piao) {
                    piao(tempPlayer, cardList, nowDisCard, action);
                } else if (action == NxGhzDisAction.action_wei) {
                    wei(tempPlayer, cardList, nowDisCard, action);
                } else if (action == NxGhzDisAction.action_liu) {
                    liu(tempPlayer, cardList, nowDisCard, action);
                } else if (action == NxGhzDisAction.action_hu || action == NxGhzDisAction.action_jiuduiban_hu) {
                    hu(tempPlayer, cardList, action, nowDisCard);
                } else if (action == NxGhzDisAction.action_peng) {
                    peng(tempPlayer, cardList, nowDisCard, action);
                } else if (action == NxGhzDisAction.action_chi) {
                    chi(tempPlayer, cardList, nowDisCard, action);
                } else if (action == NxGhzDisAction.action_pass) {
                    guo(tempPlayer, cardList, nowDisCard, action);
                }
                break;
            }
        }
        changeExtend();
    }

    /**
     * 获得出牌位置的玩家
     */
    private NxGhzPlayer getDisPlayer() {
        return seatMap.get(disCardSeat);
    }

    @Override
    public int isCanPlay() {
        if (getPlayerCount() < getMaxPlayerCount()) {
            return 1;
        }
        // for (SyPaohuziPlayer player : seatMap.values()) {
        // if (player.getIsEntryTable() != PdkConstants.table_online) {
        // // 通知其他人离线
        // broadIsOnlineMsg(player, player.getIsEntryTable());
        // return 2;
        // }
        // }
        return 0;
    }

    /**
     * 摸牌
     */
    private synchronized void checkMo() {
        // 0溜 1漂 2偎 3胡 4 碰 5吃
        if (!actionSeatMap.isEmpty()) {
            return;
        }
        if (nowDisCardSeat == 0) {
            return;
        }
        // 下一个要摸牌的人
        NxGhzPlayer player = seatMap.get(nowDisCardSeat);// 出牌人的位置即为摸牌人的位置
        if (player == null) {
            return;
        }
        if (!tempActionMap.isEmpty()) {
            LogUtil.e(player.getName() + "摸牌清理临时操作！");
            return;
        }
        if (toPlayCardFlag == 1) {// 接下来应该打牌
            return;
        }
        if (leftCards == null) {
            return;
        }
        if (this.leftCards.size() == 0 && !isHasSpecialAction()) {// 没有特殊操作 直接进行结算
            calcOver();
            return;
        }
        clearMarkMoSeat();
        boolean isZp = false;
        NxGhzCard card = null;
        if (GameServerConfig.isDeveloper() && !player.isRobot()) {
            if (zpMap.containsKey(player.getUserId()) && zpMap.get(player.getUserId()) > 0) {
                card = getNextCard(zpMap.get(player.getUserId()));
                if (card != null) {
                    zpMap.remove(player.getUserId());
                    isZp = true;
                } else {
                    card = getNextCard();
                }
            }
        }
        if (isZp == false) {
            card = getNextCard();
        }
        addPlayLog(player.getSeat(), NxGhzDisAction.action_mo + "", (card == null ? 0 : card.getId()) + "");

        StringBuilder sb = new StringBuilder("NxGhz");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append("moPai");
        sb.append("|").append(card);
        LogUtil.msgLog.info(sb.toString());

        if (card != null) {
            clearPassChiTemp();
            if (!player.isAutoPlay()) {
                player.setLastCheckTime(0);
            }
            setMoFlag(1);
            setMoSeat(player.getSeat());
            markMoSeat(card, player.getSeat());
            player.moCard(card);
            setDisCardSeat(player.getSeat());
            setNowDisCardIds(new ArrayList<>(Arrays.asList(card)));
            setNowDisCardSeat(getNextDisCardSeat());
            
            //先检测摸牌的人是不是可以歪跟溜
            boolean iswailiu = false;
            NxGhzCheckCardBean mocheckCard = player.checkCard(card, true, false);
            List<Integer> molist = mocheckCard.getActionList();
            if (molist != null && !molist.isEmpty()) {
                addAction(mocheckCard.getSeat(), molist);
                if(molist.get(0) == 1 || molist.get(2) == 1){
                	iswailiu = true;
                }
            }
            
            if(!iswailiu){
            	for (int seat : seatMap.keySet()) {
            		if(seat == player.getSeat()){
            			continue;
            		}
                    NxGhzCheckCardBean checkCard = seatMap.get(seat).checkCard(card, false, false);
                    List<Integer> list = checkCard.getActionList();
                    if (list != null && !list.isEmpty()) {
                        addAction(checkCard.getSeat(), list);
                    }
                }
            }
            
            markMoSeat(player.getSeat(), NxGhzDisAction.action_mo);
//            if(iswailiu){
//            	for (int seat : seatMap.keySet()) {
//            		if(seat == player.getSeat()){
//            			sendActionMsg(player, NxGhzDisAction.action_mo, new ArrayList<>(Arrays.asList(card)), NxGhzDisAction.action_type_mo);
//            		}else{
//            			sendActionMsg(player, NxGhzDisAction.action_mo, new ArrayList<>(Arrays.asList(NxGhzCard.getPaohzCard(0))), NxGhzDisAction.action_type_mo);
//            		}
//                }
//            }else{
            	sendActionMsg(player, NxGhzDisAction.action_mo, new ArrayList<>(Arrays.asList(card)), NxGhzDisAction.action_type_mo,iswailiu);
//            }
            if (this.leftCards != null && this.leftCards.size() == 0 && !isHasSpecialAction()) {
                calcOver();
                return;
            }
            checkAutoMo();
        }
        sendTingInfo(player);
    }

    /**
     * 除了吃和碰之外的都是特殊动作
     */
    private boolean isHasSpecialAction() {
        for (List<Integer> actionList : actionSeatMap.values()) {
            if (actionList.get(0) == 1 || actionList.get(1) == 1 || actionList.get(2) == 1 || actionList.get(3) == 1) {// 除了吃和碰之外的都是特殊动作
                return true;
            }
        }
        return false;
    }

    private void addAction(int seat, List<Integer> actionList) {
        actionSeatMap.put(seat, actionList);
        actionSeatMapCopy.put(seat, actionList);
        addPlayLog(seat, NxGhzDisAction.action_hasaction + "", StringUtil.implode(actionList));
        saveActionSeatMap();
    }

    public void appendActionSeat(int seat, List<Integer> actionlist) {
        if (actionSeatMap.containsKey(seat)) {
            List<Integer> a = actionSeatMap.get(seat);
            DataMapUtil.appendList(a, actionlist);
            addPlayLog(seat, NxGhzDisAction.action_hasaction + "", StringUtil.implode(a));
        } else {
            actionSeatMap.put(seat, actionlist);
            actionSeatMapCopy.put(seat, actionlist);
            addPlayLog(seat, NxGhzDisAction.action_hasaction + "", StringUtil.implode(actionlist));
        }
        saveActionSeatMap();
    }

    private List<Integer> removeAction(int seat) {
        List<Integer> list = actionSeatMap.remove(seat);
        saveActionSeatMap();
        return list;
    }

    /**
     * 玩家Wei操作后，清除玩家操作
     * 需要保留胡和溜
     */
    private void clearActionForWei(NxGhzPlayer player) {
        List<Integer> actionList = actionSeatMap.get(player.getSeat());
        actionSeatMap.clear();
        actionSeatMapCopy.clear();
        if (actionList != null && actionList.size() > 0) {
            for (int i = 0; i < actionList.size(); i++) {
                if (i != 0 && i != 3) {
                    actionList.set(i, 0);
                }
            }
            if (actionList.contains(1)) {
                actionSeatMap.put(player.getSeat(), actionList);
                actionSeatMapCopy.put(player.getSeat(), actionList);
            }
        }
        saveActionSeatMap();
    }

    /**
     * 清除玩家所有操作
     */
    private void clearAction() {
        actionSeatMap.clear();
        actionSeatMapCopy.clear();
        saveActionSeatMap();
    }

    private void clearHuList() {
        huConfirmList.clear();
        changeExtend();
    }

    public void saveActionSeatMap() {
        dbParamMap.put("nowAction", JSON_TAG);
    }

    /**
     * 发送所有玩家动作msg
     *
     * @param player
     * @param action
     * @param cards
     * @param actType
     */
    public void sendMoMsg(NxGhzPlayer player, int action, List<NxGhzCard> cards, int actType) {
        PlayPaohuziRes.Builder builder = PlayPaohuziRes.newBuilder();
        builder.setAction(action);
        builder.setUserId(player.getUserId() + "");
        builder.setSeat(player.getSeat());
        // builder.setNextSeat(nowDisCardSeat);
        setNextSeatMsg(builder);
        builder.setRemain(leftCards.size());
        builder.addAllPhzIds(NxGhzTool.toGhzCardIds(cards));
        builder.setActType(actType);
        sendMoMsgBySelfAction(builder, player.getSeat());
    }

    /**
     * 发送该玩家动作msg
     */
    private void sendPlayerActionMsg(NxGhzPlayer player, int action) {
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return;
        }
        PlayPaohuziRes.Builder builder = PlayPaohuziRes.newBuilder();
        builder.setAction(action);
        builder.setUserId(player.getUserId() + "");
        builder.setSeat(player.getSeat());
        setNextSeatMsg(builder);
        if (leftCards != null) {
            builder.setRemain(leftCards.size());
        }
        builder.setActType(0);
        List<Integer> actionList = actionSeatMap.get(player.getSeat());
        if (actionList != null) {
            builder.addAllSelfAct(actionList);
        }
        player.writeSocket(builder.build());
    }

    private void setNextSeatMsg(PlayPaohuziRes.Builder builder) {
        // if (!GameServerConfig.isDebug()) {
        // builder.setNextSeat(nowDisCardSeat);
        //
        // } else {
        builder.setTimeSeat(nowDisCardSeat);
        if (toPlayCardFlag == 1) {
            builder.setNextSeat(nowDisCardSeat);
        } else {
            builder.setNextSeat(0);
        }
        // }
    }

    /**
     * 发送动作msg
     *
     * @param player
     * @param action
     * @param cards
     * @param actType
     */
    private void sendActionMsg(NxGhzPlayer player, int action, List<NxGhzCard> cards, int actType,boolean... iswailiu) {
        PlayPaohuziRes.Builder builder = PlayPaohuziRes.newBuilder();
        builder.setAction(action);
        builder.setUserId(player.getUserId() + "");
        builder.setSeat(player.getSeat());
        setNextSeatMsg(builder);
        if (leftCards != null) {
            builder.setRemain(leftCards.size());
        }
        builder.addAllPhzIds(NxGhzTool.toGhzCardIds(cards));
        builder.setActType(actType);
        sendMsgBySelfAction(builder,iswailiu);
    }

    /**
     * 发送听牌动作msg
     * @param player
     * @param action
     * @param cards
     * @param actType
     */
//	private void sendTingActionMsg(NxGhzPlayer player, int action, List<Integer> cards, int actType) {
//		PlayPaohuziRes.Builder builder = PlayPaohuziRes.newBuilder();
//		builder.setAction(action);
//		builder.setUserId(player.getUserId() + "");
//		builder.setSeat(player.getSeat());
//		if (leftCards != null) {
//			builder.setRemain(leftCards.size());
//		}
//		builder.addAllPhzIds(cards);
//		builder.setActType(actType);
//		player.writeSocket(builder.build());
//	}

    /**
     * 发送消息带入自己动作
     *
     * @param builder
     */
    private void sendMoMsgBySelfAction(PlayPaohuziRes.Builder builder, int seat) {
        for (NxGhzPlayer player : seatMap.values()) {
            PlayPaohuziRes.Builder copy = builder.clone();
            if (player.getSeat() != seat) {
                // copy.clearPhzIds();
                // copy.addPhzIds(0);
            } else {
//				copy.setHuxi(player.getOutHuxi());
            }
            if (actionSeatMap.containsKey(player.getSeat())) {
                List<Integer> actionList = actionSeatMap.get(player.getSeat());
                if (actionList != null) {
                    copy.addAllSelfAct(actionList);
                }
            }
            player.writeSocket(copy.build());
        }
    }

    /**
     * 发送消息带入自己动作
     *
     * @param builder
     */
    private void sendMsgBySelfAction(PlayPaohuziRes.Builder builder,boolean... iswailiu) {
    	boolean wailiu = false;
    	boolean noMy = false;//第2次推送摸牌的时候 不发送自己的数据
    	if(iswailiu != null && iswailiu.length>0){
    		wailiu = iswailiu[0];
    		if(iswailiu.length > 1){
    			noMy = iswailiu[1];
    		}
    	}
    	if(builder.getActType() == NxGhzDisAction.action_type_mo && noMy){//自己先过
    		PlayPaohuziRes.Builder mycopy = builder.clone();
    		for (NxGhzPlayer player : seatMap.values()) {
    			if(mycopy.getSeat() == player.getSeat()){
    				mycopy.clearPhzIds();
    	    		mycopy.setAction(NxGhzDisAction.action_pass);
    	    		mycopy.setActType(NxGhzDisAction.action_type_action);
    	    		
    	    		player.writeSocket(mycopy.build());

    	            if (mycopy.getSelfActList() != null && mycopy.getSelfActList().size() > 0) {
    	                StringBuilder sb = new StringBuilder("NxGhz");
    	                sb.append("|").append(getId());
    	                sb.append("|").append(getPlayBureau());
    	                sb.append("|").append(player.getUserId());
    	                sb.append("|").append(player.getSeat());
    	                sb.append("|").append(player.isAutoPlay() ? 1 : 0);
    	                sb.append("|").append("actList");
    	                sb.append("|").append(NxGhzCheckCardBean.actionListToString(actionSeatMap.get(player.getSeat())));
    	                LogUtil.msgLog.info(sb.toString());
    	            }
    			}
    		}
    	}
    	
    	for (NxGhzPlayer player : seatMap.values()) {
            PlayPaohuziRes.Builder copy = builder.clone();
            
            if(builder.getActType() == NxGhzDisAction.action_type_mo && noMy && copy.getSeat() == player.getSeat()){
            	continue;
    		}
            
            if (actionSeatMap.containsKey(player.getSeat()) && !tempActionMap.containsKey(player.getSeat())) {
                List<Integer> actionList = actionSeatMap.get(player.getSeat());
                if (actionList != null) {
                    copy.addAllSelfAct(actionList);
                }
            }
            if (copy.getSeat() != player.getSeat() && wailiu) {
            	List<Integer> ids = copy.getPhzIdsList().stream().map(v -> 0).collect(Collectors.toList());
            	if (!CollectionUtils.isEmpty(ids)) {
                    copy.clearPhzIds();
                    copy.addAllPhzIds(ids);
                }
            }
            
            player.writeSocket(copy.build());

            if (copy.getSelfActList() != null && copy.getSelfActList().size() > 0) {
                StringBuilder sb = new StringBuilder("NxGhz");
                sb.append("|").append(getId());
                sb.append("|").append(getPlayBureau());
                sb.append("|").append(player.getUserId());
                sb.append("|").append(player.getSeat());
                sb.append("|").append(player.isAutoPlay() ? 1 : 0);
                sb.append("|").append("actList");
                sb.append("|").append(NxGhzCheckCardBean.actionListToString(actionSeatMap.get(player.getSeat())));
                LogUtil.msgLog.info(sb.toString());
            }
        }
    }

    /**
     * 推送给有动作的人消息
     */
    private void checkSendActionMsg() {
        if (actionSeatMap.isEmpty()) {
            return;
        }
        PlayPaohuziRes.Builder disBuilder = PlayPaohuziRes.newBuilder();
        NxGhzPlayer player = seatMap.get(disCardSeat);
        NxGhzResTool.buildPlayRes(disBuilder, player, 0, null);
        disBuilder.setRemain(leftCards.size());
        setNextSeatMsg(disBuilder);
        for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
            PlayPaohuziRes.Builder copy = disBuilder.clone();
            List<Integer> actionList = entry.getValue();
            copy.addAllSelfAct(actionList);
            NxGhzPlayer seatPlayer = seatMap.get(entry.getKey());
            seatPlayer.writeSocket(copy.build());
        }
    }

    public boolean checkJiuDuiBanHu(NxGhzPlayer player,NxGhzCard nowDisCard) {
        List<NxGhzCard> handCardsCopy = new ArrayList<>(player.getHandGhzs());// 手牌
        if(nowDisCard != null){
        	handCardsCopy.add(nowDisCard);
        }
        NxGhzCardIndexArr arr = NxGhzTool.getGuihzCardIndexArr(handCardsCopy);
        if (arr.getDuiziNum() == 10) {// 九对半  并且对子数为9
            return true;
        }
        return false;
    }

    /**
     * 起手九对半胡 天胡检测
     */
    public void checkAction() {
        int nowSeat = getNowDisCardSeat();
        // 先判断拿牌的玩家
        NxGhzPlayer nowPlayer = seatMap.get(nowSeat);
        if (nowPlayer == null) {
            return;
        }
//        for (int seat : seatMap.keySet()) {// 九对半直接胡
//            NxGhzPlayer seatPlayer = seatMap.get(seat);
//            if (checkJiuDuiBanHu(seatPlayer)) {
//                int[] actionArr = new int[7];
//                actionArr[6] = 1;
//                List<Integer> list = new ArrayList<>();
//                for (int val : actionArr) {
//                    list.add(val);
//                }
//                addAction(seat, list);
//            }
////			if(seatPlayer.getHandGhzs().size() == 19){
////				boolean isTing = seatPlayer.isTing(this, seatPlayer);
////				if(isTing){
////					List<Integer> cards = seatPlayer.getTingCards();
////					putTingMap(seatPlayer.getSeat(), cards);
////					sendTingActionMsg(seatPlayer, 8, seatPlayer.getTingCards(), NxGhzDisAction.action_type_ting);
////				}
////			}
//        }
        NxGhzCheckCardBean checkCard = nowPlayer.checkCard(null, true, true, false);// 庄家天胡判断
        List<Integer> list = checkCard.getActionList();
        if (list != null && !list.isEmpty()) {
            appendActionSeat(nowSeat, list);
        }
        checkSendActionMsg();
    }

    private void sleep() {
        try {
            Thread.sleep(600);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void robotDealAction() {
        if (isTest()) {
            if (leftCards.size() == 0 && !isHasSpecialAction()) {
                calcOver();
                return;
            }
            if (actionSeatMap.isEmpty()) {
                int nextseat = getNowDisCardSeat();
                NxGhzPlayer player = seatMap.get(nextseat);
                if (player != null && player.isRobot()) {
                    // 普通出牌
                    int card = NxGhzRobotAI.getInstance().outPaiHandle(0, NxGhzTool.toGhzCardIds(player.getHandGhzs()), new ArrayList<Integer>());
                    if (card == 0) {
                        return;
                    }
                    sleep();
                    List<Integer> cardList = new ArrayList<>(Arrays.asList(card));
                    play(player, cardList, 0);
                }
            } else {
                Iterator<Integer> iterator = actionSeatMap.keySet().iterator();
                while (iterator.hasNext()) {
                    Integer key = iterator.next();
                    List<Integer> value = actionSeatMap.get(key);
                    NxGhzPlayer player = seatMap.get(key);
                    if (player == null || !player.isRobot()) {
                        continue;
                    }
                    List<Integer> actions = NxGhzDisAction.parseToDisActionList(value);
                    for (int action : actions) {
                        if (!checkAction(player, null, nowDisCardIds.get(0), action)) {
                            player.writeComMessage(WebSocketMsgType.res_com_code_yj_guihz_skip);
                            continue;
                        }
                        sleep();
                        if (action == NxGhzDisAction.action_hu) {
                            broadMsg(player.getName() + "胡牌");
                            play(player, null, action);
                        } else if (action == NxGhzDisAction.action_peng) {
                            play(player, null, action);
                        } else if (action == NxGhzDisAction.action_chi) {
                            play(player, null, action);
                        } else if (action == NxGhzDisAction.action_liu) {
                            // play(player, null, action);
                        } else if (action == NxGhzDisAction.action_wei) {
                            // play(player,
                            // PaohuziTool.toPhzCardIds(nowDisCardIds), action);
                        }
                        break;

                    }
                }
            }

        }
    }

    @Override
    public int getPlayerCount() {
        return seatMap.size();
    }

    @Override
    protected void initNext1() {
        setBeRemoveCard(null);
        clearMarkMoSeat();
        clearMoSeatPair();
        clearHuList();
        setLeftCards(null);
        setStartLeftCards(null);
        setMoFlag(0);
        setMoSeat(0);
        clearAction();
        clearTempAction();
        setNowDisCardSeat(0);
        setNowDisCardIds(null);
        setFirstCard(true);
        timeNum = 0;
    }

    @Override
    public Map<String, Object> saveDB(boolean asyn) {
        if (id < 0) {
            return null;
        }

        Map<String, Object> tempMap = loadCurrentDbMap();
        if (!tempMap.isEmpty()) {
            tempMap.put("tableId", id);
            tempMap.put("roomId", roomId);
            if (tempMap.containsKey("players")) {
                tempMap.put("players", buildPlayersInfo());
            }
            if (tempMap.containsKey("outPai1")) {
                tempMap.put("outPai1", seatMap.get(1).buildOutPaiStr());
            }
            if (tempMap.containsKey("outPai2")) {
                tempMap.put("outPai2", seatMap.get(2).buildOutPaiStr());
            }
            if (tempMap.containsKey("outPai3")) {
                tempMap.put("outPai3", seatMap.get(3).buildOutPaiStr());
            }
            if (tempMap.containsKey("outPai4")) {
                tempMap.put("outPai4", seatMap.get(4).buildOutPaiStr());
            }
            if (tempMap.containsKey("handPai1")) {
                tempMap.put("handPai1", seatMap.get(1).buildHandPaiStr());
            }
            if (tempMap.containsKey("handPai2")) {
                tempMap.put("handPai2", seatMap.get(2).buildHandPaiStr());
            }
            if (tempMap.containsKey("handPai3")) {
                tempMap.put("handPai3", seatMap.get(3).buildHandPaiStr());
            }
            if (tempMap.containsKey("handPai4")) {
                tempMap.put("handPai4", seatMap.get(4).buildHandPaiStr());
            }
            if (tempMap.containsKey("answerDiss")) {
                tempMap.put("answerDiss", buildDissInfo());
            }
            if (tempMap.containsKey("nowDisCardIds")) {
                tempMap.put("nowDisCardIds", StringUtil.implode(NxGhzTool.toGhzCardIds(nowDisCardIds), ","));
            }
            if (tempMap.containsKey("leftPais")) {
                tempMap.put("leftPais", StringUtil.implode(NxGhzTool.toGhzCardIds(leftCards), ","));
            }
            if (tempMap.containsKey("nowAction")) {
                tempMap.put("nowAction", buildNowAction());
            }
            if (tempMap.containsKey("extend")) {
                tempMap.put("extend", buildExtend());
            }
            //            TableDao.getInstance().save(tempMap);
        }
        return tempMap.size() > 0 ? tempMap : null;
    }

    @Override
    public JsonWrapper buildExtend0(JsonWrapper wrapper) {
        wrapper.putString(1, StringUtil.implode(huConfirmList, ","));
        wrapper.putInt(2, moFlag);
        wrapper.putInt(3, toPlayCardFlag);
        wrapper.putInt(4, moSeat);
        if (moSeatPair != null) {
            String moSeatPairVal = moSeatPair.getId() + "_" + moSeatPair.getValue();
            wrapper.putString(5, moSeatPairVal);
        }
        wrapper.putInt(6, firstCard ? 1 : 0);
        if (beRemoveCard != null) {
            wrapper.putInt(7, beRemoveCard.getId());
        }
        wrapper.putInt(8, maxPlayerCount);
        wrapper.putInt(9, maxhuxi);
        wrapper.putInt(10, kepiao ? 1 : 0);
        wrapper.putInt(11, wuxiping ? 1 : 0);
        wrapper.putString(12, Boolean.toString(isAAConsume));
        wrapper.putInt(13, diaodiaoshou ? 1 : 0);
        JSONArray jsonArray = new JSONArray();
        for (int card : startLeftCards) {
            jsonArray.add(card);
        }
        wrapper.putString("startLeftCards", jsonArray.toString());
        JSONArray tempJsonArray = new JSONArray();
        for (int seat : tempActionMap.keySet()) {
            tempJsonArray.add(tempActionMap.get(seat).buildData());
        }
        wrapper.putString("tempActions", tempJsonArray.toString());
        wrapper.putLong(14, groupPaylogId);
        wrapper.putInt(15, autoPlayGlob);
        wrapper.putInt(16, autoTimeOut);
        wrapper.putInt(17, jiaBei);
        wrapper.putInt(18, jiaBeiFen);
        wrapper.putInt(19, jiaBeiShu);
        wrapper.putInt(20, below);
        wrapper.putInt(21, belowAdd);
        wrapper.putInt(22, this.chouPai);
        wrapper.putString(23, StringUtil.implode(chouCards, ","));
        wrapper.putInt(24, this.baoTingHuSwitchTemp ? 1 : 0);
        wrapper.putInt(25, daxiaoZhuo);
        wrapper.putInt(26, beikaobei);
        wrapper.putInt(27, shouqianshou);
        return wrapper;
    }

    @Override
    protected void deal() {
        if (lastWinSeat == 0) {
            int masterseat = playerMap.get(masterId).getSeat();
            setLastWinSeat(masterseat);
        }
        // 第一局随机庄家
        if (getPlayBureau() == 1) {
            setLastWinSeat(1 + new Random().nextInt(getMaxPlayerCount()));
        }
        setDisCardSeat(lastWinSeat);
        setNowDisCardSeat(lastWinSeat);
        setMoSeat(lastWinSeat);
        setToPlayCardFlag(1);
        markMoSeat(null, lastWinSeat);
        List<List<NxGhzCard>> list = faPai();
        int faPaiSeat = lastWinSeat;
        for (int j = 0; j < getMaxPlayerCount(); j++) {
            NxGhzPlayer player = seatMap.get(faPaiSeat);
            player.changeState(player_state.play);
            player.getFirstPais().clear();
            player.dealHandPais(list.get(j));
            player.getFirstPais().addAll(NxGhzTool.toGhzCardIds(new ArrayList<>(list.get(j))));//将初始手牌保存结算时发给客户端
            faPaiSeat = calcNextSeat(faPaiSeat);

            StringBuilder sb = new StringBuilder("NxGhz");
            sb.append("|").append(getId());
            sb.append("|").append(getPlayBureau());
            sb.append("|").append(player.getUserId());
            sb.append("|").append(player.getSeat());
            sb.append("|").append(player.getName());
            sb.append("|").append("faPai");
            sb.append("|").append(player.getHandPais());
            LogUtil.msgLog.info(sb.toString());
        }

        List<NxGhzCard> cards = list.get(getMaxPlayerCount());
        int leftSize = cards.size();
        if (chouPai > 0) {
            List<NxGhzCard> chuPaiList = cards.subList(leftSize - chouPai, leftSize);
            chouCards = NxGhzTool.toGhzCardIds(chuPaiList);
            cards = cards.subList(0, cards.size() - chouPai);

            StringBuilder sb = new StringBuilder("NxGhz");
            sb.append("|").append(getId());
            sb.append("|").append(getPlayBureau());
            sb.append("|").append("chouPai");
            sb.append("|").append(chuPaiList);
            LogUtil.msgLog.info(sb.toString());
        }

        // 桌上剩余的牌
        setLeftCards(cards);
        //桌上所有剩余牌
        setStartLeftCards(NxGhzTool.toGhzCardIds(list.get(getMaxPlayerCount())));
    }

    /**
     * 发牌
     *
     * @return
     */
    private List<List<NxGhzCard>> faPai() {
        List<Integer> copy = new ArrayList<>(NxGhzConstant.cardList);
        List<List<NxGhzCard>> list = NxGhzTool.fapai(copy, zp, getMaxPlayerCount());
        int checkTime = 0;
        while (checkTime < 10) {// 禁胡判断10次
            checkTime++;
            boolean isForbidHu = false;
            Iterator<List<NxGhzCard>> iterator = list.iterator();
            while (iterator.hasNext()) {
                List<NxGhzCard> next = iterator.next();
                if (isLargeForbidHu(next)) {
                    isForbidHu = true;
                    break;
                }
            }
            if (isForbidHu == true) {// 重新发牌
                copy = new ArrayList<>(NxGhzConstant.cardList);
                list = NxGhzTool.fapai(copy, zp, getMaxPlayerCount());
            } else {
                break;
            }
        }
        return list;
    }

    private boolean isLargeForbidHu(List<NxGhzCard> handcards) {
        List<NxGhzCard> copy = new ArrayList<>(handcards);
        if (copy.size() == 20) {// 庄家需要判断天胡和报听
            boolean isHaiDi = (getLeftCards().size() == 0) ? true : false;
            boolean canDiaoDiaoShou = this.isDiaodiaoshou();
            NxGhzHuLack lack = NxGhzTool.isHu(new ArrayList<>(), copy, null, true, 0, false, isWuxiping(), isHaiDi, canDiaoDiaoShou);
            if (lack.isHu()) {
//				System.out.println("起手天胡 重新发牌:" + copy);
                return true;
            }
            NxGhzCardIndexArr valArr = NxGhzTool.getMax(copy);
            NxGhzIndex index3 = valArr.getPaohzCardIndex(3);// 四张的牌
            if (index3 != null) {// 有四张相同的牌 含内圆的情况
//				System.out.println("起手有四张相同的牌 含内圆的情况 重新发牌:" + copy);
                return true;
            }
//			NxGhzCardIndexArr arr = NxGhzTool.getGuihzCardIndexArr(copy);
//			if(arr.getDuiziNum() >= 7) {// 对子有7对 则重新发牌
////				System.out.println("起手对子超过8对 重新发牌:" + copy);
//				return true;
//			}
            List<NxGhzCard> redCardList = NxGhzTool.findRedGhzs(copy);
            int redCardCount = redCardList.size();
            if (redCardCount < 3 || redCardCount > 11) {// 红牌小于3个 大于11个
//				System.out.println("起手红牌小于3个 大于11个:" + copy);
                return true;
            }
//			for(int index = 0; index < 14 ; index ++) {// 需要迭代判断14次
//				List<NxGhzCard> tempMjs = new ArrayList<>(copy);
//				tempMjs.remove(index);
//				if(isBaotingHu(tempMjs))// 如果有三个大门子报听
//					return true;
//			}
        } else if (copy.size() == 19) {// 闲家需要判断报听
            NxGhzCardIndexArr valArr = NxGhzTool.getMax(copy);
            NxGhzIndex index3 = valArr.getPaohzCardIndex(3);// 四张的牌
            if (index3 != null) {// 有四张相同的牌 含内圆的情况
                return true;
            }
//			NxGhzCardIndexArr arr = NxGhzTool.getGuihzCardIndexArr(copy);
//			if(arr.getDuiziNum() >= 7) {// 对子有7对 则重新发牌
////				System.out.println("起手对子超过8对 重新发牌:" + copy);
//				return true;
//			}
            List<NxGhzCard> redCardList = NxGhzTool.findRedGhzs(copy);
            int redCardCount = redCardList.size();
            if (redCardCount < 3 || redCardCount > 11) {// 红牌小于3个 大于11个
//				System.out.println("起手红牌小于3个 大于11个:" + copy);
                return true;
            }
//			boolean baotingHu = isBaotingHu(copy);
//			if(baotingHu)
//				return true;
        }
        return false;
    }


//	private boolean isBaotingHu(List<NxGhzCard> handcards) {
//		if(handcards.size() != 19)
//			return false;
//		for(int value = 1; value <= 10; value ++) {
//			NxGhzCard huCard = NxGhzCard.getGuihzCard(handcards, value);
//			if(huCard == null)
//				continue;
//			NxGhzHuLack lack = NxGhzTool.isHu(this, new ArrayList<>(), handcards, huCard, true, 0, false, isWuxiping());
//			if(lack.isHu())
//				return true;
//		}
//		for(int value = 101; value <= 110; value ++) {
//			NxGhzCard huCard = NxGhzCard.getGuihzCard(handcards, value);
//			if(huCard == null)
//				continue;
//			NxGhzHuLack lack = NxGhzTool.isHu(this, new ArrayList<>(), handcards, huCard, true, 0, false, isWuxiping());
//			if(lack.isHu())
//				return true;
//		}
//		return false;
//	}

    @Override
    public int getNextDisCardSeat() {
        if (disCardSeat == 0) {
            return lastWinSeat;
        }
        return calcNextSeat(disCardSeat);
    }

    /**
     * 计算seat右边的座位
     */
    public int calcNextSeat(int seat) {
        int nextSeat = seat + 1 > maxPlayerCount ? 1 : seat + 1;
        return nextSeat;
    }

    /**
     * 计算seat前面的座位
     */
    public int calcFrontSeat(int seat) {
        int frontSeat = seat - 1 < 1 ? maxPlayerCount : seat - 1;
        return frontSeat;
    }

    /**
     * 获取数醒座位
     */
    public int calcNextNextSeat(int seat) {
        int nextSeat = seat + 1 > maxPlayerCount ? 1 : seat + 1;
        int nextNextSeat = nextSeat + 1 > maxPlayerCount ? 1 : nextSeat + 1;
        return nextNextSeat;
    }

    @Override
    public Player getPlayerBySeat(int seat) {
        return seatMap.get(seat);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<Integer, Player> getSeatMap() {
        Object o = seatMap;
        return (Map<Integer, Player>) o;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<Long, Player> getPlayerMap() {
        Object o = playerMap;
        return (Map<Long, Player>) o;
    }

    @Override
    public CreateTableRes buildCreateTableRes(long userId, boolean isrecover, boolean isLastReady) {
        CreateTableRes.Builder res = CreateTableRes.newBuilder();
        buildCreateTableRes0(res);
        res.setNowBurCount(getPlayBureau());
        res.setTotalBurCount(getTotalBureau());
        res.setGotyeRoomId(gotyeRoomId + "");
        res.setTableId(getId() + "");
        res.setWanfa(playType);
        res.setRenshu(maxPlayerCount);
        if (leftCards != null) {
            res.setRemain(leftCards.size());
        } else {
            res.setRemain(0);
        }
        List<PlayerInTableRes> players = new ArrayList<>();
        for (NxGhzPlayer player : playerMap.values()) {
            PlayerInTableRes.Builder playerRes = player.buildPlayInTableInfo(userId, isrecover);
            playerRes.addRecover(player.getSeat() == lastWinSeat ? 1 : 0);
            if (player.getUserId() == userId) {
                playerRes.addAllHandCardIds(player.getHandPais());
                if (actionSeatMap.containsKey(player.getSeat())) {
                    List<Integer> actionList = actionSeatMap.get(player.getSeat());
                    if (actionList != null && !tempActionMap.containsKey(player.getSeat())) {
                        playerRes.addAllRecover(actionList);
                    }
                }
                List<NxGhzMenzi> passMenzis = player.getPassMenzi();
                List<Integer> intExts = new ArrayList<>();
                if (!passMenzis.isEmpty()) {
                    for (NxGhzMenzi passMenzi : passMenzis) {
                        List<Integer> menziIds = NxGhzTool.toGhzCardIds(NxGhzTool.findByVals(player.getHandGhzs(), passMenzi.getMenzi()));
                        if (menziIds.size() >= 2) {
                            intExts.addAll(menziIds);
                        }
                    }
                    playerRes.addAllIntExts(intExts);
                }
//				if(tingMap != null && tingMap.containsKey(player.getSeat())){
//					List<Integer> tings = tingMap.get(player.getSeat());
//					String tingStr = "";
//					if(tings != null && tings.size() > 0){
//						tingStr = StringUtil.implode(tings, ",");
//					}
//					playerRes.addStrExts(tingStr);
//				}
            }
            if (state == null)
                state = table_state.ready;
            playerRes.addExt(state.getId());
            playerRes.addExt(player.getTableHuxi());
            players.add(playerRes.build());
        }
        res.addAllPlayers(players);
        if (actionSeatMap.isEmpty()) {
            // int nextSeat = getNextDisCardSeat();
            if (nowDisCardSeat != 0) {
                if (toPlayCardFlag == 1) {
                    res.setNextSeat(nowDisCardSeat);
                } else {
                    res.setNextSeat(0);
                }
            }
        }
        res.addExt(nowDisCardSeat);
        res.addExt(payType);
        res.addExt(maxhuxi);
        res.addExt(kepiao ? 1 : 0);
        res.addExt(wuxiping ? 1 : 0);
        res.addExt(diaodiaoshou ? 1 : 0);
        return res.build();
    }

    @Override
    public void setConfig(int index, int val) {
    }

    public ClosingGhzInfoRes.Builder sendAccountsMsg(boolean over, boolean selfMo, List<Integer> winList, int maxFan, List<Integer> fanTypes, int totalTun, boolean isBreak) {

        // 大结算计算加倍分
        if (over && jiaBei == 1) {
            int jiaBeiPoint = 0;
            int loserCount = 0;
            for (NxGhzPlayer player : seatMap.values()) {
                if (player.getTotalPoint() > 0 && player.getTotalPoint() < jiaBeiFen) {
                    jiaBeiPoint += player.getTotalPoint() * (jiaBeiShu - 1);
                    player.setTotalPoint(player.getTotalPoint() * jiaBeiShu);
                } else if (player.getTotalPoint() < 0) {
                    loserCount++;
                }
            }
            if (jiaBeiPoint > 0) {
                for (NxGhzPlayer player : seatMap.values()) {
                    if (player.getTotalPoint() < 0) {
                        player.setTotalPoint(player.getTotalPoint() - (jiaBeiPoint / loserCount));
                    }
                }
            }
        }

        //大结算低于below分+belowAdd分
        if (over && belowAdd > 0 && playerMap.size() == 2) {
            for (NxGhzPlayer player : seatMap.values()) {
                int totalPoint = player.getTotalPoint();
                if (totalPoint > -below && totalPoint < 0) {
                    player.setTotalPoint(player.getTotalPoint() - belowAdd);
                } else if (totalPoint < below && totalPoint > 0) {
                    player.setTotalPoint(player.getTotalPoint() + belowAdd);
                }
            }
        }

        List<ClosingGhzPlayerInfoRes> list = new ArrayList<>();
        List<ClosingGhzPlayerInfoRes.Builder> builderList = new ArrayList<>();
        NxGhzPlayer winPlayer = null;

        for (NxGhzPlayer player : seatMap.values()) {
            if (winList != null && winList.contains(player.getSeat())) {
                winPlayer = seatMap.get(player.getSeat());
            }
            ClosingGhzPlayerInfoRes.Builder build;
            if (over) {
                build = player.bulidTotalClosingPlayerInfoRes(totalTun);
            } else {
                build = player.bulidOneClosingPlayerInfoRes();
            }
            build.addAllFirstCards(player.getFirstPais());//将初始手牌装入网络对象

            builderList.add(build);

            // 信用分
            if (isCreditTable()) {
                player.setWinLoseCredit(player.getTotalPoint() * creditDifen);
            }
        }
        // 信用分计算
        if (isCreditTable()) {
            // 计算信用负分
            calcNegativeCredit();

            long dyjCredit = 0;
            for (NxGhzPlayer player : seatMap.values()) {
                if (player.getWinLoseCredit() > dyjCredit) {
                    dyjCredit = player.getWinLoseCredit();
                }
            }
            for (ClosingGhzPlayerInfoRes.Builder builder : builderList) {
                NxGhzPlayer player = seatMap.get(builder.getSeat());
                calcCommissionCredit(player, dyjCredit);
                builder.setWinLoseCredit(player.getWinLoseCredit());
                builder.setCommissionCredit(player.getCommissionCredit());
            }
        } else if (isGroupTableGoldRoom()) {
            // -----------亲友圈金币场---------------------------------
            for (NxGhzPlayer player : seatMap.values()) {
                player.setWinGold(player.getTotalPoint() * gtgDifen);
            }
            calcGroupTableGoldRoomWinLimit();
            for (ClosingGhzPlayerInfoRes.Builder builder : builderList) {
                NxGhzPlayer player = seatMap.get(builder.getSeat());
                builder.setWinLoseCredit(player.getWinGold());
            }
        }
        for (ClosingGhzPlayerInfoRes.Builder builder : builderList) {
            list.add(builder.build());
        }

        ClosingGhzInfoRes.Builder res = ClosingGhzInfoRes.newBuilder();
        res.addAllLeftCards(NxGhzTool.toGhzCardIds(leftCards));
        if (fanTypes != null) {
            res.addAllFanTypes(fanTypes);
        }
        if (winPlayer != null) {
            res.setFan(maxFan);
            res.setHuxi(winPlayer.getLostPoint());
            res.setTotalTun(totalTun);
            res.setHuSeat(winPlayer.getSeat());
            if (winPlayer.getHu() != null && winPlayer.getHu().getCheckCard() != null) {
                res.setHuCard(winPlayer.getHu().getCheckCard().getId());
            }
        }
        res.addAllClosingPlayers(list);
        res.setIsBreak(isBreak ? 1 : 0);
        res.setWanfa(getWanFa());
        res.setGroupLogId((int) groupPaylogId);
        res.addAllExt(buildAccountsExt(over));
        res.addAllStartLeftCards(startLeftCards);
        res.addAllChouCards(chouCards);
        for (NxGhzPlayer player : seatMap.values()) {
            player.writeSocket(res.build());
        }
        return res;
    }

    @Override
    public void sendAccountsMsg() {
        ClosingGhzInfoRes.Builder res = sendAccountsMsg(true, false, null, 0, null, 0, true);
        saveLog(true, 0L, res.build());

    }

    public List<String> buildAccountsExt(boolean isOver) {
        List<String> ext = new ArrayList<>();
        ext.add(id + "");
        ext.add(masterId + "");
        ext.add(TimeUtil.formatTime(TimeUtil.now()));
        ext.add(playType + "");
        ext.add(getConifg(0) + "");
        ext.add(playBureau + "");
        ext.add(isOver ? 1 + "" : 0 + "");
        ext.add(maxPlayerCount + "");
        ext.add(maxhuxi + "");
        ext.add(kepiao ? 1 + "" : 0 + "");
        ext.add(wuxiping ? 1 + "" : 0 + "");
        ext.add(lastWinSeat + "");
        ext.add(diaodiaoshou ? 1 + "" : 0 + "");
        ext.add(isGroupRoom() ? loadGroupId() : "0");
        ext.add(daxiaoZhuo+"");
//        ext.add(beikaobei+"");
//        ext.add(shouqianshou+"");
        return ext;
    }

    @Override
    public int getMaxPlayerCount() {
        return maxPlayerCount;
    }

    public void createTable(Player player, int play, int bureauCount, List<Integer> params) throws Exception {
        long id = getCreateTableId(player.getUserId(), play);
        TableInf info = new TableInf();
        info.setMasterId(player.getUserId());
        info.setRoomId(0);
        info.setPlayType(play);
        info.setTableId(id);
        info.setTotalBureau(bureauCount);
        info.setPlayBureau(1);
        info.setServerId(GameServerConfig.SERVER_ID);
        info.setCreateTime(new Date());
        info.setDaikaiTableId(daikaiTableId);
        info.setExtend(buildExtend());
        TableDao.getInstance().save(info);
        loadFromDB(info);
        setMaxPlayerCount(3);
        setPayType(params.get(2).intValue());// AA支付 房主支付
        int maxhuxi = params.get(3).intValue();// 最大息数
        setMaxhuxi(maxhuxi);
        boolean kepiao = params.get(4).intValue() == 1 ? true : false;// 漂
        boolean wuxiping = params.get(5).intValue() == 1 ? true : false;// 无息平
        boolean diaodiaoshou = params.get(6).intValue() == 1 ? true : false;// 吊吊手
        setKepiao(kepiao);
        setWuxiping(wuxiping);
        setDiaodiaoshou(diaodiaoshou);
        if (payType == 1) {
            setAAConsume(true);
        }
        int playerCount = StringUtil.getIntValue(params, 7, 0);
        setMaxPlayerCount(playerCount);
        int autoTime = StringUtil.getIntValue(params, 8, 0);
        autoPlayGlob = StringUtil.getIntValue(params, 9, 0);
        if (autoTime == 1) {
            autoTime = 60;
        }
        autoTimeOut2 = autoTimeOut = autoTime * 1000;
        this.autoPlay = autoTime > 0;
        this.jiaBei = StringUtil.getIntValue(params, 10, 0);
        this.jiaBeiFen = StringUtil.getIntValue(params, 11, 100);
        this.jiaBeiShu = StringUtil.getIntValue(params, 12, 1);
        if (playerCount == 2) {
            int belowAdd = StringUtil.getIntValue(params, 13, 0);   // 低于 below 加 belowAdd 分
            if (belowAdd <= 100 && belowAdd >= 0) {
                this.belowAdd = belowAdd;
            }
            int below = StringUtil.getIntValue(params, 14, 0);
            if (below <= 100 && below >= 0) {
                this.below = below;
                if (belowAdd > 0 && below == 0) {
                    this.below = 10;
                }
            }
        }
        this.chouPai = StringUtil.getIntValue(params, 15, 0);
        if (this.chouPai != 0 && this.chouPai != 10 && this.chouPai != 20) {
            this.chouPai = 10;
        }
        if (playerCount != 2) { // 2人玩法才有抽牌
            this.chouPai = 0;
        }
        this.daxiaoZhuo = StringUtil.getIntValue(params, 16, 0);
        this.beikaobei = StringUtil.getIntValue(params, 17, 0);
        this.shouqianshou = StringUtil.getIntValue(params, 18, 0);
        this.baoTingHuSwitchTemp = true;
    }

    @Override
    public boolean isTest() {
        return false;// 是否测试环境
    }

    @Override
    public void checkReconnect(Player player) {
        checkMo();// 重连时检测是否能摸牌
        sendTingInfo((NxGhzPlayer) player);
    }

    @Override
    public void checkAutoPlay() {
        synchronized (this) {

            // -----------------有人发起解散时，停止托管，并停止托管计时----------------------
            if (getSendDissTime() > 0) {
                for (NxGhzPlayer player : seatMap.values()) {
                    if (player.getLastCheckTime() > 0) {
                        player.setLastCheckTime(player.getLastCheckTime() + 1 * 1000);
                    }
                }
                return;
            }

            // ------------------自动准备-----------------------------------
            if (autoPlay && state == table_state.ready && playedBureau > 0) {
                ++timeNum;
                for (NxGhzPlayer player : seatMap.values()) {
                    if (player.getState() == player_state.ready) {
                        continue;
                    }
                    // 玩家进入托管后，5秒自动准备
                    if (timeNum >= 5 && player.isAutoPlay()) {
                        autoReady(player);
                    } else if (timeNum >= 30) {
                        autoReady(player);
                    }
                }
                return;
            }

            // ------------------自动操作或出牌-----------------------------------
            if (!autoPlay) {
                return;
            }

            if (isAutoPlayOff()) {
                // 托管关闭
                for (int seat : seatMap.keySet()) {
                    NxGhzPlayer player = seatMap.get(seat);
                    player.setAutoPlay(false, this);
                    player.setLastOperateTime(System.currentTimeMillis());
                }
                return;
            }

            int timeout = autoTimeOut;
            long autoPlayTime = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "autoPlayTimePhz", 0 * 1000);
            long now = TimeUtil.currentTimeMillis();

            // ------------------有人可以操作,就执行操作-------------------------------
            if (!actionSeatMap.isEmpty()) {
                int action = 0, seat = 0;
                for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
                    List<Integer> actinList = NxGhzDisAction.parseToDisActionList(entry.getValue());
                    int minAction = NxGhzDisAction.getMaxPriorityAction(actinList);
                    if (action == 0) {
                        action = minAction;
                        seat = entry.getKey();
                    } else if (NxGhzDisAction.findPriorityAction2(minAction) < NxGhzDisAction.findPriorityAction2(action)) {
                        action = minAction;
                        seat = entry.getKey();
                    } else if (NxGhzDisAction.findPriorityAction2(minAction) == NxGhzDisAction.findPriorityAction2(action)) {
                        int nearSeat = getNearSeat(disCardSeat, Arrays.asList(seat, entry.getKey()));
                        seat = nearSeat;
                    }
                }
                if (action > 0 && seat > 0) {
                    NxGhzPlayer player = seatMap.get(seat);
                    if (player == null) {
                        LogUtil.errorLog.error("auto play error:tableId={},seat={} is null,seatMap={},playerMap={}", id, seat, seatMap.keySet(), playerMap.keySet());
                        return;
                    }

                    boolean auto = player.isAutoPlay();
                    if (!auto) {
                        auto = checkPlayerAuto(player, timeout);
                    }
                    if (auto) {
                        if (player.getAutoPlayTime() == 0L) {
                            player.setAutoPlayTime(now);
                        } else if (player.getAutoPlayTime() > 0L && now - player.getAutoPlayTime() >= autoPlayTime) {
                            player.setAutoPlayTime(0L);
                            if (action == NxGhzDisAction.action_chi || action == NxGhzDisAction.action_liu || action == NxGhzDisAction.action_piao) {
                                action = NxGhzDisAction.action_pass;
                            }
                            if (action == NxGhzDisAction.action_pass || action == NxGhzDisAction.action_peng || action == NxGhzDisAction.action_hu || action == NxGhzDisAction.action_wei) {
                                play(player, new ArrayList<>(), action);
                            } else {
                                checkMo();
                            }
                        }
                        return;
                    }
                    for (NxGhzPlayer other : seatMap.values()) {
                        if (other.getSeat() != seat && !other.isAutoPlay()) {
                            other.setLastCheckTime(0);
                        }
                    }
                }
            } else {
                // -------------------- 没有操作，检查是否可以出牌--------------------
                NxGhzPlayer player = seatMap.get(nowDisCardSeat);
                if (player == null) {
                    return;
                }
                if (toPlayCardFlag == 1) {
                    boolean auto = player.isAutoPlay();
                    if (!auto) {
                        auto = checkPlayerAuto(player, timeout);
                    }
                    if (auto) {
                        if (player.getAutoPlayTime() == 0L) {
                            player.setAutoPlayTime(now);
                        } else if (player.getAutoPlayTime() > 0L && now - player.getAutoPlayTime() >= autoPlayTime) {
                            player.setAutoPlayTime(0L);
                            NxGhzCard paohzCard = NxGhzTool.autoDisCard(player.getHandGhzs(), player);
                            if (paohzCard != null) {
                                play(player, Arrays.asList(paohzCard.getId()), 0);
                            }
                        }
                    }
                } else {
                    checkMo();
                }
            }
        }
    }

    public boolean checkPlayerAuto(NxGhzPlayer player, int timeout) {
        long now = TimeUtil.currentTimeMillis();
        boolean auto = false;
        if (player.getLastCheckTime() > 0) {
            auto = (int) (now - player.getLastCheckTime()) >= timeout;
            if (auto) {
                player.setAutoPlay(true, this);
            }
        } else {
            player.setLastCheckTime(now);
            player.setCheckAuto(true);
        }
        return auto;
    }

    @Override
    public Class<? extends Player> getPlayerClass() {
        return NxGhzPlayer.class;
    }

    public NxGhzCard getNextCard(int val) {
        if (this.leftCards.size() > 0) {
            Iterator<NxGhzCard> iterator = this.leftCards.iterator();
            NxGhzCard find = null;
            while (iterator.hasNext()) {
                NxGhzCard paohzCard = iterator.next();
                if (paohzCard.getVal() == val) {
                    find = paohzCard;
                    iterator.remove();
                    break;
                }
            }
            dbParamMap.put("leftPais", JSON_TAG);
            return find;
        }
        return null;
    }

    public NxGhzCard getNextCard() {
        if (this.leftCards.size() > 0) {
            NxGhzCard card = this.leftCards.remove(0);
            dbParamMap.put("leftPais", JSON_TAG);
            return card;
        }
        return null;
    }

    public List<NxGhzCard> getLeftCards() {
        return leftCards;
    }

    public void setLeftCards(List<NxGhzCard> leftCards) {
        if (leftCards == null) {
            this.leftCards.clear();
        } else {
            this.leftCards = leftCards;
        }
        dbParamMap.put("leftPais", JSON_TAG);
    }

    public void setStartLeftCards(List<Integer> startLeftCards) {
        if (startLeftCards == null) {
            this.startLeftCards.clear();
        } else {
            this.startLeftCards = startLeftCards;
        }
        dbParamMap.put("leftPais", JSON_TAG);
    }

    public int getMoSeat() {
        return moSeat;
    }

    public void setMoSeat(int lastMoSeat) {
        this.moSeat = lastMoSeat;
        changeExtend();
    }

    public List<NxGhzCard> getNowDisCardIds() {
        return nowDisCardIds;
    }

    public void setNowDisCardIds(List<NxGhzCard> nowDisCardIds) {
        this.nowDisCardIds = nowDisCardIds;
        dbParamMap.put("nowDisCardIds", JSON_TAG);
    }

    /**
     * 打出的牌是刚刚摸的
     */
    public boolean isMoFlag() {
        return moFlag == 1;
    }

    public void setMoFlag(int moFlag) {
        if (this.moFlag != moFlag) {
            this.moFlag = moFlag;
            changeExtend();
        }
    }

    public void markMoSeat(int seat, int action) {
        checkMoMark = new KeyValuePair<>();
        checkMoMark.setId(seat);
        checkMoMark.setValue(action);
        changeExtend();
    }

    private void clearMarkMoSeat() {
        checkMoMark = null;
        changeExtend();
    }

    public void markMoSeat(NxGhzCard card, int seat) {
        moSeatPair = new KeyValuePair<>();
        if (card != null) {
            moSeatPair.setId(card.getId());
        }
        moSeatPair.setValue(seat);
        changeExtend();
    }

    public void clearMoSeatPair() {
        moSeatPair = null;
    }

    public int getToPlayCardFlag() {
        return toPlayCardFlag;
    }

    public void setToPlayCardFlag(int toPlayCardFlag) {
        if (this.toPlayCardFlag != toPlayCardFlag) {
            this.toPlayCardFlag = toPlayCardFlag;
            changeExtend();
        }
    }

    @Override
    public boolean consumeCards() {
        return SharedConstants.consumecards;
    }

    public Map<Integer, List<Integer>> getActionSeatMap() {
        return actionSeatMap;
    }

    public boolean isFirstCard() {
        return firstCard;
    }

    public void setFirstCard(boolean firstCard) {
        this.firstCard = firstCard;
        changeExtend();
    }

    public int getMaxhuxi() {
        return maxhuxi;
    }

    public void setMaxhuxi(int maxhuxi) {
        this.maxhuxi = maxhuxi;
    }

    public boolean isKepiao() {
        return this.kepiao;
    }

    public void setKepiao(boolean kepiao) {
        this.kepiao = kepiao;
    }

    public boolean isWuxiping() {
        return this.wuxiping;
    }

    public void setWuxiping(boolean wuxiping) {
        this.wuxiping = wuxiping;
    }

    public boolean isDiaodiaoshou() {
        return diaodiaoshou;
    }

    public void setDiaodiaoshou(boolean diaodiaoshou) {
        this.diaodiaoshou = diaodiaoshou;
    }

    /**
     * 对应的座位cardId-seat
     */
    public KeyValuePair<Integer, Integer> getMoSeatPair() {
        return moSeatPair;
    }

    public NxGhzCard getBeRemoveCard() {
        return beRemoveCard;
    }

    /**
     * 桌子上移除的牌
     */
    public void setBeRemoveCard(NxGhzCard beRemoveCard) {
        this.beRemoveCard = beRemoveCard;
        changeExtend();
    }

    /**
     * 是否是该玩家摸的牌
     */
    public boolean isMoByPlayer(NxGhzPlayer player) {
        if (moSeatPair != null && moSeatPair.getValue() == player.getSeat()) {
            if (nowDisCardIds != null && !nowDisCardIds.isEmpty()) {
                if (nowDisCardIds.get(0).getId() == moSeatPair.getId()) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setMaxPlayerCount(int maxPlayerCount) {
        this.maxPlayerCount = maxPlayerCount;
    }

    @Override
    public int getDissPlayerAgreeCount() {
        return super.getDissPlayerAgreeCount();
    }

    @Override
    public void createTable(Player player, int play, int bureauCount, List<Integer> params, List<String> strParams, Object... objects) throws Exception {
        createTable(player, play, bureauCount, params);
    }

    @Override
    public void createTable(Player player, int play, int bureauCount, Object... objects) throws Exception {
    }

    public int getAutoTimeOut() {
        return autoTimeOut;
    }

    public String getTableMsg() {

        Map<String, Object> json = new HashMap<>();
        json.put("wanFa", "南县鬼胡子");
        if (isGroupRoom()) {
            json.put("roomName", getRoomName());
        }
        json.put("playerCount", getPlayerCount());
        json.put("count", getTotalBureau());
        if (autoPlay) {
            json.put("autoTime", autoTimeOut / 1000);
            if (autoPlayGlob == 1) {
                json.put("autoName", "单局");
            } else {
                json.put("autoName", "整局");
            }
        }
        return JSON.toJSONString(json);
    }

    public static final List<Integer> wanfaList = Arrays.asList(GameUtil.play_type_nxghz);

    public static void loadWanfaTables(Class<? extends BaseTable> cls) {
        for (Integer integer : wanfaList) {
            TableManager.wanfaTableTypesPut(integer, cls);
        }
    }

    public void sendTingInfo(NxGhzPlayer player) {
        if (player.isSiShou() || player.isSiShouTemp()) {
            player.writeSocket(PlayCardResMsg.DaPaiTingPaiRes.newBuilder().build());
            return;
        }
        long start = System.currentTimeMillis();
        if (player.getHandGhzs().size() % 3 == 2) {
            if (player.isNeedCheckTing()) {
                PlayCardResMsg.DaPaiTingPaiRes.Builder tingInfo = PlayCardResMsg.DaPaiTingPaiRes.newBuilder();
                List<NxGhzCard> cards = new ArrayList<>(player.getHandGhzs());
                Map<Integer, List<NxGhzCard>> checkedVal = new HashMap<>();
                for (NxGhzCard card : player.getHandGhzs()) {
                    if (player.getSiShouPais().contains(card.getVal())) {
                        continue;
                    }
                    List<NxGhzCard> huCards;
                    if (checkedVal.containsKey(card.getVal())) {
                        huCards = checkedVal.get(card.getVal());
                    } else {
                        cards.remove(card);
                        huCards = NxGhzTool.getTingZps(cards, player);
                        cards.add(card);
                        if (huCards == null || huCards.size() == 0) {
                            continue;
                        }
                        checkedVal.put(card.getVal(), huCards);
                    }
                    PlayCardResMsg.DaPaiTingPaiInfo.Builder ting = PlayCardResMsg.DaPaiTingPaiInfo.newBuilder();
                    ting.setMajiangId(card.getId());
                    for (NxGhzCard zp : huCards) {
                        ting.addTingMajiangIds(zp.getId());
                    }
                    tingInfo.addInfo(ting.build());
                }
                if (tingInfo.getInfoCount() > 0) {
                    player.setDaTingMsg(tingInfo.build());
                } else {
                    player.clearTingMsg();
                }
                player.setNeedCheckTing(false);
            }
            if (player.getDaTingMsg() != null) {
                player.writeSocket(player.getDaTingMsg());
            }
        } else {
            if (player.isNeedCheckTing()) {
            	player.clearTingpai();
                List<NxGhzCard> cards = new ArrayList<>(player.getHandGhzs());
                List<NxGhzCard> huCards = NxGhzTool.getTingZps(cards, player);
                if (huCards != null && huCards.size() > 0) {
                    PlayCardResMsg.TingPaiRes.Builder ting = PlayCardResMsg.TingPaiRes.newBuilder();
                    for (NxGhzCard zp : huCards) {
                        ting.addMajiangIds(zp.getId());
                        player.addTingpai(zp.getVal());
                    }
                    player.setTingMsg(ting.build());
                } else {
                    player.clearTingMsg();
                }
                player.setNeedCheckTing(false);
            }
            if (player.getTingMsg() != null) {
                player.writeSocket(player.getTingMsg());
            }
        }

        long timeUse = System.currentTimeMillis() - start;
        if (timeUse > 50) {
            StringBuilder sb = new StringBuilder("NxGhz|sendTingInfo");
            sb.append("|").append(timeUse);
            sb.append("|").append(start);
            sb.append("|").append(getId());
            sb.append("|").append(getPlayBureau());
            sb.append("|").append(player.getUserId());
            sb.append("|").append(player.getSeat());
            sb.append("|").append(player.getHandPais());
            LogUtil.monitorLog.info(sb.toString());
        }
    }

    public void resetPassChi(NxGhzPlayer player, NxGhzCard disCard, int nowAction) {
        // 只有一方操作，过吃就过了
        if (actionSeatMapCopy.size() <= 1) {
            clearPassChiTemp();
            return;
        }
        for (NxGhzPlayer p : seatMap.values()) {
            if (p.getUserId() != player.getUserId()) {
                // 不是当前操作牌的人
                if (p.hasPassChiTemp()) {
                    List<Integer> actList = actionSeatMapCopy.get(p.getSeat());
                    if (actList != null && NxGhzCheckCardBean.hasChi(actList)) {
                        // 有吃
                        if (nowAction != NxGhzDisAction.action_chi) {
                            p.resetPassChi();
                        } else {
                            List<Integer> list = NxGhzDisAction.parseToDisActionList(actList);
                            int maxAction = NxGhzDisAction.getMaxPriorityActionWithoutHu(list);
                            if (maxAction == NxGhzDisAction.action_chi) {
                                if (moSeat == player.getSeat()) {
                                    p.resetPassChi();
                                }
                            }
                        }
                    }
                }
            }
            p.clearPassChiTemp();
        }
    }

    public void clearPassChiTemp() {
        for (NxGhzPlayer p : seatMap.values()) {
            p.clearPassChiTemp();
        }
    }

    public NxGhzCheckCardBean checkHuAfterWei(NxGhzPlayer player) {
        NxGhzCheckCardBean checkBean = new NxGhzCheckCardBean();
        NxGhzHuLack huBean = player.checkHu(null, true);
        if (huBean.isHu()) {
            checkBean.setHu(true);
        }
        return checkBean;
    }

    public boolean isBaoTingHuSwitchTemp() {
        return baoTingHuSwitchTemp;
    }

    public void setBaoTingHuSwitchTemp(boolean baoTingHuSwitchTemp) {
        this.baoTingHuSwitchTemp = baoTingHuSwitchTemp;
        changeExtend();
    }

	public int getDaxiaoZhuo() {
		return daxiaoZhuo;
	}

	public void setDaxiaoZhuo(int daxiaoZhuo) {
		this.daxiaoZhuo = daxiaoZhuo;
	}
    
	public boolean isDazhuo(){
		return this.daxiaoZhuo == 1;
	}
	
	public boolean isBeikaobei(){
		return this.beikaobei == 1;
	}
	public boolean isShouqianShou(){
		return this.shouqianshou == 1;
	}
    
}
