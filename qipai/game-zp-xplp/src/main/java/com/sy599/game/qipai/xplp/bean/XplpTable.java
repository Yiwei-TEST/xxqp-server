package com.sy599.game.qipai.xplp.bean;

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

import org.apache.commons.lang3.ArrayUtils;
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
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.ComMsg.ComRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.DaPaiTingPaiInfo;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.DaPaiTingPaiRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.MoMajiangRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.PlayMajiangRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.TingPaiRes;
import com.sy599.game.msg.serverPacket.TableMjResMsg.ClosingMjInfoRes;
import com.sy599.game.msg.serverPacket.TableMjResMsg.ClosingMjPlayerInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.CreateTableRes;
import com.sy599.game.msg.serverPacket.TableRes.DealInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes;
import com.sy599.game.qipai.xplp.constant.XplpConstants;
import com.sy599.game.qipai.xplp.rule.XpLp;
import com.sy599.game.qipai.xplp.rule.XplpRobotAI;
import com.sy599.game.qipai.xplp.tool.XplpHelper;
import com.sy599.game.qipai.xplp.tool.XplpQipaiTool;
import com.sy599.game.qipai.xplp.tool.XplpResTool;
import com.sy599.game.qipai.xplp.tool.XplpTool;
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


public class XplpTable extends BaseTable {
    /**
	 * 当前打出的牌
	 */
    private List<XpLp> nowDisCardIds = new ArrayList<>();
    private Map<Integer, List<Integer>> actionSeatMap = new ConcurrentHashMap<>();
    /**
	 * 玩家位置对应临时操作 当同时存在多个可做的操作时 1当优先级最高的玩家最先操作 则清理所有操作提示 并执行该操作
	 * 2当操作优先级低的玩家先选择操作，则玩家先将所做操作存入临时操作中 当玩家优先级最高的玩家操作后 在判断临时操作是否可执行并执行
	 */
    private Map<Integer, XplpTempAction> tempActionMap = new ConcurrentHashMap<>();
    private int maxPlayerCount = 4;
    private List<XpLp> leftMajiangs = new ArrayList<>();
	/*** 玩家map */
    private Map<Long, XplpPlayer> playerMap = new ConcurrentHashMap<Long, XplpPlayer>();
	/*** 座位对应的玩家 */
    private Map<Integer, XplpPlayer> seatMap = new ConcurrentHashMap<Integer, XplpPlayer>();
	private List<Integer> huConfirmList = new ArrayList<>();// 胡牌数组
    /**
	 * 摸麻将的seat
	 */
    private int moMajiangSeat;
    /**
	 * 摸杠的麻将
	 */
    private XpLp moGang;
    /**
	 * 骰子点数
	 **/
    private int dealDice;
    /**
	 * 0点炮胡 1自摸胡
	 **/
    private int canDianPao;
    /**
	 * 选了庄闲就算分，不选就不额外算分
	 **/
    private int isCalcBanker;
    /**
	 * 胡7对
	 **/
    private int hu7dui;

	private int isAutoPlay = 0;// 是否开启自动托管
    
    private int readyTime = 0 ;
    /**
	 * 有炮必胡
	 **/
    private int youPaoBiHu;
    /**
	 * 底分
	 **/
    private int diFen;
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
	// 飘分 0:不飘 1:每局飘1 2:每局飘2 3:飘3 4:飘4 5:自由飘分 
    private int piaoFenType=0;
	// 是否已发送飘分信息
    private int isSendPiaoFenMsg=0;
    
	/** 托管1：单局，2：全局 */
    private int autoPlayGlob;
	private int autoTableCount;
	/*** 摸屁股的座标号 */
    private List<Integer> moTailPai = new ArrayList<>();
    //低于below加分
    private int belowAdd=0;
    private int below=0;

    List<Integer> paoHuSeat=new ArrayList<>();

	private int guchou;//箍臭
	private int cpNocp;//吃碰后不出同张
	private int nohua;//不带花
	private int choupaiNum;//抽牌数量
	
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
		isCalcBanker = StringUtil.getIntValue(params, 3, 0);// 庄闲算分
		guchou = StringUtil.getIntValue(params, 4, 0);// 箍臭
		cpNocp = StringUtil.getIntValue(params, 5, 0);// 吃碰后不出同张
		nohua = StringUtil.getIntValue(params, 6, 0);// 不带花
		maxPlayerCount = StringUtil.getIntValue(params, 7, 4);// 人数
		youPaoBiHu = StringUtil.getIntValue(params, 8, 0);// 有炮必胡
        
        
		isAutoPlay = StringUtil.getIntValue(params, 9, 0);// 托管
        if(isAutoPlay==1) {
			// 默认1分钟
        	isAutoPlay=60;
        }
        
		choupaiNum = StringUtil.getIntValue(params, 11, 0);// 抽牌
		diFen = 1;// 底分
        if(maxPlayerCount==2){
            jiaBei = StringUtil.getIntValue(params, 12, 0);
            jiaBeiFen = StringUtil.getIntValue(params, 13, 100);
            jiaBeiShu = StringUtil.getIntValue(params, 14, 1);
        }
		canDianPao = StringUtil.getIntValue(params, 15, 0);// 0:是否可点炮胡
		// 是否首局庄家随机
        bankerRand=StringUtil.getIntValue(params, 16, 0);
        if(bankerRand==1){
            setLastWinSeat(new Random().nextInt(maxPlayerCount) + 1);
        }
        autoPlayGlob=StringUtil.getIntValue(params, 19, 0);
        piaoFenType=StringUtil.getIntValue(params, 20, 0);
        if(maxPlayerCount==2){
            int belowAdd = StringUtil.getIntValue(params, 21, 0);
            if(belowAdd<=100&&belowAdd>=0)
                this.belowAdd=belowAdd;
            int below = StringUtil.getIntValue(params, 22, 0);
            if(below<=100&&below>=0){
                this.below=below;
                if(belowAdd>0&&below==0)
                    this.below=10;
            }
        }

        changeExtend();
        if (!isJoinPlayerAllotSeat()) {
			// getRoomModeMap().put("1", "1"); //可观战（默认）
        }
    }

    @Override
    public JsonWrapper buildExtend0(JsonWrapper wrapper) {
        for (XplpPlayer player : seatMap.values()) {
            wrapper.putString(player.getSeat(), player.toExtendStr());
        }
        wrapper.putString(5, StringUtil.implode(huConfirmList, ","));
        wrapper.putInt(7, moMajiangSeat);
        if (moGang != null) {
            wrapper.putInt(8, moGang.getId());
        } else {
            wrapper.putInt(8, 0);
        }
        wrapper.putInt(10, canDianPao);
        wrapper.putInt(11, isCalcBanker);
        wrapper.putInt(12, hu7dui);

        JSONArray tempJsonArray = new JSONArray();
        for (int seat : tempActionMap.keySet()) {
            tempJsonArray.add(tempActionMap.get(seat).buildData());
        }
        wrapper.putString("tempActions", tempJsonArray.toString());
        wrapper.putInt(13, maxPlayerCount);
        wrapper.putInt(14, dealDice);
        wrapper.putInt(15, isAutoPlay);
        wrapper.putInt(16, guchou);
        wrapper.putInt(17, cpNocp);
        wrapper.putInt(18, nohua);
        wrapper.putInt(19, choupaiNum);
        wrapper.putInt(20, youPaoBiHu);
        wrapper.putString(24, StringUtil.implode(moTailPai, ","));
        wrapper.putInt(25, diFen);
        wrapper.putInt(26, jiaBei);
        wrapper.putInt(27, jiaBeiFen);
        wrapper.putInt(28, jiaBeiShu);
        wrapper.putInt(29, bankerRand);
        wrapper.putString(32,StringUtil.implode(paoHuSeat, ","));
        wrapper.putInt(33, autoPlayGlob);
        wrapper.putInt(34, piaoFenType);
        wrapper.putInt(35, finishFapai);
        wrapper.putInt(36, isSendPiaoFenMsg);
        wrapper.putInt(37, belowAdd);
        wrapper.putInt(38, below);
        
        
        return wrapper;
    }

    @Override
    public void initExtend0(JsonWrapper wrapper) {
        for (XplpPlayer player : seatMap.values()) {
            player.initExtend(wrapper.getString(player.getSeat()));
        }
        String huListstr = wrapper.getString(5);
        if (!StringUtils.isBlank(huListstr)) {
            huConfirmList = StringUtil.explodeToIntList(huListstr);
        }
        moMajiangSeat = wrapper.getInt(7, 0);
        int moGangMajiangId = wrapper.getInt(8, 0);
        if (moGangMajiangId != 0) {
            moGang = XpLp.getMajang(moGangMajiangId);
        }
        String moGangHu = wrapper.getString(9);
        canDianPao = wrapper.getInt(10, 1);
        isCalcBanker = wrapper.getInt(11, 0);
        hu7dui = wrapper.getInt(12, 0);
        tempActionMap = loadTempActionMap(wrapper.getString("tempActions"));
        maxPlayerCount = wrapper.getInt(13, 4);
        dealDice = wrapper.getInt(14, 0);
        isAutoPlay = wrapper.getInt(15, 0);

        if(isAutoPlay ==1) {
            isAutoPlay=60;
        }
        guchou = wrapper.getInt(16, 0);
        cpNocp = wrapper.getInt(17, 0);
        nohua = wrapper.getInt(18, 0);
        choupaiNum = wrapper.getInt(19, 0);
        youPaoBiHu = wrapper.getInt(20, 0);
        String s = wrapper.getString(24);
        if (!StringUtils.isBlank(s)) {
            moTailPai = StringUtil.explodeToIntList(s);
        }
        diFen = wrapper.getInt(25,1);
        jiaBei = wrapper.getInt(26,0);
        jiaBeiFen = wrapper.getInt(27,0);
        jiaBeiShu = wrapper.getInt(28,0);
        bankerRand = wrapper.getInt(29,0);
        s = wrapper.getString(32);
        if (!StringUtils.isBlank(s)) {
            paoHuSeat = StringUtil.explodeToIntList(s);
        }
        autoPlayGlob= wrapper.getInt(33,0);
        piaoFenType= wrapper.getInt(34,0);
        finishFapai= wrapper.getInt(35,0);
        isSendPiaoFenMsg= wrapper.getInt(36,0);
        belowAdd= wrapper.getInt(37,0);
        below= wrapper.getInt(38,0);
    }



    public int getDealDice() {
        return dealDice;
    }

    public int getIsAutoPlay() {
        return isAutoPlay;
    }

    public void setIsAutoPlay(int isAutoPlay) {
        this.isAutoPlay = isAutoPlay;
    }

    public void setDealDice(int dealDice) {
        this.dealDice = dealDice;
    }

    public boolean isHu7dui() {
        return false;
    }


    public int getCanDianPao() {
        return canDianPao;
    }

    public void setCanDianPao(int canDianPao) {
        this.canDianPao = canDianPao;
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

    /**
	 * 计算庄闲
	 *
	 * @return
	 */
    public boolean isCalBanker() {
        return 1 == isCalcBanker;
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
        if (winList.size() == 0 && leftMajiangs.isEmpty()) {

        } else {
			// 先判断是自摸还是放炮
            if (winList.size() == 1 && seatMap.get(winList.get(0)).getHandMajiang().size() % 3 == 2 && winList.get(0) == moMajiangSeat) {
                selfMo = true;
            }
            XpLp huCards=null;
            if(selfMo){
                List<XpLp> handMajiang = seatMap.get(winList.get(0)).getHandMajiang();
                if(handMajiang!=null&&!handMajiang.isEmpty())
                    huCards=handMajiang.get(handMajiang.size()-1);
            }else {
                if(nowDisCardIds!=null){
                    if(nowDisCardIds.size()==1){
                        huCards=nowDisCardIds.get(0);
                    } else {
                        huCards=null;
                    }
                }
            }
            if (selfMo) {
				// 自摸
                int zhuangxian=0;
                XplpPlayer winner = seatMap.get(winList.get(0));
                for (int seat : seatMap.keySet()) {
                    if (!winList.contains(seat)) {
                        XplpPlayer player = seatMap.get(seat);
                        player.changePointArr(0,-1*diFen);
						// 分庄闲多算一个底分
                        if (isCalBanker() && (winList.get(0) == lastWinSeat||seat==lastWinSeat)) {
                            zhuangxian+=diFen;
                            player.changePointArr(3,-diFen);
                        }
                        setWinLostPiaoFen(winner,player);
                    }
                }
                for (int seat : winList) {
                    XplpPlayer player = seatMap.get(seat);
                    player.changePointArr(0,1*diFen*(seatMap.size()-1));
                    player.changePointArr(3,zhuangxian);
                    player.changeAction(XplpConstants.ACTION_COUNT_INDEX_ZIMO, 1);
                    player.addHuNum(1);
                }
            } else {
				// 小胡接炮 每人1分
				// 如果庄家输牌失分翻倍
                XplpPlayer losePlayer = seatMap.get(disCardSeat);
                losePlayer.addDianPaoNum(1);
                boolean qgh = false;
                for (int winnerSeat : winList) {
                    XplpPlayer winPlayer = seatMap.get(winnerSeat);
                    int winPoint = 1;
                    int payNum=1;
                    winPlayer.changePointArr(0,winPoint*diFen*payNum);
                    losePlayer.changePointArr(0,-winPoint*diFen*payNum);
                    winPlayer.changeAction(XplpConstants.ACTION_COUNT_INDEX_JIEPAO, 1);
                    losePlayer.changeAction(XplpConstants.ACTION_COUNT_INDEX_DIANPAO, 1);
                    winPlayer.addHuNum(1);
                    setWinLostPiaoFen(winPlayer,losePlayer);
                    if (qgh) {
                        int s=0;
                        if (isCalBanker() && winnerSeat==lastWinSeat) {
							// 分庄闲多算一分
                            winPlayer.changePointArr(3,diFen*payNum);
                            losePlayer.changePointArr(3,-diFen*payNum);
                        }else {
//                            s=diFen;
                            winPlayer.changePointArr(3,diFen);
                            losePlayer.changePointArr(3,-diFen);
                        }
                    } else {
                        if (isCalBanker() && (losePlayer.getSeat() == lastWinSeat||winnerSeat==lastWinSeat)) {
							// 分庄闲多算一个底分
                            winPlayer.changePointArr(3,diFen);
                            losePlayer.changePointArr(3,-diFen);
                        }
                    }
                }
            }
			// 非流局的情况下算飘分
            for(int seat:seatMap.keySet()){
                XplpPlayer player = seatMap.get(seat);
                player.setPoint(player.getPoint()+player.getWinLostPiaoFen());
                player.changeTotalPoint(player.getWinLostPiaoFen());
            }
        }
		// 不管流局都加分
        for (XplpPlayer player : seatMap.values()) {
            player.setLostPoint(player.sumPointArr());
            player.changePoint(player.getLostPoint());
        }
        
        
        boolean over = playBureau == totalBureau;
        if(autoPlayGlob >0) {
			// //是否解散
            boolean diss = false;
            if(autoPlayGlob ==1) {
            	 for (XplpPlayer seat : seatMap.values()) {
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

        ClosingMjInfoRes.Builder res = sendAccountsMsg(over, selfMo, winList, null, null, null, 0, false);
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
            calcCreditNew();
            diss();
        } else {
            initNext();
            calcOver1();
        }
        for (XplpPlayer player : seatMap.values()) {
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
		for (XplpPlayer seat : seatMap.values()) {
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

    public void setWinLostPiaoFen(XplpPlayer win,XplpPlayer lost) {
        if(piaoFenType>0){
            lost.setWinLostPiaoFen(lost.getWinLostPiaoFen()-win.getPiaoFen()-lost.getPiaoFen());
            win.setWinLostPiaoFen(win.getWinLostPiaoFen()+win.getPiaoFen()+lost.getPiaoFen());
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
		userLog.setType(creditMode == 1 ? 2 : 1 );
        userLog.setMaxPlayerCount(maxPlayerCount);
        userLog.setGeneralExt(buildGeneralExtForPlaylog().toString());
        long logId = TableLogDao.getInstance().save(userLog);
        saveTableRecord(logId, over, playBureau);
        for (XplpPlayer player : playerMap.values()) {
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
    private int[] zhuaNiao(boolean isQingHu, int birdNum) {
        int[] birdMjIds = null;
        if (birdNum == 10) {
            XpLp birdMj = getLeftMajiang();
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
                            XpLp mj = XpLp.getMajiangByValue(birdMjVals[i]);
                            if (mj != null) {
                                birdMjIds[i] = mj.getId();
                            }
                        }
                    }
                }
            }
        } else {
            birdNum = isQingHu ? birdNum + 1 : birdNum;
            if (birdNum > leftMajiangs.size()) {
                birdNum = leftMajiangs.size();
            }
			// 先砸鸟
            birdMjIds = new int[birdNum];
            for (int i = 0; i < birdNum; i++) {
                XpLp birdMj = getLeftMajiang();
                if (birdMj != null) {
                    birdMjIds[i] = birdMj.getId();
                }
            }
        }
        return birdMjIds;
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
                tempMap.put("nowDisCardIds", StringUtil.implode(XplpHelper.toMajiangIds(nowDisCardIds), ","));
            }
            if (tempMap.containsKey("leftPais")) {
                tempMap.put("leftPais", StringUtil.implode(XplpHelper.toMajiangIds(leftMajiangs), ","));
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
        int dealDice = 0;
        Random r = new Random();
        dealDice = (r.nextInt(6) + 1) * 10 + (r.nextInt(6) + 1);
        addPlayLog(disCardRound + "_" + lastWinSeat + "_" + XplpDisAction.action_dice + "_" + dealDice);
        setDealDice(dealDice);
		// 天胡或者暗杠
        logFaPaiTable();
        for (XplpPlayer tablePlayer : seatMap.values()) {
            DealInfoRes.Builder res = DealInfoRes.newBuilder();
            List<Integer> actionList = new ArrayList<>();
            if(guchou == 1){
               if (lastWinSeat == tablePlayer.getSeat()) {
//                   actionList = tablePlayer.checkMo(null);
               }else{
               	actionList =  Arrays.asList(0,0,0,0,0,0,1);
               }
            }else{
            	 if (lastWinSeat == tablePlayer.getSeat()) {
                   actionList = tablePlayer.checkMo(null);
               }
            }
           
            if (!actionList.isEmpty()) {
                addActionSeat(tablePlayer.getSeat(), actionList);
                res.addAllSelfAct(actionList);
                logFaPaiPlayer(tablePlayer, actionList);
            }
            res.addAllHandCardIds(tablePlayer.getHandPais());
            res.setNextSeat(getNextDisCardSeat());
            res.setGameType(getWanFa());
            res.setRemain(leftMajiangs.size());
            res.setBanker(lastWinSeat);
            res.setDealDice(dealDice);
//			if (userId == tablePlayer.getUserId()) {
//				continue;
//			}
            tablePlayer.writeSocket(res.build());
            if (tablePlayer.isAutoPlay()) {
                tablePlayer.setAutoPlayTime(0);
            }
            sendTingInfo(tablePlayer);
            logFaPaiPlayer(tablePlayer, null);
            if(tablePlayer.isAutoPlay()) {
            	addPlayLog(getDisCardRound() + "_" +tablePlayer.getSeat() + "_" + XplpConstants.action_tuoguan + "_" +1+ tablePlayer.getExtraPlayLog());
            }
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
        
//        isBegin = true;
    }

    public void moMajiang(XplpPlayer player, boolean isBuZhang) {
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
        XpLp majiang = null;
        if (disCardRound != 0) {
			// 玩家手上的牌是双数，已经摸过牌了
            if (player.isAlreadyMoMajiang()) {
                return;
            }
            if (getLeftMajiangCount() == 0) {
                calcOver();
                return;
            }
            if (GameServerConfig.isDebug() && zp != null && !zp.isEmpty()) {
                majiang=XpLp.getMajiangByValue(zp.get(0).get(0));
                zp.clear();
            }else {
                majiang = getLeftMajiang();
            }
        }
        if (majiang != null) {
            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + XplpDisAction.action_moMjiang + "_" + majiang.getId() + player.getExtraPlayLog());
            if(!player.isGuChou()){
            	player.moMajiang(majiang);
            }
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
        }
        logMoMj(player, majiang, arr);
        MoMajiangRes.Builder res = MoMajiangRes.newBuilder();
        res.setUserId(player.getUserId() + "");
        res.setSeat(player.getSeat());
        res.setRemain(leftMajiangs!=null?leftMajiangs.size():0);
        for (XplpPlayer seat : seatMap.values()) {
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
//        if(!arr.isEmpty() && arr.get(XplpConstants.ACTION_INDEX_ZIMO) == 1){//自摸直接胡
//        	hu(player, Arrays.asList(majiang), XplpDisAction.action_hu);
//        	return;
//        }
        
        if(player.isGuChou()){//箍臭了，直接下家摸牌
        	setNowDisCardSeat(calcNextSeat(player.getSeat()));
        	checkMo();
        }
    }

    /**
	 * 玩家表示胡
	 *
	 * @param player
	 * @param majiangs
	 */
    private void hu(XplpPlayer player, List<XpLp> majiangs, int action) {
        if (state != table_state.play) {
            return;
        }
        List<Integer> actionList = actionSeatMap.get(player.getSeat());
		if (actionList == null || (actionList.get(XplpConstants.ACTION_INDEX_HU) != 1
				&& actionList.get(XplpConstants.ACTION_INDEX_ZIMO) != 1)) {// 如果集合为空或者第一操作不为胡，则返回
            return;
        }
		if (!checkAction(player, majiangs, new ArrayList<Integer>(), action))
		 {// 检查优先度，胡杠碰吃 如果同时出现一个事件，按出牌座位顺序优先
			player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip);
			LogUtil.msg("有优先级更高的操作需等待！");
			return;
		 }//一炮多响去掉
        if (huConfirmList.contains(player.getSeat())) {
            return;
        }
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        List<XpLp> huHand = new ArrayList<>(player.getHandMajiang());
        boolean zimo = player.isAlreadyMoMajiang();
        XplpPlayer fangPaoPlayer;
        if (!zimo) {
        	// 放炮
            huHand.addAll(nowDisCardIds);
            builder.setFromSeat(disCardSeat);
            player.getHuType().add(XplpConstants.HU_JIPAO);
            fangPaoPlayer = seatMap.get(disCardSeat);
            fangPaoPlayer.getHuType().add(XplpConstants.HU_FANGPAO);
        } else {
            builder.addHuArray(XplpConstants.HU_ZIMO);
            player.getHuType().add(XplpConstants.HU_ZIMO);
        }
        if (!XplpTool.isPingHu(huHand)) {
            return;
        }
        buildPlayRes(builder, player, action, huHand);
        if (zimo) {
            builder.setZimo(1);
        }
        if (!huConfirmList.isEmpty()) {
            builder.addExt(StringUtil.implode(huConfirmList, ","));
        }
		// 胡
        for (XplpPlayer seat : seatMap.values()) {
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
        List<XpLp> huPai = new ArrayList<>();
        huPai.add(huHand.get(huHand.size() - 1));
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + XplpHelper.toMajiangStrs(huPai) + "_" + StringUtil.implode(player.getHuType(), ",") + player.getExtraPlayLog());
        if (isCalcOver()) {
            logActionHu(player, majiangs, "");
			// 等待别人胡牌 如果都确认完了，胡
            calcOver();
        } else {
            //removeActionSeat(player.getSeat());
            player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip, action);
        }
    }

    private void buildPlayRes(PlayMajiangRes.Builder builder, Player player, int action, List<XpLp> majiangs) {
        XplpResTool.buildPlayRes(builder, player, action, majiangs);
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
    private XplpPlayer getPlayerByHasMajiang(XpLp majiang) {
        for (XplpPlayer player : seatMap.values()) {
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
            XplpPlayer moGangPlayer = null;
            for (int huseat : huActionList) {
                if (moGangPlayer != null) {
					// 被抢杠的人可以胡的话 跳过
                    if (moGangPlayer.getSeat() == huseat) {
                        continue;
                    }
                }
                if (!huConfirmList.contains(huseat) &&
                        !(tempActionMap.containsKey(huseat) && tempActionMap.get(huseat).getAction() == XplpDisAction.action_hu)) {
                    over = false;
                    break;
                }
            }
        }

        if (!over) {
            XplpPlayer disMajiangPlayer = seatMap.get(disCardSeat);
            for (int huseat : huActionList) {
                if (huConfirmList.contains(huseat)) {
                    continue;
                }
                PlayMajiangRes.Builder disBuilder = PlayMajiangRes.newBuilder();
                XplpPlayer seatPlayer = seatMap.get(huseat);
                buildPlayRes(disBuilder, disMajiangPlayer, 0, null);
                List<Integer> actionList = actionSeatMap.get(huseat);
                disBuilder.addAllSelfAct(actionList);
                seatPlayer.writeSocket(disBuilder.build());
            }
        }

        for (XplpPlayer player : seatMap.values()) {
            if (player.isAlreadyMoMajiang() && !huConfirmList.contains(player.getSeat())) {
                over = false;
            }
        }

        return over;
    }

    /**
	 * 碰杠
	 *
	 * @param player
	 * @param majiangs
	 * @param action
	 */
    private void chiPengGang(XplpPlayer player, List<XpLp> majiangs, int action) {
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
        XpLp disMajiang = null;
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
            sameCount = XplpHelper.getMajiangCount(majiangs, majiangs.get(0).getVal());
        }
		// 如果是杠 后台来找出是明杠还是暗杠
        if (action == XplpDisAction.action_minggang) {
            majiangs = XplpHelper.getMajiangList(player.getHandMajiang(), majiangs.get(0).getVal());
            sameCount = majiangs.size();
            if (sameCount == 4) {
				// 有4张一样的牌是暗杠
                action = XplpDisAction.action_angang;
            }
			// 其他是明杠
        }
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return;
        }
        boolean hasQGangHu = false;
        if (action == XplpDisAction.action_peng) {
            boolean can = canPeng(player, majiangs, sameCount);
            if (!can) {
                return;
            }
        } else if (action == XplpDisAction.action_angang) {
            boolean can = canAnGang(player, majiangs, sameCount);

            if (!can) {
                return;
            }
            player.addAnGangNum(1);
            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + XplpHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
        } else if (action == XplpDisAction.action_minggang) {
            boolean can = canMingGang(player, majiangs, sameCount);
            if (!can) {
                return;
            }
            player.addGongGangNum(1);
            ArrayList<XpLp> mjs = new ArrayList<>(majiangs);
            if (sameCount == 3) {
                mjs.add(disMajiang);
            }
            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + XplpHelper.toMajiangStrs(mjs) + player.getExtraPlayLog());

        } else if (action == XplpDisAction.action_chi) {
			//boolean can = canChi(player, player.getHandMajiang(), majiangs, disMajiang);
//			if (!can) {
//				return;
//			}
		} else {
            return;
        }
        if (disMajiang != null) {
            if ((action == XplpDisAction.action_minggang && sameCount == 3)
                    || action == XplpDisAction.action_peng || action == XplpDisAction.action_chi) {
                if (action == XplpDisAction.action_chi) {
					majiangs.add(1, disMajiang);// 吃的牌放第二位
                } else {
                    majiangs.add(disMajiang);
                }
                builder.setFromSeat(disCardSeat);
                seatMap.get(disCardSeat).removeOutPais(nowDisCardIds, action);
            }
            if(cpNocp == 1 && (action == XplpDisAction.action_peng || action == XplpDisAction.action_chi)){
            	player.setBeforeCPCardVal(disMajiang.getVal());
            }
            
        }
        chiPengGang(builder, player, majiangs, action, hasQGangHu, sameCount);
    }

    
    
	/**
	 * 是否能碰
	 *
	 * @param player
	 * @param majiangs
	 * @param disMajiang
	 * @return
	 */
	private boolean canChi(XplpPlayer player, List<XpLp> handMajiang, List<XpLp> majiangs, XpLp disMajiang) {
		if (!actionSeatMap.containsKey(player.getSeat())) {
			return false;
		}

		if (player.isAlreadyMoMajiang()) {
			return false;
		}
//		List<Integer> pengGangSeatList = getPengGangSeatByActionMap();
//		pengGangSeatList.remove((Object) player.getSeat());
//		if (!pengGangSeatList.isEmpty()) {
//			return false;
//		}
		//
		// Majiang playCommand = null;
		// if (nowDisCardIds.size() == 1) {
		// playCommand = nowDisCardIds.get(0);
		//
		// } else {
		// for (int majiangId : gangSeatMap.keySet()) {
		// Map<Integer, List<Integer>> actionMap = gangSeatMap.get(majiangId);
		// List<Integer> action = actionMap.get(player.getSeat());
		// if (action != null) {
		// List<Integer> disActionList =
		// MajiangDisAction.parseToDisActionList(action);
		// if (disActionList.contains(MajiangDisAction.action_chi)) {
		// playCommand = Majiang.getMajang(majiangId);
		// break;
		// }
		//
		// }
		//
		// }
		//
		// }

		if (disMajiang == null) {
			return false;
		}

		if (!handMajiang.containsAll(majiangs)) {
			return false;
		}

		List<XpLp> chi = XplpTool.checkChi(majiangs, disMajiang);
		return !chi.isEmpty();
	}
    

    private void chiPengGang(PlayMajiangRes.Builder builder, XplpPlayer player, List<XpLp> majiangs, int action, boolean hasQGangHu, int sameCount) {
        List<Integer> actionList = actionSeatMap.get(player.getSeat());
        if (action == XplpDisAction.action_peng && actionList.get(XplpConstants.ACTION_INDEX_MINGGANG) == 1) {
			// 可以碰也可以杠
            player.addPassGangVal(majiangs.get(0).getVal());
        }

        player.addOutPais(majiangs, action, disCardSeat);
        buildPlayRes(builder, player, action, majiangs);
        List<Integer> actList = removeActionSeat(player.getSeat());
        if (!hasQGangHu) {
            clearActionSeatMap();
        }
        if (action == XplpDisAction.action_chi || action == XplpDisAction.action_peng) {
            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + XplpHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
        }
		// 不是普通出牌
        setNowDisCardSeat(player.getSeat());
        for (XplpPlayer seatPlayer : seatMap.values()) {
			// 推送消息
            PlayMajiangRes.Builder copy = builder.clone();
            if (actionSeatMap.containsKey(seatPlayer.getSeat())) {
                copy.addAllSelfAct(actionSeatMap.get(seatPlayer.getSeat()));
            }
            seatPlayer.writeSocket(copy.build());
        }
        if (action == XplpDisAction.action_chi || action == XplpDisAction.action_peng) {
            sendTingInfo(player);
        }
        for (Player roomPlayer : roomPlayerMap.values()) {
            PlayMajiangRes.Builder copy = builder.clone();
            roomPlayer.writeSocket(copy.build());
        }
        if (!hasQGangHu) {
            calcPoint(player, action, sameCount, majiangs);
        }
        if (!hasQGangHu && action == XplpDisAction.action_minggang || action == XplpDisAction.action_angang) {
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
    private void chuPai(XplpPlayer player, List<XpLp> majiangs, int action) {
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
            return;
        }
        if(player.getBeforeCPCardVal() > 0 && player.getBeforeCPCardVal() == majiangs.get(0).getVal()){
        	player.writeComMessage(WebSocketMsgType.res_code_mj_dis_err, action, majiangs.get(0).getId());
            return;
        }
		if (!actionSeatMap.isEmpty()) {// 出牌自动过掉手上操作
            guo(player, null, XplpDisAction.action_pass);
        }
        if (!actionSeatMap.isEmpty()) {
            player.writeComMessage(WebSocketMsgType.res_code_mj_dis_err, action, majiangs.get(0).getId());
            return;
        }
        player.setBeforeCPCardVal(0);
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        buildPlayRes(builder, player, action, majiangs);
		// 普通出牌
        clearActionSeatMap();
        setNowDisCardSeat(calcNextSeat(player.getSeat()));
        recordDisMajiang(majiangs, player);
        player.addOutPais(majiangs, action, player.getSeat());
        logAction(player, action, majiangs, null);
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + XplpHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
        List<XplpPlayer> l=new ArrayList<>();
        for (XplpPlayer p : seatMap.values()) {
            List<Integer> list;
            if (p.getUserId() != player.getUserId()) {
                list = p.checkDisMajiang(majiangs.get(0), this.canDianPao());
                if(list==null||list.isEmpty())
                    continue;
                if (list.contains(1)) {
                    if(youPaoBiHu==1&&list.get(0)==1){
                        int[] arr=new int[]{1,0,0,0,0,0};
                        list=Arrays.asList(ArrayUtils.toObject(arr));
                        paoHuSeat.add(p.getSeat());
                    }
                    addActionSeat(p.getSeat(), list);
                    p.setLastCheckTime(System.currentTimeMillis());
                    logChuPaiActList(p, majiangs.get(0), list);
                }

                if(youPaoBiHu==1&&list.get(0)==1){
                    paoHuSeat.add(p.getSeat());
//                    hu(p,majiangs,ZzMjDisAction.action_hu);
                }
            }
        }
//        for (int i = 0; i < l.size(); i++) {
//            hu(l.get(i),majiangs,ZzMjDisAction.action_hu);
//        }
        sendDisMajiangAction(builder, player);

		// 给下一家发牌
        checkMo();
    }

    public List<Integer> getHuSeatByActionMap() {
        List<Integer> huList = new ArrayList<>();
        for (int seat : actionSeatMap.keySet()) {
            List<Integer> actionList = actionSeatMap.get(seat);
            if (actionList.get(XplpConstants.ACTION_INDEX_HU) == 1 || actionList.get(XplpConstants.ACTION_INDEX_ZIMO) == 1) {
				// 胡
                huList.add(seat);
            }
        }
        return huList;
    }

    private void sendDisMajiangAction(PlayMajiangRes.Builder builder, XplpPlayer player) {
        for (XplpPlayer seatPlayer : seatMap.values()) {
            PlayMajiangRes.Builder copy = builder.clone();
            List<Integer> actionList;
			// 只推送给胡牌的人改成了推送给所有人但是必须等胡牌的人先答复
            if (actionSeatMap.containsKey(seatPlayer.getSeat())) {
                actionList = actionSeatMap.get(seatPlayer.getSeat());
            } else {
                actionList = new ArrayList<>();
            }
            copy.addAllSelfAct(actionList);
            if (seatPlayer.getSeat() == player.getSeat()) {
                copy.addExt(XplpTool.isTing(seatPlayer.getHandMajiang()) ? "1" : "0");
            }
            seatPlayer.writeSocket(copy.build());
        }
        for (Player roomPlayer : roomPlayerMap.values()) {
            PlayMajiangRes.Builder copy = builder.clone();
            roomPlayer.writeSocket(copy.build());
        }
    }

    public synchronized void playCommand(XplpPlayer player, List<XpLp> majiangs, int action) {
        playCommand(player, majiangs, null, action);
    }

    /**
	 * 出牌
	 *
	 * @param player
	 * @param majiangs
	 * @param action
	 */
    public synchronized void playCommand(XplpPlayer player, List<XpLp> majiangs, List<Integer> hucards, int action) {
        if (state != table_state.play) {
            return;
        }

        if (XplpDisAction.action_hu == action) {
            hu(player, majiangs, action);
            return;
        }
		// 手上没有要出的麻将
        if (action != XplpDisAction.action_minggang)
            if (!player.getHandMajiang().containsAll(majiangs)) {
                return;
            }
        changeDisCardRound(1);
        if (action == XplpDisAction.action_pass) {
            guo(player, majiangs, action);
        }else if(action == XplpDisAction.action_guchou){
        	guchou(player, action);
        } else if (action != 0) {
            chiPengGang(player, majiangs, action);
        } else {
            chuPai(player, majiangs, action);
        }
		// 记录最后一次动作的时间
        setLastActionTime(TimeUtil.currentTimeMillis());
    }

    private void passMoHu(XplpPlayer player, List<XpLp> majiangs, int action) {

        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        buildPlayRes(builder, player, action, majiangs);
        builder.setSeat(nowDisCardSeat);
        removeActionSeat(player.getSeat());
        player.writeSocket(builder.build());
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + XplpHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
        if (isCalcOver()) {
            calcOver();
            return;
        }
        player.setPassMajiangVal(nowDisCardIds.get(0).getVal());

        //ZzMjPlayer moGangPlayer = seatMap.get(moMajiangSeat);

        XplpPlayer moGangPlayer = seatMap.get(getNowDisCardSeat());
        majiangs = new ArrayList<>();
        majiangs.add(moGang);
        if (moGangPlayer.getaGang().contains(moGang)) {
            calcPoint(moGangPlayer, XplpDisAction.action_angang, 4, majiangs);
        } else {
            calcPoint(moGangPlayer, XplpDisAction.action_minggang, 1, majiangs);
        }

        moMajiang(moGangPlayer, true);
//		builder = PlayMajiangRes.newBuilder();
//		chiPengGang(builder, moGangPlayer, majiangs, ZzMjDisAction.action_minggang, false);
    

    }

    /**
     * pass
     *
     * @param player
     * @param majiangs
     * @param action
     */
    private void guo(XplpPlayer player, List<XpLp> majiangs, int action) {
        if (state != table_state.play) {
            return;
        }
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return;
        }
        if(paoHuSeat.contains(player.getSeat())){
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_911));
            return;
        }
        //限制过频率
        if (player.getLastPassTime() > 0 && System.currentTimeMillis() - player.getLastPassTime() <= 500) {
            logPassTime(player);
            return;
        }
        //过频率
        player.setLastPassTime(System.currentTimeMillis());
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        buildPlayRes(builder, player, action, majiangs);
        builder.setSeat(nowDisCardSeat);
        List<Integer> removeActionList = removeActionSeat(player.getSeat());
        player.writeSocket(builder.build());
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + XplpHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
        if (isCalcOver()) {
            calcOver();
            return;
        }
        if (removeActionList.get(0) == 1 && disCardSeat != player.getSeat() && nowDisCardIds.size() == 1) {
			// 漏炮
            player.setPassMajiangVal(nowDisCardIds.get(0).getVal());
        }
        if(removeActionList.get(1) == 1&& disCardSeat != player.getSeat() && nowDisCardIds.size() == 1){
        	//过碰
        	player.setPassPengMajiangVal(nowDisCardIds.get(0).getVal());
        }
        logAction(player, action, majiangs, removeActionList);
        if (!actionSeatMap.isEmpty()) {
            XplpPlayer disMajiangPlayer = seatMap.get(disCardSeat);
            PlayMajiangRes.Builder disBuilder = PlayMajiangRes.newBuilder();
            buildPlayRes(disBuilder, disMajiangPlayer, 0, null);
            for (int seat : actionSeatMap.keySet()) {
                List<Integer> actionList = actionSeatMap.get(seat);
                PlayMajiangRes.Builder copy = disBuilder.clone();
                copy.addAllSelfAct(new ArrayList<>());
                if (actionList != null && !tempActionMap.containsKey(seat) && !huConfirmList.contains(seat)) {
                    copy.addAllSelfAct(actionList);
                    XplpPlayer seatPlayer = seatMap.get(seat);
                    seatPlayer.writeSocket(copy.build());
                }
            }
        }else{
        	if (removeActionList.get(6) == 1 ) {
    			// 过完箍臭.检查庄家是不是有操作
        		XplpPlayer winPlayer = seatMap.get(lastWinSeat);
        		List<Integer> winActionList = winPlayer.checkMo(null);
        		 if(winActionList != null && !winActionList.isEmpty()){
                     
        			 for (XplpPlayer seatPlayer : seatMap.values()) {
        	   			   // 推送消息
        	               PlayMajiangRes.Builder copy = builder.clone();
        	               if (seatPlayer.getSeat() == lastWinSeat && winActionList != null && !winActionList.isEmpty()) {
        	            	   addActionSeat(lastWinSeat, winActionList);
        	            	   copy.addAllSelfAct(winActionList);
        	               }
        	               seatPlayer.writeSocket(copy.build());
        	           }
                 }else{
              	   //通知庄家出牌
                   XplpPlayer bankPlayer = seatMap.get(lastWinSeat);
          		   ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.req_com_kt_ask_dismajiang);// 推送庄家出牌
          		   bankPlayer.writeSocket(com.build());
                 }
            }
        }
        if (player.isAlreadyMoMajiang()) {
            sendTingInfo(player);
        }
		refreshTempAction(player);// 先过 后执行临时可做操作里面优先级最高的玩家操作
        checkMo();
       
    }

    /**
   	 * 箍臭
   	 * @param player
   	 * @param action
   	 */
       private void guchou(XplpPlayer player, int action) {
           if (state != table_state.play) {
               return;
           }
           logAction(player, action, null, null);
           List<Integer> actionList = actionSeatMap.get(player.getSeat());
           if (actionList==null || action != XplpDisAction.action_guchou || actionList.get(XplpConstants.ACTION_INDEX_GUCHOU) != 1) {
        	   return;
           }
           PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
           if (nowDisCardIds.size() > 1) {
   			// 当前出牌不能操作
               return;
           }
           List<Integer> winActionList = new ArrayList<>();
           for (XplpPlayer tablePlayer : seatMap.values()) {
        	   if(tablePlayer.getSeat() != player.getSeat()){
        		   if(actionSeatMap.get(tablePlayer.getSeat())!= null && !actionSeatMap.get(tablePlayer.getSeat()).isEmpty()){
        			   actionSeatMap.get(tablePlayer.getSeat()).set(XplpConstants.ACTION_INDEX_GUCHOU, 0);
        		   }
        	   }
        	   
               if (lastWinSeat == tablePlayer.getSeat()) {
                   winActionList = tablePlayer.checkMo(null);
               }
           }
           player.setGuChou(true);
           buildPlayRes(builder, player, action, null);
           clearActionSeatMap();
           
           addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + "" + player.getExtraPlayLog());
           
           for (XplpPlayer seatPlayer : seatMap.values()) {
   			   // 推送消息
               PlayMajiangRes.Builder copy = builder.clone();
               if (seatPlayer.getSeat() == lastWinSeat && winActionList != null && !winActionList.isEmpty()) {
            	   addActionSeat(lastWinSeat, winActionList);
            	   copy.addAllSelfAct(winActionList);
               }
               seatPlayer.writeSocket(copy.build());
           }
           if(winActionList != null && !winActionList.isEmpty()){
//        	   if (!actionList.isEmpty()) {
//                   addActionSeat(lastWinSeat, actionList);
//               }
           }else{
        	   //通知庄家出牌
               XplpPlayer bankPlayer = seatMap.get(lastWinSeat);
    		   ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.req_com_kt_ask_dismajiang);// 推送庄家出牌
    		   bankPlayer.writeSocket(com.build());
           }
           
          
       }

    
    private void logPassTime(XplpPlayer player) {
        StringBuilder sb = new StringBuilder();
        sb.append("XpLp");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append("pass");
        sb.append("|").append(System.currentTimeMillis() - player.getLastPassTime());
        sb.append("|").append(actionSeatMap.get(player.getSeat()));
        LogUtil.msg(sb.toString());
    }

    private void calcPoint(XplpPlayer player, int action, int sameCount, List<XpLp> majiangs) {
        int lostPoint = 0;
        int getPoint = 0;
        int[] seatPointArr = new int[getMaxPlayerCount() + 1];
        if (action == XplpDisAction.action_peng) {
            return;

        } 
        getPoint=getPoint*diFen;
        lostPoint=lostPoint*diFen;
        if (lostPoint != 0) {
            for (XplpPlayer seat : seatMap.values()) {
                if (seat.getUserId() == player.getUserId()) {
//                    player.changeLostPoint(getPoint);
                    seat.changePointArr(2,getPoint);
                    seatPointArr[player.getSeat()] = getPoint;
                } else {
//                    seat.changeLostPoint(lostPoint);
                    seat.changePointArr(2,lostPoint);
                    seatPointArr[seat.getSeat()] = lostPoint;
                }
            }
        }
//        for (ZzMjPlayer p : seatMap.values()) {
//            p.changePointArr(2,p.getLostPoint());
//        }

        String seatPointStr = "";
        for (int i = 1; i <= getMaxPlayerCount(); i++) {
            seatPointStr += seatPointArr[i] + ",";
        }
        seatPointStr = seatPointStr.substring(0, seatPointStr.length() - 1);
        ComMsg.ComRes.Builder res = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_gangFen, seatPointStr);
        GeneratedMessage msg = res.build();
        broadMsgToAll(msg);

        if (action != XplpDisAction.action_chi) {
//            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + ZzMjHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog() + "_" + seatPointStr);
        }
    }

    private void recordDisMajiang(List<XpLp> majiangs, XplpPlayer player) {
        setNowDisCardIds(majiangs);
        // changeDisCardRound(1);
        setDisCardSeat(player.getSeat());
    }

    public List<XpLp> getNowDisCardIds() {
        return nowDisCardIds;
    }

    public void setNowDisCardIds(List<XpLp> nowDisCardIds) {
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
                moMajiang(seatMap.get(nowDisCardSeat), false);
            }
            robotDealAction();

        } else {
            for (int seat : actionSeatMap.keySet()) {
                XplpPlayer player = seatMap.get(seat);
                if (player != null && player.isRobot()) {
					// 如果是机器人可以直接决定
                    List<Integer> actionList = actionSeatMap.get(seat);
                    if (actionList == null) {
                        continue;
                    }
                    List<XpLp> list = new ArrayList<>();
                    if (!nowDisCardIds.isEmpty()) {
                        list = XplpQipaiTool.getVal(player.getHandMajiang(), nowDisCardIds.get(0).getVal());
                    }
                    if (actionList.get(XplpConstants.ACTION_INDEX_HU) == 1 || actionList.get(XplpConstants.ACTION_INDEX_ZIMO) == 1) {
						// 胡
                        playCommand(player, new ArrayList<XpLp>(), XplpDisAction.action_hu);

                    } else if (actionList.get(XplpConstants.ACTION_INDEX_ANGANG) == 1) {
                        playCommand(player, list, XplpDisAction.action_angang);

                    } else if (actionList.get(XplpConstants.ACTION_INDEX_MINGGANG) == 1) {
                        playCommand(player, list, XplpDisAction.action_minggang);

                    } else if (actionList.get(XplpConstants.ACTION_INDEX_PENG) == 1) {
                        playCommand(player, list, XplpDisAction.action_peng);
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
        if (true) {
            return;
        }
        if (isTest()) {
            int nextseat = getNextDisCardSeat();
            XplpPlayer next = seatMap.get(nextseat);
            if (next != null && next.isRobot()) {
                List<Integer> actionList = actionSeatMap.get(next.getSeat());
                if (actionList != null) {
                    List<XpLp> list = null;
                    if (actionList.get(XplpConstants.ACTION_INDEX_HU) == 1 || actionList.get(XplpConstants.ACTION_INDEX_ZIMO) == 1) {
						// 胡
                        playCommand(next, new ArrayList<XpLp>(), XplpDisAction.action_hu);
                    } else if (actionList.get(XplpConstants.ACTION_INDEX_ANGANG) == 1) {
						// 机器人暗杠
                        Map<Integer, Integer> handMap = XplpHelper.toMajiangValMap(next.getHandMajiang());
                        for (Entry<Integer, Integer> entry : handMap.entrySet()) {
                            if (entry.getValue() == 4) {
								// 可以暗杠
                                list = XplpHelper.getMajiangList(next.getHandMajiang(), entry.getKey());
                            }
                        }
                        playCommand(next, list, XplpDisAction.action_angang);

                    } else if (actionList.get(XplpConstants.ACTION_INDEX_MINGGANG) == 1) {
                        Map<Integer, Integer> pengMap = XplpHelper.toMajiangValMap(next.getPeng());
                        for (XpLp handMajiang : next.getHandMajiang()) {
                            if (pengMap.containsKey(handMajiang.getVal())) {
								// 有碰过
                                list = new ArrayList<>();
                                list.add(handMajiang);
                                playCommand(next, list, XplpDisAction.action_minggang);
                                break;
                            }
                        }

                    } else if (actionList.get(XplpConstants.ACTION_INDEX_PENG) == 1) {
                        playCommand(next, list, XplpDisAction.action_peng);
                    }
                } else {
                    List<Integer> handMajiangs = new ArrayList<>(next.getHandPais());
                    XplpQipaiTool.dropHongzhongVal(handMajiangs);
                    int maJiangId = XplpRobotAI.getInstance().outPaiHandle(0, handMajiangs, new ArrayList<Integer>());
                    List<XpLp> majiangList = XplpHelper.toMajiang(Arrays.asList(maJiangId));
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

        
        
        List<Integer> copy = null;
        if(nohua == 1){
        	copy = new ArrayList<>(XplpConstants.zhuanzhuan_mjList);
        }else{
        	copy = new ArrayList<>(XplpConstants.daihua_mjList);
        }
        
        
        addPlayLog(copy.size() + "");
        List<List<XpLp>> list = null;
        if (zp != null) {
            list = XplpTool.fapai(copy, getMaxPlayerCount(), zp);
        } else {
            list = XplpTool.fapai(copy, getMaxPlayerCount());
        }
        int i = 1;
        for (XplpPlayer player : seatMap.values()) {
            player.changeState(player_state.play);
            if (player.getSeat() == lastWinSeat) {
                player.dealHandPais(list.get(0));
                continue;
            }
            player.dealHandPais(list.get(i));
            i++;
        }
		// 桌上剩余的牌
        List<XpLp> cardList = new ArrayList<>(list.get(list.size()-1));
        setLeftMajiangs(list.get(getMaxPlayerCount()));
        if(choupaiNum > 0){
        	int size = cardList.size();
            if(size < choupaiNum){
            	choupaiNum = size;
            }
        	//抽牌
//            chouCards = PaohuziTool.toPhzCardIds(cardList.subList(0, gameModel.getDiscardHoleCards()));
            setLeftMajiangs(cardList.subList(0, size-choupaiNum));
        }
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
    public void setLeftMajiangs(List<XpLp> leftMajiangs) {
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
    public XpLp getLeftMajiang() {
        if (this.leftMajiangs.size() > 0) {
            XpLp majiang = this.leftMajiangs.remove(0);
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
        res.addExt(canDianPao);            //1自摸胡
        res.addExt(isCalcBanker);           //2庄闲算分
        res.addExt(isAutoPlay);             //3是否开启自动托管
        res.addExt(youPaoBiHu);             //4有胡必胡
        res.addExt(diFen);                  //5低分
        res.addExt(guchou);                  //6是否可箍臭
        res.addExt(cpNocp);                  //7吃碰不出同张
        res.addExt(nohua);                  //8不带花
        res.addExt(choupaiNum);                  //9抽牌数量
        res.addStrExt(StringUtil.implode(moTailPai, ","));      //0

        res.setMasterId(getMasterId() + "");
        if (leftMajiangs != null) {
            res.setRemain(leftMajiangs.size());
        } else {
            res.setRemain(0);
        }
        res.setDealDice(dealDice);
        List<PlayerInTableRes> players = new ArrayList<>();
        for (XplpPlayer player : playerMap.values()) {
            PlayerInTableRes.Builder playerRes = player.buildPlayInTableInfo(isrecover);
            if (player.getUserId() == userId) {
                playerRes.addAllHandCardIds(player.getHandPais());
                if (!player.getHandMajiang().isEmpty() && player.getHandMajiang().size() % 3 == 1) {
                    if (player.isOkPlayer() && XplpTool.isTing(player.getHandMajiang())) {
                        playerRes.setUserSate(3);
                    }
                }
            }
            if(player.getSeat() == lastWinSeat && !actionSeatMap.isEmpty()){
            	boolean isSet = false;
            	for (List<Integer> acList : actionSeatMap.values()) {
					if(acList.get(XplpConstants.ACTION_INDEX_GUCHOU) == 1){
						isSet = true;
						playerRes.addExt(1);
						break;
					}
				}
            	if(!isSet){
            		playerRes.addExt(0);
            	}
            }else{
            	playerRes.addExt(0);
            }
            if (player.getSeat() == disCardSeat && nowDisCardIds != null ) {
                playerRes.addAllOutCardIds(XplpHelper.toMajiangIds(nowDisCardIds));
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
        if (actionSeatMap.isEmpty()) {
            int nextSeat = getNextDisCardSeat();
            if (nextSeat != 0) {
                res.setNextSeat(nextSeat);
            }
        }
        res.setRenshu(getMaxPlayerCount());
        res.setLastWinSeat(getLastWinSeat());
        res.addTimeOut((int) XplpConstants.AUTO_TIMEOUT);
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
        setDealDice(0);
        clearMoTailPai();
        paoHuSeat.clear();
        readyTime=0;
        finishFapai=0;
        isSendPiaoFenMsg=0;
    }

    public List<Integer> removeActionSeat(int seat) {
        List<Integer> actionList = actionSeatMap.remove(seat);
        saveActionSeatMap();
        return actionList;
    }

    public void addActionSeat(int seat, List<Integer> actionlist) {
        actionSeatMap.put(seat, actionlist);
        XplpPlayer player = seatMap.get(seat);
        addPlayLog(disCardRound + "_" + seat + "_" + XplpDisAction.action_hasAction + "_" + StringUtil.implode(actionlist) + player.getExtraPlayLog());
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
            nowDisCardIds = XplpHelper.toMajiang(StringUtil.explodeToIntList(info.getNowDisCardIds()));
        }

        if (!StringUtils.isBlank(info.getLeftPais())) {
            try {
                leftMajiangs = XplpHelper.toMajiang(StringUtil.explodeToIntList(info.getLeftPais()));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }




    private Map<Integer, XplpTempAction> loadTempActionMap(String json) {
        Map<Integer, XplpTempAction> map = new ConcurrentHashMap<>();
        if (json == null || json.isEmpty())
            return map;
        JSONArray jsonArray = JSONArray.parseArray(json);
        for (Object val : jsonArray) {
            String str = val.toString();
            XplpTempAction tempAction = new XplpTempAction();
            tempAction.initData(str);
            map.put(tempAction.getSeat(), tempAction);
        }
        return map;
    }

    /**
	 * 检查优先度 如果同时出现一个事件，按出牌座位顺序优先
	 */
    private boolean checkAction(XplpPlayer player, List<XpLp> cardList, List<Integer> hucards, int action) {
		boolean canAction = checkCanAction(player, action);// 是否优先级最高 能执行操作
		if (!canAction) {// 不能操作时 存入临时操作
            int seat = player.getSeat();
            tempActionMap.put(seat, new XplpTempAction(seat, action, cardList, hucards));
			// 玩家都已选择自己的临时操作后 选取优先级最高
            if (tempActionMap.size() == actionSeatMap.size()) {
                int maxAction = Integer.MAX_VALUE;
                int maxSeat = 0;
                Map<Integer, Integer> prioritySeats = new HashMap<>();
                int maxActionSize = 0;
                for (XplpTempAction temp : tempActionMap.values()) {
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
                XplpPlayer tempPlayer = seatMap.get(maxSeat);
                List<XpLp> tempCardList = tempActionMap.get(maxSeat).getCardList();
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
    private void refreshTempAction(XplpPlayer player) {
        tempActionMap.remove(player.getSeat());
		Map<Integer, Integer> prioritySeats = new HashMap<>();// 各位置优先操作
        for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
            int seat = entry.getKey();
            List<Integer> actionList = entry.getValue();
            List<Integer> list = XplpDisAction.parseToDisActionList(actionList);
            int priorityAction = XplpDisAction.getMaxPriorityAction(list);
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
        Iterator<XplpTempAction> iterator = tempActionMap.values().iterator();
        while (iterator.hasNext()) {
            XplpTempAction tempAction = iterator.next();
            if (tempAction.getSeat() == maxPrioritySeat) {
                int action = tempAction.getAction();
                List<XpLp> tempCardList = tempAction.getCardList();
                List<Integer> tempHuCards = tempAction.getHucards();
                XplpPlayer tempPlayer = seatMap.get(tempAction.getSeat());
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
    public boolean checkCanAction(XplpPlayer player, int action) {
		// 优先度为胡杠补碰吃
        List<Integer> stopActionList = XplpDisAction.findPriorityAction(action);
        for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
            if (player.getSeat() != entry.getKey()) {
				// 别人
                boolean can = XplpDisAction.canDisMajiang(stopActionList, entry.getValue());
                if (!can) {
                    return false;
                }
                List<Integer> disActionList = XplpDisAction.parseToDisActionList(entry.getValue());
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
    private boolean canPeng(XplpPlayer player, List<XpLp> majiangs, int sameCount) {
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
	 * 是否能明杠
	 *
	 * @param player
	 * @param majiangs
	 * @param sameCount
	 * @return
	 */
    private boolean canAnGang(XplpPlayer player, List<XpLp> majiangs, int sameCount) {
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
    private boolean canMingGang(XplpPlayer player, List<XpLp> majiangs, int sameCount) {
        List<XpLp> handMajiangs = player.getHandMajiang();
        List<Integer> pengList = XplpHelper.toMajiangVals(player.getPeng());
        if (majiangs.size() == 1) {
            if (player.getSeat() != getNextDisCardSeat()||player.getPassGangValList().contains(majiangs.get(0).getVal())) {
                return false;
            }
            if (handMajiangs.containsAll(majiangs) && pengList.contains(majiangs.get(0).getVal())) {
                return true;
            }
        } else if (majiangs.size() == 3) {
            if (sameCount != 3) {
                return false;
            }
            if (nowDisCardIds.size() != 1 || nowDisCardIds.get(0).getVal() != majiangs.get(0).getVal()) {
                return false;
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


	// 能否点炮
    public boolean canDianPao() {
        if (getCanDianPao() == 0) {
            return true;
        }
        return false;
    }

    /**
	 * @param over
	 * @param selfMo
	 * @param winList
	 * @param prickBirdMajiangIds
	 *            鸟ID
	 * @param seatBirds
	 *            鸟位置
	 * @param seatBridMap
	 *            鸟分
	 * @param isBreak
	 * @return
	 */
    public ClosingMjInfoRes.Builder sendAccountsMsg1(boolean over, boolean selfMo, List<Integer> winList, int[] prickBirdMajiangIds, int[] seatBirds, Map<Integer, Integer> seatBridMap, int catchBirdSeat, boolean isBreak) {
        List<ClosingMjPlayerInfoRes> list = new ArrayList<>();
        for (XplpPlayer player : seatMap.values()) {
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
                    XpLp huMajiang = nowDisCardIds.get(0);
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
                list.add(0, build.build());
            } else {
                list.add(build.build());
            }
        }

        ClosingMjInfoRes.Builder res = ClosingMjInfoRes.newBuilder();
        res.addAllClosingPlayers(list);
        res.setIsBreak(isBreak ? 1 : 0);
        res.setWanfa(getWanFa());
        res.addAllExt(buildAccountsExt(over?1:0));
        if (seatBirds != null) {
            res.addAllBirdSeat(DataMapUtil.toList(seatBirds));
        }
        if (prickBirdMajiangIds != null) {
            res.addAllBird(DataMapUtil.toList(prickBirdMajiangIds));
        }
        res.addAllLeftCards(XplpHelper.toMajiangIds(leftMajiangs));
        res.setCatchBirdSeat(catchBirdSeat);
        for (XplpPlayer player : seatMap.values()) {
            player.writeSocket(res.build());
        }
        broadMsgRoomPlayer(res.build());
        return res;
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
            for (XplpPlayer player : seatMap.values()) {
                if (player.getTotalPoint() > 0 && player.getTotalPoint() < jiaBeiFen) {
                    jiaBeiPoint += player.getTotalPoint() * (jiaBeiShu - 1);
                    player.setTotalPoint(player.getTotalPoint() * jiaBeiShu);
                } else if (player.getTotalPoint() < 0) {
                    loserCount++;
                }
            }
            if (jiaBeiPoint > 0) {
                for (XplpPlayer player : seatMap.values()) {
                    if (player.getTotalPoint() < 0) {
                        player.setTotalPoint(player.getTotalPoint() - (jiaBeiPoint / loserCount));
                    }
                }
            }
        }

        //大结算低于below分+belowAdd分
        if(over&&belowAdd>0&&playerMap.size()==2){
            for (XplpPlayer player : seatMap.values()) {
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
        int fangPaoSeat = selfMo ? 0 : disCardSeat;
        for (XplpPlayer player : seatMap.values()) {
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
                    XpLp huMajiang = nowDisCardIds.get(0);
                    if (!build.getHandPaisList().contains(huMajiang.getId())) {
                        build.addHandPais(huMajiang.getId());
                    }
                    build.setIsHu(huMajiang.getId());
                } else {
                    build.setIsHu(player.getLastMoMajiang().getId());
                }
            }
            if (player.getSeat() == fangPaoSeat) {
                build.setFanPao(1);
                if(huConfirmList.isEmpty()&&leftMajiangs.isEmpty())
                    build.setFanPao(0);
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
            for (XplpPlayer player : seatMap.values()) {
                if (player.getWinLoseCredit() > dyjCredit) {
                    dyjCredit = player.getWinLoseCredit();
                }
            }
            for (ClosingMjPlayerInfoRes.Builder builder : builderList) {
                XplpPlayer player = seatMap.get(builder.getSeat());
                calcCommissionCredit(player, dyjCredit);
                builder.setWinLoseCredit(player.getWinLoseCredit());
                builder.setCommissionCredit(player.getCommissionCredit());
                list.add(builder.build());
            }
        } else {
            for (ClosingMjPlayerInfoRes.Builder builder : builderList) {

                list.add(builder.build());
            }
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
        res.addAllLeftCards(XplpHelper.toMajiangIds(leftMajiangs));
//        res.setCatchBirdSeat(catchBirdSeat);
        for (XplpPlayer player : seatMap.values()) {
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
        ext.add(canDianPao + "");                        //5 只能自摸
        ext.add(isCalcBanker + "");                        //6庄闲
        ext.add(lastWinSeat + "");                            //7庄家
        ext.add(isAutoPlay + "");                        //8托管
        ext.add(youPaoBiHu + "");                        //9 有胡必胡
        ext.add(diFen + "");                            //10 底分
    	ext.add(guchou + "");     						//11 箍臭
    	ext.add(cpNocp + "");     						//12吃碰不打同张
    	ext.add(nohua + "");     				//13 不带花
    	ext.add(choupaiNum + "");     				//14 抽牌数量
        ext.add(isLiuJu() + "");                        //15流局
        ext.add(over+"");//16
        return ext;
    }

    @Override
    public void sendAccountsMsg() {
        ClosingMjInfoRes.Builder builder = sendAccountsMsg(true, false, null, null, null, null, 0, true);
        saveLog(true, 0l, builder.build());
    }

    @Override
    public Class<? extends Player> getPlayerClass() {
        return XplpPlayer.class;
    }

    @Override
    public int getWanFa() {
        return getPlayType();
    }

//	@Override
//	public boolean isTest() {
//		return super.isTest() && ZzMjConstants.isTest;
//	}

    @Override
    public void checkReconnect(Player player) {
        ((XplpPlayer) player).checkAutoPlay(0, true);
        if (state == table_state.play) {
            XplpPlayer player1 = (XplpPlayer) player;
            if (player1.getHandPais() != null && player1.getHandPais().size() > 0) {
                sendTingInfo(player1);
            }
        }
        sendPiaoReconnect(player);
    }

    private void sendPiaoReconnect(Player player){
        if(piaoFenType==0||maxPlayerCount!=getPlayerCount())
            return;
        int count=0;
        for(Map.Entry<Integer,XplpPlayer> entry:seatMap.entrySet()){
            player_state state = entry.getValue().getState();
            if(state==player_state.play||state==player_state.ready)
                count++;
        }
        if(count!=maxPlayerCount)
            return;

        for(Map.Entry<Integer,XplpPlayer> entry:seatMap.entrySet()){
            XplpPlayer p = entry.getValue();
            if(p.getUserId()==player.getUserId()){
                if(p.getPiaoFen()==-1){
                    player.writeComMessage(WebSocketMsgType.res_code_zzmj_piaofen);
                    continue;
                }
            }else {
                List<Integer> l=new ArrayList<>();
                l.add((int)p.getUserId());
                l.add(p.getPiaoFen());
                player.writeComMessage(WebSocketMsgType.res_code_zzmj_broadcast_piaofen, l);
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
            for (XplpPlayer player : seatMap.values()) {
                if (player.getLastCheckTime() > 0) {
                    player.setLastCheckTime(player.getLastCheckTime() + 1 * 1000);
                }
            }
            return;
        }
        for(XplpPlayer player : seatMap.values()){
    		List<Integer> actionList = actionSeatMap.get(player.getSeat());
    		if(actionList!= null && actionList.get(XplpConstants.ACTION_INDEX_ZIMO) == 1) {
                     // 胡
                     playCommand(player, new ArrayList<XpLp>(), XplpDisAction.action_hu);
    		}
    	}
        if (isAutoPlay < 1) {
            return;
        }

        if (isAutoPlayOff()) {
            // 托管关闭
            for (int seat : seatMap.keySet()) {
                XplpPlayer player = seatMap.get(seat);
                player.setAutoPlay(false, false);
                player.setCheckAutoPlay(false);
            }
            return;
        }

//        if (piaoFenType>0&&isSendPiaoFenMsg == 1 && finishFapai == 0) {
		// //飘分模式，还没发牌
//            for (ZzMjPlayer player : seatMap.values()) {
//                if(player.getPiaoFen()!=-1){
//                    continue;
//                }
//                boolean auto = checkPlayerAuto(player, autoTimeOut);
//                if (auto) {
//                    piaoFen(player, piaoFenType>=3?0:piaoFenType);
//                }
//            }
//            return;
//        }
        if (state == table_state.play) {
            autoPlay();
        } else {
            if (getPlayedBureau() == 0) {
                return;
            }
            readyTime ++;
//            for (HzMjPlayer player : seatMap.values()) {
//                if (player.checkAutoPlay(1, false)) {
//                    autoReady(player);
//                }
//            }
			// 开了托管的房间，xx秒后自动开始下一局
            for (XplpPlayer player : seatMap.values()) {
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
                    XplpPlayer player = seatMap.get(seat);
                    if (player == null) {
                        continue;
                    }
                    if (!player.checkAutoPlay(2, false)) {
                        continue;
                    }
                    playCommand(player, new ArrayList<>(), XplpDisAction.action_hu);
                }
                return;
            } else {
                int action = 0, seat = 0;
                for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
                    seat = entry.getKey();
                    List<Integer> actList = XplpDisAction.parseToDisActionList(entry.getValue());
                    if (actList == null) {
                        continue;
                    }

                    action = XplpDisAction.getAutoMaxPriorityAction(actList);
                    XplpPlayer player = seatMap.get(seat);
                    if (!player.checkAutoPlay(0, false)) {
                        continue;
                    }
                    boolean chuPai = false;
                    if (player.isAlreadyMoMajiang()) {
                        chuPai = true;
                    }
                    if (action == XplpDisAction.action_peng) {
                        if (player.isAutoPlaySelf()) {
							// 自己开启托管直接过
                            playCommand(player, new ArrayList<>(), XplpDisAction.action_pass);
                            if (chuPai) {
                                autoChuPai(player);
                            }
                        } else {
                            if (nowDisCardIds != null && !nowDisCardIds.isEmpty()) {
                                XpLp mj = nowDisCardIds.get(0);
                                List<XpLp> mjList = new ArrayList<>();
                                for (XpLp handMj : player.getHandMajiang()) {
                                    if (handMj.getVal() == mj.getVal()) {
                                        mjList.add(handMj);
                                        if (mjList.size() == 2) {
                                            break;
                                        }
                                    }
                                }
                                playCommand(player, mjList, XplpDisAction.action_peng);
//							autoChuPai(player);
                            }
                        }
                    } else {
                        playCommand(player, new ArrayList<>(), XplpDisAction.action_pass);
                        if (chuPai) {
                            autoChuPai(player);
                        }
                    }
                }
            }
        } else {
            boolean finishPiaoFen=true;
            for(XplpPlayer player:seatMap.values()){
                if(player.getPiaoFen()==-1){
                    finishPiaoFen=false;
                    break;
                }
            }
            if(piaoFenType>0&&!finishPiaoFen){
                for(XplpPlayer player:seatMap.values()){
                    if(player.getPiaoFen()==-1){
                        if (player == null || !player.checkAutoPiaoFen()) {
                            continue;
                        }
                        piaoFen(player,0);
                    }
                }
            }else {
                XplpPlayer player = seatMap.get(nowDisCardSeat);
                if (player == null || !player.checkAutoPlay(0, false)) {
                    return;
                }
                autoChuPai(player);
            }
        }
    }

    public void autoChuPai(XplpPlayer player) {

		// ZzMjQipaiTool.dropHongzhongVal(handMajiangs);红中麻将要去掉红中
//					int mjId = ZzMjRobotAI.getInstance().outPaiHandle(0, handMjIds, new ArrayList<>());
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
            List<XpLp> mjList = XplpHelper.toMajiang(Arrays.asList(mjId));
            playCommand(player, mjList, XplpDisAction.action_chupai);
        }
    }

    @Override
    public boolean isCanJoin0(Player player) {
        if (getPayType() == 2 || getPayType() == 3) {
			// 房主支付
            return true;
        }
        int needCards = PayConfigUtil.get(playType, totalBureau, getMaxPlayerCount(), getPayType() == 1 ? 0 : 1);
        if (needCards < 0) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_diamond_err));
            return false;
        }
		// 钻石不足不能加入房间
        if (!player.isRobot() && player.getFreeCards() + player.getCards() < needCards) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_diamond_err));
            return false;
        }
        return true;
    }

    @Override
    public void createTable(Player player, int play, int bureauCount, Object... objects) throws Exception {
    }

    @Override
    public boolean isAllReady() {
        if (getPlayerCount() < getMaxPlayerCount()) {
            return false;
        }
        for (Player player : getSeatMap().values()) {
            if(!player.isRobot()){
                if(piaoFenType>1){
                    if (!(player.getState() == player_state.ready||player.getState() == player_state.play))
                        return false;
                }else {
                    if(player.getState() != player_state.ready)
                        return false;
                }
            }
        }
        if(finishFapai==1){
        	return false;
        }
        changeTableState(table_state.play);
        if (piaoFenType==5) {
            boolean piaoFenOver = true;
            for (XplpPlayer player : playerMap.values()) {
                if(player.getPiaoFen()==-1){
                    piaoFenOver = false;
                    break;
                }
            }
            if(!piaoFenOver){
                if (isSendPiaoFenMsg==0 && finishFapai==0) {
                    LogUtil.msgLog.info("xplp|sendPiaoFen|" + getId() + "|" + getPlayBureau());
                    ComRes msg = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_zzmj_piaofen).build();
                    for (XplpPlayer player : playerMap.values()) {
                        if(player.getPiaoFen()==-1)
                            player.writeSocket(msg);
                    }
                    isSendPiaoFenMsg = 1;
                }
                return false;
            }
        }else if(piaoFenType<=3&&piaoFenType>0){
            for (XplpPlayer player : playerMap.values()) {
                player.setPiaoFen(piaoFenType);
                for (XplpPlayer p : playerMap.values()) {
                    p.writeComMessage(WebSocketMsgType.res_code_zzmj_broadcast_piaofen, (int)player.getUserId(),player.getPiaoFen());
                }
            }
        }
        return true;
    }

    public synchronized void piaoFen(XplpPlayer player,int fen){
        if (piaoFenType<=4||player.getPiaoFen()!=-1)
            return;
        if(fen>4||fen<0)
            return;
        player.setPiaoFen(fen);
        StringBuilder sb = new StringBuilder("xplp");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append("piaoFen").append("|").append(fen);
        LogUtil.msgLog.info(sb.toString());
        int confirmTime=0;
        for (Map.Entry<Integer, XplpPlayer> entry : seatMap.entrySet()) {
            entry.getValue().writeComMessage(WebSocketMsgType.res_code_zzmj_broadcast_piaofen, (int)player.getUserId(),player.getPiaoFen());
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

    public static final List<Integer> wanfaList = Arrays.asList(GameUtil.play_type_xupu_laopai);

    public static void loadWanfaTables(Class<? extends BaseTable> cls) {
        for (Integer integer : wanfaList) {
            TableManager.wanfaTableTypesPut(integer, cls);
        }
    }

    public void logFaPaiTable() {
        StringBuilder sb = new StringBuilder();
        sb.append("XpLp");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append("faPai");
        sb.append("|").append(playType);
        sb.append("|").append(maxPlayerCount);
        sb.append("|").append(getPayType());
        sb.append("|").append(lastWinSeat);
        LogUtil.msg(sb.toString());
    }

    public void logFaPaiPlayer(XplpPlayer player, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("XpLp");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append("faPai");
        sb.append("|").append(player.getHandMajiang());
        sb.append("|").append(actListToString(actList));
        LogUtil.msg(sb.toString());
    }

    public void logAction(XplpPlayer player, int action, List<XpLp> mjs, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("XpLp");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        String actStr = "unKnown-" + action;
        if (action == XplpDisAction.action_peng) {
            actStr = "peng";
        } else if (action == XplpDisAction.action_minggang) {
            actStr = "mingGang";
        } else if (action == XplpDisAction.action_chupai) {
            actStr = "chuPai";
        } else if (action == XplpDisAction.action_pass) {
            actStr = "guo";
        } else if (action == XplpDisAction.action_angang) {
            actStr = "anGang";
        } else if (action == XplpDisAction.action_chi) {
            actStr = "chi";
        }
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append(player.isAutoPlaySelf() ? 1 : 0);
        sb.append("|").append(actStr);
        sb.append("|").append(mjs);
        sb.append("|").append(actListToString(actList));
        LogUtil.msg(sb.toString());
    }

    public void logMoMj(XplpPlayer player, XpLp mj, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("XpLp");
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
//        sb.append("|").append(player.getHandMajiang());
        LogUtil.msg(sb.toString());
    }

    public void logChuPaiActList(XplpPlayer player, XpLp mj, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("XpLp");
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

    public void logActionHu(XplpPlayer player, List<XpLp> mjs, String daHuNames) {
        StringBuilder sb = new StringBuilder();
        sb.append("XpLp");
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
                if (i == XplpConstants.ACTION_INDEX_HU) {
                    sb.append("hu");
                } else if (i == XplpConstants.ACTION_INDEX_PENG) {
                    sb.append("peng");
                } else if (i == XplpConstants.ACTION_INDEX_MINGGANG) {
                    sb.append("mingGang");
                } else if (i == XplpConstants.ACTION_INDEX_ANGANG) {
                    sb.append("anGang");
                } else if (i == XplpConstants.ACTION_INDEX_CHI) {
                    sb.append("chi");
                } else if (i == XplpConstants.ACTION_INDEX_ZIMO) {
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
			// 补张，取一张
            for (int i = 0; i < leftMjCount; i++) {
                int nowIndex = i + startIndex;
                if (!moTailPai.contains(nowIndex)) {
                    moTailPai.add(nowIndex);
                    break;
                }
            }

        } else {
            int duo = gangDice / 10 + gangDice % 10;
			// 开杠打色子，取两张
            for (int i = 0, j = 0; i < leftMjCount; i++) {
                int nowIndex = i + startIndex;
                if (nowIndex % 2 == 1) {
					j++; // 取到第几剁
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


    public void sendTingInfo(XplpPlayer player) {
        if (player.isAlreadyMoMajiang()) {
            if (actionSeatMap.containsKey(player.getSeat())) {
                return;
            }
            DaPaiTingPaiRes.Builder tingInfo = DaPaiTingPaiRes.newBuilder();
            List<XpLp> cards = new ArrayList<>(player.getHandMajiang());
            Map<Integer, List<XpLp>> checked = new HashMap<>();
            for (XpLp card : cards) {
                List<XpLp> lackPaiList;
                if (checked.containsKey(card.getVal())) {
                    lackPaiList = checked.get(card.getVal());
                } else {
                	 List<XpLp> cardsCopy =  new ArrayList<>(cards);
                	 cardsCopy.remove(card);
                    lackPaiList = XplpTool.getLackList(cardsCopy);
                    if (lackPaiList != null && lackPaiList.size() > 0) {
                        checked.put(card.getVal(), lackPaiList);
                    } else {
                        continue;
                    }
                }
                DaPaiTingPaiInfo.Builder ting = DaPaiTingPaiInfo.newBuilder();
                ting.setMajiangId(card.getId());
                for (XpLp lackPai : lackPaiList) {
                    ting.addTingMajiangIds(lackPai.getId());
                }
                tingInfo.addInfo(ting.build());
            }
            if (tingInfo.getInfoCount() > 0) {
            	System.out.println("发送听牌数据："+tingInfo.build());
                player.writeSocket(tingInfo.build());
            }
        } else {
            List<XpLp> cards = new ArrayList<>(player.getHandMajiang());
            List<XpLp> lackPaiList = XplpTool.getLackList(cards);
            if (lackPaiList == null || lackPaiList.size() == 0) {
                return;
            }
            TingPaiRes.Builder ting = TingPaiRes.newBuilder();
            for (XpLp lackPai : lackPaiList) {
                ting.addMajiangIds(lackPai.getId());
            }
            System.out.println("发送听牌数据："+ting.build());
            player.writeSocket(ting.build());
        }
    }

    public String getTableMsg() {
        Map<String, Object> json = new HashMap<>();
		json.put("wanFa", "溆浦老牌");
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

    public boolean isNohua() {
		return nohua == 1;
	}

	@Override
    public String getGameName() {
		return "溆浦老牌";
    }
	
	public static void main(String[] args) {
		float chengbenjia = 5.34f;
		int chengbenNum = 33500;
		float chengben =  (chengbenjia * chengbenNum);
		float chaodiJia = 4.52f;
		int chaodiNum = 13000;
		float chaodiben =  (chaodiJia * chaodiNum);
		
		float sumCheng = chengben+chaodiben;
		int sumNum = chengbenNum + chaodiNum;
		System.out.println("本次抄底成本："+chaodiben);
		System.out.println("总成本："+sumCheng);
		float junjia = sumCheng / sumNum;
		System.out.println("均价："+junjia);
		float xianjia = 5.49f;
		System.out.println("现价："+xianjia);
		float xianZichan = sumNum * xianjia;
		float yingkui = xianZichan - sumCheng;
		System.out.println("盈亏："+yingkui);
		
//		int zhu = 5;
//		for (int i = 0; i < zhu; i++) {
//			Set<Integer> set = new TreeSet<>();
//			for (int j = 0; j < 100; j++) {
//				set.add((int)(1+Math.random()*(32)));
//				if(set.size() == 6){
//					break;
//				}
//			}
////			Set<Integer> sortSet = new TreeSet<Integer>((o1, o2) -> o2.compareTo(o1));
////			sortSet.addAll(set);
////			set.stream().sorted(Comparator.reverseOrder());
//			System.out.print("红球："+set);
//			System.out.println("  蓝球："+(int)(1+Math.random()*(16)));
//		}
		
	}
}
