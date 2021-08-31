package com.sy599.game.gcommand.group;

import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.db.bean.group.GroupTable;
import com.sy599.game.db.bean.group.GroupTableConfig;
import com.sy599.game.db.dao.group.GroupDao;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.manager.TableManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.util.LangHelp;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class GroupChangeTableCommand extends BaseCommand {

    @Override
    public void execute(Player player, MessageUnit message) throws Exception {
        ComMsg.ComReq req = (ComMsg.ComReq) this.recognize(ComMsg.ComReq.class, message);
        List<String> strParams = req.getStrParamsList();
        int paramsSize = strParams == null ? 0 : strParams.size();
        if (paramsSize < 1) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_3));
            return;
        }
        String groupId = strParams.get(0);
        BaseTable table = TableManager.getInstance().getTable(player.getPlayingTableId());
        String modeId;
        int gameType;
        if (table==null){
            GroupTableConfig groupTableConfig = GroupDao.getInstance().loadLastGroupTableConfig(Long.parseLong(groupId),0);
            if (groupTableConfig!=null){
                modeId = groupTableConfig.getKeyId().toString();
                gameType = groupTableConfig.getGameType();
            }else{
                player.writeErrMsg("换桌失败，没有找到合适的房间");
                return;
            }
        }else{
            if (table.getState() != SharedConstants.table_state.ready || table.getPlayedBureau()>0||table.getPlayBureau()>1){
                player.writeErrMsg("牌局已开始，不能换桌");
                return;
            }

            if (!table.isGroupRoom()){
                player.writeErrMsg("换桌失败，没有找到合适的房间");
                return;
            }

            if (!groupId.equals(table.loadGroupId())){
                player.writeErrMsg("换桌失败，ID不匹配:"+groupId+"-"+table.loadGroupId());
                return;
            }

            String tableKey = table.loadGroupTableKeyId();
            if (StringUtils.isBlank(tableKey)){
                player.writeErrMsg("换桌失败，没有找到合适的房间");
                return;
            }

            GroupTable groupTable = table.getGroupTable();
            if (groupTable==null){
                groupTable=GroupDao.getInstance().loadGroupTableByKeyId(tableKey);
                table.setGroupTable(groupTable);
            }
            if (groupTable==null||!tableKey.equals(groupTable.getKeyId().toString())){
                player.writeErrMsg("换桌失败，没有找到合适的房间");
                return;
            }

            modeId = groupTable.getConfigId().toString();
            gameType = table.getPlayType();

            GroupTable groupTable1 = GroupDao.getInstance().loadRandomSameModelTable(groupTable.getConfigId(),groupTable.getGroupId().intValue());
            if (groupTable1==null||tableKey.equals(groupTable1.getKeyId().toString())){
                player.writeErrMsg("换桌失败，没有可换的牌桌");
                return;
            }

            synchronized (table){
                if (table.getState() != SharedConstants.table_state.ready || table.getPlayedBureau()>0||table.getPlayBureau()>1){
                    player.writeErrMsg("牌局已开始，不能换桌");
                    return;
                }
                if (table.canQuit(player)&&table.quitPlayer(player)){
                    table.onPlayerQuitSuccess(player);
                    table.updateRoomPlayers();
                }
            }
        }

        if (StringUtils.isNotBlank(modeId)) {
            player.writeComMessage(WebSocketMsgType.com_group_change_room,groupId,modeId,gameType);
        }

    }

}
