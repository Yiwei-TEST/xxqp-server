package com.sy.sanguo.game.constants;

import com.alibaba.fastjson.JSONObject;
import com.sy.sanguo.common.init.InitData;
import com.sy.sanguo.game.bean.group.GroupCommissionConfig;
import com.sy.sanguo.game.bean.group.GroupGoldCommissionConfig;
import com.sy.sanguo.game.bean.group.GroupInfo;
import com.sy.sanguo.game.bean.group.GroupUser;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GroupConstants {
    /**
     * 群主
     **/
    public static final int GROUP_ROLE_MASTER = 0;
    /**
     * 管理员
     **/
    public static final int GROUP_ROLE_ADMIN = 1;
    /**
     * 成员
     **/
    public static final int GROUP_ROLE_MEMBER = 2;
    /**
     * 小组长
     **/
    public static final int GROUP_ROLE_TEAM_LEADER = 10;
    /**
     * 拉手
     **/
    public static final int GROUP_ROLE_PROMOTOR = 20;

    /**假桌子刷新小局最小时间 秒*/
    public static final int FAKE_TABLE_REFRESH_TIME_MINI = 30;

    /**假桌子刷新小局最小时间 秒*/
    public static final int FAKE_TABLE_REFRESH_TIME_MAX = 50;

    /**
         * 信用分记录类型：管理者加减分/赠送
     */
    public static final int CREDIT_LOG_TYPE_ADMIN = 1;
    /**
     * 信用分记录类型：牌桌佣金分
     */
    public static final int CREDIT_LOG_TYPE_COMMSION = 2;
    /**
     * 信用分记录类型：牌桌输赢分
     */
    public static final int CREDIT_LOG_TYPE_TABLE = 3;
    /**
     * 信用分记录类型：洗牌分
     */
    public static final int CREDIT_LOG_TYPE_XIPAI = 4;

    /**
     * 信用分记录类型：AA佣金分
     */
    public static final int CREDIT_LOG_TYPE_AA = 5;
    /**
     * 信用分记录类型：零钱包
     */
    public static final int CREDIT_LOG_TYPE_PURSE = 6;


    /**
     * 群主
     */
    public static boolean isMaster(int userRole) {
        return userRole == GROUP_ROLE_MASTER;
    }

    /**
     * 群主或管理员
     */
    public static boolean isMasterOrAdmin(int userRole) {
        return userRole == GROUP_ROLE_MASTER || userRole == GROUP_ROLE_ADMIN;
    }

    /**
     * 管理员
     */
    public static boolean isAdmin(int userRole) {
        return userRole == GROUP_ROLE_ADMIN;
    }

    /**
     * 小组长
     */
    public static boolean isTeamLeader(int userRole) {
        return userRole == GROUP_ROLE_TEAM_LEADER;
    }

    /**
     * 普通成员
     */
    public static boolean isMember(int userRole) {
        return userRole == GROUP_ROLE_MEMBER;
    }

    /**
     * 拉手
     */
    public static boolean isPromotor(int userRole) {
        return userRole == GROUP_ROLE_PROMOTOR;
    }


    /**
     * 小组长往下
     * lower 是 groupUser的直接下级关系
     *
     * @param groupUser
     * @param lower
     * @return
     */
    public static boolean isNextLevel(GroupUser groupUser, GroupUser lower) {
        if (!groupUser.getUserGroup().equals(lower.getUserGroup())) {
            return false;
        }
        if (GroupConstants.isTeamLeader(groupUser.getUserRole())) {
            if (lower.getPromoterLevel() == 1) {
                return true;
            }
        } else if (GroupConstants.isPromotor(groupUser.getUserRole())) {
            if (groupUser.getPromoterLevel() == 1 && lower.getPromoterId1() == groupUser.getUserId() && lower.getPromoterLevel() == 2) {
                return true;
            } else if (groupUser.getPromoterLevel() == 2 && lower.getPromoterId2() == groupUser.getUserId() && lower.getPromoterLevel() == 3) {
                return true;
            } else if (groupUser.getPromoterLevel() == 3 && lower.getPromoterId3() == groupUser.getUserId() && lower.getPromoterLevel() == 4) {
                return true;
            } else if (groupUser.getPromoterLevel() == 4 && lower.getPromoterId4() == groupUser.getUserId() && lower.getPromoterLevel() == 5) {
                return true;
            }
        } else {
            return false;
        }
        return false;
    }


    /**
     * lower 是否是 groupUser 下级
     *
     * @param groupUser
     * @param lower
     * @return
     */
    public static boolean isLower(GroupUser groupUser, GroupUser lower) {
        if (groupUser == null || lower == null) {
            return false;
        }
        if (groupUser.getUserId() == lower.getUserId()) {
            return true;
        }
        if (GroupConstants.isMasterOrAdmin(groupUser.getUserRole())) {
            return true;
        } else if (GroupConstants.isTeamLeader(groupUser.getUserRole())) {
            return lower.getPromoterLevel() > 0 && groupUser.getUserGroup().equals(lower.getUserGroup());
        } else if (GroupConstants.isPromotor(groupUser.getUserRole())) {
            if (groupUser.getPromoterLevel() == 1 && lower.getPromoterId1() == groupUser.getUserId()) {
                return true;
            } else if (groupUser.getPromoterLevel() == 2 && lower.getPromoterId2() == groupUser.getUserId()) {
                return true;
            } else if (groupUser.getPromoterLevel() == 3 && lower.getPromoterId3() == groupUser.getUserId()) {
                return true;
            } else if (groupUser.getPromoterLevel() == 4 && lower.getPromoterId4() == groupUser.getUserId()) {
                return true;
            }
            return false;
        } else {
            return false;
        }
    }


    public static final Map<Integer, GroupCommissionConfig> SYS_COMMISSION_LOG_MAP_1 = new HashMap<>();
    public static final Map<Integer, GroupCommissionConfig> SYS_COMMISSION_LOG_MAP_10 = new HashMap<>();
    public static final Map<Integer, GroupCommissionConfig> SYS_COMMISSION_LOG_MAP_100 = new HashMap<>();

    public static final Map<Integer, GroupGoldCommissionConfig> SYS_GOLD_COMMISSION_LOG_MAP = new HashMap<>();


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
        con.setCredit(0L);
        con.setLeftCredit(0L);
        con.setMaxCreditLog(0L);
        return con;
    }

    public static GroupCommissionConfig getSysCommissionConfig(int seq) {
        return SYS_COMMISSION_LOG_MAP_1.get(seq);
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

    /*** 玩家是设置过区间信分分成***/
    public static final String extKey_commissionConfig = "commissionConfig";
    /*** 玩家是设置过区间信分分成***/
    public static final String extKey_goldCommissionConfig = "goldCommissionConfig";
    /*** 合伙人洗牌分归属设置***/
    public static final String extKey_xipaiConfig = "xipaiConfig";

    /**
     * 如果俱乐部名字中包含了：元 角 毛 分 块 的，用*号代替
     *
     * @param groupName
     * @return
     */
    public static final String filterGroupName(String groupName) {
        if (InitData.groupFilterWords.isEmpty())
            return groupName;
        List<String> filterMoneyName = InitData.groupFilterWords;
        for (String moneyName : filterMoneyName) {
            if (groupName.contains(moneyName)) {
                groupName = groupName.replace(moneyName, "*");
            }
        }
        return groupName;
    }


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

    /**
     * 邀请加入
     */
    public static final int TYPE_USER_ALERT_INVITE = 1;
    /**
     * 申请加入
     */
    public static final int TYPE_USER_ALERT_APPLY = 2;
    /**
     * 踢出
     */
    public static final int TYPE_USER_ALERT_DELETE = 3;


    public static final Set<Integer> userRoleSet = new HashSet<>(Arrays.asList(USER_ROLE_HuiZhang, USER_ROLE_FuHuiZhang, USER_ROLE_ZhuGuan, USER_ROLE_ChengYuan));

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
     * 是否董事
     *
     * @param userRole
     * @return
     */
    public static final boolean isDongShi(int userRole) {
        return userRole == USER_ROLE_DongShi;
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

    /**
     * 是否管理
     *
     * @param userRole
     * @return
     */
    public static final boolean isGuanLi(int userRole) {
        return userRole == USER_ROLE_GuanLi;
    }

    /**
     * 是否组长
     *
     * @param userRole
     * @return
     */
    public static final boolean isZuZhang(int userRole) {
        return userRole == USER_ROLE_ZuZhang;
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
     * 是否是合法的权限
     *
     * @param userRole
     * @return
     */
    public static boolean isValidUserRole(int userRole) {
        return userRoleSet.contains(userRole);
    }

    /**
     * 是否可以踢出成员
     * 会长或副会长踢所有下级
     *
     * @param self
     * @param target
     * @return
     */
    public static boolean canDelete(GroupUser self, GroupUser target) {
        if (self == null || target == null) {
            return false;
        }
        if (isHuiZhangOrFuHuiZhang(self.getUserRole())) {
            return true;
        } else {
            if (!isUnder(self, target)) {
                return false;
            }
            if (self.getPromoterLevel() != target.getPromoterLevel() - 1) {
                return false;
            }
        }
        return true;
    }

    /**
     * 是否可以设置权限
     *
     * @return
     */
    public static boolean canSetRole(int selfRole, int targetRole) {
        return selfRole < targetRole;
    }

    /**
     * 权限：groupUser对targetGroupUser的上下分操作权限
     *
     * @param groupUser
     * @param targetGroupUser 必须包含userRole,userGroup,promoterLevel,promoterId1,promoterId2,promoterId3,promoterId4
     * @return opType 1：上、下分 2：上分  0：无权限
     */
    public static int getOpType(GroupUser groupUser, Map<String, Object> targetGroupUser) {
        GroupUser target = new GroupUser();
        try {
            BeanUtils.populate(target, targetGroupUser);
        } catch (Exception e) {
            return 0;
        }
        return getOpType(groupUser, target);
    }

    /**
     * 权限：groupUser对targetGroupUser的上下分操作权限
     * <p>
     * 1、会长和副会长可给所有人上下分，副会长不能给同级的副会长和会长上下下分；
     * 2、其他管理角色上下分规则为：因为数据由分成线来构成，即在你的分成线上的所有下级你都能看到，但还需要根据玩家角色的身份高低牌判断是否可以给其上下分，若比你身份高则不可上下分（同级可以给其上下分），显示上下分按钮，点击后提示权限不足；
     * 3、身份比自己高的下级玩家也不可给自己上级上分；
     *
     * @param self
     * @param target
     * @return opType 1：上、下分 2：上分  0：无权限
     */
    public static int getOpType(GroupUser self, GroupUser target) {
        int res = 0;
        if (self == null || target == null) {
            return res;
        }
        int selfRole = self.getUserRole();
        int targetRole = target.getUserRole();
        if (isHuiZhang(selfRole)) {
            // 会长
            res = 1;
            return res;
        }

        if (self.getUserId().longValue() == target.getUserId().longValue()) {
            res = 0;
            return res;
        }
        if (isFuHuiZhang(self.getUserRole())) {
            // 副会长
            res = 2;
            if (!isHuiZhangOrFuHuiZhang(targetRole)) {
                res = 1;
            }
        } else if (isChengYuan(self.getUserRole())) {
            // 普通成员
            res = 0;
        } else {
            if (!isUnder(self, target)) {
                // 不在自己的发展线上
                res = 0;
            } else {
                // 在自己发展线上
                if (selfRole >= targetRole) {
                    // 职位比自己高
                    res = 1;
                } else {
                    res = 1;
                }
            }
        }
        return res;
    }

    /**
     * 是不比自己职位低
     *
     * @param self
     * @param target
     * @return
     */
    public static boolean isSmaller(GroupUser self, GroupUser target) {
        if (self == null || target == null) {
            return false;
        }
        return isSmaller(self.getUserRole(), target.getUserRole());
    }

    /**
     * 是不比自己职位低
     *
     * @param selfRole
     * @param targetRole
     * @return
     */
    public static boolean isSmaller(int selfRole, int targetRole) {
        return targetRole > selfRole;
    }

    /**
     * 是不比自己职位高
     *
     * @param selfRole
     * @param targetRole
     * @return
     */
    public static boolean isBigger(int selfRole, int targetRole) {
        return !isSmaller(selfRole, targetRole);
    }


    /**
     * 是否是自己发展线上的
     *
     * @param self
     * @param target
     * @return
     */
    public static boolean isUnder(GroupUser self, GroupUser target) {
        if (self == null || target == null) {
            return false;
        }
        long selfId = self.getUserId();
        boolean res = false;
        switch (self.getPromoterLevel()) {
            case 1:
                res = target.getPromoterId1() == selfId;
                break;
            case 2:
                res = target.getPromoterId2() == selfId;
                break;
            case 3:
                res = target.getPromoterId3() == selfId;
                break;
            case 4:
                res = target.getPromoterId4() == selfId;
                break;
            case 5:
                res = target.getPromoterId5() == selfId;
                break;
            case 6:
                res = target.getPromoterId6() == selfId;
                break;
            case 7:
                res = target.getPromoterId7() == selfId;
                break;
            case 8:
                res = target.getPromoterId8() == selfId;
                break;
            case 9:
                res = target.getPromoterId9() == selfId;
                break;
            case 10:
                res = target.getPromoterId10() == selfId;
                break;
        }
        return res;
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
     * 设置promoterId
     * 将相应level的上级id设置成指定的id
     *
     * @param self
     * @param promoterLevel
     * @param promoterId
     */
    public static void setPromoterId(GroupUser self, int promoterLevel, long promoterId) {
        switch (promoterLevel) {
            case 1:
                self.setPromoterId1(promoterId);
                break;
            case 2:
                self.setPromoterId2(promoterId);
                break;
            case 3:
                self.setPromoterId3(promoterId);
                break;
            case 4:
                self.setPromoterId4(promoterId);
                break;
            case 5:
                self.setPromoterId5(promoterId);
                break;
            case 6:
                self.setPromoterId6(promoterId);
                break;
            case 7:
                self.setPromoterId7(promoterId);
                break;
            case 8:
                self.setPromoterId8(promoterId);
                break;
            case 9:
                self.setPromoterId9(promoterId);
                break;
            case 10:
                self.setPromoterId10(promoterId);
                break;
        }
    }

    public static GroupUserTree genGroupUserTree(GroupUser root, List<GroupUser> guList) {
        GroupUserTree res = new GroupUserTree(root);

        Map<Long, GroupUserTree> map = new HashMap<>();
        map.put(root.getUserId(), res);

        Collections.sort(guList, Comparator.comparingInt(GroupUser::getPromoterLevel));

        for (GroupUser gu : guList) {
            if (gu.getUserId().equals(root.getUserId())) {
                continue;
            }
            GroupUserTree guTree = map.get(gu.getUserId());
            if (guTree == null) {
                guTree = new GroupUserTree(gu);
            }
            GroupUserTree pre = map.get(gu.getPromoterId());
            guTree.setPre(pre);
            pre.addNextList(guTree);
            map.put(gu.getUserId(), guTree);
        }
        return res;
    }

    public static void genUpdateSqlForModifyRelation(GroupUserTree root, List<HashMap<String, Object>> sqlList) {
        // 先生成自己的
        GroupUserTree pre = root.getPre();
        if (pre == null) {
            return;
        }

        root.setPromoterId(pre.getUserId());
        root.setPromoterLevel(pre.getPromoterLevel() + 1);
        root.setPromoterId1(pre.getPromoterId1());
        root.setPromoterId2(pre.getPromoterId2());
        root.setPromoterId3(pre.getPromoterId3());
        root.setPromoterId4(pre.getPromoterId4());
        root.setPromoterId5(pre.getPromoterId5());
        root.setPromoterId6(pre.getPromoterId6());
        root.setPromoterId7(pre.getPromoterId7());
        root.setPromoterId8(pre.getPromoterId8());
        root.setPromoterId9(pre.getPromoterId9());
        root.setPromoterId10(pre.getPromoterId10());

        for (int level = root.getPromoterLevel(); level <= 10; level++) {
            root.setPromoterId(level, 0);
        }
        if (!isChengYuan(root.getUserRole())) {
            root.setPromoterId(root.getPromoterLevel(), root.getUserId());
        }
        sqlList.add(root.genUpdateSql());

        // 生成下级的
        List<GroupUserTree> nextList = root.getNextList();
        if (nextList != null && nextList.size() > 0) {
            for (GroupUserTree next : nextList) {
                genUpdateSqlForModifyRelation(next, sqlList);
            }
        }
    }


    /**
     * 亲友圈是否被禁止
     *
     * @param group
     * @return
     */
    public static boolean isGroupForbidden(GroupInfo group) {
        if (group == null) {
            return true;
        }
        JSONObject jsonObject = StringUtils.isBlank(group.getExtMsg()) ? new JSONObject() : JSONObject.parseObject(group.getExtMsg());
        if ("1".equals(jsonObject.get("forbidden"))) {
            return true;
        }
        return false;
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
    /*** 亲友圈牌桌列表：已开局的牌桌数量***/
    public static final String groupExtKey_tableNum= "tableNum";
    /*** 亲友圈玩法牌桌样式***/
    public static final String groupExtKey_tableStyle= "1";
    /*** 亲友圈玩法牌桌背景***/
    public static final String groupExtKey_tableBg= "2";
    /*** 亲友圈是否开启私密房功能***/
    public static final String groupExtKey_privateRoom= "privateRoom";
    /*** 亲友圈是否开启：禁止解散***/
    public static final String groupExtKey_forbiddenDiss = "forbiddenDiss";
    /*** 亲友圈是否开启：禁止踢人限制 0：否，1：是 开启后合伙人踢出的人三天内不能被其他合伙人拉入群***/
    public static final String groupExtKey_forbiddenKickOut = "forbiddenKickOut";


    public static Object getGroupExt(String extMsg, String key) {
        if (StringUtils.isBlank(extMsg) || StringUtils.isBlank(key)) {
            return null;
        }
        JSONObject extJson = JSONObject.parseObject(extMsg);
        return extJson.get(key);
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
        if (gu == null) {
            return true;
        }
        if (gu.getUserLevel() == null || gu.getUserLevel().intValue() <= 0) {
            return true;
        }
        return false;
    }


    /**
     * 是否有权限对玩家进行禁止游戏
     *
     * @param self
     * @param target
     * @return
     */
    public static boolean isCanForbid(GroupUser self, GroupUser target) {
        if (self == null || target == null) {
            return false;
        } else if (self.getUserId().longValue() == target.getUserId().longValue()) {
            return false;
        }
        if (isHuiZhang(self.getUserRole())) {
            return true;
        } else if (isFuHuiZhang(self.getUserRole())) {
            if (!isHuiZhangOrFuHuiZhang(target.getUserRole())) {
                return true;
            }
        } else if (isZhuGuan(self.getUserRole()) && self.getPromoterLevel() == 2) {
            if (self.getUserId().longValue() == target.getPromoterId2().longValue()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否可以创建这个玩法
     *
     * @param group
     * @param playType 玩法id
     * @return
     */
    public static boolean canCreatePlayType(GroupInfo group, int playType) {
        if (group == null || playType <= 0) {
            return false;
        }
        if (group.getGameIdSet().size() == 0) {
            return true;
        } else {
            return group.getGameIdSet().contains(playType);
        }
    }

    /**
     *  亲友圈互斥
     * @param userId1
     * @param userId2
     * @return
     */
    public static Long genGroupUserRejectKey(long userId1 , long userId2){
        if (userId1 < userId2) {
            return Long.valueOf(userId1 * 10000 + "" + userId2);
        } else {
            return Long.valueOf(userId2 * 10000 + "" + userId1);
        }
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

    public static GroupGoldCommissionConfig getSysGoldCommissionConfig(int seq) {
        return SYS_GOLD_COMMISSION_LOG_MAP.get(seq);
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
