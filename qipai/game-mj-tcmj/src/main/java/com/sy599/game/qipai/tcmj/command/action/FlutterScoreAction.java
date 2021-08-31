package com.sy599.game.qipai.tcmj.command.action;

import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.qipai.tcmj.bean.TcMjPlayer;
import com.sy599.game.qipai.tcmj.bean.TcMjTable;
import com.sy599.game.qipai.tcmj.command.AbsCodeCommandExecutor;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

import java.util.ArrayList;
import java.util.stream.IntStream;

/**
 * 飘分
 * @author Guang.OuYang
 * @date 2019/9/2-16:58
 */
public class FlutterScoreAction extends AbsCodeCommandExecutor<TcMjTable, TcMjPlayer> {
    @Override
    public Integer actionCode() {
        return WebSocketMsgType.req_code_flutter_score;  //缺省操作
    }

    @Override
    public GlobalCommonIndex globalCommonIndex() {
        return GlobalCommonIndex.COMMAND_INDEX;
    }

    @Override
    public void execute(TcMjTable table, TcMjPlayer player, CarryMessage carryMessage) {
        LogUtil.printDebug("玩家{} 飘分...:{}", player.getName(), table.getGameModel().getFlutterScore());

        //先报听再做操作
        if (!table.getGameModel().signFlutterAllOver()) {
            ComMsg.ComReq req = carryMessage.parseFrom(ComMsg.ComReq.class);
            if(table.getGameModel().getFlutterScoreType()>10) { //飘分校验
                player.setFlutter(IntStream.of(1, 2, 3).anyMatch(v -> v == req.getParams(0)) ? req.getParams(0) : -1);
            }else{ //定飘校验
                player.setFlutter(req.getParams(0) != table.getGameModel().getFlutterScoreType() ? req.getParams(0) : -1);
            }
            player.changeExtend();
            ArrayList<Integer> objects = new ArrayList<>();
            objects.add((int) player.getUserId());
            objects.addAll(req.getParamsList());
            table.broadMsgToAll(ComMsg.ComRes.newBuilder().addAllParams(objects).setCode(WebSocketMsgType.req_code_flutter_score_res).build());

            table.getGameModel().decSignTingFlag();

            //全部选择完毕
            if (table.getGameModel().getFlutterScore() == 0) {
                table.getGameModel().setFlutterScore(-1);
                table.changeExtend();
//                table.sendDealMsg();

//                TcMjPlayer bankPlayer = (TcMjPlayer) table.getSeatMap().get(table.getLastWinSeat());
//                ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.req_com_kt_ask_dismajiang);// 推送庄家出牌
//                bankPlayer.writeSocket(com.build());

                table.checkDeal();
//                LogUtil.printDebug("推送庄家出牌...庄:{}",bankPlayer.getName());
            }
        }
    }

}
