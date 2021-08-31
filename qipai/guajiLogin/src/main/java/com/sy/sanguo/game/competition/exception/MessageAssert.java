package com.sy.sanguo.game.competition.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * 错误返回类
 * @author Guang.OuYang
 * @date 2020/5/20-16:32
 */
public class MessageAssert {
	private static final Logger LOGGER = LoggerFactory.getLogger(MessageAssert.class);

	public static void assertFalse(boolean w, MessageExceptionParam msg) {
		if (msg.isThrowException()) {
			if (w)
				throw new MessageException(msg.getCode(), Optional.ofNullable(msg.getMsg()).orElse("failed"));
		} else if (w){
			LOGGER.warn("MessageAssert|{}|{}", msg.getCode(), msg.getMsg());
		}
	}
}
