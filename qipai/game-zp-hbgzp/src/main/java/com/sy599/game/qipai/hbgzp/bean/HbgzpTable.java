package com.sy599.game.qipai.hbgzp.bean;

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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.google.protobuf.GeneratedMessage;
import com.sy599.game.GameServerConfig;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
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
import com.sy599.game.msg.serverPacket.PlayCardResMsg.DaPaiTingPaiInfo;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.DaPaiTingPaiRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.MoMajiangRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.PlayMajiangRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.PlayPaohuziRes;
import com.sy599.game.msg.serverPacket.TablePhzResMsg;
import com.sy599.game.msg.serverPacket.TablePhzResMsg.ClosingPhzInfoRes;
import com.sy599.game.msg.serverPacket.TablePhzResMsg.ClosingPhzPlayerInfoRes;
import com.sy599.game.msg.serverPacket.TableRes;
import com.sy599.game.msg.serverPacket.TableRes.CreateTableRes;
import com.sy599.game.msg.serverPacket.TableRes.DealInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes;
import com.sy599.game.qipai.hbgzp.constant.HbgzpConstants;
import com.sy599.game.qipai.hbgzp.rule.Hbgzp;
import com.sy599.game.qipai.hbgzp.rule.HbgzpRobotAI;
import com.sy599.game.qipai.hbgzp.tool.HbgzpHelper;
import com.sy599.game.qipai.hbgzp.tool.HbgzpHuLack;
import com.sy599.game.qipai.hbgzp.tool.HbgzpResTool;
import com.sy599.game.qipai.hbgzp.tool.HbgzpTool;
import com.sy599.game.udplog.UdpLogger;
import com.sy599.game.util.GameConfigUtil;
import com.sy599.game.util.GameUtil;
import com.sy599.game.util.JacksonUtil;
import com.sy599.game.util.JsonWrapper;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.PayConfigUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.util.StringUtil;
import com.sy599.game.util.TimeUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;


public class HbgzpTable extends BaseTable {
    /**
     * 当前打出的牌
     */
    private List<Hbgzp> nowDisCardIds = new ArrayList<>();
    protected List<Integer> dices;
    private Map<Integer, List<Integer>> actionSeatMap = new ConcurrentHashMap<>();
    /**
     * 玩家位置对应临时操作
     * 当同时存在多个可做的操作时
     * 1当优先级最高的玩家最先操作 则清理所有操作提示 并执行该操作
     * 2当操作优先级低的玩家先选择操作，则玩家先将所做操作存入临时操作中 当玩家优先级最高的玩家操作后 在判断临时操作是否可执行并执行
     */
    private Map<Integer, HbgzpTempAction> tempActionMap = new ConcurrentHashMap<>();
    private int maxPlayerCount = 4;
    private List<Hbgzp> leftMajiangs = new ArrayList<>();
    /*** 玩家map */
    private Map<Long, HbgzpPlayer> playerMap = new ConcurrentHashMap<Long, HbgzpPlayer>();
    /*** 座位对应的玩家 */
    private Map<Integer, HbgzpPlayer> seatMap = new ConcurrentHashMap<Integer, HbgzpPlayer>();
    private List<Integer> huConfirmList = new ArrayList<>();//胡牌数组
    /**
     * 摸麻将的seat
     */
    private int moMajiangSeat;
    /**
     * 抢杠胡   杠的麻将
     */
    private Hbgzp moGang;
    /**
     * 当前杠的人
     */
    private int moGangSeat;
    private int moGangSameCount;

    /**
     * 抢杠胡 位置
     */
    private List<Integer> moGangHuList = new ArrayList<>();
    

	/** 是否是开局 **/
	private boolean isBegin = false;
    /**
     * 放杠人位置 如果是自己摸来位置为自己
     */
    private int fangGangSeat;
    /**
     * 杠开胡 位置
     */
    private int gangKaiSeat;
    /**
     * 骰子点数
     **/
    private int dealDice;

    private int tableStatus;//特殊状态 1锤
    private int isAutoPlay;//是否开启自动托管
    
    private int hupaizi=7;//胡牌要的子
    private int shihua =0;//1十个花 还是 2溜花
    private int daidingpao = 0;//1 带跑 2定跑
    private int paofen = 0;//跑分
    private int yipaoduoxiang = 0;//一炮多响
    // 是否已发送飘分信息
    private int isSendPiaoFenMsg=0;
    // 是否已发牌
    private int finishFapai=0;
    private int lowfen =0;//低于多少分
    private int lowfenAdd = 0;//加多少分
    
    private volatile Hbgzp huCard;
    
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
    	if (getState() != table_state.play) {
            return; 
		}
		if(getPlayBureau() >= getMaxPlayerCount()){
		  changeTableState(table_state.over);
		}
    	List<Integer> winList = new ArrayList<>(huConfirmList);
        boolean selfMo = false;
        int winfen = 0;
        int totalTun = 0;
        if (winList.size() == 0 && leftMajiangs.isEmpty()) {
            // 流局
        } else {
            // 先判断是自摸还是放炮
            if (winList.size() == 1 && seatMap.get(winList.get(0)).getHandMajiang().size() % 3 == 2 && winList.get(0) == moMajiangSeat) {
                selfMo = true;
            }
            if (selfMo) {
                // 自摸
                HbgzpPlayer winPlayer = seatMap.get(winList.get(0));
                int huxi=winPlayer.getTotalHu();
//                huxi += getAllPaiSuanZi(winPlayer);
                if(huxi > winPlayer.getSingleMaxHuxi()){
                	winPlayer.setSingleMaxHuxi(huxi);
                }
                int tun = countXiTun(huxi, winPlayer);
                totalTun = tun;
                int winPoint = 0;
                for (int seat : seatMap.keySet()) {
                    if (!winList.contains(seat)) {
                        int losePoint = tun;
                        losePoint += winPlayer.getPiaoFen()>0?winPlayer.getPiaoFen():0;
                        
//                        losePoint *= diFen;
                        //输家锤
                        HbgzpPlayer player = seatMap.get(seat);
                        losePoint += player.getPiaoFen()>0?player.getPiaoFen():0;
                        winPoint += losePoint;
                        player.calcResult(this, 1, -losePoint, false);
//            			player.changeLostPoint(-losePoint);
                    }
                }
                winfen = winPoint;
                winPlayer.changeAction(HbgzpConstants.ACTION_COUNT_INDEX_ZIMO, 1);
//                winPlayer.changeLostPoint(winPoint);
                winPlayer.calcResult(this, 1, winPoint, false);
            } else {
                // 小胡接炮 每人1分
                // 如果庄家输牌失分翻倍
                HbgzpPlayer losePlayer = seatMap.get(disCardSeat);
                int loseTotalPoint = 0;

                for (int winnerSeat : winList) {
                    HbgzpPlayer winPlayer = seatMap.get(winnerSeat);
                    int huxi=winPlayer.getTotalHu();
//                    huxi += getAllPaiSuanZi(winPlayer);
                    if(huxi > winPlayer.getSingleMaxHuxi()){
                    	winPlayer.setSingleMaxHuxi(huxi);
                    }
                    int tun = countXiTun(huxi, winPlayer);
                    totalTun +=tun;
                    int winPoint = 0;
//                    for (int playerSeat : seatMap.keySet()) {
//                        if (playerSeat == winnerSeat) {
//                            continue;
//                        }
//
//                        int losePoint = tun;
//                        winPoint = losePoint;
//
//                        loseTotalPoint += losePoint;
//                    }
                    int losePoint = tun;
                    losePoint+=winPlayer.getPiaoFen() > 0?winPlayer.getPiaoFen():0;
                    losePoint+=losePlayer.getPiaoFen() > 0?losePlayer.getPiaoFen():0;
                    winPoint = losePoint;
                    winfen += winPoint;
                    loseTotalPoint += losePoint;
                    //winPlayer.changeLostPoint(winPoint);
                    winPlayer.calcResult(this, 1, losePoint, false);
                    winPlayer.changeAction(HbgzpConstants.ACTION_COUNT_INDEX_JIEPAO, 1);
                    losePlayer.changeAction(HbgzpConstants.ACTION_COUNT_INDEX_DIANPAO, 1);
                }
//                losePlayer.changeLostPoint(-loseTotalPoint);
                losePlayer.calcResult(this, 1, -loseTotalPoint, false);
            }
        }
        // 不管流局都加分
//        for (HbgzpPlayer seat : seatMap.values()) {
//            seat.changePoint(seat.getLostPoint());
//        }
        
		boolean isOver = playBureau >= totalBureau;
		
        if(autoPlayGlob >0) {
//          //是否解散
            boolean diss = false;
            if(autoPlayGlob ==1) {
            	 for (HbgzpPlayer seat : seatMap.values()) {
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
        ClosingPhzInfoRes.Builder res = sendAccountsMsg(isOver, winList, winfen, totalTun, false);
//        ClosingPhzInfoRes.Builder res = sendAccountsMsg(isOver, selfMo, winList,  false);
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
//        if (getKeChui() == 1) {
//            for (int seat : seatMap.keySet()) {
//                HbgzpPlayer player = seatMap.get(seat);
//                if (player.getChui() == 1 && winList.contains(seat)) {
//                    continue;
//                }
//                player.setChui(-1);
//            }
//        }
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
        for (HbgzpPlayer player : seatMap.values()) {
            if (player.isAutoPlaySelf()) {
                player.setAutoPlay(false, false);
            }
        }
        for (Player player : seatMap.values()) {
            player.saveBaseInfo();
        }
    }
    
    /**
     * 获取花牌个数
     * @return
     */
    private int getHuapaiCount(HbgzpPlayer winPlayer){
    	if(winPlayer == null){
    		return 0;
    	}
    	int num = 0;
    	for (HbgzpCardDisType type:winPlayer.getCardTypes()){
    		for(Integer id:type.getCardIds()){
    			if(Hbgzp.isHuapai(id)){
    				num++;
    			}
    		}
    	}
    	List<Hbgzp> handCards = HbgzpTool.toPhzCards(winPlayer.getHandPais());
    	for (Hbgzp hbgzp:handCards) {
    		if(Hbgzp.isHuapai(hbgzp.getId())){
				num++;
			}
    	}
    	if(huCard != null){
    		if(Hbgzp.isHuapai(huCard.getId())){
				num++;
			}
    	}
    	return num;
    }
    
    
    public void saveLog(boolean over, long winId, Object resObject) {
        ClosingPhzInfoRes res = (ClosingPhzInfoRes) resObject;
        LogUtil.d_msg("tableId:" + id + " play:" + playBureau + " over:" + res);
        String logRes = JacksonUtil.writeValueAsString(LogUtil.buildClosingInfoResLog(res));
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
        for (HbgzpPlayer player : playerMap.values()) {
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
                tempMap.put("nowDisCardIds", StringUtil.implode(HbgzpHelper.toMajiangIds(nowDisCardIds), ","));
            }
            if (tempMap.containsKey("leftPais")) {
                tempMap.put("leftPais", StringUtil.implode(HbgzpHelper.toMajiangIds(leftMajiangs), ","));
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
        for (HbgzpPlayer player : seatMap.values()) {
            wrapper.putString(player.getSeat(), player.toExtendStr());
        }
        wrapper.putString(5, StringUtil.implode(huConfirmList, ","));
        wrapper.putInt(7, moMajiangSeat);
        if (moGang != null) {
            wrapper.putInt(8, moGang.getId());
        } else {
            wrapper.putInt(8, 0);
        }
        wrapper.putString(9, StringUtil.implode(moGangHuList, ","));
//        wrapper.putInt(12, keChui);
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
        
        wrapper.putInt(18, hupaizi);//胡牌要的子
        wrapper.putInt(19, shihua);//1十个花 还是 2溜花
        wrapper.putInt(20, daidingpao);//1 带跑 2定跑
        wrapper.putInt(21, paofen);//跑分
        wrapper.putInt(22, yipaoduoxiang);//一炮多响
        wrapper.putInt(23, lowfen);//低于多少分
        wrapper.putInt(24, lowfenAdd);//加多少分
        
        
        wrapper.putInt(27, moGangSeat);
        wrapper.putInt(28, moGangSameCount);
        wrapper.putInt(29, isAutoPlay);
        wrapper.putString(30, StringUtil.implode(moTailPai, ","));
        wrapper.putInt(31, diFen);
		wrapper.putInt(32, jiaBei);
		wrapper.putInt(33, jiaBeiFen);
		wrapper.putInt(34, jiaBeiShu);
		wrapper.putInt(35, autoPlayGlob);
        wrapper.putInt(36, finishFapai);
        wrapper.putInt(37, isSendPiaoFenMsg);
        wrapper.putInt(38, isBegin ? 1 : 0);
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
//        addPlayLog(disCardRound + "_" + lastWinSeat + "_" + HbgzpDisAction.action_dice + "_" + dealDice);
        setDealDice(dealDice);
        // 天胡或者暗杠
        logFaPaiTable();
        //TODO 从庄家的下一位开始判断是否有需要 观或滑的
//        for (int i = 0; i < maxPlayerCount-1; i++) {
//        	int nextSeat =  calcNextSeat(lastWinSeat);
//        	HbgzpPlayer player = (HbgzpPlayer) getPlayerBySeat(nextSeat);
//        	List<Integer> actionList = player.checkMo(null);
//        	List<Integer>  arr = HbgzpDisAction.keepGuanHua(actionList);
//        	
//        	
//        }
        
        
        
        for (HbgzpPlayer tablePlayer : seatMap.values()) {
            DealInfoRes.Builder res = DealInfoRes.newBuilder();
//            if (lastWinSeat == tablePlayer.getSeat()) {
                List<Integer> actionList = null;
                if(lastWinSeat != tablePlayer.getSeat()){
                	actionList = tablePlayer.checkMoQishou(null);
//                	actionList = HbgzpDisAction.keepGuanHua(actionList);
                }else{
                	actionList = tablePlayer.checkMo(null);
                }
                if (!actionList.isEmpty()) {
                    addActionSeat(tablePlayer.getSeat(), actionList);
                    res.addAllSelfAct(actionList);
                    logFaPaiPlayer(tablePlayer, actionList);
                }
//            }
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
//            	addPlayLog(getDisCardRound() + "_" +tablePlayer.getSeat() + "_" + HbgzpConstants.action_tuoguan + "_" +1);
            	addPlayLog(tablePlayer.getSeat() + "_" + HbgzpConstants.action_tuoguan + "_" +1);
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
        isBegin = true;
    }
    public void setIsBegin(boolean begin) {
		if (isBegin != begin) {
			isBegin = begin;
			changeExtend();
		}
	}
    public void moMajiang(HbgzpPlayer player) {
        moMajiang(player, false);
    }

    public void moMajiang(HbgzpPlayer player, boolean isBuZhang) {
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
        Hbgzp majiang = null;
        if (disCardRound != 0) {
            // 玩家手上的牌是双数，已经摸过牌了
            if (!isBegin() && player.isAlreadyMoMajiang()) {
                return;
            }
            if (getLeftMajiangCount() == 0) {
                calcOver();
                return;
            }
            majiang = getLeftMajiang();
        }
        if (majiang != null) {
//            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + HbgzpDisAction.action_moMjiang + "_" + majiang.getId() + player.getExtraPlayLog());
            addPlayLog(player.getSeat() + "_" + HbgzpDisAction.action_moMjiang + "_" + majiang.getId());
            player.moMajiang(majiang);
        }
        // 检查摸牌
        if(!isBegin()){
        	clearActionSeatMap();
        }
        if (disCardRound == 0) {
            return;
        }
        if (isBuZhang) {
            addMoTailPai(-1);
        }

        setMoMajiangSeat(player.getSeat());
        List<Integer> arr = new ArrayList<>();
        if(isBegin() && !player.isAlreadyMoMajiang()){
        	arr = player.checkMoQishou(majiang);
        }else{
        	arr = player.checkMo(majiang);
        }
        if (!arr.isEmpty()) {
            addActionSeat(player.getSeat(), arr);
            if (isBuZhang && (arr.get(HbgzpConstants.ACTION_INDEX_HU) == 1 || arr.get(HbgzpConstants.ACTION_INDEX_ZIMO) == 1)) {
                //杠开位置
                setGangKaiSeat(player.getSeat());
            }
        }
        logMoMj(player, majiang, arr);
        MoMajiangRes.Builder res = MoMajiangRes.newBuilder();
        res.setUserId(player.getUserId() + "");
        res.setSeat(player.getSeat());
        for (HbgzpPlayer seat : seatMap.values()) {
            if (seat.getUserId() == player.getUserId()) {
                MoMajiangRes.Builder copy = res.clone();
                copy.addAllSelfAct(arr);
                copy.addSelfAct(player.getZhaCount());
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
    private void hu(HbgzpPlayer player, List<Hbgzp> majiangs, int action) {
        if (state != table_state.play) {
            return;
        }
        List<Integer> actionList = actionSeatMap.get(player.getSeat());
        if (actionList == null || (actionList.get(HbgzpConstants.ACTION_INDEX_HU) != 1 && actionList.get(HbgzpConstants.ACTION_INDEX_ZIMO) != 1)) {// 如果集合为空或者第一操作不为胡，则返回
            return;
        }
//        if(yipaoduoxiang == 0){
//        	if (!checkAction(player, majiangs, new ArrayList<Integer>(), action)) {// 检查优先度，胡杠碰吃 如果同时出现一个事件，按出牌座位顺序优先
//    			player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip);
//    			LogUtil.msg("有优先级更高的操作需等待！");
//    			return;
//    		}//一炮多响去掉
//        }else{
//        	if (huConfirmList.contains(player.getSeat())) {
//                return;
//            }
//        }
		
        if (!checkAction(player, majiangs, new ArrayList<Integer>(), action)) {// 检查优先度，胡杠碰吃 如果同时出现一个事件，按出牌座位顺序优先
			player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip);
			LogUtil.msg("有优先级更高的操作需等待！");
			return;
		}
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        List<Hbgzp> huHand = new ArrayList<>(player.getHandMajiang());
        boolean zimo = player.isAlreadyMoMajiang();
        Hbgzp disMajiang = null;
        if (!zimo) {
            if (moGangHuList.contains(player.getSeat())) {
                // 抢杠胡
                huHand.add(moGang);
                disMajiang = moGang;
                builder.setFromSeat(nowDisCardSeat);
                player.getDahu().add(HbgzpConstants.HU_QIANGGANGHU);
                HbgzpPlayer fangPaoPlayer = seatMap.get(nowDisCardSeat);
                fangPaoPlayer.getDahu().add(HbgzpConstants.HU_FANGPAO);
            } else {
                // 放炮
                huHand.addAll(nowDisCardIds);
                disMajiang = nowDisCardIds.get(0);
                builder.setFromSeat(disCardSeat);
                player.getDahu().add(HbgzpConstants.HU_JIPAO);
                HbgzpPlayer gangPaoPlayer = seatMap.get(disCardSeat);
                gangPaoPlayer.getDahu().add(HbgzpConstants.HU_GANGPAO);
            }
        } else {
            if (getGangKaiSeat() == player.getSeat()) {
                player.getDahu().add(HbgzpConstants.HU_GANGKAI);
                if (player.getSeat() != getFangGangSeat()) {
                    HbgzpPlayer fangGangPlayer = seatMap.get(getFangGangSeat());
                    fangGangPlayer.getDahu().add(HbgzpConstants.HU_DIANGANG);
                }
            } else {
                player.getDahu().add(HbgzpConstants.HU_ZIMO);
            }
        }
        
        List<HbgzpHuLack> ping = player.checkHu1(disMajiang, zimo);
        HbgzpHuLack hu = filtrateHu(player, null, ping,disMajiang);
//        if (hu!=null&&hu.isHu()) {
//        	huCard = disMajiang;
//        	player.setHuxi(hu.getHuxi());
//            player.setHu(hu);
//            huConfirmList.add(player.getSeat());
//            addPlayLog(player.getSeat(), action + "", PaohuziTool.implodePhz(majiangs, ","));
//            sendActionMsg(player, action, null, HbgzpDisAction.action_type_action);
//            calcOver();
//        } else {
//            broadMsg(player.getName() + " 不能胡牌");
//        }
        
        
//        HbgzpHu hu = HbgzpTool.isHu(player, disMajiang);
        if (hu==null || !hu.isHu()) {
            return;
        } 
        huCard = disMajiang;
        player.setHuxi(hu.getHuxi());
        player.setHu(hu);
        if (moGangHuList.contains(player.getSeat())) {
        	HbgzpPlayer moGangPlayer = seatMap.get(moGangSeat);
            if (moGangPlayer == null) {
                moGangPlayer = getPlayerByHasMajiang(moGang);
            }
            if (moGangPlayer == null) {
                moGangPlayer = seatMap.get(moMajiangSeat);
            }
            List<Hbgzp> moGangMajiangs = new ArrayList<>();
            moGangMajiangs.add(moGang);
            moGangPlayer.addOutPais(moGangMajiangs, 0, 0);
            // 摸杠被人胡了 相当于自己出了一张牌
            recordDisMajiang(moGangMajiangs, moGangPlayer);
            moGangPlayer.qGangUpdateOutPais(moGang);
        }
        buildPlayRes(builder, player, action, huHand);
        if (zimo) {
            builder.setZimo(1);
            huCard = player.getLastMoMajiang();
        }
        if (!huConfirmList.isEmpty()) {
            builder.addExt(StringUtil.implode(huConfirmList, ","));
        }
        // 胡
        for (HbgzpPlayer seat : seatMap.values()) {
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
        List<Hbgzp> huPai = new ArrayList<>();
        huPai.add(huHand.get(huHand.size() - 1));
//        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + HbgzpHelper.toMajiangStrs(huPai) + "_" + StringUtil.implode(player.getDahu(), ",") + player.getExtraPlayLog());
        addPlayLog(player.getSeat(), action + "", HbgzpHelper.toMajiangStrs(huPai));
        logActionHu(player, majiangs, "");
        //if (isCalcOver()) {
            // 等待别人胡牌 如果都确认完了，胡
            calcOver();
//        } else {
//            //removeActionSeat(player.getSeat());
//            player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip, action);
//        }
    }

    /**
     * 发送动作msg
     *
     * @param player
     * @param action
     * @param cards
     * @param actType
     */
    private void sendActionMsg(HbgzpPlayer player, int action, List<Hbgzp> cards, int actType) {
        PlayPaohuziRes.Builder builder = PlayPaohuziRes.newBuilder();
        builder.setAction(action);
//        if(action == HbgzpDisAction.action_hu && isSelfMo(player)){
//        	builder.setAction(HbgzpDisAction.action_zimo);
//        }
//        if(action == HbgzpDisAction.action_hu && disOrMo==1){
//        	builder.setAction(HbgzpDisAction.action_fangpao);
//        }
        builder.setUserId(player.getUserId() + "");
        builder.setSeat(player.getSeat());
        builder.setHuxi(player.getOutHuxi());
        setNextSeatMsg(builder,false);
//        if (leftCards != null) {//剩余底牌
//            builder.setRemain(leftCards.size());
//        }
        builder.addAllPhzIds(HbgzpTool.toPhzCardIds(cards));
        builder.setActType(actType);
        sendMsgBySelfAction(builder);
    }
    private void setNextSeatMsg(PlayPaohuziRes.Builder builder,boolean shiZhongHu) {
        builder.setTimeSeat(nowDisCardSeat);
//        if (toPlayCardFlag == 1&&!shiZhongHu) {
//            builder.setNextSeat(nowDisCardSeat);
//        } else{
            builder.setNextSeat(0);
//        }

    }
    
    /**
     * 发送消息带入自己动作
     *
     * @param builder
     */
    private void sendMsgBySelfAction(PlayPaohuziRes.Builder builder) {

        int actType = builder.getActType();
        boolean noShow = false;
        int paoSeat = 0;
        if (HbgzpDisAction.action_type_dis == actType || HbgzpDisAction.action_type_mo == actType) {
            for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
                if (1 == entry.getValue().get(5)) {
                    noShow = true;
                    paoSeat = entry.getKey();
                }
            }
        }

        HbgzpPlayer winPlayer = seatMap.get(lastWinSeat);

        for (HbgzpPlayer player : seatMap.values()) {
            PlayPaohuziRes.Builder copy = builder.clone();
            if (copy.getSeat() == player.getSeat()) {
                copy.setHuxi(player.getOutHuxi());
                if(player.isAutoPlay() && copy.getActType() == HbgzpDisAction.action_type_dis){
                    copy.setActType(HbgzpDisAction.action_type_autoplaydis);
                }
            }


//            if (actionSeatMap.containsKey(player.getSeat())) {
//                List<Integer> actionList = getSendSelfAction(null, player.getSeat(), actionSeatMap.get(player.getSeat()));
//                if (actionList != null) {
//                    if (noShow && paoSeat != player.getSeat()) {
//                        if (1 == actionList.get(0)) {
//                            copy.addAllSelfAct(actionList);
//                        }
//                    } else {
//                        copy.addAllSelfAct(actionList);
//                    }
//                }
//            }
            player.writeSocket(copy.build());
            if (copy.getSelfActList() != null && copy.getSelfActList().size() > 0) {
                StringBuilder sb = new StringBuilder("hbgzp");
                sb.append("|").append(getId());
                sb.append("|").append(getPlayBureau());
                sb.append("|").append(player.getUserId());
                sb.append("|").append(player.getSeat());
                sb.append("|").append(player.isAutoPlay() ? 1 : 0);
                sb.append("|").append("actList");
                sb.append("|").append(HbgzpConstants.actionListToString(actionSeatMap.get(player.getSeat())));
                LogUtil.msgLog.info(sb.toString());
            }
        }
    }
    
    private void buildPlayRes(PlayMajiangRes.Builder builder, HbgzpPlayer player, int action, List<Hbgzp> majiangs) {
        HbgzpResTool.buildPlayRes(builder, player, action, majiangs);
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
    private HbgzpPlayer getPlayerByHasMajiang(Hbgzp majiang) {
        for (HbgzpPlayer player : seatMap.values()) {
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
            HbgzpPlayer moGangPlayer = null;
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
                        !(tempActionMap.containsKey(huseat) && tempActionMap.get(huseat).getAction() == HbgzpDisAction.action_hu)) {
                    over = false;
                    break;
                }
            }
        }

        if (!over) {
            HbgzpPlayer disMajiangPlayer = seatMap.get(disCardSeat);
            for (int huseat : huActionList) {
                if (huConfirmList.contains(huseat)) {
                    continue;
                }
                PlayMajiangRes.Builder disBuilder = PlayMajiangRes.newBuilder();
                HbgzpPlayer seatPlayer = seatMap.get(huseat);
                buildPlayRes(disBuilder, disMajiangPlayer, 0, null);
                List<Integer> actionList = actionSeatMap.get(huseat);
                disBuilder.addAllSelfAct(actionList);
                seatPlayer.writeSocket(disBuilder.build());
            }
        }
        for (HbgzpPlayer player : seatMap.values()) {
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
    private void chiPengGang(HbgzpPlayer player, List<Hbgzp> majiangs, int action) {
        if (state != table_state.play) {
            return;
        }
        logAction(player, action, majiangs, null);
        if (action != HbgzpDisAction.action_chi
        	&& action != HbgzpDisAction.action_angang	&& (majiangs == null || majiangs.isEmpty())) {
            return;
        }
        if(isBegin() && (action == HbgzpDisAction.action_angang||action == HbgzpDisAction.action_hua	)){
//        	if (!checkActionGH(player, majiangs, new ArrayList<Integer>(), action)) {
//                LogUtil.msg("有优先级更高的操作需等待！");
//                player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip);
//                return;
//            }
        }else{
        	if (!checkAction(player, majiangs, new ArrayList<Integer>(), action)) {
                LogUtil.msg("有优先级更高的操作需等待！");
                player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip);
                return;
            }
        }
//        if (!checkAction(player, majiangs, new ArrayList<Integer>(), action)) {
//            LogUtil.msg("有优先级更高的操作需等待！");
//            player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip);
//            return;
//        }
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        List<Hbgzp> handMajiang = new ArrayList<>(player.getHandMajiang());
        Hbgzp disMajiang = null;
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
            sameCount = HbgzpHelper.getMajiangCount(player,majiangs, majiangs.get(0).getVal());
        }
        // 如果是杠 后台来找出是明杠还是暗杠
//        if (action == HbgzpDisAction.action_minggang) {
//            majiangs = HbgzpHelper.getMajiangList(player.getHandMajiang(), majiangs.get(0).getVal());
//            sameCount = majiangs.size();
//            if (sameCount == 4) {
//                // 有4张一样的牌是暗杠
//                action = HbgzpDisAction.action_angang;
//            }
//        }
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return;
        }
        boolean hasQGangHu = false;
        if (action == HbgzpDisAction.action_chi) {
        	if(maxPlayerCount == 2 || calcNextSeat(getDisCardSeat())  != player.getSeat()){
        		return;
        	}
//            boolean can = canChi(player, handMajiang, majiangs, disMajiang);
//            if (!can) {
//                return;
//            }
        } else if (action == HbgzpDisAction.action_peng) {
            boolean can = canPeng(player, majiangs, sameCount, disMajiang);
            if (!can) {
                return;
            }
        } else if (action == HbgzpDisAction.action_angang) {
        	boolean can = canAnGang(player,handMajiang);
            if (!can) {
                return;
            }
//            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + HbgzpHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
            addPlayLog(player.getSeat(), action + "", HbgzpHelper.toMajiangStrs(majiangs));
//            if (canGangHu()) {
//                if (checkQGangHu(player, majiangs, action, sameCount)) {
//                    hasQGangHu = true;
//                    setNowDisCardSeat(player.getSeat());
//                    LogUtil.msg("tid:" + getId() + " " + player.getName() + "可以被抢暗杠胡！！");
//                }
//            }
        } else if (action == HbgzpDisAction.action_minggang) {
            boolean can = canMingGang(player, majiangs, sameCount, disMajiang);
            if (!can) {
                return;
            }
            ArrayList<Hbgzp> mjs = new ArrayList<>(majiangs);
            if (sameCount == 3) {
                mjs.add(disMajiang);
            }
//            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + HbgzpHelper.toMajiangStrs(mjs) + player.getExtraPlayLog());
            addPlayLog(player.getSeat(), action + "", HbgzpHelper.toMajiangStrs(mjs));
            // 特殊处理一张牌明杠的时候别人可以胡,明杠可抢
//            if ((sameCount == 1 && canGangHu())) {
//                if (checkQGangHu(player, majiangs, action, sameCount)) {
//                    hasQGangHu = true;
//                    setNowDisCardSeat(player.getSeat());
//                    LogUtil.msg("tid:" + getId() + " " + player.getName() + "可以被抢杠胡！！");
//                }
//            }
            //点杠可枪
//            if (sameCount == 3) {
//                if (checkQGangHu(player, mjs, action, sameCount)) {
//                    hasQGangHu = true;
//                    setNowDisCardSeat(player.getSeat());
//                    LogUtil.msg("tid:" + getId() + " " + player.getName() + "可以被抢杠胡！！");
//                }
//            }
        }  else if (action == HbgzpDisAction.action_hua) {
        	boolean can = canHua(player, majiangs, sameCount,disMajiang);
            if (!can) {
                return;
            }
            ArrayList<Hbgzp> mjs = new ArrayList<>(majiangs);
            if (sameCount == 4) {
                mjs.add(disMajiang);
            }
//            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + HbgzpHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
            addPlayLog(player.getSeat(), action + "", HbgzpHelper.toMajiangStrs(mjs));
            if (sameCount == 1 && canGangHu()) {
                if (checkQGangHu(player, majiangs, action, sameCount)) {
                    hasQGangHu = true;
                    setNowDisCardSeat(player.getSeat());
                    LogUtil.msg("tid:" + getId() + " " + player.getName() + "可以被抢滑胡！！");
                }
            }
        }else {
            return;
        }
        if (disMajiang != null) {
            if ((action == HbgzpDisAction.action_minggang && sameCount == 3)
                    || action == HbgzpDisAction.action_peng || action == HbgzpDisAction.action_chi
                    ||(action == HbgzpDisAction.action_hua && sameCount == 4)) {
//                if (action == HbgzpDisAction.action_chi) {
//                    majiangs.add(1, disMajiang);// 吃的牌放第二位
//                } else {
                    majiangs.add(disMajiang);
//                }
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
    private boolean checkQGangHu(HbgzpPlayer player, List<Hbgzp> majiangs, int action, int sameCount) {
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        Map<Integer, List<Integer>> huListMap = new HashMap<>();
        for (HbgzpPlayer seatPlayer : seatMap.values()) {
            if (seatPlayer.getUserId() == player.getUserId()) {
                continue;
            }
            if (action == HbgzpDisAction.action_angang) {
//                List<Hbgzp> copy = new ArrayList<>(seatPlayer.getHandMajiang());
//                copy.add(majiangs.get(0));
            } else {
                List<Integer> hu = seatPlayer.checkDisMajiang(majiangs.get(0), true,maxPlayerCount>2);
                if (!hu.isEmpty() && hu.get(HbgzpConstants.ACTION_INDEX_HU) == 1) {
                    hu.set(HbgzpConstants.ACTION_INDEX_CHI, 0);
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
                HbgzpPlayer seatPlayer = seatMap.get(entry.getKey());
                copy.addAllSelfAct(entry.getValue());
//				seatPlayer.writeSocket(copy.build());
            }
            return true;
        }
        return false;
    }

    private void chiPengGang(PlayMajiangRes.Builder builder, HbgzpPlayer player, List<Hbgzp> majiangs, int action, boolean hasQGangHu, int sameCount) {
        List<Integer> actionList = actionSeatMap.get(player.getSeat());
        if (action == HbgzpDisAction.action_peng && actionList.get(HbgzpConstants.ACTION_INDEX_MINGGANG) == 1) {
            // 可以碰也可以杠
            player.addPassGangVal(majiangs.get(0).getVal());
        }

        player.addOutPais(majiangs, action, disCardSeat);
        buildPlayRes(builder, player, action, majiangs);
        List<Integer> actList = removeActionSeat(player.getSeat());
        if (!isBegin() && !hasQGangHu) {
            clearActionSeatMap();
        }
        if (action == HbgzpDisAction.action_chi || action == HbgzpDisAction.action_peng) {
//            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + HbgzpHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
        	addPlayLog(player.getSeat(), action + "", HbgzpHelper.toMajiangStrs(majiangs));
        }
        if(action == HbgzpDisAction.action_chi ){
        	List<Integer> arr = player.checkJianpai();;
            if (!arr.isEmpty()) {
            	addActionSeat(player.getSeat(), arr);
            }
        }
        // 不是普通出牌
        if(!isBegin()){
        	setNowDisCardSeat(player.getSeat());}
        for (HbgzpPlayer seatPlayer : seatMap.values()) {
            // 推送消息
            PlayMajiangRes.Builder copy = builder.clone();
            if(action == HbgzpDisAction.action_angang && copy.getUserId().equals(seatPlayer.getUserId())){
            	copy.clearMajiangIds();
            }
            if (actionSeatMap.containsKey(seatPlayer.getSeat())) {
                copy.addAllSelfAct(actionSeatMap.get(seatPlayer.getSeat()));
            }
            seatPlayer.writeSocket(copy.build());
        }
        if (action == HbgzpDisAction.action_chi || action == HbgzpDisAction.action_peng) {
            sendTingInfo(player);
        }
        for (Player roomPlayer : roomPlayerMap.values()) {
            PlayMajiangRes.Builder copy = builder.clone();
            if(action == HbgzpDisAction.action_angang && copy.getUserId().equals(roomPlayer.getUserId())){
            	copy.clearMajiangIds();
            }
//            if(roomPlayer.getUserId() == player.getUserId() && action == HbgzpDisAction.action_chi){
//            	List<Integer> arr = player.checkJianpai();;
//                if (!arr.isEmpty()) {
//                	copy.addAllSelfAct(arr);
//                }
//            	
//            }
            roomPlayer.writeSocket(copy.build());
        }
        if (!hasQGangHu) {
            calcPoint(player, action, sameCount, majiangs);
        }
        if(action == HbgzpDisAction.action_chi){
        	 player.moMajiang(majiangs.get(0));
        }
        if (!hasQGangHu && (action == HbgzpDisAction.action_minggang || action == HbgzpDisAction.action_angang
        		|| action == HbgzpDisAction.action_hua)) {
            
            if(action == HbgzpDisAction.action_hua){//不扎直接滑的话 先摸一张
            	if(!player.getZhaVal().contains(majiangs.get(0).getVal())){
            		int canZhaCount = player.getZhaCount() - player.getZhaVal().size();//已经扎过的次数 - 扎过的牌 =不知道之前不知道扎得哪张牌的次数
            		if(canZhaCount == 0){
            			moMajiang(player, true);
            		}else{
            			player.reduceZhaCount();
            		}
            	}else{
            		player.reduceZhaCount();
            	}
            }
            // 明杠和暗杠摸牌
            moMajiang(player, true);
        }
       
        robotDealAction();
        logAction(player, action, majiangs, actList);
//        if(isBegin()){
//        	for (HbgzpPlayer tablePlayer : seatMap.values()) {
//                DealInfoRes.Builder res = DealInfoRes.newBuilder();
//                    List<Integer> oldactionList = actionSeatMap.get(tablePlayer.getSeat());
//                    if (oldactionList != null && oldactionList.size() > 0) {
//                        res.addAllSelfAct(oldactionList);
//                    }
//                res.addAllHandCardIds(tablePlayer.getHandPais());
//                res.setNextSeat(getNextDisCardSeat());
//                res.setGameType(getWanFa());
//                res.setRemain(leftMajiangs.size());
//                res.setBanker(lastWinSeat);
//                res.setDealDice(dealDice);
//                tablePlayer.writeSocket(res.build());
//            }
//        }
    }

    /**
     * 普通出牌
     *
     * @param player
     * @param majiangs
     * @param action
     */
    private void chuPai(HbgzpPlayer player, List<Hbgzp> majiangs, int action) {
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
        	player.writeComMessage(WebSocketMsgType.res_code_phz_dis_err, majiangs.get(0).getId());
            // 还没有摸牌
            return;
        }
        if(majiangs.get(0).getVal() == player.getChiVal()){
	        player.writeComMessage(WebSocketMsgType.res_code_phz_dis_err, majiangs.get(0).getId());
//	        LogUtil.msgLog.info("----tableId:" + getId() + "---userName:" + player.getName() + "------刚捡的牌不能打出去:--->>>>>>" + majiangs.get(0));
//	        player.writeComMessage(WebSocketMsgType.res_com_code_fangzhao, majiangs.get(0).getId());
        	return;//刚捡上来的牌不能吃
        }
        if (!actionSeatMap.isEmpty()) {//出牌自动过掉手上操作
            guo(player, null, HbgzpDisAction.action_pass);
        }
        if (!actionSeatMap.isEmpty()) {
        	player.writeComMessage(WebSocketMsgType.res_code_phz_dis_err_action, majiangs.get(0).getId());
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
//        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + HbgzpHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
        addPlayLog(player.getSeat(), action + "", HbgzpHelper.toMajiangStrs(majiangs));
        for (HbgzpPlayer seat : seatMap.values()) {
            List<Integer> list = new ArrayList<>();
            if (seat.getUserId() != player.getUserId()) {
                list = seat.checkDisMajiang(majiangs.get(0), true,maxPlayerCount>2);
                if (list.contains(1)) {
                    addActionSeat(seat.getSeat(), list);
                    seat.setLastCheckTime(System.currentTimeMillis());
                    logChuPaiActList(seat, majiangs.get(0), list);
                }
            }
        }
        sendDisMajiangAction(builder, player);
        player.clearChiVal();
        setIsBegin(false);
        // 给下一家发牌
        checkMo();
    }

    public List<Integer> getHuSeatByActionMap() {
        List<Integer> huList = new ArrayList<>();
        for (int seat : actionSeatMap.keySet()) {
            List<Integer> actionList = actionSeatMap.get(seat);
            if (actionList.get(HbgzpConstants.ACTION_INDEX_HU) == 1 || actionList.get(HbgzpConstants.ACTION_INDEX_ZIMO) == 1) {
                // 胡
                huList.add(seat);
            }

        }
        return huList;
    }

    private void sendDisMajiangAction(PlayMajiangRes.Builder builder, HbgzpPlayer player) {
        for (HbgzpPlayer seatPlayer : seatMap.values()) {
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
                copy.addExt(HbgzpTool.isTing(seatPlayer) ? "1" : "0");
            }
            seatPlayer.writeSocket(copy.build());
        }
        for (Player roomPlayer : roomPlayerMap.values()) {
            PlayMajiangRes.Builder copy = builder.clone();
            roomPlayer.writeSocket(copy.build());
        }
    }

    public synchronized void playCommand(HbgzpPlayer player, List<Hbgzp> majiangs, int action) {
        playCommand(player, majiangs, null, action);
    }

    /**
     * 出牌
     *
     * @param player
     * @param majiangs
     * @param action
     */
    public synchronized void playCommand(HbgzpPlayer player, List<Hbgzp> majiangs, List<Integer> hucards, int action) {
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
        if (HbgzpDisAction.action_hu == action) {
            hu(player, majiangs, action);
            return;
        }
        // 手上没有要出的麻将
//        if (action != HbgzpDisAction.action_minggang && action != HbgzpDisAction.action_chi) {
        if (action != HbgzpDisAction.action_minggang) {
            if (!player.getHandMajiang().containsAll(majiangs)) {
                return;
            }
        }
        changeDisCardRound(1);
        if (action == HbgzpDisAction.action_pass) {
            guo(player, majiangs, action);
        } else if (action == HbgzpDisAction.action_chupai) {
            chuPai(player, majiangs, action);
        } else {
            chiPengGang(player, majiangs, action);
        }
        // 记录最后一次动作的时间
        setLastActionTime(TimeUtil.currentTimeMillis());
    }

    private void passMoHu(HbgzpPlayer player, List<Hbgzp> majiangs, int action) {
        if (!moGangHuList.contains(player.getSeat())) {
            return;
        }

        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        buildPlayRes(builder, player, action, majiangs);
        builder.setSeat(nowDisCardSeat);
        removeActionSeat(player.getSeat());
        player.writeSocket(builder.build());
//        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + HbgzpHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
        addPlayLog(player.getSeat(), action + "", HbgzpHelper.toMajiangStrs(majiangs));
        if (isCalcOver()) {
            calcOver();
            return;
        }
        player.setPassMajiangVal(nowDisCardIds.get(0).getVal());

        if (moGangHuList.isEmpty()) {
            HbgzpPlayer moGangPlayer = seatMap.get(getNowDisCardSeat());
            majiangs = new ArrayList<>();
            majiangs.add(moGang);
            if (moGangPlayer.getaGang().contains(moGang)) {
                calcPoint(moGangPlayer, HbgzpDisAction.action_angang, 4, majiangs);
            } else {
                calcPoint(moGangPlayer, HbgzpDisAction.action_minggang, moGangSameCount > 0 ? moGangSameCount : 1, majiangs);
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
    private void guo(HbgzpPlayer player, List<Hbgzp> majiangs, int action) {
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
//        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + HbgzpHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
        addPlayLog(player.getSeat(), action + "", HbgzpHelper.toMajiangStrs(majiangs));
        if (isCalcOver()) {
            calcOver();
            return;
        }
        if (removeActionList.get(HbgzpConstants.ACTION_INDEX_HU) == 1 && disCardSeat != player.getSeat() && nowDisCardIds.size() == 1) {
            // 漏炮
            player.setPassMajiangVal(nowDisCardIds.get(0).getVal());
        }
        logAction(player, action, majiangs, removeActionList);
        if (!actionSeatMap.isEmpty()) {
            HbgzpPlayer disMajiangPlayer = seatMap.get(disCardSeat);
            PlayMajiangRes.Builder disBuilder = PlayMajiangRes.newBuilder();
            buildPlayRes(disBuilder, disMajiangPlayer, 0, null);
            for (int seat : actionSeatMap.keySet()) {
                List<Integer> actionList = actionSeatMap.get(seat);
                PlayMajiangRes.Builder copy = disBuilder.clone();
                copy.addAllSelfAct(new ArrayList<>());
                if (actionList != null && !tempActionMap.containsKey(seat) && !huConfirmList.contains(seat)) {
                    copy.addAllSelfAct(actionList);
                    HbgzpPlayer seatPlayer = seatMap.get(seat);
                    seatPlayer.writeSocket(copy.build());
                }
            }
        }
        if (player.isAlreadyMoMajiang()) {
            sendTingInfo(player);
        }
        refreshTempAction(player);// 先过 后执行临时可做操作里面优先级最高的玩家操作
        if(!isBegin()){
        	checkMo();
        }
    }

    private void calcPoint(HbgzpPlayer player, int action, int sameCount, List<Hbgzp> majiangs) {
        int lostPoint = 0, point = 0;
        int winPoint = 0;
        int[] seatPointArr = new int[getMaxPlayerCount() + 1];
        if (action == HbgzpDisAction.action_peng) {
            return;

        } else if (action == HbgzpDisAction.action_angang) {
            // 暗杠相当于自摸每人出2分
            point = 2;
            setFangGangSeat(player.getSeat());
        } else if (action == HbgzpDisAction.action_minggang) {
            if (sameCount == 1) {
                point = 1;
                setFangGangSeat(player.getSeat());
            } else if (sameCount == 3) {

                HbgzpPlayer disPlayer = seatMap.get(disCardSeat);
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
        point=0;
        if (point != 0) {
            for (HbgzpPlayer seat : seatMap.values()) {
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
        if (action != HbgzpDisAction.action_chi) {
           // addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + HbgzpHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog() + "_" + seatPointStr);
        }
    }

    private void recordDisMajiang(List<Hbgzp> majiangs, HbgzpPlayer player) {
        setNowDisCardIds(majiangs);
        // changeDisCardRound(1);
        setDisCardSeat(player.getSeat());
    }

    public List<Hbgzp> getNowDisCardIds() {
        return nowDisCardIds;
    }

    public void setNowDisCardIds(List<Hbgzp> nowDisCardIds) {
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
                HbgzpPlayer player = seatMap.get(seat);
                if (player != null && player.isRobot()) {
                    // 如果是机器人可以直接决定
                    List<Integer> actionList = actionSeatMap.get(seat);
                    if (actionList == null) {
                        continue;
                    }
                    List<Hbgzp> list = new ArrayList<>();
                    if (!nowDisCardIds.isEmpty()) {
                        list = HbgzpTool.getVal(player.getHandMajiang(), nowDisCardIds.get(0).getVal());
                    }
                    if (actionList.get(HbgzpConstants.ACTION_INDEX_HU) == 1 || actionList.get(HbgzpConstants.ACTION_INDEX_ZIMO) == 1) {
                        // 胡
                        playCommand(player, new ArrayList<Hbgzp>(), HbgzpDisAction.action_hu);

                    } else if (actionList.get(HbgzpConstants.ACTION_INDEX_ANGANG) == 1) {
                        playCommand(player, list, HbgzpDisAction.action_angang);

                    } else if (actionList.get(HbgzpConstants.ACTION_INDEX_MINGGANG) == 1) {
                        playCommand(player, list, HbgzpDisAction.action_minggang);

                    } else if (actionList.get(HbgzpConstants.ACTION_INDEX_PENG) == 1) {
                        playCommand(player, list, HbgzpDisAction.action_peng);
                    } else {
                        playCommand(player, new ArrayList<>(), HbgzpDisAction.action_pass);
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
            HbgzpPlayer next = seatMap.get(nextseat);
            if (next != null && next.isRobot()) {
                List<Integer> actionList = actionSeatMap.get(next.getSeat());
                if (actionList != null) {
                    List<Hbgzp> list = null;
                    if (actionList.get(0) == 1) {
                        // 胡
                        playCommand(next, new ArrayList<Hbgzp>(), HbgzpDisAction.action_hu);
                    } else if (actionList.get(3) == 1) {
                        // 机器人暗杠
                        Map<Integer, Integer> handMap = HbgzpHelper.toMajiangValMap(next,next.getHandMajiang());
                        for (Entry<Integer, Integer> entry : handMap.entrySet()) {
                            if (entry.getValue() == 4) {
                                // 可以暗杠
                                list = HbgzpHelper.getMajiangList(next.getHandMajiang(), entry.getKey());
                            }
                        }
                        playCommand(next, list, HbgzpDisAction.action_angang);

                    } else if (actionList.get(2) == 1) {
                        Map<Integer, Integer> pengMap = HbgzpHelper.toMajiangValMap(next,next.getPeng());
                        for (Hbgzp handMajiang : next.getHandMajiang()) {
                            if (pengMap.containsKey(handMajiang.getVal())) {
                                // 有碰过
                                list = new ArrayList<>();
                                list.add(handMajiang);
                                playCommand(next, list, HbgzpDisAction.action_minggang);
                                break;
                            }
                        }

                    } else if (actionList.get(1) == 1) {
                        playCommand(next, list, HbgzpDisAction.action_peng);
                    }
                } else {
                    List<Integer> handMajiangs = new ArrayList<>(next.getHandPais());
                    int maJiangId = HbgzpRobotAI.getInstance().outPaiHandle(0, handMajiangs, new ArrayList<Integer>());
                    List<Hbgzp> majiangList = HbgzpHelper.toMajiang(Arrays.asList(maJiangId));
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

        List<Integer> copy = HbgzpConstants.getMajiangList();
        addPlayLog(copy.size() + "");
        List<List<Hbgzp>> list = null;
        if (zp != null) {
            list = HbgzpTool.fapai(copy, getMaxPlayerCount(), zp);
        } else {
            list = HbgzpTool.fapai(copy, getMaxPlayerCount());
        }
        int i = 1;
        for (HbgzpPlayer player : playerMap.values()) {
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
        finishFapai=1;
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
    public void setLeftMajiangs(List<Hbgzp> leftMajiangs) {
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
    public Hbgzp getLeftMajiang() {
        if (this.leftMajiangs.size() > 0) {
            Hbgzp majiang = this.leftMajiangs.remove(0);
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
        res.addExt(0);                //1
        res.addExt(0);                //2带风
        res.addExt(0);                    //3
        res.addExt(0);                    //4 res.addExt(keChui);
        res.addExt(getTableStatus());        //5
        res.addExt(isAutoPlay);                //6
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
        for (HbgzpPlayer player : playerMap.values()) {
            PlayerInTableRes.Builder playerRes = player.buildPlayInTableInfo(isrecover);
            if (player.getUserId() == userId) {
                playerRes.addAllHandCardIds(player.getHandPais());
                if (!player.getHandMajiang().isEmpty() && player.getHandMajiang().size() % 3 == 1) {
                    if (player.isOkPlayer() && HbgzpTool.isTing(player)) {
                        playerRes.setUserSate(3);
                    }
                }
            }else{
            	playerRes.clearMoldCards();
            	playerRes.addAllMoldCards(player.buildDisCards(userId,true,player.getUserId()));
            }
            if (player.getSeat() == disCardSeat && nowDisCardIds != null) {
                playerRes.addAllOutCardIds(HbgzpHelper.toMajiangIds(nowDisCardIds));
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
//        if (actionSeatMap.isEmpty()) {
            int nextSeat = getNextDisCardSeat();
            if (nextSeat != 0) {
                res.setNextSeat(nextSeat);
            }
//        }
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
        clearMoTailPai();
        readyTime =0;
        finishFapai=0;
        isSendPiaoFenMsg=0;
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
        HbgzpPlayer player = seatMap.get(seat);
//        addPlayLog(disCardRound + "_" + seat + "_" + HbgzpDisAction.action_hasAction + "_" + StringUtil.implode(actionlist) + player.getExtraPlayLog());
        addPlayLog(player.getSeat(), HbgzpDisAction.action_hasAction + "", StringUtil.implode(actionlist));
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
            nowDisCardIds = HbgzpHelper.toMajiang(StringUtil.explodeToIntList(info.getNowDisCardIds()));
        }

        if (!StringUtils.isBlank(info.getLeftPais())) {
            try {
                leftMajiangs = HbgzpHelper.toMajiang(StringUtil.explodeToIntList(info.getLeftPais()));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    @Override
    public void initExtend0(JsonWrapper wrapper) {
        for (HbgzpPlayer player : seatMap.values()) {
            player.initExtend(wrapper.getString(player.getSeat()));
        }
        String huListstr = wrapper.getString(5);
        if (!StringUtils.isBlank(huListstr)) {
            huConfirmList = StringUtil.explodeToIntList(huListstr);
        }
        moMajiangSeat = wrapper.getInt(7, 0);
        int moGangMajiangId = wrapper.getInt(8, 0);
        if (moGangMajiangId != 0) {
            moGang = Hbgzp.getPaohzCard(moGangMajiangId);
        }
        String moGangHu = wrapper.getString(9);
        if (!StringUtils.isBlank(moGangHu)) {
            moGangHuList = StringUtil.explodeToIntList(moGangHu);
        }
//        keChui = wrapper.getInt(12, 0);
        tempActionMap = loadTempActionMap(wrapper.getString("tempActions"));
        maxPlayerCount = wrapper.getInt(13, 4);
        dealDice = wrapper.getInt(14, 0);
        tableStatus = wrapper.getInt(15, 0);
        fangGangSeat = wrapper.getInt(16, 0);
        gangKaiSeat = wrapper.getInt(17, 0);

        hupaizi= wrapper.getInt(18, 7);//胡牌要的子
        shihua= wrapper.getInt(19, 0);//1十个花 还是 2溜花
        daidingpao= wrapper.getInt(20, 0);//1 带跑 2定跑
        paofen= wrapper.getInt(21, 0);//跑分
        yipaoduoxiang= wrapper.getInt(22, 0);//一炮多响
        lowfen= wrapper.getInt(23, 0);//低于多少分
        lowfenAdd= wrapper.getInt(24, 0);//加多少分
        
        
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
        finishFapai= wrapper.getInt(36,0);
        isSendPiaoFenMsg= wrapper.getInt(37,0);
        isBegin = wrapper.getInt(38, 0) == 1;
    }
    /**
	 * 庄家是否出过牌了
	 *
	 * @return
	 */
	public boolean isBegin() {
		return isBegin && nowDisCardIds.size() == 0;
	}
    private Map<Integer, HbgzpTempAction> loadTempActionMap(String json) {
        Map<Integer, HbgzpTempAction> map = new ConcurrentHashMap<>();
        if (json == null || json.isEmpty())
            return map;
        JSONArray jsonArray = JSONArray.parseArray(json);
        for (Object val : jsonArray) {
            String str = val.toString();
            HbgzpTempAction tempAction = new HbgzpTempAction();
            tempAction.initData(str);
            map.put(tempAction.getSeat(), tempAction);
        }
        return map;
    }

    /**
     * 检查优先度 如果同时出现一个事件，按出牌座位顺序优先
     */
    private boolean checkAction(HbgzpPlayer player, List<Hbgzp> cardList, List<Integer> hucards, int action) {
        boolean canAction = checkCanAction(player, action);// 是否优先级最高 能执行操作
        if (!canAction) {// 不能操作时  存入临时操作
            int seat = player.getSeat();
            tempActionMap.put(seat, new HbgzpTempAction(seat, action, cardList, hucards));
            // 玩家都已选择自己的临时操作后  选取优先级最高
            if (tempActionMap.size() == actionSeatMap.size()) {
                int maxAction = Integer.MAX_VALUE;
                int maxSeat = 0;
                Map<Integer, Integer> prioritySeats = new HashMap<>();
                int maxActionSize = 0;
                for (HbgzpTempAction temp : tempActionMap.values()) {
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
                HbgzpPlayer tempPlayer = seatMap.get(maxSeat);
                List<Hbgzp> tempCardList = tempActionMap.get(maxSeat).getCardList();
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
     * 起手观滑检查优先度，按除庄家外出牌座位顺序优先
     */
    private boolean checkActionGH(HbgzpPlayer player, List<Hbgzp> cardList, List<Integer> hucards, int action) {
    	boolean canAction = checkCanActionGH(player, action);// 是否优先级最高 能执行操作
    	if (!canAction) {// 不能操作时  存入临时操作
    		int seat = player.getSeat();
    		tempActionMap.put(seat, new HbgzpTempAction(seat, action, cardList, hucards));
    		// 玩家都已选择自己的临时操作后  选取优先级最高
    		if (tempActionMap.size() == actionSeatMap.size()) {
    			 int beginSeat = lastWinSeat;
    			 for (int i = 0; i < maxPlayerCount; i++) {
    		        	int nextSeat =  calcNextSeat(beginSeat);
    		        	beginSeat = nextSeat;
    		        	if(!tempActionMap.containsKey(nextSeat)){
       		        		continue;
       		        	}
    		        	HbgzpPlayer tempPlayer = seatMap.get(nextSeat);
    	    			List<Hbgzp> tempCardList = tempActionMap.get(nextSeat).getCardList();
    	    			List<Integer> tempHuCards = tempActionMap.get(nextSeat).getHucards();
    	    			int tempAction = tempActionMap.get(nextSeat).getAction();
    	    			playCommand(tempPlayer, tempCardList, tempHuCards, tempAction);// 系统选取优先级最高操作
    		        }
    			
    			 clearTempAction();
    		} else {
    			if (isCalcOver()) {
    				calcOver();
    			}
    		}
    	} else {// 能操作 清理所有临时操作
    		if (tempActionMap != null  && tempActionMap.size()+1 == actionSeatMap.size()) {//优先级最高的最后一个确认
    			int seat = player.getSeat();
        		tempActionMap.put(seat, new HbgzpTempAction(seat, action, cardList, hucards));
    			int beginSeat = lastWinSeat;
   			 for (int i = 0; i < maxPlayerCount; i++) {
   		        	int nextSeat =  calcNextSeat(beginSeat);
   		        	beginSeat = nextSeat;
   		        	if(!tempActionMap.containsKey(nextSeat)){
   		        		continue;
   		        	}
   		        	HbgzpPlayer tempPlayer = seatMap.get(nextSeat);
   	    			List<Hbgzp> tempCardList = tempActionMap.get(nextSeat).getCardList();
   	    			List<Integer> tempHuCards = tempActionMap.get(nextSeat).getHucards();
   	    			int tempAction = tempActionMap.get(nextSeat).getAction();
   	    			playCommand(tempPlayer, tempCardList, tempHuCards, tempAction);// 系统选取优先级最高操作
   		        }
   			
   			 clearTempAction();
   			canAction = false;
   		}
    	}
    	return canAction;
    }

    /**
     * 执行可做操作里面优先级最高的玩家操作
     *
     * @param player
     */
    private void refreshTempAction(HbgzpPlayer player) {
        tempActionMap.remove(player.getSeat());
        Map<Integer, Integer> prioritySeats = new HashMap<>();//各位置优先操作
        for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
            int seat = entry.getKey();
            List<Integer> actionList = entry.getValue();
            List<Integer> list = HbgzpDisAction.parseToDisActionList(actionList);
            int priorityAction = HbgzpDisAction.getMaxPriorityAction(list);
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
        Iterator<HbgzpTempAction> iterator = tempActionMap.values().iterator();
        while (iterator.hasNext()) {
            HbgzpTempAction tempAction = iterator.next();
            if (tempAction.getSeat() == maxPrioritySeat) {
                int action = tempAction.getAction();
                List<Hbgzp> tempCardList = tempAction.getCardList();
                List<Integer> tempHuCards = tempAction.getHucards();
                HbgzpPlayer tempPlayer = seatMap.get(tempAction.getSeat());
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
    public boolean checkCanAction(HbgzpPlayer player, int action) {
        // 优先度为胡杠补碰吃
        List<Integer> stopActionList = HbgzpDisAction.findPriorityAction(action);
        for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
            if (player.getSeat() != entry.getKey()) {
                // 别人
                boolean can = HbgzpDisAction.canDisMajiang(stopActionList, entry.getValue());
                if (!can) {
                    return false;
                }
                List<Integer> disActionList = HbgzpDisAction.parseToDisActionList(entry.getValue());
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
     * 起手观滑检查优先度，按除庄家外出牌座位顺序优先
     *
     * @param player
     * @param action
     * @return
     */
    public boolean checkCanActionGH(HbgzpPlayer player, int action) {
    	// 优先度为胡杠补碰吃
//    	List<Integer> stopActionList = HbgzpDisAction.findPriorityAction(action);
    	
    	for (int i = 0; i < maxPlayerCount; i++) {
        	int nextSeat =  calcNextSeat(lastWinSeat);
        	if(!actionSeatMap.containsKey(nextSeat)){
        		continue;
        	}
        	if(player.getSeat() == nextSeat){
        		return true;
        	}else{
        		return false;
        	}
        	
    	}
    	
//    	for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
//    		if (player.getSeat() != entry.getKey()) {
//    			List<Integer> disActionList = HbgzpDisAction.parseToDisActionList(entry.getValue());
//    			if (disActionList.contains(action)) {
//    				// 同时拥有同一个事件 根据座位号来判断
//    				int actionSeat = entry.getKey();
//    				int nearSeat = getNearSeat(nowDisCardSeat, Arrays.asList(player.getSeat(), actionSeat));
//    				if (nearSeat != player.getSeat()) {
//    					return false;
//    				}
//    			}
//    		}
//    	}
    	return false;
    }


    /**
     * 是否能碰
     *
     * @param player
     * @param majiangs
     * @param sameCount
     * @return
     */
    private boolean canPeng(HbgzpPlayer player, List<Hbgzp> majiangs, int sameCount, Hbgzp disMajiang) {
        if (player.isAlreadyMoMajiang()) {
            return false;
        }
        if(player.getZhaVal().contains(majiangs.get(0).getVal())){
        	return false;
        }
        for (Hbgzp gzp:majiangs) {
			if(player.getChiIds().contains(gzp.getId())){
				return false;
			}
		}
        if (sameCount < 2) {
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
     * 是否能杠
     *
     * @param player
     * @param majiangs
     * @param sameCount
     * @return
     */
//    private boolean canAnGang(HbgzpPlayer player, List<Hbgzp> majiangs, int sameCount) {
//        if (sameCount != 4 || majiangs.size() != 4) {
//            return false;
//        }
//        if(player.getZhaVal().contains(majiangs.get(1).getVal())){//杠过得不能再杠
//        	return false;
//        }
//        if (player.getSeat() != getNextDisCardSeat()) {
//            return false;
//        }
//        return true;
//    }

    /**
     * 是否能杠
     *
     * @param player
     * @param majiangs
     * @param sameCount
     * @return
     */
    private boolean canAnGang(HbgzpPlayer player,List<Hbgzp> handPais) {
    	List<Hbgzp> copy = new ArrayList<>(handPais);
    	
    	Map<Integer, Integer> handMap = HbgzpHelper.toMajiangValMap(player,copy);
		if (handMap.containsValue(4) || handMap.containsValue(5)) {
			Map<Integer, Integer> canZhaMap = player.getCanZhaMap();
			if(canZhaMap.size()>0){
				int zhaValCount = 0;
				for (int key : canZhaMap.keySet()) {
					if(player.getZhaVal().contains(key)){
						zhaValCount ++;
					}
				}
//				int canZhaCount = player.getZhaCount() - zhaValCount;
//				int canZhaCount = player.getZhaCount() - player.getZhaVal().size();//已经扎过的次数 - 扎过的牌 =不知道之前不知道扎得哪张牌的次数
				if(canZhaMap.size()> zhaValCount){
					return true;
				}
			}
			
		}
    	
//    	if (sameCount != 4 || majiangs.size() != 4) {
//    		return false;
//    	}
//    	if(player.getZhaVal().contains(majiangs.get(1).getVal())){//杠过得不能再杠
//    		return false;
//    	}
//    	if (player.getSeat() != getNextDisCardSeat()) {
//    		return false;
//    	}
    	return true;
    }
    

    
    /**
     * 是否能招
     *
     * @param player
     * @param majiangs
     * @param sameCount
     * @return
     */
    private boolean canMingGang(HbgzpPlayer player, List<Hbgzp> majiangs, int sameCount, Hbgzp disMajiang) {
    	
    	if(player.getZhaVal().contains(majiangs.get(0).getVal())){
        	return false;
        }
    	List<Hbgzp> handMajiangs = player.getHandMajiang();
        List<Integer> pengList = HbgzpHelper.toMajiangVals(player.getPeng());

        if (majiangs.size() == 1) {
            if (player.getSeat() != getNextDisCardSeat()) {
                return false;
            }
            if (handMajiangs.containsAll(majiangs) && pengList.contains(majiangs.get(0).getVal())) {
                return true;
            }
        } else if (majiangs.size() == 3) {
            if (sameCount < 3) {
                return false;
            }
            for (Hbgzp gzp:majiangs) {
    			if(player.getChiIds().contains(gzp.getId())){
    				return false;
    			}
    		}
            if (disMajiang == null || disMajiang.getVal() != majiangs.get(0).getVal()) {
                return false;
            }
            return true;
        }

        return false;
    }
    /**
     * 是否能滑
     *
     * @param player
     * @param majiangs
     * @param sameCount
     * @return
     */
    private boolean canHua(HbgzpPlayer player, List<Hbgzp> majiangs, int sameCount, Hbgzp disMajiang) {
    	List<Hbgzp> handMajiangs = player.getHandMajiang();
        List<Integer> pengList = HbgzpHelper.toMajiangVals(player.getmGang());

        if (majiangs.size() == 1) {
            if (player.getSeat() != getNextDisCardSeat()) {
                return false;
            }
            if (handMajiangs.containsAll(majiangs) && pengList.contains(majiangs.get(0).getVal())) {
                return true;
            }
        } else if (majiangs.size() == 4) {
            if (sameCount < 4) {
                return false;
            }
            for (Hbgzp gzp:majiangs) {
    			if(player.getChiIds().contains(gzp.getId())){
    				return false;
    			}
    		}
            if (disMajiang == null || disMajiang.getVal() != majiangs.get(0).getVal()) {
                return false;
            }
            return true;
        }else if (majiangs.size() == 5) {
            if (sameCount < 5) {
                return false;
            }
            for (Hbgzp gzp:majiangs) {
    			if(player.getChiIds().contains(gzp.getId())){
    				return false;
    			}
    		}
            return true;
        }

        return false;
    }

    public Map<Integer, List<Integer>> getActionSeatMap() {
        return actionSeatMap;
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
    public void setMoGang(Hbgzp moGang, List<Integer> moGangHuList, HbgzpPlayer player, int sameCount) {
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
        return true;
    }


//    /**
//     * @param over
//     * @param selfMo
//     * @param winList
//     * @param isBreak
//     * @return
//     */
//    public ClosingPhzInfoRes.Builder sendAccountsMsg(boolean over, boolean selfMo, List<Integer> winList, boolean isBreak) {
//        if (isBreak) {
//            for (HbgzpPlayer seat : seatMap.values()) {
//                seat.changePoint(seat.getLostPoint());
//            }
//        }
//        
//        //大结算计算加倍分
//        if(over && jiaBei == 1){
//            int jiaBeiPoint = 0;
//            int loserCount = 0;
//            for (HbgzpPlayer player : seatMap.values()) {
//                if (player.getTotalPoint() > 0 && player.getTotalPoint() < jiaBeiFen) {
//                    jiaBeiPoint += player.getTotalPoint() * (jiaBeiShu - 1);
//                    player.setTotalPoint(player.getTotalPoint() * jiaBeiShu);
//                } else if (player.getTotalPoint() < 0) {
//                    loserCount++;
//                }
//            }
//            if (jiaBeiPoint > 0) {
//                for (HbgzpPlayer player : seatMap.values()) {
//                    if (player.getTotalPoint() < 0) {
//                        player.setTotalPoint(player.getTotalPoint() - (jiaBeiPoint / loserCount));
//                    }
//                }
//            }
//
//        }
//        
//        
//        HbgzpPlayer winPlayer = !CollectionUtils.isEmpty(winList) ? seatMap.get(winList.get(0)) : null;
//
//        List<TablePhzResMsg.PhzHuCardList> cardCombos = new ArrayList<>();
//
//        for (HbgzpPlayer player : seatMap.values()) {
//            ClosingPhzPlayerInfoRes.Builder build = player.bulidTotalClosingPlayerInfoRes();
//
//            LogUtil.printDebug(player.getUserId() + "结算分数:{},{} , point:{}", player.getWinLossPoint(), player.getTotalPoint(), player.getPoint());
//
//            build.addAllFirstCards(player.getFirstPais());//将初始手牌装入网络对象
//
//            for (int action : player.getActionTotalArr()) {     //0,1,2,3
//                build.addStrExt(action + "");
//            }
//
//            if (isGoldRoom()) {
//                build.addStrExt("1");//4
//                build.addStrExt(player.loadAllGolds() <= 0 ? "1" : "0");//5
//                build.addStrExt(outScoreMap == null ? "0" : outScoreMap.getOrDefault(player.getUserId(), 0).toString());//6
//            } else {
//                build.addStrExt("0");
//                build.addStrExt("0");
//                build.addStrExt("0");
//            }
//            build.addStrExt(ticketMap == null ? "0" : String.valueOf(ticketMap.getOrDefault(player.getUserId(), 0)));//7
//            builderList.add(build);
//
//            //信用分
//            if (isCreditTable()) {
//                player.setWinLoseCredit(player.getTotalPoint() * creditDifen);
//            }
//
//            TablePhzResMsg.PhzHuCardList.Builder builder = TablePhzResMsg.PhzHuCardList.newBuilder();
//            builder.setSeat(player.getSeat());
//            builder.addAllPhzCard(player.buildNormalPhzHuCards());
//            cardCombos.add(builder.build());
//        }
//
//        
//        
//        List<ClosingPhzPlayerInfoRes> list = new ArrayList<>();
//        List<ClosingPhzPlayerInfoRes.Builder> builderList = new ArrayList<>();
//        for (HbgzpPlayer player : seatMap.values()) {
//            ClosingPhzPlayerInfoRes.Builder build = null;
//            if (over) {
//                build = player.bulidTotalClosingPlayerInfoRes();
//            } else {
//                build = player.bulidOneClosingPlayerInfoRes();
//            }
//            if (winList != null && winList.contains(player.getSeat())) {
//                if (!selfMo) {
//                    // 不是自摸
//                    Hbgzp huMajiang = nowDisCardIds.get(0);
//                    if (!build.getCardsList().contains(huMajiang.getId())) {
//                        build.addCards(huMajiang.getId());
//                    }
////                    build.setIsHu(huMajiang.getId());
//                } else {
////                    build.setIsHu(player.getLastMoMajiang().getId());
//                }
//            }
//            if (winList != null && winList.contains(player.getSeat())) {
//                // 手上没有剩余的牌放第一位为赢家
//                builderList.add(0, build);
//            } else {
//                builderList.add(build);
//            }
//            //信用分
//            if (isCreditTable()) {
//                player.setWinLoseCredit(player.getTotalPoint() * creditDifen);
//            }
//        }
//
//        //信用分计算
//        if (isCreditTable()) {
//            //计算信用负分
//            calcNegativeCredit();
//            long dyjCredit = 0;
//            for (HbgzpPlayer player : seatMap.values()) {
//                if (player.getWinLoseCredit() > dyjCredit) {
//                    dyjCredit = player.getWinLoseCredit();
//                }
//            }
//            for (ClosingPhzPlayerInfoRes.Builder builder : builderList) {
//                HbgzpPlayer player = seatMap.get(builder.getSeat());
//                calcCommissionCredit(player, dyjCredit);
//                builder.setWinLoseCredit(player.getWinLoseCredit());
//                builder.setCommissionCredit(player.getCommissionCredit());
//                list.add(builder.build());
//            }
//        } else {
//            for (ClosingPhzPlayerInfoRes.Builder builder : builderList) {
//                list.add(builder.build());
//            }
//        }
//
//        ClosingPhzInfoRes.Builder res = ClosingPhzInfoRes.newBuilder();
//        res.addAllClosingPlayers(list);
//        res.setIsBreak(isBreak ? 1 : 0);
//        res.setWanfa(getWanFa());
//        res.addAllExt(buildAccountsExt(over?1:0));
//        res.addAllLeftCards(HbgzpHelper.toMajiangIds(leftMajiangs));
//        
//        
//        res.addAllAllCardsCombo(phzHuCardListBui);
//        res.addFanTypes(selfMo?1:0);
//        if (winPlayer != null) {
//            res.setTun(totalTun);// 剥皮算0等
//            res.setFan(winFen);
////            if(mt.contains(PaohuziMingTangRule.LOUDI_MINGTANG_PIAOHU)){
////                res.setHuxi(15);
////            }else {
//                res.setHuxi(winPlayer.getTotalHu());
////            }
//            res.setTotalTun(totalTun);
//            res.setHuSeat(winPlayer.getSeat());
////            if (winPlayer.getHu() != null && winPlayer.getHu().getCheckCard() != null) {
//            res.setHuCard(finalCard);
////            }
//            res.addAllCards(winPlayer.buildPhzHuCards());
//        }
//        res.addAllClosingPlayers(list);
//        res.setIsBreak(isBreak ? 1 : 0);
//        res.setWanfa(getWanFa());
//        res.addAllExt(buildAccountsExt(over));
////        res.addAllStartLeftCards(startLeftCards);
//        res.addAllIntParams(getIntParams());
//        
//        
//        for (HbgzpPlayer player : seatMap.values()) {
//            player.writeSocket(res.build());
//        }
//        broadMsgRoomPlayer(res.build());
//        return res;
//    }

    
    /**
     * 目前仅小结算展示唯一赢家,大结算可能有多个赢家,中途解散没有赢家展示,当大结算时可以不用找出小结算最终赢家
     * @param over        结束
     * @param winList     赢家列表
     * @param winFen      赢得积分
     * @param mt          番类型
     * @param totalTun    囤数
     * @param isBreak     true中途解散
     * @param outScoreMap 赢家剩余金币
     * @param ticketMap   赢家金币还能结算多少局
     * @return
     * @description 结算消息,大结算
     * @author Guang.OuYang
     * @date 2019/9/4
     */
    public ClosingPhzInfoRes.Builder sendAccountsMsg(boolean over, List<Integer> winList, int winFen,  int totalTun, boolean isBreak) {
        List<ClosingPhzPlayerInfoRes> list = new ArrayList<>();
        List<ClosingPhzPlayerInfoRes.Builder> builderList = new ArrayList<>();

        int totalAddPoint = 0;

        List<Integer> bigWinList=new ArrayList<>();
        //大结算
        //大结算,找赢家,赢家可能会有多个
        Iterator<HbgzpPlayer> iterator = seatMap.values().iterator();
        while (iterator.hasNext()) {
            HbgzpPlayer p = iterator.next();
            if (p != null) {
                //展示分
//                p.setWinLossPoint(p.getTotalPoint());
                if (p.getTotalPoint() > 0) {
                    if (over) {
                        //大结算计算加倍分
                        int addScore = calcFinalRatio(p);
                        //低于X分进行加倍
                        totalAddPoint += addScore;
                        LogUtil.printDebug("加分:{}", addScore);
                        p.setTotalPoint(p.getTotalPoint() + addScore);
                    }

                    if (!bigWinList.contains(p.getSeat()))
                        bigWinList.add(p.getSeat());
                }
            }
        }

        //总翻倍和增加的分平均扣在每个输家身上
        int avg = totalAddPoint == 0 ? 0 : bigWinList.size() < seatMap.size() ? totalAddPoint / (seatMap.size() - bigWinList.size()) : totalAddPoint;

        //使用winLossPoint展示小结算总分,总结算分额外展示
        seatMap.values().stream().filter(v -> !bigWinList.contains(v.getSeat())).forEach(v -> {
            v.setTotalPoint(v.getTotalPoint() - avg);
        });

        //赢家
        HbgzpPlayer winPlayer = !CollectionUtils.isEmpty(winList) ? seatMap.get(winList.get(0)) : null;

        List<TablePhzResMsg.PhzHuCardList> cardCombos = new ArrayList<>();

        boolean isSelf= false;
        if(winList.size() == 1 && seatMap.get(winList.get(0)).getHandMajiang().size() % 3 == 2 && winList.get(0) == moMajiangSeat ){
        	isSelf = true;
        }
        
        for (HbgzpPlayer player : seatMap.values()) {
            ClosingPhzPlayerInfoRes.Builder build = player.bulidTotalClosingPlayerInfoRes();

            LogUtil.printDebug(player.getUserId() + "结算分数:{} , point:{}",  player.getTotalPoint(), player.getPoint());

//            build.addAllFirstCards(player.getFirstPais());//将初始手牌装入网络对象

            for (int action : player.getActionTotalArr()) {     //0,1,2,3
                build.addStrExt(action + "");
            }
            build.addStrExt(player.getSingleMaxHuxi()+"");
            if (winList.size() == 1 && seatMap.get(winList.get(0)).getHandMajiang().size() % 3 == 2 && winList.get(0) == moMajiangSeat && player.getSeat() == moMajiangSeat ) {
                build.addStrExt("3");//自摸
            }else if(!isSelf&&winList!=null && winList.size()>0 && winList.contains(player.getSeat())){
            	 build.addStrExt("2");//接炮
            }else if(!isSelf&& winList != null && winList.size()>0 && player.getSeat() == disCardSeat){
            	build.addStrExt("1");//放炮
            }else{
            	build.addStrExt("0");
            }
//            if (isGoldRoom()) {
//                build.addStrExt("1");//4
//                build.addStrExt(player.loadAllGolds() <= 0 ? "1" : "0");//5
//                build.addStrExt(outScoreMap == null ? "0" : outScoreMap.getOrDefault(player.getUserId(), 0).toString());//6
//            } else {
//                build.addStrExt("0");
//                build.addStrExt("0");
//                build.addStrExt("0");
//            }
//            build.addStrExt(ticketMap == null ? "0" : String.valueOf(ticketMap.getOrDefault(player.getUserId(), 0)));//7
            builderList.add(build);

            //信用分
            if (isCreditTable()) {
                player.setWinLoseCredit(player.getTotalPoint() * creditDifen);
            }

            TablePhzResMsg.PhzHuCardList.Builder builder = TablePhzResMsg.PhzHuCardList.newBuilder();
            builder.setSeat(player.getSeat());
            builder.addAllPhzCard(player.buildNormalPhzHuCards());
            cardCombos.add(builder.build());
        }

        //信用分计算
        if (isCreditTable()) {
            //计算信用负分
            calcNegativeCredit();

            long dyjCredit = 0;
            for (HbgzpPlayer player : seatMap.values()) {
                if (player.getWinLoseCredit() > dyjCredit) {
                    dyjCredit = player.getWinLoseCredit();
                }
            }
            for (ClosingPhzPlayerInfoRes.Builder builder : builderList) {
                HbgzpPlayer player = seatMap.get(builder.getSeat());
                calcCommissionCredit(player, dyjCredit);

                builder.addStrExt(player.getWinLoseCredit() + "");      //8
                builder.addStrExt(player.getCommissionCredit() + "");   //9
                // 2019-02-26更新
                builder.setWinLoseCredit(player.getWinLoseCredit());
                builder.setCommissionCredit(player.getCommissionCredit());
                
                builder.addStrExt(player.getPiaoFen()+"");
            }
        } else if (isGroupTableGoldRoom()) {
            // -----------亲友圈金币场---------------------------------
            for (HbgzpPlayer player : seatMap.values()) {
                player.setWinGold(player.getTotalPoint() * gtgDifen);
            }
            calcGroupTableGoldRoomWinLimit();
            for (ClosingPhzPlayerInfoRes.Builder builder : builderList) {
                HbgzpPlayer player = seatMap.get(builder.getSeat());
                builder.addStrExt(player.getWinLoseCredit() + "");      //8
                builder.addStrExt(player.getCommissionCredit() + "");   //9
                builder.addStrExt(player.getPiaoFen()+"");
                builder.setWinLoseCredit(player.getWinGold());
            }
        } else {
            for (ClosingPhzPlayerInfoRes.Builder builder : builderList) {
            	HbgzpPlayer player = seatMap.get(builder.getSeat());
                builder.addStrExt(0 + ""); //8
                builder.addStrExt(0 + ""); //9
                builder.addStrExt(player.getPiaoFen()+"");

            }
        }
        for (ClosingPhzPlayerInfoRes.Builder builder : builderList) {
            HbgzpPlayer player = seatMap.get(builder.getSeat());
//            builder.addStrExt(player.getTuo() + "");      //10
            list.add(builder.build());
        }

        ClosingPhzInfoRes.Builder res = ClosingPhzInfoRes.newBuilder();
        res.addAllLeftCards(HbgzpTool.toPhzCardIds(leftMajiangs));
        if (winPlayer != null) {
            res.setTun(totalTun);// 总囤数
            res.setFan(winFen); //
            res.setHuxi(winPlayer.getTotalHu());
            res.setTotalTun(totalTun);
            res.setHuCard(huCard!=null?huCard.getId():0);
            res.setHuSeat(winPlayer.getSeat());
            if (winPlayer.getHu() != null && winPlayer.getHu().getCheckCard() != null) {
                res.setHuCard(winPlayer.getHu().getCheckCard().getId());
            }
            res.addAllCards(winPlayer.buildPhzHuCards());
        }
        res.addAllClosingPlayers(list);
        res.setIsBreak(isBreak ? 1 : 0);
        res.setWanfa(getWanFa());
        res.addAllExt(buildAccountsExt(over));
//        res.addAllStartLeftCards(startLeftCards);
        res.addAllAllCardsCombo(cardCombos);
        if (over && isGroupRoom() && !isCreditTable()) {
            res.setGroupLogId((int) saveUserGroupPlaylog());
        }
        for (HbgzpPlayer player : seatMap.values()) {
            player.writeSocket(res.build());
        }

        return res;
    }

    /**
     *@description 计算最终翻倍倍率
     *@param
     *@return
     *@author Guang.OuYang
     *@date 2019/9/16
     */
    private int calcFinalRatio(HbgzpPlayer v) {
        //负分不做翻倍
        if (v.getTotalPoint() < 0) {
            return 0;
        }
        int ratio = doubleChipEffect(v.getTotalPoint());
        //低于X分进行加倍
        int doubleChip = (ratio * v.getTotalPoint()) - v.getTotalPoint();
        LogUtil.printDebug(v.getUserId() + "大结算加倍:倍率:{},winLoss:{},totalPoint:{}->point:{}", ratio, v.getTotalPoint(), v.getTotalPoint(), v.getPoint());

        int addScore = lowScoreEffect(v.getTotalPoint());
        //低于X分进行加分
        int addScoreChip = (addScore +  v.getTotalPoint()) - v.getTotalPoint();
        LogUtil.printDebug(v.getUserId() + "大结算低于多少分+多少分:阈值:{},winLoss:{},totalPoint:{}->point:{}", addScore, v.getTotalPoint(), v.getTotalPoint(), v.getPoint());

        //把加的分都总计一下
        return doubleChip + addScoreChip;
    }
    
    public int doubleChipEffect(int currentScore) {
        return Math.max(isDoubleChip() && Math.abs(currentScore) < jiaBeiFen ? jiaBeiShu : 1, 1);
    }

    public int lowScoreEffect(int currentScore) {
        return lowfen > 0 && Math.abs(currentScore) < lowfen ? currentScore > 0 ? lowfenAdd : -lowfenAdd : 0;
    }

    public boolean isDoubleChip() {
        return jiaBei == 1;
    }
    /**
     * @param
     * @return
     * @description 结算消息
     * @author Guang.OuYang
     * @date 2019/9/4
     */
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
        ext.add(isGroupRoom() ? "1" : "0");
        ext.add(isOver ? "1" : "0");
        //金币场大于0
        ext.add(modeId);
        int ratio;
        int pay;
        if (isGoldRoom()) {
            ratio = GameConfigUtil.loadGoldRatio(modeId);
            pay = PayConfigUtil.get(playType, totalBureau, getMaxPlayerCount(), payType == 1 ? 0 : 1, modeId);
        } else {
            ratio = 1;
            pay = loadPayConfig(payType);
        }
        ext.add(String.valueOf(ratio));
        ext.add(String.valueOf(pay >= 0 ? pay : 0));
        ext.add(isGroupRoom() ? loadGroupId() : "");//13
//        ext.add(String.valueOf(gameModel.getDiscardHoleCards()));//14
        ext.add("");//14


        //信用分
        ext.add(creditMode + ""); //15
        ext.add(creditJoinLimit + "");//16
        ext.add(creditDissLimit + "");//17
        ext.add(creditDifen + "");//18
        ext.add(creditCommission + "");//19
        ext.add(creditCommissionMode1 + "");//20
        ext.add(creditCommissionMode2 + "");//21
        ext.add(autoPlay ? "1" : "0");//20
        return ext;
    }

    @Override
    public void sendAccountsMsg() {
    	ClosingPhzInfoRes.Builder res = sendAccountsMsg(true, new ArrayList<>(), 0, 0,true);
        saveLog(true, 0l, res.build());
    }

    @Override
    public Class<? extends Player> getPlayerClass() {
        return HbgzpPlayer.class;
    }

    @Override
    public int getWanFa() {
        return getPlayType();
    }

//	@Override
//	public boolean isTest() {
//		return HbgzpConstants.isTest;
//	}

    @Override
    public void checkReconnect(Player player) {
//        if (super.isAllReady() && getKeChui() > 0 && getTableStatus() == HbgzpConstants.TABLE_STATUS_CHUI) {
//            HbgzpPlayer player1 = (HbgzpPlayer) player;
//            if (player1.getChui() < 0) {
//                ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_chui, getTableStatus());
////                player1.writeSocket(com.build());
//                return;
//            }
//            if (player1.getHandPais() != null && player1.getHandPais().size() > 0) {
//                sendTingInfo(player1);
//            }
//        }
//        if (state == table_state.play) {
//            HbgzpPlayer player1 = (HbgzpPlayer) player;
//            if (player1.getHandPais() != null && player1.getHandPais().size() > 0) {
//                sendTingInfo(player1);
//            }
//        }
    	 ((HbgzpPlayer) player).checkAutoPlay(0, true);
         if (state == table_state.play) {
             HbgzpPlayer player1 = (HbgzpPlayer) player;
             if (player1.getHandPais() != null && player1.getHandPais().size() > 0) {
//                 sendTingInfo(player1);
             }
         }
         sendPiaoReconnect(player);
         sendTingInfoReconnect((HbgzpPlayer) player);
     }

     private void sendPiaoReconnect(Player player){
         if(daidingpao==0||maxPlayerCount!=getPlayerCount())
             return;
         int count=0;
         for(Map.Entry<Integer,HbgzpPlayer> entry:seatMap.entrySet()){
             player_state state = entry.getValue().getState();
             if(state==player_state.play||state==player_state.ready)
                 count++;
         }
         if(count!=maxPlayerCount)
             return;

         for(Map.Entry<Integer,HbgzpPlayer> entry:seatMap.entrySet()){
             HbgzpPlayer p = entry.getValue();
             if(p.getUserId()==player.getUserId()){
                 if(p.getPiaoFen()==-1){
                	 ComRes.Builder msg = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_hbgzp_piaofen);
                     if(daidingpao==2){
                    	 msg.addParams(p.getdPiaofen());
                     }else{
                    	 msg.addParams(paofen);
                     }
                     player.writeSocket(msg.build());
//                	 player.writeComMessage(WebSocketMsgType.res_code_hbgzp_piaofen);
                     continue;
                 }
             }else {
                 List<Integer> l=new ArrayList<>();
                 l.add((int)p.getUserId());
                 l.add(p.getPiaoFen());
                 player.writeComMessage(WebSocketMsgType.res_code_hbgzp_broadcast_piaofen, l);
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
				HbgzpPlayer player = seatMap.get(seat);
				player.setAutoPlay(false, this);
				player.setLastOperateTime(System.currentTimeMillis());
			}
			return;
		}

        if (getTableStatus() == HbgzpConstants.TABLE_STATUS_CHUI) {
            for (int seat : seatMap.keySet()) {
                HbgzpPlayer player = seatMap.get(seat);
                if   (player.getLastCheckTime() > 0 && player.getChui() >= 0) {
					player.setLastCheckTime(player.getLastCheckTime() + 1 * 1000);
					continue;
				}
                if (!player.checkAutoPlay(2, false)) {
                    continue;
                }
//                autoChui(player);
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
			for (HbgzpPlayer player : seatMap.values()) {
				if (player.getState() != player_state.entry && player.getState() != player_state.over) {
					continue;
				} else {
					if (readyTime >= 5 && player.isAutoPlay()) {
						// 玩家进入托管后，3秒自动准备
						autoReady(player);
					} 
//					else if (readyTime > 30) {
//						autoReady(player);
//					}
				}
			}
            
            
            
//            for (HbgzpPlayer player : seatMap.values()) {
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
                    HbgzpPlayer player = seatMap.get(seat);
                    if (player == null) {
                        continue;
                    }
                    if (!player.checkAutoPlay(2, false)) {
                        continue;
                    }
                    playCommand(player, new ArrayList<>(), HbgzpDisAction.action_hu);
                }
                return;
            } else {
                int action = 0, seat = 0;
                for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
                    List<Integer> actList = HbgzpDisAction.parseToDisActionList(entry.getValue());
                    if (actList == null) {
                        continue;
                    }
                    seat = entry.getKey();
                    action = HbgzpDisAction.getAutoMaxPriorityAction(actList);
                    HbgzpPlayer player = seatMap.get(seat);
                    if (!player.checkAutoPlay(0, false)) {
                        continue;
                    }
                    boolean chuPai = false;
                    if (player.isAlreadyMoMajiang()) {
                        chuPai = true;
                    }
                    if (action == HbgzpDisAction.action_peng) {
                        if (player.isAutoPlaySelf()) {
                            //自己开启托管直接过
                            playCommand(player, new ArrayList<>(), HbgzpDisAction.action_pass);
                            if (chuPai) {
                                autoChuPai(player);
                            }
                        } else {
                            if (nowDisCardIds != null && !nowDisCardIds.isEmpty()) {
                                Hbgzp mj = nowDisCardIds.get(0);
                                List<Hbgzp> mjList = new ArrayList<>();
                                for (Hbgzp handMj : player.getHandMajiang()) {
                                    if (handMj.getVal() == mj.getVal()) {
                                        mjList.add(handMj);
                                        if (mjList.size() == 2) {
                                            break;
                                        }
                                    }
                                }
                                playCommand(player, mjList, HbgzpDisAction.action_peng);
                            }
                        }
                    } else {
                        playCommand(player, new ArrayList<>(), HbgzpDisAction.action_pass);
                        if (chuPai) {
                            autoChuPai(player);
                        }
                    }
                }
            }
        } else {
            HbgzpPlayer player = seatMap.get(nowDisCardSeat);
            if (player == null || !player.checkAutoPlay(0, false)) {
                return;
            }
            autoChuPai(player);
        }
    }

    public void autoChuPai(HbgzpPlayer player) {

        //HbgzpQipaiTool.dropHongzhongVal(handMajiangs);红中麻将要去掉红中
//					int mjId = HbgzpRobotAI.getInstance().outPaiHandle(0, handMjIds, new ArrayList<>());
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
            List<Hbgzp> mjList = HbgzpHelper.toMajiang(Arrays.asList(mjId));
            playCommand(player, mjList, HbgzpDisAction.action_chupai);
        }
    }

    public void autoChui(HbgzpPlayer player) {
        int chuiVal = 0;
        if (getTableStatus() != HbgzpConstants.TABLE_STATUS_CHUI) {
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
//        keChui = StringUtil.getIntValue(params, 6, 0);//是否加锤
        isAutoPlay = StringUtil.getIntValue(params, 8, 0);
        diFen = StringUtil.getIntValue(params, 17, 1);
		 
        //加倍：0否，1是
	    this.jiaBei = StringUtil.getIntValue(params, 18, 0);
	    //加倍分
	    this.jiaBeiFen = StringUtil.getIntValue(params, 19, 0);
	    //加倍数
	    this.jiaBeiShu = StringUtil.getIntValue(params, 20, 0);
	    autoPlayGlob = StringUtil.getIntValue(params, 21, 0);

        if (playerCount != 2) {
            jiaBei = 0 ;
        }


        hupaizi= StringUtil.getIntValue(params, 22, 7);//胡牌要的子
        shihua= StringUtil.getIntValue(params, 23, 0);//1十个花  2溜花
        daidingpao= StringUtil.getIntValue(params, 24, 0);//1 带跑 2定跑
        paofen= StringUtil.getIntValue(params, 25, 0);//跑分
        yipaoduoxiang= StringUtil.getIntValue(params, 26, 0);//一炮多响
        lowfen= StringUtil.getIntValue(params, 27, 0);//低于多少分
        lowfenAdd= StringUtil.getIntValue(params, 28, 0);//加多少分
        
        
        setMaxPlayerCount(playerCount);
        changeExtend();
        if (!isJoinPlayerAllotSeat()) {
//			getRoomModeMap().put("1", "1"); //可观战（默认）
        }
    }


    @Override
    public boolean isAllReady() {
//        if (super.isAllReady()) {
//            if (getKeChui() == 1) {
//                
//                boolean bReturn = true;
//                //机器人默认处理
//                if (this.isTest()) {
//                    for (HbgzpPlayer robotPlayer : seatMap.values()) {
//                        if (robotPlayer.isRobot()) {
//                            robotPlayer.setChui(0);
//                        }
//                    }
//                }
//                ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_chui_start, getTableStatus());
//                for (HbgzpPlayer player : seatMap.values()) {
//                    if (player.getChui() < 0) {
//                    	if (getTableStatus() != HbgzpConstants.TABLE_STATUS_CHUI) {
//                    		player.setLastCheckTime(System.currentTimeMillis());
//                    	}
//                        player.writeSocket(com.build());
//                        bReturn = false;
//                    }
//                }
//                if (!bReturn) {
//                    broadMsgRoomPlayer(com.build());
//                }
//                setTableStatus(HbgzpConstants.TABLE_STATUS_CHUI);
//                return bReturn;
//            } else {
//                return true;
//            }
//        }
//        return false;

        if (getPlayerCount() < getMaxPlayerCount()) {
            return false;
        }
        for (Player player : getSeatMap().values()) {
        	if(!player.isRobot()){
                if(daidingpao>=1){
                    if (!(player.getState() == player_state.ready||player.getState() == player_state.play))
                        return false;
                }else {
                    if(player.getState() != player_state.ready)
                        return false;
                }
            }
        }
        if(finishFapai==1)
            return false;
        changeTableState(table_state.play);
        if (daidingpao>0) {
            boolean piaoFenOver = true;
            for (HbgzpPlayer player : playerMap.values()) {
                if(player.getPiaoFen()==-1){
                    piaoFenOver = false;
                    break;
                }
            }
            if(!piaoFenOver){
                if (isSendPiaoFenMsg==0 && finishFapai==0) {
                    LogUtil.msgLog.info("Hbgzp|sendPiaoFen|" + getId() + "|" + getPlayBureau());
                    for (HbgzpPlayer player : playerMap.values()) {
                    	ComRes.Builder msg = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_hbgzp_piaofen);
                        if(player.getPiaoFen()==-1){
                        	if(daidingpao==2){
                              	 msg.addParams(player.getdPiaofen());
                             }else{
                              	 msg.addParams(paofen);
                             }
                             player.writeSocket(msg.build());
                        }
                    }
                    isSendPiaoFenMsg = 1;
                }
                return false;
            }
        }
        return true;
    
    }

    public static final List<Integer> wanfaList = Arrays.asList(
            GameUtil.play_type_hubai_gezipai);

    public static void loadWanfaTables(Class<? extends BaseTable> cls) {
        for (Integer integer : wanfaList) {
            TableManager.wanfaTableTypesPut(integer, cls);
        }
    }

    public int getIsAutoPlay() {
        return isAutoPlay;
    }

    public void setIsAutoPlay(int isAutoPlay) {
        this.isAutoPlay = isAutoPlay;
    }

    public void logFaPaiTable() {
        StringBuilder sb = new StringBuilder();
        sb.append("Hbgzp");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append("faPai");
        sb.append("|").append(playType);
        sb.append("|").append(maxPlayerCount);
        sb.append("|").append(getPayType());
        sb.append("|").append(lastWinSeat);
        LogUtil.msg(sb.toString());
    }

    public void logFaPaiPlayer(HbgzpPlayer player, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("Hbgzp");
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

    public void logAction(HbgzpPlayer player, int action, List<Hbgzp> mjs, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("Hbgzp");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append(player.isAutoPlaySelf() ? 1 : 0);
        String actStr = "unKnown-" + action;
        if (action == HbgzpDisAction.action_peng) {
            actStr = "peng";
        } else if (action == HbgzpDisAction.action_minggang) {
            actStr = "minggang";
        } else if (action == HbgzpDisAction.action_chupai) {
            actStr = "chuPai";
        } else if (action == HbgzpDisAction.action_pass) {
            actStr = "guo";
        } else if (action == HbgzpDisAction.action_angang) {
            actStr = "anGang";
        } else if (action == HbgzpDisAction.action_chi) {
            actStr = "chi";
        }else if (action == HbgzpDisAction.action_hua) {
            actStr = "hua";
        }
        sb.append("|").append(actStr);
        sb.append("|").append(mjs);
        sb.append("|").append(actListToString(actList));
        LogUtil.msg(sb.toString());
    }

    public void logMoMj(HbgzpPlayer player, Hbgzp mj, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("Hbgzp");
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
        sb.append("|").append(player.getHandMajiang());
        LogUtil.msg(sb.toString());
    }

    public void logChuPaiActList(HbgzpPlayer player, Hbgzp mj, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("Hbgzp");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append("chuPaiActList");
        sb.append("|").append(mj);
        sb.append("|").append(actListToString(actList));
        sb.append("|").append(player.getHandMajiang());
        LogUtil.msg(sb.toString());
    }

    public void logActionHu(HbgzpPlayer player, List<Hbgzp> mjs, String daHuNames) {
        StringBuilder sb = new StringBuilder();
        sb.append("Hbgzp");
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
                if (i == HbgzpConstants.ACTION_INDEX_HU) {
                    sb.append("hu");
                } else if (i == HbgzpConstants.ACTION_INDEX_PENG) {
                    sb.append("peng");
                } else if (i == HbgzpConstants.ACTION_INDEX_MINGGANG) {
                    sb.append("mingGang");
                } else if (i == HbgzpConstants.ACTION_INDEX_ANGANG) {
                    sb.append("anGang");
                } else if (i == HbgzpConstants.ACTION_INDEX_CHI) {
                    sb.append("chi");
                } else if (i == HbgzpConstants.ACTION_INDEX_ZIMO) {
                    sb.append("ziMo");
                } else if (i == HbgzpConstants.ACTION_INDEX_HUA) {
                    sb.append("hua");
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



//    boolean ist = true;
    public void sendTingInfo(HbgzpPlayer player) {
    	sendTingInfoReconnect(player);
////    	if(ist){//后端先不做
////    		return;
////    	}
//        if(player.isAutoPlay()){
//            return;
//        }
//        long start = System.currentTimeMillis();
//        DaPaiTingPaiRes.Builder tingInfo = DaPaiTingPaiRes.newBuilder();
//        List<Hbgzp> huCardList = new ArrayList<>(Hbgzp.huCardList);
//        List<Hbgzp> handCardList = player.getHandMajiang();
//        Set<Integer> nohuCards = getNoHuCards(player);
//        if(huCardList == null || huCardList.size() <= 0){
//        	return;
//        }
//        
//        //找出只能组成2张牌型的牌
//        List<Hbgzp> handPais = new ArrayList<>(player.getHandMajiang());
//        HbgzpTool.sortMin(handPais);
//        List<Integer> kouList= getKouZihes(handPais, player.getChiIds());
//        if(kouList.size() > 5){
//        	return;
//        }
////        StringBuffer tingSB = new StringBuffer();
////        List<Integer> yijianciVal = new ArrayList<>();
//        List<Integer> yijianVal = new ArrayList<>();
//        for (Hbgzp card : handCardList) {
//        	List<Hbgzp> copyPais = new ArrayList<>(handCardList);
////        	if(yijianciVal.contains(card.getVal()) && !player.getChiIds().contains(card.getId())){
////        	    continue;
////        	}
//        	int cardVal = card.getVal();
//    		if(Hbgzp.isHuapai(card.getId())){
//    			cardVal = 0-cardVal;
//    		}
//    		if(yijianVal.contains(cardVal) && !player.getChiIds().contains(card.getId())){
//    			continue;
//    		}
//        	copyPais.remove(card);
//        	yijianVal.add(cardVal);
////        	yijianciVal.add(card.getVal());
//        	Set<Integer> canhuVals = new HashSet<>();
//        	for (Hbgzp nowDisCard : huCardList) {
//        		if(nohuCards.contains(nowDisCard.getVal())){
//        			continue;
//        		}
//        		List<Hbgzp> checkTingCopy = new ArrayList<>(copyPais);
//        		HbgzpHuLack lack = player.checkHuNewTing(nowDisCard, true,checkTingCopy);
//        		if (lack.isHu()) {
//        			if(nowDisCard.getId() < -30){
//        				canhuVals.add(0-nowDisCard.getVal());
//        			}else{
//        				canhuVals.add(nowDisCard.getVal());
//        			}
//                }
//			}
//        	if(canhuVals.size() <= 0){
//        		continue;
//        	}
//			 DaPaiTingPaiInfo.Builder ting = DaPaiTingPaiInfo.newBuilder();
//			 ting.setMajiangId(cardVal);
////			 tingSB.append("打：").append(cardVal);
//			 ting.addAllTingMajiangIds(canhuVals);
////			 tingSB.append("听：").append(canhuVals.toString());
////			 ting.addTingMajiangIds(81);
//			 tingInfo.addInfo(ting.build());
//		}
//        if (tingInfo.getInfoCount() > 0) {
//            player.writeSocket(tingInfo.build());
//        }
//        long timeUse = System.currentTimeMillis() - start;
//        if(timeUse > 50){
//            StringBuilder sb = new StringBuilder("Hbgzp|sendTingInfo");
//            sb.append("|").append(timeUse);
//            sb.append("|").append(start);
//            sb.append("|").append(getId());
//            sb.append("|").append(getPlayBureau());
//            sb.append("|").append(player.getUserId());
//            sb.append("|").append(player.getSeat());
//            sb.append("|").append(player.getHandPais());
//            LogUtil.monitorLog.info("------------------------------"+sb.toString());
//        }
////        System.out.println("听牌检测，用时："+(System.currentTimeMillis()-start)+"，数据："+tingSB);
    }
    
    public void sendTingInfoReconnect(HbgzpPlayer player) {
//    	if(ist){//后端先不做
//    		return;
//    	}
//    	StringBuffer sb1 = new StringBuffer();
    	if(player.isAutoPlay()){
    		return;
    	}
    	long start = System.currentTimeMillis();
    	DaPaiTingPaiRes.Builder tingInfo = DaPaiTingPaiRes.newBuilder();
    	List<Hbgzp> huCardList = new ArrayList<>(Hbgzp.huCardList);
    	List<Hbgzp> handCardList = player.getHandMajiang();
    	Set<Integer> nohuCards = getNoHuCards(player);
    	if(huCardList == null || huCardList.size() <= 0){
    		return;
    	}
    	
    	//找出只能组成2张牌型的牌
    	List<Hbgzp> handPais = new ArrayList<>(player.getHandMajiang());
    	HbgzpTool.sortMin(handPais);
    	List<Integer> kouList= getKouZihes(handPais, player.getChiIds());
    	if(kouList.size() > 5){
    		return;
    	}
    	
    	if(player.isAlreadyMoMajiang()){
//    		List<Integer> yijianciVal = new ArrayList<>();
//    		List<Integer> yijianhuaVal = new ArrayList<>();
    		List<Integer> yijianVal = new ArrayList<>();
    		
        	for (Hbgzp card : handCardList) {
        		List<Hbgzp> copyPais = new ArrayList<>(handCardList);
//        		if(!Hbgzp.isHuapai(card.getId()) && yijianciVal.contains(card.getVal()) && !player.getChiIds().contains(card.getId())){
//        			continue;
//        		}
//        		if(Hbgzp.isHuapai(card.getId()) && yijianhuaVal.contains(card.getVal()) && !player.getChiIds().contains(card.getId())){
//        			continue;
//        		}
//        		if(Hbgzp.isHuapai(card.getId())){
//        			yijianhuaVal.add(card.getVal());
//        		}
        		int cardVal = card.getVal();
        		if(Hbgzp.isHuapai(card.getId())){
        			cardVal = 0-cardVal;
        		}
        		if(yijianVal.contains(cardVal)  && !player.getChiIds().contains(card.getId())){
        			continue;
        		}
        		yijianVal.add(cardVal);
    			copyPais.remove(card);
//        		yijianciVal.add(card.getVal());
    			
        		Set<Integer> canhuVals = new HashSet<>();
        		for (Hbgzp nowDisCard : huCardList) {
        			if(nohuCards.contains(nowDisCard.getVal())){
        				continue;
        			}
        			List<Hbgzp> checkTingCopy = new ArrayList<>(copyPais);
        			HbgzpHuLack lack = player.checkHuNewTing(nowDisCard, true,checkTingCopy);
        			if (lack.isHu()) {
        				if(nowDisCard.getId() < -30){
            				canhuVals.add(0-nowDisCard.getVal());
            			}else{
            				canhuVals.add(nowDisCard.getVal());
            			}
        			}
        			
        		}
        		if(canhuVals.size() <= 0){
        			continue;
        		}
        		DaPaiTingPaiInfo.Builder ting = DaPaiTingPaiInfo.newBuilder();
        		ting.setMajiangId(cardVal);
        		ting.addAllTingMajiangIds(canhuVals);
//    			 ting.addTingMajiangIds(81);
        		tingInfo.addInfo(ting.build());
//        		 sb1.append("打：").append(Hbgzp.toStringVal(cardVal));
//    			 sb1.append("听：").append(canhuVals.toString());
        	}
    	}else{
    		List<Hbgzp> copyPais = new ArrayList<>(handCardList);
    		Set<Integer> canhuVals = new HashSet<>();
    		for (Hbgzp nowDisCard : huCardList) {
    			if(nohuCards.contains(nowDisCard.getVal())){
    				continue;
    			}
    			List<Hbgzp> checkTingCopy = new ArrayList<>(copyPais);
    			HbgzpHuLack lack = player.checkHuNewTing(nowDisCard, true,checkTingCopy);
    			if (lack.isHu()) {
    				canhuVals.add(nowDisCard.getVal());
    			}
    			
    		}
    		DaPaiTingPaiInfo.Builder ting = DaPaiTingPaiInfo.newBuilder();
    		ting.setMajiangId(0);
    		ting.addAllTingMajiangIds(canhuVals);
    		tingInfo.addInfo(ting.build());
    	
    	}
    	if (tingInfo.getInfoCount() > 0) {
    		player.writeSocket(tingInfo.build());
    	}
    	long timeUse = System.currentTimeMillis() - start;
    	if(timeUse > 50){
    		StringBuilder sb = new StringBuilder("Hbgzp|sendTingInfo");
    		sb.append("|").append(timeUse);
    		sb.append("|").append(start);
    		sb.append("|").append(getId());
    		sb.append("|").append(getPlayBureau());
    		sb.append("|").append(player.getUserId());
    		sb.append("|").append(player.getSeat());
    		sb.append("|").append(player.getHandPais());
    		LogUtil.monitorLog.info("------------------------------"+sb.toString());
    	}
//    	 System.out.println(player.getFlatId()+"听牌检测，用时："+(System.currentTimeMillis()-start)+"，数据："+sb1);
    }
    
    

    public List<Integer> getKouZihes(List<Hbgzp> handPais,List<Integer> jianCards) {
    	List<Integer> kouList = new ArrayList<>();
    	List<Hbgzp> handPaisCopy;
    	List<Hbgzp> rmList;
    	for (Hbgzp gzp : handPais) {
    		 int val = gzp.getVal();
    		 List<int[]> paiZus = HbgzpConstants.getPaiZu(val);//牌组
    		 if(paiZus != null && paiZus.size() > 0){
    	        	for (int i = 0; i < paiZus.size(); i++) {//遍历牌组，坎在最前面
    	        		int[] paiZu = paiZus.get(i);
    	        		handPaisCopy = new ArrayList<>(handPais);
    	        		rmList = HbgzpTool.removeVals(handPaisCopy, paiZu,jianCards,null);//手牌里是否有这个牌组删除
    	        		if(rmList != null){
    	        			break;
    	        		}
    	        		if(i+1 == paiZus.size()){
    	        			kouList.add(gzp.getId());
    	        		}
    	        	}
    	        }
		}
    	return kouList;
    	
//        if (handPais.size() < 3) {
//        	for (Hbgzp gzp : handPais) {
//        		kouList.add(gzp.getId());
//			}
//        	return;
//        }
//        //拆顺
//        Hbgzp card = handPais.get(0);//排过序的那到第一张
//        int val = card.getVal();
//    	List<int[]> paiZus = HbgzpConstants.getPaiZu(val);//牌组
//        if(paiZus != null && paiZus.size() > 0){
//        	for (int i = 0; i < paiZus.size(); i++) {//遍历牌组，坎在最前面
//        		int[] paiZu = paiZus.get(i);
//            	handPaisCopy = new ArrayList<>(handPais);
//                rmList = HbgzpTool.removeVals(handPaisCopy, paiZu,jianCards);//手牌里是否有这个牌组删除
//                if(rmList == null){//找不到牌组删除  进入下一个  全部都删不掉则表示不能胡
//                	if(i+1 == paiZus.size()){
//                		handPaisCopy.remove(card);
//                		kouList.add(card.getId());
//                    	getKouZihes(kouList, handPaisCopy, jianCards);
//                    }else{
//                    	continue;
//                    }
//                }else{
//                	if (handPaisCopy.size() == 0){//全都删完了，则表示可以胡
//                		return;
//                    } else {//进入下一次循环
//                    	getKouZihes(kouList, handPaisCopy, jianCards);
//                    }
//                }
//                
//            }
//        }
    }

    
    public Set<Integer> getNoHuCards(HbgzpPlayer player){
    	Set<Integer> huCardList = new HashSet<>();
//    	huCardList.addAll(player.get);

        return huCardList;
    }
	@Override
	public boolean isPlaying() {
		if (super.isPlaying()) {
			return true;
		}
		return getTableStatus() == HbgzpConstants.TABLE_STATUS_CHUI;
	}

	public HbgzpHuLack filtrateHu(HbgzpPlayer player,List<HbgzpHuLack> pao,List<HbgzpHuLack> ping,Hbgzp disMajiang){
        List<HbgzpHuLack> zaiTi=new ArrayList<>();
        List<HbgzpHuLack> hu=new ArrayList<>();
        if (pao!=null&&!pao.isEmpty()){
            for (HbgzpHuLack lack:pao){
                //跑胡不可能胡息为0（飘胡）
                int allHuxi=lack.getHuxi() + player.getOutHuxi()+ player.getAllPaiSuanZi()+(disMajiang==null?0:disMajiang.getSuanzi());
                if (allHuxi < getHupaizi())
                    continue;
                hu.add(lack);
            }
            //多个栽提胡牌型取最大胡息
            if(zaiTi.size()>=1){
                return getMaxHu(player,zaiTi);
            }
        }
        if (ping!=null&&!ping.isEmpty()){
            for (HbgzpHuLack lack:ping){
                int allHuxi=lack.getHuxi() + player.getOutHuxi() + player.getAllPaiSuanZi()+(disMajiang==null?0:disMajiang.getSuanzi());
                if (allHuxi >= getHupaizi())
                    hu.add(lack);
            }
        }
        if(hu.size()>=1){
            return getMaxHu(player,hu);
        }
        return null;
    }
	public HbgzpHuLack getMaxHu(HbgzpPlayer player,List<HbgzpHuLack> hu){
        int maxtun=-1;
        HbgzpHuLack lack1=null;
        for (HbgzpHuLack lack:hu){
            int point = lack.getHuxi()+player.getOutHuxi()+ player.getAllPaiSuanZi();
            int tun=countXiTun(point,player);
            if(maxtun<tun){
                maxtun=tun;
                lack1=lack;
                lack1.setMingTang(new ArrayList<>());
                lack1.setFinallyPoint(maxtun);
            }
//            else if(maxtun==tun && maxXing < xing){
//            	lack1=lack;
//                lack1.setMingTang(new ArrayList<>());
//                lack1.setFinallyPoint(maxtun);
//            }
        }
        player.setHuxi(lack1.getHuxi());
        return lack1;
    }
	
	
	  private int countXiTun(int huxi,HbgzpPlayer player){
        int tun=0;
        tun = huxi/hupaizi;
        
        //溜花还是十个花
        if(shihua == 1){
        	tun += getHuapaiCount(player);
        }else if(shihua ==2){
        	tun+=1;
        }
        if(diFen>0)
            tun*=diFen;
        return tun;
    }
	  public synchronized void piaoFen(HbgzpPlayer player,int fen){
	        if (player.getPiaoFen()!=-1)
	            return;
	        if(daidingpao==0)
	            return;
	        if(daidingpao==2){
	        	if(fen < player.getdPiaofen()){
	        		return;
	        	}
	        }else{
	        	if(fen > paofen){
	        		return;
	        	}
	        }
	        player.setPiaoFen(fen);
	        StringBuilder sb = new StringBuilder("hbgzp");
	        sb.append("|").append(getId());
	        sb.append("|").append(getPlayBureau());
	        sb.append("|").append(player.getUserId());
	        sb.append("|").append(player.getSeat());
	        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
	        sb.append("|").append("piaoFen").append("|").append(fen);
	        LogUtil.msgLog.info(sb.toString());
	        int confirmTime=0;
	        for (Map.Entry<Integer, HbgzpPlayer> entry : seatMap.entrySet()) {
	            entry.getValue().writeComMessage(WebSocketMsgType.res_code_hbgzp_broadcast_piaofen, (int)player.getUserId(),player.getPiaoFen());
	            if(entry.getValue().getPiaoFen()!=-1)
	                confirmTime++;
	        }
	        if (confirmTime == maxPlayerCount) {
	            checkDeal(player.getUserId());
	        }
	        if(daidingpao == 2){
	        	player.setdPiaofen(fen);
	        }
	    }
	
    public int getHupaizi() {
		return hupaizi;
	}

	@Override
    public String getGameName() {
        return "湖北个子牌";
    }
}
