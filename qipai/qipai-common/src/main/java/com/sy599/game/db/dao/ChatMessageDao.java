package com.sy599.game.db.dao;

import com.sy599.game.util.LogUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatMessageDao extends BaseDao {

	private static ChatMessageDao _inst = new ChatMessageDao();

	public static ChatMessageDao getInstance() {
		return _inst;
	}

	public HashMap<String,Object> select(Object keyId){
		try {
			return (HashMap<String,Object>)getSqlLoginClient().queryForObject("chat_message.select_one",String.valueOf(keyId));
		}catch (Exception e){
			LogUtil.errorLog.error("Exception:"+e.getMessage(),e);
		}
		return null;
	}
	public List<HashMap<String,Object>> select(String groupId,int pageNo,int pageSize){
		try {
			Map<String,Object> map=new HashMap<>();
			map.put("groupId",groupId);
			map.put("startNo",(pageNo-1)*pageSize);
			map.put("pageSize",pageSize);
			return (List<HashMap<String,Object>>)getSqlLoginClient().queryForList("chat_message.select_all",map);
		}catch (Exception e){
			LogUtil.errorLog.error("Exception:"+e.getMessage(),e);
		}
		return null;
	}
	public long insert(HashMap<String,Object> map){
		try {
			return (Long)getSqlLoginClient().insert("chat_message.insert",map);
		}catch (Exception e){
			LogUtil.errorLog.error("Exception:"+e.getMessage(),e);
		}
		return -1;
	}
}
