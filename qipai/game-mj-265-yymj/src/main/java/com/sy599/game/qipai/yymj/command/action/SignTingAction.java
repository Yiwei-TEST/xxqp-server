package com.sy599.game.qipai.yymj.command.action;

import com.sy599.game.character.Player;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.PlayCardResMsg;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.PlayMajiangRes;
import com.sy599.game.qipai.yymj.bean.YyMjPlayer;
import com.sy599.game.qipai.yymj.bean.YyMjTable;
import com.sy599.game.qipai.yymj.command.AbsCodeCommandExecutor;
import com.sy599.game.qipai.yymj.tool.MjResTool;
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
public class SignTingAction extends AbsCodeCommandExecutor<YyMjTable, YyMjPlayer> {
    @Override
    public Integer actionCode() {
        return WebSocketMsgType.req_code_tjmj_baoting;  //缺省操作
    }

    @Override
    public GlobalCommonIndex globalCommonIndex() {
        return GlobalCommonIndex.COMMAND_INDEX;
    }

    @Override
    public void execute(YyMjTable table, YyMjPlayer player, CarryMessage carryMessage) {
        LogUtil.printDebug("玩家{} 报听...ting:{}",player.getName(),table.getGameModel().getSignTing());

        //先报听再做操作
        if (!table.getGameModel().signTingAllOver()) {
            ComMsg.ComReq req = carryMessage.parseFrom(ComMsg.ComReq.class);
            player.setSignTing(req.getParams(0) == 1 ? 2 : 3);
            if(player.isSignTing()){
				player.setSignCanAutoDisCard(true);
			}
            player.changeExtend();
            ArrayList<Integer> objects = new ArrayList<>();
            objects.add((int) player.getUserId());
            objects.addAll(req.getParamsList());
            table.broadMsgToAll(ComMsg.ComRes.newBuilder().addAllParams(objects).setCode(WebSocketMsgType.req_code_tjmj_baoting_show).build());

			int signTing = table.getGameModel().decSignTingFlag();

			//全部选择完毕
            if (signTing == 0) {
                table.getGameModel().setSignTing(-1);
                table.changeExtend();
//                table.sendDealMsg();
				table.checkMo();
				table.btRoomMsg(WebSocketMsgType.req_com_kt_ask_dismajiang);

				PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
				table.buildPlayRes(builder, player, table.getDisEventAction(), table.getNowDisCardIds());
				table.sendDisMajiangAction(builder);

//				Iterator<Player> iterator = table.getSeatMap().values().iterator();
//				while (iterator.hasNext()) {
//					YyMjPlayer p =  (YyMjPlayer) iterator.next();
//					List<Integer> actionSeatMap = table.getActionSeatMap().get(p.getSeat());
////					List<Integer> actionSeatMap = p.checkMoCard(null, false);
////					if(!CollectionUtils.isEmpty(actionSeatMap) && actionSeatMap.contains(1))
////						table.addActionSeat(p.getSeat(), actionSeatMap);
//					if(!CollectionUtils.isEmpty(actionSeatMap)){
//
//
//						PlayCardResMsg.PlayMajiangRes.Builder disBuilder = PlayCardResMsg.PlayMajiangRes.newBuilder();
//						MjResTool.buildPlayRes(disBuilder, player, 0, null);
//						disBuilder.addAllSelfAct(actionSeatMap);
//						p.writeSocket(disBuilder.build());
//					}
//				}

//				YyMjPlayer bankPlayer = (YyMjPlayer) table.getSeatMap().get(table.getLastWinSeat());
//                ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.req_com_kt_ask_dismajiang);// 推送庄家出牌
//                bankPlayer.writeSocket(com.build());

                LogUtil.printDebug("推送可以出牌...庄:{}");
            }
        }
    }

}
