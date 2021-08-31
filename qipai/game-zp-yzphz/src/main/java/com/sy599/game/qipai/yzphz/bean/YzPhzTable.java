package com.sy599.game.qipai.yzphz.bean;

import static com.sy599.game.qipai.yzphz.util.YzPhzConstants.action_chi;
import static com.sy599.game.qipai.yzphz.util.YzPhzConstants.action_hu;
import static com.sy599.game.qipai.yzphz.util.YzPhzConstants.action_pao;
import static com.sy599.game.qipai.yzphz.util.YzPhzConstants.action_pass;
import static com.sy599.game.qipai.yzphz.util.YzPhzConstants.action_peng;
import static com.sy599.game.qipai.yzphz.util.YzPhzConstants.action_wangchuang;
import static com.sy599.game.qipai.yzphz.util.YzPhzConstants.action_wangdiao;
import static com.sy599.game.qipai.yzphz.util.YzPhzConstants.action_wangzha;

import java.security.SecureRandom;
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

import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.GeneratedMessage;
import com.sy599.game.GameServerConfig;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.bean.CreateTableInfo;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.common.constant.SharedConstants.player_state;
import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.db.bean.TableInf;
import com.sy599.game.db.bean.UserPlaylog;
import com.sy599.game.db.bean.UserStatistics;
import com.sy599.game.db.bean.gold.GoldRoom;
import com.sy599.game.db.dao.TableDao;
import com.sy599.game.db.dao.TableLogDao;
import com.sy599.game.db.dao.gold.GoldRoomDao;
import com.sy599.game.manager.TableManager;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.DaPaiTingPaiInfo;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.DaPaiTingPaiRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.PlayPaohuziRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.TingPaiRes;
import com.sy599.game.msg.serverPacket.TablePhzResMsg.ClosingPhzInfoRes;
import com.sy599.game.msg.serverPacket.TablePhzResMsg.ClosingPhzPlayerInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.CreateTableRes;
import com.sy599.game.msg.serverPacket.TableRes.DealInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes;
import com.sy599.game.qipai.yzphz.util.GuihzCard;
import com.sy599.game.qipai.yzphz.util.YzPhzCardMessage;
import com.sy599.game.qipai.yzphz.util.YzPhzCardResult;
import com.sy599.game.qipai.yzphz.util.YzPhzCardUtils;
import com.sy599.game.qipai.yzphz.util.YzPhzConstants;
import com.sy599.game.qipai.yzphz.util.YzPhzFanEnums;
import com.sy599.game.qipai.yzphz.util.YzPhzHandCards;
import com.sy599.game.qipai.yzphz.util.YzPhzHuPaiUtils;
import com.sy599.game.qipai.yzphz.util.zphu.ZpConstant;
import com.sy599.game.qipai.yzphz.util.zphu.ZpHuBean;
import com.sy599.game.staticdata.KeyValuePair;
import com.sy599.game.udplog.UdpLogger;
import com.sy599.game.util.DataMapUtil;
import com.sy599.game.util.GameConfigUtil;
import com.sy599.game.util.GameUtil;
import com.sy599.game.util.JacksonUtil;
import com.sy599.game.util.JsonWrapper;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.PayConfigUtil;
import com.sy599.game.util.ResourcesConfigsUtil;
import com.sy599.game.util.StringUtil;
import com.sy599.game.util.TimeUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

public class YzPhzTable extends BaseTable implements YzPhzBase {
	/*** 玩家map */
    private Map<Long, YzPhzPlayer> playerMap = new ConcurrentHashMap<>();
	/*** 座位对应的玩家 */
    private Map<Integer, YzPhzPlayer> seatMap = new ConcurrentHashMap<>();
    private List<Integer> leftCards = new ArrayList<>();
	/*** 摸牌flag */
    private volatile int moFlag;
	/*** 应该要打牌的flag */
    private volatile int toPlayCardFlag;
    private volatile YzPhzCheckCardBean autoDisBean;
    private volatile int moSeat;
    private Integer zaiCard;
    private volatile Integer beRemoveCard;
    private int maxPlayerCount = 3;
    private List<Integer> huConfirmList = new ArrayList<>();
	/*** 摸牌时对应的座位 */
    private KeyValuePair<Integer, Integer> moSeatPair;
	/*** 摸牌时对应的座位 */
    private KeyValuePair<Integer, Integer> checkMoMark;
    private int sendPaoSeat;
    private boolean firstCard = true;
    private int shuXingSeat = 0;
    private int wangbaCount = 2;
    private int fanxinConfig = 0;
	private int disposeCard = 0;// 当前处理的字牌cardId
	private int limitHu = -1;// 0有王必须自摸,1按王限制胡牌，2按照番数限制胡牌
	private int limitScore = 0;// 0不封顶
	private volatile int huCard = 0;// 胡牌card
	private volatile int paoHu = 0;// 是否跑胡
    private volatile int moCount = 0;
    private volatile int disCount = 0;
	private int red2Black = 0;// 红转朱黑（1是）
	private int minHuxi = 15;// 15胡息起胡
	private int xingRate = 1;// 醒的倍率
	private volatile int lastCard = 0;// 胡前最后一张牌
    private String modeId = "0";
    //低于below加分
    private int belowAdd=0;
    private int below=0;

    
	/** 托管 0不托管 */
	private int tuoguan;
	/** 托管1：单局，2：全局 */
    private int autoPlayGlob;
    private int autoTableCount;
	// 是否加倍：0否，1是
    private int jiaBei;
	// 加倍分数：低于xx分进行加倍
    private int jiaBeiFen;
	// 加倍倍数：翻几倍
    private int jiaBeiShu;

	/** 二人总结算封顶 */
	private int topFengding;
     /**随机庄家*/
    private  int randomZhuangj;
    private final Object cardLock = new Object();
    private volatile int timeNum = 0;
    /**
	 * 托管时间
	 */
    private volatile int autoTimeOut = Integer.MAX_VALUE;
    
	private KeyValuePair<Integer, Integer> lastSeatAction; // 最后操作的<seat,
															// action>

    /**
	 * 玩家操作
	 */
    private Map<Integer, String> userActionMap = new ConcurrentHashMap<>();

    /**
	 * 0胡 1碰 2栽 3提 4吃 5跑 6臭栽  7王调
	 */
    private volatile Map<Integer, List<Integer>> actionSeatMap = new ConcurrentHashMap<>();
    private List<Integer> nowDisCardIds = new ArrayList<>();

    @Override
    public void initExtend0(JsonWrapper wrapper) {
//		JsonWrapper wrapper = new JsonWrapper(info);
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
        String autoDisPhz = wrapper.getString(6);
        if (!StringUtils.isBlank(autoDisPhz)) {
            autoDisBean = new YzPhzCheckCardBean();
            autoDisBean.initAutoDisData(autoDisPhz);
        }
        zaiCard = wrapper.getInt(7, 0);
        sendPaoSeat = wrapper.getInt(8, 0);
        firstCard = wrapper.getInt(9, 1) == 1 ? true : false;
        beRemoveCard = wrapper.getInt(10, 0);
       // shuXingSeat = wrapper.getInt(11, 0);
        maxPlayerCount = wrapper.getInt(12, 3);
        wangbaCount = wrapper.getInt(13, 2);
        fanxinConfig = wrapper.getInt(14, 0);
        disposeCard = wrapper.getInt(15, 0);

        String lastSeatActionVal = wrapper.getString(16);
        if (!StringUtils.isBlank(lastSeatActionVal)) {
            lastSeatAction = new KeyValuePair<>();
            String[] values = lastSeatActionVal.split("_");
            String idStr = StringUtil.getValue(values, 0);
            if (!StringUtil.isBlank(idStr)) {
                lastSeatAction.setId(Integer.parseInt(idStr));
            }

            lastSeatAction.setValue(StringUtil.getIntValue(values, 1));
        }

        String limitHuStr = wrapper.getString(17);
        if (NumberUtils.isDigits(limitHuStr)) {
            limitHu = Integer.parseInt(limitHuStr);
        }
        limitScore = wrapper.getInt(18, 0);
        huCard = wrapper.getInt(19, 0);
        paoHu = wrapper.getInt(20, 0);
        moCount = wrapper.getInt(21, 0);
        disCount = wrapper.getInt(22, 0);
        red2Black = wrapper.getInt(23, 0);
        minHuxi = wrapper.getInt(24, 15);
        xingRate = wrapper.getInt(25, 1);
        lastCard = wrapper.getInt(26, 1);
        
        
        
        
        tuoguan = wrapper.getInt(27, 1);
        autoPlayGlob = wrapper.getInt(28, 1);
        
        if(tuoguan>0) {
        	autoTimeOut = (tuoguan*1000);
        }

        
        jiaBei = wrapper.getInt(29, 15);
        jiaBeiFen = wrapper.getInt(30, 1);
        jiaBeiShu = wrapper.getInt(31, 1);
		topFengding = wrapper.getInt(32, 0);
        String recallMsg = wrapper.getString("recall");
        if (StringUtils.isNotBlank(recallMsg)) {
            String[] strs = recallMsg.split(",");
            for (String str : strs) {
                int idx = str.indexOf("_");
                if (idx > 0) {
                    userActionMap.put(Integer.valueOf(str.substring(0, idx)), str.substring(idx + 1));
                }
            }
        }
        below = wrapper.getInt(33, 0);
        belowAdd = wrapper.getInt(34, 0);

        randomZhuangj =  wrapper.getInt(35, 0);

        modeId = wrapper.getString("gModeId");
        if (StringUtils.isBlank(modeId)) {
            modeId = "0";
        }
    }

    /**
	 * 记忆操作
	 *
	 * @param userSeat
	 * @param action
	 *            1胡2碰6吃7跑
	 * @param cards
	 */
    private synchronized boolean recallAction(int userSeat, int action, List<Integer> cards) {
        int seat0;
        int action0;
        if (action == action_hu || action == action_peng || action == action_chi || action == action_pao || action == action_pass) {
            if (cards == null || cards.size() == 0) {
                userActionMap.put(userSeat, String.valueOf(action));
            } else {
                StringBuilder strBuilder = new StringBuilder();
                strBuilder.append(action);
                for (Integer integer : cards) {
                    strBuilder.append("_").append(integer);
                }
                userActionMap.put(userSeat, strBuilder.toString());
            }

            seat0 = userSeat;
            action0 = action;
            changeExtend();
        } else {
            seat0 = -1;
            action0 = -1;
        }

        for (Entry<Integer, String> kv : userActionMap.entrySet()) {
            int tempSeat = kv.getKey().intValue();
            if (tempSeat != userSeat) {
                String[] strs = kv.getValue().split("_");
                int act = Integer.parseInt(strs[0]);
                if (act == action_hu) {
                    if (action0 != action_hu) {
                        action0 = action_hu;
                        seat0 = tempSeat;
                    } else {
                        if (disCardSeat == tempSeat) {
                            seat0 = tempSeat;
                        } else if (disCardSeat == seat0) {
                        } else {
                            int ts = calcNextSeat(disCardSeat);
                            int m = 0, n = 0, k = 0;
                            while (ts != disCardSeat) {
                                k++;
                                if (ts == seat0) {
                                    m = k;
                                } else if (ts == tempSeat) {
                                    n = k;
                                }
                                ts = calcNextSeat(ts);
                            }
                            if (m > n) {
                                seat0 = tempSeat;
                            }
                        }
                    }
                } else if (act == action_pao) {
                    if (action0 != action_hu) {
                        action0 = action_pao;
                        seat0 = tempSeat;
                    }
                } else if (act == action_peng) {
                    if (action0 != action_hu) {
                        action0 = action_peng;
                        seat0 = tempSeat;
                    }
                } else if (act == action_chi) {
                    if (action0 == -1 || action0 == action_pass) {
                        action0 = action_chi;
                        seat0 = tempSeat;
                    } else if (action0 == action_chi) {
                        if (disCardSeat == tempSeat) {
                            seat0 = tempSeat;
                        } else if (disCardSeat == seat0) {
                        } else {
                            int ts = calcNextSeat(disCardSeat);
                            int m = 0, n = 0, k = 0;
                            while (ts != disCardSeat) {
                                k++;
                                if (ts == seat0) {
                                    m = k;
                                } else if (ts == tempSeat) {
                                    n = k;
                                }
                                ts = calcNextSeat(ts);
                            }
                            if (m > n) {
                                seat0 = tempSeat;
                            }
                        }
                    }
                }
            }
        }

		// 0胡1碰4吃5跑
        for (Entry<Integer, List<Integer>> kv : actionSeatMap.entrySet()) {
            int tempSeat = kv.getKey().intValue();
            if (tempSeat != userSeat) {
                List<Integer> list = kv.getValue();
                if (list.get(0).intValue() == 1) {
                    if (action0 != action_hu) {
                        action0 = action_hu;
                        seat0 = tempSeat;
                    } else {
                        if (disCardSeat == tempSeat) {
                            seat0 = tempSeat;
                        } else if (disCardSeat == seat0) {
                        } else {
                            int ts = calcNextSeat(disCardSeat);
                            int m = 0, n = 0, k = 0;
                            while (ts != disCardSeat) {
                                k++;
                                if (ts == seat0) {
                                    m = k;
                                } else if (ts == tempSeat) {
                                    n = k;
                                }
                                ts = calcNextSeat(ts);
                            }
                            if (m > n) {
                                seat0 = tempSeat;
                            }
                        }
                    }
                } else if (list.get(5).intValue() == 1) {
                    if (action0 != action_hu) {
                        action0 = action_pao;
                        seat0 = tempSeat;
                    }
                } else if (list.get(1).intValue() == 1) {
                    if (action0 != action_hu) {
                        action0 = action_peng;
                        seat0 = tempSeat;
                    }
                } else if (list.get(4).intValue() == 1) {
                    if (action0 == -1 || action0 == action_pass) {
                        action0 = action_chi;
                        seat0 = tempSeat;
                    } else if (action0 == action_chi) {
                        if (disCardSeat == tempSeat) {
                            seat0 = tempSeat;
                        } else if (disCardSeat == seat0) {
                        } else {
                            int ts = calcNextSeat(disCardSeat);
                            int m = 0, n = 0, k = 0;
                            while (ts != disCardSeat) {
                                k++;
                                if (ts == seat0) {
                                    m = k;
                                } else if (ts == tempSeat) {
                                    n = k;
                                }
                                ts = calcNextSeat(ts);
                            }
                            if (m > n) {
                                seat0 = tempSeat;
                            }
                        }
                    }
                }
            }
        }

        if (seat0 != -1 && seat0 != userSeat) {
            String msg = userActionMap.get(seat0);
            int act = -1;
            List<Integer> list;
            if (msg != null) {
                list = new ArrayList<>();
                String[] strs = msg.split("_");
                act = Integer.parseInt(strs[0]);
                for (int i = 1; i < strs.length; i++) {
                    list.add(Integer.parseInt(strs[i]));
                }
            } else {
                list = new ArrayList<>();
            }
            if (act != -1 && act == action0) {
                LogUtil.msgLog.info("recall action:tableId=" + id + ",userId0=" + seatMap.get(seat0).getUserId() + ",seat0=" + seat0 + ",actionMsg=" + msg + ",card=" + lastCard);
				// action 1胡2碰6吃7跑
                if (act == 1) {
                    addAction(seat0, YzPhzCheckCardBean.hasActionList(0));
                } else if (act == 2) {
                    addAction(seat0, YzPhzCheckCardBean.hasActionList(1));
                } else if (act == 6) {
                    addAction(seat0, YzPhzCheckCardBean.hasActionList(4));
                } else if (act == 9) {
                    addAction(seat0, YzPhzCheckCardBean.hasActionList(5));
                }

                if (action0 != action_pass) {
                    play(seatMap.get(seat0), list, action0);
                    return true;
                }
            } else {
                LogUtil.msgLog.info("wait action:tableId=" + id + ",userId0=" + seatMap.get(seat0).getUserId() + ",seat0=" + seat0 + ",actions=" + actionSeatMap.get(seat0) + ",card=" + lastCard);
            }
        }
        return false;
    }

    @Override
    public <T> T getPlayer(long id, Class<T> cl) {
        return (T) playerMap.get(id);
    }

    @Override
    protected void initNowAction(String nowAction) {
        JsonWrapper wrapper = new JsonWrapper(nowAction);
        String val1 = wrapper.getString(1);
        if (!StringUtils.isBlank(val1)) {
            actionSeatMap.putAll(DataMapUtil.toListMap(val1));
        }
    }

    @Override
    protected String buildNowAction() {
        JsonWrapper wrapper = new JsonWrapper("");
        wrapper.putString(1, DataMapUtil.explodeListMap(actionSeatMap));
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
        if (state == SharedConstants.table_state.ready) {
            return;
        }

        boolean isHuangZhuang = false;
        List<Integer> winList = new ArrayList<>(huConfirmList);
        if (winList.size() == 0 && leftCards.size() == 0) {
			// 流局
            isHuangZhuang = true;
        }

        int totalFan = 1;
        List<Integer> mingtang = new ArrayList<>();
		int totalTun = 0;// 几等

        List<Integer> fanXinCards = new ArrayList<>();
        int fanXinCardId = disposeCard;
        int fanxinPoint = 0;
        boolean isSelfMo = false;

        int goldPay = 0;
        int goldRatio = 1;
        String modeId = "0";
//        boolean isGold = isGoldRoom();
//        if (isGold) {
//            try {
//                GoldRoom goldRoom = GoldRoomDao.getInstance().loadGoldRoom(id);
//                if (goldRoom != null) {
//                    modeId = goldRoom.getModeId();
//                    goldPay = PayConfigUtil.get(playType, goldRoom.getGameCount(), goldRoom.getMaxCount(), 0, goldRoom.getModeId());
//                    if (goldPay < 0) {
//                        goldPay = 0;
//                    }
//                    goldRatio = GameConfigUtil.loadGoldRatio(modeId);
//                }
//            } catch (Exception e) {
//            }
//        }

        int fanXinCardId2 =0;
        int totalPoint2 =0;
        for (int winSeat : winList) {
			// 赢的玩家
            isSelfMo = winSeat == moSeat;
            YzPhzPlayer winPlayer = seatMap.get(winSeat);
            int getPoint = 0;
            int onePoint = 0;
            int temPoint = 0;

			if (isTianHu()) {// 天胡
                isSelfMo = true;
            }
            winPlayer.changeAction(YzPhzConstants.ACTION_COUNT_INDEX_HU, 1);
            if(isSelfMo){
            	winPlayer.changeAction(YzPhzConstants.ACTION_COUNT_INDEX_ZIMO, 1);
            }


            fanxinPoint = processFanXin(winPlayer, fanXinCards);

            totalTun = winPlayer.calcHuPoint(this);
            totalFan = winPlayer.getTotalFan();
            mingtang = winPlayer.loadFan();

            if (fanXinCards.size() > 0) {
                fanXinCardId = fanXinCards.get(0);
            }
            fanXinCardId2 = fanXinCardId;
            if(fanXinCardId >80) {
            	fanXinCardId2 = winPlayer.getHuResult().getXingCard();
            	GuihzCard card =	GuihzCard.getGuihzCard(fanXinCardId2);
            	if(card !=null) {
            		fanXinCardId2 = card.getId();
            	}
            }

            int basePoint = totalTun * totalFan;
            int fanPoint = fanxinPoint * totalFan;
            onePoint = basePoint + fanPoint;
            totalPoint2 = onePoint*(getMaxPlayerCount()-1);
//            if (isGold) {
//                onePoint = onePoint * goldRatio;
//            } else {
//                if (hasZuoXing()) {
//                    if (limitScore > 0 && onePoint + 2 * fanPoint > limitScore) {
//                        onePoint = limitScore;
//
//                        if (basePoint >= limitScore) {
//                            basePoint = limitScore;
//                            fanPoint = 0;
//                        } else {
//                            fanPoint = (limitScore - basePoint) / 2;
//                        }
//                    }
//                } else {
                    if (limitScore > 0 && onePoint > limitScore) {
                        onePoint = limitScore;

                        if (basePoint >= limitScore) {
                            basePoint = limitScore;
                            fanPoint = 0;
                        } else {
                            fanPoint = limitScore - basePoint;
                        }
                    }
//                }
//            }

            for (int seat : seatMap.keySet()) {
                if (!winList.contains(seat)) {
                    YzPhzPlayer player = seatMap.get(seat);
//                    if (hasZuoXing() && seat == shuXingSeat) {
//                        YzPhzPlayer shuXingPlayer = seatMap.get(shuXingSeat);
//                        shuXingPlayer.calcResult(1, 2 * fanPoint, isHuangZhuang);
//                        continue;
//                    }
                    int tempPoint;
//                    if (isGold) {
//                        if (player.getGoldPlayer().getAllGold() < goldPay + onePoint) {
//                            tempPoint = (player.getGoldPlayer().getAllGold() > goldPay) ? (int) (player.getGoldPlayer().getAllGold() - goldPay) : 0;
//                        } else {
//                            tempPoint = onePoint;
//                        }
//                        player.changeGold((-tempPoint - goldPay), playType);
//                    } else {
                        tempPoint = basePoint + fanPoint;
//                    }
                    getPoint += tempPoint;
                    if (hasZuoXing()) {
                        temPoint = fanPoint;
                    }
                    player.calcResult(1, -(tempPoint + temPoint), isHuangZhuang);
                }
            }

            winPlayer.calcResult(1, getPoint, isHuangZhuang);

        }

        if (winList.isEmpty()) {
            if (wangbaCount <= 0) {
                for (Entry<Integer, YzPhzPlayer> kv : seatMap.entrySet()) {
                    kv.getValue().calcResult(1, 0, true);
                   
                }
            } else {
                Map<Integer, Integer> pointMap = new HashMap<>();
                for (Entry<Integer, YzPhzPlayer> kv : seatMap.entrySet()) {
                    int seat = kv.getKey().intValue();
//                    if (seat == shuXingSeat) {
//                        pointMap.put(seat, 0);
//                    } else 
                    
                    
                    {
                        Integer tempPoint = pointMap.get(seat);
                        if (tempPoint == null) {
                            tempPoint = 0;
                        }

                        for (Entry<Integer, YzPhzPlayer> kv1 : seatMap.entrySet()) {
                            int seat1 = kv1.getKey().intValue();
                            if (seat1 == seat) {
                            } else {
                                Integer tempPoint1 = pointMap.get(seat1);
                                if (tempPoint1 == null) {
                                    tempPoint1 = 0;
                                }
                                tempPoint1 += (kv.getValue().getHandCards().WANGS.size() * 5);
                                pointMap.put(seat1, tempPoint1);

                                tempPoint += (-kv.getValue().getHandCards().WANGS.size() * 5);
                            }
                        }

                        pointMap.put(seat, tempPoint);
                    }
                }

                    for (Entry<Integer, YzPhzPlayer> kv : seatMap.entrySet()) {
                        kv.getValue().calcResult(1, pointMap.get(kv.getKey()), true);
                    }
                }
            

        }
        
        boolean isOver = playBureau >= totalBureau;
        if(autoPlayGlob >0) {
			// //是否解散
            boolean diss = false;
            if(autoPlayGlob ==1) {
            	 for (YzPhzPlayer seat : seatMap.values()) {
                 	if(seat.isAutoPlay()) {
                     	diss = true;
                     	break;
                     }
                 }
            }else if (autoPlayGlob == 3) {
				diss = checkAuto3();
			}
            if(diss) {
            	 autoPlayDiss= true;
            	 isOver =true;
            }
        }
        
		if (maxPlayerCount == 2 && topFengding > 0 && winList.size() > 0) {
			YzPhzPlayer winPlayer = seatMap.get(winList.get(0));
			if (winPlayer != null && winPlayer.getTotalPoint() >= topFengding) {
				winPlayer.setTotalPoint(topFengding);
				
				 logTotalPointMsg(winPlayer,1);
				
				for (YzPhzPlayer player : seatMap.values()) {
					if (!winList.contains(player.getSeat())) {
						 logTotalPointMsg(player,1);
						player.setTotalPoint(-topFengding);
						break;
					}
				}
				isOver = true;
			}
		}
		
		
        if(isOver){
            calcPointBeforeOver();
        }

        // 金币场
        if(isGoldRoom()){
            for(YzPhzPlayer player : seatMap.values()){
                player.setPoint(player.getTotalPoint());
                player.setWinGold(player.getTotalPoint());
            }
            calcGoldRoom();
        }
        
        ClosingPhzInfoRes.Builder res = sendAccountsMsg(isOver, isSelfMo, winList, totalFan, mingtang, totalTun, false, fanXinCardId, fanxinPoint,fanXinCardId2,totalPoint2);

        
       // ClosingPhzInfoRes.Builder res = sendAccountsMsg(isOver, isSelfMo, winList, totalFan, mingtang, totalTun, false, fanXinCardId, fanxinPoint);
        saveLog(isOver, 0l, res.build());
        if (!winList.isEmpty()) {
            setLastWinSeat(winList.get(0));
        } else {
            boolean wang = false;
            int seat1 = 0;
            int seat2 = 0;
            int seat3 = 0;
            for (int seat : seatMap.keySet()) {
                YzPhzPlayer player = seatMap.get(seat);
                if (wangbaCount == 2 && player.getHandCards().WANGS.size() >= 2
                        || wangbaCount == 3 && player.getHandCards().WANGS.size() >= 2
                        || wangbaCount == 4 && player.getHandCards().WANGS.size() >= 3) {
                    setLastWinSeat(seat);
                    wang = true;
                    break;
                }
                if (wangbaCount == 4) {
                    if (player.getHandCards().WANGS.size() == 1) {
                        if (seat1 == 0) {
                            seat1 = seat;
                        } else {
                            seat2 = seat;
                        }
                    } else if (player.getHandCards().WANGS.size() == 2) {
                        seat3 = seat;
                    }
                }
            }
            if (!wang) {
                if (seat1 > 0 && seat2 > 0 && seat3 > 0) {
                    setLastWinSeat(seat3);
                } else {
                    setLastWinSeat(moSeat);
                }
            }
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

        if (isOver) {
            state = SharedConstants.table_state.over;
        }
    }
    
    
    public void calcPointBeforeOver() {
    	// 大结算计算加倍分
		// 大结算计算加倍分
        if( jiaBei == 1){
            int jiaBeiPoint = 0;
            int loserCount = 0;
                for (YzPhzPlayer player : seatMap.values()) {
               	 logTotalPointMsg(player,2);
                    if (player.getTotalPoint() > 0 && player.getTotalPoint() < jiaBeiFen) {
                        jiaBeiPoint += player.getTotalPoint() * (jiaBeiShu - 1);
                        player.setTotalPoint(player.getTotalPoint() * jiaBeiShu);
                    } else if (player.getTotalPoint() < 0) {
                        loserCount++;
                    }
                }
                if (jiaBeiPoint > 0) {
                    for (YzPhzPlayer player : seatMap.values()) {
                        if (player.getTotalPoint() < 0) {
                            player.setTotalPoint(player.getTotalPoint() - (jiaBeiPoint / loserCount));
                        }
                    }
                }
        }

       //大结算低于below分+belowAdd分
       if(belowAdd>0&&playerMap.size()==2){
           for (YzPhzPlayer player : seatMap.values()) {
           	logTotalPointMsg(player,3);
               int totalPoint = player.getTotalPoint();
               if (totalPoint >-below&&totalPoint<0) {
                   player.setTotalPoint(player.getTotalPoint()-belowAdd);
               }else if(totalPoint < below&&totalPoint>0){
                   player.setTotalPoint(player.getTotalPoint()+belowAdd);
               }
           }
       }
    	
    }
    
	private boolean checkAuto3() {
		boolean diss = false;
//		if(autoPlayGlob==3) {
			boolean diss2 = false;
			 for (YzPhzPlayer seat : seatMap.values()) {
		      	if(seat.isAutoPlay()) {
		      		diss2 = true;
		          	break;
		          }
		      }
			 if(diss2) {
				 autoTableCount +=1;
			 }else{
				 autoTableCount = 0;
			 }
			if(autoTableCount==3) {
				diss = true;
			}
//		}
		return diss;
	}


    @Override
    public UserStatistics loadRobotUserStatistics(Player player) {
        return player.isRobot() ? new UserStatistics(modeId + "_" + player.getUserId(), player.loadScore(), isMatchRoom() ? "match" : isGoldRoom() ? "gold" : "common", playType, playBureau) : null;
    }


    public int processFanXin(YzPhzPlayer player, List<Integer> fanXinCards) {
        int card = player.getHuResult().getXingCard();
        fanXinCards.add(card);
        
        int xingCount = player.loadValCount(null, YzPhzCardUtils.loadCardVal(card));
        //特殊牌型补丁 王做对。醒牌不是王 
        if(loadXingMode() == 0&& card<=80){
        	  int dui = getDui(player);
              if(dui>=2&&xingCount<6){
            	  xingCount+=dui; 
              }
        }
        
        return loadXingTun(xingCount);
    }

	private int getDui(YzPhzPlayer player) {
		List<YzPhzCardMessage>  yzlist = player.getHuResult().getCardMessageList();
		  int count = 0;
		  if(player.getHuResult().contanWangFan()){
			  return count;
		  }
		  for(YzPhzCardMessage ym : yzlist){
			  if(ym.getHuXiEnum().getAction()==0&&getWangCount(ym.getCards())==2){
				  count =2;
				  break;
			  }else if (getWangCount(ym.getCards())==3&&ym.getCards().size()==3){
				  count =3;
				  break;
			  }
		  }
		  
		return count;
	}
	
	
	
	private int getWangCount(List<Integer> cards){
		int count = 0;
		for(Integer id: cards){
			if(id>80){
				count++;
			}
		}
		return count;
		
	}

    @Override
    public boolean allowRobotJoin() {
        return StringUtils.contains(ResourcesConfigsUtil.loadServerPropertyValue("robot_modes", "|371|381|391|491|"), new StringBuilder().append("|").append(modeId).append("|").toString());
    }

    public void saveLog(boolean over, long winId, Object resObject) {
        ClosingPhzInfoRes res = (ClosingPhzInfoRes) resObject;
        LogUtil.d_msg("tableId:" + id + " play:" + playBureau + " over:" + res);
        String logRes = JacksonUtil.writeValueAsString(LogUtil.buildClosingInfoResLog(res));
        String logOtherRes = JacksonUtil.writeValueAsString(LogUtil.buildClosingInfoResOtherLog(res));
        Date now = TimeUtil.now();
        UserPlaylog userLog = new UserPlaylog();
        userLog.setLogId(playType);
        userLog.setUserId(creatorId);
        userLog.setTableId(id);
        userLog.setRes(extendLogDeal(logRes));
        userLog.setTime(now);
        userLog.setTotalCount(totalBureau);
        userLog.setCount(playBureau);
        userLog.setStartseat(lastWinSeat);
        userLog.setOutCards(playLog);
        userLog.setExtend(logOtherRes);
        userLog.setType(creditMode == 1 ? 2 : 1 );
        userLog.setMaxPlayerCount(maxPlayerCount);
        userLog.setGeneralExt(buildGeneralExtForPlaylog().toString());
        long logId = TableLogDao.getInstance().save(userLog);
        saveTableRecord(logId, over, playBureau);
        if (!isGoldRoom()) {
            for (YzPhzPlayer player : playerMap.values()) {
                player.addRecord(logId, playBureau);
            }
        }

        UdpLogger.getInstance().sendSnapshotLog(masterId, playLog, logRes);
    }

    List<Integer> str2IntList(String str) {
        List<Integer> list = new ArrayList<>();
        String[] strs = str.split(",");
        for (String temp : strs) {
            if (StringUtils.isNotBlank(temp)) {
                list.add(Integer.valueOf(temp));
            }
        }
        return list;
    }

    String intList2Str(List<Integer> list) {
        if (list == null) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (Integer temp : list) {
            stringBuilder.append(",").append(temp);
        }
        return stringBuilder.length() > 0 ? stringBuilder.substring(1) : "";
    }

    @Override
    protected void loadFromDB1(TableInf info) {
        if (!StringUtils.isBlank(info.getNowDisCardIds())) {
            this.nowDisCardIds = str2IntList(info.getNowDisCardIds());
        }
        if (!StringUtils.isBlank(info.getLeftPais())) {
            this.leftCards = str2IntList(info.getLeftPais());
        }
    }

    @Override
    protected void sendDealMsg() {
        sendDealMsg(0);
    }

    public void reSend() {
        setMoFlag(1);

        sendDealMsg(0);

        startNext();
    }

    @Override
    protected void sendDealMsg(long userId) {
		// // 天胡或者暗杠
//		int lastCardIndex = RandomUtils.nextInt(21);
		int lastCardIndex = RandomUtils.nextInt(14);
        for (YzPhzPlayer tablePlayer : seatMap.values()) {
            DealInfoRes.Builder res = DealInfoRes.newBuilder();
//            if (tablePlayer.getSeat() == shuXingSeat) {
//                res.addAllHandCardIds(seatMap.get(lastWinSeat).getHandPais());
//            } else {
//               
//            }
            
            res.addAllHandCardIds(tablePlayer.getHandPais());
            res.setNextSeat(lastWinSeat);
			res.setGameType(getWanFa());// 1跑得快 2麻将
            res.setRemain(leftCards.size());
            res.setBanker(lastWinSeat);
           int card = seatMap.get(lastWinSeat).getHandPais().get(lastCardIndex);
           if(lastWinSeat == tablePlayer.getSeat()) {
        	   res.addXiaohu(card);
           }else{
        	   res.addXiaohu(0);
           }
			
			setLastCard(card);

            tablePlayer.writeSocket(res.build());
        }
        for (YzPhzPlayer tablePlayer : seatMap.values()) {
        	sendTingInfo(tablePlayer);
        	if(tablePlayer.isAutoPlay()) {
       		 addPlayLog(tablePlayer.getSeat(), YzPhzConstants.action_tuoguan + "",1 + "");
        	}
        }

    }

    @Override
    public void startNext() {
        checkAction();
    }

    public void play(YzPhzPlayer player, List<Integer> cardIds, int action) {
        play(player, cardIds, action, false, false, false);
    }


    private synchronized void hu(YzPhzPlayer player, List<Integer> cardList, int action, Integer nowDisCard) {
        List<Integer> actionList = actionSeatMap.get(player.getSeat());
        if (actionList == null) {
            LogUtil.msgLog.info(" actionSeatMap" + player.getSeat());
            return;
        }
        saveActionSeatMap();

        if (huConfirmList.contains(player.getSeat())) {
            LogUtil.msgLog.info(" huConfirmList" + player.getSeat());
            return;
        }

        if (recallAction(player.getSeat(), action, cardList)) {
            return;
        }


        int tempAction = action;
        boolean diaoOrChuang = false;
        
      boolean canwang =   actionList.get(7) ==1||actionList.get(8) ==1||actionList.get(9) ==1;
      
      if((action == YzPhzConstants.action_wangdiao || action == YzPhzConstants.action_wangchuang
              || action == YzPhzConstants.action_wangzha)&&!canwang){
    	  return;
      }
      
      
        if (action == YzPhzConstants.action_wangdiao || action == YzPhzConstants.action_wangchuang
                || action == YzPhzConstants.action_wangzha||canwang) {

            if ((disCardSeat != player.getSeat() && actionSeatMap.size() >= 2) || toPlayCardFlag == 1) {
                LogUtil.msgLog.info("tableId=" + id + "wait seat:" + disCardSeat + "!=" + player.getSeat() + ",userId=" + player.getUserId());
                return;
            }

            tempAction = 1;
            diaoOrChuang = true;
        }


        if (!checkAction(player, tempAction)) {
//            LogUtil.msgLog.info(" checkAction" + tempAction);
			// 更新前台数据
        	player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip);
            //sendPlayerActionMsg(player, true, null, action_type_clear, action_refreshaction);
            return;
        }

        int wangHu = 0;
        if (actionList.get(0).intValue() != 1) {
            LogUtil.msgLog.info("hu actions:tableId=" + getId() + ",userId=" + player.getUserId() + ",actionList=" + actionList + ",dianjiAction=" + action);
            
            
            if ((action==YzPhzConstants.action_hu||action == YzPhzConstants.action_wangdiao) && actionList.get(7).intValue() == 1) {
                wangHu = 1;
            } else if ((action==YzPhzConstants.action_hu||action == YzPhzConstants.action_wangchuang) && actionList.get(8).intValue() == 1) {
                wangHu = 2;
            } else if ((action==YzPhzConstants.action_hu||action == YzPhzConstants.action_wangzha) && actionList.get(9).intValue() == 1) {
                wangHu = 3;
            } else {
                LogUtil.msgLog.info(" actionList" + actionList.get(0));
                return;
            }
        }

        player.setCanHuState(1);
        YzPhzCardResult hu;

        boolean isZiMo;
        boolean isPaoHu = false;
        if (diaoOrChuang) {
            nowDisCard = getNextCard();
            if (nowDisCard > 80) {
            	if(player.getHandCards().WANGS.size()>=3){
            		if(getLeftCards().size()>0){
            			Integer temp = getNextCard();
            			getLeftCards().add(nowDisCard);
            			nowDisCard = temp;
            		}
            	}
            }
            
            setMoFlag(1);
            setMoSeat(player.getSeat());
            setLastCard(nowDisCard);
            setDisposeCard(nowDisCard);
            setHuCard(nowDisCard);
            setPaoHu(0);
            isZiMo = true;
        } else {
        	isZiMo = isSelfMo(player);
          //  isZiMo = player.getSeat() == disCardSeat;

            if (player.getSeat() == paoHu) {
                //isPaoHu = true;
            }
        }
        if (isPaoHu) {
            int val = YzPhzCardUtils.loadCardVal(huCard);
            List<Integer> list = player.getHandCards().KAN.remove(val);
            if (list == null) {
                list = player.getHandCards().WEI.remove(val);
                if (list == null) {
                    list = player.getHandCards().PENG.remove(val);
                }
            }
            if (list != null) {
                list.add(huCard);
                player.getHandCards().PAO.put(val, list);
                hu = YzPhzHuPaiUtils.huPai(player.getHandCards(), 0, this, isZiMo, player.getUserId(), loadXingCard(false));
                LogUtil.msgLog.info("1paoqihu:tableId=" + getId() + ",userId=" + player.getUserId() + ",seat=" + player.getSeat() + ",list=" + list + ",hu=" + hu.isCanHu() + ",nowDisCard=" + nowDisCard);

            } else {
                hu = YzPhzHuPaiUtils.huPai(player.getHandCards(), huCard, this, isZiMo, player.getUserId(), loadXingCard(false));
                LogUtil.msgLog.info("2commonhu:tableId=" + getId() + ",userId=" + player.getUserId() + ",seat=" + player.getSeat() + ",huCard=" + huCard + ",hu=" + hu.isCanHu() + ",nowDisCard=" + nowDisCard);
            }


        } else {
            hu = YzPhzHuPaiUtils.huPai(player.getHandCards(), huCard, this, isZiMo, player.getUserId(), loadXingCard(false));

            boolean bug1flag = false;
            List<Integer> turncards = null;
            //BUG处理 2个以上的王牌 变红牌下可红胡情况下处理
//            if(hu.isCanHu() && red2Black==1 && player.getHandCards().WANGS.size()>=2 &&(player.getHandCards().WANGS.size()+hu.getRedCount()>=10) && !hu.getFans().contains(YzPhzFanEnums.HONG_HU) ){
//                YzPhzHandCards _temp =player.getHandCards().copy();
//
//                List<Integer> _ins = _temp.INS;
//                Set<Integer> _wangs =_temp.WANGS;
//                List<Integer> _remarkIns = new ArrayList<>(_ins);
//                List<Integer>  _wangsList = new ArrayList<>(_temp.WANGS);
//                List<Integer> _remarkWangs = new ArrayList<>(_wangsList);
//
//                int wangnum =_wangsList.size();
//
//                List<Integer> _list = new ArrayList<>(_temp.INS);
//                Map<Integer, Integer> _map = YzPhzCardUtils.loadCardCount(_list);//计算相同牌个数的map<val ,count>
//                Set<Integer> _tempRed =YzPhzCardUtils.getRedCards(_ins);
//                //map<val ,count>
//                int bestFan = 1;
//                YzPhzCardResult bestHuResult =null;
//                for (Map.Entry<Integer, Integer> kv : _map.entrySet()){
//                    if(kv.getKey()%10==2 ||kv.getKey()%10==7||kv.getKey()%10==10){
//                        // 红牌
//                        if(kv.getValue()<4){
//                            int restnum = 4 - kv.getValue();//可通过 王补充的癞子排数量
//                            //实际可补充的数量
//                            int laizipaiMaxNum = wangnum>=restnum?restnum:wangnum;
//                            for(int i=0;i<laizipaiMaxNum;i++){
//                                _ins = new ArrayList<>(_remarkIns);
//                                _wangsList =  new ArrayList<>(_remarkWangs);
//                                //写个方法 INS wangS 从wangs中抽取1个王变成X牌加入到INS中
//                              HashMap<String ,List<Integer>> result  =  YzPhzCardUtils.turnLaizi(_ins,_wangsList,kv.getKey(),i+1);
//                                _temp.INS.clear();
//                                _temp.WANGS.clear();
//                                 _temp.INS.addAll(result.get("handcards"));
//                                 _temp.WANGS.addAll(result.get("wangcards"));
////
//                                System.err.println("替换后手牌===");
//                                System.err.println(  _temp.INS);
//                                System.err.println(  _temp.WANGS);
//                                System.err.println("替换de pai");
//                                System.err.println(result.get("turncards"));
//
//                                YzPhzCardResult huresult =  YzPhzHuPaiUtils.huPai(_temp, huCard, this, isZiMo, player.getUserId(), loadXingCard(false));
//                                if(huresult.isCanHu()){
//                                    int totalfan =hu.getTotalFan();
////                                    for (YzPhzFanEnums r:huresult.getFans()  ) {
////                                        totalfan *=r.getFan();
////                                    }
//                                    if(totalfan>bestFan){
//                                        bug1flag = true;
//                                        bestFan = totalfan;
//                                        turncards = result.get("turncards");
//                                        bestHuResult = huresult;
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//
//                if(bug1flag && bestFan>=hu.getTotalFan()){
//                    List<YzPhzCardMessage> _cardMessageList  = bestHuResult.getCardMessageList();
//                    int i=0;
//                    for (YzPhzCardMessage msg : _cardMessageList) {
//                        for (Integer card : turncards) {
//                            if(msg.getCards().contains(card)){
//                                msg.getCards().remove(card);
//                                msg.getCards().add(_remarkWangs.get(i));
//                                i++;
//                            }
//                        }
//                    }
//                    bestHuResult.setReplaceCards(turncards);
//                    hu = bestHuResult;
//                }
//
//            }
//
            if (hu.isCanHu() &&paoHu==0) {
                LogUtil.msgLog.info("1commonhu:tableId=" + getId() + ",userId=" + player.getUserId() + ",seat=" + player.getSeat() + ",huCard=" + huCard + ",hu=" + hu.isCanHu() + ",nowDisCard=" + nowDisCard);

            } else {
                int val = YzPhzCardUtils.loadCardVal(huCard);
//                List<Integer> list = player.getHandCards().KAN.remove(val);
//                if (list == null) {
//                    list = player.getHandCards().WEI.remove(val);
//                    if (list == null) {
//                        list = player.getHandCards().PENG.remove(val);
//                    }
//                }
                List<Integer> list =   getPaoPaiList(player, val);
                if (list != null) {
                	int disc = disCardSeat;
                	 play(player, list,YzPhzConstants.action_pao , false, true,false);
                	 
                	 disCardSeat = disc;
//                    list.add(huCard);
//                    player.getHandCards().PAO.put(val, list);
                    hu = YzPhzHuPaiUtils.huPai(player.getHandCards(), 0, this, isZiMo, player.getUserId(), loadXingCard(false));
                    LogUtil.msgLog.info("2paoqihu:tableId=" + getId() + ",userId=" + player.getUserId() + ",seat=" + player.getSeat() + ",list=" + list + ",hu=" + hu.isCanHu() + ",nowDisCard=" + nowDisCard);

                }
            }
        }

        if (hu.isCanHu()) {
            player.setHuxi(hu.getTotalHuxi(false));
            if(isTianHu()) {
				// hu.addFan(YzPhzFanEnums.TIAN_HU);
				// hu.removeFan(YzPhzFanEnums.SELF);
				 hu.removeFan(YzPhzFanEnums.WANG_DIAO);
				 hu.removeFan(YzPhzFanEnums.WANG_ZHA);
				 hu.removeFan(YzPhzFanEnums.WANG_CHUANG);
				 hu.removeFan(YzPhzFanEnums.WANG_CHUANG_WANG);
				 hu.removeFan(YzPhzFanEnums.WANG_DIAO_WANG);
            }
            
//            else if(isDiHu()) {
//            	hu.addFan(YzPhzFanEnums.DI_HU);
//            }
            if (wangHu <= 0) {
                player.setHuResult(hu);
            } else if (wangHu == 1 && hu.getDiaoCardResult() != null) {
                player.setHuResult(hu.getDiaoCardResult());
            } else if (wangHu == 2 && hu.getChuangCardResult() != null) {
                player.setHuResult(hu.getChuangCardResult());
            } else if (wangHu == 3) {
                player.setHuResult(hu);
            } else {
                player.setHuResult(hu);
            }

            huConfirmList.add(player.getSeat());
            sendActionMsg(player, action, null, YzPhzConstants.action_type_action);
            if (diaoOrChuang) {
                clearAction();
                sendActionMsg(player, YzPhzConstants.action_mo, Arrays.asList(huCard), YzPhzConstants.action_type_mo);
            }
            calcOver();
        } else {
			broadMsg(player.getName() + " 不能胡牌");
			play(player, null, action_pass);
			
        }
        logActionMsg(player, nowDisCard, action, actionList);
    }
    
	private List<Integer> getPaoPaiList(YzPhzPlayer player, int val) {
		List<Integer> list = player.getHandCards().KAN.get(val);
		if (list == null) {
		    list = player.getHandCards().WEI.get(val);
		    if (list == null) {
		        list = player.getHandCards().PENG.get(val);
		    }
		}
		return list;
	}
    
    
    /**
	 * 是否自摸
	 *
	 * @param player
	 * @return
	 */
    public boolean isSelfMo(YzPhzPlayer player) {
//        if (moSeatPair != null) {
//            return moSeatPair.getValue() == player.getSeat();
//        }
        if (isMoFlag()) {
            return player.getSeat() == disCardSeat;
        }
        return false;
    }

    /**
	 * 提
	 *
	 * @param player
	 * @param cardList
	 * @param action
	 */
    private void ti(YzPhzPlayer player, List<Integer> cardList, Integer nowDisCard, int action, boolean moPai) {
		// cards肯定是4个相同的
        if (cardList == null) {
			// System.out.println("提不合法:" + cardList);
			player.writeErrMsg("提不合法:" + cardList);
            return;
        }
        int val = YzPhzCardUtils.loadCardVal(cardList.get(0));
        if (cardList.size() == 3) {

            List<Integer> tiCards = player.getHandCards().KAN.get(val);
            if (tiCards == null) {
                tiCards = player.getHandCards().WEI.get(val);
            }
            if (tiCards == null || tiCards.size() != 3) {
				// System.out.println("提不合法:" + tiCards);
				player.writeErrMsg("提不合法:" + tiCards);
                return;
            }
//			cardList.addAll(tiCards);
        } else {
            if (!player.getHandCards().INS.contains(cardList.get(0))) {
                return;
            }
        }

        boolean isZaiPao = player.getHandCards().WEI.containsKey(val);

        if (cardList.size() != 4 && !cardList.contains(nowDisCard)) {
            cardList.add(0, nowDisCard);
        }

        if (cardList.size() != 4) {
            return;
        }

        if (YzPhzCardUtils.loadIdsByVal(cardList, val).size() != 4) {
			// System.out.println("提不合法:" + cardList);
			player.writeErrMsg("提不合法:" + cardList);
            return;
        }
        addPlayLog(player.getSeat(), action + "", intList2Str(cardList));
        
        logActionMsg(player, nowDisCard, action, null);
        if (nowDisCard != null) {
            getDisPlayer().removeOutPais(nowDisCard);
        }
        setBeRemoveCard(nowDisCard);
        player.disCard(action, cardList);
        clearAction();
        setAutoDisBean(null);
        setDisCardSeat(player.getSeat());
        player.changeAction(YzPhzConstants.ACTION_COUNT_INDEX_TI, 1);
		// 检查是否能胡牌
        YzPhzCheckCardBean checkCard = player.checkCard(0, true, false, false, false, false, true);
        checkPaohuziCheckCard(checkCard);
        siShou(player);
        if (checkCard.isHu()) {
            setHuCard(0);
            setDisposeCard(0);
//            weiOrPao = cardList.get(0);
        }

		// 是否能出牌
        setDisPlayer(player, action, checkCard.isHu());
        player.setPassHuVal(0,0);
        boolean mo = actionSeatMap.isEmpty() && toPlayCardFlag == 0;

        sendActionMsg(player, action, cardList, YzPhzConstants.action_type_action, isZaiPao, false, -1);

        if (mo) {
            checkMo();
        }
    }

    private boolean siShou(YzPhzPlayer player) {
        YzPhzHandCards handCards = player.getHandCards();
        if (handCards.INS.size() == 0 && handCards.WANGS.size() > 0) {
            if (handCards.hasFourSameCard() && handCards.WANGS.size() == 1) {
            	
            } else if (!handCards.hasFourSameCard() && handCards.WANGS.size() == 2) {
            } else {
                player.setCanHuState(0);
                return true;
            }
        }
        return false;
    }

    /**
	 * 栽
	 *
	 * @param player
	 * @param cardList
	 * @param nowDisCard
	 * @param action
	 */
    private void zai(YzPhzPlayer player, List<Integer> cardList, Integer nowDisCard, int action) {
        if (!actionSeatMap.containsKey(player.getSeat())) {
//			System.out.println(actionSeatMap);
            return;
        }

        getDisPlayer().removeOutPais(nowDisCard);
        if (!cardList.contains(nowDisCard)) {
            cardList.add(0, nowDisCard);
        }
        setBeRemoveCard(nowDisCard);
        if (action == YzPhzConstants.action_zai) {
            setZaiCard(nowDisCard);
        }
        addPlayLog(player.getSeat(), action + "", intList2Str(cardList));
        logActionMsg(player, nowDisCard, action, null);
        player.disCard(action, cardList);
        clearAction();
        setAutoDisBean(null);

        
        player.setPassHuVal(0,0);
		// 检查是否能胡牌
        YzPhzCheckCardBean checkCard = player.checkCard(0, true, false, false, false, false, true);

        checkPaohuziCheckCard(checkCard);

        siShou(player);
		// 是否能出牌
        setDisPlayer(player, action, checkCard.isHu());
       
        if (checkCard.isHu()) {
            setHuCard(0);
            setDisposeCard(0);
//			sendActionMsg(player, YzPhzConstants.action_hu, new ArrayList<Integer>(), YzPhzConstants.action_type_mo);
            sendActionMsg(player, action, cardList, YzPhzConstants.action_type_action);
        } else {
            sendActionMsg(player, action, cardList, YzPhzConstants.action_type_action);
            boolean mo = actionSeatMap.isEmpty() && toPlayCardFlag == 0;
            if (mo) {
                checkMo();
            }
        }

    }


    /**
	 * 跑
	 *
	 * @param player
	 * @param cardList
	 * @param nowDisCard
	 * @param action
	 */
    private void pao(YzPhzPlayer player, List<Integer> cardList, Integer nowDisCard, int action, boolean isHu, boolean isPassHu) {
        if (cardList.size() != 3) {
			broadMsg("跑的张数不对:" + cardList);
            return;
        }
        List<Integer> actionList = actionSeatMap.get(player.getSeat());
        if (actionList == null) {
            return;
        }
        //可以胡如果不是点胡不能跑
        if (actionList.get(0) == 1&&!isHu) {
        	return;
        }
        saveActionSeatMap();
        
        if (!isHu &&recallAction(player.getSeat(), action, cardList)) {
            return;
        }
        
        if (!isHu &&!checkAction(player, action)) {
//          LogUtil.msgLog.info(" checkAction" + tempAction);
			// 更新前台数据
      	player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip);
          //sendPlayerActionMsg(player, true, null, action_type_clear, action_refreshaction);
          return;
      }


//        if (!isHu && actionList.get(5).intValue() != 1 && autoDisBean == null) {
//            return;
//        }

        boolean isZaiPao = player.isZaiPao(YzPhzCardUtils.loadCardVal(cardList.get(0)));
        getDisPlayer().removeOutPais(nowDisCard);
        if (!cardList.contains(nowDisCard)) {
            cardList.add(0, nowDisCard);
        }
        setBeRemoveCard(nowDisCard);

        if (cardList.size() == 1) {
			// 如果是一张牌说明已经在出的牌里面了
//			List<Integer> list = YzPhzCardUtils.loadSameValCards(player.getHandCards().INS,nowDisCard);
//			cardList.addAll(list);
        }

        if (cardList.size() != 4) {
            return;
        }
        logActionMsg(player, nowDisCard, action, actionList);
        addPlayLog(player.getSeat(), action + "", intList2Str(cardList));
        player.disCard(action, cardList);
        clearAction();
        saveActionSeatMap();

        setAutoDisBean(null);

        siShou(player);

        
        player.changeAction(YzPhzConstants.ACTION_COUNT_INDEX_PAO, 1);

        boolean disCard = setDisPlayer(player, action, isHu);

        boolean mo = actionSeatMap.isEmpty() && toPlayCardFlag == 0&&!isHu;;
        
		// 检查是否能胡牌
        siShou(player);

//        YzPhzCheckCardBean checkCard = player.checkCard(0, isSelfMo(player), false, false, false, false, true);
//        checkPaohuziCheckCard(checkCard);
//
//        if (checkCard.isHu()) {
//            setHuCard(0);
//            setDisposeCard(0);
////            weiOrPao = cardList.get(0);
//        }
        
		// 是否能出牌
        if (!isHu) {
            sendActionMsg(player, action, cardList, YzPhzConstants.action_type_action, isZaiPao, !disCard, -1);
        } else {
            sendActionMsg(player, action, cardList, YzPhzConstants.action_type_action, isZaiPao, false, -1);
        }

        

//        if (checkCard.isHu()) {
//            if((huBiHu==2&&player.getHandCards().WANGS.size()==0) ||(huBiHu==1&&!isMoFlag())) {
//            	toPlayCardFlag = 0;
//				checkCard.setAuto(YzPhzConstants.action_hu, new ArrayList<>());
//				setAutoDisBean(checkCard);
//				playAutoDisCard(checkCard);
//				setPaoHu(player.getSeat());
//				setHuCard(nowDisCard);
//				return;
//            }
////            weiOrPao = cardList.get(0);
//        }
        setDisCardSeat(player.getSeat());
        if (mo) {
            checkMo();
        }

    }

    /**
	 * 出牌
	 *
	 * @param player
	 * @param cardList
	 * @param action
	 */
    private void disCard(YzPhzPlayer player, List<Integer> cardList, int action) {
        if (!actionSeatMap.isEmpty()) {
			// player.writeErrMsg("动作:" +-
            // JacksonUtil.writeValueAsString(actionSeatMap));
            List<Integer> actionList;
            if (actionSeatMap.size() == 1 && (actionList = actionSeatMap.get(calcNextSeat(player.getSeat()))) != null) {
                if (actionList.get(7).intValue() == 1 || actionList.get(8).intValue() == 1 || actionList.get(9).intValue() == 1) {

                } else {
					LogUtil.e("动作:" + JacksonUtil.writeValueAsString(actionSeatMap));
                    return;
                }
            } else {
				LogUtil.e("动作:" + JacksonUtil.writeValueAsString(actionSeatMap));
                return;
            }
        }

        if (toPlayCardFlag != 1) {
			// player.writeErrMsg(player.getName() + "错误 toPlayCardFlag:" +
			// toPlayCardFlag + "出牌");
			LogUtil.e(player.getName() + "错误 toPlayCardFlag:" + toPlayCardFlag + "出牌");
//			checkMo();
            return;
        }

        if (player.getSeat() != nowDisCardSeat) {
			player.writeErrMsg("轮到:" + nowDisCardSeat + "出牌");
            return;
        }
        if (cardList.size() != 1) {
			player.writeErrMsg("出牌数量不对:" + cardList);
            return;
        }

        if (!player.getHandCards().INS.contains(cardList.get(0))) {
			player.writeErrMsg("没有这张牌:" + cardList);
            return;
        }

        userActionMap.clear();
        changeExtend();

        changeDisCount(1);
        setPaoHu(0);
        setLastCard(cardList.get(0));
        setDisposeCard(cardList.get(0));

        addPlayLog(player.getSeat(), action + "", intList2Str(cardList));
        logActionMsg(player, cardList.get(0), action, null);
		// checkFreePlayerTi(player, action);// 检查闲家提
        player.disCard(action, cardList);
        setMoFlag(0);

        LogUtil.msgLog.info("chupai from player:userId=" + player.getUserId() + ",seat=" + player.getSeat() + ",card=" + cardList);

        markMoSeat(player.getSeat(), action);
        clearMoSeatPair();
        clearLastSeatAction();
        setToPlayCardFlag(0);
        setDisCardSeat(player.getSeat());
        setNowDisCardIds(cardList);
        setNowDisCardSeat(getNextDisCardSeat());
        YzPhzCheckCardBean autoDisCard = checkDisAction(player, action, cardList.get(0), true);
//		int canPaoSeat=0;
//        if (autoDisCard == null) {
//
//            for (Entry<Integer, YzPhzPlayer> entry : seatMap.entrySet()) {
//                if (entry.getKey().intValue() == player.getSeat()) {
//                    continue;
//                }
//                YzPhzCheckCardBean rets = entry.getValue().checkPaoHu(cardList.get(0), false, false);
//
//                if (rets.isPao()) {
//                    clearAction();
//                    break;
//                }
//            }
//
//        }
        
        YzPhzPlayer nexPlayer =	seatMap.get(calcNextSeat(player.getSeat()));
        int val = YzPhzCardUtils.loadCardVal(cardList.get(0));
   	 	checkSchi(player, nexPlayer, true, val);
        

        sendActionMsg(player, action, cardList, YzPhzConstants.action_type_dis);
        if (autoDisCard != null) {
			// 系统自动出牌
            playAutoDisCard(autoDisCard);
        } else {
            if (actionSeatMap != null && actionSeatMap.size() > 0) {
//                checkSendActionMsg();
            } else {
                checkMo();
            }
        }
    }

    /**
	 * 碰
	 *
	 * @param player
	 * @param cardList
	 * @param nowDisCard
	 * @param action
	 */
    private void peng(YzPhzPlayer player, List<Integer> cardList, Integer nowDisCard, int action) {
        List<Integer> actionList = actionSeatMap.remove(player.getSeat());
        if (actionList == null) {
            return;
        }
        saveActionSeatMap();

        if (recallAction(player.getSeat(), action, cardList)) {
            return;
        }

        if (!checkAction(player, action)) {
            // player.writeErrMsg(LangMsg.code_29);
            if (actionList.get(0).intValue() == 1) {
                LogUtil.monitorLog.info("can hu xuanze peng:tableId=" + getId() + ",userId=" + player.getUserId() + ",cardVal=" + YzPhzCardUtils.loadCardVal(nowDisCard) + ",card=" + nowDisCard);

//                actionList.set(0, 0);

//                addAction(player.getSeat(), actionList);
            }
			// 更新前台数据
            player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip);
           // sendPlayerActionMsg(player, true, null, action_type_clear, action_refreshaction);
            return;
        }

        int val = YzPhzCardUtils.loadCardVal(nowDisCard);
        cardList = YzPhzCardUtils.loadIdsByVal(player.getHandCards().INS, val);
        if (cardList == null || cardList.size() != 2) {
			player.writeErrMsg("不能碰");
            return;
        }
        if (!cardList.contains(nowDisCard)) {
            cardList.add(0, nowDisCard);
        }
        setBeRemoveCard(nowDisCard);

        getDisPlayer().removeOutPais(nowDisCard);
        addPlayLog(player.getSeat(), action + "", intList2Str(cardList));
        logActionMsg(player, nowDisCard, action, actionList);
        player.disCard(action, cardList);
        clearAction();

//        for (YzPhzPlayer seatPlayer : seatMap.values()) {
//            if (seatPlayer.getSeat() == player.getSeat()) {
//                continue;
//            }
//
//            int seat1 = disCardSeat;
//            int seat2 = getNextDisCardSeat();
//
//            if ((seatPlayer.getSeat() == seat1 || seatPlayer.getSeat() == seat2) && seatPlayer.canChi(nowDisCard)) {
//                if (isMoFlag()) {
//                    seatPlayer.removePassChi(val);
//                } else {
//                    if (seatPlayer.getSeat() == seat2) {
//                        seatPlayer.removePassChi(val);
//                    }
//                }
//            }
//        }

        player.setPassHuVal(0,0);
        setDisPlayer(player, action, false);

		// 碰的情况,把所有玩家的过牌去掉

        boolean mo = actionSeatMap.isEmpty() && toPlayCardFlag == 0;

        sendActionMsg(player, action, cardList, YzPhzConstants.action_type_action);

        if (mo) {
            checkMo();
        }

    }

    /**
	 * 过
	 *
	 * @param player
	 * @param cardList
	 * @param nowDisCard
	 * @param action
	 */
    private void pass(YzPhzPlayer player, List<Integer> cardList, Integer nowDisCard, int action) {
        List<Integer> actionList = actionSeatMap.remove(player.getSeat());
        if (actionList == null) {
            return;
        }
        saveActionSeatMap();

        if (actionList.get(1).intValue() == 1) {
            int val = YzPhzCardUtils.loadCardVal(lastCard);
            player.passVal(action_peng, val);
            LogUtil.monitorLog.info("action pass peng:userId=" + player.getUserId() + ",tableId=" + getId() + ",cardVal=" + val);
        }

        List<Integer> list = YzPhzConstants.parseToDisActionList(actionList, true);
        if (list.contains(YzPhzConstants.action_zai) || list.contains(YzPhzConstants.action_ti) || list.contains(YzPhzConstants.action_chouzai)) {
            return;
        }
        if (!list.contains(YzPhzConstants.action_chi) && !list.contains(YzPhzConstants.action_peng) && !list.contains(action_hu)
                && !list.contains(YzPhzConstants.action_wangdiao)
                && !list.contains(YzPhzConstants.action_wangchuang)
                && !list.contains(YzPhzConstants.action_wangzha)) {
            return;
        }

        boolean wangDiaoOrChuang = false;
        if (list.contains(YzPhzConstants.action_wangdiao) || list.contains(YzPhzConstants.action_wangchuang)
                || list.contains(YzPhzConstants.action_wangzha)) {
            wangDiaoOrChuang = true;
        }

		// 可以胡牌，然后点了过
        boolean isPassHu = actionList.get(0).intValue() == 1;
        if (actionList.get(0).intValue() == 1 && player.getHandCards().INS.isEmpty() && player.getHandCards().WANGS.isEmpty()) {
			player.writeErrMsg("手上已经没有牌了");
            return; 
        }

        if (isTianHu()) {
            if (isPassHu) {
                LogUtil.msgLog.info("common tianhu pass:tableId=" + getId() + ",userId=" + player.getUserId() + ",actions=" + actionList);
            }
            if (toPlayCardFlag == 1) {
                sendActionMsg(player, action, cardList, YzPhzConstants.action_type_action);
            } else {
                checkMo();
            }
            return;
        }

        if(action==YzPhzConstants.action_pass){
            int logId;
            if(paoHu>0){
                logId=0;
            }else {
                logId = nowDisCard;
            }
            addPlayLog(player.getSeat(), YzPhzConstants.action_guo + "",logId+"");
            setPaoHu(0);
        }

        int val = 0;
        if (nowDisCard != null) {
            val = YzPhzCardUtils.loadCardVal(nowDisCard);

            if (actionList.get(1).intValue() == 1) {
                player.passVal(action_peng, val);
                LogUtil.monitorLog.info("action pass peng:userId=" + player.getUserId() + ",tableId=" + getId() + ",cardVal=" + val);
            }
            if (isPassHu&&player.getHandCards().WANGS.size()==0) {
            	player.setPassHuVal(val,player.getSeat()==moSeat?1:0);
            }
        }

        if (isPassHu) {
            LogUtil.msgLog.info("common hu pass:tableId=" + getId() + ",userId=" + player.getUserId() + ",actions=" + actionList + ",cardVal=" + val);
        }



        boolean checkPao = true;
        for (Entry<Integer, List<Integer>> kv : actionSeatMap.entrySet()) {
            if (kv.getValue().get(0).intValue() == 1) {
                checkPao = false;
                break;
            }
        }

        if (checkPao) {
            for (Entry<Integer, YzPhzPlayer> kv : seatMap.entrySet()) {
                YzPhzCheckCardBean rets = kv.getValue().checkPaoHu(nowDisCard, disCardSeat == kv.getKey().intValue(), false);
                if (rets.isPao()) {
                    List<Integer> list1 = kv.getValue().getHandCards().KAN.get(val);
                    if (list1 == null) {
                        list1 = kv.getValue().getHandCards().WEI.get(val);
                        if (list1 == null) {
                            list1 = kv.getValue().getHandCards().PENG.get(val);
                        }
                    }

                    YzPhzCheckCardBean pccb = new YzPhzCheckCardBean();
                    pccb.setSeat(kv.getKey());
                    pccb.setDisCard(nowDisCard);
                    pccb.setPao(true);
                    pccb.setAuto(YzPhzConstants.action_pao, new ArrayList<>(list1));
                    pccb.buildActionList();
                    checkPaohuziCheckCard(pccb);
                    setAutoDisBean(pccb);

                    playAutoDisCard(pccb);
                    return;
                }
            }
        }
        
        if (recallAction(player.getSeat(), action, Arrays.asList(nowDisCard == null ? 0 : nowDisCard, list.contains(YzPhzConstants.action_peng) ? 1 : 0, list.contains(YzPhzConstants.action_chi) ? 1 : 0))) {
            return;
        }

        if (toPlayCardFlag == 1) {
            sendActionMsg(player, action, cardList, YzPhzConstants.action_type_action);
        } else {
			// 自动出牌
            if (wangDiaoOrChuang) {

                LogUtil.msgLog.info("wang hu pass:tableId=" + getId() + ",userId=" + player.getUserId() + ",actions=" + actionList + ",cardVal=" + val);

                if (actionSeatMap.isEmpty()) {
                    setNowDisCardSeat(player.getSeat());
                    if (isMoFlag()) {
                        seatMap.get(moSeat).outCardOfMo(lastCard, this);
                    }
                    for (Entry<Integer, String> kv : userActionMap.entrySet()) {
                        String[] strs = kv.getValue().split("_");
                        if (strs.length == 4 && action_pass == Integer.parseInt(strs[0])) {
                            if (Integer.parseInt(strs[2]) == 1) {
                                int cardVal = YzPhzCardUtils.loadCardVal(Integer.parseInt(strs[1]));
                                seatMap.get(kv.getKey()).passVal(action_peng, cardVal);
                                LogUtil.monitorLog.info("action pass peng:userId=" + seatMap.get(kv.getKey()).getUserId() + ",tableId=" + getId() + ",cardVal=" + cardVal);
                            }
                            if (Integer.parseInt(strs[3]) == 1) {
                                int cardVal = YzPhzCardUtils.loadCardVal(Integer.parseInt(strs[1]));
                                seatMap.get(kv.getKey()).passVal(action_chi, cardVal);
                                LogUtil.monitorLog.info("action pass chi:userId=" + seatMap.get(kv.getKey()).getUserId() + ",tableId=" + getId() + ",cardVal=" + cardVal);
                            }
                        }
                    }
                    checkMo(true);
                } else {
//                    sendPlayerActionMsg(player, true);
                    sendPlayerActionMsg(player, true, cardList, YzPhzConstants.action_type_action, action);
                }

//				sendMoMsg(player, action, cardList, YzPhzConstants.action_type_action); 
//&&!checkHuPlayer()
            } else if (autoDisBean != null) {
                sendActionMsg(player, action, cardList, YzPhzConstants.action_type_action);
                List<Integer> list2 = autoDisBean.getActionList();
                if (list2 !=null&&!list2.isEmpty()) {
                	
                	List<Integer> list3= actionSeatMap.get(autoDisBean.getSeat());
                	if(list3!=null && !list3.isEmpty()){
                		if(list2.size()>5&&list2.get(5)==1){
                			list3.set(5, 1);
                		}
                	}else{
                		addAction(autoDisBean.getSeat(), list2);
                	}
                }
                playAutoDisCard(autoDisBean);
             
            } else {
                if (actionSeatMap.isEmpty()) {
                    if (isMoFlag()) {
                        seatMap.get(moSeat).outCardOfMo(lastCard, this);
                        
                        
                        
                        
                    }
                    for (Entry<Integer, String> kv : userActionMap.entrySet()) {
                        String[] strs = kv.getValue().split("_");
                        if (strs.length == 4 && action_pass == Integer.parseInt(strs[0])) {
                            if (Integer.parseInt(strs[2]) == 1) {
                                int cardVal = YzPhzCardUtils.loadCardVal(Integer.parseInt(strs[1]));
                                seatMap.get(kv.getKey()).passVal(action_peng, cardVal);
                                LogUtil.monitorLog.info("action pass peng:userId=" + seatMap.get(kv.getKey()).getUserId() + ",tableId=" + getId() + ",cardVal=" + cardVal);
                            }
                            if (Integer.parseInt(strs[3]) == 1) {
                                int cardVal = YzPhzCardUtils.loadCardVal(Integer.parseInt(strs[1]));
                                seatMap.get(kv.getKey()).passVal(action_chi, cardVal);
                                LogUtil.monitorLog.info("action pass chi:userId=" + seatMap.get(kv.getKey()).getUserId() + ",tableId=" + getId() + ",cardVal=" + cardVal);
                            }
                        }
                    }
                    
                    if(moSeat==player.getSeat()&&isMoFlag()){
                    	YzPhzPlayer nexPlayer =	seatMap.get(calcNextSeat(player.getSeat()));
                    	 checkSchi(player, nexPlayer, true, val);
                    }
                    
                    sendActionMsg(player, action, cardList, YzPhzConstants.action_type_action);
                    checkMo();
                } else {
                    sendPlayerActionMsg(player, true, cardList, YzPhzConstants.action_type_action, action);
                }
            }
        }

    }

    /**
	 * 吃
	 *
	 * @param player
	 * @param cardList0
	 * @param nowDisCard
	 * @param action
	 */
    private void chi(YzPhzPlayer player, List<Integer> cardList0, Integer nowDisCard, int action) {
    	 List<Integer> actionList = actionSeatMap.get(player.getSeat());
    	 if (actionList == null) {
             return;
         }
		List<Integer> cardList = null;
		if (cardList0 != null) {
			cardList = new ArrayList<>(cardList0);
			if (cardList.size() % 3 != 0) {
				player.writeErrMsg("不能吃" + cardList);
				return;
			}

			if (!cardList.contains(nowDisCard)) {
				return;
			}

			if (!checkChiXiaHuo(player, cardList0, nowDisCard, cardList)) {
				return;
			}

		}
       
        actionSeatMap.remove(player.getSeat());
        saveActionSeatMap();

        if (recallAction(player.getSeat(), action, cardList0)) {
            return;
        }
        if (sendPaoSeat > 0) {
            if (actionList.get(0).intValue() == 1) {
                sendPlayerActionMsg(player, true);
            }

            LogUtil.monitorLog.info("can hu xuanze chi:tableId=" + getId() + ",userId=" + player.getUserId() + ",cardVal=" + YzPhzCardUtils.loadCardVal(nowDisCard) + ",card=" + nowDisCard);

            checkMo();
            return;
        }

        if (!checkAction(player, action)) {
			// 能吃能碰的情况下
            if (actionList.get(1).intValue() == 1 || actionList.get(0).intValue() == 1) {

                LogUtil.monitorLog.info("can hu or peng xuanze chi:tableId=" + getId() + ",userId=" + player.getUserId() + ",cardVal=" + YzPhzCardUtils.loadCardVal(nowDisCard) + ",card=" + nowDisCard);

                if (actionList.get(1).intValue() == 1) {
                    actionList.set(1, 0);
					// // 选择了吃，那不能碰了
                    player.passVal(YzPhzConstants.action_peng, YzPhzCardUtils.loadCardVal(nowDisCard));
                }
                if (actionList.get(0).intValue() == 1) {
                    actionList.set(0, 0);

                    boolean checkPao = true;
                    for (Entry<Integer, List<Integer>> kv : actionSeatMap.entrySet()) {
                        if (kv.getValue().get(0).intValue() == 1) {
                            checkPao = false;
                            break;
                        }
                    }

                    if (checkPao) {
                        for (Entry<Integer, YzPhzPlayer> kv : seatMap.entrySet()) {
                            YzPhzCheckCardBean rets = kv.getValue().checkPaoHu(nowDisCard, disCardSeat == kv.getKey().intValue(), false);
                            if (rets.isPao()) {
                                Integer val = YzPhzCardUtils.loadCardVal(nowDisCard);
                                List<Integer> list = kv.getValue().getHandCards().KAN.get(val);
                                if (list == null) {
                                    list = kv.getValue().getHandCards().WEI.get(val);
                                    if (list == null) {
                                        list = kv.getValue().getHandCards().PENG.get(val);
                                    }
                                }

                                YzPhzCheckCardBean pccb = new YzPhzCheckCardBean();
                                pccb.setSeat(kv.getKey());
                                pccb.setDisCard(nowDisCard);
                                pccb.setPao(true);
                                pccb.setAuto(YzPhzConstants.action_pao, new ArrayList<>(list));
                                pccb.buildActionList();
                                checkPaohuziCheckCard(pccb);
                                setAutoDisBean(pccb);

                                playAutoDisCard(pccb);
                                return;
                            }
                        }
                    }
                }
            }
			// 更新前台数据
            player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip);
         //   sendPlayerActionMsg(player, true, null, action_type_clear, action_refreshaction);
            return;
        }

        if (!cardList.contains(nowDisCard)) {
            cardList.add(0, nowDisCard);
        } else {
            cardList.remove(nowDisCard);
            cardList.add(0, nowDisCard);
        }
        player.setPassHuVal(0,0);
        setBeRemoveCard(nowDisCard);
        getDisPlayer().removeOutPais(nowDisCard);

        int val = YzPhzCardUtils.loadCardVal(nowDisCard);
        for (YzPhzPlayer player1 : playerMap.values()) {
            player1.passVal(action_peng, val);
        }

        for (Entry<Integer, String> kv : userActionMap.entrySet()) {
            if (kv.getKey().intValue() == player.getSeat()) {
                continue;
            }
            String[] strs = kv.getValue().split("_");
            if (strs.length == 4 && action_pass == Integer.parseInt(strs[0])) {
                if (Integer.parseInt(strs[2]) == 1) {
                    int cardVal = YzPhzCardUtils.loadCardVal(Integer.parseInt(strs[1]));
                    seatMap.get(kv.getKey()).passVal(action_peng, cardVal);
                    LogUtil.monitorLog.info("action pass peng:userId=" + seatMap.get(kv.getKey()).getUserId() + ",tableId=" + getId() + ",cardVal=" + cardVal);
                }
                if (Integer.parseInt(strs[3]) == 1 && kv.getKey().intValue() == disCardSeat) {
                    int cardVal = YzPhzCardUtils.loadCardVal(Integer.parseInt(strs[1]));
                    seatMap.get(kv.getKey()).passVal(action_chi, cardVal);
                    LogUtil.monitorLog.info("action pass chi:userId=" + seatMap.get(kv.getKey()).getUserId() + ",tableId=" + getId() + ",cardVal=" + cardVal);
                }
            }
        }
        logActionMsg(player, nowDisCard, action, actionList);
        addPlayLog(player.getSeat(), action + "", intList2Str(cardList));
        player.disCard(action, cardList);
        clearAction();

        setDisPlayer(player, action, false);

        boolean mo = actionSeatMap.isEmpty() && toPlayCardFlag == 0;

        sendActionMsg(player, action, cardList, YzPhzConstants.action_type_action);
        if (mo) {
            checkMo();
        }

    }

	private boolean checkChiXiaHuo(YzPhzPlayer player, List<Integer> cardList0, Integer nowDisCard,
			List<Integer> cardList) {
		List<Integer>  hands =  new ArrayList<>(player.getHandCards().INS);
//		if(!hands.contains(nowDisCard)){
//			return true;
//		}
		hands.removeAll(cardList0);
		GuihzCard card = GuihzCard.getPaohzCard(nowDisCard);
		for(Integer id:hands){
			 GuihzCard card2 = GuihzCard.getPaohzCard(id);
			 if(card2.getVal()==card.getVal() ){
				 player.writeErrMsg("不能吃" + cardList);
		         return false;
			 }
		}
		return true;
	}

    public synchronized void play(YzPhzPlayer player, List<Integer> cardIds, int action, boolean moPai, boolean isHu, boolean isPassHu) {
        if (state != SharedConstants.table_state.play) {
            return;
        }
        Integer nowDisCard = null;
        List<Integer> cardList = null;
        if (action != YzPhzConstants.action_mo) {
//            if (nowDisCardIds != null && nowDisCardIds.size() == 1) {
//                nowDisCard = nowDisCardIds.get(0);
//            }
            nowDisCard = lastCard;
            if (action != YzPhzConstants.action_pass) {
                if (!player.isCanDisCard(cardIds, nowDisCard, action)) {
                    return;
                }
            }
            if (cardIds != null && !cardIds.isEmpty()) {
                cardList = cardIds;
            }
        }

//		LogUtil.msgLog.info(player.getUserId()+",playerSeat:"+player.getSeat()+",moseat:"+moSeat+",currentSeat:"+disCardSeat+",next:"+nowDisCardSeat + ",action:" + action + ",nowDisCard:" +nowDisCard+ " -" + cardIds+",moPai:"+moPai+".isHu:"+isHu+",isPassHu:"+isPassHu);

        player.setAction(action);
        if (action == YzPhzConstants.action_ti) {
            if (cardList.size() > 4) {
                for (List<Integer> tiCards : player.getHandCards().TI.values()) {
                    ti(player, new ArrayList<>(tiCards), nowDisCard, action, moPai);
                }
            } else {
                ti(player, cardList, nowDisCard, action, moPai);
            }
        } else if (action == action_hu || action == YzPhzConstants.action_wangdiao || action == YzPhzConstants.action_wangchuang
                || action == YzPhzConstants.action_wangzha) {
            hu(player, cardList, action, nowDisCard);
        } else if (action == YzPhzConstants.action_peng) {
            peng(player, cardList, nowDisCard, action);
        } else if (action == YzPhzConstants.action_chi) {
            chi(player, cardList, nowDisCard, action);
        } else if (action == YzPhzConstants.action_pass) {
            pass(player, cardList, nowDisCard, action);
        } else if (action == YzPhzConstants.action_pao) {
            pao(player, cardList, nowDisCard, action, isHu, isPassHu);
        } else if (action == YzPhzConstants.action_zai || action == YzPhzConstants.action_chouzai) {
            zai(player, cardList, nowDisCard, action);
        } else if (action == YzPhzConstants.action_mo) {
//            if (checkMoMark != null) {
//                int cAction = cardIds.get(0);
//                if (checkMoMark.getId() == player.getSeat() && checkMoMark.getValue() == cAction) {
////					checkMo();
//                } else {
//                }
//            }

        } else {
            disCard(player, cardList, action);
        }
        
        sendTingInfo(player);

    }

    /**
	 * 设置要出牌的玩家
	 *
	 * @param player
	 */
    private boolean setDisPlayer(YzPhzPlayer player, int action, boolean isHu) {
        if (this.leftCards.isEmpty()) {
			// 手上已经没有牌了
            if (!isHu) {
                calcOver();
            }
            return false;
        }

        markLastSeatAction(player.getSeat(), action);
        if (/**((isFirstDis&&player.getSeat() == lastWinSeat) || **/player.isNeedDisCard(action)) {
            if (player.getHandCards().INS.isEmpty()) {
                player.setCanHuState(0);

				// 不需要出牌 下一家直接摸牌
                setToPlayCardFlag(0);
                player.compensateCard();
                int next = calcNextSeat(player.getSeat());
                setNowDisCardSeat(next);

                if (actionSeatMap.isEmpty()) {
                    markMoSeat(player.getSeat(), action);
                }
                return false;
            } else {
                setNowDisCardSeat(player.getSeat());
                setToPlayCardFlag(1);
                return true;
            }
        } else {
			// 不需要出牌 下一家直接摸牌
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
	 * 检查优先度，胡杠补碰吃 如果同时出现一个事件，按出牌座位顺序优先
	 *
	 * @param player
	 * @param action
	 * @return
	 */
    public boolean checkAction(YzPhzPlayer player, int action) {
		// 优先度为胡杠补碰吃
        List<Integer> stopActionList = YzPhzConstants.findPriorityAction(action);
        for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
            if (player.getSeat() != entry.getKey()) {
				// 别人
                boolean can = YzPhzConstants.canDis(stopActionList, entry.getValue(), disCardSeat == player.getSeat());
                if (!can) {
                    return false;
                }
                List<Integer> disActionList = YzPhzConstants.parseToDisActionList(entry.getValue(), disCardSeat == player.getSeat());
                if (disActionList.contains(action)) {
					// 同时拥有同一个事件 根据座位号来判断
                    int actionSeat = entry.getKey();
                    int nearSeat = getNearSeat(disCardSeat, Arrays.asList(player.getSeat(), actionSeat));
                    if (nearSeat != player.getSeat()) {
                        return false;
                    }

                }

            }

        }
        return true;
    }

    private YzPhzPlayer getDisPlayer() {
        return seatMap.get(disCardSeat);
    }

    @Override
    public int isCanPlay() {
        if (getPlayerCount() < getMaxPlayerCount()) {
            return 1;
        }
        return 0;
    }

    private void checkMo() {
        checkMo(false);
    }


    private void checkMo(boolean wangDiaoOrChuang) {
        synchronized (this) {
			// 0胡 1碰 2栽 3提 4吃 5跑
            if (actionSeatMap.size() > 0 || toPlayCardFlag != 0) {
                LogUtil.monitorLog.warn("can not get next card:tableId=" + getId() + ",current=" + playBureau + ",actions=" + actionSeatMap + ",nowSeat=" + nowDisCardSeat + ",toPlayCardFlag=" + toPlayCardFlag + ",lastCard=" + lastCard);
                return;
            }

            lastSeatAction = null;
            userActionMap.clear();
            changeExtend();

            int canHu = -1;
            YzPhzCardResult cardResult = null;
//            if (!isTianHu())
//                if (wangDiaoOrChuang) {
//                    canHu = 1;
//                    for (Entry<Integer, YzPhzPlayer> entry : seatMap.entrySet()) {
//                        YzPhzCheckCardBean rets = entry.getValue().checkPaoHu(disposeCard, entry.getKey().intValue() == disCardSeat, false);
//                        if (rets.isPao()) {
//                            Integer val = YzPhzCardUtils.loadCardVal(disposeCard);
//                            List<Integer> list = entry.getValue().getHandCards().KAN.get(val);
//                            if (list == null) {
//                                list = entry.getValue().getHandCards().WEI.get(val);
//                                if (list == null) {
//                                    list = entry.getValue().getHandCards().PENG.get(val);
//                                }
//                            }
//
//                            YzPhzCheckCardBean pccb = new YzPhzCheckCardBean();
//                            setPaoCard(entry, list, pccb);
//                            break;
//                        }
//                    }
//                } else {
//                    YzPhzCheckCardBean pccb = null;
//                    int tempCard = getNextLeftCard();
//                    for (Entry<Integer, YzPhzPlayer> entry : seatMap.entrySet()) {
//                    //	boolean zimo = isSelfMo(entry.getValue());
//                        YzPhzCheckCardBean rets = entry.getValue().checkPaoHu(tempCard, entry.getKey().intValue() == nowDisCardSeat, false);
//                        //entry.getKey().intValue() == disCardSeat
//                        if (rets.isPao()) {
//                            Integer val = YzPhzCardUtils.loadCardVal(tempCard);
//                            List<Integer> list = entry.getValue().getHandCards().KAN.get(val);
//                            if (list == null) {
//                                list = entry.getValue().getHandCards().WEI.get(val);
//                                if (list == null) {
//                                    list = entry.getValue().getHandCards().PENG.get(val);
//                                }
//                            }
//
//                            pccb = new YzPhzCheckCardBean();
//                            setPaoCard(entry, list, pccb);
//                            break;
//                        }
//                    }
//
                    if (autoDisBean == null) {
                        if (leftCards == null) {
                            return;
                        }
                        if (this.leftCards != null && this.leftCards.size() == 0) {
                            calcOver();
                            return;
                        }
                        int tempCard = getNextLeftCard();
                            YzPhzPlayer player1 = seatMap.get(nowDisCardSeat);
                            if (tempCard > 80) {
                             	if(player1.getHandCards().WANGS.size()>=3){
                             		if(getLeftCards().size()>1){
                             			tempCard = getLeftCards().get(1);
                             		}
                             	}
                             }
                            
                            
                            if (player1.getHandCards().WANGS.size() > 0&&!wangDiaoOrChuang) {
                              //  int tempCard = getNextLeftCard();
                                YzPhzCheckCardBean checkCard1 = player1.checkCard(tempCard, true, false, false, false, false, true, 0);
                                if (checkCard1.isHu()) {
                                    canHu = 1;
                                    cardResult = checkCard1.getCardResult();
                                    if (checkCard1.isWangDiao() || checkCard1.isWangChuang() || checkCard1.isWangZha()) {
                                        addAction(player1.getSeat(), YzPhzCheckCardBean.hasActionList(checkCard1.isWangDiao() ? 7 : -1, checkCard1.isWangChuang() ? 8 : -1, checkCard1.isWangZha() ? 9 : -1));

                                        LogUtil.msgLog.info("tiqianhu:tableId=" + getId() + ",current=" + playBureau + ",userId=" + player1.getUserId() + ",seat=" + player1.getSeat() + ",cardVal=" + YzPhzCardUtils.loadCardVal(tempCard) + ",zha=" + checkCard1.isWangZha() + ",chuang=" + checkCard1.isWangChuang() + ",diao=" + checkCard1.isWangDiao());

                                        sendActionMsg(player1, YzPhzConstants.action_mo, Collections.<Integer>emptyList(), YzPhzConstants.action_type_mo, false, false, player1.getUserId());
                                        
                                        return;
                                    }
                                } else {
                                    canHu = 0;
                                }
                            }
                    }
//                }

            if (autoDisBean != null) {
                playAutoDisCard(autoDisBean);
                return;
            }

            if (nowDisCardSeat == 0) {
                return;
            }

			// // 下一个要摸牌的人
            YzPhzPlayer player = seatMap.get(nowDisCardSeat);


            if (leftCards == null) {
                return;
            }
            if (this.leftCards != null && this.leftCards.size() == 0 && !isHasSpecialAction()) {
                calcOver();
                return;
            }

            setPaoHu(0);
            setSendPaoSeat(0);
            Integer card = null;

            clearMarkMoSeat();

            card = getNextCard();
            
            //如果有人有3个王，再摸不给王了。除非是最后一张了
            if (card.intValue() > 80) {
            	if(player.getHandCards().WANGS.size()>=3){
            		if(getLeftCards().size()>0){
            			Integer temp = getNextCard();
            			getLeftCards().add(card);
            			card = temp;
            		}
            	}
            }
            
            setLastCard(card);
            changeMoCount(1);

            LogUtil.msgLog.info("load card from system:userId=" + player.getUserId() + ",seat=" + player.getSeat() + ",card=" + card);
            YzPhzCheckCardBean autoDisCard = null;
            addPlayLog(player.getSeat(), YzPhzConstants.action_mo + "", (card == null ? 0 : card) + "");
            logActionMsg(player, card, YzPhzConstants.action_mo, null);
            if (card != null) {

                if (card.intValue() > 80) {
					// 摸到王牌
                    setMoFlag(1);
                    setMoSeat(player.getSeat());
                    markMoSeat(card, player.getSeat());
//				player.moCard(card);
                    setDisposeCard(card);
                    List<Integer> cardList = new ArrayList<>();
                    cardList.add(card);
                    setNowDisCardIds(YzPhzCardUtils.asList(card));

                    player.setPassHuVal(0,0);
                    player.moWang(card);
                    YzPhzCheckCardBean checkCard = player.checkCard(card, true, canHu >= 0, false, false, false, canHu);

                    checkPaohuziCheckCard(checkCard);
                    setDisCardSeat(player.getSeat());
                    setNowDisCardSeat(player.getSeat());

                    if (player.getHandCards().INS.size() == 0) {
                        setToPlayCardFlag(0);
                        player.setCanHuState(0);
                        setNowDisCardSeat(calcNextSeat(player.getSeat()));
                    } else {
                        setToPlayCardFlag(1);
                    }
                    

                    sendActionMsg(player, YzPhzConstants.action_mowang, cardList, YzPhzConstants.action_type_action, false, false, -1);

                    if (checkCard.isHu()) {
                        setHuCard(card);
//                        if(huBiHu==2) {
//                        	setToPlayCardFlag(0);
//                   		 checkCard.setAuto(YzPhzConstants.action_hu, new ArrayList<>());
//                        	//setAutoDisBean(checkCard);
//                        if (checkPaohuziCheckCard(checkCard)) {
//                            autoDisCard = checkCard;
//                        }
//                        playAutoDisCard(autoDisCard);
//                        
//                        }
                       // sendActionMsg(player, YzPhzConstants.action_mo, new ArrayList<Integer>(), YzPhzConstants.action_type_mo);
                    } else {
                        if (toPlayCardFlag == 0) {
                            checkMo();
                        }
                    }
                    sendTingInfo(player);
                    return;
                }

                setMoFlag(1);
                setMoSeat(player.getSeat());
                markMoSeat(card, player.getSeat());

                setDisCardSeat(player.getSeat());
                setNowDisCardIds(YzPhzCardUtils.asList(card));
                setNowDisCardSeat(getNextDisCardSeat());
                setDisposeCard(card);

                actionSeatMap.clear();
                saveActionSeatMap();

//				int huCount=0;
                Map<Integer, YzPhzCardResult> cardResultMap = new HashMap<>(8);

               Set<Integer> huList = new HashSet<>();
                for (Entry<Integer, YzPhzPlayer> entry : seatMap.entrySet()) {
                    boolean self = entry.getKey().intValue() == player.getSeat();
                    YzPhzCheckCardBean checkCard = entry.getValue().checkCard(card, self, false, false, false, false, canHu);
                    	//能胡有王但不是自摸的
                    	if(checkCard.isHu()){
                    		if(entry.getValue().getHandCards().WANGS.size()>=1&&!self){
                    			checkCard.setHu(false);
                        	}
                    	}
                    
                     if (checkPaohuziCheckCard(checkCard)) {
                       autoDisCard = checkCard;
                       break;
//                       if(checkCard.isHu()) {
//                    	   
//                    	   break;
//                       }
                    }else  if (checkCard.isHu()) {
                        setHuCard(card);
//						huCount++;
                        checkPaohuziCheckCard(checkCard);
                        huList.add(entry.getKey());
                        if (self) {
                            if (cardResult != null) {
                                cardResultMap.put(entry.getKey(), cardResult);
                            } else {
                                cardResultMap.put(entry.getKey(), new YzPhzCardResult());
                            }
                           
                        } else {
                            cardResultMap.put(entry.getKey(), checkCard.getCardResult());
                          //  map2.put(entry.getKey(), 0);
                        }
                    }
                }
                

                int canPaoSeat = 0;
                if (autoDisCard != null) {
                    setAutoDisBean(autoDisCard);
                    setNowDisCardSeat(player.getSeat());
                } else {
                    for (Entry<Integer, YzPhzPlayer> entry : seatMap.entrySet()) {
                        boolean self = entry.getKey().intValue() == player.getSeat();
                        YzPhzCheckCardBean rets = entry.getValue().checkPaoHu(card, self, true);
                        List<Integer> actionList = actionSeatMap.get(entry.getKey());
                        if (actionList == null || actionList.isEmpty()) {
                            if (rets.isHu()) {
                              //  addAction(entry.getKey().intValue(), YzPhzCheckCardBean.uniqueHasActionList(5));
                            	  Integer val = YzPhzCardUtils.loadCardVal(card);
                                  List<Integer> list = entry.getValue().getHandCards().KAN.get(val);
                                  if (list == null) {
                                      list = entry.getValue().getHandCards().WEI.get(val);
                                      if (list == null) {
                                          list = entry.getValue().getHandCards().PENG.get(val);
                                      }
                                  }
                                  huList.add(entry.getKey());
                            	  setHuCard(card);
                                setPaoHu(entry.getKey());
                                checkPaohuziCheckCard(rets);
                                List<Integer> actionList2 = actionSeatMap.get(entry.getKey());
                                actionList2.set(5, 0);
//                                List<Integer> list2 = rets.getActionList();
//                                addAction(rets.getSeat(), list);
                                
//                                  if(checkAction(entry.getValue(), YzPhzConstants.action_hu)) {
//                                	  setHuCard(card);
//                                      setPaoHu(entry.getKey());
//                                	YzPhzCheckCardBean pccb = new YzPhzCheckCardBean();
//                                    setPaoCard(entry, list, pccb);
//                                         autoDisCard = pccb;
//                                  }
//								huCount++;
                            }
                        } else {
                            if (rets.isHu()) {
                                setHuCard(card);
                                huList.add(entry.getKey());
                                YzPhzCardResult cr = cardResultMap.get(entry.getKey());
                                if (cr == null) {
                                    setPaoHu(entry.getKey());
                                } else {
                                    if (self && !cr.isCanHu()) {
                                        YzPhzCheckCardBean checkCard = entry.getValue().checkCard(card, self, false, false, false, false);
                                        if (checkCard.isHu()) {
                                            cr = checkCard.getCardResult();
                                            cardResultMap.put(entry.getKey(), cr);
                                        }
                                    }

                                    if (cr.getTotalFan() > rets.getCardResult().getTotalFan()) {

                                    } else if (cr.getTotalFan() == rets.getCardResult().getTotalFan()) {
                                        if (cr.getTotalTun() < rets.getCardResult().getTotalTun()) {
                                            setPaoHu(entry.getKey());
                                        }
                                    } else {
                                        setPaoHu(entry.getKey());
                                    }
                                }
//								huCount+=
                                actionList.set(0, 1);
                                saveActionSeatMap();
                            }
                        }
                        if (rets.isPao()) {
                        	
                        	  List<Integer> act =  actionSeatMap.get(entry.getKey());
                        	Integer val = YzPhzCardUtils.loadCardVal(card);
                            List<Integer> list = entry.getValue().getHandCards().KAN.get(val);
                            if (list == null) {
                                list = entry.getValue().getHandCards().WEI.get(val);
                                if (list == null) {
                                    list = entry.getValue().getHandCards().PENG.get(val);
                                }
                            }
                            YzPhzCheckCardBean pccb = new YzPhzCheckCardBean();
                            setPaoCard(entry, list, pccb);
                          
							// 不能胡就跑
                            boolean canPao = !actionCanHu(act)&&!rets.isHu();
//                            canPao = ;
                            if(canPao) {
                            	autoDisCard =  pccb; 
                            }
                            
                            if(act!=null) {
                            	addAction(entry.getKey(), act);
                            }
                            break;
                        }
                    }
                }
                
                YzPhzPlayer nextPlayer = seatMap.get(calcNextSeat(player.getSeat()));
                int val = YzPhzCardUtils.loadCardVal(card);
                checkSchi(player, nextPlayer, true, val);

                markMoSeat(player.getSeat(), YzPhzConstants.action_mo);
                if (autoDisCard != null && (autoDisCard.getAutoAction() == YzPhzConstants.action_zai || autoDisCard.getAutoAction() == YzPhzConstants.action_chouzai || autoDisCard.getAutoAction() == YzPhzConstants.action_ti)) {
                    sendMoMsg(player, YzPhzConstants.action_mo, new ArrayList<>(Arrays.asList(card)), YzPhzConstants.action_type_mo);
                } else {
                    sendActionMsg(player, YzPhzConstants.action_mo, new ArrayList<>(Arrays.asList(card)), YzPhzConstants.action_type_mo);
                }

                if (this.leftCards != null && this.leftCards.size() == 0 && !isHasSpecialAction()) {
                    calcOver();
                    return;
                }

                if (autoDisCard != null) {
                    playAutoDisCard(autoDisCard);
                } else {
                    if (actionSeatMap.isEmpty()) {
                        player.outCardOfMo(card, this);
                        checkMo();
                    }
                }
            }
        }
    }
    
    
    private void checkSchi(YzPhzPlayer cPlayer,YzPhzPlayer nextPlayer,boolean isMo,int cardVal){
    	List<Integer> cList = actionSeatMap.get(cPlayer.getSeat());
    	List<Integer> nList = actionSeatMap.get(nextPlayer.getSeat());
    	//当前的不能操作
    	if(cList==null ||(cList.get(4)==0&&cList.get(2)==0)){
    		cPlayer.passVal(action_chi, cardVal);
    		if(nList==null ||nList.isEmpty()){
        		nextPlayer.passVal(action_chi, cardVal);
        	}
    		
    	}
    }
    
    public void logActionMsg(YzPhzPlayer player, int id,int action, List<Integer> actList) {
    	try {
    		GuihzCard card = GuihzCard.getPaohzCard(id);
            StringBuilder sb = new StringBuilder();
            sb.append("YzPhz");
            sb.append("|").append(getId());
            sb.append("|").append(getPlayBureau());
            sb.append("|").append(player.getUserId());
            sb.append("|").append(player.getSeat());
            sb.append("|").append(player.isAutoPlay() ? 1 : 0);
            sb.append("|").append(YzPhzConstants.renameAction(action));
            sb.append("|").append(card==null?id:card);
            sb.append("|").append(actList==null?"autoAction":actList);
            sb.append("|").append(player.getHandPais());
            LogUtil.msg(sb.toString());
		} catch (Exception e) {
		}
    	
    }

    
    private void setPaoCard(Entry<Integer, YzPhzPlayer> entry, List<Integer> list, YzPhzCheckCardBean pccb) {
		pccb.setSeat(entry.getKey());
		pccb.setDisCard(disposeCard);
		pccb.setPao(true);
		pccb.setAuto(YzPhzConstants.action_pao, new ArrayList<>(list));
		pccb.buildActionList();
		checkPaohuziCheckCard(pccb);
		setAutoDisBean(pccb);
	}
    
    
    private boolean actionCanHu(List<Integer> list) {
		if(list==null) {
			return false;
		}
		if(list.get(0)==1||list.get(7) ==1||list.get(8) ==1||list.get(9) ==1) {
			return true;
		}
		return false;
		
	}

	/**
	 * 除了吃和碰之外的都是特殊动作
	 *
	 * @return
	 */
    private boolean isHasSpecialAction() {
        boolean b = false;
        for (List<Integer> actionList : actionSeatMap.values()) {
            if (actionList.get(0).intValue() == 1 || actionList.get(2).intValue() == 1 || actionList.get(3).intValue() == 1 || actionList.get(5).intValue() == 1 || actionList.get(6).intValue() == 1) {
				// 除了吃和碰之外的都是特殊动作
                b = true;
                break;
            }
        }
        return b;
    }

    /**
	 * @param player
	 * @param action
	 * @param disCard
	 * @return 是否有系统帮助自动出牌
	 */
    private YzPhzCheckCardBean checkDisAction(YzPhzPlayer player, int action, Integer disCard, boolean isPassHu) {
        YzPhzCheckCardBean result = null;
        for (Entry<Integer, YzPhzPlayer> entry : seatMap.entrySet()) {
            if (entry.getKey().intValue() == player.getSeat()) {
                continue;
            }

            YzPhzCheckCardBean checkCard = entry.getValue().checkCard(disCard, false, isPassHu, false, false, false);
            boolean check = checkPaohuziCheckCard(checkCard);
            if (check) {
                result = checkCard;
            }
            if (!checkCard.isHu()) {
                YzPhzCheckCardBean rets = entry.getValue().checkPaoHu(disCard, entry.getKey().intValue() == player.getSeat(), !isPassHu);
//                if (rets.isHu()) {
//                    addAction(entry.getKey().intValue(), checkCard.uniqueHasActionList(0));
//                }
                
                if (rets.isPao()) {
                    Integer val = YzPhzCardUtils.loadCardVal(disCard);
                    List<Integer> list = entry.getValue().getHandCards().KAN.get(val);
                    if (list == null) {
                        list = entry.getValue().getHandCards().WEI.get(val);
                        if (list == null) {
                            list = entry.getValue().getHandCards().PENG.get(val);
                        }
                    }
                    YzPhzCheckCardBean pccb = new YzPhzCheckCardBean();
                    setPaoCard(entry, list, pccb);
                    result =  pccb; 
					// break;
                }
            }
        }
        return result;
    }

    public boolean checkPaohuziCheckCard(YzPhzCheckCardBean checkCard) {
        List<Integer> list = checkCard.getActionList();
        if (list == null || list.isEmpty()) {
            return false;
        }

        addAction(checkCard.getSeat(), list);
        List<Integer> autoDisList = checkCard.getAutoDisList();
        if (autoDisList != null) {
			// 不能胡就自动出牌
            if (checkCard.isTi() || checkCard.isZai() || checkCard.isChouZai() || checkCard.isPao()) {
                setAutoDisBean(checkCard);
                return true;
            }
        }
        return false;

    }

    public void setAutoDisBean(YzPhzCheckCardBean autoDisBean) {
        this.autoDisBean = autoDisBean;
        changeExtend();
    }

    private void addAction(int seat, List<Integer> actionList) {
        actionSeatMap.put(seat, actionList);
        addPlayLog(seat, YzPhzConstants.action_hasaction + "", StringUtil.implode(actionList));
        saveActionSeatMap();
    }

    private List<Integer> removeAction(int seat) {
        if (sendPaoSeat == seat) {
            setSendPaoSeat(0);
        }
        List<Integer> list = actionSeatMap.remove(seat);
        saveActionSeatMap();
        return list;
    }

    private void clearAction() {
        setSendPaoSeat(0);
        actionSeatMap.clear();
        userActionMap.clear();
        changeExtend();
        saveActionSeatMap();
    }

    private void clearHuList() {
        huConfirmList.clear();
        changeExtend();
    }

    public void saveActionSeatMap() {
        dbParamMap.put("nowAction", JSON_TAG);
    }

    private void sendActionMsg(YzPhzPlayer player, int action, List<Integer> cards, int actType) {
        sendActionMsg(player, action, cards, actType, false, false, -1);
    }

    /**
	 * 发送所有玩家动作msg
	 *
	 * @param player
	 * @param action
	 * @param cards
	 * @param actType
	 */
    private void sendMoMsg(YzPhzPlayer player, int action, List<Integer> cards, int actType) {
        PlayPaohuziRes.Builder builder = PlayPaohuziRes.newBuilder();
        builder.setAction(action);
        builder.setUserId(player.getUserId() + "");
        builder.setSeat(player.getSeat());
        builder.setHuxi(player.getHandCards().loadPPChuxi());
        // builder.setNextSeat(nowDisCardSeat);
        setNextSeatMsg(builder);
        builder.setRemain(leftCards.size());
        builder.addAllPhzIds(cards == null ? new ArrayList<Integer>() : cards);
        builder.setActType(actType);
        sendMoMsgBySelfAction(builder, player.getSeat());
    }

    private void sendPlayerActionMsg(YzPhzPlayer player, boolean force) {
        sendPlayerActionMsg(player, force, null, 0, YzPhzConstants.action_refreshaction);
    }

    /**
	 * 发送该玩家动作msg
	 *
	 * @param player
	 */
    private void sendPlayerActionMsg(YzPhzPlayer player, boolean force, List<Integer> cards, int actType, int action) {
        if (!force && !actionSeatMap.containsKey(player.getSeat())) {
            return;
        }
        PlayPaohuziRes.Builder builder = PlayPaohuziRes.newBuilder();
        builder.setAction(action);
        builder.setUserId(player.getUserId() + "");
        builder.setSeat(player.getSeat());
        builder.setHuxi(player.loadTWhuxi() + player.getHandCards().loadPPChuxi());
        // builder.setNextSeat(nowDisCardSeat);
        setNextSeatMsg(builder);
        if (leftCards != null) {
            builder.setRemain(leftCards.size());

        }
        if (cards != null) {
            builder.addAllPhzIds(cards);
        } else if (force) {
            builder.addAllPhzIds(Collections.<Integer>emptyList());
        }

        builder.setActType(actType);

        if (!force) {
            KeyValuePair<Boolean, Integer> zaiKeyValue = getZaiOrTiKeyValue();
            List<Integer> actionList = getSendSelfAction(zaiKeyValue, player.getSeat(), actionSeatMap.get(player.getSeat()));
            if (actionList != null) {
                builder.addAllSelfAct(actionList);
            } else {
                builder.addAllSelfAct(Collections.<Integer>emptyList());
            }
        } else {
            builder.addAllSelfAct(Collections.<Integer>emptyList());
        }

        player.writeSocket(builder.build());
    }

    private void setNextSeatMsg(PlayPaohuziRes.Builder builder) {

        builder.setTimeSeat(nowDisCardSeat);
        if (toPlayCardFlag == 1) {
            builder.setNextSeat(nowDisCardSeat);
        } else {
            builder.setNextSeat(0);

        }

    }

    /**
	 * 发送动作msg
	 *
	 * @param player
	 * @param action
	 * @param cards
	 * @param actType
	 */
    private void sendActionMsg(YzPhzPlayer player, int action, List<Integer> cards, int actType, boolean isZaiPao, boolean isChongPao, long userId) {
        PlayPaohuziRes.Builder builder = PlayPaohuziRes.newBuilder();
        builder.setAction(action);
        builder.setUserId(player.getUserId() + "");
        builder.setSeat(player.getSeat());
        builder.setHuxi(player.getHandCards().loadPPChuxi());
        setNextSeatMsg(builder);
        if (leftCards != null) {
            builder.setRemain(leftCards.size());

        }
//		System.out.println("cards:"+cards);
        builder.addAllPhzIds(cards == null ? new ArrayList<Integer>() : cards);
        builder.setActType(actType);
        if (isZaiPao) {
            builder.setIsZaiPao(1);
        }
        if (isChongPao) {
            builder.setIsChongPao(1);
        }
        sendMsgBySelfAction(builder, userId);
    }

    /**
	 * 目前的动作中是否有人有栽或者是提 || entry.getValue().get(5) == 1
	 *
	 * @return
	 */
    private KeyValuePair<Boolean, Integer> getZaiOrTiKeyValue() {
        KeyValuePair<Boolean, Integer> keyValue = new KeyValuePair<>();
        boolean isHasZaiOrTi = false;
        int zaiSeat = 0;
        for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
            if (entry.getValue().get(2) == 1 || entry.getValue().get(3) == 1 || entry.getValue().get(6) == 1) {
                isHasZaiOrTi = true;
                zaiSeat = entry.getKey();
                break;
            }
        }
        keyValue.setId(isHasZaiOrTi);
        keyValue.setValue(zaiSeat);
        return keyValue;
    }

    private List<Integer> getSendSelfAction(KeyValuePair<Boolean, Integer> zaiKeyValue, int seat, List<Integer> actionList) {

        if (actionList == null || actionList.isEmpty()) {
            return actionList;
        }

        boolean isHasZaiOrTi = zaiKeyValue.getId();
        int zaiSeat = zaiKeyValue.getValue();
        if (isHasZaiOrTi) {
            if (zaiSeat == seat) {
                return actionList;
            }
        } else if (actionList.get(0).intValue() == 1) {
            return actionList;
        } else if (actionList.get(5).intValue() == 1) {
//			if (sendPaoSeat == seat) {
            return actionList;
//			}
        } else if (actionList.get(2).intValue() == 1 || actionList.get(3).intValue() == 1) {
			// 0胡 1碰 2栽 3提 4吃 5跑
			// 如果能自动出牌的话 不需要提示
            // ...
            return null;
        } else {
            return actionList;
        }
        return null;

    }

    /**
	 * 发送消息带入自己动作
	 *
	 * @param builder
	 */
    private void sendMoMsgBySelfAction(PlayPaohuziRes.Builder builder, int seat) {
        KeyValuePair<Boolean, Integer> zaiKeyValue = getZaiOrTiKeyValue();
        for (YzPhzPlayer player : seatMap.values()) {
            PlayPaohuziRes.Builder copy = builder.clone();

//            if (player.getSeat() == shuXingSeat && seat == lastWinSeat) {
//                copy.setHuxi(seatMap.get(seat).getOutHuxi() + seatMap.get(seat).getZaiHuxi());
//            } else {
                if (player.getSeat() != seat) {
                    copy.clearPhzIds();
                    copy.addPhzIds(0);
                } else {
                    copy.setHuxi(player.getOutHuxi() + player.getZaiHuxi());
                }
                if (actionSeatMap.containsKey(player.getSeat())) {
                    List<Integer> actionList = getSendSelfAction(zaiKeyValue, player.getSeat(), actionSeatMap.get(player.getSeat()));
                    if (actionList != null) {
                        copy.addAllSelfAct(actionList);
                    }
                }
//            }

            player.writeSocket(copy.build());
        }
    }

    /**
	 * 发送消息带入自己动作
	 *
	 * @param builder
	 */
    private void sendMsgBySelfAction(PlayPaohuziRes.Builder builder, long userId) {
        KeyValuePair<Boolean, Integer> zaiKeyValue = getZaiOrTiKeyValue();

        int actType = builder.getActType();
        boolean noShow = false;
        // boolean hasHu = false;
        int paoSeat = 0;
        if (YzPhzConstants.action_type_dis == actType || YzPhzConstants.action_type_mo == actType) {
            for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
                if (1 == entry.getValue().get(5).intValue()) {
                    noShow = true;
                    paoSeat = entry.getKey();
                    break;
                }

            }
        }

        for (YzPhzPlayer player : seatMap.values()) {

            if (userId > 0 && userId != player.getUserId()) {
                continue;
            }

            PlayPaohuziRes.Builder copy = builder.clone();
//            if (player.getSeat() == shuXingSeat) {
//                copy.setHuxi(seatMap.get(lastWinSeat).getHandCards().loadPPChuxi() + seatMap.get(lastWinSeat).loadTWhuxi());
//                if (YzPhzConstants.action_type_dis == actType && player.isAutoPlay()) {
//                    copy.setActType(YzPhzConstants.action_type_dis_auto);
//                }
//            } else 
            	
            	if (copy.getSeat() == player.getSeat()) {
                copy.setHuxi(player.getHandCards().loadPPChuxi() + player.loadTWhuxi());
                if (YzPhzConstants.action_type_dis == actType && player.isAutoPlay()) {
                    copy.setActType(YzPhzConstants.action_type_dis_auto);
                }
            }
			// 需要特殊处理一下栽
            if (copy.getAction() == YzPhzConstants.action_zai || copy.getAction() == YzPhzConstants.action_chouzai || copy.getAction() == YzPhzConstants.action_ti) {
//                if (copy.getSeat() == lastWinSeat && player.getSeat() == shuXingSeat) {
//
//                } else if (copy.getSeat() != player.getSeat()) {
				// // 需要替换成0
////					List<Integer> ids = PaohuziTool.toPhzCardZeroIds(copy.getPhzIdsList());
////					if(copy.getAction() == YzPhzConstants.action_chouzai){
////						ids.set(0,copy.getPhzIdsList().get(0));
////					}
////					copy.clearPhzIds();
////					copy.addAllPhzIds(ids);
//                    //todo
//                }
            }

            if (actionSeatMap.containsKey(player.getSeat())) {
                List<Integer> actionList = getSendSelfAction(zaiKeyValue, player.getSeat(), actionSeatMap.get(player.getSeat()));
                if (actionList != null) {
                	
               	 List<Integer>	actionList2 = new ArrayList<>(actionList);
                   // copy.addAllSelfAct(actionList);
               	if(actionList2.size()>6) {
               		actionList2.set(5, 0);
               	}
                   if (noShow && paoSeat != player.getSeat()) {
						// 出牌时，别人有跑的情况不提示吃碰
                       if (1 == actionList2.get(0)) {
                           copy.addAllSelfAct(actionList2);
                       }
                   } else {
                       copy.addAllSelfAct(actionList2);
                   }
               }


            }
            player.writeSocket(copy.build());
        }
    }

    /**
	 * 推送给有动作的人消息
	 */
    private void checkSendActionMsg() {
        if (actionSeatMap.isEmpty()) {
            checkMo();
            return;
        }

        PlayPaohuziRes.Builder disBuilder = PlayPaohuziRes.newBuilder();
        YzPhzPlayer disCSMajiangPlayer = seatMap.get(disCardSeat);
        buildPlayRes(disBuilder, disCSMajiangPlayer, 0);
        disBuilder.setRemain(leftCards.size());
        disBuilder.setHuxi(disCSMajiangPlayer.getHandCards().loadPPChuxi());
        // disBuilder.setNextSeat(nowDisCardSeat);
        setNextSeatMsg(disBuilder);
        for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
            PlayPaohuziRes.Builder copy = disBuilder.clone();
            List<Integer> actionList = entry.getValue();
            copy.addAllSelfAct(actionList);
            YzPhzPlayer seatPlayer = seatMap.get(entry.getKey());
            seatPlayer.writeSocket(copy.build());
        }

    }

    public static void buildPlayRes(PlayPaohuziRes.Builder builder, Player player, int action) {
        builder.addAllPhzIds(Collections.<Integer>emptyList());
        builder.setAction(action);
        builder.setUserId(player.getUserId() + "");
        builder.setSeat(player.getSeat());
    }

    public void checkAction() {
        int nowSeat = getNowDisCardSeat();
		// 先判断拿牌的玩家
        YzPhzPlayer nowPlayer = seatMap.get(nowSeat);
        if (nowPlayer == null) {
            return;
        }
        YzPhzCheckCardBean checkCard = nowPlayer.checkCard(null, true, false, false, false, false);

        if (checkCard.isHu()) {
            lastCard = isTianHu() ? checkCard.getCardResult().getHuCard() : nowPlayer.getHandCards().SRC.get(new Random().nextInt(nowPlayer.getHandCards().SRC.size()));
            changeExtend();
        }

        checkPaohuziCheckCard(checkCard);
        checkSendActionMsg();
    }

    /**
	 * 自动出牌
	 *
	 * @param checkCard
	 */
    public void playAutoDisCard(YzPhzCheckCardBean checkCard) {
        playAutoDisCard(checkCard, false);
    }

    /**
	 * 自动出牌
	 *
	 * @param checkCard
	 * @param moPai
	 *            是否是摸牌 如果是摸牌，需要
	 */
    public void playAutoDisCard(YzPhzCheckCardBean checkCard, boolean moPai) {
        LogUtil.msgLog.info("playAutoDisCard:tableId=" + id + ",current=" + playBureau + ",toPlayCardFlag=" + toPlayCardFlag + ",seat=" + checkCard.getSeat() + ",action=" + checkCard.getActionList() + ",cards=" + checkCard.getAutoDisList() + ",actionMap=" + actionSeatMap);
        if (toPlayCardFlag == 0 && checkCard.getActionList() != null) {
            setAutoDisBean(checkCard);
            int seat = checkCard.getSeat();
            YzPhzPlayer player = seatMap.get(seat);
            if (player.isRobot()) {
                sleep();
            }
			// System.out.println(player.getName() + "自动出牌------------check:" +
			// checkCard.getAutoAction() + " " + checkCard.getAutoDisList());
            List<Integer> list = checkCard.getAutoDisList();

            play(player, list, checkCard.getAutoAction(), moPai, false, checkCard.isPassHu());

            if (actionSeatMap.isEmpty()) {
                setAutoDisBean(null);
            }
        }

    }

    private void sleep() {
    }

    @Override
    protected void robotDealAction() {
    }

    @Override
    public int getPlayerCount() {
        return seatMap.size();
    }

    @Override
    protected void initNext1() {
        setSendPaoSeat(0);
        setZaiCard(null);
        setBeRemoveCard(null);
        setAutoDisBean(null);
        clearMarkMoSeat();
        clearMoSeatPair();
        clearLastSeatAction();
        clearHuList();
        setLeftCards(null);
        setMoFlag(0);
        setMoSeat(0);
        clearAction();
        setNowDisCardSeat(0);
        setNowDisCardIds(null);
        setFirstCard(true);
        setDisposeCard(0);
//        if (hasZuoXing()) {
//            setShuXingSeat(calcNextNextSeat(getLastWinSeat()));
//        }
        moCount = 0;
        disCount = 0;
        huCard = 0;
        paoHu = 0;
        lastCard = 0;
        timeNum = 0;
        changeExtend();
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
                tempMap.put("nowDisCardIds", intList2Str(nowDisCardIds));
            }
            if (tempMap.containsKey("leftPais")) {
                tempMap.put("leftPais", intList2Str(leftCards));
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
        if (autoDisBean != null) {
            wrapper.putString(6, autoDisBean.buildAutoDisStr());

        } else {
            wrapper.putString(6, "");
        }
        if (zaiCard != null) {
            wrapper.putInt(7, zaiCard);
        }
        wrapper.putInt(8, sendPaoSeat);
        wrapper.putInt(9, firstCard ? 1 : 0);
        if (beRemoveCard != null) {
            wrapper.putInt(10, beRemoveCard);
        }
//        wrapper.putInt(11, shuXingSeat);
        wrapper.putInt(12, maxPlayerCount);
        wrapper.putInt(13, wangbaCount);
        wrapper.putInt(14, fanxinConfig);
        wrapper.putInt(15, disposeCard);
        if (lastSeatAction != null) {
            String lastSeatActionVal = lastSeatAction.getId() + "_" + lastSeatAction.getValue();
            wrapper.putString(16, lastSeatActionVal);
        }
        wrapper.putInt(17, limitHu);
        wrapper.putInt(18, limitScore);
        wrapper.putInt(19, huCard);
        wrapper.putInt(20, paoHu);
        wrapper.putInt(21, moCount);
        wrapper.putInt(22, disCount);
        wrapper.putInt(23, red2Black);
        wrapper.putInt(24, minHuxi);
        wrapper.putInt(25, xingRate);
        wrapper.putInt(26, lastCard);
        

        wrapper.putInt(27, tuoguan);
        wrapper.putInt(28, autoPlayGlob);
        
        wrapper.putInt(29, jiaBei);
        wrapper.putInt(30, jiaBeiFen);
        wrapper.putInt(31, jiaBeiShu);

		wrapper.putInt(32, topFengding);

        if (userActionMap.size() > 0) {
            StringBuilder strBuilder = new StringBuilder();
            for (Entry<Integer, String> kv : userActionMap.entrySet()) {
                strBuilder.append(",").append(kv.getKey()).append("_").append(kv.getValue());
            }
            if (strBuilder.length() > 0) {
                wrapper.putString("recall", strBuilder.substring(1));
            }
        }

        wrapper.putString("gModeId", modeId);
        wrapper.putInt(33, below);
        wrapper.putInt(34, belowAdd);
        wrapper.putInt(35, randomZhuangj);
        

        return wrapper;
    }

    public void setLastCard(int lastCard) {
        this.lastCard = lastCard;
        changeExtend();
    }

  
    
    
    @Override
    protected synchronized void deal() {
        clearAction();
        if (playedBureau <= 0) {
            for (YzPhzPlayer player : playerMap.values()) {
                player.setAutoPlay(false, this);
                player.setLastOperateTime(System.currentTimeMillis());
            }
        }


        if (lastWinSeat == 0 && seatMap.size() > 0) {
        	YzPhzPlayer player = playerMap.get(masterId);
            int masterseat = player != null ? player.getSeat() : seatMap.keySet().iterator().next();
            if(randomZhuangj==1) {
            	masterseat = new Random().nextInt(getMaxPlayerCount())+1;
            }
            setLastWinSeat(masterseat);
        }
       
        setDisCardSeat(lastWinSeat);
        setNowDisCardSeat(lastWinSeat);
        setMoSeat(lastWinSeat);
        setToPlayCardFlag(1);

        List<List<Integer>> list = null;
        
        if(zp != null && !zp.isEmpty()) {
        	list = YzPhzCardUtils.fapai2(YzPhzCardUtils.loadCards(wangbaCount > 0 ? wangbaCount : 0), zp);
        }else {
//        	list = YzPhzCardUtils.loadCards(YzPhzCardUtils.loadCards(wangbaCount > 0 ? wangbaCount : 0),  maxPlayerCount, 20);
            list = YzPhzCardUtils.fapaiControl(YzPhzCardUtils.loadCards(wangbaCount > 0 ? wangbaCount : 0), maxPlayerCount, 20,0);
        }
        //maxPlayerCount >= 3 ? 3 :
        int i = 1;
        for (YzPhzPlayer player : playerMap.values()) {
            player.changeState(SharedConstants.player_state.play);

            if (!player.isAutoPlay()) {
                player.setLastOperateTime(System.currentTimeMillis());
            }

            if (player.getSeat() == lastWinSeat) {
				player.dealHandPais(list.get(0));
//                continue;
            }else {
            	  System.out.println("list pai =              " + list.get(i) + "      ---i =" + i);
            	 player.dealHandPais(list.get(i));
                 i++;
            }
			// 数醒不发牌,设置为空List
//            if (player.getSeat() == shuXingSeat) {
//                player.dealHandPais(new ArrayList<Integer>());
//                continue;
//            }
          
           
        }
        List<Integer> left = new ArrayList<Integer>();
        
    	if(getMaxPlayerCount()<3&&list.size()==4){
			left.addAll(list.get(2));
		}
		left.addAll(list.get(list.size() - 1));
        setMoFlag(1);
		// 桌上剩余的牌
        setLeftCards(left);
    }
    
    @Override
    public int getNextDisCardSeat() {
        if (disCardSeat == 0) {
            return lastWinSeat;
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
//        if (nextSeat == shuXingSeat) {
//            nextSeat = nextSeat + 1 > maxPlayerCount ? 1 : nextSeat + 1;
//        }
        return nextSeat;
    }

    /**
	 * 计算seat前面的座位
	 *
	 * @param seat
	 * @return
	 */
    public int calcFrontSeat(int seat) {
        int frontSeat = seat - 1 < 1 ? maxPlayerCount : seat - 1;
//        if (frontSeat == shuXingSeat) {
//            frontSeat = frontSeat - 1 < 1 ? maxPlayerCount : frontSeat - 1;
//        }
        return frontSeat;
    }

    /**
	 * 获取数醒座位
	 *
	 * @param seat
	 * @return
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

    @Override
    public Map<Integer, Player> getSeatMap() {
        Object o = seatMap;
        return (Map<Integer, Player>) o;
    }

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

        if (isTianHu())
            synchronized (this) {
                if (isTianHu()) {
                    Iterator<Entry<Integer, List<Integer>>> its = actionSeatMap.entrySet().iterator();
                    while (its.hasNext()) {
                        Entry<Integer, List<Integer>> kv = its.next();
                        if (kv.getKey().intValue() != disCardSeat) {
                            its.remove();
                        } else {
                            for (int i = 0, len = kv.getValue().size(); i < len; i++) {
                                if (i == 0) {
                                    if (kv.getValue().get(i).intValue() != 1) {
                                        its.remove();
                                    }
                                } else {
                                    kv.getValue().set(i, 0);
                                }
                            }
                        }
                    }
                }
            }

        KeyValuePair<Boolean, Integer> zaiKeyValue = getZaiOrTiKeyValue();

        boolean tiOrZai = false;
        if (lastSeatAction != null) {
            if (YzPhzConstants.action_ti == lastSeatAction.getValue() || YzPhzConstants.action_zai == lastSeatAction.getValue() || YzPhzConstants.action_chouzai == lastSeatAction.getValue()) {
                tiOrZai = true;
            }
        }

        List<PlayerInTableRes> players = new ArrayList<>();
        for (YzPhzPlayer player : playerMap.values()) {
            PlayerInTableRes.Builder playerRes = player.buildPlayInTableInfo(this, userId, isrecover);
            playerRes.addRecover(player.getSeat() == lastWinSeat ? 1 : 0);
            if (player.getUserId() == userId) {
//                if (player.getSeat() == shuXingSeat) {
//                    playerRes.addAllHandCardIds(seatMap.get(lastWinSeat).loadHandCards());
//                } else
                
                {
                    playerRes.addAllHandCardIds(player.loadHandCards());
                    if (actionSeatMap.containsKey(player.getSeat())) {
                        List<Integer> actionList = getSendSelfAction(zaiKeyValue, player.getSeat(), actionSeatMap.get(player.getSeat()));
                        if (actionList != null) {
                        	if(actionList.size()>6) {
                        		actionList.set(5, 0);
                           	}
                        	String ac = userActionMap.get(player.getSeat());
                        	if(actionList.contains(1)&& ac==null){
                        		playerRes.addAllRecover(actionList);
                        	}

                        }

                    }
                }
            }
//            if (hasZuoXing()) {
//                playerRes.addExt(shuXingSeat);
//                playerRes.addExt(state.getId());
//            }
			// 是否为托管
            playerRes.addExt(player.isAutoPlay() ? 1 : 0);
            if (disposeCard > 80 || tiOrZai) {
                playerRes.clearOutCardIds();
            }
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
		res.addExt(wangbaCount);// 选王
		res.addExt(fanxinConfig);// 跟醒还是翻醒
		res.addExt(limitHu);// 有胡必胡
		res.addExt(limitScore);// 封顶
		res.addExt(payType);// 支付类型

        if (lastWinSeat == 0 && seatMap.size() > 0) {
            YzPhzPlayer player = playerMap.get(masterId);
            int masterseat = player != null ? player.getSeat() : seatMap.keySet().iterator().next();
            if(randomZhuangj==1) {
            	masterseat = new Random().nextInt(getMaxPlayerCount())+1;
            }
            setLastWinSeat(masterseat);
        }

		res.addExt(lastWinSeat);// 庄家
		res.addExt(shuXingSeat);// 数醒位置
		res.addExt(xingRate);// 双醒
        res.addExt(minHuxi);
        res.addExt(modeId.length() > 0 ? Integer.parseInt(modeId) : 0);
		res.addExt(lastCard);// 最后一张牌
		res.addExt(disCardSeat);// 出这个牌的人的位置

        res.addExt(autoPlay ? 1 : 0);// 17
        res.addTimeOut(autoPlay ?(int)autoTimeOut:0);
        
//        res.addExt(nowDisCardSeat);
//        res.addExt(wangbaCount);
//        res.addExt(fanxinConfig);
//        res.addExt(limitHu);
//        res.addExt(limitScore);
//        res.addExt(payType);
//
//        if (lastWinSeat == 0 && seatMap.size() > 0) {
//            YzPhzPlayer player = playerMap.get(masterId);
//            int masterseat = player != null ? player.getSeat() : seatMap.keySet().iterator().next();
//            setLastWinSeat(masterseat);
//        }
//
//        res.addExt(lastWinSeat);
////        res.addExt(shuXingSeat);
//        res.addExt(red2Black);
//        res.addExt(xingRate);
//        res.addExt(minHuxi);
//        res.addExt(modeId.length() > 0 ? Integer.parseInt(modeId) : 0);
		// res.addExt(lastCard);//最后一张牌
		// res.addExt(disCardSeat);//出这个牌的人的位置
//
//        int ratio;
//        int pay;
//        if (isGoldRoom()) {
//            ratio = GameConfigUtil.loadGoldRatio(modeId);
//            pay = PayConfigUtil.get(playType, totalBureau, maxPlayerCount, payType == 1 ? 0 : 1, modeId);
//        } else {
//            ratio = 1;
//            pay = PayConfigUtil.get(playType, totalBureau, maxPlayerCount, payType == 1 ? 0 : 1);
//        }
//        res.addExt(ratio);
//        res.addExt(pay >= 0 ? pay : 0);

        return buildCreateTableRes1(res, isLastReady);
    }

    @Override
    public void setConfig(int index, int val) {

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

    public int getBopiPoint(YzPhzPlayer player) {
        if (!isBoPi()) {
            return 0;
        }

        int selfPoint = 0;
        int otherPoint = 0;
        int retPoint = 0;
        for (YzPhzPlayer temp : seatMap.values()) {
            if (player.getUserId() == temp.getUserId()) {
                selfPoint = randNumber(temp.loadScore());
            } else {
                otherPoint += randNumber(temp.loadScore());
            }
        }

        retPoint = selfPoint * (seatMap.size() - 1) - otherPoint;
        return retPoint;
    }

    public ClosingPhzInfoRes.Builder sendAccountsMsg(boolean over, boolean selfMo, List<Integer> winList, int totalFan, List<Integer> fanTypes, int totalTun, boolean isBreak, int fanXinCardId, int fanxinPoint,int fanXinCardId2,int onePoint) {
    	 List<ClosingPhzPlayerInfoRes> list = new ArrayList<>();
    	 YzPhzPlayer winPlayer = null;
         
         List<ClosingPhzPlayerInfoRes.Builder> builderList = new ArrayList<>();

         
         for (YzPhzPlayer player : seatMap.values()) {
        	 logTotalPointMsg(player,4);
// 			if (player.getFirstPais()==null){
// 				continue;
// 			}
             if (winList != null && winList.contains(player.getSeat())) {
                 winPlayer = seatMap.get(player.getSeat());
             }
             ClosingPhzPlayerInfoRes.Builder build = null;
             if (over) {
                 build = player.bulidTotalClosingPlayerInfoRes(this,onePoint, over );
             } else {
                 build = player.bulidOneClosingPlayerInfoRes(this, onePoint, over);
             }
             
             
             for(int action : player.getActionTotalArr()){
             	build.addStrExt(action+"");
     		}
             
			// build.addAllFirstCards(player.getFirstPais());//将初始手牌装入网络对象
			build.addAllFirstCards(player.loadHandCards());// 手牌
//             list.add(build.build());
             builderList.add(build);
			// 信用分
             if (isCreditTable()) {
                 player.setWinLoseCredit(player.getTotalPoint() * creditDifen);
                 if(over) {
                     StringBuilder sb = new StringBuilder("YzPhz|sendAccountsMsg|1");
                     sb.append("|").append(getId());
                     sb.append("|").append(getPlayBureau());
                     sb.append("|").append(player.getUserId());
                     sb.append("|").append(player.getWinLoseCredit());
                     sb.append("|").append(player.getCommissionCredit());
                     sb.append("|").append(player.getTotalPoint());
                     sb.append("|").append(player.getPoint());
                     LogUtil.msgLog.info(sb.toString());
                 }
             }
         }
         
         
         
		// 信用分计算
         if (isCreditTable()) {
			// 计算信用负分
             calcNegativeCredit();

             long dyjCredit = 0;
             for (YzPhzPlayer player : seatMap.values()) {
                 if (player.getWinLoseCredit() > dyjCredit) {
                     dyjCredit = player.getWinLoseCredit();
                 }
             }
             for (ClosingPhzPlayerInfoRes.Builder builder : builderList) {
            	 YzPhzPlayer player = seatMap.get(builder.getSeat());
                 calcCommissionCredit(player, dyjCredit);

                 builder.addStrExt(player.getWinLoseCredit() + "");      //8
                 builder.addStrExt(player.getCommissionCredit() + "");   //9

				// 2019-02-26更新
                 builder.setWinLoseCredit(player.getWinLoseCredit());
                 builder.setCommissionCredit(player.getCommissionCredit());
                 if(over) {
                     StringBuilder sb = new StringBuilder("YzPhz|sendAccountsMsg|2");
                     sb.append("|").append(getId());
                     sb.append("|").append(getPlayBureau());
                     sb.append("|").append(player.getUserId());
                     sb.append("|").append(player.getWinLoseCredit());
                     sb.append("|").append(player.getCommissionCredit());
                     sb.append("|").append(player.getTotalPoint());
                     sb.append("|").append(player.getPoint());
                     LogUtil.msgLog.info(sb.toString());
                 }
             }
         } else if (isGroupTableGoldRoom()) {
             // -----------亲友圈金币场---------------------------------
             for (YzPhzPlayer player : seatMap.values()) {
                 player.setWinGold(player.getTotalPoint() * gtgDifen);
             }
             calcGroupTableGoldRoomWinLimit();
             for (ClosingPhzPlayerInfoRes.Builder builder : builderList) {
                 YzPhzPlayer player = seatMap.get(builder.getSeat());
                 builder.addStrExt(player.getWinLoseCredit() + "");      //8
                 builder.addStrExt(player.getCommissionCredit() + "");   //9
                 builder.setWinLoseCredit(player.getWinGold());
             }
         } else {
             for (ClosingPhzPlayerInfoRes.Builder builder : builderList) {
                 builder.addStrExt(0 + ""); //8
                 builder.addStrExt(0 + ""); //9
             }
         }

        for (ClosingPhzPlayerInfoRes.Builder builder : builderList) {
            list.add(builder.build());
        }

         ClosingPhzInfoRes.Builder res = ClosingPhzInfoRes.newBuilder();
         res.addAllLeftCards(leftCards);
         if (fanTypes != null) {
             res.addAllFanTypes(fanTypes);
         }
         res.addAllIntParams(intParams);
         int isHuangZhuang = 1;
         String replaceCards = "";
         if (winPlayer != null) {
             int tun = winPlayer.calcHuPoint(this);
			res.setTun(tun);// 
             res.setFan(totalFan);
             res.setHuxi(winPlayer.getTotalHu());
             res.setTotalTun(totalTun);
             res.setHuSeat(winPlayer.getSeat());

             res.setHuCard(lastCard);
             replaceCards = winPlayer.buildReplaceCards(YzPhzCardUtils.loadCardVal(fanXinCardId));
            
             System.out.println("replaceCards ======================================" + replaceCards);
             //特殊牌型补丁 王做对。醒牌不是王 
             if(loadXingMode() == 0&& fanXinCardId<=80){
             	  replaceCards = getOrepCards(fanXinCardId, winPlayer,replaceCards);
             }
           
             System.out.println("replaceCards ======================================" + replaceCards+"-------------------------------");
//             replaceCards="";
             res.addAllCards(winPlayer.buildPhzHuCards());
             isHuangZhuang = 0;
         }

         res.addAllClosingPlayers(list);
         res.setIsBreak(isBreak ? 1 : 0);
         res.setWanfa(getWanFa());
         res.addAllExt(buildAccountsExt(over, replaceCards, fanXinCardId, fanxinPoint, isHuangZhuang,fanXinCardId2));

         GeneratedMessage msg = res.build();
         for (YzPhzPlayer player : seatMap.values()) {
             player.writeSocket(msg);
         }

         return res;
    }

	private String getOrepCards(int fanXinCardId, YzPhzPlayer winPlayer, String replaceCards) {
		try {
			int dui = getDui(winPlayer);
			   if(dui>=2&&replaceCards.length()>0){
				   HashMap<Integer,Integer> repMap = new HashMap<>();
				  String[] str =   replaceCards.split(";");
				  for(String s :str){
					  int param = Integer.valueOf(s);
					  Integer val =  repMap.get(param);
					  if(val==null){
						  repMap.put(param, 1);
					  }else  {
						  repMap.put(param, val+1);
					  }
				  }
				  int repkey = 0;
				  for(Map.Entry<Integer,Integer> entry : repMap.entrySet()){
					  int val =entry.getValue();
					  int key = entry.getKey();
					  if(val ==dui){
						  repkey =key;
						  break;
					  }
				  }
				  
				 Integer val =  repMap.remove(repkey);
				 if(val!=null){
					 repMap.put(fanXinCardId, val);
					  StringBuffer sb = new StringBuffer();
					  for(Map.Entry<Integer,Integer> entry : repMap.entrySet()){
						  int value =entry.getValue();
						  int key = entry.getKey();
						  for (int i=0;i<value;i++ ) {
				              sb.append(key);
				              sb.append(";");
				          }
					  }
					  
					  if (sb.length() > 0) {
						  replaceCards = sb.substring(0, sb.length() - 1);
				      }
				 }
				 
			   }
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		return replaceCards;
	}
    
    
    
    public boolean checkHuPlayer() {
    	
    	 for (Entry<Integer, List<Integer>> kv : actionSeatMap.entrySet()) {
//    		 int tempSeat = kv.getKey().intValue();
                 List<Integer> list = kv.getValue();
                 if (list.get(0).intValue() == 1) {
                	 return true;
                	 
                 }
             }
        return false;
    }

    @Override
    public void sendAccountsMsg() {
        ClosingPhzInfoRes.Builder res = sendAccountsMsg(true, false, null, 0, null, 0, true, 0, 0,0,0);
        saveLog(true, 0l, res.build());

    }

    public List<String> buildAccountsExt(boolean isOver, String replaceCards, int fanXinCardId, int fanxinPoint, int isHuangZhuang,int fanXinCardId2) {
    	  List<String> ext = new ArrayList<>();
          ext.add(id + "");
          ext.add(masterId + "");
          ext.add(TimeUtil.formatTime(TimeUtil.now()));
          ext.add(playType + "");
          ext.add(fanxinConfig + "");
          ext.add(playBureau + "");
          ext.add(isOver ? 1 + "" : 0 + "");
          ext.add(maxPlayerCount + "");
          ext.add(replaceCards);
		ext.add(fanXinCardId + "");// 翻出来的牌
          ext.add(fanxinPoint + "");
		ext.add(fanXinCardId2 + "");// 醒牌
          
          
          
          ext.add(isHuangZhuang + "");
          //12
          //ext.add(wangbaCount + "");
          ext.add(isGroupRoom()?loadGroupId():"");//13
          ext.add(limitHu + "");
          ext.add(limitScore + "");

		// 金币场大于0
          ext.add(modeId);
        int ratio;
        int pay;
        if (isGoldRoom()) {
            ratio = GameConfigUtil.loadGoldRatio(modeId);
            pay = PayConfigUtil.get(playType, totalBureau, maxPlayerCount, payType == 1 ? 0 : 1, modeId);
        } else {
            ratio = 1;
            pay = PayConfigUtil.get(playType, totalBureau, maxPlayerCount, payType == 1 ? 0 : 1);
        }
        ext.add(String.valueOf(ratio));
        ext.add(String.valueOf(pay >= 0 ? pay : 0));

        return ext;
    }

    @Override
    public int getMaxPlayerCount() {
        return maxPlayerCount;
    }


    /**
	 * @param userId
	 * @param serverId
	 * @param playType
	 * @param tableType
	 *            0普通开房，1军团开房，2代开房
	 * @return
	 */
    public long loadRoomId(long userId, int serverId, int playType, int tableType) {
        long result;
        long t1 = System.currentTimeMillis();
        int count = 0;
        try {
            HashMap<String, Object> paramMap = new HashMap<>();
            paramMap.put("userId", userId);
            paramMap.put("serverId", serverId);
            paramMap.put("type", playType);

            int ret = 0;
            int min;
            int range;

            switch (tableType) {
                case 1:
                    min = 900000;
                    range = 100000;
                    break;
                case 2:
                    min = 800000;
                    range = 100000;
                    break;
                default:
//                    if (Math.random()<0.71428571D){
//                        min=100001;
//                        range=499999;
//                    }else{
//                        min=700000;
//                        range=200000;
//                    }
                    if (playType >= 44 && playType <= 52) {
                        min = 600000;
                        range = 100000;
                    } else {
                        if (wangbaCount == 2) {
                            min = 100001;
                            range = 199999;
                        } else if (wangbaCount == 3) {
                            min = 300000;
                            range = 100000;
                        } else if (wangbaCount == 4) {
                            min = 400000;
                            range = 100000;
                        } else {
                            if (Math.random() < 0.5D) {
                                min = 500000;
                                range = 100000;
                            } else {
                                min = 700000;
                                range = 100000;
                            }
                        }
                    }
                    break;
            }

            while (ret != 1 && count < 15) {
                count++;

                result = min + new SecureRandom().nextInt(range);

                paramMap.put("roomId", result);

                ret = TableDao.getInstance().updateRandomRoom(paramMap);
                if (ret == 1) {
                    return result;
                } else {
                    LogUtil.monitor_i("loadRoomId fail roomId:" + result);
                }
            }
        } catch (Exception e) {
            LogUtil.e("updateRandomRoom err" + e.getMessage(), e);
        } finally {
            LogUtil.monitor_i("loadRoomId time(ms):" + (System.currentTimeMillis() - t1) + ", times:" + count);
        }

        LogUtil.e("loadRoomId fail:times=" + count + ",roomId=0");

        return 0;
    }

    @Override
    public long getCreateTableId(long userId, int playType) {
        long tableId;

        if (isDaikaiTable() || isGoldRoom()) {
            tableId = daikaiTableId;
        } else if (groupTable != null && groupTable.getTableId() != null && groupTable.getTableId().intValue() > 0) {
            tableId = groupTable.getTableId().intValue();
        } else {
            int tableType;
            if (groupTableConfig != null || groupTable != null || allowGroupMember > 0 || isGroupRoom()) {
                tableType = 1;
            } else {
                tableType = 0;
            }
            tableId = loadRoomId(userId, GameServerConfig.SERVER_ID, playType, tableType);
        }

        return tableId;
    }

    @Override
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
        info.setExtend(buildExtend());
        TableDao.getInstance().save(info);
        loadFromDB(info);

        return true;
    }

    @Override
    public boolean createSimpleTable(Player player, int play, int bureauCount, List<Integer> params, List<String> strParams, boolean saveDb) throws Exception {
        int size = params.size();

       	/*
		 * if (size > 2) { int birdNum = StringUtil.getIntValue(params, 2, 2);//
		 * 王霸个数（默认2个） setWangbaCount(birdNum); }
		 * 
		 * long id = getCreateTableId(player.getUserId(), play);
		 * 
		 * if (id <= 0) { return false; }
		 * 
		 * if (saveDb) { TableInf info = new TableInf();
		 * info.setMasterId(player.getUserId()); info.setRoomId(0);
		 * info.setPlayType(play); info.setTableId(id);
		 * info.setTotalBureau(bureauCount); info.setPlayBureau(1);
		 * info.setServerId(GameServerConfig.SERVER_ID); info.setCreateTime(new
		 * Date()); info.setDaikaiTableId(daikaiTableId);
		 * info.setExtend(buildExtend()); TableDao.getInstance().save(info);
		 * loadFromDB(info); } else { setPlayType(play);
		 * setDaikaiTableId(daikaiTableId); this.id = id; this.totalBureau =
		 * bureauCount; this.playBureau = 1; }
		 * 
		 * int playerCount = StringUtil.getIntValue(params, 7, 0); if
		 * (playerCount == 0) { playerCount = 3; }
		 * setMaxPlayerCount(playerCount);
		 * 
		 * if (size > 3) { int fanXin = StringUtil.getIntValue(params, 3, 0);//
		 * 跟醒还是翻醒（0跟1翻） setFanxinConfig(fanXin); } if (size > 8) { limitHu =
		 * params.get(8); } if (size > 9) { limitScore = params.get(9); } if
		 * (size > 10) { payType = StringUtil.getIntValue(params, 10,
		 * 1);//1AA,2房主 } if (size > 11) { red2Black =
		 * StringUtil.getIntValue(params, 11, 0);//红转朱黑 } if (size > 12) {
		 * minHuxi = StringUtil.getIntValue(params, 12, 15);//最少胡息 } if (size >
		 * 13) { xingRate = StringUtil.getIntValue(params, 13, 1);//醒的倍率 if
		 * (xingRate <= 0) { xingRate = 1; } }
		 * 
		 * if (isGoldRoom()) { try { GoldRoom goldRoom =
		 * GoldRoomDao.getInstance().loadGoldRoom(id); if (goldRoom != null) {
		 * modeId = goldRoom.getModeId(); } } catch (Exception e) { } }
		 * 
		 * changeExtend();
		 * 
		 * LogUtil.msgLog.info("create simple table msg:tableId=" + getId() +
		 * ",creator=" + creatorId + ",params=" + params);
		 */
        return true;
    }

	@Override
	public void createTable(Player player, int play, int bureauCount, List<Integer> params, List<String> strParams,
			Object... objects) throws Exception {
		
		createTable(new CreateTableInfo(player, TABLE_TYPE_NORMAL, play, bureauCount, params, strParams, true));
		// int size = params.size();


	}
	
    @Override
    public boolean createTable(CreateTableInfo createTableInfo) throws Exception {
    	
    	  Player player = createTableInfo.getPlayer();
          int play = createTableInfo.getPlayType();
          int bureauCount =createTableInfo.getBureauCount();
          int tableType = createTableInfo.getTableType();
          List<Integer> params = createTableInfo.getIntParams();
          List<String> strParams = createTableInfo.getStrParams();
          //boolean saveDb = createTableInfo.isSaveDb();
    	
		payType = StringUtil.getIntValue(params, 2, 1);// 1AA,2房主
		limitHu = StringUtil.getIntValue(params, 3, 1);// 0有王必须自摸
		wangbaCount = StringUtil.getIntValue(params, 4, 2);// 王霸个数（默认2个）
		maxPlayerCount = StringUtil.getIntValue(params, 7, 3);//
		if (maxPlayerCount == 0 || maxPlayerCount == 4) {
			maxPlayerCount = 3;
		}

		int fanXin = StringUtil.getIntValue(params, 5, 0);// 跟醒还是翻醒（0跟1翻）
		setFanxinConfig(fanXin);
		limitScore = StringUtil.getIntValue(params, 8, 0);
		xingRate = StringUtil.getIntValue(params, 6, 1);// 醒的倍率
		if (xingRate == 1) {
			xingRate = 2;
		}
		tuoguan = StringUtil.getIntValue(params, 9, 0);

		autoPlay = tuoguan > 0;
		autoPlayGlob = StringUtil.getIntValue(params, 10, 0);

		// 加倍：0否，1是
		this.jiaBei = StringUtil.getIntValue(params, 11, 0);
		// 加倍分
		this.jiaBeiFen = StringUtil.getIntValue(params, 12, 0);
		// 加倍数
		this.jiaBeiShu = StringUtil.getIntValue(params, 13, 0);

		minHuxi = StringUtil.getIntValue(params, 14, 15);// 最少胡息
		red2Black = StringUtil.getIntValue(params, 15, 0);// 红转朱黑

		topFengding = StringUtil.getIntValue(params, 16, 0);// 2人总结算封顶
        if(maxPlayerCount==2){
            int belowAdd = StringUtil.getIntValue(params, 17, 0);
            if(belowAdd<=100&&belowAdd>=0)
                this.belowAdd=belowAdd;
            int below = StringUtil.getIntValue(params, 18, 0);
            if(below<=100&&below>=0){
                this.below=below;
                if(belowAdd>0&&below==0)
                    this.below=10;
            }
        }

        randomZhuangj =  StringUtil.getIntValue(params, 19, 0);// 随机庄家
		if (xingRate <= 0) {
			xingRate = 1;
		}
		if (limitScore == 1) {
			limitScore = 300;
		} else if (limitScore == 2) {
			limitScore = 600;
		} else if (limitScore == 3) {
			limitScore = 800;
		}

		if (tuoguan > 0) {
			autoTimeOut = tuoguan * 1000;
		}

		long id = getCreateTableId(player.getUserId(), play);

		if (id <= 0) {
			return false;
		}

		TableInf info = new TableInf();
		info.setMasterId(player.getUserId());
		info.setRoomId(0);
		info.setPlayType(play);
		info.setTableId(id);
		info.setTableType(tableType);
		info.setTotalBureau(bureauCount);
		info.setPlayBureau(1);
		info.setServerId(GameServerConfig.SERVER_ID);
		info.setCreateTime(new Date());
		info.setDaikaiTableId(daikaiTableId);
		info.setExtend(buildExtend());
		TableDao.getInstance().save(info);
		loadFromDB(info);


		changeExtend();
		 return true;
    }

    
	public void sendTingInfoOld(YzPhzPlayer player) {
		if(player!=null) {
			return;
		}
		long time1 = System.currentTimeMillis();
		
		int size = player.getHandCards().INS.size()+ player.getHandCards().WANGS.size();
		YzPhzHandCards cards  = new YzPhzHandCards();
		cards.INS.addAll(player.getHandCards().INS);
		cards.WANGS.addAll(player.getHandCards().WANGS);
		cards.TI.putAll(player.getHandCards().TI);
		cards.PAO.putAll(player.getHandCards().PAO);
		cards.WEI.putAll(player.getHandCards().WEI);
		cards.WEI_CHOU.putAll(player.getHandCards().WEI_CHOU);
		cards.PENG.putAll(player.getHandCards().PENG);
		cards.WEI.putAll(player.getHandCards().WEI);
		cards.WEI.putAll(player.getHandCards().WEI);
		cards.KAN.putAll(player.getHandCards().KAN);
		cards.CHI_JIAO.addAll(player.getHandCards().CHI_JIAO);
		cards.CHI_COMMON.addAll(player.getHandCards().CHI_COMMON);
		
		
		int size2 = cards.TI.size() + cards.PAO.size();
		DaPaiTingPaiRes.Builder tingInfo = DaPaiTingPaiRes.newBuilder();
		
		if (size % 3 == 0 || (size2>0 &&size % 3 == 2 )) {
			// if (actionSeatMap.containsKey(player.getSeat())) {
			// return;
			// }
			
			List<Integer> huCardList = new ArrayList<Integer>(GuihzCard.huCardList);
			huCardList.removeAll(cards.INS);
			for(int id:player.getHandCards().INS) {
				cards.INS.remove((Integer)id);
				HashMap<Integer,Integer> map = new HashMap<Integer,Integer>();
				for(Integer id2:huCardList) {
					GuihzCard card=	GuihzCard.getPaohzCard(id2);
					YzPhzCardResult cardResult = YzPhzHuPaiUtils.huPai(cards, id2, this, true, player.getUserId(), loadXingCard(false));	
					 if (cardResult.isCanHu()){
						 map.put(card.getVal(),id2);
					 }
					 
					 YzPhzCheckCardBean rets = player.checkPaoHu2(cards,id2, true, true);
					 if(rets.isHu()) {
						 map.put(card.getVal(),id2);
					 }
					 
				}
				cards.INS.add(id);
				if(map.size()==0) {
					continue;
				}
				DaPaiTingPaiInfo.Builder ting = DaPaiTingPaiInfo.newBuilder();
				ting.setMajiangId(id);
				ting.addAllTingMajiangIds(map.values());
				ting.addTingMajiangIds(81);
//				for (Integer id2 : cardIds) {
//					ting.addTingMajiangIds(id);
//				}
				tingInfo.addInfo(ting.build());
				
			}
			
//			List<GuihzCard> cards = new ArrayList<>(player.getHandGhzs());
//
//			for (GuihzCard card : player.getHandGhzs()) {
//				cards.remove(card);
//				List<GuihzCard> huCards = GuihuziTool.getTingZps(cards,player);
//				cards.add(card);
//				if (huCards == null || huCards.size() == 0) {
//					continue;
//				}
//				DaPaiTingPaiInfo.Builder ting = DaPaiTingPaiInfo.newBuilder();
//				ting.setMajiangId(card.getId());
//				for (GuihzCard mj : huCards) {
//					ting.addTingMajiangIds(mj.getId());
//				}
//				tingInfo.addInfo(ting.build());
//			}
			if (tingInfo.getInfoCount() > 0) {
				player.writeSocket(tingInfo.build());
			}
		} else {
//			for(int id:player.getHandCards().INS) {
//				cards.INS.remove((Integer)id);
				//Set<Integer> cardIds= new HashSet<Integer>();
				HashMap<Integer,Integer> map = new HashMap<Integer,Integer>();
				List<Integer> huCardList = new ArrayList<Integer>(GuihzCard.huCardList);
				huCardList.removeAll(cards.INS);
				for(Integer id:huCardList) {
					GuihzCard card=	GuihzCard.getPaohzCard(id);
					YzPhzCardResult cardResult = YzPhzHuPaiUtils.huPai(cards,id, this, true, player.getUserId(), loadXingCard(false));	
					 if (cardResult.isCanHu()){
						 map.put(card.getVal(),id);
						 //cardIds.add(id);
					 }
					 YzPhzCheckCardBean rets = player.checkPaoHu2(cards,id, true, true);
					 if(rets.isHu()) {
						 map.put(card.getVal(),id);
					 }
				}
//				cards.INS.add(id);
				if(map.size()==0) {
					return;
				}
				
				TingPaiRes.Builder ting = TingPaiRes.newBuilder();
				
//				ting.setMajiangId(id);
				ting.addAllMajiangIds(map.values());
				ting.addMajiangIds(81);
//				for (Integer id2 : cardIds) {
//					ting.addTingMajiangIds(id);
//				}
//				tingInfo.addInfo(ting.build());
			player.writeSocket(ting.build());

		}
		long time2 = System.currentTimeMillis();
		
		if(time2-time1>100) {
			LogUtil.msgLog.info("tingpai  time" + (time2-time1) +" handCrad = " + cards.INS + "wang " + cards.WANGS);
		}
	}
    
    
    
    
    @Override
    public void createTable(Player player, int play, int bureauCount, Object... objects) throws Exception {

    }

    @Override
    public int getWanFa() {
        return SharedConstants.game_type_paohuzi;
    }

    @Override
    public void checkReconnect(Player player) {
        LogUtil.msgLog.warn("check reconnect:tableId=" + getId() + ",current=" + playBureau + ",actions=" + actionSeatMap + ",userActions=" + userActionMap + ",toPlayCardFlag=" + toPlayCardFlag + ",lastCard=" + lastCard + ",currentSeat=" + disCardSeat + ",nextSeat=" + nowDisCardSeat);
        if (!recallAction(-1, -1, null)) {
            checkMo();
        }
        sendTingInfo((YzPhzPlayer) player);
    }

    
    @Override
    public void checkAutoPlay() {

        if (isTianHu()) {
            synchronized (this) {
                if (isTianHu()) {
                    Iterator<Entry<Integer, List<Integer>>> its = actionSeatMap.entrySet().iterator();
                    while (its.hasNext()) {
                        Entry<Integer, List<Integer>> kv = its.next();
                        if (kv.getKey().intValue() != disCardSeat) {
                            its.remove();
                        } else {
                            for (int i = 0, len = kv.getValue().size(); i < len; i++) {
                                if (i == 0) {
                                    if (kv.getValue().get(i).intValue() != 1) {
                                        its.remove();
                                    }
                                } else {
                                    kv.getValue().set(i, 0);
                                }
                            }
                        }
                    }
                }
            }
        }

//        if (isGoldRoom())
            synchronized (this) {
            	
            	
            	if (getSendDissTime() > 0) { 
	                for (YzPhzPlayer player : seatMap.values()) {
	                    if (player.getLastCheckTime() > 0) {
	                        player.setLastCheckTime(player.getLastCheckTime() + 1 * 1000);
	                    }
	                }
	                return;
	            }
	            if (autoPlay && state == table_state.ready && playedBureau > 0) {
	                ++timeNum;
	                for (YzPhzPlayer player : seatMap.values()) {
	                	if(player.getState()==player_state.ready) {
	                		continue;
	                	}
					// 玩家进入托管后，5秒自动准备
	                    if (timeNum >= 5 && player.isAutoPlay()) {
	                        autoReady(player);
	                    } else if (timeNum >= 30) {
	                        autoReady(player);
	                    }
	                }
//	                return;
	            }

            	
	            if(!autoPlay){
		               return;
		          }
            	
                if (state != SharedConstants.table_state.play) {
                    return;
                }
                if (actionSeatMap.size() > 0) {
                	   int action = 0,seat = 0;
                       for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()){
                           List<Integer> list = YzPhzConstants.parseToDisActionList(entry.getValue(),false);
                           if(list.isEmpty()){
                               LogUtil.msgLog.info("YzPhz|" + getId() + "|" + getPlayBureau() + "|" + entry.getKey() + "|" +entry.getValue() );
                               continue;
                           }
                           int minAction = Collections.min(list);
                           if(action == 0){
                               action = minAction;
                               seat = entry.getKey();
                           }else if(minAction < action){
                               action = minAction;
                               seat = entry.getKey();
                           }else if(minAction == action){
                               int nearSeat = getNearSeat(disCardSeat, Arrays.asList(seat, entry.getKey()));
                               seat = nearSeat;
                           }
                       }
                	
                	
                       if(action > 0 && seat > 0){
                        YzPhzPlayer player = seatMap.get(seat);
                        if (player == null) {
                            return;
                        }
                        boolean auto = player.isAutoPlay();
                        long currentTime = System.currentTimeMillis();
                        if (!auto) {
//                            if (currentTime - player.getLastOperateTime() >= autoTimeOut) {
                                if (player.getLastCheckTime() > 0) {
                                    if (currentTime - player.getLastCheckTime() >= autoTimeOut) {
                                        auto = true;
                                        player.setAutoPlay(true, this);
                                    }
                                } else {
                                    player.setLastCheckTime(currentTime);
                                }
//                            }
                        }

                        if (auto || player.isRobot()) {
                            if (player.getAutoPlayTime() == 0L) {
                                player.setAutoPlayTime(currentTime);
                            } else if (player.getAutoPlayTime() > 0L && currentTime - player.getAutoPlayTime() >= YzPhzConstants.AUTO_PLAY_TIME) {
                                player.setAutoPlayTime(0L);
                                List<Integer> actions = actionSeatMap.get(player.getSeat());
                                //List<Integer> actions = kv.getValue();
                                if (actions.get(0).intValue() == 1) {
                                    play(player, null, action_hu);
                                } else if (actions.get(9).intValue() == 1) {
                                    play(player, null, action_wangzha);
                                } else if (actions.get(8).intValue() == 1) {
                                    play(player, null, action_wangchuang);
                                } else if (actions.get(7).intValue() == 1) {
                                    play(player, null, action_wangdiao);
                                } else if (actions.get(1).intValue() == 1) {
                                    play(player, null, action_peng);
                                } else if (actions.get(4).intValue() == 1) {
                                    play(player, null, action_pass);
                                } else {
                                    play(player, null, action_pass);
                                }
                            }
                        }
                    }
                } else if (toPlayCardFlag == 1) {
                    if (nowDisCardSeat > 0) {
                        YzPhzPlayer player = seatMap.get(nowDisCardSeat);
                        if (player == null) {
                            return;
                        }
                        boolean auto = player.isAutoPlay();
                        long currentTime = System.currentTimeMillis();
                        if (!auto) {
//                            if (currentTime - player.getLastOperateTime() >= autoTimeOut) {
                                if (player.getLastCheckTime() > 0) {
                                    if (currentTime - player.getLastCheckTime() >= autoTimeOut) {
                                        auto = true;
                                        player.setAutoPlay(true, this);
                                    }
                                } else {
                                    player.setLastCheckTime(currentTime);
                                    player.setAutoPlayCheckedTimeAdded(false);
                                }
//                            }
                        }

                        if (auto || player.isRobot()) {
                            if (player.getAutoPlayTime() == 0L) {
                                player.setAutoPlayTime(currentTime);
                            } else if (player.getAutoPlayTime() > 0L && currentTime - player.getAutoPlayTime() >= 10) {
                                player.setAutoPlayTime(0L);
                                List<Integer> cards0 = player.getHandCards().INS;
                                Integer card = null;
                                for (int i = 0, len = cards0.size() - 1; i < len; i++) {
                                    if (YzPhzCardUtils.loadCardVal(cards0.get(i)) != YzPhzCardUtils.loadCardVal(cards0.get(i + 1))) {
                                        card = cards0.get(i);
                                        break;
                                    } else {
                                        i++;
                                    }
                                }
                                if (card == null) {
                                    card = cards0.get(cards0.size() - 1);
                                }
                                List<Integer> cards = new ArrayList<>(1);
                                cards.add(card);
                                play(player, cards, 0);
                            }
                        }
                    }
                }
            }
    }
    @Override
    public Class<? extends Player> getPlayerClass() {
        return YzPhzPlayer.class;
    }

    public Integer getNextCard() {
        synchronized (cardLock) {
            if (this.leftCards.size() > 0) {
                Integer card = this.leftCards.remove(0);
                dbParamMap.put("leftPais", JSON_TAG);
                return card;
            }
        }
        return null;
    }

    public Integer getNextLeftCard() {
        synchronized (cardLock) {
            if (this.leftCards.size() > 0) {
                Integer card = this.leftCards.get(0);
                return card;
            }
        }
        return null;
    }

    public List<Integer> getLeftCards() {
        return leftCards;
    }

    public void setLeftCards(List<Integer> leftCards) {
        if (leftCards == null) {
            this.leftCards.clear();
        } else {
            this.leftCards = leftCards;

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

    public List<Integer> getNowDisCardIds() {
        return nowDisCardIds;
    }

    public void setNowDisCardIds(List<Integer> nowDisCardIds) {
        this.nowDisCardIds = nowDisCardIds;
        dbParamMap.put("nowDisCardIds", JSON_TAG);
    }

    /**
	 * 打出的牌是刚刚摸的
	 *
	 * @return
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

    public int getLastCard() {
        return lastCard;
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

    public void clearMoSeatPair() {
        moSeatPair = null;
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

    public Integer getZaiCard() {
        return zaiCard;
    }

    public void setZaiCard(Integer zaiCard) {
        this.zaiCard = zaiCard;
        changeExtend();
    }

    public void setSendPaoSeat(int sendPaoSeat) {
        if (this.sendPaoSeat != sendPaoSeat) {
            this.sendPaoSeat = sendPaoSeat;
            changeExtend();
        }

    }

    public boolean isBoPi() {
        return false;
    }

    public Map<Integer, List<Integer>> getActionSeatMap() {
        return actionSeatMap;
    }

    public void setFirstCard(boolean firstCard) {
        this.firstCard = firstCard;
        changeExtend();
    }

    public Integer getBeRemoveCard() {
        return beRemoveCard;
    }

    /**
	 * 桌子上移除的牌
	 *
	 * @param beRemoveCard
	 */
    public void setBeRemoveCard(Integer beRemoveCard) {
        this.beRemoveCard = beRemoveCard;
        changeExtend();
    }

    public void setMaxPlayerCount(int maxPlayerCount) {
        this.maxPlayerCount = maxPlayerCount;
        changeExtend();
    }

    public int getShuXingSeat() {
        return shuXingSeat;
    }
//
//    public void setShuXingSeat(int shuXingSeat) {
//        this.shuXingSeat = shuXingSeat;
//        changeExtend();
//    }

    public boolean hasZuoXing() {
        if (4 == getMaxPlayerCount()) {
            return true;
        }
        return false;
    }

    public int getWangbaCount() {
        return wangbaCount;
    }

    public void setWangbaCount(int wangbaCount) {
        this.wangbaCount = wangbaCount;
        changeExtend();
    }

    public void setFanxinConfig(int fanxinConfig) {
        this.fanxinConfig = fanxinConfig;
        changeExtend();
    }

    public int getDisposeCard() {
        return disposeCard;
    }

    public void setDisposeCard(int disposeCard) {
        this.disposeCard = disposeCard;
        changeExtend();
    }

    public void markLastSeatAction(int seat, int action) {
        lastSeatAction = new KeyValuePair<>();
        lastSeatAction.setId(seat);
        lastSeatAction.setValue(action);
        changeExtend();
    }

    public void clearLastSeatAction() {
        this.lastSeatAction = null;
        changeExtend();
    }

    public void setHuCard(int huCard) {
        this.huCard = huCard;
        changeExtend();
    }

    public void setPaoHu(int paoHu) {
        this.paoHu = paoHu;
        changeExtend();
    }

    public void changeMoCount(int moCount) {
        this.moCount += moCount;
        changeExtend();
    }

    public void changeDisCount(int disCount) {
        this.disCount += disCount;
        changeExtend();
    }

    public int getMoCount() {
        return moCount;
    }

    public int getDisCount() {
        return disCount;
    }

    public int getHuCard() {
        return huCard;
    }


    @Override
    public int loadQihuxi() {
        return minHuxi;
    }

    @Override
    public int loadRedCount() {
        return 10;
    }

    @Override
    public boolean checkRed2Black() {
        return red2Black == 1;
    }

    @Override
    public boolean checkRed2Dian() {
        return red2Black == 1;
    }

    @Override
    public int loadRed2BlackCount() {
        return 15;
    }

    @Override
    public int loadRed2DianCount() {
        return 13;
    }

    @Override
    public int loadBaseTun() {
        return (loadQihuxi() / 3) - 3;
    }

    @Override
    public int loadCommonTun(int totalHuxi) {
        return ((totalHuxi - loadQihuxi()) / 3);
    }

    @Override
    public int loadScoreTun(int totalHuxi) {
    	return (totalHuxi / 3);
    }

    @Override
    public int loadXingMode() {
        return fanxinConfig;
    }

    @Override
    public int loadXingTun(int xingCount) {
        return xingCount * xingRate;
    }

    @Override
    public int loadXingCard(boolean tiqian) {
        if (loadXingMode() == 0) {
            if (tiqian) {
                return leftCards.get(0);
            }
            return lastCard;
        } else {
            return tiqian ? (leftCards.size() >= 2 ? leftCards.get(1) : leftCards.get(0)) : (leftCards.size() >= 1 ? leftCards.get(0) : lastCard);
        }
    }

    @Override
    public int loadHuMode() {
        return limitHu;
    }
    
    public String getTableMsg() {
        Map<String, Object> json = new HashMap<>();
       
		json.put("wanFa", "落地扫");
        if (isGroupRoom()) {
            json.put("roomName", getRoomName());
        }
        json.put("playerCount", getPlayerCount());
            json.put("count", getTotalBureau());
        if (this.autoPlay) {
            json.put("autoTime", autoTimeOut / 1000);
            if (autoPlayGlob == 1) {
				json.put("autoName", "单局");
            }else if (autoPlayGlob == 3) {
				json.put("autoName", "3局");
			}
            else {
				json.put("autoName", "整局");
            }
        }
        return JSON.toJSONString(json);
    }

    @Override
    public boolean isTianHu() {
        return moCount == 0 && disCount == 0;
    }

    public void sendTingInfo(YzPhzPlayer player) {
        if (true) {
            return;
        }
        int size = player.getHandCards().INS.size() + player.getHandCards().WANGS.size();
        int size2 = player.getHandCards().TI.size() + player.getHandCards().PAO.size();

        if (size % 3 == 0 || (size2 > 0 && size % 3 == 2)) {
            sendDaTingMap(player);
        } else {
            sendTingList(player);
        }
    }

    /**
     * 打牌听牌信息
     * @param player
     */
    public void sendDaTingMap(YzPhzPlayer player) {
        long start = System.currentTimeMillis();
        int laiZiNum = player.getHandCards().WANGS.size();
        List<Integer> ins = player.getHandCards().INS;
        int[] cardArr = new int[21];
        for (int id : ins) {
            GuihzCard card = GuihzCard.getPaohzCard(id);
            if (card != null) {
                cardArr[ZpConstant.out2In(card.getVal())] += 1;
            }
        }
        ZpHuBean huBean = new ZpHuBean(cardArr, laiZiNum);
        Map<Integer, List<Integer>> tingMap = huBean.calcDaTingMap();
        if (tingMap.size() > 0) {
            DaPaiTingPaiRes.Builder tingInfo = DaPaiTingPaiRes.newBuilder();
            for (int id : ins) {
                GuihzCard disCard = GuihzCard.getPaohzCard(id);
                if (disCard == null) {
                    continue;
                }
                int disCardVal = ZpConstant.out2In(disCard.getVal());
                List<Integer> tingVals = tingMap.get(disCardVal);
                if (tingVals == null || tingVals.size() == 0) {
                    continue;
                }
                DaPaiTingPaiInfo.Builder ting = DaPaiTingPaiInfo.newBuilder();
                ting.setMajiangId(id);
                List<Integer> tingIds = new ArrayList<>();
                for (int tingVal : tingVals) {
                    tingVal = ZpConstant.in2Out(tingVal);
                    GuihzCard tmp = GuihzCard.getGuihzCard(tingVal);
                    if (tmp != null) {
                        tingIds.add(tmp.getId());
                    }
                }
                ting.addAllTingMajiangIds(tingIds);
                tingInfo.addInfo(ting.build());
            }
            player.writeSocket(tingInfo.build());
        }
        long timeUse = System.currentTimeMillis() - start;
        if (timeUse > 10) {
            LogUtil.msgLog.info("yzphz|sendTing|sendDaTingMap|" + timeUse + "|" + getId() + "|" + player.getUserId() + "|" + player.getHandCards().INS + "|" + player.getHandCards().WANGS);
        }
    }

    /**
     * 听牌信息：不需要打牌
     * @param player
     */
    public void sendTingList(YzPhzPlayer player) {
        long start = System.currentTimeMillis();
        int laiZiNum = player.getHandCards().WANGS.size();
        List<Integer> ins = player.getHandCards().INS;
        int[] cardArr = new int[21];
        for (int id : ins) {
            GuihzCard card = GuihzCard.getPaohzCard(id);
            if (card != null) {
                cardArr[ZpConstant.out2In(card.getVal())] += 1;
            }
        }
        ZpHuBean huBean = new ZpHuBean(cardArr, laiZiNum);
        List<Integer> tingVals = huBean.calcTingList();
        if (tingVals != null && tingVals.size() > 0) {
            DaPaiTingPaiRes.Builder tingInfo = DaPaiTingPaiRes.newBuilder();
            DaPaiTingPaiInfo.Builder ting = DaPaiTingPaiInfo.newBuilder();
            ting.setMajiangId(81);
            List<Integer> tingIds = new ArrayList<>();
            for (int tingVal : tingVals) {
                tingVal = ZpConstant.in2Out(tingVal);
                GuihzCard tmp = GuihzCard.getGuihzCard(tingVal);
                if (tmp != null) {
                    tingIds.add(tmp.getId());
                }
            }
            ting.addAllTingMajiangIds(tingIds);
            tingInfo.addInfo(ting.build());
            player.writeSocket(tingInfo.build());
        }
        long timeUse = System.currentTimeMillis() - start;
        if (timeUse > 10) {
            LogUtil.msgLog.info("yzphz|sendTing|sendTingList|" + timeUse + "|" + getId() + "|" + player.getUserId() + "|" + player.getHandCards().INS + "|" + player.getHandCards().WANGS);
        }
    }
    
    public void logTotalPointMsg(YzPhzPlayer player,int logIndex){
    	StringBuilder sb = new StringBuilder("YzPhz|logTotalPoint");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getTotalPoint());
        sb.append("|").append(player.getPoint());
        sb.append("|").append(logIndex);
        LogUtil.msgLog.info(sb.toString());
    }


    public static final List<Integer> wanfaList = Arrays.asList(
    		  GameUtil.play_type_YZPHZ
    );

    public static void loadWanfaTables(Class<? extends BaseTable> cls) {
        for (Integer integer : wanfaList) {
            TableManager.wanfaTableTypesPut(integer, cls);
        }
    }
}
