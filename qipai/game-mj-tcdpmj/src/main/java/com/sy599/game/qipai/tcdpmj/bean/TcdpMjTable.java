package com.sy599.game.qipai.tcdpmj.bean;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.qipai.tcdpmj.rule.TcdpMjingTang;
import com.sy599.game.qipai.tcdpmj.tool.*;
import com.sy599.game.qipai.tcdpmj.tool.huTool.HuTool;
import com.sy599.game.qipai.tcdpmj.tool.huTool.TingTool;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
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
import com.sy599.game.msg.serverPacket.ComMsg.ComRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.DaPaiTingPaiInfo;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.DaPaiTingPaiRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.MoMajiangRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.PlayMajiangRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.TingPaiRes;
import com.sy599.game.msg.serverPacket.TableMjResMsg.ClosingMjInfoRes;
import com.sy599.game.msg.serverPacket.TableMjResMsg.ClosingMjPlayerInfoRes;
import com.sy599.game.msg.serverPacket.TableRes;
import com.sy599.game.msg.serverPacket.TableRes.CreateTableRes;
import com.sy599.game.msg.serverPacket.TableRes.DealInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes;
import com.sy599.game.qipai.tcdpmj.constant.TcdpMjConstants;
import com.sy599.game.qipai.tcdpmj.constant.TcdpMj;
import com.sy599.game.qipai.tcdpmj.rule.MjRobotAI;
import com.sy599.game.udplog.UdpLogger;
import com.sy599.game.util.DataMapUtil;
import com.sy599.game.util.GameUtil;
import com.sy599.game.util.JacksonUtil;
import com.sy599.game.util.JsonWrapper;
import com.sy599.game.util.LangHelp;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.PayConfigUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.util.StringUtil;
import com.sy599.game.util.TimeUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;


public class TcdpMjTable extends BaseTable {
    /**
	 * 当前打出的牌
	 */
    private List<TcdpMj> nowDisCardIds = new ArrayList<>();
    private Map<Integer, List<Integer>> actionSeatMap = new ConcurrentHashMap<>();
    /**
	 * 玩家位置对应临时操作 当同时存在多个可做的操作时 1当优先级最高的玩家最先操作 则清理所有操作提示 并执行该操作
	 * 2当操作优先级低的玩家先选择操作，则玩家先将所做操作存入临时操作中 当玩家优先级最高的玩家操作后 在判断临时操作是否可执行并执行
	 */
    private Map<Integer, MjTempAction> tempActionMap = new ConcurrentHashMap<>();
    private int maxPlayerCount = 4;
    private List<TcdpMj> leftMajiangs = new ArrayList<>();
	/*** 玩家map */
    private Map<Long, TcdpMjPlayer> playerMap = new ConcurrentHashMap<Long, TcdpMjPlayer>();
	/*** 座位对应的玩家 */
    private Map<Integer, TcdpMjPlayer> seatMap = new ConcurrentHashMap<Integer, TcdpMjPlayer>();
	private List<Integer> huConfirmList = new ArrayList<>();// 胡牌数组
    /**
	 * 摸麻将的seat
	 */
    private int moMajiangSeat;
    /**
	 * 骰子点数
	 **/
    private int dealDice;
    /**
	 * 0点炮胡 1自摸胡
	 **/
    private int canDianPao;
	private int isAutoPlay = 0;// 是否开启自动托管
    private int readyTime = 0 ;
	// 是否加倍：0否，1是
    private int jiaBei=0;
	// 加倍分数：低于xx分进行加倍
    private int jiaBeiFen=0;
	// 加倍倍数：翻几倍
    private int jiaBeiShu=0;

	// 是否勾选首局庄家随机
    private int bankerRand=0;
	// 是否已发牌
    private int finishFapai=0;
	/** 托管1：单局，2：全局 */
    private int autoPlayGlob;
	private int autoTableCount;
	/*** 摸屁股的座标号 */
    private List<Integer> moTailPai = new ArrayList<>();
    //低于below加分
    private int belowAdd=0;
    private int below=0;

    List<Integer> paoHuSeat=new ArrayList<>();
    //最后操作的牌
    private int lastId=0;
    //放杠座位号
    private int fangGangSeat=0;
    //补花牌
    private int buVal=0;
    //出牌数 第一张牌不能吃
    private int disNum=0;
    //只能自摸 不能抢杠胡杠上炮
    private int dianPao=0;
    //飘分 0不飘, 1飘，2定飘
    private int piaoFenType=0;
    //定飘 分数
    private int dingPiao=0;
    //杠牌人的座位
    private int gangSeat=0;
    // 0 名牌 1暗牌
    private int mingAnPai=0;
    // 0 碰一对 1一碰到底
    private int pengType=0;

    @Override
    public void createTable(Player player, int play, int bureauCount, List<Integer> params, List<String> strParams, Object... objects) throws Exception {
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


		// 0局数，1玩法Id
		payType = StringUtil.getIntValue(params, 2, 1);// 支付方式
        dianPao = StringUtil.getIntValue(params, 3, 1);// 1可点炮
        mingAnPai = StringUtil.getIntValue(params, 4, 1);//   明暗牌
        pengType = StringUtil.getIntValue(params, 5, 1);//    碰方式
        piaoFenType = StringUtil.getIntValue(params, 6, 0);// 飘分
		maxPlayerCount = StringUtil.getIntValue(params, 7, 4);// 人数
        dingPiao = StringUtil.getIntValue(params, 8, 0);// 定飘
        isAutoPlay = StringUtil.getIntValue(params, 9, 0);// 托管
        if(isAutoPlay==1) {
            // 默认1分钟
            isAutoPlay=60;
        }
        autoPlayGlob = StringUtil.getIntValue(params, 10, 0);// 单局托管
        if(maxPlayerCount==2){
            jiaBei = StringUtil.getIntValue(params, 11, 0);
            jiaBeiFen = StringUtil.getIntValue(params, 12, 100);
            jiaBeiShu = StringUtil.getIntValue(params, 13, 1);
            int belowAdd = StringUtil.getIntValue(params, 14, 0);
            if(belowAdd<=100&&belowAdd>=0)
                this.belowAdd=belowAdd;
            int below = StringUtil.getIntValue(params, 15, 0);
            if(below<=100&&below>=0){
                this.below=below;
            }
        }
        int buHua = StringUtil.getIntValue(params, 16, 0);// 是否补花
        buVal=buHua==1?211:0;


        changeExtend();
        if (!isJoinPlayerAllotSeat()) {
			// getRoomModeMap().put("1", "1"); //可观战（默认）
        }
    }

    @Override
    public JsonWrapper buildExtend0(JsonWrapper wrapper) {
        for (TcdpMjPlayer player : seatMap.values()) {
            wrapper.putString(player.getSeat(), player.toExtendStr());
        }
        wrapper.putString(5, StringUtil.implode(huConfirmList, ","));
        wrapper.putInt(6, moMajiangSeat);
        wrapper.putInt(8,buVal);
        wrapper.putInt(9, canDianPao);
        JSONArray tempJsonArray = new JSONArray();
        for (int seat : tempActionMap.keySet()) {
            tempJsonArray.add(tempActionMap.get(seat).buildData());
        }
        wrapper.putString("tempActions", tempJsonArray.toString());
        wrapper.putInt(11, maxPlayerCount);
        wrapper.putInt(12, isAutoPlay);
        wrapper.putInt(13,disNum);
        wrapper.putString(15, StringUtil.implode(moTailPai, ","));
        wrapper.putInt(17, jiaBei);
        wrapper.putInt(18, jiaBeiFen);
        wrapper.putInt(19, jiaBeiShu);
        wrapper.putInt(20, bankerRand);
        wrapper.putString(21,StringUtil.implode(paoHuSeat, ","));
        wrapper.putInt(22, autoPlayGlob);
        wrapper.putInt(23, finishFapai);
        wrapper.putInt(24, belowAdd);
        wrapper.putInt(25, below);
        wrapper.putInt(26, lastId);
        wrapper.putInt(27, fangGangSeat);
        wrapper.putInt(28, piaoFenType);
        wrapper.putInt(29, gangSeat);
        wrapper.putInt(30, mingAnPai);
        wrapper.putInt(31, pengType);
        wrapper.putInt(32, buVal);
        wrapper.putInt(33, dingPiao);
        wrapper.putInt(34, dianPao);
        return wrapper;
    }

    @Override
    public void initExtend0(JsonWrapper wrapper) {
        for (TcdpMjPlayer player : seatMap.values()) {
            player.initExtend(wrapper.getString(player.getSeat()));
        }
        String huListstr = wrapper.getString(5);
        if (!StringUtils.isBlank(huListstr)) {
            huConfirmList = StringUtil.explodeToIntList(huListstr);
        }
        moMajiangSeat = wrapper.getInt(6, 0);
        buVal = wrapper.getInt(8, 0);
        canDianPao = wrapper.getInt(9, 1);
        tempActionMap = loadTempActionMap(wrapper.getString("tempActions"));
        maxPlayerCount = wrapper.getInt(11, 4);
        isAutoPlay = wrapper.getInt(12, 0);
        disNum = wrapper.getInt(13, 0);

        if(isAutoPlay ==1) {
            isAutoPlay=60;
        }
        String s = wrapper.getString(15);
        if (!StringUtils.isBlank(s)) {
            moTailPai = StringUtil.explodeToIntList(s);
        }
        jiaBei = wrapper.getInt(17,0);
        jiaBeiFen = wrapper.getInt(18,0);
        jiaBeiShu = wrapper.getInt(19,0);
        bankerRand = wrapper.getInt(20,0);
        s = wrapper.getString(21);
        if (!StringUtils.isBlank(s)) {
            paoHuSeat = StringUtil.explodeToIntList(s);
        }
        autoPlayGlob= wrapper.getInt(22,0);
        finishFapai= wrapper.getInt(23,0);
        belowAdd= wrapper.getInt(24,0);
        below= wrapper.getInt(25,0);
        lastId= wrapper.getInt(26,0);
        fangGangSeat= wrapper.getInt(27,0);
        piaoFenType= wrapper.getInt(28,0);
        gangSeat= wrapper.getInt(29,0);
        mingAnPai= wrapper.getInt(30,0);
        pengType= wrapper.getInt(31,0);
        buVal= wrapper.getInt(32,0);
        dingPiao= wrapper.getInt(33,0);
        dianPao= wrapper.getInt(34,0);
    }

    public int getGangSeat() {
        return gangSeat;
    }

    public int getIsAutoPlay() {
        return isAutoPlay;
    }

    public void setIsAutoPlay(int isAutoPlay) {
        this.isAutoPlay = isAutoPlay;
    }

    public int conformDealDice() {
        Random rand=new Random();
        int dice1=rand.nextInt(6)+1;
        int dice2=rand.nextInt(6)+1;
        dealDice=dice1*10+dice2;
        return dealDice;
    }
    public int getCanDianPao() {
        return canDianPao;
    }

    public void setCanDianPao(int canDianPao) {
        this.canDianPao = canDianPao;
    }

    public int getDisNum() {
        return disNum;
    }

    public void setDisNum(int disNum) {
        this.disNum = disNum;
    }

    public int getBuVal() {
        return buVal;
    }

    public int getMingAnPai() {
        return mingAnPai;
    }

    public void setMingAnPai(int mingAnPai) {
        this.mingAnPai = mingAnPai;
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
        int [] birdSeat=null;
        if (winList.size() == 0 && leftMajiangs.size()<=getMaxPlayerCount()) {

        } else {
            if(gangSeat==winList.get(0)||disNum==0){
                selfMo = true;
            }else if (seatMap.get(winList.get(0)).getHandMajiang().size() % 3 == 2 && winList.get(0) == moMajiangSeat) {
                selfMo = true;
            }
            if (selfMo) {
				// 自摸
                TcdpMjPlayer winner = seatMap.get(winList.get(0));
                int mtFen=2*TcdpMjingTang.getMingTangFen(winner.getHuType(),getMaxPlayerCount());
                winner.changePointArr(0,mtFen);
                int loseFen = winner.calcWithOutGangFen();
                winner.addHuNum();
                //自摸双倍
                int winFen = 0;
                for (TcdpMjPlayer p:seatMap.values()) {
                    if(p.getSeat()!=winner.getSeat()){
                        int extraPoint = countTwoExtraPoint(winner, p);
                        p.changePoint(-loseFen-extraPoint);
                        winner.changePointArr(0,mtFen);
                        winFen+=loseFen+extraPoint;
                    }
                }
                winner.changePoint(winFen);
            }else {
                TcdpMjPlayer losePlayer;
                if(gangSeat!=0){
                    losePlayer=seatMap.get(gangSeat);
                }else{
                    losePlayer= seatMap.get(disCardSeat);
                    losePlayer.addDianPaoNum(1);
                }
                int loseFen=0;
                for (TcdpMjPlayer p:seatMap.values()) {
                    if(winList.contains(p.getSeat())){
                        //赢家处理
                        p.addHuNum();
                        int mtFen=TcdpMjingTang.getMingTangFen(p.getHuType(),getMaxPlayerCount());
                        p.changePointArr(0,mtFen);
                        int winFen = p.calcWithOutGangFen();
                        int extraPoint = countTwoExtraPoint(p, losePlayer);
                        loseFen+=winFen+extraPoint;
                        p.changePoint(winFen+extraPoint);
                    }
                }
                //输家处理
                losePlayer.changePoint(-loseFen);
            }
        }
//        for (TcdpMjPlayer p:seatMap.values()) {
//            System.out.println(selfMo+"============="+p.getName()+":"+StringUtil.implode(p.getPointArr()));
//        }
        //流局算杠分
        for (TcdpMjPlayer p: seatMap.values()) {
            p.changePoint(p.calcGangFen());
        }
        boolean over = playBureau == totalBureau;
        if(autoPlayGlob >0) {
			// //是否解散
            boolean diss = false;
            if(autoPlayGlob ==1) {
            	 for (TcdpMjPlayer seat : seatMap.values()) {
                 	if(seat.isAutoPlay()) {
                     	diss = true;
                     	break;
                     }

                 }
			} else if (autoPlayGlob == 3) {
				diss = checkAuto3();
			}
            if(diss) {
            	 autoPlayDiss= true;
            	over =true;
            }
        }

        ClosingMjInfoRes.Builder res = sendAccountsMsg(over, selfMo, winList, new ArrayList<>(), birdSeat, null, 0, false);
        if (!winList.isEmpty()) {
            if (winList.size() > 1) {
				// 一炮多响设置放炮的人为庄家
                setLastWinSeat(disCardSeat);
            } else {
                setLastWinSeat(winList.get(0));
            }
		} else if (leftMajiangs.isEmpty()) {// 黄庄
            setLastWinSeat(moMajiangSeat);
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
        for (TcdpMjPlayer player : seatMap.values()) {
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
		// if(autoPlayGlob==3) {
		boolean diss2 = false;
		for (TcdpMjPlayer seat : seatMap.values()) {
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
		// }
		return diss;
	}


    private int countTwoExtraPoint(TcdpMjPlayer win, TcdpMjPlayer lose){
        int extra=0;
        //飘分
        if(piaoFenType>0)
            extra=extra+win.getPiaoFen()+lose.getPiaoFen();
        return extra;
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
		userLog.setType(creditMode == 1 ? 2 : 1 );
        userLog.setMaxPlayerCount(maxPlayerCount);
        userLog.setGeneralExt(buildGeneralExtForPlaylog().toString());
        long logId = TableLogDao.getInstance().save(userLog);
        saveTableRecord(logId, over, playBureau);
        for (TcdpMjPlayer player : playerMap.values()) {
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
                tempMap.put("nowDisCardIds", StringUtil.implode(MjHelper.toMajiangIds(nowDisCardIds), ","));
            }
            if (tempMap.containsKey("leftPais")) {
                tempMap.put("leftPais", StringUtil.implode(MjHelper.toMajiangIds(leftMajiangs), ","));
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
    public int getPlayerCount() {
        return playerMap.size();
    }

    @Override
    protected void sendDealMsg() {
        sendDealMsg(0);
    }

    @Override
    protected void sendDealMsg(long userId) {
        addPlayLog(disCardRound + "_" + lastWinSeat + "_" + MjDisAction.action_dice + "_" + dealDice);
        conformDealDice();
        for (TcdpMjPlayer tablePlayer : seatMap.values()) {
            DealInfoRes.Builder res = DealInfoRes.newBuilder();
            List<Integer> actionList=null;
            if (lastWinSeat == tablePlayer.getSeat()) {
                actionList = tablePlayer.checkMo();
                if (actionList.contains(1)) {
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
            logFaPaiPlayer(tablePlayer, null);
            if(tablePlayer.isAutoPlay()) {
            	addPlayLog(getDisCardRound() + "_" +tablePlayer.getSeat() + "_" + TcdpMjConstants.action_tuoguan + "_" +1+ tablePlayer.getExtraPlayLog());
            }
        }
        sendFirstAct(seatMap.get(lastWinSeat));
        if (playBureau == 1) {
            setCreateTime(new Date());
        }
    }

    public void moMajiang(TcdpMjPlayer player, boolean buhua) {
        if (state != table_state.play) {
            return;
        }
		// 摸牌
        TcdpMj majiang = null;
        if (disCardRound != 0) {
			// 玩家手上的牌是双数，已经摸过牌了
            if (player.isAlreadyMoMajiang()) {
                return;
            }
            if (getLeftMajiangCount()==0) {
                calcOver();
                return;
            }
            if (GameServerConfig.isDebug() && zp != null && zp.size()==1) {
                majiang= TcdpMj.getMajiangByValue(zp.get(0).get(0));
                zp.clear();
            }else {
                majiang = getLeftMajiang();
            }
        }
        // 检查摸牌
        clearActionSeatMap();
        if (disCardRound == 0) {
            return;
        }
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + MjDisAction.action_moMjiang + "_" + majiang.getId() + player.getExtraPlayLog());
        player.moMajiang(majiang);
        lastId=majiang.getId();
        gangSeat=0;
        setMoMajiangSeat(player.getSeat());
        player.setPassHu(0);
        player.getPassPengVals().clear();
        List<Integer> arr = player.checkMo();
        if (!arr.isEmpty()) {
            addActionSeat(player.getSeat(), arr);
        }
        logMoMj(player, majiang, arr);
        if(getLeftMajiangCount()==0)
            if(arr.isEmpty()||(!arr.isEmpty()&&arr.get(TcdpMjConstants.ACTION_INDEX_HU)==0)){
                sendMoRes(player,majiang,new ArrayList<>(),buhua);
                calcOver();
                return;
            }
        sendMoRes(player,majiang,arr,buhua);
        sendTingInfo(player);
    }

    public void sendMoRes(TcdpMjPlayer player, TcdpMj mj, List<Integer> acts, boolean buhua){
        MoMajiangRes.Builder res = MoMajiangRes.newBuilder();
        res.setUserId(player.getUserId() + "");
        res.setSeat(player.getSeat());
        res.setRemain(getLeftMajiangCount());
        res.setNextDisCardIndex(nowDisCardSeat);
        for (TcdpMjPlayer seat : seatMap.values()) {
            if (seat.getUserId() == player.getUserId()) {
                MoMajiangRes.Builder copy = res.clone();
                copy.addAllSelfAct(acts);
                if (mj != null) {
                    copy.setMajiangId(mj.getId());
                }
                seat.writeSocket(copy.build());
            } else {
                if(!buhua)
                    seat.writeSocket(res.build());
            }
        }
    }


    /**
     *
     * @param act 对应下标参照{@link MjDisAction}中的静态变量
     * @return
     */
    public List<Integer> getActs(int...act){
        List<Integer> acts=new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            if(i>act.length-1){
                acts.add(0);
            }else {
                acts.add(act[i]);
            }
        }
        return acts;
    }


    /**
	 * 玩家表示胡
	 *
	 * @param player

	 */
    private void hu(TcdpMjPlayer player, int action) {
        if (state != table_state.play) {
            return;
        }
        List<Integer> actionList = actionSeatMap.get(player.getSeat());
		if (actionList == null || actionList.get(TcdpMjConstants.ACTION_INDEX_HU) != 1) {// 如果集合为空或者第一操作不为胡，则返回
            return;
        }
        if (huConfirmList.contains(player.getSeat())) {
            return;
        }else {
            // 加入胡牌数组
            addHuList(player.getSeat());
            if(otherHu(player)){
                removeActionSeat(player.getSeat());
                player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_mj);
                return;
            }
        }
        for(Integer huSeat:huConfirmList){
            dealHu(seatMap.get(huSeat),action);
        }
        calcOver();
    }

    private void dealHu(TcdpMjPlayer player, int action) {
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        List<TcdpMj> huHand = new ArrayList<>(player.getHandMajiang());
        boolean zimo = player.isAlreadyMoMajiang();
        List<Integer> ids=new ArrayList<>(player.getHandPais());
        if(lastId!=0){
            if(!ids.contains(lastId))
                ids.add(lastId);
        }
        if(!HuTool.isHuNew(ids,buVal))
            return;
        else {
            List<Integer> huType = TcdpMjingTang.getHuType(MjHelper.toMajiang(ids),gangSeat!=0);
            player.setHuType(huType);
        }
        if (!zimo) {
            builder.setFromSeat(disCardSeat);
        } else {
            builder.addHuArray(TcdpMjConstants.HU_ZIMO);
        }
        buildPlayRes(builder, player, action, MjHelper.toMajiang(ids));
        if (zimo) {
            builder.setZimo(1);
        }
        if (!huConfirmList.isEmpty()) {
            builder.addExt(StringUtil.implode(huConfirmList, ","));
        }
        // 胡
        for (TcdpMjPlayer seat : seatMap.values()) {
            // 推送消息
            seat.writeSocket(builder.build());
        }
//        for (Player roomPlayer : roomPlayerMap.values()) {
//            PlayMajiangRes.Builder copy = builder.clone();
//            roomPlayer.writeSocket(copy.build());
//        }

        changeDisCardRound(1);
        List<TcdpMj> huPai = new ArrayList<>();
        huPai.add(huHand.get(huHand.size() - 1));
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + MjHelper.toMajiangStrs(huPai) + "_"+ player.getExtraPlayLog());
        logActionHu(player, new ArrayList<>(), "");
    }

    private boolean otherHu(TcdpMjPlayer player){
        for(Map.Entry<Integer,List<Integer>> entry:actionSeatMap.entrySet()){
            if(player.getSeat()==entry.getKey())
                continue;
            if(entry.getValue().get(TcdpMjConstants.ACTION_INDEX_HU)==1)
                return true;
        }
        return false;
    }

    private void buildPlayRes(PlayMajiangRes.Builder builder, Player player, int action, List<TcdpMj> majiangs) {
        TcdpMjResTool.buildPlayRes(builder, player, action, majiangs);
    }

    private boolean isCalcOver() {
        List<Integer> huActionList = getHuSeatByActionMap();
        boolean over = false;
        if (!huActionList.isEmpty()) {
            over = true;
            for (int huseat : huActionList) {
                if (!huConfirmList.contains(huseat) &&
                        !(tempActionMap.containsKey(huseat) && tempActionMap.get(huseat).getAction() == MjDisAction.action_hu)) {
                    over = false;
                    break;
                }
            }
        }

        if (!over) {
            TcdpMjPlayer disMajiangPlayer = seatMap.get(disCardSeat);
            for (int huseat : huActionList) {
                if (huConfirmList.contains(huseat)) {
                    continue;
                }
                PlayMajiangRes.Builder disBuilder = PlayMajiangRes.newBuilder();
                TcdpMjPlayer seatPlayer = seatMap.get(huseat);
                buildPlayRes(disBuilder, disMajiangPlayer, 0, null);
                List<Integer> actionList = actionSeatMap.get(huseat);
                disBuilder.addAllSelfAct(actionList);
                seatPlayer.writeSocket(disBuilder.build());
            }
        }

        return over;
    }


    public boolean canHu(TcdpMjPlayer player){
        List<Integer> huActionList = getHuSeatByActionMap();
        if (!huActionList.isEmpty()) {
            int seat = player.getSeat();
            if(actionSeatMap.containsKey(seat)&&actionSeatMap.get(seat).get(0)==1)
                return true;
        }
        return false;
    }

    /**
	 * 碰杠
	 *
	 * @param player
	 * @param majiangs
	 * @param action
	 */
    private void chiPengGang(TcdpMjPlayer player, List<TcdpMj> majiangs, int action) {
        if (state != table_state.play) {
            return;
        }
        logAction(player, action, majiangs, null);
        if (majiangs == null || majiangs.isEmpty()) {
            return;
        }
        if (!checkAction(player, majiangs, new ArrayList<>(), action)) {
			LogUtil.msg("有优先级更高的操作需等待！");
            player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip);
            return;
        }
        if(huConfirmList.size()>0){
            for(Integer huSeat:huConfirmList){
                dealHu(seatMap.get(huSeat),MjDisAction.action_hu);
            }
            calcOver();
            return;
        }
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        TcdpMj disMajiang = null;
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
            sameCount = MjHelper.getMajiangCount(majiangs, majiangs.get(0).getVal());
        }
		// 如果是杠 后台来找出是明杠还是暗杠
        if (action == MjDisAction.action_minggang) {
            majiangs = MjHelper.getMajiangList(player.getHandMajiang(), majiangs.get(0).getVal());
            sameCount = majiangs.size();
            if (sameCount >= 4) {
				// 有4张一样的牌是暗杠
                action = MjDisAction.action_angang;
                majiangs=majiangs.subList(0,4);
            }
			// 其他是明杠
        }
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return;
        }
        if (action == MjDisAction.action_peng) {
            boolean can = canPeng(player, majiangs, sameCount);
            if (!can) {
                return;
            }
            if(pengType==0)
                player.setCanPeng(false);
        }else if (action == MjDisAction.action_chi) {
            return;
        }else if (action == MjDisAction.action_angang) {
            boolean can = canAnGang(player, majiangs, sameCount);
            if (!can) {
                return;
            }
            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + MjHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
        } else if (action == MjDisAction.action_minggang) {
            boolean can = canMingGang(player, majiangs, sameCount);
            if (!can) {
                return;
            }
            ArrayList<TcdpMj> mjs = new ArrayList<>(majiangs);
            if (sameCount == 3) {
                mjs.add(disMajiang);
            }else {
                player.setCanPeng(true);
            }
            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + MjHelper.toMajiangStrs(mjs) + player.getExtraPlayLog());
        } else {
            return;
        }
        if (disMajiang != null) {
            if ((action == MjDisAction.action_minggang && sameCount == 3) || action == MjDisAction.action_peng || action == MjDisAction.action_chi) {
                if (action == MjDisAction.action_chi) {
					majiangs.add(1, disMajiang);// 吃的牌放第二位
                } else {
                    majiangs.add(disMajiang);
                }
                builder.setFromSeat(disCardSeat);
                seatMap.get(disCardSeat).removeOutPais(nowDisCardIds);
            }
        }
        switch (action){
            case MjDisAction.action_minggang:
            case MjDisAction.action_angang:
                gang(builder,player,majiangs,action,sameCount);
                break;
            default:
                chiPeng(builder, player, majiangs, action);
                break;
        }
    }

    private void chiPeng(PlayMajiangRes.Builder builder, TcdpMjPlayer player, List<TcdpMj> majiangs, int action) {
        if (action == MjDisAction.action_peng &&actionSeatMap.get(player.getSeat()).get(TcdpMjConstants.ACTION_INDEX_MINGGANG) == 1) {
            // 可以碰也可以杠
            player.addPassGangVal(majiangs.get(0).getVal());
        }
        player.addOutPais(majiangs, action, disCardSeat);
        buildPlayRes(builder, player, action, majiangs);
        List<Integer> actList = removeActionSeat(player.getSeat());
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + MjHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
        setNowDisCardSeat(player.getSeat());
        for (TcdpMjPlayer seatPlayer : seatMap.values()) {
			// 推送消息
            PlayMajiangRes.Builder copy = builder.clone();
            if (actionSeatMap.containsKey(seatPlayer.getSeat())) {
                copy.addAllSelfAct(actionSeatMap.get(seatPlayer.getSeat()));
            }
            seatPlayer.writeSocket(copy.build());
        }
        sendTingInfo(player);
        logAction(player, action, majiangs, actList);

    }

    private void gang(PlayMajiangRes.Builder builder, TcdpMjPlayer player, List<TcdpMj> majiangs, int action ,int disSameCount) {
        player.addOutPais(majiangs, action, disCardSeat);
        List<Integer> actList = removeActionSeat(player.getSeat());
        setNowDisCardSeat(player.getSeat());
        gangSeat=player.getSeat();
        //判断是否抢杠胡，不是则补牌
        boolean hasQGangHu=false;
        if (disSameCount == 1 && canGangHu()) {
            for(Integer seat:seatMap.keySet()){
                if(seat==player.getSeat())
                    continue;
                TcdpMjPlayer p = seatMap.get(seat);
                List<Integer> copyIds = new ArrayList<>(p.getHandPais());
                copyIds.add(majiangs.get(0).getId());
                if(p.getPassHu()==0&&HuTool.isHuNew(copyIds,buVal)){
                    hasQGangHu=true;
                    //双杠抢杠胡会导致lastId与抢杠胡的牌id不一致
                    lastId=majiangs.get(0).getId();
                    List<Integer> acts=new ArrayList<>();
                    acts.add(1);
                    for (int i = 1; i < 6; i++) {
                        acts.add(0);
                    }
                    addActionSeat(p.getSeat(),acts);
                }
            }
            if(hasQGangHu){
                removeActionSeat(player.getSeat());
                LogUtil.msg("tid:" + getId() + " " + player.getName() + "可以被抢杠胡！！");
            }
        }

        //推送杠消息
        for (TcdpMjPlayer seatPlayer : seatMap.values()) {
            // 推送消息
            PlayMajiangRes.Builder copy = builder.clone();
            if(mingAnPai==0||seatPlayer.getSeat()==player.getSeat()){
                buildPlayRes(copy, player, action, majiangs);
            }else {
                List<Integer> list = new ArrayList<>();
                for (int i = 0; i < majiangs.size(); i++) {
                    list.add(0);
                }
                copy.addAllMajiangIds(list);
                copy.setAction(action);
                copy.setUserId(player.getUserId() + "");
                copy.setSeat(player.getSeat());
            }
            if(actionSeatMap.containsKey(seatPlayer.getSeat()))
                copy.addAllSelfAct(actionSeatMap.get(seatPlayer.getSeat()));
            seatPlayer.writeSocket(copy.build());
        }
        logAction(player, action, majiangs, actList);
        if(!hasQGangHu){
            calcPoint(player,action,disSameCount,majiangs);
            checkMo();
        }
    }

    private void calcPoint(TcdpMjPlayer player, int action, int sameCount, List<TcdpMj> majiangs) {
        int lostPoint = 0;
        int getPoint = 0;
        if (action == MjDisAction.action_peng) {
            return;
        } else if (action == MjDisAction.action_angang) {
            // 暗杠相当于自摸每人出2分
            lostPoint = -2;
            getPoint = 2 * (getMaxPlayerCount() - 1);
            for (TcdpMjPlayer seat : seatMap.values()) {
                if (seat.getUserId() == player.getUserId()) {
                    seat.changePointArr(3,getPoint);
                } else {
                    seat.changePointArr(3,lostPoint);
                }
            }
        } else if (action == MjDisAction.action_minggang) {
            if (sameCount == 1) {
                // 碰牌之后再抓一个牌每人出1分
                if (player.isPassGang(majiangs.get(0))) {
                    // 特殊处理 可以碰可以杠的牌 选择了碰 再杠不算分
                    return;
                }
                lostPoint = -1;
                getPoint = 1 * (getMaxPlayerCount() - 1);
                for (TcdpMjPlayer seat : seatMap.values()) {
                    if (seat.getUserId() == player.getUserId()) {
                        seat.changePointArr(4,getPoint);
                    } else {
                        seat.changePointArr(4,lostPoint);
                    }
                }
            }
            // 放杠的人出3分
            if (sameCount == 3) {
                int fen=getMaxPlayerCount()-1;
                TcdpMjPlayer disPlayer = seatMap.get(disCardSeat);
                disPlayer.addDianGangNum();
                disPlayer.changePointArr(2,-fen);
                player.changePointArr(5,fen);
            }
        }
        player.addGangNum();
    }

    /**
	 * 普通出牌
	 *
	 * @param player
	 * @param majiangs
	 * @param action
	 */
    private void chuPai(TcdpMjPlayer player, List<TcdpMj> majiangs, int action) {
        if (state != table_state.play) {
            player.writeComMessage(WebSocketMsgType.res_code_mj_dis_err, action, majiangs.get(0).getId());
            return;
        }
        if (majiangs.size() != 1) {
            player.writeComMessage(WebSocketMsgType.res_code_mj_dis_err, action, majiangs.get(0).getId());
            return;
        }
        if (!tempActionMap.isEmpty()) {
			LogUtil.e(player.getName() + "出牌清理临时操作！");
            clearTempAction();
        }
        if (!player.isAlreadyMoMajiang()) {
			// 还没有摸牌
            player.writeComMessage(WebSocketMsgType.res_code_mj_dis_err, action, majiangs.get(0).getId());
            player.writeErrMsg("等待其他玩家操作");
            return;
        }
        if(!actionSeatMap.isEmpty()){
            if(actionSeatMap.containsKey(player.getSeat()))
                removeActionSeat(player.getSeat());
            else {
                player.writeComMessage(WebSocketMsgType.res_code_mj_dis_err, action, majiangs.get(0).getId());
                return;
            }
        }
		// 普通出牌
        clearActionSeatMap();
        if(majiangs.size()==1)
            lastId=majiangs.get(0).getId();
        logAction(player, action, majiangs, null);
        if(majiangs.get(0).getVal()==buVal){
            player.addOutPais(majiangs, MjDisAction.action_buhua, player.getSeat());
            for (TcdpMjPlayer p:seatMap.values()) {
                p.writeComMessage(WebSocketMsgType.res_code_tcpfmj_bh,player.getSeat(),majiangs.get(0).getId());
            }
            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + MjDisAction.action_buhua + "_" + MjHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
            moMajiang(player,true);
        }else {
            //构建出牌消息
            PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
            builder.setAction(action);
            builder.setUserId(player.getUserId() + "");
            builder.setSeat(player.getSeat());
            recordDisMajiang(majiangs, player);
            player.addOutPais(majiangs, action, player.getSeat());
            disNum++;
            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + MjHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
            setNowDisCardSeat(calcNextSeat(player.getSeat()));
            for (TcdpMjPlayer p : seatMap.values()) {
                List<Integer> list;
                if (p.getUserId() != player.getUserId()) {
                    list = p.checkDisMajiang(majiangs.get(0), this.canDianPao(),mingAnPai);
                    if(list==null||list.isEmpty())
                        continue;
                    if (list.contains(1)) {
                        addActionSeat(p.getSeat(), list);
                        p.setLastCheckTime(System.currentTimeMillis());
                        logChuPaiActList(p, majiangs.get(0), list);
                        if(list.get(TcdpMjConstants.ACTION_INDEX_MINGGANG)==1)
                            fangGangSeat=player.getSeat();
                    }
                }
            }

            for (TcdpMjPlayer seatPlayer : seatMap.values()) {
                PlayMajiangRes.Builder copy = builder.clone();
                List<Integer> actionList;
                // 只推送给胡牌的人改成了推送给所有人但是必须等胡牌的人先答复
                if (actionSeatMap.containsKey(seatPlayer.getSeat())) {
                    actionList = actionSeatMap.get(seatPlayer.getSeat());
                } else {
                    actionList = new ArrayList<>();
                }
                copy.addAllSelfAct(actionList);
                if(mingAnPai==1&&player.getSeat()!=seatPlayer.getSeat()){
                    copy.addMajiangIds(0);
                }else {
                    copy.addAllMajiangIds(MjHelper.toMajiangIds(majiangs));
                }
                seatPlayer.writeSocket(copy.build());

            }
            checkMo();
        }
    }

    public List<Integer> getHuSeatByActionMap() {
        List<Integer> huList = new ArrayList<>();
        for (int seat : actionSeatMap.keySet()) {
            List<Integer> actionList = actionSeatMap.get(seat);
            if (actionList.get(TcdpMjConstants.ACTION_INDEX_HU) == 1) {
				// 胡
                huList.add(seat);
            }
        }
        return huList;
    }

    private void sendDisMajiangAction(PlayMajiangRes.Builder builder, TcdpMjPlayer player) {
        for (TcdpMjPlayer seatPlayer : seatMap.values()) {
            PlayMajiangRes.Builder copy = builder.clone();
            List<Integer> actionList;
			// 只推送给胡牌的人改成了推送给所有人但是必须等胡牌的人先答复
            if (actionSeatMap.containsKey(seatPlayer.getSeat())) {
                actionList = actionSeatMap.get(seatPlayer.getSeat());
            } else {
                actionList = new ArrayList<>();
            }
            copy.addAllSelfAct(actionList);
            seatPlayer.writeSocket(copy.build());
        }
//        for (Player roomPlayer : roomPlayerMap.values()) {
//            PlayMajiangRes.Builder copy = builder.clone();
//            roomPlayer.writeSocket(copy.build());
//        }
    }

    public synchronized void playCommand(TcdpMjPlayer player, List<TcdpMj> majiangs, int action) {
        playCommand(player, majiangs, null, action);
    }

    /**
	 * 出牌
	 *
	 * @param player
	 * @param majiangs
	 * @param action
	 */
    public synchronized void playCommand(TcdpMjPlayer player, List<TcdpMj> majiangs, List<Integer> hucards, int action) {
        if (state != table_state.play) {
            return;
        }
        if (MjDisAction.action_hu == action) {
            hu(player, action);
            return;
        }
		// 手上没有要出的麻将
        if (action != MjDisAction.action_minggang)
            if (!isHandCard(majiangs,player.getHandMajiang())) {
                return;
            }
        changeDisCardRound(1);
        if (action == MjDisAction.action_pass) {
            guo(player, majiangs, action);
        }else if(action != 0) {
            chiPengGang(player, majiangs, action);
        } else {
            chuPai(player, majiangs, action);
        }
		// 记录最后一次动作的时间
        setLastActionTime(TimeUtil.currentTimeMillis());
    }

    private void sendFinshActs(){
        //如果闲家没有可以起手的操作，则通知庄家操作
        boolean bankerFirst=true;
        for (Integer seat:actionSeatMap.keySet()) {
            if(seat!=lastWinSeat){
                List<Integer> list = actionSeatMap.get(seat);
                if(list.get(TcdpMjConstants.ACTION_INDEX_HU)==1||list.get(TcdpMjConstants.ACTION_INDEX_BAOTING)==1)
                    bankerFirst=false;
            }
        }
        if(bankerFirst){
            seatMap.get(lastWinSeat).writeComMessage(WebSocketMsgType.res_code_nxkwmj_finishAct);
        }else {
            seatMap.get(lastWinSeat).writeComMessage(WebSocketMsgType.res_code_nxkwmj_haveAct);
        }
    }

    private boolean isHandCard(List<TcdpMj> majiangs, List<TcdpMj> handCards){
        for (TcdpMj mj:majiangs) {
            if(!handCards.contains(mj))
                return false;
        }
        return true;
    }

    /**
     * pass
     *
     * @param player
     * @param majiangs
     * @param action
     */
    private void guo(TcdpMjPlayer player, List<TcdpMj> majiangs, int action) {
        if (state != table_state.play) {
            return;
        }
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return;
        }
        boolean passHu=false;
        if(actionSeatMap.get(player.getSeat()).get(TcdpMjConstants.ACTION_INDEX_HU)==1){
            passHu=true;
            player.setPassHu(1);

        }
        if(actionSeatMap.get(player.getSeat()).get(TcdpMjConstants.ACTION_INDEX_PENG)==1){
            player.addPassPengVals(nowDisCardIds.get(0).getVal());
        }
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        buildPlayRes(builder, player, action, majiangs);
        builder.setSeat(nowDisCardSeat);
        List<Integer> removeActionList = removeActionSeat(player.getSeat());
        player.writeSocket(builder.build());
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + MjHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
        if (removeActionList.get(0) == 1 && disCardSeat != player.getSeat() && nowDisCardIds.size() == 1) {
			// 漏炮
            player.addPassMajiangVal(nowDisCardIds.get(0).getVal());
        }
        logAction(player, action, majiangs, removeActionList);
        if(passHu&&getLeftMajiangCount()==0){
            calcOver();
            return;
        }
        if (player.isAlreadyMoMajiang()) {
            sendTingInfo(player);
        }
        if(passHu&&huConfirmList.size()>0&&!otherHu(player)){
            for(Integer huSeat:huConfirmList){
                dealHu(seatMap.get(huSeat),MjDisAction.action_hu);
            }
            calcOver();
            return;
        }else {
            refreshTempAction(player);// 先过 后执行临时可做操作里面优先级最高的玩家操作
        }
        if(actionSeatMap.isEmpty()){
            gangSeat=0;
            checkMo();
        }
        sendFinshActs();
    }

    private void recordDisMajiang(List<TcdpMj> majiangs, TcdpMjPlayer player) {
        setNowDisCardIds(majiangs);
        // changeDisCardRound(1);
        setDisCardSeat(player.getSeat());
    }


    public void setNowDisCardIds(List<TcdpMj> nowDisCardIds) {
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
                TcdpMjPlayer player = seatMap.get(nowDisCardSeat);
                //摸排
                moMajiang(player,false);
            }
            robotDealAction();

        } else {
            for (int seat : actionSeatMap.keySet()) {
                TcdpMjPlayer player = seatMap.get(seat);
                if (player != null && player.isRobot()) {
					// 如果是机器人可以直接决定
                    List<Integer> actionList = actionSeatMap.get(seat);
                    if (actionList == null) {
                        continue;
                    }
                    List<TcdpMj> list = new ArrayList<>();
                    if (!nowDisCardIds.isEmpty()) {
                        list = MjQipaiTool.getVal(player.getHandMajiang(), nowDisCardIds.get(0).getVal());
                    }
                    if (actionList.get(TcdpMjConstants.ACTION_INDEX_HU) == 1) {
						// 胡
                        playCommand(player, new ArrayList<TcdpMj>(), MjDisAction.action_hu);

                    } else if (actionList.get(TcdpMjConstants.ACTION_INDEX_ANGANG) == 1) {
                        playCommand(player, list, MjDisAction.action_angang);

                    } else if (actionList.get(TcdpMjConstants.ACTION_INDEX_MINGGANG) == 1) {
                        playCommand(player, list, MjDisAction.action_minggang);

                    } else if (actionList.get(TcdpMjConstants.ACTION_INDEX_PENG) == 1) {
                        playCommand(player, list, MjDisAction.action_peng);
                    }
                }

            }

        }
    }

    @Override
    protected void robotDealAction() {
        if (true) {
            return;
        }
        if (isTest()) {
            int nextseat = getNextDisCardSeat();
            TcdpMjPlayer next = seatMap.get(nextseat);
            if (next != null && next.isRobot()) {
                List<Integer> actionList = actionSeatMap.get(next.getSeat());
                if (actionList != null) {
                    List<TcdpMj> list = null;
                    if (actionList.get(TcdpMjConstants.ACTION_INDEX_HU) == 1) {
						// 胡
                        playCommand(next, new ArrayList<TcdpMj>(), MjDisAction.action_hu);
                    } else if (actionList.get(TcdpMjConstants.ACTION_INDEX_ANGANG) == 1) {
						// 机器人暗杠
                        Map<Integer, Integer> handMap = MjHelper.toMajiangValMap(next.getHandMajiang());
                        for (Entry<Integer, Integer> entry : handMap.entrySet()) {
                            if (entry.getValue() == 4) {
								// 可以暗杠
                                list = MjHelper.getMajiangList(next.getHandMajiang(), entry.getKey());
                            }
                        }
                        playCommand(next, list, MjDisAction.action_angang);

                    } else if (actionList.get(TcdpMjConstants.ACTION_INDEX_MINGGANG) == 1) {
                        Map<Integer, Integer> pengMap = MjHelper.toMajiangValMap(next.getPeng());
                        for (TcdpMj handMajiang : next.getHandMajiang()) {
                            if (pengMap.containsKey(handMajiang.getVal())) {
								// 有碰过
                                list = new ArrayList<>();
                                list.add(handMajiang);
                                playCommand(next, list, MjDisAction.action_minggang);
                                break;
                            }
                        }

                    } else if (actionList.get(TcdpMjConstants.ACTION_INDEX_PENG) == 1) {
                        playCommand(next, list, MjDisAction.action_peng);
                    }
                } else {
                    List<Integer> handMajiangs = new ArrayList<>(next.getHandPais());
                    MjQipaiTool.dropHongzhongVal(handMajiangs);
                    int maJiangId = MjRobotAI.getInstance().outPaiHandle(0, handMajiangs, new ArrayList<Integer>());
                    List<TcdpMj> majiangList = MjHelper.toMajiang(Arrays.asList(maJiangId));
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
            setLastWinSeat(playerMap.get(masterId).getSeat());
        }
        if (lastWinSeat == 0) {
            setLastWinSeat(1);
        }
        setDisCardSeat(lastWinSeat);
        setNowDisCardSeat(lastWinSeat);
        setMoMajiangSeat(lastWinSeat);
        List<Integer> copy = TcdpMjConstants.getMajiangList(3);
        addPlayLog(copy.size() + "");
        List<List<TcdpMj>> list = null;
        if (zp != null&&zp.size()!=0) {
            list = MjTool.fapai(copy, getMaxPlayerCount(), zp);
        } else {
            list = MjTool.fapai(copy, getMaxPlayerCount());
        }
        int i = 1;
        for (TcdpMjPlayer player : seatMap.values()) {
            player.changeState(player_state.play);
            if (player.getSeat() == lastWinSeat) {
                player.dealHandPais(list.get(0));
                continue;
            }
            player.dealHandPais(list.get(i));
            i++;
        }
		// 桌上剩余的牌
        List<TcdpMj> lefts = list.get(getMaxPlayerCount());
        setLeftMajiangs(lefts);
        finishFapai=1;
    }

    @Override
    public void startNext() {
    }

    /**
	 * 初始化桌子上剩余牌
	 *
	 * @param leftMajiangs
	 */
    public void setLeftMajiangs(List<TcdpMj> leftMajiangs) {
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
    public TcdpMj getLeftMajiang() {
        if (this.leftMajiangs.size() > 0) {
            TcdpMj majiang = this.leftMajiangs.remove(0);
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
//            if(actionSeatMap.isEmpty()||actionSeatMap.containsKey(lastWinSeat))
//                return lastWinSeat;
//            int seat = 0;
//            do {
//                seat = calcNextSeat(lastWinSeat);
//                if(actionSeatMap.containsKey(seat))
//                    return seat;
//            } while (seat==lastWinSeat);
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
        res.addExt(canDianPao);             //1
        res.addExt(isAutoPlay);             //2
        res.addExt(0);                      //3

        res.addStrExt(StringUtil.implode(moTailPai, ","));     //0
        res.setMasterId(getMasterId() + "");
        if (leftMajiangs != null) {
            res.setRemain(leftMajiangs.size());
        } else {
            res.setRemain(0);
        }
        res.setDealDice(dealDice);
        List<PlayerInTableRes> players = new ArrayList<>();
        for (TcdpMjPlayer player : playerMap.values()) {
            PlayerInTableRes.Builder playerRes = player.buildPlayInTableInfo(isrecover);
            if (player.getUserId() == userId) {
                playerRes.addAllHandCardIds(player.getHandPais());
            }
            if (player.getSeat() == disCardSeat && nowDisCardIds != null) {
                playerRes.addAllOutCardIds(MjHelper.toMajiangIds(nowDisCardIds));
            }
            playerRes.addRecover(player.getIsEntryTable());
            playerRes.addRecover(player.getSeat() == lastWinSeat ? 1 : 0);
            if (actionSeatMap.containsKey(player.getSeat())) {
				if (!tempActionMap.containsKey(player.getSeat()) && !huConfirmList.contains(player.getSeat())) {// 如果已做临时操作
																												// 则不发送前端可做的操作
																												// 或者已经操作胡了
                    playerRes.addAllRecover(actionSeatMap.get(player.getSeat()));
                }
            }
            players.add(playerRes.build());
        }
        res.addAllPlayers(players);
        int nextSeat = getNextDisCardSeat();
        if (nextSeat != 0) {
            res.setNextSeat(nextSeat);
        }
        res.setRenshu(getMaxPlayerCount());
        res.setLastWinSeat(getLastWinSeat());
        res.addTimeOut((int) TcdpMjConstants.AUTO_TIMEOUT);
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

    public int getPiaoFenType() {
        return piaoFenType;
    }

    public void setPiaoFenType(int piaoFenType) {
        this.piaoFenType = piaoFenType;
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
        dealDice=0;
        clearMoTailPai();
        paoHuSeat.clear();
        readyTime=0;
        finishFapai=0;
        lastId=0;
        fangGangSeat=0;
        disNum=0;
        gangSeat=0;
    }

    public List<Integer> removeActionSeat(int seat) {
        List<Integer> actionList = actionSeatMap.remove(seat);
        saveActionSeatMap();
        return actionList;
    }

    public void addActionSeat(int seat, List<Integer> actionlist) {
        actionSeatMap.put(seat, actionlist);
        TcdpMjPlayer player = seatMap.get(seat);
        addPlayLog(disCardRound + "_" + seat + "_" + MjDisAction.action_hasAction + "_" + StringUtil.implode(actionlist) + player.getExtraPlayLog());
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
            nowDisCardIds = MjHelper.toMajiang(StringUtil.explodeToIntList(info.getNowDisCardIds()));
        }

        if (!StringUtils.isBlank(info.getLeftPais())) {
            try {
                leftMajiangs = MjHelper.toMajiang(StringUtil.explodeToIntList(info.getLeftPais()));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }




    private Map<Integer, MjTempAction> loadTempActionMap(String json) {
        Map<Integer, MjTempAction> map = new ConcurrentHashMap<>();
        if (json == null || json.isEmpty())
            return map;
        JSONArray jsonArray = JSONArray.parseArray(json);
        for (Object val : jsonArray) {
            String str = val.toString();
            MjTempAction tempAction = new MjTempAction();
            tempAction.initData(str);
            map.put(tempAction.getSeat(), tempAction);
        }
        return map;
    }

    /**
	 * 检查优先度 如果同时出现一个事件，按出牌座位顺序优先
	 */
    private boolean checkAction(TcdpMjPlayer player, List<TcdpMj> cardList, List<Integer> hucards, int action) {
		boolean canAction = checkCanAction(player, action);// 是否优先级最高 能执行操作
		if (!canAction) {// 不能操作时 存入临时操作
            int seat = player.getSeat();
            tempActionMap.put(seat, new MjTempAction(seat, action, cardList, hucards));
			// 玩家都已选择自己的临时操作后 选取优先级最高
            if (tempActionMap.size() == actionSeatMap.size()) {
                int maxAction = Integer.MAX_VALUE;
                int maxSeat = 0;
                Map<Integer, Integer> prioritySeats = new HashMap<>();
                int maxActionSize = 0;
                for (MjTempAction temp : tempActionMap.values()) {
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
                TcdpMjPlayer tempPlayer = seatMap.get(maxSeat);
                List<TcdpMj> tempCardList = tempActionMap.get(maxSeat).getCardList();
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
    private void refreshTempAction(TcdpMjPlayer player) {
        tempActionMap.remove(player.getSeat());
		Map<Integer, Integer> prioritySeats = new HashMap<>();// 各位置优先操作
        for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
            int seat = entry.getKey();
            List<Integer> actionList = entry.getValue();
            List<Integer> list = MjDisAction.parseToDisActionList(actionList);
            int priorityAction = MjDisAction.getMaxPriorityAction(list);
            prioritySeats.put(seat, priorityAction);
        }
        int maxPriorityAction = Integer.MAX_VALUE;
        int maxPrioritySeat = 0;
		boolean isSame = true;// 是否有相同操作
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
        Iterator<MjTempAction> iterator = tempActionMap.values().iterator();
        while (iterator.hasNext()) {
            MjTempAction tempAction = iterator.next();
            if (tempAction.getSeat() == maxPrioritySeat) {
                int action = tempAction.getAction();
                List<TcdpMj> tempCardList = tempAction.getCardList();
                List<Integer> tempHuCards = tempAction.getHucards();
                TcdpMjPlayer tempPlayer = seatMap.get(tempAction.getSeat());
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
    public boolean checkCanAction(TcdpMjPlayer player, int action) {
		// 优先度为胡杠补碰吃
        List<Integer> stopActionList = MjDisAction.findPriorityAction(action);
        for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
            if (player.getSeat() != entry.getKey()) {
				// 别人
                boolean can = MjDisAction.canDisMajiang(stopActionList, entry.getValue());
                if (!can) {
                    return false;
                }
                List<Integer> disActionList = MjDisAction.parseToDisActionList(entry.getValue());
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
	 * 是否能碰
	 *
	 * @param player
	 * @param majiangs
	 * @param sameCount
	 * @return
	 */
    private boolean canPeng(TcdpMjPlayer player, List<TcdpMj> majiangs, int sameCount) {
        if (player.isAlreadyMoMajiang()) {
            return false;
        }
        if (sameCount != 2) {
            return false;
        }
        if (nowDisCardIds.isEmpty()) {
            return false;
        }
        if (majiangs.get(0).getVal() != nowDisCardIds.get(0).getVal()) {
            return false;
        }
        return true;
    }

    /**
     * 是否能碰
     *
     * @param player
     * @param majiangs
     * @return
     */
    private boolean canChi(TcdpMjPlayer player, List<TcdpMj> majiangs, TcdpMj disMj) {
        if (player.isAlreadyMoMajiang()||disNum<=1) {
            return false;
        }
        if (nowDisCardIds.isEmpty()) {
            return false;
        }
        TreeSet<Integer> set=new TreeSet();
        set.add(disMj.getVal());
        for (TcdpMj mj:majiangs) {
            set.add(mj.getVal());
        }
        Iterator<Integer> it = set.iterator();
        int x=0;
        while (it.hasNext()){
            if(x==0){
                x=it.next();
            }else {
                if(it.next()-x!=1)
                    return false;
                x=it.next();
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
    private boolean canAnGang(TcdpMjPlayer player, List<TcdpMj> majiangs, int sameCount) {
        if (sameCount < 4) {
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
    private boolean canMingGang(TcdpMjPlayer player, List<TcdpMj> majiangs, int sameCount) {
        List<TcdpMj> handMajiangs = player.getHandMajiang();
        List<Integer> pengList = MjHelper.toMajiangVals(player.getPeng());
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
//            if (nowDisCardIds.size() != 1 || nowDisCardIds.get(0).getVal() != majiangs.get(0).getVal()) {
//                return false;
//            }
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

	// 能否点炮
    public boolean canDianPao() {
        if (dianPao == 1) {
            return true;
        }
        return false;
    }

    /**
	 * @param over
	 * @param selfMo
	 * @param winList
	 * @param birdCardsId
	 *            鸟ID
	 * @param seatBirds
	 *            鸟位置
	 * @param seatBridMap
	 *            鸟分
	 * @param isBreak
	 * @return
	 */
    public ClosingMjInfoRes.Builder sendAccountsMsg(boolean over, boolean selfMo, List<Integer> winList, List<Integer> birdCardsId, int[] seatBirds, Map<Integer, Integer> seatBridMap, int catchBirdSeat, boolean isBreak) {

		// 大结算计算加倍分
        if (over && jiaBei == 1) {
            int jiaBeiPoint = 0;
            int loserCount = 0;
            for (TcdpMjPlayer player : seatMap.values()) {
                if (player.getTotalPoint() > 0 && player.getTotalPoint() < jiaBeiFen) {
                    jiaBeiPoint += player.getTotalPoint() * (jiaBeiShu - 1);
                    player.setTotalPoint(player.getTotalPoint() * jiaBeiShu);
                } else if (player.getTotalPoint() < 0) {
                    loserCount++;
                }
            }
            if (jiaBeiPoint > 0) {
                for (TcdpMjPlayer player : seatMap.values()) {
                    if (player.getTotalPoint() < 0) {
                        player.setTotalPoint(player.getTotalPoint() - (jiaBeiPoint / loserCount));
                    }
                }
            }
        }

        //大结算低于below分+belowAdd分
        if(over&&belowAdd>0&&playerMap.size()==2){
            for (TcdpMjPlayer player : seatMap.values()) {
                int totalPoint = player.getTotalPoint();
                if (totalPoint >-below&&totalPoint<0) {
                    player.setTotalPoint(player.getTotalPoint()-belowAdd);
                }else if(totalPoint < below&&totalPoint>0){
                    player.setTotalPoint(player.getTotalPoint()+belowAdd);
                }
            }
        }

        List<ClosingMjPlayerInfoRes> list = new ArrayList<>();
        List<ClosingMjPlayerInfoRes.Builder> builderList = new ArrayList<>();
        for (TcdpMjPlayer player : seatMap.values()) {
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
                build.setIsHu(lastId);
            }
            if(!selfMo){
                if(gangSeat==player.getSeat()){
                    build.setFanPao(1);
                }else if(gangSeat==0&&player.getSeat()==disCardSeat){
                    build.setFanPao(1);
                }
            }
            if (winList != null && winList.contains(player.getSeat())) {
				// 手上没有剩余的牌放第一位为赢家
                builderList.add(0, build);
            } else {
                builderList.add(build);
            }
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
            for (TcdpMjPlayer player : seatMap.values()) {
                if (player.getWinLoseCredit() > dyjCredit) {
                    dyjCredit = player.getWinLoseCredit();
                }
            }
            for (ClosingMjPlayerInfoRes.Builder builder : builderList) {
                TcdpMjPlayer player = seatMap.get(builder.getSeat());
                calcCommissionCredit(player, dyjCredit);
                builder.setWinLoseCredit(player.getWinLoseCredit());
                builder.setCommissionCredit(player.getCommissionCredit());
            }
        } else if (isGroupTableGoldRoom()) {
            // -----------亲友圈金币场---------------------------------
            for (TcdpMjPlayer player : seatMap.values()) {
                player.setWinGold(player.getTotalPoint() * gtgDifen);
            }
            calcGroupTableGoldRoomWinLimit();
            for (ClosingMjPlayerInfoRes.Builder builder : builderList) {
                TcdpMjPlayer player = seatMap.get(builder.getSeat());
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
        res.addCreditConfig(creditDifen);                        //3ClosingMjInfoRes
        res.addCreditConfig(creditCommission);                   //4
        res.addCreditConfig(creditCommissionMode1);              //5
        res.addCreditConfig(creditCommissionMode2);              //6
        res.addCreditConfig(creditCommissionLimit);              //7
        if (seatBirds != null) {
            res.addAllBirdSeat(DataMapUtil.toList(seatBirds));
        }
        if (birdCardsId != null) {
            res.addAllBird(birdCardsId);
        }
        res.addAllLeftCards(MjHelper.toMajiangIds(leftMajiangs));
        res.setCatchBirdSeat(catchBirdSeat);
        res.addAllIntParams(getIntParams());
        for (TcdpMjPlayer player : seatMap.values()) {
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
        ext.add(masterId + "");                          //2
        ext.add(TimeUtil.formatTime(TimeUtil.now()));    //3
        ext.add(playType + "");                          //4
        ext.add(canDianPao + "");                        //5
        ext.add(lastWinSeat + "");                       //6
        ext.add(isAutoPlay + "");                        //7
        ext.add(isLiuJu() + "");                         //8
        ext.add(over+"");                                //9
        return ext;
    }

    @Override
    public void sendAccountsMsg() {
        ClosingMjInfoRes.Builder builder = sendAccountsMsg(true, false, null, null, null, null, 0, true);
        saveLog(true, 0l, builder.build());
    }

    @Override
    public Class<? extends Player> getPlayerClass() {
        return TcdpMjPlayer.class;
    }

    @Override
    public int getWanFa() {
        return getPlayType();
    }


    @Override
    public void checkReconnect(Player player) {
        ((TcdpMjPlayer) player).checkAutoPlay(0, true);
        if (state == table_state.play) {
            TcdpMjPlayer player1 = (TcdpMjPlayer) player;
            if (player1.getHandPais() != null && player1.getHandPais().size() > 0) {
                sendTingInfo(player1);
            }
        }
        sendPiaoReconnect(player);
        sendFirstAct(player);
        ((TcdpMjPlayer) player).sendGangMsg();
    }

    private void sendFirstAct(Player player){
        if(disNum==0&&!actionSeatMap.isEmpty()){
            boolean bankerFirst=true;
            for (Integer seat:actionSeatMap.keySet()) {
                List<Integer> list = actionSeatMap.get(seat);
                if(seat==lastWinSeat){
                    if(list.get(TcdpMjConstants.ACTION_INDEX_HU)==1){
                        bankerFirst=true;
                        break;
                    }
                }else {
                    if(list.get(TcdpMjConstants.ACTION_INDEX_HU)==1||list.get(TcdpMjConstants.ACTION_INDEX_BAOTING)==1)
                        bankerFirst=false;
                }
            }
            if(!bankerFirst)
                player.writeComMessage(WebSocketMsgType.res_code_nxkwmj_haveAct);
        }
    }

    private void sendPiaoReconnect(Player player){
        if(piaoFenType==0||maxPlayerCount!=getPlayerCount())
            return;
        int count=0;
        for(Map.Entry<Integer, TcdpMjPlayer> entry:seatMap.entrySet()){
            player_state state = entry.getValue().getState();
            if(state==player_state.play||state==player_state.ready)
                count++;
        }
        if(count!=maxPlayerCount)
            return;

        for(Map.Entry<Integer, TcdpMjPlayer> entry:seatMap.entrySet()){
            TcdpMjPlayer p = entry.getValue();
            if(p.getUserId()==player.getUserId()){
                if(p.getPiaoFen()==-1){
                    player.writeComMessage(WebSocketMsgType.res_code_piaoFen);
                    continue;
                }
            }else {
                List<Integer> l=new ArrayList<>();
                l.add((int)p.getUserId());
                l.add(p.getPiaoFen());
                player.writeComMessage(WebSocketMsgType.res_code_broadcast_piaoFen, l);
            }
        }
    }

    @Override
    public boolean isAllReady() {
        if (getPlayerCount() < getMaxPlayerCount()) {
            return false;
        }
        for (Player player : getSeatMap().values()) {
            if(piaoFenType>0){
                if (!(player.getState() == player_state.ready||player.getState() == player_state.play))
                    return false;
            }else {
                if(player.getState() != player_state.ready)
                    return false;
            }
        }

        if(finishFapai==1)
            return false;
        changeTableState(table_state.play);
        if (piaoFenType == 2) {
            for (TcdpMjPlayer player : playerMap.values()) {
                player.setPiaoFen(dingPiao);
                for (TcdpMjPlayer p : playerMap.values()) {
                    p.writeComMessage(WebSocketMsgType.res_code_broadcast_piaoFen, (int)player.getUserId(),player.getPiaoFen());
                }
            }
        }else if (piaoFenType>0) {
            boolean piaoFenOver = true;
            for (TcdpMjPlayer player : playerMap.values()) {
                if(player.getPiaoFen()==-1){
                    piaoFenOver = false;
                    break;
                }
            }
            if(!piaoFenOver){
                if (finishFapai==0) {
                    LogUtil.msgLog.info("tcdpmj|sendPiaoFen|" + getId() + "|" + getPlayBureau());
                    ComRes msg = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_piaoFen).build();
                    for (TcdpMjPlayer player : playerMap.values()) {
                        if(player.getPiaoFen()==-1)
                            player.writeSocket(msg);
                    }
                }
                return false;
            }
        }
        return true;
    }


    @Override
    public boolean consumeCards() {
        return SharedConstants.consumecards;
    }

    @Override
    public void checkAutoPlay() {
        if (getSendDissTime() > 0) {
            for (TcdpMjPlayer player : seatMap.values()) {
                if (player.getLastCheckTime() > 0) {
                    player.setLastCheckTime(player.getLastCheckTime() + 1 * 1000);
                }
            }
            return;
        }

        if (getSendDissTime() > 0) {
            for (TcdpMjPlayer player : seatMap.values()) {
                if (player.getLastCheckTime() > 0) {
                    player.setLastCheckTime(player.getLastCheckTime() + 1 * 1000);
                }
            }
            return;
        }

        if (isAutoPlay < 1) {
            return;
        }

        if (state == table_state.play) {
            autoPlay();
        } else {
            if (getPlayedBureau() == 0) {
                return;
            }
            readyTime ++;

			// 开了托管的房间，xx秒后自动开始下一局
            for (TcdpMjPlayer player : seatMap.values()) {
                if (player.getState() != player_state.entry && player.getState() != player_state.over) {
                    continue;
                } else {
                    if (readyTime >= 5 && player.isAutoPlay()) {
						// 玩家进入托管后，5秒自动准备
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
                // 有胡处理胡
                for (int seat : huSeatList) {
                    TcdpMjPlayer player = seatMap.get(seat);
                    if (player == null) {
                        continue;
                    }
                    if (!player.checkAutoPlay(2, false)) {
                        continue;
                    }
                    playCommand(player, new ArrayList<>(), MjDisAction.action_hu);
                }
                return;
            } else {
                int action = 0, seat = 0;
                for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
                    seat = entry.getKey();
                    List<Integer> actList = MjDisAction.parseToDisActionList(entry.getValue());
                    if (actList == null) {
                        continue;
                    }

                    action = MjDisAction.getAutoMaxPriorityAction(actList);
                    TcdpMjPlayer player = seatMap.get(seat);
                    if (!player.checkAutoPlay(0, false)) {
                        continue;
                    }
                    boolean chuPai = false;
                    if (player.isAlreadyMoMajiang()) {
                        chuPai = true;
                    }
                    if (action == MjDisAction.action_peng) {
                        if (player.isAutoPlaySelf()) {
                            // 自己开启托管直接过
                            playCommand(player, new ArrayList<>(), MjDisAction.action_pass);
                            if (chuPai) {
                                autoChuPai(player);
                            }
                        } else {
                            if (nowDisCardIds != null && !nowDisCardIds.isEmpty()) {
                                TcdpMj mj = nowDisCardIds.get(0);
                                List<TcdpMj> mjList = new ArrayList<>();
                                for (TcdpMj handMj : player.getHandMajiang()) {
                                    if (handMj.getVal() == mj.getVal()) {
                                        mjList.add(handMj);
                                        if (mjList.size() == 2) {
                                            break;
                                        }
                                    }
                                }
                                playCommand(player, mjList, MjDisAction.action_peng);
                            }
                        }
                    } else {
                        playCommand(player, new ArrayList<>(), MjDisAction.action_pass);
                        if (chuPai) {
                            autoChuPai(player);
                        }
                    }
                }
            }
        } else {
            boolean finishPiaoFen=true;
            for(TcdpMjPlayer player:seatMap.values()){
                if(player.getPiaoFen()==-1){
                    finishPiaoFen=false;
                    break;
                }
            }
            if(piaoFenType>0&&!finishPiaoFen){
                for(TcdpMjPlayer player:seatMap.values()){
                    if(player.getPiaoFen()==-1){
                        if (player == null || !player.checkAutoPiaoFen()) {
                            continue;
                        }
                        piaoFen(player,0);
                    }
                }
            }else {
                TcdpMjPlayer player= seatMap.get(nowDisCardSeat);
                if (player == null || !player.checkAutoPlay(0, false)) {
                    return;
                }
                autoChuPai(player);
            }
        }
    }

    public void autoChuPai(TcdpMjPlayer player) {
        if (!player.isAlreadyMoMajiang()) {
            return;
        }
        List<Integer> handMjIds = new ArrayList<>(player.getHandPais());
        int mjId = -1;
        if (moMajiangSeat == player.getSeat()) {
            mjId = getMjIdNoWang(handMjIds,1);
        } else {
            Collections.sort(handMjIds);
            mjId = getMjIdNoWang(handMjIds,1);
        }
        if (mjId != -1) {
            List<TcdpMj> mjList = MjHelper.toMajiang(Arrays.asList(mjId));
            playCommand(player, mjList, MjDisAction.action_chupai);
        }
    }

    private int getMjIdNoWang(List<Integer> handMjIds,int num){
        int mjId = handMjIds.get(handMjIds.size() - num);
        return mjId;
    }


    @Override
    public void createTable(Player player, int play, int bureauCount, Object... objects) throws Exception {
    }


    public synchronized void piaoFen(TcdpMjPlayer player, int fen){
        if (piaoFenType==0||player.getPiaoFen()!=-1)
            return;
        if(fen>4||fen<0)
            return;
        player.setPiaoFen(fen);
        StringBuilder sb = new StringBuilder("tcdpmj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append("piaoFen").append("|").append(fen);
        LogUtil.msgLog.info(sb.toString());
        int confirmTime=0;
        for (Map.Entry<Integer, TcdpMjPlayer> entry : seatMap.entrySet()) {
            entry.getValue().writeComMessage(WebSocketMsgType.res_code_broadcast_piaoFen, (int)player.getUserId(),player.getPiaoFen());
            if(entry.getValue().getPiaoFen()!=-1)
                confirmTime++;
        }
        if (confirmTime == maxPlayerCount) {
            checkDeal(player.getUserId());
        }
    }



	// 是否两人麻将
    public boolean isTwoPlayer() {
        return getMaxPlayerCount() == 2;
    }

    public static final List<Integer> wanfaList = Arrays.asList(GameUtil.game_type_tcdpmj);

    public static void loadWanfaTables(Class<? extends BaseTable> cls) {
        for (Integer integer : wanfaList) {
            TableManager.wanfaTableTypesPut(integer, cls);
        }
    }

    public void logFaPaiTable(TcdpMj king) {
        StringBuilder sb = new StringBuilder();
        sb.append("tcdpmj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append("faPai");
        sb.append("|").append(playType);
        sb.append("|").append(maxPlayerCount);
        sb.append("|").append(getPayType());
        sb.append("|").append(lastWinSeat);
        sb.append("|").append(king);
        LogUtil.msg(sb.toString());
    }

    public void logFaPaiPlayer(TcdpMjPlayer player, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("tcdpmj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append("faPai");
        sb.append("|").append(player.getHandMajiang());
        sb.append("|").append(actListToString(actList));
        LogUtil.msg(sb.toString());
    }

    public void logAction(TcdpMjPlayer player, int action, List<TcdpMj> mjs, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("tcdpmj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        String actStr = "unKnown-" + action;
        if (action == MjDisAction.action_peng) {
            actStr = "peng";
        } else if (action == MjDisAction.action_minggang) {
            actStr = "minggang";
        } else if (action == MjDisAction.action_chupai) {
            actStr = "chuPai";
        } else if (action == MjDisAction.action_pass) {
            actStr = "guo";
        } else if (action == MjDisAction.action_angang) {
            actStr = "anGang";
        } else if (action == MjDisAction.action_chi) {
            actStr = "chi";
        }
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append(player.isAutoPlaySelf() ? 1 : 0);
        sb.append("|").append(actStr);
        sb.append("|").append(mjs);
        sb.append("|").append(actListToString(actList));
        LogUtil.msg(sb.toString());
    }

    public void logMoMj(TcdpMjPlayer player, TcdpMj mj, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("tcdpmj");
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
        sb.append("|").append(player.getHandMajiang());
        LogUtil.msg(sb.toString());
    }

    public void logMoMj(TcdpMjPlayer player, TcdpMj mj) {
        StringBuilder sb = new StringBuilder();
        sb.append("tcdpmj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append("fengDongMoPai");
        sb.append("|").append(leftMajiangs.size());
        sb.append("|").append(mj);
        LogUtil.msg(sb.toString());
    }

    public void logChuPaiActList(TcdpMjPlayer player, TcdpMj mj, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("tcdpmj");
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

    public void logActionHu(TcdpMjPlayer player, List<TcdpMj> mjs, String daHuNames) {
        StringBuilder sb = new StringBuilder();
        sb.append("tcdpmj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append("huPai");
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append(player.isAutoPlaySelf() ? 1 : 0);
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
                if (i == TcdpMjConstants.ACTION_INDEX_HU) {
                    sb.append("hu");
                } else if (i == TcdpMjConstants.ACTION_INDEX_PENG) {
                    sb.append("peng");
                } else if (i == TcdpMjConstants.ACTION_INDEX_MINGGANG) {
                    sb.append("mingGang");
                } else if (i == TcdpMjConstants.ACTION_INDEX_ANGANG) {
                    sb.append("anGang");
                } else if (i == TcdpMjConstants.ACTION_INDEX_CHI) {
                    sb.append("chi");
                } else if (i == TcdpMjConstants.ACTION_INDEX_BAOTING) {
                    sb.append("baoTing");
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


    public void sendTingInfo(TcdpMjPlayer player) {
        if (player.isAlreadyMoMajiang()) {
            if (actionSeatMap.containsKey(player.getSeat())) {
                return;
            }
            DaPaiTingPaiRes.Builder tingInfo = DaPaiTingPaiRes.newBuilder();
            List<TcdpMj> cards = new ArrayList<>(player.getHandMajiang());
            Map<Integer, List<Integer>> daTing =
                    TingTool.getDaTing(MjHelper.toMajiangIds(cards),buVal);
            for(Map.Entry<Integer, List<Integer>> entry : daTing.entrySet()){
                DaPaiTingPaiInfo.Builder ting = DaPaiTingPaiInfo.newBuilder();
                ting.addAllTingMajiangIds(entry.getValue());
                ting.setMajiangId(entry.getKey());
                tingInfo.addInfo(ting);
            }
            if(daTing.size()>0){
                player.writeSocket(tingInfo.build());
            }
        } else {
            List<TcdpMj> cards = new ArrayList<>(player.getHandMajiang());
            TingPaiRes.Builder ting = TingPaiRes.newBuilder();
            List<Integer> tingP = TingTool.getTing(MjHelper.toMajiangIds(cards),buVal);
            ting.addAllMajiangIds(tingP);
            if(tingP.size()>0){
                player.writeSocket(ting.build());
            }
        }
    }

    public String getTableMsg() {
        Map<String, Object> json = new HashMap<>();
		json.put("wanFa", "桐城点炮麻将");
        if (isGroupRoom()) {
            json.put("roomName", getRoomName());
        }
        json.put("playerCount", getPlayerCount());
        json.put("count", getTotalBureau());
        if (isAutoPlay > 0) {
            json.put("autoTime", isAutoPlay);
            if (autoPlayGlob == 1) {
				json.put("autoName", "单局");
            } else {
				json.put("autoName", "整局");
            }
        }
        return JSON.toJSONString(json);
    }

    @Override
    public String getGameName() {
		return "桐城点炮麻将";
    }
}
