package com.sy599.game.gcommand.group;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sy.mainland.util.HttpUtil;
import com.sy.mainland.util.redis.Redis;
import com.sy.mainland.util.redis.RedisUtil;
import com.sy599.game.GameServerConfig;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.db.bean.Server;
import com.sy599.game.db.bean.group.GroupTable;
import com.sy599.game.db.dao.group.GroupDao;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.manager.ServerManager;
import com.sy599.game.manager.TableManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.util.GroupRoomUtil;
import com.sy599.game.util.LangHelp;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.ResourcesConfigsUtil;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.*;

import static com.sy599.game.websocket.constant.WebSocketMsgType.res_com_group_table_msg;

public class GroupTableCommand extends BaseCommand {

    @Override
    public void execute(Player player, MessageUnit message) throws Exception {
        ComMsg.ComReq req = (ComMsg.ComReq) this.recognize(ComMsg.ComReq.class, message);
        List<String> strParams = req.getStrParamsList();
        int paramsSize = strParams == null ? 0 : strParams.size();
        if (paramsSize < 2) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_3));
            return;
        }

        String groupId = strParams.get(0);
        String tableKey = strParams.get(1);
        int groupRoom = strParams.size()>=3?Integer.parseInt(strParams.get(2)):0;

        if (groupId.length() == 0 || tableKey.length() == 0) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_3));
            return;
        }

        GroupTable groupTable;
        if (Redis.isConnected() && "1".equals(ResourcesConfigsUtil.loadServerPropertyValue("load_group_tables_from_redis"))) {
            String temp = RedisUtil.hget(GroupRoomUtil.loadGroupTableKey(String.valueOf(groupId),groupRoom), tableKey);
            if (StringUtils.isNotBlank(temp)) {
                groupTable = JSON.parseObject(temp, GroupTable.class);
            } else {
                groupTable = null;
            }
        } else {
            groupTable = GroupDao.getInstance().loadGroupTableByKeyId(tableKey);
        }

        if (groupTable == null || groupTable.isOver()) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_217));
            return;
        } else if (groupTable.isNotStart()) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_218));
            return;
        }

        int playBureau = 1;
        String msg = "{}";
        List<String> retList = new ArrayList<>(groupTable.getMaxCount().intValue());
        if (groupTable.getServerId().equals(String.valueOf(GameServerConfig.SERVER_ID))) {
            BaseTable table = TableManager.getInstance().getTable(groupTable.getTableId().longValue());
            if (table != null) {
                playBureau = groupTable.getDealCount();
                Map<Long, Player> players = table.getPlayerMap();
                for (Player player1 : players.values()) {
                    JSONObject json = new JSONObject();
                    json.put("userId", player1.getUserId());
                    json.put("online", player1.getIsOnline());
                    json.put("name", player1.getRawName());
                    json.put("headimgurl", player1.getHeadimgurl());
                    json.put("score", player1.loadAggregateScore());
                    json.put("tzScore", player1.loadTzScore());
                    retList.add(json.toString());
                }
                msg = JSON.toJSONString(retList);
            }
        } else {
            String ret = loadResult(NumberUtils.toInt(groupTable.getServerId(), 1), groupTable.getTableId().toString(), groupTable.getKeyId().toString());
            if (StringUtils.isNotBlank(ret)) {
                JSONObject json = JSON.parseObject(ret);
                if ("0".equals(json.getString("code"))) {
//                    playBureau = json.getIntValue("playBureau");
                    playBureau = json.getIntValue("dealCount");
                    msg = json.getString("msg");
                }
            }
        }

        player.writeComMessage(res_com_group_table_msg, groupId, tableKey, playBureau, msg);
    }

    protected String loadResult(int serverId, String tableId, String tableKey) {
        Server server1 = ServerManager.loadServer(serverId);
        if (server1 != null) {
            String url = server1.getIntranet();
            if (StringUtils.isBlank(url)) {
                url = server1.getHost();
            }

            if (StringUtils.isNotBlank(url)) {
                int idx = url.indexOf(".");
                if (idx > 0) {
                    idx = url.indexOf("/", idx);
                    if (idx > 0) {
                        url = url.substring(0, idx);
                    }
                    long now = System.currentTimeMillis();
                    url += "/group/msg.do?type=table&tableId=" + tableId + "&tableKey=" + tableKey;
                    String ret = HttpUtil.getUrlReturnValue(url, 2);
                    LogUtil.msgLog.info("group result:url=" + url + ",ret=" + ret+",times::"+(System.currentTimeMillis()-now));
                    return ret;
                }
            }
        }

        return null;
    }


    @Override
    public void setMsgTypeMap() {
    }

}
