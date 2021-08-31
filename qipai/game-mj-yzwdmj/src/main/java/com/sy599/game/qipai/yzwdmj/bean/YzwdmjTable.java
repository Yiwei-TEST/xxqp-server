package com.sy599.game.qipai.yzwdmj.bean;

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
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.ComMsg.ComRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.DaPaiTingPaiInfo;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.DaPaiTingPaiRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.MoMajiangRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.PlayMajiangRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.TingPaiRes;
import com.sy599.game.msg.serverPacket.TableMjResMsg.ClosingMjInfoRes;
import com.sy599.game.msg.serverPacket.TableMjResMsg.ClosingMjPlayerInfoRes;
import com.sy599.game.msg.serverPacket.TableRes;
import com.sy599.game.msg.serverPacket.TableRes.CreateTableRes;
import com.sy599.game.msg.serverPacket.TableRes.DealInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes;
import com.sy599.game.qipai.yzwdmj.constant.YzwdmjConstants;
import com.sy599.game.qipai.yzwdmj.rule.Yzwdmj;
import com.sy599.game.qipai.yzwdmj.rule.YzwdmjMingTangRule;
import com.sy599.game.qipai.yzwdmj.rule.YzwdmjRobotAI;
import com.sy599.game.qipai.yzwdmj.tool.YzwdmjHelper;
import com.sy599.game.qipai.yzwdmj.tool.YzwdmjQipaiTool;
import com.sy599.game.qipai.yzwdmj.tool.YzwdmjResTool;
import com.sy599.game.qipai.yzwdmj.tool.YzwdmjTool;
import com.sy599.game.qipai.yzwdmj.tool.hulib.util.HuUtil;
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


public class YzwdmjTable extends BaseTable {
    /**
     * 当前打出的牌
     */
    private List<Yzwdmj> nowDisCardIds = new ArrayList<>();
    private Map<Integer, List<Integer>> actionSeatMap = new ConcurrentHashMap<>();
    /**
     * 玩家位置对应临时操作
     * 当同时存在多个可做的操作时
     * 1当优先级最高的玩家最先操作 则清理所有操作提示 并执行该操作
     * 2当操作优先级低的玩家先选择操作，则玩家先将所做操作存入临时操作中 当玩家优先级最高的玩家操作后 在判断临时操作是否可执行并执行
     */
    private Map<Integer, YzwdmjTempAction> tempActionMap = new ConcurrentHashMap<>();
    private int maxPlayerCount = 4;
    private List<Yzwdmj> leftMajiangs;
    /*** 玩家map */
    private Map<Long, YzwdmjPlayer> playerMap = new ConcurrentHashMap<Long, YzwdmjPlayer>();
    /*** 座位对应的玩家 */
    private Map<Integer, YzwdmjPlayer> seatMap = new ConcurrentHashMap<Integer, YzwdmjPlayer>();
    private List<Integer> huConfirmList = new ArrayList<>();//胡牌数组
    /**
     * 摸麻将的seat
     */
    private int moMajiangSeat;
    /**
     * 摸杠的麻将
     */
    private Yzwdmj moGang;
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
    private int canDianPao;
    /**
     * 选了庄闲就算分，不选就不额外算分
     **/
    private int isCalcBanker;
    /**
     * 胡7对
     **/
    private int hu7dui;

    private int isAutoPlay=0;//是否开启自动托管
    
    private int readyTime = 0 ;

    //private int auto_ready_time = 15000;//自动落座最大等待时间

    /**
     * 抢杠胡开关
     **/
    private int qiangGangHu;
    //是否加倍：0否，1是
    private int jiaBei=0;
    //加倍分数：低于xx分进行加倍
    private int jiaBeiFen=0;
    //加倍倍数：翻几倍
    private int jiaBeiShu=0;

    //是否已发牌
    private int finishFapai=0;


    
    /**托管1：单局，2：全局*/
    private int autoPlayGlob;
    private int autoTableCount;
    /*** 摸屁股的座标号*/
    private List<Integer> moTailPai = new ArrayList<>();

    List<Integer> paoHuSeat=new ArrayList<>();
    //码牌数
    private int maPaiNum=0;
    //码牌
    private List<Integer> maPai=new ArrayList<>();
    //两片
    private int lianPian=0;
    //底分2分
    private int diFen2=0;
    //最后一张摸的牌Id
    private volatile int lastMo=0;
    //飘分 0:不飘 1:每局飘1 2:每局飘2 3:飘3  4:自由飘分  5:首局定飘
    private int piaoFenType=0;
    //碰碰胡
    private int pengPengHu=0;
    //记录跟庄牌
    private List<Integer> genZhuan=new ArrayList<>();
    //1：出牌，2：摸牌
    private int disOrMo=0;
    //无筒子
    private int wuTong=0;
    //七对两倍
    private int qiDui2Bei=0;
    //清一色2倍
    private int qingYiSe2Bei=0;
    //低于below加分
    private int belowAdd=0;
    private int below=0;
    //版本号，用于日志查看，方便检查线上代码是否更新
    private int versionCode=1001;
    @Override
    public void
    createTable(Player player, int play, int bureauCount, List<Integer> params, List<String> strParams, Object... objects) throws Exception {
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


//--------------------------------------------------------------------------------------
        payType = StringUtil.getIntValue(params, 2, 1);//支付方式
        lianPian = StringUtil.getIntValue(params, 3, 0);
        qiangGangHu = StringUtil.getIntValue(params, 4, 0);
        hu7dui = StringUtil.getIntValue(params, 5, 0);
        isCalcBanker = StringUtil.getIntValue(params, 6, 0);
        maxPlayerCount = StringUtil.getIntValue(params, 7, 4);// 比赛人数
        pengPengHu = StringUtil.getIntValue(params, 8, 0);// 碰碰胡
        diFen2 = StringUtil.getIntValue(params, 9, 0);
        maPaiNum = StringUtil.getIntValue(params, 10, 4);
        isAutoPlay = StringUtil.getIntValue(params, 11, 0);
        if(isAutoPlay==1) {
            isAutoPlay=60;
        }
        autoPlay = (isAutoPlay > 1);

        autoPlayGlob = StringUtil.getIntValue(params, 12, 0);
        if(maxPlayerCount==2){
            this.jiaBei = StringUtil.getIntValue(params, 13, 0);
            this.jiaBeiShu = StringUtil.getIntValue(params, 14, 0);
            this.jiaBeiFen = StringUtil.getIntValue(params, 15, 0);
        }
        piaoFenType=StringUtil.getIntValue(params, 16, 0);
        wuTong=StringUtil.getIntValue(params, 17, 0);
        qiDui2Bei=StringUtil.getIntValue(params, 18, 0);
        qingYiSe2Bei=StringUtil.getIntValue(params, 19, 0);
        if(maxPlayerCount==2){
            int belowAdd = StringUtil.getIntValue(params, 20, 0);
            if(belowAdd<=100&&belowAdd>=0)
                this.belowAdd=belowAdd;
            int below = StringUtil.getIntValue(params, 21, 0);
            if(below<=100&&below>=0){
                this.below=below;
                if(belowAdd>0&&below==0)
                    this.below=10;
            }
        }
        changeExtend();
        if (!isJoinPlayerAllotSeat()) {
//            getRoomModeMap().put("1", "1"); //可观战（默认）
        }
    }

    @Override
    public JsonWrapper buildExtend0(JsonWrapper wrapper) {
        for (YzwdmjPlayer player : seatMap.values()) {
            wrapper.putString(player.getSeat(), player.toExtendStr());
        }
        wrapper.putString(5, StringUtil.implode(huConfirmList, ","));
        wrapper.putInt(7, moMajiangSeat);
        if (moGang != null) {
            wrapper.putInt(8, moGang.getId());
        } else {
            wrapper.putInt(8, 0);
        }
        wrapper.putString(9, StringUtil.implode(moGangHuList, ","));
        wrapper.putInt(10, canDianPao);
        wrapper.putInt(11, isCalcBanker);
        wrapper.putInt(12, hu7dui);

        JSONArray tempJsonArray = new JSONArray();
        for (int seat : tempActionMap.keySet()) {
            tempJsonArray.add(tempActionMap.get(seat).buildData());
        }
        wrapper.putString("tempActions", tempJsonArray.toString());
        wrapper.putInt(13, maxPlayerCount);
        wrapper.putInt(14, dealDice);
        wrapper.putInt(15, isAutoPlay);
        wrapper.putInt(16, qiangGangHu);
        wrapper.putInt(17, moGangSeat);
        wrapper.putInt(18, moGangSameCount);
        wrapper.putString(19, StringUtil.implode(moTailPai, ","));
        wrapper.putInt(20, jiaBei);
        wrapper.putInt(21, jiaBeiFen);
        wrapper.putInt(22, jiaBeiShu);
        wrapper.putString(23,StringUtil.implode(paoHuSeat, ","));
        wrapper.putInt(24, autoPlayGlob);
        wrapper.putInt(25, finishFapai);
        wrapper.putInt(26, piaoFenType);
        wrapper.putInt(27, pengPengHu);
        wrapper.putInt(28, maPaiNum);
        wrapper.putInt(29, diFen2);
        wrapper.putInt(30, lianPian);
        wrapper.putString(31,StringUtil.implode(maPai, ","));
        wrapper.putInt(32, disOrMo);
        wrapper.putString(33,StringUtil.implode(genZhuan, ","));
        wrapper.putInt(34, wuTong);
        wrapper.putInt(35, qiDui2Bei);
        wrapper.putInt(36, qingYiSe2Bei);
        wrapper.putInt(37, below);
        wrapper.putInt(38, belowAdd);
        wrapper.putInt(39, lastMo);
        return wrapper;
    }

    @Override
    public void initExtend0(JsonWrapper wrapper) {
        for (YzwdmjPlayer player : seatMap.values()) {
            player.initExtend(wrapper.getString(player.getSeat()));
        }
        String huListstr = wrapper.getString(5);
        if (!StringUtils.isBlank(huListstr)) {
            huConfirmList = StringUtil.explodeToIntList(huListstr);
        }
        moMajiangSeat = wrapper.getInt(7, 0);
        int moGangMajiangId = wrapper.getInt(8, 0);
        if (moGangMajiangId != 0) {
            moGang = Yzwdmj.getMajang(moGangMajiangId);
        }
        String moGangHu = wrapper.getString(9);
        if (!StringUtils.isBlank(moGangHu)) {
            moGangHuList = StringUtil.explodeToIntList(moGangHu);
        }
        canDianPao = wrapper.getInt(10, 1);
        isCalcBanker = wrapper.getInt(11, 0);
        hu7dui = wrapper.getInt(12, 0);
        tempActionMap = loadTempActionMap(wrapper.getString("tempActions"));
        maxPlayerCount = wrapper.getInt(13, 4);
        dealDice = wrapper.getInt(14, 0);
        isAutoPlay = wrapper.getInt(15, 0);
        if(isAutoPlay ==1) {
            isAutoPlay=60;
        }
        qiangGangHu = wrapper.getInt(16, 0);
        moGangSeat = wrapper.getInt(17, 0);
        moGangSameCount = wrapper.getInt(18, 0);
        String s = wrapper.getString(19);
        if (!StringUtils.isBlank(s)) {
            moTailPai = StringUtil.explodeToIntList(s);
        }
        jiaBei = wrapper.getInt(20,0);
        jiaBeiFen = wrapper.getInt(21,0);
        jiaBeiShu = wrapper.getInt(22,0);
        s = wrapper.getString(23);
        if (!StringUtils.isBlank(s)) {
            paoHuSeat = StringUtil.explodeToIntList(s);
        }
        autoPlayGlob= wrapper.getInt(24,0);
        finishFapai= wrapper.getInt(25,0);
        piaoFenType= wrapper.getInt(26,0);
        pengPengHu= wrapper.getInt(27,0);
        maPaiNum= wrapper.getInt(28,4);
        diFen2= wrapper.getInt(29,0);
        lianPian= wrapper.getInt(30,0);
        s = wrapper.getString(31);
        if (!StringUtils.isBlank(s)) {
            maPai = StringUtil.explodeToIntList(s);
        }
        disOrMo= wrapper.getInt(32,0);
        s = wrapper.getString(33);
        if (!StringUtils.isBlank(s)) {
            genZhuan = StringUtil.explodeToIntList(s);
        }
        wuTong= wrapper.getInt(34,0);
        qiDui2Bei= wrapper.getInt(35,0);
        qingYiSe2Bei= wrapper.getInt(36,0);
        below= wrapper.getInt(37,0);
        belowAdd= wrapper.getInt(38,0);
        lastMo= wrapper.getInt(39,0);
    }



    public int getDealDice() {
        return dealDice;
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

    public boolean isHu7dui() {
        return hu7dui == 1;
    }

    public int getQiDui2Bei() {
        return qiDui2Bei;
    }

    public int getQingYiSe2Bei() {
        return qingYiSe2Bei;
    }

    public void setHu7dui(int hu7dui) {
        this.hu7dui = hu7dui;
    }

    public int getCanDianPao() {
        return canDianPao;
    }

    public void setCanDianPao(int canDianPao) {
        this.canDianPao = canDianPao;
    }

    public Yzwdmj getMoGang() {
        return moGang;
    }

    public int getLastMo() {
        return lastMo;
    }

    public int getPengPengHu() {
        return pengPengHu;
    }

    public int getLianPian() {
        return lianPian;
    }

    public int getPiaoFenType() {
        return piaoFenType;
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
        List<Integer> winList = getWinList(huConfirmList);
        boolean selfMo = false;
        Map<Integer, Integer> seatBridMap = new HashMap<>();//位置,中鸟数
        int catchBirdSeat = 0;//抓鸟人座位
        if (winList.size() == 0 && leftMajiangs.isEmpty()) {
            for(int seat:seatMap.keySet()){
                YzwdmjPlayer player = seatMap.get(seat);
                player.clearPointArr();
            }
        } else {
            // 先判断是自摸还是放炮
//            if (winList.size() == 1 && seatMap.get(winList.get(0)).getHandMajiang().size() % 3 == 2 && winList.get(0) == moMajiangSeat) {
//                selfMo = true;
//            }
            if(nowDisCardSeat==winList.get(0))
                selfMo=true;
            List<Integer> maP = getMaPaiFen();
            int maFen=0;
            if(maPaiNum==10&&maP.size()==1){
                Integer id = maP.get(0);
                if(id>200){
                    maFen=10;
                }else {
                    maFen=Yzwdmj.getMajang(id).getVal()%10;
                }
            }else {
                maFen=maP.size();
            }
            int diFen=(diFen2==1)?2:1;
            if (selfMo) {
                // 自摸+
                List<Integer> mt = new ArrayList<>();
                YzwdmjPlayer winner = seatMap.get(winList.get(0));
                winner.addZiMoNum();
                if(isCalcBanker==1&&winner.getSeat()==lastWinSeat&&playBureau>1)
                    mt.add(YzwdmjMingTangRule.LOUDI_MINGTANG_ZHUANGXIAN);
                mt = YzwdmjMingTangRule.calcMingTang(this, winner, mt);
                int fan = YzwdmjMingTangRule.countFan(mt);
                int winFen=0;
                int lostFen=0;
                for (int seat : seatMap.keySet()) {
                    YzwdmjPlayer player = seatMap.get(seat);
                    if(seat!=winner.getSeat()){
                        player.changePointArr(0,-maFen);
                        int[] pointArr = player.getPointArr();
                        lostFen = (-diFen + pointArr[0]) * fan + pointArr[1];
                        player.changePoint(lostFen);
                        winFen=winFen-lostFen;
                        setWinLostPiaoFen(winner,player);
                    }else {
                        winner.changePointArr(0,maFen);
                        winner.setHuType(mt);
                    }
                }
                winner.changePoint(winFen);
            } else {
                //抢杠胡
                YzwdmjPlayer losePlayer = seatMap.get(disCardSeat);
                int winFen=0;
                int lostFen=0;
                for (int seat : winList) {
                    YzwdmjPlayer winner = seatMap.get(seat);
                    winner.addQiangHuNum();
                    List<Integer> mt = new ArrayList<>();
                    mt.add(YzwdmjMingTangRule.LOUDI_MINGTANG_QIANGGANGHU);
                    if(isCalcBanker==1&&winner.getSeat()==lastWinSeat&&playBureau>1)
                        mt.add(YzwdmjMingTangRule.LOUDI_MINGTANG_ZHUANGXIAN);
                    mt=YzwdmjMingTangRule.calcMingTang(this,winner,mt);
                    int fan = YzwdmjMingTangRule.countFan(mt);

                    winner.changePointArr(0,maFen);
                    winner.setHuType(mt);
                    int[] pointArr = winner.getPointArr();
                    winFen = (diFen + pointArr[0]) * fan * (getMaxPlayerCount()-1) + pointArr[1];
                    winner.changePoint(winFen);
                    lostFen+=winFen;
                    setWinLostPiaoFen(winner,losePlayer);
                }
                losePlayer.changePointArr(0,-maFen);
                losePlayer.changePoint(-lostFen);
            }
            //非流局的情况下算飘分
            for(int seat:seatMap.keySet()){
                YzwdmjPlayer player = seatMap.get(seat);
                player.setPoint(player.getPoint()+player.getWinLostPiaoFen());
                player.changeTotalPoint(player.getWinLostPiaoFen());
                player.changePointArr(2,player.getWinLostPiaoFen());
            }
            //非流局下算跟庄
            if(isGenZhuang()){
                for(int seat:seatMap.keySet()){
                    YzwdmjPlayer player = seatMap.get(seat);
                   if(seat==lastWinSeat){
                       player.changePointArr(3,-3);
                       player.setPoint(player.getPoint()-3);
                   }else {
                       player.changePointArr(3,1);
                       player.setPoint(player.getPoint()+1);
                   }
                }
            }
        }


        boolean over = playBureau == totalBureau;
        if(autoPlayGlob >0) {
//          //是否解散
            boolean diss = false;
            if(autoPlayGlob ==1) {
                for (YzwdmjPlayer seat : seatMap.values()) {
                    if(seat.isAutoPlay()) {
                    diss = true;
                    break;
                    }
                }
            }
            else if(autoPlayGlob ==3) {
                diss = checkAuto3();
            }
            if(diss) {
                autoPlayDiss= true;
            	over =true;
            }
        }

        ClosingMjInfoRes.Builder res = sendAccountsMsg(over, selfMo, winList, maPai, null, catchBirdSeat, false);
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
        if (over) {
            calcOver1();
            calcOver2();
            calcOver3();
            diss();
        } else {
            initNext();
            calcOver1();
        }
        for (YzwdmjPlayer player : seatMap.values()) {
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
//		if(autoPlayGlob==3) {
        boolean diss2 = false;
        for (YzwdmjPlayer seat : seatMap.values()) {
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

    private boolean isGenZhuang(){
        if(getMaxPlayerCount()!=4)
            return false;
        if(genZhuan.contains(-1)||genZhuan.size()!=4)
            return false;
        int x=-1;
        for (int i = 0; i < genZhuan.size(); i++) {
            if(x==-1){
                x=genZhuan.get(i);
            }else {
                if(genZhuan.get(i)!=x)
                    return false;
            }
        }
        return true;
    }

    public List<Integer> getWinList(List<Integer> winList){
        List<Integer> list=new ArrayList<>(winList);
        if(winList==null||winList.size()<=1)
            return list;
        int nowSeat=nowDisCardSeat;
        while (true){
            nowSeat=calcNextSeat(nowSeat);
            if(nowSeat==nowDisCardSeat)
                break;
            if(winList.contains(nowSeat))
                list.add(nowSeat);
        }
        return list;
    }




    public void setWinLostPiaoFen(YzwdmjPlayer win,YzwdmjPlayer lost) {
        if(piaoFenType>0){
            lost.setWinLostPiaoFen(-win.getPiaoFen()-lost.getPiaoFen());
            win.setWinLostPiaoFen(win.getWinLostPiaoFen()+win.getPiaoFen()+lost.getPiaoFen());
        }
    }

    public List<Integer> getMaPaiFen(){
        List<Integer> maPai=new ArrayList<>();
        if(maPaiNum==0)
            return maPai;
        if(maPaiNum==10&&this.maPai.size()>=1){
            maPai.add(this.maPai.get(0));
        }else {
            for (int i = 0; i < this.maPai.size(); i++) {
                Integer id = this.maPai.get(i);
                int val = Yzwdmj.getMajang(id).getVal();
                int x = val % 10;
                if(id>200||x==1||x==5||x==9){
                    maPai.add(id);
                }
            }
        }
        return maPai;
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
        for (YzwdmjPlayer player : playerMap.values()) {
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
                tempMap.put("nowDisCardIds", StringUtil.implode(YzwdmjHelper.toMajiangIds(nowDisCardIds), ","));
            }
            if (tempMap.containsKey("leftPais")) {
                tempMap.put("leftPais", StringUtil.implode(YzwdmjHelper.toMajiangIds(leftMajiangs), ","));
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
        dealDice = (r.nextInt(6) + 1) * 10 + (r.nextInt(6) + 1);
        addPlayLog(disCardRound + "_" + lastWinSeat + "_" + YzwdmjDisAction.action_dice + "_" + dealDice);
        setDealDice(dealDice);
        // 天胡或者暗杠
        logFaPaiTable();
        for (YzwdmjPlayer tablePlayer : seatMap.values()) {
            DealInfoRes.Builder res = DealInfoRes.newBuilder();
            if (lastWinSeat == tablePlayer.getSeat()) {
                List<Integer> actionList = tablePlayer.checkMo(null);
                if (!actionList.isEmpty()) {
                    addActionSeat(tablePlayer.getSeat(), actionList);
                    res.addAllSelfAct(actionList);
                    logFaPaiPlayer(tablePlayer, actionList);
                }
            }
            res.addAllHandCardIds(tablePlayer.getHandPais());
            res.setNextSeat(getNextDisCardSeat());
            res.setGameType(getWanFa());
            res.setRemain(leftMajiangs.size());
            res.setBanker(lastWinSeat);
            res.setDealDice(dealDice);
//			if (userId == tablePlayer.getUserId()) {
//				continue;
//			}
            tablePlayer.writeSocket(res.build());
            if (tablePlayer.isAutoPlay()) {
                tablePlayer.setAutoPlayTime(0);
            }
            if(tablePlayer.isAutoPlay()) {
            	addPlayLog(getDisCardRound() + "_" +tablePlayer.getSeat() + "_" + YzwdmjConstants.action_tuoguan + "_" +1+ tablePlayer.getExtraPlayLog());
            }
//            sendTingInfo(tablePlayer);
            logFaPaiPlayer(tablePlayer, null);
        }
        for (Player player : getRoomPlayerMap().values()) {
            DealInfoRes.Builder res = DealInfoRes.newBuilder();
            res.setNextSeat(getNextDisCardSeat());
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

    public void moMajiang(YzwdmjPlayer player, boolean isBuZhang) {
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
        Yzwdmj majiang = null;
        if (disCardRound != 0) {
            // 玩家手上的牌是双数，已经摸过牌了
            if (player.isAlreadyMoMajiang()) {
                return;
            }
            if (getLeftMajiangCount() == 0) {
                calcOver();
                return;
            }
            if (GameServerConfig.isDebug() && zp != null && !zp.isEmpty()&&zp.size()==1) {
                majiang= Yzwdmj.getMajiangByValue(zp.get(0).get(0));
                zp.clear();
            }else {
                majiang = getLeftMajiang();
            }
        }
        if (majiang != null) {
            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + YzwdmjDisAction.action_moMjiang + "_" + majiang.getId() + player.getExtraPlayLog());
            player.moMajiang(majiang);
            lastMo=majiang.getId();
            player.passWang();
            setDisOrMo(YzwdmjDisAction.action_mo);
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
        for (YzwdmjPlayer seat : seatMap.values()) {
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
        for (Player roomPlayer : roomPlayerMap.values()) {
            MoMajiangRes.Builder copy = res.clone();
            roomPlayer.writeSocket(copy.build());
        }
    }



    /**
     * 玩家表示胡
     *
     * @param player
     * @param majiangs
     */
    private void hu(YzwdmjPlayer player, List<Yzwdmj> majiangs, int action) {
        if (state != table_state.play) {
            return;
        }
        List<Integer> actionList = actionSeatMap.get(player.getSeat());
        if (actionList == null || (actionList.get(YzwdmjConstants.ACTION_INDEX_HU) != 1 && actionList.get(YzwdmjConstants.ACTION_INDEX_ZIMO) != 1)) {// 如果集合为空或者第一操作不为胡，则返回
            return;
        }
        if (huConfirmList.contains(player.getSeat())) {
            return;
        }
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        List<Yzwdmj> huHand = new ArrayList<>(player.getHandMajiang());
        boolean zimo = player.isAlreadyMoMajiang();
        YzwdmjPlayer fangPaoPlayer;
        if (!zimo) {
            if (moGangHuList.contains(player.getSeat())) {
                // 抢杠胡
                huHand.add(moGang);
                builder.setFromSeat(nowDisCardSeat);
                builder.addHuArray(YzwdmjConstants.HU_QIANGGANGHU);
                player.getHuType().add(YzwdmjConstants.HU_QIANGGANGHU);
                fangPaoPlayer = seatMap.get(nowDisCardSeat);
                fangPaoPlayer.addGongGangNum(-1);
                fangPaoPlayer.getHuType().add(YzwdmjConstants.HU_FANGPAO);
            } else {
                // 放炮
                huHand.addAll(nowDisCardIds);
                builder.setFromSeat(disCardSeat);
                player.getHuType().add(YzwdmjConstants.HU_JIPAO);
                fangPaoPlayer = seatMap.get(disCardSeat);
                fangPaoPlayer.getHuType().add(YzwdmjConstants.HU_FANGPAO);
            }
        } else {
            builder.addHuArray(YzwdmjConstants.HU_ZIMO);
            player.getHuType().add(YzwdmjConstants.HU_ZIMO);
        }
        if (!YzwdmjTool.isHu(huHand, isHu7dui())) {
            return;
        }
        if (moGangHuList.contains(player.getSeat())) {
            YzwdmjPlayer moGangPlayer = seatMap.get(moGangSeat);
            if (moGangPlayer == null) {
                moGangPlayer = getPlayerByHasMajiang(moGang);
            }
            if (moGangPlayer == null) {
                moGangPlayer = seatMap.get(moMajiangSeat);
            }
            List<Yzwdmj> moGangMajiangs = new ArrayList<>();
            moGangMajiangs.add(moGang);
            moGangPlayer.addOutPais(moGangMajiangs, YzwdmjDisAction.action_chupai, 0);
            // 摸杠被人胡了 相当于自己出了一张牌
            recordDisMajiang(moGangMajiangs, moGangPlayer);
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
//        for (YzwdmjPlayer seat : seatMap.values()) {
//            // 推送消息
//            seat.writeSocket(builder.build());
//        }
        for (Player roomPlayer : roomPlayerMap.values()) {
            PlayMajiangRes.Builder copy = builder.clone();
            roomPlayer.writeSocket(copy.build());
        }
        // 加入胡牌数组
        addHuList(player.getSeat());
        changeDisCardRound(1);
        List<Yzwdmj> huPai = new ArrayList<>();
        huPai.add(huHand.get(huHand.size() - 1));
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + YzwdmjHelper.toMajiangStrs(huPai) + "_" + StringUtil.implode(player.getHuType(), ",") + player.getExtraPlayLog());
        logActionHu(player, majiangs, "");
        if (isCalcOver()) {
            // 胡
            for (YzwdmjPlayer seat : seatMap.values()) {
                seat.writeSocket(builder.build());
            }
            // 等待别人胡牌 如果都确认完了，胡
            calcOver();
        } else {
            //removeActionSeat(player.getSeat());
            player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip, action);
        }
    }

    /**
     * 王闯王钓胡牌
     *
     * @param player
     */
    private void huWang(YzwdmjPlayer player,int action) {
        logActionHuWang(player, player.getHandMajiang(), "",action);
        // 摸牌
        Yzwdmj majiang = null;
        if (disCardRound != 0) {
            // 玩家手上的牌是双数，已经摸过牌了
            if (player.isAlreadyMoMajiang()) {
                return;
            }
            if (getLeftMajiangCount() == 0) {
                calcOver();
                return;
            }
            if (GameServerConfig.isDebug() && zp != null && !zp.isEmpty()&&zp.size()==1) {
                majiang= Yzwdmj.getMajiangByValue(zp.get(0).get(0));
                zp.clear();
            }else {
                majiang = getLeftMajiang();
            }
            lastMo=majiang.getId();
            player.moMajiang(majiang);
        }

        List<Yzwdmj> handCards=new ArrayList<>(player.getHandMajiang());
        if(!YzwdmjMingTangRule.checkWang(handCards, majiang.getId(), new ArrayList<>(),isHu7dui()))
            return;
        player.removeWangAct(action);
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        builder.setZimo(1);
        buildPlayRes(builder, player, action, handCards);
        for (YzwdmjPlayer seat : seatMap.values()) {
            seat.writeSocket(builder.build());
        }
        // 加入胡牌数组
        addHuList(player.getSeat());
        changeDisCardRound(1);
        List<Yzwdmj> huPai = new ArrayList<>();
        huPai.add(handCards.get(handCards.size() - 1));
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + YzwdmjHelper.toMajiangStrs(huPai) + "_" + StringUtil.implode(player.getHuType(), ",") + player.getExtraPlayLog());

        calcOver();
    }

    private void buildPlayRes(PlayMajiangRes.Builder builder, Player player, int action, List<Yzwdmj> majiangs) {
        YzwdmjResTool.buildPlayRes(builder, player, action, majiangs);
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
    private YzwdmjPlayer getPlayerByHasMajiang(Yzwdmj majiang) {
        for (YzwdmjPlayer player : seatMap.values()) {
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
            YzwdmjPlayer moGangPlayer = null;
            if (!moGangHuList.isEmpty()) {
                // 如果有抢杠胡
                moGangPlayer = seatMap.get(moGangSeat);
                LogUtil.monitor_i("mogang player:" + moGangPlayer.getSeat() + " moGang:" + moGang);

            }
//            for (int huseat : huActionList) {
//                if (moGangPlayer != null) {
//                    // 被抢杠的人可以胡的话 跳过
//                    if (moGangPlayer.getSeat() == huseat) {
//                        continue;
//                    }
//                }
//                if (!huConfirmList.contains(huseat) &&
//                        !(tempActionMap.containsKey(huseat) && tempActionMap.get(huseat).getAction() == YzwdmjDisAction.action_hu)) {
//                    over = false;
//                    break;
//                }
//            }

            int huNum=0;
            for (List<Integer> acts:actionSeatMap.values()) {
                if(acts.get(YzwdmjConstants.ACTION_INDEX_ZIMO)==1)
                    huNum++;
            }

            if(huNum!=huConfirmList.size()){
                int nowSeat=nowDisCardSeat;
                while (true){
                    nowSeat=calcNextSeat(nowSeat);
                    if(nowSeat==nowDisCardSeat)
                        break;
                    if(!huActionList.contains(nowSeat))
                        continue;
                    if(!huConfirmList.contains(nowSeat)){
                        over = false;
                    }
                    break;
                }
            }
        }

        if (!over) {
            YzwdmjPlayer disMajiangPlayer = seatMap.get(disCardSeat);
            for (int huseat : huActionList) {
                if (huConfirmList.contains(huseat)) {
                    continue;
                }
                PlayMajiangRes.Builder disBuilder = PlayMajiangRes.newBuilder();
                YzwdmjPlayer seatPlayer = seatMap.get(huseat);
                buildPlayRes(disBuilder, disMajiangPlayer, 0, null);
                List<Integer> actionList = actionSeatMap.get(huseat);
                disBuilder.addAllSelfAct(actionList);
                seatPlayer.writeSocket(disBuilder.build());
            }
        }

        for (YzwdmjPlayer player : seatMap.values()) {
            if (player.isAlreadyMoMajiang() && !huConfirmList.contains(player.getSeat())) {
                over = false;
            }
        }

        return over;
    }

    /**
     * 碰杠
     *
     * @param player
     * @param majiangs
     * @param action
     */
    private void chiPengGang(YzwdmjPlayer player, List<Yzwdmj> majiangs, int action) {
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
        Yzwdmj disMajiang = null;
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
            sameCount = YzwdmjHelper.getMajiangCount(majiangs, majiangs.get(0).getVal());
        }
        // 如果是杠 后台来找出是明杠还是暗杠
        if (action == YzwdmjDisAction.action_minggang) {
            majiangs = YzwdmjHelper.getMajiangList(player.getHandMajiang(), majiangs.get(0).getVal());
            sameCount = majiangs.size();
            if (sameCount == 4) {
                // 有4张一样的牌是暗杠
                action = YzwdmjDisAction.action_angang;
            }
            // 其他是明杠
        }
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return;
        }
        boolean hasQGangHu = false;
        if (action == YzwdmjDisAction.action_peng) {
            boolean can = canPeng(player, majiangs, sameCount);
            if (!can) {
                return;
            }
        } else if (action == YzwdmjDisAction.action_angang) {
            boolean can = canAnGang(player, majiangs, sameCount);

            if (!can) {
                return;
            }
            player.addAnGangNum(1);
            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + YzwdmjHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
        } else if (action == YzwdmjDisAction.action_minggang) {
            boolean can = canMingGang(player, majiangs, sameCount);
            if (!can) {
                return;
            }
            player.addGongGangNum(1);
            ArrayList<Yzwdmj> mjs = new ArrayList<>(majiangs);
            if (sameCount == 3) {
                mjs.add(disMajiang);
            }
            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + YzwdmjHelper.toMajiangStrs(mjs) + player.getExtraPlayLog());

            // 特殊处理一张牌明杠的时候别人可以胡
            if (sameCount == 1 && canGangHu()) {
                if (checkQGangHu(player, majiangs, action, sameCount)) {
                    hasQGangHu = true;
                    setNowDisCardSeat(player.getSeat());
                    LogUtil.msg("tid:" + getId() + " " + player.getName() + "可以被抢杠胡！！");
                }
            }
            //点杠可枪
//            if (sameCount == 3 &&  canGangHu()) {
//                if (checkQGangHu(player, mjs, action, sameCount)) {
//                    hasQGangHu = true;
//                    setNowDisCardSeat(player.getSeat());
//                    LogUtil.msg("tid:" + getId() + " " + player.getName() + "可以被抢杠胡！！");
//                }
//            }
        } else {
            return;
        }
        if (disMajiang != null) {
            if ((action == YzwdmjDisAction.action_minggang && sameCount == 3)
                    || action == YzwdmjDisAction.action_peng || action == YzwdmjDisAction.action_chi) {
                if (action == YzwdmjDisAction.action_chi) {
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
    private boolean checkQGangHu(YzwdmjPlayer player, List<Yzwdmj> majiangs, int action, int sameCount) {
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        Map<Integer, List<Integer>> huListMap = new HashMap<>();
        for (YzwdmjPlayer seatPlayer : seatMap.values()) {
            if (seatPlayer.getUserId() == player.getUserId()) {
                continue;
            }
            // 推送消息
            List<Integer> hu = seatPlayer.checkDisMajiang(majiangs.get(0), this.canGangHu());
            if (!hu.isEmpty() && hu.get(0) == 1) {
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
                YzwdmjPlayer seatPlayer = seatMap.get(entry.getKey());
                copy.addAllSelfAct(entry.getValue());
                seatPlayer.writeSocket(copy.build());
            }
            return true;
        }
        return false;
    }

    private void chiPengGang(PlayMajiangRes.Builder builder, YzwdmjPlayer player, List<Yzwdmj> majiangs, int action, boolean hasQGangHu, int sameCount) {
        List<Integer> actionList = actionSeatMap.get(player.getSeat());
        if (action == YzwdmjDisAction.action_peng && actionList.get(YzwdmjConstants.ACTION_INDEX_MINGGANG) == 1) {
            // 可以碰也可以杠
            player.addPassGangVal(majiangs.get(0).getVal());
        }

        player.addOutPais(majiangs, action, disCardSeat);
        buildPlayRes(builder, player, action, majiangs);
        List<Integer> actList = removeActionSeat(player.getSeat());
        if (!hasQGangHu) {
            clearActionSeatMap();
        }
        if (action == YzwdmjDisAction.action_chi || action == YzwdmjDisAction.action_peng) {
            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + YzwdmjHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
        }
        // 不是普通出牌
        setNowDisCardSeat(player.getSeat());
        for (YzwdmjPlayer seatPlayer : seatMap.values()) {
            // 推送消息
            PlayMajiangRes.Builder copy = builder.clone();
            if (actionSeatMap.containsKey(seatPlayer.getSeat())) {
                copy.addAllSelfAct(actionSeatMap.get(seatPlayer.getSeat()));
            }
            seatPlayer.writeSocket(copy.build());
        }
        if (action == YzwdmjDisAction.action_chi || action == YzwdmjDisAction.action_peng) {
            sendTingInfo(player);
        }
        for (Player roomPlayer : roomPlayerMap.values()) {
            PlayMajiangRes.Builder copy = builder.clone();
            roomPlayer.writeSocket(copy.build());
        }
        if (!hasQGangHu) {
            calcPoint(player, action, sameCount, majiangs);
        }
        if (!hasQGangHu && action == YzwdmjDisAction.action_minggang || action == YzwdmjDisAction.action_angang) {
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
    private void chuPai(YzwdmjPlayer player, List<Yzwdmj> majiangs, int action) {
        if (state != table_state.play) {
            player.writeComMessage(WebSocketMsgType.res_code_mj_dis_err, action, majiangs.get(0).getId());
            return;
        }
        if (majiangs.size() != 1) {
            player.writeComMessage(WebSocketMsgType.res_code_mj_dis_err, action, majiangs.get(0).getId());
            return;
        }
        if (majiangs.get(0).getVal() == 201) {
            player.writeComMessage(WebSocketMsgType.res_code_mj_dis_err, action, majiangs.get(0).getId());
            return;
        }
        if (!tempActionMap.isEmpty()) {
            LogUtil.e(player.getName() + "出牌清理临时操作！");
            clearTempAction();
        }
        if (!player.isAlreadyMoMajiang()) {
            // 还没有摸牌
            player.writeComMessage(WebSocketMsgType.res_code_mj_dis_err, action, majiangs.get(0).getId());
            return;
        }
        if (!actionSeatMap.isEmpty()) {//出牌自动过掉手上操作
            guo(player, null, YzwdmjDisAction.action_pass);
        }
        if (!actionSeatMap.isEmpty()) {
            player.writeComMessage(WebSocketMsgType.res_code_mj_dis_err, action, majiangs.get(0).getId());
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
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + YzwdmjHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
        List<YzwdmjPlayer> l=new ArrayList<>();
        for (YzwdmjPlayer p : seatMap.values()) {
            List<Integer> list;
            if (p.getUserId() != player.getUserId()) {
                list = p.checkDisMajiang(majiangs.get(0), this.canDianPao());
                if(list==null||list.isEmpty())
                    continue;
                if (list.contains(1)) {

                    addActionSeat(p.getSeat(), list);
                    p.setLastCheckTime(System.currentTimeMillis());
                    logChuPaiActList(p, majiangs.get(0), list);
                }
            }
        }
//        for (int i = 0; i < l.size(); i++) {
//            hu(l.get(i),majiangs,YzwdmjDisAction.action_hu);
//        }
        sendDisMajiangAction(builder, player);

        // 给下一家发牌
        checkMo();
    }

    public List<Integer> getHuSeatByActionMap() {
        List<Integer> huList = new ArrayList<>();
        for (int seat : actionSeatMap.keySet()) {
            List<Integer> actionList = actionSeatMap.get(seat);
            if (actionList.get(YzwdmjConstants.ACTION_INDEX_HU) == 1 || actionList.get(YzwdmjConstants.ACTION_INDEX_ZIMO) == 1) {
                // 胡
                huList.add(seat);
            }
        }
        return huList;
    }

    private void sendDisMajiangAction(PlayMajiangRes.Builder builder, YzwdmjPlayer player) {
        for (YzwdmjPlayer seatPlayer : seatMap.values()) {
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
                copy.addExt(YzwdmjTool.isTing(seatPlayer.getHandMajiang(), isHu7dui()) ? "1" : "0");
            }
            seatPlayer.writeSocket(copy.build());
        }
        for (Player roomPlayer : roomPlayerMap.values()) {
            PlayMajiangRes.Builder copy = builder.clone();
            roomPlayer.writeSocket(copy.build());
        }
    }

    public synchronized void playCommand(YzwdmjPlayer player, List<Yzwdmj> majiangs, int action) {
        playCommand(player, majiangs, null, action);
    }

    /**
     * 出牌
     *
     * @param player
     * @param majiangs
     * @param action
     */
    public synchronized void playCommand(YzwdmjPlayer player, List<Yzwdmj> majiangs, List<Integer> hucards, int action) {
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

        if (YzwdmjDisAction.action_hu == action) {
            hu(player, majiangs, action);
            return;
        } else if(action == YzwdmjDisAction.action_wangchuang||action == YzwdmjDisAction.action_wangdiao) {
            huWang(player, action);
            return;
        }
        // 手上没有要出的麻将
        if (action != YzwdmjDisAction.action_minggang)
            if (!player.getHandMajiang().containsAll(majiangs)) {
                return;
            }
        changeDisCardRound(1);
        if(action==YzwdmjDisAction.action_peng||action==YzwdmjDisAction.action_minggang||action==YzwdmjDisAction.action_angang) {
            if(getMaxPlayerCount()==4&&genZhuan.size()<4)
                genZhuan.add(-1);
        }
        setDisOrMo(action);
        if (action == YzwdmjDisAction.action_pass) {
            if(!player.getWangAct().contains(1)){
                guo(player, majiangs, action);
            }else {
                guoWang(player,action);
            }

        } else if (action != 0) {
            chiPengGang(player, majiangs, action);
        } else {
            chuPai(player, majiangs, action);
            if(getMaxPlayerCount()==4&&!genZhuan.contains(-1)&&genZhuan.size()<4&&majiangs.size()==1){
                genZhuan.add(majiangs.get(0).getVal());
            }
        }
        // 记录最后一次动作的时间
        setLastActionTime(TimeUtil.currentTimeMillis());
    }

    private void setDisOrMo(int action){
        if(action==YzwdmjDisAction.action_chupai){
            disOrMo=1;
        }else if(action==YzwdmjDisAction.action_mo){
            disOrMo=2;
        }
    }

    private void passMoHu(YzwdmjPlayer player, List<Yzwdmj> majiangs, int action) {
        if (!moGangHuList.contains(player.getSeat())) {
            return;
        }

        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        buildPlayRes(builder, player, action, majiangs);
        builder.setSeat(nowDisCardSeat);
        removeActionSeat(player.getSeat());
        player.writeSocket(builder.build());
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + YzwdmjHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
        if (isCalcOver()) {
            //补胡牌动画消息
            List<Integer> winList = getWinList(huConfirmList);
            YzwdmjPlayer winner = seatMap.get(winList.get(0));
            List<Yzwdmj> huHand = new ArrayList<>(winner.getHandMajiang());
            huHand.add(moGang);
            builder = PlayMajiangRes.newBuilder();
            builder.setFromSeat(nowDisCardSeat);
            builder.addHuArray(YzwdmjConstants.HU_QIANGGANGHU);
            buildPlayRes(builder, winner, YzwdmjDisAction.action_hu, huHand);
            if (!huConfirmList.isEmpty()) {
                builder.addExt(StringUtil.implode(huConfirmList, ","));
            }
            for (YzwdmjPlayer seat : seatMap.values()) {
                seat.writeSocket(builder.build());
            }
            calcOver();
            return;
        }
        player.setPassMajiangVal(nowDisCardIds.get(0).getVal());

        //YzwdmjPlayer moGangPlayer = seatMap.get(moMajiangSeat);
        if (moGangHuList.isEmpty()) {
            YzwdmjPlayer moGangPlayer = seatMap.get(getNowDisCardSeat());
            majiangs = new ArrayList<>();
            majiangs.add(moGang);
            if (moGangPlayer.getaGang().contains(moGang)) {
                calcPoint(moGangPlayer, YzwdmjDisAction.action_angang, 4, majiangs);
            } else {
                calcPoint(moGangPlayer, YzwdmjDisAction.action_minggang, moGangSameCount > 0 ? moGangSameCount : 1, majiangs);
            }

            moMajiang(moGangPlayer, true);
//			builder = PlayMajiangRes.newBuilder();
//			chiPengGang(builder, moGangPlayer, majiangs, YzwdmjDisAction.action_minggang, false);
        }

    }

    /**
     * pass
     *
     * @param player
     * @param majiangs
     * @param action
     */
    private void guo(YzwdmjPlayer player, List<Yzwdmj> majiangs, int action) {
        if (state != table_state.play) {
            return;
        }
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return;
        }
        if(paoHuSeat.contains(player.getSeat())){
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_911));
            return;
        }

        if (!moGangHuList.isEmpty()) {
            // 有摸杠胡的优先处理
            passMoHu(player, majiangs, action);
            return;
        }
        if (actionSeatMap.get(player.getSeat()).get(YzwdmjConstants.ACTION_INDEX_MINGGANG) == 1) {
            // 可以碰也可以杠
            if(disOrMo==2)
                player.addPassGangVal(Yzwdmj.getMajang(lastMo).getVal());
        }
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        buildPlayRes(builder, player, action, majiangs);
        builder.setSeat(nowDisCardSeat);
        List<Integer> removeActionList = removeActionSeat(player.getSeat());
        player.writeSocket(builder.build());
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + YzwdmjHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
        if (isCalcOver()) {
            //补胡牌动画消息
            List<Integer> winList = getWinList(huConfirmList);
            YzwdmjPlayer winner = seatMap.get(winList.get(0));
            List<Yzwdmj> huHand = new ArrayList<>(winner.getHandMajiang());
            huHand.add(moGang);
            builder = PlayMajiangRes.newBuilder();
            builder.setFromSeat(nowDisCardSeat);
            builder.addHuArray(YzwdmjConstants.HU_QIANGGANGHU);
            buildPlayRes(builder, winner, YzwdmjDisAction.action_hu, huHand);
            if (!huConfirmList.isEmpty()) {
                builder.addExt(StringUtil.implode(huConfirmList, ","));
            }
            for (YzwdmjPlayer seat : seatMap.values()) {
                seat.writeSocket(builder.build());
            }
            calcOver();
            return;
        }
        if (removeActionList.get(0) == 1 && disCardSeat != player.getSeat() && nowDisCardIds.size() == 1) {
            // 漏炮
            player.setPassMajiangVal(nowDisCardIds.get(0).getVal());
        }
        logAction(player, action, majiangs, removeActionList);
        if (!actionSeatMap.isEmpty()) {
            YzwdmjPlayer disMajiangPlayer = seatMap.get(disCardSeat);
            PlayMajiangRes.Builder disBuilder = PlayMajiangRes.newBuilder();
            buildPlayRes(disBuilder, disMajiangPlayer, 0, null);
            for (int seat : actionSeatMap.keySet()) {
                List<Integer> actionList = actionSeatMap.get(seat);
                PlayMajiangRes.Builder copy = disBuilder.clone();
                copy.addAllSelfAct(new ArrayList<>());
                if (actionList != null && !tempActionMap.containsKey(seat) && !huConfirmList.contains(seat)) {
                    copy.addAllSelfAct(actionList);
                    YzwdmjPlayer seatPlayer = seatMap.get(seat);
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

    private void guoWang(YzwdmjPlayer player, int action) {
        logGuoWang(player,player.getWangAct());
        player.getWangAct().clear();
        player.setPassWang(1);
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        buildPlayRes(builder, player, action, new ArrayList<>());
        builder.setSeat(nowDisCardSeat);
        player.writeSocket(builder.build());
        checkMo();
    }

    private void calcPoint(YzwdmjPlayer player, int action, int sameCount, List<Yzwdmj> majiangs) {
        int lostPoint = 0;
        int getPoint = 0;
        int[] seatPointArr = new int[getMaxPlayerCount() + 1];
        if (action == YzwdmjDisAction.action_peng) {
            return;

        } else if (action == YzwdmjDisAction.action_angang) {
            // 暗杠相当于自摸每人出2分
            lostPoint = -2;
            getPoint = 2 * (getMaxPlayerCount() - 1);

        } else if (action == YzwdmjDisAction.action_minggang) {
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
            if (sameCount == 3) {
                YzwdmjPlayer disPlayer = seatMap.get(disCardSeat);
                seatPointArr[disPlayer.getSeat()] = -3;
                disPlayer.changePointArr(1,-3);
                seatPointArr[player.getSeat()] = 3;
                player.changePointArr(1,3);
            }
        }
        if(!player.getPassGangValList().containsAll(majiangs)){
            if (lostPoint != 0) {
                for (YzwdmjPlayer seat : seatMap.values()) {
                    if (seat.getUserId() == player.getUserId()) {
                        seat.changePointArr( 1,getPoint);
                        seatPointArr[player.getSeat()] = getPoint;
                    } else {
                        seat.changePointArr(1,lostPoint);
                        seatPointArr[seat.getSeat()] = lostPoint;
                    }
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

        if (action != YzwdmjDisAction.action_chi) {
//            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + YzwdmjHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog() + "_" + seatPointStr);
        }
    }

    private void recordDisMajiang(List<Yzwdmj> majiangs, YzwdmjPlayer player) {
        setNowDisCardIds(majiangs);
        // changeDisCardRound(1);
        setDisCardSeat(player.getSeat());
    }

    public List<Yzwdmj> getNowDisCardIds() {
        return nowDisCardIds;
    }

    public void setNowDisCardIds(List<Yzwdmj> nowDisCardIds) {
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
                YzwdmjPlayer player = seatMap.get(nowDisCardSeat);
                List<Integer> acts = player.checkWang(isHu7dui());
                if(acts.contains(1)&&player.getPassWang()==0){
                    //有王闯王钓，需要发送王闯王钓信息
                    sendWangAct(player,acts);
                }else {
                    //正常检测摸排
                    moMajiang(player, false);
                }
            }
            robotDealAction();

        } else {
            for (int seat : actionSeatMap.keySet()) {
                YzwdmjPlayer player = seatMap.get(seat);
                if (player != null && player.isRobot()) {
                    // 如果是机器人可以直接决定
                    List<Integer> actionList = actionSeatMap.get(seat);
                    if (actionList == null) {
                        continue;
                    }
                    List<Yzwdmj> list = new ArrayList<>();
                    if (!nowDisCardIds.isEmpty()) {
                        list = YzwdmjQipaiTool.getVal(player.getHandMajiang(), nowDisCardIds.get(0).getVal());
                    }
                    if (actionList.get(YzwdmjConstants.ACTION_INDEX_HU) == 1 || actionList.get(YzwdmjConstants.ACTION_INDEX_ZIMO) == 1) {
                        // 胡
                        playCommand(player, new ArrayList<Yzwdmj>(), YzwdmjDisAction.action_hu);

                    } else if (actionList.get(YzwdmjConstants.ACTION_INDEX_ANGANG) == 1) {
                        playCommand(player, list, YzwdmjDisAction.action_angang);

                    } else if (actionList.get(YzwdmjConstants.ACTION_INDEX_MINGGANG) == 1) {
                        playCommand(player, list, YzwdmjDisAction.action_minggang);

                    } else if (actionList.get(YzwdmjConstants.ACTION_INDEX_PENG) == 1) {
                        playCommand(player, list, YzwdmjDisAction.action_peng);
                    }
                }
            }

        }
    }

    @Override
    protected void robotDealAction() {
        if (true) {
            return;
        }
        if (isTest()) {
            int nextseat = getNextDisCardSeat();
            YzwdmjPlayer next = seatMap.get(nextseat);
            if (next != null && next.isRobot()) {
                List<Integer> actionList = actionSeatMap.get(next.getSeat());
                if (actionList != null) {
                    List<Yzwdmj> list = null;
                    if (actionList.get(YzwdmjConstants.ACTION_INDEX_HU) == 1 || actionList.get(YzwdmjConstants.ACTION_INDEX_ZIMO) == 1) {
                        // 胡
                        playCommand(next, new ArrayList<Yzwdmj>(), YzwdmjDisAction.action_hu);
                    } else if (actionList.get(YzwdmjConstants.ACTION_INDEX_ANGANG) == 1) {
                        // 机器人暗杠
                        Map<Integer, Integer> handMap = YzwdmjHelper.toMajiangValMap(next.getHandMajiang());
                        for (Entry<Integer, Integer> entry : handMap.entrySet()) {
                            if (entry.getValue() == 4) {
                                // 可以暗杠
                                list = YzwdmjHelper.getMajiangList(next.getHandMajiang(), entry.getKey());
                            }
                        }
                        playCommand(next, list, YzwdmjDisAction.action_angang);

                    } else if (actionList.get(YzwdmjConstants.ACTION_INDEX_MINGGANG) == 1) {
                        Map<Integer, Integer> pengMap = YzwdmjHelper.toMajiangValMap(next.getPeng());
                        for (Yzwdmj handMajiang : next.getHandMajiang()) {
                            if (pengMap.containsKey(handMajiang.getVal())) {
                                // 有碰过
                                list = new ArrayList<>();
                                list.add(handMajiang);
                                playCommand(next, list, YzwdmjDisAction.action_minggang);
                                break;
                            }
                        }

                    } else if (actionList.get(YzwdmjConstants.ACTION_INDEX_PENG) == 1) {
                        playCommand(next, list, YzwdmjDisAction.action_peng);
                    }
                } else {
                    List<Integer> handMajiangs = new ArrayList<>(next.getHandPais());
                    YzwdmjQipaiTool.dropHongzhongVal(handMajiangs);
                    int maJiangId = YzwdmjRobotAI.getInstance().outPaiHandle(0, handMajiangs, new ArrayList<Integer>());
                    List<Yzwdmj> majiangList = YzwdmjHelper.toMajiang(Arrays.asList(maJiangId));
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
        List<Integer> copy = YzwdmjConstants.getMajiangList(wuTong==1);
        addPlayLog(copy.size() + "");
        List<List<Yzwdmj>> list;
        if (zp != null&&zp.size()!=0) {
            list = YzwdmjTool.fapai(copy,zp,getMaxPlayerCount());
        } else {
            list = YzwdmjTool.fapai(copy, getMaxPlayerCount());
        }
        int i = 1;
        for (YzwdmjPlayer player : seatMap.values()) {
            player.changeState(player_state.play);
            if (player.getSeat() == lastWinSeat) {
                player.dealHandPais(list.get(0));
                continue;
            }
            player.dealHandPais(list.get(i));
            i++;
        }
        List<Yzwdmj> leftCards = list.get(getMaxPlayerCount());
        if(maPaiNum>0){
            if(maPaiNum==10){
                maPai = Yzwdmj.toPhzCardIds(leftCards.subList(0, 1));
                System.out.println("桌："+getId()+",局："+getPlayBureau()+",码牌："+maPai.toString());
                leftCards=leftCards.subList(1,leftCards.size());
            }else {
                maPai = Yzwdmj.toPhzCardIds(leftCards.subList(0, maPaiNum));
                System.out.println("桌："+getId()+",局："+getPlayBureau()+",码牌："+maPai.toString());
                leftCards=leftCards.subList(maPaiNum,leftCards.size());
            }
        }
        // 桌上剩余的牌
        setLeftMajiangs(leftCards);
        finishFapai=1;
    }

    @Override
    public void startNext() {
        for (YzwdmjPlayer tablePlayer : seatMap.values()) {
            sendTingInfo(tablePlayer);
        }
    }

    /**
     * 初始化桌子上剩余牌
     *
     * @param leftMajiangs
     */
    public void setLeftMajiangs(List<Yzwdmj> leftMajiangs) {
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
    public Yzwdmj getLeftMajiang() {
        if (this.leftMajiangs.size() > 0) {
            Yzwdmj majiang = this.leftMajiangs.remove(0);
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
        res.addExt(payType);                //0
        res.addExt(canDianPao);            //2
        res.addExt(isCalcBanker);           //3
        res.addExt(hu7dui);                 //4
        res.addExt(isAutoPlay);             //5
        res.addExt(qiangGangHu);            //6
        res.addExt(0);      //8

        res.addStrExt(StringUtil.implode(moTailPai, ","));      //0

        res.setMasterId(getMasterId() + "");
        if (leftMajiangs != null) {
            res.setRemain(leftMajiangs.size());
        } else {
            res.setRemain(0);
        }
        res.setDealDice(dealDice);
        List<PlayerInTableRes> players = new ArrayList<>();
        for (YzwdmjPlayer player : playerMap.values()) {
            PlayerInTableRes.Builder playerRes = player.buildPlayInTableInfo(isrecover);
            if (player.getUserId() == userId) {
                playerRes.addAllHandCardIds(player.getHandPais());
                if (!player.getHandMajiang().isEmpty() && player.getHandMajiang().size() % 3 == 1) {
                    if (player.isOkPlayer() && YzwdmjTool.isTing(player.getHandMajiang(), isHu7dui())) {
                        playerRes.setUserSate(3);
                    }
                }
            }
            if (player.getSeat() == disCardSeat && nowDisCardIds != null && moGangHuList.isEmpty()) {
                playerRes.addAllOutCardIds(YzwdmjHelper.toMajiangIds(nowDisCardIds));
            }
            playerRes.addRecover(player.getIsEntryTable());
            playerRes.addRecover(player.getSeat() == lastWinSeat ? 1 : 0);
            if(player.getWangAct().contains(1)){
                playerRes.addAllRecover(player.getWangAct());
            }else if (actionSeatMap.containsKey(player.getSeat())) {
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
        } else if (!moGangHuList.isEmpty()) {
            for (YzwdmjPlayer player : seatMap.values()) {
                if (player.getmGang() != null && player.getmGang().contains(moGang)) {
                    res.setNextSeat(player.getSeat());
                    break;
                }
            }
        }
        res.setRenshu(getMaxPlayerCount());
        res.setLastWinSeat(getLastWinSeat());
        res.addTimeOut((int) YzwdmjConstants.AUTO_TIMEOUT);
        return res.build();
    }

    public void sendWangAct(YzwdmjPlayer player,List<Integer> acts){
        MoMajiangRes.Builder res = MoMajiangRes.newBuilder();
        res.setUserId(player.getUserId() + "");
        res.setSeat(player.getSeat());
        res.addAllSelfAct(acts);
        player.writeSocket(res.build());
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
        paoHuSeat.clear();
        readyTime=0;
        finishFapai=0;
        lastMo=0;
        disOrMo=0;
        genZhuan=new ArrayList<>();
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
        YzwdmjPlayer player = seatMap.get(seat);
        addPlayLog(disCardRound + "_" + seat + "_" + YzwdmjDisAction.action_hasAction + "_" + StringUtil.implode(actionlist) + player.getExtraPlayLog());
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
            nowDisCardIds = YzwdmjHelper.toMajiang(StringUtil.explodeToIntList(info.getNowDisCardIds()));
        }

        if (!StringUtils.isBlank(info.getLeftPais())) {
            try {
                leftMajiangs = YzwdmjHelper.toMajiang(StringUtil.explodeToIntList(info.getLeftPais()));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }




    private Map<Integer, YzwdmjTempAction> loadTempActionMap(String json) {
        Map<Integer, YzwdmjTempAction> map = new ConcurrentHashMap<>();
        if (json == null || json.isEmpty())
            return map;
        JSONArray jsonArray = JSONArray.parseArray(json);
        for (Object val : jsonArray) {
            String str = val.toString();
            YzwdmjTempAction tempAction = new YzwdmjTempAction();
            tempAction.initData(str);
            map.put(tempAction.getSeat(), tempAction);
        }
        return map;
    }

    /**
     * 检查优先度 如果同时出现一个事件，按出牌座位顺序优先
     */
    private boolean checkAction(YzwdmjPlayer player, List<Yzwdmj> cardList, List<Integer> hucards, int action) {
        boolean canAction = checkCanAction(player, action);// 是否优先级最高 能执行操作
        if (!canAction) {// 不能操作时  存入临时操作
            int seat = player.getSeat();
            tempActionMap.put(seat, new YzwdmjTempAction(seat, action, cardList, hucards));
            // 玩家都已选择自己的临时操作后  选取优先级最高
            if (tempActionMap.size() == actionSeatMap.size()) {
                int maxAction = Integer.MAX_VALUE;
                int maxSeat = 0;
                Map<Integer, Integer> prioritySeats = new HashMap<>();
                int maxActionSize = 0;
                for (YzwdmjTempAction temp : tempActionMap.values()) {
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
                YzwdmjPlayer tempPlayer = seatMap.get(maxSeat);
                List<Yzwdmj> tempCardList = tempActionMap.get(maxSeat).getCardList();
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
    private void refreshTempAction(YzwdmjPlayer player) {
        tempActionMap.remove(player.getSeat());
        Map<Integer, Integer> prioritySeats = new HashMap<>();//各位置优先操作
        for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
            int seat = entry.getKey();
            List<Integer> actionList = entry.getValue();
            List<Integer> list = YzwdmjDisAction.parseToDisActionList(actionList);
            int priorityAction = YzwdmjDisAction.getMaxPriorityAction(list);
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
        Iterator<YzwdmjTempAction> iterator = tempActionMap.values().iterator();
        while (iterator.hasNext()) {
            YzwdmjTempAction tempAction = iterator.next();
            if (tempAction.getSeat() == maxPrioritySeat) {
                int action = tempAction.getAction();
                List<Yzwdmj> tempCardList = tempAction.getCardList();
                List<Integer> tempHuCards = tempAction.getHucards();
                YzwdmjPlayer tempPlayer = seatMap.get(tempAction.getSeat());
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
    public boolean checkCanAction(YzwdmjPlayer player, int action) {
        // 优先度为胡杠补碰吃
        List<Integer> stopActionList = YzwdmjDisAction.findPriorityAction(action);
        for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
            if (player.getSeat() != entry.getKey()) {
                // 别人
                boolean can = YzwdmjDisAction.canDisMajiang(stopActionList, entry.getValue());
                if (!can) {
                    return false;
                }
                List<Integer> disActionList = YzwdmjDisAction.parseToDisActionList(entry.getValue());
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
    private boolean canPeng(YzwdmjPlayer player, List<Yzwdmj> majiangs, int sameCount) {
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
    private boolean canAnGang(YzwdmjPlayer player, List<Yzwdmj> majiangs, int sameCount) {
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
    private boolean canMingGang(YzwdmjPlayer player, List<Yzwdmj> majiangs, int sameCount) {
        List<Yzwdmj> handMajiangs = player.getHandMajiang();
        List<Integer> pengList = YzwdmjHelper.toMajiangVals(player.getPeng());
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
    public void setMoGang(Yzwdmj moGang, List<Integer> moGangHuList, YzwdmjPlayer player, int sameCount) {
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
        if (getCanDianPao() == 1) {
            return true;
        }
        return false;
    }

    /**
     * @param over
     * @param selfMo
     * @param winList
     * @param maPai
     * @param seatBirds           鸟位置
     * @param isBreak
     * @return
     */
    public ClosingMjInfoRes.Builder sendAccountsMsg(boolean over, boolean selfMo, List<Integer> winList, List<Integer> maPai, int[] seatBirds, int catchBirdSeat, boolean isBreak) {

        // 大结算计算加倍分
        if(over && jiaBei == 1){
            int jiaBeiPoint = 0;
            int loserCount = 0;
            for (YzwdmjPlayer player : seatMap.values()) {
                if (player.getTotalPoint() > 0 && player.getTotalPoint() < jiaBeiFen) {
                    jiaBeiPoint += player.getTotalPoint() * (jiaBeiShu - 1);
                    player.setTotalPoint(player.getTotalPoint() * jiaBeiShu);
                } else if (player.getTotalPoint() < 0) {
                    loserCount++;
                }
            }
            if (jiaBeiPoint > 0) {
                for (YzwdmjPlayer player : seatMap.values()) {
                    if (player.getTotalPoint() < 0) {
                        player.setTotalPoint(player.getTotalPoint() - (jiaBeiPoint / loserCount));
                    }
                }
            }
        }

        //大结算低于below分+belowAdd分
        if(over&&belowAdd>0&&playerMap.size()==2){
            for (YzwdmjPlayer player : seatMap.values()) {
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
        for (YzwdmjPlayer player : seatMap.values()) {
            ClosingMjPlayerInfoRes.Builder build = null;
            if (over) {
                build = player.bulidTotalClosingPlayerInfoRes();
            } else {
                build = player.bulidOneClosingPlayerInfoRes();
            }
//            if (seatBridMap != null && seatBridMap.containsKey(player.getSeat())) {
//                build.setBirdPoint(seatBridMap.get(player.getSeat()));
//            } else {
//                build.setBirdPoint(0);
//            }
            if (winList != null && winList.contains(player.getSeat())) {
                if (!selfMo) {
                    // 不是自摸
                    Yzwdmj huMajiang = nowDisCardIds.get(0);
                    if (!build.getHandPaisList().contains(huMajiang.getId())) {
                        build.addHandPais(huMajiang.getId());
                    }
                    build.setIsHu(huMajiang.getId());
                } else {
                    build.setIsHu(player.getLastMoMajiang().getId());
                }
            }
            if (player.getSeat() == fangPaoSeat) {
                build.setFanPao(1);
                if(huConfirmList.isEmpty()&&leftMajiangs.isEmpty())
                    build.setFanPao(0);
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
            for (YzwdmjPlayer player : seatMap.values()) {
                if (player.getWinLoseCredit() > dyjCredit) {
                    dyjCredit = player.getWinLoseCredit();
                }
            }
            for (ClosingMjPlayerInfoRes.Builder builder : builderList) {
                YzwdmjPlayer player = seatMap.get(builder.getSeat());
                calcCommissionCredit(player, dyjCredit);
                builder.setWinLoseCredit(player.getWinLoseCredit());
                builder.setCommissionCredit(player.getCommissionCredit());
            }
        } else if (isGroupTableGoldRoom()) {
            // -----------亲友圈金币场---------------------------------
            for (YzwdmjPlayer player : seatMap.values()) {
                player.setWinGold(player.getTotalPoint() * gtgDifen);
            }
            calcGroupTableGoldRoomWinLimit();
            for (ClosingMjPlayerInfoRes.Builder builder : builderList) {
                YzwdmjPlayer player = seatMap.get(builder.getSeat());
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
        if (maPai != null) {
            res.addAllBird(maPai);
        }
        res.addAllLeftCards(YzwdmjHelper.toMajiangIds(leftMajiangs));
        res.setCatchBirdSeat(catchBirdSeat);
        res.addAllIntParams(getIntParams());
        for (YzwdmjPlayer player : seatMap.values()) {
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
        ext.add(masterId + "");                            //2
        ext.add(TimeUtil.formatTime(TimeUtil.now()));    //3
        ext.add(playType + "");                            //4
        ext.add(canDianPao + "");                        //5
        ext.add("");                                      //6
        ext.add(isCalcBanker + "");                        //7
        ext.add(lastWinSeat + "");                            //8
        ext.add(isAutoPlay + "");                        //9
        ext.add(qiangGangHu + "");                        //10
        ext.add(isLiuJu() + "");                        //11
        ext.add(over+"");                               //12
        return ext;
    }

    @Override
    public void sendAccountsMsg() {
        ClosingMjInfoRes.Builder builder = sendAccountsMsg(true, false, null, null, null, 0, true);
        saveLog(true, 0l, builder.build());
    }

    @Override
    public Class<? extends Player> getPlayerClass() {
        return YzwdmjPlayer.class;
    }

    @Override
    public int getWanFa() {
        return getPlayType();
    }

    @Override
    public void checkReconnect(Player player) {
        ((YzwdmjPlayer) player).checkAutoPlay(0, true);
        if (state == table_state.play) {
            YzwdmjPlayer player1 = (YzwdmjPlayer) player;
            if (player1.getHandPais() != null && player1.getHandPais().size() > 0) {
                sendTingInfo(player1);
            }
        }
        sendPiaoReconnect(player);
    }

    private void sendPiaoReconnect(Player player){
        if(piaoFenType==0||maxPlayerCount!=getPlayerCount())
            return;
        int count=0;
        for(Map.Entry<Integer,YzwdmjPlayer> entry:seatMap.entrySet()){
            player_state state = entry.getValue().getState();
            if(state==player_state.play||state==player_state.ready)
                count++;
        }
        if(count!=maxPlayerCount)
            return;

        for(Map.Entry<Integer,YzwdmjPlayer> entry:seatMap.entrySet()){
            YzwdmjPlayer p = entry.getValue();
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
    public boolean consumeCards() {
        return SharedConstants.consumecards;
    }

    @Override
    public void checkAutoPlay() {
        if (System.currentTimeMillis() - lastAutoPlayTime < 100) {
            return;
        }
        if (getSendDissTime() > 0) {
            for (YzwdmjPlayer player : seatMap.values()) {
                if (player.getLastCheckTime() > 0) {
                    player.setLastCheckTime(player.getLastCheckTime() + 1 * 1000);
                }
            }
            return;
        }
        
        if (isAutoPlay < 1) {
            return;
        }

//        if (piaoFenType>0&&isSendPiaoFenMsg == 1 && finishFapai == 0) {
//            //飘分模式，还没发牌
//            for (YzwdmjPlayer player : seatMap.values()) {
//                if(player.getPiaoFen()!=-1){
//                    continue;
//                }
//                boolean auto = checkPlayerAuto(player, autoTimeOut);
//                if (auto) {
//                    piaoFen(player, piaoFenType>=3?0:piaoFenType);
//                }
//            }
//            return;
//        }
        if (state == table_state.play) {
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
            for (YzwdmjPlayer player : seatMap.values()) {
                if (player.getState() != player_state.entry && player.getState() != player_state.over) {
                    continue;
                } else {
                    if (readyTime >= 5 && player.isAutoPlay()) {
                        // 玩家进入托管后，5秒自动准备
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
        YzwdmjPlayer p = seatMap.get(nowDisCardSeat);
        List<Integer> wangAct;
        if(p!=null){
            wangAct= p.getWangAct();
        }else {
            wangAct=new ArrayList<>();
        }
        if(wangAct.contains(1)&&p.isAutoPlay()){
            if(wangAct.get(6)==1){
                guoWang(p,YzwdmjDisAction.action_wangchuang);
            }else if(wangAct.get(7)==1){
                guoWang(p,YzwdmjDisAction.action_wangdiao);
            }
        }else
        if (!actionSeatMap.isEmpty()) {
            List<Integer> huSeatList = getHuSeatByActionMap();
            if (!huSeatList.isEmpty()) {
                //有胡处理胡
                for (int seat : huSeatList) {
                    YzwdmjPlayer player = seatMap.get(seat);
                    if (player == null) {
                        continue;
                    }
                    if (!player.checkAutoPlay(2, false)) {
                        continue;
                    }
                    playCommand(player, new ArrayList<>(), YzwdmjDisAction.action_hu);
                }
                return;
            } else {
                int action = 0, seat = 0;
                for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
                    seat = entry.getKey();
                    List<Integer> actList = YzwdmjDisAction.parseToDisActionList(entry.getValue());
                    if (actList == null) {
                        continue;
                    }

                    action = YzwdmjDisAction.getAutoMaxPriorityAction(actList);
                    YzwdmjPlayer player = seatMap.get(seat);
                    if (!player.checkAutoPlay(0, false)) {
                        continue;
                    }
                    boolean chuPai = false;
                    if (player.isAlreadyMoMajiang()) {
                        chuPai = true;
                    }
                    if (action == YzwdmjDisAction.action_peng) {
                        if (player.isAutoPlaySelf()) {
                            //自己开启托管直接过
                            playCommand(player, new ArrayList<>(), YzwdmjDisAction.action_pass);
                            if (chuPai) {
                                autoChuPai(player);
                            }
                        } else {
                            if (nowDisCardIds != null && !nowDisCardIds.isEmpty()) {
                                Yzwdmj mj = nowDisCardIds.get(0);
                                List<Yzwdmj> mjList = new ArrayList<>();
                                for (Yzwdmj handMj : player.getHandMajiang()) {
                                    if (handMj.getVal() == mj.getVal()) {
                                        mjList.add(handMj);
                                        if (mjList.size() == 2) {
                                            break;
                                        }
                                    }
                                }
                                playCommand(player, mjList, YzwdmjDisAction.action_peng);
                            }
                        }
                    } else {
                        playCommand(player, new ArrayList<>(), YzwdmjDisAction.action_pass);
                        if (chuPai) {
                            autoChuPai(player);
                        }
                    }
                }
            }
        } else {
            boolean finishPiaoFen=true;
            for(YzwdmjPlayer player:seatMap.values()){
                if(player.getPiaoFen()==-1){
                    finishPiaoFen=false;
                    break;
                }
            }
            if(piaoFenType>0&&!finishPiaoFen){
                for(YzwdmjPlayer player:seatMap.values()){
                    if(player.getPiaoFen()==-1){
                        if (player == null || !player.checkAutoPiaoFen()) {
                            continue;
                        }
                        piaoFen(player,0);
                    }
                }
            }else {
                YzwdmjPlayer player = seatMap.get(nowDisCardSeat);
                if (player == null || !player.checkAutoPlay(0, false)) {
                    return;
                }
                autoChuPai(player);
            }
        }
    }

    public void autoChuPai(YzwdmjPlayer player) {

        //YzwdmjQipaiTool.dropHongzhongVal(handMajiangs);红中麻将要去掉红中
//					int mjId = YzwdmjRobotAI.getInstance().outPaiHandle(0, handMjIds, new ArrayList<>());
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
            List<Yzwdmj> mjList = YzwdmjHelper.toMajiang(Arrays.asList(mjId));
            playCommand(player, mjList, YzwdmjDisAction.action_chupai);
        }
    }

    private int getMjIdNoWang(List<Integer> handMjIds,int num){
        int mjId = handMjIds.get(handMjIds.size() - num);
        if(mjId>200)
            return getMjIdNoWang(handMjIds,num+1);
        return mjId;
    }


    @Override
    public void createTable(Player player, int play, int bureauCount, Object... objects) throws Exception {
    }

    @Override
    public boolean isAllReady() {
        if (getPlayerCount() < getMaxPlayerCount()) {
            return false;
        }
        for (Player player : getSeatMap().values()) {
            if(!player.isRobot()){
                if(piaoFenType>1){
                    if (!(player.getState() == player_state.ready||player.getState() == player_state.play))
                        return false;
                }else {
                    if(player.getState() != player_state.ready)
                        return false;
                }
            }
        }
        if(finishFapai==1)
            return false;
        changeTableState(table_state.play);
        if (piaoFenType==4||(piaoFenType==5&&playedBureau==0)) {
            boolean piaoFenOver = true;
            for (YzwdmjPlayer player : playerMap.values()) {
                if(player.getPiaoFen()==-1){
                    piaoFenOver = false;
                    break;
                }
            }
            if(!piaoFenOver){
                if (finishFapai==0) {
                    LogUtil.msgLog.info("yzwdmj|sendPiaoFen|" + getId() + "|" + getPlayBureau());
                    ComRes msg = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_piaoFen).build();
                    for (YzwdmjPlayer player : playerMap.values()) {
                        if(player.getPiaoFen()==-1)
                            player.writeSocket(msg);
                    }
                }
                return false;
            }
        }else if(piaoFenType<=3&&piaoFenType>0){
            for (YzwdmjPlayer player : playerMap.values()) {
                player.setPiaoFen(piaoFenType);
                for (YzwdmjPlayer p : playerMap.values()) {
                    p.writeComMessage(WebSocketMsgType.res_code_broadcast_piaoFen, (int)player.getUserId(),player.getPiaoFen());
                }
            }
        }else if(piaoFenType==5&&playedBureau!=0){
            for (YzwdmjPlayer player : playerMap.values()) {
                for (YzwdmjPlayer p : playerMap.values()) {
                    p.writeComMessage(WebSocketMsgType.res_code_broadcast_piaoFen, (int)player.getUserId(),player.getPiaoFen());
                }
            }
        }
        return true;
    }

    public synchronized void piaoFen(YzwdmjPlayer player,int fen){
        if (piaoFenType<=3||player.getPiaoFen()!=-1)
            return;
        if(fen>3||fen<0)
            return;
        if(piaoFenType==5&&playBureau>1)
            return;
        player.setPiaoFen(fen);
        StringBuilder sb = new StringBuilder(versionCode+"|Yzwdmj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append("piaoFen").append("|").append(fen);
        LogUtil.msgLog.info(sb.toString());
        int confirmTime=0;
        for (Map.Entry<Integer, YzwdmjPlayer> entry : seatMap.entrySet()) {
            entry.getValue().writeComMessage(WebSocketMsgType.res_code_broadcast_piaoFen, (int)player.getUserId(),player.getPiaoFen());
            if(entry.getValue().getPiaoFen()!=-1)
                confirmTime++;
        }
        if (confirmTime == maxPlayerCount) {
            checkDeal(player.getUserId());
        }
    }

    // 是否两人麻将
    public boolean isTwoPlayer() {
        return getMaxPlayerCount() == 2;
    }

    public static final List<Integer> wanfaList = Arrays.asList(GameUtil.game_type_yzwdmj);

    public static void loadWanfaTables(Class<? extends BaseTable> cls) {
        for (Integer integer : wanfaList) {
            TableManager.wanfaTableTypesPut(integer, cls);
        }
        HuUtil.init();
    }

    public void logFaPaiTable() {
        StringBuilder sb = new StringBuilder();
        sb.append(versionCode+"|Yzwdmj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append("faPai");
        sb.append("|").append(playType);
        sb.append("|").append(maxPlayerCount);
        sb.append("|").append(getPayType());
        sb.append("|").append(lastWinSeat);
        LogUtil.msg(sb.toString());
    }

    public void logFaPaiPlayer(YzwdmjPlayer player, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append(versionCode+"|Yzwdmj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append("faPai");
        sb.append("|").append(player.getHandMajiang());
        sb.append("|").append(actListToString(actList));
        LogUtil.msg(sb.toString());
    }

    public void logAction(YzwdmjPlayer player, int action, List<Yzwdmj> mjs, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append(versionCode+"|Yzwdmj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        String actStr = "unKnown-" + action;
        if (action == YzwdmjDisAction.action_peng) {
            actStr = "peng";
        } else if (action == YzwdmjDisAction.action_minggang) {
            actStr = "baoTing";
        } else if (action == YzwdmjDisAction.action_chupai) {
            actStr = "chuPai";
        } else if (action == YzwdmjDisAction.action_pass) {
            actStr = "guo";
        } else if (action == YzwdmjDisAction.action_angang) {
            actStr = "anGang";
        } else if (action == YzwdmjDisAction.action_chi) {
            actStr = "chi";
        }
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append(player.isAutoPlaySelf() ? 1 : 0);
        sb.append("|").append(actStr);
        sb.append("|").append(mjs);
        sb.append("|").append(actListToString(actList));
        LogUtil.msg(sb.toString());
    }

    public void logGuoWang(YzwdmjPlayer player,List<Integer> acts) {
        StringBuilder sb = new StringBuilder();
        sb.append(versionCode+"|Yzwdmj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        String actStr = "guoWang-";
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append(player.isAutoPlaySelf() ? 1 : 0);
        sb.append("|").append(actStr);
        sb.append("|").append(wangActListToString(acts));
        LogUtil.msg(sb.toString());
    }

    public void logMoMj(YzwdmjPlayer player, Yzwdmj mj, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append(versionCode+"|Yzwdmj");
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

    public void logChuPaiActList(YzwdmjPlayer player, Yzwdmj mj, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append(versionCode+"|Yzwdmj");
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

    public void logActionHu(YzwdmjPlayer player, List<Yzwdmj> mjs, String daHuNames) {
        StringBuilder sb = new StringBuilder();
        sb.append(versionCode+"|Yzwdmj");
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

    public void logActionHuWang(YzwdmjPlayer player, List<Yzwdmj> mjs, String daHuNames,int act) {
        StringBuilder sb = new StringBuilder();
        sb.append(versionCode+"|Yzwdmj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        if(act==YzwdmjDisAction.action_wangchuang){
            sb.append("|").append("wangchuang");
        }else if(act == YzwdmjDisAction.action_wangdiao){
            sb.append("|").append("wangdiao");
        }
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append(player.isAutoPlaySelf() ? 1 : 0);
        sb.append("|").append(mjs);
        sb.append("|").append(player.getWangAct().toString());
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
                if (i == YzwdmjConstants.ACTION_INDEX_HU) {
                    sb.append("hu");
                } else if (i == YzwdmjConstants.ACTION_INDEX_PENG) {
                    sb.append("peng");
                } else if (i == YzwdmjConstants.ACTION_INDEX_MINGGANG) {
                    sb.append("mingGang");
                } else if (i == YzwdmjConstants.ACTION_INDEX_ANGANG) {
                    sb.append("anGang");
                } else if (i == YzwdmjConstants.ACTION_INDEX_CHI) {
                    sb.append("chi");
                } else if (i == YzwdmjConstants.ACTION_INDEX_ZIMO) {
                    sb.append("ziMo");
                } else if (i == 6) {
                    sb.append("wangChuan");
                }else if (i == 7) {
                    sb.append("wangDiao");
                }
            }
        }
        sb.append("]");
        return sb.toString();
    }

    public String wangActListToString(List<Integer> actList) {
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
                if (i == 0) {
                    sb.append("wangChuan");
                }else if (i == 1) {
                    sb.append("wangDiao");
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

    /**
     * 是否流局
     *
     * @return
     */
    public int isLiuJu() {
        return (huConfirmList.size() == 0 && leftMajiangs.size() == 0) ? 1 : 0;
    }


    public void sendTingInfo(YzwdmjPlayer player) {
        if (player.isAlreadyMoMajiang()) {
            if (actionSeatMap.containsKey(player.getSeat())) {
                return;
            }
            DaPaiTingPaiRes.Builder tingInfo = DaPaiTingPaiRes.newBuilder();
            List<Yzwdmj> cards = new ArrayList<>(player.getHandMajiang());
            int hzCount = YzwdmjTool.dropHongzhong(cards).size();
            int[] cardArr = HuUtil.toCardArray(cards);
            Map<Integer, List<Yzwdmj>> checked = new HashMap<>();
            for (Yzwdmj card : cards) {
                List<Yzwdmj> lackPaiList;
                if (checked.containsKey(card.getVal())) {
                    lackPaiList = checked.get(card.getVal());
                } else {
                    int cardIndex = HuUtil.getMjIndex(card);
                    cardArr[cardIndex] = cardArr[cardIndex] - 1;
                    lackPaiList = YzwdmjTool.getLackList(cardArr, hzCount, hu7dui==1);
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
                    ting.addTingMajiangIds(Yzwdmj.mj201.getId());
                }else {
                    for (Yzwdmj lackPai : lackPaiList) {
                        ting.addTingMajiangIds(lackPai.getId());
                    }
                }
                tingInfo.addInfo(ting.build());
            }
            if (tingInfo.getInfoCount() > 0) {
                player.writeSocket(tingInfo.build());
            }
        } else {
            List<Yzwdmj> cards = new ArrayList<>(player.getHandMajiang());
            int hzCount = YzwdmjTool.dropHongzhong(cards).size();
            int[] cardArr = HuUtil.toCardArray(cards);
            List<Yzwdmj> lackPaiList = YzwdmjTool.getLackList(cardArr, hzCount, hu7dui==1);
            if (lackPaiList == null || lackPaiList.size() == 0) {
                return;
            }
            TingPaiRes.Builder ting = TingPaiRes.newBuilder();
            if (lackPaiList.size() == 1 && null == lackPaiList.get(0)||lackPaiList.size()>=26) {
                //听所有
                ting.clear();
                ting.addMajiangIds(Yzwdmj.mj201.getId());
            } else {
                for (Yzwdmj lackPai : lackPaiList) {
                    ting.addMajiangIds(lackPai.getId());
                }
//                ting.addMajiangIds(Yzwdmj.mj201.getId());
            }
            player.writeSocket(ting.build());
        }
    }

    public String getTableMsg() {
        Map<String, Object> json = new HashMap<>();
        json.put("wanFa", "永州王钓麻将");
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
        return "永州王钓麻将";
    }
}
