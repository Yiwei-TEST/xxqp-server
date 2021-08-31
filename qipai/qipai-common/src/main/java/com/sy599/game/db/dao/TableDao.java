package com.sy599.game.db.dao;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.sy599.game.db.bean.DaikaiTable;
import com.sy599.game.db.bean.RoomBean;
import com.sy599.game.db.bean.TableInf;
import com.sy599.game.util.JacksonUtil;
import com.sy599.game.util.LogUtil;

import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.*;

public class TableDao extends BaseDao {
	private static TableDao _inst = new TableDao();

	public static TableDao getInstance() {
		return _inst;
	}

	public void batchUpdate(final List<Map<String, Object>> list){
		try {
			if (list != null && list.size()>0) {
				SqlMapClient sqlMapClient = getSqlClient();
				sqlMapClient.startBatch();
				for ( int i = 0, n = list.size(); i < n; i++) {
					Map<String, Object> paramMap = list.get(i);
					sqlMapClient.update("table.updatetableInfo", paramMap);
				}
				sqlMapClient.executeBatch();

				LogUtil.monitorLog.info("batchUpdate updatetableInfo success:count={}",list.size());
			}
		} catch (Exception e) {
			LogUtil.dbLog.error("batchUpdate updatetableInfo Exception:"+e.getMessage(),e);

			for ( int i = 0, n = list.size(); i < n; i++) {
				Map<String, Object> paramMap = list.get(i);
				try {
					getSqlClient().update("table.updatetableInfo", paramMap);
				} catch (Exception e0) {
					LogUtil.dbLog.error("singleUpdate updatetableInfo Exception:" + e0.getMessage(), e0);
				}
			}
		}
	}

	public void save(Map<String, Object> paramMap) {
		try {
//			LogUtil.dbLog.info(String.format("updTable,id:%d", paramMap.get("tableId"))+" msg:"+paramMap.toString());
			getSqlClient().update("table.updatetableInfo", paramMap);
		} catch (Exception e) {
			LogUtil.dbLog.error("#table.updatetableInfo:" + JacksonUtil.writeValueAsString(paramMap), e);
		}
	}

	public RoomBean queryRoom(long roomId) throws SQLException {
		try {
			RoomBean room = (RoomBean) this.getSqlLoginClient().queryForObject("table.queryRoom", roomId);
			return room;
		} catch (SQLException e) {
			LogUtil.dbLog.error("#table.queryRoom:" + roomId, e);
		}
		return null;
	}

	public RoomBean queryUsingRoom(long roomId) throws SQLException {
		try {
			RoomBean room = (RoomBean) this.getSqlLoginClient().queryForObject("table.queryUsingRoom", roomId);
			return room;
		} catch (SQLException e) {
			LogUtil.dbLog.error("#table.queryUsingRoom:" + roomId, e);
		}
		return null;
	}

	public void save(TableInf tableInfo) throws SQLException {
		try {
			if (tableInfo.getTableId()>0){
				getSqlClient().insert("table.savetableInfo", tableInfo);
			}else{
				LogUtil.errorLog.error("tableId error:"+tableInfo.getTableId());
			}
		} catch (SQLException e) {
			LogUtil.dbLog.error("#table.savetableInfo:" + tableInfo.getTableId(), e);
			throw e;
		}
	}


	public int delete(long id) {
        LogUtil.dbLog.info("BaseTable|delete|" + id);
		try {
			return getSqlClient().delete("table.deleteTable", id);
		} catch (SQLException e) {
			LogUtil.dbLog.error(String.format("deleteTable,TableId:%d", id), e);
		}
		return 0;
	}

	public int selectCount() {
		int playerCount = 0;
		try {
			Object result = getSqlClient().queryForObject("table.selectCount");

			if (result != null) {
				playerCount = (Integer) result;
			}

		} catch (SQLException e) {
			LogUtil.dbLog.error("#table.selectCount:", e);
		}

		return playerCount;
	}

	@SuppressWarnings("unchecked")
	public List<TableInf> selectAll(int start, int end) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("start", start);
		map.put("end", end);
		List<TableInf> result = new ArrayList<TableInf>();
		try {
			result = (List<TableInf>) getSqlClient().queryForList("table.selectActiveTable", map);
		} catch (Exception e) {
			LogUtil.dbLog.error("#table.selectAll:", e);
		}
		return result;
	}

	/**
	 * 已经创建了的房间
	 * 
	 * @param roomId
	 *            roomId
	 * @param params
	 * @return
	 */
	public int updateRoom(long roomId, Map<String, Object> params) {
		params.put("roomId", roomId);
		try {
			return getSqlLoginClient().update("table.updateRoom", params);
		} catch (SQLException e) {
			LogUtil.dbLog.error("updateRoom", e);
		}
		return 0;
	}

	/**
	 * 清理使用过的房间
	 * 
	 * @param roomId
	 * @return
	 */
	public int clearRoom(long roomId) {
		Map<String, Object> params = new HashMap<>();
		params.put("used", 0);
		params.put("serverId", 0);
		return updateRoom(roomId, params);
	}

	/**
	 * 获取房间号
	 * @param paramMap 包含userId、serverId、type（玩法）、roomId
	 * @return 1成功
	 * @throws Exception
	 */
	public int updateRandomRoom(HashMap<String, Object> paramMap) throws Exception{
		return getSqlLoginClient().update("table.updateRandomRoom", paramMap);
	}

	/**
	 *
	 * @param userId
	 * @param serverId
	 * @param type
	 * @param tableType 0普通开房，1军团开房，2代开房
	 * @return
	 */
	public long loadRoomId(long userId, int serverId, int type,int tableType){
		long result;
		long t1 = System.currentTimeMillis();
		int count = 0;
		try {
			HashMap<String, Object> paramMap = new HashMap<>();
			paramMap.put("userId", userId);
			paramMap.put("serverId", serverId);
			paramMap.put("type", type);

			int ret=0;
			int min = 100001;
			int range = 899999;

			Random random = new SecureRandom();
//			switch (tableType){
//				case 1:
//					if (random.nextInt(4)==0){
//						min=500000;
//						range=100000;
//					}else{
//						min=700000;
//						range=300000;
//					}
//					break;
//				case 2:
//					min=600000;
//					range=100000;
//					break;
//				default:
//					min=100001;
//					range=499999;
//					break;
//			}

			while (ret!=1 && count < 20){
				count++;

				result = min + random.nextInt(range);

				paramMap.put("roomId", result);

				ret=updateRandomRoom(paramMap);
				if (ret==1){
					return result;
				}else{
					LogUtil.monitor_i("loadRoomId fail roomId:"+result);
				}
			}
		} catch (Exception e) {
			LogUtil.e("updateRandomRoom err"+e.getMessage(), e);
		}finally {
			LogUtil.monitor_i("loadRoomId time(ms):"+(System.currentTimeMillis()-t1)+", times:"+count);
		}

		LogUtil.e("loadRoomId fail:times="+count+",roomId=0");
		
		return 0;
	}

	/**
	 * 
	 * @param in_createId 创建者Id
	 *            ,
	 * @param in_serverId 创建者服务器id
	 *            ,
	 * @param in_type 游戏类型
	 * @param tableType 房间类型 0普通开房，1军团开房，2代开房
	 * @return 房号
	 */
	public long callProCreateRoom(long in_createId, int in_serverId, int in_type,int tableType) {
		return loadRoomId(in_createId,in_serverId,in_type,tableType);
	}

	/**
	 * 创建代开房间
	 * 
	 * @param params
	 * @throws SQLException
	 */
	public void daikaiTable(Map<String, Object> params) throws SQLException {
		try {
			getSqlLoginClient().insert("table.daikaiTable", params);
		} catch (SQLException e) {
			LogUtil.dbLog.error("#table.daikaiTable:" + params, e);
			throw e;
		}
	}

	/**
	 * 根据tableId取代开房间数据
	 * 
	 */
	public DaikaiTable getDaikaiTableById(long tableId) {
		try {
			DaikaiTable daikaiTable = (DaikaiTable) this.getSqlLoginClient().queryForObject("table.getDaikaiTableById", tableId);
			return daikaiTable;
		} catch (SQLException e) {
			LogUtil.dbLog.error("#table.getDaikaiTableById:" + tableId, e);
		}
		return null;
	}

	/**
	 * 代开房间修改状态
	 * 
	 */
	public int dissDaikaiTable(long tableId, boolean needReturn) {
		HashMap<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("tableId", tableId);
		paramMap.put("returnFlag", needReturn ? 1 : 0);
		paramMap.put("state", needReturn ? 3 : 2);

		try {
			return getSqlLoginClient().update("table.dissDaikaiTable", paramMap);
		} catch (SQLException e) {
			LogUtil.dbLog.error("dissDaikaiTable tableId = " + tableId + " err:", e);
		}
		return 0;
	}

	/**
	 * 修改代开房间的表
	 * 
	 * @param paramMap
	 */
	public int updateDaikaiTable(HashMap<String, Object> paramMap) {
		try {
			return getSqlLoginClient().update("table.updateDaikaiTable", paramMap);
		} catch (SQLException e) {
			LogUtil.dbLog.error("updateDaikaiTable err:", e);
		}
		return 0;
	}

	/**
	 * 玩家当前代开房间的数量
	 * 
	 * @param userId
	 * @return
	 */
	public int getDaikaiTableCount(long userId) {
		try {
			return (Integer) this.getSqlLoginClient().queryForObject("table.getDaikaiTableCount", userId);
		} catch (SQLException e) {
			LogUtil.dbLog.error("getDaikaiTableCount err:", e);
		}

		return 0;
	}
	
	/**
	 * 玩家当前APP玩法代开房间的数量
	 * 
	 * @param userId
	 * @param playTypes APP对应玩法ID列表
	 * @return
	 */
	public int getWanfaDaikaiTableCount(long userId, List<Integer> playTypes) {
		int count = 0;
		try {
			@SuppressWarnings("unchecked")
			List<DaikaiTable> tables = (List<DaikaiTable>) this.getSqlLoginClient().queryForList("table.getWanfaDaikaiTables", userId);
			for(DaikaiTable table : tables) {
				if(playTypes.contains(table.getPlayType())) 
					count++;
			}
		} catch (SQLException e) {
			LogUtil.dbLog.error("getWanfaDaikaiTableCount err:", e);
		}
		return count;
	}

	/**
	 * 检测生成的代开房间号是存已经存在
	 * 
	 * @param tableId
	 * @return
	 */
	public boolean checkTableIdExist(long tableId) {
		int count = 0;
		try {
			count = (Integer)this.getSqlLoginClient().queryForObject("table.checkTableIdExist", tableId);
		} catch (SQLException e) {
			LogUtil.dbLog.error("checkTableIdExist err:", e);
		}

		if (count > 0) {
			return true;
		}
		return false;
	}

	/**
	 * 添加代理解散房间的信息
	 */
	public void addDissInfo(Map<String, Object> paramMap) {
		try {
			this.getSqlLoginClient().insert("table.addDissInfo", paramMap);
		} catch (SQLException e) {
			LogUtil.e("addDissInfo err", e);
		}
	}
}
