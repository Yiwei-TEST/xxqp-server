package com.sy599.game.gcommand.com.competition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.db.bean.Server;
import com.sy599.game.db.bean.competition.CompetitionRoom;
import com.sy599.game.db.bean.competition.CompetitionRoomConfig;
import com.sy599.game.db.dao.CompetitionDao;
import com.sy599.game.manager.ServerManager;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.util.CompetitionUtil;
import com.sy599.game.util.JacksonUtil;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

/**
 * @author Guang.OuYang
 * @date 2020/6/2-17:52
 */
public class CompetitionServerCommand {

	public static String process(Player player, ComReq req) {
		String res = null;
		try {
			if (req.getStrParamsCount() <= 0) {
				player.writeErrMsg(LangMsg.code_3);
				return res;
			}
			List<String> strParams = req.getStrParamsList();

			Long configId = Long.valueOf(strParams.get(0));
			Long playingId = Long.valueOf(strParams.get(1));
			StringBuilder sb = new StringBuilder("CompetitionServerCommand|Room|start");
			sb.append("|").append(player.getUserId());
			sb.append("|").append(strParams);
			LogUtil.msgLog.info(sb.toString());
			if (configId <= 0) {
				player.writeErrMsg(LangMsg.code_3);
				return res;
			}
			CompetitionRoomConfig config = CompetitionUtil.getCompetitionRoomConfig(configId);
			if (config == null) {
				player.writeErrMsg("玩法不存在或暂未开放");
				return res;
			}
			if (player.loadAllGolds() < config.getJoinLimit()) {
				player.writeErrMsg(LangMsg.code_903);
				return res;
			}
			Server server = null;
			CompetitionRoom room = CompetitionDao.getInstance().randomCompetitionRoom(configId, playingId);
			if (room != null) {
				server = ServerManager.loadServer(room.getServerId());
			}
			if (server == null) {
				server = ServerManager.loadServer(config.getPlayType(), Server.SERVER_TYPE_COMPETITION_ROOM);
			}
			if (server == null) {
				player.writeErrMsg(LangMsg.code_0);
				LogUtil.errorLog.error("processGoldRoom|fail|" + player.getUserId() + "|" + strParams);
				return res;
			}
			Map<String, Object> result = new HashMap<>();
			Map<String, Object> serverMap = new HashMap<>();
			serverMap.put("serverId", server.getId());
			serverMap.put("connectHost", server.getChathost());
			result.put("server", serverMap);
			if (room != null) {
				result.put("playingRoomId", room.getKeyId());
			} else {
				result.put("playingRoomId", 0);
			}
			result.put("code", 0);

			res = JSONObject.toJSONString(result);
			player.writeComMessage(WebSocketMsgType.res_code_getserverid, JacksonUtil.writeValueAsString(result));

			sb = new StringBuilder("GetServerCommand|CompetitionRoom|end");
			sb.append("|").append(player.getUserId());
			sb.append("|").append(result);
			LogUtil.msgLog.info(sb.toString());
		} catch (Exception e) {
			LogUtil.errorLog.error("processGoldRoom|error|" + e.getMessage(), e);
		}

		return res;
	}
}
