package com.sy599.game.qipai.cxmj.bean;

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

import com.sy599.game.qipai.cxmj.rule.MingTang;
import com.sy599.game.qipai.cxmj.tool.ting.TingTool;
import org.apache.commons.lang.mutable.MutableInt;
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
import com.sy599.game.msg.serverPacket.TableRes;
import com.sy599.game.msg.serverPacket.TableRes.CreateTableRes;
import com.sy599.game.msg.serverPacket.TableRes.DealInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes;
import com.sy599.game.qipai.cxmj.constant.CxMjConstants;
import com.sy599.game.qipai.cxmj.constant.CxMj;
import com.sy599.game.qipai.cxmj.rule.CxMjRobotAI;
import com.sy599.game.qipai.cxmj.tool.CxMjHelper;
import com.sy599.game.qipai.cxmj.tool.CxMjQipaiTool;
import com.sy599.game.qipai.cxmj.tool.CxMjResTool;
import com.sy599.game.qipai.cxmj.tool.CxMjTool;
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


public class CxMjTable extends BaseTable {
    /**
	 * 当前打出的牌
	 */
    private List<CxMj> nowDisCardIds = new ArrayList<>();
    private Map<Integer, List<Integer>> actionSeatMap = new ConcurrentHashMap<>();
    /**
	 * 玩家位置对应临时操作 当同时存在多个可做的操作时 1当优先级最高的玩家最先操作 则清理所有操作提示 并执行该操作
	 * 2当操作优先级低的玩家先选择操作，则玩家先将所做操作存入临时操作中 当玩家优先级最高的玩家操作后 在判断临时操作是否可执行并执行
	 */
    private Map<Integer, CxMjTempAction> tempActionMap = new ConcurrentHashMap<>();
    private int maxPlayerCount = 4;
    private List<CxMj> leftMajiangs = new ArrayList<>();
	/*** 玩家map */
    private Map<Long, CxMjPlayer> playerMap = new ConcurrentHashMap<Long, CxMjPlayer>();
	/*** 座位对应的玩家 */
    private Map<Integer, CxMjPlayer> seatMap = new ConcurrentHashMap<Integer, CxMjPlayer>();
	private List<Integer> huConfirmList = new ArrayList<>();// 胡牌数组
    /**
	 * 摸麻将的seat
	 */
    private int moMajiangSeat;
    /**
	 * 摸杠的麻将
	 */
    private CxMj moGang;
    /**
	 * 当前杠的人
	 */
    private int moGangSeat;
    private int moGangSameCount;
    /**
	 * 摸杠胡
	 */
    private List<Integer> moGangHuList = new ArrayList<>();
    /**
	 * 骰子点数
	 **/
    private int dealDice;
    /**
	 * 0点炮胡 1自摸胡
	 **/
    private int canDianPao;
    /**
	 * 胡7对
	 **/
    private int hu7dui=1;

	private int isAutoPlay = 0;// 是否开启自动托管
    
    private int readyTime = 0 ;

	// private int auto_ready_time = 15000;//自动落座最大等待时间
    /**
	 * 抢杠胡开关
	 **/
    private int qiangGangHu;
    /**
	 * 点杠可胡
	 **/
    private int dianGangKeHu;
    /**
	 * 抢杠胡包三家
	 **/
    private int qiangGangHuBaoSanJia;
    /**
	 * 底分
	 **/
    private int diFen=1;
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
	// 飘分 0:不飘 1:每局飘1 2:每局飘2 3:飘3 4:自由飘分 5:首局定飘
    private int piaoFenType=0;
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





    //----------------------
    //粘5就算
    private int nian5=0;


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
		maxPlayerCount = StringUtil.getIntValue(params, 7, 4);// 人数
        canDianPao = StringUtil.getIntValue(params, 4, 1);// 0:是否可点炮胡
        nian5 = StringUtil.getIntValue(params, 5, 0);// 0:是否可点炮胡
        isAutoPlay = StringUtil.getIntValue(params, 8, 0);// 托管时间
        if(isAutoPlay==1) {
            // 默认1分钟
            isAutoPlay=60;
        }
        autoPlayGlob = StringUtil.getIntValue(params, 9, 0);// 单局托管
        if(maxPlayerCount==2){
            jiaBei = StringUtil.getIntValue(params, 10, 0);
            jiaBeiShu = StringUtil.getIntValue(params, 11, 1);
            jiaBeiFen = StringUtil.getIntValue(params, 12, 100);
            int belowAdd = StringUtil.getIntValue(params, 13, 0);
            if(belowAdd<=100&&belowAdd>=0)
                this.belowAdd=belowAdd;
            int below = StringUtil.getIntValue(params, 14, 0);
            if(below<=100&&below>=0){
                this.below=below;
            }

        }











        changeExtend();
        if (!isJoinPlayerAllotSeat()) {
			// getRoomModeMap().put("1", "1"); //可观战（默认）
        }
    }

    @Override
    public JsonWrapper buildExtend0(JsonWrapper wrapper) {
        for (CxMjPlayer player : seatMap.values()) {
            wrapper.putString(player.getSeat(), player.toExtendStr());
        }
        wrapper.putString(5, StringUtil.implode(huConfirmList, ","));
        wrapper.putInt(6, moMajiangSeat);
        if (moGang != null) {
            wrapper.putInt(7, moGang.getId());
        } else {
            wrapper.putInt(7, 0);
        }
        wrapper.putString(8, StringUtil.implode(moGangHuList, ","));
        wrapper.putInt(9, canDianPao);
        wrapper.putInt(10, hu7dui);

        JSONArray tempJsonArray = new JSONArray();
        for (int seat : tempActionMap.keySet()) {
            tempJsonArray.add(tempActionMap.get(seat).buildData());
        }
        wrapper.putString("tempActions", tempJsonArray.toString());
        wrapper.putInt(11, maxPlayerCount);
        wrapper.putInt(12, isAutoPlay);
        wrapper.putInt(13, moGangSeat);
        wrapper.putInt(14, moGangSameCount);
        wrapper.putString(15, StringUtil.implode(moTailPai, ","));
        wrapper.putInt(16, diFen);
        wrapper.putInt(17, jiaBei);
        wrapper.putInt(18, jiaBeiFen);
        wrapper.putInt(19, jiaBeiShu);
        wrapper.putInt(20, bankerRand);
        wrapper.putString(21,StringUtil.implode(paoHuSeat, ","));
        wrapper.putInt(22, autoPlayGlob);
        wrapper.putInt(23, finishFapai);
        wrapper.putInt(24, belowAdd);
        wrapper.putInt(25, below);
        wrapper.putInt(26, nian5);
        wrapper.putInt(27, lastId);
        wrapper.putInt(28, fangGangSeat);
        return wrapper;
    }

    @Override
    public void initExtend0(JsonWrapper wrapper) {
        for (CxMjPlayer player : seatMap.values()) {
            player.initExtend(wrapper.getString(player.getSeat()));
        }
        String huListstr = wrapper.getString(5);
        if (!StringUtils.isBlank(huListstr)) {
            huConfirmList = StringUtil.explodeToIntList(huListstr);
        }
        moMajiangSeat = wrapper.getInt(6, 0);
        int moGangMajiangId = wrapper.getInt(7, 0);
        if (moGangMajiangId != 0) {
            moGang = CxMj.getMajang(moGangMajiangId);
        }
        String moGangHu = wrapper.getString(8);
        if (!StringUtils.isBlank(moGangHu)) {
            moGangHuList = StringUtil.explodeToIntList(moGangHu);
        }
        canDianPao = wrapper.getInt(9, 1);
        hu7dui = wrapper.getInt(10, 0);
        tempActionMap = loadTempActionMap(wrapper.getString("tempActions"));
        maxPlayerCount = wrapper.getInt(11, 4);
        isAutoPlay = wrapper.getInt(12, 0);

        if(isAutoPlay ==1) {
            isAutoPlay=60;
        }

        moGangSeat = wrapper.getInt(13, 0);
        moGangSameCount = wrapper.getInt(14, 0);
        String s = wrapper.getString(15);
        if (!StringUtils.isBlank(s)) {
            moTailPai = StringUtil.explodeToIntList(s);
        }
        diFen = wrapper.getInt(16,1);
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
        nian5= wrapper.getInt(26,0);
        lastId= wrapper.getInt(27,0);
        fangGangSeat= wrapper.getInt(28,0);
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
        return hu7dui == 1;
    }

    public void setHu7dui(int hu7dui) {
        this.hu7dui = hu7dui;
    }

    public int getCanDianPao() {
        return canDianPao;
    }

    public int getNian5() {
        return nian5;
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


    @Override
    public void calcOver() {
        List<Integer> winList = new ArrayList<>(huConfirmList);
        boolean selfMo = false;
		List<Integer> cardsId = null;// 鸟牌ID
		Map<Integer, Integer> seatBridMap = new HashMap<>();// 位置,中鸟数
		int catchBirdSeat = 0;// 抓鸟人座位
        if (winList.size() == 0 && leftMajiangs.size()<=getMaxPlayerCount()) {

        } else {
			// 先判断是自摸还是放炮
            if (winList.size() == 1 && seatMap.get(winList.get(0)).getHandMajiang().size() % 3 == 2 && winList.get(0) == moMajiangSeat) {
                selfMo = true;
            }
            if(seatMap.get(winList.get(0)).getVirtualHu()!=0){
                selfMo=true;
            }

            if (selfMo) {
				// 自摸
                CxMjPlayer winner = seatMap.get(winList.get(0));
                int winFen=0;
                int loseFen= MingTang.getMingTangFen(winner.getHuType(),diFen);
                if(fangGangSeat!=0){
                    loseFen*=(getMaxPlayerCount()-1);
                    CxMjPlayer fgPlayer = seatMap.get(fangGangSeat);
                    fgPlayer.changePoint(-loseFen);
                    winFen+=loseFen;
                }else {
                    for (int seat : seatMap.keySet()) {
                        if (!winList.contains(seat)) {
                            CxMjPlayer player = seatMap.get(seat);
                            player.changePoint(-loseFen);
                            winFen+=loseFen;
                        }
                    }
                }
                winner.changePoint(winFen);
                winner.addZiMoNum(1);
            } else {
				// 小胡接炮 每人1分
				// 如果庄家输牌失分翻倍
                CxMjPlayer losePlayer = seatMap.get(disCardSeat);
                losePlayer.addDianPaoNum(1);
                int loseFen=0;
                for (int winnerSeat : winList) {
                    CxMjPlayer winPlayer = seatMap.get(winnerSeat);
                    int winFen = MingTang.getMingTangFen( winPlayer.getHuType(), diFen);
                    winPlayer.changePoint(winFen);
                    winPlayer.addJiePaoNum(1);
                    loseFen+=winFen;
                }
                losePlayer.changePoint(-loseFen);
            }
			// 非流局的情况下算飘分
            for(int seat:seatMap.keySet()){
                CxMjPlayer player = seatMap.get(seat);
                player.setPoint(player.getPoint()+player.getWinLostPiaoFen());
                player.changeTotalPoint(player.getWinLostPiaoFen());
            }
        }
        
        
        boolean over = playBureau == totalBureau;
        if(autoPlayGlob >0) {
			// //是否解散
            boolean diss = false;
            if(autoPlayGlob ==1) {
            	 for (CxMjPlayer seat : seatMap.values()) {
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

        ClosingMjInfoRes.Builder res = sendAccountsMsg(over, selfMo, winList, cardsId, null, seatBridMap, catchBirdSeat, false);
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
        for (CxMjPlayer player : seatMap.values()) {
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
		for (CxMjPlayer seat : seatMap.values()) {
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

    public void setWinLostPiaoFen(CxMjPlayer win, CxMjPlayer lost) {
        if(piaoFenType>0){
            lost.setWinLostPiaoFen(-win.getPiaoFen()-lost.getPiaoFen());
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
        for (CxMjPlayer player : playerMap.values()) {
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
            CxMj birdMj = getLeftMajiang();
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
                            CxMj mj = CxMj.getMajiangByValue(birdMjVals[i]);
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
                CxMj birdMj = getLeftMajiang();
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
                tempMap.put("nowDisCardIds", StringUtil.implode(CxMjHelper.toMajiangIds(nowDisCardIds), ","));
            }
            if (tempMap.containsKey("leftPais")) {
                tempMap.put("leftPais", StringUtil.implode(CxMjHelper.toMajiangIds(leftMajiangs), ","));
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
        addPlayLog(disCardRound + "_" + lastWinSeat + "_" + CxMjDisAction.action_dice + "_" + dealDice);
        setDealDice(dealDice);
		// 天胡或者暗杠
        logFaPaiTable();
        for (CxMjPlayer tablePlayer : seatMap.values()) {
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
            	addPlayLog(getDisCardRound() + "_" +tablePlayer.getSeat() + "_" + CxMjConstants.action_tuoguan + "_" +1+ tablePlayer.getExtraPlayLog());
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
    }

    public void moMajiang(CxMjPlayer player, boolean isBuZhang) {
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
        CxMj majiang = null;
        if (disCardRound != 0) {
			// 玩家手上的牌是双数，已经摸过牌了
            if (player.isAlreadyMoMajiang()) {
                return;
            }
            if (getLeftMajiangCount() <=getMaxPlayerCount()) {
                calcOver();
                return;
            }
            if (GameServerConfig.isDebug() && zp != null && zp.size()==1) {
                majiang=CxMj.getMajiangByValue(zp.get(0).get(0));
                zp.clear();
            }else {
                majiang = getLeftMajiang();
            }
        }
        if (majiang != null) {
            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + CxMjDisAction.action_moMjiang + "_" + majiang.getId() + player.getExtraPlayLog());
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
        lastId=majiang.getId();
        setMoMajiangSeat(player.getSeat());
        List<Integer> arr = player.checkMo(majiang);

        if (!arr.isEmpty()) {
            addActionSeat(player.getSeat(), arr);
        }
        logMoMj(player, majiang, arr);
        MoMajiangRes.Builder res = MoMajiangRes.newBuilder();
        res.setUserId(player.getUserId() + "");
        res.setSeat(player.getSeat());
        res.setRemain(getLeftMajiangCount());
        for (CxMjPlayer seat : seatMap.values()) {
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

        if(getLeftMajiangCount()==getMaxPlayerCount()){
            if(arr.contains(1)){
                if(arr.get(CxMjConstants.ACTION_INDEX_HU)!=1&&arr.get(CxMjConstants.ACTION_INDEX_MINGGANG)!=1){
                    calcOver();
                    return;
                }
            }else {
                calcOver();
                return;
            }
        }
        sendTingInfo(player);
        for (Player roomPlayer : roomPlayerMap.values()) {
            MoMajiangRes.Builder copy = res.clone();
            roomPlayer.writeSocket(copy.build());
        }

    }

    public void gangBuPai(Integer id,CxMjPlayer player){
        if(id!=1004&&id!=1005)
            return;
        if(player.isAlreadyMoMajiang())
            return;
        player.moMajiang(CxMj.getMajang(id));
        List<Integer> arr = player.checkMo(null);
        addActionSeat(player.getSeat(),arr);
        MoMajiangRes.Builder res = MoMajiangRes.newBuilder();
        res.setUserId(player.getUserId() + "");
        res.setSeat(player.getSeat());
        res.setRemain(getLeftMajiangCount());
        for (CxMjPlayer seat : seatMap.values()) {
            if (seat.getUserId() == player.getUserId()) {
                MoMajiangRes.Builder copy = res.clone();
                copy.addAllSelfAct(arr);
                copy.setMajiangId(id);
                seat.writeSocket(copy.build());
            } else {
                seat.writeSocket(res.build());
            }
        }
        player.setBuId(id);
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + CxMjDisAction.action_moMjiang + "_" + id + player.getExtraPlayLog());
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
    private void hu(CxMjPlayer player, List<CxMj> majiangs, int action) {
        if (state != table_state.play) {
            return;
        }
        List<Integer> actionList = actionSeatMap.get(player.getSeat());
		if (actionList == null || (actionList.get(CxMjConstants.ACTION_INDEX_HU) != 1
				&& actionList.get(CxMjConstants.ACTION_INDEX_ZIMO) != 1)) {// 如果集合为空或者第一操作不为胡，则返回
            return;
        }
        if (huConfirmList.contains(player.getSeat())) {
            return;
        }
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();


        CxMj huCard=null;
        boolean zimo = player.isAlreadyMoMajiang();
        if(lastId!=0)
            huCard= CxMj.getMajang(lastId);
        if(player.getVirtualHu()!=0){
            huCard=CxMj.getMajang(player.getVirtualHu());
            zimo=true;
        }
        if(huCard!=null&&!player.getHandMajiang().contains(huCard))
            player.getHandMajiang().add(huCard);
        List<CxMj> huHand = new ArrayList<>(player.getHandMajiang());
        if (!TingTool.isHu(CxMjHelper.toMajiangIds(huHand))) {
            return;
        }

        List<Integer> list = MingTang.get(player.getCardTypes(), player.getHandMajiang(),this);
        player.setHuType(list);
        if (!zimo) {
            builder.setFromSeat(disCardSeat);
        } else {
            builder.addHuArray(CxMjConstants.HU_ZIMO);
            player.getHuType().add(MingTang.MINGTANG_ZIMO);
        }

//        if (moGangHuList.contains(player.getSeat())) {
//            CxMjPlayer moGangPlayer = seatMap.get(moGangSeat);
//            if (moGangPlayer == null) {
//                moGangPlayer = getPlayerByHasMajiang(moGang);
//            }
//            if (moGangPlayer == null) {
//                moGangPlayer = seatMap.get(moMajiangSeat);
//            }
//            List<CxMj> moGangMajiangs = new ArrayList<>();
//            moGangMajiangs.add(moGang);
//            moGangPlayer.addOutPais(moGangMajiangs, CxMjDisAction.action_chupai, 0);
//			// 摸杠被人胡了 相当于自己出了一张牌
//            recordDisMajiang(moGangMajiangs, moGangPlayer);
////			addPlayLog(disCardRound + "_" + player.getSeat() + "_" + CxMjDisAction.action_chupai + "_" + CxMjHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
//            moGangPlayer.qGangUpdateOutPais(moGang);
//        }
        buildPlayRes(builder, player, action, huHand);
        if (zimo) {
            builder.setZimo(1);
        }
        if (!huConfirmList.isEmpty()) {
            builder.addExt(StringUtil.implode(huConfirmList, ","));
        }
		// 胡
        for (CxMjPlayer seat : seatMap.values()) {
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
        List<CxMj> huPai = new ArrayList<>();
        huPai.add(huHand.get(huHand.size() - 1));
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + CxMjHelper.toMajiangStrs(huPai) + "_" + StringUtil.implode(player.getHuType(), ",") + player.getExtraPlayLog());
        logActionHu(player, majiangs, "");
        if (isCalcOver()) {
			// 等待别人胡牌 如果都确认完了，胡
            calcOver();
        } else {
            //removeActionSeat(player.getSeat());
            player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip, action);
        }
    }

    private void buildPlayRes(PlayMajiangRes.Builder builder, Player player, int action, List<CxMj> majiangs) {
        CxMjResTool.buildPlayRes(builder, player, action, majiangs);
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
    private CxMjPlayer getPlayerByHasMajiang(CxMj majiang) {
        for (CxMjPlayer player : seatMap.values()) {
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
            for (int huseat : huActionList) {
                if (!huConfirmList.contains(huseat) &&
                        !(tempActionMap.containsKey(huseat) && tempActionMap.get(huseat).getAction() == CxMjDisAction.action_hu)) {
                    over = false;
                    break;
                }
            }
        }

        if (!over) {
            CxMjPlayer disMajiangPlayer = seatMap.get(disCardSeat);
            for (int huseat : huActionList) {
                if (huConfirmList.contains(huseat)) {
                    continue;
                }
                PlayMajiangRes.Builder disBuilder = PlayMajiangRes.newBuilder();
                CxMjPlayer seatPlayer = seatMap.get(huseat);
                buildPlayRes(disBuilder, disMajiangPlayer, 0, null);
                List<Integer> actionList = actionSeatMap.get(huseat);
                disBuilder.addAllSelfAct(actionList);
                seatPlayer.writeSocket(disBuilder.build());
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
    private void chiPengGang(CxMjPlayer player, List<CxMj> majiangs, int action) {
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
        CxMj disMajiang = null;
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
            sameCount = CxMjHelper.getMajiangCount(majiangs, majiangs.get(0).getVal());
        }
		// 如果是杠 后台来找出是明杠还是暗杠
        if (action == CxMjDisAction.action_minggang) {
            majiangs = CxMjHelper.getMajiangList(player.getHandMajiang(), majiangs.get(0).getVal());
            sameCount = majiangs.size();
            if (sameCount >= 4) {
				// 有4张一样的牌是暗杠
                action = CxMjDisAction.action_angang;
                majiangs=majiangs.subList(0,4);
            }
			// 其他是明杠
        }
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return;
        }
        boolean hasQGangHu = false;
        if (action == CxMjDisAction.action_peng) {
            boolean can = canPeng(player, majiangs, sameCount);
            if (!can) {
                return;
            }
        } else if (action == CxMjDisAction.action_angang) {
            boolean can = canAnGang(player, majiangs, sameCount);

            if (!can) {
                return;
            }
            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + CxMjHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
        } else if (action == CxMjDisAction.action_minggang) {
            boolean can = canMingGang(player, majiangs, sameCount);
            if (!can) {
                return;
            }
            ArrayList<CxMj> mjs = new ArrayList<>(majiangs);
            if (sameCount == 3) {
                mjs.add(disMajiang);
            }
            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + CxMjHelper.toMajiangStrs(mjs) + player.getExtraPlayLog());

			// 特殊处理一张牌明杠的时候别人可以胡
            if (sameCount == 1 && canGangHu()) {
                if (checkQGangHu(player, majiangs, action, sameCount)) {
                    hasQGangHu = true;
                    setNowDisCardSeat(player.getSeat());
					LogUtil.msg("tid:" + getId() + " " + player.getName() + "可以被抢杠胡！！");
                }
            }
			// 点杠可枪
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
            if ((action == CxMjDisAction.action_minggang && sameCount == 3)
                    || action == CxMjDisAction.action_peng || action == CxMjDisAction.action_chi) {
                if (action == CxMjDisAction.action_chi) {
					majiangs.add(1, disMajiang);// 吃的牌放第二位
                } else {
                    majiangs.add(disMajiang);
                }
                builder.setFromSeat(disCardSeat);
                seatMap.get(disCardSeat).removeOutPais(nowDisCardIds, action);
            }
        }
        switch (action){
            case CxMjDisAction.action_minggang:
            case CxMjDisAction.action_angang:
                gang(builder,player,majiangs,action);
                break;
            default:
                chiPeng(builder, player, majiangs, action, hasQGangHu, sameCount);
                break;
        }
    }

    /**
	 * 抢杠胡
	 *
	 * @param player
	 * @param majiangs
	 * @param action
	 * @return
	 */
    private boolean checkQGangHu(CxMjPlayer player, List<CxMj> majiangs, int action, int sameCount) {
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        Map<Integer, List<Integer>> huListMap = new HashMap<>();
        for (CxMjPlayer seatPlayer : seatMap.values()) {
            if (seatPlayer.getUserId() == player.getUserId()) {
                continue;
            }
			// 推送消息
            List<Integer> hu = seatPlayer.checkDisMajiang(majiangs.get(0), this.canGangHu() || dianGangKeHu == 1);
            if (!hu.isEmpty() && hu.get(0) == 1) {
                addActionSeat(seatPlayer.getSeat(), hu);
                huListMap.put(seatPlayer.getSeat(), hu);
            }
        }
		// 可以胡牌
        if (!huListMap.isEmpty()) {
            setMoGang(majiangs.get(0), new ArrayList<>(huListMap.keySet()), player, sameCount);
            buildPlayRes(builder, player, action, majiangs);
            for (Entry<Integer, List<Integer>> entry : huListMap.entrySet()) {
                PlayMajiangRes.Builder copy = builder.clone();
                CxMjPlayer seatPlayer = seatMap.get(entry.getKey());
                copy.addAllSelfAct(entry.getValue());
                seatPlayer.writeSocket(copy.build());
            }
            return true;
        }
        return false;
    }

    private void chiPeng(PlayMajiangRes.Builder builder, CxMjPlayer player, List<CxMj> majiangs, int action, boolean hasQGangHu, int sameCount) {
        player.addOutPais(majiangs, action, disCardSeat);
        buildPlayRes(builder, player, action, majiangs);
        List<Integer> actList = removeActionSeat(player.getSeat());
        if (!hasQGangHu) {
            clearActionSeatMap();
        }
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + CxMjHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
        setNowDisCardSeat(player.getSeat());
        List<Integer> list = player.checkMo(null);
        if (!list.isEmpty()&&list.size()>0) {
            list.set(0,0);
            if(list.contains(1))
                addActionSeat(player.getSeat(), list);
        }
        for (CxMjPlayer seatPlayer : seatMap.values()) {
			// 推送消息
            PlayMajiangRes.Builder copy = builder.clone();
            if (actionSeatMap.containsKey(seatPlayer.getSeat())) {
                copy.addAllSelfAct(actionSeatMap.get(seatPlayer.getSeat()));
            }
            seatPlayer.writeSocket(copy.build());
        }
        sendTingInfo(player);
        for (Player roomPlayer : roomPlayerMap.values()) {
            PlayMajiangRes.Builder copy = builder.clone();
            roomPlayer.writeSocket(copy.build());
        }
        logAction(player, action, majiangs, actList);
    }

    private void gang(PlayMajiangRes.Builder builder, CxMjPlayer player, List<CxMj> majiangs, int action) {
        player.addOutPais(majiangs, action, disCardSeat);
        buildPlayRes(builder, player, action, majiangs);
        removeActionSeat(player.getSeat());
        // 不是普通出牌
        setNowDisCardSeat(player.getSeat());
        player.getVirtualGang().clear();
        player.addGangNum();
        int []arr=new int[6];
        if(player.getGangNum()==1){
            List<Integer> copy1 = new ArrayList<>(player.getHandPais());
            copy1.add(1004);
            Map<Integer,List<Integer>> gangM1 = CxMjTool.checkGang(player.getGangNum(),
                    copy1, CxMjHelper.toMajiangValMap(player.getPeng()),1004);
            if(gangM1.size()>0){
                for (Map.Entry<Integer,List<Integer>> entry:gangM1.entrySet()) {
                    if(entry.getKey()==0){
                        arr[CxMjConstants.ACTION_INDEX_HU]=1;
                        player.setVirtualHu(entry.getKey());
                    }else {
                        arr[CxMjConstants.ACTION_INDEX_MINGGANG]=1;
                        List<Integer> virtualGang = player.getVirtualGang();
                        for(Integer id:entry.getValue()){
                            if(!virtualGang.contains(id)){
                                virtualGang.add(id);
                            }
                        }
                    }
                }
            }

            List<Integer> copy2 = new ArrayList<>(player.getHandPais());
            copy2.add(1005);
            Map<Integer,List<Integer>> gangM2 = CxMjTool.checkGang(player.getGangNum(),
                    copy2, CxMjHelper.toMajiangValMap(player.getPeng()),1005);
            if(gangM2.size()>0){
                for (Map.Entry<Integer,List<Integer>> entry:gangM2.entrySet()) {
                    if(entry.getKey()==0){
                        arr[CxMjConstants.ACTION_INDEX_HU]=1;
                        player.setVirtualHu(entry.getKey());
                    }else {
                        arr[CxMjConstants.ACTION_INDEX_MINGGANG]=1;
                        List<Integer> virtualGang = player.getVirtualGang();
                        for(Integer id:entry.getValue()){
                            if(!virtualGang.contains(id)){
                                virtualGang.add(id);
                            }
                        }
                    }
                }
            }
            player.sendGangMsg();
            if(gangM1.size()>0&&gangM2.size()>0){
                //需要传补牌消息
                for (CxMjPlayer seatPlayer : seatMap.values()) {
                    // 推送消息
                    seatPlayer.writeSocket(builder.build());
                }
                player.writeComMessage(WebSocketMsgType.res_code_cxmj_gangBu);
            }else {
                int nowBu;
                if(gangM2.size()==0){
                    nowBu=1004;
                }else {
                    nowBu=1005;
                }
                player.moMajiang(CxMj.getMajang(nowBu));
                buPai(nowBu,player);
                List<Integer> list=new ArrayList<>();
                for (int val : arr) {
                    list.add(val);
                }
                addActionSeat(player.getSeat(), list);
                for (CxMjPlayer seatPlayer : seatMap.values()) {
                    // 推送消息
                    PlayMajiangRes.Builder copy = builder.clone();
                    if (actionSeatMap.containsKey(seatPlayer.getSeat())) {
                        copy.addAllSelfAct(actionSeatMap.get(seatPlayer.getSeat()));
                    }
                    seatPlayer.writeSocket(copy.build());
                }
                if(arr[CxMjConstants.ACTION_INDEX_HU]==1&&arr[CxMjConstants.ACTION_INDEX_MINGGANG]==0){
                    player.setVirtualHu(nowBu);
                    hu(player,new ArrayList<>(),CxMjDisAction.action_hu);
                }
            }
        }else if(player.getGangNum()==2){
            List<Integer> copy = new ArrayList<>(player.getHandPais());
            int bu=player.getBuId();
            int nowBu;
            if(bu==1004){
                nowBu=1005;
            }else {
                nowBu=1004;
            }
            copy.add(nowBu);
            player.moMajiang(CxMj.getMajang(nowBu));
            buPai(nowBu,player);
            Map<Integer,List<Integer>> gangM = CxMjTool.checkGang(player.getGangNum(),
                    copy, CxMjHelper.toMajiangValMap(player.getPeng()),1004);
            if(gangM.size()>0)
                arr[CxMjConstants.ACTION_INDEX_HU]=1;
            List<Integer> list=new ArrayList<>();
            for (int val : arr) {
                list.add(val);
            }
            addActionSeat(player.getSeat(), list);
            for (CxMjPlayer seatPlayer : seatMap.values()) {
                // 推送消息
                PlayMajiangRes.Builder clone = builder.clone();
                if (actionSeatMap.containsKey(seatPlayer.getSeat())) {
                    clone.addAllSelfAct(actionSeatMap.get(seatPlayer.getSeat()));
                }
                seatPlayer.writeSocket(clone.build());
            }
            player.setVirtualHu(nowBu);
            hu(player,new ArrayList<>(),CxMjDisAction.action_hu);
        }
    }


    public void buPai(Integer id,CxMjPlayer player){
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + CxMjDisAction.action_moMjiang + "_" + id + player.getExtraPlayLog());
        MoMajiangRes.Builder res = MoMajiangRes.newBuilder();
        res.setUserId(player.getUserId() + "");
        res.setSeat(player.getSeat());
        res.setRemain(getLeftMajiangCount());
        player.setBuId(id);
        for (CxMjPlayer seat : seatMap.values()) {
            if (seat.getUserId() == player.getUserId()) {
                res.setMajiangId(id);
                seat.writeSocket(res.build());
            } else {
                seat.writeSocket(res.build());
            }
        }
    }

    /**
	 * 普通出牌
	 *
	 * @param player
	 * @param majiangs
	 * @param action
	 */
    private void chuPai(CxMjPlayer player, List<CxMj> majiangs, int action) {
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
		if (!actionSeatMap.isEmpty()) {// 出牌自动过掉手上操作
            guo(player, null, CxMjDisAction.action_pass);
        }
        if (!actionSeatMap.isEmpty()) {
            player.writeComMessage(WebSocketMsgType.res_code_mj_dis_err, action, majiangs.get(0).getId());
            return;
        }
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        buildPlayRes(builder, player, action, majiangs);
		// 普通出牌
        clearActionSeatMap();
        setNowDisCardSeat(calcNextSeat(player.getSeat()));
        if(majiangs.size()==1)
            lastId=majiangs.get(0).getId();
        recordDisMajiang(majiangs, player);
        player.addOutPais(majiangs, action, player.getSeat());
        logAction(player, action, majiangs, null);
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + CxMjHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
        for (CxMjPlayer p : seatMap.values()) {
            List<Integer> list;
            if (p.getUserId() != player.getUserId()) {
                list = p.checkDisMajiang(majiangs.get(0), this.canDianPao());
                if(list==null||list.isEmpty())
                    continue;
                if (list.contains(1)) {

                    addActionSeat(p.getSeat(), list);
                    p.setLastCheckTime(System.currentTimeMillis());
                    logChuPaiActList(p, majiangs.get(0), list);
                    if(list.get(CxMjConstants.ACTION_INDEX_MINGGANG)==1)
                        fangGangSeat=player.getSeat();
                }
            }
        }
//        for (int i = 0; i < l.size(); i++) {
//            hu(l.get(i),majiangs,CxMjDisAction.action_hu);
//        }
        sendDisMajiangAction(builder, player);

		// 给下一家发牌
        checkMo();
    }

    public List<Integer> getHuSeatByActionMap() {
        List<Integer> huList = new ArrayList<>();
        for (int seat : actionSeatMap.keySet()) {
            List<Integer> actionList = actionSeatMap.get(seat);
            if (actionList.get(CxMjConstants.ACTION_INDEX_HU) == 1 || actionList.get(CxMjConstants.ACTION_INDEX_ZIMO) == 1) {
				// 胡
                huList.add(seat);
            }
        }
        return huList;
    }

    private void sendDisMajiangAction(PlayMajiangRes.Builder builder, CxMjPlayer player) {
        for (CxMjPlayer seatPlayer : seatMap.values()) {
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
        for (Player roomPlayer : roomPlayerMap.values()) {
            PlayMajiangRes.Builder copy = builder.clone();
            roomPlayer.writeSocket(copy.build());
        }
    }

    public synchronized void playCommand(CxMjPlayer player, List<CxMj> majiangs, int action) {
        playCommand(player, majiangs, null, action);
    }

    /**
	 * 出牌
	 *
	 * @param player
	 * @param majiangs
	 * @param action
	 */
    public synchronized void playCommand(CxMjPlayer player, List<CxMj> majiangs, List<Integer> hucards, int action) {
        if (state != table_state.play) {
            return;
        }
		// 被人抢杠胡
        if (!moGangHuList.isEmpty()) {
            if (!moGangHuList.contains(player.getSeat())) {
				// 自己杠的时候被人抢杠胡了 不能做其他操作
                return;
            }
        }

        if (CxMjDisAction.action_hu == action) {
            hu(player, majiangs, action);
            return;
        }
		// 手上没有要出的麻将
        if (action != CxMjDisAction.action_minggang)
            if (!isHandCard(majiangs,player.getHandMajiang())) {
                return;
            }
        changeDisCardRound(1);
        if (action == CxMjDisAction.action_pass) {
            guo(player, majiangs, action);
        } else if (action != 0) {
            chiPengGang(player, majiangs, action);
        } else {
            chuPai(player, majiangs, action);
        }
		// 记录最后一次动作的时间
        setLastActionTime(TimeUtil.currentTimeMillis());
    }

    private boolean isHandCard(List<CxMj> majiangs,List<CxMj> handCards){
        for (CxMj mj:majiangs) {
            if(mj==CxMj.mj1004||mj==CxMj.mj1005)
                continue;
            if(!handCards.contains(mj))
                return false;
        }
        return true;
    }


    private void passMoHu(CxMjPlayer player, List<CxMj> majiangs, int action) {
        if (!moGangHuList.contains(player.getSeat())) {
            return;
        }

        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        buildPlayRes(builder, player, action, majiangs);
        builder.setSeat(nowDisCardSeat);
        removeActionSeat(player.getSeat());
        player.writeSocket(builder.build());
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + CxMjHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
        if (isCalcOver()) {
            calcOver();
            return;
        }
        player.setPassMajiangVal(nowDisCardIds.get(0).getVal());

        //CxMjPlayer moGangPlayer = seatMap.get(moMajiangSeat);
        if (moGangHuList.isEmpty()) {
            CxMjPlayer moGangPlayer = seatMap.get(getNowDisCardSeat());
            majiangs = new ArrayList<>();
            majiangs.add(moGang);
            if (moGangPlayer.getaGang().contains(moGang)) {
                calcPoint(moGangPlayer, CxMjDisAction.action_angang, 4, majiangs);
            } else {
                calcPoint(moGangPlayer, CxMjDisAction.action_minggang, moGangSameCount > 0 ? moGangSameCount : 1, majiangs);
            }

            moMajiang(moGangPlayer, true);
//			builder = PlayMajiangRes.newBuilder();
//			chiPengGang(builder, moGangPlayer, majiangs, CxMjDisAction.action_minggang, false);
        }

    }

    /**
     * pass
     *
     * @param player
     * @param majiangs
     * @param action
     */
    private void guo(CxMjPlayer player, List<CxMj> majiangs, int action) {
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
        if(player.getGangNum()>0){
            LogUtil.msg("杠后不能过胡！");
            player.writeErrMsg("杠后不能过！");
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
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + CxMjHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
        if (isCalcOver()) {
            calcOver();
            return;
        }
        if (removeActionList.get(0) == 1 && disCardSeat != player.getSeat() && nowDisCardIds.size() == 1) {
			// 漏炮
            player.setPassMajiangVal(nowDisCardIds.get(0).getVal());
        }
        logAction(player, action, majiangs, removeActionList);
        if (!actionSeatMap.isEmpty()) {
            CxMjPlayer disMajiangPlayer = seatMap.get(disCardSeat);
            PlayMajiangRes.Builder disBuilder = PlayMajiangRes.newBuilder();
            buildPlayRes(disBuilder, disMajiangPlayer, 0, null);
            for (int seat : actionSeatMap.keySet()) {
                List<Integer> actionList = actionSeatMap.get(seat);
                PlayMajiangRes.Builder copy = disBuilder.clone();
                copy.addAllSelfAct(new ArrayList<>());
                if (actionList != null && !tempActionMap.containsKey(seat) && !huConfirmList.contains(seat)) {
                    copy.addAllSelfAct(actionList);
                    CxMjPlayer seatPlayer = seatMap.get(seat);
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

    private void calcPoint(CxMjPlayer player, int action, int sameCount, List<CxMj> majiangs) {
        int lostPoint = 0;
        int getPoint = 0;
        int[] seatPointArr = new int[getMaxPlayerCount() + 1];
        if (action == CxMjDisAction.action_peng) {
            return;

        } else if (action == CxMjDisAction.action_angang) {
			// 暗杠相当于自摸每人出2分
            lostPoint = -2;
            getPoint = 2 * (getMaxPlayerCount() - 1);

        } else if (action == CxMjDisAction.action_minggang) {
            if (sameCount == 1) {
				// 碰牌之后再抓一个牌每人出1分
				// 放杠的人出3分

                if (player.isPassGang(majiangs.get(0))) {
					// 特殊处理 可以碰可以杠的牌 选择了碰 再杠不算分
                    return;
                }
                lostPoint = -1;
                getPoint = 1 * (getMaxPlayerCount() - 1);
            }
			// 放杠
            if (sameCount == 3) {
                int bei=diFen;
                CxMjPlayer disPlayer = seatMap.get(disCardSeat);
//                disPlayer.changeLostPoint(-bei);
                seatPointArr[disPlayer.getSeat()] = -bei;
//                player.changeLostPoint(bei);
                seatPointArr[player.getSeat()] = bei;
            }
        }
        getPoint=getPoint*diFen;
        lostPoint=lostPoint*diFen;
        if (lostPoint != 0) {
            for (CxMjPlayer seat : seatMap.values()) {
                if (seat.getUserId() == player.getUserId()) {
//                    player.changeLostPoint(getPoint);
                    seatPointArr[player.getSeat()] = getPoint;
                } else {
//                    seat.changeLostPoint(lostPoint);
                    seatPointArr[seat.getSeat()] = lostPoint;
                }
            }
        }
//        for (CxMjPlayer p : seatMap.values()) {
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

        if (action != CxMjDisAction.action_chi) {
//            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + CxMjHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog() + "_" + seatPointStr);
        }
    }

    private void recordDisMajiang(List<CxMj> majiangs, CxMjPlayer player) {
        setNowDisCardIds(majiangs);
        // changeDisCardRound(1);
        setDisCardSeat(player.getSeat());
    }


    public void setNowDisCardIds(List<CxMj> nowDisCardIds) {
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
                CxMjPlayer player = seatMap.get(seat);
                if (player != null && player.isRobot()) {
					// 如果是机器人可以直接决定
                    List<Integer> actionList = actionSeatMap.get(seat);
                    if (actionList == null) {
                        continue;
                    }
                    List<CxMj> list = new ArrayList<>();
                    if (!nowDisCardIds.isEmpty()) {
                        list = CxMjQipaiTool.getVal(player.getHandMajiang(), nowDisCardIds.get(0).getVal());
                    }
                    if (actionList.get(CxMjConstants.ACTION_INDEX_HU) == 1 || actionList.get(CxMjConstants.ACTION_INDEX_ZIMO) == 1) {
						// 胡
                        playCommand(player, new ArrayList<CxMj>(), CxMjDisAction.action_hu);

                    } else if (actionList.get(CxMjConstants.ACTION_INDEX_ANGANG) == 1) {
                        playCommand(player, list, CxMjDisAction.action_angang);

                    } else if (actionList.get(CxMjConstants.ACTION_INDEX_MINGGANG) == 1) {
                        playCommand(player, list, CxMjDisAction.action_minggang);

                    } else if (actionList.get(CxMjConstants.ACTION_INDEX_PENG) == 1) {
                        playCommand(player, list, CxMjDisAction.action_peng);
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
            CxMjPlayer next = seatMap.get(nextseat);
            if (next != null && next.isRobot()) {
                List<Integer> actionList = actionSeatMap.get(next.getSeat());
                if (actionList != null) {
                    List<CxMj> list = null;
                    if (actionList.get(CxMjConstants.ACTION_INDEX_HU) == 1 || actionList.get(CxMjConstants.ACTION_INDEX_ZIMO) == 1) {
						// 胡
                        playCommand(next, new ArrayList<CxMj>(), CxMjDisAction.action_hu);
                    } else if (actionList.get(CxMjConstants.ACTION_INDEX_ANGANG) == 1) {
						// 机器人暗杠
                        Map<Integer, Integer> handMap = CxMjHelper.toMajiangValMap(next.getHandMajiang());
                        for (Entry<Integer, Integer> entry : handMap.entrySet()) {
                            if (entry.getValue() == 4) {
								// 可以暗杠
                                list = CxMjHelper.getMajiangList(next.getHandMajiang(), entry.getKey());
                            }
                        }
                        playCommand(next, list, CxMjDisAction.action_angang);

                    } else if (actionList.get(CxMjConstants.ACTION_INDEX_MINGGANG) == 1) {
                        Map<Integer, Integer> pengMap = CxMjHelper.toMajiangValMap(next.getPeng());
                        for (CxMj handMajiang : next.getHandMajiang()) {
                            if (pengMap.containsKey(handMajiang.getVal())) {
								// 有碰过
                                list = new ArrayList<>();
                                list.add(handMajiang);
                                playCommand(next, list, CxMjDisAction.action_minggang);
                                break;
                            }
                        }

                    } else if (actionList.get(CxMjConstants.ACTION_INDEX_PENG) == 1) {
                        playCommand(next, list, CxMjDisAction.action_peng);
                    }
                } else {
                    List<Integer> handMajiangs = new ArrayList<>(next.getHandPais());
                    CxMjQipaiTool.dropHongzhongVal(handMajiangs);
                    int maJiangId = CxMjRobotAI.getInstance().outPaiHandle(0, handMajiangs, new ArrayList<Integer>());
                    List<CxMj> majiangList = CxMjHelper.toMajiang(Arrays.asList(maJiangId));
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

//        List<Integer> copy = CxMjConstants.getMajiangList(getMaxPlayerCount());
        List<Integer> copy = CxMjConstants.getMajiangList(3);
        addPlayLog(copy.size() + "");
        List<List<CxMj>> list = null;
        if (zp != null&&zp.size()!=0) {
            list = CxMjTool.fapai(copy, getMaxPlayerCount(), zp);
        } else {
            list = CxMjTool.fapai(copy, getMaxPlayerCount());
        }
        int i = 1;
        for (CxMjPlayer player : seatMap.values()) {
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
    public void setLeftMajiangs(List<CxMj> leftMajiangs) {
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
    public CxMj getLeftMajiang() {
        if (this.leftMajiangs.size() > 0) {
            CxMj majiang = this.leftMajiangs.remove(0);
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
        res.addExt(canDianPao);            //2
        res.addExt(hu7dui);                 //4
        res.addExt(isAutoPlay);             //5
        res.addExt(qiangGangHu);            //6
        res.addExt(qiangGangHuBaoSanJia);   //7
        res.addExt(dianGangKeHu);           //11
        res.addExt(diFen);                  //12
        res.addStrExt(StringUtil.implode(moTailPai, ","));      //0

        res.setMasterId(getMasterId() + "");
        if (leftMajiangs != null) {
            res.setRemain(leftMajiangs.size());
        } else {
            res.setRemain(0);
        }
        res.setDealDice(dealDice);
        List<PlayerInTableRes> players = new ArrayList<>();
        for (CxMjPlayer player : playerMap.values()) {
            PlayerInTableRes.Builder playerRes = player.buildPlayInTableInfo(isrecover);
            if (player.getUserId() == userId) {
                playerRes.addAllHandCardIds(player.getHandPais());
            }
            if (player.getSeat() == disCardSeat && nowDisCardIds != null && moGangHuList.isEmpty()) {
                playerRes.addAllOutCardIds(CxMjHelper.toMajiangIds(nowDisCardIds));
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
        } else if (!moGangHuList.isEmpty()) {
            for (CxMjPlayer player : seatMap.values()) {
                if (player.getmGang() != null && player.getmGang().contains(moGang)) {
                    res.setNextSeat(player.getSeat());
                    break;
                }
            }
        }
        res.setRenshu(getMaxPlayerCount());
        res.setLastWinSeat(getLastWinSeat());
        res.addTimeOut((int) CxMjConstants.AUTO_TIMEOUT);
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
        clearMoGang();
        setDealDice(0);
        clearMoTailPai();
        paoHuSeat.clear();
        readyTime=0;
        finishFapai=0;
        lastId=0;
        fangGangSeat=0;
    }

    public List<Integer> removeActionSeat(int seat) {
        List<Integer> actionList = actionSeatMap.remove(seat);
        if (moGangHuList.contains(seat)) {
            removeMoGang(seat);
        }
        saveActionSeatMap();
        return actionList;
    }

    public void addActionSeat(int seat, List<Integer> actionlist) {
        actionSeatMap.put(seat, actionlist);
        CxMjPlayer player = seatMap.get(seat);
        addPlayLog(disCardRound + "_" + seat + "_" + CxMjDisAction.action_hasAction + "_" + StringUtil.implode(actionlist) + player.getExtraPlayLog());
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
            nowDisCardIds = CxMjHelper.toMajiang(StringUtil.explodeToIntList(info.getNowDisCardIds()));
        }

        if (!StringUtils.isBlank(info.getLeftPais())) {
            try {
                leftMajiangs = CxMjHelper.toMajiang(StringUtil.explodeToIntList(info.getLeftPais()));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }




    private Map<Integer, CxMjTempAction> loadTempActionMap(String json) {
        Map<Integer, CxMjTempAction> map = new ConcurrentHashMap<>();
        if (json == null || json.isEmpty())
            return map;
        JSONArray jsonArray = JSONArray.parseArray(json);
        for (Object val : jsonArray) {
            String str = val.toString();
            CxMjTempAction tempAction = new CxMjTempAction();
            tempAction.initData(str);
            map.put(tempAction.getSeat(), tempAction);
        }
        return map;
    }

    /**
	 * 检查优先度 如果同时出现一个事件，按出牌座位顺序优先
	 */
    private boolean checkAction(CxMjPlayer player, List<CxMj> cardList, List<Integer> hucards, int action) {
		boolean canAction = checkCanAction(player, action);// 是否优先级最高 能执行操作
		if (!canAction) {// 不能操作时 存入临时操作
            int seat = player.getSeat();
            tempActionMap.put(seat, new CxMjTempAction(seat, action, cardList, hucards));
			// 玩家都已选择自己的临时操作后 选取优先级最高
            if (tempActionMap.size() == actionSeatMap.size()) {
                int maxAction = Integer.MAX_VALUE;
                int maxSeat = 0;
                Map<Integer, Integer> prioritySeats = new HashMap<>();
                int maxActionSize = 0;
                for (CxMjTempAction temp : tempActionMap.values()) {
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
                CxMjPlayer tempPlayer = seatMap.get(maxSeat);
                List<CxMj> tempCardList = tempActionMap.get(maxSeat).getCardList();
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
    private void refreshTempAction(CxMjPlayer player) {
        tempActionMap.remove(player.getSeat());
		Map<Integer, Integer> prioritySeats = new HashMap<>();// 各位置优先操作
        for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
            int seat = entry.getKey();
            List<Integer> actionList = entry.getValue();
            List<Integer> list = CxMjDisAction.parseToDisActionList(actionList);
            int priorityAction = CxMjDisAction.getMaxPriorityAction(list);
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
        Iterator<CxMjTempAction> iterator = tempActionMap.values().iterator();
        while (iterator.hasNext()) {
            CxMjTempAction tempAction = iterator.next();
            if (tempAction.getSeat() == maxPrioritySeat) {
                int action = tempAction.getAction();
                List<CxMj> tempCardList = tempAction.getCardList();
                List<Integer> tempHuCards = tempAction.getHucards();
                CxMjPlayer tempPlayer = seatMap.get(tempAction.getSeat());
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
    public boolean checkCanAction(CxMjPlayer player, int action) {
		// 优先度为胡杠补碰吃
        List<Integer> stopActionList = CxMjDisAction.findPriorityAction(action);
        for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
            if (player.getSeat() != entry.getKey()) {
				// 别人
                boolean can = CxMjDisAction.canDisMajiang(stopActionList, entry.getValue());
                if (!can) {
                    return false;
                }
                List<Integer> disActionList = CxMjDisAction.parseToDisActionList(entry.getValue());
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
    private boolean canPeng(CxMjPlayer player, List<CxMj> majiangs, int sameCount) {
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
    private boolean canAnGang(CxMjPlayer player, List<CxMj> majiangs, int sameCount) {
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
    private boolean canMingGang(CxMjPlayer player, List<CxMj> majiangs, int sameCount) {
        List<CxMj> handMajiangs = player.getHandMajiang();
        List<Integer> pengList = CxMjHelper.toMajiangVals(player.getPeng());
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

    /**
	 * 摸杠别人可以胡
	 *
	 * @param moGang
	 * @param moGangHuList
	 */
    public void setMoGang(CxMj moGang, List<Integer> moGangHuList, CxMjPlayer player, int sameCount) {
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
        if (getCanDianPao() == 1) {
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
            for (CxMjPlayer player : seatMap.values()) {
                if (player.getTotalPoint() > 0 && player.getTotalPoint() < jiaBeiFen) {
                    jiaBeiPoint += player.getTotalPoint() * (jiaBeiShu - 1);
                    player.setTotalPoint(player.getTotalPoint() * jiaBeiShu);
                } else if (player.getTotalPoint() < 0) {
                    loserCount++;
                }
            }
            if (jiaBeiPoint > 0) {
                for (CxMjPlayer player : seatMap.values()) {
                    if (player.getTotalPoint() < 0) {
                        player.setTotalPoint(player.getTotalPoint() - (jiaBeiPoint / loserCount));
                    }
                }
            }
        }

        //大结算低于below分+belowAdd分
        if(over&&belowAdd>0&&playerMap.size()==2){
            for (CxMjPlayer player : seatMap.values()) {
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
        for (CxMjPlayer player : seatMap.values()) {
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
                    CxMj huMajiang = nowDisCardIds.get(0);
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
                if(huConfirmList.isEmpty())
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
            for (CxMjPlayer player : seatMap.values()) {
                if (player.getWinLoseCredit() > dyjCredit) {
                    dyjCredit = player.getWinLoseCredit();
                }
            }
            for (ClosingMjPlayerInfoRes.Builder builder : builderList) {
                CxMjPlayer player = seatMap.get(builder.getSeat());
                calcCommissionCredit(player, dyjCredit);
                builder.setWinLoseCredit(player.getWinLoseCredit());
                builder.setCommissionCredit(player.getCommissionCredit());
            }
        } else if (isGroupTableGoldRoom()) {
            // -----------亲友圈金币场---------------------------------
            for (CxMjPlayer player : seatMap.values()) {
                player.setWinGold(player.getTotalPoint() * gtgDifen);
            }
            calcGroupTableGoldRoomWinLimit();
            for (ClosingMjPlayerInfoRes.Builder builder : builderList) {
                CxMjPlayer player = seatMap.get(builder.getSeat());
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
        res.addAllLeftCards(CxMjHelper.toMajiangIds(leftMajiangs));
        res.setCatchBirdSeat(catchBirdSeat);
        res.addAllIntParams(getIntParams());
        for (CxMjPlayer player : seatMap.values()) {
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
        ext.add(canDianPao + "");                        //5
        ext.add(lastWinSeat + "");                            //6
        ext.add(isAutoPlay + "");                        //7
        ext.add(diFen + "");                            //8
        ext.add(isLiuJu() + "");                        //9
        ext.add(over+"");                               //10
        return ext;
    }

    @Override
    public void sendAccountsMsg() {
        ClosingMjInfoRes.Builder builder = sendAccountsMsg(true, false, null, null, null, null, 0, true);
        saveLog(true, 0l, builder.build());
    }

    @Override
    public Class<? extends Player> getPlayerClass() {
        return CxMjPlayer.class;
    }

    @Override
    public int getWanFa() {
        return getPlayType();
    }

//	@Override
//	public boolean isTest() {
//		return super.isTest() && CxMjConstants.isTest;
//	}

    @Override
    public void checkReconnect(Player player) {
        ((CxMjPlayer) player).checkAutoPlay(0, true);
        if (state == table_state.play) {
            CxMjPlayer player1 = (CxMjPlayer) player;
            if (player1.getHandPais() != null && player1.getHandPais().size() > 0) {
                sendTingInfo(player1);
            }
        }
        if(((CxMjPlayer) player).getGangNum()==1&&actionSeatMap.isEmpty())
            player.writeComMessage(WebSocketMsgType.res_code_cxmj_gangBu);
//        sendPiaoReconnect(player);
        ((CxMjPlayer) player).sendGangMsg();
    }

    private void sendPiaoReconnect(Player player){
        if(piaoFenType==0||maxPlayerCount!=getPlayerCount())
            return;
        int count=0;
        for(Map.Entry<Integer, CxMjPlayer> entry:seatMap.entrySet()){
            player_state state = entry.getValue().getState();
            if(state==player_state.play||state==player_state.ready)
                count++;
        }
        if(count!=maxPlayerCount)
            return;

        for(Map.Entry<Integer, CxMjPlayer> entry:seatMap.entrySet()){
            CxMjPlayer p = entry.getValue();
            if(p.getUserId()==player.getUserId()){
                if(p.getPiaoFen()==-1){
//                    player.writeComMessage(WebSocketMsgType.res_code_cxmj_piaofen);
                    continue;
                }
            }else {
                List<Integer> l=new ArrayList<>();
                l.add((int)p.getUserId());
                l.add(p.getPiaoFen());
//                player.writeComMessage(WebSocketMsgType.res_code_cxmj_broadcast_piaofen, l);
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
            for (CxMjPlayer player : seatMap.values()) {
                if (player.getLastCheckTime() > 0) {
                    player.setLastCheckTime(player.getLastCheckTime() + 1 * 1000);
                }
            }
            return;
        }

        if (isAutoPlayOff()) {
            // 托管关闭
            for (int seat : seatMap.keySet()) {
                CxMjPlayer player = seatMap.get(seat);
                player.setAutoPlay(false, false);
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
            for (CxMjPlayer player : seatMap.values()) {
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
                    CxMjPlayer player = seatMap.get(seat);
                    if (player == null) {
                        continue;
                    }
                    if (!player.checkAutoPlay(2, false)) {
                        continue;
                    }
                    playCommand(player, new ArrayList<>(), CxMjDisAction.action_hu);
                }
                return;
            } else {
                int action = 0, seat = 0;
                for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
                    seat = entry.getKey();
                    List<Integer> actList = CxMjDisAction.parseToDisActionList(entry.getValue());
                    if (actList == null) {
                        continue;
                    }

                    action = CxMjDisAction.getAutoMaxPriorityAction(actList);
                    CxMjPlayer player = seatMap.get(seat);
                    if (!player.checkAutoPlay(0, false)) {
                        continue;
                    }
                    boolean chuPai = false;
                    if (player.isAlreadyMoMajiang()) {
                        chuPai = true;
                    }
                    if(action == CxMjDisAction.action_minggang||action == CxMjDisAction.action_angang){
                        List<Integer> virtualGang = player.getVirtualGang();
                        List<CxMj> mjs = CxMjHelper.getMajiangList(player.getHandMajiang(), CxMj.getMajang(virtualGang.get(0)).getVal());
                        playCommand(player, mjs, CxMjDisAction.action_minggang);
                    }else if (action == CxMjDisAction.action_peng) {
                        if (player.isAutoPlaySelf()) {
							// 自己开启托管直接过
                            playCommand(player, new ArrayList<>(), CxMjDisAction.action_pass);
                            if (chuPai) {
                                autoChuPai(player);
                            }
                        } else {
                            if (nowDisCardIds != null && !nowDisCardIds.isEmpty()) {
                                CxMj mj = nowDisCardIds.get(0);
                                List<CxMj> mjList = new ArrayList<>();
                                for (CxMj handMj : player.getHandMajiang()) {
                                    if (handMj.getVal() == mj.getVal()) {
                                        mjList.add(handMj);
                                        if (mjList.size() == 2) {
                                            break;
                                        }
                                    }
                                }
                                playCommand(player, mjList, CxMjDisAction.action_peng);
//							autoChuPai(player);
                            }
                        }
                    } else {
                        playCommand(player, new ArrayList<>(), CxMjDisAction.action_pass);
                        if (chuPai) {
                            autoChuPai(player);
                        }
                    }
                }
            }
        } else {
            boolean gangBu=false;
            CxMjPlayer p=null;
            for(CxMjPlayer player:seatMap.values()){
                if(player.getGangNum()==1){
                    gangBu=true;
                    p=player;
                    break;
                }
            }
            if(gangBu&&p!=null&&p.isAutoPlay()){
                gangBuPai(1005,p);
            }else {
                CxMjPlayer player = seatMap.get(nowDisCardSeat);
                if (player == null || !player.checkAutoPlay(0, false)) {
                    return;
                }
                autoChuPai(player);
            }
        }
    }

    public void autoChuPai(CxMjPlayer player) {

		// CxMjQipaiTool.dropHongzhongVal(handMajiangs);红中麻将要去掉红中
//					int mjId = CxMjRobotAI.getInstance().outPaiHandle(0, handMjIds, new ArrayList<>());
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
            List<CxMj> mjList = CxMjHelper.toMajiang(Arrays.asList(mjId));
            playCommand(player, mjList, CxMjDisAction.action_chupai);
        }
    }


    @Override
    public void createTable(Player player, int play, int bureauCount, Object... objects) throws Exception {
    }


    public synchronized void piaoFen(CxMjPlayer player, int fen){
        if (piaoFenType<=3||player.getPiaoFen()!=-1)
            return;
        if(fen>3||fen<0)
            return;
        if(piaoFenType==5&&playBureau>1)
            return;
        player.setPiaoFen(fen);
        StringBuilder sb = new StringBuilder("cxmj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append("piaoFen").append("|").append(fen);
        LogUtil.msgLog.info(sb.toString());
        int confirmTime=0;
        for (Map.Entry<Integer, CxMjPlayer> entry : seatMap.entrySet()) {
//            entry.getValue().writeComMessage(WebSocketMsgType.res_code_cxmj_broadcast_piaofen, (int)player.getUserId(),player.getPiaoFen());
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

    public static final List<Integer> wanfaList = Arrays.asList(GameUtil.game_type_cxmj);

    public static void loadWanfaTables(Class<? extends BaseTable> cls) {
        for (Integer integer : wanfaList) {
            TableManager.wanfaTableTypesPut(integer, cls);
        }
    }

    public void logFaPaiTable() {
        StringBuilder sb = new StringBuilder();
        sb.append("CxMj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append("faPai");
        sb.append("|").append(playType);
        sb.append("|").append(maxPlayerCount);
        sb.append("|").append(getPayType());
        sb.append("|").append(lastWinSeat);
        LogUtil.msg(sb.toString());
    }

    public void logFaPaiPlayer(CxMjPlayer player, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("CxMj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append("faPai");
        sb.append("|").append(player.getHandMajiang());
        sb.append("|").append(actListToString(actList));
        LogUtil.msg(sb.toString());
    }

    public void logAction(CxMjPlayer player, int action, List<CxMj> mjs, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("CxMj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        String actStr = "unKnown-" + action;
        if (action == CxMjDisAction.action_peng) {
            actStr = "peng";
        } else if (action == CxMjDisAction.action_minggang) {
            actStr = "baoTing";
        } else if (action == CxMjDisAction.action_chupai) {
            actStr = "chuPai";
        } else if (action == CxMjDisAction.action_pass) {
            actStr = "guo";
        } else if (action == CxMjDisAction.action_angang) {
            actStr = "anGang";
        } else if (action == CxMjDisAction.action_chi) {
            actStr = "chi";
        }
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append(player.isAutoPlaySelf() ? 1 : 0);
        sb.append("|").append(actStr);
        sb.append("|").append(mjs);
        sb.append("|").append(actListToString(actList));
        LogUtil.msg(sb.toString());
    }

    public void logMoMj(CxMjPlayer player, CxMj mj, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("CxMj");
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

    public void logChuPaiActList(CxMjPlayer player, CxMj mj, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("CxMj");
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

    public void logActionHu(CxMjPlayer player, List<CxMj> mjs, String daHuNames) {
        StringBuilder sb = new StringBuilder();
        sb.append("CxMj");
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
                if (i == CxMjConstants.ACTION_INDEX_HU) {
                    sb.append("hu");
                } else if (i == CxMjConstants.ACTION_INDEX_PENG) {
                    sb.append("peng");
                } else if (i == CxMjConstants.ACTION_INDEX_MINGGANG) {
                    sb.append("mingGang");
                } else if (i == CxMjConstants.ACTION_INDEX_ANGANG) {
                    sb.append("anGang");
                } else if (i == CxMjConstants.ACTION_INDEX_CHI) {
                    sb.append("chi");
                } else if (i == CxMjConstants.ACTION_INDEX_ZIMO) {
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


    public void sendTingInfo(CxMjPlayer player) {
        if (player.isAlreadyMoMajiang()) {
            if (actionSeatMap.containsKey(player.getSeat())) {
                return;
            }
            DaPaiTingPaiRes.Builder tingInfo = DaPaiTingPaiRes.newBuilder();
            List<CxMj> cards = new ArrayList<>(player.getHandMajiang());
            Map<Integer, List<Integer>> daTing = TingTool.getDaTing(CxMjHelper.toMajiangIds(cards));
            for(Map.Entry<Integer, List<Integer>> entry : daTing.entrySet()){
                DaPaiTingPaiInfo.Builder ting = DaPaiTingPaiInfo.newBuilder();
                ting.addAllTingMajiangIds(entry.getValue());
                ting.setMajiangId(entry.getKey());
                tingInfo.addInfo(ting);
            }
            if(daTing.size()>0)
                player.writeSocket(tingInfo.build());

        } else {
            List<CxMj> cards = new ArrayList<>(player.getHandMajiang());
            TingPaiRes.Builder ting = TingPaiRes.newBuilder();
            List<Integer> tingP = TingTool.getTing(CxMjHelper.toMajiangIds(cards));
            ting.addAllMajiangIds(tingP);
            if(tingP.size()>0)
                player.writeSocket(ting.build());
        }
    }

    public String getTableMsg() {
        Map<String, Object> json = new HashMap<>();
		json.put("wanFa", "转转麻将");
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
		return "转转麻将";
    }
}
