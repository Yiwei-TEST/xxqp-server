package com.sy599.game.qipai.cdepaohuzi.bean;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
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
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sy599.game.GameServerConfig;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.UserResourceType;
import com.sy599.game.common.bean.CreateTableInfo;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.common.constant.SharedConstants.player_state;
import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.db.bean.DataStatistics;
import com.sy599.game.db.bean.PlayLogTable;
import com.sy599.game.db.bean.TableInf;
import com.sy599.game.db.bean.UserExtend;
import com.sy599.game.db.bean.UserGroupPlaylog;
import com.sy599.game.db.bean.UserPlaylog;
import com.sy599.game.db.bean.gold.GoldRoom;
import com.sy599.game.db.dao.DataStatisticsDao;
import com.sy599.game.db.dao.TableDao;
import com.sy599.game.db.dao.TableLogDao;
import com.sy599.game.db.dao.UserDao;
import com.sy599.game.db.dao.gold.GoldRoomDao;
import com.sy599.game.manager.TableManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.PlayPaohuziRes;
import com.sy599.game.msg.serverPacket.TablePhzResMsg;
import com.sy599.game.msg.serverPacket.TablePhzResMsg.ClosingPhzInfoRes;
import com.sy599.game.msg.serverPacket.TablePhzResMsg.ClosingPhzPlayerInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.CreateTableRes;
import com.sy599.game.msg.serverPacket.TableRes.DealInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes;
import com.sy599.game.qipai.cdepaohuzi.command.AbsCodeCommandExecutor;
import com.sy599.game.qipai.cdepaohuzi.constant.HuType;
import com.sy599.game.qipai.cdepaohuzi.constant.PaohuziConstant;
import com.sy599.game.qipai.cdepaohuzi.constant.PaohzCard;
import com.sy599.game.qipai.cdepaohuzi.rule.PaohuziIndex;
import com.sy599.game.qipai.cdepaohuzi.rule.PaohuziMingTangRule;
import com.sy599.game.qipai.cdepaohuzi.rule.PaohuziMingTangRule.BigMapEntry;
import com.sy599.game.qipai.cdepaohuzi.rule.PaohzCardIndexArr;
import com.sy599.game.qipai.cdepaohuzi.rule.RobotAI;
import com.sy599.game.qipai.cdepaohuzi.tool.PaohuziHuLack;
import com.sy599.game.qipai.cdepaohuzi.tool.PaohuziResTool;
import com.sy599.game.qipai.cdepaohuzi.tool.PaohuziTool;
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

import lombok.Data;

@Data
public class CdePaohuziTable extends BaseTable {
    @Override
    public String getGameName() {
        return "常德跑胡子";
    }

    public static final List<Integer> wanfaList = Arrays.asList(GameUtil.play_type_changdephz);

    //游戏模式
    private GameModel gameModel = GameModel.builder().build();
    /*** 玩家map */
    private Map<Long, CdePaohuziPlayer> playerMap = new ConcurrentHashMap<>();
    /*** 座位对应的玩家 */
    private Map<Integer, CdePaohuziPlayer> seatMap = new ConcurrentHashMap<>();
    //开局所有底牌
    private volatile List<Integer> startLeftCards = new ArrayList<>();
    //当前桌面底牌
    private volatile List<PaohzCard> leftCards = new ArrayList<>();
    //摸牌flag
    private volatile int moFlag;
    //应该要打牌的flag
    private volatile int toPlayCardFlag;
    private volatile PaohuziCheckCardBean autoDisBean;
    private volatile int moSeat;
    private volatile PaohzCard zaiCard;
    private volatile PaohzCard beRemoveCard;
    private volatile int maxPlayerCount = 3;
    private volatile List<Integer> huConfirmList = new ArrayList<>();
    //摸牌时对应的座位, 牌的ID->座位号
    private volatile KeyValuePair<Integer, Integer> moSeatPair;
    //摸牌时对应的座位, 座位号->操作
    private volatile KeyValuePair<Integer, Integer> checkMoMark;
    //跑的位置
    //跑:
    //a、当别人打的牌或者摸的牌，自己手上正好有一坎,4张相同自动打出，称为跑；
    //b、当玩家从墩牌上摸的牌正好是自己碰过的牌，称为跑；
    //c、碰过后，别人在摸到该张，同样可以形成跑；跑的牌必须将牌放在桌面上（四张明牌）；可以破跑胡（情况一：摸出来的被胡算平胡；情况二：摸出来的被胡算自摸）。
    private volatile int sendPaoSeat;
    private volatile boolean firstCard = true;
    private volatile int shuXingSeat = 0;
    /**
     * 0胡 1碰 2栽 3提 4吃 5跑 6臭栽
     */
    private Map<Integer, List<Integer>> actionSeatMap = new ConcurrentHashMap<>();
    //当前已经打出的牌 , 上一轮已经打出来的牌
    private volatile List<PaohzCard> nowDisCardIds = new ArrayList<>();

    //抽掉的牌堆
    private List<Integer> chouCards = new ArrayList<>();

    /**
     * 托管1：单局，2：全局, 3:全局
     */
    private int autoPlayGlob;
    private int curPlayMaxGlob;

    private volatile int timeNum = 0;

    /**
     * 玩家位置对应临时操作
     * 当同时存在多个可做的操作时
     * 1当优先级最高的玩家最先操作 则清理所有操作提示 并执行该操作
     * 2当操作优先级低的玩家先选择操作，则玩家先将所做操作存入临时操作中 当玩家优先级最高的玩家操作后 在判断临时操作是否可执行并执行
     */
    private Map<Integer, TempAction> tempActionMap = new ConcurrentHashMap<>();

    //是否完成发牌
    private int finishFapai = 0;

    /**
     * 托管时间
     */
    private volatile int autoTimeOut = Integer.MAX_VALUE;
    private volatile int autoTimeOut2 = Integer.MAX_VALUE;

    //本局胡牌类型
    private HuType huType = HuType.PING_HU;
    //跑胡
    private int paoHu=0;
    //爬坡 黄庄必定触发, 黄一次*2两次*3三次*4
    private int uphillRatio;

    /**
     * 获取所有底牌内容
     */
    public List<Integer> getStartLeftCards() {
        return startLeftCards;
    }

    @Override
    public boolean ready(Player player) {
        boolean flag = super.ready(player);
        return playedBureau > 0 && flag;
    }

    @Override
    public boolean isAllReady() {
        return isAllReadyCheck();
    }


    /**
     * @param filterUserId 过滤玩家,不发送选坨消息
     * @return
     * @description
     */
    public boolean isAllReadyCheck(int... filterUserId) {
        if (getPlayerCount() < getMaxPlayerCount()) {
            return false;
        }

        for (Player player : getSeatMap().values()) {
            if (!player.isRobot()) {
                if (player.getState() != player_state.ready)
                    return false;
            }
        }

        if (finishFapai == 1)
            return false;

        //坨选项
        if (!canStart()) {
            LogUtil.printDebug("选坨开始.... ");
            //公告客户端选锤
            //需要做重复发送检测,仅发送一次选锤信息
            if (!gameModel.isRepeatedSendTuo()) {
                gameModel.setRepeatedSendTuo(true);
                //仅仅给没有选坨的用户发送消息
                broadcast((filterUserId != null && filterUserId.length > 0) ?
                                broadcastStream()
                                        .filter(v -> ((CdePaohuziPlayer) v).getTuo() <= 0)
                                        .filter(v -> !IntStream.of(filterUserId).anyMatch(p -> p == v.getUserId())) :
                                broadcastStream()
                                        .filter(v -> ((CdePaohuziPlayer) v).getTuo() <= 0),
                        WebSocketMsgType.RES_XXGHZ_TUO_START, gameModel.getTuo());
                LogUtil.printDebug("选坨广播....");
            }

            return false;
        }

        changeTableState(table_state.play);

        return true;
    }

    /**
     * @param
     * @return
     * @description 从库内加载
     */
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
            autoDisBean = new PaohuziCheckCardBean();
            autoDisBean.initAutoDisData(autoDisPhz);
        }
        zaiCard = PaohzCard.getPaohzCard(wrapper.getInt(7, 0));
        sendPaoSeat = wrapper.getInt(8, 0);
        firstCard = wrapper.getInt(9, 1) == 1 ? true : false;
        beRemoveCard = PaohzCard.getPaohzCard(wrapper.getInt(10, 0));
        shuXingSeat = wrapper.getInt(11, 0);
        maxPlayerCount = wrapper.getInt(12, 3);
        startLeftCards = loadStartLeftCards(wrapper.getString("startLeftCards"));

        //创房选项
        gameModel = Optional.ofNullable(JSONObject.parseObject(wrapper.getString(13), GameModel.class)).orElseGet(GameModel::new);

        autoPlayGlob = wrapper.getInt(20, 0);
        autoTimeOut = wrapper.getInt(21, 0);
        if (autoPlay && autoTimeOut <= 1) {
            autoTimeOut = 60000;
        }
        autoTimeOut2 = autoTimeOut;

        if (payType == -1) {
            String isAAStr = wrapper.getString("isAAConsume");
            if (!StringUtils.isBlank(isAAStr)) {
                this.payType = Boolean.parseBoolean(wrapper.getString("isAAConsume")) ? 1 : 2;
            } else {
                payType = 1;
            }
        }

        autoPlayGlob = wrapper.getInt(20, 0);
        tempActionMap = loadTempActionMap(wrapper.getString("22"));
        finishFapai = wrapper.getInt(24, 0);
        huType = HuType.values()[wrapper.getInt(25, 0)];
        uphillRatio = wrapper.getInt(26, 0);
        paoHu = wrapper.getInt(27, 0);
        lastWinSeat = wrapper.getInt(28, 0);

        //TODO 由于版本更新，需对旧牌桌数据进行兼容,以下代码可在下一版本更新可以删除代码
        // -----------------start------------------------------
        if (finishFapai == 0) {
            // 检查玩家身上是否有发牌
            for (CdePaohuziPlayer player : seatMap.values()) {
                if (player.getHandPais() != null && player.getHandPais().size() > 0) {
                    finishFapai = 1;
                    break;
                }
            }
        }
        // -----------------end------------------------------
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

        //计算胡牌类型
        boolean isHuangZhuang = calcHuType() == HuType.HUANG_ZHUANG;

        List<Integer> winList = new ArrayList<>(huConfirmList);

        LogUtil.printDebug("胡类型:{}", huType);
        int goldPay = 0;//服务费
        int goldRatio = 1;//倍率
        boolean isGold = false;
//        try {
//            if (isGoldRoom()) {
//                GoldRoom goldRoom = GoldRoomDao.getInstance().loadGoldRoom(id);
//                if (goldRoom != null) {
//                    isGold = true;
//                    modeId = goldRoom.getModeId();
//                    goldPay = PayConfigUtil.get(playType, goldRoom.getGameCount(), goldRoom.getMaxCount(), 0, goldRoom.getModeId());
//                    if (goldPay < 0) {
//                        goldPay = 0;
//                    }
//                    goldRatio = GameConfigUtil.loadGoldRatio(modeId);
//                }
//            }
//        } catch (Exception e) {
//        }
        Map<Integer, Integer> mt = null;
        int winFen = 0;
		int at = 0;
        int totalTun = 0;// 赢的胡息数或屯数
        boolean isOver = false;
        Map<Long, Integer> outScoreMap = new HashMap<>();
        Map<Long, Integer> ticketMap = new HashMap<>();
        if (isGold) {       //金币场
            isOver = true;
            if (winList.isEmpty()) {
                for (CdePaohuziPlayer player : seatMap.values()) {
                    player.changeGold(-goldPay, playType);
                    player.calcResult(this, 1, 0, isHuangZhuang);
                }
            } else {
                CdePaohuziPlayer winPlayer = seatMap.get(winList.get(0));
                mt = PaohuziMingTangRule.calcMingTang(winPlayer);
                winFen = PaohuziMingTangRule.calcMingTangFen(winPlayer.getTotalHu(), this, mt);
                long totalWin = 0;
                for (int seat : seatMap.keySet()) {
                    if (!winList.contains(seat)) {
                        CdePaohuziPlayer player = seatMap.get(seat);
                        int lossPoint = winFen;
                        if (goldRatio > 1) {
                            lossPoint *= goldRatio;
                        }
                        long allGold = player.loadAllGolds();
                        if (allGold < goldPay + lossPoint) {
                            totalWin += (allGold - goldPay);
                            player.changeGold((int) -allGold, playType);
                            player.calcResult(this, 1, (int) (goldPay - allGold), isHuangZhuang);
                        } else {
                            totalWin += lossPoint;
                            player.changeGold((int) -(lossPoint + goldPay), playType);
                            player.calcResult(this, 1, -lossPoint, isHuangZhuang);
                        }
                    }
                }

                winPlayer.calcResult(this, 1, (int) totalWin, isHuangZhuang);

                long allGold = winPlayer.loadAllGolds();
                if (totalWin > allGold) {
                    outScoreMap.put(winPlayer.getUserId(), (int) (totalWin - allGold));

                    totalWin = allGold;
                }

                if (totalWin > 0) {
                    Integer tmpConfig = ResourcesConfigsUtil.loadIntegerValue("TicketConfig", "gold_room_award" + modeId);
                    if (tmpConfig != null && tmpConfig.intValue() > 0) {
                        int ticketCount = (int) (totalWin / (tmpConfig.intValue()));
                        if (ticketCount > 0) {
                            ticketMap.put(winPlayer.getUserId(), ticketCount);
                            UserDao.getInstance().saveOrUpdateUserExtend(new UserExtend(UserResourceType.TICKET.getType(),
                                    String.valueOf(winPlayer.getUserId()), UserResourceType.TICKET.name(), String.valueOf(ticketCount), UserResourceType.TICKET.getName()));
                            LogUtil.msgLog.info("get ticket:table modeId={},userId={},ticket={}", modeId, winPlayer.getUserId(), ticketCount);
                        }
                    }
                }

                winPlayer.changeGold((int) (totalWin - goldPay), playType);
            }
        } else {
            //非金币场 , 这里的积分计算仅仅在房间内有效, 在发送结算消息时, 计算总输赢分对于信用分(俱乐部分)的汇率转化
            for (int winSeat : winList) {
                // 赢的玩家
                boolean isSelfMo = winSeat == moSeat;
                CdePaohuziPlayer winPlayer = seatMap.get(winSeat);
//                int winnerAddPoint = 0;
                winPlayer.changeAction(PaohuziConstant.ACTION_COUNT_INDEX_HU, 1);
                if (isSelfMo) {
                    winPlayer.changeAction(PaohuziConstant.ACTION_COUNT_INDEX_ZIMO, 1);
                }
                //计算名堂
                mt = PaohuziMingTangRule.calcMingTang(winPlayer);
//              增加的囤数先增加再做翻倍
                int rewardHuXi = 0;
                int rewardTun = 0;
//

                //自摸增加基础分,自摸&放炮不会同时存在
//                if (gameModel.getSpecialPlay().isSinceTouchAdd3Score() && huType == HuType.ZI_MO) {
//                    int score = subMingTangScore(mt, PaohuziMingTangRule.LOUDI_MINGTANG_ZIMO_ADD);
//                    rewardHuXi += score;
//                    rewardTun += score;
////                    rewardTun += winPlayer.calcHuPoint(rewardHuXi, gameModel.getConverHuXiToTunRatio());
//                }

				//额外奖励分
//                winPlayer.setAwardWinLossPoint(winPlayer.getAwardWinLossPoint() + rewardTun);
                winPlayer.setAwardWinLossPoint(winPlayer.getAwardWinLossPoint());

                LogUtil.printDebug("底分:{},起胡:{},转换比例:{},增加底分:{},奖励:{}", winPlayer.getTotalHu(), gameModel.getRoundFinishLowestHuXi(), gameModel.getConverHuXiToTunRatio(),gameModel.getBasicSocreAdd(),rewardTun);

                //计算名堂分,这里总胡息减去起胡的胡息数算作1囤,要做名堂叠加,所以算出这1囤对应的胡息数再做运算
//                winFen = PaohuziMingTangRule.calcMingTangFen((winPlayer.getTotalHu() - gameModel.getRoundFinishLowestHuXi()) + (1 * gameModel.getConverHuXiToTunRatio()), this, mt) - rewardHuXi;
                at = winPlayer.calcHuPoint((winPlayer.getTotalHu() - gameModel.getRoundFinishLowestHuXi()), gameModel.getConverHuXiToTunRatio()) + gameModel.getBasicSocreAdd() + rewardTun;

                winFen = PaohuziMingTangRule.calcMingTangFen(at, this, mt) - rewardHuXi;
//                LogUtil.printDebug("自摸地胡或点炮地胡总分:{}", rewardTun);

//                爬坡
				if (getUphillRatio() > 0) {
					winFen *= (getUphillRatio() + 1);
					mt.put(PaohuziMingTangRule.LOUDI_MINGTANG_UPHILL, getUphillRatio() + 1);
					LogUtil.printDebug("爬坡:{}->{}", winFen, totalTun);
				}


				LogUtil.printDebug("名堂计算:{}->{}", at, winFen);
                LogUtil.printDebug("上限检测:{}->{}", winFen, gameModel.limitUpper(huType, winFen));

                //胡息番数封顶限制
                winFen = gameModel.limitUpper(huType, winFen);

                //胡息转换为囤数,这里起胡的胡息数算作1囤
//                totalTun = winPlayer.calcHuPoint(winFen, gameModel.getConverHuXiToTunRatio()) + rewardTun;//得屯数, 默认3息1囤
//                totalTun = winPlayer.calcHuPoint(winFen, gameModel.getConverHuXiToTunRatio()) ;//得屯数, 默认3息1囤
                totalTun = winFen;

                LogUtil.printDebug("囤数转换:{}->{}", winFen, totalTun);


                if (gameModel.getSpecialPlay().isSinceTouchAdd3Score() && gameModel.getSpecialPlay().isSinceTouchFanBei()
                        && mt.containsKey(PaohuziMingTangRule.LOUDI_MINGTANG_ZIMO) && mt.containsKey(PaohuziMingTangRule.LOUDI_MINGTANG_ZIMO_ADD)) {
                    mt.put(PaohuziMingTangRule.LOUDI_MINGTANG_ZIMO_JIAFAN_JIATUN,0);

                    mt.remove(PaohuziMingTangRule.LOUDI_MINGTANG_ZIMO_ADD);
                    mt.remove(PaohuziMingTangRule.LOUDI_MINGTANG_ZIMO);

                    LogUtil.printDebug("自摸加番加囤:{}->{}", winFen, totalTun);
                }

                //点炮, 找出点炮玩家, 扣除玩家积分, 扣除各种名堂分
                if (gameModel.getSpecialPlay().isIgnite() && huType == HuType.DIAN_PAO) {
					for (int seat : seatMap.keySet()) {
						if (winList.contains(seat)) {
							CdePaohuziPlayer player = seatMap.get(disCardSeat);
							//减去名堂分后的基础分
							LogUtil.printDebug("点炮输家{}:展示分{},{}->胡息{},扣分:{}", player.getUserId(), player.getPoint(), player.getTotalPoint(), player.getTotalPoint(), totalTun);
							player.calcResult(this, 1, -totalTun, isHuangZhuang);
							LogUtil.printDebug("点炮输家{}:展示分{},{}->胡息{},扣分:{}", player.getUserId(), player.getPoint(), player.getTotalPoint(), player.getTotalPoint(), totalTun);

							//赢家分数
							winPlayer.calcResult(this, 1, (totalTun), isHuangZhuang);
							LogUtil.printDebug("赢家{}:展示分{},{}->隐藏分{}", winPlayer.getUserId(), winPlayer.getPoint(), winPlayer.getTotalPoint(), winPlayer.getTotalPoint());
						}
					}

				}else{

					//输家处理,赢家不处理,点炮玩家单独处理
					for (int seat : seatMap.keySet()) {
						if (!winList.contains(seat)) {
							CdePaohuziPlayer player = seatMap.get(seat);
							LogUtil.printDebug("输家{}:展示分{},{}->胡息{},扣分:{}", player.getUserId(), player.getPoint(), player.getTotalPoint(), player.getTotalPoint(), totalTun);
							//每个输家都扣除赢家赢得筹码数, 赢家分数*=人数
							player.calcResult(this, 1, -totalTun, isHuangZhuang);
							LogUtil.printDebug("输家{}:展示分{},{}->胡息{},扣分:{}", player.getUserId(), player.getPoint(), player.getTotalPoint(), player.getTotalPoint(), totalTun);
						}
					}
					//赢家分数
					winPlayer.calcResult(this, 1, (totalTun * Math.max((seatMap.size() - winList.size()), 1)), isHuangZhuang);
					LogUtil.printDebug("赢家{}:展示分{},{}->隐藏分{}", winPlayer.getUserId(), winPlayer.getPoint(), winPlayer.getTotalPoint(), winPlayer.getTotalPoint());
				}

            }

            //仅计算赢家的胡息数量是否大于等于结算分即可验证是否大结算关闭房间
            isOver = isFinish(); //winPlayer.getTotalPoint()  >= gameModel.getGameFinishMaxHuXi();//

            //荒庄扣分
            if (winList.isEmpty()) {
                CdePaohuziPlayer lastWinPlayer = seatMap.get(lastWinSeat);
                lastWinPlayer.calcResult(this, 1, -lastWinPlayer.calcHuPoint(gameModel.getSpecialPlay().getNoneHuCardSubHuXi(), gameModel.getConverHuXiToTunRatio()), isHuangZhuang);

                //爬坡
				if (gameModel.getSpecialPlay().isUphill()) {
					//爬坡,不换庄
					setUphillRatio(getUphillRatio() + 1);
				}

                //换庄
//                setLastWinSeat(calcNextSeat(lastWinSeat));
            } else {
                //重置爬坡
                setUphillRatio(0);
            }
        }
        // 金币场
        if (isGoldRoom()) {
            for (CdePaohuziPlayer player : seatMap.values()) {
                player.setWinGold(player.getWinLossPoint());
            }
            calcGoldRoom();
        }
        if (autoPlayGlob > 0 && autoPlayGlob != 2) {
//          //是否解散
            boolean diss = false;
            //每一局仅+1,托管解散,1.1局后自动解散,2.全局都不解散,3.3局后解散
            for (CdePaohuziPlayer seat : seatMap.values()) {
                if (seat.isAutoPlay()) {
                    diss = ++curPlayMaxGlob >= autoPlayGlob;
                    break;
                }
            }
            if (diss) {
                autoPlayDiss = true;
                isOver = true;
            }
        }

        ClosingPhzInfoRes.Builder res = sendAccountsMsg(isOver, winList, at, mt, totalTun, false, outScoreMap, ticketMap);
        saveLog(isOver, 0L, res.build());


        if (!winList.isEmpty()) {
            //连庄
            setLastWinSeat(winList.get(0));
        }

        calcAfter();
        //结算,每局进行房卡结算
        initNext(isOver);
        if (isOver) {
            calcOver1();
            calcOver2();
            calcOver3();
            diss();
        } else {
            calcOver1();
        }
        for (Player player : seatMap.values()) {
            player.saveBaseInfo();
        }
    }

    /**
     * @param
     * @return
     * @description 点炮必定胡
     */
    public boolean igniteMustHu() {
        if (gameModel.getSpecialPlay().isIgnite() && gameModel.getSpecialPlay().isIgniteMustHu()) {
            return seatMap.values().stream().anyMatch(this::igniteMustHu);
        }
        return false;
    }

    /**
     * @param
     * @return
     * @description
     */
    private boolean igniteMustHu(CdePaohuziPlayer player) {
        boolean res = false;
        List<Integer> actionList = actionSeatMap.get(player.getSeat());
        //操作列表有胡, 同时胡牌类型是点炮
        if (!CollectionUtils.isEmpty(actionList) && actionList.get(0) > 0 && !isMoFlag() && disCardSeat != player.getSeat() /*&& gameModel.getSpecialPlay().isIgnite() && gameModel.getSpecialPlay().isIgniteMustHu()*/) {
            hu(player, null, PaohuziDisAction.action_hu, getNowDisCardIds().get(0));
            res = true;
        }
        return res;
    }

    /**
     * @param
     * @return
     * @description 获得某个名堂的胡息
     */
    public int subMingTangScore(Map<Integer,Integer> mt, Integer mtIndex) {
        int res = 0;
        PaohuziMingTangRule.BigMapEntry<Integer, String, int[]> integerStringIntegerBigMap;
        if (mt.containsKey(mtIndex) && (integerStringIntegerBigMap = PaohuziMingTangRule.SCORE_CALC.get(mtIndex)) != null) {
            if (integerStringIntegerBigMap.getE().equals("+")) {    //这里仅仅识别递增的
				res += integerStringIntegerBigMap.getV()[getGameModel().getBigSexEight() == 1 ? 0 : getGameModel().getBigSexEight() == 2 ? integerStringIntegerBigMap.getV().length > 1 ? 1 : 0 : 0];
            }
        }
        return res;
    }


    public HuType calcHuType() {
        HuType huType = null;
        if (CollectionUtils.isEmpty(huConfirmList) /*|| CollectionUtils.isEmpty(leftCards)*/) {
            // 流局
            huType = HuType.HUANG_ZHUANG;
        } else {
            int winnerSeat = seatMap.get(huConfirmList.get(0)).getSeat();
            LogUtil.printDebug("moFlag:{}, moSeat:{},winnerSeat:{}, disCardSeat:{}", isMoFlag(), getMoSeat(), winnerSeat, getDisCardSeat());

            //自摸是听牌后自己摸的一张牌胡了，平胡是别人摸出来的牌胡了，放炮是别人打出来的牌胡了
            if ((isMoFlag() || isFirstCard()) && winnerSeat == moSeat) {
                huType = HuType.ZI_MO;
            } else if (!isMoFlag() && disCardSeat != winnerSeat) {  //点炮
                huType = HuType.DIAN_PAO;
            } else {      //平胡
                huType = HuType.PING_HU;
            }
        }

        this.setHuType(huType);
        return huType;
    }

    /**
     * @param
     * @return 默认返回不加倍:1
     * @description 选坨的倍率
     */
    public int tuoRatio(CdePaohuziPlayer v, CdePaohuziPlayer v1) {
        int ratio = 1;
        if (gameModel.getTuo() > 0) {
//            long count = playerMap.values().stream().filter(v -> v.getTuo() == 1).count();
            long count = Stream.of(v, v1).filter(p -> p.getTuo() == 1).count();

            /*
            6.3 打坨：建房三选一选项：不打坨/坨对坨3番/坨对坨4番
            6.4 建房选择不打坨：全部准备直接发牌开始没有打坨/不打坨选项。
            6.5 建房时选择坨对坨3番：全部准备后出现选项打坨/不打坨，情况1：两家选择不打坨，结算时两家胡息相减后得到的积分不变；情况2：一家选择打坨一家选择不打坨，结算积分*2。情况3：两家都选择打坨，结算积分*3。
            6.6 建房时选择坨对坨4番：全部准备后出现选项打坨/不打坨，情况1：两家选择不打坨，结算时两家胡息相减后得到的积分不变；情况2：一家选择打坨一家选择不打坨，结算积分*2。情况3：两家都选择打坨，结算积分*4。
            */
            ratio = count == 1 ? 2 : count > 1 ? gameModel.getTuo() : 1;
        }

        return ratio;
    }

    /**
     * @param
     * @return
     * @description 房间解散条件, 结束条件
     */
    public boolean isFinish() {
        return playBureau >= gameModel.getGameFinishRound();
    }

    @Override
    public void saveLog(boolean over, long winId, Object resObject) {
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
        userLog.setType(creditMode == 1 ? 2 : 1);
        userLog.setGeneralExt(buildGeneralExtForPlaylog().toString());
        long logId = TableLogDao.getInstance().save(userLog);
        saveTableRecord(logId, over, playBureau);
        UdpLogger.getInstance().sendSnapshotLog(masterId, playLog, logRes);
        if (!isGoldRoom()) {
            for (CdePaohuziPlayer player : playerMap.values()) {
                player.addRecord(logId, playBureau);
            }
        }
    }

    @Override
    protected void loadFromDB1(TableInf info) {
        if (!StringUtils.isBlank(info.getNowDisCardIds())) {
            this.nowDisCardIds = PaohuziTool.explodePhz(info.getNowDisCardIds(), ",");
        }
        if (!StringUtils.isBlank(info.getLeftPais())) {
            this.leftCards = PaohuziTool.explodePhz(info.getLeftPais(), ",");
        }
        if (isGoldRoom()) {
            autoTimeOut = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "autoTimeOutPhz", 10 * 1000);
            autoTimeOut2 = autoTimeOut;
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
        CdePaohuziPlayer winPlayer = seatMap.get(lastWinSeat);

        for (CdePaohuziPlayer tablePlayer : seatMap.values()) {
            DealInfoRes.Builder res = DealInfoRes.newBuilder();
            res.addAllHandCardIds(tablePlayer.getSeat() == shuXingSeat ? winPlayer.getHandPais() : tablePlayer.getHandPais());
            res.setNextSeat(lastWinSeat);
            res.setGameType(getWanFa());// 1跑得快 2麻将
            res.setRemain(leftCards.size());
            res.setBanker(lastWinSeat);
            res.addXiaohu(winPlayer.getHandPais().get(0));
            tablePlayer.writeSocket(res.build());
            
            if(tablePlayer.isAutoPlay()) {
       		 	addPlayLog(tablePlayer.getSeat(), PaohuziDisAction.action_tuoguan + "",1 + "");
            }
        }
        
        //
        
        
    }

    @Override
    public void startNext() {
        //检测天胡
        checkTianHu(getLastWinSeat());
        checkThreeTiFiveKan();
        checkAction();
    }

    /**
     * @param cardIds 打出的牌号
     * @param action  操作指令
     * @return
     * @description 玩家操作
     */
    public void play(CdePaohuziPlayer player, List<Integer> cardIds, int action) {
        play(player, cardIds, action, false, false, false);
    }

    /**
     * @param
     * @return
     * @description 玩家指令 胡牌
     */
    private void hu(CdePaohuziPlayer player, List<PaohzCard> cardList, int action, PaohzCard nowDisCard) {
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return;
        }
        if (huConfirmList.contains(player.getSeat())) {
            return;
        }
        if (!checkAction(player, action, cardList, nowDisCard)) {
            player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip);
            // player.writeErrMsg(LangMsgEnum.code_29);
            return;
        }
        List<Integer> actionList = actionSeatMap.get(player.getSeat());
        if (actionList.get(0) != 1) {
            return;
        }
        
        if(firstCard&&nowDisCard==null){
        	 CdePaohuziPlayer bankPlayer = seatMap.get(lastWinSeat);
        	 if(bankPlayer!=null){
        		 nowDisCard = PaohzCard.getPaohzCard(bankPlayer.getHandPais().get(0));
//        		 if(bankPlayer.getSeat()!=player.getSeat()){
        			 player.setFirstDisSingleCard(true);
//        		 }
        		 List<PaohzCard> cardList2 = new ArrayList<PaohzCard>();
        		 cardList2.add(nowDisCard);
        		 setNowDisCardIds(cardList2);
        		 setMoFlag(1);
        	 }
        }
        
        PaohuziHuLack paoHu = player.checkPaoHu(nowDisCard, isSelfMo(player), (/*isBoPi() && */firstCard));
        PaohuziHuLack pingHu = player.checkHu(nowDisCard, isSelfMo(player));
        if (!pingHu.isHu()) {
            PaohuziCheckCardBean paoHuBean = player.checkPaoHu();
            if (paoHuBean.isHu()) {
                pingHu = paoHuBean.getLack();
            }
        }
        PaohuziHuLack hu = pingHu;
        if (paoHu.isHu()) {
            int paoHuOutHuXi = player.getPaoOutHuXi(nowDisCard, isSelfMo(player), (/*isBoPi() && */firstCard));
            if (paoHu.getHuxi() + paoHuOutHuXi > pingHu.getHuxi()+player.getOutHuxi()+player.getZaiHuxi()) {
                play(player, PaohuziTool.toPhzCardIds(paoHu.getPaohuList()), paoHu.getPaohuAction(), false, true, false);
                hu = player.checkHu(null, isSelfMo(player));
            }
        } else {
            hu = pingHu;
        }

        if (hu.isHu() || (gameModel.getSpecialPlay().isThreeTiFiveKan() && isThreeTiFiveKan(player))) {
            // broadMsg(player.getName() + " 胡牌");
            player.setHuxi(hu.getHuxi());
            player.setHu(hu);
            huConfirmList.add(player.getSeat());
            addPlayLog(player.getSeat(), action + "", PaohuziTool.implodePhz(cardList, ","));
            //自摸的音效客户端需要区分开来,这里临时用15代替calcHuType() == HuType.ZI_MO ? 15 :
            sendActionMsg(player,  action, null, PaohzDisAction.action_type_action);
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
    public boolean isSelfMo(CdePaohuziPlayer player) {
        if (moSeatPair != null) {
            return moSeatPair.getValue().intValue() == player.getSeat() || (player.getSeat() == shuXingSeat && moSeatPair.getValue().intValue() == lastWinSeat);
        }
        return false;
    }

    /**
     * 提
     */
    private void ti(CdePaohuziPlayer player, List<PaohzCard> cardList, PaohzCard nowDisCard, int action, boolean moPai) {
        // cards肯定是4个相同的
        if (cardList == null) {
            System.out.println("提不合法:" + cardList);
            player.writeErrMsg("提不合法:" + cardList);
            return;
        }

        if (cardList.size() == 1) {
            List<PaohzCard> tiCards = player.getTiCard(cardList.get(0));
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

        if (!PaohuziTool.isSameCard(cardList)) {
            System.out.println("提不合法:" + cardList);
            player.writeErrMsg("提不合法:" + cardList);
            return;
        }
        player.changeAction(PaohuziConstant.ACTION_COUNT_INDEX_TI, 1);
        addPlayLog(player.getSeat(), action + "", PaohuziTool.implodePhz(cardList, ","));
        if (nowDisCard != null) {
            getDisPlayer().removeOutPais(nowDisCard);
        }
        player.disCard(action, cardList);
        clearAction();
        setAutoDisBean(null);

        PaohuziCheckCardBean checkAutoDis = checkAutoDis(player, false);
        if (checkAutoDis != null) {
            playAutoDisCard(checkAutoDis, true);
        }

        // 检查是否能胡牌
        PaohuziCheckCardBean checkCard = player.checkPaoHu();
        checkPaohuziCheckCard(checkCard);

        // 是否能出牌
        if (!moPai) {
            // 不是轮到自己摸牌的时候提的牌
            boolean disCard = setOutCardPlayer(player, action, checkCard.isHu());
            if (!disCard) {
            	if(!firstCard){
            		player.setTingFlag(0);
            	}
            	
                // checkMo();
            }

        }
        sendActionMsg(player, action, cardList, PaohzDisAction.action_type_action, isZaiPao, false);
        // if (player.isFangZhao()) {
        // LogUtil.msgLog.info("----tableId:" + getId() + "---userName:" +
        // player.getName() + "------提-----解除放招状态" + cardList.get(0));
        // player.setFangZhao(0);
        // // player.writeComMessage(WebSocketMsgType.res_code_phz_fangzhao,
        // player.getUserId() + "", 0 + "");
        // relieveFangZhao(player.getUserId());
        // }

    }

    /**
     * 栽(臭栽)
     *
     * @param cardList 要栽的牌
     */
    private void zai(CdePaohuziPlayer player, List<PaohzCard> cardList, PaohzCard nowDisCard, int action) {
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return;
        }

        boolean isFristDisCard = player.isFristDisCard();
        PaohuziCheckCardBean checkAutoDis = checkAutoDis(player, false);
        if (checkAutoDis != null) {
            playAutoDisCard(checkAutoDis, true);
        }
        getDisPlayer().removeOutPais(nowDisCard);
        if (!cardList.contains(nowDisCard)) {
            cardList.add(0, nowDisCard);
        }
        setBeRemoveCard(nowDisCard);
        if (action == PaohzDisAction.action_zai) {
            setZaiCard(nowDisCard);

        }
        addPlayLog(player.getSeat(), action + "", PaohuziTool.implodePhz(cardList, ","));
        player.disCard(action, cardList);
        clearAction();
        setAutoDisBean(null);
        // 检查是否能胡牌
        PaohuziCheckCardBean checkCard = player.checkPaoHu();
        checkPaohuziCheckCard(checkCard);
        // 是否能出牌
        boolean disCard = setOutCardPlayer(player, action, isFristDisCard, checkCard.isHu());
        sendActionMsg(player, action, cardList, PaohzDisAction.action_type_action);
        // if (player.isFangZhao()) {
        // LogUtil.msgLog.info("----tableId:" + getId() + "---userName:" +
        // player.getName() + "------栽-----解除放招状态" + cardList.get(0));
        // player.setFangZhao(0);
        // // player.writeComMessage(WebSocketMsgType.res_code_phz_fangzhao,
        // player.getUserId() + "", 0 + "");
        // relieveFangZhao(player.getUserId());
        // }
        if (!disCard) {
            // checkMo();
        }

    }

    /**
     * 跑
     */
    private void pao(CdePaohuziPlayer player, List<PaohzCard> cardList, PaohzCard nowDisCard, int action, boolean isHu, boolean isPassHu) {
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

        // 能跑胡的情况下不胡挡不住跑
        if (!isHu && !checkAction(player, action, cardList, nowDisCard)) {
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
        setBeRemoveCard(nowDisCard);

        if (cardList.size() == 1) {
            // 如果是一张牌说明已经在出的牌里面了
            List<PaohzCard> list = player.getSameCards(nowDisCard);
            cardList.addAll(list);
        }

        if (cardList.size() != 4) {
            return;
        }

        // 检测是否能提
        boolean isFristDisCard = player.isFristDisCard();
        PaohuziCheckCardBean checkAutoDis = checkAutoDis(player, false);
        if (checkAutoDis != null) {
            playAutoDisCard(checkAutoDis, true);
        }
        player.changeAction(PaohuziConstant.ACTION_COUNT_INDEX_PAO, 1);
        addPlayLog(player.getSeat(), action + "", PaohuziTool.implodePhz(cardList, ","));
        player.disCard(action, cardList);
        clearAction();
        setAutoDisBean(null);

        if (!isHu && !isPassHu && isMoFlag()) {
            PaohuziCheckCardBean checkCard = player.checkPaoHu();
            checkPaohuziCheckCard(checkCard);
        }

        // 是否能出牌
        if (!isHu) {
            boolean disCard = setOutCardPlayer(player, action, isFristDisCard, false);
            sendActionMsg(player, action, cardList, PaohzDisAction.action_type_action, isZaiPao, !disCard);
            if (!disCard) {
                if (PaohuziConstant.isAutoMo) {
                    checkMo();
                }
                
                if(!firstCard){
            		player.setTingFlag(0);
            	}
            }
        } else {
            sendActionMsg(player, action, cardList, PaohzDisAction.action_type_action, isZaiPao, false);
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

    private void relieveFangZhao(long userId) {
        for (Player player : seatMap.values()) {
            player.writeComMessage(WebSocketMsgType.res_code_phz_fangzhao, userId + "", 0 + "");
        }
    }

    /**
     * 出牌
     */
    private void disCard(CdePaohuziPlayer player, List<PaohzCard> cardList, int action) {
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

        PaohuziHandCard cardBean = player.getPaohuziHandCard();
        if (!cardBean.isCanoperateCard(cardList.get(0))) {
            player.writeComMessage(WebSocketMsgType.res_code_phz_dis_err, cardList.get(0).getId());
            player.writeErrMsg("该牌不能单出:" + cardList);
            LogUtil.e("该牌不能单出:" + cardList);
            return;
        }

        // 判断是否为放招
        boolean paoFlag = gameModel.getSpecialPlay().isFangZhao() && isFangZhao(player, cardList.get(0));

        //吃边打边校验
        if (gameModel.getSpecialPlay().isEatSideDozenEdge() && cardList.size() == 1) {
            if (player.getForbidChiCardIds().contains(cardList.get(0).getId())) {
                LogUtil.e("吃边打边模式下，该回合不能出此牌:" + cardList);
                return;
            }
            if (player.getLimitChiCardIds().contains(cardList.get(0).getId())) {
                player.setCbdbNum(player.getDisNum());
                if (paoFlag) {
                    if (player.isAutoPlay()) {
                        player.setFangZhao(1);
                        player.setOnlyHht(1);
                        for (Player playerTemp : getSeatMap().values()) {
                            playerTemp.writeComMessage(WebSocketMsgType.res_code_phz_fangzhao, player.getUserId() + "", 1 + "");
                        }
                    } else if (player.getOnlyHht() == 0) {
                        player.writeComMessage(WebSocketMsgType.res_code_lyzp_cbdbfz, cardList.get(0).getId());
                        return;
                    }
                } else {
                    if (player.isAutoPlay()) {
                        player.setOnlyHht(1);
                    } else if (player.getOnlyHht() == 0) {
                        player.writeComMessage(WebSocketMsgType.res_code_lyzp_cbdb_hht, cardList.get(0).getId());
                        return;
                    }
                }
            }
        }

        // 判断是否为放招
        if (paoFlag) {
            if (player.isAutoPlay()) {//托管自动放招
                player.setFangZhao(1);
                for (Player playerTemp : getSeatMap().values()) {
                    playerTemp.writeComMessage(WebSocketMsgType.res_code_phz_fangzhao, player.getUserId() + "", 1 + "");
                }
            } else if (!player.isFangZhao() && !player.isRobot()) {
                player.writeComMessage(WebSocketMsgType.res_code_phz_dis_err, cardList.get(0).getId());
                player.writeComMessage(WebSocketMsgType.res_com_code_fangzhao, cardList.get(0).getId());
                return;
            }
        }

        
        
       
       
        // 地胡 出牌后手牌变动, 不能造成地胡
        boolean canDiHu = player.isFristDisCard();

        addPlayLog(player.getSeat(), action + "", PaohuziTool.implodePhz(cardList, ","));

        
        // 检查闲家提
        checkFreePlayerTi(player, action);
        player.disCard(action, cardList);
        setMoFlag(0);
        markMoSeat(player.getSeat(), action);
        clearMoSeatPair();
        setToPlayCardFlag(0); // 应该要打牌的flag
        setDisCardSeat(player.getSeat());
        setNowDisCardIds(cardList);
        setNowDisCardSeat(getNextDisCardSeat());
        //系统自动出牌,检测其他玩家可做的操作
        checkDisAction(player, action, cardList.get(0), false);
        if(isFirstCard()){
       	 boolean ting =  PaohuziTool.getTingZps(player);
            if(ting){
           	 player.setTingFlag(1);
            }
       }else{
       	player.setTingFlag(0);
       }
        setFirstCard(false);
        //检测点炮必胡
        if (igniteMustHu()) {
            return;
        }

        //发送所有玩家动作
        sendActionMsg(player, action, cardList, PaohzDisAction.action_type_dis);
        // if (autoDisCard != null) {
        // // 系统自动出牌
        // playAutoDisCard(autoDisCard);
        // } else {
        checkAutoMo();
        // }
    }

    private void checkAutoMo() {
        if (isTest()) {
            checkMo();
        }
    }

    private void tiLong(CdePaohuziPlayer player) {
        boolean isTiLong = false;
        List<PaohzCard> cardList = new ArrayList<>();
        while (player.getOweCardCount() < -1 - (player.getTi().size() / 4 - 1)) {
            if (!isTiLong) {
                isTiLong = true;
                removeAction(player.getSeat());
            }
            PaohzCard card = null;

//            if (card == null) {
//                card = getNextCard();
//            }
//
//            player.tiLong(card);
//            cardList.add(card);

            addPlayLog(player.getSeat(), PaohzDisAction.action_buPai + "", (card == null ? 0 : card.getId()) + "");
            StringBuilder sb = new StringBuilder("CdePhz");
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
            sendActionMsg(player, PaohzDisAction.action_tilong, cardList, PaohzDisAction.action_type_action, false, false);

            PaohuziCheckCardBean checkCard = player.checkCard(null, true, true, false);
            if (checkPaohuziCheckCard(checkCard)) {
                playAutoDisCard(checkCard);
                if (player.getSeat() != lastWinSeat && checkCard.isTi()) {
                    player.setOweCardCount(player.getOweCardCount() - 1);
                }
                tiLong(player);
            }
        }
    }

    public void checkFreePlayerTi(CdePaohuziPlayer player, int action) {
        if (player.getSeat() == lastWinSeat && player.isFristDisCard() && action != PaohzDisAction.action_ti) {
            for (int seat : getSeatMap().keySet()) {
                if (lastWinSeat == seat) {
                    continue;
                }
                CdePaohuziPlayer nowPlayer = seatMap.get(seat);
                PaohuziCheckCardBean checkCard = nowPlayer.checkCard(null, true, true, false);
                if (checkPaohuziCheckCard(checkCard)) {
                    playAutoDisCard(checkCard);
                    if (!checkCard.isTi() && nowPlayer.isFristDisCard()) {
                        nowPlayer.setFristDisCard(false);
                    }
                    tiLong(nowPlayer);
                    /*-- 暂时屏蔽两条龙的处理
					boolean needBuPai = false;
					if (checkCard.isTi()) {
						PaohuziHandCard cardBean = nowPlayer.getPaohuziHandCard();
						PaohzCardIndexArr valArr = cardBean.getGroupByNumberToArray();
						PaohuziIndex index3 = valArr.getPaohzCardIndex(3);
						if (index3 != null && index3.getLength() >= 2) {
							needBuPai = true;
						}
					}
					playAutoDisCard(checkCard);
					if (nowPlayer.isFristDisCard()) {
						nowPlayer.setFristDisCard(false);
					}

					if (needBuPai) {
						PaohzCard buPai = leftCards.remove(0);
						for (XtPaohuziPlayer tempPlayer : seatMap.values()) {
							if (seat == tempPlayer.getSeat()) {
								tempPlayer.getHandPhzs().add(buPai);
								System.out.println("----------------------------------谁补:" + tempPlayer.getName() + "  什么牌:" + buPai.getId() + "  醒子数量:" + leftCards.size());
							}
							tempPlayer.writeComMessage(WebSocketMsgType.res_com_code_phzbupai, seat, buPai.getId(), leftCards.size());
						}
					}
					 */

                }
                checkSendActionMsg();
            }
        }
    }

    
    public  void logTing(CdePaohuziPlayer player,int local){
        StringBuilder sb = new StringBuilder("CdePhzTing");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append(player.getTingFlag());
        sb.append("|").append(local);
        LogUtil.msgLog.info(sb.toString());
    }
    
    
    /**
     * 碰
     */
    private void peng(CdePaohuziPlayer player, List<PaohzCard> cardList, PaohzCard nowDisCard, int action) {
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
        if (!cardList.contains(nowDisCard)) {
            cardList.add(0, nowDisCard);
        }
        setBeRemoveCard(nowDisCard);

        boolean isFristDisCard = player.isFristDisCard();
        PaohuziCheckCardBean checkAutoDis = checkAutoDis(player, false);
        if (checkAutoDis != null) {
            playAutoDisCard(checkAutoDis, true);
        }
        getDisPlayer().removeOutPais(nowDisCard);
        addPlayLog(player.getSeat(), action + "", PaohuziTool.implodePhz(cardList, ","));
        player.disCard(action, cardList);
        clearAction();

        boolean disCard = setOutCardPlayer(player, action, isFristDisCard, false);
        sendActionMsg(player, action, cardList, PaohzDisAction.action_type_action);
        if (!disCard) {
            // checkMo();
        }

        // 碰的情况,把所有玩家的过牌去掉
        if (isMoFlag()) {
            for (CdePaohuziPlayer seatPlayer : seatMap.values()) {
                if (seatPlayer.getSeat() == player.getSeat()) {
                    continue;
                }
                seatPlayer.removePassChi(nowDisCard.getVal());
            }
        }
    }

    /**
     * 过
     */
    private void pass(CdePaohuziPlayer player, List<PaohzCard> cardList, PaohzCard nowDisCard, int action) {
        if (!actionSeatMap.containsKey(player.getSeat())) {
            // player.writeErrMsg("该玩家没有找到可以过的动作");
            return;
        }
        List<Integer> actionList = actionSeatMap.get(player.getSeat());
        List<Integer> list = PaohzDisAction.parseToDisActionList(actionList);
        // 栽，提，跑是不可以过的
        if (list.contains(PaohzDisAction.action_zai) || list.contains(PaohzDisAction.action_ti) || list.contains(PaohzDisAction.action_pao) || list.contains(PaohzDisAction.action_chouzai)) {
            return;
        }
        // 如果没有吃，碰，胡也是不可以过的
        if (!list.contains(PaohzDisAction.action_chi) && !list.contains(PaohzDisAction.action_peng) && !list.contains(PaohzDisAction.action_hu)) {
            return;
        }

        // 可以胡牌，然后点了过
        boolean isPassHu = actionList.get(0) == 1;
        if (actionList.get(0) == 1 && player.getHandPhzs().isEmpty()) {
            player.writeErrMsg("手上已经没有牌了");
            return;
        }

        if(action==PaohzDisAction.action_pass){
            int logId = 0;
            if(paoHu==1){
                logId=0;
            }else {
            	if(nowDisCard!=null){
            		logId = nowDisCard.getId();
            	}
            }
            addPlayLog(player.getSeat(), PaohzDisAction.action_guo + "",logId+"");
            setPaoHu(0);
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
            playAutoDisCard(autoDisBean);
        } else {
            PaohuziCheckCardBean checkCard = player.checkCard(nowDisCard, isSelfMo(player), isPassHu, false, false, true);
            checkCard.setPassHu(isPassHu);
            boolean check = checkPaohuziCheckCard(checkCard);
            markMoSeat(player.getSeat(), action);
            sendActionMsg(player, action, cardList, PaohzDisAction.action_type_action);
            if (check) {
                playAutoDisCard(checkCard, true);
            } else {
                if (PaohuziConstant.isAutoMo) {
                    checkMo();
                } else {
                    if (isTest()) {
                        checkMo();
                    }
                }
            }
            refreshTempAction(player);
        }
        
        if (this.leftCards.size() == 0 && !isHasSpecialAction()) {
            calcOver();
        }

    }

    /**
     * 吃
     */
    private void chi(CdePaohuziPlayer player, List<PaohzCard> cardList, PaohzCard nowDisCard, int action) {
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

//        if (cardList.size() > 3) {
//            PaohuziHandCard card = player.getPaohuziHandCard();
//            if (card.getOperateCards().size() <= cardList.size()) {
//                player.writeErrMsg("您手上没有剩余的牌可打，不能吃");
//                return;
//            }
//        }

        if (!checkAction(player, action, cardList, nowDisCard)) {
            player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip);
            // 能吃能碰的情况下
            if (actionList.get(1) == 1) {
                actionList.set(1, 0);
                // 选择了吃，那不能碰了
                player.pass(PaohzDisAction.action_peng, nowDisCard.getVal());
//                addAction(player.getSeat(), actionList);
                // 更新前台数据
//                sendPlayerActionMsg(player);
            }
            // player.writeErrMsg(LangMsgEnum.code_29);
            return;
        }

        if (PaohuziTool.isPaohuziRepeat(cardList)) {
            player.writeErrMsg("不能吃");
            return;
        }

        boolean isFristDisCard = player.isFristDisCard();
        PaohuziCheckCardBean checkAutoDis = checkAutoDis(player, false);
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
        addPlayLog(player.getSeat(), action + "", PaohuziTool.implodePhz(cardList, ","));
        player.disCard(action, cardList);
        clearAction();

        //下个出牌玩家
        boolean disCard = setOutCardPlayer(player, action, isFristDisCard, false);
        sendActionMsg(player, action, cardList, PaohzDisAction.action_type_action);

        //吃边打边
        if (gameModel.getSpecialPlay().isEatSideDozenEdge()) {
            player.chiBianDaBian(nowDisCard, cardList);
            List<Integer> forbidChiCardIds = player.getForbidChiCardIds();
            if (forbidChiCardIds != null && forbidChiCardIds.size() != 0) {
                ComMsg.ComRes msg = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_lyzp_cbdb,
                        forbidChiCardIds.toArray()).build();
                player.writeSocket(msg);
            }
        }

        if (!disCard) {
            if (PaohuziConstant.isAutoMo) {
                checkMo();
            }
        }

    }

    /**
     * @param cardIds 打出的牌号
     * @param action  操作指令
     * @return
     * @description 玩家操作
     * @author Guang.OuYang
     * @date 2019/9/2
     */
    public synchronized void play(CdePaohuziPlayer player, List<Integer> cardIds, int action, boolean moPai, boolean isHu, boolean isPassHu) {
        // 检查play状态
        if (state != table_state.play || player.getSeat() == shuXingSeat) {
            return;
        }

        PaohzCard nowDisCard = null;
        List<PaohzCard> cardList = null;
        // 非摸牌非过牌要检查能否出牌,并将要出的牌id集合变成跑胡子牌
        if (action != PaohzDisAction.action_mo) {
            if (nowDisCardIds != null && nowDisCardIds.size() == 1) {
                nowDisCard = nowDisCardIds.get(0);
            }
            if (action != PaohzDisAction.action_pass) {
                if (!player.isCanDisCard(cardIds, nowDisCard)) {
                    return;
                }
            }
            if (cardIds != null && !cardIds.isEmpty()) {
                cardList = PaohuziTool.toPhzCards(cardIds);
            }
        }

        if (action != PaohzDisAction.action_mo) {
            StringBuilder sb = new StringBuilder("CdePhz");
            sb.append("|").append(getId());
            sb.append("|").append(getPlayBureau());
            sb.append("|").append(player.getUserId());
            sb.append("|").append(player.getSeat());
            sb.append("|").append(player.isAutoPlay() ? 1 : 0);
            sb.append("|").append(PaohzDisAction.getActionName(action));
            sb.append("|").append(cardList);
            sb.append("|").append(nowDisCard);
            if (actionSeatMap.containsKey(player.getSeat())) {
                sb.append("|").append(PaohuziCheckCardBean.actionListToString(actionSeatMap.get(player.getSeat())));
            }
            LogUtil.msgLog.info(sb.toString());
        }
        // //////////////////////////////////////////////////////

        if (action == PaohzDisAction.action_ti) {
            if (cardList.size() > 4) {
                // 有多个提
                PaohzCardIndexArr arr = PaohuziTool.getMax(cardList);
                PaohuziIndex index = arr.getPaohzCardIndex(3);
                for (List<PaohzCard> tiCards : index.getPaohzValMap().values()) {
                    ti(player, tiCards, nowDisCard, action, moPai);
                }
            } else {
                ti(player, cardList, nowDisCard, action, moPai);
            }
        } else if (action == PaohzDisAction.action_hu) {
            hu(player, cardList, action, nowDisCard);
        } else if (action == PaohzDisAction.action_peng) {
            peng(player, cardList, nowDisCard, action);
        } else if (action == PaohzDisAction.action_chi) {
            chi(player, cardList, nowDisCard, action);
        } else if (action == PaohzDisAction.action_pass) {
            pass(player, cardList, nowDisCard, action);
        } else if (action == PaohzDisAction.action_pao) {
            pao(player, cardList, nowDisCard, action, isHu, isPassHu);
        } else if (action == PaohzDisAction.action_zai || action == PaohzDisAction.action_chouzai) {
            zai(player, cardList, nowDisCard, action);
        } else if (action == PaohzDisAction.action_mo) {
            if (isTest()) {
                return;
            }
            if (checkMoMark != null) {
                int cAction = cardIds.get(0);
                if (checkMoMark.getId() == player.getSeat() && checkMoMark.getValue() == cAction) {
                    checkMo();
                } /*else {
					// System.out.println("发现请求-->" + player.getName() +
					// " seat:" + player.getSeat() + "-" + checkMoMark.getId() +
					// " action:" + cAction + "- " + checkMoMark.getValue());
				}*/
            }

        } else {
            disCard(player, cardList, action);
        }
        if (!moPai && !isHu) {
            // 摸牌的时候提不需要做操作
            robotDealAction();
        }

    }

    /**
     * @param player
     * @param action 指令
     * @param action 是否能胡牌
     * @return boolean
     * @description 出牌
     * @author Guang.OuYang
     * @date 2019/9/2
     */
    private boolean setOutCardPlayer(CdePaohuziPlayer player, int action, boolean isHu) {
        return setOutCardPlayer(player, action, false, isHu);
    }

    /**
     * 设置要出牌的玩家
     */
    private boolean setOutCardPlayer(CdePaohuziPlayer player, int action, boolean isFirstDis, boolean isHu) {
        //牌堆已经没牌了
        if (this.leftCards.isEmpty()) {
            //没有胡牌计算
            if (!isHu) {
                calcOver();
            }
            return false;
        }

        boolean canDisCard = true;
        if (player.getHandPhzs().isEmpty()) {
            canDisCard = false;
        } else if (player.getOperateCards().isEmpty()) {
            canDisCard = false;
        }
        if (canDisCard && ((player.getSeat() == lastWinSeat && isFirstDis) || player.isNeedDisCard(action))) {
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
     * 自动出牌
     */
    private boolean checkAction(CdePaohuziPlayer player, int action, List<PaohzCard> cardList, PaohzCard nowDisCard) {
        // 优先度为胡杠补碰吃
        boolean canPlay = true;
        List<Integer> stopActionList = PaohzDisAction.findPriorityAction(action);
        for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
            if (player.getSeat() != entry.getKey()) {
                // 别人
                boolean can = PaohzDisAction.canDis(stopActionList, entry.getValue());
                if (!can) {
                    canPlay = false;
                }
                List<Integer> disActionList = PaohuziDisAction.parseToDisActionList(entry.getValue());
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

        // 玩家都已选择自己的临时操作后  选取优先级最高
        if (tempActionMap.size() > 0 && tempActionMap.size() == actionSeatMap.size()) {
            int maxAction = -1;
            int maxSeat = 0;
            Map<Integer, Integer> prioritySeats = new HashMap<>();
            int maxActionSize = 0;
            for (TempAction temp : tempActionMap.values()) {
                if (maxAction == -1 || PaohzDisAction.findPriorityAction(maxAction).contains(temp.getAction())) {
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
            CdePaohuziPlayer tempPlayer = seatMap.get(maxSeat);
            List<PaohzCard> tempCardList = tempActionMap.get(maxSeat).getCardList();
            for (int removeSeat : prioritySeats.keySet()) {
                if (removeSeat != maxSeat) {
                    removeAction(removeSeat);
                }
            }
            clearTempAction();
            // 系统选取优先级最高操作
            play(tempPlayer, PaohuziTool.toPhzCardIds(tempCardList), maxAction);
        } else if (tempActionMap.size() + 1 == actionSeatMap.size()) {
            // 剩下可以跑的人
            for (int s : actionSeatMap.keySet()) {
                if (!tempActionMap.containsKey(s)) {
                    List<Integer> list = actionSeatMap.get(s);
                    boolean isPao = list.get(5) == 1;
                    for (int i = 0; i < list.size(); i++) {
                        if (i != 5 && list.get(i) == 1) {
                            isPao = false;
                        }
                    }
                    if (isPao) {
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
    private void refreshTempAction(CdePaohuziPlayer player) {
        tempActionMap.remove(player.getSeat());
        Map<Integer, Integer> prioritySeats = new HashMap<>();//各位置优先操作
        for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
            int seat = entry.getKey();
            List<Integer> actionList = entry.getValue();
            List<Integer> list = PaohuziDisAction.parseToDisActionList(actionList);
            int priorityAction = PaohzDisAction.getMaxPriorityAction(list);
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
        Iterator<TempAction> iterator = tempActionMap.values().iterator();
        while (iterator.hasNext()) {
            TempAction tempAction = iterator.next();
            if (tempAction.getSeat() == maxPrioritySeat) {
                int action = tempAction.getAction();
                List<PaohzCard> tempCardList = tempAction.getCardList();
                CdePaohuziPlayer tempPlayer = seatMap.get(tempAction.getSeat());
                iterator.remove();
                // 系统选取优先级最高操作
                play(tempPlayer, PaohuziTool.toPhzCardIds(tempCardList), action);
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
    private CdePaohuziPlayer getDisPlayer() {
        return seatMap.get(disCardSeat);
    }

    @Override
    public int isCanPlay() {
        if (getPlayerCount() < getMaxPlayerCount()) {
            return 1;
        }
        // for (XtPaohuziPlayer player : seatMap.values()) {
        // if (player.getIsEntryTable() != PdkConstants.table_online) {
        // // 通知其他人离线
        // broadIsOnlineMsg(player, player.getIsEntryTable());
        // return 2;
        // }
        // }
        return 0;
    }

	public static void main(String[] args) {
    	int k = 30;
    	String e = "+";
    	int v = 90;
		System.out.println((((100 + k) * 10) + (e.equals("*") ? 1 : 0)) * 100 + (v + 0));
	}
//    public static void main(String[] args) throws Exception {
//
//        CdePaohuziPlayer xtPaohuziPlayer = new CdePaohuziPlayer();
//        CdePaohuziTable table;
//        xtPaohuziPlayer.setTable(table = new CdePaohuziTable());
//        xtPaohuziPlayer.setPlayingTableId(1000);
//        xtPaohuziPlayer.setName("1111");
//        xtPaohuziPlayer.setFlatId("11");
//
//        Field pao = CdePaohuziPlayer.class.getDeclaredField("pao");
//        pao.setAccessible(true);
//        pao.set(xtPaohuziPlayer, new ArrayList<>(Arrays.asList(PaohzCard.phz2, PaohzCard.phz12, PaohzCard.phz22, PaohzCard.phz32)));
//
//        table.setId(xtPaohuziPlayer.getPlayingTableId());
//        table.setMoFlag(1);
//        table.setMoSeat(1);
//        table.setLastWinSeat(1);
//        Field nowDisCardSeat = CdePaohuziTable.class.getSuperclass().getDeclaredField("nowDisCardSeat");
//        nowDisCardSeat.setAccessible(true);
//        nowDisCardSeat.set(table, 1);
//
//        table.setLeftCards(new ArrayList<>(Arrays.asList(PaohzCard.phz3)));
//
//        table.getSeatMap().put(1, xtPaohuziPlayer);
//
//        xtPaohuziPlayer.setHuxi(20);
//        xtPaohuziPlayer.dealHandPais(Arrays.asList(
//                PaohzCard.phz1, PaohzCard.phz11, PaohzCard.phz21,
////                PaohzCard.phz2,PaohzCard.phz22,PaohzCard.phz32,
//                PaohzCard.phz3
//        ));
//
//        table.checkMo();
////        PaohuziCheckCardBean paohuziCheckCardBean = xtPaohuziPlayer.checkCard(PaohzCard.phz13, true, false, false, false, false);
////        System.out.println(paohuziCheckCardBean.buildActionList());
////        System.out.println(paohuziCheckCardBean.isHu());
//    }

    /**
     * @param
     * @return
     * @description 摸牌, 系统发牌
     * @author Guang.OuYang
     * @date 2019/9/3
     */
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

        // 下一个要摸牌的人
        CdePaohuziPlayer player = seatMap.get(nowDisCardSeat);

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

        // PaohzCard card = PaohzCard.getPaohzCard(59);
        // PaohzCard card = getNextCard();
        PaohzCard card = null;
        if (player.getFlatId().startsWith("vkscz2855914")) {
            card = getNextCard(102);
            // if (card == null) {
            // card = PaohzCard.getPaohzCard(61);
            // }
            if (card == null) {
                card = getNextCard();
            }
        } else {
            if (GameServerConfig.isDebug() && !player.isRobot()) {
                if (zpMap.containsKey(player.getUserId()) && zpMap.get(player.getUserId()) > 0) {
                    List<PaohzCard> cardList = PaohuziTool.findPhzByVal(getLeftCards(), zpMap.get(player.getUserId()));
                    if (cardList != null && cardList.size() > 0) {
                        zpMap.remove(player.getUserId());
                        card = cardList.get(0);
                        getLeftCards().remove(card);
                    }
                }
            }
            if (card == null) {
                card = getNextCard();
            }
        }

        addPlayLog(player.getSeat(), PaohzDisAction.action_mo + "", (card == null ? 0 : card.getId()) + "");

        StringBuilder sb = new StringBuilder("CdePhz");
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

            setMoFlag(1);
            //当前摸牌标志位
            setMoSeat(player.getSeat());
            //
            markMoSeat(card, player.getSeat());
            //摸牌加入出牌队列
            player.moCard(card);
            //当前出牌人的座位
            setDisCardSeat(player.getSeat());
            //这是摸牌逻辑, 已经不再是首次出牌
            setFirstCard(false);
            //当前系统出的牌
            setNowDisCardIds(new ArrayList<>(Arrays.asList(card)));
            //当前要出牌的座位, disCardSeat的下家
            setNowDisCardSeat(getNextDisCardSeat());

            PaohuziCheckCardBean autoDisCard = null;
            //检查所有人对该卡的组合
            for (Entry<Integer, CdePaohuziPlayer> entry : seatMap.entrySet()) {
                PaohuziCheckCardBean checkCard = entry.getValue().checkCard(card, entry.getKey() == player.getSeat(), false);
                if (checkPaohuziCheckCard(checkCard)) {
                    autoDisCard = checkCard;
                }

            }

            markMoSeat(player.getSeat(), PaohzDisAction.action_mo);
            if (autoDisCard != null && autoDisCard.getAutoAction() == PaohzDisAction.action_zai) {
                sendMoMsg(player, PaohzDisAction.action_mo, new ArrayList<>(Arrays.asList(card)), PaohzDisAction.action_type_mo);
            } else {
                sendActionMsg(player, PaohzDisAction.action_mo, new ArrayList<>(Arrays.asList(card)), PaohzDisAction.action_type_mo);
            }

            if (autoDisBean != null) {
                playAutoDisCard(autoDisBean);
            }

            if (this.leftCards != null && this.leftCards.size() == 0 && !isHasSpecialAction()) {
                calcOver();
                return;
            }

//			if (PaohuziConstant.isAutoMo) {
            // if (actionSeatMap.isEmpty()) {
            // markMoSeat(player.getSeat(), PaohzDisAction.action_mo);
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
     * 其他玩家检测,打出的牌能做出的操作
     *
     * @return 是否有系统帮助自动出牌
     */
    private PaohuziCheckCardBean checkDisAction(CdePaohuziPlayer player, int action, PaohzCard disCard, boolean isFirstCard) {
        PaohuziCheckCardBean autoDisCheck = null;
        for (Entry<Integer, CdePaohuziPlayer> entry : seatMap.entrySet()) {
            //过滤当前出牌玩家
            if (entry.getKey() == player.getSeat()) {
                continue;
            }

//            PaohuziCheckCardBean checkCard = entry.getValue().checkCard(disCard, false, isFirstCard);
            //检查牌, 其他家玩家可做的操作
            PaohuziCheckCardBean checkCard = entry.getValue().checkCard(disCard, false, !isFirstCard, false, isFirstCard, false);
            //发送提胡, 跑胡动作
            if(checkCard.isHu()&&!isFirstCard){
            	checkCard.setHu(false);
            	checkCard.buildActionList();
            }
            boolean check = checkPaohuziCheckCard(checkCard);
            if (check) {
                autoDisCheck = checkCard;
            }
        }
        return autoDisCheck;
    }

    /**
     * @param
     * @return
     * @description 放招: 在其他人有偎的情况下,依然打出这张牌, 送他人跑
     * @author Guang.OuYang
     * @date 2019/9/10
     */
    private boolean isFangZhao(CdePaohuziPlayer player, PaohzCard disCard) {

        for (Entry<Integer, CdePaohuziPlayer> entry : seatMap.entrySet()) {
            if (entry.getKey() == player.getSeat()) {
                continue;
            }

            boolean flag = entry.getValue().canFangZhao(disCard);
            if (flag) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查自动提
     */
    public PaohuziCheckCardBean checkAutoDis(CdePaohuziPlayer player, boolean isMoPaiIng) {
        PaohuziCheckCardBean checkCard = player.checkTi();
        checkCard.setMoPaiIng(isMoPaiIng);
        boolean check = checkPaohuziCheckCard(checkCard);
        if (check) {
            return checkCard;
        } else {
            return null;
        }
    }

    /**
     * @param
     * @return
     * @description 提胡, 跑胡
     * @author Guang.OuYang
     * @date 2019/9/5
     */
    public boolean checkPaohuziCheckCard(PaohuziCheckCardBean checkCard) {
        List<Integer> list = checkCard.getActionList();
        if (list == null || list.isEmpty()) {
            return false;
        }

        addAction(checkCard.getSeat(), list);
        List<PaohzCard> autoDisList = checkCard.getAutoDisList();
        if (autoDisList != null) {
            // 不能胡就自动出牌
            if (!checkCard.isHu()) {
                setAutoDisBean(checkCard);
                return true;
            }
        }
        return false;

    }

    public void setAutoDisBean(PaohuziCheckCardBean autoDisBean) {
//    	if( ){
//    		this.autoDisBean = autoDisBean;
//    	}
    	if(autoDisBean==null||autoDisBean.isTi()||autoDisBean.isZai()||autoDisBean.isPao()||autoDisBean.isChouZai()){
    		this.autoDisBean = autoDisBean;
    	}
        changeExtend();
    }

    private void addAction(int seat, List<Integer> actionList) {
        actionSeatMap.put(seat, actionList);
        addPlayLog(seat, PaohzDisAction.action_hasaction + "", StringUtil.implode(actionList));
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

    /**
     * 发送所有玩家动作msg
     *
     * @param player
     * @param action
     * @param cards
     * @param actType
     */
    private void sendActionMsg(CdePaohuziPlayer player, int action, List<PaohzCard> cards, int actType) {
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
    private void sendMoMsg(CdePaohuziPlayer player, int action, List<PaohzCard> cards, int actType) {
        PlayPaohuziRes.Builder builder = PlayPaohuziRes.newBuilder();
        builder.setAction(action);
        builder.setUserId(player.getUserId() + "");
        builder.setSeat(player.getSeat());
        builder.setHuxi(player.getOutHuxi() + player.getZaiHuxi());
        // builder.setNextSeat(nowDisCardSeat);
        setNextSeatMsg(builder);
        builder.setRemain(leftCards.size());
        builder.addAllPhzIds(PaohuziTool.toPhzCardIds(cards));
        builder.setActType(actType);
        sendMoMsgBySelfAction(builder, player.getSeat());
    }

    /**
     * 发送该玩家动作msg
     */
    private void sendPlayerActionMsg(CdePaohuziPlayer player) {
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return;
        }
        PlayPaohuziRes.Builder builder = PlayPaohuziRes.newBuilder();
        builder.setAction(PaohzDisAction.action_refreshaction);
        builder.setUserId(player.getUserId() + "");
        builder.setSeat(player.getSeat());
        builder.setHuxi(player.getOutHuxi() + player.getZaiHuxi());
        // builder.setNextSeat(nowDisCardSeat);
        setNextSeatMsg(builder);
        if (leftCards != null) {
            builder.setRemain(leftCards.size());

        }
        // builder.addAllPhzIds(PaohuziTool.toPhzCardIds(nowDisCardIds));
        builder.setActType(0);
        KeyValuePair<Boolean, Integer> zaiKeyValue = getZaiOrTiKeyValue();
        List<Integer> actionList = getSendSelfAction(zaiKeyValue, player.getSeat(), actionSeatMap.get(player.getSeat()));
        if (actionList != null) {
            builder.addAllSelfAct(actionList);
        }
        player.writeSocket(builder.build());

        if (player.getSeat() == lastWinSeat && shuXingSeat > 0) {
            CdePaohuziPlayer paohuziPlayer = seatMap.get(shuXingSeat);
            paohuziPlayer.writeSocket(builder.build());
        }
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
    private void sendActionMsg(CdePaohuziPlayer player, int action, List<PaohzCard> cards, int actType, boolean isZaiPao, boolean isChongPao) {
        PlayPaohuziRes.Builder builder = PlayPaohuziRes.newBuilder();
        builder.setAction(action);
        builder.setUserId(player.getUserId() + "");
        builder.setSeat(player.getSeat());
        builder.setHuxi(player.getOutHuxi() + player.getZaiHuxi());
        setNextSeatMsg(builder);
        if (leftCards != null) {
            builder.setRemain(leftCards.size());
        }
        builder.addAllPhzIds(PaohuziTool.toPhzCardIds(cards));
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

    /**
     * 目前的动作中是否有人有栽或者是提
     *
     * @return
     */
    private KeyValuePair<Boolean, Integer> getZaiKeyValue() {
        KeyValuePair<Boolean, Integer> keyValue = new KeyValuePair<>();
        boolean isHasZaiOrTi = false;
        int zaiSeat = 0;
        for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
            if (entry.getValue().get(2) == 1) {
                isHasZaiOrTi = true;
                zaiSeat = entry.getKey();
                break;
            }
        }
        keyValue.setId(isHasZaiOrTi);
        keyValue.setValue(zaiSeat);
        return keyValue;
    }

    /**
     * 目前的动作中是否有人有栽或者是提
     *
     * @return
     */
    private KeyValuePair<Boolean, Integer> getTiKeyValue() {
        KeyValuePair<Boolean, Integer> keyValue = new KeyValuePair<>();
        boolean isHasZaiOrTi = false;
        int zaiSeat = 0;
        for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
            if (entry.getValue().get(2) == 1) {
                isHasZaiOrTi = true;
                zaiSeat = entry.getKey();
                break;
            }
        }
        keyValue.setId(isHasZaiOrTi);
        keyValue.setValue(zaiSeat);
        return keyValue;
    }

    /**
     * @param zaiOrTi    k->是否有人偎or提 v->操作座位号,
     * @param seat       庄家座位
     * @param actionList 当前座位的操作 0胡 1碰 2栽 3提 4吃 5跑 6臭栽
     * @return
     * @description
     * @author Guang.OuYang
     * @date 2019/9/10
     */
    private List<Integer> getSendSelfAction(KeyValuePair<Boolean, Integer> zaiOrTi, int seat, List<Integer> actionList) {
        boolean isHasZaiOrTi = zaiOrTi.getId();
        int zaiSeat = zaiOrTi.getValue();
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
        KeyValuePair<Boolean, Integer> zaiValue = getZaiKeyValue();
        CdePaohuziPlayer winPlayer = seatMap.get(lastWinSeat);
        CdePaohuziPlayer nowDis = seatMap.get(builder.getSeat());
        for (CdePaohuziPlayer player : seatMap.values()) {
            PlayPaohuziRes.Builder copy = builder.clone();
            if (player.getSeat() != seat) {
                // copy.clearPhzIds();
                // copy.addPhzIds(0);
                if (seat == lastWinSeat && player.getSeat() == shuXingSeat) {
                    copy.setHuxi(winPlayer.getOutHuxi() + winPlayer.getZaiHuxi());
                }
            } else {
                copy.setHuxi(player.getOutHuxi() + player.getZaiHuxi());
            }
            if (actionSeatMap.containsKey(player.getSeat())) {
                List<Integer> actionList = getSendSelfAction(zaiKeyValue, player.getSeat(), actionSeatMap.get(player.getSeat()));
                if (actionList != null) {
                    copy.addAllSelfAct(actionList);
                }
            } else if (seat == lastWinSeat && shuXingSeat == player.getSeat() && actionSeatMap.containsKey(winPlayer.getSeat())) {
                List<Integer> actionList = getSendSelfAction(zaiKeyValue, winPlayer.getSeat(), actionSeatMap.get(winPlayer.getSeat()));
                if (actionList != null) {
                    copy.addAllSelfAct(actionList);
                }
            }

            List<Integer> ids = null;
            //偎和提单独处理, 明偎盖3明1, 明提盖1明3, 起手提均为明, 暗偎暗提对手视角盖4张
            if (!gameModel.getSpecialPlay().isWeiWay() && !CollectionUtils.isEmpty(copy.getPhzIdsList()) && zaiValue.getId()) {
                // 需要替换成0
                ids = /*!player.isFristDisCard() && */copy.getSeat() != player.getSeat() ? copy.getPhzIdsList().stream().map(v -> 0).collect(Collectors.toList()) : PaohuziTool.toPhzCardZeroIds(copy.getPhzIdsList());
            }

            //暗偎屏蔽分数
            if (!gameModel.getSpecialPlay().isWeiWay() && player.getSeat() != copy.getSeat()) {
                copy.setHuxi(nowDis.getOutHuxi());
            }

            if (!CollectionUtils.isEmpty(ids)) {
                copy.clearPhzIds();
                copy.addAllPhzIds(ids);
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
        if (PaohzDisAction.action_type_dis == actType || PaohzDisAction.action_type_mo == actType) {
            for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
                if (1 == entry.getValue().get(5)) {
                    noShow = true;
                    paoSeat = entry.getKey();
                }
            }
        }

        CdePaohuziPlayer winPlayer = seatMap.get(lastWinSeat);
        CdePaohuziPlayer nowDis = seatMap.get(builder.getSeat());

        for (CdePaohuziPlayer player : seatMap.values()) {
            PlayPaohuziRes.Builder copy = builder.clone();
            if (copy.getSeat() == player.getSeat()) {
                copy.setHuxi(player.getOutHuxi() + player.getZaiHuxi());
                if (player.isAutoPlay() && copy.getActType() == PaohzDisAction.action_type_dis) {
                    copy.setActType(PaohzDisAction.action_type_autoplaydis);
                }
            } else if (copy.getSeat() == lastWinSeat && player.getSeat() == shuXingSeat) {
                copy.setHuxi(winPlayer.getOutHuxi() + winPlayer.getZaiHuxi());
                if (winPlayer.isAutoPlay() && copy.getActType() == PaohzDisAction.action_type_dis) {
                    copy.setActType(PaohzDisAction.action_type_autoplaydis);
                }
            }

            List<Integer> ids = copy.getPhzIdsList();
//            //偎和提单独处理, 明偎盖3明1, 明提盖1明3, 起手提均为明, 暗偎暗提对手视角盖4张
//            if (!gameModel.getSpecialPlay().isWeiWay() && !CollectionUtils.isEmpty(copy.getPhzIdsList()) && (copy.getAction() == PaohzDisAction.action_zai/* || copy.getAction() == PaohzDisAction.action_ti*/)) {
//                // 需要替换成0
////                ids = /*!player.isFristDisCard() && */ !gameModel.getSpecialPlay().isWeiWay() ? copy.getPhzIdsList().stream().map(v -> 0).collect(Collectors.toList()) : PaohuziTool.toPhzCardZeroIds(copy.getPhzIdsList());
//
//                //20191112暗偎交给前端做处理
////                if (copy.getAction() == PaohzDisAction.action_zai && player.getSeat() != copy.getSeat()) {
////                    ids = copy.getPhzIdsList().stream().map(v -> 0).collect(Collectors.toList());
////                }
//            }

//            //暗偎屏蔽分数
            if (!gameModel.getSpecialPlay().isWeiWay() && player.getSeat() != copy.getSeat()) {
                copy.setHuxi(nowDis.getOutHuxi());
            }
            
            
            //偎和提单独处理, 明偎盖3明1, 明提盖1明3, 起手提均为明, 暗偎暗提对手视角盖4张
//            if (!gameModel.getSpecialPlay().isWeiWay() && !CollectionUtils.isEmpty(copy.getPhzIdsList()) && copy.getAction() == PaohzDisAction.action_zai) {
//                // 需要替换成0
//            	if(copy.getSeat() != player.getSeat()){
//            		ids = /*!player.isFristDisCard() && */copy.getSeat() != player.getSeat() ? copy.getPhzIdsList().stream().map(v -> 0).collect(Collectors.toList()) : PaohuziTool.toPhzCardZeroIds(copy.getPhzIdsList());
//            	}
//            }


//            LogUtil.printDebug("玩家:{}看到玩家:{}胡息:{}",player.getName(),winPlayer.getName(),copy.getHuxi());
            if (!CollectionUtils.isEmpty(ids)) {
                copy.clearPhzIds();
                copy.addAllPhzIds(ids);
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

            } else if (player.getSeat() == shuXingSeat && actionSeatMap.containsKey(winPlayer.getSeat())) {
                List<Integer> actionList = getSendSelfAction(zaiKeyValue, winPlayer.getSeat(), actionSeatMap.get(winPlayer.getSeat()));
                if (actionList != null) {
                    // copy.addAllSelfAct(actionList);
                    if (noShow && paoSeat != winPlayer.getSeat()) {
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
                StringBuilder sb = new StringBuilder("CdePhz");
                sb.append("|").append(getId());
                sb.append("|").append(getPlayBureau());
                sb.append("|").append(player.getUserId());
                sb.append("|").append(player.getSeat());
                sb.append("|").append(player.isAutoPlay() ? 1 : 0);
                sb.append("|").append("actList");
                sb.append("|").append(PaohuziCheckCardBean.actionListToString(actionSeatMap.get(player.getSeat())));
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
        CdePaohuziPlayer disCSMajiangPlayer = seatMap.get(disCardSeat);
        PaohuziResTool.buildPlayRes(disBuilder, disCSMajiangPlayer, 0, null);
        disBuilder.setRemain(leftCards.size());
        disBuilder.setHuxi(disCSMajiangPlayer.getOutHuxi() + disCSMajiangPlayer.getZaiHuxi());
        // disBuilder.setNextSeat(nowDisCardSeat);
        setNextSeatMsg(disBuilder);
        for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
            PlayPaohuziRes.Builder copy = disBuilder.clone();
            List<Integer> actionList = entry.getValue();
            copy.addAllSelfAct(actionList);
            CdePaohuziPlayer seatPlayer = seatMap.get(entry.getKey());
            seatPlayer.writeSocket(copy.build());
            if (shuXingSeat > 0 && seatPlayer.getSeat() == lastWinSeat) {
                seatMap.get(shuXingSeat).writeSocket(copy.build());
            }
        }

    }

    public void checkAction() {
        int nowSeat = getNowDisCardSeat();
        // 先判断拿牌的玩家
        CdePaohuziPlayer nowPlayer = seatMap.get(nowSeat);
        if (nowPlayer == null) {
            return;
        }
        PaohuziCheckCardBean checkCard = nowPlayer.checkCard(null, true, true, false);
        if (checkPaohuziCheckCard(checkCard)) {
            playAutoDisCard(checkCard);
            tiLong(nowPlayer);
			/*-- 暂时屏蔽两条龙的处理
			boolean needBuPai = false;
			if (checkCard.isTi()) {
				PaohuziHandCard cardBean = nowPlayer.getPaohuziHandCard();
				PaohzCardIndexArr valArr = cardBean.getGroupByNumberToArray();
				PaohuziIndex index3 = valArr.getPaohzCardIndex(3);
				if (index3 != null && index3.getLength() >= 2) {
					needBuPai = true;
				}
			}
			playAutoDisCard(checkCard);

			if (needBuPai) {
				PaohzCard buPai = leftCards.remove(0);
				for (XtPaohuziPlayer player : seatMap.values()) {
					if (nowSeat == player.getSeat()) {
						player.getHandPhzs().add(buPai);
						System.out.println("----------------------------------谁补:" + player.getName() + "  什么牌:" + buPai.getId() + "  醒子数量:" + leftCards.size());
					}
					player.writeComMessage(WebSocketMsgType.res_com_code_phzbupai, nowSeat, buPai.getId(), leftCards.size());
				}
			}
			 */
        }
        checkSendActionMsg();
    }

    /**
     * 自动出牌
     */
    private void playAutoDisCard(PaohuziCheckCardBean checkCard) {
        playAutoDisCard(checkCard, false);
    }

    /**
     * 自动出牌
     *
     * @param moPai 是否是摸牌 如果是摸牌，需要
     */
    public void playAutoDisCard(PaohuziCheckCardBean checkCard, boolean moPai) {
        if (checkCard != null && checkCard.getActionList() != null) {
            int seat = checkCard.getSeat();
            CdePaohuziPlayer player = seatMap.get(seat);
            if (player.isRobot()) {
                sleep();
            }
            List<Integer> list = PaohuziTool.toPhzCardIds(checkCard.getAutoDisList());
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
                CdePaohuziPlayer player = seatMap.get(nextseat);
                if (player != null && player.isRobot()) {
                    // 普通出牌
                    PaohuziHandCard paohuziHandCardBean = player.getPaohuziHandCard();
                    int card = RobotAI.getInstance().outPaiHandle(0, PaohuziTool.toPhzCardIds(paohuziHandCardBean.getOperateCards()), new ArrayList<Integer>());
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
                    CdePaohuziPlayer player = seatMap.get(key);
                    if (player == null || !player.isRobot()) {
                        // player.writeErrMsg(player.getName() + " 有动作" +
                        // entry.getValue());
                        continue;
                    }
                    List<Integer> actions = PaohzDisAction.parseToDisActionList(value);
                    for (int action : actions) {
                        if (!checkAction(player, action, null, null)) {
                            continue;
                        }
                        sleep();
                        if (action == PaohzDisAction.action_hu) {
                            broadMsg(player.getName() + "胡牌");
                            play(player, null, action);
                        } else if (action == PaohzDisAction.action_peng) {
                            play(player, null, action);

                        } else if (action == PaohzDisAction.action_chi) {
                            play(player, null, action);

                        } else if (action == PaohzDisAction.action_pao) {
                            // play(player, null, action);
                        } else if (action == PaohzDisAction.action_ti) {
                            // play(player,
                            // PaohuziTool.toPhzCardIds(nowDisCardIds), action);
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
        timeNum = 0;
        clearTempAction();
        finishFapai = 0;
        setHuType(HuType.PING_HU);
        setPaoHu(0);
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
                tempMap.put("nowDisCardIds", StringUtil.implode(PaohuziTool.toPhzCardIds(nowDisCardIds), ","));
            }
            if (tempMap.containsKey("leftPais")) {
                tempMap.put("leftPais", StringUtil.implode(PaohuziTool.toPhzCardIds(leftCards), ","));
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
        wrapper.putInt(11, shuXingSeat);
        wrapper.putInt(12, maxPlayerCount);
        wrapper.putString("startLeftCards", startLeftCardsToJSON());
        wrapper.putString(13, JSONObject.toJSON(gameModel).toString());
        wrapper.putInt(20, autoPlayGlob);
        wrapper.putInt(21, autoTimeOut);
        JSONArray tempJsonArray = new JSONArray();
        for (int seat : tempActionMap.keySet()) {
            tempJsonArray.add(tempActionMap.get(seat).buildData());
        }
        wrapper.putString("22", tempJsonArray.toString());
        wrapper.putInt(24, finishFapai);
        wrapper.putInt(25, huType != null ? huType.ordinal() : 0);
        wrapper.putInt(26, uphillRatio);
        wrapper.putInt(27, paoHu);
        wrapper.putInt(28, lastWinSeat);
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
        synchronized (this) {
            if (maxPlayerCount <= 1 || maxPlayerCount > 4) {
                return;
            }

            changeTableState(table_state.play);
            deal();
        }
    }

    /**
     * @param
     * @return
     * @description 开局  发牌
     * @author Guang.OuYang
     * @date 2019/9/2
     */
    @Override
    protected void deal() {

        for (CdePaohuziPlayer player : playerMap.values()) {
            if (!player.isAlreadyMo())
                player.setLastCheckTime(0);
        }
        if (isGoldRoom()) {
            List<Long> list0 = new ArrayList<>(3);
            try {
                List<HashMap<String, Object>> list = GoldRoomDao.getInstance().loadRoomUsersLastResult(playerMap.keySet(), id);
                if (list != null) {
                    for (HashMap<String, Object> map : list) {
                        if (NumberUtils.toInt(String.valueOf(map.getOrDefault("gameResult", "0")), 0) > 0) {
                            list0.add(NumberUtils.toLong(String.valueOf(map.getOrDefault("userId", "0")), 0));
                        }
                    }
                }
            } catch (Exception e) {
            }
            if (list0.size() > 0) {
                Long userId = list0.get(new SecureRandom().nextInt(list0.size()));
                Player player = playerMap.get(userId);
                if (player != null) {
                    setLastWinSeat(player.getSeat());
                }
            }
            if (lastWinSeat <= 0) {
                setLastWinSeat(new SecureRandom().nextInt(playerMap.size()));
            }
        } else {
            //设置庄家
            if (getPlayBureau() == 1) {
                if (gameModel.getChangeBankerWay() == 1) {
                    setLastWinSeat(new Random().nextInt(getMaxPlayerCount()) + 1);
                } else {
                    setLastWinSeat(playerMap.get(masterId).getSeat());
                }
            }
        }

        if (lastWinSeat == 0) {
            setLastWinSeat(playerMap.get(masterId).getSeat());
        }

        setDisCardSeat(lastWinSeat);
        setNowDisCardSeat(lastWinSeat);
        setMoSeat(lastWinSeat);
        setToPlayCardFlag(1);
        markMoSeat(null, lastWinSeat);
        List<Integer> copy = new ArrayList<>(PaohuziConstant.cardList);
        
//        for(int i=0; i<10000;i++){
//        	List<List<PaohzCard>> list2 = PaohuziTool.fapai(copy, zp,getMaxPlayerCount());
//        	
//        	
//        	boolean sw=PaohuziTool.allReFaPai(list2, getMaxPlayerCount());
//        	if(!sw){
//        		System.out.println("---------------------------------"+list2);
//        	}
//        	
//        	
//        }
//        
        
        List<List<PaohzCard>> list = PaohuziTool.fapai(copy, zp,getMaxPlayerCount());
        
        
        
        int i = 1;
        for (CdePaohuziPlayer player : playerMap.values()) {
            player.changeState(player_state.play);
            player.getFirstPais().clear();
            if (player.getSeat() == lastWinSeat) {
                player.dealHandPais(list.get(0));
                player.getFirstPais().addAll(PaohuziTool.toPhzCardIds(new ArrayList(list.get(0))));//将初始手牌保存结算时发给客户端
                continue;
            }
            player.dealHandPais(list.get(i));
            player.getFirstPais().addAll(PaohuziTool.toPhzCardIds(new ArrayList(list.get(i))));//将初始手牌保存结算时发给客户端
            i++;

            StringBuilder sb = new StringBuilder("CdePhz");
            sb.append("|").append(getId());
            sb.append("|").append(getPlayBureau());
            sb.append("|").append(player.getUserId());
            sb.append("|").append(player.getSeat());
            sb.append("|").append(player.getName());
            sb.append("|").append("fapai");
            sb.append("|").append(player.getHandPhzs());
            LogUtil.msgLog.info(sb.toString());
        }

        List<PaohzCard> cardList = new ArrayList<>(list.get(3));
        if (maxPlayerCount <= 2) {
            cardList.addAll(list.get(2));
        }
        //桌上所有剩余牌
        setStartLeftCards(PaohuziTool.toPhzCardIds(cardList));

        // 桌上剩余的牌
        if (gameModel.getDiscardHoleCards() <= 0) {
            setLeftCards(cardList);
        } else if (gameModel.getDiscardHoleCards() >= cardList.size()) {
            setLeftCards(null);
        } else {
            int size = cardList.size();
            //抽牌
            chouCards = PaohuziTool.toPhzCardIds(cardList.subList(0, gameModel.getDiscardHoleCards()));
            setLeftCards(cardList.subList(gameModel.getDiscardHoleCards(), size));
        }
        finishFapai = 1;
    }

    /**
     * @param
     * @return
     * @description 天胡检测
     * @author Guang.OuYang
     * @date 2019/9/20
     */
    private void checkTianHu(int seat) {
        CdePaohuziPlayer player = seatMap.get(seat);
        if (player != null) {
            PaohuziHuLack paohuziHuLack = player.checkHu(null, true);

//            boolean tianHu = false;
//            PaohzCardIndexArr attr = PaohuziTool.getMax(player.getHandPhzs());
//            if (attr.getPaohzCardIndex(3).getPaohzValMap().size() >= 3 || attr.getPaohzCardIndex(2).getPaohzValMap().size() >= 5) {
//                tianHu = true;
//            }

            if (/*tianHu ||*/ paohuziHuLack.isHu()) {
                LogUtil.msgLog.info("SkyHu uid:{} seat:{} tableid: {}", player.getUserId(), seat, getId());
                PaohuziCheckCardBean check = new PaohuziCheckCardBean();
                check.setSeat(player.getSeat());
                check.setLack(paohuziHuLack);
                check.setHu(true);
                check.buildActionList();
                addAction(player.getSeat(), check.getActionList());
                sendPlayerActionMsg(player);
                player.setTingFlag(1);
            }
            
			for (Entry<Integer, CdePaohuziPlayer> entry : seatMap.entrySet()) {
				// 过滤当前出牌玩家
				if (entry.getKey() == player.getSeat() || player.getHandPais().isEmpty()) {
					continue;
				}
				if (gameModel.getSpecialPlay().isLiangZhang()) {
					// 检查牌, 其他家玩家可做的操作
					PaohuziCheckCardBean checkCard = entry.getValue().checkCard(
							PaohzCard.getPaohzCard(player.getHandPais().get(0)), false, false, false, true, false);
					if (checkCard.isHu()) {
						checkCard.setActionList(PaohzDisAction.keepHu(checkCard.getActionList()));
						addAction(entry.getValue().getSeat(), checkCard.getActionList());
						sendPlayerActionMsg(entry.getValue());
					}
				}
				boolean ting = PaohuziTool.getTingZps(entry.getValue());
				// checkCard.isHu()||
				if (ting) {
					entry.getValue().setTingFlag(1);
					// logTing(entry.getValue(), 1);
				}

			}
            
//            else {
//            	
//            	
//            }
        }
    }

    /**
     * @param
     * @return
     * @description 三提五坎检测
     * @author Guang.OuYang
     * @date 2019/9/20
     */
    private void checkThreeTiFiveKan() {
        Iterator<CdePaohuziPlayer> iterator = seatMap.values().iterator();
        while (iterator.hasNext()) {
            CdePaohuziPlayer player = iterator.next();
            if (gameModel.getSpecialPlay().isThreeTiFiveKan() && isThreeTiFiveKan(player)) {
                PaohuziHuLack paohuziHuLack = player.checkHu(null, true);

                LogUtil.msgLog.info("ThreeTiFiveKanHu uid:{} seat:{} tableid: {}", player.getUserId(), player.getSeat(), getId());
                PaohuziCheckCardBean check = new PaohuziCheckCardBean();
                check.setSeat(player.getSeat());
                check.setLack(paohuziHuLack);
                check.setHu(true);
                check.buildActionList();
                addAction(player.getSeat(), check.getActionList());
                sendPlayerActionMsg(player);
            }
        }
    }

    /**
     * @param
     * @return
     * @description 3提5坎检测
     * @author Guang.OuYang
     * @date 2019/9/24
     */
    public boolean isThreeTiFiveKan(CdePaohuziPlayer player) {
        boolean res = false;
        if (player.isFirstDisSingleCard()) {
            PaohzCardIndexArr attr = PaohuziTool.getMax(player.getHandPhzs());
            res = (attr.getPaohzCardIndex(3) != null && attr.getPaohzCardIndex(3).getPaohzValMap().size() >= 3) ||
                    (attr.getPaohzCardIndex(2) != null && attr.getPaohzCardIndex(2).getPaohzValMap().size() >= 5);
        }
        return res;
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
        if (nextSeat == shuXingSeat) {
            nextSeat = nextSeat + 1 > maxPlayerCount ? 1 : nextSeat + 1;
        }
        return nextSeat;
    }

    /**
     * 计算seat前面的座位
     */
    public int calcFrontSeat(int seat) {
        int frontSeat = seat - 1 < 1 ? maxPlayerCount : seat - 1;
        if (frontSeat == shuXingSeat) {
            frontSeat = frontSeat - 1 < 1 ? maxPlayerCount : frontSeat - 1;
        }
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
        for (CdePaohuziPlayer player : playerMap.values()) {
            PlayerInTableRes.Builder playerRes = player.buildPlayInTableInfo(userId, isrecover);
            if (playerRes == null) {
                continue;
            }
            //是否为庄
            playerRes.addRecover((player.getSeat() == lastWinSeat) ? 1 : 0);
            if (player.getUserId() == userId) {
                if (player.getSeat() == shuXingSeat) {
                    CdePaohuziPlayer winPlayer = seatMap.get(lastWinSeat);

                    playerRes.addAllHandCardIds(winPlayer.getHandPais());
                    if (actionSeatMap.containsKey(winPlayer.getSeat())) {
                        List<Integer> actionList = getSendSelfAction(zaiKeyValue, winPlayer.getSeat(), actionSeatMap.get(winPlayer.getSeat()));
                        if (actionList != null) {
                            playerRes.addAllRecover(actionList);
                        }
                    }
                } else {
                    playerRes.addAllHandCardIds(player.getHandPais());
                    if (actionSeatMap.containsKey(player.getSeat())) {
                        List<Integer> actionList = getSendSelfAction(zaiKeyValue, player.getSeat(), actionSeatMap.get(player.getSeat()));
                        if (actionList != null && !tempActionMap.containsKey(player.getSeat()) && !huConfirmList.contains(player.getSeat())) {
                            playerRes.addAllRecover(actionList);
                        }
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
        //下标二
        res.addExt(gameModel.getSpecialPlay().isRedBlackHu() ? 1 : 0);// 2
        res.addExt(gameModel.getConverHuXiToTunRatio());// 3
        res.addExt(modeId.length() > 0 ? Integer.parseInt(modeId) : 0);//4
        int ratio;
        int pay;
        if (isGoldRoom()) {
            ratio = GameConfigUtil.loadGoldRatio(modeId);
            pay = PayConfigUtil.get(playType, totalBureau, getMaxPlayerCount(), payType == 1 ? 0 : 1, modeId);
        } else {
            ratio = 1;
            pay = consumeCards() ? loadPayConfig(payType) : 0;
        }
        res.addExt(ratio);// 5
        res.addExt(pay);// 6
        res.addExt(gameModel.getDiscardHoleCards());// 7

        res.addExt(creditMode);     // 8

        res.addExt(0);
        res.addExt(0);
        res.addExt(0);
        res.addExt(0);
        res.addExt(creditCommissionMode1);// 13
        res.addExt(creditCommissionMode2);// 14
        res.addExt(autoPlay ? 1 : 0);// 15
        res.addExt(gameModel.getDoubleChip());// 16
        res.addExt(gameModel.getDoubleChipLeChip());// 17
        res.addExt(gameModel.getDoubleRatio());// 18


        res.addTimeOut((isGoldRoom() || autoPlay) ? (int) autoTimeOut : 0);
        res.addTimeOut(autoCheckTime);
        res.addTimeOut((isGoldRoom() || autoPlay) ? (int) autoTimeOut2 : 0);
        return res.build();
    }

    @Override
    public void setConfig(int index, int val) {

    }

    /**
     * 目前仅小结算展示唯一赢家,大结算可能有多个赢家,中途解散没有赢家展示,当大结算时可以不用找出小结算最终赢家
     *
     * @param over        结束
     * @param winList     赢家列表
     * @param winFen      赢得积分
     * @param mt          番类型
     * @param totalTun    囤数
     * @param isBreak     true中途解散
     * @param outScoreMap 赢家剩余金币
     * @param ticketMap   赢家金币还能结算多少局
     * @return
     * @description 结算消息, 大结算
     * @author Guang.OuYang
     * @date 2019/9/4
     */
    public ClosingPhzInfoRes.Builder sendAccountsMsg(boolean over, List<Integer> winList, int winFen, Map<Integer,Integer> mt, int totalTun, boolean isBreak, Map<Long, Integer> outScoreMap, Map<Long, Integer> ticketMap) {
        List<ClosingPhzPlayerInfoRes> list = new ArrayList<>();
        List<ClosingPhzPlayerInfoRes.Builder> builderList = new ArrayList<>();

        if(!isGoldRoom()){
        	int totalAddPoint = 0;

            List<Integer> bigWinList = new ArrayList<>();
            //大结算
            //大结算,找赢家,赢家可能会有多个
            Iterator<CdePaohuziPlayer> iterator = seatMap.values().iterator();
            while (iterator.hasNext()) {
                CdePaohuziPlayer p = iterator.next();
                if (p != null) {
                    //展示分
                    p.setWinLossPoint(p.getTotalPoint());
                    if (p.getTotalPoint() > 0) {
                        if (over) {
                            //大结算计算加倍分
                            int addScore = calcFinalRatio(p);
                            //低于X分进行加倍
                            totalAddPoint += addScore;
                            LogUtil.printDebug(p.getUserId() + "加分:{},{}", addScore, totalAddPoint);
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
                LogUtil.printDebug(v.getUserId() + "减分:{},{}", avg, v.getTotalPoint());
            });
        }

        //赢家
        CdePaohuziPlayer winPlayer = !CollectionUtils.isEmpty(winList) ? seatMap.get(winList.get(0)) : null;

        List<TablePhzResMsg.PhzHuCardList> cardCombos = new ArrayList<>();
        for (CdePaohuziPlayer player : seatMap.values()) {

            ClosingPhzPlayerInfoRes.Builder build = player.bulidTotalClosingPlayerInfoRes();

            LogUtil.printDebug(player.getUserId() + "结算分数:{},{} , point:{}", player.getWinLossPoint(), player.getTotalPoint(), player.getPoint());

            build.addAllFirstCards(player.getFirstPais());//将初始手牌装入网络对象

            for (int action : player.getActionTotalArr()) {     //0,1,2,3
                build.addStrExt(action + "");
            }

            if (isGoldRoom()) {
                build.addStrExt("1");//4
                build.addStrExt(player.loadAllGolds() <= 0 ? "1" : "0");//5
                build.addStrExt(outScoreMap == null ? "0" : outScoreMap.getOrDefault(player.getUserId(), 0).toString());//6
            } else {
                build.addStrExt("0");
                build.addStrExt("0");
                build.addStrExt("0");
            }
            build.addStrExt(ticketMap == null ? "0" : String.valueOf(ticketMap.getOrDefault(player.getUserId(), 0)));//7
            builderList.add(build);

            //信用分
            if (isCreditTable()) {
                player.setWinLoseCredit(player.getTotalPoint() * creditDifen);
            }

            TablePhzResMsg.PhzHuCardList.Builder builder = TablePhzResMsg.PhzHuCardList.newBuilder();
            builder.setSeat(player.getSeat());
            builder.addAllPhzCard(player.buildNormalPhzHuCards());
            cardCombos.add(builder.build());
        }

        //信用分计算
        if (isCreditTable()) {
            //计算信用负分
            calcNegativeCredit();

            long dyjCredit = 0;
            for (CdePaohuziPlayer player : seatMap.values()) {
                if (player.getWinLoseCredit() > dyjCredit) {
                    dyjCredit = player.getWinLoseCredit();
                }
            }
            for (ClosingPhzPlayerInfoRes.Builder builder : builderList) {
                CdePaohuziPlayer player = seatMap.get(builder.getSeat());
                calcCommissionCredit(player, dyjCredit);

                builder.addStrExt(player.getWinLoseCredit() + "");      //8
                builder.addStrExt(player.getCommissionCredit() + "");   //9
                // 2019-02-26更新
                builder.setWinLoseCredit(player.getWinLoseCredit());
                builder.setCommissionCredit(player.getCommissionCredit());
            }
        } else if (isGroupTableGoldRoom()) {
            // -----------亲友圈金币场---------------------------------
            for (CdePaohuziPlayer player : seatMap.values()) {
                player.setWinGold(player.getTotalPoint() * gtgDifen);
            }
            calcGroupTableGoldRoomWinLimit();
            for (ClosingPhzPlayerInfoRes.Builder builder : builderList) {
                CdePaohuziPlayer player = seatMap.get(builder.getSeat());
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
            CdePaohuziPlayer player = seatMap.get(builder.getSeat());
            builder.addStrExt(player.getTuo() + "");      //10
            list.add(builder.build());
        }

        ClosingPhzInfoRes.Builder res = ClosingPhzInfoRes.newBuilder();
        res.addAllLeftCards(PaohuziTool.toPhzCardIds(leftCards));
        int totalFan=0;
        if (mt != null) {
			//int bigSexEightIndex = getGameModel().bigSexEightMinSexEight();

			Iterator<Entry<Integer, Integer>> iterator1 = mt.entrySet().iterator();
			while (iterator1.hasNext()) {
				Entry<Integer, Integer> next = iterator1.next();
				BigMapEntry<Integer, String, int[]> val = PaohuziMingTangRule.SCORE_CALC.get(next.getKey());
				if (val != null && val.getE().equals("*")) {
					totalFan += val.getV()[0] + next.getValue();
					if(getGameModel().getMingTangPlayT()==3&&val.getK()!=PaohuziMingTangRule.LOUDI_MINGTANG_DAHONGHU&&val.getE().equals("*")){
						totalFan -=2;
					}
				}
				//占位传输首位1占位23位给到门子索引4位给到是番数还是囤数5位给到具体的番数或囤数值
				if (val != null){
					int fan = val.getV()[0];
					if(getGameModel().getMingTangPlayT()==3&&val.getK()!=PaohuziMingTangRule.LOUDI_MINGTANG_DAHONGHU&&val.getE().equals("*")){
						fan -=2;
					}
					int score = (((100 + val.getK()) * 10) + (val.getE().equals("*") ? 1 : 0)) * 100 + ( fan+ next.getValue());
					LogUtil.printDebug("番数:{}",score);
					res.addFanTypes(score);
				}else{
					LogUtil.printDebug("Not found {}", next.getKey());
				}
			}
        }
        if (winPlayer != null) {
//            res.setFan(winFen); //
            res.setFan(Math.max(totalFan, 1) - mt.getOrDefault(PaohuziMingTangRule.LOUDI_MINGTANG_UPHILL, 0)); //
//			res.setTun(totalTun / mt.getOrDefault(PaohuziMingTangRule.LOUDI_MINGTANG_UPHILL, 1) / res.getFan());// 总囤数
			res.setTun(winFen + (mt.containsKey(PaohuziMingTangRule.LOUDI_MINGTANG_ZIMO_ADD) ? PaohuziMingTangRule.SCORE_CALC.get(PaohuziMingTangRule.LOUDI_MINGTANG_ZIMO_ADD).getV()[0] : 0));// 总囤数
            res.setHuxi(winPlayer.getTotalHu());
            res.setTotalTun(totalTun);
            res.setHuSeat(winPlayer.getSeat());

            if (winPlayer.getHu() != null && winPlayer.getHu().getCheckCard() != null) {
                res.setHuCard(winPlayer.getHu().getCheckCard().getId());
            }
            res.addAllCards(winPlayer.buildPhzHuCards());
        }
        res.addAllClosingPlayers(list);
        res.setIsBreak(isBreak ? 1 : 0);
        res.setWanfa(getWanFa());
        res.addAllExt(buildAccountsExt(over));
        res.addAllStartLeftCards(startLeftCards);
        res.addAllAllCardsCombo(cardCombos);
        if (over && isGroupRoom() && !isCreditTable()) {
            res.setGroupLogId((int) saveUserGroupPlaylog());
        }
        for (CdePaohuziPlayer player : seatMap.values()) {
            player.writeSocket(res.build());
        }

        return res;
    }

    private int roundTen4Out5In(int lossTotalScore) {
        int val = (Math.abs(lossTotalScore) + 5) / 10 * 10;
        return lossTotalScore >= 0 ? val : -val;
    }

    /**
     * @param
     * @return
     * @description 计算最终翻倍倍率
     * @author Guang.OuYang
     * @date 2019/9/16
     */
    private int calcFinalRatio(CdePaohuziPlayer v) {
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


    @Override
    public void sendAccountsMsg() {
        //中途解散结算
        ClosingPhzInfoRes.Builder res = sendAccountsMsg(true, new ArrayList<>(), 0, null, 0, true, null, null);
        saveLog(true, 0L, res.build());
    }

    /**
     * @param
     * @return
     * @description 结算消息
     * @author Guang.OuYang
     * @date 2019/9/4
     */
    public List<String> buildAccountsExt(boolean isOver) {
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
        //金币场大于0
        ext.add(modeId);
        int ratio;
        int pay;
        if (isGoldRoom()) {
            ratio = GameConfigUtil.loadGoldRatio(modeId);
            pay = PayConfigUtil.get(playType, totalBureau, getMaxPlayerCount(), payType == 1 ? 0 : 1, modeId);
        } else {
            ratio = 1;
            pay = loadPayConfig(payType);
        }
        ext.add(String.valueOf(ratio));
        ext.add(String.valueOf(pay >= 0 ? pay : 0));
        ext.add(isGroupRoom() ? loadGroupId() : "");//13
        ext.add(String.valueOf(gameModel.getDiscardHoleCards()));//14


        //信用分
        ext.add(creditMode + ""); //15
        ext.add(creditJoinLimit + "");//16
        ext.add(creditDissLimit + "");//17
        ext.add(creditDifen + "");//18
        ext.add(creditCommission + "");//19
        ext.add(creditCommissionMode1 + "");//20
        ext.add(creditCommissionMode2 + "");//21
        ext.add(autoPlay ? "1" : "0");//22
        ext.add(Math.min(gameModel.getDoubleChip(), 1) + "");//23
        ext.add(gameModel.getDoubleChipLeChip() + "");//24
        ext.add(gameModel.getDoubleRatio() + "");//25

        ext.add(lastWinSeat + ""); // 26
        return ext;
    }

    /**
     * @param
     * @return
     * @description 解散消息 dissState 0正常打完 1群主解散 2玩家申请解散
     * @author Guang.OuYang
     * @date 2019/9/4
     */
    private String dissInfo() {
        JSONObject jsonObject = new JSONObject();
        if (getSpecialDiss() == 1) {
            jsonObject.put("dissState", "1");//群主解散
        } else {
            if (answerDissMap != null && !answerDissMap.isEmpty()) {
                jsonObject.put("dissState", "2");//玩家申请解散
                StringBuilder str = new StringBuilder();
                for (Entry<Integer, Integer> entry : answerDissMap.entrySet()) {
                    Player player0 = getSeatMap().get(entry.getKey());
                    if (player0 != null) {
                        str.append(player0.getUserId()).append(",");
                    }
                }
                if (str.length() > 0) {
                    str.deleteCharAt(str.length() - 1);
                }
                jsonObject.put("dissPlayer", str.toString());
            } else {
                jsonObject.put("dissState", "0");//正常打完
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
//        return createTable(player, play, bureauCount, params, saveDb);
        return createTable(new CreateTableInfo(player, TABLE_TYPE_NORMAL, play, bureauCount, params, strParams, true));
    }

    public void createTable(Player player, int play, int bureauCount, List<Integer> params) throws Exception {
//        createTable(player, play, bureauCount, params, true);
        createTable(new CreateTableInfo(player, TABLE_TYPE_NORMAL, play, bureauCount, params, strParams, true));
    }

//    public boolean createTable(Player player, int play, int bureauCount, List<Integer> params, boolean saveDb) throws Exception {
    public boolean createTable(CreateTableInfo createTableInfo) throws Exception {
    	 Player player = createTableInfo.getPlayer();
         int play = createTableInfo.getPlayType();
         int bureauCount =createTableInfo.getBureauCount();
         int tableType = createTableInfo.getTableType();
         List<Integer> params = createTableInfo.getIntParams();
         List<String> strParams = createTableInfo.getStrParams();
         boolean saveDb = createTableInfo.isSaveDb();
    	
    	long id = getCreateTableId(player.getUserId(), play);
        if (id <= 0) {
            return false;
        }
        if (saveDb) {
            TableInf info = new TableInf();
            info.setMasterId(player.getUserId());
            info.setRoomId(0);
            info.setTableType(tableType);
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
        } else {
            setPlayType(play);
            setDaikaiTableId(daikaiTableId);
            this.id = id;
            this.totalBureau = bureauCount;
            this.playBureau = 1;
        }

        GameModel.GameModelBuilder gameModelBuilder = GameModel.builder()
                //局数
                .gameFinishRound(StringUtil.getIntValue(params, 0, 1))
                //最大胡息结算
//                .gameFinishMaxHuXi(100)
                // 比赛人数
                .gameMaxHuman(StringUtil.getIntValue(params, 7, 2))
                //支付方式1AA,2房主
                .payType(StringUtil.getIntValue(params, 9, 0))
                //除掉的牌数量
                .discardHoleCards(StringUtil.getIntValue(params, 14, 0))
                //单局最低胡息起胡
//                .roundFinishLowestHuXi(Math.max(StringUtil.getIntValue(params, 30, 10), 10))
                .roundFinishLowestHuXi(15)
                //息转屯比例 1, 1息1屯 3 3息1屯
                //这里属于额外囤数
                .converHuXiToTunRatio(StringUtil.getIntValue(params, 13, 3) == 1 ? 1 : 3)
//                .converHuXiToTunRatio(1)
                //0随机 1先进房坐庄
                .changeBankerWay(StringUtil.getIntValue(params, 15, 0))
                //进入自动托管时间, 0.不托管 1.1分钟 60s 2.2分钟 120s, 3.3分钟 180s, 5.5分钟 300s
//                .autoOutCard(StringUtil.getIntValue(params, 23, 0) * 60000)
                //是否翻倍
                .doubleChip(StringUtil.getIntValue(params, 24, 0))
                //低于多少分翻倍
                .doubleChipLeChip(StringUtil.getIntValue(params, 25, 0))
                //翻几倍
                .doubleRatio(Math.max(StringUtil.getIntValue(params, 26, 1), 1))
                //低于多少分阈值
                .lowScoreLimit(Math.abs(StringUtil.getIntValue(params, 46, 0)))
                //低于多少分加多少分
                .lowScoreAdd(Math.abs(StringUtil.getIntValue(params, 47, 0)))
         
           
                //底分加成
                .basicSocreAdd(StringUtil.getIntValue(params, 45, 0))
//                .basicSocreAdd(0)
				//1全名堂/2红黑点 3 多红多番
				.mingTangPlayT(StringUtil.getIntValue(params, 16, 0))
				//大六八番/小六八番
				.bigSexEight(StringUtil.getIntValue(params, 17, 0))
				//总囤数限制
				.maxHuXiLimit(new int[]{
						StringUtil.getIntValue(params, 18, 0),
						StringUtil.getIntValue(params, 18, 0),
						StringUtil.getIntValue(params, 18, 0),
						StringUtil.getIntValue(params, 18, 0),
						StringUtil.getIntValue(params, 18, 0)})
//				.topScore(StringUtil.getIntValue(params, 18, 0))
				;

        GameModel.SpecialPlay.SpecialPlayBuilder specialPlayBuilder = GameModel.SpecialPlay.builder()
                //偎牌1明偎,0暗偎
//                .weiWay(StringUtil.getIntValue(params, 16, 0) > 0)
                .weiWay(false)
				//点炮
				.ignite(true)
				//叠加门子
				.repeatedEffect(true)
				;


		gameModelBuilder.specialPlay(specialPlayBuilder.build());

        this.gameModel = gameModelBuilder.build();

		if(gameModel.getMingTangPlayT()==1){
			//全名堂
			specialPlayBuilder
					.skyFloorHu(StringUtil.getIntValue(params, 42, 0)>0)
//					.seaHu(true)
//					.tingHu(true)
					.redBlackHu(StringUtil.getIntValue(params, 37, 0)>0)
					.aSmallRed(StringUtil.getIntValue(params, 38, 0)>0)
					.maxMinChar(StringUtil.getIntValue(params, 39, 0)>0)
//			.ppHu(true)
//					.monkeyShowHu(true)
					.uphill(StringUtil.getIntValue(params, 41, 0)>0)
					.groupHu(StringUtil.getIntValue(params, 20, 0)>0)
					.hanghangX(StringUtil.getIntValue(params, 19, 0)>0)
					.jiaHangHang(StringUtil.getIntValue(params, 21, 0)>0)
					.siQiHong(StringUtil.getIntValue(params, 22, 0)>0)
					.tingHu(StringUtil.getIntValue(params, 31, 0)>0)
					.monkeyShowHu(StringUtil.getIntValue(params, 32, 0)>0)
					.seaHu(StringUtil.getIntValue(params, 33, 0)>0)
					.kappa(StringUtil.getIntValue(params, 34, 0)>0)
					.liangZhang(StringUtil.getIntValue(params, 35, 0)>0)
					.sinceTouchAdd3Score(true)
//					.ppHu(StringUtil.getIntValue(params, 19, 0)>0)
					.ppHu(StringUtil.getIntValue(params, 40, 0)>0)
			;
		}else if(gameModel.getMingTangPlayT()==2){
			//红黑点
			specialPlayBuilder
					.redBlackHu(true)
					.aSmallRed(true)
					.redWu(true)
					.sinceTouchAdd3Score(true)
//					.sinceTouchAdd3Score(true)
					.ppHu(true)
			;
		}else{
			specialPlayBuilder
//			.skyFloorHu(true)
//			.seaHu(true)
//			.tingHu(true)
			.redBlackHu(true)
			.aSmallRed(true)
			.sinceTouchAdd3Score(true)
			.ppHu(true);
			

		}

		gameModelBuilder.specialPlay(specialPlayBuilder.build());

		this.gameModel = gameModelBuilder.build();


        //黄庄-10胡
//        this.gameModel.getSpecialPlay().optionNoneHuCardFiveHuXi(StringUtil.getIntValue(params, 34, 0) > 0);
        //重置坨属性
//        this.gameModel.resetTuo();

        super.autoPlay = StringUtil.getIntValue(params, 23, 0) >= 1;
        this.autoPlayGlob = StringUtil.getIntValue(params, 27, 0);
        this.maxPlayerCount = gameModel.getGameMaxHuman();


        //人数检测
        if (gameModel.getGameMaxHuman() < 2 || gameModel.getGameMaxHuman() > 3) {
            return false;
        }

		if (gameModel.getBasicSocreAdd() > 5 || gameModel.getBasicSocreAdd() < 1) {
			//默认底分1
			gameModel.setBasicSocreAdd(1);
		}

		if (Arrays.stream(new int[] { 0, 100, 200, 300, 500 }).noneMatch(v -> v != gameModel.getTopScore())) {
			//默认不设封顶
			gameModel.setTopScore(0);
		}

        //人数在2时才进行抽牌
        if (gameModel.getGameMaxHuman() > 2) {
            gameModel.setDiscardHoleCards(0);
        }

        //倍数校验,不在范围内倍率改为不加倍
        if (gameModel.getDoubleRatio() < 2 && gameModel.getDoubleRatio() > 4) {
            gameModel.setDoubleRatio(1);
        }

        super.setPayType(gameModel.getPayType());

//        if (isGoldRoom()) {
//            try {
//                GoldRoom goldRoom = GoldRoomDao.getInstance().loadGoldRoom(id);
//                if (goldRoom != null) {
//                    modeId = goldRoom.getModeId();
//                }
//            } catch (Exception e) {
//            }
//            autoTimeOut = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "autoTimeOutPhz",20 * 1000);
//        } else {
            if (autoPlay) {
                int time = StringUtil.getIntValue(params, 23, 0);
                if (time == 1) {
                    time = 60;
                }
                autoTimeOut2 = autoTimeOut = time * 1000;
            }
//        }
        changeExtend();
        LogUtil.msgLog.info("createTable tid:" + getId() + " " + player.getName() + " params" + params.toString());
        return true;
    }

    @Override
    public int getWanFa() {
        return SharedConstants.game_type_paohuzi;
    }

    @Override
    public boolean isTest() {
        return PaohuziConstant.isTest;
    }

    @Override
    public void checkReconnect(Player player) {
        checkMo();
        CdePaohuziPlayer xtPaohuziPlayer = (CdePaohuziPlayer) player;
        //吃边打边
        if (gameModel.getSpecialPlay().isEatSideDozenEdge()) {
            if (xtPaohuziPlayer.getDisNum() == xtPaohuziPlayer.getCbdbNum()) {
                List<Integer> forbidChiCardIds = xtPaohuziPlayer.getForbidChiCardIds();
                if (forbidChiCardIds != null && forbidChiCardIds.size() != 0) {
                    ComMsg.ComRes msg = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_lyzp_cbdb,
                            forbidChiCardIds.toArray()).build();
                    xtPaohuziPlayer.writeSocket(msg);
                }
            }
        }

        //重连时,发完牌后需要同步坨选项给其他玩家
        if (finishFapai == 1 && gameModel.getTuo() > 0) {
            //重连走选锤流程
            //同步消息给其他玩家是否选锤信息, 互相广播
            seatMap.values().forEach(this::broadcastTuoStateMsg);
        } else if (gameModel.getTuo() > 0) { //比赛没有开始就掉线, 重新发送选坨操作
            gameModel.setRepeatedSendTuo(false);
            isAllReadyCheck();
        }
    }

    /**
     * @param
     * @return
     * @description 给其他玩家广播当前的选坨信息
     * @author Guang.OuYang
     * @date 2019/9/11
     */
    public void broadcastTuoStateMsg(CdePaohuziPlayer player) {
        try {
            AbsCodeCommandExecutor.getGlobalActionCodeInstance(AbsCodeCommandExecutor.GlobalCommonIndex.COMMAND_INDEX, WebSocketMsgType.REQ_XXGHZ_TUO).ifPresent(v -> {
                v.execute(this, player, new AbsCodeCommandExecutor.CarryMessage(null, null, ComMsg.ComReq.newBuilder().addParams(player.getTuo()).buildPartial()));
            });
        } catch (Exception e) {
        }
    }

    /**
     * @param
     * @return
     * @description 选择了坨, 所有玩家必须选择完毕, 未选择坨直接开启发牌
     * @author Guang.OuYang
     * @date 2019/9/6
     */
    public boolean canStart() {
        return gameModel.getTuo() <= 0 || gameModel.getTuo() > 0 && getSeatMap().values().stream().allMatch(v -> ((CdePaohuziPlayer) v).getTuo() > 0);
    }


    @Override
    public void checkAutoPlay() {
        synchronized (this) {
            if (getSendDissTime() > 0) {
                for (CdePaohuziPlayer player : seatMap.values()) {
                    if (player.getLastCheckTime() > 0) {
                        player.setLastCheckTime((player.getLastCheckTime() + 1) * 1000);
                    }
                }
                return;
            }

			if (isAutoPlayOff()) {
				// 托管关闭
				for (int seat : seatMap.keySet()) {
					CdePaohuziPlayer player = seatMap.get(seat);
					player.setAutoPlay(false, this);
					player.setLastOperateTime(System.currentTimeMillis());
				}
				return;
			}

            if (autoPlay && state == table_state.ready && playedBureau > 0) {
                ++timeNum;
                for (CdePaohuziPlayer player : seatMap.values()) {
                    // 玩家进入托管后，5秒自动准备
                    if (timeNum >= 5 && player.isAutoPlay()) {
                        autoReady(player);
                    } else if (timeNum >= 30) {
                        autoReady(player);
                    }
                }
                return;
            }

            int timeout;
            if (state != table_state.play) {
                return;
            } else if (isGoldRoom() || autoPlay) {
                timeout = autoTimeOut;
            } else if (autoPlay) {
                timeout = autoTimeOut;
            } else {
                return;
            }
            //timeout = 10*1000;
            long autoPlayTime = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "autoPlayTimePhz", 2 * 1000);
            long now = TimeUtil.currentTimeMillis();

            if (!actionSeatMap.isEmpty()) {
                int action = 0, seat = 0;
                for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
                    List<Integer> list = PaohzDisAction.parseToDisActionList(entry.getValue());
                    int minAction = Collections.min(list);
                    if (action == 0) {
                        action = minAction;
                        seat = entry.getKey();
                    } else if (minAction < action) {
                        action = minAction;
                        seat = entry.getKey();
                    } else if (minAction == action) {
                        int nearSeat = getNearSeat(disCardSeat, Arrays.asList(seat, entry.getKey()));
                        seat = nearSeat;
                    }
                }
                if (action > 0 && seat > 0) {
                    CdePaohuziPlayer player = seatMap.get(seat);
                    if (player == null) {
                        LogUtil.errorLog.error("auto play error:tableId={},seat={} is null,seatMap={},playerMap={}", id, seat, seatMap.keySet(), playerMap.keySet());
                        return;
                    }

                    boolean auto = player.isAutoPlay();
                    if (!auto) {
                        auto = checkPlayerAuto(player, timeout);
                    }
                    if (auto) {
                        if (player.getAutoPlayTime() == 0L) {
                            player.setAutoPlayTime(now);
                        } else if (player.getAutoPlayTime() > 0L && now - player.getAutoPlayTime() >= autoPlayTime) {
                            player.setAutoPlayTime(0L);
                            if (action == PaohzDisAction.action_chi) {
                                action = PaohzDisAction.action_pass;
                            }
                            if (action == PaohzDisAction.action_pass || action == PaohzDisAction.action_peng || action == PaohzDisAction.action_hu) {
                                play(player, new ArrayList<Integer>(), action);
                            } else {
                                checkMo();
                            }
                        }
                        return;
                    }
                    if (action == PaohzDisAction.action_pao && player.getLastCheckTime() > 0) {
                        checkMo();
                    }
                }
            } else {
                CdePaohuziPlayer player = seatMap.get(nowDisCardSeat);
                if (player == null) {
                    return;
                }
                if (toPlayCardFlag == 1) {
                    boolean auto = player.isAutoPlay();
                    if (!auto) {
                        auto = checkPlayerAuto(player, timeout);
                    }
                    if (auto) {
                        if (player.getAutoPlayTime() == 0L) {
                            player.setAutoPlayTime(now);
                        } else if (player.getAutoPlayTime() > 0L && now - player.getAutoPlayTime() >= autoPlayTime) {
                            player.setAutoPlayTime(0L);
                            PaohzCard paohzCard = PaohuziTool.autoDisCard(player.getHandPhzs());
                            if (paohzCard != null) {
                                play(player, Arrays.asList(paohzCard.getId()), 0);
                            }
                        }
                    }
                } else {
                    checkMo();
                }
            }
        }
    }

    public boolean checkPlayerAuto(CdePaohuziPlayer player, int timeout) {
        long now = TimeUtil.currentTimeMillis();
        boolean auto = false;
        if (player.isAutoPlayChecked() || (player.getAutoPlayCheckedTime() >= timeout && !player.isAutoPlayCheckedTimeAdded())) {
            player.setAutoPlayChecked(true);
            timeout = autoTimeOut2;
        }
        long lastCheckTime = player.getLastCheckTime();
        if (lastCheckTime > 0) {
            int checkedTime = (int) (now - lastCheckTime);
            if (checkedTime >= timeout) {
                auto = true;
            }
            if (auto) {
                player.setAutoPlay(true, this);
            }
        } else {
            player.setLastCheckTime(now);
            player.setCheckAuto(true);
            player.setAutoPlayCheckedTimeAdded(false);
        }

        return auto;
    }

    @Override
    public Class<? extends Player> getPlayerClass() {
        return CdePaohuziPlayer.class;
    }

    public PaohzCard getNextCard(int val) {
        if (this.leftCards.size() > 0) {
            Iterator<PaohzCard> iterator = this.leftCards.iterator();
            PaohzCard find = null;
            while (iterator.hasNext()) {
                PaohzCard paohzCard = iterator.next();
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

    public PaohzCard getNextCard() {
        if (this.leftCards.size() > 0) {
            PaohzCard card = this.leftCards.remove(0);
            dbParamMap.put("leftPais", JSON_TAG);
            return card;
        }
        return null;
    }

    public void setLeftCards(List<PaohzCard> leftCards) {
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

    public void setMoSeat(int lastMoSeat) {
        this.moSeat = lastMoSeat;
        changeExtend();
    }

    public void setNowDisCardIds(List<PaohzCard> nowDisCardIds) {
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

    public void markMoSeat(PaohzCard card, int seat) {
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

    public void setZaiCard(PaohzCard zaiCard) {
        this.zaiCard = zaiCard;
        changeExtend();
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

    public void setFirstCard(boolean firstCard) {
        this.firstCard = firstCard;
        changeExtend();
    }

    /**
     * 桌子上移除的牌
     */
    public void setBeRemoveCard(PaohzCard beRemoveCard) {
        this.beRemoveCard = beRemoveCard;
        changeExtend();
    }

    /**
     * 是否是该玩家摸的牌
     */
    public boolean isMoByPlayer(CdePaohuziPlayer player) {
        if (moSeatPair != null && moSeatPair.getValue() == player.getSeat()) {
            if (nowDisCardIds != null && !nowDisCardIds.isEmpty()) {
                if (nowDisCardIds.get(0).getId() == moSeatPair.getId()) {
                    return true;
                }
            }
        }
        return false;
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
        //俱乐部房间 单大局大赢家、单大局大负豪、总小局数、单大局赢最多、单大局输最多 数据统计
        if (isGroupRoom()) {
            String groupId = loadGroupId();
            int maxPoint = 0;
            int minPoint = 0;
            Long dataDate = Long.valueOf(new SimpleDateFormat("yyyyMMdd").format(new Date()));

            calcDataStatistics3(groupId);

            for (CdePaohuziPlayer player : playerMap.values()) {
                //总小局数
                DataStatistics dataStatistics1 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "xjsCount", playedBureau);
                DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics1, 3);
                int finalPoint;
                finalPoint = player.loadScore();

                //总大局数
                DataStatistics dataStatistics5 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "djsCount", 1);
                DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics5, 3);
                //总积分
                DataStatistics dataStatistics6 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "zjfCount", finalPoint);
                DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics6, 3);

                if (finalPoint > 0) {
                    if (finalPoint > maxPoint) {
                        maxPoint = finalPoint;
                    }
                    //单大局赢最多
                    DataStatistics dataStatistics2 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "winMaxScore", finalPoint);
                    DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics2, 4);
                } else if (finalPoint < 0) {
                    if (finalPoint < minPoint) {
                        minPoint = finalPoint;
                    }
                    //单大局输最多
                    DataStatistics dataStatistics3 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "loseMaxScore", finalPoint);
                    DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics3, 5);
                }
            }

            for (CdePaohuziPlayer player : playerMap.values()) {
                int finalPoint;
                finalPoint = player.loadScore();
                if (maxPoint > 0 && maxPoint == finalPoint) {
                    //单大局大赢家
                    DataStatistics dataStatistics4 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "dyjCount", 1);
                    DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics4, 1);
                } else if (minPoint < 0 && minPoint == finalPoint) {
                    //单大局大负豪
                    DataStatistics dataStatistics5 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "dfhCount", 1);
                    DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics5, 2);
                }
            }
        }
    }

    public long saveUserGroupPlaylog() {
        if (!needSaveUserGroupPlayLog()) {
            return 0;
        }
        UserGroupPlaylog userGroupLog = new UserGroupPlaylog();
        userGroupLog.setTableid(id);
        userGroupLog.setUserid(creatorId);
        userGroupLog.setCount(playBureau);
        String players = "";
        String score = "";
        String diFenScore = "";
        for (CdePaohuziPlayer player : seatMap.values()) {
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
    public boolean isCreditTable(List<Integer> params) {
        return params != null && params.size() > 15 && StringUtil.getIntValue(params, 15, 0) == 1;
    }


    public static void loadWanfaTables(Class<? extends BaseTable> cls) {
        for (Integer integer : wanfaList) {
            TableManager.wanfaTableTypesPut(integer, cls);
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
            CdePaohuziPlayer player = seatMap.get(seat);
            if (player != null) {
                players.add(String.valueOf(player.getUserId()));
                scores.add(String.valueOf(player.getTotalPoint()));
            }
        }
        logTable.setPlayers(players.toString());
        logTable.setScores(scores.toString());
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
        sb.append("【").append(getId()).append("】").append(finishBureau).append("/").append(totalBureau).append("局").append("\n");
        sb.append("————————————————").append("\n");
        sb.append("【").append(getRoomName()).append("】").append("\n");
        sb.append("【").append(getGameName()).append("】").append("\n");
        sb.append("【").append(TimeUtil.formatTime(new Date())).append("】").append("\n");
        int maxPoint = -999999999;
        List<CdePaohuziPlayer> players = new ArrayList<>();
        for (CdePaohuziPlayer player : seatMap.values()) {
            int point = player.loadScore();
            if (point > maxPoint) {
                maxPoint = point;
            }
            players.add(player);
        }
        Collections.sort(players, (o1, o2) -> o2.loadScore() - o1.loadScore());

        for (CdePaohuziPlayer player : players) {
            sb.append("————————————————").append("\n");
            int point = player.loadScore();
            sb.append(StringUtil.cutHanZi(player.getName(), 5)).append("【").append(player.getUserId()).append("】").append(point == maxPoint ? "，大赢家" : "").append("\n");
            sb.append(point > 0 ? "+" : point == 0 ? "" : "-").append(Math.abs(point)).append("\n");
        }
        return sb.toString();
    }

    /**
     * @param
     * @return
     * @description 向房间内各个玩家广播消息
     */
    public Stream<Player> broadcastStream() {
        return getSeatMap().values().stream();
    }

    /**
     * @param
     * @return
     * @description 向房间内各个玩家广播消息
     */
    public void broadcast(Stream<Player> stream, int type, Object... params) {
        stream.forEach(v -> v.writeComMessage(type, params));
    }

    /**
     * @param
     * @return
     * @description 向房间内各个玩家广播消息
     */
    public void broadcast(int type, Object... params) {
        broadcast(broadcastStream(), type, params);
    }

    public GameModel getGameModel() {
        return gameModel;
    }


}
