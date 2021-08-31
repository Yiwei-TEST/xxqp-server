package com.sy.sanguo.game.competition.exception;

import lombok.Builder;
import lombok.Data;

/**
 * @author Guang.OuYang
 * @date 2020/5/20-16:38
 */
@Data
@Builder
public class MessageExceptionParam {
	private int code;    //错误代号
	private String msg;    //消息内容

	private boolean throwException;	//抛出该错误,不做消息返回了
}
