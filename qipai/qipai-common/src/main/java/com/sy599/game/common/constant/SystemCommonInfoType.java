package com.sy599.game.common.constant;

/**
 * 相关存储数据类型
 * @author taohuiliang
 * @date 2013-3-13
 * @version v1.0
 */
public enum SystemCommonInfoType {
	paomadeng,
	sqlupdate,
	gotyeToken,
	isConsumeCards,
	isConsumeGold,
	isStartGame,
	gameVersion,
	gameShutDownVersion,
	gameStartUpTime,
	gotyeKey,
	gotyeSecret,
	goldGive("5000"),
	mangguoResetJiFenTime,
	isMangGuoJiFenReset,
	;

	private final String content;

	SystemCommonInfoType(){
		this.content = "";
	}

	SystemCommonInfoType(String content){
		this.content = content;
	}

	public String getContent() {
		return content;
	}
}
