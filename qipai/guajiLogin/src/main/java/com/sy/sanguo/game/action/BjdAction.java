package com.sy.sanguo.game.action;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sy.mainland.util.CommonUtil;
import com.sy.mainland.util.HttpUtil;
import com.sy.sanguo.common.struts.GameStrutsAction;
import com.sy.sanguo.common.util.*;
import com.sy.sanguo.common.util.user.GameUtil;
import com.sy.sanguo.game.bean.RegInfo;
import com.sy.sanguo.game.bean.ServerConfig;
import com.sy.sanguo.game.bean.UserExtendInfo;
import com.sy.sanguo.game.bean.gold.GoldPlayer;
import com.sy.sanguo.game.bean.group.GroupInfo;
import com.sy.sanguo.game.bean.group.GroupReview;
import com.sy.sanguo.game.bean.group.GroupUser;
import com.sy.sanguo.game.bean.group.LogGroupUserAlert;
import com.sy.sanguo.game.constants.GroupConstants;
import com.sy.sanguo.game.dao.GoldDao;
import com.sy.sanguo.game.dao.ServerDaoImpl;
import com.sy.sanguo.game.dao.UserDao;
import com.sy.sanguo.game.dao.UserDaoImpl;
import com.sy.sanguo.game.dao.UserRelationDaoImpl;
import com.sy.sanguo.game.dao.group.GroupCreditDao;
import com.sy.sanguo.game.dao.group.GroupDao;
import com.sy.sanguo.game.dao.group.GroupDaoNew;
import com.sy.sanguo.game.dao.group.GroupRecycleDao;
import com.sy.sanguo.game.pdkuai.db.dblock.DbLockEnum;
import com.sy.sanguo.game.pdkuai.db.dblock.DbLockUtil;
import com.sy.sanguo.game.pdkuai.manager.StatisticsManager;
import com.sy.sanguo.game.pdkuai.user.Manager;
import com.sy.sanguo.game.utils.BjdUtil;
import com.sy.sanguo.game.utils.GroupUtil;
import com.sy.sanguo.game.utils.LoginUtil;
import com.sy.sanguo.game.utils.bean.CommonRes;
import com.sy599.sanguo.util.ResourcesConfigsUtil;
import com.sy599.sanguo.util.SysPartitionUtil;
import com.sy599.sanguo.util.TimeUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.StringJoiner;

public class BjdAction extends GameStrutsAction {

    private static final Logger LOGGER = LoggerFactory.getLogger("sys");

    private static final int min_group_id = 1000;

    private UserDaoImpl userDao;

    private GroupDao groupDao;

    private UserRelationDaoImpl userRelationDao;

    private GroupCreditDao groupCreditDao;

    private GroupDaoNew groupDaoNew;

    private GroupRecycleDao groupRecycleDao;

    public void setUserDao(UserDaoImpl userDao) {
        this.userDao = userDao;
    }

    public void setGroupDao(GroupDao groupDao) {
        this.groupDao = groupDao;
    }

    public void setGroupCreditDao(GroupCreditDao groupCreditDao) {
        this.groupCreditDao = groupCreditDao;
    }

    public void setUserRelationDao(UserRelationDaoImpl userRelationDao) {
        this.userRelationDao = userRelationDao;
    }

    public void setGroupDaoNew(GroupDaoNew groupDaoNew) {
        this.groupDaoNew = groupDaoNew;
    }

    public void setGroupRecycleDao(GroupRecycleDao groupRecycleDao) {
        this.groupRecycleDao = groupRecycleDao;
    }

    /**
     * 白金岛后台修改钻石后，通知用户更新钻石
     */
    public void notifyChangCards() {
        Map<String, String> params = null;
        try {
            params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("BjdAction|notifyChangCards|{}", params);
            if (!BjdUtil.checkSign(params)) {
                response(-1, LangMsg.getMsg(LangMsg.code_1));
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), 0);
            int cards = NumberUtils.toInt(params.get("cards"), 0);
            int freeCards = NumberUtils.toInt(params.get("freeCards"), 0);

            RegInfo userInfo = userDao.getUser(userId);
            if (userInfo != null && userInfo.getEnterServer() > 0) {
                ServerConfig serverConfig = new ServerConfig();
                serverConfig.setId(userInfo.getEnterServer());
                serverConfig = ServerDaoImpl.getInstance().queryServer(userInfo.getEnterServer());
                if (serverConfig != null) {
                    String url = serverConfig.getIntranet();
                    if (StringUtils.isBlank(url)) {
                        url = serverConfig.getHost();
                    }
                    if (StringUtils.isNotBlank(url)) {
                        int idx = url.indexOf(".");
                        if (idx > 0) {
                            idx = url.indexOf("/", idx);
                            if (idx > 0) {
                                url = url.substring(0, idx);
                            }
                            url += "/online/notice.do?type=notifyChangCards&userId=" + userInfo.getUserId() + "&cards=" + cards + "&freeCards=" + freeCards;
                            String noticeRet = HttpUtil.getUrlReturnValue(url);
                            LOGGER.info("BjdAction|notifyChangCards|result|{}|{}", url, noticeRet);
                            response(0, LangMsg.getMsg(LangMsg.code_0));
                            return;
                        }
                    }
                }

            }
        } catch (Exception e) {
            LOGGER.error("BjdAction|notifyChangCards|error|" + JSON.toJSONString(params), e.getMessage(), e);
            response(2, LangMsg.getMsg(LangMsg.code_4));
        }
        response(2, LangMsg.getMsg(LangMsg.code_4));
    }


    public void clientLogMsg() {
        Map<String, String> params = null;
        try {
            params = UrlParamUtil.getParameters(getRequest());
            LOGGER.debug("BjdAction|clientLogMsg|" + params);
            if (!BjdUtil.checkSign(params)) {
//                response(1, LangMsg.getMsg(LangMsg.code_1));
//                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), 0);
            String logMsg = params.get("logMsg");
            if (StringUtils.isNotBlank(logMsg)) {
                String ip = IpUtil.getIpAddr(request);
                LOGGER.info("clientLogMsg|" + userId + "|" + ip + "|" + logMsg);
                response(0, LangMsg.getMsg(LangMsg.code_0));
            }
        } catch (Exception e) {
            LOGGER.error("BjdAction|clientLogMsg|error|" + JSON.toJSONString(params), e.getMessage(), e);
            response(2, LangMsg.getMsg(LangMsg.code_4));
        }
        response(2, LangMsg.getMsg(LangMsg.code_4));
    }


    /**
     * 创建亲友圈
     */
    public void createGroup() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("createGroup|params|" + params);
            if (!BjdUtil.checkSign(params)) {
                response(-1, LangMsg.getMsg(LangMsg.code_1));
                return;
            }
            String groupName = params.get("groupName");
            long userId = NumberUtils.toLong(params.get("userId"), 0);
            int groupId = NumberUtils.toInt(params.get("groupId"), 0);

            if (StringUtils.isBlank(groupName)) {
                response(1, "请输入亲友圈名字！");
                return;
            } else if (userId <= 0 || groupId < 0) {
                response(1, LangMsg.getMsg(LangMsg.code_3));
                return;
            }
            String regex = "^[a-z0-9A-Z\u4e00-\u9fa5]+$";
            if (!groupName.matches(regex)) {
                response(1, "亲友圈名称仅限字母数字和汉字");
                return;
            }
            groupName = groupName.trim();
            String groupName0 = KeyWordsFilter.getInstance().filt(groupName);
            if (!groupName.equals(groupName0)) {
                response(1, "亲友圈名不能包含敏感字符");
                return;
            }
            String gameIds = params.get("gameIds");

            RegInfo user = userDao.getUser(userId);
            if (user == null) {
                response(2, "玩家ID错误");
                return;
            }
            synchronized (BjdAction.class) {
                CommonRes res = GroupUtil.createGroup(userDao, groupDaoNew, userId, groupId, groupName, gameIds);
                if (res.getCode() != 0) {
                    response(res.getCode(), res.getMsg());
                    return;
                }
                groupId = (int) res.getData();

                response(MessageBuilder.newInstance().builderCodeMessage(0, "创建成功").builder("groupId", groupId));
                LOGGER.info("createGroup|succ|" + JSON.toJSONString(groupId));
            }
        } catch (Exception e) {
            response(2, LangMsg.getMsg(LangMsg.code_4));
            LOGGER.error("createGroup|error|" + e.getMessage(), e);
        }
    }

    /**
     * 通过分享链接
     * 申请进群
     */
    public void inviteJoinGroup() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("applyJoinGroup|params|" + params);
            if (!BjdUtil.checkSign(params)) {
                response(-1, LangMsg.getMsg(LangMsg.code_1));
                return;
            }
            String unionId = params.get("unionId");
            long groupId = NumberUtils.toLong(params.get("groupId"), 0);
            long inviterId = NumberUtils.toLong(params.get("inviterId"), 0);

            if (StringUtils.isBlank(unionId)) {
                response(1, "参数错误：openId或unionId为空");
                return;
            }
            if (groupId <= 0) {
                response(2, "参数错误：groupId不能为空");
                return;
            }
            GroupInfo group = groupDao.loadGroupInfo(groupId, 0);
            if (group == null) {
                response(3, "参数错误：groupId为[" + groupId + "]的用户不存在");
                return;
            } else if (group.getGroupMode().intValue() != 0) {
                response(4, "亲友圈不接受申请");
                return;
            } else if (group.getCurrentCount().intValue() >= group.getMaxCount().intValue()) {
                response(5, "亲友圈人员已满");
                return;
            }
            RegInfo regInfo = userDao.getUserByUnionid(unionId);
            long userId = 0;
            if (regInfo == null) {
                response(5, "角色不存在，请先创建角色");
                return;
//                // 创建用户
//                String openId = params.get("openId");
//                String nickname = params.get("nickname");
//                int sex = NumberUtils.toInt(params.get("sex"), 0);
//                String headImgUrl = params.get("headImgUrl");
//                synchronized (BjdAction.class) {
//                    String os = params.get("os");
//                    String pf = "weixinbjd";
//                    if ("2".equals(os)) {
//                        pf = "weixinbjdIOS";
//                    }
//                    regInfo = new RegInfo();
//                    JsonWrapper userJson = new JsonWrapper("");
//                    userJson.putString("openid", openId);
//                    userJson.putString("nickname", StringUtil.filterEmoji(nickname));
//                    userJson.putString("headimgurl", headImgUrl);
//                    userJson.putString("unionid", unionId);
//                    userJson.putInt("sex", sex);
//                    long maxId = Manager.getInstance().generatePlayerId(userDao);
//                    Manager.getInstance().buildBaseUser(regInfo, pf, maxId);
//                    WeixinUtil.createRole(userJson, regInfo);
//                    userId = userDao.addUser(regInfo);
//                    userRelationDao.insert(new ThirdRelation(regInfo.getUserId(), pf, openId));
//                }
            } else {
                userId = regInfo.getUserId();
                GroupUser gu = groupDao.loadGroupUser(userId, groupId);
                if (gu != null) {
                    response(7, "参数错误：userId为[" + userId + "]的用户已是亲友圈[" + groupId + "]的成员");
                    return;
                }
                GroupUser inviter = groupDao.loadGroupUser(inviterId, groupId);
                if (inviter == null || GroupConstants.isMember(inviter.getUserRole())) {
                    response(5, "邀请人已不在该亲友圈，或邀请人权限不够");
                    return;
                }
            }
            MessageBuilder resMsg = MessageBuilder.newInstance();
            resMsg.builderCodeMessage(0, "申请加入成功");
            resMsg.builder("userId", userId);
            resMsg.builder("groupId", groupId);
            List<GroupReview> reviewList = groupDao.loadGroupReviewByUser(userId, groupId);
            if (reviewList != null && reviewList.size() > 0) {
                for (GroupReview review : reviewList) {
                    if (review.getReviewMode() == 0 || review.getReviewMode() == 1 || review.getReviewMode() == 3) {
                        response(resMsg);
                        return;
                    }
                }
            }

            // 创建加入亲友圈邀请
            GroupReview groupReview = new GroupReview();
            groupReview.setCreatedTime(new Date());
            groupReview.setCurrentState(0);
            groupReview.setGroupId(groupId);
            groupReview.setGroupName(group.getGroupName());
            groupReview.setReviewMode(3);
            groupReview.setUserId(userId);
            groupReview.setUserName(regInfo.getName());
            groupReview.setCurrentOperator(inviterId);
            if (groupDao.createGroupReview(groupReview) <= 0) {
                response(9, "操作失败，请稍后再试");
                return;
            }

            response(resMsg);
            LOGGER.info("applyJoinGroup|succ|" + JSON.toJSONString(params));
        } catch (Exception e) {
            response(99, LangMsg.getMsg(LangMsg.code_4));
            LOGGER.error("applyJoinGroup|error|" + e.getMessage(), e);
        }
    }


    /**
     * 解散亲友圈
     */
    public void dissGroup() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("dissGroup|params|" + params);
            if (!BjdUtil.checkSign(params)) {
                response(-1, LangMsg.getMsg(LangMsg.code_1));
                return;
            }
            int groupId = NumberUtils.toInt(params.get("groupId"), -1);
            if (groupId <= 0) {
                response(1, "参数错误:groupId=" + groupId);
                return;
            }
            GroupInfo groupInfo = groupDao.loadGroupInfo(groupId, 0);
            if (groupInfo == null) {
                response(2, LangMsg.getMsg(LangMsg.code_5));
                return;
            }

            int count = groupCreditDao.countUserHaveCreditForMaster(groupId);
            if (count > 0) {
                response(7, "删除失败：有成员的比赛分不为0！");
                return;
            }

            HashMap<String, Object> map = new HashMap<>();
            map.put("keyId", groupInfo.getKeyId().toString());
            map.put("groupState", "0");
            int ret1 = groupDao.deleteGroupUserByGroupId(groupId);
            int ret2 = groupDao.updateGroupInfoByKeyId(map);
            int ret3 = groupDao.deleteGroupInfoByParentGroup(groupId);
            groupDao.deleteTeamByGroupKey(String.valueOf(groupId));
            response(0, "解散亲友圈成功");
            LOGGER.info("dissGroup|succ|{}|{}|{}|{}", groupId, ret1, ret2, ret3);
        } catch (Exception e) {
            response(99, LangMsg.getMsg(LangMsg.code_4));
            LOGGER.error("dissGroup|error|" + e.getMessage(), e);
        }
    }


    /**
     * 俱乐部踢人
     */
    public void fireUser() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("fireUser|params:{}", params);
            if (!BjdUtil.checkSign(params)) {
                response(-1, LangMsg.getMsg(LangMsg.code_1));
                return;
            }
            long groupId = NumberUtils.toLong(params.get("groupId"), -1);
            long userId = NumberUtils.toLong(params.get("userId"), -1);
            if (userId <= 0 || groupId <= 0) {
                response(1, "参数错误:groupId=" + groupId + ",userId=" + userId);
                return;
            }

            GroupUser groupUser = groupDao.loadGroupUser(userId, groupId);
            if (groupUser == null) {
                response(2, "尚未加入军团");
                return;
            }
            if (groupUser.getCredit() != 0) {
                response(2, "删除失败：有成员的比赛分不为0");
                return;
            }
            int delCount = 0;
            if (GroupConstants.isHuiZhang(groupUser.getUserRole())) {
                GroupInfo groupInfo = groupDao.loadGroupInfo(groupId, 0);

                int count = groupCreditDao.countUserHaveCreditForMaster(groupUser.getGroupId());
                if (count > 0) {
                    response(7, "删除失败：有成员的比赛分不为0！");
                    return;
                }
                // 解散群
                HashMap<String, Object> map = new HashMap<>();
                map.put("keyId", groupInfo.getKeyId().toString());
                map.put("groupState", "0");
                int ret1 = groupDao.deleteGroupUserByGroupId((int) groupId);
                int ret2 = groupDao.updateGroupInfoByKeyId(map);
                int ret3 = groupDao.deleteGroupInfoByParentGroup((int) groupId);
                groupDao.deleteTeamByGroupKey(String.valueOf(groupId));
            } else if (GroupConstants.isZhuGuan(groupUser.getUserRole())) {
                long sumCredit = groupDaoNew.sumCredit(groupId, groupUser.getPromoterLevel(), groupUser.getUserId());
                if (sumCredit != 0) {
                    response(5, "失败:成员或成员下级的比赛分不为0");
                    return;
                }
                if (!groupDaoNew.isAllUserTableOver(groupId, groupUser.getUserId(), groupUser.getPromoterLevel())) {
                    response(6, "该成员或下级正在牌局中，不能进行此操作");
                    return;
                }
                List<GroupUser> list = groupDaoNew.loadGroupUsers(groupId, groupUser.getUserId(), groupUser.getPromoterLevel());
                delCount = groupDaoNew.deleteUserAndLower(groupId, groupUser.getPromoterLevel(), groupUser.getUserId());
                response(0, LangMsg.getMsg(LangMsg.code_0));

                if (list != null && list.size() > 0) {
                    GroupUser master = groupDaoNew.loadGroupMaster(groupId);
                    for (GroupUser del : list) {
                        insertGroupUserAlert(del.getGroupId(), del.getUserId(), master.getUserId(), GroupConstants.TYPE_USER_ALERT_DELETE);
                    }
                }
            } else {
                delCount = groupDao.deleteGroupUserByKeyId(groupUser.getKeyId());
            }
            LOGGER.info("fire group user success:userId={},groupUser={}", groupUser.getUserId(), JSON.toJSONString(groupUser));
            if (delCount > 0) {
                groupDao.updateGroupInfoCount(-delCount, groupId);
            }
            response(0, "操作成功！");
        } catch (Exception e) {
            LOGGER.error("fireUser|error|" + e.getMessage(), e);
        }
    }

    /**
     * 俱乐部转移
     */
    @Deprecated
    public void moveGroup() {
        try {
            if (true) {
                response(-1, "接口已关闭");
                return;
            }
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("moveGroup|params:{}", params);
            if (!BjdUtil.checkSign(params)) {
                response(-1, LangMsg.getMsg(LangMsg.code_1));
                return;
            }
            long toGroupId = NumberUtils.toLong(params.get("toGroupId"), -1);
            long fromGroupId = NumberUtils.toLong(params.get("fromGroupId"), -1);
            if (fromGroupId <= 0 || toGroupId <= 0) {
                response(1, LangMsg.getMsg(LangMsg.code_3));
                return;
            }
            String toGroupIdStr = String.valueOf(toGroupId);
            String fromGroupIdStr = String.valueOf(fromGroupId);
            GroupInfo fromGroup = groupDao.loadGroupInfo(fromGroupId, 0);
            if (fromGroup == null) {
                response(2, LangMsg.getMsg(LangMsg.code_3));
                return;
            }
            GroupInfo toGroup = groupDao.loadGroupInfo(toGroupId, 0);
            if (toGroup == null) {
                response(2, LangMsg.getMsg(LangMsg.code_3));
                return;
            }

            GroupUser fromMaster = groupDao.loadGroupMaster(fromGroupIdStr);
            if (fromMaster == null) {
                response(2, LangMsg.getMsg(LangMsg.code_3));
                return;
            }

            // 目标俱乐部所有人
            List<GroupUser> existList = groupDao.loadAllGroupUser(toGroupId);
            Map<Long, GroupUser> existMap = new HashMap<>();
            if (existList != null && existList.size() > 0) {
                for (GroupUser tmp : existList) {
                    existMap.put(tmp.getUserId(), tmp);
                }
            }

            // 被转移俱乐部所有人
            List<GroupUser> fromList = groupDao.loadAllGroupUser(fromGroupId);
            Collections.sort(fromList, Comparator.comparingInt(GroupUser::getPromoterLevel));

            GroupUser exist = groupDao.loadGroupUser(fromMaster.getUserId(), toGroupId);
            if (exist != null && !GroupConstants.isTeamLeader(exist.getUserRole())) {
                response(2, LangMsg.getMsg(LangMsg.code_3));
                return;
            }
            Map<Long, GroupUser> newMap = new HashMap<>();
            GroupUser newGu;
            String userGroupStr;
            if (exist == null) {
                // 创建小组
                String teamName = fromMaster.getUserId().toString();
                HashMap<String, Object> map = new HashMap<>();
                map.put("groupKey", toGroupIdStr);
                map.put("teamName", teamName);
                map.put("teamGroup", 0);
                map.put("createdTime", CommonUtil.dateTimeToString());
                long userGroup = groupDao.saveGroupRelation(map);
                userGroupStr = String.valueOf(userGroup);

                // 将群主转到新群,作为组长
                newGu = genGroupUserForMove(toGroup, fromMaster);
                newGu.setUserGroup(userGroupStr);
                newGu.setUserRole(GroupConstants.GROUP_ROLE_TEAM_LEADER);
                newMap.put(newGu.getUserId(), newGu);
            } else {
                userGroupStr = exist.getUserGroup();
                newMap.put(exist.getUserId(), exist);
            }

            for (GroupUser from : fromList) {
                if (existMap.containsKey(from.getUserId())) {
                    continue;
                } else if (GroupConstants.isMaster(from.getUserRole())) {
                    continue;
                } else if (GroupConstants.isAdmin(from.getUserRole())) {
                    // 管理员,设为普通成员
                    newGu = genGroupUserForMove(toGroup, from);
                    newGu.setUserRole(GroupConstants.GROUP_ROLE_MEMBER);
                    newGu.setPromoterLevel(0);
                    newGu.setUserGroup(userGroupStr);

                    newMap.put(newGu.getUserId(), newGu);
                } else if (from.getPromoterLevel() == 0 && GroupConstants.isMember(from.getUserRole())) {
                    // 群主下一级普通成员，设为普通成员
                    newGu = genGroupUserForMove(toGroup, from);
                    newGu.setUserRole(GroupConstants.GROUP_ROLE_MEMBER);
                    newGu.setPromoterLevel(0);
                    newGu.setUserGroup(userGroupStr);

                    newMap.put(newGu.getUserId(), newGu);
                } else if (GroupConstants.isTeamLeader(from.getUserRole())) {
                    // 小组长，变为一级拉手
                    newGu = genGroupUserForMove(toGroup, from);
                    newGu.setUserGroup(userGroupStr);
                    newGu.setUserRole(GroupConstants.GROUP_ROLE_PROMOTOR);
                    newGu.setPromoterLevel(1);
                    newGu.setPromoterId1(newGu.getUserId());

                    newMap.put(newGu.getUserId(), newGu);
                    long promoterId1 = newGu.getPromoterId1();

                    // 全部下级,设置为二级
                    for (GroupUser next : fromList) {
                        if (existMap.containsKey(next.getUserId())) {
                            continue;
                        }
                        if (next.getPromoterLevel() == 1 && next.getUserGroup().equals(from.getUserGroup())) {
                            newGu = genGroupUserForMove(toGroup, next);
                            newGu.setUserGroup(userGroupStr);
                            if (GroupConstants.isMember(next.getUserRole())) {
                                newGu.setUserRole(GroupConstants.GROUP_ROLE_MEMBER);
                            } else if (GroupConstants.isPromotor(next.getUserRole())) {
                                newGu.setPromoterId2(newGu.getUserId());
                                newGu.setUserRole(GroupConstants.GROUP_ROLE_PROMOTOR);
                            }
                            newGu.setPromoterLevel(2);
                            newGu.setPromoterId1(promoterId1);

                            newMap.put(newGu.getUserId(), newGu);
                        }
                    }
                } else if (GroupConstants.isPromotor(from.getUserRole()) && from.getPromoterLevel() == 1) {
                    // 一级拉手
                    GroupUser pre = newMap.get(from.getUserId());
                    if (pre == null) {
                        continue;
                    }

                    // 将下级(二级)改为三级
                    for (GroupUser next : fromList) {
                        if (existMap.containsKey(next.getUserId())) {
                            continue;
                        }
                        if (next.getPromoterLevel() == 2 && next.getPromoterId1() == from.getUserId()) {
                            newGu = genGroupUserForMove(toGroup, next);
                            newGu.setUserGroup(userGroupStr);
                            if (GroupConstants.isMember(next.getUserRole())) {
                                newGu.setUserRole(GroupConstants.GROUP_ROLE_MEMBER);
                            } else if (GroupConstants.isPromotor(next.getUserRole())) {
                                newGu.setPromoterId3(newGu.getUserId());
                                newGu.setUserRole(GroupConstants.GROUP_ROLE_PROMOTOR);
                            }
                            newGu.setPromoterLevel(3);
                            newGu.setPromoterId1(pre.getPromoterId1());
                            newGu.setPromoterId2(pre.getPromoterId2());

                            newMap.put(newGu.getUserId(), newGu);
                        }
                    }
                } else if (GroupConstants.isPromotor(from.getUserRole()) && from.getPromoterLevel() == 2) {
                    // 二级拉手
                    GroupUser pre = newMap.get(from.getUserId());
                    if (pre == null) {
                        continue;
                    }
                    // 将下级(三级)改为四级
                    for (GroupUser next : fromList) {
                        if (existMap.containsKey(next.getUserId())) {
                            continue;
                        }
                        if (next.getPromoterLevel() == 3 && next.getPromoterId2() == from.getUserId()) {
                            newGu = genGroupUserForMove(toGroup, next);
                            newGu.setUserGroup(userGroupStr);
                            if (GroupConstants.isMember(next.getUserRole())) {
                                newGu.setUserRole(GroupConstants.GROUP_ROLE_MEMBER);
                            } else if (GroupConstants.isPromotor(next.getUserRole())) {
                                newGu.setPromoterId4(newGu.getUserId());
                                newGu.setUserRole(GroupConstants.GROUP_ROLE_PROMOTOR);
                            }
                            newGu.setPromoterLevel(4);
                            newGu.setPromoterId1(pre.getPromoterId1());
                            newGu.setPromoterId2(pre.getPromoterId2());
                            newGu.setPromoterId3(pre.getPromoterId3());

                            newMap.put(newGu.getUserId(), newGu);
                        }

                    }
                } else if (GroupConstants.isPromotor(from.getUserRole()) && from.getPromoterLevel() == 3) {
                    // 三级拉手
                    GroupUser pre = newMap.get(from.getUserId());
                    if (pre == null) {
                        continue;
                    }
                    // 将下级(四级和五级)改为五级
                    for (GroupUser next : fromList) {
                        if (existMap.containsKey(next.getUserId())) {
                            continue;
                        }
                        if ((next.getPromoterLevel() == 4 || next.getPromoterLevel() == 5) && next.getPromoterId3() == from.getUserId()) {
                            newGu = genGroupUserForMove(toGroup, next);
                            newGu.setUserGroup(userGroupStr);
                            if (GroupConstants.isMember(next.getUserRole())) {
                                newGu.setUserRole(GroupConstants.GROUP_ROLE_MEMBER);
                            } else if (GroupConstants.isPromotor(next.getUserRole())) {
                                if (next.getPromoterLevel() == 4) {
                                    newGu.setUserRole(GroupConstants.GROUP_ROLE_PROMOTOR);
                                } else if (next.getPromoterLevel() == 5) {
                                    newGu.setUserRole(GroupConstants.GROUP_ROLE_MEMBER);
                                }
                            }
                            newGu.setPromoterLevel(5);
                            newGu.setPromoterId1(pre.getPromoterId1());
                            newGu.setPromoterId2(pre.getPromoterId2());
                            newGu.setPromoterId3(pre.getPromoterId3());
                            newGu.setPromoterId4(pre.getPromoterId4());

                            newMap.put(newGu.getUserId(), newGu);
                        }
                    }
                }
            }
            int moveCount = 0;
            for (GroupUser to : newMap.values()) {
                long guId = groupDao.createGroupUser(to);
                if (guId > 0) {
                    moveCount++;
                    LOGGER.info("moveGroup|" + fromGroupId + "|" + toGroupId + "|" + JSON.toJSONString(to));
                }
            }

            // 更新人数
            groupDao.updateGroupInfoCount(moveCount, toGroupId);

            LOGGER.info("moveGroup|succ|" + fromGroupId + "|" + toGroupId + "|" + moveCount);
            response(0, "操作成功！");
        } catch (Exception e) {
            LOGGER.error("moveGroup|error|" + e.getMessage(), e);
            response(0, "操作失败，请联系管理员");
        }
    }

    public GroupUser genGroupUserForMove(GroupInfo toGroup, GroupUser from) {
        GroupUser to = new GroupUser();
        to.setGroupName(toGroup.getGroupName());
        to.setGroupId(toGroup.getGroupId());
        to.setUserId(from.getUserId());
        to.setUserName(from.getUserName());
        to.setUserNickname(from.getUserNickname());
        to.setPromoterLevel(0);
        to.setInviterId(0L);
        to.setCredit(0l);
        to.setPlayCount1(0);
        to.setPlayCount2(0);
        to.setUserLevel(1);
        to.setRefuseInvite(1);
        to.setPromoterId1(0L);
        to.setPromoterId2(0L);
        to.setPromoterId3(0L);
        to.setPromoterId4(0L);
        return to;
    }


    /**
     * 亲友圈旧用户数据结构升级到新的结构
     */
    @Deprecated
    public void upgradeGroupUser() {
        try {
            if (true) {
                response(-1, "接口已关闭");
                return;
            }
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("upgradeGroupUser|params:{}", params);
            if (!BjdUtil.checkSign(params)) {
                response(-1, LangMsg.getMsg(LangMsg.code_1));
                return;
            }
            List<GroupInfo> groupList = groupDaoNew.loadAllGroup();
            if (groupList == null || groupList.size() == 0) {
                response(0, "操作成功！");
                return;
            }
            long groupId = NumberUtils.toLong(params.get("groupId"), -1);
            for (GroupInfo group : groupList) {
                if (groupId > 0 && group.getGroupId() != groupId) {
                    continue;
                }
                // 旧接口取旧数据
                GroupUser master = groupDao.loadGroupMaster(group.getGroupId().toString());
                if (master == null) {
                    continue;
                }

                List<GroupUser> guList = groupDao.loadAllGroupUser(group.getGroupId());
                Collections.sort(guList, Comparator.comparingInt(GroupUser::getPromoterLevel));

                List<HashMap<String, Object>> upgradeList = new ArrayList<>();
                Map<Long, GroupUser> userMap = new HashMap<>();
                Map<String, GroupUser> teamLeaderMap = new HashMap<>();

                // 群主
                HashMap<String, Object> map = new HashMap<>();
                map.put("keyId", master.getKeyId());
                map.put("userRole", GroupConstants.USER_ROLE_HuiZhang);
                map.put("promoterLevel", 1);
                map.put("promoterId1", master.getUserId());

                master.setUserRole(GroupConstants.USER_ROLE_HuiZhang);
                master.setPromoterLevel(1);
                master.setPromoterId1(master.getUserId());

                upgradeList.add(map);
                userMap.put(master.getUserId(), master);

                for (GroupUser gu : guList) {
                    map = new HashMap<>();
                    map.put("keyId", gu.getKeyId());
                    if (gu.getUserId().equals(master.getUserId())) {
                        continue;
                    }
                    if (gu.getPromoterLevel() == 0) {

                        map.put("promoterLevel", 2);
                        map.put("promoterId", master.getUserId());
                        map.put("promoterId1", master.getUserId());

                        gu.setPromoterLevel(2);
                        gu.setPromoterId(master.getUserId());
                        gu.setPromoterId1(master.getUserId());

                        if (GroupConstants.isAdmin(gu.getUserRole())) {
                            map.put("userRole", GroupConstants.USER_ROLE_FuHuiZhang);
                            gu.setUserRole(GroupConstants.USER_ROLE_FuHuiZhang);
                        } else if (GroupConstants.isTeamLeader(gu.getUserRole())) {
                            map.put("userRole", GroupConstants.USER_ROLE_ZhuGuan);
                            map.put("promoterId2", gu.getUserId());
                            gu.setUserRole(GroupConstants.USER_ROLE_ZhuGuan);
                            gu.setPromoterId2(gu.getUserId());
                            teamLeaderMap.put(gu.getUserGroup(), gu);
                        } else {
                            map.put("userRole", GroupConstants.USER_ROLE_ChengYuan);
                            gu.setUserRole(GroupConstants.USER_ROLE_ChengYuan);
                        }
                    } else {
                        long preId = 0;
                        if (gu.getPromoterLevel() == 1) {
                            // 找组长
                            GroupUser teamLeader = teamLeaderMap.get(gu.getUserGroup());
                            if (teamLeader != null) {
                                preId = teamLeader.getUserId();
                            }
                        } else if (gu.getPromoterLevel() == 2) {
                            preId = gu.getPromoterId1();
                        } else if (gu.getPromoterLevel() == 3) {
                            preId = gu.getPromoterId2();
                        } else if (gu.getPromoterLevel() == 4) {
                            preId = gu.getPromoterId3();
                        } else if (gu.getPromoterLevel() == 5) {
                            preId = gu.getPromoterId4();
                        }
                        GroupUser pre = userMap.get(preId);
                        if (pre == null) {
                            continue;
                        }

                        map.put("promoterLevel", pre.getPromoterLevel() + 1);
                        map.put("promoterId", pre.getUserId());
                        map.put("promoterId1", pre.getPromoterId1());
                        map.put("promoterId2", pre.getPromoterId2());
                        map.put("promoterId3", pre.getPromoterId3());
                        map.put("promoterId4", pre.getPromoterId4());
                        map.put("promoterId5", pre.getPromoterId5());
                        map.put("promoterId6", pre.getPromoterId6());
                        map.put("promoterId7", pre.getPromoterId7());
                        map.put("promoterId8", pre.getPromoterId8());
                        map.put("promoterId9", pre.getPromoterId9());
                        map.put("promoterId10", pre.getPromoterId10());

                        gu.setPromoterLevel(pre.getPromoterLevel() + 1);
                        gu.setPromoterId(pre.getUserId());
                        gu.setPromoterId1(pre.getPromoterId1());
                        gu.setPromoterId2(pre.getPromoterId2());
                        gu.setPromoterId3(pre.getPromoterId3());
                        gu.setPromoterId4(pre.getPromoterId4());
                        gu.setPromoterId5(pre.getPromoterId5());
                        gu.setPromoterId6(pre.getPromoterId6());
                        gu.setPromoterId7(pre.getPromoterId7());
                        gu.setPromoterId8(pre.getPromoterId8());
                        gu.setPromoterId9(pre.getPromoterId9());
                        gu.setPromoterId10(pre.getPromoterId10());

                        if (GroupConstants.isPromotor(gu.getUserRole())) {
                            map.put("userRole", GroupConstants.USER_ROLE_ZhuGuan);
                            map.put("promoterId" + gu.getPromoterLevel(), gu.getUserId());
                            gu.setUserRole(GroupConstants.USER_ROLE_ZhuGuan);
                            GroupConstants.setPromoterId(gu, gu.getPromoterLevel(), gu.getUserId());
                        } else {
                            map.put("userRole", GroupConstants.USER_ROLE_ChengYuan);
                            gu.setUserRole(GroupConstants.USER_ROLE_ChengYuan);
                        }
                    }
                    for (int i = 1; i <= 10; i++) {
                        if (GroupConstants.isChengYuan(gu.getUserRole())) {
                            if (i >= gu.getPromoterLevel()) {
                                map.put("promoterId" + i, 0);
                                GroupConstants.setPromoterId(gu, i, 0);
                            }
                        } else {
                            if (i > gu.getPromoterLevel()) {
                                map.put("promoterId" + i, 0);
                                GroupConstants.setPromoterId(gu, i, 0);
                            }
                        }
                    }

                    upgradeList.add(map);
                    userMap.put(gu.getUserId(), gu);
                }

                for (HashMap<String, Object> upgrade : upgradeList) {
                    groupDaoNew.updateGroupUserByKeyId(upgrade);
                    LOGGER.info("upgradeGroupUser|succ|user|" + JSON.toJSONString(upgrade));
                }
                LOGGER.info("upgradeGroupUser|succ|group|" + group.getGroupId());
            }
            response(0, "操作成功！");
        } catch (Exception e) {
            LOGGER.error("upgradeGroupUser|error|" + e.getMessage(), e);
            response(0, "操作失败，请联系管理员");
        }
    }

    /**
     * 转移俱乐部成员及下属所有成员到另外一个俱乐部的某个管理名下
     */
    public void moveGroupUser() {
        long groupId = -1;
        String unLockKey = null;
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("moveGroupUser|params:{}", params);
            if (!BjdUtil.checkSign(params)) {
                response(-1, LangMsg.getMsg(LangMsg.code_1));
                return;
            }
            // 被转移的俱乐部id
            long fromGroupId = NumberUtils.toLong(params.get("fromGroupId"), -1);
            // 被转移的管理人员id
            long fromUserId = NumberUtils.toLong(params.get("fromUserId"), -1);
            // 转移到的俱乐部id
            long toGroupId = NumberUtils.toLong(params.get("toGroupId"), -1);
            // 转移到的管理人员id
            long toUserId = NumberUtils.toLong(params.get("toUserId"), -1);
            // 是否保留信用分 0否，1是
            int retainCredit = NumberUtils.toInt(params.get("retainCredit"), 0);
            // 在指定活跃的天数内，玩家在该俱乐部参与过牌局
            int activeDay = NumberUtils.toInt(params.get("activeDay"), 0);


            if (fromGroupId <= 0 || toGroupId <= 0 || toUserId <= 0 || fromUserId <= 0 || fromGroupId == toGroupId || fromUserId == toUserId) {
                response(1, LangMsg.getMsg(LangMsg.code_3));
                return;
            }
            groupId = toGroupId;
            unLockKey = DbLockUtil.lock(DbLockEnum.GROUP_USER_MODIFY_RELATION, String.valueOf(toGroupId));
            if (unLockKey == null) {
                response(-3, LangMsg.getMsg(LangMsg.code_12));
                return;
            }
            GroupInfo fromGroup = groupDaoNew.loadGroupInfo(fromGroupId);
            if (fromGroup == null) {
                response(2, "被转移的亲友圈不存在");
                return;
            }
            GroupUser fromUser = groupDaoNew.loadGroupUser(fromGroupId, fromUserId);
            if (fromUser == null) {
                response(2, "被转移的成员不存在");
                return;
            } else if (GroupConstants.isChengYuan(fromUser.getUserRole()) || GroupConstants.isFuHuiZhang(fromUser.getUserRole())) {
                response(2, "被转移的成员不能被转移");
                return;
            }

            GroupInfo toGroup = groupDaoNew.loadGroupInfo(toGroupId);
            if (toGroup == null) {
                response(2, "目标亲友圈不存在");
                return;
            }
            GroupUser toUser = groupDaoNew.loadGroupUser(toGroupId, toUserId);
            if (toUser == null || GroupConstants.isChengYuan(toUser.getUserRole()) || GroupConstants.isFuHuiZhang(toUser.getUserRole())) {
                response(2, "目标成员不存在");
                return;
            } else if (toUser.getPromoterLevel() == 10) {
                response(2, "目标成员已经10级了");
                return;
            }


            // 目标俱乐部所有人
            List<GroupUser> existList = groupDaoNew.loadAllGroupUser(toGroupId);
            Map<Long, GroupUser> existMap = new HashMap<>();
            if (existList != null && existList.size() > 0) {
                for (GroupUser tmp : existList) {
                    existMap.put(tmp.getUserId(), tmp);
                }
            }
            Set<Long> activeSet = new HashSet<>();
            if (activeDay > 0) {
                Calendar c = Calendar.getInstance();
                c.add(Calendar.DAY_OF_YEAR, -Math.abs(activeDay));
                c.set(Calendar.HOUR_OF_DAY, 0);
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                int activeDate = Integer.parseInt(sdf.format(c.getTime()));
                List<Long> activeUserIds = groupDaoNew.loadGroupUsersByDataStatistics(fromGroupId, fromUserId, fromUser.getPromoterLevel(), activeDate);
                if (activeUserIds != null && activeUserIds.size() > 0) {
                    activeSet.addAll(activeUserIds);
                }
            }

            // 被转移的所有人
            List<GroupUser> fromList = groupDaoNew.loadGroupUsers(fromGroupId, fromUserId, fromUser.getPromoterLevel());
            Collections.sort(fromList, Comparator.comparingInt(GroupUser::getPromoterLevel));
            GroupUser exist = groupDaoNew.loadGroupUser(toGroupId, fromUserId);
            if (exist != null) {
                if (exist.getPromoterId() != toUserId) {
                    response(2, "被转移用户已存在，但不是目标用户的直接下级");
                    return;
                }
                if (GroupConstants.isChengYuan(exist.getUserRole()) || GroupConstants.isFuHuiZhang(exist.getUserRole())) {
                    response(2, "被转移用户已存在，但不能存在直接下级");
                    return;
                }
            }

            Map<Long, GroupUser> newMap = new HashMap<>();
            GroupUser newGu;
            if (exist != null) {
                newMap.put(exist.getUserId(), exist);
            } else {
                // 最上级用户
                newGu = genGroupUserForMove(toGroup, fromUser, toUser);
                if (retainCredit == 1) {
                    // 保留信用分
                    newGu.setCredit(fromUser.getCredit());
                }
                // 带职位的成员，设置本级
                GroupConstants.setPromoterId(newGu, newGu.getPromoterLevel(), newGu.getUserId());
                // 将高于本级的promoterId都设置为0
                for (int i = 1; i <= 10; i++) {
                    if (i > newGu.getPromoterLevel()) {
                        GroupConstants.setPromoterId(newGu, i, 0);
                    }
                }
                if (GroupConstants.isHuiZhang(newGu.getUserRole())) {
                    newGu.setUserRole(GroupConstants.USER_ROLE_ZhuGuan);
                }
                if (newGu.getPromoterLevel() >= 11) {
                    newGu.setPromoterLevel(11);
                    newGu.setUserRole(GroupConstants.USER_ROLE_ChengYuan);
                }
                newMap.put(newGu.getUserId(), newGu);
            }

            for (GroupUser from : fromList) {
                if (existMap.containsKey(from.getUserId())) {
                    continue;
                } else if (from.getUserId().equals(fromUserId)) {
                    continue;
                } else if (activeDay > 0 && GroupConstants.isChengYuan(from.getUserRole()) && !activeSet.contains(from.getUserId())) {
                    continue;
                }
                GroupUser pre = newMap.get(from.getPromoterId());
                if (pre == null) {
                    continue;
                }


                newGu = genGroupUserForMove(toGroup, from, pre);
                if (retainCredit == 1) {
                    // 保留信用分
                    newGu.setCredit(from.getCredit());
                }
                if (!GroupConstants.isChengYuan(from.getUserRole()) && !GroupConstants.isFuHuiZhang(from.getUserRole())) {
                    // 带职位的成员，设置本级
                    GroupConstants.setPromoterId(newGu, newGu.getPromoterLevel(), newGu.getUserId());
                }
                if (GroupConstants.isFuHuiZhang(from.getUserRole())) {
                    // 副会长变为普通成员
                    newGu.setUserRole(GroupConstants.USER_ROLE_ChengYuan);
                }

                if (newGu.getPromoterLevel() >= 11) {
                    newGu.setPromoterLevel(11);
                    newGu.setUserRole(GroupConstants.USER_ROLE_ChengYuan);
                }

                // 将高于本级的promoterId都设置为0
                for (int i = 1; i <= 10; i++) {
                    if (GroupConstants.isChengYuan(newGu.getUserRole())) {
                        if (i >= newGu.getPromoterLevel()) {
                            GroupConstants.setPromoterId(newGu, i, 0);
                        }
                    } else {
                        if (i > newGu.getPromoterLevel()) {
                            GroupConstants.setPromoterId(newGu, i, 0);
                        }
                    }
                }
                newMap.put(newGu.getUserId(), newGu);
            }
            int moveCount = 0;
            for (GroupUser to : newMap.values()) {
                if (to.getKeyId() != null && to.getKeyId() > 0) {
                    continue;
                }
                long newKeyId = groupDaoNew.createGroupUser(to);
                if (newKeyId > 0) {
                    moveCount++;
                    to.setKeyId(newKeyId);
                    LOGGER.info("moveGroup|" + fromGroupId + "|" + toGroupId + "|" + JSON.toJSONString(to));
                }
            }

            // 更新人数
            groupDaoNew.updateGroupUserCount(toGroupId);

            LOGGER.info("moveGroupUser|succ|" + fromGroupId + "|" + toGroupId + "|" + moveCount);
            response(0, "操作成功！");
        } catch (Exception e) {
            LOGGER.error("moveGroupUser|error|" + e.getMessage(), e);
            response(0, "操作失败，请联系管理员");
        } finally {
            if (unLockKey != null) {
                DbLockUtil.unLock(DbLockEnum.GROUP_USER_MODIFY_RELATION, String.valueOf(groupId), unLockKey);
            }
        }
    }


    public GroupUser genGroupUserForMove(GroupInfo toGroup, GroupUser from, GroupUser preUser) {
        GroupUser to = new GroupUser();
        to.setGroupName(toGroup.getGroupName());
        to.setGroupId(toGroup.getGroupId());
        to.setUserId(from.getUserId());
        to.setUserName(from.getUserName());
        to.setUserNickname(from.getUserNickname());
        to.setInviterId(0L);
        to.setCredit(0l);
        to.setPlayCount1(0);
        to.setPlayCount2(0);
        to.setUserLevel(1);
        to.setRefuseInvite(1);
        to.setUserRole(from.getUserRole());
        to.setPromoterId(preUser.getUserId());
        to.setPromoterLevel(preUser.getPromoterLevel() + 1);
        to.setPromoterId1(preUser.getPromoterId1());
        to.setPromoterId2(preUser.getPromoterId2());
        to.setPromoterId3(preUser.getPromoterId3());
        to.setPromoterId4(preUser.getPromoterId4());
        to.setPromoterId5(preUser.getPromoterId5());
        to.setPromoterId6(preUser.getPromoterId6());
        to.setPromoterId7(preUser.getPromoterId7());
        to.setPromoterId8(preUser.getPromoterId8());
        to.setPromoterId9(preUser.getPromoterId9());
        to.setPromoterId10(preUser.getPromoterId10());
        return to;
    }

    private void insertGroupUserAlert(long groupId, long userId, long optUserId, int type) {
        LogGroupUserAlert log = new LogGroupUserAlert();
        log.setGroupId(groupId);
        log.setUserId(userId);
        log.setOptUserId(optUserId);
        log.setType(type);
        try {
            groupDaoNew.insertGroupUserAlert(log);
        } catch (Exception e) {
            LOGGER.error("insertGroupUserAlert|error|" + e.getMessage(), e);
        }
    }


    /**
     * 分圈时的禁止接口
     */
    public void forbidGroup() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("BjdAction|forbidGroup|params|" + params);
            if (!BjdUtil.checkSign(params)) {
                response(-1, LangMsg.getMsg(LangMsg.code_1));
                return;
            }
            long groupId = NumberUtils.toLong(params.get("groupId"), 0);
            String stat = params.get("stat");  // 0：否，1：是
            GroupInfo group = groupDaoNew.loadGroupInfo(groupId);
            if (group == null) {
                response(2, LangMsg.getMsg(LangMsg.code_5));
                return;
            }

            HashMap<String, Object> map = new HashMap<>();
            JSONObject jsonObject = StringUtils.isBlank(group.getExtMsg()) ? new JSONObject() : JSONObject.parseObject(group.getExtMsg());
            jsonObject.put("forbidden", stat);
            jsonObject.put("stopCreate", stat);
            map.put("keyId", group.getKeyId());
            map.put("extMsg", jsonObject.toString());
            groupDaoNew.updateGroupByKeyId(map);

            MessageBuilder msg = MessageBuilder.newInstance();
            msg.builderCodeMessage(0, LangMsg.getMsg(LangMsg.code_0));
            msg.builder("groupId", groupId);
            msg.builder("stat", stat);
            response(msg);
            LOGGER.info("BjdAction|forbidGroup|succ|" + groupId);
        } catch (Exception e) {
            response(2, LangMsg.getMsg(LangMsg.code_4));
            LOGGER.error("forbidGroup|error|" + e.getMessage(), e);
        }
    }

    /**
     * 开启关闭新金币系统
     */
    public void switchCoin() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("BjdAction|switchCoin|params|" + params);
            if (!BjdUtil.checkSign(params)) {
                response(-1, LangMsg.getMsg(LangMsg.code_1));
                return;
            }
            long groupId = NumberUtils.toLong(params.get("groupId"), 0);
            String switchCoin = params.get("switchCoin");  // 0：否，1：是
            GroupInfo group = groupDaoNew.loadGroupInfo(groupId);
            if (group == null) {
                response(2, LangMsg.getMsg(LangMsg.code_5));
                return;
            }

            HashMap<String, Object> map = new HashMap<>();
            map.put("keyId", group.getKeyId());
            map.put("switchCoin", switchCoin);
            groupDaoNew.updateGroupByKeyId(map);

            MessageBuilder msg = MessageBuilder.newInstance();
            msg.builderCodeMessage(0, LangMsg.getMsg(LangMsg.code_0));
            msg.builder("groupId", groupId);
            msg.builder("switchCoin", switchCoin);
            response(msg);
            LOGGER.info("BjdAction|switchCoin|succ|" + groupId);
        } catch (Exception e) {
            response(2, LangMsg.getMsg(LangMsg.code_4));
            LOGGER.error("forbidGroup|error|" + e.getMessage(), e);
        }
    }


    /**
     * 回收亲友圈id
     * 10天内没有进行过牌桌
     * 将不再使用的亲友圈id回收，删除所有相关数据
     */
    public void recycleGroupId() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("BjdAction|recycleGroupId|params|" + params);
            if (!BjdUtil.checkSign(params)) {
                response(-1, LangMsg.getMsg(LangMsg.code_1));
                return;
            }
            long groupId = NumberUtils.toLong(params.get("groupId"), 0);
            if (groupId <= 0) {
                response(2, LangMsg.getMsg(LangMsg.code_5));
                return;
            }
            GroupInfo group = groupDaoNew.loadGroupInfo(groupId);
            if (group == null) {
                response(2, LangMsg.getMsg(LangMsg.code_5));
                return;
            }
            if (groupRecycleDao.countGroupTable(groupId) > 0) {
                response(2, "回收失败：15天内有进行过牌局");
                return;
            }

            groupRecycleDao.deleteGroup(groupId);
            groupRecycleDao.deleteGroupUser(groupId);
            groupRecycleDao.deleteGroupUserLog(groupId);
            groupRecycleDao.deleteGroupCommissionConfig(groupId);
            groupRecycleDao.deleteGroupCreditConfig(groupId);
            groupRecycleDao.deleteGroupReview(groupId);
            groupRecycleDao.deleteGroupTable(groupId);
            groupRecycleDao.deleteGroupTableConfig(groupId);
            if (SysPartitionUtil.isWritePartition()) {
                groupRecycleDao.deleteTableUserPartition(groupId);
                groupRecycleDao.deleteGroupCreditLogPartition(groupId);
            }
            if (SysPartitionUtil.isWriteMaster()) {
                groupRecycleDao.deleteTableUserMaster(groupId);
                groupRecycleDao.deleteGroupCreditLogMaster(groupId);
            }
            groupRecycleDao.deleteTableRecord(groupId);
            groupRecycleDao.deleteLogGroupCommission(groupId);
            groupRecycleDao.deleteLogGroupTable(groupId);
            groupRecycleDao.deleteBjdDataStatistics(groupId);
            groupRecycleDao.deleteDataStatistics(groupId);
            groupRecycleDao.deleteBjdGroupNewerBind(groupId);
            groupRecycleDao.deleteLogGroupExp(groupId);
            groupRecycleDao.deleteLogGroupUserExp(groupId);
            groupRecycleDao.deleteLogGroupUserAlert(groupId);
            groupRecycleDao.deleteLogGroupUserLevel(groupId);
            groupRecycleDao.deleteGroupUserReject(groupId);

            LOGGER.info("BjdAction|recycleGroupId|succ|" + groupId);

            MessageBuilder msg = MessageBuilder.newInstance();
            msg.builderCodeMessage(0, LangMsg.getMsg(LangMsg.code_0));
            msg.builder("groupId", groupId);
            response(msg);
        } catch (Exception e) {
            LOGGER.error("forbidGroup|error|" + e.getMessage(), e);
            response(2, LangMsg.getMsg(LangMsg.code_4));
        }
    }


    /**
     * 更换群主
     * 新群主不在群内，需要新建用户
     */
    public void changeMaster() {
        long groupId = -1;
        String unLockKey = null;
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("BjdAction|changeMaster|params|" + params);
            if (!BjdUtil.checkSign(params)) {
                response(-1, LangMsg.getMsg(LangMsg.code_1));
                return;
            }
            groupId = NumberUtils.toLong(params.get("groupId"), 0);
            long userId = NumberUtils.toLong(params.get("userId"), 0);
            if (groupId <= 0 || userId <= 0) {
                response(2, LangMsg.getMsg(LangMsg.code_3));
                return;
            }
            GroupInfo group = groupDaoNew.loadGroupInfo(groupId);
            if (group == null) {
                response(2, LangMsg.getMsg(LangMsg.code_5));
                return;
            }
            unLockKey = DbLockUtil.lock(DbLockEnum.GROUP_USER_MODIFY_RELATION, String.valueOf(groupId));
            if (unLockKey == null) {
                response(-3, LangMsg.getMsg(LangMsg.code_12));
                return;
            }
            RegInfo user = userDao.getUserForceMaster(userId);
            if (user == null) {
                response(2, LangMsg.getMsg(LangMsg.code_14));
                return;
            }
            GroupUser oldMaster = groupDaoNew.loadGroupMaster(groupId);
            GroupUser newMaster = groupDaoNew.loadGroupUser(groupId, userId);
            if (newMaster == null) {
                newMaster = GroupUtil.genGroupUser(oldMaster, user, null);
                long keyId = groupDaoNew.createGroupUser(newMaster);
                if (keyId <= 0) {
                    response(MessageBuilder.newInstance().builderCodeMessage(1002, "操作失败，请稍后再试"));
                    return;
                }
                newMaster.setKeyId(keyId);
                // 拒绝亲友圈内其他邀请
                groupDaoNew.rejectTeamInvite(groupId, userId);
                // 更新亲友圈人数
                groupDao.updateGroupInfoCount(1, groupId);
            }
            if (!GroupConstants.isChengYuan(newMaster.getUserRole()) && !GroupConstants.isFuHuiZhang(newMaster.getUserRole())) {
                response(2, "用户已存在，且不能转为群主");
                return;
            }
            // ---------  所有成员的promoterId1换成新群主  -----------------
            HashMap<String, Object> allMap1 = new HashMap<>();
            allMap1.put("groupId", groupId);
            allMap1.put("setSql", " SET promoterId1=" + newMaster.getUserId());
            allMap1.put("whereSql", "");
            groupDaoNew.updateGroupUser(allMap1);

            // ---------  所有二级成员的promoterId换成新群主  -----------------
            HashMap<String, Object> allMap2 = new HashMap<>();
            allMap2.put("groupId", groupId);
            allMap2.put("setSql", " SET promoterId=" + newMaster.getUserId());
            allMap2.put("whereSql", " AND promoterLevel = 2 ");
            groupDaoNew.updateGroupUser(allMap2);

            // ---------  旧群主的userRole、promoterId、promoterLevel  -----------------
            HashMap<String, Object> oldMap = new HashMap<>();
            oldMap.put("keyId", oldMaster.getKeyId());
            oldMap.put("userRole", GroupConstants.USER_ROLE_ChengYuan);
            oldMap.put("promoterId", newMaster.getUserId());
            oldMap.put("promoterId1", newMaster.getUserId());
            oldMap.put("promoterLevel", 2);
            groupDaoNew.updateGroupUserByKeyId(oldMap);

            // ---------  新群主的 userRole、promoterId、promoterLevel ------------------
            HashMap<String, Object> newMap = new HashMap<>();
            newMap.put("keyId", newMaster.getKeyId());
            newMap.put("userRole", GroupConstants.USER_ROLE_HuiZhang);
            newMap.put("promoterLevel", 1);
            newMap.put("promoterId", 0);
            newMap.put("promoterId1", newMaster.getUserId());
            newMap.put("promoterId2", 0);
            newMap.put("promoterId3", 0);
            newMap.put("promoterId4", 0);
            newMap.put("promoterId5", 0);
            newMap.put("promoterId6", 0);
            newMap.put("promoterId7", 0);
            newMap.put("promoterId8", 0);
            newMap.put("promoterId9", 0);
            newMap.put("promoterId10", 0);
            groupDaoNew.updateGroupUserByKeyId(newMap);


            LOGGER.info("BjdAction|changeMaster|succ|" + groupId + "|" + userId);

            MessageBuilder msg = MessageBuilder.newInstance();
            msg.builderCodeMessage(0, LangMsg.getMsg(LangMsg.code_0));
            msg.builder("groupId", groupId);
            response(msg);
        } catch (Exception e) {
            response(2, LangMsg.getMsg(LangMsg.code_4));
            LOGGER.error("forbidGroup|error|" + e.getMessage(), e);
        } finally {
            if (unLockKey != null) {
                DbLockUtil.unLock(DbLockEnum.GROUP_USER_MODIFY_RELATION, String.valueOf(groupId), unLockKey);
            }
        }
    }

    public void genGroupPartitionSeq() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("BjdAction|genGroupPartition|params|" + params);
            SysPartitionUtil.genGroupPartitionSeq();
            MessageBuilder msg = MessageBuilder.newInstance();
            msg.builderCodeMessage(0, LangMsg.getMsg(LangMsg.code_0));
            response(msg);
            LOGGER.info("BjdAction|genGroupPartition|succ|");
        } catch (Exception e) {
            response(2, LangMsg.getMsg(LangMsg.code_4));
            LOGGER.error("genGroupPartition|error|" + e.getMessage(), e);
        }
    }

    public void getGroupSeq() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("BjdAction|getGroupSeq|params|" + params);
            long groupId = NumberUtils.toLong(params.get("groupId"), 0);
            String seq1 = SysPartitionUtil.getGroupSeqForMaster(groupId);
            String seq2 = SysPartitionUtil.getGroupSeqForPartition(groupId);
            MessageBuilder msg = MessageBuilder.newInstance();
            msg.builderCodeMessage(0, LangMsg.getMsg(LangMsg.code_0));
            msg.builder("seq1", seq1);
            msg.builder("seq2", seq2);
            response(msg);
            LOGGER.info("BjdAction|getGroupSeq|succ|");
        } catch (Exception e) {
            response(2, LangMsg.getMsg(LangMsg.code_4));
            LOGGER.error("genGroupPartition|error|" + e.getMessage(), e);
        }
    }


    /**
     * 管理亲友圈玩法限制
     */
    public void updateGroupGameIds() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("updateGroupGameIds|params|" + params);
            if (!BjdUtil.checkSign(params)) {
                response(-1, LangMsg.getMsg(LangMsg.code_1));
                return;
            }
            String groupIds = params.get("groupIds");
            String gameIds = params.get("gameIds");

            if (StringUtils.isBlank(groupIds)) {
                response(1, LangMsg.getMsg(LangMsg.code_3));
                return;
            }

            String[] groupIdSplits = groupIds.split(",");
            for (String groupId : groupIdSplits) {
                if (!StringUtils.isNumeric(groupId)) {
                    response(1, LangMsg.getMsg(LangMsg.code_3));
                    return;
                }
            }
            if (StringUtils.isNotBlank(gameIds)) {
                String[] gameIdSplits = gameIds.split(",");
                for (String gameId : gameIdSplits) {
                    if (!StringUtils.isNumeric(gameId)) {
                        response(1, LangMsg.getMsg(LangMsg.code_3));
                        return;
                    }
                }
            } else {
                gameIds = "";
            }
            int ret = groupDaoNew.updateGroupGameIds(groupIds, gameIds);
            LOGGER.info("updateGroupGameIds|succ|" + groupIds + "|" + gameIds);

            MessageBuilder msg = MessageBuilder.newInstance();
            msg.builderCodeMessage(0, LangMsg.getMsg(LangMsg.code_0));
            msg.builder("ret", ret);
            response(msg);
        } catch (Exception e) {
            LOGGER.error("updateGroupGameIds|error|" + e.getMessage(), e);
            response(2, LangMsg.getMsg(LangMsg.code_4));
        }
    }

    /**
     * 管理亲友圈玩法限制
     */
    public void genGroupGoldWin() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("genGroupGoldWin|params|" + params);
            if (!BjdUtil.checkSign(params)) {
                response(-1, LangMsg.getMsg(LangMsg.code_1));
                return;
            }

            long groupId = NumberUtils.toLong(params.get("groupId"), -1);
            long dataDate = NumberUtils.toLong(params.get("dataDate"), -1);
            if (dataDate <= 0 || !TimeUtil.checkDateFormatYMD(String.valueOf(dataDate))) {
                response(1, LangMsg.getMsg(LangMsg.code_3));
                return;
            }
            if (groupId > 0) {
                StatisticsManager.getInstance().logGroupGoldWin(groupId, dataDate);
            } else {
                StatisticsManager.getInstance().logGroupGoldWin(dataDate);
            }
            LOGGER.info("genGroupGoldWin|succ|" + groupId + "|" + dataDate);
            MessageBuilder msg = MessageBuilder.newInstance();
            msg.builderCodeMessage(0, LangMsg.getMsg(LangMsg.code_0));
            response(msg);
        } catch (Exception e) {
            LOGGER.error("genGroupGoldWin|error|" + e.getMessage(), e);
            response(2, LangMsg.getMsg(LangMsg.code_4));
        }
    }

    /**
     * 修改用户信息
     */
    public void updateUser() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("BjdAction|updateUser|params|" + params);
            if (!BjdUtil.checkSign(params)) {
                response(-1, LangMsg.getMsg(LangMsg.code_1));
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), 0);
            String headimgurl = params.get("headimgurl");
            if (userId == 0 || StringUtils.isBlank(headimgurl)) {
                response(-2, LangMsg.getMsg(LangMsg.code_3));
                return;
            }

            RegInfo user = userDao.getUser(userId);
            if (user == null) {
                response(-3, "用户不存在");
                return;
            } else if (!"self".equals(user.getPf())) {
                response(-3, "非账号登录，不允许修改");
                return;
            }
            UserExtendInfo userExtendInfo = userDao.getUserExtendinfByUserId(userId);
            if (userExtendInfo != null && userExtendInfo.getLastUpHeadimgTime() != null) {
                long now = System.currentTimeMillis();
                if (userExtendInfo.getLastUpHeadimgTime().getTime() + 15 * TimeUtil.DAY_IN_MINILLS > now) {
                    String errorMsg = "下次可修改日期【" + TimeUtil.formatTime(new Date(userExtendInfo.getLastUpHeadimgTime().getTime() + 15 * TimeUtil.DAY_IN_MINILLS)) + "】";
                    response(-3, errorMsg);
                    return;
                }
            }
            Map<String, Object> modify = new HashMap<>();
            modify.put("headimgurl", headimgurl);
            int ret = userDao.updateUser(userId, modify);
            if (ret <= 0) {
                response(-4, LangMsg.getMsg(LangMsg.code_4));
                return;
            }
            MessageBuilder msg = MessageBuilder.newInstance();
            msg.builderCodeMessage(0, LangMsg.getMsg(LangMsg.code_0));
            response(msg);
            GameUtil.sendUpdateUser(user.getEnterServer(), userId, null, headimgurl, null);
            if (userExtendInfo == null) {
                userDao.insertUserExtendinf(userId, "", 0);
            }
            Map<String, Object> map = new HashMap<>();
            map.put("lastUpHeadimgTime", new Date());
            userDao.updateUserExtendinf(userId, map);
            LOGGER.info("BjdAction|updateUser|succ|" + userId + "|" + headimgurl);
        } catch (Exception e) {
            response(2, LangMsg.getMsg(LangMsg.code_4));
            LOGGER.error("updateUser|error|" + e.getMessage(), e);
        }
    }

    /**
     * 查询手机号
     */
    public void decryptPhoneNumAES() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("BjdAction|decryptPhoneNumAES|params|" + params);
            String userIdListStr = params.get("userIdList");
            String pwd = params.get("pwd");
            String localPwd = ResourcesConfigsUtil.loadServerPropertyValue("interface_decryptPhoneNumAES_pwd", "helloYiWei2020");

            if (StringUtils.isBlank(pwd) || !localPwd.equals(pwd)) {
                response(-1, LangMsg.getMsg(LangMsg.code_3));
                return;
            }
            if (!StringUtils.isNotBlank(userIdListStr)) {
                response(-1, LangMsg.getMsg(LangMsg.code_3));
                return;
            }
            String[] splits = userIdListStr.split(",");
            for (String split : splits) {
                if (!StringUtils.isNumeric(split)) {
                    response(-1, LangMsg.getMsg(LangMsg.code_3));
                    return;
                }
            }
            List<HashMap<String, Object>> list = userDao.loadAllUserPhoneNum(userIdListStr);
            Map<String, Map<String, Object>> map = new HashMap<>();
            for (HashMap<String, Object> data : list) {
                map.put(String.valueOf(data.get("userId")), data);
            }
            StringJoiner sj = new StringJoiner(",");
            for (String split : splits) {
                Map<String, Object> data = map.get(split);
                if (data != null) {
                    String phoneNum = String.valueOf(data.get("phoneNum"));
                    if (StringUtils.isNotBlank(phoneNum) && !"null".equals(phoneNum)) {
                        sj.add(String.valueOf(data.get("userId")) + "|" + LoginUtil.decryptPhoneNumAES(phoneNum));
                    } else {
                        sj.add(String.valueOf(data.get("userId")) + "|0");
                    }
                } else {
                    sj.add(split + "|0");
                }
            }
            MessageBuilder msg = MessageBuilder.newInstance();
            msg.builderCodeMessage(0, LangMsg.getMsg(LangMsg.code_0));
            msg.builder("data", sj.toString());
            response(msg);
            LOGGER.info("BjdAction|decryptPhoneNumAES|succ|" + userIdListStr);
        } catch (Exception e) {
            response(2, LangMsg.getMsg(LangMsg.code_4));
            LOGGER.error("decryptPhoneNumAES|error|" + e.getMessage(), e);
        }
    }


    /**
     * 用户发奖励
     */
    public void changeUserCurrency() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("BjdAction|changeUserCurrency|params|" + params);
            if (!BjdUtil.checkSign(params)) {
                response(-1, LangMsg.getMsg(LangMsg.code_1));
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), 0);
            long cards = NumberUtils.toLong(params.get("cards"), 0);
            long freeCards = NumberUtils.toLong(params.get("freeCards"), 0);
            long golds = NumberUtils.toLong(params.get("golds"), 0);
            long freeGolds = NumberUtils.toLong(params.get("freeGolds"), 0);
            long goldBeans = NumberUtils.toLong(params.get("goldBeans"), 0);
            int changeType = NumberUtils.toInt(params.get("changeType"), 0);

            if (changeType <= 20000 || changeType > 30000) {
                response(-2, LangMsg.getMsg(LangMsg.code_3));
                return;
            }
            if (userId == 0) {
                response(-2, LangMsg.getMsg(LangMsg.code_3));
                return;
            }

            RegInfo user = userDao.getUser(userId);
            if (user == null) {
                response(-3, "用户不存在");
                return;
            }

            int retGold = 0;
            if (golds > 0 || freeGolds > 0) {
                retGold = GoldDao.getInstance().addUserGold(userId, freeGolds, golds, changeType);
            }

            int retCard = 0;
            if (freeCards != 0 || cards != 0) {
                if(changeType == 20002){
                    // 指定类型加减钻石，至多减少到0
                    retCard = UserDao.getInstance().addUserCards1(userId, freeCards, cards, changeType);
                }else{
                    retCard = UserDao.getInstance().addUserCards(userId, freeCards, cards, changeType);
                }
            }

            int retGoldBean = 0;
            if (goldBeans > 0) {
                retGoldBean = UserDao.getInstance().addUserGoldenBeans(userId, goldBeans, changeType);
            }

            MessageBuilder msg = MessageBuilder.newInstance();
            msg.builderCodeMessage(0, LangMsg.getMsg(LangMsg.code_0));
            response(msg);
            LOGGER.info("BjdAction|changeUserCurrency|succ|" + userId + "|" + retGold + "|" + retCard + "|" + retGoldBean + "|" + params);
        } catch (Exception e) {
            response(2, LangMsg.getMsg(LangMsg.code_4));
            LOGGER.error("changeUserCurrency|error|" + e.getMessage(), e);
        }
    }

    /**
     * 创建机器人
     */
    public void genRobot() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("BjdAction|genRobot|params|" + params);
            if (!BjdUtil.checkSign(params)) {
                response(-1, LangMsg.getMsg(LangMsg.code_1));
                return;
            }
            int count = NumberUtils.toInt(params.get("count"), 0);
            String platform = "self";
            String password = "123456";
            String deviceCode = "";
            int ret = 0;
            Date now = new Date();
            List<Map<String, Object>> headimgList = UserDao.getInstance().loadRobotSysProp(1, count);
            List<Map<String, Object>> nickNameList = UserDao.getInstance().loadRobotSysProp(2, count);
            StringJoiner usedKeyIdList = new StringJoiner(",");
            Random rnd = new Random();
            for (int i = 0; i < count; i++) {
                if (headimgList.size() < i || nickNameList.size() < i) {
                    continue;
                }
                long userId = Manager.getInstance().generateRobotPlayerId();
                String username = "sr" + userId;
                String nickName = (String)nickNameList.get(i).get("prop");
                String headimgurl = (String)headimgList.get(i).get("prop");
                usedKeyIdList.add(nickNameList.get(i).get("keyId").toString());
                usedKeyIdList.add(headimgList.get(i).get("keyId").toString());
                RegInfo regInfo = new RegInfo();
                regInfo.setIsRobot(1);
                regInfo.setFlatId(username);
                int sex = new Random().nextInt(100) >= 70 ? Constants.SEX_FEMALE : Constants.SEX_MALE;
                regInfo.setSex(sex);
                regInfo.setName(nickName);
                regInfo.setPw(LoginUtil.genPw(password));
                regInfo.setSessCode(LoginUtil.genSessCode(username));
                if (StringUtils.isNotBlank(deviceCode)) {
                    regInfo.setDeviceCode(deviceCode);
                }
                if (StringUtils.isNotBlank(headimgurl)) {
                    regInfo.setHeadimgurl(headimgurl);
                }
                Manager.getInstance().buildBaseUser(regInfo, platform, userId);
                if (this.userDao.addUser(regInfo) > 0) {
                    ret++;
                }

                long give = 3000 * (1 + rnd.nextInt(12));
                GoldPlayer goldInfo = new GoldPlayer();
                goldInfo.setUserId(userId);
                goldInfo.setUserName(nickName);
                goldInfo.setUserNickName(nickName);
                goldInfo.setHeadimgurl(headimgurl);
                goldInfo.setSex(sex);
                goldInfo.setFreeGold(give);
                goldInfo.setGold(0);
                goldInfo.setRegTime(new Date());
                goldInfo.setLastLoginTime(goldInfo.getRegTime());
                GoldDao.getInstance().createGoldUser(goldInfo);

                HashMap<String, Object> robotMap = new HashMap<>();
                robotMap.put("userId", userId);
                robotMap.put("type", 1);
                robotMap.put("used", 0);
                robotMap.put("usedCount", 0);
                robotMap.put("createTime", now);
                robotMap.put("lastUseTime", now);
                UserDao.getInstance().insertRobot(robotMap);

            }

            UserDao.getInstance().useRobotSysProp(usedKeyIdList.toString());
            MessageBuilder msg = MessageBuilder.newInstance();
            msg.builderCodeMessage(0, LangMsg.getMsg(LangMsg.code_0));
            msg.builder("count", count);
            msg.builder("ret", ret);
            response(msg);
            LOGGER.info("BjdAction|genRobot|succ|" + count + "|" + ret + "|" + params);
        } catch (Exception e) {
            response(2, LangMsg.getMsg(LangMsg.code_4));
            LOGGER.error("genRobot|error|" + e.getMessage(), e);
        }
    }

    /**
     * 修改
     */
    public void updateGroup() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("BjdAction|updateGroup|params|" + params);
            if (!BjdUtil.checkSign(params)) {
                response(-1, LangMsg.getMsg(LangMsg.code_1));
                return;
            }
            long groupId = NumberUtils.toLong(params.get("groupId"), 0);
            int optType = NumberUtils.toInt(params.get("optType"), 0);
            GroupInfo group = groupDaoNew.loadGroupInfo(groupId);
            if (group == null) {
                response(-1, LangMsg.getMsg(LangMsg.code_5));
                return;
            }
            HashMap<String, Object> map = new HashMap<>();
            map.put("keyId", group.getKeyId());
            if (optType == 1) {
                int maxCount = NumberUtils.toInt(params.get("maxCount"), 0);
                if (maxCount <= 0) {
                    response(-1, LangMsg.getMsg(LangMsg.code_3));
                    return;
                }
                map.put("maxCount", maxCount);
            } else if (optType == 2) {
                int isCredit = NumberUtils.toInt(params.get("isCredit"), 0);
                if (isCredit != 0 && isCredit != 1) {
                    response(-1, LangMsg.getMsg(LangMsg.code_3));
                    return;
                }
                map.put("isCredit", isCredit);
            } else {
                response(-1, LangMsg.getMsg(LangMsg.code_3));
                return;
            }
            int ret = groupDaoNew.updateGroupByKeyId(map);
            MessageBuilder msg = MessageBuilder.newInstance();
            msg.builderCodeMessage(0, LangMsg.getMsg(LangMsg.code_0));
            msg.builder("ret", ret);
            response(msg);
            LOGGER.info("BjdAction|updateGroup|succ|" + ret + "|" + params);
        } catch (Exception e) {
            response(2, LangMsg.getMsg(LangMsg.code_4));
            LOGGER.error("updateGroup|error|" + e.getMessage(), e);
        }
    }

}
