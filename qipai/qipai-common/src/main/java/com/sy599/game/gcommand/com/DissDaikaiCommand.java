package com.sy599.game.gcommand.com;

import com.sy599.game.GameServerConfig;
import com.sy599.game.assistant.AssisServlet;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.db.bean.DaikaiTable;
import com.sy599.game.db.dao.TableDao;
import com.sy599.game.db.enums.CardSourceType;
import com.sy599.game.db.enums.UserMessageEnum;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.manager.ServerManager;
import com.sy599.game.manager.TableManager;
import com.sy599.game.message.MessageUtil;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.util.HttpGameUtil;
import com.sy599.game.util.LangHelp;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.StringUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

import java.util.HashMap;
import java.util.Map;

public class DissDaikaiCommand extends BaseCommand {

	@Override
	public void execute(Player player, MessageUnit message) throws Exception {
		ComReq req = (ComReq) this.recognize(ComReq.class, message);

		if (req.getStrParamsCount() < 1) {
			return;
		}
		String tableIdStr = req.getStrParams(0);
		long tableId = 0;
		if (!tableIdStr.isEmpty()) {
			tableId = Long.parseLong(tableIdStr);
		}
		DaikaiTable daikaiTable = TableDao.getInstance().getDaikaiTableById(tableId);
		if (daikaiTable == null) {
			player.writeErrMsg(LangMsg.code_1, tableId);
			return;
		}

        if (!StringUtil.isBlank(daikaiTable.getPlayerInfo())) {
            player.writeErrMsg(LangMsg.code_42, tableId);
            return;
        }

        int returnCard = daikaiTable.getNeedCard();
        int serverId = daikaiTable.getServerId();
        if (serverId>0 && serverId != GameServerConfig.SERVER_ID) {
            // 如果房间不在本服 则通知其他服解散房间
            Map<String, String> map = new HashMap<>();
            map.put("tableIds", tableIdStr);
            map.put("specialDiss", "2");
            String res = HttpGameUtil.sendDissInfo(serverId, map);
            LogUtil.msg("sendDissInfo-->serverId:" + serverId + ",infoMap:" + ServerManager.loadServer(serverId) + ",res:" + res);
            if (tableIdStr.equals(res)) {
                if (returnCard!=0) {
                    player.changeCards(returnCard, 0, true, CardSourceType.daikaiTable_diss_FZ);
                }
                LogUtil.msgLog.info("daikai diss table:tableId=" + daikaiTable.getTableId() + ",daikaiId=" + daikaiTable.getDaikaiId());
                player.writeComMessage(WebSocketMsgType.res_com_code_dissdaikai, tableId);
                // 存到消息
				MessageUtil.sendMessage(UserMessageEnum.TYPE1,player, "解散代开房间[" + tableId + "]返还:房卡x" + returnCard,null);
            } else {
                if ("code_42".equals(res)) {
                    player.writeErrMsg(LangMsg.code_42, tableId);
                } else {
                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_59));
                }
            }
            return;
        }

		BaseTable table = TableManager.getInstance().getTable(tableId);
		// 获得房间的信息,如果房间内存在玩家则不能解散
		if (table != null) {
			// 房间有人不能解散
			if (table.getPlayerCount() > 0) {
				player.writeErrMsg(LangMsg.code_42, tableId);
				return;
			}
			Map<Long, Player> roomPlayerMap = new HashMap<>(table.getRoomPlayerMap());

			table.setTiqianDiss(true);
            LogUtil.msgLog.info("BaseTable|dissReason|DissDaikaiCommand|1|" + table.getId() + "|" + table.getPlayBureau() + "|" + player.getUserId());
			if (table.diss() > 0) {
			    if (returnCard>0) {
                    player.changeCards(returnCard, 0, true, CardSourceType.daikaiTable_diss_FZ);
                }
				LogUtil.msgLog.info("daikai diss table:tableId=" + table.getId() + ",master=" + table.getMasterId() + ",userId=" + player.getUserId() + ",seat=" + player.getSeat());
				player.writeComMessage(WebSocketMsgType.res_com_code_dissdaikai, tableId);
				for (Player roomPlayer : roomPlayerMap.values()) {
					roomPlayer.setPlayingTableId(0);
					roomPlayer.saveBaseInfo();
					roomPlayer.writeComMessage(WebSocketMsgType.res_code_err, LangHelp.getMsg(LangMsg.code_8, table.getId()), WebSocketMsgType.sc_code_err_table);
				}
				// 存到消息
				MessageUtil.sendMessage(UserMessageEnum.TYPE1,player, "解散房间[" + tableId + "]返还:钻石x" + returnCard + (returnCard==0?" AA代开":""),null);
			}
			return;
		}

		// 还没有创建房间
		if (TableDao.getInstance().dissDaikaiTable(tableId, false) > 0 && returnCard>0) {
			player.changeCards(returnCard, 0, true, CardSourceType.daikaiTable_diss_FZ);
		}
		player.writeComMessage(WebSocketMsgType.res_com_code_dissdaikai, tableId);
        // 存到消息
		MessageUtil.sendMessage(UserMessageEnum.TYPE1,player, "解散房间[" + tableId + "]返还:钻石x" + returnCard + (returnCard==0?" AA代开":""),null);
		if (SharedConstants.isAssisOpen()&&!StringUtil.isBlank(daikaiTable.getAssisCreateNo())) {
			AssisServlet.sendRoomStatus(daikaiTable, "1");
		}
	}

    @Override
	public void setMsgTypeMap() {
		// TODO Auto-generated method stub

	}

}
