package com.sy599.game.qipai.qianfen.command.com;

import com.sy599.game.character.Player;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.qipai.qianfen.bean.QianfenPlayer;
import com.sy599.game.qipai.qianfen.bean.QianfenTable;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.netty.coder.MessageUnit;

import java.util.List;

public class CutCardsCommand extends BaseCommand<QianfenPlayer> {
    public static final int REQ_CUT_CARDS_COMMAND = 121;
    public static final int RES_CUT_CARDS_COMMAND = 1121;

    @Override
    public void execute(QianfenPlayer player, MessageUnit message) throws Exception {
        if(player==null)return;
        QianfenTable table = player.getPlayingTable(QianfenTable.class);
        if(table==null)return;

        if (table.getState() == SharedConstants.table_state.ready && table.isAllReady()){
            boolean fapai = false;
            synchronized (table.getLock()){
                if (table.getState() == SharedConstants.table_state.ready && table.isAllReady()){
                    int playedBureau = table.getPlayedBureau();
                    try {
                        //下游切牌
                        if ((playedBureau == 0 && (player.getUserId() == table.getMasterId() || table.getMasterId()<=0&&player.getSeat()==1)) ||(playedBureau > 0 && player.getResults().get(playedBureau - 1).get(4).intValue() == table.getMaxPlayerCount())) {
                            ComMsg.ComReq req = (ComMsg.ComReq) recognize(ComMsg.ComReq.class, message);
                            List<Integer> intList = req.getParamsList();
                            if (intList.size() >= 3) {
                                int process = intList.get(0);
                                int state = intList.get(1);//0正在切牌1切牌完成
                                int card = intList.get(2);

                                if (state == 1 && card > 0){
                                    table.setFanCard(card);
                                    fapai = true;
                                }

                                for (Player player1 : table.getPlayerMap().values()) {
                                    if (state == 1 || player1 != player) {
                                        player1.writeComMessage(RES_CUT_CARDS_COMMAND, (int) player.getUserId(), player.getSeat(), process, state, card);
                                    }
                                }
                            }
                        }
                    }catch (Exception e){
                        LogUtil.errorLog.error("Exception:"+e.getMessage(),e);
                    }
                }
            }
            if (fapai){
                table.ready();
                table.checkDeal();
                table.startNext();
            }
        }else{
            return;
        }
    }

    @Override
    public void setMsgTypeMap() {
        // TODO Auto-generated method stub

    }
}
