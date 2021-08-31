package com.sy.sanguo.game.pdkuai.game;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.util.HttpUtil;
import com.sy.sanguo.common.util.JacksonUtil;
import com.sy.sanguo.common.util.JsonWrapper;
import com.sy.sanguo.common.util.MD5Util;
import com.sy.sanguo.common.util.NetTool;
import com.sy.sanguo.game.bean.RegInfo;
import com.sy.sanguo.game.pdkuai.action.BaseAction;

public class PromotionAction extends BaseAction {
	private static final String APPID = "03136e2fc870e98d1660eb41beab95de";
	private static final String APPSecret = "f6e809f8ee7f6ab1ef06406bf531442d";
	/**
	 * 注册绑定码
	 */
	public static final int BINDID = 685986;

	@Override
	public String execute() throws Exception {
		int functype = this.getInt("funcType");
		switch (functype) {
		case 1:
			getUser();
			break;
		case 2:
			getSeniority();
			break;
		default:
			break;
		}
		return result;
	}

	public void getSeniority() throws Exception {
		Long userId = this.getLong("userId");
		RegInfo regInfo = userDao.getUser(userId);
		Map<String, Object> result = new HashMap<String, Object>();
		if (regInfo.getUsedCards() == 0) {
			result.put("msg", "需要消耗房卡才能领取奖励！");
			writeMsg(2, result);
			return;
		}
		result.put("msg", "该用户领奖符合条件");
		writeMsg(0, result);
	}

	public void getUser() throws Exception {
		Long userId = this.getLong("userId");// 用户标识
		RegInfo regInfo = userDao.getUser(userId);
		Map<String, Object> result = new HashMap<String, Object>();
		String phoneStr = this.getString("phoneCode");
		if (regInfo == null) {
			result.put("msg", "没找到该玩家！");
			writeMsg(2, result);
			return;
		}
		if (regInfo.getUsedCards() == 0) {
			result.put("msg", "需要消耗房卡才能领取奖励！");
			writeMsg(2, result);
			return;
		}
		String md5 = MD5Util.getStringMD5(phoneStr + APPID + APPSecret);
		// String
		// urlStr="http://g.zyh5.cn/ToOutWebSite/CallBack?userkey=18507312202&appid=03136e2fc870e98d1660eb41beab95de&token=9ad717f8e01ad11b0ba02f3d53d9cebd";
		String urlStr = "http://g.zyh5.cn/ToOutWebSite/CallBack?userkey="
				+ phoneStr + "&appid=" + APPID + "&token=" + md5;
		HttpUtil http = new HttpUtil(urlStr);
		String post = http.get("");
		JsonWrapper josn = new JsonWrapper(post);
		String state = josn.getString("status");
		if (!StringUtils.isBlank(state)) {
			if (0 == Integer.parseInt(state)) {
				Long uid = userDao.getUserPromotionByUid(userId);
				if (uid == null) {
					userDao.insertUserPromotion(userId, phoneStr);
					result.put("msg", "该用户领奖符合条件");
					writeMsg(0, result);
					return;
				} else {
					GameBackLogger.SYS_LOG.error("PromotionAction uid!=null:"+ post);
				}
			} else {
				GameBackLogger.SYS_LOG.error("PromotionAction state!=0:"+ " post:" + post);
			}
		} else {
			GameBackLogger.SYS_LOG.error("PromotionAction state==null:"+ " post:" + post);
		}
		result.put("msg", "不可重复领奖!");
		writeMsg(3, result);
	}
	
	public static void main(String[] args) throws Exception {
//		String urlStr="http://g.zyh5.cn/ToOutWebSite/CallBack";
		String urlStr="http://g.zyh5.cn/ToOutWebSite/CallBack?userkey=18507312202&appid=03136e2fc870e98d1660eb41beab95de&token=9ad717f8e01ad11b0ba02f3d53d9cebd";
		HttpUtil http=new HttpUtil(urlStr);
		Map<String,String> param=new HashMap<String,String>();
		param.put("userkey", "18507312202");
		param.put("appid", APPID);
		param.put("token", "9ad717f8e01ad11b0ba02f3d53d9cebd");
		String post=http.get("");
		System.out.println(post);
		
//		NetTool tool=new NetTool();
//		post=tool.sendPostRequest(urlStr, param, "UTF-8");
//		System.out.println(post);
	}
}
