package com.sy.sanguo.game.competition.exception;

import lombok.Data;

/**
 * @author Guang.OuYang
 * @date 2020/5/20-16:33
 */
@Data
public class MessageException extends RuntimeException {

	private int code;
	private String msg;

	public MessageException(int code, String message) {
		super(message);
		this.code = code;
		this.msg = message;
	}


	//堆栈这部分数据不需要返回
	@Override
	public synchronized Throwable fillInStackTrace() {
		return this;
	}

}
