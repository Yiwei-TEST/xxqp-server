package com.sy.sanguo.game.action.group;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sy.mainland.util.CommonUtil;
import com.sy.mainland.util.PropertiesCacheUtil;
import com.sy.sanguo.common.struts.GameStrutsAction;
import com.sy.sanguo.common.util.*;
import com.sy.sanguo.common.util.lenovo.DateUtil;
import com.sy.sanguo.common.util.user.GameUtil;
import com.sy.sanguo.game.bean.Activity;
import com.sy.sanguo.game.bean.RegInfo;
import com.sy.sanguo.game.bean.group.*;
import com.sy.sanguo.game.constants.GroupConstants;
import com.sy.sanguo.game.constants.GroupUserTree;
import com.sy.sanguo.game.dao.ActivityDao;
import com.sy.sanguo.game.dao.DataStatisticsDao;
import com.sy.sanguo.game.dao.UserDaoImpl;
import com.sy.sanguo.game.dao.group.GroupDao;
import com.sy.sanguo.game.dao.group.GroupDaoNew;
import com.sy.sanguo.game.dao.group.GroupWarnDao;
import com.sy.sanguo.game.pdkuai.constants.SharedConstants;
import com.sy.sanguo.game.pdkuai.db.bean.RedPacketRecord;
import com.sy.sanguo.game.pdkuai.db.dao.RedPacketDao;
import com.sy.sanguo.game.pdkuai.db.dao.TableLogDao;
import com.sy.sanguo.game.pdkuai.db.dblock.DbLockEnum;
import com.sy.sanguo.game.pdkuai.db.dblock.DbLockUtil;
import com.sy.sanguo.game.pdkuai.user.Manager;
import com.sy.sanguo.game.utils.BjdUtil;
import com.sy.sanguo.game.utils.GroupUtil;
import com.sy.sanguo.game.utils.bean.CommonRes;
import com.sy599.sanguo.util.GroupConfigUtil;
import com.sy599.sanguo.util.ResourcesConfigsUtil;
import com.sy599.sanguo.util.TimeUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 亲友圈
 */
public class GroupActionNew extends GameStrutsAction {

    private static final Logger LOGGER = LoggerFactory.getLogger("sys");

    private static final int min_group_id = 1000;
    private UserDaoImpl userDao;
    private GroupDao groupDao;
    private GroupDaoNew groupDaoNew;
    private DataStatisticsDao dataStatisticsDao;
    private GroupWarnDao groupWarnDao;

    private static final Map<String, Object> lockMap = new ConcurrentHashMap<>();

    public Object getGroupLock(long groupId) {
        String key = groupId + "";
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

    public void setUserDao(UserDaoImpl userDao) {
        this.userDao = userDao;
    }

    public void setGroupDao(GroupDao groupDao) {
        this.groupDao = groupDao;
    }

    public void setDataStatisticsDao(DataStatisticsDao dataStatisticsDao) {
        this.dataStatisticsDao = dataStatisticsDao;
    }

    public void setGroupDaoNew(GroupDaoNew groupDaoNew) {
        this.groupDaoNew = groupDaoNew;
    }

    public void setGroupWarnDao(GroupWarnDao groupWarnDao) {
        this.groupWarnDao = groupWarnDao;
    }

    public RegInfo checkSessCodeNew(long userId, String sessCode) throws Exception {
        if (sessCode == null) {
            return null;
        }
        RegInfo user = userDao.getUserForceMaster(userId);
        if (user == null || !sessCode.equals(user.getSessCode())) {
            return null;
        }
        return user;
    }

    /**
     * 创建亲友圈
     */
    public void createGroup() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("createGroup|params|" + params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            String groupName = params.get("groupName");
            long userId = NumberUtils.toLong(params.get("userId"), 0);
            int groupId = NumberUtils.toInt(params.get("groupId"), 0);

            RegInfo user = userDao.getUser(userId);
            if(user.getIsCreateGroup()==0){
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_8), getRequest(), getResponse(), false);
                return;
            }
            CommonRes res = GroupUtil.createGroup(userDao, groupDaoNew, userId, 0, "" + userId, null);
            if (res.getCode() != 0) {
                OutputUtil.output(res.getCode(), res.getMsg(), getRequest(), getResponse(), false);
                return;
            }
            groupId = (int) res.getData();

            //  通知代理后台
            BjdUtil.notifyCreateGroup(userId, groupId);
            OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(0, "创建成功").builder("groupId", groupId), getRequest(), getResponse(), null, false);
            LOGGER.info("createGroup|succ|" + groupId + "|" + groupName);
        } catch (Exception e) {
            OutputUtil.output(2, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
            LOGGER.error("createGroup|error|" + e.getMessage(), e);
        }

    }

    /**
     * 获取亲友圈等级配置信息
     */
    public void loadGroupLevelConfig() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("loadGroupLevelConfig|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), 0);
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
                return;
            }
            JSONObject json = new JSONObject();
            json.put("gConfigs", GroupConfigUtil.getGroupLevelConfigList());
            json.put("guConfigs", GroupConfigUtil.getGroupUserLevelConfigList());

            OutputUtil.output(0, json, getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }


    /**
     * 获取亲友圈列表信息
     */
    public void loadGroupList() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("loadGroupList|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), 0);
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
                return;
            }
            int pageNo = NumberUtils.toInt(params.get("pageNo"), 1);
            int pageSize = NumberUtils.toInt(params.get("pageSize"), 10);
            if (pageNo < 1) {
                pageNo = 1;
            }
            if (pageSize < 1) {
                pageSize = 10;
            }

            JSONObject json = new JSONObject();
            json.put("userId", user.getUserId());
            json.put("name", user.getName());
            json.put("sex", user.getSex());
            json.put("enterServer", user.getEnterServer());
            json.put("headimgurl", user.getHeadimgurl());
            json.put("isOnLine", user.getIsOnLine());

            List<GroupUser> groupUsers = groupDaoNew.loadGroupUsersByUser(userId, pageNo, pageSize);
            if (groupUsers == null && groupUsers.size() == 0) {
                //没有亲友圈
                json.put("list", Collections.emptyList());
                OutputUtil.output(0, json, getRequest(), getResponse(), false);
                return;
            }
            List<HashMap<String, Object>> list = new ArrayList<>(groupUsers.size());
            for (GroupUser self : groupUsers) {
                HashMap<String, Object> data = genGroupInfoMap(self);
                if (data != null) {
                    list.add(data);
                }
            }
            json.put("list", list);
            OutputUtil.output(0, json, getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 生成亲友圈信息
     *
     * @param self
     * @return
     * @throws Exception
     */
    public HashMap<String, Object> genGroupInfoMap(GroupUser self) throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        GroupInfo group = groupDaoNew.loadGroupInfo(self.getGroupId());
        if (group == null) {
            LOGGER.error("group is not exists,delete all group user:groupId={}", self.getGroupId());
            return null;
        }
        map.put("groupKeyId", group.getKeyId());
        map.put("groupId", group.getGroupId());
        map.put("groupName", GroupConstants.filterGroupName(group.getGroupName()));
        map.put("content", group.getContent());
        map.put("currentCount", group.getCurrentCount());
        JSONObject jsonObject = StringUtils.isBlank(group.getExtMsg()) ? new JSONObject() : JSONObject.parseObject(group.getExtMsg());
        if ("1".equals(PropertiesCacheUtil.getValueOrDefault("group_kf_oq", "0", Constants.GAME_FILE))) {
            String str = jsonObject.getString("oq");
            if (StringUtils.isBlank(str)) {
                jsonObject.put("oq", "-q");
            }
        }
        if (!jsonObject.containsKey("ac")) {
            jsonObject.put("ac", "-a");
        }
        map.put("extMsg", jsonObject.toString());
        map.put("creditOpen", group.getIsCredit());
        map.put("creditAllotMode", group.getCreditAllotMode());
        map.put("gameIDs", group.getGameIds());
        map.put("masterCard", 0);
        if (GroupConstants.isHuiZhang(self.getUserRole())) {
            RegInfo masterInf = userDao.getUser(self.getUserId());
            if (masterInf != null) {
                map.put("masterImg", masterInf.getHeadimgurl());
                map.put("payBindId", masterInf.getPayBindId());
                map.put("masterCard", (masterInf.getCards() + masterInf.getFreeCards()));
            }
        } else {
            GroupUser groupMaster = groupDaoNew.loadGroupMaster(self.getGroupId());
            if (groupMaster != null) {
                RegInfo masterInf = userDao.getUser(groupMaster.getUserId());
                if (masterInf != null) {
                    map.put("masterImg", masterInf.getHeadimgurl());
                    map.put("payBindId", masterInf.getPayBindId());
                    map.put("masterCard", (masterInf.getCards() + masterInf.getFreeCards()));
                }
            } else {
                map.put("masterImg", "");
            }
        }
        map.put("tables", groupDao.countGroupTables(self.getGroupId()));

        Map<String, Object> gLevelMsg = new HashMap<>();
        gLevelMsg.put("switchCoin", group.getSwitchCoin());
        gLevelMsg.put("level", group.getLevel());
        gLevelMsg.put("exp", group.getExp());
        gLevelMsg.put("totalExp", group.getTotalExp());
        gLevelMsg.put("creditExpToday", group.getCreditExpToday());
        map.put("gLevelMsg", gLevelMsg);

        map.put("userLevel", self.getUserLevel());
        map.put("userRole", self.getUserRole());
        map.put("frameId", self.getFrameId());
        map.put("refuseInvite", self.getRefuseInvite());
        map.put("credit", self.getCredit());
        map.put("creditLock", self.getCreditLock());
        map.put("userLevel", self.getUserLevel());
        map.put("promoterLevel", self.getPromoterLevel());

        Map<String, Object> guLevelMsg = new HashMap<>();
        guLevelMsg.put("level", self.getLevel());
        guLevelMsg.put("exp", self.getExp());
        guLevelMsg.put("totalExp", self.getTotalExp());
        guLevelMsg.put("creditExpToday", self.getCreditExpToday());
        map.put("guLevelMsg", guLevelMsg);

        List<HashMap<String, Object>> levelLogList = groupDaoNew.loadLogGroupUserLevel(self.getGroupId(), self.getUserId());
        if (levelLogList != null && levelLogList.size() > 0) {
            for (HashMap<String, Object> levelLog : levelLogList) {
                levelLog.put("addCoin", 2000);
            }
            map.put("guLevelLog", levelLogList);
        }
        return map;
    }


    /**
     * 获取亲友圈信息
     */
    public void loadGroupInfo() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("loadGroupInfo|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), 0);
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
                return;
            }

            long groupId = NumberUtils.toLong(params.get("groupId"), 0);
            GroupUser self = groupDaoNew.loadGroupUser(groupId, userId);
            if (self == null) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_5), getRequest(), getResponse(), false);
                return;
            }
            HashMap<String, Object> data = genGroupInfoMap(self);
            if (data == null) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_5), getRequest(), getResponse(), false);
                return;
            }

            JSONObject json = new JSONObject();
            json.put("userId", user.getUserId());
            json.put("name", user.getName());
            json.put("sex", user.getSex());
            json.put("enterServer", user.getEnterServer());
            json.put("headimgurl", user.getHeadimgurl());
            json.put("isOnLine", user.getIsOnLine());
            json.put("data", genGroupInfoMap(self));
            OutputUtil.output(0, json, getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 获取亲友圈成员信息
     */
    public void loadGroupUserList() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("loadGroupUserList|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), 0);
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
                return;
            }
            int pageNo = NumberUtils.toInt(params.get("pageNo"), 1);
            int pageSize = NumberUtils.toInt(params.get("pageSize"), 10);
            if (pageNo < 1) {
                pageNo = 1;
            }
            if (pageSize < 1) {
                pageSize = 10;
            }
            long groupId = NumberUtils.toLong(params.get("groupId"), 0);
            String keyWord = params.get("keyWord");
            if (groupId <= 0 || pageNo <= 0 || pageSize <= 0) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            int optType = NumberUtils.toInt(params.get("optType"), 1); // 查询类型，1：模糊查询，2，userId精确查找
            if (optType == 1 || optType == 2) {
                // --------------1：模糊查询，2，userId精确查找------------------
                Integer userCount = groupDaoNew.countGroupUserList(groupId, keyWord, optType);
                List<HashMap<String, Object>> list;
                if (userCount > 0) {
                    list = groupDaoNew.loadGroupUserList(groupId, pageNo, pageSize, keyWord, optType);
                } else {
                    list = Collections.emptyList();
                }
                JSONObject json = new JSONObject();
                json.put("pageNo", pageNo);
                json.put("pageSize", pageSize);
                json.put("pages", (int) Math.ceil(userCount * 1.0 / pageSize));
                json.put("list", list);
                OutputUtil.output(0, json, getRequest(), getResponse(), false);
            } else {
                OutputUtil.output(7, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }


    /**
     * 获取自己以下所有管理
     */
    public void userListAdmin() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("userListAdmin|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), 0);
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
                return;
            }
            long groupId = NumberUtils.toLong(params.get("groupId"), -1);
            long targetUserId = NumberUtils.toLong(params.get("targetUserId"), -1);
            int optType = NumberUtils.toInt(params.get("optType"), 2); // 查询类型，1：所有成员，2，查看直接下级
            String keyWord = params.get("keyWord");
            if (groupId <= 0) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            //亲友圈玩家列表
            GroupInfo group = groupDaoNew.loadGroupInfo(groupId);
            if (group == null) {
                OutputUtil.output(2, LangMsg.getMsg(LangMsg.code_5), getRequest(), getResponse(), false);
                return;
            }
            GroupUser self = groupDaoNew.loadGroupUser(groupId, userId);
            if (self == null) {
                OutputUtil.output(3, LangMsg.getMsg(LangMsg.code_5), getRequest(), getResponse(), false);
                return;
            }
            if (GroupConstants.isFuHuiZhang(self.getUserRole())) {
                // 副会长查看自己，实际为会长
                self = groupDaoNew.loadGroupMaster(groupId);
                userId = self.getUserId();
            }
            GroupUser target;
            if (targetUserId == -1) {
                targetUserId = userId;
                target = self;
            } else {
                target = groupDaoNew.loadGroupUser(groupId, targetUserId);
            }
            if (GroupConstants.isFuHuiZhang(target.getUserRole())) {
                target = groupDaoNew.loadGroupMaster(groupId);
                targetUserId = target.getUserId();
            }

            if (target == null || GroupConstants.isChengYuan(target.getUserRole())) {
                OutputUtil.output(4, "该成员没有下级成员", getRequest(), getResponse(), false);
                return;
            }

            JSONObject json = new JSONObject();
            List<HashMap<String, Object>> adminList = groupDaoNew.userListAdmin(groupId, target.getPromoterLevel(), targetUserId, optType, keyWord);
            if (adminList == null) {
                adminList = new ArrayList<>();
            }

            json.put("adminList", adminList);
            OutputUtil.output(0, json, getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }


    /**
     * 获取直接下级成员信息
     */
    public void userListNextLevel() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("userListNextLevel|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), 0);
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
                return;
            }
            long groupId = NumberUtils.toInt(params.get("groupId"), -1);
            long targetUserId = NumberUtils.toInt(params.get("targetUserId"), -1);
            String keyWord = params.get("keyWord");
            int pageNo = NumberUtils.toInt(params.get("pageNo"), 1);
            int pageSize = NumberUtils.toInt(params.get("pageSize"), 30);
            if (pageNo < 1) {
                pageNo = 1;
            }
            if (pageSize < 1) {
                pageSize = 10;
            }

            int orderByField = NumberUtils.toInt(params.get("orderByField"), 1); // 1默认，2登录时间
            int orderByType = NumberUtils.toInt(params.get("orderByType"), 3);   // 1

            if (groupId <= 0) {
                OutputUtil.output(2, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            GroupInfo group = groupDaoNew.loadGroupInfo(groupId);
            if (group == null) {
                OutputUtil.output(2, LangMsg.getMsg(LangMsg.code_5), getRequest(), getResponse(), false);
                return;
            }
            GroupUser self = groupDaoNew.loadGroupUser(groupId, userId);
            if (self == null) {
                OutputUtil.output(2, LangMsg.getMsg(LangMsg.code_8), getRequest(), getResponse(), false);
                return;
            }  /*else if (GroupConstants.isForbidden(self)) {        //20210611注释： 只禁创进房间 不禁打开成员列表
                OutputUtil.output(2, "你已被禁止游戏无法打开成员列表，请联系上级管理人员", getRequest(), getResponse(), false);
                return;
            }*/

            JSONObject json = new JSONObject();
            int userCount = 0;
            int onLineCount = 0;
            List<HashMap<String, Object>> userList = new ArrayList<>();
            GroupUser target = null;

            if (GroupConstants.isHuiZhangOrFuHuiZhang(self.getUserRole()) && targetUserId == -1) {
                // 会长或副会长，查看所有
                userCount = group.getCurrentCount();
                onLineCount = groupDaoNew.countOnlineUserListAll(groupId);
                userList = groupDaoNew.userListAll(groupId, orderByField, orderByType, pageNo, pageSize, keyWord);
                if (GroupConstants.isHuiZhang(self.getUserRole())) {
                    target = self;
                    targetUserId = userId;
                } else {
                    target = groupDaoNew.loadGroupMaster(groupId);
                    targetUserId = target.getUserId();
                }
            } else {
                if (targetUserId == -1) {
                    targetUserId = userId;
                }
                target = groupDaoNew.loadGroupUser(groupId, targetUserId);
                userCount = groupDaoNew.countUserListNextLevel(groupId, target.getPromoterLevel(), targetUserId, keyWord);
                if (userCount > 0) {
                    onLineCount = groupDaoNew.countOnlineUserListNextLevel(groupId, target.getPromoterLevel(), targetUserId);
                    userList = groupDaoNew.userListNextLevel(groupId, target.getPromoterLevel(), targetUserId, orderByField, orderByType, pageNo, pageSize, keyWord);
                }
            }

            json.put("userList", userList);
            json.put("total", userCount);
            if (StringUtils.isBlank(keyWord)) {
                json.put("onLineCount", onLineCount);
                json.put("totalUserCount", userCount);
            }
            json.put("pages", (int) Math.ceil(userCount * 1.0 / pageSize));

            OutputUtil.output(0, json, getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 查询所有下级成员
     */
    public void searchUserList() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("searchUserList|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), 0);
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
                return;
            }
            long groupId = NumberUtils.toInt(params.get("groupId"), -1);
            String keyWord = params.get("keyWord");
            int pageNo = NumberUtils.toInt(params.get("pageNo"), 1);
            int pageSize = NumberUtils.toInt(params.get("pageSize"), 30);
            if (pageNo < 1) {
                pageNo = 1;
            }
            if (pageSize < 1) {
                pageSize = 10;
            }
            if (groupId <= 0) {
                OutputUtil.output(2, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            GroupInfo groupInfo = groupDaoNew.loadGroupInfo(groupId);
            if (groupInfo == null) {
                OutputUtil.output(2, LangMsg.getMsg(LangMsg.code_5), getRequest(), getResponse(), false);
                return;
            }
            GroupUser self = groupDaoNew.loadGroupUser(groupId, userId);
            if (self == null) {
                OutputUtil.output(2, LangMsg.getMsg(LangMsg.code_8), getRequest(), getResponse(), false);
                return;
            }
            if (GroupConstants.isFuHuiZhang(self.getUserRole())) {
                self = groupDaoNew.loadGroupMaster(groupId);
            }

            JSONObject json = new JSONObject();
            int userCount = 0;
            List<HashMap<String, Object>> userList = Collections.emptyList();
            if (!StringUtils.isBlank(keyWord)) {
                userCount = groupDaoNew.countUserListForSearch(groupId, self.getPromoterLevel(), self.getUserId(), keyWord);
                if (userCount > 0) {
                    userList = groupDaoNew.userListForSearch(groupId, self.getPromoterLevel(), self.getUserId(), pageNo, pageSize, keyWord);
                }
            }
            json.put("userList", userList);
            json.put("total", userCount);
            json.put("pages", (int) Math.ceil(userCount * 1.0 / pageSize));

            OutputUtil.output(0, json, getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }


    /**
     * 邀请
     */
    public void inviteUser() {
        Map<String, String> params = null;
        long groupId = -1;
        String unLockKey = null;
        try {
            params = UrlParamUtil.getParameters(getRequest());
            LOGGER.debug("inviteUser|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), 0);
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
                return;
            }

            groupId = NumberUtils.toLong(params.get("groupId"), -1);
            long targetUserId = NumberUtils.toLong(params.get("targetUserId"), -1);
            GroupInfo group = groupDaoNew.loadGroupInfo(groupId);
            if (group == null) {
                OutputUtil.output(4, LangMsg.getMsg(LangMsg.code_5), getRequest(), getResponse(), false);
                return;
            }
            if (GroupConstants.isGroupForbidden(group)) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_20), getRequest(), getResponse(), false);
                return;
            }

            unLockKey = DbLockUtil.lock(DbLockEnum.GROUP_USER_MODIFY_RELATION, String.valueOf(groupId));
            if (unLockKey == null) {
                OutputUtil.output(-3, LangMsg.getMsg(LangMsg.code_12), getRequest(), getResponse(), false);
                return;
            }
            if (group.getCurrentCount().intValue() >= group.getMaxCount().intValue()) {
                OutputUtil.output(6, "操作失败：亲友圈人员已满", getRequest(), getResponse(), false);
                return;
            }
            GroupUser self = groupDaoNew.loadGroupUser(groupId, userId);
            if (self == null) {
                OutputUtil.output(1, "操作失败：用户[" + userId + "]与亲友圈[" + groupId + "]不匹配", getRequest(), getResponse(), false);
                return;
            } else if (GroupConstants.isChengYuan(self.getUserRole())) {
                OutputUtil.output(5, LangMsg.getMsg(LangMsg.code_8), getRequest(), getResponse(), false);
                return;
            } else if (self.getPromoterLevel() >= 11) {
                OutputUtil.output(5, "操作失败：已经超过10级了，不能拉人了", getRequest(), getResponse(), false);
                return;
            }
            RegInfo targetUser = userDao.getUser(targetUserId);
            if (targetUser == null) {
                OutputUtil.output(-1, "操作失败：用户不存在", getRequest(), getResponse(), false);
                return;
            }
            GroupUser target = groupDaoNew.loadGroupUser(groupId, targetUserId);
            if (target != null) {
                OutputUtil.output(5, "操作失败：该玩家已是群内成员", getRequest(), getResponse(), false);
                return;
            }
            GroupReview groupReview = groupDaoNew.loadTeamInvite(groupId, targetUserId, userId);
            if (groupReview != null) {
                OutputUtil.output(5, "操作失败：已邀请过", getRequest(), getResponse(), false);
                return;
            }

            if(group.getExtMsg().contains(GroupConstants.groupExtKey_forbiddenKickOut)){
                JSONObject json = StringUtils.isBlank(group.getExtMsg()) ? new JSONObject() : JSONObject.parseObject(group.getExtMsg());
                String op = json.getString(GroupConstants.groupExtKey_forbiddenKickOut);
                if(null!=op && op.equals("1")){
                    if (!GroupConstants.isHuiZhangOrFuHuiZhang(self.getUserRole())) {
                        //2020年12月2日  被合伙人踢出的亲友圈玩家 三天内 不能被其他合伙人再拉入亲友圈 、会长副会长可以拉
                        List<HashMap<String, Object>> kol = groupDaoNew.loadGroupUserAlertKickOutLog(groupId,userId,3,targetUserId);
                        if(null!=kol && kol.size()==1){
                            HashMap<String, Object> kolMap = kol.get(0);
                            if(null!=kolMap){
                                long _roleType = Long.valueOf(kolMap.get("userRole").toString());//1：会长， 2：副会长，  10000：合伙人 ，
                                long optUseid = Long.valueOf(kolMap.get("optUserId").toString());

                                String _strDate = kolMap.get("createdTime").toString();
                                Date _createdTime = DateUtil.stringToDate(_strDate,"yyyy-MM-dd HH:mm:ss");
                                Calendar a = Calendar.getInstance();
                                a.setTime(_createdTime);
                                a.add(Calendar.DAY_OF_MONTH,3);
                                Date delayTime =a.getTime();
                                Date _nowDate = new Date();
                                String str = DateUtil.dateToString(delayTime,"yyyy-MM-dd HH:mm:ss");
                                if((_roleType ==1 || _roleType ==2) && _nowDate.before(delayTime) && optUseid!=userId){
                                    //会长副会长踢出去 合伙人不能回群
                                    OutputUtil.output(6, "玩家"+str+"前暂时无法邀请", getRequest(), getResponse(), false);
                                    return;
                                }

                                if(_roleType==10000 && _nowDate.before(delayTime) &&  optUseid!=userId){
                                    OutputUtil.output(6, "玩家"+str+"前暂时无法邀请", getRequest(), getResponse(), false);
                                    return;
                                };
                            }
                        }
                    }

                }
            }


            if (group.getSwitchInvite() == 1) { // 亲友圈拉人需要对方同意
                groupReview = new GroupReview();
                groupReview.setCreatedTime(new Date());
                groupReview.setCurrentState(0);
                groupReview.setGroupId(groupId);
                groupReview.setGroupName(self.getGroupName());
                groupReview.setReviewMode(3);
                groupReview.setUserId(targetUserId);
                groupReview.setUserName(self.getUserName());
                groupReview.setCurrentOperator(userId);
                if (groupDao.createGroupReview(groupReview) <= 0) {
                    OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1002, "操作失败，请稍后再试"), getRequest(), getResponse(), null, false);
                    return;
                }
            } else {  // 亲友圈拉人不需要对方同意
                GroupUser optInviter = self;
                if (GroupConstants.isFuHuiZhang(self.getUserRole())) {
                    self = groupDaoNew.loadGroupMaster(groupId);
                }
                GroupUserLog userLog = groupDaoNew.loadGroupUserLog(groupId, targetUserId);
                GroupUser newer = GroupUtil.genGroupUser(self, targetUser, userLog);
                long groupUserId = groupDaoNew.createGroupUser(newer);
                if (groupUserId <= 0) {
                    OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1002, "操作失败，请稍后再试"), getRequest(), getResponse(), null, false);
                    return;
                }

                // 拒绝亲友圈内其他邀请
                groupDaoNew.rejectTeamInvite(groupId, userId);

                // 更新亲友圈人数
                groupDao.updateGroupInfoCount(1, groupId);

                OutputUtil.output(0, "加入亲友圈成功", getRequest(), getResponse(), false);

                if (userLog != null) {
                    groupDaoNew.deleteGroupUserLog(userLog.getKeyId());
                }

                insertGroupUserAlert(newer.getGroupId(), newer.getUserId(), optInviter.getUserId(), GroupConstants.TYPE_USER_ALERT_INVITE);
            }
            OutputUtil.output(0, LangMsg.getMsg(LangMsg.code_0), getRequest(), getResponse(), false);
            LOGGER.info("inviteUser|succ|params:{}", params);
        } catch (Exception e) {
            LOGGER.error("inviteUser|error|" + JSON.toJSONString(params), e.getMessage(), e);
            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        } finally {
            if (unLockKey != null) {
                DbLockUtil.unLock(DbLockEnum.GROUP_USER_MODIFY_RELATION, String.valueOf(groupId), unLockKey);
            }
        }
    }

    /**
     * 回应邀请
     */
    public void responseInvite() {
        Map<String, String> params = null;
        long groupId = -1;
        String unLockKey = null;
        try {
            params = UrlParamUtil.getParameters(getRequest());
            LOGGER.debug("responseInvite|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), 0);
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
                return;
            }

            long keyId = NumberUtils.toLong(params.get("keyId"), -1);
            int respType = NumberUtils.toInt(params.get("respType"), -1);

            RegInfo targetUser = userDao.getUser(userId);
            if (targetUser == null) {
                OutputUtil.output(-1, "操作失败：用户不存在", getRequest(), getResponse(), false);
                return;
            }
            GroupReview groupReview = groupDao.loadGroupReviewByKeyId(keyId);
            if (groupReview == null) {
                OutputUtil.output(3, "操作失败：消息ID错误", getRequest(), getResponse(), false);
                return;
            } else if (groupReview.getCurrentState().intValue() > 0) {
                OutputUtil.output(4, "操作失败：消息已处理", getRequest(), getResponse(), false);
                return;
            }
            groupId = groupReview.getGroupId();
            GroupInfo groupInfo = groupDaoNew.loadGroupInfo(groupId);
            if (GroupConstants.isGroupForbidden(groupInfo)) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_20), getRequest(), getResponse(), false);
                return;
            }

            unLockKey = DbLockUtil.lock(DbLockEnum.GROUP_USER_MODIFY_RELATION, String.valueOf(groupId));
            if (unLockKey == null) {
                OutputUtil.output(-3, LangMsg.getMsg(LangMsg.code_12), getRequest(), getResponse(), false);
                return;
            }
            GroupUser inviter = groupDaoNew.loadGroupUserForceMaster(groupId, groupReview.getCurrentOperator());
            if (inviter == null) {
                OutputUtil.output(4, "操作失败：消息已过期", getRequest(), getResponse(), false);
                HashMap<String, Object> map = new HashMap<>();
                map.put("keyId", String.valueOf(keyId));
                map.put("currentState", "2");
                map.put("operateTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                map.put("currentOperator", userId);
                groupDao.updateGroupReviewByKeyId(map);
                OutputUtil.output(0, "拒绝加入亲友圈成功", getRequest(), getResponse(), false);
                return;
            }
            if (respType != 1) {
                // 拒绝
                HashMap<String, Object> map = new HashMap<>();
                map.put("keyId", String.valueOf(keyId));
                map.put("currentState", "2");
                map.put("operateTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                map.put("currentOperator", userId);
                groupDao.updateGroupReviewByKeyId(map);
                OutputUtil.output(0, "拒绝加入亲友圈成功", getRequest(), getResponse(), false);
                return;
            }

            boolean needReturn = false;
            if (groupInfo == null) {
                OutputUtil.output(4, LangMsg.getMsg(LangMsg.code_5), getRequest(), getResponse(), false);
                needReturn = true;
            }
            if (groupInfo.getCurrentCount().intValue() >= groupInfo.getMaxCount().intValue()) {
                OutputUtil.output(6, "操作失败：亲友圈人员已满", getRequest(), getResponse(), false);
                needReturn = true;
            }
            int groupCount = groupDao.loadGroupCount(groupId);
            if (groupCount >= 20) {
                OutputUtil.output(3, "操作失败：最多只能创建或加入20个亲友圈！", getRequest(), getResponse(), false);
                needReturn = true;
            }
            GroupUser newer = groupDaoNew.loadGroupUser(groupId, userId);
            if (newer != null) {
                OutputUtil.output(3, "操作失败：已加入该亲友圈", getRequest(), getResponse(), false);
                needReturn = true;
            }
            HashMap<String, Object> map = new HashMap<>();
            map.put("keyId", String.valueOf(keyId));
            map.put("currentState", "1");
            map.put("operateTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            map.put("currentOperator", userId);
            groupDao.updateGroupReviewByKeyId(map);
            if (needReturn) {
                return;
            }

            if (inviter == null) {
                map = new HashMap<>();
                map.put("keyId", String.valueOf(keyId));
                map.put("currentState", "2");
                map.put("operateTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                map.put("currentOperator", userId);
                groupDao.updateGroupReviewByKeyId(map);
                OutputUtil.output(0, "加入失败", getRequest(), getResponse(), false);
                return;
            }
            GroupUser optInviter = inviter;
            if (GroupConstants.isFuHuiZhang(inviter.getUserRole()) || GroupConstants.isChengYuan(inviter.getUserRole())) {
                // 副会长邀请进来的算群主的
                // 邀请人已变为普通成员后，也算给群主
                inviter = groupDaoNew.loadGroupMaster(inviter.getGroupId());
            }
            GroupUserLog userLog = groupDaoNew.loadGroupUserLog(inviter.getGroupId(), groupReview.getUserId());

            newer = GroupUtil.genGroupUser(inviter, targetUser, userLog);

            long groupUserId = groupDaoNew.createGroupUser(newer);
            if (groupUserId <= 0) {
                OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1002, "操作失败，请稍后再试"), getRequest(), getResponse(), null, false);
                return;
            }


            // 拒绝亲友圈内其他邀请
            groupDaoNew.rejectTeamInvite(groupId, userId);

            // 更新亲友圈人数
            groupDao.updateGroupInfoCount(1, groupId);

            OutputUtil.output(0, "加入亲友圈成功", getRequest(), getResponse(), false);

            if (userLog != null) {
                groupDaoNew.deleteGroupUserLog(userLog.getKeyId());
            }

            insertGroupUserAlert(newer.getGroupId(), newer.getUserId(), optInviter.getUserId(), GroupConstants.TYPE_USER_ALERT_INVITE);
            LOGGER.info("responseInvite|succ|params:{}", params);
        } catch (Exception e) {
            LOGGER.error("responseInvite|error|" + JSON.toJSONString(params), e.getMessage(), e);
            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        } finally {
            if (unLockKey != null) {
                DbLockUtil.unLock(DbLockEnum.GROUP_USER_MODIFY_RELATION, String.valueOf(groupId), unLockKey);
            }
        }
    }


    /**
     * 申请加入
     * 发送申请
     */
    public void applyJoinGroup() {
        try {

            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("applyJoinGroup|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), 0);
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
                return;
            }
            int groupCount = groupDao.loadGroupCount(userId);
            if (groupCount >= 20) {
                OutputUtil.output(1, "最多只能创建或加入20个俱乐部！", getRequest(), getResponse(), false);
                return;
            }
            long groupId = NumberUtils.toLong(params.get("groupId"), -1);
            GroupUser groupUser = groupDao.loadGroupUser(userId, groupId);
            if (groupUser != null) {
                OutputUtil.output(2, "已加入该亲友圈", getRequest(), getResponse(), false);
                return;
            }

            GroupInfo group = groupDaoNew.loadGroupInfo(groupId);
            if (group == null) {
                OutputUtil.output(3, LangMsg.getMsg(LangMsg.code_5), getRequest(), getResponse(), false);
                return;
            } else if (group.getGroupMode().intValue() != 0) {
                OutputUtil.output(4, "亲友圈不接受申请", getRequest(), getResponse(), false);
                return;
            } else if (group.getCurrentCount().intValue() >= group.getMaxCount().intValue()) {
                OutputUtil.output(5, "亲友圈人员已满", getRequest(), getResponse(), false);
                return;
            }

            GroupReview groupReview = groupDao.loadGroupReview0(groupId, userId, 1);
            if (groupReview != null && groupReview.getReviewMode() == 1 && groupReview.getCurrentOperator() == null) {
                OutputUtil.output(6, "已申请", getRequest(), getResponse(), false);
                return;
            }
            groupReview = new GroupReview();
            groupReview.setCreatedTime(new Date());
            groupReview.setCurrentState(0);
            groupReview.setGroupId(groupId);
            groupReview.setGroupName(group.getGroupName());
            groupReview.setReviewMode(1);
            groupReview.setUserId(userId);
            groupReview.setUserName(user.getName());
            if (groupDao.createGroupReview(groupReview) <= 0) {
                OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1002, "操作失败，请稍后再试"), getRequest(), getResponse(), null, false);
                return;
            }

            OutputUtil.output(0, "申请成功", getRequest(), getResponse(), false);
            LOGGER.info("applyJoinGroup|succ|", JSON.toJSONString(groupReview));
            refreshStateToManager(groupId);
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    private void refreshStateToManager(long groupId) {
        try {
            //通知游戏服刷新
            List<Map<String, Object>> managerList = groupDaoNew.loadGroupManagers(groupId);
            if (managerList == null || managerList.size() == 0) {
                return;
            }
            for (Map<String, Object> manager : managerList) {
                GameUtil.refreshState((int) manager.get("enterServer"), (long) manager.get("userId"), 1, "");
            }
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 应答玩家申请
     */
    public void responseApply() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("responseApply|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), -1);
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
                return;
            }

            long keyId = NumberUtils.toLong(params.get("keyId"), -1);
            String value = params.get("value");
            GroupReview groupReview = groupDao.loadGroupReviewByKeyId(keyId);
            if (groupReview == null) {
                OutputUtil.output(1, "消息ID错误", getRequest(), getResponse(), false);
                return;
            } else if (groupReview.getCurrentState().intValue() > 0) {
                OutputUtil.output(2, "消息已处理", getRequest(), getResponse(), false);
                return;
            } else if (groupReview.getReviewMode().intValue() != 1) {
                OutputUtil.output(3, "type错误", getRequest(), getResponse(), false);
                return;
            }

            int groupCount = groupDao.loadGroupCount(groupReview.getUserId());
            if (groupCount >= 20) {
                OutputUtil.output(4, "最多只能创建或加入20个俱乐部！", getRequest(), getResponse(), false);
                return;
            }

            GroupUser self = groupDaoNew.loadGroupUser(groupReview.getGroupId(), userId);
            if (self == null) {
                OutputUtil.output(5, "userId错误", getRequest(), getResponse(), false);
                return;
            } else if (!GroupConstants.isHuiZhangOrFuHuiZhang(self.getUserRole())) {
                OutputUtil.output(6, "权限不够：仅限会长和副会长", getRequest(), getResponse(), false);
                return;
            }
            if (!"1".equals(value)) {
                // 拒绝加入
                HashMap<String, Object> map = new HashMap<>();
                map.put("keyId", String.valueOf(keyId));
                map.put("currentState", "2");
                map.put("operateTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                map.put("currentOperator", self.getUserId());
                groupDao.updateGroupReviewByKeyId(map);
                OutputUtil.output(0, "拒绝加入军团成功", getRequest(), getResponse(), false);
                LOGGER.info("responseApply|succ|" + value + "|" + JSON.toJSONString(groupReview));
                return;
            }

            // 同意
            GroupUser target = groupDaoNew.loadGroupUser(groupReview.getGroupId(), groupReview.getUserId());
            boolean needReturn = false;
            if (target != null) {
                OutputUtil.output(7, "已加入军团", getRequest(), getResponse(), false);
                needReturn = true;
            }
            GroupInfo groupInfo = groupDaoNew.loadGroupInfo(groupReview.getGroupId());
            if (groupInfo == null) {
                OutputUtil.output(8, LangMsg.getMsg(LangMsg.code_5), getRequest(), getResponse(), false);
                needReturn = true;
            } else if (groupInfo.getCurrentCount().intValue() >= groupInfo.getMaxCount().intValue()) {
                OutputUtil.output(9, "军团人员已满", getRequest(), getResponse(), false);
                needReturn = true;
            }

            // 修改申请记录
            HashMap<String, Object> map = new HashMap<>();
            map.put("keyId", String.valueOf(keyId));
            map.put("currentState", "1");
            map.put("operateTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            map.put("currentState", "1");
            map.put("currentOperator", self.getUserId());
            groupDao.updateGroupReviewByKeyId(map);
            if (needReturn) {
                return;
            }
            GroupUser inviter = self;
            GroupUser optInviter = inviter;
            if (GroupConstants.isFuHuiZhang(inviter.getUserRole())) {
                // 副会长邀请进来的算群主的
                inviter = groupDaoNew.loadGroupMaster(self.getGroupId());
            }
            // 创建亲友圈成员

            RegInfo targetUser = userDao.getUser(groupReview.getUserId());


            GroupUserLog userLog = groupDaoNew.loadGroupUserLog(inviter.getGroupId(), groupReview.getUserId());

            GroupUser newer = new GroupUser();
            newer.setCreatedTime(new Date());
            newer.setGroupId(inviter.getGroupId());
            newer.setGroupName(groupReview.getGroupName());
            newer.setInviterId(groupReview.getCurrentOperator());
            newer.setPlayCount1(0);
            newer.setPlayCount2(0);
            newer.setUserId(groupReview.getUserId());
            newer.setUserLevel(1);
            newer.setUserRole(GroupConstants.USER_ROLE_ChengYuan);
            newer.setUserName(targetUser.getName());
            newer.setUserNickname(targetUser.getName());
            newer.setCredit(userLog != null ? userLog.getCredit() : 0L);
            newer.setInviterId(inviter.getUserId());
            newer.setPromoterId(inviter.getUserId());
            newer.setPromoterLevel(inviter.getPromoterLevel() + 1);
            newer.setPromoterId1(inviter.getPromoterId1());
            newer.setPromoterId2(inviter.getPromoterId2());
            if (groupDaoNew.createGroupUser(newer) <= 0) {
                OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(10, "操作失败，请稍后再试"), getRequest(), getResponse(), null, false);
                return;
            }
            groupDao.updateGroupInfoCount(1, groupInfo.getGroupId());

            OutputUtil.output(0, "加入军团成功", getRequest(), getResponse(), false);
            LOGGER.info("responseApply|succ|params:{}" + params);

            if (userLog != null) {
                groupDaoNew.deleteGroupUserLog(userLog.getKeyId());
            }

            // 写入变动日志
            insertGroupUserAlert(newer.getGroupId(), newer.getUserId(), optInviter.getUserId(), GroupConstants.TYPE_USER_ALERT_APPLY);

            //通知游戏服刷新\
            GameUtil.refreshState(targetUser.getEnterServer(), groupReview.getUserId(), 1, "");
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }


    /**
     * 设置权限
     * 修改权限
     */
    public void updateUserRole() {
        Map<String, String> params = null;
        try {
            params = UrlParamUtil.getParameters(getRequest());
            LOGGER.debug("updateUserRole|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), 0);
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
                return;
            }

            long groupId = NumberUtils.toLong(params.get("groupId"), -1);
            long targetUserId = NumberUtils.toLong(params.get("targetUserId"), -1);
            int userRole = NumberUtils.toInt(params.get("userRole"), -1);

            GroupInfo groupInfo = groupDaoNew.loadGroupInfo(groupId);
            if (GroupConstants.isGroupForbidden(groupInfo)) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_20), getRequest(), getResponse(), false);
                return;
            }
            if (!GroupConstants.isValidUserRole(userRole)) {
                OutputUtil.output(1, "操作失败：权限不合法" + userRole, getRequest(), getResponse(), false);
                return;
            }
            GroupUser self = groupDaoNew.loadGroupUser(groupId, userId);
            if (self == null) {
                OutputUtil.output(2, "操作失败：用户[" + userId + "]与亲友圈[" + groupId + "]不匹配", getRequest(), getResponse(), false);
                return;
            } else if (GroupConstants.isChengYuan(self.getUserRole())) {
                OutputUtil.output(8, LangMsg.getMsg(LangMsg.code_8), getRequest(), getResponse(), false);
                return;
            } else if (GroupConstants.isFuHuiZhang(userRole) && !GroupConstants.isHuiZhang(self.getUserRole())) {
                OutputUtil.output(8, LangMsg.getMsg(LangMsg.code_8), getRequest(), getResponse(), false);
                return;
            }
            GroupUser target = groupDaoNew.loadGroupUser(groupId, targetUserId);
            if (target == null) {
                OutputUtil.output(4, "操作失败：非群内成员", getRequest(), getResponse(), false);
                return;
            } else if (target.getPromoterLevel() == 11 && !GroupConstants.isChengYuan(userRole)) {
                OutputUtil.output(5, "操作失败：已经10级了", getRequest(), getResponse(), false);
                return;
            } else if (self.getPromoterLevel() > target.getPromoterLevel()) {
                OutputUtil.output(8, LangMsg.getMsg(LangMsg.code_8), getRequest(), getResponse(), false);
                return;
            } else if (!GroupConstants.isHuiZhangOrFuHuiZhang(self.getUserRole()) && !GroupConstants.isUnder(self, target)) {
                OutputUtil.output(8, LangMsg.getMsg(LangMsg.code_8), getRequest(), getResponse(), false);
                return;
            } else if (!GroupConstants.isFuHuiZhang(target.getUserRole()) && !GroupConstants.isChengYuan(target.getUserRole())) {
                OutputUtil.output(8, "操作失败：不允许操作", getRequest(), getResponse(), false);
                return;
            }

            HashMap<String, Object> map = new HashMap<>();
            map.put("keyId", target.getKeyId());
            map.put("userRole", userRole);

            if (GroupConstants.isFuHuiZhang(userRole)) {
                GroupUser master = groupDaoNew.loadGroupMaster(groupId);
                map.put("promoterLevel", 2);
                map.put("promoterId", master.getUserId());
                map.put("promoterId1", master.getUserId());
                map.put("promoterId2", 0);
                map.put("promoterId3", 0);
                map.put("promoterId4", 0);
                map.put("promoterId5", 0);
                map.put("promoterId6", 0);
                map.put("promoterId7", 0);
                map.put("promoterId8", 0);
                map.put("promoterId9", 0);
                map.put("promoterId10", 0);
            } else if (!GroupConstants.isFuHuiZhang(userRole) && !GroupConstants.isChengYuan(userRole)) {
                map.put("promoterId" + target.getPromoterLevel(), target.getUserId());
            } else if (GroupConstants.isChengYuan(userRole)) {
                map.put("promoterId" + target.getPromoterLevel(), 0);
            }
            groupDaoNew.updateGroupUserByKeyId(map);
            OutputUtil.output(0, LangMsg.getMsg(LangMsg.code_0), getRequest(), getResponse(), false);
            LOGGER.info("updateUserRole|succ|params:{}" + params);
        } catch (Exception e) {
            LOGGER.error("updateUserRole|error|" + JSON.toJSONString(params), e.getMessage(), e);
            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        }
    }

    /**
     * 删除成员
     */
    public void deleteUser() {
        Map<String, String> params = null;
        long groupId = -1;
        String unLockKey = null;
        try {
            params = UrlParamUtil.getParameters(getRequest());
            LOGGER.debug("deleteUser|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), 0);
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
                return;
            }
            groupId = NumberUtils.toLong(params.get("groupId"), -1);
            long targetUserId = NumberUtils.toLong(params.get("targetUserId"), -1);

            GroupInfo groupInfo = groupDaoNew.loadGroupInfo(groupId);
            if (GroupConstants.isGroupForbidden(groupInfo)) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_20), getRequest(), getResponse(), false);
                return;
            }

            unLockKey = DbLockUtil.lock(DbLockEnum.GROUP_USER_MODIFY_RELATION, String.valueOf(groupId));
            if (unLockKey == null) {
                OutputUtil.output(-3, LangMsg.getMsg(LangMsg.code_12), getRequest(), getResponse(), false);
                return;
            }
            GroupUser self = groupDaoNew.loadGroupUser(groupId, userId);
            if (self == null) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_7), getRequest(), getResponse(), false);
                return;
            }
            GroupUser target = groupDaoNew.loadGroupUser(groupId, targetUserId);
            if (target == null) {
                OutputUtil.output(5, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            if (!GroupConstants.isHuiZhangOrFuHuiZhang(self.getUserRole())) {
                Object value = GroupConstants.getGroupExt(groupInfo.getExtMsg(), GroupConstants.groupExtKey_masterDelete);
                if (value != null && "1".equals(value.toString())) {
                    OutputUtil.output(5, LangMsg.getMsg(LangMsg.code_8), getRequest(), getResponse(), false);
                    return;
                }
            }
            if (!GroupConstants.canDelete(self, target)) {
                OutputUtil.output(5, LangMsg.getMsg(LangMsg.code_8), getRequest(), getResponse(), false);
                return;
            }

            int delCount = 0;
            if (GroupConstants.isHuiZhang(target.getUserRole())) {
                OutputUtil.output(5, LangMsg.getMsg(LangMsg.code_8), getRequest(), getResponse(), false);
                return;
            } else if (GroupConstants.isChengYuan(target.getUserRole()) || GroupConstants.isFuHuiZhang(target.getUserRole())) {
                RegInfo targetUser = userDao.getUser(targetUserId);
                if (targetUser == null) {
                    OutputUtil.output(6, "用户不存在：" + targetUserId, getRequest(), getResponse(), false);
                    return;
                }
                if (target.getCredit() != 0 || target.getTempCredit() != 0) {
                    OutputUtil.output(5, LangMsg.getMsg(LangMsg.code_21), getRequest(), getResponse(), false);
                    return;
                }
                if (targetUser.getPlayingTableId() > 0 && !groupDaoNew.isGroupTableOver(groupId, targetUser.getPlayingTableId())) {
                    OutputUtil.output(6, LangMsg.getMsg(LangMsg.code_13), getRequest(), getResponse(), false);
                    return;
                }
                if (target.getCreditPurse() > 0 && target.getPromoterId() > 0){
                    //转移零钱包到群主的积分上
                    GroupUser groupMaster = groupDaoNew.loadGroupMaster(self.getGroupId());
                    long destId = groupMaster.getUserId();
                    RegInfo destRegInfo = this.userDao.getUser(destId);
                    boolean isDestInGame = false;
                    if (destRegInfo.getPlayingTableId() > 0 && !groupDaoNew.isGroupTableOver(groupId, destRegInfo.getPlayingTableId())) {
                        isDestInGame = true;
                    }
                    int transferResult = 0;
                    if (isDestInGame) {
                        transferResult = groupDao.transferGroupUserPurseToTempCredit(targetUserId, destId, self.getGroupId(), target.getCreditPurse().intValue());
                    } else {
                        transferResult = groupDao.transferGroupUserPurseToCredit(targetUserId, destId, self.getGroupId(), target.getCreditPurse().intValue());
                    }

                    if (transferResult == 2) {
                        // 写入日志
                        HashMap<String, Object> logFrom = new HashMap<>();
                        logFrom.put("groupId", self.getGroupId());
                        logFrom.put("optUserId", targetUserId);
                        logFrom.put("userId", destId);
                        logFrom.put("tableId", 0);
                        logFrom.put("credit", target.getCreditPurse());
                        logFrom.put("type", 1);
                        logFrom.put("flag", 1);
                        logFrom.put("userGroup", 0);
                        logFrom.put("mode", 0);
                        groupDaoNew.insertGroupCreditLog(logFrom);

                        // 写入日志
                        HashMap<String, Object> logDest = new HashMap<>();
                        logDest.put("groupId", self.getGroupId());
                        logDest.put("optUserId", destId);
                        logDest.put("userId", targetUserId);
                        logDest.put("tableId", 0);
                        logDest.put("credit", -Math.abs(target.getCreditPurse()));
                        logDest.put("type", 1);
                        logDest.put("flag", 1);
                        logDest.put("userGroup", 0);
                        logDest.put("mode", 1);
                        groupDaoNew.insertGroupCreditLog(logDest);

                        //单独记录上下分
                        groupDaoNew.insertGroupCreditLogTransfer(logFrom);
                        groupDaoNew.insertGroupCreditLogTransfer(logDest);

                        if (destRegInfo.getEnterServer() > 0 && destRegInfo.getIsOnLine() > 0) {
                            GameUtil.sendCreditUpdate(destRegInfo.getEnterServer(), destId, groupId);
                        }
                    }
                }

                //普通成员或副会长直接踢
                delCount = groupDao.deleteGroupUserByKeyId(target.getKeyId());

                OutputUtil.output(0, LangMsg.getMsg(LangMsg.code_0), getRequest(), getResponse(), false);

                // 写入日志
                insertGroupUserAlert(target.getGroupId(), target.getUserId(), self.getUserId(), GroupConstants.TYPE_USER_ALERT_DELETE);

                if (target.getCredit() != 0) {
                    // 记录用户的信用分
                    GroupUserLog userLog = new GroupUserLog();
                    userLog.setGroupId(groupId);
                    userLog.setUserId(target.getUserId());
                    userLog.setCredit(target.getCredit());
                    groupDaoNew.insertGroupUserLog(userLog);
                }
                if (targetUser != null && targetUser.getGoldRoomGroupId() == groupId) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("goldRoomGroupId", 0);
                    userDao.updateUser(user.getUserId(), map);

                    if (targetUser.getEnterServer() > 0 && targetUser.getIsOnLine() == 1) {
                        GameUtil.sendGoldRoomGroupId(targetUser.getEnterServer(), userId);
                    }
                }

            } else {
                long sumCredit = groupDaoNew.sumCredit(groupId, target.getPromoterLevel(), target.getUserId());
                if (sumCredit != 0) {
                    OutputUtil.output(5, LangMsg.getMsg(LangMsg.code_21), getRequest(), getResponse(), false);
                    return;
                }
                if (!groupDaoNew.isAllUserTableOver(groupId, target.getUserId(), target.getPromoterLevel())) {
                    OutputUtil.output(6, LangMsg.getMsg(LangMsg.code_13), getRequest(), getResponse(), false);
                    return;
                }
                List<GroupUser> list = groupDaoNew.loadGroupUsers(groupId, target.getUserId(), target.getPromoterLevel());
                delCount = groupDaoNew.deleteUserAndLower(groupId, target.getPromoterLevel(), target.getUserId());
                OutputUtil.output(0, LangMsg.getMsg(LangMsg.code_0), getRequest(), getResponse(), false);

                if (list != null && list.size() > 0) {
                    for (GroupUser del : list) {
                        insertGroupUserAlert(del.getGroupId(), del.getUserId(), self.getUserId(), GroupConstants.TYPE_USER_ALERT_DELETE);
                        RegInfo delUser = userDao.getUser(del.getUserId());
                        if(delUser != null && delUser.getGoldRoomGroupId() == groupId){
                            Map<String, Object> map = new HashMap<>();
                            map.put("goldRoomGroupId", 0);
                            userDao.updateUser(user.getUserId(), map);

                            if (delUser.getEnterServer() > 0 && delUser.getIsOnLine() == 1) {
                                GameUtil.sendGoldRoomGroupId(delUser.getEnterServer(), userId);
                            }
                        }
                    }
                }
            }

            //更新群人数量
            groupDao.updateGroupInfoCount(-delCount, groupId);
            LOGGER.info("deleteUser|succ|params:{}" + params);
        } catch (Exception e) {
            LOGGER.error("deletePromoter|error|" + JSON.toJSONString(params), e.getMessage(), e);
            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        } finally {
            if (unLockKey != null) {
                DbLockUtil.unLock(DbLockEnum.GROUP_USER_MODIFY_RELATION, String.valueOf(groupId), unLockKey);
            }
        }
    }


    /**
     * 比赛分管理
     * 小组、拉手组
     */
    public void teamList() {
        Map<String, String> params = null;
        try {
            params = UrlParamUtil.getParameters(getRequest());
            LOGGER.debug("teamList|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), 0);
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
                return;
            }
            int groupId = NumberUtils.toInt(params.get("groupId"), -1);
            int pageNo = NumberUtils.toInt(params.get("pageNo"), 1);
            int pageSize = NumberUtils.toInt(params.get("pageSize"), 30);

            String keyWord = params.get("keyWord");

            pageNo = pageNo < 0 ? 1 : pageNo;
            pageSize = pageSize < 0 ? 5 : pageSize > 30 ? 30 : pageSize;

            GroupUser groupUser = groupDaoNew.loadGroupUser(groupId, userId);
            if (groupUser == null) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            GroupInfo groupInfo = groupDaoNew.loadGroupInfo(groupId);
            if (groupInfo == null) {
                OutputUtil.output(4, LangMsg.getMsg(LangMsg.code_5), getRequest(), getResponse(), false);
                return;
            }
            GroupUser target = groupUser;
            long targetUserId = NumberUtils.toLong(params.get("targetUserId"), 0);
            if (targetUserId > 0) {
                target = groupDaoNew.loadGroupUser(groupId, targetUserId);
                if (target == null) {
                    OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                    return;
                }
            } else {
                targetUserId = userId;
                target = groupUser;
            }

            if (GroupConstants.isFuHuiZhang(target.getUserRole())) {
                target = groupDaoNew.loadGroupMaster(groupId);
                targetUserId = target.getUserId();
            }

            JSONObject json = new JSONObject();
            json.put("pageNo", pageNo);
            json.put("pageSize", pageSize);
            int teamCount = groupDaoNew.countTeamList(groupId, target.getPromoterLevel(), target.getUserId(), keyWord);
            if (StringUtils.isBlank(keyWord)) {
                teamCount++;
            }
            List<Map<String, Object>> teamList = new ArrayList<>();
            if (teamCount > 0 && target.getPromoterLevel() != 10) {
                teamList = groupDaoNew.teamList(groupId, target.getPromoterLevel(), target.getUserId(), keyWord, pageNo, pageSize);
            }
            boolean userXipaiSet = false;
            JSONObject extJson = StringUtils.isBlank(groupUser.getExt()) ? new JSONObject() : JSONObject.parseObject(groupUser.getExt());
            if (GroupConstants.isHuiZhangOrFuHuiZhang(groupUser.getUserRole()) ) {
                userXipaiSet = true;
            } else if(extJson.getIntValue(GroupConstants.extKey_xipaiConfig) == 1){
                userXipaiSet = true;
            }
            if (teamList != null && teamList.size() > 0) {
                for (Map<String, Object> team : teamList) {
                    team.put("canSet", 1); // 是否可设置赠送分成
                    if (team.get("userId").toString().equals(target.getUserId().toString())) {
                        team.put("canSet", 0);
                    }
                    team.put("xipaiSet", 0);//是否可设置洗牌分
                    if(userXipaiSet && Integer.parseInt(team.get("promoterLevel").toString()) == groupUser.getPromoterLevel() + 1 ){    //直属下级
                        team.put("xipaiSet", 1);
                    }
                }
            }
            json.put("myCredit", groupUser.getCredit());
            json.put("myRate", groupUser.getCreditCommissionRate());
            json.put("teamList", teamList);
            json.put("viewTeamUser", 1);
            json.put("creditRate", groupInfo.getCreditRate());
            json.put("total", teamCount);
            json.put("pages", (int) Math.ceil(teamCount * 1.0 / pageSize));
            json.put("creditAllotMode", groupInfo.getCreditAllotMode());
            json.put("totalCredit", groupDaoNew.sumCredit(groupId, target.getPromoterLevel(), target.getUserId()) + ((GroupConstants.isHuiZhang(target.getUserRole()) && targetUserId == userId) ? groupDaoNew.loadCreditWheelPool(groupId) : 0));
            json.put("totalCommissionCredit", groupDaoNew.sumCommissionCredit(groupId, userId));
            OutputUtil.output(0, json, getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("teamList|error|" + JSON.toJSONString(params), e.getMessage(), e);
            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        }
    }

    /**
     * 比赛分管理
     * 附属成员信息
     */
    public void teamUserList() {
        Map<String, String> params = null;
        try {
            params = UrlParamUtil.getParameters(getRequest());
            LOGGER.debug("teamUserList|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), 0);
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
                return;
            }

            long groupId = NumberUtils.toLong(params.get("groupId"), -1);
            int pageNo = NumberUtils.toInt(params.get("pageNo"), 1);
            int pageSize = NumberUtils.toInt(params.get("pageSize"), 30);
            int orderByField = NumberUtils.toInt(params.get("orderByField"), 1);
            int orderByType = NumberUtils.toInt(params.get("orderByType"), 3);

            String keyWord = params.get("keyWord");

            pageNo = pageNo < 0 ? 1 : pageNo;
            pageSize = pageSize < 1 ? 5 : pageSize > 30 ? 30 : pageSize;

            GroupUser groupUser = groupDao.loadGroupUser(userId, groupId);
            if (groupUser == null) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            GroupInfo groupInfo = groupDaoNew.loadGroupInfo(groupId);
            if (groupInfo == null) {
                OutputUtil.output(4, LangMsg.getMsg(LangMsg.code_5), getRequest(), getResponse(), false);
                return;
            }
            GroupUser targetGroupUser = groupUser;
            long targetUserId = NumberUtils.toLong(params.get("targetUserId"), -1);
            int viewTeamList = 1;
            if (targetUserId > 0) {
                targetGroupUser = groupDao.loadGroupUser(targetUserId, groupId);
                // 查看下级的组信息
                if (targetGroupUser != null) {
                    viewTeamList = 0;
                }
            }
            if (targetUserId == -1) {
                targetUserId = userId;
            }
            GroupUser target = groupDaoNew.loadGroupUser(groupId, targetUserId);

            if (GroupConstants.isFuHuiZhang(target.getUserRole())) {
                target = groupDaoNew.loadGroupMaster(groupId);
                targetUserId = target.getUserId();
            }

            int userCount = groupDaoNew.countUserListNextLevel(groupId, target.getPromoterLevel(), targetUserId, keyWord);
            List<HashMap<String, Object>> userList = null;
            if (userCount > 0) {
                userList = groupDaoNew.teamUserList(groupId, target.getPromoterLevel(), targetUserId, orderByField, orderByType, pageNo, pageSize, keyWord);
            }
            if (userList != null && userList.size() > 0) {
                for (Map<String, Object> userData : userList) {
                    userData.put("opType", GroupConstants.getOpType(groupUser, userData));
                }
            } else {
                userList = Collections.emptyList();
            }

            JSONObject json = new JSONObject();
            json.put("pageNo", pageNo);
            json.put("pageSize", pageSize);
            json.put("userList", userList);
            json.put("myCredit", groupUser.getCredit());
            json.put("viewTeamList", viewTeamList);
            json.put("total", userCount);
            json.put("pages", (int) Math.ceil(userCount * 1.0 / pageSize));
            json.put("creditAllotMode", groupInfo.getCreditAllotMode());
            json.put("totalCredit", groupDaoNew.sumCredit(groupId, target.getPromoterLevel(), target.getUserId()) + (GroupConstants.isHuiZhang(target.getUserRole()) && userId == targetUserId ? groupDaoNew.loadCreditWheelPool(groupId) : 0));
            json.put("totalCommissionCredit", groupDaoNew.sumCommissionCredit(groupId, userId));
            OutputUtil.output(0, json, getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("teamUserList|error|" + JSON.toJSONString(params), e.getMessage(), e);
            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        }
    }


    /**
     * 修改、上、下信用分
     */
    public void updateCredit() {
        Map<String, String> params = null;
        try {
            params = UrlParamUtil.getParameters(getRequest());
            LOGGER.debug("updateCredit|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), -1);
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
                return;
            }
            long destUserId = NumberUtils.toLong(params.get("destUserId"), -1);
            long groupId = NumberUtils.toLong(params.get("groupId"), -1);

            GroupInfo group = groupDaoNew.loadGroupInfo(groupId);
            if (GroupConstants.isGroupForbidden(group)) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_20), getRequest(), getResponse(), false);
                return;
            }
            if (userId == -1 || groupId == -1 || destUserId == -1) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }

            // 操作者
            GroupUser self = groupDaoNew.loadGroupUser(groupId, userId);
            if (self == null) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            } else if (user.getPlayingTableId() > 0 && !groupDaoNew.isGroupTableOver(groupId, user.getPlayingTableId())) {
                OutputUtil.output(6, LangMsg.getMsg(LangMsg.code_19), getRequest(), getResponse(), false);
                return;
            } else if (self.getCreditLock() == 1) {
                // 20191015不限制
//                OutputUtil.output(6, "你的比赛分已上锁，请先解锁", getRequest(), getResponse(), false);
//                return;
            }
            int credit = NumberUtils.toInt(params.get("credit"), 0);
            // 被操作的成员
            GroupUser destGroupUser = groupDaoNew.loadGroupUser(groupId, destUserId);
            if (destGroupUser == null) {
                OutputUtil.output(4, LangMsg.getMsg(LangMsg.code_14), getRequest(), getResponse(), false);
                return;
            }

            RegInfo destRegInfo = this.userDao.getUser(destUserId);
            boolean isDestInGame = false;
            if (destRegInfo == null) {
                OutputUtil.output(6, "用户不存在：" + destUserId, getRequest(), getResponse(), false);
                return;
            }/* else if (destRegInfo.getPlayingTableId() > 0 && !groupDaoNew.isGroupTableOver(groupId, destRegInfo.getPlayingTableId())) {
                OutputUtil.output(6, LangMsg.getMsg(LangMsg.code_13), getRequest(), getResponse(), false);
                return;
            }*/ else if (credit > 0 && destRegInfo.getPlayingTableId() > 0 && !groupDaoNew.isGroupTableOver(groupId, destRegInfo.getPlayingTableId())) {
                isDestInGame = true;
            } else if (credit < 0) {
                if (destRegInfo.getPlayingTableId() > 0 && !groupDaoNew.isGroupTableOver(groupId, destRegInfo.getPlayingTableId())) {
                    OutputUtil.output(6, LangMsg.getMsg(LangMsg.code_13), getRequest(), getResponse(), false);
                    return;
                }
                if (!GroupConstants.isHuiZhangOrFuHuiZhang(self.getUserRole())) {
                    // 会长副会长，不受信用分锁限制
                    if (destGroupUser.getCreditLock() == 1) {
                        OutputUtil.output(6, LangMsg.getMsg(LangMsg.code_17), getRequest(), getResponse(), false);
                        return;
                    }

                    // 玩家下线，信用分自动上锁机制
                    JSONObject extJson = StringUtils.isBlank(group.getExtMsg()) ? new JSONObject() : JSONObject.parseObject(group.getExtMsg());
                    int creditLockOffline = 0;
                    Object obj = extJson.get("creditLockOffline");
                    if (obj != null) {
                        creditLockOffline = Integer.parseInt(obj.toString());
                    }
                    if (creditLockOffline == 1 && destRegInfo.getIsOnLine() == 0) {
                        int timeMinute = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "groupCreditLockOfflineTime", 1);
                        if (TimeUtil.isBeforeMinute(destRegInfo.getLogoutTime(), timeMinute)) {
                            OutputUtil.output(8, LangMsg.getMsg(LangMsg.code_18), getRequest(), getResponse(), false);
                            return;
                        }
                    }
                }
            }

            if (group.getSwitchCoin() == 0 && GroupConstants.isHuiZhang(self.getUserRole()) && userId == destUserId) {

                //只有群主可以对自己信用分进行操作
                if (credit < 0 && destGroupUser.getCredit() < Math.abs(credit)) {
                    OutputUtil.output(7, LangMsg.getMsg(LangMsg.code_16), getRequest(), getResponse(), false);
                    return;
                }
                int updateResult = updateCredit(self, destGroupUser, credit);
                if (updateResult == 1) {
                    if (destRegInfo.getEnterServer() > 0) {
                        GameUtil.sendCreditUpdate(destRegInfo.getEnterServer(), userId, groupId);
                    }
                }

                // 记录群内总的分数变动
                JSONObject extJson = StringUtils.isBlank(group.getExtMsg()) ? new JSONObject() : JSONObject.parseObject(group.getExtMsg());
                extJson.put("totalCredit", groupDaoNew.sumCredit(groupId, 1, self.getUserId()));
                HashMap<String, Object> map = new HashMap<>();
                map.put("keyId", group.getKeyId().toString());
                map.put("extMsg", extJson.toString());
                groupDao.updateGroupInfoByKeyId(map);

                LOGGER.info("updateCredit|1|" + groupId + "|" + updateResult + "|" + userId + "|" + destUserId + "|" + credit);
            } else {
                //信用分转移
                if (userId == destUserId) {
                    OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                    return;
                }
                int opType = GroupConstants.getOpType(self, destGroupUser);
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
                    } else if (credit > 0 && self.getCredit() < credit) {
                        OutputUtil.output(7, LangMsg.getMsg(LangMsg.code_15), getRequest(), getResponse(), false);
                        return;
                    }
                }

                //信用分从fromId转移到destId
                long fromId = self.getUserId();
                long destId = destGroupUser.getUserId();
                if (credit < 0) {
                    //当信用分为负数时，表示信用分从destGroupUser转移到groupUser
                    fromId = destGroupUser.getUserId();
                    destId = self.getUserId();
                }

                int transferResult = 0;
                boolean isMasterReduce = false; // 是否是群主在给成员下分
                if (group.getSwitchCoin() == 1 && GroupConstants.isHuiZhang(self.getUserRole()) && credit < 0) {
                    transferResult = groupDaoNew.reduceGroupUserCreditForMaster(destUserId, self.getGroupId(), Math.abs(credit));
                    isMasterReduce = true;
                } else {
                    if (isDestInGame) {
                        transferResult = groupDao.transferGroupUserTempCredit(fromId, destId, self.getGroupId(), Math.abs(credit));
                    } else {
                        transferResult = groupDao.transferGroupUserCredit(fromId, destId, self.getGroupId(), Math.abs(credit));
                    }
                }
                if (isMasterReduce) {
                    // 群主在给成员下分
                    if (transferResult == 1) {
                        // 写入日志
                        HashMap<String, Object> creditLog = new HashMap<>();
                        creditLog.put("groupId", self.getGroupId());
                        creditLog.put("optUserId", self.getUserId());
                        creditLog.put("userId", destGroupUser.getUserId());
                        creditLog.put("tableId", 0);
                        creditLog.put("credit", credit);
                        creditLog.put("type", 1);
                        creditLog.put("flag", 1);
                        creditLog.put("userGroup", destGroupUser.getUserGroup());
                        creditLog.put("mode", 1);
                        groupDaoNew.insertGroupCreditLog(creditLog);

                        //单独记录上下分
                        groupDaoNew.insertGroupCreditLogTransfer(creditLog);

                        // 记录群主对别人的下分记录
                        groupDaoNew.insertGroupCreditLogMaster(creditLog);

                        // 增加俱乐部经验
                        addGroupExp(group, self, destGroupUser, credit);
                    }
                } else {
                    if (transferResult == 2) {
                        // 写入日志
                        HashMap<String, Object> logFrom = new HashMap<>();
                        logFrom.put("groupId", self.getGroupId());
                        logFrom.put("optUserId", fromId);
                        logFrom.put("userId", destId);
                        logFrom.put("tableId", 0);
                        logFrom.put("credit", Math.abs(credit));
                        logFrom.put("type", 1);
                        logFrom.put("flag", 1);
                        logFrom.put("userGroup", destGroupUser.getUserGroup());
                        logFrom.put("mode", fromId == userId ? 1 : 0);
                        groupDaoNew.insertGroupCreditLog(logFrom);

                        // 写入日志
                        HashMap<String, Object> logDest = new HashMap<>();
                        logDest.put("groupId", self.getGroupId());
                        logDest.put("optUserId", destId);
                        logDest.put("userId", fromId);
                        logDest.put("tableId", 0);
                        logDest.put("credit", -Math.abs(credit));
                        logDest.put("type", 1);
                        logDest.put("flag", 1);
                        logDest.put("userGroup", destGroupUser.getUserGroup());
                        logDest.put("mode", fromId == destUserId ? 1 : 0);
                        groupDaoNew.insertGroupCreditLog(logDest);

                        //单独记录上下分
                        groupDaoNew.insertGroupCreditLogTransfer(logFrom);
                        groupDaoNew.insertGroupCreditLogTransfer(logDest);

                        if (user.getEnterServer() > 0) {
                            GameUtil.sendCreditUpdate(user.getEnterServer(), userId, groupId);
                        }
                        if (destRegInfo.getEnterServer() > 0 && destRegInfo.getIsOnLine() > 0) {
                            GameUtil.sendCreditUpdate(destRegInfo.getEnterServer(), destUserId, groupId);
                        }
                    } else {
                        OutputUtil.output(7, LangMsg.getMsg(LangMsg.code_16), getRequest(), getResponse(), false);
                        return;
                    }
                }
                LOGGER.info("updateCredit|2|" + groupId + "|" + transferResult + "|" + self.getUserId() + "|" + destGroupUser.getUserId() + "|" + credit);
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
                OutputUtil.output(0, "操作失败", getRequest(), getResponse(), false);
            }
            if (updateResult == 1) {
                // 写入日志
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
                groupDaoNew.insertGroupCreditLog(creditLog);

                // 记录群主对自己信用分的操作记录
                if (groupUser.getUserId().equals(destGroupUser.getUserId())) {
                    groupDaoNew.insertGroupCreditLogMaster(creditLog);
                }

                //单独记录上下分
                groupDaoNew.insertGroupCreditLogTransfer(creditLog);
            }
        } catch (Exception e) {
            LOGGER.error("updateCredit|error|" + updateResult + "|" + groupUser.getGroupId() + "|" + groupUser.getUserId() + "|" + destGroupUser.getUserId() + "|" + credit + "|", e);
        }
        return updateResult;
    }

    /**
     * 零钱包操作
     */
    public void opCreditPurse() {
        long userId = 0;
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, "签名验证失败", getRequest(), getResponse(), false);
                return;
            }
            String mUserId = params.get("mUserId");
            String type = params.get("msgType");
            int groupId = Integer.parseInt(params.get("groupId"));
            if (!CommonUtil.isPureNumber(mUserId)) {
                OutputUtil.output(-1, "ID错误", getRequest(), getResponse(), false);
                return;
            }
            userId = Long.parseLong(mUserId);
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
                return;
            }


            if ("getInfo".equals(type)) {
                GroupUser groupUser = groupDao.loadGroupUser(Long.parseLong(mUserId), groupId);
                if (groupUser != null) {
                    if (GroupConstants.isFuHuiZhang(groupUser.getUserRole())) {
                        //管理员相当于查看群主的
                        groupUser = groupDaoNew.loadGroupMaster(groupId);
                        if (groupUser == null) {
                            OutputUtil.output(-1, "权限不足", getRequest(), getResponse(), false);
                            return;
                        }
                    }
                    Map<String, Object> map = new HashMap<>(8);
                    map.put("groupId", String.valueOf(groupId));
                    map.put("userId", groupUser.getUserId());
                    String startDate = params.get("startDate");
                    String endDate = params.get("endDate");
                    if(startDate != null && endDate != null) {
                        map.put("startDate", TimeUtil.parseDate(startDate, "00:00:00"));
                        map.put("endDate", TimeUtil.parseDate(endDate, "23:59:59"));
                    }
//                    long totalRangeCredit = this.groupDaoNew.loadGroupCreditCommission(map);
                    map.put("startDate", TimeUtil.getStartOfDay(-1));
                    map.put("endDate", TimeUtil.getEndOfDay(-1));
                    long yesterdayCredit = this.groupDaoNew.loadGroupCreditCommission(map);
                    Map<String, Object> result = new HashMap<>();
                    result.put("creditPurse", groupUser.getCreditPurse());
                    result.put("creditYesterday", yesterdayCredit);
//                    result.put("totalRangeCredit", totalRangeCredit);// 某时间段内的返佣积分
//                    result.put("creditTotal", groupUser.getCreditTotal());
                    OutputUtil.output(0, result, getRequest(), getResponse(), false);
                }
                else {
                    OutputUtil.output(-1, "成员不存在", getRequest(), getResponse(), false);
                }
            }
            else if ("draw".equals(type)) {
                //提取积分
                String strCreditPurse = params.get("creditPurse");
                if (!StringUtils.isNumeric(strCreditPurse)) {
                    OutputUtil.output(-1, "请输入正数", getRequest(), getResponse(), false);
                    return;
                }
                int creditPurse = Integer.parseInt(strCreditPurse);
                if (creditPurse <= 0) {
                    OutputUtil.output(-1, "请输入正数", getRequest(), getResponse(), false);
                    return;
                }

                RegInfo regInfo = this.userDao.getUser(userId);
                if (regInfo == null) {
                    OutputUtil.output(6, "玩家不存在", getRequest(), getResponse(), false);
                    return;
                }


                    GroupUser groupUser = groupDao.loadGroupUser(userId, groupId);
                    if (groupUser != null) {
                        if (GroupConstants.isFuHuiZhang(groupUser.getUserRole())) {
                            OutputUtil.output(-1, "权限不足", getRequest(), getResponse(), false);
                            return;
                        }

                        if (creditPurse > 0 && groupUser.getCreditPurse() >= creditPurse) {
                            HashMap<String,Object> map = new HashMap<>();
                            map.put("keyId",groupUser.getKeyId());
                            map.put("credit", creditPurse);
                            map.put("creditPurse", -creditPurse);
                            int ret = groupDaoNew.updateGroupUserCreditPurse(map);
                            if (ret == 1) {
                                HashMap<String, Object> creditLog = new HashMap<>();
                                creditLog.put("groupId", groupUser.getGroupId());
                                creditLog.put("optUserId", groupUser.getUserId());
                                creditLog.put("userId", groupUser.getUserId());
                                creditLog.put("tableId", 0);
                                creditLog.put("credit", creditPurse);
                                creditLog.put("type", GroupConstants.CREDIT_LOG_TYPE_PURSE);
                                creditLog.put("flag", 1);
                                creditLog.put("userGroup", groupUser.getUserGroup());
                                creditLog.put("mode", 1);
                                groupDaoNew.insertGroupCreditLog(creditLog);
                                if (regInfo.getEnterServer() > 0) {
                                    GameUtil.sendCreditUpdate(regInfo.getEnterServer(), userId, groupId);
                                }
                                OutputUtil.output(0, (groupUser.getCreditPurse()-creditPurse), getRequest(), getResponse(), false);
                            } else {
                                OutputUtil.output(-1, "积分不够", getRequest(), getResponse(), false);
                            }
                        }else {
                            OutputUtil.output(-1, "积分不够", getRequest(), getResponse(), false);
                        }
                    }
                    else {
                        OutputUtil.output(-1, "成员不存在", getRequest(), getResponse(), false);
                    }
            }
            else if ("save".equals(type)) {
                //存积分
                String strCreditPurse = params.get("creditPurse");
                if (!StringUtils.isNumeric(strCreditPurse)) {
                    OutputUtil.output(-1, "请输入正数", getRequest(), getResponse(), false);
                    return;
                }
                int creditPurse = Integer.parseInt(strCreditPurse);
                if (creditPurse <= 0) {
                    OutputUtil.output(-1, "请输入正数", getRequest(), getResponse(), false);
                    return;
                }

                RegInfo regInfo = this.userDao.getUser(userId);
                if (regInfo == null) {
                    OutputUtil.output(6, "玩家不存在", getRequest(), getResponse(), false);
                    return;
                }

                GroupUser groupUser = groupDao.loadGroupUser(userId, groupId);
                if (groupUser == null) {
                    OutputUtil.output(-1, "成员不存在", getRequest(), getResponse(), false);
                    return;
                }

                if (GroupConstants.isFuHuiZhang(groupUser.getUserRole())) {
                    OutputUtil.output(-1, "权限不足", getRequest(), getResponse(), false);
                    return;
                }

                if (groupUser.getCredit() < creditPurse) {
                    OutputUtil.output(-1, "积分不够", getRequest(), getResponse(), false);
                    return;
                }

                HashMap<String,Object> map = new HashMap<>();
                map.put("keyId",groupUser.getKeyId());
                map.put("credit", -creditPurse);
                map.put("creditPurse", creditPurse);
                int ret = groupDaoNew.updateGroupUserCreditPurse(map);
                if (ret == 1) {
                    HashMap<String, Object> creditLog = new HashMap<>();
                    creditLog.put("groupId", groupUser.getGroupId());
                    creditLog.put("optUserId", groupUser.getUserId());
                    creditLog.put("userId", groupUser.getUserId());
                    creditLog.put("tableId", 0);
                    creditLog.put("credit", -creditPurse);
                    creditLog.put("type", GroupConstants.CREDIT_LOG_TYPE_PURSE);
                    creditLog.put("flag", 1);
                    creditLog.put("userGroup", groupUser.getUserGroup());
                    creditLog.put("mode", 1);
                    groupDaoNew.insertGroupCreditLog(creditLog);
                    if (regInfo.getEnterServer() > 0) {
                        GameUtil.sendCreditUpdate(regInfo.getEnterServer(), userId, groupId);
                    }
                    OutputUtil.output(0, groupUser.getCreditPurse()+creditPurse, getRequest(), getResponse(), false);
                }
                else {
                    OutputUtil.output(-1, "积分不够", getRequest(), getResponse(), false);
                    return;
                }

            }
        }catch(Exception e) {
            OutputUtil.output(4, "系统异常,请稍后再试", getRequest(), getResponse(), false);
            e.printStackTrace();
        }
    }



    /**
     * 战绩查询
     * 列表
     */
    public void loadTablePlayLogs() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("loadTablePlayLogs|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), 0);
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
                return;
            }
            long groupId = NumberUtils.toLong(params.get("groupId"), -1);

            String startDate = params.get("startDate");
            String endDate = params.get("endDate");
            if (!TimeUtil.checkDateFormat(startDate) || !TimeUtil.checkDateFormat(endDate)) {
                OutputUtil.output(3, "日期格式错误：" + startDate + "," + endDate, getRequest(), getResponse(), false);
                return;
            }
            int pageNo = NumberUtils.toInt(params.get("pageNo"), 1);
            int pageSize = NumberUtils.toInt(params.get("pageSize"), 30);
            int isSelf = NumberUtils.toInt(params.get("isSelf"), 1);

            // 默认取当天数据
            GroupUser self = groupDaoNew.loadGroupUser(groupId, userId);
            if (self == null) {
                OutputUtil.output(3, LangMsg.getMsg(LangMsg.code_9), getRequest(), getResponse(), false);
                return;
            }
            long queryUserId = NumberUtils.toLong(params.get("queryUserId"), 0);
            long queryTableId = NumberUtils.toLong(params.get("queryTableId"), 0);
            int playType = NumberUtils.toInt(params.get("playType"), 0);

            String currentState = "'2','4','5'"; // 正常解散和中途解散
            if ("1".equals(params.get("condition"))) {
                // 中途解散
                currentState = "'4','5'";
            }

            long promoterId = 0;
            int promoterLevel = 0;
            if (isSelf == 1) {
                // 只查自己
                queryUserId = userId;
            } else {
                // 查看自己及名下所有的
                if (GroupConstants.isChengYuan(self.getUserRole())) {
                    // 普通成员只查看自己的
                    queryUserId = userId;
                } else if (GroupConstants.isFuHuiZhang(self.getUserRole())) {
                    // 副会长以会长身份查看
                    GroupUser master = groupDaoNew.loadGroupMaster(groupId);
                    promoterId = master.getUserId();
                    promoterLevel = master.getPromoterLevel();
                } else {
                    promoterId = self.getUserId();
                    promoterLevel = self.getPromoterLevel();
                }
            }
            GroupInfo group = groupDaoNew.loadGroupInfo(groupId);
            long count = 0;
            long totalPoint = 0;
            if (group.getIsCredit() == GroupInfo.isCredit_gold) {
                long startDateLong = Long.valueOf(TimeUtil.getDataDate(startDate));
                long endDateLong = Long.valueOf(TimeUtil.getDataDate(endDate));
                HashMap<String, Object> jsData = groupDaoNew.jsFromGroupGoldWinLog(groupId, userId, startDateLong, endDateLong);
                count = Long.valueOf(jsData.get("selfJsCount").toString());
                totalPoint = Long.valueOf(jsData.get("selfWin").toString());
            } else {
                count = groupDaoNew.getUserGroupTableIdCount(groupId, promoterId, promoterLevel, queryUserId, queryTableId, currentState, playType, startDate, endDate);
            }
            List<GroupTable> groupTables = groupDaoNew.getUserPlayLogGroupTable(groupId, promoterId, promoterLevel, queryUserId, queryTableId, currentState, playType, startDate, endDate, pageNo, pageSize);
            JSONObject json = new JSONObject();
            List<Map<String, Object>> results = new ArrayList<>();
            if (groupTables == null || groupTables.isEmpty()) {
                json.put("list", results);
                json.put("tables", count);
                json.put("totalPoint", totalPoint);
                OutputUtil.output(0, json, getRequest(), getResponse(), false);
                return;
            }
            StringBuilder tableNo = new StringBuilder();
            for (GroupTable groupTable : groupTables) {
                tableNo.append(groupTable.getKeyId()).append(",");
            }
            tableNo.deleteCharAt(tableNo.length() - 1);
            List<HashMap<String, Object>> users = groupDao.loadTableUserByTableNo(tableNo.toString(), groupId);
            Map<String, List<Map<String, Object>>> tableUserMap = new HashMap<>();
            List<String> userIdList = new ArrayList<>();
            StringBuilder userIds = new StringBuilder();
            for (Map<String, Object> userData : users) {
                String uId = String.valueOf(userData.get("userId"));
                if (!userIdList.contains(uId)) {
                    userIds.append(userData.get("userId")).append(",");
                }
                String tNo = String.valueOf(userData.get("tableNo"));
                if (tableUserMap.containsKey(tNo)) {
                    tableUserMap.get(tNo).add(userData);
                } else {
                    List<Map<String, Object>> list = new ArrayList<>();
                    list.add(userData);
                    tableUserMap.put(tNo, list);
                }
            }
            userIds.deleteCharAt(userIds.length() - 1);
            List<HashMap<String, Object>> userNameList = userDao.loadUsersByUserId(userIds.toString());//userId userName
            Map<String, String> userNameMap = new HashMap<>();
            for (HashMap<String, Object> tmp : userNameList) {
                userNameMap.put(String.valueOf(tmp.get("userId")), String.valueOf(tmp.get("userName")));
            }

            SimpleDateFormat hfm = new SimpleDateFormat("HH:mm:ss");
            for (GroupTable groupTable : groupTables) {
                Map<String, Object> map = new HashMap<>();
                map.put("overTime", hfm.format(groupTable.getOverTime()));
                map.put("tableId", groupTable.getTableId());
                String tableMsg = groupTable.getTableMsg();

                String playTypeTmp;
                if (StringUtils.isNotBlank(tableMsg)) {
                    if (tableMsg.startsWith("{") && tableMsg.endsWith("}")) {
                        JSONObject jsonObject = JSONObject.parseObject(tableMsg);
                        playTypeTmp = jsonObject.getString("type");
                        if (StringUtils.isBlank(playTypeTmp)) {
                            String ints = jsonObject.getString("ints");
                            playTypeTmp = ints.split(",")[1];
                        }
                    } else {
                        playTypeTmp = tableMsg.split(",")[1];
                    }
                } else {
                    playTypeTmp = "0";
                }
                map.put("playType", playTypeTmp);
                List<Map<String, Object>> list = tableUserMap.get(String.valueOf(groupTable.getKeyId()));
                StringBuilder players = new StringBuilder();
                StringBuilder point = new StringBuilder();
                StringBuilder isWinner = new StringBuilder();
                StringBuilder winLoseCredit = new StringBuilder();
                StringBuilder commissionCredit = new StringBuilder();
                long minKeyId = 0;
                int index = 0, masterNameIndex = 0;
                if (list != null) {
                    for (Map<String, Object> userMap : list) {
                        if (userMap.containsKey("userId")) {
                            String name = userNameMap.get(String.valueOf(userMap.get("userId")));
                            name = name == null ? "" : name.replace(",", "");
                            players.append(name).append(",");
                            point.append(userMap.get("playResult")).append(",");
                            isWinner.append(userMap.get("isWinner")).append(",");
                            winLoseCredit.append(userMap.get("winLoseCredit")).append(",");
                            commissionCredit.append(userMap.get("commissionCredit")).append(",");
                            long keyId = Long.valueOf(String.valueOf(userMap.get("keyId"))).longValue();
                            if (minKeyId == 0 || minKeyId > keyId) {
                                minKeyId = keyId;
                                masterNameIndex = index;
                            }
                            index++;
                        }
                    }
                }
                if (players.length() > 0) {
                    players.deleteCharAt(players.length() - 1);
                    point.deleteCharAt(point.length() - 1);
                    isWinner.deleteCharAt(isWinner.length() - 1);
                    winLoseCredit.deleteCharAt(winLoseCredit.length() - 1);
                    commissionCredit.deleteCharAt(commissionCredit.length() - 1);
                    map.put("players", players.toString());
                    map.put("point", point.toString());
                    map.put("isWinner", isWinner.toString());
                    map.put("masterNameIndex", masterNameIndex);
                    map.put("winLoseCredit", winLoseCredit.toString());
                    map.put("commissionCredit", commissionCredit.toString());

                }
                map.put("playedBureau", groupTable.getPlayedBureau());
                map.put("currentState", groupTable.getCurrentState());
                map.put("tableNo", groupTable.getKeyId());
                map.put("roomName", groupTable.getTableName());
                results.add(map);
            }
            json.put("list", results);
            json.put("tables", count);
            json.put("totalPoint", totalPoint);
            OutputUtil.output(0, json, getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 战绩详情
     */
    public void loadTableRecord() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("loadTableRecord|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), 0);
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
                return;
            }

            String tableNo = params.get("tableNo");
            String isClub = params.get("isClub");
            if (StringUtils.isBlank(tableNo)) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            List<HashMap<String, Object>> list = groupDao.loadTableRecordByTableNo(tableNo);
            if (list != null && list.size() > 0) {
                StringBuilder strBuilder = new StringBuilder();
                String modeMsg = "", resultMsg = "";
                for (HashMap<String, Object> map : list) {
                    strBuilder.append(",\"").append(map.get("logId")).append("\"");
                    if (map.containsKey("recordType") && "1".equals(String.valueOf(map.get("recordType")))) {
                        modeMsg = map.containsKey("modeMsg") ? String.valueOf(map.get("modeMsg")) : "";
                        resultMsg = map.containsKey("resultMsg") ? String.valueOf(map.get("resultMsg")) : "";
                    }
                }
                strBuilder.deleteCharAt(0);
                List list1 = TableLogDao.getInstance().selectUserLogs(strBuilder.toString());

                if (list1 != null && list1.size() > 0) {
                    list1 = Manager.getInstance().buildUserPlayTbaleMsg(1, list1, userId);
                }

                if (!StringUtils.isBlank(isClub) && "1".equals(isClub)) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("code", 0);
                    jsonObject.put("playLog", list1 == null ? "" : list1);
                    jsonObject.put("modeMsg", modeMsg);
                    jsonObject.put("resultMsg", resultMsg);
                    OutputUtil.output(jsonObject, getRequest(), getResponse(), null, false);
                } else {
                    OutputUtil.output(0, list1 == null ? Collections.emptyList() : list1, getRequest(), getResponse(), false);
                }
            } else {
                OutputUtil.output(0, Collections.emptyList(), getRequest(), getResponse(), false);
            }

        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 玩家列表
     * 信用分明细
     * 比赛分明细
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
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
                return;
            }

            long groupId = NumberUtils.toInt(params.get("groupId"), -1);
            GroupInfo group = groupDaoNew.loadGroupInfo(groupId);
            if (group == null) {
                OutputUtil.output(2, LangMsg.getMsg(LangMsg.code_7), getRequest(), getResponse(), false);
                return;
            }

            GroupUser self = groupDao.loadGroupUser(userId, groupId);
            if (self == null) {
                OutputUtil.output(2, LangMsg.getMsg(LangMsg.code_7), getRequest(), getResponse(), false);
                return;
            } else if (GroupConstants.isChengYuan(self.getUserRole())) {
                OutputUtil.output(2, "普通成员，请查看明细", getRequest(), getResponse(), false);
                return;
            }
            if (GroupConstants.isFuHuiZhang(self.getUserRole())) {
                self = groupDaoNew.loadGroupMaster(groupId);
                userId = self.getUserId();
            }
            int creditOrder = NumberUtils.toInt(params.get("creditOrder"), 0); //信用分排序：0或不传值：从大到小 1：从小到大,默认会只查负分玩家
            int pageNo = NumberUtils.toInt(params.get("pageNo"), 1);
            int pageSize = NumberUtils.toInt(params.get("pageSize"), 30);

            String keyWord = params.get("keyWord");

            pageNo = pageNo < 0 ? 1 : pageNo;
            pageSize = pageSize < 1 ? 5 : pageSize > 30 ? 30 : pageSize;
            List<HashMap<String, Object>> userList = Collections.emptyList();
            JSONObject json = new JSONObject();
            json.put("pageNo", pageNo);
            json.put("pageSize", pageSize);
            int userCount = groupDaoNew.countUserListUnder(groupId, self.getPromoterLevel(), self.getUserId(), keyWord, creditOrder);
            if (userCount > 0) {
                userList = groupDaoNew.userListUnder(groupId, self.getPromoterLevel(), self.getUserId(), pageNo, pageSize, keyWord, creditOrder);
            }
            if (pageNo == 1 && userList != null && userList.size() > 0 && StringUtils.isBlank(keyWord)) {
                // 列表中有自己，则提到最前，没有就新建数，把自己提到最前
                int selfIndex = -1;
                for (int i = 0; i < userList.size(); i++) {
                    Map<String, Object> data = userList.get(i);
                    if (data != null && String.valueOf(userId).equals(String.valueOf(data.get("userId")))) {
                        selfIndex = i;
                        break;
                    }
                }
                HashMap<String, Object> selfData;
                if (selfIndex != -1) {
                    selfData = userList.remove(selfIndex);
                } else {
                    selfData = new HashMap<>();
                    selfData.put("userId", self.getUserId());
                    RegInfo selfInfo = userDao.getUser(userId);
                    if (selfInfo != null) {
                        selfData.put("userName", selfInfo.getName());
                        selfData.put("headimgurl", selfInfo.getHeadimgurl());
                    } else {
                        selfData.put("userName", "");
                        selfData.put("headimgurl", "");
                    }
                    selfData.put("credit", self.getCredit());
                }
                List<HashMap<String, Object>> tmpList = new ArrayList<>();
                tmpList.add(selfData);
                tmpList.addAll(userList);
                userList = tmpList;
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
     * 信用分明细
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
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
                return;
            }

            long groupId = NumberUtils.toInt(params.get("groupId"), -1);
            GroupInfo group = groupDaoNew.loadGroupInfo(groupId);
            if (group == null) {
                OutputUtil.output(2, LangMsg.getMsg(LangMsg.code_7), getRequest(), getResponse(), false);
                return;
            }

            long targetId = NumberUtils.toLong(params.get("targetId"), 0);
            int selectType = NumberUtils.toInt(params.get("selectType"), 0); // 0：所有，1：上下分，2：佣金，3：牌局   4洗牌分  6零钱存取
            int fullQLType = NumberUtils.toInt(params.get("fullQLType"), 0);
            int upOrDown = 0;
            GroupUser self = groupDao.loadGroupUser(userId, groupId);
            if (self == null) {
                OutputUtil.output(2, LangMsg.getMsg(LangMsg.code_7), getRequest(), getResponse(), false);
                return;
            }
            if (GroupConstants.isFuHuiZhang(self.getUserRole())) {
                // 副会长以会长的身份查看
                self = groupDaoNew.loadGroupMaster(groupId);
            }
            GroupUser target = groupDao.loadGroupUser(targetId, groupId);
            if (target == null) {
                OutputUtil.output(2, LangMsg.getMsg(LangMsg.code_14), getRequest(), getResponse(), false);
                return;
            }
            if (targetId != userId && !GroupConstants.isUnder(self, target)) {
                OutputUtil.output(2, LangMsg.getMsg(LangMsg.code_7), getRequest(), getResponse(), false);
                return;
            }
            int pageNo = NumberUtils.toInt(params.get("pageNo"), 1);
            int pageSize = NumberUtils.toInt(params.get("pageSize"), 30);
            pageNo = pageNo < 0 ? 1 : pageNo;
            pageSize = pageSize < 1 ? 5 : pageSize > 30 ? 30 : pageSize;
            String startDate = params.get("startDate"); // 日期格式：2019-08-01 00:00:01
            String endDate = params.get("endDate"); // 日期格式：2019-08-01 00:00:01
            if (!TimeUtil.checkDateFormat(startDate) || !TimeUtil.checkDateFormat(endDate)) {
                OutputUtil.output(3, "日期格式错误：" + startDate + "," + endDate, getRequest(), getResponse(), false);
                return;
            }
            boolean isLookXipai = false;
            JSONObject extJson = StringUtils.isBlank(target.getExt()) ? new JSONObject() : JSONObject.parseObject(target.getExt());
            if (GroupConstants.isHuiZhangOrFuHuiZhang(self.getUserRole()) ) {
                isLookXipai = true;
            } else if(extJson.getIntValue(GroupConstants.extKey_xipaiConfig) == 1){
                isLookXipai = true;
            }

            int dataCount = groupDaoNew.countCreditLogList(groupId, targetId, selectType, startDate, endDate, upOrDown,isLookXipai, fullQLType == 1);
            long sumCredit = 0;
            List<Map<String, Object>> dataList = Collections.emptyList();
            if (dataCount > 0) {
                dataList = groupDaoNew.creditLogList(groupId, targetId, selectType, startDate, endDate, upOrDown, pageNo, pageSize,isLookXipai, fullQLType == 1);
                sumCredit = groupDaoNew.sumCreditLogList(groupId, targetId, selectType, startDate, endDate, upOrDown,isLookXipai, fullQLType == 1);
            }
            JSONObject json = new JSONObject();
            json.put("pageNo", pageNo);
            json.put("pageSize", pageSize);
            json.put("total", dataCount);
            json.put("dataList", dataList);
            json.put("sumCredit", sumCredit);
            json.put("pages", (int) Math.ceil(dataCount * 1.0 / pageSize));
            OutputUtil.output(0, json, getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("creditLogList|error|" + JSON.toJSONString(params), e.getMessage(), e);
            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        }
    }


    /**
     * 抽水值配置列表: 新
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
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
                return;
            }

            long groupId = NumberUtils.toInt(params.get("groupId"), -1);
            long targetUserId = NumberUtils.toLong(params.get("targetUserId"), 0);

            GroupInfo group = groupDaoNew.loadGroupInfo(groupId);
            if (group == null) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            GroupUser self = groupDaoNew.loadGroupUser(groupId, userId);
            if (self == null || GroupConstants.isChengYuan(self.getUserRole())) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            GroupUser target = groupDaoNew.loadGroupUser(groupId, targetUserId);
            if (target == null || GroupConstants.isChengYuan(target.getUserRole()) || GroupConstants.isFuHuiZhang(target.getUserRole())) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            if (!GroupConstants.isHuiZhangOrFuHuiZhang(self.getUserRole())) {
                if (userId != targetUserId) { // 可以自己看自己的
                    if (!GroupConstants.isUnder(self, target)) {
                        OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_7), getRequest(), getResponse(), false);
                        return;
                    }
                }
            }
            JSONObject json = new JSONObject();
            json.put("creditAllotMode", group.getCreditAllotMode());
            json.put("creditRate", group.getCreditRate());
            json.put("dataList", loadCommissionConfig(self, target, group.getCreditRate()));
            OutputUtil.output(0, json, getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("loadCommissionConfig|error|" + JSON.toJSONString(params), e.getMessage(), e);
            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        }
    }

    private List<Map<String, Object>> loadCommissionConfig(GroupUser groupUser, GroupUser target, int creditRate) throws Exception {
        long groupId = groupUser.getGroupId();
        Map<Integer, GroupCommissionConfig> creditConfigMap = new HashMap<>();
        List<GroupCommissionConfig> creditConfigList = groupDaoNew.loadCommissionConfig(groupId, target.getUserId());
        if (creditConfigList != null && !creditConfigList.isEmpty()) {
            for (GroupCommissionConfig c : creditConfigList) {
                creditConfigMap.put(c.getSeq(), c);
            }
        }

        List<GroupCommissionConfig> preCreditConfigList = null;
        Map<Integer, GroupCommissionConfig> preCreditConfigMap = new HashMap<>();
        List<Map<String, Object>> dataList = new ArrayList<>();
        if (!groupUser.getUserId().equals(target.getUserId())) {
            long preUserId = target.getPromoterId();
            if (preUserId > 0) {
                preCreditConfigList = groupDaoNew.loadCommissionConfig(groupId, preUserId);
            }
            if (preCreditConfigList != null && !preCreditConfigList.isEmpty()) {
                for (GroupCommissionConfig c : preCreditConfigList) {
                    preCreditConfigMap.put(c.getSeq(), c);
                }
            }
            int count = GroupConstants.SYS_COMMISSION_LOG_MAP_1.size();
            for (int seq = 1; seq <= count; seq++) {
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
                    data.put("preValue", sysConfig.getMaxCredit() - preConfig.getLeftCredit());   // 上级已使用的分
                    data.put("myMaxValue", preConfig.getLeftCredit());    // 我可设置的最大分
                } else {
                    data.put("preValue", 0);
                    data.put("myMaxValue", sysConfig.getMaxCredit());
                }
                dataList.add(data);
            }
        } else {
            // 自己查自己的，不需要查看上级
            int count = GroupConstants.SYS_COMMISSION_LOG_MAP_1.size();
            for (int seq = 1; seq <= count; seq++) {
                GroupCommissionConfig selfConfig = creditConfigMap.get(seq);
                if (selfConfig == null) {
                    GroupCommissionConfig sysConfig = GroupConstants.getSysCommissionConfig(seq, creditRate);
                    selfConfig = sysConfig;
                }
                Map<String, Object> data = new HashMap<>();
                data.put("minValue", selfConfig.getMinCredit());
                data.put("maxValue", selfConfig.getMaxCredit());
                data.put("preValue", selfConfig.getMaxCredit() - selfConfig.getLeftCredit());
                dataList.add(data);
            }
        }

        return dataList;
    }


    /**
     * 修改抽水值: 新
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
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
                return;
            }

            long targetUserId = NumberUtils.toLong(params.get("targetUserId"), 0);
            long groupId = NumberUtils.toInt(params.get("groupId"), -1);
            int xipai = NumberUtils.toInt(params.get("xipai"), -1);
            String values = params.get("values");
            if (StringUtils.isBlank(values)) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            GroupInfo group = groupDao.loadGroupInfo(groupId, 0);
            if (group == null) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            GroupUser self = groupDao.loadGroupUser(userId, groupId);
            if (self == null || GroupConstants.isChengYuan(self.getUserRole())) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            GroupUser target = groupDao.loadGroupUser(targetUserId, groupId);
            if (target == null || GroupConstants.isChengYuan(target.getUserRole()) || GroupConstants.isFuHuiZhang(target.getUserRole())) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
//            if (xipai > 0 && target.getPromoterLevel() != 2){    //给非一级合伙人设置洗牌分所有权
//                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
//                return;
//            }
            if (!GroupConstants.isHuiZhangOrFuHuiZhang(self.getUserRole())) {
                if (!GroupConstants.isUnder(self, target)) {
                    OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_7), getRequest(), getResponse(), false);
                    return;
                }
            }

            String[] valueArr = values.split(",");
            List<GroupCommissionConfig> selfConfigList = groupDaoNew.loadCommissionConfig(groupId, targetUserId);
            Map<Integer, GroupCommissionConfig> selfConfigMap = new HashMap<>();
            if (selfConfigList != null && !selfConfigList.isEmpty()) {
                for (GroupCommissionConfig c : selfConfigList) {
                    selfConfigMap.put(c.getSeq(), c);
                }
            }

            Map<Integer, GroupCommissionConfig> preConfigMap = new HashMap<>();
            long preUserId = target.getPromoterId();
            if (preUserId != 0) {
                List<GroupCommissionConfig> preConfigList = groupDaoNew.loadCommissionConfig(groupId, preUserId);
                if (preConfigList != null && !preConfigList.isEmpty()) {
                    for (GroupCommissionConfig c : preConfigList) {
                        preConfigMap.put(c.getSeq(), c);
                    }
                }
            }

            Date now = new Date();
            for (int seq = 1; seq <= valueArr.length; seq++) {
                GroupCommissionConfig sysConfig = GroupConstants.getSysCommissionConfig(seq, group.getCreditRate());
                GroupCommissionConfig selfConfig = selfConfigMap.get(seq);
                long credit = Long.valueOf(valueArr[seq - 1]);
                long creditLog = 0;
                long leftCredit = sysConfig.getMaxCredit() - credit;
                if (preConfigMap.containsKey(seq)) {
                    creditLog = preConfigMap.get(seq).getCredit();
                    leftCredit = preConfigMap.get(seq).getLeftCredit() - credit;
                    if (leftCredit < 0) {
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
                    groupDaoNew.insertCommissionConfig(selfConfig);
                } else {
                    if (credit != selfConfig.getCredit()) {
                        groupDaoNew.updateCommissionConfig(groupId, targetUserId, seq, credit, leftCredit, creditLog);
                    }
                }
            }
            JSONObject json = new JSONObject();
            json.put("msg", LangMsg.getMsg(LangMsg.code_0));
            OutputUtil.output(0, json, getRequest(), getResponse(), false);

            //记录ext
            HashMap<String, String> extMap = new HashMap<>();
            extMap.put(GroupConstants.extKey_commissionConfig, "1");
            if(xipai >= 0){
                xipai = xipai > 1 ? 1 :xipai;
                extMap.put(GroupConstants.extKey_xipaiConfig, xipai+"");
            }
            updateGroupUserExt(target, extMap);

            LOGGER.info("updateCommissionConfig|succ|params:{}", params);
        } catch (Exception e) {
            LOGGER.error("updateCommissionConfig|error|" + JSON.toJSONString(params), e.getMessage(), e);
            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        }
    }

    /**
     * 更新玩家的ext信息
     *
     * @param groupUser
     * @param key
     * @param value
     * @throws Exception
     */
    public void updateGroupUserExt(GroupUser groupUser, String key, String value) throws Exception {
        JSONObject extJson = StringUtils.isBlank(groupUser.getExt()) ? new JSONObject() : JSONObject.parseObject(groupUser.getExt());
        HashMap<String, Object> map = new HashMap<>();
        if (!extJson.containsKey(key)) {
            extJson.put(key, value);
            map.put("ext", extJson.toString());
        } else {
            if (!value.equals(extJson.getString(key))) {
                extJson.put(key, value);
                map.put("ext", extJson.toString());
            }
        }
        if (map.size() > 0) {
            map.put("keyId", groupUser.getKeyId());
            groupDao.updateGroupUserByKeyId(map);
        }
    }

    /**
     * 更新玩家的ext信息
     *
     * @param groupUser
     * @param kvMap
     * @throws Exception
     */
    public void updateGroupUserExt(GroupUser groupUser, Map<String,String> kvMap) throws Exception {
        JSONObject extJson = StringUtils.isBlank(groupUser.getExt()) ? new JSONObject() : JSONObject.parseObject(groupUser.getExt());
        HashMap<String, Object> map = new HashMap<>();
        for(Map.Entry entry : kvMap.entrySet()) {
            String key = entry.getKey().toString();
            String value = entry.getValue().toString();
            if (!extJson.containsKey(key)) {
                extJson.put(key, value);
            } else {
                if (!value.equals(extJson.getString(key))) {
                    extJson.put(key, value);
                }
            }
        }
        map.put("ext", extJson.toString());
        if (map.size() > 0) {
            map.put("keyId", groupUser.getKeyId());
            groupDao.updateGroupUserByKeyId(map);
        }
    }


    /**
     * 群统计
     * 赠送统计
     */
    public void commissionLog() {
        Map<String, String> params = null;
        try {
            params = UrlParamUtil.getParameters(getRequest());
            LOGGER.debug("commissionLog|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), -1);
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
                return;
            }
            long groupId = NumberUtils.toLong(params.get("groupId"), -1);
            if (groupId == -1) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            GroupUser self = groupDaoNew.loadGroupUser(groupId, userId);
            if (self == null) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            } else if (GroupConstants.isChengYuan(self.getUserRole())) {
                OutputUtil.output(0, LangMsg.getMsg(LangMsg.code_8), getRequest(), getResponse(), false);
                return;
            }
            if (GroupConstants.isFuHuiZhang(self.getUserRole())) {
                self = groupDaoNew.loadGroupMaster(groupId);
            }
            long dataDate = NumberUtils.toLong(params.get("dataDate"), -1); // 日期格式：20190801
            if (dataDate == -1) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            List<HashMap<String, Object>> dataList = groupDaoNew.commissionLog(groupId, self.getUserId(), self.getPromoterLevel(), dataDate);
            if (dataList != null && dataList.size() > 0) {
                Map<String, String> creditMap = groupDaoNew.creditStatisticsMap(groupId, self.getUserId(), self.getPromoterLevel());
                for (HashMap<String, Object> data : dataList) {

                    String sumCredit = creditMap.get(data.get("promoterIdKey").toString());
                    sumCredit = sumCredit == null ? "0" : sumCredit;
                    data.put("sumCredit", sumCredit);

                    String negativeCredit = creditMap.get("-" + data.get("promoterIdKey").toString());
                    negativeCredit = negativeCredit == null ? "0" : negativeCredit;
                    data.put("sumNegativeCredit", negativeCredit);
                }
            }
            JSONObject json = new JSONObject();
            json.put("dataList", dataList);
            json.put("rootId", self.getUserId());
            OutputUtil.output(0, json, getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("commissionLog|error|" + JSON.toJSONString(params), e.getMessage(), e);
            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        }
    }

    /**
     * 群统计
     * 查看下级
     */
    public void commissionLogNextLevel() {
        Map<String, String> params = null;
        try {
            params = UrlParamUtil.getParameters(getRequest());
            LOGGER.debug("commissionLogNextLevel|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), -1);
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
                return;
            }
            long targetUserId = NumberUtils.toLong(params.get("targetUserId"), -1);
            long queryUserId = NumberUtils.toLong(params.get("queryUserId"), 0);
            long groupId = NumberUtils.toLong(params.get("groupId"), -1);
            if (groupId == -1) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
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

            GroupUser self = groupDaoNew.loadGroupUser(groupId, userId);
            if (self == null) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            } else if (GroupConstants.isChengYuan(self.getUserRole())) {
                OutputUtil.output(0, LangMsg.getMsg(LangMsg.code_8), getRequest(), getResponse(), false);
                return;
            }
            if (GroupConstants.isFuHuiZhang(self.getUserRole())) {
                self = groupDaoNew.loadGroupMaster(groupId);
            }

            long dataDate = NumberUtils.toLong(params.get("dataDate"), -1); // 日期格式：20190801
            if (dataDate == -1) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }

            GroupUser target = groupDaoNew.loadGroupUser(groupId, targetUserId);
            if (target == null) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            } else if (GroupConstants.isChengYuan(target.getUserRole())) {
                OutputUtil.output(0, LangMsg.getMsg(LangMsg.code_8), getRequest(), getResponse(), false);
                return;
            } else if (!GroupConstants.isUnder(self, target)) {
                OutputUtil.output(0, LangMsg.getMsg(LangMsg.code_8), getRequest(), getResponse(), false);
                return;
            }

            if (GroupConstants.isFuHuiZhang(target.getUserRole())) {
                target = groupDaoNew.loadGroupMaster(groupId);
            }

            int count = groupDaoNew.countCommissionLogNextLevel(groupId, target.getUserId(), target.getPromoterLevel(), queryUserId);
            List<HashMap<String, Object>> dataList = Collections.emptyList();
            if (count > 0) {
                dataList = groupDaoNew.commissionLogNextLevel(groupId, target.getUserId(), target.getPromoterLevel(), dataDate, queryUserId, pageNo, pageSize);
            }
            JSONObject json = new JSONObject();
            json.put("pageNo", pageNo);
            json.put("pageSize", pageSize);
            json.put("total", count);
            json.put("pages", (int) Math.ceil(count * 1.0 / pageSize));
            json.put("dataList", dataList);
            OutputUtil.output(0, json, getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("commissionLogNextLevel|error|" + JSON.toJSONString(params), e.getMessage(), e);
            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        }
    }

    /**
     * 排行统计
     * 成员统计
     */
    public void rankList() {
        Map<String, String> params = null;
        try {
            params = UrlParamUtil.getParameters(getRequest());
            LOGGER.debug("rankList|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), -1);
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
                return;
            }
            int rankField = NumberUtils.toInt(params.get("rankField"), 1);
            int rankType = NumberUtils.toInt(params.get("rankType"), 1);
            long groupId = NumberUtils.toLong(params.get("groupId"), -1);
            long targetUserId = NumberUtils.toLong(params.get("targetId"), -1);
            long queryUserId = NumberUtils.toLong(params.get("queryUserId"), -1);
            int optType = NumberUtils.toInt(params.get("optType"), 1); // 查询类型，1：所有成员，2，查看直接下级
            optType = (optType != 1 && optType != 2) ? 1 : optType;
            if (groupId == -1) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
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

            GroupUser self = groupDaoNew.loadGroupUser(groupId, userId);
            if (self == null) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            if (optType == 1) {
                targetUserId = userId;
            }
            if (targetUserId <= 0) {
                targetUserId = userId;
            }

            GroupUser target = groupDaoNew.loadGroupUser(groupId, targetUserId);
            if (target == null) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            if (GroupConstants.isFuHuiZhang(target.getUserRole())) {
                target = groupDaoNew.loadGroupMaster(groupId);
            }
            if (GroupConstants.isChengYuan(self.getUserRole())) {
                OutputUtil.output(0, LangMsg.getMsg(LangMsg.code_8), getRequest(), getResponse(), false);
                return;
            }

            long startDate = NumberUtils.toLong(params.get("startDate"), -1); // 日期格式：20190801
            long endDate = NumberUtils.toLong(params.get("endDate"), -1); // 日期格式：20190801
            if (startDate == -1 || endDate == -1) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                Date now = new Date();
                startDate = Long.valueOf(sdf.format(now));
                endDate = Long.valueOf(sdf.format(now));
            }
            optType = 1;
            int count = groupDaoNew.countRankList(groupId, target.getUserId(), target.getPromoterLevel(), startDate, endDate, queryUserId, optType);
            List<HashMap<String, Object>> dataList = Collections.emptyList();
            HashMap<String, Object> sumData = null;
            if (count > 0) {
                dataList = groupDaoNew.rankList(groupId, target.getUserId(), target.getPromoterLevel(), startDate, endDate, rankField, rankType, queryUserId, optType, pageNo, pageSize);
                if (dataList != null && dataList.size() > 0 && queryUserId == -1) {
                    sumData = groupDaoNew.rankSum(groupId, target.getUserId(), target.getPromoterLevel(), startDate, endDate, optType);
                }
            }
            JSONObject json = new JSONObject();
            json.put("pageNo", pageNo);
            json.put("pageSize", pageSize);
            json.put("total", count);
            json.put("pages", (int) Math.ceil(count * 1.0 / pageSize));
            json.put("dataList", dataList);
            if (sumData != null) {
                json.put("sumData", sumData);
            }
            OutputUtil.output(0, json, getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("rankList|error|" + JSON.toJSONString(params), e.getMessage(), e);
            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        }
    }

    /**
     * 查询拉手
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
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
                return;
            }

            long groupId = NumberUtils.toLong(params.get("groupId"), -1);
            int mode = NumberUtils.toInt(params.get("mode"), 1);
            int orderByField = NumberUtils.toInt(params.get("orderByField"), 1);
            int orderByType = NumberUtils.toInt(params.get("orderByType"), 1);

            GroupUser self = groupDao.loadGroupUser(userId, groupId);
            if (self == null) {
                OutputUtil.output(1, "参数错误：用户[" + userId + "]与俱乐部[" + groupId + "]不匹配", getRequest(), getResponse(), false);
                return;
            }
            JSONObject json = new JSONObject();
            json.put("mode", mode);
            String keyWord = params.get("keyWord");
            int pageNo = NumberUtils.toInt(params.get("pageNo"), 1);
            int pageSize = NumberUtils.toInt(params.get("pageSize"), 30);
            if (pageNo <= 0) {
                pageNo = 1;
            }
            if (pageSize <= 0 || pageSize > 30) {
                pageSize = 30;
            }
            List<HashMap<String, Object>> groupUsers = Collections.emptyList();
            List<HashMap<String, Object>> dataList = Collections.emptyList();
            if (GroupConstants.isFuHuiZhang(self.getUserRole())) {
                self = groupDaoNew.loadGroupMaster(groupId);
            }
            int dataCount = groupDaoNew.countSearchGroupUser(groupId, self.getUserId(), self.getPromoterLevel(), keyWord);
            if (dataCount > 0) {
                groupUsers = groupDaoNew.searchGroupUser(groupId, self.getUserId(), self.getPromoterLevel(), keyWord, pageNo, pageSize);
            }

            if (dataCount > 0 && groupUsers != null && groupUsers.size() > 0) {
                dataList = new ArrayList<>();
                for (HashMap<String, Object> gu : groupUsers) {
                    gu.put("opType", GroupConstants.getOpType(self, gu));
                    dataList.add(gu);
                }
            }
            json.put("pageNo", pageNo);
            json.put("pageSize", pageSize);
            json.put("total", dataCount);
            json.put("pages", (int) Math.ceil(dataCount * 1.0 / pageSize));
            json.put("dataList", dataList);
            OutputUtil.output(0, json, getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("searchUser|error|" + JSON.toJSONString(params), e.getMessage(), e);
            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        }
    }


    /**
     * 更改军团成员信息
     */
    public void updateGroupUser() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("updateGroupUser|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), -1);
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
                return;
            }
            long groupId = NumberUtils.toLong(params.get("groupId"), -1);
            if (groupId <= 0) {
                OutputUtil.output(1, "userId错误", getRequest(), getResponse(), false);
                return;
            }
            GroupUser self = groupDaoNew.loadGroupUser(groupId, userId);
            if (self == null) {
                OutputUtil.output(3, LangMsg.getMsg(LangMsg.code_11), getRequest(), getResponse(), false);
                return;
            }
            int optType = NumberUtils.toInt(params.get("optType"), -1);
            if (optType == 1) { // 禁止或取消禁止游戏
                long targetUserId = NumberUtils.toLong(params.get("targetId"), -1);
                GroupUser target = groupDaoNew.loadGroupUser(groupId, targetUserId);
                if (target == null) {
                    OutputUtil.output(3, LangMsg.getMsg(LangMsg.code_11), getRequest(), getResponse(), false);
                    return;
                }
                if (!GroupConstants.isCanForbid(self, target)) {
                    OutputUtil.output(3, LangMsg.getMsg(LangMsg.code_8), getRequest(), getResponse(), false);
                    return;
                }
                int userState = NumberUtils.toInt(params.get("userState"), 0); // 0禁止游戏，1非禁止游戏
                int stateType = NumberUtils.toInt(params.get("stateType"), 0); // 0对个人操作，1对名下所有人操作
                if (GroupConstants.isChengYuan(target.getUserRole())) {
                    stateType = 0;
                }
                int ret = groupDaoNew.forbidGroupUser(target, userState, stateType);
                LOGGER.info("updateGroupUser|succ|" + optType + "|" + userId + "|" + targetUserId + "|" + userState + "|" + stateType + "|" + ret);
            } else if (optType == 2) {
                // 信用分上锁和解锁
                if (GroupConstants.isHuiZhang(self.getUserRole())) {
                    OutputUtil.output(3, LangMsg.getMsg(LangMsg.code_8), getRequest(), getResponse(), false);
                    return;
                }
                int creditLock = NumberUtils.toInt(params.get("creditLock"), -1);
                if (creditLock != 1) {
                    creditLock = 0;
                }
                if (creditLock == 1 && user.getPlayingTableId() > 0 && !groupDaoNew.isGroupTableOver(groupId, user.getPlayingTableId())) {
                    OutputUtil.output(3, "你在牌桌中无法上锁", getRequest(), getResponse(), false);
                    return;
                }
                HashMap<String, Object> map = new HashMap<>();
                map.put("keyId", self.getKeyId());
                map.put("creditLock", creditLock);
                int ret = groupDaoNew.updateGroupUserByKeyId(map);
                LOGGER.info("updateGroupUser|succ|" + ret + "|" + JSON.toJSONString(map));
            } else if (optType == 3) {
                // 修改头相框
                int frameId = NumberUtils.toInt(params.get("frameId"), 0);
                HashMap<String, Object> map = new HashMap<>();
                map.put("keyId", self.getKeyId());
                map.put("frameId", frameId);
                int ret = groupDaoNew.updateGroupUserByKeyId(map);
                LOGGER.info("updateGroupUser|succ|" + ret + "|" + JSON.toJSONString(map));
            } else {
                OutputUtil.output(4, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            OutputUtil.output(0, LangMsg.getMsg(LangMsg.code_0), getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 更改军团信息
     */
    public void updateGroupInfo() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("updateGroupInfo|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), -1);
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
                return;
            }
            long groupId = NumberUtils.toLong(params.get("groupId"), -1);
            if (groupId <= 0) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            GroupUser self = groupDaoNew.loadGroupUser(groupId, userId);
            if (self == null) {
                OutputUtil.output(3, LangMsg.getMsg(LangMsg.code_11), getRequest(), getResponse(), false);
                return;
            }
            GroupInfo group = groupDaoNew.loadGroupInfo(groupId);
            if (group == null) {
                OutputUtil.output(3, LangMsg.getMsg(LangMsg.code_5), getRequest(), getResponse(), false);
                return;
            }

            int optType = NumberUtils.toInt(params.get("optType"), -1);

            if (optType == 1) {
                // 修改群公告
                if (!GroupConstants.isHuiZhangOrFuHuiZhang(self.getUserRole())) {
                    OutputUtil.output(3, LangMsg.getMsg(LangMsg.code_8), getRequest(), getResponse(), false);
                    return;
                }
                String content = params.get("content");
                if (StringUtils.isNotBlank(content)) {
                    content = content.trim();
                    String content0 = KeyWordsFilter.getInstance().filt(content);
                    if (!content.equals(content0)) {
                        OutputUtil.output(1, "公告内容不能包含敏感字符", getRequest(), getResponse(), false);
                        return;
                    }
                    content = GroupConstants.filterGroupName(content);
                }
                HashMap<String, Object> map = new HashMap<>();
                map.put("keyId", group.getKeyId());
                map.put("content", content);
                int ret = groupDao.updateGroupInfoByKeyId(map);
                LOGGER.info("updateGroupInfo|succ|" + ret + "|" + JSON.toJSONString(map));
            } else if (optType == 2) {
                // 修改群名字
                if (!GroupConstants.isHuiZhangOrFuHuiZhang(self.getUserRole())) {
                    OutputUtil.output(3, LangMsg.getMsg(LangMsg.code_8), getRequest(), getResponse(), false);
                    return;
                }
                String groupName = params.get("groupName");
                String regex = "^[a-z0-9A-Z\u4e00-\u9fa5]+$";
                if (!groupName.matches(regex)) {
                    OutputUtil.output(1, "俱乐部名称仅限字母数字和汉字", getRequest(), getResponse(), false);
                    return;
                }
                groupName = groupName.trim();
                String groupName0 = KeyWordsFilter.getInstance().filt(groupName);
                if (!groupName.equals(groupName0)) {
                    OutputUtil.output(1, "军团名不能包含敏感字符", getRequest(), getResponse(), false);
                    return;
                }
                groupName = GroupConstants.filterGroupName(groupName);
                HashMap<String, Object> map = new HashMap<>();
                map.put("keyId", group.getKeyId());
                map.put("groupName", groupName);
                int ret = groupDao.updateGroupInfoByKeyId(map);
                LOGGER.info("updateGroupInfo|succ|params:{}" + params);
            } else if (optType == 101) {
                // 玩法设置亲友圈背景、桌子样式、桌布

                // ------------- 亲友圈背景 -------------------
                if (params.containsKey(GroupConstants.groupExtKey_backGround)) {
                    List<String> keyList = new ArrayList<>();
                    keyList.add(GroupConstants.groupExtKey_backGround);
                    updateGroupExtMsg(params, self, group, keyList);
                }

                // -------------- 牌桌样式、背景 -----------------
                if (params.containsKey("configs")) {
                    String configsStr = params.get("configs");
                    if (StringUtils.isBlank(configsStr)) {
                        OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                        return;
                    }
                    String[] configs = configsStr.split("[;]");
                    for (String config : configs) {
                        if (StringUtils.isBlank(config)) {
                            continue;
                        }
                        String[] splits = config.split(",");
                        if (splits.length < 3) {
                            continue;
                        }
                        long keyId = Long.valueOf(splits[0]);
                        GroupInfo subGroup = groupDaoNew.loadGroupByKeyId(keyId);
                        if (subGroup == null || subGroup.getParentGroup() != groupId) {
                            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                            return;
                        }

                        Map<String, String> paramMap = new HashMap<>();
                        paramMap.put(GroupConstants.groupExtKey_tableStyle, splits[1]);
                        paramMap.put(GroupConstants.groupExtKey_tableBg, splits[2]);

                        List<String> keyList = new ArrayList<>();
                        keyList.add(GroupConstants.groupExtKey_tableStyle);
                        keyList.add(GroupConstants.groupExtKey_tableBg);

                        updateGroupExtMsg(paramMap, self, subGroup, keyList);
                    }
                }
            } else {
                String key = null;
                switch (optType) {
                    case 3:
                        // 申请房间解散次数
                        key = GroupConstants.groupExtKey_dismissCount;
                        break;
                    case 4:
                        // 是否进房间自动准备
                        key = GroupConstants.groupExtKey_autoReadyOnJoin;
                        break;
                    case 5:
                        // 玩家下线,信用分自动上锁
                        key = GroupConstants.groupExtKey_creditLockOffline;
                        break;
                    case 6:
                        // 群主才可踢人
                        key = GroupConstants.groupExtKey_masterDelete;
                        break;
                    case 7:
                        // 同ip禁止进入
                        key = GroupConstants.groupExtKey_sameIpLimit;
                        break;
                    case 8:
                        // 不打开GPS禁止进入
                        key = GroupConstants.groupExtKey_openGpsLimit;
                        break;
                    case 9:
                        // 距离太近禁止进入
                        key = GroupConstants.groupExtKey_distanceLimit;
                        break;
                    case 10:
                        // 亲友圈背景
                        key = GroupConstants.groupExtKey_backGround;
                        break;
                    case 11:
                        // 牌桌列表：已开局的牌桌数量
                        key = GroupConstants.groupExtKey_tableNum;
                        break;
                    case 12:
                        // 私密房功能开关
                        key = GroupConstants.groupExtKey_privateRoom;
                        break;
                    case 13:
                        // 禁止解散
                        key = GroupConstants.groupExtKey_forbiddenDiss;
                        break;
                    case 14:
                        // 禁止解散
                        key = GroupConstants.groupExtKey_forbiddenKickOut;
                        break;
                    case 15:
                        //幸运转盘活动
                        key = GroupConstants.groupExtKey_creditWheel;
                        break;
                }
                if (null == key) {
                    OutputUtil.output(4, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                    return;
                }
                List<String> keyList = new ArrayList<>();
                keyList.add(key);
                if (!updateGroupExtMsg(params, self, group, keyList)) {
                    return;
                }
            }
            OutputUtil.output(0, LangMsg.getMsg(LangMsg.code_0), getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    private boolean updateGroupExtMsg(Map<String, String> params, GroupUser self, GroupInfo group, List<String> keyList) throws Exception {
        if (!GroupConstants.isHuiZhangOrFuHuiZhang(self.getUserRole())) {
            OutputUtil.output(3, LangMsg.getMsg(LangMsg.code_8), getRequest(), getResponse(), false);
            return false;
        }
        HashMap<String, Object> map = new HashMap<>();
        JSONObject json = StringUtils.isBlank(group.getExtMsg()) ? new JSONObject() : JSONObject.parseObject(group.getExtMsg());
        for (String key : keyList) {
            String value = params.get(key);
            if (StringUtils.isNotBlank(value)) {
                json.put(key, value);
                if (GroupConstants.groupExtKey_distanceLimit.equals(key) && "1".equals(value)) {
                    json.put(GroupConstants.groupExtKey_openGpsLimit, "1");
                } else if (GroupConstants.groupExtKey_openGpsLimit.equals(key) && "0".equals(value)) {
                    json.put(GroupConstants.groupExtKey_distanceLimit, "0");
                }
            }
        }
        map.put("extMsg", json.toString());
        map.put("keyId", group.getKeyId());
        int ret = groupDao.updateGroupInfoByKeyId(map);
        LOGGER.info("updateGroupInfo|succ|params:{}" + params + "|" + ret);
        return true;
    }

    /**
     * 更改群内关系链接
     * optType=1 分配邀请人
     * optType=2 降为成员
     */
    public void modifyRelation() {
        long groupId = -1;
        String unLockKey = null;
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("modifyRelation|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), -1);
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
                return;
            }
            groupId = NumberUtils.toLong(params.get("groupId"), -1);
            if (groupId <= 0) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            GroupInfo groupInfo = groupDaoNew.loadGroupInfo(groupId);
            if (GroupConstants.isGroupForbidden(groupInfo)) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_20), getRequest(), getResponse(), false);
                return;
            }

            unLockKey = DbLockUtil.lock(DbLockEnum.GROUP_USER_MODIFY_RELATION, String.valueOf(groupId));
            if (unLockKey == null) {
                OutputUtil.output(-3, LangMsg.getMsg(LangMsg.code_12), getRequest(), getResponse(), false);
                return;
            }
            GroupUser self = groupDaoNew.loadGroupUser(groupId, userId);
            if (self == null) {
                OutputUtil.output(2, LangMsg.getMsg(LangMsg.code_11), getRequest(), getResponse(), false);
                return;
            }
            int optType = NumberUtils.toInt(params.get("optType"), -1);
            List<HashMap<String, Object>> sqlList = new ArrayList<>();
            if (optType == 1) { // 分配邀请人
                long fromId = NumberUtils.toLong(params.get("fromId"), -1);
                long targetId = NumberUtils.toLong(params.get("targetId"), -1);
                if (userId == fromId) {
                    OutputUtil.output(3, LangMsg.getMsg(LangMsg.code_7), getRequest(), getResponse(), false);
                    return;
                }
                if (!GroupConstants.isHuiZhangOrFuHuiZhang(self.getUserRole())) {
                    OutputUtil.output(4, LangMsg.getMsg(LangMsg.code_8), getRequest(), getResponse(), false);
                    return;
                }
                GroupUser from = groupDaoNew.loadGroupUser(groupId, fromId);
                if (from == null) {
                    OutputUtil.output(5, LangMsg.getMsg(LangMsg.code_22,5), getRequest(), getResponse(), false);
                    return;
                } else if (GroupConstants.isHuiZhangOrFuHuiZhang(from.getUserRole())) {
                    OutputUtil.output(6, LangMsg.getMsg(LangMsg.code_22,6), getRequest(), getResponse(), false);
                    return;
                }
                GroupUser target = groupDaoNew.loadGroupUser(groupId, targetId);
                if (target == null) {
                    OutputUtil.output(7, LangMsg.getMsg(LangMsg.code_22,7), getRequest(), getResponse(), false);
                    return;
                } else if (GroupConstants.isChengYuan(target.getUserRole())) {
                    OutputUtil.output(8, LangMsg.getMsg(LangMsg.code_22,8), getRequest(), getResponse(), false);
                    return;
                } else if (from.getUserId().equals(target.getUserId())) {
                    OutputUtil.output(9, LangMsg.getMsg(LangMsg.code_22,9), getRequest(), getResponse(), false);
                    return;
                } else if (from.getPromoterId().equals(target.getUserId())) {
                    OutputUtil.output(10, LangMsg.getMsg(LangMsg.code_22,10), getRequest(), getResponse(), false);
                    return;
                } else if (GroupConstants.isUnder(from, target)) {
                    OutputUtil.output(11, LangMsg.getMsg(LangMsg.code_22,11), getRequest(), getResponse(), false);
                    return;
                }

                int ret = 0;
                if (GroupConstants.isChengYuan(from.getUserRole())) { // from是普通成员，只需修改from的数据
                    if (target.getPromoterLevel() >= 10) {
                        OutputUtil.output(12, LangMsg.getMsg(LangMsg.code_22,12), getRequest(), getResponse(), false);
                        return;
                    }
                    HashMap<String, Object> map = new HashMap<>(8);
                    map.put("keyId", from.getKeyId());
                    map.put("promoterId", target.getUserId());
                    map.put("promoterLevel", target.getPromoterLevel() + 1);
                    map.put("promoterId1", target.getPromoterId1());
                    map.put("promoterId2", target.getPromoterId2());
                    map.put("promoterId3", target.getPromoterId3());
                    map.put("promoterId4", target.getPromoterId4());
                    map.put("promoterId5", target.getPromoterId5());
                    map.put("promoterId6", target.getPromoterId6());
                    map.put("promoterId7", target.getPromoterId7());
                    map.put("promoterId8", target.getPromoterId8());
                    map.put("promoterId9", target.getPromoterId9());
                    map.put("promoterId10", target.getPromoterId10());
                    ret = groupDaoNew.updateGroupUserByKeyId(map);
                    sqlList.add(map);
                } else { // from是管理人员，还需要修改from的所有下级玩家

                    int toLevel = target.getPromoterLevel();
                    int fromLevel = from.getPromoterLevel();
                    int maxPromoterLevel = groupDaoNew.getMaxPromoterLevel(groupId, fromId, fromLevel);
                    if (toLevel + (maxPromoterLevel - fromLevel) + 1 > 10) {
                        OutputUtil.output(13, LangMsg.getMsg(LangMsg.code_22,13), getRequest(), getResponse(), false);
                        return;
                    }

                    List<GroupUser> list = groupDaoNew.loadGroupUsers(groupId, from.getUserId(), from.getPromoterLevel());
                    GroupUserTree root = GroupConstants.genGroupUserTree(from, list);

                    GroupUserTree pre = new GroupUserTree(target);
                    root.setPre(pre);

                    GroupConstants.genUpdateSqlForModifyRelation(root, sqlList);

                    if (sqlList != null && sqlList.size() > 0) {
                        groupDaoNew.updateGroupUserByKeyIdBatch(sqlList);
                        ret = sqlList.size();
                    }
                }
                LOGGER.info("modifyRelation|succ|" + params.get("sign") + "|" + ret + "|" + JSON.toJSONString(sqlList));
            } else if (optType == 2) { // 降为成员
                long targetId = NumberUtils.toLong(params.get("targetId"), -1);
                if (userId == targetId) {
                    OutputUtil.output(3, LangMsg.getMsg(LangMsg.code_7), getRequest(), getResponse(), false);
                    return;
                }
                GroupUser target = groupDaoNew.loadGroupUser(groupId, targetId);
                if (target == null) {
                    OutputUtil.output(4, LangMsg.getMsg(LangMsg.code_22,4), getRequest(), getResponse(), false);
                    return;
                } else if (GroupConstants.isHuiZhang(target.getUserRole())) {
                    OutputUtil.output(5, LangMsg.getMsg(LangMsg.code_22,5), getRequest(), getResponse(), false);
                    return;
                } else if (GroupConstants.isChengYuan(target.getUserRole())) {
                    OutputUtil.output(6, LangMsg.getMsg(LangMsg.code_22,6), getRequest(), getResponse(), false);
                    return;
                } else if (GroupConstants.isChengYuan(self.getUserRole())) {
                    OutputUtil.output(7, LangMsg.getMsg(LangMsg.code_22,7), getRequest(), getResponse(), false);
                    return;
                }
                int ret = 0;
                if (GroupConstants.isFuHuiZhang(target.getUserRole())) {
                    if (!GroupConstants.isHuiZhangOrFuHuiZhang(self.getUserRole())) {
                        OutputUtil.output(9, LangMsg.getMsg(LangMsg.code_22,8), getRequest(), getResponse(), false);
                        return;
                    }
                    HashMap<String, Object> map = new HashMap<>(8);
                    map.put("keyId", target.getKeyId());
                    map.put("userRole", GroupConstants.USER_ROLE_ChengYuan);
                    ret = groupDaoNew.updateGroupUserByKeyId(map);
                    sqlList.add(map);
                } else {
                    if (!GroupConstants.isHuiZhangOrFuHuiZhang(self.getUserRole()) && !GroupConstants.isZhuGuan(self.getUserRole())) {
                        OutputUtil.output(10, LangMsg.getMsg(LangMsg.code_22,9), getRequest(), getResponse(), false);
                        return;
                    } else if (GroupConstants.isZhuGuan(self.getUserRole())){   //检测被修改玩家是不是自己下面的
                        if(!GroupConstants.isUnder(self,target)){
                            OutputUtil.output(11, LangMsg.getMsg(LangMsg.code_22,10), getRequest(), getResponse(), false);
                            return;
                        }
                    }
                    List<GroupUser> list = groupDaoNew.loadGroupUsers(groupId, target.getUserId(), target.getPromoterLevel());
                    // 自己
                    HashMap<String, Object> rootSql = new HashMap<>();
                    rootSql.put("promoterId" + (target.getPromoterLevel()), 0);
                    rootSql.put("userRole", GroupConstants.USER_ROLE_ChengYuan);
                    rootSql.put("keyId", target.getKeyId());
                    sqlList.add(rootSql);

                    if (list.size() > 1) {
                        GroupUserTree root = GroupConstants.genGroupUserTree(target, list);

                        GroupUser promoter = groupDaoNew.loadGroupUser(groupId, target.getPromoterId());
                        GroupUserTree newRoot = new GroupUserTree(promoter);

                        // 所有下级成员
                        for (GroupUserTree nextTree : root.getNextList()) {
                            // 将所有下级的上级换成自己的上级
                            nextTree.setPre(newRoot);
                            GroupConstants.genUpdateSqlForModifyRelation(nextTree, sqlList);
                        }
                    }

                    if (sqlList != null && sqlList.size() > 0) {
                        groupDaoNew.updateGroupUserByKeyIdBatch(sqlList);
                        ret = sqlList.size();
                    }
                }
                //判断是否有预警分设置
                List<GroupWarn> warnList = groupWarnDao.getGroupWarnByUserIdAndGroupId(target.getUserId(),(int)groupId);
                if(warnList!=null && warnList.size() > 0){
                    groupWarnDao.deleteGroupWarn((int)groupId,target.getUserId());
                }
                //零钱包转到群主身上
                if (GroupConstants.isZhuGuan(target.getUserRole()) && target.getCreditPurse() > 0) {
                    GroupUser groupMaster = groupDaoNew.loadGroupMaster(self.getGroupId());
                    long destId = groupMaster.getUserId();
                    RegInfo destRegInfo = this.userDao.getUser(destId);
                    boolean isDestInGame = false;
                    if (destRegInfo.getPlayingTableId() > 0 && !groupDaoNew.isGroupTableOver(groupId, destRegInfo.getPlayingTableId())) {
                        isDestInGame = true;
                    }
                    int transferResult = 0;
                    if (isDestInGame) {
                        transferResult = groupDao.transferGroupUserPurseToTempCredit(targetId, destId, self.getGroupId(), target.getCreditPurse().intValue());
                    } else {
                        transferResult = groupDao.transferGroupUserPurseToCredit(targetId, destId, self.getGroupId(), target.getCreditPurse().intValue());
                    }

                    if (transferResult == 2) {
                        // 写入日志
                        HashMap<String, Object> logFrom = new HashMap<>();
                        logFrom.put("groupId", self.getGroupId());
                        logFrom.put("optUserId", targetId);
                        logFrom.put("userId", destId);
                        logFrom.put("tableId", 0);
                        logFrom.put("credit", target.getCreditPurse());
                        logFrom.put("type", 1);
                        logFrom.put("flag", 1);
                        logFrom.put("userGroup", 0);
                        logFrom.put("mode", 0);
                        groupDaoNew.insertGroupCreditLog(logFrom);

                        // 写入日志
                        HashMap<String, Object> logDest = new HashMap<>();
                        logDest.put("groupId", self.getGroupId());
                        logDest.put("optUserId", destId);
                        logDest.put("userId", targetId);
                        logDest.put("tableId", 0);
                        logDest.put("credit", -Math.abs(target.getCreditPurse()));
                        logDest.put("type", 1);
                        logDest.put("flag", 1);
                        logDest.put("userGroup", 0);
                        logDest.put("mode", 1);
                        groupDaoNew.insertGroupCreditLog(logDest);

                        //单独记录上下分
                        groupDaoNew.insertGroupCreditLogTransfer(logFrom);
                        groupDaoNew.insertGroupCreditLogTransfer(logDest);

                        if (destRegInfo.getEnterServer() > 0 && destRegInfo.getIsOnLine() > 0) {
                            GameUtil.sendCreditUpdate(destRegInfo.getEnterServer(), destId, groupId);
                        }
                    }
                }

                LOGGER.info("modifyRelation|succ|" + params.get("sign") + "|" + ret + "|" + JSON.toJSONString(sqlList));
            } else {
                OutputUtil.output(4, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            OutputUtil.output(0, LangMsg.getMsg(LangMsg.code_0), getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        } finally {
            if (unLockKey != null) {
                DbLockUtil.unLock(DbLockEnum.GROUP_USER_MODIFY_RELATION, String.valueOf(groupId), unLockKey);
            }
        }
    }

    /**
     * 操作日志
     * 信用分上下分记录
     */
    public void loadCreditLog() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("loadCreditLog|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toInt(params.get("userId"), 0);
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
                return;
            }

            int groupId = NumberUtils.toInt(params.get("groupId"), -1);
            int pageNo = NumberUtils.toInt(params.get("pageNo"), 1);
            int pageSize = NumberUtils.toInt(params.get("pageSize"), 30);
            int isPositive = NumberUtils.toInt(params.get("isPositive"), 0);
            String startDate = params.get("startDate"); // 日期格式：2019-08-01 00:00:01
            String endDate = params.get("endDate"); // 日期格式：2019-08-01 23:59:59
            if (!TimeUtil.checkDateFormat(startDate) || !TimeUtil.checkDateFormat(endDate)) {
                OutputUtil.output(3, "日期格式错误：" + startDate + "," + endDate, getRequest(), getResponse(), false);
                return;
            }
            int selectType = NumberUtils.toInt(params.get("selectType"), 0); //1,通过keyWord查操作人 2,keyWord查被操作人 0都查
            long targetUserId = NumberUtils.toLong(params.get("keyWord"), -1);


            GroupUser self = groupDao.loadGroupUser(userId, groupId);
            if (self == null) {
                OutputUtil.output(3, LangMsg.getMsg(LangMsg.code_9), getRequest(), getResponse(), false);
                return;
            }

            Map<String, Object> map = new HashMap<>(8);
            map.put("groupId", groupId);
            map.put("type", 1);
            map.put("startNo", (pageNo - 1) * pageSize);
            map.put("pageSize", pageSize);
            if (isPositive > 0) {
                map.put("isPositive", 1);
            } else {
                map.put("isNegative", 1);
            }
            map.put("selectType", selectType);
            map.put("startDate", startDate);
            map.put("endDate", endDate);
            boolean loadData = true;
            if (GroupConstants.isHuiZhangOrFuHuiZhang(self.getUserRole())) {
                if (targetUserId == -1) {
                    targetUserId = self.getUserId();
                } else {
                    GroupUser target = groupDaoNew.loadGroupUser(groupId, targetUserId);
                    if (target == null) {
                        loadData = false;
                    }
                }
            } else if (GroupConstants.isChengYuan(self.getUserRole())) {
                targetUserId = self.getUserId();
            } else {
                if (targetUserId == -1) {
                    targetUserId = self.getUserId();
                } else {
                    GroupUser target = groupDaoNew.loadGroupUser(groupId, targetUserId);
                    if (target == null || !GroupConstants.isUnder(self, target)) {
                        loadData = false;
                    }
                }
            }
            int count = 0;
            List<HashMap<String, Object>> resList = Collections.emptyList();
            if (loadData) {
                map.put("keyWord", targetUserId);
                count = this.groupDaoNew.countGroupCreditLog(map);
                if (count > 0) {
                    resList = this.groupDaoNew.loadGroupCreditLog(map);
                }
            } else {
                count = 0;
            }
            JSONObject json = new JSONObject();
            json.put("pageNo", pageNo);
            json.put("pageSize", pageSize);
            json.put("list", resList);
            json.put("pages", (int) Math.ceil(count * 1.0 / pageSize));
            json.put("total", count);
            OutputUtil.output(0, json, getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("loadCreditLog|error|" + e.getMessage(), e);
            OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        }
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
     * 加入消息
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
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
                return;
            }

            int groupId = NumberUtils.toInt(params.get("groupId"), -1);
            int pageNo = NumberUtils.toInt(params.get("pageNo"), 1);
            int pageSize = NumberUtils.toInt(params.get("pageSize"), 30);
            long queryUserId = NumberUtils.toInt(params.get("queryUserId"), 0);
            int selectType = NumberUtils.toInt(params.get("selectType"), 0); // 查询类型，0：查询所有，1：查询被操作人，2：查询操作人，默认0
            int alertType = NumberUtils.toInt(params.get("alertType"), 0); // 消息类型：0所有，1邀请，2申请，3踢出

            GroupUser self = groupDao.loadGroupUser(userId, groupId);
            if (self == null) {
                OutputUtil.output(3, LangMsg.getMsg(LangMsg.code_9), getRequest(), getResponse(), false);
                return;
            } else if (!GroupConstants.isHuiZhangOrFuHuiZhang(self.getUserRole())) {
                OutputUtil.output(3, LangMsg.getMsg(LangMsg.code_8), getRequest(), getResponse(), false);
                return;
            }

            int count = groupDaoNew.countGroupUserAlert(groupId, selectType, queryUserId, alertType);
            List<HashMap<String, Object>> dataList = Collections.emptyList();
            if (count > 0) {
                dataList = groupDaoNew.loadGroupUserAlert(groupId, selectType, queryUserId, alertType, pageNo, pageSize);
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

    /**
     * 群统计
     * 局数统计
     */
    public void loadTableCount() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("loadTableCount|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            String groupId = params.get("groupId");
            long userId = NumberUtils.toInt(params.get("userId"), 0);
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
                return;
            }

            GroupUser self = groupDaoNew.loadGroupUser(NumberUtils.toLong(groupId), userId);
            if (self == null) {
                OutputUtil.output(3, LangMsg.getMsg(LangMsg.code_9), getRequest(), getResponse(), false);
                return;
            } else if (!GroupConstants.isHuiZhangOrFuHuiZhang(self.getUserRole())) {
                OutputUtil.output(5, LangMsg.getMsg(LangMsg.code_10), getRequest(), getResponse(), false);
                return;
            }
//            long groupIdLong = Long.valueOf(groupId);
//            if(GroupConstants.isFuHuiZhang(self.getUserRole())){
//                self = groupDaoNew.loadGroupMaster(groupIdLong);
//            }

            List<HashMap<String, Object>> list = groupDaoNew.loadTableCount(groupId);
            List<HashMap<String, Object>> list2 = dataStatisticsDao.loadGroupDecDiamond(groupId);
            if (list == null) {
                list = new ArrayList<>();
            }
            Calendar ca = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            List<String> dateStrList = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                String dateStr = sdf.format(ca.getTime());
                dateStrList.add(dateStr);
                ca.set(Calendar.DATE, ca.get(Calendar.DATE) - 1);
            }
            HashMap<String, Object> map;
            HashMap<String, Object> map2 = new HashMap<>();
            HashMap<String, Object> map3 = new HashMap<>();
            if (list.isEmpty()) {
                for (String dateStr : dateStrList) {
                    map = new HashMap<>();
                    map.put("ctime", dateStr);
                    map.put("c", "0");
                    map.put("decdiamond", "0");
                    list.add(map);
                }
            } else {
                for (HashMap<String, Object> m : list) {
                    map2.put(String.valueOf(m.get("ctime")), m.get("c"));
                }
                for (HashMap<String, Object> m : list2) {
                    map3.put(String.valueOf(m.get("ctime")), m.get("c"));
                }

                list.clear();
                for (String dateStr : dateStrList) {
                    map = new HashMap<>();
                    map.put("ctime", dateStr);
                    map.put("c", map2.containsKey(dateStr) ? map2.get(dateStr) : "0");
                    map.put("decDiamond", map3.containsKey(dateStr) ? map3.get(dateStr) : "0");
                    list.add(map);
                }
            }

            JSONObject json = new JSONObject();
            json.put("list", list);
            int activity = 0;
            // 百万大奖活动期间
            json.put("activity", activity);
            OutputUtil.output(0, json, getRequest(), getResponse(), false);
        } catch (Exception e) {
            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 对应人气桌数量
     */
    public void loadFakeTableCount() {
        Map<String, String> params = null;
        try {
            params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("loadFakeTableCount|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long configId = NumberUtils.toLong(params.get("configId"), -1);
            long userId = NumberUtils.toInt(params.get("userId"), 0);
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
                return;
            }
            int fakeTableCount = groupDaoNew.loadFakeTableCount(configId, -1);
            JSONObject json = new JSONObject();
            json.put("fakeTableCount", fakeTableCount);
            OutputUtil.output(0, json, getRequest(), getResponse(), false);
        } catch (Exception e) {
            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
            LOGGER.error("loadFakeTableCount|error|" + JSON.toJSONString(params), e.getMessage(), e);
        }
    }

    /**
     * 赠送统计
     */
    public void loadCreditCommissionLog() {
        Map<String, String> params = null;
        try {
            params = UrlParamUtil.getParameters(getRequest());
            LOGGER.debug("loadCreditCommissionLog|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toInt(params.get("userId"), 0);
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
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

            int orderByField = NumberUtils.toInt(params.get("orderByField"), 1);
            int orderByType = NumberUtils.toInt(params.get("orderByType"), 3);

            long groupId = NumberUtils.toLong(params.get("groupId"), -1);
            if (groupId == -1) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }

            GroupUser self = groupDao.loadGroupUser(userId, groupId);
            if (self == null) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            String startDate = params.get("startDate"); // 日期格式：2019-08-01 00:00:01
            String endDate = params.get("endDate"); // 日期格式：2019-08-01 23:59:59
            if (!TimeUtil.checkDateFormat(startDate) || !TimeUtil.checkDateFormat(endDate)) {
                OutputUtil.output(3, "日期格式错误：" + startDate + "," + endDate, getRequest(), getResponse(), false);
                return;
            }

            int count = 0;
            Long totalCommissionCredit = 0l;
            List<HashMap<String, Object>> dataList = null;

            if (GroupConstants.isFuHuiZhang(self.getUserRole())) {
                self = groupDaoNew.loadGroupMaster(groupId);
            }
            count = groupDaoNew.countCreditCommissionLog(groupId, self.getUserId(), self.getPromoterLevel(), startDate, endDate);
            if (count > 0) {
                dataList = groupDaoNew.creditCommissionLog(groupId, self.getUserId(), self.getPromoterLevel(), startDate, endDate, orderByField, orderByType, pageNo, pageSize);
//                List<HashMap<String, Object>> zjsList = groupDaoNew.creditZjs(groupId, self.getUserId(), self.getPromoterLevel(), startDate, endDate);
                List<HashMap<String, Object>> zjsList = groupDaoNew.creditZjsNew(groupId, self.getUserId(), self.getPromoterLevel(), startDate, endDate);
                Map<String, HashMap<String, Object>> zjsMap = new HashMap<>();
                if (zjsList != null && zjsList.size() > 0) {
                    for (HashMap<String, Object> zjs : zjsList) {
                        zjsMap.put(zjs.get("promoterId").toString(), zjs);
                    }
                }
                for (HashMap<String, Object> data : dataList) {
                    String promoterIdStr = data.get("promoterId").toString();
                    HashMap<String, Object> zjs = zjsMap.get(promoterIdStr);
                    if (zjs != null) {
                        data.putAll(zjs);
                    } else {
                        data.put("zjs", 0);
                    }
                    //本组
                    long promoterIdLong = Long.valueOf(promoterIdStr);
                    if ("0".equals(promoterIdStr)) {
                        promoterIdLong = self.getUserId();
                    }
                    RegInfo regInfo = userDao.getUser(promoterIdLong);
                    if (regInfo != null) {
                        if ("0".equals(promoterIdStr)) {
                            data.put("teamName", "本组");
                            data.put("userId", self.getUserId());
                            data.put("promoterLevel", self.getPromoterLevel());
                        } else {
                            data.put("teamName", regInfo.getName());
                            data.put("userId", regInfo.getUserId());
                            data.put("promoterLevel", self.getPromoterLevel() + 1);
                        }
                        data.put("userName", regInfo.getName());
                        data.put("headimgurl", regInfo.getHeadimgurl());
                    }
                }
                totalCommissionCredit = groupDaoNew.sumCommissionCreditLog(groupId, self.getUserId(), startDate, endDate);
            } else {
                dataList = Collections.emptyList();
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
     * 赠送统计
     * 赠送详情
     */
    public void loadCreditCommissionLogByUser() {
        Map<String, String> params = null;
        try {
            params = UrlParamUtil.getParameters(getRequest());
            LOGGER.debug("loadCreditCommissionLogByUser|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toInt(params.get("userId"), 0);
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
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
            GroupUser self = groupDaoNew.loadGroupUser(groupId, userId);
            if (self == null) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            if (GroupConstants.isFuHuiZhang(self.getUserRole())) {
                self = groupDaoNew.loadGroupMaster(groupId);
            }
            String startDate = params.get("startDate"); // 日期格式：2019-08-01 00:00:01
            String endDate = params.get("endDate"); // 日期格式：2019-08-01 23:59:59
            if (!TimeUtil.checkDateFormat(startDate) || !TimeUtil.checkDateFormat(endDate)) {
                OutputUtil.output(3, "日期格式错误：" + startDate + "," + endDate, getRequest(), getResponse(), false);
                return;
            }
            int count = 0;
            Long totalCommissionCredit = 0l;
            Long targetId = NumberUtils.toLong(params.get("targetId"), 0);
            if (targetId == 0) {
                targetId = self.getUserId();
            }
            GroupUser target = groupDaoNew.loadGroupUser(groupId, targetId);
            List<HashMap<String, Object>> dataList = null;
            if (GroupConstants.isChengYuan(self.getUserRole())) {
                OutputUtil.output(0, LangMsg.getMsg(LangMsg.code_8), getRequest(), getResponse(), false);
                return;
            } else {
                count = groupDaoNew.countCreditCommissionLogByUser(groupId, self.getUserId(), targetId, target.getPromoterLevel(), startDate, endDate);
                if (count > 0) {
                    dataList = groupDaoNew.creditCommissionLogByUser(groupId, self.getUserId(), targetId, target.getPromoterLevel(), startDate, endDate, pageNo, pageSize);
                    totalCommissionCredit = groupDaoNew.sumCommissionCreditLog(groupId, self.getUserId(), startDate, endDate);
                } else {
                    dataList = Collections.emptyList();
                }
            }

            // 局数
            List<HashMap<String, Object>> zjsList = groupDaoNew.creditZjsByUser(groupId, self.getUserId(), targetId, target.getPromoterLevel(), startDate, endDate);
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
//                if (data.get("promoterId").toString().equals(String.valueOf(userId))) {
//                    data.put("promoterId", 0);
//                }
                userIds.add(uid);
            }
            // 输赢信用分
            if (userIds.length() > 0) {
                String startDateyyyyMMdd = TimeUtil.getSimpleDay(TimeUtil.parseTimeInDate(startDate));
                String endDateyyyyMMdd = TimeUtil.getSimpleDay(TimeUtil.parseTimeInDate(endDate));
                Map<String, Object> winLoseCreditMap = groupDaoNew.winLoseCreditByUserIdsToMap(groupId, userIds.toString(), startDateyyyyMMdd, endDateyyyyMMdd);
                for (HashMap<String, Object> data : dataList) {
                    String uid = data.get("userId").toString();
                    if (winLoseCreditMap.containsKey(uid)) {
                        data.put("winLoseCredit", winLoseCreditMap.get(uid));
                    } else {
                        data.put("winLoseCredit", 0);
                    }
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
     * 赠送分查询
     */
    public void searchCreditCommissionLog() {
        Map<String, String> params = null;
        try {
            params = UrlParamUtil.getParameters(getRequest());
            LOGGER.debug("searchCreditCommissionLog|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toInt(params.get("userId"), 0);
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
                return;
            }
            long targetId = NumberUtils.toLong(params.get("targetId"), -1);
            long groupId = NumberUtils.toLong(params.get("groupId"), -1);
            if (groupId == -1 || targetId == -1) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }

            String startDate = params.get("startDate"); // 日期格式：2019-08-01 00:00:01
            String endDate = params.get("endDate"); // 日期格式：2019-08-01 23:59:59
            if (!TimeUtil.checkDateFormat(startDate) || !TimeUtil.checkDateFormat(endDate)) {
                OutputUtil.output(3, "日期格式错误：" + startDate + "," + endDate, getRequest(), getResponse(), false);
                return;
            }

            GroupUser self = groupDaoNew.loadGroupUser(groupId, userId);
            if (self == null) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            GroupUser target = groupDaoNew.loadGroupUser(groupId, targetId);
            if (target == null) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_14), getRequest(), getResponse(), false);
                return;
            }
            if (GroupConstants.isFuHuiZhang(self.getUserRole())) {
                self = groupDaoNew.loadGroupMaster(groupId);
            }
            JSONObject json = new JSONObject();
            HashMap<String, Object> data = groupDaoNew.searchCommissionLog(groupId, self.getUserId(), targetId, startDate, endDate);
            if (data != null && Long.parseLong(data.get("commissionCredit").toString()) > 0) {
                RegInfo regInfo = userDao.getUser(target.getUserId());
                if (regInfo != null) {
                    data.put("userId", target.getUserId());
                    data.put("userName", regInfo.getName());
                    data.put("headimgurl", regInfo.getHeadimgurl());
                }
                json.put("data", data);
                String startDateyyyyMMdd = TimeUtil.getSimpleDay(TimeUtil.parseTimeInDate(startDate));
                String endDateyyyyMMdd = TimeUtil.getSimpleDay(TimeUtil.parseTimeInDate(endDate));
                HashMap<String, Object> commissonData = groupDaoNew.logGroupCommissionBuUser(groupId, targetId, startDateyyyyMMdd, endDateyyyyMMdd);
                if (commissonData != null) {
                    data.putAll(commissonData);
                } else {
                    data.put("zjs", 0);
                    data.put("winLoseCredit", 0);
                }
                data.put("promoterId", target.getPromoterId());
            }

            OutputUtil.output(0, json, getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("searchCreditCommissionLog|error|" + JSON.toJSONString(params), e.getMessage(), e);
            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        }
    }

    /**
     * 修改信用分兑换比例
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
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
                return;
            }
            long groupId = NumberUtils.toInt(params.get("groupId"), -1);
            int creditRate = NumberUtils.toInt(params.get("creditRate"), -1);
            if (creditRate != 1 && creditRate != 10 && creditRate != 100) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            GroupInfo groupInfo = groupDaoNew.loadGroupInfo(groupId);
            if (groupInfo == null) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            GroupUser groupUser = groupDaoNew.loadGroupUser(groupId, userId);
            if (groupUser == null || !GroupConstants.isHuiZhang(groupUser.getUserRole())) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_7), getRequest(), getResponse(), false);
                return;
            }
            if (groupDaoNew.updateCreditRate(groupId, creditRate) <= 0) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
                return;
            }
            for (int seq = 1; seq <= 10; seq++) {
                GroupCommissionConfig sysConifg = GroupConstants.getSysCommissionConfig(seq, creditRate);
                if (sysConifg != null) {
                    groupDaoNew.resetCommissionConfig(groupId, seq, sysConifg.getMinCredit(), sysConifg.getMaxCredit());
                }
            }
            JSONObject json = new JSONObject();
            json.put("msg", LangMsg.getMsg(LangMsg.code_0));
            OutputUtil.output(0, json, getRequest(), getResponse(), false);
            LOGGER.info("updateGroupCreditRate|succ|params:{}", params);
        } catch (Exception e) {
            LOGGER.error("updateGroupCreditRate|error|" + JSON.toJSONString(params), e.getMessage(), e);
            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        }
    }

    /**
     * 设置分成模式
     */
    public void updateAllotMode() {
        Map<String, String> params = null;
        try {
            params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("updateAllotMode|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), 0);
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
                return;
            }

            long groupId = NumberUtils.toInt(params.get("groupId"), -1);
            int mode = NumberUtils.toInt(params.get("mode"), 1); // 分成模式：1：大赢家分成，2：参与分成
            if (mode != 1 && mode != 2) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_23), getRequest(), getResponse(), false);
                return;
            }
            String modeSet = ResourcesConfigsUtil.loadStringValue("ServerConfig", "groupAllotModeSet", "1");
            if (!modeSet.contains(String.valueOf(mode))) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_23), getRequest(), getResponse(), false);
                return;
            }
            GroupInfo groupInfo = groupDaoNew.loadGroupInfo(groupId);
            if (groupInfo == null) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_7), getRequest(), getResponse(), false);
                return;
            }

            GroupUser groupUser = groupDaoNew.loadGroupUser(groupId, userId);
            if (groupUser == null || !GroupConstants.isHuiZhang(groupUser.getUserRole())) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_7), getRequest(), getResponse(), false);
                return;
            }

            if (groupDaoNew.updateCreditAllotMode(groupId, mode) <= 0) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
                return;
            }

            OutputUtil.output(0, LangMsg.getMsg(LangMsg.code_0), getRequest(), getResponse(), false);
            LOGGER.info("updateAllotMode|succ:{}", params);
        } catch (Exception e) {
            LOGGER.error("updateAllotMode|error|" + JSON.toJSONString(params), e.getMessage(), e);
            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        }
    }

    /**
     * 亲友圈经验成长
     *
     * @param group
     * @param master
     * @param destGroupUser
     * @param credit
     */
    private void addGroupExp(GroupInfo group, GroupUser master, GroupUser destGroupUser, long credit) {
        try {
            long gExp = (Math.abs(credit) / 100) * group.getCreditRate();
            if (gExp == 0) {
                return;
            }
            long guExp = gExp;


            // 增加亲友圈经验
            SysGroupLevelConfig gConfig = GroupConfigUtil.getGroupLevelConfig(group.getLevel());
            if (gConfig.getExp() > 0 && group.getCreditExpToday() < gConfig.getCreditExpLimit()) {
                if (group.getCreditExpToday() + gExp >= gConfig.getCreditExpLimit()) {
                    gExp = gConfig.getCreditExpLimit() - group.getCreditExpToday();
                }
                groupDaoNew.addGroupExp(group.getKeyId(), gExp, gExp);
                groupDaoNew.calcGroupLevel(group.getKeyId());

                SysGroupLevelConfig next = GroupConfigUtil.getGroupLevelConfig(gConfig.getLevel() + 1);
                if (next != null && next.getExp() == 0) {
                    groupDaoNew.calcGroupExp(group.getKeyId());
                }

            } else {
                gExp = 0;
            }
            Date now = new Date();
            HashMap<String, Object> gLogMap = new HashMap<>();
            gLogMap.put("groupId", group.getGroupId());
            gLogMap.put("userId", destGroupUser.getUserId());
            gLogMap.put("optUserId", master.getUserId());
            gLogMap.put("tableId", 0);
            gLogMap.put("credit", credit);
            gLogMap.put("exp", gExp);
            gLogMap.put("createdTime", now);
            groupDaoNew.insertLogGroupExp(gLogMap);

            // 增加群亲友圈成员经验
            SysGroupUserLevelConfig guConfig = GroupConfigUtil.getGroupUserLevelConfig(destGroupUser.getLevel());
            if (guConfig.getExp() > 0 && destGroupUser.getCreditExpToday() < guConfig.getCreditExpLimit()) {
                if (destGroupUser.getCreditExpToday() + guExp >= guConfig.getCreditExpLimit()) {
                    guExp = guConfig.getCreditExpLimit() - destGroupUser.getCreditExpToday();
                }
                groupDaoNew.addGroupUserExp(destGroupUser.getKeyId(), guExp, guExp);
                groupDaoNew.calcGroupUserLevel(destGroupUser.getKeyId());

                SysGroupUserLevelConfig next = GroupConfigUtil.getGroupUserLevelConfig(guConfig.getLevel() + 1);
                if (next != null && next.getExp() == 0) {
                    groupDaoNew.calcGroupUserExp(destGroupUser.getKeyId());
                }

                // 推送消息给前端
                GroupUser destGuNew = groupDaoNew.loadGroupUser(destGroupUser.getGroupId(), destGroupUser.getUserId());
                if (destGuNew.getLevel() > destGroupUser.getLevel()) {
                    RegInfo destRegInfo = userDao.getUser(destGroupUser.getUserId());
                    if (destRegInfo != null && destRegInfo.getIsOnLine() == 1 && destRegInfo.getEnterServer() > 0) {
                        GameUtil.sendGroupUserLevelUp(destRegInfo.getEnterServer(), destGuNew.getUserId(), destGuNew.getGroupId(), destGuNew.getLevel());
                    }
                    // 升级记录
                    HashMap<String, Object> guLevelMap = new HashMap<>();
                    guLevelMap.put("groupId", destGuNew.getGroupId());
                    guLevelMap.put("userId", destGuNew.getUserId());
                    guLevelMap.put("level", destGuNew.getLevel());
                    guLevelMap.put("stat", 1);
                    guLevelMap.put("createdTime", now);
                    guLevelMap.put("lastUpTime", now);
                    try {
                        groupDaoNew.insertLogGroupUserLevel(guLevelMap);
                    } catch (Exception e) {
                        LOGGER.error("insertLogGroupUserLevel|error|" + group.getGroupId() + "|" + master.getUserId() + "|" + destGroupUser.getUserId() + "|" + credit, e.getMessage(), e);
                    }
                }
            } else {
                guExp = 0;
            }

            HashMap<String, Object> guLogMap = new HashMap<>();
            guLogMap.put("groupId", group.getGroupId());
            guLogMap.put("userId", destGroupUser.getUserId());
            guLogMap.put("optUserId", master.getUserId());
            guLogMap.put("tableId", 0);
            guLogMap.put("credit", credit);
            guLogMap.put("exp", guExp);
            guLogMap.put("createdTime", now);
            groupDaoNew.insertLogGroupUserExp(guLogMap);

        } catch (Exception e) {
            LOGGER.error("addGroupExp|error|" + group.getGroupId() + "|" + master.getUserId() + "|" + destGroupUser.getUserId() + "|" + credit, e.getMessage(), e);
        }
    }


    /**
     * 亲友圈成长
     * 消耗统计
     */
    public void loadGroupExpLog() {
        Map<String, String> params = null;
        try {
            params = UrlParamUtil.getParameters(getRequest());
            LOGGER.debug("loadGroupExpLog|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toInt(params.get("userId"), 0);
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
                return;
            }
            long targetId = NumberUtils.toLong(params.get("targetId"), -1);
            long groupId = NumberUtils.toLong(params.get("groupId"), -1);
            if (groupId == -1 || targetId == -1) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }

            int pageNo = NumberUtils.toInt(params.get("pageNo"), 1);
            int pageSize = NumberUtils.toInt(params.get("pageSize"), 10);
            if (pageNo < 1) {
                pageNo = 1;
            }
            if (pageSize < 1) {
                pageSize = 10;
            }

            String startDate = params.get("startDate"); // 日期格式：2019-08-01 00:00:01
            String endDate = params.get("endDate"); // 日期格式：2019-08-01 23:59:59
            if (!TimeUtil.checkDateFormat(startDate) || !TimeUtil.checkDateFormat(endDate)) {
                OutputUtil.output(3, "日期格式错误：" + startDate + "," + endDate, getRequest(), getResponse(), false);
                return;
            }

            GroupUser self = groupDaoNew.loadGroupUser(groupId, userId);
            if (self == null) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            if (!GroupConstants.isHuiZhangOrFuHuiZhang(self.getUserRole())) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_8), getRequest(), getResponse(), false);
                return;
            }

            JSONObject json = new JSONObject();
            int dataCount = groupDaoNew.countLogGroupExp(groupId, targetId, startDate, endDate);
            if (dataCount > 0) {
                List<HashMap<String, Object>> dataList = groupDaoNew.loadLogGroupExp(groupId, targetId, startDate, endDate, pageNo, pageSize);
                json.put("logList", dataList);
            } else {
                json.put("logList", Collections.emptyList());
            }
            json.put("totalCredit", groupDaoNew.sumCredit(groupId, 0, 0));
            json.put("masterCredit", self.getCredit());
            json.put("consumeCredit", groupDaoNew.sumLogGroupExpCredit(groupId, startDate, endDate));
            json.put("pageNo", pageNo);
            json.put("pageSize", pageSize);
            json.put("total", dataCount);
            json.put("pages", (int) Math.ceil(dataCount * 1.0 / pageSize));
            OutputUtil.output(0, json, getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("loadGroupExpLog|error|" + JSON.toJSONString(params), e.getMessage(), e);
            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        }
    }

    /**
     * 亲友圈成员
     * 领取升级奖励
     */
    public void awardLevelUp() {
        Map<String, String> params = null;
        try {
            params = UrlParamUtil.getParameters(getRequest());
            LOGGER.debug("awardLevelUp|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toInt(params.get("userId"), 0);
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
                return;
            }
            long groupId = NumberUtils.toLong(params.get("groupId"), -1);
            int level = NumberUtils.toInt(params.get("level"), -1);
            if (groupId == -1 || level == -1) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }

            GroupUser self = groupDaoNew.loadGroupUser(groupId, userId);
            if (self == null) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            Long keyId = groupDaoNew.loadLogGroupUserLevelKeyId(groupId, userId, level);
            if (keyId == null || keyId == 0) {
                OutputUtil.output(1, "没有奖励可领取", getRequest(), getResponse(), false);
                return;
            }
            groupDaoNew.updateLogGroupUserLevel(keyId);
            long addCoin = 2000;
            if (user.getEnterServer() > 0) {
                GameUtil.changCoinAndNotify(user.getEnterServer(), userId, 0, addCoin, "onLevelUp");
            }
            JSONObject json = new JSONObject();
            json.put("addCoin", addCoin);
            json.put("level", level);
            OutputUtil.output(0, json, getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("awardLevelUp|error|" + JSON.toJSONString(params), e.getMessage(), e);
            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        }
    }

    /**
     * 赠送详情
     * 指定牌桌的赠送详情
     */
    public void loadCommissionDetailForTable() {
        Map<String, String> params = null;
        try {
            params = UrlParamUtil.getParameters(getRequest());
            LOGGER.debug("loadCommissionDetailForTable|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toInt(params.get("userId"), 0);
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
                return;
            }
            long groupId = NumberUtils.toLong(params.get("groupId"), -1);
            long groupTableId = NumberUtils.toLong(params.get("groupTableId"), -1);
            if (groupId == -1) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            GroupUser self = groupDaoNew.loadGroupUser(groupId, userId);
            if (self == null) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            GroupTable groupTable = groupDaoNew.loadGroupTableById(groupTableId, groupId);
            if (groupTable == null || groupTable.getGroupId().longValue() != groupId) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }

            JSONObject json = new JSONObject();
            List<HashMap<String, Object>> dataList = groupDaoNew.loadCommissionDetailForTable(groupTableId, groupId);
            if (dataList == null) {
                dataList = Collections.emptyList();
            }
            json.put("dataList", dataList);
            OutputUtil.output(0, json, getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("loadCommissionDetailForTable|error|" + JSON.toJSONString(params), e.getMessage(), e);
            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        }
    }

    /**
     * spy2d牌桌内点击玩家头像得到的信息：一级代理信息、当日输赢分
     */
    public void loadGroupUserInfoForSpy() {
        Map<String, String> params = null;
        try {
            params = UrlParamUtil.getParameters(getRequest());
            LOGGER.debug("loadGroupUserInfoForSpy|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toInt(params.get("userId"), 0);
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
                return;
            }
            long groupId = NumberUtils.toLong(params.get("groupId"), -1);
            long targetUserId = NumberUtils.toLong(params.get("targetUserId"), -1);
            if (groupId == -1) {
                OutputUtil.output(-3, "", getRequest(), getResponse(), false);
                return;
            }
            GroupUser self = groupDaoNew.loadGroupUser(groupId, userId);
            if (self == null) {
                OutputUtil.output(-4, "", getRequest(), getResponse(), false);
                return;
            } else if (self.getIsSpy() != 2) {
                OutputUtil.output(-5, "", getRequest(), getResponse(), false);
                return;
            }
            if(targetUserId == -1) {
                return;
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date now = new Date();
            String startDate = sdf.format(DateUtil.getDayStartDate(now));
            String endDate = sdf.format(DateUtil.getNextDayStartDate(now));

            GroupUser target = groupDaoNew.loadGroupUser(groupId, targetUserId);
            long upUserId = target.getPromoterId2();    //一级代理
            String upUserName = "";
            if(upUserId == 0){
                upUserId = targetUserId;
                upUserName = target.getUserName();
            } else {
                RegInfo upUser = userDao.getUser(upUserId);
                if(upUser != null){
                    upUserName = upUser.getName();
                }
            }
            Integer sumCredit = groupDaoNew.sumCreditByType(groupId, targetUserId, 3, startDate, endDate);
            JSONObject json = new JSONObject();

            json.put("userId", target.getUserId());
            json.put("upUserId", upUserId);
            json.put("upUserName", upUserName);
            json.put("sumCredit", sumCredit);

            OutputUtil.output(0, json, getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("loadGroupUserInfoForSpy|error|" + JSON.toJSONString(params), e.getMessage(), e);
            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        }
    }

    /**
     * 亲友圈互斥名单列表
     */
    public void loadGroupUserRejectList() {
        Map<String, String> params = null;
        try {
            params = UrlParamUtil.getParameters(getRequest());
            LOGGER.debug("loadGroupUserRejectList|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toInt(params.get("userId"), 0);
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
                return;
            }
            long groupId = NumberUtils.toLong(params.get("groupId"), -1);
            long targetUserId = NumberUtils.toLong(params.get("targetUserId"), -1);
            if (groupId == -1) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            int pageNo = NumberUtils.toInt(params.get("pageNo"), 1);
            int pageSize = NumberUtils.toInt(params.get("pageSize"), 30);
            if (pageNo < 1) {
                pageNo = 1;
            }
            if (pageSize < 1) {
                pageSize = 10;
            }
            GroupUser self = groupDaoNew.loadGroupUser(groupId, userId);
            if (self == null) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            } else if (!GroupConstants.isHuiZhangOrFuHuiZhang(self.getUserRole())) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_8), getRequest(), getResponse(), false);
                return;
            }
            boolean needQuery = true;
            if (targetUserId > 0) {
                GroupUser target = groupDaoNew.loadGroupUser(groupId, targetUserId);
                if (target == null) {
                    needQuery = false;
                }
            }

            JSONObject json = new JSONObject();
            List<HashMap<String, Object>> dataList = Collections.emptyList();
            if (needQuery) {
                dataList = groupDaoNew.loadGroupUserRejectList(groupId, targetUserId, pageNo, pageSize);
                if (dataList == null) {
                    dataList = Collections.emptyList();
                }
            }
            json.put("dataList", dataList);
            OutputUtil.output(0, json, getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("loadGroupUserRejectList|error|" + JSON.toJSONString(params), e.getMessage(), e);
            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        }
    }

    /**
     * 修改互斥名单
     * 1、新增名单
     * 2、删除名单
     */
    public void updateGroupUserReject() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("updateGroupUserReject|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), -1);
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
                return;
            }
            long groupId = NumberUtils.toLong(params.get("groupId"), -1);
            if (groupId <= 0) {
                OutputUtil.output(1, "userId错误", getRequest(), getResponse(), false);
                return;
            }
            GroupUser self = groupDaoNew.loadGroupUser(groupId, userId);
            if (self == null) {
                OutputUtil.output(3, LangMsg.getMsg(LangMsg.code_11), getRequest(), getResponse(), false);
                return;
            } else if (!GroupConstants.isHuiZhangOrFuHuiZhang(self.getUserRole())) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_8), getRequest(), getResponse(), false);
                return;
            }
            int optType = NumberUtils.toInt(params.get("optType"), -1);
            if (optType == 1) {
                // ----------------新建互斥---------------------------------
                long targetUserId1 = NumberUtils.toLong(params.get("targetUserId1"), -1);
                long targetUserId2 = NumberUtils.toLong(params.get("targetUserId2"), -1);
                GroupUser target1 = groupDaoNew.loadGroupUser(groupId, targetUserId1);
                if (target1 == null) {
                    OutputUtil.output(3, LangMsg.getMsg(LangMsg.code_11), getRequest(), getResponse(), false);
                    return;
                }
                GroupUser target2 = groupDaoNew.loadGroupUser(groupId, targetUserId2);
                if (target2 == null) {
                    OutputUtil.output(3, LangMsg.getMsg(LangMsg.code_11), getRequest(), getResponse(), false);
                    return;
                }
                // 是否已经存在
                GroupUserReject exist = groupDaoNew.loadGroupUserReject(groupId, GroupConstants.genGroupUserRejectKeyStr(targetUserId1, targetUserId2));
                if (null != exist) {
                    OutputUtil.output(3, "两个玩家，已经设置过互斥", getRequest(), getResponse(), false);
                    return;
                }

                GroupUserReject reject = new GroupUserReject();
                reject.setGroupId(groupId);
                reject.setUserId1(targetUserId1 < targetUserId2 ? targetUserId1 : targetUserId2);
                reject.setUserId2(targetUserId1 < targetUserId2 ? targetUserId2 : targetUserId1);
                reject.setUserIdKey(0L);
                reject.setUserIdKeyStr(GroupConstants.genGroupUserRejectKeyStr(reject.getUserId1(), reject.getUserId2()));
                reject.setCreatedTime(new Date());
                groupDaoNew.createGroupUserReject(reject);
                LOGGER.info("updateGroupUserReject|succ|" + optType + "|" + userId + "|" + groupId + "|" + targetUserId1 + "|" + targetUserId2);
            } else if (optType == 2) {
                // ----------------删除互斥---------------------------------
                long keyId = NumberUtils.toLong(params.get("keyId"), -1);
                int ret = groupDaoNew.deleteGroupUserReject(keyId, groupId);
                LOGGER.info("updateGroupUserReject|succ|" + optType + "|" + userId + "|" + groupId + "|" + keyId);
            } else {
                OutputUtil.output(4, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            OutputUtil.output(0, LangMsg.getMsg(LangMsg.code_0), getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 修改亲友圈玩法配置
     */
    public void updateGroupTableConfig() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("updateGroupTableConfig|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), -1);
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
                return;
            }
            long groupId = NumberUtils.toLong(params.get("groupId"), -1);
            if (groupId <= 0) {
                OutputUtil.output(1, "userId错误", getRequest(), getResponse(), false);
                return;
            }
            GroupUser self = groupDaoNew.loadGroupUser(groupId, userId);
            if (self == null) {
                OutputUtil.output(3, LangMsg.getMsg(LangMsg.code_11), getRequest(), getResponse(), false);
                return;
            } else if (!GroupConstants.isHuiZhangOrFuHuiZhang(self.getUserRole())) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_8), getRequest(), getResponse(), false);
                return;
            }
            int optType = NumberUtils.toInt(params.get("optType"), -1);
            if (optType == 1) {
                // ----------------批量修改排序值---------------------------------

                String configsStr = params.get("orderConfigs");
                if (StringUtils.isBlank(configsStr)) {
                    OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                    return;
                }
                String[] configs = configsStr.split("[;]");
                for (String config : configs) {
                    if (StringUtils.isBlank(config)) {
                        continue;
                    }
                    String[] splits = config.split(",");
                    if (splits.length < 2) {
                        continue;
                    }
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("groupId", groupId);
                    map.put("keyId", Long.valueOf(splits[0]));
                    map.put("tableOrder", Integer.valueOf(splits[1]));
                    groupDaoNew.updateGroupTableConfigByKeyId(map);
                }
                LOGGER.info("updateGroupTableConfig|succ|" + optType + "|" + userId + "|" + groupId);
            } else {
                OutputUtil.output(4, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            OutputUtil.output(0, LangMsg.getMsg(LangMsg.code_0), getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 一键踢人
     * 不活跃成员
     */
    public void loadInactiveUserList() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("loadInactiveUserList|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), -1);
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
                return;
            }

            int groupId = NumberUtils.toInt(params.get("groupId"), -1);
            int pageNo = NumberUtils.toInt(params.get("pageNo"), 1);
            int pageSize = NumberUtils.toInt(params.get("pageSize"), 100);

            GroupUser groupUser = groupDaoNew.loadGroupUser(groupId, userId);
            if (groupUser == null) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_9), getRequest(), getResponse(), false);
                return;
            }
            if (!GroupConstants.isHuiZhang(groupUser.getUserRole())) {
                //普通成员无权查看
                OutputUtil.output(2, LangMsg.getMsg(LangMsg.code_10), getRequest(), getResponse(), false);
                return;
            }
            String dateType = params.get("dateType"); // 1:一个月,2:二个月,3:三个月,4:7天，5:15天
            String creditLimit = params.get("creditLimit");   // 0：信用分为0 1:信用分不为0
            List<HashMap<String, Object>> inactiveUserList = groupDaoNew.loadInactiveUser(groupId, dateType, creditLimit, pageNo, pageSize);
            int userCount = 0;
            if (inactiveUserList != null && inactiveUserList.size() > 0) {
                userCount = inactiveUserList.size();
            } else {
                inactiveUserList = Collections.emptyList();
            }
//            int userCount = groupDao.countInactiveUser(groupId, dateType);

            JSONObject json = new JSONObject();
            json.put("pageNo", pageNo);
            json.put("pageSize", pageSize);
            json.put("total", userCount);
            json.put("pages", (int) Math.ceil(userCount * 1.0 / pageSize));
            json.put("list", inactiveUserList);
            OutputUtil.output(0, json, getRequest(), getResponse(), false);

        } catch (Exception e) {
            OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
            LOGGER.error("loadInactiveUserList|error|" + e.getMessage(), e);
        }
    }

    /**
     * 一键踢人
     * 踢出非活跃用户
     */
    public void fireInactiveUser() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("fireInactiveUser|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), -1);
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
                return;
            }
            int groupId = NumberUtils.toInt(params.get("groupId"), -1);
            if (groupId <= 0) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            GroupUser groupUser = groupDaoNew.loadGroupUser(groupId, userId);
            if (groupUser == null) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_9), getRequest(), getResponse(), false);
                return;
            }
            if (!GroupConstants.isHuiZhang(groupUser.getUserRole())) {
                //普通成员无权查看
                OutputUtil.output(2, LangMsg.getMsg(LangMsg.code_10), getRequest(), getResponse(), false);
                return;
            }
            String userIdsStr = params.get("userIds");
            if (StringUtils.isBlank(userIdsStr)) {
                OutputUtil.output(2, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            String[] userIds = userIdsStr.split(",");
            StringJoiner sj = new StringJoiner(",");
            for (String delId : userIds) {
                if (CommonUtil.isPureNumber(delId)) {
                    sj.add(delId);
                }
            }
            List<GroupUser> delGuList = Collections.emptyList();
            if (sj.toString().length() > 0) {
                delGuList = groupDaoNew.loadGroupUserByUserIds(groupId, sj.toString());
            }

            int delCount = 0;
            if (delGuList.size() > 0) {
                delCount = groupDaoNew.fireInactiveUser(sj.toString(), groupId);
                if (delCount > 0) {
                    groupDao.updateGroupInfoCount(-delCount, groupId);
                    if (delCount == delGuList.size()) {
                        for (GroupUser del : delGuList) {
                            insertGroupUserAlert(del.getGroupId(), del.getUserId(), userId, GroupConstants.TYPE_USER_ALERT_DELETE);
                        }
                    } else {
                        for (GroupUser del : delGuList) {
                            GroupUser gu = groupDaoNew.loadGroupUserForceMaster(groupId, userId);
                            if (gu == null) {
                                insertGroupUserAlert(del.getGroupId(), del.getUserId(), userId, GroupConstants.TYPE_USER_ALERT_DELETE);
                            }
                        }
                    }
                }
            }
            OutputUtil.output(0, "操作成功：踢出" + delCount + "人", getRequest(), getResponse(), false);
            LOGGER.info("fireInactiveUser|succ|params:{}", params);
        } catch (Exception e) {
            OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
            LOGGER.error("fireInactiveUser|error|" + e.getMessage(), e);
        }
    }


    /**
     * 金币房模式
     * 群统计、查看下级
     */
    public void groupGoldWinLog() {
        Map<String, String> params = null;
        try {
            params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("groupGoldWinLog|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), -1);
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
                return;
            }
            long groupId = NumberUtils.toLong(params.get("groupId"), -1);
            long targetUserId = NumberUtils.toLong(params.get("targetUserId"), -1);
            long queryUserId = NumberUtils.toLong(params.get("queryUserId"), -1);
            int pageNo = NumberUtils.toInt(params.get("pageNo"), 1);
            int pageSize = NumberUtils.toInt(params.get("pageSize"), 20);
            int tag = NumberUtils.toInt(params.get("tag"), -1);
            if (tag != 0 && tag != 1 && tag != -1) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            if (pageNo < 1) {
                pageNo = 1;
            }
            if (pageSize < 1) {
                pageSize = 20;
            }

            if (groupId == -1) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            GroupUser self = groupDaoNew.loadGroupUser(groupId, userId);
            if (self == null) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            } else if (GroupConstants.isChengYuan(self.getUserRole())) {
                OutputUtil.output(0, LangMsg.getMsg(LangMsg.code_8), getRequest(), getResponse(), false);
                return;
            }
            if (GroupConstants.isFuHuiZhang(self.getUserRole())) {
                self = groupDaoNew.loadGroupMaster(groupId);
            }
            if (targetUserId == -1) {
                targetUserId = self.getUserId();
            }
            GroupUser target = groupDaoNew.loadGroupUser(groupId, targetUserId);
            if (target == null) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            } else if (GroupConstants.isFuHuiZhang(target.getUserRole()) || GroupConstants.isChengYuan(target.getUserRole())) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_8), getRequest(), getResponse(), false);
                return;
            }
            long dataDate = NumberUtils.toLong(params.get("dataDate"), -1); // 日期格式：20190801
            if (dataDate == -1) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }

            List<HashMap<String, Object>> dataList = groupDaoNew.groupGoldWinLog(groupId, target.getUserId(), target.getPromoterLevel(), dataDate, pageNo, pageSize, queryUserId, tag);
            JSONObject json = new JSONObject();
            json.put("dataList", dataList);
            json.put("rootId", self.getUserId());
            OutputUtil.output(0, json, getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("groupGoldWinLog|error|" + JSON.toJSONString(params), e.getMessage(), e);
            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        }
    }

    /**
     * 金币房模式
     * 群统计 -> 点击成员
     */
    public void groupGoldWinLogNextLevel() {
        Map<String, String> params = null;
        try {
            params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("groupGoldWinLogNextLevel|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), -1);
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
                return;
            }
            long groupId = NumberUtils.toLong(params.get("groupId"), -1);
            long targetUserId = NumberUtils.toLong(params.get("targetUserId"), -1);
            int pageNo = NumberUtils.toInt(params.get("pageNo"), 1);
            int pageSize = NumberUtils.toInt(params.get("pageSize"), 20);
            long queryUserId = NumberUtils.toLong(params.get("queryUserId"), 0);
            int tag = NumberUtils.toInt(params.get("tag"), -1);
            if (tag != 0 && tag != 1 && tag != -1) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            if (pageNo < 1) {
                pageNo = 1;
            }
            if (pageSize < 1) {
                pageSize = 20;
            }
            if (groupId == -1) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            GroupUser self = groupDaoNew.loadGroupUser(groupId, userId);
            if (self == null) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            } else if (GroupConstants.isChengYuan(self.getUserRole())) {
                OutputUtil.output(0, LangMsg.getMsg(LangMsg.code_8), getRequest(), getResponse(), false);
                return;
            }
            if (GroupConstants.isFuHuiZhang(self.getUserRole())) {
                self = groupDaoNew.loadGroupMaster(groupId);
            }
            GroupUser target = groupDaoNew.loadGroupUser(groupId, targetUserId);
            if (target == null) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            } else if (GroupConstants.isFuHuiZhang(target.getUserRole()) || GroupConstants.isChengYuan(target.getUserRole())) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_8), getRequest(), getResponse(), false);
                return;
            }

            long dataDate = NumberUtils.toLong(params.get("dataDate"), -1); // 日期格式：20190801
            if (dataDate == -1) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            List<HashMap<String, Object>> dataList = groupDaoNew.groupGoldWinLogNextLevel(groupId, target.getUserId(), target.getPromoterLevel(), dataDate, pageNo, pageSize, queryUserId, tag);
            JSONObject json = new JSONObject();
            json.put("dataList", dataList);
            json.put("rootId", self.getUserId());
            OutputUtil.output(0, json, getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("groupGoldWinLogNextLevel|error|" + JSON.toJSONString(params), e.getMessage(), e);
            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        }
    }

    /**
     * 金币房模式
     * 标记
     */
    public void updateGroupGoldWinTag() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("updateGroupGoldWinTag|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), -1);
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
                return;
            }
            long groupId = NumberUtils.toLong(params.get("groupId"), -1);
            if (groupId <= 0) {
                OutputUtil.output(1, "userId错误", getRequest(), getResponse(), false);
                return;
            }
            GroupUser self = groupDaoNew.loadGroupUser(groupId, userId);
            if (self == null) {
                OutputUtil.output(3, LangMsg.getMsg(LangMsg.code_11), getRequest(), getResponse(), false);
                return;
            } else if (GroupConstants.isChengYuan(self.getUserRole())) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_8), getRequest(), getResponse(), false);
                return;
            }
            if (GroupConstants.isFuHuiZhang(self.getUserRole())) {
                self = groupDaoNew.loadGroupMaster(groupId);
            }
            int tag = NumberUtils.toInt(params.get("tag"), -1);
            if (tag != 0 && tag != 1) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            String keyIds = params.get("keyIds");
            if (StringUtils.isNotBlank(keyIds)) {
                String[] splits = keyIds.split(",");
                Integer validCount = groupDaoNew.countValidGroupGoldWin(groupId, keyIds, self.getUserId(), self.getPromoterLevel());
                if (splits.length != validCount) {
                    OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_8), getRequest(), getResponse(), false);
                    return;
                }
                int ret = groupDaoNew.updateGroupGoldWinTag(groupId, keyIds, tag);
                LOGGER.info("updateGroupGoldWinTag|succ|" + userId + "|" + groupId + "|" + keyIds + "|" + tag + "|" + ret);
            }
            OutputUtil.output(0, LangMsg.getMsg(LangMsg.code_0), getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 金币房模式
     * 成员统计
     */
    public void groupGoldWinLogList() {
        Map<String, String> params = null;
        try {
            params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("groupGoldWinLogList|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), -1);
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
                return;
            }
            int rankField = NumberUtils.toInt(params.get("rankField"), 1);
            int rankType = NumberUtils.toInt(params.get("rankType"), 1);
            long groupId = NumberUtils.toLong(params.get("groupId"), -1);
            long targetUserId = NumberUtils.toLong(params.get("targetId"), -1);
            long queryUserId = NumberUtils.toLong(params.get("queryUserId"), -1);
            if (groupId == -1) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
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

            GroupUser self = groupDaoNew.loadGroupUser(groupId, userId);
            if (self == null) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            if (targetUserId <= 0) {
                targetUserId = userId;
            }

            GroupUser target = groupDaoNew.loadGroupUser(groupId, targetUserId);
            if (target == null) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            if (GroupConstants.isFuHuiZhang(target.getUserRole())) {
                target = groupDaoNew.loadGroupMaster(groupId);
            }
            if (GroupConstants.isChengYuan(self.getUserRole())) {
                OutputUtil.output(0, LangMsg.getMsg(LangMsg.code_8), getRequest(), getResponse(), false);
                return;
            }

            long dataDate = NumberUtils.toLong(params.get("dataDate"), -1); // 日期格式：20190801
            if (dataDate == -1) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                Date now = new Date();
                dataDate = Long.valueOf(sdf.format(now));
            }
            HashMap<String, Object> sumData = null;
            List<HashMap<String, Object>> dataList = groupDaoNew.groupGoldWinLogNextAll(groupId, target.getUserId(), target.getPromoterLevel(), dataDate, rankField, rankType, queryUserId, pageNo, pageSize);
            if (dataList != null && dataList.size() > 0 && queryUserId == -1) {
                sumData = groupDaoNew.groupGoldWinLogNextAllSum(groupId, target.getUserId(), target.getPromoterLevel(), dataDate);
            }
            JSONObject json = new JSONObject();
            json.put("pageNo", pageNo);
            json.put("pageSize", pageSize);
            json.put("rankField",rankField);
            json.put("rankType",rankType);
            json.put("queryUserId",queryUserId);
            json.put("dataList", dataList);
            if (sumData != null) {
                json.put("sumData", sumData);
            }
            OutputUtil.output(0, json, getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("groupGoldWinLogList|error|" + JSON.toJSONString(params), e.getMessage(), e);
            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        }
    }


    /**
     * 预警分列表
     */
    public void groupWarnList() {
        if ("0".equals(ResourcesConfigsUtil.loadServerPropertyValue("group_warn_switch"))) {
            OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_6), getRequest(), getResponse(), false);
            return;
        }

        Map<String, String> params = null;
        try {
            params = UrlParamUtil.getParameters(getRequest());
            LOGGER.debug("teamList|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), 0);
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
                return;
            }
            int groupId = NumberUtils.toInt(params.get("groupId"), -1);
            int pageNo = NumberUtils.toInt(params.get("pageNo"), 1);
            int pageSize = NumberUtils.toInt(params.get("pageSize"), 30);

            String keyWord = params.get("keyWord");

            pageNo = pageNo < 0 ? 1 : pageNo;
            pageSize = pageSize < 0 ? 5 : pageSize > 30 ? 30 : pageSize;

            GroupUser groupUser = groupDaoNew.loadGroupUser(groupId, userId);
            if (groupUser == null) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            if(GroupConstants.isFuHuiZhang(groupUser.getUserRole())){//副会长跟会长统一权限
                GroupUser huizhang = groupDaoNew.loadGroupMaster(groupId);
                if(huizhang != null){
                    groupUser = huizhang;
                }
            }
            GroupInfo groupInfo = groupDaoNew.loadGroupInfo(groupId);
            if (groupInfo == null) {
                OutputUtil.output(4, LangMsg.getMsg(LangMsg.code_5), getRequest(), getResponse(), false);
                return;
            }
            JSONObject json = new JSONObject();
            json.put("pageNo", pageNo);
            json.put("pageSize", pageSize);

            List<Map<String, Object>> groupWarnList = groupWarnDao.selectGroupWarn(groupId, groupUser.getPromoterLevel(), groupUser.getUserId(), keyWord, pageNo, pageSize);

            json.put("groupWarnList", groupWarnList);
            OutputUtil.output(0, json, getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("groupWarnList|error|" + JSON.toJSONString(params), e.getMessage(), e);
            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        }
    }



    /**
     * 添加预警分设置
     */
    public void addGroupWarn() {
        if ("0".equals(ResourcesConfigsUtil.loadServerPropertyValue("group_warn_switch"))) {
            OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_6), getRequest(), getResponse(), false);
            return;
        }
        Map<String, String> params = null;
        try {
            params = UrlParamUtil.getParameters(getRequest());
            LOGGER.debug("addGroupWarn|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), 0);
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
                return;
            }
            int groupId = NumberUtils.toInt(params.get("groupId"), -1);
            long targetUserId = NumberUtils.toLong(params.get("targetUserId"), 0);
            if (targetUserId <= 0) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            GroupUser groupUser = groupDaoNew.loadGroupUser(groupId, userId);
            if (groupUser == null) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            if(GroupConstants.isFuHuiZhang(groupUser.getUserRole())){//副会长跟会长统一权限
                GroupUser huizhang = groupDaoNew.loadGroupMaster(groupId);
                if(huizhang != null){
                    groupUser = huizhang;
                }
            }
            GroupInfo groupInfo = groupDaoNew.loadGroupInfo(groupId);
            if (groupInfo == null) {
                OutputUtil.output(4, LangMsg.getMsg(LangMsg.code_5), getRequest(), getResponse(), false);
                return;
            }
//            RegInfo regInfo = this.userDao.getUser(targetUserId);
//            if (regInfo == null) {
//                OutputUtil.output(1, "玩家不存在", getRequest(), getResponse(), false);
//                return;
//            }
            List<GroupWarn> warnList = groupWarnDao.getGroupWarnByUserIdAndGroupId(targetUserId,groupId);
            if(warnList!=null && warnList.size() > 0){
                OutputUtil.output(1, "请不要重复添加", getRequest(), getResponse(), false);
                return;
            }
            GroupUser target = groupDaoNew.loadGroupUser(groupId, targetUserId);
            if (target == null) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_14), getRequest(), getResponse(), false);
                return;
            }
            if(GroupConstants.isChengYuan(target.getUserRole())){
                OutputUtil.output(1, "不能给普通成员设置", getRequest(), getResponse(), false);
                return;
            }
            //判断是不是我的直系下级
            if (target.getPromoterId() != groupUser.getUserId().longValue()) {
                response(2, LangMsg.getMsg(LangMsg.code_7));
                return;
            }

            GroupWarn groupWarn = new GroupWarn();
            groupWarn.setGroupId(groupId);
            groupWarn.setUserId(targetUserId);
            groupWarn.setWarnSwitch(0);
            groupWarn.setWarnScore(0);
            groupWarn.setCreateTime(System.currentTimeMillis());
            groupWarn.setUpdateTime(new Date());
            groupWarnDao.insertGroupWarn(groupWarn);

            List<Map<String, Object>> groupWarnList = groupWarnDao.selectGroupWarn(groupId, groupUser.getPromoterLevel(), groupUser.getUserId(), targetUserId+"", 1, 10);

            JSONObject json = new JSONObject();
            json.put("groupWarnList", groupWarnList);
            OutputUtil.output(0, json, getRequest(), getResponse(), false);
            LOGGER.info("addGroupWarn|params|" + params);
        } catch (Exception e) {
            LOGGER.error("addGroupWarn|error|" + JSON.toJSONString(params), e.getMessage(), e);
            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        }
    }


    /**
     * 修改预警分设置
     */
    public void updateGroupWarn() {
        if ("0".equals(ResourcesConfigsUtil.loadServerPropertyValue("group_warn_switch"))) {
            OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_6), getRequest(), getResponse(), false);
            return;
        }
        Map<String, String> params = null;
        try {
            params = UrlParamUtil.getParameters(getRequest());
            LOGGER.debug("teamList|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), 0);
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
                return;
            }
            int groupId = NumberUtils.toInt(params.get("groupId"), -1);
            long targetUserId = NumberUtils.toLong(params.get("targetUserId"), 0);
            if (targetUserId <= 0) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            GroupUser groupUser = groupDaoNew.loadGroupUser(groupId, userId);
            if (groupUser == null) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            if(GroupConstants.isFuHuiZhang(groupUser.getUserRole())){//副会长跟会长统一权限
                GroupUser huizhang = groupDaoNew.loadGroupMaster(groupId);
                if(huizhang != null){
                    groupUser = huizhang;
                }
            }
            GroupInfo groupInfo = groupDaoNew.loadGroupInfo(groupId);
            if (groupInfo == null) {
                OutputUtil.output(4, LangMsg.getMsg(LangMsg.code_5), getRequest(), getResponse(), false);
                return;
            }
//            RegInfo regInfo = this.userDao.getUser(targetUserId);
//            if (regInfo == null) {
//                OutputUtil.output(1, "玩家不存在", getRequest(), getResponse(), false);
//                return;
//            }
            List<GroupWarn> warnList = groupWarnDao.getGroupWarnByUserIdAndGroupId(targetUserId,groupId);
            if(warnList==null || warnList.size() <= 0){
                OutputUtil.output(1, "数据不存在，请先添加", getRequest(), getResponse(), false);
                return;
            }
            GroupUser target = groupDaoNew.loadGroupUser(groupId, targetUserId);
            if (target == null) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_14), getRequest(), getResponse(), false);
                return;
            }
            // 判断是不是我的直系下级
            if (target.getPromoterId() != groupUser.getUserId().longValue()) {
                response(2, LangMsg.getMsg(LangMsg.code_7));
                return;
            }

            int warnScore = NumberUtils.toInt(params.get("warnScore"), 0);
            int warnSwitch = NumberUtils.toInt(params.get("warnSwitch"), 0);
            if(warnScore <0){
                OutputUtil.output(1, "请不要设置负数", getRequest(), getResponse(), false);
                return;
            }

            groupWarnDao.updateGroupWarn(groupId,targetUserId,warnScore,warnSwitch);

            List<Map<String, Object>> groupWarnList = groupWarnDao.selectGroupWarn(groupId, groupUser.getPromoterLevel(), groupUser.getUserId(), targetUserId+"", 1, 10);

            JSONObject json = new JSONObject();
            json.put("groupWarnList", groupWarnList);
            OutputUtil.output(0, json, getRequest(), getResponse(), false);
            LOGGER.info("updateGroupWarn|params|" + params);
        } catch (Exception e) {
            LOGGER.error("updateGroupWarn|error|" + JSON.toJSONString(params), e.getMessage(), e);
            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        }
    }

    /**
     * 删除预警分设置
     */
    public void deleteGroupWarn() {
        Map<String, String> params = null;
        try {
            params = UrlParamUtil.getParameters(getRequest());
            LOGGER.debug("deleteGroupWarn|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), 0);
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
                return;
            }
            int groupId = NumberUtils.toInt(params.get("groupId"), -1);
            long targetUserId = NumberUtils.toLong(params.get("targetUserId"), 0);
            if (targetUserId <= 0) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            List<GroupWarn> warnList = groupWarnDao.getGroupWarnByUserIdAndGroupId(targetUserId,groupId);
            if(warnList==null || warnList.size() <= 0){
                OutputUtil.output(1, "数据不存在", getRequest(), getResponse(), false);
                return;
            }
            GroupUser target = groupDaoNew.loadGroupUser(groupId, targetUserId);
            if (target == null) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_14), getRequest(), getResponse(), false);
                return;
            }
            GroupUser groupUser = groupDaoNew.loadGroupUser(groupId, userId);
            if (groupUser == null) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            if(GroupConstants.isFuHuiZhang(groupUser.getUserRole())){//副会长跟会长统一权限
                GroupUser huizhang = groupDaoNew.loadGroupMaster(groupId);
                if(huizhang != null){
                    groupUser = huizhang;
                }
            }
            //判断是不是我的直系下级
            if (target.getPromoterId() !=  groupUser.getUserId().longValue()) {
                response(2, LangMsg.getMsg(LangMsg.code_7));
                return;
            }
            groupWarnDao.deleteGroupWarn(groupId,targetUserId);

            JSONObject json = new JSONObject();
            json.put("groupId", groupId);
            json.put("targetUserId", targetUserId);
            OutputUtil.output(0, json, getRequest(), getResponse(), false);
            LOGGER.info("deleteGroupWarn|params|" + params);
        } catch (Exception e) {
            LOGGER.error("deleteGroupWarn|error|" + JSON.toJSONString(params), e.getMessage(), e);
            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        }
    }

    /**
     * 设置新运转盘
     */
    public void setCreditWheel() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("setCreditWheel|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), -1);
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
                return;
            }
            long groupId = NumberUtils.toLong(params.get("groupId"), -1);
            if (groupId <= 0) {
                OutputUtil.output(1, "groupId错误", getRequest(), getResponse(), false);
                return;
            }
            GroupUser self = groupDaoNew.loadGroupUser(groupId, userId);
            if (self == null) {
                OutputUtil.output(3, LangMsg.getMsg(LangMsg.code_11), getRequest(), getResponse(), false);
                return;
            }
            if (!GroupConstants.isHuiZhang(self.getUserRole())) {
                OutputUtil.output(4, LangMsg.getMsg(LangMsg.code_8), getRequest(), getResponse(), false);
                return;
            }
            int opType = NumberUtils.toInt(params.get("opType"), -1);
            synchronized(getGroupLock(groupId)) {
                if (opType == 1) {     //增减奖池
                    int credit = NumberUtils.toInt(params.get("credit"), 0);
                    long creditPool = groupDaoNew.loadCreditWheelPool(groupId);
                    int ret = 0;
                    if (credit > 0) {     //增加
                        if (self.getCredit() < credit) {
                            OutputUtil.output(5, LangMsg.getMsg(LangMsg.code_15), getRequest(), getResponse(), false);
                            return;
                        }
                        ret = groupDao.transferGroupUserCreditToWheel(userId,groupId,credit);
                    } else {
                        if (creditPool < Math.abs(credit)){
                            OutputUtil.output(6, "奖池剩余分不足", getRequest(), getResponse(), false);
                            return;
                        }
                        ret = groupDao.transferWheelToGroupUserCredit(userId,groupId,Math.abs(credit));
                    }
                    if (ret == 2){
                        // 写入日志
                        HashMap<String, Object> logFrom = new HashMap<>();
                        logFrom.put("groupId", groupId);
                        logFrom.put("optUserId", userId);
                        logFrom.put("userId", userId);
                        logFrom.put("tableId", 0);
                        logFrom.put("credit", -1 * credit);
                        logFrom.put("type", GroupConstants.CREDIT_LOG_TYPE_WHEEL);
                        logFrom.put("flag", 1);
                        logFrom.put("userGroup", 0);
                        logFrom.put("mode", 1);
                        groupDaoNew.insertGroupCreditLog(logFrom);
                        if (user.getEnterServer() > 0) {
                            GameUtil.sendCreditUpdate(user.getEnterServer(), userId, groupId);
                        }
                    }
                    OutputUtil.output(0, creditPool + credit, getRequest(), getResponse(), false);
                } else if (opType == 2) {      //设置下次必抽大奖
                    HashMap<String, Object> wheelMap = new HashMap<>();
                    wheelMap.put("groupId", groupId);
                    wheelMap.put("nextWin", 1);
                    groupDaoNew.updateCreditWheel(wheelMap);
                    OutputUtil.output(0, LangMsg.getMsg(LangMsg.code_0), getRequest(), getResponse(), false);
                } else {
                    OutputUtil.output(4, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                    return;
                }
            }
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 幸运转盘
     */
    public void creditWheel() {
        Map<String, String> params = null;
        try {
            params = UrlParamUtil.getParameters(getRequest());
            LOGGER.debug("creditWheel|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), 0);
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(2, LangMsg.getMsg(LangMsg.code_2), getRequest(), getResponse(), false);
                return;
            }
            long groupId = NumberUtils.toLong(params.get("groupId"), -1);
            int opType = NumberUtils.toInt(params.get("opType"), 1);

            RegInfo regInfo = this.userDao.getUser(userId);
            if (regInfo == null) {
                OutputUtil.output(8, "玩家不存在", getRequest(), getResponse(), false);
                return;
            }
            GroupInfo group = groupDaoNew.loadGroupInfo(groupId);
            if (group == null) {
                OutputUtil.output(3, LangMsg.getMsg(LangMsg.code_5), getRequest(), getResponse(), false);
                return;
            }
            JSONObject extJson = JSONObject.parseObject(group.getExtMsg());
            int openWheel = extJson.getIntValue(GroupConstants.groupExtKey_creditWheel);
            if (openWheel <= 0) {
                OutputUtil.output(4, "活动未开启！", getRequest(), getResponse(), false);
                return;
            }
            GroupUser groupUser = groupDaoNew.loadGroupUser(groupId, userId);
            if (groupUser == null) {
                OutputUtil.output(5, LangMsg.getMsg(LangMsg.code_14), getRequest(), getResponse(), false);
                return;
            }
            HashMap<String, Object> guWheelMap = groupDaoNew.loadGroupUserwheel(groupUser.getKeyId());
            if(opType == 1){        //打开活动面板
                int lastCount = guWheelMap == null ? groupUser.getPlayCount1() : (int)guWheelMap.get("lastCount");
                int needPlayCount = guWheelMap == null ? openWheel : openWheel - ((groupUser.getPlayCount2()-lastCount) % openWheel);
                if (needPlayCount == 0)
                    needPlayCount = openWheel;
                JSONObject json = new JSONObject();
                json.put("groupId", groupId);
                json.put("wheelCount", guWheelMap != null ? guWheelMap.get("wheelCount") : 0);
                json.put("needPlayCount", needPlayCount);
                json.put("creditPool", groupDaoNew.loadCreditWheelPool(groupId));
                OutputUtil.output(0, json, getRequest(), getResponse(), false);
            } else if (opType == 2) { //抽奖
                int wheelCount = guWheelMap == null ? 0 :(int)guWheelMap.get("wheelCount");
                if(wheelCount <= 0){
                    OutputUtil.output(6, "没有抽奖资格", getRequest(), getResponse(), false);
                    return;
                }
                boolean needSendMarquee = false;
                int prize = 0;
                synchronized(getGroupLock(groupId)){
                    GroupCreditWheel gcw = groupDaoNew.loadGroupCreditWheel(groupId);
                    if(gcw == null){
                        OutputUtil.output(7, "活动未开启", getRequest(), getResponse(), false);
                        return;
                    }
                    boolean resetNextWin = false;
                    if(gcw.getNextWin() == 1 && gcw.getBiggestPrize() <= gcw.getCreditPool()){      //直接中最大奖
                        prize = gcw.getBiggestPrize();
                        resetNextWin = true;
                    } else {        //摇奖
                        prize = MathUtil.draw(gcw.getDrawMap());
                        if(prize > gcw.getCreditPool()){  //超过奖池的直接谢谢惠顾
                            prize = 0;
                        }
                    }
                    int transferResult = 0;
                    if(prize > 0) {
                        transferResult = groupDao.transferWheelToGroupUserCredit(userId, groupId, prize);
                        if( transferResult == 2) {
                            // 写入日志
                            HashMap<String, Object> logFrom = new HashMap<>();
                            logFrom.put("groupId", groupId);
                            logFrom.put("optUserId", userId);
                            logFrom.put("userId", userId);
                            logFrom.put("tableId", 0);
                            logFrom.put("credit", prize);
                            logFrom.put("type", GroupConstants.CREDIT_LOG_TYPE_WHEEL);
                            logFrom.put("flag", 1);
                            logFrom.put("userGroup", 0);
                            logFrom.put("mode", 1);
                            groupDaoNew.insertGroupCreditLog(logFrom);
                            if (regInfo.getEnterServer() > 0) {
                                GameUtil.sendCreditUpdate(regInfo.getEnterServer(), userId, groupId);
                            }

                            HashMap<String, Object> wheelMap = new HashMap<>();
                            wheelMap.put("groupId", groupId);
                            wheelMap.put("totalPayAdd", prize);
                            if(resetNextWin){       //重置nextWin
                                wheelMap.put("nextWin", 0);
                            }
                            groupDaoNew.updateCreditWheel(wheelMap);
                            if (prize == gcw.getBiggestPrize())
                            {
                                needSendMarquee = true;     //大奖推送跑马灯
                            }
                        } else {
                            OutputUtil.output(9, "抽奖出错！！", getRequest(), getResponse(), false);
                            return;
                        }
                    }
                    groupDaoNew.updateUserWheelCount(wheelCount-1, groupUser.getKeyId());
                    OutputUtil.output(0, prize, getRequest(), getResponse(), false);
                }
                if(needSendMarquee) {    //推送跑马灯
                    GameUtil.sendMarquee(0, userId, groupId, "恭喜玩家" + regInfo.getName() + "在幸运转盘中抽到大奖" + prize / 100 + "分！", 1);
                }

            }

            LOGGER.info("creditWheel|params|" + params);
        } catch (Exception e) {
            LOGGER.error("creditWheel|error|" + JSON.toJSONString(params), e.getMessage(), e);
            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        }
    }

}
