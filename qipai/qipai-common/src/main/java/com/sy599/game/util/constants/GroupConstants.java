package com.sy599.game.util.constants;

import com.alibaba.fastjson.JSONObject;
import com.sy599.game.db.bean.group.GroupCommissionConfig;
import com.sy599.game.db.bean.group.GroupGoldCommissionConfig;
import com.sy599.game.db.bean.group.GroupUser;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class GroupConstants {

    public static final Map<Integer, GroupCommissionConfig> SYS_COMMISSION_LOG_MAP_1 = new HashMap<>();
    public static final Map<Integer, GroupCommissionConfig> SYS_COMMISSION_LOG_MAP_10 = new HashMap<>();
    public static final Map<Integer, GroupCommissionConfig> SYS_COMMISSION_LOG_MAP_100 = new HashMap<>();

    public static final Map<Integer, GroupGoldCommissionConfig> SYS_GOLD_COMMISSION_LOG_MAP = new HashMap<>();

    public static final TreeMap<Long, Long> SYS_COIN_CONSUME_MAP = new TreeMap<>();

    /*** 默认棋分***/
    public static final long DEFAULT_SCORE = 10000000;

    /**
     * 会长
     **/
    public static final int USER_ROLE_HuiZhang = 1;
    /**
     * 副会长
     **/
    public static final int USER_ROLE_FuHuiZhang = 2;
    /**
     * 董事
     */
    public static final int USER_ROLE_DongShi = 5000;
    /**
     * 主管
     */
    public static final int USER_ROLE_ZhuGuan = 10000;
    /**
     * 管理
     */
    public static final int USER_ROLE_GuanLi = 20000;
    /**
     * 组长
     */
    public static final int USER_ROLE_ZuZhang = 30000;
    /**
     * 成员
     */
    public static final int USER_ROLE_ChengYuan = 90000;


    static {

        SYS_COMMISSION_LOG_MAP_1.put(1, genGroupCommissionConfig(1, 0, 200));
        SYS_COMMISSION_LOG_MAP_1.put(2, genGroupCommissionConfig(2, 201, 300));
        SYS_COMMISSION_LOG_MAP_1.put(3, genGroupCommissionConfig(3, 301, 400));
        SYS_COMMISSION_LOG_MAP_1.put(4, genGroupCommissionConfig(4, 401, 500));
        SYS_COMMISSION_LOG_MAP_1.put(5, genGroupCommissionConfig(5, 501, 600));
        SYS_COMMISSION_LOG_MAP_1.put(6, genGroupCommissionConfig(6, 601, 800));
        SYS_COMMISSION_LOG_MAP_1.put(7, genGroupCommissionConfig(7, 801, 1000));
        SYS_COMMISSION_LOG_MAP_1.put(8, genGroupCommissionConfig(8, 1001, 1500));
        SYS_COMMISSION_LOG_MAP_1.put(9, genGroupCommissionConfig(9, 1501, 2000));
        SYS_COMMISSION_LOG_MAP_1.put(10, genGroupCommissionConfig(10, 2001, 999900));

        SYS_COMMISSION_LOG_MAP_10.put(1, genGroupCommissionConfig(1, 0, 2000));
        SYS_COMMISSION_LOG_MAP_10.put(2, genGroupCommissionConfig(2, 2001, 3000));
        SYS_COMMISSION_LOG_MAP_10.put(3, genGroupCommissionConfig(3, 3001, 4000));
        SYS_COMMISSION_LOG_MAP_10.put(4, genGroupCommissionConfig(4, 4001, 5000));
        SYS_COMMISSION_LOG_MAP_10.put(5, genGroupCommissionConfig(5, 5001, 6000));
        SYS_COMMISSION_LOG_MAP_10.put(6, genGroupCommissionConfig(6, 6001, 8000));
        SYS_COMMISSION_LOG_MAP_10.put(7, genGroupCommissionConfig(7, 8001, 10000));
        SYS_COMMISSION_LOG_MAP_10.put(8, genGroupCommissionConfig(8, 10001, 15000));
        SYS_COMMISSION_LOG_MAP_10.put(9, genGroupCommissionConfig(9, 15001, 20000));
        SYS_COMMISSION_LOG_MAP_10.put(10, genGroupCommissionConfig(10, 20001, 9999900));

        SYS_COMMISSION_LOG_MAP_100.put(1, genGroupCommissionConfig(1, 0, 20000));
        SYS_COMMISSION_LOG_MAP_100.put(2, genGroupCommissionConfig(2, 20001, 30000));
        SYS_COMMISSION_LOG_MAP_100.put(3, genGroupCommissionConfig(3, 30001, 40000));
        SYS_COMMISSION_LOG_MAP_100.put(4, genGroupCommissionConfig(4, 40001, 50000));
        SYS_COMMISSION_LOG_MAP_100.put(5, genGroupCommissionConfig(5, 50001, 60000));
        SYS_COMMISSION_LOG_MAP_100.put(6, genGroupCommissionConfig(6, 60001, 80000));
        SYS_COMMISSION_LOG_MAP_100.put(7, genGroupCommissionConfig(7, 80001, 100000));
        SYS_COMMISSION_LOG_MAP_100.put(8, genGroupCommissionConfig(8, 10001, 150000));
        SYS_COMMISSION_LOG_MAP_100.put(9, genGroupCommissionConfig(9, 150001, 200000));
        SYS_COMMISSION_LOG_MAP_100.put(10, genGroupCommissionConfig(10, 200001, 9999900));

        SYS_COIN_CONSUME_MAP.put(19999L, 200L);
        SYS_COIN_CONSUME_MAP.put(29999L, 300L);
        SYS_COIN_CONSUME_MAP.put(39999L, 500L);
        SYS_COIN_CONSUME_MAP.put(49999L, 600L);
        SYS_COIN_CONSUME_MAP.put(59999L, 700L);
        SYS_COIN_CONSUME_MAP.put(69999L, 800L);
        SYS_COIN_CONSUME_MAP.put(79999L, 900L);
        SYS_COIN_CONSUME_MAP.put(89999L, 1000L);
        SYS_COIN_CONSUME_MAP.put(99999L, 1000L);
        SYS_COIN_CONSUME_MAP.put(109999L, 1000L);
        SYS_COIN_CONSUME_MAP.put(999999999999L, 1500L);

        SYS_GOLD_COMMISSION_LOG_MAP.put(1, genGroupGoldCommissionConfig(1, 0, 500));
        SYS_GOLD_COMMISSION_LOG_MAP.put(2, genGroupGoldCommissionConfig(2, 501, 1000));
        SYS_GOLD_COMMISSION_LOG_MAP.put(3, genGroupGoldCommissionConfig(3, 1001, 2000));
        SYS_GOLD_COMMISSION_LOG_MAP.put(4, genGroupGoldCommissionConfig(4, 2001, 3000));
        SYS_GOLD_COMMISSION_LOG_MAP.put(5, genGroupGoldCommissionConfig(5, 3001, 4000));
        SYS_GOLD_COMMISSION_LOG_MAP.put(6, genGroupGoldCommissionConfig(6, 4001, 5000));
        SYS_GOLD_COMMISSION_LOG_MAP.put(7, genGroupGoldCommissionConfig(7, 5001, 6000));
        SYS_GOLD_COMMISSION_LOG_MAP.put(8, genGroupGoldCommissionConfig(8, 6001, 7000));
        SYS_GOLD_COMMISSION_LOG_MAP.put(9, genGroupGoldCommissionConfig(9, 7001, 8000));
        SYS_GOLD_COMMISSION_LOG_MAP.put(10, genGroupGoldCommissionConfig(10, 8001, 9000));
        SYS_GOLD_COMMISSION_LOG_MAP.put(11, genGroupGoldCommissionConfig(11, 9001, 10000));
        SYS_GOLD_COMMISSION_LOG_MAP.put(12, genGroupGoldCommissionConfig(12, 10001, 15000));
        SYS_GOLD_COMMISSION_LOG_MAP.put(13, genGroupGoldCommissionConfig(13, 15001, 20000));
        SYS_GOLD_COMMISSION_LOG_MAP.put(14, genGroupGoldCommissionConfig(14, 20001, 999999900));

    }

    public static GroupCommissionConfig genGroupCommissionConfig(int seq, long minCredit, long maxCredit) {
        GroupCommissionConfig con = new GroupCommissionConfig();
        con.setSeq(seq);
        con.setMinCredit(minCredit);
        con.setMaxCredit(maxCredit);
        con.setCredit(0l);
        return con;
    }

    public static GroupCommissionConfig getSysCommissionConfig(int seq, int creditRate) {
        if (creditRate == 1) {
            return SYS_COMMISSION_LOG_MAP_1.get(seq);
        } else if (creditRate == 10) {
            return SYS_COMMISSION_LOG_MAP_10.get(seq);
        } else {
            return SYS_COMMISSION_LOG_MAP_100.get(seq);
        }
    }

    public static GroupCommissionConfig getSysCommssionConfig(int creditRate, long credit) {
        Map<Integer, GroupCommissionConfig> map;
        if (creditRate == 1) {
            map = SYS_COMMISSION_LOG_MAP_1;
        } else if (creditRate == 10) {
            map = SYS_COMMISSION_LOG_MAP_10;
        } else {
            map = SYS_COMMISSION_LOG_MAP_100;
        }
        for (GroupCommissionConfig config : map.values()) {
            if (credit >= config.getMinCredit() && credit <= config.getMaxCredit()) {
                return config;
            }
        }
        return null;
    }

    public static long getSysCoinConsume(long credit) {
        long res = 0L;
        for (Map.Entry<Long, Long> entry : SYS_COIN_CONSUME_MAP.entrySet()) {
            if (entry.getKey() >= credit) {
                res = entry.getValue();
                break;
            }
        }
        return res;
    }

    /**
     * 是否被禁止
     * 创建房间，进房间
     * 打开成员列表
     *
     * @param gu
     * @return
     */
    public static boolean isForbidden(GroupUser gu) {
        if (gu == null || gu.getUserLevel() == null || gu.getUserLevel() <= 0) {
            return true;
        }
        return false;
    }

    /**
     * 是否主管
     *
     * @param userRole
     * @return
     */
    public static final boolean isZhuGuan(int userRole) {
        return userRole == USER_ROLE_ZhuGuan;
    }

    /*** 申请房间解散次数 0：无限***/
    public static final String groupExtKey_dismissCount = "dismissCount";
    /*** 是否进房间自动准备，0：否，1：是***/
    public static final String groupExtKey_autoReadyOnJoin = "autoReadyOnJoin";
    /*** 玩家下线,信用分自动上锁，0：否，1：是***/
    public static final String groupExtKey_creditLockOffline = "creditLockOffline";
    /*** 群主才可踢人，0：否，1：是***/
    public static final String groupExtKey_masterDelete = "masterDelete";
    /*** 同ip禁止进入，0：否，1：是***/
    public static final String groupExtKey_sameIpLimit = "sameIpLimit";
    /*** 不打开GPS禁止进入，0：否，1：是***/
    public static final String groupExtKey_openGpsLimit = "openGpsLimit";
    /*** 距离太近禁止进入，0：否，1：是***/
    public static final String groupExtKey_distanceLimit = "distanceLimit";
    /*** 是否允许信用负分，0：否，1：是***/
    public static final String groupExtKey_negativeCredit = "negativeCredit";
    /*** 亲友圈背景***/
    public static final String groupExtKey_backGround = "backGround";
    /*** 亲友圈玩法牌桌样式***/
    public static final String groupExtKey_tableStyle = "1";
    /*** 亲友圈玩去牌桌背景***/
    public static final String groupExtKey_tableBg = "2";
    /*** 亲友圈牌桌列表：已开局的牌桌数量***/
    public static final String groupExtKey_tableNum = "tableNum";
    /*** 亲友圈是否开启私密房功能***/
    public static final String groupExtKey_privateRoom = "privateRoom";
    /*** 亲友圈是停止开房功能***/
    public static final String groupExtKey_stopCreate = "stopCreate";
    /*** 亲友圈是否开启：禁止解散***/
    public static final String groupExtKey_forbiddenDiss = "forbiddenDiss";
    /*** 合伙人洗牌分归属设置***/
    public static final String extKey_xipaiConfig = "xipaiConfig";
    /*** 幸运转盘，0：关闭，>0：多少局一次抽奖机会***/
    public static final String groupExtKey_creditWheel = "creditWheel";

    public static Object getGroupExt(String extMsg, String key) {
        if (StringUtils.isBlank(extMsg) || StringUtils.isBlank(key)) {
            return null;
        }
        JSONObject extJson = JSONObject.parseObject(extMsg);
        return extJson.get(key);
    }

    /**
     * 亲友圈互斥
     *
     * @param userId1
     * @param userId2
     * @return
     */
    public static Long genGroupUserRejectKey(long userId1, long userId2) {
        if (userId1 < userId2) {
            return Long.valueOf(userId1 * 10000 + "" + userId2);
        } else {
            return Long.valueOf(userId2 * 10000 + "" + userId1);
        }
    }

    /**
     * 亲友圈好友
     *
     * @param userId1
     * @param userId2
     * @return
     */
    public static Long genGroupUserFriendKey(long userId1, long userId2) {
        if (userId1 < userId2) {
            return Long.valueOf(userId1 * 10000 + "" + userId2);
        } else {
            return Long.valueOf(userId2 * 10000 + "" + userId1);
        }
    }

    /**
     * 获取自己相应等级的上级id
     *
     * @param self
     * @param promoterLevel
     */
    public static long getPromoterId(GroupUser self, int promoterLevel) {
        switch (promoterLevel) {
            case 1:
                return self.getPromoterId1();
            case 2:
                return self.getPromoterId2();
            case 3:
                return self.getPromoterId3();
            case 4:
                return self.getPromoterId4();
            case 5:
                return self.getPromoterId5();
            case 6:
                return self.getPromoterId6();
            case 7:
                return self.getPromoterId7();
            case 8:
                return self.getPromoterId8();
            case 9:
                return self.getPromoterId9();
            case 10:
                return self.getPromoterId10();
            default:
                return 0;
        }
    }

    /**
     * 是否会长
     *
     * @param userRole
     * @return
     */
    public static final boolean isHuiZhang(int userRole) {
        return userRole == USER_ROLE_HuiZhang;
    }

    /**
     * 是否会长或副会长
     *
     * @param userRole
     * @return
     */
    public static final boolean isHuiZhangOrFuHuiZhang(int userRole) {
        return isHuiZhang(userRole) || isFuHuiZhang(userRole);
    }

    /**
     * 是否副会长
     *
     * @param userRole
     * @return
     */
    public static final boolean isFuHuiZhang(int userRole) {
        return userRole == USER_ROLE_FuHuiZhang;
    }

    /**
     * 是否成员
     *
     * @param userRole
     * @return
     */
    public static final boolean isChengYuan(int userRole) {
        return userRole == USER_ROLE_ChengYuan;
    }

    /**
     * 两个人是否可以玩五子棋
     * @param guSelf
     * @param guOther
     * @return
     */
    public static boolean isCanPlayWzq(GroupUser guSelf , GroupUser guOther){
        if(isHuiZhangOrFuHuiZhang(guSelf.getUserRole()) || isHuiZhangOrFuHuiZhang(guOther.getUserRole())){
            return true;
        }
        if (guSelf.getPromoterLevel() > guOther.getPromoterLevel()) {
            if (guOther.getUserId().equals(getPromoterId(guSelf, guOther.getPromoterLevel()))) {
                return true;
            }
        } else if (guSelf.getPromoterLevel() < guOther.getPromoterLevel()) {
            if (guSelf.getUserId().equals(getPromoterId(guOther, guSelf.getPromoterLevel()))) {
                return true;
            }
        }
        return false;
    }

    public static GroupGoldCommissionConfig genGroupGoldCommissionConfig(int seq, long min, long max) {
        GroupGoldCommissionConfig con = new GroupGoldCommissionConfig();
        con.setSeq(seq);
        con.setMinValue(min);
        con.setMaxValue(max);
        con.setLeftValue(0L);
        con.setValue(0L);
        con.setMaxLog(0L);
        return con;
    }

    public static GroupGoldCommissionConfig getSysGoldCommissionConfig(int seq, long value) {
        for (GroupGoldCommissionConfig config : SYS_GOLD_COMMISSION_LOG_MAP.values()) {
            if (value >= config.getMinValue() && value <= config.getMaxValue()) {
                return config;
            }
        }
        return null;
    }

    /**
     * 获取用户的下一级关系
     * @param groupUser
     * @param promoterLevel
     * @return
     */
    public static long getNextId(GroupUser groupUser, int promoterLevel){
        long nextUserId = 0l;
        switch (promoterLevel) {
            case 1:
                nextUserId = groupUser.getPromoterId2();
                break;
            case 2:
                nextUserId = groupUser.getPromoterId3();
                break;
            case 3:
                nextUserId = groupUser.getPromoterId4();
                break;
            case 4:
                nextUserId = groupUser.getPromoterId5();
                break;
            case 5:
                nextUserId = groupUser.getPromoterId6();
                break;
            case 6:
                nextUserId = groupUser.getPromoterId7();
                break;
            case 7:
                nextUserId = groupUser.getPromoterId8();
                break;
            case 8:
                nextUserId = groupUser.getPromoterId9();
                break;
            case 9:
                nextUserId = groupUser.getPromoterId10();
                break;
        }
        return nextUserId;
    }

    /**
     * 获取用户的上一级关系
     * @param groupUser
     * @param promoterLevel
     * @return
     */
    public static long getUpId(GroupUser groupUser, int promoterLevel){
        long upUserId = 0l;
        switch (promoterLevel) {
            case 1:
                upUserId = groupUser.getPromoterId1();
                break;
            case 2:
                upUserId = groupUser.getPromoterId1();
                break;
            case 3:
                upUserId = groupUser.getPromoterId2();
                break;
            case 4:
                upUserId = groupUser.getPromoterId3();
                break;
            case 5:
                upUserId = groupUser.getPromoterId4();
                break;
            case 6:
                upUserId = groupUser.getPromoterId5();
                break;
            case 7:
                upUserId = groupUser.getPromoterId6();
                break;
            case 8:
                upUserId = groupUser.getPromoterId7();
                break;
            case 9:
                upUserId = groupUser.getPromoterId8();
                break;
            case 10:
                upUserId = groupUser.getPromoterId9();
                break;
        }
        return upUserId;
    }

    /**
     * 亲友圈互斥
     *
     * @param userId1
     * @param userId2
     * @return
     */
    public static String genGroupUserRejectKeyStr(long userId1, long userId2) {
        if (userId1 < userId2) {
            return userId1 * 10000 + "" + userId2;
        } else {
            return userId2 * 10000 + "" + userId1;
        }
    }

    /**
     * 亲友圈好友
     *
     * @param userId1
     * @param userId2
     * @return
     */
    public static String genGroupUserFriendKeyStr(long userId1, long userId2) {
        if (userId1 < userId2) {
            return userId1 * 10000 + "" + userId2;
        } else {
            return userId2 * 10000 + "" + userId1;
        }
    }

}
