package com.sy.sanguo.game.pdkuai.db.dao;

import com.sy.mainland.util.cache.CacheEntity;
import com.sy.mainland.util.cache.CacheEntityUtil;
import com.sy.sanguo.game.bean.group.GroupConfig;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AgencyShowDao extends BaseDao  {
    private static AgencyShowDao _inst = new AgencyShowDao();

    public static AgencyShowDao getInstance() {
        return _inst;
    }


    public List<String> selectAgencyShow() {
        try {
            String key="selectAgencyShow";
            CacheEntity<List<String>> cacheEntity = CacheEntityUtil.getCache(key);
            if (cacheEntity==null){
                List<String>  list =getSql().queryForList("agency_show.selectAgencyShow");
                cacheEntity = new CacheEntity<>(list==null?new ArrayList<String>():list,5*60);
                CacheEntityUtil.setCache(key,cacheEntity);
            }
            return cacheEntity.getValue();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
