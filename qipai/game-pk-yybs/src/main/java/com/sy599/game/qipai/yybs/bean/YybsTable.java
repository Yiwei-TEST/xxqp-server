package com.sy599.game.qipai.yybs.bean;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sy.mainland.util.CommonUtil;
import com.sy599.game.GameServerConfig;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.common.constant.SharedConstants.player_state;
import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.db.bean.DataStatistics;
import com.sy599.game.db.bean.TableInf;
import com.sy599.game.db.bean.UserPlaylog;
import com.sy599.game.db.dao.DataStatisticsDao;
import com.sy599.game.db.dao.TableDao;
import com.sy599.game.db.dao.TableLogDao;
import com.sy599.game.manager.TableManager;
import com.sy599.game.msg.serverPacket.ComMsg.ComRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.PlayCardRes;
import com.sy599.game.msg.serverPacket.TableRes.*;
import com.sy599.game.qipai.yybs.constant.YybsConstants;
import com.sy599.game.qipai.yybs.tool.CardTool;
import com.sy599.game.qipai.yybs.util.CardType;
import com.sy599.game.qipai.yybs.util.CardUtils;
import com.sy599.game.udplog.UdpLogger;
import com.sy599.game.util.*;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class YybsTable extends BaseTable {
    public static final String GAME_CODE = "Yybs";
    private static final int JSON_TAG = 1;
    /*** 当前牌桌上出的牌 */
    private volatile List<Integer> nowDisCardIds = new ArrayList<>();
    /*** 玩家map */
    private Map<Long, YybsPlayer> playerMap = new ConcurrentHashMap<Long, YybsPlayer>();
    /*** 座位对应的玩家 */
    private Map<Integer, YybsPlayer> seatMap = new ConcurrentHashMap<Integer, YybsPlayer>();
    /*** 最大玩家数量 */
    private volatile int maxPlayerCount = 3;

    private volatile int showCardNumber = 0; // 是否显示剩余牌数量

    public static final int FAPAI_PLAYER_COUNT = 3;// 发牌人数
    public  volatile  int restartFapai=0;
    private volatile int timeNum = 0;

    /**
     * 双进单出
     */
    private int shuangjinDC;
    /**
     * 报副留守
     */
    private int baofuLS;

    /**
     * 投降询问
     */
    private int touxiangXW;

    /**
     * 带红2  1不带  2带
     */
    private int option_red2;

    /**
     * 反主次数
     */
    private int option_fanzhunum;

    /**
     * 0=小反125大反155  1=小反130大反160
     */
    private int option_fanzhuFen;
    /**
     * 允许查牌
     */
    private int checkPai;
    /**
     * 抽6
     */
    private int chouLiu;

    private List<Integer> teamSeat = new ArrayList<>();// 队伍。

    private String seatJsonStr ="";
    /** 上把庄*/
//	private int lastbanker;


    /**
     * 庄是否1v3
     */
    private int is_1v3 = 0;
    /**
     * 基础底分 反主+1
     */
    private volatile int baseScore = 0;
    private volatile int fanzhuNum = 0;


    /**
     * 托管1：单局，2：全局
     */
    private int autoPlayGlob;
    private int autoTableCount;


    // 新的一轮，3人为2人pass之后为新的一轮出牌
    private boolean newRound = true;
    // pass累计
    /**
     * 托管时间
     */
    private volatile int autoTimeOut = 5 * 24 * 60 * 60 * 1000;
    private volatile int autoTimeOut2 = 5 * 24 * 60 * 60 * 1000;

    // 是否已经发牌
    private int finishFapai = 0;
    // 是否已经飘分
    private int finishiPiaofen = 0;
    // 是否已经抢主
    private volatile int finishQz = 0;
    // 一轮出的牌都会临时存在这里
    private volatile List<PlayerCard> noPassDisCard = new ArrayList<>();
    // 回放手牌
    private volatile String replayDisCard = "";

    private List<Integer> dipai = new ArrayList<>();// 底牌
    private List<Integer> zhuoFen = new ArrayList<>();// 捉的分
    private List<Integer> turnWinSeats = new ArrayList<>();
    /**
     * 叫的分
     **/
    private int jiaoFen = 80;
    private String jiaozhuCardStr = "";
    private boolean isPai = false;// 是否加拍
    private int zhuColor = -1;//  方片 1 梅花2 红3 黑桃4
    private int banker = 0;// 庄家座位

    private int turnFirstSeat = 0;// 当前轮第一个出牌的座位
    private int disColor; //轮首次出的花色 方片 1 梅花2 洪涛3 黑桃4  5主牌
    private int turnNum;//回合，每出一轮为1回合
//	private int firstJiaofenSeat;//第一个叫庄的人

    /**
     * 特殊状态 1
     **/
    private int tableStatus = 0;
    // 低于below加分
    private int belowAdd = 0;
    private int below = 0;


    private long touxiangTime;


    public String getReplayDisCard() {
        return replayDisCard;
    }

    public void setReplayDisCard(String replayDisCard) {
        this.replayDisCard = replayDisCard;
    }


    @Override
    protected void loadFromDB1(TableInf info) {
        if (!StringUtils.isBlank(info.getNowDisCardIds())) {
            this.nowDisCardIds = StringUtil.explodeToIntList(info.getNowDisCardIds());
        }
        if (!StringUtils.isBlank(info.getHandPai9())) {
            this.dipai = StringUtil.explodeToIntList(info.getHandPai9());
        }
        if (!StringUtils.isBlank(info.getHandPai10())) {
            this.zhuoFen = StringUtil.explodeToIntList(info.getHandPai10());
        }
        if (!StringUtils.isBlank(info.getOutPai9())) {
            this.turnWinSeats = StringUtil.explodeToIntList(info.getOutPai9());
        }

    }

    public long getId() {
        return id;
    }

    public YybsPlayer getPlayer(long id) {
        return playerMap.get(id);
    }


    /**
     * 一局结束
     */
    public void calcOver() {
        int Adddifen = 0;
        boolean koudi = false;
        int score = 0;
        boolean toux = isTouXiang();
        if (toux) {
            calTouxiFen();
        } else if (!autoPlayDiss) {

            if(is_1v3==1){
                if (turnFirstSeat != banker ) {
                    // 最后一手牌大的玩家扣底
                    Adddifen = checkKoudi();
                    if (Adddifen == -1) {
                        Adddifen = 0;
                    } else {
                        koudi = true;
                    }
                }
            }else if(is_1v3==2){
                //队友模式
                int bankteamfrend = getTeam(banker);
                if (turnFirstSeat != banker && turnFirstSeat!=bankteamfrend ) {
                    // 最后一手牌大的玩家扣底
                    Adddifen = checkKoudi();
                    if (Adddifen == -1) {
                        Adddifen = 0;
                    } else {
                        koudi = true;
                    }
                }
            }

            score = CardUtils.loadCardScore(zhuoFen);
            score += Adddifen;
            commonOver(score);
        }


        boolean isOver = playBureau >= totalBureau;
        if (autoPlayGlob > 0) {
            // //是否解散
            boolean diss = false;
            if (autoPlayGlob == 1) {
                for (YybsPlayer seat : seatMap.values()) {
                    if (seat.isAutoPlay()) {
                        diss = true;
                        break;
                    }
                }
            } else if (autoPlayGlob == 3) {
                diss = checkAuto3();
            }

            if (diss || autoPlayDiss) {
                autoPlayDiss = true;
                isOver = true;
            }
        }
        calcAfter();
        ClosingInfoRes.Builder res = sendAccountsMsg(isOver, false, score, koudi, Adddifen, toux);
        saveLog(isOver, 0, res.build());
        // setLastWinSeat(bankPlayer.getSeat());
        if (isOver) {
            calcOver1();
            calcOver2();
            calcOver3();
            diss();
//			for (Player player : seatMap.values()) {
//				player.saveBaseInfo();
//			}
        } else {
            initNext();
            calcOver1();
        }

    }

    /**
     * 单局 投降算分
     */
    private void calTouxiFen() {
        YybsPlayer losePlayer = seatMap.get(banker);
        if (touxiangXW == 1 || touxiangXW == 2) {
            int winScore = touxiangXW;
            int xianjia_piaofen = 0;
            for (YybsPlayer player : seatMap.values()) {
                if (player.getSeat() == banker) {
                    continue;
                }

                player.calcWin(1, winScore);


            }

            losePlayer.calcLost(1, -winScore * (maxPlayerCount - 1));

        } else {
            System.out.println(" ERROR ！ 投降算分参数touxiangXW错误");
        }

    }

    private boolean isTouXiang() {
        List<Integer> agreeTX = new ArrayList<Integer>();
        for (YybsPlayer player : seatMap.values()) {
            if (player.getTouXiang() == 2 || player.getTouXiang() == 1) {
                agreeTX.add(player.getTouXiang());
            }
        }
        if (agreeTX.size() == maxPlayerCount) {
            return true;
        } else if (touxiangXW == 2) {//直接投降
            return true;
        } else if (touxiangXW != 1 && agreeTX.contains(1)) {
            return true;
        }
        return false;
    }

    private void commonOver(int score) {
        boolean isWin = true;
        if (score >= 80) {
            isWin = false;
        }

        if (is_1v3 == 1) {
            if (isWin) {
                //庄赢
                int winScore = 1;
                if (score == 0) {
                    //大光
                    winScore = 3;
                } else if (score < 30 && score > 0) {
                    //小光
                    winScore = 2;
                } else if (score >= 30 && score < 80) {
                    //过庄
                    winScore = 1;
                }
                winScore = winScore * baseScore;
                YybsPlayer winPlayer = seatMap.get(banker);
                int total_piaofen = 0;
                for (YybsPlayer player : seatMap.values()) {
                    if (player.getSeat() == banker) {
                        continue;
                    }

                    player.calcLost(1, -winScore);

                }

                winPlayer.calcWin(1, winScore * (maxPlayerCount - 1));

                lastWinSeat = winPlayer.getSeat();
            } else {
                //闲赢
                int loseScore = 1;
//			0=小反125大反155  1=小反130大反160
                if (option_fanzhuFen == 0) {
                    if (score >= 155) {
                        loseScore = 3;
                    } else if (score >= 125 && score <= 155) {
                        loseScore = 2;
                    } else if (score >= 80 && score < 125) {
                        loseScore = 1;
                    }
                } else if (option_fanzhuFen == 1) {
                    if (score >= 160) {
                        loseScore = 3;
                    } else if (score >= 130 && score <= 160) {
                        loseScore = 2;
                    } else if (score >= 80 && score < 130) {
                        loseScore = 1;
                    }
                }
                loseScore = loseScore * baseScore;

                YybsPlayer losePlayer = seatMap.get(banker);
                int total_piaofen = 0;
                for (YybsPlayer player : seatMap.values()) {
                    if (player.getSeat() == banker) {
                        continue;
                    }
                    player.calcWin(1, loseScore);
                }
                losePlayer.calcLost(1, -loseScore * (maxPlayerCount - 1));
            }
        } else {
            if (isWin) {
                //庄赢
                int winScore = 1;
                if (score == 0) {
                    //大光
                    winScore = 3;
                } else if (score < 30 && score > 0) {
                    //小光
                    winScore = 2;
                } else if (score >= 30 && score < 80) {
                    //过庄
                    winScore = 1;
                }

                winScore = winScore * baseScore;
                YybsPlayer winPlayer = seatMap.get(banker);
                int friend = getTeam(banker);
                YybsPlayer winPlayer2 = seatMap.get(friend);
                int total_piaofen = 0;
                for (YybsPlayer player : seatMap.values()) {
                    if (player.getSeat() == banker || player.getSeat() == friend) {
                        continue;
                    }
                    player.calcLost(1, -winScore);
                }

                winPlayer.calcWin(1, winScore);
                winPlayer2.calcWin(1, winScore);
                lastWinSeat = winPlayer.getSeat();
            } else {
                //闲赢
                int loseScore = 1;
//			0=小反125大反155  1=小反130大反160
                if (option_fanzhuFen == 0) {
                    if (score >= 155) {
                        loseScore = 3;
                    } else if (score >= 125 && score <= 155) {
                        loseScore = 2;
                    } else if (score >= 80 && score < 125) {
                        loseScore = 1;
                    }
                } else if (option_fanzhuFen == 1) {
                    if (score >= 160) {
                        loseScore = 3;
                    } else if (score >= 130 && score <= 160) {
                        loseScore = 2;
                    } else if (score >= 80 && score < 130) {
                        loseScore = 1;
                    }
                }
                loseScore = loseScore * baseScore;
                YybsPlayer losePlayer = seatMap.get(banker);
                int friend = getTeam(banker);
                YybsPlayer losePlayer2 = seatMap.get(friend);
                for (YybsPlayer player : seatMap.values()) {
                    if (player.getSeat() == banker || player.getSeat() == friend) {
                        continue;
                    }
                    player.calcWin(1, loseScore);

                }
                losePlayer.calcLost(1, -loseScore);
                losePlayer2.calcLost(1, -loseScore);
            }
        }

//        System.out.println("庄:" + banker);
//        for (YybsPlayer player : seatMap.values()) {
//            System.out.println(player.getSeat() + " 飘分:" + player.getPiaofen() + " 小局:" + player.getPoint() + " 总:" + player.getTotalPoint() + "");
//        }
    }

    private int getTeam(int banker) {
        if (banker == 1) {
            return 3;
        } else if (banker == 2) {
            return 4;
        } else if (banker == 3) {
            return 1;
        } else if (banker == 4) {
            return 2;
        } else {
            return 0;
        }
    }


    private int checkKoudi() {
        int Adddifen;
        YybsPlayer lastPlayer = seatMap.get(turnFirstSeat);
        if (lastPlayer == null || lastPlayer.getHandPais().size() > 0) {
            return -1;
        }
        int addBei = 1;
        List<Integer> cards = lastPlayer.getCurOutCard(getTurnNum() - 1);
        if (!CardTool.allZhu(cards, zhuColor)) {
            return -1;
        }
        if (cards != null) {
            CardType ct = CardTool.getCardType(cards, zhuColor, isChouLiu());
            if (ct.getType() == CardType.DUI) {
                addBei = 2;
            } else if (ct.getType() == CardType.TUOLAJI) {
//				addBei = 4;
                addBei = cards.size();

            }
        }
        Adddifen = CardUtils.loadCardScore(dipai) * addBei;
        // 底牌
        PlayCardRes.Builder res = PlayCardRes.newBuilder();
        res.setUserId(lastPlayer.getUserId() + "");
        res.setSeat(turnFirstSeat);
        res.setIsPlay(2);
        res.setCardType(YybsConstants.RES_KOUDI);
        res.addAllCardIds(dipai);
        List<Integer> scoreCards = CardUtils.getScoreCards(dipai);
        if (scoreCards.size() > 0) {
            // zhuoFen.addAll(scoreCards);
            res.addAllScoreCard(scoreCards);
        }
        int tfen = CardUtils.loadCardScore(zhuoFen) + Adddifen;
        addPlayLog(addSandhPlayLog(0, YybsConstants.TABLE_KOUDI, dipai, false, tfen, scoreCards, getNowDisCardSeat(), ""));
        res.setCurScore(tfen);
        for (Player player : seatMap.values()) {
            player.writeSocket(res.build());
        }
        return Adddifen;
    }

    private boolean checkAuto3() {
        boolean diss = false;
//		if(autoPlayGlob==3) {
        boolean diss2 = false;
        for (YybsPlayer seat : seatMap.values()) {
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
//		}
        return diss;
    }

    @Override
    public void calcDataStatistics2() {
        // 俱乐部房间 单大局大赢家、单大局大负豪、总小局数、单大局赢最多、单大局输最多 数据统计
        if (isGroupRoom()) {
            String groupId = loadGroupId();
            int maxPoint = 0;
            int minPoint = 0;
            Long dataDate = Long.valueOf(new SimpleDateFormat("yyyyMMdd").format(new Date()));
            // 俱乐部活动总大局数
            calcDataStatistics3(groupId);

            //Long dataDate, String dataCode, String userId, String gameType, String dataType, int dataValue
            for (YybsPlayer player : playerMap.values()) {
                // 总小局数
                DataStatistics dataStatistics1 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "xjsCount", playedBureau);
                DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics1, 3);
                // 总大局数
                DataStatistics dataStatistics5 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "djsCount", 1);
                DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics5, 3);
                // 总积分
                DataStatistics dataStatistics6 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "zjfCount", player.loadScore());
                DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics6, 3);
                if (player.loadScore() > 0) {
                    if (player.loadScore() > maxPoint) {
                        maxPoint = player.loadScore();
                    }
                    // 单大局赢最多
                    DataStatistics dataStatistics2 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "winMaxScore", player.loadScore());
                    DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics2, 4);
                } else if (player.loadScore() < 0) {
                    if (player.loadScore() < minPoint) {
                        minPoint = player.loadScore();
                    }
                    // 单大局输最多
                    DataStatistics dataStatistics3 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "loseMaxScore", player.loadScore());
                    DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics3, 5);
                }
            }

            for (YybsPlayer player : playerMap.values()) {
                if (maxPoint > 0 && maxPoint == player.loadScore()) {
                    // 单大局大赢家
                    DataStatistics dataStatistics4 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "dyjCount", 1);
                    DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics4, 1);
                } else if (minPoint < 0 && minPoint == player.loadScore()) {
                    // 单大局大负豪
                    DataStatistics dataStatistics5 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "dfhCount", 1);
                    DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics5, 2);
                }
            }
        }
    }


    public void saveLog(boolean over, long winId, Object resObject) {
        ClosingInfoRes res = (ClosingInfoRes) resObject;
        LogUtil.d_msg("tableId:" + id + " play:" + playBureau + " over:" + res);
        String logRes = JacksonUtil.writeValueAsString(LogUtil.buildClosingInfoResLog(res));
        Map<String, Object> map = LogUtil.buildClosingInfoResOtherLog(res);
        map.put("intParams", getIntParams());
        String logOtherRes = JacksonUtil.writeValueAsString(map);
        Date now = TimeUtil.now();

        UserPlaylog userLog = new UserPlaylog();
        userLog.setUserId(creatorId);
        userLog.setLogId(playType);
        userLog.setTableId(id);
        userLog.setRes(extendLogDeal(logRes));
        userLog.setTime(now);
        userLog.setTotalCount(totalBureau);
        userLog.setCount(playBureau);
        userLog.setStartseat(lastWinSeat);
        userLog.setOutCards(playLog);
        userLog.setExtend(logOtherRes);
        userLog.setMaxPlayerCount(maxPlayerCount);
        userLog.setType(creditMode == 1 ? 2 : 1);
        userLog.setGeneralExt(buildGeneralExtForPlaylog().toString());
        long logId = TableLogDao.getInstance().save(userLog);
        saveTableRecord(logId, over, playBureau);

        if (!isGoldRoom()) {
            for (YybsPlayer player : playerMap.values()) {
                player.addRecord(logId, playBureau);
            }
        }

        UdpLogger.getInstance().sendSnapshotLog(masterId, playLog, logRes);
    }

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
                tempMap.put("outPai1", StringUtil.implodeLists(seatMap.get(1).getOutPais()));
            }
            if (tempMap.containsKey("outPai2")) {
                tempMap.put("outPai2", StringUtil.implodeLists(seatMap.get(2).getOutPais()));
            }
            if (tempMap.containsKey("outPai3")) {
                tempMap.put("outPai3", StringUtil.implodeLists(seatMap.get(3).getOutPais()));
            }
            if (tempMap.containsKey("outPai4")) {
                tempMap.put("outPai4", StringUtil.implodeLists(seatMap.get(4).getOutPais()));
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
            if (tempMap.containsKey("handPai9")) {
                tempMap.put("handPai9", StringUtil.implode(dipai, ","));
            }

            if (tempMap.containsKey("answerDiss")) {
                tempMap.put("answerDiss", buildDissInfo());
            }
            if (tempMap.containsKey("nowDisCardIds")) {
                tempMap.put("nowDisCardIds", StringUtil.implode(nowDisCardIds, ","));
            }
            if (tempMap.containsKey("extend")) {
                tempMap.put("extend", buildExtend());
                tempMap.put("handPai10", StringUtil.implode(zhuoFen, ","));
                tempMap.put("outPai9", StringUtil.implode(turnWinSeats, ","));
            }
//			TableDao.getInstance().save(tempMap);
        }
        return tempMap.size() > 0 ? tempMap : null;
    }

    @Override
    public JsonWrapper buildExtend0(JsonWrapper wrapper) {
//		JsonWrapper wrapper = new JsonWrapper("");
        wrapper.putInt(2, maxPlayerCount);
        wrapper.putInt(3, showCardNumber);
//		return wrapper.toString();
        wrapper.putString(5, replayDisCard);
        wrapper.putInt(6, autoTimeOut);
        wrapper.putInt(7, autoPlayGlob);
        wrapper.putInt(8, jiaoFen);
        wrapper.putInt(9, newRound ? 1 : 0);
        wrapper.putInt(10, finishFapai);
        wrapper.putInt(11, belowAdd);
        wrapper.putInt(12, below);
        wrapper.putInt(13, isPai ? 1 : 0);
        wrapper.putInt(14, zhuColor);
        wrapper.putInt(15, banker);
        wrapper.putInt(16, turnFirstSeat);
        wrapper.putInt(17, disColor);
        wrapper.putInt(18, turnNum);
        wrapper.putInt(19, tableStatus);
        wrapper.putInt(20, touxiangXW);
        wrapper.putInt(21, shuangjinDC);
        wrapper.putInt(22, baofuLS);
        wrapper.putInt(23, checkPai);
        wrapper.putInt(24, chouLiu);
        wrapper.putInt(25, 0);
        wrapper.putInt(26, 0);
        wrapper.putInt(27, 0);
        wrapper.putInt(28, 0);
        wrapper.putInt(29, 0);
        wrapper.putLong(30, touxiangTime);
        wrapper.putInt(31, option_red2);
        wrapper.putInt(32, option_fanzhunum);
        wrapper.putInt(33, option_fanzhuFen);
        wrapper.putString(34, jiaozhuCardStr);
        wrapper.putInt(35, is_1v3);
        wrapper.putInt(36, fanzhuNum);
        wrapper.putInt(37, baseScore);
        wrapper.putInt(38, finishQz);
        if(teamSeat.size()==0){
            wrapper.putString(39, "");
        } else if(teamSeat.size()==1){
            //1v3
            wrapper.putString(39, teamSeat.get(0)+"");
        }else if(teamSeat.size()==2){
            //结对
            wrapper.putString(39, "1,3");
        }
        wrapper.putString("40",seatJsonStr);
        return wrapper;
    }

    @Override
    public void initExtend0(JsonWrapper wrapper) {
//		JsonWrapper wrapper = new JsonWrapper(info);
        maxPlayerCount = wrapper.getInt(2, 3);
        if (maxPlayerCount == 0) {
            maxPlayerCount = 4;
        }
        showCardNumber = wrapper.getInt(3, 0);
        if (payType == -1) {
            String isAAStr = wrapper.getString("isAAConsume");
            if (!StringUtils.isBlank(isAAStr)) {
                this.payType = Boolean.parseBoolean(wrapper.getString("isAAConsume")) ? 1 : 2;
            } else {
                payType = 1;
            }
        }
        replayDisCard = wrapper.getString(5);
        autoTimeOut = wrapper.getInt(6, 0);
        autoPlayGlob = wrapper.getInt(7, 0);
        jiaoFen = wrapper.getInt(8, 0);
        newRound = wrapper.getInt(9, 1) == 1;
        finishFapai = wrapper.getInt(10, 0);
        belowAdd = wrapper.getInt(11, 0);
        below = wrapper.getInt(12, 0);
        autoTimeOut2 = autoTimeOut;
        // 设置默认值
        if (autoPlay && autoTimeOut <= 1) {
            autoTimeOut2 = autoTimeOut = 60000;
        }
        isPai = wrapper.getInt(13, 0) == 1;
        zhuColor = wrapper.getInt(14, 0);
        banker = wrapper.getInt(15, 0);
        turnFirstSeat = wrapper.getInt(16, 0);
        disColor = wrapper.getInt(17, 1);
        turnNum = wrapper.getInt(18, 0);
        tableStatus = wrapper.getInt(19, 0);
        touxiangXW = wrapper.getInt(20, 0);
        shuangjinDC = wrapper.getInt(21, 0);
        baofuLS = wrapper.getInt(22, 0);
        checkPai = wrapper.getInt(23, 0);
        chouLiu = wrapper.getInt(24, 0);
//		sixtyQJ=wrapper.getInt(25, 0);
//		xiaoguangF=wrapper.getInt(26, 0);
//		daDaoTQ=wrapper.getInt(27, 0);
//		jiaofenJP=wrapper.getInt(28, 0);
//		jiaofenJD=wrapper.getInt(29, 0);
        touxiangTime = wrapper.getLong(30, 0);
        option_red2 = wrapper.getInt(31, 0);
        option_fanzhunum = wrapper.getInt(32, 0);
        option_fanzhuFen = wrapper.getInt(33, 0);
        jiaozhuCardStr = wrapper.getString(34);
        is_1v3 = wrapper.getInt(35, 0);
        fanzhuNum = wrapper.getInt(36, 0);
        baseScore = wrapper.getInt(37, 1);
        finishQz = wrapper.getInt(38, 0);
        String teamSeatStr = wrapper.getString(39);

        if(null==teamSeatStr || "".equals(teamSeatStr)){
        } else if(teamSeatStr.length()==1){
            teamSeat.clear();
            teamSeat.add(Integer.valueOf(teamSeatStr));
        }else if("1,3".equals(teamSeatStr)){
            teamSeat.clear();
            teamSeat.add(1);
            teamSeat.add(3);
        }

        if(banker==0 && finishFapai==1 && getTableStatus()== YybsConstants.TABLE_STATUS_JIAOZHU && restartFapai==0){
            restartFapai=1;
            ReStartFaPaiThread();
        }
      //处理。重启换位置问题
        seatJsonStr  = wrapper.getString(40);
//        if(is_1v3==2 && !"".equals(seatJsonStr) && null!=seatJsonStr){
//            resetSeatMap();
//        }

    }
    public void resetSeatMap(){
        LinkedHashMap jon2 =  (LinkedHashMap) JSONUtils.parse(seatJsonStr);
        for(YybsPlayer p: playerMap.values()){
            //userid  seat
            Object index =  jon2.get(String.valueOf(p.getUserId()));
            int i = Integer.valueOf (index.toString());
            seatMap.put(i,playerMap.get(p.getUserId()));
        };

    }
    public  int getNewSeat(long userid){
        if(is_1v3==2 && !"".equals(seatJsonStr) && null!=seatJsonStr){
                LinkedHashMap jon2 =  (LinkedHashMap) JSONUtils.parse(seatJsonStr);
                Object index =  jon2.get(String.valueOf(userid));
                int i = Integer.valueOf (index.toString());
                return i;
        }

      return 0;
    }
    protected String buildPlayersInfo() {
        StringBuilder sb = new StringBuilder();
        for (YybsPlayer pdkPlayer : playerMap.values()) {
            sb.append(pdkPlayer.toInfoStr()).append(";");
        }
        // playerInfos = sb.toString();
        return sb.toString();
    }

    public void changePlayers() {
        dbParamMap.put("players", JSON_TAG);
    }

    public void changeCards(int seat) {
        dbParamMap.put("outPai" + seat, JSON_TAG);
        dbParamMap.put("handPai" + seat, JSON_TAG);
    }

    /**
     * 开始发牌
     */
    public void fapai() {
        playLog = "";
        synchronized (this) {
            changeTableState(table_state.play);
            timeNum = 0;
            List<List<Integer>> list;
            list = CardTool.fapai(maxPlayerCount, isChouLiu(), zp, isChouEr());
            int i = 0;

//            for (int j=1;i<maxPlayerCount;j++) {
//            	YybsPlayer player  =seatMap.get(j);
            for (YybsPlayer player : playerMap.values()) {
                //	: playerMap.values()
                player.changeState(player_state.play);
                player.dealHandPais(list.get(i), this);
                i++;

                if (!player.isAutoPlay()) {
                    player.setAutoPlay(false, this);
                    player.setLastOperateTime(System.currentTimeMillis());
                }
//
                StringBuilder sb = new StringBuilder("YiYangBaShi");
                sb.append("|").append(getId());
                sb.append("|").append(getPlayBureau());
                sb.append("|").append(player.getUserId());
                sb.append("|").append(player.getSeat());
                sb.append("|").append(player.getName());
                sb.append("|").append(player.isAutoPlay() ? 1 : 0);
                sb.append("|").append("fapai");
                sb.append("|").append(player.getHandPais());
                LogUtil.msgLog.info(sb.toString());
            }
            setDipai(list.get(i));
        }

        finishFapai = 1;
        noticeJiaofen();
        //起线程。20S之后如果没人抢主  则重新发牌
        ReStartFaPaiThread();

    }

    private void ReStartFaPaiThread() {
        final long yybstabid =	tableInf.getTableId();
        new Thread(new Runnable() {
            @Override
            public void run() {
                //需要执行的代码
                try {
//                    System.out.println(" 等待线程开启。20S后无法抢庄重新发牌！");
                    Thread.currentThread().sleep(20*1000);
                    if(banker==0 && yybstabid== tableInf.getTableId()){
                        fapai();
                        for (int i = 1; i <= getMaxPlayerCount(); i++) {
                            Player player = getSeatMap().get(i);
                            addPlayLog(StringUtil.implode(player.getHandPais(), ","));
                        }
                    }else{
                        return;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } }).start();
    }

    @Override
    public int getNextDisCardSeat() {
        if (disCardSeat == 0) {
            return banker;
        }
        return calcNextSeat(disCardSeat);
    }

    /**
     * 计算seat右边的座位
     *
     * @param seat
     * @return
     */
    public int calcNextSeat(int seat) {
        int nextSeat = seat + 1 > maxPlayerCount ? 1 : seat + 1;
        return nextSeat;
    }

    public YybsPlayer getPlayerBySeat(int seat) {
        //int next = seat >= maxPlayerCount ? 1 : seat + 1;
        return seatMap.get(seat);

    }

    private void addGameActionLog(Player player, String str) {

        StringBuilder sb = new StringBuilder("YiYangBaShi");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append(player.getName());
        sb.append("|").append(str);
        LogUtil.msgLog.info(sb.toString());
    }

    public Map<Integer, Player> getSeatMap() {
        Object o = seatMap;
        return (Map<Integer, Player>) o;
    }


    @Override
    public CreateTableRes buildCreateTableRes(long userId, boolean isrecover, boolean isLastReady) {
        CreateTableRes.Builder res = CreateTableRes.newBuilder();
        buildCreateTableRes0(res);
        synchronized (this) {
            res.setNowBurCount(getPlayBureau());
            res.setTotalBurCount(getTotalBureau());
            res.setGotyeRoomId(gotyeRoomId + "");
            res.setTableId(getId() + "");
            res.setWanfa(playType);
            List<PlayerInTableRes> players = new ArrayList<>();
            for (YybsPlayer player : playerMap.values()) {
                PlayerInTableRes.Builder playerRes = player.buildPlayInTableInfo(userId, isrecover);
                if (playerRes == null) {
                    continue;
                }
                if (player.getUserId() == userId) {
                    // 如果是自己重连能看到手牌
                    playerRes.addAllHandCardIds(player.getHandPais());
                } else {
                    // 如果是别人重连，轮到出牌人出牌时要不起可以去掉
                }
                if (player.getSeat() == banker && getTableStatus() == YybsConstants.TABLE_STATUS_PLAY) {
                    playerRes.addAllMoldIds(dipai);
                }

                if (player.getSeat() == disCardSeat && nowDisCardIds != null && nowDisCardIds.size() > 0) {
                    //  playerRes.addAllOutCardIds(nowDisCardIds);
//                    playerRes.addRecover(cardType);
                }
                players.add(playerRes.build());
            }
            res.addAllPlayers(players);
            //int nextSeat = getNextDisCardSeat();

//            if(getTableStatus() == YybsConstants.TABLE_STATUS_JIAOZHU) {
//            	nextSeat = getNextActionSeat();
//            }
            if (nowDisCardSeat != 0) {
                res.setNextSeat(nowDisCardSeat);
            }

            //桌状态 1叫分2选主3埋牌 10抢主
            res.setRemain(getTableStatus());
            res.addAllScoreCard(zhuoFen);
            res.setRenshu(this.maxPlayerCount);

            res.addExt(this.payType);//0支付方式
            res.addExt(jiaoFen);//1牌桌叫的分
            res.addExt(zhuColor);//2叫的主的花色
            res.addExt(banker);//3庄的座位号
            res.addExt(isPai ? 1 : 0);//4是否加拍1：加拍
            res.addExt(CardUtils.loadCardScore(zhuoFen));//5分值

            res.addExt(CommonUtil.isPureNumber(modeId) ? Integer.parseInt(modeId) : 0);//6
            int ratio;
            int pay;

            ratio = 1;
            pay = consumeCards() ? loadPayConfig(payType) : 0;
            res.addExt(ratio);//6
            res.addExt(pay);//7
            res.addExt(lastWinSeat);//8

            res.addExtStr(String.valueOf(matchId));//0
            res.addExtStr(cardMarkerToJSON());//1
            res.addTimeOut(autoPlay ? autoTimeOut : 0);//9
//			if (autoPlay) {
//				if (disCardRound == 0) {
//					res.addTimeOut((autoTimeOut + 5000));
//				} else {
//					res.addTimeOut(autoTimeOut);
//				}
//			} else {
//				res.addTimeOut(0);
//			}

//            res.addExt(playedBureau);//11
//            res.addExt(disCardRound);//12
//            res.addExt(creditMode); //14
//            res.addExt(creditCommissionMode1);//19
//            res.addExt(creditCommissionMode2);//20
//            res.addExt(autoPlay ? 1 : 0);//21
//            res.addExt(tableStatus);//25
            res.addExt(playedBureau);//10
            res.addExt(disCardRound);//11
            res.addExt(creditMode); //12
            res.addExt(creditCommissionMode1);//13
            res.addExt(creditCommissionMode2);//14
            res.addExt(autoPlay ? 1 : 0);//15
            res.addExt(tableStatus);//16
            res.addExt(finishQz);//17  1已结束抢主  0未结束抢主
            res.addExt(is_1v3);//18  1独占 2结队 0没选
            res.addExt(baseScore);//19
            res.addStrExt(jiaozhuCardStr);//0 当前抢主牌
            res.addStrExt(banker + "");//1 当前座位号
        }

        return res.build();
    }

    public int getOnTablePlayerNum() {
        int num = 0;
        for (YybsPlayer player : seatMap.values()) {
            if (player.getIsLeave() == 0) {
                num++;
            }
        }
        return num;
    }


    /**
     * 出牌
     *
     * @param player
     * @param cards
     */
    public void disCards(YybsPlayer player, List<Integer> cards) {
        setDisCardSeat(player.getSeat());
        if (turnFirstSeat == player.getSeat()) {
            //插画
            int res = CardTool.checkCardValue(player.getHandPais(), cards, zhuColor, disColor, true, isChouLiu());
            if (res < 0) {
                player.writeErrMsg("出牌不符合规则。");
                return;
            } else {
                int type = res % 10;
                if (type >= CardType.SHUAIPAI) {
                    for (YybsPlayer p : seatMap.values()) {
                        if (turnFirstSeat == p.getSeat()) {
                            continue;
                        }
                        if (p.getBaofu() == 0) {
                            player.writeErrMsg("其他还有玩家没报副，不能甩牌");
                            return;
                        }
                    }
                }
                disColor = res;
            }
        } else {
            YybsPlayer bankP = seatMap.get(turnFirstSeat);
            List<Integer> list = bankP.getCurOutCard(getTurnNum());
            if (list.size() != cards.size()) {
                player.writeErrMsg("出牌不符合规则。");
                return;
            }
            int res = CardTool.checkCardValue(player.getHandPais(), cards, zhuColor, disColor, false, isChouLiu());
            if (res < 0) {
                player.writeErrMsg("出牌不符合规则。");
                return;
            }
        }
        player.addOutPais(cards, this);
        setDisCardSeat(player.getSeat());
        int nextSeat = getNextDisCardSeat();

        // 构建出牌消息
        PlayCardRes.Builder res = PlayCardRes.newBuilder();
        res.setIsClearDesk(0);
        res.setCardType(0);
        boolean isOver = false;
        if (turnFirstSeat != player.getSeat()) {
            List<Integer> firstList = seatMap.get(turnFirstSeat).getCurOutCard(getTurnNum());
            boolean firstZhu = CardTool.allZhu(firstList, zhuColor);
            int baofu = 0;
            if (firstZhu) {
                baofu += checkBaofu(firstZhu, player, cards);
            }
            res.setIsLet(baofu);
        }
        if (nextSeat == turnFirstSeat) {// 一轮打完
            // 1.算一轮哪家大
            // 2.下一轮谁出牌
            // 3.报副状态设置
            isOver = turnOver(res, player, cards);
        } else {
            setNowDisCardSeat(getNextDisCardSeat());
            addPlayLog(addSandhPlayLog(player.getSeat(), YybsConstants.TABLE_STATUS_PLAY, cards, player.getBaofu() == 1 ? true : false, 0, null, getNextDisCardSeat(), ""));
        }


        setNowDisCardIds(cards);
        if (cards != null) {
            noPassDisCard.add(new PlayerCard(player.getName(), cards));
        }
        res.addAllCardIds(getNowDisCardIds());
        res.setNextSeat(getNowDisCardSeat());
        res.setUserId(player.getUserId() + "");
        res.setSeat(player.getSeat());
        res.setIsPlay(2);
        setReplayDisCard();
        for (YybsPlayer p : seatMap.values()) {
            p.writeSocket(res.build());
        }

        if (isOver) {
            state = table_state.over;
        }

    }


    private boolean turnOver(PlayCardRes.Builder res, YybsPlayer player, List<Integer> cards) {
        boolean isOver = true;
        HashMap<Integer, CardType> pmap = new HashMap<Integer, CardType>();

        for (YybsPlayer p : seatMap.values()) {
            List<Integer> list = p.getCurOutCard(getTurnNum());
            pmap.put(p.getSeat(), CardTool.getCardType(list, zhuColor, isChouLiu()));
            if (p.getHandPais().size() != 0) {
                isOver = false;
            }
        }
        CardType result = CardTool.getTunWin(pmap, turnFirstSeat, zhuColor);
        List<Integer> fenCards = null;
        if (is_1v3 == 1) {
            if (result.getCardIds().size() > 0 && result.getType() != banker) {
                zhuoFen.addAll(result.getCardIds());
                res.addAllScoreCard(result.getCardIds());
                fenCards = result.getCardIds();
            }
        } else if (is_1v3 == 2) {
            int bankteamfrend = getTeam(banker);
            if (result.getCardIds().size() > 0 && result.getType() != banker && result.getType() != bankteamfrend) {
                zhuoFen.addAll(result.getCardIds());
                res.addAllScoreCard(result.getCardIds());
                fenCards = result.getCardIds();
            }
        }
//		if(result.getCardIds().size()>0&&result.getType()!=banker && ) {
//			zhuoFen.addAll(result.getCardIds());
//			res.addAllScoreCard(result.getCardIds());
//			fenCards = result.getCardIds();
//		}

        int totalScore = CardUtils.loadCardScore(zhuoFen);
        res.setCurScore(totalScore);
        res.setIsClearDesk(1);

        addPlayLog(addSandhPlayLog(player.getSeat(), YybsConstants.TABLE_STATUS_PLAY, cards, player.getBaofu() == 1 ? true : false, totalScore, fenCards, result.getType(), ""));

        setNowDisCardSeat(result.getType());
        setTurnFirstSeat(result.getType());
        turnWinSeats.add(result.getType());
        addTurnNum(1);
        disColor = 0;

        return isOver;
    }


    private CardType sortTurn() {
        HashMap<Integer, CardType> pmap = new HashMap<Integer, CardType>();
        for (YybsPlayer p : seatMap.values()) {
            List<Integer> list = p.getCurOutCard(getTurnNum());
            if (list == null) {
                continue;
            }
            pmap.put(p.getSeat(), CardTool.getCardType(list, zhuColor, isChouLiu()));
        }
        CardType result = CardTool.getTunWin(pmap, turnFirstSeat, zhuColor);
        return result;
    }


    private int checkBaofu(boolean firstZhu, YybsPlayer p, List<Integer> list) {
        if (p.getBaofu() == 1) {
            return CardTool.getBaofuValue(p.getSeat());
        }
        int baofu = 0;
        if (firstZhu && p.getSeat() != turnFirstSeat) {
            boolean oZhu = CardTool.allZhu(list, zhuColor);
            if (!oZhu) {//报副
                p.setBaofu(1);
                baofu += CardTool.getBaofuValue(p.getSeat());
            }
        }
        return baofu;
    }


    public void setReplayDisCard() {
        List<PlayerCard> cards = new ArrayList<>();
        int size = noPassDisCard.size();
        for (int i = 0; i < 3 && i < size; i++) {
            cards.add(noPassDisCard.get(size - 1 - i));
        }
        setReplayDisCard(cards.toString());
        noPassDisCard.clear();
    }


    /**
     * 打牌
     *
     * @param player
     * @param cards
     */
    public void playCommand(YybsPlayer player, int action, List<Integer> cards) {
        synchronized (this) {
            if (state != table_state.play) {
                return;
            }
            //100111
            if (action == YybsConstants.REQ_MAIPAI) {
                playMaipai(player, cards);
                return;
            }

            //出牌阶段
            if (getTableStatus() != YybsConstants.TABLE_STATUS_PLAY) {
                return;
            }

            if (!containCards(player.getHandPais(), cards)) {
                return;
            }

            StringBuilder sb = new StringBuilder("YiYangBaShi");
            sb.append("|").append(getId());
            sb.append("|").append(getPlayBureau());
            sb.append("|").append(player.getUserId());
            sb.append("|").append(player.getSeat());
            sb.append("|").append(player.isAutoPlay() ? 1 : 0);
            sb.append("|").append("chuPai");
            sb.append("|").append(cards);
            LogUtil.msgLog.info(sb.toString());
            if (cards != null && cards.size() > 0) {
                changeDisCardRound(1);
                // 出牌了
                disCards(player, cards);
            } else {
                if (disCardRound > 0) {
                    changeDisCardRound(1);
                }
            }
            setLastActionTime(TimeUtil.currentTimeMillis());
            if (isOver()) {
                calcOver();
            } else {
                int nextSeat = calcNextSeat(player.getSeat());
                YybsPlayer nextPlayer = seatMap.get(nextSeat);
                if (!nextPlayer.isRobot()) {
                    nextPlayer.setNextAutoDisCardTime(TimeUtil.currentTimeMillis() + autoTimeOut);
                }
            }
        }
    }

    private boolean containCards(List<Integer> handCards, List<Integer> cards) {
        for (Integer id : cards) {
            if (!handCards.contains(id)) {
                return false;
            }
        }
        return true;

    }

    /**
     * @param player
     * @param cards
     */
    private void playMaipai(YybsPlayer player, List<Integer> cards) {
//        if ((!isChouLiu() && cards.size() != 8) || (getMaxPlayerCount() == 3 && isChouLiu() && cards.size() != 9)) {
//            return;
//        }
        if(cards.size()!=8){
            return;
        }
        if (getTableStatus() != YybsConstants.TABLE_STATUS_MAIPAI) {
            addGameActionLog(player, "NoMaiPaiState");
            return;
        }
        setDipai(cards);
        addPlayLog(addSandhPlayLog(player.getSeat(), YybsConstants.TABLE_STATUS_MAIPAI, cards, false, 0, null, player.getSeat(), ""));
//		dipai = cards;
        PlayCardRes.Builder res = PlayCardRes.newBuilder();
        res.setUserId(player.getUserId() + "");
        res.setSeat(player.getSeat());
        res.setCardType(YybsConstants.REQ_MAIPAI);
        res.setIsPlay(1);
        res.setNextSeat(player.getSeat());
        res.addAllCardIds(cards);

        for (YybsPlayer sanPlayer : seatMap.values()) {
            sanPlayer.writeSocket(res.build());
        }
        for (Integer id : dipai) {
            player.getHandPais().remove(id);
//			player.removeHandPais(dipai);
        }
        Collections.sort(player.getHandPais());
//        System.out.println( player.getSeat()+"埋完手牌："+ player.getHandPais());
//        for (YybsPlayer sanPlayer : seatMap.values()) {
//           Collections.sort(sanPlayer.getHandPais());
//           System.out.println(sanPlayer.getSeat()+"   "+ sanPlayer.getHandPais().toString());
//        }
        changeCards(player.getSeat());//刷新DB缓存
        addTurnNum(1);
        setTurnFirstSeat(player.getSeat());
        setTableStatus(YybsConstants.TABLE_STATUS_XUANDUI);
    }


    public String addSandhPlayLog(int seat, int action, List<Integer> cards, boolean baofu, int fen, List<Integer> fenCards, int nextSeat, String newseat) {
        JSONObject json = new JSONObject();
        json.put("seat", seat);
        json.put("action", action);
        json.put("vals", cards);
        if (baofu) {
            json.put("baofu", 1);
        }
        json.put("fen", fen);
        if (fenCards != null) {
            json.put("fenCards", fenCards);
        }
        json.put("nextSeat", nextSeat);
        if (!"".equals(newseat)) {
            json.put("newseat", newseat);
        }
        return json.toJSONString();
    }

    public String addSandhPlayLogNewSeat(int seat, int action, List<Integer> cards, boolean baofu, int fen, List<Integer> fenCards, int nextSeat, JSONObject newseat) {
        JSONObject json = new JSONObject();
        json.put("seat", seat);
        json.put("action", action);
        json.put("vals", cards);
        if (baofu) {
            json.put("baofu", 1);
        }
        json.put("fen", fen);
        if (fenCards != null) {
            json.put("fenCards", fenCards);
        }
        json.put("nextSeat", nextSeat);
        json.put("newseat", newseat);
        return json.toJSONString();
    }

    public void playXuanzhu(YybsPlayer player, int zhu) {
        if (nowDisCardSeat != player.getSeat()) {
            LogUtil.msgLog.info("now actionseat is error  + nowDisCardSeat = " + nowDisCardSeat + "actionSeat = " + player.getSeat());
            return;
        }
        if (zhu < 0 || zhu > 4) {
            LogUtil.msgLog.info("xuanzhu  params error zhu" + zhu + " seat = " + player.getSeat());
            return;
        }
        if (zhuColor != -1) {
            LogUtil.msgLog.info("has already xuanzhu " + player.getSeat());
            return;
        }

        if (getTableStatus() != YybsConstants.TABLE_STATUS_XUANZHU) {
            addGameActionLog(player, "NoXuanZhuState");
            return;
        }
        zhuColor = zhu;
        ArrayList<Integer> val = new ArrayList<>();
        val.add(zhu);
        addPlayLog(addSandhPlayLog(player.getSeat(), YybsConstants.TABLE_STATUS_XUANZHU, val, false, 0, null, player.getSeat(), ""));
        ComRes.Builder builder = SendMsgUtil.buildComRes(WebSocketMsgType.REQ_XUANZHU, zhu);
        for (YybsPlayer splayer : seatMap.values()) {
            splayer.writeSocket(builder.build());
        }

        //选完主后拿底牌埋牌
        YybsPlayer banker1 = seatMap.get(banker);
        banker1.addHandPais(dipai);
        addPlayLog(addSandhPlayLog(banker, YybsConstants.TABLE_DINGZHUANG, dipai, false, 0, null, banker, ""));
        ComRes.Builder builder2 = SendMsgUtil.buildComRes(WebSocketMsgType.RES_DINGZHUANG, dipai, banker + "", zhu + "");
        for (YybsPlayer splayer : seatMap.values()) {
            splayer.writeSocket(builder2.build());
        }

        setTableStatus(YybsConstants.TABLE_STATUS_MAIPAI);

    }


    public void playChuPaiRecord(YybsPlayer player) {
        JSONArray jarr = new JSONArray();
        for (YybsPlayer splayer : seatMap.values()) {
            JSONObject json = new JSONObject();
            json.put("seat", splayer.getSeat());
            JSONArray jarr2 = new JSONArray();
            for (int i = 1; i <= turnNum; i++) {
                JSONObject json2 = new JSONObject();
                if (i > turnWinSeats.size()) {
                    continue;
                }
                Integer seat = turnWinSeats.get(i - 1);
                List<Integer> cards = splayer.getCurOutCard(i);
                json2.put("cards", cards);
                json2.put("win", splayer.getSeat() == seat ? 1 : 0);
                jarr2.add(json2);

            }
            json.put("cardArr", jarr2);
            jarr.add(json);
        }
        ComRes.Builder builder = SendMsgUtil.buildComRes(WebSocketMsgType.RES_CHUPAI_RECORD, jarr.toJSONString());
        player.writeSocket(builder.build());
    }

    /**
     * 发送已出分牌
     *
     * @param player
     */
    public void playFenPaiRecord(YybsPlayer player) {
        JSONArray jarr = new JSONArray();
        for (YybsPlayer splayer : seatMap.values()) {
            JSONObject json = new JSONObject();
            json.put("seat", splayer.getSeat());
            List<List<Integer>> outpais = splayer.getOutPais();
            List<Integer> fecardsAry = new ArrayList<>();
            for (int i = 0; i < outpais.size(); i++) {
                List<Integer> cards = outpais.get(i);
                for (int fencard : cards) {
                    int val = CardUtils.loadCardValue(fencard);
                    if (val == 5 || val == 10 || val == 13) {
                        fecardsAry.add(fencard);
                    }
                }
                json.put("cards", fecardsAry);
            }
            jarr.add(json);
        }
        ComRes.Builder builder = SendMsgUtil.buildComRes(4102, jarr.toJSONString());
        player.writeSocket(builder.build());
    }

    /**
     * 查看分牌
     *
     * @param player
     */
    public void playChaDi(YybsPlayer player) {
        if (player.getSeat() != banker) {
            return;
        }
        JSONArray jarr = new JSONArray();
        for (YybsPlayer splayer : seatMap.values()) {
            JSONObject json = new JSONObject();
            json.put("seat", splayer.getSeat());
            json.put("cardArr", dipai);
            jarr.add(json);
        }
        ComRes.Builder builder = SendMsgUtil.buildComRes(WebSocketMsgType.RES_CHUPAI_RECORD, jarr.toJSONString());
        player.writeSocket(builder.build());
    }

    public void playLiushou(YybsPlayer player, int color) {
        for (YybsPlayer splayer : seatMap.values()) {
            if (splayer.getBaofu() == 0 && splayer.getSeat() != banker) {
                return;
            }
        }
        player.setLiushou(color);

        ArrayList<Integer> val = new ArrayList<>();
        val.add(color);
        addPlayLog(addSandhPlayLog(player.getSeat(), YybsConstants.TABLE_LIUSHOU_PLAY, val, player.getBaofu() == 1 ? true : false, 0, null, getNowDisCardSeat(), ""));
        if (!player.isAutoPlay()) {
            player.setAutoPlay(false, this);
            player.setLastOperateTime(System.currentTimeMillis());
        }
        ComRes.Builder builder = SendMsgUtil.buildComRes(WebSocketMsgType.RES_Liushou, player.getSeat(), color);
        for (YybsPlayer splayer : seatMap.values()) {
            splayer.writeSocket(builder.build());
        }
    }

    /**
     * @param player 玩家
     * @param type   //投降  1：发起1倍投降    2：同意     3：拒绝  4：2倍直接投降
     * @param txtype 1=询问投降  4=直接投降 闲家各+2
     */
    public void playTouxiang(YybsPlayer player, int type, int txtype) {
        if (type < 0 || type > 4) {
            return;
        }
        if (player.getTouXiang() == 1 && txtype == 1) {
            return;
        }
        if (txtype == 1 && touxiangXW == 0) {
            touxiangXW = 1;
        }
        if (txtype == 4) {
            touxiangXW = 2;
        }
        player.setTouXiang(type);
        if (touxiangXW == 1) {
            List<Integer> touxs = new ArrayList<Integer>();
            if (type == 1) {
                setTouxiangTime(TimeUtil.currentTimeMillis());
            } else if (type == 3) {
                setTouxiangTime(0);
            }
            sendTouxiangMsg(touxs);
            if (touxs.size() == maxPlayerCount) {
                state = table_state.over;
            }

        } else {
            // 直接结算
            touxiangXW = 2;
            state = table_state.over;
        }
        if (state == table_state.over) {
            calcOver();
        }

    }

    private void sendTouxiangMsg(List<Integer> touxs) {
        JSONArray jarr = new JSONArray();
        for (YybsPlayer splayer : seatMap.values()) {
            JSONObject json = new JSONObject();
            json.put("seat", splayer.getSeat());
            json.put("state", splayer.getTouXiang());
            if (splayer.getTouXiang() == 1 || splayer.getTouXiang() == 2) {
                touxs.add(splayer.getTouXiang());
            }
            jarr.add(json);
        }

        int txTime = (int) (TimeUtil.currentTimeMillis() - getTouxiangTime());
        txTime = autoTimeOut - txTime;
        if (txTime < 0 || !autoPlay) {
            txTime = 0;
        }
        //
        ComRes.Builder builder = SendMsgUtil.buildComRes(WebSocketMsgType.RES_TOUX, txTime, jarr.toString());
        for (YybsPlayer splayer : seatMap.values()) {
            splayer.writeSocket(builder.build());
        }
    }

    /**
     * 来米喊话
     *
     * @param player
     */
    public void playLaiMi(YybsPlayer player) {

        JSONArray jarr = new JSONArray();
        for (YybsPlayer splayer : seatMap.values()) {
            JSONObject json = new JSONObject();
            json.put("seat", player.getSeat());
            jarr.add(json);
        }
        ComRes.Builder builder = SendMsgUtil.buildComRes(3116, jarr.toString());
        for (YybsPlayer splayer : seatMap.values()) {
            splayer.writeSocket(builder.build());
        }
    }

    /**
     * 有人飘分
     *
     * @param player
     * @param piaofen
     */
    public void playPiaoFen(YybsPlayer player, int piaofen) {

//		if(nowDisCardSeat!=player.getSeat()) {
//			LogUtil.msgLog.info("now actionseat is error  + nowDisCardSeat = "+nowDisCardSeat + "actionSeat = "+player.getSeat());
//			return;
//		}

        if (getTableStatus() != YybsConstants.TABLE_STATUS_PIAOFEN) {
            addGameActionLog(player, "NoPIAOFENstate");
            return;
        }
        if (piaofen < 0 || piaofen > 3) {
            LogUtil.msgLog.info("" + player.getName() + " seat=[" + player.getSeat() + "]" + " piaofen=[" + piaofen + "] is error");
            return;
        }
        //已结束飘分=1
        if (finishiPiaofen == 1) {
            return;
        }

        player.setPiaofen(piaofen);
//		JSONArray jarr = new JSONArray();
//		for (YybsPlayer splayer : seatMap.values()) {
        JSONObject json = new JSONObject();
        json.put("seat", player.getSeat());
        json.put("piaofen", player.getPiaofen());
//			jarr.add(json);
//		}

        ComRes.Builder builder = SendMsgUtil.buildComRes(3117, 1, json.toString());
        for (YybsPlayer splayer : seatMap.values()) {
            splayer.writeSocket(builder.build());
        }

        if (!player.isAutoPlay()) {
            player.setAutoPlay(false, this);
            player.setLastOperateTime(System.currentTimeMillis());
        }
        List<Integer> val = new ArrayList<>();
        val.add(piaofen);
//	    addPlayLog(addSandhPlayLog(player.getSeat(), YybsConstants.TABLE_PIAOFEN_PLAY, val,player.getBaofu()==1?true:false,0,null,getNowDisCardSeat()));
//	    addPlayLog(addSandhPlayLog(player.getSeat(), YybsConstants.TABLE_STATUS_PIAOFEN, val,player.getBaofu()==1?true:false,0,null,getNowDisCardSeat()));

        //飘完发牌
        finishiPiaofen = 1;
        for (YybsPlayer splayer : seatMap.values()) {
            if (splayer.getPiaofen() == -1) {
                finishiPiaofen = 0;
                return;
            }
        }
        if (finishiPiaofen == 1) {
            fapai();
//			noticeJiaofen();
        }
    }

    /**
     * 叫分
     *
     * @param player
     * @param Fztype 反主类型： 0 弃权   1 抢主 。 2 反主
     * @param pai    叫主牌
     */
    public void playJiaoZhu(YybsPlayer player, int Fztype, String pai) {
        if (Fztype < 0 || Fztype > 2) {
            addGameActionLog(player, "  params error Fztype " + Fztype);
            return;
        }
        ArrayList<Integer> paiarr = new ArrayList<>();
        if (Fztype != 0) {
            String[] strpaiarr = pai.split(",");
            for (String s : strpaiarr) {
                paiarr.add(Integer.valueOf(s));
            }
            if (!containCards(player.getHandPais(), paiarr)) {
                return;
            }
        }
        synchronized (this) {
            if (Fztype == 1 && finishQz == 1) {
                player.writeErrMsg("已有玩家抢庄！");
                return;
            } else {
                finishQz = 1;//已经抢完 等待反主
            }
            if (Fztype == 1) {
                baseScore = 1;
            }

            if (Fztype == 2) {
                if (nowDisCardSeat != player.getSeat()) {
                    LogUtil.msgLog.info("now actionseat is error  + nowDisCardSeat = " + nowDisCardSeat + "actionSeat = " + player.getSeat());
                    return;
                }
                //反主大小判断。 只能用最小得反
                if (!CardUtils.compareFanzhuPai(jiaozhuCardStr, pai, player)) {
                    return;
                }
                fanzhuNum++;
                if (fanzhuNum > option_fanzhunum) {
                    LogUtil.msgLog.info(player.getName() + " seat=[" + player.getSeat() + "] fztype=" + Fztype + " 超过反主次数上限");
                    return;
                }
                baseScore++;
            }

            if (getTableStatus() != YybsConstants.TABLE_STATUS_JIAOZHU) {
                addGameActionLog(player, "Nojiaozhustate");
                return;
            }

            int bankerSeat = 0;
            if (Fztype > 0) {
                setJiaozhuCardStr(pai);
                banker = player.getSeat();
            }


            setDisCardSeat(player.getSeat());
            player.setJiaoZhu(Fztype);
            int nextActionSeat = 0;
            int nextS = player.getSeat();
            for (int i = 0; i < maxPlayerCount - 1; i++) {
                nextS += 1;
                if (nextS > maxPlayerCount) {
                    nextS = 1;
                }
                YybsPlayer nextPlayer = seatMap.get(nextS);
                if (nextPlayer.getJiaoZhu() == 0) {
                    //
                    continue;
                }
                nextActionSeat = nextPlayer.getSeat();
                break;
            }

            boolean isjiaozhuOver = false;
            int count0 = 0;
            for (YybsPlayer p : playerMap.values()) {
                if (p.getJiaoZhu() == 0) {
                    count0++;
                }
            }

            //无人叫主 重新发牌
            if ((count0 == 4 && "".equals(jiaozhuCardStr)) && maxPlayerCount == 4) {
                fapai();
                for (YybsPlayer er : seatMap.values()) {
                    addPlayLog(StringUtil.implode(er.getHandPais(), ","));
                }
				JSONObject json = new JSONObject();
				for (YybsPlayer p : playerMap.values()) {
					json.put(p.getUserId()+"", p.getSeat());
				}
				addPlayLog(addSandhPlayLogNewSeat(0, YybsConstants.TABLE_STATUS_INITSEAT, null, false, 0, null, 0, json));

				return;
            }

//		if((count0==3 &&"".equals(jiaozhuCardStr))  && maxPlayerCount == 3 ){
//			//三个人弃权。 重新发牌
//			fapai();
//			for (YybsPlayer er : seatMap.values()) {
//				addPlayLog(StringUtil.implode(er.getHandPais(), ","));
//			}
//			return;
//		}
            if (fanzhuNum >= option_fanzhunum) {
                //到达反主上限 结束。
                isjiaozhuOver = true;
            }

            if (count0 == maxPlayerCount && !"".equals(jiaozhuCardStr)) {
                isjiaozhuOver = true;
            }
            //仅剩最后 自己反自己刷分  下家位置处理
            if (count0 == (maxPlayerCount - 1) && !"".equals(jiaozhuCardStr) && Fztype == 2 && banker == player.getSeat()) {
                nextActionSeat = player.getSeat();
            }
//		if(!"".equals(jiaozhuCardStr) && player.getSeat()==banker && Fztype==0){
//			isjiaozhuOver = true;
//		}
            if (isjiaozhuOver) {
                bankerSeat = banker;
            }

            setNowDisCardSeat(nextActionSeat);
            List<Integer> baseScoreList = new ArrayList<>();
            baseScoreList.add(baseScore);

            addPlayLog(addSandhPlayLog(player.getSeat(), YybsConstants.TABLE_STATUS_JIAOZHU, paiarr, false, baseScore, null, nextActionSeat, ""));
            LogUtil.msgLog.info(player.getName() + " select jiaozhu param:Fztype=" + Fztype + " [0弃.1抢.2反]; param:pai=" + paiarr.toString());

            ComRes.Builder jbuilder = SendMsgUtil.buildComRes(WebSocketMsgType.REQ_YYBS_JIAOZHU, Fztype, pai, player.getSeat(),
                    bankerSeat == 0 ? nextActionSeat : 0, baseScore);

            for (YybsPlayer splayer : seatMap.values()) {
                splayer.writeSocket(jbuilder.build());
            }
            //录像。叫主反主流程
//		  addPlayLog(addSandhPlayLog(player.getSeat(), YybsConstants.TABLE_STATUS_JIAOZHU, val,false,0,null,player.getSeat()));

            // 定庄了 进入选主阶段
            if (bankerSeat != 0) {
                ArrayList<Integer> dipailist = new ArrayList<>();
                //录像 定庄
                addPlayLog(addSandhPlayLog(bankerSeat, YybsConstants.TABLE_DINGZHUANG, null, false, 0, null, bankerSeat, ""));
                banker = bankerSeat;
                setNowDisCardSeat(bankerSeat);
                if (CardUtils.toIntResult(jiaozhuCardStr) <= 5) {
                    //自动选主
                    int c = CardUtils.selColor(jiaozhuCardStr);
                    ComRes.Builder builder = SendMsgUtil.buildComRes(WebSocketMsgType.RES_DINGZHUANG, dipailist, bankerSeat + "", c + "");
                    for (YybsPlayer splayer : seatMap.values()) {
                        splayer.writeSocket(builder.build());
                    }
                    setTableStatus(YybsConstants.TABLE_STATUS_XUANZHU);
                    playXuanzhu(seatMap.get(bankerSeat), c);
                    return;
                } else {
                    ComRes.Builder builder = SendMsgUtil.buildComRes(WebSocketMsgType.RES_DINGZHUANG, dipailist, bankerSeat + "");
                    for (YybsPlayer splayer : seatMap.values()) {
                        splayer.writeSocket(builder.build());
                    }
                    setTableStatus(YybsConstants.TABLE_STATUS_XUANZHU);
                }
            }
        }
    }


    /**
     * @param player 玩家
     * @param dui    1=1v3模式  ;2 =队友模式
     */
    public void playerXuanDui(YybsPlayer player, int dui) {
        if (player.getSeat() != banker) {
            LogUtil.msgLog.info("now actionseat is error  + banker = " + banker + " actionSeat = " + player.getSeat());
            return;
        }

        if (getTableStatus() != YybsConstants.TABLE_STATUS_XUANDUI) {
            addGameActionLog(player, "No TABLE_STATUS_XUANDUI State");
            return;
        }
        ComRes.Builder builder = SendMsgUtil.buildComRes(WebSocketMsgType.REQ_YYBS_XUANDUI, dui, player.getSeat());
        for (YybsPlayer splayer : seatMap.values()) {
            splayer.writeSocket(builder.build());
        }
        LogUtil.msgLog.info(player.getName() + " select teamtype[1=1v3,2=team] param:1v3=" + dui);
        setTableStatus(YybsConstants.TABLE_STATUS_PLAY);
        if (1 == dui) {
            is_1v3 = 1;
            //bank
            getTeamSeat().add(banker);
        } else if (2 == dui) {
            is_1v3 = 2;
            getTeamSeat().add(1);
            getTeamSeat().add(3);
            //4 3 2 1  黑红梅方
            int teampai = zhuColor * 100 + 6;
            if (isChouLiu()) {//抽6 主7结对
                teampai = zhuColor * 100 + 7;
            }

            //按主牌的6选队友。
            boolean is2 = false;
            Map<Integer, Integer> randomCardNumMap = new HashMap<>();
            for (YybsPlayer p : playerMap.values()) {
                int num = 0;
                if (p.getSeat() == banker) {
                    List<Integer> list = new ArrayList<>();
                    list.addAll(p.getHandPais());
                    list.addAll(dipai);
                    num = CardUtils.getSelTeamCardNum(list, teampai);
                } else {
                    num = CardUtils.getSelTeamCardNum(p.getHandPais(), teampai);
                }

                if (num == 2) {
                    is2 = true;
                    break;
                }
                randomCardNumMap.put(p.getSeat(), num);
            }
            long banker_userid = seatMap.get(banker).getUserId();
            //根据发牌情况中含有同一个randomcard 的数量进行位置处理
            if (!is2) {
                //2在 randomCardNumMap 中的value 直接是对家 不用改
                int seat1 = 0;
                int seat2 = 0;
                List<Integer> poslistA = new ArrayList<>();
                List<Integer> poslistB = new ArrayList<>();
                Iterator<Map.Entry<Integer, Integer>> it = randomCardNumMap.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<Integer, Integer> entry = it.next();
                    if (entry.getValue() == 1) {
                        poslistA.add(entry.getKey());
                    } else if (entry.getValue() == 0) {
                        poslistB.add(entry.getKey());
                    }
                }
                //A B  重新调整位置  13 给A  24 给B
                Map<Integer, YybsPlayer> seatMapnew = new ConcurrentHashMap<Integer, YybsPlayer>();
                for (YybsPlayer p : seatMap.values()) {
                    p.setSeat(0);
                }
                YybsPlayer pa1 = seatMap.get(poslistA.get(0));
                seatMapnew.put(1, pa1);
                pa1.setSeat(1);

                if (pa1.getUserId() == banker_userid) {
                    banker = 1;
                    turnFirstSeat = 1;
                }


                YybsPlayer pa3 = seatMap.get(poslistA.get(1));
                seatMapnew.put(3, pa3);
                pa3.setSeat(3);

                if (pa3.getUserId() == banker_userid) {
                    banker = 3;
                    turnFirstSeat = 3;
                }
                YybsPlayer pa2 = seatMap.get(poslistB.get(0));
                seatMapnew.put(2, pa2);
                pa2.setSeat(2);
                if (pa2.getUserId() == banker_userid) {
                    banker = 2;
                    turnFirstSeat = 2;
                }

                YybsPlayer pa4 = seatMap.get(poslistB.get(1));
                seatMapnew.put(4, pa4);
                pa4.setSeat(4);
                if (pa4.getUserId() == banker_userid) {
                    banker = 4;
                    turnFirstSeat = 4;
                }
//                seatMap.clear();
//                seatMap = seatMapnew;
                seatMap.put(1,pa1);
                seatMap.put(2,pa2);
                seatMap.put(3,pa3);
                seatMap.put(4,pa4);
                changeCards(1);
                changeCards(2);
                changeCards(3);
                changeCards(4);
                changePlayers();
                //调整后
//                System.out.println("----------------");
//                for (YybsPlayer p : seatMap.values()) {
//                    System.out.println(p.getSeat() + " " + p.getName() + " " + p.getHandPais());
//                }

                JSONObject seat_json = new JSONObject();
                for (YybsPlayer p : seatMap.values()) {
                    seat_json.put(p.getUserId()+"",p.getSeat());
                }
                seatJsonStr = seat_json.toJSONString();
            }
            //发送 //TODO 重连
            setNowDisCardSeat(banker);
            turnFirstSeat =banker;
            for (YybsPlayer splayer : seatMap.values()) {
                CreateTableRes b = buildCreateTableRes(splayer.getUserId(), true, false);
                splayer.writeSocket(b);
            }
        }
//		playLog="";
//		for (YybsPlayer p: seatMap.values()  ) {
//			StringBuilder sb = new StringBuilder("YiYangBaShi");
//			sb.append("|").append(getId());
//			sb.append("|").append(getPlayBureau());
//			sb.append("|").append(p.getUserId());
//			sb.append("|").append(p.getSeat());
//			sb.append("|").append(p.getName());
//			sb.append("|").append(p.isAutoPlay() ? 1 : 0);
//			sb.append("|").append("fapai");
//			sb.append("|").append(p.getHandPais());
//			LogUtil.msgLog.info(sb.toString());
//		}
        //录像。
//		for (int i = 1; i <= getMaxPlayerCount(); i++) {
//			Player p = getSeatMap().get(i);
//			addPlayLog(StringUtil.implode(p.getHandPais(), ","));
//		}
        List<Integer> duiv = new ArrayList<>();
        duiv.add(dui);
        addPlayLog(addSandhPlayLog(player.getSeat(), YybsConstants.TABLE_STATUS_DINGDUI, duiv, false, 0, null, player.getSeat(), ""));

        //
        JSONObject newseat = new JSONObject();
        for (YybsPlayer p : playerMap.values()) {
            newseat.put(p.getUserId() + "", p.getSeat());
        }

        addPlayLog(addSandhPlayLogNewSeat(player.getSeat(), YybsConstants.TABLE_STATUS_CHANGESEAT, null, false, 0, null, player.getSeat(), newseat));
    }


    public int getAutoTimeOut() {
        return autoTimeOut;
    }

    /**
     * 人数未满或者人员离线
     *
     * @return 0 可以打牌 1人数未满 2人员离线
     */
    public int isCanPlay() {
        if (seatMap.size() < getMaxPlayerCount()) {
            return 1;
        }
        for (YybsPlayer player : seatMap.values()) {
            if (player.getIsEntryTable() != SharedConstants.table_online) {
                // 通知其他人离线
                broadIsOnlineMsg(player, player.getIsEntryTable());
                return 2;
            }
        }
        return 0;
    }

    @Override
    public <T> T getPlayer(long id, Class<T> cl) {
        return (T) playerMap.get(id);
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
    protected void initNext1() {
        setNowDisCardIds(null);
        replayDisCard = "";
        timeNum = 0;
        newRound = true;
        finishFapai = 0;
        zhuoFen.clear();
        dipai.clear();
        turnNum = 0;
        turnFirstSeat = 0;
        zhuColor = -1;
        banker = 0;
        jiaoFen = 0;
        isPai = false;
        turnWinSeats.clear();
        setTableStatus(0);
        setTouxiangTime(0);
        touxiangXW = 0;
        jiaozhuCardStr = "";
        baseScore = 0;
        fanzhuNum = 0;
        finishQz = 0;
        is_1v3 = 0;
        restartFapai=0;
        teamSeat.clear();
        seatJsonStr="";
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
    protected void sendDealMsg(long userId) {

		JSONObject json = new JSONObject();
		for (YybsPlayer player : playerMap.values()) {
			json.put(player.getUserId()+"", player.getSeat());
		}
		addPlayLog(addSandhPlayLogNewSeat(0, YybsConstants.TABLE_STATUS_INITSEAT, null, false, 0, null, 0, json));



	}

    private void noticeJiaofen() {
        //第一句随机。第二句上把坐庄
        nowDisCardSeat = RandomUtils.nextInt(maxPlayerCount) + 1;
        setDisCardSeat(nowDisCardSeat);
        //进入叫分环节
        setTableStatus(YybsConstants.TABLE_STATUS_JIAOZHU);
        for (YybsPlayer tablePlayer : seatMap.values()) {
            DealInfoRes.Builder res = DealInfoRes.newBuilder();
            res.addAllHandCardIds(tablePlayer.getHandPais());
            //叫分的人座位
            res.setNextSeat(nowDisCardSeat);
            res.setGameType(getWanFa());//
            res.setRemain(getTableStatus());
            // res.setBanker(lastWinSeat);
            tablePlayer.writeSocket(res.build());
            if (tablePlayer.isAutoPlay()) {
                ArrayList<Integer> val = new ArrayList<>();
                val.add(1);
                addPlayLog(addSandhPlayLog(tablePlayer.getSeat(), YybsConstants.action_tuoguan, val, false, 0, null, 0, ""));
            } else {
                tablePlayer.setPiaofenCheckTime(System.currentTimeMillis() + 20 * 1000);//自动叫主拖管时间
            }
        }
    }

    @Override
    protected void robotDealAction() {
    }

    @Override
    protected void deal() {

    }

    @Override
    public Map<Long, Player> getPlayerMap() {
        Object o = playerMap;
        return (Map<Long, Player>) o;
    }

    @Override
    public int getMaxPlayerCount() {
        return maxPlayerCount;
    }


    public void setMaxPlayerCount(int maxPlayerCount) {
        this.maxPlayerCount = maxPlayerCount;
        changeExtend();
    }

    public List<Integer> getNowDisCardIds() {
        return nowDisCardIds;
    }

    public void setNowDisCardIds(List<Integer> nowDisCardIds) {
        if (nowDisCardIds == null) {
            this.nowDisCardIds.clear();

        } else {
            this.nowDisCardIds = nowDisCardIds;

        }
        dbParamMap.put("nowDisCardIds", JSON_TAG);
    }

    public boolean saveSimpleTable() throws Exception {
        TableInf info = new TableInf();
        info.setMasterId(masterId);
        info.setRoomId(0);
        info.setPlayType(playType);
        info.setTableId(id);
        info.setTotalBureau(totalBureau);
        info.setPlayBureau(1);
        info.setServerId(GameServerConfig.SERVER_ID);
        info.setCreateTime(new Date());
        info.setDaikaiTableId(daikaiTableId);

        changeExtend();

        info.setExtend(buildExtend());
        TableDao.getInstance().save(info);
        loadFromDB(info);
        return true;
    }

    public boolean createSimpleTable(Player player, int play, int bureauCount, List<Integer> params, List<String> strParams, boolean saveDb) throws Exception {
        return createTable(player, play, bureauCount, params, saveDb);
    }

    public void createTable(Player player, int play, int bureauCount, List<Integer> params) throws Exception {
        createTable(player, play, bureauCount, params, true);
    }

    public boolean createTable(Player player, int play, int bureauCount, List<Integer> params, boolean saveDb) throws Exception {
        // objects对象的值列表
        long id = getCreateTableId(player.getUserId(), play);
        if (id <= 0) {
            return false;
        }
        if (saveDb) {
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
        } else {
            setPlayType(play);
            setDaikaiTableId(daikaiTableId);
            this.id = id;
            this.totalBureau = bureauCount;
            this.playBureau = 1;
        }

        payType = StringUtil.getIntValue(params, 2, 1);// 1AA,2房主
        shuangjinDC = StringUtil.getIntValue(params, 3, 1);// 双进单出
        baofuLS = StringUtil.getIntValue(params, 4, 2);// 报副留守
        checkPai = StringUtil.getIntValue(params, 5, 0);// 允许查牌1
        chouLiu = StringUtil.getIntValue(params, 6, 1);// 抽6
        maxPlayerCount = StringUtil.getIntValue(params, 7, 4);// 人数
        touxiangXW = 0;//= StringUtil.getIntValue(params, 8, 0);// 投降需询问
//		sixtyQJ = StringUtil.getIntValue(params, 9, 0);// 60分起叫
//		xiaoguangF = StringUtil.getIntValue(params, 10, 0);// 小光起分 0:30分
//		daDaoTQ = StringUtil.getIntValue(params, 11, 0);// 大倒提前结束
//		jiaofenJP = StringUtil.getIntValue(params, 12, 0);// 叫分加拍
//		jiaofenJD = StringUtil.getIntValue(params, 13, 0);// 叫分进档
        if (maxPlayerCount == 0) {
            maxPlayerCount = 4;
        }
        int time = StringUtil.getIntValue(params, 14, 0);
        this.autoPlay = time > 1;
        autoPlayGlob = StringUtil.getIntValue(params, 15, 0); //1单局  2整局  3三局
        if (time > 0) {
            autoTimeOut2 = autoTimeOut = (time * 1000);
        }
        option_red2 = StringUtil.getIntValue(params, 16, 0);// 1不带  2带
        option_fanzhunum = StringUtil.getIntValue(params, 17, 0);//  3 5 99
        option_fanzhuFen = StringUtil.getIntValue(params, 18, 0);//0=小反125大反155  1=小反130大反160
        setLastActionTime(TimeUtil.currentTimeMillis());
        return true;
    }

    @Override
    protected void initNowAction(String nowAction) {

    }


    @Override
    protected String buildNowAction() {
        return null;
    }

    @Override
    public void setConfig(int index, int val) {

    }

    public ClosingInfoRes.Builder sendAccountsMsg(boolean over, Player winPlayer, boolean isBreak) {
        return sendAccountsMsg(over, isBreak, 0, false, 0, false);
    }

    /**
     * 发送结算msg
     *
     * @param over 是否已经结束
     * @return
     */
    public ClosingInfoRes.Builder sendAccountsMsg(boolean over, boolean isBreak, int score, boolean koudi, int difen, boolean touxiang) {
        List<ClosingPlayerInfoRes> list = new ArrayList<>();
        List<ClosingPlayerInfoRes.Builder> builderList = new ArrayList<>();

        int minPointSeat = 0;
        int minPoint = 0;
//        if (winPlayer != null) {
//            for (YybsPlayer player : seatMap.values()) {
//                if (player.getUserId() == winPlayer.getUserId()) {
//                    continue;
//                }
//                if (minPoint == 0 || player.getPoint() < minPoint) {
//                    minPoint = player.getPlayPoint();
//                    minPointSeat = player.getSeat();
//                }
//            }
//        }


        // 大结算低于below分+belowAdd分
        if (over && belowAdd > 0 && playerMap.size() == 2) {
            for (YybsPlayer player : seatMap.values()) {
                int totalPoint = player.getTotalPoint();
                if (totalPoint > -below && totalPoint < 0) {
                    player.setTotalPoint(player.getTotalPoint() - belowAdd);
                } else if (totalPoint < below && totalPoint > 0) {
                    player.setTotalPoint(player.getTotalPoint() + belowAdd);
                }
            }
        }


        for (YybsPlayer player : seatMap.values()) {
            ClosingPlayerInfoRes.Builder build = null;
            if (over) {
                build = player.bulidTotalClosingPlayerInfoRes();
            } else {
                build = player.bulidOneClosingPlayerInfoRes();

            }
            // 所有牌
            // 添加手牌
//            List<Integer> allCard = new ArrayList<Integer>();
//            for (Integer v : player.getHandPais()) {
//                if (!allCard.contains(v)) {
//                    allCard.add(v);
//                }
//            }
//			// 添加已出的牌
//            for (List<Integer> c : player.getOutPais()) {
//                for (Integer v : c) {
//                    if (!allCard.contains(v)) {
//                        allCard.add(v);
//                    }
//                }
//            }
//
//            JSONArray jsonArray = new JSONArray();
//            for (int card : allCard) {
//                if (card != 0) {
//                    jsonArray.add(card);
//                }
//            }
            //build.addExt(jsonArray.toString()); // 0

            build.addExt("0");// 3
            build.addExt("0");// 4
            build.addExt("0");// 5

            build.addExt(String.valueOf(player.getCurrentLs()));// 6
            build.addExt(String.valueOf(player.getMaxLs()));// 7
            build.addExt(String.valueOf(matchId));// 8


//            if (winPlayer != null && player.getUserId() == winPlayer.getUserId()) {
//				// 手上没有剩余的牌放第一位为赢家
//                builderList.add(0, build);
//            } else {
            builderList.add(build);
//            }

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
            for (YybsPlayer player : seatMap.values()) {
                if (player.getWinLoseCredit() > dyjCredit) {
                    dyjCredit = player.getWinLoseCredit();
                }
            }
            for (ClosingPlayerInfoRes.Builder builder : builderList) {
                YybsPlayer player = seatMap.get(builder.getSeat());
                calcCommissionCredit(player, dyjCredit);

                builder.addExt(player.getWinLoseCredit() + "");      //10
                builder.addExt(player.getCommissionCredit() + "");   //11

                // 2019-02-26更新
                builder.setWinLoseCredit(player.getWinLoseCredit());
                builder.setCommissionCredit(player.getCommissionCredit());
            }
        } else if (isGroupTableGoldRoom()) {
            // -----------亲友圈金币场---------------------------------
            for (YybsPlayer player : seatMap.values()) {
                player.setWinGold(player.getTotalPoint() * gtgDifen);
            }
            calcGroupTableGoldRoomWinLimit();
            for (ClosingPlayerInfoRes.Builder builder : builderList) {
                YybsPlayer player = seatMap.get(builder.getSeat());
                builder.addExt(player.getWinLoseCredit() + ""); // 10
                builder.addExt(player.getCommissionCredit() + ""); // 11
                builder.setWinLoseCredit(player.getWinGold());
            }
        } else {
            for (ClosingPlayerInfoRes.Builder builder : builderList) {
                YybsPlayer player = seatMap.get(builder.getSeat());
                builder.addExt(0 + ""); //10
                builder.addExt(0 + ""); //11
            }
        }
        for (ClosingPlayerInfoRes.Builder builder : builderList) {
            YybsPlayer player = seatMap.get(builder.getSeat());
            builder.addExt(player.getPiaofen() + ""); //13
            list.add(builder.build());
        }

        ClosingInfoRes.Builder res = ClosingInfoRes.newBuilder();
        res.setIsBreak(isBreak ? 1 : 0);
        res.setWanfa(getWanFa());
        res.addAllClosingPlayers(list);
        res.addAllExt(buildAccountsExt(over ? 1 : 0, score, koudi, touxiang));
//        if(koudi){
//        	res.addAllCutCard(dipai);
//        }
        if (over && isGroupRoom() && !isCreditTable()) {
            res.setGroupLogId((int) saveUserGroupPlaylog());
        }
        for (YybsPlayer player : seatMap.values()) {
            player.writeSocket(res.build());
        }
        return res;
    }

    public List<String> buildAccountsExt(int over, int score, boolean koudi, boolean touxiang) {
        List<String> ext = new ArrayList<>();
        ext.add(id + "");//0
        ext.add(masterId + "");//1
        ext.add(TimeUtil.formatTime(TimeUtil.now()));//2
        ext.add(playType + "");//3
        // 设置当前第几局
        ext.add(playBureau + "");//4
        ext.add(isGroupRoom() ? "1" : "0");//5
        // 金币场大于0
        ext.add(CommonUtil.isPureNumber(modeId) ? modeId : "0");//6
        int ratio;
        int pay;
        ratio = 1;
        pay = loadPayConfig(payType);
        ext.add(String.valueOf(ratio));//7
        ext.add(String.valueOf(pay >= 0 ? pay : 0));//8
        ext.add(String.valueOf(payType));//9
        ext.add(String.valueOf(playedBureau));//10

        ext.add(String.valueOf(matchId));//11
        ext.add(isGroupRoom() ? loadGroupId() : "");//12

        ext.add(creditMode + ""); //13
        ext.add(creditJoinLimit + "");//14
        ext.add(creditDissLimit + "");//15
        ext.add(creditDifen + "");//16
        ext.add(creditCommission + "");//17
        ext.add(creditCommissionMode1 + "");//18
        ext.add(creditCommissionMode2 + "");//19
        ext.add((autoPlay ? 1 : 0) + "");//20
        ext.add(over + ""); // 21
        ext.add(baseScore + ""); //  22
        ext.add(score + ""); // 23
        ext.add(koudi ? "1" : "0");
        ext.add(isPai ? "1" : "0");
        ext.add(touxiangXW + ""); //26  1询问 2=2倍投降
        return ext;
    }


    @Override
    public String loadGameCode() {
        return GAME_CODE;
    }

    @Override
    public void sendAccountsMsg() {
        ClosingInfoRes.Builder builder = sendAccountsMsg(true, null, true);
        saveLog(true, 0l, builder.build());
    }

    @Override
    public Class<? extends Player> getPlayerClass() {
        return YybsPlayer.class;
    }

    @Override
    public int getWanFa() {
        return GameUtil.play_type_pk_yybs;
    }

    @Override
    public void checkReconnect(Player player) {
        YybsTable table = player.getPlayingTable(YybsTable.class);
        // player.writeSocket(SendMsgUtil.buildComRes(WebSocketMsgType.req_code_pdk_playBack, table.getReplayDisCard()).build());

        checkTouxiang(player);
        //
    }

    private void checkTouxiang(Player player) {
        if (touxiangXW != 1) {
            return;
        }
        JSONArray jarr = new JSONArray();
        List<Integer> touxs = new ArrayList<Integer>();
        for (YybsPlayer splayer : seatMap.values()) {
            JSONObject json = new JSONObject();
            json.put("seat", splayer.getSeat());
            json.put("state", splayer.getTouXiang());
            touxs.add(splayer.getTouXiang());
            jarr.add(json);
        }
        if (touxs.contains(1) && !touxs.contains(3)) {
            int txTime = (int) (TimeUtil.currentTimeMillis() - getTouxiangTime());
            txTime = autoTimeOut - txTime;
            if (txTime < 0 || !autoPlay) {
                txTime = 0;
            }
            ComRes.Builder builder = SendMsgUtil.buildComRes(WebSocketMsgType.RES_TOUX, txTime, jarr.toString());
            player.writeSocket(builder.build());
        }
    }


    // 是否显示剩余牌的数量
    public boolean isShowCardNumber() {
        return 1 == getShowCardNumber();
    }

    @Override
    public void checkAutoPlay() {
        synchronized (this) {

            if (checkLastTurn()) {
                return;
            }
            if (!autoPlay) {
                return;
            }
            // 发起解散，停止倒计时
            if (getSendDissTime() > 0) {
                for (YybsPlayer player : seatMap.values()) {
                    if (player.getLastCheckTime() > 0) {
                        player.setLastCheckTime(player.getLastCheckTime() + 1 * 1000);
                    }
                }
                return;
            }
            if (isAutoPlayOff()) {
                // 托管关闭
                for (int seat : seatMap.keySet()) {
                    YybsPlayer player = seatMap.get(seat);
                    player.setAutoPlay(false, this);
                }
                return;
            }
            // 准备托管
            if (state == table_state.ready && playedBureau > 0) {
                ++timeNum;
                for (YybsPlayer player : seatMap.values()) {
                    // 玩家进入托管后，5秒自动准备
                    if (timeNum >= 5 && player.isAutoPlay()) {
                        autoReady(player);
                    } else if (timeNum >= 30) {
                        autoReady(player);
                    }
                }
                return;
            }

	// 自动。
//            if (state == table_state.play && getTableStatus() == YybsConstants.TABLE_STATUS_JIAOZHU && banker == 0) {
////			 	随即玩家抢主
//                //检查红2+10  20S
//                long time2 = System.currentTimeMillis();
//                long time3 = 0l;
//                long countbq = 0;
//                for (YybsPlayer player : seatMap.values()) {
//                    time3 = player.getPiaofenCheckTime();
//                    if (player.isAutoPlay() || time2 > player.getPiaofenCheckTime()) {
//                        int isred2 = 0;
//                        int is10 = 0;
//                        int pai1 = 0;
//                        int paiwang = 0;
//                        for (int pai : player.getHandPais()) {
//                            // 	带2qiang
//                            if (CardUtils.loadCardValue(pai) == 15 && (CardUtils.loadCardColor(pai) == 3 || CardUtils.loadCardColor(pai) == 1)) {
//                                isred2++;
//                            }
//                            if (CardUtils.loadCardValue(pai) == 10) {
//                                is10++;
//                                pai1 = pai;
//                            }
//                            if (CardUtils.loadCardValue(pai) == 1 || CardUtils.loadCardValue(pai) == 2) {
//                                paiwang++;
//                            }
//                        }
//                        if (isDaiEr() && isred2 >= 1 && is10 >= 1) {
//                            playJiaoZhu(player, 1, pai1 + "");
//                            break;
//                        } else if (!isDaiEr() && is10 >= 1) {
//                            playJiaoZhu(player, 1, pai1 + "");
//                            break;
//                        } else if (paiwang >= 3) {
//                            playJiaoZhu(player, 1, 410 + "");
//                            break;
//                        } else {
//                            countbq++;
//                        }
//                        if (banker != 0) {
//                            break;
//                        }
//                    }
//                }
//                if (countbq >= maxPlayerCount && time2 > time3) {
//                    //所有玩家无牌抢庄
//                    fapai();
//					JSONObject json = new JSONObject();
//					for (YybsPlayer p : playerMap.values()) {
//						json.put(p.getUserId()+"", p.getSeat());
//					}
//					addPlayLog(addSandhPlayLogNewSeat(0, YybsConstants.TABLE_STATUS_INITSEAT, null, false, 0, null, 0, json));
//
//				}
//                return;
//            }
            YybsPlayer player = seatMap.get(nowDisCardSeat);
            if (player == null) {
                return;
            }

            if (getTableStatus() == 0 || state != table_state.play) {
                return;
            }

            // 托管投降检查
            checkTouxiangTimeOut();

            int timeout;

            if (autoPlay) {
                timeout = autoTimeOut;
                if (disCardRound == 0) {
                    timeout = autoTimeOut;
                }
            } else if (player.isRobot()) {
                timeout = 3 * SharedConstants.SENCOND_IN_MINILLS;
            } else {
                return;
            }

            long autoPlayTime = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "autoPlayTimePdk", 2 * 1000);
            long now = TimeUtil.currentTimeMillis();
            boolean auto = player.isAutoPlay();
            if (!auto) {
//                if (GameServerConfig.isAbroad()) {
//                    if (!player.isRobot() && now >= player.getNextAutoDisCardTime()) {
//                        auto = true;
//                        player.setAutoPlay(true, this);
//                    }
//                } else {
                auto = checkPlayerAuto(player, timeout);
//                }
            }

            if (auto || player.isRobot()) {
                boolean autoPlay = false;
//                if (GameServerConfig.isAbroad()) {
//                    if (player.isRobot()) {
//                        autoPlayTime = MathUtil.mt_rand(2, 6) * 1000;
//                    } else {
//                        autoPlay = true;
//                    }
//                }
//                if (player.getAutoPlayTime() == 0L && !autoPlay) {
//                    player.setAutoPlayTime(now);
//                } else if (autoPlay || (player.getAutoPlayTime() > 0L && now - player.getAutoPlayTime() >= autoPlayTime)) {
                player.setAutoPlayTime(0L);
                if (state == table_state.play) {
                    if (getTableStatus() == YybsConstants.TABLE_STATUS_PLAY) {
                        //托管出牌
                        autoChuPai(player);
                    } else if (getTableStatus() == YybsConstants.TABLE_STATUS_JIAOZHU) {
                        playJiaoZhu(player, 0, "");
                    } else if (getTableStatus() == YybsConstants.TABLE_STATUS_XUANDUI) {
                        playerXuanDui(player, 1);
                    } else if (getTableStatus() == YybsConstants.TABLE_STATUS_MAIPAI) {
                        List<Integer> disList = new ArrayList<Integer>();
                        int size = 8;
//                    		if(!isChouLiu()){
//                    			size= 8;
//                    		}else if(getMaxPlayerCount() == 3 && isChouLiu() ) {
//                    			size=9;
//                    		}
                        List<Integer> zCards = CardUtils.getZhu(player.getHandPais(), zhuColor);
                        List<Integer> curList = new ArrayList<>(player.getHandPais());
                        curList.removeAll(zCards);
                        if (curList.size() < size) {
                            disList.addAll(curList);
                            disList.addAll(zCards.subList(0, size - curList.size()));
                        } else {
                            disList.addAll(curList.subList(0, size));
                        }
                        playMaipai(player, disList);

                    }

                }
            }
        }
//        }
    }

    private void checkTouxiangTimeOut() {
        if (tableStatus == YybsConstants.TABLE_STATUS_MAIPAI && touxiangXW == 1) {
            if (getTouxiangTime() > 0) {
                int txTime = (int) (TimeUtil.currentTimeMillis() - getTouxiangTime());
                List<Integer> agreeTX = new ArrayList<Integer>();
                for (YybsPlayer player : seatMap.values()) {
                    if (player.getLastCheckTime() > 0) {
                        player.setLastCheckTime(player.getLastCheckTime() + 1 * 1000);
                    }
                    if (player.getTouXiang() == 0 && txTime >= autoTimeOut) {
                        player.setTouXiang(2);
                    }

                    if (player.getTouXiang() == 2 || player.getTouXiang() == 1) {
                        agreeTX.add(player.getTouXiang());
                    }
                }

                if (agreeTX.size() == maxPlayerCount) {
                    sendTouxiangMsg(agreeTX);
                    state = table_state.over;
                    calcOver();
                }

            }
        }
    }

    private boolean checkLastTurn() {
        if (getTableStatus() == YybsConstants.TABLE_STATUS_PLAY) {
            YybsPlayer player = seatMap.get(nowDisCardSeat);
            if (player == null) {
                return false;
            }
            int firstSeat = getTurnFirstSeat();

            if (firstSeat != player.getSeat()) {
                YybsPlayer fiser = seatMap.get(firstSeat);
                if (fiser != null && fiser.getHandPais().isEmpty()) {
                    playCommand(player, 0, new ArrayList<>(player.getHandPais()));
                    return true;
                }
            } else {
                //除了这个人其他都报副了
                boolean allBaofu = true;
                for (Map.Entry<Integer, YybsPlayer> entry : seatMap.entrySet()) {
                    if (entry.getValue().getSeat() == firstSeat) {
                        continue;
                    }
                    if (entry.getValue().getBaofu() != 1) {
                        allBaofu = false;
                        break;
                    }
                }

                //都报副了就全部甩出去if(!CardTool.allZhu(cards, zhuColor)){
                boolean isAuto = false;
                CardType ct = CardTool.getCardType(player.getHandPais(), zhuColor, isChouLiu());
                if (ct.getType() == CardType.DAN || ct.getType() == CardType.DUI || ct.getType() == CardType.TUOLAJI) {
                    isAuto = true;
                } else if (ct.getType() == CardType.SHUAI_LIAN_DUI || ct.getType() == CardType.SHUAIPAI || ct.getType() == CardType.SHUAIPAI2) {
                    if (allBaofu && CardTool.allZhu(player.getHandPais(), zhuColor)) {
                        isAuto = true;
                    }
                }
                if (isAuto) {
                    playCommand(player, 0, new ArrayList<>(player.getHandPais()));
                    return true;
                }


            }
        }
        return false;
    }

    private void autoChuPai(YybsPlayer player) {
        List<Integer> curList = new ArrayList<>(player.getHandPais());
        if (curList.isEmpty()) {
            return;
        }
        int firstSeat = getTurnFirstSeat();
        List<Integer> disList = new ArrayList<Integer>();
        // 轮首次出牌
        if (firstSeat == player.getSeat()) {
            // 随便出个单牌
            int rand = RandomUtils.nextInt(curList.size());
            disList.add(curList.get(rand));

        } else {

            int color = disColor / 10;
            int type = disColor % 10;
            YybsPlayer fiser = seatMap.get(turnFirstSeat);
            List<Integer> list = fiser.getCurOutCard(getTurnNum());

            List<Integer> cards = null;
            if (color == zhuColor) {
                cards = CardUtils.getZhu(curList, color);
            } else {
                cards = CardUtils.getColorCards(curList, color);
            }
            // 没有这个花色
            if (cards == null || cards.isEmpty()) {
                CardUtils.sortCards(curList);
                int addC = list.size();
                disList.addAll(curList.subList(0, addC));
            } else if (cards.size() < list.size()) {
                disList.addAll(cards);
                curList.removeAll(cards);
                CardUtils.sortCards(curList);
                int addC = list.size() - cards.size();
                disList.addAll(curList.subList(0, addC));
            } else {

                if (type == CardType.DAN) {
                    disList.add(cards.get(0));
                } else if (type == CardType.DUI || type == CardType.TUOLAJI) {
                    int needDuiCount = list.size() / 2;
                    List<Integer> dui = CardUtils.getDuiCards(cards, needDuiCount);
                    if (dui.isEmpty()) {
                        disList.addAll(cards.subList(0, list.size()));
                    } else {
                        disList.addAll(dui);
                        // 对子数小于别人的
                        if (dui.size() / 2 < needDuiCount) {
                            cards.removeAll(dui);
                            CardUtils.sortCards(cards);
                            disList.addAll(cards.subList(0, list.size() - dui.size()));
                        }
                    }

                }

            }

        }

        playCommand(player, 0, disList);
    }


    public boolean checkPlayerAuto(YybsPlayer player, int timeout) {
        if (player.isAutoPlay()) {
            return true;
        }
        long now = TimeUtil.currentTimeMillis();
        boolean auto = false;
        if (player.isAutoPlayChecked() || (player.getAutoPlayCheckedTime() >= timeout && !player.isAutoPlayCheckedTimeAdded())) {
            player.setAutoPlayChecked(true);
            timeout = autoTimeOut2;
        }
        if (player.getLastCheckTime() > 0) {
            int checkedTime = (int) (now - player.getLastCheckTime());
            if (checkedTime >= timeout) {
                auto = true;
            }
            if (auto) {
                player.setAutoPlay(true, this);
            }
        } else {
            player.setLastCheckTime(now);
            player.setAutoPlayCheckedTimeAdded(false);
        }

        return auto;
    }

    private String cardMarkerToJSON() {
        JSONObject jsonObject = new JSONObject();
        for (Map.Entry<Integer, YybsPlayer> entry : seatMap.entrySet()) {
            jsonObject.put("" + entry.getKey(), entry.getValue().getOutPais());
        }
        return jsonObject.toString();
    }


    public int getShowCardNumber() {
        return showCardNumber;
    }

    public void setShowCardNumber(int showCardNumber) {
        this.showCardNumber = showCardNumber;
        changeExtend();
    }


    @Override
    public void createTable(Player player, int play, int bureauCount, List<Integer> params, List<String> strParams, Object... objects) throws Exception {
        createTable(player, play, bureauCount, params);
    }

    @Override
    public void createTable(Player player, int play, int bureauCount, Object... objects) throws Exception {

    }

    @Override
    public boolean isCreditTable(List<Integer> params) {
        return params != null && params.size() > 13 && StringUtil.getIntValue(params, 13, 0) == 1;
    }

    public String getGameName() {
        return "益陽巴十";
    }

    public static final List<Integer> wanfaList = Arrays.asList(GameUtil.play_type_pk_yybs);


    public static void loadWanfaTables(Class<? extends BaseTable> cls) {
        for (Integer integer : wanfaList) {
            TableManager.wanfaTableTypesPut(integer, cls);
        }
    }

    @Override
    public boolean allowRobotJoin() {
        return StringUtils.contains(ResourcesConfigsUtil.loadServerPropertyValue("robot_modes", ""), new StringBuilder().append("|").append(modeId).append("|").toString());
    }

    public void setTableStatus(int tableStatus) {
        this.tableStatus = tableStatus;
        changeExtend();
    }

    public int getTableStatus() {
        return tableStatus;
    }


    @Override
    public boolean isAllReady() {
        return isAllReady1();
//        }else {
//            return super.isAllReady();
//        }
    }

    public boolean isAllReady1() {
        if (super.isAllReady()) {
            if (playBureau != 1) {
                return true;
            }
            // 只有第一局需要推送打鸟消息
            if (jiaoFen > 0) {
                boolean isAllDaNiao = true;
                if (this.isTest()) {
                    // 机器人默认处理
//                    for (YybsPlayer robotPlayer : seatMap.values()) {
//                    }
                }
                ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_pdk_daniao, 1);
                for (YybsPlayer player : seatMap.values()) {
                }
                if (!isAllDaNiao) {
                    broadMsgRoomPlayer(com.build());
                }
                return isAllDaNiao;
            } else {
                for (YybsPlayer player : seatMap.values()) {
                }
                return true;
            }
        }
        return false;
    }


    @Override
    public boolean canQuit(Player player) {
        if (state == table_state.play || playedBureau > 0 || isMatchRoom() || isGoldRoom()) {
            return false;
        } else if (state == table_state.ready) {
            return true;
        } else {
            return true;
        }
    }


    public boolean isNewRound() {
        return newRound;
    }

    public void setNewRound(boolean newRound) {
        this.newRound = newRound;
        changeExtend();
    }


    public int getBanker() {
        return banker;
    }

    @Override
    public int getDissPlayerAgreeCount() {
        return getPlayerCount();
    }

    public boolean isChouLiu() {
        return chouLiu == 1;
    }

    public boolean isChouEr() {
        return option_red2 == 1;
    }

    public boolean isDaiEr() {
        return option_red2 == 2;
    }

    public long getTouxiangTime() {
        return touxiangTime;
    }

    public void setTouxiangTime(long touxiangTime) {
        this.touxiangTime = touxiangTime;
    }

    public String getTableMsg() {
        Map<String, Object> json = new HashMap<>();
        json.put("wanFa", "益陽巴十");
        if (isGroupRoom()) {
            json.put("roomName", getRoomName());
        }
        json.put("playerCount", getPlayerCount());
        json.put("count", getTotalBureau());
        if (autoPlay) {
            json.put("autoTime", autoTimeOut2 / 1000);
            if (autoPlayGlob == 1) {
                json.put("autoName", "单局");
            } else {
                json.put("autoName", "整局");
            }
        }
        return JSON.toJSONString(json);
    }


    public void setDipai(List<Integer> dipai) {
        this.dipai = dipai;
        dbParamMap.put("handPai9", JSON_TAG);
    }

    public int getTurnFirstSeat() {
        return turnFirstSeat;
    }

    public void setTurnFirstSeat(int turnFirstSeat) {
        this.turnFirstSeat = turnFirstSeat;
    }

    public int getTurnNum() {
        return turnNum;
    }

    public void setJiaoZhu(int jiaoFen) {
        this.jiaoFen = jiaoFen;
        changeExtend();
    }

    public void setTurnNum(int turnNum) {
        this.turnNum = turnNum;
    }

    public void addTurnNum(int turnNum) {
        this.turnNum += turnNum;
        changeExtend();
    }

    public String getJiaozhuCardStr() {
        return jiaozhuCardStr;
    }

    public void setJiaozhuCardStr(String jiaozhuCardStr) {
        this.jiaozhuCardStr = jiaozhuCardStr;
        changeExtend();
    }

    public static void main(String[] args) {
        System.out.println(11);
    }

    public List<Integer> getTeamSeat() {
        return teamSeat;
    }

    public void setTeamSeat(List<Integer> teamSeat) {
        this.teamSeat = teamSeat;
    }

    public int isIs_1v3() {
        return is_1v3;
    }

    public void setIs_1v3(int is_1v3) {
        this.is_1v3 = is_1v3;
    }
}
