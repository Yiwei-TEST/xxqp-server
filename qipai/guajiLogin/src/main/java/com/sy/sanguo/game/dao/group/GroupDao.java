package com.sy.sanguo.game.dao.group;

import com.sy.mainland.util.CommonUtil;
import com.sy.mainland.util.cache.CacheEntity;
import com.sy.mainland.util.cache.CacheEntityUtil;
import com.sy.sanguo.common.parent.impl.CommonDaoImpl;
import com.sy.sanguo.game.bean.group.*;
import com.sy599.sanguo.util.SysPartitionUtil;
import com.sy599.sanguo.util.TimeUtil;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;

public class GroupDao extends CommonDaoImpl {
    public List<GroupConfig> loadALLGroupConfig() throws Exception{
        CacheEntity<List<GroupConfig>> cacheEntity = CacheEntityUtil.getCache("groupConfigs");
        if (cacheEntity==null){
            List<GroupConfig> list = this.getSqlMapClient().queryForList("group.all_group_config");
            cacheEntity = new CacheEntity<>(list ==null?new ArrayList<GroupConfig>():list,5*60);
            CacheEntityUtil.setCache("groupConfigs",cacheEntity);
        }
        return cacheEntity.getValue();
    }
    public GroupConfig loadGroupConfig(int groupLevel) throws Exception{
        String key="groupConfig_groupLevel_"+groupLevel;
        CacheEntity<GroupConfig> cacheEntity = CacheEntityUtil.getCache(key);
        if (cacheEntity==null){
            Map<String,Object> map=new HashMap<>(8);
            map.put("groupLevel",String.valueOf(groupLevel));
            GroupConfig gc=(GroupConfig)this.getSqlMapClient().queryForObject("group.group_config",map);
            cacheEntity = new CacheEntity<>(gc==null?new GroupConfig():gc,5*60);
            CacheEntityUtil.setCache(key,cacheEntity);
        }

        return cacheEntity.getValue().getGroupLevel()==null?null:cacheEntity.getValue();
    }
    public GroupInfo loadGroupInfo(long groupId,long parentGroup) throws Exception{
        Map<String,Object> map=new HashMap<>(8);
        map.put("groupId",String.valueOf(groupId));
        map.put("parentGroup",String.valueOf(parentGroup));
        return (GroupInfo)this.getSqlMapClient().queryForObject("group.group_info_id",map);
    }

    public boolean existsGroupInfo(long groupId,long parentGroup) throws Exception{
        Map<String,Object> map=new HashMap<>(8);
        map.put("groupId",String.valueOf(groupId));
        map.put("parentGroup",String.valueOf(parentGroup));
        Integer ret=(Integer) this.getSqlMapClient().queryForObject("group.check_group_info_id",map);
        return ret==null||ret.intValue()>0;
    }


    public GroupInfo loadGroupInfoByKeyId(long keyId) throws Exception{
        Map<String,Object> map=new HashMap<>(8);
        map.put("keyId",String.valueOf(keyId));
        return (GroupInfo)this.getSqlMapClient().queryForObject("group.group_info_keyId",map);
    }

    public GroupUser loadGroupMaster(String groupId) throws Exception{
        return (GroupUser)this.getSqlMapClient().queryForObject("group.group_user_master_userId",groupId);
    }

    public HashMap<String,Object> searchGroupInfo(long groupId,long parentGroup) throws Exception{
        Map<String,Object> map=new HashMap<>(8);
        map.put("groupId",String.valueOf(groupId));
        map.put("parentGroup",String.valueOf(parentGroup));
        return (HashMap<String,Object>)this.getSqlMapClient().queryForObject("group.search_group_info_id",map);
    }

    public GroupInfo loadGroupInfo(String groupName,String parentGroup) throws Exception{
        Map<String,Object> map=new HashMap<>(8);
        map.put("groupName",groupName);
        map.put("parentGroup",parentGroup);
        return (GroupInfo)this.getSqlMapClient().queryForObject("group.group_info_name",map);
    }

    public GroupTable loadRandomGroupTable(long modeId) throws Exception{
        Map<String,Object> map=new HashMap<>(8);
        map.put("configId",String.valueOf(modeId));
        Calendar calendar=Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR,-1);
        map.put("myDate",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime()));
        return (GroupTable)this.getSqlMapClient().queryForObject("group.one_group_table_random",map);
    }

    public GroupTable loadGroupTable(long keyId,String groupId) throws Exception{
        Map<String,Object> map=new HashMap<>(8);
        map.put("keyId",String.valueOf(keyId));
        return (GroupTable)this.getSqlMapClient().queryForObject("group.one_group_table",map);
    }

    public GroupTable loadGroupTableByConfigId(long groupId, long configId) throws Exception {
        Map<String,Object> map=new HashMap<>(8);
        map.put("groupId",String.valueOf(groupId));
        map.put("configId",String.valueOf(configId));
        return (GroupTable)this.getSqlMapClient().queryForObject("group.one_group_table_by_configId",map);
    }

    public Integer loadMaxGroupId() throws Exception{
        return (Integer) this.getSqlMapClient().queryForObject("group.group_info_max_groupId");
    }

    public Integer countSubGroup(String parentGroup) throws Exception{
        return (Integer) this.getSqlMapClient().queryForObject("group.group_info_count_parentGroup",parentGroup);
    }

    public List<GroupInfo> loadSubGroups(String parentGroup) throws Exception{
        return (List<GroupInfo>) this.getSqlMapClient().queryForList("group.all_group_info_parentGroup",parentGroup);
    }

    public long createGroup(GroupInfo groupInfo) throws Exception{
        Long ret = (Long)this.getSqlMapClient().insert("group.create_group_info",groupInfo);
        return ret == null ? -1 :ret.longValue();
    }

    public GroupUser loadGroupUser(long userId,long groupId) throws Exception{
        Map<String,Object> map=new HashMap<>(8);
        map.put("userId",String.valueOf(userId));
        if (groupId>0){
            map.put("groupId",String.valueOf(groupId));
        }
        return (GroupUser)this.getSqlMapClient().queryForObject("group.group_user_userId",map);
    }

    public List<GroupUser> loadGroupUsersByUser(long userId,int userRole, int pageNo, int pageSize) throws Exception{
        Map<String,Object> map=new HashMap<>(8);
        map.put("userId",String.valueOf(userId));
        if (userRole>=0){
            map.put("userRole",userRole);
        }
        map.put("startNo",(pageNo - 1) * pageSize);
        map.put("pageSize",pageSize);
        return (List<GroupUser>)this.getSqlMapClient().queryForList("group.group_users_userId",map);
    }
    
    
    
    public List<Long> loadGroupUserIdsByRole(int userRole) throws Exception{
        Map<String,Object> map=new HashMap<>(8);
        map.put("userRole",String.valueOf(userRole));
        return (List<Long>)this.getSqlMapClient().queryForList("group.group_users_userRole",map);
    }
    

    public List<Map<String, Object>> loadGroupManagers(String groupId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        return (List<Map<String, Object>>)this.getSqlMapClient().queryForList("group.loadGroupManagers", map);
    }

    public int loadGroupCount(long userId) throws Exception{
        Map<String,Object> map=new HashMap<>(8);
        map.put("userId",String.valueOf(userId));
        Integer integer=(Integer)this.getSqlMapClient().queryForObject("group.user_all_group_count",map);
        return integer==null?0:integer.intValue();
    }

    public int loadMyGroupCount(long userId) throws Exception{
        Map<String,Object> map=new HashMap<>(8);
        map.put("userId",String.valueOf(userId));
        Integer integer=(Integer)this.getSqlMapClient().queryForObject("group.me_all_group_count",map);
        return integer==null?0:integer.intValue();
    }

    public GroupTableConfig loadGroupTableConfig(int groupId,int parentGroup, String modeMsg) throws Exception{
        Map<String,Object> map=new HashMap<>(8);
        map.put("groupId",String.valueOf(groupId));
        map.put("parentGroup",String.valueOf(parentGroup));
        map.put("modeMsg",String.valueOf(modeMsg));
        return (GroupTableConfig)this.getSqlMapClient().queryForObject("group.one_group_table_config",map);
    }

    public GroupTableConfig loadGroupTableConfig(long keyId) throws Exception{
        return (GroupTableConfig)this.getSqlMapClient().queryForObject("group.one_group_table_config_keyId",String.valueOf(keyId));
    }

    public long createGroupUser(GroupUser groupUser) throws Exception{
        Long ret = (Long)this.getSqlMapClient().insert("group.create_group_user",groupUser);
        return ret == null ? -1 :ret.longValue();
    }

    public List<GroupTableConfig> loadGroupTableConfig(int groupId,int parentGroup) throws Exception{
        Map<String,Object> map=new HashMap<>(8);
        map.put("groupId",String.valueOf(groupId));
        map.put("parentGroup",String.valueOf(parentGroup));
        return (List<GroupTableConfig>)this.getSqlMapClient().queryForList("group.all_group_table_config",map);
    }

    public List<GroupTableConfig> loadGroupTableConfigByPage(int groupId,int parentGroup, int pageNo, int pageSize) throws Exception{
        Map<String,Object> map=new HashMap<>(8);
        map.put("groupId", String.valueOf(groupId));
        map.put("parentGroup", String.valueOf(parentGroup));
        map.put("startNo", (pageNo - 1) * pageSize);
        map.put("pageSize", pageSize);
        return (List<GroupTableConfig>)this.getSqlMapClient().queryForList("group.all_group_table_config_by_page",map);
    }

    public List<HashMap<String,Object>> statisticsMilitaryExploits(String groupId,String startDate,String endDate,int pageNo,int pageSize) throws Exception{
        HashMap<String,Object> map=new HashMap<>(8);
        map.put("groupId",groupId);
        map.put("startDate",startDate);
        map.put("endDate",endDate);
        map.put("startNo",(pageNo-1)*pageSize);
        map.put("pageSize",pageSize);
        map.put("parentGroup","0");
        map.put("gpSeq", SysPartitionUtil.getGroupSeqForRead(Long.valueOf(groupId)));
        return (List<HashMap<String,Object>>)this.getSqlMapClient().queryForList("group.statistics_military_exploits",map);
    }

    public List<HashMap<String,Object>> loadGroupUserRecords(String groupId,String userId,String startDate,String endDate,int pageNo,int pageSize) throws Exception{
        HashMap<String,Object> map=new HashMap<>(16);
        map.put("groupId",groupId);
        map.put("userId",userId);
        map.put("startDate",startDate);
        map.put("endDate",endDate);
        map.put("startNo",(pageNo-1)*pageSize);
        map.put("pageSize",pageSize);
        map.put("parentGroup","0");
        map.put("gpSeq", SysPartitionUtil.getGroupSeqForRead(Long.valueOf(groupId)));
        return (List<HashMap<String,Object>>)this.getSqlMapClient().queryForList("group.load_table_users",map);
    }

    public List<HashMap<String,Object>> loadTableUserInfo(String tableNos,long groupId) throws Exception{
        HashMap<String,Object> map=new HashMap<>(8);
        map.put("tableNos",tableNos);
        map.put("gpSeq", SysPartitionUtil.getGroupSeqForRead(groupId));
        return (List<HashMap<String,Object>>)this.getSqlMapClient().queryForList("group.load_table_user_info",map);
    }

    public List<HashMap<String,Object>> loadGroupTables(String tableNos,String groupId) throws Exception{
        HashMap<String,Object> map=new HashMap<>(8);
        map.put("tableNos",tableNos);
        return (List<HashMap<String,Object>>)this.getSqlMapClient().queryForList("group.load_group_tables",tableNos);
    }

    public List<HashMap<String,Object>> loadGroupTableConfigs(String configIds) throws Exception{
        return (List<HashMap<String,Object>>)this.getSqlMapClient().queryForList("group.load_group_table_configs",configIds);
    }

    public List<HashMap<String,Object>> loadTableRecordByTableNo(String tableNo) throws Exception{
        return (List<HashMap<String,Object>>)this.getSqlMapClient().queryForList("group.load_table_record_tableNo",tableNo);
    }
    
    public List<HashMap<String,Object>> loadTableRecordByTableId(String tableId) throws Exception{
        return (List<HashMap<String,Object>>)this.getSqlMapClient().queryForList("group.load_table_record_tableId",tableId);
    }

    public List<HashMap<String,Object>> loadTableRecords(String groupId,String userId, int pageNo, int pageSize) throws Exception{
        HashMap<String,Object> map=new HashMap<>(8);
        map.put("groupId",groupId);

        if (StringUtils.isNotBlank(userId)){
            map.put("userId",userId);
        }

        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar cal=Calendar.getInstance();
        map.put("endDate",sdf.format(cal.getTime()));
        cal.add(Calendar.DAY_OF_YEAR,-1);
        map.put("startDate",sdf.format(cal.getTime()));
        map.put("startNo", (pageNo - 1) * pageSize);
        map.put("pageSize", pageSize);
        map.put("gpSeq", SysPartitionUtil.getGroupSeqForRead(Long.valueOf(groupId)));
        return (List<HashMap<String,Object>>)this.getSqlMapClient().queryForList(StringUtils.isNotBlank(userId)?"group.load_table_logs":"group.load_group_table_logs",map);
    }

    public Integer loadTableRecordsCount(String groupId,String userId) throws Exception{
        HashMap<String,Object> map=new HashMap<>(8);
        map.put("groupId",groupId);

        if (StringUtils.isNotBlank(userId)){
            map.put("userId",userId);
        }

        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar cal=Calendar.getInstance();
        map.put("endDate",sdf.format(cal.getTime()));
        cal.add(Calendar.DAY_OF_YEAR,-1);
        map.put("startDate",sdf.format(cal.getTime()));
        map.put("gpSeq", SysPartitionUtil.getGroupSeqForRead(Long.valueOf(groupId)));
        return (Integer)this.getSqlMapClient().queryForObject(StringUtils.isNotBlank(userId)?"group.load_table_logs_count":"group.load_group_table_logs_count",map);
    }
    
    
    
    
    public List<HashMap<String,Object>> loadTableRecordsXgmj(String groupId,String userId,String condition,long queryTableId,String startDate,String endDate,int pageNo,int pageSize) throws Exception{
        HashMap<String,Object> map=new HashMap<>(8);
        map.put("groupId",groupId);

        if (StringUtils.isNotBlank(userId)){
            map.put("userId",userId);
        }
        map.put("endDate",endDate);
        if(condition.equals("4")) {
        	map.put("condition",condition);
        }
        map.put("startDate",startDate);
        if(queryTableId != 0) {
        	map.put("queryTableId",queryTableId);
        }
        map.put("startNo", (pageNo-1)*pageSize);
        map.put("pageSize", pageSize);
        map.put("gpSeq", SysPartitionUtil.getGroupSeqForRead(Long.valueOf(groupId)));
        
        return (List<HashMap<String,Object>>)this.getSqlMapClient().queryForList(StringUtils.isNotBlank(userId)?"group.load_table_logs_xgmj":"group.load_group_table_logs_xgmj",map);
    }
    
    
    public int loadTableRecordsXgmjCount(String groupId,String userId,String condition,long queryTableId,String startDate,String endDate) throws Exception{
        HashMap<String,Object> map=new HashMap<>(8);
        map.put("groupId",groupId);

        if (StringUtils.isNotBlank(userId)){
            map.put("userId",userId);
        }
        map.put("endDate",endDate);
        if(condition.equals("4")) {
        	map.put("condition",condition);
        }
        map.put("startDate",startDate);
        if(queryTableId != 0) {
        	map.put("queryTableId",queryTableId);
        }
        map.put("gpSeq", SysPartitionUtil.getGroupSeqForRead(Long.valueOf(groupId)));
        Integer ret=(Integer)this.getSqlMapClient().queryForObject(StringUtils.isNotBlank(userId)?"group.load_table_logs_xgmj_count":"group.load_group_table_logs_xgmj_count",map);
        return ret==null?0:ret.intValue();
    }
    
    

    public List<GroupUser> loadGroupUsers(int groupId,int pageNo,int pageSize,String userGroup) throws Exception{
        return loadGroupUsers(groupId, pageNo, pageSize,null,userGroup);
    }

    public List<GroupUser> loadGroupUsers(int groupId,int pageNo,int pageSize,String keyWord,String userGroup) throws Exception{
        Map<String,Object> map=new HashMap<>(8);
        map.put("groupId",String.valueOf(groupId));
        map.put("startNo",(pageNo-1)*pageSize);
        map.put("pageSize",pageSize);
        if (StringUtils.isNotBlank(keyWord)) {
            map.put("keyWord",keyWord);
        }
        if (StringUtils.isNotBlank(userGroup)) {
            map.put("userGroup",userGroup);
        }
        return (List<GroupUser>)this.getSqlMapClient().queryForList("group.all_group_user",map);
    }

    public List<GroupUser> loadAllGroupUser(long groupId) throws Exception{
        Map<String,Object> map=new HashMap<>(8);
        map.put("groupId",groupId);
        return (List<GroupUser>)this.getSqlMapClient().queryForList("group.loadAllGroupUser",map);
    }


    public List<HashMap<String,Object>> loadGroupUserMsgs(int groupId,int pageNo,int pageSize,String userGroup,String orderRule) throws Exception{
        return loadGroupUserMsgs(groupId, pageNo, pageSize,null,userGroup,orderRule);
    }

    public List<HashMap<String,Object>> loadGroupUserMsgs(int groupId,int pageNo,int pageSize,String keyWord,String userGroup,String orderRule) throws Exception{
        Map<String,Object> map=new HashMap<>(8);
        map.put("groupId",String.valueOf(groupId));
        map.put("startNo",(pageNo-1)*pageSize);
        map.put("pageSize",pageSize);
        if (StringUtils.isNotBlank(keyWord)) {
            map.put("keyWord",keyWord);
        }
        if (StringUtils.isNotBlank(userGroup)) {
            map.put("userGroup",userGroup);
        }
        map.put("orderRule",orderRule);
        
        return (List<HashMap<String,Object>>)this.getSqlMapClient().queryForList("group.all_group_user_msg",map);
    }

    public List<GroupTable> loadGroupTables(long groupId,int pageNo,int pageSize) throws Exception{
        Map<String,Object> map=new HashMap<>(8);
        map.put("groupId",String.valueOf(groupId));
        map.put("startNo",(pageNo-1)*pageSize);
        map.put("pageSize",pageSize);
        return (List<GroupTable>)this.getSqlMapClient().queryForList("group.group_tables_groupId",map);
    }

    public List<GroupTable> loadGroupTablesByConfigIds(long groupId, String configIds) throws Exception{
        Map<String,Object> map=new HashMap<>(8);
        map.put("groupId",String.valueOf(groupId));
        map.put("configIds", configIds);
        return (List<GroupTable>)this.getSqlMapClient().queryForList("group.group_tables_groupId_by_configIds",map);
    }

    public int countGroupTables(Number groupId) throws Exception{
        Map<String,Object> map=new HashMap<>(8);
        map.put("groupId",groupId.toString());
        Integer ret=(Integer)this.getSqlMapClient().queryForObject("group.count_tables_groupId",map);
        return ret==null?0:ret.intValue();
    }

    public Integer countGroupUser(int groupId,String keyWord,String userGroup) throws Exception{
        Map<String,Object> map=new HashMap<>(8);
        map.put("groupId",String.valueOf(groupId));
        if (StringUtils.isNotEmpty(keyWord)) {
            map.put("keyWord",keyWord);
        }
        if (StringUtils.isNotBlank(userGroup)) {
            map.put("userGroup",userGroup);
        }
        return (Integer)this.getSqlMapClient().queryForObject("group.count_group_user_keyword",map);
    }

    public long createGroupTableConfig(GroupTableConfig groupTableConfig) throws Exception{
        Long ret = (Long)this.getSqlMapClient().insert("group.insert_group_table_config",groupTableConfig);
        return ret == null ? -1 :ret.longValue();
    }

    public GroupReview loadGroupReview0(long groupId,long userId,int reviewMode) throws Exception{
        Map<String,Object> map=new HashMap<>(8);
        map.put("groupId",String.valueOf(groupId));
        map.put("userId",String.valueOf(userId));
        map.put("reviewMode",String.valueOf(reviewMode));
        return (GroupReview)this.getSqlMapClient().queryForObject("group.one0_group_review",map);
    }
    public GroupReview loadGroupReviewByKeyId(long keyId) throws Exception{
        Map<String,Object> map=new HashMap<>(8);
        map.put("keyId",String.valueOf(keyId));
        return (GroupReview)this.getSqlMapClient().queryForObject("group.one_group_review_keyId",map);
    }

    public int updateGroupReviewByKeyId(HashMap<String,Object> map) throws Exception{
        return this.getSqlMapClient().update("group.update_group_review_keyId",map);
    }

    public int updateGroupTableConfigByKeyId(HashMap<String,Object> map) throws Exception{
        return this.getSqlMapClient().update("group.update_group_table_config_keyId",map);
    }

    public int updateGroupUser(String groupName,long groupId) throws Exception{
        Map<String,Object> map=new HashMap<>(8);
        map.put("groupId",String.valueOf(groupId));
        map.put("groupName",groupName);
        return this.getSqlMapClient().update("group.update_group_user_groupId",map);
    }
    
    public int updateGroupUserGameInvite(long groupId, long uid, int status) throws Exception{
        Map<String,Object> map=new HashMap<>(8);
        map.put("groupId",String.valueOf(groupId));
        map.put("uid", uid);
        map.put("status", status);
        return this.getSqlMapClient().update("group.update_group_user_refuseInvite",map);
    }

    public int updateGroupInfoByKeyId(HashMap<String,Object> map) throws Exception{
        return this.getSqlMapClient().update("group.update_group_info_keyId",map);
    }

    public int updateGroupInfoCount(int count,long groupId) throws Exception{
        Map<String,Object> map=new HashMap<>(8);
        map.put("groupId",String.valueOf(groupId));
        map.put("parentGroup","0");
        map.put("count",count);
        return this.getSqlMapClient().update("group.update_group_info_count_groupId",map);
    }

    public int countGroupUser(int groupId) throws Exception{
        return (Integer) this.getSqlMapClient().queryForObject("group.count_group_user",String.valueOf(groupId));
    }

    public int updateGroupUserByKeyId(HashMap<String,Object> map) throws Exception{
        return this.getSqlMapClient().update("group.update_group_user_keyId",map);
    }

    public int deleteGroupTableConfigByKeyId(Long keyId) throws Exception{
        return this.getSqlMapClient().delete("group.delete_group_table_config_keyId",keyId.toString());
    }

    public int deleteGroupTableConfig(String groupId,String parentGroup) throws Exception{
        Map<String,Object> map=new HashMap<>(8);
        map.put("groupId",groupId);
        map.put("parentGroup",parentGroup);
        return this.getSqlMapClient().delete("group.update_group_table_config_state",map);
    }

    public int deleteGroupUserByKeyId(Long keyId) throws Exception{
        return this.getSqlMapClient().delete("group.delete_group_user_keyId",keyId.toString());
    }

    public int deleteGroupUsers(List<String> userIds,String groupId,String userGroup) throws Exception{
        if (userIds==null||userIds.size()==0){
            return 0;
        }
        Map<String,Object> map=new HashMap<>(8);
        map.put("groupId",groupId);
        if (StringUtils.isNotBlank(userGroup)) {
            map.put("userGroup", userGroup);
        }
        StringBuilder strBuilder = new StringBuilder();
        for (String userId : userIds){
            strBuilder.append(",'").append(userId).append("'");
        }
        map.put("userIds", strBuilder.substring(1));

        return this.getSqlMapClient().delete("group.delete_group_users",map);
    }

    public int deleteAllGroupUsers(String groupId,String userGroup,String outUser) throws Exception{
        Map<String,Object> map=new HashMap<>(8);
        map.put("groupId",groupId);
        if (StringUtils.isNotBlank(userGroup)) {
            map.put("userGroup", userGroup);
        }
        if (StringUtils.isNotBlank(outUser)) {
            map.put("outUser", outUser);
        }
        return this.getSqlMapClient().delete("group.delete_all_group_users",map);
    }

    public int deleteGroupUserByGroupId(Integer groupId) throws Exception{
        return this.getSqlMapClient().delete("group.delete_group_user_groupId",groupId.toString());
    }

    public int deleteGroupInfoByGroupId(Integer groupId,Integer parentGroup) throws Exception{
        Map<String,Object> map=new HashMap<>(8);
        map.put("groupId",groupId.toString());
        map.put("parentGroup",parentGroup.toString());
        return this.getSqlMapClient().delete("group.delete_group_info_groupId",map);
    }

    public int deleteGroupInfoByParentGroup(Integer parentGroup) throws Exception{
        return this.getSqlMapClient().delete("group.delete_group_info_parentGroup",parentGroup.toString());
    }

    public int deleteGroupInfoByKeyId(String keyId) throws Exception{
        return this.getSqlMapClient().delete("group.delete_group_info_keyId",keyId);
    }

    public long createGroupReview(GroupReview groupReview) throws Exception{
        Long ret = (Long)this.getSqlMapClient().insert("group.create_group_review",groupReview);
        return ret == null ? -1 :ret.longValue();
    }

    public List<GroupReview> loadGroupReviewByUserId(long userId) throws Exception{
        Map<String,Object> map=new HashMap<>(8);
        map.put("userId",String.valueOf(userId));
        return this.getSqlMapClient().queryForList("group.all0_group_review_userId",map);
    }

    public List<GroupReview> loadGroupReviewByGroupId(long groupId) throws Exception{
        Map<String,Object> map=new HashMap<>(8);
        map.put("groupId",String.valueOf(groupId));
        return this.getSqlMapClient().queryForList("group.all0_group_review_groupId",map);
    }

    public List<HashMap<String,Object>> loadGroupTables(String groupId,String userId,String groupRoom,int currentCount0) throws Exception{
        Map<String,Object> map=new HashMap<>(8);
        map.put("groupId",groupId);

        if (StringUtils.isNotBlank(userId)){
            map.put("userId",userId);
        }

        if(currentCount0 > 0){
            map.put("currentCount0",currentCount0);
        }

        if (StringUtils.isNotBlank(groupRoom)){
            if ("ALL".equals(groupRoom)){
            }else if (CommonUtil.isPureNumber(groupRoom)){
                map.put("groupRoom","'%\"room\":\""+groupRoom+"\"%'");
            }else{
                return null;
            }
        }
        return (List<HashMap<String,Object>>)this.getSqlMapClient().queryForList("group.load_group_table_all",map);
    }

    public int getGroupTablesCount(String groupId,String userId,String groupRoom,int currentCount0) throws Exception{
        Map<String,Object> map=new HashMap<>(8);
        map.put("groupId",groupId);

        if (StringUtils.isNotBlank(userId)){
            map.put("userId",userId);
        }

        if(currentCount0 > 0){
            map.put("currentCount0",currentCount0);
        }

        if (StringUtils.isNotBlank(groupRoom)){
            if ("ALL".equals(groupRoom)){
            }else if (CommonUtil.isPureNumber(groupRoom)){
                map.put("groupRoom","'%\"room\":\""+groupRoom+"\"%'");
            }else{
                return 0;
            }
        }
        return (int)this.getSqlMapClient().queryForObject("group.get_group_table_all_count",map);
    }

    public int updateGroupTableByKeyId(String keyId,int currentCount0,String groupId) throws Exception{
        Map<String,Object> map=new HashMap<>(8);
        map.put("keyId",keyId);
        if(currentCount0 > 0){
            map.put("currentCount0",1);
        }
        return this.getSqlMapClient().update("group.update_group_table_keyId",map);
    }

    public List<HashMap<String,Object>> getTableCount(String groupId) throws Exception{
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("group.group_table_count",map);
    }
    public int getTableCountByDate(String groupId, String startDate, String endDate, String playedBureau, String condition) throws Exception{
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        map.put("currentState", condition);
        if (!StringUtils.isBlank(playedBureau)) {
            map.put("playedBureau", playedBureau);
        }
        Object o = this.getSqlMapClient().queryForObject("group.group_table_count_one",map);
        if (o == null) {
            return 0;
        } else {
            return (int) o;
        }
    }

    public GroupTableConfig loadLastGroupTableConfig(long groupId, int parentGroup) throws Exception{
        Map<String,Object> map=new HashMap<>(8);
        map.put("groupId",String.valueOf(groupId));
        map.put("parentGroup",String.valueOf(parentGroup));
        return (GroupTableConfig)this.getSqlMapClient().queryForObject("group.last_one_group_table_config",map);
    }

    public List<GroupTable> getTableCountDetails(String groupId, String startDate, String endDate, int startNO, int pageSize, String playedBureau, String condition) throws Exception{
        Map<String, Object> map = new HashMap<>(16);
        map.put("groupId", groupId);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        map.put("startNo", startNO*pageSize);
        map.put("pageSize", pageSize);
        map.put("currentState", condition);
        if (!StringUtils.isBlank(playedBureau)) {
            map.put("playedBureau", playedBureau);
        }
        return (List<GroupTable>) this.getSqlMapClient().queryForList("group.group_table_count_details",map);
    }

    public List<HashMap<String, Object>> loadGroupMasterIdsByGroupIds(String groupIds) throws Exception{
        Object o = this.getSqlMapClient().queryForList("group.group_master_ids", groupIds);
        return (List<HashMap<String, Object>>) o;
    }

    public List<Integer> getUserGroupTableId(String groupId, String userGroup, long userId, int type ,long promoterId, int promoterLevel,long tableId,String startDate, String endDate, int pageNo, int pageSize) throws Exception{
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        if(userId > 0) {
            map.put("userId", userId);
        }
        if(type >= 0) {
            map.put("type", type);
        }
        if(StringUtils.isNotBlank(userGroup)){
            map.put("userGroup",userGroup);
        }
        if (promoterId != 0) {
            map.put("promoterId" + promoterLevel, promoterId);
        }
        if(tableId > 0){
            map.put("tableId",tableId);
        }
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        map.put("startNo", (pageNo-1)*pageSize);
        map.put("pageSize", pageSize);
        map.put("gpSeq", SysPartitionUtil.getGroupSeqForRead(Long.valueOf(groupId)));
        return (List<Integer>) this.getSqlMapClient().queryForList("group.table_user_tableNo",map);
    }
    public int getUserGroupTableIdCount(String groupId, String userGroup, long userId, int type,long promoterId, int promoterLevel,long tableId, String startDate, String endDate) throws Exception{
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        if(userId > 0) {
            map.put("userId", userId);
        }
        if(type >= 0) {
            map.put("type", type);
        }
        if(StringUtils.isNotBlank(userGroup)){
            map.put("userGroup",userGroup);
        }
        if (promoterId != 0) {
            map.put("promoterId" + promoterLevel, promoterId);
        }
        if(tableId > 0){
            map.put("tableId",tableId);
        }
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        map.put("gpSeq", SysPartitionUtil.getGroupSeqForRead(Long.valueOf(groupId)));
        return (Integer) this.getSqlMapClient().queryForObject("group.table_user_tableNo_count",map);
    }
    public List<GroupTable> getUserPlayLogGroupTable(String tableNoStr,String groupId) throws Exception{
        Map<String, Object> map = new HashMap<>(8);
        map.put("tableNoStr", tableNoStr);
        return (List<GroupTable>) this.getSqlMapClient().queryForList("group.user_playLog_group_table",tableNoStr);
    }
    public List<GroupTable> getPlayLogGroupTable(String groupId, String currentState, long tableId,int type, String startDate, String endDate, int pageNo, int pageSize) throws Exception{
        Map<String, Object> map = new HashMap<>(16);
        map.put("groupId", groupId);
        if(type >= 0){
            map.put("type", type);
        }
        map.put("currentState", currentState);
        if(tableId > 0){
            map.put("tableId", tableId);
        }
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        map.put("startNo", (pageNo-1)*pageSize);
        map.put("pageSize", pageSize);
        return (List<GroupTable>) this.getSqlMapClient().queryForList("group.playLog_group_table",map);
    }
    public int getPlayLogGroupTableCount(String groupId, String currentState, long tableId, int type, String startDate, String endDate) throws Exception{
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        if(type > 0){
            map.put("type", type);
        }
        map.put("currentState", currentState);
        if(tableId > 0){
            map.put("tableId", tableId);
        }
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        return (Integer) this.getSqlMapClient().queryForObject("group.playLog_group_table_count",map);
    }
    public List<HashMap<String, Object>> loadTableUserByTableNo(String tableNoStr,long groupId) throws Exception{
        Map<String, Object> map = new HashMap<>(8);
        map.put("tableNoStr", tableNoStr);
        map.put("gpSeq", SysPartitionUtil.getGroupSeqForRead(Long.valueOf(groupId)));
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("group.loadTableUserByTableNo",map);
    }

    public List<HashMap<String,Object>> loadGroupTeams0(String groupKey,String userGroup) throws Exception{
        HashMap<String, Object> map = new HashMap<>(8);
        map.put("groupKey", groupKey);
        map.put("userGroup", userGroup);
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("group.loadTeams0",map);
    }

    public List<HashMap<String,Object>> loadGroupTeams1(String groupKey,String userGroup) throws Exception{
        HashMap<String, Object> map = new HashMap<>(8);
        map.put("groupKey", groupKey);
        map.put("userGroup", userGroup);
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("group.loadTeams1",map);
    }

    public List<HashMap<String,Object>> loadGroupTeams2(String groupKey,String teamGroup) throws Exception{
        HashMap<String, Object> map = new HashMap<>(8);
        map.put("groupKey", groupKey);
        map.put("teamGroup", teamGroup);
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("group.loadTeams2",map);
    }

    public HashMap<String,Object> loadOneTeam(String keyId) throws Exception{
        HashMap<String, Object> map = new HashMap<>(8);
        map.put("keyId", keyId);
        return (HashMap<String, Object>) this.getSqlMapClient().queryForObject("group.loadOneTeam",map);
    }

    public long saveGroupRelation(HashMap<String, Object> map) throws Exception{
        Long ret = (Long) this.getSqlMapClient().insert("group.saveGroupRelation",map);
        return ret == null ? -1 :ret.longValue();
    }

    public int deleteGroupUserByTeam(String groupId,String userGroup) throws Exception{
        HashMap<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("userGroup", userGroup);
        return this.getSqlMapClient().delete("group.delete_group_user_team",map);
    }

    public int updateTeam(HashMap<String, Object> map) throws Exception{
        return this.getSqlMapClient().update("group.updateTeam",map);
    }

    public int deleteTeam(String keyId) throws Exception{
        HashMap<String, Object> map = new HashMap<>(8);
        map.put("keyId", keyId);
        return this.getSqlMapClient().delete("group.deleteTeam",map);
    }

    public int deleteTeamByGroupKey(String groupKey) throws Exception{
        HashMap<String, Object> map = new HashMap<>(8);
        map.put("groupKey", groupKey);
        return this.getSqlMapClient().delete("group.deleteTeamByGroupKey",map);
    }

    public GroupUser loadGroupTeamMaster(String groupId,String userGroup) throws Exception{
        HashMap<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        if(StringUtils.isNotBlank(userGroup)) {
            map.put("userGroup", userGroup);
        }
        return (GroupUser)this.getSqlMapClient().queryForObject("group.group_user_master_team",map);
    }

    public int loadGroupTeamCount(String groupId,String teamGroup) throws Exception{
        Map<String,Object> map=new HashMap<>(8);
        map.put("groupId",groupId);
        map.put("teamGroup",teamGroup);
        Integer integer=(Integer)this.getSqlMapClient().queryForObject("group.group_all_team_count",map);
        return integer==null?0:integer.intValue();
    }

    public List<HashMap<String,Object>> loadGroupTeamUserData(String dataType,String groupId,String userGroup,String userIds,String startDate,String endDate) throws Exception{
        String sqlId;
        switch (dataType){
            case "zjs":
                sqlId = "group.group_team_user_data_zjs";
                break;
            case "dyj":
                sqlId = "group.group_team_user_data_dyj";
                break;
            default:
                return null;
        }

        Date currentDate = new Date();
        SimpleDateFormat sdf0=new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat sdf1=new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdf2=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if (endDate!=null&&endDate.length()==8){
            Date date=sdf0.parse(endDate);
            if (date.getTime()>currentDate.getTime()){
                endDate = sdf1.format(currentDate)+" 23:59:59";
            }else{
                endDate = sdf1.format(date)+" 23:59:59";
            }
        }else{
            endDate = sdf1.format(currentDate)+" 23:59:59";
        }
        if (startDate!=null&&startDate.length()==8){
            Date date=sdf0.parse(startDate);
            if (date.getTime()>currentDate.getTime()){
                startDate = sdf1.format(currentDate)+" 00:00:00";
            }else{
                startDate = sdf1.format(date)+" 00:00:00";
            }
        }else{
            startDate = sdf1.format(currentDate)+" 00:00:00";
        }
        Date date1 = sdf2.parse(startDate);
        Date date2 = sdf2.parse(endDate);
        if (date1.getTime()>date2.getTime()){
            return null;
        }else if (date2.getTime()-date1.getTime()>31*24*60*60*1000L){
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date2);
            calendar.add(Calendar.DAY_OF_YEAR,-31);

            startDate = sdf2.format(calendar.getTime());
        }

        HashMap<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("userGroup", userGroup);
        map.put("userIds", userIds);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        map.put("gpSeq", SysPartitionUtil.getGroupSeqForRead(Long.valueOf(groupId)));
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList(sqlId,map);
    }

    public int loadGroupTeamData(String dataType,String groupId,String userGroup,String startDate,String endDate) throws Exception{
        String sqlId;
        switch (dataType){
            case "zjs":
                sqlId = "group.group_team_data_zjs";
                break;
            case "dyj":
                sqlId = "group.group_team_data_dyj";
                break;
            default:
                return 0;
        }

        Date currentDate = new Date();
        SimpleDateFormat sdf0=new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat sdf1=new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdf2=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if (endDate!=null&&endDate.length()==8){
            Date date=sdf0.parse(endDate);
            if (date.getTime()>currentDate.getTime()){
                endDate = sdf1.format(currentDate)+" 23:59:59";
            }else{
                endDate = sdf1.format(date)+" 23:59:59";
            }
        }else{
            endDate = sdf1.format(currentDate)+" 23:59:59";
        }
        if (startDate!=null&&startDate.length()==8){
            Date date=sdf0.parse(startDate);
            if (date.getTime()>currentDate.getTime()){
                startDate = sdf1.format(currentDate)+" 00:00:00";
            }else{
                startDate = sdf1.format(date)+" 00:00:00";
            }
        }else{
            startDate = sdf1.format(currentDate)+" 00:00:00";
        }
        Date date1 = sdf2.parse(startDate);
        Date date2 = sdf2.parse(endDate);
        if (date1.getTime()>date2.getTime()){
            return 0;
        }else if (date2.getTime()-date1.getTime()>31*24*60*60*1000L){
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date2);
            calendar.add(Calendar.DAY_OF_YEAR,-31);

            startDate = sdf2.format(calendar.getTime());
        }

        HashMap<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("userGroup", userGroup);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        map.put("gpSeq", SysPartitionUtil.getGroupSeqForRead(Long.valueOf(groupId)));
        return (Integer) this.getSqlMapClient().queryForObject(sqlId,map);
    }

    public int deleteGroupMatch(String groupId) throws Exception{
        Map<String,Object> map=new HashMap<>(8);
        map.put("groupCode",groupId);
        return this.getSqlMapClient().delete("group.delete_group_match_users",map);
    }

    public GroupInfo loadGroupInfoAll(long groupId,long parentGroup) throws Exception{
        Map<String,Object> map=new HashMap<>(8);
        map.put("groupId",String.valueOf(groupId));
        map.put("parentGroup",String.valueOf(parentGroup));
        return (GroupInfo)this.getSqlMapClient().queryForObject("group.group_info_id_all",map);
    }

    public int updateGroupUserCredit(HashMap<String,Object> map) throws Exception{
        return this.getSqlMapClient().update("group.update_group_user_credit",map);
    }

    public int insertGroupCreditLog(HashMap<String,Object> map) throws Exception{
        int res = 0;
        if(SysPartitionUtil.isWriteMaster()) {
            map.put("gpSeq", SysPartitionUtil.getGroupSeqForMaster(Long.valueOf(String.valueOf(map.get("groupId")))));
            res = this.getSqlMapClient().update("group.insert_group_credit_log", map);
        }
        if(SysPartitionUtil.isWritePartition()) {
            map.put("gpSeq", SysPartitionUtil.getGroupSeqForPartition(Long.valueOf(String.valueOf(map.get("groupId")))));
            this.getSqlMapClient().update("group.insert_group_credit_log", map);
        }
        return res;
    }

    public List<HashMap<String, Object>> loadGroupUserMsgsCredit(int groupId, int pageNo, int pageSize, String keyWord,String creditOrder) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", String.valueOf(groupId));
        map.put("startNo", (pageNo - 1) * pageSize);
        map.put("pageSize", pageSize);
        if (StringUtils.isNotBlank(keyWord)) {
            map.put("keyWord", keyWord);
        }
        if (StringUtils.isNotBlank(creditOrder)) {
            map.put("creditOrder", creditOrder);
        }
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("group.all_group_user_msg_credit", map);
    }

    public List<HashMap<String, Object>> loadGroupCreditLog(Map<String,Object> params) throws Exception {
        params.put("gpSeq", SysPartitionUtil.getGroupSeqForRead(Long.valueOf(String.valueOf(params.get("groupId")))));
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("group.load_group_credit_log", params);
    }
    public int countGroupCreditLog(Map<String,Object> params) throws Exception {
        params.put("gpSeq", SysPartitionUtil.getGroupSeqForRead(Long.valueOf(String.valueOf(params.get("groupId")))));
        return (Integer) this.getSqlMapClient().queryForObject("group.count_group_credit_log", params);
    }

    public int countAnyGroupCreditLog(Map<String,Object> params) throws Exception {
        params.put("gpSeq", SysPartitionUtil.getGroupSeqForRead(Long.valueOf(String.valueOf(params.get("groupId")))));
        return (Integer) this.getSqlMapClient().queryForObject("group.count_any_group_credit_log", params);
    }


    public List<HashMap<String, Object>> loadGroupTeamsCredit(String groupId, String userGroup, int pageNo, int pageSize) throws Exception {
        HashMap<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("startNo", (pageNo - 1) * pageSize);
        map.put("pageSize", pageSize);
        if (StringUtils.isNotBlank(userGroup)) {
            map.put("userGroup", userGroup);
        }
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("group.loadGroupTeamsCredit", map);
    }

    public List<HashMap<String, Object>> loadGroupRelationCredit(String groupId, String userGroup) throws Exception {
        HashMap<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        if (StringUtils.isNotBlank(userGroup)) {
            map.put("userGroup", userGroup);
        }
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("group.loadGroupRelation", map);
    }

    public List<HashMap<String, Object>> loadGroupTeamUsersCredit(String groupId, String userGroup, String keyWord, int pageNo, int pageSize) throws Exception {
        HashMap<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        if(StringUtils.isNotBlank(userGroup)) {
            map.put("userGroup", userGroup);
        }
        map.put("startNo", (pageNo - 1) * pageSize);
        map.put("pageSize", pageSize);
        if(StringUtils.isNotBlank(keyWord)){
            map.put("keyWord", keyWord);
        }
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("group.loadGroupTeamUsersCredit", map);
    }

    /**
     * 转移信用分接口
     *
     * @param fromUserId
     * @param destUserId
     * @param credit     必须是正数
     * @return
     * @throws Exception
     */
    public int transferGroupUserCredit(long fromUserId, long destUserId, long groupId, int credit) throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        map.put("fromUserId", fromUserId);
        map.put("destUserId", destUserId);
        map.put("groupId", groupId);
        map.put("credit", Math.abs(credit));
        return this.getSqlMapClient().update("group.transferGroupUserCredit", map);
    }

    /**
     * 转移零钱包到信用分接口
     *
     * @param fromUserId
     * @param destUserId
     * @param credit     必须是正数
     * @return
     * @throws Exception
     */
    public int transferGroupUserPurseToCredit(long fromUserId, long destUserId, long groupId, int credit) throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        map.put("fromUserId", fromUserId);
        map.put("destUserId", destUserId);
        map.put("groupId", groupId);
        map.put("credit", Math.abs(credit));
        return this.getSqlMapClient().update("group.transferGroupUserPurseToCredit", map);
    }

    /**
     * 转移信用分到tempCredit接口
     *
     * @param fromUserId
     * @param destUserId
     * @param credit     必须是正数
     * @return
     * @throws Exception
     */
    public int transferGroupUserTempCredit(long fromUserId, long destUserId, long groupId, int credit) throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        map.put("fromUserId", fromUserId);
        map.put("destUserId", destUserId);
        map.put("groupId", groupId);
        map.put("credit", Math.abs(credit));
        return this.getSqlMapClient().update("group.transferGroupUserTempCredit", map);
    }

    /**
     * 转移零钱包到tempCredit接口
     *
     * @param fromUserId
     * @param destUserId
     * @param credit     必须是正数
     * @return
     * @throws Exception
     */
    public int transferGroupUserPurseToTempCredit(long fromUserId, long destUserId, long groupId, int credit) throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        map.put("fromUserId", fromUserId);
        map.put("destUserId", destUserId);
        map.put("groupId", groupId);
        map.put("credit", Math.abs(credit));
        return this.getSqlMapClient().update("group.transferGroupUserPurseToTempCredit", map);
    }

    /**
     * 转移信用转盘奖池到玩家信用分接口
     *
     * @param userId
     * @param groupId
     * @param credit     必须是正数
     * @return
     * @throws Exception
     */
    public int transferWheelToGroupUserCredit(long userId,  long groupId, int credit) throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("groupId", groupId);
        map.put("credit", Math.abs(credit));
        return this.getSqlMapClient().update("group.transferWheelToGroupUserCredit", map);
    }

    /**
     * 转移玩家信用分到信用转盘奖池接口
     *
     * @param userId
     * @param groupId
     * @param credit     必须是正数
     * @return
     * @throws Exception
     */
    public int transferGroupUserCreditToWheel(long userId,  long groupId, int credit) throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("groupId", groupId);
        map.put("credit", Math.abs(credit));
        return this.getSqlMapClient().update("group.transferGroupUserCreditToWheel", map);
    }


    public long sumTeamCredit(String groupId, String userGroup) throws Exception {
        HashMap<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        if (StringUtils.isNotBlank(userGroup)) {
            map.put("userGroup", userGroup);
        }
        return (Long) this.getSqlMapClient().queryForObject("group.sumTeamCredit", map);
    }


    public List<HashMap<String, Object>> loadGroupTeamCommissionCreditLog(String groupId, String userId, String dateType) throws Exception {

        int dayOffset = TimeUtil.getDayOffset(dateType);
        String startDate = TimeUtil.getStartOfDay(dayOffset);
        String endDate = TimeUtil.getEndOfDay(dayOffset);
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("userId", userId);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        map.put("type", 2);
        map.put("gpSeq", SysPartitionUtil.getGroupSeqForRead(Long.valueOf(groupId)));
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("group.load_group_team_commission_credit_log", map);
    }

    public Long sumGroupTeamCommissionCreditLog(String groupId, String userId,String userGroup, String dateType) throws Exception {

        int dayOffset = TimeUtil.getDayOffset(dateType);
        String startDate = TimeUtil.getStartOfDay(dayOffset);
        String endDate = TimeUtil.getEndOfDay(dayOffset);
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("userId", userId);
        if (StringUtils.isNotBlank(userGroup)) {
            map.put("userGroup", userGroup);
        }
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        map.put("type", 2);
        map.put("gpSeq", SysPartitionUtil.getGroupSeqForRead(Long.valueOf(groupId)));
        Long res = (Long)this.getSqlMapClient().queryForObject("group.sum_group_team_commission_credit_log", map);
        return res != null ? res:0;
    }

    /**
     * 小组信用房总局数
     *
     * @param groupId
     * @param dateType 1:今天,2:昨天,3:前天
     * @return
     * @throws Exception
     */
    public List<HashMap<String, Object>> loadGroupTeamCreditZjs(String groupId, String dateType) throws Exception {

        int dayOffset = TimeUtil.getDayOffset(dateType);
        String startDate = TimeUtil.getStartOfDay(dayOffset);
        String endDate = TimeUtil.getEndOfDay(dayOffset);

        HashMap<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        map.put("gpSeq", SysPartitionUtil.getGroupSeqForRead(Long.valueOf(groupId)));
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("group.group_team_credit_zjs", map);
    }

    public Integer countGroupTeamUserCreditLog(String groupId,  String userId, String userGroup, String keyWord, String dateType) throws Exception {

        int dayOffset = TimeUtil.getDayOffset(dateType);
        String startDate = TimeUtil.getStartOfDay(dayOffset);
        String endDate = TimeUtil.getEndOfDay(dayOffset);

        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        if (StringUtils.isNotBlank(userGroup)) {
            map.put("userGroup", userGroup);
        }
        if (StringUtils.isNotBlank(keyWord)) {
            map.put("keyWord", keyWord);
        }
        map.put("userId", userId);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        map.put("type", 2);
        map.put("gpSeq", SysPartitionUtil.getGroupSeqForRead(Long.valueOf(groupId)));
        Integer res = (Integer) this.getSqlMapClient().queryForObject("group.count_group_team_user_credit_log", map);
        return res != null ? res : 0;
    }

    public List<HashMap<String, Object>> loadGroupTeamUserCreditLog(String groupId, String userId, String userGroup, String keyWord, String dateType, int pageNo, int pageSize ) throws Exception {

        int dayOffset = TimeUtil.getDayOffset(dateType);
        String startDate = TimeUtil.getStartOfDay(dayOffset);
        String endDate = TimeUtil.getEndOfDay(dayOffset);

        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        if (StringUtils.isNotBlank(userGroup)) {
            map.put("userGroup", userGroup);
        }
        if (StringUtils.isNotBlank(keyWord)) {
            map.put("keyWord", keyWord);
        }
        map.put("userId", userId);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        map.put("type", 2);
        map.put("startNo",(pageNo - 1) * pageSize);
        map.put("pageSize",pageSize);
        map.put("gpSeq", SysPartitionUtil.getGroupSeqForRead(Long.valueOf(groupId)));
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("group.load_group_team_user_credit_log", map);
    }

    /**
     * 小组信用房总局数
     *
     * @param groupId
     * @param dateType 1:今天,2:昨天,3:前天
     * @return
     * @throws Exception
     */
    public List<HashMap<String, Object>> loadGroupTeamUserCreditZjs(String groupId,String userGroup, String dateType) throws Exception {

        int dayOffset = TimeUtil.getDayOffset(dateType);
        String startDate = TimeUtil.getStartOfDay(dayOffset);
        String endDate = TimeUtil.getEndOfDay(dayOffset);

        HashMap<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("userGroup", userGroup);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        map.put("gpSeq", SysPartitionUtil.getGroupSeqForRead(Long.valueOf(groupId)));
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("group.group_team_user_credit_zjs", map);
    }


    public Long sumGroupTeamCredit(String groupId, String userGroup) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("userGroup", userGroup);
        Long res = (Long) this.getSqlMapClient().queryForObject("group.sum_group_team_credit", map);
        return res != null ? res : 0;
    }

    public List<Integer> loadGroupRoomId(String parentGroup) throws Exception{
        return (List<Integer>) this.getSqlMapClient().queryForList("group.load_group_room_id",parentGroup);
    }

    public GroupTableConfig loadOneGroupTableConfig(int parentGroup) throws Exception{
        Map<String,Object> map=new HashMap<>(8);
        map.put("parentGroup",String.valueOf(parentGroup));
        return (GroupTableConfig)this.getSqlMapClient().queryForObject("group.loadOneGroupTableConfig",map);
    }

    public Integer countInactiveUser(int groupId, String dateType) throws Exception {
        int dayOffset = -30;
        if ("2".equals(dateType)) {
            dayOffset = -60;
        } else if ("3".equals(dateType)) {
            dayOffset = -90;
        }
        String endDate = TimeUtil.getStartOfDay(dayOffset);
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("endDate", endDate);
        Integer res = (Integer) this.getSqlMapClient().queryForObject("group.countInactiveUser", map);
        return res != null ? res : 0;
    }

    public List<HashMap<String, Object>> loadInactiveUser(int groupId, String dateType, int pageNo, int pageSize) throws Exception {
        int dayOffset = -30;
        if ("2".equals(dateType)) {
            dayOffset = -60;
        } else if ("3".equals(dateType)) {
            dayOffset = -90;
        }
        String endDate = TimeUtil.getStartOfDay(dayOffset);
        HashMap<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("endDate", endDate);
        map.put("startNo", (pageNo - 1) * pageSize);
        map.put("pageSize", pageSize);
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("group.loadInactiveUser", map);
    }

    public int fireInactiveUser(List<String> userIds, long groupId) throws Exception {
        if (userIds == null || userIds.size() == 0) {
            return 0;
        }
        Map<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        StringBuilder strBuilder = new StringBuilder();
        for (String userId : userIds) {
            strBuilder.append(",'").append(userId).append("'");
        }
        map.put("userIds", strBuilder.substring(1));

        return this.getSqlMapClient().delete("group.fireInactiveUser", map);
    }

    public List<GroupReview> loadGroupReviewByUser(long userId, long groupId) throws Exception {
        Map<String, Object> map = new HashMap<>(8);
        map.put("userId", String.valueOf(userId));
        map.put("groupId", String.valueOf(groupId));
        return (List<GroupReview>) this.getSqlMapClient().queryForList("group.loadGroupReviewByUser", map);
    }

    public Integer countGroupRoom(long groupId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", groupId);
        Integer res = (Integer) this.getSqlMapClient().queryForObject("group.countGroupRoom", map);
        return res != null ? res : 0;
    }


    public List<HashMap<String, Object>> loadGroupDataZjsDyj(String groupId, String startDate, String endDate) throws Exception {
        Date currentDate = new Date();
        SimpleDateFormat sdf0 = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (endDate != null && endDate.length() == 8) {
            Date date = sdf0.parse(endDate);
            if (date.getTime() > currentDate.getTime()) {
                endDate = sdf1.format(currentDate) + " 23:59:59";
            } else {
                endDate = sdf1.format(date) + " 23:59:59";
            }
        } else {
            endDate = sdf1.format(currentDate) + " 23:59:59";
        }
        if (startDate != null && startDate.length() == 8) {
            Date date = sdf0.parse(startDate);
            if (date.getTime() > currentDate.getTime()) {
                startDate = sdf1.format(currentDate) + " 00:00:00";
            } else {
                startDate = sdf1.format(date) + " 00:00:00";
            }
        } else {
            startDate = sdf1.format(currentDate) + " 00:00:00";
        }
        Date date1 = sdf2.parse(startDate);
        Date date2 = sdf2.parse(endDate);
        if (date1.getTime() > date2.getTime()) {
            return null;
        } else if (date2.getTime() - date1.getTime() > 31 * 24 * 60 * 60 * 1000L) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date2);
            calendar.add(Calendar.DAY_OF_YEAR, -31);

            startDate = sdf2.format(calendar.getTime());
        }

        HashMap<String, Object> map = new HashMap<>(8);
        map.put("groupId", groupId);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        map.put("gpSeq", SysPartitionUtil.getGroupSeqForRead(Long.valueOf(groupId)));
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("group.group_data_zjs_dyj", map);
    }

}
