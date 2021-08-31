package com.sy599.game.qipai.xxgaohuzi.command.action;

import com.google.protobuf.GeneratedMessage;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.PlayCardReqMsg;
import com.sy599.game.qipai.xxgaohuzi.bean.PaohzDisAction;
import com.sy599.game.qipai.xxgaohuzi.bean.XxGaohuziPlayer;
import com.sy599.game.qipai.xxgaohuzi.bean.XxGaohuziTable;
import com.sy599.game.qipai.xxgaohuzi.command.AbsCodeCommandExecutor;
import com.sy599.game.qipai.xxgaohuzi.constant.PaohzCard;
import com.sy599.game.util.DataMapUtil;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 6.3 打坨：建房三选一选项：不打坨/坨对坨3番/坨对坨4番
 * <p>
 * 6.4 建房选择不打坨：全部准备直接发牌开始没有打坨/不打坨选项。
 * <p>
 * 6.5 建房时选择坨对坨3番：全部准备后出现选项打坨/不打坨，情况1：两家选择不打坨，结算时两家胡息相减后得到的积分不变；情况2：一家选择打坨一家选择不打坨，结算积分*2。情况3：两家都选择打坨，结算积分*3。
 * <p>
 * 6.6 建房时选择坨对坨4番：全部准备后出现选项打坨/不打坨，情况1：两家选择不打坨，结算时两家胡息相减后得到的积分不变；情况2：一家选择打坨一家选择不打坨，结算积分*2。情况3：两家都选择打坨，结算积分*4。
 * @author Guang.OuYang
 * @date 2019/9/2-16:58
 */
public class GhzTuoAction extends AbsCodeCommandExecutor<XxGaohuziTable, XxGaohuziPlayer> {
    @Override
    public Integer actionCode() {
        return WebSocketMsgType.REQ_XXGHZ_TUO;
    }

    @Override
    public GlobalCommonIndex globalCommonIndex() {
        return GlobalCommonIndex.COMMAND_INDEX;
    }

    @Override
    public void execute(XxGaohuziTable table, XxGaohuziPlayer player, CarryMessage carryMessage) {
        ComMsg.ComReq comMsg = carryMessage.parseFrom(ComMsg.ComReq.class);
        //1坨, 2不坨
        int params = comMsg.getParams(0);
        LogUtil.printDebug("Player{},开始选坨:{}",player.getUserId(),params);
        //默认2不坨
        player.setTuo(params != 1 && params != 2 ? 2 : params);
        //同步到其他玩家,
        //XXX 这里long强转当user数量上来之后会出现异常
        table.broadcast(actionCode(), (int) player.getUserId(), params);
        //检查发牌
        player.getPlayingTable().checkDeal(0);

        LogUtil.printDebug("Player{},选坨完毕:{}",player.getUserId(),params);

    }

}
