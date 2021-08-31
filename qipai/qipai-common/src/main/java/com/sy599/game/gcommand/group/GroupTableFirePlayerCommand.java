package com.sy599.game.gcommand.group;

import com.alibaba.fastjson.JSONObject;
import com.sy.mainland.util.HttpUtil;
import com.sy599.game.GameServerConfig;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.db.bean.Server;
import com.sy599.game.db.bean.group.GroupTable;
import com.sy599.game.db.bean.group.GroupUser;
import com.sy599.game.db.dao.group.GroupDao;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.manager.ServerManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.util.LangHelp;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupTableFirePlayerCommand extends BaseCommand {

    @Override
    public void execute(Player player, MessageUnit message) throws Exception {
        ComMsg.ComReq req = (ComMsg.ComReq) this.recognize(ComMsg.ComReq.class, message);
        List<String> strParams = req.getStrParamsList();
        int paramsSize = strParams == null ? 0 : strParams.size();
        if (paramsSize < 3) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_3));
            return;
        }
        String groupId = strParams.get(0);
        String tableKey = strParams.get(1);
        String playerId = strParams.get(2);

        GroupUser groupUser = GroupDao.getInstance().loadGroupUser(player.getUserId(),groupId);
        int userRole;
        if (groupUser==null||(userRole=groupUser.getUserRole().intValue())!=0&&userRole!=1){
            player.writeErrMsg("您无权操作，仅限群主或管理员");
            return;
        }

        GroupTable groupTable = GroupDao.getInstance().loadGroupTableByKeyId(tableKey);
        if (groupTable==null){
            player.writeErrMsg("操作失败:房间已解散");
            return;
        }
        if (groupTable.getCurrentCount().intValue()<1){
            player.writeErrMsg("操作失败:该房间没有玩家");
            return;
        }
        if (!groupTable.isNotStart()){
            player.writeErrMsg("操作失败:牌局已开始");
            return;
        }

        if (String.valueOf(GameServerConfig.SERVER_ID).equals(groupTable.getServerId())){
            Player player1 = PlayerManager.getInstance().getPlayer(Long.valueOf(playerId));
            if (player1==null){
                player.writeErrMsg("操作失败:该玩家不在该房间中");
                return;
            }
            BaseTable table = player1.getPlayingTable();
            if (table==null||!table.isGroupRoom()||!tableKey.equals(table.loadGroupTableKeyId())){
                player.writeErrMsg("操作失败:该玩家不在该房间中");
                return;
            }
            if (!groupId.equals(table.loadGroupId())){
                player.writeErrMsg("操作失败:该房间不在此亲友圈中");
                return;
            }

            boolean success = false;
            synchronized (table){
                if (table.getState() != SharedConstants.table_state.ready || table.getPlayedBureau()>0||table.getPlayBureau()>1){
                    player.writeErrMsg("牌局已开始，不能踢除该玩家");
                    return;
                }
                if (table.canQuit(player1)&&table.quitPlayer(player1)){
                    table.onPlayerQuitSuccess(player1);
                    table.updateRoomPlayers();
                    success = true;
                    player1.writeErrMsg(LangMsg.code_269,table.getId());
                }
            }
            if (success) {
                player.writeComMessage(WebSocketMsgType.com_group_room_fire_player, groupId, tableKey,playerId);
            }else{
                player.writeErrMsg("操作失败:不能把该玩家从该房间中踢除");
            }
        }else{
            Server server = ServerManager.loadServer(Integer.parseInt(groupTable.getServerId()));
            if (server!=null) {
                Map<String, String> paramsMap = new HashMap<>();
                paramsMap.put("type", "groupRoomFire");
                paramsMap.put("groupId", groupId);
                paramsMap.put("tableKey", tableKey);
                paramsMap.put("playerId", playerId);
                String result = HttpUtil.getUrlReturnValue(ServerManager.loadRootUrl(server) + "/group/msg.do", "UTF-8", "POST", paramsMap);
                if (StringUtils.isNotBlank(result)){
                    JSONObject jsonObject = JSONObject.parseObject(result);
                    if ("0".equals(jsonObject.getString("code"))){
                        player.writeComMessage(WebSocketMsgType.com_group_room_fire_player, groupId, tableKey,playerId);
                    }else{
                        player.writeErrMsg(jsonObject.getString("msg"));
                    }
                }else{
                    player.writeErrMsg("请稍后再试");
                }
            }else{
                player.writeErrMsg("操作失败:请稍后再试");
            }
        }

    }

}
