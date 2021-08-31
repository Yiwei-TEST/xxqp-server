package com.sy599.game.db.dao;

import com.sy.mainland.util.CommonUtil;
import com.sy599.game.db.bean.ServerConfig;
import com.sy599.game.util.LogUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerDao extends BaseDao {

	private static ServerDao _inst = new ServerDao();

	public static ServerDao getInstance() {
		return _inst;
	}

	public List<ServerConfig> queryAllServer() {
		try {
			List<HashMap<String,Object>> object = (List<HashMap<String,Object>>) this.getSqlLoginClient().queryForList("server.queryAllServer");
			if (object==null||object.size()==0){
				return null;
			}else{
				List<ServerConfig> serverConfigs = new ArrayList<>(object.size());
				for (HashMap<String,Object> map:object){
					serverConfigs.add(CommonUtil.copyMap2Object(map,new ServerConfig()));
				}
				return serverConfigs;
			}
		} catch (Exception e) {
			LogUtil.e("server.queryAllServer err", e);
		}
		
		return null;
	}

	public int updateServerOnlineCount(int serverId,int onlineCount){
		try {
			Map<String,String> map=new HashMap<>();
			map.put("id",String.valueOf(serverId));
			map.put("onlineCount",String.valueOf(onlineCount));
			return this.getSqlLoginClient().update("server.updateServerOnlineCount",map);
		} catch (SQLException e) {
			LogUtil.e("server.updateServerOnlineCount err:"+e.getMessage(), e);
		}
		return -1;
	}
}
