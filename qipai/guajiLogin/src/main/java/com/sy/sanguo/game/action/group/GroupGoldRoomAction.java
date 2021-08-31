package com.sy.sanguo.game.action.group;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sy.mainland.util.CommonUtil;
import com.sy.mainland.util.PropertiesCacheUtil;
import com.sy.sanguo.common.struts.GameStrutsAction;
import com.sy.sanguo.common.util.*;
import com.sy.sanguo.common.util.user.GameUtil;
import com.sy.sanguo.game.bean.Activity;
import com.sy.sanguo.game.bean.RegInfo;
import com.sy.sanguo.game.bean.gold.GoldRoom;
import com.sy.sanguo.game.bean.group.GroupCommissionConfig;
import com.sy.sanguo.game.bean.group.GroupGoldCommissionConfig;
import com.sy.sanguo.game.bean.group.GroupInfo;
import com.sy.sanguo.game.bean.group.GroupReview;
import com.sy.sanguo.game.bean.group.GroupTable;
import com.sy.sanguo.game.bean.group.GroupUser;
import com.sy.sanguo.game.bean.group.GroupUserLog;
import com.sy.sanguo.game.bean.group.GroupUserReject;
import com.sy.sanguo.game.bean.group.LogGroupUserAlert;
import com.sy.sanguo.game.bean.group.SysGroupLevelConfig;
import com.sy.sanguo.game.bean.group.SysGroupUserLevelConfig;
import com.sy.sanguo.game.constants.GroupConstants;
import com.sy.sanguo.game.constants.GroupUserTree;
import com.sy.sanguo.game.dao.ActivityDao;
import com.sy.sanguo.game.dao.DataStatisticsDao;
import com.sy.sanguo.game.dao.UserDaoImpl;
import com.sy.sanguo.game.dao.group.GroupDao;
import com.sy.sanguo.game.dao.group.GroupDaoNew;
import com.sy.sanguo.game.dao.group.GroupGoldRoomDao;
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
public class GroupGoldRoomAction extends GameStrutsAction {

    private static final Logger LOGGER = LoggerFactory.getLogger("sys");

    private static final int min_group_id = 1000;
    private UserDaoImpl userDao;
    private GroupDao groupDao;
    private GroupDaoNew groupDaoNew;
    private GroupGoldRoomDao groupGoldRoomDao;
    private DataStatisticsDao dataStatisticsDao;

    private static final Map<String, Object> lockMap = new ConcurrentHashMap<>();

    public Object getUserGroupLock(long groupId, long userId) {
        String key = groupId + "_" + userId;
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

    public void setGroupGoldRoomDao(GroupGoldRoomDao groupGoldRoomDao) {
        this.groupGoldRoomDao = groupGoldRoomDao;
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
     * 分成
     * 推广管理
     */
    public void goldTeamList() {
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
                OutputUtil.output(-2, "身份信息验证失败", getRequest(), getResponse(), false);
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
            int teamCount = groupGoldRoomDao.countTeamList(groupId, target.getPromoterLevel(), target.getUserId(), keyWord);
            if (StringUtils.isBlank(keyWord)) {
                teamCount++;
            }
            List<Map<String, Object>> teamList = new ArrayList<>();
            if (teamCount > 0 && target.getPromoterLevel() != 10) {
                teamList = groupGoldRoomDao.teamList(groupId, target.getPromoterLevel(), target.getUserId(), keyWord, pageNo, pageSize);
            }
            if (teamList != null && teamList.size() > 0) {
                for (Map<String, Object> team : teamList) {
                    team.put("canSet", 1); // 是否可设置赠送分成
                    if (team.get("userId").toString().equals(target.getUserId().toString())) {
                        team.put("canSet", 0);
                    }
                }
            }
            json.put("teamList", teamList);
            json.put("viewTeamUser", 1);
            json.put("total", teamCount);
            json.put("pages", (int) Math.ceil(teamCount * 1.0 / pageSize));
            OutputUtil.output(0, json, getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("teamList|error|" + JSON.toJSONString(params), e.getMessage(), e);
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
    private void updateGroupUserExt(GroupUser groupUser, String key, String value) throws Exception {
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
     * 金币场
     * 分成配置列表
     */
    public void loadGoldCommissionConfig() {
        Map<String, String> params = null;
        try {
            params = UrlParamUtil.getParameters(getRequest());
            LOGGER.debug("loadGoldCommissionConfig|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), 0);
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, "身份信息验证失败", getRequest(), getResponse(), false);
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
                OutputUtil.output(1, "参数错误：成员或副会长", getRequest(), getResponse(), false);
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
            json.put("dataList", loadGoldCommissionConfig(self, target));
            OutputUtil.output(0, json, getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("loadGoldCommissionConfig|error|" + JSON.toJSONString(params), e.getMessage(), e);
            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        }
    }

    private List<Map<String, Object>> loadGoldCommissionConfig(GroupUser groupUser, GroupUser target) throws Exception {
        long groupId = groupUser.getGroupId();
        Map<Integer, GroupGoldCommissionConfig> configMap = new HashMap<>();
        List<GroupGoldCommissionConfig> creditConfigList = groupGoldRoomDao.loadGoldCommissionConfig(groupId, target.getUserId());
        if (creditConfigList != null && !creditConfigList.isEmpty()) {
            for (GroupGoldCommissionConfig c : creditConfigList) {
                configMap.put(c.getSeq(), c);
            }
        }

        List<GroupGoldCommissionConfig> preConfigList = null;
        Map<Integer, GroupGoldCommissionConfig> preConfigMap = new HashMap<>();
        List<Map<String, Object>> dataList = new ArrayList<>();
        if (!groupUser.getUserId().equals(target.getUserId())) {
            long preUserId = target.getPromoterId();
            if (preUserId > 0) {
                preConfigList = groupGoldRoomDao.loadGoldCommissionConfig(groupId, preUserId);
            }
            if (preConfigList != null && !preConfigList.isEmpty()) {
                for (GroupGoldCommissionConfig c : preConfigList) {
                    preConfigMap.put(c.getSeq(), c);
                }
            }
            int count = GroupConstants.SYS_GOLD_COMMISSION_LOG_MAP.size();
            for (int seq = 1; seq <= count; seq++) {
                GroupGoldCommissionConfig sysConfig = GroupConstants.getSysGoldCommissionConfig(seq);
                GroupGoldCommissionConfig selfConfig = configMap.get(seq);
                if (selfConfig == null) {
                    selfConfig = sysConfig;
                }
                Map<String, Object> data = new HashMap<>();
                data.put("minValue", selfConfig.getMinValue());
                data.put("maxValue", selfConfig.getMaxValue());
                data.put("myValue", selfConfig.getValue());
                data.put("logValue", selfConfig.getMaxLog());
                if (preConfigMap.containsKey(seq)) {
                    GroupGoldCommissionConfig preConfig = preConfigMap.get(seq);
                    data.put("preValue", sysConfig.getMaxValue() - preConfig.getLeftValue());   // 上级已使用的分
                    data.put("myMaxValue", preConfig.getLeftValue());    // 我可设置的最大分
                } else {
                    data.put("preValue", 0);
                    data.put("myMaxValue", sysConfig.getMaxValue());
                }
                dataList.add(data);
            }
        } else {
            // 自己查自己的，不需要查看上级
            int count = GroupConstants.SYS_GOLD_COMMISSION_LOG_MAP.size();
            for (int seq = 1; seq <= count; seq++) {
                GroupGoldCommissionConfig selfConfig = configMap.get(seq);
                if (selfConfig == null) {
                    GroupGoldCommissionConfig sysConfig = GroupConstants.getSysGoldCommissionConfig(seq);
                    selfConfig = sysConfig;
                }
                Map<String, Object> data = new HashMap<>();
                data.put("minValue", selfConfig.getMinValue());
                data.put("maxValue", selfConfig.getMaxValue());
                data.put("preValue", selfConfig.getMaxValue() - selfConfig.getLeftValue());
                dataList.add(data);
            }
        }

        return dataList;
    }


    /**
     * 修改金币场分成
     */
    public void updateGoldCommissionConfig() {
        Map<String, String> params = null;
        try {
            params = UrlParamUtil.getParameters(getRequest());
            LOGGER.debug("updateGoldCommissionConfig|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), 0);
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, "身份信息验证失败", getRequest(), getResponse(), false);
                return;
            }

            long targetUserId = NumberUtils.toLong(params.get("targetUserId"), 0);
            long groupId = NumberUtils.toInt(params.get("groupId"), -1);
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
            if (!GroupConstants.isHuiZhangOrFuHuiZhang(self.getUserRole())) {
                if (!GroupConstants.isUnder(self, target)) {
                    OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_7), getRequest(), getResponse(), false);
                    return;
                }
            }

            String[] valueArr = values.split(",");
            List<GroupGoldCommissionConfig> selfConfigList = groupGoldRoomDao.loadGoldCommissionConfig(groupId, targetUserId);
            Map<Integer, GroupGoldCommissionConfig> selfConfigMap = new HashMap<>();
            if (selfConfigList != null && !selfConfigList.isEmpty()) {
                for (GroupGoldCommissionConfig c : selfConfigList) {
                    selfConfigMap.put(c.getSeq(), c);
                }
            }

            Map<Integer, GroupGoldCommissionConfig> preConfigMap = new HashMap<>();
            long preUserId = target.getPromoterId();
            if (preUserId != 0) {
                List<GroupGoldCommissionConfig> preConfigList = groupGoldRoomDao.loadGoldCommissionConfig(groupId, preUserId);
                if (preConfigList != null && !preConfigList.isEmpty()) {
                    for (GroupGoldCommissionConfig c : preConfigList) {
                        preConfigMap.put(c.getSeq(), c);
                    }
                }
            }

            Date now = new Date();
            for (int seq = 1; seq <= valueArr.length; seq++) {
                GroupGoldCommissionConfig sysConfig = GroupConstants.getSysGoldCommissionConfig(seq);
                GroupGoldCommissionConfig selfConfig = selfConfigMap.get(seq);
                long value = Long.valueOf(valueArr[seq - 1]);
                long maxLog = 0;
                long leftValue = sysConfig.getMaxValue() - value;
                if (preConfigMap.containsKey(seq)) {
                    maxLog = preConfigMap.get(seq).getValue();
                    leftValue = preConfigMap.get(seq).getLeftValue() - value;
                    if (leftValue < 0) {
                        leftValue = 0;
                    }
                }
                if (selfConfig == null) {
                    selfConfig = GroupConstants.genGroupGoldCommissionConfig(sysConfig.getSeq(), sysConfig.getMinValue(), sysConfig.getMaxValue());
                    selfConfig.setGroupId(groupId);
                    selfConfig.setUserId(targetUserId);
                    selfConfig.setValue(value);
                    selfConfig.setLeftValue(leftValue);
                    selfConfig.setMaxLog(maxLog);
                    selfConfig.setCreatedTime(now);
                    selfConfig.setLastUpTime(now);
                    groupGoldRoomDao.insertGoldCommissionConfig(selfConfig);
                } else {
                    if (value != selfConfig.getValue()) {
                        groupGoldRoomDao.updateGoldCommissionConfig(groupId, targetUserId, seq, value, leftValue, maxLog);
                    }
                }
            }
            JSONObject json = new JSONObject();
            json.put("msg", LangMsg.getMsg(LangMsg.code_0));
            OutputUtil.output(0, json, getRequest(), getResponse(), false);

            //记录玩家已经修改过此值
            updateGroupUserExt(target, GroupConstants.extKey_goldCommissionConfig, "1");

            LOGGER.info("updateGoldCommissionConfig|succ|params:{}", params);
        } catch (Exception e) {
            LOGGER.error("updateGoldCommissionConfig|error|" + JSON.toJSONString(params), e.getMessage(), e);
            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        }
    }

    /**
     * 金币场：
     * 战绩查询
     */
    public void loadGoldTablePlayLogs() {
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
                OutputUtil.output(-2, "身份信息验证失败", getRequest(), getResponse(), false);
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

            List<GoldRoom> goldRooms = null;
            int count = groupGoldRoomDao.getUserGoldTableIdCount(groupId, promoterId, promoterLevel, queryUserId, queryTableId, currentState, playType, startDate, endDate);
            if (count > 0) {
                goldRooms = groupGoldRoomDao.getUserPlayLogGroupTable(groupId, promoterId, promoterLevel, queryUserId, queryTableId, currentState, playType, startDate, endDate, pageNo, pageSize);
            }

            JSONObject json = new JSONObject();
            List<Map<String, Object>> results = new ArrayList<>();
            if (count == 0 || goldRooms == null || goldRooms.isEmpty()) {
                json.put("list", results);
                json.put("tables", count);
                OutputUtil.output(0, json, getRequest(), getResponse(), false);
                return;
            }
            StringJoiner roomIds = new StringJoiner(",");
            for (GoldRoom goldRoom : goldRooms) {
                roomIds.add(goldRoom.getKeyId().toString());
            }
            List<HashMap<String, Object>> users = groupGoldRoomDao.loadGoldRoomUserByTableId(roomIds.toString(), groupId);
            Map<String, List<Map<String, Object>>> tableUserMap = new HashMap<>();
            List<String> userIdList = new ArrayList<>();
            StringJoiner userIds = new StringJoiner(",");
            for (Map<String, Object> userData : users) {
                String uId = String.valueOf(userData.get("userId"));
                if (!userIdList.contains(uId)) {
                    userIds.add(String.valueOf(userData.get("userId")));
                }
                String tableId = String.valueOf(userData.get("roomId"));
                if (!tableUserMap.containsKey(tableId)) {
                    List<Map<String, Object>> list = new ArrayList<>();
                    tableUserMap.put(tableId, list);
                }
                tableUserMap.get(tableId).add(userData);
            }

            SimpleDateFormat hfm = new SimpleDateFormat("HH:mm:ss");
            for (GoldRoom goldRoom : goldRooms) {
                Map<String, Object> map = new HashMap<>();
                map.put("overTime", hfm.format(goldRoom.getModifiedTime()));
                map.put("tableId", goldRoom.getTableId());
                map.put("goldRoomId", goldRoom.getKeyId());
                List<Map<String, Object>> playerList = new ArrayList<>();
                List<Map<String, Object>> list = tableUserMap.get(String.valueOf(goldRoom.getKeyId()));
                if (list != null) {
                    for (Map<String, Object> userMap : list) {
                        if (userMap.containsKey("userId")) {
                            Map<String, Object> pMap = new HashMap<>();
                            pMap.put("name", userMap.get("name"));
                            pMap.put("gameResult", userMap.get("gameResult"));
                            playerList.add(pMap);
                        }
                    }
                }
                map.put("playerList", playerList);
                map.put("playedBureau", 1);
                map.put("roomName", goldRoom.getTableName());
                map.put("tableMsg", goldRoom.getTableMsg());
                map.put("goldMsg", goldRoom.getGoldMsg());
                results.add(map);
            }
            json.put("list", results);
            json.put("tables", count);
            OutputUtil.output(0, json, getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 战绩详情
     */
    public void loadGoldTableRecord() {
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
                OutputUtil.output(-2, "身份信息验证失败", getRequest(), getResponse(), false);
                return;
            }

            Long goldRoomId = NumberUtils.toLong(params.get("goldRoomId"), -1);
            if (goldRoomId <= 0) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            List<HashMap<String, Object>> list = groupGoldRoomDao.loadGoldRoomTableRecord(goldRoomId);
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

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("code", 0);
                jsonObject.put("playLog", list1 == null ? "" : list1);
                jsonObject.put("modeMsg", modeMsg);
                jsonObject.put("resultMsg", resultMsg);
                OutputUtil.output(jsonObject, getRequest(), getResponse(), null, false);
            } else {
                OutputUtil.output(0, Collections.emptyList(), getRequest(), getResponse(), false);
            }

        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }


    /**
     * 群统计
     * 推广统计
     */
    public void goldCommissionLog() {
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
                OutputUtil.output(-2, "身份信息验证失败", getRequest(), getResponse(), false);
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
            List<HashMap<String, Object>> dataList = groupGoldRoomDao.goldCommissionLog(groupId, self.getUserId(), self.getPromoterLevel(), dataDate);
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
     * 推广统计
     * 查看下级
     */
    public void goldCommissionLogNextLevel() {
        Map<String, String> params = null;
        try {
            params = UrlParamUtil.getParameters(getRequest());
            LOGGER.debug("goldCommissionLogNextLevel|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), -1);
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, "身份信息验证失败", getRequest(), getResponse(), false);
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

            int count = groupGoldRoomDao.countGoldCommissionLogNextLevel(groupId, target.getUserId(), target.getPromoterLevel(), queryUserId);
            List<HashMap<String, Object>> dataList = Collections.emptyList();
            if (count > 0) {
                dataList = groupGoldRoomDao.goldCommissionLogNextLevel(groupId, target.getUserId(), target.getPromoterLevel(), dataDate, queryUserId, pageNo, pageSize);
            }
            JSONObject json = new JSONObject();
            json.put("pageNo", pageNo);
            json.put("pageSize", pageSize);
            json.put("total", count);
            json.put("pages", (int) Math.ceil(count * 1.0 / pageSize));
            json.put("dataList", dataList);
            OutputUtil.output(0, json, getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("goldCommissionLogNextLevel|error|" + JSON.toJSONString(params), e.getMessage(), e);
            OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_4), getRequest(), getResponse(), false);
        }
    }

    /**
     * 成员统计
     */
    public void goldRankList() {
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
                OutputUtil.output(-2, "身份信息验证失败", getRequest(), getResponse(), false);
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
            int count = groupGoldRoomDao.countRankList(groupId, target.getUserId(), target.getPromoterLevel(), startDate, endDate, queryUserId, optType);
            List<HashMap<String, Object>> dataList = Collections.emptyList();
            HashMap<String, Object> sumData = null;
            if (count > 0) {
                dataList = groupGoldRoomDao.rankList(groupId, target.getUserId(), target.getPromoterLevel(), startDate, endDate, rankField, rankType, queryUserId, optType, pageNo, pageSize);
                if (dataList != null && dataList.size() > 0 && queryUserId == -1) {
                    sumData = groupGoldRoomDao.rankSum(groupId, target.getUserId(), target.getPromoterLevel(), startDate, endDate, optType);
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
     * SOLO
     * 战绩详情
     */
    public void loadSoloRoomTableRecord() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("loadSoloRoomTableRecord|params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            long userId = NumberUtils.toLong(params.get("userId"), 0);
            RegInfo user = checkSessCodeNew(userId, params.get("sessCode"));
            if (user == null) {
                OutputUtil.output(-2, "身份信息验证失败", getRequest(), getResponse(), false);
                return;
            }
            long queryUserId = NumberUtils.toLong(params.get("queryUserId"), 0);
            String startDate = params.get("startDate");
            String endDate = params.get("endDate");
            if (!TimeUtil.checkDateFormat(startDate) || !TimeUtil.checkDateFormat(endDate)) {
                OutputUtil.output(3, "日期格式错误：" + startDate + "," + endDate, getRequest(), getResponse(), false);
                return;
            }
            int pageNo = NumberUtils.toInt(params.get("pageNo"), 1);
            int pageSize = NumberUtils.toInt(params.get("pageSize"), 30);

            int count = groupGoldRoomDao.countSoloRoomTableRecord(userId, queryUserId, startDate, endDate);
            List<Map<String, Object>> dataList;
            if (count > 0) {
                dataList = groupGoldRoomDao.getSoloRoomTableRecord(userId, queryUserId, startDate, endDate, pageNo, pageSize);
            }else{
                dataList = Collections.emptyList();
            }
            JSONObject json = new JSONObject();
            json.put("dataList",dataList);
            json.put("totalCount", count);
            OutputUtil.output(0, json, getRequest(), getResponse(), false);
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

}
