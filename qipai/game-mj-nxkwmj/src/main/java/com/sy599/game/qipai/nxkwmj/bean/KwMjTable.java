package com.sy599.game.qipai.nxkwmj.bean;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.sy599.game.msg.serverPacket.PlayCardResMsg.*;
import com.sy599.game.qipai.nxkwmj.rule.KwMingTang;
import com.sy599.game.qipai.nxkwmj.tool.*;
import com.sy599.game.qipai.nxkwmj.tool.huTool.HuTool;
import com.sy599.game.qipai.nxkwmj.tool.huTool.TingTool;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
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
import com.sy599.game.qipai.nxkwmj.constant.KwMjConstants;
import com.sy599.game.qipai.nxkwmj.constant.KwMj;
import com.sy599.game.qipai.nxkwmj.rule.MjRobotAI;
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


public class KwMjTable extends BaseTable {
    /**
	 * ??????????????????
	 */
    private List<KwMj> nowDisCardIds = new ArrayList<>();
    private Map<Integer, List<Integer>> actionSeatMap = new ConcurrentHashMap<>();
    /**
	 * ?????????????????????????????? ??????????????????????????????????????? 1??????????????????????????????????????? ??????????????????????????? ??????????????????
	 * 2???????????????????????????????????????????????????????????????????????????????????????????????? ?????????????????????????????????????????? ?????????????????????????????????????????????
	 */
    private Map<Integer, MjTempAction> tempActionMap = new ConcurrentHashMap<>();
    private int maxPlayerCount = 4;
    private List<KwMj> leftMajiangs = new ArrayList<>();
	/*** ??????map */
    private Map<Long, KwMjPlayer> playerMap = new ConcurrentHashMap<Long, KwMjPlayer>();
	/*** ????????????????????? */
    private Map<Integer, KwMjPlayer> seatMap = new ConcurrentHashMap<Integer, KwMjPlayer>();
	private List<Integer> huConfirmList = new ArrayList<>();// ????????????
    /**
	 * ????????????seat
	 */
    private int moMajiangSeat;
    /**
	 * ????????????
	 **/
    private int dealDice;
    /**
	 * 0????????? 1?????????
	 **/
    private int canDianPao;
	private int isAutoPlay = 0;// ????????????????????????
    private int readyTime = 0 ;
	// ???????????????0??????1???
    private int jiaBei=0;
	// ?????????????????????xx???????????????
    private int jiaBeiFen=0;
	// ????????????????????????
    private int jiaBeiShu=0;

	// ??????????????????????????????
    private int bankerRand=0;
	// ???????????????
    private int finishFapai=0;
	/** ??????1????????????2????????? */
    private int autoPlayGlob;
	private int autoTableCount;
	/*** ????????????????????? */
    private List<Integer> moTailPai = new ArrayList<>();
    //??????below??????
    private int belowAdd=0;
    private int below=0;

    List<Integer> paoHuSeat=new ArrayList<>();
    //??????????????????
    private int lastId=0;
    //???????????????
    private int fangGangSeat=0;
    //??????
    private int zhengWang=0;
    //??????
    private List<Integer> chunWang=new ArrayList<>();
    //????????? ?????????????????????
    private int disNum=0;
    //????????????
    private int ceiling=0;
    //???????????? 0 ???????????? 1?????????
    private int kaiWang=0;
    //?????????
    private int catchBirdNum=0;
    //???????????? 0?????? 1??????
    private int birdType=0;
    //???????????? ????????????????????????
    private int onlyZimo=0;
    //?????? 1??????0??????
    private int piaoFenType=0;
    //??????
    private int zuoYa=0;
    //????????????
    private List<Integer> buIds=new ArrayList<>();
    //??????????????????
    private int gangSeat=0;
    //???????????????????????????
    private int kingId=0;
    //???????????????????????????
    private int askSeat=0;
    //??????????????????????????????
    private int hdFirstSeat=0;
    //2???????????????
    private int twoNiaoType=0;
    //????????????????????????????????????????????????
    private int sendJzkgSeat=0;
    //????????????
    private int choupai=0;
    //1????????????2?????????
    private int disOrMo=0;
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


		// 0?????????1??????Id
		payType = StringUtil.getIntValue(params, 2, 1);// ????????????
		maxPlayerCount = StringUtil.getIntValue(params, 7, 4);// ??????

        ceiling = StringUtil.getIntValue(params, 3, 0);// ??????
        kaiWang = StringUtil.getIntValue(params, 4, 0);// ??????
        catchBirdNum = StringUtil.getIntValue(params, 5, 0);// ?????????
        birdType = StringUtil.getIntValue(params, 6, 0);// ???????????? 0?????? 1??????
        onlyZimo = StringUtil.getIntValue(params, 8, 0);// ????????????
        piaoFenType = StringUtil.getIntValue(params, 9, 0);// ??????
        zuoYa = StringUtil.getIntValue(params, 10, 0);// ??????
        isAutoPlay = StringUtil.getIntValue(params, 11, 0);// ??????
        if(isAutoPlay==1) {
            // ??????1??????
            isAutoPlay=60;
        }
        autoPlayGlob = StringUtil.getIntValue(params, 12, 0);// ????????????
        if(maxPlayerCount==2){
            jiaBei = StringUtil.getIntValue(params, 13, 0);
            jiaBeiFen = StringUtil.getIntValue(params, 14, 100);
            jiaBeiShu = StringUtil.getIntValue(params, 15, 1);
            int belowAdd = StringUtil.getIntValue(params, 16, 0);
            if(belowAdd<=100&&belowAdd>=0)
                this.belowAdd=belowAdd;
            int below = StringUtil.getIntValue(params, 17, 0);
            if(below<=100&&below>=0){
                this.below=below;
            }
            twoNiaoType = StringUtil.getIntValue(params, 18, 0);//0????????????1:13579??????
        }
        choupai = StringUtil.getIntValue(params, 19, 0);//0????????????1:13579??????
        changeExtend();
        if (!isJoinPlayerAllotSeat()) {
			// getRoomModeMap().put("1", "1"); //?????????????????????
        }
    }

    @Override
    public JsonWrapper buildExtend0(JsonWrapper wrapper) {
        for (KwMjPlayer player : seatMap.values()) {
            wrapper.putString(player.getSeat(), player.toExtendStr());
        }
        wrapper.putInt(4, disOrMo);
        wrapper.putString(5, StringUtil.implode(huConfirmList, ","));
        wrapper.putInt(6, moMajiangSeat);
        wrapper.putString(7,StringUtil.implode(chunWang, ","));
        wrapper.putInt(9, canDianPao);
        wrapper.putInt(10,zhengWang);
        JSONArray tempJsonArray = new JSONArray();
        for (int seat : tempActionMap.keySet()) {
            tempJsonArray.add(tempActionMap.get(seat).buildData());
        }
        wrapper.putString("tempActions", tempJsonArray.toString());
        wrapper.putInt(11, maxPlayerCount);
        wrapper.putInt(12, isAutoPlay);
        wrapper.putInt(13,disNum);
        wrapper.putString(15, StringUtil.implode(moTailPai, ","));
        wrapper.putInt(17, jiaBei);
        wrapper.putInt(18, jiaBeiFen);
        wrapper.putInt(19, jiaBeiShu);
        wrapper.putInt(20, bankerRand);
        wrapper.putString(21,StringUtil.implode(paoHuSeat, ","));
        wrapper.putInt(22, autoPlayGlob);
        wrapper.putInt(23, finishFapai);
        wrapper.putInt(24, belowAdd);
        wrapper.putInt(25, below);
        wrapper.putInt(27, lastId);
        wrapper.putInt(28, fangGangSeat);
        wrapper.putInt(29, ceiling);
        wrapper.putInt(30, kaiWang);
        wrapper.putInt(31, catchBirdNum);
        wrapper.putInt(32, birdType);
        wrapper.putInt(33, onlyZimo);
        wrapper.putInt(34, piaoFenType);
        wrapper.putInt(35, zuoYa);
        wrapper.putInt(36, kingId);
        wrapper.putInt(37, gangSeat);
        wrapper.putString(38,StringUtil.implode(buIds, ","));
        wrapper.putInt(39, askSeat);
        wrapper.putInt(40, twoNiaoType);
        wrapper.putInt(41, sendJzkgSeat);
        wrapper.putInt(42, hdFirstSeat);
        wrapper.putInt(43, choupai);
        return wrapper;
    }

    @Override
    public void initExtend0(JsonWrapper wrapper) {
        for (KwMjPlayer player : seatMap.values()) {
            player.initExtend(wrapper.getString(player.getSeat()));
        }
        disOrMo = wrapper.getInt(4,0);
        String huListstr = wrapper.getString(5);
        if (!StringUtils.isBlank(huListstr)) {
            huConfirmList = StringUtil.explodeToIntList(huListstr);
        }
        moMajiangSeat = wrapper.getInt(6, 0);
        String chunWang = wrapper.getString(7);
        if (!StringUtils.isBlank(chunWang)) {
            this.chunWang = StringUtil.explodeToIntList(chunWang);
        }
        canDianPao = wrapper.getInt(9, 1);
        zhengWang = wrapper.getInt(10,0);
        tempActionMap = loadTempActionMap(wrapper.getString("tempActions"));
        maxPlayerCount = wrapper.getInt(11, 4);
        isAutoPlay = wrapper.getInt(12, 0);
        disNum = wrapper.getInt(13, 0);

        if(isAutoPlay ==1) {
            isAutoPlay=60;
        }
        String s = wrapper.getString(15);
        if (!StringUtils.isBlank(s)) {
            moTailPai = StringUtil.explodeToIntList(s);
        }
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
        lastId= wrapper.getInt(27,0);
        fangGangSeat= wrapper.getInt(28,0);
        ceiling= wrapper.getInt(29,0);
        kaiWang= wrapper.getInt(30,0);
        catchBirdNum= wrapper.getInt(31,0);
        birdType= wrapper.getInt(32,0);
        onlyZimo= wrapper.getInt(33,0);
        piaoFenType= wrapper.getInt(34,0);
        zuoYa= wrapper.getInt(35,0);
        kingId= wrapper.getInt(36,0);
        gangSeat= wrapper.getInt(37,0);
        s = wrapper.getString(38);
        if (!StringUtils.isBlank(s)) {
            buIds = StringUtil.explodeToIntList(s);
        }
        askSeat= wrapper.getInt(39,0);
        twoNiaoType= wrapper.getInt(40,0);
        sendJzkgSeat= wrapper.getInt(41,0);
        hdFirstSeat= wrapper.getInt(42,0);
        choupai= wrapper.getInt(43,0);
    }



    public int getDealDice() {
        return dealDice;
    }

    public int getGangSeat() {
        return gangSeat;
    }

    public List<Integer> getBuIds() {
        return buIds;
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

    public int getCanDianPao() {
        return canDianPao;
    }

    public void setCanDianPao(int canDianPao) {
        this.canDianPao = canDianPao;
    }

    public int getDisNum() {
        return disNum;
    }

    public void setDisNum(int disNum) {
        this.disNum = disNum;
    }

    public List<Integer> getChunWang() {
        return chunWang;
    }

    public int getCeiling() {
        return ceiling;
    }

    public void setChunWang(List<Integer> chunWang) {
        this.chunWang = chunWang;
    }

    public int getZhengWang() {
        return zhengWang;
    }

    public void setZhengWang(int zhengWang) {
        this.zhengWang = zhengWang;
    }

    public int getAskSeat() {
        return askSeat;
    }

    public void setLastId(int lastId) {
        this.lastId = lastId;
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
        List<Integer> birdIds = new ArrayList<>();// ??????ID
        int [] birdSeat=null;
        if (winList.size() == 0) {

        } else {
            //??????????????????2????????????????????????34????????????
            if(leftMajiangs.size()==0){
                birdIds.add(lastId);
            } else {
                if(leftMajiangs.size()>=catchBirdNum){
                    birdIds=MjHelper.toMajiangIds(leftMajiangs.subList(0,catchBirdNum));
                }else {
                    birdIds=MjHelper.toMajiangIds(leftMajiangs);
                }
            }
            birdSeat=new int[birdIds.size()];
            if(askSeat==0){
                if(gangSeat==winList.get(0)||disNum==0){
                    selfMo = true;
                }else if (seatMap.get(winList.get(0)).getHandMajiang().size() % 3 == 2 && winList.get(0) == moMajiangSeat) {
                    selfMo = true;
                }
            }else {
                if(askSeat==winList.get(0))
                    selfMo = true;
            }
            int[] bird= getBirdNum(birdIds,birdSeat,getMaxPlayerCount(),winList.get(0));
            //?????????????????????
            if (selfMo) {
				// ??????
                KwMjPlayer winner = seatMap.get(winList.get(0));
                int winFen=0;
                int mtFen = KwMingTang.getMingTangFen(winner.getIdMingTang(), ceiling == 0);
                for (int seat : seatMap.keySet()) {
                    if (!winList.contains(seat)) {
                        int birdNum = countBirdNum(bird, winner.getSeat(), seat);
                        int loseFen=mtFen;
                        if(birdType==1)
                            loseFen*=(birdNum+1);
                        else
                            loseFen+=birdNum;
                        KwMjPlayer player = seatMap.get(seat);
                        int extraFen = countTwoExtraPoint(winner, player);
                        player.changePoint(-loseFen-extraFen);
                        winFen=winFen+loseFen+extraFen;
                    }
                }
                winner.changePoint(winFen);
                winner.addZiMoNum(1);
            } else {
                KwMjPlayer losePlayer;
                if(askSeat!=0)
                    losePlayer=seatMap.get(askSeat);
                else if(gangSeat!=0){
                    losePlayer=seatMap.get(gangSeat);
                }else{
                    losePlayer= seatMap.get(disCardSeat);
                }
                losePlayer.addDianPaoNum(1);
                int loseFen=0;
                for (int winnerSeat : winList) {
                    KwMjPlayer winPlayer = seatMap.get(winnerSeat);
                    int winFen = KwMingTang.getMingTangFen( winPlayer.getIdMingTang(),ceiling==0);
                    int birdNum=countBirdNum(bird,winnerSeat, losePlayer.getSeat());
                    if(birdType==1)
                        winFen*=(birdNum+1);
                    else
                        winFen+=birdNum;
                    int extraFen = countTwoExtraPoint(winPlayer, losePlayer);
                    winPlayer.changePoint(winFen+extraFen);
                    loseFen=loseFen-winFen-extraFen;
                    winPlayer.addJiePaoNum(1);
                }
                losePlayer.changePoint(loseFen);
            }
        }


        boolean over = playBureau == totalBureau;
        if(autoPlayGlob >0) {
			// //????????????
            boolean diss = false;
            if(autoPlayGlob ==1) {
            	 for (KwMjPlayer seat : seatMap.values()) {
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

        ClosingMjInfoRes.Builder res = sendAccountsMsg(over, selfMo, winList, birdIds, birdSeat, null, 0, false);
        if (!winList.isEmpty()) {
            if (winList.size() > 1) {
				// ???????????????????????????????????????
                setLastWinSeat(disCardSeat);
            } else {
                setLastWinSeat(winList.get(0));
            }
		} else if (leftMajiangs.isEmpty()) {// ??????
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
        for (KwMjPlayer player : seatMap.values()) {
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
		for (KwMjPlayer seat : seatMap.values()) {
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

    private int[] getBirdNum(List<Integer> birdIds, int[] birdSeat, int playerCount, int catchSeat){
        int[] bird=new int[4];
        if(playerCount==3){
            for (int i = 0; i < birdIds.size(); i++) {
                int yu=((KwMj.getMajang(birdIds.get(i)).getVal()%10)-1)%4;
                bird[yu]++;
                if(yu<3){
                    int bingoSeat = calcSeat(catchSeat, yu, playerCount);
                    birdSeat[i]=bingoSeat;
                }else {
                    birdSeat[i]=0;
                }
            }
        }else {
            for (int i = 0; i < birdIds.size(); i++) {
                int yu=((KwMj.getMajang(birdIds.get(i)).getVal()%10)-1)%playerCount;
                bird[yu]++;
                int bingoSeat = calcSeat(catchSeat, yu, playerCount);
                birdSeat[i]=bingoSeat;
            }
        }

        return bird;
    }

    private int countBirdNum(int[] bird,int startSeat,int countSeat){
        seatMap.get(startSeat).setValidBirdNum(bird[0]);
        if(getMaxPlayerCount()==2&&twoNiaoType==1){
            return bird[0];
        }else {
            int index=0;
            int seat=startSeat;
            while (seat!=countSeat){
                index++;
                seat = calcNextSeat(seat);
            }
            seatMap.get(countSeat).setValidBirdNum(bird[index]);
            return bird[0]+bird[index];
        }
    }

    private int countTwoExtraPoint(KwMjPlayer win,KwMjPlayer lose){
        int extra=0;
        //??????
        extra=zuoYa*2;
        //??????
        if(piaoFenType>0)
            extra=extra+win.getPiaoFen()+lose.getPiaoFen();
        return extra;
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
        for (KwMjPlayer player : playerMap.values()) {
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
        dealDice = (r.nextInt(6) + 1);
        addPlayLog(disCardRound + "_" + lastWinSeat + "_" + MjDisAction.action_dice + "_" + dealDice);
        setDealDice(dealDice);
        KwMj king = confirmKing(dealDice);
        // ??????????????????
        logFaPaiTable(king);
        for (KwMjPlayer tablePlayer : seatMap.values()) {
            DealInfoRes.Builder res = DealInfoRes.newBuilder();
            List<Integer> actionList=null;
            if (lastWinSeat == tablePlayer.getSeat()) {
                actionList = tablePlayer.checkMo();
            }
            actionList=tablePlayer.checkQiShouHu(actionList);
            if(tablePlayer.getSeat()!=lastWinSeat){
                actionList=tablePlayer.checkBaoTing(actionList,getChunWang(),getZhengWang());
            }
            if (actionList.contains(1)) {
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
            res.setLaiZiVal(king.getId());
            tablePlayer.writeSocket(res.build());
            if (tablePlayer.isAutoPlay()) {
                tablePlayer.setAutoPlayTime(0);
            }
            sendTingInfo(tablePlayer);
            logFaPaiPlayer(tablePlayer, null);
            if(tablePlayer.isAutoPlay()) {
            	addPlayLog(getDisCardRound() + "_" +tablePlayer.getSeat() + "_" + KwMjConstants.action_tuoguan + "_" +1+ tablePlayer.getExtraPlayLog());
            }
        }
        sendFirstAct(seatMap.get(lastWinSeat));
        if (playBureau == 1) {
            setCreateTime(new Date());
        }
    }

    private KwMj confirmKing(int dealDice){
        KwMj king ;
        int ver=leftMajiangs.size() - dealDice * 2 - 1;
        if(zp!=null&&zp.get(0).size()==1){
            //?????????????????????
            king = KwMj.getMajiangByVal(zp.get(0).get(0));
            //???????????????????????????????????????
            leftMajiangs.remove(king);
        }else {
            king = leftMajiangs.get(ver);
        }
        if(kaiWang==0){
            zhengWang=king.getVal();
            int yu=zhengWang%10;
            int chu=zhengWang/10;
            if(yu==9){
                chunWang.add(zhengWang-1);
                chunWang.add(chu*10+1);
            }else if(yu==1){
                chunWang.add(zhengWang+1);
                chunWang.add(chu*10+9);
            }else {
                chunWang.add(zhengWang+1);
                chunWang.add(zhengWang-1);
            }
        }else {
            chunWang.add(king.getVal());
        }
        this.kingId=king.getId();
        return king;
    }

    public void moMajiang(KwMjPlayer player) {
        if (state != table_state.play) {
            return;
        }
		// ??????
        KwMj majiang = null;
        if (disCardRound != 0) {
			// ????????????????????????????????????????????????
            if (player.isAlreadyMoMajiang()) {
                return;
            }
            if (getLeftMajiangCount()==0) {
                calcOver();
                return;
            }
            if (GameServerConfig.isDebug() && zp != null && zp.size()==1) {
                majiang= KwMj.getMajiangByValue(zp.get(0).get(0));
                zp.clear();
            }else {
                majiang = getLeftMajiang();
            }
        }
        boolean haiDi=leftMajiangs.size()==0;
        if (majiang != null&&!haiDi) {
            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + MjDisAction.action_moMjiang + "_" + majiang.getId() + player.getExtraPlayLog());
            player.moMajiang(majiang);
        }
		// ????????????
        clearActionSeatMap();
        if (disCardRound == 0) {
            return;
        }
        setMoMajiangSeat(player.getSeat());
        player.setPassHu(0);
        if(player.getFengDong()==1&&!haiDi){
            logMoMj(player, majiang);
            MoMajiangRes.Builder res = MoMajiangRes.newBuilder();
            res.setUserId(player.getUserId() + "");
            res.setSeat(player.getSeat());
            res.setRemain(getLeftMajiangCount());
            for (KwMjPlayer seat : seatMap.values()) {
                if(seat.getSeat()==player.getSeat())
                    res.setMajiangId(majiang.getId());
                seat.writeSocket(res.build());
            }
            if(getLeftMajiangCount()==getMaxPlayerCount()){
                calcOver();
                return;
            }
            fengDongChuPai(player,majiang,MjDisAction.action_chupai);
        }else{
            lastId=majiang.getId();
            //????????????????????????????????????????????????
            if(haiDi){
                hdFirstSeat=askSeat=player.getSeat();
                askHaiDi(askSeat);
            }else if(player.getMoDa()==1){
                boolean isHu=HuTool.isHu(player.getHandPais(),getChunWang(),getZhengWang(),player.getCardTypes(),false,true);
                if(isHu){
                    List<Integer> acts = getActs(1);
                    sendMoRes(player,majiang,acts);
                    addActionSeat(player.getSeat(),acts);
                    hu(player,new ArrayList<>(),MjDisAction.action_hu);
                }else {
                    List<KwMj> dis=new ArrayList<>();
                    dis.add(majiang);
                    sendMoRes(player,majiang,getActs());
                    chuPai(player,dis,MjDisAction.action_chupai);
                }
            }else {
                List<Integer> arr = player.checkMo();
                if (!arr.isEmpty()) {
                    addActionSeat(player.getSeat(), arr);
                }
                logMoMj(player, majiang, arr);
                sendMoRes(player,majiang,arr);
                sendTingInfo(player);
            }
        }
    }

    public void sendMoRes(KwMjPlayer player,KwMj mj,List<Integer> acts){
        MoMajiangRes.Builder res = MoMajiangRes.newBuilder();
        res.setUserId(player.getUserId() + "");
        res.setSeat(player.getSeat());
        res.setRemain(getLeftMajiangCount());
        res.setNextDisCardIndex(nowDisCardSeat);
        for (KwMjPlayer seat : seatMap.values()) {
            if (seat.getUserId() == player.getUserId()) {
                MoMajiangRes.Builder copy = res.clone();
                copy.addAllSelfAct(acts);
                if (mj != null) {
                    copy.setMajiangId(mj.getId());
                }
                seat.writeSocket(copy.build());
            } else {
                seat.writeSocket(res.build());
            }
        }
    }


    /**
     *
     * @param act ??????????????????{@link MjDisAction}??????????????????
     * @return
     */
    public List<Integer> getActs(int...act){
        List<Integer> acts=new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            if(i>act.length-1){
                acts.add(0);
            }else {
                acts.add(act[i]);
            }
        }
        return acts;
    }

    public void askHaiDi(int askSeat){
        if(askSeat==0)
            return;
        KwMjPlayer player = seatMap.get(askSeat);
        if(player.getFengDong()==1||player.isAutoPlay()){
            this.askSeat=askSeat=calcNextSeat(askSeat);
            if(askSeat!=hdFirstSeat)
                askHaiDi(askSeat);
            else {
                calcOver();
            }
        }else {
            player.writeComMessage(WebSocketMsgType.res_code_asklastmajiang,1);
        }
    }

    public void answerHaiDi(KwMjPlayer player,int answer){
        if(player.getSeat()!=askSeat)
            return;
        if(answer==MjDisAction.action_passmo){
            KwMjPlayer passPlayer = seatMap.get(askSeat);
            passPlayer.writeComMessage(WebSocketMsgType.res_code_asklastmajiang,0);
            askSeat=calcNextSeat(askSeat);
            if(askSeat!=moMajiangSeat){
                askHaiDi(askSeat);
            }else {
                calcOver();
            }
        }else {
            KwMj haiDi=KwMj.getMajang(lastId);
            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + MjDisAction.action_moMjiang + "_" + haiDi.getId() + player.getExtraPlayLog());
            player.moMajiang(haiDi);

            boolean isHu=HuTool.isHu(player.getHandPais(),getChunWang(),getZhengWang(),player.getCardTypes(),false,true);
            List<Integer> acts = getActs(isHu?1:0);
            addActionSeat(player.getSeat(), acts);
            logMoMj(player, KwMj.getMajang(lastId), acts);
            sendMoRes(player,haiDi,acts);
            if(!isHu){
                List<KwMj> mjs=new ArrayList<>();
                mjs.add(haiDi);
                chuPai(player,mjs,MjDisAction.action_chupai);
            }
        }
    }


    /**
	 * ???????????????
	 *
	 * @param player
	 * @param majiangs
	 */
    private void hu(KwMjPlayer player, List<KwMj> majiangs, int action) {
        if (state != table_state.play) {
            return;
        }
        List<Integer> actionList = actionSeatMap.get(player.getSeat());
		if (actionList == null || actionList.get(KwMjConstants.ACTION_INDEX_HU) != 1) {// ?????????????????????????????????????????????????????????
            return;
        }
        if (huConfirmList.contains(player.getSeat())) {
            return;
        }
        if(!canHu(player))
            return;
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        List<KwMj> huHand = new ArrayList<>(player.getHandMajiang());
        boolean zimo = player.isAlreadyMoMajiang();
        if(buIds.size()!=0){
            for(Integer buId:buIds){
                List<Integer> copy=MjHelper.toMajiangIds(huHand);
                copy.add(buId);
                List<Integer> huType = HuTool.getHuType(copy, getChunWang(), player, this,zimo);
                if (huType.size()!=0) {
                    player.addMingTang(buId,huType);
                }
            }
            if(gangSeat==player.getSeat())
                zimo=true;
        }else {
            KwMj huCard=null;
            if(lastId==0){
                //?????????????????????
            }else {
                huCard= KwMj.getMajang(lastId);
                if(huCard!=null&&!player.getHandMajiang().contains(huCard))
                    player.getHandMajiang().add(huCard);
            }
            List<Integer> huType = HuTool.getHuType(player.getHandPais(), getChunWang(), player, this,zimo);
            if (huType.size()==0) {
                return;
            }
            if(lastId!=0)
                player.addMingTang(lastId,huType);
            else//??????????????????
                player.addMingTang(player.getHandPais().get(player.getHandMajiang().size()-1),huType);
        }
        KwMingTang.addOddHuType(this,player,zimo);
        KwMingTang.removeLess(player.getIdMingTang(),this);
        if(buIds.size()!=0){
            for (Map.Entry<Integer,List<Integer>> entry:player.getIdMingTang().entrySet()) {
                huHand.add(KwMj.getMajang(entry.getKey()));
            }
        }

        if (!zimo) {
            builder.setFromSeat(disCardSeat);
        } else {
            builder.addHuArray(KwMjConstants.HU_ZIMO);
        }
        buildPlayRes(builder, player, action, huHand);
        if (zimo) {
            builder.setZimo(1);
        }



        if (!huConfirmList.isEmpty()) {
            builder.addExt(StringUtil.implode(huConfirmList, ","));
        }
		// ???
        for (KwMjPlayer seat : seatMap.values()) {
			// ????????????
            seat.writeSocket(builder.build());
        }
        for (Player roomPlayer : roomPlayerMap.values()) {
            PlayMajiangRes.Builder copy = builder.clone();
            roomPlayer.writeSocket(copy.build());
        }
		// ??????????????????
        addHuList(player.getSeat());
        changeDisCardRound(1);
        List<KwMj> huPai = new ArrayList<>();
        huPai.add(huHand.get(huHand.size() - 1));
//        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + MjHelper.toMajiangStrs(huPai) + "_" + StringUtil.implode(player.getHuType(), ",") + player.getExtraPlayLog());
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + MjHelper.toMajiangStrs(huPai) + "_"+ player.getExtraPlayLog());
        logActionHu(player, majiangs, "");
        calcOver();

    }

    private void buildPlayRes(PlayMajiangRes.Builder builder, Player player, int action, List<KwMj> majiangs) {
        KwMjResTool.buildPlayRes(builder, player, action, majiangs);
    }


    private boolean isCalcOver() {
        List<Integer> huActionList = getHuSeatByActionMap();
        boolean over = false;
        if (!huActionList.isEmpty()) {
            over = true;
            for (int huseat : huActionList) {
                if (!huConfirmList.contains(huseat) &&
                        !(tempActionMap.containsKey(huseat) && tempActionMap.get(huseat).getAction() == MjDisAction.action_hu)) {
                    over = false;
                    break;
                }
            }
        }

        if (!over) {
            KwMjPlayer disMajiangPlayer = seatMap.get(disCardSeat);
            for (int huseat : huActionList) {
                if (huConfirmList.contains(huseat)) {
                    continue;
                }
                PlayMajiangRes.Builder disBuilder = PlayMajiangRes.newBuilder();
                KwMjPlayer seatPlayer = seatMap.get(huseat);
                buildPlayRes(disBuilder, disMajiangPlayer, 0, null);
                List<Integer> actionList = actionSeatMap.get(huseat);
                disBuilder.addAllSelfAct(actionList);
                seatPlayer.writeSocket(disBuilder.build());
            }
        }

        return over;
    }


    public boolean canHu(KwMjPlayer player){
        List<Integer> huActionList = getHuSeatByActionMap();
        if (!huActionList.isEmpty()) {
            int seat=nowDisCardSeat;
            do {
                if(actionSeatMap.containsKey(seat)&&actionSeatMap.get(seat).get(0)==1){
                    if(seat==player.getSeat())
                        return true;
                    else
                        return false;
                }else {
                    seat=calcNextSeat(seat);
                }
            } while(nowDisCardSeat!=seat);
        }
        return false;
    }

    /**
	 * ??????
	 *
	 * @param player
	 * @param majiangs
	 * @param action
	 */
    private void chiPengGang(KwMjPlayer player, List<KwMj> majiangs, int action) {
        if (state != table_state.play) {
            return;
        }
        logAction(player, action, majiangs, null);
        if (majiangs == null || majiangs.isEmpty()) {
            return;
        }
        for (KwMj mj:majiangs) {
            if(getChunWang().contains(mj.getVal()))
                return;
        }
        if (!checkAction(player, majiangs, new ArrayList<>(), action)) {
			LogUtil.msg("???????????????????????????????????????");
            player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip);
            return;
        }
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        KwMj disMajiang = null;
        if (nowDisCardIds.size() > 1) {
			// ????????????????????????
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
            sameCount = MjHelper.getMajiangCount(majiangs, majiangs.get(0).getVal());
        }
		// ???????????? ????????????????????????????????????
        if (action == MjDisAction.action_minggang) {
            majiangs = MjHelper.getMajiangList(player.getHandMajiang(), majiangs.get(0).getVal());
            sameCount = majiangs.size();
            if (sameCount >= 4) {
				// ???4????????????????????????
                action = MjDisAction.action_angang;
                majiangs=majiangs.subList(0,4);
            }
			// ???????????????
        }
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return;
        }
        if (action == MjDisAction.action_peng) {
            boolean can = canPeng(player, majiangs, sameCount);
            if (!can) {
                return;
            }
        }else if (action == MjDisAction.action_chi) {
            boolean can = canChi(player, majiangs, disMajiang);
            if (!can) {
                return;
            }
        }else if (action == MjDisAction.action_angang) {
            boolean can = canAnGang(player, majiangs, sameCount);

            if (!can) {
                return;
            }
            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + MjHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
        } else if (action == MjDisAction.action_minggang) {
            boolean can = canMingGang(player, majiangs, sameCount);
            if (!can) {
                return;
            }
            ArrayList<KwMj> mjs = new ArrayList<>(majiangs);
            if (sameCount == 3) {
                mjs.add(disMajiang);
            }
            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + MjHelper.toMajiangStrs(mjs) + player.getExtraPlayLog());
        } else {
            return;
        }
        if (disMajiang != null) {
            if ((action == MjDisAction.action_minggang && sameCount == 3) || action == MjDisAction.action_peng || action == MjDisAction.action_chi) {
                if (action == MjDisAction.action_chi) {
					majiangs.add(1, disMajiang);// ?????????????????????
                } else {
                    majiangs.add(disMajiang);
                }
                builder.setFromSeat(disCardSeat);
                seatMap.get(disCardSeat).removeOutPais(nowDisCardIds);
            }
        }
        switch (action){
            case MjDisAction.action_minggang:
            case MjDisAction.action_angang:
                gang(builder,player,majiangs,action,sameCount);
                break;
            default:
                chiPeng(builder, player, majiangs, action);
                break;
        }
    }

    private void chiPeng(PlayMajiangRes.Builder builder, KwMjPlayer player, List<KwMj> majiangs, int action) {
        player.addOutPais(majiangs, action, disCardSeat);
        buildPlayRes(builder, player, action, majiangs);
        List<Integer> actList = actionSeatMap.get(player.getSeat());
        clearActionSeatMap();
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + MjHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
        setNowDisCardSeat(player.getSeat());
        int[] arr = player.checkGang();
        List<Integer> list = new ArrayList<>();
        for (int val : arr) {
            list.add(val);
        }
        if(list.contains(1))
            addActionSeat(player.getSeat(),list);
        for (KwMjPlayer seatPlayer : seatMap.values()) {
			// ????????????
            PlayMajiangRes.Builder copy = builder.clone();
            if (actionSeatMap.containsKey(seatPlayer.getSeat())) {
                copy.addAllSelfAct(actionSeatMap.get(seatPlayer.getSeat()));
            }
            seatPlayer.writeSocket(copy.build());
        }
        sendTingInfo(player);
        logAction(player, action, majiangs, actList);

    }

    private void gang(PlayMajiangRes.Builder builder, KwMjPlayer player, List<KwMj> majiangs, int action, int disSameCount) {
        List<Integer> actionList = actionSeatMap.get(player.getSeat());
        //???????????????????????????????????????
        List<Integer> copyI = player.removeGangId(player.getHandMajiang(), majiangs.get(0).getVal());
        if(TingTool.getTing(copyI,getChunWang(),getZhengWang(),player.getCardTypes(),false).size()==0){
            player.writeErrMsg("??????????????????");
            return;
        }
        if (action == MjDisAction.action_peng && actionList.get(KwMjConstants.ACTION_INDEX_MINGGANG) == 1) {
            // ?????????????????????
            player.addPassGangVal(majiangs.get(0).getVal());
        }

        player.addOutPais(majiangs, action, disCardSeat);
        buildPlayRes(builder, player, action, majiangs);
        List<Integer> actList = removeActionSeat(player.getSeat());
        setNowDisCardSeat(player.getSeat());
        gangSeat=player.getSeat();
        player.setMoDa(1);
        //???????????????????????????????????????
        boolean hasQGangHu=false;
        if (disSameCount == 1 && canGangHu()) {
            for(Integer seat:seatMap.keySet()){
                if(seat==player.getSeat())
                    continue;
                KwMjPlayer p = seatMap.get(seat);
                List<Integer> copyIds = new ArrayList<>(p.getHandPais());
                copyIds.add(majiangs.get(0).getId());
                if(HuTool.isHu(copyIds,getChunWang(),getZhengWang(),p.getCardTypes(),false,false)){
                    hasQGangHu=true;
                    List<Integer> acts=new ArrayList<>();
                    acts.add(1);
                    for (int i = 1; i < 6; i++) {
                        acts.add(0);
                    }
                    addActionSeat(p.getSeat(),acts);
                }
            }
            if(hasQGangHu){
                removeActionSeat(player.getSeat());
                LogUtil.msg("tid:" + getId() + " " + player.getName() + "????????????????????????");
            }
        }
        //???????????????
        for (KwMjPlayer seatPlayer : seatMap.values()) {
            // ????????????
            PlayMajiangRes.Builder copy = builder.clone();
            if(actionSeatMap.containsKey(seatPlayer.getSeat()))
                copy.addAllSelfAct(actionSeatMap.get(seatPlayer.getSeat()));
            seatPlayer.writeSocket(copy.build());
        }
        sendTingInfo(player);
        if(!hasQGangHu)
            buPai(player,majiangs.get(majiangs.size()-1).getId());
        robotDealAction();
        logAction(player, action, majiangs, actList);
        player.setGangWang(0);
        if(actionSeatMap.isEmpty()){
            setNowDisCardSeat(calcNextSeat(player.getSeat()));
            checkMo();
        }
    }

    private void buPai(KwMjPlayer player,int gangId){
        List<Integer> buIds = MjHelper.toMajiangIds(leftMajiangs.subList(0, 2));
        setLeftMajiangs(leftMajiangs.subList(2,leftMajiangs.size()));
        this.buIds=buIds;
        this.gangSeat=player.getSeat();
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + MjDisAction.action_moMjiang + "_" + StringUtil.implode(buIds, ",") + player.getExtraPlayLog());
        GangMoMajiangRes.Builder builder = GangMoMajiangRes.newBuilder();
        builder.setUserId(player.getUserId()+"");
        builder.setSeat(player.getSeat());
        builder.setName(player.getName());
        builder.setGangId(gangId);
        builder.setRemain(getLeftMajiangCount());
        for (KwMjPlayer p : seatMap.values()) {
            GangMoMajiangRes.Builder clone = builder.clone();
            for (Integer id:buIds){
                GangPlayMajiangRes.Builder res=GangPlayMajiangRes.newBuilder();
                res.setMajiangId(id);
                List<Integer> handPais = new ArrayList<>(p.getHandPais());

                handPais.add(id);
                boolean selfMo=player.getSeat()==p.getSeat();
                if((onlyZimo==1&&selfMo)||onlyZimo==0){
                    if(p.getPassHu()==0&&HuTool.isHu(handPais,getChunWang(),getZhengWang(),p.getCardTypes(),false,selfMo)){
                        List<Integer> acts = getActs(1);
                        res.addAllSelfAct(acts);
                        addActionSeat(p.getSeat(),acts);
                    }
                }
                clone.addGangActs(res);
            }
            p.writeSocket(clone.build());
        }
        if(actionSeatMap.isEmpty()){
            this.buIds.clear();
            gangSeat=0;
        }

    }



    /**
	 * ????????????
	 *
	 * @param player
	 * @param majiangs
	 * @param action
	 */
    private void chuPai(KwMjPlayer player, List<KwMj> majiangs, int action) {
        if (state != table_state.play) {
            player.writeComMessage(WebSocketMsgType.res_code_mj_dis_err, action, majiangs.get(0).getId());
            return;
        }
        if (majiangs.size() != 1) {
            player.writeComMessage(WebSocketMsgType.res_code_mj_dis_err, action, majiangs.get(0).getId());
            return;
        }
        if (!tempActionMap.isEmpty()) {
			LogUtil.e(player.getName() + "???????????????????????????");
            clearTempAction();
        }
        if (!player.isAlreadyMoMajiang()||player.getFengDong()==1) {
			// ???????????????
            player.writeComMessage(WebSocketMsgType.res_code_mj_dis_err, action, majiangs.get(0).getId());
            player.writeErrMsg("????????????????????????");
            return;
        }
        if (chunWang.contains(majiangs.get(0).getVal())) {
            // ?????????????????????
            player.writeComMessage(WebSocketMsgType.res_code_mj_dis_err, action, majiangs.get(0).getId());
            player.writeErrMsg("???????????????");
            return;
        }
        if(!actionSeatMap.isEmpty()){
            if(actionSeatMap.containsKey(player.getSeat()))
                removeActionSeat(player.getSeat());
            else {
                player.writeComMessage(WebSocketMsgType.res_code_mj_dis_err, action, majiangs.get(0).getId());
                return;
            }
        }
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        buildPlayRes(builder, player, action, majiangs);
		// ????????????
        clearActionSeatMap();
        setNowDisCardSeat(calcNextSeat(player.getSeat()));
        if(majiangs.size()==1)
            lastId=majiangs.get(0).getId();
        recordDisMajiang(majiangs, player);
        player.addOutPais(majiangs, action, player.getSeat());
        disNum++;
        logAction(player, action, majiangs, null);
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + MjHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
        for (KwMjPlayer p : seatMap.values()) {
            List<Integer> list;
            if (p.getUserId() != player.getUserId()&&p.getFengDong()!=1) {
                list = p.checkDisMajiang(majiangs.get(0), this.canDianPao(),
                        leftMajiangs.size()==0,p.getSeat()==calcNextSeat(player.getSeat()));
                if(list==null||list.isEmpty())
                    continue;
                if (list.contains(1)) {
                    addActionSeat(p.getSeat(), list);
                    p.setLastCheckTime(System.currentTimeMillis());
                    logChuPaiActList(p, majiangs.get(0), list);
                    if(list.get(KwMjConstants.ACTION_INDEX_MINGGANG)==1)
                        fangGangSeat=player.getSeat();
                }
            }
        }
        sendDisMajiangAction(builder);
        checkMo();
    }

    private void sendDisMajiangAction(PlayMajiangRes.Builder builder) {
        for (KwMjPlayer seatPlayer : seatMap.values()) {
            PlayMajiangRes.Builder copy = builder.clone();
            List<Integer> actionList;
            // ???????????????????????????????????????????????????????????????????????????????????????
            if (actionSeatMap.containsKey(seatPlayer.getSeat())) {
                actionList = actionSeatMap.get(seatPlayer.getSeat());
            } else {
                actionList = new ArrayList<>();
            }
            copy.addAllSelfAct(actionList);
            seatPlayer.writeSocket(copy.build());
        }
    }

    private void fengDongChuPai(KwMjPlayer player, KwMj mj,int action) {
        List<KwMj> mjs=new ArrayList<>();
        mjs.add(mj);

        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        builder.setAction(action);
        builder.setUserId(player.getUserId() + "");
        builder.setSeat(player.getSeat());
        // ????????????
        setNowDisCardSeat(calcNextSeat(player.getSeat()));
        player.addOutFengDong(mj);
        disNum++;
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" +mj.getId() + player.getExtraPlayLog());
        for (KwMjPlayer seatPlayer : seatMap.values()) {
            PlayMajiangRes.Builder copy = builder.clone();
            if(seatPlayer.getSeat()==player.getSeat()){
                copy.addMajiangIds(mj.getId());
            }else {
                copy.addMajiangIds(0);
            }
            seatPlayer.writeSocket(copy.build());
        }
        checkMo();
    }

    public void fengDong(KwMjPlayer player){
        player.setFengDong(1);
        changeDisCardRound(1);
        addPlayLog(player.getSeat(),MjDisAction.action_fengdong);

        if(actionSeatMap.containsKey(player.getSeat())){
            guo(player,new ArrayList<>(),MjDisAction.action_pass);
        }
        removeActionSeat(player.getSeat());
        boolean allFengDong=true;
        for (KwMjPlayer p:seatMap.values()) {
            if(p.getFengDong()==0)
                allFengDong=false;
        }
        if(allFengDong){
            calcOver();
            return;
        }

        if(player.isAlreadyMoMajiang()){
            fengDongChuPai(player,player.getLastMoMajiang(),MjDisAction.action_chupai);
        }
    }

    public List<Integer> getHuSeatByActionMap() {
        List<Integer> huList = new ArrayList<>();
        for (int seat : actionSeatMap.keySet()) {
            List<Integer> actionList = actionSeatMap.get(seat);
            if (actionList.get(KwMjConstants.ACTION_INDEX_HU) == 1) {
				// ???
                huList.add(seat);
            }
        }
        return huList;
    }



    public synchronized void playCommand(KwMjPlayer player, List<KwMj> majiangs, int action) {
        playCommand(player, majiangs, null, action);
    }

    /**
	 * ??????
	 *
	 * @param player
	 * @param majiangs
	 * @param action
	 */
    public synchronized void playCommand(KwMjPlayer player, List<KwMj> majiangs, List<Integer> hucards, int action) {
        if (state != table_state.play) {
            return;
        }
        if (MjDisAction.action_hu == action) {
            hu(player, majiangs, action);
            return;
        }
		// ???????????????????????????
        if (action != MjDisAction.action_minggang)
            if (!isHandCard(majiangs,player.getHandMajiang())) {
                return;
            }
        changeDisCardRound(1);
        if (action == MjDisAction.action_pass) {
            guo(player, majiangs, action);
        }else if(action==MjDisAction.action_baoting){
            baoTing(player,action);
        }else if(action != 0) {
            chiPengGang(player, majiangs, action);
        } else {
            chuPai(player, majiangs, action);
        }
		// ?????????????????????????????????
        setLastActionTime(TimeUtil.currentTimeMillis());
    }

    public void baoTing(KwMjPlayer player,int action){
        if(disNum!=0||player.getSeat()==lastWinSeat)
            return;
        removeActionSeat(player.getSeat());
        player.setBaoTing(1);
        logBaoTing(player);
        addPlayLog(player.getSeat(),action);
        for (KwMjPlayer p:seatMap.values()) {
            p.writeComMessage(WebSocketMsgType.res_code_nxkwmj_baoTing,player.getSeat());
        }
        sendFinshActs();
    }

    private void sendFinshActs(){
        if(disNum==0) {
            //???????????????????????????????????????????????????????????????
            boolean bankerFirst = true;
            for (Integer seat : actionSeatMap.keySet()) {
                if (seat != lastWinSeat) {
                    List<Integer> list = actionSeatMap.get(seat);
                    if (list.get(KwMjConstants.ACTION_INDEX_HU) == 1 || list.get(KwMjConstants.ACTION_INDEX_BAOTING) == 1)
                        bankerFirst = false;
                }
            }
            if (bankerFirst) {
                seatMap.get(lastWinSeat).writeComMessage(WebSocketMsgType.res_code_nxkwmj_finishAct);
            } else {
                seatMap.get(lastWinSeat).writeComMessage(WebSocketMsgType.res_code_nxkwmj_haveAct);
            }
        }
    }

    private boolean isHandCard(List<KwMj> majiangs, List<KwMj> handCards){
        for (KwMj mj:majiangs) {
            if(!handCards.contains(mj))
                return false;
        }
        return true;
    }

    /**
     * pass
     *
     * @param player
     * @param majiangs
     * @param action
     */
    private void guo(KwMjPlayer player, List<KwMj> majiangs, int action) {
        if (state != table_state.play) {
            return;
        }
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return;
        }
        if(actionSeatMap.get(player.getSeat()).get(KwMjConstants.ACTION_INDEX_HU)==1){
            if(player.getBaoTing()==1)
                player.setPassBaoTing(1);
            else{
                player.setPassHu(1);
                if(buIds.size()>0){
                    int maxFen=0;
                    for (Integer buId:buIds) {
                        ArrayList<Integer> copy = new ArrayList<>(player.getHandPais());
                        copy.add(buId);
                        int fen=player.checkHuFen(copy, disOrMo == 2);
                        if(fen>maxFen)
                            maxFen=fen;
                    }
                    player.setPassFen(maxFen);
                }else {
                    ArrayList<Integer> copy = new ArrayList<>(player.getHandPais());
                    if(disOrMo==1)
                        copy.add(nowDisCardIds.get(0).getId());
                    player.setPassFen(player.checkHuFen(copy, disOrMo == 2));
                }
            }
        }
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        buildPlayRes(builder, player, action, majiangs);
        builder.setSeat(nowDisCardSeat);
        List<Integer> removeActionList = removeActionSeat(player.getSeat());
        player.writeSocket(builder.build());
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + MjHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
        if (removeActionList.get(0) == 1 && disCardSeat != player.getSeat() && nowDisCardIds.size() == 1) {
			// ??????
            player.addPassMajiangVal(nowDisCardIds.get(0).getVal());
        }
        logAction(player, action, majiangs, removeActionList);
        if (player.isAlreadyMoMajiang()) {
            sendTingInfo(player);
        }
		refreshTempAction(player);// ?????? ???????????????????????????????????????????????????????????????
        if(actionSeatMap.isEmpty()){
            buIds.clear();
            gangSeat=0;
            checkMo();
        }
        if(player.getGangWang()==1)
            player.setGangWang(0);
        sendFinshActs();
    }

    private void recordDisMajiang(List<KwMj> majiangs, KwMjPlayer player) {
        setNowDisCardIds(majiangs);
        disOrMo=1;
        setDisCardSeat(player.getSeat());
    }


    public void setNowDisCardIds(List<KwMj> nowDisCardIds) {
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
                KwMjPlayer player = seatMap.get(nowDisCardSeat);
                //????????????????????????????????????
                if(canJzkg(player)){
                    player.writeComMessage(WebSocketMsgType.res_code_nxkwmj_jzkg);
                    sendJzkgSeat=player.getSeat();
                }else {
                    //???????????????
                    moMajiang(player);
                }
            }
            robotDealAction();

        } else {
            for (int seat : actionSeatMap.keySet()) {
                KwMjPlayer player = seatMap.get(seat);
                if (player != null && player.isRobot()) {
					// ????????????????????????????????????
                    List<Integer> actionList = actionSeatMap.get(seat);
                    if (actionList == null) {
                        continue;
                    }
                    List<KwMj> list = new ArrayList<>();
                    if (!nowDisCardIds.isEmpty()) {
                        list = MjQipaiTool.getVal(player.getHandMajiang(), nowDisCardIds.get(0).getVal());
                    }
                    if (actionList.get(KwMjConstants.ACTION_INDEX_HU) == 1) {
						// ???
                        playCommand(player, new ArrayList<KwMj>(), MjDisAction.action_hu);

                    } else if (actionList.get(KwMjConstants.ACTION_INDEX_ANGANG) == 1) {
                        playCommand(player, list, MjDisAction.action_angang);

                    } else if (actionList.get(KwMjConstants.ACTION_INDEX_MINGGANG) == 1) {
                        playCommand(player, list, MjDisAction.action_minggang);

                    } else if (actionList.get(KwMjConstants.ACTION_INDEX_PENG) == 1) {
                        playCommand(player, list, MjDisAction.action_peng);
                    }
                }

            }

        }
    }

    private boolean canJzkg(KwMjPlayer player){
        List<KwMj> peng = player.getPeng();
        if (peng==null||peng.size()==0)
            return false;
        boolean pengWang=false;
        for (KwMj mj:peng) {
            if(mj.getVal()==zhengWang){
                pengWang = true;
                break;
            }
        }
        if(pengWang){
            if(TingTool.getTing(player.getHandPais(),getChunWang(),getZhengWang(),player.getCardTypes(),false).size()>0)
                return true;
        }
        return false;
    }

    public void jzkGang(KwMjPlayer player,int isGang){
        sendJzkgSeat=0;
        if(isGang==1&&canJzkg(player)){
            PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
            List<KwMj> mjs=new ArrayList<>();
            mjs.add(KwMj.getMajang(kingId));
            player.addOutPais(mjs, MjDisAction.action_minggang, disCardSeat);
            buildPlayRes(builder, player, MjDisAction.action_minggang, mjs);
            setNowDisCardSeat(player.getSeat());
            gangSeat=player.getSeat();
            player.setMoDa(1);
            //???????????????
            for (KwMjPlayer seatPlayer : seatMap.values()) {
                // ????????????
                PlayMajiangRes.Builder copy = builder.clone();
                if(actionSeatMap.containsKey(seatPlayer.getSeat()))
                    copy.addAllSelfAct(actionSeatMap.get(seatPlayer.getSeat()));
                seatPlayer.writeSocket(copy.build());
            }

            buPai(player,mjs.get(mjs.size()-1).getId());
            logAction(player, MjDisAction.action_minggang, mjs,new ArrayList<>());
            player.setGangWang(0);
            if(actionSeatMap.isEmpty()){
                checkMo();
            }
        }else
            moMajiang(player);
    }

    @Override
    protected void robotDealAction() {
        if (true) {
            return;
        }
        if (isTest()) {
            int nextseat = getNextDisCardSeat();
            KwMjPlayer next = seatMap.get(nextseat);
            if (next != null && next.isRobot()) {
                List<Integer> actionList = actionSeatMap.get(next.getSeat());
                if (actionList != null) {
                    List<KwMj> list = null;
                    if (actionList.get(KwMjConstants.ACTION_INDEX_HU) == 1) {
						// ???
                        playCommand(next, new ArrayList<KwMj>(), MjDisAction.action_hu);
                    } else if (actionList.get(KwMjConstants.ACTION_INDEX_ANGANG) == 1) {
						// ???????????????
                        Map<Integer, Integer> handMap = MjHelper.toMajiangValMap(next.getHandMajiang());
                        for (Entry<Integer, Integer> entry : handMap.entrySet()) {
                            if (entry.getValue() == 4) {
								// ????????????
                                list = MjHelper.getMajiangList(next.getHandMajiang(), entry.getKey());
                            }
                        }
                        playCommand(next, list, MjDisAction.action_angang);

                    } else if (actionList.get(KwMjConstants.ACTION_INDEX_MINGGANG) == 1) {
                        Map<Integer, Integer> pengMap = MjHelper.toMajiangValMap(next.getPeng());
                        for (KwMj handMajiang : next.getHandMajiang()) {
                            if (pengMap.containsKey(handMajiang.getVal())) {
								// ?????????
                                list = new ArrayList<>();
                                list.add(handMajiang);
                                playCommand(next, list, MjDisAction.action_minggang);
                                break;
                            }
                        }

                    } else if (actionList.get(KwMjConstants.ACTION_INDEX_PENG) == 1) {
                        playCommand(next, list, MjDisAction.action_peng);
                    }
                } else {
                    List<Integer> handMajiangs = new ArrayList<>(next.getHandPais());
                    MjQipaiTool.dropHongzhongVal(handMajiangs);
                    int maJiangId = MjRobotAI.getInstance().outPaiHandle(0, handMajiangs, new ArrayList<Integer>());
                    List<KwMj> majiangList = MjHelper.toMajiang(Arrays.asList(maJiangId));
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
        List<Integer> copy = KwMjConstants.getMajiangList(3);
        addPlayLog(copy.size() + "");
        List<List<KwMj>> list = null;
        if (zp != null&&zp.size()!=0) {
            list = MjTool.fapai(copy, getMaxPlayerCount(), zp);
        } else {
            list = MjTool.fapai(copy, getMaxPlayerCount());
        }
        int i = 1;
        for (KwMjPlayer player : seatMap.values()) {
            player.changeState(player_state.play);
            if (player.getSeat() == lastWinSeat) {
                player.dealHandPais(list.get(0));
                continue;
            }
            player.dealHandPais(list.get(i));
            i++;
        }
		// ??????????????????
        List<KwMj> lefts = list.get(getMaxPlayerCount());
        if(getMaxPlayerCount()==2&&choupai==1){
            if(lefts.size()>40)
                lefts=lefts.subList(0,lefts.size()-40);
        }
        setLeftMajiangs(lefts);
        finishFapai=1;
    }

    @Override
    public void startNext() {
    }

    /**
	 * ???????????????????????????
	 *
	 * @param leftMajiangs
	 */
    public void setLeftMajiangs(List<KwMj> leftMajiangs) {
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
    public KwMj getLeftMajiang() {
        if (this.leftMajiangs.size() > 0) {
            KwMj majiang = this.leftMajiangs.remove(0);
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
//            if(actionSeatMap.isEmpty()||actionSeatMap.containsKey(lastWinSeat))
//                return lastWinSeat;
//            int seat = 0;
//            do {
//                seat = calcNextSeat(lastWinSeat);
//                if(actionSeatMap.containsKey(seat))
//                    return seat;
//            } while (seat==lastWinSeat);
            return lastWinSeat;
        } else {
            return nowDisCardSeat;
        }
    }


    public int calcSeat(int seat,int time,int maxCount) {
        int resultSeat=seat;
        for (int i = 0; i < time; i++) {
            resultSeat=resultSeat + 1 > maxCount? 1 : resultSeat + 1;
        }
        return resultSeat;
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
        res.addExt(canDianPao);             //1
        res.addExt(isAutoPlay);             //2
        res.addExt(0);                      //3
        res.addExt(kingId);                //4

        res.addStrExt(StringUtil.implode(moTailPai, ","));     //0
        res.setMasterId(getMasterId() + "");
        if (leftMajiangs != null) {
            res.setRemain(leftMajiangs.size());
        } else {
            res.setRemain(0);
        }
        res.setDealDice(dealDice);
        List<PlayerInTableRes> players = new ArrayList<>();
        for (KwMjPlayer player : playerMap.values()) {
            PlayerInTableRes.Builder playerRes = player.buildPlayInTableInfo(isrecover);
            if (player.getUserId() == userId) {
                playerRes.addAllHandCardIds(player.getHandPais());
            }
            if (player.getSeat() == disCardSeat && nowDisCardIds != null) {
                playerRes.addAllOutCardIds(MjHelper.toMajiangIds(nowDisCardIds));
            }
            playerRes.addRecover(player.getIsEntryTable());
            playerRes.addRecover(player.getSeat() == lastWinSeat ? 1 : 0);
            if (actionSeatMap.containsKey(player.getSeat())) {
				if (!tempActionMap.containsKey(player.getSeat()) && !huConfirmList.contains(player.getSeat())) {// ????????????????????????
																												// ?????????????????????????????????
																												// ????????????????????????
                    playerRes.addAllRecover(actionSeatMap.get(player.getSeat()));
                }
            }
            players.add(playerRes.build());
        }
        res.addAllPlayers(players);
        int nextSeat = getNextDisCardSeat();
        if (nextSeat != 0) {
            res.setNextSeat(nextSeat);
        }
        res.setRenshu(getMaxPlayerCount());
        res.setLastWinSeat(getLastWinSeat());
        res.addTimeOut((int) KwMjConstants.AUTO_TIMEOUT);
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
        lastId=0;
        fangGangSeat=0;
        disNum=0;
        buIds.clear();
        kingId=0;
        gangSeat=0;
        askSeat=0;
        chunWang.clear();
        zhengWang=0;
        hdFirstSeat=0;
        disOrMo=0;
    }

    public List<Integer> removeActionSeat(int seat) {
        List<Integer> actionList = actionSeatMap.remove(seat);
        saveActionSeatMap();
        return actionList;
    }

    public void addActionSeat(int seat, List<Integer> actionlist) {
        actionSeatMap.put(seat, actionlist);
        KwMjPlayer player = seatMap.get(seat);
        addPlayLog(disCardRound + "_" + seat + "_" + MjDisAction.action_hasAction + "_" + StringUtil.implode(actionlist) + player.getExtraPlayLog());
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
            nowDisCardIds = MjHelper.toMajiang(StringUtil.explodeToIntList(info.getNowDisCardIds()));
        }

        if (!StringUtils.isBlank(info.getLeftPais())) {
            try {
                leftMajiangs = MjHelper.toMajiang(StringUtil.explodeToIntList(info.getLeftPais()));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

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

    /**
	 * ??????????????? ????????????????????????????????????????????????????????????
	 */
    private boolean checkAction(KwMjPlayer player, List<KwMj> cardList, List<Integer> hucards, int action) {
		boolean canAction = checkCanAction(player, action);// ????????????????????? ???????????????
		if (!canAction) {// ??????????????? ??????????????????
            int seat = player.getSeat();
            tempActionMap.put(seat, new MjTempAction(seat, action, cardList, hucards));
			// ?????????????????????????????????????????? ?????????????????????
            if (tempActionMap.size() == actionSeatMap.size()) {
                int maxAction = Integer.MAX_VALUE;
                int maxSeat = 0;
                Map<Integer, Integer> prioritySeats = new HashMap<>();
                int maxActionSize = 0;
                for (MjTempAction temp : tempActionMap.values()) {
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
                KwMjPlayer tempPlayer = seatMap.get(maxSeat);
                List<KwMj> tempCardList = tempActionMap.get(maxSeat).getCardList();
                List<Integer> tempHuCards = tempActionMap.get(maxSeat).getHucards();
                for (int removeSeat : prioritySeats.keySet()) {
                    if (removeSeat != maxSeat) {
                        removeActionSeat(removeSeat);
                    }
                }
                clearTempAction();
				playCommand(tempPlayer, tempCardList, tempHuCards, maxAction);// ?????????????????????????????????
            } else {
                if (isCalcOver()) {
                    calcOver();
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
    private void refreshTempAction(KwMjPlayer player) {
        tempActionMap.remove(player.getSeat());
		Map<Integer, Integer> prioritySeats = new HashMap<>();// ?????????????????????
        for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
            int seat = entry.getKey();
            List<Integer> actionList = entry.getValue();
            List<Integer> list = MjDisAction.parseToDisActionList(actionList);
            int priorityAction = MjDisAction.getMaxPriorityAction(list);
            prioritySeats.put(seat, priorityAction);
        }
        int maxPriorityAction = Integer.MAX_VALUE;
        int maxPrioritySeat = 0;
		boolean isSame = true;// ?????????????????????
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
                List<KwMj> tempCardList = tempAction.getCardList();
                List<Integer> tempHuCards = tempAction.getHucards();
                KwMjPlayer tempPlayer = seatMap.get(tempAction.getSeat());
                iterator.remove();
				playCommand(tempPlayer, tempCardList, tempHuCards, action);// ?????????????????????????????????
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
    public boolean checkCanAction(KwMjPlayer player, int action) {
		// ???????????????????????????
        List<Integer> stopActionList = MjDisAction.findPriorityAction(action);
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
	 * ????????????
	 *
	 * @param player
	 * @param majiangs
	 * @param sameCount
	 * @return
	 */
    private boolean canPeng(KwMjPlayer player, List<KwMj> majiangs, int sameCount) {
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
     * ????????????
     *
     * @param player
     * @param majiangs
     * @return
     */
    private boolean canChi(KwMjPlayer player, List<KwMj> majiangs,KwMj disMj) {
        if (nowDisCardIds.isEmpty()) {
            return false;
        }
        TreeSet<Integer> set=new TreeSet();
        set.add(disMj.getVal());
        for (KwMj mj:majiangs) {
            set.add(mj.getVal());
        }
        Iterator<Integer> it = set.iterator();
        int x=0;
        while (it.hasNext()){
            if(x==0){
                x=it.next();
            }else {
                if(it.next()-x!=1)
                    return false;
                x=it.next();
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
    private boolean canAnGang(KwMjPlayer player, List<KwMj> majiangs, int sameCount) {
        if (sameCount < 4) {
            return false;
        }
        if (player.getSeat() != getNextDisCardSeat()) {
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
    private boolean canMingGang(KwMjPlayer player, List<KwMj> majiangs, int sameCount) {
        List<KwMj> handMajiangs = player.getHandMajiang();
        List<Integer> pengList = MjHelper.toMajiangVals(player.getPeng());
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
        disOrMo=2;
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
	 * ????????????
	 *
	 * @return
	 */
    public boolean canGangHu() {
        return true;
    }

	// ????????????
    public boolean canDianPao() {
        if (onlyZimo != 1) {
            return true;
        }
        return false;
    }

    /**
	 * @param over
	 * @param selfMo
	 * @param winList
	 * @param birdCardsId
	 *            ???ID
	 * @param seatBirds
	 *            ?????????
	 * @param seatBridMap
	 *            ??????
	 * @param isBreak
	 * @return
	 */
    public ClosingMjInfoRes.Builder sendAccountsMsg(boolean over, boolean selfMo, List<Integer> winList, List<Integer> birdCardsId, int[] seatBirds, Map<Integer, Integer> seatBridMap, int catchBirdSeat, boolean isBreak) {

		// ????????????????????????
        if (over && jiaBei == 1) {
            int jiaBeiPoint = 0;
            int loserCount = 0;
            for (KwMjPlayer player : seatMap.values()) {
                if (player.getTotalPoint() > 0 && player.getTotalPoint() < jiaBeiFen) {
                    jiaBeiPoint += player.getTotalPoint() * (jiaBeiShu - 1);
                    player.setTotalPoint(player.getTotalPoint() * jiaBeiShu);
                } else if (player.getTotalPoint() < 0) {
                    loserCount++;
                }
            }
            if (jiaBeiPoint > 0) {
                for (KwMjPlayer player : seatMap.values()) {
                    if (player.getTotalPoint() < 0) {
                        player.setTotalPoint(player.getTotalPoint() - (jiaBeiPoint / loserCount));
                    }
                }
            }
        }

        //???????????????below???+belowAdd???
        if(over&&belowAdd>0&&playerMap.size()==2){
            for (KwMjPlayer player : seatMap.values()) {
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
        for (KwMjPlayer player : seatMap.values()) {
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
                build.setIsHu(lastId);
            }
            if (player.getSeat() == fangPaoSeat) {
                build.setFanPao(1);
                if(huConfirmList.isEmpty())
                    build.setFanPao(0);
            }
            if (winList != null && winList.contains(player.getSeat())) {
				// ?????????????????????????????????????????????
                builderList.add(0, build);
            } else {
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
            for (KwMjPlayer player : seatMap.values()) {
                if (player.getWinLoseCredit() > dyjCredit) {
                    dyjCredit = player.getWinLoseCredit();
                }
            }
            for (ClosingMjPlayerInfoRes.Builder builder : builderList) {
                KwMjPlayer player = seatMap.get(builder.getSeat());
                calcCommissionCredit(player, dyjCredit);
                builder.setWinLoseCredit(player.getWinLoseCredit());
                builder.setCommissionCredit(player.getCommissionCredit());
            }
        } else if (isGroupTableGoldRoom()) {
            // -----------??????????????????---------------------------------
            for (KwMjPlayer player : seatMap.values()) {
                player.setWinGold(player.getTotalPoint() * gtgDifen);
            }
            calcGroupTableGoldRoomWinLimit();
            for (ClosingMjPlayerInfoRes.Builder builder : builderList) {
                KwMjPlayer player = seatMap.get(builder.getSeat());
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
        res.addAllLeftCards(MjHelper.toMajiangIds(leftMajiangs));
        res.setCatchBirdSeat(catchBirdSeat);
        res.addAllIntParams(getIntParams());
        for (KwMjPlayer player : seatMap.values()) {
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
        ext.add(masterId + "");                          //2
        ext.add(TimeUtil.formatTime(TimeUtil.now()));    //3
        ext.add(playType + "");                          //4
        ext.add(canDianPao + "");                        //5
        ext.add(lastWinSeat + "");                       //6
        ext.add(isAutoPlay + "");                        //7
        ext.add(isLiuJu() + "");                         //8
        ext.add(over+"");                                //9
        ext.add(kingId+"");                              //10
        return ext;
    }

    @Override
    public void sendAccountsMsg() {
        ClosingMjInfoRes.Builder builder = sendAccountsMsg(true, false, null, null, null, null, 0, true);
        saveLog(true, 0l, builder.build());
    }

    @Override
    public Class<? extends Player> getPlayerClass() {
        return KwMjPlayer.class;
    }

    @Override
    public int getWanFa() {
        return getPlayType();
    }


    @Override
    public void checkReconnect(Player player) {
        ((KwMjPlayer) player).checkAutoPlay(0, true);
        if (state == table_state.play) {
            KwMjPlayer player1 = (KwMjPlayer) player;
            if (player1.getHandPais() != null && player1.getHandPais().size() > 0) {
                sendTingInfo(player1);
            }
        }
        sendGangBuMsg((KwMjPlayer) player);
        sendPiaoReconnect(player);
        if(askSeat==player.getSeat())
            askHaiDi(askSeat);
        sendFirstAct(player);
        if(sendJzkgSeat==player.getSeat()){
            player.writeComMessage(WebSocketMsgType.res_code_nxkwmj_jzkg);
        }
    }

    private void sendFirstAct(Player player){
        if(disNum==0){
            boolean bankerFirst=true;
            for (Integer seat:actionSeatMap.keySet()) {
                List<Integer> list = actionSeatMap.get(seat);
                if(seat==lastWinSeat){
                    if(list.get(KwMjConstants.ACTION_INDEX_HU)==1){
                        bankerFirst=true;
                        break;
                    }
                }else {
                    if(list.get(KwMjConstants.ACTION_INDEX_BAOTING)==1)
                        bankerFirst=false;
                }
            }
            if(player.getSeat()==lastWinSeat)
                if(!bankerFirst)
                    player.writeComMessage(WebSocketMsgType.res_code_nxkwmj_haveAct);
                else
                    player.writeComMessage(WebSocketMsgType.res_code_nxkwmj_finishAct);
        }
    }


    private void sendGangBuMsg(KwMjPlayer player){
        if(buIds.size()==0)
            return;
        KwMjPlayer p = seatMap.get(gangSeat);
        if(p==null)
            return;
        GangMoMajiangRes.Builder builder = GangMoMajiangRes.newBuilder();
        builder.setUserId(p.getUserId()+"");
        builder.setSeat(p.getSeat());
        builder.setName(p.getName());
        builder.setGangId(0);
        GangMoMajiangRes.Builder clone = builder.clone();
        for (Integer id:buIds){
            GangPlayMajiangRes.Builder res=GangPlayMajiangRes.newBuilder();
            res.setMajiangId(id);
            if(actionSeatMap.containsKey(player.getSeat())){
                //????????????????????????????????????????????????????????????????????????????????????????????????????????????
                List<Integer> acts = actionSeatMap.get(player.getSeat());
                res.addAllSelfAct(acts);
                addActionSeat(player.getSeat(),acts);
            }
            clone.addGangActs(res);
        }
        player.writeSocket(clone.build());
    }

    private void sendPiaoReconnect(Player player){
        if(piaoFenType==0||maxPlayerCount!=getPlayerCount())
            return;
        int count=0;
        for(Map.Entry<Integer, KwMjPlayer> entry:seatMap.entrySet()){
            player_state state = entry.getValue().getState();
            if(state==player_state.play||state==player_state.ready)
                count++;
        }
        if(count!=maxPlayerCount)
            return;

        for(Map.Entry<Integer, KwMjPlayer> entry:seatMap.entrySet()){
            KwMjPlayer p = entry.getValue();
            if(p.getUserId()==player.getUserId()){
                if(p.getPiaoFen()==-1){
                    player.writeComMessage(WebSocketMsgType.res_code_piaoFen);
                    continue;
                }
            }else {
                List<Integer> l=new ArrayList<>();
                l.add((int)p.getUserId());
                l.add(p.getPiaoFen());
                player.writeComMessage(WebSocketMsgType.res_code_broadcast_piaoFen, l);
            }
        }
    }

    @Override
    public boolean isAllReady() {
        if (getPlayerCount() < getMaxPlayerCount()) {
            return false;
        }
        for (Player player : getSeatMap().values()) {
            if(piaoFenType>0){
                if (!(player.getState() == player_state.ready||player.getState() == player_state.play))
                    return false;
            }else {
                if(player.getState() != player_state.ready)
                    return false;
            }
        }

        if(finishFapai==1)
            return false;
        changeTableState(table_state.play);
        if (piaoFenType>0) {
            boolean piaoFenOver = true;
            for (KwMjPlayer player : playerMap.values()) {
                if(player.getPiaoFen()==-1){
                    piaoFenOver = false;
                    break;
                }
            }
            if(!piaoFenOver){
                if (finishFapai==0) {
                    LogUtil.msgLog.info("Kwmj|sendPiaoFen|" + getId() + "|" + getPlayBureau());
                    ComRes msg = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_piaoFen).build();
                    for (KwMjPlayer player : playerMap.values()) {
                        if(player.getPiaoFen()==-1)
                            player.writeSocket(msg);
                    }
                }
                return false;
            }
        }
        return true;
    }


    @Override
    public boolean consumeCards() {
        return SharedConstants.consumecards;
    }

    @Override
    public void checkAutoPlay() {
        if (getSendDissTime() > 0) {
            for (KwMjPlayer player : seatMap.values()) {
                if (player.getLastCheckTime() > 0) {
                    player.setLastCheckTime(player.getLastCheckTime() + 1 * 1000);
                }
            }
            return;
        }

        if (getSendDissTime() > 0) {
            for (KwMjPlayer player : seatMap.values()) {
                if (player.getLastCheckTime() > 0) {
                    player.setLastCheckTime(player.getLastCheckTime() + 1 * 1000);
                }
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

			// ????????????????????????xx???????????????????????????
            for (KwMjPlayer player : seatMap.values()) {
                if (player.getState() != player_state.entry && player.getState() != player_state.over) {
                    continue;
                } else {
                    if (readyTime >= 5 && player.isAutoPlay()) {
						// ????????????????????????5???????????????
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
                    KwMjPlayer player = seatMap.get(seat);
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
                int action = 0, seat = 0;
                for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
                    seat = entry.getKey();
                    List<Integer> actList = MjDisAction.parseToDisActionList(entry.getValue());
                    if (actList == null) {
                        continue;
                    }

                    action = MjDisAction.getAutoMaxPriorityAction(actList);
                    KwMjPlayer player = seatMap.get(seat);
                    if (!player.checkAutoPlay(0, false)) {
                        continue;
                    }
                    boolean chuPai = false;
                    if (player.isAlreadyMoMajiang()) {
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
                                KwMj mj = nowDisCardIds.get(0);
                                List<KwMj> mjList = new ArrayList<>();
                                for (KwMj handMj : player.getHandMajiang()) {
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
                    } else {
                        playCommand(player, new ArrayList<>(), MjDisAction.action_pass);
                        if (chuPai) {
                            autoChuPai(player);
                        }
                    }
                }
            }
        } else {
            boolean finishPiaoFen=true;
            for(KwMjPlayer player:seatMap.values()){
                if(player.getPiaoFen()==-1){
                    finishPiaoFen=false;
                    break;
                }
            }
            if(piaoFenType>0&&!finishPiaoFen){
                for(KwMjPlayer player:seatMap.values()){
                    if(player.getPiaoFen()==-1){
                        if (player == null || !player.checkAutoPiaoFen()) {
                            continue;
                        }
                        piaoFen(player,0);
                    }
                }
            }else {
                KwMjPlayer player;
                if(askSeat!=0)
                    player=seatMap.get(askSeat);
                else
                    player= seatMap.get(nowDisCardSeat);
                if (player == null || !player.checkAutoPlay(0, false)) {
                    return;
                }
                autoChuPai(player);
            }
        }
    }

    public void autoChuPai(KwMjPlayer player) {
        if (!player.isAlreadyMoMajiang()) {
            return;
        }
        List<Integer> handMjIds = new ArrayList<>(player.getHandPais());
        int mjId = -1;
        if (moMajiangSeat == player.getSeat()) {
            mjId = getMjIdNoWang(handMjIds,1);
        } else {
            Collections.sort(handMjIds);
            mjId = getMjIdNoWang(handMjIds,1);
        }
        if (mjId != -1) {
            List<KwMj> mjList = MjHelper.toMajiang(Arrays.asList(mjId));
            playCommand(player, mjList, MjDisAction.action_chupai);
        }
    }

    private int getMjIdNoWang(List<Integer> handMjIds,int num){
        int mjId = handMjIds.get(handMjIds.size() - num);
        int val=KwMj.getMajang(mjId).getVal();
        if(chunWang.contains(val))
            return getMjIdNoWang(handMjIds,num+1);
        return mjId;
    }


    @Override
    public void createTable(Player player, int play, int bureauCount, Object... objects) throws Exception {
    }


    public synchronized void piaoFen(KwMjPlayer player, int fen){
        if (piaoFenType==0||player.getPiaoFen()!=-1)
            return;
        if(fen>4||fen<0)
            return;
        player.setPiaoFen(fen);
        StringBuilder sb = new StringBuilder("kwmj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append("piaoFen").append("|").append(fen);
        LogUtil.msgLog.info(sb.toString());
        int confirmTime=0;
        for (Map.Entry<Integer, KwMjPlayer> entry : seatMap.entrySet()) {
            entry.getValue().writeComMessage(WebSocketMsgType.res_code_broadcast_piaoFen, (int)player.getUserId(),player.getPiaoFen());
            if(entry.getValue().getPiaoFen()!=-1)
                confirmTime++;
        }
        if (confirmTime == maxPlayerCount) {
            checkDeal(player.getUserId());
        }
    }



	// ??????????????????
    public boolean isTwoPlayer() {
        return getMaxPlayerCount() == 2;
    }


    public static final List<Integer> wanfaList = Arrays.asList(GameUtil.game_type_nxkwmj);

    public static void loadWanfaTables(Class<? extends BaseTable> cls) {
        for (Integer integer : wanfaList) {
            TableManager.wanfaTableTypesPut(integer, cls);
        }
    }

    public void logFaPaiTable(KwMj king) {
        StringBuilder sb = new StringBuilder();
        sb.append("kwmj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append("faPai");
        sb.append("|").append(playType);
        sb.append("|").append(maxPlayerCount);
        sb.append("|").append(getPayType());
        sb.append("|").append(lastWinSeat);
        sb.append("|").append(king);
        LogUtil.msg(sb.toString());
    }

    public void logFaPaiPlayer(KwMjPlayer player, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("kwmj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append("faPai");
        sb.append("|").append(player.getHandMajiang());
        sb.append("|").append(actListToString(actList));
        LogUtil.msg(sb.toString());
    }

    public void logAction(KwMjPlayer player, int action, List<KwMj> mjs, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("kwmj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        String actStr = "unKnown-" + action;
        if (action == MjDisAction.action_peng) {
            actStr = "peng";
        } else if (action == MjDisAction.action_minggang) {
            actStr = "baoTing";
        } else if (action == MjDisAction.action_chupai) {
            actStr = "chuPai";
        } else if (action == MjDisAction.action_pass) {
            actStr = "guo";
        } else if (action == MjDisAction.action_angang) {
            actStr = "anGang";
        } else if (action == MjDisAction.action_chi) {
            actStr = "chi";
        }
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append(player.isAutoPlaySelf() ? 1 : 0);
        sb.append("|").append(actStr);
        sb.append("|").append(mjs);
        sb.append("|").append(actListToString(actList));
        LogUtil.msg(sb.toString());
    }

    public void logMoMj(KwMjPlayer player, KwMj mj, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("kwmj");
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

    public void logMoMj(KwMjPlayer player, KwMj mj) {
        StringBuilder sb = new StringBuilder();
        sb.append("kwmj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append("fengDongMoPai");
        sb.append("|").append(leftMajiangs.size());
        sb.append("|").append(mj);
        LogUtil.msg(sb.toString());
    }

    public void logChuPaiActList(KwMjPlayer player, KwMj mj, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("kwmj");
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

    public void logBaoTing(KwMjPlayer player) {
        StringBuilder sb = new StringBuilder();
        sb.append("kwmj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append("baoTing");
        sb.append("|").append(player.getHandMajiang());
        LogUtil.msg(sb.toString());
    }

    public void logActionHu(KwMjPlayer player, List<KwMj> mjs, String daHuNames) {
        StringBuilder sb = new StringBuilder();
        sb.append("kwmj");
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
                if (i == KwMjConstants.ACTION_INDEX_HU) {
                    sb.append("hu");
                } else if (i == KwMjConstants.ACTION_INDEX_PENG) {
                    sb.append("peng");
                } else if (i == KwMjConstants.ACTION_INDEX_MINGGANG) {
                    sb.append("mingGang");
                } else if (i == KwMjConstants.ACTION_INDEX_ANGANG) {
                    sb.append("anGang");
                } else if (i == KwMjConstants.ACTION_INDEX_CHI) {
                    sb.append("chi");
                } else if (i == KwMjConstants.ACTION_INDEX_BAOTING) {
                    sb.append("baoTing");
                }
            }
        }
        sb.append("]");
        return sb.toString();
    }

    /**
	 * ?????????????????????
	 *
	 * @return
	 */
    public int getLeftMajiangCount() {
        return this.leftMajiangs.size();
    }

    /**
	 * ???????????????
	 */
    public void clearMoTailPai() {
        this.moTailPai.clear();
        changeExtend();
    }

    /**
	 * ????????????
	 *
	 * @return
	 */
    public int isLiuJu() {
        return (huConfirmList.size() == 0 && leftMajiangs.size() == 0) ? 1 : 0;
    }


    public void sendTingInfo(KwMjPlayer player) {
        if (player.isAlreadyMoMajiang()) {
            if (actionSeatMap.containsKey(player.getSeat())) {
                return;
            }
            DaPaiTingPaiRes.Builder tingInfo = DaPaiTingPaiRes.newBuilder();
            List<KwMj> cards = new ArrayList<>(player.getHandMajiang());
            Map<Integer, List<Integer>> daTing = TingTool.getDaTing(MjHelper.toMajiangIds(cards),getChunWang(),getZhengWang(),player.getCardTypes());
            for(Map.Entry<Integer, List<Integer>> entry : daTing.entrySet()){
                DaPaiTingPaiInfo.Builder ting = DaPaiTingPaiInfo.newBuilder();
                ting.addAllTingMajiangIds(entry.getValue());
                ting.setMajiangId(entry.getKey());
                tingInfo.addInfo(ting);
            }
            if(daTing.size()>0)
                player.writeSocket(tingInfo.build());

        } else {
            List<KwMj> cards = new ArrayList<>(player.getHandMajiang());
            TingPaiRes.Builder ting = TingPaiRes.newBuilder();
            List<Integer> tingP = TingTool.getTing(MjHelper.toMajiangIds(cards),getChunWang(),getZhengWang(),player.getCardTypes(),false);
            ting.addAllMajiangIds(tingP);
            if(tingP.size()>0)
                player.writeSocket(ting.build());
        }
    }

    public String getTableMsg() {
        Map<String, Object> json = new HashMap<>();
		json.put("wanFa", "??????????????????");
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
		return "??????????????????";
    }
}
