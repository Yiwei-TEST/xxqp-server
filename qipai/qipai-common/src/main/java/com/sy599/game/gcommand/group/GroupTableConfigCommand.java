package com.sy599.game.gcommand.group;

import com.alibaba.fastjson.JSONObject;
import com.sy.mainland.util.MessageBuilder;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.db.bean.group.GroupInfo;
import com.sy599.game.db.bean.group.GroupTableConfig;
import com.sy599.game.db.bean.group.GroupUser;
import com.sy599.game.db.dao.group.GroupDao;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.util.LangHelp;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.ResourcesConfigsUtil;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.sy599.game.websocket.constant.WebSocketMsgType.res_com_group_table_config;

public class GroupTableConfigCommand extends BaseCommand {

    public static final Map<Long, List<HashMap<String, Object>>> dataListMap = new ConcurrentHashMap<>();
    public static final Map<Long, Long> refreshMap = new ConcurrentHashMap<>();
    public static final Map<Long, Long> groupLockMap = new ConcurrentHashMap<>();
    public static final Set<String> configKeySet = new HashSet<>(Arrays.asList("keyId", "groupId", "configState", "goldMsg", "tableName", "tableOrder", "creditMsg", "modeMsg"));
    public static final Set<String> groupKeySet = new HashSet<>(Arrays.asList("keyId", "groupId", "extMsg", "startedNum", "groupState", "groupName", "config"));

    @Override
    public void setMsgTypeMap() {
    }

    @Override
    public void execute(Player player, MessageUnit message) throws Exception {
        ComMsg.ComReq req = (ComMsg.ComReq) this.recognize(ComMsg.ComReq.class, message);
        List<Integer> params = req.getParamsList();
        int paramsSize = params == null ? 0 : params.size();
        if (paramsSize == 0) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_3));
            return;
        }
        long groupId = params.get(0);
        GroupUser groupUser = player.getGroupUser();
        if (groupUser == null || groupUser.getGroupId().intValue() != groupId) {
            groupUser = player.loadGroupUser(String.valueOf(groupId));
            if (groupUser == null) {
                player.writeErrMsg(LangMsg.code_63, groupId);
                return;
            }
        }

        // -------------- 刷新缓存数据 ----------------------------
        Long refreshTime = refreshMap.get(groupId);
        if (refreshTime == null || System.currentTimeMillis() - refreshTime > 3000) {
            Long groupLock = getGroupLock(groupId);
            synchronized (groupLock) {
                refreshTime = refreshMap.get(groupId);
                if (refreshTime == null || System.currentTimeMillis() - refreshTime > 3000) {
                    refreshData(groupId, groupUser);
                }
            }
        }

        // -------------- 推送数据 ------------------------------
        List<HashMap<String, Object>> groupList = dataListMap.get(groupId);
        MessageBuilder msg = MessageBuilder.newInstance();
        msg.builder("code", 0);
        msg.builder("groupId", groupId);
        if (groupList == null || groupList.size() == 0) {
            msg.builder("list", Collections.emptyList());
        } else {
            msg.builder("list", groupList);
        }
        player.writeComMessage(res_com_group_table_config, groupId, msg.toString());
    }

    public void refreshData(long groupId, GroupUser groupUser) {
        try {
            List<HashMap<String, Object>> groupList = GroupDao.getInstance().loadSubGroups((int) groupId, null);
            if (groupList == null || groupList.size() == 0) {
                groupList = createGroupRoom(groupUser);
            } else {
                List<HashMap<String, Object>> configList = GroupDao.getInstance().loadAllLastGroupTableConfig(null, String.valueOf(groupId));
                if (configList != null && configList.size() > 0) {
                    Map<String, Long> startNumMap = GroupDao.getInstance().countGroupStartedTablesAll(String.valueOf(groupId));
                    Map<String, Long> fakeTableNumMap = GroupDao.getInstance().countFakeTablesAll();
                    for (HashMap<String, Object> group : groupList) {
                        String parentGroup = String.valueOf(group.get("parentGroup"));
                        String groupId0 = String.valueOf(group.get("groupId"));

                        for (HashMap<String, Object> config : configList) {
                            if (parentGroup.equals(String.valueOf(config.get("parentGroup"))) && groupId0.equals(String.valueOf(config.get("groupId")))) {
                                // 清理不需要的字段值
                                List<String> keySet = new ArrayList<>(config.keySet());
                                for (String key : keySet) {
                                    if (!configKeySet.contains(key)) {
                                        config.remove(key);
                                    }
                                }
                                group.put("config", config);
                                break;
                            }
                        }

                        if (group.containsKey("config")) {
                            HashMap<String, Object> config1 = (HashMap<String, Object>) group.get("config");
                            long startNum = 0;
                            if (startNumMap.containsKey(String.valueOf(config1.get("keyId")))) {
                                startNum += startNumMap.get(String.valueOf(config1.get("keyId")));
                            }
                            if (fakeTableNumMap.containsKey(String.valueOf(config1.get("keyId")))) {
                                startNum += fakeTableNumMap.get(String.valueOf(config1.get("keyId")));
                            }
                            group.put("startedNum", startNum);
                        }

                        // 清理不需要的字段值
                        List<String> keySet = new ArrayList<>(group.keySet());
                        for (String key : keySet) {
                            if (!groupKeySet.contains(key)) {
                                group.remove(key);
                            }
                        }
                    }
                }
            }
            if (groupList != null && groupList.size() > 0) {
                dataListMap.put(groupId, groupList);
                refreshMap.put(groupId, System.currentTimeMillis());
            }
        } catch (Exception e) {
            LogUtil.errorLog.error("refreshData|error|" + groupId, e);
        }
    }


    /**
     * 创建包间
     *
     * @param groupUser
     * @return
     * @throws Exception
     */
    private List<HashMap<String, Object>> createGroupRoom(GroupUser groupUser) throws Exception {
        if (!"1".equals(ResourcesConfigsUtil.loadServerPropertyValue("group_room_mode"))) {
            return null;
        }
        if (groupUser == null || groupUser.getUserRole() != 0) {
            return null;
        }
        GroupInfo groupInfo = GroupDao.getInstance().loadGroupInfo(groupUser.getGroupId(), 0);
        if (groupInfo == null) {
            return null;
        }
        JSONObject jsonObject = StringUtils.isBlank(groupInfo.getExtMsg()) ? new JSONObject() : JSONObject.parseObject(groupInfo.getExtMsg());
        String str = jsonObject.getString("oq");
        if (str == null || (!str.contains("+q") && str.contains("-q"))) {
            //非快速开房
            return null;
        }
        List<GroupInfo> groupRooms = GroupDao.getInstance().loadAllGroupRoom(groupUser.getGroupId().toString());
        if (groupRooms != null && groupRooms.size() > 0) {
            return null;
        }
        GroupTableConfig config = GroupDao.getInstance().loadLastGroupTableConfig(groupUser.getGroupId(), 0);
        if (config == null) {
            return null;
        }

        Date now = new Date();
        int subId = 1;
        GroupInfo groupNew = new GroupInfo();
        groupNew.setCreatedTime(now);
        groupNew.setCreatedUser(groupUser.getUserId());
        groupNew.setCurrentCount(1);
        groupNew.setDescMsg("");
        groupNew.setGroupId(subId);
        groupNew.setParentGroup(groupUser.getGroupId());
        groupNew.setExtMsg("");
        groupNew.setGroupLevel(0);
        groupNew.setGroupMode(0);
        groupNew.setGroupName("包厢1");
        groupNew.setMaxCount(0);
        groupNew.setGroupState("1");
        groupNew.setModifiedTime(now);
        long subGroupKeyId = GroupDao.getInstance().createGroup(groupNew);

        if (subGroupKeyId > 0) {
            GroupTableConfig configNew = new GroupTableConfig();
            configNew.setCreatedTime(now);
            configNew.setDescMsg(config.getDescMsg());
            configNew.setGroupId(Long.valueOf(subId));
            configNew.setParentGroup(groupUser.getGroupId().longValue());
            configNew.setModeMsg(config.getDescMsg());
            configNew.setPlayCount(0L);
            configNew.setTableMode(config.getTableMode());
            configNew.setTableName(config.getTableName());
            configNew.setTableOrder(config.getTableOrder());
            configNew.setGameType(config.getGameType());
            configNew.setGameCount(config.getGameCount());
            configNew.setPayType(config.getPayType());
            configNew.setPlayerCount(config.getPlayerCount());
            configNew.setConfigState("1");
            long configId = GroupDao.getInstance().createGroupTableConfig(configNew);
            if (configId <= 0) {
                GroupDao.getInstance().deleteGroupInfoByKeyId(String.valueOf(subGroupKeyId));
            }

        }
        return GroupDao.getInstance().loadSubGroups(groupUser.getGroupId(), null);
    }

    public static Long getGroupLock(long groupId) {
        Long groupLock = groupLockMap.get(groupId);
        if (groupLock == null) {
            groupLock = groupLockMap.putIfAbsent(groupId, Long.valueOf(groupId));
            if (groupLock == null) {
                groupLock = groupLockMap.get(groupId);
            }
        }
        return groupLock;
    }

    public static void clearCacheData() {
        List<Long> groupIds = new ArrayList<>(refreshMap.keySet());
        long now = System.currentTimeMillis();
        for (long groupId : groupIds) {
            Long groupLock = getGroupLock(groupId);
            synchronized (groupLock) {
                if (refreshMap.get(groupId) != null && refreshMap.get(groupId) + 60 * 1000 < now) {
                    refreshMap.remove(groupId);
                    dataListMap.remove(groupId);
                }
            }
        }
    }

}
