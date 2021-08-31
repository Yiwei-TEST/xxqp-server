package com.sy.sanguo.game.dao;
 
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.game.bean.GroupServer;
import com.sy.sanguo.game.bean.ServerConfig;
import com.sy.sanguo.game.pdkuai.db.dao.BaseDao;

public class ServerDaoImpl extends BaseDao {

	private static ServerDaoImpl _inst = new ServerDaoImpl();

	public static ServerDaoImpl getInstance() {
		return _inst;
	}
	
	@SuppressWarnings("unchecked")
	public List<GroupServer> loadServer() throws Exception {
		return (List<GroupServer>) getSql().queryForList("groupserver.selectSystemServer");
	}
	
	public int updateServerConfig(Map<String, Object> param) {
		int cont = 0;
		try {
			Object object = getSql().update("server.updateServerConfig", param);
			if (object != null) {
				cont = (int) object;
			}
		} catch (SQLException e) {
			GameBackLogger.SYS_LOG.error("server.updateServerConfig err", e);
		}
		
		return cont;
	}

	public List<ServerConfig> queryAllServer() {

		List<ServerConfig> serverConfigs = null;
		try {
			Object object = this.getSql().queryForList("server.queryAllServer");
			serverConfigs = (List<ServerConfig>) object;
		} catch (SQLException e) {
			GameBackLogger.SYS_LOG.error("server.queryAllServer err", e);
		}

		return serverConfigs;
	}

	public ServerConfig queryServer(int serverId){
		try {
			return (ServerConfig)this.getSql().queryForObject("server.queryServer",String.valueOf(serverId));
		} catch (SQLException e) {
			GameBackLogger.SYS_LOG.error("server.queryServer err", e);
		}
		return null;
	}
	
	public void updateServerByMap(int id,Map<String,Object> params ){
		try {
			params.put("id", id);
			 getSql().update("server.updateServerByMap", params);
		} catch (SQLException e) {
			GameBackLogger.SYS_LOG.error("server.updateServerByMap err", e);
		}
		
	}
	
	public void clearServerConfig(){
		try {
			 getSql().delete("server.clearServerConfig");
		} catch (SQLException e) {
			GameBackLogger.SYS_LOG.error("server.clearServerConfig err", e);
		}
	}
	
}
