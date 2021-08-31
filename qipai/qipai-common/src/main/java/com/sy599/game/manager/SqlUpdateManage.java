package com.sy599.game.manager;

import com.sy599.game.common.datasource.DataSourceManager;
import com.sy599.game.db.dao.SystemCommonInfoDao;
import com.sy599.game.util.Constants;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.gold.constants.GoldConstans;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * sql更新语句
 * 
 * @author lc
 * 
 */
public class SqlUpdateManage {
	private static SqlUpdateManage _inst = new SqlUpdateManage();
	private static Map<String, String> sqlLoginMap = new LinkedHashMap<String, String>();
	private static Map<String, String> sqlLoginMap2 = new LinkedHashMap<>();

	public static SqlUpdateManage getInstance() {
		return _inst;
	}

	static {

		for (Entry kv:TableManager.wanfaTableTypes.entrySet()){
			sqlLoginMap.put("playType"+kv.getKey().toString(),"ALTER TABLE roomcard_consume_statistics ADD COLUMN playType"+kv.getKey().toString()+" int(11) DEFAULT '0' COMMENT 'playType"+kv.getKey().toString()+"'");
			if (GoldConstans.isGoldSiteOpen()) {
				sqlLoginMap2.put("playType"+kv.getKey().toString(),"ALTER TABLE roomgold_consume_statistics ADD COLUMN playType"+kv.getKey().toString()+" int(11) DEFAULT '0' COMMENT 'playType"+kv.getKey().toString()+"'");
			}
		}

	}

	/**
	 * 检查是否有sql更新
	 */
	public void check() {
		checkLoginSql();
	}

	private void checkLoginSql() {

		try{
			HashMap<String,Object> map = new HashMap<>(8);
			map.put("tableName","t_gold_room");
			map.put("dbName",DataSourceManager.getLoginDbName());
			Long initValue = (Long)DataSourceManager.getLoginSqlMapClient().queryForObject("check_table.select_table_auto_increment",map);
			if (initValue != null && initValue.longValue() < Constants.MIN_GOLD_ID){
				map.put("initValue",Constants.MIN_GOLD_ID);
				DataSourceManager.getLoginSqlMapClient().update("check_table.update_table_auto_increment",map);
			}
		}catch (Exception e){
			LogUtil.errorLog.error("Exception:"+e.getMessage(),e);
		}

		//检查表是否存在,不存在就创建表
		try{
			DataSourceManager.getLoginSqlMapClient().update("check_table.t_user_statistics");
		}catch (Exception e){
			LogUtil.errorLog.error("Exception:"+e.getMessage(),e);
		}

		try{
			DataSourceManager.getLoginSqlMapClient().update("check_table.t_login_data");
		}catch (Exception e){
			LogUtil.errorLog.error("Exception:"+e.getMessage(),e);
		}

		try{
			DataSourceManager.getLoginSqlMapClient().update("check_table.t_online_data");
		}catch (Exception e){
			LogUtil.errorLog.error("Exception:"+e.getMessage(),e);
		}

		try{
			if (DataSourceManager.getLogSqlMapClient()!=null){
				DataSourceManager.getLogSqlMapClient().update("check_table.user_card_record");
			}else{
				DataSourceManager.getLoginSqlMapClient().update("check_table.user_card_record");
			}
		}catch (Exception e){
			LogUtil.errorLog.error("Exception:"+e.getMessage(),e);
		}

		checkRoomcardConsume();
		if (GoldConstans.isGoldSiteOpen()) {
			checkRoomgoldConsume();
		}
	}

	private void checkRoomgoldConsume() {
		List<Map<String, Object>> list = SystemCommonInfoDao.getInstance().showLoginColumns("roomgold_consume_statistics");
		if (list != null) {
			for (Entry<String, String> entry : sqlLoginMap2.entrySet()) {
				boolean find = false;
				for (Map<String, Object> columns : list) {
					String field = columns.get("Field").toString();
					if (field.equals(entry.getKey())) {
						find = true;
						break;
					}
				}
				if (!find) {
					LogUtil.msgLog.info("update login sql -->" + entry.getValue());
					SystemCommonInfoDao.getInstance().updateLoginSql(entry.getValue());
				}
			}
		}
	}

	private void checkRoomcardConsume() {
		List<Map<String, Object>> list = SystemCommonInfoDao.getInstance().showLoginColumns("roomcard_consume_statistics");
		if (list != null) {
			for (Entry<String, String> entry : sqlLoginMap.entrySet()) {
				boolean find = false;
				for (Map<String, Object> columns : list) {
					String field = columns.get("Field").toString();
					if (field.equals(entry.getKey())) {
						find = true;
						break;
					}
				}
				if (!find) {
					LogUtil.msgLog.info("update login sql -->" + entry.getValue());
					SystemCommonInfoDao.getInstance().updateLoginSql(entry.getValue());
				}
			}

		}
	}
}
