
package com.sy599.game.websocket.netty.coder;

import com.google.protobuf.GeneratedMessage;

/**
 * Created by lz
 */
public class MessageUnit {
    public final static int PACKAGE_HEAD_LENGTH = 8;
    public final static int MAX_PACKAGE_LENGTH = Short.MAX_VALUE;

    short length;
    int checkCode;
    short msgType;
    private boolean notCheckCode;
    byte[] content;
    GeneratedMessage message;

    public MessageUnit() {
    }

    public short getLength() {
        return this.length;
    }

    public void setLength(short length) {
        this.length = length;
    }

    public int getCheckCode() {
        return this.checkCode;
    }

    public void setCheckCode(int checkCode) {
        this.checkCode = checkCode;
    }

    public short getMsgType() {
        return this.msgType;
    }

    public void setMsgType(short msgType) {
        this.msgType = msgType;
    }

    public byte[] getContent() {
        return this.content;
    }

    public void appendContent(byte[] appendContent) {
        if (this.content == null) {
            this.content = appendContent;
        } else {
            byte[] result = new byte[this.content.length + appendContent.length];
            System.arraycopy(this.content, 0, result, 0, this.content.length);
            System.arraycopy(appendContent, 0, result, this.content.length, appendContent.length);
            this.content = result;
        }

    }

    public void setMessage(GeneratedMessage message) {
        this.message = message;
    }

    public int needLength() {
        if (content == null){
            return this.length;
        }else{
            return this.length - content.length;
        }
    }

    public GeneratedMessage getMessage() {
        return this.message;
    }

    public String getMsgLog() {
        return this.message != null ? this.message.toString() : null;
    }

    public boolean isNotCheckCode() {
        return this.notCheckCode;
    }

    public void setNotCheckCode(boolean notCheckCode) {
        this.notCheckCode = notCheckCode;
    }

    /**
     * 检查消息是否接收完成
     *
     * @return
     */
    public boolean complete() {
        return (this.content == null||this.content.length==0) ? false : (this.length == this.content.length);
    }
    /**
     * 已接收的数据长度
     * @return
     */
    public int currentLength(){
        return this.content == null ? 0 : this.content.length;
    }

    public String msgLog() {
        if (message != null) {
            return message.toString();
        }
        return null;
    }

    public String simple() {
        return new StringBuilder("type:").append(msgType).append(",checkCode:").append(checkCode).append(",length:").append(length).toString();
    }

}
