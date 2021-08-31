package com.sy.sanguo.game.pdkuai.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

import org.apache.commons.lang.StringUtils;

import com.sy.sanguo.common.util.JsonWrapper;
import com.sy.sanguo.common.util.NetTool;
import com.sy.sanguo.common.util.TaskExecutor;
import com.sy.sanguo.game.pdkuai.constants.SharedConstants;
import com.sy.sanguo.game.pdkuai.constants.SystemCommonInfoType;
import com.sy.sanguo.game.pdkuai.db.bean.GotyeRoomInfo;
import com.sy.sanguo.game.pdkuai.db.bean.SystemCommonInfo;
import com.sy.sanguo.game.pdkuai.db.dao.GotyeRoomDao;
import com.sy.sanguo.game.pdkuai.util.LogUtil;
import com.sy599.sanguo.util.TimeUtil;


/**
 * 语音管理类
 * @author Administrator
 *
 */
public class GotyeChatManager {
	private static String client_id = "5e6869b8-7781-4440-bdeb-5d32b44dc734";
	private static String clientSecret = "93a23539337b4b89b5e515ccf3cb8758";
	private String api_url = "";
	private String access_token = "";
	private long expires_in = 0;

	private static GotyeChatManager _inst = new GotyeChatManager();

	public static GotyeChatManager getInstance() {
		return _inst;
	}
		
	/**
	 * 检查token过期
	 */
	public void checkAccessToken() {
		if (StringUtils.isBlank(access_token) || this.expires_in - 100 * SharedConstants.SENCOND_IN_MINILLS < TimeUtil.currentTimeMillis()) {
			getAccessToken();
		}
	}
	
	private JsonWrapper post(String url, Map<String, String> map, boolean sendToken) throws Exception {
		NetTool tool = new NetTool();
		if (sendToken) {
			checkAccessToken();
			tool.addReqProperty("Authorization", "Bearer " + access_token);
		}
		String post = tool.sendPost(url, map, "utf-8");
		JsonWrapper wrapper = new JsonWrapper(post);
		return wrapper;

	}
	
	public void loadFromDB() {
		SystemCommonInfo info = SystemCommonInfoManager.getInstance().getSystemCommonInfo(SystemCommonInfoType.gotyeToken);
		if (info != null) {
			if (!StringUtils.isBlank(info.getContent())) {
				JsonWrapper wrapper = new JsonWrapper(info.getContent());
				refreshToken(wrapper);
			} else {
			}
		}

		checkAccessToken();
		
		/*
		long time1 = TimeUtil.currentTimeMillis();
		int totalCount = GotyeRoomDao.getInstance().selectCount();
		int start = 0;
		long time2 = TimeUtil.currentTimeMillis();
		LogUtil.monitorLog.info("time2:" + (time2 - time1));
		int noloadcount = 0;
		int offcount = 0;
		while (start < totalCount) {
			long time3 = TimeUtil.currentTimeMillis();
			List<GotyeRoomInfo> list = GotyeRoomDao.getInstance().getAll(start, 1000);
			long time4 = TimeUtil.currentTimeMillis();
			LogUtil.monitorLog.info("time3:" + (time4 - time3));
			for (GotyeRoomInfo userInfo : list) {
				roomMap.put(userInfo.getRoomId(), userInfo);
				if (userInfo.getIsUse() == 1) {
					BaseTable baseTable = TableManager.getInstance().getTable(userInfo.getTableId());
					if (baseTable == null) {
						delRoomList.add(userInfo.getRoomId());
					} else {
						useRoomList.add(userInfo.getRoomId());

					}

				} else if (userInfo.getIsUse() == 2) {
					delRoomList.add(userInfo.getRoomId());

				} else {
					notUseRoomIdList.add(userInfo.getRoomId());
				}
			}
			long time5 = TimeUtil.currentTimeMillis();
			LogUtil.monitorLog.info("time4:" + (time5 - time4) + " offcount" + offcount);
			start += 1000;
		}
		LogUtil.monitorLog.info("noloadcount:" + noloadcount + " of offcount:" + offcount);
		*/
		
		// checkRoom();
	}
	
	public void getAccessToken() {
		String url = "https://api.gotye.com.cn/api/accessToken";
		Map<String, String> map = new HashMap<>();
		map.put("grant_type", "client_credentials");
		map.put("client_id", client_id);
		map.put("client_secret", clientSecret);
		try {
			JsonWrapper wrapper = post(url, map, false);
			long expires_in = wrapper.getLong("expires_in", 0);
			if (expires_in != 0) {
				wrapper.putLong("expires_in", TimeUtil.currentTimeMillis() + expires_in * 1000);
				refreshToken(wrapper);
				SystemCommonInfo info = SystemCommonInfoManager.getInstance().getSystemCommonInfo(SystemCommonInfoType.gotyeToken);
				info.setContent(wrapper.toString());
				SystemCommonInfoManager.getInstance().updateSystemCommonInfo(info);
			}

		} catch (Exception e) {
			LogUtil.e("getAccessToken", e);
		}

	}
	
	public void delIMRoom(long roomId) {
		String url = api_url + "/DelIMRoom";
		Map<String, String> map = new HashMap<>();
		map.put("appkey", client_id);
		map.put("roomId", roomId + "");
		try {
			JsonWrapper wrapper = post(url, map, true);
			// {"systime":1470820022905,"affectedRows":1,"accessPath":"DelIMRoom","status":200,"runtime":17}
			int status = wrapper.getInt("status", 0);
			if (status == 200 || status == 3003) {
				GotyeRoomDao.getInstance().del(roomId);
				LogUtil.i("delIMRoom-->" + roomId + " state:" + status);
//				delRoomList.remove(roomId);
//				roomMap.remove(roomId);
				// if (del == 1) {
				// useRoomList.remove((Object) roomId);
				// }
			} else {
				LogUtil.e("delIMRoom err-->" + wrapper.toString());

			}
			
			
			GotyeRoomDao.getInstance().del(roomId);

		} catch (Exception e) {
			LogUtil.e("DelIMRoom", e);
		}
	}
	
	// 删除语音房间
	public void deleteGotyeRoom() {
		List<GotyeRoomInfo> lists = GotyeRoomDao.getInstance().canDelGotyeRoom(10);
		
		if (lists.size() <= 0) {
			return;
		}
		
		long roomId = 0;
		for (GotyeRoomInfo temp : lists) {
			roomId = temp.getRoomId();
			delIMRoom(roomId);
			System.out.println("删除语音房间" + roomId);
		}
	}
	
//	public void checkIMRoom() {
//		if (delRoomList.isEmpty()) {
//			return;
//		}
//		List<Long> copy = new ArrayList<>(delRoomList);
//		Iterator<Long> iterable = copy.iterator();
//		int i = 0;
//		while (iterable.hasNext()) {
//			long del = iterable.next();
//			delIMRoom(del);
//			iterable.remove();
//			i++;
//			if (i > 10) {
//				// 20秒钟删除10个房间
//				break;
//			}
//		}
//
//		checkRoom();
//	}
	
	/**
	 * 删除语音房间id
	 * 
	 * @param roomId
	 */
//	public void delRoomId(long roomId) {
//		boolean remove = useRoomList.remove((Object) roomId);
//		if (remove) {
//			GotyeRoomDao.getInstance().updateDel(roomId);
//			delRoomList.add(roomId);
//		}
//
//	}

	
	/**
	 * 获取一个没有使用过的语音房间id
	 * 
	 * @return
	 */
//	public synchronized long getNotUseRoomId(long tableId) {
//		if (GameServerConfig.isDebug()) {
//			return 0;
//		}
//		checkRoom();
//		if (notUseRoomIdList.size() > 0) {
//			long roomId = notUseRoomIdList.remove(0);
//			useRoomList.add(roomId);
//			GotyeRoomDao.getInstance().updateUse(roomId, tableId);
//			return roomId;
//		}
//		return 0;
//
//	}
	
	// 创建语音房间
	public void createGotyeRoom() {
		int notUseCnt = GotyeRoomDao.getInstance().getNotUseCnt();
		if (notUseCnt < 200) {
			for (int i=0; i< 10; i++) {
				createIMRoom(1 + "_test", 1, 8);
			}
		}
	}
	
//	public void checkRoom() {
//		final int needCount = 5;
//		if (isCheckRoomIng) {
//			return;
//		}
//		if (GameServerConfig.isDebug()) {
//			return;
//		}
//		if (notUseRoomIdList.size() < needCount) {
//			long now = TimeUtil.currentTimeMillis();
//			if (now - checkTime < 10 * SharedConstants.SENCOND_IN_MINILLS) {
//				return;
//			}
//			new Thread(new Runnable() {
//				@Override
//				public void run() {
//					try {
//						isCheckRoomIng = true;
//						int totalCount = 0;
//						int addCount = needCount * 2 - notUseRoomIdList.size();
//						while (addCount > 0 && totalCount < 10) {
//							totalCount++;
//							createIMRoom(1 + "_test", 1, 4);
//							addCount = needCount * 2 - notUseRoomIdList.size();
//						}
//					} catch (Exception e) {
//						LogUtil.e("checkRoom", e);
//					} finally {
//						isCheckRoomIng = false;
//					}
//
//				}
//			}).start();
//			checkTime = now;
//		}
//
//	}
	
//	public void GetIMRooms(long roomId) {
//		String url = api_url + "/DelIMRoom";
//		Map<String, String> map = new HashMap<>();
//		map.put("appkey", client_id);
//		map.put("roomId", roomId + "");
//		try {
//			JsonWrapper wrapper = post(url, map, true);
//		} catch (Exception e) {
//			e.printStackTrace();
//			LogUtil.e("DelIMRoom", e);
//		}
//	}
	
	/**
	 * @param roomName
	 *            聊天室名称
	 * @param roomType
	 *            1为普通聊天室，支持文字、图片和语音 2为抢麦聊天室
	 * @param maxUserNumber
	 *            最大用户数
	 */
	public void createIMRoom(String roomName, int roomType, int maxUserNumber) {
		String url = api_url + "/CreateIMRoom";
		Map<String, String> map = new HashMap<>();
		map.put("appkey", client_id);
		map.put("roomName", roomName);
		map.put("head", "null"); // 头像, 图片二进制流用base64编码生成的字符串
		map.put("roomType", 1 + "");
		map.put("scope", 0 + ""); // 0表示应用级别 1表示开发者级别
		map.put("maxUserNumber", maxUserNumber + "");
		try {
			JsonWrapper wrapper = post(url, map, true);
			int status = wrapper.getInt("status", 0);
			if (status == 200) {
				JSONObject json = wrapper.getJosn("entity");
				long roomId = json.getLong("roomId");
				GotyeRoomInfo info = new GotyeRoomInfo();
				info.setCreateTime(TimeUtil.now());
				info.setRoomId(roomId);
				info.setRoomName(roomName);

//				notUseRoomIdList.add(roomId);
//				roomMap.put(roomId, info);
				GotyeRoomDao.getInstance().save(info);
				LogUtil.i("createIMRoom-->" + roomId);
			} else {
				LogUtil.e("createIMRoom err-->" + wrapper.toString());

			}
			
//			long roomId = RandomUtils.nextInt(999999) + 1000000;
//			while (gotyeChatList.contains(roomId)) {
//				roomId = RandomUtils.nextInt(999999) + 1000000;
//			}
//			GotyeRoomInfo info = new GotyeRoomInfo();
//			info.setCreateTime(TimeUtil.now());
//			info.setRoomId(roomId);
//			info.setRoomName(roomName);
//
//			notUseRoomIdList.add(roomId);
//			roomMap.put(roomId, info);
//			GotyeRoomDao.getInstance().save(info);
//			LogUtil.i("createIMRoom-->" + roomId);
		} catch (Exception e) {
			LogUtil.e("createIMRoom", e);
		}
	}
	
	private void refreshToken(JsonWrapper wrapper) {
		long expires_in = wrapper.getLong("expires_in", 0);
		if (expires_in != 0) {
			LogUtil.i("GotyeChatManager refreshToken-->" + wrapper.toString());
			this.expires_in = expires_in;
			api_url = wrapper.getString("api_url");
			access_token = wrapper.getString("access_token");
		}

	}
	
	public void init() {
		loadFromDB();
		
		TaskExecutor.getInstance().submitSchTask(new Runnable() {// 创建语音房间
			@Override
			public void run() {
				createGotyeRoom();
			}
		}, 0, 20 * SharedConstants.SENCOND_IN_MINILLS);// 20秒执行一次
		
		TaskExecutor.getInstance().submitSchTask(new Runnable() {// 删除语音房间
			@Override
			public void run() {
				deleteGotyeRoom();
			}
		}, 0, 20 * SharedConstants.SENCOND_IN_MINILLS);
	}
	
	public static void main(String[] args) {

	}
}
