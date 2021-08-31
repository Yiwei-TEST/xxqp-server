package com.sy.sanguo.game.staticdata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.sy.sanguo.common.util.JacksonUtil;
import com.sy.sanguo.game.bean.PfSdkConfig;

/**
 * 益玩静态数据(同一个平台适配多个游戏)
 * 
 * @author lc
 * 
 */
public class YiwanStaticData {
	/** 益玩安卓平台 **/
	private static Map<String, PfSdkConfig> yiwanMap = new HashMap<String, PfSdkConfig>();
	static {
		// 屠龙令
		PfSdkConfig yiwantl = new PfSdkConfig();
		yiwantl.setAppId("10300");
		yiwantl.setAppKey("LvNeTqbBmZkwf2X0");
		yiwantl.setPf("yiwantl");
		yiwanMap.put("yiwantl", yiwantl);
		// 芈月传
		PfSdkConfig ywmyz = new PfSdkConfig();
		ywmyz.setAppId("10313");
		ywmyz.setAppKey("KCTxSI7jwgeaPVUQ");
		ywmyz.setLog(true);
		ywmyz.setPf("ywmyz");
		ywmyz.setReyunAppId("928b794296d00e9300d0ae2a038a6b8d");
		yiwanMap.put("ywmyz", ywmyz);
		// 奇迹
		PfSdkConfig yiwanqj = new PfSdkConfig();
		yiwanqj.setAppId("10374");
		yiwanqj.setAppKey("ZfRtYmpe2HsJ7hTN");
		yiwanqj.setLog(true);
		yiwanqj.setPf("yiwanqj");
		yiwanqj.setReyunAppId("a57c611fdd97d438cc177152db026c99");
		yiwanMap.put("yiwanqj", yiwanqj);
		// 奇迹钻石
		PfSdkConfig yiwanqjzs = new PfSdkConfig();
		yiwanqjzs.setAppId("10413");
		yiwanqjzs.setAppKey("VvpKUeRaNcODkyQ");
		yiwanqjzs.setLog(true);
		yiwanqjzs.setReyunAppId("0842360ef395c3194b58088c7b39f55e");
		yiwanqjzs.setPf("yiwanqjzs");
		yiwanMap.put("yiwanqjzs", yiwanqjzs);
		// 奇迹刷宝
		PfSdkConfig yiwanqjshuab = new PfSdkConfig();
		yiwanqjshuab.setAppId("10414");
		yiwanqjshuab.setAppKey("eRZWgVSvrLIXDoiY");
		yiwanqjshuab.setLog(true);
		yiwanqjshuab.setReyunAppId("2829ce0793f2f92ed3c63544d63dcf8d");
		yiwanqjshuab.setPf("yiwanqjshuab");
		yiwanMap.put("yiwanqjshuab", yiwanqjshuab);
		// 奇迹传奇
		PfSdkConfig yiwanqjcq = new PfSdkConfig();
		yiwanqjcq.setAppId("10415");
		yiwanqjcq.setAppKey("VNtiQIdyHnA9Gehq");
		yiwanqjcq.setLog(true);
		yiwanqjcq.setReyunAppId("9cf00e1d1d1bf31c389b2902c3553f5d");
		yiwanqjcq.setPf("yiwanqjcq");
		yiwanMap.put("yiwanqjcq", yiwanqjcq);
		// 奇迹SF
		PfSdkConfig yiwanqjsf = new PfSdkConfig();
		yiwanqjsf.setAppId("10416");
		yiwanqjsf.setAppKey("EBhf7JmXCNG9ktc");
		yiwanqjsf.setLog(true);
		yiwanqjsf.setReyunAppId("b340cb749028690ed335f74c4b08862a");
		yiwanqjsf.setPf("yiwanqjsf");
		yiwanMap.put("yiwanqjsf", yiwanqjsf);
		// 奇迹大天使
		PfSdkConfig yiwanqjdts = new PfSdkConfig();
		yiwanqjdts.setAppId("10417");
		yiwanqjdts.setAppKey("cgaSp7UOCrfyWqoG");
		yiwanqjdts.setLog(true);
		yiwanqjdts.setReyunAppId("9d610d1f14c771efb23a9f30a1ddf976");
		yiwanqjdts.setPf("yiwanqjdts");
		yiwanMap.put("yiwanqjdts", yiwanqjdts);
	}

	/** 益玩ios平台 **/
	private static Map<String, PfSdkConfig> yiwanIosMap = new HashMap<String, PfSdkConfig>();
	static {
		// 芈月传
		PfSdkConfig ywmyzios = new PfSdkConfig();
		ywmyzios.setAppId("10003");
		ywmyzios.setAppKey("7uws3TvjkOEAmio9");
		ywmyzios.setPf("ywmyzios");
		yiwanIosMap.put("ywmyzios", ywmyzios);

		// 秦时芈月
		PfSdkConfig ywqsmyios = new PfSdkConfig();
		ywqsmyios.setAppId("10004");
		ywqsmyios.setAppKey("bcf5QP6q8hnasSO7");
		ywqsmyios.setPf("ywqsmyios");
		yiwanIosMap.put("ywqsmyios", ywqsmyios);

		// 天天挂传奇IOS
		PfSdkConfig ttgcqios = new PfSdkConfig();
		ttgcqios.setAppId("10005");
		ttgcqios.setAppKey("MdrJafAkx53TWY9o");
		ttgcqios.setPf("ttgcqios");
		yiwanIosMap.put("ttgcqios", ttgcqios);
	}

	/**
	 * 获取益玩静态数据
	 * 
	 * @param pf
	 * @return
	 */
	public static PfSdkConfig getYiwanConfig(String pf) {
		if (yiwanMap.containsKey(pf)) {
			return yiwanMap.get(pf);
		}
		if (yiwanIosMap.containsKey(pf)) {
			return yiwanIosMap.get(pf);
		}
		return null;
	}

	public static PfSdkConfig getYiwanConfigByChannel(String pfChannel) {
		PfSdkConfig config = getYiwanConfig(pfChannel);
		if (config != null) {
			return config;
		}

		List<String> list = new ArrayList<String>();
		for (String key : yiwanMap.keySet()) {
			if (pfChannel.startsWith(key)) {
				list.add(key);
				// return yiwanMap.get(key);
			}
		}

		for (String key : yiwanIosMap.keySet()) {
			if (pfChannel.startsWith(key)) {
				list.add(key);
				// return yiwanIosMap.get(key);
			}
		}

		String find = "";
		int find_reLength = 0;
		for (String key : list) {
			int reLength = pfChannel.replace(key, "").length();
			if (StringUtils.isBlank(find) || reLength < find_reLength) {
				find = key;
				find_reLength = reLength;
			}
		}
		return getYiwanConfig(find);
	}

	/**
	 * Pf是否在静态数据Map中
	 * 
	 * @param pf
	 * @return
	 */
	public static boolean isHasPf(String pf) {
		if (yiwanMap.containsKey(pf)) {
			return true;
		}
		return false;
	}

	/**
	 * Pf是否在静态数据Map中
	 * 
	 * @param pf
	 * @return
	 */
	public static boolean isIosHasPf(String pf) {
		if (yiwanIosMap.containsKey(pf)) {
			return true;
		}
		return false;
	}

	public static void main(String[] args) {

		System.out.println(JacksonUtil.writeValueAsString(getYiwanConfigByChannel("yiwanqjdtscom")));
		System.out.println("ywmyzcom".replace("ywmyz", ""));
		System.out.println(JacksonUtil.writeValueAsString(getYiwanConfigByChannel("yiwanqjcom")));
	}

}
