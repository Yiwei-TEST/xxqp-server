package com.sy599.game.extend;

import com.alibaba.fastjson.TypeReference;
import com.sy599.game.character.Player;
import com.sy599.game.db.bean.UserFirstmyth;
import com.sy599.game.db.bean.serverSign.UserSevenSignInfo;
import com.sy599.game.db.bean.task.UserTaskInfo;
import com.sy599.game.util.*;
import org.apache.commons.lang.StringUtils;

import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MyExtend {
	private Player player;
	private long noticeTime;
	private Map<Integer, Integer> pointMap = new HashMap<Integer, Integer>();
	/*** 赢次数 */
	private Map<Integer, Integer> victoryCountMap = new HashMap<Integer, Integer>();
	/*** 输次数 */
	private Map<Integer, Integer> defeatCountMap = new HashMap<Integer, Integer>();
	/*** 最近胜负次数 */
	private List<Integer> latestCountList = new ArrayList<>();
	/** 1-7跑得快[0赢分最高,1关别人最多,2输分最多,3炸弹最多,4组局最多,5飞机最多,6被全关最多] **/
	private Map<Integer, int[]> fengshenMap = new TreeMap<Integer, int[]>();
	private int versions;
	/**
	 * 经纬度
	 */
	private String latitudeLongitude;

	/**
	 * 记录玩家状态（0正常参加游戏，1观战，2中途加入游戏，3下注）
	 */
	private Map<String,String> playerStateMap = new ConcurrentHashMap<>();

	/**
	 * 记录玩家状态（0正常参加游戏，1观战，2中途加入游戏，3下注）
	 */
	public Map<String, String> getPlayerStateMap() {
		return playerStateMap;
	}

	/**
	 * 记录玩家最后一次游戏时间
	 */
	public String lastPlayTime;

	/**
	 * 玩家任务信息(芒果)
	 */
	private UserTaskInfo userTaskInfo;

	/**
	 * 玩家七日签到信息(芒果)
	 */
	private UserSevenSignInfo userSevenSignInfo;

	private volatile boolean isGroupMatch = false;

	public String getLastPlayTime() {
		return lastPlayTime;
	}

	public void setLastPlayTime(String lastPlayTime) {
		this.lastPlayTime = lastPlayTime;
		player.changeExtend();
	}

	public MyExtend(Player player) {
		this.player = player;
	}

	public void initData(String data) {
		player.setTotalCount(0);
		if (!StringUtils.isBlank(data)) {
			int totalCount=0;
			JsonWrapper wrapper = new JsonWrapper(data);

			isGroupMatch = wrapper.isHas("match");

			String point = wrapper.getString(1);
			if (!StringUtils.isBlank(point)) {
				pointMap = DataMapUtil.implode(point);

			}
			String fengshen = wrapper.getString(2);
			if (!StringUtils.isBlank(fengshen)) {
				fengshenMap = DataMapUtil.toArrMap(fengshen);

			}
			noticeTime = wrapper.getLong(3, 0);
			versions = wrapper.getInt(4, 0);
			String victoryStr = wrapper.getString(5);
			if (!StringUtils.isBlank(victoryStr)) {
				victoryCountMap = DataMapUtil.implode(victoryStr);
				for (Map.Entry<Integer,Integer> kv:victoryCountMap.entrySet()){
					if(kv.getValue()!=null){
						totalCount+=kv.getValue().intValue();
					}
				}
			}
			String defeatStr = wrapper.getString(6);
			if (!StringUtils.isBlank(defeatStr)) {
				defeatCountMap = DataMapUtil.implode(defeatStr);
				for (Map.Entry<Integer,Integer> kv:defeatCountMap.entrySet()){
					if(kv.getValue()!=null){
						totalCount+=kv.getValue().intValue();
					}
				}
			}

			player.setTotalCount(totalCount);

			latitudeLongitude=wrapper.getString(7);

			String playerStateMapStr=wrapper.getString(8);
			if (!StringUtils.isBlank(playerStateMapStr)) {
				String[] temps=playerStateMapStr.split("\\;");
				for (String temp:temps){
					int idx=temp.indexOf(":");
					if (idx>0){
						playerStateMap.put(temp.substring(0,idx),temp.substring(idx+1));
					}
				}

			}

			String latestCountListStr = wrapper.getString(9);
			if (!StringUtil.isBlank(latestCountListStr)) {
				latestCountList = StringUtil.explodeToIntList(latestCountListStr);
			}

			String lastPlayTime = wrapper.getString(10);
			if (!StringUtil.isBlank(lastPlayTime)) {
				Date date = null;
				try {
					date = TimeUtil.getDateByString(lastPlayTime, "yyyy-MM-dd HH:mm:ss");
				} catch (ParseException e) {
					LogUtil.e("init lastPlayTime err", e);
				}
				player.setLastPlayTime(date);
			}

			String userTaskInfoStr = wrapper.getString(11);
			if (!StringUtils.isBlank(userTaskInfoStr)) {
				UserTaskInfo temp = JacksonUtil.readValue(userTaskInfoStr, new TypeReference<UserTaskInfo>() {
				});
				if (temp!=null){
					userTaskInfo = temp;
				}
			}

			String userSevenSignInfoStr = wrapper.getString(12);
			if (!StringUtils.isBlank(userSevenSignInfoStr)) {
				UserSevenSignInfo temp = JacksonUtil.readValue(userSevenSignInfoStr, new TypeReference<UserSevenSignInfo>() {
				});
				if (temp!=null){
					userSevenSignInfo = temp;
				}
			}
		}
	}

	public List<Integer> getLatestCountList() {
		return latestCountList;
	}

	public void setLatestCountList(List<Integer> latestCountList) {
		this.latestCountList = latestCountList;
	}

	public String getLatitudeLongitude() {
		return latitudeLongitude;
	}

	public void setLatitudeLongitude(String latitudeLongitude) {
		this.latitudeLongitude = latitudeLongitude;
		player.changeExtend();
	}

	public int getVersions() {
		return versions;
	}

	public void setVersions(int versions) {
		if (this.versions == versions) {
			return;
		}
		this.versions = versions;
		player.changeExtend();
	}

	public Map<Integer, Integer> getPointMap() {
		return pointMap;
	}

	public void changePoint(int type, int point) {
		changePoint(type, point, null);
	}

	public void changePoint(int type, int point, Boolean victory) {
		int nowPoint = 0;
		if (pointMap.containsKey(type)) {
			nowPoint = pointMap.get(type);
		}
		pointMap.put(type, nowPoint + point);

		// 计算输赢次数
		if (victory == null) {
			if (point > 0) {
				victory = true;
			} else {
				victory = false;
			}
		}

		changeWinLossCount(type, victory);

		String countStr = ResourcesConfigsUtil.loadServerPropertyValue("lastestCount");
		if (!StringUtil.isBlank(countStr) && !"0".equals(countStr)) {
			while(latestCountList.size()>=Integer.parseInt(countStr)) {
				latestCountList.remove(0);
			}
			latestCountList.add(victory?1:0);
		}

		player.changeTotalCount();

		player.changeExtend();
	}
	/**
	 * 修改输赢次数
	 * @param type
	 * @param victory
	 */
	public void changeWinLossCount(int type, Boolean victory){
		int nowCount = 0;
		if (victory) {
			if (victoryCountMap.containsKey(type)) {
				nowCount = victoryCountMap.get(type);
			}
			victoryCountMap.put(type, nowCount + 1);
		} else {
			if (defeatCountMap.containsKey(type)) {
				nowCount = defeatCountMap.get(type);
			}
			defeatCountMap.put(type, nowCount + 1);
		}
	}

	public boolean isGroupMatch() {
		return isGroupMatch;
	}

	public void setGroupMatch(boolean groupMatch) {
		if (isGroupMatch != groupMatch){
			isGroupMatch = groupMatch;
			player.changeExtend();
		}
	}

	public String toJson() {
		JsonWrapper wrapper = new JsonWrapper("");
		if (isGroupMatch){
			wrapper.putString("match","1");
		}
		wrapper.putString(1, DataMapUtil.explode(pointMap));
		wrapper.putString(2, DataMapUtil.explodeArrMap(fengshenMap));
		wrapper.putLong(3, noticeTime);
		wrapper.putLong(4, versions);
		wrapper.putString(5, DataMapUtil.explode(victoryCountMap));
		wrapper.putString(6, DataMapUtil.explode(defeatCountMap));
		wrapper.putString(7, latitudeLongitude);
		if (playerStateMap!=null&&playerStateMap.size()>0){
			StringBuilder strBuilder=new StringBuilder();
			for (Map.Entry<String,String> kv:playerStateMap.entrySet()){
				strBuilder.append(";").append(kv.getKey()).append(":").append(kv.getValue());
			}
			wrapper.putString(8, strBuilder.substring(1));
		}
		wrapper.putString(9, StringUtil.implode(latestCountList));
		wrapper.putString(10, lastPlayTime);
		if(userTaskInfo != null)
			wrapper.putString(11, JacksonUtil.writeValueAsString(userTaskInfo));
		if(userSevenSignInfo != null)
			wrapper.putString(12, JacksonUtil.writeValueAsString(userSevenSignInfo));
		return wrapper.toString();
	}

	/** 0-6跑得快[0赢分最高,1关别人最多,2输分最多,3炸弹最多,4组局最多,5飞机最多,6被全关最多] **/
	public void setPdkFengshen(int index, int val) {
		setFengshen(index, val);

	}

	public UserFirstmyth buildFrstmyth() {
		return buildFrstmyth(0,0);
	}
	public UserFirstmyth buildFrstmyth(int gameType, int groupId) {
		int day = TimeUtil.formatDayIntTime(TimeUtil.now());
		int[] arr = fengshenMap.get(day);
		if (arr != null) {
			UserFirstmyth bean = new UserFirstmyth();
			bean.setUserId(player.getUserId());
			bean.setUserName(player.getName());
			bean.setGameType(gameType);
			bean.setGroupId(groupId);
			bean.setRecordDate(day);
			Map<String, Object> valueMap = new HashMap<>();
			for (int i = 0; i < arr.length; i++) {
				valueMap.put("record" + (i + 1), arr[i]);
			}
			ClassTool.setValue(bean, valueMap);
			return bean;
		}
		return null;
	}

	public void setFengshen(int index, int val) {
		int day = TimeUtil.formatDayIntTime(TimeUtil.now());
		int[] arr = fengshenMap.get(day);
		if (arr == null) {
			arr = new int[14];
			fengshenMap.put(day, arr);
		}
		arr[index] += val;
		while (fengshenMap.size() > 2) {
			fengshenMap.remove(fengshenMap.keySet().toArray()[0]);
		}
		// if(fengshenMap.size()>2){
		//
		// }
		player.changeExtend();
	}

	/** 7-13麻将[7赢分最多,8自摸最多,9输分最多,10放炮最多,11组局游戏最多,12最多杠,13点杠最多] **/
	public void setMjFengshen(int index, int val) {
		setFengshen(index, val);
	}

	public long getNoticeTime() {
		return noticeTime;
	}

	public void setNoticeTime(long noticeTime) {
		this.noticeTime = noticeTime;
		player.changeExtend();
	}

	public Map<Integer, Integer> getVictoryOrDefeatCountMap() {
		return victoryCountMap;
	}

	public void setVictoryOrDefeatCountMap(Map<Integer, Integer> victoryOrDefeatCountMap) {
		this.victoryCountMap = victoryOrDefeatCountMap;
	}

	public UserTaskInfo getUserTaskInfo() {
		if(userTaskInfo == null) {
			long curGold = player.getGoldPlayer().getAllGold();
			userTaskInfo = new UserTaskInfo(0, curGold, new HashMap<>());
			player.changeExtend();
		}
		return userTaskInfo;
	}

	/**
	 * 更新玩家活动记录信息
	 * @param userTaskInfo
	 */
	public void updateUserTaskInfo(UserTaskInfo userTaskInfo) {
		this.userTaskInfo = userTaskInfo;
		player.changeExtend();
	}

	/**
	 * 更新玩家金币历史最高值
	 */
	public void updateUserMaxGold() {
		long curGold = player.getGoldPlayer().getAllGold();
		if(userTaskInfo == null) {
			userTaskInfo = new UserTaskInfo(0, curGold, new HashMap<>());
			player.changeExtend();
		} else {
			if(curGold > userTaskInfo.getMaxGold()) {
				userTaskInfo.setMaxGold(curGold);
				player.changeExtend();
			}
		}
	}

	public UserSevenSignInfo getUserSevenSignInfo() {
		if(userSevenSignInfo == null) {
			userSevenSignInfo = new UserSevenSignInfo(0, new ArrayList<>());
			player.changeExtend();
		}
		return userSevenSignInfo;
	}

	public void UpdateUserSevenSignInfo(UserSevenSignInfo userSevenSignInfo) {
		this.userSevenSignInfo = userSevenSignInfo;
		player.changeExtend();
	}
}
