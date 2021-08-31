package com.sy599.game.gcommand.com;

import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.util.LangHelp;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

import java.util.List;

public class ComMsgCommand extends BaseCommand {
    private final static int getGoldenBeans=1;
    private final static int getTWinRwardsInfo=2;
    private final static int getwinReward=3;

    /**赠钻权限查询 */
    private final static int getSendDiamondsPermission=4;
    /**获取赠钻记录 */
    private final static int getSendDiamondsRecord=5;
    /**赠送钻石 */
    private final static int sendDiamonds=6;

    @Override
    public void execute(Player player, MessageUnit message) throws Exception {
        ComMsg.ComReq req = (ComMsg.ComReq) this.recognize(ComMsg.ComReq.class, message);
        List<Integer> lists = req.getParamsList();
        if (lists == null || lists.size() == 0) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_3));
            return;
        }
        
        // 获得传递过来的操作指令
        int command = req.getParams(0);
        LogUtil.msgLog.info("ComMsgCommand|"+command+"|"+player.getUserId());
        switch (command){
            case getGoldenBeans:
                player.selectGoldenBeans();

                break;
            case getTWinRwardsInfo:
                player.getWinReardInfo();
                break;
            case getwinReward:
            	//int id = req.getParams(1);
                player.getWinReard();
                break;
            //case getSendDiamondsPermission:
                //player.getSendDiamondsPermission();
            case getSendDiamondsRecord:
                int pageno = req.getParams(1);
                long pagesize =10;
                long acpid =req.getParams(2);
                String begintime =req.getStrParams(0);
                String endtime =req.getStrParams(1);
                player.getSendDiamondsRecord(player.getUserId(),pageno,pagesize,acpid,begintime,endtime);
                break;
            case sendDiamonds:
                long acpUserid = req.getParams(1);
                long diamondsNum =req.getParams(2);
               player.sendDiamonds(player.getUserId(),acpUserid,diamondsNum);
                break;
        }
        
        
        
        
        
        
        
        
    }

    @Override
    public void setMsgTypeMap() {

    }

}