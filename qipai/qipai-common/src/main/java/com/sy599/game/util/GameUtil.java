package com.sy599.game.util;

import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.common.executor.TaskExecutor;
import com.sy599.game.db.bean.RegInfo;
import com.sy599.game.db.bean.group.GroupInfo;
import com.sy599.game.db.bean.group.GroupTable;
import com.sy599.game.db.bean.group.GroupTableConfig;
import com.sy599.game.db.bean.group.GroupUser;
import com.sy599.game.db.dao.UserDao;
import com.sy599.game.db.dao.group.GroupDao;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.manager.TableManager;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class GameUtil {
    /**
     * 防止用户出现在多个房间
     **/
    public static final Map<Long, String> USER_COMMAND_MAP = new ConcurrentHashMap<>();

    /**
     * 新版邵阳跑胡子
     **/
    public static final int play_type_syphz = 10;
    /**
     * 新版邵阳剥皮
     **/
    public static final int play_type_sybp = 11;

    /*** 轮流当庄十点半 */
    public static final int play_type_tenthirty_taketurn = 61;
    /**
     * 十点半房主霸王庄
     **/
    public static final int play_type_tenthirty_bawang = 62;

    /**
     * 十点半每局抢庄
     **/
    public static final int play_type_tenthirty_robwang = 63;
    /**
     * 十点半最小牌当庄
     **/
    public static final int play_type_tenthirty_lastwang = 64;

    /*** 三猴子轮流当庄 */
    public static final int play_type_three_taketurn = 65;
    /**
     * 三猴子每局抢庄
     **/
    public static final int play_type_three_robwang = 66;
    /**
     * 三猴子最小牌当庄
     **/
    public static final int play_type_three_lastwang = 67;
    /**
     * 三猴子房主霸王庄
     **/
    public static final int play_type_three_bawang = 68;

    /*** 把把抢庄斗牛 */
    public static final int play_type_dn_rob = 20;
    /*** 轮流抢庄斗牛 */
    public static final int play_type_dn_robtaketurns = 21;
    /*** 牛牛抢庄斗牛 */
    public static final int play_type_dn_robniuniu = 22;
    /**
     * 房主霸王庄斗牛
     **/
    public static final int play_type_dn_robbawang = 23;
    /**
     * 大牌为庄斗牛
     **/
    public static final int play_type_dn_robfirst = 24;
    /**
     * 闲家推注玩法
     **/
    public static final int play_type_dn_pushBet = 25;
    /**
     * 新增把把暗牌抢庄斗牛
     */
    public static final int play_type_dn_robanpai = 26;

    /**
     * 快乐邵阳跑胡子
     **/
    public static final int play_type_kl_shaoyang = 30;
    /**
     * 快乐邵阳剥皮
     **/
    public static final int play_type_kl_bopi = 31;
    /**
     * 邵阳跑胡子
     **/
    public static final int play_type_shaoyang = 32;

    /**
     * 邵阳剥皮
     **/
    public static final int play_type_bopi = 33;
    /**
     * 大字剥皮
     **/
    public static final int play_type_dazibopi = 803;

    /**
     * 甘肃扬沙子
     **/
    public static final int play_type_ysz = 81;

    /**
     * 甘肃麻将
     */
    public static final int play_type_gsmj = 101;
    /**
     * 谷仓麻将
     */
    public static final int play_type_gucang = 105;

    /**
     * 陇南摆叫麻将玩法ID
     **/
    public static final int game_type_majiang_lnbj = 102;
    /**
     * 陇南摆叫麻将三人玩法ID
     **/
    public static final int game_type_majiang_lnbj_3 = 108;
    /**
     * 陇南摆叫麻将两人玩法ID
     **/
    public static final int game_type_majiang_lnbj_2 = 109;

    /**
     * 卡二条麻将玩法ID
     **/
    public static final int game_type_majiang_kaertiao = 103;

    /**
     * 卡二条麻将二人玩法ID
     **/
    public static final int game_type_majiang_kaertiao_2 = 110;

    /**
     * 兰州会牌麻将玩法ID
     **/
    public static final int game_type_majiang_lzhp = 104;

    /**
     * 张掖麻将玩法ID
     **/
    public static final int game_type_majiang_zhangye = 106;

    /**
     * 兰州会牌麻将玩法ID
     **/
    public static final int game_type_majiang_qyhs = 107;

    /**
     * 岷县咣咣麻将玩法ID
     **/
    public static final int game_type_majiang_minXianGG = 111;

    /**
     * 秦安麻将玩法ID
     **/
    public static final int game_type_majiang_qamj = 112;

    /**
     * 三人斗地主
     **/
    public static final int ddz_three = 91;
    /**
     * 二人斗地主
     **/
    public static final int ddz_two = 92;
    /**
     * 赖子玩法
     **/
    public static final int ddz_three_niggle = 93;

    /** 快乐四喜：2人玩法 **/
    public static final int play_type_2PERSON_4Xi = 210;
    /** 快乐四喜：3人玩法 **/
    public static final int play_type_3PERSON_4Xi = 211;
    /** 快乐四喜：4人玩法 **/
    public static final int play_type_4PERSON_4Xi = 212;

    /**
     * 3副牌玩法
     **/
    public static final int play_type_3POK = 113;
    /**
     * 4副牌玩法
     **/
    public static final int play_type_4POK = 114;
    /**
     * 三人3副牌玩法
     **/
    public static final int play_type_3PERSON_3POK = 115;
    /**
     * 三人4副牌玩法
     **/
    public static final int play_type_3PERSON_4POK = 116;
    /**
     * 两人3副牌玩法
     **/
    public static final int play_type_2PERSON_3POK = 117;
    /**
     * 两人4副牌玩法
     **/
    public static final int play_type_2PERSON_4POK = 118;


    /**
     * 15张玩法
     **/
    public static final int play_type_15 = 15;
    /**
     * 16张玩法
     **/
    public static final int play_type_16 = 16;
    /**
     * 11张玩法
     **/
    public static final int play_type_11 = 11;
    
    /**
     * 跑得快11张玩法
     **/
    public static final int play_type_1011 = 1011;
    
    /**
     * yz15张玩法
     **/
    public static final int play_type_yz15 = 17;
    /**
     * yz16张玩法
     **/
    public static final int play_type_yz16 = 18;
    /***--------------------娄底放炮罚------------------------------------**/
    public static final int game_type_ldfpf=199;
    /***--------------------郴州  字牌------------------------------------**/
    public static final int game_type_zzzp = 198;
    /***--------------------耒阳  字牌------------------------------------**/
    public static final int game_type_lyzp = 197;
    /***--------------------捉红字  字牌------------------------------------**/
    public static final int game_type_zhz = 196;
    /***--------------------湘阴捉红字  字牌------------------------------------**/
    public static final int game_type_xyzhz = 195;
    /***--------------------字牌  衡阳十胡卡------------------------------------**/
    public static final int game_type_hyshk = 194;
    /***--------------------字牌  永州王钓麻将------------------------------------**/
    public static final int game_type_yzwdmj = 193;
    /***--------------------字牌  蓝山字牌------------------------------------**/
    public static final int game_type_lszp = 192;
    /***--------------------麻将  楚雄麻将------------------------------------**/
    public static final int game_type_cxmj = 191;
    /***--------------------字牌  千分------------------------------------**/
    public static final int game_type_qf = 190;
    /***--------------------麻将  宁乡开完麻将------------------------------------**/
    public static final int game_type_nxkwmj = 189;
    /***--------------------麻将  桐城跑风麻将------------------------------------**/
    public static final int game_type_tcpfmj = 188;
    /***--------------------麻将  桐城点炮麻将------------------------------------**/
    public static final int game_type_tcdpmj = 187;
    /***--------------------麻将  桐城点炮麻将------------------------------------**/
    public static final int game_type_nanxmj = 186;
    /***--------------------麻将  重庆血战麻将------------------------------------**/
    public static final int game_type_cqxzmj = 185;
    /***--------------------麻将  郑州跑得快------------------------------------**/
    public static final int game_type_zzpdk = 184;
    /***--------------------麻将  全州麻将------------------------------------**/
    public static final int game_type_qzmj = 183;
    /**
     * 碰胡子
     **/
    public static final int play_type_penghuzi = 250;
    
    
    
    
    /**
     * 永州扯胡子
     **/
    public static final int play_type_LDSPHZ = 229;
    
    
//    public static final int play_type_yzphz_4_4 = 36;
//    public static final int play_type_yzphz_3_2 = 37;
//    public static final int play_type_yzphz_3_4 = 38;
//    public static final int play_type_yzphz_3_3 = 39;
//    public static final int play_type_yzphz_4_3 = 40;
//    public static final int play_type_yzphz_2_2 = 41;
//    public static final int play_type_yzphz_2_3 = 42;
//    public static final int play_type_yzphz_2_4 = 43;
    
    /**
     * 永州扯胡子
     **/
    public static final int play_type_yzphz_4_2 = 35;
    public static final int play_type_yzphz_4_4 = 36;
    public static final int play_type_yzphz_3_2 = 37;
    public static final int play_type_yzphz_3_4 = 38;
    public static final int play_type_yzphz_3_3 = 39;
    public static final int play_type_yzphz_4_3 = 40;
    public static final int play_type_yzphz_2_2 = 41;
    public static final int play_type_yzphz_2_3 = 42;
    public static final int play_type_yzphz_2_4 = 43;

    /**
     * 衡阳六胡抢
     **/
    public static final int play_type_hylhq_2_0 = 44;
    public static final int play_type_hylhq_2_1 = 45;
    public static final int play_type_hylhq_2_2 = 46;
    public static final int play_type_hylhq_3_0 = 47;
    public static final int play_type_hylhq_3_1 = 48;
    public static final int play_type_hylhq_3_2 = 49;
    public static final int play_type_hylhq_4_0 = 50;
    public static final int play_type_hylhq_4_1 = 51;
    public static final int play_type_hylhq_4_2 = 52;

    /**
     * 湘西三皮
     **/
    public static final int play_type_sp = 121;

    /**
     * 转转麻将
     **/
    public static final int play_type_zhuanzhuan = 1;
    /**
     * 长沙麻将
     **/
    public static final int play_type_changesha = 2;
    /**
     * 红中麻将
     **/
    public static final int play_type_hongzhong = 3;
    /**
     * 安化麻将
     **/
    public static final int play_type_anhua = 4;
    /**
     * 长春麻将
     **/
    public static final int play_type_ccmj = 5;

    /**
     * 半边天炸玩法
     **/
    public static final int play_type_bbtz = 131;

    /**
     * 沅江鬼胡子玩法
     **/
    public static final int play_type_yjghz = 39;
   
    /***
     * 常德跑胡子
     */
    public static final int play_type_changdephz =  53;
    
    
    /***
     * 溆浦跑胡子
     */
    public static final int play_type_xpphz =  521;
 
    
    /***
     * 红拐弯跑胡子
     */
    public static final int play_type_HGWphz =  252;
    
    
    /***
     * 安乡偎麻雀
     */
    public static final int play_type_Axphz =  254;
    

    /**
     * 沅江鬼胡子八软息玩法
     **/
    public static final int play_type_yjghzbrx = 54;
    /**
     * 王者麻将(沅江)
     **/
    public static final int play_type_yuanjiang = 6;
    /**
     * 王者跑得快15张(沅江)
     **/
    public static final int play_type_pdk_yj15 = 15;
    /**
     * 王者跑得快16张(沅江)
     **/
    public static final int play_type_pdk_yj16 = 16;

    /**
     * 千分 三个人三副牌(去掉3,4)
     **/
    public static final int play_type_qianfen3_3_70 = 70;

    /**
     * 千分 三个人三副牌(去掉3,4,6,7)
     **/
    public static final int play_type_qianfen3_3_71 = 71;

    /**
     * 千分 二个人三副牌(去掉3,4)
     **/
    public static final int play_type_qianfen2_3_72 = 72;

    /**
     * 千分 二个人三副牌(去掉3,4,6,7)
     **/
    public static final int play_type_qianfen2_3_73 = 73;

    /**
     * 划水麻将玩法ID
     **/
    public static final int game_type_majiang_plhs = 500;

    /**
     * 静宁打经麻将玩法ID
     **/
    public static final int game_type_majiang_jndj = 501;

    /**
     * 天水麻将玩法ID
     **/
    public static final int game_type_majiang_tsmj = 502;

    /**
     * 金昌划水麻将玩法ID
     **/
    public static final int game_type_majiang_jchs = 503;

    /**
     * 陇西麻将玩法ID
     **/
    public static final int game_type_majiang_longximj = 504;

    /**
     * 武威麻将玩法ID
     **/
    public static final int game_type_majiang_wuweimj = 505;

    /**
     * 酒泉嘉峪关悄悄胡玩法ID
     **/
    public static final int game_type_majiang_jqmj_jyg = 506;

    /**
     * 酒泉三报麻将玩法ID
     **/
    public static final int game_type_majiang_jqmj_sanbao = 507;

    /**
     * 酒泉挑经玩法ID
     **/
    public static final int game_type_majiang_jqmj_tiaojing = 508;

    /**
     * 酒泉二报玩法ID
     **/
    public static final int game_type_majiang_jqmj_erbao = 509;

    /**
     * 兰州二报玩法ID
     **/
    public static final int game_type_majiang_lz_erbao = 510;

    /**
     * 兰州翻金玩法ID
     **/
    public static final int game_type_majiang_lz_fanjin = 511;

    /**
     * 山西斗地主
     **/
    public static final int game_type_shangxi_doudizhu = 600;
    /**
     * 临汾斗地主
     **/
    public static final int game_type_linfen_doudizhu = 601;
    /**
     * 二人斗地主
     **/
    public static final int game_type_er_doudizhu = 602;
    /**
     * 运城四人斗地主
     **/
    public static final int game_type_yuncheng_doudizhu = 603;

    /**
     * 龙虎斗
     **/
    public static final int game_type_longhudou = 640;
    /**
     * 二八杠
     **/
    public static final int game_type_erbagang = 641;

    public static final int game_type_caishaizi = 642;

    /**
     * 安化斗地主3人玩法
     **/
    public static final int game_type_ah_ddz91 = 91;

    /**
     * 安化斗地主2人玩法
     **/
    public static final int game_type_ah_ddz92 = 92;

    /**
     * 安化跑得快玩法ID
     **/
    public static final int game_type_ah_pdk15 = 15;

    /**
     * 安化跑得快玩法ID
     **/
    public static final int game_type_ah_pdk16 = 16;

    /**
     * 安化转转麻将玩法ID
     **/
    public static final int game_type_ah_zzmj = 1;

    /**
     * 安化红中麻将玩法ID
     **/
    public static final int game_type_ah_hzmj = 3;

    /**
     * 安化长沙麻将玩法ID
     **/
    public static final int game_type_ah_csmj = 2;

    /**
     * 安化麻将玩法ID
     **/
    public static final int game_type_ah_mj = 4;

    /**
     * 安化斗牛玩法ID
     **/
    public static final int game_type_ah_dn20 = 20;

    /**
     * 安化斗牛玩法ID
     **/
    public static final int game_type_ah_dn23 = 23;

    /**
     * 安化斗牛玩法ID
     **/
    public static final int game_type_ah_dn22 = 22;

    /**
     * 安化斗牛玩法ID
     **/
    public static final int game_type_ah_dn21 = 21;

    /**
     * 安化长春麻将玩法ID
     **/
    public static final int game_type_ah_ccmj = 5;

    /**
     * 安化跑胡子玩法ID
     **/
    public static final int game_type_ah_phz = 31;

    /**
     * 安化邵阳跑胡子玩法ID
     **/
    public static final int game_type_ah_syphz = 32;

    /**
     * 安化剥皮玩法ID
     **/
    public static final int game_type_ah_sybp = 33;

    /**
     * 安化娄底跑胡子玩法ID
     **/
    public static final int game_type_ah_loudi_phz = 34;

    /**
     * 安化桂林跑胡子玩法ID
     **/
    public static final int game_type_ah_guilin_phz = 35;

    /**
     * 安化三公玩法ID
     **/
    public static final int game_type_ah_sg42 = 42;

    /**
     * 安化三公玩法ID
     **/
    public static final int game_type_ah_sg41 = 41;
    /**
     * 双扣玩法2-A游戏ID
     */
    public static final int GAME_SJ_TO_A_ID = 200;
    /**
     * 双扣玩法2-6游戏ID
     */
    public static final int GAME_SJ_TO_6_ID = 201;
    /**
     * 双扣玩法2-10游戏ID
     */
    public static final int GAME_SJ_TO_10_ID = 202;

    /***--------------------转转 麻将------------------------------------**/
    public static final int game_type_zzmj = 220;
    /***--------------------红中 麻将------------------------------------**/
    public static final int game_type_hzmj = 221;
    /***--------------------芷江 麻将------------------------------------**/
    public static final int game_type_zjmj = 804;
    /***--------------------长沙 麻将------------------------------------**/
    public static final int game_type_csmj = 222;
    /***--------------------宁乡 麻将------------------------------------**/
    public static final int game_type_nxmj = 248;
    /***--------------------邵阳 麻将------------------------------------**/
    public static final int game_type_symj = 223;
    
    /***--------------------保山 麻将------------------------------------**/
    public static final int game_type_bsmj = 225;

    /***--------------------衡阳六胡抢------------------------------------**/
    public static final int game_type_hylhq= 226;
    
    
    /***--------------------推倒胡------------------------------------**/
    public static final int game_type_TdhMj= 227;
    
    
    /***--------------------岳阳歪胡子-------------------------------**/
    public static final int game_type_YyWaiHZ= 228;
    
    /***--------------------落地扫-------------------------------**/
    public static final int game_type_LDSPHZ= 229;

    /***--------------------江华跑胡子-------------------------------**/
    public static final int game_type_JHPHZ= 281;

    /***--------------------三打哈------------------------------**/
    public static final int game_type_sandh = 231;
    
    /***--------------------打坨------------------------------**/
    public static final int game_type_DATUO = 232;
    
    
    /***--------------------道州 麻将------------------------------------**/
    public static final int game_type_daozmj = 251;
    
    /***--------------------郑州 麻将------------------------------------**/
    public static final int game_type_zhengzmj = 253;
    
    /**
     * 永州扯胡子
     **/
    public static final int play_type_YZPHZ = 230;

	/**
	 * 永州老戳
	 * */
	public static final int play_type_yongzhou_laochuo = 301;
    /**
     * 石门跑胡子
     * */
    public static final int play_type_shimen_paohuzi = 300;

    /**
     * 湘潭跑胡子
     **/
    public static final int play_type_xiangtan_paohuzi = 235;
    /**
     * 湘乡告胡子
     **/
    public static final int play_type_xiangxiang_gaohuzi = 236;
    /**
     * 湘乡跑胡子
     **/
    public static final int play_type_xiangxiang_paohuzi = 237;
    /**
     * 桂林跑胡子
     * */
    public static final int play_type_guilin_paohuzi = 245;
    /**
     * 安化跑胡子
     * */
    public static final int play_type_anhua_paohuzi = 238;

    /**
     * 宁乡跑胡子
     * */
    public static final int play_type_ningxiang_paohuzi = 246;
    /**
     * 溆浦老牌
     * */
    public static final int play_type_xupu_laopai = 805;
    /**
     * 汉寿跑胡子
     * */
    public static final int play_type_hanshou_paohuzi = 249;


    /**
     * 湘西2710
     * */
    public static final int play_type_xiangxi2710_paohuzi = 800;
    /**
     * 南县鬼胡子
     * */
    public static final int play_type_nxghz = 801;
    /**
     * 益阳歪胡子
     * */
    public static final int play_type_yiyangwhz = 802;
    /**
     * 湖北个子牌
     * */
    public static final int play_type_hubai_gezipai = 247;

    /**
     * 牛十别
     * */
    public static final int play_type_pk_nsb= 255;
    /**
     * 永州（新田）包牌
     * */
    public static final int play_type_pk_xtbp= 256;
    /**
     * 益阳巴十
     * */
    public static final int play_type_pk_yybs= 257;
    /**
     * 桐城掼蛋
     * */
    public static final int play_type_pk_tcgd= 258;
    /**
     * 衡山同花
     * */
    public static final int play_type_pk_hsth= 259;

    /**
     * 桃江麻将
     * */
    public static final int play_type_tjmj = 260;

    /**潮汕麻将*/
    public static final int play_type_chaosmj = 261;
    /**通城麻将*/
    public static final int play_type_tcmj = 262;
    /**宁远麻将*/
    public static final int play_type_nymj = 263;
    /***--------------------德宏 麻将------------------------------------**/
    public static final int game_type_dehmj = 239;

	/**2人斗地主*/
	public static  final int play_type_pk_2renddz = 264;

    /**益阳麻将*/
    public static final int play_type265_yymj = 265;
	/**望城跑胡子*/
	public static final int play_type266_wcphz = 266;

    /**靖州麻将*/
    public static final int play_type_mj_jzmj = 270;

    /**常德拖拉机*/
    public static final int play_type_pk_cdtlj = 271;


    /*** 五子棋***/
    public static final int play_type_wzq = 1000;


    public static boolean isPlayNewSyPhz(int playType) {
        return playType == play_type_syphz;
    }

    public static boolean isPlayNewSyBp(int playType) {
        return playType == play_type_sybp;
    }


    public static boolean isPlayYjGame() {
        return "1".equals(ResourcesConfigsUtil.loadServerPropertyValue("game_wz"));
    }

    public static boolean isPlayAhGame() {
        return "1".equals(ResourcesConfigsUtil.loadServerPropertyValue("game_ah"));
    }

    public static boolean isPlayAhDdz(int playType) {
        return isPlayAhGame() && (playType == game_type_ah_ddz91 || playType == game_type_ah_ddz92);
    }

    public static boolean isPlayAhPdk(int playType) {
        return isPlayAhGame() && (playType == game_type_ah_pdk15 || playType == game_type_ah_pdk16);
    }

    public static boolean isPlayAhZzOrHzMj(int playType) {
        return isPlayAhGame() && (playType == game_type_ah_zzmj || playType == game_type_ah_hzmj);
    }

    public static boolean isPlayAhCsMj(int playType) {
        return isPlayAhGame() && (playType == game_type_ah_csmj);
    }

    public static boolean isPlayAhMj(int playType) {
        return isPlayAhGame() && (playType == game_type_ah_mj);
    }

    public static boolean isPlayAhDn(int playType) {
        return isPlayAhGame() && (playType == game_type_ah_dn20 || playType == game_type_ah_dn21 || playType == game_type_ah_dn22 || playType == game_type_ah_dn23);
    }

    public static boolean isPlayAhCcMj(int playType) {
        return isPlayAhGame() && (playType == game_type_ah_ccmj);
    }

    public static boolean isPlayAhPhz(int playType) {
        return isPlayAhGame() && (playType == game_type_ah_phz);
    }
    public static boolean isPlaySmPhz(int playType) {
        return (playType == play_type_shimen_paohuzi);
    }

    public static boolean isPlayYzLCPhz(int playType) {
        return (playType == play_type_yongzhou_laochuo);
    }

    public static boolean isPlayAhSyPhz(int playType) {
        return isPlayAhGame() && (playType == game_type_ah_syphz || playType == game_type_ah_sybp);
    }
    public static boolean isPlayBSMj(int playType) {
        return playType == game_type_bsmj;
    }

    public static boolean isPlayDehMj(int playType) {
        return playType == game_type_dehmj;
    }


    public static boolean isPlayAhLdPhz(int playType) {
        return isPlayAhGame() && (playType == game_type_ah_loudi_phz);
    }

    public static boolean isPlayAhGlPhz(int playType) {
        return isPlayAhGame() && (playType == game_type_ah_guilin_phz);
    }

    public static boolean isPlayAhSg(int playType) {
        return isPlayAhGame() && (playType == game_type_ah_sg41 || playType == game_type_ah_sg42);
    }

    /**
     * 是否是玩千分游戏
     *
     * @param playType
     * @return
     */
    public static final boolean isPlayQianFen(int playType) {
        return playType == play_type_qianfen3_3_70 || playType == play_type_qianfen3_3_71 || playType == play_type_qianfen2_3_72 || playType == play_type_qianfen2_3_73;
    }

    public static boolean isPlayYjGhz(int playType) {
        return playType == play_type_yjghz; //|| playType == play_type_yjghzbrx
    }

    public static boolean isPlayYjMj(int playType) {
        return playType == play_type_yuanjiang;
    }

    public static boolean isPlayYjPdk(int playType) {
        if (isPlayYjGame()) {
            return playType == play_type_pdk_yj15 || playType == play_type_pdk_yj16;
        }
        return false;
    }



    public static boolean isPlayBbtz(int playType) {
        if (playType == play_type_bbtz) {
            return true;
        }
        return false;
    }

    public static boolean isPlayNn(int playType) {
        return SharedConstants.isKingOfBull() && (playType == play_type_dn_robniuniu || playType == play_type_dn_robtaketurns || playType == play_type_dn_rob || playType == play_type_dn_robbawang || playType == play_type_dn_robfirst
                || playType == play_type_dn_pushBet);
    }

    public static boolean isPlayDn(int playType) {
        return playType == play_type_dn_robniuniu || playType == play_type_dn_robtaketurns || playType == play_type_dn_rob || playType == play_type_dn_robbawang || playType == play_type_dn_robfirst
                || playType == play_type_dn_pushBet;
    }

    public static boolean isPlayBopi(int playType) {
        return playType == play_type_bopi || playType == play_type_kl_bopi;
    }

    public static boolean isPlaySyPhz(int playType) {
        return playType == play_type_shaoyang || playType == play_type_bopi || playType == play_type_kl_shaoyang || playType == play_type_kl_bopi;
    }
    public static boolean isPlayDzbp(int playType) {
    	return playType == play_type_dazibopi;
    }

    public static boolean isPlayXtPhz(int playType) {
        return playType == play_type_xiangtan_paohuzi;
    }
    public static boolean isPlayNxPhz(int playType) {
    	return playType == play_type_ningxiang_paohuzi;
    }
    public static boolean isPlayXpLp(int playType) {
    	return playType == play_type_xupu_laopai;
    }
    public static boolean isPlayHsPhz(int playType) {
    	return playType == play_type_hanshou_paohuzi;
    }
    public static boolean isPlayHbgzp(int playType) {
    	return playType == play_type_hubai_gezipai;
    }

    public static boolean isPlayXxGhz(int playType) {
        return playType == play_type_xiangxiang_gaohuzi;
    }

    public static boolean isPlayXxPhz(int playType) {
        return playType == play_type_xiangxiang_paohuzi;
    }

    public static boolean isPlayAHPhzNew(int playType) {
        return playType == play_type_anhua_paohuzi;
    }


    public static boolean isPlayCdPhz(int playType) {
        return playType == play_type_changdephz;
    }
    
    public static boolean isPlayHGWPhz(int playType) {
        return playType == play_type_HGWphz||playType ==play_type_Axphz||playType == play_type_xpphz;
    }
    

    public static boolean isPlayTenthirty(int playType) {
        return playType == play_type_tenthirty_taketurn || playType == play_type_tenthirty_bawang || playType == play_type_tenthirty_robwang || playType == play_type_tenthirty_lastwang;
    }

    public static boolean isPlayThreeMonkeys(int playType) {
        return playType == play_type_three_taketurn || playType == play_type_three_robwang;
    }

    public static boolean isPlayGSMajiang(int playType) {
        return (playType == play_type_gsmj || playType == play_type_gucang);
    }

    public static boolean isPlayDdz(int playType) {
        return playType == ddz_three || playType == ddz_two || playType == ddz_three_niggle;
    }

    public static boolean isLdfpf(int playType) {
        return playType==game_type_ldfpf;
    }
    public static boolean isZzzp(int playType) {
        return playType==game_type_zzzp;
    }
    public static boolean isLyzp(int playType) {
        return playType==game_type_lyzp;
    }
    public static boolean isZhz(int playType) {
        return playType==game_type_zhz;
    }
    public static boolean isXx2710(int playType) {
    	return playType==play_type_xiangxi2710_paohuzi;
    }
    public static boolean isNxghz(int playType) {
    	return playType==play_type_nxghz;
    }
    public static boolean isYiYangWhz(int playType) {
    	return playType==play_type_yiyangwhz;
    }
    public static boolean isHylhq(int playType) {
        return playType==game_type_hylhq;
    }
    public static boolean isHyshk(int playType) {
        return playType==game_type_hyshk;
    }
    public static boolean isGlphz(int playType) {
    	return playType==play_type_guilin_paohuzi;
    }
    public static boolean isYzwdmj(int playType) {
        return playType==game_type_yzwdmj;
    }

    public static boolean isLszp(int playType) {
        return playType==game_type_lszp;
    }

    public static boolean isPlayDtz(int playType) {
        switch (playType) {
            case play_type_3POK:
            case play_type_4POK:
            case play_type_3PERSON_3POK:
            case play_type_3PERSON_4POK:
            case play_type_2PERSON_3POK:
            case play_type_2PERSON_4POK:
            case play_type_2PERSON_4Xi:
            case play_type_3PERSON_4Xi:
            case play_type_4PERSON_4Xi:
                return true;
            default:
                return false;
        }
    }

    public static boolean isPlayPdk(int playType) {
        if (playType == play_type_15 || playType == play_type_16|| playType == play_type_1011|| playType == play_type_11||playType == game_type_zzpdk) {
            return true;
        }
        return false;
    }

    public static boolean isPlayZZPdk(int playType) {
        if (playType == game_type_zzpdk) {
            return true;
        }
        return false;
    }

    public static boolean isPlayYzPdk(int playType) {
        if (playType == play_type_yz15 || playType == play_type_yz16) {
            return true;
        }
        return false;
    }

    public static boolean isPlaySp(int playType) {
        return playType == play_type_sp;
    }

    public static boolean isPlayMajiang(int playType) {
        if (playType == play_type_hongzhong || playType == play_type_zhuanzhuan) {
            return true;
        }
        return false;
    }

    public static boolean isPlayCSMajiang(int playType) {
        if (playType == play_type_changesha) {
            return true;
        }
        return false;
    }

    public static boolean isPlayAhMajiang(int playType) {
        if (playType == play_type_anhua) {
            return true;
        }
        return false;
    }

    public static boolean isPlayCCMajiang(int playType) {
        if (playType == play_type_ccmj) {
            return true;
        }
        return false;
    }

    /**
     * 是否百人玩法
     *
     * @param playType
     * @return
     */
    public static boolean isPlayBaiRenWanfa(int playType) {
        if (playType == game_type_longhudou || playType == game_type_erbagang) {
            return true;
        }
        return false;
    }

    /**
     * 五子棋
     * @param playType
     * @return
     */
    public static boolean isPlayWzq(int playType) {
        return playType == play_type_wzq;
    }

    /**
     * 速创建房间模式下检测智能创房
     *
     * @param groupId
     * @param playerClass
     */
    @Deprecated
    public final static void autoCreateGroupTable(final String groupId, final Class<? extends Player> playerClass) {
    	autoCreateGroupTable(groupId, playerClass, 0);
    }
    @Deprecated
    public final static void autoCreateGroupTable(final String groupId, final Class<? extends Player> playerClass, long configId) {}

    private static final Map<String, Object> lockMap = new ConcurrentHashMap<>();

    public static Object getAutoCreateLock(String groupId) {
        Object res = lockMap.get(groupId);
        if (res != null) {
            return res;
        } else {
            synchronized (lockMap) {
                res = lockMap.get(groupId);
                if (res != null) {
                    return res;
                } else {
                    res = new Object();
                    lockMap.put(groupId, res);
                    return res;
                }
            }
        }
    }

    /**
     * 计算两点之间距离
     *
     * @param start
     * @param end
     * @return 米
     */
    public static double getDistanceOld(String start, String end) {
        String[] starts = start.split(",");
        String[] ends = end.split(",");
        double lat1 = (Math.PI / 180) * NumberUtils.toDouble(starts[0]);
        double lat2 = (Math.PI / 180) * NumberUtils.toDouble(ends[0]);

        double lon1 = (Math.PI / 180) * NumberUtils.toDouble(starts[1]);
        double lon2 = (Math.PI / 180) * NumberUtils.toDouble(ends[1]);

        //地球半径
        double R = 6371;

        //两点间距离 km，如果想要米的话，结果*1000
        double d = Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1)) * R;
        return (int) d * 1000;
    }

    /**
     * 获取牌局结束的条件值
     *
     * @param ints
     * @return
     */
    public static final int loadOverValue(List<Integer> ints) {
        int size;
        if (ints == null || (size = ints.size()) < 2) {
            return 0;
        }
        int playType = ints.get(1);
        int count = ints.get(0);
        if (isPlayQianFen(playType)) {
            return size >= 6 ? ints.get(5) : 0;
        } else if (isPlayDtz(playType)) {
            return size >= 4 ? ints.get(3) : 0;
        } else {
            return count;
        }
    }

	public static boolean isPlaySandh(int playType) {
		return playType == game_type_sandh;
	}
	public static boolean isPlayDaTuo(int playType) {
		return playType == game_type_DATUO;
	}

	public static boolean isPlayPengHuzi(int playType) {
		return playType == play_type_penghuzi;
	}


    public static boolean isPlayZzMj(int playType){
        return playType == game_type_zzmj;
    }
    public static boolean isPlayCxMj(int playType){
        return playType == game_type_cxmj;
    }
    public static boolean isPlayNxkwMj(int playType){
        return playType == game_type_nxkwmj;
    }
    public static boolean isPlayQzMj(int playType){
        return playType == game_type_qzmj;
    }
    public static boolean isPlayTcpfMj(int playType){
        return playType == game_type_tcpfmj;
    }
    public static boolean isPlayTcdpMj(int playType){
        return playType == game_type_tcdpmj;
    }

    public static boolean isPlayCqxzMj(int playType){
        return playType == game_type_cqxzmj;
    }
    public static boolean isPlayNanxMj(int playType){
        return playType == game_type_nanxmj;
    }
    public static boolean isQianFen(int playType){
        return playType == game_type_qf;
    }
    public static boolean isPlayHzMj(int playType){
        return playType == game_type_hzmj;
    }
    public static boolean isPlayZjMj(int playType){
    	return playType == game_type_zjmj;
    }
    public static boolean isPlayDzMj(int playType){
        return playType == game_type_daozmj||isPlayZhengZmj(playType);
    }
    
    
    public static boolean isPlayZhengZmj(int playType){
        return playType == game_type_zhengzmj;
    }
    
    
    public static boolean isPlaySyMj(int playType){
        return playType == game_type_symj;
    }
    public static boolean isPlayCsMj(int playType){
        return playType == game_type_csmj;
    }
    public static boolean isPlayNxMj(int playType){
    	return playType == game_type_nxmj;
    }

    public static boolean isPlayTjMj(int playType){
        return playType == play_type_tjmj;
    }

    public static boolean isPlay265YyMj(int playType){
        return playType == play_type265_yymj;
    }

    public static boolean isPlayChaosMj(int playType){
        return playType == play_type_chaosmj;
    }

    public static boolean isPlayTcMj(int playType){
        return playType == play_type_tcmj;
    }

    public static boolean isPlayNyMj(int playType){
        return playType == play_type_nymj;
    }

    public static boolean isPlayTdhMj(int playType){
        return playType == game_type_TdhMj;
    }

    public static boolean isPlayYyWhz(int playType){
        return playType == game_type_YyWaiHZ;
    }

    public static boolean isPlayLDSPhz(int playType){
        return playType == game_type_LDSPHZ ||playType == play_type_YZPHZ||playType==game_type_JHPHZ;
    }
    public static boolean isPlayNiuShiBie(int playType) {
        return playType == play_type_pk_nsb;
    }
    public static boolean isPlayTcgd(int playType) {
        return playType == play_type_pk_tcgd;
    }
    public static boolean isPlayHsth(int playType) {
        return playType == play_type_pk_hsth;
    }

    public static boolean isPlayXTBP(int playType) {
        return playType == play_type_pk_xtbp;
    }
    public static boolean isPlayYYBS(int playType) {
        return playType == play_type_pk_yybs;
    }
    public static boolean isPlayCDTLJ(int playType) {
        return playType == play_type_pk_cdtlj;
    }

    public static boolean isPlay2renDdz(int playType) {
        return playType == play_type_pk_2renddz;
    }
    public static boolean isPlayJingZhouMJ(int playType) {
        return playType == play_type_mj_jzmj;
    }
    public static boolean isPlayWcphz(int playType) {
        return playType == play_type266_wcphz;
    }

    /**
     * 该玩法的空桌数量
     * @param table
     * @return
     */
    public static int loadGroupRoomEmptyTableCount(BaseTable table) {
        if (table.isGroupRoom()) {
            GroupTable gt = table.getGroupTable();
            String groupId = table.loadGroupId();
            if (gt != null) {
                try {
                    Integer count = GroupDao.getInstance().loadGroupRoomEmptyTableCount(Long.valueOf(groupId), "0", gt.getConfigId());
                    if (count != null) {
                        return count.intValue();
                    }
                } catch (Exception e) {
                    LogUtil.errorLog.error("loadGroupRoomTableCount|error|" + e.getMessage(), e);
                }
            }
        }
        return 0;
    }

    public final static void autoCreateGroupTableNew(BaseTable table) {
        if (table.isGroupRoom()) {
            GroupTable gt = table.getGroupTable();
            if (gt != null && gt.getConfigId() > 0 && (gt.getIsPrivate() != null && gt.getIsPrivate() != 1)) {
                GameUtil.autoCreateGroupTableNew(table.loadGroupId(), gt.getConfigId());
            }
        }
    }

    public final static void autoCreateGroupTableNew(final String groupId, long configId) {
        final long now = System.currentTimeMillis();
        LogUtil.monitorLog.info("autoCreateGroupTable|submit|" + now + "|" + groupId + "|" + configId);
        TaskExecutor.SINGLE_EXECUTOR_SERVICE_GROUP.execute(new Runnable() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                try {
                    LogUtil.monitorLog.info("autoCreateGroupTable|run|" + now + "|" + groupId + "|" + "|" + configId);
                    //快速创建房间模式下检测智能创房 - 智能补房
                    GroupInfo group = GroupDao.getInstance().loadGroupInfo(groupId, "0");
                    if (group == null || group.getExtMsg() == null) {
                        return;
                    }
                    GroupUser master = GroupDao.getInstance().loadGroupMaster(group.getGroupId().toString());
                    if (master == null) {
                        return;
                    }

                    if (configId > 0) {
                        GroupTableConfig config = GroupDao.getInstance().loadGroupTableConfig(configId);
                        if (config == null || !"1".equals(config.getConfigState())) {
                            LogUtil.monitorLog.info("autoCreateGroupTable|error|5|" + now + "|" + groupId + "|" + configId + "|" + (config != null ? config.getConfigState() : ""));
                            return;
                        }
                        synchronized (getAutoCreateLock(groupId)) {
                            Integer count = GroupDao.getInstance().loadGroupRoomEmptyTableCount(group.getGroupId(), "0", config.getKeyId());
                            if (count == null || count.intValue() > 0) {
                                LogUtil.monitorLog.info("autoCreateGroupTable|fail|6|" + now + "|" + groupId + "|" + configId + "|" + count + "|" + 1 + "|" + config.getGroupId());
                                return;
                            }
                            Player player = PlayerManager.getInstance().getPlayer(master.getUserId());
                            if (player == null) {
                                RegInfo user = UserDao.getInstance().selectUserByUserId(master.getUserId());
                                if (user == null) {
                                    return;
                                }
                                BaseTable table = TableManager.getInstance().getInstanceTable(config.getGameType());
                                player = ObjectUtil.newInstance(table.getPlayerClass());
                                player.loadFromDB(user);
                            }
                            int createCount = 1;
                            for(int i = 0 ; i < createCount ; i++) {
                                createTable(player, groupId, config);
                            }
                        }
                    } else {
                        List<GroupInfo> groupRooms = GroupDao.getInstance().loadAllGroupRoom(groupId);
                        if (groupRooms == null || groupRooms.size() == 0) {
                            return;
                        }
                        Player player = PlayerManager.getInstance().getPlayer(master.getUserId());
                        for (GroupInfo groupRoom : groupRooms) {
                            GroupTableConfig config = GroupDao.getInstance().loadLastGroupTableConfig(groupRoom.getGroupId(), group.getGroupId());
                            if (config == null) {
                                return;
                            }
                            synchronized (getAutoCreateLock(groupId)) {
                                Integer count = GroupDao.getInstance().loadGroupRoomEmptyTableCount(group.getGroupId(), "0", config.getKeyId());
                                if (count == null || count.intValue() > 0) {
                                    LogUtil.monitorLog.info("autoCreateGroupTable|fail|6|" + now + "|" + groupId + "|" + configId + "|" + count + "|" + 1 + "|" + config.getGroupId());
                                    continue;
                                }
                                if (player == null) {
                                    RegInfo user = UserDao.getInstance().selectUserByUserId(master.getUserId());
                                    if (user == null) {
                                        return;
                                    }
                                    BaseTable table = TableManager.getInstance().getInstanceTable(config.getGameType());
                                    player = ObjectUtil.newInstance(table.getPlayerClass());
                                    player.loadFromDB(user);
                                }
                                int createCount = 1;
                                for(int i = 0 ; i < createCount ; i++) {
                                    createTable(player, groupId, config);
                                }
                            }
                        }
                        if(player != null) {
                            player.writeComMessage(WebSocketMsgType.MULTI_CREATE_TABLE, 1, 0);
                        }
                    }
                } catch (Throwable t) {
                    LogUtil.errorLog.error("autoCreateGroupTable Throwable:" + t.getMessage(), t);
                } finally {
                    LogUtil.monitorLog.info("autoCreateGroupTable|timeUse|" + now + "|" + groupId + "|" + configId + "|" + (System.currentTimeMillis() - start));
                }
            }

            public void createTable(Player player, String groupId, GroupTableConfig config) {
                JsonWrapper json;
                List<Integer> intsList = null;
                List<String> strsList = null;
                try {
                    json = new JsonWrapper(config.getModeMsg());
                    intsList = GameConfigUtil.string2IntList(json.getString("ints"));
                    strsList = GameConfigUtil.string2List(json.getString("strs"));
                } catch (Throwable th) {
                } finally {
                    if ((intsList == null || intsList.size() == 0) && (strsList == null || strsList.size() == 0)) {
                        intsList = GameConfigUtil.string2IntList(config.getModeMsg());
                        strsList = new ArrayList<>();
                    }
                }
                try {
                    strsList.add(groupId);
                    strsList.add("1");
                    strsList.add("1");
                    strsList.add(String.valueOf(config.getKeyId()));
                    Map<String, Object> properties = new HashMap<>();
                    properties.put("autoCreate", "1");

                    StringBuilder sb = new StringBuilder("createTable1|autoCreateGroupTable");
                    sb.append("|").append(player.getUserId());
                    sb.append("|").append(intsList);
                    sb.append("|").append(strsList);
                    sb.append("|").append(properties);
                    sb.append("|").append(groupId);
                    sb.append("|").append(config.getKeyId());
                    LogUtil.monitorLog.info(sb.toString());

                    TableManager.getInstance().createTable(player, intsList, strsList, 0, 0, true, properties, null, null);
                } catch (Throwable t) {
                    LogUtil.errorLog.error("autoCreateGroupTable Throwable:" + t.getMessage(), t);
                }
            }
        });
    }

    /*** 地球半径单位米**/
    private static final double EARTH_RADIUS = 6371393;

    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    /**
     * 经纬度获取距离，单位为米
     * @param start
     * @param end
     * @return
     */
    public static double getDistance(String start, String end) {
        String[] starts = start.split(",");
        String[] ends = end.split(",");

        double lat1 = NumberUtils.toDouble(starts[0]);
        double lon1 = NumberUtils.toDouble(starts[1]);

        double lat2 = NumberUtils.toDouble(ends[0]);
        double lon2 = NumberUtils.toDouble(ends[1]);
        return getDistance(lat1,lon1,lat2,lon2);
    }

    /**
     * 经纬度获取距离，单位为米
     **/
    public static double getDistance(double lat1, double lng1, double lat2, double lng2) {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lng1) - rad(lng2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000d) / 10000d;
        return s;
    }

    public static void main(String[] args) {
        String start = "28.189340277777777,112.88541856553819,0";
        String end = "28.189859,112.891197,0";
        System.out.println(getDistance(start,end));
        System.out.println(getDistanceOld(start,end));
    }


}
