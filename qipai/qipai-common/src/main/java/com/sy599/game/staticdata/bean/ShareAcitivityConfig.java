package com.sy599.game.staticdata.bean;

import com.sy599.game.util.StringUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author liuping
 * 分享领钻活动配置
 */
public class ShareAcitivityConfig extends ActivityConfigInfo {

	private Map<String, String> diamondGoldGames;
	
	private Map<String, String> diamondConfigGames;
	
	@Override
	public void configParamsAndRewards() {
		diamondGoldGames = new HashMap<>();
		List<String> goldGameList = StringUtil.explodeToStringList(params, "&");
		for(String temp : goldGameList) {
			String [] arr = temp.split("=");
			diamondGoldGames.put(arr[0], arr[1]);
		}
		diamondConfigGames = new HashMap<>();
		List<String> configGameList = StringUtil.explodeToStringList(rewards, "&");
		for(String temp : configGameList) {
			String [] arr = temp.split("=");
			diamondConfigGames.put(arr[0], arr[1]);
		}
	}

	public Map<String, String> getDiamondGoldGames() {
		return diamondGoldGames;
	}

	public void setDiamondGoldGames(Map<String, String> diamondGoldGames) {
		this.diamondGoldGames = diamondGoldGames;
	}

	public Map<String, String> getDiamondConfigGames() {
		return diamondConfigGames;
	}

	public void setDiamondConfigGames(Map<String, String> diamondConfigGames) {
		this.diamondConfigGames = diamondConfigGames;
	}

	
}
