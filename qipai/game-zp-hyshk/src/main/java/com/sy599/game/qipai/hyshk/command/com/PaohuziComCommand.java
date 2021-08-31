package com.sy599.game.qipai.hyshk.command.com;

import com.sy599.game.character.Player;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.qipai.hyshk.been.HyshkPlayer;
import com.sy599.game.qipai.hyshk.been.HyshkTable;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PaohuziComCommand extends BaseCommand<HyshkPlayer> {
	@Override
	public void execute(HyshkPlayer player, MessageUnit message) throws Exception {
		ComReq req = (ComReq) this.recognize(ComReq.class, message);

		HyshkTable table = player.getPlayingTable(HyshkTable.class);
		if (table == null) {
			return;
		}
		synchronized (table) {
            if (req.getCode() == 131) {
                player.setAutoPlay(false, table);
                player.setLastOperateTime(System.currentTimeMillis());
            } else if (req.getCode() == WebSocketMsgType.req_com_fangzhao) {
                int pai = req.getParams(0);
                LogUtil.msgLog.info("----tableId:" + table.getId() + "---userName:" + player.getName() + "------->已确认放招:" + pai);
                player.setFangZhao(1);
                player.setAutoPlay(false,table);
                player.setLastOperateTime(System.currentTimeMillis());
                List<Integer> cards = new ArrayList<>(Arrays.asList(pai));
                table.play(player, cards, 0);

                // player.writeComMessage(WebSocketMsgType.res_code_phz_fangzhao, player.getUserId() + "", 1 + "");
                for (Player playerTemp : table.getSeatMap().values()) {
                    playerTemp.writeComMessage(WebSocketMsgType.res_code_phz_fangzhao, player.getUserId() + "", 1 + "");
                }
            }
            else if (req.getCode() == WebSocketMsgType.req_code_hyshk_chui) {
                player.setAutoPlay(false, table);
                player.setLastOperateTime(System.currentTimeMillis());
                table.chui(player, req.getParams(0));
            }
        }

	}

	@Override
	public void setMsgTypeMap() {

	}

}
