package com.sy599.game.qipai.hzmj.bean;

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

import com.sy599.game.common.bean.CreateTableInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
import com.sy599.game.db.bean.group.GroupTable;
import com.sy599.game.db.dao.TableDao;
import com.sy599.game.db.dao.TableLogDao;
import com.sy599.game.db.dao.UserDao;
import com.sy599.game.db.dao.group.GroupDao;
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
import com.sy599.game.qipai.hzmj.constant.HzMjConstants;
import com.sy599.game.qipai.hzmj.rule.HzMj;
import com.sy599.game.qipai.hzmj.rule.HzMjRobotAI;
import com.sy599.game.qipai.hzmj.tool.HzMjHelper;
import com.sy599.game.qipai.hzmj.tool.HzMjQipaiTool;
import com.sy599.game.qipai.hzmj.tool.HzMjResTool;
import com.sy599.game.qipai.hzmj.tool.HzMjTool;
import com.sy599.game.qipai.hzmj.tool.hulib.util.HuUtil;
import com.sy599.game.udplog.UdpLogger;
import com.sy599.game.util.DataMapUtil;
import com.sy599.game.util.GameUtil;
import com.sy599.game.util.JacksonUtil;
import com.sy599.game.util.JsonWrapper;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.PayConfigUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.util.StringUtil;
import com.sy599.game.util.TimeUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;


public class HzMjTable extends BaseTable {
    /**
     * 当前打出的牌
     */
    private List<HzMj> nowDisCardIds = new ArrayList<>();
    protected List<Integer> dices;
    private Map<Integer, List<Integer>> actionSeatMap = new ConcurrentHashMap<>();
    /**
     * 玩家位置对应临时操作
     * 当同时存在多个可做的操作时
     * 1当优先级最高的玩家最先操作 则清理所有操作提示 并执行该操作
     * 2当操作优先级低的玩家先选择操作，则玩家先将所做操作存入临时操作中 当玩家优先级最高的玩家操作后 在判断临时操作是否可执行并执行
     */
    private Map<Integer, HzMjTempAction> tempActionMap = new ConcurrentHashMap<>();
    private int maxPlayerCount = 4;
    private List<HzMj> leftMajiangs = new ArrayList<>();
    /*** 玩家map */
    private Map<Long, HzMjPlayer> playerMap = new ConcurrentHashMap<Long, HzMjPlayer>();
    /*** 座位对应的玩家 */
    private Map<Integer, HzMjPlayer> seatMap = new ConcurrentHashMap<Integer, HzMjPlayer>();
    private List<Integer> huConfirmList = new ArrayList<>();//胡牌数组
    /**
     * 摸麻将的seat
     */
    private int moMajiangSeat;
    /**
     * 摸杠的麻将
     */
    private HzMj moGang;
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
    private int dianPaoZimo;
    /**
     * 选了庄闲就算分，不选就不额外算分
     **/
    private int isCalcBanker;
    /**
     * 抓鸟个数
     **/
    private int birdNum;
    /**
     * 胡7对
     **/
    private int hu7dui;

    private int isAutoPlay;//托管时间

    /**
     * 胡几奖几
     **/
    private int zhuaJiJiangJi;
    /**
     * 一鸟全中开关
     **/
    private int yiNiaoQuanZhong;
    /**
     * 鸟加分
     **/
    private int niaoFen;
    /**
     * 无红中加鸟
     **/
    private int wuHongZhongJiaNiao;
    
    /**
     * 无红中加倍
     **/
    private int wuHongZhongJiaBei;
    /**
     * 抢杠胡开关
     **/
    private int qiangGangHu;
    /**
     * 点杠可抢杠胡
     */
    private int dianGangKeHu;
    /**
     * 抢杠胡包三家
     **/
    private int qiangGangHuBaoSanJia;
    /**
     * 有炮必胡
     **/
    private int youPaoBiHu;
    /**
     * 0不飘分，1自由飘分，2首局定飘，3飘1分，4飘2分，5飘3分
     **/
    private int kePiao;
    /**
     * 159中鸟
     **/
    private int zhongNiao159;

    /**
     * 不能一炮多响
     **/
    private int noYiPaoDuoX;

    /**
     * 中途解散算杠分
     **/
    private int ztJsGangFen;

    /**
     * 底分
     **/
    private int diFen;

    private int tableStatus;//特殊状态 1飘分

    //是否加倍：0否，1是
    private int jiaBei;
    //加倍分数：低于xx分进行加倍
    private int jiaBeiFen;
    //加倍倍数：翻几倍
    private int jiaBeiShu;
    
    /**无红中自摸*/
    private int noHzZimo;
    /**无红中放炮*/
    private int noHzPao;
    /**无红中七对，碰碰胡，清一色*/
    private int noHzQPQ;
    
    /**托管1：单局，2：全局*/
    private int autoPlayGlob;
    private int autoTableCount;

    /** 自摸分：默认算2分*/
    private int ziMoFen = 2;

    /*** 摸屁股的座标号*/
    private List<Integer> moTailPai = new ArrayList<>();

    private int readyTime = 0 ;
    
    
    /** 金鸟：不中算全中*/
    private int buzhongzhong = 0;
    /** 4红中胡牌*/
    private int sihongzHu = 0;
    
    private int baahongz = 0;
    
    /**
     * 自摸必胡
     **/
    private int zimoBihu;
    //低于below加分
    private int belowAdd=0;
    private int below=0;
    
    private int hongzhongBJP = 0;
    
    

    public int getDealDice() {
        return dealDice;
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

    public int getDianPaoZimo() {
        return dianPaoZimo;
    }

    public void setDianPaoZimo(int dianPaoZimo) {
        this.dianPaoZimo = dianPaoZimo;
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
        List<Integer> winList = new ArrayList<>(huConfirmList);
        boolean selfMo = false;
        int[] birdMjIds = null;//抓的鸟牌Id
        int[] seatBirds = null;//中鸟的位置
        Map<Integer, Integer> seatBirdMap = new HashMap<>();//位置,中鸟数
        int catchBirdSeat = lastWinSeat;
        int huSeat = winList.size() > 0 ? winList.get(0) : 0;
        if (winList.size() == 0 && leftMajiangs.isEmpty()) {
            // 流局
        } else {
            // 先判断是自摸还是放炮
            if (winList.size() == 1 && seatMap.get(winList.get(0)).getHandMajiang().size() % 3 == 2 && winList.get(0) == moMajiangSeat) {
                selfMo = true;
            }
            if(nowDisCardIds==null ||nowDisCardIds.size()==0) {
            	 selfMo = true;
            }
            int birdNum = getBirdNum();
            //胡几奖几
            if (zhuaJiJiangJi == 1) {
                HzMj huMj = null;
                if (selfMo) {
                    huMj = seatMap.get(winList.get(0)).getLastMoMajiang();
                } else {
                    huMj = !nowDisCardIds.isEmpty() ? nowDisCardIds.get(0) : null;
                }
                birdNum = huMj.getVal() == HzMj.getHongZhongVal() ? 10 : huMj.getVal() % 10;
            }
            //无红中加鸟
            if (wuHongZhongJiaNiao > 0) {
                int startSeat = winList.size() > 1 ? disCardSeat : winList.get(0);
                int hongZhongCount = HzMjQipaiTool.getMajiangCount(seatMap.get(startSeat).getHandMajiang(), HzMj.getHongZhongVal());
                if (hongZhongCount == 0) {
                    birdNum += wuHongZhongJiaNiao;
                }
            }

            if (birdNum > 0 && !leftMajiangs.isEmpty()) {
                // 先砸鸟
                birdMjIds = zhuaNiao(birdNum);
                // 抓到鸟的座位
                boolean isDuoXiang = winList.size() > 1;
                for (int winSeat : winList) {
                    catchBirdSeat = winSeat;
                    if (selfMo) {
                        seatBirds = zhongNiao(birdMjIds, lastWinSeat, winSeat);
                    } else {
                        if (isDuoXiang) {
                            catchBirdSeat = disCardSeat;
                            seatBirds = zhongNiao(birdMjIds, lastWinSeat, disCardSeat);
                        } else {
                            seatBirds = zhongNiao(birdMjIds, lastWinSeat, winSeat);
                        }
                    }
                }
                for (int i = 1; i < seatBirds.length; i++) {
                    if (seatBirds[i] > 0) {
                        HzMjPlayer p = seatMap.get(i);
                        if (p != null) {
                            p.changeAction(HzMjConstants.ACTION_COUNT_INDEX_ZHONGNIAO, seatBirds[i]);
                        }
                    }
                }
            }
            if (selfMo) {
                // 看庄家抓了几个鸟
                int birdPoint = seatBirds == null ? 0 : calcBirdPoint(seatBirds, winList.get(0));
                if(birdPoint ==0 &&birdNum>0 && buzhongzhong==1) {
                	birdPoint = birdNum;
                }
                seatBirdMap.put(winList.get(0), birdPoint);
                if(niaoFen==3){
                	birdPoint = (int)Math.pow(2, birdPoint);
                }else{
                	birdPoint *= niaoFen;
                }
                // 自摸
                int loseTotalPoint = 0;
                int totalHuPoint = 0;
                int totalNiaoPoint = 0;
                int totalPiaoPoint = 0;

                int winSeat = winList.get(0);
               
                HzMjPlayer winner = seatMap.get(winSeat);
                int addFen=0;
                if(noHzZimo==1 && !winner.haveHongzhong()) {
                	addFen+=1;
                }
                
				if (noHzQPQ == 1) {
					if (winner.getHuType().contains(HzMjConstants.HU_QINGYISE)
							|| winner.getHuType().contains(HzMjConstants.HU_QIDUI)
							|| winner.getHuType().contains(HzMjConstants.HU_PENGPENGHU)) {
						addFen+=1;
					}
				}

				  int hongZhongCount = HzMjQipaiTool.getMajiangCount(seatMap.get(winSeat).getHandMajiang(), HzMj.getHongZhongVal());
                 
                for (int seat : seatMap.keySet()) {
                    if (seat != winSeat) {
                        HzMjPlayer loser = seatMap.get(seat);
                        int huPoint = ziMoFen;
                        int niaoPoint = birdPoint;
                        int gangPoint = loser.getLostPoint();
                        int piaoPoint = (loser.getPiaoPoint() + winner.getPiaoPoint());

                        if (isCalBanker() && (seat == lastWinSeat || winList.get(0) == lastWinSeat)) {
                            // 分庄闲多算一分
                            huPoint += 1;
                        }
                      //  huPoint+=addFen;
                        int losePoint =0;
                        if(niaoFen==3){
                        	losePoint = huPoint*niaoPoint + piaoPoint+addFen;
                        }else{
                        	losePoint = huPoint + niaoPoint + piaoPoint+addFen;
                        }
                        losePoint *=diFen;
                        if(wuHongZhongJiaBei==1&& hongZhongCount == 0) {
                        	losePoint = losePoint*2;
                        }
                        loser.changeLostPoint(-losePoint);
                        loser.changePointArr(0, -huPoint);
                        if(niaoFen==3){
                        	loser.changePointArr(1, niaoPoint);
                        }else{
                        	loser.changePointArr(1, -niaoPoint);
                        }
                        loser.changePointArr(2, gangPoint);
                        loser.changePointArr(3, -piaoPoint);

                        totalHuPoint += huPoint;
                        totalPiaoPoint += piaoPoint;
                        totalNiaoPoint += niaoPoint;
                        loseTotalPoint += losePoint;
                    }
                }
                winner.changeAction(HzMjConstants.ACTION_COUNT_INDEX_ZIMO, 1);
                winner.changePointArr(0, totalHuPoint);
                if(niaoFen==3){
                	winner.changePointArr(1, birdPoint);
                }else{
                	winner.changePointArr(1, totalNiaoPoint);
                }
                winner.changePointArr(2, winner.getLostPoint());
                winner.changePointArr(3, totalPiaoPoint);
                winner.changeLostPoint(loseTotalPoint);
            } else {
                // 小胡接炮 每人1分
                HzMjPlayer loser = seatMap.get(disCardSeat);
               HzMj huMj = !nowDisCardIds.isEmpty() ? nowDisCardIds.get(0) : null;
                boolean isQiangGangHu = false;
                int totalLosePoint = 0;
                int totalHuPoint = 0;
                int totalNiaoPoint = 0;
                int totalGangPoint = loser.getLostPoint();
                int totalPiaoPoint = 0;
                for (int winSeat : winList) {
                    HzMjPlayer winner = seatMap.get(winSeat);
                    int hongZhongCount = HzMjQipaiTool.getMajiangCount(seatMap.get(winSeat).getHandMajiang(), HzMj.getHongZhongVal());
                  
                    if(huMj!=null && huMj.isHongzhong()) {
                    	hongZhongCount +=1;
                    }
                    int addFen=0;
                    int huPoint = 1;
                    int niaoPoint = 0;
                    int gangPoint = winner.getLostPoint();
                    if(noHzPao==1 && !winner.haveHongzhong()) {
                    	addFen+=1;
                    }
                    
                    
                    if (noHzQPQ == 1) {
                    	if (winner.getHuType().contains(HzMjConstants.HU_QINGYISE)
    							|| winner.getHuType().contains(HzMjConstants.HU_QIDUI)
    							|| winner.getHuType().contains(HzMjConstants.HU_PENGPENGHU)) {
    						addFen+=1;
    					}
    				}
                    
                    int piaoPoint = 0;
                    if (moGangHuList.contains(winSeat) && qiangGangHuBaoSanJia == 1) {
                        isQiangGangHu = true;
                        huPoint += 1;
                    }
//                    huPoint = huPoint;
                   // huPoint +=addFen;
                    if (isCalBanker() && (winSeat == lastWinSeat || loser.getSeat() == lastWinSeat)) {
                        // 分庄闲多算一分
                        huPoint += 1;
                    }
                    if (winList.size() > 1) {
                        niaoPoint = seatBirds == null ? 0 : calcBirdPoint(seatBirds, disCardSeat);
                        if(niaoPoint ==0 &&birdNum>0 && buzhongzhong==1) {
                        	niaoPoint = birdNum;
                        }
                       // niaoPoint *= niaoFen;
                        
                        if(niaoFen==3){
                        	niaoPoint = (int)Math.pow(2, niaoPoint);
                        }else{
                        	niaoPoint *= niaoFen;
                        }
                        
                        seatBirdMap.put(disCardSeat, niaoPoint);
                    } else {
                        niaoPoint = seatBirds == null ? 0 : calcBirdPoint(seatBirds, winSeat);
                        if(niaoPoint ==0 &&birdNum>0 && buzhongzhong==1) {
                        	niaoPoint = birdNum;
                        }
                        if(niaoFen==3){
                        	niaoPoint = (int)Math.pow(2, niaoPoint);
                        }else{
                        	niaoPoint *= niaoFen;
                        }
                        seatBirdMap.put(winSeat, niaoPoint);
                    }

                    // 胡牌

                    winner.changeAction(HzMjConstants.ACTION_COUNT_INDEX_JIEPAO, 1);
                    loser.changeAction(HzMjConstants.ACTION_COUNT_INDEX_DIANPAO, 1);

                    if (isQiangGangHu) {
                        //抢杠胡陪三家
                        huPoint *= (getMaxPlayerCount() - 1);
                        niaoPoint *= (getMaxPlayerCount() - 1);
                        HzMjPlayer winPlayerTmp = seatMap.get(winSeat);
                        for (HzMjPlayer p : seatMap.values()) {
                            if (p.getSeat() == winSeat) {
                                continue;
                            }
                            piaoPoint += (p.getPiaoPoint() + winPlayerTmp.getPiaoPoint());
                        }
                    } else {
                        piaoPoint = (loser.getPiaoPoint() + winner.getPiaoPoint());
                    }
                    
                    int point = 0;
                    if(niaoFen==3){
                    	point = huPoint * niaoPoint + piaoPoint+addFen;
                    }else{
                    	point = huPoint + niaoPoint + piaoPoint+addFen;
                    }
                    
                    point *=diFen;
                    if(wuHongZhongJiaBei==1&& hongZhongCount == 0) {
                    	point = point*2;
                    }
                    winner.changeLostPoint(point);
                    winner.changePointArr(0, huPoint);
                   
                    winner.changePointArr(1, niaoPoint);
                    winner.changePointArr(2, gangPoint);
                    winner.changePointArr(3, piaoPoint);
                    totalLosePoint += point;
                    totalHuPoint += huPoint;
                    totalPiaoPoint += piaoPoint;
                    totalNiaoPoint += niaoPoint;
                }
                loser.changeLostPoint(-totalLosePoint);
                loser.changePointArr(0, -totalHuPoint);
                
                if(niaoFen==3){
                	loser.changePointArr(1, 0);
                }else{
                	loser.changePointArr(1, -totalNiaoPoint);
                }
                
                loser.changePointArr(2, totalGangPoint);
                loser.changePointArr(3, -totalPiaoPoint);

                // 未放炮，也未接炮的玩法，杠分显示
                for (HzMjPlayer p : seatMap.values()) {
                    if (!winList.contains(p.getSeat()) && p.getSeat() != loser.getSeat()) {
                        p.changePointArr(2, p.getLostPoint());
                    }
                }
            }
        }
        for (HzMjPlayer seat : seatMap.values()) {
            seat.changePoint(seat.getLostPoint());
            logHuPoint(seat);
        }

        boolean over = playBureau == totalBureau;
        if (autoPlayGlob > 0) {
//          //是否解散
            boolean diss = false;
            if (autoPlayGlob == 1) {
                for (HzMjPlayer seat : seatMap.values()) {
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

        if(over){
            calcPointBeforeOver();
        }

        // 金币场
        if(isGoldRoom()){
            for(HzMjPlayer player : seatMap.values()){
                player.setPoint(player.getTotalPoint());
                player.setWinGold(player.getTotalPoint());
            }
            calcGoldRoom();
        }

        // -----------solo------------------
        if (isSoloRoom()) {
            if (huSeat != 0) {
                for (HzMjPlayer player : seatMap.values()) {
                    if (player.getSeat() == huSeat) {
                        player.setSoloWinner(true);
                    } else {
                        player.setSoloWinner(false);
                    }
                }
                calcSoloRoom();
            }
        }


        ClosingMjInfoRes.Builder res = sendAccountsMsg(over, selfMo, winList, birdMjIds, seatBirds, seatBirdMap, catchBirdSeat, false);

        //定下局庄
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
        calcAfter();
        saveLog(over, 0l, res.build());

        if (playBureau >= totalBureau || over) {
            calcOver1();
            calcOver2();
            calcOver3();
            diss();
        } else {
            initNext();
            calcOver1();
        }

        for (HzMjPlayer player : seatMap.values()) {
            if (player.isAutoPlaySelf()) {
                player.setAutoPlay(false, false);
            }
        }
        for (Player player : seatMap.values()) {
            player.saveBaseInfo();
        }
    }

    public void calcPointBeforeOver() {
        //大结算计算加倍分
        if (jiaBei == 1) {
            int jiaBeiPoint = 0;
            int loserCount = 0;
            for (HzMjPlayer player : seatMap.values()) {
                if (player.getTotalPoint() > 0 && player.getTotalPoint() < jiaBeiFen) {
                    jiaBeiPoint += player.getTotalPoint() * (jiaBeiShu - 1);
                    player.setTotalPoint(player.getTotalPoint() * jiaBeiShu);
                } else if (player.getTotalPoint() < 0) {
                    loserCount++;
                }
            }
            if (jiaBeiPoint > 0) {
                for (HzMjPlayer player : seatMap.values()) {
                    if (player.getTotalPoint() < 0) {
                        player.setTotalPoint(player.getTotalPoint() - (jiaBeiPoint / loserCount));
                    }
                }
            }
        }

        //大结算低于below分+belowAdd分
        if (belowAdd > 0 && playerMap.size() == 2) {
            for (HzMjPlayer player : seatMap.values()) {
                int totalPoint = player.getTotalPoint();
                if (totalPoint > -below && totalPoint < 0) {
                    player.setTotalPoint(player.getTotalPoint() - belowAdd);
                } else if (totalPoint < below && totalPoint > 0) {
                    player.setTotalPoint(player.getTotalPoint() + belowAdd);
                }
            }
        }
    }

    private boolean checkAuto3() {
		boolean diss = false;
//		if(autoPlayGlob==3) {
			boolean diss2 = false;
			 for (HzMjPlayer seat : seatMap.values()) {
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
        for (HzMjPlayer player : playerMap.values()) {
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

    private int calcBirdPoint(int[] seatBridArr, int seat) {
        return seatBridArr[seat];
    }

    /**
     * 抓鸟
     *
     * @return
     */
    private int[] zhuaNiao(int birdNum) {
        birdNum = birdNum > leftMajiangs.size() ? leftMajiangs.size() : birdNum;
        int[] bird = new int[birdNum];
        for (int i = 0; i < birdNum; i++) {
            HzMj birdMj = getLeftMajiang();
            if (birdMj != null) {
                bird[i] = birdMj.getId();
            }
        }
        return bird;
    }

    /**
     * 中鸟
     *
     * @param birdMjIds
     * @param startSeat 起始算鸟位
     * @param zhongSeat 中鸟位
     * @return arr[seat] = 中鸟数
     */
    private int[] zhongNiao(int[] birdMjIds, int startSeat, int zhongSeat) {
        int[] seatArr = new int[getMaxPlayerCount() + 1];
        for (int i = 0; i < birdMjIds.length; i++) {
            HzMj mj = HzMj.getMajang(birdMjIds[i]);
            if (yiNiaoQuanZhong == 1) {
                if (mj.isHongzhong()) {
                    seatArr[zhongSeat] += 10;
                } else if (!mj.isFeng()) {
                    seatArr[zhongSeat] += mj.getPai();
                }
            } else {
                if (zhongNiao159 == 1) {
                    if (mj.isHongzhong()) {
                        seatArr[zhongSeat] += 1;
                    } else if (!mj.isFeng()) {
                        if (mj.getPai() == 1 || mj.getPai() == 5 || mj.getPai() == 9) {
                            seatArr[zhongSeat] += 1;
                        }
                    }
                } else {
                    if (mj.isHongzhong()) {
                        seatArr[zhongSeat] = seatArr[zhongSeat] + 1;
                    } else if (!mj.isFeng()) {
                        int pai = (mj.getPai() - 1) % 4;// 从自己开始算 所以减1
                        int birdSeat = pai + startSeat > 4 ? pai + startSeat - 4 : pai + startSeat;
                        if (getMaxPlayerCount() == 3) {
                            //三人玩法，桌面上的鸟位置是1，3，4，而后端的位置是1，2，3
                            if (zhongSeat == 3) {
                                if (birdSeat == 4) {
                                    seatArr[zhongSeat] += 1;
                                }
                            } else if (birdSeat == zhongSeat) {
                                seatArr[birdSeat] += 1;
                            }
                        } else {
                            if (birdSeat == zhongSeat) {
                                seatArr[zhongSeat] += 1;
                            }
                        }
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
                tempMap.put("nowDisCardIds", StringUtil.implode(HzMjHelper.toMajiangIds(nowDisCardIds), ","));
            }
            if (tempMap.containsKey("leftPais")) {
                tempMap.put("leftPais", StringUtil.implode(HzMjHelper.toMajiangIds(leftMajiangs), ","));
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
        for (HzMjPlayer player : seatMap.values()) {
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
        wrapper.putInt(10, dianPaoZimo);
        wrapper.putInt(11, isCalcBanker);
        wrapper.putInt(12, hu7dui);

        JSONArray tempJsonArray = new JSONArray();
        for (int seat : tempActionMap.keySet()) {
            tempJsonArray.add(tempActionMap.get(seat).buildData());
        }
        wrapper.putString("tempActions", tempJsonArray.toString());
        wrapper.putInt(13, maxPlayerCount);
        wrapper.putInt(14, dealDice);
        wrapper.putInt(15, zhuaJiJiangJi);
        wrapper.putInt(16, yiNiaoQuanZhong);
        wrapper.putInt(17, niaoFen);
        wrapper.putInt(18, wuHongZhongJiaNiao);
        wrapper.putInt(19, youPaoBiHu);
        wrapper.putInt(20, qiangGangHu);
        wrapper.putInt(21, qiangGangHuBaoSanJia);
        wrapper.putInt(22, isAutoPlay);
        wrapper.putInt(23, dianGangKeHu);
        wrapper.putInt(24, moGangSeat);
        wrapper.putInt(25, moGangSameCount);
        wrapper.putString(26, StringUtil.implode(moTailPai, ","));
        wrapper.putInt(27, kePiao);
        wrapper.putInt(28, diFen);
        wrapper.putInt(29, zhongNiao159);
        wrapper.putInt(30, jiaBei);
        wrapper.putInt(31, jiaBeiFen);
        wrapper.putInt(32, jiaBeiShu);
        wrapper.putInt(33, noHzZimo);
        wrapper.putInt(34, noHzPao);
        wrapper.putInt(35, noHzQPQ);
        wrapper.putInt(36, autoPlayGlob);
        wrapper.putInt(37, wuHongZhongJiaBei);
        wrapper.putInt(38, ziMoFen);
        
        wrapper.putInt(39, sihongzHu);
        wrapper.putInt(40, buzhongzhong);
        wrapper.putInt(41, baahongz);
        
        wrapper.putInt(42, zimoBihu);
        wrapper.putInt(43, below);
        wrapper.putInt(44, belowAdd);
        
        wrapper.putInt(45, hongzhongBJP);
        wrapper.putInt(46, noYiPaoDuoX);
        wrapper.putInt(47, ztJsGangFen);
        
        
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
        addPlayLog(disCardRound + "_" + lastWinSeat + "_" + HzMjDisAction.action_dice + "_" + dealDice);
        setDealDice(dealDice);
        logFaPaiTable();
        // 天胡或者暗杠
        boolean chupai = true;
        
        for (HzMjPlayer tablePlayer : seatMap.values()) {
      	  int hongZhongCount = HzMjQipaiTool.getMajiangCount(tablePlayer.getHandMajiang(), HzMj.getHongZhongVal());
            if(hongZhongCount >=4 &&isSiBaHZ()) {
            	chupai = false;
            } 
      	
      }
        
        for (HzMjPlayer tablePlayer : seatMap.values()) {
            DealInfoRes.Builder res = DealInfoRes.newBuilder();
            if (lastWinSeat == tablePlayer.getSeat() ||isSiBaHZ()) {
                List<Integer> actionList = tablePlayer.checkMo(null);
                if (!actionList.isEmpty()) {
                    addActionSeat(tablePlayer.getSeat(), actionList);
                    res.addAllSelfAct(actionList);
                    logFaPaiPlayer(tablePlayer, actionList);
                }
            }
            res.addAllHandCardIds(tablePlayer.getHandPais());
            if(chupai){
            	res.setNextSeat(getNextDisCardSeat());
            }else {
            	res.setNextSeat(0);

            }
            res.setGameType(getWanFa());
            res.setRemain(leftMajiangs.size());
            res.setBanker(lastWinSeat);
            res.setDealDice(dealDice);
//			if (userId == tablePlayer.getUserId()) {
//				continue;
//			}
//            int hongZhongCount = HzMjQipaiTool.getMajiangCount(tablePlayer.getHandMajiang(), HzMj.getHongZhongVal());
//            if(hongZhongCount==4) {
//            	chupai = false;
//            } 
            tablePlayer.writeSocket(res.build());
            if (tablePlayer.isAutoPlay()) {
                tablePlayer.setAutoPlayTime(0);
            }
            sendTingInfo(tablePlayer);
            logFaPaiPlayer(tablePlayer, null);
            
            if(tablePlayer.isAutoPlay()) {
            	addPlayLog(getDisCardRound() + "_" +tablePlayer.getSeat() + "_" + HzMjDisAction.action_tuoguan + "_" +1+ tablePlayer.getExtraPlayLog());
            }
        }
        for (Player player : getRoomPlayerMap().values()) {
            DealInfoRes.Builder res = DealInfoRes.newBuilder();
			if (chupai) {
				res.setNextSeat(getNextDisCardSeat());
			} else {
				res.setNextSeat(0);
			}
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

    public void moMajiang(HzMjPlayer player, boolean isBuZhang) {
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
        HzMj majiang = null;
        if (disCardRound != 0) {
            // 玩家手上的牌是双数，已经摸过牌了
            if (player.isAlreadyMoMajiang()) {
            	PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
                buildPlayRes(builder, player, HzMjDisAction.action_pass, null);
                builder.setSeat(nowDisCardSeat);
                player.writeSocket(builder.build());
                return;
            }
            if (getLeftMajiangCount() == 0) {
                calcOver();
                return;
            }

            if (GameServerConfig.isDebug() && !player.isRobot()) {
                if (zpMap.containsKey(player.getUserId()) && zpMap.get(player.getUserId()) > 0) {
                    majiang = HzMjHelper.findMajiangByVal(leftMajiangs, zpMap.get(player.getUserId()));
                    if (majiang != null) {
                        zpMap.remove(player.getUserId());
                        leftMajiangs.remove(majiang);
                    }
                }
            }
            if (majiang == null) {
                majiang = getLeftMajiang();
            }
        }
        if (majiang != null) {
            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + HzMjDisAction.action_moMjiang + "_" + majiang.getId() + player.getExtraPlayLog());
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
        }
        logMoMj(player, majiang, arr);
        MoMajiangRes.Builder res = MoMajiangRes.newBuilder();
        res.setUserId(player.getUserId() + "");
        res.setSeat(player.getSeat());
        for (HzMjPlayer seat : seatMap.values()) {
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
       
        for (Player roomPlayer : roomPlayerMap.values()) {
            MoMajiangRes.Builder copy = res.clone();
            roomPlayer.writeSocket(copy.build());
        }
        
        
        sendTingInfo(player);
    }

    /**
     * 打红中麻将
     *
     * @return
     */
//	public boolean isHzMaJiang() {
//		return playType == ZZMajiangConstants.play_type_hongzhong;
//	}

    /**
     * 红中自摸自动胡
     *
     * @param player
     * @param actionList
     */
//	public void autoZiMoHu() {
//		if (isHzMaJiang()) {
//			for (int seat : actionSeatMap.keySet()) {
//				List<Integer> actionList = actionSeatMap.get(seat);
//				if (actionList.get(0) == 1) {
//					// 可以胡牌 自动胡
//					ZZMajiangPlayer player = seatMap.get(seat);
//					hu(player, null, HzMjDisAction.action_hu);
//				}
//			}
//		}
//
//	}

    /**
     * 玩家表示胡
     *
     * @param player
     * @param majiangs
     */
    private void hu(HzMjPlayer player, List<HzMj> majiangs, int action) {
        if (state != table_state.play) {
            return;
        }
        List<Integer> actionList = actionSeatMap.get(player.getSeat());
        if (actionList == null
                || (actionList.get(HzMjConstants.ACTION_INDEX_HU) != 1 && actionList.get(HzMjConstants.ACTION_INDEX_ZIMO) != 1)) {// 如果集合为空或者第一操作不为胡，则返回
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
        List<HzMj> huHand = new ArrayList<>(player.getHandMajiang());
        boolean zimo = player.isAlreadyMoMajiang();
        if(nowDisCardIds==null ||nowDisCardIds.size()==0) {
        	zimo =true;
        }
        if (!zimo) {
            if (moGangHuList.contains(player.getSeat())) {
                // 抢杠胡
                huHand.add(moGang);
                builder.setFromSeat(nowDisCardSeat);
                builder.addHuArray(HzMjConstants.HU_QIANGGANGHU);
                player.getHuType().add(HzMjConstants.HU_QIANGGANGHU);
                HzMjPlayer fangPaoPlayer = seatMap.get(nowDisCardSeat);
                fangPaoPlayer.getHuType().add(HzMjConstants.HU_FANGPAO);
            } else {
                // 放炮
                huHand.addAll(nowDisCardIds);
                builder.setFromSeat(disCardSeat);
             //   player.getHuType().add(HzMjConstants.HU_JIPAO);
                HzMjPlayer fangPaoPlayer = seatMap.get(disCardSeat);
                fangPaoPlayer.getHuType().add(HzMjConstants.HU_FANGPAO);
            }
        } else {
            builder.addHuArray(HzMjConstants.HU_ZIMO);
            //player.getHuType().add(HzMjConstants.HU_ZIMO);
        }
        if (!HzMjTool.isHu(huHand, this)) {
            return;
        }
        
        
		if (noHzQPQ == 1) {
			List<HzMj> allMajiangs = new ArrayList<>();
			allMajiangs.addAll(huHand);
			allMajiangs.addAll(player.getGang());
			allMajiangs.addAll(player.getPeng());

			
			if (HzMjTool.isPengPengHu(allMajiangs)) {
				player.getHuType().add(HzMjConstants.HU_PENGPENGHU);
			}
			
			if (HzMjTool.isHuQidui(allMajiangs)) {
				player.getHuType().add(HzMjConstants.HU_QIDUI); 
			}
			if (HzMjTool.isQingyise(allMajiangs)) {
				player.getHuType().add(HzMjConstants.HU_QINGYISE);
			}
		}
		
        
        
        
        
        
        if (moGangHuList.contains(player.getSeat())) {
            HzMjPlayer moGangPlayer = seatMap.get(moGangSeat);

            if (moGangPlayer == null) {
                moGangPlayer = getPlayerByHasMajiang(moGang);
            }
            if (moGangPlayer == null) {
                moGangPlayer = seatMap.get(moMajiangSeat);
            }
            List<HzMj> moGangMajiangs = new ArrayList<>();
            moGangMajiangs.add(moGang);
            moGangPlayer.addOutPais(moGangMajiangs, 0, 0);
            // 摸杠被人胡了 相当于自己出了一张牌
            recordDisMajiang(moGangMajiangs, moGangPlayer);
//			addPlayLog(disCardRound + "_" + player.getSeat() + "_" + HzMjDisAction.action_chupai + "_" + HzMjHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
            moGangPlayer.qGangUpdateOutPais(moGang);
        }
        buildPlayRes(builder, player, action, huHand);
        if (zimo) {
            builder.setZimo(1);
        }
        if (!huConfirmList.isEmpty()) {
            builder.addExt(StringUtil.implode(huConfirmList, ","));
        }
        // 胡
        for (HzMjPlayer seat : seatMap.values()) {
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
        List<HzMj> huPai = new ArrayList<>();
        huPai.add(huHand.get(huHand.size() - 1));
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + HzMjHelper.toMajiangStrs(huPai) + "_" + StringUtil.implode(player.getHuType(), ",") + player.getExtraPlayLog());
        logActionHu(player, majiangs, "");
        if (isCalcOver()) {
            // 等待别人胡牌 如果都确认完了，胡
            calcOver();
        } else {
            //removeActionSeat(player.getSeat());
            player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip, action);
        }
    }

    private void buildPlayRes(PlayMajiangRes.Builder builder, Player player, int action, List<HzMj> majiangs) {
        HzMjResTool.buildPlayRes(builder, player, action, majiangs);
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
    private HzMjPlayer getPlayerByHasMajiang(HzMj majiang) {
        for (HzMjPlayer player : seatMap.values()) {
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
            HzMjPlayer moGangPlayer = null;
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
                        !(tempActionMap.containsKey(huseat) && tempActionMap.get(huseat).getAction() == HzMjDisAction.action_hu)) {
                    over = false;
                    break;
                }
            }
        }
        
        if(noYiPaoDuoX==1&&huActionList.size()>1){
        	boolean over2 = yiPaoYiXiang(disCardSeat, huActionList, huConfirmList);
        	if(over2){
        		over = over2;
        	}
        }
        

        if (!over) {
            HzMjPlayer disMajiangPlayer = seatMap.get(disCardSeat);
            for (int huseat : huActionList) {
                if (huConfirmList.contains(huseat)) {
                    continue;
                }
                PlayMajiangRes.Builder disBuilder = PlayMajiangRes.newBuilder();
                HzMjPlayer seatPlayer = seatMap.get(huseat);
                buildPlayRes(disBuilder, disMajiangPlayer, 0, null);
                List<Integer> actionList = actionSeatMap.get(huseat);
                disBuilder.addAllSelfAct(actionList);
                seatPlayer.writeSocket(disBuilder.build());
            }
        }
//        for (HzMjPlayer player : seatMap.values()) {
//        	 int hongZhongCount = HzMjQipaiTool.getMajiangCount(player.getHandMajiang(), HzMj.getHongZhongVal());
//            if ((player.isAlreadyMoMajiang() ||hongZhongCount==4)&& !huConfirmList.contains(player.getSeat())) {
//                over = false;
//            }
//        }
        return over;
    }
    
    
    
    private boolean yiPaoYiXiang(int disCardSeat, List<Integer> huActionList,List<Integer> huConfirmList){
    
    	 // HzMjPlayer disMajiangPlayer = seatMap.get(disCardSeat);
    	//  HashMap<Integer,Integer> proMap = new HashMap<Integer,Integer>();
    	  
    	  int firstSeat = 0;
    	  int seat = disCardSeat;
    	  for(int i=0;i<maxPlayerCount-1;i++){
    		   seat = calcNextSeat(seat);
    		  if(seat ==disCardSeat){
    			  continue;
    		  }
    		  if(huActionList.contains(seat)){
    			  firstSeat =seat;
    			  break;
    		  }
    	  }
    	  if(huConfirmList.contains(firstSeat)){
    		  huConfirmList.clear();
    		  huConfirmList.add(firstSeat);
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
    private void chiPengGang(HzMjPlayer player, List<HzMj> majiangs, int action) {
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
        HzMj disMajiang = null;
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
            sameCount = HzMjHelper.getMajiangCount(majiangs, majiangs.get(0).getVal());
        }
        // 如果是杠 后台来找出是明杠还是暗杠
        if (action == HzMjDisAction.action_minggang || action == HzMjDisAction.action_angang) {
            majiangs = HzMjHelper.getMajiangList(player.getHandMajiang(), majiangs.get(0).getVal());
            sameCount = majiangs.size();
            if (sameCount == 4) {
                // 有4张一样的牌是暗杠
                action = HzMjDisAction.action_angang;
            }
            // 其他是明杠
        }
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return;
        }
        boolean hasQGangHu = false;
        if (action == HzMjDisAction.action_peng) {
            boolean can = canPeng(player, majiangs, sameCount);
            if (!can) {
                return;
            }
        } else if (action == HzMjDisAction.action_angang) {
            boolean can = canAnGang(player, majiangs, sameCount);
            if (!can) {
                return;
            }
            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + HzMjHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
        } else if (action == HzMjDisAction.action_minggang) {
            boolean can = canMingGang(player, majiangs, sameCount);
            if (!can) {
                return;
            }

            ArrayList<HzMj> mjs = new ArrayList<>(majiangs);
            if (sameCount == 3) {
                mjs.add(disMajiang);
            }
            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + HzMjHelper.toMajiangStrs(mjs) + player.getExtraPlayLog());

            // 特殊处理一张牌明杠的时候别人可以胡
            if (sameCount == 1 && canGangHu()) {
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
            if ((action == HzMjDisAction.action_minggang && sameCount == 3)
                    || action == HzMjDisAction.action_peng || action == HzMjDisAction.action_chi) {
                if (action == HzMjDisAction.action_chi) {
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
    private boolean checkQGangHu(HzMjPlayer player, List<HzMj> majiangs, int action, int sameCount) {
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        Map<Integer, List<Integer>> huListMap = new HashMap<>();
        for (HzMjPlayer seatPlayer : seatMap.values()) {
            if (seatPlayer.getUserId() == player.getUserId()) {
                continue;
            }
            // 推送消息
            List<Integer> hu = seatPlayer.checkDisMajiang(majiangs.get(0), this.canGangHu() || dianGangKeHu == 1);
            if (!hu.isEmpty() && hu.get(0) == 1) {
            	//红中不可接炮不能抢杠胡
            	if(hongzhongBJP==1&&seatPlayer.haveHongzhong()){
            		continue;
            	}
            	
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
                HzMjPlayer seatPlayer = seatMap.get(entry.getKey());
                copy.addAllSelfAct(entry.getValue());
                seatPlayer.writeSocket(copy.build());
            }
            return true;
        }
        return false;
    }

    private void chiPengGang(PlayMajiangRes.Builder builder, HzMjPlayer player, List<HzMj> majiangs, int action, boolean hasQGangHu, int sameCount) {

        List<Integer> actionList = actionSeatMap.get(player.getSeat());
        if (action == HzMjDisAction.action_peng && actionList.get(HzMjConstants.ACTION_INDEX_MINGGANG) == 1) {
            // 可以碰也可以杠
            player.addPassGangVal(majiangs.get(0).getVal());
        }

        player.addOutPais(majiangs, action, disCardSeat);
        buildPlayRes(builder, player, action, majiangs);
        List<Integer> actList = removeActionSeat(player.getSeat());
        if (!hasQGangHu) {
            clearActionSeatMap();
        }
        if (action == HzMjDisAction.action_chi || action == HzMjDisAction.action_peng) {
            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + HzMjHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
        }
        // 不是普通出牌
        setNowDisCardSeat(player.getSeat());
        for (HzMjPlayer seatPlayer : seatMap.values()) {
            // 推送消息
            PlayMajiangRes.Builder copy = builder.clone();
            if (actionSeatMap.containsKey(seatPlayer.getSeat())) {
                copy.addAllSelfAct(actionSeatMap.get(seatPlayer.getSeat()));
            }
            seatPlayer.writeSocket(copy.build());
        }
        if (action == HzMjDisAction.action_chi || action == HzMjDisAction.action_peng) {
            sendTingInfo(player);
        }
        for (Player roomPlayer : roomPlayerMap.values()) {
            PlayMajiangRes.Builder copy = builder.clone();
            roomPlayer.writeSocket(copy.build());
        }
        if (!hasQGangHu) {
            calcPoint(player, action, sameCount, majiangs);
        }
        if (!hasQGangHu && action == HzMjDisAction.action_minggang || action == HzMjDisAction.action_angang) {
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
    private void chuPai(HzMjPlayer player, List<HzMj> majiangs, int action) {
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
        if (majiangs.get(0).isHongzhong()) {
//            return;
        }
        if (!player.isAlreadyMoMajiang()) {
            // 还没有摸牌
            return;
        }
        if (!actionSeatMap.isEmpty()) {//出牌自动过掉手上操作
            guo(player, null, HzMjDisAction.action_pass);
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
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + HzMjHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
        for (HzMjPlayer seat : seatMap.values()) {
            if (seat.getUserId() != player.getUserId()) {
            	boolean canHu= this.canDianPao();
            	if(noHzPao==1&&!seat.haveHongzhong()) {
            		canHu = true;
            	}
            	
            	if(hongzhongBJP==1&&seat.haveHongzhong()){
            		canHu = false;
            	}
                List<Integer> list = seat.checkDisMajiang(majiangs.get(0), canHu);
                if (list.contains(1)) {
                	 addActionSeat(seat.getSeat(), list);
                	  checkYouPaoBiHuPG(seat);
                    seat.setLastCheckTime(System.currentTimeMillis());
                    logChuPaiActList(seat, majiangs.get(0), list);
                }
            }
        }
        sendDisMajiangAction(builder, player);

        // 给下一家发牌
        checkMo();

    }

    /***
     * 检查you炮必胡碰杠 不发碰杠操作
     * @param player
     */
	private void checkYouPaoBiHuPG(HzMjPlayer player) {
		if(youPaoBiHu==1) {
		  	List<Integer> actionList = actionSeatMap.get(player.getSeat());
		      if (actionList != null
		              && (actionList.get(HzMjConstants.ACTION_INDEX_HU) == 1)&& player.getSeat()!=moMajiangSeat) {// 如果有炮必胡，被点炮不能过
		         if(actionList.get(HzMjConstants.ACTION_INDEX_PENG) == 1) {
		        	 actionList.set(HzMjConstants.ACTION_INDEX_PENG, 0);
		         }
		         if(actionList.get(HzMjConstants.ACTION_INDEX_MINGGANG) == 1) {
		        	 actionList.set(HzMjConstants.ACTION_INDEX_MINGGANG, 0);
		         }
		      }
		  }
	}

    public List<Integer> getHuSeatByActionMap() {
        List<Integer> huList = new ArrayList<>();
        for (int seat : actionSeatMap.keySet()) {
            List<Integer> actionList = actionSeatMap.get(seat);
            if (actionList.get(HzMjConstants.ACTION_INDEX_HU) == 1 || actionList.get(HzMjConstants.ACTION_INDEX_ZIMO) == 1) {
                // 胡
                huList.add(seat);
            }

        }
        return huList;
    }

    private void sendDisMajiangAction(PlayMajiangRes.Builder builder, HzMjPlayer player) {
        for (HzMjPlayer seatPlayer : seatMap.values()) {
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
                copy.addExt(HzMjTool.isTing(seatPlayer.getHandMajiang(), isHu7dui()) ? "1" : "0");
            }
            seatPlayer.writeSocket(copy.build());
        }
        for (Player roomPlayer : roomPlayerMap.values()) {
            PlayMajiangRes.Builder copy = builder.clone();
            roomPlayer.writeSocket(copy.build());
        }
    }

    public synchronized void playCommand(HzMjPlayer player, List<HzMj> majiangs, int action) {
        playCommand(player, majiangs, null, action);
    }

    /**
     * 出牌
     *
     * @param player
     * @param majiangs
     * @param action
     */
    public synchronized void playCommand(HzMjPlayer player, List<HzMj> majiangs, List<Integer> hucards, int action) {
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

        if (HzMjDisAction.action_hu == action) {
            hu(player, majiangs, action);
            return;
        }
        // 手上没有要出的麻将
        if (action != HzMjDisAction.action_minggang)
            if (!player.getHandMajiang().containsAll(majiangs)) {
                return;
            }
        changeDisCardRound(1);
        if (action == HzMjDisAction.action_pass) {
            guo(player, majiangs, action);
        } else if (action != 0) {
            chiPengGang(player, majiangs, action);
        } else {
            chuPai(player, majiangs, action);
        }
        // 记录最后一次动作的时间
        setLastActionTime(TimeUtil.currentTimeMillis());
    }

    private void passMoHu(HzMjPlayer player, List<HzMj> majiangs, int action) {
        if (!moGangHuList.contains(player.getSeat())) {
            return;
        }

        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        buildPlayRes(builder, player, action, majiangs);
        builder.setSeat(nowDisCardSeat);
        removeActionSeat(player.getSeat());
        player.writeSocket(builder.build());
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + HzMjHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
        if (isCalcOver()) {
            calcOver();
            return;
        }
        player.setPassMajiangVal(nowDisCardIds.get(0).getVal());

        if (moGangHuList.isEmpty()) {
            HzMjPlayer moGangPlayer = seatMap.get(getNowDisCardSeat());
            majiangs = new ArrayList<>();
            majiangs.add(moGang);
            if (moGangPlayer.getaGang().contains(moGang)) {
                calcPoint(moGangPlayer, HzMjDisAction.action_angang, 4, majiangs);
            } else {
                calcPoint(moGangPlayer, HzMjDisAction.action_minggang, moGangSameCount > 0 ? moGangSameCount : 1, majiangs);
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
    private void guo(HzMjPlayer player, List<HzMj> majiangs, int action) {
        if (state != table_state.play) {
            return;
        }
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return;
        }
        
        // 如果有炮必胡，被点炮不能过
        if(youPaoBiHu==1) {
        	List<Integer> actionList = actionSeatMap.get(player.getSeat());
            if (actionList != null
                    && (actionList.get(HzMjConstants.ACTION_INDEX_HU) == 1)&& player.getSeat()!=moMajiangSeat) {
                return;
            }
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
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + HzMjHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
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
            HzMjPlayer disMajiangPlayer = seatMap.get(disCardSeat);
            PlayMajiangRes.Builder disBuilder = PlayMajiangRes.newBuilder();
            buildPlayRes(disBuilder, disMajiangPlayer, 0, null);
            for (int seat : actionSeatMap.keySet()) {
                List<Integer> actionList = actionSeatMap.get(seat);
                PlayMajiangRes.Builder copy = disBuilder.clone();
                copy.addAllSelfAct(new ArrayList<>());
                if (actionList != null && !tempActionMap.containsKey(seat) && !huConfirmList.contains(seat)) {
                    copy.addAllSelfAct(actionList);
                    HzMjPlayer seatPlayer = seatMap.get(seat);
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

    private void calcPoint(HzMjPlayer player, int action, int sameCount, List<HzMj> majiangs) {
        int lostPoint = 0;
        int getPoint = 0;
        int[] seatPointArr = new int[getMaxPlayerCount() + 1];
        if (action == HzMjDisAction.action_peng) {
            return;

        } else if (action == HzMjDisAction.action_angang) {
            // 暗杠相当于自摸每人出2分
            lostPoint = -2;
            getPoint = 2 * (getMaxPlayerCount() - 1);

        } else if (action == HzMjDisAction.action_minggang) {
            if (sameCount == 1) {
                // 碰牌之后再抓一个牌每人出1分
                // 放杠的人出3分

                if (player.isPassGang(majiangs.get(0))) {
                    // 特殊处理 可以碰可以杠的牌 选择了碰 再杠不算分
                    return;
                }
                lostPoint = -1;
                getPoint = 1 * (getMaxPlayerCount() - 1);
            } else if (sameCount == 3) {
                // 放杠
                HzMjPlayer disPlayer = seatMap.get(disCardSeat);
                //disPlayer.getMyExtend().setMjFengshen(FirstmythConstants.firstmyth_index13, 1);
                int point = (getMaxPlayerCount() - 1);
                
                disPlayer.changeLostPoint(-(point*diFen));
                seatPointArr[disPlayer.getSeat()] = -point;
                player.changeLostPoint(point*diFen);
                seatPointArr[player.getSeat()] = point;
            }
        }

        if (lostPoint != 0) {
            for (HzMjPlayer seat : seatMap.values()) {
                if (seat.getUserId() == player.getUserId()) {
                    player.changeLostPoint(getPoint*diFen);
                    seatPointArr[player.getSeat()] = getPoint;
                } else {
                    seat.changeLostPoint(lostPoint*diFen);
                    seatPointArr[seat.getSeat()] = lostPoint;
                }
            }
        }

        String seatPointStr = "";
        for (int i = 1; i <= getMaxPlayerCount(); i++) {
            seatPointStr += seatPointArr[i] + ",";
        }
        seatPointStr = seatPointStr.substring(0, seatPointStr.length() - 1);
        ComMsg.ComRes.Builder res = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_gangFen, seatPointStr);
        GeneratedMessage msg = res.build();
        broadMsgToAll(msg);

        if (action != HzMjDisAction.action_chi) {
//            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + HzMjHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog() + "_" + seatPointStr);
        }
    }

    private void recordDisMajiang(List<HzMj> majiangs, HzMjPlayer player) {
        setNowDisCardIds(majiangs);
        // changeDisCardRound(1);
        setDisCardSeat(player.getSeat());
    }

    public List<HzMj> getNowDisCardIds() {
        return nowDisCardIds;
    }

    public void setNowDisCardIds(List<HzMj> nowDisCardIds) {
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
                HzMjPlayer player = seatMap.get(seat);
                if (player != null && player.isRobot()) {
                    // 如果是机器人可以直接决定
                    List<Integer> actionList = actionSeatMap.get(seat);
                    if (actionList == null) {
                        continue;
                    }
                    List<HzMj> list = new ArrayList<>();
                    if (!nowDisCardIds.isEmpty()) {
                        list = HzMjQipaiTool.getVal(player.getHandMajiang(), nowDisCardIds.get(0).getVal());
                    }
                    if (actionList.get(HzMjConstants.ACTION_INDEX_HU) == 1 || actionList.get(HzMjConstants.ACTION_INDEX_ZIMO) == 1) {
                        // 胡
                        playCommand(player, new ArrayList<HzMj>(), HzMjDisAction.action_hu);

                    } else if (actionList.get(HzMjConstants.ACTION_INDEX_ANGANG) == 1) {
                        playCommand(player, list, HzMjDisAction.action_angang);

                    } else if (actionList.get(HzMjConstants.ACTION_INDEX_MINGGANG) == 1) {
                        playCommand(player, list, HzMjDisAction.action_minggang);

                    } else if (actionList.get(HzMjConstants.ACTION_INDEX_PENG) == 1) {
                        playCommand(player, list, HzMjDisAction.action_peng);
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
            HzMjPlayer next = seatMap.get(nextseat);
            if (next != null && next.isRobot()) {
                List<Integer> actionList = actionSeatMap.get(next.getSeat());
                if (actionList != null) {
                    List<HzMj> list = null;
                    if (actionList.get(0) == 1) {
                        // 胡
                        playCommand(next, new ArrayList<HzMj>(), HzMjDisAction.action_hu);
                    } else if (actionList.get(3) == 1) {
                        // 机器人暗杠
                        Map<Integer, Integer> handMap = HzMjHelper.toMajiangValMap(next.getHandMajiang());
                        for (Entry<Integer, Integer> entry : handMap.entrySet()) {
                            if (entry.getValue() == 4) {
                                // 可以暗杠
                                list = HzMjHelper.getMajiangList(next.getHandMajiang(), entry.getKey());
                            }
                        }
                        playCommand(next, list, HzMjDisAction.action_angang);

                    } else if (actionList.get(2) == 1) {
                        Map<Integer, Integer> pengMap = HzMjHelper.toMajiangValMap(next.getPeng());
                        for (HzMj handMajiang : next.getHandMajiang()) {
                            if (pengMap.containsKey(handMajiang.getVal())) {
                                // 有碰过
                                list = new ArrayList<>();
                                list.add(handMajiang);
                                playCommand(next, list, HzMjDisAction.action_minggang);
                                break;
                            }
                        }

                    } else if (actionList.get(1) == 1) {
                        playCommand(next, list, HzMjDisAction.action_peng);
                    }
                } else {
                    List<Integer> handMajiangs = new ArrayList<>(next.getHandPais());
                    HzMjQipaiTool.dropHongzhongVal(handMajiangs);
                    int maJiangId = HzMjRobotAI.getInstance().outPaiHandle(0, handMajiangs, new ArrayList<Integer>());
                    List<HzMj> majiangList = HzMjHelper.toMajiang(Arrays.asList(maJiangId));
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

        List<Integer> copy = HzMjConstants.getMajiangList();
        
        //红中
        if(baahongz==1) {
        	 for (int i = 201; i <= 204; i++) {
             	copy.add(i);
             }
        }
       
        addPlayLog(copy.size() + "");
        List<List<HzMj>> list = null;
        if (zp != null) {
            list = HzMjTool.fapai(copy, getMaxPlayerCount(), zp);
        } else {
            list = HzMjTool.fapai(copy, getMaxPlayerCount());
        }
        int i = 1;
        for (HzMjPlayer player : playerMap.values()) {
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
    public void setLeftMajiangs(List<HzMj> leftMajiangs) {
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
    public HzMj getLeftMajiang() {
        if (this.leftMajiangs.size() > 0) {
            HzMj majiang = this.leftMajiangs.remove(0);
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
        res.addExt(payType);                      //0
        res.addExt(birdNum);                      //1
        res.addExt(dianPaoZimo);                  //2
        res.addExt(isCalcBanker);                 //3
        res.addExt(hu7dui);                       //4
        res.addExt(1);                            //5
        res.addExt(isAutoPlay);                   //6
        res.addExt(qiangGangHu);                  //7
        res.addExt(qiangGangHuBaoSanJia);         //8
        res.addExt(zhuaJiJiangJi);                //9
        res.addExt(yiNiaoQuanZhong);              //10
        res.addExt(niaoFen);                      //11
        res.addExt(wuHongZhongJiaNiao);           //12
        res.addExt(youPaoBiHu);                   //13
        res.addExt(dianGangKeHu);                 //14
        res.addExt(kePiao);                       //15
        res.addExt(diFen);                        //16
        res.addExt(zhongNiao159);                 //17
        res.addExt(jiaBei);           //18
        res.addExt(jiaBeiFen);        //19
        res.addExt(jiaBeiShu);        //20
        res.addExt(wuHongZhongJiaBei);        //21
        res.addExt(ziMoFen);        //22
        res.addExt(sihongzHu);        //22
        res.addExt(buzhongzhong);        //22
        res.addExt(baahongz);        //22
        
        

        res.addStrExt(StringUtil.implode(moTailPai, ","));      //0

        res.setMasterId(getMasterId() + "");
        if (leftMajiangs != null) {
            res.setRemain(leftMajiangs.size());
        } else {
            res.setRemain(0);
        }
        res.setDealDice(dealDice);
        List<PlayerInTableRes> players = new ArrayList<>();
        for (HzMjPlayer player : playerMap.values()) {
            PlayerInTableRes.Builder playerRes = player.buildPlayInTableInfo(isrecover);
            if (player.getUserId() == userId) {
                playerRes.addAllHandCardIds(player.getHandPais());
                if (!player.getHandMajiang().isEmpty() && player.getHandMajiang().size() % 3 == 1) {
                    if (player.isOkPlayer() && HzMjTool.isTing(player.getHandMajiang(), isHu7dui())) {
                        playerRes.setUserSate(3);
                    }
                }
            }

            if (player.getSeat() == disCardSeat && nowDisCardIds != null) {
                playerRes.addAllOutCardIds(HzMjHelper.toMajiangIds(nowDisCardIds));
            }
            playerRes.addRecover(player.getIsEntryTable());
            playerRes.addRecover(player.getSeat() == lastWinSeat ? 1 : 0);
            if (actionSeatMap.containsKey(player.getSeat())) {
                if (!tempActionMap.containsKey(player.getSeat()) && !huConfirmList.contains(player.getSeat())) {// 如果已做临时操作 则不发送前端可做的操作 或者已经操作胡了
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
        clearMoTailPai();
        readyTime = 0 ;
        autoPlayDiss= false;
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
        HzMjPlayer player = seatMap.get(seat);
        addPlayLog(disCardRound + "_" + seat + "_" + HzMjDisAction.action_hasAction + "_" + StringUtil.implode(actionlist) + player.getExtraPlayLog());
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
            nowDisCardIds = HzMjHelper.toMajiang(StringUtil.explodeToIntList(info.getNowDisCardIds()));
        }

        if (!StringUtils.isBlank(info.getLeftPais())) {
            try {
                leftMajiangs = HzMjHelper.toMajiang(StringUtil.explodeToIntList(info.getLeftPais()));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    @Override
    public void initExtend0(JsonWrapper wrapper) {
        for (HzMjPlayer player : seatMap.values()) {
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
            moGang = HzMj.getMajang(moGangMajiangId);
        }
        String moGangHu = wrapper.getString(9);
        if (!StringUtils.isBlank(moGangHu)) {
            moGangHuList = StringUtil.explodeToIntList(moGangHu);
        }
        dianPaoZimo = wrapper.getInt(10, 1);
        isCalcBanker = wrapper.getInt(11, 0);
        hu7dui = wrapper.getInt(12, 0);
        tempActionMap = loadTempActionMap(wrapper.getString("tempActions"));
        maxPlayerCount = wrapper.getInt(13, 4);
        dealDice = wrapper.getInt(14, 0);
        zhuaJiJiangJi = wrapper.getInt(15, 0);
        yiNiaoQuanZhong = wrapper.getInt(16, 0);
        niaoFen = wrapper.getInt(17, 1);
        wuHongZhongJiaNiao = wrapper.getInt(18, 1);
        youPaoBiHu = wrapper.getInt(19, 0);
        if (dianPaoZimo == 0) {
            qiangGangHu = wrapper.getInt(20, 1);
        } else {
            qiangGangHu = wrapper.getInt(20, 0);
        }
        qiangGangHuBaoSanJia = wrapper.getInt(21, 0);
        isAutoPlay = wrapper.getInt(22, 0);
        if(isAutoPlay==1) {
        	isAutoPlay = 60;
        }
        dianGangKeHu = wrapper.getInt(23, 0);
        moGangSeat = wrapper.getInt(24, 0);
        moGangSameCount = wrapper.getInt(25, 0);

        String moTailPaiStr = wrapper.getString(26);
        if (!StringUtils.isBlank(moTailPaiStr)) {
            moTailPai = StringUtil.explodeToIntList(moTailPaiStr);
        }
        kePiao = wrapper.getInt(27, 0);
        diFen = wrapper.getInt(28, 1);
        zhongNiao159 = wrapper.getInt(29, 0);
        jiaBei = wrapper.getInt(30, 0);
        jiaBeiFen = wrapper.getInt(31, 0);
        jiaBeiShu = wrapper.getInt(32, 0);
        
        
        noHzZimo=  wrapper.getInt(33, 0);
        noHzPao= wrapper.getInt(34, 0);
        noHzQPQ= wrapper.getInt(35, 0);
        autoPlayGlob=  wrapper.getInt(36, 0);
        
        wuHongZhongJiaBei = wrapper.getInt(37, 0);
        ziMoFen = wrapper.getInt(38, 2);
        
        sihongzHu = wrapper.getInt(39, 0);
        buzhongzhong = wrapper.getInt(40, 0);
        baahongz = wrapper.getInt(41, 0);
        
        zimoBihu = wrapper.getInt(42, 0);
        below = wrapper.getInt(43, 0);
        belowAdd = wrapper.getInt(44, 0);

        
        hongzhongBJP  = wrapper.getInt(45, 0);
        
        
        noYiPaoDuoX = wrapper.getInt( 46, 0);
        ztJsGangFen = wrapper.getInt(47, 0);
        
        
        //亲友圈开房数据修复
        if (isGroupRoom() && getServerKey().contains("_")) {
            try {
                String[] temps = getServerKey().split("_");
                if (temps.length >= 2) {
                    GroupTable groupTable = GroupDao.getInstance().loadGroupTableByKeyId(temps[1]);
                    if(groupTable!=null&& groupTable.getTableMsg()!=null) {
                    	JSONObject jsonObject = JSONObject.parseObject(groupTable.getTableMsg());
            			String ints = jsonObject.getString("ints");
            			if (!StringUtils.isBlank(ints)) {
            				String[] strArr = ints.split(",");
            				if(strArr.length>23){
            					noHzZimo = NumberUtils.toInt(strArr[23]);
            				}
            				if(strArr.length>24){
            					noHzPao = NumberUtils.toInt(strArr[24]);
            				}
            				if(strArr.length>25){
            					noHzQPQ = NumberUtils.toInt(strArr[25]);
            				}
            				if(strArr.length>26){
            					autoPlayGlob = NumberUtils.toInt(strArr[26]);
            				}
            				
            			}
                    }
                }
            } catch (Throwable t) {
                LogUtil.errorLog.error("Throwable:" + t.getMessage(), t);
            }
            }
    

    }

    private Map<Integer, HzMjTempAction> loadTempActionMap(String json) {
        Map<Integer, HzMjTempAction> map = new ConcurrentHashMap<>();
        if (json == null || json.isEmpty())
            return map;
        JSONArray jsonArray = JSONArray.parseArray(json);
        for (Object val : jsonArray) {
            String str = val.toString();
            HzMjTempAction tempAction = new HzMjTempAction();
            tempAction.initData(str);
            map.put(tempAction.getSeat(), tempAction);
        }
        return map;
    }

    /**
     * 检查优先度 如果同时出现一个事件，按出牌座位顺序优先
     */
    private boolean checkAction(HzMjPlayer player, List<HzMj> cardList, List<Integer> hucards, int action) {
        boolean canAction = checkCanAction(player, action);// 是否优先级最高 能执行操作
        if (!canAction) {// 不能操作时  存入临时操作
            int seat = player.getSeat();
            tempActionMap.put(seat, new HzMjTempAction(seat, action, cardList, hucards));
            // 玩家都已选择自己的临时操作后  选取优先级最高
            if (tempActionMap.size() == actionSeatMap.size()) {
                int maxAction = Integer.MAX_VALUE;
                int maxSeat = 0;
                Map<Integer, Integer> prioritySeats = new HashMap<>();
                int maxActionSize = 0;
                for (HzMjTempAction temp : tempActionMap.values()) {
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
                HzMjPlayer tempPlayer = seatMap.get(maxSeat);
                List<HzMj> tempCardList = tempActionMap.get(maxSeat).getCardList();
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
    private void refreshTempAction(HzMjPlayer player) {
        tempActionMap.remove(player.getSeat());
        Map<Integer, Integer> prioritySeats = new HashMap<>();//各位置优先操作
        for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
            int seat = entry.getKey();
            List<Integer> actionList = entry.getValue();
            List<Integer> list = HzMjDisAction.parseToDisActionList(actionList);
            int priorityAction = HzMjDisAction.getMaxPriorityAction(list);
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
        Iterator<HzMjTempAction> iterator = tempActionMap.values().iterator();
        while (iterator.hasNext()) {
            HzMjTempAction tempAction = iterator.next();
            if (tempAction.getSeat() == maxPrioritySeat) {
                int action = tempAction.getAction();
                List<HzMj> tempCardList = tempAction.getCardList();
                List<Integer> tempHuCards = tempAction.getHucards();
                HzMjPlayer tempPlayer = seatMap.get(tempAction.getSeat());
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
    public boolean checkCanAction(HzMjPlayer player, int action) {
        // 优先度为胡杠补碰吃
        List<Integer> stopActionList = HzMjDisAction.findPriorityAction(action);
        for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
            if (player.getSeat() != entry.getKey()) {
                // 别人
                boolean can = HzMjDisAction.canDisMajiang(stopActionList, entry.getValue());
                if (!can) {
                    return false;
                }
                List<Integer> disActionList = HzMjDisAction.parseToDisActionList(entry.getValue());
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
    private boolean canPeng(HzMjPlayer player, List<HzMj> majiangs, int sameCount) {
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
    private boolean canAnGang(HzMjPlayer player, List<HzMj> majiangs, int sameCount) {
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
    private boolean canMingGang(HzMjPlayer player, List<HzMj> majiangs, int sameCount) {
        List<HzMj> handMajiangs = player.getHandMajiang();
        List<Integer> pengList = HzMjHelper.toMajiangVals(player.getPeng());

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
    public void setMoGang(HzMj moGang, List<Integer> moGangHuList, HzMjPlayer player, int sameCount) {
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
        if (getDianPaoZimo() == 0) {
            return true;
        }
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

        List<ClosingMjPlayerInfoRes> list = new ArrayList<>();
        List<ClosingMjPlayerInfoRes.Builder> builderList = new ArrayList<>();
        int fangPaoSeat = selfMo ? 0 : disCardSeat;
        for (HzMjPlayer player : seatMap.values()) {
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
                	//无红中放炮+1分
                	 if(noHzPao==1&& !player.haveHongzhong()) {
                		 build.addExt(1);
                     }else{
                    	 build.addExt(0);
                     }
                    // 不是自摸
                    HzMj huMajiang = nowDisCardIds.get(0);
                    if (!build.getHandPaisList().contains(huMajiang.getId())) {
                        build.addHandPais(huMajiang.getId());
                    }
                    build.setIsHu(huMajiang.getId());
                } else {
                	//无红中自摸+1分
                	if(noHzZimo==1&& !player.haveHongzhong()) {
                		build.addExt(2);
                     }else{
                    	 build.addExt(0);
                     }
                	 int hongZhongCount = HzMjQipaiTool.getMajiangCount(player.getHandMajiang(), HzMj.getHongZhongVal());
                	 if(hongZhongCount>=4 && isSiBaHZ()) {
                		 build.setIsHu(HzMj.mj201.getId());
                	 }else {
                		 build.setIsHu(player.getLastMoMajiang().getId());
                	 } 
                   
                   
                }
            }
            if (player.getSeat() == fangPaoSeat) {
                build.setFanPao(1);
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
            for (HzMjPlayer player : seatMap.values()) {
                if (player.getWinLoseCredit() > dyjCredit) {
                    dyjCredit = player.getWinLoseCredit();
                }
            }
            for (ClosingMjPlayerInfoRes.Builder builder : builderList) {
                HzMjPlayer player = seatMap.get(builder.getSeat());
                calcCommissionCredit(player, dyjCredit);
                builder.setWinLoseCredit(player.getWinLoseCredit());
                builder.setCommissionCredit(player.getCommissionCredit());
            }
        } else if (isGroupTableGoldRoom()) {
            // -----------亲友圈金币场---------------------------------

            for (HzMjPlayer player : seatMap.values()) {
                player.setWinGold(player.getTotalPoint() * gtgDifen);
            }
            calcGroupTableGoldRoomWinLimit();
            for (ClosingMjPlayerInfoRes.Builder builder : builderList) {
                HzMjPlayer player = seatMap.get(builder.getSeat());
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
        res.addAllLeftCards(HzMjHelper.toMajiangIds(leftMajiangs));
        res.setCatchBirdSeat(catchBirdSeat);
        for (HzMjPlayer player : seatMap.values()) {
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
        ext.add(id + "");                               //1
        ext.add(masterId + "");                         //2
        ext.add(TimeUtil.formatTime(createTime));       //3
        ext.add(playType + "");                         //4
        ext.add(dianPaoZimo + "");                      //5
        ext.add(birdNum + "");                          //6
        ext.add(isCalcBanker + "");                     //7
        ext.add(hu7dui + "");                           //8
        ext.add(isAutoPlay + "");                       //9
        ext.add(qiangGangHu + "");                      //10
        ext.add(qiangGangHuBaoSanJia + "");             //11
        ext.add(zhuaJiJiangJi + "");                    //12
        ext.add(yiNiaoQuanZhong + "");                  //13
        ext.add(niaoFen + "");                          //14
        ext.add(wuHongZhongJiaNiao + "");               //15
        ext.add(youPaoBiHu + "");                       //16
        ext.add(dianGangKeHu + "");                     //17
        ext.add(kePiao + "");                           //18
        ext.add(diFen + "");                            //19
        ext.add(isLiuJu() + "");                        //20
        ext.add(zhongNiao159 + "");                     //21
        ext.add(lastWinSeat + "");                        //22
        ext.add(jiaBei + "");              //23
        ext.add(jiaBeiFen + "");           //24
        ext.add(jiaBeiShu + "");           //25
        ext.add(String.valueOf(playedBureau));//26
        ext.add(over + "");           //27
        return ext;
    }

    @Override
    public void sendAccountsMsg() {

        if (ztJsGangFen == 1) {
            for (HzMjPlayer seat : seatMap.values()) {
                seat.changePoint(seat.getLostPoint());
                logHuPoint(seat);
            }
        }

        calcPointBeforeOver();
        ClosingMjInfoRes.Builder builder = sendAccountsMsg(true, false, null, null, null, null, 0, true);
        saveLog(true, 0l, builder.build());
    }

    @Override
    public Class<? extends Player> getPlayerClass() {
        return HzMjPlayer.class;
    }

    @Override
    public int getWanFa() {
        return getPlayType();
    }

//	@Override
//	public boolean isTest() {
//		return HzMjConstants.isTest;
//	}

    @Override
    public void checkReconnect(Player player) {
        if (super.isAllReady() && getKePiao() > 0 && getTableStatus() == HzMjConstants.TABLE_STATUS_PIAO) {
            HzMjPlayer player1 = (HzMjPlayer) player;
            if (player1.getPiaoPoint() < 0) {
                ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_table_status_piao, getTableStatus());
                player1.writeSocket(com.build());
                return;
            }
            if (player1.getHandPais() != null && player1.getHandPais().size() > 0) {
                sendTingInfo(player1);
            }
        }
        if (state == table_state.play) {
            HzMjPlayer player1 = (HzMjPlayer) player;
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
            for (HzMjPlayer player : seatMap.values()) {
                if (player.getLastCheckTime() > 0) {
                    player.setLastCheckTime(player.getLastCheckTime() + 1 * 1000);
                }
            }
            return;
        }
        

        if(zimoBihu==1){
        	for(HzMjPlayer player : seatMap.values()){
        		List<Integer> actionList = actionSeatMap.get(player.getSeat());
        		if(actionList!= null &&(actionList.get(HzMjConstants.ACTION_INDEX_HU) == 1 || actionList.get(HzMjConstants.ACTION_INDEX_ZIMO) == 1)) {
                         // 胡
                         playCommand(player, new ArrayList<HzMj>(), HzMjDisAction.action_hu);
        		}
        	}
        	
        }
        
        
        if (isAutoPlay < 1) {
            return;
        }

        if (isAutoPlayOff()) {
            // 托管关闭
            for (int seat : seatMap.keySet()) {
                HzMjPlayer player = seatMap.get(seat);
                player.setAutoPlay(false, false);
                player.setCheckAutoPlay(false);
            }
            return;
        }

        if (getTableStatus() == HzMjConstants.TABLE_STATUS_PIAO) {
//            for (int seat : seatMap.keySet()) {
//                HzMjPlayer player = seatMap.get(seat);
//                if (!player.checkAutoPlay(2, false)) {
//                    continue;
//                }
//                autoPiao(player);
//            }
        	
        	
        	
        	
        	for (int seat : seatMap.keySet()) {
        		HzMjPlayer player = seatMap.get(seat);
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
				HzMjPlayer player = seatMap.get(seat);
				if (player.getPiaoPoint() < 0) {
					piao = false;
				}

			}
			if (piao) {
				setTableStatus(HzMjConstants.AUTO_PLAY_TIME);
			}
        	
        } else if (state == table_state.play) {
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
            //开了托管的房间，xx秒后自动开始下一局
            for (HzMjPlayer player : seatMap.values()) {
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
                    HzMjPlayer player = seatMap.get(seat);
                    if (player == null) {
                        continue;
                    }
                    if (!player.checkAutoPlay(2, false)) {
                        continue;
                    }
                    playCommand(player, new ArrayList<>(), HzMjDisAction.action_hu);
                }
                return;
            } else {
                int action, seat;
                for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
                    List<Integer> actList = HzMjDisAction.parseToDisActionList(entry.getValue());
                    if (actList == null) {
                        continue;
                    }
                    seat = entry.getKey();
                    action = HzMjDisAction.getAutoMaxPriorityAction(actList);
                    HzMjPlayer player = seatMap.get(seat);
                    if (!player.checkAutoPlay(0, false)) {
                        continue;
                    }
                    boolean chuPai = false;
                    if (player.isAlreadyMoMajiang()) {
                        chuPai = true;
                    }
                    if (action == HzMjDisAction.action_peng) {
                        if (player.isAutoPlaySelf()) {
                            //自己开启托管直接过
                            playCommand(player, new ArrayList<>(), HzMjDisAction.action_pass);
                            if (chuPai) {
                                autoChuPai(player);
                            }
                        } else {
                            if (nowDisCardIds != null && !nowDisCardIds.isEmpty()) {
                                HzMj mj = nowDisCardIds.get(0);
                                List<HzMj> mjList = new ArrayList<>();
                                for (HzMj handMj : player.getHandMajiang()) {
                                    if (handMj.getVal() == mj.getVal()) {
                                        mjList.add(handMj);
                                        if (mjList.size() == 2) {
                                            break;
                                        }
                                    }
                                }
                                playCommand(player, mjList, HzMjDisAction.action_peng);
                            }
                        }
                    } else {
                        playCommand(player, new ArrayList<>(), HzMjDisAction.action_pass);
                        if (chuPai) {
                            autoChuPai(player);
                        }
                    }
                }
            }
        } else {
            HzMjPlayer player = seatMap.get(nowDisCardSeat);
            if (player == null || !player.checkAutoPlay(0, false)) {
                return;
            }
            autoChuPai(player);
        }
    }

    public void autoChuPai(HzMjPlayer player) {

        if (!player.isAlreadyMoMajiang()) {
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
        HzMj mj = HzMj.getMajang(mjId);
        if (mj != null && mj.getVal() == HzMj.getHongZhongVal()) {
            mjId = -1;
            index--;
            Collections.sort(handMjIds);
        }
        while (mjId == -1 && index >= 0) {
            mjId = handMjIds.get(index);
            mj = HzMj.getMajang(mjId);
            if (mj != null && mj.getVal() == HzMj.getHongZhongVal()) {
                mjId = -1;
                index--;
            }
        }
        if (mjId != -1) {
            List<HzMj> mjList = HzMjHelper.toMajiang(Arrays.asList(mjId));
            playCommand(player, mjList, HzMjDisAction.action_chupai);
        }
    }

    public void autoPiao(HzMjPlayer player) {
        int piaoPoint = 0;
        if (getTableStatus() != HzMjConstants.TABLE_STATUS_PIAO) {
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

	private void sendPiaoPoint(HzMjPlayer player, int piaoPoint) {
		ComMsg.ComRes.Builder build = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_piao_fen, player.getSeat(), piaoPoint);
        broadMsg(build.build());
        broadMsgRoomPlayer(build.build());
	}

    public int getIsCalcBanker() {
        return isCalcBanker;
    }

    public void setIsCalcBanker(int isCalcBanker) {
        this.isCalcBanker = isCalcBanker;
    }


    @Override
    public void createTable(Player player, int play, int bureauCount, Object... objects) throws Exception {
    }

    @Override
    public void createTable(Player player, int play, int bureauCount, List<Integer> params, List<String> strParams, Object... objects) throws Exception {
        createTable(new CreateTableInfo(player, TABLE_TYPE_NORMAL, play, bureauCount, params, strParams, true));
    }

    @Override
    public boolean createTable(CreateTableInfo createTableInfo) throws Exception {
        Player player = createTableInfo.getPlayer();
        int play = createTableInfo.getPlayType();
        int bureauCount =createTableInfo.getBureauCount();
        int tableType = createTableInfo.getTableType();
        List<Integer> params = createTableInfo.getIntParams();
        List<String> strParams = createTableInfo.getStrParams();
        boolean saveDb = createTableInfo.isSaveDb();

        long id = getCreateTableId(player.getUserId(), play);
        TableInf info = new TableInf();
        info.setTableType(tableType);
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
        birdNum = StringUtil.getIntValue(params, 3, 0);//抓几个鸟
        dianPaoZimo = StringUtil.getIntValue(params, 4, 0);//点炮胡 自摸胡
        isCalcBanker = StringUtil.getIntValue(params, 5, 0);//分庄闲
        hu7dui = StringUtil.getIntValue(params, 6, 0);//可胡7对
        isAutoPlay = StringUtil.getIntValue(params, 8, 0);
        if(isAutoPlay==1) {
        	isAutoPlay=120;
        }
        autoPlay = (isAutoPlay > 1);
        if (dianPaoZimo == 0) {
            qiangGangHu = StringUtil.getIntValue(params, 9, 1);//抢杠胡
        } else {
            qiangGangHu = StringUtil.getIntValue(params, 9, 0);//抢杠胡
        }
        qiangGangHuBaoSanJia = StringUtil.getIntValue(params, 10, 0);//抢杠胡包三家
        if (qiangGangHuBaoSanJia == 1) {
            qiangGangHu = 1;
        }
        zhuaJiJiangJi = StringUtil.getIntValue(params, 11, 0);//胡几奖几
        yiNiaoQuanZhong = StringUtil.getIntValue(params, 12, 0);//一鸟全中
        niaoFen = StringUtil.getIntValue(params, 13, 1);//鸟分
        wuHongZhongJiaNiao = StringUtil.getIntValue(params, 14, 1);//无红中加鸟
        youPaoBiHu = StringUtil.getIntValue(params, 15, 0);//有炮必胡
        dianGangKeHu = StringUtil.getIntValue(params, 16, 0);//点杠可胡
        kePiao = StringUtil.getIntValue(params, 17, 0);//是否飘分
        diFen = StringUtil.getIntValue(params, 18, 1); //底分
        zhongNiao159 = StringUtil.getIntValue(params, 19, 1); //底分

        this.jiaBei = StringUtil.getIntValue(params, 20, 0);
        this.jiaBeiFen = StringUtil.getIntValue(params, 21, 0);
        this.jiaBeiShu = StringUtil.getIntValue(params, 22, 0);
        
        this.noHzZimo = StringUtil.getIntValue(params, 23, 0);
        this.noHzPao = StringUtil.getIntValue(params, 24, 0);
        this.noHzQPQ = StringUtil.getIntValue(params, 25, 0);
        
        this.autoPlayGlob = StringUtil.getIntValue(params, 26, 0);

        wuHongZhongJiaBei = StringUtil.getIntValue(params, 27, 0);
        ziMoFen = StringUtil.getIntValue(params, 28, 2);
        sihongzHu = StringUtil.getIntValue(params, 29, 0);
        buzhongzhong = StringUtil.getIntValue(params, 30, 0);
        
        baahongz = StringUtil.getIntValue(params, 31, 0);
        
        zimoBihu = StringUtil.getIntValue(params, 32, 0);//自摸必胡
        
        if(diFen<=0){
        	diFen = 1;
        }

        if(playerCount==2){
            int belowAdd = StringUtil.getIntValue(params, 33, 0);
            if(belowAdd<=100&&belowAdd>=0)
                this.belowAdd=belowAdd;
            int below = StringUtil.getIntValue(params, 34, 0);
            if(below<=100&&below>=0){
                this.below=below;
                if(belowAdd>0&&below==0)
                    this.below=10;
            }
        }
        
        hongzhongBJP = StringUtil.getIntValue(params, 35, 0);
        noYiPaoDuoX = StringUtil.getIntValue(params, 36, 0);
        ztJsGangFen = StringUtil.getIntValue(params, 37, 0);
        
        
        
        
        if(ziMoFen != 1 && ziMoFen != 2){
            ziMoFen = 2;
        }

        if(niaoFen==0) {
        	 this.niaoFen = 2;
        }
        wuHongZhongJiaNiao = wuHongZhongJiaNiao < 0 ? 0 : (wuHongZhongJiaNiao > 2 ? 2 : wuHongZhongJiaNiao);
        if (zhuaJiJiangJi == 1) {
            birdNum = 0;
        }
        if (playerCount != 2) {
            jiaBei = 0;
        }
        if (yiNiaoQuanZhong == 1) {
            birdNum = 1;
        }
        
        setMaxPlayerCount(playerCount);
        setPayType(payType);
        changeExtend();
        if (!isJoinPlayerAllotSeat()) {
//            getRoomModeMap().put("1", "1"); //可观战（默认）
        }
        return true;
    }

    public static final List<Integer> wanfaList = Arrays.asList(
            GameUtil.game_type_hzmj);

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
        sb.append("HzMj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append("faPai");
        sb.append("|").append(playType);
        sb.append("|").append(maxPlayerCount);
        sb.append("|").append(getPayType());
        sb.append("|").append(lastWinSeat);
        LogUtil.msg(sb.toString());
    }

    public void logFaPaiPlayer(HzMjPlayer player, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("HzMj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append("faPai");
        sb.append("|").append(player.getHandMajiang());
        sb.append("|").append(actListToString(actList));
        LogUtil.msg(sb.toString());
    }

    public void logAction(HzMjPlayer player, int action, List<HzMj> mjs, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("HzMj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        String actStr = "unKnown-" + action;
        if (action == HzMjDisAction.action_peng) {
            actStr = "peng";
        } else if (action == HzMjDisAction.action_minggang) {
            actStr = "mingGang";
        } else if (action == HzMjDisAction.action_chupai) {
            actStr = "chuPai";
        } else if (action == HzMjDisAction.action_pass) {
            actStr = "guo";
        } else if (action == HzMjDisAction.action_angang) {
            actStr = "anGang";
        } else if (action == HzMjDisAction.action_chi) {
            actStr = "chi";
        }
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append(player.isAutoPlaySelf() ? 1 : 0);
        sb.append("|").append(actStr);
        sb.append("|").append(mjs);
        sb.append("|").append(actListToString(actList));
        LogUtil.msg(sb.toString());
    }

    public void logMoMj(HzMjPlayer player, HzMj mj, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("HzMj");
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

    public void logChuPaiActList(HzMjPlayer player, HzMj mj, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("HzMj");
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

    public void logActionHu(HzMjPlayer player, List<HzMj> mjs, String daHuNames) {
        StringBuilder sb = new StringBuilder();
        sb.append("HzMj");
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

    public void logHuPoint(HzMjPlayer player) {
        StringBuilder sb = new StringBuilder();
        sb.append("HzMj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append("huPoint");
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append(player.isAutoPlaySelf() ? 1 : 0);
        sb.append("|").append(player.getHandMajiang());
        sb.append("|").append(StringUtil.implode(player.getPointArr(), ","));
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
                if (i == HzMjConstants.ACTION_INDEX_HU) {
                    sb.append("hu");
                } else if (i == HzMjConstants.ACTION_INDEX_PENG) {
                    sb.append("peng");
                } else if (i == HzMjConstants.ACTION_INDEX_MINGGANG) {
                    sb.append("mingGang");
                } else if (i == HzMjConstants.ACTION_INDEX_ANGANG) {
                    sb.append("anGang");
                } else if (i == HzMjConstants.ACTION_INDEX_CHI) {
                    sb.append("chi");
                } else if (i == HzMjConstants.ACTION_INDEX_ZIMO) {
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

    public int getKePiao() {
        return kePiao;
    }

    @Override
    public boolean isAllReady() {
        if (super.isAllReady()) {
            if (getKePiao() > 0) {
//                setTableStatus(HzMjConstants.TABLE_STATUS_PIAO);
                boolean bReturn = true;
                //机器人默认处理
                if (this.isTest()) {
                    for (HzMjPlayer robotPlayer : seatMap.values()) {
                        if (robotPlayer.isRobot()) {
                            robotPlayer.setPiaoPoint(1);
                        }
                    }
                }
                ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_table_status_piao, getTableStatus());
                for (HzMjPlayer player : seatMap.values()) {
                    if (player.getPiaoPoint() < 0) {
                    	if(getKePiao()==3){
                    		player.setPiaoPoint(1);
                    	}else if(getKePiao()==4){
                    		player.setPiaoPoint(2);
                    	}else if(getKePiao()==5){
                    		player.setPiaoPoint(3);
                    	}else {
                    		 player.writeSocket(com.build());
                             bReturn = false;
                    	}
                    	if(getKePiao()>=3) {
                    		 sendPiaoPoint(player, player.getPiaoPoint());
                    	}
                    	if (getTableStatus() != HzMjConstants.TABLE_STATUS_PIAO) {
							player.setLastCheckTime(System.currentTimeMillis());
						}
                       
                    }
                }
                
                setTableStatus(HzMjConstants.TABLE_STATUS_PIAO);
                if (!bReturn) {
                    broadMsgRoomPlayer(com.build());
                }
                return bReturn;
            } else {
                for (HzMjPlayer player : seatMap.values()) {
                    player.setPiaoPoint(0);
                }
                return true;
            }
        }
        return false;
    }

	@Override
	public boolean isPlaying() {
		if (super.isPlaying()) {
			return true;
		}
		return getTableStatus() == HzMjConstants.TABLE_STATUS_PIAO;
	}

    public void setTableStatus(int tableStatus) {
        this.tableStatus = tableStatus;
    }

    public int getTableStatus() {
        return tableStatus;
    }

    /**
     * 是否流局
     *
     * @return
     */
    public int isLiuJu() {
        return (huConfirmList.size() == 0 && leftMajiangs.size() == 0) ? 1 : 0;
    }

    
    
    public int getSihongzHu() {
		return sihongzHu;
	}

	public int getBaahongz() {
		return baahongz;
	}
	
	
	public boolean isSiBaHZ(){
		//||baahongz==1
		return (sihongzHu==1) &&getDisCardRound()==0;
	}

	public void sendTingInfoOld(HzMjPlayer player) {
        if (player.isAlreadyMoMajiang()) {
            if (actionSeatMap.containsKey(player.getSeat())) {
                return;
            }
            DaPaiTingPaiRes.Builder tingInfo = DaPaiTingPaiRes.newBuilder();
            List<HzMj> cards = new ArrayList<>(player.getHandMajiang());
            int hzCount = HzMjTool.dropHongzhong(cards).size();
            Map<Integer, List<HzMj>> checked = new HashMap<>();
            for (HzMj card : cards) {
                if (card.isHongzhong()) {
                    continue;
                }
                List<HzMj> lackPaiList;
                if (checked.containsKey(card.getVal())) {
                    lackPaiList = checked.get(card.getVal());
                } else {
                    List<HzMj> copy = new ArrayList<>(cards);
                    copy.remove(card);
                    lackPaiList = HzMjTool.getLackListOld(copy, hzCount, hu7dui == 1);
                    if (lackPaiList.size() > 0) {
                        checked.put(card.getVal(), lackPaiList);
                    } else {
                        continue;
                    }
                }

                DaPaiTingPaiInfo.Builder ting = DaPaiTingPaiInfo.newBuilder();
                ting.setMajiangId(card.getId());
                if (lackPaiList.size() == 1 && null == lackPaiList.get(0)) {
                    //听所有
                    ting.addTingMajiangIds(HzMj.mj201.getId());
                } else {
                    for (HzMj lackPai : lackPaiList) {
                        ting.addTingMajiangIds(lackPai.getId());
                    }
                    ting.addTingMajiangIds(HzMj.mj201.getId());
                }
                tingInfo.addInfo(ting.build());
            }
            if (tingInfo.getInfoCount() > 0) {
                player.writeSocket(tingInfo.build());
            }
        } else {
            List<HzMj> cards = new ArrayList<>(player.getHandMajiang());
            int hzCount = HzMjTool.dropHongzhong(cards).size();
            List<HzMj> lackPaiList = HzMjTool.getLackListOld(cards, hzCount, hu7dui == 1);
            if (lackPaiList == null || lackPaiList.size() == 0) {
                return;
            }
            TingPaiRes.Builder ting = TingPaiRes.newBuilder();
            if (lackPaiList.size() == 1 && null == lackPaiList.get(0)) {
                //听所有
                ting.addMajiangIds(HzMj.mj201.getId());
            } else {
                for (HzMj lackPai : lackPaiList) {
                    ting.addMajiangIds(lackPai.getId());
                }
                ting.addMajiangIds(HzMj.mj201.getId());
            }
            player.writeSocket(ting.build());
        }

    }

    public void sendTingInfo(HzMjPlayer player) {
        if (player.isAlreadyMoMajiang()) {
            if (actionSeatMap.containsKey(player.getSeat())) {
                return;
            }
            DaPaiTingPaiRes.Builder tingInfo = DaPaiTingPaiRes.newBuilder();
            List<HzMj> cards = new ArrayList<>(player.getHandMajiang());
            int hzCount = HzMjTool.dropHongzhong(cards).size();
            int[] cardArr = HuUtil.toCardArray(cards);
            Map<Integer, List<HzMj>> checked = new HashMap<>();
            for (HzMj card : cards) {
                if (card.isHongzhong()) {
                    continue;
                }
                List<HzMj> lackPaiList;
                if (checked.containsKey(card.getVal())) {
                    lackPaiList = checked.get(card.getVal());
                } else {
                    int cardIndex = HuUtil.getMjIndex(card);
                    cardArr[cardIndex] = cardArr[cardIndex] - 1;
                    lackPaiList = HzMjTool.getLackList(cardArr, hzCount, hu7dui == 1);
                    cardArr[cardIndex] = cardArr[cardIndex] + 1;
                    if (lackPaiList.size() > 0) {
                        checked.put(card.getVal(), lackPaiList);
                    } else {
                        continue;
                    }
                }

                DaPaiTingPaiInfo.Builder ting = DaPaiTingPaiInfo.newBuilder();
                ting.setMajiangId(card.getId());
                if (lackPaiList.size() == 1 && null == lackPaiList.get(0)) {
                    //听所有
                    ting.addTingMajiangIds(HzMj.mj201.getId());
                } else {
                    for (HzMj lackPai : lackPaiList) {
                        ting.addTingMajiangIds(lackPai.getId());
                    }
                    ting.addTingMajiangIds(HzMj.mj201.getId());
                }
                tingInfo.addInfo(ting.build());
            }
            if (tingInfo.getInfoCount() > 0) {
                player.writeSocket(tingInfo.build());
            }
        } else {
            List<HzMj> cards = new ArrayList<>(player.getHandMajiang());
            int hzCount = HzMjTool.dropHongzhong(cards).size();
            int[] cardArr = HuUtil.toCardArray(cards);
            List<HzMj> lackPaiList = HzMjTool.getLackList(cardArr, hzCount, hu7dui == 1);
            if (lackPaiList == null || lackPaiList.size() == 0) {
                return;
            }
            TingPaiRes.Builder ting = TingPaiRes.newBuilder();
            if (lackPaiList.size() == 1 && null == lackPaiList.get(0)) {
                //听所有
                ting.addMajiangIds(HzMj.mj201.getId());
            } else {
                for (HzMj lackPai : lackPaiList) {
                    ting.addMajiangIds(lackPai.getId());
                }
                ting.addMajiangIds(HzMj.mj201.getId());
            }
            player.writeSocket(ting.build());
        }
    }
    
    

    public int getDiFen() {
		return diFen;
	}

	public String getTableMsg() {
        Map<String, Object> json = new HashMap<>();
        json.put("wanFa", "红中麻将");
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
        return "红中麻将";
    }
}
