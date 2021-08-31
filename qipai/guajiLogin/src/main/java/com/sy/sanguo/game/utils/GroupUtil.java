package com.sy.sanguo.game.utils;

import com.alibaba.fastjson.JSONObject;
import com.sy.mainland.util.PropertiesCacheUtil;
import com.sy.sanguo.common.util.Constants;
import com.sy.sanguo.common.util.KeyWordsFilter;
import com.sy.sanguo.common.util.LangMsg;
import com.sy.sanguo.game.action.group.GroupAction;
import com.sy.sanguo.game.bean.RegInfo;
import com.sy.sanguo.game.bean.group.GroupInfo;
import com.sy.sanguo.game.bean.group.GroupUser;
import com.sy.sanguo.game.bean.group.GroupUserLog;
import com.sy.sanguo.game.constants.GroupConstants;
import com.sy.sanguo.game.dao.UserDaoImpl;
import com.sy.sanguo.game.dao.group.GroupDaoNew;
import com.sy.sanguo.game.utils.bean.CommonRes;
import com.sy599.sanguo.util.ResourcesConfigsUtil;
import org.apache.commons.lang3.StringUtils;

import java.security.SecureRandom;
import java.util.Date;

public class GroupUtil {

    private static final int min_group_id = 1000;
    private static final int default_credit_rate = 1;
    private static final int default_is_credit = 0;
    private static final int default_credit_allot_mode = 2;
    private static final int default_group_limit = 100;

    public static CommonRes createGroup(UserDaoImpl userDao, GroupDaoNew groupDaoNew, long userId, int groupId, String groupName, String gameIds) throws Exception {
        if (StringUtils.isBlank(groupName)) {
            return new CommonRes(1, "请输入亲友圈名字！");
        } else if (userId <= 0) {
            return new CommonRes(1, LangMsg.getMsg(LangMsg.code_3));
        }
        String regex = "^[a-z0-9A-Z\u4e00-\u9fa5]+$";
        if (!groupName.matches(regex)) {
            return new CommonRes(1, "亲友圈名称仅限字母数字和汉字");
        }
        groupName = groupName.trim();
        String groupName0 = KeyWordsFilter.getInstance().filt(groupName);
        if (!groupName.equals(groupName0)) {
            return new CommonRes(1, "亲友圈名不能包含敏感字符");
        }

        RegInfo user = userDao.getUser(userId);
        int default_card_limit = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "create_group_card_limit",0);
        if (user == null) {
            return new CommonRes(1, "玩家ID错误");
        } else if (user.getCards() + user.getFreeCards() < default_card_limit) {
            return new CommonRes(1, "钻石数量不足" + default_card_limit + ",创建失败");
        } else if (groupDaoNew.countGroupByCreator(userId) >= default_group_limit) {
            return new CommonRes(1, "创建亲友圈已达上限：" + default_group_limit + "个,创建失败");
        }

        synchronized (GroupAction.class) {
            if (groupId > 0) {
                GroupInfo exist = groupDaoNew.loadGroupInfo(groupId, 0);
                if (exist != null) {
                    return new CommonRes(1, "已存在id为" + groupId + "的亲友圈，请重新设置");
                }
            } else {
                SecureRandom random = new SecureRandom();
                int base = 9000;
                groupId = min_group_id + random.nextInt(base);
                boolean canCreate = false;
                int c = 0;
                while (c < 3) {
                    c++;
                    if (groupDaoNew.existsGroupInfo(groupId, 0)) {
                        groupId = min_group_id * c * 10 + random.nextInt(base * c * 10);
                    } else {
                        canCreate = true;
                        break;
                    }
                }
                if (!canCreate) {
                    return new CommonRes(1, "请稍后再试");
                }
            }
            Date now = new Date();
            GroupInfo group = new GroupInfo();
            group.setCreatedTime(now);
            group.setCreatedUser(userId);
            group.setCurrentCount(1);
            group.setDescMsg("");
            group.setGroupId(groupId);
            group.setGroupLevel(1);
            group.setGroupMode(0);
            group.setGroupName(groupName);
            int default_max_member_count = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "create_group_member_count",500);
            group.setMaxCount(default_max_member_count);
            group.setParentGroup(0);
            group.setGroupState("1");
            group.setModifiedTime(now);
            group.setCreditRate(default_credit_rate);
            group.setIsCredit(default_is_credit);
            group.setCreditAllotMode(default_credit_allot_mode);
            group.setGameIds(gameIds);

            JSONObject jsonObject = new JSONObject();
            String defaultStr = PropertiesCacheUtil.getValueOrDefault("group_kf_default", "", Constants.GAME_FILE);
            if (!StringUtils.isBlank(defaultStr)) {
                String[] strs = defaultStr.split(",");
                for (String value : strs) {
                    if ((value.startsWith("+q") || value.startsWith("-q"))) {
                        jsonObject.put("oq", value);
                    } else if ((value.startsWith("+p3") || value.startsWith("-p3"))) {
                        jsonObject.put("pc", value);
                    } else if ((value.startsWith("+r") || value.startsWith("-r"))) {
                        jsonObject.put("cr", value);
                    }
                }
            }
            group.setExtMsg(jsonObject.toString());
            if (groupDaoNew.createGroup(group) <= 0) {
                return new CommonRes(1, "操作失败，请稍后再试");
            }
            GroupUser groupUser = new GroupUser();
            groupUser.setCreatedTime(new Date());
            groupUser.setGroupId(group.getGroupId());
            groupUser.setGroupName(group.getGroupName());
            groupUser.setInviterId(userId);
            groupUser.setPlayCount1(0);
            groupUser.setPlayCount2(0);
            groupUser.setUserId(userId);
            groupUser.setUserLevel(1);
            groupUser.setUserRole(GroupConstants.USER_ROLE_HuiZhang);
            groupUser.setUserName(user.getName());
            groupUser.setUserNickname(user.getName());
            groupUser.setPromoterLevel(1);
            groupUser.setCredit(0l);
            groupUser.setPromoterId(0L);
            groupUser.setPromoterId1(userId);
            if (groupDaoNew.createGroupUser(groupUser) <= 0) {
                groupDaoNew.deleteGroupInfoByGroupId(group.getGroupId(), 0);
                return new CommonRes(1, "操作失败，请稍后再试");
            }
            CommonRes res = new CommonRes(0, "创建成功");
            res.setData(groupId);
            return res;
        }
    }

    public static GroupUser genGroupUser(GroupUser inviter, RegInfo targetUser, GroupUserLog userLog) {
        long groupId = inviter.getGroupId();
        long targetUserId = targetUser.getUserId();

        GroupUser gu = new GroupUser();
        gu.setCreatedTime(new Date());
        gu.setGroupId((int) groupId);
        gu.setGroupName(inviter.getGroupName());
        gu.setInviterId(inviter.getUserId());
        gu.setPlayCount1(0);
        gu.setPlayCount2(0);
        gu.setUserId(targetUserId);
        gu.setUserLevel(1);
        gu.setUserRole(GroupConstants.USER_ROLE_ChengYuan);
        gu.setUserName(targetUser.getName());
        gu.setUserNickname(targetUser.getName());
        gu.setCredit(userLog != null ? userLog.getCredit() : 0L);
        gu.setInviterId(inviter.getUserId());
        gu.setPromoterId(inviter.getUserId());
        gu.setPromoterLevel(inviter.getPromoterLevel() + 1);
        gu.setPromoterId1(inviter.getPromoterId1());
        gu.setPromoterId2(inviter.getPromoterId2());
        gu.setPromoterId3(inviter.getPromoterId3());
        gu.setPromoterId4(inviter.getPromoterId4());
        gu.setPromoterId5(inviter.getPromoterId5());
        gu.setPromoterId6(inviter.getPromoterId6());
        gu.setPromoterId7(inviter.getPromoterId7());
        gu.setPromoterId8(inviter.getPromoterId8());
        gu.setPromoterId9(inviter.getPromoterId9());
        gu.setPromoterId10(inviter.getPromoterId10());

        switch (gu.getPromoterLevel()) {
            case 2:
                gu.setPromoterId1(inviter.getUserId());
                break;
            case 3:
                gu.setPromoterId2(inviter.getUserId());
                break;
            case 4:
                gu.setPromoterId3(inviter.getUserId());
                break;
            case 5:
                gu.setPromoterId4(inviter.getUserId());
                break;
            case 6:
                gu.setPromoterId5(inviter.getUserId());
                break;
            case 7:
                gu.setPromoterId6(inviter.getUserId());
                break;
            case 8:
                gu.setPromoterId7(inviter.getUserId());
                break;
            case 9:
                gu.setPromoterId8(inviter.getUserId());
                break;
            case 10:
                gu.setPromoterId9(inviter.getUserId());
                break;
        }
        return gu;
    }
}
