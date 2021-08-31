package com.sy599.game.websocket;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.GeneratedMessage;
import com.sy599.game.GameServerConfig;
import com.sy599.game.character.Player;
import com.sy599.game.db.bean.RegInfo;
import com.sy599.game.manager.MsgManager;
import com.sy599.game.msg.serverPacket.ComMsg.ComRes;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.MessageTransmits;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageCoder;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class MyWebSocket {
    private LinkedBlockingQueue<MessageTransmits.Message> msgQueue = new LinkedBlockingQueue<>();
    private AtomicBoolean state = new AtomicBoolean(true);
    private ChannelHandlerContext ctx;
    private Player player;
    private int channelMark = 1;
    private volatile long sendMessageTime;
    private long num;
    /*** 账号冲突 */
    private boolean loginError;
    private volatile Date dateTime = new Date();
    private volatile boolean loginSuccess = false;

    public boolean isLoginSuccess() {
        return loginSuccess;
    }

    public void setLoginSuccess(boolean loginSuccess) {
        this.loginSuccess = loginSuccess;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public AtomicBoolean getState() {
        return state;
    }

    public LinkedBlockingQueue<MessageTransmits.Message> getMsgQueue() {
        return msgQueue;
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public void stCtx(ChannelHandlerContext session) {
        this.ctx = session;
    }


    public int getChannelMark() {
        return channelMark;
    }

    public void setChannelMark(int channelMark) {
        this.channelMark = channelMark;
    }

    /**
     * 发送msg
     *
     * @param message
     */
    public void send(GeneratedMessage message) {
        if (message != null) {
            MessageUnit messageUnit = new MessageUnit();
            messageUnit.setMessage(message);
            send(messageUnit);
        }
    }

    /**
     * 发送msg
     *
     * @param message
     */
    public void send(MessageUnit message) {
        if (message != null) {
            try {
                if (player != null) {
                    if (message.isNotCheckCode()/** || (message.getMessage() instanceof ComRes)**/) {
                        message.setCheckCode(player.getMsgCheckCode());
                    } else {
                        message.setCheckCode(player.incrementAndGetMsgCheckCode());
                    }
                }
                if (send(ctx, message)) {
                    sendMessageTime = System.currentTimeMillis();
                }
//				LogUtil.msgLog.info("response>>>MsgType:"+ MsgManager.getInstance().getMsgType(message.getMessage().getClass())+" MsgLog:"+message.getMsgLog()+" MsgLength:"+message.getLength()+" CheckCode:"+message.getCheckCode()+message.isNotCheckCode()
//						+" ClassName:"+message.getMessage().getClass().getName());
            } catch (Exception e) {
                LogUtil.e("websocket send :" + e.getMessage(), e);
            }
        } /*else {
			LogUtil.msgLog.info("session is null or session is not open--> send msg error");
		}
*/
    }

    /**
     * 发送信息
     *
     * @param type
     * @param content
     */
    public void send(int type, String content) {
        try {
            ComRes.Builder com = ComRes.newBuilder();
            com.setCode(type);
            List<String> list = new ArrayList<String>();
            list.add(content);
            com.addAllStrParams(list);
            send(com.build());
        } catch (Exception e) {
            LogUtil.e("websocket send", e);
        }
    }

    /**
     * 发送信息
     *
     * @param type
     * @param content
     */
    public void send(int type, int content) {
        try {
            ComRes.Builder com = ComRes.newBuilder();
            com.setCode(type);
            List<Integer> list = new ArrayList<Integer>();
            list.add(content);
            com.addAllParams(list);
            send(com.build());
        } catch (Exception e) {
            LogUtil.e("websocket send", e);
        }
    }

    public long getNum() {
        return num;
    }

    public void setNum(long num) {
        this.num = num;
    }

    public boolean isLoginError() {
        return loginError;
    }

    /**
     * 账号冲突
     */
    public void accountConflict(Object obj) {
        String channelId = getCtx() != null ? getCtx().channel().id().asShortText() : "";
        if (obj instanceof Player) {
            Player player1 = (Player) obj;
            LogUtil.monitorLog.error("login|MyWebSocket|accountConflict|1|" + player1.getUserId() + "|" + channelId + "|" + player1.getPlayingTableId() + "|" + player1.getSeat() + "|" + player1.getEnterServer() + "|" + GameServerConfig.SERVER_ID);
        } else if (obj instanceof RegInfo) {
            RegInfo player1 = (RegInfo) obj;
            LogUtil.monitorLog.error("login|MyWebSocket|accountConflict|2|" + player1.getUserId() + "|" + channelId + "|" + player1.getPlayingTableId() + "|" + player1.getEnterServer() + "|" + GameServerConfig.SERVER_ID);
        } else {
            LogUtil.monitorLog.error("login|MyWebSocket|accountConflict|3|" + JSON.toJSONString(obj) + "|" + channelId + "|" + GameServerConfig.SERVER_ID);
        }
        ComRes.Builder build = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_err, WebSocketMsgType.sc_code_err_login, channelId);
        send(build.build());
    }
    /**
     * 发送通用消息
     *
     * @param code
     * @param params
     */
    public void sendComMessage(int code, Object... params) {
        ComRes.Builder res = SendMsgUtil.buildComRes(code, params);
        send(res.build());
    }

    public void setLoginError(boolean loginError) {
        this.loginError = loginError;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public long getSendMessageTime() {
        return sendMessageTime;
    }

    public void close(boolean delay) {
        if (this.ctx != null) {
//			if (delay){
//				WebSocketManager.delayCloseSessionMap.put(session,System.currentTimeMillis());
//			}else{
            ctx.close();
//			}
        }
    }

    public void close() {
        close(true);
    }


    /**
     * 消息发送
     *
     * @param ctx
     * @param messageUnit
     */
    private final boolean send(ChannelHandlerContext ctx, MessageUnit messageUnit) throws Exception {
        if (ctx != null && !ctx.isRemoved()) {
            if (messageUnit.getMsgType() <= 0) {
                byte[] data = messageUnit.getContent();
                if ((data == null || data.length == 0) && messageUnit.getMessage() != null) {
                    messageUnit.setMsgType(MsgManager.getInstance().getMsgType(messageUnit.getMessage().getClass()));
                }
            }

//            LogUtil.msgLog.info("send msg:channelId={},userId={},msgType={},code={},checkCode={}", ctx.channel().id().asShortText(), player != null ? player.getUserId() : "unknown", messageUnit.getMsgType(), (messageUnit.getMessage() instanceof ComRes) ? ((ComRes) messageUnit.getMessage()).getCode() : "unknown", messageUnit.getCheckCode());

            List<byte[]> list = MessageCoder.encode(messageUnit);
            int len;
            if (list != null && (len = list.size() - 1) >= 0) {
                if (len == 0) {
                    return send(ctx, list.get(len), true);
                } else {
                    for (int i = 0; i < len; i++) {
                        send(ctx, list.get(i), false);
                    }
                    return send(ctx, list.get(len), true);
                }
            }
        }
        return false;
    }

    /**
     * 消息发送
     *
     * @param ctx
     * @param msg
     */
    private final boolean send(ChannelHandlerContext ctx, byte[] msg, boolean flush) throws Exception {
        if (ctx == null || ctx.isRemoved()) {
            return false;
        }
        msgQueue.add(new MessageTransmits.Message(msg, flush));

        MessageTransmits.message(this);

        return true;
    }

}
