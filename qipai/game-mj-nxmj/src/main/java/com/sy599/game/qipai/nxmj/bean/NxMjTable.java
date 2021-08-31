package com.sy599.game.qipai.nxmj.bean;

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

import com.alibaba.fastjson.JSON;
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
import com.sy599.game.qipai.nxmj.constant.NxMjAction;
import com.sy599.game.qipai.nxmj.constant.NxMjConstants;
import com.sy599.game.qipai.nxmj.rule.NxMj;
import com.sy599.game.qipai.nxmj.rule.NxMjHelper;
import com.sy599.game.qipai.nxmj.rule.NxMjRobotAI;
import com.sy599.game.qipai.nxmj.rule.NxMjRule;
import com.sy599.game.qipai.nxmj.tool.NxMjQipaiTool;
import com.sy599.game.qipai.nxmj.tool.NxMjResTool;
import com.sy599.game.qipai.nxmj.tool.NxMjTool;
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
 * @author liuping 长沙麻将牌桌信息
 */
public class NxMjTable extends BaseTable {
	/**
	 * 当前桌上打出的牌
	 */
	private List<NxMj> nowDisCardIds = new ArrayList<>();
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
	private List<NxMj> leftMajiangs = new ArrayList<>();
	/**
	 * 当前房间所有玩家信息map
	 */
	private Map<Long, NxMjPlayer> playerMap = new ConcurrentHashMap<Long, NxMjPlayer>();
	/**
	 * 座位对应的玩家信息MAP
	 */
	private Map<Integer, NxMjPlayer> seatMap = new ConcurrentHashMap<Integer, NxMjPlayer>();
	/**
	 * 胡确认信息
	 */
	private Map<Integer, Integer> huConfirmMap = new HashMap<>();
	/**
	 * 玩家位置对应临时操作 当同时存在多个可做的操作时 1当优先级最高的玩家最先操作 则清理所有操作提示 并执行该操作
	 * 2当操作优先级低的玩家先选择操作，则玩家先将所做操作存入临时操作中 当玩家优先级最高的玩家操作后 在判断临时操作是否可执行并执行
	 */
	private Map<Integer, NxMjTempAction> tempActionMap = new ConcurrentHashMap<>();
	/**
	 * 抓鸟
	 */
	private int birdNum;
	/**
	 * 计算庄闲
	 */
	private int isCalcBanker;
	/**
	 * 计算鸟的算法 1：中鸟+1 2：中鸟翻倍 3：中鸟加倍
	 */
	private int calcBird;
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
	private NxMj moGang;
	/**
	 * 杠出来的麻将
	 */
	private NxMj gangMajiang;
	/**
	 * 摸杠胡
	 */
	private List<Integer> moGangHuList = new ArrayList<>();
	/**
	 * 杠后出的两张牌
	 */
	private List<NxMj> gangDisMajiangs = new ArrayList<>();
	
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
	private NxMj lastMajiang;
	/**
	 *
	 */
	private int disEventAction;

	/*** GPS预警 */
	private int gpsWarn = 0;
	/*** 缺一色 */
	private int queYiSe = 0;
	/*** 板板胡 */
	private int banbanHu = 0;
	/*** 一枝花 */
	private int yiZhiHua = 0;
	/*** 一点红 */
	private int yiDianHong = 0;
	/*** 六六顺 */
	private int liuliuShun = 0;
	/*** 大四喜 */
	private int daSiXi = 0;
	/*** 金童玉女 */
	private int jinTongYuNu = 0;
	/*** 节节高 */
	private int jieJieGao = 0;
	/*** 三同 */
	private int sanTong = 0;
	/*** 中途六六顺 */
	private int zhongTuLiuLiuShun = 0;
	/*** 中途四喜 */
	private int zhongTuSiXi = 0;

	/*** 需要展示牌的玩家座位号 */
	private List<Integer> showMjSeat = new ArrayList<>();

	private int tableStatus;// 特殊状态 1飘分

	/*** 杠打色子 **/
	private int gangDice = -1;

	/*** 摸屁股的座标号 */
	private List<Integer> moTailPai = new ArrayList<>();

	/** 杠后摸的两张牌中被要走的 **/
	private NxMj gangActedMj = null;

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

	// 三人：空鸟 1：四八空鸟 2:鸟不落空
	private int kongBird;
	// （二人选）1：不能吃
	private int buChi;
	// （二人选）只能大胡
	private int OnlyDaHu;
	// （二人选）小胡自摸
	private int xiaohuZiMo;
	// （二人选）缺一门
	private int queYiMen;

	private int jiajianghu;

	/** 托管1：单局，2：全局 */
	private int autoPlayGlob;
	private int autoTableCount;
	private int isAutoPlay;// 托管时间
	private int readyTime = 0;

	private int menqing;// 门清
	private int topFen;// 分数限制
	
	private int gangMoSi;// 杠摸4张
	
	private int qiShouNiaoFen = 1;// 起手鸟分1:不算鸟分
	//低于below加分
	private int belowAdd=0;
	private int below=0;
	
	
	private int gangBuF;// 杠补算分
	private int quanqiurJiang;// 全求人吊将
	
	private int difen;// 底分
	
	private int menQingZM;// 门清
	private int pinghuNojiepao;// 平胡不接炮
	private int choupai40;//抽40张牌
	
	@Override
	protected boolean quitPlayer1(Player player) {
		return false;
	}

	@Override
	public boolean canQuit(Player player) {
		if (super.canQuit(player)) {
			return getTableStatus() != NxMjConstants.TABLE_STATUS_PIAO;
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
			seatBirds = birdToSeat(birdMjIds, lastWinSeat);
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
			NxMjPlayer winPlayer = null;
			if (winSeatList.size() == 1) {
				winPlayer = seatMap.get(winSeatList.get(0));
				if ((winPlayer.isAlreadyMoMajiang() || winPlayer.isGangshangHua())
						&& winSeatList.get(0) == moMajiangSeat) {
					selfMo = true;
				}
			}
			// 庄家
			if (selfMo) {
//				int xiaohuNum = winPlayer.getHuXiaohu().size() > 0 ? winPlayer.getHuXiaohu().size():1;
				calZiMoPoint(seatBirdMap, winPlayer, true, 1, true, selfMo);
				winPlayer.changeAction(7, 1);
				winPlayer.getMyExtend().setMjFengshen(FirstmythConstants.firstmyth_index8, 1);
			} else {
				NxMjPlayer losePlayer = seatMap.get(disCardSeat);
				int loserSeat = losePlayer.getSeat();
				losePlayer.getMyExtend().setMjFengshen(FirstmythConstants.firstmyth_index10, winSeatList.size());
				int totalLosePoint = 0;
				for (int winSeat : winSeatList) {
					// 胡牌
					winPlayer = seatMap.get(winSeat);
					int daHuCount = winPlayer.getDahuPointCount();
					// 底分
					int winPoint = difen;
					if (daHuCount > 0) {
						winPoint = calcDaHuPoint(daHuCount);
						losePlayer.changeActionTotal(NxMjAction.ACTION_COUNT_DAHU_DIANPAO, 1);
						winPlayer.changeActionTotal(NxMjAction.ACTION_COUNT_DAHU_JIEPAO, 1);
					} else {
						winPoint = difen;
						losePlayer.changeActionTotal(NxMjAction.ACTION_COUNT_XIAOHU_DIANPAO, 1);
						winPlayer.changeActionTotal(NxMjAction.ACTION_COUNT_XIAOHU_JIEPAO, 1);
					}

					// 庄闲分
					if ((winPlayer.getSeat() == lastWinSeat || loserSeat == lastWinSeat)) {
						winPoint = calcBankerPoint(winPoint, daHuCount);
					}

					int totalBirdNum = seatBirdMap.get(winSeat) + seatBirdMap.get(loserSeat);

					winPoint = calcBirdPoint(winPoint, totalBirdNum, true);

					// 飘分
					if (kePiao >= 1) {
						winPoint += (winPlayer.getPiaoPoint() + losePlayer.getPiaoPoint());
					}

					totalLosePoint += winPoint;
					winPlayer.changeAction(6, 1);
					losePlayer.changeAction(0, 1);
					winPlayer.setLostPoint(winPoint);
				}
				losePlayer.setLostPoint(-totalLosePoint);
			}

		}

		// 小胡计算
		for (NxMjPlayer winPlayer : seatMap.values()) {
			if (winPlayer.getHuXiaohu().size() == 0) {
				continue;
			}

//			boolean addBirdPoint = true;
//			// 已经加过鸟分了就不再加了
//			if (winSeatList.contains(winPlayer.getSeat())) {
//				addBirdPoint = false;
//			}
//			
//			if(qiShouNiaoFen ==1) {
//				addBirdPoint = false;
//			}
			boolean addBirdPoint = false;
//			Map<Integer, Integer> copyMap = new HashMap<>(seatBirdMap);
//			copyMap.replaceAll((s1,s2)->{
//		        if(s2.intValue()>0) {
//		        s2=0;
//		        }
//		        return s2;
//			});
			calZiMoPoint(seatBirdMap, winPlayer, false, winPlayer.getHuXiaohu().size(), addBirdPoint, selfMo);
		}

		for (NxMjPlayer seat : seatMap.values()) {
			
			if (topFen > 0 && maxPlayerCount == 2) {
				if (Math.abs(seat.getLostPoint()) > topFen) {
					seat.setLostPoint(seat.getLostPoint() > 0 ? topFen : -topFen);
				}
			}
			seat.changePoint(seat.getLostPoint()+seat.getGangPoint());

		}

		boolean over = playBureau == totalBureau;

		if (autoPlayGlob > 0) {
			// //是否解散
			boolean diss = false;
			if (autoPlayGlob == 1) {
				for (NxMjPlayer seat : seatMap.values()) {
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

		for (NxMjPlayer player : seatMap.values()) {
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
		for (NxMjPlayer seat : seatMap.values()) {
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


	private void calZiMoPoint(Map<Integer, Integer> seatBirdMap, NxMjPlayer winPlayer, boolean dahu, int xiaohuNum,
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
			winPlayer.changeActionTotal(NxMjAction.ACTION_COUNT_DAHU_ZIMO, 1);
		} else {
			winPoint = difen;
			winPlayer.changeActionTotal(NxMjAction.ACTION_COUNT_XIAOHU_ZIMO, 1);
		}

		// 赢家庄闲分
		if (winPlayer.getSeat() == lastWinSeat) {
			winPoint = calcBankerPoint(winPoint, daHuCount);
		}
		int winBirdNum = seatBirdMap.get(winSeat);
		int totalWinPoint = 0;
		for (int loserSeat : seatMap.keySet()) {
			// 除了赢家的其他人
			if (winSeat == loserSeat) {
				continue;
			}
			NxMjPlayer loser = seatMap.get(loserSeat);
			int losePoint = winPoint;
			// 输家庄闲分
			if (loser.getSeat() == lastWinSeat) {
				losePoint = calcBankerPoint(losePoint, daHuCount);
			}

			losePoint *= xiaohuNum;

			int totalBirdNum = winBirdNum + seatBirdMap.get(loserSeat);

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
				if (kePiao >= 1) {
					losePoint += (loser.getPiaoPoint() + winPlayer.getPiaoPoint());
				}
			}

			totalWinPoint += losePoint;
			loser.changeLostPoint(-losePoint);
		}
		winPlayer.changeLostPoint(totalWinPoint);
	}

	/**
	 * 计算鸟分加成
	 *
	 * @param point
	 * @param bird
	 * @return
	 */
	private int calcBirdPoint(int point, int bird, boolean addBirdPoint) {
		if (bird <= 0) {
			return point;
		}
		if (calcBird == 2 && addBirdPoint) {

			// 加分最后结算
			point = point + bird*1;
		} else if (calcBird == 1 && addBirdPoint) {
			// 翻倍是2的bird次方
//			point = (int) (point * (Math.pow(2, bird)));
			point *= (bird + 1);
		} 
//		else if (calcBird == 3) {
//			// 加倍
//			point *= (bird + 1);
//		}
		return point;
	}

	/**
	 * 计算庄闲加成
	 *
	 * @param point
	 * @return
	 */
	private int calcBankerPoint(int point, int dahuCount) {

//		if (dahuCount == 0) {
//			dahuCount = 1;
//		}
//		point += dahuCount;

		return point;
	}

	/**
	 * 计算大胡
	 *
	 * @return
	 */
	private int calcDaHuPoint(int daHuCount) {
//		int point = 5+difen;
		int point = 8;
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
		NxMjPlayer player = seatMap.get(seat);
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
		for (NxMjPlayer player : playerMap.values()) {
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
		// for (NxMjPlayer player : playerMap.values()) {
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
	private int[] zhuaNiao(NxMj lastMaj) {
		// 先砸鸟
		int realBirdNum = leftMajiangs.size() > birdNum ? birdNum : leftMajiangs.size();

		if (realBirdNum == 0) {
//			realBirdNum = birdNum;
			realBirdNum = 1;//2020.2.26 海底修改只算1个鸟
		}
		int[] bird = new int[realBirdNum];
		for (int i = 0; i < realBirdNum; i++) {
			NxMj prickbirdMajiang = null;
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
			NxMj majiang = NxMj.getMajang(prickBirdMajiangIds[i]);
			int prickbirdPai = majiang.getPai();

			int prickbirdseat = 0;
			if (maxPlayerCount == 4) {
				prickbirdPai = (prickbirdPai - 1) % 4;// 从自己开始算 所以减1
				prickbirdseat = prickbirdPai + bankerSeat > 4 ? prickbirdPai + bankerSeat - 4
						: prickbirdPai + bankerSeat;
			} else if (maxPlayerCount == 3) {
				// 鸟不落空
				if (kongBird == 2) {
					prickbirdPai = (prickbirdPai - 1) % 3;// 从自己开始算 所以减1
					prickbirdseat = prickbirdPai + bankerSeat > 3 ? prickbirdPai + bankerSeat - 3
							: prickbirdPai + bankerSeat;
				} else {
					// 4-8 空鸟
					if (prickbirdPai == 1 || prickbirdPai == 5 || prickbirdPai == 9) {
						prickbirdseat = bankerSeat;
					} else if (prickbirdPai == 2 || prickbirdPai == 6) {
						// 庄下家
						prickbirdseat = (bankerSeat % 3) + 1;
					} else if (prickbirdPai == 3 || prickbirdPai == 7) {
						// 庄上家
						prickbirdseat = ((bankerSeat % 3) + 1) % 3 + 1;
					}
				}
			} else {
//				if (prickbirdPai == 1 || prickbirdPai == 5 || prickbirdPai == 9) {
//					prickbirdseat = bankerSeat;
//				} else if (prickbirdPai == 3 || prickbirdPai == 7) {
//					prickbirdseat = (bankerSeat % 2) + 1;
//				}
				if (prickbirdPai == 1 || prickbirdPai == 3 || prickbirdPai == 5 ||prickbirdPai == 7 ||prickbirdPai == 9) {
					prickbirdseat = bankerSeat;
				} else{
					prickbirdseat = (bankerSeat % 2) + 1;
				}

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
				tempMap.put("nowDisCardIds", StringUtil.implode(NxMjHelper.toMajiangIds(nowDisCardIds), ","));
			}
			if (tempMap.containsKey("leftPais")) {
				tempMap.put("leftPais", StringUtil.implode(NxMjHelper.toMajiangIds(leftMajiangs), ","));
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
	// for (NxMjPlayer player : seatMap.values()) {
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
		addPlayLog(disCardRound + "_" + lastWinSeat + "_" + NxMjDisAction.action_dice + "_" + dealDice);
		setDealDice(dealDice);
		logFaPaiTable();

		for (NxMjPlayer tablePlayer : seatMap.values()) {
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

			sendTingInfo(tablePlayer);
			
			  if(tablePlayer.isAutoPlay()) {
	            	addPlayLog(getDisCardRound() + "_" +tablePlayer.getSeat() + "_" + NxMjConstants.action_tuoguan + "_" +1);
	           }

		}
		isBegin = true;
		if (!hasXiaoHu() && !hasBaoTing()) {
			// 没有操作的话通知庄家出牌
			NxMjPlayer bankPlayer = seatMap.get(lastWinSeat);
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
	public void moMajiang(NxMjPlayer player, boolean isBuzhang) {
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
		NxMj majiang = null;
		if (disCardRound != 0) {
			// 玩家手上的牌是双数，已经摸过牌了
			if (player.isAlreadyMoMajiang()) {
				return;
			}
			if (GameServerConfig.isDebug() && !player.isRobot()) {
				if (zpMap.containsKey(player.getUserId()) && zpMap.get(player.getUserId()) > 0) {
					majiang = NxMjQipaiTool.findMajiangByVal(leftMajiangs, zpMap.get(player.getUserId()));
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
			addPlayLog(disCardRound + "_" + player.getSeat() + "_" + NxMjDisAction.action_moMjiang + "_"
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
			// 如果杠了之后，摸牌不能杠，那有杠也不能杠
			if (!player.getGang().isEmpty() && !checkSameMj(player.getPeng(), majiang)) {
				arr.set(NxMjAction.MINGGANG, 0);
				arr.set(NxMjAction.ANGANG, 0);
				arr.set(NxMjAction.BUZHANG, 0);
				arr.set(NxMjAction.BUZHANG_AN, 0);
			}
			// 报听 不能杠
			if (player.isBaoting()) {
				arr.set(NxMjAction.MINGGANG, 0);
				arr.set(NxMjAction.ANGANG, 0);
				arr.set(NxMjAction.BUZHANG, 0);
				arr.set(NxMjAction.BUZHANG_AN, 0);
			}
			coverAddActionSeat(player.getSeat(), arr);
		}
		MoMajiangRes.Builder res = MoMajiangRes.newBuilder();
		res.setUserId(player.getUserId() + "");
		res.setRemain(getLeftMajiangCount());
		res.setSeat(player.getSeat());

		// boolean playCommand = !player.getGang().isEmpty() && arr.isEmpty();
		logMoMj(player, majiang, arr);
		for (NxMjPlayer seat : seatMap.values()) {
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

	private boolean checkSameMj(List<NxMj> list, NxMj majiang) {
		if (list.size() == 0) {
			return false;
		}
		for (NxMj mj : list) {
			if (mj.getVal() == majiang.getVal()) {
				return true;
			}
		}
		return false;
	}

	public void calcMoLastSeats(int firstSeat) {
		for (int i = 0; i < getMaxPlayerCount(); i++) {
			NxMjPlayer player = seatMap.get(firstSeat);
//			if (player.isTingPai(-1)) {
				setFristLastMajiangSeat(player.getSeat());
				addMoLastSeat(player.getSeat());
//			}
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
		NxMjPlayer player = seatMap.get(sendSeat);
		sendMoLast(player, 1);
	}

	private void buildPlayRes(PlayMajiangRes.Builder builder, Player player, int action, List<NxMj> majiangs) {
		NxMjResTool.buildPlayRes(builder, player, action, majiangs);
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
	 *            小胡类型 NxMjAction
	 * @param action
	 */
	public synchronized void huXiaoHu(NxMjPlayer player, List<NxMj> majiangs, int xiaoHuType, int action) {
		List<Integer> actionList = actionSeatMap.get(player.getSeat());
		if (actionList == null || actionList.isEmpty() || actionList.get(xiaoHuType) == 0) {// 不能胡该小胡
			return;
		}

		NxMjiangHu hu = new NxMjiangHu();
		List<NxMj> copy2 = new ArrayList<>(player.getHandMajiang());
		NxMjRule.checkXiaoHu2(hu, copy2, isBegin(), this);

		HashMap<Integer, Map<Integer, List<NxMj>>> xiaohuMap = hu.getXiaohuMap();
		Map<Integer, List<NxMj>> map = xiaohuMap.get(xiaoHuType);
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
		
		
		List<NxMj> vals= map.get(huCard);
		
		addXiaoHuCards(player, xiaoHuType, vals);
        player.addXiaoHuMjList(majiangs);
//		
//		if(xiaoHuType ==NxMjAction.LIULIUSHUN||xiaoHuType==NxMjAction.ZHONGTULIULIUSHUN){
//			
//		}else{
//			player.addXiaoHu2(xiaoHuType, huCard);
//		}
		
		
		
	

		removeActionSeat(player.getSeat());
		addPlayLog(disCardRound + "_" + player.getSeat() + "_" + NxMjDisAction.action_xiaohu + "_"
				+ NxMjHelper.toMajiangStrs(majiangs) + "_" + xiaoHuType);
		PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
		buildPlayRes(builder, player, NxMjDisAction.action_xiaohu, majiangs);
		builder.addHuArray(xiaoHuType);
		boolean isBegin = isBegin();
		List<Integer> selfActList = player.checkMo(null, isBegin);
		if (!selfActList.isEmpty()) {
			if (isBegin) {
				if (hasXiaoHu(selfActList) || hasBaoTing(selfActList)) {
					addActionSeat(player.getSeat(), selfActList);
				}
			} else {
				addActionSeat(player.getSeat(), selfActList);
			}
		}

		logAction(player, action, xiaoHuType, majiangs, selfActList);
		for (NxMjPlayer seat : seatMap.values()) {
			PlayMajiangRes.Builder copy = builder.clone();
			if (actionSeatMap.containsKey(seat.getSeat())) {
				copy.addAllSelfAct(actionSeatMap.get(seat.getSeat()));
			}
			seat.writeSocket(copy.build());
		}
		calcXiaoHuPoint(player, xiaoHuType);
		addShowMjSeat(player.getSeat(), xiaoHuType);
		checkBegin(player);
	}
	
	public synchronized void baoting(NxMjPlayer player, List<NxMj> majiangs, int xiaoHuType, int action) {
		List<Integer> actionList = actionSeatMap.get(player.getSeat());
		if (actionList == null || actionList.isEmpty() || actionList.get(NxMjAction.BAOTING) == 0) {// 不能报听
			return;
		}
		boolean isBegin = isBegin();
		if(!isBegin){
			return;
		}
		removeActionSeat(player.getSeat());
		addPlayLog(disCardRound + "_" + player.getSeat() + "_" + NxMjDisAction.action_baoting + "_"
				+ NxMjHelper.toMajiangStrs(majiangs) + "_" + xiaoHuType);
		PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
		buildPlayRes(builder, player, NxMjDisAction.action_baoting, majiangs);
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
		for (NxMjPlayer seat : seatMap.values()) {
			PlayMajiangRes.Builder copy = builder.clone();
			if (actionSeatMap.containsKey(seat.getSeat())) {
				copy.addAllSelfAct(actionSeatMap.get(seat.getSeat()));
			}
			seat.writeSocket(copy.build());
		}
		checkBegin(player);
	}
	
	
	
	
	public boolean checkXiaoHuCards(int type,int val){
		
		for (NxMjPlayer seat : seatMap.values()) {
			List<Integer> vals = seat.getHuxiaoHuCardVal().get(type);
			if(vals!=null && vals.contains(val)){
				return true;
			}
		}
		return false;
	}
	

	private void addXiaoHuCards(NxMjPlayer player, int xiaoHuType, List<NxMj> mjVals) {
		List<Integer> valus = new ArrayList<Integer>();
		if(mjVals!=null && !mjVals.isEmpty()){
			for(NxMj mj: mjVals){
				valus.add(mj.getVal());
			}
		}
	
		player.addLiuLiuShunHu2(xiaoHuType, valus);
	}

	/**
	 * 如果是起手判断是否还有人可胡小胡，检查庄家发牌后有没有操作，没有的话通知庄家出牌
	 */
	public void checkBegin(NxMjPlayer player) {
		boolean isBegin = isBegin();
		if (isBegin && !hasXiaoHu() && !hasBaoTing()) {
			NxMjPlayer bankPlayer = seatMap.get(lastWinSeat);
			List<Integer> actList = bankPlayer.checkMo(null, isBegin);
			if (!actList.isEmpty()) {
				PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
				buildPlayRes(builder, player, NxMjDisAction.action_pass, new ArrayList<>());
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
    private void hu(NxMjPlayer player, List<NxMj> majiangs, int action) {
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
        NxMj disMajiang = null;
        NxMjiangHu huBean = null;
        List<NxMj> huMjs = new ArrayList<>();
        int fromSeat = 0;
        boolean isGangShangHu = false;



        if (!zimo) {
			if (moGangHuList.contains(player.getSeat())) {// 强杠胡
                disMajiang = moGang;
                fromSeat = moMajiangSeat;
                huMjs.add(moGang);
			} else if (isHasGangAction(player.getSeat())) {// 杠上炮 杠上花
                fromSeat = moMajiangSeat;
                Map<Integer, NxMjiangHu> huMap = new HashMap<>();
                List<Integer> daHuMjIds = new ArrayList<>();
                List<Integer> huMjIds = new ArrayList<>();
                int dahuPoint = -1;
                for (int majiangId : gangSeatMap.keySet()) {
                    NxMjiangHu temp = player.checkHu(NxMj.getMajang(majiangId), disCardRound == 0);
                    if(!temp.isHu()){
                        continue;
                    }
                    temp.initDahuList();
                    if(gangMoSi == 0){
                        huMap.put(majiangId,temp);
                        huMjIds.add(majiangId);
                        if(temp.isDahu()){
                            daHuMjIds.add(majiangId);
                        }
                    }
                   
                    if(gangMoSi == 1 && temp.getDahuPoint() > dahuPoint){
                    	dahuPoint = temp.getDahuPoint();
                    	 huMap.clear();
                    	 huMap.put(majiangId,temp);
                    	 huMjIds.clear();
                         huMjIds.add(majiangId);
                         daHuMjIds.clear();
                         if(temp.isDahu()){
                             daHuMjIds.add(majiangId);
                         }
                    }
                }
                if(daHuMjIds.size() >0){
					// 有大胡
                    for(int mjId : huMjIds){
                        NxMjiangHu temp = huMap.get(mjId);
                        if (moMajiangSeat == player.getSeat()) {
                            temp.setGangShangHua(true);
                            isGangShangHu = true;
                        } else {
							// 出掉杠牌
                        	NxMjPlayer mPlayer = seatMap.get(moMajiangSeat);
                       	 	removeGangMj(mPlayer, mjId);
                            temp.setGangShangPao(true);
                        }
                        temp.initDahuList();
                        if(huBean == null){
                            huBean = temp;
                        }else{
                            huBean.addToDahu(temp.getDahuList());
                            huBean.getShowMajiangs().add(NxMj.getMajang(mjId));
                        }
                        player.addHuMjId(mjId);
                        huMjs.add(NxMj.getMajang(mjId));
                    }
                }else if(huMjIds.size() > 0){
					// 没有大胡
                    for(int mjId : huMjIds) {
                        NxMjiangHu temp = huMap.get(mjId);
                        if (moMajiangSeat == player.getSeat()) {
                            temp.setGangShangHua(true);
                            isGangShangHu = true;
                        } else {
                        	NxMjPlayer mPlayer = seatMap.get(moMajiangSeat);
                        	removeGangMj(mPlayer, mjId);
                            temp.setGangShangPao(true);
                        }
                        temp.initDahuList();
                        if(huBean == null){
                            huBean = temp;
                        }else{
                            huBean.addToDahu(temp.getDahuList());
                            huBean.getShowMajiangs().add(NxMj.getMajang(mjId));
                        }
                        player.addHuMjId(mjId);
                        huMjs.add(NxMj.getMajang(mjId));
                    }
                }else{
                    huBean = new NxMjiangHu();
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

            } else if (!nowDisCardIds.isEmpty()) {
                disMajiang = nowDisCardIds.get(0);
                fromSeat = disCardSeat;
                huMjs.add(disMajiang);
            }
        }else{
            huMjs.add(player.getHandMajiang().get(player.getHandMajiang().size()-1));
        }
        if(isBegin()){
		 //天胡
            if(huBean == null){
                huBean = player.checkHu(null,true);
            }
            if(huBean.isHu()){
                huBean.setTianhu(true);
                huBean.initDahuList();
            }
        //}else if(disCardSeat == lastWinSeat && seatMap.get(lastWinSeat).getOutPais().size() == 1 && nowDisCardIds.size() == 1){
          }else if(isDihu() && !zimo){
		 //地胡
            if(huBean == null){
                huBean = player.checkHu(nowDisCardIds.get(0),true);
            }
            if(huBean.isHu()){
                huBean.setDihu(true);
                huBean.initDahuList();
            }
        }

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
        	NxMjPlayer disPlayer = seatMap.get(disCardSeat);
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
		// 检查门清
//        if(!huBean.isMenqing()&&(getMenqing()==1 &&!player.isChiPengGang())) {
//			huBean.setMenqing(true);
//			 huBean.initDahuList();
//		}
        if(!huBean.isMenqing()&&(getMenqingZM()==1 &&!player.isMenqing()&&zimo)) {
			if(player.getaGang().size() == 0){
				huBean.setMenqing(true);
				huBean.initDahuList();
			}else if(player.getaGang().size() == 4 && isGangShangHu){
				huBean.setMenqing(true);
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
            if (disEventAction != NxMjDisAction.action_buzhang) {
                huBean.setQGangHu(true);
                huBean.initDahuList();
            }
			// 抢杠胡
          //  NxMjPlayer moGangPlayer = getPlayerByHasMajiang(moGang);
            //if (moGangPlayer == null) {
            NxMjPlayer    moGangPlayer = seatMap.get(nowDisCardSeat);
            //}
            List<NxMj> moGangMajiangs = new ArrayList<>();
            moGangMajiangs.add(moGang);
            moGangPlayer.removeGangMj(moGangMajiangs.get(0));
//            if(huBean.isQGangHu()) {
//            	
//            }else {
//            	moGangPlayer.addOutPais(moGangMajiangs, 0,0);
//            }
			// 摸杠被人胡了 相当于自己出了一张牌
            recordDisMajiang(moGangMajiangs, moGangPlayer);
           // addPlayLog(disCardRound + "_" + moGangPlayer.getSeat() + "_" + 0 + "_" + NxMjHelper.toMajiangStrs(moGangMajiangs));
        }
        if (huBean.getDahuPoint() > 0) {
            player.setDahu(huBean.getDahuList());
        }
//        if (huBean.getDahuPoint() > 0) {
//            player.setDahu(huBean.getDahuList());
//            if (zimo) {
//                int point = 0;
//                for (NxMjPlayer seatPlayer : seatMap.values()) {
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
            List<NxMj> gangDisMajiangs = getGangDisMajiangs();
            List<NxMj> chuMjs = new ArrayList<>();
            if(gangDisMajiangs != null && gangDisMajiangs.size() >0){
                for(NxMj mj : gangDisMajiangs){
                    if(!huMjs.contains(mj)){
                        chuMjs.add(mj);
                    }
                }
            }
            if(chuMjs != null){
                PlayMajiangRes.Builder chuPaiMsg = PlayMajiangRes.newBuilder();
                buildPlayRes(chuPaiMsg, player, NxMjDisAction.action_chupai, chuMjs);
                chuPaiMsg.setFromSeat(-1);
                broadMsgToAll(chuPaiMsg.build());
                player.addOutPais(chuMjs, NxMjDisAction.action_chupai,player.getSeat());
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
        for (NxMjPlayer seat : seatMap.values()) {
			// 推送消息
            seat.writeSocket(builder.build());
        }
		// 加入胡牌数组
        addHuList(player.getSeat(), disMajiang == null ? 0 : disMajiang.getId());
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_"+ NxMjHelper.toMajiangStrs(huMjs)+"_"+StringUtil.implode(player.getDahu(), ","));
        if (isCalcOver()) {
			// 等待别人胡牌 如果都确认完了，胡
            calcOver();
        }else{
            player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip);
        }
    }

    public boolean isDihu(){
//    	return disCardSeat == lastWinSeat && seatMap.get(lastWinSeat).getOutPais().size() == 1;
    	return false;//2020.4.13 不要地胡
    }
    
	private void checkRemoveMj(NxMjPlayer player, int action) {
		//NxMj mjA = null;
        NxMj mjB = null;
		for (int majiangId2 : gangSeatMap.keySet()) {
			Map<Integer, List<Integer>> map = gangSeatMap.get(majiangId2);
			List<Integer> actList = map.get(player.getSeat());
			if (actList == null) {
				continue;
			}
//			if (actList.get(NxMjAction.ZIMO) == 1) {
//				mjA = NxMj.getMajang(majiangId2);
//			} else

			if (actList.get(NxMjAction.MINGGANG) == 1) {
				mjB = NxMj.getMajang(majiangId2);
			}
        }


		if(mjB!=null) {
//			if(mjA.getId() !=mjB.getId()) {
			// 从手牌移除掉
				List<NxMj> list = new ArrayList<>();
				list.add(mjB);
				checkMoOutCard(list, player, action);
//			}
		}
	}

	private void removeGangMj(NxMjPlayer player, int mjId) {
		List<NxMj> moList = new ArrayList<>();
		moList.add(NxMj.getMajang(mjId));
		player.addOutPais(moList, 0, player.getSeat());
	}

	/**
	 * 找出拥有这张麻将的玩家
	 *
	 * @param majiang
	 * @return
	 */
	private NxMjPlayer getPlayerByHasMajiang(NxMj majiang) {
		for (NxMjPlayer player : seatMap.values()) {
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
			NxMjPlayer moGangPlayer = null;
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
			NxMjPlayer disCSMajiangPlayer = seatMap.get(disCardSeat);
			for (int huseat : huActionList) {
				if (huConfirmMap.containsKey(huseat)) {
					if (disCardRound == 0) {
						// 天胡
						removeActionSeat(huseat);
					}
					continue;
				}
				PlayMajiangRes.Builder disBuilder = PlayMajiangRes.newBuilder();
				NxMjPlayer seatPlayer = seatMap.get(huseat);
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
	private void chiPengGang(NxMjPlayer player, List<NxMj> majiangs, int action) {
		List<Integer> actionList0 = actionSeatMap.get(player.getSeat());
		if(actionList0==null ||actionList0.isEmpty()){
			return;
		}
		
		PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
		logAction(player, action, 0, majiangs, null);
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
			for (NxMj mj : nowDisCardIds) {
				List<Integer> hu = player.checkDisMaj(mj, false);
				if (!hu.isEmpty() && hu.get(0) == 1) {
					// && (actionList.get(NxMjAction.HU) == 1)
					List<Integer> actionList = actionSeatMap.get(player.getSeat());
					if (actionList != null) {
						actionList.set(NxMjAction.HU, 0);
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

		List<NxMj> handMajiang = new ArrayList<>(player.getHandMajiang());
		NxMj disMajiang = null;
		if (isHasGangAction()) {
			for (int majiangId : gangSeatMap.keySet()) {
				if (action == NxMjDisAction.action_chi) {
					List<Integer> majiangIds = NxMjHelper.toMajiangIds(majiangs);
					if (majiangIds.contains(majiangId)) {
						disMajiang = NxMj.getMajang(majiangId);
						gangActedMj = disMajiang;
						handMajiang.add(disMajiang);
						if (majiangs.size() > 1) {
							majiangs.remove(disMajiang);
						}
						break;
					}
				} else {
					NxMj mj = NxMj.getMajang(majiangId);
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
			sameCount = NxMjHelper.getMajiangCount(majiangs, majiangs.get(0).getVal());
		}
		if (action == NxMjDisAction.action_buzhang) {
			if (sameCount == 0) {
				majiangs.add(disMajiang);
			}
			majiangs = NxMjHelper.getMajiangList(player.getHandMajiang(), majiangs.get(0).getVal());
			sameCount = majiangs.size();
			if (sameCount == 0) {
				majiangs.add(disMajiang);
			}
		} else if (action == NxMjDisAction.action_minggang) {
			if (majiangs.size() == 0) {
				majiangs.add(disMajiang);
			}
			// 如果是杠 后台来找出是明杠还是暗杠
			majiangs = NxMjHelper.getMajiangList(player.getHandMajiang(), majiangs.get(0).getVal());
			sameCount = majiangs.size();
			if (sameCount == 4) {
				// 有4张一样的牌是暗杠
				action = NxMjDisAction.action_angang;
			} else if (sameCount == 0) {
				majiangs.add(disMajiang);
			}
			// 其他是明杠

		} else if (action == NxMjDisAction.action_buzhang_an) {
			// 暗杠补张
			majiangs = NxMjHelper.getMajiangList(player.getHandMajiang(), majiangs.get(0).getVal());
			sameCount = majiangs.size();
		}
		// /////////////////////
		if (action == NxMjDisAction.action_chi) {
			boolean can = canChi(player, player.getHandMajiang(), majiangs, disMajiang);
			if (!can) {
				return;
			}
		} else if (action == NxMjDisAction.action_peng) {
			boolean can = canPeng(player, majiangs, sameCount, disMajiang);
			if (!can) {
				return;
			}
		} else if (action == NxMjDisAction.action_angang) {
			boolean can = canAnGang(player, majiangs, sameCount, action);
			if (!can) {
				player.writeErrMsg("不能杠此张牌");
				return;
			}
			// 如果只剩下一张牌 问要不要&& isBuzhang
			if (getLeftMajiangCount() == 1) {
				player.writeErrMsg("海底不能杠");
				return;
			}
			if (!player.isTingPai(majiangs.get(0).getVal())) {
				player.writeErrMsg("不能杠此张牌");
				return;
			}
		} else if (action == NxMjDisAction.action_minggang) {
			boolean can = canMingGang(player, player.getHandMajiang(), majiangs, sameCount, disMajiang);
			if (!can) {
				player.writeErrMsg("不能杠此张牌");
				return;
			}

			// 如果只剩下一张牌 问要不要&& isBuzhang
			if (getLeftMajiangCount() == 1) {
				player.writeErrMsg("海底不能杠");
				return;
			}

			if (player.getGangCGangMjs().size() == 2 && disMajiang != null) {
				player.getGangCGangMjs().remove((Integer)disMajiang.getId());
				List<NxMj> list = new ArrayList<>();
				list.add(NxMj.getMajang(player.getGangCGangMjs().remove(0)));
				checkMoOutCard(list, player, action);
			}
			
			
			if (!player.isTingPai(majiangs.get(0).getVal())) {
				player.writeErrMsg("不能杠此张牌");
				return;
			}
			
			// 特殊处理一张牌明杠的时候别人可以 胡
			if (sameCount == 1 && canGangHu()) {
				if (checkQGangHu(player, majiangs, action)) {
					// return;
					moMj = false;
				}
			}
		} else if (action == NxMjDisAction.action_buzhang) {
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
				if (checkQGangHu(player, majiangs, action)) {
					// 抢杠胡可以杠下来
					// return;
					moMj = false;
				}
			}
		} else if (action == NxMjDisAction.action_buzhang_an) {
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
		} else {
			return;
		}
		calcPoint(player, action, sameCount, majiangs);
		boolean disMajiangMove = false;
		if (disMajiang != null) {
			// 碰或者杠
			if (action == NxMjDisAction.action_minggang && sameCount == 3) {
				// 接杠
				disMajiangMove = true;
			} else if (action == NxMjDisAction.action_chi) {
				// 吃
				disMajiangMove = true;
			} else if (action == NxMjDisAction.action_peng) {
				// 碰
				disMajiangMove = true;
			} else if (action == NxMjDisAction.action_buzhang && sameCount == 3) {
				// 自己三张补张
				disMajiangMove = true;
			}
		}
		if (disMajiangMove) {
			if (action == NxMjDisAction.action_chi) {
				majiangs.add(1, disMajiang);// 吃的牌放第二位
			} else {
				majiangs.add(disMajiang);
			}
			builder.setFromSeat(disCardSeat);
			List<NxMj> disMajiangs = new ArrayList<>();
			disMajiangs.add(disMajiang);
			seatMap.get(disCardSeat).removeOutPais(disMajiangs, action);
		}
		chiPengGang(builder, player, majiangs, action, moMj);
	}

	private void chiPengGang(PlayMajiangRes.Builder builder, NxMjPlayer player, List<NxMj> majiangs, int action,
			boolean moMj) {
		setIsBegin(false);
		processHideMj(player);

		player.addOutPais(majiangs, action, disCardSeat);
		buildPlayRes(builder, player, action, majiangs);
		List<Integer> removeActList = removeActionSeat(player.getSeat());
		clearGangActionMap();
		if (moMj) {
			clearActionSeatMap();
		}

		addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + NxMjHelper.toMajiangStrs(majiangs));
		// 不是普通出牌
		setNowDisCardSeat(player.getSeat());
		checkClearGangDisMajiang();
		if (action == NxMjDisAction.action_chi || action == NxMjDisAction.action_peng) {
			List<Integer> arr = player.checkMo(null, false);
			// 吃碰之后还有操作
			if (!arr.isEmpty()) {
				arr.set(NxMjAction.ZIMO, 0);
				arr.set(NxMjAction.HU, 0);
				arr.set(NxMjAction.ZHONGTULIULIUSHUN, 0);
				arr.set(NxMjAction.ZHONGTUSIXI, 0);
				addActionSeat(player.getSeat(), arr);
			}
		}
		for (NxMjPlayer seatPlayer : seatMap.values()) {
			// 推送消息
			PlayMajiangRes.Builder copy = builder.clone();
			if (actionSeatMap.containsKey(seatPlayer.getSeat())) {
				copy.addAllSelfAct(actionSeatMap.get(seatPlayer.getSeat()));
			}
			seatPlayer.writeSocket(copy.build());
		}

		// 取消漏炮
		player.setPassMajiangVal(0);
		if (action == NxMjDisAction.action_minggang || action == NxMjDisAction.action_angang) {
			// 明杠和暗杠摸牌
			if (moMj) {
				gangMoMajiang(player, majiangs.get(0), action);
			}

		} else if (action == NxMjDisAction.action_buzhang) {
			// 补张
			if (moMj) {
				moMajiang(player, true);
			}

		} else if (action == NxMjDisAction.action_buzhang_an) {
			// 补张
			moMajiang(player, true);

		}

		if (action == NxMjDisAction.action_chi || action == NxMjDisAction.action_peng) {
			sendTingInfo(player);
		}

		setDisEventAction(action);
		robotDealAction();
		logAction(player, action, 0, majiangs, removeActList);
	}

	/**
	 * 杠后摸两张牌
	 */
	private void gangMoMajiang(NxMjPlayer player, NxMj gangMajiang, int action) {
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
		int leftMjCount = getLeftMajiangCount();
		if (leftMjCount == 0) {
			calcOver();
			return;
		}
		// 连摸两张牌
		int moNum = 2;
		
		if(gangMoSi==1) {
			moNum=4;
		}
		List<NxMj> moList = new ArrayList<>();
		Random r = new Random();
		gangDice = (r.nextInt(6) + 1) * 10 + (r.nextInt(6) + 1);
//		int leftMjCount = getLeftMajiangCount();
		int leftDuo = leftMjCount % 2 == 0 ? leftMjCount / 2 : leftMjCount / 2 + 1;
//		if (leftDuo >= gangDice / 10 + gangDice % 10) {
			if (GameServerConfig.isDeveloper()) {
				NxMj majiang1 = NxMjHelper.findMajiangByVal(leftMajiangs,31);
				if (majiang1 != null) {
					leftMajiangs.remove(majiang1);
					moList.add(majiang1);
				}
				NxMj majiang2 = NxMjHelper.findMajiangByVal(leftMajiangs, 33);
				if (majiang2 != null) {
					leftMajiangs.remove(majiang2);
					moList.add(majiang2);
				}
				if(gangMoSi==1) {
					NxMj majiang3 = NxMjHelper.findMajiangByVal(leftMajiangs,28);
					if (majiang3 != null) {
						leftMajiangs.remove(majiang3);
						moList.add(majiang3);
					}
					NxMj majiang4 = NxMjHelper.findMajiangByVal(leftMajiangs, 28);
					if (majiang4 != null) {
						leftMajiangs.remove(majiang4);
						moList.add(majiang4);
					}
				}
			}
		int mjCount = getLeftMajiangCount();
		if(mjCount==1){
			moNum=0;
		}else if(mjCount==2){
			moNum =1;
		}else if(mjCount==3){
			moNum =2;
		}else if(mjCount==4){
			if(gangMoSi==1) {
				moNum=3;
			}
		}
			while (moList.size() < moNum) {
				NxMj majiang = getLeftMajiang();
				if (majiang != null) {
					moList.add(majiang);
				} else {
					break;
				}
			}
			addMoTailPai(gangDice);
//		}

		addPlayLog(disCardRound + "_" + player.getSeat() + "_" + NxMjDisAction.action_moGangMjiang + "_" + gangDice
				+ "_" + NxMjHelper.implodeMajiang(moList, ","));

		// 检查摸牌
		clearActionSeatMap();
		clearGangActionMap();
		// 打出这两张牌
		setDisCardSeat(player.getSeat());
		setGangDisMajiangs(moList);
		setMoMajiangSeat(player.getSeat());
		player.setPassMajiangVal(0);
		//记录当局杠状态
		player.setBureauStatus(1);
		
		setGangMajiang(gangMajiang);
		setNowDisCardSeat(calcNextSeat(player.getSeat()));
		// setNowDisCardSeat(player.getSeat());
		setNowDisCardIds(moList);
		// player.addOutPais(moList, 0,player.getSeat());
		// /////////////////////////////////////////////////////////////////////////////////////////
        logGangMoMj(player,moList);
		boolean canHu = false;
		List<NxMj> moGangMj1s = new ArrayList<>();
		// 摸了牌后可以胡牌
		for (NxMj majiang : moList) {
			for (NxMjPlayer seatPlayer : seatMap.values()) {
				List<Integer> actionList = seatPlayer.checkDisMaj(majiang, false,true);
				actionList = NxMjAction.keepHu(actionList);
				if(!actionList.contains(1)){
					actionList = Collections.EMPTY_LIST;
				}
				if (seatPlayer.getSeat() == player.getSeat()) {
					// 摸杠人只能胡
					if (NxMjAction.hasHu(actionList)) {
						boolean addGang = false;
						if (NxMjAction.hasGang(actionList)) {
							addGang = true;
						}
						actionList = NxMjAction.keepHu(actionList);
						actionList.set(NxMjAction.HU, 0);
						actionList.set(NxMjAction.ZIMO, 1);
						if (addGang) {
							actionList.set(NxMjAction.MINGGANG, 1);
							actionList.set(NxMjAction.BUZHANG, 1);
							moGangMj1s.add(majiang);
							player.addGangcGangMj(majiang.getId());
							// seatPlayer.moMajiang(majiang);
						}
						canHu = true;
						addActionSeat(player.getSeat(), actionList);
						List<Integer> list2 = new ArrayList<Integer>(actionList);
						addGangActionSeat(majiang.getId(), player.getSeat(), list2);
						logAction(seatPlayer, action, -1, Arrays.asList(majiang), actionList);
					} else if (NxMjAction.hasGang(actionList)) {
						actionList = NxMjAction.keepHu(actionList);
						actionList.set(NxMjAction.MINGGANG, 1);
						actionList.set(NxMjAction.BUZHANG, 1);
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
				for (NxMj moMj : moList) {
					Map<Integer, List<Integer>> seatActionList = gangSeatMap.get(moMj.getId());
					if (seatActionList != null && seatActionList.containsKey(player.getSeat())) {
						continue;
					}
					List<NxMj> list = new ArrayList<>();
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
				for (NxMj moMj : moList) {
					GangPlayMajiangRes.Builder playerMsg = GangPlayMajiangRes.newBuilder();
					playerMsg.setMajiangId(moMj.getId());
					Map<Integer, List<Integer>> seatActionList = gangSeatMap.get(moMj.getId());

					if (seatActionList != null && seatActionList.containsKey(player.getSeat())) {
						playerMsg.addAllSelfAct(seatActionList.get(player.getSeat()));
					}
					gangMsg.addGangActs(playerMsg);
				}
				player.writeSocket(gangMsg.build());

				for (NxMjPlayer seatPlayer : seatMap.values()) {
					if (player.getSeat() != seatPlayer.getSeat()) {
						gangMsg.clearGangActs();
						seatPlayer.writeSocket(gangMsg.build());
						// 开杠人能胡，必胡，去掉其他人的所有操作
						removeActionSeat(seatPlayer.getSeat());
					}
//					gangNoticePlayer(seatPlayer, gangMajiang, moList);
				}
				gangNoticePlayer(player, gangMajiang, moList);
			}

		} else {
			// 自己打出两牌
			player.addOutPais(moList, 0, player.getSeat());

			addPlayLog(disCardRound + "_" + player.getSeat() + "_" + 0 + "_" + NxMjHelper.toMajiangStrs(moList));
			gangNoticePlayer(player, gangMajiang, moList);

			PlayMajiangRes.Builder chuPaiMsg = PlayMajiangRes.newBuilder();
			buildPlayRes(chuPaiMsg, player, NxMjDisAction.action_chupai, moList);
			for (NxMjPlayer seatPlayer : seatMap.values()) {
				chuPaiMsg.setFromSeat(-1);
				seatPlayer.writeSocket(chuPaiMsg.build());
			}
			broadMsgRoomPlayer(chuPaiMsg.build());

			sendTingInfo(player);
			if (isHasGangAction()) {
				// 如果有人能做动作
				robotDealAction();
			} else {
				checkMo();
			}
		}
	}

	private void checkMoOutCard(List<NxMj> list, NxMjPlayer player, int action) {

		player.addOutPais(list, 0, player.getSeat());
		addPlayLog(disCardRound + "_" + player.getSeat() + "_" + 0 + "_" + NxMjHelper.toMajiangStrs(list));
		logAction(player, action, 0, list, null);
		PlayMajiangRes.Builder chuPaiMsg = PlayMajiangRes.newBuilder();
		buildPlayRes(chuPaiMsg, player, NxMjDisAction.action_chupai, list);
		for (NxMjPlayer seatPlayer : seatMap.values()) {
			chuPaiMsg.setFromSeat(-1);
			seatPlayer.writeSocket(chuPaiMsg.build());
		}
	}

	private void gangNoticePlayer(NxMjPlayer player, NxMj gangMajiang, List<NxMj> moList) {
		// 发送摸牌消息res
		GangMoMajiangRes.Builder gangMsg = null;
		for (NxMjPlayer seatPlayer : seatMap.values()) {
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
			for (NxMj majiang : moList) {
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

	private boolean checkQGangHu(NxMjPlayer player, List<NxMj> majiangs, int action) {
		PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
		Map<Integer, List<Integer>> huListMap = new HashMap<>();
		for (NxMjPlayer seatPlayer : seatMap.values()) {
			if (seatPlayer.getUserId() == player.getUserId()) {
				continue;
			}
			// 推送消息
			List<Integer> hu = seatPlayer.checkDisMaj(majiangs.get(0), false);
			hu = NxMjAction.keepHu(hu);
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
				NxMjPlayer seatPlayer = seatMap.get(entry.getKey());
				copy.addAllSelfAct(entry.getValue());
				seatPlayer.writeSocket(copy.build());
			}
			return true;
		}
		return false;

	}

	public void checkSendGangRes(Player player) {
		if (isHasGangAction()) {
			List<NxMj> moList = getGangDisMajiangs();
			NxMjPlayer disPlayer = seatMap.get(disCardSeat);
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
			for (NxMj mj : moList) {
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
	private void chuPai(NxMjPlayer player, List<NxMj> majiangs, int action) {
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
		if (!player.getGang().isEmpty()) {
			// 已经杠过了牌
			if (player.getLastMoMajiang().getId() != majiangs.get(0).getId()) {
				return;
			}
		}
		if (!actionSeatMap.isEmpty() && player.getGang().isEmpty()) {// 出牌自动过掉手上操作
			guo(player, null, NxMjDisAction.action_pass);
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
		setNowDisCardSeat(calcNextSeat(player.getSeat()));
		recordDisMajiang(majiangs, player);
		player.addOutPais(majiangs, action, player.getSeat());
		player.clearPassHu();
		logAction(player, action, 0, majiangs, null);
		for (NxMjPlayer seat : seatMap.values()) {
			List<Integer> list = new ArrayList<>();
			if (seat.getUserId() != player.getUserId()) {
				list = seat.checkDisMajiang(majiangs.get(0));
				if (list.contains(1)) {
					// 如果杠了之后，別人出的牌不能做杠操作
					if (!seat.getGang().isEmpty()) {
						list.set(NxMjAction.MINGGANG, 0);
						list.set(NxMjAction.ANGANG, 0);
						list.set(NxMjAction.BUZHANG, 0);
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
		addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + NxMjHelper.toMajiangStrs(majiangs));
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
			if (actionList.get(NxMjAction.HU) == 1 || actionList.get(NxMjAction.ZIMO) == 1) {
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
			for (NxMjPlayer seatPlayer : seatMap.values()) {
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
			for (NxMjPlayer seat : seatMap.values()) {
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

	private void err(NxMjPlayer player, int action, String errMsg) {
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
	public synchronized void playCommand(NxMjPlayer player, List<NxMj> majiangs, int action) {
		if (!moGangHuList.isEmpty()) {// 被人抢杠胡
			if (!moGangHuList.contains(player.getSeat())) {
				return;
			}
		}

		if (NxMjDisAction.action_hu == action) {
			hu(player, majiangs, action);
			return;
		}
		// 手上没有要出的麻将
		if (!isHasGangAction() && action != NxMjDisAction.action_minggang && action != NxMjDisAction.action_buzhang)
			if (!player.getHandMajiang().containsAll(majiangs)) {
				err(player, action, "没有找到出的牌" + majiangs);
				return;
			}
		changeDisCardRound(1);
		if (action == NxMjDisAction.action_pass) {
			guo(player, majiangs, action);
		} else if (action == NxMjDisAction.action_moMjiang) {
		}else if (action != 0) {
			if (hasXiaoHu()) {
				return;
			}
			chiPengGang(player, majiangs, action);
		} else {
			if (isBegin() && hasXiaoHu()) {
				return;
			}
			chuPai(player, majiangs, action);
		}

	}

	/**
	 * 最后一张牌(海底捞)
	 *
	 * @param player
	 * @param action
	 */
	public synchronized void moLastMajiang(NxMjPlayer player, int action) {
		if (getLeftMajiangCount() != 1) {
			return;
		}
		if (player.getSeat() != askLastMajaingSeat) {
			return;
		}

		if (action == NxMjDisAction.action_passmo) {
			// 发送下一个海底摸牌res
			sendMoLast(player, 0);
			removeMoLastSeat(player.getSeat());
			if (moLastSeats == null || moLastSeats.size() == 0) {
				calcOver();
				return;
			}
			sendAskLastMajiangRes(0);
			addPlayLog(disCardRound + "_" + player.getSeat() + "_" + NxMjDisAction.action_pass + "_");
		} else {
			sendMoLast(player, 0);
			clearMoLastSeat();
			clearActionSeatMap();
			setMoLastMajiangSeat(player.getSeat());
			NxMj majiang = getLeftMajiang();
			addPlayLog(disCardRound + "_" + player.getSeat() + "_" + NxMjDisAction.action_moLastMjiang + "_"
					+ majiang.getId());
			setMoMajiangSeat(player.getSeat());
			player.setPassMajiangVal(0);
			setLastMajiang(majiang);
			setDisCardSeat(player.getSeat());

			// /////////////////////////////////////////////
			// 发送海底捞的牌

			// /////////////////////////////////////////

			List<NxMj> disMajiangs = new ArrayList<>();
			disMajiangs.add(majiang);

			MoMajiangRes.Builder moRes = MoMajiangRes.newBuilder();
			moRes.setUserId(player.getUserId() + "");
			moRes.setRemain(getLeftMajiangCount());
			moRes.setSeat(player.getSeat());

			// 先看看自己能不能胡
			List<Integer> selfActList = player.checkDisMajiang(majiang);
			player.moMajiang(majiang);
			selfActList = NxMjAction.keepHu(selfActList);
			if (selfActList != null && !selfActList.isEmpty()) {
				if (selfActList.contains(1)) {
					addActionSeat(player.getSeat(), selfActList);
				}
			}
			for (NxMjPlayer seatPlayer : seatMap.values()) {
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
			if (NxMjAction.hasHu(selfActList)) {
				// 优先自己胡
				// hu(player, null, NxMjDisAction.action_moLastMjiang_hu);
				return;
			} else {
				chuLastPai(player);
			}
			// for (int seat : actionSeatMap.keySet()) {
			// hu(seatMap.get(seat), null, action);
			// }
		}

	}

	private void chuLastPai(NxMjPlayer player) {
		NxMj majiang = lastMajiang;
		List<NxMj> disMajiangs = new ArrayList<>();
		disMajiangs.add(majiang);
		PlayMajiangRes.Builder chuRes = NxMjResTool.buildPlayRes(player, NxMjDisAction.action_chupai, disMajiangs);
		addPlayLog(disCardRound + "_" + player.getSeat() + "_" + NxMjDisAction.action_chupai + "_"
				+ NxMjHelper.toMajiangStrs(disMajiangs));
		setNowDisCardIds(disMajiangs);
		setNowDisCardSeat(calcNextSeat(player.getSeat()));
		recordDisMajiang(disMajiangs, player);
		player.addOutPais(disMajiangs, NxMjDisAction.action_chupai, player.getSeat());
		player.clearPassHu();
		for (NxMjPlayer seatPlayer : seatMap.values()) {
			if (seatPlayer.getUserId() == player.getUserId()) {
				seatPlayer.writeSocket(chuRes.clone().build());
				continue;
			}
			List<Integer> otherActList = seatPlayer.checkDisMajiang(majiang);
			otherActList = NxMjAction.keepHu(otherActList);
			PlayMajiangRes.Builder msg = chuRes.clone();
			if (NxMjAction.hasHu(otherActList)) {
				addActionSeat(seatPlayer.getSeat(), otherActList);
				msg.addAllSelfAct(otherActList);
			}
			seatPlayer.writeSocket(msg.build());
		}
		if (actionSeatMap.isEmpty()) {
			calcOver();
		}
	}

	private void passMoHu(NxMjPlayer player, List<NxMj> majiangs, int action) {
		if (!moGangHuList.contains(player.getSeat())) {
			return;
		}

		PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
		buildPlayRes(builder, player, action, majiangs);
		builder.setSeat(nowDisCardSeat);
		removeActionSeat(player.getSeat());
		player.writeSocket(builder.build());
		addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + NxMjHelper.toMajiangStrs(majiangs));
		if (isCalcOver()) {
			calcOver();
			return;
		}
		player.setPassMajiangVal(nowDisCardIds.get(0).getVal());

		NxMjPlayer moGangPlayer = seatMap.get(moMajiangSeat);
		if (moGangHuList.isEmpty()) {
			majiangs = new ArrayList<>();
			majiangs.add(moGang);
			if (disEventAction == NxMjDisAction.action_buzhang) {
				moMajiang(moGangPlayer, true);
			} else {
				gangMoMajiang(moGangPlayer, majiangs.get(0), disEventAction);
			}

			// calcPoint(moGangPlayer, NxMjDisAction.action_minggang, 1,
			// majiangs);
			// builder = PlayMajiangRes.newBuilder();
			// chiPengGang(builder, moGangPlayer, majiangs,
			// NxMjDisAction.action_minggang,true);
		}

	}

	/**
	 * guo
	 *
	 * @param player
	 * @param majiangs
	 * @param action
	 */
	private void guo(NxMjPlayer player, List<NxMj> majiangs, int action) {
		if (!actionSeatMap.containsKey(player.getSeat())) {
			return;
		}
		if (!moGangHuList.isEmpty()) {
			// 有摸杠胡的优先处理
			passMoHu(player, majiangs, action);
			return;
		}
		List<Integer> removeActionList = removeActionSeat(player.getSeat());
		int xiaoHu = NxMjAction.getFirstXiaoHu(removeActionList);
		logAction(player, action, xiaoHu, majiangs, removeActionList);
		boolean isBegin = isBegin();
		if (xiaoHu != -1) {
			player.addPassXiaoHu(xiaoHu);
			player.addPassXiaoHuList2(xiaoHu);
			List<Integer> actionList = player.checkMo(null, isBegin);
			if (!actionList.isEmpty()) {
				actionList.set(xiaoHu, 0);
				if (NxMjAction.getFirstXiaoHu(actionList) != -1) {
					// 过小胡后，还有小胡，直接提示小胡
					addActionSeat(player.getSeat(), actionList);
					PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
					buildPlayRes(builder, player, action, majiangs);
					builder.setSeat(nowDisCardSeat);
					builder.addAllSelfAct(actionList);
					player.writeSocket(builder.build());
					logAction(player, action, xiaoHu, majiangs, actionList);
					addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_"
							+ NxMjHelper.toMajiangStrs(majiangs));
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
		addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + NxMjHelper.toMajiangStrs(majiangs));
		if (isCalcOver()) {
			calcOver();
			return;
		}
		if (NxMjAction.hasHu(removeActionList) && disCardSeat != player.getSeat() && nowDisCardIds.size() == 1) {
			// 漏炮
			player.passHu(nowDisCardIds.get(0).getVal());
		}

		// nowDisCardIds.size() == 1
		if (removeActionList.get(0) == 1 && disCardSeat != player.getSeat()) {
			if (nowDisCardIds.size() > 1) {
				for (NxMj mj : nowDisCardIds) {
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
			NxMjPlayer disCSMajiangPlayer = seatMap.get(disCardSeat);
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
				NxMjPlayer seatPlayer = seatMap.get(seat);
				seatPlayer.writeSocket(copy.build());
			}
		}
		// && tempActionMap.size()==0

		if ((player.isAlreadyMoMajiang()||player.getGangCGangMjs().size()==2) && !player.getGang().isEmpty() && actionSeatMap.get(player.getSeat()) == null) {
			// 杠牌后自动出牌
			List<NxMj> disMjiang = new ArrayList<>();
			//disMjiang.add(player.getLastMoMajiang());
			if (player.getGangCGangMjs().size()>0 ) {
				disMjiang.add(NxMj.getMajang(player.getGangCGangMjs().remove(0)));
//				player.getGangCGangMjs().remove(0);
//				List<NxMj> list = new ArrayList<>();
				if (player.getGangCGangMjs().size()>0 ) {
					disMjiang.add(NxMj.getMajang(player.getGangCGangMjs().remove(0)));
				}
				//checkMoOutCard(list, player, action);
			}
			
			if (isHasGangAction()||disMjiang.size()==2) {
				checkMoOutCard(disMjiang, player, action);
			} else {
					chuPai(player, disMjiang, 0);
			}
		}

		if (isBegin && xiaoHu == -1 && player.getSeat() == lastWinSeat) {
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

	private void calcPoint(NxMjPlayer player, int action, int sameCount, List<NxMj> majiangs) {
		if(gangBuF!=1) {
			return;
		}
		int lostPoint = 0;
		int getPoint = 0;
		if (action == NxMjDisAction.action_peng) {
			List<Integer> actionList = actionSeatMap.get(player.getSeat());
			if (actionList.get(2) == 1 ||actionList.get(5) == 1 ) {
				// 可以碰也可以杠
				player.addPassGangVal(majiangs.get(0).getVal());
			}
			return;

		} else if (action == NxMjDisAction.action_angang||action == NxMjDisAction.action_buzhang_an) {
			// 暗杠相当于自摸每人出2分
			lostPoint = -2;
			 getPoint = 2 * (getMaxPlayerCount() - 1);

		} else if (action == NxMjDisAction.action_minggang||action == NxMjDisAction.action_buzhang) {
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
			else if (sameCount == 3) {
				NxMjPlayer disPlayer = seatMap.get(disCardSeat);
				
				  int point = (getMaxPlayerCount() - 1);
	                disPlayer.changeGangPoint(-point);
	                player.changeGangPoint(point);
				
				//disPlayer.getMyExtend().setMjFengshen(FirstmythConstants.firstmyth_index13, 1);
				// disPlayer.changeGangPoint(-3);
				//player.changeGangPoint(3);
				// }
			}
			//
		}
		
		if (lostPoint != 0) {
			for (NxMjPlayer seat : seatMap.values()) {
				if (seat.getUserId() == player.getUserId()) {
					player.changeGangPoint(getPoint);
				} else {
					seat.changeGangPoint(lostPoint);
				}
			}
		}
		 
	}

	private void calcXiaoHuPoint(NxMjPlayer player, int xiaoIndex) {
//		int count = player.getXiaoHuCount(xiaoIndex);
//		int lostPoint = -2 * count;
//		int getPoint = 6 * count;
//		if (lostPoint != 0) {
//			for (NxMjPlayer seat : seatMap.values()) {
//				if (seat.getUserId() == player.getUserId()) {
//					seat.changeGangPoint(getPoint);
//				} else {
//					seat.changeGangPoint(lostPoint);
//				}
//			}
//		}
	}

	private void recordDisMajiang(List<NxMj> majiangs, NxMjPlayer player) {
		setNowDisCardIds(majiangs);
		setDisCardSeat(player.getSeat());
	}

	public List<NxMj> getNowDisCardIds() {
		return nowDisCardIds;
	}

	public void setDisEventAction(int disAction) {
		this.disEventAction = disAction;
		changeExtend();
	}

	public void setNowDisCardIds(List<NxMj> nowDisCardIds) {
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
				NxMjPlayer player = seatMap.get(seat);
				if (player != null && player.isRobot()) {
					// 如果是机器人可以直接决定
					List<Integer> actionList = actionSeatMap.get(seat);
					if (actionList == null) {
						continue;
					}
					List<NxMj> list = new ArrayList<>();
					if (!nowDisCardIds.isEmpty()) {
						list = NxMjQipaiTool.getVal(player.getHandMajiang(), nowDisCardIds.get(0).getVal());
					}
					if (actionList.get(0) == 1) {
						// 胡
						playCommand(player, new ArrayList<NxMj>(), NxMjDisAction.action_hu);

					} else if (actionList.get(3) == 1) {
						playCommand(player, list, NxMjDisAction.action_angang);

					} else if (actionList.get(2) == 1) {
						playCommand(player, list, NxMjDisAction.action_minggang);

					} else if (actionList.get(1) == 1) {
						playCommand(player, list, NxMjDisAction.action_peng);

					} else if (actionList.get(4) == 1) {
						playCommand(player, player.getCanChiMajiangs(nowDisCardIds.get(0)), NxMjDisAction.action_chi);

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
			// for (NxMjPlayer player : seatMap.values()) {
			// if (player.isRobot() && player.canXiaoHu()) {
			// playCommand(player, new ArrayList<NxMj>(),
			// NxMjDisAction.action_xiaohu);
			// }
			// }

			int nextseat = getNextActionSeat();
			NxMjPlayer next = seatMap.get(nextseat);
			if (next != null && next.isRobot()) {
				List<Integer> actionList = actionSeatMap.get(next.getSeat());
				int xiaoHuAction = -1;
				if (actionList != null) {
					List<NxMj> list = null;
					if (actionList.get(0) == 1) {
						// 胡
						playCommand(next, new ArrayList<NxMj>(), NxMjDisAction.action_hu);

					} else if ((xiaoHuAction = NxMjAction.getFirstXiaoHu(actionList)) > 0) {

						playCommand(next, new ArrayList<NxMj>(), NxMjDisAction.action_pass);

					} else if (actionList.get(3) == 1) {
						// 机器人暗杠
						Map<Integer, Integer> handMap = NxMjHelper.toMajiangValMap(next.getHandMajiang());
						for (Entry<Integer, Integer> entry : handMap.entrySet()) {
							if (entry.getValue() == 4) {
								// 可以暗杠
								list = NxMjHelper.getMajiangList(next.getHandMajiang(), entry.getKey());
							}
						}
						playCommand(next, list, NxMjDisAction.action_angang);

					} else if (actionList.get(5) == 1) {
						// 机器人补张
						Map<Integer, Integer> handMap = NxMjHelper.toMajiangValMap(next.getHandMajiang());
						for (Entry<Integer, Integer> entry : handMap.entrySet()) {
							if (entry.getValue() == 4) {
								// 可以补张
								list = NxMjHelper.getMajiangList(next.getHandMajiang(), entry.getKey());
							}
						}
						if (list == null) {
							if (next.isAlreadyMoMajiang()) {
								list = NxMjQipaiTool.getVal(next.getHandMajiang(), next.getLastMoMajiang().getVal());

							} else {
								list = NxMjQipaiTool.getVal(next.getHandMajiang(), nowDisCardIds.get(0).getVal());
								list.add(nowDisCardIds.get(0));
							}
						}

						playCommand(next, list, NxMjDisAction.action_buzhang);

					} else if (actionList.get(2) == 1) {
						Map<Integer, Integer> pengMap = NxMjHelper.toMajiangValMap(next.getPeng());
						for (NxMj handMajiang : next.getHandMajiang()) {
							if (pengMap.containsKey(handMajiang.getVal())) {
								// 有碰过
								list = new ArrayList<>();
								list.add(handMajiang);
								playCommand(next, list, NxMjDisAction.action_minggang);
								break;
							}
						}

					} else if (actionList.get(1) == 1) {
						// playCommand(next, list, NxMjDisAction.action_peng);

					} else if (actionList.get(4) == 1) {
						NxMj majiang = null;
						List<NxMj> chiList = null;
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
										majiang = NxMj.getMajang(majiangId);
										chiList = next.getCanChiMajiangs(majiang);
										chiList.add(majiang);
										break;
									}

								}

							}

						}

						playCommand(next, chiList, NxMjDisAction.action_chi);

					} else {
						System.out.println("!!!!!!!!!!" + JacksonUtil.writeValueAsString(actionList));

					}

				} else {
					int maJiangId = NxMjRobotAI.getInstance().outPaiHandle(0, next.getHandPais(),
							new ArrayList<Integer>());
					List<NxMj> majiangList = NxMjHelper.toMajiang(Arrays.asList(maJiangId));
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
		if (maxPlayerCount == 2 && queYiMen == 1) {
			copy = new ArrayList<>();
			for (Integer id : NxMjConstants.zhuanzhuan_mjList) {
				NxMj mj = NxMj.getMajang(id);
				if (mj.getColourVal() == 1) {
					continue;
				}
				copy.add(id);
			}
		} else if(maxPlayerCount == 2 && isChoupai40()){
			copy = new ArrayList<>(NxMjConstants.zhuanzhuan_mjList);
			for (int i = 0; i < 40; i++) {
				int max = copy.size();
				int index =(int) (Math.random()*(max-1));
				copy.remove(index);
			}
		}else {
			copy = new ArrayList<>(NxMjConstants.zhuanzhuan_mjList);
		}

		// List<Integer> copy = new
		// ArrayList<>(NxMjConstants.zhuanzhuan_mjList);
		addPlayLog(copy.size() + "");
		List<List<NxMj>> list;
		if (zp == null) {
			list = NxMjTool.fapai(copy, getMaxPlayerCount());
		} else {
			list = NxMjTool.fapai(copy, getMaxPlayerCount(), zp);
		}
		int i = 1;
		List<Integer> removeIndex = new ArrayList<>();
		for (NxMjPlayer player : playerMap.values()) {
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
		List<NxMj> leftMjs = new ArrayList<>();
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
	public void setLeftMajiangs(List<NxMj> leftMajiangs) {
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
	public NxMj getLeftMajiang() {
		if (this.leftMajiangs.size() > 0) {
			NxMj majiang = this.leftMajiangs.remove(0);
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
		res.addExt(queYiSe); // 5
		res.addExt(banbanHu); // 6
		res.addExt(yiZhiHua); // 7
		res.addExt(liuliuShun); // 8
		res.addExt(daSiXi); // 9
		res.addExt(jinTongYuNu); // 10
		res.addExt(jieJieGao); // 11
		res.addExt(sanTong); // 12
		res.addExt(zhongTuLiuLiuShun); // 13
		res.addExt(zhongTuSiXi); // 14
		res.addExt(kePiao); // 15
		res.addExt(isCalcBanker); // 16
		res.addExt(isBegin() ? 1 : 0); // 17
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
		for (NxMjPlayer player : playerMap.values()) {
			PlayerInTableRes.Builder playerRes = player.buildPlayInTableInfo(isrecover);
			if (player.getUserId() == userId) {
				playerRes.addAllHandCardIds(player.getHandPais());
			}

			if (showMjSeat.contains(player.getSeat()) && player.getHuXiaohu().size() > 0) {
				List<Integer> ids = NxMjHelper.toMajiangIds(player.showXiaoHuMajiangs(player.getHuXiaohu().get(player.getHuXiaohu().size() - 1), true));
				if (ids != null) {
					if (player.getUserId() == userId) {
						playerRes.addAllIntExts(ids);
					} else {
						playerRes.addAllHandCardIds(ids);
					}
				}
			}
			if (player.getSeat() == disCardSeat && nowDisCardIds != null) {
				playerRes.addAllOutCardIds(NxMjHelper.toMajiangIds(nowDisCardIds));
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
		setIsCalcBanker(1);
		setCalcBird(2);
	}

	private Map<Integer, NxMjTempAction> loadTempActionMap(String json) {
		Map<Integer, NxMjTempAction> map = new ConcurrentHashMap<>();
		if (json == null || json.isEmpty())
			return map;
		JSONArray jsonArray = JSONArray.parseArray(json);
		for (Object val : jsonArray) {
			String str = val.toString();
			NxMjTempAction tempAction = new NxMjTempAction();
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
		addPlayLog(disCardRound + "_" + seat + "_" + NxMjDisAction.action_hasAction + "_"
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
			addPlayLog(disCardRound + "_" + seat + "_" + NxMjDisAction.action_hasAction + "_" + StringUtil.implode(a));
		} else {
			actionSeatMap.put(seat, actionlist);
			addPlayLog(disCardRound + "_" + seat + "_" + NxMjDisAction.action_hasAction + "_"
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
			nowDisCardIds = NxMjHelper.toMajiang(StringUtil.explodeToIntList(info.getNowDisCardIds()));
		}

		if (!StringUtils.isBlank(info.getLeftPais())) {
			leftMajiangs = NxMjHelper.toMajiang(StringUtil.explodeToIntList(info.getLeftPais()));
		}

	}

	// @Override
	// public void initExtend(String info) {
	// if (StringUtils.isBlank(info)) {
	// return;
	// }
	// JsonWrapper wrapper = new JsonWrapper(info);
	// for (NxMjPlayer player : seatMap.values()) {
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
	private boolean canChi(NxMjPlayer player, List<NxMj> handMajiang, List<NxMj> majiangs, NxMj disMajiang) {
		if (!actionSeatMap.containsKey(player.getSeat())) {
			return false;
		}

		if (maxPlayerCount == 2 && buChi == 1) {
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

		List<NxMj> chi = NxMjTool.checkChi(majiangs, disMajiang);
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
	private boolean canPeng(NxMjPlayer player, List<NxMj> majiangs, int sameCount, NxMj disMajiang) {
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
	private boolean canAnGang(NxMjPlayer player, List<NxMj> majiangs, int sameCount, int action) {
		if (sameCount != 4) {
			return false;
		}
		if (player.getSeat() != getNextDisCardSeat() && action != NxMjDisAction.action_buzhang) {
			return false;
		}
		if (player.getSeat() != getNextDisCardSeat() && action != NxMjDisAction.action_buzhang_an) {
			return false;
		}
		return true;
	}

	/**
	 * 检查优先度 如果同时出现一个事件，按出牌座位顺序优先
	 */
	private boolean checkAction(NxMjPlayer player, List<NxMj> cardList, List<Integer> hucards, int action) {
		boolean canAction = checkCanAction(player, action);// 是否优先级最高 能执行操作
		if (canAction == false) {// 不能操作时 存入临时操作
			int seat = player.getSeat();
			tempActionMap.put(seat, new NxMjTempAction(seat, action, cardList, hucards));
			// 玩家都已选择自己的临时操作后 选取优先级最高
			if (tempActionMap.size() == actionSeatMap.size()) {
				int maxAction = Integer.MAX_VALUE;
				int maxSeat = 0;
				Map<Integer, Integer> prioritySeats = new HashMap<>();
				int maxActionSize = 0;
				for (NxMjTempAction temp : tempActionMap.values()) {
					int prioAction = NxMjDisAction.getPriorityAction(temp.getAction());
					int prioAction2 = NxMjDisAction.getPriorityAction(maxAction);
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
				NxMjPlayer tempPlayer = seatMap.get(maxSeat);
				List<NxMj> tempCardList = tempActionMap.get(maxSeat).getCardList();
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
	private void refreshTempAction(NxMjPlayer player) {
		tempActionMap.remove(player.getSeat());
		Map<Integer, Integer> prioritySeats = new HashMap<>();
		for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
			int seat = entry.getKey();
			List<Integer> actionList = entry.getValue();
			List<Integer> list = NxMjDisAction.parseToDisActionList(actionList);
			int priorityAction = NxMjDisAction.getMaxPriorityAction(list);
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
		Iterator<NxMjTempAction> iterator = tempActionMap.values().iterator();
		while (iterator.hasNext()) {
			NxMjTempAction tempAction = iterator.next();
			if (tempAction.getSeat() == maxPrioritySeat) {
				int action = tempAction.getAction();
				List<NxMj> tempCardList = tempAction.getCardList();
				NxMjPlayer tempPlayer = seatMap.get(tempAction.getSeat());
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
	public boolean checkCanAction(NxMjPlayer player, int action) {
		// 优先度为胡杠补碰吃
		List<Integer> stopActionList = NxMjDisAction.findPriorityAction(action);
		for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
			if (player.getSeat() != entry.getKey()) {
				// 别人
				boolean can = NxMjDisAction.canDisMajiang(stopActionList, entry.getValue());
				if (!can) {
					return false;
				}
				List<Integer> disActionList = NxMjDisAction.parseToDisActionList(entry.getValue());
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
	private boolean canMingGang(NxMjPlayer player, List<NxMj> handMajiang, List<NxMj> majiangs, int sameCount,
			NxMj disMajiang) {
		List<Integer> pengList = NxMjHelper.toMajiangVals(player.getPeng());

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

	public void setLastMajiang(NxMj lastMajiang) {
		this.lastMajiang = lastMajiang;
		changeExtend();
	}

	public void setMoLastMajiangSeat(int moLastMajiangSeat) {
		this.moLastMajiangSeat = moLastMajiangSeat;
		changeExtend();
	}

	public void setGangMajiang(NxMj gangMajiang) {
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
	public void setMoGang(NxMj moGang, List<Integer> moGangHuList) {
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

	public void setGangDisMajiangs(List<NxMj> gangDisMajiangs) {
		this.gangDisMajiangs = gangDisMajiangs;
		changeExtend();
	}

	public List<NxMj> getGangDisMajiangs() {
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
			for (NxMjPlayer player : seatMap.values()) {
				if (player.getTotalPoint() > 0 && player.getTotalPoint() < jiaBeiFen) {
					jiaBeiPoint += player.getTotalPoint() * (jiaBeiShu - 1);
					player.setTotalPoint(player.getTotalPoint() * jiaBeiShu);
				} else if (player.getTotalPoint() < 0) {
					loserCount++;
				}
			}
			if (jiaBeiPoint > 0) {
				for (NxMjPlayer player : seatMap.values()) {
					if (player.getTotalPoint() < 0) {
						player.setTotalPoint(player.getTotalPoint() - (jiaBeiPoint / loserCount));
					}
				}
			}
		}

		//大结算低于below分+belowAdd分
		if(over&&belowAdd>0&&playerMap.size()==2){
			for (NxMjPlayer player : seatMap.values()) {
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
		for (NxMjPlayer player : seatMap.values()) {
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
						NxMj huMajiang = nowDisCardIds.get(0);
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
			for (NxMjPlayer player : seatMap.values()) {
				if (player.getWinLoseCredit() > dyjCredit) {
					dyjCredit = player.getWinLoseCredit();
				}
			}
			for (ClosingMjPlayerInfoRes.Builder builder : builderList) {
				NxMjPlayer player = seatMap.get(builder.getSeat());
				calcCommissionCredit(player, dyjCredit);
				builder.setWinLoseCredit(player.getWinLoseCredit());
				builder.setCommissionCredit(player.getCommissionCredit());
				list.add(builder.build());
			}
		} else if (isGroupTableGoldRoom()) {
            // -----------亲友圈金币场---------------------------------
            for (NxMjPlayer player : seatMap.values()) {
                player.setWinGold(player.getTotalPoint() * gtgDifen);
            }
            calcGroupTableGoldRoomWinLimit();
            for (ClosingMjPlayerInfoRes.Builder builder : builderList) {
                NxMjPlayer player = seatMap.get(builder.getSeat());
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
		res.addAllLeftCards(NxMjHelper.toMajiangIds(leftMajiangs));
		for (NxMjPlayer player : seatMap.values()) {
			player.writeSocket(res.build());
		}
		return res;

	}

	/**
	 * 杠上花和杠上炮
	 *
	 * @return
	 */
	public NxMj getGangHuMajiang(int seat) {
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
		return NxMj.getMajang(majiangId);

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
		ext.add(queYiSe + "");
		ext.add(banbanHu + "");
		ext.add(yiZhiHua + "");
		ext.add(liuliuShun + "");
		ext.add(daSiXi + "");
		ext.add(jinTongYuNu + "");
		ext.add(jieJieGao + "");
		ext.add(sanTong + "");
		ext.add(zhongTuLiuLiuShun + "");
		ext.add(zhongTuSiXi + "");
		ext.add(kePiao + "");
		ext.add(isCalcBanker + "");
		ext.add(birdNum + "");
		ext.add(isAutoPlay + "");
		ext.add(over + ""); // 27
		ext.add(yiDianHong + ""); // 28
		return ext;
	}

	@Override
	public void sendAccountsMsg() {
		ClosingMjInfoRes.Builder builder = sendAccountsMsg(true, false, null, null, null, null, true, 0, 0);
		saveLog(true, 0l, builder.build());
	}

	public Class<? extends Player> getPlayerClass() {
		return NxMjPlayer.class;
	}

	@Override
	public int getWanFa() {
		return GameUtil.game_type_nxmj;
	}

	@Override
	public void checkReconnect(Player player) {
		if (super.isAllReady() && getKePiao() == 1 && getTableStatus() == NxMjConstants.TABLE_STATUS_PIAO) {
			NxMjPlayer player1 = (NxMjPlayer) player;
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
			if (player instanceof NxMjPlayer) {
				NxMjPlayer csMjPlayer = (NxMjPlayer) player;
				if (csMjPlayer != null) {
					if (csMjPlayer.isAlreadyMoMajiang()) {
						if (!csMjPlayer.getGang().isEmpty()) {
							List<NxMj> disMajiangs = new ArrayList<>();
							disMajiangs.add(csMjPlayer.getLastMoMajiang());
							chuPai(csMjPlayer, disMajiangs, 0);
						}
					}
				}
			}
		}
		if (isBegin() && player.getSeat() == lastWinSeat && actionSeatMap.isEmpty()) {
			// 如果是起手判断是否还有人可胡小胡 没有的话通知庄家出牌
			NxMjPlayer bankPlayer = seatMap.get(lastWinSeat);
			ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.req_com_kt_ask_dismajiang);// 推送庄家出牌
			bankPlayer.writeSocket(com.build());
		}

		if (state == table_state.play) {
			if (player.getHandPais() != null && player.getHandPais().size() > 0) {
				sendTingInfo((NxMjPlayer) player);
			}
		}

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
			for (NxMjPlayer player : seatMap.values()) {
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
		for (NxMjPlayer player : seatMap.values()) {

			if ((!player.getGang().isEmpty() || player.isBaoting())
					&& player.isAlreadyMoMajiang() && getMoMajiangSeat() == player.getSeat()) {
				// 能胡牌就不自动打出去
				List<Integer> actionList = actionSeatMap.get(player.getSeat());
				if (actionList != null && (actionList.get(NxMjAction.HU) == 1 || actionList.get(NxMjAction.ZIMO) == 1
						|| actionList.get(NxMjAction.MINGGANG) == 1 || actionList.get(NxMjAction.BUZHANG) == 1
						|| hasXiaoHu(actionList))) {
					continue;
				}

				if (nowDisCardSeat != player.getSeat()) {
					continue;
				}
				List<NxMj> disMjiang = new ArrayList<>();
				disMjiang.add(player.getHandMajiang().get(player.getHandMajiang().size() - 1));
				chuPai(player, disMjiang, NxMjDisAction.action_chupai);
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
            	NxMjPlayer player = seatMap.get(seat);
                player.setAutoPlay(false, false);
                player.setCheckAutoPlay(false);
            }
            return;
        }
		
		if (getTableStatus() == NxMjConstants.TABLE_STATUS_PIAO) {
			for (int seat : seatMap.keySet()) {
				NxMjPlayer player = seatMap.get(seat);
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
				NxMjPlayer player = seatMap.get(seat);
				if (player.getPiaoPoint() < 0) {
					piao = false;
				}

			}
			if (piao) {
				setTableStatus(NxMjConstants.AUTO_PLAY_TIME);
			}

		} else if (state == table_state.play) {
			autoPlay();
		} else {
			if (getPlayedBureau() == 0) {
				return;
			}
			readyTime++;
			// for (NxMjPlayer player : seatMap.values()) {
			// if (player.checkAutoPlay(1, false)) {
			// autoReady(player);
			// }
			// }
			// 开了托管的房间，xx秒后自动开始下一局
			for (NxMjPlayer player : seatMap.values()) {
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
					NxMjPlayer player = seatMap.get(seat);
					if (player == null) {
						continue;
					}
					if (!player.checkAutoPlay(2, false)) {
						continue;
					}
					playCommand(player, new ArrayList<>(), NxMjDisAction.action_hu);
				}
				return;
			} else {
				int action, seat;
				for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
					List<Integer> actList = NxMjDisAction.parseToDisActionList(entry.getValue());
					if (actList == null) {
						continue;
					}
					seat = entry.getKey();
					action = NxMjDisAction.getAutoMaxPriorityAction(actList);
					NxMjPlayer player = seatMap.get(seat);
					if (!player.checkAutoPlay(0, false)) {
						continue;
					}
					boolean chuPai = false;
					if (player.isAlreadyMoMajiang()) {
						chuPai = true;
					}
					if (action == NxMjDisAction.action_peng) {
						if (player.isAutoPlaySelf()) {
							// 自己开启托管直接过
							playCommand(player, new ArrayList<>(), NxMjDisAction.action_pass);
							if (chuPai) {
								autoChuPai(player);
							}
						} else {
							if (nowDisCardIds != null && !nowDisCardIds.isEmpty()) {
                                for(NxMj mj : nowDisCardIds) {
                                    List<NxMj> mjList = new ArrayList<>();
                                    for (NxMj handMj : player.getHandMajiang()) {
                                        if (handMj.getVal() == mj.getVal()) {
                                            mjList.add(handMj);
                                            if (mjList.size() == 2) {
                                                break;
                                            }
                                        }
                                    }
                                    if(mjList.size() >= 2){
                                        playCommand(player, mjList, NxMjDisAction.action_peng);
                                        break;
                                    }
                                }
							}
						}
					}
					// else if(action == NxMjDisAction.action_chi){
					// playCommand(player, new ArrayList<>(),
					// NxMjDisAction.action_chi);
					// if (chuPai) {
					// autoChuPai(player);
					// }
					//
					// }
					else {
						playCommand(player, new ArrayList<>(), NxMjDisAction.action_pass);
						if (chuPai) {
							autoChuPai(player);
						}
					}
				}
			}
		} else {
			NxMjPlayer player = seatMap.get(nowDisCardSeat);
			if (player == null || !player.checkAutoPlay(0, false)) {
				return;
			}
			autoChuPai(player);
		}
	}

	public void autoChuPai(NxMjPlayer player) {

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
		// NxMj mj = NxMj.getMajang(mjId);

		while (mjId == -1 && index >= 0) {
			mjId = handMjIds.get(index);
			// mj = NxMj.getMajang(mjId);

		}
		if (mjId != -1) {
			List<NxMj> mjList = NxMjHelper.toMajiang(Arrays.asList(mjId));
			playCommand(player, mjList, NxMjDisAction.action_chupai);
		}
	}

	public void autoPiao(NxMjPlayer player) {
		int piaoPoint = 0;
		if (getTableStatus() != NxMjConstants.TABLE_STATUS_PIAO) {
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
		for (NxMjPlayer player : seatMap.values()) {
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
			moGang = NxMj.getMajang(moGangMajiangId);
		}
		String moGangHu = extend.getString(9);
		if (!StringUtils.isBlank(moGangHu)) {
			moGangHuList = StringUtil.explodeToIntList(moGangHu);
		}
		String gangDisMajiangstr = extend.getString(10);
		if (!StringUtils.isBlank(gangDisMajiangstr)) {
			gangDisMajiangs = NxMjHelper.explodeMajiang(gangDisMajiangstr, ",");
		}
		int gangMajiang = extend.getInt(11, 0);
		if (gangMajiang != 0) {
			this.gangMajiang = NxMj.getMajang(gangMajiang);
		}

		askLastMajaingSeat = extend.getInt(12, 0);
		moLastMajiangSeat = extend.getInt(13, 0);
		int lastMajiangId = extend.getInt(14, 0);
		if (lastMajiangId != 0) {
			this.lastMajiang = NxMj.getMajang(lastMajiangId);
		}
		fristLastMajiangSeat = extend.getInt(15, 0);
		disEventAction = extend.getInt(16, 0);
		isCalcBanker = extend.getInt(17, 1);
		calcBird = extend.getInt(18, 1);
		kePiao = extend.getInt(19, 0);
		tempActionMap = loadTempActionMap(extend.getString("tempActions"));

		gpsWarn = extend.getInt(20, 0);
		queYiSe = extend.getInt(21, 0);
		banbanHu = extend.getInt(22, 0);
		yiZhiHua = extend.getInt(23, 0);
		liuliuShun = extend.getInt(24, 0);
		daSiXi = extend.getInt(25, 0);
		jinTongYuNu = extend.getInt(26, 0);
		jieJieGao = extend.getInt(27, 0);
		sanTong = extend.getInt(28, 0);
		zhongTuLiuLiuShun = extend.getInt(29, 0);
		zhongTuSiXi = extend.getInt(30, 0);
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

		kongBird = extend.getInt(41, 0);
		buChi = extend.getInt(42, 0);
		OnlyDaHu = extend.getInt(43, 0);
		xiaohuZiMo = extend.getInt(44, 0);
		queYiMen = extend.getInt(45, 0);
		jiajianghu = extend.getInt(46, 0);

		isAutoPlay = extend.getInt(47, 0);
		autoPlayGlob = extend.getInt(48, 0);
		menqing = extend.getInt(49, 0);
		tableStatus = extend.getInt(50, 0);
		topFen = extend.getInt(51, 0);
		gangMoSi = extend.getInt(52, 0);
		qiShouNiaoFen= extend.getInt(53, 1);
		below= extend.getInt(54, 0);
		belowAdd= extend.getInt(55, 0);
		gangBuF= extend.getInt(56, 0);
		quanqiurJiang= extend.getInt(57, 0);
		difen= extend.getInt(58, 0);
		
		menQingZM = extend.getInt(59, 0);
		pinghuNojiepao = extend.getInt(60, 0);
		choupai40 = extend.getInt(61, 0);
		yiDianHong = extend.getInt(62, 0);
	}

	@Override
	public JsonWrapper buildExtend0(JsonWrapper wrapper) {
		// 1-4 玩家座位信息
		for (NxMjPlayer player : seatMap.values()) {
			wrapper.putString(player.getSeat(), player.toExtendStr());
		}
		wrapper.putString(5, DataMapUtil.explode(huConfirmMap));
		wrapper.putInt(6, birdNum);
		wrapper.putInt(7, moMajiangSeat);
		wrapper.putInt(8, moGang != null ? moGang.getId() : 0);
		wrapper.putString(9, StringUtil.implode(moGangHuList, ","));
		wrapper.putString(10, NxMjHelper.implodeMajiang(gangDisMajiangs, ","));
		wrapper.putInt(11, gangMajiang != null ? gangMajiang.getId() : 0);
		wrapper.putInt(12, askLastMajaingSeat);
		wrapper.putInt(13, moLastMajiangSeat);
		wrapper.putInt(14, lastMajiang != null ? lastMajiang.getId() : 0);
		wrapper.putInt(15, fristLastMajiangSeat);
		wrapper.putInt(16, disEventAction);
		wrapper.putInt(17, isCalcBanker);
		wrapper.putInt(18, calcBird);
		wrapper.putInt(19, kePiao);
		JSONArray tempJsonArray = new JSONArray();
		for (int seat : tempActionMap.keySet()) {
			tempJsonArray.add(tempActionMap.get(seat).buildData());
		}
		wrapper.putString("tempActions", tempJsonArray.toString());

		wrapper.putInt(20, gpsWarn);
		wrapper.putInt(21, queYiSe);
		wrapper.putInt(22, banbanHu);
		wrapper.putInt(23, yiZhiHua);
		wrapper.putInt(24, liuliuShun);
		wrapper.putInt(25, daSiXi);
		wrapper.putInt(26, jinTongYuNu);
		wrapper.putInt(27, jieJieGao);
		wrapper.putInt(28, sanTong);
		wrapper.putInt(29, zhongTuLiuLiuShun);
		wrapper.putInt(30, zhongTuSiXi);
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

		wrapper.putInt(41, kongBird);
		wrapper.putInt(42, buChi);
		wrapper.putInt(43, OnlyDaHu);
		wrapper.putInt(44, xiaohuZiMo);
		wrapper.putInt(45, queYiMen);
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
		wrapper.putInt(59, menQingZM);
		wrapper.putInt(60, pinghuNojiepao);
		wrapper.putInt(61, choupai40);
		wrapper.putInt(62, yiDianHong);
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
		calcBird = StringUtil.getIntValue(params, 3, 0);
		birdNum = StringUtil.getIntValue(params, 4, 0);
		gpsWarn = StringUtil.getIntValue(params, 5, 0);
		queYiSe = StringUtil.getIntValue(params, 6, 0);

		yiZhiHua = StringUtil.getIntValue(params, 8, 0);
		liuliuShun = StringUtil.getIntValue(params, 9, 0);
		daSiXi = StringUtil.getIntValue(params, 10, 0);
		jinTongYuNu = StringUtil.getIntValue(params, 11, 0);
		jieJieGao = StringUtil.getIntValue(params, 12, 0);
		sanTong = StringUtil.getIntValue(params, 13, 0);
		zhongTuLiuLiuShun = StringUtil.getIntValue(params, 14, 0);
		zhongTuSiXi = StringUtil.getIntValue(params, 15, 0);
		kePiao = StringUtil.getIntValue(params, 16, 0);
		isCalcBanker = StringUtil.getIntValue(params, 18, 0);
		banbanHu = StringUtil.getIntValue(params, 17, 1);

		// 加倍：0否，1是
		this.jiaBei = StringUtil.getIntValue(params, 19, 0);
		// 加倍分
		this.jiaBeiFen = StringUtil.getIntValue(params, 20, 0);
		// 加倍数
		this.jiaBeiShu = StringUtil.getIntValue(params, 21, 0);

		if (maxPlayerCount == 3) {
			// 三人：空鸟 1：四八空鸟 2:鸟不落空
			kongBird = StringUtil.getIntValue(params, 22, 0);
		} else if (maxPlayerCount == 2) {
			// （二人选）1：不能吃
			buChi = StringUtil.getIntValue(params, 23, 0);
			// （二人选）只能大胡
			OnlyDaHu = StringUtil.getIntValue(params, 24, 0);
			// （二人选）小胡自摸
			xiaohuZiMo = StringUtil.getIntValue(params, 25, 0);
			// （二人选）缺一门
			queYiMen = StringUtil.getIntValue(params, 26, 0);
		}

		jiajianghu = StringUtil.getIntValue(params, 27, 0);

		isAutoPlay = StringUtil.getIntValue(params, 28, 0);
		this.autoPlayGlob = StringUtil.getIntValue(params, 29, 0);
		menqing = StringUtil.getIntValue(params, 30, 0);

		topFen = StringUtil.getIntValue(params, 31, 0);
		
		
		gangMoSi= StringUtil.getIntValue(params, 32, 0);
		
		
//		qiShouNiaoFen =  StringUtil.getIntValue(params, 33, 1);
		

		if(maxPlayerCount==2){
			int belowAdd = StringUtil.getIntValue(params, 34, 0); 
			if(belowAdd<=100&&belowAdd>=0)
				this.belowAdd=belowAdd;
			int below = StringUtil.getIntValue(params, 35, 0);
			if(below<=100&&below>=0){
				this.below=below;
				if(belowAdd>0&&below==0)
					this.below=10;
			}
		}
		gangBuF =  StringUtil.getIntValue(params, 36, 0);
		quanqiurJiang=  StringUtil.getIntValue(params, 37, 0);
//		difen=  StringUtil.getIntValue(params, 38, 0);
		
		menQingZM =  StringUtil.getIntValue(params, 39, 0);
		pinghuNojiepao =  StringUtil.getIntValue(params, 40, 0);
		choupai40 =  StringUtil.getIntValue(params, 41, 0);
		yiDianHong =  StringUtil.getIntValue(params, 42, 0);
		
		if(difen==0){
			difen=4;
		}
		if (maxPlayerCount != 2) {
			jiaBei = 0;
		}
		playedBureau = 0;

		// getRoomModeMap().put("1", "1"); //可观战（默认）
	}


	public void sendTingInfo(NxMjPlayer player) {
		if (player.isAlreadyMoMajiang()) {
			// if (actionSeatMap.containsKey(player.getSeat())) {
			// return;
			// }
			DaPaiTingPaiRes.Builder tingInfo = DaPaiTingPaiRes.newBuilder();
			List<NxMj> cards = new ArrayList<>(player.getHandMajiang());

			for (NxMj card : player.getHandMajiang()) {
				cards.remove(card);
				List<NxMj> huCards = NxMjTool.getTingMjs(cards, player.getGang(), player.getPeng(), player.getChi(),
						player.getBuzhang(), true, OnlyDaHu == 1,getQuanqiurJiang());
				cards.add(card);
				if (huCards == null || huCards.size() == 0) {
					continue;
				}
				DaPaiTingPaiInfo.Builder ting = DaPaiTingPaiInfo.newBuilder();
				ting.setMajiangId(card.getId());
				for (NxMj mj : huCards) {
					ting.addTingMajiangIds(mj.getId());
				}
				tingInfo.addInfo(ting.build());
			}
			if (tingInfo.getInfoCount() > 0) {
				player.writeSocket(tingInfo.build());
			}
		} else {
			List<NxMj> cards = new ArrayList<>(player.getHandMajiang());
			List<NxMj> huCards = NxMjTool.getTingMjs(cards, player.getGang(), player.getPeng(), player.getChi(),
					player.getBuzhang(), true, OnlyDaHu == 1,getQuanqiurJiang());

			if (huCards == null || huCards.size() == 0) {
				return;
			}
			TingPaiRes.Builder ting = TingPaiRes.newBuilder();
			for (NxMj mj : huCards) {
				ting.addMajiangIds(mj.getId());
			}
			player.writeSocket(ting.build());

		}
	}

	public static final List<Integer> wanfaList = Arrays.asList(GameUtil.game_type_nxmj);

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
		case NxMjAction.QUEYISE:
			return queYiSe == 1;
		case NxMjAction.BANBANHU:
			return banbanHu == 1;
		case NxMjAction.YIZHIHUA:
			return yiZhiHua == 1;
		case NxMjAction.YIDIANHONG:
			return yiDianHong == 1;
		case NxMjAction.LIULIUSHUN:
			return liuliuShun == 1;
		case NxMjAction.DASIXI:
			return daSiXi == 1;
		case NxMjAction.JINGTONGYUNU:
			return jinTongYuNu == 1;
		case NxMjAction.JIEJIEGAO:
			return jieJieGao == 1;
		case NxMjAction.SANTONG:
			return sanTong == 1;
		case NxMjAction.ZHONGTULIULIUSHUN:
			return zhongTuLiuLiuShun == 1;
		case NxMjAction.ZHONGTUSIXI:
			return zhongTuSiXi == 1;
		default:
			return false;
		}
	}

	public void logFaPaiTable() {
		StringBuilder sb = new StringBuilder();
		sb.append("NxMj");
		sb.append("|").append(getId());
		sb.append("|").append(getPlayBureau());
		sb.append("|").append("faPai");
		sb.append("|").append(playType);
		sb.append("|").append(maxPlayerCount);
		sb.append("|").append(getPayType());
		sb.append("|").append(calcBird);
		sb.append("|").append(birdNum);
		sb.append("|").append(kePiao);
		sb.append("|").append(queYiSe);
		sb.append("|").append(banbanHu);
		sb.append("|").append(yiZhiHua);
		sb.append("|").append(liuliuShun);
		sb.append("|").append(daSiXi);
		sb.append("|").append(jinTongYuNu);
		sb.append("|").append(jieJieGao);
		sb.append("|").append(sanTong);
		sb.append("|").append(zhongTuSiXi);
		sb.append("|").append(zhongTuLiuLiuShun);
		sb.append("|").append(lastWinSeat);
		sb.append("|").append(yiDianHong);
		LogUtil.msg(sb.toString());
	}

	public void logFaPaiPlayer(NxMjPlayer player, List<Integer> actList) {
		StringBuilder sb = new StringBuilder();
		sb.append("NxMj");
		sb.append("|").append(getId());
		sb.append("|").append(getPlayBureau());
		sb.append("|").append(player.getUserId());
		sb.append("|").append(player.getSeat());
		sb.append("|").append("faPai");
		sb.append("|").append(player.getHandMajiang());
		sb.append("|").append(actListToString(actList));
		LogUtil.msg(sb.toString());
	}

	public void logMoMj(NxMjPlayer player, NxMj mj, List<Integer> actList) {
		StringBuilder sb = new StringBuilder();
		sb.append("NxMj");
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

    public void logGangMoMj(NxMjPlayer player, List<NxMj> mjs) {
        StringBuilder sb = new StringBuilder();
        sb.append("NxMj");
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

	public void logChuPaiActList(NxMjPlayer player, NxMj mj, List<Integer> actList) {
		StringBuilder sb = new StringBuilder();
		sb.append("NxMj");
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

	public void logAction(NxMjPlayer player, int action, int xiaoHuType, List<NxMj> mjs, List<Integer> actList) {
		StringBuilder sb = new StringBuilder();
		sb.append("NxMj");
		sb.append("|").append(getId());
		sb.append("|").append(getPlayBureau());
		sb.append("|").append(player.getUserId());
		sb.append("|").append(player.getSeat());
		String actStr = "unKnown-" + action;
		if (action == NxMjDisAction.action_peng) {
			actStr = "peng";
		} else if (action == NxMjDisAction.action_minggang) {
			actStr = "mingGang";
		} else if (action == NxMjDisAction.action_chupai) {
			actStr = "chuPai";
		} else if (action == NxMjDisAction.action_pass) {
			actStr = "guo";
		} else if (action == NxMjDisAction.action_angang) {
			actStr = "anGang";
		} else if (action == NxMjDisAction.action_chi) {
			actStr = "chi";
		} else if (action == NxMjDisAction.action_buzhang) {
			actStr = "buZhang";
		} else if (action == NxMjDisAction.action_xiaohu) {
			actStr = "xiaoHu";
		} else if (action == NxMjDisAction.action_buzhang_an) {
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
				if (i == NxMjAction.HU) {
					sb.append("hu");
				} else if (i == NxMjAction.PENG) {
					sb.append("peng");
				} else if (i == NxMjAction.MINGGANG) {
					sb.append("mingGang");
				} else if (i == NxMjAction.ANGANG) {
					sb.append("anGang");
				} else if (i == NxMjAction.CHI) {
					sb.append("chi");
				} else if (i == NxMjAction.BUZHANG) {
					sb.append("buZhang");
				} else if (i == NxMjAction.QUEYISE) {
					sb.append("queYiSe");
				} else if (i == NxMjAction.BANBANHU) {
					sb.append("banBanHu");
				} else if (i == NxMjAction.YIZHIHUA) {
					sb.append("yiZhiHua");
				} else if (i == NxMjAction.LIULIUSHUN) {
					sb.append("liuLiuShun");
				} else if (i == NxMjAction.DASIXI) {
					sb.append("daSiXi");
				} else if (i == NxMjAction.JINGTONGYUNU) {
					sb.append("jinTongYuNu");
				} else if (i == NxMjAction.JIEJIEGAO) {
					sb.append("jieJieGao");
				} else if (i == NxMjAction.SANTONG) {
					sb.append("sanTong");
				} else if (i == NxMjAction.ZHONGTUSIXI) {
					sb.append("zhongTuSiXi");
				} else if (i == NxMjAction.ZHONGTULIULIUSHUN) {
					sb.append("zhongTuLiuLiuShun");
				} else if (i == NxMjAction.BUZHANG_AN) {
					sb.append("buZhangAn");
				} else if (i == NxMjAction.BAOTING) {
					sb.append("baoting");
				}else if (i == NxMjAction.YIDIANHONG) {
					sb.append("yidianhong");
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
	public void processHideMj(NxMjPlayer player) {
		if (showMjSeat.contains(player.getSeat()) && disCardRound != 0) {
			PlayMajiangRes.Builder hideMj = PlayMajiangRes.newBuilder();
			buildPlayRes(hideMj, player, NxMjDisAction.action_hideMj, null);
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
		// if(xiaoHuType == NxMjAction.QUEYISE || xiaoHuType ==
		// NxMjAction.BANBANHU || xiaoHuType == NxMjAction.YIZHIHUA){
		if (!showMjSeat.contains(seat)) {
			showMjSeat.add(seat);
			changeExtend();
		}
		// }

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
					for (NxMjPlayer robotPlayer : seatMap.values()) {
						if (robotPlayer.isRobot()) {
							robotPlayer.setPiaoPoint(1);
						}
					}
				}
				for (NxMjPlayer player : seatMap.values()) {
					if (player.getPiaoPoint() < 0) {
						ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_table_status_piao,
								getTableStatus());
						player.writeSocket(com.build());
						if (getTableStatus() != NxMjConstants.TABLE_STATUS_PIAO) {
							player.setLastCheckTime(System.currentTimeMillis());
						}
						bReturn = false;
						// if(getTableStatus()!=NxMjConstants.TABLE_STATUS_PIAO)
						// {
						//
						// }
					}
				}
				setTableStatus(NxMjConstants.TABLE_STATUS_PIAO);

				return bReturn;
			} else {
				int point = 0;
				if (getKePiao() == 2 || getKePiao() == 3 || getKePiao() == 4) {
					point = getKePiao() - 1;
				}

				for (NxMjPlayer player : seatMap.values()) {
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
		List<NxMj> moList = getGangDisMajiangs();
		if (moList != null && moList.size() > 0 && actionSeatMap.isEmpty()) {
			NxMjPlayer player = seatMap.get(getMoMajiangSeat());
			for (NxMjPlayer seatPlayer : seatMap.values()) {
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
	public void sendMoLast(NxMjPlayer player, int state) {
		ComRes.Builder res = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_asklastmajiang, state);
		player.writeSocket(res.build());
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
		for (List<Integer> actList : actionSeatMap.values()) {
			if (actList == null || actList.size() == 0) {
				continue;
			}
			if (NxMjAction.getFirstXiaoHu(actList) != -1) {
				return true;
			}
		}
		return false;
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
	public boolean hasXiaoHu(List<Integer> actList) {
		if (NxMjAction.getFirstXiaoHu(actList) != -1) {
			return true;
		}
		return false;
	}
	/**
	 * 是否报听
	 *
	 * @return
	 */
	public boolean hasBaoTing(List<Integer> actList) {
		if (actList !=null && actList.size() >=19 && actList.get(18) == 1) {
			return true;
		}
		return false;
	}

	@Override
	public boolean isPlaying() {
		if (super.isPlaying()) {
			return true;
		}
		return getTableStatus() == NxMjConstants.TABLE_STATUS_PIAO;
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

	public int getBuChi() {
		return buChi;
	}

	public void setBuChi(int buChi) {
		this.buChi = buChi;
	}

	public int getXiaohuZiMo() {
		return xiaohuZiMo;
	}

	public void setXiaohuZiMo(int xiaohuZiMo) {
		this.xiaohuZiMo = xiaohuZiMo;
	}

	public int getOnlyDaHu() {
		return OnlyDaHu;
	}

	public void setOnlyDaHu(int onlyDaHu) {
		OnlyDaHu = onlyDaHu;
	}

	public int getJiajianghu() {
		return jiajianghu;
	}

	public void setJiajianghu(int jiajianghu) {
		this.jiajianghu = jiajianghu;
	}

	public int getQueYiMen() {
		return queYiMen;
	}

	public int getMenqing() {
		return menqing;
	}
	public int getMenqingZM() {
		return menQingZM;
	}
	public boolean isChoupai40(){
		return choupai40 == 1;
	}

	public int getPinghuNojiepao() {
		return pinghuNojiepao;
	}

	public int getQuanqiurJiang() {
		return quanqiurJiang;
	}

	public String getTableMsg() {
		Map<String, Object> json = new HashMap<>();
		json.put("wanFa", "宁乡麻将");
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
		return "宁乡麻将";
    }
    
    public static void main(String[] args) {
//    	List<Integer> copy = new ArrayList<>(NxMjConstants.zhuanzhuan_mjList);
//    	
//		for (int i = 0; i < 100000; i++) {
//			for (int j = 0; j < 40; j++) {
//				int max = copy.size();
//				int index =(int) (Math.random()*(max-1));
//				System.out.print(index);
//				System.out.print(",");
//			}
//			System.out.println();
//		}
    	Map<Integer, Integer> copyMap = new HashMap<>();
    	copyMap.put(1, 2);
    	copyMap.put(2, 2);
    	copyMap.put(3, 1);
    	copyMap.put(4, 0);
		copyMap.replaceAll((s1,s2)->{
	        if(s2.intValue()>0) {
	        s2=0;
	    }
	        return s2;
		});
		System.out.println(copyMap);
	}

}
