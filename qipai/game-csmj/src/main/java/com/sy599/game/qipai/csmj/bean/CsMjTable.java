package com.sy599.game.qipai.csmj.bean;

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
import com.sy599.game.qipai.csmj.robot.btlibrary.CsmjMidRobotActionResult;
import com.sy599.game.robot.RobotManager;
import jbt.execution.core.IBTExecutor;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.sy599.game.GameServerConfig;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.FirstmythConstants;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.common.constant.SharedConstants.player_state;
import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.db.bean.TableInf;
import com.sy599.game.db.bean.UserPlaylog;
import com.sy599.game.db.dao.TableDao;
import com.sy599.game.db.dao.TableLogDao;
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
import com.sy599.game.msg.serverPacket.TableRes.CreateTableRes;
import com.sy599.game.msg.serverPacket.TableRes.DealInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes;
import com.sy599.game.qipai.csmj.constant.CsMjAction;
import com.sy599.game.qipai.csmj.constant.CsMjConstants;
import com.sy599.game.qipai.csmj.rule.CsMj;
import com.sy599.game.qipai.csmj.rule.CsMjHelper;
import com.sy599.game.qipai.csmj.rule.CsMjRobotAI;
import com.sy599.game.qipai.csmj.rule.CsMjRule;
import com.sy599.game.qipai.csmj.tool.CsMjQipaiTool;
import com.sy599.game.qipai.csmj.tool.CsMjResTool;
import com.sy599.game.qipai.csmj.tool.CsMjTool;
import com.sy599.game.udplog.UdpLogger;
import com.sy599.game.util.DataMapUtil;
import com.sy599.game.util.GameUtil;
import com.sy599.game.util.JacksonUtil;
import com.sy599.game.util.JsonWrapper;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.util.StringUtil;
import com.sy599.game.util.TimeUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.qipai.csmj.robot.btlibrary.CsmjRobotActionResult;

/**
 * @author liuping ????????????????????????
 */
public class CsMjTable extends BaseTable {
	/**
	 * ????????????????????????
	 */
	private List<CsMj> nowDisCardIds = new ArrayList<>();
	/**
	 * ?????????????????????????????????
	 */
	private Map<Integer, List<Integer>> actionSeatMap = new ConcurrentHashMap<>();
	/**
	 * 0??? 1??? 2?????? 3??????(???????????????????????? ????????????3??????)4??? 5??????(6????????? 7????????? 8????????? 9????????? 10????????? 11??????
	 * 12????????? 13???????????? 14???????????????)
	 */
	private Map<Integer, Map<Integer, List<Integer>>> gangSeatMap = new ConcurrentHashMap<>();
	/**
	 * ??????????????????????????????
	 */
	private int maxPlayerCount = 4;
	/**
	 * ????????????????????????????????????
	 */
	private List<CsMj> leftMajiangs = new ArrayList<>();
	/**
	 * ??????????????????????????????map
	 */
	private Map<Long, CsMjPlayer> playerMap = new ConcurrentHashMap<Long, CsMjPlayer>();
	/**
	 * ???????????????????????????MAP
	 */
	private Map<Integer, CsMjPlayer> seatMap = new ConcurrentHashMap<Integer, CsMjPlayer>();
	/**
	 * ???????????????
	 */
	private Map<Integer, Integer> huConfirmMap = new HashMap<>();
	/**
	 * ?????????????????????????????? ??????????????????????????????????????? 1??????????????????????????????????????? ??????????????????????????? ??????????????????
	 * 2???????????????????????????????????????????????????????????????????????????????????????????????? ?????????????????????????????????????????? ?????????????????????????????????????????????
	 */
	private Map<Integer, CsMjTempAction> tempActionMap = new ConcurrentHashMap<>();
	/**
	 * ??????
	 */
	private int birdNum;
	/**
	 * ????????????
	 */
	private int isCalcBanker;
	/**
	 * ?????????????????? 1?????????+1 2??????????????? 3???????????????
	 */
	private int calcBird;
	/**
	 * ???????????? 1?????? 0?????????
	 */
	private int kePiao;

	/**
	 * ????????????seat
	 */
	private int moMajiangSeat;
	/**
	 * ???????????????
	 */
	private CsMj moGang;
	/**
	 * ??????????????????
	 */
	private CsMj gangMajiang;
	/**
	 * ?????????
	 */
	private List<Integer> moGangHuList = new ArrayList<>();
	/**
	 * ?????????????????????
	 */
	private List<CsMj> gangDisMajiangs = new ArrayList<>();
	/**
	 * ?????????????????????
	 */
	private int moLastMajiangSeat;
	/**
	 * ????????????????????????
	 */
	private int askLastMajaingSeat;
	/**
	 * ??????????????????????????????
	 */
	private int fristLastMajiangSeat;

	/**
	 * ?????????????????????
	 */
	private List<Integer> moLastSeats = new ArrayList<>();
	/**
	 * ??????????????????
	 */
	private CsMj lastMajiang;
	/**
	 *
	 */
	private int disEventAction;

	/*** GPS?????? */
	private int gpsWarn = 0;
	/*** ????????? */
	private int queYiSe = 0;
	/*** ????????? */
	private int banbanHu = 0;
	/*** ????????? */
	private int yiZhiHua = 0;
	/*** ????????? */
	private int liuliuShun = 0;
	/*** ????????? */
	private int daSiXi = 0;
	/*** ???????????? */
	private int jinTongYuNu = 0;
	/*** ????????? */
	private int jieJieGao = 0;
	/*** ?????? */
	private int sanTong = 0;
	/*** ??????????????? */
	private int zhongTuLiuLiuShun = 0;
	/*** ???????????? */
	private int zhongTuSiXi = 0;

	/*** ????????????????????????????????? */
	private List<Integer> showMjSeat = new ArrayList<>();

	private int tableStatus;// ???????????? 1??????

	/*** ???????????? **/
	private int gangDice = -1;

	/*** ????????????????????? */
	private List<Integer> moTailPai = new ArrayList<>();

	/** ???????????????????????????????????? **/
	private CsMj gangActedMj = null;

	/** ??????????????? **/
	private boolean isBegin = false;

	private int randomBanker;

	private int dealDice;

	// ???????????????0??????1???
	private int jiaBei;
	// ?????????????????????xx???????????????
	private int jiaBeiFen;
	// ????????????????????????
	private int jiaBeiShu;

	// ??????????????? 1??????????????? 2:????????????
	private int kongBird;
	// ???????????????1????????????
	private int buChi;
	// ???????????????????????????
	private int OnlyDaHu;
	// ???????????????????????????
	private int xiaohuZiMo;
	// ????????????????????????
	private int queYiMen;

	private int jiajianghu;

	/** ??????1????????????2????????? */
	private int autoPlayGlob;
	private int autoTableCount;
	private int isAutoPlay;// ????????????
	private int readyTime = 0;

	private int menqing;// ??????
	private int topFen;// ????????????
	
	private int gangMoSi;// ??????4???
	
	private int qiShouNiaoFen;// ????????????1:????????????
	//??????below??????
	private int belowAdd=0;
	private int below=0;
	
	
	private int gangBuF;// ????????????
	private int quanqiurJiang;// ???????????????
	
	private int difen;// ??????
	
	private int menQingZM;// ??????
	
	
	private int jsXiaoHuF;// ??????????????????
	
	private int xiaoHuAuto;// ???????????????
	
	private int xiaoHuGdF;//???????????????
	
	private int jjHKqFg;//?????????????????????
	
	@Override
	protected boolean quitPlayer1(Player player) {
		return false;
	}

	@Override
	public boolean canQuit(Player player) {
		if (super.canQuit(player)) {
			return getTableStatus() != CsMjConstants.TABLE_STATUS_PIAO;
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
		if(state!=table_state.play){
    		return;
		}
		List<Integer> winSeatList = new ArrayList<>(huConfirmMap.keySet());
		boolean selfMo = false;
		int[] birdMjIds = null;
		int[] seatBirds = null;
		Map<Integer, Integer> seatBirdMap = new HashMap<>();
		boolean flow = false;

		int startseat = 0;
		int catchBirdSeat = 0;
		

		// ??????
		boolean zhuaNiao = true;
		if (winSeatList.size() == 0) {
			// ??????
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
			// ??????
			if (leftMajiangs.size() == 0) {
				birdMjIds = zhuaNiao(lastMajiang);
			} else {
				// ?????????
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

		// ?????????
		if (winSeatList.size() != 0) {
			// ??????????????????????????????
			CsMjPlayer winPlayer = null;
			if (winSeatList.size() == 1) {
				winPlayer = seatMap.get(winSeatList.get(0));
				if ((winPlayer.isAlreadyMoMajiang() || winPlayer.isGangshangHua())
						&& winSeatList.get(0) == moMajiangSeat) {
					selfMo = true;
				}
			}
			// ??????
			if (selfMo) {
				calZiMoPoint(seatBirdMap, winPlayer, true, 1, true, selfMo);
				winPlayer.changeAction(7, 1);
				winPlayer.getMyExtend().setMjFengshen(FirstmythConstants.firstmyth_index8, 1);
			} else {
				CsMjPlayer losePlayer = seatMap.get(disCardSeat);
				int loserSeat = losePlayer.getSeat();
				losePlayer.getMyExtend().setMjFengshen(FirstmythConstants.firstmyth_index10, winSeatList.size());
				int totalLosePoint = 0;
				for (int winSeat : winSeatList) {
					// ??????
					winPlayer = seatMap.get(winSeat);
					int daHuCount = winPlayer.getDahuPointCount();
					// ??????
					int winPoint = difen;
					if (daHuCount > 0) {
						winPoint = calcDaHuPoint(daHuCount);
						losePlayer.changeActionTotal(CsMjAction.ACTION_COUNT_DAHU_DIANPAO, 1);
						winPlayer.changeActionTotal(CsMjAction.ACTION_COUNT_DAHU_JIEPAO, 1);
					} else {
						winPoint = difen;
						losePlayer.changeActionTotal(CsMjAction.ACTION_COUNT_XIAOHU_DIANPAO, 1);
						winPlayer.changeActionTotal(CsMjAction.ACTION_COUNT_XIAOHU_JIEPAO, 1);
					}

					// ?????????
					if ((winPlayer.getSeat() == lastWinSeat || loserSeat == lastWinSeat)) {
						winPoint = calcBankerPoint(winPoint, daHuCount);
					}

					int totalBirdNum = seatBirdMap.get(winSeat) + seatBirdMap.get(loserSeat);

					winPoint = calcBirdPoint(winPoint, totalBirdNum, true);

					// ??????
					if (kePiao >= 1) {
						winPoint += (winPlayer.getPiaoPoint() + losePlayer.getPiaoPoint());
					}

					
					if (winPoint > topFen&&topFen>0) {
						if(maxPlayerCount ==3||maxPlayerCount==4){
							winPoint = topFen;
						}
					}
					
					
					totalLosePoint += winPoint;
					winPlayer.changeAction(6, 1);
					losePlayer.changeAction(0, 1);
					winPlayer.setLostPoint(winPoint);
				}
				losePlayer.setLostPoint(-totalLosePoint);
			}

		}

		// ????????????
		calXiaoHuFen(winSeatList, selfMo, seatBirdMap);

		boolean over = playBureau == totalBureau;
        if (autoPlayGlob > 0) {
            // //????????????
            boolean diss = false;
            if (autoPlayGlob == 1) {
                for (CsMjPlayer seat : seatMap.values()) {
                    if (seat.isAutoPlay()) {
                        diss = true;
                        break;
                    }

                }
            } else if (autoPlayGlob == 3) {
                diss = checkAuto3(3);
            }else if (autoPlayGlob == 4) {
                diss = checkAuto3(2);
            }
            if (diss) {
                autoPlayDiss = true;
                over = true;
            }
        }

		if(over){
		    calcPointBeforeOver();
        }

        // -----------?????????---------------------------------
        if (isGoldRoom()) {
            for (CsMjPlayer player : seatMap.values()) {
                player.setPoint(player.getTotalPoint());
                player.setWinGold(player.getTotalPoint());
            }
            calcGoldRoom();
        }


		// ?????????????????????
		ClosingMjInfoRes.Builder res = sendAccountsMsg(over, selfMo, winSeatList, birdMjIds, seatBirds, seatBirdMap,
				false, startseat, catchBirdSeat);
		// ????????????
		if (!flow) {
			if (winSeatList.size() > 1) {
				// ???????????????????????????????????????
				setLastWinSeat(disCardSeat);
			} else {
				setLastWinSeat(winSeatList.get(0));
			}

		} else {
			if (moLastMajiangSeat != 0) {
				// ??????????????????????????????????????????
				setLastWinSeat(moLastMajiangSeat);

			} else if (fristLastMajiangSeat != 0) {
				// ???????????????????????????????????????????????????????????????
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

		for (CsMjPlayer player : seatMap.values()) {
			if (player.isAutoPlaySelf()) {
				player.setAutoPlay(false, false);
			}
		}

		for (Player player : seatMap.values()) {
			player.saveBaseInfo();
		}
	}

    public void calcPointBeforeOver() {
        //  ------------???????????????????????? ------------
        if (jiaBei == 1) {
            int jiaBeiPoint = 0;
            int loserCount = 0;
            for (CsMjPlayer player : seatMap.values()) {
                if (player.getTotalPoint() > 0 && player.getTotalPoint() < jiaBeiFen) {
                    jiaBeiPoint += player.getTotalPoint() * (jiaBeiShu - 1);
                    player.setTotalPoint(player.getTotalPoint() * jiaBeiShu);
                } else if (player.getTotalPoint() < 0) {
                    loserCount++;
                }
            }
            if (jiaBeiPoint > 0) {
                for (CsMjPlayer player : seatMap.values()) {
                    if (player.getTotalPoint() < 0) {
                        player.setTotalPoint(player.getTotalPoint() - (jiaBeiPoint / loserCount));
                    }
                }
            }
        }

        // ------------???????????????below???+belowAdd??? ------------
        if (belowAdd > 0 && playerMap.size() == 2) {
            for (CsMjPlayer player : seatMap.values()) {
                int totalPoint = player.getTotalPoint();
                if (totalPoint > -below && totalPoint < 0) {
                    player.setTotalPoint(player.getTotalPoint() - belowAdd);
                } else if (totalPoint < below && totalPoint > 0) {
                    player.setTotalPoint(player.getTotalPoint() + belowAdd);
                }
            }
        }
    }

    private void calXiaoHuFen(List<Integer> winSeatList, boolean selfMo, Map<Integer, Integer> seatBirdMap) {
		for (CsMjPlayer winPlayer : seatMap.values()) {
			if (winPlayer.getHuXiaohu().size() == 0) {
				continue;
			}

			//????????????2???
			if(xiaoHuGdF==1){
				calXiaoPoint2(winPlayer, winPlayer.getHuXiaohu().size());
			}else{
				boolean addBirdPoint = true;
				// ????????????????????????????????????
				if (winSeatList.contains(winPlayer.getSeat())) {
					addBirdPoint = false;
				}
				
				if(qiShouNiaoFen ==1) {
					addBirdPoint = false;
				}

				calZiMoPoint(seatBirdMap, winPlayer, false, winPlayer.getHuXiaohu().size(), addBirdPoint, selfMo);
			}
			
			
		}

		for (CsMjPlayer seat : seatMap.values()) {
			
			if (topFen > 0 && maxPlayerCount == 2) {
				if (Math.abs(seat.getLostPoint()) > topFen) {
					seat.setLostPoint(seat.getLostPoint() > 0 ? topFen : -topFen);
				}
			}
			seat.changePoint(seat.getLostPoint()+seat.getGangPoint());

		}
	}

	private boolean checkAuto3(int count) {
		boolean diss = false;
		// if(autoPlayGlob==3) {
		boolean diss2 = false;
		for (CsMjPlayer seat : seatMap.values()) {
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
		if (autoTableCount == count) {
			diss = true;
		}
		// }
		return diss;
	}


	private void calZiMoPoint(Map<Integer, Integer> seatBirdMap, CsMjPlayer winPlayer, boolean dahu, int xiaohuNum,
			boolean addBirdPoint, boolean zimo) {
		int daHuCount = 0;
		if (dahu) {
			daHuCount = winPlayer.getDahuPointCount();
		}
		int winSeat = winPlayer.getSeat();

		int winPoint = difen;
		// ??????
		if (daHuCount > 0) {
			winPoint = calcDaHuPoint(daHuCount);
			winPlayer.changeActionTotal(CsMjAction.ACTION_COUNT_DAHU_ZIMO, 1);
		} else {
			winPoint = difen;
			winPlayer.changeActionTotal(CsMjAction.ACTION_COUNT_XIAOHU_ZIMO, 1);
		}

		// ???????????????
		if (winPlayer.getSeat() == lastWinSeat) {
			winPoint = calcBankerPoint(winPoint, daHuCount);
		}
		//seatBirdMap.get(winSeat)
		int winBirdNum = getBirdBySeat(seatBirdMap, winSeat);
		int totalWinPoint = 0;
		for (int loserSeat : seatMap.keySet()) {
			// ????????????????????????
			if (winSeat == loserSeat) {
				continue;
			}
			CsMjPlayer loser = seatMap.get(loserSeat);
			int losePoint = winPoint;
			// ???????????????
			if (loser.getSeat() == lastWinSeat) {
				losePoint = calcBankerPoint(losePoint, daHuCount);
			}

			losePoint *= xiaohuNum;

			//seatBirdMap.get(loserSeat)
			int totalBirdNum = winBirdNum + getBirdBySeat(seatBirdMap, loserSeat);

			// ??????????????????????????????????????????????????????????????????
			if (!zimo && loser.getSeat() != disCardSeat) {
				addBirdPoint = true;
			}
			// ????????????
			if(dahu||qiShouNiaoFen!=1) {
				losePoint = calcBirdPoint(losePoint, totalBirdNum, addBirdPoint);	
			}
			// ??????
			if (dahu) {
				if (kePiao >= 1) {
					losePoint += (loser.getPiaoPoint() + winPlayer.getPiaoPoint());
				}
			}
			
			if (dahu&&losePoint > topFen&&topFen>0) {
				
				if(maxPlayerCount ==3||maxPlayerCount==4){
					losePoint = topFen;
				}
			}
			totalWinPoint += losePoint;
			loser.changeLostPoint(-losePoint);
		}
		winPlayer.changeLostPoint(totalWinPoint);
	}
	
	
	
	
	private void calXiaoPoint2(CsMjPlayer winPlayer, int xiaohuNum) {
		int totalWinPoint = 0;
		for (int loserSeat : seatMap.keySet()) {
			// ????????????????????????
			if (winPlayer.getSeat() == loserSeat) {
				continue;
			}
			CsMjPlayer loser = seatMap.get(loserSeat);
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
	 * ??????????????????
	 *
	 * @param point
	 * @param bird
	 * @return
	 */
	private int calcBirdPoint(int point, int bird, boolean addBirdPoint) {
		if (bird <= 0) {
			return point;
		}
		if (calcBird == 1 && addBirdPoint) {

			// ??????????????????
			point = point + bird;
		} else if (calcBird == 2) {
			// ?????????2???bird??????
			point = (int) (point * (Math.pow(2, bird)));

		} else if (calcBird == 3) {
			// ??????
			point *= (bird + 1);
		}
		return point;
	}

	/**
	 * ??????????????????
	 *
	 * @param point
	 * @return
	 */
	private int calcBankerPoint(int point, int dahuCount) {

		if (dahuCount == 0) {
			dahuCount = 1;
		}
		point += dahuCount;

		return point;
	}

	/**
	 * ????????????
	 *
	 * @return
	 */
	private int calcDaHuPoint(int daHuCount) {
		int point = 5+difen;
		point = point * daHuCount;

		return point;
	}

	/**
	 * ??????????????? ???????????????????????????????????????
	 *
	 * @param seat
	 * @return
	 */
	private int calcXiaoHuPoint(int seat) {
		int lostXiaoHuCount = 0;
		CsMjPlayer player = seatMap.get(seat);
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
		for (CsMjPlayer player : playerMap.values()) {
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
		// for (CsMjPlayer player : playerMap.values()) {
		// player.addRecord(logId, playBureau);
		// }
		// UdpLogger.getInstance().sendSnapshotLog(masterId, playLog, logRes);
	}

	/**
	 * ????????????
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
	 * ??????
	 *
	 * @return
	 */
	private int[] zhuaNiao(CsMj lastMaj) {
		// ?????????
		int realBirdNum = leftMajiangs.size() > birdNum ? birdNum : leftMajiangs.size();

		if (realBirdNum == 0) {
			realBirdNum = birdNum;
		}
		int[] bird = new int[realBirdNum];
		for (int i = 0; i < realBirdNum; i++) {
			CsMj prickbirdMajiang = null;
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
		// ???????????????
		return bird;
	}

	/**
	 * ???????????????????????????
	 *
	 *            rdMajiangIds
	 * @param bankerSeat
	 * @return
	 */
	private int[] birdToSeat(int[] prickBirdMajiangIds, int bankerSeat) {
		int[] seatArr = new int[prickBirdMajiangIds.length];
		for (int i = 0; i < prickBirdMajiangIds.length; i++) {
			CsMj majiang = CsMj.getMajang(prickBirdMajiangIds[i]);
			int prickbirdPai = majiang.getPai();

			int prickbirdseat = 0;
			if (maxPlayerCount == 4) {
				prickbirdPai = (prickbirdPai - 1) % 4;// ?????????????????? ?????????1
				prickbirdseat = prickbirdPai + bankerSeat > 4 ? prickbirdPai + bankerSeat - 4
						: prickbirdPai + bankerSeat;
			} else if (maxPlayerCount == 3) {
				// ????????????
				if (kongBird == 2) {
					prickbirdPai = (prickbirdPai - 1) % 3;// ?????????????????? ?????????1
					prickbirdseat = prickbirdPai + bankerSeat > 3 ? prickbirdPai + bankerSeat - 3
							: prickbirdPai + bankerSeat;
				} else {
					// 4-8 ??????
					if (prickbirdPai == 1 || prickbirdPai == 5 || prickbirdPai == 9) {
						prickbirdseat = bankerSeat;
					} else if (prickbirdPai == 2 || prickbirdPai == 6) {
						// ?????????
						prickbirdseat = (bankerSeat % 3) + 1;
					} else if (prickbirdPai == 3 || prickbirdPai == 7) {
						// ?????????
						prickbirdseat = ((bankerSeat % 3) + 1) % 3 + 1;
					}
				}
			} else {
				if (prickbirdPai == 1 || prickbirdPai == 5 || prickbirdPai == 9) {
					prickbirdseat = bankerSeat;
				} else if (prickbirdPai == 3 || prickbirdPai == 7) {
					prickbirdseat = (bankerSeat % 2) + 1;
				}

				// //?????? 2468 ??????
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
				tempMap.put("nowDisCardIds", StringUtil.implode(CsMjHelper.toMajiangIds(nowDisCardIds), ","));
			}
			if (tempMap.containsKey("leftPais")) {
				tempMap.put("leftPais", StringUtil.implode(CsMjHelper.toMajiangIds(leftMajiangs), ","));
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
	// for (CsMjPlayer player : seatMap.values()) {
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
		addPlayLog(disCardRound + "_" + lastWinSeat + "_" + CsMjDisAction.action_dice + "_" + dealDice);
		setDealDice(dealDice);
		logFaPaiTable();

		for (CsMjPlayer tablePlayer : seatMap.values()) {
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
	            	addPlayLog(getDisCardRound() + "_" +tablePlayer.getSeat() + "_" + CsMjConstants.action_tuoguan + "_" +1);
	           }

		}
		isBegin = true;
		if (!hasXiaoHu()) {
			// ????????????????????????????????????
			CsMjPlayer bankPlayer = seatMap.get(lastWinSeat);
			ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.req_com_kt_ask_dismajiang);// ??????????????????
			bankPlayer.writeSocket(com.build());
			// isBegin = false;
		}
	}

	/**
	 * ??????
	 *
	 * @param player
	 */
	public void moMajiang(CsMjPlayer player, boolean isBuzhang) {
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

		// ???????????????????????? ????????????&& isBuzhang
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
		// ??????
		CsMj majiang = null;
		if (disCardRound != 0) {
			// ????????????????????????????????????????????????
			if (player.isAlreadyMoMajiang()) {
				return;
			}
			if (GameServerConfig.isDebug() && !player.isRobot()) {
				if (zpMap.containsKey(player.getUserId()) && zpMap.get(player.getUserId()) > 0) {
					majiang = CsMjQipaiTool.findMajiangByVal(leftMajiangs, zpMap.get(player.getUserId()));
					if (majiang != null) {
						zpMap.remove(player.getUserId());
						leftMajiangs.remove(majiang);
					}
				}
			}
			// ???????????????????????????
			// ????????????????????? ?????????
			// majiang=majiangt
			// majiang = MajiangHelper.findMajiangByVal(leftMajiangs, 25);
			// leftMajiangs.remove(majiang);
			if (majiang == null) {
				majiang = getLeftMajiang();
				
			}
		}
		if (majiang != null) {
			addPlayLog(disCardRound + "_" + player.getSeat() + "_" + CsMjDisAction.action_moMjiang + "_"
					+ majiang.getId());
			player.moMajiang(majiang);
		}

		processHideMj(player);

		// ????????????
		clearActionSeatMap();
		if (disCardRound == 0) {
			return;
		}
		setMoMajiangSeat(player.getSeat());
		List<Integer> arr = player.checkMo(majiang, false);

		if (!arr.isEmpty()) {
			// ????????????????????????????????????????????????????????????
			if (!player.getGang().isEmpty() && !checkSameMj(player.getPeng(), majiang)) {
				arr.set(CsMjAction.MINGGANG, 0);
				arr.set(CsMjAction.ANGANG, 0);
				arr.set(CsMjAction.BUZHANG, 0);
				arr.set(CsMjAction.BUZHANG_AN, 0);
			}
			coverAddActionSeat(player.getSeat(), arr);
		}
		MoMajiangRes.Builder res = MoMajiangRes.newBuilder();
		res.setUserId(player.getUserId() + "");
		res.setRemain(getLeftMajiangCount());
		res.setSeat(player.getSeat());

		// boolean playCommand = !player.getGang().isEmpty() && arr.isEmpty();
		logMoMj(player, majiang, arr);
		for (CsMjPlayer seat : seatMap.values()) {
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

	private boolean checkSameMj(List<CsMj> list, CsMj majiang) {
		if (list.size() == 0) {
			return false;
		}
		for (CsMj mj : list) {
			if (mj.getVal() == majiang.getVal()) {
				return true;
			}
		}
		return false;
	}

	public void calcMoLastSeats(int firstSeat) {
		for (int i = 0; i < getMaxPlayerCount(); i++) {
			CsMjPlayer player = seatMap.get(firstSeat);
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
	 * ?????????????????????
	 *
	 * @param seat
	 *            0????????????????????????>0??????????????????????????????????????????
	 * @return ???????????????????????????
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
		CsMjPlayer player = seatMap.get(sendSeat);
		sendMoLast(player, 1);
	}

	private void buildPlayRes(PlayMajiangRes.Builder builder, Player player, int action, List<CsMj> majiangs) {
		CsMjResTool.buildPlayRes(builder, player, action, majiangs);
		buildPlayRes1(builder);
	}

	private void buildPlayRes1(PlayMajiangRes.Builder builder) {
		// builder
	}

	/**
	 * ?????????
	 *
	 * @param player
	 * @param majiangs
	 *            ?????????????????????
	 * @param xiaoHuType
	 *            ???????????? CsMjAction
	 * @param action
	 */
	public synchronized void huXiaoHu(CsMjPlayer player, List<CsMj> majiangs, int xiaoHuType, int action) {
		List<Integer> actionList = actionSeatMap.get(player.getSeat());
		if (actionList == null || actionList.isEmpty() || actionList.get(xiaoHuType) == 0) {// ??????????????????
			return;
		}

		CsMjiangHu hu = new CsMjiangHu();
		List<CsMj> copy2 = new ArrayList<>(player.getHandMajiang());
		CsMjRule.checkXiaoHu2(hu, copy2, isBegin(), this);

		HashMap<Integer, Map<Integer, List<CsMj>>> xiaohuMap = hu.getXiaohuMap();
		Map<Integer, List<CsMj>> map = xiaohuMap.get(xiaoHuType);
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

		if (!player.getHandMajiang().containsAll(majiangs)) {// ??????????????????????????????
			return;
		}
		
		
		List<CsMj> vals= map.get(huCard);
		
		addXiaoHuCards(player, xiaoHuType, vals);
        player.addXiaoHuMjList(majiangs);
//		
//		if(xiaoHuType ==CsMjAction.LIULIUSHUN||xiaoHuType==CsMjAction.ZHONGTULIULIUSHUN){
//			
//		}else{
//			player.addXiaoHu2(xiaoHuType, huCard);
//		}
		
		
		
	

		removeActionSeat(player.getSeat());
		addPlayLog(disCardRound + "_" + player.getSeat() + "_" + CsMjDisAction.action_xiaohu + "_"
				+ CsMjHelper.toMajiangStrs(majiangs) + "_" + xiaoHuType);
		PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
		buildPlayRes(builder, player, CsMjDisAction.action_xiaohu, majiangs);
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
		for (CsMjPlayer seat : seatMap.values()) {
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
	
	
	
	
	public boolean checkXiaoHuCards(int type,int val){
		
		for (CsMjPlayer seat : seatMap.values()) {
			List<Integer> vals = seat.getHuxiaoHuCardVal().get(type);
			if(vals!=null && vals.contains(val)){
				return true;
			}
		}
		return false;
	}
	

	private void addXiaoHuCards(CsMjPlayer player, int xiaoHuType, List<CsMj> mjVals) {
		List<Integer> valus = new ArrayList<Integer>();
		if(mjVals!=null && !mjVals.isEmpty()){
			for(CsMj mj: mjVals){
				valus.add(mj.getVal());
			}
		}
	
		player.addLiuLiuShunHu2(xiaoHuType, valus);
	}

	/**
	 * ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
	 */
	public void checkBegin(CsMjPlayer player) {
		boolean isBegin = isBegin();
		if (isBegin && !hasXiaoHu()) {
			CsMjPlayer bankPlayer = seatMap.get(lastWinSeat);
			List<Integer> actList = bankPlayer.checkMo(null, isBegin);
			if (!actList.isEmpty()) {
				PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
				buildPlayRes(builder, player, CsMjDisAction.action_pass, new ArrayList<>());
				if (!actList.isEmpty()) {
					addActionSeat(bankPlayer.getSeat(), actList);
					builder.addAllSelfAct(actList);
				}
				bankPlayer.writeSocket(builder.build());
			}
			ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.req_com_kt_ask_dismajiang);// ??????????????????
			bankPlayer.writeSocket(com.build());
		}
	}

	/**
	 * ???????????????
	 *
	 * @param player
	 * @param majiangs
	 */
    private void hu(CsMjPlayer player, List<CsMj> majiangs, int action) {
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return;
        }
        if (huConfirmMap.containsKey(player.getSeat())) {
            return;
        }
        
        if(hasXiaoHu()){
        	return;
        }
        
        huExt(player, action,true);
    }

	private void huExt(CsMjPlayer player, int action,boolean flag) {
		checkRemoveMj(player, action);
        boolean zimo = player.isAlreadyMoMajiang();
        CsMj disMajiang = null;
        CsMjiangHu huBean = null;
        List<CsMj> huMjs = new ArrayList<>();
        int fromSeat = 0;
        boolean isGangShangHu = false;



        if (!zimo) {
			if (moGangHuList.contains(player.getSeat())) {// ?????????
                disMajiang = moGang;
                fromSeat = moMajiangSeat;
                huMjs.add(moGang);
			} else if (isHasGangAction(player.getSeat())) {// ????????? ?????????
                fromSeat = moMajiangSeat;
                Map<Integer, CsMjiangHu> huMap = new HashMap<>();
                List<Integer> daHuMjIds = new ArrayList<>();
                List<Integer> huMjIds = new ArrayList<>();
                for (int majiangId : gangSeatMap.keySet()) {
                    CsMjiangHu temp = player.checkHu(CsMj.getMajang(majiangId), disCardRound == 0);
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
					// ?????????
                    for(int mjId : huMjIds){
                        CsMjiangHu temp = huMap.get(mjId);
                        if (moMajiangSeat == player.getSeat()) {
                            temp.setGangShangHua(true);
                            isGangShangHu = true;
                        } else {
							// ????????????
                        	CsMjPlayer mPlayer = seatMap.get(moMajiangSeat);
                       	 removeGangMj(mPlayer, mjId);
                            temp.setGangShangPao(true);
                        }
                        temp.initDahuList();
                        if(huBean == null){
                            huBean = temp;
                        }else{
                            huBean.addToDahu(temp.getDahuList());
                            huBean.getShowMajiangs().add(CsMj.getMajang(mjId));
                        }
                        player.addHuMjId(mjId);
                        huMjs.add(CsMj.getMajang(mjId));
                    }
                }else if(huMjIds.size() > 0){
					// ????????????
                    for(int mjId : huMjIds) {
                        CsMjiangHu temp = huMap.get(mjId);
                        if (moMajiangSeat == player.getSeat()) {
                            temp.setGangShangHua(true);
                            isGangShangHu = true;
                        } else {
                        	CsMjPlayer mPlayer = seatMap.get(moMajiangSeat);
                        	removeGangMj(mPlayer, mjId);
                            temp.setGangShangPao(true);
                        }
                        temp.initDahuList();
                        if(huBean == null){
                            huBean = temp;
                        }else{
                            huBean.addToDahu(temp.getDahuList());
                            huBean.getShowMajiangs().add(CsMj.getMajang(mjId));
                        }
                        player.addHuMjId(mjId);
                        huMjs.add(CsMj.getMajang(mjId));
                    }
                }else{
                    huBean = new CsMjiangHu();
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
//        if(isBegin()){
		// //??????
//            if(huBean == null){
//                huBean = player.checkHu(null,true);
//            }
//            if(huBean.isHu()){
//                huBean.setTianhu(true);
//                huBean.initDahuList();
//            }
//        }else if(nowDisCardSeat == lastWinSeat && seatMap.get(lastWinSeat).getOutPais().size() == 0){
		// //??????
//            if(huBean == null){
//                huBean = player.checkHu(lastMajiang,true);
//            }
//            if(huBean.isHu()){
//                huBean.setDihu(true);
//                huBean.initDahuList();
//            }
//        }

        if (huBean == null) {
			// ??????
            huBean = player.checkHu(disMajiang, disCardRound == 0);
            if (huBean.isHu() && lastMajiang != null) {
                huBean.setHaidilaoyue(true);
                huBean.initDahuList();
            }
        }
        if (!huBean.isHu()) {
			// ???????????????
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
        
		// ????????????
        if(!huBean.isMenqing()&&(getMenqing()==1 &&!player.isChiPengGang())) {
			huBean.setMenqing(true);
			 huBean.initDahuList();
		}
        if(!huBean.isMenqing()&&(getMenqingZM()==1 &&!player.isChiPengGang()&&zimo)) {
			huBean.setMenqing(true);
			 huBean.initDahuList();
		}
		// ?????????????????????????????????
    	CsMjPlayer csplayer = getPlayerBySeat(getLastWinSeat());
        
        if(disCardRound==0) {
        	 huBean.setTianhu(true);
             huBean.initDahuList();
        }else if(csplayer!=null&&csplayer.getDisCount()==1&&csplayer.getSeat()==disCardSeat&&player.getSeat()!= moMajiangSeat) {
        	 huBean.setDihu(true);
             huBean.initDahuList();
        }
        
        
		// ???????????????
        if (moGangHuList.contains(player.getSeat())) {
			// ??????????????????????????????
            if (disEventAction != CsMjDisAction.action_buzhang) {
                huBean.setQGangHu(true);
                huBean.initDahuList();
            }
			// ?????????
          //  CsMjPlayer moGangPlayer = getPlayerByHasMajiang(moGang);
            //if (moGangPlayer == null) {
            CsMjPlayer    moGangPlayer = seatMap.get(nowDisCardSeat);
            //}
            List<CsMj> moGangMajiangs = new ArrayList<>();
            moGangMajiangs.add(moGang);
            moGangPlayer.removeGangMj(moGangMajiangs.get(0));
//            if(huBean.isQGangHu()) {
//            	
//            }else {
//            	moGangPlayer.addOutPais(moGangMajiangs, 0,0);
//            }
			// ?????????????????? ??????????????????????????????
            recordDisMajiang(moGangMajiangs, moGangPlayer);
           // addPlayLog(disCardRound + "_" + moGangPlayer.getSeat() + "_" + 0 + "_" + CsMjHelper.toMajiangStrs(moGangMajiangs));
        }
        if (huBean.getDahuPoint() > 0) {
            player.setDahu(huBean.getDahuList());
        }
//        if (huBean.getDahuPoint() > 0) {
//            player.setDahu(huBean.getDahuList());
//            if (zimo) {
//                int point = 0;
//                for (CsMjPlayer seatPlayer : seatMap.values()) {
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
			// ????????????????????????????????????????????????????????????
            List<CsMj> gangDisMajiangs = getGangDisMajiangs();
            List<CsMj> chuMjs = new ArrayList<>();
            if(gangDisMajiangs != null && gangDisMajiangs.size() >0){
                for(CsMj mj : gangDisMajiangs){
                    if(!huMjs.contains(mj)){
                        chuMjs.add(mj);
                    }
                }
            }
            if(chuMjs != null){
                PlayMajiangRes.Builder chuPaiMsg = PlayMajiangRes.newBuilder();
                buildPlayRes(chuPaiMsg, player, CsMjDisAction.action_chupai, chuMjs);
                chuPaiMsg.setFromSeat(-1);
                broadMsgToAll(chuPaiMsg.build());
                player.addOutPais(chuMjs, CsMjDisAction.action_chupai,player.getSeat());
            }
        }

        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        buildPlayRes(builder, player, action, huBean.getShowMajiangs());
        builder.addAllHuArray(player.getDahu());
        if (zimo) {
            builder.setZimo(1);
        }
        builder.setFromSeat(fromSeat);
        
        
     // ??????????????????
        addHuList(player.getSeat(), disMajiang == null ? 0 : disMajiang.getId());
        
        List<Integer> passHuSeats = new ArrayList<>();
		// ???
        for (CsMjPlayer seat : seatMap.values()) {
        	if(disMajiang!=null&&seat.isPassHu().contains(disMajiang.getVal())){
        		passHuSeats.add(seat.getSeat());
        	}
			// ????????????
            seat.writeSocket(builder.build());
        }
		
        
        List<Integer> huActionList = getHuSeatByActionMap();
        huActionList.addAll(passHuSeats);
        if(huActionList.size()>1&&flag){
        	for (int huseat : huActionList) {
        		if(huseat!=player.getSeat()){
        			 addHuList(huseat, disMajiang == null ? 0 : disMajiang.getId());
					addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_"+ CsMjHelper.toMajiangStrs(huMjs)+"_"+StringUtil.implode(player.getDahu(), ","));
					huExt(seatMap.get(huseat), action,false);
            		}
        		
        	}
        }
        
        
//        if(huConfirmMap.size())
        
        
           if (isCalcOver()) {
			// ?????????????????? ???????????????????????????
            calcOver();
        }else{
            player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip);
        }
	}

	private void checkRemoveMj(CsMjPlayer player, int action) {
		//CsMj mjA = null;
		
        List<CsMj> mjBs = new ArrayList<CsMj>();
		for (int majiangId2 : gangSeatMap.keySet()) {
			Map<Integer, List<Integer>> map = gangSeatMap.get(majiangId2);
			List<Integer> actList = map.get(player.getSeat());
			if (actList == null) {
				continue;
			}
//			if (actList.get(CsMjAction.ZIMO) == 1) {
//				mjA = CsMj.getMajang(majiangId2);
//			} else

			if (actList.get(CsMjAction.MINGGANG) == 1) {
				mjBs.add(CsMj.getMajang(majiangId2));
			}
        }

		if(mjBs.size()>0) {
//			if(mjA.getId() !=mjB.getId()) {
			// ??????????????????
				List<CsMj> list = new ArrayList<>();
				list.addAll(mjBs);
				checkMoOutCard(list, player, action);
//			}
		}
	}

	private void removeGangMj(CsMjPlayer player, int mjId) {
		List<CsMj> moList = new ArrayList<>();
		moList.add(CsMj.getMajang(mjId));
		player.addOutPais(moList, 0, player.getSeat());
	}

	/**
	 * ?????????????????????????????????
	 *
	 * @param majiang
	 * @return
	 */
	private CsMjPlayer getPlayerByHasMajiang(CsMj majiang) {
		for (CsMjPlayer player : seatMap.values()) {
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
		// ??????????????????????????????
		if (!huActionList.isEmpty()) {
			over = true;
			CsMjPlayer moGangPlayer = null;
			if (!moGangHuList.isEmpty()) {
				// ??????????????????
				moGangPlayer = getPlayerByHasMajiang(moGang);
				if (moGangPlayer == null) {
					moGangPlayer = seatMap.get(moMajiangSeat);
				}
				LogUtil.monitor_i("mogang player:" + moGangPlayer.getSeat() + " moGang:" + moGang);
			}
			for (int huseat : huActionList) {
				if (moGangPlayer != null) {
					// ?????????????????????????????? ??????
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
			CsMjPlayer disCSMajiangPlayer = seatMap.get(disCardSeat);
			for (int huseat : huActionList) {
				if (huConfirmMap.containsKey(huseat)) {
					if (disCardRound == 0) {
						// ??????
						removeActionSeat(huseat);
					}
					continue;
				}
				PlayMajiangRes.Builder disBuilder = PlayMajiangRes.newBuilder();
				CsMjPlayer seatPlayer = seatMap.get(huseat);
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
	 * ?????????
	 *
	 * @param player
	 * @param majiangs
	 * @param action
	 */
	private void chiPengGang(CsMjPlayer player, List<CsMj> majiangs, int action) {
		List<Integer> actionList0 = actionSeatMap.get(player.getSeat());
		if(actionList0==null ||actionList0.isEmpty()){
			return;
		}
		
		PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
		logAction(player, action, 0, majiangs, null);
		// if (nowDisCardIds.size() > 1 && !isHasGangAction()) {
		// // ????????????????????????
		// return;
		// }
		List<Integer> huList = getHuSeatByActionMap();
		huList.remove((Object) player.getSeat());
		// if (!huList.isEmpty()) {
		// // ????????????
		// return;
		// }

		// ????????????????????????????????????????????????????????????????????????
		if (nowDisCardIds.size() > 1) {
			for (CsMj mj : nowDisCardIds) {
				List<Integer> hu = player.checkDisMaj(mj, false);
				if (!hu.isEmpty() && hu.get(0) == 1) {
					// && (actionList.get(CsMjAction.HU) == 1)
					List<Integer> actionList = actionSeatMap.get(player.getSeat());
					if (actionList != null) {
						actionList.set(CsMjAction.HU, 0);
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

		List<CsMj> handMajiang = new ArrayList<>(player.getHandMajiang());
		CsMj disMajiang = null;
		if (isHasGangAction()) {
			for (int majiangId : gangSeatMap.keySet()) {
				if (action == CsMjDisAction.action_chi) {
					List<Integer> majiangIds = CsMjHelper.toMajiangIds(majiangs);
					if (majiangIds.contains(majiangId)) {
						disMajiang = CsMj.getMajang(majiangId);
						gangActedMj = disMajiang;
						handMajiang.add(disMajiang);
						if (majiangs.size() > 1) {
							majiangs.remove(disMajiang);
						}
						break;
					}
				} else {
					CsMj mj = CsMj.getMajang(majiangId);
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
			sameCount = CsMjHelper.getMajiangCount(majiangs, majiangs.get(0).getVal());
		}
		if (action == CsMjDisAction.action_buzhang) {
			if (sameCount == 0) {
				majiangs.add(disMajiang);
			}
			majiangs = CsMjHelper.getMajiangList(player.getHandMajiang(), majiangs.get(0).getVal());
			sameCount = majiangs.size();
			if (sameCount == 0) {
				majiangs.add(disMajiang);
			}
		} else if (action == CsMjDisAction.action_minggang) {
			if (majiangs.size() == 0) {
				majiangs.add(disMajiang);
			}
			// ???????????? ????????????????????????????????????
			majiangs = CsMjHelper.getMajiangList(player.getHandMajiang(), majiangs.get(0).getVal());
			sameCount = majiangs.size();
			if (sameCount == 4) {
				// ???4????????????????????????
				action = CsMjDisAction.action_angang;
			} else if (sameCount == 0) {
				majiangs.add(disMajiang);
			}
			// ???????????????

		} else if (action == CsMjDisAction.action_buzhang_an) {
			// ????????????
			majiangs = CsMjHelper.getMajiangList(player.getHandMajiang(), majiangs.get(0).getVal());
			sameCount = majiangs.size();
		}
		// /////////////////////
		if (action == CsMjDisAction.action_chi) {
			boolean can = canChi(player, player.getHandMajiang(), majiangs, disMajiang);
			if (!can) {
				return;
			}
		} else if (action == CsMjDisAction.action_peng) {
			boolean can = canPeng(player, majiangs, sameCount, disMajiang);
			if (!can) {
				return;
			}
		} else if (action == CsMjDisAction.action_angang) {
			boolean can = canAnGang(player, majiangs, sameCount, action);
			if (!can) {
				player.writeErrMsg("??????????????????");
				return;
			}
			// ???????????????????????? ????????????&& isBuzhang
			if (getLeftMajiangCount() == 1) {
				player.writeErrMsg("???????????????");
				return;
			}
			if (!player.isTingPai(majiangs.get(0).getVal())) {
				player.writeErrMsg("??????????????????");
				return;
			}
		} else if (action == CsMjDisAction.action_minggang) {
			boolean can = canMingGang(player, player.getHandMajiang(), majiangs, sameCount, disMajiang);
			if (!can) {
				player.writeErrMsg("??????????????????");
				return;
			}

			// ???????????????????????? ????????????&& isBuzhang
			if (getLeftMajiangCount() == 1) {
				player.writeErrMsg("???????????????");
				return;
			}

			if (player.getGangCGangMjs().size() == 2 && disMajiang != null) {
				player.getGangCGangMjs().remove((Integer)disMajiang.getId());
				List<CsMj> list = new ArrayList<>();
				list.add(CsMj.getMajang(player.getGangCGangMjs().remove(0)));
				checkMoOutCard(list, player, action);
			}
			
			
			if (!player.isTingPai(majiangs.get(0).getVal())) {
				player.writeErrMsg("??????????????????");
				return;
			}
			
			// ???????????????????????????????????????????????? ???
			if ((sameCount == 1||jjHKqFg==1) && canGangHu()) {
				if (checkQGangHu(player, majiangs, action,sameCount)) {
					// return;
					moMj = false;
				}
			}
		} else if (action == CsMjDisAction.action_buzhang) {
			boolean can = false;
			if (sameCount == 4) {
				can = canAnGang(player, majiangs, sameCount, action);
			} else {
				can = canMingGang(player, player.getHandMajiang(), majiangs, sameCount, disMajiang);
			}
			// ???????????????????????? ????????????&& isBuzhang
			if (getLeftMajiangCount() == 1) {
				player.writeErrMsg("???????????????");
				return;
			}
			if (!can) {
				return;
			}
			// ???????????????????????????????????????????????????
			if (sameCount == 1 && canGangHu()) {
				if (checkQGangHu(player, majiangs, action,sameCount)) {
					// ????????????????????????
					// return;
					moMj = false;
				}
			}
		} else if (action == CsMjDisAction.action_buzhang_an) {
			boolean can = false;
			if (sameCount == 4) {
				can = canAnGang(player, majiangs, sameCount, action);
			}
			// ???????????????????????? ????????????&& isBuzhang
			if (getLeftMajiangCount() == 1 ) {
				player.writeErrMsg("???????????????");
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
			// ????????????
			if (action == CsMjDisAction.action_minggang && sameCount == 3) {
				// ??????
				disMajiangMove = true;
			} else if (action == CsMjDisAction.action_chi) {
				// ???
				disMajiangMove = true;
			} else if (action == CsMjDisAction.action_peng) {
				// ???
				disMajiangMove = true;
			} else if (action == CsMjDisAction.action_buzhang && sameCount == 3) {
				// ??????????????????
				disMajiangMove = true;
			}
		}
		if (disMajiangMove) {
			if (action == CsMjDisAction.action_chi) {
				majiangs.add(1, disMajiang);// ?????????????????????
			} else {
				majiangs.add(disMajiang);
			}
			builder.setFromSeat(disCardSeat);
			List<CsMj> disMajiangs = new ArrayList<>();
			disMajiangs.add(disMajiang);
			seatMap.get(disCardSeat).removeOutPais(disMajiangs, action);
		}
		chiPengGang(builder, player, majiangs, action, moMj);
	}

	private void chiPengGang(PlayMajiangRes.Builder builder, CsMjPlayer player, List<CsMj> majiangs, int action,
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

		addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + CsMjHelper.toMajiangStrs(majiangs));
		// ??????????????????
		setNowDisCardSeat(player.getSeat());
		checkClearGangDisMajiang();
		if (action == CsMjDisAction.action_chi || action == CsMjDisAction.action_peng) {
			List<Integer> arr = player.checkMo(null, false);
			// ????????????????????????
			if (!arr.isEmpty()) {
				arr.set(CsMjAction.ZIMO, 0);
				arr.set(CsMjAction.HU, 0);
				arr.set(CsMjAction.ZHONGTULIULIUSHUN, 0);
				arr.set(CsMjAction.ZHONGTUSIXI, 0);
				addActionSeat(player.getSeat(), arr);
			}
		}
		for (CsMjPlayer seatPlayer : seatMap.values()) {
			// ????????????
			PlayMajiangRes.Builder copy = builder.clone();
			if (actionSeatMap.containsKey(seatPlayer.getSeat())) {
				copy.addAllSelfAct(actionSeatMap.get(seatPlayer.getSeat()));
			}
			seatPlayer.writeSocket(copy.build());
		}

		// ????????????
		player.setPassMajiangVal(0);
		if (action == CsMjDisAction.action_minggang || action == CsMjDisAction.action_angang) {
			// ?????????????????????
			if (moMj) {
				gangMoMajiang(player, majiangs.get(0), action);
			}

		} else if (action == CsMjDisAction.action_buzhang) {
			// ??????
			if (moMj) {
				moMajiang(player, true);
			}

		} else if (action == CsMjDisAction.action_buzhang_an) {
			// ??????
			moMajiang(player, true);

		}

		if (action == CsMjDisAction.action_chi || action == CsMjDisAction.action_peng) {
			sendTingInfo(player);
		}

		setDisEventAction(action);
		robotDealAction();
		logAction(player, action, 0, majiangs, removeActList);
	}

	/**
	 * ??????????????????
	 */
	private void gangMoMajiang(CsMjPlayer player, CsMj gangMajiang, int action) {
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
		// ???????????????
		int moNum = 2;
		
		if(gangMoSi==1) {
			moNum=4;
		}
		List<CsMj> moList = new ArrayList<>();
		Random r = new Random();
		gangDice = (r.nextInt(6) + 1) * 10 + (r.nextInt(6) + 1);
		int leftMjCount = getLeftMajiangCount();
		int leftDuo = leftMjCount % 2 == 0 ? leftMjCount / 2 : leftMjCount / 2 + 1;
//		if (leftDuo >= gangDice / 10 + gangDice % 10) {
			if (GameServerConfig.isDeveloper()) {
				CsMj majiang1 = CsMjHelper.findMajiangByVal(leftMajiangs,25);
				if (majiang1 != null) {
					leftMajiangs.remove(majiang1);
					moList.add(majiang1);
				}
				CsMj majiang2 = CsMjHelper.findMajiangByVal(leftMajiangs, 22);
				if (majiang2 != null) {
					leftMajiangs.remove(majiang2);
					moList.add(majiang2);
				}
				if(gangMoSi==1) {
					CsMj majiang3 = CsMjHelper.findMajiangByVal(leftMajiangs,26);
					if (majiang3 != null) {
						leftMajiangs.remove(majiang3);
						moList.add(majiang3);
					}
					CsMj majiang4 = CsMjHelper.findMajiangByVal(leftMajiangs, 36);
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
				CsMj majiang = getLeftMajiang();
				if (majiang != null) {
					moList.add(majiang);
				} else {
					break;
				}
			}
			addMoTailPai(gangDice);
//		}

		addPlayLog(disCardRound + "_" + player.getSeat() + "_" + CsMjDisAction.action_moGangMjiang + "_" + gangDice
				+ "_" + CsMjHelper.implodeMajiang(moList, ","));

		// ????????????
		clearActionSeatMap();
		clearGangActionMap();
		// ??????????????????
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
        logGangMoMj(player,moList);
		boolean canHu = false;
		List<CsMj> moGangMj1s = new ArrayList<>();
		// ????????????????????????
		for (CsMj majiang : moList) {
			for (CsMjPlayer seatPlayer : seatMap.values()) {
				List<Integer> actionList = seatPlayer.checkDisMaj(majiang, false);
				if (seatPlayer.getSeat() == player.getSeat()) {
					// ??????????????????
					if (CsMjAction.hasHu(actionList)) {
						boolean addGang = false;
						if (CsMjAction.hasGang(actionList)) {
							addGang = true;
						}
						actionList = CsMjAction.keepHu(actionList);
						actionList.set(CsMjAction.HU, 0);
						actionList.set(CsMjAction.ZIMO, 1);
						if (addGang) {
							actionList.set(CsMjAction.MINGGANG, 1);
							actionList.set(CsMjAction.BUZHANG, 1);
							moGangMj1s.add(majiang);
							player.addGangcGangMj(majiang.getId());
							// seatPlayer.moMajiang(majiang);
						}
						canHu = true;
						addActionSeat(player.getSeat(), actionList);
						List<Integer> list2 = new ArrayList<Integer>(actionList);
						addGangActionSeat(majiang.getId(), player.getSeat(), list2);
						logAction(seatPlayer, action, -1, Arrays.asList(majiang), actionList);
					} else if (CsMjAction.hasGang(actionList)) {
						actionList = CsMjAction.keepHu(actionList);
						actionList.set(CsMjAction.MINGGANG, 1);
						actionList.set(CsMjAction.BUZHANG, 1);
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
				for (CsMj moMj : moList) {
					Map<Integer, List<Integer>> seatActionList = gangSeatMap.get(moMj.getId());
					if (seatActionList != null && seatActionList.containsKey(player.getSeat())) {
						continue;
					}
					List<CsMj> list = new ArrayList<>();
					list.add(moMj);
					checkMoOutCard(list, player, action);
				}
			} else {
				// ??????????????????
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
				for (CsMj moMj : moList) {
					GangPlayMajiangRes.Builder playerMsg = GangPlayMajiangRes.newBuilder();
					playerMsg.setMajiangId(moMj.getId());
					Map<Integer, List<Integer>> seatActionList = gangSeatMap.get(moMj.getId());

					if (seatActionList != null && seatActionList.containsKey(player.getSeat())) {
						playerMsg.addAllSelfAct(seatActionList.get(player.getSeat()));
					}
					gangMsg.addGangActs(playerMsg);
				}
				player.writeSocket(gangMsg.build());

				for (CsMjPlayer seatPlayer : seatMap.values()) {
					if (player.getSeat() != seatPlayer.getSeat()) {
						gangMsg.clearGangActs();
						seatPlayer.writeSocket(gangMsg.build());
						// ?????????????????????????????????????????????????????????
						removeActionSeat(seatPlayer.getSeat());
					}
				}
			}

		} else {
			// ??????????????????
			player.addOutPais(moList, 0, player.getSeat());

			addPlayLog(disCardRound + "_" + player.getSeat() + "_" + 0 + "_" + CsMjHelper.toMajiangStrs(moList));
			gangNoticePlayer(player, gangMajiang, moList);

			PlayMajiangRes.Builder chuPaiMsg = PlayMajiangRes.newBuilder();
			buildPlayRes(chuPaiMsg, player, CsMjDisAction.action_chupai, moList);
			for (CsMjPlayer seatPlayer : seatMap.values()) {
				chuPaiMsg.setFromSeat(-1);
				seatPlayer.writeSocket(chuPaiMsg.build());
			}
			broadMsgRoomPlayer(chuPaiMsg.build());

			sendTingInfo(player);
			if (isHasGangAction()) {
				// ????????????????????????
				robotDealAction();
			} else {
				checkMo();
			}
		}
	}

	private void checkMoOutCard(List<CsMj> list, CsMjPlayer player, int action) {

		player.addOutPais(list, 0, player.getSeat());
		addPlayLog(disCardRound + "_" + player.getSeat() + "_" + 0 + "_" + CsMjHelper.toMajiangStrs(list));
		logAction(player, action, 0, list, null);
		PlayMajiangRes.Builder chuPaiMsg = PlayMajiangRes.newBuilder();
		buildPlayRes(chuPaiMsg, player, CsMjDisAction.action_chupai, list);
		for (CsMjPlayer seatPlayer : seatMap.values()) {
			chuPaiMsg.setFromSeat(-1);
			seatPlayer.writeSocket(chuPaiMsg.build());
		}
	}

	private void gangNoticePlayer(CsMjPlayer player, CsMj gangMajiang, List<CsMj> moList) {
		// ??????????????????res
		GangMoMajiangRes.Builder gangMsg = null;
		for (CsMjPlayer seatPlayer : seatMap.values()) {
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
			for (CsMj majiang : moList) {
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

	private boolean checkQGangHu(CsMjPlayer player, List<CsMj> majiangs, int action,int sameCount) {
		PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
		Map<Integer, List<Integer>> huListMap = new HashMap<>();
		for (CsMjPlayer seatPlayer : seatMap.values()) {
			if (seatPlayer.getUserId() == player.getUserId()) {
				continue;
			}
			// ????????????
			List<Integer> hu = seatPlayer.checkDisMaj(majiangs.get(0), false);
			hu = CsMjAction.keepHu(hu);
			if (!hu.isEmpty() && hu.get(0) == 1) {
				//????????????????????????????????????????????????????????????
				if(getJiajianghu()==1){
					List<Integer> hu2 = seatPlayer.checkDisMaj(majiangs.get(0), true);
					if(action == CsMjDisAction.action_buzhang){
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

		// ????????????
		if (!huListMap.isEmpty()) {
			setDisEventAction(action);
			setMoGang(majiangs.get(0), new ArrayList<>(huListMap.keySet()));
			List<CsMj> al = new ArrayList<CsMj>();
			al.add(majiangs.get(0));
			buildPlayRes(builder, player, action,al);
			for (Entry<Integer, List<Integer>> entry : huListMap.entrySet()) {
				PlayMajiangRes.Builder copy = builder.clone();
				CsMjPlayer seatPlayer = seatMap.get(entry.getKey());
				copy.addAllSelfAct(entry.getValue());
				seatPlayer.writeSocket(copy.build());
			}
			return true;
		}
		return false;

	}

	public void checkSendGangRes(Player player) {
		if (isHasGangAction()) {
			List<CsMj> moList = getGangDisMajiangs();
			CsMjPlayer disPlayer = seatMap.get(disCardSeat);
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
			for (CsMj mj : moList) {
				GangPlayMajiangRes.Builder playBuilder = GangPlayMajiangRes.newBuilder();
				playBuilder.setMajiangId(mj.getId());
				Map<Integer, List<Integer>> seatActionList = gangSeatMap.get(mj.getId());
				if (seatActionList != null && seatActionList.containsKey(player.getSeat())) {
					playBuilder.addAllSelfAct(seatActionList.get(player.getSeat()));
				}
				gangbuilder.addGangActs(playBuilder);
			}
			if (isHasGangAction(disCardSeat) && player.getSeat() != disCardSeat) {
				// ???????????????????????????????????????????????????????????????
				gangbuilder.clearGangActs();
			}
			player.writeSocket(gangbuilder.build());
		}
	}

	/**
	 * ????????????
	 *
	 * @param player
	 * @param majiangs
	 * @param action
	 */
	private void chuPai(CsMjPlayer player, List<CsMj> majiangs, int action) {
		if (majiangs.size() != 1) {
			return;
		}
		if (!player.isAlreadyMoMajiang()) {
			// ???????????????
			return;
		}
		if (!tempActionMap.isEmpty() && player.getGang().isEmpty()) {
			LogUtil.e(player.getName() + "???????????????????????????");
			clearTempAction();
		}
		if (!player.getGang().isEmpty()) {
			// ??????????????????
			if (player.getLastMoMajiang().getId() != majiangs.get(0).getId()) {
				return;
			}
		}
		if (!actionSeatMap.isEmpty() && player.getGang().isEmpty()) {// ??????????????????????????????
			guo(player, null, CsMjDisAction.action_pass);
		}
		if (!actionSeatMap.isEmpty() && player.getGang().isEmpty()) {
			player.writeComMessage(WebSocketMsgType.res_code_mj_dis_err, action, majiangs.get(0).getId());
			return;
		}
		player.setDisCount(player.getDisCount()+1);
		PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
		buildPlayRes(builder, player, action, majiangs);
		// ????????????
		clearActionSeatMap();
		clearGangActionMap();
		setNowDisCardSeat(calcNextSeat(player.getSeat()));
		recordDisMajiang(majiangs, player);
		player.addOutPais(majiangs, action, player.getSeat());
		player.clearPassHu();
		logAction(player, action, 0, majiangs, null);
		for (CsMjPlayer seat : seatMap.values()) {
			List<Integer> list = new ArrayList<>();
			if (seat.getUserId() != player.getUserId()) {
				list = seat.checkDisMajiang(majiangs.get(0));
				if (list.contains(1)) {
					// ??????????????????????????????????????????????????????
					if (!seat.getGang().isEmpty()) {
						list.set(CsMjAction.MINGGANG, 0);
						list.set(CsMjAction.ANGANG, 0);
						list.set(CsMjAction.BUZHANG, 0);
					}

					addActionSeat(seat.getSeat(), list);
					logChuPaiActList(seat, majiangs.get(0), list);
				}
			}
		}
		
		setDisEventAction(action);
		sendDisMajiangAction(builder);
		// ????????????
		player.setPassMajiangVal(0);
		addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + CsMjHelper.toMajiangStrs(majiangs));
		setIsBegin(false);
		// ??????????????????
		checkMo();
	}

	public List<Integer> getPengGangSeatByActionMap() {
		List<Integer> huList = new ArrayList<>();
		for (int seat : actionSeatMap.keySet()) {
			List<Integer> actionList = actionSeatMap.get(seat);
			if (actionList.get(0) == 3) {
				// ???
				huList.add(seat);
			}

		}
		return huList;
	}

	public List<Integer> getHuSeatByActionMap() {
		List<Integer> huList = new ArrayList<>();
		for (int seat : actionSeatMap.keySet()) {
			List<Integer> actionList = actionSeatMap.get(seat);
			if (actionList.get(CsMjAction.HU) == 1 || actionList.get(CsMjAction.ZIMO) == 1) {
				// ???
				huList.add(seat);
			}

		}
		return huList;
	}

	private void sendDisMajiangAction(PlayMajiangRes.Builder builder) {
		// ????????????????????? ?????????
		// ??????????????????
		buildPlayRes1(builder);
		List<Integer> huList = getHuSeatByActionMap();
		if (huList.size() > 0) {
			// ?????????,?????????
			for (CsMjPlayer seatPlayer : seatMap.values()) {
				PlayMajiangRes.Builder copy = builder.clone();
				List<Integer> actionList;
				// ???????????????????????????????????????????????????????????????????????????????????????
				if (actionSeatMap.containsKey(seatPlayer.getSeat())) {
					// if (huList.contains(seatPlayer.getSeat())) {
					actionList = actionSeatMap.get(seatPlayer.getSeat());
				} else {
					// ?????????????????????
					actionList = new ArrayList<>();
				}
				copy.addAllSelfAct(actionList);
				seatPlayer.writeSocket(copy.build());
			}

		} else {
			// ??????????????????????????????
			for (CsMjPlayer seat : seatMap.values()) {
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

	private void err(CsMjPlayer player, int action, String errMsg) {
		LogUtil.e("play:tableId-->" + id + " playerId-->" + player.getUserId() + " action-->" + action + " err:"
				+ errMsg);
	}

	/**
	 * ??????
	 *
	 * @param player
	 * @param majiangs
	 * @param action
	 */
	public synchronized void playCommand(CsMjPlayer player, List<CsMj> majiangs, int action) {
		if (!moGangHuList.isEmpty()) {// ???????????????
			if (!moGangHuList.contains(player.getSeat())) {
				return;
			}
		}

		if (CsMjDisAction.action_hu == action) {
			hu(player, majiangs, action);
			return;
		}
		// ???????????????????????????
		if (!isHasGangAction() && action != CsMjDisAction.action_minggang && action != CsMjDisAction.action_buzhang)
			if (!player.getHandMajiang().containsAll(majiangs)) {
				err(player, action, "?????????????????????" + majiangs);
				return;
			}
//		if(action != CsMjDisAction.action_pass){
			changeDisCardRound(1);
//		}
		if (action == CsMjDisAction.action_pass) {
			guo(player, majiangs, action);
		} else if (action == CsMjDisAction.action_moMjiang) {
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
		}
	}

	/**
	 * ???????????????(?????????)
	 *
	 * @param player
	 * @param action
	 */
	public synchronized void moLastMajiang(CsMjPlayer player, int action) {
		if (getLeftMajiangCount() != 1) {
			return;
		}
		if (player.getSeat() != askLastMajaingSeat) {
			return;
		}

		if (action == CsMjDisAction.action_passmo) {
			// ???????????????????????????res
			sendMoLast(player, 0);
			removeMoLastSeat(player.getSeat());
			if (moLastSeats == null || moLastSeats.size() == 0) {
				calcOver();
				return;
			}
			sendAskLastMajiangRes(0);
			addPlayLog(disCardRound + "_" + player.getSeat() + "_" + CsMjDisAction.action_pass + "_");
		} else {
			sendMoLast(player, 0);
			clearMoLastSeat();
			clearActionSeatMap();
			setMoLastMajiangSeat(player.getSeat());
			CsMj majiang = getLeftMajiang();
			addPlayLog(disCardRound + "_" + player.getSeat() + "_" + CsMjDisAction.action_moLastMjiang + "_"
					+ majiang.getId());
			setMoMajiangSeat(player.getSeat());
			player.setPassMajiangVal(0);
			setLastMajiang(majiang);
			setDisCardSeat(player.getSeat());

			// /////////////////////////////////////////////
			// ?????????????????????

			// /////////////////////////////////////////

			List<CsMj> disMajiangs = new ArrayList<>();
			disMajiangs.add(majiang);

			MoMajiangRes.Builder moRes = MoMajiangRes.newBuilder();
			moRes.setUserId(player.getUserId() + "");
			moRes.setRemain(getLeftMajiangCount());
			moRes.setSeat(player.getSeat());

			// ???????????????????????????
			List<Integer> selfActList = player.checkDisMajiang(majiang);
			player.moMajiang(majiang);
			selfActList = CsMjAction.keepHu(selfActList);
			if (selfActList != null && !selfActList.isEmpty()) {
				if (selfActList.contains(1)) {
					addActionSeat(player.getSeat(), selfActList);
				}
			}
			for (CsMjPlayer seatPlayer : seatMap.values()) {
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

			// ????????????
			if (CsMjAction.hasHu(selfActList)) {
				// ???????????????
				// hu(player, null, CsMjDisAction.action_moLastMjiang_hu);
				return;
			} else {
				chuLastPai(player);
			}
			// for (int seat : actionSeatMap.keySet()) {
			// hu(seatMap.get(seat), null, action);
			// }
		}

	}

	private void chuLastPai(CsMjPlayer player) {
		CsMj majiang = lastMajiang;
		List<CsMj> disMajiangs = new ArrayList<>();
		disMajiangs.add(majiang);
		PlayMajiangRes.Builder chuRes = CsMjResTool.buildPlayRes(player, CsMjDisAction.action_chupai, disMajiangs);
		addPlayLog(disCardRound + "_" + player.getSeat() + "_" + CsMjDisAction.action_chupai + "_"
				+ CsMjHelper.toMajiangStrs(disMajiangs));
		setNowDisCardIds(disMajiangs);
		setNowDisCardSeat(calcNextSeat(player.getSeat()));
		recordDisMajiang(disMajiangs, player);
		player.addOutPais(disMajiangs, CsMjDisAction.action_chupai, player.getSeat());
		player.clearPassHu();
		for (CsMjPlayer seatPlayer : seatMap.values()) {
			if (seatPlayer.getUserId() == player.getUserId()) {
				seatPlayer.writeSocket(chuRes.clone().build());
				continue;
			}
			List<Integer> otherActList = seatPlayer.checkDisMajiang(majiang);
			otherActList = CsMjAction.keepHu(otherActList);
			PlayMajiangRes.Builder msg = chuRes.clone();
			if (CsMjAction.hasHu(otherActList)) {
				addActionSeat(seatPlayer.getSeat(), otherActList);
				msg.addAllSelfAct(otherActList);
			}
			seatPlayer.writeSocket(msg.build());
		}
		if (actionSeatMap.isEmpty()) {
			calcOver();
		}
	}

	private void passMoHu(CsMjPlayer player, List<CsMj> majiangs, int action) {
		if (!moGangHuList.contains(player.getSeat())) {
			return;
		}

		PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
		buildPlayRes(builder, player, action, majiangs);
		builder.setSeat(nowDisCardSeat);
		removeActionSeat(player.getSeat());
		player.writeSocket(builder.build());
		addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + CsMjHelper.toMajiangStrs(majiangs));
		if (isCalcOver()) {
			calcOver();
			return;
		}
		player.setPassMajiangVal(nowDisCardIds.get(0).getVal());

		CsMjPlayer moGangPlayer = seatMap.get(moMajiangSeat);
		if (moGangHuList.isEmpty()) {
			majiangs = new ArrayList<>();
			majiangs.add(moGang);
			if (disEventAction == CsMjDisAction.action_buzhang) {
				moMajiang(moGangPlayer, true);
			} else {
				gangMoMajiang(moGangPlayer, majiangs.get(0), disEventAction);
			}

			// calcPoint(moGangPlayer, CsMjDisAction.action_minggang, 1,
			// majiangs);
			// builder = PlayMajiangRes.newBuilder();
			// chiPengGang(builder, moGangPlayer, majiangs,
			// CsMjDisAction.action_minggang,true);
		}

	}

	/**
	 * guo
	 *
	 * @param player
	 * @param majiangs
	 * @param action
	 */
	private void guo(CsMjPlayer player, List<CsMj> majiangs, int action) {
		if (!actionSeatMap.containsKey(player.getSeat())) {
			return;
		}
		if (!moGangHuList.isEmpty()) {
			// ???????????????????????????
			passMoHu(player, majiangs, action);
			return;
		}
		  //???????????????
        if (player.getLastPassTime() > 0 && System.currentTimeMillis() - player.getLastPassTime() <= 1500) {
        	logPassTime(player);
            return;
        }
        //?????????
        player.setLastPassTime(System.currentTimeMillis());
		
		
		List<Integer> removeActionList = removeActionSeat(player.getSeat());
		int xiaoHu = CsMjAction.getFirstXiaoHu(removeActionList);
		logAction(player, action, xiaoHu, majiangs, removeActionList);
		boolean isBegin = isBegin();
		if (xiaoHu != -1) {
			player.addPassXiaoHu(xiaoHu);
			player.addPassXiaoHuList2(xiaoHu);
			List<Integer> actionList = player.checkMo(null, isBegin);
			if (!actionList.isEmpty()) {
				actionList.set(xiaoHu, 0);
				if (CsMjAction.getFirstXiaoHu(actionList) != -1) {
					// ????????????????????????????????????????????????
					addActionSeat(player.getSeat(), actionList);
					PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
					buildPlayRes(builder, player, action, majiangs);
					builder.setSeat(nowDisCardSeat);
					builder.addAllSelfAct(actionList);
					player.writeSocket(builder.build());
					logAction(player, action, xiaoHu, majiangs, actionList);
					addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_"
							+ CsMjHelper.toMajiangStrs(majiangs));
					return;
				} else {
					addActionSeat(player.getSeat(), actionList);
				}
			}
		}

		if (moLastMajiangSeat == player.getSeat()) {
			// ???????????????????????????????????????????????????
			chuLastPai(player);
			return;
		}
		checkClearGangDisMajiang();
		PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
		buildPlayRes(builder, player, action, majiangs);
		builder.setSeat(nowDisCardSeat);
		player.writeSocket(builder.build());
		addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + CsMjHelper.toMajiangStrs(majiangs));
		if (isCalcOver()) {
			calcOver();
			return;
		}
		if (CsMjAction.hasHu(removeActionList) && disCardSeat != player.getSeat() && nowDisCardIds.size() == 1) {
			// ??????
			player.passHu(nowDisCardIds.get(0).getVal());
		}

		// nowDisCardIds.size() == 1
		if (removeActionList.get(0) == 1 && disCardSeat != player.getSeat()) {
			if (nowDisCardIds.size() > 1) {
				for (CsMj mj : nowDisCardIds) {
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
			CsMjPlayer disCSMajiangPlayer = seatMap.get(disCardSeat);
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
				CsMjPlayer seatPlayer = seatMap.get(seat);
				seatPlayer.writeSocket(copy.build());
			}
		}
		// && tempActionMap.size()==0

		if ((player.isAlreadyMoMajiang()||player.getGangCGangMjs().size()==2) && !player.getGang().isEmpty() && actionSeatMap.get(player.getSeat()) == null) {
			// ?????????????????????
			List<CsMj> disMjiang = new ArrayList<>();
			//disMjiang.add(player.getLastMoMajiang());
			if (player.getGangCGangMjs().size()>0 ) {
				disMjiang.add(CsMj.getMajang(player.getGangCGangMjs().remove(0)));
//				player.getGangCGangMjs().remove(0);
//				List<CsMj> list = new ArrayList<>();
				if (player.getGangCGangMjs().size()>0 ) {
					disMjiang.add(CsMj.getMajang(player.getGangCGangMjs().remove(0)));
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
			// ???????????????????????????????????????
			ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.req_com_kt_ask_dismajiang);
			player.writeSocket(com.build());
		} else {
			checkBegin(player);
		}

		if (player.isAlreadyMoMajiang()) {
			sendTingInfo(player);
		}

		// ?????? ???????????????????????????????????????????????????????????????
		refreshTempAction(player);
		checkMo();
	}


	@Override
	public synchronized void checkRobotPlay() {
		// ????????????
		if (state == table_state.ready) {
			for (CsMjPlayer player : seatMap.values()) {
				if (player.isRobot()) {
					player.calcRobotActionRND(4, 4);
					player.addRobotActionCounter();
					if (player.canRobotAction()) {
						autoReady(player);
						player.resetRobotActionCounter();
					}
				}
			}
			return;
		}

		if (state != table_state.play) {
			return;
		}
		CsMjPlayer player = seatMap.get(getNextDisCardSeat());
		if (player == null) {
			return;
		}

		if (!player.isRobot()) {
			return;
		}

		//?????????????????????
		player.addRobotActionCounter();
		if (!player.canRobotAction()) {
			return;
		}


		//???????????????
		long start = System.currentTimeMillis();
		IBTExecutor robotAI = RobotManager.generateRobotAI(playType, player.getRobotAILevel());

		StringBuilder sb = new StringBuilder("CsMj|robot");
		sb.append("|").append(getId());
		sb.append("|").append(getPlayBureau());
		sb.append("|").append(player.getUserId());
		sb.append("|").append(player.getSeat());
		sb.append("|").append(player.isAutoPlay() ? 1 : 0);

		robotAI.getRootContext().clear();

		// ?????????????????????
		HashMap<String, Object> map = CsmjMidRobotActionResult.RoBotAIBehavior(robotAI, actionSeatMap, player, getOnlyDaHu(), getQuanqiurJiang(), leftMajiangs, this);

		if (player.isAutoPlay()) {
			//????????? ??????
			if (getNowDisCardSeat() == player.getSeat()) {
				List<Integer> actmap = actionSeatMap.get(player.getSeat());
				if (null != actmap && actmap.contains(1)) {
					playCommand(player, new ArrayList<CsMj>(), CsMjDisAction.action_pass);
					player.setAutoPlay(false, false);
					sb.append("|death deal|action_pass");
				} else {
					List a = new ArrayList<CsMj>();
					a.add(player.getLastMoMajiang());
					playCommand(player, a, CsMjDisAction.action_chupai);
					sb.append("|death deal|action_chupai");
				}
			}
			map = null;
			player.resetRobotActionCounter();
			player.calcRobotActionRND(1, 1);

		}

		if (null != map) {
			String lastmap = player.getRobotLastActionMap();
			if (lastmap.equals(map.toString())) {
				//????????????????????????
				player.setRobotLastActionRepeatCount(player.getRobotLastActionRepeatCount() + 1);
				if (player.getRobotLastActionRepeatCount() > 4) {
					player.setAutoPlay(true, true);
				}
			} else {
				player.setRobotLastActionMap(map.toString());
				player.setRobotLastActionRepeatCount(0);
			}
			sb.append("|").append(map.toString());
			setLastActionTime(TimeUtil.currentTimeMillis());
			player.resetRobotActionCounter();
			player.calcRobotActionRND(1, 1);
		}

		long timeUse = System.currentTimeMillis() - start;
		if (timeUse >= 0) {
			sb.append("|").append(timeUse);
		}
		LogUtil.robot.info(sb.toString());
		player.setAutoPlay(false, false);
		player.setCheckAutoPlay(false);


//		if(!isCalcOver()){
//			int nextSeat = calcNextSeat(player.getSeat());
//			CsMjPlayer nextPlayer = seatMap.get(nextSeat);
//			if (!nextPlayer.isRobot()) {
//				nextPlayer.setAutoPlayCheckedTime(TimeUtil.currentTimeMillis() + autoTimeOut);
//			}
//		}

	}

	private void logPassTime(CsMjPlayer player) {
		StringBuilder sb = new StringBuilder();
		sb.append("CsMj");
		sb.append("|").append(getId());
		sb.append("|").append(getPlayBureau());
		sb.append("|").append(player.getUserId());
		sb.append("|").append(player.getSeat());
		sb.append("|").append("pass");
		sb.append("|").append(System.currentTimeMillis() - player.getLastPassTime());
		sb.append("|").append(actionSeatMap.get(player.getSeat()));
		LogUtil.msg(sb.toString());
	}

	private void calcPoint(CsMjPlayer player, int action, int sameCount, List<CsMj> majiangs) {
		if(gangBuF!=1) {
			return;
		}
		int lostPoint = 0;
		int getPoint = 0;
		if (action == CsMjDisAction.action_peng) {
			List<Integer> actionList = actionSeatMap.get(player.getSeat());
			if (actionList.get(2) == 1 ||actionList.get(5) == 1 ) {
				// ?????????????????????
				player.addPassGangVal(majiangs.get(0).getVal());
			}
			return;

		} else if (action == CsMjDisAction.action_angang||action == CsMjDisAction.action_buzhang_an) {
			// ??????????????????????????????2???
			lostPoint = -2;
			 getPoint = 2 * (getMaxPlayerCount() - 1);

		} else if (action == CsMjDisAction.action_minggang||action == CsMjDisAction.action_buzhang) {
			if (sameCount == 1) {
				// ????????????????????????????????????1???
				// ???????????????3???

				if (player.isPassGang(majiangs.get(0))) {
					// ???????????? ???????????????????????? ???????????? ???????????????
					return;
				}
				lostPoint = -1;
				 getPoint = 1 * (getMaxPlayerCount() - 1);
			}
			// ??????
			else if (sameCount == 3) {
				CsMjPlayer disPlayer = seatMap.get(disCardSeat);
				
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
			for (CsMjPlayer seat : seatMap.values()) {
				if (seat.getUserId() == player.getUserId()) {
					player.changeGangPoint(getPoint);
				} else {
					seat.changeGangPoint(lostPoint);
				}
			}
		}
		 
	}

	private void calcXiaoHuPoint(CsMjPlayer player, int xiaoIndex) {
//		int count = player.getXiaoHuCount(xiaoIndex);
//		int lostPoint = -2 * count;
//		int getPoint = 6 * count;
//		if (lostPoint != 0) {
//			for (CsMjPlayer seat : seatMap.values()) {
//				if (seat.getUserId() == player.getUserId()) {
//					seat.changeGangPoint(getPoint);
//				} else {
//					seat.changeGangPoint(lostPoint);
//				}
//			}
//		}
	}

	private void recordDisMajiang(List<CsMj> majiangs, CsMjPlayer player) {
		setNowDisCardIds(majiangs);
		setDisCardSeat(player.getSeat());
	}

	public List<CsMj> getNowDisCardIds() {
		return nowDisCardIds;
	}

	public void setDisEventAction(int disAction) {
		this.disEventAction = disAction;
		changeExtend();
	}

	public void setNowDisCardIds(List<CsMj> nowDisCardIds) {
		if (nowDisCardIds == null) {
			this.nowDisCardIds.clear();

		} else {
			this.nowDisCardIds = nowDisCardIds;

		}
		dbParamMap.put("nowDisCardIds", JSON_TAG);
	}

	/**
	 * ????????????
	 */
	public void checkMo() {
		if (actionSeatMap.isEmpty()) {
			if (nowDisCardSeat != 0) {
				moMajiang(seatMap.get(nowDisCardSeat), false);

			}
			robotDealAction();

		} else {
//			for (int seat : actionSeatMap.keySet()) {
//				CsMjPlayer player = seatMap.get(seat);
//				if (player != null && player.isRobot()) {
//					// ????????????????????????????????????
//					List<Integer> actionList = actionSeatMap.get(seat);
//					if (actionList == null) {
//						continue;
//					}
//					List<CsMj> list = new ArrayList<>();
//					if (!nowDisCardIds.isEmpty()) {
//						list = CsMjQipaiTool.getVal(player.getHandMajiang(), nowDisCardIds.get(0).getVal());
//					}
//					if (actionList.get(0) == 1) {
//						// ???
//						playCommand(player, new ArrayList<CsMj>(), CsMjDisAction.action_hu);
//
//					} else if (actionList.get(3) == 1) {
//						playCommand(player, list, CsMjDisAction.action_angang);
//
//					} else if (actionList.get(2) == 1) {
//						playCommand(player, list, CsMjDisAction.action_minggang);
//
//					} else if (actionList.get(1) == 1) {
//						playCommand(player, list, CsMjDisAction.action_peng);
//
//					} else if (actionList.get(4) == 1) {
//						playCommand(player, player.getCanChiMajiangs(nowDisCardIds.get(0)), CsMjDisAction.action_chi);
//
//					} else {
//						System.out.println("---------->" + JacksonUtil.writeValueAsString(actionList));
//					}
//				}
//				// else {
//				// // ???????????????????????????
//				// player.writeSocket(builder.build());
//				// }
//
//			}

		}
	}

	@Override
	protected void robotDealAction() {
		if (isTest()) {
			// for (CsMjPlayer player : seatMap.values()) {
			// if (player.isRobot() && player.canXiaoHu()) {
			// playCommand(player, new ArrayList<CsMj>(),
			// CsMjDisAction.action_xiaohu);
			// }
			// }

			int nextseat = getNextActionSeat();
			CsMjPlayer next = seatMap.get(nextseat);
			if (next != null && next.isRobot()) {
				List<Integer> actionList = actionSeatMap.get(next.getSeat());
				int xiaoHuAction = -1;
				if (actionList != null) {
					List<CsMj> list = null;
					if (actionList.get(0) == 1) {
						// ???
						playCommand(next, new ArrayList<CsMj>(), CsMjDisAction.action_hu);

					} else if ((xiaoHuAction = CsMjAction.getFirstXiaoHu(actionList)) > 0) {

						playCommand(next, new ArrayList<CsMj>(), CsMjDisAction.action_pass);

					} else if (actionList.get(3) == 1) {
						// ???????????????
						Map<Integer, Integer> handMap = CsMjHelper.toMajiangValMap(next.getHandMajiang());
						for (Entry<Integer, Integer> entry : handMap.entrySet()) {
							if (entry.getValue() == 4) {
								// ????????????
								list = CsMjHelper.getMajiangList(next.getHandMajiang(), entry.getKey());
							}
						}
						playCommand(next, list, CsMjDisAction.action_angang);

					} else if (actionList.get(5) == 1) {
						// ???????????????
						Map<Integer, Integer> handMap = CsMjHelper.toMajiangValMap(next.getHandMajiang());
						for (Entry<Integer, Integer> entry : handMap.entrySet()) {
							if (entry.getValue() == 4) {
								// ????????????
								list = CsMjHelper.getMajiangList(next.getHandMajiang(), entry.getKey());
							}
						}
						if (list == null) {
							if (next.isAlreadyMoMajiang()) {
								list = CsMjQipaiTool.getVal(next.getHandMajiang(), next.getLastMoMajiang().getVal());

							} else {
								list = CsMjQipaiTool.getVal(next.getHandMajiang(), nowDisCardIds.get(0).getVal());
								list.add(nowDisCardIds.get(0));
							}
						}

						playCommand(next, list, CsMjDisAction.action_buzhang);

					} else if (actionList.get(2) == 1) {
						Map<Integer, Integer> pengMap = CsMjHelper.toMajiangValMap(next.getPeng());
						for (CsMj handMajiang : next.getHandMajiang()) {
							if (pengMap.containsKey(handMajiang.getVal())) {
								// ?????????
								list = new ArrayList<>();
								list.add(handMajiang);
								playCommand(next, list, CsMjDisAction.action_minggang);
								break;
							}
						}

					} else if (actionList.get(1) == 1) {
						// playCommand(next, list, CsMjDisAction.action_peng);

					} else if (actionList.get(4) == 1) {
						CsMj majiang = null;
						List<CsMj> chiList = null;
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
										majiang = CsMj.getMajang(majiangId);
										chiList = next.getCanChiMajiangs(majiang);
										chiList.add(majiang);
										break;
									}

								}

							}

						}

						playCommand(next, chiList, CsMjDisAction.action_chi);

					} else {
						System.out.println("!!!!!!!!!!" + JacksonUtil.writeValueAsString(actionList));

					}

				} else {
					int maJiangId = CsMjRobotAI.getInstance().outPaiHandle(0, next.getHandPais(),
							new ArrayList<Integer>());
					List<CsMj> majiangList = CsMjHelper.toMajiang(Arrays.asList(maJiangId));
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
			// ??????????????????
			int masterseat = playerMap.get(masterId).getSeat();
			setLastWinSeat(masterseat);
		}
		setDisCardSeat(lastWinSeat);
		setNowDisCardSeat(lastWinSeat);
		setMoMajiangSeat(lastWinSeat);

		List<Integer> copy = null;
		if (maxPlayerCount == 2 && queYiMen == 1) {
			copy = new ArrayList<>();
			for (Integer id : CsMjConstants.zhuanzhuan_mjList) {
				CsMj mj = CsMj.getMajang(id);
				if (mj.getColourVal() == 1) {
					continue;
				}
				copy.add(id);
			}
		} else {
			copy = new ArrayList<>(CsMjConstants.zhuanzhuan_mjList);
		}

		// List<Integer> copy = new
		// ArrayList<>(CsMjConstants.zhuanzhuan_mjList);
		addPlayLog(copy.size() + "");
		List<List<CsMj>> list;
		System.out.println(zp);
		if (zp == null) {
			list = CsMjTool.fapai(copy, getMaxPlayerCount());
		} else {
			list = CsMjTool.fapai(copy, getMaxPlayerCount(), zp);
		}
		int i = 1;
		List<Integer> removeIndex = new ArrayList<>();
		for (CsMjPlayer player : playerMap.values()) {
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

		// ??????????????????
		List<CsMj> leftMjs = new ArrayList<>();
		// ???????????????????????????????????????
		for (int j = 0; j < list.size(); j++) {
			
			if (!removeIndex.contains(j)) {
				leftMjs.addAll(list.get(j));
			}
		}
		setLeftMajiangs(leftMjs);
	}

	/**
	 * ???????????????????????????
	 *
	 * @param leftMajiangs
	 */
	public void setLeftMajiangs(List<CsMj> leftMajiangs) {
		if (leftMajiangs == null) {
			this.leftMajiangs.clear();
		} else {
			this.leftMajiangs = leftMajiangs;

		}
		dbParamMap.put("leftPais", JSON_TAG);
	}

	/**
	 * ?????????????????????
	 *
	 * @return
	 */
	public CsMj getLeftMajiang() {
		if (this.leftMajiangs.size() > 0) {
			CsMj majiang = this.leftMajiangs.remove(0);
			dbParamMap.put("leftPais", JSON_TAG);
			return majiang;
		}
		return null;
	}

	/**
	 * ?????????????????????
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
	 * ??????????????????????????????????????????????????????
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
				if (entry.getValue().get(0) == 1) {// ???
					return entry.getKey();
				}
				if (entry.getValue().get(2) == 1) {// ???
					return entry.getKey();
				}
				if (entry.getValue().get(1) == 1) {// ???
					return entry.getKey();
				}
				if (entry.getValue().get(4) == 1) {// ???
					return entry.getKey();
				}
			}
			return seat;
		}
	}

	//
	// private int getNearSeat(int nowSeat, List<Integer> seatList) {
	// if (seatList.contains(nowSeat)) {
	// // ???????????????????????????
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
	 * ??????seat???????????????
	 *
	 * @param seat
	 * @return
	 */
	public int calcNextSeat(int seat) {
		return seat + 1 > maxPlayerCount ? 1 : seat + 1;
	}

	@Override
	public CsMjPlayer getPlayerBySeat(int seat) {
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
        res.setTableId(getId()+"");
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
		for (CsMjPlayer player : playerMap.values()) {
			PlayerInTableRes.Builder playerRes = player.buildPlayInTableInfo(isrecover);
			if (player.getUserId() == userId) {
				playerRes.addAllHandCardIds(player.getHandPais());
			}

			if (showMjSeat.contains(player.getSeat()) && player.getHuXiaohu().size() > 0) {
				List<Integer> ids = CsMjHelper.toMajiangIds(player.showXiaoHuMajiangs(player.getHuXiaohu().get(player.getHuXiaohu().size() - 1), true));
				if (ids != null) {
					if (player.getUserId() == userId) {
						playerRes.addAllIntExts(ids);
					} else {
						playerRes.addAllHandCardIds(ids);
					}

				}
			}
			if (player.getSeat() == disCardSeat && nowDisCardIds != null) {
				playerRes.addAllOutCardIds(CsMjHelper.toMajiangIds(nowDisCardIds));
			}
			playerRes.addRecover(player.getSeat() == lastWinSeat ? 1 : 0);
			if (!isHasGangAction(player.getSeat()) && actionSeatMap.containsKey(player.getSeat())
					&& !huConfirmMap.containsKey(player.getSeat())) {
				if (!tempActionMap.containsKey(player.getSeat())) {// ????????????????????????
																	// ?????????????????????????????????
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

	private Map<Integer, CsMjTempAction> loadTempActionMap(String json) {
		Map<Integer, CsMjTempAction> map = new ConcurrentHashMap<>();
		if (json == null || json.isEmpty())
			return map;
		JSONArray jsonArray = JSONArray.parseArray(json);
		for (Object val : jsonArray) {
			String str = val.toString();
			CsMjTempAction tempAction = new CsMjTempAction();
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
		addPlayLog(disCardRound + "_" + seat + "_" + CsMjDisAction.action_hasAction + "_"
				+ StringUtil.implode(actionlist));
		saveActionSeatMap();
	}

	public void addActionSeat(int seat, List<Integer> actionlist) {
		// ????????????????????????
		if (!actionlist.contains(1)) {
			return;
		}
		if (actionSeatMap.containsKey(seat)) {
			List<Integer> a = actionSeatMap.get(seat);
			DataMapUtil.appendList(a, actionlist);
			addPlayLog(disCardRound + "_" + seat + "_" + CsMjDisAction.action_hasAction + "_" + StringUtil.implode(a));
		} else {
			actionSeatMap.put(seat, actionlist);
			addPlayLog(disCardRound + "_" + seat + "_" + CsMjDisAction.action_hasAction + "_"
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
	 * ????????????????????????
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
			nowDisCardIds = CsMjHelper.toMajiang(StringUtil.explodeToIntList(info.getNowDisCardIds()));
		}

		if (!StringUtils.isBlank(info.getLeftPais())) {
			leftMajiangs = CsMjHelper.toMajiang(StringUtil.explodeToIntList(info.getLeftPais()));
		}

	}

	// @Override
	// public void initExtend(String info) {
	// if (StringUtils.isBlank(info)) {
	// return;
	// }
	// JsonWrapper wrapper = new JsonWrapper(info);
	// for (CsMjPlayer player : seatMap.values()) {
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
	 * ????????????
	 *
	 * @param player
	 * @param majiangs
	 * @param disMajiang
	 * @return
	 */
	private boolean canChi(CsMjPlayer player, List<CsMj> handMajiang, List<CsMj> majiangs, CsMj disMajiang) {
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

		List<CsMj> chi = CsMjTool.checkChi(majiangs, disMajiang);
		return !chi.isEmpty();
	}

	/**
	 * ????????????
	 *
	 * @param player
	 * @param majiangs
	 * @param sameCount
	 * @return
	 */
	private boolean canPeng(CsMjPlayer player, List<CsMj> majiangs, int sameCount, CsMj disMajiang) {
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
	 * ???????????????
	 *
	 * @param player
	 * @param majiangs
	 * @param sameCount
	 * @return
	 */
	private boolean canAnGang(CsMjPlayer player, List<CsMj> majiangs, int sameCount, int action) {
		if (sameCount != 4) {
			return false;
		}
		if (player.getSeat() != getNextDisCardSeat() && action != CsMjDisAction.action_buzhang) {
			return false;
		}
		if (player.getSeat() != getNextDisCardSeat() && action != CsMjDisAction.action_buzhang_an) {
			return false;
		}
		return true;
	}

	/**
	 * ??????????????? ????????????????????????????????????????????????????????????
	 */
	private boolean checkAction(CsMjPlayer player, List<CsMj> cardList, List<Integer> hucards, int action) {
		boolean canAction = checkCanAction(player, action);// ????????????????????? ???????????????
		if (canAction == false) {// ??????????????? ??????????????????
			int seat = player.getSeat();
			tempActionMap.put(seat, new CsMjTempAction(seat, action, cardList, hucards));
			// ?????????????????????????????????????????? ?????????????????????
			if (tempActionMap.size() == actionSeatMap.size()) {
				int maxAction = Integer.MAX_VALUE;
				int maxSeat = 0;
				Map<Integer, Integer> prioritySeats = new HashMap<>();
				int maxActionSize = 0;
				for (CsMjTempAction temp : tempActionMap.values()) {
					int prioAction = CsMjDisAction.getPriorityAction(temp.getAction());
					int prioAction2 = CsMjDisAction.getPriorityAction(maxAction);
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
				CsMjPlayer tempPlayer = seatMap.get(maxSeat);
				List<CsMj> tempCardList = tempActionMap.get(maxSeat).getCardList();
				for (int removeSeat : prioritySeats.keySet()) {
					if (removeSeat != maxSeat) {
						removeActionSeat(removeSeat);
					}
				}
				clearTempAction();
				playCommand(tempPlayer, tempCardList, maxAction);// ?????????????????????????????????
			} else {
				if (isCalcOver()) {// ??????????????????????????????
					calcOver();
					return canAction;
				}
			}
		} else {// ????????? ????????????????????????
			clearTempAction();
		}
		return canAction;
	}

	/**
	 * ??????????????????????????????????????????????????????
	 *
	 * @param player
	 */
	private void refreshTempAction(CsMjPlayer player) {
		tempActionMap.remove(player.getSeat());
		Map<Integer, Integer> prioritySeats = new HashMap<>();
		for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
			int seat = entry.getKey();
			List<Integer> actionList = entry.getValue();
			List<Integer> list = CsMjDisAction.parseToDisActionList(actionList);
			int priorityAction = CsMjDisAction.getMaxPriorityAction(list);
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
		Iterator<CsMjTempAction> iterator = tempActionMap.values().iterator();
		while (iterator.hasNext()) {
			CsMjTempAction tempAction = iterator.next();
			if (tempAction.getSeat() == maxPrioritySeat) {
				int action = tempAction.getAction();
				List<CsMj> tempCardList = tempAction.getCardList();
				CsMjPlayer tempPlayer = seatMap.get(tempAction.getSeat());
				playCommand(tempPlayer, tempCardList, action);// ?????????????????????????????????
				iterator.remove();
				break;
			}
		}
		changeExtend();
	}

	/**
	 * ????????????????????????????????? ????????????????????????????????????????????????????????????
	 *
	 * @param player
	 * @param action
	 * @return
	 */
	public boolean checkCanAction(CsMjPlayer player, int action) {
		// ???????????????????????????
		List<Integer> stopActionList = CsMjDisAction.findPriorityAction(action);
		for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
			if (player.getSeat() != entry.getKey()) {
				// ??????
				boolean can = CsMjDisAction.canDisMajiang(stopActionList, entry.getValue());
				if (!can) {
					return false;
				}
				List<Integer> disActionList = CsMjDisAction.parseToDisActionList(entry.getValue());
				if (disActionList.contains(action)) {
					// ??????????????????????????? ????????????????????????
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
	 * ???????????????
	 *
	 * @param player
	 * @param majiangs
	 * @param sameCount
	 * @return
	 */
	private boolean canMingGang(CsMjPlayer player, List<CsMj> handMajiang, List<CsMj> majiangs, int sameCount,
			CsMj disMajiang) {
		List<Integer> pengList = CsMjHelper.toMajiangVals(player.getPeng());

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

	public int getAskLastMajaingSeat() {
		return askLastMajaingSeat;
	}

	public void setFristLastMajiangSeat(int fristLastMajiangSeat) {
		this.fristLastMajiangSeat = fristLastMajiangSeat;
		changeExtend();
	}

	public void setLastMajiang(CsMj lastMajiang) {
		this.lastMajiang = lastMajiang;
		changeExtend();
	}

	public void setMoLastMajiangSeat(int moLastMajiangSeat) {
		this.moLastMajiangSeat = moLastMajiangSeat;
		changeExtend();
	}

	public void setGangMajiang(CsMj gangMajiang) {
		this.gangMajiang = gangMajiang;
		changeExtend();
	}

	/**
	 * ?????????????????????
	 *
	 * @param moGang
	 *            ?????????
	 * @param moGangHuList
	 *            ????????????????????????list
	 */
	public void setMoGang(CsMj moGang, List<Integer> moGangHuList) {
		this.moGang = moGang;
		this.moGangHuList = moGangHuList;
		changeExtend();
	}

	/**
	 * ???????????????
	 */
	public void clearMoGang() {
		this.moGang = null;
		this.moGangHuList.clear();
		changeExtend();
	}

	public void setGangDisMajiangs(List<CsMj> gangDisMajiangs) {
		this.gangDisMajiangs = gangDisMajiangs;
		changeExtend();
	}

	public List<CsMj> getGangDisMajiangs() {
		return gangDisMajiangs;
	}

	/**
	 * ?????????????????????
	 */
	public void clearGangDisMajiangs() {
		this.gangActedMj = null;
		this.gangMajiang = null;
		this.gangDisMajiangs.clear();
		this.gangDice = -1;
		changeExtend();
	}

	/**
	 * guo ?????????
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
	 * ???????????????
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
	 * ????????????
	 *
	 * @return
	 */
	public boolean canGangHu() {
		return true;
	}

	public ClosingMjInfoRes.Builder sendAccountsMsg(boolean over, boolean selfMo, List<Integer> winList,
			int[] prickBirdMajiangIds, int[] seatBirds, Map<Integer, Integer> seatBirdMap, boolean isBreak,
			int bankerSeat, int catchBirdSeat) {

		List<ClosingMjPlayerInfoRes> list = new ArrayList<>();
		List<ClosingMjPlayerInfoRes.Builder> builderList = new ArrayList<>();
		int fangPaoSeat = selfMo ? 0 : disCardSeat;
		if (winList == null || winList.size() == 0) {
			fangPaoSeat = 0;
		}
		for (CsMjPlayer player : seatMap.values()) {
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
					// ????????????
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
						CsMj huMajiang = nowDisCardIds.get(0);
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
				// ?????????????????????????????????????????????
				// list.add(0, build.build());
				builderList.add(0, build);
			} else {
				// list.add(build.build());
				builderList.add(build);
			}
			// ?????????
			if (isCreditTable()) {
				player.setWinLoseCredit(player.getTotalPoint() * creditDifen);
			}
		}

		// ???????????????
		if (isCreditTable()) {
			// ??????????????????
			calcNegativeCredit();
			long dyjCredit = 0;
			for (CsMjPlayer player : seatMap.values()) {
				if (player.getWinLoseCredit() > dyjCredit) {
					dyjCredit = player.getWinLoseCredit();
				}
			}
			for (ClosingMjPlayerInfoRes.Builder builder : builderList) {
				CsMjPlayer player = seatMap.get(builder.getSeat());
				calcCommissionCredit(player, dyjCredit);
				builder.setWinLoseCredit(player.getWinLoseCredit());
				builder.setCommissionCredit(player.getCommissionCredit());
			}
		} else if (isGroupTableGoldRoom()) {
            // -----------??????????????????---------------------------------

            for (CsMjPlayer player : seatMap.values()) {
                player.setWinGold(player.getTotalPoint() * gtgDifen);
            }
            calcGroupTableGoldRoomWinLimit();
            for (ClosingMjPlayerInfoRes.Builder builder : builderList) {
                CsMjPlayer player = seatMap.get(builder.getSeat());
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
		res.addAllLeftCards(CsMjHelper.toMajiangIds(leftMajiangs));
		for (CsMjPlayer player : seatMap.values()) {
			player.writeSocket(res.build());
		}
		return res;

	}

	/**
	 * ?????????????????????
	 *
	 * @return
	 */
	public CsMj getGangHuMajiang(int seat,CsMj disMajiang) {
		CsMj gMj= null;
		for (Entry<Integer, Map<Integer, List<Integer>>> entry : gangSeatMap.entrySet()) {
			Map<Integer, List<Integer>> actionMap = entry.getValue();
			if (actionMap.containsKey(seat)) {
				List<Integer> actionList = actionMap.get(seat);
				if (actionList != null && !actionList.isEmpty() && actionList.get(0) == 1) {
					int majiangId = entry.getKey();
					 gMj = CsMj.getMajang(majiangId);
					if(disMajiang!=null&&gMj!=null &&disMajiang.getVal()==gMj.getVal()){
						return gMj;
					}
					break;
				}
			}
		}
		return gMj;

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
		return ext;
	}

	@Override
	public void sendAccountsMsg() {
		
		// ????????????
		if(jsXiaoHuF==1){
			boolean xiaohu = false;
			for (CsMjPlayer winPlayer : seatMap.values()) {
				if (winPlayer.getHuXiaohu().size() == 0) {
					continue;
				}
				xiaohu = true;
				break;
			}
			if(xiaohu){
				calXiaoHuFen(new ArrayList<Integer>(), true, null);
			}
		}
        calcPointBeforeOver();
		ClosingMjInfoRes.Builder builder = sendAccountsMsg(true, false, null, null, null, null, true, 0, 0);
		saveLog(true, 0l, builder.build());
	}

	public Class<? extends Player> getPlayerClass() {
		return CsMjPlayer.class;
	}

	@Override
	public int getWanFa() {
		return GameUtil.game_type_csmj;
	}

	@Override
	public void checkReconnect(Player player) {
		if (super.isAllReady() && getKePiao() == 1 && getTableStatus() == CsMjConstants.TABLE_STATUS_PIAO) {
			CsMjPlayer player1 = (CsMjPlayer) player;
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
			// ????????????????????????????????????
			if (player instanceof CsMjPlayer) {
				CsMjPlayer csMjPlayer = (CsMjPlayer) player;
				if (csMjPlayer != null) {
					if (csMjPlayer.isAlreadyMoMajiang()) {
						if (!csMjPlayer.getGang().isEmpty()) {
							List<CsMj> disMajiangs = new ArrayList<>();
							disMajiangs.add(csMjPlayer.getLastMoMajiang());
							chuPai(csMjPlayer, disMajiangs, 0);
						}
					}
				}
			}
		}
		if (isBegin() && player.getSeat() == lastWinSeat && actionSeatMap.isEmpty()) {
			// ???????????????????????????????????????????????? ??????????????????????????????
			CsMjPlayer bankPlayer = seatMap.get(lastWinSeat);
			ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.req_com_kt_ask_dismajiang);// ??????????????????
			bankPlayer.writeSocket(com.build());
		}

		if (state == table_state.play) {
			if (player.getHandPais() != null && player.getHandPais().size() > 0) {
				sendTingInfo((CsMjPlayer) player);
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
		// ???????????????????????????
		if (getSendDissTime() > 0) {
			for (CsMjPlayer player : seatMap.values()) {
				if (player.getLastCheckTime() > 0) {
					player.setLastCheckTime(player.getLastCheckTime() + 1 * 1000);
				}
			}
			return;
		}

		for (CsMjPlayer player : seatMap.values()) {
//			if(player.isRobot() && table_state.play==getState()){
//				List<Integer> actionList = actionSeatMap.get(player.getSeat());
//				//diss();
//				IBTExecutor robotAI =  CsMjRobotAIGenerator.getInst().generateRobotAI(1);
//				CsmjMidRobotActionResult.RoBotAIBehavior(robotAI,actionSeatMap, player, getOnlyDaHu(), getQuanqiurJiang(), leftMajiangs, this);
////				return;
//
//			}
			if (!player.getGang().isEmpty() && player.isAlreadyMoMajiang() && getMoMajiangSeat() == player.getSeat()) {
				// ??????????????????????????????
				List<Integer> actionList = actionSeatMap.get(player.getSeat());
				if (actionList != null && (actionList.get(CsMjAction.HU) == 1 || actionList.get(CsMjAction.ZIMO) == 1
						|| actionList.get(CsMjAction.MINGGANG) == 1 || actionList.get(CsMjAction.BUZHANG) == 1
						|| hasXiaoHu(actionList))) {
					continue;
				}

				if (nowDisCardSeat != player.getSeat()) {
					continue;
				}
				List<CsMj> disMjiang = new ArrayList<>();
				disMjiang.add(player.getHandMajiang().get(player.getHandMajiang().size() - 1));
				chuPai(player, disMjiang, CsMjDisAction.action_chupai);
				changeDisCardRound(1);
				// ?????????????????????????????????????????????????????????????????????
				setLastAutoPlayTime(System.currentTimeMillis());
				return;
			}
		}

		if (isAutoPlay < 1) {
			return;
		}

        if (isAutoPlayOff()) {
            // ????????????
            for (int seat : seatMap.keySet()) {
                CsMjPlayer player = seatMap.get(seat);
                player.setAutoPlay(false, false);
                player.setCheckAutoPlay(false);
            }
            return;
        }

		if (getTableStatus() == CsMjConstants.TABLE_STATUS_PIAO) {
			for (int seat : seatMap.keySet()) {
				CsMjPlayer player = seatMap.get(seat);
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
				CsMjPlayer player = seatMap.get(seat);
				if (player.getPiaoPoint() < 0) {
					piao = false;
				}

			}
			if (piao) {
				setTableStatus(CsMjConstants.AUTO_PLAY_TIME);
			}

		} else if (state == table_state.play) {
			autoPlay();
		} else {
			if (getPlayedBureau() == 0) {
				return;
			}
			readyTime++;
			// for (CsMjPlayer player : seatMap.values()) {
			// if (player.checkAutoPlay(1, false)) {
			// autoReady(player);
			// }
			// }
			// ????????????????????????xx???????????????????????????
			for (CsMjPlayer player : seatMap.values()) {
				if (player.getState() != player_state.entry && player.getState() != player_state.over) {
					continue;
				} else {
					if (readyTime >= 5 && player.isAutoPlay()) {
						// ????????????????????????3???????????????
						autoReady(player);
					} else if (readyTime > 30) {
						autoReady(player);
					}
				}
			}
		}

	}

	/**
	 * ????????????
	 */
	public synchronized void autoPlay() {
		if (state != table_state.play) {
			return;
		}
		if (!actionSeatMap.isEmpty()) {
			List<Integer> huSeatList = getHuSeatByActionMap();
			if (!huSeatList.isEmpty()) {
				// ???????????????
				for (int seat : huSeatList) {
					CsMjPlayer player = seatMap.get(seat);
					if (player == null) {
						continue;
					}
					if (!player.checkAutoPlay(2, false)) {
						continue;
					}
					playCommand(player, new ArrayList<>(), CsMjDisAction.action_hu);
				}
				return;
			} else {
				int action, seat;
				for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
					List<Integer> actList = CsMjDisAction.parseToDisActionList(entry.getValue());
					if (actList == null) {
						continue;
					}
					seat = entry.getKey();
					action = CsMjDisAction.getAutoMaxPriorityAction(actList);
					CsMjPlayer player = seatMap.get(seat);
					if (!player.checkAutoPlay(0, false)) {
						continue;
					}
					boolean chuPai = false;
					if (player.isAlreadyMoMajiang()) {
						chuPai = true;
					}
					if (action == CsMjDisAction.action_peng) {
						if (player.isAutoPlaySelf()) {
							// ???????????????????????????
							playCommand(player, new ArrayList<>(), CsMjDisAction.action_pass);
							if (chuPai) {
								autoChuPai(player);
							}
						} else {
							if (nowDisCardIds != null && !nowDisCardIds.isEmpty()) {
                                for(CsMj mj : nowDisCardIds) {
                                    List<CsMj> mjList = new ArrayList<>();
                                    for (CsMj handMj : player.getHandMajiang()) {
                                        if (handMj.getVal() == mj.getVal()) {
                                            mjList.add(handMj);
                                            if (mjList.size() == 2) {
                                                break;
                                            }
                                        }
                                    }
                                    if(mjList.size() >= 2){
                                        playCommand(player, mjList, CsMjDisAction.action_peng);
                                        break;
                                    }
                                }
							}
						}
					}
					// else if(action == CsMjDisAction.action_chi){
					// playCommand(player, new ArrayList<>(),
					// CsMjDisAction.action_chi);
					// if (chuPai) {
					// autoChuPai(player);
					// }
					//
					// }
					else {
						playCommand(player, new ArrayList<>(), CsMjDisAction.action_pass);
						if (chuPai) {
							autoChuPai(player);
						}
					}
				}
			}
		} else {
			CsMjPlayer player = seatMap.get(nowDisCardSeat);
			if (player == null || !player.checkAutoPlay(0, false)) {
				return;
			}


			if (player.getSeat() == askLastMajaingSeat) {
				moLastMajiang(player,0);
			}


			autoChuPai(player);
		}
	}

	public void autoChuPai(CsMjPlayer player) {

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
		// CsMj mj = CsMj.getMajang(mjId);

		while (mjId == -1 && index >= 0) {
			mjId = handMjIds.get(index);
			// mj = CsMj.getMajang(mjId);

		}
		if (mjId != -1) {
			List<CsMj> mjList = CsMjHelper.toMajiang(Arrays.asList(mjId));
			playCommand(player, mjList, CsMjDisAction.action_chupai);
		}
	}

	public void autoPiao(CsMjPlayer player) {
		int piaoPoint = 0;
		if (getTableStatus() != CsMjConstants.TABLE_STATUS_PIAO) {
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
		for (CsMjPlayer player : seatMap.values()) {
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
			moGang = CsMj.getMajang(moGangMajiangId);
		}
		String moGangHu = extend.getString(9);
		if (!StringUtils.isBlank(moGangHu)) {
			moGangHuList = StringUtil.explodeToIntList(moGangHu);
		}
		String gangDisMajiangstr = extend.getString(10);
		if (!StringUtils.isBlank(gangDisMajiangstr)) {
			gangDisMajiangs = CsMjHelper.explodeMajiang(gangDisMajiangstr, ",");
		}
		int gangMajiang = extend.getInt(11, 0);
		if (gangMajiang != 0) {
			this.gangMajiang = CsMj.getMajang(gangMajiang);
		}

		askLastMajaingSeat = extend.getInt(12, 0);
		moLastMajiangSeat = extend.getInt(13, 0);
		int lastMajiangId = extend.getInt(14, 0);
		if (lastMajiangId != 0) {
			this.lastMajiang = CsMj.getMajang(lastMajiangId);
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
		qiShouNiaoFen= extend.getInt(53, 0);
		below= extend.getInt(54, 0);
		belowAdd= extend.getInt(55, 0);
		gangBuF= extend.getInt(56, 0);
		quanqiurJiang= extend.getInt(57, 0);
		difen= extend.getInt(58, 0);
		
		menQingZM = extend.getInt(59, 0);
		jsXiaoHuF = extend.getInt(60, 0);
		
		xiaoHuAuto = extend.getInt(61, 0);
		xiaoHuGdF = extend.getInt(62, 0);
		jjHKqFg = extend.getInt(63, 0);
	}

	@Override
	public JsonWrapper buildExtend0(JsonWrapper wrapper) {
		// 1-4 ??????????????????
		for (CsMjPlayer player : seatMap.values()) {
			wrapper.putString(player.getSeat(), player.toExtendStr());
		}
		wrapper.putString(5, DataMapUtil.explode(huConfirmMap));
		wrapper.putInt(6, birdNum);
		wrapper.putInt(7, moMajiangSeat);
		wrapper.putInt(8, moGang != null ? moGang.getId() : 0);
		wrapper.putString(9, StringUtil.implode(moGangHuList, ","));
		wrapper.putString(10, CsMjHelper.implodeMajiang(gangDisMajiangs, ","));
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
		wrapper.putInt(60, jsXiaoHuF);
		
		wrapper.putInt(61, xiaoHuAuto);
		wrapper.putInt(62, xiaoHuGdF);
		wrapper.putInt(63, jjHKqFg);
		
		
		return wrapper;
	}

    @Override
    public void createTable(Player player, int play, int bureauCount, List<Integer> params, List<String> strParams,Object... objects) throws Exception {
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

		long id = getCreateTableId(player.getUserId(), playType);
		TableInf info = new TableInf();
		info.setMasterId(player.getUserId());
        info.setTableType(tableType);
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

		maxPlayerCount = StringUtil.getIntValue(params, 7, 4);// ????????????
		payType = StringUtil.getIntValue(params, 2, 1);// ????????????
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

		// ?????????0??????1???
		this.jiaBei = StringUtil.getIntValue(params, 19, 0);
		// ?????????
		this.jiaBeiFen = StringUtil.getIntValue(params, 20, 0);
		// ?????????
		this.jiaBeiShu = StringUtil.getIntValue(params, 21, 0);

		if (maxPlayerCount == 3) {
			// ??????????????? 1??????????????? 2:????????????
			kongBird = StringUtil.getIntValue(params, 22, 0);
		} else if (maxPlayerCount == 2) {
			// ???????????????1????????????
			buChi = StringUtil.getIntValue(params, 23, 0);
			// ???????????????????????????
			OnlyDaHu = StringUtil.getIntValue(params, 24, 0);
			// ???????????????????????????
			xiaohuZiMo = StringUtil.getIntValue(params, 25, 0);
			// ????????????????????????
			queYiMen = StringUtil.getIntValue(params, 26, 0);
		}

		jiajianghu = StringUtil.getIntValue(params, 27, 0);

		isAutoPlay = StringUtil.getIntValue(params, 28, 0);
		this.autoPlayGlob = StringUtil.getIntValue(params, 29, 0);
		menqing = StringUtil.getIntValue(params, 30, 0);

		topFen = StringUtil.getIntValue(params, 31, 0);
		
		
		gangMoSi= StringUtil.getIntValue(params, 32, 0);
		
		
		qiShouNiaoFen =  StringUtil.getIntValue(params, 33, 0);
		

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
		difen=  StringUtil.getIntValue(params, 38, 0);
		
		menQingZM =  StringUtil.getIntValue(params, 39, 0);
		
		jsXiaoHuF =  StringUtil.getIntValue(params, 40, 0);
		
		xiaoHuAuto =  StringUtil.getIntValue(params, 41, 0);
		
		xiaoHuGdF =  StringUtil.getIntValue(params, 42, 0);
		
		jjHKqFg =  StringUtil.getIntValue(params, 43, 0);
		
		if(difen==0){
			difen=1;
		}
		if (maxPlayerCount != 2) {
			jiaBei = 0;
		}
		playedBureau = 0;
		// getRoomModeMap().put("1", "1"); //?????????????????????
        return true;
	}

	public void sendTingInfo(CsMjPlayer player) {
		if (player.isAlreadyMoMajiang()) {
			// if (actionSeatMap.containsKey(player.getSeat())) {
			// return;
			// }
			DaPaiTingPaiRes.Builder tingInfo = DaPaiTingPaiRes.newBuilder();
			List<CsMj> cards = new ArrayList<>(player.getHandMajiang());

			for (CsMj card : player.getHandMajiang()) {
				cards.remove(card);
				List<CsMj> huCards = CsMjTool.getTingMjs(cards, player.getGang(), player.getPeng(), player.getChi(),
						player.getBuzhang(), true, OnlyDaHu == 1,getQuanqiurJiang());
				cards.add(card);
				if (huCards == null || huCards.size() == 0) {
					continue;
				}
				DaPaiTingPaiInfo.Builder ting = DaPaiTingPaiInfo.newBuilder();
				ting.setMajiangId(card.getId());
				for (CsMj mj : huCards) {
					ting.addTingMajiangIds(mj.getId());
				}
				tingInfo.addInfo(ting.build());
			}
			if (tingInfo.getInfoCount() > 0) {
				player.writeSocket(tingInfo.build());
			}
		} else {
			List<CsMj> cards = new ArrayList<>(player.getHandMajiang());
			List<CsMj> huCards = CsMjTool.getTingMjs(cards, player.getGang(), player.getPeng(), player.getChi(),
					player.getBuzhang(), true, OnlyDaHu == 1,getQuanqiurJiang());

			if (huCards == null || huCards.size() == 0) {
				return;
			}
			TingPaiRes.Builder ting = TingPaiRes.newBuilder();
			for (CsMj mj : huCards) {
				ting.addMajiangIds(mj.getId());

			}

			player.writeSocket(ting.build());

		}
	}

	public static final List<Integer> wanfaList = Arrays.asList(GameUtil.game_type_csmj);

	public static void loadWanfaTables(Class<? extends BaseTable> cls) {
		for (Integer integer : wanfaList) {
			TableManager.wanfaTableTypesPut(integer, cls);
		}
	}

	/**
	 * ?????????????????????
	 *
	 * @param actionIndex
	 *            CSMajiangConstants?????????
	 * @return
	 */
	public boolean canXiaoHu(int actionIndex) {

		// if(maxPlayerCount==2 && OnlyDaHu ==1) {
		// return false;
		// }
		switch (actionIndex) {
		case CsMjAction.QUEYISE:
			return queYiSe == 1;
		case CsMjAction.BANBANHU:
			return banbanHu == 1;
		case CsMjAction.YIZHIHUA:
			return yiZhiHua == 1;
		case CsMjAction.LIULIUSHUN:
			return liuliuShun == 1;
		case CsMjAction.DASIXI:
			return daSiXi == 1;
		case CsMjAction.JINGTONGYUNU:
			return jinTongYuNu == 1;
		case CsMjAction.JIEJIEGAO:
			return jieJieGao == 1;
		case CsMjAction.SANTONG:
			return sanTong == 1;
		case CsMjAction.ZHONGTULIULIUSHUN:
			return zhongTuLiuLiuShun == 1;
		case CsMjAction.ZHONGTUSIXI:
			return zhongTuSiXi == 1;
		default:
			return false;
		}
	}

	public void logFaPaiTable() {
		StringBuilder sb = new StringBuilder();
		sb.append("CsMj");
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
		LogUtil.msg(sb.toString());
	}

	public void logFaPaiPlayer(CsMjPlayer player, List<Integer> actList) {
		StringBuilder sb = new StringBuilder();
		sb.append("CsMj");
		sb.append("|").append(getId());
		sb.append("|").append(getPlayBureau());
		sb.append("|").append(player.getUserId());
		sb.append("|").append(player.getSeat());
		sb.append("|").append("faPai");
		sb.append("|").append(player.getHandMajiang());
		sb.append("|").append(actListToString(actList));
		LogUtil.msg(sb.toString());
	}

	public void logMoMj(CsMjPlayer player, CsMj mj, List<Integer> actList) {
		StringBuilder sb = new StringBuilder();
		sb.append("CsMj");
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

    public void logGangMoMj(CsMjPlayer player, List<CsMj> mjs) {
        StringBuilder sb = new StringBuilder();
        sb.append("CsMj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append("gangMoPai");
        sb.append("|").append(getLeftMajiangCount());
        sb.append("|").append(mjs);
        sb.append("|").append(player.getHandMajiang());
        LogUtil.msg(sb.toString());
    }

	public void logChuPaiActList(CsMjPlayer player, CsMj mj, List<Integer> actList) {
		StringBuilder sb = new StringBuilder();
		sb.append("CsMj");
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

	public void logAction(CsMjPlayer player, int action, int xiaoHuType, List<CsMj> mjs, List<Integer> actList) {
		StringBuilder sb = new StringBuilder();
		sb.append("CsMj");
		sb.append("|").append(getId());
		sb.append("|").append(getPlayBureau());
		sb.append("|").append(player.getUserId());
		sb.append("|").append(player.getSeat());
		String actStr = "unKnown-" + action;
		if (action == CsMjDisAction.action_peng) {
			actStr = "peng";
		} else if (action == CsMjDisAction.action_minggang) {
			actStr = "mingGang";
		} else if (action == CsMjDisAction.action_chupai) {
			actStr = "chuPai";
		} else if (action == CsMjDisAction.action_pass) {
			actStr = "guo";
		} else if (action == CsMjDisAction.action_angang) {
			actStr = "anGang";
		} else if (action == CsMjDisAction.action_chi) {
			actStr = "chi";
		} else if (action == CsMjDisAction.action_buzhang) {
			actStr = "buZhang";
		} else if (action == CsMjDisAction.action_xiaohu) {
			actStr = "xiaoHu";
		} else if (action == CsMjDisAction.action_buzhang_an) {
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
				if (i == CsMjAction.HU) {
					sb.append("hu");
				} else if (i == CsMjAction.PENG) {
					sb.append("peng");
				} else if (i == CsMjAction.MINGGANG) {
					sb.append("mingGang");
				} else if (i == CsMjAction.ANGANG) {
					sb.append("anGang");
				} else if (i == CsMjAction.CHI) {
					sb.append("chi");
				} else if (i == CsMjAction.BUZHANG) {
					sb.append("buZhang");
				} else if (i == CsMjAction.QUEYISE) {
					sb.append("queYiSe");
				} else if (i == CsMjAction.BANBANHU) {
					sb.append("banBanHu");
				} else if (i == CsMjAction.YIZHIHUA) {
					sb.append("yiZhiHua");
				} else if (i == CsMjAction.LIULIUSHUN) {
					sb.append("liuLiuShun");
				} else if (i == CsMjAction.DASIXI) {
					sb.append("daSiXi");
				} else if (i == CsMjAction.JINGTONGYUNU) {
					sb.append("jinTongYuNu");
				} else if (i == CsMjAction.JIEJIEGAO) {
					sb.append("jieJieGao");
				} else if (i == CsMjAction.SANTONG) {
					sb.append("sanTong");
				} else if (i == CsMjAction.ZHONGTUSIXI) {
					sb.append("zhongTuSiXi");
				} else if (i == CsMjAction.ZHONGTULIULIUSHUN) {
					sb.append("zhongTuLiuLiuShun");
				} else if (i == CsMjAction.BUZHANG_AN) {
					sb.append("buZhangAn");
				}
			}
		}
		sb.append("]");
		return sb.toString();
	}

	/**
	 * ???????????????????????????????????????
	 *
	 * @param player
	 */
	public void processHideMj(CsMjPlayer player) {
		if (showMjSeat.contains(player.getSeat()) && disCardRound != 0) {
			PlayMajiangRes.Builder hideMj = PlayMajiangRes.newBuilder();
			buildPlayRes(hideMj, player, CsMjDisAction.action_hideMj, null);
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
		// if(xiaoHuType == CsMjAction.QUEYISE || xiaoHuType ==
		// CsMjAction.BANBANHU || xiaoHuType == CsMjAction.YIZHIHUA){
		if (!showMjSeat.contains(seat)) {
			showMjSeat.add(seat);
			changeExtend();
		}
		// }

	}

	/**
	 * ????????????????????????
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
				// ?????????????????????
				if (this.isTest()) {
					for (CsMjPlayer robotPlayer : seatMap.values()) {
						if (robotPlayer.isRobot()) {
							robotPlayer.setPiaoPoint(1);
						}
					}
				}
				for (CsMjPlayer player : seatMap.values()) {
					if (player.getPiaoPoint() < 0) {
						ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_table_status_piao,
								getTableStatus());
						player.writeSocket(com.build());
						if (getTableStatus() != CsMjConstants.TABLE_STATUS_PIAO) {
							player.setLastCheckTime(System.currentTimeMillis());
						}
						bReturn = false;
						// if(getTableStatus()!=CsMjConstants.TABLE_STATUS_PIAO)
						// {
						//
						// }
					}
				}
				setTableStatus(CsMjConstants.TABLE_STATUS_PIAO);

				return bReturn;
			} else {
				int point = 0;
				if (getKePiao() == 2 || getKePiao() == 3 || getKePiao() == 4) {
					point = getKePiao() - 1;
				}

				for (CsMjPlayer player : seatMap.values()) {
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
			// ??????????????????
			for (int i = 0; i < leftMjCount; i++) {
				int nowIndex = i + startIndex;
				if (!moTailPai.contains(nowIndex)) {
					moTailPai.add(nowIndex);
					break;
				}
			}

		} else {
			int duo = gangDice / 10 + gangDice % 10;
			// ???????????????????????????
			for (int i = 0, j = 0; i < leftMjCount; i++) {
				int nowIndex = i + startIndex;
				if (nowIndex % 2 == 1) {
					j++; // ???????????????
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
	 * ???????????????
	 */
	public void clearMoTailPai() {
		this.moTailPai.clear();
		changeExtend();
	}

	/**
	 * ??????????????????????????????
	 */
	public void checkClearGangDisMajiang() {
		List<CsMj> moList = getGangDisMajiangs();
		if (moList != null && moList.size() > 0 && actionSeatMap.isEmpty()) {
			CsMjPlayer player = seatMap.get(getMoMajiangSeat());
			for (CsMjPlayer seatPlayer : seatMap.values()) {
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
	 * ?????????????????????
	 *
	 * @param player
	 * @param state
	 *            1????????????????????????0?????????????????????????????????
	 */
	public void sendMoLast(CsMjPlayer player, int state) {
		ComRes.Builder res = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_asklastmajiang, state);
		player.writeSocket(res.build());
	}

	/**
	 * ?????????????????????
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
			if (CsMjAction.getFirstXiaoHu(actList) != -1) {
				return true;
			}
		}
		return false;
	}

	/**
	 * ?????????????????????
	 *
	 * @return
	 */
	public boolean hasXiaoHu(List<Integer> actList) {
		if (CsMjAction.getFirstXiaoHu(actList) != -1) {
			return true;
		}
		return false;
	}

	@Override
	public boolean isPlaying() {
		if (super.isPlaying()) {
			return true;
		}
		return getTableStatus() == CsMjConstants.TABLE_STATUS_PIAO;
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
	

	public int getXiaoHuAuto() {
		return xiaoHuAuto;
	}

	public int getQuanqiurJiang() {
		return quanqiurJiang;
	}

	public String getTableMsg() {
		Map<String, Object> json = new HashMap<>();
		json.put("wanFa", "????????????");
		if (isGroupRoom()) {
			json.put("roomName", getRoomName());
		}
		json.put("playerCount", getPlayerCount());
		json.put("count", getTotalBureau());
		if (isAutoPlay > 0) {
			json.put("autoTime", isAutoPlay);
			if (autoPlayGlob == 1) {
				json.put("autoName", "??????");
			} else {
				json.put("autoName", "??????");
			}
		}
		return JSON.toJSONString(json);
	}

    @Override
    public String getGameName() {
		return "????????????";
    }

	public List<CsMj> getLeftMajiangs() {
		return leftMajiangs;
	}
}
