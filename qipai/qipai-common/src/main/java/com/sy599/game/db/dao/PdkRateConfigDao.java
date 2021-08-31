package com.sy599.game.db.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.sy.mainland.util.CommonUtil;
import com.sy599.game.db.bean.PdkRateConfig;
import com.sy599.game.util.LogUtil;

public class PdkRateConfigDao extends BaseDao {
	
	
	private static PdkRateConfigDao _inst = new PdkRateConfigDao();
	private static List<PdkRateConfig> configs = new ArrayList<>();

	public static PdkRateConfigDao getInstance() {
		return _inst;
	}

	
	public List<PdkRateConfig> queryAllPdkConfig() {
		if(configs.isEmpty()){
			initAllPdkConfig();
		}
		return configs;
	}
	
	public List<PdkRateConfig> initAllPdkConfig() {
		List<PdkRateConfig> configs2 = new ArrayList<>();
		try {
			List<HashMap<String,Object>> object = (List<HashMap<String,Object>>) this.getSqlLoginClient().queryForList("pdk_config.queryAllPdkConfig");
			if (object==null||object.size()==0){
				return configs2;
			}else{
				//List<PdkRateConfig> pdkConfigs = new ArrayList<>(object.size());
				for (HashMap<String,Object> map:object){
					configs2.add(CommonUtil.copyMap2Object(map,new PdkRateConfig()));
				}
				//configs.addAll(pdkConfigs);
				configs = configs2;
				return configs2;
			}
		} catch (Exception e) {
			LogUtil.e("pdk_config.queryAllPdkConfig err", e);
		}
		
		return configs2;
	}
}
