package com.sy599.game.qipai.dtz.command.play;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.db.bean.UserPlaylog;
import com.sy599.game.db.dao.TableLogDao;
import com.sy599.game.msg.UserPlayMsg;
import com.sy599.game.msg.UserPlayTableMsg;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.qipai.dtz.constant.DtzzConstants;
import com.sy599.game.util.JacksonUtil;
import com.sy599.game.util.JsonWrapper;
import com.sy599.game.util.TimeUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class RecordCommand extends BaseCommand {

	@Override
	public void execute(Player player, MessageUnit message) throws Exception {
		ComReq req = (ComReq) this.recognize(ComReq.class, message);

		int logType = 0;
		if (req.getParamsCount() > 0) {
			logType = req.getParams(0);
		}
		long logId = 0;
		if (req.getStrParamsCount() > 0) {
			logId = Long.parseLong(req.getStrParams(0));
		}
		Map<String, Object> playLogMap = new HashMap<String, Object>();
		List<List<Long>> lists = player.getRecord();
		if (lists == null) {
			playLogMap.put("playLog", Collections.EMPTY_LIST);
			player.writeComMessage(WebSocketMsgType.res_code_record, JacksonUtil.writeValueAsString(playLogMap));
			return;
		}

		List<Long> selList;
		if (logId != 0) {
			// 查询详情
			selList = new ArrayList<Long>();
			for (List<Long> list : lists) {
				if (list != null && !list.isEmpty() && list.contains(logId)) {
					selList = list;
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
			logList = TableLogDao.getInstance().selectUserLogByLogId(selList);
		}
		// 筛选一下
		screen(logList, logType);
		if (!logList.isEmpty() && logId == 0) {
			Collections.reverse(logList);
		}
		List<UserPlayTableMsg> playLog = buildUserPlayTbaleMsg(logId, logList, player.getUserId());
		playLogMap.put("playLog", playLog);
		if (logId != 0) {
			player.writeComMessage(WebSocketMsgType.res_code_record, logType, 0, JacksonUtil.writeValueAsString(playLogMap));

		} else {
			player.writeComMessage(WebSocketMsgType.res_code_record, logType, JacksonUtil.writeValueAsString(playLogMap));

		}
	}

	/**
	 * 筛选
	 * 
	 * @param logList
	 * @param logType
	 */
	public void screen(List<UserPlaylog> logList, int logType) {
		Iterator<UserPlaylog> iterator = logList.iterator();
		Date now = TimeUtil.now();
		while (iterator.hasNext()) {
			UserPlaylog log = iterator.next();
			int apartHours = TimeUtil.apartHours(now, log.getTime());
			int hour = 48;
			if (apartHours > hour) {
				iterator.remove();
			} else if (logType != 0 && SharedConstants.getType((int) log.getLogId()) != logType) {
				iterator.remove();
			}
		}
	}

	public List<UserPlayTableMsg> buildUserPlayTbaleMsg(long logId, List<UserPlaylog> list, long viewUserId) {
		List<UserPlayTableMsg> result = new ArrayList<UserPlayTableMsg>();
		for (UserPlaylog log : list) {
			if (logId != 0 && StringUtils.isBlank(log.getOutCards())) {
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
				
				if (msg.getPlayType() == DtzzConstants.play_type_3POK || msg.getPlayType() == DtzzConstants.play_type_4POK 
						|| msg.getPlayType() == DtzzConstants.play_type_3PERSON_3POK || msg.getPlayType() == DtzzConstants.play_type_3PERSON_4POK 
						|| msg.getPlayType() == DtzzConstants.play_type_2PERSON_3POK || msg.getPlayType() == DtzzConstants.play_type_2PERSON_4POK) {
					String ext = resMap.getString("ext");
					if (!StringUtils.isEmpty(ext)) {
						JSONArray jsonArray = JSONArray.parseArray(ext);
						playerMsg.setGroup(jsonArray.getIntValue(12));
						playerMsg.setMingci(jsonArray.getIntValue(13));
						playerMsg.setWinGroup(jsonArray.size() >= 15 ? jsonArray.getIntValue(14) : 0);
						playerMsg.setDtzOrXiScore(jsonArray.size() >= 16 ? jsonArray.getIntValue(15) : 0);
					}
					playerMsg.setJiangli((jsonObject == null) ? 0 : jsonObject.getIntValue("jiangli"));
				}
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

	@Override
	public void setMsgTypeMap() {

	}

}
