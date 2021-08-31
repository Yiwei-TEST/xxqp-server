package com.sy.sanguo.game.action.group;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sy.mainland.util.CommonUtil;
import com.sy.mainland.util.PropertiesCacheUtil;
import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.struts.GameStrutsAction;
import com.sy.sanguo.common.util.*;
import com.sy.sanguo.common.util.user.GameUtil;
import com.sy.sanguo.game.bean.RegInfo;
import com.sy.sanguo.game.bean.Server;
import com.sy.sanguo.game.bean.group.*;
import com.sy.sanguo.game.dao.DataStatisticsDao;
import com.sy.sanguo.game.dao.UserDaoImpl;
import com.sy.sanguo.game.dao.group.GroupDao;
import com.sy.sanguo.game.dao.group.GroupDaoManager;
import com.sy.sanguo.game.service.SysInfManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.*;

/**
 * 俱乐部(牛牛)
 */
public class NewGroupAction extends GameStrutsAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(NewGroupAction.class);

    private UserDaoImpl userDao;
    private GroupDao groupDao;
    private DataStatisticsDao dataStatisticsDao;
    private static final int min_group_id = 1000;

    /**
     * 创建军团(包厢)
     */
    public void createGroup() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            String groupName = params.get("groupName");
            int groupLevel = NumberUtils.toInt(params.get("groupLevel"), 1);
            long userId = NumberUtils.toLong(params.get("userId"), 0);
            int subId = NumberUtils.toInt(params.get("subId"), 0);
            int gId = NumberUtils.toInt(params.get("groupId"), 0);
            String wanfaIds = params.get("allWanfas");
            if (StringUtils.isBlank(groupName)) {
                OutputUtil.output(1, "请输入俱乐部名字！", getRequest(), getResponse(), false);
                return;
            } else if (userId <= 0 || subId < 0) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
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
            GroupConfig groupConfig = null;
            if (subId == 0) {
                groupConfig = groupDao.loadGroupConfig(groupLevel);
                if (groupConfig == null) {
                    OutputUtil.output(1, "俱乐部等级配置错误", getRequest(), getResponse(), false);
                    return;
                }
            }
            RegInfo user = userDao.getUser(userId);
            if (user == null) {
                OutputUtil.output(2, "玩家ID错误", getRequest(), getResponse(), false);
                return;
            } else if (subId == 0 && user.getCards() + user.getFreeCards() < groupConfig.getGroupCoin().longValue()) {
                OutputUtil.output(3, "钻石不足", getRequest(), getResponse(), false);
                return;
            }
            int groupCount = groupDao.loadGroupCount(userId);
            if (groupCount >= 20) {
                OutputUtil.output(3, "最多只能创建或加入20个俱乐部！", getRequest(), getResponse(), false);
                return;
            }
            if (subId == 0 && groupConfig != null) {
                String createGroupNeedCards = PropertiesCacheUtil.getValue("createGroupNeedCards",Constants.GAME_FILE);
                if (StringUtils.isNotBlank(createGroupNeedCards)) {
                    String[] temps = createGroupNeedCards.split(",");
                    int groupCount0 = groupDao.loadMyGroupCount(userId);
                    if (user.getCards() + user.getFreeCards() < Integer.parseInt(temps.length >= (groupCount0 + 1) ? temps[groupCount0] : temps[temps.length - 1])) {
                        OutputUtil.output(3, "创建失败！(" + (temps.length >= (groupCount0 + 1) ? temps[groupCount0] : temps[temps.length - 1]) + "钻石可免费创建第" + (groupCount0 + 1) + "个俱乐部)", getRequest(), getResponse(), false);
                        return;
                    }
                } else {
                    int groupCoin = groupConfig.getGroupCoin().intValue();
                    if (groupCoin <= 0) {
                        int groupCount0 = groupDao.loadMyGroupCount(userId);
                        if (user.getCards() + user.getFreeCards() < -groupCoin * (groupCount0 + 1)) {
                            OutputUtil.output(3, "创建失败！(" + (-groupCoin * (groupCount0 + 1)) + "钻石可免费创建第" + (groupCount0 + 1) + "个俱乐部)", getRequest(), getResponse(), false);
                            return;
                        }
                    }
                }
            }
            int count = 0;
            GroupUser groupUser;
            GroupInfo groupInfo;
            synchronized (NewGroupAction.class) {
                groupUser = groupDao.loadGroupUser(userId, gId);
                if (subId == 0 && groupUser != null) {
//                    OutputUtil.output(6, "已加入军团:" + groupUser.getGroupName(), getRequest(), getResponse(), false);
//                    return;
                } else if (subId > 0) {
                    if (groupUser == null) {
                        OutputUtil.output(7, "尚未加入军团", getRequest(), getResponse(), false);
                        return;
                    } else if (groupUser.getUserRole().intValue() > 1) {
                        OutputUtil.output(9, LangMsg.getMsg(LangMsg.code_10), getRequest(), getResponse(), false);
                        return;
                    }
                }

                if (subId > 0) {
                    count = groupDao.countSubGroup(groupUser.getGroupId().toString());
                    if (count >= 50) {
                        OutputUtil.output(8, "包厢数量超过上限", getRequest(), getResponse(), false);
                        return;
                    }
                }
                if (subId == 0) {
                    SecureRandom random = new SecureRandom();
                    int base = 9000;
                    int groupId = min_group_id + random.nextInt(base);//groupDao.loadMaxGroupId();
                    boolean canCreate = false;
                    int c = 0;
                    while (c < 3) {
                        c++;
                        if (groupDao.existsGroupInfo(groupId, 0)) {
                            groupId = min_group_id * c * 10 + random.nextInt(base * c * 10);
                        } else {
                            canCreate = true;
                            break;
                        }
                    }
                    if (!canCreate) {
                        OutputUtil.output(5, "请稍后再试", getRequest(), getResponse(), false);
                        return;
                    }
                    groupInfo = new GroupInfo();
                    groupInfo.setCreatedTime(new Date());
                    groupInfo.setCreatedUser(userId);
                    groupInfo.setCurrentCount(1);
                    groupInfo.setDescMsg("");
                    groupInfo.setGroupId(groupId);
                    JSONObject jsonObject = new JSONObject();
                    String defaultStr = PropertiesCacheUtil.getValueOrDefault("group_kf_default", "",Constants.GAME_FILE);
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
                    if (!StringUtils.isBlank(wanfaIds)) {
                        jsonObject.put("wanfaIds", wanfaIds);
                    }
                    groupInfo.setExtMsg(jsonObject.toString());
                    groupInfo.setGroupLevel(groupConfig.getGroupLevel());
                    groupInfo.setGroupMode(0);
                    groupInfo.setGroupName(groupName);
                    groupInfo.setMaxCount(groupConfig.getMaxCount());
                    groupInfo.setParentGroup(0);
                    groupInfo.setGroupState("1");
                    groupInfo.setModifiedTime(groupInfo.getCreatedTime());
                    if(groupDao.createGroup(groupInfo)<=0){
                        OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1002, "操作失败，请稍后再试"), getRequest(), getResponse(), null, false);
                        return;
                    }
                    if (subId == 0) {// 初始化俱乐部成员数据
                        groupUser = new GroupUser();
                        groupUser.setCreatedTime(new Date());
                        groupUser.setGroupId(groupInfo.getGroupId());
                        groupUser.setGroupName(groupInfo.getGroupName());
                        groupUser.setInviterId(userId);
                        groupUser.setPlayCount1(0);
                        groupUser.setPlayCount2(0);
                        groupUser.setUserId(userId);
                        groupUser.setUserLevel(1);
                        groupUser.setUserRole(0);
                        groupUser.setUserName(user.getName());
                        groupUser.setUserNickname(user.getName());
                        groupUser.setUserGroup("0");
                        groupUser.setCredit(0l);
                        if(groupDao.createGroupUser(groupUser)<=0){
                            groupDao.deleteGroupInfoByGroupId(groupInfo.getGroupId(),0);
                            return;
                        }

                        LOGGER.info("create groupUser success:{}", JSON.toJSONString(groupUser));

                        if (groupConfig.getGroupCoin().intValue() > 0) {
                            int ret = userDao.consumeUserCards(user, groupConfig.getGroupCoin().intValue());

                            LOGGER.info("create group consumeUserCards ret:{},userId:{}", ret, user.getUserId());
                        }
                    }
                    MessageBuilder msgBuilder = MessageBuilder.newInstance().builderCodeMessage(0, "创建成功");
                    msgBuilder.builder("groupId", groupInfo.getGroupId());
                    GroupDaoManager.getInstance().createEmptyTableConfigs(groupId, 1,10);
                    GroupDaoManager.getInstance().createEmptyTableConfigs(groupId, 2,10);
                    OutputUtil.output(msgBuilder, getRequest(), getResponse(), null, false);
                    LOGGER.info("create groupInfo success:{}", JSON.toJSONString(groupInfo));
                } else {
                    if (subId<=0||subId>=1000000){
                        OutputUtil.output(4, "大侠，请换个包厢序号吧", getRequest(), getResponse(), false);
                        return;
                    }
                    GroupInfo exitGroupInfo = groupDao.loadGroupInfoAll(subId,groupUser.getGroupId());
                    if(exitGroupInfo == null) {
                        groupInfo = new GroupInfo();
                        groupInfo.setCreatedTime(new Date());
                        groupInfo.setCreatedUser(userId);
                        groupInfo.setCurrentCount(1);
                        groupInfo.setDescMsg("");
                        groupInfo.setGroupId(subId);
                        groupInfo.setParentGroup(groupUser.getGroupId());
                        groupInfo.setExtMsg("");
                        groupInfo.setGroupLevel(0);
                        groupInfo.setGroupMode(0);
                        groupInfo.setGroupName(groupName0);
                        groupInfo.setMaxCount(0);
                        groupInfo.setGroupState("1");
                        groupInfo.setModifiedTime(groupInfo.getCreatedTime());
                        if (groupDao.createGroup(groupInfo) <= 0) {
                            OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(1002, "操作失败，请稍后再试"), getRequest(), getResponse(), null, false);
                            return;
                        }
                    }else{
                        groupInfo = exitGroupInfo;
                        HashMap<String,Object> map = new HashMap<>();
                        map.put("keyId",groupInfo.getKeyId());
                        map.put("groupName",groupName);
                        map.put("createdUser",userId);
                        map.put("createdTime",new Date());
                        map.put("maxCount",0);
                        map.put("groupState","1");
                        map.put("ModifiedTime",groupInfo.getCreatedTime());
                        groupDao.updateGroupInfoByKeyId(map);
                    }
                    OutputUtil.output(MessageBuilder.newInstance().builderCodeMessage(0, "包厢创建成功").builder("groupId", groupInfo.getParentGroup()), getRequest(), getResponse(), null, false);
                    LOGGER.info("create groupSubInfo success:{},total={}", JSON.toJSONString(groupInfo), count + 1);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 获取军团房间信息
     */
    public void loadGroupTables() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            String userId = params.get("oUserId");
            long groupId = NumberUtils.toLong(params.get("groupId"), -1);
            int pageNo = NumberUtils.toInt(params.get("pageNo"), 1);
            int pageSize = NumberUtils.toInt(params.get("pageSize"), 30);
            if (groupId <= 0 || pageNo < 1 || pageSize < 1) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            GroupUser groupUser = groupDao.loadGroupUser(Long.parseLong(userId), groupId);
            if (groupUser == null) {
                OutputUtil.output(2, "您还没有加入军团", getRequest(), getResponse(), false);
                return;
            }
            if (pageSize > 50) {
                pageSize = 50;
            }
            // 目前获取所有
            List<GroupTableConfig> tableConfigs = groupDao.loadGroupTableConfigByPage((int)groupId, 0, pageNo, pageSize);
            if(tableConfigs == null || tableConfigs.isEmpty()) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            MessageBuilder msgBuilder = MessageBuilder.newInstance();
            msgBuilder.builder("code", 0);
            msgBuilder.builder("groupId", groupId);
            msgBuilder.builder("pageNo", pageNo);
            msgBuilder.builder("pageSize", pageSize);
            Map<Long, GroupTableConfig> tableConfigMap = new HashMap<>();
            StringBuilder strBuilder = new StringBuilder();
            for(GroupTableConfig tableConfig : tableConfigs) {
                tableConfigMap.put(tableConfig.getKeyId(), tableConfig);
                strBuilder.append(",\"").append(tableConfig.getKeyId()).append("\"");
            }
            strBuilder.deleteCharAt(0);
            JSONArray ja = new JSONArray();
            List<GroupTable> list = groupDao.loadGroupTablesByConfigIds(groupId, strBuilder.toString());
            if (list != null) {
                for(GroupTable groupTable : list) {
                    JSONObject jo = groupTable.getJsonObj();
                    List<HashMap<String, Object>> members = groupTable.getCurrentCount().intValue() <= 0 ? null : groupDao.loadTableUserInfo(groupTable.getKeyId().toString(),groupTable.getGroupId());
                    if (members == null || members.size() == 0) {
                        jo.put("members", Collections.emptyList());
                    } else {
                        List<Map<String, Object>> members0 = new ArrayList<>(members.size());
                        for (HashMap<String, Object> map : members) {
                            Map<String, Object> map0 = userDao.loadUserBase(String.valueOf(map.get("userId")));
                            if (map0 != null) {
                                members0.add(map0);
                            }
                        }
                        jo.put("members", members0);
                    }
                    jo.put("tableMode", tableConfigMap.get(groupTable.getConfigId()).getTableMode());
                    jo.put("tableMsg", tableConfigMap.get(groupTable.getConfigId()).getModeMsg());// 玩法信息
                    ja.add(jo);
                    tableConfigMap.remove(groupTable.getConfigId());
                }
            }
            if(!tableConfigMap.isEmpty()) {// 余下的空桌子
                for(GroupTableConfig tableConfig : tableConfigMap.values()) {
                    JSONObject jo = new JSONObject();
                    jo.put("configId", tableConfig.getKeyId());// 房间索引号
                    jo.put("tableMsg", tableConfig.getModeMsg());// 房间配置
                    jo.put("tableMode", tableConfig.getTableMode());
                    ja.add(jo);
                }
            }
            msgBuilder.builder("list", ja);
            if(groupUser.getUserRole().intValue() <= 1) {// 是否有玩家申请加入俱乐部消息
                List<GroupReview> groupReviews = groupDao.loadGroupReviewByGroupId(groupId);
                if(groupReviews != null && !groupReviews.isEmpty())
                    msgBuilder.builder("newMsg", 1);
                else
                    msgBuilder.builder("newMsg", 0);
            } else
                msgBuilder.builder("newMsg", 0);
            OutputUtil.output(msgBuilder, getRequest(), getResponse(), null, false);
        } catch (Exception e) {
            System.err.println("Exception:" + e.getMessage());
        }
    }

    /**
     * 设置玩法
     */
    public void createTableConfig() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            int configId = NumberUtils.toInt(params.get("configId"), -1);// 桌子索引号
            int gameType = NumberUtils.toInt(params.get("gameType"), -1);// 玩法
            int payType = NumberUtils.toInt(params.get("payType"), -1);// 付费方式
            int gameCount = NumberUtils.toInt(params.get("gameCount"), -1);// 局数
            int playerCount = NumberUtils.toInt(params.get("playerCount"), -1);// 人数
            String modeMsg = params.get("modeMsg");// 牌局详细参数
            String tableName = params.get("tableName");// 房间名
            String descMsg = params.get("descMsg");
            String tableMode = params.get("tableMode");// table类型 1普通房 2比赛房
            int tableOrder = NumberUtils.toInt(params.get("tableOrder"), 1);
            long userId = NumberUtils.toLong(params.get("userId"), -1);
            int subId = NumberUtils.toInt(params.get("subId"), -1);
            if (tableOrder <= 0 || tableOrder >= 1000000) {
                OutputUtil.output(1, "牌局序号错误", getRequest(), getResponse(), false);
                return;
            } else if (gameType <= -1 || payType <= -1 || gameCount <= 0 || playerCount <= 0 || userId <= 0 || subId < 0) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            } else if (StringUtils.isBlank(modeMsg)) {
                OutputUtil.output(2, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            long groupId = NumberUtils.toLong(params.get("groupId"), -1);
            GroupUser groupUser = groupDao.loadGroupUser(userId, groupId);
            if (groupUser == null) {
                OutputUtil.output(3, "尚未加入军团", getRequest(), getResponse(), false);
                return;
            }
            if (groupUser.getUserRole().intValue() > 1) {
                OutputUtil.output(4, LangMsg.getMsg(LangMsg.code_10), getRequest(), getResponse(), false);
                return;
            }

            synchronized (NewGroupAction.class) {
                GroupTableConfig groupTableConfig = groupDao.loadGroupTableConfig(configId);
                if(groupTableConfig == null) {
                    OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                    return;
                }
                if(StringUtils.isNotBlank(groupTableConfig.getModeMsg())) {
                    OutputUtil.output(1, "房间已设置玩法", getRequest(), getResponse(), false);
                    return;
                }
                HashMap<String, Object> map = new HashMap<>();
                if (StringUtils.isNotBlank(descMsg)) {
                    map.put("descMsg", descMsg);
                }
                map.put("modeMsg", modeMsg);
                map.put("tableName", tableName);
                map.put("tableOrder", tableOrder);
                map.put("gameType", gameType);
                map.put("gameCount", gameCount);
                map.put("payType", payType);
                map.put("playerCount", playerCount);
                map.put("configState", "1");
                map.put("createdTime", CommonUtil.dateTimeToString());
                map.put("keyId", groupTableConfig.getKeyId().toString());
                groupDao.updateGroupTableConfigByKeyId(map);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("code", 0);
                jsonObject.put("message", LangMsg.getMsg(LangMsg.code_0));
                jsonObject.put("tableMsg", modeMsg);// 房间配置
                OutputUtil.output(jsonObject, getRequest(), getResponse(), null, false);
                jsonObject.put("tableMsg", "");// 房间配置
                return;
            }
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 创建桌
     */
    public void createTable(){
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            String userId = params.get("oUserId");
            String groupId = params.get("groupId");
            String tableCount = params.get("tableCount");
            String configId = params.get("configId");
            if (!NumberUtils.isDigits(groupId) || !NumberUtils.isDigits(userId) || !NumberUtils.isDigits(tableCount) || !NumberUtils.isDigits(configId)) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            GroupTableConfig groupTableConfig0 = groupDao.loadGroupTableConfig(Long.parseLong(configId));
            if (groupTableConfig0 == null) {
                OutputUtil.output(3, "该牌局配置不存在", getRequest(), getResponse(), false);
                return;
            }
            if (StringUtils.isBlank(groupTableConfig0.getModeMsg())) {// 未设置玩法 先设置玩法
                OutputUtil.output(5, "未设置玩法", getRequest(), getResponse(), false);
                return;
            }
            GroupTable groupTable = groupDao.loadGroupTableByConfigId(Long.parseLong(groupId), Long.parseLong(configId));
            RegInfo user = userDao.getUser(Long.parseLong(userId));
            int serverId = user.getEnterServer();
            if(serverId<=0){
                Server server = SysInfManager.loadServer(groupTableConfig0.getGameType(),1);
                if(server != null){
                    serverId = server.getId();
                }
            }
            if(groupTable != null && groupTable.getTableId() > 0) {// 加入房间
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("code", 0);
                jsonObject.put("message", LangMsg.getMsg(LangMsg.code_0));
                jsonObject.put("tableId", groupTable.getTableId());
                OutputUtil.output(jsonObject, getRequest(), getResponse(), null, false);
                return;
//                Map<String, String> infoMap = new HashMap<>();
//                infoMap.put("userId", userId);
//                infoMap.put("tableId", groupTable.getTableId() + "");
//                infoMap.put("gameType", groupTableConfig0.getGameType() + "");
//                infoMap.put("serverType", "1");
//                infoMap.put("playNo", "0");
//                infoMap.put("gId", groupId);
//                infoMap.put("modelId", configId);
//                String res = "";
//                if(serverId > 0) {
//                    res = GameUtil.sendJoinTable(serverId, infoMap);
//                    GameBackLogger.SYS_LOG.info("sendJoinTable-->serverId:" + serverId + ",infoMap:" + infoMap + ",res:" + res);
//                    JSONObject jsonObject = new JSONObject();
//                    if("succeed".equals(res)) {
//                        jsonObject.put("code", 0);
//                        jsonObject.put("message", LangMsg.getMsg(LangMsg.code_0));
//                        OutputUtil.output(jsonObject, getRequest(), getResponse(), null, false);
//                        return;
//                    }else{
//                        OutputUtil.output(4, res, getRequest(), getResponse(), false);
//                        return;
//                    }
//                } else {
//                    OutputUtil.output(3, "找不到服务器", getRequest(), getResponse(), false);
//                }
            } else {// 创建房间
                GroupUser groupUser = groupDao.loadGroupUser(Long.parseLong(userId), Long.parseLong(groupId));
                if(groupUser == null || groupUser.getUserRole().intValue() > 1){
                    OutputUtil.output(2, "您不是群主(管理员)，无法操作！", getRequest(), getResponse(), false);
                    return;
                }
                Map<String, String> infoMap = new HashMap<>();
                infoMap.put("oUserId", userId);
                infoMap.put("groupId", groupId);
                infoMap.put("tableCount", tableCount);
                infoMap.put("tableVisible", "1");
                infoMap.put("modeId", configId);
                String res = "";
                if(serverId > 0) {
                    res = GameUtil.sendCreateTable(serverId, infoMap);
                    GameBackLogger.SYS_LOG.info("sendCreateTable-->serverId:" + serverId + ",infoMap:" + infoMap + ",res:" + res);
                    JSONObject jsonObject = new JSONObject();
                    if("succeed".equals(res)){
                        jsonObject.put("code", 0);
                        jsonObject.put("message", LangMsg.getMsg(LangMsg.code_0));
                        OutputUtil.output(jsonObject, getRequest(), getResponse(), null, false);
                        return;
                    }else{
                        OutputUtil.output(4, res, getRequest(), getResponse(), false);
                        return;
                    }
                } else {
                    OutputUtil.output(3, "找不到服务器", getRequest(), getResponse(), false);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 解散牌桌(解散房间和清空玩法操作)
     * 群主管理员可解散未开局牌桌或清空玩法
     */
    public void dissSingleTable() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            String groupId = params.get("groupId");
            String userId = params.get("oUserId");
            String configId = params.get("configId");
            if (!NumberUtils.isDigits(groupId) || !NumberUtils.isDigits(userId)) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            GroupUser groupUser = groupDao.loadGroupUser(Long.parseLong(userId), Long.parseLong(groupId));
            if(groupUser == null || groupUser.getUserRole().intValue() > 1){
                OutputUtil.output(2, "您不是群主(管理员)，无法操作！", getRequest(), getResponse(), false);
                return;
            }
            GroupTable groupTable = groupDao.loadGroupTableByConfigId(Long.parseLong(groupId), Long.parseLong(configId));
            if(groupTable == null || Integer.parseInt(groupTable.getCurrentState()) >= 2 || groupUser.getGroupId().intValue() != Integer.parseInt(groupId)){
                OutputUtil.output(2, "桌子不存在，无法操作！", getRequest(), getResponse(), false);
                return;
            }
            JSONObject jsonObject = new JSONObject();
            if(groupTable.getTableId() > 0) {// 解散房间
                Map<String, String> infoMap = new HashMap<>();
                infoMap.put("tableIds", groupTable.getTableId() + "");
                infoMap.put("keyIds", groupTable.getKeyId() + "");
                infoMap.put("specialDiss", "1");
                String res = "";
                int c = 0;
                while (StringUtils.isBlank(res) && c <= 5) {
                    c++;
                    int serverId = Integer.parseInt(groupTable.getServerId());
                    res = GameUtil.sendDissInfo(serverId, infoMap);
                    GameBackLogger.SYS_LOG.info("sendDissInfo-->serverId:" + serverId + ",infoMap:" + infoMap + ",res:" + res);
                }
                jsonObject.put("code", 0);
                jsonObject.put("message", LangMsg.getMsg(LangMsg.code_0));
            } else {// 清空玩法
                GroupTableConfig tableConfig = groupDao.loadGroupTableConfig(Long.parseLong(configId));
                if(tableConfig == null) {
                    OutputUtil.output(1, "桌子不存在", getRequest(), getResponse(), false);
                    return;
                }
                HashMap<String, Object> map = new HashMap<>();
                map.put("keyId", tableConfig.getKeyId().toString());
                map.put("modeMsg", "");
                map.put("playCount", 0);
                map.put("tableName", "");
                map.put("tableOrder", 1);
                map.put("gameType", 0);
                map.put("gameCount", 0);
                map.put("payType", 0);
                map.put("playerCount", 0);
                map.put("configState", "1");
                groupDao.updateGroupTableConfigByKeyId(map);
                jsonObject.put("code", 0);
                jsonObject.put("message", LangMsg.getMsg(LangMsg.code_0));
                jsonObject.put("tableMsg", "");// 房间配置
            }
            OutputUtil.output(jsonObject, getRequest(), getResponse(), null, false);
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 创建空房间
     */
    public void createEmptyTable() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            String groupId = params.get("groupId");
            String userId = params.get("oUserId");
            String tableMode = params.get("tableMode");
            if (!NumberUtils.isDigits(groupId) || !NumberUtils.isDigits(userId)) {
                OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3), getRequest(), getResponse(), false);
                return;
            }
            GroupUser groupUser = groupDao.loadGroupUser(Long.parseLong(userId), Long.parseLong(groupId));
            if(groupUser == null || groupUser.getUserRole().intValue() > 1){
                OutputUtil.output(2, "您不是群主(管理员)，无法操作！", getRequest(), getResponse(), false);
                return;
            }
            List<Long> configIds = GroupDaoManager.getInstance().createEmptyTableConfigs(Long.parseLong(groupId), Integer.parseInt(tableMode), 1);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("code", 0);
            jsonObject.put("message", LangMsg.getMsg(LangMsg.code_0));
            jsonObject.put("configId", configIds.get(0));// 房间索引号
            jsonObject.put("tableMsg", "");// 房间配置
            jsonObject.put("tableMode", tableMode);
            OutputUtil.output(jsonObject, getRequest(), getResponse(), null, false);
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 获取俱乐部管理员信息(头像 名字 id)
     */
    public void loadGroupManagers() {
        try {
            Map<String, String> params = UrlParamUtil.getParameters(getRequest());
            LOGGER.info("params:{}", params);
            if (!checkSign(params)) {
                OutputUtil.output(-1, LangMsg.getMsg(LangMsg.code_1), getRequest(), getResponse(), false);
                return;
            }
            String groupId = params.get("groupId");
            List<Map<String, Object>> groupUserList = groupDao.loadGroupManagers(groupId);;//groupDao.loadGroupManagers(groupId);
            JSONArray ja = new JSONArray();
            if(groupUserList != null && !groupUserList.isEmpty()) {
                for(Map<String, Object> groupUser : groupUserList) {
                    JSONObject jo = new JSONObject();
                    jo.put("userId", groupUser.get("userId"));
                    jo.put("headimgurl", groupUser.get("headimgurl"));
                    jo.put("name", groupUser.get("name"));
                    ja.add(jo);
                }
            }
            OutputUtil.output(ja, getRequest(), getResponse(), null, false);
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }
    }

//需要新增的一些协议 或者 某些老协议里要新增的数据
//2：获取某个合伙人的当前分成模式(模式A、B、C)和分成比例
//
//3：修改某个合伙人的当前分成模式和比例
//
//4：合伙人管理 获取所有合伙人信息 (原来有接口 即组长信息) 但是 额外需要 大赢家 局数 消耗
//
//5：查找合伙人(或成员) 输入合伙人ID和时区 获取合伙人ID 昵称 大赢家 局数 消耗  (或输入玩家ID和时区 查找某个成员的 昵称 大赢家 局数 消耗 的接口)
//
//            6：我的零钱包 获取我的 剩余未提取积分 昨日收入积分 总共收入积分
//
//7：我的零钱包 提取积分
//
//8：获取当前俱乐部的比赛限制 参与游戏分数 参与抢庄分数 分成模式(大赢家、所有赢家) （比例、固定） 百分比
//
//9：修改当前俱乐部的比赛限制 (同上)
//
//10：获取俱乐部所有成员协议中 增加玩家的积分数据
//
//11：新增赠送积分接口 (操作者ID 被操作者ID)
//
//12：成员管理 要新增数据 100分 300分 500分 总分 大赢家

//    模式A：从赢家分红里按群主与该赢家对应小组组长的分成比例。比如小组a的成员一局游戏最为大赢家赢了1000分，分成方式是大赢家5%，群主与小组a的组长分成比例为80%，群主得1000*5%*20%=10，小组a的组长得1000*5%*80=40；
//
//    模式B：输赢的正负分均按分成计算然后各取50%，比如一局游戏内小组a成员a1得1000分、a2负700分、小组b的b1得500分、b2负800分，分成方式是10%，群主与组长a分成比例为80%，与组长b分成70%，组长a得（1000+700）*10%*50%*80%=68，组长b得（500+800）*10%*50%*70%=45（小数点向下取整），群主得36；
//
//    模式C：根据参与的所有人平均分成，即每个玩家属于哪个小组的就给对应小组的组长平均分成，例如有总共8人对局，其中总分红200分，那么平均分配给8人，就是每人25分，再用25分根据对应群主和组长分成比例算出分成。


    public void setUserDao(UserDaoImpl userDao) {
        this.userDao = userDao;
    }

    public void setGroupDao(GroupDao groupDao) {
        this.groupDao = groupDao;
    }

    public void setDataStatisticsDao(DataStatisticsDao dataStatisticsDao) {
        this.dataStatisticsDao = dataStatisticsDao;
    }
}
