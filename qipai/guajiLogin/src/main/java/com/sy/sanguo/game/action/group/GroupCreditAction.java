package com.sy.sanguo.game.action.group;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sy.sanguo.common.struts.GameStrutsAction;
import com.sy.sanguo.common.util.LangMsg;
import com.sy.sanguo.common.util.MessageBuilder;
import com.sy.sanguo.common.util.OutputUtil;
import com.sy.sanguo.common.util.UrlParamUtil;
import com.sy.sanguo.common.util.user.GameUtil;
import com.sy.sanguo.game.bean.RegInfo;
import com.sy.sanguo.game.bean.group.GroupCommissionConfig;
import com.sy.sanguo.game.bean.group.GroupCreditConfig;
import com.sy.sanguo.game.bean.group.GroupInfo;
import com.sy.sanguo.game.bean.group.GroupReview;
import com.sy.sanguo.game.bean.group.GroupTableConfig;
import com.sy.sanguo.game.bean.group.GroupUser;
import com.sy.sanguo.game.bean.group.LogGroupUserAlert;
import com.sy.sanguo.game.constants.GroupConstants;
import com.sy.sanguo.game.constants.TableConfigConstants;
import com.sy.sanguo.game.dao.DataStatisticsDao;
import com.sy.sanguo.game.dao.UserDaoImpl;
import com.sy.sanguo.game.dao.group.GroupCreditDao;
import com.sy.sanguo.game.dao.group.GroupDao;
import com.sy599.sanguo.util.ResourcesConfigsUtil;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;

public class GroupCreditAction extends GameStrutsAction {

    private static final Logger LOGGER = LoggerFactory.getLogger("sys");

    private static final Map<String, Object> lockMap = new ConcurrentHashMap<>();

    private UserDaoImpl userDao;
    private GroupDao groupDao;
    private GroupCreditDao groupCreditDao;
    private DataStatisticsDao dataStatisticsDao;

    public void setUserDao(UserDaoImpl userDao) {
        this.userDao = userDao;
    }

    public void setGroupDao(GroupDao groupDao) {
        this.groupDao = groupDao;
    }

    public void setDataStatisticsDao(DataStatisticsDao dataStatisticsDao) {
        this.dataStatisticsDao = dataStatisticsDao;
    }

    public void setGroupCreditDao(GroupCreditDao groupCreditDao) {
        this.groupCreditDao = groupCreditDao;
    }

    public boolean checkSessCode(long userId, String sessCode) throws Exception {
        RegInfo user = userDao.getUser(userId);
        if (sessCode == null || user == null || !sessCode.equals(user.getSessCode())) {
            OutputUtil.output(4, "???????????????????????????", getRequest(), getResponse(), false);
            return false;
        }
        return true;
    }

    public Object getUserGroupLock(long groupId, String userGroup) {
        String key = groupId + userGroup;
        if (lockMap.containsKey(key)) {
            return lockMap.get(key);
        } else {
            synchronized (lockMap) {
                if (lockMap.containsKey(key)) {
                    return lockMap.get(key);
                }
                Object obj = new Object();
                lockMap.put(key, obj);
                return obj;
            }
        }
    }

    /**
     * ??????????????????
     */
    public void teamList() {
        Map<String, String> params = null;
        try {
            if(true){
                interfaceDeprecated();
                return;
            }
            params = UrlParamUtil.getParameters(getRequest());
            LOGGER.debug("teamList|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), 0);
            if (!checkSessCode(userId, params.get("sessCode"))) {
                return;
            }
            int groupId = NumberUtils.toInt(params.get("groupId"), -1);
            int pageNo = NumberUtils.toInt(params.get("pageNo"), 1);
            int pageSize = NumberUtils.toInt(params.get("pageSize"), 30);

            String keyWord = params.get("keyWord");

            pageNo = pageNo < 0 ? 1 : pageNo;
            pageSize = pageSize < 0 ? 5 : pageSize > 30 ? 30 : pageSize;

            GroupUser groupUser = groupDao.loadGroupUser(userId, groupId);
            if (groupUser == null) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            GroupInfo groupInfo = groupDao.loadGroupInfo(groupId, 0);
            if (groupInfo == null) {
                OutputUtil.output(4, LangMsg.getMsg(LangMsg.code_5), getRequest(), getResponse(), false);
                return;
            }
            GroupUser targetGroupUser = groupUser;
            long targetUserId = NumberUtils.toLong(params.get("targetUserId"), 0);
            if (targetUserId > 0) {
                targetGroupUser = groupDao.loadGroupUser(targetUserId, groupId);
                if (targetGroupUser == null) {
                    OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                    return;
                }
            }

            JSONObject json = new JSONObject();
            json.put("pageNo", pageNo);
            json.put("pageSize", pageSize);
            int teamCount = 0;
            if (GroupConstants.isMasterOrAdmin(targetGroupUser.getUserRole())) {
                GroupUser master = targetGroupUser;
                if (GroupConstants.isAdmin(targetGroupUser.getUserRole())) {
                    master = groupDao.loadGroupMaster(String.valueOf(groupId));
                }
                // ????????????????????????????????????
                teamCount = groupCreditDao.countTeamListForMaster(groupId, keyWord);

                List<Map<String, Object>> teamList = groupCreditDao.teamListForMaster(groupId, keyWord, pageNo, pageSize);
                if (teamList != null && teamList.size() > 0) {
                    for (Map<String, Object> team : teamList) {
                        team.put("canSet", 1);
                        if (team.get("userId").toString().equals(master.getUserId().toString())) {
                            team.put("canSet", 0);
                        }
                    }
                } else {
                    teamList = new ArrayList<>();
                }
                json.put("teamList", teamList);
                json.put("myCredit", groupUser.getCredit());
                json.put("myRate",100);
                json.put("totalCredit", groupCreditDao.sumCreditForMaster(groupId));

            } else if (GroupConstants.isTeamLeader(targetGroupUser.getUserRole())) {
                // ??????????????????????????????????????????
                String userGroup = targetGroupUser.getUserGroup();
                teamCount = groupCreditDao.countTeamListForTeamLeader(groupId, userGroup, keyWord);
                List<Map<String, Object>> teamList = groupCreditDao.teamListForTeamLeader(groupId, userGroup, targetGroupUser.getUserId(), keyWord, pageNo, pageSize);
                if (teamList != null && teamList.size() > 0) {
                    int index = 0;
                    int remIndex = -1;
                    for (Map<String, Object> team : teamList) {
                        team.put("canSet", 1);
                        if (team.get("userId").toString().equals(targetGroupUser.getUserId().toString())) {
                            if (targetUserId > 0) {
                                remIndex = index;
                            }
                            team.put("canSet", 0);
                        }
                        index++;
                    }
                    if (remIndex != -1) {
                        teamList.remove(remIndex);
                    }
                } else {
                    teamList = new ArrayList<>();
                }
                json.put("teamList", teamList);
                json.put("myCredit", groupUser.getCredit());
                json.put("myRate",targetGroupUser.getCreditCommissionRate());
                json.put("totalCredit", groupCreditDao.sumCreditForTeamLeader(groupId, targetGroupUser.getUserGroup()));
            } else if (GroupConstants.isPromotor(targetGroupUser.getUserRole())) {
                // ???????????????????????????????????????
                String userGroup = targetGroupUser.getUserGroup();
                String promoterId = String.valueOf(targetGroupUser.getUserId());
                int promoterLevel = targetGroupUser.getPromoterLevel();
                List<Map<String, Object>> teamList = Collections.emptyList();
                if (promoterLevel < 4) { // 4??????????????????????????????
                    teamCount = groupCreditDao.countTeamListForPromoter(groupId, userGroup, promoterId, promoterLevel, keyWord);
                    teamList = groupCreditDao.teamListForPromoter(groupId, userGroup, promoterId, promoterLevel, keyWord, pageNo, pageSize);
                    if (teamList != null && teamList.size() > 0) {
                        int index = 0;
                        int remIndex = -1;
                        for (Map<String, Object> team : teamList) {
                            team.put("canSet", 1);
                            if (team.get("userId").toString().equals(targetGroupUser.getUserId().toString())) {
                                if (targetUserId > 0) {
                                    remIndex = index;
                                }
                                team.put("canSet", 0);
                            }
                            index++;
                        }
                        if (remIndex != -1) {
                            teamList.remove(remIndex);
                        }
                    }
                } else if (promoterLevel == 4) {
                    if (targetUserId == 0) {
                        Map<String, Map<String, Object>> maps = groupCreditDao.countTeamUserForPromoter4(groupId, userGroup, promoterId);
                        Map<String, Object> selfTeam = new HashMap<>();
                        selfTeam.put("userId", groupUser.getUserId());
                        selfTeam.put("creditCommissionRate", groupUser.getCreditCommissionRate());
                        selfTeam.put("teamName", "??????");
                        RegInfo regInfo = userDao.getUser(userId);
                        if (regInfo != null) {
                            selfTeam.put("headimgurl", regInfo.getHeadimgurl());
                            selfTeam.put("name", regInfo.getName());
                        }
                        if (maps.containsKey("0")) {
                            selfTeam.putAll(maps.get("0"));
                        }
                        teamList = new ArrayList<>();
                        teamList.add(selfTeam);
                    }
                }
                json.put("teamList", teamList);
                json.put("myCredit", groupUser.getCredit());
                json.put("myRate",targetGroupUser.getCreditCommissionRate());
                json.put("totalCredit", groupCreditDao.sumCreditForPromoter(groupId, targetGroupUser.getUserGroup(), targetGroupUser.getUserId(), targetGroupUser.getPromoterLevel()));
            } else {
                // ??????????????????????????????????????????????????????????????????
                List<Map<String, Object>> teamList = new ArrayList<>();
                GroupUser preGroupUser = null;
                boolean showPre = true;
                if(showPre) {
                    if ("0".equals(groupUser.getUserGroup())) {
                        //??????????????????????????????
                        preGroupUser = groupDao.loadGroupMaster(String.valueOf(groupId));
                    } else {
                        if (groupUser.getPromoterLevel() == 1) {
                            // ???????????????
                            preGroupUser = groupDao.loadGroupTeamMaster(String.valueOf(groupId), groupUser.getUserGroup());
                        } else {
                            // ??????????????????
                            long preUserId = 0;
                            if (groupUser.getPromoterLevel() == 2) {
                                preUserId = groupUser.getPromoterId1();
                            } else if (groupUser.getPromoterLevel() == 3) {
                                preUserId = groupUser.getPromoterId2();
                            } else if (groupUser.getPromoterLevel() == 4) {
                                preUserId = groupUser.getPromoterId3();
                            } else if (groupUser.getPromoterLevel() == 5) {
                                preUserId = groupUser.getPromoterId4();
                            }
                            preGroupUser = groupDao.loadGroupUser(preUserId, groupId);
                        }
                    }
                    Map<String, Object> preMap = new HashMap<>();
                    Map<String, Object> preUserBase = userDao.loadUserBase(String.valueOf(preGroupUser.getUserId()));
                    preMap.put("userId", preGroupUser.getUserId());
                    preMap.put("userName", preGroupUser.getUserName());
                    if (preUserBase.get("userName") != null) {
                        preMap.put("userName", preUserBase.get("userName"));
                    }
                    preMap.put("headimgurl", "");
                    if (preUserBase.get("headimgurl") != null) {
                        preMap.put("headimgurl", preUserBase.get("headimgurl"));
                    }
                    preMap.put("credit", preGroupUser.getCredit());
                    preMap.put("opType", 2);
                    teamList.add(preMap);
                }

                Map<String, Object> selfUserBase = userDao.loadUserBase(String.valueOf(groupUser.getUserId()));
                Map<String, Object> selfMap = new HashMap<>();
                selfMap.put("userId", groupUser.getUserId());
                selfMap.put("userName", groupUser.getUserName());
                if(selfUserBase.get("userName") != null) {
                    selfMap.put("userName", selfUserBase.get("userName"));
                }
                selfMap.put("headimgurl", "");
                if(selfUserBase.get("headimgurl") != null) {
                    selfMap.put("headimgurl", selfUserBase.get("headimgurl"));
                }
                selfMap.put("credit", groupUser.getCredit());
                selfMap.put("opType", 0);
                teamList.add(selfMap);
                json.put("teamList", teamList);

                json.put("myCredit", groupUser.getCredit());
                json.put("totalCredit", 0);
            }
            json.put("viewTeamUser", 1);
            json.put("creditRate",groupInfo.getCreditRate());
            json.put("total", teamCount);
            json.put("pages", (int) Math.ceil(teamCount * 1.0 / pageSize));
            json.put("creditAllotMode", groupInfo.getCreditAllotMode());
            OutputUtil.output(0, json, getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("teamList|error|" + JSON.toJSONString(params), e.getMessage(), e);
            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        }
    }


    /**
     * ??????????????????
     */
    public void teamUserList() {
        Map<String, String> params = null;
        try {
            if(true){
                interfaceDeprecated();
                return;
            }
            params = UrlParamUtil.getParameters(getRequest());
            LOGGER.debug("teamUserList|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), 0);
            if (!checkSessCode(userId, params.get("sessCode"))) {
                return;
            }

            int groupId = NumberUtils.toInt(params.get("groupId"), -1);
            int pageNo = NumberUtils.toInt(params.get("pageNo"), 1);
            int pageSize = NumberUtils.toInt(params.get("pageSize"), 30);

            String keyWord = params.get("keyWord");

            pageNo = pageNo < 0 ? 1 : pageNo;
            pageSize = pageSize < 1 ? 5 : pageSize > 30 ? 30 : pageSize;

            GroupUser groupUser = groupDao.loadGroupUser(userId, groupId);
            if (groupUser == null) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            GroupInfo groupInfo = groupDao.loadGroupInfo(groupId, 0);
            if (groupInfo == null) {
                OutputUtil.output(4, LangMsg.getMsg(LangMsg.code_5), getRequest(), getResponse(), false);
                return;
            }
            GroupUser targetGroupUser = groupUser;
            long targetUserId = NumberUtils.toLong(params.get("targetUserId"), 0);
            int viewTeamList = 1;
            if (targetUserId > 0) {
                targetGroupUser = groupDao.loadGroupUser(targetUserId, groupId);
                // ????????????????????????
                if (targetGroupUser != null) {
                    viewTeamList = 0;
                }
            }

            JSONObject json = new JSONObject();
            json.put("pageNo", pageNo);
            json.put("pageSize", pageSize);
            int userCount = 0;
            if (GroupConstants.isMasterOrAdmin(targetGroupUser.getUserRole())) {
                // ???????????????????????????????????????????????????
                userCount = groupCreditDao.countUserListForMaster(groupId, keyWord);
                List<Map<String, Object>> userList = groupCreditDao.userListForMaster(groupId, keyWord, pageNo, pageSize);
                if (userList != null && userList.size() > 0) {
                    // opType 1??????????????? 2?????????  0????????????
                    for (Map<String, Object> user : userList) {
                        user.put("opType", getOpType(groupUser, user));
                    }
                } else {
                    userList = Collections.emptyList();
                }
                json.put("userList", userList);
                json.put("myCredit", groupUser.getCredit());
                json.put("totalCredit", groupCreditDao.sumCreditForMaster(groupId));
            } else if (GroupConstants.isTeamLeader(targetGroupUser.getUserRole())) {
                //???????????????????????????????????????
                String userGroup = targetGroupUser.getUserGroup();
                int promoterLevel = 1;
                userCount = groupCreditDao.countUserListForTeamLeader(groupId, userGroup, promoterLevel, keyWord);
                List<Map<String, Object>> userList = groupCreditDao.userListForTeamLeader(groupId, userGroup, promoterLevel, keyWord, pageNo, pageSize);
                if (userList != null && userList.size() > 0) {
                    // opType 1??????????????? 2?????????  0????????????
                    for (Map<String, Object> user : userList) {
                        user.put("opType", getOpType(groupUser, user));
                    }
                } else {
                    userList = Collections.emptyList();
                }
                json.put("userList", userList);
                json.put("myCredit", groupUser.getCredit());
                json.put("totalCredit", groupCreditDao.sumCreditForTeamLeader(groupId, targetGroupUser.getUserGroup()));
            } else if (GroupConstants.isPromotor(targetGroupUser.getUserRole())) {
                String userGroup = targetGroupUser.getUserGroup();
                long promoterId = targetGroupUser.getUserId();
                int promoterLevel = targetGroupUser.getPromoterLevel();
                userCount = groupCreditDao.countUserListForPromoter(groupId, userGroup, promoterId, promoterLevel, keyWord);
                List<Map<String, Object>> userList = groupCreditDao.userListForPromoter(groupId, userGroup, promoterId, promoterLevel, keyWord, pageNo, pageSize);
                if (userList != null && userList.size() > 0) {
                    // opType 1??????????????? 2?????????  0????????????
                    for (Map<String, Object> user : userList) {
                        user.put("opType", getOpType(groupUser, user));
                    }
                } else {
                    userList = Collections.emptyList();
                }
                json.put("userList", userList);
                json.put("myCredit", groupUser.getCredit());
                json.put("totalCredit", groupCreditDao.sumCreditForPromoter(groupId, targetGroupUser.getUserGroup(), targetGroupUser.getUserId(), targetGroupUser.getPromoterLevel()));
            } else {
                //????????????????????????????????????????????????
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            json.put("viewTeamList", viewTeamList);
            json.put("total", userCount);
            json.put("pages", (int) Math.ceil(userCount * 1.0 / pageSize));
            json.put("creditAllotMode", groupInfo.getCreditAllotMode());
            OutputUtil.output(0, json, getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("teamUserList|error|" + JSON.toJSONString(params), e.getMessage(), e);
            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        }
    }


    /**
     * ????????????
     */
    public void updatePromoter() {
        Map<String, String> params = null;
        try {
            if(true){
                interfaceDeprecated();
                return;
            }
            params = UrlParamUtil.getParameters(getRequest());
            LOGGER.debug("updatePromoter|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), 0);
            if (!checkSessCode(userId, params.get("sessCode"))) {
                return;
            }
            int maxPromoterLevel = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "maxPromoterLevel", 0);
            if (maxPromoterLevel <= 0) {
                OutputUtil.output(1, "????????????????????????", getRequest(), getResponse(), false);
                return;
            }
            if (maxPromoterLevel > 4) {
                maxPromoterLevel = 4;
            }

            long groupId = NumberUtils.toLong(params.get("groupId"), -1);
            long targetUserId = NumberUtils.toLong(params.get("targetUserId"), -1);
            GroupUser groupUser = groupDao.loadGroupUser(userId, groupId);
            if (groupUser == null) {
                OutputUtil.output(1, "?????????????????????[" + userId + "]????????????[" + groupId + "]?????????", getRequest(), getResponse(), false);
                return;
            }
            synchronized (getUserGroupLock(groupId, groupUser.getUserGroup())) {
                groupUser = groupDao.loadGroupUser(userId, groupId);
                if (groupUser == null) {
                    OutputUtil.output(1, "?????????????????????[" + userId + "]????????????[" + groupId + "]?????????", getRequest(), getResponse(), false);
                    return;
                } else if (GroupConstants.isMasterOrAdmin(groupUser.getUserRole()) || GroupConstants.isMember(groupUser.getUserRole())) {
                    OutputUtil.output(4, LangMsg.getMsg(LangMsg.code_8), getRequest(), getResponse(), false);
                    return;
                } else if (maxPromoterLevel == 1 && !GroupConstants.isTeamLeader(groupUser.getUserRole())) {
                    OutputUtil.output(4, LangMsg.getMsg(LangMsg.code_8), getRequest(), getResponse(), false);
                    return;
                } else if (GroupConstants.isPromotor(groupUser.getUserRole()) && groupUser.getPromoterLevel() >= maxPromoterLevel) {
                    OutputUtil.output(4, LangMsg.getMsg(LangMsg.code_8), getRequest(), getResponse(), false);
                    return;
                }
                GroupUser targetGroupUser = groupDao.loadGroupUser(targetUserId, groupId);
                if (targetGroupUser == null) {
                    OutputUtil.output(4, "????????????????????????????????????", getRequest(), getResponse(), false);
                    return;
                }
                // ?????????????????????????????????????????????
                if (!GroupConstants.isMember(targetGroupUser.getUserRole())) {
                    OutputUtil.output(4, "?????????????????????????????????????????????????????????????????????", getRequest(), getResponse(), false);
                    return;
                }
                if (!GroupConstants.isNextLevel(groupUser, targetGroupUser)) {
                    OutputUtil.output(4, "?????????????????????????????????????????????????????????????????????", getRequest(), getResponse(), false);
                    return;
                }

                HashMap<String, Object> map = new HashMap<>();
                map.put("keyId", targetGroupUser.getKeyId());
                map.put("userRole", GroupConstants.GROUP_ROLE_PROMOTOR);
                map.put("promoterId" + targetGroupUser.getPromoterLevel(), targetGroupUser.getUserId());
                groupDao.updateGroupUserByKeyId(map);
            }
            OutputUtil.output(0, LangMsg.getMsg(LangMsg.code_0), getRequest(), getResponse(), false);
            LOGGER.info("updatePromoter|" + groupId + "|" + userId + "|" + targetUserId);
        } catch (Exception e) {
            LOGGER.error("updatePromoter|error|" + JSON.toJSONString(params), e.getMessage(), e);
            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        }
    }

    /**
     * ?????????????????????
     */
    public void deletePromoter() {
        Map<String, String> params = null;
        try {
            if(true){
                interfaceDeprecated();
                return;
            }
            params = UrlParamUtil.getParameters(getRequest());
            LOGGER.debug("deletePromoter|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), 0);
            if (!checkSessCode(userId, params.get("sessCode"))) {
                return;
            }

            long groupId = NumberUtils.toLong(params.get("groupId"), -1);
            long targetUserId = NumberUtils.toLong(params.get("targetUserId"), -1);

            GroupUser groupUser = groupDao.loadGroupUser(userId, groupId);
            if (groupUser == null) {
                OutputUtil.output(1, "?????????????????????[" + userId + "]????????????[" + groupId + "]?????????", getRequest(), getResponse(), false);
                return;
            }
            synchronized (getUserGroupLock(groupId, groupUser.getUserGroup())) {
                groupUser = groupDao.loadGroupUser(userId, groupId);
                if (groupUser == null) {
                    OutputUtil.output(1, "?????????????????????[" + userId + "]????????????[" + groupId + "]?????????", getRequest(), getResponse(), false);
                    return;
                }
                GroupUser targetGroupUser = groupDao.loadGroupUser(targetUserId, groupId);
                if (targetGroupUser == null) {
                    OutputUtil.output(5, "???????????????????????????", getRequest(), getResponse(), false);
                    return;
                }
                if (!GroupConstants.isNextLevel(groupUser, targetGroupUser)) {
                    OutputUtil.output(4, "??????????????????????????????????????????", getRequest(), getResponse(), false);
                    return;
                }
                if (targetGroupUser.getCredit() != 0 || targetGroupUser.getCreditPurse() != 0) {
                    OutputUtil.output(5, "????????????:???????????????????????????0", getRequest(), getResponse(), false);
                    return;
                }
                int delCount = 0;
                if (GroupConstants.isPromotor(targetGroupUser.getUserRole())) {
                    int count = groupCreditDao.countUserHaveCreditForPromoter(groupId, targetGroupUser.getUserGroup(), targetUserId, targetGroupUser.getPromoterLevel());
                    if (count > 0) {
                        OutputUtil.output(5, "????????????:???????????????????????????0", getRequest(), getResponse(), false);
                        return;
                    }

                    List<GroupUser> list = groupCreditDao.allUserForPromoter(groupId, targetGroupUser.getUserGroup(), targetUserId, targetGroupUser.getPromoterLevel());

                    // ???????????????????????????????????????
                    groupCreditDao.deleteGroupCreditConfigForPromoter(groupId, targetUserId, targetGroupUser.getPromoterLevel());
                    // ??????????????????????????????
                    delCount = groupCreditDao.deleteUserForPromoter(groupId, targetGroupUser.getUserGroup(), targetUserId, targetGroupUser.getPromoterLevel());
                    // ?????????????????????
                    groupCreditDao.deleteGroupCreditConfig(groupId, targetUserId);
                    // ?????????????????????
                    groupCreditDao.deleteCommissionConfig(groupId, targetUserId);
                    // ????????????
                    groupDao.deleteGroupUserByKeyId(targetGroupUser.getKeyId());

                    // ????????????
                    if(list != null && list.size() > 0){
                        for(GroupUser gu : list){
                            insertGroupUserAlert(groupId, gu.getUserId(), userId, GroupConstants.TYPE_USER_ALERT_DELETE);
                        }
                    }
                } else if (GroupConstants.isMember(targetGroupUser.getUserRole())) {
                    delCount = groupDao.deleteGroupUserByKeyId(targetGroupUser.getKeyId());
                    // ????????????
                    insertGroupUserAlert(groupId, targetUserId, userId, GroupConstants.TYPE_USER_ALERT_DELETE);
                } else {
                    OutputUtil.output(1, "???????????????????????????", getRequest(), getResponse(), false);
                    return;
                }
                //??????????????????
                groupDao.updateGroupInfoCount(-delCount, groupId);
            }
            OutputUtil.output(0, LangMsg.getMsg(LangMsg.code_0), getRequest(), getResponse(), false);
            LOGGER.info("deletePromoter|" + groupId + "|" + userId + "|" + targetUserId);
        } catch (Exception e) {
            LOGGER.error("deletePromoter|error|" + JSON.toJSONString(params), e.getMessage(), e);
            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        }
    }

    /**
     * ????????????
     */
    public void inviteUser() {
        Map<String, String> params = null;
        try {
            if(true){
                interfaceDeprecated();
                return;
            }
            params = UrlParamUtil.getParameters(getRequest());
            LOGGER.debug("inviteUser|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), 0);
            if (!checkSessCode(userId, params.get("sessCode"))) {
                return;
            }

            long groupId = NumberUtils.toLong(params.get("groupId"), -1);
            long targetUserId = NumberUtils.toLong(params.get("targetUserId"), -1);

            GroupUser groupUser = groupDao.loadGroupUser(userId, groupId);
            if (groupUser == null) {
                OutputUtil.output(1, "?????????????????????[" + userId + "]????????????[" + groupId + "]?????????", getRequest(), getResponse(), false);
                return;
            }
            synchronized (getUserGroupLock(groupId, groupUser.getUserGroup())) {
                GroupInfo groupInfo = groupDao.loadGroupInfo(groupId, 0);
                if (groupInfo == null) {
                    OutputUtil.output(4, LangMsg.getMsg(LangMsg.code_5), getRequest(), getResponse(), false);
                    return;
                }
                if (groupInfo.getCurrentCount().intValue() >= groupInfo.getMaxCount().intValue()) {
                    OutputUtil.output(6, "??????????????????", getRequest(), getResponse(), false);
                    return;
                }

                groupUser = groupDao.loadGroupUser(userId, groupId);
                if (groupUser == null) {
                    OutputUtil.output(1, "?????????????????????[" + userId + "]????????????[" + groupId + "]?????????", getRequest(), getResponse(), false);
                    return;
                }
                if (!GroupConstants.isPromotor(groupUser.getUserRole()) && !GroupConstants.isTeamLeader(groupUser.getUserRole())) {
                    OutputUtil.output(5, "??????????????????????????????????????????????????????", getRequest(), getResponse(), false);
                    return;
                }

                RegInfo targetUser = userDao.getUser(targetUserId);
                if (targetUser == null) {
                    OutputUtil.output(-1, "???????????????", getRequest(), getResponse(), false);
                    return;
                }
                GroupUser targetGroupUser = groupDao.loadGroupUser(targetUserId, groupId);
                if (targetGroupUser != null) {
                    OutputUtil.output(5, "??????????????????????????????????????????", getRequest(), getResponse(), false);
                    return;
                }
                GroupReview groupReview = groupCreditDao.loadTeamInvite(groupId, targetUserId, userId);
                if (groupReview != null) {
                    OutputUtil.output(5, "???????????????????????????", getRequest(), getResponse(), false);
                    return;
                }

                groupReview = new GroupReview();
                groupReview.setCreatedTime(new Date());
                groupReview.setCurrentState(0);
                groupReview.setGroupId(groupId);
                groupReview.setGroupName(groupUser.getGroupName());
                groupReview.setReviewMode(3);
                groupReview.setUserId(targetUserId);
                groupReview.setUserName(groupUser.getUserName());
                groupReview.setCurrentOperator(userId);
                if (groupDao.createGroupReview(groupReview) <= 0) {
                    OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1002, "??????????????????????????????"), getRequest(), getResponse(), null, false);
                    return;
                }
            }
            OutputUtil.output(0, LangMsg.getMsg(LangMsg.code_0), getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("inviteUser|error|" + JSON.toJSONString(params), e.getMessage(), e);
            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        }
    }

    /**
     * ????????????
     */
    public void responseInvite() {
        Map<String, String> params = null;
        try {
            if(true){
                interfaceDeprecated();
                return;
            }
            params = UrlParamUtil.getParameters(getRequest());
            LOGGER.debug("responseInvite|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), 0);
            if (!checkSessCode(userId, params.get("sessCode"))) {
                return;
            }

            long keyId = NumberUtils.toLong(params.get("keyId"), -1);
            int respType = NumberUtils.toInt(params.get("respType"), -1);

            RegInfo userInf = userDao.getUser(userId);
            if (userInf == null) {
                OutputUtil.output(-1, "???????????????", getRequest(), getResponse(), false);
                return;
            }
            GroupReview groupReview = groupDao.loadGroupReviewByKeyId(keyId);
            if (groupReview == null) {
                OutputUtil.output(3, "??????ID??????", getRequest(), getResponse(), false);
                return;
            } else if (groupReview.getCurrentState().intValue() > 0) {
                OutputUtil.output(4, "???????????????", getRequest(), getResponse(), false);
                return;
            }
            long groupId = groupReview.getGroupId();
            GroupUser inviter = groupDao.loadGroupUser(groupReview.getCurrentOperator(), groupId);
            if (inviter == null) {
                OutputUtil.output(4, "???????????????", getRequest(), getResponse(), false);
                HashMap<String, Object> map = new HashMap<>();
                map.put("keyId", String.valueOf(keyId));
                map.put("currentState", "2");
                map.put("operateTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                map.put("currentOperator", userId);
                groupDao.updateGroupReviewByKeyId(map);
                OutputUtil.output(0, "????????????????????????", getRequest(), getResponse(), false);
                return;
            }
            synchronized (getUserGroupLock(groupId, inviter.getUserGroup())) {

                groupId = groupReview.getGroupId();
                if (respType == 1) { // ??????
                    GroupInfo groupInfo = groupDao.loadGroupInfo(groupId, 0);
                    if (groupInfo == null) {
                        OutputUtil.output(4, LangMsg.getMsg(LangMsg.code_5), getRequest(), getResponse(), false);
                        return;
                    }
                    if (groupInfo.getCurrentCount().intValue() >= groupInfo.getMaxCount().intValue()) {
                        OutputUtil.output(6, "??????????????????", getRequest(), getResponse(), false);
                        return;
                    }

                    int groupCount = groupDao.loadGroupCount(groupId);
                    if (groupCount >= 20) {
                        OutputUtil.output(3, "???????????????????????????20???????????????", getRequest(), getResponse(), false);
                        return;
                    }
                    GroupUser groupUser = groupDao.loadGroupUser(userId, groupId);
                    if (groupUser != null) {
                        OutputUtil.output(3, "??????????????????", getRequest(), getResponse(), false);
                        return;
                    }
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("keyId", String.valueOf(keyId));
                    map.put("currentState", "1");
                    map.put("operateTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                    map.put("currentOperator", userId);
                    groupDao.updateGroupReviewByKeyId(map);
                    inviter = groupDao.loadGroupUser(groupReview.getCurrentOperator(), groupId);
                    if (inviter != null) {
                        groupUser = new GroupUser();
                        groupUser.setCreatedTime(new Date());
                        groupUser.setGroupId((int) groupId);
                        groupUser.setGroupName(groupReview.getGroupName());
                        groupUser.setInviterId(groupReview.getCurrentOperator());
                        groupUser.setPlayCount1(0);
                        groupUser.setPlayCount2(0);
                        groupUser.setUserId(groupReview.getUserId());
                        groupUser.setUserLevel(1);
                        groupUser.setUserRole(GroupConstants.GROUP_ROLE_MEMBER);
                        groupUser.setUserName(userInf.getName());
                        groupUser.setUserNickname(userInf.getName());
                        groupUser.setUserGroup(inviter.getUserGroup());
                        groupUser.setCredit(0L);
                        groupUser.setInviterId(inviter.getUserId());

                        groupUser.setUserGroup(inviter.getUserGroup());
                        groupUser.setPromoterLevel(inviter.getPromoterLevel() + 1);
                        groupUser.setPromoterId1(inviter.getPromoterId1());
                        groupUser.setPromoterId2(inviter.getPromoterId2());
                        groupUser.setPromoterId3(inviter.getPromoterId3());
                        groupUser.setPromoterId4(inviter.getPromoterId4());
                        if (inviter.getPromoterLevel() == 1) {
                            groupUser.setPromoterId1(inviter.getUserId());
                        } else if (inviter.getPromoterLevel() == 2) {
                            groupUser.setPromoterId2(inviter.getUserId());
                        } else if (inviter.getPromoterLevel() == 3) {
                            groupUser.setPromoterId3(inviter.getUserId());
                        } else if (inviter.getPromoterLevel() == 4) {
                            groupUser.setPromoterId4(inviter.getUserId());
                        }
                        long groupUserId = groupDao.createGroupUser(groupUser);
                        if (groupUserId <= 0) {
                            OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1002, "??????????????????????????????"), getRequest(), getResponse(), null, false);
                            return;
                        }

                        // ??????????????????????????????
                        groupCreditDao.rejectTeamInvite(groupId, userId);

                        // ?????????????????????
                        groupDao.updateGroupInfoCount(1, groupId);

                        insertGroupUserAlert(groupUser.getGroupId(), groupUser.getUserId(), inviter.getUserId(), GroupConstants.TYPE_USER_ALERT_INVITE);

                        OutputUtil.output(0, "??????????????????", getRequest(), getResponse(), false);
                    } else {
                        map = new HashMap<>();
                        map.put("keyId", String.valueOf(keyId));
                        map.put("currentState", "2");
                        map.put("operateTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                        map.put("currentOperator", userId);
                        groupDao.updateGroupReviewByKeyId(map);
                        OutputUtil.output(0, "????????????", getRequest(), getResponse(), false);
                    }
                } else { // ??????
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("keyId", String.valueOf(keyId));
                    map.put("currentState", "2");
                    map.put("operateTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                    map.put("currentOperator", userId);
                    groupDao.updateGroupReviewByKeyId(map);
                    OutputUtil.output(0, "????????????????????????", getRequest(), getResponse(), false);
                }
            }
        } catch (Exception e) {
            LOGGER.error("responseInvite|error|" + JSON.toJSONString(params), e.getMessage(), e);
            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        }
    }

    /**
     * ????????????
     */
    public void deleteTeam() {
        Map<String, String> params = null;
        try {
            if(true){
                interfaceDeprecated();
                return;
            }
            params = UrlParamUtil.getParameters(getRequest());
            LOGGER.debug("deleteTeam|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), 0);
            if (!checkSessCode(userId, params.get("sessCode"))) {
                return;
            }
            long groupId = NumberUtils.toLong(params.get("groupId"), -1);
            String userGroup = params.get("userGroup");
            if (StringUtils.isBlank(userGroup) || "0".equals(userGroup)) {
                OutputUtil.output(1, "?????????????????????????????????", getRequest(), getResponse(), false);
                return;
            }
            GroupUser groupUser = groupDao.loadGroupUser(userId, groupId);
            if (groupUser == null || !GroupConstants.isMasterOrAdmin(groupUser.getUserRole())) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_7), getRequest(), getResponse(), false);
                return;
            }
            int delCount = 0;
            synchronized (getUserGroupLock(groupId, userGroup)) {
                GroupUser teamLeader = groupCreditDao.loadGroupTeamMaster(groupId, userGroup);
                if (teamLeader == null) {
                    OutputUtil.output(5, LangMsg.getMsg(LangMsg.code_7), getRequest(), getResponse(), false);
                    return;
                }
                int count = groupCreditDao.countUserHaveCreditForTeamLeader(groupUser.getGroupId(), userGroup);
                if (count > 0) {
                    OutputUtil.output(7, "???????????????????????????????????????????????????", getRequest(), getResponse(), false);
                    return;
                }
                List<GroupUser> list = groupCreditDao.allUserForTeamLeader(groupId, userGroup);

                // ????????????????????????????????????
                groupCreditDao.deleteGroupCreditConfigForTeamLeader(groupId, userGroup);
                // ????????????
                groupCreditDao.deleteCommissionConfig(groupId, teamLeader.getUserId());
                // ???????????????????????????
                delCount = groupCreditDao.deleteUserForTeamLeader(groupId, userGroup);
                // ??????t_group_relation
                groupDao.deleteTeam(userGroup);
                //??????????????????
                groupDao.updateGroupInfoCount(-delCount, groupUser.getGroupId());

                // ????????????
                if(list != null && list.size() > 0){
                    for(GroupUser gu : list){
                        insertGroupUserAlert(groupId, gu.getUserId(), userId, GroupConstants.TYPE_USER_ALERT_DELETE);
                    }
                }
            }
            OutputUtil.output(0, LangMsg.getMsg(LangMsg.code_0), getRequest(), getResponse(), false);
            LOGGER.info("deleteTeam|" + groupId + "|" + userId + "|" + userGroup + "|" + delCount);
        } catch (Exception e) {
            LOGGER.error("deleteTeam|error|" + JSON.toJSONString(params), e.getMessage(), e);
            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        }
    }

    /**
     * ????????????
     */
    public void searchUser() {
        Map<String, String> params = null;
        try {
            params = UrlParamUtil.getParameters(getRequest());
            LOGGER.debug("searchUser|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), 0);
            if (!checkSessCode(userId, params.get("sessCode"))) {
                return;
            }

            long groupId = NumberUtils.toLong(params.get("groupId"), -1);
            int mode = NumberUtils.toInt(params.get("mode"), 1);

            GroupUser groupUser = groupDao.loadGroupUser(userId, groupId);
            if (groupUser == null) {
                OutputUtil.output(1, "?????????????????????[" + userId + "]????????????[" + groupId + "]?????????", getRequest(), getResponse(), false);
                return;
            }
            synchronized (getUserGroupLock(groupId, groupUser.getUserGroup())) {
                groupUser = groupDao.loadGroupUser(userId, groupId);
                if (groupUser == null) {
                    OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_7), getRequest(), getResponse(), false);
                    return;
                }
                JSONObject json = new JSONObject();
                json.put("mode", mode);
                if (mode == 1) { // ????????????
                    long targetUserId = NumberUtils.toLong(params.get("targetUserId"), -1);
                    RegInfo targetUser = userDao.getUser(targetUserId);
                    if (targetUser == null) {
                        OutputUtil.output(-1, "???????????????", getRequest(), getResponse(), false);
                        return;
                    }
                    json.put("userId", targetUserId);
                    json.put("headimgurl", targetUser.getHeadimgurl());
                    json.put("userName", targetUser.getName());
                    json.put("canInvite", 0); // ????????????
                    json.put("canUp", 0);// ????????????
                    json.put("canDelete", 0); // ????????????
                    json.put("canDeleteMember", 0); // ????????????
                    GroupUser targetGroupUser = groupDao.loadGroupUser(targetUserId, groupId);
                    if (targetGroupUser != null) {
                        if (GroupConstants.isNextLevel(groupUser, targetGroupUser)) {
                            if (GroupConstants.isMember(targetGroupUser.getUserRole())) {
                                json.put("canUp", 1);
                                json.put("canDeleteMember", 1); //????????????????????????
                            } else if (GroupConstants.isPromotor(targetGroupUser.getUserRole())) {
                                json.put("canDelete", 1);   //????????????????????????
                            }
                        }
                    } else {
                        json.put("canInvite", 1);
                    }
                } else { // ????????????
                    String keyWord = params.get("keyWord");
                    int pageNo = NumberUtils.toInt(params.get("pageNo"), 1);
                    int pageSize = NumberUtils.toInt(params.get("pageSize"), 30);
                    if (pageNo <= 0) {
                        pageNo = 1;
                    }
                    if (pageSize <= 0 || pageSize > 30) {
                        pageSize = 30;
                    }
                    Integer dataCount = 0;
                    List<HashMap<String, Object>> dataList = Collections.emptyList();
                    List<HashMap<String, Object>> groupUsers = Collections.emptyList();
                    if (GroupConstants.isMasterOrAdmin(groupUser.getUserRole())) {
                        dataCount = groupCreditDao.countSearchGroupUserForMaster(groupId, keyWord);
                        if (dataCount > 0) {
                            groupUsers = groupCreditDao.searchGroupUserForMaster(groupId, keyWord, pageNo, pageSize);
                        }
                    } else if (GroupConstants.isTeamLeader(groupUser.getUserRole())) {
                        dataCount = groupCreditDao.countSearchGroupUserForTeamLeader(groupId, keyWord, groupUser.getUserGroup());
                        if (dataCount > 0) {
                            groupUsers = groupCreditDao.searchGroupUserForTeamLeader(groupId, keyWord, groupUser.getUserGroup(), pageNo, pageSize);
                        }
                    } else if (GroupConstants.isPromotor(groupUser.getUserRole())) {
                        dataCount = groupCreditDao.countSearchGroupUserForPromoter(groupId, keyWord, groupUser);
                        if (dataCount > 0) {
                            groupUsers = groupCreditDao.searchGroupUserForPromoter(groupId, keyWord, groupUser, pageNo, pageSize);
                        }
                    } else {
                        // ?????????????????????
                        dataCount = 1;
                        if (dataCount > 0) {
                            groupUsers = groupCreditDao.searchGroupUserForUser(groupId, groupUser.getUserId());
                        }
                    }

                    if (dataCount > 0 && groupUsers != null && groupUsers.size() > 0) {
                        dataList = new ArrayList<>();
                        for (HashMap<String, Object> gu : groupUsers) {
                            gu.put("opType", getOpType(groupUser, gu));
                            dataList.add(gu);
                        }
                        if (groupUsers.size() == 1) {
                            HashMap<String, Object> gu = groupUsers.get(0);
                            long targetUserId = (Long) gu.get("userId");
                            GroupUser targetGroupUser = groupDao.loadGroupUser(targetUserId, groupId);
                            boolean searchSuperior = false;
                            if (GroupConstants.isMasterOrAdmin(groupUser.getUserRole())) {
                                searchSuperior = true;
                            } else if (userId == targetUserId) {
                                searchSuperior = true;
                            }
//                            searchSuperior = false;
                            if (searchSuperior) {
                                // ????????????????????????????????????????????????
                                HashMap<String, Object> superiorMsg = new HashMap();
                                GroupUser superior;
                                if ("0".equals(targetGroupUser.getUserGroup()) || GroupConstants.isTeamLeader(targetGroupUser.getUserRole())) {
                                    // ??????????????????????????????????????????
                                    superior = groupDao.loadGroupMaster(String.valueOf(groupId));
                                } else if (targetGroupUser.getPromoterLevel() == 1) {
                                    // ???????????????
                                    superior = groupDao.loadGroupTeamMaster(String.valueOf(groupId), targetGroupUser.getUserGroup());
                                } else {
                                    // ???????????????
                                    long promoterId = 0;
                                    if (targetGroupUser.getPromoterLevel() == 2) {
                                        promoterId = targetGroupUser.getPromoterId1();
                                    } else if (targetGroupUser.getPromoterLevel() == 3) {
                                        promoterId = targetGroupUser.getPromoterId2();
                                    } else if (targetGroupUser.getPromoterLevel() == 4) {
                                        promoterId = targetGroupUser.getPromoterId3();
                                    } else if (targetGroupUser.getPromoterLevel() == 5) {
                                        promoterId = targetGroupUser.getPromoterId4();
                                    }
                                    superior = groupDao.loadGroupUser(promoterId, groupId);
                                }
                                if (superior != null) {
                                    superiorMsg.put("userId", superior.getUserId());
                                    superiorMsg.put("userName", superior.getUserName());
                                    superiorMsg.put("credit", superior.getCredit());
                                    superiorMsg.put("opType", getOpType(groupUser, superior));
                                    RegInfo userInf = userDao.getUser(superior.getUserId());
                                    if (userInf != null) {
                                        superiorMsg.put("headimgurl", userInf.getHeadimgurl());
                                    }
                                } else {
                                    superiorMsg.put("userId", 0);
                                    superiorMsg.put("userName", "");
                                    superiorMsg.put("opType", 0);
                                }
                                dataList.add(0, superiorMsg);
                            }
                        }
                    }
                    json.put("pageNo", pageNo);
                    json.put("pageSize", pageSize);
                    json.put("total", dataCount);
                    json.put("pages", (int) Math.ceil(dataCount * 1.0 / pageSize));
                    json.put("dataList", dataList);
                }
                OutputUtil.output(0, json, getRequest(), getResponse(), false);
            }
        } catch (Exception e) {
            LOGGER.error("searchUser|error|" + JSON.toJSONString(params), e.getMessage(), e);
            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        }
    }

    /**
     * ?????????groupUser???targetGroupUser????????????????????????
     *
     * @param groupUser
     * @param targetGroupUser
     * @return opType 1??????????????? 2?????????  0????????????
     */
    private int getOpType(GroupUser groupUser, GroupUser targetGroupUser) {
        int res = 0;
        if (groupUser == null || targetGroupUser == null) {
            return res;
        }
        if (GroupConstants.isMaster(groupUser.getUserRole())) { // ??????
            //?????????????????????????????????
            res = 1;
        } else if (GroupConstants.isAdmin(groupUser.getUserRole())) { // ?????????
            res = 2;
            if (groupUser.getUserId().longValue() == targetGroupUser.getUserId().longValue()) {
                res = 0;
            } else if (!GroupConstants.isMasterOrAdmin(targetGroupUser.getUserRole())) {
                // ?????????????????????????????????????????????????????????
                res = 1;
            }

        } else if (GroupConstants.isTeamLeader(groupUser.getUserRole())) { // ?????????
            if (groupUser.getUserId().longValue() == targetGroupUser.getUserId().longValue()) {
                res = 0;
            } else if (GroupConstants.isMasterOrAdmin(targetGroupUser.getUserRole())) {
                // ???????????????????????????
                res = 2;
            } else if (GroupConstants.isTeamLeader(targetGroupUser.getUserRole())) {
                // ??????????????????
                res = 2;
            } else if (groupUser.getUserGroup().equals(targetGroupUser.getUserGroup())) {
                // ?????????????????????????????????
                res = 1;
            }

        } else if (GroupConstants.isPromotor(groupUser.getUserRole())) { // ??????
            if (groupUser.getUserId().longValue() == targetGroupUser.getUserId().longValue()) {
                res = 0;
            } else if (GroupConstants.isMasterOrAdmin(targetGroupUser.getUserRole())) {
                // ???????????????????????????
                res = 2;
            } else if (GroupConstants.isNextLevel(targetGroupUser, groupUser) || groupUser.getPromoterLevel() == targetGroupUser.getPromoterLevel()) {
                // ?????????????????????????????????????????????
                res = 2;
            } else if (groupUser.getPromoterLevel() == 1 && targetGroupUser.getPromoterId1() == groupUser.getUserId()) {
                // ??????????????????????????????
                res = 1;
            } else if (groupUser.getPromoterLevel() == 2 && targetGroupUser.getPromoterId2() == groupUser.getUserId()) {
                res = 1;
            } else if (groupUser.getPromoterLevel() == 3 && targetGroupUser.getPromoterId3() == groupUser.getUserId()) {
                res = 1;
            } else if (groupUser.getPromoterLevel() == 4 && targetGroupUser.getPromoterId4() == groupUser.getUserId()) {
                res = 1;
            }

        } else if (GroupConstants.isMember(groupUser.getUserRole())) { // ????????????
            if (groupUser.getUserId().longValue() == targetGroupUser.getUserId().longValue()) {
                res = 0;
            } else if (GroupConstants.isMasterOrAdmin(targetGroupUser.getUserRole())) {
                // ???????????????????????????
                res = 2;
            } else if (GroupConstants.isNextLevel(targetGroupUser, groupUser)) {
                // ????????????????????????
                res = 2;
            }

        }
        return res;
    }

    /**
     * ?????????groupUser???targetGroupUser????????????????????????
     *
     * @param groupUser
     * @param targetGroupUser ????????????userRole,userGroup,promoterLevel,promoterId1,promoterId2,promoterId3,promoterId4
     * @return opType 1??????????????? 2?????????  0????????????
     */
    private int getOpType(GroupUser groupUser, Map<String, Object> targetGroupUser) {
        GroupUser target = new GroupUser();
        try {
            BeanUtils.populate(target, targetGroupUser);
        } catch (Exception e) {
            return 0;
        }
        return getOpType(groupUser, target);
    }



    /**
     * ?????????
     */
    public void loadCreditConfig() {
        Map<String, String> params = null;
        try {
            if(true){
                interfaceDeprecated();
                return;
            }
            params = UrlParamUtil.getParameters(getRequest());
            LOGGER.debug("loadCreditConfig|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), 0);
            long targetUserId = NumberUtils.toLong(params.get("targetUserId"), 0);
            if (!checkSessCode(userId, params.get("sessCode"))) {
                return;
            }

            long groupId = NumberUtils.toInt(params.get("groupId"), -1);
            GroupInfo groupInfo = groupDao.loadGroupInfo(groupId, 0);
            if (groupInfo == null) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            GroupUser groupUser = groupDao.loadGroupUser(userId, groupId);
            if (groupUser == null || GroupConstants.isMember(groupUser.getUserRole())) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            GroupUser targetGroupUser = groupDao.loadGroupUser(targetUserId, groupId);
            if (targetGroupUser == null || GroupConstants.isMember(targetGroupUser.getUserRole())) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            JSONObject json = new JSONObject();
            json.put("creditAllotMode", groupInfo.getCreditAllotMode());
            json.put("dataList", loadGroupCreditConfig(groupUser, targetUserId, groupInfo.getCreditAllotMode()));
            OutputUtil.output(0, json, getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("loadCreditConfig|error|" + JSON.toJSONString(params), e.getMessage(), e);
            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        }
    }

    private List<Map<String, Object>> loadGroupCreditConfig(GroupUser groupUser, long targetUserId, int creditAllotMode) throws Exception {
        long userId = groupUser.getUserId();
        long groupId = groupUser.getGroupId();
        List<Map<String, Object>> tableConfigList = groupCreditDao.loadGroupTableConfigWithRoomName(groupId);
        List<GroupCreditConfig> creditConfigList = null;
        List<GroupCreditConfig> preCreditConfigList = null;
        if (GroupConstants.isMasterOrAdmin(groupUser.getUserRole())) {
            // ??????????????????
            long masterId = userId;
            if (GroupConstants.isAdmin(groupUser.getUserRole())) {
                GroupUser master = groupDao.loadGroupMaster(String.valueOf(groupId));
                masterId = master.getUserId();
            }
            creditConfigList = groupCreditDao.loadCreditConfig(groupId, masterId, targetUserId);
        } else if (GroupConstants.isTeamLeader(groupUser.getUserRole())) {
            // ?????????
            creditConfigList = groupCreditDao.loadCreditConfig(groupId, userId, targetUserId);

            // ?????????????????????
            GroupUser master = groupDao.loadGroupMaster(String.valueOf(groupId));
            long masterId = master.getUserId();
            preCreditConfigList = groupCreditDao.loadCreditConfig(groupId, masterId, userId);
        } else if (GroupConstants.isPromotor(groupUser.getUserRole())) {
            //??????
            creditConfigList = groupCreditDao.loadCreditConfig(groupId, userId, targetUserId);
            long preUserId = 0l; // ??????????????????
            if (groupUser.getPromoterLevel() == 1) {
                // ????????????????????????
                GroupUser teamLeader = groupDao.loadGroupTeamMaster(String.valueOf(groupId), groupUser.getUserGroup());
                if (teamLeader != null) {
                    preUserId = teamLeader.getUserId();
                }
            } else if (groupUser.getPromoterLevel() == 2) {
                preUserId = groupUser.getPromoterId1();
            } else if (groupUser.getPromoterLevel() == 3) {
                preUserId = groupUser.getPromoterId2();
            } else if (groupUser.getPromoterLevel() == 4) {
                preUserId = groupUser.getPromoterId3();
            } else if (groupUser.getPromoterLevel() == 5) {
                preUserId = groupUser.getPromoterId4();
            }
            preCreditConfigList = groupCreditDao.loadCreditConfig(groupId, preUserId, userId);
        }
        Map<Long, GroupCreditConfig> creditConfigMap = new HashMap<>();
        if (creditConfigList != null && !creditConfigList.isEmpty()) {
            for (GroupCreditConfig c : creditConfigList) {
                creditConfigMap.put(c.getConfigId(), c);
            }
        }
        Map<Long, GroupCreditConfig> preCreditConfigMap = new HashMap<>();
        if (preCreditConfigList != null && !preCreditConfigList.isEmpty()) {
            for (GroupCreditConfig c : preCreditConfigList) {
                preCreditConfigMap.put(c.getConfigId(), c);
            }
        }

        List<Map<String, Object>> dataList = new ArrayList<>();
        if (tableConfigList != null && tableConfigList.size() > 0) {
            for (Map<String, Object> config : tableConfigList) {
                String modeMsg = (String) config.get("modeMsg");
                if (!TableConfigConstants.isCredit(modeMsg)) {
                    continue;
                }
                Map<String, Object> data = new HashMap<>();
                Long keyId = (Long) config.get("keyId");
                data.put("configId", config.get("keyId"));
                data.put("modeMsg", config.get("modeMsg"));
                data.put("name", config.get("groupName"));
                data.put("seq", config.get("groupId"));
                int initValue = TableConfigConstants.getCreditCommission(modeMsg);
                data.put("initValue", initValue);
                if (GroupConstants.isMasterOrAdmin(groupUser.getUserRole())) {
                    // ?????????????????????
                    data.put("myValue", initValue);
                    data.put("maxValue", TableConfigConstants.getCreditCommissionLimit(modeMsg, creditAllotMode));
                } else {
                    if (preCreditConfigMap.containsKey(keyId)) {
                        GroupCreditConfig c = preCreditConfigMap.get(keyId);
                        data.put("myValue", c.getCredit());
                        data.put("maxValue", c.getCredit());
                    } else {
                        data.put("maxValue", 0);
                        data.put("myValue", 0);
                    }
                }
                if (creditConfigMap.containsKey(keyId)) {
                    GroupCreditConfig c = creditConfigMap.get(keyId);
                    data.put("nextValue", c.getCredit());
                    data.put("maxValueLog", c.getMaxCreditLog());
                } else {
                    data.put("nextValue", 0);
                    data.put("maxValueLog", 0);
                }
                dataList.add(data);
            }
        }
        return dataList;
    }


    /**
     * ???????????????
     */
    public void updateCreditConfig() {
        Map<String, String> params = null;
        try {
            if(true){
                interfaceDeprecated();
                return;
            }
            params = UrlParamUtil.getParameters(getRequest());
            LOGGER.debug("updateCreditConfig|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), 0);
            long targetUserId = NumberUtils.toLong(params.get("targetUserId"), 0);
            if (!checkSessCode(userId, params.get("sessCode"))) {
                return;
            }

            long groupId = NumberUtils.toInt(params.get("groupId"), -1);
            int mode = NumberUtils.toInt(params.get("mode"), -1);


            GroupInfo groupInfo = groupDao.loadGroupInfo(groupId, 0);
            if (groupInfo == null) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            GroupUser groupUser = groupDao.loadGroupUser(userId, groupId);
            if (groupUser == null || GroupConstants.isMember(groupUser.getUserRole())) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_7), getRequest(), getResponse(), false);
                return;
            }
            int credit = NumberUtils.toInt(params.get("credit"), -1);
            List<GroupTableConfig> tableConfigList;
            Map<Long, Integer> creditMap = new HashMap<>();
            if (mode == 1) {
                if (groupId <= 0 || credit < 0) {
                    OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                    return;
                }
                // ??????
                tableConfigList = groupCreditDao.loadAllGroupTableConfig(groupId);
            } else {
                // ????????????
                String configs = params.get("configs");
                if (StringUtils.isBlank(configs)) {
                    OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                    return;
                }
                String[] split1 = configs.split(";");
                String configIds = "";
                for (String config : split1) {
                    String[] split2 = config.split(",");
                    if (split2.length < 2) {
                        OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                        return;
                    }
                    long configId = Long.valueOf(split2[0]);
                    int creditValue = Integer.valueOf(split2[1]);
                    if (creditValue < 0) {
                        OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                        return;
                    }
                    configIds += configId + ",";
                    creditMap.put(configId, creditValue);
                }
                if (configIds.length() > 0) {
                    configIds = configIds.substring(0, configIds.length() - 1);
                }
                if (configIds.length() > 0) {
                    tableConfigList = groupCreditDao.loadAllGroupTableConfigByIds(groupId, configIds);
                } else {
                    tableConfigList = null;
                }
            }
            if (tableConfigList == null || tableConfigList.size() == 0) {
                OutputUtil.output(1, "??????????????????????????????", getRequest(), getResponse(), false);
                return;
            }
            for (GroupTableConfig tableConfig : tableConfigList) {
                long configId = tableConfig.getKeyId();
                if (mode != 1) {
                    credit = creditMap.get(configId);
                }
                int maxValue = 0;
                long masterId = 0;
                if (GroupConstants.isMasterOrAdmin(groupUser.getUserRole())) {
                    // ??????????????????
                    maxValue = TableConfigConstants.getCreditCommissionLimit(tableConfig.getModeMsg(), groupInfo.getCreditAllotMode());
                    if (GroupConstants.isAdmin(groupUser.getUserRole())) {
                        GroupUser master = groupDao.loadGroupMaster(String.valueOf(groupId));
                        masterId = master.getUserId();
                    }
                } else if (GroupConstants.isTeamLeader(groupUser.getUserRole())) {
                    // ?????????
                    // ?????????????????????
                    GroupUser master = groupDao.loadGroupMaster(String.valueOf(groupId));
                    masterId = master.getUserId();
                    GroupCreditConfig preConfig = groupCreditDao.loadCreditConfigByConfigId(groupId, masterId, userId, configId);
                    if (preConfig != null) {
                        maxValue = preConfig.getCredit();
                    }
                } else if (GroupConstants.isPromotor(groupUser.getUserRole())) {
                    //??????
                    long preUserId = 0l;
                    if (groupUser.getPromoterLevel() == 1) {
                        // ????????????????????????
                        GroupUser teamLeader = groupDao.loadGroupTeamMaster(String.valueOf(groupId), groupUser.getUserGroup());
                        if (teamLeader != null) {
                            preUserId = teamLeader.getUserId();
                        }
                    } else if (groupUser.getPromoterLevel() == 2) {
                        preUserId = groupUser.getPromoterId1();
                    } else if (groupUser.getPromoterLevel() == 3) {
                        preUserId = groupUser.getPromoterId2();
                    } else if (groupUser.getPromoterLevel() == 4) {
                        preUserId = groupUser.getPromoterId3();
                    } else if (groupUser.getPromoterLevel() == 5) {
                        preUserId = groupUser.getPromoterId4();
                    }
                    GroupCreditConfig preConfig = groupCreditDao.loadCreditConfigByConfigId(groupId, preUserId, userId, configId);
                    if (preConfig != null) {
                        maxValue = preConfig.getCredit();
                    }
                }
                if (credit > maxValue) {
                    credit = maxValue;
                }
                long preUserId = userId;
                if (GroupConstants.isAdmin(groupUser.getUserRole())) {
                    preUserId = masterId;
                }
                int oldValue = 0;
                GroupCreditConfig creditConfig = groupCreditDao.loadCreditConfigByConfigId(groupId, preUserId, targetUserId, configId);
                if (creditConfig != null) {
                    // ??????
                    groupCreditDao.updateGroupCreditConfig(creditConfig.getKeyId(), credit, maxValue);
                    oldValue = creditConfig.getCredit();
                } else {
                    // ??????
                    creditConfig = new GroupCreditConfig();
                    creditConfig.setConfigId(configId);
                    creditConfig.setGroupId(groupId);
                    creditConfig.setPreUserId(preUserId);
                    creditConfig.setUserId(targetUserId);
                    creditConfig.setCredit(credit);
                    creditConfig.setMaxCreditLog(maxValue);
                    creditConfig.setCreatedTime(new Date());
                    creditConfig.setLastUpTime(new Date());
                    groupCreditDao.insertGroupCreditConfig(creditConfig);
                }
                LOGGER.info("updateCreditConfig|" + groupId + "|" + userId + "|" + configId + "|" + credit + "|" + maxValue + "|" + oldValue);
            }
            JSONObject json = new JSONObject();
            json.put("creditAllotMode", groupInfo.getCreditAllotMode());
            json.put("dataList", loadGroupCreditConfig(groupUser, targetUserId, groupInfo.getCreditAllotMode()));
            OutputUtil.output(0, json, getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("updateCreditConfig|error|" + JSON.toJSONString(params), e.getMessage(), e);
            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        }
    }

    /**
     * ??????????????????
     */
    public void updateAllotMode() {
        Map<String, String> params = null;
        try {
            params = UrlParamUtil.getParameters(getRequest());
            LOGGER.debug("updateAllotMode|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), 0);
            if (!checkSessCode(userId, params.get("sessCode"))) {
                return;
            }

            long groupId = NumberUtils.toInt(params.get("groupId"), -1);
            int mode = NumberUtils.toInt(params.get("mode"), 1); // ???????????????1?????????????????????2???????????????
            if (mode != 1 && mode != 2) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_23), getRequest(), getResponse(), false);
                return;
            }
            String modeSet = ResourcesConfigsUtil.loadStringValue("ServerConfig", "groupAllotModeSet", "1");
            if (!modeSet.contains(String.valueOf(mode))) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_23), getRequest(), getResponse(), false);
                return;
            }
            GroupInfo groupInfo = groupDao.loadGroupInfo(groupId, 0);
            if (groupInfo == null) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_7), getRequest(), getResponse(), false);
                return;
            }

            GroupUser groupUser = groupDao.loadGroupUser(userId, groupId);
            if (groupUser == null || !GroupConstants.isMaster(groupUser.getUserRole())) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_7), getRequest(), getResponse(), false);
                return;
            }

            if (groupCreditDao.updateCreditAllotMode(groupId, mode) <= 0) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
                return;
            }

            OutputUtil.output(0, LangMsg.getMsg(LangMsg.code_0), getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("updateAllotMode|error|" + JSON.toJSONString(params), e.getMessage(), e);
            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        }
    }

    /**
     * ???????????????????????????
     */
    public void updateCredit() {
        Map<String, String> params = null;
        try {
            if(true){
                interfaceDeprecated();
                return;
            }
            params = UrlParamUtil.getParameters(getRequest());
            LOGGER.debug("updateCredit|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), -1);
            if (!checkSessCode(Long.valueOf(userId), params.get("sessCode"))) {
                return;
            }
            long destUserId = NumberUtils.toLong(params.get("destUserId"), -1);
            long groupId = NumberUtils.toLong(params.get("groupId"), -1);

            if (userId == -1 || groupId == -1 || destUserId == -1) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }

            // ?????????
            GroupUser groupUser = groupDao.loadGroupUser(userId, groupId);
            if (groupUser == null) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            RegInfo userInfo = this.userDao.getUser(userId);
            if (userInfo == null) {
                OutputUtil.output(6, "??????????????????" + userId, getRequest(), getResponse(), false);
                return;
            }
            if (userInfo.getPlayingTableId() > 0 && !groupCreditDao.isGroupTableOver(groupId, userInfo.getPlayingTableId())) {
                OutputUtil.output(6, LangMsg.getMsg(LangMsg.code_19), getRequest(), getResponse(), false);
                return;
            }


            // ??????????????????
            GroupUser destGroupUser = groupDao.loadGroupUser(destUserId, groupId);
            if (destGroupUser == null) {
                OutputUtil.output(4, LangMsg.getMsg(LangMsg.code_14), getRequest(), getResponse(), false);
                return;
            }
            RegInfo destRegInfo = this.userDao.getUser(destUserId);
            if (destRegInfo == null) {
                OutputUtil.output(6, "??????????????????" + destUserId, getRequest(), getResponse(), false);
                return;
            }
            if (destRegInfo.getPlayingTableId() > 0 && !groupCreditDao.isGroupTableOver(groupId, destRegInfo.getPlayingTableId())) {
                OutputUtil.output(6, LangMsg.getMsg(LangMsg.code_13), getRequest(), getResponse(), false);
                return;
            }

            int credit = NumberUtils.toInt(params.get("credit"), 0);

            if (GroupConstants.isMaster(groupUser.getUserRole()) && userId == destUserId) {
                //????????????????????????????????????????????????
                if (credit < 0 && destGroupUser.getCredit() < Math.abs(credit)) {
                    OutputUtil.output(7, "?????????????????????????????????", getRequest(), getResponse(), false);
                    return;
                }
                int updateResult = updateCredit(groupUser, destGroupUser, credit);
                if (updateResult == 1) {
                    if (destRegInfo.getEnterServer() > 0) {
                        GameUtil.sendCreditUpdate(destRegInfo.getEnterServer(), userId, groupId);
                    }
                }
                LOGGER.info("updateCredit|1|" + groupId + "|" + updateResult + "|" + userId + "|" + destUserId + "|" + credit);
            } else {
                //???????????????
                if (userId == destUserId) {
                    OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                    return;
                }
                if(groupUser.getCreditLock() == 1){
                    OutputUtil.output(6, "???????????????????????????????????????", getRequest(), getResponse(), false);
                    return;
                }
                if(credit < 0 && destGroupUser.getCreditLock() == 1){
                    OutputUtil.output(6, LangMsg.getMsg(LangMsg.code_17), getRequest(), getResponse(), false);
                    return;
                }
                int opType = getOpType(groupUser, destGroupUser);
                if (opType == 0) {
                    OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_8), getRequest(), getResponse(), false);
                    return;
                } else if (opType == 2 && credit < 0) {
                    OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_8), getRequest(), getResponse(), false);
                    return;
                }
                if (credit > 0 && opType == 0) {
                    if (credit < 0 && destGroupUser.getCredit() < Math.abs(credit)) {
                        OutputUtil.output(7, LangMsg.getMsg(LangMsg.code_16), getRequest(), getResponse(), false);
                        return;
                    } else if (credit > 0 && groupUser.getCredit() < credit) {
                        OutputUtil.output(7, LangMsg.getMsg(LangMsg.code_15), getRequest(), getResponse(), false);
                        return;
                    }
                }

                //????????????fromId?????????destId
                long fromId = groupUser.getUserId();
                long destId = destGroupUser.getUserId();
                if (credit < 0) {
                    //?????????????????????????????????????????????destGroupUser?????????groupUser
                    fromId = destGroupUser.getUserId();
                    destId = groupUser.getUserId();
                }
                int transferResult = groupDao.transferGroupUserCredit(fromId, destId, groupUser.getGroupId(), Math.abs(credit));
                if (transferResult == 2) {
                    // ????????????
                    HashMap<String, Object> logFrom = new HashMap<>();
                    logFrom.put("groupId", groupUser.getGroupId());
                    logFrom.put("optUserId", fromId);
                    logFrom.put("userId", destId);
                    logFrom.put("tableId", 0);
                    logFrom.put("credit", Math.abs(credit));
                    logFrom.put("type", 1);
                    logFrom.put("flag", 1);
                    logFrom.put("userGroup", destGroupUser.getUserGroup());
                    logFrom.put("mode", fromId == userId ? 1 : 0);
                    groupDao.insertGroupCreditLog(logFrom);

                    // ????????????
                    HashMap<String, Object> logDest = new HashMap<>();
                    logDest.put("groupId", groupUser.getGroupId());
                    logDest.put("optUserId", destId);
                    logDest.put("userId", fromId);
                    logDest.put("tableId", 0);
                    logDest.put("credit", -Math.abs(credit));
                    logDest.put("type", 1);
                    logDest.put("flag", 1);
                    logDest.put("userGroup", destGroupUser.getUserGroup());
                    logDest.put("mode", fromId == destUserId ? 1 : 0);
                    groupDao.insertGroupCreditLog(logDest);

                    if (userInfo.getEnterServer() > 0) {
                        GameUtil.sendCreditUpdate(userInfo.getEnterServer(), userId, groupId);
                    }

                    if (destRegInfo.getEnterServer() > 0 && destRegInfo.getIsOnLine() > 0) {
                        GameUtil.sendCreditUpdate(destRegInfo.getEnterServer(), destUserId, groupId);
                    }
                } else {
                    OutputUtil.output(7, "???????????????", getRequest(), getResponse(), false);
                    return;
                }
                LOGGER.info("updateCredit|2|" + groupId + "|" + transferResult + "|" + groupUser.getUserId() + "|" + destGroupUser.getUserId() + "|" + credit);
            }
            OutputUtil.output(0, LangMsg.getMsg(LangMsg.code_0), getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("updateCredit|error|" + JSON.toJSONString(params), e.getMessage(), e);
            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        }
    }

    private int updateCredit(GroupUser groupUser, GroupUser destGroupUser, int credit) {
        int updateResult = -1;
        try {
            HashMap<String, Object> map = new HashMap<>();
            map.put("credit", credit);
            map.put("keyId", destGroupUser.getKeyId().toString());
            updateResult = groupDao.updateGroupUserCredit(map);
            if (updateResult == 1) {
                JSONObject json = new JSONObject();
                GroupUser tmp = this.groupDao.loadGroupUser(groupUser.getUserId(), groupUser.getGroupId());
                json.put("credit", tmp != null ? tmp.getCredit() : 0);
                OutputUtil.output(0, json, getRequest(), getResponse(), false);
            } else {
                OutputUtil.output(0, "????????????", getRequest(), getResponse(), false);
            }
            if (updateResult == 1) {
                // ????????????
                HashMap<String, Object> creditLog = new HashMap<>();
                creditLog.put("groupId", groupUser.getGroupId());
                creditLog.put("optUserId", groupUser.getUserId());
                creditLog.put("userId", destGroupUser.getUserId());
                creditLog.put("tableId", 0);
                creditLog.put("credit", credit);
                creditLog.put("type", 1);
                creditLog.put("flag", 1);
                creditLog.put("userGroup", destGroupUser.getUserGroup());
                creditLog.put("mode", 1);
                groupDao.insertGroupCreditLog(creditLog);
            }
        } catch (Exception e) {
            LOGGER.error("updateCredit|error|" + updateResult + "|" + groupUser.getGroupId() + "|" + groupUser.getUserId() + "|" + destGroupUser.getUserId() + "|" + credit + "|", e);
        }
        return updateResult;
    }

    /**
     * ????????????
     */
    public void loadCreditCommissionLog() {
        Map<String, String> params = null;
        try {
            if(true){
                interfaceDeprecated();
                return;
            }
            params = UrlParamUtil.getParameters(getRequest());
            LOGGER.debug("loadCreditCommissionLog|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), -1);
            if (!checkSessCode(Long.valueOf(userId), params.get("sessCode"))) {
                return;
            }
            String keyWord = params.get("keyWord");
            int pageNo = NumberUtils.toInt(params.get("pageNo"), 1);
            int pageSize = NumberUtils.toInt(params.get("pageSize"), 30);
            if (pageNo <= 0) {
                pageNo = 1;
            }
            if (pageSize <= 0 || pageSize > 30) {
                pageSize = 30;
            }

            long groupId = NumberUtils.toLong(params.get("groupId"), -1);
            if (groupId == -1) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }

            GroupUser groupUser = groupDao.loadGroupUser(userId, groupId);
            if (groupUser == null) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            String dateType = params.get("dateType"); // 1:??????,2:??????,3:??????
            int count = 0;
            Long totalCommissionCredit = 0l;
            List<HashMap<String, Object>> dataList = null;
            if (GroupConstants.isMasterOrAdmin(groupUser.getUserRole())) {
                // ??????
                long masterId = groupUser.getUserId();
                if (GroupConstants.isAdmin(groupUser.getUserRole())) {
                    masterId = groupDao.loadGroupMaster(String.valueOf(groupId)).getUserId();
                }
                count = groupCreditDao.countCreditCommissionLogForMaster(groupId, masterId, keyWord, dateType);
                if (count > 0) {
                    dataList = groupCreditDao.creditCommissionLogForMaster(groupId, masterId, dateType, keyWord, pageNo, pageSize);
                    //??????????????????
                    List<HashMap<String, Object>> teamMsgList = groupDao.loadGroupRelationCredit(String.valueOf(groupId), null);
                    Map<String, HashMap<String, Object>> teamMsgMap = new HashMap<>();

                    for (HashMap<String, Object> teamMsg : teamMsgList) {
                        teamMsgMap.put(teamMsg.get("userGroup").toString(), teamMsg);
                    }
                    List<HashMap<String, Object>> zjsList = groupCreditDao.creditZjsForMaster(groupId, dateType);
                    Map<String, HashMap<String, Object>> zjsMap = new HashMap<>();
                    if (zjsList != null && zjsList.size() > 0) {
                        for (HashMap<String, Object> zjs : zjsList) {
                            zjsMap.put(zjs.get("userGroup").toString(), zjs);
                        }
                    }
                    for (HashMap<String, Object> data : dataList) {
                        String userGroup = data.get("userGroup").toString();
                        HashMap<String, Object> zjs = zjsMap.get(userGroup);
                        if (zjs != null) {
                            data.putAll(zjs);
                        } else {
                            data.put("zjs", 0);
                        }
                        HashMap<String, Object> teamMsg = teamMsgMap.get(userGroup);
                        if (teamMsg != null) {
                            data.putAll(teamMsg);
                        } else {
                            if ("0".equals(userGroup)) {
                                //???????????????
                                data.put("teamName", "??????");
                                RegInfo regInfo = userDao.getUser(masterId);
                                if (regInfo != null) {
                                    data.put("userId", masterId);
                                    data.put("userName", regInfo.getName());
                                    data.put("headimgurl", regInfo.getHeadimgurl());
                                } else {
                                    data.put("userId", "000000");
                                    data.put("userName", "??????");
                                    data.put("headimgurl", "");
                                }
                            } else {
                                data.put("teamName", "???????????????");
                                data.put("userId", "000000");
                                data.put("userName", "?????????");
                                data.put("headimgurl", "");
                            }
                        }
                    }
                    totalCommissionCredit = groupCreditDao.sumCommissionCreditLog(groupId, masterId, dateType);
                } else {
                    dataList = Collections.emptyList();
                }

            } else if (GroupConstants.isTeamLeader(groupUser.getUserRole())) {
                // ?????????
                if (groupUser.getUserId().toString().equals(keyWord)) {
                    keyWord = "0";
                }
                count = groupCreditDao.countCreditCommissionLogForTeamLeader(groupId, groupUser.getUserId(), groupUser.getPromoterLevel(), keyWord, dateType);
                if (count > 0) {
                    dataList = groupCreditDao.creditCommissionLogForTeamLeader(groupId, groupUser.getUserId(), groupUser.getPromoterLevel(), dateType, keyWord, pageNo, pageSize);
                    //????????????
                    List<HashMap<String, Object>> teamMsgList = groupCreditDao.loadPromoterMsgForTeamLeader(groupId, groupUser.getUserGroup());
                    Map<String, HashMap<String, Object>> teamMsgMap = new HashMap<>();
                    for (HashMap<String, Object> teamMsg : teamMsgList) {
                        teamMsgMap.put(teamMsg.get("userId").toString(), teamMsg);
                    }
                    List<HashMap<String, Object>> zjsList = groupCreditDao.creditZjsForTeamLeader(groupId, groupUser.getUserGroup(), 0, dateType);
                    Map<String, HashMap<String, Object>> zjsMap = new HashMap<>();
                    if (zjsList != null && zjsList.size() > 0) {
                        for (HashMap<String, Object> zjs : zjsList) {
                            zjsMap.put(zjs.get("userId").toString(), zjs);
                        }
                    }
                    for (HashMap<String, Object> data : dataList) {
                        String uid = data.get("userId").toString();
                        HashMap<String, Object> zjs = zjsMap.get(uid);
                        if (zjs != null) {
                            data.putAll(zjs);
                        } else {
                            data.put("zjs", 0);
                        }
                        HashMap<String, Object> teamMsg = teamMsgMap.get(uid);
                        if (teamMsg != null) {
                            data.putAll(teamMsg);
                            data.put("userGroup", uid); //??????????????????
                        } else {
                            if ("0".equals(uid)) {
                                //???????????????
                                data.put("teamName", "??????");
                                RegInfo regInfo = userDao.getUser(groupUser.getUserId());
                                if (regInfo != null) {
                                    data.put("userId", groupUser.getUserId());
                                    data.put("userName", regInfo.getName());
                                    data.put("headimgurl", regInfo.getHeadimgurl());
                                } else {
                                    data.put("userId", "000000");
                                    data.put("userName", "??????");
                                    data.put("headimgurl", "");
                                }
                                data.put("userGroup", 0);//??????????????????
                            } else {
                                data.put("teamName", "???????????????");
                                data.put("userId", "000000");
                                data.put("userName", "?????????");
                                data.put("headimgurl", "");
                            }
                        }
                    }
                    totalCommissionCredit = groupCreditDao.sumCommissionCreditLog(groupId, userId, dateType);
                } else {
                    dataList = Collections.emptyList();
                }
            } else if (GroupConstants.isPromotor(groupUser.getUserRole())) {
                // ??????
                if (groupUser.getPromoterLevel() == 4) {
                    count = 1;
                } else {
                    count = groupCreditDao.countCreditCommissionLogForPromoter(groupId, groupUser.getUserId(), groupUser.getPromoterLevel(), keyWord, dateType);
                }
                if (count > 0) {
                    List<HashMap<String, Object>> zjsList;
                    if (groupUser.getPromoterLevel() == 4) {
                        dataList = groupCreditDao.creditCommissionLogForPromoter4(groupId, groupUser.getUserId(), dateType);
                        if (dataList == null || dataList.size() == 0) {
                            count = 0;
                        }
                        zjsList = groupCreditDao.creditZjsForPromoter4(groupId, groupUser.getUserGroup(), groupUser.getUserId(), groupUser.getPromoterLevel(), dateType);
                    } else {
                        dataList = groupCreditDao.creditCommissionLogForPromoter(groupId, groupUser.getUserId(), groupUser.getPromoterLevel(), dateType, keyWord, pageNo, pageSize);
                        zjsList = groupCreditDao.creditZjsForPromoter(groupId, groupUser.getUserGroup(), groupUser.getUserId(), groupUser.getPromoterLevel(), dateType);
                    }
                    //????????????
                    List<HashMap<String, Object>> teamMsgList = groupCreditDao.loadPromoterMsgForPromoter(groupId, groupUser.getUserId(), groupUser.getPromoterLevel());
                    Map<String, HashMap<String, Object>> teamMsgMap = new HashMap<>();
                    for (HashMap<String, Object> teamMsg : teamMsgList) {
                        teamMsgMap.put(teamMsg.get("userId").toString(), teamMsg);
                    }
                    Map<String, HashMap<String, Object>> zjsMap = new HashMap<>();
                    if (zjsList != null && zjsList.size() > 0) {
                        for (HashMap<String, Object> zjs : zjsList) {
                            zjsMap.put(zjs.get("userId").toString(), zjs);
                        }
                    }
                    for (HashMap<String, Object> data : dataList) {
                        String uid = data.get("userId").toString();
                        HashMap<String, Object> zjs = zjsMap.get(uid);
                        if (zjs != null) {
                            data.putAll(zjs);
                        } else {
                            data.put("zjs", 0);
                        }
                        HashMap<String, Object> teamMsg = teamMsgMap.get(uid);
                        if (teamMsg != null) {
                            data.putAll(teamMsg);
                            data.put("userGroup", uid);//??????????????????
                        } else {
                            if ("0".equals(uid)) {
                                //???????????????
                                data.put("teamName", "??????");
                                RegInfo regInfo = userDao.getUser(groupUser.getUserId());
                                if (regInfo != null) {
                                    data.put("userId", groupUser.getUserId());
                                    data.put("userName", regInfo.getName());
                                    data.put("headimgurl", regInfo.getHeadimgurl());
                                } else {
                                    data.put("userId", "000000");
                                    data.put("userName", "??????");
                                    data.put("headimgurl", "");
                                }
                                data.put("userGroup", 0);//??????????????????
                            } else {
                                data.put("teamName", "???????????????");
                                data.put("userId", "000000");
                                data.put("userName", "?????????");
                                data.put("headimgurl", "");
                            }
                        }
                    }
                    totalCommissionCredit = groupCreditDao.sumCommissionCreditLog(groupId, userId, dateType);
                } else {
                    dataList = Collections.emptyList();
                }
            } else {
                OutputUtil.output(0, LangMsg.getMsg(LangMsg.code_8), getRequest(), getResponse(), false);
                return;
            }
            JSONObject json = new JSONObject();
            json.put("pageNo", pageNo);
            json.put("pageSize", pageSize);
            json.put("total", count);
            json.put("pages", (int) Math.ceil(count * 1.0 / pageSize));
            json.put("dataList", dataList);
            json.put("totalCommissionCredit", totalCommissionCredit);
            OutputUtil.output(0, json, getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("loadCreditCommissionLog|error|" + JSON.toJSONString(params), e.getMessage(), e);
            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        }
    }

    /**
     * ???????????????
     */
    public void searchCreditCommissionLog() {
        Map<String, String> params = null;
        try {
            params = UrlParamUtil.getParameters(getRequest());
            LOGGER.debug("loadCreditCommissionLog|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), -1);
            if (!checkSessCode(Long.valueOf(userId), params.get("sessCode"))) {
                return;
            }
            long targetUserId = NumberUtils.toLong(params.get("targetUserId"), -1);
            long groupId = NumberUtils.toLong(params.get("groupId"), -1);
            String dateType = params.get("dateType"); // 1:??????,2:??????,3:??????
            if (groupId == -1 || targetUserId == -1) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }

            GroupUser groupUser = groupDao.loadGroupUser(userId, groupId);
            if (groupUser == null) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            GroupUser targetGroupUser = groupDao.loadGroupUser(targetUserId, groupId);
            if (targetGroupUser == null) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_14), getRequest(), getResponse(), false);
                return;
            }
            if(GroupConstants.isAdmin(groupUser.getUserRole())){
                GroupUser master = groupDao.loadGroupMaster(groupUser.getGroupId().toString());
                if(master != null){
                    userId = master.getUserId();
                }
            }
            JSONObject json = new JSONObject();
            HashMap<String, Object> data = groupCreditDao.searchCommissionLog(groupId, userId, targetUserId, dateType);
            if (data != null && Long.parseLong(data.get("commissionCredit").toString()) > 0) {
                RegInfo regInfo = userDao.getUser(targetGroupUser.getUserId());
                if (regInfo != null) {
                    data.put("userId", targetGroupUser.getUserId());
                    data.put("userName", regInfo.getName());
                    data.put("headimgurl", regInfo.getHeadimgurl());
                }
                json.put("data", data);
                HashMap<String, Object> commissonData = groupCreditDao.logGroupCommissionBuUser(groupId, targetUserId, dateType);
                if (commissonData != null) {
                    data.putAll(commissonData);
                } else {
                    data.put("zjs", 0);
                    data.put("winLoseCredit", 0);
                }
                if (targetGroupUser.getPromoterId4() > 0) {
                    data.put("promoterId", targetGroupUser.getPromoterId4());
                } else if (targetGroupUser.getPromoterId3() > 0) {
                    data.put("promoterId", targetGroupUser.getPromoterId3());
                } else if (targetGroupUser.getPromoterId2() > 0) {
                    data.put("promoterId", targetGroupUser.getPromoterId2());
                } else if (targetGroupUser.getPromoterId1()> 0) {
                    data.put("promoterId", targetGroupUser.getPromoterId1());
                } else {
                    data.put("promoterId", 0);
                }
                if (data.get("promoterId").toString().equals(String.valueOf(userId))) {
                    data.put("promoterId", 0);
                }
                data.remove("promoterId1");
                data.remove("promoterId2");
                data.remove("promoterId3");
                data.remove("promoterId4");
            }

            OutputUtil.output(0, json, getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("searchCreditCommissionLog|error|" + JSON.toJSONString(params), e.getMessage(), e);
            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        }
    }

    /**
     * ????????????
     */
    public void loadCreditCommissionLogByUser() {
        Map<String, String> params = null;
        try {
            if(true){
                interfaceDeprecated();
                return;
            }
            params = UrlParamUtil.getParameters(getRequest());
            LOGGER.debug("loadCreditCommissionLogByUser|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), -1);
            if (!checkSessCode(Long.valueOf(userId), params.get("sessCode"))) {
                return;
            }
            int pageNo = NumberUtils.toInt(params.get("pageNo"), 1);
            int pageSize = NumberUtils.toInt(params.get("pageSize"), 30);
            if (pageNo <= 0) {
                pageNo = 1;
            }
            if (pageSize <= 0 || pageSize > 30) {
                pageSize = 30;
            }

            long groupId = NumberUtils.toLong(params.get("groupId"), -1);
            if (groupId == -1) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            GroupUser groupUser = groupDao.loadGroupUser(userId, groupId);
            if (groupUser == null) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            String dateType = params.get("dateType"); // 1:??????,2:??????,3:??????
            int count = 0;
            Long totalCommissionCredit = 0l;
            List<HashMap<String, Object>> dataList = null;
            String userGroup;
            if (GroupConstants.isMasterOrAdmin(groupUser.getUserRole())) {
                userGroup = params.get("keyWord");
                if (StringUtils.isBlank(userGroup)) {
                    OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                    return;
                }
                // ??????
                long masterId = groupUser.getUserId();
                if (GroupConstants.isAdmin(groupUser.getUserRole())) {
                    masterId = groupDao.loadGroupMaster(String.valueOf(groupId)).getUserId();
                }
                count = groupCreditDao.countCreditCommissionLogByUserForMaster(groupId, userGroup, masterId, dateType);
                if (count > 0) {
                    dataList = groupCreditDao.creditCommissionLogByUserForMaster(groupId, userGroup, masterId, dateType, pageNo, pageSize);
                    totalCommissionCredit = groupCreditDao.sumCommissionCreditLog(groupId, userId, dateType);
                } else {
                    dataList = Collections.emptyList();
                }

            } else if (GroupConstants.isTeamLeader(groupUser.getUserRole()) || GroupConstants.isPromotor(groupUser.getUserRole())) {
                // ?????????
                Long promoterId = Long.valueOf(params.get("keyWord"));
                userGroup = groupUser.getUserGroup();
                count = groupCreditDao.countCreditCommissionLogByUser(groupId, userGroup, userId, promoterId, groupUser.getPromoterLevel(), dateType);
                if (count > 0) {
                    dataList = groupCreditDao.creditCommissionLogByUser(groupId, userGroup, userId, promoterId, groupUser.getPromoterLevel(), dateType, pageNo, pageSize);
                    totalCommissionCredit = groupCreditDao.sumCommissionCreditLog(groupId, userId, dateType);
                } else {
                    dataList = Collections.emptyList();
                }
            } else {
                OutputUtil.output(0, LangMsg.getMsg(LangMsg.code_8), getRequest(), getResponse(), false);
                return;
            }

            // ??????
            List<HashMap<String, Object>> zjsList = groupCreditDao.creditZjsByUser(groupId, userGroup, 1, dateType);
            Map<String, HashMap<String, Object>> zjsMap = new HashMap<>();
            if (zjsList.size() > 0 && zjsList.size() > 0) {
                for (HashMap<String, Object> zjs : zjsList) {
                    zjsMap.put(zjs.get("userId").toString(), zjs);
                }
            }
            StringJoiner userIds = new StringJoiner(",");
            for (HashMap<String, Object> data : dataList) {
                String uid = data.get("userId").toString();
                HashMap<String, Object> zjs = zjsMap.get(uid);
                if (zjs != null) {
                    data.putAll(zjs);
                } else {
                    data.put("zjs", 0);
                }
                if ((Long) data.get("promoterId4") > 0) {
                    data.put("promoterId", data.get("promoterId4"));
                } else if ((Long) data.get("promoterId3") > 0) {
                    data.put("promoterId", data.get("promoterId3"));
                } else if ((Long) data.get("promoterId2") > 0) {
                    data.put("promoterId", data.get("promoterId2"));
                } else if ((Long) data.get("promoterId1") > 0) {
                    data.put("promoterId", data.get("promoterId1"));
                } else {
                    data.put("promoterId", 0);
                }
                if (data.get("promoterId").toString().equals(String.valueOf(userId))) {
                    data.put("promoterId", 0);
                }
                data.remove("promoterId1");
                data.remove("promoterId2");
                data.remove("promoterId3");
                data.remove("promoterId4");
                userIds.add(uid);
            }
            // ???????????????
            Map<String, Object> winLoseCreditMap = groupCreditDao.winLoseCreditByUserIdsToMap(groupId, userIds.toString(), dateType);
            for (HashMap<String, Object> data : dataList) {
                String uid = data.get("userId").toString();
                if (winLoseCreditMap.containsKey(uid)) {
                    data.put("winLoseCredit", winLoseCreditMap.get(uid));
                } else {
                    data.put("winLoseCredit", 0);
                }
            }

            JSONObject json = new JSONObject();
            json.put("pageNo", pageNo);
            json.put("pageSize", pageSize);
            json.put("total", count);
            json.put("pages", (int) Math.ceil(count * 1.0 / pageSize));
            json.put("dataList", dataList);
            json.put("totalCommissionCredit", totalCommissionCredit);
            OutputUtil.output(0, json, getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("loadCreditCommissionLogByUser|error|" + JSON.toJSONString(params), e.getMessage(), e);
            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        }
    }

    /**
     * ??????????????????
     */
    public void updateCommissionRate() {
        Map<String, String> params = null;
        try {
            params = UrlParamUtil.getParameters(getRequest());
            LOGGER.debug("updateCommissionRate|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), 0);
            if (!checkSessCode(userId, params.get("sessCode"))) {
                return;
            }
            int rate = NumberUtils.toInt(params.get("commissionRate"), 0);
            if (rate < 0 || rate > 100) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }

            int groupId = NumberUtils.toInt(params.get("groupId"), -1);
            long targetUserId = NumberUtils.toInt(params.get("targetUserId"), -1);
            GroupInfo groupInfo = groupDao.loadGroupInfo(groupId, 0);
            if (groupInfo == null) {
                OutputUtil.output(2, LangMsg.getMsg(LangMsg.code_7), getRequest(), getResponse(), false);
                return;
            }

            GroupUser groupUser = groupDao.loadGroupUser(userId, groupId);
            if (groupUser == null || GroupConstants.isMember(groupUser.getUserRole())) {
                OutputUtil.output(2, LangMsg.getMsg(LangMsg.code_7), getRequest(), getResponse(), false);
                return;
            }
            GroupUser targetGroupUser = groupDao.loadGroupUser(targetUserId, groupId);
            if (targetGroupUser == null || GroupConstants.isMember(targetGroupUser.getUserRole())) {
                OutputUtil.output(2, LangMsg.getMsg(LangMsg.code_7), getRequest(), getResponse(), false);
                return;
            }
            if (GroupConstants.isMasterOrAdmin(groupUser.getUserRole())) {
                // ????????????????????????????????????????????????????????????
                int limitRate = -1;
                //???????????????
                if (GroupConstants.isTeamLeader(targetGroupUser.getUserRole())) {
                    limitRate = 100;
                } else {
                    GroupUser preLevel = findPreLevel(targetGroupUser);
                    if (preLevel != null) {
                        limitRate = preLevel.getCreditCommissionRate();
                    }
                }
                if (rate > limitRate) {
                    OutputUtil.output(3, "??????????????????????????????????????????" + limitRate, getRequest(), getResponse(), false);
                    return;
                }
            } else {
                if (!GroupConstants.isNextLevel(groupUser, targetGroupUser)) {
                    OutputUtil.output(2, LangMsg.getMsg(LangMsg.code_7), getRequest(), getResponse(), false);
                    return;
                }
                if (rate > groupUser.getCreditCommissionRate()) {
                    OutputUtil.output(3, "??????????????????????????????????????????" + groupUser.getCreditCommissionRate(), getRequest(), getResponse(), false);
                    return;
                }
            }
            String userGroup = targetGroupUser.getUserGroup();
            long promoterId = targetGroupUser.getUserId();
            int promoterLevel = targetGroupUser.getPromoterLevel();
            int userCount = groupCreditDao.countUserForPromoter(groupId, userGroup, promoterId, promoterLevel,null);
            int groupIncomeNum = Integer.valueOf(ResourcesConfigsUtil.loadServerPropertyValue("groupIncomeNum", "5"));
            if (userCount < groupIncomeNum) {
                OutputUtil.output(3, "???????????????????????????"+groupIncomeNum+"??????????????????????????????", getRequest(), getResponse(), false);
                return;
            }

            HashMap<String, Object> map = new HashMap<>();
            map.put("keyId", targetGroupUser.getKeyId());
            map.put("creditCommissionRate", rate);
            groupDao.updateGroupUserByKeyId(map);

            OutputUtil.output(0, LangMsg.getMsg(LangMsg.code_0), getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("updateCommissionRate|error|" + JSON.toJSONString(params), e.getMessage(), e);
            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        }
    }

    /**
     * ???????????????
     *
     * @param target
     * @return
     */
    private GroupUser findPreLevel(GroupUser target) {
        GroupUser res = null;
        if (target == null) {
            return res;
        }
        try {
            if (target.getPromoterLevel() == 0) {
                res = groupDao.loadGroupMaster(target.getGroupId().toString());
            } else if (target.getPromoterLevel() == 1) {
                res = groupDao.loadGroupTeamMaster(target.getGroupId().toString(), target.getUserGroup());
            } else if (target.getPromoterLevel() == 2) {
                res = groupDao.loadGroupUser(target.getPromoterId1(), target.getGroupId());
            } else if (target.getPromoterLevel() == 3) {
                res = groupDao.loadGroupUser(target.getPromoterId2(), target.getGroupId());
            } else if (target.getPromoterLevel() == 4) {
                res = groupDao.loadGroupUser(target.getPromoterId3(), target.getGroupId());
            } else if (target.getPromoterLevel() == 5) {
                res = groupDao.loadGroupUser(target.getPromoterId4(), target.getGroupId());
            }
        } catch (Exception e) {
            LOGGER.error("findPreLevel|error|" + JSON.toJSONString(target), e.getMessage(), e);
        }
        return res;
    }

    /**
     * ??????????????????????????????
     */
    public void userListForLogDetail() {
        Map<String, String> params = null;
        try {
            params = UrlParamUtil.getParameters(getRequest());
            LOGGER.debug("userListForLog|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), 0);
            if (!checkSessCode(userId, params.get("sessCode"))) {
                return;
            }

            long groupId = NumberUtils.toInt(params.get("groupId"), -1);
            GroupInfo groupInfo = groupDao.loadGroupInfo(groupId, 0);
            if (groupInfo == null) {
                OutputUtil.output(2, LangMsg.getMsg(LangMsg.code_7), getRequest(), getResponse(), false);
                return;
            }

            GroupUser groupUser = groupDao.loadGroupUser(userId, groupId);
            if (groupUser == null) {
                OutputUtil.output(2, LangMsg.getMsg(LangMsg.code_7), getRequest(), getResponse(), false);
                return;
            }
            int creditOrder = NumberUtils.toInt(params.get("creditOrder"), 0); //??????????????????0??????????????????????????? 1???????????????,???????????????????????????
            int pageNo = NumberUtils.toInt(params.get("pageNo"), 1);
            int pageSize = NumberUtils.toInt(params.get("pageSize"), 30);

            String keyWord = params.get("keyWord");

            pageNo = pageNo < 0 ? 1 : pageNo;
            pageSize = pageSize < 1 ? 5 : pageSize > 30 ? 30 : pageSize;
            int userCount = 0;
            List<Map<String, Object>> userList = Collections.emptyList();
            JSONObject json = new JSONObject();
            json.put("pageNo", pageNo);
            json.put("pageSize", pageSize);
            if (GroupConstants.isMasterOrAdmin(groupUser.getUserRole())) {
                // ?????????????????????????????????
                userCount = groupCreditDao.countUserForMaster(groupId, keyWord);
                if (userCount > 0) {
                    userList = groupCreditDao.userListForMaster(groupId, keyWord,creditOrder, pageNo, pageSize);
                }
            } else if (GroupConstants.isTeamLeader(groupUser.getUserRole())) {
                String userGroup = groupUser.getUserGroup();
                userCount = groupCreditDao.countUserForTeamLeader(groupId, userGroup, keyWord);
                if (userCount > 0) {
                    userList = groupCreditDao.userListForTeamLeader(groupId, userGroup, keyWord,creditOrder, pageNo, pageSize);
                }
            } else if (GroupConstants.isPromotor(groupUser.getUserRole())) {
                String userGroup = groupUser.getUserGroup();
                long promoterId = groupUser.getUserId();
                int promoterLevel = groupUser.getPromoterLevel();
                userCount = groupCreditDao.countUserForPromoter(groupId, userGroup, promoterId, promoterLevel, keyWord);
                if (userCount > 0) {
                    userList = groupCreditDao.userListForPromoter(groupId, userGroup, promoterId, promoterLevel, keyWord,creditOrder, pageNo, pageSize);
                }
            } else {
                OutputUtil.output(2, "??????????????????????????????", getRequest(), getResponse(), false);
                return;
            }
            if (pageNo == 1 && userList != null && userList.size() > 0 && StringUtils.isBlank(keyWord)) {
                // ?????????????????????????????????????????????????????????????????????????????????
                int selfIndex = -1;
                for (int i = 0; i < userList.size(); i++) {
                    Map<String, Object> data = userList.get(i);
                    if (data != null && String.valueOf(userId).equals(String.valueOf(data.get("userId")))) {
                        selfIndex = i;
                        break;
                    }
                }
                Map<String, Object> selfData;
                if (selfIndex != -1) {
                    selfData = userList.remove(selfIndex);
                } else {
                    selfData = new HashMap<>();
                    selfData.put("userId", groupUser.getUserId());
                    RegInfo self = userDao.getUser(userId);
                    if (self != null) {
                        selfData.put("userName", self.getName());
                        selfData.put("headimgurl", self.getHeadimgurl());
                    } else {
                        selfData.put("userName", "");
                        selfData.put("headimgurl", "");
                    }
                    selfData.put("credit", groupUser.getCredit());
                }
                List<Map<String, Object>> tmpList = new ArrayList<>();
                tmpList.add(selfData);
                tmpList.addAll(userList);
                userList = tmpList;
            }
            if (userList != null && userList.size() > 0) {
                List<GroupUser> masterAndTeamLeader = groupCreditDao.loadMasterAndTeamLeader(groupId);
                Map<String, GroupUser> userGroupMap = new HashMap<>();
                if (masterAndTeamLeader != null && masterAndTeamLeader.size() > 0) {
                    for (GroupUser u : masterAndTeamLeader) {
                        userGroupMap.put(u.getUserGroup(), u);
                    }
                }
                for (Map<String, Object> user : userList) {
                    //?????????id
                    GroupUser tmp = userGroupMap.get(user.get("userGroup"));
                    if (tmp != null) {
                        user.put("teamLeaderId", tmp.getUserId());
                    }

                    //????????????id
                    if (Integer.valueOf(0) == user.get("promoterLevel")) {
                        tmp = userGroupMap.get("0");
                        if (tmp != null) {
                            user.put("preUserId", tmp.getUserId());
                        }
                    } else if (Integer.valueOf(1) == user.get("promoterLevel")) {
                        tmp = userGroupMap.get(user.get("userGroup"));
                        if (tmp != null) {
                            user.put("preUserId", tmp.getUserId());
                        }
                    } else if (Integer.valueOf(2) == user.get("promoterLevel")) {
                        user.put("preUserId", user.get("promoterId1"));
                    } else if (Integer.valueOf(3) == user.get("promoterLevel")) {
                        user.put("preUserId", user.get("promoterId2"));
                    } else if (Integer.valueOf(4) == user.get("promoterLevel")) {
                        user.put("preUserId", user.get("promoterId3"));
                    } else if (Integer.valueOf(5) == user.get("promoterLevel")) {
                        user.put("preUserId", user.get("promoterId4"));
                    }
                }
            }
            json.put("userList", userList);
            json.put("userCount", userCount);
            json.put("total", userCount);
            json.put("pages", (int) Math.ceil(userCount * 1.0 / pageSize));
            OutputUtil.output(0, json, getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("userListForLog|error|" + JSON.toJSONString(params), e.getMessage(), e);
            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        }
    }


    /**
     * ???????????????
     */
    public void creditLogList() {
        Map<String, String> params = null;
        try {
            params = UrlParamUtil.getParameters(getRequest());
            LOGGER.debug("creditLogList|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), 0);
            if (!checkSessCode(userId, params.get("sessCode"))) {
                return;
            }

            long groupId = NumberUtils.toInt(params.get("groupId"), -1);
            GroupInfo groupInfo = groupDao.loadGroupInfo(groupId, 0);
            if (groupInfo == null) {
                OutputUtil.output(2, LangMsg.getMsg(LangMsg.code_7), getRequest(), getResponse(), false);
                return;
            }

            long targetId = NumberUtils.toLong(params.get("targetId"), 0);
            int selectType = NumberUtils.toInt(params.get("selectType"), 0); // 0????????????1???????????????2????????????3?????????
            int upOrDown = 0;
            GroupUser groupUser = groupDao.loadGroupUser(userId, groupId);
            if (groupUser == null) {
                OutputUtil.output(2, LangMsg.getMsg(LangMsg.code_7), getRequest(), getResponse(), false);
                return;
            }
            GroupUser targetUser = groupDao.loadGroupUser(targetId, groupId);
            if (targetUser == null) {
                OutputUtil.output(2, LangMsg.getMsg(LangMsg.code_14), getRequest(), getResponse(), false);
                return;
            }
            if (targetId != userId && !GroupConstants.isLower(groupUser, targetUser)) {
                OutputUtil.output(2, LangMsg.getMsg(LangMsg.code_7), getRequest(), getResponse(), false);
                return;
            }
            int pageNo = NumberUtils.toInt(params.get("pageNo"), 1);
            int pageSize = NumberUtils.toInt(params.get("pageSize"), 30);
            pageNo = pageNo < 0 ? 1 : pageNo;
            pageSize = pageSize < 1 ? 5 : pageSize > 30 ? 30 : pageSize;
            String dateType = params.get("dateType"); // 1:??????,2:??????,3:??????
            int dataCount = groupCreditDao.countCreditLogList(groupId, targetId, selectType, dateType, upOrDown);
            List<Map<String, Object>> dataList = Collections.emptyList();
            long sumCredit = 0;
            if(dataCount >0){
                dataList = groupCreditDao.creditLogList(groupId, targetId, selectType, dateType, upOrDown, pageNo, pageSize);
                sumCredit = groupCreditDao.sumCreditLogList(groupId, targetId, selectType, dateType, upOrDown);
            }
            JSONObject json = new JSONObject();
            json.put("pageNo", pageNo);
            json.put("pageSize", pageSize);
            json.put("total", dataCount);
            json.put("sumCredit", sumCredit);
            json.put("dataList", dataList);
            json.put("pages", (int) Math.ceil(dataCount * 1.0 / pageSize));
            OutputUtil.output(0, json, getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("creditLogList|error|" + JSON.toJSONString(params), e.getMessage(), e);
            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        }
    }

    /**
     * ?????????????????????: ???
     */
    public void loadCommissionConfig() {
        Map<String, String> params = null;
        try {
            params = UrlParamUtil.getParameters(getRequest());
            LOGGER.debug("loadCommissionConfig|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), 0);

            if (!checkSessCode(userId, params.get("sessCode"))) {
                return;
            }
            long targetUserId = NumberUtils.toLong(params.get("targetUserId"), 0);
            long groupId = NumberUtils.toInt(params.get("groupId"), -1);
            GroupInfo groupInfo = groupDao.loadGroupInfo(groupId, 0);
            if (groupInfo == null) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            GroupUser groupUser = groupDao.loadGroupUser(userId, groupId);
            if (groupUser == null || GroupConstants.isMember(groupUser.getUserRole())) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            GroupUser targetGroupUser = groupDao.loadGroupUser(targetUserId, groupId);
            if (targetGroupUser == null || GroupConstants.isMember(targetGroupUser.getUserRole())) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            if (!GroupConstants.isLower(groupUser, targetGroupUser)) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_7), getRequest(), getResponse(), false);
                return;
            }
            JSONObject json = new JSONObject();
            json.put("creditAllotMode", groupInfo.getCreditAllotMode());
            json.put("creditRate", groupInfo.getCreditRate());
            json.put("dataList", loadCommissionConfig(groupUser, targetGroupUser, groupInfo.getCreditRate()));
            OutputUtil.output(0, json, getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("loadCommissionConfig|error|" + JSON.toJSONString(params), e.getMessage(), e);
            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        }
    }

    private List<Map<String, Object>> loadCommissionConfig(GroupUser groupUser, GroupUser target, int creditRate) throws Exception {
        long groupId = groupUser.getGroupId();
        List<GroupCommissionConfig> creditConfigList = groupCreditDao.loadCommissionConfig(groupId, target.getUserId());
        Map<Integer, GroupCommissionConfig> creditConfigMap = new HashMap<>();
        if (creditConfigList != null && !creditConfigList.isEmpty()) {
            for (GroupCommissionConfig c : creditConfigList) {
                creditConfigMap.put(c.getSeq(), c);
            }
        }

        List<GroupCommissionConfig> preCreditConfigList = null;
        Map<Integer, GroupCommissionConfig> preCreditConfigMap = new HashMap<>();
        long preUserId = getPreUserId(target);
        if (preUserId > 0) {
            preCreditConfigList = groupCreditDao.loadCommissionConfig(groupId, preUserId);
        }
        if (preCreditConfigList != null && !preCreditConfigList.isEmpty()) {
            for (GroupCommissionConfig c : preCreditConfigList) {
                preCreditConfigMap.put(c.getSeq(), c);
            }
        }

        List<Map<String, Object>> dataList = new ArrayList<>();
        for (int seq = 1; seq <= 10; seq++) {
            GroupCommissionConfig sysConfig = GroupConstants.getSysCommissionConfig(seq, creditRate);
            GroupCommissionConfig selfConfig = creditConfigMap.get(seq);
            if (selfConfig == null) {
                selfConfig = sysConfig;
            }
            Map<String, Object> data = new HashMap<>();
            data.put("minValue", selfConfig.getMinCredit());
            data.put("maxValue", selfConfig.getMaxCredit());
            data.put("myValue", selfConfig.getCredit());
            data.put("logValue", selfConfig.getMaxCreditLog());
            if (preCreditConfigMap.containsKey(seq)) {
                GroupCommissionConfig preConfig = preCreditConfigMap.get(seq);
                data.put("preValue", sysConfig.getMaxCredit() - preConfig.getLeftCredit());   // ?????????????????????
                data.put("myMaxValue", preConfig.getLeftCredit());    // ????????????????????????
            } else {
                data.put("preValue", 0);
                data.put("myMaxValue", sysConfig.getMaxCredit());
            }
            dataList.add(data);
        }
        return dataList;
    }


    /**
     * ???????????????: ???
     */
    public void updateCommissionConfig() {
        Map<String, String> params = null;
        try {
            params = UrlParamUtil.getParameters(getRequest());
            LOGGER.debug("updateCommissionConfig|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), 0);

            if (!checkSessCode(userId, params.get("sessCode"))) {
                return;
            }
            long targetUserId = NumberUtils.toLong(params.get("targetUserId"), 0);
            long groupId = NumberUtils.toInt(params.get("groupId"), -1);
            String values = params.get("values");
            if (StringUtils.isBlank(values)) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            GroupInfo groupInfo = groupDao.loadGroupInfo(groupId, 0);
            if (groupInfo == null) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            GroupUser groupUser = groupDao.loadGroupUser(userId, groupId);
            if (groupUser == null || GroupConstants.isMember(groupUser.getUserRole())) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            GroupUser targetGroupUser = groupDao.loadGroupUser(targetUserId, groupId);
            if (targetGroupUser == null || GroupConstants.isMember(targetGroupUser.getUserRole())) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            if (!GroupConstants.isLower(groupUser, targetGroupUser)) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_7), getRequest(), getResponse(), false);
                return;
            }

            String[] valueArr = values.split(",");
            List<GroupCommissionConfig> selfConfigList = groupCreditDao.loadCommissionConfig(groupId, targetUserId);
            Map<Integer, GroupCommissionConfig> selfConfigMap = new HashMap<>();
            if (selfConfigList != null && !selfConfigList.isEmpty()) {
                for (GroupCommissionConfig c : selfConfigList) {
                    selfConfigMap.put(c.getSeq(), c);
                }
            }

            Map<Integer, GroupCommissionConfig> preConfigMap = new HashMap<>();
            long preUserId = getPreUserId(targetGroupUser);
            if (preUserId != 0) {
                List<GroupCommissionConfig> preConfigList = groupCreditDao.loadCommissionConfig(groupId, preUserId);
                if (preConfigList != null && !preConfigList.isEmpty()) {
                    for (GroupCommissionConfig c : preConfigList) {
                        preConfigMap.put(c.getSeq(), c);
                    }
                }
            }

            Date now = new Date();
            for (int seq = 1; seq <= valueArr.length; seq++) {
                GroupCommissionConfig sysConfig = GroupConstants.getSysCommissionConfig(seq, groupInfo.getCreditRate());
                GroupCommissionConfig selfConfig = selfConfigMap.get(seq);
                long credit = Long.valueOf(valueArr[seq-1]);
                long creditLog = 0;
                long leftCredit = sysConfig.getMaxCredit() - credit;
                if (preConfigMap.containsKey(seq)) {
                    creditLog = preConfigMap.get(seq).getCredit();
                    leftCredit = preConfigMap.get(seq).getLeftCredit() - credit;
                    if(leftCredit < 0){
                        leftCredit = 0;
                    }
                }
                if (selfConfig == null) {
                    selfConfig = GroupConstants.genGroupCommissionConfig(sysConfig.getSeq(), sysConfig.getMinCredit(), sysConfig.getMaxCredit());
                    selfConfig.setGroupId(groupId);
                    selfConfig.setUserId(targetUserId);
                    selfConfig.setCredit(credit);
                    selfConfig.setLeftCredit(leftCredit);
                    selfConfig.setMaxCreditLog(creditLog);
                    selfConfig.setCreatedTime(now);
                    selfConfig.setLastUpTime(now);
                    groupCreditDao.insertCommissionConfig(selfConfig);
                } else {
                    if (credit != selfConfig.getCredit()) {
                        groupCreditDao.updateCommissionConfig(groupId, targetUserId, seq, credit,leftCredit, creditLog);
                    }
                }
            }
            JSONObject json = new JSONObject();
            json.put("msg",LangMsg.getMsg(LangMsg.code_0));
            OutputUtil.output(0, json, getRequest(), getResponse(), false);

            //?????????????????????????????????
            updateGroupUserExt(targetGroupUser, GroupConstants.extKey_commissionConfig, "1");

            LOGGER.info("updateCommissionConfig|succ|params:{}", params);
        } catch (Exception e) {
            LOGGER.error("updateCommissionConfig|error|" + JSON.toJSONString(params), e.getMessage(), e);
            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        }
    }

    public long getPreUserId(GroupUser groupUser) throws Exception {
        long groupId = groupUser.getGroupId();
        long preUserId = 0;
        if (GroupConstants.isTeamLeader(groupUser.getUserRole())) {
            // ?????????
            // ?????????????????????
            GroupUser master = groupDao.loadGroupMaster(String.valueOf(groupId));
            preUserId = master.getUserId();
        } else if (GroupConstants.isPromotor(groupUser.getUserRole())) {
            //??????
            if (groupUser.getPromoterLevel() == 1) {
                // ????????????????????????
                GroupUser teamLeader = groupDao.loadGroupTeamMaster(String.valueOf(groupId), groupUser.getUserGroup());
                if (teamLeader != null) {
                    preUserId = teamLeader.getUserId();
                }
            } else if (groupUser.getPromoterLevel() == 2) {
                preUserId = groupUser.getPromoterId1();
            } else if (groupUser.getPromoterLevel() == 3) {
                preUserId = groupUser.getPromoterId2();
            } else if (groupUser.getPromoterLevel() == 4) {
                preUserId = groupUser.getPromoterId3();
            } else if (groupUser.getPromoterLevel() == 5) {
                preUserId = groupUser.getPromoterId4();
            }
        }
        return preUserId;
    }

    /**
     * ???????????????????????????
     */
    public void updateGroupCreditRate() {
        Map<String, String> params = null;
        try {
            params = UrlParamUtil.getParameters(getRequest());
            LOGGER.debug("updateGroupCreditRate|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), 0);
            if (!checkSessCode(userId, params.get("sessCode"))) {
                return;
            }
            long groupId = NumberUtils.toInt(params.get("groupId"), -1);
            int creditRate = NumberUtils.toInt(params.get("creditRate"), -1);
            if (creditRate != 1 && creditRate != 10 && creditRate != 100) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            GroupInfo groupInfo = groupDao.loadGroupInfo(groupId, 0);
            if (groupInfo == null) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            GroupUser groupUser = groupDao.loadGroupUser(userId, groupId);
            if (groupUser == null || !GroupConstants.isMaster(groupUser.getUserRole())) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_7), getRequest(), getResponse(), false);
                return;
            }
            if (groupCreditDao.updateCreditRate(groupId, creditRate) <= 0) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
                return;
            }
            for (int seq= 1; seq <= 10; seq++) {
                GroupCommissionConfig sysConifg = GroupConstants.getSysCommissionConfig(seq, creditRate);
                if(sysConifg != null){
                    groupCreditDao.resetCommissionConfig(groupId,seq,sysConifg.getMinCredit(),sysConifg.getMaxCredit());
                }
            }
            JSONObject json = new JSONObject();
            json.put("msg",LangMsg.getMsg(LangMsg.code_0));
            OutputUtil.output(0, json, getRequest(), getResponse(), false);
            LOGGER.info("updateGroupCreditRate|succ|params:{}", params);
        } catch (Exception e) {
            LOGGER.error("updateGroupCreditRate|error|" + JSON.toJSONString(params), e.getMessage(), e);
            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        }
    }

    /**
     * ???????????????ext??????
     * @param groupUser
     * @param key
     * @param value
     * @throws Exception
     */
    public void updateGroupUserExt(GroupUser groupUser , String key , String value) throws Exception{

        JSONObject extJson = StringUtils.isBlank(groupUser.getExt()) ? new JSONObject() : JSONObject.parseObject(groupUser.getExt());
        HashMap<String, Object> map = new HashMap<>();
        if (!extJson.containsKey(key)) {
            extJson.put(key,value);
            map.put("ext", extJson.toString());
        } else {
            if (!value.equals(extJson.getString(key))) {
                extJson.put(key,value);
                map.put("ext", extJson.toString());
            }
        }
        if (map.size() > 0) {
            map.put("keyId", groupUser.getKeyId());
            groupDao.updateGroupUserByKeyId(map);
        }
    }

    private void insertGroupUserAlert(long groupId, long userId, long optUserId, int type) {
        LogGroupUserAlert log = new LogGroupUserAlert();
        log.setGroupId(groupId);
        log.setUserId(userId);
        log.setOptUserId(optUserId);
        log.setType(type);
        try {
            groupCreditDao.insertGroupUserAlert(log);
        } catch (Exception e) {
            LOGGER.error("insertGroupUserAlert|error|" + e.getMessage(), e);
        }
    }

    /**
     * ????????????
     */
    public void loadGroupUserAlert() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("loadGroupUserAlert|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toInt(params.get("userId"), 0);
            if (!checkSessCode(userId, params.get("sessCode"))) {
                return;
            }
            int groupId = NumberUtils.toInt(params.get("groupId"), -1);
            int pageNo = NumberUtils.toInt(params.get("pageNo"), 1);
            int pageSize = NumberUtils.toInt(params.get("pageSize"), 30);
            long queryUserId = NumberUtils.toInt(params.get("queryUserId"), 0);
            int selectType = NumberUtils.toInt(params.get("selectType"), 0); // ???????????????0??????????????????1????????????????????????2???????????????????????????0

            GroupUser self = groupDao.loadGroupUser(userId, groupId);
            if (self == null) {
                OutputUtil.output(3, LangMsg.getMsg(LangMsg.code_9), getRequest(), getResponse(), false);
                return;
            } else if (!GroupConstants.isMasterOrAdmin(self.getUserRole())) {
                OutputUtil.output(3, LangMsg.getMsg(LangMsg.code_8), getRequest(), getResponse(), false);
                return;
            }

            int count = groupCreditDao.countGroupUserAlert(groupId, selectType, queryUserId);
            List<HashMap<String, Object>> dataList = Collections.emptyList();
            if (count > 0) {
                dataList = groupCreditDao.loadGroupUserAlert(groupId, selectType, queryUserId, pageNo, pageSize);
            }
            JSONObject json = new JSONObject();
            json.put("pageNo", pageNo);
            json.put("pageSize", pageSize);
            json.put("list", dataList);
            json.put("pages", (int) Math.ceil(count * 1.0 / pageSize));
            json.put("total", count);
            OutputUtil.output(0, json, getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("loadGroupUserAlert|error|" + e.getMessage(), e);
            OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        }
    }

}
