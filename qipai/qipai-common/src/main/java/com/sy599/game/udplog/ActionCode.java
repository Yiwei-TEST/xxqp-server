package com.sy599.game.udplog;

/**
 * 用于日志记录中表示玩家的具体行为
 * @author taohuiliang
 * @date 2013-3-30
 * @version v1.0
 */
public class ActionCode {
    /**强化装备  **/
    public static final int STRENGTH_EQUIP = 1;
    /**卖出装备  **/
    public static final int SELL_EQUIP = 2;
    /** 建筑生产 **/
    public static final int BUILDING_PRODUCE=3;
    /** 建筑收获 **/
    public static final int BUILDING_HARVEST=4;
    /** 清建筑CD **/
    public static final int BUILDING_CLEAR_CD=5;
    /** 天书激活 **/
    public static final int BOOK_ACTIVATE=6;
    /** 军师上供 **/
    public static final int COMMANDER_OFFER_UP=7;
    /** 日常任务 **/
    public static final int DAILY_QUEST=8;
    /** 招魂 **/
    public static final int HERO_SOUL=9;
    /** 招魂 一键成长**/
    public static final int HERO_SOUL_SAFE_GROW=10;
    /** 招魂 成长**/
    public static final int HERO_SOUL_GROW=11;
    /** 招魂 初始化**/
    public static final int HERO_SOUL_INIT=12;
    /** 挂机 **/
    public static final int KEEP_OL=13;
    /** 采石 **/
    public static final int MINING=14;
    /** 招财 **/
    public static final int MONEY_TREE=15;
    /** 俸禄 **/
    public static final int SALARY=16;
    /** 训练通关 **/
    public static final int TRIAL_PASS=17;
    /** 挑战通关 **/
    public static final int CHALLENGE_PASS=18;
    /** 平乱收获功勋 **/
    public static final int SKYGOD_HARVEST=19;
    /** 平乱通关 **/
    public static final int SKYGOD_PASS=20;
    /** 征战通关 **/
    public static final int STORYLINE_PASS=21;
    /** 训练购买体力 **/
    public static final int TRIAL_BUY_ENERGY=22;
    /** 同盟抽奖 **/
    public static final int LEAGUE_AWARD=23;
    /** 同盟捐献 **/
    public static final int LEAGUE_DEVOTE=24;
    /** 同盟浇水 **/
    public static final int LEAGUE_WATER=25;
    /** 卸载宝石 **/
    public static final int JEWEL_TAKE_OFF=26;
    /** 宝石培养 **/
    public static final int JEWEL_STRENGTH=27;
    /** 法宝出售 **/
    public static final int MAGIC_WEAPON_SELL=28;
    /** 法宝降级 **/
    public static final int MAGIC_WEAPON_DOWN=29;
    /** 法宝升级 **/
    public static final int MAGIC_WEAPON_UP=30;
    /** 兑换将魂 **/
    public static final int HERO_SOUL_EXCHANGE=31;
    /** 招募将领 **/
    public static final int HIRE_HERO=32;
    /** 新手引导 **/
    public static final int GUIDE_QUEST=33;
    /** 新手引导 **/
    public static final int PRESTIGE_UPGRADE=34;
    /** 竞技场清CD **/
    public static final int ARENA_CLEAR_CD=35;
    /** 神秘商店购买 **/
    public static final int STORE_M_BUY=36;
    /** 普通商店购买 **/
    public static final int STORE_BUY=37;
    /** 拆解宝石 **/
    public static final int JEWEL_DECOMPOSE=38;
    /** 邮件领取 **/
    public static final int MAIL_GET=39;
    /** 领取礼包 **/
    public static final int RECEIVE_GIFT=40;
    /** 竞技场参与奖励 **/
    public static final int ARENA_NORMAL_AWARD=41;
    /** 创建同盟 **/
    public static final int LEAGUE_CREATE = 42;
    /** 竞技场排名奖励 **/
    public static final int ARENA_RANK_AWARD = 43;
    /** 每日签到奖励 **/
    public static final int SIGN_DAY_GET=44;
    /** 签到领取奖励 **/
    public static final int SIGN_AWARD_GET=45;
    /** 充值增加元宝 **/
    public static final int YUANBAO_BUY=46;
    /** 运营平台修改VIP等级 **/
    public static final int HT_EDIT_USER=47;
    /** 购买地宫额外次数 **/
    public static final int TEMPLE_BUY_TIMES=48;
    /** 护符替换 **/
    public static final int PROTECT_ITEM_REPLACE=49;
    /** 地宫奖励 **/
    public static final int TEMPLE_AWARD=50;
    /** 地宫刷新宝箱 **/
    public static final int TEMPLE_REFRESH=51;
    /** 招募英雄获取奖励 **/
    public static final int ACTIVE_HERO_AWARD=52;
    /** 登陆获取奖励 **/
    public static final int ACTIVE_LOGIN_AWARD=53;
    /** 提升等级获取奖励 **/
    public static final int ACTIVE_LEVEL_AWARD=54;
    /** 在线奖励 **/
    public static final int ACTIVE_ONLINE_AWARD=55;
    /** VIP奖励 **/
    public static final int ACTIVE_VIP_AWARD=56;
    /** 消耗奖励 **/
    public static final int ACTIVE_CONSUME_AWARD=57;
    /** 军师奖励 **/
    public static final int ACTIVE_COMMANDER_AWARD=58;
    /** CDK奖励 **/
    public static final int ACTIVE_CDK_AWARD=59;
    /** 邮件领取 **/
    public static final int MAIL_AWARD=60;
    /** 貂蝉活动领取 **/
    public static final int ACTIVE_DIAOCHAN_AWARD=61;
    /** 充值活动领取 **/
    public static final int ACTIVE_PAY_AWARD=62;
    /** 每天赠送一个银镐 **/
    public static final int HOE_EACH_DAY=63;
    /** 邮件删除 **/
    public static final int MAIL_DELETE=64;
    /** 竞技场购买次数 **/
    public static final int ARENA_BUY_TIMES=65;
    /** 赔偿奖励 **/
    public static final int Compenstate_Award=66;
    /** 赠送鲜花 **/
    public static final int SEND_FLOWER=67;
    /** 世界BOSS重生 **/
    public static final int WORLD_BOSS_REBORN=68;
    /** 世界BOSS鼓舞 **/
    public static final int WORLD_BOSS_HEARTEN=69;
    /** 打世界BOSS **/
    public static final int WORLD_BOSS_FIGHT=70;
    /** 同盟战鼓舞 **/
    public static final int LEAGUE_BATTLE_HEARTEN=71;
    /** 世界BOSS清除CD **/
    public static final int WORLD_BOSS_CLEARCD=72;
    /** 开启地宫宝箱 **/
    public static final int TEMPLE_OPEN_BOX=73;
    
    /** 运镖劫镖奖励 **/
    public static final int TRANS_ROB_AWARD=74;
    /** 刷新镖车ID扣除元宝 **/
    public static final int TRANS_REFRESH_VERCHID=75;
    
    /** 购买镖车运镖次数 **/
    public static final int TRANS_FOOD_COUNT=76;
    
    /** 购买镖车劫镖次数 **/
    public static final int TRANS_ROB_COUNT=77;
    
    /** 镖车结算 **/
    public static final int TRANS_END_AWARD=78;
    
    /** 镖车清CD **/
    public static final int TRANS_CLEAR_CD=79;
    
    /** 购买红色镖车 **/
    public static final int TRANS_BUY_RED=80;
    
    /** 购买鼓舞次数 **/
    public static final int TRANS_BUF_COUNT=81;
    
    /** 自动普通运镖 **/
    public static final int TRANS_AUTO_NORMAL=82;
    /** 自动豪华运镖 **/
    public static final int TRANS_AUTO_ADVANCE=83;
    
    /** 宝库奖励 **/
    public static final int TREASURE_BONUS=84;
    /** 宝库重置 **/
    public static final int TREASURE_RESETLEVEL_BONUS=85;
    /** 购买宝库次数 **/
    public static final int TREASURE_YUANBAO_TIMES=86;
    /** 刷新神秘商店 **/
    public static final int MSTORE_REFRESH=87;
    /** 炼魂 **/
    public static final int EXP_2_SOUL=88;
    /** 天书升级 **/
    public static final int BOOK_UPGRADE=89;
    /** 比武大会鼓舞 **/
    public static final int TOURNAMENT_HEARTEN=90;
    /** 补签扣除元宝 **/
    public static final int SIGN_ADD=91;
    /** 升级曹仁 **/
    public static final int ACTIVE_CAOREN=92;
    /** 圣诞活动中的累积充值奖励 **/
    public static final int AWARD_CHRISTMAS_1=93;
    /** 圣诞活动中的法宝兑换**/
    public static final int AWARD_CHRISTMAS_2=94;
    /** 跨服积分赛购买次数 **/
    public static final int CROSS_WP_BUY_TIMES=95;
    /** 跨服积分赛刷新对手 **/
    public static final int CROSS_WP_REFRESH=96;
    /**在线时间奖励*/
    public static final int AWARD_ONLINE2=97;
    /**三日目标奖励*/
    public static final int AWARD_THREETARGET=98;
    /** 领取CDK礼包**/
    public static final int AWARD_CDK=99;
    /** 跨服积分兑换 **/
    public static final int CROSS_POINTS_STORE=100;
    /** 武将培养 **/
    public static final int HERO_TRAIN=101;
    /** 武将升阶 **/
    public static final int HERO_UPGRADE=102;
    /** 金矿(七星台)掠夺**/
    public static final int GOLD_MINE_FIGHT=103;
    /** 金矿(七星台),双倍效果扣除元宝**/
    public static final int GOLD_MINE_DOUBLE=104;
    /** 金矿主动收获**/
    public static final int GOLD_MINE_GET=105;
    /** 金矿元宝购买次数 **/
    public static final int GOLD_MINE_BUY_TIMES=106;
    /** 7日豪礼 **/
    public static final int PAY_AMOUNT_EVERYDAY=107;
    /** 特殊成长礼包 **/
    public static final int SPECIAL_AWARD_BAG=108;
    /** 新活动奖励 **/
    public static final int NEW_ACTIVE_BONUS=109;
    /** 新活动兑换扣除 **/
    public static final int NEW_ACTIVE_EXCHANGE=110;
    /** 更新30M、80M升级包奖励 **/
    public static final int UPDATE_PACKAGE_GIFT=111;
    /** 大富翁商店购买 **/
    public static final int ZILLIONAIRE_STORE=112;
    /** 大富翁战斗奖励 **/
    public static final int ZILLIONAIRE_FIGHT=113;
    /** 时间获取奖励 **/
    public static final int ZILLIONAIRE_TIME=114;
    /** 大富翁购买行动力 **/
    public static final int ZILLIONAIRE_BUY_ENERGY=115;
    /** 大富翁购买遥控骰子 **/
    public static final int ZILLIONAIRE_BUY_TELE=116;
    /** 武将抽取 **/
    public static final int HERO_LOTTERY=117;
    /** 武将升星 **/
    public static final int HERO_STAR_UP=118;
    /** 刷挑战通关 **/
    public static final int RE_CHALLENGE_PASS=119;
    /** 购买士气值 **/
    public static final int MORALE_BUY=120;
    /** 世界BOSS购买次数 **/
    public static final int WORLD_BOSS_TIMES=121;
    /** 大富翁答题 **/
    public static final int ZILLIONAIRE_ANSWER=122;
    /**章节奖励*/
    public static final int ACTIVE_CHAPTER_BONUS = 123;
    /** 购买挑战次数 **/
    public static final int CHALLENGE_TIMES_BUY=124;
    /** 月卡每天奖励 **/
    public static final int MONTH_CARD_EACHDAY=125;
    /** 任务集市 **/
    public static final int QQ_CONTRACT=126;
    /** 话费活动 **/
    public static final int TELE=127;
    /** 同盟副本 **/
    public static final int LEAGUE_PVE=128;
    /** 主角培养 **/
    public static final int LEADING_ROLE=129;
    /** 充值奖励 **/
    public static final int PAY_AWARD=130;
    /** 同盟副本购买挑战次数 **/
    public static final int LEAGUE_PVE_BUY=131;
    /** 每日首冲奖励 **/
    public static final int FIRSTPAY_BONUS_PERDAY=132;
    /** 诸葛亮活动消耗元宝**/
    public static final int ZGL_DRAW=134;
    /** 诸葛亮活动领取奖励**/
    public static final int ZGL_AWARD=135;
    /** 幻境 **/
    public static final int FAIRYLAND=136;
    /** 积分商店购买 **/
    public static final int STORE_CP_BUY=137;
}
