package com.sy599.game.qipai.yjmj.bean;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.sy599.game.GameServerConfig;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.FirstmythConstants;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.common.constant.SharedConstants.player_state;
import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.db.bean.TableInf;
import com.sy599.game.db.bean.UserGroupPlaylog;
import com.sy599.game.db.bean.UserPlaylog;
import com.sy599.game.db.dao.TableDao;
import com.sy599.game.db.dao.TableLogDao;
import com.sy599.game.manager.TableManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.ComMsg.ComRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.GangMoMajiangRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.GangPlayMajiangRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.MoMajiangRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.PlayMajiangRes;
import com.sy599.game.msg.serverPacket.TableMjResMsg;
import com.sy599.game.msg.serverPacket.TableRes.*;
import com.sy599.game.qipai.yjmj.constant.YjMjConstants;
import com.sy599.game.qipai.yjmj.rule.MajiangHelper;
import com.sy599.game.qipai.yjmj.rule.RobotAI;
import com.sy599.game.qipai.yjmj.rule.YjMj;
import com.sy599.game.qipai.yjmj.tool.YjMjResTool;
import com.sy599.game.qipai.yjmj.tool.YjMjTool;
import com.sy599.game.qipai.yjmj.tool.YjMjQipaiTool;
import com.sy599.game.qipai.yjmj.tool.YjMjHelper;
import com.sy599.game.qipai.yjmj.tool.HuUtil;
import com.sy599.game.udplog.UdpLogger;
import com.sy599.game.util.*;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 沅江麻将牌桌信息
 */
public class YjMjTable extends BaseTable {
    /**
     * 当前桌上打出的牌
     */
    private List<YjMj> nowDisCardIds = new ArrayList<>();
    /**
     * 0胡 1碰 2明杠 3暗杠 4接杠 5杠爆 6报听
     */
    private Map<Integer, List<Integer>> actionSeatMap = new ConcurrentHashMap<>();
    /**
     * 0胡 1碰 2明杠 3暗杠 4接杠 5杠爆 6报听
     */
    private Map<Integer, Map<Integer, List<Integer>>> gangSeatMap = new ConcurrentHashMap<>();
    /**
     * 房间最大玩家人数上限
     */
    private int maxPlayerCount = 4;
    /**
     * 当前剩下的牌（庄上的牌）
     */
    private List<YjMj> leftMajiangs = new ArrayList<>();
    /**
     * 当前房间所有玩家信息map
     */
    private Map<Long, YjMjPlayer> playerMap = new ConcurrentHashMap<Long, YjMjPlayer>();
    /**
     * 座位对应的玩家信息MAP
     */
    private Map<Integer, YjMjPlayer> seatMap = new ConcurrentHashMap<Integer, YjMjPlayer>();
    /**
     * 胡确认信息
     */
    private Map<Integer, Integer> huConfirmMap = new HashMap<>();
    /**
     * 抓鸟
     */
    private int birdNum;
    /**
     * 计算庄闲 (1胡牌为庄 2上把为庄)
     */
    private int isCalcBanker;
    /**
     * 计算鸟的算法 1乘法 2加法
     */
    private int calcBird;

    /**
     * 番数上限 0无上限  默认24倍  实际番数上限定义在YJMajiangConstants
     */
    private int fanshuLimit;
    /**
     * 是否有门清 0无门清 1有门清
     */
    private int hasMenQing;
    /**
     * 门清将将胡是否可接炮  0不可接炮  1可接炮
     */
    private int menQingJiangJiangHu;
    /**
     * 一字撬有喜 没喜  0有喜  1没喜
     */
    private int yizhiqiao;
    /**
     * 卡撬 一字撬有喜时 碰碰胡且手上的牌只有四张牌是两个对子时不能胡牌 只能碰
     */
    private int kaqiao;
    /**
     * 摸麻将的seat
     */
    private int moMajiangSeat;
    /**
     * 摸杠的麻将
     */
    private YjMj moGang;
    /**
     * 杠出来的麻将
     */
    private YjMj gangMajiang;
    /**
     * 摸杠胡
     */
    private List<Integer> moGangHuList = new ArrayList<>();
    /**
     * 杠后出的两张牌
     */
    private List<YjMj> gangDisMajiangs = new ArrayList<>();
    /**
     * 摸海底捞的座位（最后摸牌的座位）
     */
    private int moLastMajiangSeat;
    /**
     * 已报听的座位号---是否pass胡 0没有 1pass胡
     */
    private Map<Integer, Integer> baotingSeat = new HashMap<>();
    /**
     * 最后一张麻将
     */
    private YjMj lastMajiang;
    /**
     * 当前出牌action
     */
    private int disEventAction;
    /**
     * 过手碰记录 玩家如果第一次没有碰  再没过手之前 不能再碰这个麻将
     */
    private Map<Integer, List<Integer>> pengPassMap = new HashMap<>();
    /**
     * 过手胡记录 玩家如果第一次没有胡牌  再没过手之前 不能再接炮
     */
    private List<Integer> huPassList = new ArrayList<>();

    /**
     * 鸟牌集
     */
    private List<YjMj> birdPaiList = new ArrayList<>();

    private long groupPaylogId = 0;  //俱乐部战绩记录id

    private int readyTime = 0;
    /**
     * 托管1：单局，2：全局
     */
    private int autoPlayGlob;
    private int autoTableCount;
    // 托管时间
    private int autoTime;
    private int tableStatus;//特殊状态 1飘分

    // 是否加倍：0否，1是
    private int jiaBei;
    // 加倍分数：低于xx分进行加倍
    private int jiaBeiFen;
    // 加倍倍数：翻几倍
    private int jiaBeiShu;
    // 低于 below 加 belowAdd 分
    private int belowAdd = 0;
    private int below = 0;

    // 抽牌，0表示不抽，13，26表示抽牌的数量
    private int chouPai = 0;
    // 抽牌牌堆
    List<Integer> chouCards = new ArrayList<>();

    // 是否码码胡
    private int maMaHu = 0;

    public List<YjMj> getBirdPaiList() {
        return birdPaiList;
    }

    public void setBirdPaiList(List<YjMj> birdPaiList) {
        this.birdPaiList = birdPaiList;
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
    public int isCanPlay() {
        return 0;
    }

    @Override
    public void calcOver() {
        List<Integer> winList = new ArrayList<>(huConfirmMap.keySet());
        boolean selfMo = false;
        int[] birdMjIds = null;
        int[] seatBirds = null;
        Map<Integer, Integer> seatBirdMap = new HashMap<>();
        boolean flow = false;
        int startseat = 0;
        int fangPaoSeat = 0;
        int[] jiePaoSeat = null;
        if (winList.size() == 0) { // 流局
            flow = true;
        } else { // 先判断是自摸还是放炮
            YjMjPlayer winPlayer = null;
            if (winList.size() == 1) {
                winPlayer = seatMap.get(winList.get(0));
                if ((winPlayer.isAlreadyMoMajiang() || winPlayer.isGangshangHua()) && winList.get(0) == moMajiangSeat) {
                    if (moGang == null)
                        selfMo = true;
                }
            }
            // 如果通炮按放炮的座位开始算 通炮从放炮的开始算
            if (isCalcBanker == 1) { // 胡牌为抓鸟庄
                startseat = winList != null && winList.size() > 1 ? disCardSeat : winList.get(0);
            } else {// 上局赢家为抓鸟庄
                startseat = lastWinSeat;
            }
            if (!winList.isEmpty() && selfMo) { // 自摸才扎鸟
                pickBird();// 先砸鸟
                seatBirds = birdToSeat(startseat);// 抓到鸟的座位
            }

            if (selfMo) {
                int winSeat = winList.get(0);
                int winBirdNum = calcBirdNum(seatBirds, winSeat);
                seatBirdMap.put(winList.get(0), winBirdNum);
                int loseTotalPoint = 0;
                for (int seat : seatMap.keySet()) {
                    if (!winList.contains(seat)) {// 除了赢家的其他人
                        YjMjPlayer player = seatMap.get(seat);
                        int losePoint = player.getLostPoint();
                        int birdCount = calcBirdNum(seatBirds, seat);
                        if (birdCount != 0) {
                            seatBirdMap.put(seat, birdCount);
                        }
                        losePoint = calcBirdPoint(losePoint, winBirdNum + birdCount);// 鸟加成
                        loseTotalPoint += losePoint;
                        player.setLostPoint(losePoint);
                    }
                }
                for (int seat : winList) {
                    YjMjPlayer player = seatMap.get(seat);
                    player.changeAction(0, 1);
                    player.getMyExtend().setMjFengshen(FirstmythConstants.firstmyth_index8, 1);
                    player.setLostPoint(-loseTotalPoint);
                }
            } else {
                // 放炮不扎鸟
                YjMjPlayer losePlayer = seatMap.get(disCardSeat);
                losePlayer.getMyExtend().setMjFengshen(FirstmythConstants.firstmyth_index10, winList.size());
                int totalLosePoint = 0;
                jiePaoSeat = new int[winList.size()];
                int i = 0;
                for (int seat : winList) {// 胡牌
                    winPlayer = seatMap.get(seat);
                    int point = winPlayer.getLostPoint();
                    point *= 2;// 放炮翻倍
                    if (getFanshuLimit() > 0 && Math.abs(point) >= YjMjConstants.fanshuLimit) {// 放炮加上番数上限
                        point = YjMjConstants.fanshuLimit;
                    }
                    winPlayer.changeAction(1, 1);
                    totalLosePoint += point;
                    winPlayer.setLostPoint(point);
                    jiePaoSeat[i++] = seat;
                }
                losePlayer.changeAction(2, 1);
                losePlayer.setLostPoint(-totalLosePoint);
                fangPaoSeat = losePlayer.getSeat();
            }
        }
        // 不管流局都加分
        calcExtraPoint(winList);// 杠分和一字撬有喜算分
        for (YjMjPlayer player : seatMap.values()) {//设置玩家局分point
            player.changePoint(player.getLostPoint());
            logHuPoint(player);
        }

        boolean over = playBureau >= totalBureau;
        if (autoPlayGlob > 0) {
            //是否解散
            boolean diss = false;
            if (autoPlayGlob == 1) {
                for (YjMjPlayer seat : seatMap.values()) {
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
                over = true;
            }
        }

        YjClosingInfoRes.Builder res = sendAccountsMsg(over, selfMo, winList, birdMjIds, seatBirds, seatBirdMap, false, startseat, fangPaoSeat, jiePaoSeat);
        if (!flow) {// 没有流局
            if (winList.size() > 1) {// 一炮多响设置放炮的人为庄家
                setLastWinSeat(disCardSeat);
            } else {
                setLastWinSeat(winList.get(0));
            }
        } else {
            if (moLastMajiangSeat != 0) {// 流局的话 最后摸的玩家为庄家
                setLastWinSeat(moLastMajiangSeat);
            }
        }
        calcAfter();
        saveLog(over, 0l, res.build());
        if (over) {
            calcOver1();
            calcOver2();
            calcOver3();
            diss();
        } else {
            initNext();
            calcOver1();
        }

        for (YjMjPlayer player : seatMap.values()) {
            if (player.isAutoPlaySelf()) {
                player.setAutoPlay(false, false);
            }
        }
        for (Player player : seatMap.values()) {
            player.saveBaseInfo();
        }
    }

    private boolean checkAuto3() {
        boolean diss = false;
        boolean diss2 = false;
        for (YjMjPlayer seat : seatMap.values()) {
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

    /**
     * 杠分以及一字撬有喜算分
     */
    private void calcExtraPoint(List<Integer> winSeats) {
        if (getYizhiqiao() > 0) {// 一字撬有喜算分
            for (int seat : seatMap.keySet()) {
                YjMjPlayer player = seatMap.get(seat);
                if (player.getHandPais().size() <= 2) {
                    for (int tempSeat : seatMap.keySet()) {
                        if (tempSeat != seat) {// 其他人给基础分
                            YjMjPlayer tempPlayer = seatMap.get(tempSeat);
                            tempPlayer.changeLostPoint(-YjMjHu.xiaoMengZiBasePoint * 2);
                        }
                    }
                    player.changeLostPoint(2 * YjMjHu.xiaoMengZiBasePoint * (maxPlayerCount - 1));
                }
            }
        }
        for (int seat : seatMap.keySet()) {// 杠算分： 明杠自己杠每人给基础分, 明杠别人打的牌 对方给2倍基础分, 暗杠每人给2倍基础分
            YjMjPlayer player = seatMap.get(seat);
            int aGangNum = player.getGangInfos()[0];// 0暗杠次数
            int mGangNum = player.getGangInfos()[1];// 1摸杠次数
            int jGangNum = player.getGangInfos()[2];// 2接杠次数
            int fGangNum = player.getGangInfos()[3];// 3放杠次数
            if (aGangNum > 0 || mGangNum > 0) {
                int gangWinPoint = 0;
                for (int tempSeat : seatMap.keySet()) {
                    if (seat != tempSeat) {
                        YjMjPlayer tempPlayer = seatMap.get(tempSeat);
                        int gangLossPoint = (aGangNum * 2 + mGangNum) * YjMjHu.xiaoMengZiBasePoint;
                        gangWinPoint += gangLossPoint;
                        tempPlayer.changeLostPoint(-gangLossPoint);
                    }
                }
                player.changeLostPoint(gangWinPoint);
            }
            if (jGangNum > 0) {
                player.changeLostPoint(jGangNum * 2 * YjMjHu.xiaoMengZiBasePoint);
            }
            if (fGangNum > 0) {
                player.changeLostPoint(-fGangNum * 2 * YjMjHu.xiaoMengZiBasePoint);
            }
        }
    }

    /**
     * 计算鸟分加成
     *
     * @param point
     * @param bird
     * @return
     */
    private int calcBirdPoint(int point, int bird) {
        if (bird > 0) {
            if (calcBird == 1) {// 加
                point = point * (bird + 1);
            } else if (calcBird == 2) {// 乘 其实是2的2次方
                point = (int) (point * (Math.pow(2, bird)));
            } else {// 加1
                point = point - bird;
            }
        }
        if (getFanshuLimit() > 0 && Math.abs(point) >= YjMjConstants.fanshuLimit) {
            return -YjMjConstants.fanshuLimit;
        }
        return point;
    }

    public void saveLog(boolean over, long winId, Object resObject) {
        LogUtil.d_msg("tableId:" + id + " play:" + playBureau + " over:" + resObject);
        YjClosingInfoRes res = (YjClosingInfoRes) resObject;
        String logRes = JacksonUtil.writeValueAsString(YjMjTool.buildClosingInfoResLog(res));
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
            userGroupLog.setDiFen(1 + "");
            userGroupLog.setOvertime(sdf.format(now));
            userGroupLog.setPlayercount(maxPlayerCount);
            String groupId = isGroupRoom() ? loadGroupId() : 0 + "";
            userGroupLog.setGroupid(Long.parseLong(groupId));
            userGroupLog.setGamename("王者麻将");
            userGroupLog.setTotalCount(totalBureau);
            if (playBureau == 1) {
                groupPaylogId = TableLogDao.getInstance().saveGroupPlayLog(userGroupLog);
            } else if (playBureau > 1 && groupPaylogId != 0) {
                userGroupLog.setId(groupPaylogId);
                TableLogDao.getInstance().updateGroupPlayLog(userGroupLog);
            }
        }
        saveTableRecord(logId, over, playBureau);
        for (YjMjPlayer player : playerMap.values()) {
            player.addRecord(logId, playBureau);
        }
        UdpLogger.getInstance().sendSnapshotLog(masterId, playLog, logRes);
    }

    /**
     * 中鸟个数
     *
     * @param seatBirdArr
     * @param seat
     * @return
     */
    private int calcBirdNum(int[] seatBirdArr, int seat) {
        if (seatBirdArr == null) {
            return 0;
        }
        int point = 0;
        for (int seatBird : seatBirdArr) {
            if (seat == seatBird) {
                point++;
            }
        }
        return point;
    }

    /**
     * 抓鸟
     *
     * @return
     */
    private void pickBird() {
        birdPaiList.clear();
        if (getLeftMajiangCount() > 0) {
            // 桌上还有剩余牌
            YjMj last = null;
            for (int i = 0; i < birdNum; i++) {
                YjMj birdMj = getLeftMajiang();
                if (birdMj != null) {
                    // 如果桌面上已经没有牌了拿桌面上最后一次摸的牌
                    last = birdMj;
                }
                birdPaiList.add(last);
            }
        } else {
            // 摸了海底牌
            for (int i = 0; i < this.birdNum; i++) {
                birdPaiList.add(this.getLastMajiang());
            }
        }
    }

    /**
     * 中鸟
     * 麻将算出座位
     *
     * @param winSeat
     * @return
     */
    private int[] birdToSeat(int winSeat) {
        int birdSize = birdPaiList.size();
        if (birdSize == 0) {
            return null;
        }
        int[] seatArr = new int[birdSize];
        for (int i = 0; i < birdSize; i++) {
            YjMj birdMj = birdPaiList.get(i);
            int birdMjPai = birdMj.getPai();
            if (getMaxPlayerCount() == 2) {
                // -----------沅江二人玩法，159中自己，26中对家--------------
                int loseSeat = winSeat == 1 ? 2 : 1;
                if (birdMjPai == 1 || birdMjPai == 5 || birdMjPai == 9) {
                    seatArr[i] = winSeat;
                } else if (birdMjPai == 2 || birdMjPai == 6) {
                    seatArr[i] = loseSeat;
                }
            } else {
                birdMjPai = (birdMjPai - 1) % 4;// 从自己开始算 所以减1
                int birdSeat = birdMjPai + winSeat > 4 ? birdMjPai + winSeat - 4 : birdMjPai + winSeat;
                seatArr[i] = birdSeat;
            }
        }
        return seatArr;
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
                tempMap.put("handPai1", StringUtil.implode(seatMap.get(1).getHandPais(), ","));
            }
            if (tempMap.containsKey("handPai2")) {
                tempMap.put("handPai2", StringUtil.implode(seatMap.get(2).getHandPais(), ","));
            }
            if (tempMap.containsKey("handPai3")) {
                tempMap.put("handPai3", StringUtil.implode(seatMap.get(3).getHandPais(), ","));
            }
            if (tempMap.containsKey("handPai4")) {
                tempMap.put("handPai4", StringUtil.implode(seatMap.get(4).getHandPais(), ","));
            }
            if (tempMap.containsKey("answerDiss")) {
                tempMap.put("answerDiss", buildDissInfo());
            }
            if (tempMap.containsKey("nowDisCardIds")) {
                tempMap.put("nowDisCardIds", StringUtil.implode(MajiangHelper.toMajiangIds(nowDisCardIds), ","));
            }
            if (tempMap.containsKey("leftPais")) {
                tempMap.put("leftPais", StringUtil.implode(MajiangHelper.toMajiangIds(leftMajiangs), ","));
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
    public int getPlayerCount() {
        return playerMap.size();
    }

    @Override
    protected void sendDealMsg() {
        sendDealMsg(0);
    }

    @Override
    protected void sendDealMsg(long userId) {// 天胡或者暗杠 报听
        boolean hasBaoTing = false;
        logFaPaiTable();
        DealInfoRes.Builder bankRes = null;
        for (YjMjPlayer tablePlayer : seatMap.values()) {
            DealInfoRes.Builder res = DealInfoRes.newBuilder();
            List<Integer> actionList = tablePlayer.checkMo(null, true, false);
            if (!actionList.isEmpty()) {
                if (actionList.get(YjMjConstants.ACTION_INDEX_BAOTING) == 1) {
                    hasBaoTing = true;
                }
                addActionSeat(tablePlayer.getSeat(), actionList);
                res.addAllSelfAct(actionList);
            }
            res.addAllHandCardIds(tablePlayer.getHandPais());
            res.setNextSeat(getNextDisCardSeat());
            res.setGameType(getWanFa());
            res.setRemain(leftMajiangs.size());
            res.setBanker(lastWinSeat);
            logFaPaiPlayer(tablePlayer, actionList);
            if (tablePlayer.getSeat() == lastWinSeat) {
                bankRes = res;
                continue;
            }
            tablePlayer.writeSocket(res.build());
            sendTingInfo(tablePlayer);
            if (tablePlayer.isAutoPlay()) {
                addPlayLog(getDisCardRound() + "_" + tablePlayer.getSeat() + "_" + YjMjDisAction.action_tuoguan + "_" + 1 + tablePlayer.getExtraPlayLog());
            }
        }
        YjMjPlayer bankPlayer = seatMap.get(lastWinSeat);
        bankRes.setBaoting(hasBaoTing ? 1 : 0);
        bankPlayer.writeSocket(bankRes.build());
        sendTingInfo(bankPlayer);
    }

    /**
     * 摸牌
     *
     * @param player
     * @param isGangMo 是否杠后摸牌
     */
    public void moMajiang(YjMjPlayer player, boolean isGangMo) {
        if (state != table_state.play) {
            return;
        }
        if (leftMajiangs.size() == 0) {
            calcOver();
            return;
        }
        if (leftMajiangs.size() <= maxPlayerCount) {
            setMoLastMajiangSeat(player.getSeat());
        }
        // 摸牌
        YjMj majiang = null;
        boolean isZp = false;
        if (disCardRound != 0) {
            if (player.isAlreadyMoMajiang()) {
                return;
            }
            if (GameServerConfig.isDeveloper() && !player.isRobot()) {
                if (zpMap.containsKey(player.getUserId()) && zpMap.get(player.getUserId()) > 0) {
                    majiang = MajiangHelper.findMajiangByVal(leftMajiangs, zpMap.get(player.getUserId()));
                    if (majiang != null) {
                        zpMap.remove(player.getUserId());
                        leftMajiangs.remove(majiang);
                        isZp = true;
                    } else {
                        if (leftMajiangs.size() <= maxPlayerCount) {
                            majiang = getLeftMajiang();
                            majiang = YjMj.getMajiangByValue(zpMap.get(player.getUserId()));
                            isZp = true;
                        }
                    }
                }
            }
            if (isZp == false) {
                majiang = getLeftMajiang();
            }
        }
        if (majiang != null) {
            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + YjMjDisAction.action_moMjiang + "_" + majiang.getId());
            player.moMajiang(majiang);
            if (leftMajiangs.size() < maxPlayerCount) {
                player.setHaidiMajiang(majiang.getId());
                setLastMajiang(majiang);
            }
        }
        // 检查摸牌
        clearActionSeatMap();
        if (disCardRound == 0) {
            return;
        }
        setMoMajiangSeat(player.getSeat());
        List<Integer> arr = player.checkMo(majiang, false, isGangMo);
        if (!arr.isEmpty()) {
            coverAddActionSeat(player.getSeat(), arr);
        }
        logMoMj(player, majiang, arr);
        MoMajiangRes.Builder res = MoMajiangRes.newBuilder();
        res.setUserId(player.getUserId() + "");
        res.setRemain(getLeftMajiangCount());
        res.setSeat(player.getSeat());
        boolean disMajiang = baotingSeat.containsKey(player.getSeat()) && arr.isEmpty() ? true : false;
        for (YjMjPlayer seat : seatMap.values()) {
            if (seat.getUserId() == player.getUserId()) {
                if (true) {
                    MoMajiangRes.Builder copy = res.clone();
                    copy.addAllSelfAct(arr);
                    if (majiang != null) {
                        copy.setMajiangId(majiang.getId());
                    }
                    seat.writeSocket(copy.build());
                }
            } else {
                seat.writeSocket(res.build());
            }
        }
        if (disMajiang && leftMajiangs.size() >= maxPlayerCount) {// 报听自动出牌
            List<YjMj> disMjiang = new ArrayList<>();
            disMjiang.add(majiang);
//            chuPai(player, disMjiang, 0);
        }
        sendTingInfo(player);
    }

    private void buildPlayRes(PlayMajiangRes.Builder builder, Player player, int action, List<YjMj> majiangs) {
        YjMjResTool.buildPlayRes(builder, player, action, majiangs);
        buildPlayRes1(builder);
    }

    private void buildPlayRes1(PlayMajiangRes.Builder builder) {
        // builder
    }

    /**
     * 玩家表示胡
     *
     * @param player
     * @param majiangs
     */
    private void hu(YjMjPlayer player, List<YjMj> majiangs, int action) {
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return;
        }
        if (huConfirmMap.containsKey(player.getSeat())) {
            return;
        }
        boolean zimo = player.isAlreadyMoMajiang();// 是否自摸
        YjMj disMajiang = null;
        if (!zimo) { // 不是自摸
            if (moGangHuList.contains(player.getSeat())) {
                disMajiang = moGang;// 强杠胡
            } else {
                if (!nowDisCardIds.isEmpty()) {
                    disMajiang = nowDisCardIds.get(0);
                }
            }
        }
        if (lastMajiang != null && moGangHuList.contains(player.getSeat())) {
            disMajiang = moGang;// 强杠胡
            zimo = false;
        }

        YjMjHu huBean = player.checkHu(disMajiang, nowDisCardIds == null || nowDisCardIds.isEmpty() ? true : false);
        if (actionSeatMap.get(player.getSeat()).get(5) == 1) {// 杠爆胡
            huBean.setGangBao(true);
            huBean.initDahuList();
        }
        if (huBean.isHu() && lastMajiang != null) {
            huBean.setHaidilao(true);
            huBean.initDahuList();
        }
        if (!huBean.isHu()) {
            return;
        }
        int fromSeat = 0;
        if (moGangHuList.contains(player.getSeat())) { // 抢杠胡算牌型分
            if (disEventAction == YjMjDisAction.action_minggang) {// 抢杠胡
                huBean.setQiangGangHu(true);
                if (huBean.isHaidilao()) {// 抢杠胡没有海底捞
                    huBean.setHaidilao(false);
                }
                huBean.initDahuList();
            }
            YjMjPlayer moGangPlayer = getPlayerByHasMajiang(moGang);
            if (moGangPlayer == null) {
                moGangPlayer = seatMap.get(moMajiangSeat);
            }
            List<YjMj> moGangMajiangs = new ArrayList<>();
            moGangMajiangs.add(moGang);
            moGangPlayer.addOutPais(moGangMajiangs, -1, 0);// 摸杠被人胡了 相当于自己出了一张牌
            PlayMajiangRes.Builder disBuilder = PlayMajiangRes.newBuilder();// 打出杠的牌
            buildPlayRes(disBuilder, moGangPlayer, 0, moGangMajiangs);
            fromSeat = moGangPlayer.getSeat();
            sendDisMajiangAction(disBuilder);
            recordDisMajiang(moGangMajiangs, moGangPlayer);
            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + 0 + "_" + MajiangHelper.toMajiangStrs(majiangs));
        }
        if (baotingSeat.containsKey(player.getSeat())) { // 报听算大胡
            if (huBean.isQiangGangHu() && baotingSeat.get(player.getSeat()) == 1) {// 玩家胡强杠胡时 如果报听并且有牌没胡  则不算报听门子
                huBean.setBaoting(false);
            } else {
                huBean.setBaoting(true);
            }
            huBean.initDahuList();
        }
        if (huBean.getDahuPoint() > 0) { // 大胡
            player.setDahu(huBean.getDahuList());
            if (zimo) { // 自摸
                int point = 0;
                for (YjMjPlayer seatPlayer : seatMap.values()) {
                    if (seatPlayer.getSeat() != player.getSeat()) {
                        int dahuPoint = huBean.getDahuPoint();
                        if (getFanshuLimit() > 0 && dahuPoint > YjMjConstants.fanshuLimit) {// 有番数上限
                            dahuPoint = YjMjConstants.fanshuLimit;
                        }
                        point += dahuPoint;
                        seatPlayer.changeLostPoint(-huBean.getDahuPoint());
                    }
                }
                player.changeLostPoint(point);
            } else {
                player.changeLostPoint(huBean.getDahuPoint());
                seatMap.get(disCardSeat).changeLostPoint(-huBean.getDahuPoint());
            }
        } else { // 小胡自摸
            if (zimo) {
                int point = 0;
                for (YjMjPlayer seatPlayer : seatMap.values()) {
                    if (seatPlayer.getSeat() != player.getSeat()) {
                        point += YjMjHu.xiaoMengZiBasePoint;
                        seatPlayer.changeLostPoint(-YjMjHu.xiaoMengZiBasePoint);
                    }
                }
                player.changeLostPoint(point);
            }
        }
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        buildPlayRes(builder, player, action, huBean.getShowMajiangs());
        if (fromSeat > 0) {
            builder.setFromSeat(fromSeat);
        }
        builder.addAllHuArray(player.getDahu());
        if (zimo) {
            builder.setZimo(1);
        }
        for (YjMjPlayer seat : seatMap.values()) {// 推送胡消息
            seat.writeSocket(builder.build());
        }
        logActionHu(player, majiangs);
        addHuList(player.getSeat(), disMajiang == null ? 0 : disMajiang.getId());// 加入胡牌数组
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_");
        if (isCalcOver()) { // 等待别人胡牌 如果都确认完了，胡
            calcOver();
        }
    }

    /**
     * 找出拥有这张麻将的玩家
     *
     * @param majiang
     * @return
     */
    private YjMjPlayer getPlayerByHasMajiang(YjMj majiang) {
        for (YjMjPlayer player : seatMap.values()) {
            if (player.getmGang() != null && player.getmGang().contains(majiang)) {
                return player;
            }
        }
        for (YjMjPlayer player : seatMap.values()) {
            if (player.getHandMajiang() != null && player.getHandMajiang().contains(majiang)) {
                return player;
            }
            if (player.getOutMajing() != null && player.getOutMajing().contains(majiang)) {
                return player;
            }
        }
        return null;
    }

    private boolean isCalcOver() {
        List<Integer> huActionList = getHuSeatByActionMap();
        boolean over = false;
        // 起牌小胡可以继续打牌
        if (!huActionList.isEmpty()) {
            over = true;
            YjMjPlayer moGangPlayer = null;
            if (!moGangHuList.isEmpty()) {
                // 如果有抢杠胡
                moGangPlayer = getPlayerByHasMajiang(moGang);
                LogUtil.monitor_i("mogang player:" + moGangPlayer.getSeat() + " moGang:" + moGang);
            }
            for (int huseat : huActionList) {
                if (moGangPlayer != null) {
                    // 被抢杠的人可以胡的话 跳过
                    if (moGangPlayer.getSeat() == huseat) {
                        continue;
                    }
                }
                if (!huConfirmMap.containsKey(huseat)) {
                    over = false;
                    break;
                }
            }
        }

        if (!over) {
            YjMjPlayer disYJMajiangPlayer = seatMap.get(disCardSeat);
            for (int huseat : huActionList) {
                if (huConfirmMap.containsKey(huseat)) {
                    if (nowDisCardIds == null || nowDisCardIds.isEmpty() ? true : false) {// 天胡
                        removeActionSeat(huseat);
                    }
                    continue;
                }
                PlayMajiangRes.Builder disBuilder = PlayMajiangRes.newBuilder();
                YjMjPlayer seatPlayer = seatMap.get(huseat);
                buildPlayRes(disBuilder, disYJMajiangPlayer, 0, null);
                List<Integer> actionList = actionSeatMap.get(huseat);
                disBuilder.addAllSelfAct(actionList);
                seatPlayer.writeSocket(disBuilder.build());
            }
        }
        return over;
    }

    // private boolean isCalcOver() {
    // return isCalcOver(null);
    // }

    /**
     * 碰杠
     *
     * @param player
     * @param mjs    碰的牌 或 杠的牌
     * @param action
     */
    private void chiPengGang(YjMjPlayer player, List<YjMj> mjs, int action) {
        logAction(player, action, mjs, null);

        if (mjs == null || mjs.size() == 0) {
            LogUtil.msgLog.info("YjMj|chiPengGang|error|" + getId() + "|" + player.getUserId() + "|" + player.getUserId() + "|" + action + "|" + player.getHandMajiang());
            return;
        }

        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        List<Integer> huList = getHuSeatByActionMap();
        huList.remove((Object) player.getSeat());//删除玩家action
        if (!huList.isEmpty()) {// 还有其他玩家胡  应该优先让其他玩家胡
            return;
        }
        if (!checkAction(player, action)) {
            return;
        }

        List<YjMj> handMajiang = new ArrayList<>(player.getHandMajiang());
        YjMj disMajiang = null;
        if (isHasGangAction()) {// 有杠的操作
            List<Integer> majiangIds = MajiangHelper.toMajiangIds(mjs);
            for (int majiangId : gangSeatMap.keySet()) {
                if (majiangIds.contains(majiangId)) {
                    disMajiang = YjMj.getMajang(majiangId);
                    handMajiang.add(disMajiang);
                    if (mjs.size() > 1) {
                        mjs.remove(disMajiang);
                    }
                    break;
                }
            }
            if (disMajiang == null) {// 不存在杠的麻将
                return;
            }
        } else {
            if (!nowDisCardIds.isEmpty()) {
                disMajiang = nowDisCardIds.get(0);
            }
        }
        int sameCount = 0;
        if (mjs.size() > 0) {
            sameCount = MajiangHelper.getMajiangCount(mjs, mjs.get(0).getVal());
        }
        if (action == YjMjDisAction.action_jiegang) { // 接杠
            mjs = MajiangHelper.getMajiangList(handMajiang, mjs.get(0).getVal());
            sameCount = mjs.size();
        } else if (action == YjMjDisAction.action_minggang || action == YjMjDisAction.action_angang) {// 如果是杠 后台来找出是明杠还是暗杠
            mjs = MajiangHelper.getMajiangList(handMajiang, mjs.get(0).getVal());
            sameCount = mjs.size();
            if (sameCount == 4) {// 有4张一样的牌是暗杠
                action = YjMjDisAction.action_angang;
            }
        }
        // 碰 杠验证
        boolean hasQGangHu = false;
        if (action == YjMjDisAction.action_peng) {
            int curSize = MajiangHelper.getMajiangList(handMajiang, mjs.get(0).getVal()).size();
            if (curSize == 3) {// 可以杠的牌选择了碰 以后不能再杠了
                player.removeUncheckmGang(mjs.get(0).getVal());
            }
            sameCount = 2;
            boolean can = canPeng(player, mjs, sameCount, disMajiang);
            if (!can) {
                return;
            }
        } else if (action == YjMjDisAction.action_angang) {
            boolean can = canAnGang(player, mjs, sameCount);
            if (!can) {
                return;
            }
            player.updateGangInfo(0, 1);
        } else if (action == YjMjDisAction.action_minggang) {
            boolean can = canMingGang(player, handMajiang, mjs, sameCount, disMajiang);
            if (!can) {
                return;
            }
            //不能杠的牌
            if (!player.getUncheckmGangs().contains(mjs.get(0).getVal())) {
            	return;
            }
            
            if (sameCount == 1 && canGangHu()) {// 特殊处理一张牌明杠的时候别人可以胡
                if (checkQGangHu(player, mjs, action)) {
                    hasQGangHu = true;
                    LogUtil.monitor_i("有玩家可抢杠胡！！");
                }
            }
            if (!hasQGangHu)
                player.updateGangInfo(1, 1);
        } else if (action == YjMjDisAction.action_jiegang) {
            boolean can = canMingGang(player, handMajiang, mjs, sameCount, disMajiang);
            if (!can) {
                return;
            }
            if (disCardSeat != player.getSeat()) {
                YjMjPlayer disPlayer = this.seatMap.get(disCardSeat);
                disPlayer.updateGangInfo(3, 1);
                player.updateGangInfo(2, 1);
            }
        } else {
            return;
        }
        calcPoint(player, action, sameCount, mjs);
        boolean disMajiangMove = false;
        if (disMajiang != null) {// 碰或者杠
            if ((action != YjMjDisAction.action_minggang && action != YjMjDisAction.action_angang && action != YjMjDisAction.action_jiegang)
                    || (action == YjMjDisAction.action_minggang && sameCount != 1) || (action == YjMjDisAction.action_jiegang && sameCount != 1 && sameCount != 4)) {
                disMajiangMove = true;
            }
        }
        if (disMajiangMove) {
            if (action == YjMjDisAction.action_chi) {
                // 吃的牌放第二位
                mjs.add(1, disMajiang);
            } else {
                mjs.add(disMajiang);
            }
            builder.setFromSeat(disCardSeat);
            List<YjMj> disMajiangs = new ArrayList<>();
            disMajiangs.add(disMajiang);
            seatMap.get(disCardSeat).removeOutPais(disMajiangs, action);
        }
        chiPengGang(builder, player, mjs, action, hasQGangHu);
    }

    private void chiPengGang(PlayMajiangRes.Builder builder, YjMjPlayer player, List<YjMj> majiangs, int action, boolean hasQGangHu) {
        if (action == YjMjDisAction.action_peng) {// 杠碰的牌特殊标记action
            int curSize = MajiangHelper.getMajiangList(player.getHandMajiang(), majiangs.get(0).getVal()).size();
            if (curSize == 3) {
                action = YjMjDisAction.action_gangPeng;
            }
        }
        player.addOutPais(majiangs, action, disCardSeat);
        buildPlayRes(builder, player, action, majiangs);
        removeActionSeat(player.getSeat());
        clearGangActionMap();
        if (!hasQGangHu) {
            clearActionSeatMap();
        }
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + MajiangHelper.toMajiangStrs(majiangs));
        // 不是普通出牌
        setNowYjDisCardSeat(player.getSeat(), false);
        if (action == YjMjDisAction.action_peng || action == YjMjDisAction.action_gangPeng) {// 碰后可杠
            List<Integer> arr = player.checkMo(null, false, false);
            if (!arr.isEmpty()) {
                addActionSeat(player.getSeat(), arr);
            }
        }
        for (YjMjPlayer seatPlayer : seatMap.values()) {
            // 推送消息
            PlayMajiangRes.Builder copy = builder.clone();
            if (actionSeatMap.containsKey(seatPlayer.getSeat())) {
                copy.addAllSelfAct(actionSeatMap.get(seatPlayer.getSeat()));
            }
            seatPlayer.writeSocket(copy.build());
        }
        if (action == YjMjDisAction.action_chi || action == YjMjDisAction.action_peng) {
            sendTingInfo(player);
        }
        player.setPassMajiangVal(0);// 取消漏炮
        if (action == YjMjDisAction.action_minggang || action == YjMjDisAction.action_angang || action == YjMjDisAction.action_jiegang) {
            if (!hasQGangHu) {// 如果有玩家抢杠胡暂时不补张
                if (getLeftMajiangCount() > maxPlayerCount)// 海底捞四张牌 不进行补张
                    moMajiang(player, true);
                else {// 海底捞时 暗杠不补张  由下家直接摸牌
                    Map<Integer, Integer> handMap = MajiangHelper.toMajiangValMap(player.getHandMajiang());
                    if (handMap.containsValue(4)) {// 如果还能暗杠 则继续暗杠
                        List<Integer> list = new ArrayList<>();
                        int[] arr = new int[7];
                        arr[3] = 1;
                        for (int val : arr) {
                            list.add(val);
                        }
                        addActionSeat(player.getSeat(), list);
                        logAction(player, action, majiangs, list);
                        PlayMajiangRes.Builder anGangBuilder = PlayMajiangRes.newBuilder();
                        anGangBuilder.setFromSeat(disCardSeat);
                        buildPlayRes(anGangBuilder, player, YjMjDisAction.action_haidi_angang, null);
                        anGangBuilder.addAllSelfAct(actionSeatMap.get(player.getSeat()));
                        player.writeSocket(anGangBuilder.build());
                    } else {
                        if (getLeftMajiangCount() < maxPlayerCount) {
                            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + YjMjDisAction.action_haodilaoPass + "_");
                            PlayMajiangRes.Builder haidilaoGuoBuilder = PlayMajiangRes.newBuilder();
                            buildPlayRes(haidilaoGuoBuilder, player, YjMjDisAction.action_haodilaoPass, null);
                            for (YjMjPlayer seat : seatMap.values()) {
                                seat.writeSocket(haidilaoGuoBuilder.build());
                            }
                        }
                        setNowYjDisCardSeat(player.getSeat(), true);
                        YjMjPlayer next = seatMap.get(nowDisCardSeat);
                        moMajiang(next, false);// 杠后不补张由下家摸牌
                    }
                }
            }
        }
        setDisEventAction(action);
        robotDealAction();
    }

    /**
     * 是否抢杠胡
     *
     * @param gangPlayer
     * @param majiangs
     * @param action
     * @return
     */
    private boolean checkQGangHu(YjMjPlayer gangPlayer, List<YjMj> majiangs, int action) {
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        Map<Integer, List<Integer>> huMap = new HashMap<>();
        for (YjMjPlayer player : seatMap.values()) {
            if (player.getUserId() == gangPlayer.getUserId()) {
                continue;
            }
            // 推送消息
            List<Integer> actList = player.checkDisMj(majiangs.get(0), true);
            if (!actList.isEmpty() && actList.get(YjMjConstants.ACTION_INDEX_HU) == 1) {
                addActionSeat(player.getSeat(), actList);
                huMap.put(player.getSeat(), actList);
                logQiangGangHu(player, majiangs, actList);
            }
        }
        if (huMap.isEmpty()) {
            return false;
        }
        // 可以胡牌
        // 明杠玩家先直接杠 但不算分 只有当抢杠的玩家多选择过才算杠分
        setDisEventAction(action);
        setMoGang(majiangs.get(0), new ArrayList<>(huMap.keySet()));
        buildPlayRes(builder, gangPlayer, action, majiangs);
        for (Entry<Integer, List<Integer>> entry : huMap.entrySet()) {// 通知玩家可抢杠胡
            PlayMajiangRes.Builder copy = builder.clone();
            YjMjPlayer seatPlayer = seatMap.get(entry.getKey());
            copy.addAllSelfAct(entry.getValue());
            seatPlayer.writeSocket(copy.build());
            LogUtil.monitor_i("可抢杠胡的玩家名:" + seatPlayer.getName() + "座位号:" + entry.getKey());
        }
        return true;

    }

    public void checkSendGangRes(Player player) {
        if (isHasGangAction(player.getSeat()) && actionSeatMap.containsKey(player.getSeat())) {
            YjMjPlayer disPlayer = seatMap.get(disCardSeat);
            GangMoMajiangRes.Builder gangbuilder = GangMoMajiangRes.newBuilder();
            gangbuilder.setGangId(gangMajiang.getId());
            gangbuilder.setUserId(disPlayer.getUserId() + "");
            gangbuilder.setName(disPlayer.getName() + "");
            gangbuilder.setSeat(disPlayer.getSeat());
            gangbuilder.setRemain(leftMajiangs.size());
            gangbuilder.setReconnect(1);

            for (int majiangId : gangSeatMap.keySet()) {
                GangPlayMajiangRes.Builder playBuilder = GangPlayMajiangRes.newBuilder();
                playBuilder.setMajiangId(majiangId);
                Map<Integer, List<Integer>> seatActionList = gangSeatMap.get(majiangId);
                if (seatActionList == null) {
                    continue;
                }
                if (seatActionList.containsKey(player.getSeat())) {
                    playBuilder.addAllSelfAct(seatActionList.get(player.getSeat()));
                }
                gangbuilder.addGangActs(playBuilder);
            }
            player.writeSocket(gangbuilder.build());
        }
    }

    /**
     * 普通出牌
     *
     * @param player
     * @param majiangs
     * @param action
     */
    private void chuPai(YjMjPlayer player, List<YjMj> majiangs, int action) {
        if (majiangs.size() != 1) {
            return;
        }
        if (!player.isAlreadyMoMajiang()) {
            // 还没有摸牌
            LogUtil.errorLog.error("玩家还没有摸牌：" + player.getUserId());
            return;
        }
        if (!actionSeatMap.isEmpty()) {
            return;
        }
        if (getBaotingSeat().containsKey(player.getSeat())) { // 报听玩家只允许出最后摸的牌
            if (majiangs.get(0).getId() != player.getHandMajiang().get(player.getHandMajiang().size() - 1).getId()) {
                return;
            }
        }
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        buildPlayRes(builder, player, action, majiangs);
        clearActionSeatMap();
        clearGangActionMap();
        setNowYjDisCardSeat(player.getSeat(), true);
        recordDisMajiang(majiangs, player);
        player.addOutPais(majiangs, action, 0);

        // 庄家出第一张牌时，检查报听
        if (player.getSeat() == getLastWinSeat()
                && hadNotMoMj()
                && player.getHandPais().size() >= 13
                && player.isTingPai(0)) {

            int[] arr = new int[YjMjConstants.ACTION_INDEX_SIZE];
            arr[YjMjConstants.ACTION_INDEX_BAOTING] = 1;
            List<Integer> list = new ArrayList<>();
            for (int val : arr) {
                list.add(val);
            }
            addActionSeat(player.getSeat(), list);
            setDisEventAction(action);

            builder.addAllSelfAct(list);
            player.writeSocket(builder.build());
            logAction(player, action, majiangs, list);
            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + MajiangHelper.toMajiangStrs(majiangs));
            return;
        }

        logAction(player, action, majiangs, null);

        // 普通出牌
        for (YjMjPlayer seat : seatMap.values()) {
            if (seat.getUserId() == player.getUserId()) {
                continue;
            }
            List<Integer> list = seat.checkDisMj(majiangs.get(0), false);
            if (list.contains(1)) {
                addActionSeat(seat.getSeat(), list);
                logChuPaiActList(seat, majiangs.get(0), list);
            }
        }

        setDisEventAction(action);
        sendDisMajiangAction(builder);
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + MajiangHelper.toMajiangStrs(majiangs));
        checkMo();// 给下一家发牌
    }

    /**
     * 没人摸过麻将
     *
     * @return
     */
    public boolean hadNotMoMj() {
        return getLeftMajiangCount() == (108 - (maxPlayerCount * 13 + 1 + chouPai));
    }

    public List<Integer> getPengGangSeatByActionMap() {
        List<Integer> huList = new ArrayList<>();
        for (int seat : actionSeatMap.keySet()) {
            List<Integer> actionList = actionSeatMap.get(seat);
            if (actionList.get(0) == 3) {
                // 胡
                huList.add(seat);
            }

        }
        return huList;
    }

    //胡action列表 （可能多个玩家同时胡）
    public List<Integer> getHuSeatByActionMap() {
        List<Integer> huList = new ArrayList<>();
        for (int seat : actionSeatMap.keySet()) {
            List<Integer> actionList = actionSeatMap.get(seat);
            if (actionList.get(YjMjConstants.ACTION_INDEX_HU) == 1) {// 胡
                huList.add(seat);
            }
        }
        return huList;
    }

    /**
     * 向客户端推送 玩家可做的操作
     *
     * @param builder
     */
    private void sendDisMajiangAction(PlayMajiangRes.Builder builder) {
        // 如果有人可以胡 优先胡
        // 把胡的找出来
        buildPlayRes1(builder);
        List<Integer> huList = getHuSeatByActionMap();
        if (huList.size() > 0) {
            // 有人胡,优先胡
            for (YjMjPlayer seatPlayer : seatMap.values()) {
                PlayMajiangRes.Builder copy = builder.clone();
                List<Integer> actionList;
                // 只推送给胡牌的人改成了推送给所有人但是必须等胡牌的人先答复
                if (actionSeatMap.containsKey(seatPlayer.getSeat())) {
                    actionList = actionSeatMap.get(seatPlayer.getSeat());
                } else {// 其他碰杠先无视
                    actionList = new ArrayList<>();
                }
                copy.addAllSelfAct(actionList);
                seatPlayer.writeSocket(copy.build());
            }

        } else {
            // 没人胡，推送普通碰杠
            for (YjMjPlayer seat : seatMap.values()) {
                PlayMajiangRes.Builder copy = builder.clone();
                List<Integer> actionList;
                if (actionSeatMap.containsKey(seat.getSeat())) {
                    actionList = actionSeatMap.get(seat.getSeat());
                } else {
                    actionList = new ArrayList<>();
                }
                copy.addAllSelfAct(actionList);
                seat.writeSocket(copy.build());
            }
        }

    }

    private void err(YjMjPlayer player, int action, String errMsg) {
        LogUtil.e("play:tableId-->" + id + " playerId-->" + player.getUserId() + " action-->" + action + " err:" + errMsg);
    }

    /**
     * 出牌
     *
     * @param player
     * @param majiangs
     * @param action
     */
    public synchronized void playCommand(YjMjPlayer player, List<YjMj> majiangs, int action) {
        // 被人抢杠胡
        if (!moGangHuList.isEmpty()) {
            if (!moGangHuList.contains(player.getSeat())) {
                return;
            }
        }

        if (YjMjDisAction.action_hu == action) {
            hu(player, majiangs, action);
            return;
        }

        if (getLeftMajiangCount() == (108 - (maxPlayerCount * 13 + 1 + birdNum)) && lastWinSeat == player.getSeat()) {// 庄家起手时如果有闲家报听  庄家除了能胡以外不能做其他操作
            for (int seat : actionSeatMap.keySet()) {
                if (seat != player.getSeat() && actionSeatMap.containsKey(seat) && actionSeatMap.get(seat).get(6) == 1) {
                    return;
                }
            }
        }

        // 手上没有要出的麻将
        if (!isHasGangAction() && action != YjMjDisAction.action_minggang && action != YjMjDisAction.action_jiegang && action != YjMjDisAction.action_peng)
            if (majiangs == null || !player.getHandMajiang().containsAll(majiangs)) {
                err(player, action, "没有找到出的牌" + majiangs);
                return;
            }
        changeDisCardRound(1);
        if (action == YjMjDisAction.action_pass) {
            guo(player, majiangs, action);
        } else if (action == YjMjDisAction.action_haodilaoPass) {//海底捞玩家过牌 不打牌
            moLast4Majiang(player, YjMjDisAction.action_haodilaoPass);
        } else if (action == YjMjDisAction.action_baoting) {// 玩家报听
            baoTing(player, action);
        } else if (action == YjMjDisAction.action_moMjiang) {
        } else if (action != 0) {
            chiPengGang(player, majiangs, action);
        } else {
            chuPai(player, majiangs, action);
        }

    }

    /**
     * 海底捞最后四张 暂时未处理
     *
     * @param player
     * @param action
     */
    public synchronized void moLast4Majiang(YjMjPlayer player, int action) {
        if (!moGangHuList.isEmpty() && moGangHuList.contains(player.getSeat())) {// 海底捞时有摸杠胡可胡 选择过
            List<YjMj> majiangs = new ArrayList<>();
            majiangs.add(moGang);
            passMoHu(player, majiangs, action);
            return;
        }
        List<Integer> actList = removeActionSeat(player.getSeat());
        logAction(player, action, null, actList);
        if (!actionSeatMap.isEmpty()) {
            return;
        }
        if (state != table_state.play) {
            return;
        }
        if (getDisCardRound() <= 0) {
            return;
        }
        if (action == YjMjDisAction.action_haodilaoPass) {
            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + YjMjDisAction.action_haodilaoPass + "_");

            PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
            buildPlayRes(builder, player, action, null);
            for (YjMjPlayer seat : seatMap.values()) {
                seat.writeSocket(builder.build());
            }
            if (getLeftMajiangCount() == 0) {
                calcOver();
                return;
            }
            YjMjPlayer next = seatMap.get(calcNextSeat(player.getSeat()));
            if (next.isAlreadyMoMajiang()) {// 下家已摸牌 直接忽略
                return;
            }
            setNowYjDisCardSeat(moLastMajiangSeat, true);
            next = seatMap.get(nowDisCardSeat);
            moMajiang(next, false);
            robotDealAction();
        }
    }

    /**
     * 闲家报听完毕 通知庄家发牌
     *
     * @param leisurePlayer
     */
    public synchronized boolean leisureBaotingfinish(YjMjPlayer leisurePlayer) {
        for (int actionSeat : actionSeatMap.keySet()) {
            if (actionSeat != lastWinSeat && actionSeatMap.get(actionSeat).get(YjMjConstants.ACTION_INDEX_BAOTING) == 1) {// 闲家还没有报听完毕
                return false;
            }
        }
        // 闲家报听完毕通知庄家发牌
        Player bankPlayer = seatMap.get(lastWinSeat);
        ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_ask_dismajiang);
        bankPlayer.writeSocket(com.build());
        return true;
    }

    /**
     * 玩家起手报听
     *
     * @param player
     * @param action
     */
    public synchronized void baoTing(YjMjPlayer player, int action) {
        if (actionSeatMap.isEmpty() || !actionSeatMap.containsKey(player.getSeat()) || actionSeatMap.get(player.getSeat()).get(YjMjConstants.ACTION_INDEX_BAOTING) == 0) {// 玩家是否能报听
            return;
        }
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        buildPlayRes(builder, player, action, null);
        builder.setSeat(player.getSeat());
        for (YjMjPlayer seatPlayer : seatMap.values()) {// 推送报听消息给前端
            seatPlayer.writeSocket(builder.build());
        }
        removeActionSeat(player.getSeat());
        addBaotingSeat(player.getSeat());// 记录报听的玩家座位
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + YjMjDisAction.action_baoting + "_");
        logAction(player, action, null, null);
        if (player.getSeat() != lastWinSeat) {
            leisureBaotingfinish(player);
            return;
        }
        // 普通出牌
        PlayMajiangRes.Builder builder2 = PlayMajiangRes.newBuilder();
        buildPlayRes(builder2, player, 0, nowDisCardIds);
        builder2.setSeat(player.getSeat());
        for (YjMjPlayer seat : seatMap.values()) {
            if (seat.getUserId() == player.getUserId()) {
                continue;
            }
            List<Integer> list = seat.checkDisMj(nowDisCardIds.get(0), false);
            if (list.contains(1)) {
                addActionSeat(seat.getSeat(), list);
            }
        }
        setDisEventAction(action);
        sendDisMajiangAction(builder2);
        player.setPassMajiangVal(0);// 取消漏炮
        setNowYjDisCardSeat(player.getSeat(), true);
//		addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + MajiangHelper.toMajiangStrs(nowDisCardIds));
        checkMo();// 给下一家发牌
    }

    private void passMoHu(YjMjPlayer player, List<YjMj> majiangs, int action) {
        if (!moGangHuList.contains(player.getSeat())) {
            return;
        }
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        buildPlayRes(builder, player, action, majiangs);
        builder.setSeat(nowDisCardSeat);
        removeActionSeat(player.getSeat());
        player.writeSocket(builder.build());
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + MajiangHelper.toMajiangStrs(majiangs));
        if (isCalcOver()) {
            calcOver();
            return;
        }
        player.setPassMajiangVal(nowDisCardIds.get(0).getVal());
        YjMjPlayer moGangPlayer = seatMap.get(moMajiangSeat);
        if (moGangHuList.isEmpty()) {
            moGangPlayer.updateGangInfo(1, 1);// 全部过则算分
            majiangs = new ArrayList<>();
            majiangs.add(moGang);
            calcPoint(moGangPlayer, YjMjDisAction.action_minggang, 1, majiangs);
            builder = PlayMajiangRes.newBuilder();
            clearMoGang();// 清除摸杠胡记录
            // 直接通知下家摸牌
            if (getLeftMajiangCount() >= maxPlayerCount) // 玩家直接补张出牌
                moMajiang(moGangPlayer, true);
            else {// 海底捞时 明杠 暗杠不补张  由下家直接摸牌  海底捞四张牌 不进行补张
                setNowYjDisCardSeat(moGangPlayer.getSeat(), true);
                YjMjPlayer next = seatMap.get(nowDisCardSeat);
                moMajiang(next, true);
            }
            //chiPengGang(builder, moGangPlayer, majiangs, YjMjDisAction.action_minggang);
        }
    }

    /**
     * pass
     *
     * @param player
     * @param majiangs
     * @param action
     */
    private void guo(YjMjPlayer player, List<YjMj> majiangs, int action) {
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return;
        }
        if (!moGangHuList.isEmpty()) {// 有摸杠胡的优先处理
            passMoHu(player, majiangs, action);
            return;
        }
        if (actionSeatMap.get(player.getSeat()).get(2) == 1) {// 可以明杠
            Map<Integer, Integer> pengMap = MajiangHelper.toMajiangValMap(player.getPeng());
            for (YjMj handMajiang : player.getHandMajiang()) {// 手上就有一张杠牌
                if (pengMap.containsKey(handMajiang.getVal())) {
                    if (player.getUncheckmGangs().contains(handMajiang.getVal())) {
                        player.removeUncheckmGang(handMajiang.getVal());// 清除明杠状态  不能再明杠了
                    }
                }
            }
        }
        if (actionSeatMap.get(player.getSeat()).get(0) == 1) {// 可以胡
            if (baotingSeat.containsKey(player.getSeat()) && baotingSeat.get(player.getSeat()) == 0) {// 报听后如果第一个炮没胡 之后就不能接炮
                baotingSeat.put(player.getSeat(), 1);
            }
            if (nowDisCardSeat != player.getSeat()) {
                addHuPassSeat(player.getSeat());
            }
        }
        if (actionSeatMap.get(player.getSeat()).get(1) == 1) {// 可以碰
            if (nowDisCardSeat != player.getSeat()) {
                addPengPassSeat(player.getSeat(), nowDisCardIds.get(0).getVal());
            }
        }
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        buildPlayRes(builder, player, action, majiangs);
        builder.setSeat(nowDisCardSeat);
        player.writeSocket(builder.build());
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + MajiangHelper.toMajiangStrs(majiangs));

        List<Integer> removeActionList = removeActionSeat(player.getSeat());
        logAction(player, action, majiangs, removeActionList);

        if (removeActionList != null && removeActionList.get(YjMjConstants.ACTION_INDEX_BAOTING) == 1) {// 报听选择过
            if (player.getSeat() != lastWinSeat) {// 闲家报听选择过
                leisureBaotingfinish(player);
                return;
            } else {// 庄家报听选择过 第一张出的牌推送给其他玩家
                PlayMajiangRes.Builder builder2 = PlayMajiangRes.newBuilder();
                buildPlayRes(builder2, player, YjMjDisAction.action_chupai, nowDisCardIds);
                builder2.setSeat(player.getSeat());
                for (YjMjPlayer seat : seatMap.values()) {
                    if (seat.getUserId() == player.getUserId()) {
                        continue;
                    }
                    List<Integer> list = seat.checkDisMj(nowDisCardIds.get(0), false);
                    if (list.contains(1)) {
                        addActionSeat(seat.getSeat(), list);
                        logChuPaiActList(seat, nowDisCardIds.get(0), list);
                    }
                }
                if (!actionSeatMap.isEmpty()) {
                    sendDisMajiangAction(builder2);
                    return;
                }
            }
        }

        if (isCalcOver()) {
            calcOver();
            return;
        }

        // 漏炮
        if (removeActionList != null && removeActionList.get(YjMjConstants.ACTION_INDEX_HU) == 1) {
            if (player.isAlreadyMoMajiang()) {
                // 自摸
                player.setPassMajiangVal(player.getHandMajiang().get(player.getHandMajiang().size() - 1).getVal());
            } else if (disCardSeat != player.getSeat() && nowDisCardIds.size() == 1) {
                // 放炮
                player.setPassMajiangVal(nowDisCardIds.get(0).getVal());
            }
        }

        if (!actionSeatMap.isEmpty()) {
            YjMjPlayer disPlayer = seatMap.get(disCardSeat);
            PlayMajiangRes.Builder disBuilder = PlayMajiangRes.newBuilder();
            buildPlayRes(disBuilder, disPlayer, 0, null);
            for (int seat : actionSeatMap.keySet()) {
                List<Integer> actionList = actionSeatMap.get(seat);
                if (actionList.get(YjMjConstants.ACTION_INDEX_HU) == 1) {// 胡牌略过，因为胡牌消息在出牌的时候已经推送过了
                    continue;
                }
                if (isHasGangAction(seat)) {
                    continue;
                }
                PlayMajiangRes.Builder copy = disBuilder.clone();
                copy.addAllSelfAct(actionList);
                YjMjPlayer seatPlayer = seatMap.get(seat);
                seatPlayer.writeSocket(copy.build());
            }
        }
        if (player.isAlreadyMoMajiang()) {
            sendTingInfo(player);
        }
        checkMo();
    }

    private void calcPoint(YjMjPlayer player, int action, int sameCount, List<YjMj> majiangs) {
        if (sameCount == 3) {
            YjMjPlayer disPlayer = seatMap.get(disCardSeat);
            disPlayer.getMyExtend().setMjFengshen(FirstmythConstants.firstmyth_index13, 1);
        }
    }

    private void recordDisMajiang(List<YjMj> majiangs, YjMjPlayer player) {
        setNowDisCardIds(majiangs);
        String disCardStr = "";
        for (YjMj cards : nowDisCardIds) {
            disCardStr += (cards.toString() + ",");
        }
        setDisCardSeat(player.getSeat());
    }

    public List<YjMj> getNowDisCardIds() {
        return nowDisCardIds;
    }

    public void setDisEventAction(int disAction) {
        this.disEventAction = disAction;
        changeExtend();
    }

    public void setNowDisCardIds(List<YjMj> nowDisCardIds) {
        if (nowDisCardIds == null) {
            this.nowDisCardIds.clear();

        } else {
            this.nowDisCardIds = nowDisCardIds;
        }
        dbParamMap.put("nowDisCardIds", JSON_TAG);
    }

    /**
     * 检查摸牌
     */
    public void checkMo() {
        if (actionSeatMap.isEmpty()) {
            if (nowDisCardSeat != 0) {
                YjMjPlayer player = seatMap.get(nowDisCardSeat);
                moMajiang(player, false);
            }
            robotDealAction();
        } else {
            for (int seat : actionSeatMap.keySet()) {
                YjMjPlayer player = seatMap.get(seat);
                if (player != null && player.isRobot()) {
                    // 如果是机器人可以直接决定
                    List<Integer> actionList = actionSeatMap.get(seat);
                    if (actionList == null) {
                        continue;
                    }
                    List<YjMj> list = new ArrayList<>();
                    if (!nowDisCardIds.isEmpty()) {
                        list = YjMjQipaiTool.getVal(player.getHandMajiang(), nowDisCardIds.get(0).getVal());
                    }
                    //0胡 1碰 2明刚 3暗杠 4接杠 6报听
                    if (actionList.get(0) == 1) {// 胡
                        playCommand(player, new ArrayList<YjMj>(), YjMjDisAction.action_hu);
                    } else if (actionList.get(6) == 1) {
                        baoTing(player, YjMjDisAction.action_baoting);
                    } else if (actionList.get(2) == 1) {
                        playCommand(player, list, YjMjDisAction.action_minggang);
                    } else if (actionList.get(3) == 1) {
                        playCommand(player, list, YjMjDisAction.action_angang);
                    } else if (actionList.get(4) == 1) {
                        playCommand(player, list, YjMjDisAction.action_jiegang);
                    } else if (actionList.get(1) == 1) {
                        playCommand(player, list, YjMjDisAction.action_peng);
                    } else {
                        System.out.println("---------->" + JacksonUtil.writeValueAsString(actionList));
                    }
                }
            }

        }
    }

    @Override
    protected void robotDealAction() {
        if (isTest()) {
            int nextseat = getNextActionSeat();
            YjMjPlayer next = seatMap.get(nextseat);
            if (next != null && next.isRobot()) {
                List<Integer> actionList = actionSeatMap.get(next.getSeat());
                if (actionList != null) {// 0胡 1碰 2明刚 3暗杠 4接杠
                    List<YjMj> list = null;
                    if (actionList.get(0) == 1) {// 胡
                        playCommand(next, new ArrayList<YjMj>(), YjMjDisAction.action_hu);
                    } else if (actionList.get(6) == 1) {// 2报听
                        baoTing(next, YjMjDisAction.action_baoting);
                    } else if (actionList.get(2) == 1) {// 2明杠
                        Map<Integer, Integer> pengMap = MajiangHelper.toMajiangValMap(next.getPeng());
                        for (YjMj handMajiang : next.getHandMajiang()) {
                            if (pengMap.containsKey(handMajiang.getVal())) {// 有碰过
                                list = new ArrayList<>();
                                list.add(handMajiang);
                                playCommand(next, list, YjMjDisAction.action_minggang);
                                break;
                            }
                        }
                    } else if (actionList.get(3) == 1) {// 3暗杠
                        Map<Integer, Integer> handMap = MajiangHelper.toMajiangValMap(next.getHandMajiang());
                        for (Entry<Integer, Integer> entry : handMap.entrySet()) {
                            if (entry.getValue() == 4) {// 可以暗杠
                                list = MajiangHelper.getMajiangList(next.getHandMajiang(), entry.getKey());
                            }
                        }
                        playCommand(next, list, YjMjDisAction.action_angang);
                    } else if (actionList.get(4) == 1) {// 4接杠
                        Map<Integer, Integer> pengMap = MajiangHelper.toMajiangValMap(next.getPeng());
                        for (YjMj handMajiang : next.getHandMajiang()) {
                            if (pengMap.containsKey(handMajiang.getVal())) {
                                list = new ArrayList<>();
                                list.add(handMajiang);
                                playCommand(next, list, YjMjDisAction.action_jiegang);
                                break;
                            }
                        }
                    } else if (actionList.get(1) == 1) {
                        playCommand(next, list, YjMjDisAction.action_peng);
                    } else {
                        System.out.println("!!!!!!!!!!" + JacksonUtil.writeValueAsString(actionList));
                    }
                } else {
                    if (getLeftMajiangCount() >= maxPlayerCount) {
                        int maJiangId = RobotAI.getInstance().outPaiHandle(0, next.getHandPais(), new ArrayList<Integer>());
                        List<YjMj> majiangList = MajiangHelper.toMajiang(Arrays.asList(maJiangId));
                        playCommand(next, majiangList, 0);
                    } else {// 海底捞四张牌 不出牌 直接过
                        moLast4Majiang(next, YjMjDisAction.action_haodilaoPass);
                    }
                }
            }
        }
    }

    @Override
    protected void deal() {
        if (lastWinSeat == 0) {
            // 第一局丢筛子
            int masterseat = playerMap.get(masterId).getSeat();
            setLastWinSeat(masterseat);
        }
        setDisCardSeat(lastWinSeat);
        setNowYjDisCardSeat(lastWinSeat, false);
        setMoMajiangSeat(lastWinSeat);
        List<List<YjMj>> list = faPai();
        int i = 1;
        for (YjMjPlayer player : playerMap.values()) {
            player.changeState(player_state.play);
            if (player.getSeat() == lastWinSeat) {
                player.dealHandPais(list.get(0));
                continue;
            }
            player.dealHandPais(list.get(i));
            i++;
        }
        List<YjMj> lefts = list.get(getMaxPlayerCount());

        // 鸟牌集
//        List<YjMj> birds = new ArrayList<>();
//        for (int j = 0; j < birdNum; j++) {
//            birds.add(lefts.get(0));
//            lefts.remove(0);
//        }
//        setBirdPaiList(birds);

        // 抽牌
        int leftSize = lefts.size();
        if (chouPai > 0 && leftSize > chouPai) {
            List<YjMj> chuPaiList = lefts.subList(leftSize - chouPai, leftSize);
            chouCards = YjMjTool.toIds(chuPaiList);
            lefts = lefts.subList(0, lefts.size() - chouPai);

            StringBuilder sb = new StringBuilder("YjMj");
            sb.append("|").append(getId());
            sb.append("|").append(getPlayBureau());
            sb.append("|").append("chouPai");
            sb.append("|").append(chuPaiList);
            LogUtil.msgLog.info(sb.toString());
        }
        // 桌上剩余的牌
        setLeftMajiangs(lefts);

    }

    /**
     * 发牌
     *
     * @return
     */
    private List<List<YjMj>> faPai() {
        List<Integer> copy = new ArrayList<>(YjMjConstants.yuanjiang_mjList);
        List<List<YjMj>> list;
        // 禁止出现天胡  三门子报听
        if (zp != null) {
            list = YjMjTool.fapai(copy, maxPlayerCount, zp);
        } else {
            list = YjMjTool.fapai(copy, maxPlayerCount);
        }
        int checkTime = 0;
        while (checkTime < 10) { // 禁胡判断10次
            boolean isForbidHu = false;
            Iterator<List<YjMj>> iterator = list.iterator();
            while (iterator.hasNext()) {
                List<YjMj> next = iterator.next();
                if (isLargeForbidHu(next)) {
                    isForbidHu = true;
                    break;
                }
            }
            if (isForbidHu == true) {// 重新发牌
                copy = new ArrayList<>(YjMjConstants.yuanjiang_mjList);
                if (zp != null) {
                    list = YjMjTool.fapai(copy, maxPlayerCount, zp);
                } else {
                    list = YjMjTool.fapai(copy, maxPlayerCount);
                }
            } else
                break;
            checkTime++;
        }
        return list;
    }

    /**
     * 天胡去掉  出现三门子报听去掉
     *
     * @param yjMajiangs
     * @return
     */
    private boolean isLargeForbidHu(List<YjMj> yjMajiangs) {
        List<YjMj> gang = new ArrayList<>();
        List<YjMj> aGangs = new ArrayList<>();
        List<YjMj> peng = new ArrayList<>();
        boolean isMenQing = canMenQing();
        boolean isMaMaHu = canMaMaHu();
        List<YjMj> copy = new ArrayList<>(yjMajiangs);
        if (copy.size() == 14) {// 庄家需要判断天胡和报听
            YjMjHu hu = YjMjTool.isHuYuanjiang(copy, gang, aGangs, peng, true, false, isMenQing, isMaMaHu);
            if (hu.isHu())
                return true;
            else {// 判断报听
                for (int index = 0; index < 14; index++) {// 需要迭代判断14次
                    List<YjMj> tempMjs = new ArrayList<>(copy);
                    tempMjs.remove(index);
                    if (isLargeForbidHu(tempMjs))// 如果有三个大门子报听
                        return true;
                }
                return false;
            }
        }
        if (copy.size() == 13) {
            if (copy.size() % 3 != 2) {
                copy.add(YjMj.getMajang(201));// 加上万能牌
            }
            YjMjHu hu = YjMjTool.isHuYuanjiang(copy, gang, aGangs, peng, false, false, isMenQing, isMaMaHu);
            hu.initDahuList();
            if (hu.getDahuList().size() >= 3 || hu.isShuang7xiaodui() || hu.isSan7xiaodui()) {// 双豪华7小对 和 三豪华7小对报听都取消
                return true;
            } else
                return false;
        }
        return false;
    }

    /**
     * 初始化桌子上剩余牌
     *
     * @param leftMajiangs
     */
    public void setLeftMajiangs(List<YjMj> leftMajiangs) {
        if (leftMajiangs == null) {
            this.leftMajiangs.clear();
        } else {
            this.leftMajiangs = leftMajiangs;

        }
        dbParamMap.put("leftPais", JSON_TAG);
    }

    /**
     * 剩余牌的第一张
     *
     * @return
     */
    public YjMj getLeftMajiang() {
        if (this.leftMajiangs.size() > 0) {
            YjMj majiang = this.leftMajiangs.remove(0);
            dbParamMap.put("leftPais", JSON_TAG);
            return majiang;
        }
        return null;
    }

    /**
     * 桌上剩余的牌数
     *
     * @return
     */
    public int getLeftMajiangCount() {
        return this.leftMajiangs.size();
    }

    @Override
    public int getNextDisCardSeat() {
        if (state != table_state.play) {
            return 0;
        }
        if (disCardRound == 0) {
            return lastWinSeat;
        } else {
            return nowDisCardSeat;
        }
    }

    /**
     * 综合动作得出下一个可以出牌的人的座位
     *
     * @return
     */
    public int getNextActionSeat() {
        if (actionSeatMap.isEmpty()) {
            return getNextDisCardSeat();

        } else {
            int seat = 0;
            // 0胡 1碰 2明杠 3暗杠 4接杠
            for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
                if (seat == 0) {
                    seat = entry.getKey();
                }
                if (entry.getValue().get(0) == 1) {// 胡
                    return entry.getKey();
                }
                if (entry.getValue().get(2) == 1) {// 明杠
                    return entry.getKey();
                }
                if (entry.getValue().get(3) == 1) {// 暗杠
                    return entry.getKey();
                }
                if (entry.getValue().get(4) == 1) {// 接杠
                    return entry.getKey();
                }
                if (entry.getValue().get(1) == 1) {// 碰
                    return entry.getKey();
                }
            }
            return seat;
        }
    }

    /**
     * 计算seat右边的座位
     *
     * @param seat
     * @return
     */
    public int calcNextSeat(int seat) {
        return seat + 1 > maxPlayerCount ? 1 : seat + 1;
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

    @Override
    public CreateTableRes buildCreateTableRes(long userId, boolean isrecover, boolean isLastReady) {
        CreateTableRes.Builder res = CreateTableRes.newBuilder();
        buildCreateTableRes0(res);
        res.setNowBurCount(getPlayBureau());
        res.setTotalBurCount(getTotalBureau());
        res.setGotyeRoomId(gotyeRoomId + "");
        res.setTableId(getId() + "");
        res.setWanfa(playType);
        res.addExt(birdNum);                // 0
        res.addExt(getConifg(0));     // 1
        res.addExt(isCalcBanker);           // 2
        res.addExt(calcBird);               // 3
        // 4房费 5番数上限 6是否有门清 7门清将将胡是否可接炮 8一字撬有喜
        res.addExt(payType);                // 4房费
        res.addExt(fanshuLimit);            // 5番数上限
        res.addExt(hasMenQing);             // 6是否有门清
        res.addExt(menQingJiangJiangHu);    // 7门清将将胡是否可接炮
        res.addExt(yizhiqiao);              // 8一字撬有喜
        res.addExt(kaqiao);                 // 9卡撬
        res.addExt(jiaBei);                 // 10加倍
        res.addExt(jiaBeiFen);              // 11加倍分
        res.addExt(jiaBeiShu);              // 12加倍数
        res.addExt(autoPlayGlob);           // 13托管
        res.addExt(autoTime);               // 14托管时间
        res.addExt(below);                  // 15 低于 below 加 belowAdd 分
        res.addExt(belowAdd);               // 16

        res.setRenshu(maxPlayerCount);
        if (leftMajiangs != null) {
            res.setRemain(leftMajiangs.size());
        } else {
            res.setRemain(0);
        }
        List<PlayerInTableRes> players = new ArrayList<>();
        for (YjMjPlayer player : playerMap.values()) {
            PlayerInTableRes.Builder playerRes = player.buildPlayInTableInfo(isrecover);
            if (player.getUserId() == userId) {
                playerRes.addAllHandCardIds(player.getHandPais());
            }
            if (player.getSeat() == disCardSeat && nowDisCardIds != null) {
                playerRes.addAllOutCardIds(MajiangHelper.toMajiangIds(nowDisCardIds));
            }
            playerRes.addRecover(player.getSeat() == lastWinSeat ? 1 : 0);
            if (!isHasGangAction(player.getSeat()) && actionSeatMap.containsKey(player.getSeat()) && !huConfirmMap.containsKey(player.getSeat())) {
                playerRes.addAllRecover(actionSeatMap.get(player.getSeat()));
            }
            players.add(playerRes.build());
        }
        res.addAllPlayers(players);
        if (actionSeatMap.isEmpty()) {
            int nextSeat = getNextDisCardSeat();
            if (nextSeat != 0) {
                res.setNextSeat(nextSeat);
            }
        }
        return res.build();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getPlayer(long id, Class<T> cl) {
        return (T) playerMap.get(id);
    }

    @Override
    public int getMaxPlayerCount() {
        return maxPlayerCount;
    }

    public void setMaxPlayerCount(int playerCount) {
        this.maxPlayerCount = playerCount;
        changeExtend();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<Long, Player> getPlayerMap() {
        Object o = playerMap;
        return (Map<Long, Player>) o;
    }

    @Override
    protected void initNext1() {
        clearHuList();
        clearActionSeatMap();
        clearGangActionMap();
        setLeftMajiangs(null);
        setNowDisCardIds(null);
        clearMoGang();
        clearGangDisMajiangs();
        setMoLastMajiangSeat(0);
        setDisEventAction(0);
        setLastMajiang(null);
        clearBaotingSeat();
        clearHuPass();
        clearPengPass();
        birdPaiList.clear();
        readyTime = 0;
    }

    @Override
    public void createTable(Player player, int play, int bureauCount, Object... objects) throws Exception {
    }

    public List<Integer> removeActionSeat(int seat) {
        List<Integer> actionList = actionSeatMap.remove(seat);
        if (moGangHuList.contains(seat)) {
            removeMoGang(seat);
        }
        removeGangActionSeat(0, seat);
        saveActionSeatMap();
        return actionList;
    }

    public boolean isHasGangAction() {
        boolean has = false;
        if (gangSeatMap.isEmpty()) {
            has = false;
        }
        for (Map<Integer, List<Integer>> actionList : gangSeatMap.values()) {
            if (!actionList.isEmpty()) {
                has = true;
                break;
            }
        }
        return has;
    }

    public boolean isHasGangAction(int seat) {
        boolean has = false;
        for (Map<Integer, List<Integer>> actionMap : gangSeatMap.values()) {
            if (!actionMap.isEmpty() && actionMap.containsKey(seat)) {
                has = true;
                break;
            }
        }
        return has;
    }

    public boolean isHasGangAction(int majiang, int seat) {
        if (gangSeatMap.containsKey(majiang)) {
            if (gangSeatMap.get(majiang).containsKey(seat)) {
                return true;
            }
        }
        return false;
    }

    public void removeGangActionSeat(int majiangId, int seat) {
        if (majiangId != 0) {
            Map<Integer, List<Integer>> actionMap = gangSeatMap.get(majiangId);
            if (actionMap != null) {
                actionMap.remove(seat);
                saveActionSeatMap();
            }
        } else {
            for (Map<Integer, List<Integer>> actionMap : gangSeatMap.values()) {
                actionMap.remove(seat);
            }
            saveActionSeatMap();
        }

    }

    public void addGangActionSeat(int majiang, int seat, List<Integer> actionList) {
        Map<Integer, List<Integer>> actionMap;
        if (gangSeatMap.containsKey(majiang)) {
            actionMap = gangSeatMap.get(majiang);
        } else {
            actionMap = new HashMap<>();
            gangSeatMap.put(majiang, actionMap);
        }
        if (!actionList.isEmpty()) {
            actionMap.put(seat, actionList);

        }
        saveActionSeatMap();
    }

    public void clearGangActionMap() {
        if (!gangSeatMap.isEmpty()) {
            gangSeatMap.clear();
            saveActionSeatMap();
        }
    }

    public void coverAddActionSeat(int seat, List<Integer> actionlist) {
        actionSeatMap.put(seat, actionlist);
        addPlayLog(disCardRound + "_" + seat + "_" + YjMjDisAction.action_hasAction + "_" + StringUtil.implode(actionlist));
        saveActionSeatMap();
    }

    public void addActionSeat(int seat, List<Integer> actionlist) {
        if (actionSeatMap.containsKey(seat)) {
            List<Integer> a = actionSeatMap.get(seat);
            DataMapUtil.appendList(a, actionlist);
            addPlayLog(disCardRound + "_" + seat + "_" + YjMjDisAction.action_hasAction + "_" + StringUtil.implode(a));
        } else {
            actionSeatMap.put(seat, actionlist);
            addPlayLog(disCardRound + "_" + seat + "_" + YjMjDisAction.action_hasAction + "_" + StringUtil.implode(actionlist));
        }
        saveActionSeatMap();
    }

    public void clearActionSeatMap() {
        if (!actionSeatMap.isEmpty()) {
            actionSeatMap.clear();
            saveActionSeatMap();
        }
    }

    public void clearHuList() {
        huConfirmMap.clear();
        changeExtend();
    }

    public void addHuList(int seat, int majiangId) {
        if (!huConfirmMap.containsKey(seat)) {
            huConfirmMap.put(seat, majiangId);
        }
        changeExtend();
    }

    public void saveActionSeatMap() {
        dbParamMap.put("nowAction", JSON_TAG);
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
            gangSeatMap = DataMapUtil.toListMapMap(val2);

        }
    }

    @Override
    protected void loadFromDB1(TableInf info) {
        if (!StringUtils.isBlank(info.getNowDisCardIds())) {
            nowDisCardIds = MajiangHelper.toMajiang(StringUtil.explodeToIntList(info.getNowDisCardIds()));
        }

        if (!StringUtils.isBlank(info.getLeftPais())) {
            leftMajiangs = MajiangHelper.toMajiang(StringUtil.explodeToIntList(info.getLeftPais()));
        }

    }

    /**
     * 是否能吃
     *
     * @param player
     * @param majiangs
     * @return
     */
    @SuppressWarnings("unused")
    private boolean canChi(YjMjPlayer player, List<YjMj> handMajiang, List<YjMj> majiangs, YjMj disMajiang) {
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return false;
        }
        if (player.isAlreadyMoMajiang()) {
            return false;
        }
        List<Integer> pengGangSeatList = getPengGangSeatByActionMap();
        pengGangSeatList.remove((Object) player.getSeat());
        if (!pengGangSeatList.isEmpty()) {
            return false;
        }

        if (disMajiang == null) {
            return false;
        }

        if (!handMajiang.containsAll(majiangs)) {
            return false;
        }

        List<YjMj> chi = YjMjTool.checkChi(majiangs, disMajiang);
        return !chi.isEmpty();
    }

    /**
     * 是否能碰
     *
     * @param player
     * @param majiangs
     * @param sameCount
     * @return
     */
    private boolean canPeng(YjMjPlayer player, List<YjMj> majiangs, int sameCount, YjMj disMajiang) {
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return false;
        }
        if (player.isAlreadyMoMajiang()) {
            return false;
        }
        if (sameCount != 2) {
            return false;
        }
        if (disMajiang == null) {
            return false;
        }
        if (majiangs.get(0).getVal() != disMajiang.getVal()) {
            return false;
        }
        return true;
    }

    /**
     * 是否能明杠
     *
     * @param player
     * @param majiangs
     * @param sameCount
     * @return
     */
    private boolean canAnGang(YjMjPlayer player, List<YjMj> majiangs, int sameCount) {
        if (sameCount != 4) {
            return false;
        }
        if (player.getSeat() != getNextDisCardSeat()) {
            return false;
        }
        return true;
    }

    /**
     * 检查优先度，胡杠补碰吃 如果同时出现一个事件，按出牌座位顺序优先
     * 优先度为胡杠补碰吃
     *
     * @param player
     * @param action
     * @return
     */
    public boolean checkAction(YjMjPlayer player, int action) {
        List<Integer> stopActionList = YjMjDisAction.findPriorityAction(action);
        for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
            if (player.getSeat() != entry.getKey()) {// 别人
                boolean can = YjMjDisAction.canDisMajiang(stopActionList, entry.getValue());
                if (!can) {
                    return false;
                }
                List<Integer> disActionList = YjMjDisAction.parseToDisActionList(entry.getValue());
                if (disActionList.contains(action)) {// 同时拥有同一个事件 根据座位号来判断
                    int actionSeat = entry.getKey();
                    int nearSeat = getNearSeat(nowDisCardSeat, Arrays.asList(player.getSeat(), actionSeat));
                    if (nearSeat != player.getSeat()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * 是否能明杠
     *
     * @param player
     * @param majiangs
     * @param sameCount
     * @return
     */
    private boolean canMingGang(YjMjPlayer player, List<YjMj> handMajiang, List<YjMj> majiangs, int sameCount, YjMj disMajiang) {
        List<Integer> pengList = MajiangHelper.toMajiangVals(player.getPeng());
        if (majiangs.size() == 1) {
            if (!isHasGangAction() && player.getSeat() != getNextDisCardSeat()) {
                return false;
            }
            if (handMajiang.containsAll(majiangs) && pengList.contains(majiangs.get(0).getVal())) {
                return true;
            }
        } else if (majiangs.size() == 3) {
            if (sameCount != 3) {
                return false;
            }
            if (!actionSeatMap.containsKey(player.getSeat())) {
                return false;
            }
            if (disMajiang == null || disMajiang.getVal() != majiangs.get(0).getVal()) {
                return false;
            }
            return true;
        }

        return false;
    }

    public Map<Integer, List<Integer>> getActionSeatMap() {
        robotDealAction();
        return actionSeatMap;
    }

    public int getBirdNum() {
        return birdNum;
    }

    public void setBirdNum(int birdNum) {
        this.birdNum = birdNum;
        changeExtend();
    }

    public void setMoMajiangSeat(int moMajiangSeat) {
        this.moMajiangSeat = moMajiangSeat;
        changeExtend();
    }

    public void setLastMajiang(YjMj lastMajiang) {
        this.lastMajiang = lastMajiang;
        changeExtend();
    }

    public YjMj getLastMajiang() {
        return this.lastMajiang;
    }

    public void setMoLastMajiangSeat(int moLastMajiangSeat) {
        this.moLastMajiangSeat = moLastMajiangSeat;
        changeExtend();
    }

    public void setGangMajiang(YjMj gangMajiang) {
        this.gangMajiang = gangMajiang;
        changeExtend();
    }

    public void addBaotingSeat(int seat) {
        this.baotingSeat.put(seat, 0);
        changeExtend();
    }

    public void clearBaotingSeat() {
        this.baotingSeat.clear();
        changeExtend();
    }

    public Map<Integer, Integer> getBaotingSeat() {
        return baotingSeat;
    }

    /**
     * 摸杠别人可以胡
     *
     * @param moGang           杠的牌
     * @param moGangHuSeatList 可以胡的人的座位list
     */
    public void setMoGang(YjMj moGang, List<Integer> moGangHuSeatList) {
        this.moGang = moGang;
        this.moGangHuList = moGangHuSeatList;
        changeExtend();
    }

    /**
     * 清除摸杠胡
     */
    public void clearMoGang() {
        this.moGang = null;
        this.moGangHuList.clear();
        changeExtend();
    }

    public void setGangDisMajiangs(List<YjMj> gangDisMajiangs) {
        this.gangDisMajiangs = gangDisMajiangs;
        changeExtend();
    }

    /**
     * 清理杠后摸的牌
     */
    public void clearGangDisMajiangs() {
        this.gangMajiang = null;
        this.gangDisMajiangs.clear();
        changeExtend();
    }

    /**
     * pass 摸杠胡
     *
     * @param seat
     */
    public void removeMoGang(int seat) {
        this.moGangHuList.remove((Object) seat);
        changeExtend();
    }

    public List<Integer> getMoGangHuSeats() {
        return moGangHuList;
    }

    public int getMoMajiangSeat() {
        return moMajiangSeat;
    }

    @Override
    protected String buildNowAction() {
        JsonWrapper wrapper = new JsonWrapper("");
        wrapper.putString(1, DataMapUtil.explodeListMap(actionSeatMap));
        wrapper.putString(2, DataMapUtil.explodeListMapMap(gangSeatMap));
        return wrapper.toString();
    }

    @Override
    public void setConfig(int index, int val) {
    }

    /**
     * 只能自摸胡
     *
     * @return
     */
    public boolean moHu() {
        if (getConifg(0) == 2) {
            return true;
        }
        return false;
    }

    /**
     * 抢杠胡开关
     *
     * @return
     */
    public boolean canGangHu() {
//		if (getConifg(0) == 1) {
//			return true;
//		}
        return true;
    }

    public YjClosingInfoRes.Builder sendAccountsMsg(boolean over, boolean selfMo, List<Integer> winList, int[] prickBirdMajiangIds, int[] seatBirds, Map<Integer, Integer> seatBridMap, boolean isBreak, int bankerSeat, int fangPaoSeat, int[] jiePaoSeat) {

        if (isBreak) {
            // 不管流局都加分
            calcExtraPoint(winList);// 杠分和一字撬有喜算分
            for (YjMjPlayer player : seatMap.values()) {//设置玩家局分point
                player.changePoint(player.getLostPoint());
                logHuPoint(player);
            }
        }

        //大结算计算加倍分
        if (over && jiaBei == 1) {
            int jiaBeiPoint = 0;
            int loserCount = 0;
            for (YjMjPlayer player : seatMap.values()) {
                if (player.getTotalPoint() > 0 && player.getTotalPoint() < jiaBeiFen) {
                    jiaBeiPoint += player.getTotalPoint() * (jiaBeiShu - 1);
                    player.setTotalPoint(player.getTotalPoint() * jiaBeiShu);
                } else if (player.getTotalPoint() < 0) {
                    loserCount++;
                }
            }
            if (jiaBeiPoint > 0) {
                for (YjMjPlayer player : seatMap.values()) {
                    if (player.getTotalPoint() < 0) {
                        player.setTotalPoint(player.getTotalPoint() - (jiaBeiPoint / loserCount));
                    }
                }
            }
        }

        //大结算低于below分+belowAdd分
        if (over && belowAdd > 0 && playerMap.size() == 2) {
            for (YjMjPlayer player : seatMap.values()) {
                int totalPoint = player.getTotalPoint();
                if (totalPoint > -below && totalPoint < 0) {
                    player.setTotalPoint(player.getTotalPoint() - belowAdd);
                } else if (totalPoint < below && totalPoint > 0) {
                    player.setTotalPoint(player.getTotalPoint() + belowAdd);
                }
            }
        }

        List<YjClosingPlayerInfoRes.Builder> builderList = new ArrayList<>();
        List<YjClosingPlayerInfoRes> list = new ArrayList<>();
        for (YjMjPlayer player : seatMap.values()) {
            YjClosingPlayerInfoRes.Builder build = null;
            if (over) {
                build = player.bulidTotalClosingPlayerInfoRes();
            } else {
                build = player.bulidOneClosingPlayerInfoRes();
            }
            if (seatBridMap != null && seatBridMap.containsKey(player.getSeat())) {
                build.addActionCounts(seatBridMap.get(player.getSeat()));
            } else {
                build.addActionCounts(0);
            }
            if (winList != null && winList.contains(player.getSeat())) {
                YjMj huMajiang = null;
                if (isHasGangAction()) {
                    huMajiang = getGangHuMajiang(player.getSeat());
                }
                if (!selfMo) {
                    // 不是自摸
                    if (huMajiang == null) {
                        huMajiang = nowDisCardIds.get(0);
                    }
                    if (!build.getCardsList().contains(huMajiang.getId())) {
                        build.addCards(huMajiang.getId());
                    }
                    build.setIsHu(huMajiang.getId());
                } else {
                    if (huMajiang == null) {
                        huMajiang = player.getLastMoMajiang();
                    }
                    if (!build.getCardsList().contains(huMajiang.getId())) {
                        build.addCards(huMajiang.getId());
                    }
                    build.setIsHu(huMajiang.getId());
                }
            }
            if (winList != null && winList.contains(player.getSeat())) {
                // 手上没有剩余的牌放第一位为赢家
                builderList.add(0, build);
            } else {
                builderList.add(build);
            }

            //信用分
            if (isCreditTable()) {
                player.setWinLoseCredit(player.getTotalPoint() * creditDifen);
            }
        }

        //信用分计算
        if (isCreditTable()) {
            //计算信用负分
            calcNegativeCredit();
            long dyjCredit = 0;
            for (YjMjPlayer player : seatMap.values()) {
                if (player.getWinLoseCredit() > dyjCredit) {
                    dyjCredit = player.getWinLoseCredit();
                }
            }
            for (YjClosingPlayerInfoRes.Builder builder : builderList) {
                YjMjPlayer player = seatMap.get(builder.getSeat());
                calcCommissionCredit(player, dyjCredit);
                builder.setWinLoseCredit(player.getWinLoseCredit());
                builder.setCommissionCredit(player.getCommissionCredit());
            }
        } else if (isGroupTableGoldRoom()) {
            // -----------亲友圈金币场---------------------------------
            for (YjMjPlayer player : seatMap.values()) {
                player.setWinGold(player.getTotalPoint() * gtgDifen);
            }
            calcGroupTableGoldRoomWinLimit();
            for (YjClosingPlayerInfoRes.Builder builder : builderList) {
                YjMjPlayer player = seatMap.get(builder.getSeat());
                builder.setWinLoseCredit(player.getWinGold());
            }
        }

        for (YjClosingPlayerInfoRes.Builder builder : builderList) {
            list.add(builder.build());
        }

        YjClosingInfoRes.Builder res = YjClosingInfoRes.newBuilder();
        res.addAllClosingPlayers(list);
        res.setIsBreak(isBreak ? 1 : 0);
        res.setWanfa(getWanFa());
        res.setGroupLogId((int) groupPaylogId);
        res.addAllExt(buildAccountsExt(bankerSeat, over));
        res.addAllLeftCards(MajiangHelper.toMajiangIds(leftMajiangs));
        res.setFangPaoSeat(fangPaoSeat);
        res.addCreditConfig(creditMode);                         //0
        res.addCreditConfig(creditJoinLimit);                    //1
        res.addCreditConfig(creditDissLimit);                    //2
        res.addCreditConfig(creditDifen);                        //3
        res.addCreditConfig(creditCommission);                   //4
        res.addCreditConfig(creditCommissionMode1);              //5
        res.addCreditConfig(creditCommissionMode2);              //6
        res.addCreditConfig(creditCommissionLimit);              //7
        res.addAllIntParams(intParams);
        res.addAllChouCards(chouCards);
        if (jiePaoSeat != null)
            res.addAllJiePaoSeat(DataMapUtil.toList(jiePaoSeat));
        if (seatBirds != null) {
            res.addAllBirdSeat(DataMapUtil.toList(seatBirds));
        }
        if (birdPaiList != null) {
            res.addAllBird(YjMjQipaiTool.toMajiangIds(birdPaiList));
        }
        for (YjMjPlayer player : seatMap.values()) {
            player.writeSocket(res.build());
        }
        return res;

    }

    /**
     * 杠上花和杠上炮
     *
     * @return
     */
    public YjMj getGangHuMajiang(int seat) {
        int majiangId = 0;
        for (Entry<Integer, Map<Integer, List<Integer>>> entry : gangSeatMap.entrySet()) {
            Map<Integer, List<Integer>> actionMap = entry.getValue();
            if (actionMap.containsKey(seat)) {
                List<Integer> actionList = actionMap.get(seat);
                if (actionList != null && !actionList.isEmpty() && actionList.get(0) == 1) {
                    majiangId = entry.getKey();
                    break;
                }
            }
        }
        return YjMj.getMajang(majiangId);

    }

    /**
     * 牌桌ID 房主ID 当前结算时间  玩法  是否能抢杠胡 扎鸟数 计算庄闲 计算鸟的算法  鸟扎中座位  总局数 番数上限 是否有门清 门清将将胡是否可接炮 一字撬有喜
     *
     * @param bankerSeat
     * @return
     */
    public List<String> buildAccountsExt(int bankerSeat, boolean isOver) {
        List<String> ext = new ArrayList<>();
        ext.add(id + "");                               // 0牌桌ID
        ext.add(masterId + "");                         // 1房主ID
        ext.add(TimeUtil.formatTime(TimeUtil.now()));   // 2当前结算时间
        ext.add(playType + "");                         // 3玩法
        ext.add(getConifg(0) + "");               // 4是否能抢杠胡
        ext.add(birdNum + "");                          // 5扎鸟数
        ext.add(isCalcBanker + "");                     // 6计算庄闲
        ext.add(calcBird + "");                         // 7计算鸟的算法
        ext.add(bankerSeat + "");                       // 8鸟扎中座位
        ext.add(totalBureau + "");                      // 9总局数
        ext.add(fanshuLimit + "");                      // 10番数上限
        ext.add(hasMenQing + "");                       // 11是否有门清
        ext.add(menQingJiangJiangHu + "");              // 12门清将将胡是否可接炮
        ext.add(yizhiqiao + "");                        // 13一字撬有喜
        ext.add(kaqiao + "");                           // 14卡撬
        ext.add(isGroupRoom() ? loadGroupId() : "0");   // 15
        ext.add(maxPlayerCount + "");                   // 16
        ext.add(isOver ? "1" : "0");                    // 17
        return ext;
    }

    @Override
    public void sendAccountsMsg() {
        YjClosingInfoRes.Builder builder = sendAccountsMsg(true, false, null, null, null, null, true, 0, 0, null);
        saveLog(true, 0l, builder.build());
    }

    public Class<? extends Player> getPlayerClass() {
        return YjMjPlayer.class;
    }

    @Override
    public int getWanFa() {
        return GameUtil.play_type_yuanjiang;
    }

    @Override
    public boolean isTest() {
        return YjMjConstants.isTest;
    }

    @Override
    public void checkReconnect(Player player) {
        seatMap.get(player.getSeat()).checkSendActionRes();
        checkSendGangRes(player);
        if (actionSeatMap.isEmpty()) {// 没有其他可操作的动作事件
            if (player instanceof YjMjPlayer) {
                YjMjPlayer csMjPlayer = (YjMjPlayer) player;
                if (csMjPlayer != null) {
                    if (csMjPlayer.isAlreadyMoMajiang()) {
                        if (baotingSeat.containsKey(csMjPlayer.getSeat())) {// 报听自动打牌
                            List<YjMj> disMajiangs = new ArrayList<>();
                            disMajiangs.add(csMjPlayer.getLastMoMajiang());
                            chuPai(csMjPlayer, disMajiangs, 0);
                        }
                    }
                }
            }
        }
        if (state == table_state.play) {
            YjMjPlayer player1 = (YjMjPlayer) player;
            if (player1.getHandPais() != null && player1.getHandPais().size() > 0) {
                sendTingInfo(player1);
            }
        }
    }

    public static void main(String[] args) {
        System.out.println(Math.pow(3, 2));
    }

    @SuppressWarnings({"unused", "static-access"})
    private YjClosingInfoRes.Builder test() {
        String[] str = new String[]{
                "{\"dahus\":[1,8],\"icon\":\"http://wx.qlogo.cn/mmopen/25FRchib0VdljibYZe4WsZN0pbBQQECYc0B4V9Bjn5HlDzqeM4JLfp3SIWRCKDDl5VlNibBKViam8xYibFiaWe7Fibm3Ihu8pWWTiaMY/0\",\"point\":-108,\"leftCardNum\":0,\"sex\":1,\"totalPoint\":-153,\"seat\":1,\"name\":\"千金一诺\",\"userId\":\"103596\",\"actionCounts\":[1,1,0,1],\"isHu\":77,\"cards\":[65,5,35,56,44,14,53,101,20,47,23,104,50,77]}",
                "{\"icon\":\"http://wx.qlogo.cn/mmopen/SOkBQWIHibUbEabTknxaXHYMQMZFMKyoHmuG3LNKOFLvxTQegZwa3UFHOR5Uy3feibDDHnMnd9cErXcG7tgdc8icicOVooGzjypia/0\",\"point\":-12,\"leftCardNum\":0,\"sex\":1,\"totalPoint\":55,\"seat\":2,\"name\":\"GG GL HF\",\"userId\":\"103614\",\"actionCounts\":[0,0,0,0],\"cards\":[98,29,84,15,37,64,2,22,103,76,11,93,13]}",
                "{\"icon\":\"http://wx.qlogo.cn/mmopen/g9RQicMD01M2MfibJkibYic3OAuv4cwTgPfdBBmy6ImlJCWNNJN6IMJbHtKQugt4EPOHzybcY7Sh7MvojoKSQp3s8l0MxDJmFprH/0\",\"point\":-12,\"leftCardNum\":0,\"sex\":1,\"totalPoint\":-6,\"seat\":3,\"name\":\"justin -wan\",\"userId\":\"103592\",\"actionCounts\":[0,0,0,0],\"cards\":[91,78,51,105,7,61,34,12,39,66,85,30,59]}",
                "{\"icon\":\"http://wx.qlogo.cn/mmopen/SOkBQWIHibUbffmgIJic7USO8hIz91ica5ESiankd2YPSTlfbMItaFeKmqnVdSS21IOP1FWBanzbZDraFRncfo8BXg/0\",\"point\":-12,\"leftCardNum\":0,\"sex\":2,\"totalPoint\":-40,\"seat\":4,\"name\":\"罗梦\",\"userId\":\"103630\",\"actionCounts\":[0,0,0,0],\"cards\":[95,80,63,9,107,90,41,73,46,100,6,60,33]}"

        };

        YjClosingInfoRes.Builder infolist = YjClosingInfoRes.newBuilder();
        for (String resValue : str) {
            YjClosingPlayerInfoRes.Builder info = YjClosingPlayerInfoRes.newBuilder();
            Map<String, Object> resMap = JacksonUtil.readValue(resValue, new TypeReference<Map<String, Object>>() {
            });
            for (Object o : resMap.keySet()) {
                String key = o.toString();
                List<FieldDescriptor> list = info.getDescriptor().getFields();
                for (FieldDescriptor field : list) {

                    if (field.getName().equals(key)) {
                        info.setField(field, resMap.get(key));

                    }
                }
            }

            infolist.addClosingPlayers(info);
            infolist.setWanfa(getWanFa());
            infolist.addAllExt(buildAccountsExt(0, false));
        }

        for (YjMjPlayer player : seatMap.values()) {
            player.writeSocket(infolist.build());
        }
        return infolist;
    }

    @Override
    public boolean consumeCards() {
        return SharedConstants.consumecards;
    }


    @Override
    public void checkAutoPlay() {
        if (getSendDissTime() > 0) {
            for (YjMjPlayer player : seatMap.values()) {
                if (player.getLastCheckTime() > 0) {
                    player.setLastCheckTime(player.getLastCheckTime() + 1 * 1000);
                }
            }
            return;
        }

        // 报听自动出牌
        if (baotingSeat.containsKey(nowDisCardSeat)) {
            YjMjPlayer player = seatMap.get(nowDisCardSeat);
            if (player != null && !actionSeatMap.containsKey(nowDisCardSeat) && !player.hadMoHaiDi()) {
                autoChuPai(player);
                return;
            }
        }

        if (!autoPlay) {
            return;
        }

        if (isAutoPlayOff()) {
            // 托管关闭
            for (int seat : seatMap.keySet()) {
                YjMjPlayer player = seatMap.get(seat);
                player.setAutoPlay(false, false);
                player.setCheckAutoPlay(false);
            }
            return;
        }

        if (getTableStatus() == YjMjConstants.TABLE_STATUS_PIAO) {
            for (int seat : seatMap.keySet()) {
                YjMjPlayer player = seatMap.get(seat);
                if (player.getLastCheckTime() > 0 && player.getPiaoPoint() >= 0) {
                    player.setLastCheckTime(player.getLastCheckTime() + 1 * 1000);
                    continue;
                }
                player.checkAutoPlay(2, false);
                if (!player.isAutoPlay()) {
                    continue;
                }
                autoPiao(player);
            }
            boolean piao = true;
            for (int seat : seatMap.keySet()) {
                YjMjPlayer player = seatMap.get(seat);
                if (player.getPiaoPoint() < 0) {
                    piao = false;
                }

            }
            if (piao) {
                setTableStatus(YjMjConstants.AUTO_PLAY_TIME);
            }

        } else if (state == table_state.play) {
            autoPlay();
        } else {
            if (getPlayedBureau() == 0) {
                return;
            }
            readyTime++;
            //开了托管的房间，xx秒后自动开始下一局
            for (YjMjPlayer player : seatMap.values()) {
                if (player.getState() != player_state.entry && player.getState() != player_state.over) {
                    continue;
                } else {
                    if (readyTime >= 5 && player.isAutoPlay()) {
                        // 玩家进入托管后，3秒自动准备
                        autoReady(player);
                    } else if (readyTime > 30) {
                        autoReady(player);
                    }
                }
            }
        }
    }

    /**
     * 自动出牌
     */
    public synchronized void autoPlay() {
        if (state != table_state.play) {
            return;
        }

        if (!actionSeatMap.isEmpty()) {
            List<Integer> huSeatList = getHuSeatByActionMap();
            if (!huSeatList.isEmpty()) {
                //有胡处理胡
                for (int seat : huSeatList) {
                    YjMjPlayer player = seatMap.get(seat);
                    if (player == null) {
                        continue;
                    }
                    if (!player.checkAutoPlay(2, false)) {
                        continue;
                    }
                    playCommand(player, new ArrayList<>(), YjMjDisAction.action_hu);
                }
                return;
            } else {
                int action, seat;
                for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
                    List<Integer> actList = YjMjDisAction.parseToDisActionList(entry.getValue());
                    if (actList == null) {
                        continue;
                    }
                    seat = entry.getKey();
                    action = YjMjDisAction.getAutoMaxPriorityAction(actList);
                    YjMjPlayer player = seatMap.get(seat);
                    if (!player.checkAutoPlay(0, false)) {
                        continue;
                    }
                    boolean chuPai = false;
                    if (player.isAlreadyMoMajiang() && !player.hadMoHaiDi()) {
                        chuPai = true;
                    }
                    if (action == YjMjDisAction.action_peng) {
                        if (player.isAutoPlaySelf()) {
                            //自己开启托管直接过
                            playCommand(player, new ArrayList<>(), YjMjDisAction.action_pass);
                            if (chuPai) {
                                autoChuPai(player);
                            }
                        } else {
                            if (nowDisCardIds != null && !nowDisCardIds.isEmpty()) {
                                YjMj mj = nowDisCardIds.get(0);
                                List<YjMj> mjList = new ArrayList<>();
                                for (YjMj handMj : player.getHandMajiang()) {
                                    if (handMj.getVal() == mj.getVal()) {
                                        mjList.add(handMj);
                                        if (mjList.size() == 2) {
                                            break;
                                        }
                                    }
                                }
                                playCommand(player, mjList, YjMjDisAction.action_peng);
                            }
                        }
                    } else {
                        if (player.hadMoHaiDi()) {
                            playCommand(player, new ArrayList<>(), YjMjDisAction.action_haodilaoPass);
                        } else {
                            playCommand(player, new ArrayList<>(), YjMjDisAction.action_pass);
                            if (chuPai) {
                                autoChuPai(player);
                            }
                        }
                    }
                }
            }
        } else {
            YjMjPlayer player = seatMap.get(nowDisCardSeat);
            if (player == null || !player.checkAutoPlay(0, false)) {
                return;
            }
            if (player.hadMoHaiDi()) {
                playCommand(player, new ArrayList<>(), YjMjDisAction.action_haodilaoPass);
            } else {
                autoChuPai(player);
            }
        }
    }

    public void autoChuPai(YjMjPlayer player) {

        if (!player.isAlreadyMoMajiang()) {
            return;
        } else if (player.hadMoHaiDi()) {
            // 摸海底不打牌
            return;
        }

        List<Integer> handMjIds = new ArrayList<>(player.getHandPais());
        int index = handMjIds.size() - 1;
        int mjId = -1;
        if (moMajiangSeat == player.getSeat()) {
            mjId = handMjIds.get(index);
        } else {
            Collections.sort(handMjIds);
            mjId = handMjIds.get(index);
        }
        YjMj mj = YjMj.getMajang(mjId);
        if (mjId != -1) {
            List<YjMj> mjList = YjMjHelper.toMajiang(Arrays.asList(mjId));
            playCommand(player, mjList, YjMjDisAction.action_chupai);
        }
    }

    public boolean IsCalcBankerPoint() {
        return true;
    }

    public void setIsCalcBanker(int isCalcBanker) {
        this.isCalcBanker = isCalcBanker;
        changeExtend();
    }

    public int getCalcBird() {
        return calcBird;
    }

    public void setCalcBird(int calcBird) {
        this.calcBird = calcBird;
        changeExtend();
    }

    public int getFanshuLimit() {
        return fanshuLimit;
    }

    public void setFanshuLimit(int fanshuLimit) {
        this.fanshuLimit = fanshuLimit;
    }

    public boolean canMenQing() {
        return hasMenQing == 1;
    }

    public void setHasMenQing(int hasMenQing) {
        this.hasMenQing = hasMenQing;
    }

    public int getMenQingJiangJiangHu() {
        return menQingJiangJiangHu;
    }

    public void setMenQingJiangJiangHu(int menQingJiangJiangHu) {
        this.menQingJiangJiangHu = menQingJiangJiangHu;
    }

    public int getYizhiqiao() {
        return yizhiqiao;
    }

    public void setYizhiqiao(int yizhiqiao) {
        this.yizhiqiao = yizhiqiao;
    }

    public int getKaqiao() {
        return kaqiao;
    }

    public void setKaqiao(int kaqiao) {
        this.kaqiao = kaqiao;
    }

    /**
     * 玩家过手后清理胡 碰过手信息
     *
     * @param seat
     */
    public void removeHuPengPass(int seat) {
        boolean update = false;
        if (huPassList.contains((Object) seat)) {
            huPassList.remove((Object) seat);// 胡过手清理
            update = true;
        }
        if (pengPassMap.containsKey((Object) seat)) {
            pengPassMap.remove((Object) seat);// 碰过手清理
            update = true;
        }
        if (update)
            changeExtend();
    }

    /**
     * 沅江麻将设置出牌座位号
     *
     * @param seat
     * @param calcNextSeat 是否计算下个位置
     */
    public void setNowYjDisCardSeat(int seat, boolean calcNextSeat) {
        removeHuPengPass(seat);
        if (calcNextSeat) {
            int nextSeat = calcNextSeat(seat);
            setNowDisCardSeat(nextSeat);
        } else {
            setNowDisCardSeat(seat);
        }
    }

    public void clearHuPass() {
        huPassList.clear();
        changeExtend();
    }

    public void clearPengPass() {
        pengPassMap.clear();
        changeExtend();
    }

    public List<Integer> getHuPassSeat() {
        return huPassList;
    }

    public Map<Integer, List<Integer>> getPengPassSeat() {
        return pengPassMap;
    }

    /**
     * 是否已经碰过(未过手)
     *
     * @param seat
     * @param majiangValue
     * @return
     */
    public boolean inPengPassSeat(int seat, int majiangValue) {
        if (pengPassMap.containsKey((Object) seat) && pengPassMap.get((Object) seat).contains((Object) majiangValue))
            return true;
        else
            return false;
    }

    public void addHuPassSeat(int seat) {
        if (!huPassList.contains(seat)) {
            huPassList.add(seat);
            changeExtend();
        }
    }

    public void addPengPassSeat(int seat, int pengMajiangValue) {
        if (pengPassMap.containsKey(seat)) {
            pengPassMap.get(seat).add(pengMajiangValue);
        } else {
            List<Integer> pengMajiangs = new ArrayList<>();
            pengMajiangs.add(pengMajiangValue);
            pengPassMap.put(seat, pengMajiangs);
        }
        changeExtend();
    }

    @Override
    public void initExtend0(JsonWrapper extend) {
        for (YjMjPlayer player : seatMap.values()) {
            player.initExtend(extend.getString(player.getSeat()));
        }
        String huListstr = extend.getString(5);
        if (!StringUtils.isBlank(huListstr)) {
            huConfirmMap = DataMapUtil.implode(huListstr);
        }
        birdNum = extend.getInt(6, 0);
        moMajiangSeat = extend.getInt(7, 0);
        int moGangMajiangId = extend.getInt(8, 0);
        if (moGangMajiangId != 0) {
            moGang = YjMj.getMajang(moGangMajiangId);
        }
        String moGangHu = extend.getString(9);
        if (!StringUtils.isBlank(moGangHu)) {
            moGangHuList = StringUtil.explodeToIntList(moGangHu);
        }
        String gangDisMajiangstr = extend.getString(10);
        if (!StringUtils.isBlank(gangDisMajiangstr)) {
            gangDisMajiangs = MajiangHelper.explodeMajiang(gangDisMajiangstr, ",");
        }
        int gangMajiang = extend.getInt(11, 0);
        if (gangMajiang != 0) {
            this.gangMajiang = YjMj.getMajang(gangMajiang);
        }
        //12暂时未空
        moLastMajiangSeat = extend.getInt(13, 0);
        int lastMajiangId = extend.getInt(14, 0);
        if (lastMajiangId != 0) {
            this.lastMajiang = YjMj.getMajang(lastMajiangId);
        }
        String baotingSeatStr = extend.getString(15);
        if (!StringUtils.isBlank(baotingSeatStr)) {
            baotingSeat = DataMapUtil.implode(baotingSeatStr);
        }
        disEventAction = extend.getInt(16, 0);
        isCalcBanker = extend.getInt(17, 1);
        calcBird = extend.getInt(18, 1);
        String huPassStr = extend.getString(19);
        if (!StringUtils.isBlank(huPassStr)) {
            huPassList = StringUtil.explodeToIntList(huPassStr);
        }
        String pengPassStr = extend.getString(20);
        if (!StringUtils.isBlank(pengPassStr)) {
            pengPassMap = DataMapUtil.toListMap(pengPassStr);
        }
        payType = extend.getInt(21, 0);
        fanshuLimit = extend.getInt(22, 0);
        hasMenQing = extend.getInt(23, 0);
        menQingJiangJiangHu = extend.getInt(24, 0);
        yizhiqiao = extend.getInt(25, 0);
        kaqiao = extend.getInt(26, 0);
        isAAConsume = Boolean.parseBoolean(extend.getString(27));
        maxPlayerCount = extend.getInt(28, 4);
        if (maxPlayerCount <= 0) {
            maxPlayerCount = 4;
        }
        String birdPaiListStr = extend.getString(29);
        if (!StringUtils.isBlank(birdPaiListStr)) {
            birdPaiList = MajiangHelper.explodeMajiang(birdPaiListStr, ",");
        }
        groupPaylogId = extend.getLong(30, 0);
        autoPlayGlob = extend.getInt(31, 0);
        autoTime = extend.getInt(32, 0);
        tableStatus = extend.getInt(33, 0);
        jiaBei = extend.getInt(34, 0);
        jiaBeiFen = extend.getInt(35, 0);
        jiaBeiShu = extend.getInt(36, 0);
        below = extend.getInt(37, 0);
        belowAdd = extend.getInt(38, 0);
        this.chouPai = extend.getInt(39, 0);
        String chouCardsStr = extend.getString(40);
        if (StringUtils.isNotBlank(chouCardsStr)) {
            this.chouCards = StringUtil.explodeToIntList(chouCardsStr);
        }
        this.maMaHu = extend.getInt(41, 0);
    }

    @Override
    public JsonWrapper buildExtend0(JsonWrapper extend) {
        for (YjMjPlayer player : seatMap.values()) {
            extend.putString(player.getSeat(), player.toExtendStr());
        }
        extend.putString(5, DataMapUtil.explode(huConfirmMap));
        extend.putInt(6, birdNum);
        extend.putInt(7, moMajiangSeat);
        if (moGang != null) {
            extend.putInt(8, moGang.getId());
        } else {
            extend.putInt(8, 0);
        }
        extend.putString(9, StringUtil.implode(moGangHuList, ","));
        extend.putString(10, MajiangHelper.implodeMajiang(gangDisMajiangs, ","));
        if (gangMajiang != null) {
            extend.putInt(11, gangMajiang.getId());
        } else {
            extend.putInt(11, 0);
        }
        //12暂时未空
        extend.putInt(13, moLastMajiangSeat);
        if (lastMajiang != null) {
            extend.putInt(14, lastMajiang.getId());
        } else {
            extend.putInt(14, 0);
        }
        extend.putString(15, DataMapUtil.explode(baotingSeat));
        extend.putInt(16, disEventAction);
        extend.putInt(17, isCalcBanker);
        extend.putInt(18, calcBird);
        extend.putString(19, StringUtil.implode(huPassList, ","));
        extend.putString(20, DataMapUtil.explodeListMap(pengPassMap));
        extend.putInt(21, payType);
        extend.putInt(22, fanshuLimit);
        extend.putInt(23, hasMenQing);
        extend.putInt(24, menQingJiangJiangHu);
        extend.putInt(25, yizhiqiao);
        extend.putInt(26, kaqiao);
        extend.putString(27, Boolean.toString(isAAConsume));
        extend.putInt(28, maxPlayerCount);
        extend.putString(29, MajiangHelper.implodeMajiang(birdPaiList, ","));
        extend.putLong(30, groupPaylogId);
        extend.putInt(31, autoPlayGlob);
        extend.putInt(32, autoTime);
        extend.putInt(33, tableStatus);
        extend.putInt(34, jiaBei);
        extend.putInt(35, jiaBeiFen);
        extend.putInt(36, jiaBeiShu);
        extend.putInt(37, below);
        extend.putInt(38, belowAdd);
        extend.putInt(39, this.chouPai);
        extend.putString(40, StringUtil.implode(this.chouCards, ","));
        extend.putInt(41, this.maMaHu);
        return extend;
    }

    @Override
    public void createTable(Player player, int play, int bureauCount, List<Integer> params, List<String> strParams, Object... objects) throws Exception {
        //0 局数  1玩法   2 番数上限  3是否有门清 4门清将将胡是否可接炮  5一字撬是否有喜  7人数  10房费
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
        info.setConfig(String.valueOf(0));
        info.setExtend(buildExtend());
        TableDao.getInstance().save(info);
        loadFromDB(info);
        setIsCalcBanker(1);// 默认胡牌为庄
        setCalcBird(1);// 默认乘法 翻倍
        this.fanshuLimit = StringUtil.getIntValue(params, 2, 0);// 番数上限  默认无上限
        this.hasMenQing = StringUtil.getIntValue(params, 3, 0);// 是否有门清 默认无门清
        this.birdNum = StringUtil.getIntValue(params, 4, 1); // 抓鸟抓中自己 1 5 9所有玩家翻倍    3 7对门翻倍  2 6下家翻倍  4 8上家翻倍
        this.yizhiqiao = StringUtil.getIntValue(params, 5, 0);// 一字撬是否有喜  默认没喜
        this.kaqiao = StringUtil.getIntValue(params, 6, 0);// 卡撬
        this.maxPlayerCount = StringUtil.getIntValue(params, 7, 4);// 比赛人数
        if (this.yizhiqiao == 0) {
            this.kaqiao = 0;
        }
        int payType = StringUtil.getIntValue(params, 10, 0);// 房费类型
        setPayType(payType);
        if (payType == 1) {// 1AA模式  2房主模式
            setAAConsume(true);
        }

        this.autoTime = StringUtil.getIntValue(params, 11, 0);     // 托管时间
        this.autoPlayGlob = StringUtil.getIntValue(params, 12, 0); // 全局托管
        if (autoTime > 0) {
            this.autoPlay = true;
        }

        this.jiaBei = StringUtil.getIntValue(params, 13, 0);        // 加倍
        this.jiaBeiFen = StringUtil.getIntValue(params, 14, 0);     // 加倍分
        this.jiaBeiShu = StringUtil.getIntValue(params, 15, 0);     // 加倍数

        if (this.maxPlayerCount == 2) {
            int belowAdd = StringUtil.getIntValue(params, 16, 0);   // 低于 below 加 belowAdd 分
            if (belowAdd <= 100 && belowAdd >= 0) {
                this.belowAdd = belowAdd;
            }
            int below = StringUtil.getIntValue(params, 17, 0);
            if (below <= 100 && below >= 0) {
                this.below = below;
                if (belowAdd > 0 && below == 0) {
                    this.below = 10;
                }
            }
        }

        this.chouPai = StringUtil.getIntValue(params, 18, 0);
        if (this.chouPai != 0 && this.chouPai != 13 && this.chouPai != 26) {
            this.chouPai = 13;
        }
        if (this.maxPlayerCount != 2) { // 2人玩法才有抽牌
            this.chouPai = 0;
        }

        this.maMaHu = StringUtil.getIntValue(params, 19, 1);
    }


    public static final List<Integer> wanfaList = Arrays.asList(GameUtil.play_type_yuanjiang);

    public static void loadWanfaTables(Class<? extends BaseTable> cls) {
        for (Integer integer : wanfaList) {
            TableManager.wanfaTableTypesPut(integer, cls);
        }
        HuUtil.init();
    }

    public String getTableMsg() {

        Map<String, Object> json = new HashMap<>();
        json.put("wanFa", "沅江麻将");
        if (isGroupRoom()) {
            json.put("roomName", getRoomName());
        }
        json.put("playerCount", getPlayerCount());
        json.put("count", getTotalBureau());
        if (autoPlay) {
            json.put("autoTime", autoTime / 1000);
            if (autoPlayGlob == 1) {
                json.put("autoName", "单局");
            } else {
                json.put("autoName", "整局");
            }
        }
        return JSON.toJSONString(json);
    }

    public int getAutoPlayGlob() {
        return autoPlayGlob;
    }

    public void setAutoPlayGlob(int autoPlayGlob) {
        this.autoPlayGlob = autoPlayGlob;
    }

    public int getAutoTableCount() {
        return autoTableCount;
    }

    public void setAutoTableCount(int autoTableCount) {
        this.autoTableCount = autoTableCount;
    }

    public int getAutoTime() {
        return autoTime;
    }

    public void setAutoTime(int autoTime) {
        this.autoTime = autoTime;
    }


    public void setTableStatus(int tableStatus) {
        this.tableStatus = tableStatus;
    }

    public int getTableStatus() {
        return tableStatus;
    }

    public void autoPiao(YjMjPlayer player) {
        int piaoPoint = 0;
        if (getTableStatus() != YjMjConstants.TABLE_STATUS_PIAO) {
            return;
        }
        if (player.getPiaoPoint() < 0) {
            player.setPiaoPoint(piaoPoint);
        } else {
            return;
        }
        sendPiaoPoint(player, piaoPoint);
        checkDeal(player.getUserId());
    }

    private void sendPiaoPoint(YjMjPlayer player, int piaoPoint) {
        ComMsg.ComRes.Builder build = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_piao_fen, player.getSeat(), piaoPoint);
        broadMsg(build.build());
        broadMsgRoomPlayer(build.build());
    }

    public int getJiaBei() {
        return jiaBei;
    }

    public void setJiaBei(int jiaBei) {
        this.jiaBei = jiaBei;
    }

    public int getJiaBeiFen() {
        return jiaBeiFen;
    }

    public void setJiaBeiFen(int jiaBeiFen) {
        this.jiaBeiFen = jiaBeiFen;
    }

    public int getJiaBeiShu() {
        return jiaBeiShu;
    }

    public void setJiaBeiShu(int jiaBeiShu) {
        this.jiaBeiShu = jiaBeiShu;
    }

    public void sendTingInfo(YjMjPlayer player) {
        long start = System.currentTimeMillis();
        boolean jjHu = player.allOutMjIsJiang();
        boolean mmHu = this.maMaHu == 1 && player.allOutMjIsMa();
        if (player.isAlreadyMoMajiang()) {
            if (actionSeatMap.containsKey(player.getSeat())) {
                return;
            }
            PlayCardResMsg.DaPaiTingPaiRes.Builder tingInfo = PlayCardResMsg.DaPaiTingPaiRes.newBuilder();
            List<YjMj> cards = new ArrayList<>(player.getHandMajiang());
            int hzCount = YjMjTool.dropHongzhong(cards).size();
            int[] cardArr = HuUtil.toCardArray(cards);
            Map<Integer, List<YjMj>> checked = new HashMap<>();
            for (YjMj card : cards) {
                if (card.isHongzhong()) {
                    continue;
                }
                List<YjMj> lackPaiList;
                if (checked.containsKey(card.getVal())) {
                    lackPaiList = checked.get(card.getVal());
                } else {
                    int cardIndex = HuUtil.getMjIndex(card);
                    cardArr[cardIndex] = cardArr[cardIndex] - 1;
                    lackPaiList = YjMjTool.getLackList(cardArr, hzCount, true, jjHu, mmHu);
                    cardArr[cardIndex] = cardArr[cardIndex] + 1;
                    if (lackPaiList.size() > 0) {
                        checked.put(card.getVal(), lackPaiList);
                    } else {
                        continue;
                    }
                }

                PlayCardResMsg.DaPaiTingPaiInfo.Builder ting = PlayCardResMsg.DaPaiTingPaiInfo.newBuilder();
                ting.setMajiangId(card.getId());
                if (lackPaiList.size() == 1 && null == lackPaiList.get(0)) {
                    //听所有
                    ting.addTingMajiangIds(YjMj.mj201.getId());
                } else {
                    for (YjMj lackPai : lackPaiList) {
                        ting.addTingMajiangIds(lackPai.getId());
                    }
                }
                tingInfo.addInfo(ting.build());
            }
            if (tingInfo.getInfoCount() > 0) {
                player.writeSocket(tingInfo.build());
            }
        } else {
            List<YjMj> cards = new ArrayList<>(player.getHandMajiang());
            int hzCount = YjMjTool.dropHongzhong(cards).size();
            int[] cardArr = HuUtil.toCardArray(cards);
            List<YjMj> lackPaiList = YjMjTool.getLackList(cardArr, hzCount, true, jjHu, mmHu);
            if (lackPaiList == null || lackPaiList.size() == 0) {
                return;
            }
            PlayCardResMsg.TingPaiRes.Builder ting = PlayCardResMsg.TingPaiRes.newBuilder();
            if (lackPaiList.size() == 1 && null == lackPaiList.get(0)) {
                //听所有
                ting.addMajiangIds(YjMj.mj201.getId());
            } else {
                for (YjMj lackPai : lackPaiList) {
                    ting.addMajiangIds(lackPai.getId());
                }
            }
            player.writeSocket(ting.build());
        }
        long timeUse = (System.currentTimeMillis() - start);
        if (timeUse > 50) {
            StringBuilder sb = new StringBuilder("sendTingInfo");
            sb.append("|").append(timeUse);
            sb.append("|").append(getId());
            sb.append("|").append(getPlayedBureau());
            sb.append("|").append(player.getUserId());
            sb.append("|").append(player.getSeat());
            sb.append("|").append(player.getHandMajiang());
            LogUtil.msgLog.info(sb.toString());
        }
    }

    public void logFaPaiTable() {
        StringBuilder sb = new StringBuilder();
        sb.append("YjMj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append("faPai");
        sb.append("|").append(playType);
        sb.append("|").append(maxPlayerCount);
        sb.append("|").append(getPayType());
        sb.append("|").append(lastWinSeat);
        LogUtil.msg(sb.toString());
    }

    public void logFaPaiPlayer(YjMjPlayer player, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("YjMj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append("faPai");
        sb.append("|").append(player.getHandMajiang());
        sb.append("|").append(actListToString(actList));
        LogUtil.msg(sb.toString());
    }

    public void logAction(YjMjPlayer player, int action, List<YjMj> mjs, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("YjMj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        String actStr = "unKnown-" + action;
        switch (action) {
            case YjMjDisAction.action_peng:
                actStr = "peng";
                break;
            case YjMjDisAction.action_minggang:
                actStr = "mingGang";
                break;
            case YjMjDisAction.action_chupai:
                actStr = "chuPai";
                break;
            case YjMjDisAction.action_pass:
                actStr = "guo";
                break;
            case YjMjDisAction.action_angang:
                actStr = "anGang";
                break;
            case YjMjDisAction.action_chi:
                actStr = "chi";
                break;
            case YjMjDisAction.action_jiegang:
                actStr = "jieGang";
                break;
            case YjMjDisAction.action_baoting:
                actStr = "baoTing";
                break;
            case YjMjDisAction.action_haodilaoPass:
                actStr = "guoHaiDi";
                break;
        }
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append(player.isAutoPlaySelf() ? 1 : 0);
        sb.append("|").append(actStr);
        sb.append("|").append(mjs);
        sb.append("|").append(actListToString(actList));
        LogUtil.msg(sb.toString());
    }

    public void logMoMj(YjMjPlayer player, YjMj mj, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("YjMj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append("moPai");
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append(player.isAutoPlaySelf() ? 1 : 0);
        sb.append("|").append(leftMajiangs.size());
        sb.append("|").append(mj);
        sb.append("|").append(actListToString(actList));
        sb.append("|").append(player.getHandPais());
        LogUtil.msg(sb.toString());
    }

    public void logChuPaiActList(YjMjPlayer player, YjMj mj, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("YjMj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append("chuPaiActList");
        sb.append("|").append(mj);
        sb.append("|").append(actListToString(actList));
        sb.append("|").append(player.getHandPais());
        LogUtil.msg(sb.toString());
    }

    public void logActionHu(YjMjPlayer player, List<YjMj> mjs) {
        StringBuilder sb = new StringBuilder();
        sb.append("YjMj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append("huPai");
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append(player.isAutoPlaySelf() ? 1 : 0);
        sb.append("|").append(mjs);
        sb.append("|").append(player.getDaHuNames());
        LogUtil.msg(sb.toString());
    }

    public void logHuPoint(YjMjPlayer player) {
        StringBuilder sb = new StringBuilder();
        sb.append("YjMj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append("huPoint");
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append(player.isAutoPlaySelf() ? 1 : 0);
        sb.append("|").append(player.getPoint());
        sb.append("|").append(player.getTotalPoint());
        sb.append("|").append(player.getHandPais());
        LogUtil.msg(sb.toString());
    }

    public void logQiangGangHu(YjMjPlayer player, List<YjMj> mjs, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("YjMj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append("qiangGangHu");
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append(player.isAutoPlaySelf() ? 1 : 0);
        sb.append("|").append(mjs);
        sb.append("|").append(actListToString(actList));
        sb.append("|").append(player.getHandMajiang());
        LogUtil.msg(sb.toString());
    }

    public String actListToString(List<Integer> actList) {
        if (actList == null || actList.size() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < actList.size(); i++) {
            if (actList.get(i) == 1) {
                if (sb.length() > 1) {
                    sb.append(",");
                }
                switch (i) {
                    case YjMjConstants.ACTION_INDEX_HU:
                        sb.append("hu");
                        break;
                    case YjMjConstants.ACTION_INDEX_PENG:
                        sb.append("peng");
                        break;
                    case YjMjConstants.ACTION_INDEX_MINGGANG:
                        sb.append("mingGang");
                        break;
                    case YjMjConstants.ACTION_INDEX_ANGANG:
                        sb.append("anGang");
                        break;
                    case YjMjConstants.ACTION_INDEX_JIEGANG:
                        sb.append("jieGang");
                        break;
                    case YjMjConstants.ACTION_INDEX_GANGBAO:
                        sb.append("gangBao");
                        break;
                    case YjMjConstants.ACTION_INDEX_BAOTING:
                        sb.append("baoTing");
                        break;
                    default:
                        sb.append(i);
                        break;
                }
            }
        }
        sb.append("]");
        return sb.toString();
    }

    public int getChouPai() {
        return chouPai;
    }

    public void setChouPai(int chouPai) {
        this.chouPai = chouPai;
    }

    public List<Integer> getChouCards() {
        return chouCards;
    }

    public void setChouCards(List<Integer> chouCards) {
        this.chouCards = chouCards;
    }

    public boolean canMaMaHu() {
        return maMaHu == 1;
    }
}