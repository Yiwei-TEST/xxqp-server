package com.sy599.game.manager;

import com.sy599.game.util.LogUtil;

public class DBManager {

	public static void saveDB(boolean asyn) {
		SystemCommonInfoManager.getInstance().saveDB(asyn);
		TableManager.getInstance().saveDB(asyn);
		PlayerManager.getInstance().saveDB(asyn);
	}

	public static void loadDataFromDB() {
		SystemCommonInfoManager.getInstance().initData();
		LogUtil.msgLog.info("loadDataFromDB -SystemCommonInfoManager- finished");
//		TableManager.getInstance().initTablePlayType();
//		PlayerManager.getInstance().loadFromDB();
		TableManager.getInstance().initData();
		LogUtil.msgLog.info("loadDataFromDB -TableManager- finished");
		GotyeChatManager.getInstance().loadFromDB();
		LogUtil.msgLog.info("loadFromDB -GotyeChatManager- finished");
		MarqueeManager.getInstance().initData();
		LogUtil.msgLog.info("loadDataFromDB -MarqueeManager- finished");
	}

}
