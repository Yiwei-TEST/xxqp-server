package com.sy599.game.qipai.tjmj.command.action;

import com.google.protobuf.GeneratedMessage;
import com.sy599.game.character.Player;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.PlayCardResMsg;
import com.sy599.game.qipai.tjmj.bean.TjMjPlayer;
import com.sy599.game.qipai.tjmj.bean.TjMjTable;
import com.sy599.game.qipai.tjmj.command.AbsCodeCommandExecutor;
import com.sy599.game.qipai.tjmj.tool.MjResTool;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 报听
 * @author Guang.OuYang
 * @date 2019/9/2-16:58
 */
public class SignTingAction extends AbsCodeCommandExecutor<TjMjTable, TjMjPlayer> {
    @Override
    public Integer actionCode() {
        return WebSocketMsgType.req_code_tjmj_baoting;  //缺省操作
    }

    @Override
    public GlobalCommonIndex globalCommonIndex() {
        return GlobalCommonIndex.COMMAND_INDEX;
    }

    @Override
    public void execute(TjMjTable table, TjMjPlayer player, CarryMessage carryMessage) {
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

                TjMjPlayer bankPlayer = (TjMjPlayer) table.getSeatMap().get(table.getLastWinSeat());

                List<Integer> oldActionSeatMap = table.getActionSeatMap().get(bankPlayer.getSeat());
                List<Integer> actionSeatMap = bankPlayer.checkMoCard(null, false);

                if (!CollectionUtils.isEmpty(actionSeatMap)) {
                    //这里如果之前过掉了自摸, 则二次不给自摸
                    actionSeatMap.set(17, CollectionUtils.isEmpty(oldActionSeatMap) ? 0 : oldActionSeatMap.get(17));
                }

                if(!CollectionUtils.isEmpty(actionSeatMap) && actionSeatMap.contains(1))
                    table.addActionSeat(bankPlayer.getSeat(), actionSeatMap);

                PlayCardResMsg.PlayMajiangRes.Builder disBuilder = PlayCardResMsg.PlayMajiangRes.newBuilder();
                MjResTool.buildPlayRes(disBuilder, player, 0, null);
                disBuilder.addAllSelfAct(actionSeatMap);
                bankPlayer.writeSocket(disBuilder.build());

                ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.req_com_kt_ask_dismajiang);// 推送庄家出牌
                bankPlayer.writeSocket(com.build());


                LogUtil.printDebug("推送庄家出牌...庄:{}",bankPlayer.getName());
            }
        }
    }

}
