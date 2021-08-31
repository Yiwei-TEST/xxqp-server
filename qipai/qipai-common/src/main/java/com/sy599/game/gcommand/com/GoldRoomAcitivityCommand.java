package com.sy599.game.gcommand.com;

import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.util.LangHelp;
import com.sy599.game.websocket.netty.coder.MessageUnit;

import java.util.List;

public class GoldRoomAcitivityCommand  extends BaseCommand {
    private static final int queryRankList=101;
    private static final int getReward=102;
    /**
     * 金币场结算豆大于1000豆 看完视频 可获取奖励
     */
    private static final int getWatchAdsReward=104;
    private static final int get7xiRank=105;
    private static final int get7xiReward=106;
    @Override
    public void execute(Player player, MessageUnit message) throws Exception {
        {
            ComMsg.ComReq req = (ComMsg.ComReq) this.recognize(ComMsg.ComReq.class, message);
            List<Integer> lists = req.getParamsList();
            if (lists == null || lists.size() == 0) {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_3));
                return;
            }
            // 获得传递过来的操作指令
            int command = req.getParams(0);
            if(command==101 || command==102){
                return;
            }
            switch (command){
                case queryRankList:
                   // player.queryGoldRoomActivityRankList();//端午排行榜查询
                    player.writeErrMsg("活动已结束");
                    break;
                case getReward://端午排行榜获取奖励
                   // player.getReWard();
                    player.writeErrMsg("活动已结束!");
                    break;
                case getWatchAdsReward:
                    int roomids = req.getParams(1);
                    int beans = req.getParams(2);
                    player.getWatchAdsReWard(roomids,beans);
                    break;
                case get7xiRank:
                    player.queryGoldRoom7xiActivityRankList();
                    break;
                case get7xiReward:
                    player.getReWard7xiActivity();
                    break;
            }
        }
    }
}
