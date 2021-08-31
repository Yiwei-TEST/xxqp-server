package com.sy599.game.websocket.constant;

public class WebSocketMsgType {

    /**
     *
     */
    public static final short sc_activity = 8000;
    public static final short sc_single_activity = 8001;
    public static final short sc_task = 8002;
    public static final short sc_rank = 8003;
    public static final short sc_redbag = 8004;
    public static final short sc_kefuhao = 8005;
    public static final short sc_mgPlayerInfo = 8006;

    public static final short sc_bairen_tableInfo = 8007;
    public static final short sc_bairen_trend = 8008;
    public static final short sc_bairen_bet = 8009;

    /**
     * 前台发送msgType消息
     **/
    public static final short union_login = 1000;
    public static final short cs_login = 1001;
    public static final short cs_com = 1002;
    public static final short cs_play = 1003;
    public static final short cs_diss = 1004;
    /**
     * 精彩活动
     */
    public static final int cs_activity = 1005;
    /**
     * 任务
     */
    public static final int cs_task = 1006;
    /**
     * 排行榜
     */
    public static final int cs_rank = 1007;
    /**
     * 现金红包活动
     */
    public static final int cs_redbag = 1008;
    /**
     * 客服号获取
     */
    public static final int cs_kefuhao = 1009;
    /**
     * 芒果玩家用户信息获取
     */
    public static final int cs_mg_playerInfo = 1010;
    /**
     * 王者千分转盘活动
     */
    public static final int cs_luck_redbag = 1011;

    /**
     * 低限制条件的退出金币场房间（每小局结束后，可退出，无需房间大结算）
     */
    public static final int cs_quit_gold_room = 1012;

    /**
     * 加入百人玩法房间
     */
    public static final int cs_join_bairen_table = 1013;

    /**
     * 玩家主动退出百人玩法房间
     */
    public static final int cs_quit_bairen_table = 1014;

    /**
     * 端午活动金币场奖励消息
     */
    public static int cs_duanwu_gold_room_acitiviyt_reward = 1015;

    /**
     * 端午活动金币场排行榜消息
     */
    public static final int cs_duanwu_gold_room_acitiviyt = 1016;

    /**
     * 7xi活动金币场奖励消息
     */
    public static int cs_7xi_gold_room_acitiviyt_reward = 1019;

    /**
     * 7xi活动金币场排行榜消息
     */
    public static final int cs_7xi_gold_room_acitiviyt = 1020;
    public static final int cs_7xi_gold_room_calcpushmsg = 1021;

    /** 登录错误消息提示 */
    public static final int sc_err_login = 3;

    /**
     * 返回给前台的msgType消息
     **/
    public static final short union_login_success= 2000;//com
    public static final short union_login_fail = 2001;//com
    public static final short sc_com = 5001;
    public static final short sc_createtable = 5002;
    public static final short sc_jointable = 5003;
    public static final short sc_dealcards = 5004;
    public static final short sc_playcardres = 5005;
    public static final short sc_closinginfores = 5006;
    public static final short sc_ping = 5007;
    public static final short sc_message = 5008;
    public static final short sc_playmajiang = 5009;
    public static final short sc_momajiang = 5010;
    public static final short sc_csgangplay = 5011;
    public static final short sc_gameSite = 5012;
    public static final short sc_playpaohuzi = 5013;
    public static final short sc_closingphzinfores = 5014;
    public static final short sc_closingmjinfores = 5015;
    public static final short sc_closingyjmjinfores = 5016;
    public static final short sc_closingghzinfores = 5017;
    public static final short sc_bufamomajiang = 5018;
    public static final short sc_tingpaiinfo = 5019;
    public static final short sc_dapaitingpaiinfo = 5020;
    public static final short sc_groupTableList = 5021;
    public static final short sc_userHeadImgList = 5022;
    public static final short sc_cqxzmjBoard = 5023;
    public static final short sc_goldRoomArea = 5024;
    public static final short sc_goldRoomConfig = 5025;
    public static final short sc_config = 5026;
    public static final short sc_mission = 5027;
    public static final short sc_goldRoomHall = 5028;
    public static final short sc_activeLZ = 5029;
    public static final short sc_activeQueQiao = 5030;
    public static final short sc_queQiaoInviteBoard = 5031;

    /**
     * 聊天
     */
    public static final short sc_chat = 5000;
    public static final short sc_gold_chat = 4999;


	//活动场,主消息,参数内0: 1刷新活动人数, 2结算消息展示
	public static final short competition_msg_refresh = 6000;
	//结算
	public static final short competition_msg_clearing = 6001;
	//赛前推送
	public static final short competition_msg_before_playing = 6002;
	//开赛推送
	public static final short competition_msg_begin_playing = 6003;
	//房间内排名推送
	public static final short competition_msg_rank = 6004;
	//回到报名界面
	public static final short competition_msg_back_sign = 6005;

	//流局
	public static final short competition_msg_case_off = 6006;


    // ///////////////////////////////////////////////////////////////////

    /**
     * 红点提示@author lc
     */
    public enum TipsEnum {
        message(1), draw(2);
        private int id;

        private TipsEnum(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

    }

    /**
     * 比赛场状态
     **/
    public static final int sc_code_gamestate = 20;

    /**
     * 比赛场分配牌桌ID
     **/
    public static final int sc_code_gamesitetableid = 21;

    /**
     * 比赛结束弹框
     **/
    public static final int sc_code_endgamesite = 101;

    /**
     * 比赛场推送消息
     **/
    public static final int sc_code_gamesitepush = 102;

    /**
     * 推送比赛场报名人数
     **/
    public static final int sc_code_pushapplynumber = 103;

    /**
     * 推送比赛场等待界面
     **/
    public static final int sc_code_pushwaitview = 104;

    // /sc_code_err 对应;

    /**
     * 账号冲突
     **/
    public static final int sc_code_err_login = 1;
    /**
     * 重连进入房间错误
     **/
    public static final int sc_code_err_table = 2;
    /**
     * checkcode err
     **/
    public static final int sc_code_err_checkcode = 3;

    /***  防作弊限制***/
    public static final int sc_code_err_ipOrGps = 4;

    // --------------------------------------------推送
    /** 返回给前台通用的code消息 **/
    /**
     * 通用提示
     */
    public static final int res_code_com = 0;
    /**
     * 登录
     **/
    public static final int res_code_login = 1;
    /**
     * 状态
     **/
    public static final int res_code_state = 2;
    /**
     * 错误信息
     **/
    public static final int res_code_err = 3;
    /**
     * 发送解散房间
     **/
    public static final int res_code_senddisstable = 4;
    /**
     * 应答解散房间
     **/
    public static final int res_code_disstable = 5;
    /**
     * 应答不解散房间
     **/
    public static final int res_code_nodisstable = 6;
    /**
     * 聊天
     **/
    public static final int res_code_chat = 7;
    /**
     * 房间在线
     **/
    public static final int res_code_isonlinetable = 8;
    /**
     * 开始下一局
     **/
    public static final int res_code_isstartnext = 9;
    /**
     * 房卡更新
     **/
    public static final int res_code_cards = 10;
    /**
     * 进入大转盘
     **/
    public static final int res_code_drawlotteryenter = 11;
    /**
     * 大转盘抽奖
     **/
    public static final int res_code_drawlottery = 12;
    /**
     * 领取活动奖励
     **/
    public static final int res_code_getActivityAward = 13;
    /**
     * 退出房间
     **/
    public static final int res_code_tablequit = 14;
    /**
     * 重启
     **/
    public static final int res_code_shutdown = 15;
    /**
     * 战绩
     **/
    public static final int res_code_record = 16;
    /**
     * 长沙海底捞月
     **/
    public static final int res_code_asklastmajiang = 17;
    // /** 长沙海底捞月 **/
    // public static final int sc_code_lastmajiang = 18;
    /**
     * 提示
     **/
    public static final int res_code_tips = 18;
    /**
     * 跑马灯
     **/
    public static final int res_code_marquee = 19;
    /**
     * 斗牛提示前台下注
     **/
    public static final int res_code_dn_bet = 22;
    /**
     * 斗牛提示前台状态
     **/
    public static final int res_code_dn_state = 23;
    /**
     * 斗牛提示前台抢庄
     **/
    public static final int res_code_dn_robbanker = 24;
    /**
     * 斗牛提示前台让庄
     **/
    public static final int res_code_dn_letbanker = 25;
    /**
     * 斗牛提示前台定庄
     **/
    public static final int res_code_dn_banker = 26;
    /**
     * 斗牛提示前台定庄
     **/
    public static final int res_code_dn_showcard = 27;
    /**
     * 斗牛取消托管
     */
    public static final int res_code_dn_offAutoplay = 77;
    /**
     * 跑胡子--放招推送
     **/
    public static final int res_code_phz_fangzhao = 28;
    /**
     * 推送选择放招
     **/
    public static final int res_com_code_fangzhao = 29;
    /**
     * 跑胡子补牌
     **/
    public static final int res_com_code_phzbupai = 30;
    /**
     * 代开房间
     **/
    public static final int res_com_code_daikaitable = 31;

    /**
     * 三公提示前台下注
     **/
    public static final int res_code_sg_bet = 32;
    /**
     * 三公提示前台状态
     **/
    public static final int res_code_sg_state = 33;
    /**
     * 三公提示前台开牌
     **/
    public static final int res_code_sg_showcard = 34;
    /**
     * 取最佳服务器(推送)
     **/
    public static final int res_code_getserverid = 35;
    /**
     * 经纬度(推送)
     **/
    public static final int res_code_latitudeandlongitude = 36;
    /**
     * 客户端更新经纬度(推送)
     **/
    public static final int res_com_update_latitudeandlongitude = 251;
    /**
     * 向其他玩家推送当前玩家信息（userId、seat、语音id）(推送)
     **/
    public static final int res_code_gotye = 37;
    /**
     * 推送解绑消息
     **/
    public static final int res_code_removebind = 38;
    /**
     * 百人玩法状态推送
     */
    public static final int res_com_bairen_table_state = 39;
    /**
     * 百人玩法结算推送
     */
    public static final int res_com_hw_account = 41;
    /**
     * 百人玩法上庄推送
     */
    public static final int res_com_hw_shangZhuang = 55;

    /**
     * 信用分更新
     **/
    public static final int res_code_credit_update = 56;

    /**
     * 金币更新
     **/
    public static final int res_code_coin = 57;

    /**
     * 牌桌内的金币更新
     **/
    public static final int res_code_coin_table_start = 58;
    /**
     * 牌桌内的输赢金币
     **/
    public static final int res_code_coin_table_over = 59;

    /**
     * 头像变更
     **/
    public static final int res_code_headimgurl = 60;

    /**
     * 金币场匹配超时
     **/
    public static final int res_code_goldRoomMatchTimeOut = 61;

    /**
     * 金币场切服务器(推送)
     **/
    public static final int res_code_goldRoomMatchCrossServer= 62;

    /**
     * 金币场:成功加入匹配
     **/
    public static final int res_code_joinGoldRoomMatch= 63;
    /**
     * 金币场:成功离开匹配
     **/
    public static final int res_code_quitGoldRoomMatch= 64;
    /**
     * 推送玩家资源信息
     **/
    public static final int res_com_resource = 888;

    /**
     * 切换金币场
     **/
    public static final int res_com_gold_exchangegoldsite = 901;
    /**
     * 金币玩家修改信息
     **/
    public static final int res_com_gold_golduserchange = 902;
    /**
     * 金币更新
     **/
    public static final int res_com_gold = 903;
    /**
     * 推送领取救济金弹框
     **/
    public static final int res_com_sendremedy = 904;
    /**
     * 推送踢人弹框
     **/
    public static final int res_com_leaveplayer = 905;
    /**
     * 领取救济金结果
     **/
    public static final int res_com_drawremedyresult = 906;
    /**
     * 推送充值弹框
     **/
    public static final int res_com_sendrecharge = 907;
    /**
     * 推送分享领金币弹框
     **/
    public static final int res_com_sendshare = 908;
    /**
     * 芒果跑得快推送玩家段位等级
     **/
    public static final int res_com_sendGrade = 909;

    /**
     * 比赛场托管
     **/
    public static final int res_com_code_trusteeship = 105;
    /**
     * 比赛场人数不足解散
     **/
    public static final int res_com_code_dismissmatch = 106;
    /**
     * 切牌
     **/
    public static final int res_com_code_cutcard = 110;
    /**
     * 同意解散房间
     **/
    public static final int res_com_code_agreediss = 111;
    /**
     * 领取到了红包
     **/
    public static final int res_com_code_hb = 112;
    /**
     * 代开房间房主id
     **/
    public static final int res_com_code_daikaimasterid = 113;
    /**
     * 当月签到信息
     **/
    public static final int res_code_signinfo = 114;
    /**
     * 签到
     **/
    public static final int res_code_sign = 115;
    /**
     * 当月签到信息
     **/

    /**
     * 一般消息
     **/
    public static final int req_code_comMsg = 1111;
    public static final int res_code_getGoldenBeans = 1111;
    public static final int res_code_goldsigninfo = 1114;
    /**
     * 签到
     **/
    public static final int req_code_goldsign = 1115;
    public static final int res_code_goldsign = 1115;
    /**
     * 破产补助
     **/
    public static final int getRes_code_sendbrokemsg = 1116;
    /**
     *领取破产奖励
     **/
    public static final int res_code_borkeaward = 1119;
    /**
     *领取破产奖励
     **/
    public static final int res_code_borkeawardVidea = 1127;
    /**
     *领取破产奖励
     **/
    public static final int res_code_buygold = 1120;

    /**
     *领取视频奖励
     **/
    public static final int req_code_videoAward = 1125;
    /**
     *领取视频奖励
     **/
    public static final int req_code_getVideoAwardMsg = 1126;
    
    
    /**
     * 领取累计胜利奖励
     */
    public static final int res_code_getWinReward=1130;
    
    /**
     * 打开累计胜利数据
     */
    public static final int res_code_getWinRewardInfo=1128;
    
    
    /**
     * 推送可领取累计胜利奖励
     */
    public static final int res_code_sendWinReward=1129;
    /**
     * 金币和任务相关
     **/
    public static final int res_code_missionabout = 1117;
    /**
     * 活动相关
     **/
    public static final int res_code_active = 1150;
    public static final int res_code_receiveawardLZ= 1151;
    /**
     * 鹊桥活动
     */
    public static final int res_code_QueQiaoInvite= 1152;
    public static final int res_code_QueQiaoAllowInvite= 1153;
    public static final int res_code_receiveawardQueQiao= 1154;
    public static final int res_code_QueQiaoRedCom= 1155;

    /**
     * 发送破产提示
     **/
    public static final int res_code_brokeshare = 1118;
    /**
     *领取任务奖励成功
     **/
    public static final int res_code_receiveaward= 1122;
    public static final int RES_CUT_CARDS_COMMAND= 1121;
    /**
     *任务红点消息
     **/
    public static final int res_code_missionRedCom= 1123;
    /**
     *龙舟红点消息
     **/
    public static final int res_code_lzRedCom= 1124;

    /**
     * 金币场结算 输赢超1000以上 可观看广告视频消息
     */
    public static final int res_code_goldRoomSendWatchAdsMsg= 1132;

    /**
     * 金币场结算 输赢超1000以上 拿得奖消息
     */
    public static final int res_code_goldRoomWatchAdsReward= 1133;

    /**
     * 大厅 赠送金币按钮 权限消息
     */
    public static final int res_code_permission= 1141;

    /**
     * 弹出绑定邀请码的弹框
     **/
    public static final int res_code_bindout = 116;
    /**
     * 换牌
     **/
    public static final int res_com_code_changecard = 117;
    /**
     * 翻牌
     **/
    public static final int res_com_code_draw = 118;
    /**
     * 代开房间解散
     **/
    public static final int res_com_code_dissdaikai = 119;

    /**
     * 观察者状态--发牌
     */
    public static final int res_com_code_observer = 120;

    /**
     * 观察者状态--刷新
     */
    public static final int res_com_code_observer_state = 121;
    /**
     * 七日签到--芒果
     **/
    public static final int res_code_seven_sign = 133;

    /** 升级提示前台抢庄成功 **/

    /**
     * 长春麻将托管
     **/
    public static final int res_com_code_cccanceltrusteeship = 301;

    /**
     * 十点半提示前台下注
     **/
    public static final int res_code_tenthirty_bet = 41;
    /**
     * 十点半提示前台状态
     **/
    public static final int res_code_tenthirty_state = 42;
    /**
     * 十点半提示前台抢庄
     **/
    public static final int res_code_tenthirty_robbanker = 43;
    /**
     * 十点半提示前台让庄
     **/
    public static final int res_code_tenthirty_letbanker = 44;
    /**
     * 十点半提示前台定庄
     **/
    public static final int res_code_tenthirty_banker = 45;
    /**
     * 十点半提示前台显示牌
     **/
    public static final int res_code_tenthirty_showcard = 46;
    /**
     * 十点半提示前台要牌
     **/
    public static final int res_code_tenthirty_wantcard = 47;
    /**
     * 十点半提示前台随机切出一张牌
     **/
    public static final int res_code_tenthirty_cutpai = 48;
    /**
     * 十点半提示前台爆牌
     **/
    public static final int res_code_tenthirty_boompai = 49;
    /**
     * 十点半提示前台下一个要牌的位置
     **/
    public static final int res_code_tenthirty_nextSeat = 50;
    /**
     * 十点半提示前台局外人的位置集合
     **/
    public static final int res_code_tenthirty_outer = 51;
    /**
     * 十点半提示前台亮牌
     **/
    public static final int res_code_tenthirty_liangpai = 52;
    /**
     * 十点半提示前台洗牌
     **/
    public static final int res_code_tenthirty_washpai = 53;
    /**
     * 十点半提示前台摇点
     **/
    public static final int res_code_threemonkeys_rock = 54;

    /**
     * 挖坑提示前台叫分
     **/
    public static final int res_code_wk_bet = 91;
    /**
     * 挖坑提示前台挖坑者消息
     **/
    public static final int res_code_wk_digger = 92;
    /**
     * 挖坑提示前台状态
     **/
    public static final int res_code_wk_state = 93;
    /**
     * 挖坑提示前台开始出牌
     **/
    public static final int res_code_wk_startGame = 94;


    /**
     * 扬沙子提示前台下注
     **/
    public static final int res_code_ysz_bet = 61;
    /**
     * 扬沙子提示前台状态
     **/
    public static final int res_code_ysz_state = 62;
    /**
     * 扬沙子提示前台看牌
     **/
    public static final int res_code_ysz_seecards = 63;
    /**
     * 扬沙子提示前台弃牌
     **/
    public static final int res_code_ysz_pass = 64;
    /**
     * 扬沙子提示前台下一个玩家出牌的位置
     **/
    public static final int res_code_ysz_nextseat = 65;
    /**
     * 扬沙子提示比牌结果
     **/
    public static final int res_code_ysz_compare = 66;
    /**
     * 扬沙子提示开始游戏
     **/
    public static final int res_code_ysz_startgame = 67;
    /**
     * 扬沙子提示前台每个玩家可操作筹码的信息
     **/
    public static final int res_code_ysz_avaliblebet = 68;
    /**
     * 扬沙子提示前台自动跟注结果
     **/
    public static final int res_code_ysz_autofollow = 69;
    /**
     * 扬沙子提示前台开牌结果
     **/
    public static final int res_code_ysz_open = 70;
    /**
     * 扬沙子提示前台打牌的轮数
     **/
    public static final int res_code_ysz_discardround = 71;
    /**
     * 扬沙子定庄
     **/
    public static final int res_code_ysz_checkbanker = 72;

    /**
     * 斗地主提示前台切牌
     **/
    public static final int res_code_ddz_cutcard = 81;
    /**
     * 斗地主提示前台叫地主，抢地主，不抢结果
     **/
    public static final int res_code_ddz_roblandlord = 82;
    /**
     * 斗地主提示前台阶段
     **/
    public static final int res_code_ddz_phase = 83;
    /**
     * 斗地主提示前台倍数
     **/
    public static final int res_code_ddz_ratio = 84;
    /**
     * 斗地主提示前台地主
     **/
    public static final int res_code_ddz_surelandlord = 85;
    /**
     * 斗地主提示前台出牌
     **/
    public static final int res_code_ddz_discard = 86;
    /**
     * 斗地主提示前台春天
     **/
    public static final int res_code_ddz_spring = 87;
    /**
     * 斗地主提示前台押注(如山西斗地主 踢)
     **/
    public static final int res_code_ddz_bet = 88;
    /**
     * 斗地主提示前台明牌
     **/
    public static final int res_code_ddz_mingPai = 89;
    /**
     * 龙虎斗押注
     */
    public static final int res_code_lhd_bet = 90;

    /**
     * 三皮提示前台状态
     **/
    public static final int res_code_sp_state = 401;
    /**
     * 三皮提示前台发牌
     **/
    public static final int res_code_sp_fapai = 402;
    /**
     * 三皮提示前台跟注，反踢，下注情况
     **/
    public static final int res_code_sp_bet = 403;
    /**
     * 三皮提示前台下一个玩家的位置情况
     **/
    public static final int res_code_sp_nextseat = 404;
    /**
     * 三皮提示前台玩家的弃牌情况
     **/
    public static final int res_code_sp_pass = 405;
    /**
     * 三皮提示前台看牌结果
     **/
    public static final int res_code_sp_seecard = 406;
    /**
     * 三皮提示前台飞牌
     **/
    public static final int res_code_sp_fly = 407;
    /**
     * 三皮提示前台打骰
     **/
    public static final int res_code_sp_datou = 408;

    /**
     * 跑胡子禁止出牌
     **/
    public static final int res_code_phz_dis_err = 301;
    /**
     * 跑胡子禁止出牌
     **/
    public static final int res_code_phz_dis_err_action = 3011;
    /**
     * 张掖麻将抛分下注
     **/
    public static final int res_com_zymj_paoFen = 302;
    /**
     * 张掖麻将甩牌已选定
     **/
    public static final int res_com_zymj_shuaiPai = 303;
    /**
     * 张掖麻将玩家都甩完牌后通知庄家出牌
     **/
    public static final int res_com_zymj_ask_dismajiang = 304;
    /**
     * 庆阳滑水麻将设置鱼子
     **/
    public static final int res_com_qyhs_yuzi = 305;
    /**
     * 秦安麻将每局结束换嘴子
     **/
    public static final int res_com_qamj_huanZuizi = 306;
    /**
     * 安化字牌听牌
     */
    public static final int RES_COM_AHPHZ_TING = 307;
    /**
     * 安化麻将听牌
     */
    public static final int RES_COM_AH_MAHJONG_TING = 308;
    // --------------------------------------------接口

    /** com消息 **/
    /**
     * 长沙麻将海底捞
     **/
    public static final int req_com_lastmajiang = 215;
    /**
     * 斗牛抢庄
     **/
    public static final int req_com_dn_robbanker = 16;
    /**
     * 斗牛让庄
     **/
    public static final int req_com_dn_letbanker = 17;
    /**
     * 斗牛下注
     **/
    public static final int req_com_dn_bet = 18;
    /**
     * 斗牛游戏开始
     **/
    public static final int req_com_dn_startgame = 19;
    /**
     * 斗牛亮牌
     **/
    public static final int req_com_dn_showcard = 20;
    /**
     * 斗牛 玩家托管设置
     */
    public static final int req_com_dn_autoplay = 35;
    /**
     * 斗牛 玩家搓牌
     */
    public static final int req_com_dn_cuopai = 36;
    /**
     * 斗牛 智能托管
     */
    public static final int req_com_dn_smartAutoPlay = 37;
    /**
     * 相同IP解散房间
     **/
    public static final int req_com_sameip_dissroom = 21;
    /**
     * 进入切牌
     **/
    public static final int req_com_enter_cutcard = 22;
    /**
     * 切牌
     **/
    public static final int req_com_cutcard = 23;
    /**
     * 跑胡子放招
     **/
    public static final int req_com_fangzhao = 24;
    /**
     * 代开房间
     **/
    public static final int req_com_daikaitable = 25;

    /**
     * 三公游戏开始
     **/
    public static final int req_com_sg_startgame = 26;
    /**
     * 三公下注
     **/
    public static final int req_com_sg_bet = 27;
    /**
     * 三公游戏开始
     **/
    public static final int req_com_sg_showcard = 28;
    /**
     * 玩家更新经纬度
     **/
    public static final int req_com_latitudeandlongitude = 30;

    /**
     * 向其他玩家推送当前玩家信息（userId、seat、语音id）
     **/
    public static final int req_com_gotye = 31;

    /**
     * 换牌请求
     **/
    public static final int req_com_changecard = 32;
    /**
     * 斗牛翻牌
     **/
    public static final int req_com_dn_draw = 33;
    /**
     * 龙虎斗玩家列表
     **/
    public static final int req_com_lhd_playerlist = 34;

    /**
     * 切换金币场
     **/
    public static final int req_com_goldsite = 901;
    /**
     * 金币玩家修改信息
     **/
    public static final int req_com_golduserchange = 902;
    /**
     * 领取救济金
     **/
    public static final int req_com_drawremedy = 903;
    /**
     * 金币场聊天室
     **/
    public static final int req_com_goldchat = 904;

    /**
     * 登陆金币场
     **/
    public static final int req_com_gold_login = 905;

    /**
     * 金币场兑换
     **/
    public static final int req_com_gold_exchange = 906;

    /**
     * 金币场兑换商品品项
     **/
    public static final int res_code_gold_exchange_items = 1907;

    /**
     * 金币场场次信息
     **/
    public static final int res_code_gold_room_level = 1908;

    /**
     * 金币场弃牌
     **/
    public static final int req_com_golddrop = 920;

    /**
     * 俱乐部牌桌
     **/
    public static final int req_com_group_tables = 95;

    /**
     * 俱乐部配置信息
     **/
    public static final int req_com_group_table_config = 134;

    /**
     * 俱乐部房间列表
     **/
    public static final int req_com_group_table_list = 135;

    /**
     * 俱乐部房间列表
     **/
    public static final int req_com_group_table_list_new = 136;


    /**
     * 金币场相关
     **/
    public static final int req_com_gold_room_command = 137;
    /**
     * solo相关
     **/
    public static final int req_com_solo_room_command = 138;
    /**
     * 金币场端午活动2020年6月22日
     **/
    public static final int req_com_gold_room_activity_command = 139;


    /**
     * 俱乐部牌桌
     **/
    public static final int res_com_group_tables = 95;

    /**
     * 俱乐部配置信息
     **/
    public static final int res_com_group_table_config = 134;
    /**
     * 俱乐部房间列表
     **/
    public static final int res_com_group_table_list = 135;

    /**
     * 俱乐部牌桌详细信息
     **/
    public static final int req_com_group_table_msg = 96;

    /**
     * 俱乐部匹配
     **/
    public static final int req_com_group_match = 97;

    /**
     * 俱乐部换桌
     **/
    public static final int com_group_change_room = 98;

    /**
     * 俱乐部房间踢人
     **/
    public static final int com_group_room_fire_player = 99;

    /**
     * 俱乐部房间邀请
     **/
    public static final int com_group_room_invite = 80;

    /**
     * 俱乐部牌桌详细信息
     **/
    public static final int res_com_group_table_msg = 96;

    /**
     * 晋级赛
     **/
    public static final int req_com_match_code = 100;

    /**
     * 晋级赛消息
     */
    public static final int req_com_match_msg_code = 107;

    /**
     * 晋级赛排名
     **/
    public static final int req_com_match_rank_code = 200;

    /**
     * 服务器转发通知客户端更新经纬度
     **/
    public static final int req_com_update_latitudeandlongitude = 250;

    /**
     * 甘肃升级抢庄
     **/
    public static final int req_com_sj_robBanker = 301;
    /**
     * 甘肃升级反主
     **/
    public static final int req_com_sj_antiprincipal = 302;
    /**
     * 甘肃升级自保
     **/
    public static final int req_com_sj_selfProtect = 303;
    /**
     * 甘肃升级无主
     **/
    public static final int req_com_sj_noBanker = 304;
    /**
     * 甘肃升级扣底
     **/
    public static final int req_com_sj_changeUnderCards = 305;
    /**
     * 卡二条麻将通知庄家出牌
     **/
    public static final int req_com_kt_ask_dismajiang = 306;
    /**
     * 张掖麻将抛分下注
     **/
    public static final int req_com_zymj_paoFen = 307;
    /**
     * 秦安麻将每局结束换嘴子
     **/
    public static final int req_com_qamj_huanZuizi = 308;
    /**
     * 麻将同意洗牌
     **/
    public static final int res_com_code_majiang_canel_xiPai = 309;
    /**
     * 麻将不同意洗牌
     */
    public static final int res_com_code_majiang_agree_xiPai = 310;
    /**
     * 比赛场等待界面
     **/
    public static final int req_com_code_gamestate = 201;
    /**
     * 比赛场取消托管
     **/
    public static final int req_com_code_canceltrusteeship = 202;
    /**
     * 托管
     **/
    public static final int req_com_code_cccanceltrusteeship = 203;
    /**
     * 麻将洗牌功能
     **/
    public static final int req_com_code_majiang_xiPai = 204;
    /**
     * 取最佳服务器
     **/
    public static final int req_com_getserverid = 29;
    /**
     * 十点半抢庄
     **/
    public static final int req_com_tenthirty_robbanker = 41;
    /**
     * 十点半让庄
     **/
    public static final int req_com_tenthirty_letbanker = 42;
    /**
     * 十点半下注
     **/
    public static final int req_com_tenthirty_bet = 43;
    /**
     * 十点半游戏开始
     **/
    public static final int req_com_tenthirty_startgame = 44;
    /**
     * 十点半游戏完成牌显示
     **/
    public static final int req_com_tenthirty_showcard = 45;
    /**
     * 十点半游戏要牌
     **/
    public static final int req_com_tenthirty_wantcard = 46;
    /**
     * 三猴子摇色子
     **/
    public static final int req_com_threemonkeys_rock = 47;

    /**
     * 挖坑叫分
     **/
    public static final int req_com_wk_bet = 51;
    /**
     * 挖坑游戏开始出牌
     **/
    public static final int req_com_wk_startGame = 52;


    /**
     * 扬沙子下注
     **/
    public static final int req_com_ysz_bet = 61;
    /**
     * 扬沙子看牌
     **/
    public static final int req_com_ysz_seeCards = 62;
    /**
     * 扬沙子弃牌
     **/
    public static final int req_com_ysz_pass = 63;
    /**
     * 扬沙子开始游戏
     **/
    public static final int req_com_ysz_startgame = 64;
    /**
     * 扬沙子自动跟注
     **/
    public static final int req_com_ysz_autofollow = 65;


    /**
     * 斗地主切牌
     **/
    public static final int req_com_ddz_cutcard = 81;
    /**
     * 抢地主(叫地主，不抢)
     **/
    public static final int req_com_ddz_roblandlord = 82;

    /**
     * 山西斗地主踢(押注)
     */
    public static final int req_com_sxddz_bet_ti = 83;

    /**
     * 山西斗地主明牌
     */
    public static final int req_com_sxddz_mingPai = 84;

    /**
     * 三皮开始游戏
     **/
    public static final int req_com_sp_startgame = 401;
    /**
     * 三皮弃牌
     **/
    public static final int req_com_sp_pass = 402;
    /**
     * 三皮跟注，反踢，下注
     **/
    public static final int req_com_sp_bet = 403;
    /**
     * 三皮看牌
     **/
    public static final int req_com_sp_seecard = 404;
    /**
     * 三皮飞牌
     **/
    public static final int req_com_sp_fly = 405;
    /**
     * 三皮打骰
     **/
    public static final int req_com_sp_datou = 406;
///////////////以下是升级的/////////////////////
//客户端发送到服务器的消息
	/**
	 * 亮主
	 */
	public static final int REQ_COM_SJ_LIANGZHU = 90;
	/**
	 * 反主
	 */
	public static final int REQ_COM_SJ_FANZHU = 91;
	/**
	 * 炒底
	 */
	public static final int REQ_COM_SJ_CHAODI = 92;
	/**
	 * 埋底牌
	 */
	public static final int REQ_COM_SJ_MAIDI = 93;
	/**
	 * 放弃抄底
	 */
	public static final int REQ_COM_SJ_GIVE_CHAODI = 94;

	/**
	 * 亮主
	 */
	public static final int RES_COM_SJ_LIANGZHU = 90;
	/**
	 * 自保
	 */
	public static final int RES_COM_SJ_SELF_PROTECTED = 91;
	/**
	 * 推送底牌给庄家
	 */
	public static final int RES_COM_SJ_PUSH_BOTTOM_CARD = 92;
	/**
	 * 反主
	 */
	public static final int RES_COM_SJ_FANZHU = 93;
	/**
	 * 询问反主
	 */
	public static final int RES_COM_SJ_ASK_FANZHU = 94;
	/**
	 * 推送玩家底牌
	 */
	public static final int RES_COM_SJ_BOTTOM_CARD = 95;
	/**
	 * 询问炒底
	 */
	public static final int RES_COM_SJ_ASK_CHAODI = 96;
	/**
	 * 当前牌局打无主
	 */
	public static final int RES_COM_SJ_NO_RANK = 97;
	/**
	 * 扣底完成
	 */
	public static final int RES_COM_SJ_KOUDI = 98;
	/**
	 * 开始游戏
	 */
	public static final int RES_COM_SJ_START = 99;

    /**
     * 代开房间解散
     **/
    public static final int req_com_dissdaikai = 201;

///////////////以下是打筒子的/////////////////////
//客户端发送到服务器的消息
    /**
     * dtz不出牌
     */
    public static final int REQ_COM_GIVEUP = 124;
    /**
     * 推送选牌 分组
     */
    public static final int COM_SELECT_SEAT = 120;
    /**
     * 语音独立处理消息号
     */
    public static final int REQ_GOTYE = 126;
    /**
     * 战绩查看消息号
     */
    public static final int REQ_RECORD = 127;
    /**
     * 查看记牌器
     */
    public static final int REQ_CARD_MARKER = 129;
    /**
     * 打筒子托管
     */
    public static final int REQ_DTZ_AUTOPLAY = 131;

//服务器回传给客户端的消息
    /**
     * 客户端回传的
     */
    public static final int RES_SELECT_SEAT_OTHER = 121;
    /**
     * 完成了选座位
     */
    public static final int RES_SELECT_SEAT_DONE = 122;
    /**
     * 是否清空当前分数
     */
    public static final int RES_DISCARD_RUNS = 123;
    /**
     * 传给他名次
     */
    public static final int RES_MINGCI = 125;
    /**
     * 开打前准备信息推送
     */
    public static final int RES_READYMSG = 128;
    /**
     * 记牌器推送
     */
    public static final int RES_CARD_MARKER = 130;
    /**
     * 打筒子托管推送
     */
    public static final int RES_DTZ_AUTOPLAY = 132;

    /**
     * 托管倒计时累计满推送
     */
    public static final int AUTOPLAY_CHECKED = 133;
    /**
     * 加入金币场失败
     **/
    public static final int GOLD_JOIN_FAIL = 1100;
    /**
     * 加入金币场成功，等待开局
     **/
    public static final int GOLD_JOIN_WAIT = 1101;
    /**
     * 加入金币场成功，开局
     **/
    public static final int GOLD_JOIN_SUCCESS = 1102;
    /**
     * 加入金币场成功，开局
     **/
    public static final int GOLD_JOIN_TIP = 1103;

    /**
     * 批量创建房间
     **/
    public static final int MULTI_CREATE_TABLE = 1104;
    /**
     * 金币场换桌
     **/
    public static final int REQ_GOLD_CHANGE_TABLE = 1105;
    /**
     * 金币场换桌返回
     **/
    public static final int RES_GOLD_CHANGE_TABLE = 1105;
    /**
     * 金币不足
     **/
    public static final int RES_GOLD_POOR = 1106;
    
    /**
     * 刷新邀请
     **/
    public static final int COMMON_FRESH_APPLY = 1107;
    

    /*以下是半边天炸*/
    /**
     * 锤
     **/
    public static final int REQ_BBTZ_CHUI = 151;
    /**
     * 开枪
     **/
    public static final int REQ_BBTZ_KAIQIANG = 152;
    /**
     * 抢庄
     **/
    public static final int REQ_BBTZ_ROB_BANKER = 153;
    /**
     * 陡
     **/
    public static final int REQ_BBTZ_DOU = 154;

    /**
     * 半边天炸提示前台状态
     **/
    public static final int RES_BBTZ_STATE = 160;
    /**
     * 推送锤
     */
    public static final int RES_BBTZ_CHUI = 161;
    /**
     * 推送开枪投降
     */
    public static final int RES_BBTZ_KAIQIANG = 162;
    /**
     * 推送抢庄
     */
    public static final int RES_BBTZ_ROB_BANKER = 163;
    /**
     * 推送定庄
     */
    public static final int RES_BBTZ_BANKER = 164;
    /**
     * 推送陡
     */
    public static final int RES_BBTZ_DOU = 165;
    /**
     * 推送本轮结束
     */
    public static final int RES_BBTZ_ROUND_OVER = 166;

    /** 沅江麻将询问是否出牌 **/
    public static final int res_com_code_yjmj_ask_dis = 302;
    /** 王者鬼胡子低优先级操作跳过**/
    public static final int res_com_code_yj_guihz_skip = 306;

    /** 沅江麻将庄家出牌询问 **/
    public static final int res_code_ask_dismajiang = 122;

    /** 临时操作忽略返回码 **/
    public static final int res_com_code_temp_action_skip = 400;
    /** 临时操作忽略返回码-麻将 **/
    public static final int res_com_code_temp_action_mj = 410;

    /**
     * 礼券消息code
     */
    public static final int com_code_ticket = 300;

    /**
     * 俱乐部多玩法创房
     */
    public static final int group_balcony_createdtable = 155;


    /** 麻将特殊状态：飘分 **/
    public static final int res_code_table_status_piao = 200;
    /** 飘分*/
    public static final int req_code_piao_fen = 201;
    /** 飘分*/
    public static final int res_code_piao_fen = 201;
    /** 麻将听**/
    public static final int req_code_mj_ting = 202;
    /** 麻将听**/
    public static final int res_code_mj_ting = 202;
    /** 托管**/
    public static final int req_code_tuoguan = 210;
    /** 托管**/
    public static final int res_code_tuoguan = 210;
    /** 锤**/
    public static final int res_code_chui = 215;
    /** 锤**/
    public static final int req_code_chui = 215;
    
    /** 起始锤**/
    public static final int res_code_chui_start = 216;
    
    /** 杠分**/
    public static final int res_code_gangFen = 212;
    /** 麻将禁止出牌 **/
    public static final int res_code_mj_dis_err = 213;


    /** 麻将推送：买点 **/
    public static final int res_code_table_status_buy_point = 212;
    /** 买点*/
    public static final int req_code_piao_buy_point = 211;
    /** 买点*/
    public static final int res_code_buy_point = 211;

    /** 跑得快：牌桌的打鸟 **/
    public static final int res_code_pdk_daniao = 214;
    /** 跑得快：牌桌的打鸟 **/
    public static final int req_code_pdk_daniao = 214;

    /**桃江麻将:报听*/
    public static final int req_code_tjmj_baoting = 230;
    public static final int req_code_tjmj_baoting_show = 231;

    //潮汕麻将:最后一圈
    public static final int res_code_chaosmj_final_circle_tips= 235;


    /**通城麻将:飘分*/
    public static final int req_code_flutter_score = 236;
    public static final int req_code_flutter_score_res = 237;

    /*** 进金币场前先选择亲友圈 ***/
    public static final int req_code_choose_group_for_gold_room = 238;

    /*** solo房间配置列表 ***/
    public static final int req_code_solo_room_config_list = 239;

    /**
     * 调摸牌
     */
    public static final int req_code_set_mo = 610;
    /**
     * 可选摸牌返回
     */
    public static final int res_code_set_mo = 620;


    /**跑得快牌飘分**/
    public static final int req_code_pdk_piaofen=2021;
    public static final int res_code_pdk_piaofen=2021;
    public static final int res_code_pdk_broadcast_piaofen=2022;



    /** 准备前的房间设置补充 娄底放炮罚为打鸟信息**/
    public static final int req_code_table_replenish=2010;
    /** 打鸟返回code**/
    public static final int req_code_daniao_return=2011;
    /** 局内打鸟，发送打鸟及位置信息**/
    public static final int req_code_daniao_seat=2012;
    /**跑得快回放**/
    public static final int req_code_pdk_playBack=2013;
    /**放炮罚弃胡消息**/
    public static final int req_code_ldfpf_lockhand=2014;
    public static final int req_code_ldfpf_daniao=2032;
    public static final int res_code_ldfpf_daniao=2032;
    public static final int res_code_ldfpf_broadcast_daniao=2033;


    /**郴州字牌飘分**/
    public static final int req_code_zzzp_piaofen=2015;
    public static final int res_code_zzzp_piaofen=2015;
    public static final int res_code_zzzp_broadcast_piaofen=2016;
   
    /**转转麻将牌飘分**/
    public static final int req_code_zzmj_piaofen=2023;
    public static final int res_code_zzmj_piaofen=2023;
    public static final int res_code_zzmj_broadcast_piaofen=2024;

    public static final int req_code_piaoFen=2023;
    public static final int res_code_piaoFen=2023;
    public static final int res_code_broadcast_piaoFen=2024;
    
    /**个子牌飘分**/
    public static final int req_code_hbgzp_piaofen=2301;
    public static final int res_code_hbgzp_piaofen=2301;
    public static final int res_code_hbgzp_broadcast_piaofen=2302;
    /**XX2710 更新过掉得门子*/
    public static final int req_code_xx2710_passmenzi = 2303;
    /**XX2710 更新过掉得门子*/
    public static final int req_code_nxghz_passmenzi = 2304;
    /**益阳歪胡子 第一局发牌通知前端显示神牌*/
    public static final int req_code_YYWHZ_shenpai = 2305;
    /**益阳歪胡子  胡牌通知前端显示抽出的神腰*/
    public static final int req_code_YYWHZ_yaopai = 2306;
    /**耒阳字牌**/
    //吃边打边，返回禁止
    public static final int res_code_lyzp_cbdb=2017;
    //吃边打边，提示只能胡红黑堂
    public static final int res_code_lyzp_cbdb_hht=2018;
    //吃边打边，确认出牌
    public static final int req_code_lyzp_cbdb_hht=2018;
    //同时出发吃边打边和放招提示
    public static final int res_code_lyzp_cbdbfz=2019;
    //确认吃边打边放招提示
    public static final int req_code_lyzp_cbdbfz=2019;

    /**捉红字**/
    //吃完之后不能出相同的牌，置灰的手牌
    public static final int res_code_zhz_zhihui=2020;

    /**邵阳剥皮锤**/
    public static final int req_code_sybp_chui=2025;
    public static final int res_code_sybp_chui=2025;
    public static final int res_code_sybp_broadcast_chui=2026;

    /**衡阳十胡卡锤**/
    public static final int req_code_hyshk_chui=2025;
    public static final int res_code_hyshk_chui=2025;
    public static final int res_code_hyshk_broadcast_chui=2026;
    public static final int res_code_hyshk_shizhongcard=2027;




    /**
     * 湘乡告胡子坨
     * */
    public static final int RES_XXGHZ_TUO_START = 2101;
    /**
     * 湘乡告胡子坨
     * */
    public static final int REQ_XXGHZ_TUO = 2100;
    
    //------------------三打哈---------------------
    
    /**叫分加拍*/
    public static final int REQ_JIAOFEN=3100;
    /**选主*/
    public static final int REQ_XUANZHU=3101;
    /**定庄*/
    public static final int RES_DINGZHUANG=3102;
    /**获取出牌记录*/
    public static final int RES_CHUPAI_RECORD=3103;
    /**留守*/
    public static final int RES_Liushou=3104;
    /**投降*/
    public static final int RES_TOUX=3105;
    
    /**永州王钓麻将飘分**/
    public static final int req_code_yzwdmj_piaofen=2023;
    public static final int res_code_yzwdmj_piaofen=2023;
    public static final int res_code_yzwdmj_broadcast_piaofen=2024;
    
  //------------------垫坨---------------------
    /**独战*/
    public static final int DUZHAN_DIANTUO=4100;

    /**分组消息*/
    public static final int DUZHAN_TEAM=4101;
    
    
    /**获取分*/
    public static final int HUOFEN_DIANTUO=4102;
    
    
    /**队友手牌*/
    public static final int DUIYOU_HAND=4103;
    
    
    /**出单报王*/
    public static final int BAOWANG_HAND=4104;

    /**蓝山字牌飘分**/
    public static final int req_code_lszp_piaofen=2030;
    public static final int res_code_lszp_piaofen=2030;
    public static final int res_code_lszp_broadcast_piaofen=2031;

    /**千分刷新桌面分**/
    public static final int res_code_qf_refreshF=2041;
    public static final int res_code_qf_refreshM=2042;
    public static final int res_code_qf_refreshJ=2043;
    
    //-----------------------碰胡子------------------
    /**分数更新*/
    public static final int RES_FRESH_FEN=5100;
    
    
    /**五福报警*/
    public static final int RES_WUFU_BAOJING=5101;
    
    /**五福选择*/
    public static final int RES_WUFU_XUANZ=5102;
    
    
    
    /** 碰胡子特殊状态：打鸟 **/
    public static final int res_code_table_status_PENGHU_DANAIO = 5103;
    /** 打鸟 */
    public static final int req_code_piao_PENGHU_DANAIO  = 5104;
    /** 打鸟 */
    public static final int res_code_piao_PENGHU_DANAIO = 5104;
    
    /** 换座位 */
    public static final int res_code_piao_PENGHU_CHANGGE_SEAT = 5105;
    

    /**楚雄麻将获取杠信息**/
    public static final int res_code_cxmj_gangMsg=2044;
    /**楚雄麻将杠补牌选择**/
    public static final int res_code_cxmj_gangBu=2045;
    public static final int req_code_cxmj_gangBu=2045;


    /**宁乡开完麻将封东**/
    public static final int res_code_nxkwmj_fengDong=2061;
    public static final int req_code_nxkwmj_fengDong=2061;
    /**宁乡开完麻将报听**/
    public static final int res_code_nxkwmj_baoTing=2062;
    public static final int res_code_nxkwmj_haveAct=2063;
    public static final int res_code_nxkwmj_finishAct=2064;
    /**宁乡开完麻将借子开杠**/
    public static final int res_code_nxkwmj_jzkg=2065;
    public static final int req_code_nxkwmj_jzkg=2065;

    /**桐城跑风麻将补花**/
    public static final int res_code_tcpfmj_bh=2070;
    /**桐城跑风麻将跑风**/
    public static final int res_code_tcpfmj_pf=2071;
    /**南县麻将庄家报听出牌**/
    public static final int res_code_nanxmj_btcp=2080;
    /**南县麻将海底没胡**/
    public static final int res_code_nanxmj_hdmh=2081;
    /**重庆血战麻将通知换张**/
    public static final int res_code_cqxzmj_tzhz=2082;
    /**接收客户端换张消息**/
    public static final int req_code_cqxzmj_hz=2083;
    /**客户端换张消息成功返回**/
    public static final int res_code_cqxzmj_hz=2083;
    /**结束换张**/
    public static final int res_code_cqxzmj_finishHz=2084;
    /**重庆血战麻将通知定缺**/
    public static final int res_code_cqxzmj_tzdq=2085;
    /**接收客户端定缺消息**/
    public static final int req_code_cqxzmj_dq=2086;
    /**客户端定缺消息成功返回**/
    public static final int res_code_cqxzmj_dq=2086;
    /**结束定缺**/
    public static final int res_code_cqxzmj_finishDq=2087;


    /** 俱乐部玩家升级**/
    public static final int res_com_group_user_level_up = 2050;


    /**牛十别 明牌**/
    public static final int req_code_nsb_mingpai=4200;
    
    
    
    /** 自动摸打**/
    public static final int req_code_TDH_AUTO_MD = 6100;
    
    /** 推送报听*/
    public static final int req_code_ts_pao_ting = 6101;
    /** 报听*/
    public static final int res_code_bs_Baoting = 6102;
    
    /** 带跟刷新尾牌*/
    public static final int res_code_deh_TMJ = 6103;

    // ---------------沅江鬼胡子2200-2300----------------------------
    /*** 沅江鬼胡子：过吃重置 ***/
    public static final int res_code_yjghz_resetGuoChi = 2200;
    /*** 南县鬼胡子：过吃重置 ***/
    public static final int res_code_nxghz_resetGuoChi = 2200;
    /*** 益阳歪胡子：过吃重置 ***/
    public static final int res_code_yiyangwhz_resetGuoChi = 2200;

    /**益阳巴十 叫主*/
    public static final int REQ_YYBS_JIAOZHU  = 3100;
    public static final int REQ_YYBS_XUANZHU = 3101;
    /**庄选队伍*/
    public static final int REQ_YYBS_XUANDUI = 3106;
    /**常德拖拉机 对家选底牌定队*/
    public static final int REQ_CDTLJ_XUANDIPAI =3107;
    // ---------------道州麻将----------------------------
    /**番王*/
    public static final int FAN_WANG_=7100;

    /** 桐城掼蛋-红桃三位置消息*/
    public static final short REQ_TCGD_HONGTAO3 = 4133;
    /** 桐城掼蛋-新位置信息*/
    public static final short REQ_TCGD_NEW_SEAT = 4134;

    /**2人抢地主 叫地主*/
    public static final short REQ_2RenDDZ_START_CallLandlord =4140;

    /**2人抢地主 抢地主*/
    public static final short REQ_2RenDDZ_START_RobLandlord =4141;

    /**2人抢地主 底牌*/
    public static final short REQ_2RenDDZ_DIPAIMSG=4142;

    /**2人抢地主 请地主让牌*/
    public static final short REQ_2RenDDZ_RangPai=4143;
    /**2人抢地主  选择加倍 */
    public static final short REQ_2RenDDZ_SelJiaBei=4144;

    /*** 五子棋：提示玩家要选分**/
    public static final int res_code_wzq_cost_type = 4201;
    /*** 五子棋：玩家选的分数**/
    public static final int res_code_wzq_cost_type_value = 4202;
    /*** 五子棋：认输**/
    public static final int res_code_wzq_giveUp = 4203;
    
    /*** 选择箍臭**/
    public static final int req_code_GU_CHOU = 4204;
    /*** 箍臭消息推送**/
    public static final int res_code_GU_CHOU = 4205;

    //----4300 - 4400
    public static final int res_code_pdk_disCard_error = 4300;

    /**洗牌*/
    public static final int req_code_xipai = 4501;
    /**洗牌结果推送**/
    public static final int res_code_xipai_result = 4502;
    /**通知播放洗牌动画**/
    public static final int res_code_xipai = 4503;
    /**通知首局洗牌**/
    public static final int res_code_first_xipai_inform=4504;
    /**首局洗牌选择上传*/
    public static final int req_code_first_xipai = 4505;
    /**首局洗牌选择结果*/
    public static final int res_code_first_xipai = 4506;



    /**
     * 权限
     */
    public static final int req_code_quanxian = 4520;
    /**添加结果*/
    public static final int req_code_quanxian_add = 4521;
    /**删除结果*/
    public static final int req_code_quanxian_delete = 4522;
}
