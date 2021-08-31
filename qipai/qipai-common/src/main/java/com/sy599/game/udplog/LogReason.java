package com.sy599.game.udplog;

import java.lang.annotation.*;

/**
 * 日志REASON注解，主要用于以后web系统解析日志原因
 * 
 * @author taohuiliang
 * @date 2012-11-5
 * @version v1.0
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface LogReason {
	/**
	 * @return String 日志reason的描述信息
	 */
	public String desc() default "no desc";
}
