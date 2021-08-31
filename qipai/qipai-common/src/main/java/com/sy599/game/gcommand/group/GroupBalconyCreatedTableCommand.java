package com.sy599.game.gcommand.group;

import java.util.*;

import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.db.bean.group.GroupTable;
import com.sy599.game.db.dao.group.GroupDao;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.gcommand.table.JoinTableCommand;
import com.sy599.game.manager.TableManager;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.ResourcesConfigsUtil;
import com.sy599.game.util.StringUtil;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.msg.serverPacket.TableRes.CreateTableRes;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import org.apache.commons.lang3.math.NumberUtils;

public class GroupBalconyCreatedTableCommand extends BaseCommand {
    @Override
    public void execute(Player player, MessageUnit message) throws Exception {
        if (player.getMyExtend().isGroupMatch()){
            player.writeErrMsg(LangMsg.code_257);
            return;
        }

        ComReq req = (ComReq) this.recognize(ComReq.class, message);
        List<Integer> params = req.getParamsList();
        List<String> strParams = req.getStrParamsList();
        String groupIdStr = strParams.size()>0&& NumberUtils.isDigits(strParams.get(0))&&Long.parseLong(strParams.get(0))>0L?strParams.get(0):null;  //俱乐部id
        int groupId =  groupIdStr != null ? Integer.parseInt(groupIdStr) : 0;
        if("1".equals(ResourcesConfigsUtil.loadServerPropertyValue("group_table_more_wanfa")) && groupId > 0){  //多玩法俱乐部创房
            int gameType = StringUtil.getIntValue(params,0,0);  //游戏类型
            int serverType = StringUtil.getIntValue(params,1,1);  //游戏服类型0练习场1普通场2金币场3竞技场
            int playNo = StringUtil.getIntValue(params,2,-1);  //房间玩法编号
            int userId = StringUtil.getIntValue(params, 3, 0);   //用户id

            String tableCount =  strParams.size()>1?strParams.get(1):"1";
            String tableVisible = strParams.size()>2?strParams.get(2):"1";
            String configId = strParams.size()>3?strParams.get(3):"";  //创房模式ID

            GroupTable groupTable = GroupDao.getInstance().selectUserGroupTable(userId+"",groupId,Integer.parseInt(configId));
            List<Integer> params2 = new ArrayList<>();
            List<String> strParams2 = new ArrayList<>();
            if(groupTable == null || groupTable.getCurrentCount() >= groupTable.getMaxCount()){
                params2.clear();
                strParams2.clear();
                strParams2.add(groupId+"");
                strParams2.add(tableCount);
                strParams2.add(tableVisible);
                strParams2.add(configId);

                StringBuilder sb = new StringBuilder("createTable1|GroupBalconyCreatedTableCommand|1");
                sb.append("|").append(player.getUserId());
                sb.append("|").append(params2);
                sb.append("|").append(strParams2);
                sb.append("|").append(userId);
                LogUtil.monitorLog.info(sb.toString());

                TableManager.getInstance().createTable(player, params2, strParams2, 0, userId,null);
            }else {
                int tableId = groupTable.getTableId();  //房间id
                params2.clear();
                strParams2.clear();
                params2.add(tableId);
                params2.add(gameType);
                params2.add(serverType);
                params2.add(playNo);
                params2.add(groupId);
                strParams2.add(configId);
                new JoinTableCommand().execute(player,params2,strParams2);
            }
        }
    }

    @Override
    public void setMsgTypeMap() {
        msgTypeMap.put(CreateTableRes.class, WebSocketMsgType.sc_createtable);
    }

}
