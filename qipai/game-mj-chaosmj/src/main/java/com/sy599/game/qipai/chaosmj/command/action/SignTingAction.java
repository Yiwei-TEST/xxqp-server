package com.sy599.game.qipai.chaosmj.command.action;

import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.qipai.chaosmj.bean.ChaosMjPlayer;
import com.sy599.game.qipai.chaosmj.bean.ChaosMjTable;
import com.sy599.game.qipai.chaosmj.command.AbsCodeCommandExecutor;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

import java.util.ArrayList;

/**
 * 报听
 * @author Guang.OuYang
 * @date 2019/9/2-16:58
 */
public class SignTingAction extends AbsCodeCommandExecutor<ChaosMjTable, ChaosMjPlayer> {
    @Override
    public Integer actionCode() {
        return WebSocketMsgType.req_code_tjmj_baoting;  //缺省操作
    }

    @Override
    public GlobalCommonIndex globalCommonIndex() {
        return GlobalCommonIndex.COMMAND_INDEX;
    }

    @Override
    public void execute(ChaosMjTable table, ChaosMjPlayer player, CarryMessage carryMessage) {
        LogUtil.printDebug("玩家{} 报听...ting:{}",player.getName(),table.getGameModel().getSignTing());

        //先报听再做操作
        if (!table.getGameModel().signTingAllOver()) {
            ComMsg.ComReq req = carryMessage.parseFrom(ComMsg.ComReq.class);
            player.setSignTing(req.getParams(0) == 1 ? 2 : 3);
            player.changeExtend();
            ArrayList<Integer> objects = new ArrayList<>();
            objects.add((int) player.getUserId());
            objects.addAll(req.getParamsList());
            table.broadMsgToAll(ComMsg.ComRes.newBuilder().addAllParams(objects).setCode(WebSocketMsgType.req_code_tjmj_baoting_show).build());

            table.getGameModel().decSignTingFlag();

            //全部选择完毕
            if (table.getGameModel().getSignTing() == 0) {
                table.getGameModel().setSignTing(-1);
                table.changeExtend();
//                table.sendDealMsg();

                ChaosMjPlayer bankPlayer = (ChaosMjPlayer) table.getSeatMap().get(table.getLastWinSeat());
                ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.req_com_kt_ask_dismajiang);// 推送庄家出牌
                bankPlayer.writeSocket(com.build());

                LogUtil.printDebug("推送庄家出牌...庄:{}",bankPlayer.getName());
            }
        }
    }

}
