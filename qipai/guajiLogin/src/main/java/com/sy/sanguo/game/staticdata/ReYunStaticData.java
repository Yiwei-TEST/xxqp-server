package com.sy.sanguo.game.staticdata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.sy.sanguo.common.util.JacksonUtil;
import com.sy.sanguo.game.bean.PfSdkConfig;

public class ReYunStaticData {
	private static Map<String, PfSdkConfig> reyunMap = new HashMap<String, PfSdkConfig>();
	static {
		PfSdkConfig weixin = new PfSdkConfig();
		weixin.setPf("weixin");
		weixin.setLog(false);
		weixin.setReyunAppId("1421745f9ac2f7b4d960c9124e06c631");
		reyunMap.put(weixin.getPf(), weixin);
	}

	/**
	 * 获取静态数据
	 * 
	 * @param pf
	 * @return
	 */
	public static PfSdkConfig getReyun(String pf) {
		if (reyunMap.containsKey(pf)) {
			return reyunMap.get(pf);
		}
		return null;
	}

	public static PfSdkConfig getYiwanConfigByChannel(String pfChannel) {
		PfSdkConfig config = getReyun(pfChannel);
		if (config != null) {
			return config;
		}

		List<String> list = new ArrayList<String>();
		for (String key : reyunMap.keySet()) {
			if (pfChannel.startsWith(key)) {
				list.add(key);
				// return yiwanMap.get(key);
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
		return getReyun(find);
	}

	public static void main(String[] args) {
		System.out.println(JacksonUtil.writeValueAsString(getYiwanConfigByChannel("vega")));
		;
	}
}
