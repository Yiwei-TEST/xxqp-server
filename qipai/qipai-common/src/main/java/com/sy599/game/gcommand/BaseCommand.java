package com.sy599.game.gcommand;

import com.google.protobuf.GeneratedMessage;
import com.sy599.game.character.Player;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.PlayCardReqMsg;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import io.netty.channel.ChannelHandlerContext;

import java.lang.reflect.Method;
import java.util.HashMap;

public abstract class BaseCommand<T extends Player>{
	protected ChannelHandlerContext ctx;
	protected T player;
	protected HashMap<Class<?>, Short> msgTypeMap = new HashMap<>();

	public BaseCommand() {
		setMsgTypeMap();
	}

	public void setPlayer(T player) {
		if (ctx == null && player != null){
			ctx = player.getMyWebSocket().getCtx();
		}
		this.player = player;
	}

	public GeneratedMessage recognize(Class<? extends GeneratedMessage> cl, MessageUnit message) {
		GeneratedMessage msg = null;
		try {
			if (cl == ComMsg.ComReq.class){
				msg = ComMsg.ComReq.parseFrom(message.getContent());
			}else if(cl == PlayCardReqMsg.PlayCardReq.class){
				msg = PlayCardReqMsg.PlayCardReq.parseFrom(message.getContent());
			}else if(cl == PlayCardReqMsg.BJPlayCardReq.class){
				msg = PlayCardReqMsg.BJPlayCardReq.parseFrom(message.getContent());
			}else{
				Method method = cl.getMethod("parseFrom", byte[].class);
				msg = (GeneratedMessage) method.invoke(null, message.getContent());
			}
		} catch (Exception e) {
			LogUtil.e("#MessageRecognizer.recognize.err:"+e.getMessage(), e);
		}
		if (player != null){
			player.setRecMsg(msg);
		}
		message.setMessage(msg);
		return msg;
	}

	public ChannelHandlerContext getCtx() {
		return ctx;
	}

	public void setCtx(ChannelHandlerContext ctx) {
		this.ctx = ctx;
	}

	public abstract void execute(T player, MessageUnit message) throws Exception;

	public void execute(MessageUnit message) throws Exception{
		execute(player,message);
	}

	/**
	 * 允许player为空
	 * @return
	 */
	public boolean allowPlayerIsNull(){
		return false;
	}

	public void setMsgTypeMap(){}

	public HashMap<Class<?>, Short> getMsgTypeMap() {
		return msgTypeMap;
	}
}
