package com.sy599.game.util;

import com.sy599.game.websocket.MyWebSocket;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.atomic.LongAdder;

/**
 * 消息发送
 */
public final class MessageTransmits {

    private static final LongAdder MSG_COUNT = new LongAdder();
    private static final long INIT_TIME = System.currentTimeMillis();
    private static boolean isOn = false;

    private static void init() {
        synchronized (MessageTransmits.class) {
            if (!isOn) {
                isOn = true;
                LogUtil.msgLog.info("MessageTransmits start");
            }
        }
    }

    /**
     * 消息数+1
     */
    public static void add() {
        MSG_COUNT.add(1L);
    }

    public static final void message(final MyWebSocket webSocket) {
        if (!webSocket.getCtx().isRemoved()) {
            if (webSocket.getState().compareAndSet(true, false)) {
                Message msg = webSocket.getMsgQueue().poll();
                if (msg != null) {
                    ChannelPromise promise = webSocket.getCtx().newPromise();
                    promise.addListener(new GenericFutureListener<Future<? super Void>>() {
                        @Override
                        public void operationComplete(Future<? super Void> future) throws Exception {
                            add();
                            if (webSocket.getState().compareAndSet(false, true)) {
                                if (webSocket.getMsgQueue().size()>0){
                                    message(webSocket);
                                }
                            }
                        }
                    });

                    if (msg.flush()) {
                        webSocket.getCtx().writeAndFlush(webSocket.getChannelMark() == 1 ? new BinaryWebSocketFrame(Unpooled.wrappedBuffer(msg.msg())) : Unpooled.wrappedBuffer(msg.msg()), promise);
                    } else {
                        webSocket.getCtx().write(webSocket.getChannelMark() == 1 ? new BinaryWebSocketFrame(Unpooled.wrappedBuffer(msg.msg())) : Unpooled.wrappedBuffer(msg.msg()), promise);
                    }
                } else {
                    webSocket.getState().compareAndSet(false, true);
                }
            }
        }
    }

    public static void shutDown() {
        synchronized (MessageTransmits.class) {
            if (isOn) {
                isOn = false;

                long endTime = System.currentTimeMillis();
                long msgCount = MSG_COUNT.sum();
                long time = (endTime - INIT_TIME) / 1000;
                if (time <= 0) {
                    time = 1L;
                }
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(INIT_TIME);
                String time1 = sdf.format(calendar.getTime());
                calendar.setTimeInMillis(endTime);
                String time2 = sdf.format(calendar.getTime());

                LogUtil.msgLog.info("MessageTransmits shutDown:time(s)={},msgCount={},avg(s)={},startTime={},endTime={}", time, msgCount, msgCount / time, time1, time2);
            }
        }
    }

    public static class Message {
        private final byte[] msg;
        private final boolean flush;

        static {
            init();
        }

        public Message(byte[] msg, boolean flush) {
            this.msg = msg;
            this.flush = flush;
        }

        public byte[] msg() {
            return msg;
        }

        public boolean flush() {
            return flush;
        }
    }

}
