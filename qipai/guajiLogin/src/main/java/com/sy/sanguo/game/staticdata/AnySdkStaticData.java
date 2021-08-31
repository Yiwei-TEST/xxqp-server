package com.sy.sanguo.game.staticdata;

import java.util.HashMap;
import java.util.Map;

import com.sy.sanguo.game.bean.PfSdkConfig;

public class AnySdkStaticData {
	/** 益玩安卓平台 **/
	private static Map<String, PfSdkConfig> pfMap = new HashMap<String, PfSdkConfig>();
	static {
		// anysdk
		PfSdkConfig anysdk = new PfSdkConfig();
		anysdk.setAppKey("7HGO4K61M8N2D9LARSPU");
		anysdk.setPayKey("5B6D41405E190E7218C666F2CB7E2728");
		anysdk.setPf("anysdk");
		pfMap.put("anysdk", anysdk);
	}

	/**
	 * Pf是否在静态数据Map中
	 * 
	 * @param pf
	 * @return
	 */
	public static boolean isHasPf(String pf) {
		if (pfMap.containsKey(pf)) {
			return true;
		}
		return false;
	}

	/**
	 * 获取静态数据
	 * 
	 * @param pf
	 * @return
	 */
	public static PfSdkConfig getConfig(String pf) {
		if (pfMap.containsKey(pf)) {
			return pfMap.get(pf);
		}
		return null;
	}
}
