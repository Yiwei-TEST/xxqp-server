package com.sy599.game.util;

import com.sy599.game.common.constant.LangMsg;

/**
 * 语言
 * 
 * @author lc
 * 
 */
public class LangHelp {
	public static String getMsg(String langKey,Object... o) {
		return LangMsg.getMsg(langKey, o);
	}

}
