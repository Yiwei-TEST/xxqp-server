package com.sy.sanguo.game.pdkuai.db.dao;


import com.sy.sanguo.game.pdkuai.db.bean.ResourcesConfigs;
import java.util.List;

public class ResourcesConfigsDao extends BaseDao {

	private static ResourcesConfigsDao resourcesConfigsDao = new ResourcesConfigsDao();
	
	public static ResourcesConfigsDao getInstance(){
		return resourcesConfigsDao;
	}
	
	public List<ResourcesConfigs> loadAllConfigs() throws Exception {
		return (List<ResourcesConfigs>) this.getSql().queryForList("resources_configs.load_all_configs");
	}
}
