package com.sy599.game.activity;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.TypeReference;
import com.sy599.game.activity.msg.ActivityMsg;
import com.sy599.game.character.Player;
import com.sy599.game.db.bean.activityRecord.UserActivityRecord;
import com.sy599.game.staticdata.StaticDataManager;
import com.sy599.game.staticdata.model.Activity;
import com.sy599.game.staticdata.model.Award;
import com.sy599.game.util.*;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyActivity {
	private Player player;
	private Map<Integer, List<Integer>> activityBonusStateMap;
	private Map<Integer, Long> bonusStateTimeMap;
	private Map<Integer, Map<Long, Integer>> timeFlagMap;
	/**
	 * 玩家精彩活动记录信息
	 */
	private UserActivityRecord activityRecord;

	public MyActivity(Player player) {
		this.player = player;
		this.activityBonusStateMap = new HashMap<>();
		this.bonusStateTimeMap = new HashMap<>();
		this.timeFlagMap = new HashMap<>();
		this.activityRecord = new UserActivityRecord();
	}

	public void loadFromDB(String data) {
		if (!StringUtils.isBlank(data)) {
			JsonWrapper json = new JsonWrapper(data);
			String bonus = json.getString(1);
			if (!StringUtils.isBlank(bonus)) {
				Map<Integer, List<Integer>> temp = JacksonUtil.readValue(bonus, new TypeReference<Map<Integer, List<Integer>>>() {
				});
				if (temp!=null){
					activityBonusStateMap = temp;
				}
			}
			String award = json.getString(2);
			if (!StringUtils.isBlank(award)) {
				Map<Integer, Long> temp = JacksonUtil.readValue(award, new TypeReference<Map<Integer, Long>>() {
				});
				if (temp!=null){
					bonusStateTimeMap = temp;
				}
			}
			String timeStr = json.getString(3);
			if (!StringUtils.isBlank(timeStr)) {
				Map<Integer, Map<Long, Integer>> temp = JacksonUtil.readValue(timeStr, new TypeReference<Map<Integer, Map<Long, Integer>>>() {
				});
				if (temp!=null){
					timeFlagMap = temp;
				}
			}
			String consumeDaimRecordStr = json.getString(4);
			if (!StringUtils.isBlank(consumeDaimRecordStr)) {
				UserActivityRecord temp = JacksonUtil.readValue(consumeDaimRecordStr, new TypeReference<UserActivityRecord>() {
				});
				if(temp == null) {
					Map<String, String> tempMap  = JacksonUtil.readValue(consumeDaimRecordStr, new TypeReference<HashMap<String, String>>(){});
					if(tempMap.containsKey("oldBackGifRecord")) {
						String ja = (String)tempMap.get("oldBackGifRecord");
						if(ja.equals("[]")) {
							tempMap.put("oldBackGifRecord", "{}");
							Map<String, String> resetMap = new HashMap<>();
							for(String key : tempMap.keySet()) {
								resetMap.put("\"" + key + "\"", tempMap.get(key));
							}
							consumeDaimRecordStr = JacksonUtil.writeValueAsString(resetMap);
							temp = JacksonUtil.readValue(consumeDaimRecordStr, new TypeReference<UserActivityRecord>() {
							});
                            player.changeActivity();
                        }
					}
				}
				if (temp != null){
					activityRecord = temp;
				}
			}
		} else {
			this.activityBonusStateMap = new HashMap<>();
			this.bonusStateTimeMap = new HashMap<>();
			this.timeFlagMap = new HashMap<>();
			this.activityRecord = new UserActivityRecord();
		}
	}

	public UserActivityRecord getUserActivityRecord() {
		long resetTime = activityRecord.getResetTime();
		long curTime = TimeUtil.currentTimeMillis();
		if (!activityRecord.getReceiveGrades().isEmpty() && !TimeUtil.isSameDay(resetTime, curTime)) {// 每日0点重置玩家活动数据
			activityRecord.resetConsumeDaimRecordData();
			activityRecord.resetSmashEggTasks();
			activityRecord.setResetTime(curTime);
			updateActivityRecord(activityRecord);
		}
		return activityRecord;
	}

    /**
     * 更新玩家活动记录信息
     * @param record
     * @param saveDB 是否立即入库  true是 false否
     */
	public void updateActivityRecord(UserActivityRecord record, boolean saveDB) {
		this.activityRecord = record;
		player.changeActivity();
		if(saveDB == true) {
			player.saveBaseInfo();
		}
	}

	/**
	 * 更新玩家活动记录信息 默认不立即入口
	 * @param record
	 */
	public void updateActivityRecord(UserActivityRecord record) {
		updateActivityRecord(record, false);
	}

	/**
	 * 购买完后将奖励状态置成已领取（2）
	 *
	 * @param obj
	 * @param index
	 *            void
	 * @throws
	 */
	public void updateAwardState(Object obj, int index, long currentTime) {
		Activity activity = null;
		if (obj instanceof Integer) {
			activity = StaticDataManager.activityMap.get((Integer) obj);
		} else if (obj instanceof Activity) {
			activity = (Activity) obj;
		}
		List<Integer> list = this.activityBonusStateMap.get(activity.getId());
		if (list == null) {
			return;
		}

		list.set(index, 2);
		player.changeActivity();
	}

	public Award getAward(Object obj, int index) {
		Activity activity = null;
		if (obj instanceof Integer) {
			activity = StaticDataManager.activityMap.get((Integer) obj);
		} else if (obj instanceof Activity) {
			activity = (Activity) obj;
		}
		return activity.getAwardList().get(index);

	}

	public List<Integer> getActivityBonusState(Activity activity) {
		// 普通活动
		List<Integer> list = activityBonusStateMap.get(activity.getId());
		if (list == null) {
			list = initActivityBonusState(activity);
		}
		return list;
	}

	private List<Integer> initActivityBonusState(Activity activity) {
		List<Integer> list;
		list = new ArrayList<Integer>();

		if (activity.getAwardList() == null || activity.getAwardList().isEmpty()) {
			return list;
		}

		for (int i = 0; i < activity.getAwardList().size(); i++) {
			list.add(0);
		}

		activityBonusStateMap.put(activity.getId(), list);
		bonusStateTimeMap.put(activity.getId(), TimeUtil.currentTimeMillis());
		player.changeActivity();
		return list;
	}

	public boolean isCanGetAward(Object obj, int index, int val) {
		Activity activity = null;
		if (obj instanceof Integer) {
			activity = StaticDataManager.activityMap.get((Integer) obj);
		} else if (obj instanceof Activity) {
			activity = (Activity) obj;
		}

		if (activity == null) {
			return false;
		}

		List<Integer> list = getActivityBonusState(activity);
		if (list == null || index < 0 || index >= list.size()) {
			return false;
		}

		if (list.get(index) == 2) {
			return false;
		}

		String conditions = activity.getConditions();
		List<Integer> conditionList = StringUtil.explodeToIntList(conditions);
		if (index >= conditionList.size()) {
			return false;
		}
		if (val >= conditionList.get(index) && conditionList.get(index) != 0) {
			return true;
		}
		return false;

	}

	public boolean isCanGetAward(Object obj, int val) {
		Activity activity = null;
		if (obj instanceof Integer) {
			activity = StaticDataManager.activityMap.get((Integer) obj);
		} else if (obj instanceof Activity) {
			activity = (Activity) obj;
		}

		if (activity == null) {
			return false;
		}

		List<Integer> list = getActivityBonusState(activity);
		if (list == null) {
			return false;
		}

		String conditions = activity.getConditions();
		List<Integer> conditionList = StringUtil.explodeToIntList(conditions);
		if (conditionList == null) {
			return false;
		}

		for (int i = 0; i < list.size(); i++) {
			int state = list.get(i);
			if (state != 2) {
				int condition = conditionList.get(i);
				if (val >= condition && condition != 0) {
					return true;
				}

			}
		}

		return false;

	}

	public List<ActivityMsg> buildMessage(Activity activity, int curVal) {
		List<ActivityMsg> list = new ArrayList<>();
		List<Integer> stateList = getActivityBonusState(activity);
		List<Integer> cList = StringUtil.explodeToIntList(activity.getConditions());
		List<Award> awardList = activity.getAwardList();
		for (int i = 0; i < awardList.size(); i++) {
			ActivityMsg msg = new ActivityMsg();
			msg.setTarVal(cList.get(i));
			msg.setAward(AwardUtil.buildAwardMap(awardList.get(i)));
			int sate = stateList.get(i);
			if (sate == 0) {
				if (curVal >= msg.getTarVal()) {
					msg.setCanGet(1);
				}
			} else {
				msg.setCanGet(sate);
			}

			list.add(msg);
		}
		return list;
	}

	public void changeTimeFlag(int activityId, long time, int flag) {
		if (timeFlagMap.containsKey(activityId)) {
			Map<Long, Integer> map = timeFlagMap.get(activityId);
			int nowFlag = 0;
			if (map.containsKey(time)) {
				nowFlag = map.get(time);
			} else {
				map.clear();
			}
			map.put(time, nowFlag + flag);
		} else {
			Map<Long, Integer> map = new HashMap<>();
			map.put(time, flag);
			timeFlagMap.put(activityId, map);
		}
		player.changeActivity();

	}

	public void clearTimeFlag(int activityId, long time) {
		if (timeFlagMap.containsKey(activityId)) {
			Map<Long, Integer> map = timeFlagMap.get(activityId);
			map.clear();

		}
		player.changeActivity();

	}

	public int getTimeFlag(int activityId, long time) {
		if (timeFlagMap.containsKey(activityId)) {
			Map<Long, Integer> map = timeFlagMap.get(activityId);
			if (map.containsKey(time)) {
				return map.get(time);
			}
		}
		return 0;
	}

	public String toJson() {
		JsonWrapper json = new JsonWrapper("");
		json.putString(1, JacksonUtil.writeValueAsString(activityBonusStateMap));
		json.putString(2, JacksonUtil.writeValueAsString(bonusStateTimeMap));
		json.putString(3, JacksonUtil.writeValueAsString(timeFlagMap));
		json.putString(4, JacksonUtil.writeValueAsString(activityRecord));
		return json.toString();
	}
}
