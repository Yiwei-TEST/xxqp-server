package com.sy599.game.qipai.dtz.command.play;

import com.sy599.game.character.Player;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.qipai.dtz.bean.DtzPlayer;
import com.sy599.game.qipai.dtz.bean.DtzTable;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * required int32 code = 1;
 * repeated int32 params = 2;
 * 玩家选择座位
 * @author wangjie
 */
public class PlayerSelectSeatCommand extends BaseCommand {

    @Override
    public void execute(Player player, MessageUnit message) throws Exception {
        ComReq req = (ComReq) this.recognize(ComReq.class, message);
        DtzPlayer pdkplayer = (DtzPlayer) player;
        DtzTable pdkTable = player.getPlayingTable(DtzTable.class);

        if (pdkTable == null || req.getParamsCount() == 0) return;
        int selectSeat = req.getParams(0);
        List<Integer> seatList = Arrays.asList(1,2,3,4);
        if (!seatList.contains(selectSeat)) {
            LogUtil.e("table:" + pdkTable.getId() + " name:" + pdkplayer.getName() + " selectSeat:"+selectSeat);
            return;
        }
        if(!pdkTable.isNew){
        	return;
        }
        synchronized (pdkTable) {
        for (Player player0 : pdkTable.getPlayerMap().values()) {
            if (player0.getSeat() == selectSeat) {
                LogUtil.msg("table:" + pdkTable.getId() + " " + pdkplayer.getName() + " 座位 " + selectSeat + " 已经被选..");
                return;
            }
            player0.writeComMessage(WebSocketMsgType.RES_SELECT_SEAT_OTHER, selectSeat, (int) player.getUserId());
        }
        int group =  pdkTable.getGroup(selectSeat);
        if (group<0) { return;}
       
            pdkplayer.setSeat(selectSeat);
            pdkTable.groupPlayer(group, pdkplayer);
            LogUtil.msg("table:" + pdkTable.getId() + "玩家: " + pdkplayer.getName() + " 选择了座位  :" + selectSeat + " 他的分组是:" + group);
            // 检查分组是否完成
            chekGroupOver(pdkTable);
            // 记录分组情况的日志
            StringBuilder str = new StringBuilder("table:" + pdkTable.getId() + "现在已经选择座位的玩家: \n");
            for (Map.Entry<Integer, List<DtzPlayer>> entry : pdkTable.getGroupMap().entrySet()) {
                str.append("组:group -> ").append(entry.getKey());
                for (DtzPlayer player1 : entry.getValue()) {
                    str.append("  玩家:").append(player1.getName()).append(" 属于分组 ").append(entry.getKey()).append(" 座位 ").append(player1.getSeat());
                }
                str.append("\n");
            }
            LogUtil.msg(str.toString());
        }
    }

    private void chekGroupOver(DtzTable pdkTable) {
        if (pdkTable.checkChooseDone()) {
            Map<Integer, DtzPlayer> map = pdkTable.getSeatMap();
            map.clear();
            for (List<DtzPlayer> list : pdkTable.getGroupMap().values()) {
                for (DtzPlayer player1 : list) {
                    if (!map.containsKey(player1.getSeat())) {
                        map.put(player1.getSeat(), player1);
                    } else {
                        LogUtil.msg("table:" + pdkTable.getId() + "玩家:" + player1.getName() + " 的seat:" + player1.getSeat() +
                                " 已经存在于seatMap 这个相同的玩家是:" + map.get(player1.getSeat()).getName() +
                                " 他的seat应该是:" + pdkTable.randomSeat());
                    }
                }
            }
            LogUtil.msg("table:" + pdkTable.getId() + "座位选择已完成.");
            ArrayList<Integer> userids = new ArrayList<>();
            for (int i = 1; i <= pdkTable.getMaxPlayerCount(); i++) {
                if (pdkTable.getSeatMap().containsKey(i)) {
                    userids.add((int) pdkTable.getSeatMap().get(i).getUserId());
                }
            }
            ComMsg.ComRes.Builder build = SendMsgUtil.buildComRes(WebSocketMsgType.RES_SELECT_SEAT_DONE, userids);
            pdkTable.broadMsg(build.build());
//                int seat = pdkTable.getMasterSeat();
//                pdkTable.setNowDisCardSeat(seat - 1);
            pdkTable.isNew = false;
//                for (Player pl : pdkTable.getPlayerMap().values()) {
//                    DtzPlayer pdkPlayer = (DtzPlayer) pl;
//                    pdkPlayer.setRoundScore(0);
//                    pdkPlayer.setPoint(0);
//                    pdkPlayer.setDtzTotalPoint(0);
//                }
        } else {
            //选座时发送玩家当前座位准备信息
            pdkTable.sendPlayerStatusMsg();
        }
    }

    @Override
    public void setMsgTypeMap() {
    }
}
