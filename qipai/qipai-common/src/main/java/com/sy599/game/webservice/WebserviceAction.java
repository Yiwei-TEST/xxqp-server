package com.sy599.game.webservice;

import com.sy.mainland.util.redis.Redis;
import com.sy.mainland.util.redis.RedisUtil;
import com.sy599.game.assistant.AssisServlet;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.character.ProtoPlayer;
import com.sy599.game.common.action.BaseAction;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.db.bean.DaikaiTable;
import com.sy599.game.db.bean.RegInfo;
import com.sy599.game.db.bean.group.GroupTable;
import com.sy599.game.db.dao.TableDao;
import com.sy599.game.db.dao.UserDao;
import com.sy599.game.db.dao.group.GroupDao;
import com.sy599.game.db.enums.CardSourceType;
import com.sy599.game.manager.MonitorManger;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.manager.TableManager;
import com.sy599.game.util.GroupRoomUtil;
import com.sy599.game.util.JacksonUtil;
import com.sy599.game.util.JsonWrapper;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.ServerUtil;
import com.sy599.game.util.StringUtil;
import com.sy599.game.websocket.MyWebSocket;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WebserviceAction extends BaseAction {

	@Override
	public void execute() throws Exception {
		switch (this.getInt("funcType")) {
			case 1:
				getMonitorMsg();
				break;
			case 2:
				refreshPlayer();
				break;
			case 3:
				tickoffPlayer();
				break;
			case 4:
				dissTable();
				break;
			case 5:
				createGroupTable();
				break;	
		}
	}

	private void createGroupTable() throws Exception{
		String userId = this.getString("oUserId");
		String groupId = this.getString("groupId");
		String tableCount = this.getString("tableCount");
		String tableVisible = this.getString("tableVisible");
		String modeId = this.getString("modeId");
		LogUtil.msgLog.info("WebserviceAction-->createGroupTable-->userId:{},groupId={},tableCount:{},tableVisible:{},modeId:{}",userId,groupId,tableCount,tableVisible,modeId);
		if (!StringUtil.isBlank(userId)&&!StringUtil.isBlank(groupId)&&!StringUtil.isBlank(tableCount)&&!StringUtil.isBlank(tableVisible)&&!StringUtil.isBlank(modeId)) {
			Player player= PlayerManager.getInstance().getPlayer(Long.valueOf(userId));
			if(player == null){
				RegInfo user = UserDao.getInstance().selectUserByUserId(Long.valueOf(userId));
				player = new ProtoPlayer();
				player.loadFromDB(user);
			}
			if(player != null){
				List<String> strParams = new ArrayList<String>();
				strParams.add(groupId);
				strParams.add(tableCount);
				strParams.add(tableVisible);
				strParams.add(modeId);

                StringBuilder sb = new StringBuilder("createTable1|WebserviceAction|1");
                sb.append("|").append(player.getUserId());
                sb.append("|").append(strParams);
                LogUtil.monitorLog.info(sb.toString());

				TableManager.getInstance().createTable(player, new ArrayList<Integer>(), strParams, 0, 0,null);
				if(StringUtil.isBlank(player.getErrerMsg())){
					writeMsg(getResponse(),"succeed");
				}else{
					writeMsg(getResponse(),player.getErrerMsg());
				}
				player.setErrerMsg("");
			}
		}
	}
	private void dissTable() throws Exception{
		String tableIds = this.getString("tableIds");
		String keyIds = this.getString("keyIds");
		String specialDiss = this.getString("specialDiss");
		LogUtil.msgLog.info("WebserviceAction-->dissTable-->tableIds:{},groupTableKeys={},specialDiss:{}",tableIds,keyIds,specialDiss);
		if (!StringUtil.isBlank(tableIds)&&!StringUtil.isBlank(keyIds)) {
			String[] strs = tableIds.split(",");
			String[] strs2 = keyIds.split(",");
			if (strs.length==strs2.length){
				for (int i = 0; i < strs.length; i++) {
					String tableId = strs[i];
					String keyId = strs2[i];
					long id = NumberUtils.toLong(tableId, 0);
					BaseTable table = TableManager.getInstance().getTable(id);
					if (table != null) {
						if (table.isGroupRoom()) {
							synchronized (table) {
								if ("1".equals(specialDiss)) {
									table.setSpecialDiss(1);
								}
								table.dissGroupRoom();
							}
						}
					} else {
						returnGroupRoomCards(keyId);
					}
				}
				writeMsg(getResponse(),tableIds);
			}else{
				LogUtil.errorLog.error("WebserviceAction-->dissTable params error-->tableIds:"+tableIds + ",groupTableKeys="+keyIds);
				writeMsg(getResponse(),"");
			}
		} else if (!StringUtils.isBlank(tableIds) && "2".equals(specialDiss)){
			// 解散代开
			String[] strs = tableIds.split(",");
			for (String tableId : strs) {
				long id = NumberUtils.toLong(tableId, 0);
				DaikaiTable daikaiTable = TableDao.getInstance().getDaikaiTableById(id);
				if (daikaiTable == null) {
					writeMsg(getResponse(),"code_59");
					return;
				}
				BaseTable table = TableManager.getInstance().getTable(id);
				if (table != null) {
					synchronized (table) {
						if (table.getPlayerMap().size() > 0) {
							writeMsg(getResponse(), "code_42");
							return;
						} else {
							table.setSpecialDiss(2);
							table.calcOver3();
							table.setTiqianDiss(true);
                            LogUtil.msgLog.info("BaseTable|dissReason|WebserviceAction|1|" + table.getId() + "|" + table.getPlayBureau());
							int diss = table.diss();
							if (diss <= 0) {
								LogUtil.errorLog.error("WebserviceAction-->diss daikai Table error-->tableIds:" + tableIds + ",diss=" + diss);
								writeMsg(getResponse(), "code_59");
								return;
							}
						}
					}
				} else {
					TableDao.getInstance().dissDaikaiTable(id, false);
					if (SharedConstants.isAssisOpen()&&!StringUtil.isBlank(daikaiTable.getAssisCreateNo())) {
						AssisServlet.sendRoomStatus(daikaiTable, "1");
					}
				}
			}
			writeMsg(getResponse(),tableIds);
		} else {
			writeMsg(getResponse(),"");
		}
	}

	private void returnGroupRoomCards(String keyId) throws Exception{
		GroupTable groupTable=GroupDao.getInstance().loadGroupTableByKeyId(keyId);
		if (groupTable!=null && !groupTable.isOver()){
			HashMap<String,Object> map=new HashMap<>();
			map.put("currentState",groupTable.isNotStart()?"3":"4");
			map.put("keyId",keyId);
			int ret = GroupDao.getInstance().updateGroupTableByKeyId(map);

			if (Redis.isConnected()){
				RedisUtil.zrem(GroupRoomUtil.loadGroupKey(groupTable.getGroupId().toString(),groupTable.loadGroupRoom()),groupTable.getKeyId().toString());
				RedisUtil.hdel(GroupRoomUtil.loadGroupTableKey(groupTable.getGroupId().toString(),groupTable.loadGroupRoom()),groupTable.getKeyId().toString());
			}

			LogUtil.msgLog.info("returnGroupRoomCards|dissGroupTable|msg="+ JacksonUtil.writeValueAsString(groupTable));

			if (groupTable.isNotStart()&&ret>0){
				//根据payType返回钻石currentState
                boolean repay = TableManager.repay(null, null, groupTable);
                if(!repay) {
                    String[] tempMsgs = new JsonWrapper(groupTable.getTableMsg()).getString("strs").split(";")[0].split("_");
                    String payType = tempMsgs[0];
                    if (tempMsgs.length >= 4) {
                        if ("2".equals(payType) || "3".equals(payType)) {
                            CardSourceType sourceType;
                            if ("2".equals(payType))
                                sourceType = CardSourceType.groupTable_diss_FZ;
                            else
                                sourceType = CardSourceType.groupTable_diss_QZ;
                            Player payPlayer = PlayerManager.getInstance().getPlayer(Long.valueOf(tempMsgs[2]));
                            if (payPlayer != null) {
                                payPlayer.changeCards(Integer.parseInt(tempMsgs[3]), 0, true, sourceType);
                            } else {
                                RegInfo user = UserDao.getInstance().selectUserByUserId(Long.valueOf(tempMsgs[2]));
                                payPlayer = new ProtoPlayer();
                                payPlayer.loadFromDB(user);
                                payPlayer.changeCards(Integer.parseInt(tempMsgs[3]), 0, true, sourceType);

                                if (payPlayer.getEnterServer() > 0 && user.getIsOnLine() == 1) {
                                    ServerUtil.notifyPlayerCards(payPlayer.getEnterServer(), payPlayer.getUserId(), 0, Long.valueOf(tempMsgs[3]));
                                }
                            }
                        }
                    }
                }
			}
		}
	}

	private void getMonitorMsg() throws Exception {
//		if (MinuteTask.monitorMsg == null) {
//			MinuteTask.monitorMsg = MonitorManger.getInst().buildMonitorMsg();
//		}
		this.writeErrMsg(0, MonitorManger.getInst().buildMonitorMsg());
	}

	private void tickoffPlayer() {
		long userId = this.getLong("kickoffId");
		Player player = PlayerManager.getInstance().getPlayer(userId);
		if (player == null) {
			return;
		}
		MyWebSocket myWebSocket = player.getMyWebSocket();
		if (myWebSocket == null) {
			return;
		}

	}

	private void refreshPlayer() {

	}

	private static void writeMsg(HttpServletResponse response, String msg) throws IOException {
		Writer writer = response.getWriter();
		writer.write(msg);
		writer.flush();
		writer.close();
	}
}
