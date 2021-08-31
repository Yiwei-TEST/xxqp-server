package com.sy599.game.gcommand.group;

import com.alibaba.fastjson.JSONObject;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.db.bean.group.GroupInfo;
import com.sy599.game.db.bean.group.GroupTable;
import com.sy599.game.db.bean.group.GroupTableConfig;
import com.sy599.game.db.bean.group.GroupUser;
import com.sy599.game.db.dao.group.GroupDao;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.manager.TableManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.GroupTableList;
import com.sy599.game.msg.serverPacket.GroupTableList.GroupTableListMsg;
import com.sy599.game.msg.serverPacket.GroupTableList.MemberMsg;
import com.sy599.game.msg.serverPacket.GroupTableList.TableMsg;
import com.sy599.game.util.LangHelp;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.ResourcesConfigsUtil;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class GroupTableListNewCommand extends BaseCommand {

    @Override
    public void setMsgTypeMap() {
    }

    public static final Map<Long, List<TableMsg>> tableMsgMap = new ConcurrentHashMap<>();
    public static final Map<Long, Long> refreshMap = new ConcurrentHashMap<>();
    public static final Map<Long, Long> groupLockMap = new ConcurrentHashMap<>();

    /**
     * 用户访问接口频率
     * key=userId value=时间戳
     */
    public static final Map<String, Long> USER_ACCESS_TIME_MAP = new ConcurrentHashMap<>();

    /**
     * 用户访问某个分页数据的频率
     *
     * @return true：受限制，false：未受限制
     */
    public boolean accessFrequencyLimit(long userId, int pageNo) {
        String key = userId + "_" + pageNo;
        Long lastAccessTime = USER_ACCESS_TIME_MAP.get(key);
        Long now = System.currentTimeMillis();
        if (lastAccessTime == null) {
            USER_ACCESS_TIME_MAP.put(key, now);
            return false;
        }
        if ((now - lastAccessTime) <= ResourcesConfigsUtil.loadServerConfigIntegerValue("GroupTableListNewCommandAccessTimeLimit", 1000)) {
            return true;
        }
        USER_ACCESS_TIME_MAP.put(key, now);
        return false;
    }

    @Override
    public void execute(Player player, MessageUnit message) throws Exception {
        if(true){
            player.writeErrMsg("软件版本过低，请先升级到最新版本！");
            return;
        }

        ComMsg.ComReq req = (ComMsg.ComReq) this.recognize(ComMsg.ComReq.class, message);
        List<Integer> params = req.getParamsList();
        List<String> strParams = req.getStrParamsList();
        int strParamsSize = strParams == null ? 0 : strParams.size();
        int paramsSize = params == null ? 0 : params.size();
        if (paramsSize == 0) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_3));
            return;
        }
        long groupId = params.get(0);
        int pageNo = paramsSize >= 2 ? params.get(1) : 1;
        int pageSize = paramsSize >= 3 ? params.get(2) : 30;
        if (pageNo < 1) {
            pageNo = 1;
        }
        if (pageSize < 1) {
            pageSize = 1;
        }
        if (pageSize > 25) {
            pageSize = 25;
        } else if (pageSize < 5) {
            pageSize = 5;
        }

        long configId = strParamsSize >= 1 ? Long.valueOf(strParams.get(0)) : 0l;
        long tableId = strParamsSize >= 2 ? Long.valueOf(strParams.get(1)) : 0l;
        long userId = strParamsSize >= 3 ? Long.valueOf(strParams.get(2)) : 0l;

        if (tableId <= 0 && userId <= 0) {
            if (accessFrequencyLimit(player.getUserId(), pageNo)) {
                return;
            }
        }

        GroupUser groupUser = GroupDao.getInstance().loadGroupUser(player.getUserId(),String.valueOf(groupId));
        if (groupUser == null) {
            player.writeErrMsg(LangMsg.code_63, groupId);
            return;
        }
        int tableOrder = 2;
        GroupInfo group = GroupDao.getInstance().loadGroupInfo(groupId);
        if (group != null && StringUtils.isNotBlank(group.getExtMsg())) {
            JSONObject json = JSONObject.parseObject(group.getExtMsg());
            String chatStr = json.getString("tableOrder");
            if ("1".equals(chatStr)) {
                tableOrder = 1;
            }
        }
        if (!tableMsgMap.containsKey(groupId)) {
            List<HashMap<String, Object>> list = GroupDao.getInstance().loadSubGroups((int) groupId, null);
            if (list == null || list.size() == 0) {
                // 默认没有包间，创建一个包间
                createGroupRoom(groupUser);
            }
        }
        Long startTime = System.currentTimeMillis();
        Long refreshTime = refreshMap.get(groupId);
        if (refreshTime == null || System.currentTimeMillis() - refreshTime > 2000) {
            Long groupLock = getGroupLock(groupId);
            synchronized (groupLock) {
                refreshTime = refreshMap.get(groupId);
                if (refreshTime == null || System.currentTimeMillis() - refreshTime > 2000) {
                    genMsg(groupId, tableOrder);
                }
            }
        }

        int code = 0; // 0：所有，1：查询configId，2：查询tableId 3：查询userId
        //推送牌桌
        GroupTableListMsg.Builder msg = GroupTableListMsg.newBuilder();
        msg.setGroupId(groupId);
        msg.setPageNo(pageNo);
        msg.setPageSize(pageSize);
        List<TableMsg> cacheList = tableMsgMap.get(groupId);
        List<TableMsg> msgList = new ArrayList<>();
        if (configId > 0) {
            // 只查某个玩法
            code = 1;
            if (cacheList != null && cacheList.size() > 0) {
                for (TableMsg tmp : cacheList) {
                    if (tmp.getConfigId() == configId) {
                        msgList.add(tmp);
                    }
                }
            }
        } else if (tableId > 0) {
            // 只查某个牌桌
            code = 2;
            if (cacheList != null && cacheList.size() > 0) {
                for (TableMsg tmp : cacheList) {
                    if (tmp.getTableId() == tableId) {
                        msgList.add(tmp);
                        break;
                    }
                }
            }
        } else if (userId > 0) {
            // 只查某个用户
            code = 3;
            if (cacheList != null && cacheList.size() > 0) {
                for (TableMsg tmp : cacheList) {
                    List<MemberMsg> membersList = tmp.getMembersList();
                    boolean finded = false;
                    for (MemberMsg member : membersList) {
                        if (member.getUserId() == userId) {
                            msgList.add(tmp);
                            finded = true;
                            break;
                        }
                    }
                    if (finded) {
                        break;
                    }
                }
            }
        } else {
            if (cacheList != null && cacheList.size() > 0) {
                msgList = new ArrayList<>(cacheList);
            }
        }

        if (msgList != null && msgList.size() > 0) {
            int total = msgList.size();
            int fromIndex = (pageNo - 1) * pageSize;
            int toIndex = pageNo * pageSize;
            if (total > fromIndex) {
                toIndex = total > toIndex ? toIndex : total;
                List<TableMsg> dataList = msgList.subList(fromIndex, toIndex);
                msg.setTableCount(total);
                msg.addAllTables(dataList);
            }
        } else {
            msg.setTableCount(0);
        }
        msg.setCode(code);
//        msg.setGroupLevel(group.getLevel());
//        msg.setGroupExp(group.getExp());
//        msg.setGroupUserLevel(groupUser.getLevel());
//        msg.setGroupUserExp(groupUser.getExp());
        player.writeSocket(msg.build());

        long timeUse = System.currentTimeMillis() - startTime;
        if (timeUse > 200) {
            StringBuilder sb = new StringBuilder("xnlog|GroupTableListNewCommand");
            sb.append("|").append(timeUse);
            sb.append("|").append(startTime);
            sb.append("|").append(groupId);
            sb.append("|").append(player.getUserId());
            sb.append("|").append(pageNo);
            sb.append("|").append(pageSize);
            LogUtil.monitorLog.info(sb.toString());
        }
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


    public void genMsg(long groupId, int tableOrder) throws Exception {
        List<TableMsg> dataList = new ArrayList<>();
        List<GroupTable> gtList = GroupDao.getInstance().loadGroupTables(groupId, -1, 0, 0, tableOrder, 1, 1000);
        Set<Long> remSet = new HashSet<>();
        if (gtList != null) {
            String[] groupFreeStrs = ResourcesConfigsUtil.loadServerPropertyValue("group_" + groupId + "_free_date", "").split("_");
            if (groupFreeStrs != null && groupFreeStrs.length > 1) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date freeStart = sdf.parse(groupFreeStrs[0]);
                Date freeEnd = sdf.parse(groupFreeStrs[1]);
                for (GroupTable groupTable : gtList) {
                    Date createdTime = groupTable.getCreatedTime();
                    int currentCount = groupTable.getCurrentCount();
                    if ((createdTime.after(freeStart) && createdTime.before(freeEnd)) && currentCount == 0) {
                        GroupDao.getInstance().deleteGroupTableByKeyId(groupTable.getKeyId());
                        remSet.add(groupTable.getKeyId());
                    }
                }
            }
        }

        if (gtList == null && gtList.size() == 0 || gtList.size() == remSet.size()) {
            // 没有数据，直接返回
            tableMsgMap.put(groupId, Collections.emptyList());
            refreshMap.put(groupId, System.currentTimeMillis());
            return;
        }

        //---------------------加载玩家数据-----------------------------------
        Map<String, List<HashMap<String, Object>>> membersMap = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        for (GroupTable gt : gtList) {
            BaseTable table = TableManager.getInstance().getTable(gt.getTableId().longValue());
            String[] ts;
            if (table != null && table.isGroupRoom() && (ts = table.getServerKey().split("_")).length >= 2 && ts[1].equals(gt.getKeyId().toString())) {
                if (table.getPlayerCount() <= 0) {
                    continue;
                }
                List<HashMap<String, Object>> members = new ArrayList<>();
                for (Player p1 : table.getPlayerMap().values()) {
                    HashMap<String, Object> member = new HashMap<>();
                    member.put("userId", p1.getUserId());
                    member.put("headimgurl", p1.getHeadimgurl());
                    member.put("userName", p1.getRawName());
                    member.put("isOnLine", p1.getIsOnline());
                    member.put("sex", p1.getSex());
                    members.add(member);
                }
                membersMap.put(gt.getKeyId().toString(), members);
            } else if (gt.getCurrentCount().intValue() > 0) {
                sb.append(",'").append(gt.getKeyId().toString()).append("'");
            }
        }
        if (sb.length() > 0) {
            List<HashMap<String, Object>> members = GroupDao.getInstance().loadTableUserInfo(sb.substring(1),groupId);
            if (members != null && members.size() > 0) {
                for (HashMap<String, Object> map : members) {
                    String tableNo = String.valueOf(map.remove("tableNo"));
                    List<HashMap<String, Object>> tempList = membersMap.get(tableNo);
                    if (tempList == null) {
                        tempList = new ArrayList<>();
                        membersMap.put(tableNo, tempList);
                    }
                    tempList.add(map);
                }
            }
        }

        // -----------------测试代码------------
//        List<GroupTable> addList = new ArrayList<>();
//        for (GroupTable gt : gtList) {
//            List<HashMap<String, Object>> members = membersMap.get(gt.getKeyId().toString());
//            if (members != null && members.size() > 0) {
//                for (int i = 0; i < 200; i++) {
//                    addList.add(gt);
//                }
//            }
//        }
//        gtList.addAll(addList);
        // -----------------测试代码------------

        int i = 0;
        for (GroupTable gt : gtList) {
            if (remSet.contains(gt.getKeyId())) {
                continue;
            }
            List<HashMap<String, Object>> members = membersMap.get(gt.getKeyId().toString());
            GroupTableList.TableMsg.Builder data = GroupTableList.TableMsg.newBuilder();
            data.setGroupId(groupId);
            copyData(data, gt, members);
            dataList.add(data.build());

        }
        tableMsgMap.put(groupId, dataList);
        refreshMap.put(groupId, System.currentTimeMillis());

        LogUtil.monitorLog.debug("GroupTableListNewCommand|genMsg|" + groupId + "|" + tableOrder);
    }

    public void copyData(TableMsg.Builder tableMsg, GroupTable gt, List<HashMap<String, Object>> members) {
        tableMsg.setKeyId(gt.getKeyId());
        tableMsg.setServerId(gt.getServerId());
        tableMsg.setConfigId(gt.getConfigId());
        tableMsg.setTableId(gt.getTableId());
        String tableName = gt.getTableName() != null ? gt.getTableName() : null;
        if (tableName != null && tableName.length() > 8) {
            tableName.substring(0, 8);
        }
        tableMsg.setTableName(tableName);
//        tableMsg.setTableMsg(gt.getTableMsg());
//        tableMsg.setCreditMsg(gt.getCreditMsg());
        tableMsg.setCurrentState(gt.getCurrentState());
        tableMsg.setType(gt.getType());
        tableMsg.setCurrentCount(gt.getCurrentCount());
        tableMsg.setMaxCount(gt.getMaxCount());
        tableMsg.setPlayedBureau(gt.getPlayedBureau());
//        tableMsg.setDealCount(gt.getDealCount());
        tableMsg.setNotStart(gt.getDealCount() <= 0);
//        tableMsg.setCreatedTime(gt.getCreatedTime().getTime());
//        tableMsg.setOverTime(gt.getOverTime().getTime());
        if (members != null && members.size() > 0) {
            for (HashMap<String, Object> memberMap : members) {
                MemberMsg.Builder memberMsg = MemberMsg.newBuilder();
                memberMsg.setUserId((Long) memberMap.get("userId"));
                String userName = memberMap.get("userName") != null ? memberMap.get("userName").toString() : null;
                if (userName != null && userName.length() > 8) {
                    userName = userName.substring(0, 8);
                }
                memberMsg.setUserName(userName);
                String headImg = memberMap.get("headimgurl") != null ? memberMap.get("headimgurl").toString() : null;
                if (headImg != null) {
                    memberMsg.setHeadimgurl(headImg);
                }
//                memberMsg.setSex((Integer) memberMap.get("sex"));
                memberMsg.setIsOnLine((Integer) memberMap.get("isOnLine"));
                tableMsg.addMembers(memberMsg);
            }
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

    public static void clearCacheData() {
        List<Long> groupIds = new ArrayList<>(refreshMap.keySet());
        long now = System.currentTimeMillis();
        for (long groupId : groupIds) {
            Long groupLock = getGroupLock(groupId);
            synchronized (groupLock) {
                if (refreshMap.get(groupId) != null && refreshMap.get(groupId) + 60 * 1000 < now) {
                    refreshMap.remove(groupId);
                    tableMsgMap.remove(groupId);
                }
            }
        }
    }

}
