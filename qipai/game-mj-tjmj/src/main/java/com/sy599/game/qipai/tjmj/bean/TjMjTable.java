package com.sy599.game.qipai.tjmj.bean;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
import com.sy599.game.msg.serverPacket.PlayCardResMsg.*;
import com.sy599.game.msg.serverPacket.TableMjResMsg.ClosingMjInfoRes;
import com.sy599.game.msg.serverPacket.TableMjResMsg.ClosingMjPlayerInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.CreateTableRes;
import com.sy599.game.msg.serverPacket.TableRes.DealInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes;
import com.sy599.game.qipai.tjmj.command.AbsCodeCommandExecutor;
import com.sy599.game.qipai.tjmj.constant.MjAction;
import com.sy599.game.qipai.tjmj.constant.MjConstants;
import com.sy599.game.qipai.tjmj.rule.Mj;
import com.sy599.game.qipai.tjmj.rule.MjHelper;
import com.sy599.game.qipai.tjmj.rule.MjRobotAI;
import com.sy599.game.qipai.tjmj.rule.MjRule;
import com.sy599.game.qipai.tjmj.tool.MjQipaiTool;
import com.sy599.game.qipai.tjmj.tool.MjResTool;
import com.sy599.game.qipai.tjmj.tool.MjTool;
import com.sy599.game.udplog.UdpLogger;
import com.sy599.game.util.*;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author liuping ????????????????????????
 */
@Data
public class TjMjTable extends BaseTable {
    public static final List<Integer> wanfaList = Arrays.asList(GameUtil.play_type_tjmj);

    @Override
    public int getWanFa() {
        return GameUtil.play_type_tjmj;
    }


    @Override
    public String getGameName() {
        return "????????????";
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
                json.put("autoName", "??????");
            } else {
                json.put("autoName", "??????");
            }
        }
        return JSON.toJSONString(json);
    }

    /**
     * ??????????????????
     */
    private GameModel gameModel;
    /**
     * ????????????????????????
     */
    private List<Mj> nowDisCardIds = new ArrayList<>();
    /**
     * ????????????????????????????????? com.sy599.game.qipai.tjmj.constant.MjAction
     * ??????0;???1;??????2;??????3;???4;??????5;?????????6;?????????7;????????? 8;?????????9;?????????10;????????????11;?????????12;??????13;????????????14;???????????????15;????????????16;??????17;?????????18
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
    private List<Mj> leftMajiangs = new ArrayList<>();
    /**
     * ??????????????????????????????map
     */
    private Map<Long, TjMjPlayer> playerMap = new ConcurrentHashMap<Long, TjMjPlayer>();
    /**
     * ???????????????????????????MAP
     */
    private Map<Integer, TjMjPlayer> seatMap = new ConcurrentHashMap<Integer, TjMjPlayer>();
    /**
     * ???????????????
     */
    private Map<Integer, Integer> huConfirmMap = new HashMap<>();
    /**
     * ?????????????????????????????? ??????????????????????????????????????? 1??????????????????????????????????????? ??????????????????????????? ??????????????????
     * 2???????????????????????????????????????????????????????????????????????????????????????????????? ?????????????????????????????????????????? ?????????????????????????????????????????????
     */
    private Map<Integer, MjTempAction> tempActionMap = new ConcurrentHashMap<>();

    //??????????????????,????????????????????????????????????
    private int fromSeat;
    private List<Integer> showMjLists = new ArrayList<>();
    /**
     * ????????????seat
     */
    private int moMajiangSeat;
    /**???????????????seat,?????????????????????????????????????????????????????????*/
    private int nowGangSeat;
    //?????????????????????
    private int curGangSeat;
    /**
     * ???????????????
     */
    private Mj moGang;
    /**
     * ??????????????????
     */
    private Mj gangMajiang;
    /**
     * ?????????
     */
    private List<Integer> moGangHuList = new ArrayList<>();
    /**
     * ?????????????????????
     */
    private List<Mj> gangDisMajiangs = new ArrayList<>();
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
    private Mj lastMajiang;

    /**
     * ?????????????????????
     */
    private Mj nowMoCard;
    /**
     * ??????/ ??????/ ?????????, ???????????????
     */
    private Mj kingCard;
    private Mj kingCard2;
    /**
     * ??????, ???????????????
     */
    private Mj floorCard;

    //??????, ??????
    private Mj tmpFloorCard;
    //??????, ??????
    private Mj tmpKingCard;
    private Mj tmpKingCard2;
    /**
     * ??????????????????????????????,?????????
     */
    private int firstFloorIndex;
    /**???????????????????????????*/
    private int dealDiceFloorKing;

    /**
     *
     */
    private int disEventAction;

    /*** ????????????????????????????????? */
    private List<Integer> showMjSeat = new ArrayList<>();

    /*** ???????????? **/
    private int gangDice = -1;

    /*** ????????????????????? */
    private List<Integer> moTailPai = new ArrayList<>();

    /**
     * ????????????????????????????????????
     **/
    private Mj gangActedMj = null;

    /**
     * ???????????????
     **/
    private boolean isBegin = false;

    //??????
    private int dealDice;


    /**
     * ??????1????????????2?????????
     */
    private int autoPlayGlob;
    private int autoTableCount;
    private int isAutoPlay;// ????????????
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
    	if(state!=table_state.play){
    		return;
		}
        List<Integer> winSeatList = new ArrayList<>(huConfirmMap.keySet());
        try {
            //????????????
            winSeatList.stream().forEach(TjMjTable.this::sendOverSpecialEffect);
        } catch (Exception e) {
            LogUtil.errorLog.error("calOver.sendOverSpecialEffect winList:{},e:{}", winSeatList, e);
        }

        boolean selfMo = false;
        Integer[] birdMjIds = null;
        List<Integer> seatBirds = new ArrayList<>();
        List<Integer> birdsIds = new ArrayList<>();
        //??????->?????????
        Map<Integer, Integer> seatBirdMap = new HashMap<>();
        boolean flow = false;

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

        try {
            StringBuilder sb = new StringBuilder();
            Iterator<Integer> iterator = winSeatList.iterator();
            while (iterator.hasNext()) {
                Integer ac = iterator.next();
                if (ac != null && seatMap.containsKey(ac)) {
                    sb.append(seatMap.get(ac).getDahu());
                    sb.append(";");
                }
            }
            LogUtil.msgLog.info("tjmj_room_calcover:{},{},{},{}",
                    getId(),
                    getPlayBureau(),
                    sb,
                    ExceptionUtils.getStackTrace(new Throwable()).trim().replace("\r\n", "/").substring(0, 480));
        } catch (Exception e) {
        }

        if (zhuaNiao) {
            // ??????
//            if (getLeftMajiangCount() == 0) {
//                birdMjIds = zhuaNiao(lastMajiang);
//            } else {
            // ?????????
            birdMjIds = zhuaNiao(null);
//            }

            int startSeat = winSeatList.get(0);
            //?????????????????????????????????1??????
            for (int i = 1; i <= seatMap.keySet().size(); i++) {
                //?????????,??????????????????, ???????????????
                if (gameModel.getSpecialPlay().getBirdOption() == 2) {
                    if (startSeat != winSeatList.get(0)) {
                        seatBirdMap.put(startSeat, 0);
                        continue;
                    }
                    LogUtil.printDebug("?????????:::seat:{},num:{}", startSeat, gameModel.getSpecialPlay().getBirdNum());
                    seatBirdMap.put(startSeat, gameModel.getSpecialPlay().getBirdNum());

                    //???????????????????????????
                    for (int j = 0; j < gameModel.getSpecialPlay().getBirdOption(); j++) {
                        seatBirds.add(i);
                    }
                } else {
                    seatBirdMap.put(startSeat, gameModel.birdIsThisSeat(i, startSeat, birdMjIds, seatBirds, birdsIds));
                    LogUtil.printDebug("??????:::seat:{}->{},????????????:{},num:{}", startSeat, seatMap.get(startSeat).getName(), i, seatBirdMap.get(startSeat));
                }

                startSeat = calcNextSeat(startSeat);
            }

            for (int i = 0; i < birdMjIds.length; i++) {
                if (!birdsIds.contains(Mj.getMajang(birdMjIds[i]).getId())) {
                    birdsIds.add(birdMjIds[i]);
                }
            }

//            for (int i = 0 ;i<birdMjIds.length;i++){
//                LogUtil.printDebug("?????????:{}", Mj.getMajang(birdMjIds[i]));
//            }
//
//            for(int i = 0 ; i < seatBirds.size();i++){
//                LogUtil.printDebug("????????????:{},{},{}", seatBirds.get(i), Mj.getMajang(birdMjIds[i]), seatMap.get(seatBirds.get(i)).getName());
//            }

        } else {
            for (int seat : seatMap.keySet()) {
                seatBirdMap.put(seat, 0);
            }
        }

        // ?????????
        if (winSeatList.size() != 0) {
            // ??????????????????????????????
            TjMjPlayer winPlayer = null;
            if (winSeatList.size() == 1) {
                winPlayer = seatMap.get(winSeatList.get(0));

                if ((winPlayer.noNeedMoCard() || winPlayer.isGangshangHua()) && winSeatList.get(0) == moMajiangSeat) {
                    selfMo = true;
                }
            }

            //??????????????????1, ???????????????????????????, ????????????, ????????????????????????
            else if(winSeatList.size() > 1) {
                try {
                    LogUtil.msgLog.info("tjmj_room|calcover|winner>1|{}|{}|{}|{}|{}|{}", getId(), getPlayBureau(), winSeatList, getActionSeatMap(), getNowDisCardIds(), ExceptionUtils.getStackTrace(new Throwable()).trim().replace("\r\n", "/").substring(0, 480));
                    if (seatMap.containsKey(disCardSeat)) {
                        int huSeat = calcNextSeat(disCardSeat);
                        int index = 0;
                        while(!winSeatList.contains(huSeat)) {
                            //?????????
//                            if (!winSeatList.contains(huSeat)) {
//                                huSeat = winSeatList.get(0);
//                            }
                            huSeat = calcNextSeat(huSeat);
                            index ++;
                            if (index>=seatMap.size()) {
                                break;
                            }
                        }

                        winPlayer = seatMap.get(huSeat);

                        if (winPlayer == null) {
                            winPlayer = seatMap.get(winSeatList.get(0));
                        }

                        if ((winPlayer.noNeedMoCard() || winPlayer.isGangshangHua()) && winSeatList.get(0) == moMajiangSeat) {
                            selfMo = true;
                        }
                        LogUtil.msgLog.info("tjmj_room|calcover|winner>1,optionwinner|{}|{}|{}|{}|{}|{}", getId(), getPlayBureau(), winSeatList, disCardSeat, calcNextSeat(disCardSeat), winPlayer.getSeat());
                    }
                } catch (Exception e) {
                    LogUtil.errorLog.error("tjmj_room,{},{}", getId(), e);
                    winPlayer = seatMap.get(winSeatList.get(0));
                }
            }

            // ??????,??????, ????????????
            if (selfMo) {

				if (!gameModel.isEightKing()) {
					four4kingSelfTouch(selfMo, seatBirdMap, winPlayer);
				} else {
					eight8kingSelfTouch(selfMo, seatBirdMap, winPlayer);
				}

			}else {
                //?????????????????????????????????
                //??????, ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                //??????????????????????????????????????????
                //????????????
                TjMjPlayer losePlayer = seatMap.get(disCardSeat);

                boolean isQiangGHu = winPlayer.getDahu().contains(9);

                boolean isGangUpGun = winPlayer.getDahu().contains(7);

                boolean isBaoTing = losePlayer.isSignTing();
                //?????????&????????????
                boolean gangUpGun = isGangUpGun || isQiangGHu || isBaoTing;

                losePlayer.changeAction(0, 1);
                losePlayer.getMyExtend().setMjFengshen(FirstmythConstants.firstmyth_index10, winSeatList.size());

                TjMjPlayer calcScorePlay;
//                int basicScore;

                //?????????????????????,???????????????
                List<Integer> addTionMenZi = new ArrayList<>();
                //???????????????????????????
                if (gangUpGun) {
                    //?????????,??????????????????
                    calcScorePlay = losePlayer;
//                    basicScore = gameModel.getSpecialPlay().getSelfMo();

                    //??????
                    List<Mj> cards = new ArrayList<>(losePlayer.getHandMajiang());
                    List<Mj> huCards = MjTool.getTingMjs(cards, losePlayer.getGang(), losePlayer.getPeng(), losePlayer.getChi(),
                            losePlayer.getBuzhang(), true, gameModel.getSpecialPlay().isOnlyDaHu(), gameModel.getSpecialPlay().isQuanQiuRenJiang() ? 1 : 0, this, losePlayer, false);

                    List<Mj> isntKingTingCard = huCards;//huCards.stream().filter(v -> kingCard != null && v.getVal() != kingCard.getVal()).collect(Collectors.toList());

                    Iterator<Mj> iterator1 = isntKingTingCard.iterator();
                    MjiangHu mjiangHu = null;
                    int maxScore = 0;
                    while (iterator1.hasNext()) {
                        Mj next =  iterator1.next();
                        //??????????????????????????????????????????
						MjiangHu fmjiangHu = losePlayer.checkHu(next, isBegin, false);

                        //????????????, ???????????????????????????, ?????????????????????,???????????????????????????,?????????????????????????????????
                        giveKingOrFloorHuCheck(fmjiangHu, next);

						//7?????????????????????,???????????????????????????7???
						giveSevenDuiHuCheck(cards, fmjiangHu);

						fmjiangHu.initDahuList();
                        int curScore = MjiangHu.calcMenZiScore(this, fmjiangHu.buildDahuList(),true);
                        if (mjiangHu == null || curScore > maxScore) {
                            mjiangHu = fmjiangHu;
                            maxScore = curScore;
                        }
                    }

                    //??????????????????,???????????????????????????
                    if (isQiangGHu || isGangUpGun) {
                        mjiangHu.setGangShangHua(true);
                    }

                    //??????????????????????????????
                    if (isGangUpGun || isBaoTing) {
                        mjiangHu.setPingHu(false);

                        for (TjMjPlayer p : seatMap.values()){
                            if (p.getSeat() != losePlayer.getSeat() && p.getSeat() != winPlayer.getSeat()) {
                                p.setDahu(new ArrayList<>(0));
                            }
                        }
                    }

                    mjiangHu.initDahuList();

                    losePlayer.setDahu(mjiangHu.buildDahuList());

                    //??????????????????,????????????,??????????????????????????????
                    Iterator<Integer> iterator = winPlayer.getDahu().iterator();
                    while (iterator.hasNext()) {
                        Integer next = iterator.next() + 1;
                        if (next != 7 && next != 8 && next!=10) {
                            iterator.remove();
                        }
                    }

                    addTionMenZi.addAll(winPlayer.getDahu());

                    LogUtil.printDebug("??????????????????:{}, {}",  MjTool.dahuListToString(addTionMenZi), winPlayer.getDahu());
                    ArrayList<Integer> integers = new ArrayList<>(losePlayer.getDahu());
                    integers.addAll(addTionMenZi);
                    losePlayer.setDahu(integers);
//                    winPlayer.setDahu(calcScorePlay.getDahu());
                } else {
                    //???????????????,??????????????????
                    calcScorePlay = winPlayer;
                    losePlayer.setDahu(Collections.emptyList());
//                    basicScore = gameModel.getSpecialPlay().getGunHu();
                }

                //??????????????????,???????????????????????????, ??????+1
                int winScore = MjiangHu.calcMenZiScore(this, calcScorePlay.getDahu(), gangUpGun);

                //??????????????????????????????, ??????????????????
//                int winScore = MjiangHu.calcMenZiScore(this, calcScorePlay.getDahu(), basicScore);


                losePlayer.getDahu().removeAll(addTionMenZi);


                List<Integer> calcScoreSeat = new ArrayList<>();

                //?????????, ???????????????????????????????????????????????????????????????
                //?????????????????????????????????????????????????????????,???????????????????????????
                boolean isMaxInMaxOut = gangUpGun && gameModel.getSpecialPlay().isMaxInMaxOut();
                //????????????, ??????????????????*??????
//                if (isMaxInMaxOut) {
//                    //?????????, ????????????, ??????????????????
//                    calcScoreSeat.addAll(seatMap.keySet());
//                } else
                {
                    //????????????,???????????????????????????
                    calcScoreSeat.add(winPlayer.getSeat());
                    calcScoreSeat.add(losePlayer.getSeat());
                }

                //??????????????????????????????
                winScore = calcBirdPoint(winScore, seatBirdMap.get(calcScorePlay.getSeat()), true);

                LogUtil.printDebug("??????????????????:??????:{}, ??????:{}, {}", getGameModel().getSpecialPlay().getPaoHuGungHuBasicScore(gangUpGun), winScore, MjTool.dahuListToString(calcScorePlay.getDahu()));

                int lossTotalSubScore = winScore;

                //????????????
                if (winPlayer.getDahuCount() > 0) {
                    losePlayer.changeActionTotal(MjAction.ACTION_COUNT_DAHU_DIANPAO, 1);
                    winPlayer.changeActionTotal(MjAction.ACTION_COUNT_DAHU_JIEPAO, 1);
                }else{
                    losePlayer.changeActionTotal(MjAction.ACTION_COUNT_XIAOHU_DIANPAO, 1);
                    winPlayer.changeActionTotal(MjAction.ACTION_COUNT_XIAOHU_JIEPAO, 1);
                }

                int allBirdNum = 0;
                for (Iterator<Integer> iterator = calcScoreSeat.iterator(); iterator.hasNext(); ) {
                    Integer seat = iterator.next();
                    //???????????????,???????????????????????????,????????????
                    if (seatBirdMap.get(seat) > 0 && seat != calcScorePlay.getSeat()) {
//                        lossTotalSubScore += calcBirdPoint(winScore, seatBirdMap.get(seat), true);
                        allBirdNum += seatBirdMap.get(seat);
                    }
                }

                lossTotalSubScore = calcBirdPoint(lossTotalSubScore, allBirdNum, true);
                LogUtil.printDebug("???????????????:{}, ???:{}", lossTotalSubScore, allBirdNum);

                //?????????????????????
                if (lossTotalSubScore == 0) {
                    lossTotalSubScore = winScore;
                }

                if(isMaxInMaxOut) {
                    //*????????????
                    lossTotalSubScore *= (seatMap.size() - 1);
                }

                LogUtil.printDebug("?????????:{}", lossTotalSubScore);

                //????????????
//                lossTotalSubScore = gameModel.topFenCalc(lossTotalSubScore);

                LogUtil.printDebug("??????:=>{}", lossTotalSubScore);

                //??????????????????????????????
                winPlayer.changePoint(lossTotalSubScore + winPlayer.getGangPoint());
                losePlayer.changePoint(-lossTotalSubScore + losePlayer.getGangPoint());
            }
        }

//        for (TjMjPlayer seat : seatMap.values()) {
//            if (gameModel.topFen() /*&& maxPlayerCount == 2*/) {
//                if (Math.abs(seat.getLostPoint()) > gameModel.getTopFen()) {
//                    seat.setLostPoint(seat.getLostPoint() > 0 ? gameModel.getTopFen() : -gameModel.getTopFen());
//                }
//            }
//            seat.changePoint(seat.getLostPoint() + seat.getGangPoint());
//        }

        boolean over = playBureau == totalBureau;

        if (autoPlayGlob > 0) {
            // //????????????
            boolean diss = false;
            if (autoPlayGlob == 1) {
                for (TjMjPlayer seat : seatMap.values()) {
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

        // ?????????????????????
        ClosingMjInfoRes.Builder res = sendAccountsMsg(over, selfMo, winSeatList, birdsIds.toArray(new Integer[0]), seatBirds.toArray(new Integer[0]), seatBirdMap,
                false, lastWinSeat, catchBirdSeat);
        // ????????????
        if (!flow) {
            if (winSeatList.size() > 1) {
                //  ???????????????????????????????????????
                //????????????????????????????????????????????????
                setLastWinSeat(getNextSeat(disCardSeat));
            } else {
                //????????????
                setLastWinSeat(winSeatList.get(0));
            }

        } else {
            //?????????????????????????????????????????????
            setLastWinSeat(moLastMajiangSeat);

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

        for (TjMjPlayer player : seatMap.values()) {
            if (player.isAutoPlaySelf()) {
                player.setAutoPlay(false, false);
            }

            calcOverAfterClearInfo(player);
            player.saveBaseInfo();
        }
		if(over){
			state=table_state.over;
		}
    }

    /**
     *@description 7???????????????
     *@param
     *@return
     *@author Guang.OuYang
     *@date 2020/4/22
     */
	private void giveSevenDuiHuCheck(List<Mj> cards, MjiangHu fmjiangHu) {
		if(fmjiangHu.isSevenDui()){
			//????????????4????????????
			long count = cards.stream().collect(Collectors.groupingBy(Mj::getVal)).values().stream().filter(v -> !CollectionUtils.isEmpty(v) && v.size() >= 4).mapToInt(v -> 1).count();
			//???2???4???,??????????????????
			if (count >= 2) {
				fmjiangHu.setShuang7xiaodui(true);
				fmjiangHu.setHao7xiaodui(false);
				fmjiangHu.setXiaodui(false);
			}else if(count >= 1){ //???1???4???,???????????????7???
				fmjiangHu.setHao7xiaodui(true);
				fmjiangHu.setShuang7xiaodui(false);
				fmjiangHu.setXiaodui(false);
			}else{  //??????4???,?????????7??????????????????7???
				fmjiangHu.setShuang7xiaodui(false);
				fmjiangHu.setHao7xiaodui(false);
				fmjiangHu.setXiaodui(true);
			}
		}
	}

	/**
     *@description 4???????????????
     *@param
     *@return
     *@author Guang.OuYang
     *@date 2020/2/18
     */
	private void four4kingSelfTouch(boolean selfMo, Map<Integer, Integer> seatBirdMap, TjMjPlayer winPlayer) {
		//????????????
		boolean isDaHu = winPlayer.getDahuCount() > 0;

		//??????????????????,???????????????????????????, ??????+1
		int winScore = MjiangHu.calcMenZiScore(this, winPlayer.getDahu(), selfMo);

		int totalWinScore = 0;

		LogUtil.printDebug("??????????????????:{}, {}", winScore, MjTool.dahuListToString(winPlayer.getDahu()));

		//???????????????
		winScore = calcBirdPoint(winScore, seatBirdMap.get(winPlayer.getSeat()), true);

		LogUtil.printDebug("???????????????:{}, ???:{}", winScore, seatBirdMap.get(winPlayer.getSeat()));

		Iterator<TjMjPlayer> iterator = seatMap.values().iterator();
		while (iterator.hasNext()) {
			TjMjPlayer player = iterator.next();
			if (player.getSeat() != winPlayer.getSeat()) {
				player.setDahu(Collections.emptyList());
				//??????????????????
				int lossScore = calcBirdPoint(winScore, seatBirdMap.get(player.getSeat()), true);

				LogUtil.printDebug(player.getName() + ",???:{}, ???:{} , ??????lossScore:{}", seatBirdMap.get(player.getSeat()), winScore, lossScore);

				player.changePoint(-lossScore + player.getGangPoint());

				totalWinScore += lossScore;
			}else{
				winPlayer.changeActionTotal(isDaHu ? MjAction.ACTION_COUNT_DAHU_ZIMO : MjAction.ACTION_COUNT_XIAOHU_ZIMO, 1);
			}
		}

		LogUtil.printDebug("?????????:{}", totalWinScore);

		//??????
//                totalWinScore = gameModel.topFenCalc(totalWinScore);

		LogUtil.printDebug("??????:=>{}", totalWinScore);

		//?????????
		winPlayer.changePoint(totalWinScore + winPlayer.getGangPoint());
		winPlayer.changeAction(7, 1);
		winPlayer.getMyExtend().setMjFengshen(FirstmythConstants.firstmyth_index8, 1);
	}

	/**
	 *@description 8???????????????
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/2/18
	 */
	private void eight8kingSelfTouch(boolean selfMo, Map<Integer, Integer> seatBirdMap, TjMjPlayer winPlayer) {
		//????????????
		boolean isDaHu = winPlayer.getDahuCount() > 0;

		//??????????????????,???????????????????????????, ??????+1
		int winScore = MjiangHu.calcMenZiScore(this, winPlayer.getDahu(), selfMo);

		int totalWinScore = 0;

		LogUtil.printDebug("??????????????????:{}, {}", winScore, MjTool.dahuListToString(winPlayer.getDahu()));

		//???????????????
		winScore = calcBirdPoint(winScore, seatBirdMap.get(winPlayer.getSeat()), true);

		LogUtil.printDebug("???????????????:{}, ???:{}", winScore, seatBirdMap.get(winPlayer.getSeat()));

		Iterator<TjMjPlayer> iterator = seatMap.values().iterator();
		while (iterator.hasNext()) {
			TjMjPlayer losePlayer = iterator.next();
			if (losePlayer.getSeat() != winPlayer.getSeat()) {
				if (losePlayer.isSignTing()) {  //8????????????????????????????????????,???????????????*??????????????????

					//??????
					List<Mj> cards = new ArrayList<>(losePlayer.getHandMajiang());
					List<Mj> huCards = MjTool.getTingMjs(cards, losePlayer.getGang(), losePlayer.getPeng(), losePlayer.getChi(),
							losePlayer.getBuzhang(), true, gameModel.getSpecialPlay().isOnlyDaHu(), gameModel.getSpecialPlay().isQuanQiuRenJiang() ? 1 : 0, this, losePlayer, false);

					List<Mj> isntKingTingCard = huCards;//huCards.stream().filter(v -> kingCard != null && v.getVal() != kingCard.getVal()).collect(Collectors.toList());

					Iterator<Mj> iterator1 = isntKingTingCard.iterator();
					MjiangHu mjiangHu = null;
					int maxScore = 0;
					while (iterator1.hasNext()) {
						Mj next =  iterator1.next();
						//??????????????????????????????????????????
						MjiangHu fmjiangHu = losePlayer.checkHu2(next, isBegin, false);

						//????????????, ???????????????????????????, ?????????????????????,???????????????????????????,?????????????????????????????????
						giveKingOrFloorHuCheck(fmjiangHu, next);

						fmjiangHu.initDahuList();
						int curScore = MjiangHu.calcMenZiScore(this, fmjiangHu.buildDahuList(),true);
						if (mjiangHu == null || curScore > maxScore) {
							mjiangHu = fmjiangHu;
							maxScore = curScore;
						}
					}

					mjiangHu.initDahuList();

					losePlayer.setDahu(mjiangHu.buildDahuList());

					//?????????
					int loseScore = MjiangHu.calcMenZiScore(this, losePlayer.getDahu(), true);
					loseScore = calcBirdPoint(loseScore, seatBirdMap.get(losePlayer.getSeat()), true);

					int totalLoseScore = 0;
					for (int i = 1 ; i <= seatMap.size(); i++){
						if (i == losePlayer.getSeat()) {
							continue;
						}
						//?????????????????????
						totalLoseScore += calcBirdPoint(loseScore, seatBirdMap.get(i), true);
					}

					totalWinScore += totalLoseScore;

					losePlayer.changePoint(-totalLoseScore + losePlayer.getGangPoint());
				} else {
					losePlayer.setDahu(Collections.emptyList());
					//??????????????????
					int lossScore = calcBirdPoint(winScore, seatBirdMap.get(losePlayer.getSeat()), true);

					LogUtil.printDebug(losePlayer.getName() + ",???:{}, ???:{} , ??????lossScore:{}", seatBirdMap.get(losePlayer.getSeat()), winScore, lossScore);

					losePlayer.changePoint(-lossScore + losePlayer.getGangPoint());

					totalWinScore += lossScore;
				}
			}else{
				winPlayer.changeActionTotal(isDaHu ? MjAction.ACTION_COUNT_DAHU_ZIMO : MjAction.ACTION_COUNT_XIAOHU_ZIMO, 1);
			}
		}

		LogUtil.printDebug("?????????:{}", totalWinScore);

		//??????
//                totalWinScore = gameModel.topFenCalc(totalWinScore);

		LogUtil.printDebug("??????:=>{}", totalWinScore);

		//?????????
		winPlayer.changePoint(totalWinScore + winPlayer.getGangPoint());
		winPlayer.changeAction(7, 1);
		winPlayer.getMyExtend().setMjFengshen(FirstmythConstants.firstmyth_index8, 1);
	}

	public boolean isKingCard(Mj mj) {
		return mj != null && ((kingCard != null && mj.getVal() == kingCard.getVal()) || (kingCard2 != null && mj.getVal() == kingCard2.getVal()));
	}

	/**
     *@description 
     *@param
     *@return 
     *@author Guang.OuYang
     *@date 2019/12/13
     */
    private void giveKingOrFloorHuCheck(MjiangHu mjiangHu, Mj finalMj) {
        //???????????????
//        boolean isKingCard = (finalMj != null && kingCard != null && finalMj.getVal() == kingCard.getVal()) ;
        boolean isKingCard = isKingCard(finalMj);
        //???????????????
        boolean isFloorCard = (finalMj != null && floorCard != null && finalMj.getVal() == floorCard.getVal()) ;
        //????????????????????????????????????????????????????????????????????????
        if (isKingCard) {
			mjiangHu.setTianhu(false);
			//???????????????,???????????????????????????,????????????????????????
			if (mjiangHu.isTiantiantianhu()) {
				mjiangHu.setTiantiantianhu(false);
				mjiangHu.setTiantianhu(true);
				mjiangHu.setTianhu(false);
			} else if (mjiangHu.isTiantianhu()) {
				mjiangHu.setTiantianhu(false);
				mjiangHu.setTianhu(true);
			}
		} else if (isFloorCard) {
            mjiangHu.setDihu(false);
            //???????????????,???????????????????????????,????????????????????????
            if (mjiangHu.isDidihu()) {
                mjiangHu.setTiantianhu(false);
                mjiangHu.setTianhu(true);
            }
        }
    }

    /**
     * @param
     * @return
     * @description ?????????????????????????????????
     * @author Guang.OuYang
     * @date 2019/10/24
     */
    public void calcOverAfterClearInfo(TjMjPlayer player) {
        player.setFirstDisCard(true);
        player.setKingCardNumHuFlag(0);
        player.setFloorCardNumHuFlag(0);
        player.setSignTing(0);
        player.setSignTingPao(0);
        player.setTakeCardFlag(0);
        setNowGangSeat(0);
    }

    private boolean checkAuto3() {
        boolean diss = false;
        // if(autoPlayGlob==3) {
        boolean diss2 = false;
        for (TjMjPlayer seat : seatMap.values()) {
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


    private void calZiMoPoint(Map<Integer, Integer> seatBirdMap, TjMjPlayer winPlayer, boolean dahu, int xiaohuNum,
                              boolean addBirdPoint, boolean zimo) {

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

        if (gameModel.getSpecialPlay().getCalcBird() == 1 && addBirdPoint) {
            // ??????????????????
            point = point + bird;
        } /*else if (gameModel.getSpecialPlay().getCalcBird() == 2) {
            // ?????????2???bird??????
            point = (int) (point * (Math.pow(2, bird)));
        } */ else if (gameModel.getSpecialPlay().getCalcBird() == 2) {
            // ??????
            point *= Math.max(bird * 2, 1);
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
        int point = 6;
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
        TjMjPlayer player = seatMap.get(seat);
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
        for (TjMjPlayer player : playerMap.values()) {
            player.addRecord(logId, playBureau);
        }
        UdpLogger.getInstance().sendSnapshotLog(masterId, playLog, logRes);
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
     * ??????, ???????????????
     *
     * @return
     */
    private Integer[] zhuaNiao(Mj lastMaj) {
        // ?????????
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
        // ???????????????
        return bird;
    }

    /**
     * ???????????????????????????
     * <p>
     * rdMajiangIds
     *
     * @param bankerSeat
     * @return
     */
    private int[] birdToSeat(int[] prickBirdMajiangIds, int bankerSeat) {
        int[] seatArr = new int[prickBirdMajiangIds.length];


//        for (int i = 0; i < prickBirdMajiangIds.length; i++) {
//            Mj majiang = Mj.getMajang(prickBirdMajiangIds[i]);
//            int prickbirdPai = majiang.getPai();
//
//            int prickbirdseat = 0;
//            if (maxPlayerCount == 4) {
//                prickbirdPai = (prickbirdPai - 1) % 4;// ?????????????????? ?????????1
//                prickbirdseat = prickbirdPai + bankerSeat > 4 ? prickbirdPai + bankerSeat - 4
//                        : prickbirdPai + bankerSeat;
//            } else if (maxPlayerCount == 3) {
//                // ????????????
//                if (gameModel.getSpecialPlay().getBirdOption() == 2) {
//                    prickbirdPai = (prickbirdPai - 1) % 3;// ?????????????????? ?????????1
//                    prickbirdseat = prickbirdPai + bankerSeat > 3 ? prickbirdPai + bankerSeat - 3
//                            : prickbirdPai + bankerSeat;
//                } else {
//                    // 4-8 ??????
//                    if (prickbirdPai == 1 || prickbirdPai == 5 || prickbirdPai == 9) {
//                        prickbirdseat = bankerSeat;
//                    } else if (prickbirdPai == 2 || prickbirdPai == 6) {
//                        // ?????????
//                        prickbirdseat = (bankerSeat % 3) + 1;
//                    } else if (prickbirdPai == 3 || prickbirdPai == 7) {
//                        // ?????????
//                        prickbirdseat = ((bankerSeat % 3) + 1) % 3 + 1;
//                    }
//                }
//            } else {
//                if (prickbirdPai == 1 || prickbirdPai == 5 || prickbirdPai == 9) {
//                    prickbirdseat = bankerSeat;
//                } else if (prickbirdPai == 3 || prickbirdPai == 7) {
//                    prickbirdseat = (bankerSeat % 2) + 1;
//                }
//
//                // //?????? 2468 ??????
//                // if(prickbirdPai%2==0) {
//                // continue;
//                // }prickbirdseat = (bankerSeat%3)+1;
//                //
//                // prickbirdseat = bankerSeat;
//
//            }
//
//
////            //?????????
////            if (gameModel.getSpecialPlay().getBirdOption() == 2) {
////                prickbirdPai = (prickbirdPai - 1) % 3;// ?????????????????? ?????????1
////                prickbirdseat = prickbirdPai + bankerSeat > 3 ? prickbirdPai + bankerSeat - 3 : prickbirdPai + bankerSeat;
////            } else if (gameModel.getSpecialPlay().getBirdOption() == 1) {  //159??????
////                if (prickbirdPai == 1 || prickbirdPai == 5 || prickbirdPai == 9) {
////                    prickbirdseat = bankerSeat;
////                } else if (prickbirdPai == 3 || prickbirdPai == 7) {
////                    prickbirdseat = (bankerSeat % 2) + 1;
////                }
////            } else if (!(prickbirdPai % 2 == 0)) { //????????????
////                prickbirdseat = (bankerSeat % 2) + 1;
////            }
//
//            seatArr[i] = prickbirdseat;
//        }
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

        for (TjMjPlayer player : seatMap.values()) {
            sendDealInfo(player);

            int kingCardNum = 0;
            int floorCardNum = 0;
            for (Iterator<Mj> iterator = player.getHandMajiang().iterator(); iterator.hasNext(); ) {
				Mj tmpMj = iterator.next();
//				if (tmpMj.getVal() == tmpKingCard.getVal() || (tmpKingCard2 != null && tmpMj.getVal() == tmpKingCard2.getVal()))
				if (isKingCard(tmpMj)) {
					kingCardNum += 1;
				}
				else if (tmpMj.getVal() == tmpFloorCard.getVal()) {
					floorCardNum += 1;
				}

			}

            //?????????????????????3???????????????????????????????????????????????????????????????????????????????????????????????????2+1?????????+????????????????????????????????????
            //????????????????????????4??????????????????????????????????????????????????????????????????????????????????????????????????????4+1????????????+???????????????????????????????????????
            player.setKingCardNumHuFlag(kingCardNum);

            //?????????????????????3?????????????????????????????????????????????????????????+?????????????????????????????????????????????????????????????????????
            //????????????????????????4?????????????????????????????????????????????????????????+???????????????????????????????????????
            player.setFloorCardNumHuFlag(floorCardNum);
        }

        if (checkSignTingInfo()) {
            return;
        }

//        if (!hasXiaoHu()) {
        // ????????????????????????????????????
        TjMjPlayer bankPlayer = seatMap.get(lastWinSeat);
        ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.req_com_kt_ask_dismajiang);// ??????????????????
        bankPlayer.writeSocket(com.build());
        // isBegin = false;
//        }
    }

    /**
     * @param
     * @return
     * @description ????????????????????????????????????
     * @author Guang.OuYang
     * @date 2019/10/21
     */
    private void sendDealInfo(TjMjPlayer tablePlayer) {
        DealInfoRes.Builder res = DealInfoRes.newBuilder();
        List<Integer> actionList = tablePlayer.checkMoCard(null, true);
        try{
            LogUtil.msgLog.info("tjmj_room_deal_msg:{},seat:{},name:{},playBureau:{},disCardRound:{},firstDis:{},handCard:{}, {} ", getId(), tablePlayer.getSeat(),
                    tablePlayer.getName(),
                    getPlayBureau(),
                    getDisCardRound(),
                    tablePlayer.isFirstDisCard(),
                    tablePlayer.getHandMajiang(),
                    actionList);
        }catch (Exception e){
        }

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
        res.setNextDisCardIndex(getAndCalcFirstFloorIndex());
        //??????
        if (floorCard != null)
            res.setDiCardId(floorCard.getId());
        //??????
        if (kingCard != null)
            res.setKingCardId(kingCard.getId());
		if (kingCard2 != null)
			res.setKingCardId((((1000 + kingCard.getId()) * 1000) + kingCard2.getId()));

		res.setDealDiceFloorIndex(this.dealDiceFloorKing);
        logFaPaiPlayer(tablePlayer, actionList);
        tablePlayer.writeSocket(res.build());

        sendTingInfo(tablePlayer);
    }

    /**
     * ??????
     *
     * @param player
     */
    public void moMajiang(TjMjPlayer player, boolean isBuzhang) {
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
        if (disCardRound != 0 && player.noNeedMoCard()) {
            return;
        }
        if (getLeftMajiangCount() == 0) {
            calcOver();
            return;
        }

        // ???????????????????????? ????????????&& isBuzhang
        if (gameModel.getSpecialPlay().isHaiDiLaoYue() && getLeftMajiangCount() == 1) {
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


        /*?????? -> ??????
        ??????: 1.?????? 2.??????
        1.??????:
        ?????? -> ?????? -> ??????
        2.??????:
        ?????? -> ??????(????????????), ?????????????????????????????????, ???????????????????????????

        ?????????????????????:
        2??? -> ???????????? -> ??????????????????*/
        //???????????????????????????????????????????????????????????????????????????????????????
        if (player.getTakeCardFlag() > 0 && gameModel.getSpecialPlay().isPassHuLimit()) {
            player.setTakeCardFlag(0);
            player.changeExtend();
        }
		setCurGangSeat(0);
        // ??????
        nowMoCard = null;
        if (disCardRound != 0) {
            // ????????????????????????????????????????????????
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
            // ???????????????????????????
            // ????????????????????? ?????????
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

        // ????????????
        clearActionSeatMap();
        if (disCardRound == 0) {
            return;
        }
        setMoMajiangSeat(player.getSeat());
        List<Integer> arr = player.checkMoCard(nowMoCard, false);

        if (!arr.isEmpty()) {
//            // ????????????????????????????????????????????????????????????
//            if (!player.getGang().isEmpty() && !checkSameMj(player.getPeng(), nowMoCard)) {
//                arr.set(MjAction.MINGGANG, 0);
//                arr.set(MjAction.ANGANG, 0);
//                arr.set(MjAction.BUZHANG, 0);
//                arr.set(MjAction.BUZHANG_AN, 0);
//            }
            coverAddActionSeat(player.getSeat(), arr);
        }

//        try{
//            LogUtil.msgLog.info("tjmj_room_selfMo2:{},playBureau:{},round:{},leftCard:{},play:{}->seat:{},opera:{}({},{}),action:{}->{},stack:{}",
//                    getId(),
//                    getPlayBureau(),
//                    getDisCardRound(),
//                    getLeftMajiangs().size(),
//                    player.getUserId(),
//                    player.getSeat(),
//                    nowMoCard,
//                    nowMoCard!=null?nowMoCard.getId():"null",
//                    nowMoCard!=null?nowMoCard.getVal():"null",
//                    arr,
//                    getActionSeatMap().get(player.getSeat()),
//                    ExceptionUtils.getStackTrace(new Throwable()).trim().replace("\r\n","/").substring(0, 480));
//        }catch(Exception e){
//        }

        MoMajiangRes.Builder res = MoMajiangRes.newBuilder();
        res.setUserId(player.getUserId() + "");
        res.setRemain(leftMajiangs.size());
        res.setSeat(player.getSeat());
        res.setNextDisCardIndex(getAndCalcFirstFloorIndex());
        // boolean playCommand = !player.getGang().isEmpty() && arr.isEmpty();
        logMoMj(player, nowMoCard, arr);
        for (TjMjPlayer seat : seatMap.values()) {
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
            TjMjPlayer player = seatMap.get(firstSeat);
            if (player.isTingPai(-1, false,false)) {
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
     * @param seat 0????????????????????????>0??????????????????????????????????????????
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
        TjMjPlayer player = seatMap.get(sendSeat);
        sendMoLast(player, 1);
    }

    private void buildPlayRes(PlayMajiangRes.Builder builder, Player player, int action, List<Mj> majiangs) {
        MjResTool.buildPlayRes(builder, player, action, majiangs);
        buildPlayRes1(builder);
    }

    private void buildPlayRes1(PlayMajiangRes.Builder builder) {
        // builder
    }

    /**
     * ?????????
     *
     * @param player
     * @param majiangs   ?????????????????????
     * @param xiaoHuType ???????????? TjMjAction
     * @param action
     */
    public synchronized void huXiaoHu(TjMjPlayer player, List<Mj> majiangs, int xiaoHuType, int action) {
        List<Integer> actionList = actionSeatMap.get(player.getSeat());
        if (actionList == null || actionList.isEmpty() || actionList.get(xiaoHuType) == 0) {// ??????????????????
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

        if (!player.getHandMajiang().containsAll(majiangs)) {// ??????????????????????????????
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
        for (TjMjPlayer seat : seatMap.values()) {
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
     * ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     */
    public void checkBegin(TjMjPlayer player) {
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
//            ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.req_com_kt_ask_dismajiang);// ??????????????????
//            bankPlayer.writeSocket(com.build());
//        }
    }

    /**
     * @param
     * @return
     * @description ????????????(?????????)&?????????(???????????????)
     * @author Guang.OuYang
     * @date 2019/10/16
     */
    public MjiangHu checkGangUpFlowerOrGun(List<Integer> huMjIds, Map<Integer, MjiangHu> huMap, TjMjPlayer player, MjiangHu huBean, List<Mj> huMjs) {
        // ?????????
        for (int mjId : huMjIds) {
            MjiangHu temp = huMap.get(mjId);
            if (moMajiangSeat == player.getSeat()) {
                if (gameModel.getSpecialPlay().isGangUpFlower()) {
                    temp.setGangShangHua(true);
                }
            } else {
                if (gameModel.getSpecialPlay().isGangUpGun()) {
                    // ????????????
                    TjMjPlayer mPlayer = seatMap.get(moMajiangSeat);
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

    public MjiangHu checkGangUpFlowerOrGun(TjMjPlayer player, List<Mj> majiangs, List<Mj> huMjs) {
        MjiangHu huBean = null;
        Map<Integer, MjiangHu> huMap = new HashMap<>();
        List<Integer> daHuMjIds = new ArrayList<>();
        List<Integer> huMjIds = new ArrayList<>();

        //???????????????????????????
//        int mjId = majiangs.get(0).getId();
//        MjiangHu finalHu = player.checkHu(Mj.getMajang(mjId), disCardRound == 0);
        int maxScore = 0;
        int mjId = 0;
        MjiangHu finalHu = null;
        for (int majiangId : gangSeatMap.keySet()) {
            MjiangHu temp = player.checkHu(Mj.getMajang(majiangId), disCardRound == 0, !isHasGangAction(player.getSeat()));
            if (!temp.isHu()) {
                continue;
            }
            temp.setGangShangHua(true);
            int curScore = MjiangHu.calcMenZiScore(this, temp.buildDahuList(), true);
            if (maxScore == 0 || curScore > maxScore) {
                maxScore = curScore;
                finalHu = temp;
                mjId = majiangId;
            }

            temp.setGangShangHua(false);
        }
        LogUtil.printDebug("?????????:{},{}",Mj.getMajang(mjId), finalHu.buildDahuList());

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
            // ????????????
            huBean = checkGangUpFlowerOrGun(huMjIds, huMap, player, huBean, huMjs);
        } else {
            huBean = new MjiangHu();
        }

        return huBean;
    }

    /**
     * ???????????????
     *
     * @param player
     * @param majiangs
     */
    private void hu(TjMjPlayer player, List<Mj> majiangs, int action) {
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return;
        }

        //??????????????????
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
        LogUtil.printDebug("{},{}????????????,????????????14???:{}, ????????????:{}", player.getName(), player.getSeat(), zimo, isHasGangAction(player.getSeat()));

        boolean isRobGangHu = false;
        if (!zimo) {
            if (gameModel.getSpecialPlay().isRobGangHu() && moGangHuList.contains(player.getSeat())) {// ?????????
                disMajiang = moGang;
                fromSeat = moMajiangSeat;
                huMjs.add(moGang);
                isRobGangHu = true;
            } else if ((gameModel.getSpecialPlay().isGangUpFlower() || gameModel.getSpecialPlay().isGangUpGun()) && isHasGangAction(player.getSeat())) {// ????????? ?????????

                //???????????????????????????
//                if (/*moMajiangSeat == player.getSeat() &&*/ majiangs.isEmpty()) {
//                    return;
//                }

                fromSeat = moMajiangSeat;

                //???????????? , ?????????
                huBean = checkGangUpFlowerOrGun(player, majiangs, huMjs);

                isGangShangHu = huBean.isGangShangHua();

                if (huBean.isHu()) {
                    if (disCardSeat == player.getSeat()) {
                        zimo = true;
                    }
                }

            } else if (lastMajiang != null && (gameModel.getSpecialPlay().isHaiDiLaoYue() || gameModel.getSpecialPlay().isHaiDiPao())) {
                huBean = player.checkHu(lastMajiang, disCardRound == 0);
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

            } else if (!nowDisCardIds.isEmpty()) {
                disMajiang = nowDisCardIds.get(0);
                fromSeat = disCardSeat;
                huMjs.add(disMajiang);
            }
        } else {
            huMjs.add(player.getHandMajiang().get(player.getHandMajiang().size() - 1));
        }

        if (huBean == null) {
            // ??????
            huBean = player.checkHu(disMajiang, disCardRound == 0);
            if (huBean.isHu() && lastMajiang != null && gameModel.getSpecialPlay().isHaiDiLaoYue()) {
                huBean.setHaidilaoyue(true);
                huBean.initDahuList();
            }
        }

        if (isRobGangHu && gameModel.getSpecialPlay().isRobGangHu()) {
            // ???????????????
//                List<Integer> hu = player.checkDisCard(disMajiang, false, false);
//                if (!hu.isEmpty() && hu.get(0) == 1) {
            huBean.setHu(true);
            huBean.setQiangGangHu(true);
            huBean.initDahuList();
//                }
//            if (!huBean.isHu()) {
//                return;
//            }
        }

        // ????????????
        if (!huBean.isMenqing() && (gameModel.getSpecialPlay().isMenqing() && !player.isChiPengGang())) {
            huBean.setMenqing(true);
            huBean.initDahuList();
        }

        // ?????????????????????????????????
//        if (gameModel.getSpecialPlay().isSkyHu() && disCardRound == 0) {
//            huBean.setTianhu(true);
//            huBean.initDahuList();
//        } else if (gameModel.getSpecialPlay().isFloorHu() && player.isFirstDisCard()) {
//            huBean.setDihu(true);
//            huBean.initDahuList();
//        }

        // ???????????????
        if (gameModel.getSpecialPlay().isRobGangHu() && moGangHuList.contains(player.getSeat())) {
            // ??????????????????????????????
//            if (disEventAction != MjDisAction.action_buzhang) {
//                huBean.setQGangHu(true);
//                huBean.initDahuList();
//            }
            // ?????????
            TjMjPlayer moGangPlayer = seatMap.get(nowDisCardSeat);
            List<Mj> moGangMajiangs = new ArrayList<>();
            moGangMajiangs.add(moGang);
            //????????????,????????????????????????
//            moGangPlayer.removeGangMj(moGangMajiangs.get(0));
            // ?????????????????? ??????????????????????????????
            recordDisMajiang(moGangMajiangs, moGangPlayer);
        }

        if (isGangShangHu) {
            // ????????????????????????????????????????????????????????????
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

        this.fromSeat = fromSeat;
        this.showMjLists = huBean.getShowMajiangs().stream().map(v -> v.getId()).collect(Collectors.toList());

//        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
//        buildPlayRes(builder, player, action, huBean.getShowMajiangs());
//
//        if (zimo) {
//            builder.setZimo(1);
//        }

//        LogUtil.printDebug("????????????:{}, {}, {}", zimo, player.getKingCardNumHuFlag(), gameModel.getSpecialPlay().getKingHuNum());

        huBean.initDahuList();

        player.setDahu(huBean.buildDahuList());

        try {
            LogUtil.msgLog.info("tjmj_room_hu:{},{},{},{},{},{},{},{},{},{},{}",
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
                    ExceptionUtils.getStackTrace(new Throwable()).trim().replace("\r\n", "/").substring(0, 480));
        } catch (Exception e) {
            LogUtil.errorLog.error("tjmj_room_hu error:{}", e);
        }

//        builder.addAllHuArray(player.getDahu());
//        builder.setFromSeat(fromSeat);
//        // ???
//        for (TjMjPlayer seat : seatMap.values()) {
//            // ????????????
//            seat.writeSocket(builder.build());
//        }
        // ??????????????????
        addHuList(player.getSeat(), disMajiang == null ? 0 : disMajiang.getId());
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + MjHelper.toMajiangStrs(huMjs) + "_" + StringUtil.implode(player.getDahu(), ","));

        // ?????????????????? ???????????????????????????
        // ??????????????????
        if (isCalcOver()) {
            calcOver();
        } else {
            player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip);
        }
    }

    /**buildbui
     *@description ????????????????????????
     *@param
     *@return
     *@author Guang.OuYang
     *@date 2019/12/9
     */
    public void sendOverSpecialEffect(Integer seatNum) {
        TjMjPlayer player = seatMap.get(seatNum);
        try {
            PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
            buildPlayRes(builder, player, MjDisAction.action_hu, !CollectionUtils.isEmpty(showMjLists) ? this.showMjLists.stream().map(v -> Mj.getMajang(v)).collect(Collectors.toList()) : Collections.emptyList());

            builder.setZimo(player.noNeedMoCard() ? 1 : 0);

            builder.addAllHuArray(player.getDahu());

            builder.setFromSeat(fromSeat);
            // ???
            for (TjMjPlayer seat : seatMap.values()) {
                // ????????????
                seat.writeSocket(builder.build());
            }
        } catch (Exception e) {
            LogUtil.errorLog.error("sendOverSpecialEffect error, {}, {}", seatNum, player, seatMap);
            throw e;
        }
    }

    private void checkRemoveMj(TjMjPlayer player, int action) {
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
            // ??????????????????
            List<Mj> list = new ArrayList<>();
            list.add(mjB);
            checkMoOutCard(list, player, action);
        }
    }

    private void removeGangMj(TjMjPlayer player, int mjId) {
        List<Mj> moList = new ArrayList<>();
        moList.add(Mj.getMajang(mjId));
        player.addOutPais(moList, 0, player.getSeat());
    }

    /**
     * ?????????????????????????????????
     *
     * @param majiang
     * @return
     */
    private TjMjPlayer getPlayerByHasMajiang(Mj majiang) {
        for (TjMjPlayer player : seatMap.values()) {
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
            TjMjPlayer moGangPlayer = null;
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

        if (over) {
            //??????

            //??????????????????-----------------
            if (!gameModel.getSpecialPlay().isOneGunMultiRing()) {
                int nextSeat = calcNextSeat(disCardSeat);
                //?????????????????????????????????
                while (!huConfirmMap.containsKey(nextSeat)) {
                    nextSeat = calcNextSeat(nextSeat);
                }

                Iterator<Entry<Integer, Integer>> iterator = huConfirmMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    Entry<Integer, Integer> next = iterator.next();
                    if (nextSeat != next.getKey()) {
                        //???????????????????????????????????????
                        TjMjPlayer play = seatMap.get(next.getKey());
                        if (play != null && !CollectionUtils.isEmpty(play.getDahu())) {
                            play.setDahu(Collections.emptyList());
                        }
                        iterator.remove();
                    }
                }
            }
            //----------------
        }

        if (!over) {
            TjMjPlayer disCSMajiangPlayer = seatMap.get(disCardSeat);
            for (int huseat : huActionList) {
                if (huConfirmMap.containsKey(huseat)) {
                    if (disCardRound == 0) {
                        // ??????
                        removeActionSeat(huseat);
                    }
                    continue;
                }
                PlayMajiangRes.Builder disBuilder = PlayMajiangRes.newBuilder();
                TjMjPlayer seatPlayer = seatMap.get(huseat);
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
    private void chiPengGang(TjMjPlayer player, List<Mj> majiangs, int action) {
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        logAction(player, action, 0, majiangs, null);

        List<Integer> huList = getHuSeatByActionMap();
        huList.remove((Object) player.getSeat());

        // ????????????????????????????????????????????????????????????????????????
        if (nowDisCardIds.size() > 1) {
            for (Mj mj : nowDisCardIds) {
				List<Integer> hu = player.checkDisCard(mj, false, false, isKingCard(mj), true);
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
        if (isHasGangAction()) {
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
                    if (mj != null && !CollectionUtils.isEmpty(majiangs) && majiangs.get(0).getVal() == mj.getVal()) {
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
            // ???????????? ????????????????????????????????????
            majiangs = MjHelper.getMajiangList(player.getHandMajiang(), majiangs.get(0).getVal());
            sameCount = majiangs.size();
            if (sameCount == 4) {
                // ???4????????????????????????
                action = MjDisAction.action_angang;
            } else if (sameCount == 0) {
                majiangs.add(disMajiang);
            }
            // ???????????????

        } else if (action == MjDisAction.action_buzhang_an) {
            // ????????????
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
                player.writeErrMsg("??????????????????");
                return;
            }
            // ???????????????????????? ????????????&& isBuzhang
            if (leftMajiangs.size() < gameModel.getSpecialPlay().getStayLastCard()) {
                player.writeErrMsg("???????????????");
                return;
            }
            if (!player.isTingPai(majiangs.get(0).getVal(), true, false)) {
                player.writeErrMsg("??????????????????");
                return;
            }

            // ???????????????????????????????????????????????? ???
            if (/*sameCount == 1 &&*/ canAnGangHu()) {
                if (checkQGangHu(player, majiangs, action)) {
                    // return;
                    moMj = false;
                }
            }
        } else if (action == MjDisAction.action_minggang) {
            boolean can = canMingGang(player, player.getHandMajiang(), majiangs, sameCount, disMajiang);
            if (!can) {
                player.writeErrMsg("??????????????????");
                return;
            }
            if (!player.isTingPai(majiangs.get(0).getVal(), true, false)) {
                player.writeErrMsg("??????????????????");
                return;
            }
            // ???????????????????????? ????????????&& isBuzhang
            if (leftMajiangs.size() < gameModel.getSpecialPlay().getStayLastCard()) {
                player.writeErrMsg("???????????????");
                return;
            }
            // ???????????????????????????????????????????????? ???
            if (/*sameCount == 1 &&*/ canGangHu()) {
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
                if (checkQGangHu(player, majiangs, action)) {
                    // ????????????????????????
                    // return;
                    moMj = false;
                }
            }
        } else if (action == MjDisAction.action_buzhang_an) {
            boolean can = false;
            if (sameCount == 4) {
                can = canAnGang(player, majiangs, sameCount, action);
            }
            // ???????????????????????? ????????????&& isBuzhang
            if (getLeftMajiangCount() == 1) {
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
            if (action == MjDisAction.action_minggang && sameCount == 3) {
                // ??????
                disMajiangMove = true;
            } else if (action == MjDisAction.action_chi) {
                // ???
                disMajiangMove = true;
            } else if (action == MjDisAction.action_peng) {
                // ???
                disMajiangMove = true;
            } else if (action == MjDisAction.action_buzhang && sameCount == 3) {
                // ??????????????????
                disMajiangMove = true;
            }
        }
        if (disMajiangMove) {
            if (action == MjDisAction.action_chi) {
                majiangs.add(1, disMajiang);// ?????????????????????
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

    private void chiPengGang(PlayMajiangRes.Builder builder, TjMjPlayer player, List<Mj> majiangs, int action,
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
        // ??????????????????
        setNowDisCardSeat(player.getSeat());
        checkClearGangDisMajiang();
        if (action == MjDisAction.action_chi || action == MjDisAction.action_peng) {
            List<Integer> arr = player.checkMoCard(null, false);
            // ????????????????????????
            if (!arr.isEmpty()) {
                arr.set(MjAction.ZIMO, 0);
                arr.set(MjAction.HU, 0);
                arr.set(MjAction.ZHONGTULIULIUSHUN, 0);
                arr.set(MjAction.ZHONGTUSIXI, 0);
                addActionSeat(player.getSeat(), arr);
            }
        }
        for (TjMjPlayer seatPlayer : seatMap.values()) {
            // ????????????
            PlayMajiangRes.Builder copy = builder.clone();
            if (actionSeatMap.containsKey(seatPlayer.getSeat())) {
                copy.addAllSelfAct(actionSeatMap.get(seatPlayer.getSeat()));
            }
            seatPlayer.writeSocket(copy.build());
        }

        // ????????????
        player.setPassMajiangVal(0);
        if (action == MjDisAction.action_minggang || action == MjDisAction.action_angang) {
            // ?????????????????????
            if (moMj) {
				setCurGangSeat(player.getSeat());
                gangMoMajiang(player, majiangs.get(0), action);
				setCurGangSeat(0);
            }

        } else if (action == MjDisAction.action_buzhang) {
            // ??????
            if (moMj) {
                moMajiang(player, true);
            }

        } else if (action == MjDisAction.action_buzhang_an) {
            // ??????
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
     * ??????????????????
     */
    private void gangMoMajiang(TjMjPlayer player, Mj gangMajiang, int action) {
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
        if (leftMajiangs.size() < gameModel.getSpecialPlay().getStayLastCard()) {
            calcOver();
            return;
        }

        // ???????????????
        int moNum = 2;

        if (gameModel.getSpecialPlay().isGangMoNum()) {
            moNum = 3;
        }

        List<Mj> moList = new ArrayList<>();
        Random r = new Random();
        gangDice = (r.nextInt(6) + 1) * 10 + (r.nextInt(6) + 1);

        while (moList.size() < moNum) {
            Mj majiang = getLeftMajiang(player);
            if (majiang != null) {
                moList.add(majiang);
            } else {
                break;
            }
        }
        addMoTailPai(gangDice);

        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + MjDisAction.action_moGangMjiang + "_" + gangDice
                + "_" + MjHelper.implodeMajiang(moList, ","));

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

        boolean canHu = false;
        Mj moGangMj = null;
//        boolean next=true;
        // ????????????????????????

        int nextSeat = player.getSeat();
        for (int i = 0; i < seatMap.size(); i++) {
            //????????????,??????,??????
            TjMjPlayer seatPlayer = seatMap.get(nextSeat);
            for (Mj majiang : moList) {
				List<Integer> actionList = seatPlayer.checkDisCard(majiang, false, true, seatPlayer.getSeat() != player.getSeat() && isKingCard(majiang), false/*seatPlayer.getSeat() != player.getSeat()*/);
                if (seatPlayer.getSeat() == player.getSeat()) {
                    // ??????????????????
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
//                            actionList.set(MjAction.BUZHANG, 1);
                            moGangMj = majiang;
                            // seatPlayer.moMajiang(majiang);
                        }
                        canHu = true;
                        addActionSeat(player.getSeat(), actionList);
                        List<Integer> list2 = new ArrayList<Integer>(actionList);
                        addGangActionSeat(majiang.getId(), player.getSeat(), list2);
                        logAction(player, action, -1, Arrays.asList(majiang), actionList);
                    }
                    //?????????????????????, ?????????, ???????????????
//                    else if (MjAction.hasGang(actionList)) {
//                        actionList = MjAction.keepHu(actionList);
//                        actionList.set(MjAction.MINGGANG, 1);
////                        actionList.set(MjAction.BUZHANG, 1);
//                        moGangMj = majiang;
//                        // seatPlayer.moMajiang(majiang);
//                        addActionSeat(player.getSeat(), actionList);
//                        List<Integer> list2 = new ArrayList<Integer>(actionList);
//                        addGangActionSeat(majiang.getId(), player.getSeat(), list2);
//                        logAction(seatPlayer, action, -1, Arrays.asList(majiang), actionList);
//                    }
                } else {
                    //??????????????????
                    if (!actionList.isEmpty() && actionList.contains(1)) {
//                        next = checkDisCardAfter(player, Arrays.asList(majiang), true, seatPlayer, actionList);
                        //?????????????????????????????????, ?????????????????????????????????????????????, ???????????????
                        boolean existsHuAction = gangSeatMap.values().stream().anyMatch(v -> v != null && v.values().stream().anyMatch(v1 -> v1 != null && MjAction.hasHu(v1)));
                        //???????????????, ????????????????????????
                        for (int j = 0; j < actionList.size(); j++) {
                            //?????????????????????????????????, ?????????????????????????????????????????????, ???????????????
                            if ((j != MjAction.HU && j != MjAction.ZIMO) || existsHuAction) {
                                actionList.set(j, 0);
                            }
                        }

                        //????????????????????????
                        if (actionList.contains(1)) {
                            List<Integer> list2 = new ArrayList<Integer>(actionList);
                            //?????????, ?????????????????????
                            addGangActionSeat(majiang.getId(), seatPlayer.getSeat(), list2);

                            addActionSeat(seatPlayer.getSeat(), actionList);

                            logAction(seatPlayer, action, -1, Arrays.asList(majiang), actionList);
                        }
                    }
                }
            }
            nextSeat = calcNextSeat(nextSeat);
        }

        if (moGangMj != null) {
            player.moMajiang(moGangMj);
        }

        //???????????????,?????????????????????
        if (isHasGangAction(player.getSeat())) {
            //?????????
            if (!canHu) {
                //??????????????????
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
            }
//            else
            {
                // ??????????????????
                GangMoMajiangRes.Builder gangMsg = GangMoMajiangRes.newBuilder();
                gangMsg.setRemain(leftMajiangs.size());
                gangMsg.setGangId(gangMajiang.getId());
                gangMsg.setUserId(player.getUserId() + "");
                gangMsg.setName(player.getName() + "");
                gangMsg.setSeat(player.getSeat());
                gangMsg.setReconnect(0);
                gangMsg.setDice(gangDice);
                gangMsg.setHasAct(isHasGangAction() ? 1 : 0);
                gangMsg.setMjNum(moList.size());
                gangMsg.setNextDisCardIndex(getAndCalcFirstFloorIndex());
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

                if (!CollectionUtils.isEmpty(gangMsg.getGangActsList())) {
                    List<GangPlayMajiangRes> gangActsList = gangMsg.getGangActsList();
                    gangMsg.clearGangActs();
                    GangPlayMajiangRes.Builder playerMsg = GangPlayMajiangRes.newBuilder();
                    Iterator<GangPlayMajiangRes> iterator = gangActsList.iterator();
                    while (iterator.hasNext()) {
                        GangPlayMajiangRes next1 = iterator.next();
                        playerMsg.setMajiangId(next1.getMajiangId());
                        playerMsg.addAllSelfAct(Collections.emptyList());
                        gangMsg.addGangActs(playerMsg);
                    }
                }

                for (TjMjPlayer seatPlayer : seatMap.values()) {
                    if (player.getSeat() != seatPlayer.getSeat()) {
                        seatPlayer.writeSocket(gangMsg.build());
                        // ?????????????????????????????????????????????????????????
                        removeActionSeat(seatPlayer.getSeat());
                    }
                }
            }

        } else {  //?????????????????????????????????
            // ??????????????????
            player.addOutPais(moList, 0, player.getSeat());

            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + 0 + "_" + MjHelper.toMajiangStrs(moList));
            gangNoticePlayer(player, gangMajiang, moList);

            PlayMajiangRes.Builder chuPaiMsg = PlayMajiangRes.newBuilder();
            buildPlayRes(chuPaiMsg, player, MjDisAction.action_chupai, moList);
            for (TjMjPlayer seatPlayer : seatMap.values()) {
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

    private void checkMoOutCard(List<Mj> list, TjMjPlayer player, int action) {

        player.addOutPais(list, 0, player.getSeat());
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + 0 + "_" + MjHelper.toMajiangStrs(list));
        logAction(player, action, 0, list, null);
        PlayMajiangRes.Builder chuPaiMsg = PlayMajiangRes.newBuilder();
        buildPlayRes(chuPaiMsg, player, MjDisAction.action_chupai, list);
        for (TjMjPlayer seatPlayer : seatMap.values()) {
            chuPaiMsg.setFromSeat(-1);
            seatPlayer.writeSocket(chuPaiMsg.build());
        }
    }

    private void gangNoticePlayer(TjMjPlayer player, Mj gangMajiang, List<Mj> moList) {
        // ??????????????????res
        GangMoMajiangRes.Builder gangMsg = null;
        for (TjMjPlayer seatPlayer : seatMap.values()) {
            gangMsg = GangMoMajiangRes.newBuilder();
            gangMsg.setRemain(leftMajiangs.size());
            gangMsg.setGangId(gangMajiang.getId());
            gangMsg.setUserId(player.getUserId() + "");
            gangMsg.setName(player.getName() + "");
            gangMsg.setSeat(player.getSeat());
            gangMsg.setReconnect(0);
            gangMsg.setDice(gangDice);
            gangMsg.setHasAct(isHasGangAction() ? 1 : 0);
            gangMsg.setMjNum(moList.size());
            gangMsg.setNextDisCardIndex(getAndCalcFirstFloorIndex());
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

    private boolean checkQGangHu(TjMjPlayer player, List<Mj> majiangs, int action) {
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        Map<Integer, List<Integer>> huListMap = new HashMap<>();
        for (TjMjPlayer seatPlayer : seatMap.values()) {
            if (seatPlayer.getUserId() == player.getUserId()) {
                continue;
            }
            // ????????????
			List<Integer> hu = seatPlayer.checkDisCard(majiangs.get(0), false, true, isKingCard(majiangs.get(0)), false);
            hu = MjAction.keepHu(hu);
            if (!hu.isEmpty() && hu.get(0) == 1) {

                if (action != MjDisAction.action_angang) {
					if (MjAction.hasHu(hu)) {
						for (int i = 0 ; i < hu.size();i++){
							if(MjAction.HU!=i && MjAction.ZIMO!=i){
								hu.set(i,0);
							}
						}
					}

					clearAllAction(seatPlayer, hu);
					addActionSeat(seatPlayer.getSeat(), hu);
                    huListMap.put(seatPlayer.getSeat(), hu);
                    nowGangSeat = player.getSeat();
                }else{
                    //??????????????????,???????????????????????????
                    List<Mj> huCards = MjTool.getTingMjs(seatPlayer.getHandMajiang(), seatPlayer.getGang(), seatPlayer.getPeng(), seatPlayer.getChi(),
                            seatPlayer.getBuzhang(), true, gameModel.getSpecialPlay().isOnlyDaHu(), gameModel.getSpecialPlay().isQuanQiuRenJiang() ? 1 : 0, this, seatPlayer, true);
                    //??????????????????????????????,????????????????????????
                    if (!CollectionUtils.isEmpty(huCards) && huCards.size() >= gameModel.getSpecialPlay().getRobAnGangHuTingCardSizeMust()) {
						if (MjAction.hasHu(hu)) {
							for (int i = 0 ; i < hu.size();i++){
								if(MjAction.HU!=i && MjAction.ZIMO!=i){
									hu.set(i,0);
								}
							}
						}

						clearAllAction(seatPlayer, hu);
						addActionSeat(seatPlayer.getSeat(), hu);
                        huListMap.put(seatPlayer.getSeat(), hu);
                        nowGangSeat = player.getSeat();
                    }
                }
            }
        }

        // ????????????
        if (!huListMap.isEmpty()) {
            setDisEventAction(action);
            setMoGang(majiangs.get(0), new ArrayList<>(huListMap.keySet()));
//            buildPlayRes(builder, player, action, majiangs);
//            for (Entry<Integer, List<Integer>> entry : huListMap.entrySet()) {
//                PlayMajiangRes.Builder copy = builder.clone();
//                TjMjPlayer seatPlayer = seatMap.get(entry.getKey());
//                copy.addAllSelfAct(entry.getValue());
//                seatPlayer.writeSocket(copy.build());
//            }
            return true;
        }
        return false;

    }

	private void clearAllAction(TjMjPlayer seatPlayer, List<Integer> hu) {
		if (!CollectionUtils.isEmpty(getActionSeatMap().get(seatPlayer.getSeat()))) {
			for (int i = 0; i < hu.size(); i++) {
				getActionSeatMap().get(seatPlayer.getSeat()).set(i, 0);
			}
		}
	}

	public void checkSendGangRes(Player player) {
        if (isHasGangAction()) {
            List<Mj> moList = getGangDisMajiangs();
            TjMjPlayer disPlayer = seatMap.get(disCardSeat);
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
            gangbuilder.setNextDisCardIndex(getAndCalcFirstFloorIndex());
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
    private void disCard(TjMjPlayer player, List<Mj> majiangs, int action) {
//        try{
//            LogUtil.msgLog.info("tjmj_room_discard:{},playBureau:{},round:{},leftCard:{},play:{}->seat:{},opera:{},action:{}->{},stack:{}",
//                    getId(),
//                    getPlayBureau(),
//                    getDisCardRound(),
//                    getLeftMajiangs().size(),
//                    player.getUserId(),
//                    player.getSeat(),
//                    majiangs,
//                    action,
//                    getActionSeatMap().get(player.getSeat()),
//                    ExceptionUtils.getStackTrace(new Throwable()).trim().replace("\r\n","/").substring(0, 480));
//        }catch(Exception e){
//        }

        if (!gameModel.signTingAllOver()) {
            LogUtil.e(player.getName() + "?????????????????????");
            return;
        }

        if (majiangs.size() != 1) {
            return;
        }
        if (!player.noNeedMoCard()) {
            // ???????????????
            return;
        }

        if (!tempActionMap.isEmpty() && player.getGang().isEmpty()) {
            LogUtil.e(player.getName() + "???????????????????????????");
            clearTempAction();
        }

        if (gameModel.getSpecialPlay().isPassHuLimit() && MjAction.hasHu(getActionSeatMap().get(player.getSeat()))) {
            //???????????????????????????????????????????????????????????????????????????????????????
            player.setTakeCardFlag(player.getDahuScore());
            player.changeExtend();
        }

        if (!player.getGang().isEmpty()) {
            // ??????????????????
            if (player.getLastMoMajiang().getId() != majiangs.get(0).getId()) {
                return;
            }
        }
        if (!actionSeatMap.isEmpty() && player.getGang().isEmpty()) {// ??????????????????????????????
            guo(player, null, MjDisAction.action_pass);
        }
        if (!actionSeatMap.isEmpty() && player.getGang().isEmpty()) {
            player.writeComMessage(WebSocketMsgType.res_code_mj_dis_err, action, majiangs.get(0).getId());
            return;
        }
		setCurGangSeat(0);
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
        boolean next = true;
		int nextSeat = player.getSeat();
        for (int i = 0; i < seatMap.size() - 1; i++) {
            //?????????????????????????????????
            TjMjPlayer seat = seatMap.get(nextSeat=calcNextSeat(nextSeat));
            List<Integer> list = new ArrayList<>();
            if (next && seat.getUserId() != player.getUserId()) {
                //?????????????????????
                list = seat.checkDisCard(majiangs.get(0));
                checkDisCardAfter(player, majiangs, next, seat, list);
            }
        }

        setDisEventAction(action);
        //??????????????????????????????????????????
        sendDisMajiangAction(builder);
        // ????????????
        player.setPassMajiangVal(0);

        if(!CollectionUtils.isEmpty(majiangs)/* && player.getHandMajiang().get(player.getHandMajiang().size()-1) == majiangs.get(0)*/) {
            player.setPassMajiangVal(majiangs.get(0).getVal());
        }

        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + MjHelper.toMajiangStrs(majiangs));
        setIsBegin(false);
        //????????????
        player.setFirstDisCard(false);
        //??????????????????
        sendTingInfo(player);
        // ??????????????????
        checkMo();
    }

    /**
     *@description
     *@param otherSeat ?????????
     *@param majiangs ????????????
     *@param next ???????????? ??????????????????
     *@param selfSeat ?????????
     *@param actionList
     *@return
     *@author Guang.OuYang
     *@date 2019/11/11
     */
    private boolean checkDisCardAfter(TjMjPlayer otherSeat, List<Mj> majiangs, boolean next, TjMjPlayer selfSeat, List<Integer> actionList) {
        try{
            LogUtil.msgLog.info("tjmj_discard_after1_room:{},round:{},leftCard:{},play:{}->seat:{},opera:{},mj:{}->name:{},actionList:{}",
                    getId(),
                    getPlayBureau(),
                    getLeftMajiangs().size(),
                    otherSeat.getUserId(),
                    otherSeat.getSeat(),
                    majiangs,
                    selfSeat.getUserId(),
                    selfSeat.getName(),
                    actionList);
        }catch(Exception e){
        }

        StringBuilder sb = new StringBuilder();
        if (actionList.contains(1)) {
            LogUtil.printDebug("?????????????????????1:{}",actionList);
            //??????????????????????????????????????????????????????
            //???????????????????????????????????????????????????????????????????????????????????????
            //????????????????????????
            //??????????????????>?????????????????????,????????????????????????
            int curScore = MjiangHu.calcMenZiScore(this, selfSeat.checkHu(majiangs.get(0), isBegin).buildDahuList(), getGameModel().getSpecialPlay().selfMoEqGunHu());
            /*?????? -> ??????
            ??????: 1.?????? 2.??????
            1.??????:
            ?????? -> ?????? -> ??????
            2.??????:
            ?????? -> ??????(????????????), ?????????????????????????????????, ???????????????????????????

            ?????????????????????:
            2??? -> ???????????? -> ??????????????????*/

            //1.?????????????????????
            //2.??????????????????????????????,????????????
			//3.???????????????,?????????,????????????
            if (actionList.get(MjAction.QIANG_GANG_HU) != 1 && selfSeat.getTakeCardFlag() != 0 && curScore <= selfSeat.getTakeCardFlag() && (((TjMjTable)otherSeat.getPlayingTable(TjMjTable.class)).getGameModel().isEightKing() || !otherSeat.isSignTing()))
            if (gameModel.getSpecialPlay().isPassHuLimit() && (actionList.get(MjAction.HU) == 1) && (selfSeat.getTakeCardFlag() > 0)) {
                actionList.set(MjAction.HU, 0);
                actionList.set(MjAction.QIANG_GANG_HU, 0);
                sb.append(" stp1." + curScore + "," + selfSeat.getTakeCardFlag());
            }

            //????????????
            if (actionList.get(MjAction.QIANG_GANG_HU) != 1 && (actionList.get(MjAction.PENG) == 1 || actionList.get(MjAction.CHI) == 1 || actionList.get(MjAction.HU) == 1 ) && !CollectionUtils.isEmpty(majiangs) && selfSeat.getPassMajiangVal() == majiangs.get(0).getVal()) {
                actionList.set(MjAction.PENG, 0);
                actionList.set(MjAction.CHI, 0);
                actionList.set(MjAction.HU, 0);
                sb.append(" stp2." + majiangs + "," + selfSeat.getPassMajiangVal());
            }

            //????????????????????????,
            if (actionList.get(MjAction.HU) == 1 && (selfSeat.isSignTing() && selfSeat.getSignTingPao() == 1)) {
            	//2020/2/28  ?????????????????????????????????????????????
//                actionList.set(MjAction.HU, 0);
                sb.append(" stp3.");
            }

            //????????????
            if (!gameModel.getSpecialPlay().isCanBuCard()) {
                actionList.set(MjAction.BUZHANG, 0);
            }

            //?????????????????????????????????
            if (actionList.get(MjAction.HU) == 1 && selfSeat.getPassMajiangVal() == majiangs.get(0).getVal()) {
                actionList.set(MjAction.HU, 0);
                sb.append(" stp4.");
            }
            // ??????????????????????????????????????????????????????
//                    if (!seat.getGang().isEmpty()) {
//                        list.set(MjAction.MINGGANG, 0);
//                        list.set(MjAction.ANGANG, 0);
//                        list.set(MjAction.BUZHANG, 0);
//                    }

            //7?????????????????????
            if (!gameModel.getSpecialPlay().isXiaoDuiGang() && actionList.get(MjAction.XIAO_DUI) == 1) {
                actionList.set(MjAction.MINGGANG, 0);
                actionList.set(MjAction.ANGANG, 0);
                actionList.set(MjAction.BUZHANG, 0);
                sb.append(" stp5.");
            }

            //?????????????????????????????????,
            //???????????????,???????????????????????????????????????
            if (actionList.get(MjAction.HU) == 1 && actionList.get(MjAction.PING_HU) == 1 && (actionList.get(MjAction.YING_ZHUANG) == 0) && !selfSeat.isSignTing() && !otherSeat.isSignTing() && actionList.get(MjAction.QIANG_GANG_HU) == 0) {
                //111
                actionList.set(MjAction.HU, 0);
                actionList.set(MjAction.PING_HU, 0);
                sb.append(" stp6.");
            }

            //???????????????????????????
            if (actionList.get(MjAction.HU) == 1 && gameModel.getSpecialPlay().isBlackSkyHu() && actionList.get(MjAction.BLACK_SKY_HU) == 1) {
                actionList.set(MjAction.HU, 0);
                actionList.set(MjAction.BLACK_SKY_HU, 0);
                sb.append(" stp7.");
            }

            //??????????????????,???????????????, ?????????????????????,????????????,????????????
			//???????????????
			//????????????
            if (actionList.get(MjAction.HU) == 1
                    && selfSeat.getHandMajiang()
                    .stream()
                    .filter(v -> isKingCard(v))
                    .count() >= gameModel.getSpecialPlay().getKingHuNum()
//                    && actionList.get(MjAction.YING_ZHUANG) != 1
					&& actionList.get(MjAction.QIANG_GANG_HU) != 1
					&& !(otherSeat.isSignTing())
			) {
                actionList.set(MjAction.HU, 0);
                sb.append(" stp8.");
            }

			//????????????????????????????????????????????????????????????????????????
			if (gameModel.isEightKing() && actionList.get(MjAction.YING_ZHUANG) != 1) {
				actionList.set(MjAction.HU, 0);
				sb.append(" stp9.");
			}

            //????????????????????????,????????????????????????
            if (selfSeat.isSignTing()) {
                for (int i = 0; i < actionList.size(); i++) {
                    if (actionList.get(i) > 0 && gameModel.getSpecialPlay().signTingNotToDoAction(i)) {
                        actionList.set(i, 0);
                    }
                }
            }

            //??????????????????
            actionList.set(MjAction.BLACK_SKY_HU,0);
            actionList.set(MjAction.PING_HU,0);
            actionList.set(MjAction.YING_ZHUANG,0);
            actionList.set(MjAction.QIANG_GANG_HU,0);
            actionList.set(MjAction.XIAO_DUI,0);
//            actionList.set(MjAction.TIAN_HU,0);


            //????????????, ???????????????????????????
            //???????????????????????????
//            if (!actionList.isEmpty() && !(!gameModel.getSpecialPlay().isOneGunMultiRing() && getActionSeatMap().values().stream().anyMatch(a -> a.get(MjAction.HU) == 1))) {
//                //???????????????????????????
//                actionList.set(MjAction.HU, 0);
//            }

            addActionSeat(selfSeat.getSeat(), actionList);

            logChuPaiActList(selfSeat, majiangs.get(0), actionList);
            LogUtil.printDebug("?????????????????????2:{}",actionList);
        }

        try{
            LogUtil.msgLog.info("tjmj_discard_after2_room:{},round:{},leftCard:{},play:{}->seat:{},opera:{},mj:{}->name:{},actionList:{}, {}",
                    getId(),
                    getDisCardRound(),
                    getLeftMajiangs().size(),
                    otherSeat.getUserId(),
                    otherSeat.getSeat(),
                    majiangs,
                    selfSeat.getUserId(),
                    selfSeat.getName(),
                    actionList, sb.toString());
        }catch(Exception e){
        }


        return next;
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
            if (actionList.get(MjAction.HU) == 1 || actionList.get(MjAction.ZIMO) == 1) {
                // ???
                huList.add(seat);
            }

        }
        return huList;
    }

    /**
     * @param
     * @return
     * @description ??????????????????????????????????????????
     * @author Guang.OuYang
     * @date 2019/10/21
     */
    private void sendDisMajiangAction(PlayMajiangRes.Builder builder) {
        // ????????????????????? ?????????
        // ??????????????????
        buildPlayRes1(builder);
        List<Integer> huList = getHuSeatByActionMap();
        if (huList.size() > 0) {
            // ?????????,?????????
            for (TjMjPlayer seatPlayer : seatMap.values()) {
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
            for (TjMjPlayer seat : seatMap.values()) {
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

    private void err(TjMjPlayer player, int action, String errMsg) {
        LogUtil.e("play:tableId-->" + id + " playerId-->" + player.getUserId() + " action-->" + action + " err:"
                + errMsg);
    }

    /**
     * ??????
     *
     * @param player   ????????????
     * @param majiangs
     * @param action   ????????????
     */
    public synchronized void playCommand(TjMjPlayer player, List<Mj> majiangs, int action) {
//        try{
//            LogUtil.msgLog.info("tjmj_room_playcommon:{},{},{},{},{},{},{},{},{}",
//                    getId(),
//                    getPlayBureau(),
//                    getDisCardRound(),
//                    getLeftMajiangs().size(),
//                    player.getUserId(),
//                    player.getSeat(),
//                    action,
//                    majiangs,
//                    actionSeatMap
//                    );
//        }catch (Exception e){
//        }

        if (!moGangHuList.isEmpty()) {// ???????????????, ???????????????????????????????????????????????????????????????????????????
            if (!moGangHuList.contains(player.getSeat())) {
                return;
            }
        }

        if (MjDisAction.action_hu == action) {
            hu(player, majiangs, action);
            return;
        }

        // ???????????????????????????
        if (!isHasGangAction() && action != MjDisAction.action_minggang && action != MjDisAction.action_buzhang)
            if (!player.getHandMajiang().containsAll(majiangs)) {
                err(player, action, "?????????????????????" + majiangs);
                return;
            }

        if (player.isSignTing() && gameModel.getSpecialPlay().signTingNotToDoActionDis(action) && nowMoCard != null && !majiangs.isEmpty() && majiangs.get(0).getId() != nowMoCard.getId()) {
            err(player, action, "???????????????????????????" + majiangs);
            return;
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
     * ???????????????(?????????)
     *
     * @param player
     * @param action
     */
    public synchronized void moLastMajiang(TjMjPlayer player, int action) {
        if (getLeftMajiangCount() != 1) {
            return;
        }
        if (player.getSeat() != askLastMajaingSeat) {
            return;
        }

        if (action == MjDisAction.action_passmo) {
            // ???????????????????????????res
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
            // ?????????????????????

            // /////////////////////////////////////////

            List<Mj> disMajiangs = new ArrayList<>();
            disMajiangs.add(majiang);

            MoMajiangRes.Builder moRes = MoMajiangRes.newBuilder();
            moRes.setUserId(player.getUserId() + "");
            moRes.setRemain(leftMajiangs.size());
            moRes.setSeat(player.getSeat());
            moRes.setNextDisCardIndex(getAndCalcFirstFloorIndex());

            // ???????????????????????????
            List<Integer> selfActList = player.checkDisCard(majiang);
            player.moMajiang(majiang);
            selfActList = MjAction.keepHu(selfActList);
            if (selfActList != null && !selfActList.isEmpty()) {
                if (selfActList.contains(1)) {
                    addActionSeat(player.getSeat(), selfActList);
                }
            }
            for (TjMjPlayer seatPlayer : seatMap.values()) {
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
            if (MjAction.hasHu(selfActList)) {
                // ???????????????
                // hu(player, null, TjMjDisAction.action_moLastMjiang_hu);
                return;
            } else {
                chuLastPai(player);
            }
            // for (int seat : actionSeatMap.keySet()) {
            // hu(seatMap.get(seat), null, action);
            // }
        }

    }

    private void chuLastPai(TjMjPlayer player) {
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
        for (TjMjPlayer seatPlayer : seatMap.values()) {
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

    private void passMoHu(TjMjPlayer player, List<Mj> majiangs, int action) {
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
        if(!CollectionUtils.isEmpty(nowDisCardIds)){
            player.setPassMajiangVal(nowDisCardIds.get(0).getVal());
        }
        TjMjPlayer moGangPlayer = seatMap.get(nowGangSeat);
        if (moGangHuList.isEmpty() && moGangPlayer != null) {
            majiangs = new ArrayList<>();
            majiangs.add(moGang);
            if (disEventAction == MjDisAction.action_buzhang) {
                moMajiang(moGangPlayer, true);
            } else {
                gangMoMajiang(moGangPlayer, majiangs.get(0), disEventAction);
            }

            // calcPoint(moGangPlayer, TjMjDisAction.action_minggang, 1,
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
    private void guo(TjMjPlayer player, List<Mj> majiangs, int action) {
//        try{
//            LogUtil.msgLog.info("tjmj_room_guo:{},round:{},leftCard:{},play:{}->seat:{},opera:{},{},action:{}->{},stack:{}",
//                    getId(),
//                    getDisCardRound(),
//                    getLeftMajiangs().size(),
//                    player.getUserId(),
//                    player.getSeat(),
//                    majiangs,
//                    nowDisCardIds,
//                    action,
//                    getActionSeatMap().get(player.getSeat()),
//                    ExceptionUtils.getStackTrace(new Throwable()).trim().replace("\r\n","/").substring(0, 650));
//        }catch(Exception e){
//        }

        if (!actionSeatMap.containsKey(player.getSeat())) {
            return;
        }

        if (!moGangHuList.isEmpty()) {
            // ???????????????????????????
            passMoHu(player, majiangs, action);
            return;
        }

        //??????????????????????????????????????????????????????
        if (player.isSignTing() && actionSeatMap.get(player.getSeat()).get(0) == 1) {
            player.setSignTingPao(1);
        }

        if (gameModel.getSpecialPlay().isPassHuLimit() && MjAction.hasHu(getActionSeatMap().get(player.getSeat()))) {
            //???????????????????????????????????????????????????????????????????????????????????????
            player.setTakeCardFlag(MjiangHu.calcMenZiScore(this, player.checkHu((player.noNeedMoCard() || CollectionUtils.isEmpty(getNowDisCardIds()) ? null : getNowDisCardIds().get(0)), isBegin).buildDahuList(), getGameModel().getSpecialPlay().selfMoEqGunHu()));
            player.changeExtend();
        }

//        if (actionSeatMap.get(player.getSeat()).get(0) == 1 || actionSeatMap.get(player.getSeat()).get(17) == 1 || actionSeatMap.get(player.getSeat()).get(1) == 1 || actionSeatMap.get(player.getSeat()).get(4) == 1) {
//            //???????????????????????????????????????????????????????????????????????????????????????
//            player.setTakeCardFlag(player.getDahuScore());
//            player.changeExtend();
//        }
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
                    // ????????????????????????????????????????????????
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
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + MjHelper.toMajiangStrs(majiangs));
        if (isCalcOver()) {
            calcOver();
            return;
        }
        if (MjAction.hasHu(removeActionList) && disCardSeat != player.getSeat() && nowDisCardIds.size() == 1) {
            // ??????
            player.passHu(nowDisCardIds.get(0).getVal());
        }

        // nowDisCardIds.size() == 1
        if (removeActionList.get(0) == 1 && disCardSeat != player.getSeat()) {
            if (nowDisCardIds.size() > 1) {
                for (Mj mj : nowDisCardIds) {
                    List<Integer> hu = player.checkDisCard(mj, false, false, isKingCard(mj), true);
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
            TjMjPlayer disCSMajiangPlayer = seatMap.get(disCardSeat);
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
                TjMjPlayer seatPlayer = seatMap.get(seat);
                seatPlayer.writeSocket(copy.build());
            }
        }
        // && tempActionMap.size()==0

        if (player.noNeedMoCard() && !player.getGang().isEmpty() && actionSeatMap.get(player.getSeat()) == null) {
            // ?????????????????????
            List<Mj> disMjiang = new ArrayList<>();
            disMjiang.add(player.getLastMoMajiang());
            if (isHasGangAction()) {
                checkMoOutCard(disMjiang, player, action);
            } else {
                disCard(player, disMjiang, 0);
            }
        }

//        if (isBegin && xiaoHu == -1 && player.getSeat() == lastWinSeat) {
        if (!checkSignTingInfo() && player.getSeat() == lastWinSeat && actionSeatMap.isEmpty()) {
            // ???????????????????????????????????????
            ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.req_com_kt_ask_dismajiang);
            player.writeSocket(com.build());
        } else {
            checkBegin(player);
        }

        if (player.noNeedMoCard()) {
            sendTingInfo(player);
        }

        // ?????? ???????????????????????????????????????????????????????????????
        refreshTempAction(player);
        checkMo();
    }

    private void calcPoint(TjMjPlayer player, int action, int sameCount, List<Mj> majiangs) {
        if (!gameModel.getSpecialPlay().isGangBuF()) {
            return;
        }
        int lostPoint = 0;
        int getPoint = 0;
        if (action == MjDisAction.action_peng) {
            List<Integer> actionList = actionSeatMap.get(player.getSeat());
            if (actionList.get(2) == 1 || actionList.get(5) == 1) {
                // ?????????????????????
                player.addPassGangVal(majiangs.get(0).getVal());
            }
            return;

        } else if (action == MjDisAction.action_angang || action == MjDisAction.action_buzhang_an) {
            // ??????????????????????????????2???
            lostPoint = -2;
            getPoint = 2 * (getMaxPlayerCount() - 1);

        } else if (action == MjDisAction.action_minggang || action == MjDisAction.action_buzhang) {
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
                TjMjPlayer disPlayer = seatMap.get(disCardSeat);

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
            for (TjMjPlayer seat : seatMap.values()) {
                if (seat.getUserId() == player.getUserId()) {
                    player.changeGangPoint(getPoint);
                } else {
                    seat.changeGangPoint(lostPoint);
                }
            }
        }

    }

    private void calcXiaoHuPoint(TjMjPlayer player, int xiaoIndex) {
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

    private void recordDisMajiang(List<Mj> majiangs, TjMjPlayer player) {
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
     * ????????????
     */
    public void checkMo() {
        if (actionSeatMap.isEmpty()) {
            if (nowDisCardSeat != 0) {
                moMajiang(seatMap.get(nowDisCardSeat), false);

            }
            robotDealAction();

        } else {
            for (int seat : actionSeatMap.keySet()) {
                TjMjPlayer player = seatMap.get(seat);
                if (player != null && player.isRobot()) {
                    // ????????????????????????????????????
                    List<Integer> actionList = actionSeatMap.get(seat);
                    if (actionList == null) {
                        continue;
                    }
                    List<Mj> list = new ArrayList<>();
                    if (!nowDisCardIds.isEmpty()) {
                        list = MjQipaiTool.getVal(player.getHandMajiang(), nowDisCardIds.get(0).getVal());
                    }
                    if (actionList.get(0) == 1) {
                        // ???
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
                // // ???????????????????????????
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
            TjMjPlayer next = seatMap.get(nextseat);
            if (next != null && next.isRobot()) {
                List<Integer> actionList = actionSeatMap.get(next.getSeat());
                int xiaoHuAction = -1;
                if (actionList != null) {
                    List<Mj> list = null;
                    if (actionList.get(0) == 1) {
                        // ???
                        playCommand(next, new ArrayList<Mj>(), MjDisAction.action_hu);

                    } else if ((xiaoHuAction = MjAction.getFirstXiaoHu(actionList)) > 0) {

                        playCommand(next, new ArrayList<Mj>(), MjDisAction.action_pass);

                    } else if (actionList.get(3) == 1) {
                        // ???????????????
                        Map<Integer, Integer> handMap = MjHelper.toMajiangValMap(next.getHandMajiang());
                        for (Entry<Integer, Integer> entry : handMap.entrySet()) {
                            if (entry.getValue() == 4) {
                                // ????????????
                                list = MjHelper.getMajiangList(next.getHandMajiang(), entry.getKey());
                            }
                        }
                        playCommand(next, list, MjDisAction.action_angang);

                    } else if (actionList.get(5) == 1) {
                        // ???????????????
                        Map<Integer, Integer> handMap = MjHelper.toMajiangValMap(next.getHandMajiang());
                        for (Entry<Integer, Integer> entry : handMap.entrySet()) {
                            if (entry.getValue() == 4) {
                                // ????????????
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
                                // ?????????
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

    @Override
    protected void deal() {
        if (lastWinSeat == 0) {
            // ??????????????????
            int masterseat = playerMap.get(masterId).getSeat();
            setLastWinSeat(masterseat);
        }

        getGameModel().setSignTing(0);
        setDisCardSeat(lastWinSeat);
        setNowDisCardSeat(lastWinSeat);
        setMoMajiangSeat(lastWinSeat);
        setNowGangSeat(0);

        List<Integer> copy = null;
        if (maxPlayerCount == 2 && gameModel.getSpecialPlay().isQueYiMen()) {
            copy = new ArrayList<>();
            for (Integer id : MjConstants.zhuanzhuan_mjList) {
                Mj mj = Mj.getMajang(id);
                if (mj.getColourVal() == 1) {
                    continue;
                }
                copy.add(id);
            }
        } else {
            copy = new ArrayList<>(MjConstants.zhuanzhuan_mjList);
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
            assign = Mj.getMajangByVal(zp.get(playerMap.values().size()).get(0));
            leftMjs.remove(0);
            leftMjs.add(assign);
        }

        createKingCardAndFloorCard(assign, leftMjs);

        LogUtil.printDebug("??????:{},??????:{}", floorCard, kingCard);


        int i = 1;
//        List<Integer> removeIndex = new ArrayList<>();
        for (TjMjPlayer player : playerMap.values()) {
            if (player.getSeat() == lastWinSeat) {
                List<Mj> mjs2 = new ArrayList<>(list.get(0));
                if (zp == null || zp.isEmpty()) {
                    removeMaxKingFloorCard(list.get(0), leftMjs, mjs2);
                }
                player.dealHandPais(list.get(0));
                player.clearPlayerOverInfo();
                player.changeState(player_state.play);
//                removeIndex.add(0);
                continue;
            }

            List<Mj> mjs = new ArrayList<>(list.get(i));
            if (zp == null || zp.isEmpty()) {
                removeMaxKingFloorCard(list.get(i), leftMjs, mjs);
            }
            player.dealHandPais(list.get(i));
            player.clearPlayerOverInfo();
            player.changeState(player_state.play);
//            removeIndex.add(i);
            i++;
        }

        // ??????????????????
//        List<Mj> leftMjs = new ArrayList<>();
//        // ???????????????????????????????????????
//        for (int j = 0; j < list.size(); j++) {
//
//            if (!removeIndex.contains(j)) {
//                leftMjs.addAll(list.get(j));
//            }
//        }

        // ??????????????????
        if (gameModel.getDiscardHoleCards() <= 0) {
            setLeftMajiangs(leftMjs);
        } else if (gameModel.getDiscardHoleCards() >= leftMjs.size()) {
            setLeftMajiangs(new ArrayList<>());
        } else if(gameModel.getDiscardHoleCards()>0){
            int size = leftMjs.size();
            //??????
//            leftMjs = leftMjs.subList(0, gameModel.getDiscardHoleCards());
            setLeftMajiangs(leftMjs.subList(gameModel.getDiscardHoleCards(), size));
        }else{
            setLeftMajiangs(leftMjs);
        }

//       try{
//           StringBuilder sbd = new StringBuilder("");
//           seatMap.values().forEach(v-> {
//               sbd.append(v.getSeat() + "(" + v.getName() + ")" + ":" + v.isFirstDisCard() + "_" + v.getHandMajiang());
//           });
//           LogUtil.msgLog.info("tjmj_room_deal:{}, firstAndHandCard:{} ", getId(), sbd.toString());
//       }catch(Exception e){
//       }
    }

    /**
     * @param
     * @return
     * @description ??????????????????
     * @author Guang.OuYang
     * @date 2019/10/30
     */
    private void createKingCardAndFloorCard(Mj assign,List<Mj> leftMajiangs) {
        Random r = new Random();
        int dealDice = (r.nextInt(6) + 1) * 10 + (r.nextInt(6) + 1);
        setDealDice(dealDice);

        int a = (r.nextInt(6) + 1);
        int b = (r.nextInt(6) + 1);
        //?????????=2???
        int dealDiceFloorKing = (a + b) * 2;
        this.dealDiceFloorKing = a * 10 + b;
        int floorCardIndex = leftMajiangs.size() - dealDiceFloorKing;
        LogUtil.printDebug("??????INDEX:{}->{}", floorCardIndex, leftMajiangs.get(floorCardIndex));
        this.firstFloorIndex = dealDiceFloorKing;
        this.tmpFloorCard = leftMajiangs.get((floorCardIndex > leftMajiangs.size() ? leftMajiangs.size() : floorCardIndex) - 1);
        if (assign != null) {
            tmpFloorCard = assign;
        }

        this.tmpKingCard = Mj.getMajang(tmpFloorCard.getId() + 1);
        if (this.tmpKingCard.getHuase() != tmpFloorCard.getHuase()) {
            this.tmpKingCard = Mj.getMajang(tmpKingCard.getId() - 9);
        }


        if(getGameModel().isEightKing()){
			this.tmpKingCard2 = Mj.getMajang(tmpFloorCard.getId() + 2);
			if (this.tmpKingCard2.getHuase() != tmpFloorCard.getHuase()) {
				this.tmpKingCard2 = Mj.getMajang(tmpKingCard2.getId() - 9);
			}
		}


        setKingAndFloorMj();
    }

    public int getAndCalcFirstFloorIndex() {
        return Math.max(leftMajiangs.size() - firstFloorIndex, 0);
    }

    /**
     * @param
     * @return
     * @description ??????????????????????????????????????????
     * @author Guang.OuYang
     * @date 2019/12/2
     */
    public void setKingAndFloorMj() {
        this.kingCard = this.tmpKingCard;
        this.kingCard2 = this.tmpKingCard2;
        this.floorCard = this.tmpFloorCard;
//        for (TjMjPlayer player : seatMap.values()) {
//            sendDealInfo(player);
//        }
    }

    /**
     *@description
     *@param
     *@return
     *@author Guang.OuYang
     *@date 2019/10/30
     */
    private void removeMaxKingFloorCard(List<Mj> list, List<Mj> leftMjs, List<Mj> mjs) {
        int index = 0;
        int floorNum = 0;
        int kingNum = 0;
        //??????????????????>2
        if (tmpFloorCard != null || tmpKingCard != null) {
            Iterator<Mj> iterator = mjs.iterator();
            while (iterator.hasNext()) {
                Mj next = iterator.next();
                if (tmpFloorCard != null && tmpFloorCard.getVal() == next.getVal()) {
                    floorNum++;
                } else if (isKingCard(next)) {
                    kingNum++;
                }

                if (floorNum > 2 || kingNum > 2) {
                    floorNum = 2;
                    kingNum = 2;
                    leftMjs.add(list.get(index));
                    Mj remove = leftMjs.remove(0);
                    while (remove.getVal() == tmpFloorCard.getVal() || isKingCard(remove)) {
                        leftMjs.add(remove);
                        remove = leftMjs.remove(0);
                    }
                    list.set(index, remove);
                }
                index++;
            }
        }
    }

    /**
     * ???????????????????????????
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
     * ?????????????????????
     *
     * @return
     */
    public Mj getLeftMajiang(TjMjPlayer player/*, int ... count*/) {
//        if (count.length <= 0) {
//            count = new int[1];
//        }
        if (this.leftMajiangs.size() > 0 /*&& count[0] < this.leftMajiangs.size()*/) {
            Mj majiang = this.leftMajiangs.remove(0);

            //??????????????????2???
//            if (player != null
//                    &&((getKingCard() != null && majiang != null && majiang.getVal() == getKingCard().getVal() && player.getKingCardNumHuFlag() > 1)
////                            || (getFloorCard() != null && majiang != null && majiang.getVal() == getFloorCard().getVal() && player.getFloorCardNumHuFlag() > 1)
//                    )
//            ) {
//                leftMajiangs.add(majiang);
//                count[0] += 1;
//                return getLeftMajiang(player, count);
//            }

            if (player != null && isKingCard(majiang)) {
                player.setKingCardNumHuFlag(player.getKingCardNumHuFlag() + 1);
            } else if (player != null && getFloorCard() != null && getFloorCard().getVal() == getFloorCard().getVal()) {
                player.setFloorCardNumHuFlag(player.getFloorCardNumHuFlag() + 1);
            }
            dbParamMap.put("leftPais", JSON_TAG);
            return majiang;
        }

//        if (leftMajiangs.size() - getFirstFloorIndex() == 0) {
//            setKingAndFloorMj();
//        }

        LogUtil.msgLog.info("{},TjmjLeftGeNull:{},???:{},???:{}", this.getId(), this.leftMajiangs, player.getFloorCardNumHuFlag(), player.getKingCardNumHuFlag());
        return null;
    }


    /**
     * ?????????????????????
     *
     * @return
     */
    public int getLeftMajiangCount() {
        return Math.max(this.leftMajiangs.size() - gameModel.getSpecialPlay().getStayLastCard(), 0);
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
        //??????
        res.addExt(floorCard == null ? 0 : floorCard.getId());
        //??????
        res.addExt(kingCard == null ? 0 : kingCard.getId());
        //20
        res.addExt(getAndCalcFirstFloorIndex());
		res.addExt(kingCard2 == null ? 0 : kingCard2.getId());

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
        for (TjMjPlayer player : playerMap.values()) {
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
        seatMap.values().forEach(p -> {
            if (!getGameModel().signTingAllOver()) {
                systemAutoSignTingInfo(p);
            }
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

    public boolean addGangActionSeat(int majiang, int seat, List<Integer> actionList) {
        Map<Integer, List<Integer>> actionMap;
        //????????????,??????????????????
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
        return true;
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
        // ????????????????????????
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
     * ????????????
     *
     * @param player
     * @param majiangs
     * @param disMajiang
     * @return
     */
    private boolean canChi(TjMjPlayer player, List<Mj> handMajiang, List<Mj> majiangs, Mj disMajiang) {
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return false;
        }

        if (maxPlayerCount == 2 && gameModel.getSpecialPlay().isBuChi()) {
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

        List<Mj> chi = MjTool.checkChi(majiangs, disMajiang, player.getPlayingTable(TjMjTable.class));
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
    private boolean canPeng(TjMjPlayer player, List<Mj> majiangs, int sameCount, Mj disMajiang) {
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
     * ???????????????
     *
     * @param player
     * @param majiangs
     * @param sameCount
     * @return
     */
    private boolean canAnGang(TjMjPlayer player, List<Mj> majiangs, int sameCount, int action) {
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
     * ??????????????? ????????????????????????????????????????????????????????????
     */
    private boolean checkAction(TjMjPlayer player, List<Mj> cardList, List<Integer> hucards, int action) {
        boolean canAction = checkCanAction(player, action);// ????????????????????? ???????????????
        if (canAction == false) {// ??????????????? ??????????????????
            int seat = player.getSeat();
            tempActionMap.put(seat, new MjTempAction(seat, action, cardList, hucards));
            // ?????????????????????????????????????????? ?????????????????????
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
                TjMjPlayer tempPlayer = seatMap.get(maxSeat);
                List<Mj> tempCardList = tempActionMap.get(maxSeat).getCardList();
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
    private void refreshTempAction(TjMjPlayer player) {
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
                TjMjPlayer tempPlayer = seatMap.get(tempAction.getSeat());
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
    public boolean checkCanAction(TjMjPlayer player, int action) {
        // ???????????????????????????
        List<Integer> stopActionList = MjDisAction.findPriorityAction(action);
        //????????????????????????????????? com.sy599.game.qipai.tjmj.constant.MjAction
        //     * ??????0;???1;??????2;??????3;???4;??????5;?????????6;?????????7;????????? 8;?????????9;?????????10;????????????11;?????????12;??????13;????????????14;???????????????15;????????????16;??????17;
        for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
            if (player.getSeat() != entry.getKey()) {
                // ??????
                boolean can = MjDisAction.canDisMajiang(stopActionList, entry.getValue());
                if (!can) {
                    return false;
                }
                List<Integer> disActionList = MjDisAction.parseToDisActionList(entry.getValue());
                if (disActionList.contains(action)) {
                    // ??????????????????????????? ????????????????????????
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
     * ???????????????
     *
     * @param player
     * @param majiangs
     * @param sameCount
     * @return
     */
    private boolean canMingGang(TjMjPlayer player, List<Mj> handMajiang, List<Mj> majiangs, int sameCount,
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
     * ?????????????????????
     *
     * @param moGang       ?????????
     * @param moGangHuList ????????????????????????list
     */
    public void setMoGang(Mj moGang, List<Integer> moGangHuList) {
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

    public void setGangDisMajiangs(List<Mj> gangDisMajiangs) {
        this.gangDisMajiangs = gangDisMajiangs;
        changeExtend();
    }

    public List<Mj> getGangDisMajiangs() {
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
        return gameModel.getSpecialPlay().isRobGangHu();
    }

    public boolean canAnGangHu(){
        return gameModel.getSpecialPlay().isRobGangHu();
    }

    /**
     * @param
     * @return
     * @description ????????????????????????
     * @author Guang.OuYang
     * @date 2019/9/16
     */
    private int calcFinalRatio(TjMjPlayer v) {
        //??????????????????
        if (v.getTotalPoint() < 0) {
            return 0;
        }

        int ratio = gameModel.doubleChipEffect(v.getTotalPoint());
        //??????X???????????????
        int doubleChip = (ratio * v.getTotalPoint()) - v.getTotalPoint();
        LogUtil.printDebug(v.getUserId() + "???????????????:??????:{},winLoss:{},totalPoint:{}->point:{}", ratio, v.getWinLossPoint(), v.getTotalPoint(), v.getPoint());

        int addScore = gameModel.lowScoreEffect(doubleChip + v.getTotalPoint());
        //??????X???????????????
        int addScoreChip = (addScore + v.getTotalPoint()) - v.getTotalPoint();
        LogUtil.printDebug(v.getUserId() + "????????????????????????+?????????:??????:{},winLoss:{},totalPoint:{}->point:{}", addScore, v.getWinLossPoint(), v.getTotalPoint(), v.getPoint());

        //???????????????????????????
        return doubleChip + addScoreChip;
    }

    public ClosingMjInfoRes.Builder sendAccountsMsg(boolean over, boolean selfMo, List<Integer> winList,
                                                    Integer[] prickBirdMajiangIds, Integer[] seatBirds, Map<Integer, Integer> seatBirdMap, boolean isBreak,
                                                    int bankerSeat, int catchBirdSeat) {
//        for (int i = 0 ; i < seatMap.values().size();i++){
//            System.out.println("??????:::" + seatMap.get(i + 1).getSeat()+":"+seatMap.get(i + 1).getName());
//        }
//        for (int i = 0; i < prickBirdMajiangIds.length; i++) {
//            System.out.println("???:" + Mj.getMajang(prickBirdMajiangIds[i]) + " _??????:" + (i < seatBirds.length ? seatBirds[i] : "nil") + " ??????:" + seatMap.get(seatBirds[i]).getName());
//        }

        int totalAddPoint = 0;

        List<Integer> bigWinList = new ArrayList<>();
        //?????????
        //?????????,?????????,????????????????????????
        Iterator<TjMjPlayer> iterator = seatMap.values().iterator();
        while (iterator.hasNext()) {
            TjMjPlayer p = iterator.next();
            if (p != null) {
                //?????????
                p.setWinLossPoint(p.getTotalPoint());
                if (p.getTotalPoint() > 0) {
                    if (over) {
                        //????????????????????????
                        int addScore = calcFinalRatio(p);
                        //??????X???????????????
                        totalAddPoint += addScore;
                        LogUtil.printDebug("??????:{}", addScore);
                        p.setTotalPoint(p.getTotalPoint() + addScore);
                    }

                    if (!bigWinList.contains(p.getSeat()))
                        bigWinList.add(p.getSeat());
                }
            }
        }

        //??????????????????????????????????????????????????????
        int avg = totalAddPoint == 0 ? 0 : bigWinList.size() < seatMap.size() ? totalAddPoint / (seatMap.size() - bigWinList.size()) : totalAddPoint;

        //??????winLossPoint?????????????????????,????????????????????????
        seatMap.values().stream().filter(v -> !bigWinList.contains(v.getSeat())).forEach(v -> {
            v.setTotalPoint(v.getTotalPoint() - avg);
        });

        List<ClosingMjPlayerInfoRes> list = new ArrayList<>();
        List<ClosingMjPlayerInfoRes.Builder> builderList = new ArrayList<>();
        int fangPaoSeat = selfMo ? 0 : disCardSeat;

        if (winList == null || winList.size() == 0) {
            fangPaoSeat = 0;
        }

        for (TjMjPlayer player : seatMap.values()) {
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
            for (TjMjPlayer player : seatMap.values()) {
                if (player.getWinLoseCredit() > dyjCredit) {
                    dyjCredit = player.getWinLoseCredit();
                }
            }
            for (ClosingMjPlayerInfoRes.Builder builder : builderList) {
                TjMjPlayer player = seatMap.get(builder.getSeat());
                calcCommissionCredit(player, dyjCredit);
                builder.setWinLoseCredit(player.getWinLoseCredit());
                builder.setCommissionCredit(player.getCommissionCredit());
            }
        } else if (isGroupTableGoldRoom()) {
            // -----------??????????????????---------------------------------
            for (TjMjPlayer player : seatMap.values()) {
                player.setWinGold(player.getTotalPoint() * gtgDifen);
            }
            calcGroupTableGoldRoomWinLimit();
            for (ClosingMjPlayerInfoRes.Builder builder : builderList) {
                TjMjPlayer player = seatMap.get(builder.getSeat());
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
            res.addAllBirdSeat(Arrays.asList(seatBirds));
        }
        if (prickBirdMajiangIds != null) {
            res.addAllBird(Arrays.asList(prickBirdMajiangIds));
        }
        res.setCatchBirdSeat(catchBirdSeat);
        res.addAllLeftCards(MjHelper.toMajiangIds(leftMajiangs));
        for (TjMjPlayer player : seatMap.values()) {
            player.writeSocket(res.build());
        }
        return res;

    }

    /**
     * ?????????????????????
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
        ext.add(String.valueOf(kingCard2 == null ? 0 : kingCard2.getId())); // 27

        return ext;
    }

    @Override
    public void sendAccountsMsg() {
        ClosingMjInfoRes.Builder builder = sendAccountsMsg(true, false, null, null, null, null, true, 0, 0);
        saveLog(true, 0l, builder.build());
    }

    public Class<? extends Player> getPlayerClass() {
        return TjMjPlayer.class;
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

//        if(!getGameModel().signTingAllOver()){
//            return;
//        }

//        checkDeal();

        //??????????????????????????????+??????????????????
        if(state == table_state.play && player.getHandPais() != null && player.getHandPais().size() > 0) {
//            sendDealInfo((TjMjPlayer) player);
            sendTingInfo((TjMjPlayer)player);
        }


        checkSendGangRes(player);
        if (askLastMajaingSeat != 0) {
            sendAskLastMajiangRes(player.getSeat());
        }

        if (actionSeatMap.isEmpty()) {
            // ????????????????????????????????????
            if (player instanceof TjMjPlayer) {
                TjMjPlayer csMjPlayer = (TjMjPlayer) player;
                if (csMjPlayer != null) {
                    if (csMjPlayer.noNeedMoCard()) {
                        if (!csMjPlayer.getGang().isEmpty()) {
                            List<Mj> disMajiangs = new ArrayList<>();
                            disMajiangs.add(csMjPlayer.getLastMoMajiang());
                            disCard(csMjPlayer, disMajiangs, 0);
                        }
                    }
                }
            }
        }

        if (/*isBegin() && */!checkSignTingInfo() && seatMap.containsKey(lastWinSeat)/*&& player.getSeat() == lastWinSeat && actionSeatMap.isEmpty()*/) {
            // ???????????????????????????????????????????????? ??????????????????????????????
            TjMjPlayer bankPlayer = seatMap.get(lastWinSeat);
            ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.req_com_kt_ask_dismajiang);// ??????????????????
            bankPlayer.writeSocket(com.build());
        }

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
        // ???????????????????????????
        if (getSendDissTime() > 0) {
            for (TjMjPlayer player : seatMap.values()) {
                if (player.getLastCheckTime() > 0) {
                    player.setLastCheckTime(player.getLastCheckTime() + 1 * 1000);
                }
            }
            return;
        }

        // //???????????????????????????
        // if(!getActionSeatMap().isEmpty()){
        // return ;
        // }
        for (TjMjPlayer player : seatMap.values()) {

            if ((!player.getGang().isEmpty() || player.isSignTing()) && player.noNeedMoCard() && getMoMajiangSeat() == player.getSeat()) {
                // ??????????????????????????????
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
                disMjiang.add(player.getHandMajiang().get(player.getHandMajiang().size() - 1));
                disCard(player, disMjiang, MjDisAction.action_chupai);
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
				TjMjPlayer player = seatMap.get(seat);
				player.setAutoPlay(false, false);
				player.setCheckAutoPlay(false);
			}
			return;
		}

//        if (isBegin() && actionSeatMap.isEmpty()) {
//            // ???????????????????????????????????????????????? ??????????????????????????????
//            TjMjPlayer bankPlayer = seatMap.get(lastWinSeat);
//            ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.req_com_kt_ask_dismajiang);// ??????????????????
//            bankPlayer.writeSocket(com.build());
//        }

        if (!gameModel.signTingAllOver()) {
            for (TjMjPlayer player : seatMap.values()) {
                if (player.getLastCheckTime() > 0) {
                    player.setLastCheckTime(player.getLastCheckTime() + 1 * 1000);
                    continue;
                }
                player.checkAutoPlay(2, false);
                if (!player.isAutoPlay()) {
                    continue;
                }
                systemAutoSignTingInfo(player);
            }
        }

        if (gameModel.getSpecialPlay().getTableStatus() == MjConstants.TABLE_STATUS_PIAO) {
            for (int seat : seatMap.keySet()) {
                TjMjPlayer player = seatMap.get(seat);
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
                TjMjPlayer player = seatMap.get(seat);
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
            // ????????????????????????xx???????????????????????????
            for (TjMjPlayer player : seatMap.values()) {
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
     *@description ????????????????????????, ??????2?????????
     *@param
     *@return
     *@author Guang.OuYang
     *@date 2019/10/26
     */
    private void systemAutoSignTingInfo(TjMjPlayer player) {
        try {
            AbsCodeCommandExecutor.getGlobalActionCodeInstance(AbsCodeCommandExecutor.GlobalCommonIndex.COMMAND_INDEX, WebSocketMsgType.req_code_tjmj_baoting)
                    .orElse(AbsCodeCommandExecutor.getGlobalActionCodeInstance(AbsCodeCommandExecutor.GlobalCommonIndex.COMMAND_INDEX, -1).get())
                    .execute0(player, null, null, ComMsg.ComReq.newBuilder().setCode(WebSocketMsgType.req_code_tjmj_baoting).addParams(2).build());
        } catch (Exception e) {
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
                    TjMjPlayer player = seatMap.get(seat);
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
                    TjMjPlayer player = seatMap.get(seat);
                    if (!player.checkAutoPlay(0, false)) {
                        continue;
                    }
                    boolean chuPai = false;
                    if (player.noNeedMoCard()) {
                        chuPai = true;
                    }
                    if (action == MjDisAction.action_peng) {
                        if (player.isAutoPlaySelf()) {
                            // ???????????????????????????
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
            TjMjPlayer player = seatMap.get(nowDisCardSeat);
            if (player == null || !player.checkAutoPlay(0, false)) {
                return;
            }
            autoChuPai(player);
        }
    }

    public void autoChuPai(TjMjPlayer player) {

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

        while (mjId == -1 && index >= 0) {
            mjId = handMjIds.get(index);
            // mj = TjMj.getMajang(mjId);

        }
        if (mjId != -1) {
            List<Mj> mjList = MjHelper.toMajiang(Arrays.asList(mjId));
            playCommand(player, mjList, MjDisAction.action_chupai);
        }
    }

    public void autoPiao(TjMjPlayer player) {
        int piaoPoint = 0;
        if (gameModel.getSpecialPlay().getTableStatus() != MjConstants.TABLE_STATUS_PIAO) {
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
        for (TjMjPlayer player : seatMap.values()) {
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

        //????????????
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
        nowGangSeat = extend.getInt(53, 0);

        int tmpFloorCardId = extend.getInt(54, 0);
        if (tmpFloorCardId > 0)
            tmpFloorCard = Mj.getMajang(tmpFloorCardId);
        int tmpKingCardId = extend.getInt(55, 0);
        if (tmpKingCardId > 0)
            tmpKingCard = Mj.getMajang(tmpKingCardId);

        fromSeat = extend.getInt(56, 0);

        String showMjListsTmp = extend.getString(57);
        if (!StringUtils.isBlank(showMjListsTmp)) {
            this.showMjLists = StringUtil.explodeToIntList(showMjListsTmp);
        }

		int kingCardId2 = extend.getInt(58, 0);
		if (kingCardId2 > 0)
			kingCard2 = Mj.getMajang(kingCardId2);
		curGangSeat = extend.getInt(59, 0);
    }

    @Override
    public JsonWrapper buildExtend0(JsonWrapper wrapper) {
        // 1-4 ??????????????????
        for (TjMjPlayer player : seatMap.values()) {
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
        wrapper.putInt(49, nowMoCard == null?0:nowMoCard.getId());
        wrapper.putInt(50, floorCard == null?0:floorCard.getId());
        wrapper.putInt(51, kingCard == null?0:kingCard.getId());
        wrapper.putInt(52, lastWinSeat);
        wrapper.putInt(53, nowGangSeat);
        wrapper.putInt(54, tmpFloorCard == null?0:tmpFloorCard.getId());
        wrapper.putInt(55, tmpKingCard == null?0:tmpKingCard.getId());
        wrapper.putInt(56, fromSeat);
        wrapper.putString(57, StringUtil.implode(showMjLists, ","));
		wrapper.putInt(58, kingCard2 == null?0:kingCard2.getId());
		wrapper.putInt(59, curGangSeat);
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
                //???????????? 2??????????????? 3???????????????
                .calcBird(StringUtil.getIntValue(params, 3, 0))
                //?????????
                .birdNum(StringUtil.getIntValue(params, 4, 0))
                //??????
                .signTing(StringUtil.getIntValue(params, 5, 0) > 0)
                //7??????
                .sevenPairs(true/*StringUtil.getIntValue(params, 6, 0) > 0*/)
                //??????7??????
                .superSevenPairs(StringUtil.getIntValue(params, 6, 0) > 0)
                //?????????7??????
                .specialSuperSevenPairs(StringUtil.getIntValue(params, 8, 0) > 0)
                //????????????7??????
                .specialSSuperSevenPairs(StringUtil.getIntValue(params, 6, 0) > 0)
                //?????????,??????8???
                .stayLastCard(8)
                //?????????
                .selfMoHu(StringUtil.getIntValue(params, 9, 0) > 0)
                //?????????
                .robGangHu(true)
                .robAnGangHu(true)
                //??????????????????????????????>=??????,???????????????????????????
                .robAnGangHuTingCardSizeMust(9 * 3)
                //??????????????????
                .skyHuRobGangHu(StringUtil.getIntValue(params, 11, 0) > 0)
                //????????????
//                .maxInMaxOut(StringUtil.getIntValue(params, 12, 0) > 0)
                .maxInMaxOut(true)
                //?????????,?????????
//                .gangUpFlower(StringUtil.getIntValue(params, 13, 0) > 0)
//                .gangUpGun(StringUtil.getIntValue(params, 13, 0) > 0)//
                .gangUpFlower(true)
                .gangUpGun(true)
                //??????
                .buChi(StringUtil.getIntValue(params, 15, 0) == 0)
                //??????3???
                .gangMoNum(StringUtil.getIntValue(params, 16, 0) > 0 ? 3 : 0)
                //????????????
                .birdOption(StringUtil.getIntValue(params, 17, 0) > 0 ? 2 : 0)
                //?????????
                .blackSkyHu(true)
                //?????????
                .allOfTheSameColor(true)
                //?????????
                .ppHu(true)
                //?????????
                .jjHu(true)
                .floorHuNum(3)
				.kingHuNum(3)
                .repeatedEffect(true)
                .onlyDaHu(true)
                .canBuCard(false)
                .passHuLimit(true)
                .openingHu(true)
                .build();

        //??????3??????2,??????2??????3
        specialPlay.setPaoHuGangHu(StringUtil.getIntValue(params, 10, 0));


        if (specialPlay.getCalcBird() != 2 && specialPlay.getCalcBird() != 3) {
            specialPlay.setCalcBird(2);
        }

        gameModel = GameModel.builder().specialPlay(specialPlay)
//                .calcBanker(StringUtil.getIntValue(params, 18, 0) > 0)
                //??????????????????
                .basicRatio(Math.max(1, StringUtil.getIntValue(params, 18, 0)))
                //?????????
                .gameFinishRound(bureauCount)
                //?????????0??????1???
                .doubleChip(StringUtil.getIntValue(params, 19, 0))
                //?????????
                .doubleChipLeChip(StringUtil.getIntValue(params, 20, 0))
                //?????????
                .doubleRatio(StringUtil.getIntValue(params, 21, 0))
                //????????????
                .topFen(StringUtil.getIntValue(params, 31, 0))
                //????????????
                .payType(StringUtil.getIntValue(params, 2, 1))
                //????????????
                .gameMaxHuman(StringUtil.getIntValue(params, 7, 4))
                //?????????????????????
                .lowScoreLimit(Math.abs(StringUtil.getIntValue(params, 22, 0)))
                //???????????????????????????
                .lowScoreAdd(Math.abs(StringUtil.getIntValue(params, 23, 0)))
                //??????????????????
                .discardHoleCards(StringUtil.getIntValue(params, 24, 0))
				//??????8?????????
				.eightKing(StringUtil.getIntValue(params, 25, 0) > 0)
                .build();

        maxPlayerCount = gameModel.getGameMaxHuman();
        totalBureau = gameModel.getGameFinishRound();

        super.setPayType(gameModel.getPayType());

        //????????????????????????
		if (gameModel.isEightKing()) {
			specialPlay.setKingHuNum(4);
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

        if (maxPlayerCount != 2) {
            gameModel.setDoubleChip(0);
        }

        gameModel.setTopFen(gameModel.getTopFen() == 1 ? 32 : gameModel.getTopFen() == 2 ? 64 : Integer.MAX_VALUE);

        if (maxPlayerCount != 3) {
            // ??????????????? 1??????????????? 2:????????????
//            specialPlay.setBirdOption(0);
        } else if (maxPlayerCount != 2) {
//            // ???????????????1????????????
//            specialPlay.setBuChi(false);
//            // ???????????????????????????
//            specialPlay.setOnlyDaHu(false);
//            // ???????????????????????????
//            specialPlay.setXiaohuZiMo(false);
//            // ????????????????????????
//            specialPlay.setQueYiMen(false);
        }

        if (maxPlayerCount > 2 || (gameModel.getDiscardHoleCards() > 0 && gameModel.getDiscardHoleCards() % 13 != 0)) {
            //?????????2??????????????????
            gameModel.setDiscardHoleCards(0);
        }


        isAutoPlay = StringUtil.getIntValue(params, 28, 0);
        this.autoPlayGlob = StringUtil.getIntValue(params, 29, 0);
        playedBureau = 0;
        changeExtend();
        // getRoomModeMap().put("1", "1"); //?????????????????????
        LogUtil.msgLog.info("createTable tid:" + getId() + " " + player.getName() + " params" + params.toString());
    }


    /**
     * @param
     * @return
     * @description ????????????
     * @author Guang.OuYang
     * @date 2019/10/16
     */
    public void sendTingInfo(TjMjPlayer player) {
        if (player.noNeedMoCard()) {        //????????????
            DaPaiTingPaiRes.Builder tingInfo = DaPaiTingPaiRes.newBuilder();
            List<Mj> cards = new ArrayList<>(player.getHandMajiang());

            for (Mj card : player.getHandMajiang()) {
                cards.remove(card);
                List<Mj> huCards = MjTool.getTingMjs(cards, player.getGang(), player.getPeng(), player.getChi(),
                        player.getBuzhang(), true, gameModel.getSpecialPlay().isOnlyDaHu(), gameModel.getSpecialPlay().isQuanQiuRenJiang() ? 1 : 0, this, player, false);
                cards.add(card);

                LogUtil.printDebug("??????{}...??????:{}->{}", player.getName(), card, huCards);

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
        } else {//????????????
            List<Mj> cards = new ArrayList<>(player.getHandMajiang());
            List<Mj> huCards = MjTool.getTingMjs(cards, player.getGang(), player.getPeng(), player.getChi(),
                    player.getBuzhang(), true, gameModel.getSpecialPlay().isOnlyDaHu(), gameModel.getSpecialPlay().isQuanQiuRenJiang() ? 1 : 0, this, player, false);

            LogUtil.printDebug("??????{}...??????:{}", player.getName(), huCards);

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
     *@description ??????????????????
     *@param
     *@return
     *@author Guang.OuYang
     *@date 2019/10/23
     */
    public boolean checkSignTingInfo() {
        boolean flag = false;
        if (gameModel.getSpecialPlay().isSignTing() && !gameModel.signTingAllOver()) {
            for (Iterator<TjMjPlayer> iterator = seatMap.values().iterator(); iterator.hasNext(); ) {
                TjMjPlayer player = iterator.next();
                List<Mj> cards = new ArrayList<>(player.getHandMajiang());
                List<Mj> huCards = MjTool.getTingMjs(cards, player.getGang(), player.getPeng(), player.getChi(),
                        player.getBuzhang(), true, gameModel.getSpecialPlay().isOnlyDaHu(), gameModel.getSpecialPlay().isQuanQiuRenJiang() ? 1 : 0, this, player, false);

                if (player.getSignTing() == 0 && (huCards == null || huCards.size() == 0)) {
                    player.setSignTing(3);
                    player.changeExtend();
                    continue;
                }

                if (player.getSignTing() == 0 || player.getSignTing() == 1) {
                    //??????????????????
                    player.writeSocket(ComMsg.ComRes.newBuilder().setCode(WebSocketMsgType.req_code_tjmj_baoting).addParams(1).build());
                }

                if (player.getSignTing() == 0) {
                    player.setSignTing(1);
                    player.changeExtend();
                    gameModel.incSignTingFlag();
                    changeExtend();
                    LogUtil.printDebug("??????????????????...??????:{}", huCards);
                }

                if(player.getSignTing() == 1)
                    flag = true;
            }

            if (!flag) {
                gameModel.setSignTing(-1);
                changeExtend();
                LogUtil.printDebug("??????????????????...??????...");
            }
        }

        return flag;
    }

    public static void loadWanfaTables(Class<? extends BaseTable> cls) {
        for (Integer integer : wanfaList) {
            TableManager.wanfaTableTypesPut(integer, cls);
        }
    }

    /**
     * ?????????????????????
     *
     * @param actionIndex CSMajiangConstants?????????
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
        sb.append("TjMj");
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

    public void logFaPaiPlayer(TjMjPlayer player, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("TjMj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append("faPai");
        sb.append("|").append(player.getHandMajiang());
        sb.append("|").append(actListToString(actList));
        LogUtil.msg(sb.toString());
    }

    public void logMoMj(TjMjPlayer player, Mj mj, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("TjMj");
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

    public void logChuPaiActList(TjMjPlayer player, Mj mj, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("TjMj");
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

    public void logAction(TjMjPlayer player, int action, int xiaoHuType, List<Mj> mjs, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("TjMj");
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
     * ???????????????????????????????????????
     *
     * @param player
     */
    public void processHideMj(TjMjPlayer player) {
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

    public boolean isPlayerAllReady(){
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
            if (gameModel.getSpecialPlay().getKePiao() == 1) {
                boolean bReturn = true;
                // ?????????????????????
                if (this.isTest()) {
                    for (TjMjPlayer robotPlayer : seatMap.values()) {
                        if (robotPlayer.isRobot()) {
                            robotPlayer.setPiaoPoint(1);
                        }
                    }
                }
                for (TjMjPlayer player : seatMap.values()) {
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

                for (TjMjPlayer player : seatMap.values()) {
                    player.setPiaoPoint(point);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * @param gangDice ????????????????????????
     * @return
     * @description ????????????
     * @author Guang.OuYang
     * @date 2019/10/16
     */
    public void addMoTailPai(int gangDice) {
        int leftMjCount = leftMajiangs.size();
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
        List<Mj> moList = getGangDisMajiangs();
        if (moList != null && moList.size() > 0 && actionSeatMap.isEmpty()) {
            TjMjPlayer player = seatMap.get(getMoMajiangSeat());
            for (TjMjPlayer seatPlayer : seatMap.values()) {
                GangMoMajiangRes.Builder gangbuilder = GangMoMajiangRes.newBuilder();
                gangbuilder.setRemain(leftMajiangs.size());
                gangbuilder.setGangId(gangMajiang.getId());
                gangbuilder.setUserId(player.getUserId() + "");
                gangbuilder.setName(player.getName() + "");
                gangbuilder.setSeat(player.getSeat());
                gangbuilder.setReconnect(0);
                gangbuilder.setDice(0);
                gangbuilder.setNextDisCardIndex(getAndCalcFirstFloorIndex());
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
     * @param state  1????????????????????????0?????????????????????????????????
     */
    public void sendMoLast(TjMjPlayer player, int state) {
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
            if (MjAction.getFirstXiaoHu(actList) != -1) {
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
