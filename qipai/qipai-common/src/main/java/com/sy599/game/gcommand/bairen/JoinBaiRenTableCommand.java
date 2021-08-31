package com.sy599.game.gcommand.bairen;

import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.db.bean.Server;
import com.sy599.game.db.bean.gold.GoldRoom;
import com.sy599.game.db.bean.gold.GoldRoomUser;
import com.sy599.game.db.bean.group.GroupTable;
import com.sy599.game.db.dao.gold.GoldRoomDao;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.jjs.util.JjsUtil;
import com.sy599.game.manager.TableManager;
import com.sy599.game.msg.serverPacket.BaiRenTableMsg;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.TableRes;
import com.sy599.game.util.*;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

import java.util.ArrayList;
import java.util.List;

/**
 * 加入百人房间 暂时一个玩法对应一个房间
 */
public class JoinBaiRenTableCommand extends BaseCommand {

    @Override
    public void execute(Player player, MessageUnit message) throws Exception {
        if (player.getMyExtend().isGroupMatch()) {
            player.writeErrMsg(LangMsg.code_257);
            return;
        }
        ComMsg.ComReq req = (ComMsg.ComReq) this.recognize(ComMsg.ComReq.class, message);
        List<Integer> paramList = req.getParamsList();
        List<String> strParamList = req.getStrParamsList();
        if (player.isMatching()) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_221));
            return;
        }
        int paramsCount = paramList != null ? paramList.size() : 0;
        int strParamsCount = strParamList != null ? strParamList.size() : 0;
        int gameType = paramsCount > 0 ? paramList.get(0).intValue() : 0;// 玩法ID
//        int serverType = paramsCount > 2 ? paramList.get(1).intValue() : 1;//游戏服类型0练习场1普通场2金币场3竞技场4百人场
//        if(serverType != 4) {
//            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_3, player.getPlayingTableId()));
//            return;
//        }
        BaseTable table = player.getPlayingTable();
        if (table != null) {// 玩家已正在玩的房间中
            if(player.getPlayingTableId() != gameType) {
                player.setPlayingTableId(gameType);
                player.saveBaseInfo();
            }
            BaiRenTableMsg.BaiRenTableRes res = table.buildBaiRenTableRes(player.getUserId(), true, false);
            player.writeSocket(res);
            table.broadOnlineStateMsg();
            return;
        }
        if (player.getPlayingTableId() != 0) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_2, player.getPlayingTableId()));
            return;
        }
        if (player.isPlayingMatch()) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_221));
            return;
        }
        player.setGroupUser(null);// 重置玩家俱乐部成员数据
        player.setIsGroup(0);
        List<Integer> params = new ArrayList<>();
        params.add(gameType);
        table = TableManager.getInstance().createBaiRenTable(player, params ,new ArrayList<>());
        if(table != null)
            player.writeSocket(table.buildBaiRenTableRes(player.getUserId()));
        return;
    }

    @Override
    public void setMsgTypeMap() {

    }

    public static void joinTable(Player player) throws Exception {
        List<Integer> params = new ArrayList<>();
        params.add(GameUtil.game_type_longhudou);
        BaseTable table = TableManager.getInstance().createBaiRenTable(player, params ,new ArrayList<>());
        player.writeSocket(table.buildBaiRenTableRes(player.getUserId()));
    }
}
