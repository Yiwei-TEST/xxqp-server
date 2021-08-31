package com.sy.sanguo.game.dao;

import com.sy.sanguo.common.parent.impl.CommonDaoImpl;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataStatisticsDao extends CommonDaoImpl {
    public List<Map<String,Object>> loadCurrentDataStatisticsList1(String dataDate,String dataCode,String dataType,String userIdKey ,int pageNo,int pageSize) throws Exception{
        Map<String,Object> map=new HashMap<>();
        map.put("dataDate",dataDate);
        map.put("dataCode",dataCode);
        map.put("dataType",dataType);
        if(StringUtils.isNotBlank(userIdKey)){
            map.put("userIdKey",userIdKey);
        }
        map.put("startNo",(pageNo - 1) * pageSize);
        map.put("pageSize",pageSize);
        return (List<Map<String,Object>>)this.getSqlMapClient().queryForList("dataStatistics.page_range_desc",map);
    }

    public List<Map<String,Object>> loadCurrentDataStatisticsList2(String dataDate,String dataCode,String dataType,int pageNo,int pageSize) throws Exception{
        Map<String,Object> map=new HashMap<>();
        map.put("dataDate",dataDate);
        map.put("dataCode",dataCode);
        map.put("dataType",dataType);
        map.put("startNo",(pageNo - 1) * pageSize);
        map.put("pageSize",pageSize);
        return (List<Map<String,Object>>)this.getSqlMapClient().queryForList("dataStatistics.page_range_asc",map);
    }

    public List<Map<String,Object>> loadCurrentDataStatisticsList4(String dataDate,String dataCode,String dataType,int pageNo,int pageSize, String userIdStr) throws Exception{
        Map<String,Object> map=new HashMap<>();
        map.put("dataDate",dataDate);
        map.put("dataCode",dataCode);
        map.put("dataType",dataType);
        map.put("startNo",(pageNo - 1) * pageSize);
        map.put("pageSize",pageSize);
        map.put("userIdStr", userIdStr);
        return (List<Map<String,Object>>)this.getSqlMapClient().queryForList("dataStatistics.page_range_returnOne_desc",map);
    }

    public List<Map<String,Object>> loadRangeDataStatisticsList1(Integer startDate,Integer endDate,String dataCode,String dataType,String userIdKey,int pageNo,int pageSize,String orderRule) throws Exception{
        Map<String,Object> map=new HashMap<>();
        map.put("startDate",startDate);
        map.put("endDate",endDate);
        map.put("dataCode",dataCode);
        map.put("dataType",dataType);
        if(StringUtils.isNotBlank(userIdKey)){
           map.put("userIdKey",userIdKey);
        }
        map.put("startNo",(pageNo - 1) * pageSize);
        map.put("pageSize",pageSize);
        
        map.put("orderRule", orderRule);
        
        return (List<Map<String,Object>>)this.getSqlMapClient().queryForList("dataStatistics.page_range_sum",map);
    }

    public List<Map<String,Object>> loadRangeDataStatisticsList2(Integer startDate,Integer endDate,String dataCode,String dataType,int pageNo,int pageSize) throws Exception{
        Map<String,Object> map=new HashMap<>();
        map.put("startDate",startDate);
        map.put("endDate",endDate);
        map.put("dataCode",dataCode);
        map.put("dataType",dataType);
        map.put("startNo",(pageNo - 1) * pageSize);
        map.put("pageSize",pageSize);
        return (List<Map<String,Object>>)this.getSqlMapClient().queryForList("dataStatistics.page_range_max",map);
    }

    public List<Map<String,Object>> loadRangeDataStatisticsList3(Integer startDate,Integer endDate,String dataCode,String dataType,int pageNo,int pageSize) throws Exception{
        Map<String,Object> map=new HashMap<>();
        map.put("startDate",startDate);
        map.put("endDate",endDate);
        map.put("dataCode",dataCode);
        map.put("dataType",dataType);
        map.put("startNo",(pageNo - 1) * pageSize);
        map.put("pageSize",pageSize);
        return (List<Map<String,Object>>)this.getSqlMapClient().queryForList("dataStatistics.page_range_min",map);
    }

    public List<Map<String,Object>> loadRangeDataStatisticsList4(Integer startDate,Integer endDate,String dataCode,String dataType,int pageNo,int pageSize, String userIdStr) throws Exception{
        Map<String,Object> map=new HashMap<>();
        map.put("startDate",startDate);
        map.put("endDate",endDate);
        map.put("dataCode",dataCode);
        map.put("dataType",dataType);
        map.put("startNo",(pageNo - 1) * pageSize);
        map.put("pageSize",pageSize);
        map.put("userIdStr", userIdStr);
        return (List<Map<String,Object>>)this.getSqlMapClient().queryForList("dataStatistics.page_range_sum_returnOne",map);
    }

    /**
     * 所有游戏俱乐部牌局统计排行榜
     */
    public List<Map<String,Object>> loadCurrentDataStatisticsList5(Integer dataDateMark, String dataType, int pageNo, int pageSize) throws Exception{
        Map<String,Object> map=new HashMap<>();
        map.put("dataDate",dataDateMark);
        map.put("dataType",dataType);
        map.put("startNo",(pageNo - 1) * pageSize);
        map.put("pageSize",pageSize);
        return (List<Map<String,Object>>)this.getSqlMapClient().queryForList("dataStatistics.all_club_range_count",map);
    }

    /**
     * 百万大奖俱乐部牌局统计排行榜
     */
    public int loadDataStatisticsList6(Integer dataDateMark, String dataType, String dataCode) throws Exception{
        Map<String,Object> map=new HashMap<>();
        map.put("dataDate",dataDateMark);
        map.put("dataType",dataType);
        map.put("dataCode", dataCode);
        Object o = this.getSqlMapClient().queryForObject("dataStatistics.megabucks_club_count",map);
        if (o == null) {
            return 0;
        } else {
            return (int) o;
        }
    }

    public long loadSumDataStatisticsList(Integer startDate,Integer endDate,String dataCode,String dataType,String userIdKey) throws Exception{
        Map<String,Object> map=new HashMap<>();
        map.put("startDate",startDate);
        map.put("endDate",endDate);
        map.put("dataCode",dataCode);
        map.put("dataType",dataType);
        if(StringUtils.isNotBlank(userIdKey)){
            map.put("userIdKey",userIdKey);
        }
        return (Long)this.getSqlMapClient().queryForObject("dataStatistics.data_type_sum",map);
    }

    public List<HashMap<String,Object>> loadGroupDjs(String groupId)throws Exception{
        Map<String, Object> map = new HashMap<>(8);
        map.put("dataCode", "group"+groupId);
        map.put("dataType", "jlbDjs");
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("dataStatistics.load_group_djs",map);
    }
    
    public List<HashMap<String,Object>> loadGroupDecDiamond(String groupId)throws Exception{
        Map<String, Object> map = new HashMap<>(8);
        map.put("dataCode", "group"+groupId);
        map.put("userId", ""+groupId);
        map.put("dataType", "decDiamond");
        return (List<HashMap<String, Object>>) this.getSqlMapClient().queryForList("dataStatistics.load_group_decdiamond",map);
    }
    
    
    
    
}
