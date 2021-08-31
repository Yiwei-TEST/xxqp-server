package com.sy599.game.db.dao;

import com.alibaba.fastjson.JSONArray;
import com.sy599.game.util.LogUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseConfigDao extends BaseDao {

	private final static BaseConfigDao _inst = new BaseConfigDao();

	public static BaseConfigDao getInstance() {
		return _inst;
	}

	public List<HashMap<String,Object>> selectAllByType(String msgType) {
		try {
			Map<String,Object> map = new HashMap<>(4);
			map.put("msgType",msgType);
			return (List<HashMap<String,Object>>) this.getSqlLoginClient().queryForList("baseConfig.selectAllByType",map);
		} catch (SQLException e) {
			LogUtil.e("Exception :"+e.getMessage(), e);
		}
		return null;
	}

	public List<HashMap<String,Object>> selectAllByTypes(String... msgTypes) {
		if (msgTypes==null||msgTypes.length==0){
			return null;
		}
		try {
			StringBuilder stringBuilder = new StringBuilder();
			for (String msgType : msgTypes){
				stringBuilder.append(",'").append(msgType).append("'");
			}

			Map<String,Object> map = new HashMap<>(4);
			map.put("msgTypes",stringBuilder.substring(1));
			return (List<HashMap<String,Object>>) this.getSqlLoginClient().queryForList("baseConfig.selectAllByTypes",map);
		} catch (SQLException e) {
			LogUtil.e("Exception :"+e.getMessage(), e);
		}
		return null;
	}

	public List<HashMap<String,Object>> selectAll() {
		try {
			return (List<HashMap<String,Object>>) this.getSqlLoginClient().queryForList("baseConfig.selectAll");
		} catch (SQLException e) {
			LogUtil.e("Exception :"+e.getMessage(), e);
		}
		return null;
	}

	public List<String[]> loadValueList(List<HashMap<String,Object>> mapList){
		if (mapList==null||mapList.size()==0){
			return null;
		}else{
			List<String[]> resultList = new ArrayList<>(mapList.size());
			for (HashMap<String,Object> map : mapList){
				Object value = map.get("msgValue");
				if (value!=null){
					JSONArray jsonArray = JSONArray.parseArray(value.toString());
					String[] strs = new String[jsonArray.size()];
					for (int i=0,len=jsonArray.size();i<len;i++){
						strs[i] = jsonArray.getString(i);
					}
					resultList.add(strs);
				}
			}
			return resultList.size()==0?null:resultList;
		}
	}

}
