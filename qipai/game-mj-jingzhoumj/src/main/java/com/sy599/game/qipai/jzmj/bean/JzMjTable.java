package com.sy599.game.qipai.jzmj.bean;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.sy599.game.GameServerConfig;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.FirstmythConstants;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.common.constant.SharedConstants.player_state;
import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.db.bean.TableInf;
import com.sy599.game.db.bean.UserPlaylog;
import com.sy599.game.db.dao.TableDao;
import com.sy599.game.db.dao.TableLogDao;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.manager.TableManager;
import com.sy599.game.msg.serverPacket.ComMsg.ComRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.*;
import com.sy599.game.msg.serverPacket.TableMjResMsg.ClosingMjInfoRes;
import com.sy599.game.msg.serverPacket.TableMjResMsg.ClosingMjPlayerInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.CreateTableRes;
import com.sy599.game.msg.serverPacket.TableRes.DealInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes;
import com.sy599.game.qipai.jzmj.constant.JzMjAction;
import com.sy599.game.qipai.jzmj.constant.JzMjConstants;
import com.sy599.game.qipai.jzmj.rule.JzMj;
import com.sy599.game.qipai.jzmj.rule.JzMjHelper;
import com.sy599.game.qipai.jzmj.rule.JzMjRobotAI;
import com.sy599.game.qipai.jzmj.rule.JzMjRule;
import com.sy599.game.qipai.jzmj.tool.JzMjQipaiTool;
import com.sy599.game.qipai.jzmj.tool.JzMjResTool;
import com.sy599.game.qipai.jzmj.tool.JzMjTool;
import com.sy599.game.udplog.UdpLogger;
import com.sy599.game.util.*;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author liuping 靖州麻将牌桌信息
 */
public class JzMjTable extends BaseTable {
	/**
	 * 当前桌上打出的牌
	 */
	private List<JzMj> nowDisCardIds = new ArrayList<>();
	/**
	 * 所有玩家当前可作的操作
	 */
	private Map<Integer, List<Integer>>

			actionSeatMap = new ConcurrentHashMap<>();
	/**
	 * 0胡 1碰 2明刚 3暗杠(暗杠后来不需要了 暗杠也用3标记)4吃 5补张(6缺一色 7板板胡 8大四喜 9六六顺 10节节高 11三同
	 * 12一枝花 13中途四喜 14中途六六顺)
	 */
	private Map<Integer, Map<Integer, List<Integer>>> gangSeatMap = new ConcurrentHashMap<>();
	/**
	 * 房间最大玩家人数上限
	 */
	private int maxPlayerCount = 4;
	/**
	 * 当前剩下的牌（庄上的牌）
	 */
	private List<JzMj> leftMajiangs = new ArrayList<>();
	/**
	 * 当前房间所有玩家信息map
	 */
	private Map<Long, JzMjPlayer> playerMap = new ConcurrentHashMap<Long, JzMjPlayer>();
	/**
	 * 座位对应的玩家信息MAP
	 */
	private Map<Integer, JzMjPlayer> seatMap = new ConcurrentHashMap<Integer, JzMjPlayer>();
	/**
	 * 胡确认信息
	 */
	private Map<Integer, Integer> huConfirmMap = new HashMap<>();
	/**
	 * 玩家位置对应临时操作 当同时存在多个可做的操作时 1当优先级最高的玩家最先操作 则清理所有操作提示 并执行该操作
	 * 2当操作优先级低的玩家先选择操作，则玩家先将所做操作存入临时操作中 当玩家优先级最高的玩家操作后 在判断临时操作是否可执行并执行
	 */
	private Map<Integer, JzMjTempAction> tempActionMap = new ConcurrentHashMap<>();
	/**
	 * 抓鸟
	 */
	private int birdNum;


	/**
	 * 计算庄闲
	 */
	private int isCalcBanker;
	/**
	 * 计算鸟的算法 1：中鸟翻倍 2：中鸟加倍X2
	 */
	private int calcBird;

	/**
	 * 中鸟加分选项 calcBird =1 ；
	 * 0=加胡牌分 {1,2,3,4}=加op_hitBirdPoint分
	 */
	private int op_hitBirdPoint;

	/**
	 * 摸麻将的seat
	 */
	private int moMajiangSeat;
	/**
	 * 摸杠的麻将
	 */
	private JzMj moGang;
	/**
	 * 杠出来的麻将
	 */
	private JzMj gangMajiang;
	/**
	 * 摸杠胡
	 */
	private List<Integer> moGangHuList = new ArrayList<>();
	/**
	 * 杠后出的两张牌
	 */
	private List<JzMj> gangDisMajiangs = new ArrayList<>();
	/**
	 * 摸海底捞的座位
	 */
	private int moLastMajiangSeat;
	/**
	 * 询问海底捞的座位
	 */
	private int askLastMajaingSeat;
	/**
	 * 第一次出现海底的座位
	 */
	private int fristLastMajiangSeat;

	/**
	 * 摸海底的座位号
	 */
	private List<Integer> moLastSeats = new ArrayList<>();
	/**
	 * 最后一张麻将
	 */
	private JzMj lastMajiang;
	/**
	 *
	 */
	private int disEventAction;
	private int isXianShangZhuang =0;
	private int lastZhuang =0;
	/*** GPS预警 */
	private int gpsWarn = 0;
	/*** 缺一色 */
	 private int queYiSe = 1;
	/*** 板板胡 */
	 private int banbanHu = 1;

	 private int OnlyDaHu =0;
	/*** 一枝花 */
	//private int yiZhiHua = 0;
	/*** 六六顺 */
	// int liuliuShun = 0;
	/*** 大四喜 */
	//private int daSiXi = 0;
	/*** 金童玉女 */
	//private int jinTongYuNu = 0;
//	private int jieJieGao = 0;
	/*** 三同 */
	//private int sanTong = 0;
	/*** 中途六六顺 */
//	private int zhongTuLiuLiuShun = 0;
	/*** 中途四喜 */
	// int zhongTuSiXi = 0;

	/*** 需要展示牌的玩家座位号 */
	private List<Integer> showMjSeat = new ArrayList<>();

	private int tableStatus;// 特殊状态 1飘分

	private int gangSaveSeat =0;

	/*** 杠打色子 **/
	private int gangDice = -1;

	/*** 摸屁股的座标号 */
	private List<Integer> moTailPai = new ArrayList<>();

	/** 杠后摸的两张牌中被要走的 **/
	private JzMj gangActedMj = null;

	/** 是否是开局 **/
	private boolean isBegin = false;

	private int randomBanker;

	private int dealDice;

	// 是否加倍：0否，1是
	private int jiaBei;
	// 加倍分数：低于xx分进行加倍
	private int jiaBeiFen;
	// 加倍倍数：翻几倍
	private int jiaBeiShu;

//	// 三人：空鸟 1：四八空鸟 2:鸟不落空
//	private int kongBird;
//	// （二人选）1：不能吃
 	private int buChi;
//	// （二人选）只能大胡
//	private int OnlyDaHu;
//	// （二人选）小胡自摸
//	private int xiaohuZiMo;
//	// （二人选）缺一门
//	private int queYiMen;

	private int jiajianghu;

	/** 托管1：单局，2：全局 */
	private int autoPlayGlob;
	private int autoTableCount;
	private int isAutoPlay;// 托管时间
	private int readyTime = 0;

	private int menqing;// 门清
	private int topFen;// 分数限制

	private int gangMoSi;// 杠摸4张

	private int qiShouNiaoFen;// 起手鸟分1:不算鸟分
	//低于below加分
	private int belowAdd=0;
	private int below=0;


	private int gangBuF;// 杠补算分
	private int quanqiurJiang;// 全求人吊将

	private int difen;// 底分

	private int fengding;

	private int choupai;
	//private int menQingZM;// 门清


	//private int jsXiaoHuF;// 解散小胡算分

	private int xiaoHuAuto;// 小胡自动胡

	//private int xiaoHuGdF;//小胡固定分

	private int jjHKqFg;//假将胡可抢放杠

	private int firstMomj ;

	// 出完听牌。此值为1 玩家听，过时在此状态。展示出牌nowdiscards
	private int table_tingpai=0;
	@Override
	protected boolean quitPlayer1(Player player) {
		return false;
	}

	@Override
	public boolean canQuit(Player player) {
		if (super.canQuit(player)) {
			return getTableStatus() != JzMjConstants.TABLE_STATUS_PIAO;
		}
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
		List<Integer> winSeatList = new ArrayList<>(huConfirmMap.keySet());
		boolean selfMo = false;
		int[] birdMjIds = null;
		int[] seatBirds = null;
		Map<Integer, Integer> seatBirdMap = new HashMap<>();
		boolean flow = false;

		int startseat = 0;
		int catchBirdSeat = 0;


		// 扎鸟
		boolean zhuaNiao = true;
		if (winSeatList.size() == 0) {
			// 流局
			flow = true;
			zhuaNiao = false;
			for (int otherSeat : seatMap.keySet()) {
				if (seatMap.get(otherSeat).getHuXiaohu().size() > 0) {
					zhuaNiao = true;
					break;
				}
			}
		}

		if (zhuaNiao) {
			// 海底
			if (leftMajiangs.size() == 0) {
				birdMjIds = zhuaNiao(lastMajiang);
			} else {
				// 先砸鸟
				birdMjIds = zhuaNiao(null);
			}

			//只有159中鸟
			if(winSeatList.size()>1){
				//一炮多响放炮人中
				seatBirds = birdToSeat(birdMjIds, disCardSeat);
			}else if(winSeatList.size()==1){
				seatBirds = birdToSeat(birdMjIds, winSeatList.get(0));
			}

			for (int seat : seatMap.keySet()) {
				int birdNum = calcBirdNum(seatBirds, seat);
				seatBirdMap.put(seat, birdNum);
			}
		} else {
			for (int seat : seatMap.keySet()) {
				seatBirdMap.put(seat, 0);
			}
		}
		//额外 只有159中鸟
		//System.out.println("===============================鸟================zhuaNiao="+zhuaNiao);
		//System.out.println(seatBirdMap);

		// 算胡的
		int xszLostSeat =0;
		if (winSeatList.size() != 0) {
			// 先判断是自摸还是放炮
			JzMjPlayer winPlayer = null;
			if (winSeatList.size() == 1) {
				winPlayer = seatMap.get(winSeatList.get(0));
				if ((winPlayer.isAlreadyMoMajiang() || winPlayer.isGangshangHua())
						&& winSeatList.get(0) == moMajiangSeat) {
					selfMo = true;
				}
			}
			// 庄家

			if (selfMo) {
				//calZiMoPointJzMj(seatBirdMap, winPlayer, true, 1, true, selfMo);
				calZiMoPointJzMj(seatBirdMap, winPlayer,   1, true, selfMo);
				winPlayer.changeAction(7, 1);//自摸=7
				winPlayer.getMyExtend().setMjFengshen(FirstmythConstants.firstmyth_index8, 1);
			} else {
				JzMjPlayer losePlayer = seatMap.get(disCardSeat);
				int loserSeat = losePlayer.getSeat();
				xszLostSeat = loserSeat;
				losePlayer.getMyExtend().setMjFengshen(FirstmythConstants.firstmyth_index10, winSeatList.size());
				int totalLosePoint = 0;
				for (int winSeat : winSeatList) {
					// 胡牌
					winPlayer = seatMap.get(winSeat);
					int daHuCount = winPlayer.getDahuPointCount();
					int bs =1;
					 if(daHuCount>1){
						bs =(int)Math.pow(2,daHuCount-1);
					}

					// 底分
					int winPoint = 0;
					if (daHuCount > 0) {
						//大胡接炮
//						闲家接闲家：6分
//						庄家接闲家或闲家接庄家：12分
//						闲家接闲上庄或闲上庄接闲家：18分
						//System.out.println("========================================大胡接炮");
						 	winPlayer.setXjsHuType(JzMjConstants.xjsHuType_DaHuJiePao);
						if(losePlayer.getSeat()!= lastWinSeat && winSeat !=lastWinSeat){
							//System.out.println("==============闲家接闲家：6分");
							winPoint =6;
						}else if(losePlayer.getSeat()==lastWinSeat && winSeat!=lastWinSeat && isXianShangZhuang ==0){
							//System.out.println("==============庄家接闲家或闲家接庄家：12分");
							winPoint =12;
						}else if(lastWinSeat ==winSeat && losePlayer.getSeat()!= lastWinSeat && isXianShangZhuang ==0){
							//System.out.println("==============庄家接闲家或闲家接庄家：12分");
							winPoint =12;
						}else if(losePlayer.getSeat()==lastWinSeat && winSeat!=lastWinSeat && isXianShangZhuang ==1){
							//System.out.println("==============闲家接闲上庄或闲上庄接闲家：18分");
							winPoint =18;
						}else if(lastWinSeat ==winSeat && losePlayer.getSeat()!= lastWinSeat&& isXianShangZhuang ==1){
							//System.out.println("==============闲家接闲上庄或闲上庄接闲家：18分");
							winPoint =18;
						}

						//System.out.println("==============bs: "+bs+"  win="+winPlayer.getName()+" isBaoting="+winPlayer.isBaoting());
						winPoint =  winPoint * bs;
						losePlayer.changeActionTotal(JzMjAction.ACTION_COUNT_DAHU_DIANPAO, 1);
						winPlayer.changeActionTotal(JzMjAction.ACTION_COUNT_DAHU_JIEPAO, 1);
					} else {
						//小胡接炮
						//System.out.println("========================================小胡接炮");
						winPlayer.setXjsHuType(JzMjConstants.xjsHuType_XiaoHuJiePao);
						winPoint = 0;
						if(losePlayer.getSeat()!= lastWinSeat && winSeat !=lastWinSeat){
							//System.out.println("==============闲家接闲家：1分");
							winPoint =1;
						}else if(losePlayer.getSeat()==lastWinSeat && winSeat!=lastWinSeat && isXianShangZhuang ==0){
							//System.out.println("==============庄家接闲家或闲家接庄家：4分");
							winPoint =4;
						}else if(lastWinSeat ==winSeat && losePlayer.getSeat()!= lastWinSeat && isXianShangZhuang ==0){
							winPoint =4;
							//System.out.println("==============庄家接闲家或闲家接庄家：4分");
						}else if(losePlayer.getSeat()==lastWinSeat && winSeat!=lastWinSeat && isXianShangZhuang ==1){
							winPoint =6;
							//System.out.println("==============闲家接闲上庄或闲上庄接闲家：6分");
						}else if(lastWinSeat ==winSeat && losePlayer.getSeat()!= lastWinSeat&& isXianShangZhuang ==1){
							winPoint =6;
							//System.out.println("==============闲家接闲上庄或闲上庄接闲家：6分");
						}
						losePlayer.changeActionTotal(JzMjAction.ACTION_COUNT_XIAOHU_DIANPAO, 1);
						winPlayer.changeActionTotal(JzMjAction.ACTION_COUNT_XIAOHU_JIEPAO, 1);
					}
//					if(losePlayer.isBaoting()){
//						winPoint*=2;
//						//System.out.println(losePlayer.getName()+" 报听输X2 ");
//					}
//					// 庄闲分
//					if ((winPlayer.getSeat() == lastWinSeat || loserSeat == lastWinSeat)) {
//						winPoint = calcBankerPoint(winPoint, daHuCount);
//					}
					//System.out.println("daHuCount="+daHuCount+"  "+losePlayer.getName()+"=baoting="+losePlayer.isBaoting()+ "   winPoint=" +winPoint);
					int totalBirdNum = seatBirdMap.get(winSeat) + seatBirdMap.get(loserSeat);

					winPoint = calcBirdPoint(winPoint, totalBirdNum, true);


					totalLosePoint += winPoint;
					winPlayer.changeAction(6, 1);//6=胡
					losePlayer.changeAction(0, 1);//0=点炮
					winPlayer.setLostPoint(winPoint);
				}
				losePlayer.setLostPoint(-totalLosePoint);
			}

		}
		//流局杠分不算
		if(flow){
			for(JzMjPlayer p:seatMap.values()){
				p.setGangPoint(0);
			}
		}
		// 小胡计算aaa
		 calXiaoHuFen(winSeatList, selfMo, seatBirdMap);
		//封顶计算

		for (JzMjPlayer seat : seatMap.values()) {
			//System.out.println(seat.getName()+" 输="+seat.getLostPoint()+" 杠"+seat.getGangPoint());
			seat.setPoint(seat.getLostPoint()+seat.getGangPoint());
			 if (topFen > 0 ) {
				if (Math.abs(seat.getPoint()) > topFen) {
					seat.setPoint(seat.getPoint() > 0 ? topFen : -topFen);
				}
			}
			 seat.changeTotalPoint(seat.getPoint());

		}
		boolean over = playBureau == totalBureau;

		if (autoPlayGlob > 0) {
			// //是否解散
			boolean diss = false;
			if (autoPlayGlob == 1) {
				for (JzMjPlayer seat : seatMap.values()) {
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

		// 不管流局都加分
		ClosingMjInfoRes.Builder res = sendAccountsMsg(over, selfMo, winSeatList, birdMjIds, seatBirds, seatBirdMap,
				false, startseat, catchBirdSeat);
		// 没有流局

//		if (!flow) {
//			//有一炮多响，离点炮近的玩家优先胡牌
//			if (winSeatList.size() > 1) {
//				int winseat =0;
//				int nseat = getNextSeat(disCardSeat);
//				for(int seat : winSeatList){
//					if(nseat== seat){
//						winseat =seat;
//						break;
//					}else{
//						nseat = getNextSeat(nseat);
//					}
//				}
//				//System.out.println(disCardSeat+"=放炮位置=========================有一炮多响，离点炮近的玩家优先胡牌 原响炮位置:"+winSeatList);
//				winSeatList.clear();
//				winSeatList.add(winseat);
//				//System.out.println(disCardSeat+"=放炮位置=========================有一炮多响，离点炮近的玩家优先胡牌:"+winSeatList);
//			}
//		}
		if (!flow) {
			for(JzMjPlayer p :seatMap.values()){
				p.setIsxsz(0);
			}
			if(maxPlayerCount>2 && winSeatList.size()==1){
				int wins = winSeatList.get(0);
				if((xszLostSeat!=lastWinSeat && wins!=lastWinSeat && !selfMo )|| (lastWinSeat == wins && isXianShangZhuang ==1)){
					//System.out.println("产生闲上庄 庄家为"+seatMap.get(wins).getName()+" seat ="+wins);
					setIsXianShangZhuang(1);
					seatMap.get(wins).setIsxsz(1);
				}else{
					setIsXianShangZhuang(0);
				}
			} else{
				setIsXianShangZhuang(0);
			}
			if (winSeatList.size() > 1) {
				// 1、通炮下一把的庄是给放炮人的下家
				//2、通炮鸟就是看放炮人的鸟
				setLastWinSeat(getNextSeat(disCardSeat));
			} else {
				setLastWinSeat(winSeatList.get(0));
			}

		} else {
			//流局连庄
			setLastWinSeat(lastWinSeat);
//			for(JzMjPlayer p :seatMap.values()){
//				p.setIsxsz(0);
//			}
//			setIsXianShangZhuang(0);
//			if (moLastMajiangSeat != 0) {
//				// 流局有人要了海底，要的人当庄
//				setLastWinSeat(moLastMajiangSeat);
//
//			} else if (fristLastMajiangSeat != 0) {
//				// 流局了没有人要海底，第一个可以选择的人当庄
//				setLastWinSeat(fristLastMajiangSeat);
//
//			}

		}

		saveLog(over, 0l, res.build());

		calcAfter();
		if (over) {
			calcOver1();
			calcOver2();
			calcOver3();
			diss();
		} else {
			initNext();
			calcOver1();
		}

		for (JzMjPlayer player : seatMap.values()) {
			if (player.isAutoPlaySelf()) {
				player.setAutoPlay(false, false);
			}
		}

		for (Player player : seatMap.values()) {
			player.saveBaseInfo();
		}
	}

	public void calcOverQiShouHu( List<Integer> winSeatList) {
		// List<Integer> winSeatList = new ArrayList<>(huConfirmMap.keySet());
		boolean selfMo = false;
		int[] birdMjIds = null;
		int[] seatBirds = null;
		 JzMjPlayer	winPlayer =seatMap.get(winSeatList.get(0));
		if(null==winPlayer){
			return;
		}
		Map<Integer, Integer> seatBirdMap = new HashMap<>();
		boolean flow = false;

		int startseat = 0;
		int catchBirdSeat = 0;


		// 扎鸟
		boolean zhuaNiao = true;
		if (winSeatList.size() == 0) {
			// 流局
				flow = true;
				zhuaNiao = false;
				for (int otherSeat : seatMap.keySet()) {
					if (seatMap.get(otherSeat).getHuXiaohu().size() > 0) {
						zhuaNiao = true;
						break;
					}
				}
			}
			int bird159=0;
			if (zhuaNiao) {
				// 海底
				if (leftMajiangs.size() == 0) {
					birdMjIds = zhuaNiao(lastMajiang);
				} else {
					// 先砸鸟
					birdMjIds = zhuaNiao(null);
				}


				//seatBirds = birdToSeat(birdMjIds, lastWinSeat);
				seatBirds = birdToSeat(birdMjIds, winPlayer.getSeat());
				for (int seat : seatMap.keySet()) {
					int birdNum = calcBirdNum(seatBirds, seat);
					seatBirdMap.put(seat, birdNum);
				}
			} else {
				for (int seat : seatMap.keySet()) {
					seatBirdMap.put(seat, 0);
				}
			}
			 //System.out.println("===============================鸟================zhuaNiao="+zhuaNiao);
			 //System.out.println(seatBirdMap);

			 int winTotalPoint=0;
		for(int winseats: winSeatList){
			//System.out.println("=======winseats="+winseats);
			int winBirdNum = getBirdBySeat(seatBirdMap,winseats);
			for (JzMjPlayer losep :seatMap.values()){
				if(winseats == losep.getSeat()){
					continue;
				}

				int totalBirdNum = winBirdNum + getBirdBySeat(seatBirdMap, losep.getSeat());
				// 输家鸟分
				int losePoint = Math.abs(losep.getLostPoint());
				if(birdNum>0) {
					losePoint = calcBirdPoint(losePoint, totalBirdNum, true);
				}
				//System.out.println("=======lose="+losep.getName()+" totalBirdNum"+totalBirdNum+" losePoint:"+totalBirdNum);
				losep.setLostPoint(-losePoint);
				winTotalPoint +=losePoint;
			}
			winPlayer.setLostPoint(winTotalPoint);
		}

		//
		if(winPlayer.getSeat()==lastWinSeat && isXianShangZhuang==1){
			for(JzMjPlayer p:seatMap.values()){
				p.setGangPoint(0);
			}
			setIsXianShangZhuang(1);
		}else{
			for(JzMjPlayer p:seatMap.values()){
				p.setGangPoint(0);
				p.setIsxsz(0);
			}
			setIsXianShangZhuang(0);
		}

		//封顶计算
		for (JzMjPlayer seat : seatMap.values()) {
			//System.out.println(seat.getName()+" 输="+seat.getLostPoint()+" 杠"+seat.getGangPoint());
			seat.setPoint(seat.getLostPoint()+seat.getGangPoint());
			if (topFen > 0 ) {
				if (Math.abs(seat.getPoint()) > topFen) {
					seat.setPoint(seat.getPoint() > 0 ? topFen : -topFen);
				}
			}
			seat.changeTotalPoint(seat.getPoint());
		}
//		for (JzMjPlayer seat : seatMap.values()) {
//			if (topFen > 0 ) {
//				if (Math.abs(seat.getLostPoint()) > topFen) {
//					seat.setLostPoint(seat.getLostPoint() > 0 ? topFen : -topFen);
//				}
//			}
//			//System.out.println(seat.getName()+" =lostpoint="+seat.getLostPoint()+"  ==========杠分="+seat.getGangPoint());
//			seat.changePoint(seat.getLostPoint()+seat.getGangPoint());
//		}
//

		boolean over = playBureau == totalBureau;

		if (autoPlayGlob > 0) {
			// //是否解散
			boolean diss = false;
			if (autoPlayGlob == 1) {
				for (JzMjPlayer seat : seatMap.values()) {
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

		// 不管流局都加分
		ClosingMjInfoRes.Builder res = sendAccountsMsg(over, selfMo, winSeatList, birdMjIds, seatBirds, seatBirdMap,
				false, startseat, catchBirdSeat);
		// 没有流局
		if (!flow) {
			//有一炮多响，离点炮近的玩家优先胡牌
			if (winSeatList.size() > 1) {
				int winseat =0;
				int nseat = getNextSeat(disCardSeat);
				for(int seat : winSeatList){
					if(nseat== seat){
						winseat =seat;
						break;
					}else{
						nseat = getNextSeat(nseat);
					}
				}
				//System.out.println(disCardSeat+"=放炮位置=========================有一炮多响，离点炮近的玩家优先胡牌 原响炮位置:"+winSeatList);
				winSeatList.clear();
				winSeatList.add(winseat);
				//System.out.println(disCardSeat+"=放炮位置=========================有一炮多响，离点炮近的玩家优先胡牌:"+winSeatList);
			}
		}
		if (!flow) {
			if (winSeatList.size() > 1) {
				// 一炮多响设置放炮的人为庄家
				setLastWinSeat(disCardSeat);
			} else {
				setLastWinSeat(winSeatList.get(0));
			}

		} else {
			//流局连庄
			setLastWinSeat(lastWinSeat);
//			if (moLastMajiangSeat != 0) {
//				// 流局有人要了海底，要的人当庄
//				setLastWinSeat(moLastMajiangSeat);
//
//			} else if (fristLastMajiangSeat != 0) {
//				// 流局了没有人要海底，第一个可以选择的人当庄
//				setLastWinSeat(fristLastMajiangSeat);
//
//			}

		}

		saveLog(over, 0l, res.build());

		calcAfter();
		if (over) {
			calcOver1();
			calcOver2();
			calcCreditNew();
			diss();
		} else {
			initNext();
			calcOver1();
		}

		for (JzMjPlayer player : seatMap.values()) {
			if (player.isAutoPlaySelf()) {
				player.setAutoPlay(false, false);
			}
		}

		for (Player player : seatMap.values()) {
			player.saveBaseInfo();
		}
	}

	private void calXiaoHuFen(List<Integer> winSeatList, boolean selfMo, Map<Integer, Integer> seatBirdMap) {
		for (JzMjPlayer winPlayer : seatMap.values()) {
			if (winPlayer.getHuXiaohu().size() == 0) {
				continue;
			}

			{
				boolean addBirdPoint = true;
				// 已经加过鸟分了就不再加了
				if (winSeatList.contains(winPlayer.getSeat())) {
					addBirdPoint = false;
				}

				if(qiShouNiaoFen ==1) {
					addBirdPoint = false;
				}
				calZiMoPointJzMj(seatBirdMap, winPlayer,  winPlayer.getHuXiaohu().size(), addBirdPoint, selfMo);
			}
		}

	}

	private boolean checkAuto3() {
		boolean diss = false;
		// if(autoPlayGlob==3) {
		boolean diss2 = false;
		for (JzMjPlayer seat : seatMap.values()) {
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

	private void calZiMoPointJzMj(Map<Integer, Integer> seatBirdMap, JzMjPlayer winPlayer,   int xiaohuNum,
							  boolean addBirdPoint, boolean zimo) {
		int daHuCount = 0;
		int bs =1;
		boolean   dahu = false;
		  daHuCount = winPlayer.getDahuPointCount();
		  if(daHuCount>0){
			  if(daHuCount>1){
				  bs =(int)Math.pow(2,daHuCount-1);
			  }
			  dahu = true;
		  }
		//System.out.println("=============daHuCount==="+daHuCount);
		int winSeat = winPlayer.getSeat();
		int winPoint = 0;
		// 底分
		if (daHuCount > 0) {
			winPlayer.changeActionTotal(JzMjAction.ACTION_COUNT_DAHU_ZIMO, 1);
		} else {
			winPlayer.changeActionTotal(JzMjAction.ACTION_COUNT_XIAOHU_ZIMO, 1);
		}

		int winBirdNum = getBirdBySeat(seatBirdMap, winSeat);
		int totalWinPoint = 0;
		for (int loserSeat : seatMap.keySet()) {
			// 除了赢家的其他人
			if (winSeat == loserSeat) {
				continue;
			}
			JzMjPlayer loser = seatMap.get(loserSeat);
			int losePoint = 0;
			if(dahu && zimo ){
				//System.out.println("=================大胡自摸==================");
				winPlayer.setXjsHuType(JzMjConstants.xjsHuType_DaHuZiMo);
				//.大胡自摸
//				  闲家自摸：庄家8分，闲家2分，闲上庄12分
				if(winPlayer.getSeat() != lastWinSeat){
					if(loser.getSeat()==lastWinSeat && isXianShangZhuang==1){
						losePoint =12;
						//System.out.println("============ 闲大胡=============闲上庄 扣 12分========");
					}else if(loser.getSeat()==lastWinSeat && isXianShangZhuang==0){
						losePoint = 8;
						//System.out.println("=============闲大胡  ============庄家扣每人8分========");
					}else{
						losePoint = 2;
						//System.out.println("============= 闲大胡============闲家扣：每人2分=======");
					}
				}else if(winPlayer.getSeat() == lastWinSeat && isXianShangZhuang == 0){
					//庄家自摸：每人8分
					losePoint = 8;
					//System.out.println("=========================庄家自摸：每人8分=======");
				}else if(winPlayer.getSeat() == lastWinSeat && isXianShangZhuang == 1){
					// 闲上庄自摸：每人12分
					losePoint = 12;

					//System.out.println("=========================闲上庄自摸：每人12分=======");
				}
				losePoint*=bs;
			}else if(!dahu && zimo){
				//小胡自摸
				//System.out.println("=================小胡自摸==================");
				winPlayer.setXjsHuType(JzMjConstants.xjsHuType_XiaoHuZiMo);
				//闲家自摸：庄家输4分、闲家输1分
				//庄家自摸：每家4分
				//如有闲上庄，则庄家在加2分
				if(winPlayer.getSeat() != lastWinSeat){
					if(loser.getSeat()==lastWinSeat && isXianShangZhuang==1){
						losePoint = 4+2; //System.out.println("==========闲 小胡===============闲上庄 扣6分=======");
					}else if(loser.getSeat()==lastWinSeat && isXianShangZhuang==0){
						losePoint = 4;//System.out.println("==========闲小胡=============== 庄扣 4分=======");
					}else{
						losePoint = 1;//System.out.println("==========闲 小胡===============闲 扣 1分=======");
					}
				}else if(winPlayer.getSeat() == lastWinSeat && isXianShangZhuang == 0){
					//庄家自摸：每人4分
					losePoint = 4;//System.out.println("==========小胡=============== 庄自摸 每人扣 4分=======");
				}else if(winPlayer.getSeat() == lastWinSeat && isXianShangZhuang == 1){
					// 闲上庄自摸：每人6分
					losePoint = 6;//System.out.println("==========小胡===============闲上庄 每人扣 6分=======");
				}
				losePoint*=bs;
			}

			if(winPlayer.isBaoting()){
				//System.out.println("赢家报听===================x2===== ");
				//System.out.println();
			}
			//losePoint *= xiaohuNum;
			int totalBirdNum = winBirdNum + getBirdBySeat(seatBirdMap, loserSeat);
			// 如果放炮的话，算小胡鸟，没放炮的人需要加鸟分
			if (!zimo && loser.getSeat() != disCardSeat) {
				addBirdPoint = true;
			}
			// 输家鸟分
			if(dahu||qiShouNiaoFen!=1) {
				losePoint = calcBirdPoint(losePoint, totalBirdNum, addBirdPoint);
			}

//			if (dahu&&losePoint > topFen&&topFen>0) {
//					losePoint = topFen;
//			}
			totalWinPoint += losePoint;
			loser.changeLostPoint(-losePoint);
		}
		winPlayer.changeLostPoint(totalWinPoint);
	}

	private void calZiMoPoint(Map<Integer, Integer> seatBirdMap, JzMjPlayer winPlayer, boolean dahu, int xiaohuNum,
                              boolean addBirdPoint, boolean zimo) {
		int daHuCount = 0;
		if (dahu) {
			daHuCount = winPlayer.getDahuPointCount();
		}
		int winSeat = winPlayer.getSeat();

		int winPoint = difen;
		// 底分
		if (daHuCount > 0) {
			winPoint = calcDaHuPoint(daHuCount);
			winPlayer.changeActionTotal(JzMjAction.ACTION_COUNT_DAHU_ZIMO, 1);
		} else {
			winPoint = difen;
			winPlayer.changeActionTotal(JzMjAction.ACTION_COUNT_XIAOHU_ZIMO, 1);
		}

		// 赢家庄闲分
		if (winPlayer.getSeat() == lastWinSeat) {
			winPoint = calcBankerPoint(winPoint, daHuCount);
		}
		//seatBirdMap.get(winSeat)
		int winBirdNum = getBirdBySeat(seatBirdMap, winSeat);
		int totalWinPoint = 0;
		for (int loserSeat : seatMap.keySet()) {
			// 除了赢家的其他人
			if (winSeat == loserSeat) {
				continue;
			}
			JzMjPlayer loser = seatMap.get(loserSeat);
			int losePoint = winPoint;
			// 输家庄闲分
			if (loser.getSeat() == lastWinSeat) {
				losePoint = calcBankerPoint(losePoint, daHuCount);
			}

			losePoint *= xiaohuNum;

			//seatBirdMap.get(loserSeat)
			int totalBirdNum = winBirdNum + getBirdBySeat(seatBirdMap, loserSeat);

			// 如果放炮的话，算小胡鸟，没放炮的人需要加鸟分
			if (!zimo && loser.getSeat() != disCardSeat) {
				addBirdPoint = true;
			}
			// 输家鸟分
			if(dahu||qiShouNiaoFen!=1) {
				losePoint = calcBirdPoint(losePoint, totalBirdNum, addBirdPoint);
			}
			// 飘分
			if (dahu) {
//				if (kePiao >= 1) {
//					losePoint += (loser.getPiaoPoint() + winPlayer.getPiaoPoint());
//				}
			}

//			if (dahu&&losePoint > topFen&&topFen>0) {
//					losePoint = topFen;
//			}
			totalWinPoint += losePoint;
			loser.changeLostPoint(-losePoint);
		}
		winPlayer.changeLostPoint(totalWinPoint);
	}




	private void calXiaoPoint2(JzMjPlayer winPlayer, int xiaohuNum) {
		int totalWinPoint = 0;
		for (int loserSeat : seatMap.keySet()) {
			// 除了赢家的其他人
			if (winPlayer.getSeat() == loserSeat) {
				continue;
			}
			JzMjPlayer loser = seatMap.get(loserSeat);
			int losePoint = 2*xiaohuNum;
			totalWinPoint += losePoint;
			loser.changeLostPoint(-losePoint);
		}
		winPlayer.changeLostPoint(totalWinPoint);
	}




	private int getBirdBySeat(Map<Integer, Integer> seatBirdMap,int seat){
		if(seatBirdMap ==null){
			return 0;
		}
		Integer bnum = seatBirdMap.get(seat);
		if(bnum==null){
			return 0;
		}
		return bnum;


	}


	/**
	 * 计算鸟分加成
	 *计算鸟的算法  1：中鸟相加结算 2：中鸟加倍
	 * 相加op_hitBirdPoint=0 加胡牌分
	 * op_hitBirdPoint>0 加op_hitBirdPoint分
	 * @param point
	 * @param bird
	 * @return
	 */
	private int calcBirdPoint(int point, int bird, boolean addBirdPoint) {
		if (bird <= 0) {
			return point;
		}
		if (calcBird == 1 && addBirdPoint) {
			// 加分最后结算
			//point = point + bird;
			//System.out.println("中鸟加算 point="+point+" bird="+bird+" op_hitBirdPoint="+op_hitBirdPoint);
			int hitBirdPoint =0;
		 	if(op_hitBirdPoint==0){
		 		hitBirdPoint = point;
			}else{
				hitBirdPoint = op_hitBirdPoint;
			}
			point =point+bird*hitBirdPoint;
			//System.out.println("最后point="+point);
		} else if (calcBird == 2) {
			// 翻倍是2的bird次方
			point = (int) (point * (Math.pow(2, bird)));
			//System.out.println("中鸟翻倍 point="+point+" bird="+bird+" ");
		}
		return point;
	}

	/**
	 * 计算庄闲加成
	 *
	 * @param point
	 * @return
	 */
	private int calcBankerPoint(int point, int dahuCount) {

		if (dahuCount == 0) {
			dahuCount = 1;
		}
		//③牌型叠加
		//最高叠加5个大胡。

		//point += dahuCount;
		double aa =Math.pow(2,dahuCount);
		int b =(int)aa;
		point =  point*b ;

		return point;
	}

	/**
	 * 计算大胡
	 *
	 * @return
	 */
	private int calcDaHuPoint(int daHuCount) {
		int point = 5+difen;
		point = point * daHuCount;

		return point;
	}

	/**
	 * 计算小胡分 正分代表赢分，负分代表输分
	 *
	 * @param seat
	 * @return
	 */
	private int calcXiaoHuPoint(int seat) {
		int lostXiaoHuCount = 0;
		JzMjPlayer player = seatMap.get(seat);
		for (int otherSeat : seatMap.keySet()) {
			if (otherSeat != seat) {
				lostXiaoHuCount += seatMap.get(otherSeat).getHuXiaohu().size();
			}

		}
		return player.getHuXiaohu().size() * 2 * (getMaxPlayerCount() - 1) - lostXiaoHuCount * 2;
	}

	public void saveLog(boolean over, long winId, Object resObject) {
		ClosingMjInfoRes res = (ClosingMjInfoRes) resObject;
		LogUtil.d_msg("tableId:" + id + " play:" + playBureau + " over:" + res);
		String logRes = JacksonUtil.writeValueAsString(LogUtil.buildMJClosingInfoResLog(res));
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
		userLog.setType(creditMode == 1 ? 2 : 1);
		userLog.setMaxPlayerCount(maxPlayerCount);
		userLog.setGeneralExt(buildGeneralExtForPlaylog().toString());
		long logId = TableLogDao.getInstance().save(userLog);
		saveTableRecord(logId, over, playBureau);
		for (JzMjPlayer player : playerMap.values()) {
			player.addRecord(logId, playBureau);
		}
		UdpLogger.getInstance().sendSnapshotLog(masterId, playLog, logRes);

		// LogUtil.d_msg("tableId:" + id + " play:" + playBureau + " over:" +
		// res);
		// String logRes =
		// JacksonUtil.writeValueAsString(LogUtil.buildClosingInfoResLog(res));
		// String logOtherRes =
		// JacksonUtil.writeValueAsString(LogUtil.buildClosingInfoResOtherLog(res));
		// Date now = TimeUtil.now();
		// UserPlaylog userLog = new UserPlaylog();
		// userLog.setLogId(playType);
		// userLog.setTableId(id);
		// userLog.setRes(logRes);
		// userLog.setTime(now);
		// userLog.setTotalCount(totalBureau);
		// userLog.setCount(playBureau);
		// userLog.setStartseat(lastWinSeat);
		// userLog.setOutCards(playLog);
		// userLog.setExtend(logOtherRes);
		// long logId = TableLogDao.getInstance().save(userLog);
		// for (JzMjPlayer player : playerMap.values()) {
		// player.addRecord(logId, playBureau);
		// }
		// UdpLogger.getInstance().sendSnapshotLog(masterId, playLog, logRes);
	}

	/**
	 * 中鸟个数
	 *
	 * @param seatBirdArr
	 * @param seat
	 * @return
	 */
	private int calcBirdNum(int[] seatBirdArr, int seat) {
		int birdNum = 0;
		for (int seatBird : seatBirdArr) {
			if (seat == seatBird) {
				birdNum++;
			}
		}
		return birdNum;
	}

	/**
	 * 抓鸟
	 *
	 * @return
	 */
	private int[] zhuaNiao(JzMj lastMaj) {
		// 先砸鸟
		int realBirdNum = leftMajiangs.size() > birdNum ? birdNum : leftMajiangs.size();

		if (realBirdNum == 0) {
			realBirdNum = birdNum;
		}
		int[] bird = new int[realBirdNum];
		for (int i = 0; i < realBirdNum; i++) {
			JzMj prickbirdMajiang = null;
			if (lastMaj != null) {
				prickbirdMajiang = lastMaj;
			} else {
				prickbirdMajiang = getLeftMajiang();
			}

			if (prickbirdMajiang != null) {
				bird[i] = prickbirdMajiang.getId();
			} else {
				break;
			}
		}
		// 算鸟砸中谁
		return bird;
	}

	/**
	 * 中鸟的麻将算出座位
	 *
	 *            rdMajiangIds
	 * @param bankerSeat
	 * @return
	 */
	private int[] birdToSeat(int[] prickBirdMajiangIds, int bankerSeat) {
		int[] seatArr = new int[prickBirdMajiangIds.length];
		for (int i = 0; i < prickBirdMajiangIds.length; i++) {
			JzMj majiang = JzMj.getMajang(prickBirdMajiangIds[i]);
			int prickbirdPai = majiang.getPai();

			int prickbirdseat = 0;
			if (maxPlayerCount == 4) {
//				prickbirdPai = (prickbirdPai - 1) % 4;// 从自己开始算 所以减1
//				prickbirdseat = prickbirdPai + bankerSeat > 4 ? prickbirdPai + bankerSeat - 4
//						: prickbirdPai + bankerSeat;
				if (prickbirdPai == 1 || prickbirdPai == 5 || prickbirdPai == 9) {
					prickbirdseat = bankerSeat;
				}
			} else if (maxPlayerCount == 3) {
				// 鸟不落空
//				if (kongBird == 2) {
//					prickbirdPai = (prickbirdPai - 1) % 3;// 从自己开始算 所以减1
//					prickbirdseat = prickbirdPai + bankerSeat > 3 ? prickbirdPai + bankerSeat - 3
//							: prickbirdPai + bankerSeat;
//				} else
					{
					// 4-8 空鸟
					if (prickbirdPai == 1 || prickbirdPai == 5 || prickbirdPai == 9) {
						prickbirdseat = bankerSeat;
					}
//					else if (prickbirdPai == 2 || prickbirdPai == 6) {
//						// 庄下家
//						prickbirdseat = (bankerSeat % 3) + 1;
//					} else if (prickbirdPai == 3 || prickbirdPai == 7) {
//						// 庄上家
//						prickbirdseat = ((bankerSeat % 3) + 1) % 3 + 1;
//					}
				}
			} else {
				if (prickbirdPai == 1 || prickbirdPai == 5 || prickbirdPai == 9) {
					prickbirdseat = bankerSeat;
				}
//				else if (prickbirdPai == 3 || prickbirdPai == 7) {
//					prickbirdseat = (bankerSeat % 2) + 1;
//				}

				// //两人 2468 空鸟
				// if(prickbirdPai%2==0) {
				// continue;
				// }prickbirdseat = (bankerSeat%3)+1;
				//
				// prickbirdseat = bankerSeat;

			}

			seatArr[i] = prickbirdseat;
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
				tempMap.put("nowDisCardIds", StringUtil.implode(JzMjHelper.toMajiangIds(nowDisCardIds), ","));
			}
			if (tempMap.containsKey("leftPais")) {
				tempMap.put("leftPais", StringUtil.implode(JzMjHelper.toMajiangIds(leftMajiangs), ","));
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

	// public String buildExtend() {
	// JsonWrapper wrapper = new JsonWrapper("");
	// for (JzMjPlayer player : seatMap.values()) {
	// wrapper.putString(player.getSeat(), player.toExtendStr());
	// }
	// wrapper.putString(5, DataMapUtil.explode(huConfirmMap));
	// wrapper.putInt(6, birdNum);
	// wrapper.putInt(7, moMajiangSeat);
	// if (moGang != null) {
	// wrapper.putInt(8, moGang.getId());
	//
	// } else {
	// wrapper.putInt(8, 0);
	//
	// }
	// wrapper.putString(9, StringUtil.implode(moGangHuList, ","));
	// wrapper.putString(10, MajiangHelper.implodeMajiang(gangDisMajiangs,
	// ","));
	// if (gangMajiang != null) {
	// wrapper.putInt(11, gangMajiang.getId());
	//
	// } else {
	// wrapper.putInt(11, 0);
	//
	// }
	// wrapper.putInt(12, askLastMajaingSeat);
	// wrapper.putInt(13, moLastMajiangSeat);
	// if (lastMajiang != null) {
	// wrapper.putInt(14, lastMajiang.getId());
	// } else {
	// wrapper.putInt(14, 0);
	// }
	// wrapper.putInt(15, fristLastMajiangSeat);
	// wrapper.putInt(16, disEventAction);
	// wrapper.putInt(17, isCalcBanker);
	// wrapper.putInt(18, calcBird);
	// return wrapper.toString();
	// }

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
		Random r = new Random();
		int dealDice = (r.nextInt(6) + 1) * 10 + (r.nextInt(6) + 1);
		addPlayLog(disCardRound + "_" + lastWinSeat + "_" + JzMjDisAction.action_dice + "_" + dealDice);
		setDealDice(dealDice);
		logFaPaiTable();

		for (JzMjPlayer tablePlayer : seatMap.values()) {
			DealInfoRes.Builder res = DealInfoRes.newBuilder();
			//System.out.println(tablePlayer.getName()+"  "+tablePlayer.getHandMajiang().size());
			List<Integer> actionList = tablePlayer.checkMo(null, true);
			//System.out.println("fapai===============actionList");
			//System.out.println(actionList);
			if (!actionList.isEmpty()) {
				addActionSeat(tablePlayer.getSeat(), actionList);
				res.addAllSelfAct(actionList);
			}
			res.addAllHandCardIds(tablePlayer.getHandPais());
			res.setNextSeat(getNextDisCardSeat());
			res.setGameType(getWanFa());
			res.setRemain(leftMajiangs.size());
			res.setBanker(lastWinSeat);
			res.setDealDice(dealDice);

			logFaPaiPlayer(tablePlayer, actionList);
			tablePlayer.writeSocket(res.build());

			sendTingInfo(tablePlayer);

			  if(tablePlayer.isAutoPlay()) {
	            	addPlayLog(getDisCardRound() + "_" +tablePlayer.getSeat() + "_" + JzMjConstants.action_tuoguan + "_" +1);
	           }

		}
		isBegin = true;

		if (!hasXiaoHu() && !hasBaoTing()) {
			// 没有操作的话通知庄家出牌
			JzMjPlayer bankPlayer = seatMap.get(lastWinSeat);
			ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.req_com_kt_ask_dismajiang);// 推送庄家出牌
			bankPlayer.writeSocket(com.build());
			// isBegin = false;
		}
	}

	/**
	 * 摸牌
	 *
	 * @param player
	 */
	public void moMajiang(JzMjPlayer player, boolean isBuzhang) {
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
		if (disCardRound != 0 && player.isAlreadyMoMajiang()) {
			return;
		}
		if (leftMajiangs.size() <=birdNum) {
			calcOver();
			return;
		}

		// 如果只剩下一张牌 问要不要&& isBuzhang
		if (getLeftMajiangCount() == 1 ) {
			calcMoLastSeats(player.getSeat());
			sendAskLastMajiangRes(0);
			if (moLastSeats == null || moLastSeats.size() == 0) {
				calcOver();
			}
			return;
		}
		if (isBuzhang) {
			addMoTailPai(-1);
		}
		// 摸牌
		JzMj majiang = null;
		if (disCardRound != 0) {
			// 玩家手上的牌是双数，已经摸过牌了
			if (player.isAlreadyMoMajiang()) {
				return;
			}
			if (GameServerConfig.isDebug() && !player.isRobot()) {
				if (zpMap.containsKey(player.getUserId()) && zpMap.get(player.getUserId()) > 0) {
					majiang = JzMjQipaiTool.findMajiangByVal(leftMajiangs, zpMap.get(player.getUserId()));
					if (majiang != null) {
						zpMap.remove(player.getUserId());
						leftMajiangs.remove(majiang);
					}
				}
			}
			// 不是庄家第一次出牌
			// 不是第一次出牌 ，摸牌
			// majiang=majiangt
			// majiang = MajiangHelper.findMajiangByVal(leftMajiangs, 25);
			// leftMajiangs.remove(majiang);
			if (majiang == null) {
				majiang = getLeftMajiang();
			}
		}
		if (majiang != null) {
			addPlayLog(disCardRound + "_" + player.getSeat() + "_" + JzMjDisAction.action_moMjiang + "_"
					+ majiang.getId());
			player.moMajiang(majiang);
//			if(player.getCpnum()==0){
//				//第一次摸牌。板板胡啊
//				JzMjiangHu hu = new JzMjiangHu();
//				List<JzMj> copy2 = new ArrayList<>(player.getHandMajiang());
//				JzMjRule.checkXiaoHu2(hu, copy2, isBegin(), this);
//				if(hu.isBanbanhu()){
//					//System.out.println("============闲===起手摸牌板板胡");
//
//					List<Integer> winseat = new ArrayList<>();
//					winseat.add(player.getSeat());
//					calcOverQiShouHu(winseat);
//					return;
//				}
//			}
		}

		processHideMj(player);

		// 检查摸牌
		clearActionSeatMap();
		if (disCardRound == 0) {
			return;
		}
		setMoMajiangSeat(player.getSeat());
		List<Integer> arr = player.checkMo(majiang, false);

		if (!arr.isEmpty()) {
			// 如果杠了之后，摸牌不能杠，那有杠也不能杠
			if (!player.getGang().isEmpty() && !checkSameMj(player.getPeng(), majiang)) {
//				arr.set(JzMjAction.MINGGANG, 0);
//				arr.set(JzMjAction.ANGANG, 0);
//				arr.set(JzMjAction.BUZHANG, 0);
//				arr.set(JzMjAction.BUZHANG_AN, 0);
			}

			// 报听 不能杠
			if (player.isBaoting()) {
				arr.set(JzMjAction.MINGGANG, 0);
				arr.set(JzMjAction.ANGANG, 0);
				arr.set(JzMjAction.BUZHANG, 0);
				arr.set(JzMjAction.BUZHANG_AN, 0);
			}
			coverAddActionSeat(player.getSeat(), arr);
		}
		MoMajiangRes.Builder res = MoMajiangRes.newBuilder();
		res.setUserId(player.getUserId() + "");
		res.setRemain(getLeftMajiangCount());
		res.setSeat(player.getSeat());

		// boolean playCommand = !player.getGang().isEmpty() && arr.isEmpty();
		logMoMj(player, majiang, arr);
		for (JzMjPlayer seat : seatMap.values()) {
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

		boolean hasHu = false;
		for(Map.Entry<Integer,List<Integer>> entry :actionSeatMap.entrySet()){
			if(JzMjAction.hasHu(entry.getValue())){
				hasHu =true;
				break;
			};
		}
//		boolean hasGang = false;
//		for(Map.Entry<Integer,List<Integer>> entry :actionSeatMap.entrySet()){
//			if(JzMjAction.hasGang(entry.getValue())){
//				hasGang =true;
//				break;
//			};
//		}
		if(birdNum>0 && getLeftMajiangCount()<=birdNum && !hasHu ){
			//保留鸟牌，比如抓6鸟就要留6张牌不能摸
			calcOver();
		}
	}

	private boolean checkSameMj(List<JzMj> list, JzMj majiang) {
		if (list.size() == 0) {
			return false;
		}
		for (JzMj mj : list) {
			if (mj.getVal() == majiang.getVal()) {
				return true;
			}
		}
		return false;
	}

	public void calcMoLastSeats(int firstSeat) {
		for (int i = 0; i < getMaxPlayerCount(); i++) {
			JzMjPlayer player = seatMap.get(firstSeat);
			if (player.isTingPai(-1)) {
				setFristLastMajiangSeat(player.getSeat());
				addMoLastSeat(player.getSeat());
			}
			firstSeat = calcNextSeat(firstSeat);
		}
		if (moLastSeats != null && moLastSeats.size() > 0) {
			setFristLastMajiangSeat(moLastSeats.get(0));
			setAskLastMajaingSeat(moLastSeats.get(0));
		}
	}

	/**
	 * 推送摸海底消息
	 *
	 * @param seat
	 *            0表示推送第一个，>0表示当前推送的是自己，就推送
	 * @return 返回当前推送的座位
	 */
	public void sendAskLastMajiangRes(int seat) {
		if (moLastSeats == null || moLastSeats.size() == 0) {
			return;
		}
		int sendSeat = moLastSeats.get(0);
		if (seat > 0 && sendSeat != seat) {
			return;
		}
		setAskLastMajaingSeat(sendSeat);
		JzMjPlayer player = seatMap.get(sendSeat);
		sendMoLast(player, 1);
	}

	private void buildPlayRes(PlayMajiangRes.Builder builder, Player player, int action, List<JzMj> majiangs) {
		JzMjResTool.buildPlayRes(builder, player, action, majiangs);
		buildPlayRes1(builder);
	}

	private void buildPlayRes1(PlayMajiangRes.Builder builder) {
		// builder
	}

	/**
	 * 胡小胡
	 *
	 * @param player
	 * @param majiangs
	 *            小胡展示的麻将
	 * @param xiaoHuType
	 *            小胡类型 JzMjAction
	 * @param action
	 */
	public synchronized void huXiaoHu(JzMjPlayer player, List<JzMj> majiangs, int xiaoHuType, int action) {
		List<Integer> actionList = actionSeatMap.get(player.getSeat());
		if (actionList == null || actionList.isEmpty() || actionList.get(xiaoHuType) == 0) {// 不能胡该小胡
			return;
		}

		JzMjiangHu hu = new JzMjiangHu();
		List<JzMj> copy2 = new ArrayList<>(player.getHandMajiang());
		JzMjRule.checkXiaoHu2(hu, copy2, isBegin(), this);

		HashMap<Integer, Map<Integer, List<JzMj>>> xiaohuMap = hu.getXiaohuMap();
		Map<Integer, List<JzMj>> map = xiaohuMap.get(xiaoHuType);
		if (map == null) {
			return;
		}

		List<Integer> keys = new ArrayList<>();
		if (map.size() == 0) {
			keys.add(0);
		} else {
			keys.addAll(map.keySet());
		}

		int huCard = 0;

		for (Integer key : keys) {
			if (!player.canHuXiaoHu2(xiaoHuType, key)) {
				continue;
			}
			huCard = key;
			break;
		}

		if (!player.getHandMajiang().containsAll(majiangs)) {// 小胡展示的麻将不存在
			return;
		}


		List<JzMj> vals= map.get(huCard);

		addXiaoHuCards(player, xiaoHuType, vals);
		player.addXiaoHuMjList(majiangs);

		removeActionSeat(player.getSeat());
		addPlayLog(disCardRound + "_" + player.getSeat() + "_" + JzMjDisAction.action_xiaohu + "_"
				+ JzMjHelper.toMajiangStrs(majiangs) + "_" + xiaoHuType);
		PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
		buildPlayRes(builder, player, JzMjDisAction.action_xiaohu, majiangs);
		builder.addHuArray(xiaoHuType);
		boolean isBegin = isBegin();
		List<Integer> selfActList = player.checkMo(null, isBegin);
		if (!selfActList.isEmpty()) {
			if (isBegin) {
				if (hasXiaoHu(selfActList)) {
					addActionSeat(player.getSeat(), selfActList);
				}
			} else {
				addActionSeat(player.getSeat(), selfActList);
			}
		}

		logAction(player, action, xiaoHuType, majiangs, selfActList);
		for (JzMjPlayer seat : seatMap.values()) {
			PlayMajiangRes.Builder copy = builder.clone();
			if (actionSeatMap.containsKey(seat.getSeat())) {
				copy.addAllSelfAct(actionSeatMap.get(seat.getSeat()));
			}
			seat.writeSocket(copy.build());
		}
		calcXiaoHuPoint(player, xiaoHuType);
		addShowMjSeat(player.getSeat(), xiaoHuType);
		checkBegin2(player);
	}
	/**
	 * 如果是起手判断是否还有人可胡小胡，检查庄家发牌后有没有操作，没有的话进行结算
	 */
	public void checkBegin2(JzMjPlayer player) {
		boolean isBegin = isBegin();
		if (isBegin && !hasXiaoHu()) {
			List<Integer> winseat = new ArrayList<>();
			winseat.add(player.getSeat());
			 calcOverQiShouHu(winseat);
		}
	}



	public boolean checkXiaoHuCards(int type,int val){

		for (JzMjPlayer seat : seatMap.values()) {
			List<Integer> vals = seat.getHuxiaoHuCardVal().get(type);
			if(vals!=null && vals.contains(val)){
				return true;
			}
		}
		return false;
	}


	private void addXiaoHuCards(JzMjPlayer player, int xiaoHuType, List<JzMj> mjVals) {
		List<Integer> valus = new ArrayList<Integer>();
		if(mjVals!=null && !mjVals.isEmpty()){
			for(JzMj mj: mjVals){
				valus.add(mj.getVal());
			}
		}

		player.addLiuLiuShunHu2(xiaoHuType, valus);
	}

	public synchronized void chupaibaoting(JzMjPlayer player, List<JzMj> majiangs, int xiaoHuType, int action) {
		List<Integer> actionList = actionSeatMap.get(player.getSeat());
		if (actionList == null || actionList.isEmpty() || actionList.get(JzMjAction.BAOTING) == 0) {// 不能报听
			return;
		}

		majiangs =getNowDisCardIds();
		List<JzMj> chupailist = new ArrayList<>(majiangs);
		//System.out.println("chupaibaoting===="+majiangs);
 		if(getTable_tingpai()==1){
 			//出完牌报听流程
			PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
			clearActionSeatMap();
			clearGangActionMap();
			setNowDisCardSeat(calcNextSeat(player.getSeat()));
			player.clearPassHu();
			player.setBaotingStatus(1);

			//听牌消息
			buildPlayRes(builder, player, JzMjDisAction.action_baoting, majiangs);
			for (JzMjPlayer seat : seatMap.values()) {
				List<Integer> list = new ArrayList<>();
				if (seat.getUserId() != player.getUserId()) {
					list = seat.checkDisMajiang(majiangs.get(0));
					if (list.contains(1)) {
						// 如果杠了之后，別人出的牌不能做杠操作
//						if (!seat.getGang().isEmpty()) {
//							list.set(JzMjAction.MINGGANG, 0);
//							list.set(JzMjAction.ANGANG, 0);
//							list.set(JzMjAction.BUZHANG, 0);
//						}
						addActionSeat(seat.getSeat(), list);
					}
				}
			}
			//重发出牌消息
			PlayMajiangRes.Builder chupaicopy = PlayMajiangRes.newBuilder();
			buildPlayRes(chupaicopy, player, JzMjDisAction.action_chupai, chupailist);
			sendDisMajiangAction(chupaicopy);
			setDisEventAction(action);

			//发送听牌消息
			 sendDisMajiangAction(builder);

			// 取消漏炮
			player.setPassMajiangVal(0);
//			addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + JzMjHelper.toMajiangStrs(majiangs));
//			setIsBegin(false);
			// 给下一家发牌
			setTable_tingpai(0);
			checkMo();
 			return;
		}
		 //正常起手报听流程。
		removeActionSeat(player.getSeat());
		addPlayLog(disCardRound + "_" + player.getSeat() + "_" + JzMjDisAction.action_baoting + "_"
				+ JzMjHelper.toMajiangStrs(majiangs) + "_" + xiaoHuType);
		PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
		buildPlayRes(builder, player, JzMjDisAction.action_baoting, majiangs);
		player.setBaotingStatus(1);

		List<Integer> selfActList = player.checkMo(null, isBegin);
		if (!selfActList.isEmpty()) {
			if (isBegin) {
				if (hasXiaoHu(selfActList)) {
					addActionSeat(player.getSeat(), selfActList);
				}
			} else {
				addActionSeat(player.getSeat(), selfActList);
			}
		}
		logAction(player, action, xiaoHuType, majiangs, selfActList);
		for (JzMjPlayer seat : seatMap.values()) {
			PlayMajiangRes.Builder copy = builder.clone();
			if (actionSeatMap.containsKey(seat.getSeat())) {
				copy.addAllSelfAct(actionSeatMap.get(seat.getSeat()));
			}

			seat.writeSocket(copy.build());
		}
		checkBegin(player);

	}

	public synchronized void baoting(JzMjPlayer player, List<JzMj> majiangs, int xiaoHuType, int action) {
		List<Integer> actionList = actionSeatMap.get(player.getSeat());
		if (actionList == null || actionList.isEmpty() || actionList.get(JzMjAction.BAOTING) == 0) {// 不能报听
			return;
		}
		boolean isBegin = isBegin();
		if(!isBegin){
			return;
		}
		removeActionSeat(player.getSeat());
		addPlayLog(disCardRound + "_" + player.getSeat() + "_" + JzMjDisAction.action_baoting + "_"
				+ JzMjHelper.toMajiangStrs(majiangs) + "_" + xiaoHuType);
		PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
		buildPlayRes(builder, player, JzMjDisAction.action_baoting, majiangs);
		player.setBaotingStatus(1);

		List<Integer> selfActList = player.checkMo(null, isBegin);
		if (!selfActList.isEmpty()) {
			if (isBegin) {
				if (hasXiaoHu(selfActList)) {
					addActionSeat(player.getSeat(), selfActList);
				}
			} else {
				addActionSeat(player.getSeat(), selfActList);
			}
		}
		logAction(player, action, xiaoHuType, majiangs, selfActList);
		for (JzMjPlayer seat : seatMap.values()) {
			PlayMajiangRes.Builder copy = builder.clone();
			if (actionSeatMap.containsKey(seat.getSeat())) {
				copy.addAllSelfAct(actionSeatMap.get(seat.getSeat()));
			}
			seat.writeSocket(copy.build());
		}
		checkBegin(player);
	}
	/**
	 * 如果是起手判断是否还有人可胡小胡，检查庄家发牌后有没有操作，没有的话通知庄家出牌
	 */
	public void checkBegin(JzMjPlayer player) {
		boolean isBegin = isBegin();
		if (isBegin && !hasXiaoHu() && !hasBaoTing()) {
			JzMjPlayer bankPlayer = seatMap.get(lastWinSeat);
			List<Integer> actList = bankPlayer.checkMo(null, isBegin);
			//System.out.println("=================如果是起手判断是否还有人可胡小胡，检查庄家发牌后==========");
			if (!actList.isEmpty()) {
				PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
				buildPlayRes(builder, player, JzMjDisAction.action_pass, new ArrayList<>());
				if (!actList.isEmpty()) {
					addActionSeat(bankPlayer.getSeat(), actList);
					builder.addAllSelfAct(actList);
				}
				bankPlayer.writeSocket(builder.build());
			}
			ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.req_com_kt_ask_dismajiang);// 推送庄家出牌
			bankPlayer.writeSocket(com.build());
		}
	}

	/**
	 * 玩家表示胡
	 *
	 * @param player
	 * @param majiangs
	 */
    private void hu(JzMjPlayer player, List<JzMj> majiangs, int action) {
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return;
        }
        if (huConfirmMap.containsKey(player.getSeat())) {
            return;
        }

        if(hasXiaoHu()){
        	return;
        }

        checkRemoveMj(player, action);
        boolean zimo = player.isAlreadyMoMajiang();
        JzMj disMajiang = null;
        JzMjiangHu huBean = null;
        List<JzMj> huMjs = new ArrayList<>();
        int fromSeat = 0;
        boolean isGangShangHu = false;

        if (!zimo) {
			if (moGangHuList.contains(player.getSeat())) {// 强杠胡
                disMajiang = moGang;
                fromSeat = moMajiangSeat;
                huMjs.add(moGang);
			}
			else if (isHasGangAction(player.getSeat())) {
				// 杠上炮 杠上花
                fromSeat = moMajiangSeat;
                Map<Integer, JzMjiangHu> huMap = new HashMap<>();
                List<Integer> daHuMjIds = new ArrayList<>();
                List<Integer> huMjIds = new ArrayList<>();
                for (int majiangId : gangSeatMap.keySet()) {
                    JzMjiangHu temp = player.checkHu(JzMj.getMajang(majiangId), disCardRound == 0);
                    if(!temp.isHu()){
                        continue;
                    }
                    temp.initDahuList();
                    huMap.put(majiangId,temp);
                    huMjIds.add(majiangId);
                    if(temp.isDahu()){
                        daHuMjIds.add(majiangId);
                    }
                }
                if(daHuMjIds.size() >0){
					// 有大胡
                    for(int mjId : huMjIds){
                        JzMjiangHu temp = huMap.get(mjId);
                        if (moMajiangSeat == player.getSeat()) {
                            temp.setGangShangHua(true);
                            isGangShangHu = true;
                        } else {
							// 出掉杠牌
                        	JzMjPlayer mPlayer = seatMap.get(moMajiangSeat);
                       	 removeGangMj(mPlayer, mjId);
                            temp.setGangShangPao(true);
                        }
                        temp.initDahuList();
                        if(huBean == null){
                            huBean = temp;
                        }else{
                            huBean.addToDahu(temp.getDahuList());
                            huBean.getShowMajiangs().add(JzMj.getMajang(mjId));
                        }
                        player.addHuMjId(mjId);
                        huMjs.add(JzMj.getMajang(mjId));
                    }
                }else if(huMjIds.size() > 0){
					// 没有大胡
                    for(int mjId : huMjIds) {
                        JzMjiangHu temp = huMap.get(mjId);
                        if (moMajiangSeat == player.getSeat()) {
                            temp.setGangShangHua(true);
                            isGangShangHu = true;
                        } else {
                        	JzMjPlayer mPlayer = seatMap.get(moMajiangSeat);
                        	removeGangMj(mPlayer, mjId);
                            temp.setGangShangPao(true);
                        }
                        temp.initDahuList();
                        if(huBean == null){
                            huBean = temp;
                        }else{
                            huBean.addToDahu(temp.getDahuList());
                            huBean.getShowMajiangs().add(JzMj.getMajang(mjId));
                        }
                        player.addHuMjId(mjId);
                        huMjs.add(JzMj.getMajang(mjId));
                    }
                }else{
                    huBean = new JzMjiangHu();
                }

                if (huBean.isHu()) {
                    if (disCardSeat == player.getSeat()) {
                        zimo = true;
                    }
                }

            }
			else if (lastMajiang != null) {
                huBean = player.checkHu(lastMajiang, disCardRound == 0);
                if (huBean.isHu()) {
                    if (moLastMajiangSeat == player.getSeat()) {
                        huBean.setHaidilaoyue(true);
                    } else {
                        huBean.setHaidipao(true);
                    }
                    huBean.initDahuList();
                }
                fromSeat = moLastMajiangSeat;
                huMjs.add(lastMajiang);

            } else if (!nowDisCardIds.isEmpty()) {
                disMajiang = nowDisCardIds.get(0);
                fromSeat = disCardSeat;
                huMjs.add(disMajiang);
            }
        }else{
        	//胡的牌
            huMjs.add(player.getHandMajiang().get(player.getHandMajiang().size()-1));

        }

		int tempcp =seatMap.get(lastWinSeat).getCpnum();
		if(tempcp==1 && nowDisCardSeat!=lastWinSeat){
//		 List<JzMj> tempMj = seatMap.get(lastWinSeat).getOutMajing();
            if(huBean == null){
               // huBean = player.checkHu(lastMajiang,true);
				huBean = player.checkHu(disMajiang,true);
            }
            if(huBean.isHu()){
                huBean.setDihu(true);
                huBean.initDahuList();
            }
        }
		if(huBean ==null && player.getCpnum() == 0 ){
			huBean = player.checkHu(disMajiang, true);
			huBean.setTianhu(true);
			huBean.initDahuList();
		}
        if (huBean == null) {
			// 自摸
            huBean = player.checkHu(disMajiang, disCardRound == 0);
            if (huBean.isHu() && lastMajiang != null) {
                huBean.setHaidilaoyue(true);
                huBean.initDahuList();
            }
			//System.out.println("================自摸胡===============================");
			//System.out.println("huSeat ="+player.getSeat());
			if(gangSaveSeat>0 && gangSaveSeat == player.getSeat()){
				if(moMajiangSeat  == player.getSeat()){
					if(null==huBean){
						huBean.initDahuList();
					}
					isGangShangHu =true;
					huBean.setGangShangHua(true);
					huBean.initDahuList();
				}
			}
        }
		 /// 2020年5月22日15:53:19 抢杠胡修正
		 int tpseat =player.getSeat();
		 if(gangSaveSeat>0 && gangSaveSeat !=tpseat){
				if(moMajiangSeat  != tpseat){
					if(null==huBean){
						huBean.initDahuList();
					}
					huBean.setGangShangPao(true);
					huBean.initDahuList();
				}
		 }

        if (!huBean.isHu()) {
			// 检测抢杠胡
        	if(moGangHuList.contains(player.getSeat())) {
        		 List<Integer> hu = player.checkDisMaj(disMajiang, false);
        		 if (!hu.isEmpty() && hu.get(0) == 1) {
        			 huBean.setHu(true);
        		 }
        	}
        	if(!huBean.isHu()) {
        		 return;
        	}
        }
		//检查报听
		if(!zimo){
			if(!huBean.isBaoting() && player.isBaoting()) {
				huBean.setBaoting(true);
				huBean.initDahuList();
			}
			JzMjPlayer disPlayer = seatMap.get(disCardSeat);
			if(!huBean.isBaoting() && disPlayer != null && disPlayer.isBaoting()){
				huBean.setBaoting(true);
				huBean.initDahuList();
			}
		}else{
			if(!huBean.isBaoting() && player.isBaoting()) {
				huBean.setBaoting(true);
				huBean.initDahuList();
			}
		}

		// 没出牌就有人胡了，天胡
        if(disCardRound==0) {
        	 huBean.setTianhu(true);
             huBean.initDahuList();
        }else if(disCardRound==1&&player.getSeat()!= moMajiangSeat) {
        	 huBean.setDihu(true);
             huBean.initDahuList();
        }


		// 算牌型的分
        if (moGangHuList.contains(player.getSeat())) {
			// 补张的时候不算抢杠胡
            if (disEventAction != JzMjDisAction.action_buzhang) {
                huBean.setQGangHu(true);
                huBean.initDahuList();
            }
			// 抢杠胡
          //  JzMjPlayer moGangPlayer = getPlayerByHasMajiang(moGang);
            //if (moGangPlayer == null) {
            JzMjPlayer moGangPlayer = seatMap.get(nowDisCardSeat);
            //}
            List<JzMj> moGangMajiangs = new ArrayList<>();
            moGangMajiangs.add(moGang);
            moGangPlayer.removeGangMj(moGangMajiangs.get(0));
//            if(huBean.isQGangHu()) {
//
//            }else {
//            	moGangPlayer.addOutPais(moGangMajiangs, 0,0);
//            }
			// 摸杠被人胡了 相当于自己出了一张牌
            recordDisMajiang(moGangMajiangs, moGangPlayer);
           // addPlayLog(disCardRound + "_" + moGangPlayer.getSeat() + "_" + 0 + "_" + JzMjHelper.toMajiangStrs(moGangMajiangs));
        }
        if (huBean.getDahuPoint() > 0) {
            player.setDahu(huBean.getDahuList());
        }
//        if (huBean.getDahuPoint() > 0) {
//            player.setDahu(huBean.getDahuList());
//            if (zimo) {
//                int point = 0;
//                for (JzMjPlayer seatPlayer : seatMap.values()) {
//                    if (seatPlayer.getSeat() != player.getSeat()) {
//                        point += huBean.getDahuPoint();
//                        seatPlayer.changeLostPoint(-huBean.getDahuPoint());
//                    }
//                }
//                player.changeLostPoint(point);
//            } else {
//                player.changeLostPoint(huBean.getDahuPoint());
//                seatMap.get(disCardSeat).changeLostPoint(-huBean.getDahuPoint());
//            }
//        }

        if(isGangShangHu){
			// 杠上花，只胡一张牌时，另外一张牌需要打出
            List<JzMj> gangDisMajiangs = getGangDisMajiangs();
            List<JzMj> chuMjs = new ArrayList<>();
            if(gangDisMajiangs != null && gangDisMajiangs.size() >0){
                for(JzMj mj : gangDisMajiangs){
                    if(!huMjs.contains(mj)){
                        chuMjs.add(mj);
                    }
                }
            }
            if(chuMjs != null){
                PlayMajiangRes.Builder chuPaiMsg = PlayMajiangRes.newBuilder();
                buildPlayRes(chuPaiMsg, player, JzMjDisAction.action_chupai, chuMjs);
                chuPaiMsg.setFromSeat(-1);
                broadMsgToAll(chuPaiMsg.build());
                player.addOutPais(chuMjs, JzMjDisAction.action_chupai,player.getSeat());
            }
        }

        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        buildPlayRes(builder, player, action, huBean.getShowMajiangs());
        builder.addAllHuArray(player.getDahu());
        if (zimo) {
            builder.setZimo(1);
        }
        builder.setFromSeat(fromSeat);
		// 胡
        for (JzMjPlayer seat : seatMap.values()) {
			// 推送消息
            seat.writeSocket(builder.build());
        }
		// 加入胡牌数组
        addHuList(player.getSeat(), disMajiang == null ? 0 : disMajiang.getId());
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_"+ JzMjHelper.toMajiangStrs(huMjs)+"_"+StringUtil.implode(player.getDahu(), ","));
        if (isCalcOver()) {
			// 等待别人胡牌 如果都确认完了，胡
            calcOver();
        }else{
            player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip);
        }
    }

	private void checkRemoveMj(JzMjPlayer player, int action) {
		//JzMj mjA = null;

        List<JzMj> mjBs = new ArrayList<JzMj>();
		for (int majiangId2 : gangSeatMap.keySet()) {
			Map<Integer, List<Integer>> map = gangSeatMap.get(majiangId2);
			List<Integer> actList = map.get(player.getSeat());
			if (actList == null) {
				continue;
			}
//			if (actList.get(JzMjAction.ZIMO) == 1) {
//				mjA = JzMj.getMajang(majiangId2);
//			} else

			if (actList.get(JzMjAction.MINGGANG) == 1) {
				mjBs.add(JzMj.getMajang(majiangId2));
			}
        }

		if(mjBs.size()>0) {
//			if(mjA.getId() !=mjB.getId()) {
			// 从手牌移除掉
				List<JzMj> list = new ArrayList<>();
				list.addAll(mjBs);
				checkMoOutCard(list, player, action);
//			}
		}
	}

	private void removeGangMj(JzMjPlayer player, int mjId) {
		List<JzMj> moList = new ArrayList<>();
		moList.add(JzMj.getMajang(mjId));
		player.addOutPais(moList, 0, player.getSeat());
	}

	/**
	 * 找出拥有这张麻将的玩家
	 *
	 * @param majiang
	 * @return
	 */
	private JzMjPlayer getPlayerByHasMajiang(JzMj majiang) {
		for (JzMjPlayer player : seatMap.values()) {
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
		// 起牌小胡 本局结束开始下局
		if (!huActionList.isEmpty()) {
			over = true;
			JzMjPlayer moGangPlayer = null;
			if (!moGangHuList.isEmpty()) {
				// 如果有抢杠胡
				moGangPlayer = getPlayerByHasMajiang(moGang);
				if (moGangPlayer == null) {
					moGangPlayer = seatMap.get(moMajiangSeat);
				}
				LogUtil.monitor_i("mogang player:" + moGangPlayer.getSeat() + " moGang:" + moGang);
			}
			for (int huseat : huActionList) {
				if (moGangPlayer != null) {
					// 被抢杠的人可以胡的话 跳过
//					if (moGangPlayer.getSeat() == huseat) {
//						continue;
//					}
				}
				if (!huConfirmMap.containsKey(huseat)) {
					over = false;
					break;
				}
			}
		}

		if (!over) {
			JzMjPlayer disCSMajiangPlayer = seatMap.get(disCardSeat);
			for (int huseat : huActionList) {
				if (huConfirmMap.containsKey(huseat)) {
					if (disCardRound == 0) {
						// 天胡
						removeActionSeat(huseat);
					}
					continue;
				}
				PlayMajiangRes.Builder disBuilder = PlayMajiangRes.newBuilder();
				JzMjPlayer seatPlayer = seatMap.get(huseat);
				buildPlayRes(disBuilder, disCSMajiangPlayer, 0, null);
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
	 * 吃碰杠
	 *
	 * @param player
	 * @param majiangs
	 * @param action
	 */
	private void chiPengGang(JzMjPlayer player, List<JzMj> majiangs, int action) {
		List<Integer> actionList0 = actionSeatMap.get(player.getSeat());
		if(actionList0==null ||actionList0.isEmpty()){
			return;
		}

		PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
		logAction(player, action, 0, majiangs, null);

		List<Integer> huList = getHuSeatByActionMap();
		huList.remove((Object) player.getSeat());
		// if (!huList.isEmpty()) {
		// // 胡最优先
		// return;
		// }

		// 处理杠完可吃可碰又可以胡，吃碰的话那就等于过胡了
		if (nowDisCardIds.size() > 1) {
			for (JzMj mj : nowDisCardIds) {
				List<Integer> hu = player.checkDisMaj(mj, false);
				if (!hu.isEmpty() && hu.get(0) == 1) {
					// && (actionList.get(JzMjAction.HU) == 1)
					List<Integer> actionList = actionSeatMap.get(player.getSeat());
					if (actionList != null) {
						actionList.set(JzMjAction.HU, 0);
					}
					player.setPassMajiangVal(mj.getVal());
					break;
				}
			}
		}

		if (!checkAction(player, majiangs, new ArrayList<>(), action)) {
			player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip);
			return;
		}

		List<JzMj> handMajiang = new ArrayList<>(player.getHandMajiang());
		JzMj disMajiang = null;
		if (isHasGangAction()) {
			for (int majiangId : gangSeatMap.keySet()) {
				if (action == JzMjDisAction.action_chi) {
					List<Integer> majiangIds = JzMjHelper.toMajiangIds(majiangs);
					if (majiangIds.contains(majiangId)) {
						disMajiang = JzMj.getMajang(majiangId);
						gangActedMj = disMajiang;
						handMajiang.add(disMajiang);
						if (majiangs.size() > 1) {
							majiangs.remove(disMajiang);
						}
						break;
					}
				} else {
					JzMj mj = JzMj.getMajang(majiangId);
					if (mj != null && majiangs.get(0).getVal() == mj.getVal()) {
						disMajiang = mj;
						int removeIndex = -1;
						for (int i = 0; i < majiangs.size(); i++) {
							if (majiangs.get(i).getId() == majiangId) {
								removeIndex = i;
							}
						}
						if (removeIndex != -1) {
							majiangs.remove(removeIndex);
						}
					}
				}
			}
			if (disMajiang == null) {
				return;
			}
		} else {
			if (!nowDisCardIds.isEmpty()) {
				disMajiang = nowDisCardIds.get(0);
			}
		}

		int sameCount = 0;
		boolean moMj = true;
		if (majiangs.size() > 0) {
			sameCount = JzMjHelper.getMajiangCount(majiangs, majiangs.get(0).getVal());
		}
		if (action == JzMjDisAction.action_buzhang) {
			if (sameCount == 0) {
				majiangs.add(disMajiang);
			}
			majiangs = JzMjHelper.getMajiangList(player.getHandMajiang(), majiangs.get(0).getVal());
			sameCount = majiangs.size();
			if (sameCount == 0) {
				majiangs.add(disMajiang);
			}
		} else if (action == JzMjDisAction.action_minggang) {
			if (majiangs.size() == 0) {
				majiangs.add(disMajiang);
			}
			// 如果是杠 后台来找出是明杠还是暗杠
			majiangs = JzMjHelper.getMajiangList(player.getHandMajiang(), majiangs.get(0).getVal());
			sameCount = majiangs.size();
			if (sameCount == 4) {
				// 有4张一样的牌是暗杠
				action = JzMjDisAction.action_angang;
			} else if (sameCount == 0) {
				majiangs.add(disMajiang);
			}
			// 其他是明杠

		} else if (action == JzMjDisAction.action_buzhang_an) {
			// 暗杠补张
			majiangs = JzMjHelper.getMajiangList(player.getHandMajiang(), majiangs.get(0).getVal());
			sameCount = majiangs.size();
		}
		// /////////////////////
		if (action == JzMjDisAction.action_chi) {
			boolean can = canChi(player, player.getHandMajiang(), majiangs, disMajiang);
			if (!can) {
				return;
			}
		}
		else if (action == JzMjDisAction.action_peng) {
			boolean can = canPeng(player, majiangs, sameCount, disMajiang);
			if (!can) {
				return;
			}
		}
		else if (action == JzMjDisAction.action_buzhang || action == JzMjDisAction.action_minggang) {
			// 2020年5月21日16:38:29 靖州麻将随时都能杠修改 吧明杠action转补涨
			action = JzMjDisAction.action_buzhang;
			boolean can = false;
			if (sameCount == 4) {
				can = canAnGang(player, majiangs, sameCount, action);
			} else {
				can = canMingGang(player, player.getHandMajiang(), majiangs, sameCount, disMajiang);
			}
			// 如果只剩下一张牌 问要不要&& isBuzhang
			if (getLeftMajiangCount() == 1) {
				player.writeErrMsg("海底不能补");
				return;
			}
			if (!can) {
				return;
			}
			// 特殊处理一张牌明杠的时候别人可以胡
			if (sameCount == 1 && canGangHu()) {
				if (checkQGangHu(player, majiangs, action,sameCount)) {
					// 抢杠胡可以杠下来
					// return;
					moMj = false;
				}
			}
		}
		else if (action == JzMjDisAction.action_buzhang_an || action == JzMjDisAction.action_angang) {
			// 2020年5月21日16:38:29 靖州麻将随时都能杠修改 吧暗杠action转补涨
			// action = JzMjDisAction.action_buzhang_an;
			boolean can = false;
			if (sameCount == 4) {
				can = canAnGang(player, majiangs, sameCount, action);
			}
			// 如果只剩下一张牌 问要不要&& isBuzhang
			if (getLeftMajiangCount() == 1 ) {
				player.writeErrMsg("海底不能补");
				return;
			}

			if (!can) {
				return;
			}
		}
		else {
			return;
		}
		//

		boolean disMajiangMove = false;
		if (disMajiang != null) {
			// 碰或者杠
			if (action == JzMjDisAction.action_minggang && sameCount == 3) {
				// 接杠
				disMajiangMove = true;
			} else if (action == JzMjDisAction.action_chi) {
				// 吃
				disMajiangMove = true;
			} else if (action == JzMjDisAction.action_peng) {
				// 碰
				disMajiangMove = true;
			} else if (action == JzMjDisAction.action_buzhang && sameCount == 3) {
				// 自己三张补张
				disMajiangMove = true;
			}
		}
		if (disMajiangMove) {
			if (action == JzMjDisAction.action_chi) {
				majiangs.add(1, disMajiang);// 吃的牌放第二位
			} else {
				majiangs.add(disMajiang);
			}
			builder.setFromSeat(disCardSeat);
			List<JzMj> disMajiangs = new ArrayList<>();
			disMajiangs.add(disMajiang);
			seatMap.get(disCardSeat).removeOutPais(disMajiangs, action);
		}

		chiPengGang(builder, player, majiangs, action, moMj);

		calcPoint(player, action, sameCount, majiangs);
	}


	private void chiPengGang(PlayMajiangRes.Builder builder, JzMjPlayer player, List<JzMj> majiangs, int action,
                             boolean moMj) {
		setIsBegin(false);
//		int  sameCount = 0;
//		sameCount  = JzMjHelper.getMajiangCount(player.getHandMajiang(), majiangs.get(0).getVal());

		processHideMj(player);

		player.addOutPais(majiangs, action, disCardSeat);
		buildPlayRes(builder, player, action, majiangs);
		List<Integer> removeActList = removeActionSeat(player.getSeat());
		clearGangActionMap();
		if (moMj) {
			clearActionSeatMap();
		}

		addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + JzMjHelper.toMajiangStrs(majiangs));
		// 不是普通出牌
		setNowDisCardSeat(player.getSeat());
		checkClearGangDisMajiang();

		if (action == JzMjDisAction.action_chi || action == JzMjDisAction.action_peng) {
			List<Integer> arr = player.checkMo(null, false);
			// 吃碰之后还有操作
			if (!arr.isEmpty()) {
				arr.set(JzMjAction.ZIMO, 0);
				arr.set(JzMjAction.HU, 0);
				arr.set(JzMjAction.ZHONGTULIULIUSHUN, 0);
				arr.set(JzMjAction.ZHONGTUSIXI, 0);
				addActionSeat(player.getSeat(), arr);
			}
		}
		for (JzMjPlayer seatPlayer : seatMap.values()) {
			// 推送消息
			PlayMajiangRes.Builder copy = builder.clone();
			if (actionSeatMap.containsKey(seatPlayer.getSeat())) {
				copy.addAllSelfAct(actionSeatMap.get(seatPlayer.getSeat()));
			}
			seatPlayer.writeSocket(copy.build());
		}

		// 取消漏炮
		player.setPassMajiangVal(0);
//		if (action == JzMjDisAction.action_minggang || action == JzMjDisAction.action_angang) {
//			// 明杠和暗杠摸牌
//			if (moMj) {
//				 player.addCpnum(1);//庄家第一次出牌。闲家直接杠 杠完出牌不算报听了
//				  gangMoMajiang(player, majiangs.get(0), action);
//				// moMajiang(player, true);
//				//player.checkMo(getLeftMajiang(),fals)
//			}
//
//		} else
		 if (action == JzMjDisAction.action_buzhang ||action == JzMjDisAction.action_minggang) {
			// 补张
			if (moMj) {
				player.addCpnum(1);//庄家第一次出牌。闲家直接杠 杠完出牌不算报听了
				moMajiang(player, true);
			}
		} else if (action == JzMjDisAction.action_buzhang_an ||action == JzMjDisAction.action_angang) {
			// 补张
			player.addCpnum(1);//庄家第一次出牌。闲家直接杠 杠完出牌不算报听了
			moMajiang(player, true);
		}

		//calcPoint(player,action,sameCount,null);
		if (action == JzMjDisAction.action_chi || action == JzMjDisAction.action_peng) {
			sendTingInfo(player);
		}

		setDisEventAction(action);
		robotDealAction();
		logAction(player, action, 0, majiangs, removeActList);

		if (action == JzMjDisAction.action_buzhang ||action == JzMjDisAction.action_minggang) {
			// 补张
				JzMj momj =player.getLastMoMajiang();
				//System.out.println("=========gangSeatMap=====action_minggang============");
				//System.out.println(momj);
				//System.out.println("moMajiangseat ="+moMajiangSeat);
				//System.out.println("playerSeat ="+player.getSeat());
				gangSaveSeat =player.getSeat();
		}else if (action == JzMjDisAction.action_buzhang_an ||action == JzMjDisAction.action_angang) {
			gangSaveSeat =player.getSeat();
		}else{
			gangSaveSeat =0;
		}
	}


	/**
	 * 杠后摸两张牌
	 */
	private void gangMoMajiang(JzMjPlayer player, JzMj gangMajiang, int action) {
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
		if (leftMajiangs.size() == 0) {
			calcOver();
			return;
		}
		// 连摸两张牌
		int moNum = 1;

		if(gangMoSi==1) {
			moNum=1;
		}
		List<JzMj> moList = new ArrayList<>();
		Random r = new Random();
		gangDice = (r.nextInt(6) + 1) * 10 + (r.nextInt(6) + 1);
		int leftMjCount = getLeftMajiangCount();
		int leftDuo = leftMjCount % 2 == 0 ? leftMjCount / 2 : leftMjCount / 2 + 1;
//		if (leftDuo >= gangDice / 10 + gangDice % 10) {
			if (GameServerConfig.isDeveloper()) {
				JzMj majiang1 = getLeftMajiang();//JzMjHelper.findMajiangByVal(leftMajiangs,25);
				if (majiang1 != null) {
					leftMajiangs.remove(majiang1);
					moList.add(majiang1);
				}
			}else{
				JzMj majiang2 =  getLeftMajiang();
				if (majiang2 != null) {
					leftMajiangs.remove(majiang2);
					moList.add(majiang2);
				}
			}

			int mjCount = getLeftMajiangCount();
			if(mjCount==1){
				moNum=1;
			}else if(mjCount==2){
				moNum =1;
			}else if(mjCount==3){
				moNum =1;
			}else if(mjCount==4){
				if(gangMoSi==1) {
					moNum=1;
				}
			}

			while (moList.size() < moNum) {
				JzMj majiang = getLeftMajiang();
				if (majiang != null) {
					moList.add(majiang);
				} else {
					break;
				}
			}
			addMoTailPai(gangDice);
//		}

		addPlayLog(disCardRound + "_" + player.getSeat() + "_" + JzMjDisAction.action_moGangMjiang + "_" + gangDice
				+ "_" + JzMjHelper.implodeMajiang(moList, ","));

		// 检查摸牌
		clearActionSeatMap();
		clearGangActionMap();


		// 打出这两张牌
		setDisCardSeat(player.getSeat());
		setGangDisMajiangs(moList);
		setMoMajiangSeat(player.getSeat());
		player.setPassMajiangVal(0);

		setGangMajiang(gangMajiang);
		//setNowDisCardSeat(calcNextSeat(player.getSeat()));
		setNowDisCardSeat(player.getSeat());
		setNowDisCardIds(moList);
		 player.addOutPais(moList, 0,player.getSeat());
		// /////////////////////////////////////////////////////////////////////////////////////////
        logGangMoMj(player,moList);
		boolean canHu = false;
		List<JzMj> moGangMj1s = new ArrayList<>();
		// 摸了牌后可以胡牌
		for (JzMj majiang : moList) {
			for (JzMjPlayer seatPlayer : seatMap.values()) {
				List<Integer> actionList = seatPlayer.checkDisMaj(majiang, false);
				if (seatPlayer.getSeat() == player.getSeat()) {
					// 摸杠人只能胡
					if (JzMjAction.hasHu(actionList)) {
						boolean addGang = false;
						if (JzMjAction.hasGang(actionList)) {
							addGang = true;
						}
						actionList = JzMjAction.keepHu(actionList);
						actionList.set(JzMjAction.HU, 0);
						actionList.set(JzMjAction.ZIMO, 1);
						if (addGang) {
							actionList.set(JzMjAction.MINGGANG, 1);
							actionList.set(JzMjAction.BUZHANG, 1);
							moGangMj1s.add(majiang);
							player.addGangcGangMj(majiang.getId());
							// seatPlayer.moMajiang(majiang);
						}
						canHu = true;
						addActionSeat(player.getSeat(), actionList);
						List<Integer> list2 = new ArrayList<Integer>(actionList);
						addGangActionSeat(majiang.getId(), player.getSeat(), list2);
						logAction(seatPlayer, action, -1, Arrays.asList(majiang), actionList);
					} else if (JzMjAction.hasGang(actionList)) {
						actionList = JzMjAction.keepHu(actionList);
						actionList.set(JzMjAction.MINGGANG, 1);
						actionList.set(JzMjAction.BUZHANG, 1);
						moGangMj1s.add(majiang);
						player.addGangcGangMj(majiang.getId());
						// seatPlayer.moMajiang(majiang);
						addActionSeat(player.getSeat(), actionList);
						List<Integer> list2 = new ArrayList<Integer>(actionList);
						addGangActionSeat(majiang.getId(), player.getSeat(), list2);
						logAction(seatPlayer, action, -1, Arrays.asList(majiang), actionList);
					}
				} else {
					if (!actionList.isEmpty()) {
						addActionSeat(seatPlayer.getSeat(), actionList);
						List<Integer> list2 = new ArrayList<Integer>(actionList);
						addGangActionSeat(majiang.getId(), seatPlayer.getSeat(), list2);
						logAction(seatPlayer, action, -1, Arrays.asList(majiang), actionList);
					}
				}
			}
		}

		if (!moGangMj1s.isEmpty()) {
			player.moMajiang(moGangMj1s.get(0));
			if(moGangMj1s.size()>1){
				player.moMajiang(moGangMj1s.get(1));
			}
		}

		if (isHasGangAction(player.getSeat())) {
			if (!canHu) {
				gangNoticePlayer(player, gangMajiang, moList);
				for (JzMj moMj : moList) {
					Map<Integer, List<Integer>> seatActionList = gangSeatMap.get(moMj.getId());
					if (seatActionList != null && seatActionList.containsKey(player.getSeat())) {
						continue;
					}
					List<JzMj> list = new ArrayList<>();
					list.add(moMj);
					checkMoOutCard(list, player, action);
				}
			} else {
				// 自己的胡操作
				GangMoMajiangRes.Builder gangMsg = GangMoMajiangRes.newBuilder();
				gangMsg.setRemain(getLeftMajiangCount());
				gangMsg.setGangId(gangMajiang.getId());
				gangMsg.setUserId(player.getUserId() + "");
				gangMsg.setName(player.getName() + "");
				gangMsg.setSeat(player.getSeat());
				gangMsg.setReconnect(0);
				gangMsg.setDice(gangDice);
				gangMsg.setHasAct(isHasGangAction() ? 1 : 0);
				gangMsg.setMjNum(moList.size());
				for (JzMj moMj : moList) {
					GangPlayMajiangRes.Builder playerMsg = GangPlayMajiangRes.newBuilder();
					playerMsg.setMajiangId(moMj.getId());
					Map<Integer, List<Integer>> seatActionList = gangSeatMap.get(moMj.getId());

					if (seatActionList != null && seatActionList.containsKey(player.getSeat())) {
						playerMsg.addAllSelfAct(seatActionList.get(player.getSeat()));
					}
					gangMsg.addGangActs(playerMsg);
				}
				player.writeSocket(gangMsg.build());

				for (JzMjPlayer seatPlayer : seatMap.values()) {
					if (player.getSeat() != seatPlayer.getSeat()) {
						gangMsg.clearGangActs();
						seatPlayer.writeSocket(gangMsg.build());
						// 开杠人能胡，必胡，去掉其他人的所有操作
						removeActionSeat(seatPlayer.getSeat());
					}
				}
			}

		} else {
			// 自己打出两牌
			player.addOutPais(moList, 0, player.getSeat());

			addPlayLog(disCardRound + "_" + player.getSeat() + "_" + 0 + "_" + JzMjHelper.toMajiangStrs(moList));
			gangNoticePlayer(player, gangMajiang, moList);

			PlayMajiangRes.Builder chuPaiMsg = PlayMajiangRes.newBuilder();
			buildPlayRes(chuPaiMsg, player, JzMjDisAction.action_chupai, moList);
			for (JzMjPlayer seatPlayer : seatMap.values()) {
				chuPaiMsg.setFromSeat(-1);
				seatPlayer.writeSocket(chuPaiMsg.build());
			}
			broadMsgRoomPlayer(chuPaiMsg.build());

			sendTingInfo(player);
			if (isHasGangAction()) {
				// 如果有人能做动作
				robotDealAction();
			} else {
				if(birdNum>0 && getLeftMajiangCount()<=birdNum){
					//保留鸟牌，比如抓6鸟就要留6张牌不能摸
					calcOver();
					return;
				}
				checkMo();
			}
		}
	}

	private void checkMoOutCard(List<JzMj> list, JzMjPlayer player, int action) {

		player.addOutPais(list, 0, player.getSeat());
		addPlayLog(disCardRound + "_" + player.getSeat() + "_" + 0 + "_" + JzMjHelper.toMajiangStrs(list));
		logAction(player, action, 0, list, null);
		PlayMajiangRes.Builder chuPaiMsg = PlayMajiangRes.newBuilder();
		buildPlayRes(chuPaiMsg, player, JzMjDisAction.action_chupai, list);
		for (JzMjPlayer seatPlayer : seatMap.values()) {
			chuPaiMsg.setFromSeat(-1);
			seatPlayer.writeSocket(chuPaiMsg.build());
		}
	}

	private void gangNoticePlayer(JzMjPlayer player, JzMj gangMajiang, List<JzMj> moList) {
		// 发送摸牌消息res
		GangMoMajiangRes.Builder gangMsg = null;
		for (JzMjPlayer seatPlayer : seatMap.values()) {
			gangMsg = GangMoMajiangRes.newBuilder();
			gangMsg.setRemain(getLeftMajiangCount());
			gangMsg.setGangId(gangMajiang.getId());
			gangMsg.setUserId(player.getUserId() + "");
			gangMsg.setName(player.getName() + "");
			gangMsg.setSeat(player.getSeat());
			gangMsg.setReconnect(0);
			gangMsg.setDice(gangDice);
			gangMsg.setHasAct(isHasGangAction() ? 1 : 0);
			gangMsg.setMjNum(moList.size());
			for (JzMj majiang : moList) {
				GangPlayMajiangRes.Builder playerMsg = GangPlayMajiangRes.newBuilder();
				playerMsg.setMajiangId(majiang.getId());
				Map<Integer, List<Integer>> seatActionMap = gangSeatMap.get(majiang.getId());
				if (seatActionMap != null && seatActionMap.containsKey(seatPlayer.getSeat())) {
					playerMsg.addAllSelfAct(seatActionMap.get(seatPlayer.getSeat()));
				}
				gangMsg.addGangActs(playerMsg);
			}
			seatPlayer.writeSocket(gangMsg.build());
		}
		gangMsg.clearGangActs();
		broadMsgRoomPlayer(gangMsg.build());
	}

	private boolean checkQGangHu(JzMjPlayer player, List<JzMj> majiangs, int action, int sameCount) {
		PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
		Map<Integer, List<Integer>> huListMap = new HashMap<>();
		for (JzMjPlayer seatPlayer : seatMap.values()) {
			if (seatPlayer.getUserId() == player.getUserId()) {
				continue;
			}
			// 推送消息
			List<Integer> hu = seatPlayer.checkDisMaj(majiangs.get(0), false);
			hu = JzMjAction.keepHu(hu);
			if (!hu.isEmpty() && hu.get(0) == 1) {
				//勾了假将胡，自己假将，不能抢别人补张的牌
				if(getJiajianghu()==1){
					List<Integer> hu2 = seatPlayer.checkDisMaj(majiangs.get(0), true);
					if(action == JzMjDisAction.action_buzhang){
						if (hu2.isEmpty() || hu2.get(0) == 0) {
							continue;
						}
					}else{
						if(sameCount==3&&!hu2.isEmpty()&& hu2.get(0)==1){
							continue;
						}
					}

				}
				actionSeatMap.remove(seatPlayer.getSeat());
				addActionSeat(seatPlayer.getSeat(), hu);
				huListMap.put(seatPlayer.getSeat(), hu);
			}
		}

		// 可以胡牌
		if (!huListMap.isEmpty()) {
			setDisEventAction(action);
			setMoGang(majiangs.get(0), new ArrayList<>(huListMap.keySet()));
			List<JzMj> al = new ArrayList<JzMj>();
			al.add(majiangs.get(0));
			buildPlayRes(builder, player, action,al);
			for (Entry<Integer, List<Integer>> entry : huListMap.entrySet()) {
				PlayMajiangRes.Builder copy = builder.clone();
				JzMjPlayer seatPlayer = seatMap.get(entry.getKey());
				copy.addAllSelfAct(entry.getValue());
				seatPlayer.writeSocket(copy.build());
			}
			return true;
		}
		return false;

	}

	public void checkSendGangRes(Player player) {
		if (isHasGangAction()) {
			List<JzMj> moList = getGangDisMajiangs();
			JzMjPlayer disPlayer = seatMap.get(disCardSeat);
			GangMoMajiangRes.Builder gangbuilder = GangMoMajiangRes.newBuilder();
			gangbuilder.setGangId(gangMajiang.getId());
			gangbuilder.setUserId(disPlayer.getUserId() + "");
			gangbuilder.setName(disPlayer.getName() + "");
			gangbuilder.setSeat(disPlayer.getSeat());
			gangbuilder.setRemain(leftMajiangs.size());
			gangbuilder.setReconnect(1);
			gangbuilder.setDice(gangDice);
			gangbuilder.setHasAct(isHasGangAction() ? 1 : 0);
			gangbuilder.setMjNum(moList.size());
			for (JzMj mj : moList) {
				GangPlayMajiangRes.Builder playBuilder = GangPlayMajiangRes.newBuilder();
				playBuilder.setMajiangId(mj.getId());
				Map<Integer, List<Integer>> seatActionList = gangSeatMap.get(mj.getId());
				if (seatActionList != null && seatActionList.containsKey(player.getSeat())) {
					playBuilder.addAllSelfAct(seatActionList.get(player.getSeat()));
				}
				gangbuilder.addGangActs(playBuilder);
			}
			if (isHasGangAction(disCardSeat) && player.getSeat() != disCardSeat) {
				// 庄家未操作，其他玩家不能看到杠后摸的两张牌
				gangbuilder.clearGangActs();
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
	private void chuPai(JzMjPlayer player, List<JzMj> majiangs, int action) {
		if (majiangs.size() != 1) {
			return;
		}
		if (!player.isAlreadyMoMajiang()) {
			// 还没有摸牌
			return;
		}
		if (!tempActionMap.isEmpty() && player.getGang().isEmpty()) {
			LogUtil.e(player.getName() + "出牌清理临时操作！");
			clearTempAction();
		}
//		if (!player.getGang().isEmpty()) {
//			// 已经杠过了牌只能出摸得的牌
//			if (player.getLastMoMajiang().getId() != majiangs.get(0).getId()) {
//				return;
//			}
//		}
		if (!actionSeatMap.isEmpty() && player.getGang().isEmpty()) {// 出牌自动过掉手上操作
			guo(player, null, JzMjDisAction.action_pass);
		}
		if (!actionSeatMap.isEmpty() && player.getGang().isEmpty()) {
			player.writeErrMsg("请等待其他玩家操作完毕！");
			player.writeComMessage(WebSocketMsgType.res_code_mj_dis_err, action, majiangs.get(0).getId());
			return;
		}
		//第一次出牌。出完能报听。
		player.addCpnum(1);
		boolean istp = false;
		if(player.getCpnum()==1 && player.getSeat()==lastWinSeat){
			//庄家出第一章可报听 闲家可起手报听
			PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
			buildPlayRes(builder, player, action, majiangs);
			clearActionSeatMap();
			clearGangActionMap();
			recordDisMajiang(majiangs, player);
			player.addOutPais(majiangs, action, player.getSeat());
			player.clearPassHu();
			istp =player.isFirstOutTingPai();
			if(istp){
				logAction(player, action, 0, majiangs, null);
				List<Integer> actionList = new ArrayList<>();
				JzMjAction arr1 = new JzMjAction();
				arr1.addBaoTing();
				int [] arr = arr1.getArr();
				for (int val : arr) {
					actionList.add(val);
				}
				addActionSeat(player.getSeat(), actionList);
				logChuPaiActList(player, majiangs.get(0), actionList);
				builder.addAllSelfAct(actionList);
				player.writeSocket(builder.build());
				setDisEventAction(action);
	  			 //sendDisMajiangAction(builder);
				// 取消漏炮
				player.setPassMajiangVal(0);
				addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + JzMjHelper.toMajiangStrs(majiangs));
				setIsBegin(false);
				setTable_tingpai(1);
				sendTingInfo(player);
				return;
			}else{
				setNowDisCardSeat(calcNextSeat(player.getSeat()));
				logAction(player, action, 0, majiangs, null);
				for (JzMjPlayer seat : seatMap.values()) {
					List<Integer> list = new ArrayList<>();
					if (seat.getUserId() != player.getUserId()) {
						list = seat.checkDisMajiang(majiangs.get(0));
						if (list.contains(1)) {
							// 如果杠了之后，別人出的牌不能做杠操作
							if (!seat.getGang().isEmpty()) {
//								list.set(JzMjAction.MINGGANG, 0);
//								list.set(JzMjAction.ANGANG, 0);
//								list.set(JzMjAction.BUZHANG, 0);
							}
							addActionSeat(seat.getSeat(), list);
							logChuPaiActList(seat, majiangs.get(0), list);
						}
					}
				}
				setDisEventAction(action);

				sendDisMajiangAction(builder);

				// 取消漏炮
				player.setPassMajiangVal(0);
				addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + JzMjHelper.toMajiangStrs(majiangs));
				setIsBegin(false);
				// 给下一家发牌
				checkMo();
				return;
			}
		}

		PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
		buildPlayRes(builder, player, action, majiangs);
		// 普通出牌
		clearActionSeatMap();
		clearGangActionMap();
		setNowDisCardSeat(calcNextSeat(player.getSeat()));
		recordDisMajiang(majiangs, player);//记录chupai
		player.addOutPais(majiangs, action, player.getSeat());
		player.clearPassHu();
		logAction(player, action, 0, majiangs, null);
		for (JzMjPlayer seat : seatMap.values()) {
			List<Integer> list = new ArrayList<>();
			if (seat.getUserId() != player.getUserId()) {
				list = seat.checkDisMajiang(majiangs.get(0));
				if (list.contains(1)) {
					// 如果杠了之后，別人出的牌不能做杠操作
					if (!seat.getGang().isEmpty()) {
//						list.set(JzMjAction.MINGGANG, 0);
//						list.set(JzMjAction.ANGANG, 0);
//						list.set(JzMjAction.BUZHANG, 0);
					}
					addActionSeat(seat.getSeat(), list);
					logChuPaiActList(seat, majiangs.get(0), list);
				}
			}
		}
		setDisEventAction(action);
		sendDisMajiangAction(builder);
		// 取消漏炮
		player.setPassMajiangVal(0);
		addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + JzMjHelper.toMajiangStrs(majiangs));
		setIsBegin(false);
		// 给下一家发牌
		checkMo();

	}

	private void chuPai3(JzMjPlayer player, List<JzMj> majiangs, int action) {
		if (majiangs.size() != 1) {
			return;
		}
//		if (!player.isAlreadyMoMajiang()) {
//			// 还没有摸牌
//			return;
//		}
		if (!tempActionMap.isEmpty() && player.getGang().isEmpty()) {
			LogUtil.e(player.getName() + "出牌清理临时操作！");
			clearTempAction();
		}
//		if (!player.getGang().isEmpty()) {
//			// 已经杠过了牌
//			if (player.getLastMoMajiang().getId() != majiangs.get(0).getId()) {
//				return;
//			}
//		}
//		if (!actionSeatMap.isEmpty() && player.getGang().isEmpty()) {// 出牌自动过掉手上操作
//			guo(player, null, JzMjDisAction.action_pass);
//		}
//		if (!actionSeatMap.isEmpty() && player.getGang().isEmpty()) {
//			player.writeComMessage(WebSocketMsgType.res_code_mj_dis_err, action, majiangs.get(0).getId());
//			return;
//		}
		PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
		buildPlayRes(builder, player, action, majiangs);
		// 普通出牌
		clearActionSeatMap();
		clearGangActionMap();
		setNowDisCardSeat(calcNextSeat(player.getSeat()));
		//recordDisMajiang(majiangs, player);
		//player.addOutPais(majiangs, action, player.getSeat());
		player.clearPassHu();
		logAction(player, action, 0, majiangs, null);
		for (JzMjPlayer seat : seatMap.values()) {
			List<Integer> list = new ArrayList<>();
			if (seat.getUserId() != player.getUserId()) {
				list = seat.checkDisMajiang(majiangs.get(0));
				if (list.contains(1)) {
					// 如果杠了之后，別人出的牌不能做杠操作
					if (!seat.getGang().isEmpty()) {
//						list.set(JzMjAction.MINGGANG, 0);
//						list.set(JzMjAction.ANGANG, 0);
//						list.set(JzMjAction.BUZHANG, 0);
					}

					addActionSeat(seat.getSeat(), list);
					logChuPaiActList(seat, majiangs.get(0), list);
				}
			}
		}
		setDisEventAction(action);
		sendDisMajiangAction(builder);
		// 取消漏炮
		player.setPassMajiangVal(0);
		addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + JzMjHelper.toMajiangStrs(majiangs));
		setIsBegin(false);
		// 给下一家发牌
		checkMo();
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

	public List<Integer> getHuSeatByActionMap() {
		List<Integer> huList = new ArrayList<>();
		for (int seat : actionSeatMap.keySet()) {
			List<Integer> actionList = actionSeatMap.get(seat);
			if (actionList.get(JzMjAction.HU) == 1 || actionList.get(JzMjAction.ZIMO) == 1) {
				// 胡
				huList.add(seat);
			}

		}
		return huList;
	}

	private void sendDisMajiangAction(PlayMajiangRes.Builder builder) {
		// 如果有人可以胡 优先胡
		// 把胡的找出来
		buildPlayRes1(builder);
		List<Integer> huList = getHuSeatByActionMap();
		if (huList.size() > 0) {
			// 有人胡,优先胡
			for (JzMjPlayer seatPlayer : seatMap.values()) {
				PlayMajiangRes.Builder copy = builder.clone();
				List<Integer> actionList;
				// 只推送给胡牌的人改成了推送给所有人但是必须等胡牌的人先答复
				if (actionSeatMap.containsKey(seatPlayer.getSeat())) {
					// if (huList.contains(seatPlayer.getSeat())) {
					actionList = actionSeatMap.get(seatPlayer.getSeat());
				} else {
					// 其他碰杠先无视
					actionList = new ArrayList<>();
				}
				copy.addAllSelfAct(actionList);
				seatPlayer.writeSocket(copy.build());
			}

		} else {
			// 没人胡，推送普通碰杠
			for (JzMjPlayer seat : seatMap.values()) {
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

	private void err(JzMjPlayer player, int action, String errMsg) {
		LogUtil.e("play:tableId-->" + id + " playerId-->" + player.getUserId() + " action-->" + action + " err:"
				+ errMsg);
	}

	/**
	 * 出牌
	 *
	 * @param player
	 * @param majiangs
	 * @param action
	 */
	public synchronized void playCommand(JzMjPlayer player, List<JzMj> majiangs, int action) {
		if (!moGangHuList.isEmpty()) {// 被人抢杠胡
			if (!moGangHuList.contains(player.getSeat())) {
				return;
			}
		}

		if (JzMjDisAction.action_hu == action) {
			hu(player, majiangs, action);
			return;
		}
		// 手上没有要出的麻将
		if (!isHasGangAction() && action != JzMjDisAction.action_minggang && action != JzMjDisAction.action_buzhang)
			if (!player.getHandMajiang().containsAll(majiangs)) {
				err(player, action, "没有找到出的牌" + majiangs);
				return;
			}
//		if(action != JzMjDisAction.action_pass){
			changeDisCardRound(1);
//		}
		if (action == JzMjDisAction.action_pass) {
			guo(player, majiangs, action);
		} else if (action == JzMjDisAction.action_moMjiang) {

		} else if (action != 0) {
			if (hasXiaoHu()) {
				return;
			}
			chiPengGang(player, majiangs, action);
		} else {
			if (isBegin() && hasXiaoHu()) {
				return;
			}
			chuPai(player, majiangs, action);
			if(gangSaveSeat>0 && player.getSeat()!=gangSaveSeat){
				gangSaveSeat = 0;
			}
		}
	}

	/**
	 * 最后一张牌(海底捞)
	 *
	 * @param player
	 * @param action
	 */
	public synchronized void moLastMajiang(JzMjPlayer player, int action) {
		if (getLeftMajiangCount() != 1) {
			return;
		}
		if (player.getSeat() != askLastMajaingSeat) {
			return;
		}

		if (action == JzMjDisAction.action_passmo) {
			// 发送下一个海底摸牌res
			sendMoLast(player, 0);
			removeMoLastSeat(player.getSeat());
			if (moLastSeats == null || moLastSeats.size() == 0) {
				calcOver();
				return;
			}
			sendAskLastMajiangRes(0);
			addPlayLog(disCardRound + "_" + player.getSeat() + "_" + JzMjDisAction.action_pass + "_");
		} else {
			sendMoLast(player, 0);
			clearMoLastSeat();
			clearActionSeatMap();
			setMoLastMajiangSeat(player.getSeat());
			JzMj majiang = getLeftMajiang();
			addPlayLog(disCardRound + "_" + player.getSeat() + "_" + JzMjDisAction.action_moLastMjiang + "_"
					+ majiang.getId());
			setMoMajiangSeat(player.getSeat());
			player.setPassMajiangVal(0);
			setLastMajiang(majiang);
			setDisCardSeat(player.getSeat());

			// /////////////////////////////////////////////
			// 发送海底捞的牌

			// /////////////////////////////////////////

			List<JzMj> disMajiangs = new ArrayList<>();
			disMajiangs.add(majiang);

			MoMajiangRes.Builder moRes = MoMajiangRes.newBuilder();
			moRes.setUserId(player.getUserId() + "");
			moRes.setRemain(getLeftMajiangCount());
			moRes.setSeat(player.getSeat());

			// 先看看自己能不能胡
			List<Integer> selfActList = player.checkDisMajiang(majiang);
			player.moMajiang(majiang);
			selfActList = JzMjAction.keepHu(selfActList);
			if (selfActList != null && !selfActList.isEmpty()) {
				if (selfActList.contains(1)) {
					addActionSeat(player.getSeat(), selfActList);
				}
			}
			for (JzMjPlayer seatPlayer : seatMap.values()) {
				if (seatPlayer.getUserId() == player.getUserId()) {
					MoMajiangRes.Builder selfMsg = moRes.clone();
					selfMsg.addAllSelfAct(selfActList);
					selfMsg.setMajiangId(majiang.getId());
					player.writeSocket(selfMsg.build());
				} else {
					MoMajiangRes.Builder otherMsg = moRes.clone();
					seatPlayer.writeSocket(otherMsg.build());
				}
			}

			// 自己能胡
			if (JzMjAction.hasHu(selfActList)) {
				// 优先自己胡
				// hu(player, null, JzMjDisAction.action_moLastMjiang_hu);
				return;
			} else {
				chuLastPai(player);
			}
			// for (int seat : actionSeatMap.keySet()) {
			// hu(seatMap.get(seat), null, action);
			// }
		}

	}

	private void chuLastPai(JzMjPlayer player) {
		JzMj majiang = lastMajiang;
		List<JzMj> disMajiangs = new ArrayList<>();
		disMajiangs.add(majiang);
		PlayMajiangRes.Builder chuRes = JzMjResTool.buildPlayRes(player, JzMjDisAction.action_chupai, disMajiangs);
		addPlayLog(disCardRound + "_" + player.getSeat() + "_" + JzMjDisAction.action_chupai + "_"
				+ JzMjHelper.toMajiangStrs(disMajiangs));
		setNowDisCardIds(disMajiangs);
		setNowDisCardSeat(calcNextSeat(player.getSeat()));
		recordDisMajiang(disMajiangs, player);
		player.addOutPais(disMajiangs, JzMjDisAction.action_chupai, player.getSeat());
		player.clearPassHu();
		for (JzMjPlayer seatPlayer : seatMap.values()) {
			if (seatPlayer.getUserId() == player.getUserId()) {
				seatPlayer.writeSocket(chuRes.clone().build());
				continue;
			}
			List<Integer> otherActList = seatPlayer.checkDisMajiang(majiang);
			otherActList = JzMjAction.keepHu(otherActList);
			PlayMajiangRes.Builder msg = chuRes.clone();
			if (JzMjAction.hasHu(otherActList)) {
				addActionSeat(seatPlayer.getSeat(), otherActList);
				msg.addAllSelfAct(otherActList);
			}
			seatPlayer.writeSocket(msg.build());
		}
		if (actionSeatMap.isEmpty()) {
			calcOver();
		}
	}

	private void passMoHu(JzMjPlayer player, List<JzMj> majiangs, int action) {
		if (!moGangHuList.contains(player.getSeat())) {
			return;
		}

		PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
		buildPlayRes(builder, player, action, majiangs);
		builder.setSeat(nowDisCardSeat);
		removeActionSeat(player.getSeat());
		player.writeSocket(builder.build());
		addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + JzMjHelper.toMajiangStrs(majiangs));
		if (isCalcOver()) {
			calcOver();
			return;
		}
		player.setPassMajiangVal(nowDisCardIds.get(0).getVal());

		JzMjPlayer moGangPlayer = seatMap.get(moMajiangSeat);
		if (moGangHuList.isEmpty()) {
			majiangs = new ArrayList<>();
			majiangs.add(moGang);
 			if (disEventAction == JzMjDisAction.action_buzhang) {
			moGangPlayer.addCpnum(1);//庄家第一次出牌。闲家直接杠 杠完出牌不算报听了
//			moMajiang(moGangPlayer, true);
 			}
 			else {
 				gangMoMajiang(moGangPlayer, majiangs.get(0), disEventAction);
 			}


		}

	}

	/**
	 * guo
	 *
	 * @param player
	 * @param majiangs
	 * @param action
	 */
	private void guo(JzMjPlayer player, List<JzMj> majiangs, int action) {
		if (!actionSeatMap.containsKey(player.getSeat())) {
			return;
		}
		if (!moGangHuList.isEmpty()) {
			// 有摸杠胡的优先处理
			passMoHu(player, majiangs, action);
			return;
		}
		  //限制过频率
        if (player.getLastPassTime() > 0 && System.currentTimeMillis() - player.getLastPassTime() <= 1500) {
        	logPassTime(player);
            return;
        }
        //过频率
        player.setLastPassTime(System.currentTimeMillis());
		if(table_tingpai==1){

			PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
			majiangs=  getNowDisCardIds();
			buildPlayRes(builder, player, action, majiangs);
			builder.setSeat(nowDisCardSeat);
			player.writeSocket(builder.build());
			addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + JzMjHelper.toMajiangStrs(majiangs));

			chuPai3(player,getNowDisCardIds(),JzMjDisAction.action_chupai);
			setTable_tingpai(0);
			return;
		}
		List<Integer> removeActionList = removeActionSeat(player.getSeat());
		int xiaoHu = JzMjAction.getFirstXiaoHu(removeActionList);
		logAction(player, action, xiaoHu, majiangs, removeActionList);
		boolean isBegin = isBegin();
		if (xiaoHu != -1) {
			player.addPassXiaoHu(xiaoHu);
			player.addPassXiaoHuList2(xiaoHu);
			List<Integer> actionList = player.checkMo(null, isBegin);
			if (!actionList.isEmpty()) {
				actionList.set(xiaoHu, 0);
				if (JzMjAction.getFirstXiaoHu(actionList) != -1) {
					// 过小胡后，还有小胡，直接提示小胡
					addActionSeat(player.getSeat(), actionList);
					PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
					buildPlayRes(builder, player, action, majiangs);
					builder.setSeat(nowDisCardSeat);
					builder.addAllSelfAct(actionList);
					player.writeSocket(builder.build());
					logAction(player, action, xiaoHu, majiangs, actionList);
					addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_"
							+ JzMjHelper.toMajiangStrs(majiangs));
					return;
				} else {
					addActionSeat(player.getSeat(), actionList);
				}
			}
		}

		if (moLastMajiangSeat == player.getSeat()) {
			// 摸海底可以胡的人点过，将海底牌打出
			chuLastPai(player);
			return;
		}
		checkClearGangDisMajiang();
		PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
		buildPlayRes(builder, player, action, majiangs);
		builder.setSeat(nowDisCardSeat);
		player.writeSocket(builder.build());
		addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + JzMjHelper.toMajiangStrs(majiangs));
		if (isCalcOver()) {
			calcOver();
			return;
		}
		if (JzMjAction.hasHu(removeActionList) && disCardSeat != player.getSeat() && nowDisCardIds.size() == 1) {
			// 漏炮
			player.passHu(nowDisCardIds.get(0).getVal());
		}

		// nowDisCardIds.size() == 1
		if (removeActionList.get(0) == 1 && disCardSeat != player.getSeat()) {
			if (nowDisCardIds.size() > 1) {
				for (JzMj mj : nowDisCardIds) {
					List<Integer> hu = player.checkDisMaj(mj, false);
					if (!hu.isEmpty() && hu.get(0) == 1) {
						player.setPassMajiangVal(mj.getVal());
						break;
					}
				}
			} else if (nowDisCardIds.size() == 1) {
				player.setPassMajiangVal(nowDisCardIds.get(0).getVal());
			}
		}
		if (!actionSeatMap.isEmpty()) {
			JzMjPlayer disCSMajiangPlayer = seatMap.get(disCardSeat);
			PlayMajiangRes.Builder disBuilder = PlayMajiangRes.newBuilder();
			buildPlayRes(disBuilder, disCSMajiangPlayer, 0, null);
			for (int seat : actionSeatMap.keySet()) {
				List<Integer> actionList = actionSeatMap.get(seat);
				PlayMajiangRes.Builder copy = disBuilder.clone();
				copy.addAllSelfAct(new ArrayList<>());
				if (actionList != null && !tempActionMap.containsKey(seat)) {
					if (actionList != null) {
						copy.addAllSelfAct(actionList);
					}
				}
				JzMjPlayer seatPlayer = seatMap.get(seat);
				seatPlayer.writeSocket(copy.build());
			}
		}
		// && tempActionMap.size()==0

//		if ((player.isAlreadyMoMajiang()||player.getGangCGangMjs().size()==2)
//				&& !player.getGang().isEmpty() && actionSeatMap.get(player.getSeat()) == null) {
//			// 杠牌后自动出牌
//			List<JzMj> disMjiang = new ArrayList<>();
//			//disMjiang.add(player.getLastMoMajiang());
//			if (player.getGangCGangMjs().size()>0 ) {
//				disMjiang.add(JzMj.getMajang(player.getGangCGangMjs().remove(0)));
//				if (player.getGangCGangMjs().size()>0 ) {
//					disMjiang.add(JzMj.getMajang(player.getGangCGangMjs().remove(0)));
//				}
//			}
//			if (isHasGangAction()||disMjiang.size()==2) {
//				checkMoOutCard(disMjiang, player, action);
//			} else {
//					chuPai(player, disMjiang, 0);
//			}
//		}

		 if (isBegin && xiaoHu == -1 && player.getSeat() == lastWinSeat ) {
			// 庄家过非小胡，提示庄家出牌
			 if(!actionSeatMap.isEmpty()){
			 	 //其他玩家有报听的情况。先让其他玩家动作
				 checkBegin(player);
			 }else{
				 ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.req_com_kt_ask_dismajiang);
				 player.writeSocket(com.build());
			 }
		} else {
			checkBegin(player);
		}

		if (player.isAlreadyMoMajiang()) {
			sendTingInfo(player);
		}

		// 先过 后执行临时可做操作里面优先级最高的玩家操作
		refreshTempAction(player);
		checkMo();
	}

	private void logPassTime(JzMjPlayer player) {
		StringBuilder sb = new StringBuilder();
		sb.append("JzMj");
		sb.append("|").append(getId());
		sb.append("|").append(getPlayBureau());
		sb.append("|").append(player.getUserId());
		sb.append("|").append(player.getSeat());
		sb.append("|").append("pass");
		sb.append("|").append(System.currentTimeMillis() - player.getLastPassTime());
		sb.append("|").append(actionSeatMap.get(player.getSeat()));
		LogUtil.msg(sb.toString());
	}

	//杠分计算
	private void calcPoint(JzMjPlayer player, int action, int sameCount, List<JzMj> majiangs) {
		if(gangBuF!=1) {
			return;
		}
		int lostPoint = 0;
		int getPoint = 0;

			//System.out.println("=====杠分计算===========================");
	 		if (action == JzMjDisAction.action_angang||action == JzMjDisAction.action_buzhang_an) {
			// 暗杠
			if(action == JzMjDisAction.action_buzhang_an){
				player.changeAction(8,1);//8=暗杠补张
			}
			if(action == JzMjDisAction.action_angang){
				player.changeAction(3,1);//3=暗杠
			}

			if(player.getSeat() == lastWinSeat && isXianShangZhuang == 1){
				//闲上庄暗杠 每人6分。
				//System.out.println("==============闲上庄暗杠 每人6分。==");
				lostPoint = -6;
				getPoint = 6*(maxPlayerCount-1);
				countGangPoint(player,getPoint,lostPoint);
				// xjs GangPoint
				for (JzMjPlayer p : seatMap.values()) {
					if(p.getSeat()==player.getSeat()){
						p.addXjsAnGangPoint(getPoint);
					}else{
						p.addXjsAnGangPoint(lostPoint);
					}
				}

			}else if(player.getSeat() == lastWinSeat && isXianShangZhuang == 0){
				//庄  每人4分。
				//System.out.println("==============庄  暗杠 每人4分。==");
				lostPoint = -4;
				getPoint = 4*(maxPlayerCount-1);
				countGangPoint(player,getPoint,lostPoint);
				for (JzMjPlayer p : seatMap.values()) {
					if(p.getSeat()==player.getSeat()){
						p.addXjsAnGangPoint(getPoint);
					}else{
						p.addXjsAnGangPoint(lostPoint);
					}
				}
			}else{
				//闲家 庄家4分，闲上庄6分，闲家2分
				if(isXianShangZhuang==1){
					getPoint = 6+2*(maxPlayerCount-2);
					//System.out.println("==============闲  暗杠  有闲上庄  6+2*(maxPlayerCount-2) getPoint=="+getPoint);
				}else if(isXianShangZhuang==0){
					getPoint = 4+2*(maxPlayerCount-2);
					//System.out.println("==============闲  暗杠  无闲上庄 4+2*(maxPlayerCount-2) getPoint=="+getPoint);
				}
				for (JzMjPlayer p : seatMap.values()) {
					if (p.getSeat() == lastWinSeat && isXianShangZhuang ==1) {
						p.changeGangPoint(-6);
						p.addXjsAnGangPoint(-6);
					} else if (p.getSeat() == lastWinSeat && isXianShangZhuang ==0) {
						p.changeGangPoint(-4);
						p.addXjsAnGangPoint(-4);
					}else if(p.getSeat() == player.getSeat()){
						p.changeGangPoint(getPoint);
						p.addXjsAnGangPoint(getPoint);
					}else{
						p.changeGangPoint(-2);
						p.addXjsAnGangPoint(-2);
					}
				}
			}
		}
		else if (action == JzMjDisAction.action_minggang  ||action == JzMjDisAction.action_buzhang) {
			player.changeAction(2,1);//2=明杠
		//	if (sameCount == 1) {
////				// 碰牌之后再抓一个牌每人出1分
////				// 放杠的人出3分
//				if (player.isPassGang(majiangs.get(0))) {
//					// 特殊处理 可以碰可以杠的牌 选择了碰 再杠不算分
//					return;
//				}
		//	} else if (sameCount == 3)
			{
				// 放杠
				   JzMjPlayer fgPlayer = seatMap.get(disCardSeat);
				   int gangSeat = player.getSeat();
				   int fgSeat = fgPlayer.getSeat();
				   if(fgPlayer.getSeat()!=player.getSeat()){
					   //接杠
					  if(fgPlayer.getSeat()!=lastWinSeat && player.getSeat()!=lastWinSeat){
					  		//闲家接闲家杠：2分
						  //System.out.println("=========放杠=====闲家接闲家杠2分 "+fgPlayer.getName()+"-2   |  "+player.getName()+" +2 ");
						  fgPlayer.changeGangPoint(-2);
						  player.changeGangPoint(2);

						  fgPlayer.addXjsZhiGangPoint(-2);
						  player.addXjsZhiGangPoint(2);
					  }else if((lastWinSeat == player.getSeat() && fgPlayer.getSeat()!=lastWinSeat)
							  ||(lastWinSeat != player.getSeat() && fgPlayer.getSeat()==lastWinSeat)
							  && isXianShangZhuang==0){
						 //庄家接闲家杠或闲家接庄家杠：4分
						  //System.out.println("=========放杠=====庄家接闲家杠或闲家接庄家杠：4分"+fgPlayer.getName()+"-2   |  "+player.getName()+" +2 ");
						  fgPlayer.changeGangPoint(-4);
						  player.changeGangPoint(4);
						  fgPlayer.addXjsZhiGangPoint(-4);
						  player.addXjsZhiGangPoint(4);
					  }else if((lastWinSeat == player.getSeat() && fgPlayer.getSeat()!=lastWinSeat)
							 	||(lastWinSeat != player.getSeat() && fgPlayer.getSeat()==lastWinSeat)
							  && isXianShangZhuang==1){
						  //闲上庄接闲家或者闲家接闲上庄：6分
						  //System.out.println("=========放杠=====庄家接闲家杠或闲家接庄家杠：6分"+fgPlayer.getName()+"-2   |  "+player.getName()+" +2 ");
						  fgPlayer.changeGangPoint(-6);
						  player.changeGangPoint(6);
						  fgPlayer.addXjsZhiGangPoint(-6);
						  player.addXjsZhiGangPoint(6);
					  }
				   }else{
				   		//明杠
						if(player.getSeat()!=lastWinSeat){
							//闲家明杠：庄家2分，闲上庄3分，闲家1分
							if(isXianShangZhuang==1){
								getPoint = 3+1*(maxPlayerCount-2);
							}else{
								getPoint = 2+1*(maxPlayerCount-2);
							}
							for (JzMjPlayer p : seatMap.values()) {
							 	if(isXianShangZhuang==1 && p.getSeat() == lastWinSeat){
							 		p.changeGangPoint(-3);//，闲上庄3分
									p.addXjsMingGangPoint(-3);
									//System.out.println("=========闲明杠：，闲上庄扣3分： =");
								}else if(isXianShangZhuang==0 && p.getSeat() == lastWinSeat){
									p.changeGangPoint(-2);//，庄家2分
									p.addXjsMingGangPoint(-2);
									//System.out.println("=========闲明杠： 庄扣2分： =");
								}else if(p.getSeat() == player.getSeat()){
							 		p.changeGangPoint(getPoint);
							 	 	p.addXjsMingGangPoint(getPoint);
							 		//System.out.println("=========闲明杠： 闲得分"+getPoint);
								}else{
							 		p.changeGangPoint(-1);
									p.addXjsMingGangPoint(-1);
							 		//System.out.println("=========闲明杠： 闲扣1分： =");
								}
							}
						}else if(player.getSeat() == lastWinSeat && isXianShangZhuang==1){
							//闲上庄明杠：每家3分
							//System.out.println("=========/闲上庄明杠：每家3分=");
							getPoint = 3*(maxPlayerCount-1);
							lostPoint =-3;
							countGangPoint(player,getPoint,lostPoint);
							for (JzMjPlayer p : seatMap.values()) {
								if(p.getSeat()==player.getSeat()){
									p.addXjsMingGangPoint(getPoint);
								}else{
									p.addXjsMingGangPoint(lostPoint);
								}
							}
						}else if(player.getSeat() == lastWinSeat && isXianShangZhuang==0){
							//庄家明杠：每家2分
							getPoint = 2*(maxPlayerCount-1);//System.out.println("=========/庄家明杠：每家2分： =");
							lostPoint =-2;
							countGangPoint(player,getPoint,lostPoint);
							for (JzMjPlayer p : seatMap.values()) {
								if(p.getSeat()==player.getSeat()){
									p.addXjsMingGangPoint(getPoint);
								}else{
									p.addXjsMingGangPoint(lostPoint);
								}
							}
						}
				   }
			}
			//
		}

//		if (lostPoint != 0) {
//			for (JzMjPlayer seat : seatMap.values()) {
//				if (seat.getUserId() == player.getUserId()) {
//					player.changeGangPoint(getPoint);
//				} else {
//					seat.changeGangPoint(lostPoint);
//				}
//			}
//		}

	}
	private void countGangPoint(JzMjPlayer player ,int getPoint,int lostPoint){
		for (JzMjPlayer seat : seatMap.values()) {
			if (seat.getUserId() == player.getUserId()) {
				player.changeGangPoint(getPoint);
			} else {
				seat.changeGangPoint(lostPoint);
			}
		}
	}
	private void calcXiaoHuPoint(JzMjPlayer winPlayer, int xiaoIndex) {
		int totalWinPoint =0;
		for (int loserSeat : seatMap.keySet()) {
			// 除了赢家的其他人
			if (winPlayer.getSeat() == loserSeat) {
				continue;
			}
			JzMjPlayer loser = seatMap.get(loserSeat);
			int losePoint = 0;
			if(winPlayer.getSeat() != lastWinSeat){
				if(loser.getSeat()==lastWinSeat && isXianShangZhuang==1){
					losePoint = 4+2; //System.out.println("==========闲 板板胡或 缺一色===============闲上庄 扣6分=======");
				}else if(loser.getSeat()==lastWinSeat && isXianShangZhuang==0){
					losePoint = 4;//System.out.println("==========闲小胡=板板胡或 缺一色============== 庄扣 6分=======");
				}else{
					losePoint = 1;//System.out.println("==========闲 小胡==板板胡或 缺一色=============闲 扣 6分=======");
				}
			}else if(winPlayer.getSeat() == lastWinSeat && isXianShangZhuang == 0){
				//庄家自摸：每人4分
				losePoint = 4;//System.out.println("========庄家==小胡====板板胡或 缺一色=========== 庄自摸 每人扣 4分=======");
			}else if(winPlayer.getSeat() == lastWinSeat && isXianShangZhuang == 1){
				// 闲上庄自摸：每人6分
				losePoint = 6;//System.out.println("======闲上庄====小胡======板板胡或 缺一色=========闲上庄 每人扣 6分=======");
			}
			totalWinPoint += losePoint;
			loser.changeLostPoint(-losePoint);

		}
		winPlayer.changeLostPoint(totalWinPoint);
	}

	private void recordDisMajiang(List<JzMj> majiangs, JzMjPlayer player) {
		setNowDisCardIds(majiangs);
		setDisCardSeat(player.getSeat());
	}

	public List<JzMj> getNowDisCardIds() {
		return nowDisCardIds;
	}

	public void setDisEventAction(int disAction) {
		this.disEventAction = disAction;
		changeExtend();
	}

	public void setNowDisCardIds(List<JzMj> nowDisCardIds) {
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
				JzMjPlayer player = seatMap.get(seat);
				if (player != null && player.isRobot()) {
					// 如果是机器人可以直接决定
					List<Integer> actionList = actionSeatMap.get(seat);
					if (actionList == null) {
						continue;
					}
					List<JzMj> list = new ArrayList<>();
					if (!nowDisCardIds.isEmpty()) {
						list = JzMjQipaiTool.getVal(player.getHandMajiang(), nowDisCardIds.get(0).getVal());
					}
					if (actionList.get(0) == 1) {
						// 胡
						playCommand(player, new ArrayList<JzMj>(), JzMjDisAction.action_hu);

					} else if (actionList.get(3) == 1) {
						playCommand(player, list, JzMjDisAction.action_angang);

					} else if (actionList.get(2) == 1) {
						playCommand(player, list, JzMjDisAction.action_minggang);

					} else if (actionList.get(1) == 1) {
						playCommand(player, list, JzMjDisAction.action_peng);

					} else if (actionList.get(4) == 1) {
						playCommand(player, player.getCanChiMajiangs(nowDisCardIds.get(0)), JzMjDisAction.action_chi);

					} else {
						//System.out.println("---------->" + JacksonUtil.writeValueAsString(actionList));
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
			// for (JzMjPlayer player : seatMap.values()) {
			// if (player.isRobot() && player.canXiaoHu()) {
			// playCommand(player, new ArrayList<JzMj>(),
			// JzMjDisAction.action_xiaohu);
			// }
			// }

			int nextseat = getNextActionSeat();
			JzMjPlayer next = seatMap.get(nextseat);
			if (next != null && next.isRobot()) {
				List<Integer> actionList = actionSeatMap.get(next.getSeat());
				int xiaoHuAction = -1;
				if (actionList != null) {
					List<JzMj> list = null;
					if (actionList.get(0) == 1) {
						// 胡
						playCommand(next, new ArrayList<JzMj>(), JzMjDisAction.action_hu);

					} else if ((xiaoHuAction = JzMjAction.getFirstXiaoHu(actionList)) > 0) {

						playCommand(next, new ArrayList<JzMj>(), JzMjDisAction.action_pass);

					} else if (actionList.get(3) == 1) {
						// 机器人暗杠
						Map<Integer, Integer> handMap = JzMjHelper.toMajiangValMap(next.getHandMajiang());
						for (Entry<Integer, Integer> entry : handMap.entrySet()) {
							if (entry.getValue() == 4) {
								// 可以暗杠
								list = JzMjHelper.getMajiangList(next.getHandMajiang(), entry.getKey());
							}
						}
						playCommand(next, list, JzMjDisAction.action_angang);

					} else if (actionList.get(5) == 1) {
						// 机器人补张
						Map<Integer, Integer> handMap = JzMjHelper.toMajiangValMap(next.getHandMajiang());
						for (Entry<Integer, Integer> entry : handMap.entrySet()) {
							if (entry.getValue() == 4) {
								// 可以补张
								list = JzMjHelper.getMajiangList(next.getHandMajiang(), entry.getKey());
							}
						}
						if (list == null) {
							if (next.isAlreadyMoMajiang()) {
								list = JzMjQipaiTool.getVal(next.getHandMajiang(), next.getLastMoMajiang().getVal());

							} else {
								list = JzMjQipaiTool.getVal(next.getHandMajiang(), nowDisCardIds.get(0).getVal());
								list.add(nowDisCardIds.get(0));
							}
						}

						playCommand(next, list, JzMjDisAction.action_buzhang);

					} else if (actionList.get(2) == 1) {
						Map<Integer, Integer> pengMap = JzMjHelper.toMajiangValMap(next.getPeng());
						for (JzMj handMajiang : next.getHandMajiang()) {
							if (pengMap.containsKey(handMajiang.getVal())) {
								// 有碰过
								list = new ArrayList<>();
								list.add(handMajiang);
								playCommand(next, list, JzMjDisAction.action_minggang);
								break;
							}
						}

					} else if (actionList.get(1) == 1) {
						// playCommand(next, list, JzMjDisAction.action_peng);

					} else if (actionList.get(4) == 1) {
						JzMj majiang = null;
						List<JzMj> chiList = null;
						if (nowDisCardIds.size() == 1) {
							majiang = nowDisCardIds.get(0);
							chiList = next.getCanChiMajiangs(majiang);
						} else {
							for (int majiangId : gangSeatMap.keySet()) {
								Map<Integer, List<Integer>> actionMap = gangSeatMap.get(majiangId);
								List<Integer> action = actionMap.get(next.getSeat());
								if (action != null) {
									// List<Integer> disActionList =
									// MajiangDisAction.parseToDisActionList(action);
									if (action.get(4) == 1) {
										majiang = JzMj.getMajang(majiangId);
										chiList = next.getCanChiMajiangs(majiang);
										chiList.add(majiang);
										break;
									}

								}

							}

						}

						playCommand(next, chiList, JzMjDisAction.action_chi);

					} else {
						//System.out.println("!!!!!!!!!!" + JacksonUtil.writeValueAsString(actionList));

					}

				} else {
					int maJiangId = JzMjRobotAI.getInstance().outPaiHandle(0, next.getHandPais(),
							new ArrayList<Integer>());
					List<JzMj> majiangList = JzMjHelper.toMajiang(Arrays.asList(maJiangId));
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
			setIsXianShangZhuang(0);
		}

		setDisCardSeat(lastWinSeat);
		setNowDisCardSeat(lastWinSeat);
		setMoMajiangSeat(lastWinSeat);

		List<Integer> copy = null;
		copy = new ArrayList<>(JzMjConstants.zhuanzhuan_mjList);
		Collections.shuffle(copy);
		if(null==zp){
			if (maxPlayerCount == 2 && choupai>0) {
				copy = copy.subList(0,copy.size()-choupai);
			}
		}
		addPlayLog(copy.size() + "");
		List<List<JzMj>> list;
		if (zp == null) {
			list = JzMjTool.fapai(copy, getMaxPlayerCount());
		} else {
			list = JzMjTool.fapai(copy, getMaxPlayerCount(), zp);
		}
		int i = 1;
		List<Integer> removeIndex = new ArrayList<>();
		setTable_tingpai(0);
		for (JzMjPlayer player : playerMap.values()) {
			player.changeState(player_state.play);

			if (player.getSeat() == lastWinSeat) {
				player.dealHandPais(list.get(0));
				removeIndex.add(0);
				continue;
			}
			player.dealHandPais(list.get(i));
			removeIndex.add(i);
			i++;
		}
		for (JzMjPlayer player : playerMap.values()) {
			if (player.getSeat() == lastWinSeat) {
				firstMomj = player.getHandMajiang().get(13).getId();
				//System.out.println(player.getHandMajiang().get(13)+"++++++++++++++++++++++==firstMomj="+firstMomj);
				break;
			}
		}

		// 桌上剩余的牌
		List<JzMj> leftMjs = new ArrayList<>();
		// 没有发出去的牌退回剩余牌中
		for (int j = 0; j < list.size(); j++) {
			if (!removeIndex.contains(j)) {
				leftMjs.addAll(list.get(j));
			}
		}

		/// 调牌 设置剩余牌长度
		if(null!=zp){
			int zp_setLeftLengthIndex = maxPlayerCount+1;
			if(zp.size()>zp_setLeftLengthIndex){
				int subLeftMaJiangsLengthIndex = zp.get(zp_setLeftLengthIndex).get(0);
				setLeftMajiangs(leftMjs.subList(0,subLeftMaJiangsLengthIndex));
			}else{
				setLeftMajiangs(leftMjs);
			}
		}else{
			setLeftMajiangs(leftMjs);
		}
	}

	/**
	 * 初始化桌子上剩余牌
	 *
	 * @param leftMajiangs
	 */
	public void setLeftMajiangs(List<JzMj> leftMajiangs) {
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
	public JzMj getLeftMajiang() {
		if (this.leftMajiangs.size() > 0) {
			JzMj majiang = this.leftMajiangs.remove(0);
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
		// return 1;
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
			for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
				if (seat == 0) {
					seat = entry.getKey();
				}
				if (entry.getValue().get(0) == 1) {// 胡
					return entry.getKey();
				}
				if (entry.getValue().get(2) == 1) {// 杠
					return entry.getKey();
				}
				if (entry.getValue().get(1) == 1) {// 碰
					return entry.getKey();
				}
				if (entry.getValue().get(4) == 1) {// 吃
					return entry.getKey();
				}
			}
			return seat;
		}
	}

	//
	// private int getNearSeat(int nowSeat, List<Integer> seatList) {
	// if (seatList.contains(nowSeat)) {
	// // 出牌离自己是最近的
	// return nowSeat;
	// }
	// for (int i = 0; i < 3; i++) {
	// int seat = calcNextSeat(nowSeat);
	// if (seatList.contains(seat)) {
	// return seat;
	// }
	// nowSeat = seat;
	// }
	// return 0;
	// }

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
		res.setLastWinSeat(lastWinSeat);
		res.setMasterId(masterId + "");
		res.addExt(payType); // 0
		res.addExt(getConifg(0)); // 1
		res.addExt(calcBird); // 2
		res.addExt(birdNum); // 3
		res.addExt(gpsWarn); // 4
		res.addExt(0); // 5 queYiSe
		res.addExt(banbanHu); // 6 banbanHu
		res.addExt(0); // 7 yiZhiHua
		res.addExt(0); // 8 liuliuShun
		res.addExt(0); // 9 daSiXi
		res.addExt(0); // 10 jinTongYuNu
		res.addExt(0); // 11 jieJieGao
		res.addExt(0); // 12 sanTong
 		res.addExt(0); // 13 zhongTuLiuLiuShun
 		res.addExt(0); // 14 zhongTuSiXi
	 	res.addExt(0); // 15 kePiao
		res.addExt(isCalcBanker); // 16
		res.addExt(isBegin() ? 1 : 0); // 17
		//System.out.println(" isbegin ====== " + isBegin());

		res.addStrExt(StringUtil.implode(moTailPai, ",")); // 0
		res.setDealDice(dealDice);
		res.setRenshu(getMaxPlayerCount());
		if (leftMajiangs != null) {
			res.setRemain(leftMajiangs.size());
		} else {
			res.setRemain(0);
		}
		List<PlayerInTableRes> players = new ArrayList<>();
		for (JzMjPlayer player : playerMap.values()) {
			PlayerInTableRes.Builder playerRes = player.buildPlayInTableInfo(isrecover);
			if (player.getUserId() == userId) {
				playerRes.addAllHandCardIds(player.getHandPais());
			}

			if (showMjSeat.contains(player.getSeat()) && player.getHuXiaohu().size() > 0) {
				List<Integer> ids = JzMjHelper.toMajiangIds(player.showXiaoHuMajiangs(player.getHuXiaohu().get(player.getHuXiaohu().size() - 1), true));
				if (ids != null) {
					if (player.getUserId() == userId) {
						playerRes.addAllIntExts(ids);
					} else {
						playerRes.addAllHandCardIds(ids);
					}

				}
			}
			if (player.getSeat() == disCardSeat && nowDisCardIds != null) {
				playerRes.addAllOutCardIds(JzMjHelper.toMajiangIds(nowDisCardIds));
			}
			playerRes.addRecover(player.getSeat() == lastWinSeat ? 1 : 0);
			if (!isHasGangAction(player.getSeat()) && actionSeatMap.containsKey(player.getSeat())
					&& !huConfirmMap.containsKey(player.getSeat())) {
				if (!tempActionMap.containsKey(player.getSeat())) {// 如果已做临时操作
																	// 则不发送前端可做的操作
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
		setAskLastMajaingSeat(0);
		setFristLastMajiangSeat(0);
		setMoLastMajiangSeat(0);
		setDisEventAction(0);
		setLastMajiang(null);
		clearTempAction();
		clearShowMjSeat();
		clearMoLastSeat();
		setDealDice(0);
		clearMoTailPai();
		readyTime = 0;
		firstMomj=0;
		setTable_tingpai(0);
	}

	@Override
	public void createTable(Player player, int play, int bureauCount, Object... objects) throws Exception {
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
		info.setConfig(objects[1].toString());
		TableDao.getInstance().save(info);
		loadFromDB(info);
		// int birdNum = (int) objects[0];
		// if (birdNum > 2) {
		// birdNum = 2;
		// }
		setBirdNum(2);
		// setIsCalcBanker((int) objects[2]);
		// setCalcBird((int) objects[3]);
		setIsCalcBanker(1);
		setCalcBird(2);
	}

	private Map<Integer, JzMjTempAction> loadTempActionMap(String json) {
		Map<Integer, JzMjTempAction> map = new ConcurrentHashMap<>();
		if (json == null || json.isEmpty())
			return map;
		JSONArray jsonArray = JSONArray.parseArray(json);
		for (Object val : jsonArray) {
			String str = val.toString();
			JzMjTempAction tempAction = new JzMjTempAction();
			tempAction.initData(str);
			map.put(tempAction.getSeat(), tempAction);
		}
		return map;
	}

	private void clearTempAction() {
		tempActionMap.clear();
		changeExtend();
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
		if (!actionlist.contains(1)) {
			LogUtil.msgLog.error("add actionSeat zero: coverAddActionSeat");
			return;
		}
		actionSeatMap.put(seat, actionlist);
		addPlayLog(disCardRound + "_" + seat + "_" + JzMjDisAction.action_hasAction + "_"
				+ StringUtil.implode(actionlist));
		saveActionSeatMap();
	}

	public void addActionSeat(int seat, List<Integer> actionlist) {
		// 没有操作就不加入
		if (!actionlist.contains(1)) {
			return;
		}
		if (actionSeatMap.containsKey(seat)) {
			List<Integer> a = actionSeatMap.get(seat);
			DataMapUtil.appendList(a, actionlist);
			addPlayLog(disCardRound + "_" + seat + "_" + JzMjDisAction.action_hasAction + "_" + StringUtil.implode(a));
		} else {
			actionSeatMap.put(seat, actionlist);
			addPlayLog(disCardRound + "_" + seat + "_" + JzMjDisAction.action_hasAction + "_"
					+ StringUtil.implode(actionlist));
		}
		saveActionSeatMap();
	}

	public void clearActionSeatMap() {
		if (!actionSeatMap.isEmpty()) {
			actionSeatMap.clear();
			saveActionSeatMap();
		}
	}

	/**
	 * 是否有人可胡小胡
	 *
	 * @return
	 */
	public boolean canHuXiaohu() {
		for (List<Integer> list : actionSeatMap.values()) {
			List<Integer> xiaoHuActions = list.subList(6, 14);
			if (xiaoHuActions.contains(1)) {
				return true;
			}
		}
		return false;
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
			nowDisCardIds = JzMjHelper.toMajiang(StringUtil.explodeToIntList(info.getNowDisCardIds()));
		}

		if (!StringUtils.isBlank(info.getLeftPais())) {
			leftMajiangs = JzMjHelper.toMajiang(StringUtil.explodeToIntList(info.getLeftPais()));
		}

	}

	// @Override
	// public void initExtend(String info) {
	// if (StringUtils.isBlank(info)) {
	// return;
	// }
	// JsonWrapper wrapper = new JsonWrapper(info);
	// for (JzMjPlayer player : seatMap.values()) {
	// player.initExtend(wrapper.getString(player.getSeat()));
	// }
	// String huListstr = wrapper.getString(5);
	// if (!StringUtils.isBlank(huListstr)) {
	// huConfirmMap = DataMapUtil.implode(huListstr);
	// }
	// birdNum = wrapper.getInt(6, 0);
	// moMajiangSeat = wrapper.getInt(7, 0);
	// int moGangMajiangId = wrapper.getInt(8, 0);
	// if (moGangMajiangId != 0) {
	// moGang = Majiang.getMajang(moGangMajiangId);
	// }
	// String moGangHu = wrapper.getString(9);
	// if (!StringUtils.isBlank(moGangHu)) {
	// moGangHuList = StringUtil.explodeToIntList(moGangHu);
	// }
	// String gangDisMajiangstr = wrapper.getString(10);
	// if (!StringUtils.isBlank(gangDisMajiangstr)) {
	// gangDisMajiangs = MajiangHelper.explodeMajiang(gangDisMajiangstr, ",");
	// }
	// int gangMajiang = wrapper.getInt(11, 0);
	// if (gangMajiang != 0) {
	// this.gangMajiang = Majiang.getMajang(gangMajiang);
	// }
	//
	// askLastMajaingSeat = wrapper.getInt(12, 0);
	// moLastMajiangSeat = wrapper.getInt(13, 0);
	// int lastMajiangId = wrapper.getInt(14, 0);
	// if (lastMajiangId != 0) {
	// this.lastMajiang = Majiang.getMajang(lastMajiangId);
	// }
	// fristLastMajiangSeat = wrapper.getInt(15, 0);
	// disEventAction = wrapper.getInt(16, 0);
	// isCalcBanker = wrapper.getInt(17, 1);
	// calcBird = wrapper.getInt(18, 1);
	// // disAction = wrapper.getInt(11, 0);
	// // wrapper.putInt(17, isCalcBanker);
	// // wrapper.putInt(18, calcBird);
	//
	// }

	/**
	 * 是否能碰
	 *
	 * @param player
	 * @param majiangs
	 * @param disMajiang
	 * @return
	 */
	private boolean canChi(JzMjPlayer player, List<JzMj> handMajiang, List<JzMj> majiangs, JzMj disMajiang) {
		if (!actionSeatMap.containsKey(player.getSeat())) {
			return false;
		}

//		if (maxPlayerCount == 2 && buChi == 1) {
//			return false;
//		}

		if (player.isAlreadyMoMajiang()) {
			return false;
		}
		List<Integer> pengGangSeatList = getPengGangSeatByActionMap();
		pengGangSeatList.remove((Object) player.getSeat());
		if (!pengGangSeatList.isEmpty()) {
			return false;
		}
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

		List<JzMj> chi = JzMjTool.checkChi(majiangs, disMajiang);
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
	private boolean canPeng(JzMjPlayer player, List<JzMj> majiangs, int sameCount, JzMj disMajiang) {
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
	private boolean canAnGang(JzMjPlayer player, List<JzMj> majiangs, int sameCount, int action) {
		if (sameCount != 4) {
			return false;
		}
		if (player.getSeat() != getNextDisCardSeat() && action != JzMjDisAction.action_buzhang) {
			return false;
		}
		if (player.getSeat() != getNextDisCardSeat() && action != JzMjDisAction.action_buzhang_an) {
			return false;
		}
		return true;
	}

	/**
	 * 检查优先度 如果同时出现一个事件，按出牌座位顺序优先
	 */
	private boolean checkAction(JzMjPlayer player, List<JzMj> cardList, List<Integer> hucards, int action) {
		boolean canAction = checkCanAction(player, action);// 是否优先级最高 能执行操作
		if (canAction == false) {// 不能操作时 存入临时操作
			int seat = player.getSeat();
			tempActionMap.put(seat, new JzMjTempAction(seat, action, cardList, hucards));
			// 玩家都已选择自己的临时操作后 选取优先级最高
			if (tempActionMap.size() == actionSeatMap.size()) {
				int maxAction = Integer.MAX_VALUE;
				int maxSeat = 0;
				Map<Integer, Integer> prioritySeats = new HashMap<>();
				int maxActionSize = 0;
				for (JzMjTempAction temp : tempActionMap.values()) {
					int prioAction = JzMjDisAction.getPriorityAction(temp.getAction());
					int prioAction2 = JzMjDisAction.getPriorityAction(maxAction);
					if (prioAction < prioAction2) {
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
				JzMjPlayer tempPlayer = seatMap.get(maxSeat);
				List<JzMj> tempCardList = tempActionMap.get(maxSeat).getCardList();
				for (int removeSeat : prioritySeats.keySet()) {
					if (removeSeat != maxSeat) {
						removeActionSeat(removeSeat);
					}
				}
				clearTempAction();
				playCommand(tempPlayer, tempCardList, maxAction);// 系统选取优先级最高操作
			} else {
				if (isCalcOver()) {// 判断是否牌局是否结束
					calcOver();
					return canAction;
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
	private void refreshTempAction(JzMjPlayer player) {
		tempActionMap.remove(player.getSeat());
		Map<Integer, Integer> prioritySeats = new HashMap<>();
		for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
			int seat = entry.getKey();
			List<Integer> actionList = entry.getValue();
			List<Integer> list = JzMjDisAction.parseToDisActionList(actionList);
			int priorityAction = JzMjDisAction.getMaxPriorityAction(list);
			prioritySeats.put(seat, priorityAction);
		}
		int maxPriorityAction = Integer.MAX_VALUE;
		int maxPrioritySeat = 0;
		boolean isSame = true;
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
		Iterator<JzMjTempAction> iterator = tempActionMap.values().iterator();
		while (iterator.hasNext()) {
			JzMjTempAction tempAction = iterator.next();
			if (tempAction.getSeat() == maxPrioritySeat) {
				int action = tempAction.getAction();
				List<JzMj> tempCardList = tempAction.getCardList();
				JzMjPlayer tempPlayer = seatMap.get(tempAction.getSeat());
				playCommand(tempPlayer, tempCardList, action);// 系统选取优先级最高操作
				iterator.remove();
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
	public boolean checkCanAction(JzMjPlayer player, int action) {
		// 优先度为胡杠补碰吃
		List<Integer> stopActionList = JzMjDisAction.findPriorityAction(action);
		for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
			if (player.getSeat() != entry.getKey()) {
				// 别人
				boolean can = JzMjDisAction.canDisMajiang(stopActionList, entry.getValue());
				if (!can) {
					return false;
				}
				List<Integer> disActionList = JzMjDisAction.parseToDisActionList(entry.getValue());
				if (disActionList.contains(action)) {
					// 同时拥有同一个事件 根据座位号来判断
					int actionSeat = entry.getKey();
					int nearFirstSeat =nowDisCardSeat;
					if (isHasGangAction(player.getSeat())) {
						nearFirstSeat = disCardSeat;
					}
					int nearSeat = getNearSeat(nearFirstSeat, Arrays.asList(player.getSeat(), actionSeat));
					if (nearSeat != player.getSeat()) {
						return false;
					}

				}

			}

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
	private boolean canMingGang(JzMjPlayer player, List<JzMj> handMajiang, List<JzMj> majiangs, int sameCount,
                                JzMj disMajiang) {
		List<Integer> pengList = JzMjHelper.toMajiangVals(player.getPeng());

		if (majiangs.size() == 1) {
			if (!isHasGangAction() && player.getSeat() != getNextDisCardSeat()) {
				return false;
			}
			if (handMajiang.containsAll(majiangs) && pengList.contains(majiangs.get(0).getVal())) {
				return true;
			}
			if (pengList.contains(disMajiang.getVal())) {
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

//	public int getKePiao() {
//		return kePiao;
//	}
//
//	public void setKePiao(int kePiao) {
//		this.kePiao = kePiao;
//		changeExtend();
//	}

	public void setMoMajiangSeat(int moMajiangSeat) {
		this.moMajiangSeat = moMajiangSeat;
		changeExtend();
	}

	public void setAskLastMajaingSeat(int askLastMajaingSeat) {
		this.askLastMajaingSeat = askLastMajaingSeat;
		changeExtend();
	}

	public void setFristLastMajiangSeat(int fristLastMajiangSeat) {
		this.fristLastMajiangSeat = fristLastMajiangSeat;
		changeExtend();
	}

	public void setLastMajiang(JzMj lastMajiang) {
		this.lastMajiang = lastMajiang;
		changeExtend();
	}

	public void setMoLastMajiangSeat(int moLastMajiangSeat) {
		this.moLastMajiangSeat = moLastMajiangSeat;
		changeExtend();
	}

	public void setGangMajiang(JzMj gangMajiang) {
		this.gangMajiang = gangMajiang;
		changeExtend();
	}

	/**
	 * 摸杠别人可以胡
	 *
	 * @param moGang
	 *            杠的牌
	 * @param moGangHuList
	 *            可以胡的人的座位list
	 */
	public void setMoGang(JzMj moGang, List<Integer> moGangHuList) {
		this.moGang = moGang;
		this.moGangHuList = moGangHuList;
		changeExtend();
	}

	/**
	 * 清除摸刚胡
	 */
	public void clearMoGang() {
		this.moGang = null;
		this.moGangHuList.clear();
		changeExtend();
	}

	public void setGangDisMajiangs(List<JzMj> gangDisMajiangs) {
		this.gangDisMajiangs = gangDisMajiangs;
		changeExtend();
	}

	public List<JzMj> getGangDisMajiangs() {
		return gangDisMajiangs;
	}

	/**
	 * 清理杠后摸的牌
	 */
	public void clearGangDisMajiangs() {
		this.gangActedMj = null;
		this.gangMajiang = null;
		this.gangDisMajiangs.clear();
		this.gangDice = -1;
		changeExtend();
	}

	/**
	 * guo 摸杠胡
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
		wrapper.putString(1, DataMapUtil.explodeListMap(actionSeatMap));
		wrapper.putString(2, DataMapUtil.explodeListMapMap(gangSeatMap));
		// w
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
	 * 能抢杠胡
	 *
	 * @return
	 */
	public boolean canGangHu() {
		return true;
	}

	public ClosingMjInfoRes.Builder sendAccountsMsg(boolean over, boolean selfMo, List<Integer> winList,
			int[] prickBirdMajiangIds, int[] seatBirds, Map<Integer, Integer> seatBirdMap, boolean isBreak,
			int bankerSeat, int catchBirdSeat) {

		// 大结算计算加倍分
		if (over && jiaBei == 1) {
			int jiaBeiPoint = 0;
			int loserCount = 0;
			for (JzMjPlayer player : seatMap.values()) {
				if (player.getTotalPoint() > 0 && player.getTotalPoint() < jiaBeiFen) {
					jiaBeiPoint += player.getTotalPoint() * (jiaBeiShu - 1);
					player.setTotalPoint(player.getTotalPoint() * jiaBeiShu);
				} else if (player.getTotalPoint() < 0) {
					loserCount++;
				}
			}
			if (jiaBeiPoint > 0) {
				for (JzMjPlayer player : seatMap.values()) {
					if (player.getTotalPoint() < 0) {
						player.setTotalPoint(player.getTotalPoint() - (jiaBeiPoint / loserCount));
					}
				}
			}
		}

		//大结算低于below分+belowAdd分
		if(over&&belowAdd>0&&playerMap.size()==2){
			for (JzMjPlayer player : seatMap.values()) {
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
		if (winList == null || winList.size() == 0) {
			fangPaoSeat = 0;
		}
		for (JzMjPlayer player : seatMap.values()) {
			ClosingMjPlayerInfoRes.Builder build = null;
			if (over) {
				build = player.buildTotalClosingPlayerInfoRes();
			} else {
				build = player.buildOneClosingPlayerInfoRes();
			}
			if (seatBirdMap != null && seatBirdMap.containsKey(player.getSeat())) {
				build.setBirdPoint(seatBirdMap.get(player.getSeat()));
			} else {
				build.setBirdPoint(0);
			}
			if (winList != null && winList.contains(player.getSeat())) {
				if (!selfMo) {
					// 不是自摸
					List<Integer> huMjIds = player.getHuMjIds();
					if (huMjIds != null && huMjIds.size() > 0) {
						for (int mjId : huMjIds) {
							if (!build.getHandPaisList().contains(mjId)) {
								build.addHandPais(mjId);
							}
						}
						int isHu = 0;
						int isHu2 = 0;
						if (huMjIds.size() >= 2) {
							isHu = huMjIds.get(0) * 1000 + huMjIds.get(1);
							if(huMjIds.size()==3){
								isHu2 = huMjIds.get(2);
							}else if(huMjIds.size()==4) {
								isHu2 = huMjIds.get(2) * 1000 + huMjIds.get(3);
							}
						} else {
							isHu = huMjIds.get(0);
						}
						build.setTotalFan(isHu2);
						build.setIsHu(isHu);
					} else {
						if(nowDisCardIds.isEmpty()){
							int mjs = firstMomj;//庄家起手
							build.setIsHu(mjs);
						}else{
							JzMj huMajiang = nowDisCardIds.get(0);
							if (!build.getHandPaisList().contains(huMajiang.getId())) {
								build.addHandPais(huMajiang.getId());
							}
							build.setIsHu(huMajiang.getId());
						}
					}
				} else {
					List<Integer> huMjIds = player.getHuMjIds();
					if (huMjIds != null && huMjIds.size() > 0) {
						for (int mjId : huMjIds) {
							if (!build.getHandPaisList().contains(mjId)) {
								build.addHandPais(mjId);
							}
						}
						int isHu = 0;
						int isHu2 = 0;
						if (huMjIds.size() >= 2) {
							isHu = huMjIds.get(0) * 1000 + huMjIds.get(1);
							if(huMjIds.size()==3){
								isHu2 = huMjIds.get(2);
							}else if(huMjIds.size()==4) {
								isHu2 = huMjIds.get(2) * 1000 + huMjIds.get(3);
							}
						} else {
							isHu = huMjIds.get(0);
						}
						build.setIsHu(isHu);
						build.setTotalFan(isHu2);
					} else {
						build.setIsHu(player.getLastMoMajiang().getId());
					}
				}
			}
			if (player.getSeat() == fangPaoSeat) {
				build.setFanPao(1);
			}

			if (winList != null && winList.contains(player.getSeat())) {
				// 手上没有剩余的牌放第一位为赢家
				// list.add(0, build.build());
				builderList.add(0, build);
			} else {
				// list.add(build.build());
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
			for (JzMjPlayer player : seatMap.values()) {
				if (player.getWinLoseCredit() > dyjCredit) {
					dyjCredit = player.getWinLoseCredit();
				}
			}
			for (ClosingMjPlayerInfoRes.Builder builder : builderList) {
				JzMjPlayer player = seatMap.get(builder.getSeat());
				calcCommissionCredit(player, dyjCredit);
				builder.setWinLoseCredit(player.getWinLoseCredit());
				builder.setCommissionCredit(player.getCommissionCredit());
			}
		} else if (isGroupTableGoldRoom()) {
            // -----------亲友圈金币场---------------------------------
            for (JzMjPlayer player : seatMap.values()) {
                player.setWinGold(player.getTotalPoint() * gtgDifen);
            }
            calcGroupTableGoldRoomWinLimit();
            for (ClosingMjPlayerInfoRes.Builder builder : builderList) {
                JzMjPlayer player = seatMap.get(builder.getSeat());
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
		res.addAllExt(buildAccountsExt(over ? 1 : 0));
		res.addCreditConfig(creditMode); // 0
		res.addCreditConfig(creditJoinLimit); // 1
		res.addCreditConfig(creditDissLimit); // 2
		res.addCreditConfig(creditDifen); // 3
		res.addCreditConfig(creditCommission); // 4
		res.addCreditConfig(creditCommissionMode1); // 5
		res.addCreditConfig(creditCommissionMode2); // 6
		res.addCreditConfig(creditCommissionLimit); // 7
		if (seatBirds != null) {
			res.addAllBirdSeat(DataMapUtil.toList(seatBirds));
		}
		if (prickBirdMajiangIds != null) {
			res.addAllBird(DataMapUtil.toList(prickBirdMajiangIds));
		}
		res.setCatchBirdSeat(catchBirdSeat);
		res.addAllLeftCards(JzMjHelper.toMajiangIds(leftMajiangs));
		for (JzMjPlayer player : seatMap.values()) {
			player.writeSocket(res.build());
		}
		return res;

	}

	/**
	 * 杠上花和杠上炮
	 *
	 * @return
	 */
	public JzMj getGangHuMajiang(int seat) {
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
		if (majiangId == 0) {
			return null;
		}
		return JzMj.getMajang(majiangId);

	}

	public List<String> buildAccountsExt(int over) {
		List<String> ext = new ArrayList<>();
		if (isGroupRoom()) {
			ext.add(loadGroupId());
		} else {
			ext.add("0");
		}
		ext.add(id + "");
		ext.add(masterId + "");
		ext.add(TimeUtil.formatTime(TimeUtil.now()));
		ext.add(playType + "");
		ext.add(getMasterName() + "");

		ext.add(getConifg(0) + "");
		ext.add(lastWinSeat + "");
		ext.add(calcBird + "");
		ext.add(gpsWarn + "");
//		ext.add(queYiSe + "");
//		ext.add(banbanHu + "");
//		ext.add(yiZhiHua + "");
//		ext.add(liuliuShun + "");
//		ext.add(daSiXi + "");
//		ext.add(jinTongYuNu + "");
//		ext.add(jieJieGao + "");
//		ext.add(sanTong + "");
//		ext.add(zhongTuLiuLiuShun + "");
//		ext.add(zhongTuSiXi + "");
//		ext.add(kePiao + "");
		ext.add(isCalcBanker + "");
		ext.add(birdNum + "");
		ext.add(isAutoPlay + "");
		ext.add(over + ""); // 27
		return ext;
	}

	@Override
	public void sendAccountsMsg() {

		// 小胡计算
//		if(jsXiaoHuF==1){
//			boolean xiaohu = false;
//			for (JzMjPlayer winPlayer : seatMap.values()) {
//				if (winPlayer.getHuXiaohu().size() == 0) {
//					continue;
//				}
//				xiaohu = true;
//				break;
//			}
//			if(xiaohu){
//				calXiaoHuFen(new ArrayList<Integer>(), true, null);
//			}
//		}

		ClosingMjInfoRes.Builder builder = sendAccountsMsg(true, false, null, null, null, null, true, 0, 0);
		saveLog(true, 0l, builder.build());
	}

	public Class<? extends Player> getPlayerClass() {
		return JzMjPlayer.class;
	}

	@Override
	public int getWanFa() {
		return GameUtil.play_type_mj_jzmj;
	}

	@Override
	public void checkReconnect(Player player) {
//		if (super.isAllReady() && getKePiao() == 1 && getTableStatus() == JzMjConstants.TABLE_STATUS_PIAO) {
//			JzMjPlayer player1 = (JzMjPlayer) player;
//			if (player1.getPiaoPoint() < 0) {
//				ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_table_status_piao,
//						getTableStatus());
//				player1.writeSocket(com.build());
//				return;
//			}
//		}
		checkSendGangRes(player);
		if (askLastMajaingSeat != 0) {
			sendAskLastMajiangRes(player.getSeat());
		}
		if (actionSeatMap.isEmpty()) {
			// 没有其他可操作的动作事件
			if (player instanceof JzMjPlayer) {
				JzMjPlayer JzMjPlayer = (JzMjPlayer) player;
				if (JzMjPlayer != null) {
					if (JzMjPlayer.isAlreadyMoMajiang()) {
//						if (!JzMjPlayer.getGang().isEmpty()) {
//							List<JzMj> disMajiangs = new ArrayList<>();
//							disMajiangs.add(JzMjPlayer.getLastMoMajiang());
//							chuPai(JzMjPlayer, disMajiangs, 0);
//						}
					}
				}
			}
		}
		if (isBegin() && player.getSeat() == lastWinSeat && actionSeatMap.isEmpty()) {
			// 如果是起手判断是否还有人可胡小胡 没有的话通知庄家出牌
			JzMjPlayer bankPlayer = seatMap.get(lastWinSeat);
			ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.req_com_kt_ask_dismajiang);// 推送庄家出牌
			bankPlayer.writeSocket(com.build());
		}

		if (state == table_state.play) {
			if (player.getHandPais() != null && player.getHandPais().size() > 0) {
				sendTingInfo((JzMjPlayer) player);
			}
		}

	}

	public static void main(String[] args) {
		//System.out.println(Math.pow(3, 2));
	}

	@Override
	public boolean consumeCards() {
		return SharedConstants.consumecards;
	}

	@Override
	public void checkAutoPlay() {
		if (System.currentTimeMillis() - lastAutoPlayTime < 100) {
			return;
		}
		// 发起解散不自动打牌
		if (getSendDissTime() > 0) {
			for (JzMjPlayer player : seatMap.values()) {
				if (player.getLastCheckTime() > 0) {
					player.setLastCheckTime(player.getLastCheckTime() + 1 * 1000);
				}
			}
			return;
		}
		// //有操作时不自动打牌
		// if(!getActionSeatMap().isEmpty()){
		// return ;
		// }
		for (JzMjPlayer player : seatMap.values()) {
			if(!player.getGang().isEmpty() ){
				continue;
			}
			// if ((!player.getGang().isEmpty() || player.isBaoting())
			if ( player.isBaoting() && player.isAlreadyMoMajiang() && getMoMajiangSeat() == player.getSeat()) {
				// 能胡牌就不自动打出去
				List<Integer> actionList = actionSeatMap.get(player.getSeat());
				if (actionList != null && (actionList.get(JzMjAction.HU) == 1 || actionList.get(JzMjAction.ZIMO) == 1
						|| actionList.get(JzMjAction.MINGGANG) == 1 || actionList.get(JzMjAction.BUZHANG) == 1
						|| actionList.get(JzMjAction.ANGANG) == 1 || actionList.get(JzMjAction.BUZHANG_AN) == 1
						|| hasXiaoHu(actionList))) {
					continue;
				}

				if (nowDisCardSeat != player.getSeat()) {
					continue;
				}
				//System.out.println("===========action//System.out.println(player.getGang());");
				//System.out.println(player.getGang());
				List<JzMj> disMjiang = new ArrayList<>();
				disMjiang.add(player.getHandMajiang().get(player.getHandMajiang().size() - 1));
				 chuPai(player, disMjiang, JzMjDisAction.action_chupai);
				// 执行完一个就退出，防止出牌操作后有报听玩家摸牌
				setLastAutoPlayTime(System.currentTimeMillis());
				return;
			}
		}

		if (isAutoPlay < 1) {
			return;
		}

        if (isAutoPlayOff()) {
            // 托管关闭
            for (int seat : seatMap.keySet()) {
                JzMjPlayer player = seatMap.get(seat);
                player.setAutoPlay(false, false);
                player.setCheckAutoPlay(false);
            }
            return;
        }

		if (getTableStatus() == JzMjConstants.TABLE_STATUS_PIAO) {
			for (int seat : seatMap.keySet()) {
				JzMjPlayer player = seatMap.get(seat);
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
				JzMjPlayer player = seatMap.get(seat);
				if (player.getPiaoPoint() < 0) {
					piao = false;
				}

			}
			if (piao) {
				setTableStatus(JzMjConstants.AUTO_PLAY_TIME);
			}

		} else if (state == table_state.play) {
			autoPlay();
		} else {
			if (getPlayedBureau() == 0) {
				return;
			}
			readyTime++;
			// for (JzMjPlayer player : seatMap.values()) {
			// if (player.checkAutoPlay(1, false)) {
			// autoReady(player);
			// }
			// }
			// 开了托管的房间，xx秒后自动开始下一局
			for (JzMjPlayer player : seatMap.values()) {
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
				// 有胡处理胡
				for (int seat : huSeatList) {
					JzMjPlayer player = seatMap.get(seat);
					if (player == null) {
						continue;
					}
					if (!player.checkAutoPlay(2, false)) {
						continue;
					}
					playCommand(player, new ArrayList<>(), JzMjDisAction.action_hu);
				}
				return;
			} else {
				int action, seat;
				for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
					List<Integer> actList = JzMjDisAction.parseToDisActionList(entry.getValue());
					if (actList == null) {
						continue;
					}
					seat = entry.getKey();
					action = JzMjDisAction.getAutoMaxPriorityAction(actList);
					JzMjPlayer player = seatMap.get(seat);
					if (!player.checkAutoPlay(0, false)) {
						continue;
					}
					boolean chuPai = false;
					if (player.isAlreadyMoMajiang()) {
						chuPai = true;
					}
					if (action == JzMjDisAction.action_peng) {
						if (player.isAutoPlaySelf()) {
							// 自己开启托管直接过
							playCommand(player, new ArrayList<>(), JzMjDisAction.action_pass);
							if (chuPai) {
								autoChuPai(player);
							}
						} else {
							if (nowDisCardIds != null && !nowDisCardIds.isEmpty()) {
                                for(JzMj mj : nowDisCardIds) {
                                    List<JzMj> mjList = new ArrayList<>();
                                    for (JzMj handMj : player.getHandMajiang()) {
                                        if (handMj.getVal() == mj.getVal()) {
                                            mjList.add(handMj);
                                            if (mjList.size() == 2) {
                                                break;
                                            }
                                        }
                                    }
                                    if(mjList.size() >= 2){
                                        playCommand(player, mjList, JzMjDisAction.action_peng);
                                        break;
                                    }
                                }
							}
						}
					}
					// else if(action == JzMjDisAction.action_chi){
					// playCommand(player, new ArrayList<>(),
					// JzMjDisAction.action_chi);
					// if (chuPai) {
					// autoChuPai(player);
					// }
					//
					// }
					else {
						playCommand(player, new ArrayList<>(), JzMjDisAction.action_pass);
						if (chuPai) {
							autoChuPai(player);
						}
					}
				}
			}
		} else {
			JzMjPlayer player = seatMap.get(nowDisCardSeat);
			if (player == null || !player.checkAutoPlay(0, false)) {
				return;
			}
			autoChuPai(player);
		}
	}

	public void autoChuPai(JzMjPlayer player) {
		//System.out.println("autoChuPai===========================");
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
		// JzMj mj = JzMj.getMajang(mjId);

		while (mjId == -1 && index >= 0) {
			mjId = handMjIds.get(index);
			// mj = JzMj.getMajang(mjId);

		}
		if (mjId != -1) {
			List<JzMj> mjList = JzMjHelper.toMajiang(Arrays.asList(mjId));
			playCommand(player, mjList, JzMjDisAction.action_chupai);
		}
	}

	public void autoPiao(JzMjPlayer player) {
		int piaoPoint = 0;
		if (getTableStatus() != JzMjConstants.TABLE_STATUS_PIAO) {
			return;
		}
		if (player.getPiaoPoint() < 0) {
			player.setPiaoPoint(piaoPoint);
		} else {
			return;
		}
		ComRes.Builder build = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_piao_fen, player.getSeat(),
				piaoPoint);
		broadMsg(build.build());
		broadMsgRoomPlayer(build.build());
		checkDeal(player.getUserId());
	}

	public boolean IsCalcBankerPoint() {
		return isCalcBanker == 1;
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

	@Override
	public void initExtend0(JsonWrapper extend) {
		for (JzMjPlayer player : seatMap.values()) {
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
			moGang = JzMj.getMajang(moGangMajiangId);
		}
		String moGangHu = extend.getString(9);
		if (!StringUtils.isBlank(moGangHu)) {
			moGangHuList = StringUtil.explodeToIntList(moGangHu);
		}
		String gangDisMajiangstr = extend.getString(10);
		if (!StringUtils.isBlank(gangDisMajiangstr)) {
			gangDisMajiangs = JzMjHelper.explodeMajiang(gangDisMajiangstr, ",");
		}
		int gangMajiang = extend.getInt(11, 0);
		if (gangMajiang != 0) {
			this.gangMajiang = JzMj.getMajang(gangMajiang);
		}

		askLastMajaingSeat = extend.getInt(12, 0);
		moLastMajiangSeat = extend.getInt(13, 0);
		int lastMajiangId = extend.getInt(14, 0);
		if (lastMajiangId != 0) {
			this.lastMajiang = JzMj.getMajang(lastMajiangId);
		}
		fristLastMajiangSeat = extend.getInt(15, 0);
		disEventAction = extend.getInt(16, 0);
		isCalcBanker = extend.getInt(17, 1);
		calcBird = extend.getInt(18, 1);
		//kePiao = extend.getInt(19, 0);
		tempActionMap = loadTempActionMap(extend.getString("tempActions"));

		gpsWarn = extend.getInt(20, 0);
//		queYiSe = extend.getInt(21, 0);
//		banbanHu = extend.getInt(22, 0);
//		yiZhiHua = extend.getInt(23, 0);
//		liuliuShun = extend.getInt(24, 0);
//		daSiXi = extend.getInt(25, 0);
//		jinTongYuNu = extend.getInt(26, 0);
//		jieJieGao = extend.getInt(27, 0);
//		sanTong = extend.getInt(28, 0);
//		zhongTuLiuLiuShun = extend.getInt(29, 0);
//		zhongTuSiXi = extend.getInt(30, 0);
		String showMj = extend.getString(31);
		if (!StringUtils.isBlank(showMj)) {
			showMjSeat = StringUtil.explodeToIntList(showMj);
		}
		maxPlayerCount = extend.getInt(32, 4);
		gangDice = extend.getInt(33, -1);
		String moTailPaiStr = extend.getString(34);
		if (!StringUtils.isBlank(moTailPaiStr)) {
			moTailPai = StringUtil.explodeToIntList(moTailPaiStr);
		}
		String moLastSeatsStr = extend.getString(35);
		if (!StringUtils.isBlank(moLastSeatsStr)) {
			moLastSeats = StringUtil.explodeToIntList(moLastSeatsStr);
		}
		isBegin = extend.getInt(36, 0) == 1;
		dealDice = extend.getInt(37, 0);

		jiaBei = extend.getInt(38, 0);
		jiaBeiFen = extend.getInt(39, 0);
		jiaBeiShu = extend.getInt(40, 0);

//		kongBird = extend.getInt(41, 0);
		buChi = extend.getInt(42, 0);
//		OnlyDaHu = extend.getInt(43, 0);
//		xiaohuZiMo = extend.getInt(44, 0);
//		queYiMen = extend.getInt(45, 0);
		jiajianghu = extend.getInt(46, 0);

		isAutoPlay = extend.getInt(47, 0);
		autoPlayGlob = extend.getInt(48, 0);
		menqing = extend.getInt(49, 0);
		tableStatus = extend.getInt(50, 0);
		topFen = extend.getInt(51, 0);
		gangMoSi = extend.getInt(52, 0);
		qiShouNiaoFen= extend.getInt(53, 0);
		below= extend.getInt(54, 0);
		belowAdd= extend.getInt(55, 0);
		gangBuF= extend.getInt(56, 0);
		quanqiurJiang= extend.getInt(57, 0);
		difen= extend.getInt(58, 0);
//		menQingZM = extend.getInt(59, 0);
//		jsXiaoHuF = extend.getInt(60, 0);
		xiaoHuAuto = extend.getInt(61, 0);
//		xiaoHuGdF = extend.getInt(62, 0);
		jjHKqFg = extend.getInt(63, 0);
		choupai =extend.getInt(64, 0);
		isXianShangZhuang=extend.getInt(65, 0);
		firstMomj =extend.getInt(66, 0);
		table_tingpai =extend.getInt(67, 0);
		gangSaveSeat =extend.getInt(68,0);
		op_hitBirdPoint =extend.getInt(69,0);
	}

	@Override
	public JsonWrapper buildExtend0(JsonWrapper wrapper) {
		// 1-4 玩家座位信息
		for (JzMjPlayer player : seatMap.values()) {
			wrapper.putString(player.getSeat(), player.toExtendStr());
		}
		wrapper.putString(5, DataMapUtil.explode(huConfirmMap));
		wrapper.putInt(6, birdNum);
		wrapper.putInt(7, moMajiangSeat);
		wrapper.putInt(8, moGang != null ? moGang.getId() : 0);
		wrapper.putString(9, StringUtil.implode(moGangHuList, ","));
		wrapper.putString(10, JzMjHelper.implodeMajiang(gangDisMajiangs, ","));
		wrapper.putInt(11, gangMajiang != null ? gangMajiang.getId() : 0);
		wrapper.putInt(12, askLastMajaingSeat);
		wrapper.putInt(13, moLastMajiangSeat);
		wrapper.putInt(14, lastMajiang != null ? lastMajiang.getId() : 0);
		wrapper.putInt(15, fristLastMajiangSeat);
		wrapper.putInt(16, disEventAction);
		wrapper.putInt(17, isCalcBanker);
		wrapper.putInt(18, calcBird);
		//wrapper.putInt(19, kePiao);
		JSONArray tempJsonArray = new JSONArray();
		for (int seat : tempActionMap.keySet()) {
			tempJsonArray.add(tempActionMap.get(seat).buildData());
		}
		wrapper.putString("tempActions", tempJsonArray.toString());

		wrapper.putInt(20, gpsWarn);
//		wrapper.putInt(21, queYiSe);//queYiSe
//		wrapper.putInt(22, banbanHu);//queYiSe
//		wrapper.putInt(23, yiZhiHua);//yiZhiHua
//		wrapper.putInt(24, liuliuShun);//liuliuShun
//		wrapper.putInt(25, daSiXi);//daSiXi
//		wrapper.putInt(26, jinTongYuNu);//jinTongYuNu
//		wrapper.putInt(27, jieJieGao);//jieJieGao
//		wrapper.putInt(28, sanTong);//sanTong
//		wrapper.putInt(29, zhongTuLiuLiuShun);//zhongTuLiuLiuShun
//		wrapper.putInt(30, zhongTuSiXi);//zhongTuSiXi
		wrapper.putInt(21, queYiSe);
		wrapper.putInt(22, banbanHu);
		wrapper.putInt(23, 0);
		wrapper.putInt(24, 0);
		wrapper.putInt(25, 0);
		wrapper.putInt(26, 0);
		wrapper.putInt(27, 0);
		wrapper.putInt(28, 0);
		wrapper.putInt(29, 0);
		wrapper.putInt(30, 0);
		wrapper.putString(31, StringUtil.implode(showMjSeat, ","));
		wrapper.putInt(32, maxPlayerCount);
		wrapper.putInt(33, gangDice);
		wrapper.putString(34, StringUtil.implode(moTailPai, ","));
		wrapper.putString(35, StringUtil.implode(moLastSeats, ","));
		wrapper.putInt(36, isBegin ? 1 : 0);
		wrapper.putInt(37, dealDice);

		wrapper.putInt(38, jiaBei);
		wrapper.putInt(39, jiaBeiFen);
		wrapper.putInt(40, jiaBeiShu);

		wrapper.putInt(41, 0);//kongBird
		wrapper.putInt(42, buChi);//kongBird
		wrapper.putInt(43, 0);//OnlyDaHu
		wrapper.putInt(44, 0);//xiaohuZiMo
		wrapper.putInt(45, 0);//queYiMen
		wrapper.putInt(41, 0);
		wrapper.putInt(42, buChi);
		wrapper.putInt(43, 0);
		wrapper.putInt(44, 0);
		wrapper.putInt(45, 0);
		wrapper.putInt(46, jiajianghu);

		wrapper.putInt(47, isAutoPlay);
		wrapper.putInt(48, autoPlayGlob);
		wrapper.putInt(49, menqing);
		wrapper.putInt(50, tableStatus);
		wrapper.putInt(51, topFen);
		wrapper.putInt(52, gangMoSi);
		wrapper.putInt(53, qiShouNiaoFen);
		wrapper.putInt(56, gangBuF);
		wrapper.putInt(54, below);
		wrapper.putInt(55, belowAdd);
		wrapper.putInt(57, quanqiurJiang);
		wrapper.putInt(58, difen);
		//wrapper.putInt(59, menQingZM);
		//wrapper.putInt(60, jsXiaoHuF);
		wrapper.putInt(59, 0);
		wrapper.putInt(60, 0);
		wrapper.putInt(61, xiaoHuAuto);
		//wrapper.putInt(62, xiaoHuGdF);//
		wrapper.putInt(62, 0);
		wrapper.putInt(63, jjHKqFg);
		wrapper.putInt(64, choupai);
		wrapper.putInt(65, isXianShangZhuang);
		wrapper.putInt(66, firstMomj);
		wrapper.putInt(67, table_tingpai);
		wrapper.putInt(68, gangSaveSeat);
		wrapper.putInt(69, op_hitBirdPoint);
		return wrapper;
	}

	@Override
	public void createTable(Player player, int playType, int bureauCount, List<Integer> params, List<String> strParams,
			Object... objects) throws Exception {
		long id = getCreateTableId(player.getUserId(), playType);
		TableInf info = new TableInf();
		info.setMasterId(player.getUserId());
		info.setRoomId(0);
		info.setPlayType(playType);
		info.setTableId(id);
		info.setTotalBureau(bureauCount);
		info.setPlayBureau(1);
		info.setServerId(GameServerConfig.SERVER_ID);
		info.setCreateTime(new Date());
		info.setDaikaiTableId(daikaiTableId);
		info.setConfig(String.valueOf(0));
		TableDao.getInstance().save(info);
		loadFromDB(info);

		maxPlayerCount = StringUtil.getIntValue(params, 7, 4);// 比赛人数
		payType = StringUtil.getIntValue(params, 2, 1);// 支付方式
		birdNum = StringUtil.getIntValue(params, 3, 0);//  0 2 4 6 8
		calcBird = StringUtil.getIntValue(params, 4, 0);//1：中鸟+1 2：中鸟翻倍 3：中鸟加倍
		if(maxPlayerCount==2){
			choupai = StringUtil.getIntValue(params, 5, 0);//抽牌 0 13  26
		}
		topFen = StringUtil.getIntValue(params, 6, 0);//封顶21 31 42

		isAutoPlay = StringUtil.getIntValue(params, 8, 0);
		this.autoPlayGlob = StringUtil.getIntValue(params, 9, 0);
		// 加倍：0否，1是
		this.jiaBei = StringUtil.getIntValue(params, 10, 0);
		// 加倍分
		this.jiaBeiFen = StringUtil.getIntValue(params, 11, 0);
		// 加倍数
		this.jiaBeiShu = StringUtil.getIntValue(params, 12, 0);
		gangMoSi=  0;
		gangBuF =1;
		buChi =0;
		if(maxPlayerCount==2){
			int belowAdd = StringUtil.getIntValue(params, 13, 0);
			if(belowAdd<=100&&belowAdd>=0)
				this.belowAdd=belowAdd;
			int below = StringUtil.getIntValue(params, 14, 0);
			if(below<=100&&below>=0){
				this.below=below;
				if(belowAdd>0&&below==0)
					this.below=10;
			}
		}
		op_hitBirdPoint = StringUtil.getIntValue(params, 15, 0);
		quanqiurJiang= 1;// StringUtil.getIntValue(params, 37, 0);
		jjHKqFg =  StringUtil.getIntValue(params, 43, 0);
		if (maxPlayerCount != 2) {
			jiaBei = 0;
		}
		playedBureau = 0;

		// getRoomModeMap().put("1", "1"); //可观战（默认）
	}


	public void sendTingInfo(JzMjPlayer player) {
		if (player.isAlreadyMoMajiang()) {
			// if (actionSeatMap.containsKey(player.getSeat())) {
			// return;
			// }
			DaPaiTingPaiRes.Builder tingInfo = DaPaiTingPaiRes.newBuilder();
			List<JzMj> cards = new ArrayList<>(player.getHandMajiang());

			for (JzMj card : player.getHandMajiang()) {
				cards.remove(card);
				List<JzMj> huCards = JzMjTool.getTingMjs(cards, player.getGang(), player.getPeng(), player.getChi(),
						player.getBuzhang(), true, OnlyDaHu == 1,getQuanqiurJiang());
				cards.add(card);

				if (huCards == null || huCards.size() == 0) {
					continue;
				}
				DaPaiTingPaiInfo.Builder ting = DaPaiTingPaiInfo.newBuilder();
				ting.setMajiangId(card.getId());
				for (JzMj mj : huCards) {
					ting.addTingMajiangIds(mj.getId());
				}
				tingInfo.addInfo(ting.build());
			}
			if (tingInfo.getInfoCount() > 0) {

				player.writeSocket(tingInfo.build());
			}
		} else {
			List<JzMj> cards = new ArrayList<>(player.getHandMajiang());
			List<JzMj> huCards = JzMjTool.getTingMjs(cards, player.getGang(), player.getPeng(), player.getChi(),
					player.getBuzhang(), true, OnlyDaHu == 1,getQuanqiurJiang());

			if (huCards == null || huCards.size() == 0) {
				return;
			}
			TingPaiRes.Builder ting = TingPaiRes.newBuilder();
			for (JzMj mj : huCards) {
				ting.addMajiangIds(mj.getId());
			}

			player.writeSocket(ting.build());

		}
	}

	public static final List<Integer> wanfaList = Arrays.asList(GameUtil.play_type_mj_jzmj);

	public static void loadWanfaTables(Class<? extends BaseTable> cls) {
		for (Integer integer : wanfaList) {
			TableManager.wanfaTableTypesPut(integer, cls);
		}
	}

	/**
	 * 是否可以胡小胡
	 *
	 * @param actionIndex
	 *            CSMajiangConstants类定义
	 * @return
	 */
	public boolean canXiaoHu(int actionIndex) {

		// if(maxPlayerCount==2 && OnlyDaHu ==1) {
		// return false;
		// }
		switch (actionIndex) {
			case JzMjAction.QUEYISE:
				return queYiSe == 1;
			case JzMjAction.BANBANHU:
				return banbanHu == 1;
			default:
				return false;
		}
	}

	public void logFaPaiTable() {
		StringBuilder sb = new StringBuilder();
		sb.append("JzMj");
		sb.append("|").append(getId());
		sb.append("|").append(getPlayBureau());
		sb.append("|").append("faPai");
		sb.append("|").append(playType);
		sb.append("|").append(maxPlayerCount);
		sb.append("|").append(getPayType());
		sb.append("|").append(calcBird);
		sb.append("|").append(birdNum);
		sb.append("|").append(0);//kePiao
		sb.append("|").append(queYiSe);
		sb.append("|").append(banbanHu);
		sb.append("|").append(0);//yiZhiHua
		sb.append("|").append(0);//liuliuShun
		sb.append("|").append(0);//daSiXi
		sb.append("|").append(0);//jinTongYuNu
		sb.append("|").append(0);//jieJieGao
		sb.append("|").append(0);//sanTong
		sb.append("|").append(0);//zhongTuSiXi
		sb.append("|").append(0);//zhongTuLiuLiuShun
		sb.append("|").append(lastWinSeat);
		LogUtil.msg(sb.toString());
	}

	public void logFaPaiPlayer(JzMjPlayer player, List<Integer> actList) {
		StringBuilder sb = new StringBuilder();
		sb.append("JzMj");
		sb.append("|").append(getId());
		sb.append("|").append(getPlayBureau());
		sb.append("|").append(player.getUserId());
		sb.append("|").append(player.getSeat());
		sb.append("|").append("faPai");
		sb.append("|").append(player.getHandMajiang());
		sb.append("|").append(actListToString(actList));
		LogUtil.msg(sb.toString());
	}

	public void logMoMj(JzMjPlayer player, JzMj mj, List<Integer> actList) {
		StringBuilder sb = new StringBuilder();
		sb.append("JzMj");
		sb.append("|").append(getId());
		sb.append("|").append(getPlayBureau());
		sb.append("|").append(player.getUserId());
		sb.append("|").append(player.getSeat());
		sb.append("|").append("moPai");
		sb.append("|").append(getLeftMajiangCount());
		sb.append("|").append(mj);
		sb.append("|").append(actListToString(actList));
		sb.append("|").append(player.getHandMajiang());
		LogUtil.msg(sb.toString());
	}

    public void logGangMoMj(JzMjPlayer player, List<JzMj> mjs) {
        StringBuilder sb = new StringBuilder();
        sb.append("JzMj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append("gangMoPai");
        sb.append("|").append(getLeftMajiangCount());
        sb.append("|").append(mjs);
        sb.append("|").append("");
        sb.append("|").append(player.getHandMajiang());
        LogUtil.msg(sb.toString());
    }

	public void logChuPaiActList(JzMjPlayer player, JzMj mj, List<Integer> actList) {
		StringBuilder sb = new StringBuilder();
		sb.append("JzMj");
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

	public void logAction(JzMjPlayer player, int action, int xiaoHuType, List<JzMj> mjs, List<Integer> actList) {
		StringBuilder sb = new StringBuilder();
		sb.append("JzMj");
		sb.append("|").append(getId());
		sb.append("|").append(getPlayBureau());
		sb.append("|").append(player.getUserId());
		sb.append("|").append(player.getSeat());
		String actStr = "unKnown-" + action;
		if (action == JzMjDisAction.action_peng) {
			actStr = "peng";
		} else if (action == JzMjDisAction.action_minggang) {
			actStr = "mingGang";
		} else if (action == JzMjDisAction.action_chupai) {
			actStr = "chuPai";
		} else if (action == JzMjDisAction.action_pass) {
			actStr = "guo";
		} else if (action == JzMjDisAction.action_angang) {
			actStr = "anGang";
		} else if (action == JzMjDisAction.action_chi) {
			actStr = "chi";
		} else if (action == JzMjDisAction.action_buzhang) {
			actStr = "buZhang";
		} else if (action == JzMjDisAction.action_xiaohu) {
			actStr = "xiaoHu";
		} else if (action == JzMjDisAction.action_buzhang_an) {
			actStr = "buZhangAn";
		}
		sb.append("|").append(xiaoHuType);
		sb.append("|").append(actStr);
		sb.append("|").append(mjs);
		sb.append("|").append(actListToString(actList));
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
				if (i == JzMjAction.HU) {
					sb.append("hu");
				} else if (i == JzMjAction.PENG) {
					sb.append("peng");
				} else if (i == JzMjAction.MINGGANG) {
					sb.append("mingGang");
				} else if (i == JzMjAction.ANGANG) {
					sb.append("anGang");
				} else if (i == JzMjAction.CHI) {
					sb.append("chi");
				} else if (i == JzMjAction.BUZHANG) {
					sb.append("buZhang");
				} else if (i == JzMjAction.QUEYISE) {
					sb.append("queYiSe");
				} else if (i == JzMjAction.BANBANHU) {
					sb.append("banBanHu");
				} else if (i == JzMjAction.YIZHIHUA) {
					sb.append("yiZhiHua");
				} else if (i == JzMjAction.LIULIUSHUN) {
					sb.append("liuLiuShun");
				} else if (i == JzMjAction.DASIXI) {
					sb.append("daSiXi");
				} else if (i == JzMjAction.JINGTONGYUNU) {
					sb.append("jinTongYuNu");
				} else if (i == JzMjAction.JIEJIEGAO) {
					sb.append("jieJieGao");
				} else if (i == JzMjAction.SANTONG) {
					sb.append("sanTong");
				} else if (i == JzMjAction.ZHONGTUSIXI) {
					sb.append("zhongTuSiXi");
				} else if (i == JzMjAction.ZHONGTULIULIUSHUN) {
					sb.append("zhongTuLiuLiuShun");
				} else if (i == JzMjAction.BUZHANG_AN) {
					sb.append("buZhangAn");
				} else if (i == JzMjAction.BAOTING) {
					sb.append("baoting");
				}
			}
		}
		sb.append("]");
		return sb.toString();
	}

	/**
	 * 小胡展示的麻将需要隐藏起来
	 *
	 * @param player
	 */
	public void processHideMj(JzMjPlayer player) {
		if (showMjSeat.contains(player.getSeat()) && disCardRound != 0) {
			PlayMajiangRes.Builder hideMj = PlayMajiangRes.newBuilder();
			buildPlayRes(hideMj, player, JzMjDisAction.action_hideMj, null);
			broadMsgToAll(hideMj.build());
			showMjSeat.remove(Integer.valueOf(player.getSeat()));
			player.clearXiaoHuMjList();
		}
	}

	public void clearShowMjSeat() {
		showMjSeat.clear();
		changeExtend();
	}

	public void addShowMjSeat(int seat, int xiaoHuType) {
		// if(xiaoHuType == JzMjAction.QUEYISE || xiaoHuType ==
		// JzMjAction.BANBANHU || xiaoHuType == JzMjAction.YIZHIHUA){
		if (!showMjSeat.contains(seat)) {
			showMjSeat.add(seat);
			changeExtend();
		}
		// }

	}

	/**
	 * --//庄家是否出过牌了
	 * add 是否开局都没出牌
	 * @return
	 */
	public boolean isBegin() {
		return  isBegin && nowDisCardIds.size() == 0 ;
	}

	public void setIsBegin(boolean begin) {
		if (isBegin != begin) {
			isBegin = begin;
			changeExtend();
		}
	}

	@Override
	public boolean isAllReady() {
		if (super.isAllReady()) {
//			if (getKePiao() == 1) {
//				if (2== 1) {
//				boolean bReturn = true;
				// 机器人默认处理
//				if (this.isTest()) {
//					for (JzMjPlayer robotPlayer : seatMap.values()) {
//						if (robotPlayer.isRobot()) {
//							robotPlayer.setPiaoPoint(1);
//						}
//					}
//				}
//				for (JzMjPlayer player : seatMap.values()) {
//					if (player.getPiaoPoint() < 0) {
//						ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_table_status_piao,
//								getTableStatus());
//						player.writeSocket(com.build());
//						if (getTableStatus() != JzMjConstants.TABLE_STATUS_PIAO) {
//							player.setLastCheckTime(System.currentTimeMillis());
//						}
//						bReturn = false;
//						// if(getTableStatus()!=JzMjConstants.TABLE_STATUS_PIAO)
//						// {
//						//
//						// }
//					}
//				}
//				setTableStatus(JzMjConstants.TABLE_STATUS_PIAO);

//				return bReturn;
//			} else {
//				int point = 0;
//				if (getKePiao() == 2 || getKePiao() == 3 || getKePiao() == 4) {
//					point = getKePiao() - 1;
//				}
//
//				for (JzMjPlayer player : seatMap.values()) {
//					player.setPiaoPoint(point);
//				}
				return true;
//			}
		}
		return false;
	}

	public void setTableStatus(int tableStatus) {
		this.tableStatus = tableStatus;
	}

	public int getTableStatus() {
		return tableStatus;
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
	 * 杠后推送给玩家杠结束
	 */
	public void checkClearGangDisMajiang() {
		List<JzMj> moList = getGangDisMajiangs();
		if (moList != null && moList.size() > 0 && actionSeatMap.isEmpty()) {
			JzMjPlayer player = seatMap.get(getMoMajiangSeat());
			for (JzMjPlayer seatPlayer : seatMap.values()) {
				GangMoMajiangRes.Builder gangbuilder = GangMoMajiangRes.newBuilder();
				gangbuilder.setRemain(getLeftMajiangCount());
				gangbuilder.setGangId(gangMajiang.getId());
				gangbuilder.setUserId(player.getUserId() + "");
				gangbuilder.setName(player.getName() + "");
				gangbuilder.setSeat(player.getSeat());
				gangbuilder.setReconnect(0);
				gangbuilder.setDice(0);
				if (gangActedMj != null) {
					GangPlayMajiangRes.Builder playBuilder = GangPlayMajiangRes.newBuilder();
					playBuilder.setMajiangId(gangActedMj.getId());
					gangbuilder.addGangActs(playBuilder);
				}
				seatPlayer.writeSocket(gangbuilder.build());
			}
			clearGangDisMajiangs();
		}
	}

	public void clearMoLastSeat() {
		moLastSeats.clear();
		changeExtend();
	}

	public void addMoLastSeat(int seat) {
		if (moLastSeats == null) {
			moLastSeats = new ArrayList<>();
		}
		moLastSeats.add(seat);
		changeExtend();
	}

	public void removeMoLastSeat(int seat) {
		int removIndex = -1;
		for (int i = 0; i < moLastSeats.size(); i++) {
			if (moLastSeats.get(i) == seat) {
				removIndex = i;
				break;
			}
		}
		if (removIndex != -1) {
			moLastSeats.remove(removIndex);
		}
		changeExtend();
	}

	/**
	 * 询问玩家措海底
	 *
	 * @param player
	 * @param state
	 *            1底单玩家摸海底，0通知玩家关闭摸海底界面
	 */
	public void sendMoLast(JzMjPlayer player, int state) {
		ComRes.Builder res = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_asklastmajiang, state);
		player.writeSocket(res.build());
	}
	/**
	 * 是否玩家有报听
	 *
	 * @return
	 */
	public boolean hasBaoTing() {
		if (actionSeatMap.isEmpty()) {
			return false;
		}
		for (List<Integer> actList : actionSeatMap.values()) {
			if (actList == null || actList.size() == 0) {
				continue;
			}
			if (actList !=null && actList.size() >=19 && actList.get(18) == 1) {
				return true;
			}
		}
		return false;
	}
	/**
	 * 是否玩家有小胡
	 *
	 * @return
	 */
	public boolean hasXiaoHu() {
		if (actionSeatMap.isEmpty()) {
			return false;
		}
		//System.out.println("=======================actionSeatMap:");
		//System.out.println(actionSeatMap);
		for (List<Integer> actList : actionSeatMap.values()) {
			if (actList == null || actList.size() == 0) {
				continue;
			}
			if (JzMjAction.getFirstXiaoHu(actList) != -1) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 是否玩家有小胡
	 *
	 * @return
	 */
	public boolean hasXiaoHu(List<Integer> actList) {
		if (JzMjAction.getFirstXiaoHu(actList) != -1) {
			return true;
		}
		return false;
	}

	@Override
	public boolean isPlaying() {
		if (super.isPlaying()) {
			return true;
		}
		return getTableStatus() == JzMjConstants.TABLE_STATUS_PIAO;
	}

	public int getIsAutoPlay() {
		return isAutoPlay;
	}

	public void setIsAutoPlay(int isAutoPlay) {
		this.isAutoPlay = isAutoPlay;
	}

	public int getDealDice() {
		return dealDice;
	}

	public void setDealDice(int dealDice) {
		this.dealDice = dealDice;
	}

//	public int getBuChi() {
//		return buChi;
//	}
//
//	public void setBuChi(int buChi) {
//		this.buChi = buChi;
//	}
//
//	public int getXiaohuZiMo() {
//		return xiaohuZiMo;
//	}
//
//	public void setXiaohuZiMo(int xiaohuZiMo) {
//		this.xiaohuZiMo = xiaohuZiMo;
//	}
//
//	public int getOnlyDaHu() {
//		return OnlyDaHu;
//	}
//
//	public void setOnlyDaHu(int onlyDaHu) {
//		OnlyDaHu = onlyDaHu;
//	}

	public int getJiajianghu() {
		return jiajianghu;
	}

	public void setJiajianghu(int jiajianghu) {
		this.jiajianghu = jiajianghu;
	}

//	public int getQueYiMen() {
//		return queYiMen;
//	}

	public int getMenqing() {
		return menqing;
	}
//	 public int getMenqingZM() {
//		return menQingZM;
//	}


	public int getXiaoHuAuto() {
		return xiaoHuAuto;
	}

	public int getQuanqiurJiang() {
		return quanqiurJiang;
	}

	public int getIsXianShangZhuang() {
		return isXianShangZhuang;
	}

	public void setIsXianShangZhuang(int isXianShangZhuang) {
		this.isXianShangZhuang = isXianShangZhuang;
		changeExtend();
	}

	public String getTableMsg() {
		Map<String, Object> json = new HashMap<>();
		json.put("wanFa", "靖州麻将");
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
		return "靖州麻将";
    }
	public int getOnlyDaHu(){
		return 0;
	}
	public int getXiaohuZiMo(){
		return 0;
	}
	public int getBuChi(){
		return 0;
	}
	public int getMenqingZM(){
		return 0;
	}

	public int getTable_tingpai() {
		return table_tingpai;
	}

	public void setTable_tingpai(int table_tingpai) {
		this.table_tingpai = table_tingpai;

	}
}
