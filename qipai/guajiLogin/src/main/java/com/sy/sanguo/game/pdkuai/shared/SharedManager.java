package com.sy.sanguo.game.pdkuai.shared;

import java.util.HashMap;
import java.util.Map;

import com.sy599.sanguo.util.TimeUtil;

public class SharedManager {
	private static SharedManager _inst = new SharedManager();
	private long refreshTime;
	private Map<String, String> shareMap = new HashMap<String, String>();

	public static SharedManager getInstance() {
		return _inst;
	}

	public Map<String, String> getShareMap() {
		return shareMap;
	}

	public void refreshShare(Map<String, String> shareMap) {
		this.shareMap = shareMap;
		this.refreshTime = TimeUtil.currentTimeMillis();
	}

	public long getRefreshTime() {
		return refreshTime;
	}

}
