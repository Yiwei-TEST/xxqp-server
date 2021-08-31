package com.sy599.game.qipai.wcphz.command.action;

import java.util.ArrayList;
import java.util.stream.IntStream;

import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.qipai.wcphz.bean.WcPaohuziPlayer;
import com.sy599.game.qipai.wcphz.bean.WcPaohuziTable;
import com.sy599.game.qipai.wcphz.command.AbsCodeCommandExecutor;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

/**
 * 飘分
 * @author Guang.OuYang
 * @date 2019/9/2-16:58
 */
public class FlutterScoreAction extends AbsCodeCommandExecutor<WcPaohuziTable, WcPaohuziPlayer> {
    @Override
    public Integer actionCode() {
        return WebSocketMsgType.req_code_flutter_score;  //缺省操作
    }

    @Override
    public GlobalCommonIndex globalCommonIndex() {
        return GlobalCommonIndex.COMMAND_INDEX;
    }

    @Override
    public void execute(WcPaohuziTable table, WcPaohuziPlayer player, CarryMessage carryMessage) {
        LogUtil.printDebug("玩家{} 飘分...:{}", player.getName(), table.getGameModel().getFlutterScore());

        //先报听再做操作
        if (!table.getGameModel().signFlutterAllOver()) {
            ComMsg.ComReq req = carryMessage.parseFrom(ComMsg.ComReq.class);
			if (table.getGameModel().getSpecialPlay().getFlutterScoreType() > 0) { //飘分校验
				player.setFlutter((IntStream.of(1, 2, 3, 4, 5).anyMatch(v -> v == req.getParams(0)) ? req.getParams(0) : 0));
			} else { //定飘校验
                player.setFlutter(req.getParams(0) != table.getGameModel().getSpecialPlay().getFlutterScoreType2() ? req.getParams(0) : 0);
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
                table.checkDeal();
//                LogUtil.printDebug("推送庄家出牌...庄:{}",bankPlayer.getName());
            }
        }
    }

}
