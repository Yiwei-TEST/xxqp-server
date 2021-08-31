package com.sy599.game.qipai.tdhmj.bean;

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
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.ComMsg.ComRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.DaPaiTingPaiInfo;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.DaPaiTingPaiRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.GangMoMajiangRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.GangPlayMajiangRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.MoMajiangRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.PlayMajiangRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.TingPaiRes;
import com.sy599.game.msg.serverPacket.TableMjResMsg.ClosingMjInfoRes;
import com.sy599.game.msg.serverPacket.TableMjResMsg.ClosingMjPlayerInfoRes;
import com.sy599.game.msg.serverPacket.TableRes;
import com.sy599.game.msg.serverPacket.TableRes.CreateTableRes;
import com.sy599.game.msg.serverPacket.TableRes.DealInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes;
import com.sy599.game.qipai.tdhmj.constant.TdhMjAction;
import com.sy599.game.qipai.tdhmj.constant.TdhMjConstants;
import com.sy599.game.qipai.tdhmj.rule.TdhMj;
import com.sy599.game.qipai.tdhmj.rule.TdhMjHelper;
import com.sy599.game.qipai.tdhmj.rule.TdhMjRobotAI;
import com.sy599.game.qipai.tdhmj.tool.TdhMjQipaiTool;
import com.sy599.game.qipai.tdhmj.tool.TdhMjResTool;
import com.sy599.game.qipai.tdhmj.tool.TdhMjTool;
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

/**
 * @author l 推倒胡麻将牌桌信息
 */
public class TdhMjTable extends BaseTable {
	/**
	 * 当前桌上打出的牌
	 */
	private List<TdhMj> nowDisCardIds = new ArrayList<>();
	/**
	 * 所有玩家当前可作的操作
	 */
	private Map<Integer, List<Integer>> actionSeatMap = new ConcurrentHashMap<>();
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
	private List<TdhMj> leftMajiangs = new ArrayList<>();
	/**
	 * 当前房间所有玩家信息map
	 */
	private Map<Long, TdhMjPlayer> playerMap = new ConcurrentHashMap<Long, TdhMjPlayer>();
	/**
	 * 座位对应的玩家信息MAP
	 */
	private Map<Integer, TdhMjPlayer> seatMap = new ConcurrentHashMap<Integer, TdhMjPlayer>();
	/**
	 * 胡确认信息
	 */
	private Map<Integer, Integer> huConfirmMap = new HashMap<>();
	/**
	 * 玩家位置对应临时操作 当同时存在多个可做的操作时 1当优先级最高的玩家最先操作 则清理所有操作提示 并执行该操作
	 * 2当操作优先级低的玩家先选择操作，则玩家先将所做操作存入临时操作中 当玩家优先级最高的玩家操作后 在判断临时操作是否可执行并执行
	 */
	private Map<Integer, TdhMjTempAction> tempActionMap = new ConcurrentHashMap<>();
	/**
	 * 抓鸟
	 */
	private int birdNum;

	// /**
	// * 计算鸟的算法 1：中鸟+1 2：中鸟翻倍 3：中鸟加倍
	// */
	// private int calcBird;
	/**
	 * 是否飘分 1飘分 0不飘分
	 */
	private int kePiao;

	/**
	 * 摸麻将的seat
	 */
	private int moMajiangSeat;
	/**
	 * 摸杠的麻将
	 */
	private TdhMj moGang;
	/**
	 * 杠出来的麻将
	 */
	private TdhMj gangMajiang;
	/**
	 * 摸杠胡
	 */
	private List<Integer> moGangHuList = new ArrayList<>();
	/**
	 * 杠后出的两张牌
	 */
	private List<TdhMj> gangDisMajiangs = new ArrayList<>();
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
	private TdhMj lastMajiang;
	/**
	 *
	 */
	private int disEventAction;

	/*** GPS预警 */
	// private int gpsWarn = 0;

	/*** 明杠过不补 */
	private int gangBubu = 0;
	/*** 将将胡自摸 */
	private int jiangHuZiMo = 0;
	/*** 清一色可吃 */
	private int qingyiseChi = 0;
	/*** 跟张不点炮 */
	private int genZhangBuPao = 0;
	/*** 碰碰胡接炮不算将将胡 */
	private int pengpengHuJiePao = 0;

	/*** 需要展示牌的玩家座位号 */
	private List<Integer> showMjSeat = new ArrayList<>();

	private int tableStatus;// 特殊状态 1飘分

	/*** 杠打色子 **/
	private int gangDice = -1;

	/*** 摸屁股的座标号 */
	private List<Integer> moTailPai = new ArrayList<>();

	/** 杠后摸的两张牌中被要走的 **/
	private TdhMj gangActedMj = null;

	/** 是否是开局 **/
	private boolean isBegin = false;

	private int dealDice;

	// 是否加倍：0否，1是
	private int jiaBei;
	// 加倍分数：低于xx分进行加倍
	private int jiaBeiFen;
	// 加倍倍数：翻几倍
	private int jiaBeiShu;

	/** 托管1：单局，2：全局 */
	private int autoPlayGlob;
	private int autoTableCount;

	private int isAutoPlay;// 托管时间

	private int readyTime = 0;

	private int dahuDifen;//大胡底分
	private int topFen;// 胡分数限制

	

	
	
	
	@Override
	protected boolean quitPlayer1(Player player) {
		return false;
	}

	@Override
	public boolean canQuit(Player player) {
		if (super.canQuit(player)) {
			return getTableStatus() != TdhMjConstants.TABLE_STATUS_PIAO;
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

		}

		if (zhuaNiao && birdNum == 1) {

			// 海底
			if (leftMajiangs.size() == 0) {
				birdMjIds = zhuaNiao(lastMajiang);
			} else {
				// 先砸鸟
				birdMjIds = zhuaNiao(null);
			}
			int birdSeat = 0;

			TdhMjPlayer huPlayer = seatMap.get(winSeatList.get(0));
			if (winSeatList.size() > 1 || (huPlayer.getDahu() != null && (huPlayer.getDahu().contains(9)||huPlayer.getDahu().contains(4)))) {
				birdSeat = disCardSeat;
			} else {
				birdSeat = winSeatList.get(0);
			}
			seatBirds = birdToSeat(birdMjIds, birdSeat);
			for (int seat : seatMap.keySet()) {
				int birdNum = calcBirdNum(seatBirds, seat);
				seatBirdMap.put(seat, birdNum);

			}

		} else {
			for (int seat : seatMap.keySet()) {
				seatBirdMap.put(seat, 0);
			}
		}

		// 算胡的
		if (winSeatList.size() != 0) {
			// 先判断是自摸还是放炮
			TdhMjPlayer winPlayer = null;
			if (winSeatList.size() == 1) {
				winPlayer = seatMap.get(winSeatList.get(0));
				if ((winPlayer.isAlreadyMoMajiang() || winPlayer.isGangshangHua())
						&& winSeatList.get(0) == moMajiangSeat) {
					selfMo = true;
				}
			}
			// 庄家
			if (selfMo) {
				winPlayer.changeActionTotal(TdhMjAction.ZIMO_COUNT, 1);
				calZiMoPoint(seatBirdMap, winPlayer, true, selfMo);
				winPlayer.changeAction(7, 1);
				winPlayer.getMyExtend().setMjFengshen(FirstmythConstants.firstmyth_index8, 1);
			} else {

				TdhMjPlayer losePlayer = seatMap.get(disCardSeat);

				TdhMjPlayer huPlayer = seatMap.get(winSeatList.get(0));
				// 杠上炮
				if (huPlayer.getDahu() != null && (huPlayer.getDahu().contains(9) || huPlayer.getDahu().contains(4))) {
					checkGSPHu(winSeatList, seatBirdMap, losePlayer);
				} else {
					commonPaoHu(winSeatList, seatBirdMap, losePlayer);

				}

				// 未放炮，也未接炮的玩法，杠分显示
				for (TdhMjPlayer p : seatMap.values()) {
					if (!winSeatList.contains(p.getSeat()) && p.getSeat() != losePlayer.getSeat()) {
						p.changePointArr(2, p.getLostPoint());
					}
				}
			}

		}

		for (TdhMjPlayer seat : seatMap.values()) {
			seat.changePoint(seat.getLostPoint());
			if (flow) {
				seat.changePointArr(2, seat.getLostPoint());
			}
		}

		boolean over = playBureau == totalBureau;

		if(autoPlayGlob >0) {
			// //是否解散
			boolean diss = false;
			if(autoPlayGlob ==1) {
				for (TdhMjPlayer seat : seatMap.values()) {
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

		// 不管流局都加分
		ClosingMjInfoRes.Builder res = sendAccountsMsg(over, selfMo, winSeatList, birdMjIds, seatBirds, seatBirdMap,
				false, startseat, catchBirdSeat);
		// 没有流局
		if (!flow) {
			if (winSeatList.size() > 1) {
				// 一炮多响设置放炮的人为庄家
				setLastWinSeat(disCardSeat);
			} else {
				setLastWinSeat(winSeatList.get(0));
			}

		} else {
			if (moLastMajiangSeat != 0) {
				// 流局有人要了海底，要的人当庄
				setLastWinSeat(moLastMajiangSeat);

			} else if (fristLastMajiangSeat != 0) {
				// 流局了没有人要海底，第一个可以选择的人当庄
				setLastWinSeat(fristLastMajiangSeat);

			}

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

		for (TdhMjPlayer player : seatMap.values()) {
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
		for (TdhMjPlayer seat : seatMap.values()) {
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

	private void commonPaoHu(List<Integer> winSeatList, Map<Integer, Integer> seatBirdMap, TdhMjPlayer losePlayer) {
		TdhMjPlayer winPlayer;
		int loserSeat = losePlayer.getSeat();
		losePlayer.getMyExtend().setMjFengshen(FirstmythConstants.firstmyth_index10, winSeatList.size());
		int totalLosePoint = 0;

		int totalHuPoint = 0;
		int totalGangPoint = losePlayer.getLostPoint();
		int totalPiaoPoint = 0;
		losePlayer.changeActionTotal(TdhMjAction.DIANPAO_COUNT, 1);
		for (int winSeat : winSeatList) {
			// 胡牌
			winPlayer = seatMap.get(winSeat);
			int daHuCount = winPlayer.getDahuPointCount();
			// 底分
			int winPoint = 0;
			if (daHuCount > 0) {
				winPoint = calcDaHuPoint(daHuCount);
				winPlayer.changeActionTotal(TdhMjAction.DAHU_COUNT, 1);
				// winPlayer.changeActionTotal(TdhMjAction.ACTION_COUNT_DAHU_JIEPAO,1);
			} else {
				winPoint = 1;
				winPlayer.changeActionTotal(TdhMjAction.XIAOHU_COUNT, 1);
			}

			int totalBirdNum = seatBirdMap.get(winSeat) + seatBirdMap.get(loserSeat);

			winPoint = calcBirdPoint(winPoint, totalBirdNum);

			if(topFen==1&&winPoint>80){
				winPoint = 80;
			}
			totalHuPoint += winPoint;
			winPlayer.changePointArr(0, winPoint);
			// 飘分
			if (kePiao >= 1) {
				int piaoPoint = (winPlayer.getPiaoPoint() + losePlayer.getPiaoPoint());
				winPoint += piaoPoint;
				totalPiaoPoint += piaoPoint;
				winPlayer.changePointArr(3, piaoPoint);
			}
			int gangPoint = winPlayer.getLostPoint();
			totalLosePoint += winPoint;
			winPlayer.changeAction(6, 1);
			losePlayer.changeAction(0, 1);
			winPlayer.changeLostPoint(winPoint);
			winPlayer.changePointArr(2, gangPoint);

		}

		losePlayer.changePointArr(0, -totalHuPoint);
		losePlayer.changePointArr(2, totalGangPoint);
		losePlayer.changePointArr(3, -totalPiaoPoint);
		losePlayer.changeLostPoint(-totalLosePoint);
	}

	private void checkGSPHu(List<Integer> winSeatList, Map<Integer, Integer> seatBirdMap, TdhMjPlayer losePlayer) {

		int totalLosePoint = 0;
		int totalHuPoint = 0;
		int totalGangPoint = losePlayer.getLostPoint();
		int totalPiaoPoint = 0;
		losePlayer.changeActionTotal(TdhMjAction.DIANPAO_COUNT, 1);

		HashMap<Integer, HashSet<Integer>> huMap = new HashMap<Integer, HashSet<Integer>>();
		for (int winSeat : winSeatList) {
			// 胡牌
			TdhMjPlayer player = seatMap.get(winSeat);
			// 杠分
			player.changePointArr(2, player.getLostPoint());

			List<Integer> huMjIds = player.getHuMjIds();
			if (huMjIds != null && huMjIds.size() > 0) {
				for (Integer mjId : huMjIds) {
					HashSet<Integer> set = huMap.get(mjId);
					if (set == null) {
						set = new HashSet<Integer>();
						huMap.put(mjId, set);
					}
					set.add(winSeat);
				}
			}
		}

		List<TdhMj> cards = new ArrayList<>(losePlayer.getHandMajiang());
		List<TdhMj> huCards = TdhMjTool.getTingMjs(cards, this, losePlayer, true);

		int dahuSize = 0;
		for (TdhMj mj : huCards) {
			cards.add(mj);
			TdhMjiangHu hu = TdhMjTool.isHuTuiDaoHu(cards, this, losePlayer, mj, true);
			if (hu.getDahuSize() > dahuSize) {
				dahuSize = hu.getDahuSize();
			}
			cards.remove(mj);
		}

		dahuSize += 1;

		for (Map.Entry<Integer, HashSet<Integer>> entry : huMap.entrySet()) {
			HashSet<Integer> winSeat = entry.getValue();
			int losePoint = calcDaHuPoint(dahuSize);

			int bird = seatBirdMap.get(losePlayer.getSeat());
			int losePoint2 = losePoint;
			losePoint *= (maxPlayerCount - 1);
			if (bird > 0) {
				losePoint *= 2;
			} else {
				if (winSeat.size() != maxPlayerCount - 1) {
					losePoint += losePoint2;
				}
			}

			
//			totalHuPoint += losePoint;
//			totalLosePoint += losePoint;

			int point2 = losePoint / (winSeat.size());
			for (Integer win : winSeat) {
				TdhMjPlayer wplayer = seatMap.get(win);
				wplayer.changeLostPoint(point2);
				int bird2 = seatBirdMap.get(win);

				if (bird2 > 0 && (winSeat.size() == maxPlayerCount - 1)) {
					wplayer.changeLostPoint(losePoint2);
//					totalHuPoint += losePoint2;
//					totalLosePoint += losePoint2;
				}
			}
		}

		for (int winSeat : winSeatList) {
			// 胡牌
			TdhMjPlayer winPlayer = seatMap.get(winSeat);
			int[] pointArr = winPlayer.getPointArr();
			int huPoint = winPlayer.getLostPoint() - pointArr[2];
			

			if(topFen==1&&huPoint>80){
				huPoint = 80;
			}
			totalHuPoint += huPoint;
			totalLosePoint += huPoint;
			
			winPlayer.changePointArr(0, huPoint);
			winPlayer.setLostPoint(huPoint+ pointArr[2]);
			// 飘分
			if (kePiao >= 1) {
				int piaoPoint = (winPlayer.getPiaoPoint() + losePlayer.getPiaoPoint());
				totalPiaoPoint += piaoPoint;
				totalLosePoint += piaoPoint;
				winPlayer.changeLostPoint(piaoPoint);
				winPlayer.changePointArr(3, piaoPoint);
			}

		}

		losePlayer.changePointArr(0, -totalHuPoint);
		losePlayer.changePointArr(2, totalGangPoint);
		losePlayer.changePointArr(3, -totalPiaoPoint);
		losePlayer.changeLostPoint(-totalLosePoint);

	}

	private void calZiMoPoint(Map<Integer, Integer> seatBirdMap, TdhMjPlayer winPlayer, boolean dahu, boolean zimo) {
		int daHuCount = 0;
		if (dahu) {
			daHuCount = winPlayer.getDahuPointCount();
		}
		int winSeat = winPlayer.getSeat();

		int winPoint = 0;
		// 底分
		if (daHuCount > 0) {
			//检查刚上花
		List<Integer> daHuCount2 =	winPlayer.getDahuPointCount2();
		
		if(daHuCount2.size()>1){
			for(Integer count : daHuCount2){
				winPoint += calcDaHuPoint(count);
			}
		}else {
			winPoint = calcDaHuPoint(daHuCount);
		}
		
			
			winPlayer.changeActionTotal(TdhMjAction.DAHU_COUNT, 1);
		} else {
			winPlayer.changeActionTotal(TdhMjAction.XIAOHU_COUNT, 1);
			winPoint = 1;
		}

		// 自摸
		int totalHuPoint = 0;
		int totalPiaoPoint = 0;

		int winBirdNum = seatBirdMap.get(winSeat) == null ? 0 : seatBirdMap.get(winSeat);
		int totalWinPoint = 0;
		for (int loserSeat : seatMap.keySet()) {
			// 除了赢家的其他人
			if (winSeat == loserSeat) {
				continue;
			}

			TdhMjPlayer loser = seatMap.get(loserSeat);
			int losePoint = winPoint;

			int gangPoint = loser.getLostPoint();
			int piaoPoint = (loser.getPiaoPoint() + winPlayer.getPiaoPoint());

			int totalBirdNum = winBirdNum + seatBirdMap.get(loserSeat);
			// 输家鸟分
			losePoint = calcBirdPoint(losePoint, totalBirdNum);
			
			if(topFen==1&&losePoint>80){
				losePoint = 80;
			}
			int huPoint = losePoint;
			// 飘分
			if (dahu) {
				if (kePiao >= 1) {
					losePoint += (loser.getPiaoPoint() + winPlayer.getPiaoPoint());
				}
			}
			totalHuPoint += huPoint;
			totalPiaoPoint += piaoPoint;
			totalWinPoint += losePoint;
			loser.changeLostPoint(-losePoint);
			loser.changePointArr(0, -huPoint);
			loser.changePointArr(2, gangPoint);
			loser.changePointArr(3, -piaoPoint);
		}

		winPlayer.changePointArr(0, totalHuPoint);
		winPlayer.changePointArr(2, winPlayer.getLostPoint());
		winPlayer.changePointArr(3, totalPiaoPoint);
		winPlayer.changeLostPoint(totalWinPoint);
	}

	/**
	 * 计算鸟分加成
	 *
	 * @param point
	 * @param bird
	 * @return
	 */
	private int calcBirdPoint(int point, int bird) {
		// if (bird <= 0) {
		// return point;
		// }
		// if (calcBird == 1&& addBirdPoint) {
		//
		// // 加分最后结算
		// point = point + bird;
		// } else if (calcBird == 2) {
		// // 翻倍是2的bird次方
		// point = (int) (point * (Math.pow(2, bird)));
		//
		// } else if (calcBird == 3) {
		// // 加倍
		point *= (bird + 1);
		// }
		return point;
	}

	/**
	 * 计算大胡
	 * 
	 * @return
	 */
	private int calcDaHuPoint(int daHuCount) {
		int point = dahuDifen;
		point = (int) (point * (Math.pow(2, daHuCount - 1)));

		return point;
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
		for (TdhMjPlayer player : playerMap.values()) {
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
		// for (TdhMjPlayer player : playerMap.values()) {
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
	private int[] zhuaNiao(TdhMj lastMaj) {
		// 先砸鸟
		int realBirdNum = leftMajiangs.size() > birdNum ? birdNum : leftMajiangs.size();

		if (realBirdNum == 0) {
			realBirdNum = birdNum;
		}
		int[] bird = new int[realBirdNum];
		for (int i = 0; i < realBirdNum; i++) {
			TdhMj prickbirdMajiang = null;
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
	 * @param bankerSeat
	 * @return
	 */
	private int[] birdToSeat(int[] prickBirdMajiangIds, int bankerSeat) {
		int[] seatArr = new int[prickBirdMajiangIds.length];
		for (int i = 0; i < prickBirdMajiangIds.length; i++) {
			TdhMj majiang = TdhMj.getMajang(prickBirdMajiangIds[i]);
			int prickbirdPai = majiang.getPai();

			int prickbirdseat = 0;
			prickbirdPai = (prickbirdPai - 1) % maxPlayerCount;// 从自己开始算 所以减1
			prickbirdseat = prickbirdPai + bankerSeat > maxPlayerCount ? prickbirdPai + bankerSeat - maxPlayerCount
					: prickbirdPai + bankerSeat;

			// if(maxPlayerCount==4) {
			// prickbirdPai = (prickbirdPai - 1) % 4;// 从自己开始算 所以减1
			// prickbirdseat = prickbirdPai + bankerSeat > 4 ? prickbirdPai +
			// bankerSeat - 4 : prickbirdPai + bankerSeat;
			// }else if(maxPlayerCount==3){
			// //鸟不落空
			// //4-8 空鸟
			// if(prickbirdPai ==1 ||prickbirdPai==5 || prickbirdPai==9) {
			// prickbirdseat = bankerSeat;
			// }else if(prickbirdPai ==2 || prickbirdPai ==6) {
			// //庄下家
			// prickbirdseat = (bankerSeat%3)+1;
			// }else if(prickbirdPai ==3 || prickbirdPai ==7) {
			// //庄上家
			// prickbirdseat = ((bankerSeat%3)+1)%3+1;
			// }
			// }else {
			// if(prickbirdPai ==1 ||prickbirdPai==5 || prickbirdPai==9) {
			// prickbirdseat = bankerSeat;
			// }else if(prickbirdPai ==3 || prickbirdPai ==7) {
			// prickbirdseat = (bankerSeat%2)+1;
			// }
			//
			//// //两人 2468 空鸟
			//// if(prickbirdPai%2==0) {
			//// continue;
			//// }prickbirdseat = (bankerSeat%3)+1;
			////
			//// prickbirdseat = bankerSeat;
			//
			// }

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
				tempMap.put("nowDisCardIds", StringUtil.implode(TdhMjHelper.toMajiangIds(nowDisCardIds), ","));
			}
			if (tempMap.containsKey("leftPais")) {
				tempMap.put("leftPais", StringUtil.implode(TdhMjHelper.toMajiangIds(leftMajiangs), ","));
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
	// for (TdhMjPlayer player : seatMap.values()) {
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
		addPlayLog(disCardRound + "_" + lastWinSeat + "_" + TdhMjDisAction.action_dice + "_" + dealDice);
		setDealDice(dealDice);
		logFaPaiTable();

		for (TdhMjPlayer tablePlayer : seatMap.values()) {
			DealInfoRes.Builder res = DealInfoRes.newBuilder();
			List<Integer> actionList = tablePlayer.checkMo(null, true);
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
			if (tablePlayer.isAutoPlay()) {
				addPlayLog(getDisCardRound() + "_" + tablePlayer.getSeat() + "_" + TdhMjConstants.action_tuoguan + "_"
						+ 1);
			}
			tablePlayer.setAutoMD(0);
			sendTingInfo(tablePlayer);

		}
		isBegin = true;
		// 没有操作的话通知庄家出牌
		TdhMjPlayer bankPlayer = seatMap.get(lastWinSeat);
		ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.req_com_kt_ask_dismajiang);// 推送庄家出牌
		bankPlayer.writeSocket(com.build());
		// isBegin = false;
	}

	/**
	 * 摸牌
	 *
	 * @param player
	 */
	public void moMajiang(TdhMjPlayer player, boolean isBuzhang) {
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
		if (leftMajiangs.size() == 0) {
			calcOver();
			return;
		}

		// 如果只剩下一张牌 问要不要
		if (getLeftMajiangCount() == 1 && !isBuzhang) {
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
		TdhMj majiang = null;
		if (disCardRound != 0) {
			// 玩家手上的牌是双数，已经摸过牌了
			if (player.isAlreadyMoMajiang()) {
				return;
			}
			if (GameServerConfig.isDebug() && !player.isRobot()) {
				if (zpMap.containsKey(player.getUserId()) && zpMap.get(player.getUserId()) > 0) {
					majiang = TdhMjQipaiTool.findMajiangByVal(leftMajiangs, zpMap.get(player.getUserId()));
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
			addPlayLog(disCardRound + "_" + player.getSeat() + "_" + TdhMjDisAction.action_moMjiang + "_"
					+ majiang.getId());
			player.moMajiang(majiang);
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
			// 如果杠了之后，摸牌不能杠，那有杠也不能杠&& !checkSameMj(player.getPeng(), majiang)
			if (!player.getGang().isEmpty() ) {
				// arr.set(TdhMjAction.MINGGANG, 0);
				// arr.set(TdhMjAction.ANGANG, 0);
				arr.set(TdhMjAction.BUZHANG, 0);
				arr.set(TdhMjAction.BUZHANG_AN, 0);
			}
			coverAddActionSeat(player.getSeat(), arr);
		}
		MoMajiangRes.Builder res = MoMajiangRes.newBuilder();
		res.setUserId(player.getUserId() + "");
		res.setRemain(getLeftMajiangCount());
		res.setSeat(player.getSeat());

		// boolean playCommand = !player.getGang().isEmpty() && arr.isEmpty();
		logMoMj(player, majiang, arr);
		for (TdhMjPlayer seat : seatMap.values()) {
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
	}

	private boolean checkSameMj(List<TdhMj> list, TdhMj majiang) {
		if (list.size() == 0) {
			return false;
		}
		for (TdhMj mj : list) {
			if (mj.getVal() == majiang.getVal()) {
				return true;
			}
		}
		return false;
	}

	public void calcMoLastSeats(int firstSeat) {
		for (int i = 0; i < getMaxPlayerCount(); i++) {
			TdhMjPlayer player = seatMap.get(firstSeat);
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
		TdhMjPlayer player = seatMap.get(sendSeat);
		sendMoLast(player, 1);
	}

	private void buildPlayRes(PlayMajiangRes.Builder builder, Player player, int action, List<TdhMj> majiangs) {
		TdhMjResTool.buildPlayRes(builder, player, action, majiangs);
		buildPlayRes1(builder);
	}

	private void buildPlayRes1(PlayMajiangRes.Builder builder) {
		// builder
	}

	/**
	 * 如果是起手判断是否还有人可胡小胡，检查庄家发牌后有没有操作，没有的话通知庄家出牌
	 */
	public void checkBegin(TdhMjPlayer player) {
		boolean isBegin = isBegin();
		if (isBegin) {
			TdhMjPlayer bankPlayer = seatMap.get(lastWinSeat);
			List<Integer> actList = bankPlayer.checkMo(null, isBegin);
			if (!actList.isEmpty()) {
				PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
				buildPlayRes(builder, player, TdhMjDisAction.action_pass, new ArrayList<>());
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
	private void hu(TdhMjPlayer player, List<TdhMj> majiangs, int action) {
		if (!actionSeatMap.containsKey(player.getSeat())) {
			return;
		}
		if (huConfirmMap.containsKey(player.getSeat())) {
			return;
		}

		checkRemoveMj(player, action);
		boolean zimo = player.isAlreadyMoMajiang();
		TdhMj disMajiang = null;
		TdhMjiangHu huBean = null;
		List<TdhMj> huMjs = new ArrayList<>();
		int fromSeat = 0;
		boolean isGangShangHu = false;
		if (!zimo) {
			if (moGangHuList.contains(player.getSeat())) {// 强杠胡
				disMajiang = moGang;
				fromSeat = moMajiangSeat;
				huMjs.add(moGang);
			} else if (isHasGangAction(player.getSeat())) {// 杠上炮 杠上花
				fromSeat = moMajiangSeat;
				Map<Integer, TdhMjiangHu> huMap = new HashMap<>();
				List<Integer> daHuMjIds = new ArrayList<>();
				List<Integer> huMjIds = new ArrayList<>();
				for (int majiangId : gangSeatMap.keySet()) {
					TdhMjiangHu temp = player.checkHu(TdhMj.getMajang(majiangId), disCardRound == 0);
					if (!temp.isHu()) {
						continue;
					}
					temp.initDahuList();
					huMap.put(majiangId, temp);
					huMjIds.add(majiangId);
					if (temp.isDahu()) {
						daHuMjIds.add(majiangId);
					}
				}
				if (daHuMjIds.size() > 0) {
					// 有大胡
					for (int mjId : huMjIds) {
						TdhMjiangHu temp = huMap.get(mjId);
						if (moMajiangSeat == player.getSeat()) {
							temp.setGangShangHua(true);
							isGangShangHu = true;
						} else {
							temp.setGangShangPao(true);
						}
						temp.initDahuList();
						if (huBean == null) {
							huBean = temp;
						} else {
							huBean.addToDahu(temp.getDahuList());
						}
						player.addHuMjId(mjId);
						huMjs.add(TdhMj.getMajang(mjId));
					}
				} else if (huMjIds.size() > 0) {
					// 没有大胡
					for (int mjId : huMjIds) {
						TdhMjiangHu temp = huMap.get(mjId);
						if (moMajiangSeat == player.getSeat()) {
							temp.setGangShangHua(true);
							isGangShangHu = true;
						} else {
							temp.setGangShangPao(true);
						}
						temp.initDahuList();
						if (huBean == null) {
							huBean = temp;
						} else {
							huBean.addToDahu(temp.getDahuList());
						}
						player.addHuMjId(mjId);
						huMjs.add(TdhMj.getMajang(mjId));
					}
				} else {
					huBean = new TdhMjiangHu();
				}

				if (huBean.isHu()) {
					if (disCardSeat == player.getSeat()) {
						zimo = true;
					}
				}

			} else if (lastMajiang != null) {
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
				player.addHuMjId(lastMajiang.getId());

			} else if (!nowDisCardIds.isEmpty()) {
				disMajiang = nowDisCardIds.get(0);
				fromSeat = disCardSeat;
				huMjs.add(disMajiang);
			}
		} else {
			huMjs.add(player.getHandMajiang().get(player.getHandMajiang().size() - 1));
		}
		// if(isBegin()){
		// //天胡
		// if(huBean == null){
		// huBean = player.checkHu(null,true);
		// }
		// if(huBean.isHu()){
		// huBean.setTianhu(true);
		// huBean.initDahuList();
		// }
		// }else if(nowDisCardSeat == lastWinSeat &&
		// seatMap.get(lastWinSeat).getOutPais().size() == 0){
		// //地胡
		// if(huBean == null){
		// huBean = player.checkHu(lastMajiang,true);
		// }
		// if(huBean.isHu()){
		// huBean.setDihu(true);
		// huBean.initDahuList();
		// }
		// }

		if (huBean == null) {
			// 自摸
			huBean = player.checkHu(disMajiang, disCardRound == 0);
			if (huBean.isHu() && lastMajiang != null) {
				huBean.setHaidilaoyue(true);
				huBean.initDahuList();
			}
		}
		if (!huBean.isHu()) {
			// 检测抢杠胡
			if (moGangHuList.contains(player.getSeat())) {
				List<Integer> hu = player.checkDisMaj(disMajiang, false);
				if (!hu.isEmpty() && hu.get(0) == 1) {
					huBean.setHu(true);
				}
			}
			if (!huBean.isHu()) {
				return;
			}
		}

		// 没出牌就有人胡了，天胡
		// if(disCardRound==0) {
		// huBean.setTianhu(true);
		// huBean.initDahuList();
		// }else if(disCardRound==1&&player.getSeat()!= moMajiangSeat) {
		// huBean.setDihu(true);
		// huBean.initDahuList();
		// }

		// 算牌型的分
		if (moGangHuList.contains(player.getSeat())) {
			// 补张的时候不算抢杠胡
			// if (disEventAction != TdhMjDisAction.action_buzhang) {
			huBean.setQGangHu(true);
			huBean.initDahuList();
			// }
			// 抢杠胡
			TdhMjPlayer moGangPlayer = seatMap.get(nowDisCardSeat);
			int majCount = getLeftMajiangCount();
			//海底时杠上炮的位置
			if(majCount==1&&moGangPlayer != null){
				int fSeat = calcFrontSeat(moGangPlayer.getSeat());
				moGangPlayer = seatMap.get(fSeat);
			}
			
			if (moGangPlayer == null) {
				moGangPlayer = seatMap.get(moMajiangSeat);
			}
			List<TdhMj> moGangMajiangs = new ArrayList<>();
			moGangMajiangs.add(moGang);
			if (huBean.isQGangHu()) {
				moGangPlayer.removeGangMj(moGangMajiangs.get(0));
			}
			// else {
			// moGangPlayer.addOutPais(moGangMajiangs, 0,0);
			// }
			// 摸杠被人胡了 相当于自己出了一张牌
			recordDisMajiang(moGangMajiangs, moGangPlayer);
			addPlayLog(disCardRound + "_" + player.getSeat() + "_" + 0 + "_" + TdhMjHelper.toMajiangStrs(majiangs));
		}
		if (huBean.getDahuPoint() > 0) {
			player.setDahu(huBean.getDahuList());
		}

		if (isGangShangHu) {
			// 杠上花，只胡一张牌时，另外一张牌需要打出
			List<TdhMj> gangDisMajiangs = getGangDisMajiangs();
			List<TdhMj> chuMjs = new ArrayList<>();
			if (gangDisMajiangs != null && gangDisMajiangs.size() > 0) {
				for (TdhMj mj : gangDisMajiangs) {
					if (!huMjs.contains(mj)) {
						chuMjs.add(mj);
					}
				}
			}
			if (chuMjs != null) {
				PlayMajiangRes.Builder chuPaiMsg = PlayMajiangRes.newBuilder();
				buildPlayRes(chuPaiMsg, player, TdhMjDisAction.action_chupai, chuMjs);
				chuPaiMsg.setFromSeat(-1);
				broadMsgToAll(chuPaiMsg.build());
				player.addOutPais(chuMjs, TdhMjDisAction.action_chupai, player.getSeat());
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
		for (TdhMjPlayer seat : seatMap.values()) {
			// 推送消息
			seat.writeSocket(builder.build());
		}
		// 加入胡牌数组
		addHuList(player.getSeat(), disMajiang == null ? 0 : disMajiang.getId());
		addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + TdhMjHelper.toMajiangStrs(huMjs) + "_"
				+ StringUtil.implode(player.getDahu(), ","));
		if (isCalcOver()) {
			// 等待别人胡牌 如果都确认完了，胡
			calcOver();
		} else {
			player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip);
		}
	}

	/**
	 * 找出拥有这张麻将的玩家
	 *
	 * @param majiang
	 * @return
	 */
	private TdhMjPlayer getPlayerByHasMajiang(TdhMj majiang) {
		for (TdhMjPlayer player : seatMap.values()) {
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
		// 起牌小胡可以继续打牌
		if (!huActionList.isEmpty()) {
			over = true;
			TdhMjPlayer moGangPlayer = null;
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
					if (moGangPlayer.getSeat() == huseat) {
						continue;
					}
				}
				if (!huConfirmMap.containsKey(huseat)) {
					over = false;
					break;
				}
			}
		}

		if (!over) {
			TdhMjPlayer disCSMajiangPlayer = seatMap.get(disCardSeat);
			for (int huseat : huActionList) {
				if (huConfirmMap.containsKey(huseat)) {
					if (disCardRound == 0) {
						// 天胡
						removeActionSeat(huseat);
					}
					continue;
				}
				PlayMajiangRes.Builder disBuilder = PlayMajiangRes.newBuilder();
				TdhMjPlayer seatPlayer = seatMap.get(huseat);
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
	private void chiPengGang(TdhMjPlayer player, List<TdhMj> majiangs, int action) {
		List<Integer> actionList0 = actionSeatMap.get(player.getSeat());
		if(actionList0==null ||actionList0.isEmpty()){
			return;
		}
		

		PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
		logAction(player, action, majiangs, null);
		// if (nowDisCardIds.size() > 1 && !isHasGangAction()) {
		// // 当前出牌不能操作
		// return;
		// }
		List<Integer> huList = getHuSeatByActionMap();
		huList.remove((Object) player.getSeat());
		// if (!huList.isEmpty()) {
		// // 胡最优先
		// return;
		// }

		// 处理杠完可吃可碰又可以胡，吃碰的话那就等于过胡了
		if (nowDisCardIds.size() > 1) {
			for (TdhMj mj : nowDisCardIds) {
				List<Integer> hu = player.checkDisMaj(mj, false);
				if (!hu.isEmpty() && hu.get(0) == 1) {
					// && (actionList.get(TdhMjAction.HU) == 1)
					List<Integer> actionList = actionSeatMap.get(player.getSeat());
					if (actionList != null) {
						actionList.set(TdhMjAction.HU, 0);
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

		List<TdhMj> handMajiang = new ArrayList<>(player.getHandMajiang());
		TdhMj disMajiang = null;
		if (isHasGangAction()) {
			for (int majiangId : gangSeatMap.keySet()) {
				if (action == TdhMjDisAction.action_chi) {
					List<Integer> majiangIds = TdhMjHelper.toMajiangIds(majiangs);
					if (majiangIds.contains(majiangId)) {
						disMajiang = TdhMj.getMajang(majiangId);
						gangActedMj = disMajiang;
						handMajiang.add(disMajiang);
						if (majiangs.size() > 1) {
							majiangs.remove(disMajiang);
						}
						break;
					}
				} else {
					TdhMj mj = TdhMj.getMajang(majiangId);
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
			sameCount = TdhMjHelper.getMajiangCount(majiangs, majiangs.get(0).getVal());
		}
		if (action == TdhMjDisAction.action_buzhang) {
			if (sameCount == 0) {
				majiangs.add(disMajiang);
			}
			majiangs = TdhMjHelper.getMajiangList(player.getHandMajiang(), majiangs.get(0).getVal());
			sameCount = majiangs.size();
			if (sameCount == 0) {
				majiangs.add(disMajiang);
			}
		} else if (action == TdhMjDisAction.action_minggang) {
			if (majiangs.size() == 0) {
				majiangs.add(disMajiang);
			}
			// 如果是杠 后台来找出是明杠还是暗杠
			majiangs = TdhMjHelper.getMajiangList(player.getHandMajiang(), majiangs.get(0).getVal());
			sameCount = majiangs.size();
			if (sameCount == 4) {
				// 有4张一样的牌是暗杠
				action = TdhMjDisAction.action_angang;
			} else if (sameCount == 0) {
				majiangs.add(disMajiang);
			}
			// 其他是明杠

		} else if (action == TdhMjDisAction.action_buzhang_an) {
			// 暗杠补张
			majiangs = TdhMjHelper.getMajiangList(player.getHandMajiang(), majiangs.get(0).getVal());
			sameCount = majiangs.size();
		}
		// /////////////////////
		if (action == TdhMjDisAction.action_chi) {
			boolean can = canChi(player, player.getHandMajiang(), majiangs, disMajiang);
			if (!can) {
				return;
			}
		} else if (action == TdhMjDisAction.action_peng) {
			boolean can = canPeng(player, majiangs, sameCount, disMajiang);
			if (!can) {
				return;
			}
		} else if (action == TdhMjDisAction.action_angang) {
			boolean can = canAnGang(player, majiangs, sameCount, action);
			if (!can) {
				player.writeErrMsg("不能杠此张牌");
				return;
			}
			if (!player.isTingPai(majiangs.get(0).getVal())) {
				player.writeErrMsg("不能杠此张牌");
				return;
			}
		} else if (action == TdhMjDisAction.action_minggang) {
			boolean can = canMingGang(player, player.getHandMajiang(), majiangs, sameCount, disMajiang);
			if (!can) {
				player.writeErrMsg("不能杠此张牌");
				return;
			}
			if (!player.isTingPai(majiangs.get(0).getVal())) {
				player.writeErrMsg("不能杠此张牌");
				return;
			}
			// 特殊处理一张牌明杠的时候别人可以 胡
			// if (sameCount == 1 && canGangHu()) {
			// if (checkQGangHu(player, majiangs, action)) {
			// // return;
			// moMj = false;
			// }
			// }
		} else if (action == TdhMjDisAction.action_buzhang) {
			boolean can = false;
			if (sameCount == 4) {
				can = canAnGang(player, majiangs, sameCount, action);
			} else {
				can = canMingGang(player, player.getHandMajiang(), majiangs, sameCount, disMajiang);
			}
			
			if (disMajiang!=null) {
				List<Integer> pengList = TdhMjHelper.toMajiangVals(player.getPeng());
				//补的牌是打的牌 并且补碰是不行的
				if(pengList.contains(majiangs.get(0).getVal())&&majiangs.get(0).getVal()==disMajiang.getVal()){
					return;
				}
			}
			if (!can) {
				return;
			}
			// 特殊处理一张牌明杠的时候别人可以胡
			if (sameCount == 1 && canGangHu()) {
				if (checkQGangHu(player, majiangs, action)) {
					// 抢杠胡可以杠下来
					// return;
					moMj = false;
				}
			}
		} else if (action == TdhMjDisAction.action_buzhang_an) {
			boolean can = false;
			if (sameCount == 4) {
				can = canAnGang(player, majiangs, sameCount, action);
			}
			if (!can) {
				return;
			}
		} else {
			return;
		}

		if (moMj) {
			calcPoint(player, action, sameCount, majiangs);
		}
		boolean disMajiangMove = false;
		if (disMajiang != null) {
			// 碰或者杠
			if (action == TdhMjDisAction.action_minggang && sameCount == 3) {
				// 接杠
				disMajiangMove = true;
			} else if (action == TdhMjDisAction.action_chi) {
				// 吃
				disMajiangMove = true;
			} else if (action == TdhMjDisAction.action_peng) {
				// 碰
				disMajiangMove = true;
			} else if (action == TdhMjDisAction.action_buzhang && sameCount == 3) {
				// 自己三张补张
				disMajiangMove = true;
			}
		}
		if (disMajiangMove) {
			if (action == TdhMjDisAction.action_chi) {
				majiangs.add(1, disMajiang);// 吃的牌放第二位
			} else {
				majiangs.add(disMajiang);
			}
			builder.setFromSeat(disCardSeat);
			List<TdhMj> disMajiangs = new ArrayList<>();
			disMajiangs.add(disMajiang);
			seatMap.get(disCardSeat).removeOutPais(disMajiangs, action);
		}
		chiPengGang(builder, player, majiangs, action, moMj);
	}

	private void chiPengGang(PlayMajiangRes.Builder builder, TdhMjPlayer player, List<TdhMj> majiangs, int action,
			boolean moMj) {
		setIsBegin(false);
		processHideMj(player);
		List<Integer> actionList = actionSeatMap.get(player.getSeat());
		if (action == TdhMjDisAction.action_peng
				&& (actionList.get(TdhMjAction.MINGGANG) == 1 || actionList.get(TdhMjAction.BUZHANG) == 1)) {
			// 可以碰也可以杠
			player.addPassGangVal(majiangs.get(0).getVal());
		}

		player.addOutPais(majiangs, action, disCardSeat);
		buildPlayRes(builder, player, action, majiangs);
		List<Integer> removeActList = removeActionSeat(player.getSeat());
		clearGangActionMap();
		if (moMj) {
			clearActionSeatMap();
		}

		addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + TdhMjHelper.toMajiangStrs(majiangs));
		// 不是普通出牌
		setNowDisCardSeat(player.getSeat());
		checkClearGangDisMajiang();
		if (action == TdhMjDisAction.action_chi || action == TdhMjDisAction.action_peng) {
			List<Integer> arr = player.checkMo(null, false);
			// 吃碰之后还有操作
			if (!arr.isEmpty()) {
				arr.set(TdhMjAction.ZIMO, 0);
				arr.set(TdhMjAction.HU, 0);
				addActionSeat(player.getSeat(), arr);
			}
		}
		for (TdhMjPlayer seatPlayer : seatMap.values()) {
			// 推送消息
			PlayMajiangRes.Builder copy = builder.clone();
			if (actionSeatMap.containsKey(seatPlayer.getSeat())) {
				copy.addAllSelfAct(actionSeatMap.get(seatPlayer.getSeat()));
			}
			seatPlayer.writeSocket(copy.build());
		}

		// 取消漏炮
		player.setPassMajiangVal(0);
		int majCount = getLeftMajiangCount();
		if (action == TdhMjDisAction.action_minggang || action == TdhMjDisAction.action_angang) {

			if (majCount == 1) {
				setNowDisCardSeat(calcNextSeat(player.getSeat()));
				checkMo();
			} else {

				// 明杠和暗杠摸牌
				if (moMj) {
					gangMoMajiang(player, majiangs.get(0), action);
				}
			}

		} else if (action == TdhMjDisAction.action_buzhang) {
			if (majCount == 1) {
				setNowDisCardSeat(calcNextSeat(player.getSeat()));
				checkMo();
//				setDisCardSeat(player.getSeat());
			} else {
				// 补张
				if (moMj) {
					moMajiang(player, true);
				}
			}
		} else if (action == TdhMjDisAction.action_buzhang_an) {
			if (majCount == 1) {
				setNowDisCardSeat(calcNextSeat(player.getSeat()));
				checkMo();
			} else {
				// 补张
				moMajiang(player, true);
			}

		}

		if (action == TdhMjDisAction.action_chi || action == TdhMjDisAction.action_peng) {
			sendTingInfo(player);
		}

		setDisEventAction(action);
		robotDealAction();
		logAction(player, action, majiangs, removeActList);
	}

	/**
	 * 杠后摸两张牌
	 */
	private void gangMoMajiang(TdhMjPlayer player, TdhMj gangMajiang, int action) {
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
		List<TdhMj> moList = new ArrayList<>();
		Random r = new Random();
		gangDice = (r.nextInt(6) + 1) * 10 + (r.nextInt(6) + 1);
		int leftMjCount = getLeftMajiangCount();
		int leftDuo = leftMjCount % 2 == 0 ? leftMjCount / 2 : leftMjCount / 2 + 1;
//		if (leftDuo >= gangDice / 10 + gangDice % 10) {
			if (GameServerConfig.isDeveloper()) {
				TdhMj majiang1 = TdhMjHelper.findMajiangByVal(leftMajiangs, 34);
				if (majiang1 != null) {
					leftMajiangs.remove(majiang1);
					moList.add(majiang1);
				}
				TdhMj majiang2 = TdhMjHelper.findMajiangByVal(leftMajiangs, 35);
				if (majiang2 != null) {
					leftMajiangs.remove(majiang2);
					moList.add(majiang2);
				}
			}

			int mjCount = getLeftMajiangCount();
			
			if(mjCount==1){
				mjCount=0;
			}else if(mjCount==2){
				mjCount =1;
			}else {
				mjCount =2;
			}
			
			
			while (moList.size() < mjCount) {
				TdhMj majiang = getLeftMajiang();
				if (majiang != null) {
					moList.add(majiang);
				} else {
					break;
				}
			}
			addMoTailPai(gangDice);
//		}

		addPlayLog(disCardRound + "_" + player.getSeat() + "_" + TdhMjDisAction.action_moGangMjiang + "_" + gangDice
				+ "_" + TdhMjHelper.implodeMajiang(moList, ","));

		// 检查摸牌
		clearActionSeatMap();
		clearGangActionMap();
		// 打出这两张牌
		setDisCardSeat(player.getSeat());
		setGangDisMajiangs(moList);
		setMoMajiangSeat(player.getSeat());
		player.setPassMajiangVal(0);

		setGangMajiang(gangMajiang);
		setNowDisCardSeat(calcNextSeat(player.getSeat()));
		// setNowDisCardSeat(player.getSeat());
		setNowDisCardIds(moList);
		// player.addOutPais(moList, 0,player.getSeat());
		// /////////////////////////////////////////////////////////////////////////////////////////

		boolean canHu = false;
		TdhMj moGangMj = null;
		// 摸了牌后可以胡牌
		for (TdhMj majiang : moList) {
			for (TdhMjPlayer seatPlayer : seatMap.values()) {
				List<Integer> actionList = seatPlayer.checkDisMaj(majiang, false);
				if (seatPlayer.getSeat() == player.getSeat()) {

					int gangvalue = TdhMjAction.hasGang(actionList);
					// 摸杠人只能胡
					if (TdhMjAction.hasHu(actionList)) {

						actionList = TdhMjAction.keepHu(actionList);
						actionList.set(TdhMjAction.HU, 0);
						actionList.set(TdhMjAction.ZIMO, 1);
						//checkMAgang(majiang, seatPlayer, actionList, gangvalue);
						if (gangvalue > 0) {
							moGangMj = majiang;
						}
						canHu = true;
						addActionSeat(player.getSeat(), actionList);
						List<Integer> list2 = new ArrayList<Integer>(actionList);
						addGangActionSeat(majiang.getId(), player.getSeat(), list2);
						// addGangActionSeat(majiang.getId(), player.getSeat(),
						// actionList);
						logAction(seatPlayer, action, Arrays.asList(majiang), actionList);
					} else if (gangvalue > 0) {
						actionList = TdhMjAction.keepHu(actionList);

						//checkMAgang(majiang, seatPlayer, actionList, gangvalue);
						if (gangvalue > 0) {
							moGangMj = majiang;
						}
						addActionSeat(player.getSeat(), actionList);
						List<Integer> list2 = new ArrayList<Integer>(actionList);
//						addGangActionSeat(majiang.getId(), player.getSeat(), list2);
						logAction(seatPlayer, action, Arrays.asList(majiang), actionList);
					}
				} else {
					if (TdhMjAction.hasHu(actionList)) {
						actionList = TdhMjAction.keepHu(actionList);
						actionList.set(TdhMjAction.HU, 1);

						addActionSeat(seatPlayer.getSeat(), actionList);
						List<Integer> list2 = new ArrayList<Integer>(actionList);
						addGangActionSeat(majiang.getId(), seatPlayer.getSeat(), list2);
						logAction(seatPlayer, action, Arrays.asList(majiang), actionList);
					}
				}
			}
		}

//		if (moGangMj != null) {
//			player.moMajiang(moGangMj);
//		}
		if (isHasGangAction(player.getSeat())) {
			if (!canHu) {
				gangNoticePlayer(player, gangMajiang, moList);
				for (TdhMj moMj : moList) {
					Map<Integer, List<Integer>> seatActionList = gangSeatMap.get(moMj.getId());
					if (seatActionList != null && seatActionList.containsKey(player.getSeat())) {
						continue;
					}
					List<TdhMj> list = new ArrayList<>();
					list.add(moMj);
					checkMoOutCard(list, player, action);

					// List<TdhMj> list = new ArrayList<>();
					// list.add(moMj);
					// player.addOutPais(list, 0,player.getSeat());
					// addPlayLog(disCardRound + "_" + player.getSeat() + "_" +
					// 0 + "_" + TdhMjHelper.toMajiangStrs(list));
					// logAction(player, action, list, null);
					// PlayMajiangRes.Builder chuPaiMsg =
					// PlayMajiangRes.newBuilder();
					// buildPlayRes(chuPaiMsg, player,
					// TdhMjDisAction.action_chupai, list);
					// for (TdhMjPlayer seatPlayer : seatMap.values()) {
					// chuPaiMsg.setFromSeat(-1);
					// seatPlayer.writeSocket(chuPaiMsg.build());
					// }
				}
			} else {
				// 自己的胡操作
			//	gangNoticePlayer(player, gangMajiang, moList);
				
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
				for (TdhMj moMj : moList) {
					GangPlayMajiangRes.Builder playerMsg = GangPlayMajiangRes.newBuilder();
					playerMsg.setMajiangId(moMj.getId());
					Map<Integer, List<Integer>> seatActionList = gangSeatMap.get(moMj.getId());

					if (seatActionList != null && seatActionList.containsKey(player.getSeat())) {
						playerMsg.addAllSelfAct(seatActionList.get(player.getSeat()));
					}
					gangMsg.addGangActs(playerMsg);
				}
				player.writeSocket(gangMsg.build());

				gangMsg.clearGangActs();
				for (TdhMj moMj : moList) {
					GangPlayMajiangRes.Builder playerMsg = GangPlayMajiangRes.newBuilder();
					playerMsg.setMajiangId(moMj.getId());
					gangMsg.addGangActs(playerMsg);
				}
			
				for (TdhMjPlayer seatPlayer : seatMap.values()) {
					if (player.getSeat() != seatPlayer.getSeat()) {
						//
						seatPlayer.writeSocket(gangMsg.build());
						// 开杠人能胡，必胡，去掉其他人的所有操作
						removeActionSeat(seatPlayer.getSeat());
					}
				}
			}

		} else {
			// 自己打出两牌
			player.addOutPais(moList, 0, player.getSeat());
			if (moList.size() > 0) {
				addPlayLog(disCardRound + "_" + player.getSeat() + "_" + 0 + "_" + TdhMjHelper.toMajiangStrs(moList));
			}
			gangNoticePlayer(player, gangMajiang, moList);

			PlayMajiangRes.Builder chuPaiMsg = PlayMajiangRes.newBuilder();
			buildPlayRes(chuPaiMsg, player, TdhMjDisAction.action_chupai, moList);
			for (TdhMjPlayer seatPlayer : seatMap.values()) {
				chuPaiMsg.setFromSeat(-1);
				seatPlayer.writeSocket(chuPaiMsg.build());
			}
			broadMsgRoomPlayer(chuPaiMsg.build());

			if (isHasGangAction()) {
				// 如果有人能做动作
				robotDealAction();
			} else {
				checkMo();
			}
		}

	}

	private void checkRemoveMj(TdhMjPlayer player, int action) {
		// TdhMj mjA = null;
		TdhMj mjB = null;
		for (int majiangId2 : gangSeatMap.keySet()) {
			Map<Integer, List<Integer>> map = gangSeatMap.get(majiangId2);
			List<Integer> actList = map.get(player.getSeat());
			if (actList == null) {
				continue;
			}
			// if (actList.get(TdhMjAction.ZIMO) == 1) {
			// mjA = TdhMj.getMajang(majiangId2);
			// } else

			if (actList.get(TdhMjAction.MINGGANG) == 1 || actList.get(TdhMjAction.ANGANG) == 1) {
				mjB = TdhMj.getMajang(majiangId2);
			}
		}

		if (mjB != null) {
			// if(mjA.getId() !=mjB.getId()) {
			// 从手牌移除掉
			List<TdhMj> list = new ArrayList<>();
			list.add(mjB);
			checkMoOutCard(list, player, action);
			// }
		}
	}

	private void checkMAgang(TdhMj majiang, TdhMjPlayer seatPlayer, List<Integer> actionList, int gangvalue) {
		if (gangvalue > 0) {
			// seatPlayer.moMajiang(majiang);
			if (gangvalue == 1) {
				actionList.set(TdhMjAction.MINGGANG, 1);
			} else if (gangvalue == 2) {
				actionList.set(TdhMjAction.ANGANG, 1);
			}
		}
	}

	private void gangNoticePlayer(TdhMjPlayer player, TdhMj gangMajiang, List<TdhMj> moList) {
		// 发送摸牌消息res
		GangMoMajiangRes.Builder gangMsg = null;
		for (TdhMjPlayer seatPlayer : seatMap.values()) {
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
			for (TdhMj majiang : moList) {
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

	private boolean checkQGangHu(TdhMjPlayer player, List<TdhMj> majiangs, int action) {
		PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
		Map<Integer, List<Integer>> huListMap = new HashMap<>();
		for (TdhMjPlayer seatPlayer : seatMap.values()) {
			if (seatPlayer.getUserId() == player.getUserId()) {
				continue;
			}
			// 推送消息
			List<Integer> hu = seatPlayer.checkDisMaj(majiangs.get(0), false);
			hu = TdhMjAction.keepHu(hu);
			if (!hu.isEmpty() && hu.get(0) == 1) {
				addActionSeat(seatPlayer.getSeat(), hu);
				huListMap.put(seatPlayer.getSeat(), hu);
			}
		}

		// 可以胡牌
		if (!huListMap.isEmpty()) {
			setDisEventAction(action);
			setMoGang(majiangs.get(0), new ArrayList<>(huListMap.keySet()));
			buildPlayRes(builder, player, action, majiangs);
			for (Entry<Integer, List<Integer>> entry : huListMap.entrySet()) {
				PlayMajiangRes.Builder copy = builder.clone();
				TdhMjPlayer seatPlayer = seatMap.get(entry.getKey());
				copy.addAllSelfAct(entry.getValue());
				seatPlayer.writeSocket(copy.build());
			}
			return true;
		}
		return false;

	}

	public void checkSendGangRes(Player player) {
		if (isHasGangAction()) {
			List<TdhMj> moList = getGangDisMajiangs();
			TdhMjPlayer disPlayer = seatMap.get(disCardSeat);
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
			for (TdhMj mj : moList) {
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
	private void chuPai(TdhMjPlayer player, List<TdhMj> majiangs, int action) {
		if (majiangs.size() != 1) {
			return;
		}
		if (!player.isAlreadyMoMajiang()) {
			// 还没有摸牌
			return;
		}
		if (!tempActionMap.isEmpty()) {
			LogUtil.e(player.getName() + "出牌清理临时操作！");
			clearTempAction();
		}
		if (!player.getGang().isEmpty()) {
			// 已经杠过了牌
			if (player.getLastMoMajiang().getId() != majiangs.get(0).getId()) {
				return;
			}
		}
		if (!actionSeatMap.isEmpty() && player.getGang().isEmpty()) {// 出牌自动过掉手上操作
			guo(player, null, TdhMjDisAction.action_pass);
		}
		if (!actionSeatMap.isEmpty() && player.getGang().isEmpty()) {
			player.writeComMessage(WebSocketMsgType.res_code_mj_dis_err, action, majiangs.get(0).getId());
			return;
		}
		PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
		buildPlayRes(builder, player, action, majiangs);
		// 普通出牌
		clearActionSeatMap();
		clearGangActionMap();
		checkClearGangDisMajiang();
		setNowDisCardSeat(calcNextSeat(player.getSeat()));
		recordDisMajiang(majiangs, player);
		player.addOutPais(majiangs, action, player.getSeat());
		player.clearPassHu();
		logAction(player, action, majiangs, null);
		for (TdhMjPlayer seat : seatMap.values()) {
			List<Integer> list = new ArrayList<>();
			if (seat.getUserId() != player.getUserId()) {
				list = seat.checkDisMajiang(majiangs.get(0));
				if (list.contains(1)) {
					// 如果杠了之后，別人出的牌不能做杠操作
					if (!seat.getGang().isEmpty()) {
						// list.set(TdhMjAction.MINGGANG, 0);
						// list.set(TdhMjAction.ANGANG, 0);
						list.set(TdhMjAction.BUZHANG, 0);
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
		addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + TdhMjHelper.toMajiangStrs(majiangs));
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
			if (actionList.get(TdhMjAction.HU) == 1 || actionList.get(TdhMjAction.ZIMO) == 1) {
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
			for (TdhMjPlayer seatPlayer : seatMap.values()) {
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
			for (TdhMjPlayer seat : seatMap.values()) {
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

	private void err(TdhMjPlayer player, int action, String errMsg) {
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
	public synchronized void playCommand(TdhMjPlayer player, List<TdhMj> majiangs, int action) {
		if (!moGangHuList.isEmpty()) {// 被人抢杠胡
			if (!moGangHuList.contains(player.getSeat())) {
				return;
			}
		}

		if (TdhMjDisAction.action_hu == action) {
			hu(player, majiangs, action);
			return;
		}
		// 手上没有要出的麻将
		if (!isHasGangAction() && action != TdhMjDisAction.action_minggang && action != TdhMjDisAction.action_buzhang)
			if (!player.getHandMajiang().containsAll(majiangs)) {
				err(player, action, "没有找到出的牌" + majiangs);
				return;
			}
		changeDisCardRound(1);
		if (action == TdhMjDisAction.action_pass) {
			guo(player, majiangs, action);
		} else if (action == TdhMjDisAction.action_moMjiang) {
		} else if (action != 0) {
			chiPengGang(player, majiangs, action);
		} else {

			chuPai(player, majiangs, action);
		}

	}

	/**
	 * 最后一张牌(海底捞)
	 *
	 * @param player
	 * @param action
	 */
	public synchronized void moLastMajiang(TdhMjPlayer player, int action) {
		if (getLeftMajiangCount() != 1) {
			return;
		}
		if (player.getSeat() != askLastMajaingSeat) {
			return;
		}

		if (action == TdhMjDisAction.action_passmo) {
			// 发送下一个海底摸牌res
			sendMoLast(player, 0);
			removeMoLastSeat(player.getSeat());
			if (moLastSeats == null || moLastSeats.size() == 0) {
				calcOver();
				return;
			}
			sendAskLastMajiangRes(0);
			addPlayLog(disCardRound + "_" + player.getSeat() + "_" + TdhMjDisAction.action_pass + "_");
		} else {
			sendMoLast(player, 0);
			clearMoLastSeat();
			clearActionSeatMap();
			setMoLastMajiangSeat(player.getSeat());
			TdhMj majiang = getLeftMajiang();
			addPlayLog(disCardRound + "_" + player.getSeat() + "_" + TdhMjDisAction.action_moLastMjiang + "_"
					+ majiang.getId());
			setMoMajiangSeat(player.getSeat());
			player.setPassMajiangVal(0);
			setLastMajiang(majiang);
			setDisCardSeat(player.getSeat());

			// /////////////////////////////////////////////
			// 发送海底捞的牌

			// /////////////////////////////////////////

			List<TdhMj> disMajiangs = new ArrayList<>();
			disMajiangs.add(majiang);

			MoMajiangRes.Builder moRes = MoMajiangRes.newBuilder();
			moRes.setUserId(player.getUserId() + "");
			moRes.setRemain(getLeftMajiangCount());
			moRes.setSeat(player.getSeat());

			// 先看看自己能不能胡
			List<Integer> selfActList = player.checkDisMaj(majiang, false);
			player.moMajiang(majiang);
			selfActList = TdhMjAction.keepHu(selfActList);
			if (selfActList != null && !selfActList.isEmpty()) {
				if (selfActList.contains(1)) {
					addActionSeat(player.getSeat(), selfActList);
				}
			}
			for (TdhMjPlayer seatPlayer : seatMap.values()) {
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
			if (TdhMjAction.hasHu(selfActList)) {
				// 优先自己胡
				// hu(player, null, TdhMjDisAction.action_moLastMjiang_hu);
				return;
			} else {
				chuLastPai(player);
			}
			// for (int seat : actionSeatMap.keySet()) {
			// hu(seatMap.get(seat), null, action);
			// }
		}

	}

	private void chuLastPai(TdhMjPlayer player) {
		TdhMj majiang = lastMajiang;
		List<TdhMj> disMajiangs = new ArrayList<>();
		disMajiangs.add(majiang);
		PlayMajiangRes.Builder chuRes = TdhMjResTool.buildPlayRes(player, TdhMjDisAction.action_chupai, disMajiangs);
		addPlayLog(disCardRound + "_" + player.getSeat() + "_" + TdhMjDisAction.action_chupai + "_"
				+ TdhMjHelper.toMajiangStrs(disMajiangs));
		setNowDisCardIds(disMajiangs);
		setNowDisCardSeat(calcNextSeat(player.getSeat()));
		recordDisMajiang(disMajiangs, player);
		player.addOutPais(disMajiangs, TdhMjDisAction.action_chupai, player.getSeat());
		player.clearPassHu();
		for (TdhMjPlayer seatPlayer : seatMap.values()) {
			if (seatPlayer.getUserId() == player.getUserId()) {
				seatPlayer.writeSocket(chuRes.clone().build());
				continue;
			}
			List<Integer> otherActList = seatPlayer.checkDisMaj(majiang, false);
			otherActList = TdhMjAction.keepHu(otherActList);
			PlayMajiangRes.Builder msg = chuRes.clone();
			if (TdhMjAction.hasHu(otherActList)) {
				addActionSeat(seatPlayer.getSeat(), otherActList);
				msg.addAllSelfAct(otherActList);
			}
			seatPlayer.writeSocket(msg.build());
		}
		if (actionSeatMap.isEmpty()) {
			calcOver();
		}
	}

	private void passMoHu(TdhMjPlayer player, List<TdhMj> majiangs, int action) {
		if (!moGangHuList.contains(player.getSeat())) {
			return;
		}

		PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
		buildPlayRes(builder, player, action, majiangs);
		builder.setSeat(nowDisCardSeat);
		removeActionSeat(player.getSeat());
		player.writeSocket(builder.build());
		addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + TdhMjHelper.toMajiangStrs(majiangs));
		if (isCalcOver()) {
			calcOver();
			return;
		}

		if (nowDisCardIds.size() > 0) {
			player.setPassMajiangVal(nowDisCardIds.get(0).getVal());
		} else if (majiangs != null && majiangs.size() > 0) {
			player.setPassMajiangVal(majiangs.get(0).getVal());
		}

		TdhMjPlayer moGangPlayer = seatMap.get(moMajiangSeat);
		if (moGangHuList.isEmpty()) {
			majiangs = new ArrayList<>();
			majiangs.add(moGang);
			if (disEventAction == TdhMjDisAction.action_buzhang) {
				moMajiang(moGangPlayer, true);
			} else {
				gangMoMajiang(moGangPlayer, majiangs.get(0), disEventAction);
			}

			calcPoint(moGangPlayer, TdhMjDisAction.action_minggang, 1, majiangs);
			// builder = PlayMajiangRes.newBuilder();
			// chiPengGang(builder, moGangPlayer, majiangs,
			// TdhMjDisAction.action_minggang,true);
		}

	}

	/**
	 * guo
	 *
	 * @param player
	 * @param majiangs
	 * @param action
	 */
	private void guo(TdhMjPlayer player, List<TdhMj> majiangs, int action) {
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
		
		
		List<Integer> removeActionList = removeActionSeat(player.getSeat());
		logAction(player, action, majiangs, removeActionList);
		boolean isBegin = isBegin();

		if (moLastMajiangSeat == player.getSeat()) {
			// 摸海底可以胡的人点过，将海底牌打出
			chuLastPai(player);
			return;
		}

		if (removeActionList.get(TdhMjAction.MINGGANG) == 1 || removeActionList.get(TdhMjAction.BUZHANG) == 1) {
			// 过杠
			Map<Integer, Integer> pengMap = TdhMjHelper.toMajiangValMap(player.getPeng());
			for (TdhMj handMajiang : player.getHandMajiang()) {
				if (pengMap.containsKey(handMajiang.getVal())) {
					player.addPassGangVal(handMajiang.getVal());
					break;
				}
			}
		}

		checkClearGangDisMajiang();
		PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
		buildPlayRes(builder, player, action, majiangs);
		builder.setSeat(nowDisCardSeat);
		player.writeSocket(builder.build());
		addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + TdhMjHelper.toMajiangStrs(majiangs));
		if (isCalcOver()) {
			calcOver();
			return;
		}
		if (TdhMjAction.hasHu(removeActionList) && disCardSeat != player.getSeat() && nowDisCardIds.size() == 1) {
			// 漏炮
			player.passHu(nowDisCardIds.get(0).getVal());
		}

		// nowDisCardIds.size() == 1
		if (removeActionList.get(0) == 1 && disCardSeat != player.getSeat()) {
			if (nowDisCardIds.size() > 1) {
				for (TdhMj mj : nowDisCardIds) {
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
			TdhMjPlayer disCSMajiangPlayer = seatMap.get(disCardSeat);
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
				TdhMjPlayer seatPlayer = seatMap.get(seat);
				seatPlayer.writeSocket(copy.build());
			}
		}

		if (player.isAlreadyMoMajiang() && !player.getGang().isEmpty() && actionSeatMap.get(player.getSeat()) == null) {
			// 杠牌后自动出牌
			List<TdhMj> disMjiang = new ArrayList<>();
			disMjiang.add(player.getLastMoMajiang());
			if (isHasGangAction()) {
				checkMoOutCard(disMjiang, player, action);
			} else {
				chuPai(player, disMjiang, 0);
			}
		}

		if (isBegin && player.getSeat() == lastWinSeat) {
			// 庄家过非小胡，提示庄家出牌
			ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.req_com_kt_ask_dismajiang);
			player.writeSocket(com.build());
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

	private void logPassTime(TdhMjPlayer player) {
		StringBuilder sb = new StringBuilder();
		sb.append("TdhMj");
		sb.append("|").append(getId());
		sb.append("|").append(getPlayBureau());
		sb.append("|").append(player.getUserId());
		sb.append("|").append(player.getSeat());
		sb.append("|").append("pass");
		sb.append("|").append(System.currentTimeMillis() - player.getLastPassTime());
		sb.append("|").append(actionSeatMap.get(player.getSeat()));
		LogUtil.msg(sb.toString());
	}

	private void checkMoOutCard(List<TdhMj> list, TdhMjPlayer player, int action) {

		player.addOutPais(list, 0, player.getSeat());
		if (list.size() > 0) {
			addPlayLog(disCardRound + "_" + player.getSeat() + "_" + 0 + "_" + TdhMjHelper.toMajiangStrs(list));
		}
		logAction(player, action, list, null);
		PlayMajiangRes.Builder chuPaiMsg = PlayMajiangRes.newBuilder();
		buildPlayRes(chuPaiMsg, player, TdhMjDisAction.action_chupai, list);
		for (TdhMjPlayer seatPlayer : seatMap.values()) {
			chuPaiMsg.setFromSeat(-1);
			seatPlayer.writeSocket(chuPaiMsg.build());
		}
	}

	private void calcPoint(TdhMjPlayer player, int action, int sameCount, List<TdhMj> majiangs) {
		int lostPoint = 0;
		int getPoint = 0;
		int[] seatPointArr = new int[getMaxPlayerCount() + 1];
		if (action == TdhMjDisAction.action_peng) {
			return;

		} else if (action == TdhMjDisAction.action_angang || action == TdhMjDisAction.action_buzhang_an) {
			// 暗杠相当于自摸每人出2分
			lostPoint = -2;
			getPoint = 2 * (getMaxPlayerCount() - 1);
			player.changeActionTotal(TdhMjAction.ANGANG_COUNT, 1);

		} else if (action == TdhMjDisAction.action_minggang || action == TdhMjDisAction.action_buzhang) {
			if (sameCount == 1) {
				// 碰牌之后再抓一个牌每人出1分
				// 放杠的人出3分

				if (player.isPassGang(majiangs.get(0))) {
					// 特殊处理 可以碰可以杠的牌 选择了碰 再杠不算分
					return;
				}
				lostPoint = -1;
				getPoint = 1 * (getMaxPlayerCount() - 1);
				player.changeActionTotal(TdhMjAction.MING_GANG_COUNT, 1);
			} else if (sameCount == 3) {
				// 放杠
				TdhMjPlayer disPlayer = seatMap.get(disCardSeat);
				// disPlayer.getMyExtend().setMjFengshen(FirstmythConstants.firstmyth_index13,
				// 1);
				int point = (getMaxPlayerCount() - 1);
				disPlayer.changeLostPoint(-point);
				seatPointArr[disPlayer.getSeat()] = -point;
				player.changeLostPoint(point);
				seatPointArr[player.getSeat()] = point;
			}
		}

		if (lostPoint != 0) {
			for (TdhMjPlayer seat : seatMap.values()) {
				if (seat.getUserId() == player.getUserId()) {
					player.changeLostPoint(getPoint);
					seatPointArr[player.getSeat()] = getPoint;
				} else {
					seat.changeLostPoint(lostPoint);
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
	}

	private void recordDisMajiang(List<TdhMj> majiangs, TdhMjPlayer player) {
		setNowDisCardIds(majiangs);
		setDisCardSeat(player.getSeat());
	}

	public List<TdhMj> getNowDisCardIds() {
		return nowDisCardIds;
	}

	public void setDisEventAction(int disAction) {
		this.disEventAction = disAction;
		changeExtend();
	}

	public void setNowDisCardIds(List<TdhMj> nowDisCardIds) {
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
				TdhMjPlayer player = seatMap.get(seat);
				if (player != null && player.isRobot()) {
					// 如果是机器人可以直接决定
					List<Integer> actionList = actionSeatMap.get(seat);
					if (actionList == null) {
						continue;
					}
					List<TdhMj> list = new ArrayList<>();
					if (!nowDisCardIds.isEmpty()) {
						list = TdhMjQipaiTool.getVal(player.getHandMajiang(), nowDisCardIds.get(0).getVal());
					}
					if (actionList.get(0) == 1) {
						// 胡
						playCommand(player, new ArrayList<TdhMj>(), TdhMjDisAction.action_hu);

					} else if (actionList.get(3) == 1) {
						playCommand(player, list, TdhMjDisAction.action_angang);

					} else if (actionList.get(2) == 1) {
						playCommand(player, list, TdhMjDisAction.action_minggang);

					} else if (actionList.get(1) == 1) {
						playCommand(player, list, TdhMjDisAction.action_peng);

					} else if (actionList.get(4) == 1) {
						playCommand(player, player.getCanChiMajiangs(nowDisCardIds.get(0)), TdhMjDisAction.action_chi);

					} else {
						System.out.println("---------->" + JacksonUtil.writeValueAsString(actionList));
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
			int nextseat = getNextActionSeat();
			TdhMjPlayer next = seatMap.get(nextseat);
			if (next != null && next.isRobot()) {
				List<Integer> actionList = actionSeatMap.get(next.getSeat());
				if (actionList != null) {
					List<TdhMj> list = null;
					if (actionList.get(0) == 1) {
						// 胡
						playCommand(next, new ArrayList<TdhMj>(), TdhMjDisAction.action_hu);

					} else if (actionList.get(3) == 1) {
						// 机器人暗杠
						Map<Integer, Integer> handMap = TdhMjHelper.toMajiangValMap(next.getHandMajiang());
						for (Entry<Integer, Integer> entry : handMap.entrySet()) {
							if (entry.getValue() == 4) {
								// 可以暗杠
								list = TdhMjHelper.getMajiangList(next.getHandMajiang(), entry.getKey());
							}
						}
						playCommand(next, list, TdhMjDisAction.action_angang);

					} else if (actionList.get(5) == 1) {
						// 机器人补张
						Map<Integer, Integer> handMap = TdhMjHelper.toMajiangValMap(next.getHandMajiang());
						for (Entry<Integer, Integer> entry : handMap.entrySet()) {
							if (entry.getValue() == 4) {
								// 可以补张
								list = TdhMjHelper.getMajiangList(next.getHandMajiang(), entry.getKey());
							}
						}
						if (list == null) {
							if (next.isAlreadyMoMajiang()) {
								list = TdhMjQipaiTool.getVal(next.getHandMajiang(), next.getLastMoMajiang().getVal());

							} else {
								list = TdhMjQipaiTool.getVal(next.getHandMajiang(), nowDisCardIds.get(0).getVal());
								list.add(nowDisCardIds.get(0));
							}
						}

						playCommand(next, list, TdhMjDisAction.action_buzhang);

					} else if (actionList.get(2) == 1) {
						Map<Integer, Integer> pengMap = TdhMjHelper.toMajiangValMap(next.getPeng());
						for (TdhMj handMajiang : next.getHandMajiang()) {
							if (pengMap.containsKey(handMajiang.getVal())) {
								// 有碰过
								list = new ArrayList<>();
								list.add(handMajiang);
								playCommand(next, list, TdhMjDisAction.action_minggang);
								break;
							}
						}

					} else if (actionList.get(1) == 1) {
						// playCommand(next, list, TdhMjDisAction.action_peng);

					} else if (actionList.get(4) == 1) {
						TdhMj majiang = null;
						List<TdhMj> chiList = null;
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
										majiang = TdhMj.getMajang(majiangId);
										chiList = next.getCanChiMajiangs(majiang);
										chiList.add(majiang);
										break;
									}

								}

							}

						}

						playCommand(next, chiList, TdhMjDisAction.action_chi);

					} else {
						System.out.println("!!!!!!!!!!" + JacksonUtil.writeValueAsString(actionList));

					}

				} else {
					int maJiangId = TdhMjRobotAI.getInstance().outPaiHandle(0, next.getHandPais(),
							new ArrayList<Integer>());
					List<TdhMj> majiangList = TdhMjHelper.toMajiang(Arrays.asList(maJiangId));
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
			// 第一局丢筛子
			int masterseat = playerMap.get(masterId).getSeat();
			setLastWinSeat(masterseat);
		}
		setDisCardSeat(lastWinSeat);
		setNowDisCardSeat(lastWinSeat);
		setMoMajiangSeat(lastWinSeat);

		List<Integer> copy = null;
		copy = new ArrayList<>(TdhMjConstants.zhuanzhuan_mjList);

		// List<Integer> copy = new
		// ArrayList<>(TdhMjConstants.zhuanzhuan_mjList);
		addPlayLog(copy.size() + "");
		List<List<TdhMj>> list;
		if (zp == null) {
			list = TdhMjTool.fapai(copy, getMaxPlayerCount());
		} else {
			list = TdhMjTool.fapai(copy, getMaxPlayerCount(), zp);
		}
		int i = 1;
		List<Integer> removeIndex = new ArrayList<>();
		for (TdhMjPlayer player : playerMap.values()) {
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

		// 桌上剩余的牌
		List<TdhMj> leftMjs = new ArrayList<>();
		// 没有发出去的牌退回剩余牌中
		for (int j = 0; j < list.size(); j++) {
			if (!removeIndex.contains(j)) {
				leftMjs.addAll(list.get(j));
			}
		}
		setLeftMajiangs(leftMjs);
	}

	/**
	 * 初始化桌子上剩余牌
	 *
	 * @param leftMajiangs
	 */
	public void setLeftMajiangs(List<TdhMj> leftMajiangs) {
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
	public TdhMj getLeftMajiang() {
		if (this.leftMajiangs.size() > 0) {
			TdhMj majiang = this.leftMajiangs.remove(0);
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
	
    /**
	 * 计算seat前面的座位
	 *
	 * @param seat
	 * @return
	 */
    public int calcFrontSeat(int seat) {
        int frontSeat = seat - 1 < 1 ? maxPlayerCount : seat - 1;
        return frontSeat;
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
		res.addExt(birdNum); // 2抓鸟
		// res.addExt(gpsWarn); //
		res.addExt(gangBubu); // 3过杠不补
		res.addExt(jiangHuZiMo); // 4将将胡自摸
		res.addExt(qingyiseChi); // 5清一色可吃
		res.addExt(genZhangBuPao); // 6跟张不掉跑
		res.addExt(pengpengHuJiePao); // 7碰碰胡接炮
		res.addExt(kePiao); // 8飘分
		res.addExt(isBegin() ? 1 : 0); // 9 是否开始
		
		

		System.out.println(" isbegin ====== " + isBegin());

		res.addStrExt(StringUtil.implode(moTailPai, ",")); // 0
		res.setDealDice(dealDice);
		res.setRenshu(getMaxPlayerCount());
		if (leftMajiangs != null) {
			res.setRemain(leftMajiangs.size());
		} else {
			res.setRemain(0);
		}
		List<PlayerInTableRes> players = new ArrayList<>();
		for (TdhMjPlayer player : playerMap.values()) {
			PlayerInTableRes.Builder playerRes = player.buildPlayInTableInfo(isrecover);
			if (player.getUserId() == userId) {
				playerRes.addAllHandCardIds(player.getHandPais());
			}

			if (player.getSeat() == disCardSeat && nowDisCardIds != null) {
				playerRes.addAllOutCardIds(TdhMjHelper.toMajiangIds(nowDisCardIds));
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
	}

	private Map<Integer, TdhMjTempAction> loadTempActionMap(String json) {
		Map<Integer, TdhMjTempAction> map = new ConcurrentHashMap<>();
		if (json == null || json.isEmpty())
			return map;
		JSONArray jsonArray = JSONArray.parseArray(json);
		for (Object val : jsonArray) {
			String str = val.toString();
			TdhMjTempAction tempAction = new TdhMjTempAction();
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
		addPlayLog(disCardRound + "_" + seat + "_" + TdhMjDisAction.action_hasAction + "_"
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
			addPlayLog(disCardRound + "_" + seat + "_" + TdhMjDisAction.action_hasAction + "_" + StringUtil.implode(a));
		} else {
			actionSeatMap.put(seat, actionlist);
			addPlayLog(disCardRound + "_" + seat + "_" + TdhMjDisAction.action_hasAction + "_"
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
			nowDisCardIds = TdhMjHelper.toMajiang(StringUtil.explodeToIntList(info.getNowDisCardIds()));
		}

		if (!StringUtils.isBlank(info.getLeftPais())) {
			leftMajiangs = TdhMjHelper.toMajiang(StringUtil.explodeToIntList(info.getLeftPais()));
		}

	}

	// @Override
	// public void initExtend(String info) {
	// if (StringUtils.isBlank(info)) {
	// return;
	// }
	// JsonWrapper wrapper = new JsonWrapper(info);
	// for (TdhMjPlayer player : seatMap.values()) {
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
	private boolean canChi(TdhMjPlayer player, List<TdhMj> handMajiang, List<TdhMj> majiangs, TdhMj disMajiang) {
		if (!actionSeatMap.containsKey(player.getSeat())) {
			return false;
		}

		if (qingyiseChi != 1) {
			return false;
		}

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

		List<TdhMj> chi = TdhMjTool.checkChi(majiangs, disMajiang);
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
	private boolean canPeng(TdhMjPlayer player, List<TdhMj> majiangs, int sameCount, TdhMj disMajiang) {
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
	 * 是否能暗杠
	 *
	 * @param player
	 * @param majiangs
	 * @param sameCount
	 * @return
	 */
	private boolean canAnGang(TdhMjPlayer player, List<TdhMj> majiangs, int sameCount, int action) {
		if (sameCount != 4) {
			return false;
		}
		// if (player.getSeat() != getNextDisCardSeat() && action !=
		// TdhMjDisAction.action_buzhang) {
		// return false;
		// }
		// if (player.getSeat() != getNextDisCardSeat() && action !=
		// TdhMjDisAction.action_buzhang_an) {
		// return false;
		// }
		return true;
	}

	/**
	 * 检查优先度 如果同时出现一个事件，按出牌座位顺序优先
	 */
	private boolean checkAction(TdhMjPlayer player, List<TdhMj> cardList, List<Integer> hucards, int action) {
		boolean canAction = checkCanAction(player, action);// 是否优先级最高 能执行操作
		if (canAction == false) {// 不能操作时 存入临时操作
			int seat = player.getSeat();
			tempActionMap.put(seat, new TdhMjTempAction(seat, action, cardList, hucards));
			// 玩家都已选择自己的临时操作后 选取优先级最高
			if (tempActionMap.size() == actionSeatMap.size()) {
				int maxAction = Integer.MAX_VALUE;
				int maxSeat = 0;
				Map<Integer, Integer> prioritySeats = new HashMap<>();
				int maxActionSize = 0;
				for (TdhMjTempAction temp : tempActionMap.values()) {
					int prioAction = TdhMjDisAction.getPriorityAction(temp.getAction());
					int prioAction2 = TdhMjDisAction.getPriorityAction(maxAction);
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
				TdhMjPlayer tempPlayer = seatMap.get(maxSeat);
				List<TdhMj> tempCardList = tempActionMap.get(maxSeat).getCardList();
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
	private void refreshTempAction(TdhMjPlayer player) {
		tempActionMap.remove(player.getSeat());
		Map<Integer, Integer> prioritySeats = new HashMap<>();
		for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
			int seat = entry.getKey();
			List<Integer> actionList = entry.getValue();
			List<Integer> list = TdhMjDisAction.parseToDisActionList(actionList);
			int priorityAction = TdhMjDisAction.getMaxPriorityAction(list);
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
		Iterator<TdhMjTempAction> iterator = tempActionMap.values().iterator();
		while (iterator.hasNext()) {
			TdhMjTempAction tempAction = iterator.next();
			if (tempAction.getSeat() == maxPrioritySeat) {
				int action = tempAction.getAction();
				List<TdhMj> tempCardList = tempAction.getCardList();
				TdhMjPlayer tempPlayer = seatMap.get(tempAction.getSeat());
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
	public boolean checkCanAction(TdhMjPlayer player, int action) {
		// 优先度为胡杠补碰吃
		List<Integer> stopActionList = TdhMjDisAction.findPriorityAction(action);
		for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
			if (player.getSeat() != entry.getKey()) {
				// 别人
				boolean can = TdhMjDisAction.canDisMajiang(stopActionList, entry.getValue());
				if (!can) {
					return false;
				}
				List<Integer> disActionList = TdhMjDisAction.parseToDisActionList(entry.getValue());
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
	 * 是否能暗杠
	 *
	 * @param player
	 * @param majiangs
	 * @param sameCount
	 * @return
	 */
	private boolean canMingGang(TdhMjPlayer player, List<TdhMj> handMajiang, List<TdhMj> majiangs, int sameCount,
			TdhMj disMajiang) {
		List<Integer> pengList = TdhMjHelper.toMajiangVals(player.getPeng());

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

	public int getKePiao() {
		return kePiao;
	}

	public void setKePiao(int kePiao) {
		this.kePiao = kePiao;
		changeExtend();
	}

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

	public void setLastMajiang(TdhMj lastMajiang) {
		this.lastMajiang = lastMajiang;
		changeExtend();
	}

	public void setMoLastMajiangSeat(int moLastMajiangSeat) {
		this.moLastMajiangSeat = moLastMajiangSeat;
		changeExtend();
	}

	public void setGangMajiang(TdhMj gangMajiang) {
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
	public void setMoGang(TdhMj moGang, List<Integer> moGangHuList) {
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

	public void setGangDisMajiangs(List<TdhMj> gangDisMajiangs) {
		this.gangDisMajiangs = gangDisMajiangs;
		changeExtend();
	}

	public List<TdhMj> getGangDisMajiangs() {
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
			for (TdhMjPlayer player : seatMap.values()) {
				if (player.getTotalPoint() > 0 && player.getTotalPoint() < jiaBeiFen) {
					jiaBeiPoint += player.getTotalPoint() * (jiaBeiShu - 1);
					player.setTotalPoint(player.getTotalPoint() * jiaBeiShu);
				} else if (player.getTotalPoint() < 0) {
					loserCount++;
				}
			}
			if (jiaBeiPoint > 0) {
				for (TdhMjPlayer player : seatMap.values()) {
					if (player.getTotalPoint() < 0) {
						player.setTotalPoint(player.getTotalPoint() - (jiaBeiPoint / loserCount));
					}
				}
			}
		}

		List<ClosingMjPlayerInfoRes> list = new ArrayList<>();
		List<ClosingMjPlayerInfoRes.Builder> builderList = new ArrayList<>();
		int fangPaoSeat = selfMo ? 0 : disCardSeat;
		if (winList == null || winList.size() == 0) {
			fangPaoSeat = 0;
		}
		for (TdhMjPlayer player : seatMap.values()) {
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
						if (huMjIds.size() == 2) {
							isHu = huMjIds.get(0) * 1000 + huMjIds.get(1);
						} else {
							isHu = huMjIds.get(0);
						}
						build.setIsHu(isHu);
					} else {
						TdhMj huMajiang = nowDisCardIds.get(0);
						if (!build.getHandPaisList().contains(huMajiang.getId())) {
							build.addHandPais(huMajiang.getId());
						}
						build.setIsHu(huMajiang.getId());
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
						if (huMjIds.size() == 2) {
							isHu = huMjIds.get(0) * 1000 + huMjIds.get(1);
						} else {
							isHu = huMjIds.get(0);
						}
						build.setIsHu(isHu);
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
			for (TdhMjPlayer player : seatMap.values()) {
				if (player.getWinLoseCredit() > dyjCredit) {
					dyjCredit = player.getWinLoseCredit();
				}
			}
			for (ClosingMjPlayerInfoRes.Builder builder : builderList) {
				TdhMjPlayer player = seatMap.get(builder.getSeat());
				calcCommissionCredit(player, dyjCredit);
				builder.setWinLoseCredit(player.getWinLoseCredit());
				builder.setCommissionCredit(player.getCommissionCredit());
			}
		} else if (isGroupTableGoldRoom()) {
            // -----------亲友圈金币场---------------------------------
            for (TdhMjPlayer player : seatMap.values()) {
                player.setWinGold(player.getTotalPoint() * gtgDifen);
            }
            calcGroupTableGoldRoomWinLimit();
            for (ClosingMjPlayerInfoRes.Builder builder : builderList) {
                TdhMjPlayer player = seatMap.get(builder.getSeat());
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
		res.addAllLeftCards(TdhMjHelper.toMajiangIds(leftMajiangs));
		for (TdhMjPlayer player : seatMap.values()) {
			player.writeSocket(res.build());
		}
		return res;

	}

	/**
	 * 杠上花和杠上炮
	 *
	 * @return
	 */
	public TdhMj getGangHuMajiang(int seat) {
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
		return TdhMj.getMajang(majiangId);

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
		// ext.add(calcBird + "");
		ext.add(0 + "");
		ext.add(gangBubu + ""); // 5
		ext.add(jiangHuZiMo + ""); // 6
		ext.add(qingyiseChi + ""); // 7
		ext.add(genZhangBuPao + ""); // 8
		ext.add(pengpengHuJiePao + ""); // 9
		ext.add(kePiao + "");
		ext.add(birdNum + "");
		ext.add(isAutoPlay + "");
		ext.add(over + ""); // 17
		return ext;
	}

	@Override
	public void sendAccountsMsg() {
		for (TdhMjPlayer seat : seatMap.values()) {
			seat.changePoint(seat.getLostPoint());
			seat.changePointArr(2, seat.getLostPoint());
		}
		ClosingMjInfoRes.Builder builder = sendAccountsMsg(true, false, null, null, null, null, true, 0, 0);
		saveLog(true, 0l, builder.build());
	}

	public Class<? extends Player> getPlayerClass() {
		return TdhMjPlayer.class;
	}

	@Override
	public int getWanFa() {
		return GameUtil.game_type_TdhMj;
	}

	@Override
	public void checkReconnect(Player player) {
		if (super.isAllReady() && getKePiao() == 1 && getTableStatus() == TdhMjConstants.TABLE_STATUS_PIAO) {
			TdhMjPlayer player1 = (TdhMjPlayer) player;
			if (player1.getPiaoPoint() < 0) {
				ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_table_status_piao,
						getTableStatus());
				player1.writeSocket(com.build());
				return;
			}
		}
		checkSendGangRes(player);
		if (askLastMajaingSeat != 0) {
			sendAskLastMajiangRes(player.getSeat());
		}
		if (actionSeatMap.isEmpty()) {
			// 没有其他可操作的动作事件
			if (player instanceof TdhMjPlayer) {
				TdhMjPlayer TdhMjPlayer = (TdhMjPlayer) player;
				if (TdhMjPlayer != null) {
					if (TdhMjPlayer.isAlreadyMoMajiang()) {
						if (!TdhMjPlayer.getGang().isEmpty()) {
							List<TdhMj> disMajiangs = new ArrayList<>();
							disMajiangs.add(TdhMjPlayer.getLastMoMajiang());
							chuPai(TdhMjPlayer, disMajiangs, 0);
						}
					}
				}
			}
		}
		if (isBegin() && player.getSeat() == lastWinSeat && actionSeatMap.isEmpty()) {
			// 如果是起手判断是否还有人可胡小胡 没有的话通知庄家出牌
			TdhMjPlayer bankPlayer = seatMap.get(lastWinSeat);
			ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.req_com_kt_ask_dismajiang);// 推送庄家出牌
			bankPlayer.writeSocket(com.build());
		}

		if (state == table_state.play) {
			if (player.getHandPais() != null && player.getHandPais().size() > 0) {
				sendTingInfo((TdhMjPlayer) player);
			}
		}

	}

	public static void main(String[] args) {
		System.out.println(Math.pow(3, 2));
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
			for (TdhMjPlayer player : seatMap.values()) {
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
		
		
		TdhMjPlayer player2 = seatMap.get(nowDisCardSeat);
		if(player2!=null){
			List<Integer> actionList = actionSeatMap.get(player2.getSeat());
			checkAutoMD(player2,actionList);
		}
		
		for (TdhMjPlayer player : seatMap.values()) {
			
			// 能胡牌就不自动打出去
			if (!player.getGang().isEmpty() && player.isAlreadyMoMajiang() && getMoMajiangSeat() == player.getSeat()) {
				List<Integer> actionList = actionSeatMap.get(player.getSeat());
				if (actionList != null && (actionList.get(TdhMjAction.HU) == 1 || actionList.get(TdhMjAction.ZIMO) == 1
						|| actionList.get(TdhMjAction.MINGGANG) == 1 || actionList.get(TdhMjAction.ANGANG) == 1)) {
					continue;
				}

				if (nowDisCardSeat != player.getSeat()) {
					continue;
				}
				List<TdhMj> disMjiang = new ArrayList<>();
				disMjiang.add(player.getHandMajiang().get(player.getHandMajiang().size() - 1));
				chuPai(player, disMjiang, TdhMjDisAction.action_chupai);
				// 执行完一个就退出，防止出牌操作后有报听玩家摸牌
				setLastAutoPlayTime(System.currentTimeMillis());
				sendTingInfo(player);
				return;
			}
		}

	
		
		if (isAutoPlay < 1) {
			return;
		}

		if (getTableStatus() == TdhMjConstants.TABLE_STATUS_PIAO) {
			for (int seat : seatMap.keySet()) {
				TdhMjPlayer player = seatMap.get(seat);
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
				TdhMjPlayer player = seatMap.get(seat);
				if (player.getPiaoPoint() < 0) {
					piao = false;
				}

			}
			if (piao) {
				setTableStatus(TdhMjConstants.AUTO_PLAY_TIME);
			}

		} else if (state == table_state.play) {
			autoPlay();
		} else {
			if (getPlayedBureau() == 0) {
				return;
			}
			readyTime++;
			// for (TdhMjPlayer player : seatMap.values()) {
			// if (player.checkAutoPlay(1, false)) {
			// autoReady(player);
			// }
			// }
			// 开了托管的房间，xx秒后自动开始下一局
			for (TdhMjPlayer player : seatMap.values()) {
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

	private void checkAutoMD(TdhMjPlayer player, List<Integer> actionList) {
		if (player.getAutoMD() == 1) {
//				if (player.getLastCheckTime() > 0) {
//					player.setLastCheckTime(player.getLastCheckTime() + 1 * 1000);
//				}
				if (actionList!=null) {
					if((actionList.get(TdhMjAction.HU) == 1 || actionList.get(TdhMjAction.ZIMO) == 1)||actionList.get(TdhMjAction.ANGANG) == 1||actionList.get(TdhMjAction.MINGGANG) == 1){
						//playCommand(player, new ArrayList<>(), TdhMjDisAction.action_hu);
					}else{
						playCommand(player, new ArrayList<>(), TdhMjDisAction.action_pass);
						if(!player.isAutoPlay()){
							player.setAutoPlay(false,false);
						}
					}
				} else {
					if(!player.isAutoPlay()){
						player.setAutoPlay(false,false);
					}
					autoChuPai(player);
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
					TdhMjPlayer player = seatMap.get(seat);
					if (player == null) {
						continue;
					}
					if (!player.checkAutoPlay(2, false)) {
						continue;
					}
					playCommand(player, new ArrayList<>(), TdhMjDisAction.action_hu);
				}
				return;
			} else {
				int action, seat;
				for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
					List<Integer> actList = TdhMjDisAction.parseToDisActionList(entry.getValue());
					if (actList == null) {
						continue;
					}
					seat = entry.getKey();
					action = TdhMjDisAction.getAutoMaxPriorityAction(actList);
					TdhMjPlayer player = seatMap.get(seat);
					if (!player.checkAutoPlay(0, false)) {
						continue;
					}
					boolean chuPai = false;
					if (player.isAlreadyMoMajiang()) {
						chuPai = true;
					}
//					if (action == TdhMjDisAction.action_peng) {
//						if (player.isAutoPlaySelf()) {
//							// 自己开启托管直接过
//							playCommand(player, new ArrayList<>(), TdhMjDisAction.action_pass);
//							if (chuPai) {
//								autoChuPai(player);
//							}
//						} else {
//							if (nowDisCardIds != null && !nowDisCardIds.isEmpty()) {
//								TdhMj mj = nowDisCardIds.get(0);
//								List<TdhMj> mjList = new ArrayList<>();
//								for (TdhMj handMj : player.getHandMajiang()) {
//									if (handMj.getVal() == mj.getVal()) {
//										mjList.add(handMj);
//										if (mjList.size() == 2) {
//											break;
//										}
//									}
//								}
//								playCommand(player, mjList, TdhMjDisAction.action_peng);
//							}
//						}
//					}
//					else {
						playCommand(player, new ArrayList<>(), TdhMjDisAction.action_pass);
						if (chuPai) {
							autoChuPai(player);
						}
//					}
				}
			}
		} else {
			TdhMjPlayer player = seatMap.get(nowDisCardSeat);
			if (player == null || !player.checkAutoPlay(0, false)) {
				return;
			}
			autoChuPai(player);
		}
	}

	public void autoChuPai(TdhMjPlayer player) {

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
		// TdhMj mj = TdhMj.getMajang(mjId);

		while (mjId == -1 && index >= 0) {
			mjId = handMjIds.get(index);
			// mj = TdhMj.getMajang(mjId);

		}
		if (mjId != -1) {
			List<TdhMj> mjList = TdhMjHelper.toMajiang(Arrays.asList(mjId));
			playCommand(player, mjList, TdhMjDisAction.action_chupai);
		}
	}

	public void autoPiao(TdhMjPlayer player) {
		int piaoPoint = 0;
		if (getTableStatus() != TdhMjConstants.TABLE_STATUS_PIAO) {
			return;
		}
		if (player.getPiaoPoint() < 0) {
			player.setPiaoPoint(piaoPoint);
		} else {
			return;
		}
		ComMsg.ComRes.Builder build = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_piao_fen, player.getSeat(),
				piaoPoint);
		broadMsg(build.build());
		broadMsgRoomPlayer(build.build());
		checkDeal(player.getUserId());
	}

	@Override
	public void initExtend0(JsonWrapper extend) {
		for (TdhMjPlayer player : seatMap.values()) {
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
			moGang = TdhMj.getMajang(moGangMajiangId);
		}
		String moGangHu = extend.getString(9);
		if (!StringUtils.isBlank(moGangHu)) {
			moGangHuList = StringUtil.explodeToIntList(moGangHu);
		}
		String gangDisMajiangstr = extend.getString(10);
		if (!StringUtils.isBlank(gangDisMajiangstr)) {
			gangDisMajiangs = TdhMjHelper.explodeMajiang(gangDisMajiangstr, ",");
		}
		int gangMajiang = extend.getInt(11, 0);
		if (gangMajiang != 0) {
			this.gangMajiang = TdhMj.getMajang(gangMajiang);
		}

		askLastMajaingSeat = extend.getInt(12, 0);
		moLastMajiangSeat = extend.getInt(13, 0);
		int lastMajiangId = extend.getInt(14, 0);
		if (lastMajiangId != 0) {
			this.lastMajiang = TdhMj.getMajang(lastMajiangId);
		}
		fristLastMajiangSeat = extend.getInt(15, 0);
		disEventAction = extend.getInt(16, 0);
		kePiao = extend.getInt(19, 0);
		tempActionMap = loadTempActionMap(extend.getString("tempActions"));

		// gpsWarn = extend.getInt(20, 0);
		gangBubu = extend.getInt(21, 0);
		jiangHuZiMo = extend.getInt(22, 0);
		qingyiseChi = extend.getInt(23, 0);
		genZhangBuPao = extend.getInt(24, 0);
		pengpengHuJiePao = extend.getInt(25, 0);

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

		isAutoPlay = extend.getInt(47, 0);
		autoPlayGlob = extend.getInt(48, 0);
		tableStatus = extend.getInt(50, 0);
		topFen = extend.getInt(51, 0);
		dahuDifen = extend.getInt(52, 0);
		
	}

	@Override
	public JsonWrapper buildExtend0(JsonWrapper wrapper) {
		// 1-4 玩家座位信息
		for (TdhMjPlayer player : seatMap.values()) {
			wrapper.putString(player.getSeat(), player.toExtendStr());
		}
		wrapper.putString(5, DataMapUtil.explode(huConfirmMap));
		wrapper.putInt(6, birdNum);
		wrapper.putInt(7, moMajiangSeat);
		wrapper.putInt(8, moGang != null ? moGang.getId() : 0);
		wrapper.putString(9, StringUtil.implode(moGangHuList, ","));
		wrapper.putString(10, TdhMjHelper.implodeMajiang(gangDisMajiangs, ","));
		wrapper.putInt(11, gangMajiang != null ? gangMajiang.getId() : 0);
		wrapper.putInt(12, askLastMajaingSeat);
		wrapper.putInt(13, moLastMajiangSeat);
		wrapper.putInt(14, lastMajiang != null ? lastMajiang.getId() : 0);
		wrapper.putInt(15, fristLastMajiangSeat);
		wrapper.putInt(16, disEventAction);
		wrapper.putInt(19, kePiao);
		JSONArray tempJsonArray = new JSONArray();
		for (int seat : tempActionMap.keySet()) {
			tempJsonArray.add(tempActionMap.get(seat).buildData());
		}
		wrapper.putString("tempActions", tempJsonArray.toString());

		// wrapper.putInt(20, gpsWarn);

		wrapper.putInt(21, gangBubu);
		wrapper.putInt(22, jiangHuZiMo);
		wrapper.putInt(23, qingyiseChi);
		wrapper.putInt(24, genZhangBuPao);
		wrapper.putInt(25, pengpengHuJiePao);

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
		wrapper.putInt(47, isAutoPlay);
		wrapper.putInt(48, autoPlayGlob);
		wrapper.putInt(50, tableStatus);
		wrapper.putInt(51, topFen);
		wrapper.putInt(52, dahuDifen);
		
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
		birdNum = StringUtil.getIntValue(params, 10, 0);
		kePiao = StringUtil.getIntValue(params, 6, 0);

		// 加倍：0否，1是
		this.jiaBei = StringUtil.getIntValue(params, 3, 0);
		// 加倍分
		this.jiaBeiFen = StringUtil.getIntValue(params, 4, 0);
		// 加倍数
		this.jiaBeiShu = StringUtil.getIntValue(params, 5, 0);

		isAutoPlay = StringUtil.getIntValue(params, 8, 0);
		this.autoPlayGlob = StringUtil.getIntValue(params, 9, 0);
		gangBubu = StringUtil.getIntValue(params, 11, 0);
		jiangHuZiMo = StringUtil.getIntValue(params, 12, 0);

		qingyiseChi = StringUtil.getIntValue(params, 13, 0);

		genZhangBuPao = StringUtil.getIntValue(params, 14, 0);

		pengpengHuJiePao = StringUtil.getIntValue(params, 15, 0);
		
		
		dahuDifen = StringUtil.getIntValue(params, 16, 0);
		if(dahuDifen==0){
			dahuDifen = 5;
		}else {
			dahuDifen = 10;
		}
		
		topFen = StringUtil.getIntValue(params, 17, 0);

		if (maxPlayerCount != 2) {
			jiaBei = 0;
		}
		playedBureau = 0;

		// getRoomModeMap().put("1", "1"); //可观战（默认）
	}

	public void sendTingInfo(TdhMjPlayer player) {
		if (player.isAlreadyMoMajiang()) {
			// if (actionSeatMap.containsKey(player.getSeat())) {
			// return;
			// }
			DaPaiTingPaiRes.Builder tingInfo = DaPaiTingPaiRes.newBuilder();
			List<TdhMj> cards = new ArrayList<>(player.getHandMajiang());

			for (TdhMj card : player.getHandMajiang()) {
				cards.remove(card);
				List<TdhMj> huCards = TdhMjTool.getTingMjs(cards, this, player, true);
				cards.add(card);
				if (huCards == null || huCards.size() == 0) {
					continue;
				}
				DaPaiTingPaiInfo.Builder ting = DaPaiTingPaiInfo.newBuilder();
				ting.setMajiangId(card.getId());
				for (TdhMj mj : huCards) {
					ting.addTingMajiangIds(mj.getId());
				}
				tingInfo.addInfo(ting.build());
			}
			if (tingInfo.getInfoCount() > 0) {
				player.writeSocket(tingInfo.build());
			}
		} else {
			List<TdhMj> cards = new ArrayList<>(player.getHandMajiang());
			List<TdhMj> huCards = TdhMjTool.getTingMjs(cards, this, player, true);

			if (huCards == null || huCards.size() == 0) {
				return;
			}
			TingPaiRes.Builder ting = TingPaiRes.newBuilder();
			for (TdhMj mj : huCards) {
				ting.addMajiangIds(mj.getId());
			}
			player.writeSocket(ting.build());

		}
	}

	public static final List<Integer> wanfaList = Arrays.asList(GameUtil.game_type_TdhMj);

	public static void loadWanfaTables(Class<? extends BaseTable> cls) {
		for (Integer integer : wanfaList) {
			TableManager.wanfaTableTypesPut(integer, cls);
		}
	}

	public void logFaPaiTable() {
		StringBuilder sb = new StringBuilder();
		sb.append("TdhMj");
		sb.append("|").append(getId());
		sb.append("|").append(getPlayBureau());
		sb.append("|").append("faPai");
		sb.append("|").append(playType);
		sb.append("|").append(maxPlayerCount);
		sb.append("|").append(getPayType());
		sb.append("|").append(birdNum);
		sb.append("|").append(kePiao);
		sb.append("|").append(gangBubu);
		sb.append("|").append(jiangHuZiMo);
		sb.append("|").append(qingyiseChi);
		sb.append("|").append(genZhangBuPao);
		sb.append("|").append(pengpengHuJiePao);
		sb.append("|").append(lastWinSeat);
		LogUtil.msg(sb.toString());
	}

	public void logFaPaiPlayer(TdhMjPlayer player, List<Integer> actList) {
		StringBuilder sb = new StringBuilder();
		sb.append("TdhMj");
		sb.append("|").append(getId());
		sb.append("|").append(getPlayBureau());
		sb.append("|").append(player.getUserId());
		sb.append("|").append(player.getSeat());
		sb.append("|").append("faPai");
		sb.append("|").append(player.getHandMajiang());
		sb.append("|").append(actListToString(actList));
		LogUtil.msg(sb.toString());
	}

	public void logMoMj(TdhMjPlayer player, TdhMj mj, List<Integer> actList) {
		StringBuilder sb = new StringBuilder();
		sb.append("TdhMj");
		sb.append("|").append(getId());
		sb.append("|").append(getPlayBureau());
		sb.append("|").append(player.getUserId());
		sb.append("|").append(player.getSeat());
		sb.append("|").append("moPai");
		sb.append("|").append(getLeftMajiangCount());
		sb.append("|").append(mj);
		sb.append("|").append(actListToString(actList));
//		sb.append("|").append(player.getHandMajiang());
		LogUtil.msg(sb.toString());
	}

	public void logChuPaiActList(TdhMjPlayer player, TdhMj mj, List<Integer> actList) {
		StringBuilder sb = new StringBuilder();
		sb.append("TdhMj");
		sb.append("|").append(getId());
		sb.append("|").append(getPlayBureau());
		sb.append("|").append(player.getUserId());
		sb.append("|").append(player.getSeat());
		sb.append("|").append("chuPaiActList");
		sb.append("|").append(mj);
		sb.append("|").append(actListToString(actList));
//		sb.append("|").append(player.getHandMajiang());
		LogUtil.msg(sb.toString());
	}

	public void logAction(TdhMjPlayer player, int action, List<TdhMj> mjs, List<Integer> actList) {
		StringBuilder sb = new StringBuilder();
		sb.append("TdhMj");
		sb.append("|").append(getId());
		sb.append("|").append(getPlayBureau());
		sb.append("|").append(player.getUserId());
		sb.append("|").append(player.getSeat());
		String actStr = "unKnown-" + action;
		if (action == TdhMjDisAction.action_peng) {
			actStr = "peng";
		} else if (action == TdhMjDisAction.action_minggang) {
			actStr = "mingGang";
		} else if (action == TdhMjDisAction.action_chupai) {
			actStr = "chuPai";
		} else if (action == TdhMjDisAction.action_pass) {
			actStr = "guo";
		} else if (action == TdhMjDisAction.action_angang) {
			actStr = "anGang";
		} else if (action == TdhMjDisAction.action_chi) {
			actStr = "chi";
		} else if (action == TdhMjDisAction.action_buzhang) {
			actStr = "buZhang";
		} else if (action == TdhMjDisAction.action_buzhang_an) {
			actStr = "buZhangAn";
		}
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
				if (i == TdhMjAction.HU) {
					sb.append("hu");
				} else if (i == TdhMjAction.PENG) {
					sb.append("peng");
				} else if (i == TdhMjAction.MINGGANG) {
					sb.append("mingGang");
				} else if (i == TdhMjAction.ANGANG) {
					sb.append("anGang");
				} else if (i == TdhMjAction.CHI) {
					sb.append("chi");
				} else if (i == TdhMjAction.BUZHANG) {
					sb.append("buZhang");
				} else if (i == TdhMjAction.BUZHANG_AN) {
					sb.append("buZhangAn");
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
	public void processHideMj(TdhMjPlayer player) {
		if (showMjSeat.contains(player.getSeat()) && disCardRound != 0) {
			PlayMajiangRes.Builder hideMj = PlayMajiangRes.newBuilder();
			buildPlayRes(hideMj, player, TdhMjDisAction.action_hideMj, null);
			broadMsgToAll(hideMj.build());
			showMjSeat.remove(Integer.valueOf(player.getSeat()));
		}
	}

	public void clearShowMjSeat() {
		showMjSeat.clear();
		changeExtend();
	}

	/**
	 * 庄家是否出过牌了
	 * 
	 * @return
	 */
	public boolean isBegin() {
		return isBegin && nowDisCardIds.size() == 0;
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
			if (getKePiao() == 1) {
				boolean bReturn = true;
				// 机器人默认处理
				if (this.isTest()) {
					for (TdhMjPlayer robotPlayer : seatMap.values()) {
						if (robotPlayer.isRobot()) {
							robotPlayer.setPiaoPoint(1);
						}
					}
				}
				for (TdhMjPlayer player : seatMap.values()) {
					if (player.getPiaoPoint() < 0) {
						ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_table_status_piao,
								getTableStatus());
						player.writeSocket(com.build());
						if (getTableStatus() != TdhMjConstants.TABLE_STATUS_PIAO) {
							player.setLastCheckTime(System.currentTimeMillis());
						}
						bReturn = false;

					}
				}
				setTableStatus(TdhMjConstants.TABLE_STATUS_PIAO);

				return bReturn;
			} else {
				int point = 0;
				if (getKePiao() == 2 || getKePiao() == 3 || getKePiao() == 4) {
					point = getKePiao() - 1;
				}

				for (TdhMjPlayer player : seatMap.values()) {
					player.setPiaoPoint(point);
				}
				return true;
			}
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
		List<TdhMj> moList = getGangDisMajiangs();
		if (moList != null && moList.size() > 0 && actionSeatMap.isEmpty()) {
			TdhMjPlayer player = seatMap.get(getMoMajiangSeat());
			for (TdhMjPlayer seatPlayer : seatMap.values()) {
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
	public void sendMoLast(TdhMjPlayer player, int state) {
		ComRes.Builder res = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_asklastmajiang, state);
		player.writeSocket(res.build());
	}

	@Override
	public boolean isPlaying() {
		if (super.isPlaying()) {
			return true;
		}
		return getTableStatus() == TdhMjConstants.TABLE_STATUS_PIAO;
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

	public int getJiangHuZiMo() {
		return jiangHuZiMo;
	}

	public int getQingyiseChi() {
		return qingyiseChi;
	}

	public int getGangBubu() {
		return gangBubu;
	}

	public int getPengpengHuJiePao() {
		return pengpengHuJiePao;
	}
	

	@Override
	public String getGameName() {
		return "推倒胡";
	}

}
