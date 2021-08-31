package com.sy599.game.websocket.netty.coder;

import com.google.protobuf.GeneratedMessage;
import com.sy599.game.util.Commands;
import com.sy599.game.util.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * 消息编码解码器
 * Created by lz.
 */
public final class MessageCoder {
    private static final Logger LOGGER = LoggerFactory.getLogger("msg");

    /**
     * 解码
     *
     * @param bytes
     * @param msgUnit
     * @return
     * @see MessageUnit#complete()
     */
    public static MessageUnit decode(byte[] bytes, MessageUnit msgUnit) {
        int len;
        if (bytes == null || (len = bytes.length) == 0) {
            LogUtil.errorLog.warn("package empty, decode fail:bytes length=0");
            return null;
        }

        ByteBuffer byteBuff = ByteBuffer.wrap(bytes);
        if (msgUnit == null) {
            if (len < MessageUnit.PACKAGE_HEAD_LENGTH) {
                LogUtil.errorLog.warn("package length too short, decode fail:bytes length={}", len);
                return null;
            }

            short length = byteBuff.getShort();
            int checkCode = byteBuff.getInt();
            short msgType = byteBuff.getShort();

            if (length < 0) {
                throw new RuntimeException("decodeError|lengthTooShort|" + len + "|" + msgType + "|" + length);
            }

            if (!Commands.contains(msgType)) {
                throw new RuntimeException("decodeError|unknownMsgType|" + len + "|" + msgType + "|" + length);
            }

            if (len > MessageUnit.MAX_PACKAGE_LENGTH || length > MessageUnit.MAX_PACKAGE_LENGTH) {
                throw new RuntimeException("decodeError|lengthTooLong|" + len + "|" + msgType + "|" + length);
            }

            msgUnit = new MessageUnit();
            msgUnit.setLength(length);
            msgUnit.setCheckCode(checkCode);
            msgUnit.setMsgType(msgType);
        }

        byte[] content = new byte[msgUnit.needLength() > byteBuff.remaining() ? byteBuff.remaining() : msgUnit.needLength()];
        byteBuff.get(content);
        msgUnit.appendContent(content);
        return msgUnit;
    }

    /**
     * 编码（如果返回list的大小大于1则需要多次发送）<br/>
     * 消息最大长度为 32767
     *
     * @param msgUnit msgType、checkCode和message需要赋值
     * @return
     */
    public static List<byte[]> encode(MessageUnit msgUnit) {
        GeneratedMessage msg = msgUnit.getMessage();

        byte[] data = msgUnit.getContent();
        String className = null;
            if (data == null || data.length == 0) {
                if (msg != null && msg.isInitialized()) {
                    data = msg.toByteArray();
                    className = msg.getClass().getName();
                }
            } else {
            className = "bytes";
        }

        try {
            if (msgUnit.getMsgType() <= 0) {
                throw new RuntimeException("encodeError|unknownMessageType|" + msgUnit.getMsgType() + "|" + className);
            }

            int contentLength = data != null ? data.length : 0;
            if (contentLength > Short.MAX_VALUE) {
                throw new RuntimeException("encodeError|packageLengthTooLong|" + contentLength + "|" + msgUnit.getMsgType() + "|" + className);
            }

            int length = contentLength + MessageUnit.PACKAGE_HEAD_LENGTH;
            ByteBuffer buf;
            if (length > MessageUnit.MAX_PACKAGE_LENGTH) {
                buf = ByteBuffer.allocate(MessageUnit.MAX_PACKAGE_LENGTH);
            } else {
                buf = ByteBuffer.allocate(length);
            }

            buf.putShort((short) contentLength);
            buf.putInt(msgUnit.getCheckCode());
            buf.putShort(msgUnit.getMsgType());

            List<byte[]> list = new ArrayList<>();
            if (contentLength > 0 && contentLength > buf.remaining()) {
                ByteBuffer buffer = ByteBuffer.wrap(data);

                while (buffer.hasRemaining()) {
                    if (buf.remaining() == 0) {
                        buf.flip();

                        list.add(buf.array());

                        buf = ByteBuffer.allocate(MessageUnit.MAX_PACKAGE_LENGTH);
                    }

                    byte b = buffer.get();
                    buf.put(b);
                    if (!buffer.hasRemaining()) {
                        buf.flip();
                        list.add(buf.array());
                    }
                }
            } else {
                if (data != null) {
                    buf.put(data);
                }
                buf.flip();
                list.add(buf.array());
            }

            return list;
        } catch (Exception e) {
            LOGGER.error("message:length=" + data.length + ",type=" + msgUnit.getMsgType() + ",class=" + className + ",error=" + e.getMessage(), e);
            throw new RuntimeException("encodeError|exception|" + data.length + "|" + msgUnit.getMsgType() + "|" + className + "|" + e.getMessage(), e);
        }
    }
}
