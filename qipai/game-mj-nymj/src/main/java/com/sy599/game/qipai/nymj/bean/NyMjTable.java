package com.sy599.game.qipai.nymj.bean;

import java.lang.reflect.Array;
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
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
import com.sy599.game.msg.serverPacket.TableMjResMsg;
import com.sy599.game.msg.serverPacket.TableMjResMsg.ClosingMjInfoRes;
import com.sy599.game.msg.serverPacket.TableMjResMsg.ClosingMjPlayerInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.CreateTableRes;
import com.sy599.game.msg.serverPacket.TableRes.DealInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes;
import com.sy599.game.qipai.nymj.constant.MjAction;
import com.sy599.game.qipai.nymj.constant.MjConstants;
import com.sy599.game.qipai.nymj.rule.Mj;
import com.sy599.game.qipai.nymj.rule.MjRobotAI;
import com.sy599.game.qipai.nymj.rule.MjRule;
import com.sy599.game.qipai.nymj.tool.MjResTool;
import com.sy599.game.qipai.nymj.tool.MjTool;
import com.sy599.game.qipai.nymj.command.AbsCodeCommandExecutor;
import com.sy599.game.qipai.nymj.rule.MjHelper;
import com.sy599.game.qipai.nymj.tool.MjQipaiTool;
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
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;

@Data
public class NyMjTable extends BaseTable {
    public static final List<Integer> wanfaList = Arrays.asList(GameUtil.play_type_nymj);

    @Override
    public int getWanFa() {
        return GameUtil.play_type_nymj;
    }

    @Override
    public String getGameName() {
        return "宁远麻将";
    }

    public String getTableMsg() {
        Map<String, Object> json = new HashMap<>();
        json.put("wanFa", getGameName());
        if (isGroupRoom()) {
            json.put("roomName", getRoomName());
        }
        json.put("playerCount", getPlayerCount());
        json.put("count", getTotalBureau());
        if (this.autoPlay) {
            json.put("autoTime", isAutoPlay);
            if (autoPlayGlob == 1) {
                json.put("autoName", "单局");
            } else {
                json.put("autoName", "整局");
            }
        }
        return JSON.toJSONString(json);
    }

    /**
     * 游戏模式配置
     */
    private GameModel gameModel;
    /**
     * 当前桌上打出的牌
     */
    private List<Mj> nowDisCardIds = new ArrayList<>();
    /**
     * 所有玩家当前可作的操作 com.sy599.game.qipai.tjmj.constant.MjAction
     * 接炮0;碰1;明杠2;暗杠3;吃4;补张5;缺一色6;板板胡7;一枝花 8;六六顺9;大四喜10;金童玉女11;节节高12;三同13;中途四喜14;中途六六顺15;暗杠补张16;自摸17;黑天胡18
     */
    private Map<Integer, List<Integer>> actionSeatMap = new ConcurrentHashMap<>();
    /**
     * 麻将ID->playerSeat->ActionList 0胡 1碰 2明刚 3暗杠(暗杠后来不需要了 暗杠也用3标记)4吃 5补张(6缺一色 7板板胡 8大四喜 9六六顺 10节节高 11三同 12一枝花 13中途四喜 14中途六六顺)
     */
    private Map<Integer, Map<Integer, List<Integer>>> gangSeatMap = new ConcurrentHashMap<>();
    /**
     * 房间最大玩家人数上限
     */
    private int maxPlayerCount = 4;
    /**
     * 当前剩下的牌（庄上的牌）
     */
    private List<Mj> leftMajiangs = new ArrayList<>();
    /**
     * 当前房间所有玩家信息map
     */
    private Map<Long, NyMjPlayer> playerMap = new ConcurrentHashMap<Long, NyMjPlayer>();
    /**
     * 座位对应的玩家信息MAP
     */
    private Map<Integer, NyMjPlayer> seatMap = new ConcurrentHashMap<Integer, NyMjPlayer>();
    /**
     * 胡确认信息
     */
    private Map<Integer, Integer> huConfirmMap = new HashMap<>();
    /**
     * 玩家位置对应临时操作 当同时存在多个可做的操作时 1当优先级最高的玩家最先操作 则清理所有操作提示 并执行该操作
     * 2当操作优先级低的玩家先选择操作，则玩家先将所做操作存入临时操作中 当玩家优先级最高的玩家操作后 在判断临时操作是否可执行并执行
     */
    private Map<Integer, MjTempAction> tempActionMap = new ConcurrentHashMap<>();

    /**
     * 摸麻将的seat
     */
    private int moMajiangSeat;
    /**
     * 当前摸杠的seat,该字段用于抢杠胡(杠上炮|杠上花必胡)过后返回主动权的操作
     */
    private int nowGangSeat;
    /**
     * 摸杠的麻将
     */
    private Mj moGang;
    /**
     * 杠出来的麻将
     */
    private Mj gangMajiang;
    /**
     * 摸杠胡
     */
    private List<Integer> moGangHuList = new ArrayList<>();
    /**
     * 杠后出的两张牌
     */
    private List<Mj> gangDisMajiangs = new ArrayList<>();
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
    private Mj lastMajiang;

    /**
     * 当前摸出来的牌
     */
    private Mj nowMoCard;
    /**
     * 天牌/ 王牌/ 癞子牌, 可变任意牌
     */
    private Mj kingCard;
    /**
     * 地牌, 可用来胡牌
     */
    private Mj floorCard;

    /**
     * 头一轮跟庄出牌
     */
    private boolean followMaster;

    /**
     *
     */
    private int disEventAction;

    /*** 需要展示牌的玩家座位号 */
    private List<Integer> showMjSeat = new ArrayList<>();

    /*** 杠打色子 **/
    private int gangDice = -1;

    /*** 摸屁股的座标号 */
    private List<Integer> moTailPai = new ArrayList<>();

    /**
     * 杠后摸的两张牌中被要走的
     **/
    private Mj gangActedMj = null;

    /**
     * 是否是开局
     **/
    private boolean isBegin = false;

    /**
     * 骰子
     */
    private int dealDice;

    /**
     * 连庄：
     * 玩家如果连续胡牌（点炮、自摸）两局或两局以上，视为连庄；即玩家在当前局胡牌后，下一局再次胡牌，则为连庄，需要在该局胡牌分上多加连庄分数。
     */
    private int reappointment;

    /**
     * 托管1：单局，2：全局
     */
    private int autoPlayGlob;
    private int autoTableCount;
    private int isAutoPlay;// 托管时间
    private int readyTime = 0;

    @Override
    protected boolean quitPlayer1(Player player) {
        return false;
    }

    @Override
    public boolean canQuit(Player player) {
        if (super.canQuit(player)) {
            return gameModel.getSpecialPlay().getTableStatus() != MjConstants.TABLE_STATUS_PIAO;
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
        List<Integer> seatBirds = new ArrayList<>();
        List<Integer> birdsIds = new ArrayList<>();
        //座位->中鸟数
        Map<Integer, Integer> seatBirdMap = new HashMap<>();
        //所有买的马的归属 麻将ID->seat
//        Map<Integer, Integer> allBuyHorsesToSeat = new HashMap<>();
        //所有马的归属,麻将ID,中的座位号,所属座位号
        MjiangHu.BigMap<Integer, Integer, Integer> seatBirdBigMap = new MjiangHu.BigMap<Integer, Integer, Integer>(20);

        //流局
        boolean flow = winSeatList.size() == 0;
        //抢杠胡
        boolean qiangGangHu = false;

//         扎鸟
        boolean zhuaNiao = true;
        if (winSeatList.size() == 0) {
            // 流局
            flow = true;
//            zhuaNiao = false;
            for (int otherSeat : seatMap.keySet()) {
                if (seatMap.get(otherSeat).getHuXiaohu().size() > 0) {
                    zhuaNiao = true;
                    break;
                }
            }
        }

        //算鸟
		boolean goldBirdDouble = calcBird(zhuaNiao, winSeatList, seatBirdMap, seatBirdBigMap, seatBirds, birdsIds, true);

		//算杠
        calcGangScore(seatBirdBigMap, winSeatList);

		// 先判断是自摸还是放炮
		NyMjPlayer winPlayer = null;
		//大胡原始分, 这里不做重新计算,保留原始值, 更加精确
		int daHuBasicScore = 0;
        // 算胡的
        if (winSeatList.size() != 0) {
            if (winSeatList.size() == 1) {
                winPlayer = seatMap.get(winSeatList.get(0));
                if ((winPlayer.noNeedMoCard() || winPlayer.isGangshangHua())
                        && winSeatList.get(0) == moMajiangSeat) {
                    selfMo = true;
                }
            }

			qiangGangHu = gameModel.getSpecialPlay().isRobGangHu() && winPlayer != null && winPlayer.getDahu().contains(9);

            // 庄家,自摸, 各家出分, 吃杠杠爆全包算点炮, 门子输家必须加上
            if (selfMo && !qiangGangHu) {
                //大胡自摸
                boolean isDaHu = winPlayer.getDahuCount() > 0;

                //计算门子得分,自摸没有门子算平胡, 平胡+1
                int winScore = (MjiangHu.calcMenZiScore(this, winPlayer.getDahu())) ;

                //大胡原始分, 这里不做重新计算,保留原始值, 更加精确
				daHuBasicScore = winScore * gameModel.getBasicScore();

				winPlayer.setDaHuScore(winScore * (gameModel.getGameMaxHuman() - 1) * gameModel.getBasicScore());

				int winnerBirdNum = seatBirdMap.get(winPlayer.getSeat()) ;//calcBirdNum(seatBirdBigMap, winPlayer.getSeat(), winPlayer.getSeat(), true, true, false);

				//金马翻倍和一鸟全中 互斥
				int goldBird = gameModel.getSpecialPlay().isOneBirdScore() ? 1 : Math.max(1, (gameModel.isAllBirdDouble() && goldBirdDouble ? 2 : 0));

				int winnerBirdBasicScore = calcBirdPoint(winScore, winnerBirdNum * goldBird, true);

				LogUtil.printDebug("鸟得分:{} -> {}", winScore, winnerBirdBasicScore);

				//金马翻倍
				winScore = winnerBirdBasicScore * gameModel.getBasicScore() + (winPlayer.getFlutterVal());

				if (gameModel.isAllBirdDouble() && goldBirdDouble && goldBird > 1) {
					//金马翻倍门子
					winPlayer.getDahu().add(33);
					LogUtil.printDebug("增加金马全中~");
				}

				//填充闲家减的马分
				seatBirdMap.entrySet().iterator().forEachRemaining(v->{
					if (v.getValue() == 0) {
						v.setValue(-(winnerBirdNum * Math.max(1, goldBird) * gameModel.getBasicScore()));
					} else {
						v.setValue(v.getValue() * Math.max(1, goldBird) * (gameModel.getGameMaxHuman() - 1) * gameModel.getBasicScore());
					}
				});

				LogUtil.printDebug("计算门子得分:{}, {}, 飄分{}, 基礎分:{}, 金马翻倍:{}", winScore, MjTool.dahuListToString(winPlayer.getDahu()), winPlayer.getFlutterVal(), gameModel.getBasicScore(), winScore - winnerBirdBasicScore);

                int totalWinScore = 0;

                LogUtil.printDebug("得分:{}, 飘分 {}, {}", winScore, winPlayer.getFlutterVal());

                int winnerFlutter = 0;

                Iterator<NyMjPlayer> iterator = seatMap.values().iterator();
                while (iterator.hasNext()) {
                    NyMjPlayer player = iterator.next();
                    if (player.getSeat() != winPlayer.getSeat()) {
                        int pScore = (winScore + (player.getFlutterVal()));
						totalWinScore += pScore;
						int pFlutter = player.getFlutterVal() + winPlayer.getFlutterVal();
						winnerFlutter += pFlutter;
						player.setFlutter(-pFlutter);
                        player.changePoint(-pScore);

                        LogUtil.printDebug("输家飘分:{}, {}",player.getName(),player.getFlutter());
                    }
                }


                winPlayer.setFlutter(winnerFlutter);
                LogUtil.printDebug("{} 总赢分:{},杠+分:{}, 飘分:{}", winPlayer.getName(), totalWinScore, winPlayer.getGangPoint(), winPlayer.getFlutter());
                winPlayer.changeActionTotal(isDaHu ? MjAction.ACTION_COUNT_DAHU_ZIMO : MjAction.ACTION_COUNT_XIAOHU_ZIMO, 1);
                //赢得分
                winPlayer.changePoint(totalWinScore);
                winPlayer.changeAction(7, 1);
                winPlayer.getMyExtend().setMjFengshen(FirstmythConstants.firstmyth_index8, 1);
            }
        }

        //流局不算杠
//		if (!flow) {
			//杠翻倍, 杠分在胡到大胡或者硬胡分数翻倍, 仅赢家
			//结算杠分
			Iterator<NyMjPlayer> iterator = seatMap.values().iterator();
			while (iterator.hasNext()) {
				NyMjPlayer next = iterator.next();
				if (next != null) {
					next.changePoint(next.getGangPoint());

					LogUtil.printDebug("{},杠分:{},{}, 明杠:{}, 暗杠:{}, 放杠:{}, 马跟杠:{}", next.getName(), next.getGangPoint(), next.getPoint(), next.getMGangScore(), next.getAGangScore(), next.getFGangScore(), next.getHorseAndGangScore());
					LogUtil.printDebug("明杠:{}, 暗杠:{}, 放杠:{}", next.getMGangScoreHorseAndGang(), next.getAGangScoreHorseAndGang(), next.getFGangScoreHorseAndGang());

					if (!winSeatList.contains(next.getSeat()) && winPlayer!=null) {
						//输家去掉胡牌的标志位
						next.setDahu(winPlayer.getDahu());
						next.setDaHuScore(-daHuBasicScore);
					}
				}
			}
//		}

        boolean over = playBureau == totalBureau;

        if (autoPlayGlob > 0) {
            // //是否解散
            boolean diss = false;
            if (autoPlayGlob == 1) {
                for (NyMjPlayer seat : seatMap.values()) {
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
        ClosingMjInfoRes.Builder res = sendAccountsMsg(over, selfMo, winSeatList, birdsIds.toArray(new Integer[0]), seatBirds.toArray(new Integer[0]), seatBirdMap, seatBirdBigMap, false, lastWinSeat, 0);

        // 没有流局
        if (!flow) {
            // 若胡出抢杠胡，转庄到点炮的人
            if (winSeatList.size() > 1 || qiangGangHu) {
                //  一炮多响设置放炮的人为庄家
                //一炮多响，离点炮近的玩家优先胡牌
//                setLastWinSeat(getNextSeat(disCardSeat));
                setLastWinSeat(disCardSeat);
            } else {
                //胡牌为庄
                setLastWinSeat(winSeatList.get(0));
            }
        } else {
            //流局摸最后一张牌的玩家下局为庄
//            setLastWinSeat(moLastMajiangSeat);
            //流局庄家下家上庄
            setLastWinSeat(calcNextSeat(getLastWinSeat()));
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

        for (NyMjPlayer player : seatMap.values()) {
            if (player.isAutoPlaySelf()) {
                player.setAutoPlay(false, false);
            }

            calcOverAfterClearInfo(player);
            player.saveBaseInfo();
        }
    }

    /**
     * @param
     * @return
     * @description //上一局庄家
     * @author Guang.OuYang
     * @date 2019/12/5
     */
    public void calcSpecialMenZi(int upWinnerSeat, List<Integer> winnerSeat) {
        //2人没有跟庄
        int followMasterAddScore = followMaster && gameModel.isFollowMaster() ? gameModel.getFollowMaster() : 0;//

//        int upWinnerSeat = lastWinSeat;

        Iterator<NyMjPlayer> iterator1 = seatMap.values().iterator();
        while (iterator1.hasNext()) {
            NyMjPlayer player = iterator1.next();

            if (!winnerSeat.contains(player.getSeat())) {
                List<Mj> huCards = MjTool.getTingMjs(new ArrayList<>(player.getHandMajiang()), player.getGang(), player.getPeng(), player.getChi(),
                        player.getBuzhang(), true, gameModel.getSpecialPlay().isOnlyDaHu(), gameModel.getSpecialPlay().isQuanQiuRenJiang() ? 1 : 0, this, player);
                ArrayList<Integer> menzi = new ArrayList<>(player.getDahu());
                //十倍不计分
                boolean isNMulNotCalcScore = false;
                //死胡
                boolean isDeathHu = false;
                //死胡不做胡牌检测,不加分
                if (!huConfirmMap.containsKey(player.getSeat()) && !player.isDeathHu() && huCards.size() > 0) {
                    //如果可能,用非王牌做胡牌门子
                    int maxScore = 0;
                    MjiangHu finalHu = null;
                    Iterator<Mj> iterator = huCards.iterator();
                    while (iterator.hasNext()) {
                        Mj next = iterator.next();
                        MjiangHu mjiangHu = player.checkHu(next, isBegin);
                        int nowScore = MjiangHu.calcMenZiScore(this, MjiangHu.calcDaHuBasicBase(mjiangHu.buildDahuList()));
                        if (maxScore == 0 || nowScore > maxScore) {
                            maxScore = nowScore;
                            finalHu = mjiangHu;
                        }
                    }

                    if (gameModel.getNMulNotCalcScore() > 0 && maxScore >= gameModel.getNMulNotCalcScore()) {
//                        finalHu.setShiBeiBuJiFen(true);
                        isNMulNotCalcScore = true;
                    }

                    finalHu.initDahuList();

//                    player.setDahu(finalHu.buildDahuList());
                    menzi.addAll(finalHu.buildDahuList());

                    LogUtil.printDebug("{} 输家门子:{}", player.getName(), MjTool.dahuListToString(player.getDahu()), MjTool.dahuListToString(menzi));

                } else if (!huConfirmMap.containsKey(player.getSeat())) {
                    isDeathHu = true;
                    LogUtil.printDebug("{} 死胡:{}", MjTool.dahuListToString(player.getDahu()));
                }

                /** <p>  1.碰碰胡2.将将胡 3.七小对4.清一色 5.豪华七小对4 6.超豪华七小对  7.杠上开花 8.杠上炮 9.报听 10.抢杠胡 11.黑天胡 12.地胡 13.倒底胡 14.天胡 15天天胡4 16地地胡4 17平胡1
                 * 18硬庄 19跟庄 20十三幺  21三豪华7对 22混一色 23字一色 24十八罗汉 25一九胡 26清一九 27海底捞月 28吃杠杠爆全包 29十倍不计分 30死胡</p>*/
                //十倍不计分
                if (isNMulNotCalcScore) {
                    menzi.add(28);
                }
                //死胡
                if (isDeathHu) {
                    //死胡
                    menzi.add(29);
                }

                player.setDahu(menzi);

                LogUtil.printDebug("{} 输家门子:{}", player.getName(), MjTool.dahuListToString(player.getDahu()));
            }

            if (player.getSeat() != upWinnerSeat) {
                ArrayList<Integer> menzi = new ArrayList<>(player.getDahu());

                //跟庄
                boolean isAddGenZhuang = followMasterAddScore > 0;

                /** <p>  1.碰碰胡2.将将胡 3.七小对4.清一色 5.豪华七小对4 6.超豪华七小对  7.杠上开花 8.杠上炮 9.报听 10.抢杠胡 11.黑天胡 12.地胡 13.倒底胡 14.天胡 15天天胡4 16地地胡4 17平胡1
                 * 18硬庄 19跟庄 20十三幺  21三豪华7对 22混一色 23字一色 24十八罗汉 25一九胡 26清一九 27海底捞月 28吃杠杠爆全包 29十倍不计分 30死胡</p>*/
                //跟庄+1
                if (isAddGenZhuang) {
                    menzi.add(18);
                    player.changePoint(followMasterAddScore);
                }

                player.setDahu(menzi);

                LogUtil.printDebug("{} 输家门子:{}", player.getName(), MjTool.dahuListToString(player.getDahu()));

            } else {
                player.changePoint(-((seatMap.size() - 1) * followMasterAddScore));
            }
        }
    }

    /**
     * @param seatBirdBigMap 所有马的归属,麻将ID,中的座位号,所属座位号
     * @param winner         当前算谁的鸟
     * @param loser          加入谁买的鸟计算
     * @param reversal       反转买马结果,false非中鸟->true中鸟, 预想结果找到winner全部奖马数量+当前loser(买中winner的方位|没买中winner方位)买的马数
     * @param notSelf
     * @return
     * @description
     * @author Guang.OuYang
     * @date 2019/11/26
     */
    public int calcBirdNum(MjiangHu.BigMap<Integer, Integer, Integer> seatBirdBigMap, int winner, int loser, boolean reversal, boolean addAward, boolean notSelf) {
        int totalNum = 0;
        Iterator<MjiangHu.BigMapEntry<Integer, Integer, Integer>> iterable = seatBirdBigMap.iterable();
        while (iterable.hasNext()) {
            MjiangHu.BigMapEntry<Integer, Integer, Integer> next = iterable.next();
            //0为奖马, 获得loser的买的马中不为winner的马,或为winner的马
            if ((!addAward && next.getV() == 0)) {
                continue;
            }

            //其他人的马,赢家
            //赢家算入奖马和买马
            //奖马, 买马
            if (notSelf && ((next.getV() == winner || next.getV() == 0) && ((reversal && next.getE() == loser) || (!reversal && next.getE() != winner)))) {
                totalNum += 1;
            }

            //自己的马, 输家
            //输家仅加入买马
            //奖马, 买马
            else if (!notSelf && ((next.getV() == loser || next.getV() == 0) && ((reversal && next.getE() == loser) || (!reversal && next.getE() != winner)))) {
                totalNum += 1;
            }
        }
        return totalNum;
    }

    /**
     * @param seatBirdBigMap 所有马的归属,麻将ID,中的座位号,所属座位号(0系统奖马)
     * @return
     * @description 算杠分, 这里有马跟杠, 需要在结算时明确知道鸟数量的时候+杠分,不能提前算出来杠分
     * @author Guang.OuYang
     * @date 2019/11/21
     */
    public void calcGangScore(MjiangHu.BigMap<Integer, Integer, Integer> seatBirdBigMap, List<Integer> winnerSeats) {
        //清空杠分
        Iterator<NyMjPlayer> iterator4 = seatMap.values().iterator();
        while (iterator4.hasNext()) {
            NyMjPlayer next = iterator4.next();
            next.setGangPoint(0);
            next.setMGangScore(0);
            next.setAGangScore(0);
            next.setFGangScore(0);

            next.setMGangScoreHorseAndGang(0);
            next.setAGangScoreHorseAndGang(0);
            next.setFGangScoreHorseAndGang(0);
            next.setHorseAndGangScore(0);
//
//            //清空输家的杠
//            if (!winnerSeats.contains(next.getSeat())) {
//                next.getGangSeat().clear();
//            }
        }

        //分离出奖的马
        Map<Integer, Integer> seatBirdMap = new HashMap<>();
        Iterator<MjiangHu.BigMapEntry<Integer, Integer, Integer>> iterable = seatBirdBigMap.iterable();
        while (iterable.hasNext()) {
            MjiangHu.BigMapEntry<Integer, Integer, Integer> next = iterable.next();
            //这里仅仅加入奖马,没有买马
            if (next.getV() == 0 && next.getE() > 0) {
                seatBirdMap.merge(next.getE(), 1, (oV, nV) -> oV + nV);
            }
        }

        //算杠, 勾选马跟杠后, 额外增加马数*杠数的分数
        Iterator<NyMjPlayer> iterator = seatMap.values().iterator();
        while (iterator.hasNext()) {
            NyMjPlayer curtPlay = iterator.next();
            //赢家有大门子杠翻倍
			int gangDouble = winnerSeats.contains(curtPlay.getSeat()) ? ((gameModel.getSpecialPlay().isGangDouble() && curtPlay.getDahuCount() > 0) ? 2 : 1) : 1;

            //需要单独-3杠分的玩家
            Map<Integer, Integer> needSubScoreSeat = new HashMap<>();

            //需要单独-3杠分的玩家, 基础刚分, 展示用
            Map<Integer, Integer> needSubScoreSeatBasic = new HashMap<>(), needSubScoreSeatBasicBird = new HashMap<>();

            //暗杠一个2分
            int anScore = 0;//next.getaGang().size() * 2 * (Math.max(1,  (gameModel.isBirdAndGang() ? next.getaGang().size() * 2 * seatBirdMap.get(nowGangSeat) : 1)));
            //明杠一个*人数分
            int mingScore = 0;
            //放杠+分 一个3分
            int singleScore = 0;

            //暗杠\明杠\放杠基础分
            int anScoreBasic = 0, mingScoreBasic = 0;
            //暗杠\明杠\放杠马跟杠额外分
            int anScoreBasicBird = 0, mingScoreBasicBird = 0;

            //上一个检测过的杠val
//            int upGangVal = 0;
            Iterator<Entry<Integer, Integer>> iterator1 = curtPlay.getGangSeat().entrySet().iterator();

            while (iterator1.hasNext()) {
                Entry<Integer, Integer> next1 = iterator1.next();
//                Mj mj = Mj.ge tMajang(next1.getKey());
                //值>1000属于操作,<1000属于单独扣分的具体位置
                int singleSubSeat = next1.getValue();

                int birdNum = !seatBirdMap.containsKey(curtPlay.getSeat()) ? 0 : seatBirdMap.get(curtPlay.getSeat());
                //>1000属于需要都扣分的操作性质
                if (singleSubSeat < 1000) { //放杠
                    //马跟杠,需要+上放杠人的马分
                    int loveBirdNum = !seatBirdMap.containsKey(singleSubSeat) ? 0 : seatBirdMap.get(singleSubSeat);
                    //放杠1分
                    int basic = 3 * gangDouble * gameModel.getBasicScore();
                    int birdBasic = (gameModel.isBirdAndGang() ? basic * birdNum + basic * loveBirdNum : 0);
                    int bScore = (basic + birdBasic);
                    needSubScoreSeat.merge(singleSubSeat, bScore, (oV, nV) -> oV + nV);
                    singleScore += bScore;

                    needSubScoreSeatBasic.merge(singleSubSeat, basic, (oV, nV) -> oV + nV);
                    needSubScoreSeatBasicBird.merge(singleSubSeat, birdBasic, (oV, nV) -> oV + nV);

                } else {
                    int action = singleSubSeat - 1000;
                    if (action == MjDisAction.action_minggang) {    //明杠
                        int basic = 1 * gangDouble * gameModel.getBasicScore();
                        int birdScore = (gameModel.isBirdAndGang() ? basic * birdNum : 0);
                        //这里要全部扣分
                        mingScore += (basic + birdScore);

                        mingScoreBasic += basic;
                        mingScoreBasicBird += birdScore;
                    } else if (action == MjDisAction.action_angang) {  //暗杠
                        int basic = 2 * gangDouble * gameModel.getBasicScore();
                        int birdScore = (gameModel.isBirdAndGang() ? basic * birdNum : 0);

                        anScore += (basic + birdScore);

                        anScoreBasic += basic;
                        anScoreBasicBird += birdScore;
                    }
                }
            }
            boolean haveMingAnGang = mingScore > 0 || anScore > 0;
            boolean haveSingleGang = singleScore > 0;

            if (haveMingAnGang) {
                //处理暗杠和明杠
                Iterator<NyMjPlayer> iterator2 = seatMap.values().iterator();
                while (iterator2.hasNext()) {
                    NyMjPlayer chaosMjPlayer = iterator2.next();
                    if (chaosMjPlayer.getSeat() != curtPlay.getSeat()) {
                        chaosMjPlayer.changeGangPoint(-anScore - mingScore);

                        chaosMjPlayer.setMGangScore(chaosMjPlayer.getMGangScore() - mingScoreBasic);
                        chaosMjPlayer.setAGangScore(chaosMjPlayer.getAGangScore() - anScoreBasic);
                        chaosMjPlayer.setMGangScoreHorseAndGang(chaosMjPlayer.getMGangScoreHorseAndGang() - mingScoreBasic - mingScoreBasicBird);
                        chaosMjPlayer.setAGangScoreHorseAndGang(chaosMjPlayer.getAGangScoreHorseAndGang() - anScoreBasic - anScoreBasicBird);
                        chaosMjPlayer.setHorseAndGangScore(chaosMjPlayer.getHorseAndGangScore() - mingScoreBasicBird - anScoreBasicBird);

                        curtPlay.setAGangScore(curtPlay.getAGangScore() + anScoreBasic);
                        curtPlay.setMGangScore(curtPlay.getMGangScore() + mingScoreBasic);
                        curtPlay.setMGangScoreHorseAndGang(curtPlay.getMGangScoreHorseAndGang() + mingScoreBasic + mingScoreBasicBird);
                        curtPlay.setAGangScoreHorseAndGang(curtPlay.getAGangScoreHorseAndGang() + anScoreBasic + anScoreBasicBird);
                        curtPlay.setHorseAndGangScore(curtPlay.getHorseAndGangScore() + anScoreBasicBird + mingScoreBasicBird);

                    }
                }
            }

            if (haveSingleGang) {
                //处理放杠
                Iterator<Entry<Integer, Integer>> iterator3 = needSubScoreSeat.entrySet().iterator();
                while (iterator3.hasNext()) {
                    Entry<Integer, Integer> fangGangSeat = iterator3.next();
                    NyMjPlayer chaosMjPlayer = seatMap.get(fangGangSeat.getKey());
                    chaosMjPlayer.changeGangPoint(-fangGangSeat.getValue());

                    //杠基础分
                    int birdBasic = needSubScoreSeatBasic.get(fangGangSeat.getKey());
                    //马跟杠基础分
                    int horseAndGangBasic = needSubScoreSeatBasicBird.get(fangGangSeat.getKey());
                    //马跟杠+杠完整分
                    int totalScore = birdBasic + horseAndGangBasic;

                    chaosMjPlayer.setFGangScore(chaosMjPlayer.getFGangScore() - birdBasic);
                    chaosMjPlayer.setFGangScoreHorseAndGang(chaosMjPlayer.getFGangScoreHorseAndGang() - totalScore);
                    chaosMjPlayer.setHorseAndGangScore(chaosMjPlayer.getHorseAndGangScore() - horseAndGangBasic);

                    curtPlay.setFGangScore(curtPlay.getFGangScore() + needSubScoreSeatBasic.get(fangGangSeat.getKey()));
                    curtPlay.setFGangScoreHorseAndGang(curtPlay.getFGangScoreHorseAndGang() + totalScore);
                    curtPlay.setHorseAndGangScore(curtPlay.getHorseAndGangScore() + horseAndGangBasic);
                }
            }

            if (haveSingleGang || haveMingAnGang) {
                //杠+分
                curtPlay.changeGangPoint(((anScore + mingScore) * (seatMap.size() - 1)) + singleScore);
                curtPlay.setMGangScoreAdd(mingScore * (seatMap.size() - 1));
                curtPlay.setAGangScoreAdd(anScore * (seatMap.size() - 1));
                curtPlay.setFGangScoreAdd(singleScore);
            }
        }
    }

    /**
     * @param zhuaNiao
     * @param winSeatList
     * @param seatBirdMap          seat->中鸟数
     * @param seatBirdBigMap       麻将->谁中->谁的鸟
     * @param seatBirds
     * @param birdsIds
     * @param onlyWinnerAwardHorse 仅赢家抓系统产出的鸟
     * @return
     * @description
     * @author Guang.OuYang
     * @date 2019/11/23
     */
    public boolean calcBird(boolean zhuaNiao, List<Integer> winSeatList, Map<Integer, Integer> seatBirdMap, MjiangHu.BigMap<Integer, Integer, Integer> seatBirdBigMap, List<Integer> seatBirds, List<Integer> birdsIds, boolean onlyWinnerAwardHorse) {
		boolean res = false;
    	if (zhuaNiao) {
            // 先砸鸟
            Integer[] birdMjIds = zhuaNiao(null);

            if (birdMjIds == null || birdMjIds.length <= 0) {
                return res;
            }
			//金马翻倍
			res = Stream.of(birdMjIds).filter(v -> Stream.of(gameModel.birdSeat(0)).anyMatch(v1 -> v1 == Mj.getMajang(v).getPai())).count() == birdMjIds.length;

            List<Integer> systemBirdIds = Stream.of(birdMjIds).map(v -> v).collect(Collectors.toList());

            //系统产出的马
            systemBirdIds.forEach(v -> {
                seatBirdBigMap.put(v, 0, 0);
            });

            //所有人买的马
            //买马:开局每个人都有n个买的马,结算时根据赢家方位马中则不算马,没中则需要出赢家底分加上单个马的分数
            seatMap.values().forEach(v -> v.getBuyHorses().forEach(v1 -> {
                seatBirdBigMap.put(v1, 0, v.getSeat());
            }));

            //一炮多响
            //2.  一炮多响的情况，由放炮的人拿马，如果中对应的马，要赔更多，点炮所输分数加倍（不包含杠牌的分数），中一个马加一倍，两个马加两倍
            if (winSeatList.size() > 1) {
                winSeatList = new ArrayList<>();
                winSeatList.add(disCardSeat);
            }

            Iterator<Integer> iterator = winSeatList.iterator();
            while (iterator.hasNext()) {
                //赢家
                Integer startSeat = iterator.next();

                int curWinnerSeat = getLastWinSeat();
                int finalWinnerSeat = 1;
                while (curWinnerSeat != startSeat) {
                    curWinnerSeat = calcNextSeat(curWinnerSeat);
                    finalWinnerSeat++;
                }

                NyMjPlayer winnerPlay = seatMap.get(startSeat);

                List<Integer> allBirds = new ArrayList<>();
                //赢家算入系统奖马
                //买马
                if (!CollectionUtils.isEmpty(winnerPlay.getBuyHorses()))
                    allBirds.addAll(winnerPlay.getBuyHorses());
                //系统马
                allBirds.addAll(systemBirdIds);
                //庄家中自己
                seatBirdMap.put(startSeat, gameModel.birdIsThisSeat(finalWinnerSeat, startSeat, startSeat, allBirds.toArray(new Integer[0]), seatBirds, birdsIds, seatBirdBigMap));
                if (LogUtil.msgLog.isDebugEnabled())
                    LogUtil.printDebug("中鸟:::seat:{}->{},转换位置:{},num:{}, {}", startSeat, seatMap.get(startSeat).getName(), 1, seatBirdMap.get(startSeat), allBirds.stream().map(v -> Mj.getMajang(v)).collect(Collectors.toList()));

                int nextSeat = startSeat;
                //非赢家, 需要把自己的鸟加入庄家列表进行计算, 没有中到庄家的鸟则需要额外给分
                for (int i = 1; i <= seatMap.keySet().size(); i++) {
                    if (nextSeat != startSeat) {
                        //输家中赢家
                        Integer[] buyHorse = seatMap.get(nextSeat).getBuyHorses().toArray(new Integer[0]);
                        seatBirdMap.put(nextSeat, gameModel.birdIsThisSeat(finalWinnerSeat, nextSeat, startSeat, buyHorse, seatBirds, birdsIds, seatBirdBigMap));
                        if (LogUtil.msgLog.isDebugEnabled())
                            LogUtil.printDebug("中鸟:::seat:{}->{},转换位置:{},num:{}, {}", nextSeat, seatMap.get(nextSeat).getName(), i, seatBirdMap.get(startSeat), Arrays.asList(buyHorse).stream().map(v -> Mj.getMajang(v)).collect(Collectors.toList()));

                    }
                    nextSeat = calcNextSeat(nextSeat);
                }

                if (LogUtil.msgLog.isDebugEnabled()) {
                    for (int i = 0; i < seatBirds.size(); i++) {
                        LogUtil.printDebug("{} 中鸟 {}", seatMap.get(seatBirds.get(i)).getName(), Mj.getMajang(birdsIds.get(i)));
                    }
                }
            }

//            Iterator<NyMjPlayer> iterator3 = seatMap.values().iterator();
//            while (iterator3.hasNext()) {
//                NyMjPlayer next = iterator3.next();
//                if (winSeatList.contains(next.getSeat())) {
//                    //庄家买自己的+中的马
//                    seatBirdMap.put(next.getSeat(), calcBirdNum(seatBirdBigMap, next.getSeat(), next.getSeat(), true, true, false));
//                } else {
//                    int num = 0;
//                    Iterator<Integer> iterator2 = winSeatList.iterator();
//                    while (iterator2.hasNext()) {
//                        Integer winner = iterator2.next();
//                        //输家买庄家中的马
//                        num += calcBirdNum(seatBirdBigMap, winner, next.getSeat(), false, false, false);
//                    }
//                    seatBirdMap.put(next.getSeat(), gameModel.getSpecialPlay().getBuyBirdNum() - num);
//                }
//            }
        } else {
            for (int seat : seatMap.keySet()) {
                seatBirdMap.put(seat, 0);
            }
        }

    	return res;
    }

    /**
     * @param
     * @return
     * @description 小结算后应该清空的信息
     * @author Guang.OuYang
     * @date 2019/10/24
     */
    public void calcOverAfterClearInfo(NyMjPlayer player) {
        player.setFirstDisCard(true);
        player.setKingCardNumHuFlag(0);
        player.setFloorCardNumHuFlag(0);
        player.setFlutter(0);
        player.setSignTingPao(0);
        player.setTakeCardFlag(0);
        player.setDeathHu(false);
        player.getBuyHorses().clear();
        player.getGangSeat().clear();
        player.setFGangScoreAdd(0);
        player.setMGangScoreAdd(0);
        player.setAGangScoreAdd(0);
        player.setDaHuScore(0);
    }

    private boolean checkAuto3() {
        boolean diss = false;
        // if(autoPlayGlob==3) {
        boolean diss2 = false;
        for (NyMjPlayer seat : seatMap.values()) {
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

        LogUtil.printDebug("鸟基础分:{}", point);

        point += bird;

        LogUtil.printDebug("鸟翻倍:{}", point);
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
        point += dahuCount;

        return point;
    }

    /**
     * 计算大胡
     *
     * @return
     */
    private int calcDaHuPoint(int daHuCount) {
        int point = 6;
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
        NyMjPlayer player = seatMap.get(seat);
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
        for (NyMjPlayer player : playerMap.values()) {
            player.addRecord(logId, playBureau);
        }
        UdpLogger.getInstance().sendSnapshotLog(masterId, playLog, logRes);
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
     * 抓鸟, 给出鸟数量
     *
     * @return
     */
    private Integer[] zhuaNiao(Mj lastMaj) {
        // 先砸鸟
        int realBirdNum = leftMajiangs.size() > gameModel.getSpecialPlay().getBirdNum() ? gameModel.getSpecialPlay().getBirdNum() : leftMajiangs.size();

        if (realBirdNum < 0) {
            realBirdNum = 0;
        }
        Integer[] bird = new Integer[realBirdNum];
        for (int i = 0; i < realBirdNum; i++) {
            Mj prickbirdMajiang = null;
            if (lastMaj != null) {
                prickbirdMajiang = lastMaj;
            } else {
                prickbirdMajiang = getLeftMajiang(null);
            }

            if (prickbirdMajiang != null) {
                bird[i] = prickbirdMajiang.getId();
            } else {
                break;
            }
        }
        // 算鸟砸中谁
        return bird;
		//FIXME 金马翻倍
//        return new Integer[]{Mj.findAnyValByMj(25).getId(),Mj.findAnyValByMj(21).getId(),Mj.findAnyValByMj(11).getId(),Mj.findAnyValByMj(15).getId(),Mj.findAnyValByMj(31).getId(),Mj.findAnyValByMj(35).getId()};
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

    // public String buildExtend() {
    // JsonWrapper wrapper = new JsonWrapper("");
    // for (TjMjPlayer player : seatMap.values()) {
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
        addPlayLog(disCardRound + "_" + lastWinSeat + "_" + MjDisAction.action_dice + "_" + dealDice);

        logFaPaiTable();

        this.isBegin = true;
        for (NyMjPlayer player : seatMap.values()) {
            sendDealInfo(player);

            if (kingCard != null && floorCard != null) {
                int kingCardNum = 0;
                int floorCardNum = 0;
                for (Iterator<Mj> iterator = player.getHandMajiang().iterator(); iterator.hasNext(); ) {
                    Mj tmpMj = iterator.next();
                    if (tmpMj.getVal() == kingCard.getVal()) {
                        kingCardNum += 1;
                    } else if (tmpMj.getVal() == floorCard.getVal()) {
                        floorCardNum += 1;
                    }
                }

                //①天胡：手中有3张“王”自摸胡牌（需要满足胡牌类型），可以和其他牌型叠加如天胡带平2+1，天胡+胡牌番型（炮胡不算天胡）
                //②天天胡：手中有4张“王”自摸胡牌（需要满足胡牌类型），可以和其他牌型叠加如天天胡带平4+1，天天胡+胡牌番型（炮胡不算天天胡）
                player.setKingCardNumHuFlag(kingCardNum);

                //③地胡：手中有3张“地牌”即可胡牌，若有其他牌型算地胡+胡牌牌型（三个地牌不能拆开），（炮胡不算地胡）
                //④地地胡：手中有4张“地牌”即可胡牌，若有其他牌型算地胡+胡牌牌型，（炮胡不算地胡）
                player.setFloorCardNumHuFlag(floorCardNum);
            }
        }

//        if (checkFlutterScoreInfo()) {
//            return;
//        }

//        if (!hasXiaoHu()) {
        // 没有操作的话通知庄家出牌
        NyMjPlayer bankPlayer = seatMap.get(lastWinSeat);
        ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.req_com_kt_ask_dismajiang);// 推送庄家出牌
        bankPlayer.writeSocket(com.build());
        // isBegin = false;
//        }
    }

    /**
     * @param
     * @return
     * @description 发送玩家发牌后的操作信息
     * @author Guang.OuYang
     * @date 2019/10/21
     */
    private void sendDealInfo(NyMjPlayer tablePlayer) {
        //买马
        if (gameModel.getSpecialPlay().getBuyBirdNum() > 0) {
            for (int i = 0; i < gameModel.getSpecialPlay().getBuyBirdNum(); i++) {
                tablePlayer.getBuyHorses().add(getLeftMajiang(tablePlayer).getId());
            }
        }

        DealInfoRes.Builder res = DealInfoRes.newBuilder();
        List<Integer> actionList = tablePlayer.checkMoCard(null, true);
        if (!actionList.isEmpty() && tablePlayer.noNeedMoCard()) {
            addActionSeat(tablePlayer.getSeat(), actionList);
            res.addAllSelfAct(actionList);
        }
        res.addAllHandCardIds(tablePlayer.getHandPais());
        res.setNextSeat(getNextDisCardSeat());
        res.setGameType(getWanFa());
        res.setRemain(getLeftMajiangCount());
        res.setBanker(lastWinSeat);
        res.setDealDice(dealDice);

        //地牌
        if (floorCard != null)
            res.setDiCardId(floorCard.getId());
        //王牌
        if (kingCard != null)
            res.setKingCardId(kingCard.getId());

        //买的马
        res.addAllHorses(tablePlayer.getBuyHorses());

        logFaPaiPlayer(tablePlayer, actionList);
        tablePlayer.writeSocket(res.build());

        sendTingInfo(tablePlayer);
    }

    public void moMajiang(NyMjPlayer player, boolean isBuzhang) {
        moMajiang(player, isBuzhang, false);
    }

    /**
     * 摸牌
     *
     * @param player
     */
    public void moMajiang(NyMjPlayer player, boolean isBuzhang, boolean gangHouMo) {
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

        if (getLeftMajiangCount() == 0) {
            calcOver();
            return;
        }

        if (disCardRound != 0 && player.noNeedMoCard()) {
            return;
        }

        //漏胡：玩家有胡点过后，过手后才能炮胡，需要自己出牌才算过手
        if (player.getTakeCardFlag() > 0 && gameModel.getSpecialPlay().isPassHuLimit()) {
            player.setTakeCardFlag(0);
            player.changeExtend();
        }

        // 如果只剩下一张牌 问要不要&& isBuzhang
        if (gameModel.getSpecialPlay().isHaiDiLaoYue() && getLeftMajiangCount() == 1) {
//            calcMoLastSeats(player.getSeat());
//            sendAskLastMajiangRes(0);
//            if (moLastSeats == null || moLastSeats.size() == 0) {
//                calcOver();
//            }
//            try {
//                AbsCodeCommandExecutor.getGlobalActionCodeInstance(AbsCodeCommandExecutor.GlobalCommonIndex.COMMAND_INDEX, WebSocketMsgType.req_com_lastmajiang)
//                        .orElse(AbsCodeCommandExecutor.getGlobalActionCodeInstance(AbsCodeCommandExecutor.GlobalCommonIndex.COMMAND_INDEX, -1).get())
//                        .execute0(player, null, null, ComMsg.ComReq.newBuilder().setCode(WebSocketMsgType.req_com_lastmajiang).addParams(2).build());
//            } catch (Exception e) {
//            }
            moLastMajiang(player, 2);
            return;
        }

        if (isBuzhang) {
            addMoTailPai(-1);
        }
        // 摸牌
        nowMoCard = null;
        if (disCardRound != 0) {
            // 玩家手上的牌是双数，已经摸过牌了
            if (player.noNeedMoCard()) {
                return;
            }
            if (GameServerConfig.isDebug() && !player.isRobot()) {
                if (zpMap.containsKey(player.getUserId()) && zpMap.get(player.getUserId()) > 0) {
                    nowMoCard = MjQipaiTool.findMajiangByVal(leftMajiangs, zpMap.get(player.getUserId()));
                    if (nowMoCard != null) {
                        zpMap.remove(player.getUserId());
                        leftMajiangs.remove(nowMoCard);
                    }
                }
            }
            // 不是庄家第一次出牌
            // 不是第一次出牌 ，摸牌
            // majiang=majiangt
            // majiang = MajiangHelper.findMajiangByVal(leftMajiangs, 25);
            // leftMajiangs.remove(majiang);
            if (nowMoCard == null) {
                nowMoCard = getLeftMajiang(player);
            }
        }
        if (nowMoCard != null) {
            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + MjDisAction.action_moMjiang + "_"
                    + nowMoCard.getId());
            player.moMajiang(nowMoCard);
        }

        LogUtil.printDebug("nowMoCard::::{}:{}---{}", player.getName(), nowMoCard, getLeftMajiangs());

        processHideMj(player);

        // 检查摸牌
        clearActionSeatMap();
        if (disCardRound == 0) {
            return;
        }
        setMoMajiangSeat(player.getSeat());
        List<Integer> arr = player.checkMoCard(nowMoCard, false);

        if (!arr.isEmpty()) {
//            // 如果杠了之后，摸牌不能杠，那有杠也不能杠
//            if (!player.getGang().isEmpty() && !checkSameMj(player.getPeng(), nowMoCard)) {
//                arr.set(MjAction.MINGGANG, 0);
//                arr.set(MjAction.ANGANG, 0);
//                arr.set(MjAction.BUZHANG, 0);
//                arr.set(MjAction.BUZHANG_AN, 0);
//            }
            coverAddActionSeat(player.getSeat(), arr);

            //杠后摸得牌, 这里需要验证杠上花
            if (gangHouMo) {
                addGangActionSeat(nowMoCard.getId(), player.getSeat(), arr);
            }
        }
        MoMajiangRes.Builder res = MoMajiangRes.newBuilder();
        res.setUserId(player.getUserId() + "");
        res.setRemain(getLeftMajiangCount());
        res.setSeat(player.getSeat());

        // boolean playCommand = !player.getGang().isEmpty() && arr.isEmpty();
        logMoMj(player, nowMoCard, arr);
        for (NyMjPlayer seat : seatMap.values()) {
            if (seat.getUserId() == player.getUserId()) {
                MoMajiangRes.Builder copy = res.clone();
                copy.addAllSelfAct(arr);
                if (nowMoCard != null) {
                    copy.setMajiangId(nowMoCard.getId());
                }
                seat.writeSocket(copy.build());
            } else {
                seat.writeSocket(res.build());
            }
        }
        sendTingInfo(player);

        //最后一圈
        if (getLeftMajiangCount() == gameModel.getSpecialPlay().getFinalOneCircleCardNum()) {
            seatMap.values().forEach(v -> v.writeSocket(SendMsgUtil.buildComRes(WebSocketMsgType.res_code_chaosmj_final_circle_tips, 0).build()));
        }

    }

    public void checkMustHu(List<Integer> arr) {
        //有胡必胡, 没有其他
        if (gameModel.isMustHu() && MjAction.hasHu(arr)) {
            for (int i = 0; i < arr.size(); i++) {
                if (arr.get(i) > 0 && i != MjAction.HU && i != MjAction.ZIMO) {
                    arr.set(i, 0);
                }
            }
        }
    }

    private boolean checkSameMj(List<Mj> list, Mj majiang) {
        if (list.size() == 0) {
            return false;
        }
        for (Mj mj : list) {
            if (mj.getVal() == majiang.getVal()) {
                return true;
            }
        }
        return false;
    }

    public void calcMoLastSeats(int firstSeat) {
        for (int i = 0; i < getMaxPlayerCount(); i++) {
            NyMjPlayer player = seatMap.get(firstSeat);
//            if (player.isTingPai(-1, false)) {
            setFristLastMajiangSeat(player.getSeat());
            addMoLastSeat(player.getSeat());
//            }
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
     * @param seat 0表示推送第一个，>0表示当前推送的是自己，就推送
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
        //海底不询问
//        ChaosMjPlayer player = seatMap.get(sendSeat);
//        sendMoLast(player, 1);
    }

    private void buildPlayRes(PlayMajiangRes.Builder builder, Player player, int action, List<Mj> majiangs) {
        MjResTool.buildPlayRes(builder, player, action, majiangs);
        buildPlayRes1(builder);
    }

    private void buildPlayRes1(PlayMajiangRes.Builder builder) {
        // builder
    }

    /**
     * 胡小胡
     *
     * @param player
     * @param majiangs   小胡展示的麻将
     * @param xiaoHuType 小胡类型 TjMjAction
     * @param action
     */
    public synchronized void huXiaoHu(NyMjPlayer player, List<Mj> majiangs, int xiaoHuType, int action) {
        List<Integer> actionList = actionSeatMap.get(player.getSeat());
        if (actionList == null || actionList.isEmpty() || actionList.get(xiaoHuType) == 0) {// 不能胡该小胡
            return;
        }

        MjiangHu hu = new MjiangHu();
        List<Mj> copy2 = new ArrayList<>(player.getHandMajiang());
        MjRule.checkXiaoHu2(hu, copy2, isBegin(), this, player);

        HashMap<Integer, Map<Integer, List<Mj>>> xiaohuMap = hu.getXiaohuMap();
        Map<Integer, List<Mj>> map = xiaohuMap.get(xiaoHuType);
        if (map == null) {
            return;
        }

        List<Integer> keys = new ArrayList<Integer>();
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
        player.addXiaoHu2(xiaoHuType, huCard);

        removeActionSeat(player.getSeat());
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + MjDisAction.action_xiaohu + "_"
                + MjHelper.toMajiangStrs(majiangs) + "_" + xiaoHuType);
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        buildPlayRes(builder, player, MjDisAction.action_xiaohu, majiangs);
        builder.addHuArray(xiaoHuType);
        boolean isBegin = isBegin();
        List<Integer> selfActList = player.checkMoCard(null, isBegin);
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
        for (NyMjPlayer seat : seatMap.values()) {
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

    /**
     * 如果是起手判断是否还有人可胡小胡，检查庄家发牌后有没有操作，没有的话通知庄家出牌
     */
    public void checkBegin(NyMjPlayer player) {
//        boolean isBegin = isBegin();
//        if (isBegin && !hasXiaoHu()) {
//            TjMjPlayer bankPlayer = seatMap.get(lastWinSeat);
//            List<Integer> actList = bankPlayer.checkMoCard(null, isBegin);
//            if (!actList.isEmpty()) {
//                PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
//                buildPlayRes(builder, player, MjDisAction.action_pass, new ArrayList<>());
//                if (!actList.isEmpty()) {
//                    addActionSeat(bankPlayer.getSeat(), actList);
//                    builder.addAllSelfAct(actList);
//                }
//                bankPlayer.writeSocket(builder.build());
//            }
//            ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.req_com_kt_ask_dismajiang);// 推送庄家出牌
//            bankPlayer.writeSocket(com.build());
//        }
    }

    /**
     * @param
     * @return
     * @description 杠上开花(杠上胡)&杠上炮(杠上被抢胡)
     * @author Guang.OuYang
     * @date 2019/10/16
     */
    public MjiangHu checkGangUpFlowerOrGun(List<Integer> huMjIds, Map<Integer, MjiangHu> huMap, NyMjPlayer player, MjiangHu huBean, List<Mj> huMjs) {
        // 有大胡
        for (int mjId : huMjIds) {
            MjiangHu temp = huMap.get(mjId);
            if (moMajiangSeat == player.getSeat()) {
                if (gameModel.getSpecialPlay().isGangUpFlower()) {
                    temp.setGangShangHua(true);
                    Integer disSeatOrAction = gangMajiang == null ? null : player.getGangSeat().get(gangMajiang.getVal());
                    if (disSeatOrAction != null && disSeatOrAction < 1000) {
                        temp.setEatGangBurstAllInCharge(disSeatOrAction);
                    }
//                    temp.setZiMo(false);
//                    temp.setDianPao(false);
                }
            } else {
                if (gameModel.getSpecialPlay().isGangUpGun()) {
                    // 出掉杠牌
                    NyMjPlayer mPlayer = seatMap.get(moMajiangSeat);
                    removeGangMj(mPlayer, mjId);
                    temp.setGangShangPao(true);
                }
            }
            temp.initDahuList();
            if (huBean == null) {
                huBean = temp;
            } else {
                huBean.addToDahu(temp.getDahuList());
                huBean.getShowMajiangs().add(Mj.getMajang(mjId));
            }
            player.addHuMjId(mjId);
            huMjs.add(Mj.getMajang(mjId));
        }

        return huBean;
    }

    public MjiangHu checkGangUpFlowerOrGun(NyMjPlayer player, List<Mj> majiangs, List<Mj> huMjs) {
        MjiangHu huBean = null;
        Map<Integer, MjiangHu> huMap = new HashMap<>();
        List<Integer> daHuMjIds = new ArrayList<>();
        List<Integer> huMjIds = new ArrayList<>();

        //计算取出分数最高的
//        int mjId = majiangs.get(0).getId();
//        MjiangHu finalHu = player.checkHu(Mj.getMajang(mjId), disCardRound == 0);

        int maxScore = 0;
        int mjId = 0;
        MjiangHu finalHu = null;
        for (int majiangId : gangSeatMap.keySet()) {
            List<Integer> menzi = gangSeatMap.get(majiangId).get(player.getSeat());
            if (CollectionUtils.isEmpty(menzi)) {
                continue;
            }

            MjiangHu temp = null;
            if (gameModel.getSpecialPlay().isGangMoNum()) {
                temp = player.checkHu(Mj.getMajang(majiangId), disCardRound == 0, !isHasGangAction(player.getSeat()));
                if (!temp.isHu()) {
                    continue;
                }
                temp.setGangShangHua(true);

                menzi = temp.buildDahuList();
            } else {
                Mj mj = player.getHandMajiang().remove(player.getHandMajiang().size() - 1);

                if (mj.getId() == majiangId) {
                    temp = player.checkHu(mj, disCardRound == 0);
                    if (!temp.isHu()) {
                        continue;
                    }
                    temp.setGangShangHua(true);

                    menzi = temp.buildDahuList();
                }

                player.getHandMajiang().add(mj);
            }

            if (!CollectionUtils.isEmpty(menzi)) {
                int curScore = MjiangHu.calcMenZiScore(this, menzi);

                if (curScore > maxScore) {
                    maxScore = curScore;
                    finalHu = temp;
                    mjId = majiangId;
                }
            }
        }

        if (finalHu != null) {
            finalHu.initDahuList();
            huMap.put(mjId, finalHu);
            huMjIds.add(mjId);
            if (finalHu.isDahu()) {
                daHuMjIds.add(mjId);
            }
        }

        if (daHuMjIds.size() > 0) {
            huBean = checkGangUpFlowerOrGun(huMjIds, huMap, player, huBean, huMjs);
        } else if (huMjIds.size() > 0) {
            // 没有大胡
            huBean = checkGangUpFlowerOrGun(huMjIds, huMap, player, huBean, huMjs);
        } else {
            huBean = new MjiangHu();
        }

        return huBean;
    }

    /**
     * 玩家表示胡
     *
     * @param player
     * @param majiangs
     */
    private void hu(NyMjPlayer player, List<Mj> majiangs, int action) {
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return;
        }

        //不允许自摸胡
        if (actionSeatMap.get(player.getSeat()).get(17) == 1 && !gameModel.getSpecialPlay().isSelfMoHu()) {
            return;
        }

        if (huConfirmMap.containsKey(player.getSeat())) {
            return;
        }

//        if(hasXiaoHu()){
//        	return;
//        }

        checkRemoveMj(player, action);
        boolean zimo = player.noNeedMoCard();
        Mj disMajiang = null;
        MjiangHu huBean = null;
        List<Mj> huMjs = new ArrayList<>();
        int fromSeat = 0;
        boolean isGangShangHu = false;
//        boolean playerRobGangHu = !CollectionUtils.isEmpty(player.getDahu()) && player.getDahu().get(MjAction.QIANG_GANG_HU)==1;
        LogUtil.printDebug("{},{}表示胡牌,牌数是否14张:{}, 杠的玩家:{}", player.getName(), player.getSeat(), zimo, isHasGangAction(player.getSeat()));

        boolean isRobGangHu = false;
//        if (!zimo || !gameModel.getSpecialPlay().isGangMoNum()) {
        if (gameModel.getSpecialPlay().isRobGangHu() && moGangHuList.contains(player.getSeat())) {// 强杠胡
            disMajiang = moGang;
            fromSeat = moMajiangSeat;
            huMjs.add(moGang);
            isRobGangHu = true;
        } else if ((gameModel.getSpecialPlay().isGangUpFlower() || gameModel.getSpecialPlay().isGangUpGun()) && isHasGangAction(player.getSeat())) {// 杠上炮 杠上花

//                //杠上花必须有选择牌
//                if (/*moMajiangSeat == player.getSeat() &&*/ majiangs.isEmpty()) {
//                    return;
//                }

            fromSeat = moMajiangSeat;

            //杠上开花 , 杠上炮
            huBean = checkGangUpFlowerOrGun(player, majiangs, huMjs);

            isGangShangHu = huBean.isGangShangHua();

            if (huBean.isHu()) {
                if (disCardSeat == player.getSeat()) {
                    zimo = true;
                }
            }

        } else if (lastMajiang != null && (gameModel.getSpecialPlay().isHaiDiLaoYue() || gameModel.getSpecialPlay().isHaiDiPao())) {
//            huBean = player.checkHu(lastMajiang, disCardRound == 0);
            huBean = player.checkHu(null, disCardRound == 0);
            if (huBean.isHu()) {
                if (moLastMajiangSeat == player.getSeat()) {
                    huBean.setHaidilaoyue(gameModel.getSpecialPlay().isHaiDiLaoYue());
                } else {
                    huBean.setHaidipao(gameModel.getSpecialPlay().isHaiDiPao());
                }
                huBean.initDahuList();
            }
            fromSeat = moLastMajiangSeat;
            huMjs.add(lastMajiang);

        } else if (zimo) {
            huMjs.add(player.getHandMajiang().get(player.getHandMajiang().size() - 1));
        } else if (!nowDisCardIds.isEmpty()) {
            disMajiang = nowDisCardIds.get(0);
            fromSeat = disCardSeat;
            huMjs.add(disMajiang);
        }
//        } else {
//            huMjs.add(player.getHandMajiang().get(player.getHandMajiang().size() - 1));
//        }

        if (huBean == null) {
            // 自摸
            huBean = player.checkHu(disMajiang, disCardRound == 0);
            if (huBean.isHu() && lastMajiang != null && gameModel.getSpecialPlay().isHaiDiLaoYue()) {
                huBean.setHaidilaoyue(true);
                huBean.initDahuList();
            }
        }
        if (isRobGangHu && gameModel.getSpecialPlay().isRobGangHu()) {
            huBean.setHu(true);
            huBean.setQiangGangHu(true);
            huBean.initDahuList();
        }

        // 检查门清
        if (!huBean.isMenqing() && (gameModel.getSpecialPlay().isMenqing() && !player.isChiPengGang())) {
            huBean.setMenqing(true);
            huBean.initDahuList();
        }

        // 算牌型的分
        if (gameModel.getSpecialPlay().isRobGangHu() && moGangHuList.contains(player.getSeat())) {
            // 补张的时候不算抢杠胡
//            if (disEventAction != MjDisAction.action_buzhang) {
//                huBean.setQGangHu(true);
//                huBean.initDahuList();
//            }
            // 抢杠胡
            NyMjPlayer moGangPlayer = seatMap.get(nowDisCardSeat);
            List<Mj> moGangMajiangs = new ArrayList<>();
            moGangMajiangs.add(moGang);
            //被抢杠胡,从手牌去掉这张牌
//            moGangPlayer.removeGangMj(moGangMajiangs.get(0));
            // 摸杠被人胡了 相当于自己出了一张牌
            recordDisMajiang(moGangMajiangs, moGangPlayer);
        }

        if (isGangShangHu) {
            // 杠上花，只胡一张牌时，另外一张牌需要打出
            List<Mj> gangDisMajiangs = getGangDisMajiangs();
            List<Mj> chuMjs = new ArrayList<>();
            if (gangDisMajiangs != null && gangDisMajiangs.size() > 0) {
                for (Mj mj : gangDisMajiangs) {
                    if (!huMjs.contains(mj)) {
                        chuMjs.add(mj);
                    }
                }
            }
            if (chuMjs != null) {
                PlayMajiangRes.Builder chuPaiMsg = PlayMajiangRes.newBuilder();
                buildPlayRes(chuPaiMsg, player, MjDisAction.action_chupai, chuMjs);
                chuPaiMsg.setFromSeat(-1);
                broadMsgToAll(chuPaiMsg.build());
                player.addOutPais(chuMjs, MjDisAction.action_chupai, player.getSeat());
            }
        }

        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        buildPlayRes(builder, player, action, huBean.getShowMajiangs());


        if (zimo) {
            builder.setZimo(1);

            int kingNum = player.getKingCardNumHuFlag();//getKingCard() != null ? player.getHandMajiang().stream().filter(v -> v.getVal() == getKingCard().getVal()).mapToInt(v -> 1).sum() : 0;

            //天胡,只有王牌没有用出去算天胡
            //①天胡：手中有3张“王”自摸胡牌（需要满足胡牌类型），可以和其他牌型叠加如天胡带平2+1，天胡+胡牌番型（炮胡不算天胡）
            //②天天胡：手中有4张“王”自摸胡牌（需要满足胡牌类型），可以和其他牌型叠加如天天胡带平4+1，天天胡+胡牌番型（炮胡不算天天胡）
            if (/*player.getDahu().contains(MjAction.YING_ZHUANG) &&*/ kingNum >= gameModel.getSpecialPlay().getKingHuNum() + 1) {
                huBean.setTiantianhu(true);
//                huBean.setTianhu(true);
            } else if (/*player.getDahu().contains(MjAction.YING_ZHUANG) &&*/ kingNum >= gameModel.getSpecialPlay().getKingHuNum()) {
                huBean.setTianhu(true);
            }
        }

        LogUtil.printDebug("天胡检测:{}, {}, {}", zimo, player.getKingCardNumHuFlag(), gameModel.getSpecialPlay().getKingHuNum());

        huBean.initDahuList();

        if (!huBean.getDahuList().isEmpty()) {
            player.setDahu(huBean.getDahuList());
        }

        try {
            LogUtil.msgLog.info("nymj_room_hu:{},{},{},{},{},{},{},{},{},{},{},{}",
                    getId(),
                    getPlayBureau(),
                    disMajiang,
                    player.getSeat(),
                    player.getName(),
                    huBean,
                    player.getDahu(),
                    zimo,
                    actionSeatMap,
                    moGangHuList,
                    player.getHandMajiang(),
                    ExceptionUtils.getStackTrace(new Throwable()).trim().replace("\r\n", "/").substring(0, 480));
        } catch (Exception e) {
            LogUtil.errorLog.error("tjmj_room_hu error:{}", e);
        }

        builder.addAllHuArray(player.getDahu());

        builder.setFromSeat(fromSeat);
        // 胡
        for (NyMjPlayer seat : seatMap.values()) {
            // 推送消息
            seat.writeSocket(builder.build());
        }
        // 加入胡牌数组
        addHuList(player.getSeat(), disMajiang == null ? 0 : disMajiang.getId());
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + MjHelper.toMajiangStrs(huMjs) + "_" + StringUtil.implode(player.getDahu(), ","));
        if (isCalcOver()) {
            // 等待别人胡牌 如果都确认完了，胡
            calcOver();
        } else {
            player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip);
        }
    }

    private void checkRemoveMj(NyMjPlayer player, int action) {
        Mj mjB = null;
        for (int majiangId2 : gangSeatMap.keySet()) {
            Map<Integer, List<Integer>> map = gangSeatMap.get(majiangId2);
            List<Integer> actList = map.get(player.getSeat());
            if (actList == null) {
                continue;
            }

            if (actList.get(MjAction.MINGGANG) == 1) {
                mjB = Mj.getMajang(majiangId2);
            }
        }


        if (mjB != null) {
            // 从手牌移除掉
            List<Mj> list = new ArrayList<>();
            list.add(mjB);
            checkMoOutCard(list, player, action);
        }
    }

    private void removeGangMj(NyMjPlayer player, int mjId) {
        List<Mj> moList = new ArrayList<>();
        moList.add(Mj.getMajang(mjId));
        player.addOutPais(moList, 0, player.getSeat());
    }

    /**
     * 找出拥有这张麻将的玩家
     *
     * @param majiang
     * @return
     */
    private NyMjPlayer getPlayerByHasMajiang(Mj majiang) {
        for (NyMjPlayer player : seatMap.values()) {
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
            NyMjPlayer moGangPlayer = null;
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
        //没有一炮多响-----------------
        if (over && !gameModel.getSpecialPlay().isOneGunMultiRing()) {
            //结束
            int nextSeat = calcNextSeat(disCardSeat);
            //找出距离胡牌玩家最近的
            while (!huConfirmMap.containsKey(nextSeat)) {
                nextSeat = calcNextSeat(nextSeat);
            }

            Iterator<Entry<Integer, Integer>> iterator = huConfirmMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<Integer, Integer> next = iterator.next();
                if (nextSeat != next.getKey()) {
                    //清理其余胡牌非最终赢家门子
                    NyMjPlayer play = seatMap.get(next.getKey());
                    if (play != null && !CollectionUtils.isEmpty(play.getDahu())) {
                        play.setDahu(Collections.emptyList());
                    }
                    iterator.remove();
                }
            }
        }
        if (!over) {
            NyMjPlayer disCSMajiangPlayer = seatMap.get(disCardSeat);
            for (int huseat : huActionList) {
                if (huConfirmMap.containsKey(huseat)) {
                    if (disCardRound == 0) {
                        // 天胡
                        removeActionSeat(huseat);
                    }
                    continue;
                }
                PlayMajiangRes.Builder disBuilder = PlayMajiangRes.newBuilder();
                NyMjPlayer seatPlayer = seatMap.get(huseat);
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
    private void chiPengGang(NyMjPlayer player, List<Mj> majiangs, int action) {
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        logAction(player, action, 0, majiangs, null);

        List<Integer> huList = getHuSeatByActionMap();
        huList.remove((Object) player.getSeat());

        // 处理杠完可吃可碰又可以胡，吃碰的话那就等于过胡了
        if (nowDisCardIds.size() > 1) {
            for (Mj mj : nowDisCardIds) {
                List<Integer> hu = player.checkDisCard(mj, false, false, getKingCard() != null && mj.getVal() == getKingCard().getVal());
                if (!hu.isEmpty() && hu.get(0) == 1) {
                    // && (actionList.get(TjMjAction.HU) == 1)
                    List<Integer> actionList = actionSeatMap.get(player.getSeat());
                    if (actionList != null) {
                        actionList.set(MjAction.HU, 0);
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

        List<Mj> handMajiang = new ArrayList<>(player.getHandMajiang());
        Mj disMajiang = null;
        if (isHasGangAction() && gameModel.getSpecialPlay().isGangMoNum()) {
            for (int majiangId : gangSeatMap.keySet()) {
                if (action == MjDisAction.action_chi) {
                    List<Integer> majiangIds = MjHelper.toMajiangIds(majiangs);
                    if (majiangIds.contains(majiangId)) {
                        disMajiang = Mj.getMajang(majiangId);
                        gangActedMj = disMajiang;
                        handMajiang.add(disMajiang);
                        if (majiangs.size() > 1) {
                            majiangs.remove(disMajiang);
                        }
                        break;
                    }
                } else {
                    Mj mj = Mj.getMajang(majiangId);
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
            sameCount = MjHelper.getMajiangCount(majiangs, majiangs.get(0).getVal());
        }
        if (action == MjDisAction.action_buzhang) {
            if (sameCount == 0) {
                majiangs.add(disMajiang);
            }
            majiangs = MjHelper.getMajiangList(player.getHandMajiang(), majiangs.get(0).getVal());
            sameCount = majiangs.size();
            if (sameCount == 0) {
                majiangs.add(disMajiang);
            }
        } else if (action == MjDisAction.action_minggang) {
            if (majiangs.size() == 0) {
                majiangs.add(disMajiang);
            }
            // 如果是杠 后台来找出是明杠还是暗杠
            majiangs = MjHelper.getMajiangList(player.getHandMajiang(), majiangs.get(0).getVal());
            sameCount = majiangs.size();
            if (sameCount == 4) {
                // 有4张一样的牌是暗杠
                action = MjDisAction.action_angang;
            } else if (sameCount == 0) {
                majiangs.add(disMajiang);
            }
            // 其他是明杠

        } else if (action == MjDisAction.action_buzhang_an) {
            // 暗杠补张
            majiangs = MjHelper.getMajiangList(player.getHandMajiang(), majiangs.get(0).getVal());
            sameCount = majiangs.size();
        }
        // /////////////////////
        if (action == MjDisAction.action_chi) {
            boolean can = canChi(player, player.getHandMajiang(), majiangs, disMajiang);
            if (!can) {
                return;
            }
        } else if (action == MjDisAction.action_peng) {
            boolean can = canPeng(player, majiangs, sameCount, disMajiang);
            if (!can) {
                return;
            }
        } else if (action == MjDisAction.action_angang) {
            boolean can = canAnGang(player, majiangs, sameCount, action);
            if (!can) {
                player.writeErrMsg("不能杠此张牌");
                return;
            }
            // 如果只剩下一张牌 问要不要&& isBuzhang
            if (getLeftMajiangCount() == -1) {
                player.writeErrMsg("海底不能杠");
                return;
            }
            if (gameModel.isNeedTingGang() && !player.isTingPai(majiangs.get(0).getVal(), true)) {
                player.writeErrMsg("不能杠此张牌");
                return;
            }
        } else if (action == MjDisAction.action_minggang) {
            boolean can = canMingGang(player, player.getHandMajiang(), majiangs, sameCount, disMajiang);
            if (!can) {
                player.writeErrMsg("不能杠此张牌");
                return;
            }
            if (gameModel.isNeedTingGang() && !player.isTingPai(majiangs.get(0).getVal(), true)) {
                player.writeErrMsg("不能杠此张牌");
                return;
            }
            // 如果只剩下一张牌 问要不要&& isBuzhang
            if (getLeftMajiangCount() == -1) {
                player.writeErrMsg("海底不能杠");
                return;
            }
            // 特殊处理一张牌明杠的时候别人可以 胡
            if (sameCount == 1 && canGangHu()) {
                if (checkQGangHu(player, majiangs, action)) {
                    // return;
                    moMj = false;
                }
            }
        } else if (action == MjDisAction.action_buzhang) {
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
        } else if (action == MjDisAction.action_buzhang_an) {
            boolean can = false;
            if (sameCount == 4) {
                can = canAnGang(player, majiangs, sameCount, action);
            }
            // 如果只剩下一张牌 问要不要&& isBuzhang
            if (getLeftMajiangCount() == 1) {
                player.writeErrMsg("海底不能补");
                return;
            }

            if (!can) {
                return;
            }
        } else {
            return;
        }
        calcGangScore(player, action, sameCount, majiangs, !moMj);
        boolean disMajiangMove = false;
        if (disMajiang != null) {
            // 碰或者杠
            if (action == MjDisAction.action_minggang && sameCount == 3) {
                // 接杠
                disMajiangMove = true;
            } else if (action == MjDisAction.action_chi) {
                // 吃
                disMajiangMove = true;
            } else if (action == MjDisAction.action_peng) {
                // 碰
                disMajiangMove = true;
            } else if (action == MjDisAction.action_buzhang && sameCount == 3) {
                // 自己三张补张
                disMajiangMove = true;
            }
        }
        if (disMajiangMove) {
            if (action == MjDisAction.action_chi) {
                majiangs.add(1, disMajiang);// 吃的牌放第二位
            } else {
                majiangs.add(disMajiang);
            }
            builder.setFromSeat(disCardSeat);
            List<Mj> disMajiangs = new ArrayList<>();
            disMajiangs.add(disMajiang);
            seatMap.get(disCardSeat).removeOutPais(disMajiangs, action);
        }
        chiPengGang(builder, player, majiangs, action, moMj);
    }

    private void chiPengGang(PlayMajiangRes.Builder builder, NyMjPlayer player, List<Mj> majiangs, int action,
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

        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + MjHelper.toMajiangStrs(majiangs));
        // 不是普通出牌
        setNowDisCardSeat(player.getSeat());
        checkClearGangDisMajiang();
        if (action == MjDisAction.action_chi || action == MjDisAction.action_peng) {
            List<Integer> arr = player.checkMoCard(null, false);
            // 吃碰之后还有操作
            if (!arr.isEmpty()) {
                arr.set(MjAction.ZIMO, 0);
                arr.set(MjAction.HU, 0);
                arr.set(MjAction.ZHONGTULIULIUSHUN, 0);
                arr.set(MjAction.ZHONGTUSIXI, 0);
                addActionSeat(player.getSeat(), arr);
            }
        }
        for (NyMjPlayer seatPlayer : seatMap.values()) {
            // 推送消息
            PlayMajiangRes.Builder copy = builder.clone();
            if (actionSeatMap.containsKey(seatPlayer.getSeat())) {
                copy.addAllSelfAct(actionSeatMap.get(seatPlayer.getSeat()));
            }
            seatPlayer.writeSocket(copy.build());
        }

        // 取消漏炮
        player.setPassMajiangVal(0);
        if (action == MjDisAction.action_minggang || action == MjDisAction.action_angang) {
            // 明杠和暗杠摸牌
            if (moMj) {
                //杠后如果不摸牌, 则默认补一张
                if (gameModel.getSpecialPlay().isGangMoNum()) {
                    gangMoMajiang(player, majiangs.get(0), disEventAction);
                } else {
                    setGangMajiang(majiangs.get(0));
                    moMajiang(player, false, true);
                }
//                gangMoMajiang(player, majiangs.get(0), action);
            }

        } else if (action == MjDisAction.action_buzhang) {
            // 补张
            if (moMj) {
                moMajiang(player, true);
            }

        } else if (action == MjDisAction.action_buzhang_an) {
            // 补张
            moMajiang(player, true);

        }

        if (action == MjDisAction.action_chi || action == MjDisAction.action_peng) {
            sendTingInfo(player);
        }

        setDisEventAction(action);
        robotDealAction();
        logAction(player, action, 0, majiangs, removeActList);
    }

    /**
     * 杠后摸两张牌
     */
    private void gangMoMajiang(NyMjPlayer player, Mj gangMajiang, int action) {
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
        if (getLeftMajiangCount() == 0) {
            calcOver();
            return;
        }

        // 连摸两张牌
        int moNum = 2;

        if (gameModel.getSpecialPlay().isGangMoNum()) {
            moNum = gameModel.getSpecialPlay().getGangMoNum();
        }

        List<Mj> moList = new ArrayList<>();

        if (gameModel.getSpecialPlay().isGangMoNum()) {
            Random r = new Random();
            gangDice = (r.nextInt(6) + 1) * 10 + (r.nextInt(6) + 1);
        }

//        int leftMjCount = getLeftMajiangCount();
//        int leftDuo = leftMjCount % 2 == 0 ? leftMjCount / 2 : leftMjCount / 2 + 1;
//        if (leftDuo >= gangDice / 10 + gangDice % 10) {
//            if (GameServerConfig.isDeveloper()) {
//                Mj majiang1 = MjHelper.findMajiangByVal(leftMajiangs, 25);
//                if (majiang1 != null) {
//                    leftMajiangs.remove(majiang1);
//                    moList.add(majiang1);
//                }
//                Mj majiang2 = MjHelper.findMajiangByVal(leftMajiangs, 22);
//                if (majiang2 != null) {
//                    leftMajiangs.remove(majiang2);
//                    moList.add(majiang2);
//                }
//                if (gameModel.getSpecialPlay().isGangMoNum()) {
//                    Mj majiang3 = MjHelper.findMajiangByVal(leftMajiangs, 28);
//                    if (majiang3 != null) {
//                        leftMajiangs.remove(majiang3);
//                        moList.add(majiang3);
//                    }
//                    Mj majiang4 = MjHelper.findMajiangByVal(leftMajiangs, 28);
//                    if (majiang4 != null) {
//                        leftMajiangs.remove(majiang4);
//                        moList.add(majiang4);
//                    }
//                }
//            }

        while (moList.size() < moNum) {
            Mj majiang = getLeftMajiang(player);
            if (majiang != null) {
                moList.add(majiang);
            } else {
                break;
            }
        }
        addMoTailPai(gangDice);
//        }

        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + MjDisAction.action_moGangMjiang + "_" + gangDice
                + "_" + MjHelper.implodeMajiang(moList, ","));

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
        Mj moGangMj = null;
        boolean next = true;
        // 摸了牌后可以胡牌
        for (Mj majiang : moList) {
            for (NyMjPlayer seatPlayer : seatMap.values()) {
                List<Integer> actionList = seatPlayer.checkDisCard(majiang, false, true, seatPlayer.getSeat() != player.getSeat());
                if (seatPlayer.getSeat() == player.getSeat()) {
                    // 摸杠人只能胡
                    if (MjAction.hasHu(actionList)) {
                        boolean addGang = false;
                        if (MjAction.hasGang(actionList)) {
                            addGang = true;
                        }
                        actionList = MjAction.keepHu(actionList);
                        actionList.set(MjAction.HU, 0);
                        actionList.set(MjAction.ZIMO, 1);
                        if (addGang) {
                            actionList.set(MjAction.MINGGANG, 1);
                            actionList.set(MjAction.BUZHANG, 1);
                            moGangMj = majiang;
                            // seatPlayer.moMajiang(majiang);
                        }
                        canHu = true;
                        addActionSeat(player.getSeat(), actionList);
                        List<Integer> list2 = new ArrayList<Integer>(actionList);
                        addGangActionSeat(majiang.getId(), player.getSeat(), list2);
                        logAction(player, action, -1, Arrays.asList(majiang), actionList);
                    } else if (MjAction.hasGang(actionList)) {
                        actionList = MjAction.keepHu(actionList);
                        actionList.set(MjAction.MINGGANG, 1);
                        actionList.set(MjAction.BUZHANG, 1);
                        moGangMj = majiang;
                        // seatPlayer.moMajiang(majiang);
                        addActionSeat(player.getSeat(), actionList);
                        List<Integer> list2 = new ArrayList<Integer>(actionList);
                        addGangActionSeat(majiang.getId(), player.getSeat(), list2);
                        logAction(seatPlayer, action, -1, Arrays.asList(majiang), actionList);
                    }
                } else {
                    if (next && !actionList.isEmpty()) {
                        next = checkDisCardAfter(player, Arrays.asList(majiang), true, seatPlayer, actionList);
                        if (actionList.contains(1)) {
                            addActionSeat(seatPlayer.getSeat(), actionList);
                            List<Integer> list2 = new ArrayList<Integer>(actionList);
                            addGangActionSeat(majiang.getId(), seatPlayer.getSeat(), list2);
                            logAction(seatPlayer, action, -1, Arrays.asList(majiang), actionList);
                        }
                    }
                }
            }
        }

        if (moGangMj != null) {
            player.moMajiang(moGangMj);
        }

        if (isHasGangAction(player.getSeat())) {
            //不能胡
            if (!canHu) {
                //出杠后摸的牌
                gangNoticePlayer(player, gangMajiang, moList);
                //
                for (Mj moMj : moList) {
                    Map<Integer, List<Integer>> seatActionList = gangSeatMap.get(moMj.getId());
                    if (seatActionList != null && seatActionList.containsKey(player.getSeat())) {
                        continue;
                    }
                    List<Mj> list = new ArrayList<>();
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
                for (Mj moMj : moList) {
                    GangPlayMajiangRes.Builder playerMsg = GangPlayMajiangRes.newBuilder();
                    playerMsg.setMajiangId(moMj.getId());
                    Map<Integer, List<Integer>> seatActionList = gangSeatMap.get(moMj.getId());

                    if (seatActionList != null && seatActionList.containsKey(player.getSeat())) {
                        playerMsg.addAllSelfAct(seatActionList.get(player.getSeat()));
                    }
                    gangMsg.addGangActs(playerMsg);
                }
                player.writeSocket(gangMsg.build());

                for (NyMjPlayer seatPlayer : seatMap.values()) {
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

            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + 0 + "_" + MjHelper.toMajiangStrs(moList));
            gangNoticePlayer(player, gangMajiang, moList);

            PlayMajiangRes.Builder chuPaiMsg = PlayMajiangRes.newBuilder();
            buildPlayRes(chuPaiMsg, player, MjDisAction.action_chupai, moList);
            for (NyMjPlayer seatPlayer : seatMap.values()) {
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

    private void checkMoOutCard(List<Mj> list, NyMjPlayer player, int action) {

        player.addOutPais(list, 0, player.getSeat());
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + 0 + "_" + MjHelper.toMajiangStrs(list));
        logAction(player, action, 0, list, null);
        PlayMajiangRes.Builder chuPaiMsg = PlayMajiangRes.newBuilder();
        buildPlayRes(chuPaiMsg, player, MjDisAction.action_chupai, list);
        for (NyMjPlayer seatPlayer : seatMap.values()) {
            chuPaiMsg.setFromSeat(-1);
            seatPlayer.writeSocket(chuPaiMsg.build());
        }
    }

    private void gangNoticePlayer(NyMjPlayer player, Mj gangMajiang, List<Mj> moList) {
        // 发送摸牌消息res
        GangMoMajiangRes.Builder gangMsg = null;
        for (NyMjPlayer seatPlayer : seatMap.values()) {
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
            for (Mj majiang : moList) {
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

    private boolean checkQGangHu(NyMjPlayer player, List<Mj> majiangs, int action) {
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        Map<Integer, List<Integer>> huListMap = new HashMap<>();
        for (NyMjPlayer seatPlayer : seatMap.values()) {
            if (seatPlayer.getUserId() == player.getUserId()) {
                continue;
            }
            // 推送消息
            List<Integer> hu = seatPlayer.checkDisCard(majiangs.get(0), false, true, getKingCard() != null && majiangs.get(0).getVal() == getKingCard().getVal());
            hu = MjAction.keepHu(hu);
            if (!hu.isEmpty() && hu.get(0) == 1) {
                addActionSeat(seatPlayer.getSeat(), hu);
                huListMap.put(seatPlayer.getSeat(), hu);
                nowGangSeat = player.getSeat();
            }
        }

        // 可以胡牌
        if (!huListMap.isEmpty()) {
            setDisEventAction(action);
            setMoGang(majiangs.get(0), new ArrayList<>(huListMap.keySet()));
            buildPlayRes(builder, player, action, majiangs);
            for (Entry<Integer, List<Integer>> entry : huListMap.entrySet()) {
                PlayMajiangRes.Builder copy = builder.clone();
                NyMjPlayer seatPlayer = seatMap.get(entry.getKey());
                copy.addAllSelfAct(entry.getValue());
                seatPlayer.writeSocket(copy.build());
            }
            return true;
        }
        return false;

    }

    public void checkSendGangRes(Player player) {
        if (isHasGangAction()) {
            List<Mj> moList = getGangDisMajiangs();
            NyMjPlayer disPlayer = seatMap.get(disCardSeat);
            GangMoMajiangRes.Builder gangbuilder = GangMoMajiangRes.newBuilder();
            gangbuilder.setGangId(gangMajiang.getId());
            gangbuilder.setUserId(disPlayer.getUserId() + "");
            gangbuilder.setName(disPlayer.getName() + "");
            gangbuilder.setSeat(disPlayer.getSeat());
            gangbuilder.setRemain(getLeftMajiangCount());
            gangbuilder.setReconnect(1);
            gangbuilder.setDice(gangDice);
            gangbuilder.setHasAct(isHasGangAction() ? 1 : 0);
            gangbuilder.setMjNum(moList.size());
            for (Mj mj : moList) {
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
    private void disCard(NyMjPlayer player, List<Mj> majiangs, int action) {
        if (!gameModel.signFlutterAllOver()) {
            LogUtil.e(player.getName() + "报听没有选择！");
            return;
        }

        //打的是王牌,同时手里存在非王牌, 只有手里就剩余王牌时限制王牌不能打
		if (cantDisKingCard(player, majiangs)) {
            LogUtil.e(player.getName() + "不能出手王牌！");
            player.writeErrMsg("王牌不能打出!");
            return;
        }

        if (majiangs.size() != 1) {
            return;
        }
        if (!player.noNeedMoCard()) {
            // 还没有摸牌
            return;
        }
        if (!tempActionMap.isEmpty() && player.getGang().isEmpty()) {
            LogUtil.e(player.getName() + "出牌清理临时操作！");
            clearTempAction();
        }

        if (!CollectionUtils.isEmpty(majiangs)) {
            player.setPassMajiangVal(majiangs.get(0).getVal());
        }

        if (!player.getGang().isEmpty() && gameModel.getSpecialPlay().isGangMoNum()) {
            // 已经杠过了牌
            if (player.getLastMoMajiang().getId() != majiangs.get(0).getId()) {
                return;
            }
        }
        if (!actionSeatMap.isEmpty() && player.getGang().isEmpty()) {// 出牌自动过掉手上操作
            guo(player, null, MjDisAction.action_pass);
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
        boolean next = true;
        for (NyMjPlayer seat : seatMap.values()) {
            List<Integer> list = new ArrayList<>();
            if (next && seat.getUserId() != player.getUserId()) {
                //其他人可做操作
                list = seat.checkDisCard(majiangs.get(0));
                next = checkDisCardAfter(player, majiangs, next, seat, list);
            }
        }

        setDisEventAction(action);
        //给所有人广播当前可以做的操作
        sendDisMajiangAction(builder);
        // 取消漏炮
        player.setPassMajiangVal(0);

        if (!CollectionUtils.isEmpty(majiangs)) {
            player.setPassMajiangVal(majiangs.get(0).getVal());
        }

        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + MjHelper.toMajiangStrs(majiangs));
        setIsBegin(false);
        //首次出牌
        player.setFirstDisCard(false);
        //重发听牌消息
        sendTingInfo(player);
        // 给下一家发牌
        checkMo();
    }

	private boolean cantDisKingCard(NyMjPlayer player, List<Mj> majiangs) {
		return !gameModel.isCanOutKing() && MjRule.isKingCard(majiangs.get(0), getKingCard()) && player.getHandMajiang().stream().anyMatch(v -> !MjRule.isKingCard(v, getKingCard()));
	}

	/**
     * @param otherSeat
     * @param majiangs   打出的牌
     * @param next       一炮多响 是否继续监测
     * @param selfSeat   出牌者
     * @param actionList
     * @return
     * @description
     * @author Guang.OuYang
     * @date 2019/11/11
     */
    private boolean checkDisCardAfter(NyMjPlayer otherSeat, List<Mj> majiangs, boolean next, NyMjPlayer selfSeat, List<Integer> actionList) {
        if (actionList.contains(1)) {
            LogUtil.printDebug("{}玩家可做的操作1:{}", selfSeat.getName(), actionList);

            //漏胡：玩家有胡点过后，过手后才能炮胡，需要自己摸牌才算过手
            //抢杠胡不需要过手
            //门子分数一旦>上一次过掉的牌,那么允许点炮胡牌
            List<Integer> pActions = selfSeat.checkHu(majiangs.get(0), isBegin).buildDahuList();
            if (actionList.get(MjAction.QIANG_GANG_HU) != 1 && MjiangHu.calcMenZiScore(this, pActions)/*player.getDahuScore()*/ <= selfSeat.getTakeCardFlag())
                if (gameModel.getSpecialPlay().isPassHuLimit() && (actionList.get(MjAction.HU) == 1 || /*actionList.get(MjAction.QIANG_GANG_HU) == 1 ||*/ actionList.get(MjAction.PENG) == 1 || actionList.get(MjAction.CHI) == 1)
                        && selfSeat.getTakeCardFlag() > 0) {
                    actionList.set(MjAction.HU, 0);
                    actionList.set(MjAction.QIANG_GANG_HU, 0);
                    actionList.set(MjAction.PENG, 0);
                    actionList.set(MjAction.CHI, 0);
                }

            //最后一圈不能碰：最后底牌需要留8张牌，最后12张的时候算最后一圈
			if (isFinalOneCircle() && actionList.get(MjAction.PENG) == 1) {
                actionList.set(MjAction.PENG, 0);
            }

			//王牌不能碰,不能杠
//			if(majiangs.stream().anyMatch(v -> MjRule.isKingCard(v, kingCard))){
//				actionList.set(MjAction.PENG,0);
//				actionList.set(MjAction.ANGANG,0);
//				actionList.set(MjAction.MINGGANG,0);
//			}

            //补张开关
            if (!gameModel.getSpecialPlay().isCanBuCard()) {
                actionList.set(MjAction.BUZHANG, 0);
            }

            //7小对听牌不能杠
            if (!gameModel.getSpecialPlay().isXiaoDuiGang() && actionList.get(MjAction.XIAO_DUI) == 1) {
                actionList.set(MjAction.MINGGANG, 0);
                actionList.set(MjAction.ANGANG, 0);
                actionList.set(MjAction.BUZHANG, 0);
            }

            //鸡胡可吃胡：鸡胡可接炮
            //鸡胡不能吃胡：鸡胡不能接炮
            //所有10倍以上牌型听牌了可以接炮，如果没人放炮，自己又没摸到，被别人胡了之后不用扣分。
            if (actionList.get(MjAction.HU) == 1 /*&& actionList.get(MjAction.PING_HU) == 1*/ && (!gameModel.isIgnite() /*|| !gameModel.canIgnite(MjiangHu.calcMenZiScore(this, MjiangHu.calcDaHuBasicBase(pActions)))*/)) {
                //111
                actionList.set(MjAction.HU, 0);
                actionList.set(MjAction.PING_HU, 0);
            }

            //黑天胡不能进行炮胡
            if (actionList.get(MjAction.HU) == 1 && gameModel.getSpecialPlay().isBlackSkyHu() && actionList.get(MjAction.BLACK_SKY_HU) == 1) {
                actionList.set(MjAction.HU, 0);
                actionList.set(MjAction.BLACK_SKY_HU, 0);
            }

            //报听后除了杠和胡,其他操作都不进行
//            if (selfSeat.getFlutterTypeBool()) {
//                for (int i = 0; i < actionList.size(); i++) {
//                    if (actionList.get(i) > 0 && gameModel.getSpecialPlay().signTingNotToDoAction(i)) {
//                        actionList.set(i, 0);
//                    }
//                }
//            }

            //有胡必胡, 没有其他
            checkMustHu(actionList);

            //清理临时操作
            actionList.set(MjAction.BLACK_SKY_HU, 0);
            actionList.set(MjAction.PING_HU, 0);
            actionList.set(MjAction.YING_ZHUANG, 0);
            actionList.set(MjAction.QIANG_GANG_HU, 0);
            actionList.set(MjAction.XIAO_DUI, 0);

            addActionSeat(selfSeat.getSeat(), actionList);
            logChuPaiActList(selfSeat, majiangs.get(0), actionList);
            LogUtil.printDebug("{},玩家可做的操作2:{}", selfSeat.getName(), actionList);
        }

        //有人接炮, 检测有没有一炮多响
        if (!gameModel.getSpecialPlay().isOneGunMultiRing() && !actionList.isEmpty() && actionList.get(MjAction.HU) == 1) {
            next = false;
        }
        return next;
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
            if (actionList.get(MjAction.HU) == 1 || actionList.get(MjAction.ZIMO) == 1) {
                // 胡
                huList.add(seat);
            }

        }
        return huList;
    }

    /**
     * @param
     * @return
     * @description 给所有人广播当前可以做的操作
     * @author Guang.OuYang
     * @date 2019/10/21
     */
    private void sendDisMajiangAction(PlayMajiangRes.Builder builder) {
        // 如果有人可以胡 优先胡
        // 把胡的找出来
        buildPlayRes1(builder);
        List<Integer> huList = getHuSeatByActionMap();
        if (huList.size() > 0) {
            // 有人胡,优先胡
            for (NyMjPlayer seatPlayer : seatMap.values()) {
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
            for (NyMjPlayer seat : seatMap.values()) {
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

    private void err(NyMjPlayer player, int action, String errMsg) {
        LogUtil.e("play:tableId-->" + id + " playerId-->" + player.getUserId() + " action-->" + action + " err:"
                + errMsg);
    }

    /**
     * 出牌
     *
     * @param player   出牌玩家
     * @param majiangs
     * @param action   出牌指令
     */
    public synchronized void playCommand(NyMjPlayer player, List<Mj> majiangs, int action) {
        if (!moGangHuList.isEmpty()) {// 被人抢杠胡, 弯杠（可抢杠胡）：玩家先碰，然后摸到一张碰了的牌。
            if (!moGangHuList.contains(player.getSeat())) {
                return;
            }
        }

        if (MjDisAction.action_hu == action) {
            hu(player, majiangs, action);
            return;
        }

        // 手上没有要出的麻将
        if (!isHasGangAction() && action != MjDisAction.action_minggang && action != MjDisAction.action_buzhang)
            if (!player.getHandMajiang().containsAll(majiangs)) {
                err(player, action, "没有找到出的牌" + majiangs);
                return;
            }

//        if (player.getFlutterTypeBool() && gameModel.getSpecialPlay().signTingNotToDoActionDis(action) && nowMoCard != null && !majiangs.isEmpty() && majiangs.get(0).getId() != nowMoCard.getId()) {
//            err(player, action, "报听后不能出其他牌" + majiangs);
//            return;
//        }

        //跟庄+n
        if (gameModel.isFollowMaster() && disCardRound > 0 && disCardRound < seatMap.size()) {
            followMaster = !CollectionUtils.isEmpty(majiangs) && !CollectionUtils.isEmpty(nowDisCardIds) && majiangs.get(0).getVal() == nowDisCardIds.get(0).getVal();
        }

        changeDisCardRound(1);

        if (action == MjDisAction.action_pass) {
            guo(player, majiangs, action);
        } else if (action == MjDisAction.action_moMjiang) {
        } else if (action != 0) {
            if (hasXiaoHu()) {
                return;
            }
            chiPengGang(player, majiangs, action);
        } else {
            if (isBegin() && hasXiaoHu()) {
                return;
            }
            disCard(player, majiangs, action);
        }

    }

    /**
     * 最后一张牌(海底捞)
     *
     * @param player
     * @param action
     */
    public synchronized void moLastMajiang(NyMjPlayer player, int action) {
        if (getLeftMajiangCount() != 1) {
            return;
        }
//        if (player.getSeat() != askLastMajaingSeat) {
//            return;
//        }

        if (action == MjDisAction.action_passmo) {
            // 发送下一个海底摸牌res
            sendMoLast(player, 0);
            removeMoLastSeat(player.getSeat());
            if (moLastSeats == null || moLastSeats.size() == 0) {
                calcOver();
                return;
            }
            sendAskLastMajiangRes(0);
            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + MjDisAction.action_pass + "_");
        } else {
            sendMoLast(player, 0);
            clearMoLastSeat();
            clearActionSeatMap();
            setMoLastMajiangSeat(player.getSeat());
            Mj majiang = getLeftMajiang(player);
            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + MjDisAction.action_moLastMjiang + "_"
                    + majiang.getId());
            setMoMajiangSeat(player.getSeat());
            player.setPassMajiangVal(0);
            setLastMajiang(majiang);
            setDisCardSeat(player.getSeat());

            // /////////////////////////////////////////////
            // 发送海底捞的牌

            // /////////////////////////////////////////

            List<Mj> disMajiangs = new ArrayList<>();
            disMajiangs.add(majiang);

            MoMajiangRes.Builder moRes = MoMajiangRes.newBuilder();
            moRes.setUserId(player.getUserId() + "");
            moRes.setRemain(getLeftMajiangCount());
            moRes.setSeat(player.getSeat());

            // 先看看自己能不能胡
//            List<Integer> selfActList = player.checkDisCard(majiang);
            player.moMajiang(majiang);
            List<Integer> selfActList = player.checkMoCard(majiang, isBegin);
            selfActList = MjAction.keepHu(selfActList);
            if (selfActList != null && !selfActList.isEmpty()) {
                if (selfActList.contains(1)) {
                    addActionSeat(player.getSeat(), selfActList);
                }
            }
            for (NyMjPlayer seatPlayer : seatMap.values()) {
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

            //没有海底炮
            // 自己能胡
            if (MjAction.hasHu(selfActList)) {
                // 优先自己胡
                // hu(player, null, TjMjDisAction.action_moLastMjiang_hu);
                return;
            } else if (gameModel.getSpecialPlay().isHaiDiPao()) {
                chuLastPai(player);
            } else { //自己不能胡直接结算
                calcOver();
            }
            //-----------
            // for (int seat : actionSeatMap.keySet()) {
            // hu(seatMap.get(seat), null, action);
            // }
        }

    }

    private void chuLastPai(NyMjPlayer player) {
        Mj majiang = lastMajiang;
        List<Mj> disMajiangs = new ArrayList<>();
        disMajiangs.add(majiang);
        PlayMajiangRes.Builder chuRes = MjResTool.buildPlayRes(player, MjDisAction.action_chupai, disMajiangs);
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + MjDisAction.action_chupai + "_"
                + MjHelper.toMajiangStrs(disMajiangs));
        setNowDisCardIds(disMajiangs);
        setNowDisCardSeat(calcNextSeat(player.getSeat()));
        recordDisMajiang(disMajiangs, player);
        player.addOutPais(disMajiangs, MjDisAction.action_chupai, player.getSeat());
        player.clearPassHu();
        for (NyMjPlayer seatPlayer : seatMap.values()) {
            if (seatPlayer.getUserId() == player.getUserId()) {
                seatPlayer.writeSocket(chuRes.clone().build());
                continue;
            }
            List<Integer> otherActList = seatPlayer.checkDisCard(majiang);
            otherActList = MjAction.keepHu(otherActList);
            PlayMajiangRes.Builder msg = chuRes.clone();
            if (MjAction.hasHu(otherActList)) {
                addActionSeat(seatPlayer.getSeat(), otherActList);
                msg.addAllSelfAct(otherActList);
            }
            seatPlayer.writeSocket(msg.build());
        }
        if (actionSeatMap.isEmpty()) {
            calcOver();
        }
    }

    private void passMoHu(NyMjPlayer player, List<Mj> majiangs, int action) {
        if (!moGangHuList.contains(player.getSeat())) {
            return;
        }

        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        buildPlayRes(builder, player, action, majiangs);
        builder.setSeat(nowDisCardSeat);
        removeActionSeat(player.getSeat());
        player.writeSocket(builder.build());
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + MjHelper.toMajiangStrs(majiangs));
        if (isCalcOver()) {
            calcOver();
            return;
        }
        player.setPassMajiangVal(nowDisCardIds.get(0).getVal());

        NyMjPlayer moGangPlayer = seatMap.get(nowGangSeat);
        if (moGangHuList.isEmpty()) {
            majiangs = new ArrayList<>();
            majiangs.add(moGang);
            if (disEventAction == MjDisAction.action_buzhang) {
                moMajiang(moGangPlayer, true);
            } else {
//                //杠后如果不摸牌, 则默认补一张
//                if(gameModel.getSpecialPlay().isGangMoNum()){
//                    gangMoMajiang(moGangPlayer, majiangs.get(0), disEventAction);
//                }else{
//                    moMajiang(moGangPlayer, true);
//                }
                gangMoMajiang(moGangPlayer, majiangs.get(0), disEventAction);
            }

            // calcGangScore(moGangPlayer, TjMjDisAction.action_minggang, 1,
            // majiangs);
            // builder = PlayMajiangRes.newBuilder();
            // chiPengGang(builder, moGangPlayer, majiangs,
            // TjMjDisAction.action_minggang,true);
        }

    }

    /**
     * guo
     *
     * @param player
     * @param majiangs
     * @param action
     */
    private void guo(NyMjPlayer player, List<Mj> majiangs, int action) {
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return;
        }

        if (!moGangHuList.isEmpty() && gameModel.getSpecialPlay().isGangMoNum()) {
            // 有摸杠胡的优先处理
            passMoHu(player, majiangs, action);
            return;
        }

        if (gameModel.isMustHu() && (actionSeatMap.get(player.getSeat()).get(0) == 1 || actionSeatMap.get(player.getSeat()).get(17) == 1)) {
            LogUtil.printDebug("有胡必胡, 不能过.");
            return;
        }

        //明杠过杠
        if (gameModel.getSpecialPlay().isPassGang() && actionSeatMap.get(player.getSeat()).get(2) == 1) {
            player.addPassGangVal(CollectionUtils.isEmpty(majiangs) ? player.getHandMajiang().get(player.getHandMajiang().size() - 1).getVal() : majiangs.get(0).getVal());
        }

        //报听第一次炮胡没有接炮，后面只能自摸
//        if (player.getFlutterTypeBool() && actionSeatMap.get(player.getSeat()).get(0) == 1) {
//            player.setSignTingPao(1);
//        }

        if (actionSeatMap.get(player.getSeat()).get(0) == 1 || actionSeatMap.get(player.getSeat()).get(17) == 1 || actionSeatMap.get(player.getSeat()).get(1) == 1 || actionSeatMap.get(player.getSeat()).get(4) == 1) {
            //漏胡：玩家有胡点过后，过手后才能炮胡，需要自己摸牌才算过手
            player.setTakeCardFlag(player.getDahuScore());
            player.changeExtend();
        }
        List<Integer> removeActionList = removeActionSeat(player.getSeat());
        int xiaoHu = MjAction.getFirstXiaoHu(removeActionList);
        logAction(player, action, xiaoHu, majiangs, removeActionList);
        boolean isBegin = isBegin();
        if (xiaoHu != -1) {
            player.addPassXiaoHu(xiaoHu);
            player.addPassXiaoHuList2(xiaoHu);
            List<Integer> actionList = player.checkMoCard(null, isBegin);
            if (!actionList.isEmpty()) {
                actionList.set(xiaoHu, 0);
                if (MjAction.getFirstXiaoHu(actionList) != -1) {
                    // 过小胡后，还有小胡，直接提示小胡
                    addActionSeat(player.getSeat(), actionList);
                    PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
                    buildPlayRes(builder, player, action, majiangs);
                    builder.setSeat(nowDisCardSeat);
                    builder.addAllSelfAct(actionList);
                    player.writeSocket(builder.build());
                    logAction(player, action, xiaoHu, majiangs, actionList);
                    addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_"
                            + MjHelper.toMajiangStrs(majiangs));
                    return;
                } else {
                    addActionSeat(player.getSeat(), actionList);
                }
            }
        }

        if (moLastMajiangSeat == player.getSeat() && gameModel.getSpecialPlay().isHaiDiPao()) {
            // 摸海底可以胡的人点过，将海底牌打出
            chuLastPai(player);
            return;
        }
        checkClearGangDisMajiang();
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        buildPlayRes(builder, player, action, majiangs);
        builder.setSeat(nowDisCardSeat);
        player.writeSocket(builder.build());
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + MjHelper.toMajiangStrs(majiangs));
        if (isCalcOver()) {
            calcOver();
            return;
        }
        if (MjAction.hasHu(removeActionList) && disCardSeat != player.getSeat() && nowDisCardIds.size() == 1) {
            // 漏炮
            player.passHu(nowDisCardIds.get(0).getVal());
        }

        // nowDisCardIds.size() == 1
        if (removeActionList.get(0) == 1 && disCardSeat != player.getSeat()) {
            if (nowDisCardIds.size() > 1) {
                for (Mj mj : nowDisCardIds) {
                    List<Integer> hu = player.checkDisCard(mj, false, false, getKingCard() != null && mj.getVal() == getKingCard().getVal());
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
            NyMjPlayer disCSMajiangPlayer = seatMap.get(disCardSeat);
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
                NyMjPlayer seatPlayer = seatMap.get(seat);
                seatPlayer.writeSocket(copy.build());
            }
        }
        // && tempActionMap.size()==0

        if (gameModel.isGangAfterAutoPlay() && player.noNeedMoCard() && !player.getGang().isEmpty() && actionSeatMap.get(player.getSeat()) == null) {
            // 杠牌后自动出牌
            List<Mj> disMjiang = new ArrayList<>();
            disMjiang.add(player.getLastMoMajiang());
            if (isHasGangAction()) {
                checkMoOutCard(disMjiang, player, action);
            } else {
                disCard(player, disMjiang, 0);
            }
        }

//        if (isBegin && xiaoHu == -1 && player.getSeat() == lastWinSeat) {
        if (!checkFlutterScoreInfo() && player.getSeat() == lastWinSeat && actionSeatMap.isEmpty()) {
            // 庄家过非小胡，提示庄家出牌
            ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.req_com_kt_ask_dismajiang);
            player.writeSocket(com.build());
        } else {
            checkBegin(player);
        }

        if (player.noNeedMoCard()) {
            sendTingInfo(player);
        }

        // 先过 后执行临时可做操作里面优先级最高的玩家操作
        refreshTempAction(player);
        checkMo();
    }

    /**
     * @param robGang 被抢杠, 不计分
     * @return
     * @description
     * @author Guang.OuYang
     * @date 2019/12/20
     */
    private void calcGangScore(NyMjPlayer player, int action, int sameCount, List<Mj> majiangs, boolean robGang) {
        if (!gameModel.getSpecialPlay().isGangBuF()) {
            return;
        }
        int lostPoint = 0;
        int getPoint = 0;
        if (action == MjDisAction.action_peng) {
            List<Integer> actionList = actionSeatMap.get(player.getSeat());
            if (actionList.get(2) == 1 || actionList.get(5) == 1) {
                // 可以碰也可以杠
                player.addPassGangVal(majiangs.get(0).getVal());
            }
            return;

        } else if (action == MjDisAction.action_angang || action == MjDisAction.action_buzhang_an) {
            // 暗杠相当于自摸每人出2分
            lostPoint = -2;
            getPoint = 2 * (getMaxPlayerCount() - 1);

            if (action == MjDisAction.action_angang && !robGang) {
                player.getGangSeat().put(majiangs.get(0).getVal(), MjDisAction.action_angang + 1000);
            }
        } else if (action == MjDisAction.action_minggang || action == MjDisAction.action_buzhang) {
            if (sameCount == 1) {
                // 碰牌之后再抓一个牌每人出1分
                // 放杠的人出3分

//                if (player.isPassGang(majiangs.get(0))) {
//                    // 特殊处理 可以碰可以杠的牌 选择了碰 再杠不算分
//                    return;
//                }
                lostPoint = -1;
                getPoint = 1 * (getMaxPlayerCount() - 1);

                //放杠标记
                if (action == MjDisAction.action_minggang && !robGang) {
                    player.getGangSeat().put(majiangs.get(0).getVal(), MjDisAction.action_minggang + 1000);
                }
            }
            // 放杠
            else if (sameCount == 3) {
                NyMjPlayer disPlayer = seatMap.get(disCardSeat);

                int point = (getMaxPlayerCount() - 1);
                disPlayer.changeGangPoint(-point);
                player.changeGangPoint(point);

                //放杠标记
                if (action == MjDisAction.action_minggang && !robGang) {
                    player.getGangSeat().put(majiangs.get(0).getVal(), disCardSeat);
                    //放杠算入明杠
//                    player.getGangSeat().put(majiangs.get(0).getVal(), MjDisAction.action_minggang + 1000);
                }
                //disPlayer.getMyExtend().setMjFengshen(FirstmythConstants.firstmyth_index13, 1);
                // disPlayer.changeGangPoint(-3);
                //player.changeGangPoint(3);
                // }
            }

        }

        if (lostPoint != 0) {
            for (NyMjPlayer seat : seatMap.values()) {
                if (seat.getUserId() == player.getUserId()) {
                    player.changeGangPoint(getPoint);
                } else {
                    seat.changeGangPoint(lostPoint);
                }
            }
        }

    }

    private void calcXiaoHuPoint(NyMjPlayer player, int xiaoIndex) {
//		int count = player.getXiaoHuCount(xiaoIndex);
//		int lostPoint = -2 * count;
//		int getPoint = 6 * count;
//		if (lostPoint != 0) {
//			for (TjMjPlayer seat : seatMap.values()) {
//				if (seat.getUserId() == player.getUserId()) {
//					seat.changeGangPoint(getPoint);
//				} else {
//					seat.changeGangPoint(lostPoint);
//				}
//			}
//		}
    }

    private void recordDisMajiang(List<Mj> majiangs, NyMjPlayer player) {
        setNowDisCardIds(majiangs);
        setDisCardSeat(player.getSeat());
    }

    public List<Mj> getNowDisCardIds() {
        return nowDisCardIds;
    }

    public void setDisEventAction(int disAction) {
        this.disEventAction = disAction;
        changeExtend();
    }

    public void setNowDisCardIds(List<Mj> nowDisCardIds) {
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
                NyMjPlayer player = seatMap.get(seat);
                if (player != null && player.isRobot()) {
                    // 如果是机器人可以直接决定
                    List<Integer> actionList = actionSeatMap.get(seat);
                    if (actionList == null) {
                        continue;
                    }
                    List<Mj> list = new ArrayList<>();
                    if (!nowDisCardIds.isEmpty()) {
                        list = MjQipaiTool.getVal(player.getHandMajiang(), nowDisCardIds.get(0).getVal());
                    }
                    if (actionList.get(0) == 1) {
                        // 胡
                        playCommand(player, new ArrayList<Mj>(), MjDisAction.action_hu);

                    } else if (actionList.get(3) == 1) {
                        playCommand(player, list, MjDisAction.action_angang);

                    } else if (actionList.get(2) == 1) {
                        playCommand(player, list, MjDisAction.action_minggang);

                    } else if (actionList.get(1) == 1) {
                        playCommand(player, list, MjDisAction.action_peng);

                    } else if (actionList.get(4) == 1) {
                        playCommand(player, player.getCanChiMajiangs(nowDisCardIds.get(0)), MjDisAction.action_chi);

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
            // for (TjMjPlayer player : seatMap.values()) {
            // if (player.isRobot() && player.canXiaoHu()) {
            // playCommand(player, new ArrayList<TjMj>(),
            // TjMjDisAction.action_xiaohu);
            // }
            // }

            int nextseat = getNextActionSeat();
            NyMjPlayer next = seatMap.get(nextseat);
            if (next != null && next.isRobot()) {
                List<Integer> actionList = actionSeatMap.get(next.getSeat());
                int xiaoHuAction = -1;
                if (actionList != null) {
                    List<Mj> list = null;
                    if (actionList.get(0) == 1) {
                        // 胡
                        playCommand(next, new ArrayList<Mj>(), MjDisAction.action_hu);

                    } else if ((xiaoHuAction = MjAction.getFirstXiaoHu(actionList)) > 0) {

                        playCommand(next, new ArrayList<Mj>(), MjDisAction.action_pass);

                    } else if (actionList.get(3) == 1) {
                        // 机器人暗杠
                        Map<Integer, Integer> handMap = MjHelper.toMajiangValMap(next.getHandMajiang());
                        for (Entry<Integer, Integer> entry : handMap.entrySet()) {
                            if (entry.getValue() == 4) {
                                // 可以暗杠
                                list = MjHelper.getMajiangList(next.getHandMajiang(), entry.getKey());
                            }
                        }
                        playCommand(next, list, MjDisAction.action_angang);

                    } else if (actionList.get(5) == 1) {
                        // 机器人补张
                        Map<Integer, Integer> handMap = MjHelper.toMajiangValMap(next.getHandMajiang());
                        for (Entry<Integer, Integer> entry : handMap.entrySet()) {
                            if (entry.getValue() == 4) {
                                // 可以补张
                                list = MjHelper.getMajiangList(next.getHandMajiang(), entry.getKey());
                            }
                        }
                        if (list == null) {
                            if (next.noNeedMoCard()) {
                                list = MjQipaiTool.getVal(next.getHandMajiang(), next.getLastMoMajiang().getVal());

                            } else {
                                list = MjQipaiTool.getVal(next.getHandMajiang(), nowDisCardIds.get(0).getVal());
                                list.add(nowDisCardIds.get(0));
                            }
                        }

                        playCommand(next, list, MjDisAction.action_buzhang);

                    } else if (actionList.get(2) == 1) {
                        Map<Integer, Integer> pengMap = MjHelper.toMajiangValMap(next.getPeng());
                        for (Mj handMajiang : next.getHandMajiang()) {
                            if (pengMap.containsKey(handMajiang.getVal())) {
                                // 有碰过
                                list = new ArrayList<>();
                                list.add(handMajiang);
                                playCommand(next, list, MjDisAction.action_minggang);
                                break;
                            }
                        }

                    } else if (actionList.get(1) == 1) {
                        // playCommand(next, list, TjMjDisAction.action_peng);

                    } else if (actionList.get(4) == 1) {
                        Mj majiang = null;
                        List<Mj> chiList = null;
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
                                        majiang = Mj.getMajang(majiangId);
                                        chiList = next.getCanChiMajiangs(majiang);
                                        chiList.add(majiang);
                                        break;
                                    }

                                }

                            }

                        }

                        playCommand(next, chiList, MjDisAction.action_chi);

                    } else {
                        System.out.println("!!!!!!!!!!" + JacksonUtil.writeValueAsString(actionList));

                    }

                } else {
                    int maJiangId = MjRobotAI.getInstance().outPaiHandle(0, next.getHandPais(),
                            new ArrayList<Integer>());
                    List<Mj> majiangList = MjHelper.toMajiang(Arrays.asList(maJiangId));
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

    public boolean isFinalOneCircle() {
        return getLeftMajiangCount() < gameModel.getSpecialPlay().getFinalOneCircleCardNum();
    }

    public void setLastWinSeat(int winnerSeat) {
        super.setLastWinSeat(winnerSeat);
    }

    @Override
    protected void deal() {
		if (lastWinSeat == 0) {
			// 第一局丢筛子
			setLastWinSeat(playerMap.get(masterId).getSeat());
		}

//        getGameModel().setFlutterScore(0);
        setDisCardSeat(lastWinSeat);
        setNowDisCardSeat(lastWinSeat);
        setMoMajiangSeat(lastWinSeat);

        List<Integer> defaultCards = gameModel.isNoneChar() ? MjConstants.noneChar : MjConstants.fullMj;

        List<Integer> copy = null;
        if (maxPlayerCount == 2 && gameModel.getSpecialPlay().isQueYiMen()) {
            copy = new ArrayList<>();
            for (Integer id : defaultCards) {
                Mj mj = Mj.getMajang(id);
                if (mj.getColourVal() == 1) {
                    continue;
                }
                copy.add(id);
            }
        } else {
            copy = new ArrayList<>(defaultCards);
        }

        // List<Integer> copy = new
        // ArrayList<>(TjMjConstants.zhuanzhuan_mjList);
        addPlayLog(copy.size() + "");
        List<List<Mj>> list;
        if (zp == null) {
            list = MjTool.fapai(copy, getMaxPlayerCount());
        } else {
            list = MjTool.fapai(copy, getMaxPlayerCount(), zp);
        }

        List<Mj> leftMjs = list.get(playerMap.values().size());

        Mj assign = null;
        if (zp != null && zp.size() > gameModel.getGameMaxHuman()) {
            assign = leftMjs.remove(0);
            leftMjs.add(assign);
        }

        createKingCardAndFloorCard(assign, leftMjs);

        LogUtil.printDebug("地牌:{},王牌:{}", floorCard, kingCard);

        int i = 1;
//        List<Integer> removeIndex = new ArrayList<>();
        for (NyMjPlayer player : playerMap.values()) {
            if (player.getSeat() == lastWinSeat) {

                List<Mj> mjs2 = new ArrayList<>(list.get(0));

                removeMaxKingFloorCard(list.get(0), leftMjs, mjs2);

                player.dealHandPais(list.get(0));

//                player.clearPlayerOverInfo();

                player.changeState(player_state.play);
//                removeIndex.add(0);
                continue;
            }

            List<Mj> mjs = new ArrayList<>(list.get(i));

            removeMaxKingFloorCard(list.get(i), leftMjs, mjs);

            player.dealHandPais(list.get(i));

//            player.clearPlayerOverInfo();

            player.changeState(player_state.play);

//            removeIndex.add(i);
            i++;
        }

        // 桌上剩余的牌
//        List<Mj> leftMjs = new ArrayList<>();
//        // 没有发出去的牌退回剩余牌中
//        for (int j = 0; j < list.size(); j++) {
//
//            if (!removeIndex.contains(j)) {
//                leftMjs.addAll(list.get(j));
//            }
//        }


		//特殊处理最后一张牌不为红中,一马全中时不好定义红中的属性
		int redMiddleVal = 201;
        int maxSize = leftMjs.size() - 1;
		Mj mj;
		while (redMiddleVal == (mj = leftMjs.get(maxSize)).getVal()) {
			int index = maxSize - 1;
			//倒数第二张为红中则递增到第三张
			while (redMiddleVal == leftMjs.get(index).getVal()) {
				index--;
			}
			Mj m = leftMjs.get(index);
			//交换两张牌
			leftMjs.set(maxSize, m);
			leftMjs.set(index, mj);
		}

		// 桌上剩余的牌
        if (gameModel.getDiscardHoleCards() <= 0) {
            setLeftMajiangs(leftMjs);
//            setLeftMajiangs(leftMjs.subList(0,13));
        } else if (gameModel.getDiscardHoleCards() >= leftMjs.size()) {
            setLeftMajiangs(new ArrayList<>());
        } else if (gameModel.getDiscardHoleCards() > 0) {
            int size = leftMjs.size();
            //抽牌
//            leftMjs = leftMjs.subList(0, gameModel.getDiscardHoleCards());
            setLeftMajiangs(leftMjs.subList(gameModel.getDiscardHoleCards(), size));
        } else {
            setLeftMajiangs(leftMjs);
        }
    }

    /**
     * @param
     * @return
     * @description 地牌王牌创建
     * @author Guang.OuYang
     * @date 2019/10/30
     *
     */
    private void createKingCardAndFloorCard(Mj assign, List<Mj> leftMajiangs) {
        if (!gameModel.isCreateKingCard()) {
            return;
        }

        Random r = new Random();
        int dealDice = (r.nextInt(6) + 1) * 10 + (r.nextInt(6) + 1);
        setDealDice(dealDice);

       //定赖：发完牌后，翻起牌墩的最后一张牌，不会放回牌墩，这张牌的牌序+1为赖子。

        if (assign != null) {
            this.floorCard = assign;
        }else{
			if (!gameModel.isNoneChar()) {
				//固定为北
        		this.floorCard = Mj.getMajang(121);
			}else{
            	this.floorCard = leftMajiangs.get(0);
			}
        }

        LogUtil.printDebug("地牌INDEX:{}->{}", leftMajiangs.size() - 1, floorCard);

        //东南西北中发白<-
        if (this.floorCard.getId() > 108) {
            //中发白201~212
            if (this.floorCard.getId() > 124) {
                this.kingCard = Mj.findAnyValByMj(this.floorCard.getVal() + 10);
                if (this.kingCard == null) {
                    this.kingCard = Mj.getMajang(109);
                }
            } else { //109~124
                this.kingCard = Mj.findAnyValByMj(this.floorCard.getVal() + 10);
                if (this.kingCard == null) {
                    this.kingCard = Mj.getMajang(201);
                }
            }

        } else {
            this.kingCard = Mj.getMajang(floorCard.getId() + 1);
            if (this.kingCard.getHuase() != floorCard.getHuase()) {
                this.kingCard = Mj.getMajang(kingCard.getId() - 9);
            }
        }


    }

    /**
     * @param
     * @return
     * @description
     * @author Guang.OuYang
     * @date 2019/10/30
     */
    private void removeMaxKingFloorCard(List<Mj> list, List<Mj> leftMjs, List<Mj> mjs) {
//        int index = 0;
//        int floorNum = 0;
//        int kingNum = 0;
//        //屏蔽王牌地牌>2
//        if (floorCard != null || kingCard != null) {
//            Iterator<Mj> iterator = mjs.iterator();
//            while (iterator.hasNext()) {
//                Mj next = iterator.next();
//                if (floorCard != null && floorCard.getVal() == next.getVal()) {
//                    floorNum++;
//                } else if (kingCard != null && kingCard.getVal() == next.getVal()) {
//                    kingNum++;
//                }
//
//                if (floorNum > 2 || kingNum > 2) {
//                    floorNum = 2;
//                    kingNum = 2;
//                    leftMjs.add(list.get(index));
//                    Mj remove = leftMjs.remove(0);
//                    while (remove.getVal() == floorCard.getVal() || remove.getVal() == kingCard.getVal()) {
//                        leftMjs.add(remove);
//                        remove = leftMjs.remove(0);
//                    }
//                    list.set(index, remove);
//                }
//                index++;
//            }
//        }
    }

    /**
     * 初始化桌子上剩余牌
     *
     * @param leftMajiangs
     */
    public void setLeftMajiangs(List<Mj> leftMajiangs) {
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
    public Mj getLeftMajiang(NyMjPlayer player) {
        if (this.leftMajiangs.size() > 0) {
            Mj majiang = this.leftMajiangs.remove(0);

//            //地牌王牌最多2张
//            if (player != null &&
//                    ((getKingCard() != null && majiang != null && majiang.getVal() == getKingCard().getVal() && player.getKingCardNumHuFlag() > 1)
//                            || (getFloorCard() != null && majiang != null && majiang.getVal() == getFloorCard().getVal() && player.getFloorCardNumHuFlag() > 1))) {
//                leftMajiangs.add(majiang);
//                return getLeftMajiang(player);
//            }
//
            if (player != null && getKingCard() != null && majiang.getVal() == getKingCard().getVal()) {
                player.setKingCardNumHuFlag(player.getKingCardNumHuFlag() + 1);
            } else if (player != null && getFloorCard() != null && getFloorCard().getVal() == getFloorCard().getVal()) {
                player.setFloorCardNumHuFlag(player.getFloorCardNumHuFlag() + 1);
            }
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
        return Math.max(this.leftMajiangs.size() - gameModel.getSpecialPlay().getStayLastCard(), 0);
        // return 1;
    }

//    public int getLeftMajiangRealCount() {
//        return this.leftMajiangs.size() ;
//        // return 1;
//    }

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
        res.addExt(gameModel.getSpecialPlay().getCalcBird()); // 2
        res.addExt(gameModel.getSpecialPlay().getBirdNum()); // 3
        res.addExt(gameModel.getSpecialPlay().isGpsWarn() ? 1 : 0); // 4
        res.addExt(gameModel.getSpecialPlay().isQueYiSe() ? 1 : 0); // 5
        res.addExt(gameModel.getSpecialPlay().isBlackSkyHu() ? 1 : 0); // 6
        res.addExt(gameModel.getSpecialPlay().isYiZhiHua() ? 1 : 0); // 7
        res.addExt(gameModel.getSpecialPlay().isLiuliuShun() ? 1 : 0); // 8
        res.addExt(gameModel.getSpecialPlay().isDaSiXi() ? 1 : 0); // 9
        res.addExt(gameModel.getSpecialPlay().isJinTongYuNv() ? 1 : 0); // 10
        res.addExt(gameModel.getSpecialPlay().isJieJieGao() ? 1 : 0); // 11
        res.addExt(gameModel.getSpecialPlay().isSanTong() ? 1 : 0); // 12
        res.addExt(gameModel.getSpecialPlay().isZhongTuLiuLiuShun() ? 1 : 0); // 13
        res.addExt(gameModel.getSpecialPlay().isZhongTuSiXi() ? 1 : 0); // 14
        res.addExt(gameModel.getSpecialPlay().getKePiao()); // 15
        res.addExt(gameModel.isCalcBanker() ? 1 : 0); // 16
        res.addExt(isBegin() ? 1 : 0); // 17
        //地牌
        res.addExt(floorCard == null ? 0 : floorCard.getId());
        //王牌
        res.addExt(kingCard == null ? 0 : kingCard.getId());
        System.out.println(" isbegin ====== " + isBegin());

        res.addStrExt(StringUtil.implode(moTailPai, ",")); // 0
        res.setDealDice(dealDice);
        res.setRenshu(getMaxPlayerCount());
        if (leftMajiangs != null) {
            res.setRemain(getLeftMajiangCount());
        } else {
            res.setRemain(0);
        }
        List<PlayerInTableRes> players = new ArrayList<>();
        for (NyMjPlayer player : playerMap.values()) {
            PlayerInTableRes.Builder playerRes = player.buildPlayInTableInfo(isrecover);
            if (player.getUserId() == userId) {
                playerRes.addAllHandCardIds(player.getHandPais());
            }

            if (showMjSeat.contains(player.getSeat()) && player.getHuXiaohu().size() > 0) {
                List<Integer> ids = MjHelper.toMajiangIds(
                        player.showXiaoHuMajiangs(player.getHuXiaohu().get(player.getHuXiaohu().size() - 1), true));
                if (ids != null) {
                    if (player.getUserId() == userId) {
                        playerRes.addAllIntExts(ids);
                    } else {
                        playerRes.addAllHandCardIds(ids);
                    }

                }
            }
            if (player.getSeat() == disCardSeat && nowDisCardIds != null) {
                playerRes.addAllOutCardIds(MjHelper.toMajiangIds(nowDisCardIds));
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
        getGameModel().setFlutterScore(0);
        readyTime = 0;
        seatMap.values().forEach(p -> {
//            if (!getGameModel().signFlutterAllOver()) {
//                systemAutoSignTingInfo(p);
//            }
            p.clearPlayerOverInfo();
        });
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

        gameModel.getSpecialPlay().setBirdNum(2);
        gameModel.setCalcBanker(true);
        gameModel.getSpecialPlay().setCalcBird(2);

        changeExtend();
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
        addPlayLog(disCardRound + "_" + seat + "_" + MjDisAction.action_hasAction + "_"
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
            addPlayLog(disCardRound + "_" + seat + "_" + MjDisAction.action_hasAction + "_" + StringUtil.implode(a));
        } else {
            actionSeatMap.put(seat, actionlist);
            addPlayLog(disCardRound + "_" + seat + "_" + MjDisAction.action_hasAction + "_"
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
            nowDisCardIds = MjHelper.toMajiang(StringUtil.explodeToIntList(info.getNowDisCardIds()));
        }

        if (!StringUtils.isBlank(info.getLeftPais())) {
            leftMajiangs = MjHelper.toMajiang(StringUtil.explodeToIntList(info.getLeftPais()));
        }

    }

    // @Override
    // public void initExtend(String info) {
    // if (StringUtils.isBlank(info)) {
    // return;
    // }
    // JsonWrapper wrapper = new JsonWrapper(info);
    // for (TjMjPlayer player : seatMap.values()) {
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
    private boolean canChi(NyMjPlayer player, List<Mj> handMajiang, List<Mj> majiangs, Mj disMajiang) {
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return false;
        }

        if (/*maxPlayerCount == 2 && */gameModel.getSpecialPlay().isBuChi()) {
            return false;
        }

        if (player.noNeedMoCard()) {
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

        List<Mj> chi = MjTool.checkChi(majiangs, disMajiang, player.getPlayingTable(NyMjTable.class));
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
    private boolean canPeng(NyMjPlayer player, List<Mj> majiangs, int sameCount, Mj disMajiang) {
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return false;
        }
        if (player.noNeedMoCard()) {
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
    private boolean canAnGang(NyMjPlayer player, List<Mj> majiangs, int sameCount, int action) {
        if (sameCount != 4) {
            return false;
        }
        if (player.getSeat() != getNextDisCardSeat() && action != MjDisAction.action_buzhang) {
            return false;
        }
        if (player.getSeat() != getNextDisCardSeat() && action != MjDisAction.action_buzhang_an) {
            return false;
        }
        return true;
    }

    /**
     * 检查优先度 如果同时出现一个事件，按出牌座位顺序优先
     */
    private boolean checkAction(NyMjPlayer player, List<Mj> cardList, List<Integer> hucards, int action) {
        boolean canAction = checkCanAction(player, action);// 是否优先级最高 能执行操作
        if (canAction == false) {// 不能操作时 存入临时操作
            int seat = player.getSeat();
            tempActionMap.put(seat, new MjTempAction(seat, action, cardList, hucards));
            // 玩家都已选择自己的临时操作后 选取优先级最高
            if (tempActionMap.size() == actionSeatMap.size()) {
                int maxAction = Integer.MAX_VALUE;
                int maxSeat = 0;
                Map<Integer, Integer> prioritySeats = new HashMap<>();
                int maxActionSize = 0;
                for (MjTempAction temp : tempActionMap.values()) {
                    int prioAction = MjDisAction.getPriorityAction(temp.getAction());
                    int prioAction2 = MjDisAction.getPriorityAction(maxAction);
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
                NyMjPlayer tempPlayer = seatMap.get(maxSeat);
                List<Mj> tempCardList = tempActionMap.get(maxSeat).getCardList();
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
    private void refreshTempAction(NyMjPlayer player) {
        tempActionMap.remove(player.getSeat());
        Map<Integer, Integer> prioritySeats = new HashMap<>();
        for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
            int seat = entry.getKey();
            List<Integer> actionList = entry.getValue();
            List<Integer> list = MjDisAction.parseToDisActionList(actionList);
            int priorityAction = MjDisAction.getMaxPriorityAction(list);
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
        Iterator<MjTempAction> iterator = tempActionMap.values().iterator();
        while (iterator.hasNext()) {
            MjTempAction tempAction = iterator.next();
            if (tempAction.getSeat() == maxPrioritySeat) {
                int action = tempAction.getAction();
                List<Mj> tempCardList = tempAction.getCardList();
                NyMjPlayer tempPlayer = seatMap.get(tempAction.getSeat());
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
    public boolean checkCanAction(NyMjPlayer player, int action) {
        // 优先度为胡杠补碰吃
        List<Integer> stopActionList = MjDisAction.findPriorityAction(action);
        //所有玩家当前可作的操作 com.sy599.game.qipai.tjmj.constant.MjAction
        //     * 接炮0;碰1;明杠2;暗杠3;吃4;补张5;缺一色6;板板胡7;一枝花 8;六六顺9;大四喜10;金童玉女11;节节高12;三同13;中途四喜14;中途六六顺15;暗杠补张16;自摸17;
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
     * 是否能暗杠
     *
     * @param player
     * @param majiangs
     * @param sameCount
     * @return
     */
    private boolean canMingGang(NyMjPlayer player, List<Mj> handMajiang, List<Mj> majiangs, int sameCount,
                                Mj disMajiang) {
        List<Integer> pengList = MjHelper.toMajiangVals(player.getPeng());

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

    public void setLastMajiang(Mj lastMajiang) {
        this.lastMajiang = lastMajiang;
        changeExtend();
    }

    public void setMoLastMajiangSeat(int moLastMajiangSeat) {
        this.moLastMajiangSeat = moLastMajiangSeat;
        changeExtend();
    }

    public void setGangMajiang(Mj gangMajiang) {
        this.gangMajiang = gangMajiang;
        changeExtend();
    }

    /**
     * 摸杠别人可以胡
     *
     * @param moGang       杠的牌
     * @param moGangHuList 可以胡的人的座位list
     */
    public void setMoGang(Mj moGang, List<Integer> moGangHuList) {
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

    public void setGangDisMajiangs(List<Mj> gangDisMajiangs) {
        this.gangDisMajiangs = gangDisMajiangs;
        changeExtend();
    }

    public List<Mj> getGangDisMajiangs() {
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
        return gameModel.getSpecialPlay().isRobGangHu();
    }

    public boolean canAnGangHu() {
        return gameModel.getSpecialPlay().isRobGangHu();
    }

    /**
     * @param
     * @return
     * @description 计算最终翻倍倍率
     * @author Guang.OuYang
     * @date 2019/9/16
     */
    private int calcFinalRatio(NyMjPlayer v) {
        //负分不做翻倍
        if (v.getTotalPoint() < 0) {
            return 0;
        }

        int ratio = gameModel.doubleChipEffect(v.getTotalPoint());
        //低于X分进行加倍
        int doubleChip = (ratio * v.getTotalPoint()) - v.getTotalPoint();
        LogUtil.printDebug(v.getUserId() + "大结算加倍:倍率:{},winLoss:{},totalPoint:{}->point:{}", ratio, v.getWinLossPoint(), v.getTotalPoint(), v.getPoint());

        int addScore = gameModel.lowScoreEffect(doubleChip + v.getTotalPoint());
        //低于X分进行加分
        int addScoreChip = (addScore + v.getTotalPoint()) - v.getTotalPoint();
        LogUtil.printDebug(v.getUserId() + "大结算低于多少分+多少分:阈值:{},winLoss:{},totalPoint:{}->point:{}", addScore, v.getWinLossPoint(), v.getTotalPoint(), v.getPoint());

        //把加的分都总计一下
        return doubleChip + addScoreChip;
    }

    public ClosingMjInfoRes.Builder sendAccountsMsg(boolean over, boolean selfMo, List<Integer> winList,
                                                    Integer[] prickBirdMajiangIds, Integer[] seatBirds, Map<Integer, Integer> seatBirdMap, MjiangHu.BigMap<Integer, Integer, Integer> seatBirdBigMap, boolean isBreak,
                                                    int bankerSeat, int catchBirdSeat) {

        int totalAddPoint = 0;

        List<Integer> bigWinList = new ArrayList<>();
        //大结算
        //大结算,找赢家,赢家可能会有多个
        Iterator<NyMjPlayer> iterator = seatMap.values().iterator();
        while (iterator.hasNext()) {
            NyMjPlayer p = iterator.next();
            if (p != null) {
                //展示分
                p.setWinLossPoint(p.getTotalPoint());
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

        List<ClosingMjPlayerInfoRes> list = new ArrayList<>();
        List<ClosingMjPlayerInfoRes.Builder> builderList = new ArrayList<>();
        int fangPaoSeat = selfMo ? 0 : disCardSeat;

        if (winList == null || winList.size() == 0) {
            fangPaoSeat = 0;
        }

        for (NyMjPlayer player : seatMap.values()) {
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
                            if (huMjIds.size() == 3) {
                                isHu2 = huMjIds.get(2);
                            } else if (huMjIds.size() == 4) {
                                isHu2 = huMjIds.get(2) * 1000 + huMjIds.get(3);
                            }
                        } else {
                            isHu = huMjIds.get(0);
                        }
                        build.setTotalFan(isHu2);
                        build.setIsHu(isHu);
                    } else {
                        Mj huMajiang = nowDisCardIds.get(0);
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
                            if (huMjIds.size() == 3) {
                                isHu2 = huMjIds.get(2);
                            } else if (huMjIds.size() == 4) {
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
            for (NyMjPlayer player : seatMap.values()) {
                if (player.getWinLoseCredit() > dyjCredit) {
                    dyjCredit = player.getWinLoseCredit();
                }
            }
            for (ClosingMjPlayerInfoRes.Builder builder : builderList) {
                NyMjPlayer player = seatMap.get(builder.getSeat());
                calcCommissionCredit(player, dyjCredit);
                builder.setWinLoseCredit(player.getWinLoseCredit());
                builder.setCommissionCredit(player.getCommissionCredit());
            }
        } else if (isGroupTableGoldRoom()) {
            // -----------亲友圈金币场---------------------------------
            for (NyMjPlayer player : seatMap.values()) {
                player.setWinGold(player.getTotalPoint() * gtgDifen);
            }
            calcGroupTableGoldRoomWinLimit();
            for (ClosingMjPlayerInfoRes.Builder builder : builderList) {
                NyMjPlayer player = seatMap.get(builder.getSeat());
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

        if (seatBirdBigMap != null) {
            List<TableMjResMsg.BirdAttribute> birdAttrs = new ArrayList<>();

            Iterator<MjiangHu.BigMapEntry<Integer, Integer, Integer>> iterable = seatBirdBigMap.iterable();
            while (iterable.hasNext()) {
                MjiangHu.BigMapEntry<Integer, Integer, Integer> next = iterable.next();
                TableMjResMsg.BirdAttribute.Builder builder = TableMjResMsg.BirdAttribute.newBuilder();
                builder.setMjId(next.getK());
                builder.setAwardSeat(next.getE());
                builder.setBelongSeat(next.getV());
                birdAttrs.add(builder.build());
            }
            res.addAllBirdAttr(birdAttrs);
        }

        if (seatBirds != null) {
            res.addAllBirdSeat(Arrays.asList(seatBirds));
        }
        if (prickBirdMajiangIds != null) {
            res.addAllBird(Arrays.asList(prickBirdMajiangIds));
        }
        res.setCatchBirdSeat(catchBirdSeat);
        res.addAllLeftCards(MjHelper.toMajiangIds(leftMajiangs));
        for (NyMjPlayer player : seatMap.values()) {
            player.writeSocket(res.build());
        }
        return res;

    }

    /**
     * 杠上花和杠上炮
     *
     * @return
     */
    public Mj getGangHuMajiang(int seat) {
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
        return Mj.getMajang(majiangId);

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

        ext.add(gameModel.getSpecialPlay().getCalcBird() + "");
        ext.add((gameModel.getSpecialPlay().isGpsWarn() ? 1 : 0) + "");
        ext.add((gameModel.getSpecialPlay().isQueYiSe() ? 1 : 0) + "");
        ext.add((gameModel.getSpecialPlay().isBlackSkyHu() ? 1 : 0) + "");
        ext.add((gameModel.getSpecialPlay().isYiZhiHua() ? 1 : 0) + "");
        ext.add((gameModel.getSpecialPlay().isLiuliuShun() ? 1 : 0) + "");
        ext.add((gameModel.getSpecialPlay().isDaSiXi() ? 1 : 0) + "");
        ext.add((gameModel.getSpecialPlay().isJinTongYuNv() ? 1 : 0) + "");
        ext.add((gameModel.getSpecialPlay().isJieJieGao() ? 1 : 0) + "");
        ext.add((gameModel.getSpecialPlay().isSanTong() ? 1 : 0) + "");
        ext.add((gameModel.getSpecialPlay().isZhongTuLiuLiuShun() ? 1 : 0) + "");
        ext.add((gameModel.getSpecialPlay().isZhongTuSiXi() ? 1 : 0) + "");
        ext.add((gameModel.getSpecialPlay().getKePiao()) + "");
        ext.add((gameModel.isCalcBanker() ? 1 : 0) + "");
        ext.add(gameModel.getSpecialPlay().getBirdNum() + "");
        ext.add(isAutoPlay + "");
        ext.add(over + ""); // 25
        ext.add(String.valueOf(floorCard == null ? 0 : floorCard.getId())); // 26
        ext.add(String.valueOf(kingCard == null ? 0 : kingCard.getId())); // 27
        return ext;
    }

    @Override
    public void sendAccountsMsg() {
        ClosingMjInfoRes.Builder builder = sendAccountsMsg(true, false, null, null, null, null, null, true, 0, 0);
        saveLog(true, 0l, builder.build());
    }

    public Class<? extends Player> getPlayerClass() {
        return NyMjPlayer.class;
    }

    @Override
    public void checkReconnect(Player player) {
//        if (super.isAllReady() && gameModel.getSpecialPlay().getKePiao() > 0 && gameModel.getSpecialPlay().getTableStatus() == MjConstants.TABLE_STATUS_PIAO) {
//            TjMjPlayer player1 = (TjMjPlayer) player;
//            if (player1.getPiaoPoint() < 0) {
//                ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_table_status_piao,
//                        gameModel.getSpecialPlay().getTableStatus());
//                player1.writeSocket(com.build());
//                return;
//            }
//        }

//        checkDeal();

        //最先检测可以做的操作+牌桌部分信息
        if (state == table_state.play && player.getHandPais() != null && player.getHandPais().size() > 0) {
//            sendDealInfo((TjMjPlayer) player);
            sendTingInfo((NyMjPlayer) player);
        }


        checkSendGangRes(player);
        if (askLastMajaingSeat != 0) {
            sendAskLastMajiangRes(player.getSeat());
        }

//        if (actionSeatMap.isEmpty()) {
//            // 没有其他可操作的动作事件
//            if (player instanceof ChaosMjPlayer) {
//                ChaosMjPlayer csMjPlayer = (ChaosMjPlayer) player;
//                if (csMjPlayer != null) {
//                    if (csMjPlayer.noNeedMoCard()) {
//                        if (!csMjPlayer.getGang().isEmpty()) {
//                            List<Mj> disMajiangs = new ArrayList<>();
//                            disMajiangs.add(csMjPlayer.getLastMoMajiang());
//                            disCard(csMjPlayer, disMajiangs, 0);
//                        }
//                    }
//                }
//            }
//        }

//        if (/*isBegin() && */!checkFlutterScoreInfo() && seatMap.containsKey(lastWinSeat)/*&& player.getSeat() == lastWinSeat && actionSeatMap.isEmpty()*/) {
//            // 如果是起手判断是否还有人可胡小胡 没有的话通知庄家出牌
//            TcMjPlayer bankPlayer = seatMap.get(lastWinSeat);
//            ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.req_com_kt_ask_dismajiang);// 推送庄家出牌
//            bankPlayer.writeSocket(com.build());
//        }


        if(super.isAllReady() && checkFlutterScoreInfo()){
            return;
        }

        //最后一圈
        if (getLeftMajiangCount() == gameModel.getSpecialPlay().getFinalOneCircleCardNum()) {
            player.writeSocket(SendMsgUtil.buildComRes(WebSocketMsgType.res_code_chaosmj_final_circle_tips, 0).build());
        }

        //最后一圈不能胡就自动摸牌
//        if (isFinalOneCircle()) {
//            checkMo();
//        }

//        if (state == table_state.play) {
//            if (player.getHandPais() != null && player.getHandPais().size() > 0) {
//                sendTingInfo((TjMjPlayer) player);
//            }
//        }

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
            for (NyMjPlayer player : seatMap.values()) {
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
        if (gameModel.isGangAfterAutoPlay()) {
            for (NyMjPlayer player : seatMap.values()) {

                if ((!player.getGang().isEmpty() /*|| player.getFlutterTypeBool()*/) && player.noNeedMoCard() && getMoMajiangSeat() == player.getSeat()) {
                    // 能胡牌就不自动打出去
                    List<Integer> actionList = actionSeatMap.get(player.getSeat());
                    if (actionList != null && (actionList.get(MjAction.HU) == 1 || actionList.get(MjAction.ZIMO) == 1
                            || actionList.get(MjAction.MINGGANG) == 1 || actionList.get(MjAction.ANGANG) == 1 || actionList.get(MjAction.BUZHANG) == 1
                            || hasXiaoHu(actionList))) {
                        continue;
                    }

                    if (nowDisCardSeat != player.getSeat()) {
                        continue;
                    }

                    List<Mj> disMjiang = new ArrayList<>();
                    if(!MjRule.isKingCard(player.getHandMajiang().get(player.getHandMajiang().size()-1), kingCard)) {
						disMjiang.add(player.getHandMajiang().get(player.getHandMajiang().size() - 1));
					} else {
						for (int i = player.getHandMajiang().size() - 1; i >= 0; i--) {
							if (!MjRule.isKingCard(player.getHandMajiang().get(i), kingCard)) {
								disMjiang.add(player.getHandMajiang().get(i));
								break;
							}
						}
					}

                    disCard(player, disMjiang, MjDisAction.action_chupai);
                    // 执行完一个就退出，防止出牌操作后有报听玩家摸牌
                    setLastAutoPlayTime(System.currentTimeMillis());
                    return;
                }
            }
        }

        //最后一圈
        if (gameModel.getSpecialPlay().isHaiDiLaoYue() && isFinalOneCircle() && getLeftMajiangCount() >= 0) {
            int nowMoCardSeat = nowDisCardSeat;
            int maxTryCount = getLeftMajiangCount() - gameModel.getSpecialPlay().getFinalOneCircleCardNum() + 30;
            //这里没有操作需要直接给下个人发牌
            if (!(getActionSeatMap().containsKey(nowMoCardSeat) && getActionSeatMap().get(nowMoCardSeat).contains(1))) {
                nowDisCardSeat = calcNextSeat(nowMoCardSeat);
                checkMo();
                //容错+3,直接结算
                if (maxTryCount-- <= 0 && state != table_state.over) {
                    calcOver();
                }
            }
        }

        if (isAutoPlay < 1) {
            return;
        }

		if (isAutoPlayOff()) {
			// 托管关闭
			for (int seat : seatMap.keySet()) {
				NyMjPlayer player = seatMap.get(seat);
				player.setAutoPlay(false, false);
				player.setCheckAutoPlay(false);
			}
			return;
		}

//        if (isBegin() && actionSeatMap.isEmpty()) {
//            // 如果是起手判断是否还有人可胡小胡 没有的话通知庄家出牌
//            NyMjPlayer bankPlayer = seatMap.get(lastWinSeat);
//			if (bankPlayer!=null) {
//				ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.req_com_kt_ask_dismajiang);// 推送庄家出牌
//				bankPlayer.writeSocket(com.build());
//			}
//        }

        if (!gameModel.signFlutterAllOver() && gameModel.getFlutterScore() > 0) {
            for (NyMjPlayer player : seatMap.values()) {
//                if (player.getLastCheckTime() == 0) {
//                    player.setLastCheckTime(player.getLastCheckTime() + 1 * 1000);
//                    continue;
//                }
                player.checkAutoPlay(2, false);
                if (!player.isAutoPlay() || player.getFlutter() != -2) {
                    continue;
                }
                systemAutoSignTingInfo(player);
            }
        }

        if (gameModel.getSpecialPlay().getTableStatus() == MjConstants.TABLE_STATUS_PIAO) {
            for (int seat : seatMap.keySet()) {
                NyMjPlayer player = seatMap.get(seat);
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
                NyMjPlayer player = seatMap.get(seat);
                if (player.getPiaoPoint() < 0) {
                    piao = false;
                }

            }
            if (piao) {
                gameModel.getSpecialPlay().setTableStatus(MjConstants.AUTO_PLAY_TIME);
            }

        } else if (state == table_state.play) {
            autoPlay();
        } else {
            if (getPlayedBureau() == 0) {
                return;
            }
            readyTime++;
            // for (TjMjPlayer player : seatMap.values()) {
            // if (player.checkAutoPlay(1, false)) {
            // autoReady(player);
            // }
            // }
            // 开了托管的房间，xx秒后自动开始下一局
            for (NyMjPlayer player : seatMap.values()) {
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
     * @param
     * @return
     * @description 系统选定报听消息, 默认2不报听
     * @author Guang.OuYang
     * @date 2019/10/26
     */
    private void systemAutoSignTingInfo(NyMjPlayer player) {
        try {
            AbsCodeCommandExecutor.getGlobalActionCodeInstance(AbsCodeCommandExecutor.GlobalCommonIndex.COMMAND_INDEX, WebSocketMsgType.req_code_flutter_score)
                    .orElse(AbsCodeCommandExecutor.getGlobalActionCodeInstance(AbsCodeCommandExecutor.GlobalCommonIndex.COMMAND_INDEX, -1).get())
                    .execute0(player, null, null, ComMsg.ComReq.newBuilder().setCode(WebSocketMsgType.req_code_flutter_score).addParams(0).build());
        } catch (Exception e) {
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
                    NyMjPlayer player = seatMap.get(seat);
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
                int action, seat;
                for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
                    List<Integer> actList = MjDisAction.parseToDisActionList(entry.getValue());
                    if (actList == null) {
                        continue;
                    }
                    seat = entry.getKey();
                    action = MjDisAction.getAutoMaxPriorityAction(actList);
                    NyMjPlayer player = seatMap.get(seat);
                    if (!player.checkAutoPlay(0, false)) {
                        continue;
                    }
                    boolean chuPai = false;
                    if (player.noNeedMoCard()) {
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
                                Mj mj = nowDisCardIds.get(0);
                                List<Mj> mjList = new ArrayList<>();
                                for (Mj handMj : player.getHandMajiang()) {
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
                    }
                    // else if(action == TjMjDisAction.action_chi){
                    // playCommand(player, new ArrayList<>(),
                    // TjMjDisAction.action_chi);
                    // if (disCard) {
                    // autoChuPai(player);
                    // }
                    //
                    // }
                    else {
                        playCommand(player, new ArrayList<>(), MjDisAction.action_pass);
                        if (chuPai) {
                            autoChuPai(player);
                        }
                    }
                }
            }
        } else {
            NyMjPlayer player = seatMap.get(nowDisCardSeat);
            if (player == null || !player.checkAutoPlay(0, false)) {
                return;
            }
            autoChuPai(player);
        }
    }

    public void autoChuPai(NyMjPlayer player) {

        if (!player.noNeedMoCard()) {
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
        // TjMj mj = TjMj.getMajang(mjId);

		while ((mjId = handMjIds.get(index)) > 0 && cantDisKingCard(player, Arrays.asList(Mj.getMajang(mjId)))) {
			mjId = -1;
			index -= 1;
        }
        if (mjId != -1) {
            List<Mj> mjList = MjHelper.toMajiang(Arrays.asList(mjId));
            playCommand(player, mjList, MjDisAction.action_chupai);
        }
    }

    public void autoPiao(NyMjPlayer player) {
        int piaoPoint = 0;
        if (gameModel.getSpecialPlay().getTableStatus() != MjConstants.TABLE_STATUS_PIAO) {
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

    @Override
    public void initExtend0(JsonWrapper extend) {
        for (NyMjPlayer player : seatMap.values()) {
            player.initExtend(extend.getString(player.getSeat()));
        }
        String huListstr = extend.getString(5);
        if (!StringUtils.isBlank(huListstr)) {
            huConfirmMap = DataMapUtil.implode(huListstr);
        }
        moMajiangSeat = extend.getInt(7, 0);
        int moGangMajiangId = extend.getInt(8, 0);
        if (moGangMajiangId != 0) {
            moGang = Mj.getMajang(moGangMajiangId);
        }
        String moGangHu = extend.getString(9);
        if (!StringUtils.isBlank(moGangHu)) {
            moGangHuList = StringUtil.explodeToIntList(moGangHu);
        }
        String gangDisMajiangstr = extend.getString(10);
        if (!StringUtils.isBlank(gangDisMajiangstr)) {
            gangDisMajiangs = MjHelper.explodeMajiang(gangDisMajiangstr, ",");
        }
        int gangMajiang = extend.getInt(11, 0);
        if (gangMajiang != 0) {
            this.gangMajiang = Mj.getMajang(gangMajiang);
        }

        askLastMajaingSeat = extend.getInt(12, 0);
        moLastMajiangSeat = extend.getInt(13, 0);
        int lastMajiangId = extend.getInt(14, 0);
        if (lastMajiangId != 0) {
            this.lastMajiang = Mj.getMajang(lastMajiangId);
        }
        fristLastMajiangSeat = extend.getInt(15, 0);
        disEventAction = extend.getInt(16, 0);

        //创房选项
        gameModel = Optional.ofNullable(JSONObject.parseObject(extend.getString(17), GameModel.class)).orElseGet(GameModel::new);

        tempActionMap = loadTempActionMap(extend.getString("tempActions"));

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

        isAutoPlay = extend.getInt(47, 0);
        autoPlayGlob = extend.getInt(48, 0);
        int nowMoCardId = extend.getInt(49, 0);
        if (nowMoCardId > 0)
            nowMoCard = Mj.getMajang(nowMoCardId);
        int floorCardId = extend.getInt(50, 0);
        if (floorCardId > 0)
            floorCard = Mj.getMajang(floorCardId);
        int kingCardId = extend.getInt(51, 0);
        if (kingCardId > 0)
            kingCard = Mj.getMajang(kingCardId);

        lastWinSeat = extend.getInt(52, 0);
        followMaster = extend.getInt(53, 0) > 0;
        reappointment = extend.getInt(54, 0);
        nowGangSeat = extend.getInt(55, 0);
    }

    @Override
    public JsonWrapper buildExtend0(JsonWrapper wrapper) {
        // 1-4 玩家座位信息
        for (NyMjPlayer player : seatMap.values()) {
            wrapper.putString(player.getSeat(), player.toExtendStr());
        }
        wrapper.putString(5, DataMapUtil.explode(huConfirmMap));


        wrapper.putInt(7, moMajiangSeat);
        wrapper.putInt(8, moGang != null ? moGang.getId() : 0);
        wrapper.putString(9, StringUtil.implode(moGangHuList, ","));
        wrapper.putString(10, MjHelper.implodeMajiang(gangDisMajiangs, ","));
        wrapper.putInt(11, gangMajiang != null ? gangMajiang.getId() : 0);
        wrapper.putInt(12, askLastMajaingSeat);
        wrapper.putInt(13, moLastMajiangSeat);
        wrapper.putInt(14, lastMajiang != null ? lastMajiang.getId() : 0);
        wrapper.putInt(15, fristLastMajiangSeat);
        wrapper.putInt(16, disEventAction);
        wrapper.putString(17, JSONObject.toJSON(gameModel).toString());
        JSONArray tempJsonArray = new JSONArray();
        for (int seat : tempActionMap.keySet()) {
            tempJsonArray.add(tempActionMap.get(seat).buildData());
        }
        wrapper.putString("tempActions", tempJsonArray.toString());
        wrapper.putString(31, StringUtil.implode(showMjSeat, ","));
        wrapper.putInt(32, maxPlayerCount);
        wrapper.putInt(33, gangDice);
        wrapper.putString(34, StringUtil.implode(moTailPai, ","));
        wrapper.putString(35, StringUtil.implode(moLastSeats, ","));
        wrapper.putInt(36, isBegin ? 1 : 0);
        wrapper.putInt(37, dealDice);
        wrapper.putInt(47, isAutoPlay);
        wrapper.putInt(48, autoPlayGlob);
        wrapper.putInt(49, nowMoCard == null ? 0 : nowMoCard.getId());
        wrapper.putInt(50, floorCard == null ? 0 : floorCard.getId());
        wrapper.putInt(51, kingCard == null ? 0 : kingCard.getId());
        wrapper.putInt(52, lastWinSeat);
        wrapper.putInt(53, followMaster ? 1 : 0);
        wrapper.putInt(54, reappointment);
        wrapper.putInt(55, nowGangSeat);
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

        GameModel.SpecialPlay specialPlay = GameModel.SpecialPlay.builder()
                //七对
                .sevenPairs(true)
                //允许自摸
                .selfMoHu(true)
                //清一色
                .allOfTheSameColor(true)
				//碰碰胡
				.ppHu(true)
                //天地胡要求
                .kingHuNum(99)
                .floorHuNum(99)
                //门子重复计算
                .repeatedEffect(false)
                //没有小胡
                .onlyDaHu(true)
                //漏胡过张
                .passHuLimit(true)
                //杠算分
                .gangBuF(true)
				//抓鸟
				.birdNum(StringUtil.getIntValue(params, 10, 1))
				//留底牌
				.stayLastCard(StringUtil.getIntValue(params, 10, 1))
				//一鸟全中
				.oneBirdScore(StringUtil.getIntValue(params, 11, 0) > 0)
				//杠翻倍
				.gangDouble(StringUtil.getIntValue(params, 12, 0) > 0)
				.buChi(true)
				.calcBird(3)
                .build();

//        if (specialPlay.getCalcBird() != 2 && specialPlay.getCalcBird() != 3) {
//            specialPlay.setCalcBird(2);
//        }

        gameModel = GameModel.builder().specialPlay(specialPlay)
                .gameFinishRound(bureauCount)
                //加倍：0否，1是
                .doubleChip(StringUtil.getIntValue(params, 19, 0))
                //加倍分
                .doubleChipLeChip(StringUtil.getIntValue(params, 20, 0))
                //加倍数
                .doubleRatio(StringUtil.getIntValue(params, 21, 0))
                //支付方式
                .payType(StringUtil.getIntValue(params, 2, 1))
                //比赛人数
                .gameMaxHuman(StringUtil.getIntValue(params, 7, 4))
                //无字
				.noneChar(StringUtil.getIntValue(params, 8, 4) == 0)
//                //点炮设置
                .ignite(false)
                //1.王霸
				//在所有玩家起完13张牌，庄家起了14张牌后，此时需要翻王，庄家将最前面的一张牌翻出，此牌的下一张即为王，如翻出5筒，6筒是王，翻出9筒，1筒是王。。
				//王可充当任何牌使用，但不参与碰和杠。王不可以打出去的，一直留在手里
//                .createKingCard(StringUtil.getIntValue(params, 37, 0) > 0)
                .createKingCard(true)
                //需要听牌才能杠
                .needTingGang(false)
                //需要258将
//                .need258(StringUtil.getIntValue(params, 4, 1) > 0)
                .need258(true)
                //飘分: 己方飘1对方飘1,赢家+2
                //飘分类型,8抬飘, 9追飘, 定飘1,2,3 飘分11,12,13
                .flutterScoreType(StringUtil.getIntValue(params, 5, 1))
                //基础分, 这是个倍数
                .basicScore(StringUtil.getIntValue(params, 6, 1))
				//金马翻倍
                .allBirdDouble(StringUtil.getIntValue(params, 9, 0) > 0)
				//王牌能否出手
				.canOutKing(StringUtil.getIntValue(params, 13, 0) > 0)
				.need258(false)
                .build();

        maxPlayerCount = gameModel.getGameMaxHuman();
        totalBureau = gameModel.getGameFinishRound();

        super.setPayType(gameModel.getPayType());

        if (gameModel.getFlutterScore() < 0 || gameModel.getFlutterScore() > 3) {
            gameModel.setFlutterScore(0);
        }

        if (!IntStream.of(/*1, 2, 3, */11, 12, 13/*, 8, 9*/).anyMatch(v -> gameModel.getFlutterScore() == v)) {
            gameModel.setFlutterScore(0);
        }

        if (!IntStream.of(1, 2, 4, 6).anyMatch(v -> gameModel.getSpecialPlay().getBirdNum() == v)) {
			gameModel.getSpecialPlay().setBirdNum(0);
		}


        if (maxPlayerCount == 2) {
            int belowAdd = StringUtil.getIntValue(params, 34, 0);
            if (belowAdd <= 100 && belowAdd >= 0)
                gameModel.setLowScoreAdd(belowAdd);
            int below = StringUtil.getIntValue(params, 35, 0);
            if (below <= 100 && below >= 0) {
                gameModel.setLowScoreLimit(below);
                if (belowAdd > 0 && below == 0)
                    gameModel.setLowScoreLimit(10);
            }
        }

        //点炮
//        if (gameModel.isIgnite()) {
//            gameModel.setIgniteScoreArrive(0);
//        } else {
//            //10倍不计分：
//            //所有10倍以上牌型听牌了可以接炮，如果没人放炮，自己又没摸到，被别人胡了之后不用扣分。
//            gameModel.setIgniteScoreArrive(10);
//            gameModel.setIgnite(true);
//        }

        if (maxPlayerCount != 2) {
            gameModel.setDoubleChip(0);
        }

        gameModel.setTopFen(gameModel.getTopFen() > 0 ? 10 : 0);

        if (maxPlayerCount > 2 || (gameModel.getDiscardHoleCards() > 0 && gameModel.getDiscardHoleCards() % 13 != 0)) {
            //人数在2时才进行抽牌
            gameModel.setDiscardHoleCards(0);
        }


        isAutoPlay = StringUtil.getIntValue(params, 28, 0);
        this.autoPlayGlob = StringUtil.getIntValue(params, 29, 0);
        playedBureau = 0;
        changeExtend();
        // getRoomModeMap().put("1", "1"); //可观战（默认）

        LogUtil.msgLog.info("createTable tid:" + getId() + " " + player.getName() + " params" + params.toString());
    }



    /**
     * @param
     * @return
     * @description 听牌信息
     * @author Guang.OuYang
     * @date 2019/10/16
     */
    public void sendTingInfo(NyMjPlayer player) {
        if (player.noNeedMoCard()) {        //起手庄家
            DaPaiTingPaiRes.Builder tingInfo = DaPaiTingPaiRes.newBuilder();
            List<Mj> cards = new ArrayList<>(player.getHandMajiang());

            for (Mj card : player.getHandMajiang()) {
                cards.remove(card);
                List<Mj> huCards = MjTool.getTingMjs(cards, player.getGang(), player.getPeng(), player.getChi(),
                        player.getBuzhang(), getGameModel().isNeed258(), gameModel.getSpecialPlay().isOnlyDaHu(), gameModel.getSpecialPlay().isQuanQiuRenJiang() ? 1 : 0, this, player);
                cards.add(card);

                LogUtil.printDebug("玩家{}...听牌:{}", player.getName(), huCards);

                if (huCards == null || huCards.size() == 0) {
                    continue;
                }

                DaPaiTingPaiInfo.Builder ting = DaPaiTingPaiInfo.newBuilder();
                ting.setMajiangId(card.getId());
                for (Mj mj : huCards) {
                    ting.addTingMajiangIds(mj.getId());
                }
                tingInfo.addInfo(ting.build());
            }
            if (tingInfo.getInfoCount() > 0) {
                player.writeSocket(tingInfo.build());
            }
        } else {//起手闲家
            List<Mj> cards = new ArrayList<>(player.getHandMajiang());
            List<Mj> huCards = MjTool.getTingMjs(cards, player.getGang(), player.getPeng(), player.getChi(),
                    player.getBuzhang(), getGameModel().isNeed258(), gameModel.getSpecialPlay().isOnlyDaHu(), gameModel.getSpecialPlay().isQuanQiuRenJiang() ? 1 : 0, this, player);

            LogUtil.printDebug("玩家{}...听牌:{}", player.getName(), huCards);

            if (huCards == null || huCards.size() == 0) {
                return;
            }

            TingPaiRes.Builder ting = TingPaiRes.newBuilder();
            for (Mj mj : huCards) {
                ting.addMajiangIds(mj.getId());
            }
            player.writeSocket(ting.build());
        }
    }

    /**
     * @param
     * @return
     * @description 检测飘分消息
     * @author Guang.OuYang
     * @date 2019/10/23
     */
    public boolean checkFlutterScoreInfo() {
        boolean flag = true;
        if (gameModel.isFlutterScore() && !gameModel.signFlutterAllOver()) {
        	int count = 0;
            for (Iterator<NyMjPlayer> iterator = seatMap.values().iterator(); iterator.hasNext(); ) {
                NyMjPlayer player = iterator.next();

                //定飘，是固定的飘分
                if (player.getFlutter()==0&&gameModel.getFlutterScoreType() < 10) {
                    player.setFlutter(gameModel.getFlutterScoreType());
                    broadMsgToAll(ComRes.newBuilder().addAllParams(Arrays.asList(Long.valueOf(player.getUserId()).intValue(), gameModel.getFlutterScoreType()))
                            .setCode(WebSocketMsgType.req_code_flutter_score_res).build());

                    //设置选择完毕，下个回合前不再进入该逻辑
                    gameModel.setFlutterScore(-1);

                    changeExtend();
                    LogUtil.printDebug("闲家可以飘分{}...定飄:{}"/*, huCards*/, player.getName(), player.getFlutter());

                }

                else if (player.getFlutter() == 0) {
                    player.setFlutter(-2);
                    player.changeExtend();
                    gameModel.incFlutterScoreFlag();
                    changeExtend();
                    LogUtil.printDebug("闲家可以飘分...听牌:{}"/*, huCards*/, player.getName());
                }

                if (player.getFlutter() == 0 || player.getFlutter() == -2) {
                    LogUtil.printDebug("飄分參數:{}",gameModel.getFlutterScoreType());
                    //额外飘分消息
                    player.writeSocket(ComRes.newBuilder().setCode(WebSocketMsgType.req_code_flutter_score)
                            .addParams(gameModel.getFlutterScoreType())
//                            .addParams(gameModel.getFlutterScoreType())
                            .build());
					count ++ ;
                }

                flag = count > 0;
            }

            if (!flag) {
                gameModel.setFlutterScore(-1);
                changeExtend();
                LogUtil.printDebug("不选择飘分...");
            }
        }else {
            flag = false;
        }

        return flag;
    }

    public static void loadWanfaTables(Class<? extends BaseTable> cls) {
        for (Integer integer : wanfaList) {
            TableManager.wanfaTableTypesPut(integer, cls);
        }
    }

    /**
     * 是否可以胡小胡
     *
     * @param actionIndex CSMajiangConstants类定义
     * @return
     */
    public boolean canXiaoHu(int actionIndex) {

        switch (actionIndex) {
            case MjAction.QUEYISE:
                return gameModel.getSpecialPlay().isQueYiSe();
            case MjAction.BANBANHU:
                return gameModel.getSpecialPlay().isBlackSkyHu();
            case MjAction.YIZHIHUA:
                return gameModel.getSpecialPlay().isYiZhiHua();
            case MjAction.LIULIUSHUN:
                return gameModel.getSpecialPlay().isLiuliuShun();
            case MjAction.DASIXI:
                return gameModel.getSpecialPlay().isDaSiXi();
            case MjAction.JINGTONGYUNU:
                return gameModel.getSpecialPlay().isJinTongYuNv();
            case MjAction.JIEJIEGAO:
                return gameModel.getSpecialPlay().isJieJieGao();
            case MjAction.SANTONG:
                return gameModel.getSpecialPlay().isSanTong();
            case MjAction.ZHONGTULIULIUSHUN:
                return gameModel.getSpecialPlay().isZhongTuLiuLiuShun();
            case MjAction.ZHONGTUSIXI:
                return gameModel.getSpecialPlay().isZhongTuSiXi();
            default:
                return false;
        }
    }

    public void logFaPaiTable() {
        StringBuilder sb = new StringBuilder();
        sb.append("NyMj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append("faPai");
        sb.append("|").append(playType);
        sb.append("|").append(maxPlayerCount);
        sb.append("|").append(getPayType());
        sb.append("|").append(gameModel.getSpecialPlay().getCalcBird());
        sb.append("|").append(gameModel.getSpecialPlay().getBirdNum());
        sb.append("|").append(gameModel.getSpecialPlay().getKePiao());
        sb.append("|").append(gameModel.getSpecialPlay().isQueYiSe() ? 1 : 0);
        sb.append("|").append(gameModel.getSpecialPlay().isBlackSkyHu() ? 1 : 0);
        sb.append("|").append(gameModel.getSpecialPlay().isYiZhiHua() ? 1 : 0);
        sb.append("|").append(gameModel.getSpecialPlay().isLiuliuShun() ? 1 : 0);
        sb.append("|").append(gameModel.getSpecialPlay().isDaSiXi() ? 1 : 0);
        sb.append("|").append(gameModel.getSpecialPlay().isJinTongYuNv() ? 1 : 0);
        sb.append("|").append(gameModel.getSpecialPlay().isJieJieGao() ? 1 : 0);
        sb.append("|").append(gameModel.getSpecialPlay().isSanTong() ? 1 : 0);
        sb.append("|").append(gameModel.getSpecialPlay().isZhongTuSiXi() ? 1 : 0);
        sb.append("|").append(gameModel.getSpecialPlay().isZhongTuLiuLiuShun() ? 1 : 0);
        sb.append("|").append(lastWinSeat);
        LogUtil.msg(sb.toString());
    }

    public void logFaPaiPlayer(NyMjPlayer player, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("NyMj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append("faPai");
        sb.append("|").append(player.getHandMajiang());
        sb.append("|").append(actListToString(actList));
        LogUtil.msg(sb.toString());
    }

    public void logMoMj(NyMjPlayer player, Mj mj, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("NyMj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append("moPai");
        sb.append("|").append(leftMajiangs.size());
        sb.append("|").append(mj);
        sb.append("|").append(actListToString(actList));
        sb.append("|").append(player.getHandMajiang());
        LogUtil.msg(sb.toString());
    }

    public void logChuPaiActList(NyMjPlayer player, Mj mj, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("NyMj");
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

    public void logAction(NyMjPlayer player, int action, int xiaoHuType, List<Mj> mjs, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("NyMj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        String actStr = "unKnown-" + action;
        if (action == MjDisAction.action_peng) {
            actStr = "peng";
        } else if (action == MjDisAction.action_minggang) {
            actStr = "mingGang";
        } else if (action == MjDisAction.action_chupai) {
            actStr = "disCard";
        } else if (action == MjDisAction.action_pass) {
            actStr = "guo";
        } else if (action == MjDisAction.action_angang) {
            actStr = "anGang";
        } else if (action == MjDisAction.action_chi) {
            actStr = "chi";
        } else if (action == MjDisAction.action_buzhang) {
            actStr = "buZhang";
        } else if (action == MjDisAction.action_xiaohu) {
            actStr = "xiaoHu";
        } else if (action == MjDisAction.action_buzhang_an) {
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
				if (i == MjAction.HU || i == MjAction.ZIMO) {
                    sb.append("hu");
                } else if (i == MjAction.PENG) {
                    sb.append("peng");
                } else if (i == MjAction.MINGGANG) {
                    sb.append("mingGang");
                } else if (i == MjAction.ANGANG) {
                    sb.append("anGang");
                } else if (i == MjAction.CHI) {
                    sb.append("chi");
                } else if (i == MjAction.BUZHANG) {
                    sb.append("buZhang");
                } else if (i == MjAction.QUEYISE) {
                    sb.append("queYiSe");
                } else if (i == MjAction.BANBANHU) {
                    sb.append("banBanHu");
                } else if (i == MjAction.YIZHIHUA) {
                    sb.append("yiZhiHua");
                } else if (i == MjAction.LIULIUSHUN) {
                    sb.append("liuLiuShun");
                } else if (i == MjAction.DASIXI) {
                    sb.append("daSiXi");
                } else if (i == MjAction.JINGTONGYUNU) {
                    sb.append("jinTongYuNv");
                } else if (i == MjAction.JIEJIEGAO) {
                    sb.append("jieJieGao");
                } else if (i == MjAction.SANTONG) {
                    sb.append("sanTong");
                } else if (i == MjAction.ZHONGTUSIXI) {
                    sb.append("zhongTuSiXi");
                } else if (i == MjAction.ZHONGTULIULIUSHUN) {
                    sb.append("zhongTuLiuLiuShun");
                } else if (i == MjAction.BUZHANG_AN) {
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
    public void processHideMj(NyMjPlayer player) {
        if (showMjSeat.contains(player.getSeat()) && disCardRound != 0) {
            PlayMajiangRes.Builder hideMj = PlayMajiangRes.newBuilder();
            buildPlayRes(hideMj, player, MjDisAction.action_hideMj, null);
            broadMsgToAll(hideMj.build());
            showMjSeat.remove(Integer.valueOf(player.getSeat()));
        }
    }

    public void clearShowMjSeat() {
        showMjSeat.clear();
        changeExtend();
    }

    public void addShowMjSeat(int seat, int xiaoHuType) {
        if (!showMjSeat.contains(seat)) {
            showMjSeat.add(seat);
            changeExtend();
        }
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

    public boolean isPlayerAllReady() {
        for (Player player : getSeatMap().values()) {
            if (!player.isRobot() && player.getState() != player_state.ready) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isAllReady() {
//        if ((isPlayerAllReady() && getState() != SharedConstants.table_state.play) || (!isBegin && getState() == SharedConstants.table_state.play)) {

        if (super.isAllReady()) {
            //飄分
            if (checkFlutterScoreInfo()) {
                return false;
            }

            if (gameModel.getSpecialPlay().getKePiao() == 1) {
                boolean bReturn = true;
                // 机器人默认处理
                if (this.isTest()) {
                    for (NyMjPlayer robotPlayer : seatMap.values()) {
                        if (robotPlayer.isRobot()) {
                            robotPlayer.setPiaoPoint(1);
                        }
                    }
                }
                for (NyMjPlayer player : seatMap.values()) {
                    if (player.getPiaoPoint() < 0) {
                        ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_table_status_piao, gameModel.getSpecialPlay().getTableStatus());
                        player.writeSocket(com.build());
                        if (gameModel.getSpecialPlay().getTableStatus() != MjConstants.TABLE_STATUS_PIAO) {
                            player.setLastCheckTime(System.currentTimeMillis());
                        }
                        bReturn = false;
                    }
                }
                gameModel.getSpecialPlay().setTableStatus(MjConstants.TABLE_STATUS_PIAO);

                return bReturn;
            } else {
                int point = 0;
                if (gameModel.getSpecialPlay().getKePiao() == 2 || gameModel.getSpecialPlay().getKePiao() == 3 || gameModel.getSpecialPlay().getKePiao() == 4) {
                    point = gameModel.getSpecialPlay().getKePiao() - 1;
                }

                for (NyMjPlayer player : seatMap.values()) {
                    player.setPiaoPoint(point);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * @param gangDice 杠后打的骰子点数
     * @return
     * @description 杠后摸牌
     * @author Guang.OuYang
     * @date 2019/10/16
     */
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
        List<Mj> moList = getGangDisMajiangs();
        if (moList != null && moList.size() > 0 && actionSeatMap.isEmpty()) {
            NyMjPlayer player = seatMap.get(getMoMajiangSeat());
            for (NyMjPlayer seatPlayer : seatMap.values()) {
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
     * @param state  1底单玩家摸海底，0通知玩家关闭摸海底界面
     */
    public void sendMoLast(NyMjPlayer player, int state) {
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
            if (MjAction.getFirstXiaoHu(actList) != -1) {
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
        if (MjAction.getFirstXiaoHu(actList) != -1) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isPlaying() {
        if (super.isPlaying()) {
            return true;
        }
        return gameModel.getSpecialPlay().getTableStatus() == MjConstants.TABLE_STATUS_PIAO;
    }
}
