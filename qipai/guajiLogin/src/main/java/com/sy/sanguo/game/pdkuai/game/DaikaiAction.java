package com.sy.sanguo.game.pdkuai.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sy.sanguo.game.pdkuai.util.LogUtil;
import org.apache.commons.lang3.StringUtils;
import com.alibaba.fastjson.TypeReference;

import com.sy.sanguo.common.util.JacksonUtil;
import com.sy.sanguo.common.util.JsonWrapper;
import com.sy.sanguo.common.util.StringUtil;
import com.sy.sanguo.game.msg.UserPlayMsg;
import com.sy.sanguo.game.msg.UserPlayTableMsg;
import com.sy.sanguo.game.pdkuai.action.BaseAction;
import com.sy.sanguo.game.pdkuai.constants.SharedConstants;
import com.sy.sanguo.game.pdkuai.db.bean.DaikaiTable;
import com.sy.sanguo.game.pdkuai.db.bean.UserPlaylog;
import com.sy.sanguo.game.pdkuai.db.dao.DaikaiTableDao;
import com.sy599.sanguo.util.TimeUtil;

public class DaikaiAction extends BaseAction {

	@Override
	public String execute(){

		try{
			int functype = this.getInt("funcType");

			switch (functype) {
			case 1:
				getDaikaiList();// 获取代开列表
				break;

			case 2:
				getDaikaiRecord();// 获取代开列表
				break;
			}
		}catch (Throwable t){
			LogUtil.e("Throwable:"+t.getMessage(),t);
		}
		
		return result;
	}
	
	public void getDaikaiList() {
		Map<String, Object> result = new HashMap<>();
		long userId = this.getLong("userId");
		String wanfa = this.getString("wanfa");

		LogUtil.i("getDaikaiList:userId="+userId+",wanfa="+wanfa);

		List<Integer> wanfas = new ArrayList<>();
		if(StringUtils.isNotBlank(wanfa))
			wanfas = StringUtil.explodeToIntList(wanfa);
		List<DaikaiTable> daikaiList = DaikaiTableDao.getInstance().getDaikaiList(userId);
		
		List<UserPlayMsg> playerMsgList = null;
		UserPlayMsg playerMsg = null;
		List<DaikaiTable> filterDaikaiTables = new ArrayList<>();
		for (DaikaiTable table : daikaiList) {
			if(!wanfas.isEmpty() && !wanfas.contains(table.getPlayType())) {
				continue;
			}
			String playerInfo = table.getPlayerInfo();
			if (StringUtils.isBlank(playerInfo)) {
				table.setPlayerMsg(new ArrayList<UserPlayMsg>());
			} else {
				playerMsgList = new ArrayList<>();
				String[] arr = playerInfo.split(";");
				for (String str : arr) {
					String[] arr2 = str.split(",");
					playerMsg = new UserPlayMsg();
					playerMsg.setName(arr2[0]);
					playerMsg.setSex(Integer.parseInt(arr2[1]));
					playerMsgList.add(playerMsg);
				}
				table.setPlayerMsg(playerMsgList);
			}
			filterDaikaiTables.add(table);
		}
		
		result.put("daikaiList", filterDaikaiTables);
		this.writeMsg(0, result);
	}
	
	
	public void getDaikaiRecord(){
		Map<String, Object> result = new HashMap<>();
		
		long userId = this.getLong("userId");
		int logType = this.getInt("logType");
		long logId = this.getLong("logId");
		String wanfa = this.getString("wanfa");
		int wanfaType = this.getInt("wanfaType");

		LogUtil.i("getDaikaiRecord:userId="+userId+"logType="+logType+",logId="+logId+",wanfa="+wanfa);

		List<Integer> wanfas = new ArrayList<>();
		if(StringUtils.isNotBlank(wanfa))
			wanfas = StringUtil.explodeToIntList(wanfa);		
		Map<String, Object> playLogMap = new HashMap<String, Object>();
		List<DaikaiTable> recordTableIds = DaikaiTableDao.getInstance().getRecordTableId(userId);
		
		if (recordTableIds.size() <= 0) {
			playLogMap.put("playLog", Collections.EMPTY_LIST);
			result.put("playLogMap", playLogMap);
			result.put("code", 0);
			this.writeMsg(0, result);
			return;
		}
		
		Map<Long, DaikaiTable> tempTableIds = new HashMap<>();
		for (DaikaiTable daikaiTable : recordTableIds) {
			if(!wanfas.isEmpty() && !wanfas.contains(daikaiTable.getPlayType())) {
				continue;
			}
			tempTableIds.put(daikaiTable.getTableId(), daikaiTable);
		}

		List<List<Long>> lists = new ArrayList<>();
		if(!tempTableIds.isEmpty()) {
			String tableIds = StringUtil.implode(new ArrayList<>(tempTableIds.keySet()));
			List<UserPlaylog> recordIds = DaikaiTableDao.getInstance().queryRecordIdByTableId(tableIds, userId);
			Map<Long, List<Long>> tempMap = new HashMap<>();
			List<Long> tempList = null;
			long tempTableId = 0;
			if(recordIds != null && !recordIds.isEmpty()) {
				for (UserPlaylog userPlaylog : recordIds) {
					tempTableId = userPlaylog.getTableId();
					tempList = tempMap.get(tempTableId);
					if (tempList == null) {
						tempList = new ArrayList<>();
						tempMap.put(tempTableId, tempList);
					}
					tempList.add(userPlaylog.getId());
				}
			}
			lists.addAll(tempMap.values());
		}
		if (lists.size() <= 0) {
			playLogMap.put("playLog", Collections.EMPTY_LIST);
			result.put("playLogMap", playLogMap);
			result.put("code", 0);
			this.writeMsg(0, result);
//			player.writeComMessage(WebSocketMsgType.res_code_record, JacksonUtil.writeValueAsString(playLogMap));
			return;
		}

		List<Long> selList;
		if (logId != 0) {
			// 查询详情
			selList = new ArrayList<Long>();
			for (List<Long> list : lists) {
				if (list != null && !list.isEmpty() && list.contains(logId)) {
					if(logType != 0){
						selList.add(list.get(list.size() - 1));
					}else{
						selList = list;
					}
				}
			}
		} else {
			// 查看每一大局的最后一局
			selList = new ArrayList<Long>();
			for (List<Long> list : lists) {
				if (list != null && !list.isEmpty()) {
					selList.add(list.get(list.size() - 1));
				}
			}
		}

		List<UserPlaylog> logList = null;
		if (selList.isEmpty()) {
			logList = new ArrayList<>();
		} else {
			logList = DaikaiTableDao.getInstance().selectUserLogByLogId(selList);
		}
		
		// 筛选一下
		screen(logList, logType);
		if (!logList.isEmpty() && logId == 0 && logType != 0) {
			Collections.reverse(logList);
		}
		List<UserPlayTableMsg> playLog = buildUserPlayTbaleMsg(logId, logType, logList, userId, wanfaType);
		
		for (UserPlayTableMsg userPlayTableMsg : playLog) {
			DaikaiTable tempTable = tempTableIds.get(userPlayTableMsg.getTableId());
			if (tempTable != null) {
				userPlayTableMsg.setCreatePara(tempTable.getCreatePara());
				userPlayTableMsg.setCreateStrPara(tempTable.getCreateStrPara());
			}
		}
		
		playLogMap.put("playLog", playLog);
		
//		if (logId != 0) {
//			player.writeComMessage(WebSocketMsgType.res_code_record, logType, 0, JacksonUtil.writeValueAsString(playLogMap));
//		} else {
//			player.writeComMessage(WebSocketMsgType.res_code_record, logType, JacksonUtil.writeValueAsString(playLogMap));
//		}
		
		result.put("playLogMap", playLogMap);
		this.writeMsg(0, result);
	}


	public void screen(List<UserPlaylog> logList, int logType) {
		Iterator<UserPlaylog> iterator = logList.iterator();
		Date now = TimeUtil.now();
		while (iterator.hasNext()) {
			UserPlaylog log = iterator.next();
			int apartHours = TimeUtil.apartHours(now, log.getTime());
			int hour = 24;
			if (apartHours > hour) {
				iterator.remove();
			} else if (logType != 0 && SharedConstants.getType((int) log.getLogId()) != logType) {
				iterator.remove();
			}
		}
	}
	public List<UserPlayTableMsg> buildUserPlayTbaleMsg(long logId, int logType, List<UserPlaylog> list, long viewUserId, int wanfa){
		if(wanfa == 8){
			return buildUserPlayTbaleMsgDtz(logId, logType,list,viewUserId);
		}else{
			return buildUserPlayTbaleMsg1(logId, logType,list,viewUserId);
		}
	}
	public List<UserPlayTableMsg> buildUserPlayTbaleMsg1(long logId, int logType, List<UserPlaylog> list, long viewUserId) {
		List<UserPlayTableMsg> result = new ArrayList<UserPlayTableMsg>();
		for (UserPlaylog log : list) {
			if (logId != 0 && logType==0 && StringUtils.isBlank(log.getOutCards())) {
				// 查询详细牌局信息 但是没有打牌记录
				continue;
			}
			UserPlayTableMsg msg = new UserPlayTableMsg();
			msg.setId(log.getId());
			msg.setPlayType((int) log.getLogId());
			msg.setPlayCount(log.getCount());
			msg.setTableId(log.getTableId());
			msg.setTime(TimeUtil.formatTime(log.getTime()));
			msg.setClosingMsg(log.getExtend());
			msg.setMaxPlayerCount(log.getMaxPlayerCount());
			String res = log.getRes();
			List<String> resList = JacksonUtil.readValue(res, new TypeReference<List<String>>() {
			});
			msg.setResList(resList);
			msg.setPlay(log.getOutCards());
			List<UserPlayMsg> playerMsgList = new ArrayList<UserPlayMsg>();
			for (String resValue : resList) {
				JsonWrapper resMap = new JsonWrapper(resValue);
				long userId = resMap.getLong("userId", 0);
				int leftCardNum = resMap.getInt("leftCardNum", 0);
				int isHu = resMap.getInt("isHu", 0);
				int point = resMap.getInt("point", 0);
				if (userId == viewUserId) {
					if (log.getLogId() == 15 || log.getLogId() == 16) {
						if (leftCardNum == 0) {
							msg.setIsWin(1);

						}
					} else if (log.getLogId() < 10) {
						if (isHu > 0) {
							msg.setIsWin(1);
						}
					} else {
						if(point > 0){
							msg.setIsWin(1);
						}
					}
				}

				UserPlayMsg playerMsg = new UserPlayMsg();
				playerMsg.setName(resMap.getString("name"));
				playerMsg.setPoint(resMap.getInt("point", 0));
				playerMsg.setTotalPoint(resMap.getInt("totalPoint", 0));
				playerMsg.setUserId(userId);
				playerMsg.setSex(resMap.getInt("sex", 1));
				if (resMap.isHas("bopiPoint")) {
					playerMsg.setBopiPoint(resMap.getInt("bopiPoint", 0));
				}
				if (resMap.isHas("ext") && !StringUtils.isEmpty(resMap.getString("ext"))){
					playerMsg.setExt(resMap.getString("ext").replaceAll("\\\\", ""));
				}
				if (userId == viewUserId) {
					playerMsgList.add(0, playerMsg);
				} else {
					playerMsgList.add(playerMsg);

				}
			}
			msg.setPlayerMsg(playerMsgList);
			result.add(msg);
		}
		return result;
	}
	public List<UserPlayTableMsg> buildUserPlayTbaleMsgDtz(long logId, int logType, List<UserPlaylog> list, long viewUserId) {
		List<UserPlayTableMsg> result = new ArrayList<UserPlayTableMsg>();
		for (UserPlaylog log : list) {
			if (logId != 0 && logType==0 && StringUtils.isBlank(log.getOutCards())) {
				// 查询详细牌局信息 但是没有打牌记录
				continue;
			}
			UserPlayTableMsg msg = new UserPlayTableMsg();
			msg.setId(log.getId());
			msg.setPlayType((int) log.getLogId());
			msg.setPlayCount(log.getCount());
			msg.setTotalCount(log.getTotalCount());
			msg.setTableId(log.getTableId());
			msg.setTime(TimeUtil.formatTime(log.getTime()));
			msg.setClosingMsg(log.getExtend());
			JSONObject jsonObject = null;
			if (!StringUtils.isEmpty(log.getExtend())) {
				try{
					jsonObject = JSONObject.parseObject(log.getExtend());
					msg.setGroupScoreA((jsonObject.containsKey("aScore") ? jsonObject.getIntValue("aScore") : 0));
					msg.setGroupScoreB((jsonObject.containsKey("bScore") ? jsonObject.getIntValue("bScore") : 0));
					msg.setGroupScoreC((jsonObject.containsKey("cScore") ? jsonObject.getIntValue("cScore") : 0));
					if(jsonObject.containsKey("cutCardList")){
						List<Integer> loadKou8CardList = new ArrayList<Integer>();
				    	if (jsonObject.getString("cutCardList") != null && !jsonObject.getString("cutCardList").isEmpty()){
				    		JSONArray jsonArray = JSONArray.parseArray(jsonObject.getString("cutCardList"));
							for (Object val : jsonArray) {
								loadKou8CardList.add(Integer.valueOf(val.toString()));
							}
						}
						msg.setCutCardList(loadKou8CardList);
					}else{
						msg.setCutCardList(new ArrayList<Integer>());
					}
				}catch (Exception e){e.printStackTrace();}
			}
			String res = log.getRes();
			List<String> resList = JacksonUtil.readValue(res, new TypeReference<List<String>>() {});
			msg.setResList(resList);
			msg.setPlay(log.getOutCards());
			List<UserPlayMsg> playerMsgList = new ArrayList<UserPlayMsg>();
			for (String resValue : resList) {
				JsonWrapper resMap = new JsonWrapper(resValue);
				long userId = resMap.getLong("userId", 0);
				int leftCardNum = resMap.getInt("leftCardNum", 0);
				int isHu = resMap.getInt("isHu", 0);
				int point = resMap.getInt("point", 0);
				if (userId == viewUserId) {
					if (log.getLogId() == 15 || log.getLogId() == 16) {
						if (leftCardNum == 0) {
							msg.setIsWin(1);

						}
					} else if (log.getLogId() < 10) {
						if (isHu > 0) {
							msg.setIsWin(1);
						}
					} else {
						if (point > 0) {
							msg.setIsWin(1);
						}
					}
				}

				UserPlayMsg playerMsg = new UserPlayMsg();
				playerMsg.setName(resMap.getString("name"));
				playerMsg.setPoint(resMap.getInt("point", 0));
				playerMsg.setTotalPoint(resMap.getInt("totalPoint", 0));
				playerMsg.setWinLossPoint(resMap.getInt("boom", 0));
				
					String ext = resMap.getString("ext");
					if (!StringUtils.isEmpty(ext)) {
						JSONArray jsonArray = JSONArray.parseArray(ext);
						playerMsg.setGroup(jsonArray.getIntValue(12));
						playerMsg.setMingci(jsonArray.getIntValue(13));
						playerMsg.setWinGroup(jsonArray.size() >= 15 ? jsonArray.getIntValue(14) : 0);
						playerMsg.setDtzOrXiScore(jsonArray.size() >= 16 ? jsonArray.getIntValue(15) : 0);
					}
					playerMsg.setJiangli((jsonObject == null) ? 0 : jsonObject.getIntValue("jiangli"));
				playerMsg.setSex(resMap.getInt("sex", 0));
				if (resMap.isHas("bopiPoint")) {
					playerMsg.setBopiPoint(resMap.getInt("bopiPoint", 0));
				}
				playerMsg.setUserId(userId);
				if (userId == viewUserId) {
					playerMsgList.add(0, playerMsg);
				} else {
					playerMsgList.add(playerMsg);

				}
			}
			msg.setPlayerMsg(playerMsgList);
			result.add(msg);
		}
		return result;
	}

}
