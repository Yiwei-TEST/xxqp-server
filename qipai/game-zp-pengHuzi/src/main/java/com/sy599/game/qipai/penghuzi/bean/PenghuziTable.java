package com.sy599.game.qipai.penghuzi.bean;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sy599.game.GameServerConfig;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.bean.CreateTableInfo;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.common.constant.SharedConstants.player_state;
import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.db.bean.DataStatistics;
import com.sy599.game.db.bean.PlayLogTable;
import com.sy599.game.db.bean.TableInf;
import com.sy599.game.db.bean.UserGroupPlaylog;
import com.sy599.game.db.bean.UserPlaylog;
import com.sy599.game.db.dao.DataStatisticsDao;
import com.sy599.game.db.dao.TableDao;
import com.sy599.game.db.dao.TableLogDao;
import com.sy599.game.db.dao.gold.GoldRoomDao;
import com.sy599.game.manager.TableManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.ComMsg.ComRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.DaPaiTingPaiInfo;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.DaPaiTingPaiRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.PlayPaohuziRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.TingPaiRes;
import com.sy599.game.msg.serverPacket.TablePhzResMsg.ClosingPhzInfoRes;
import com.sy599.game.msg.serverPacket.TablePhzResMsg.ClosingPhzPlayerInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.CreateTableRes;
import com.sy599.game.msg.serverPacket.TableRes.DealInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes;
import com.sy599.game.qipai.penghuzi.constant.PengHZMingTang;
import com.sy599.game.qipai.penghuzi.constant.PenghuziConstant;
import com.sy599.game.qipai.penghuzi.constant.PenghzCard;
import com.sy599.game.qipai.penghuzi.rule.PenghuziIndex;
import com.sy599.game.qipai.penghuzi.rule.PenghzCardIndexArr;
import com.sy599.game.qipai.penghuzi.rule.RobotAI;
import com.sy599.game.qipai.penghuzi.tool.PenghuziHuLack;
import com.sy599.game.qipai.penghuzi.tool.PenghuziResTool;
import com.sy599.game.qipai.penghuzi.tool.PenghuziTool;
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
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.util.StringUtil;
import com.sy599.game.util.TimeUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

public class PenghuziTable extends BaseTable {
	/*** 玩家map */
    private Map<Long, PenghuziPlayer> playerMap = new ConcurrentHashMap<>();
	/*** 座位对应的玩家 */
    private Map<Integer, PenghuziPlayer> seatMap = new ConcurrentHashMap<>();
    /**
	 * 开局所有底牌
	 **/
    private volatile List<Integer> startLeftCards = new ArrayList<>();
    /**
	 * 当前桌面底牌
	 **/
    private volatile List<PenghzCard> leftCards = new ArrayList<>();
	/*** 摸牌flag */
    private volatile int moFlag;
	/*** 应该要打牌的flag */
    private volatile int toPlayCardFlag;
    private volatile PenghuziCheckCardBean autoDisBean;
    private volatile int moSeat;
    private volatile PenghzCard zaiCard;
    private volatile PenghzCard beRemoveCard;
    private volatile int maxPlayerCount = 3;
    private volatile List<Integer> huConfirmList = new ArrayList<>();
	/*** 摸牌时对应的座位 */
    private volatile KeyValuePair<Integer, Integer> moSeatPair;
	/*** 摸牌时对应的座位 */
    private volatile KeyValuePair<Integer, Integer> checkMoMark;
    private volatile int sendPaoSeat;
    private volatile boolean firstCard = true;
    private volatile int ceiling =0;
    /**
	 * 0胡 1碰 2栽 3提 4吃 5跑 6臭栽
	 */
    private Map<Integer, List<Integer>> actionSeatMap = new ConcurrentHashMap<>();
    private volatile List<PenghzCard> nowDisCardIds = new ArrayList<>();
	// 1连庄 2 连庄x2
    private volatile int isLianBanker;

	private volatile int catCardCount = 0;// 抽掉的牌数量
	// 抽掉的牌堆
    private List<Integer> chouCards=new ArrayList<>();
    
    
	// 1先进房做庄2 随机装
    private volatile int suiJiZhuang;
    
	// 1强制胡牌2 不强制
    private volatile int qiangzhiHu;
    
	private int tableStatus;// 特殊状态 1飘分
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
    
	/** 托管1：单局，2：全局 */
    private int autoPlayGlob;

	private int autoTableCount;

    private volatile int timeNum = 0;
    
	private int disCardCout1;// 
    

    /**
	 * 玩家位置对应临时操作 当同时存在多个可做的操作时 1当优先级最高的玩家最先操作 则清理所有操作提示 并执行该操作
	 * 2当操作优先级低的玩家先选择操作，则玩家先将所做操作存入临时操作中 当玩家优先级最高的玩家操作后 在判断临时操作是否可执行并执行
	 */
    private Map<Integer, TempAction> tempActionMap = new ConcurrentHashMap<>();
	// 是否完成发牌
    private int finishFapai=0;
    //低于below加分
    private int belowAdd=0;
    private int below=0;

    //跑胡
    private int paoHu=0;
    
    
    //乱位
    private int randomSeat=0;
    
    private int noDui=0;
    
    private int fanZhongZhuang=0;
    
 
    /**1:勾选打鸟玩法*/
    private int daNiaoWF=0;//
    
    /**
     * 
     * 	1  有庄有鸟:就是A玩家胡牌庄，只要是庄就自动押鸟 3分或6分
		2   围鸟:       必须每个玩家各对押  3分或6分
		3 围鸟加鸟.在原有围鸟基础上再加鸟5分/10分，即8分/16分
		4 自由压鸟:玩家自由选择押多少分  ，牌桌内不押，3，5，6，10 五个选项选择
     * 
     * */
    private int daNiaoVal=0;//
    
    /***
     * 1.小七对带坎：庄家15张牌的7个对子中，多余的那张牌必须和其中的一个对子形成一坎才能胡七对
	 *2.小七对不带坎：庄家15张牌只需要形成7对就能胡七对
     */
    private int xiaoQiDuiWF=0;

    
    
    
    
    

    public boolean getFirstCard(){
    	return firstCard;
    }

	public int getIsLianBanker() {
		return isLianBanker;
	}

	public void setIsLianBanker(int isLianBanker) {
		this.isLianBanker = isLianBanker;
	}

    public int getCeiling() {
        return ceiling;
    }

    public void setCeiling(int ceiling) {
        this.ceiling = ceiling;
        changeExtend();
    }

    public int getFinishFapai() {
        return finishFapai;
    }


    public int getPaoHu() {
        return paoHu;
    }

    public void setPaoHu(int paoHu) {
        this.paoHu = paoHu;
    }

    public void setFinishFapai(int finishFapai) {
        this.finishFapai = finishFapai;
    }

    /**
	 * 获取所有底牌内容
	 */
    public List<Integer> getStartLeftCards() {
        return startLeftCards;
    }

    @Override
    public boolean ready(Player player) {
        boolean flag=super.ready(player);
//        if(playedBureau>0)
//            return flag;
//        int count=0;
//        for(PenghuziPlayer p:seatMap.values()){
//            if (p.getState() == player_state.ready||p.getState() == player_state.play)
//                count++;
//        }
        return flag;
    }

    @Override
    public boolean isAllReady() {
        if (getPlayerCount() < getMaxPlayerCount()) {
            return false;
        }
        if(!super.isAllReady()){
        	return false;
        }
        
        for (Player player : getSeatMap().values()) {
            if(!player.isRobot()){
                    if(player.getState() != player_state.ready)
                        return false;
            }
        }
        
    	if(super.isAllReady()&&playBureau==1&&randomSeat==1){
    		if(getTableStatus()==0){
    			changgeSeat();
    		}
    	}
        
        if (getDaNiaoWF()>= 1) {
			boolean bReturn = true;
			// 机器人默认处理
			if (this.isTest()) {
				for (PenghuziPlayer robotPlayer : seatMap.values()) { 
					if (robotPlayer.isRobot()) {
						robotPlayer.setDaNiaoPoint(1);
					}
				}
			}
			boolean youzyn = false;
			for (PenghuziPlayer player : seatMap.values()) {
				if(getDaNiaoVal()==1){
					if(player.getSeat()==lastWinSeat&&player.getLianZhuangC()>0){
						youzyn = true;
					}
				
				}
			}
			
			for (PenghuziPlayer player : seatMap.values()) {
				if(getDaNiaoVal()==1){
					if(player.getSeat()==lastWinSeat&&player.getLianZhuangC()>0){
//						if(player.getDaNiaoPoint()<0){
//							
//						}
					}else {
						if(player.getDaNiaoPoint()<0){
							player.setDaNiaoPoint(0);
							if(youzyn){
								ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_piao_PENGHU_DANAIO, player.getSeat(), player.getDaNiaoPoint());
								for (Player tableplayer : getSeatMap().values()) {// 推送客户端玩家抛分情况
									tableplayer.writeSocket(com.build());
								}
							}
							
						}
						
					}
				}
				
				
				if (player.getDaNiaoPoint() < 0) {
					ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_table_status_PENGHU_DANAIO,
							getTableStatus());
					player.writeSocket(com.build());
					if (getTableStatus() != PenghuziConstant.TABLE_STATUS_DANIAO) {
						player.setLastCheckTime(System.currentTimeMillis());
					}
					bReturn = false;
					// if(getTableStatus()!=CsMjConstants.TABLE_STATUS_PIAO)
					// {
					//
					// }
				}
			}
			if(!bReturn){
				setTableStatus(PenghuziConstant.TABLE_STATUS_DANIAO);
			}else{
				setTableStatus(0);
			}

			return bReturn;
		} else {
			int point = 0;
			
			for (PenghuziPlayer player : seatMap.values()) {
				player.setDaNiaoPoint(point);
			}
			
			 changeTableState(table_state.play);
			 setTableStatus(0);
			return true;
		}
        
       
        //return true;
    }



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
        String autoDisPhz = wrapper.getString(6);
        if (!StringUtils.isBlank(autoDisPhz)) {
            autoDisBean = new PenghuziCheckCardBean();
            autoDisBean.initAutoDisData(autoDisPhz);
        }
        zaiCard = PenghzCard.getPaohzCard(wrapper.getInt(7, 0));
        sendPaoSeat = wrapper.getInt(8, 0);
        firstCard = wrapper.getInt(9, 1) == 1 ? true : false;
        beRemoveCard = PenghzCard.getPaohzCard(wrapper.getInt(10, 0));
        maxPlayerCount = wrapper.getInt(12, 3);
        startLeftCards = loadStartLeftCards(wrapper.getString("startLeftCards"));
        ceiling = wrapper.getInt(13, 0);
        isLianBanker = wrapper.getInt(15, 0);
        if (payType== -1) {
            String isAAStr =  wrapper.getString("isAAConsume");
            if (!StringUtils.isBlank(isAAStr)) {
                this.payType = Boolean.parseBoolean(wrapper.getString("isAAConsume"))?1:2;
            } else {
                payType=1;
            }
        }

        catCardCount = wrapper.getInt("catCardCount", catCardCount);

        jiaBei = wrapper.getInt(17, 0);
        jiaBeiFen = wrapper.getInt(18, 0);
        jiaBeiShu = wrapper.getInt(19, 0);
        
        autoPlayGlob = wrapper.getInt(20, 0);
        autoTimeOut = wrapper.getInt(21, 0);
        if(autoPlay && autoTimeOut <=1) {
        	autoTimeOut= 60000;
        }
        autoTimeOut2 =autoTimeOut;

        tempActionMap = loadTempActionMap(wrapper.getString("22"));
        finishFapai = wrapper.getInt(24, 0);
        below = wrapper.getInt(25, 0);
        belowAdd = wrapper.getInt(26, 0);
        paoHu = wrapper.getInt(27, 0);
        disCardCout1= wrapper.getInt(28, 0);
        
        
        
        randomSeat= wrapper.getInt(29, 0);
        noDui= wrapper.getInt(30, 0);
        fanZhongZhuang= wrapper.getInt(31, 0);
        daNiaoWF= wrapper.getInt(32, 0);
        xiaoQiDuiWF= wrapper.getInt(33, 0);
        daNiaoVal = wrapper.getInt(34, 0);
        suiJiZhuang= wrapper.getInt(35, 0);
        qiangzhiHu= wrapper.getInt(36, 0);
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
        if (state == table_state.ready) {
            return;
        }
        boolean isHuangZhuang = false;
        List<Integer> winList = new ArrayList<>(huConfirmList);
        
        int dahu = 0;
        PenghuziPlayer wPlayer = null;
        int lastZhangC = 0;//上把庄的连庄次数放炮反中庄用
    	PenghuziPlayer lastPlayer	= seatMap.get(lastWinSeat);
    	
    	
    	//结算显示上一局庄的连庄情况
    	int lastZhuangX = 1;
    	if(lastPlayer!=null){
    		lastZhuangX = lastPlayer.getLianZhuangC();
    		if(lastZhuangX==0){
    			lastZhuangX =1;
    		}else{
    			if(isLianBanker==3){
    				lastZhuangX =1;
    			}else{
    				lastZhuangX+=1;
    			}
    		}
    	}
    	
    	if(!winList.isEmpty()){
    		PenghuziPlayer	winPlayer	= seatMap.get(winList.get(0));
    		String str ="CalOverPHZ";
    		if(lastPlayer!=null){
    			str+=lastPlayer.getName()+""+lastPlayer.getSeat();
    		}
    		logGameAcMsg(winPlayer, lastZhuangX, 0,str);
        	
    	}
    	
    	
    	
        if (winList.size() == 0 && leftCards.size() == 0) {
			// 流局
            isHuangZhuang = true;
        	if(lastPlayer!=null){
    			lastPlayer.setLianZhuangC(0);
    		}
        }else{
        	 wPlayer	= seatMap.get(winList.get(0));
        	 dahu = wPlayer.getHu().getDaHu();
        	if(lastWinSeat==winList.get(0)){
        		if(wPlayer!=null){
        			//if(qiangzhiHu)
        			wPlayer.setLianZhuangC(wPlayer.getLianZhuangC()+1);
        			if(isLianBanker==2){ 
        				//wPlayer.setLianZhuangC(wPlayer.getLianZhuangC()+1);
        				if(wPlayer.getLianZhuangC()>2){
        					wPlayer.setLianZhuangC(2);
        				}
        			}else if(isLianBanker ==3){//不中庄
        				wPlayer.setLianZhuangC(1);
        			}
        			wPlayer.changeAction(PenghuziConstant.ACTION_COUNT_INDEX_ZZ, 1);
        		}
        	}else {
        	
        		if(lastPlayer!=null){
        			if(disCardSeat==lastPlayer.getSeat()){
        				lastZhangC = lastPlayer.getLianZhuangC();
        			}
        			lastPlayer.setLianZhuangC(0);
        		}
        		wPlayer.setLianZhuangC(1);
        	}
        	
        	//lianZhangCount = wPlayer.getLianZhuangC();
        	
        }
        
       
        PenghuziPlayer fangPaoPlayer = null;
        if(!isMoFlag()&&wPlayer!=null&&!getFirstCard()) {//
//        	fangPaoSeat = disCardSeat;
        	fangPaoPlayer = seatMap.get(disCardSeat);
        }
        
        if (wPlayer!=null&&dahu==PengHZMingTang.PAO_HU&&!getNowDisCardIds().isEmpty()) {
        	PenghzCard card = getNowDisCardIds().get(0);
        	if(wPlayer.getPengMap().get(card.getVal())!=null){
        		fangPaoPlayer =	seatMap.get(wPlayer.getPengMap().get(card.getVal()));
        	}
        }
        
        int winFen = 0;
        boolean isOver = false;
        if(fangPaoPlayer!=null){
        	int lianz= wPlayer.getLianZhuangC();
        	if(fanZhongZhuang==1){
        		if(lastZhangC>0){
        			if(isLianBanker>=2){ 
        				if(lastZhangC<2){
        					lastZhangC=2;
        				}
        			}else {
        				lastZhangC+=1;
        			}
        			lianz = lastZhangC;
        		}
        		
        	}
        	
        	int huPoint = lianz*(maxPlayerCount-1)*4;
        	if(dahu==PengHZMingTang.XIAO_QI_DUI||dahu==PengHZMingTang.WU_FU){
        		if(isLianBanker>=2){
        			huPoint=0;
        		}else {
        			huPoint-=(maxPlayerCount-1)*4;
        		}
        	}
//        	if(dahu==PengHZMingTang.DI_HU){
//        		
//        	}
        	int dahuPoint = getDahuPoint(dahu, lianz,isLianBanker);
        	dahuPoint*=(maxPlayerCount-1);
        	
        	if(getDisCardCout()==1){
				dahu = PengHZMingTang.DI_HU;
				huPoint = 8*(maxPlayerCount-1);
			}
        	
        	huPoint+=dahuPoint;
        	if(wPlayer.isHasNoDui()){
        		huPoint+=6;
        		if(dahu!=PengHZMingTang.DI_HU){
        			dahu = PengHZMingTang.WUDUI_HU;
        		}
        	}
        	int niaoFen = wPlayer.getDaNiaoPoint()+fangPaoPlayer.getDaNiaoPoint();
        	huPoint+=niaoFen;
        	
        	
        	wPlayer.calcResult(this, 1, huPoint, isHuangZhuang);
        	wPlayer.changeAction(PenghuziConstant.ACTION_COUNT_INDEX_HU, 1);
        	fangPaoPlayer.calcResult(this, 1, -huPoint, isHuangZhuang);
        	
        	fangPaoPlayer.changeAction(PenghuziConstant.ACTION_COUNT_INDEX_DIAN_PAO, 1);
        }else{
        	if(wPlayer!=null){
        		int lianz= wPlayer.getLianZhuangC();
        		int huPoint = lianz*4;
        		if(dahu==PengHZMingTang.XIAO_QI_DUI||dahu==PengHZMingTang.WU_FU){
        			if(isLianBanker>=2){
            			huPoint=0;
            		}else {
            			huPoint-=4;
            		}
            	}
        		int dahuPoint = getDahuPoint(dahu, wPlayer.getLianZhuangC(),isLianBanker);
        		//dahuPoint*=(maxPlayerCount-1);
            	huPoint+=dahuPoint;
            	if(wPlayer.isHasNoDui()){
            		huPoint+=6;
            		if(dahu!=PengHZMingTang.DI_HU){
            			dahu = PengHZMingTang.WUDUI_HU;
            		}
            	}
            	int totalPoint = 0;
        		for (PenghuziPlayer seat : seatMap.values()) {
           		 if(wPlayer.getSeat()==seat.getSeat()){
           			 continue;
           		 }
           		int niaoFen = wPlayer.getDaNiaoPoint()+seat.getDaNiaoPoint();
            	int losePoint = huPoint+niaoFen;
            	totalPoint+=losePoint;
           		seat.calcResult(this, 1, -losePoint, isHuangZhuang);
           	 }
        		
        		wPlayer.calcResult(this, 1, totalPoint, isHuangZhuang);
        		wPlayer.changeAction(PenghuziConstant.ACTION_COUNT_INDEX_HU, 1);
        	}
        }
        for (PenghuziPlayer seat : seatMap.values()) {
        	seat.changeTotalPoint(seat.getPoint());
        }
        

        
        
            //boolean selfMo = false;
//            if (!winList.isEmpty()) {
//               // winPlayer = seatMap.get(winList.get(0));
//                //selfMo = winPlayer.getSeat() == moSeat;
//            } else {
//               // PenghuziPlayer lastWinPlayer = seatMap.get(lastWinSeat);
//            }
              isOver = playBureau >= totalBureau;
        
        if(autoPlayGlob >0) {
			// //是否解散
            boolean diss = false;
            if(autoPlayGlob ==1) {
            	 for (PenghuziPlayer seat : seatMap.values()) {
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
            	 isOver =true;
            }
        }
        
        
        if(!winList.isEmpty()){
    		PenghuziPlayer	winPlayer	= seatMap.get(winList.get(0));
    		String str = "CalOverPHZ";
    		if(fangPaoPlayer!=null){
    			str+=fangPaoPlayer.getName()+""+fangPaoPlayer.getSeat();
    		}
    		logGameAcMsg(winPlayer, lastZhuangX, 1,str);
        	
    	}
        
        if(isOver){
            calcPointBeforeOver();
        }

        // 金币场
        if(isGoldRoom()){
            for(PenghuziPlayer player : seatMap.values()){
                player.setPoint(player.getTotalPoint());
                player.setWinGold(player.getTotalPoint());
            }
            calcGoldRoom();
        }
        
        
        
        ClosingPhzInfoRes.Builder res = sendAccountsMsg(isOver, winList, winFen,dahu, false, fangPaoPlayer==null?0:fangPaoPlayer.getSeat(),lastZhuangX);
        saveLog(isOver,0L, res.build());
        if (!winList.isEmpty()) {
            setLastWinSeat(winList.get(0));
        } else {
               // int next = getNextSeat(lastWinSeat);
                setLastWinSeat(getMoSeat());
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
    
    
    private int getDahuPoint(int dahu,int zhuangCount,int lianz){
    	int dahuPoint = 0;
//    	zhuangCount -=1;
    	if(dahu==PengHZMingTang.SAN_DA||dahu==PengHZMingTang.SI_QING){
    		dahuPoint += 5;
    	}else if(dahu==PengHZMingTang.XIAO_QI_DUI||dahu==PengHZMingTang.WU_FU){
    		dahuPoint = 40;
//    		zhuangCount=0;
    	}else if(dahu==PengHZMingTang.PENG_HU){
    		dahuPoint = 1;
    	}else if(dahu==PengHZMingTang.SAO_SAN_DA||dahu==PengHZMingTang.SAO_SI_QING){
    		dahuPoint += 6;
    		zhuangCount -=1;
    	}else if(dahu==PengHZMingTang.SAO_HU){
    		dahuPoint += 2;
    		zhuangCount -=1;
    	}else if(dahu==PengHZMingTang.TI_LONG_HU){
    		dahuPoint += 8;
    		zhuangCount -=1;
    	}else if(dahu==PengHZMingTang.PAO_HU){
    		dahuPoint += 4;
    		zhuangCount -=1;
    	}
    	
//    	else if(dahu == PengHZMingTang.DI_HU){
//    		dahuPoint = 8;
//    	}
    	
    	int totalPoint = 0;
    	if(zhuangCount>0){
    		if(lianz==2){
    			totalPoint = zhuangCount*dahuPoint;
    		}else{
    			if(dahu==PengHZMingTang.SAN_DA||dahu==PengHZMingTang.SI_QING||dahu==PengHZMingTang.PENG_HU||dahu==PengHZMingTang.XIAO_QI_DUI||dahu==PengHZMingTang.WU_FU){
    				totalPoint = dahuPoint;
    			}
    		}
    		
    	}
    	
    	return totalPoint;
    	
    }

	private boolean checkAuto3() {
		boolean diss = false;
		// if(autoPlayGlob==3) {
		boolean diss2 = false;
		for (PenghuziPlayer seat : seatMap.values()) {
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

    @Override
    public void saveLog(boolean over,long winId, Object resObject) {
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
        userLog.setMaxPlayerCount(maxPlayerCount);
        userLog.setType(creditMode == 1 ? 2 : 1 );
        userLog.setGeneralExt(buildGeneralExtForPlaylog().toString());
        long logId = TableLogDao.getInstance().save(userLog);
        saveTableRecord(logId, over, playBureau);
        UdpLogger.getInstance().sendSnapshotLog(masterId, playLog, logRes);
        if (!isGoldRoom()){
        	for (PenghuziPlayer player : playerMap.values()) {
                player.addRecord(logId, playBureau);
            }
        }
    }

    @Override
    protected void loadFromDB1(TableInf info) {
        if (!StringUtils.isBlank(info.getNowDisCardIds())) {
            this.nowDisCardIds = PenghuziTool.explodePhz(info.getNowDisCardIds(), ",");
        }
        if (!StringUtils.isBlank(info.getLeftPais())) {
            this.leftCards = PenghuziTool.explodePhz(info.getLeftPais(), ",");
        }
        if (isGoldRoom()){
			autoTimeOut = ResourcesConfigsUtil.loadIntegerValue("ServerConfig","autoTimeOutPhz",10*1000);
            autoTimeOut2=autoTimeOut;
		}else{
//            autoTimeOut = ResourcesConfigsUtil.loadIntegerValue("ServerConfig","autoTimeOutPhzNormal",60*1000);
//            autoTimeOut2 = ResourcesConfigsUtil.loadIntegerValue("ServerConfig","autoTimeOutPhzNormal2",10*1000);
        }
    }

    @Override
    protected void sendDealMsg() {
        sendDealMsg(0);
    }

    @Override
    protected void sendDealMsg(long userId) {
		// 天胡或者暗杠
//        int lastCardIndex = RandomUtils.nextInt(21);
        PenghuziPlayer winPlayer = seatMap.get(lastWinSeat);

        for (PenghuziPlayer tablePlayer : seatMap.values()) {
            DealInfoRes.Builder res = DealInfoRes.newBuilder();
            res.addAllHandCardIds(tablePlayer.getHandPais());
            res.setNextSeat(lastWinSeat);
			res.setGameType(getWanFa());// 1跑得快 2麻将
            res.setRemain(leftCards.size());
            res.setBanker(lastWinSeat);
            res.addXiaohu(winPlayer.getHandPais().get(0));
            if(tablePlayer.isAutoPlay()) {
       		 addPlayLog(tablePlayer.getSeat(), PenghzDisAction.action_tuoguan + "",1 + "");
            }
            
            if(tablePlayer.getLianZhuangC()>0){
            	 addPlayLog(tablePlayer.getSeat(), PenghzDisAction.action_Zhuang + "",(tablePlayer.getLianZhuangC()+1) + "");
            }
            tablePlayer.writeSocket(res.build());
            
            sendTingInfo(tablePlayer);
        }
        
        
        checkQiShouZaiTi();
    }

    @Override
    public synchronized void startNext() {
        checkAction();
    }

    public void play(PenghuziPlayer player, List<Integer> cardIds, int action) {
        play(player, cardIds, action, false, false, false);
    }

    private void hu(PenghuziPlayer player, List<PenghzCard> cardList, int action, PenghzCard nowDisCard) {
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return;
        }
        if (huConfirmList.contains(player.getSeat())) {
            return;
        }
        if (!checkAction(player, action,cardList,nowDisCard)) {
            player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip);
            // player.writeErrMsg(LangMsgEnum.code_29);
            return;
        }
        List<Integer> actionList = actionSeatMap.get(player.getSeat());
        if (actionList.get(0) != 1) {
            return;
        }
        PenghuziHuLack paoHu = player.checkPaoHu(nowDisCard, isSelfMo(player), firstCard);
        PenghuziHuLack pingHu = player.checkHu(nowDisCard, isSelfMo(player));
        if (!pingHu.isHu()) {
            PenghuziCheckCardBean paoHuBean = player.checkPaoHu();
            if (paoHuBean.isHu()) {
                pingHu = paoHuBean.getLack();
            }
        }
        PenghuziHuLack hu = pingHu;
        if (paoHu.isHu()) {
        	
        	
//            int paoHuOutHuXi = player.getPaoOutHuXi(nowDisCard, isSelfMo(player),  firstCard);
//            if (paoHu.getHuxi() + paoHuOutHuXi > pingHu.getHuxi()) {
                play(player, PenghuziTool.toPhzCardIds(paoHu.getPaohuList()), paoHu.getPaohuAction(), false, true, false);
                hu = player.checkHu(null, isSelfMo(player));
//            }
        } else {
            hu = pingHu;
        }

        if (hu.isHu()&&player.getSiShou()==0) {
			// broadMsg(player.getName() + " 胡牌");
          //  player.setHuxi(hu.getHuxi());
            player.setHu(hu);
            huConfirmList.add(player.getSeat());
            addPlayLog(player.getSeat(), action + "", PenghuziTool.implodePhz(cardList, ","));
            sendActionMsg(player, action, null, PenghzDisAction.action_type_action);
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
    public boolean isSelfMo(PenghuziPlayer player) {
        if (moSeatPair != null) {
            return moSeatPair.getValue().intValue() == player.getSeat() ;
        }
        return false;
    }

    /**
	 * 提
	 */
    private void ti(PenghuziPlayer player, List<PenghzCard> cardList, PenghzCard nowDisCard, int action, boolean moPai) {
		// cards肯定是4个相同的
        if (cardList == null) {
			System.out.println("提不合法:" + cardList);
			player.writeErrMsg("提不合法:" + cardList);
            return;
        }

        if (cardList.size() == 1) {
            List<PenghzCard> tiCards = player.getTiCard(cardList.get(0));
            if (tiCards == null || tiCards.size() != 3) {
				System.out.println("提不合法:" + tiCards);
				player.writeErrMsg("提不合法:" + tiCards);
                return;
            }
            cardList.addAll(tiCards);
        } else {
            if (!player.getHandPhzs().contains(cardList.get(0))) {
                return;
            }
        }
		// 是否栽跑
        boolean isZaiPao = player.isZaiPao(cardList.get(0).getVal());

        if (cardList.size() != 4 && !cardList.contains(nowDisCard)) {
            cardList.add(0, nowDisCard);
        }

        if (cardList.size() != 4) {
            return;
        }

        if (!PenghuziTool.isSameCard(cardList)) {
			System.out.println("提不合法:" + cardList);
			player.writeErrMsg("提不合法:" + cardList);
            return;
        }
	//	player.changeAction(PenghuziConstant.ACTION_COUNT_INDEX_TI, 1);
        addPlayLog(player.getSeat(), action + "", PenghuziTool.implodePhz(cardList, ","));
        if (nowDisCard != null) {
            getDisPlayer().removeOutPais(nowDisCard);
        }
        player.disCard(action, cardList);
        clearAction();
        setAutoDisBean(null);

        PenghuziCheckCardBean checkAutoDis = checkAutoDis(player, false);
        if (checkAutoDis != null) {
            playAutoDisCard(checkAutoDis, true);
        }

		// 检查是否能胡牌
        PenghuziCheckCardBean checkCard = player.checkPaoHu();
        checkPaohuziCheckCard(checkCard);
        calActionPoint(player, action, isZaiPao, 0);
		// 是否能出牌
        if (!moPai) {
			// 不是轮到自己摸牌的时候提的牌
        	if(isMoFlag()){
        		boolean disCard = setDisPlayer(player, action, checkCard.isHu());
        	}
        }
        sendActionMsg(player, action, cardList, PenghzDisAction.action_type_action, isZaiPao, false);
  
        

    }

    /**
	 * 栽(臭栽)
	 *
	 * @param cardList
	 *            要栽的牌
	 */
    private void zai(PenghuziPlayer player, List<PenghzCard> cardList, PenghzCard nowDisCard, int action) {
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return;
        }

        PenghuziCheckCardBean checkAutoDis = checkAutoDis(player, false);
        if (checkAutoDis != null) {
            playAutoDisCard(checkAutoDis, true);
        }
        getDisPlayer().removeOutPais(nowDisCard);
        if (nowDisCard!=null&&!cardList.contains(nowDisCard)) {
            cardList.add(0, nowDisCard);
        }
        setBeRemoveCard(nowDisCard);
        if (action == PenghzDisAction.action_zai) {
            setZaiCard(nowDisCard);
            
        }
        addPlayLog(player.getSeat(), action + "", PenghuziTool.implodePhz(cardList, ","));
        player.disCard(action, cardList);
        clearAction();
        setAutoDisBean(null);
		// 检查是否能胡牌
        PenghuziCheckCardBean checkCard = player.checkPaoHu();
        checkPaohuziCheckCard(checkCard);
		// 是否能出牌
        calActionPoint(player, action, false, 0);
        if(isMoFlag()){
        	boolean disCard = setDisPlayer(player, action, checkCard.isHu());
        }
        sendActionMsg(player, action, cardList, PenghzDisAction.action_type_action);
        
        

    }

    /**
	 * 跑
	 */
    private void pao(PenghuziPlayer player, List<PenghzCard> cardList, PenghzCard nowDisCard, int action, boolean isHu, boolean isPassHu) {
        if (cardList.size() != 3 && cardList.size() != 1) {
			broadMsg("跑的张数不对:" + cardList);
            return;
        }
        List<Integer> actionList = actionSeatMap.get(player.getSeat());
        if (actionList == null) {
            return;
        }
        if (!isHu && actionList.get(5) != 1) {
            return;
        }

		// 能跑胡的情况下不 胡挡不住跑
        if (!isHu && !checkAction(player, action,cardList,nowDisCard)) {
			// 发现别人能胡
			// 能跑能胡的情况下
            if (actionList.get(0) == 1) {
                actionList.set(0, 0);
                addAction(player.getSeat(), actionList);
				// 更新前台数据
                setSendPaoSeat(player.getSeat());
                sendPlayerActionMsg(player);
            }
            // player.writeErrMsg(LangMsgEnum.code_29);
            return;
        }
        boolean isZaiPao = player.isZaiPao(cardList.get(0).getVal());
        getDisPlayer().removeOutPais(nowDisCard);
        if (!cardList.contains(nowDisCard)) {
            cardList.add(0, nowDisCard);
        }
        
        if (cardList.size() == 1) {
			// 如果是一张牌说明已经在出的牌里面了
            List<PenghzCard> list = player.getSameCards(nowDisCard);
            cardList.addAll(list);
        }
        
        Integer fangSeat =  player.getPengMap().get(cardList.get(0).getVal());
        if(fangSeat==null){
        	if(!isMoFlag()){
        		fangSeat = disCardSeat;
        	}else{
        		fangSeat = 0;
        	}
        	logGameAcMsg(player, fangSeat, 0,"fangPaoMsg");
        }else {
        	logGameAcMsg(player, fangSeat, 1,"fangPaoMsg");
        	PenghuziPlayer seatPlayer = seatMap.get(fangSeat);
        	PenghzCard disCard = null;
        	for(PenghzCard id: seatPlayer.getOutPaisCard()){
        		if(id.getVal()==cardList.get(0).getVal()){
        			disCard = id;
        			break;
        		}
        	}
        	if(disCard!=null){
        		seatPlayer.removeOutPais(disCard);
        		cardList.add(cardList.size()-1, disCard);
        	}
        	 
        	
        }
        
        setBeRemoveCard(nowDisCard);


        if (cardList.size() != 4) {
            return;
        }

		// 检测是否能提
        PenghuziCheckCardBean checkAutoDis = checkAutoDis(player, false);
        if (checkAutoDis != null) {
            playAutoDisCard(checkAutoDis, true);
        }
		//player.changeAction(PenghuziConstant.c, 1);
        addPlayLog(player.getSeat(), action + "", PenghuziTool.implodePhz(cardList, ","));
        player.disCard(action, cardList);
        clearAction();
        setAutoDisBean(null);

        if (!isHu && !isPassHu && isMoFlag()) {
            PenghuziCheckCardBean checkCard = player.checkPaoHu();
            checkPaohuziCheckCard(checkCard);
        }
        
        
        calActionPoint(player, action, isZaiPao, fangSeat);
		// 是否能出牌
        if (!isHu) {
            boolean disCard = setDisPlayer(player, action, false);
            sendActionMsg(player, action, cardList, PenghzDisAction.action_type_action, isZaiPao, !disCard);
            if (!disCard) {
                if (PenghuziConstant.isAutoMo) {
                    checkMo();
                }
            }
        } else {
            sendActionMsg(player, action, cardList, PenghzDisAction.action_type_action, isZaiPao, false);
        }

        // if (player.isFangZhao()) {
        // LogUtil.msgLog.info("----tableId:" + getId() + "---userName:" +
		// player.getName() + "------跑-----解除放招状态" + cardList.get(0));
        // player.setFangZhao(0);
        // // player.writeComMessage(WebSocketMsgType.res_code_phz_fangzhao,
        // player.getUserId() + "", 0 + "");
        // relieveFangZhao(player.getUserId());
        // }

    }


    /**
	 * 出牌
	 */
    private void disCard(PenghuziPlayer player, List<PenghzCard> cardList, int action) {
        if (!actionSeatMap.isEmpty()) {
        	player.writeComMessage(WebSocketMsgType.res_code_phz_dis_err, cardList.get(0).getId());
			LogUtil.e("动作:" + JacksonUtil.writeValueAsString(actionSeatMap));
            return;
        }

        if (toPlayCardFlag != 1) {
    		player.writeComMessage(WebSocketMsgType.res_code_phz_dis_err, cardList.get(0).getId());
			LogUtil.e(player.getName() + "错误 toPlayCardFlag:" + toPlayCardFlag + "出牌");
            checkMo();
            return;
        }

        if (player.getSeat() != nowDisCardSeat) {
        	player.writeComMessage(WebSocketMsgType.res_code_phz_dis_err, cardList.get(0).getId());
			player.writeErrMsg("轮到:" + nowDisCardSeat + "出牌");
            return;
        }
        if (cardList.size() != 1) {
        	player.writeComMessage(WebSocketMsgType.res_code_phz_dis_err, cardList.get(0).getId());
			player.writeErrMsg("出牌数量不对:" + cardList);
            return;
        }

        PenghuziHandCard cardBean = player.getPaohuziHandCard();
        if (!cardBean.isCanoperateCard(cardList.get(0))) {
        	player.writeComMessage(WebSocketMsgType.res_code_phz_dis_err, cardList.get(0).getId());
			player.writeErrMsg("该牌不能单出:" + cardList);
			LogUtil.e("该牌不能单出:" + cardList);
            return;
        }

		
        boolean canDiHu = false;
		if (firstCard) {
			firstCard = false;
		}

        addPlayLog(player.getSeat(), action + "", PenghuziTool.implodePhz(cardList, ","));
//		checkFreePlayerTi(player, action);// 检查闲家提
        player.disCard(action, cardList);
        setMoFlag(0);
        markMoSeat(player.getSeat(), action);
        clearMoSeatPair();
		setToPlayCardFlag(0); // 应该要打牌的flag
        setDisCardSeat(player.getSeat());
        setNowDisCardIds(cardList);
        setNowDisCardSeat(getNextDisCardSeat());
        
		setDisCardCout(getDisCardCout()+1);
        
        int pwp =  player.getPWPTCount(null);
        if(player.getHandPais().size()==2&&pwp==4&&PenghuziTool.isDuiZi(player.getHandPais())){
        	player.setWufuBaoj(0);
        	if(!player.isAutoPlay()){
        		ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.RES_WUFU_BAOJING,player.getSeat());
    			player.writeSocket(com.build());
        	}
			
			if(player.isAutoPlay()){
				 player.setWufuBaoj(1);
		           addPlayLog(player.getSeat(), PenghzDisAction.action_WUFU_DENG + "", 1 + "");
					// player.writeComMessage(WebSocketMsgType.res_code_phz_fangzhao, player.getUserId() + "", 1 + "");
					for (Player playerTemp : getSeatMap().values()) {
						playerTemp.writeComMessage(WebSocketMsgType.RES_WUFU_XUANZ, player.getSeat(), 1);
					}
					wufuXz(player);
			}
        }else {
        	PenghuziCheckCardBean autoDisCard = checkDisAction(player, action, cardList.get(0), canDiHu);
        }
        
        
        sendActionMsg(player, action, cardList, PenghzDisAction.action_type_dis);
        
       
        
        
        // if (autoDisCard != null) {
		// // 系统自动出牌
        // playAutoDisCard(autoDisCard);
        // } else {
        checkAutoMo();
        
        sendTingInfo(player);
        // }
    }

    private void checkAutoMo() {
        if (isTest()) {
            checkMo();

        }
    }

    private void tiLong(PenghuziPlayer player) {
        boolean isTiLong = false;
        List<PenghzCard> cardList = new ArrayList<>();
        while (player.getOweCardCount() < -1) {
            if (!isTiLong) {
                isTiLong = true;
                removeAction(player.getSeat());
            }
            PenghzCard card = null;
            if (GameServerConfig.isDebug()) {
                if (card == null) {
                    card = getNextCard(106);
                }
                if (card == null) {
                    card = getNextCard(4);
                }
            }

            if (card == null) {
                card = getNextCard();

            }
            player.tiLong(card);
            cardList.add(card);

            addPlayLog(player.getSeat(), PenghzDisAction.action_buPai + "", (card == null ? 0 : card.getId()) + "");
            StringBuilder sb = new StringBuilder("PengHz");
            sb.append("|").append(getId());
            sb.append("|").append(getPlayBureau());
            sb.append("|").append(player.getUserId());
            sb.append("|").append(player.getSeat());
            sb.append("|").append(player.isAutoPlay() ? 1 : 0);
            sb.append("|").append("tiLong");
            sb.append("|").append(card);
            LogUtil.msgLog.info(sb.toString());
        }

        if (isTiLong) {
            sendActionMsg(player, PenghzDisAction.action_tilong, cardList, PenghzDisAction.action_type_action, false, false);

            PenghuziCheckCardBean checkCard = player.checkCard(null, true, true, false);
            if (checkPaohuziCheckCard(checkCard)) {
                playAutoDisCard(checkCard);
                if (player.getSeat() != lastWinSeat && checkCard.isTi()) {
                    player.setOweCardCount(player.getOweCardCount() - 1);
                }
                tiLong(player);
            }
        }
    }

//    public void checkFreePlayerTi(PenghuziPlayer player, int action) {
//        if (player.getSeat() == lastWinSeat && player.isFristDisCard() && action != PenghzDisAction.action_ti) {
//            for (int seat : getSeatMap().keySet()) {
//                if (lastWinSeat == seat) {
//                    continue;
//                }
//                PenghuziPlayer nowPlayer = seatMap.get(seat);
//                PenghuziCheckCardBean checkCard = nowPlayer.checkCard(null, true, true, false);
//                if (checkPaohuziCheckCard(checkCard)) {
//                    playAutoDisCard(checkCard);
//                    tiLong(nowPlayer);
//					/*-- 暂时屏蔽两条龙的处理
//					boolean needBuPai = false;
//					if (checkCard.isTi()) {
//						PenghuziHandCard cardBean = nowPlayer.getPaohuziHandCard();
//						PenghzCardIndexArr valArr = cardBean.getIndexArr();
//						PenghuziIndex index3 = valArr.getPaohzCardIndex(3);
//						if (index3 != null && index3.getLength() >= 2) {
//							needBuPai = true;
//						}
//					}
//					playAutoDisCard(checkCard);
//					if (nowPlayer.isFristDisCard()) {
//						nowPlayer.setFristDisCard(false);
//					}
//					
//					if (needBuPai) {
//						PenghzCard buPai = leftCards.remove(0);
//						for (PenghuziPlayer tempPlayer : seatMap.values()) {
//							if (seat == tempPlayer.getSeat()) {
//								tempPlayer.getHandPhzs().add(buPai);
//								System.out.println("----------------------------------谁补:" + tempPlayer.getName() + "  什么牌:" + buPai.getId() + "  醒子数量:" + leftCards.size());
//							}
//							tempPlayer.writeComMessage(WebSocketMsgType.res_com_code_phzbupai, seat, buPai.getId(), leftCards.size());
//						}
//					}
//					 */
//
//                }
//                checkSendActionMsg();
//            }
//        }
//    }

    /**
	 * 碰
	 */
    private void peng(PenghuziPlayer player, List<PenghzCard> cardList, PenghzCard nowDisCard, int action) {
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return;
        }
        if (!checkAction(player, action, cardList, nowDisCard)) {
            player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip);
            return;
        }

        cardList = player.getPengList(nowDisCard, cardList);
        if (cardList == null) {
			player.writeErrMsg("不能碰");
            return;
        }
        if (!cardList.contains(nowDisCard) &&isMoFlag()) {
            cardList.add(0, nowDisCard);
        }
        setBeRemoveCard(nowDisCard);

        PenghuziCheckCardBean checkAutoDis = checkAutoDis(player, false);
        if (checkAutoDis != null) {
            playAutoDisCard(checkAutoDis, true);
        }
        
        
        if(isMoFlag()){
        	getDisPlayer().removeOutPais(nowDisCard);
        }
        addPlayLog(player.getSeat(), action + "", PenghuziTool.implodePhz(cardList, ","));
        player.disCard(action, cardList);
        clearAction();

        boolean disCard = setDisPlayer(player, action, false);
        sendActionMsg(player, action, cardList, PenghzDisAction.action_type_action);
        if (!disCard) {
            // checkMo();
        }

		// 碰的情况,把所有玩家的过牌去掉
        
        int fangSeat = 0;
        if (isMoFlag()) {
            for (PenghuziPlayer seatPlayer : seatMap.values()) {
                if (seatPlayer.getSeat() == player.getSeat()) {
                    continue;
                }
                seatPlayer.removePassChi(nowDisCard.getVal());
            }
        }else{
        	fangSeat = getDisCardSeat();
        	player.addPlayerPengVal(nowDisCard.getVal(), fangSeat);
        }
        
        calActionPoint(player, action, false, fangSeat);
    }
    
    
    
    /**
     * 
     * @param player
     * @param action
     * @param isZaiPao
     * @param fangSeat
     * @return
     */
    private int calActionPoint(PenghuziPlayer player,int action,boolean isZaiPao,int fangSeat){
    	
    	int mingTang = 0;
    	int addPoint =0;
    	if(action ==PenghzDisAction.action_peng){
    		addPoint+=1;
    	}
    	else if(action ==PenghzDisAction.action_zai||action ==PenghzDisAction.action_chouzai){
    		addPoint+=2;
    	}else if(action ==PenghzDisAction.action_pao){
    		addPoint+=4;
    	}else if(action ==PenghzDisAction.action_ti){
    		if(isZaiPao){
    			addPoint+=8;
    		}else{
    			addPoint+=10;
    		}
    	}
    	
    	int pwpt = player.getPWPTCount(null);
    	
    	if((pwpt==3||pwpt==4)&&(action !=PenghzDisAction.action_ti&&action !=PenghzDisAction.action_pao)){
    		addPoint+=4;
    		//mingTang =pwpt-2;
    	}else if(pwpt==5){
    		addPoint = 0;
    		//addPoint+=40;
    	}
    	if(addPoint==0){
    		return 0;
    	}
    	
    	if(pwpt>2){
    		mingTang =pwpt-2;
    		if(action==PenghzDisAction.action_pao){
    			mingTang =0;
    		}else if(action == PenghzDisAction.action_ti){
    			mingTang =PengHZMingTang.TI_LONG_HU;
    		}else if(action == PenghzDisAction.action_zai||action == PenghzDisAction.action_chouzai){
    			mingTang +=9;
    		}
    		
    	}
    	
    	
    	JSONArray jarr = new JSONArray();
    	
    	int totalPoint = addPoint*(maxPlayerCount-1);
    	player.changePoint(totalPoint);
    	
    	
    	JSONObject json = new JSONObject();
		json.put("seat", player.getSeat());
		json.put("fen", totalPoint);
		json.put("mingtang", mingTang);
		jarr.add(json);
		
		 addPlayLog(player.getSeat(), PenghzDisAction.action_jiaFen + "",player.getPoint()+"", mingTang+"");
    	if(fangSeat>0){
    		PenghuziPlayer seatPlayer = seatMap.get(fangSeat);
    		if(seatPlayer!=null){
    			seatPlayer.changePoint(-totalPoint);
    			JSONObject json2 = new JSONObject();
    			json2.put("seat", seatPlayer.getSeat());
    			json2.put("fen", -totalPoint);
    			json2.put("mingtang", mingTang);
        		jarr.add(json2);
        		 addPlayLog(seatPlayer.getSeat(), PenghzDisAction.action_jiaFen + "",seatPlayer.getPoint()+"", mingTang+"");
        		
    		}
    		
    	}else {
    		for (PenghuziPlayer seatPlayer : seatMap.values()) {
                if (seatPlayer.getSeat() == player.getSeat()) {
                    continue;
                }
                seatPlayer.changePoint(-addPoint);
                JSONObject json2 = new JSONObject();
                json2.put("seat", seatPlayer.getSeat());
                json2.put("fen", -addPoint);
                json2.put("mingtang", mingTang);
                addPlayLog(seatPlayer.getSeat(), PenghzDisAction.action_jiaFen + "",seatPlayer.getPoint()+"", mingTang+"");
        		jarr.add(json2);
            }
    	}
    	
   	ComRes.Builder builder = SendMsgUtil.buildComRes(WebSocketMsgType.RES_FRESH_FEN,action,jarr.toString());
   	for (PenghuziPlayer seatPlayer : seatMap.values()) {
   		seatPlayer.writeSocket(builder.build());
    }
    	
    	return mingTang;
    	
    }
    
    
    
    
    
    private int changgeSeat(){
    	
    	if(maxPlayerCount==2){
    		return 0;
    	}
    	Set<Integer> seats = seatMap.keySet();
    	List<Integer> seatList = new ArrayList<Integer>();
    	
    	seatList.addAll(seats);
    	
    	
    	int seat0 = seatList.get(0);
    	int seatN = seatList.get(seatList.size()-1);
    	
    	
    	
    	JSONArray jarr = new JSONArray();
    	
    	
    	
    	JSONObject json = new JSONObject();
    	PenghuziPlayer repDuijPlayer = seatMap.get(seat0);
    	PenghuziPlayer repSeatPlayer = seatMap.get(seatN);
    	
    	json.put("seat0", repDuijPlayer.getSeat());
		json.put("userId", repDuijPlayer.getUserId());
		json.put("seat1", seatN);
		jarr.add(json);
		repDuijPlayer.setSeat(seatN);
		
		
		
		JSONObject json2 = new JSONObject();
		json2.put("seat0", repSeatPlayer.getSeat());
		json2.put("userId", repSeatPlayer.getUserId());
		json2.put("seat1", seat0);
		jarr.add(json2);
		repSeatPlayer.setSeat(seat0);
		seatMap.put(seatN, repDuijPlayer);
		seatMap.put(seat0, repSeatPlayer);
    	
		for (PenghuziPlayer seatPlayer : seatMap.values()) {
			
//			if(seatPlayer.getSeat()!=){
//				
//				
//				JSONObject json3 = new JSONObject();
//				json.put("seat0", seatPlayer.getSeat());
//				json.put("userId", repSeatPlayer.getUserId());
//				json.put("seat1", seatPlayer.getSeat());
//				jarr.add(json3);
//			}
//			
			
		}
    

   	ComRes.Builder builder = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_piao_PENGHU_CHANGGE_SEAT,1,jarr.toString());
   	for (PenghuziPlayer seatPlayer : seatMap.values()) {
   		seatPlayer.writeSocket(builder.build());
    }
   	return 0;
    	
    	
    }
    	    
    
    
    
    

    /**
	 * 过
	 */
    private void pass(PenghuziPlayer player, List<PenghzCard> cardList, PenghzCard nowDisCard, int action) {
        if (!actionSeatMap.containsKey(player.getSeat())) {
			// player.writeErrMsg("该玩家没有找到可以过的动作");
            return;
        }
        List<Integer> actionList = actionSeatMap.get(player.getSeat());
        List<Integer> list = PenghzDisAction.parseToDisActionList(actionList);
		// 栽，提，跑是不可以过的
        if (list.contains(PenghzDisAction.action_zai) || list.contains(PenghzDisAction.action_ti) || list.contains(PenghzDisAction.action_pao) || list.contains(PenghzDisAction.action_chouzai)) {
            return;
        }
		// 如果没有吃，碰，胡也是不可以过的
        if (!list.contains(PenghzDisAction.action_chi) && !list.contains(PenghzDisAction.action_peng) && !list.contains(PenghzDisAction.action_hu)) {
            return;
        }

		// 可以胡牌，然后点了过
        boolean isPassHu = actionList.get(0) == 1;
        if (actionList.get(0) == 1 && player.getHandPhzs().isEmpty()) {
			player.writeErrMsg("手上已经没有牌了");
            return;
        }

        if(action==PenghzDisAction.action_pass){
            int logId;
            if(paoHu==1){
                logId=0;
            }else {
                logId = nowDisCard.getId();
            }
            addPlayLog(player.getSeat(), PenghzDisAction.action_guo + "",logId+"");
            setPaoHu(0);
        }

        if(player.getOperateCards().isEmpty()){
            player.setSiShou(1);
        }

        int val = 0;
        if (nowDisCard != null) {
            val = nowDisCard.getVal();
        }

        boolean addPassChi = false;
        if (player.getSeat() == moSeat) {
            addPassChi = true;
        }

		// 将pass的吃碰值添加到passChi或passPeng中
        for (int passAction : list) {
            player.pass(passAction, val, addPassChi);

        }
        removeAction(player.getSeat());

		// 自动出牌
        if (autoDisBean != null) {
            refreshTempAction(player);
            if(player.getSeat()==autoDisBean.getSeat()){
            	 if(autoDisBean.getAutoAction()==PenghzDisAction.action_pao){
                 	autoDisBean.setPao(true);
                 	autoDisBean.setPassHu(isPassHu);
                 	autoDisBean.buildActionList();
                 }
            }
            
            addAction(autoDisBean.getSeat(), autoDisBean.getActionList());
            playAutoDisCard(autoDisBean);
        } else {
            PenghuziCheckCardBean checkCard = player.checkCard(nowDisCard, isSelfMo(player), isPassHu, false, false, true);
            checkCard.setPassHu(isPassHu);
            boolean check = checkPaohuziCheckCard(checkCard);
            markMoSeat(player.getSeat(), action);
            sendActionMsg(player, action, cardList, PenghzDisAction.action_type_action);
            if (check) {
                playAutoDisCard(checkCard, true);
            } else {
                if (PenghuziConstant.isAutoMo) {
                    checkMo();
                } else {
                    if (isTest()) {
                        checkMo();
                    }
                }
            }
            refreshTempAction(player);
        }

        if (this.leftCards.size() == 0 && actionSeatMap.isEmpty()) {
            calcOver();
        }

    }

    /**
	 * 吃
	 */
    private void chi(PenghuziPlayer player, List<PenghzCard> cardList, PenghzCard nowDisCard, int action) {
        List<Integer> actionList = actionSeatMap.get(player.getSeat());
        if (actionList == null) {
            return;
        }
        if (cardList != null) {
            if (cardList.size() % 3 != 0) {
				player.writeErrMsg("不能吃" + cardList);
                return;
            }

            if (!cardList.contains(nowDisCard)) {
                return;
            }
        }

        cardList = player.getChiList(nowDisCard, cardList);
        if (cardList == null) {
			player.writeErrMsg("不能吃");
            return;
        }

        if (cardList.size() > 3) {
            PenghuziHandCard card = player.getPaohuziHandCard();
            if (card.getOperateCards().size() <= cardList.size()) {
				player.writeErrMsg("您手上没有剩余的牌可打，不能吃");
                return;
            }
        }

        if (!checkAction(player, action,cardList,nowDisCard)) {
            player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip);
			// 能吃能碰的情况下
            if (actionList.get(1) == 1) {
                actionList.set(1, 0);
				// 选择了吃，那不能碰了
                player.pass(PenghzDisAction.action_peng, nowDisCard.getVal());
//                addAction(player.getSeat(), actionList);
				// 更新前台数据
//                sendPlayerActionMsg(player);
            }
            // player.writeErrMsg(LangMsgEnum.code_29);
            return;
        }

        if (PenghuziTool.isPaohuziRepeat(cardList)) {
			player.writeErrMsg("不能吃");
            return;
        }

        PenghuziCheckCardBean checkAutoDis = checkAutoDis(player, false);
        if (checkAutoDis != null) {
            playAutoDisCard(checkAutoDis, true);
        }

        if (!cardList.contains(nowDisCard)) {
            cardList.add(0, nowDisCard);
        } else {
            cardList.remove(nowDisCard);
            cardList.add(0, nowDisCard);
        }
        setBeRemoveCard(nowDisCard);

        getDisPlayer().removeOutPais(nowDisCard);
        addPlayLog(player.getSeat(), action + "", PenghuziTool.implodePhz(cardList, ","));
        player.disCard(action, cardList);
        clearAction();

        boolean disCard = setDisPlayer(player, action, false);
        sendActionMsg(player, action, cardList, PenghzDisAction.action_type_action);
        if (!disCard) {
            if (PenghuziConstant.isAutoMo) {
                checkMo();
            }
        }

    }

    public synchronized void play(PenghuziPlayer player, List<Integer> cardIds, int action, boolean moPai, boolean isHu, boolean isPassHu) {
		// 检查play状态
        if (state != table_state.play ) {
            return;
        }

        PenghzCard nowDisCard = null;
        List<PenghzCard> cardList = null;
		// 非摸牌非过牌要检查能否出牌,并将要出的牌id集合变成跑胡子牌
        if (action != PenghzDisAction.action_mo) {
            if (nowDisCardIds != null && nowDisCardIds.size() == 1) {
                nowDisCard = nowDisCardIds.get(0);
            }
            if (action != PenghzDisAction.action_pass) {
                if (!player.isCanDisCard(cardIds, nowDisCard)) {
                    return;
                }
            }
            if (cardIds != null && !cardIds.isEmpty()) {
                cardList = PenghuziTool.toPhzCards(cardIds);
            }
        }

        if (action != PenghzDisAction.action_mo) {
            StringBuilder sb = new StringBuilder("PengHz");
            sb.append("|").append(getId());
            sb.append("|").append(getPlayBureau());
            sb.append("|").append(player.getUserId());
            sb.append("|").append(player.getSeat());
            sb.append("|").append(player.isAutoPlay() ? 1 : 0);
            sb.append("|").append(PenghzDisAction.getActionName(action));
            sb.append("|").append(cardList);
            sb.append("|").append(nowDisCard);
            if (actionSeatMap.containsKey(player.getSeat())) {
                sb.append("|").append(PenghuziCheckCardBean.actionListToString(actionSeatMap.get(player.getSeat())));
            }
            LogUtil.msgLog.info(sb.toString());
        }
        // //////////////////////////////////////////////////////

        if (action == PenghzDisAction.action_ti) {
            if (cardList.size() > 4) {
				// 有多个提
                PenghzCardIndexArr arr = PenghuziTool.getMax(cardList);
                PenghuziIndex index = arr.getPaohzCardIndex(3);
                for (List<PenghzCard> tiCards : index.getPaohzValMap().values()) {
                    ti(player, tiCards, nowDisCard, action, moPai);
                }
            } else {
                ti(player, cardList, nowDisCard, action, moPai);
            }
        } else if (action == PenghzDisAction.action_hu) {
            hu(player, cardList, action, nowDisCard);
        } else if (action == PenghzDisAction.action_peng) {
            peng(player, cardList, nowDisCard, action);
        } else if (action == PenghzDisAction.action_chi) {
            chi(player, cardList, nowDisCard, action);
        } else if (action == PenghzDisAction.action_pass) {
            pass(player, cardList, nowDisCard, action);
        } else if (action == PenghzDisAction.action_pao) {
            pao(player, cardList, nowDisCard, action, isHu, isPassHu);
        } else if (action == PenghzDisAction.action_zai || action == PenghzDisAction.action_chouzai) {
            zai(player, cardList, nowDisCard, action);
        } else if (action == PenghzDisAction.action_mo) {
            if (isTest()) {
                return;
            }
            if (checkMoMark != null) {
                int cAction = cardIds.get(0);
                if (checkMoMark.getId() == player.getSeat() && checkMoMark.getValue() == cAction) {
                	if(player.getWufuBaoj()!=0&&!isBaojingPlayer()){
                		checkMo();
                	}
                } /*
					 * else { // System.out.println("发现请求-->" + player.getName()
					 * + // " seat:" + player.getSeat() + "-" +
					 * checkMoMark.getId() + // " action:" + cAction + "- " +
					 * checkMoMark.getValue()); }
					 */
            }

        } else {
            disCard(player, cardList, action);
        }
        if (!moPai && !isHu) {
			// 摸牌的时候提不需要做操作
            robotDealAction();
        }
        
        
        if (action >= PenghzDisAction.action_peng && action != PenghzDisAction.action_pass
				&& action != PenghzDisAction.action_mo) {
			sendTingInfo(player);
		}
        

    }
    
    private boolean isBaojingPlayer(){
    	for (PenghuziPlayer player : seatMap.values()) {
    		if(player.getWufuBaoj()==0){
    			return true;
    		}
    	}
    	return false;
    }
    

//    private boolean setDisPlayer(PenghuziPlayer player, int action, boolean isHu) {
//        return setDisPlayer(player, action, isHu);
//    }

    /**
	 * 设置要出牌的玩家
	 */
    private boolean setDisPlayer(PenghuziPlayer player, int action, boolean isHu) {
        if (this.leftCards.isEmpty()) {
			// 手上已经没有牌了
//            if (!isHu) {
//                calcOver();
//            }
//            return false;
        }

        boolean canDisCard = true;
        if (player.getHandPhzs().isEmpty()) {
            canDisCard = false;

        } else if (player.getOperateCards().isEmpty()) {
            canDisCard = false;
            if(!isHu)
                player.setSiShou(1);
        }
        if (canDisCard && (player.isNeedDisCard(action))) {
            setNowDisCardSeat(player.getSeat());
            setToPlayCardFlag(1);
            return true;
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
	 */
    private boolean checkAction(PenghuziPlayer player, int action, List<PenghzCard> cardList, PenghzCard nowDisCard) {
		// 优先度为胡杠补碰吃
        boolean canPlay = true;
        List<Integer> stopActionList = PenghzDisAction.findPriorityAction(action);
        for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
            if (player.getSeat() != entry.getKey()) {
				// 别人
                boolean can = PenghzDisAction.canDis(stopActionList, entry.getValue());
                if (!can) {
                    canPlay = false;
                }
                List<Integer> disActionList = PenghuziDisAction.parseToDisActionList(entry.getValue());
                if (disActionList.contains(action)) {
					// 同时拥有同一个事件 根据座位号来判断
                    int actionSeat = entry.getKey();
                    int nearSeat = getNearSeat(disCardSeat, Arrays.asList(player.getSeat(), actionSeat));
                    if (nearSeat != player.getSeat()) {
                        canPlay = false;
                    }

                }
            }
        }
        if (canPlay) {
            clearTempAction();
            return true;
        }

        int seat = player.getSeat();
        tempActionMap.put(seat, new TempAction(seat, action, cardList, nowDisCard));

		// 玩家都已选择自己的临时操作后 选取优先级最高
        if (tempActionMap.size() > 0 && tempActionMap.size() == actionSeatMap.size()) {
            int maxAction = -1;
            int maxSeat = 0;
            Map<Integer, Integer> prioritySeats = new HashMap<>();
            int maxActionSize = 0;
            for (TempAction temp : tempActionMap.values()) {
                if (maxAction == -1 || PenghzDisAction.findPriorityAction(maxAction).contains(temp.getAction())) {
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
            PenghuziPlayer tempPlayer = seatMap.get(maxSeat);
            List<PenghzCard> tempCardList = tempActionMap.get(maxSeat).getCardList();
            for (int removeSeat : prioritySeats.keySet()) {
                if (removeSeat != maxSeat) {
                    removeAction(removeSeat);
                }
            }
            clearTempAction();
			// 系统选取优先级最高操作
            play(tempPlayer, PenghuziTool.toPhzCardIds(tempCardList), maxAction);
        }else if(tempActionMap.size() + 1 == actionSeatMap.size() ){
			// 剩下可以跑的人
            for(int s : actionSeatMap.keySet()){
                if(!tempActionMap.containsKey(s)){
                    List<Integer> list = actionSeatMap.get(s);
                    boolean isPao = list.get(5) == 1;
                    for(int i= 0 ;i < list.size() ;i++){
                        if(i != 5 && list.get(i) == 1 ){
                            isPao = false;
                        }
                    }
                    if(isPao){
						// 表演跑
                        if (autoDisBean != null) {
                            playAutoDisCard(autoDisBean);
                        }
                    }
                }
            }
        }
        return canPlay;
    }

    /**
	 * 执行可做操作里面优先级最高的玩家操作
	 *
	 * @param player
	 */
    private void refreshTempAction(PenghuziPlayer player) {
        tempActionMap.remove(player.getSeat());
		Map<Integer, Integer> prioritySeats = new HashMap<>();// 各位置优先操作
        for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
            int seat = entry.getKey();
            List<Integer> actionList = entry.getValue();
            List<Integer> list = PenghuziDisAction.parseToDisActionList(actionList);
            int priorityAction = PenghzDisAction.getMaxPriorityAction(list);
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
        Iterator<TempAction> iterator = tempActionMap.values().iterator();
        while (iterator.hasNext()) {
            TempAction tempAction = iterator.next();
            if (tempAction.getSeat() == maxPrioritySeat) {
                int action = tempAction.getAction();
                List<PenghzCard> tempCardList = tempAction.getCardList();
                PenghuziPlayer tempPlayer = seatMap.get(tempAction.getSeat());
                iterator.remove();
				// 系统选取优先级最高操作
                play(tempPlayer, PenghuziTool.toPhzCardIds(tempCardList), action);
                break;
            }
        }
        changeExtend();
    }


    private void clearTempAction() {
        if (!tempActionMap.isEmpty()) {
            tempActionMap.clear();
            changeExtend();
        }
    }


    /**
	 * 获得出牌位置的玩家
	 */
    private PenghuziPlayer getDisPlayer() {
        return seatMap.get(disCardSeat);
    }

    private void record(PenghuziPlayer player, int action, List<PenghzCard> cardList) {
    }

    @Override
    public int isCanPlay() {
        if (getPlayerCount() < getMaxPlayerCount()) {
            return 1;
        }
        // for (PenghuziPlayer player : seatMap.values()) {
        // if (player.getIsEntryTable() != PdkConstants.table_online) {
		// // 通知其他人离线
        // broadIsOnlineMsg(player, player.getIsEntryTable());
        // return 2;
        // }
        // }
        return 0;
    }

    private synchronized void checkMo() {
        if (autoDisBean != null) {
            playAutoDisCard(autoDisBean);
        }

		// 0胡 1碰 2栽 3提 4吃 5跑
        if (!actionSeatMap.isEmpty()) {
            return;
        }
        if (nowDisCardSeat == 0) {
            return;
        }

		// // 下一个要摸牌的人
        PenghuziPlayer player = seatMap.get(nowDisCardSeat);
        // if (moSeat == player.getSeat()) {
        // if (moSeat == disCardSeat) {
        // broadMsg("moSeat == disCardSeat" + moSeat + " " + disCardSeat);
        // return;
        //
        // }
        // }

        if (toPlayCardFlag == 1) {
			// 接下来应该打牌
            return;
        }

        if (leftCards == null) {
            return;
        }
        if (this.leftCards.size() == 0 && !isHasSpecialAction()) {
            calcOver();
            return;
        }

        clearMarkMoSeat();

        // PenghzCard card = PenghzCard.getPaohzCard(59);
        // PenghzCard card = getNextCard();
        PenghzCard card = null;
        if (player.getFlatId().startsWith("vkscz2855914")) {
            card = getNextCard(102);
            // if (card == null) {
            // card = PenghzCard.getPaohzCard(61);
            // }
            if (card == null) {
                card = getNextCard();
            }
        } else {
            if (GameServerConfig.isDebug() && !player.isRobot()) {
                if (zpMap.containsKey(player.getUserId()) && zpMap.get(player.getUserId()) > 0) {
                    List<PenghzCard> cardList = PenghuziTool.findPhzByVal(getLeftCards(), zpMap.get(player.getUserId()));
                    if (cardList != null && cardList.size() > 0) {
                        zpMap.remove(player.getUserId());
                        card = cardList.get(0);
                        getLeftCards().remove(card);
                    }
                }
            }
            if(card == null){
                card = getNextCard();
            }
        }

        addPlayLog(player.getSeat(), PenghzDisAction.action_mo + "", (card == null ? 0 : card.getId()) + "");

        StringBuilder sb = new StringBuilder("PengHz");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append("moPai");
        sb.append("|").append(card);
        LogUtil.msgLog.info(sb.toString());

        if (card != null) {
            if (isTest()) {
                sleep();

            }
            // PenghuziCheckCardBean checkAutoDis = checkAutoDis(player, true);
            // if (checkAutoDis != null) {
            // playAutoDisCard(checkAutoDis, true);
            //
            // }

            setMoFlag(1);
            setMoSeat(player.getSeat());
            markMoSeat(card, player.getSeat());
            player.moCard(card);
            setDisCardSeat(player.getSeat());
            setFirstCard(false);
            setNowDisCardIds(new ArrayList<>(Arrays.asList(card)));
            setNowDisCardSeat(getNextDisCardSeat());

            PenghuziCheckCardBean autoDisCard = null;	
            for (Entry<Integer, PenghuziPlayer> entry : seatMap.entrySet()) {
                PenghuziCheckCardBean checkCard = entry.getValue().checkCard(card, entry.getKey() == player.getSeat(), false);
                if (checkPaohuziCheckCard(checkCard)) {
                    autoDisCard = checkCard;
                }

            }

            markMoSeat(player.getSeat(), PenghzDisAction.action_mo);
            if (autoDisCard != null && autoDisCard.getAutoAction() == PenghzDisAction.action_zai) {
                sendMoMsg(player, PenghzDisAction.action_mo, new ArrayList<>(Arrays.asList(card)), PenghzDisAction.action_type_mo);

            } else {
                sendActionMsg(player, PenghzDisAction.action_mo, new ArrayList<>(Arrays.asList(card)), PenghzDisAction.action_type_mo);

            }

            boolean isAutoAc = false;
            if (autoDisBean != null) {
            	isAutoAc = true;
                playAutoDisCard(autoDisBean);
            }

            if (this.leftCards != null && this.leftCards.size() == 0 && actionSeatMap.isEmpty()&&!isAutoAc) {
                calcOver();
                return;
            }

//			if (PenghuziConstant.isAutoMo) {
            // if (actionSeatMap.isEmpty()) {
            // markMoSeat(player.getSeat(), PenghzDisAction.action_mo);
            // }
            // checkMo();
//			}
            checkAutoMo();
        }
    }

    /**
	 * 除了吃和碰之外的都是特殊动作
	 */
    private boolean isHasSpecialAction() {
        boolean b = false;
        for (List<Integer> actionList : actionSeatMap.values()) {
            if (actionList.get(0) == 1 || actionList.get(2) == 1 || actionList.get(3) == 1 || actionList.get(5) == 1 || actionList.get(6) == 1) {
				// 除了吃和碰之外的都是特殊动作
                b = true;
                break;
            }
        }
        return b;
    }

    /**
	 * @return 是否有系统帮助自动出牌
	 */
    private PenghuziCheckCardBean checkDisAction(PenghuziPlayer player, int action, PenghzCard disCard, boolean isFirstCard) {
        PenghuziCheckCardBean autoDisCheck = null;
        for (Entry<Integer, PenghuziPlayer> entry : seatMap.entrySet()) {
            if (entry.getKey() == player.getSeat()) {
                continue;
            }

//            PenghuziCheckCardBean checkCard = entry.getValue().checkCard(disCard, false, isFirstCard);
            PenghuziCheckCardBean checkCard = entry.getValue().checkCard(disCard,false,false,false,isFirstCard,false);
            boolean check = checkPaohuziCheckCard(checkCard);
            if (check) {
                autoDisCheck = checkCard;
            }
        }
        return autoDisCheck;
    }


    /**
	 * 检查自动提
	 */
    private PenghuziCheckCardBean checkAutoDis(PenghuziPlayer player, boolean isMoPaiIng) {
        PenghuziCheckCardBean checkCard = player.checkTi();
        checkCard.setMoPaiIng(isMoPaiIng);
        boolean check = checkPaohuziCheckCard(checkCard);
        if (check) {
            return checkCard;
        } else {
            return null;
        }
    }

    public boolean checkPaohuziCheckCard(PenghuziCheckCardBean checkCard) {
        List<Integer> list = checkCard.getActionList();
        if (list == null || list.isEmpty()) {
            return false;
        }

        addAction(checkCard.getSeat(), list);
        List<PenghzCard> autoDisList = checkCard.getAutoDisList();
        if (autoDisList != null||list.get(0)==1) {
        	
        	//四清的时候选了强制胡牌可胡牌，如果有五福不能强制胡
//        	if(list.get(0)==1){
//        		PenghuziPlayer player =	seatMap.get(checkCard.getSeat());
//        		if(player!=null&&qiangzhiHu==1){
//        			int count = player.getPWPTCount(null);
//        			int hanc = PenghuziTool.getHandPaiCo(player.getHandPais());
//        			if(count==4&& player.getHandPais().size()==3&&hanc==2){
//        				return false;
//        			}
//        		}
//        	}
        	
            if (checkCard.getAutoAction()!=0) {
            	if(autoDisBean!=null&&checkCard.getAutoAction()==PenghzDisAction.action_hu&&autoDisBean.getSeat()!=checkCard.getSeat()){
            		if(autoDisBean.getAutoAction()==PenghzDisAction.action_zai||autoDisBean.getAutoAction()==PenghzDisAction.action_ti||autoDisBean.getAutoAction()==PenghzDisAction.action_chouzai){
            			 return false;
            		}
            		
            		if(autoDisBean.getAutoAction()==PenghzDisAction.action_hu){
            			//两个都是胡
            			int seat1= autoDisBean.getSeat();
            			int seat2 = checkCard.getSeat();
            			int nextSeat = getNearSeat(getMoSeat(), Arrays.asList(seat1, seat2));
            			if(nextSeat==seat1){
            				return false;
            			}
            		}
            		
            		
            		
            	}
            	
                setAutoDisBean(checkCard);
                return true;
            }
        }
        return false;

    }

    public void setAutoDisBean(PenghuziCheckCardBean autoDisBean) {
        this.autoDisBean = autoDisBean;
        changeExtend();
    }

    private void addAction(int seat, List<Integer> actionList) {
        actionSeatMap.put(seat, actionList);
        addPlayLog(seat, PenghzDisAction.action_hasaction + "", StringUtil.implode(actionList));
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
        saveActionSeatMap();
    }

    private void clearHuList() {
        huConfirmList.clear();
        changeExtend();
    }

    public void saveActionSeatMap() {
        dbParamMap.put("nowAction", JSON_TAG);
    }
    
    
    public void wufuXz(PenghuziPlayer player){
    	if(nowDisCardIds.isEmpty()){
    		return;
    	}
       PenghuziCheckCardBean autoDisCard = checkDisAction(player, 0, nowDisCardIds.get(0), false);
        sendActionMsg(player, 0, new ArrayList<>(), PenghzDisAction.action_type_dis);
        
        sendTingInfo(player);
        
    }
    

    private void sendActionMsg(PenghuziPlayer player, int action, List<PenghzCard> cards, int actType) {
        sendActionMsg(player, action, cards, actType, false, false);
    }

    /**
	 * 发送所有玩家动作msg
	 *
	 * @param player
	 * @param action
	 * @param cards
	 * @param actType
	 */
    private void sendMoMsg(PenghuziPlayer player, int action, List<PenghzCard> cards, int actType) {
        PlayPaohuziRes.Builder builder = PlayPaohuziRes.newBuilder();
        builder.setAction(action);
        builder.setUserId(player.getUserId() + "");
        builder.setSeat(player.getSeat());
        builder.setHuxi(player.getPoint());
        // builder.setNextSeat(nowDisCardSeat);
        setNextSeatMsg(builder);
        builder.setRemain(leftCards.size());
        builder.addAllPhzIds(PenghuziTool.toPhzCardIds(cards));
        builder.setActType(actType);
        sendMoMsgBySelfAction(builder, player.getSeat());
    }

    /**
	 * 发送该玩家动作msg
	 */
    private void sendPlayerActionMsg(PenghuziPlayer player) {
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return;
        }
        PlayPaohuziRes.Builder builder = PlayPaohuziRes.newBuilder();
        builder.setAction(PenghzDisAction.action_refreshaction);
        builder.setUserId(player.getUserId() + "");
        builder.setSeat(player.getSeat());
        builder.setHuxi(player.getPoint());
        // builder.setNextSeat(nowDisCardSeat);
        setNextSeatMsg(builder);
        if (leftCards != null) {
            builder.setRemain(leftCards.size());
        }
        // builder.addAllPhzIds(PenghuziTool.toPhzCardIds(nowDisCardIds));
        builder.setActType(0);
        KeyValuePair<Boolean, Integer> zaiKeyValue = getZaiOrTiKeyValue();
        List<Integer> actionList = getSendSelfAction(zaiKeyValue, player.getSeat(), actionSeatMap.get(player.getSeat()));
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
    private void sendActionMsg(PenghuziPlayer player, int action, List<PenghzCard> cards, int actType, boolean isZaiPao, boolean isChongPao) {
        PlayPaohuziRes.Builder builder = PlayPaohuziRes.newBuilder();
        builder.setAction(action);
        builder.setUserId(player.getUserId() + "");
        builder.setSeat(player.getSeat());
        builder.setHuxi(player.getPoint());
        setNextSeatMsg(builder);
        if (leftCards != null) {
            builder.setRemain(leftCards.size());

        }
        builder.addAllPhzIds(PenghuziTool.toPhzCardIds(cards));
        builder.setActType(actType);
        if (isZaiPao) {
            builder.setIsZaiPao(1);
        }
        if (isChongPao) {
            builder.setIsChongPao(1);
        }
        sendMsgBySelfAction(builder);
    }

    /**
	 * 目前的动作中是否有人有栽或者是提
	 *
	 * @return
	 */
    private KeyValuePair<Boolean, Integer> getZaiOrTiKeyValue() {
        KeyValuePair<Boolean, Integer> keyValue = new KeyValuePair<>();
        boolean isHasZaiOrTi = false;
        int zaiSeat = 0;
        for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
            if (entry.getValue().get(2) == 1 || entry.getValue().get(3) == 1) {
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
        boolean isHasZaiOrTi = zaiKeyValue.getId();
        int zaiSeat = zaiKeyValue.getValue();
        if (isHasZaiOrTi) {
            if (zaiSeat == seat) {
                return actionList;
            }
        } else if (actionList.get(0) == 1) {
            return actionList;
        } else if (actionList.get(5) == 1) {
            if (sendPaoSeat == seat) {
                return actionList;
            }
        } else if (actionList.get(2) == 1 || actionList.get(3) == 1) {
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
        PenghuziPlayer winPlayer = seatMap.get(lastWinSeat);
        for (PenghuziPlayer player : seatMap.values()) {
            PlayPaohuziRes.Builder copy = builder.clone();
            copy.setHuxi(player.getPoint());
             // 需要特殊处理一下栽
//                if (copy.getAction() == PenghzDisAction.action_zai) {
                    if (copy.getSeat() != player.getSeat()) {
    						// 需要替换成0
                            List<Integer> ids = PenghuziTool.toPhzCardZeroIds(copy.getPhzIdsList());
                            ids.set(0, 0);
                            copy.clearPhzIds();
                            copy.addAllPhzIds(ids);
                    }
//                }
            
            if (actionSeatMap.containsKey(player.getSeat())) {
                List<Integer> actionList = getSendSelfAction(zaiKeyValue, player.getSeat(), actionSeatMap.get(player.getSeat()));
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
    private void sendMsgBySelfAction(PlayPaohuziRes.Builder builder) {
        KeyValuePair<Boolean, Integer> zaiKeyValue = getZaiOrTiKeyValue();

        int actType = builder.getActType();
        boolean noShow = false;
        // boolean hasHu = false;
        int paoSeat = 0;
        if (PenghzDisAction.action_type_dis == actType || PenghzDisAction.action_type_mo == actType) {
            for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
                if (1 == entry.getValue().get(5)) {
                    noShow = true;
                    paoSeat = entry.getKey();
                }

                // if (1 == entry.getValue().get(0)) {
                // hasHu = true;
                // }
            }

            // if (hasHu) {
            // noShow = false;
            // }
        }

        PenghuziPlayer winPlayer = seatMap.get(lastWinSeat);

        for (PenghuziPlayer player : seatMap.values()) {
            PlayPaohuziRes.Builder copy = builder.clone();
            if (copy.getSeat() == player.getSeat()) {
                copy.setHuxi(player.getPoint());
                if(player.isAutoPlay() && copy.getActType() == PenghzDisAction.action_type_dis){
                	copy.setActType(PenghzDisAction.action_type_autoplaydis);
                }
            }
            
			// 需要特殊处理一下栽
            if (copy.getAction() == PenghzDisAction.action_zai) {
                if (copy.getSeat() != player.getSeat()) {
						// 需要替换成0
                        List<Integer> ids = PenghuziTool.toPhzCardZeroIds(copy.getPhzIdsList());
                        copy.clearPhzIds();
                        copy.addAllPhzIds(ids);
                }
            }

            if (actionSeatMap.containsKey(player.getSeat())) {
                List<Integer> actionList = getSendSelfAction(zaiKeyValue, player.getSeat(), actionSeatMap.get(player.getSeat()));
                if (actionList != null) {
                    // copy.addAllSelfAct(actionList);
                    if (noShow && paoSeat != player.getSeat()) {
						// 出牌时，别人有跑的情况不提示吃碰
                        if (1 == actionList.get(0)) {
                            copy.addAllSelfAct(actionList);
                        }
                    } else {
                        copy.addAllSelfAct(actionList);
                    }
                }

            }         
  
            player.writeSocket(copy.build());

            if (copy.getSelfActList() != null && copy.getSelfActList().size() > 0) {
                StringBuilder sb = new StringBuilder("PengHz");
                sb.append("|").append(getId());
                sb.append("|").append(getPlayBureau());
                sb.append("|").append(player.getUserId());
                sb.append("|").append(player.getSeat());
                sb.append("|").append(player.isAutoPlay() ? 1 : 0);
                sb.append("|").append("actList");
                sb.append("|").append(PenghuziCheckCardBean.actionListToString(actionSeatMap.get(player.getSeat())));
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
        PenghuziPlayer disCSMajiangPlayer = seatMap.get(disCardSeat);
        if(disCSMajiangPlayer==null){
        	return;
        }
        
        PenghuziResTool.buildPlayRes(disBuilder, disCSMajiangPlayer, 0, null);
        disBuilder.setRemain(leftCards.size());
        disBuilder.setHuxi(disCSMajiangPlayer.getPoint());
        // disBuilder.setNextSeat(nowDisCardSeat);
        setNextSeatMsg(disBuilder);
        for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
            PlayPaohuziRes.Builder copy = disBuilder.clone();
            List<Integer> actionList = entry.getValue();
            copy.addAllSelfAct(actionList);
            PenghuziPlayer seatPlayer = seatMap.get(entry.getKey());
            seatPlayer.writeSocket(copy.build());
        }

    }

    public void checkAction() {
//        int nowSeat = getNowDisCardSeat();
		// 先判断拿牌的玩家
//        PenghuziPlayer nowPlayer1 = seatMap.get(nowSeat);
//        if (nowPlayer == null) {
//            return;
//        }
    }

	private void checkQiShouZaiTi() {
		StringBuilder sb = new StringBuilder("PengHzAutoTK");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        LogUtil.msgLog.info(sb.toString());
    	try {
    		   //检查4坎
    		for (int i = 0; i < 4; i++) {
    			for (PenghuziPlayer player : seatMap.values()) {
    				if (player.getHandPhzs().isEmpty()) {
    					continue;
    				}
    				PenghuziCheckCardBean checkCard = player.checkCard(null, true, true, false);
    				if (checkPaohuziCheckCard(checkCard)) {
    					// if(!checkCard.isHu()){
    					playAutoDisCard(checkCard);
    					// }
    					tiLong(player);
    				}
    			}
    		}
		} catch (Exception e) {
			 LogUtil.errorLog.info(sb.toString()+" " + e);
		}
     
        checkSendActionMsg();
	}

    /**
	 * 自动出牌
	 */
    private void playAutoDisCard(PenghuziCheckCardBean checkCard) {
        playAutoDisCard(checkCard, false);
    }

    /**
	 * 自动出牌
	 *
	 * @param moPai
	 *            是否是摸牌 如果是摸牌，需要
	 */
    private void playAutoDisCard(PenghuziCheckCardBean checkCard, boolean moPai) {
        if (checkCard.getActionList() != null) {
            int seat = checkCard.getSeat();
            PenghuziPlayer player = seatMap.get(seat);
            if (player.isRobot()) {
                sleep();
            }
            List<Integer> list = PenghuziTool.toPhzCardIds(checkCard.getAutoDisList());
            play(player, list, checkCard.getAutoAction(), moPai, false, checkCard.isPassHu());

            if (actionSeatMap.isEmpty()) {
                setAutoDisBean(null);
            }
        }

    }

    private void sleep() {
        try {
            Thread.sleep(1500);
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
                PenghuziPlayer player = seatMap.get(nextseat);
                if (player != null && player.isRobot()) {
					// 普通出牌
                    PenghuziHandCard paohuziHandCardBean = player.getPaohuziHandCard();
                    int card = RobotAI.getInstance().outPaiHandle(0, PenghuziTool.toPhzCardIds(paohuziHandCardBean.getOperateCards()), new ArrayList<Integer>());
                    if (card == 0) {
                        return;
                    }
                    sleep();
                    List<Integer> cardList = new ArrayList<>(Arrays.asList(card));
                    play(player, cardList, 0);
                }
            } else {
                // (Entry<Integer, List<Integer>> entry :
                // actionSeatMap.entrySet())
                Iterator<Integer> iterator = actionSeatMap.keySet().iterator();
                while (iterator.hasNext()) {
                    Integer key = iterator.next();
                    List<Integer> value = actionSeatMap.get(key);
                    PenghuziPlayer player = seatMap.get(key);
                    if (player == null || !player.isRobot()) {
						// player.writeErrMsg(player.getName() + " 有动作" +
                        // entry.getValue());
                        continue;
                    }
                    List<Integer> actions = PenghzDisAction.parseToDisActionList(value);
                    for (int action : actions) {
                        if (!checkAction(player, action,null,null)) {
                            continue;
                        }
                        sleep();
                        if (action == PenghzDisAction.action_hu) {
							broadMsg(player.getName() + "胡牌");
                            play(player, null, action);
                        } else if (action == PenghzDisAction.action_peng) {
                            play(player, null, action);

                        } else if (action == PenghzDisAction.action_chi) {
                            play(player, null, action);

                        } else if (action == PenghzDisAction.action_pao) {
                            // play(player, null, action);
                        } else if (action == PenghzDisAction.action_ti) {
                            // play(player,
                            // PenghuziTool.toPhzCardIds(nowDisCardIds), action);
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
        setSendPaoSeat(0);
        setZaiCard(null);
        setBeRemoveCard(null);
        setAutoDisBean(null);
        clearMarkMoSeat();
        clearMoSeatPair();
        clearHuList();
        setLeftCards(null);
        setStartLeftCards(null);
        setMoFlag(0);
        setMoSeat(0);
        clearAction();
        setNowDisCardSeat(0);
        setNowDisCardIds(null);
        setFirstCard(true);
        timeNum = 0 ;
        clearTempAction();
        finishFapai=0;
        setPaoHu(0);
        disCardCout1 =0;
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
                tempMap.put("nowDisCardIds", StringUtil.implode(PenghuziTool.toPhzCardIds(nowDisCardIds), ","));
            }
            if (tempMap.containsKey("leftPais")) {
                tempMap.put("leftPais", StringUtil.implode(PenghuziTool.toPhzCardIds(leftCards), ","));
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
//		JsonWrapper wrapper = new JsonWrapper("");
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
            wrapper.putInt(7, zaiCard.getId());
        }
        wrapper.putInt(8, sendPaoSeat);
        wrapper.putInt(9, firstCard ? 1 : 0);
        if (beRemoveCard != null) {
            wrapper.putInt(10, beRemoveCard.getId());
        }
        wrapper.putInt(12, maxPlayerCount);
        wrapper.putString("startLeftCards", startLeftCardsToJSON());
        wrapper.putInt(13, ceiling);
        wrapper.putInt(15, isLianBanker);
        wrapper.putInt("catCardCount", catCardCount);

        wrapper.putInt(17, jiaBei);
        wrapper.putInt(18, jiaBeiFen);
        wrapper.putInt(19, jiaBeiShu);
        wrapper.putInt(20, autoPlayGlob);
        wrapper.putInt(21, autoTimeOut);
        JSONArray tempJsonArray = new JSONArray();
        for (int seat : tempActionMap.keySet()) {
            tempJsonArray.add(tempActionMap.get(seat).buildData());
        }
        wrapper.putString("22", tempJsonArray.toString());
        wrapper.putInt(24, finishFapai);
        wrapper.putInt(25, below);
        wrapper.putInt(26, belowAdd);
        wrapper.putInt(27, paoHu);
        
        wrapper.putInt(28, disCardCout1);
        
        
        wrapper.putInt(29, randomSeat);
        wrapper.putInt(30, noDui);
        wrapper.putInt(31, fanZhongZhuang);
        wrapper.putInt(32, daNiaoWF);
        wrapper.putInt(33, xiaoQiDuiWF);
        wrapper.putInt(34, daNiaoVal);
        wrapper.putInt(35, suiJiZhuang);
        wrapper.putInt(36, qiangzhiHu);
        
        
        return wrapper;
    }

    private String startLeftCardsToJSON() {
        JSONArray jsonArray = new JSONArray();
        for (int card : startLeftCards) {
            jsonArray.add(card);
        }
        return jsonArray.toString();
    }

    @Override
    public void fapai() {
        synchronized (this){
            if (maxPlayerCount<=1||maxPlayerCount>4){
                return;
            }

            changeTableState(table_state.play);
            deal();
        }
    }

    @Override
    protected void deal() {
//    	if (playedBureau<=0){
//			for (PenghuziPlayer player : playerMap.values()) {
//				player.setAutoPlay(false,this);
//				player.setLastOperateTime(System.currentTimeMillis());
//			}
//		}
    	actionSeatMap.clear();
    	
        for (PenghuziPlayer player : playerMap.values()) {
            if(!player.isAlreadyMo())
                player.setLastCheckTime(0);
        }
    	if (isGoldRoom()){
			List<Long> list0=new ArrayList<>(3);
			try {
				List<HashMap<String, Object>> list = GoldRoomDao.getInstance().loadRoomUsersLastResult(playerMap.keySet(),id);
				if(list!=null){
					for (HashMap<String, Object> map:list){
						if (NumberUtils.toInt(String.valueOf(map.getOrDefault("gameResult","0")),0)>0){
							list0.add(NumberUtils.toLong(String.valueOf(map.getOrDefault("userId","0")),0));
						}
					}
				}
			}catch (Exception e){
			}
			if (list0.size()>0){
				Long userId=list0.get(new SecureRandom().nextInt(list0.size()));
				Player player = playerMap.get(userId);
				if (player!=null){
					setLastWinSeat(player.getSeat());
				}
			}
			if (lastWinSeat<=0){
				setLastWinSeat(new SecureRandom().nextInt(playerMap.size()));
			}
		}else{
			if(getPlayBureau()==1 && isGroupRoom()){
				if(suiJiZhuang==1){
					 int masterseat = playerMap.get(masterId).getSeat();
			            setLastWinSeat(masterseat);
				}else{
					setLastWinSeat(new Random().nextInt(getMaxPlayerCount())+1);
				}
	    	}
		}
        if (lastWinSeat == 0) {
            int masterseat = playerMap.get(masterId).getSeat();
            setLastWinSeat(masterseat);
        }
        setDisCardSeat(lastWinSeat);
        setNowDisCardSeat(lastWinSeat);
        setMoSeat(lastWinSeat);
        setToPlayCardFlag(1);
        markMoSeat(null, lastWinSeat);
        List<Integer> copy = new ArrayList<>(PenghuziConstant.cardList);
        List<List<PenghzCard>> list = PenghuziTool.fapai(copy, zp,getMaxPlayerCount());
        int i = 1;
        
        
        for (PenghuziPlayer player : playerMap.values()) {
            player.changeState(player_state.play);
            player.getFirstPais().clear();
            if (player.getSeat() == lastWinSeat) {
                player.dealHandPais(list.get(0));
				player.getFirstPais().addAll(PenghuziTool.toPhzCardIds(new ArrayList(list.get(0))));// 将初始手牌保存结算时发给客户端
                continue;
            }
            player.dealHandPais(list.get(i));
			player.getFirstPais().addAll(PenghuziTool.toPhzCardIds(new ArrayList(list.get(i))));// 将初始手牌保存结算时发给客户端
            i++;

            if(noDui==1){
            	int duis = PenghuziTool.getHandPaiCo(player.getHandPais());
            	if(duis==player.getHandPais().size()){
            		player.setHasNoDui(true);
            	}
            }
            StringBuilder sb = new StringBuilder("PengHz");
            sb.append("|").append(getId());
            sb.append("|").append(getPlayBureau());
            sb.append("|").append(player.getUserId());
            sb.append("|").append(player.getSeat());
            sb.append("|").append(player.getName());
            sb.append("|").append("fapai");
            sb.append("|").append(player.getHandPhzs());
            LogUtil.msgLog.info(sb.toString());
        }

        List<PenghzCard> cardList = new ArrayList<>(list.get(list.size()-1));
        if (maxPlayerCount<=2){
           // cardList.addAll(list.get(2));
        }
        int size = cardList.size();
		// 抽排
//        chouCards=PenghuziTool.toPhzCardIds(cardList.subList(size-catCardCount,size));
//        switch (catCardCount){
//            case 20:
//                cardList=cardList.subList(0,cardList.size()-catCardCount);
//                break;
//            case 10:
//                cardList=cardList.subList(0,cardList.size()-catCardCount);
//                break;
//        }

		// 桌上所有剩余牌
        setStartLeftCards(PenghuziTool.toPhzCardIds(cardList));

		// 桌上剩余的牌
        if (catCardCount<=0){
            setLeftCards(cardList);
        }else if (catCardCount>=cardList.size()){
            setLeftCards(null);
        }else{
            setLeftCards(new ArrayList<>(cardList.subList(catCardCount,cardList.size())));
        }
        finishFapai=1;

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

    @Override
    public Map<Integer, PenghuziPlayer> getSeatMap() {
      //  Object o = seatMap;
        return  seatMap;
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

        KeyValuePair<Boolean, Integer> zaiKeyValue = getZaiOrTiKeyValue();
        int autoCheckTime = 0;
        List<PlayerInTableRes> players = new ArrayList<>();
        for (PenghuziPlayer player : playerMap.values()) {
            PlayerInTableRes.Builder playerRes = player.buildPlayInTableInfo(userId, isrecover);
            if (playerRes==null){
                continue;
            }
			// 是否为庄
            playerRes.addRecover((player.getSeat() == lastWinSeat) ? 1 : 0);
            if (player.getUserId() == userId) {
                    playerRes.addAllHandCardIds(player.getHandPais());
                    if (actionSeatMap.containsKey(player.getSeat())) {
                        List<Integer> actionList = getSendSelfAction(zaiKeyValue, player.getSeat(), actionSeatMap.get(player.getSeat()));
                        if (actionList != null && !tempActionMap.containsKey(player.getSeat()) && !huConfirmList.contains(player.getSeat())) {
                            playerRes.addAllRecover(actionList);
                        }
                    
                }
            }
            players.add(playerRes.build());

            if (autoPlay && player.isCheckAuto()) {
                int timeOut = autoTimeOut;
                if (player.getAutoPlayCheckedTime() >= autoTimeOut && !player.isAutoPlayCheckedTimeAdded()) {
                    timeOut = autoTimeOut2;
                }
                autoCheckTime = timeOut - (int) (System.currentTimeMillis() - player.getLastCheckTime());
            }
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
        res.addExt(nowDisCardSeat); // 0
        res.addExt(payType);// 1
		// 下标二
        res.addExt(ceiling);// 2
        res.addExt(isLianBanker);// 3
        
        res.addExt(modeId.length()>0?Integer.parseInt(modeId):0);//4
		int ratio;
		int pay;
		if (isGoldRoom()){
			ratio = GameConfigUtil.loadGoldRatio(modeId);
			pay = PayConfigUtil.get(playType,totalBureau,getMaxPlayerCount(),payType == 1 ? 0 : 1,modeId);
		}else{
			ratio = 1;
			pay = consumeCards()?loadPayConfig(payType):0;
		}
		res.addExt(ratio);// 5
		res.addExt(pay);// 6
        res.addExt(catCardCount);// 7

        res.addExt(creditMode);     // 8
        res.addExt(getTableStatus()); //9
       
//        res.addExt(creditJoinLimit);// 11
//        res.addExt(creditDissLimit);// 12
//        res.addExt(creditDifen);    // 13
//        res.addExt(creditCommission);// 14

        res.addExt(0);
        res.addExt(0);
        res.addExt(0);
        res.addExt(0);
        res.addExt(creditCommissionMode1);// 15
        res.addExt(creditCommissionMode2);// 16
        res.addExt(autoPlay ? 1 : 0);// 17
        res.addExt(jiaBei);// 18
        res.addExt(jiaBeiFen);// 19
        res.addExt(jiaBeiShu);// 20

        res.addTimeOut((isGoldRoom() || autoPlay) ?(int)autoTimeOut:0);
        res.addTimeOut(autoCheckTime);
        res.addTimeOut((isGoldRoom() || autoPlay) ?(int) autoTimeOut2 :0);
        return res.build();
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

    public void calcPointBeforeOver() {
    	
    	
    	// 大结算计算加倍分
        if(jiaBei == 1){
            int jiaBeiPoint = 0;
            int loserCount = 0;
                for (PenghuziPlayer player : seatMap.values()) {
                    if (player.getTotalPoint() > 0 && player.getTotalPoint() < jiaBeiFen) {
                        jiaBeiPoint += player.getTotalPoint() * (jiaBeiShu - 1);
                        player.setTotalPoint(player.getTotalPoint() * jiaBeiShu);
                    } else if (player.getTotalPoint() < 0) {
                        loserCount++;
                    }
                }
                if (jiaBeiPoint > 0) {
                    for (PenghuziPlayer player : seatMap.values()) {
                        if (player.getTotalPoint() < 0) {
                            player.setTotalPoint(player.getTotalPoint() - (jiaBeiPoint / loserCount));
                        }
                    }
                }

//             
        }
        
        //大结算低于below分+belowAdd分
        if(belowAdd>0&&playerMap.size()==2){
            for (PenghuziPlayer player : seatMap.values()) {
                int totalPoint = player.getTotalPoint();
                if (totalPoint >-below&&totalPoint<0) {
                    player.setTotalPoint(player.getTotalPoint()-belowAdd);
                }else if(totalPoint < below&&totalPoint>0){
                    player.setTotalPoint(player.getTotalPoint()+belowAdd);
                }
            }
        }
    	
    	
    }
    
    


    public ClosingPhzInfoRes.Builder sendAccountsMsg(boolean over, List<Integer> winList, int winFen, int mt, boolean isBreak,int paoPlayer,int lastLianz) {
        List<ClosingPhzPlayerInfoRes> list = new ArrayList<>();
        List<ClosingPhzPlayerInfoRes.Builder> builderList = new ArrayList<>();
        PenghuziPlayer winPlayer = null;
       


        //大结算低于below分+belowAdd分
//        if(over&&belowAdd>0&&playerMap.size()==2){
//                for (PenghuziPlayer player : seatMap.values()) {
//                    long totalPoint = player.getWinLoseCredit();
//                    if (totalPoint >-below&&totalPoint<0) {
//                        player.setWinLossPoint(player.getWinLossPoint()-belowAdd);
//                    }else if(totalPoint < below&&totalPoint>0){
//                        player.setWinLossPoint(player.getWinLossPoint()+belowAdd);
//                    }
//                }
//        }

        for (PenghuziPlayer player : seatMap.values()) {
            if (winList != null && winList.contains(player.getSeat())) {
                winPlayer = seatMap.get(player.getSeat());
            }
            ClosingPhzPlayerInfoRes.Builder build;
            if (over) {
                build = player.bulidTotalClosingPlayerInfoRes(true);
            } else {
                build = player.bulidOneClosingPlayerInfoRes(false);
            }
			build.addAllFirstCards(player.getFirstPais());// 将初始手牌装入网络对象
            for(int action : player.getActionTotalArr()){
            	build.addStrExt(action+"");
    		}
           
            builderList.add(build);

			// 信用分
            if(isCreditTable()){
                    player.setWinLoseCredit(player.getTotalPoint() * creditDifen);
                
            }
        }

		// 信用分计算
        if (isCreditTable()) {
			// 计算信用负分
            calcNegativeCredit();

            long dyjCredit = 0;
            for (PenghuziPlayer player : seatMap.values()) {
                if (player.getWinLoseCredit() > dyjCredit) {
                    dyjCredit = player.getWinLoseCredit();
                }
            }
            for (ClosingPhzPlayerInfoRes.Builder builder : builderList) {
                PenghuziPlayer player = seatMap.get(builder.getSeat());
                calcCommissionCredit(player, dyjCredit);

                builder.addStrExt(player.getWinLoseCredit() + "");      //8
                builder.addStrExt(player.getCommissionCredit() + "");   //9
				// 2019-02-26更新
                builder.setWinLoseCredit(player.getWinLoseCredit());
                builder.setCommissionCredit(player.getCommissionCredit());
            }
        } else if (isGroupTableGoldRoom()) {
            // -----------亲友圈金币场---------------------------------
            for (PenghuziPlayer player : seatMap.values()) {
                player.setWinGold(player.getTotalPoint() * gtgDifen);
            }
            calcGroupTableGoldRoomWinLimit();
            for (ClosingPhzPlayerInfoRes.Builder builder : builderList) {
                PenghuziPlayer player = seatMap.get(builder.getSeat());
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
            PenghuziPlayer player = seatMap.get(builder.getSeat());
            list.add(builder.build());
        }

        ClosingPhzInfoRes.Builder res = ClosingPhzInfoRes.newBuilder();
        res.addAllLeftCards(PenghuziTool.toPhzCardIds(leftCards));
        res.setFan(mt);
        int nextZC =1;
        if (winPlayer != null) {
			res.setTun(0);// 
//            res.setFan(winFen);
            res.setHuxi(winPlayer.getPoint());
            res.setTotalTun(lastLianz);
            nextZC = winPlayer.getLianZhuangC();
            res.setHuSeat(winPlayer.getSeat());
            if (winPlayer.getHu() != null && winPlayer.getHu().getCheckCard() != null) {
                res.setHuCard(winPlayer.getHu().getCheckCard().getId());
            }
            res.addAllCards(winPlayer.buildPhzHuCards());
        }
        res.addAllClosingPlayers(list);
        res.setIsBreak(isBreak ? 1 : 0);
        res.setWanfa(getWanFa());
        res.addAllExt(buildAccountsExt(over,paoPlayer,nextZC));
        res.addAllStartLeftCards(startLeftCards);
        res.addAllIntParams(getIntParams());
        
        if (over && isGroupRoom() && !isCreditTable()) {
            res.setGroupLogId((int) saveUserGroupPlaylog());
        }
        for (PenghuziPlayer player : seatMap.values()) {
            player.writeSocket(res.build());
        }
        return res;
    }
    
    
    
    


    @Override
    public void sendAccountsMsg() {
    	
    	if(playedBureau==0&&getState()!=table_state.play){
    		return;
    	}
    	int pdu =playedBureau;
        for (PenghuziPlayer seat : seatMap.values()) {
        	seat.changeTotalPoint(seat.getPoint());
        	if(!seat.getHandPais().isEmpty()){
        		if(playedBureau==0){
          		  playedBureau = 1;
          		  consume();
          		setTiqianDiss(true);
                }else if(pdu!=0){
                	playedBureau += 1;
                	pdu = 0;
                	setTiqianDiss(true);
                }
        	}
        }
      
        ClosingPhzInfoRes.Builder res = sendAccountsMsg(true, null, 0, 0, true,0,1);
        saveLog(true,0L, res.build());
    }
    
    
    public boolean isDissSendAccountsMsg() {
    	
        return true;
    }
    public boolean isCommonOver() {
    	if(tiqianDiss){
    		return !tiqianDiss;
    	}
        return playedBureau == totalBureau  ;
    }
    
    

    public List<String> buildAccountsExt(boolean isOver,int paoSeat,int nextZC) {
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
		ext.add(isOver ? dissInfo() : "");
		// 金币场大于0
		ext.add(modeId);
		int ratio;
		int pay;
	
			ratio = 1;
			pay = loadPayConfig(payType);
		
		ext.add(String.valueOf(ratio));
		ext.add(String.valueOf(pay>=0?pay:0));
        ext.add(isGroupRoom()?loadGroupId():"");//13
        ext.add(String.valueOf(catCardCount));//14
        ext.add(paoSeat+"");//15
        ext.add(GameUtil.play_type_penghuzi+"");//16
        if(isLianBanker==3){
        	ext.add(1+"");//16
        }else{
        	ext.add((nextZC+1)+"");//16
        }
        
        

		// 信用分
        ext.add(creditMode + ""); //15
        ext.add(creditJoinLimit + "");//16
        ext.add(creditDissLimit + "");//17
        ext.add(creditDifen + "");//18
        ext.add(creditCommission + "");//19
        ext.add(creditCommissionMode1 + "");//20
        ext.add(creditCommissionMode2 + "");//21
        ext.add(autoPlay ? "1" : "0");//20
        ext.add(jiaBei + "");//22
        ext.add(jiaBeiFen + "");//23
        ext.add(jiaBeiShu + "");//24
        return ext;
    }
	private String dissInfo(){
    	JSONObject jsonObject = new JSONObject();
    	if(getSpecialDiss() == 1){
			jsonObject.put("dissState", "1");// 群主解散
        }else{
            if(answerDissMap != null && !answerDissMap.isEmpty()){
				jsonObject.put("dissState", "2");// 玩家申请解散
                StringBuilder str = new StringBuilder();
                for(Entry<Integer, Integer> entry : answerDissMap.entrySet()){
                    Player player0 = getSeatMap().get(entry.getKey());
                    if(player0 != null){
                        str.append(player0.getUserId()).append(",");
                    }
                }
                if(str.length()>0){
                    str.deleteCharAt(str.length()-1);
                }
                jsonObject.put("dissPlayer", str.toString());
            }else{
				jsonObject.put("dissState", "0");// 正常打完
            }
        }
    	return jsonObject.toString();
    }

    @Override
    public int getMaxPlayerCount() {
        return maxPlayerCount;
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
		info.setExtend(buildExtend());
		TableDao.getInstance().save(info);
		loadFromDB(info);
		return true;
	}
    
    public boolean createSimpleTable(Player player, int play, int bureauCount, List<Integer> params, List<String> strParams, boolean saveDb) throws Exception {
		return createTable(player,play,bureauCount,params,saveDb);
	}
    
    public void createTable(Player player, int play, int bureauCount, List<Integer> params) throws Exception {
		createTable(player,play,bureauCount,params,true);
	}
    
    @Override
    public boolean createTable(CreateTableInfo createTableInfo) throws Exception {
  	  Player player = createTableInfo.getPlayer();
      int play = createTableInfo.getPlayType();
      int bureauCount =createTableInfo.getBureauCount();
      int tableType = createTableInfo.getTableType();
      List<Integer> params = createTableInfo.getIntParams();
     // List<String> strParams = createTableInfo.getStrParams();
      boolean saveDb = createTableInfo.isSaveDb();
    	
    	 long id = getCreateTableId(player.getUserId(), play);
         if (id<=0){
 			return false;
 		}
         if(saveDb){
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
         }else{
         	setPlayType(play);
 			setDaikaiTableId(daikaiTableId);
 			this.id=id;
 			this.totalBureau=bureauCount;
 			this.playBureau=1;
         }
         
 		int playerCount = StringUtil.getIntValue(params, 7, 0);// 比赛人数
 		
 		payType = StringUtil.getIntValue(params, 2, 1);// 支付方式

 		int catCardCount = StringUtil.getIntValue(params, 3, 0);// 除掉的牌数量
 		
 		qiangzhiHu= StringUtil.getIntValue(params, 4, 0);//强制胡牌1：强制 2不强制
 		suiJiZhuang= StringUtil.getIntValue(params, 5, 0);//1先进坐庄 2随机
 		
 		 isLianBanker = StringUtil.getIntValue(params, 6, 0);// 1连中 2：x2
 		 
 		 int time    = StringUtil.getIntValue(params, 8, 0);
 	        autoPlayGlob = StringUtil.getIntValue(params, 9, 0);
 		// 加倍：0否，1是
 		this.jiaBei = StringUtil.getIntValue(params, 10, 0);
 		// 加倍分
 		this.jiaBeiFen = StringUtil.getIntValue(params, 11, 0);
 		// 加倍数
 		this.jiaBeiShu = StringUtil.getIntValue(params, 12, 0);
 		
 		if(time>0){
 			this.autoPlay =true;
 		}

         setMaxPlayerCount(playerCount);
         if(maxPlayerCount==2){
             int belowAdd = StringUtil.getIntValue(params, 14, 0);
             if(belowAdd<=100&&belowAdd>=0)
                 this.belowAdd=belowAdd;
             int below = StringUtil.getIntValue(params, 13, 0);
             if(below<=100&&below>=0){
                 this.below=below;
                 if(belowAdd>0&&below==0)
                     this.below=10;
             }
         }
         
         
         this.randomSeat = StringUtil.getIntValue(params, 15, 0);
         this.noDui = StringUtil.getIntValue(params, 16, 0);
         this.fanZhongZhuang = StringUtil.getIntValue(params, 17, 0);
         this.daNiaoWF = StringUtil.getIntValue(params, 18, 0);
         this.daNiaoVal = StringUtil.getIntValue(params, 19, 0);
         this.xiaoQiDuiWF = StringUtil.getIntValue(params, 20, 0);
         
         
         
         
         if (playerCount<=1||playerCount>4){
             return false;
         }
         if(playerCount == 3 || playerCount == 4){
             catCardCount = 0 ;
         }
         this.catCardCount = catCardCount;

         if(this.getMaxPlayerCount() != 2){
             jiaBei = 0 ;
         }
             if(autoPlay){
//             	 time = StringUtil.getIntValue(params, 23, 0);
             	if(time ==1) {
             		time=60;
             	}
             	autoTimeOut2 =autoTimeOut =time*1000 ;
             }
         changeExtend();
         LogUtil.msgLog.info("createTable tid:"+getId()+" "+player.getName() + " params"+params.toString());
         return true;
    }
    
    
    public boolean createTable(Player player, int play, int bureauCount, List<Integer> params, boolean saveDb) throws Exception {
    	
    	return createTable(new CreateTableInfo(player, TABLE_TYPE_NORMAL, play, bureauCount, params, strParams, true));
    	
       
    }

    @Override
    public int getWanFa() {
        return SharedConstants.game_type_paohuzi;
    }

    @Override
    public boolean isTest() {
        return PenghuziConstant.isTest;
    }

    @Override
    public void checkReconnect(Player player) {
    	PenghuziPlayer player1 = (PenghuziPlayer) player;
    	if (super.isAllReady() && getDaNiaoWF() >= 1 && getTableStatus() == PenghuziConstant.TABLE_STATUS_DANIAO) {
			if (player1.getDaNiaoPoint() < 0) {
//				ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_table_status_PENGHU_DANAIO,
//						getTableStatus());
//				player1.writeSocket(com.build());
//				return;
			}
		}
    	
    	if(!isBaojingPlayer())
        checkMo();
        
        sendTingInfo(player1);
    }


    @Override
    public void checkAutoPlay() {
        synchronized (this){
            if (getSendDissTime() > 0) {
                for (PenghuziPlayer player : seatMap.values()) {
                    if (player.getLastCheckTime() > 0) {
                        player.setLastCheckTime(player.getLastCheckTime() + 1 * 1000);
                    }
                }
                return;
            }
            
//            if(autoDisBean!=null&&isFirstCard()){
//            	playAutoDisCard(autoDisBean);
//            }
//            
            if (autoPlay && state == table_state.ready && playedBureau > 0&&getTableStatus() != PenghuziConstant.TABLE_STATUS_DANIAO) {
                ++timeNum;
                for (PenghuziPlayer player : seatMap.values()) {
					// 玩家进入托管后，5秒自动准备
                    if (timeNum >= 5 && player.isAutoPlay()) {
                        autoReady(player);
                    } else if (timeNum >= 30) {
                        autoReady(player);
                    }
                }
                return;
            }
            
            
    		if (getTableStatus() == PenghuziConstant.TABLE_STATUS_DANIAO&&autoPlay) {
    			for (int seat : seatMap.keySet()) {
    				PenghuziPlayer player = seatMap.get(seat);
    				if (player.getLastCheckTime() > 0 && player.getDaNiaoPoint() >= 0) {
    					player.setLastCheckTime(player.getLastCheckTime() + 1 * 1000);
    					continue;
    				}
    				
    				if(player.getDaNiaoPoint()>=0){
    					continue;
    				}
    				 boolean auto = player.isAutoPlay();
                     if(!auto){
                         auto = checkPlayerAuto(player,autoTimeOut);
                     }
    				
    				if (auto) {
   					int daNaioFen = 0;
    					if(daNiaoVal==1){
    						if(player.getSeat()==lastWinSeat){
    							daNaioFen = 3;
    						}
    					}else if(daNiaoVal==2){
//    						playDaNiao(player, 3);
    						daNaioFen = 3;
//    						player.setDaNiaoPoint(3);
    					}else if(daNiaoVal==3){
//    						player.setDaNiaoPoint(6);
    						daNaioFen = 8;
    					}else if(daNiaoVal==4){
    						daNaioFen = 0;
    					}
    					playDaNiao(player, daNaioFen);
    				}
    			}
    		
    		}
            
            

            int timeout;
            if(state != table_state.play){
                return;
            }else if(autoPlay){
                timeout = autoTimeOut;
            }else{
                return;
            }
            
         
            //timeout = 10*1000;
            long autoPlayTime = ResourcesConfigsUtil.loadIntegerValue("ServerConfig","autoPlayTimePhz",2*1000);
            long now = TimeUtil.currentTimeMillis();

            if(!actionSeatMap.isEmpty()){
                int action = 0,seat = 0;
                for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()){
                    List<Integer> list = PenghzDisAction.parseToDisActionList(entry.getValue());
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
                    PenghuziPlayer player = seatMap.get(seat);
                    if (player==null){
                        LogUtil.errorLog.error("auto play error:tableId={},seat={} is null,seatMap={},playerMap={}",id,seat,seatMap.keySet(),playerMap.keySet());
                        return;
                    }

                    boolean auto = player.isAutoPlay();
                    if(!auto){
                        auto = checkPlayerAuto(player,timeout);
                    }
                    if(auto){
                        if (player.getAutoPlayTime() == 0L) {
                            player.setAutoPlayTime(now);
                        } else if (player.getAutoPlayTime() > 0L && now - player.getAutoPlayTime() >= autoPlayTime){
                            player.setAutoPlayTime(0L);
                            if(action == PenghzDisAction.action_chi){
                                action = PenghzDisAction.action_pass;
                            }
                            if(action == PenghzDisAction.action_pass || action == PenghzDisAction.action_peng || action == PenghzDisAction.action_hu){
                                play(player, new ArrayList<Integer>(), action);
                            }else{
                                checkMo();
                            }
                        }
                        return;
                    }
                    if(action == PenghzDisAction.action_pao && player.getLastCheckTime()>0){
                        checkMo();
                    }
                }
            }else{
                PenghuziPlayer player = seatMap.get(nowDisCardSeat);
                if (player == null) {
                    return;
                }
                if(toPlayCardFlag==1){
                    boolean auto = player.isAutoPlay();
                    if(!auto){
                        auto = checkPlayerAuto(player,timeout);
                    }
                    if(auto){
                        if (player.getAutoPlayTime() == 0L) {
                            player.setAutoPlayTime(now);
                        } else if (player.getAutoPlayTime() > 0L && now - player.getAutoPlayTime() >= autoPlayTime){
                            player.setAutoPlayTime(0L);
                            PenghzCard paohzCard = PenghuziTool.autoDisCard(player.getHandPhzs());
                            if(paohzCard != null){
                                play(player, Arrays.asList(paohzCard.getId()), 0);
                            }
                        }
                    }
                }else{
                	PenghuziPlayer player2 = seatMap.get(disCardSeat);
                	if(player2!=null && player2.getWufuBaoj()==0){
                		return;
                	}
                    checkMo();
                }
            }
        }
    }

    public boolean checkPlayerAuto(PenghuziPlayer player ,int timeout){
        long now = TimeUtil.currentTimeMillis();
        boolean auto = false;
        if (player.isAutoPlayChecked() || (player.getAutoPlayCheckedTime() >= timeout && !player.isAutoPlayCheckedTimeAdded())) {
            player.setAutoPlayChecked(true);
            timeout = autoTimeOut2;
        }
        long lastCheckTime = player.getLastCheckTime();
        if (lastCheckTime > 0) {
            int checkedTime = (int) (now - lastCheckTime);
//            if (checkedTime > 10 * 1000) {
//                player.addAutoPlayCheckedTime(1 * 1000);
//                if (!player.isAutoPlayCheckedTimeAdded()) {
//                    player.setAutoPlayCheckedTimeAdded(true);
//                    player.addAutoPlayCheckedTime(10 * 1000);
//                }
//                if(!player.isAutoPlayChecked() && player.getAutoPlayCheckedTime() >= timeout){
			// // 推送消息
//                    ComMsg.ComRes msg = SendMsgUtil.buildComRes(133, player.getSeat(), (int) player.getUserId()).build();
//                    broadMsg(msg);
//                    broadMsg0(msg);
//                    auto = true;
//
//                }
//            }
            if (checkedTime >= timeout) {
                auto = true;
            }
            if(auto){
                player.setAutoPlay(true, this);
            }
//            System.out.println("checkPlayerAuto----" + player.getSeat() + "|" + player.getUserId() + "|" + player.getAutoPlayCheckedTime() + "|" + checkedTime + "|" + auto);
        } else {
            player.setLastCheckTime(now);
            player.setCheckAuto(true);
            player.setAutoPlayCheckedTimeAdded(false);
        }

        return auto;
    }
    
    
    
    public void sendTingInfo(PenghuziPlayer player) {
        if(player.isAutoPlay()||player.getHandPais().isEmpty()){
            return;
        }
    	if (player.getHandPhzs().size() % 3 == 0||( (player.getPaoTi() > 0 && player.getHandPhzs().size() % 3 == 2))) {
			// if (actionSeatMap.containsKey(player.getSeat())) {
			// return;
			// }

    		DaPaiTingPaiRes.Builder tingInfo = DaPaiTingPaiRes.newBuilder();
			List<PenghzCard> cards = new ArrayList<>(player.getHandPhzs());

			for (PenghzCard card :player.getHandPhzs()) {
				cards.remove(card);
				List<PenghzCard> huCards = PenghuziTool.getTingZps(cards,player);
				cards.add(card);
				if (huCards == null || huCards.size() == 0) {
					continue;
				}
				
				DaPaiTingPaiInfo.Builder ting = DaPaiTingPaiInfo.newBuilder();
				ting.setMajiangId(card.getId());
				for (PenghzCard mj : huCards) {
					ting.addTingMajiangIds(mj.getId());
				}
				tingInfo.addInfo(ting.build());
			}
			if (tingInfo.getInfoCount() > 0) {
				player.writeSocket(tingInfo.build());
			}
		} else {
			List<PenghzCard> cards = new ArrayList<>(player.getHandPhzs());
			List<PenghzCard> huCards = PenghuziTool.getTingZps(cards,player);

			if (huCards == null || huCards.size() == 0) {
				return;
			}
			TingPaiRes.Builder ting = TingPaiRes.newBuilder();
			for (PenghzCard zp : huCards) {
				ting.addMajiangIds(zp.getId());
			}
			player.writeSocket(ting.build());

		}
    }

    
    
    
	public void playDaNiao(PenghuziPlayer player, int paoFen) {
		player.setDaNiaoPoint(paoFen <= 0 ? 0 : paoFen);
		ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_piao_PENGHU_DANAIO, player.getSeat(), player.getDaNiaoPoint());
		
		for (Player tableplayer : getSeatMap().values()) {// 推送客户端玩家抛分情况
			tableplayer.writeSocket(com.build());
		}
		checkDeal();
		startNext();
	}


    @Override
    public Class<? extends Player> getPlayerClass() {
        return PenghuziPlayer.class;
    }

    public PenghzCard getNextCard(int val) {
        if (this.leftCards.size() > 0) {
            Iterator<PenghzCard> iterator = this.leftCards.iterator();
            PenghzCard find = null;
            while (iterator.hasNext()) {
                PenghzCard paohzCard = iterator.next();
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

    public PenghzCard getNextCard() {
        if (this.leftCards.size() > 0) {
            PenghzCard card = this.leftCards.remove(0);
            dbParamMap.put("leftPais", JSON_TAG);
            return card;
        }
        return null;
    }

    public List<PenghzCard> getLeftCards() {
        return leftCards;
    }

    public void setLeftCards(List<PenghzCard> leftCards) {
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
        changeExtend();
    }

    public int getMoSeat() {
        return moSeat;
    }

    public void setMoSeat(int lastMoSeat) {
        this.moSeat = lastMoSeat;
        changeExtend();
    }

    public List<PenghzCard> getNowDisCardIds() {
        return nowDisCardIds;
    }

    public void setNowDisCardIds(List<PenghzCard> nowDisCardIds) {
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
	public int getDisCardCout() {
		return disCardCout1;
	}

	public void setDisCardCout(int disCardCout) {
		this.disCardCout1 = disCardCout;
		if(disCardCout<3){
			changeExtend();
		}
	}

    public void markMoSeat(PenghzCard card, int seat) {
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

    // public boolean checkMo

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

    public PenghzCard getZaiCard() {
        return zaiCard;
    }

    public void setZaiCard(PenghzCard zaiCard) {
        this.zaiCard = zaiCard;
        changeExtend();
    }

    public int getSendPaoSeat() {
        return sendPaoSeat;
    }

    public void setSendPaoSeat(int sendPaoSeat) {
        if (this.sendPaoSeat != sendPaoSeat) {
            this.sendPaoSeat = sendPaoSeat;
            changeExtend();
        }

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

    /**
	 * 对应的座位cardId-seat
	 */
    public KeyValuePair<Integer, Integer> getMoSeatPair() {
        return moSeatPair;
    }

    public PenghzCard getBeRemoveCard() {
        return beRemoveCard;
    }

    /**
	 * 桌子上移除的牌
	 */
    public void setBeRemoveCard(PenghzCard beRemoveCard) {
        this.beRemoveCard = beRemoveCard;
        changeExtend();
    }

    /**
	 * 是否是该玩家摸的牌
	 */
    public boolean isMoByPlayer(PenghuziPlayer player) {
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
        changeExtend();
    }



    @Override
    public int getDissPlayerAgreeCount() {
        return getPlayerCount();
    }

    @Override
    public void createTable(Player player, int play, int bureauCount, List<Integer> params, List<String> strParams,
                            Object... objects) throws Exception {
        createTable(player, play, bureauCount, params);
    }

    @Override
    public void createTable(Player player, int play, int bureauCount, Object... objects) throws Exception {
    }



	@Override
    public void calcDataStatistics2() {
		// 俱乐部房间 单大局大赢家、单大局大负豪、总小局数、单大局赢最多、单大局输最多 数据统计
        if(isGroupRoom()){
            String groupId=loadGroupId();
            int maxPoint=0;
            int minPoint=0;
            Long dataDate=Long.valueOf(new SimpleDateFormat("yyyyMMdd").format(new Date()));
            //Long dataDate, String dataCode, String userId, String gameType, String dataType, int dataValue

            calcDataStatistics3(groupId);

            for (PenghuziPlayer player:playerMap.values()){
				// 总小局数
                DataStatistics dataStatistics1=new DataStatistics(dataDate,"group"+groupId,String.valueOf(player.getUserId()),String.valueOf(playType),"xjsCount",playedBureau);
                DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics1,3);
                int finalPoint;
                    finalPoint = player.loadScore();
				// 总大局数
                DataStatistics dataStatistics5=new DataStatistics(dataDate,"group"+groupId,String.valueOf(player.getUserId()),String.valueOf(playType),"djsCount",1);
                DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics5,3);
				// 总积分
                DataStatistics dataStatistics6=new DataStatistics(dataDate,"group"+groupId,String.valueOf(player.getUserId()),String.valueOf(playType),"zjfCount",finalPoint);
                DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics6,3);

                if (finalPoint >0){
                    if (finalPoint >maxPoint){
                        maxPoint= finalPoint;
                    }
					// 单大局赢最多
                    DataStatistics dataStatistics2=new DataStatistics(dataDate,"group"+groupId,String.valueOf(player.getUserId()),String.valueOf(playType),"winMaxScore", finalPoint);
                    DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics2,4);
                }else if (finalPoint <0){
                    if (finalPoint <minPoint){
                        minPoint= finalPoint;
                    }
					// 单大局输最多
                    DataStatistics dataStatistics3=new DataStatistics(dataDate,"group"+groupId,String.valueOf(player.getUserId()),String.valueOf(playType),"loseMaxScore", finalPoint);
                    DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics3,5);
                }
            }

            for (PenghuziPlayer player:playerMap.values()){
                int finalPoint;
                    finalPoint = player.loadScore();
                if (maxPoint>0&&maxPoint== finalPoint){
					// 单大局大赢家
                    DataStatistics dataStatistics4=new DataStatistics(dataDate,"group"+groupId,String.valueOf(player.getUserId()),String.valueOf(playType),"dyjCount",1);
                    DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics4,1);
                }else if (minPoint<0&&minPoint== finalPoint){
					// 单大局大负豪
                    DataStatistics dataStatistics5=new DataStatistics(dataDate,"group"+groupId,String.valueOf(player.getUserId()),String.valueOf(playType),"dfhCount",1);
                    DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics5,2);
                }
            }
        }
    }

    public long saveUserGroupPlaylog() {
        if(!needSaveUserGroupPlayLog()){
            return 0;
        }
        UserGroupPlaylog userGroupLog = new UserGroupPlaylog();
        userGroupLog.setTableid(id);
        userGroupLog.setUserid(creatorId);
        userGroupLog.setCount(playBureau);
        String players = "";
        String score = "";
        String diFenScore = "";
        for (PenghuziPlayer player : seatMap.values()) {
            players += player.getUserId() + ",";
                score += player.getTotalPoint() + ",";
                diFenScore += player.getTotalPoint() + ",";
        }
        userGroupLog.setPlayers(players.length() > 0 ? players.substring(0, players.length() - 1) : "");
        userGroupLog.setScore(score.length() > 0 ? score.substring(0, score.length() - 1) : "");
        userGroupLog.setDiFenScore(diFenScore.length() > 0 ? diFenScore.substring(0, diFenScore.length() - 1) : "");
        userGroupLog.setDiFen("");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        userGroupLog.setCreattime(sdf.format(createTime));
        userGroupLog.setOvertime(sdf.format(new Date()));
        userGroupLog.setPlayercount(maxPlayerCount);
        userGroupLog.setGroupid(Long.parseLong(loadGroupId()));
        userGroupLog.setGamename(getGameName());
        userGroupLog.setTotalCount(totalBureau);
        return TableLogDao.getInstance().saveGroupPlayLog(userGroupLog);
    }

    @Override
    public String getGameName() {
        String res = "";
        if (playType == 250) {
			res = "碰胡子";
        }
        return res;
    }


    public int getAutoTimeOut() {
        return autoTimeOut;
    }

    public int getAutoTimeOut2() {
        return autoTimeOut2;
    }

    
    
    public int getQiangzhiHu() {
		return qiangzhiHu;
	}

	@Override
    public boolean isCreditTable(List<Integer> params){
        return params != null && params.size() > 15 && StringUtil.getIntValue(params, 15, 0) == 1;
    }

    public static final List<Integer> wanfaList = Arrays.asList(GameUtil.play_type_penghuzi);

    public static void loadWanfaTables(Class<? extends BaseTable> cls){
        for (Integer integer:wanfaList){
            TableManager.wanfaTableTypesPut(integer,cls);
        }
    }

    @Override
    public int getLogGroupTableBureau() {
            return super.getLogGroupTableBureau();
        
    }

    private Map<Integer, TempAction> loadTempActionMap(String json) {
        Map<Integer, TempAction> map = new ConcurrentHashMap<>();
        if (json == null || json.isEmpty())
            return map;
        JSONArray jsonArray = JSONArray.parseArray(json);
        for (Object val : jsonArray) {
            String str = val.toString();
            TempAction tempAction = new TempAction();
            tempAction.initData(str);
            map.put(tempAction.getSeat(), tempAction);
        }
        return map;
    }
    


    public void setDataForPlayLogTable(PlayLogTable logTable) {
        StringJoiner players = new StringJoiner(",");
        StringJoiner scores = new StringJoiner(",");
        for (int seat = 1, length = getSeatMap().size(); seat <= length; seat++) {
            PenghuziPlayer player = seatMap.get(seat);
            players.add(String.valueOf(player.getUserId()));
                scores.add(String.valueOf(player.getTotalPoint()));
        }
        logTable.setPlayers(players.toString());
        logTable.setScores(scores.toString());
    }
    public void logGameAcMsg(PenghuziPlayer player,int fangPao,int index,String str){
    	StringBuilder sb = new StringBuilder("PengHz");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(str);
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append(fangPao);
        sb.append("|").append(index);
        sb.append("|").append(player.getLianZhuangC());
        LogUtil.msgLog.info(sb.toString());
    }

    
	@Override
	public boolean canQuit(Player player) {
		if (super.canQuit(player)) {
			return getTableStatus() != PenghuziConstant.TABLE_STATUS_DANIAO;
		}
		return false;
	}
	
    
    public int getTableStatus() {
		return tableStatus;
	}

	public void setTableStatus(int tableStatus) {
		this.tableStatus = tableStatus;
	}
	
	public int getDaNiaoWF() {
		return daNiaoWF;
	}

	public void setDaNiaoWF(int daNiaoWF) {
		this.daNiaoWF = daNiaoWF;
	}
	
	public int getDaNiaoVal() {
		return daNiaoVal;
	}

	public void setDaNiaoVal(int daNiaoVal) {
		this.daNiaoVal = daNiaoVal;
	}

	public String getTableMsg() {
        Map<String, Object> json = new HashMap<>();
			json.put("wanFa", "碰胡子");
        if (isGroupRoom()) {
            json.put("roomName", getRoomName());
        }
        json.put("playerCount", getPlayerCount());
            json.put("count", getTotalBureau());
        if (this.autoPlay) {
            json.put("autoTime", autoTimeOut / 1000);
            if (autoPlayGlob == 1) {
				json.put("autoName", "单局");
            } else {
				json.put("autoName", "整局");
            }
        }
        return JSON.toJSONString(json);
    }

    
    @Override
    public String getTableMsgForXianLiao() {
        StringBuilder sb = new StringBuilder();
		sb.append("【").append(getId()).append("】").append(finishBureau).append("/").append(totalBureau).append("局")
				.append("\n");
		sb.append("————————————————").append("\n");
		sb.append("【").append(getRoomName()).append("】").append("\n");
		sb.append("【").append(getGameName()).append("】").append("\n");
		sb.append("【").append(TimeUtil.formatTime(new Date())).append("】").append("\n");
        int maxPoint = -999999999;
        List<PenghuziPlayer> players = new ArrayList<>();
        for (PenghuziPlayer player : seatMap.values()) {
            int point = player.loadScore();
            if (point > maxPoint) {
                maxPoint = point;
            }
            players.add(player);
        }
        Collections.sort(players, new Comparator<PenghuziPlayer>() {
            @Override
            public int compare(PenghuziPlayer o1, PenghuziPlayer o2) {
                    return o2.loadScore() - o1.loadScore();
            }
        });
        for (PenghuziPlayer player : players) {
			sb.append("————————————————").append("\n");
            int point = player.loadScore();
			sb.append(StringUtil.cutHanZi(player.getName(), 5)).append("【").append(player.getUserId()).append("】")
					.append(point == maxPoint ? "，大赢家" : "").append("\n");
            sb.append(point > 0 ? "+" : point == 0 ? "" : "-").append(Math.abs(point)).append("\n");
        }
        return sb.toString();
    }

}
