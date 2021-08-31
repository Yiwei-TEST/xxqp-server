package com.sy599.game.gcommand.group;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sy.mainland.util.CommonUtil;
import com.sy.mainland.util.HttpUtil;
import com.sy.mainland.util.MessageBuilder;
import com.sy.mainland.util.redis.Redis;
import com.sy.mainland.util.redis.RedisUtil;
import com.sy599.game.GameServerConfig;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.common.executor.TaskExecutor;
import com.sy599.game.db.bean.Server;
import com.sy599.game.db.bean.group.GroupInfo;
import com.sy599.game.db.bean.group.GroupTable;
import com.sy599.game.db.bean.group.GroupTableConfig;
import com.sy599.game.db.bean.group.GroupUser;
import com.sy599.game.db.dao.UserDao;
import com.sy599.game.db.dao.group.GroupDao;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.manager.ServerManager;
import com.sy599.game.manager.TableManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.util.*;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.sy599.game.websocket.constant.WebSocketMsgType.res_com_group_tables;

public class GroupCommand extends BaseCommand {

    @Override
    public void execute(Player player, MessageUnit message) throws Exception {
        if(true){
            return;
        }
        ComMsg.ComReq req = (ComMsg.ComReq) this.recognize(ComMsg.ComReq.class, message);
        List<Integer> params = req.getParamsList();
        List<String> strParams = req.getStrParamsList();
        int paramsSize = params == null ? 0 : params.size();
        int strParamsSize = strParams == null ? 0 : strParams.size();
        if (paramsSize == 0) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_3));
            return;
        }

        int groupRoom = strParamsSize > 0 ? Integer.parseInt(strParams.get(0)) : 0;

        int groupId = params.get(0);
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

        int member = paramsSize >= 4 ? params.get(3) : 0;

        GroupUser groupUser = player.getGroupUser();
        if (groupUser == null || groupUser.getGroupId().intValue() != groupId) {
            groupUser = player.loadGroupUser(String.valueOf(groupId));
            if (groupUser == null) {
                player.writeErrMsg(LangMsg.code_63, groupId);
                return;
            }
        }
        int tableOrder = 2;
        GroupInfo group = GroupDao.getInstance().loadGroupInfo(groupId,0);
        if(group != null && StringUtils.isNotBlank(group.getExtMsg())){
            JSONObject json = JSONObject.parseObject(group.getExtMsg());
            String chatStr  = json.getString("tableOrder");
            if("1".equals(chatStr)){
                tableOrder = 1;
            }
        }

        MessageBuilder messageBuilder = MessageBuilder.newInstance()
                .builder("code", 0).builder("groupId", groupId).builder("room",groupRoom).builder("pageNo", pageNo).builder("pageSize", pageSize);

        int config = paramsSize >= 5 ? params.get(4) : 0;

        int loadTableMark = 0;
        //切换按钮切换到大厅时，不强制跳转包间
        int switchRoom = strParamsSize > 1 ? Integer.parseInt(strParams.get(1)) : 1;
        // 获取包厢
        if (groupRoom < 0){
            List<HashMap<String,Object>> list = GroupDao.getInstance().loadSubGroups(groupId,null/**groupUser.getUserRole().intValue()==0?null:"1"**/);
            if (list==null||list.size()==0){
                messageBuilder.builder("list",Collections.emptyList());
                list = createGroupRoom(groupUser);
            }
            if(list != null && list.size() > 0) {
                messageBuilder.builder("list", list);
                List<HashMap<String, Object>> list2 = GroupDao.getInstance().loadAllLastGroupTableConfig(null, String.valueOf(groupId));
                if (list2 != null && list2.size() > 0) {
                    for (HashMap<String, Object> map1 : list) {
                        String parentGroup = String.valueOf(map1.get("parentGroup"));
                        String groupId0 = String.valueOf(map1.get("groupId"));
                        for (HashMap<String, Object> map2 : list2) {
                            if (parentGroup.equals(String.valueOf(map2.get("parentGroup"))) && groupId0.equals(String.valueOf(map2.get("groupId")))) {
                                map1.put("config", map2);
                                break;
                            }
                        }
                        if (map1.containsKey("config")) {
                            HashMap<String, Object> config1 = (HashMap<String, Object>) map1.get("config");
                            map1.put("startedNum", GroupDao.getInstance().countGroupStartedTables(parentGroup, String.valueOf(config1.get("keyId"))));
                        }
                    }
                }
            }
            if (groupRoom==-1){
                loadTableMark=1;
            }else{
                loadTableMark=-1;
            }
            if(list != null && list.size() == 1 && switchRoom != 1 && !"1".equals(ResourcesConfigsUtil.loadServerPropertyValue("group_hall_all_tables"))){
                groupRoom = (Integer)list.get(0).get("groupId");
                messageBuilder.builder("room",groupRoom);
                loadTableMark = 0 ;
            }
        }
        if (groupRoom>0){
            List<HashMap<String,Object>> list = GroupDao.getInstance().loadSubGroupBaseMsgs(groupId,groupRoom);
            if (list==null||list.size()==0){
                loadTableMark = -2;
            }else{
                HashMap<String,Object> map1 = list.get(0);
                messageBuilder.builder("current",map1);

                int groupRoom1 = CommonUtil.object2Int(map1.get("groupId"));
                if (groupRoom1 != groupRoom){
                    groupRoom = groupRoom1;

                    if(!"1".equals(ResourcesConfigsUtil.loadServerPropertyValue("group_hall_all_tables"))) {
                        messageBuilder.builder("room",groupRoom);
                    }
                }

                if (list.size()>=2){
                    HashMap<String,Object> pre = null;
                    HashMap<String,Object> next = null;
                    for (HashMap<String,Object> temp : list){
                        int tempRoom = CommonUtil.object2Int(temp.get("groupId"));
                        if (pre==null&&tempRoom<groupRoom){
                            pre = temp;
                        }else if (next==null&&tempRoom>groupRoom){
                            next = temp;
                        }
                    }
                    if (pre!=null){
                        messageBuilder.builder("pre",pre);
                    }
                    if (next!=null){
                        messageBuilder.builder("next",next);
                    }
                }

            }
        }

        if (config == 1) {
            List<GroupTableConfig> configList;
            if("1".equals(ResourcesConfigsUtil.loadServerPropertyValue("group_table_more_wanfa"))) {  //安化多玩法特殊处理
                configList = groupRoom<=0?GroupDao.getInstance().loadGroupTableConfig2(groupId, 0):GroupDao.getInstance().loadGroupTableConfig2(groupRoom, groupId);
                configList = shrinkageCapacityList(configList);
            }else{
                configList = groupRoom<=0?GroupDao.getInstance().loadGroupTableLastConfig(groupId, 0):GroupDao.getInstance().loadGroupTableLastConfig(groupRoom, groupId);
            }
            messageBuilder.builder("configs", configList == null ? Collections.EMPTY_LIST : configList);
        }

        List<Map<String, Object>> retList;
        List<GroupTable> tableList;

        if (loadTableMark>=0) {
            if (Redis.isConnected() && "1".equals(ResourcesConfigsUtil.loadServerPropertyValue("load_group_tables_from_redis"))) {
                Set<String> sets = RedisUtil.zrevrangeByScore(GroupRoomUtil.loadGroupKey(String.valueOf(groupId), groupRoom), GroupRoomUtil.MAX_WEIGHT, GroupRoomUtil.MIN_WEIGHT, (pageNo - 1) * pageSize, pageSize);
                if (sets == null || sets.size() == 0) {
                    tableList = null;
                } else {
                    List<String> list1 = RedisUtil.hmget(GroupRoomUtil.loadGroupTableKey(String.valueOf(groupId), groupRoom), sets.toArray(new String[sets.size()]));
                    if (list1 == null || list1.size() == 0) {
                        tableList = null;
                    } else {
                        tableList = new ArrayList<>(list1.size());

                        for (String str : list1) {
                            tableList.add(JSON.parseObject(str, GroupTable.class));
                        }
                    }
                }
            } else {
                if(groupRoom <= 0){
                    //大厅
                    if("1".equals(ResourcesConfigsUtil.loadServerPropertyValue("group_hall_all_tables"))){
                        //大厅显示所有房间
                        tableList = GroupDao.getInstance().loadGroupTables(groupId, -1,0, 0,tableOrder, pageNo, pageSize);
                    }else{
                        tableList = GroupDao.getInstance().loadGroupTablesGroupId(groupId, groupRoom,pageNo, pageSize);
                        //大厅每个包间放两个未开局，且未满人的房间
                        List<GroupTableConfig> configs = GroupDao.getInstance().loadGroupTableConfig(0,groupId);
                        if(configs != null && configs.size() > 0){
                            int pSize = 2;
                            int andNotFull = 1;
                            int orStarted = 0;
                            for(GroupTableConfig con : configs){
                                List<GroupTable> tmpList = GroupDao.getInstance().loadGroupTables(groupId, con.getGroupId().intValue(),orStarted, andNotFull,tableOrder, pageNo, pSize);
                                if(tmpList != null && tmpList.size() >0){
                                    tableList.addAll(tmpList);
                                }
                            }
                        }
                    }
                }else{
                    tableList = GroupDao.getInstance().loadGroupTablesGroupId(groupId, groupRoom,pageNo, pageSize);
                }
            }
        }else{
            tableList = null;
        }

        if(tableList != null){
//            if("1".equals(ResourcesConfigsUtil.loadServerPropertyValue("group_hall_all_tables"))) {
//                Collections.sort(tableList);
//            }
            for(GroupTable groupTable : tableList){
                String[] groupFreeStrs = ResourcesConfigsUtil.loadServerPropertyValue("group_" + groupId + "_free_date", "").split("_");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date createdTime = groupTable.getCreatedTime();
                Date date = new Date();
                int currentCount = groupTable.getCurrentCount();
                if(groupFreeStrs != null && groupFreeStrs.length > 1)
                {
                    if ((sdf.parse(groupFreeStrs[0]).before(createdTime) && sdf.parse(groupFreeStrs[1]).after(createdTime)) &&
                            (!(sdf.parse(groupFreeStrs[0]).before(date) && sdf.parse(groupFreeStrs[1]).after(date)) && currentCount == 0)) {
                        GroupDao.getInstance().deleteGroupTableByKeyId(groupTable.getKeyId());
                    }
                }
            }
        }

        int progress = paramsSize >= 6 ? params.get(5) : 0;

        if (tableList != null) {
            retList = new ArrayList<>(tableList.size());
            boolean loadMember = member == 1;
            ArrayList<Map<String, Object>> baseList = new ArrayList<>();

            Map<String, List<HashMap<String, Object>>> membersMap = null;
            if (loadMember) {
                StringBuilder stringBuilder = new StringBuilder();
                for (GroupTable gt : tableList) {
                    BaseTable table = TableManager.getInstance().getTable(gt.getTableId().longValue());
                    String[] ts;
                    if (table != null && table.isGroupRoom() && (ts = table.getServerKey().split("_")).length >= 2 && ts[1].equals(gt.getKeyId().toString())) {
                        if (table.getPlayerCount() > 0) {
                            List<HashMap<String, Object>> members = new ArrayList<>();

                            for (Player player1 : table.getPlayerMap().values()) {
                                HashMap<String, Object> map = new HashMap<>();
                                map.put("userId", player1.getUserId());
                                map.put("headimgurl", player1.getHeadimgurl());
                                map.put("userName", player1.getRawName());
                                map.put("isOnLine", player1.getIsOnline());
                                map.put("sex", player1.getSex());
                                members.add(map);
                            }

                            if (membersMap == null) {
                                membersMap = new HashMap<>();
                            }
                            membersMap.put(gt.getKeyId().toString(), members);
                        }
                    } else if (gt.getCurrentCount().intValue() > 0) {
                        stringBuilder.append(",'").append(gt.getKeyId().toString()).append("'");
                    }
                }
                if (stringBuilder.length() > 0) {
                    List<HashMap<String, Object>> members = GroupDao.getInstance().loadTableUserInfo(stringBuilder.substring(1),group.getGroupId());
                    if (members != null && members.size() > 0) {
                        if (membersMap == null) {
                            membersMap = new HashMap<>();
                        }

                        for (HashMap<String, Object> map : members) {
                            String tableKey = String.valueOf(map.remove("tableNo"));
                            List<HashMap<String, Object>> tempList = membersMap.get(tableKey);
                            if (tempList == null) {
                                tempList = new ArrayList<>();
                                tempList.add(map);
                                membersMap.put(tableKey, tempList);
                            } else {
                                tempList.add(map);
                            }
                        }
                    }
                }
            }

            Set<String> userSet = new HashSet<>();
            Map<String,StringBuilder> serverTableMap = new HashMap<>();
            Map<String,Map<String, Object>> tableIdMap = new HashMap<>();
            for (GroupTable gt : tableList) {
                Map<String, Object> tableMap = (JSONObject) JSONObject.toJSON(gt);
                if (loadMember) {
//					List<HashMap<String, Object>> members = gt.getCurrentCount().intValue() <= 0 ? null : GroupDao.getInstance().loadTableUserInfo(gt.getKeyId().toString());
                    List<HashMap<String, Object>> members = membersMap == null ? null : membersMap.get(gt.getKeyId().toString());
                    if (members == null || members.size() == 0) {
                        tableMap.put("members", Collections.emptyList());
                    } else {
//						List<Map<String, Object>> members0 = new ArrayList<>(members.size());
//						for (HashMap<String, Object> map : members) {
//							Map<String, Object> map0 = loadUserBase(String.valueOf(map.get("userId")));
//							if (map0 != null) {
//								members0.add(map0);
//							}
//						}
                        tableMap.put("members", members);
                    }
                }
                List<Integer> ints;
                String msg = gt.getTableMsg();
                if (msg != null && msg.startsWith("{") && msg.endsWith("}")) {
                    JSONObject json = JSONObject.parseObject(msg);
                    ints = GameConfigUtil.string2IntList(json.getString("ints"),",");
                    String str = json.getString("strs");
                    if (str != null && str.contains(";")) {
                        str = str.substring(0, str.indexOf(";"));
                        if (str.contains("_")) {
                            str = str.split("_")[1];
                            if (userSet.add(str)) {
                                Map<String, Object> map = loadUserBase(str);
                                if (map != null) {
                                    baseList.add(loadMember ? new HashMap<>(map) : map);
                                }
                            }
                        }
                    }
                }else{
                    ints = null;
                }

                if (progress == 1){
                    if (gt.getServerId().equals(String.valueOf(GameServerConfig.SERVER_ID))){
                        BaseTable table = TableManager.getInstance().getTable(gt.getTableId().longValue());
                        if (table != null){
                            if("1".equals(ResourcesConfigsUtil.loadServerPropertyValue("group_table_current_bureau"))){  //俱乐部房间实时局数显示
                                int dealCount = GroupDao.getInstance().selectGroupTableDealCount(gt.getKeyId());
                                tableMap.put("progress0", dealCount);
                            }else{
                                tableMap.put("progress0",table.loadOverCurrentValue());
                            }
                            tableMap.put("progress1",table.loadOverValue());
                        }else{
                            tableMap.put("progress0",0);
                            tableMap.put("progress1", GameUtil.loadOverValue(ints));
                        }
                    }else{
                        if("1".equals(ResourcesConfigsUtil.loadServerPropertyValue("group_table_current_bureau"))){
                            int dealCount = GroupDao.getInstance().selectGroupTableDealCount(gt.getKeyId());
                            tableMap.put("progress0", dealCount);
                            tableMap.put("progress1", GameUtil.loadOverValue(ints));
                        }else{
                            if ("0".equals(gt.getCurrentState())){
                                tableMap.put("progress0",0);
                                tableMap.put("progress1", GameUtil.loadOverValue(ints));
                            }
                        }
                        StringBuilder strBuilder = serverTableMap.get(gt.getServerId());
                        if (strBuilder == null){
                            strBuilder = new StringBuilder();
                            strBuilder.append(gt.getTableId());
                            serverTableMap.put(gt.getServerId(),strBuilder);
                        }else{
                            strBuilder.append(",").append(gt.getTableId());
                        }

                        tableIdMap.put(gt.getTableId().toString(),tableMap);
                    }
                }

                retList.add(tableMap);
            }

            messageBuilder.builder("users", baseList);

            messageBuilder.builder("tableCount", GroupDao.getInstance().countGroupTables(groupId));

            if (serverTableMap.size()>0){
                JSONArray list1 = new JSONArray(tableIdMap.size());
                if (serverTableMap.size() == 1){
                    Map.Entry<String,StringBuilder> kv = serverTableMap.entrySet().iterator().next();
                    JSONArray jsonArray = loadResult(Integer.parseInt(kv.getKey()),kv.getValue().toString());

                    if (jsonArray!=null) {
                        list1.addAll(jsonArray);
                    }
                }else{
                    CountDownLatch countDownLatch = new CountDownLatch(serverTableMap.size());
                    for (Map.Entry<String,StringBuilder> kv:serverTableMap.entrySet()){
                        asynLoad(countDownLatch,list1,Integer.parseInt(kv.getKey()),kv.getValue().toString());
                    }
                    try {
                        //最多等待三秒
                        countDownLatch.await(3, TimeUnit.SECONDS);
                    }catch (Exception e){}

                }

                for (Object jsonObject : list1){
                    JSONObject jsonObject1 = (jsonObject instanceof JSONObject)?(JSONObject)jsonObject:JSONObject.parseObject(jsonObject.toString());
                    Map<String,Object> tableMap = tableIdMap.get(jsonObject1.getString("tableId"));
                    if (tableMap != null){
                        tableMap.putAll(jsonObject1);
                    }
                }
            }
        } else {
            retList = Collections.emptyList();

            messageBuilder.builder("tableCount", 0);
        }

        if("1".equals(ResourcesConfigsUtil.loadServerPropertyValue("group_table_more_wanfa"))) {   //安化数据缩容
            if(retList != null && retList.size() > 0){
                retList = shrinkageCapacityListMap(retList);
            }
        }
        switch (loadTableMark){
            case 0:
                messageBuilder.builder("list", retList);
                break;
            case 1:
                messageBuilder.builder("tables", retList);
                break;
            case -1:
                break;
            case -2:
                messageBuilder.builder("list", retList);
                break;
        }

        player.writeComMessage(res_com_group_tables, groupId, pageNo, pageSize, messageBuilder.toString());
    }

    /**
     * 安化俱乐部房间数据清除无用字段
     * @param listMap
     * @return
     */
    private List<Map<String, Object>> shrinkageCapacityListMap(List<Map<String, Object>> listMap){
        for(Map<String, Object> map : listMap){
            map.remove("playedBureau");
            map.remove("overTime");
            map.remove("configId");
            map.remove("currentCount");
            map.remove("createdTime");
            map.remove("zeroOver");
            map.remove("playing");
            map.remove("currentState");
            map.remove("midwayOver");
            map.remove("normalOver");
            map.remove("dealCount");
            map.remove("over");
        }
        return listMap;
    }

    /**
     * 安化俱乐部玩法信息清除无用字段
     * @param configs
     * @return
     */
    private  List<GroupTableConfig> shrinkageCapacityList( List<GroupTableConfig> configs){
        for(GroupTableConfig config : configs){
            config.setDescMsg(null);
            config.setTableMode(null);
            config.setTableName(null);
            config.setTableOrder(null);
            config.setParentGroup(null);
            config.setPlayCount(null);
        }
        return configs;
    }

    private static Map<String, Object> loadUserBase(String userId) throws Exception {
        Player player = PlayerManager.getInstance().getPlayer(Long.valueOf(userId));
        if (player == null) {
            return UserDao.getInstance().loadUserBase(userId);
        } else {
            Map<String, Object> map = new HashMap<>();
            //userId,headimgurl,userName,isOnLine,sex
            map.put("userId", player.getUserId());
            map.put("headimgurl", player.getHeadimgurl());
            map.put("userName", player.getRawName());
            map.put("isOnLine", player.getIsOnline());
            map.put("sex", player.getSex());
            return map;
        }
    }

    @Override
    public void setMsgTypeMap() {
    }

    protected void asynLoad(final CountDownLatch countDownLatch,final JSONArray jsonArray,final int serverId, final String tableIds){
        TaskExecutor.EXECUTOR_SERVICE.execute(new Runnable() {
            @Override
            public void run() {
                try{
                    JSONArray jsonArray0 = loadResult(serverId,tableIds);
                    if (jsonArray0!=null) {
                        synchronized (this){
                            jsonArray.addAll(jsonArray0);
                        }
                    }
                }finally {
                    countDownLatch.countDown();
                }
            }
        });
    }

    protected static JSONArray loadResult(int serverId, String tableIds) {
        Server server1 = ServerManager.loadServer(serverId);
        if (server1 != null) {
            String url = server1.getIntranet();
            if (StringUtils.isBlank(url)) {
                url = server1.getHost();
            }

            if (StringUtils.isNotBlank(url)) {
                int idx = url.indexOf(".");
                if (idx > 0) {
                    idx = url.indexOf("/", idx);
                    if (idx > 0) {
                        url = url.substring(0, idx);
                    }
                    url += "/group/msg.do?type=progress&tableIds=" + tableIds;
                    String ret = HttpUtil.getUrlReturnValue(url);
                    LogUtil.msgLog.info("group result:url=" + url + ",ret=" + ret);

                    return StringUtils.isNotBlank(ret)?JSONObject.parseObject(ret).getJSONArray("datas"):null;
                }
            }
        }

        return null;
    }

    /**
     * 创建包间
     * @param groupUser
     * @return
     * @throws Exception
     */
    private List<HashMap<String, Object>> createGroupRoom(GroupUser groupUser) throws Exception {
        if( !"1".equals(ResourcesConfigsUtil.loadServerPropertyValue("group_room_mode"))){
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
        int subId = 1 ;
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

        if(subGroupKeyId > 0){
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
            if(configId <= 0 ){
                GroupDao.getInstance().deleteGroupInfoByKeyId(String.valueOf(subGroupKeyId));
            }

        }
        return GroupDao.getInstance().loadSubGroups(groupUser.getGroupId(), null);
    }

}
