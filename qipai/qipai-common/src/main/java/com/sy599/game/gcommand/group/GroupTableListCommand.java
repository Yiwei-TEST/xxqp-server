package com.sy599.game.gcommand.group;

import com.alibaba.fastjson.JSONObject;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.db.bean.group.*;
import com.sy599.game.db.dao.group.GroupDao;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.manager.TableManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.GroupTableList;
import com.sy599.game.msg.serverPacket.GroupTableList.GroupTableListMsg;
import com.sy599.game.msg.serverPacket.GroupTableList.MemberMsg;
import com.sy599.game.msg.serverPacket.GroupTableList.TableMsg;
import com.sy599.game.msg.serverPacket.GroupTableList.HeadImgListMsg;
import com.sy599.game.util.LangHelp;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.MathUtil;
import com.sy599.game.util.ResourcesConfigsUtil;
import com.sy599.game.util.constants.GroupConstants;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GroupTableListCommand extends BaseCommand {

    @Override
    public void setMsgTypeMap() {
    }

    public static final Map<Long, List<TableMsg>> tableMsgMap = new ConcurrentHashMap<>();
    public static final Map<Long, List<TableMsg>> tableMsgMapForDetail = new ConcurrentHashMap<>();
    public static final Map<Long, Long> refreshMap = new ConcurrentHashMap<>();
    public static final Map<Long, Long> groupLockMap = new ConcurrentHashMap<>();

    public static final Map<Long, String> userHeadImgMap = new HashMap<>();
    public static final Map<Long, Long> userHeadImgRefreshMap = new HashMap<>();


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
        if ((now - lastAccessTime) <= ResourcesConfigsUtil.loadServerConfigIntegerValue("GroupTableListCommandAccessTimeLimit", 1000)) {
            return true;
        }
        USER_ACCESS_TIME_MAP.put(key, now);
        return false;
    }

    @Override
    public void execute(Player player, MessageUnit message) throws Exception {

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
        // 0：所有，1：查询configId的所有牌桌，2：查询tableId 3：查询userId所在的牌桌 , 4:查用户头像
        int optType = paramsSize >= 2 ? params.get(1) : 1;

        // -----------------频率限制-----------------------------------
        if (optType == 0) {
            if (accessFrequencyLimit(player.getUserId(), 1)) {
                return;
            }
        }

        GroupUser groupUser = GroupDao.getInstance().loadGroupUser(player.getUserId(), String.valueOf(groupId));
        if (groupUser == null) {
            player.writeErrMsg(LangMsg.code_63, groupId);
            return;
        }

        GroupInfo group = GroupDao.getInstance().loadGroupInfo(groupId);
        if (group == null) {
            player.writeErrMsg(LangMsg.code_63, groupId);
            return;
        }

        // ---------------------牌桌排序--------------------------------
        int tableOrder = 2; //排序
        int tableNum = 0; // 显示开局数量
        if (StringUtils.isNotBlank(group.getExtMsg())) {
            JSONObject json = JSONObject.parseObject(group.getExtMsg());
            String tableOrderStr = json.getString("tableOrder");
            if ("1".equals(tableOrderStr)) {
                tableOrder = 1;
            }
            tableNum = json.getIntValue(GroupConstants.groupExtKey_tableNum);
        }

        if (!tableMsgMap.containsKey(groupId)) {
            List<HashMap<String, Object>> list = GroupDao.getInstance().loadSubGroups((int) groupId, null);
            if (list == null || list.size() == 0) {
                // 默认没有包间，创建一个包间
                createGroupRoom(groupUser);
            }
        }


        // -------------- 刷新缓存数据----------------------------
        long timeLimit = ResourcesConfigsUtil.getGroupTableListRefreshTime();
        Long refreshTime = refreshMap.get(groupId);
        Long startTime = System.currentTimeMillis();
        if (refreshTime == null || System.currentTimeMillis() - refreshTime > timeLimit) {
            Long groupLock = getGroupLock(groupId);
            synchronized (groupLock) {
                refreshTime = refreshMap.get(groupId);
                if (refreshTime == null || System.currentTimeMillis() - refreshTime > timeLimit) {
                    genMsg(group, tableOrder);
                    long timeUse = System.currentTimeMillis() - startTime;
                    if (timeUse > 200) {
                        StringBuilder sb = new StringBuilder("xnlog|GroupTableListCommand|genMsg");
                        sb.append("|").append(timeUse);
                        sb.append("|").append(startTime);
                        sb.append("|").append(groupId);
                        sb.append("|").append(player.getUserId());
                        LogUtil.monitorLog.info(sb.toString());
                    }
                }
            }
        }


        // ----------------推送牌桌--------------------------------
        List<TableMsg> msgList = new ArrayList<>();
        if (optType == 0) {
            // ----------------查所有牌桌--------------------------------
            List<TableMsg> cacheList = tableMsgMap.get(groupId);
            if (cacheList != null && cacheList.size() > 0) {
                // 限制已开局牌桌数量
                if (tableNum > 0) {
                    int hasTableNum = 0;
                    for (TableMsg msg : cacheList) {
                        if ("1".equals(msg.getCurrentState())) {
//                            if (hasTableNum >= tableNum) {            20210330 去掉一开局桌子显示数限制
//                                continue;
//                            }
                            hasTableNum++;
                        }
                        msgList.add(msg);
                    }
                } else {
                    msgList = new ArrayList<>(cacheList);
                }
            }
        } else if (optType == 1) {
            // ----------------只查某个玩法--------------------------------
            List<TableMsg> cacheList = tableMsgMap.get(groupId);
            long configId = strParamsSize > 0 ? Long.valueOf(strParams.get(0)) : 0l;
            if (cacheList != null && cacheList.size() > 0) {
                for (TableMsg tmp : cacheList) {
                    if (tmp.getConfigId() == configId) {
                        msgList.add(tmp);
                    }
                }
            }
        } else if (optType == 2) {
            // ----------------只查某个牌桌--------------------------------
            List<TableMsg> cacheList = tableMsgMapForDetail.get(groupId);
            long tableId = strParamsSize > 0 ? Long.valueOf(strParams.get(0)) : 0l;
            if (cacheList != null && cacheList.size() > 0) {
                for (TableMsg tmp : cacheList) {
                    if (tmp.getTableId() == tableId) {
                        msgList.add(tmp);
                        break;
                    }
                }
            }
        } else if (optType == 3) {
            // ----------------只查某个用户----------------------------------
            long userId = strParamsSize > 0 ? Long.valueOf(strParams.get(0)) : 0l;
            List<TableMsg> cacheList = tableMsgMapForDetail.get(groupId);
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
        } else if (optType == 4) {
            // -----------------查询用户头像---------------------
            String userIdsStr = strParamsSize > 0 ? strParams.get(0) : "";
            if (StringUtils.isNotBlank(userIdsStr)) {
                HeadImgListMsg.Builder headImgList = HeadImgListMsg.newBuilder();
                String[] userIds = userIdsStr.split(",");
                for (String userIdStr : userIds) {
                    long userId = Long.valueOf(userIdStr);
                    MemberMsg.Builder memberMsg = MemberMsg.newBuilder();
                    memberMsg.setUserId(userId);
                    if (userHeadImgMap.containsKey(userId)) {
                        memberMsg.setHeadimgurl(userHeadImgMap.get(userId));
                    }
                    headImgList.addHeadImgs(memberMsg);
                }
                player.writeSocket(headImgList.build());
            }
            return;
        }


        GroupTableListMsg.Builder msg = GroupTableListMsg.newBuilder();
        msg.setGroupId(groupId);
        msg.setTableCount(msgList.size());
        // 最多放limit桌数据
        int limit = ResourcesConfigsUtil.getGroupTableListCountLimit();
        if (msgList.size() > limit) {
            msg.addAllTables(msgList.subList(0, limit));
        } else {
            msg.addAllTables(msgList);
        }
        msg.setCode(optType);
//        msg.setGroupLevel(group.getLevel());
//        msg.setGroupExp(group.getExp());
//        msg.setGroupUserLevel(groupUser.getLevel());
//        msg.setGroupUserExp(groupUser.getExp());
        player.writeSocket(msg.build());


        long timeUse = System.currentTimeMillis() - startTime;
        if (timeUse > 200) {
            StringBuilder sb = new StringBuilder("xnlog|GroupTableListCommand");
            sb.append("|").append(timeUse);
            sb.append("|").append(startTime);
            sb.append("|").append(groupId);
            sb.append("|").append(player.getUserId());
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


    /**
     * 刷新缓存数据
     *
     * @param group
     * @throws Exception
     */
    public void genMsg(GroupInfo group, int tableOrder) throws Exception {
        if (null == group) {
            return;
        }
        long groupId = group.getGroupId();


        List<Map<String, Object>> tableConfigOrderList =  GroupDao.getInstance().loadTableConfigOrder(groupId);
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

        Map<Long, Integer> orderMap = new HashMap<>();
        if (tableConfigOrderList != null && tableConfigOrderList.size() > 0){
            for(Map map : tableConfigOrderList){
                orderMap.put(Long.parseLong(map.get("keyId").toString()), Integer.parseInt(map.get("tableOrder").toString()));
            }
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
                    member.put("userName", p1.getRawName());
                    member.put("isOnLine", p1.getIsOnline());
                    member.put("headimgurl", p1.getHeadimgurl());
                    member.put("sex", p1.getSex());
                    members.add(member);
                }
                membersMap.put(gt.getKeyId().toString(), members);
            } else if (gt.getCurrentCount().intValue() > 0) {
                sb.append(",'").append(gt.getKeyId().toString()).append("'");
            }
        }
        if (sb.length() > 0) {
            List<HashMap<String, Object>> members = GroupDao.getInstance().loadTableUserInfoForceMaster(sb.substring(1),groupId);
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


        // -------------- 加载假桌子----------------------------
        List<GroupFakeTable> fakeTableList = GroupDao.getInstance().loadGroupFakeTable(groupId, null, null);
        StringBuilder fakeTableIdstrBuilder = new StringBuilder();

        for (GroupFakeTable fakeTable : fakeTableList) {
            fakeTableIdstrBuilder.append(",").append(fakeTable.getKeyId());
        }
        if (fakeTableIdstrBuilder.length() > 0) {
            fakeTableIdstrBuilder.deleteCharAt(0);
        }
        List<Map<String, Object>> fakeHeadImgList = new ArrayList<>();
        if(fakeTableIdstrBuilder.length() > 0) {
            fakeHeadImgList = GroupDao.getInstance().loadFakeImgByTableId(fakeTableIdstrBuilder.toString());
        }

        // -----------------测试代码------------
//        List<GroupTable> addList = new ArrayList<>();
//        for (GroupTable gt : gtList) {
//            List<HashMap<String, Object>> members = membersMap.get(gt.getKeyId().toString());
//            if (members != null && members.size() > 0) {
//                for (int i = 0; i < 50; i++) {
//                    addList.add(gt);
//                }
//                break;
//            }
//        }
//        gtList.addAll(addList);
        // -----------------测试代码------------

        List<TableMsg> dataList = new ArrayList<>();
        List<TableMsg> dataListForDetail = new ArrayList<>();
        Map<Long, GroupTable> gtMap = new HashMap<>();
        for (GroupTable gt : gtList) {
            if (remSet.contains(gt.getKeyId())) {
                continue;
            }

            List<HashMap<String, Object>> members = membersMap.get(gt.getKeyId().toString());
            GroupTableList.TableMsg.Builder data = GroupTableList.TableMsg.newBuilder();
            data.setGroupId(groupId);
            copyData(data, gt, members, false);
            dataList.add(data.build());

            List<HashMap<String, Object>> membersForDetail = membersMap.get(gt.getKeyId().toString());
            GroupTableList.TableMsg.Builder dataForDetail = GroupTableList.TableMsg.newBuilder();
            dataForDetail.setGroupId(groupId);
            copyData(dataForDetail, gt, membersForDetail, true);
            dataListForDetail.add(dataForDetail.build());

            if(!gtMap.containsKey(gt.getConfigId())) {
                gtMap.put(gt.getConfigId(), gt);
            }
        }

        for (GroupFakeTable gft : fakeTableList) {
            GroupTable gt = gtMap.get(gft.getConfigId());
            if(gt == null){
                continue;
            }
            GroupTableList.TableMsg.Builder data = GroupTableList.TableMsg.newBuilder();
            data.setGroupId(groupId);
            copyDataFakeTable(data, gft, gt, fakeHeadImgList, false);
            dataList.add(data.build());

            GroupTableList.TableMsg.Builder dataForDetail = GroupTableList.TableMsg.newBuilder();
            dataForDetail.setGroupId(groupId);
            copyDataFakeTable(dataForDetail, gft, gt, fakeHeadImgList, false);
            dataListForDetail.add(dataForDetail.build());
        }

        // -----------------排序------------
        Collections.sort(dataList, new Comparator<TableMsg>() {
            @Override
            public int compare(GroupTableList.TableMsg o1, GroupTableList.TableMsg o2) {
                //TODO

                int priority1 = getBasePriority(o1.getCurrentCount(), o1.getMaxCount(), tableOrder);
                int priority2 = getBasePriority(o2.getCurrentCount(), o2.getMaxCount(), tableOrder);
                if (priority1 > priority2) {
                    return 1;
                }
                else if (priority1 < priority2) {
                    return -1;
                }
                if(orderMap.containsKey(o1.getConfigId()) && orderMap.containsKey(o2.getConfigId())) {
                    if (orderMap.get(o1.getConfigId()) < orderMap.get(o2.getConfigId())) {  //tableOrder排序
                        return -1;
                    } else if (orderMap.get(o1.getConfigId()) > orderMap.get(o2.getConfigId())) {
                        return 1;
                    }
                }
                if (o1.getConfigId() < o2.getConfigId()) {      //configId排序
                    return -1;
                } else if (o1.getConfigId() > o2.getConfigId()) {
                    return 1;
                }
                return 0;
            }

        });

        tableMsgMap.put(groupId, dataList);
        tableMsgMapForDetail.put(groupId, dataListForDetail);
        refreshMap.put(groupId, System.currentTimeMillis());

        LogUtil.monitorLog.debug("GroupTableListNewCommand|genMsg|" + groupId);
    }

    /**
     * 根据桌子坐人状态排序
     * @param memberCount
     * @param maxCount
     * @param order 1空桌在满桌后 2空桌在满桌前
     * @return
     */
    private static int getBasePriority(int memberCount, int maxCount, int order) {
        if(order == 1) {
            if (memberCount == 0) {
                return 3;
            }
            if (memberCount == maxCount) {
                return 2;
            }
        } else {
            if (memberCount == maxCount) {
                return 3;
            }
            if (memberCount == 0) {
                return 2;
            }

        }
       return 1;
    }

    public void copyData(TableMsg.Builder tableMsg, GroupTable gt, List<HashMap<String, Object>> members, boolean forDetail) {
        tableMsg.setKeyId(gt.getKeyId());
        tableMsg.setServerId(gt.getServerId());
        tableMsg.setConfigId(gt.getConfigId());
        tableMsg.setTableId(gt.getTableId());
        String tableName = gt.getTableName() != null ? gt.getTableName() : null;
        if (tableName != null && tableName.length() > 8) {
            tableName.substring(0, 8);
        }
        tableMsg.setTableName(tableName);
        if (forDetail) {
//            tableMsg.setTableMsg(gt.getTableMsg());
//            tableMsg.setCreditMsg(gt.getCreditMsg());
        }
        tableMsg.setCurrentState(gt.getCurrentState());
        tableMsg.setType(gt.getType());
        tableMsg.setCurrentCount(gt.getCurrentCount());
        tableMsg.setMaxCount(gt.getMaxCount());
        tableMsg.setPlayedBureau(gt.getPlayedBureau());
//        tableMsg.setDealCount(gt.getDealCount());
        tableMsg.setNotStart(gt.getDealCount() <= 0);
//        tableMsg.setIsPrivate(gt.getIsPrivate());
        if (forDetail) {
//            tableMsg.setCreatedTime(gt.getCreatedTime().getTime());
//            tableMsg.setOverTime(gt.getOverTime().getTime());
        }
        tableMsg.setPlayType(gt.getPlayType());
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
                if (headImg != null && forDetail) {
                    memberMsg.setHeadimgurl(headImg);
                    userHeadImgMap.put(memberMsg.getUserId(), headImg);
                    userHeadImgRefreshMap.put(memberMsg.getUserId(), System.currentTimeMillis());
                }
//                memberMsg.setSex((Integer) memberMap.get("sex"));
                memberMsg.setIsOnLine((Integer) memberMap.get("isOnLine"));
                memberMsg.clearHeadimgurl();//tableMsgMap不要头像信息
                tableMsg.addMembers(memberMsg);
            }
        }
    }

    public void copyDataFakeTable(GroupTableList.TableMsg.Builder tableMsg, GroupFakeTable gft, GroupTable gt, List<Map<String, Object>> fakeHeadImgList, boolean forDetail) {

        tableMsg.setKeyId(gft.getKeyId() * -1);   //假桌子 keyId负值
        tableMsg.setServerId(gt.getServerId());
        tableMsg.setConfigId(gt.getConfigId());
        long fakeTableId =  gt.getTableId() - gft.getKeyId();
        if(fakeTableId < 100000){
            fakeTableId = gt.getTableId() + gft.getKeyId();
        }
        if (fakeTableId > 999999){
            fakeTableId = MathUtil.mt_rand(100000, 999999);
        }
        tableMsg.setTableId(fakeTableId);
        String tableName = gt.getTableName() != null ? gt.getTableName() : null;
        if (tableName != null && tableName.length() > 8) {
            tableName.substring(0, 8);
        }
        tableMsg.setTableName(tableName);
        if (forDetail) {
//            tableMsg.setTableMsg(gt.getTableMsg());
//            tableMsg.setCreditMsg(gt.getCreditMsg());
        }
        tableMsg.setCurrentState("1");
        tableMsg.setType(gt.getType());
        tableMsg.setCurrentCount(gt.getMaxCount());
        tableMsg.setMaxCount(gt.getMaxCount());
        tableMsg.setPlayedBureau(gft.getPlayedBureau());
//        tableMsg.setDealCount(gft.getPlayedBureau());
        tableMsg.setNotStart(false);
//        tableMsg.setIsPrivate(gt.getIsPrivate());
        if (forDetail) {
//            tableMsg.setCreatedTime(gt.getCreatedTime().getTime());
//            tableMsg.setOverTime(gt.getOverTime().getTime());
        }
        tableMsg.setPlayType(gt.getPlayType());

        List<Map<String, Object>> memberHeadImgList  = new ArrayList<>();
        for(Map<String, Object> map : fakeHeadImgList){
            if(Long.parseLong(map.get("fakeTableId").toString()) == gft.getKeyId()){
                memberHeadImgList.add(map);
            }
        }
        if(memberHeadImgList.size() > 0){
            for (Map<String, Object> memberMap : memberHeadImgList) {
                GroupTableList.MemberMsg.Builder memberMsg = GroupTableList.MemberMsg.newBuilder();
                memberMsg.setUserId(Long.parseLong(memberMap.get("keyId").toString()));
                String headImg = memberMap.get("headimgurl") != null ? memberMap.get("headimgurl").toString() : null;
                String name = memberMap.get("name") != null ? memberMap.get("name").toString() : "";
                if (headImg != null) {
                    memberMsg.setHeadimgurl(headImg);
                    memberMsg.setUserName(name);
                    userHeadImgMap.put(memberMsg.getUserId(), headImg);
                    userHeadImgRefreshMap.put(memberMsg.getUserId(), System.currentTimeMillis());
                }
                memberMsg.setIsOnLine(1);
                memberMsg.clearHeadimgurl();    //tableMsgMap不要头像信息
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
                    tableMsgMapForDetail.remove(groupId);
                }
            }
        }

        List<Long> userIds = new ArrayList<>(userHeadImgRefreshMap.keySet());
        now = System.currentTimeMillis();
        for (Long userId : userIds) {
            if (userHeadImgRefreshMap.get(userId) != null && userHeadImgRefreshMap.get(userId) + 60 * 1000 < now) {
                userHeadImgMap.remove(userId);
                userHeadImgRefreshMap.remove(userId);
            }
        }
    }

}
