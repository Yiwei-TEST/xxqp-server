package com.sy599.game.qipai.symj.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.google.protobuf.GeneratedMessage;
import com.sy599.game.GameServerConfig;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.common.constant.SharedConstants.player_state;
import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.db.bean.TableInf;
import com.sy599.game.db.bean.UserPlaylog;
import com.sy599.game.db.dao.TableDao;
import com.sy599.game.db.dao.TableLogDao;
import com.sy599.game.db.dao.UserDao;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.manager.TableManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.ComMsg.ComRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.MoMajiangRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.PlayMajiangRes;
import com.sy599.game.msg.serverPacket.TableMjResMsg.ClosingMjInfoRes;
import com.sy599.game.msg.serverPacket.TableMjResMsg.ClosingMjPlayerInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.CreateTableRes;
import com.sy599.game.msg.serverPacket.TableRes.DealInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes;
import com.sy599.game.qipai.symj.constant.SyMjConstants;
import com.sy599.game.qipai.symj.rule.SyMj;
import com.sy599.game.qipai.symj.rule.SyMjRobotAI;
import com.sy599.game.qipai.symj.tool.SyMjHelper;
import com.sy599.game.qipai.symj.tool.SyMjQipaiTool;
import com.sy599.game.qipai.symj.tool.SyMjResTool;
import com.sy599.game.qipai.symj.tool.SyMjTool;
import com.sy599.game.qipai.symj.tool.hulib.util.HuUtil;
import com.sy599.game.udplog.UdpLogger;
import com.sy599.game.util.DataMapUtil;
import com.sy599.game.util.GameUtil;
import com.sy599.game.util.JacksonUtil;
import com.sy599.game.util.JsonWrapper;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.util.StringUtil;
import com.sy599.game.util.TimeUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;


public class SyMjTable extends BaseTable {
    /**
     * 当前打出的牌
     */
    private List<SyMj> nowDisCardIds = new ArrayList<>();
    protected List<Integer> dices;
    private Map<Integer, List<Integer>> actionSeatMap = new ConcurrentHashMap<>();
    /**
     * 玩家位置对应临时操作
     * 当同时存在多个可做的操作时
     * 1当优先级最高的玩家最先操作 则清理所有操作提示 并执行该操作
     * 2当操作优先级低的玩家先选择操作，则玩家先将所做操作存入临时操作中 当玩家优先级最高的玩家操作后 在判断临时操作是否可执行并执行
     */
    private Map<Integer, SyMjTempAction> tempActionMap = new ConcurrentHashMap<>();
    private int maxPlayerCount = 4;
    private List<SyMj> leftMajiangs = new ArrayList<>();
    /*** 玩家map */
    private Map<Long, SyMjPlayer> playerMap = new ConcurrentHashMap<Long, SyMjPlayer>();
    /*** 座位对应的玩家 */
    private Map<Integer, SyMjPlayer> seatMap = new ConcurrentHashMap<Integer, SyMjPlayer>();
    private List<Integer> huConfirmList = new ArrayList<>();//胡牌数组
    /**
     * 摸麻将的seat
     */
    private int moMajiangSeat;
    /**
     * 抢杠胡   杠的麻将
     */
    private SyMj moGang;
    /**
     * 当前杠的人
     */
    private int moGangSeat;
    private int moGangSameCount;

    /**
     * 抢杠胡 位置
     */
    private List<Integer> moGangHuList = new ArrayList<>();
    /**
     * 放杠人位置 如果是自己摸来位置为自己
     */
    private int fangGangSeat;
    /**
     * 杠开胡 位置
     */
    private int gangKaiSeat;
    /**
     * 杠上炮  杠后一个打出牌
     */
    private boolean isGangPao;
    /**
     * 骰子点数
     **/
    private int dealDice;
    /**
     * 抓鸟个数
     **/
    private int birdNum;
    /**
     * 带风
     **/
    private int daiFeng;
    /**
     * 可以吃 1可吃 2清一色可吃
     **/
    private int keChi;
    /**
     * 可锤
     **/
    private int keChui;

    private int tableStatus;//特殊状态 1锤
    private int isAutoPlay;//是否开启自动托管
    /**
     * 抢杠胡开关
     **/
    private int qiangGangHu;
    /**
     * 抢杠胡包三家
     **/
    private int qiangGangHuBaoSanJia;
    /**
     * 上下中鸟
     **/
    private int shangZhongXiaNiao;
    /**
     * 点杠可胡
     **/
    private int dianGangKeHu;
    /**
     * 点杠三家付
     **/
    private int dianGangSanJiaFu;
    /**
     * 杠开胡包三家
     **/
    private int gangKaiHuBaoSanJia;
    /**
     * 杠后炮三家付
     **/
    private int gangHouPaoSanJiaFu;
    /**
     * 抢杠胡算自摸
     **/
    private int qiangGangHuSuanZiMo;
    /**
     * 底分
     **/
    private int diFen;
    
    /**托管1：单局，2：全局*/
    private int autoPlayGlob;
    
    //是否加倍：0否，1是
    private int jiaBei;
    //加倍分数：低于xx分进行加倍
    private int jiaBeiFen;
    //加倍倍数：翻几倍
    private int jiaBeiShu;
    
	private int readyTime = 0;

    /*** 摸屁股的座标号*/
    private List<Integer> moTailPai = new ArrayList<>();


    public boolean isGangPao() {
        return isGangPao;
    }

    public void setGangPao(boolean isGangPao) {
        this.isGangPao = isGangPao;
        changeExtend();
    }

    public int getGangKaiSeat() {
        return gangKaiSeat;
    }

    public void setGangKaiSeat(int gangKaiSeat) {
        this.gangKaiSeat = gangKaiSeat;
        changeExtend();
    }

    public int getFangGangSeat() {
        return fangGangSeat;
    }

    public void setFangGangSeat(int fangGangSeat) {
        this.fangGangSeat = fangGangSeat;
        changeExtend();
    }

    public int getTableStatus() {
        return tableStatus;
    }

    public void setTableStatus(int tableStatus) {
        this.tableStatus = tableStatus;
    }

    public int getDealDice() {
        return dealDice;
    }

    public void setDealDice(int dealDice) {
        this.dealDice = dealDice;
    }

    public int getKeChui() {
        return keChui;
    }

    public void setKeChui(int keChui) {
        this.keChui = keChui;
    }

    public int getDaiFeng() {
        return daiFeng;
    }

    public void setDaiFeng(int daiFeng) {
        this.daiFeng = daiFeng;
    }

    public int getKeChi() {
        return keChi;
    }

    public void setKeChi(int keChi) {
        this.keChi = keChi;
    }

    public boolean isKeChi() {
        return keChi > 0;
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
        List<Integer> winList = new ArrayList<>(huConfirmList);
        boolean selfMo = false;
        int[] birdMjIds = null;//抓的鸟牌Id
        int[] seatBirds = null;//中鸟的位置
        Map<Integer, Integer> seatBridMap = new HashMap<>();//位置,中鸟数
        int catchBirdSeat = 0;//抓鸟人座位
        if (winList.size() == 0 && leftMajiangs.isEmpty()) {
            // 流局
        } else {
            // 先判断是自摸还是放炮
            if (winList.size() == 1 && seatMap.get(winList.get(0)).getHandMajiang().size() % 3 == 2 && winList.get(0) == moMajiangSeat) {
                selfMo = true;
            }
            if (getBirdNum() > 0 && !leftMajiangs.isEmpty()) {
                // 先砸鸟
                birdMjIds = zhuaNiao(getBirdNum());
                // 一炮多响按放炮的座位开始算
                int startSeat = winList.size() > 1 ? disCardSeat : winList.get(0);
                // 中鸟的座位
                seatBirds = birdToSeat(birdMjIds, startSeat, selfMo);
                catchBirdSeat = startSeat;
            }
            if (selfMo) {
                // 自摸
                SyMjPlayer winPlayer = seatMap.get(winList.get(0));

                // 中鸟
                int birdPoint = seatBirds == null ? 0 : calcBirdPoint(seatBirds, winPlayer.getSeat());
                seatBridMap.put(winList.get(0), birdPoint);

                //大胡
                int daHuFen = SyMjTool.calcDaHuPoint(winPlayer.getDahu());
                //杠开并点杠
                boolean dianGangKai = (getGangKaiSeat() == winPlayer.getSeat() && getFangGangSeat() != winPlayer.getSeat());

                int loseTotalPoint = 0;
                int winPoint = 0;
                for (int seat : seatMap.keySet()) {
                    if (!winList.contains(seat)) {
                        int losePoint = 0;
                        //计大胡分
                        if (daHuFen > 0) {
                            losePoint += daHuFen;
                        } else {
                            losePoint += 2;
                        }

                        losePoint *= diFen;

                        losePoint += birdPoint;
                        //赢家锤
                        if (winPlayer.getChui() == 1) {
                            losePoint *= 2;
                        }
                        //输家锤
                        SyMjPlayer player = seatMap.get(seat);
                        if (player.getChui() == 1) {
                            losePoint *= 2;
                        }
                        winPoint += losePoint;
                        if (dianGangKai) {
                            //接杠后摸牌自摸
                            if (gangKaiHuBaoSanJia == 1) {
                                //不包三家，三家都出
                                loseTotalPoint += losePoint;
                            } else {
                                player.changeLostPoint(-losePoint);
                            }
                        } else {
                            player.changeLostPoint(-losePoint);
                        }
                    }
                }
                winPlayer.changeAction(SyMjConstants.ACTION_COUNT_INDEX_ZIMO, 1);
                winPlayer.changeLostPoint(winPoint);
                if (dianGangKai && gangKaiHuBaoSanJia == 1) {
                    //杠开胡，且包三家，点杠者出
                    seatMap.get(getFangGangSeat()).changeLostPoint(-loseTotalPoint);
                }
            } else {
                // 小胡接炮 每人1分
                // 如果庄家输牌失分翻倍
                SyMjPlayer losePlayer = seatMap.get(disCardSeat);
                int loseTotalPoint = 0;
                int fangPaoSeat = disCardSeat;

                for (int winnerSeat : winList) {
                    SyMjPlayer winPlayer = seatMap.get(winnerSeat);
                    // 鸟分
                    int birdPoint = 0;

                    if (winList.size() > 1) {
                        //一炮多响，算放炮者中鸟
                        birdPoint = seatBirds == null ? 0 : calcBirdPoint(seatBirds, disCardSeat);
                        seatBridMap.put(disCardSeat, birdPoint);
                    } else {
                        birdPoint = seatBirds == null ? 0 : calcBirdPoint(seatBirds, winnerSeat);
                        seatBridMap.put(winnerSeat, birdPoint);
                    }

                    // 大胡分
                    int daHuFen = SyMjTool.calcDaHuPoint(winPlayer.getDahu());

                    if (winPlayer.getDahu().contains(SyMjConstants.HU_QIANGGANGHU)) {
                        //抢杠胡
                        int winPoint = 0;
                        for (int playerSeat : seatMap.keySet()) {

                            if (playerSeat == winnerSeat) {
                                continue;
                            }
                            if (qiangGangHuSuanZiMo != 1 && playerSeat != fangPaoSeat) {
                                //抢杠胡不算自摸，只有点炮方出钱
                                continue;
                            }
                            int losePoint = 0;

                            if (daHuFen > 0) {
                                losePoint += daHuFen;
                            } else {
                                losePoint += qiangGangHuSuanZiMo == 1 ? 2 : 1;
                            }

                            losePoint *= diFen;

                            losePoint += birdPoint;


                            if (winPlayer.getChui() == 1) {
                                losePoint *= 2;
                            }
                            if (seatMap.get(playerSeat).getChui() == 1) {
                                losePoint *= 2;
                            }
                            winPoint += losePoint;
                            if (qiangGangHuBaoSanJia == 1) {
                                //包三家，由点炮方出
                                loseTotalPoint += losePoint;
                            } else {
                                //不包三家，每方都出，点炮方最后出
                                if (playerSeat == fangPaoSeat) {
                                    loseTotalPoint += losePoint;
                                } else {
                                    if (playerSeat != winnerSeat) {
                                        seatMap.get(playerSeat).changeLostPoint(-losePoint);
                                    }
                                }
                            }
                        }
                        winPlayer.changeLostPoint(winPoint);
                    } else {
                        //杠后炮
                        int winPoint = 0;
                        for (int playerSeat : seatMap.keySet()) {
                            if (playerSeat == winnerSeat) {
                                continue;
                            }

                            int losePoint = 0;

                            if (daHuFen > 0) {
                                losePoint += daHuFen;
                            } else {
                                losePoint += 2;
                            }

                            losePoint *= diFen;

                            losePoint += birdPoint;

                            if (winPlayer.getChui() == 1) {
                                losePoint *= 2;
                            }
                            if (seatMap.get(playerSeat).getChui() == 1) {
                                losePoint *= 2;
                            }

                            winPoint += losePoint;

                            if (gangHouPaoSanJiaFu == 1) {
                                if (playerSeat == fangPaoSeat) {
                                    loseTotalPoint += losePoint;
                                } else {
                                    if (playerSeat != winnerSeat) {
                                        seatMap.get(playerSeat).changeLostPoint(-losePoint);
                                    }
                                }
                            } else {
                                loseTotalPoint += losePoint;
                            }
                        }
                        winPlayer.changeLostPoint(winPoint);
                    }
                    winPlayer.changeAction(SyMjConstants.ACTION_COUNT_INDEX_JIEPAO, 1);
                    losePlayer.changeAction(SyMjConstants.ACTION_COUNT_INDEX_DIANPAO, 1);
                }
                losePlayer.changeLostPoint(-loseTotalPoint);
            }
        }
        // 不管流局都加分
        for (SyMjPlayer seat : seatMap.values()) {
            seat.changePoint(seat.getLostPoint());
        }
        
        
        
		boolean isOver = playBureau >= totalBureau;
		
		
        if(autoPlayGlob >0) {
//          //是否解散
            boolean diss = false;
            if(autoPlayGlob ==1) {
            	 for (SyMjPlayer seat : seatMap.values()) {
                 	if(seat.isAutoPlay()) {
                     	diss = true;
                     	break;
                     }
                 }
            }
            if(diss) {
            	 autoPlayDiss= true;
            	 isOver =true;
            }
        }
		
        
        
        ClosingMjInfoRes.Builder res = sendAccountsMsg(isOver, selfMo, winList, birdMjIds, seatBirds, seatBridMap, catchBirdSeat, false);
        if (!winList.isEmpty()) {
            if (winList.size() > 1) {
                // 一炮多响设置放炮的人为庄家
                setLastWinSeat(disCardSeat);
            } else {
                setLastWinSeat(winList.get(0));
            }
        } else if (leftMajiangs.isEmpty()) {//黄庄
            setLastWinSeat(moMajiangSeat);
        }
        if (getKeChui() == 1) {
            for (int seat : seatMap.keySet()) {
                SyMjPlayer player = seatMap.get(seat);
                if (player.getChui() == 1 && winList.contains(seat)) {
                    continue;
                }
                player.setChui(-1);
            }
        }
        calcAfter();
        
        
        saveLog(isOver, 0l, res.build());
        if (isOver) {
            calcOver1();
            calcOver2();
            calcOver3();
            diss();
        } else {
            initNext();
            calcOver1();
        }
        for (SyMjPlayer player : seatMap.values()) {
            if (player.isAutoPlaySelf()) {
                player.setAutoPlay(false, false);
            }
        }
        for (Player player : seatMap.values()) {
            player.saveBaseInfo();
        }
    }

    public void saveLog(boolean over, long winId, Object resObject) {
        ClosingMjInfoRes res = (ClosingMjInfoRes) resObject;
        LogUtil.d_msg("tableId:" + id + " play:" + playBureau + " over:" + res);
        String logRes = JacksonUtil.writeValueAsString(LogUtil.buildMJClosingInfoResLog(res));
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
        userLog.setExtend(logOtherRes);
//		userLog.setMasterName(getMasterName());
        userLog.setType(creditMode == 1 ? 2 : 1 );
        userLog.setMaxPlayerCount(maxPlayerCount);
        userLog.setGeneralExt(buildGeneralExtForPlaylog().toString());
        long logId = TableLogDao.getInstance().save(userLog);
        saveTableRecord(logId, over, playBureau);
        for (SyMjPlayer player : playerMap.values()) {
            player.addRecord(logId, playBureau);
        }
        UdpLogger.getInstance().sendSnapshotLog(masterId, playLog, logRes);
    }

    public String getMasterName() {
        Player master = PlayerManager.getInstance().getPlayer(creatorId);
        String masterName = "";
        if (master == null) {
            masterName = UserDao.getInstance().selectNameByUserId(creatorId);
        } else {
            masterName = master.getName();
        }
        return masterName;
    }

    private int calcBirdPoint(int[] seatBirdArr, int seat) {
        return seatBirdArr[seat];
    }

    /**
     * 抓鸟
     *
     * @return
     */
    private int[] zhuaNiao(int birdNum) {
        int[] birdMjIds = null;
        if (shangZhongXiaNiao == 1) {
            SyMj birdMj = getLeftMajiang();
            if (birdMj != null) {
                if (birdMj.isFeng() || birdMj.isZhongFaBai()) {
                    birdMjIds = new int[1];
                    birdMjIds[0] = birdMj.getId();
                } else {
                    birdMjIds = new int[3];
                    int[] birdMjVals = new int[3];
                    if (birdMj.getVal() % 10 == 1) {
                        birdMjVals[0] = birdMj.getHuase() * 10 + 9;
                        birdMjVals[1] = birdMj.getVal();
                        birdMjVals[2] = birdMj.getVal() + 1;
                    } else if (birdMj.getVal() % 10 == 9) {
                        birdMjVals[0] = birdMj.getVal() - 1;
                        birdMjVals[1] = birdMj.getVal();
                        birdMjVals[2] = birdMj.getHuase() * 10 + 1;
                    } else {
                        birdMjVals[0] = birdMj.getVal() - 1;
                        birdMjVals[1] = birdMj.getVal();
                        birdMjVals[2] = birdMj.getVal() + 1;
                    }
                    for (int i = 0; i < birdMjVals.length; i++) {
                        if (i == 1) {
                            birdMjIds[i] = birdMj.getId();
                        } else {
                            SyMj mj = SyMj.getMajiangByValue(birdMjVals[i]);
                            if (mj != null) {
                                birdMjIds[i] = mj.getId();
                            }
                        }
                    }
                }
            }
        } else {
            if (birdNum > leftMajiangs.size()) {
                birdNum = leftMajiangs.size();
            }
            // 先砸鸟
            birdMjIds = new int[birdNum];
            for (int i = 0; i < birdNum; i++) {
                SyMj birdMj = getLeftMajiang();
                if (birdMj != null) {
                    birdMjIds[i] = birdMj.getId();
                }
            }
        }
        return birdMjIds;
    }

    /**
     * 中鸟
     *
     * @param birdMjIds
     * @param winSeat
     * @return arr[seat] = 中鸟数
     */
    private int[] birdToSeat(int[] birdMjIds, int winSeat, boolean selfMo) {
        int[] seatArr = new int[getMaxPlayerCount() + 1];
        if (shangZhongXiaNiao == 1) {
            Map<Integer, Integer> mjValCountMap = new HashMap();
            for (int birdMjId : birdMjIds) {
                SyMj mj = SyMj.getMajang(birdMjId);
                if (mjValCountMap.containsKey(mj.getVal())) {
                    mjValCountMap.put(mj.getVal(), mjValCountMap.get(mj.getVal()) + 1);
                } else {
                    mjValCountMap.put(mj.getVal(), 1);
                }
            }
            SyMjPlayer player = seatMap.get(winSeat);
            List<SyMj> allMj = new ArrayList(player.getHandMajiang());
            allMj.addAll(player.getGang());
            allMj.addAll(player.getPeng());
            allMj.addAll(player.getaGang());
            if (!selfMo) {
                SyMj huMj = !nowDisCardIds.isEmpty() ? nowDisCardIds.get(0) : null;
                if (huMj != null) {
                    allMj.add(huMj);
                }
            }
            for (SyMj handMj : allMj) {
                if (mjValCountMap.containsKey(handMj.getVal())) {
                    seatArr[player.getSeat()] = seatArr[player.getSeat()] + mjValCountMap.get(handMj.getVal());
                }
            }
        } else {
            for (int i = 0; i < birdMjIds.length; i++) {
                SyMj mj = SyMj.getMajang(birdMjIds[i]);
                if (mj != null && !mj.isFeng() && !mj.isZhongFaBai()) {
                    int birdMjVal = (mj.getPai() - 1) % 4;// 从自己开始算 所以减1
                    int birdSeat = birdMjVal + winSeat > 4 ? birdMjVal + winSeat - 4 : birdMjVal + winSeat;
//					seatArr[birdSeat] = seatArr[birdSeat] + 1;
                    if (birdSeat == winSeat) {
                        seatArr[birdSeat] = seatArr[birdSeat] + 1;
                    }
                }
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
                tempMap.put("nowDisCardIds", StringUtil.implode(SyMjHelper.toMajiangIds(nowDisCardIds), ","));
            }
            if (tempMap.containsKey("leftPais")) {
                tempMap.put("leftPais", StringUtil.implode(SyMjHelper.toMajiangIds(leftMajiangs), ","));
            }
            if (tempMap.containsKey("nowAction")) {
                tempMap.put("nowAction", buildNowAction());
            }
            if (tempMap.containsKey("extend")) {
                tempMap.put("extend", buildExtend());
            }
        }
        return tempMap.size() > 0 ? tempMap : null;
    }

    @Override
    public JsonWrapper buildExtend0(JsonWrapper wrapper) {
        for (SyMjPlayer player : seatMap.values()) {
            wrapper.putString(player.getSeat(), player.toExtendStr());
        }
        wrapper.putString(5, StringUtil.implode(huConfirmList, ","));
        wrapper.putInt(6, birdNum);
        wrapper.putInt(7, moMajiangSeat);
        if (moGang != null) {
            wrapper.putInt(8, moGang.getId());
        } else {
            wrapper.putInt(8, 0);
        }
        wrapper.putString(9, StringUtil.implode(moGangHuList, ","));
        wrapper.putInt(10, daiFeng);
        wrapper.putInt(11, keChi);
        wrapper.putInt(12, keChui);
        JSONArray tempJsonArray = new JSONArray();
        for (int seat : tempActionMap.keySet()) {
            tempJsonArray.add(tempActionMap.get(seat).buildData());
        }
        wrapper.putString("tempActions", tempJsonArray.toString());
        wrapper.putInt(13, maxPlayerCount);
        wrapper.putInt(14, dealDice);
        wrapper.putInt(15, tableStatus);
        wrapper.putInt(16, fangGangSeat);
        wrapper.putInt(17, gangKaiSeat);
        wrapper.putInt(18, isGangPao ? 1 : 0);
        wrapper.putInt(19, qiangGangHu);
        wrapper.putInt(20, qiangGangHuBaoSanJia);
        wrapper.putInt(21, shangZhongXiaNiao);
        wrapper.putInt(22, dianGangKeHu);
        wrapper.putInt(23, dianGangSanJiaFu);
        wrapper.putInt(24, gangKaiHuBaoSanJia);
        wrapper.putInt(25, gangHouPaoSanJiaFu);
        wrapper.putInt(26, qiangGangHuSuanZiMo);
        wrapper.putInt(27, moGangSeat);
        wrapper.putInt(28, moGangSameCount);
        wrapper.putInt(29, isAutoPlay);
        wrapper.putString(30, StringUtil.implode(moTailPai, ","));
        wrapper.putInt(31, diFen);
        
        
        
		
		wrapper.putInt(32, jiaBei);
		wrapper.putInt(33, jiaBeiFen);
		wrapper.putInt(34, jiaBeiShu);
		
		wrapper.putInt(35, autoPlayGlob);
        
        return wrapper;
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
        int dealDice = 0;
        Random r = new Random();
        dealDice = (r.nextInt(6) + 1) * 10 + (r.nextInt(6) + 1);
        addPlayLog(disCardRound + "_" + lastWinSeat + "_" + SyMjDisAction.action_dice + "_" + dealDice);
        setDealDice(dealDice);
        // 天胡或者暗杠
        logFaPaiTable();
        for (SyMjPlayer tablePlayer : seatMap.values()) {
            DealInfoRes.Builder res = DealInfoRes.newBuilder();
            if (lastWinSeat == tablePlayer.getSeat()) {
                List<Integer> actionList = tablePlayer.checkMo(null);
                if (!actionList.isEmpty()) {
                    addActionSeat(tablePlayer.getSeat(), actionList);
                    res.addAllSelfAct(actionList);
                    logFaPaiPlayer(tablePlayer, actionList);
                }
            }
            res.addAllHandCardIds(tablePlayer.getHandPais());
            res.setNextSeat(getNextDisCardSeat());
            res.setGameType(getWanFa());
            res.setRemain(leftMajiangs.size());
            res.setBanker(lastWinSeat);
            res.setDealDice(dealDice);
            tablePlayer.writeSocket(res.build());
            if (tablePlayer.isAutoPlay()) {
                tablePlayer.setAutoPlayTime(0);
            }
            sendTingInfo(tablePlayer);
            if(tablePlayer.isAutoPlay()) {
            	addPlayLog(getDisCardRound() + "_" +tablePlayer.getSeat() + "_" + SyMjConstants.action_tuoguan + "_" +1);
           }
            logFaPaiPlayer(tablePlayer, null);
        }
        for (Player player : getRoomPlayerMap().values()) {
            DealInfoRes.Builder res = DealInfoRes.newBuilder();
            res.setNextSeat(getNextDisCardSeat());
            res.setGameType(getWanFa());
            res.setRemain(leftMajiangs.size());
            res.setBanker(lastWinSeat);
            res.setDealDice(dealDice);
            player.writeSocket(res.build());
        }
        if (playBureau == 1) {
            setCreateTime(new Date());
        }
    }

    public void moMajiang(SyMjPlayer player) {
        moMajiang(player, false);
    }

    public void moMajiang(SyMjPlayer player, boolean isBuZhang) {
        if (state != table_state.play) {
            return;
        }
        if (player.isRobot()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // 摸牌
        SyMj majiang = null;
        if (disCardRound != 0) {
            // 玩家手上的牌是双数，已经摸过牌了
            if (player.isAlreadyMoMajiang()) {
                return;
            }
            if (getLeftMajiangCount() == 0) {
                calcOver();
                return;
            }
            majiang = getLeftMajiang();
        }
        if (majiang != null) {
            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + SyMjDisAction.action_moMjiang + "_" + majiang.getId() + player.getExtraPlayLog());
            player.moMajiang(majiang);
        }
        // 检查摸牌
        clearActionSeatMap();
        if (disCardRound == 0) {
            return;
        }
        if (isBuZhang) {
            addMoTailPai(-1);
        }

        setMoMajiangSeat(player.getSeat());
        List<Integer> arr = player.checkMo(majiang);
        if (!arr.isEmpty()) {
            addActionSeat(player.getSeat(), arr);
            if (isBuZhang && (arr.get(SyMjConstants.ACTION_INDEX_HU) == 1 || arr.get(SyMjConstants.ACTION_INDEX_ZIMO) == 1)) {
                //杠开位置
                setGangKaiSeat(player.getSeat());
            }
        }
        setGangPao(isBuZhang);
        logMoMj(player, majiang, arr);
        MoMajiangRes.Builder res = MoMajiangRes.newBuilder();
        res.setUserId(player.getUserId() + "");
        res.setSeat(player.getSeat());
        for (SyMjPlayer seat : seatMap.values()) {
            if (seat.getUserId() == player.getUserId()) {
                MoMajiangRes.Builder copy = res.clone();
                copy.addAllSelfAct(arr);
                if (majiang != null) {
                    copy.setMajiangId(majiang.getId());
                }
                seat.writeSocket(copy.build());
            } else {
                seat.writeSocket(res.build());
            }
        }
        sendTingInfo(player);
        for (Player roomPlayer : roomPlayerMap.values()) {
            MoMajiangRes.Builder copy = res.clone();
            roomPlayer.writeSocket(copy.build());
        }
    }

    /**
     * 玩家表示胡
     *
     * @param player
     * @param majiangs
     */
    private void hu(SyMjPlayer player, List<SyMj> majiangs, int action) {
        if (state != table_state.play) {
            return;
        }
        List<Integer> actionList = actionSeatMap.get(player.getSeat());
        if (actionList == null || (actionList.get(SyMjConstants.ACTION_INDEX_HU) != 1 && actionList.get(SyMjConstants.ACTION_INDEX_ZIMO) != 1)) {// 如果集合为空或者第一操作不为胡，则返回
            return;
        }
//		if (!checkAction(player, majiangs, new ArrayList<Integer>(), action)) {// 检查优先度，胡杠碰吃 如果同时出现一个事件，按出牌座位顺序优先
//			player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip);
//			LogUtil.msg("有优先级更高的操作需等待！");
//			return;
//		}//一炮多响去掉
        if (huConfirmList.contains(player.getSeat())) {
            return;
        }
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        List<SyMj> huHand = new ArrayList<>(player.getHandMajiang());
        boolean zimo = player.isAlreadyMoMajiang();
        SyMj disMajiang = null;
        if (!zimo) {
            if (moGangHuList.contains(player.getSeat())) {
                // 抢杠胡
                huHand.add(moGang);
                disMajiang = moGang;
                builder.setFromSeat(nowDisCardSeat);
                player.getDahu().add(SyMjConstants.HU_QIANGGANGHU);
                SyMjPlayer fangPaoPlayer = seatMap.get(nowDisCardSeat);
                fangPaoPlayer.getDahu().add(SyMjConstants.HU_FANGPAO);
            } else {
                // 放炮
                huHand.addAll(nowDisCardIds);
                disMajiang = nowDisCardIds.get(0);
                builder.setFromSeat(disCardSeat);
                player.getDahu().add(SyMjConstants.HU_JIPAO);
                SyMjPlayer gangPaoPlayer = seatMap.get(disCardSeat);
                gangPaoPlayer.getDahu().add(SyMjConstants.HU_GANGPAO);
            }
        } else {
            if (getGangKaiSeat() == player.getSeat()) {
                player.getDahu().add(SyMjConstants.HU_GANGKAI);
                if (player.getSeat() != getFangGangSeat()) {
                    SyMjPlayer fangGangPlayer = seatMap.get(getFangGangSeat());
                    fangGangPlayer.getDahu().add(SyMjConstants.HU_DIANGANG);
                }
            } else {
                player.getDahu().add(SyMjConstants.HU_ZIMO);
            }
        }
        SyMjHu hu = SyMjTool.isHu(player, disMajiang);
        if (!hu.isHu()) {
            return;
        }
        if (hu.isPingHu()) {
            player.getDahu().add(SyMjConstants.HU_PINGHU);
        }
        if (moGangHuList.contains(player.getSeat())) {
            SyMjPlayer moGangPlayer = seatMap.get(moGangSeat);
            if (moGangPlayer == null) {
                moGangPlayer = getPlayerByHasMajiang(moGang);
            }
            if (moGangPlayer == null) {
                moGangPlayer = seatMap.get(moMajiangSeat);
            }
            List<SyMj> moGangMajiangs = new ArrayList<>();
            moGangMajiangs.add(moGang);
            moGangPlayer.addOutPais(moGangMajiangs, 0, 0);
            // 摸杠被人胡了 相当于自己出了一张牌
            recordDisMajiang(moGangMajiangs, moGangPlayer);
//			addPlayLog(disCardRound + "_" + moGangPlayer.getSeat() + "_" + SyMjDisAction.action_chupai + "_" + SyMjHelper.toMajiangStrs(moGangMajiangs)+moGangPlayer.getExtraPlayLog());
            moGangPlayer.qGangUpdateOutPais(moGang);
        }
        if (hu.isDaHu()) {
            player.getDahu().addAll(hu.getDaHuList());
        }
        if (!player.getDahu().isEmpty()) {
            builder.addAllHuArray(player.getDahu());
        }
        buildPlayRes(builder, player, action, huHand);
        if (zimo) {
            builder.setZimo(1);
        }
        if (!huConfirmList.isEmpty()) {
            builder.addExt(StringUtil.implode(huConfirmList, ","));
        }
        // 胡
        for (SyMjPlayer seat : seatMap.values()) {
            // 推送消息
            seat.writeSocket(builder.build());
        }
        for (Player roomPlayer : roomPlayerMap.values()) {
            PlayMajiangRes.Builder copy = builder.clone();
            roomPlayer.writeSocket(copy.build());
        }
        // 加入胡牌数组
        addHuList(player.getSeat());
        changeDisCardRound(1);
        List<SyMj> huPai = new ArrayList<>();
        huPai.add(huHand.get(huHand.size() - 1));
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + SyMjHelper.toMajiangStrs(huPai) + "_" + StringUtil.implode(player.getDahu(), ",") + player.getExtraPlayLog());
        logActionHu(player, majiangs, "");
        if (isCalcOver()) {
            // 等待别人胡牌 如果都确认完了，胡
            calcOver();
        } else {
            //removeActionSeat(player.getSeat());
            player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip, action);
        }
    }

    private void buildPlayRes(PlayMajiangRes.Builder builder, Player player, int action, List<SyMj> majiangs) {
        SyMjResTool.buildPlayRes(builder, player, action, majiangs);
        buildPlayRes1(builder);
    }

    private void buildPlayRes1(PlayMajiangRes.Builder builder) {
        // builder
    }

    /**
     * 找出拥有这张麻将的玩家
     *
     * @param majiang
     * @return
     */
    private SyMjPlayer getPlayerByHasMajiang(SyMj majiang) {
        for (SyMjPlayer player : seatMap.values()) {
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
        if (!huActionList.isEmpty()) {
            over = true;
            SyMjPlayer moGangPlayer = null;
            if (!moGangHuList.isEmpty()) {
                // 如果有抢杠胡
                moGangPlayer = seatMap.get(moGangSeat);
                LogUtil.monitor_i("mogang player:" + moGangPlayer.getSeat() + " moGang:" + moGang);

            }
            for (int huseat : huActionList) {
                if (moGangPlayer != null) {
                    // 被抢杠的人可以胡的话 跳过
                    if (moGangPlayer.getSeat() == huseat) {
                        continue;
                    }
                }
                if (!huConfirmList.contains(huseat) &&
                        !(tempActionMap.containsKey(huseat) && tempActionMap.get(huseat).getAction() == SyMjDisAction.action_hu)) {
                    over = false;
                    break;
                }
            }
        }

        if (!over) {
            SyMjPlayer disMajiangPlayer = seatMap.get(disCardSeat);
            for (int huseat : huActionList) {
                if (huConfirmList.contains(huseat)) {
                    continue;
                }
                PlayMajiangRes.Builder disBuilder = PlayMajiangRes.newBuilder();
                SyMjPlayer seatPlayer = seatMap.get(huseat);
                buildPlayRes(disBuilder, disMajiangPlayer, 0, null);
                List<Integer> actionList = actionSeatMap.get(huseat);
                disBuilder.addAllSelfAct(actionList);
                seatPlayer.writeSocket(disBuilder.build());
            }
        }
        for (SyMjPlayer player : seatMap.values()) {
            if (player.isAlreadyMoMajiang() && !huConfirmList.contains(player.getSeat())) {
                over = false;
            }
        }
        return over;
    }

    /**
     * 吃碰杠
     *
     * @param player
     * @param majiangs
     * @param action
     */
    private void chiPengGang(SyMjPlayer player, List<SyMj> majiangs, int action) {
        if (state != table_state.play) {
            return;
        }
        logAction(player, action, majiangs, null);
        if (majiangs == null || majiangs.isEmpty()) {
            return;
        }
        if (!checkAction(player, majiangs, new ArrayList<Integer>(), action)) {
            LogUtil.msg("有优先级更高的操作需等待！");
            player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip);
            return;
        }
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        List<SyMj> handMajiang = new ArrayList<>(player.getHandMajiang());
        SyMj disMajiang = null;
        if (nowDisCardIds.size() > 1) {
            // 当前出牌不能操作
            return;
        }
        List<Integer> huList = getHuSeatByActionMap();
        huList.remove((Object) player.getSeat());
        if (!huList.isEmpty()) {
            return;
        }
        if (!nowDisCardIds.isEmpty()) {
            disMajiang = nowDisCardIds.get(0);
        }
        int sameCount = 0;
        if (majiangs.size() > 0) {
            sameCount = SyMjHelper.getMajiangCount(majiangs, majiangs.get(0).getVal());
        }
        // 如果是杠 后台来找出是明杠还是暗杠
        if (action == SyMjDisAction.action_minggang) {
            majiangs = SyMjHelper.getMajiangList(player.getHandMajiang(), majiangs.get(0).getVal());
            sameCount = majiangs.size();
            if (sameCount == 4) {
                // 有4张一样的牌是暗杠
                action = SyMjDisAction.action_angang;
            }
            // 其他是明杠
        }
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return;
        }
        if (isGangPao) {
            setGangPao(false);
        }
        boolean hasQGangHu = false;
        if (action == SyMjDisAction.action_chi) {
            boolean can = canChi(player, handMajiang, majiangs, disMajiang);
            if (!can) {
                return;
            }
        } else if (action == SyMjDisAction.action_peng) {
            boolean can = canPeng(player, majiangs, sameCount, disMajiang);
            if (!can) {
                return;
            }
        } else if (action == SyMjDisAction.action_angang) {
            boolean can = canAnGang(player, majiangs, sameCount);
            if (!can) {
                return;
            }
            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + SyMjHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
            if (canGangHu()) {
                if (checkQGangHu(player, majiangs, action, sameCount)) {
                    hasQGangHu = true;
                    setNowDisCardSeat(player.getSeat());
                    LogUtil.msg("tid:" + getId() + " " + player.getName() + "可以被抢暗杠胡！！");
                }
            }
        } else if (action == SyMjDisAction.action_minggang) {
            boolean can = canMingGang(player, majiangs, sameCount, disMajiang);
            if (!can) {
                return;
            }
            ArrayList<SyMj> mjs = new ArrayList<>(majiangs);
            if (sameCount == 3) {
                mjs.add(disMajiang);
            }
            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + SyMjHelper.toMajiangStrs(mjs) + player.getExtraPlayLog());
            // 特殊处理一张牌明杠的时候别人可以胡,明杠可抢
            if ((sameCount == 1 && canGangHu())) {
                if (checkQGangHu(player, majiangs, action, sameCount)) {
                    hasQGangHu = true;
                    setNowDisCardSeat(player.getSeat());
                    LogUtil.msg("tid:" + getId() + " " + player.getName() + "可以被抢杠胡！！");
                }
            }
            //点杠可枪
            if (sameCount == 3 && dianGangKeHu == 1) {
                if (checkQGangHu(player, mjs, action, sameCount)) {
                    hasQGangHu = true;
                    setNowDisCardSeat(player.getSeat());
                    LogUtil.msg("tid:" + getId() + " " + player.getName() + "可以被抢杠胡！！");
                }
            }
        } else {
            return;
        }
        if (disMajiang != null) {
            if ((action == SyMjDisAction.action_minggang && sameCount == 3)
                    || action == SyMjDisAction.action_peng || action == SyMjDisAction.action_chi) {
                if (action == SyMjDisAction.action_chi) {
                    majiangs.add(1, disMajiang);// 吃的牌放第二位
                } else {
                    majiangs.add(disMajiang);
                }
                builder.setFromSeat(disCardSeat);
                seatMap.get(disCardSeat).removeOutPais(nowDisCardIds, action);
            }
        }
        chiPengGang(builder, player, majiangs, action, hasQGangHu, sameCount);
    }

    /**
     * 抢杠胡
     *
     * @param player
     * @param majiangs
     * @param action
     * @return
     */
    private boolean checkQGangHu(SyMjPlayer player, List<SyMj> majiangs, int action, int sameCount) {
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        Map<Integer, List<Integer>> huListMap = new HashMap<>();
        for (SyMjPlayer seatPlayer : seatMap.values()) {
            if (seatPlayer.getUserId() == player.getUserId()) {
                continue;
            }
            if (action == SyMjDisAction.action_angang) {
                List<SyMj> copy = new ArrayList<>(seatPlayer.getHandMajiang());
                copy.add(majiangs.get(0));
                if (SyMjTool.isShiSanYaoHu(copy)) {
                    List<Integer> hu = new ArrayList<>();
                    int[] arr = new int[SyMjConstants.ACTION_INDEX_LENGTH];
                    arr[SyMjConstants.ACTION_INDEX_HU] = 1;
                    for (int val : arr) {
                        hu.add(val);
                    }
                    addActionSeat(seatPlayer.getSeat(), hu);
                    huListMap.put(seatPlayer.getSeat(), hu);
                }
            } else {
                List<Integer> hu = seatPlayer.checkDisMajiang(majiangs.get(0), this.canGangHu() || dianGangKeHu == 1);
                if (!hu.isEmpty() && hu.get(SyMjConstants.ACTION_INDEX_HU) == 1) {
                    hu.set(SyMjConstants.ACTION_INDEX_CHI, 0);
                    addActionSeat(seatPlayer.getSeat(), hu);
                    huListMap.put(seatPlayer.getSeat(), hu);
                }
            }
        }
        // 可以胡牌
        if (!huListMap.isEmpty()) {
            setMoGang(majiangs.get(0), new ArrayList<>(huListMap.keySet()), player, sameCount);
            buildPlayRes(builder, player, action, majiangs);
            for (Entry<Integer, List<Integer>> entry : huListMap.entrySet()) {
                PlayMajiangRes.Builder copy = builder.clone();
                SyMjPlayer seatPlayer = seatMap.get(entry.getKey());
                copy.addAllSelfAct(entry.getValue());
//				seatPlayer.writeSocket(copy.build());
            }
            return true;
        }
        return false;
    }

    private void chiPengGang(PlayMajiangRes.Builder builder, SyMjPlayer player, List<SyMj> majiangs, int action, boolean hasQGangHu, int sameCount) {
        List<Integer> actionList = actionSeatMap.get(player.getSeat());
        if (action == SyMjDisAction.action_peng && actionList.get(SyMjConstants.ACTION_INDEX_MINGGANG) == 1) {
            // 可以碰也可以杠
            player.addPassGangVal(majiangs.get(0).getVal());
        }

        player.addOutPais(majiangs, action, disCardSeat);
        buildPlayRes(builder, player, action, majiangs);
        List<Integer> actList = removeActionSeat(player.getSeat());
        if (!hasQGangHu) {
            clearActionSeatMap();
        }
        if (action == SyMjDisAction.action_chi || action == SyMjDisAction.action_peng) {
            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + SyMjHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
        }
        // 不是普通出牌
        setNowDisCardSeat(player.getSeat());
        for (SyMjPlayer seatPlayer : seatMap.values()) {
            // 推送消息
            PlayMajiangRes.Builder copy = builder.clone();
            if (actionSeatMap.containsKey(seatPlayer.getSeat())) {
                copy.addAllSelfAct(actionSeatMap.get(seatPlayer.getSeat()));
            }
            seatPlayer.writeSocket(copy.build());
        }
        if (action == SyMjDisAction.action_chi || action == SyMjDisAction.action_peng) {
            sendTingInfo(player);
        }
        for (Player roomPlayer : roomPlayerMap.values()) {
            PlayMajiangRes.Builder copy = builder.clone();
            roomPlayer.writeSocket(copy.build());
        }
        if (!hasQGangHu) {
            calcPoint(player, action, sameCount, majiangs);
        }
        if (!hasQGangHu && (action == SyMjDisAction.action_minggang || action == SyMjDisAction.action_angang)) {
            // 明杠和暗杠摸牌
            moMajiang(player, true);
        }
        robotDealAction();
        logAction(player, action, majiangs, actList);
    }

    /**
     * 普通出牌
     *
     * @param player
     * @param majiangs
     * @param action
     */
    private void chuPai(SyMjPlayer player, List<SyMj> majiangs, int action) {
        if (state != table_state.play) {
            return;
        }
        if (majiangs.size() != 1) {
            return;
        }
        if (!tempActionMap.isEmpty()) {
            LogUtil.e(player.getName() + "出牌清理临时操作！");
            clearTempAction();
        }
        if (!player.isAlreadyMoMajiang()) {
            // 还没有摸牌
            return;
        }
        if (!actionSeatMap.isEmpty()) {//出牌自动过掉手上操作
            guo(player, null, SyMjDisAction.action_pass);
        }
        if (!actionSeatMap.isEmpty()) {
            return;
        }
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        buildPlayRes(builder, player, action, majiangs);
        // 普通出牌
        clearActionSeatMap();
        setNowDisCardSeat(calcNextSeat(player.getSeat()));
        recordDisMajiang(majiangs, player);
        player.addOutPais(majiangs, action, player.getSeat());
        logAction(player, action, majiangs, null);
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + SyMjHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
        for (SyMjPlayer seat : seatMap.values()) {
            List<Integer> list = new ArrayList<>();
            if (seat.getUserId() != player.getUserId()) {
                list = seat.checkDisMajiang(majiangs.get(0), (this.canDianPao() || isGangPao()));
                if (list.contains(1)) {
                    addActionSeat(seat.getSeat(), list);
                    seat.setLastCheckTime(System.currentTimeMillis());
                    logChuPaiActList(seat, majiangs.get(0), list);
                }
            }
        }
        sendDisMajiangAction(builder, player);

        // 给下一家发牌
        checkMo();
    }

    public List<Integer> getHuSeatByActionMap() {
        List<Integer> huList = new ArrayList<>();
        for (int seat : actionSeatMap.keySet()) {
            List<Integer> actionList = actionSeatMap.get(seat);
            if (actionList.get(SyMjConstants.ACTION_INDEX_HU) == 1 || actionList.get(SyMjConstants.ACTION_INDEX_ZIMO) == 1) {
                // 胡
                huList.add(seat);
            }

        }
        return huList;
    }

    private void sendDisMajiangAction(PlayMajiangRes.Builder builder, SyMjPlayer player) {
        for (SyMjPlayer seatPlayer : seatMap.values()) {
            PlayMajiangRes.Builder copy = builder.clone();
            List<Integer> actionList;
            // 只推送给胡牌的人改成了推送给所有人但是必须等胡牌的人先答复
            if (actionSeatMap.containsKey(seatPlayer.getSeat())) {
                actionList = actionSeatMap.get(seatPlayer.getSeat());
            } else {
                actionList = new ArrayList<>();
            }
            copy.addAllSelfAct(actionList);
            if (seatPlayer.getUserId() == player.getUserId()) {
                copy.addExt(SyMjTool.isTing(seatPlayer) ? "1" : "0");
            }
            seatPlayer.writeSocket(copy.build());
        }
        for (Player roomPlayer : roomPlayerMap.values()) {
            PlayMajiangRes.Builder copy = builder.clone();
            roomPlayer.writeSocket(copy.build());
        }
    }

    public synchronized void playCommand(SyMjPlayer player, List<SyMj> majiangs, int action) {
        playCommand(player, majiangs, null, action);
    }

    /**
     * 出牌
     *
     * @param player
     * @param majiangs
     * @param action
     */
    public synchronized void playCommand(SyMjPlayer player, List<SyMj> majiangs, List<Integer> hucards, int action) {
        if (state != table_state.play) {
            return;
        }

        // 被人抢杠胡 自己杠的时候被人抢杠胡了 不能做其他操作
        if (!moGangHuList.isEmpty()) {
            if (!moGangHuList.contains(player.getSeat())) {
                return;
            }
        }

        //胡牌
        if (SyMjDisAction.action_hu == action) {
            hu(player, majiangs, action);
            return;
        }
        // 手上没有要出的麻将
        if (action != SyMjDisAction.action_minggang) {
            if (!player.getHandMajiang().containsAll(majiangs)) {
                return;
            }
        }
        changeDisCardRound(1);
        if (action == SyMjDisAction.action_pass) {
            guo(player, majiangs, action);
        } else if (action == SyMjDisAction.action_chupai) {
            chuPai(player, majiangs, action);
        } else {
            chiPengGang(player, majiangs, action);
        }
        // 记录最后一次动作的时间
        setLastActionTime(TimeUtil.currentTimeMillis());
    }

    private void passMoHu(SyMjPlayer player, List<SyMj> majiangs, int action) {
        if (!moGangHuList.contains(player.getSeat())) {
            return;
        }

        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        buildPlayRes(builder, player, action, majiangs);
        builder.setSeat(nowDisCardSeat);
        removeActionSeat(player.getSeat());
        player.writeSocket(builder.build());
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + SyMjHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
        if (isCalcOver()) {
            calcOver();
            return;
        }
        player.setPassMajiangVal(nowDisCardIds.get(0).getVal());

        if (moGangHuList.isEmpty()) {
            SyMjPlayer moGangPlayer = seatMap.get(getNowDisCardSeat());
            majiangs = new ArrayList<>();
            majiangs.add(moGang);
            if (moGangPlayer.getaGang().contains(moGang)) {
                calcPoint(moGangPlayer, SyMjDisAction.action_angang, 4, majiangs);
            } else {
                calcPoint(moGangPlayer, SyMjDisAction.action_minggang, moGangSameCount > 0 ? moGangSameCount : 1, majiangs);
            }
            moMajiang(moGangPlayer, true);
        }

    }

    /**
     * pass
     *
     * @param player
     * @param majiangs
     * @param action
     */
    private void guo(SyMjPlayer player, List<SyMj> majiangs, int action) {
        if (state != table_state.play) {
            return;
        }
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return;
        }
        if (!moGangHuList.isEmpty()) {
            // 有摸杠胡的优先处理
            passMoHu(player, majiangs, action);
            return;
        }
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        buildPlayRes(builder, player, action, majiangs);
        builder.setSeat(nowDisCardSeat);
        List<Integer> removeActionList = removeActionSeat(player.getSeat());
        player.writeSocket(builder.build());
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + SyMjHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
        if (isCalcOver()) {
            calcOver();
            return;
        }
        if (removeActionList.get(SyMjConstants.ACTION_INDEX_HU) == 1 && disCardSeat != player.getSeat() && nowDisCardIds.size() == 1) {
            // 漏炮
            player.setPassMajiangVal(nowDisCardIds.get(0).getVal());
        }
        logAction(player, action, majiangs, removeActionList);
        if (!actionSeatMap.isEmpty()) {
            SyMjPlayer disMajiangPlayer = seatMap.get(disCardSeat);
            PlayMajiangRes.Builder disBuilder = PlayMajiangRes.newBuilder();
            buildPlayRes(disBuilder, disMajiangPlayer, 0, null);
            for (int seat : actionSeatMap.keySet()) {
                List<Integer> actionList = actionSeatMap.get(seat);
                PlayMajiangRes.Builder copy = disBuilder.clone();
                copy.addAllSelfAct(new ArrayList<>());
                if (actionList != null && !tempActionMap.containsKey(seat) && !huConfirmList.contains(seat)) {
                    copy.addAllSelfAct(actionList);
                    SyMjPlayer seatPlayer = seatMap.get(seat);
                    seatPlayer.writeSocket(copy.build());
                }
            }
        }
        if (player.isAlreadyMoMajiang()) {
            sendTingInfo(player);
        }
        refreshTempAction(player);// 先过 后执行临时可做操作里面优先级最高的玩家操作
        checkMo();
    }

    private void calcPoint(SyMjPlayer player, int action, int sameCount, List<SyMj> majiangs) {
        int lostPoint = 0, point = 0;
        int winPoint = 0;
        int[] seatPointArr = new int[getMaxPlayerCount() + 1];
        if (action == SyMjDisAction.action_peng) {
            return;

        } else if (action == SyMjDisAction.action_angang) {
            // 暗杠相当于自摸每人出2分
            point = 2;
            setFangGangSeat(player.getSeat());
        } else if (action == SyMjDisAction.action_minggang) {
            if (sameCount == 1) {
                point = 1;
                setFangGangSeat(player.getSeat());
            } else if (sameCount == 3) {
                if (dianGangSanJiaFu == 1) {
                    //点杠三家付
                    point = 1;
                    setFangGangSeat(disCardSeat);
                } else {
                    SyMjPlayer disPlayer = seatMap.get(disCardSeat);
                    lostPoint = getMaxPlayerCount() - 1;
                    if (disPlayer.getChui() == 1) {
                        lostPoint *= 2;
                    }
                    if (player.getChui() == 1) {
                        lostPoint *= 2;
                    }
                    disPlayer.changeLostPoint(-lostPoint);
                    seatPointArr[disPlayer.getSeat()] = -lostPoint;
                    player.changeLostPoint(lostPoint);
                    seatPointArr[player.getSeat()] = lostPoint;
                    setFangGangSeat(disPlayer.getSeat());

                }
            }
        }
        if (point != 0) {
            for (SyMjPlayer seat : seatMap.values()) {
                if (seat.getUserId() != player.getUserId()) {
                    lostPoint = point;
                    if (seat.getChui() == 1) {
                        lostPoint *= 2;
                    }
                    if (player.getChui() == 1) {
                        lostPoint *= 2;
                    }
                    winPoint += lostPoint;
                    seat.changeLostPoint(-lostPoint);
                    seatPointArr[seat.getSeat()] = -lostPoint;
                }
            }
            player.changeLostPoint(winPoint);
            seatPointArr[player.getSeat()] = winPoint;
        }

        String seatPointStr = "";
        for (int i = 1; i <= getMaxPlayerCount(); i++) {
            seatPointStr += seatPointArr[i] + ",";
        }
        seatPointStr = seatPointStr.substring(0, seatPointStr.length() - 1);
        ComMsg.ComRes.Builder res = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_gangFen, seatPointStr);
        GeneratedMessage msg = res.build();
        broadMsgToAll(msg);
        if (action != SyMjDisAction.action_chi) {
           // addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + SyMjHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog() + "_" + seatPointStr);
        }
    }

    private void recordDisMajiang(List<SyMj> majiangs, SyMjPlayer player) {
        setNowDisCardIds(majiangs);
        // changeDisCardRound(1);
        setDisCardSeat(player.getSeat());
    }

    public List<SyMj> getNowDisCardIds() {
        return nowDisCardIds;
    }

    public void setNowDisCardIds(List<SyMj> nowDisCardIds) {
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
                moMajiang(seatMap.get(nowDisCardSeat));

            }
            robotDealAction();

        } else {
            for (int seat : actionSeatMap.keySet()) {
                SyMjPlayer player = seatMap.get(seat);
                if (player != null && player.isRobot()) {
                    // 如果是机器人可以直接决定
                    List<Integer> actionList = actionSeatMap.get(seat);
                    if (actionList == null) {
                        continue;
                    }
                    List<SyMj> list = new ArrayList<>();
                    if (!nowDisCardIds.isEmpty()) {
                        list = SyMjQipaiTool.getVal(player.getHandMajiang(), nowDisCardIds.get(0).getVal());
                    }
                    if (actionList.get(SyMjConstants.ACTION_INDEX_HU) == 1 || actionList.get(SyMjConstants.ACTION_INDEX_ZIMO) == 1) {
                        // 胡
                        playCommand(player, new ArrayList<SyMj>(), SyMjDisAction.action_hu);

                    } else if (actionList.get(SyMjConstants.ACTION_INDEX_ANGANG) == 1) {
                        playCommand(player, list, SyMjDisAction.action_angang);

                    } else if (actionList.get(SyMjConstants.ACTION_INDEX_MINGGANG) == 1) {
                        playCommand(player, list, SyMjDisAction.action_minggang);

                    } else if (actionList.get(SyMjConstants.ACTION_INDEX_PENG) == 1) {
                        playCommand(player, list, SyMjDisAction.action_peng);
                    } else {
                        playCommand(player, new ArrayList<>(), SyMjDisAction.action_pass);
                    }
                }
                // else {
                // // 是玩家需要发送消息
                // player.writeSocket(builder.build());
                // }

            }

        }
    }

    @Override
    protected void robotDealAction() {
        if (isTest()) {
            int nextseat = getNextDisCardSeat();
            SyMjPlayer next = seatMap.get(nextseat);
            if (next != null && next.isRobot()) {
                List<Integer> actionList = actionSeatMap.get(next.getSeat());
                if (actionList != null) {
                    List<SyMj> list = null;
                    if (actionList.get(0) == 1) {
                        // 胡
                        playCommand(next, new ArrayList<SyMj>(), SyMjDisAction.action_hu);
                    } else if (actionList.get(3) == 1) {
                        // 机器人暗杠
                        Map<Integer, Integer> handMap = SyMjHelper.toMajiangValMap(next.getHandMajiang());
                        for (Entry<Integer, Integer> entry : handMap.entrySet()) {
                            if (entry.getValue() == 4) {
                                // 可以暗杠
                                list = SyMjHelper.getMajiangList(next.getHandMajiang(), entry.getKey());
                            }
                        }
                        playCommand(next, list, SyMjDisAction.action_angang);

                    } else if (actionList.get(2) == 1) {
                        Map<Integer, Integer> pengMap = SyMjHelper.toMajiangValMap(next.getPeng());
                        for (SyMj handMajiang : next.getHandMajiang()) {
                            if (pengMap.containsKey(handMajiang.getVal())) {
                                // 有碰过
                                list = new ArrayList<>();
                                list.add(handMajiang);
                                playCommand(next, list, SyMjDisAction.action_minggang);
                                break;
                            }
                        }

                    } else if (actionList.get(1) == 1) {
                        playCommand(next, list, SyMjDisAction.action_peng);
                    }
                } else {
                    List<Integer> handMajiangs = new ArrayList<>(next.getHandPais());
                    SyMjQipaiTool.dropHongzhongVal(handMajiangs);
                    int maJiangId = SyMjRobotAI.getInstance().outPaiHandle(0, handMajiangs, new ArrayList<Integer>());
                    List<SyMj> majiangList = SyMjHelper.toMajiang(Arrays.asList(maJiangId));
                    if (next.isRobot()) {
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    playCommand(next, majiangList, 0);
                }

            }
        }
    }

    @Override
    protected void deal() {
        if (lastWinSeat == 0) {
            int masterseat = playerMap.get(masterId).getSeat();
            setLastWinSeat(masterseat);
        }
        setDisCardSeat(lastWinSeat);
        setNowDisCardSeat(lastWinSeat);
        setMoMajiangSeat(lastWinSeat);

        List<Integer> copy = SyMjConstants.getMajiangList(getDaiFeng());
        addPlayLog(copy.size() + "");
        List<List<SyMj>> list = null;
        if (zp != null) {
            list = SyMjTool.fapai(copy, getMaxPlayerCount(), zp);
        } else {
            list = SyMjTool.fapai(copy, getMaxPlayerCount());
        }
        int i = 1;
        for (SyMjPlayer player : playerMap.values()) {
            player.changeState(player_state.play);
            if (player.getSeat() == lastWinSeat) {
                player.dealHandPais(list.get(0));
                continue;
            }
            player.dealHandPais(list.get(i));
            i++;
        }
        // 桌上剩余的牌
        setLeftMajiangs(list.get(getMaxPlayerCount()));
        setTableStatus(0);
    }

    @Override
    public void startNext() {
        // 直接胡牌
        // autoZiMoHu();
    }

    /**
     * 初始化桌子上剩余牌
     *
     * @param leftMajiangs
     */
    public void setLeftMajiangs(List<SyMj> leftMajiangs) {
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
    public SyMj getLeftMajiang() {
        if (this.leftMajiangs.size() > 0) {
            SyMj majiang = this.leftMajiangs.remove(0);
            dbParamMap.put("leftPais", JSON_TAG);
            return majiang;
        }
        return null;
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
        res.addExt(payType);                //0
        res.addExt(birdNum);                //1
        res.addExt(daiFeng);                //2
        res.addExt(keChi);                    //3
        res.addExt(keChui);                    //4
        res.addExt(getTableStatus());        //5
        res.addExt(isAutoPlay);                //6
        res.addExt(qiangGangHu);            //7
        res.addExt(qiangGangHuBaoSanJia);    //8
        res.addExt(shangZhongXiaNiao);        //9
        res.addExt(dianGangKeHu);            //10
        res.addExt(dianGangSanJiaFu);        //11
        res.addExt(gangKaiHuBaoSanJia);        //12
        res.addExt(gangHouPaoSanJiaFu);        //13
        res.addExt(qiangGangHuSuanZiMo);    //14
        res.addExt(diFen);                    //15

        res.addStrExt(StringUtil.implode(moTailPai, ","));      //0

        res.setMasterId(getMasterId() + "");
        if (leftMajiangs != null) {
            res.setRemain(leftMajiangs.size());
        } else {
            res.setRemain(0);
        }
        res.setDealDice(dealDice);
        List<PlayerInTableRes> players = new ArrayList<>();
        for (SyMjPlayer player : playerMap.values()) {
            PlayerInTableRes.Builder playerRes = player.buildPlayInTableInfo(isrecover);
            if (player.getUserId() == userId) {
                playerRes.addAllHandCardIds(player.getHandPais());
                if (!player.getHandMajiang().isEmpty() && player.getHandMajiang().size() % 3 == 1) {
                    if (player.isOkPlayer() && SyMjTool.isTing(player)) {
                        playerRes.setUserSate(3);
                    }
                }
            }
            if (player.getSeat() == disCardSeat && nowDisCardIds != null) {
                playerRes.addAllOutCardIds(SyMjHelper.toMajiangIds(nowDisCardIds));
            }
            playerRes.addRecover(player.getIsEntryTable());
            playerRes.addRecover(player.getSeat() == lastWinSeat ? 1 : 0);
            if (actionSeatMap.containsKey(player.getSeat())) {
                if (!tempActionMap.containsKey(player.getSeat()) && !huConfirmList.contains(player.getSeat())) {
                    //if (!tempActionMap.containsKey(player.getSeat()) || huConfirmList.contains(player.getSeat())) {// 如果已做临时操作 则不发送前端可做的操作 或者已经操作胡了
                    playerRes.addAllRecover(actionSeatMap.get(player.getSeat()));
                }
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
        res.setRenshu(getMaxPlayerCount());
        res.setLastWinSeat(getLastWinSeat());
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

    public void setMaxPlayerCount(int maxPlayerCount) {
        this.maxPlayerCount = maxPlayerCount;
    }

    @Override
    public Map<Long, Player> getPlayerMap() {
        Object o = playerMap;
        return (Map<Long, Player>) o;
    }

    @Override
    protected void initNext1() {
        clearHuList();
        clearActionSeatMap();
        setLeftMajiangs(null);
        setNowDisCardIds(null);
        clearMoGang();
        setDealDice(0);
        setFangGangSeat(0);
        setGangKaiSeat(0);
        setGangPao(false);
        clearMoTailPai();
        readyTime =0;
    }

    public List<Integer> removeActionSeat(int seat) {
        List<Integer> actionList = actionSeatMap.remove(seat);
        if (moGangHuList.contains(seat)) {
            removeMoGang(seat);
        }
        if (seat == getGangKaiSeat()) {
            setGangKaiSeat(0);
        }
        saveActionSeatMap();
        return actionList;
    }

    public void addActionSeat(int seat, List<Integer> actionlist) {
        actionSeatMap.put(seat, actionlist);
        SyMjPlayer player = seatMap.get(seat);
        addPlayLog(disCardRound + "_" + seat + "_" + SyMjDisAction.action_hasAction + "_" + StringUtil.implode(actionlist) + player.getExtraPlayLog());
        saveActionSeatMap();
    }

    public void clearActionSeatMap() {
        if (!actionSeatMap.isEmpty()) {
            actionSeatMap.clear();
            saveActionSeatMap();
        }
    }

    private void clearTempAction() {
        if (!tempActionMap.isEmpty()) {
            tempActionMap.clear();
            changeExtend();
        }
    }

    public void clearHuList() {
        huConfirmList.clear();
        changeExtend();
    }

    public void addHuList(int seat) {
        if (!huConfirmList.contains(seat)) {
            huConfirmList.add(seat);

        }
        changeExtend();
    }

    public void saveActionSeatMap() {
        dbParamMap.put("nowAction", JSON_TAG);
    }

    @Override
    protected void initNowAction(String nowAction) {
        JsonWrapper wrapper = new JsonWrapper(nowAction);
        for (int i = 1; i <= 4; i++) {
            String val = wrapper.getString(i);
            if (!StringUtils.isBlank(val)) {
                actionSeatMap.put(i, StringUtil.explodeToIntList(val));

            }
        }
    }

    @Override
    protected void loadFromDB1(TableInf info) {
        if (!StringUtils.isBlank(info.getNowDisCardIds())) {
            nowDisCardIds = SyMjHelper.toMajiang(StringUtil.explodeToIntList(info.getNowDisCardIds()));
        }

        if (!StringUtils.isBlank(info.getLeftPais())) {
            try {
                leftMajiangs = SyMjHelper.toMajiang(StringUtil.explodeToIntList(info.getLeftPais()));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    @Override
    public void initExtend0(JsonWrapper wrapper) {
        for (SyMjPlayer player : seatMap.values()) {
            player.initExtend(wrapper.getString(player.getSeat()));
        }
        String huListstr = wrapper.getString(5);
        if (!StringUtils.isBlank(huListstr)) {
            huConfirmList = StringUtil.explodeToIntList(huListstr);
        }
        birdNum = wrapper.getInt(6, 0);
        moMajiangSeat = wrapper.getInt(7, 0);
        int moGangMajiangId = wrapper.getInt(8, 0);
        if (moGangMajiangId != 0) {
            moGang = SyMj.getMajang(moGangMajiangId);
        }
        String moGangHu = wrapper.getString(9);
        if (!StringUtils.isBlank(moGangHu)) {
            moGangHuList = StringUtil.explodeToIntList(moGangHu);
        }
        daiFeng = wrapper.getInt(10, 0);
        keChi = wrapper.getInt(11, 0);
        keChui = wrapper.getInt(12, 0);
        tempActionMap = loadTempActionMap(wrapper.getString("tempActions"));
        maxPlayerCount = wrapper.getInt(13, 4);
        dealDice = wrapper.getInt(14, 0);
        tableStatus = wrapper.getInt(15, 0);
        fangGangSeat = wrapper.getInt(16, 0);
        gangKaiSeat = wrapper.getInt(17, 0);
        setGangPao(wrapper.getInt(18, 0) == 1);

        qiangGangHu = wrapper.getInt(19, 1);
        qiangGangHuBaoSanJia = wrapper.getInt(20, 0);
        shangZhongXiaNiao = wrapper.getInt(21, 0);
        dianGangKeHu = wrapper.getInt(22, 0);
        dianGangSanJiaFu = wrapper.getInt(23, 0);
        gangKaiHuBaoSanJia = wrapper.getInt(24, 0);
        gangHouPaoSanJiaFu = wrapper.getInt(25, 1);
        qiangGangHuSuanZiMo = wrapper.getInt(26, 0);
        moGangSeat = wrapper.getInt(27, 0);
        moGangSameCount = wrapper.getInt(28, 0);
        isAutoPlay = wrapper.getInt(29, 0);
        
        
    
        
        String moTailPaiStr = wrapper.getString(30);
        if (!StringUtils.isBlank(moTailPaiStr)) {
            moTailPai = StringUtil.explodeToIntList(moTailPaiStr);
        }
        diFen = wrapper.getInt(31, 1);
        
        
        
        jiaBei = wrapper.getInt(32, 0);
        jiaBeiFen = wrapper.getInt(33, 0);
        jiaBeiShu = wrapper.getInt(34, 0);
        autoPlayGlob = wrapper.getInt(35, 0);
    }

    private Map<Integer, SyMjTempAction> loadTempActionMap(String json) {
        Map<Integer, SyMjTempAction> map = new ConcurrentHashMap<>();
        if (json == null || json.isEmpty())
            return map;
        JSONArray jsonArray = JSONArray.parseArray(json);
        for (Object val : jsonArray) {
            String str = val.toString();
            SyMjTempAction tempAction = new SyMjTempAction();
            tempAction.initData(str);
            map.put(tempAction.getSeat(), tempAction);
        }
        return map;
    }

    /**
     * 检查优先度 如果同时出现一个事件，按出牌座位顺序优先
     */
    private boolean checkAction(SyMjPlayer player, List<SyMj> cardList, List<Integer> hucards, int action) {
        boolean canAction = checkCanAction(player, action);// 是否优先级最高 能执行操作
        if (!canAction) {// 不能操作时  存入临时操作
            int seat = player.getSeat();
            tempActionMap.put(seat, new SyMjTempAction(seat, action, cardList, hucards));
            // 玩家都已选择自己的临时操作后  选取优先级最高
            if (tempActionMap.size() == actionSeatMap.size()) {
                int maxAction = Integer.MAX_VALUE;
                int maxSeat = 0;
                Map<Integer, Integer> prioritySeats = new HashMap<>();
                int maxActionSize = 0;
                for (SyMjTempAction temp : tempActionMap.values()) {
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
                SyMjPlayer tempPlayer = seatMap.get(maxSeat);
                List<SyMj> tempCardList = tempActionMap.get(maxSeat).getCardList();
                List<Integer> tempHuCards = tempActionMap.get(maxSeat).getHucards();
                for (int removeSeat : prioritySeats.keySet()) {
                    if (removeSeat != maxSeat) {
                        removeActionSeat(removeSeat);
                    }
                }
                clearTempAction();
                playCommand(tempPlayer, tempCardList, tempHuCards, maxAction);// 系统选取优先级最高操作
            } else {
                if (isCalcOver()) {
                    calcOver();
                }
            }
        } else {// 能操作 清理所有临时操作
            clearTempAction();
        }
        return canAction;
    }

    /**
     * 执行可做操作里面优先级最高的玩家操作
     *
     * @param player
     */
    private void refreshTempAction(SyMjPlayer player) {
        tempActionMap.remove(player.getSeat());
        Map<Integer, Integer> prioritySeats = new HashMap<>();//各位置优先操作
        for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
            int seat = entry.getKey();
            List<Integer> actionList = entry.getValue();
            List<Integer> list = SyMjDisAction.parseToDisActionList(actionList);
            int priorityAction = SyMjDisAction.getMaxPriorityAction(list);
            prioritySeats.put(seat, priorityAction);
        }
        int maxPriorityAction = Integer.MAX_VALUE;
        int maxPrioritySeat = 0;
        boolean isSame = true;//是否有相同操作
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
        Iterator<SyMjTempAction> iterator = tempActionMap.values().iterator();
        while (iterator.hasNext()) {
            SyMjTempAction tempAction = iterator.next();
            if (tempAction.getSeat() == maxPrioritySeat) {
                int action = tempAction.getAction();
                List<SyMj> tempCardList = tempAction.getCardList();
                List<Integer> tempHuCards = tempAction.getHucards();
                SyMjPlayer tempPlayer = seatMap.get(tempAction.getSeat());
                iterator.remove();
                playCommand(tempPlayer, tempCardList, tempHuCards, action);// 系统选取优先级最高操作
                break;
            }
        }
        changeExtend();
    }

    /**
     * 检查优先度，胡杠补碰吃 如果同时出现一个事件，按出牌座位顺序优先
     *
     * @param player
     * @param action
     * @return
     */
    public boolean checkCanAction(SyMjPlayer player, int action) {
        // 优先度为胡杠补碰吃
        List<Integer> stopActionList = SyMjDisAction.findPriorityAction(action);
        for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
            if (player.getSeat() != entry.getKey()) {
                // 别人
                boolean can = SyMjDisAction.canDisMajiang(stopActionList, entry.getValue());
                if (!can) {
                    return false;
                }
                List<Integer> disActionList = SyMjDisAction.parseToDisActionList(entry.getValue());
                if (disActionList.contains(action)) {
                    // 同时拥有同一个事件 根据座位号来判断
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
     * 是否能吃
     *
     * @param player
     * @param handMajiang
     * @param majiangs
     * @param disMajiang
     * @return
     */
    private boolean canChi(SyMjPlayer player, List<SyMj> handMajiang, List<SyMj> majiangs, SyMj disMajiang) {
        if (player.isAlreadyMoMajiang()) {
            return false;
        }
        for (int seat : actionSeatMap.keySet()) {
            if (seat != player.getSeat()) {
                List<Integer> actionList = actionSeatMap.get(seat);
                if (actionList.get(1) == 1 || actionList.get(2) == 1 || actionList.get(3) == 1 || actionList.get(4) == 1) {// 胡
                    return false;// 碰杠优先
                }
            }
        }
        if (disMajiang == null) {
            return false;
        }
        if (!handMajiang.containsAll(majiangs)) {
            return false;
        }
        List<SyMj> chi = SyMjTool.checkChi(majiangs, disMajiang);
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
    private boolean canPeng(SyMjPlayer player, List<SyMj> majiangs, int sameCount, SyMj disMajiang) {
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
    private boolean canAnGang(SyMjPlayer player, List<SyMj> majiangs, int sameCount) {
        if (sameCount != 4) {
            return false;
        }
        if (player.getSeat() != getNextDisCardSeat()) {
            return false;
        }
        
        return true;
    }

    /**
     * 是否能暗杠
     *
     * @param player
     * @param majiangs
     * @param sameCount
     * @return
     */
    private boolean canMingGang(SyMjPlayer player, List<SyMj> majiangs, int sameCount, SyMj disMajiang) {
        List<SyMj> handMajiangs = player.getHandMajiang();
        List<Integer> pengList = SyMjHelper.toMajiangVals(player.getPeng());

        
        if (majiangs.size() == 1) {
            if (player.getSeat() != getNextDisCardSeat()) {
                return false;
            }
            if (handMajiangs.containsAll(majiangs) && pengList.contains(majiangs.get(0).getVal())) {
                return true;
            }
        } else if (majiangs.size() == 3) {
            if (sameCount != 3) {
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
        return actionSeatMap;
    }

    public int getBirdNum() {
        return birdNum;
    }

    public void setBirdNum(int birdNum) {
        this.birdNum = birdNum;
    }

    public void setMoMajiangSeat(int moMajiangSeat) {
        this.moMajiangSeat = moMajiangSeat;
        changeExtend();
    }

    /**
     * 摸杠别人可以胡
     *
     * @param moGang
     * @param moGangHuList
     */
    public void setMoGang(SyMj moGang, List<Integer> moGangHuList, SyMjPlayer player, int sameCount) {
        this.moGang = moGang;
        this.moGangHuList = moGangHuList;
        this.moGangSeat = player.getSeat();
        this.moGangSameCount = sameCount;
        changeExtend();
    }

    /**
     * 清除摸刚胡
     */
    public void clearMoGang() {
        this.moGang = null;
        this.moGangHuList.clear();
        this.moGangSeat = 0;
        this.moGangSameCount = 0;
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

    public int getMoMajiangSeat() {
        return moMajiangSeat;
    }

    @Override
    protected String buildNowAction() {
        JsonWrapper wrapper = new JsonWrapper("");
        for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
            wrapper.putString(entry.getKey(), StringUtil.implode(entry.getValue(), ","));
        }
        return wrapper.toString();
    }

    @Override
    public void setConfig(int index, int val) {

    }

    /**
     * 能抢杠胡
     *
     * @return
     */
    public boolean canGangHu() {
        return qiangGangHu == 1;
    }

    // 能否点炮
    public boolean canDianPao() {
        return false;
    }

    /**
     * @param over
     * @param selfMo
     * @param winList
     * @param prickBirdMajiangIds 鸟ID
     * @param seatBirds           鸟位置
     * @param seatBridMap         鸟分
     * @param isBreak
     * @return
     */
    public ClosingMjInfoRes.Builder sendAccountsMsg(boolean over, boolean selfMo, List<Integer> winList, int[] prickBirdMajiangIds, int[] seatBirds, Map<Integer, Integer> seatBridMap, int catchBirdSeat, boolean isBreak) {
        if (isBreak) {
            for (SyMjPlayer seat : seatMap.values()) {
                seat.changePoint(seat.getLostPoint());
            }
        }
        
        //大结算计算加倍分
        if(over && jiaBei == 1){
            int jiaBeiPoint = 0;
            int loserCount = 0;
            for (SyMjPlayer player : seatMap.values()) {
                if (player.getTotalPoint() > 0 && player.getTotalPoint() < jiaBeiFen) {
                    jiaBeiPoint += player.getTotalPoint() * (jiaBeiShu - 1);
                    player.setTotalPoint(player.getTotalPoint() * jiaBeiShu);
                } else if (player.getTotalPoint() < 0) {
                    loserCount++;
                }
            }
            if (jiaBeiPoint > 0) {
                for (SyMjPlayer player : seatMap.values()) {
                    if (player.getTotalPoint() < 0) {
                        player.setTotalPoint(player.getTotalPoint() - (jiaBeiPoint / loserCount));
                    }
                }
            }

        }
        
        List<ClosingMjPlayerInfoRes> list = new ArrayList<>();
        List<ClosingMjPlayerInfoRes.Builder> builderList = new ArrayList<>();
        for (SyMjPlayer player : seatMap.values()) {
            ClosingMjPlayerInfoRes.Builder build = null;
            if (over) {
                build = player.bulidTotalClosingPlayerInfoRes();
            } else {
                build = player.bulidOneClosingPlayerInfoRes();
            }
            if (seatBridMap != null && seatBridMap.containsKey(player.getSeat())) {
                build.setBirdPoint(seatBridMap.get(player.getSeat()));
            } else {
                build.setBirdPoint(0);
            }
            if (winList != null && winList.contains(player.getSeat())) {
                if (!selfMo) {
                    // 不是自摸
                    SyMj huMajiang = nowDisCardIds.get(0);
                    if (!build.getHandPaisList().contains(huMajiang.getId())) {
                        build.addHandPais(huMajiang.getId());
                    }
                    build.setIsHu(huMajiang.getId());
                } else {
                    build.setIsHu(player.getLastMoMajiang().getId());
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
            for (SyMjPlayer player : seatMap.values()) {
                if (player.getWinLoseCredit() > dyjCredit) {
                    dyjCredit = player.getWinLoseCredit();
                }
            }
            for (ClosingMjPlayerInfoRes.Builder builder : builderList) {
                SyMjPlayer player = seatMap.get(builder.getSeat());
                calcCommissionCredit(player, dyjCredit);
                builder.setWinLoseCredit(player.getWinLoseCredit());
                builder.setCommissionCredit(player.getCommissionCredit());
            }
        } else if (isGroupTableGoldRoom()) {
            // -----------亲友圈金币场---------------------------------
            for (SyMjPlayer player : seatMap.values()) {
                player.setWinGold(player.getTotalPoint() * gtgDifen);
            }
            calcGroupTableGoldRoomWinLimit();
            for (ClosingMjPlayerInfoRes.Builder builder : builderList) {
                SyMjPlayer player = seatMap.get(builder.getSeat());
                builder.setWinLoseCredit(player.getWinGold());
            }
        }
        for (ClosingMjPlayerInfoRes.Builder builder : builderList) {
            list.add(builder.build());
        }

        ClosingMjInfoRes.Builder res = ClosingMjInfoRes.newBuilder();
        res.addAllClosingPlayers(list);
        res.setIsBreak(isBreak ? 1 : 0);
        res.setWanfa(getWanFa());
        res.addAllExt(buildAccountsExt(over?1:0));
        res.addCreditConfig(creditMode);                         //0
        res.addCreditConfig(creditJoinLimit);                    //1
        res.addCreditConfig(creditDissLimit);                    //2
        res.addCreditConfig(creditDifen);                        //3
        res.addCreditConfig(creditCommission);                   //4
        res.addCreditConfig(creditCommissionMode1);              //5
        res.addCreditConfig(creditCommissionMode2);              //6
        res.addCreditConfig(creditCommissionLimit);              //7
        if (seatBirds != null) {
            res.addAllBirdSeat(DataMapUtil.toList(seatBirds));
        }
        if (prickBirdMajiangIds != null) {
            res.addAllBird(DataMapUtil.toList(prickBirdMajiangIds));
        }
        res.addAllLeftCards(SyMjHelper.toMajiangIds(leftMajiangs));
        res.setCatchBirdSeat(catchBirdSeat);
        for (SyMjPlayer player : seatMap.values()) {
            player.writeSocket(res.build());
        }
        broadMsgRoomPlayer(res.build());
        return res;
    }

    public List<String> buildAccountsExt(int over) {
        List<String> ext = new ArrayList<>();
        if (isGroupRoom()) {
            ext.add(loadGroupId());
        } else {
            ext.add("0");
        }
        ext.add(id + "");                                //1
        ext.add(masterId + "");                            //2
        ext.add(TimeUtil.formatTime(TimeUtil.now()));    //3
        ext.add(playType + "");                            //4
        ext.add(daiFeng + "");                            //5
        ext.add(keChi + "");                            //6
        ext.add(birdNum + "");                            //7
        ext.add(keChui + "");                                //8
        ext.add(isAutoPlay + "");                            //9
        ext.add(qiangGangHu + "");                        //10
        ext.add(qiangGangHuBaoSanJia + "");                //11
        ext.add(shangZhongXiaNiao + "");                    //12
        ext.add(dianGangKeHu + "");                        //13
        ext.add(dianGangSanJiaFu + "");                    //14
        ext.add(gangKaiHuBaoSanJia + "");                //15
        ext.add(gangHouPaoSanJiaFu + "");                    //16
        ext.add(qiangGangHuSuanZiMo + "");                //17
        ext.add(diFen + "");                                //18
        ext.add(isLiuJu() + "");                            //19
        ext.add(over + "");                            //19
        return ext;
    }

    @Override
    public void sendAccountsMsg() {
        ClosingMjInfoRes.Builder builder = sendAccountsMsg(true, false, null, null, null, null, 0, true);
        saveLog(true, 0l, builder.build());
    }

    @Override
    public Class<? extends Player> getPlayerClass() {
        return SyMjPlayer.class;
    }

    @Override
    public int getWanFa() {
        return getPlayType();
    }

//	@Override
//	public boolean isTest() {
//		return SyMjConstants.isTest;
//	}

    @Override
    public void checkReconnect(Player player) {
        if (super.isAllReady() && getKeChui() > 0 && getTableStatus() == SyMjConstants.TABLE_STATUS_CHUI) {
            SyMjPlayer player1 = (SyMjPlayer) player;
            if (player1.getChui() < 0) {
                ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_chui, getTableStatus());
//                player1.writeSocket(com.build());
                return;
            }
            if (player1.getHandPais() != null && player1.getHandPais().size() > 0) {
                sendTingInfo(player1);
            }
        }
        if (state == table_state.play) {
            SyMjPlayer player1 = (SyMjPlayer) player;
            if (player1.getHandPais() != null && player1.getHandPais().size() > 0) {
                sendTingInfo(player1);
            }
        }
    }

    @Override
    public boolean consumeCards() {
        return SharedConstants.consumecards;
    }

    @Override
    public void checkAutoPlay() {
        if (getSendDissTime() > 0) {
            return;
        }

        if (isAutoPlayOff()) {
            // 托管关闭
            for (int seat : seatMap.keySet()) {
                SyMjPlayer player = seatMap.get(seat);
                player.setAutoPlay(false, false);
                player.setCheckAutoPlay(false);
            }
            return;
        }

        if (getTableStatus() == SyMjConstants.TABLE_STATUS_CHUI) {
            for (int seat : seatMap.keySet()) {
                SyMjPlayer player = seatMap.get(seat);
                if (player.getLastCheckTime() > 0 && player.getChui() >= 0) {
					player.setLastCheckTime(player.getLastCheckTime() + 1 * 1000);
					continue;
				}
                if (!player.checkAutoPlay(2, false)) {
                    continue;
                }
                autoChui(player);
            }
        } else if (state == table_state.play) {
            autoPlay();
        } else {
            if (getPlayedBureau() == 0) {
                return;
            }
            
            
            if (getPlayedBureau() == 0) {
				return;
			}
			readyTime++;
			// for (CsMjPlayer player : seatMap.values()) {
			// if (player.checkAutoPlay(1, false)) {
			// autoReady(player);
			// }
			// }
			// 开了托管的房间，xx秒后自动开始下一局
			for (SyMjPlayer player : seatMap.values()) {
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
            
            
            
//            for (SyMjPlayer player : seatMap.values()) {
//                if (player.checkAutoPlay(1, false)) {
//                    autoReady(player);
//                }
//            }
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
                    SyMjPlayer player = seatMap.get(seat);
                    if (player == null) {
                        continue;
                    }
                    if (!player.checkAutoPlay(2, false)) {
                        continue;
                    }
                    playCommand(player, new ArrayList<>(), SyMjDisAction.action_hu);
                }
                return;
            } else {
                int action = 0, seat = 0;
                for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
                    List<Integer> actList = SyMjDisAction.parseToDisActionList(entry.getValue());
                    if (actList == null) {
                        continue;
                    }
                    seat = entry.getKey();
                    action = SyMjDisAction.getAutoMaxPriorityAction(actList);
                    SyMjPlayer player = seatMap.get(seat);
                    if (!player.checkAutoPlay(0, false)) {
                        continue;
                    }
                    boolean chuPai = false;
                    if (player.isAlreadyMoMajiang()) {
                        chuPai = true;
                    }
                    if (action == SyMjDisAction.action_peng) {
                        if (player.isAutoPlaySelf()) {
                            //自己开启托管直接过
                            playCommand(player, new ArrayList<>(), SyMjDisAction.action_pass);
                            if (chuPai) {
                                autoChuPai(player);
                            }
                        } else {
                            if (nowDisCardIds != null && !nowDisCardIds.isEmpty()) {
                                SyMj mj = nowDisCardIds.get(0);
                                List<SyMj> mjList = new ArrayList<>();
                                for (SyMj handMj : player.getHandMajiang()) {
                                    if (handMj.getVal() == mj.getVal()) {
                                        mjList.add(handMj);
                                        if (mjList.size() == 2) {
                                            break;
                                        }
                                    }
                                }
                                playCommand(player, mjList, SyMjDisAction.action_peng);
                            }
                        }
                    } else {
                        playCommand(player, new ArrayList<>(), SyMjDisAction.action_pass);
                        if (chuPai) {
                            autoChuPai(player);
                        }
                    }
                }
            }
        } else {
            SyMjPlayer player = seatMap.get(nowDisCardSeat);
            if (player == null || !player.checkAutoPlay(0, false)) {
                return;
            }
            autoChuPai(player);
        }
    }

    public void autoChuPai(SyMjPlayer player) {

        //SyMjQipaiTool.dropHongzhongVal(handMajiangs);红中麻将要去掉红中
//					int mjId = SyMjRobotAI.getInstance().outPaiHandle(0, handMjIds, new ArrayList<>());
        if (!player.isAlreadyMoMajiang()) {
            return;
        }
        List<Integer> handMjIds = new ArrayList<>(player.getHandPais());
        int mjId = -1;
        if (moMajiangSeat == player.getSeat()) {
            mjId = handMjIds.get(handMjIds.size() - 1);
        } else {
            Collections.sort(handMjIds);
            mjId = handMjIds.get(handMjIds.size() - 1);
        }
        if (mjId != -1) {
            List<SyMj> mjList = SyMjHelper.toMajiang(Arrays.asList(mjId));
            playCommand(player, mjList, SyMjDisAction.action_chupai);
        }
    }

    public void autoChui(SyMjPlayer player) {
        int chuiVal = 0;
        if (getTableStatus() != SyMjConstants.TABLE_STATUS_CHUI) {
            return;
        }
        if (player.getChui() == -1 && (chuiVal == 0 || chuiVal == 1)) {
            player.setChui(chuiVal);
        } else {
            return;
        }
        ComMsg.ComRes.Builder build = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_chui, player.getSeat(), chuiVal);
        broadMsg(build.build());
        broadMsgRoomPlayer(build.build());
        checkDeal(player.getUserId());
    }


    @Override
    public void createTable(Player player, int play, int bureauCount, Object... objects) throws Exception {
    }

    @Override
    public void createTable(Player player, int play, int bureauCount, List<Integer> params, List<String> strParams,
                            Object... objects) throws Exception {
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

        int playerCount = StringUtil.getIntValue(params, 7, 4);// 比赛人数
        payType = StringUtil.getIntValue(params, 2, 1);//支付方式
        daiFeng = StringUtil.getIntValue(params, 3, 0);//带风
        keChi = StringUtil.getIntValue(params, 4, 0);//可吃 1可吃 2清一色可吃
        birdNum = StringUtil.getIntValue(params, 5, 0);//抓几个鸟
        keChui = StringUtil.getIntValue(params, 6, 0);//是否加锤
        isAutoPlay = StringUtil.getIntValue(params, 8, 0);
        qiangGangHu = StringUtil.getIntValue(params, 9, 1);//可抢杠胡
        qiangGangHuBaoSanJia = StringUtil.getIntValue(params, 10, 0);//抢杠胡包三家
        shangZhongXiaNiao = StringUtil.getIntValue(params, 11, 0);//上中下鸟
        dianGangKeHu = StringUtil.getIntValue(params, 12, 0);//点杠可胡
        dianGangSanJiaFu = StringUtil.getIntValue(params, 13, 0);//点杠三家付
        gangKaiHuBaoSanJia = StringUtil.getIntValue(params, 14, 0);//杠开胡包三家
        gangHouPaoSanJiaFu = StringUtil.getIntValue(params, 15, 1);//杠后炮三家付
        qiangGangHuSuanZiMo = StringUtil.getIntValue(params, 16, 0);//抢杠胡算自摸
        diFen = StringUtil.getIntValue(params, 17, 1);
        
        
		
		 
		 
		 
		  //加倍：0否，1是
	        this.jiaBei = StringUtil.getIntValue(params, 18, 0);
	        //加倍分
	        this.jiaBeiFen = StringUtil.getIntValue(params, 19, 0);
	        //加倍数
	        this.jiaBeiShu = StringUtil.getIntValue(params, 20, 0);
	        autoPlayGlob = StringUtil.getIntValue(params, 21, 0);

        if (shangZhongXiaNiao == 1) {
            birdNum = 1;
        }
        if (playerCount != 2) {
            //三人玩法默认抢杠胡算自摸
            qiangGangHuSuanZiMo = 1;
            jiaBei = 0 ;
        }

        setMaxPlayerCount(playerCount);
        changeExtend();
        if (!isJoinPlayerAllotSeat()) {
//			getRoomModeMap().put("1", "1"); //可观战（默认）
        }
    }

    // 是否两人麻将
    public boolean isTwoPlayer() {
        return getMaxPlayerCount() == 2;
    }


    @Override
    public boolean isAllReady() {
        if (super.isAllReady()) {
            if (getKeChui() == 1) {
                
                boolean bReturn = true;
                //机器人默认处理
                if (this.isTest()) {
                    for (SyMjPlayer robotPlayer : seatMap.values()) {
                        if (robotPlayer.isRobot()) {
                            robotPlayer.setChui(0);
                        }
                    }
                }
                ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_chui_start, getTableStatus());
                for (SyMjPlayer player : seatMap.values()) {
                    if (player.getChui() < 0) {
                    	if (getTableStatus() != SyMjConstants.TABLE_STATUS_CHUI) {
                    		player.setLastCheckTime(System.currentTimeMillis());
                    	}
                        player.writeSocket(com.build());
                        bReturn = false;
                    }
                }
                if (!bReturn) {
                    broadMsgRoomPlayer(com.build());
                }
                setTableStatus(SyMjConstants.TABLE_STATUS_CHUI);
                return bReturn;
            } else {
                return true;
            }
        }
        return false;
    }

    public static final List<Integer> wanfaList = Arrays.asList(
            GameUtil.game_type_symj);

    public static void loadWanfaTables(Class<? extends BaseTable> cls) {
        for (Integer integer : wanfaList) {
            TableManager.wanfaTableTypesPut(integer, cls);
        }

        HuUtil.init();
    }

    public int getIsAutoPlay() {
        return isAutoPlay;
    }

    public void setIsAutoPlay(int isAutoPlay) {
        this.isAutoPlay = isAutoPlay;
    }

    public void logFaPaiTable() {
        StringBuilder sb = new StringBuilder();
        sb.append("SyMj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append("faPai");
        sb.append("|").append(playType);
        sb.append("|").append(maxPlayerCount);
        sb.append("|").append(getPayType());
        sb.append("|").append(lastWinSeat);
        LogUtil.msg(sb.toString());
    }

    public void logFaPaiPlayer(SyMjPlayer player, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("SyMj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append(player.getName());
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append(player.isAutoPlaySelf() ? 1 : 0);
        sb.append("|").append("faPai");
        sb.append("|").append(player.getHandMajiang());
        sb.append("|").append(actListToString(actList));
        LogUtil.msg(sb.toString());
    }

    public void logAction(SyMjPlayer player, int action, List<SyMj> mjs, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("SyMj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append(player.isAutoPlaySelf() ? 1 : 0);
        String actStr = "unKnown-" + action;
        if (action == SyMjDisAction.action_peng) {
            actStr = "peng";
        } else if (action == SyMjDisAction.action_minggang) {
            actStr = "mingGang";
        } else if (action == SyMjDisAction.action_chupai) {
            actStr = "chuPai";
        } else if (action == SyMjDisAction.action_pass) {
            actStr = "guo";
        } else if (action == SyMjDisAction.action_angang) {
            actStr = "anGang";
        } else if (action == SyMjDisAction.action_chi) {
            actStr = "chi";
        }
        sb.append("|").append(actStr);
        sb.append("|").append(mjs);
        sb.append("|").append(actListToString(actList));
        LogUtil.msg(sb.toString());
    }

    public void logMoMj(SyMjPlayer player, SyMj mj, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("SyMj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append(player.isAutoPlaySelf() ? 1 : 0);
        sb.append("|").append("moPai");
        sb.append("|").append(leftMajiangs.size());
        sb.append("|").append(mj);
        sb.append("|").append(actListToString(actList));
//        sb.append("|").append(player.getHandMajiang());
        LogUtil.msg(sb.toString());
    }

    public void logChuPaiActList(SyMjPlayer player, SyMj mj, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("SyMj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append("chuPaiActList");
        sb.append("|").append(mj);
        sb.append("|").append(actListToString(actList));
//        sb.append("|").append(player.getHandMajiang());
        LogUtil.msg(sb.toString());
    }

    public void logActionHu(SyMjPlayer player, List<SyMj> mjs, String daHuNames) {
        StringBuilder sb = new StringBuilder();
        sb.append("SyMj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append(player.isAutoPlaySelf() ? 1 : 0);
        sb.append("|").append("huPai");
        sb.append("|").append(mjs);
        sb.append("|").append(daHuNames);
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
                if (i == SyMjConstants.ACTION_INDEX_HU) {
                    sb.append("hu");
                } else if (i == SyMjConstants.ACTION_INDEX_PENG) {
                    sb.append("peng");
                } else if (i == SyMjConstants.ACTION_INDEX_MINGGANG) {
                    sb.append("mingGang");
                } else if (i == SyMjConstants.ACTION_INDEX_ANGANG) {
                    sb.append("anGang");
                } else if (i == SyMjConstants.ACTION_INDEX_CHI) {
                    sb.append("chi");
                } else if (i == SyMjConstants.ACTION_INDEX_ZIMO) {
                    sb.append("ziMo");
                }
            }
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * 桌上剩余的牌数
     *
     * @return
     */
    public int getLeftMajiangCount() {
        return this.leftMajiangs.size();
    }

    public void addMoTailPai(int gangDice) {
        int leftMjCount = getLeftMajiangCount();
        int startIndex = 0;
        if (moTailPai.contains(0)) {
            int lastIndex = moTailPai.get(0);
            for (int i = 1; i < moTailPai.size(); i++) {
                if (moTailPai.get(i) == lastIndex + 1) {
                    lastIndex++;
                } else {
                    break;
                }
            }
            startIndex = lastIndex + 1;
        }
        if (gangDice == -1) {
            //补张，取一张
            for (int i = 0; i < leftMjCount; i++) {
                int nowIndex = i + startIndex;
                if (!moTailPai.contains(nowIndex)) {
                    moTailPai.add(nowIndex);
                    break;
                }
            }

        } else {
            int duo = gangDice / 10 + gangDice % 10;
            //开杠打色子，取两张
            for (int i = 0, j = 0; i < leftMjCount; i++) {
                int nowIndex = i + startIndex;
                if (nowIndex % 2 == 1) {
                    j++; //取到第几剁
                }
                if (moTailPai.contains(nowIndex)) {
                    if (nowIndex % 2 == 1) {
                        duo++;
                        leftMjCount = leftMjCount + 2;
                    }
                } else {
                    if (j == duo) {
                        moTailPai.add(nowIndex);
                        moTailPai.add(nowIndex - 1);
                        break;
                    }

                }
            }

        }
        Collections.sort(moTailPai);
        changeExtend();
    }

    /**
     * 清除摸屁股
     */
    public void clearMoTailPai() {
        this.moTailPai.clear();
        changeExtend();
    }

    /**
     * 是否流局
     *
     * @return
     */
    public int isLiuJu() {
        return (huConfirmList.size() == 0 && leftMajiangs.size() == 0) ? 1 : 0;
    }


    public void sendTingInfoOld(SyMjPlayer player) {
        if (player.isAlreadyMoMajiang()) {
            if (actionSeatMap.containsKey(player.getSeat())) {
                return;
            }
            PlayCardResMsg.DaPaiTingPaiRes.Builder tingInfo = PlayCardResMsg.DaPaiTingPaiRes.newBuilder();
            List<SyMj> cards = new ArrayList<>(player.getHandMajiang());
            int hzCount = SyMjTool.dropHongzhong(cards).size();
            Map<Integer, List<SyMj>> checked = new HashMap<>();
            for (SyMj card : cards) {
                if (card.isHongzhong()) {
                    continue;
                }
                List<SyMj> lackPaiList;
                if (checked.containsKey(card.getVal())) {
                    lackPaiList = checked.get(card.getVal());
                } else {
                    List<SyMj> copy = new ArrayList<>(cards);
                    copy.remove(card);
                    lackPaiList = SyMjTool.getLackList(copy, hzCount, true);
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
                    ting.addTingMajiangIds(SyMj.mj201.getId());
                } else {
                    for (SyMj lackPai : lackPaiList) {
                        ting.addTingMajiangIds(lackPai.getId());
                    }
                    ting.addTingMajiangIds(SyMj.mj201.getId());
                }
                tingInfo.addInfo(ting.build());
            }
            if (tingInfo.getInfoCount() > 0) {
                player.writeSocket(tingInfo.build());
            }
        } else {
            List<SyMj> cards = new ArrayList<>(player.getHandMajiang());
            int hzCount = SyMjTool.dropHongzhong(cards).size();
            List<SyMj> lackPaiList = SyMjTool.getLackList(cards, hzCount, true);
            if (lackPaiList == null || lackPaiList.size() == 0) {
                return;
            }
            PlayCardResMsg.TingPaiRes.Builder ting = PlayCardResMsg.TingPaiRes.newBuilder();
            if (lackPaiList.size() == 1 && null == lackPaiList.get(0)) {
                //听所有
                ting.addMajiangIds(SyMj.mj201.getId());
            } else {
                for (SyMj lackPai : lackPaiList) {
                    ting.addMajiangIds(lackPai.getId());
                }
                ting.addMajiangIds(SyMj.mj201.getId());
            }
            player.writeSocket(ting.build());
        }

    }

    public void sendTingInfo(SyMjPlayer player) {
        if (player.isAlreadyMoMajiang()) {
            if (actionSeatMap.containsKey(player.getSeat())) {
                return;
            }
            PlayCardResMsg.DaPaiTingPaiRes.Builder tingInfo = PlayCardResMsg.DaPaiTingPaiRes.newBuilder();
            List<SyMj> cards = new ArrayList<>(player.getHandMajiang());
            int hzCount = SyMjTool.dropHongzhong(cards).size();
            int[] cardArr = HuUtil.toCardArray(cards);
            Map<Integer, List<SyMj>> checked = new HashMap<>();
            List<SyMj> cards2 = new ArrayList<>(player.getHandMajiang());
            for (SyMj card : cards) {
                if (card.isHongzhong()) {
                    continue;
                }
                List<SyMj> lackPaiList;
                if (checked.containsKey(card.getVal())) {
                    lackPaiList = checked.get(card.getVal());
                } else {
                    int cardIndex = HuUtil.getMjIndex(card);
                    cardArr[cardIndex] = cardArr[cardIndex] - 1;
                    cards2.remove(card);
                    lackPaiList = SyMjTool.getLackList(cardArr, hzCount, true,player,this,cards2);
                    cardArr[cardIndex] = cardArr[cardIndex] + 1;
                    cards2.add(card);
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
                    ting.addTingMajiangIds(SyMj.mj201.getId());
                } else {
                    for (SyMj lackPai : lackPaiList) {
                        ting.addTingMajiangIds(lackPai.getId());
                    }
                }
                tingInfo.addInfo(ting.build());
            }
            if (tingInfo.getInfoCount() > 0) {
                player.writeSocket(tingInfo.build());
            }
        } else {
            List<SyMj> cards = new ArrayList<>(player.getHandMajiang());
            int hzCount = SyMjTool.dropHongzhong(cards).size();
            int[] cardArr = HuUtil.toCardArray(cards);
            List<SyMj> lackPaiList = SyMjTool.getLackList(cardArr, hzCount, true,player,this,cards);
            if (lackPaiList == null || lackPaiList.size() == 0) {
                return;
            }
            PlayCardResMsg.TingPaiRes.Builder ting = PlayCardResMsg.TingPaiRes.newBuilder();
            if (lackPaiList.size() == 1 && null == lackPaiList.get(0)) {
                //听所有
                ting.addMajiangIds(SyMj.mj201.getId());
            } else {
                for (SyMj lackPai : lackPaiList) {
                    ting.addMajiangIds(lackPai.getId());
                }
            }
            player.writeSocket(ting.build());
        }
    }
    

	@Override
	public boolean isPlaying() {
		if (super.isPlaying()) {
			return true;
		}
		return getTableStatus() == SyMjConstants.TABLE_STATUS_CHUI;
	}

    
    @Override
    public String getGameName() {
        return "邵阳麻将";
    }
}
