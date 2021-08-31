package com.sy599.game.gcommand.login.base.pfs.weixin;

import com.sy599.game.common.asyn.AsynUtil;
import com.sy599.game.db.bean.RegInfo;
import com.sy599.game.gcommand.login.util.LoginUtil;
import com.sy599.game.util.JsonWrapper;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.MD5Util;
import com.sy599.game.util.StringUtil;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;


public final class WeixinUtil {
	/**
	 * 根据code获取token
	 * 
	 * @param code
	 * @return 
	 *         {"scope":"snsapi_userinfo","unionid":"omkQws_l0hwNIa5qJzIuAXZqTlYc"
	 *         ,"openid":"oHTS3v_xExapmQq3IyOVxEQ14iRE","expires_in":7200,
	 *         "refresh_token":
	 *         "gOMLrv7ISf-wlxrbHc1EDiz5VB8BTnZRZZY7Ag75zBLsIJXnoji-k__7Zp3qADMfte2MJNEbk1KxgqEJu8RapVDkgNCVWjaaD5_o9Rp3NGo"
	 *         ,"access_token":
	 *         "O6UvHGuK7Qyvmhr9nh2P2t5Ml48b1RhpzOtqmxSiL4HSvM9dB8qlhhVT1yVS4HqounFKjkFhDvEF9aSJAcD0ustP3EdwMl0ejrfspYZUMvo"
	 *         }
	 */
	public static JsonWrapper getAccessToken(String appId, String secret, String code) {
		String url = "https://api.weixin.qq.com/sns/oauth2/access_token";
		String grant_type = "authorization_code";
		HashMap<String, String> params = new HashMap<String, String>();
		try {
			params.put("appId", appId);
			params.put("secret", secret);
			params.put("code", code);
			params.put("grant_type", grant_type);

			String resp = AsynUtil.submit(url,"UTF-8","GET",params,null,1);
			if (!StringUtils.isBlank(resp)) {
				return new JsonWrapper(resp);
			}
		} catch (Exception e) {
			LogUtil.msgLog.error("weixin getAccessToken error:"+e.getMessage(), e);
		}
		return null;
	}

	public static JsonWrapper jscode2session(String appId, String secret, String code) {
		String url = "https://api.weixin.qq.com/sns/jscode2session";
		String grant_type = "authorization_code";
		HashMap<String, String> params = new HashMap<String, String>();
		try {
			params.put("appid", appId);
			params.put("secret", secret);
			params.put("js_code", code);
			params.put("grant_type", grant_type);

			String resp = AsynUtil.submit(url,"UTF-8","GET",params,null,1);
			if (StringUtils.isNotBlank(resp)) {
				JsonWrapper json = new JsonWrapper(resp);
				if (json.isHas("session_key")){
					return json;
				}
			}
		} catch (Exception e) {
			LogUtil.msgLog.error("weixin jscode2session error:"+e.getMessage(), e);
		}
		return null;
	}

	/**
	 * 获取用户信息
	 * 
	 * @param access_token
	 * @param openid
	 * @return
	 */
	public static JsonWrapper getUserinfo(String access_token, String openid) {
		String url = "https://api.weixin.qq.com/sns/userinfo";
		HashMap<String, String> params = new LinkedHashMap<>();
		try {
			params.put("access_token", access_token);
			params.put("openid", openid);

			String resp = AsynUtil.submit(url,"UTF-8","GET",params,null,1);
			if (!StringUtils.isBlank(resp)) {
				return new JsonWrapper(resp);
			}
		} catch (Exception e) {
			LogUtil.msgLog.error("weixin getUserinfo error", e);
		}
		return null;
	}

	public static void createRole(JsonWrapper wrapper, RegInfo regInfo) throws Exception {
		String openid = wrapper.getString("openid");
		String nickname = wrapper.getString("nickname");
		nickname = StringUtil.filterEmoji(nickname);
		String headimgurl = wrapper.getString("headimgurl");
		String unionid = wrapper.getString("unionid");
		int sex = wrapper.getInt("sex", 1);
		if (sex == 0) {
			sex = 1;
		}
		if (headimgurl==null){
			headimgurl="";
		}else if(headimgurl.startsWith("https")){
            headimgurl = headimgurl.replaceFirst("https","http");
        }

		regInfo.setFlatId(openid);
		regInfo.setName(nickname);
		String imageName = MD5Util.getMD5String(headimgurl);
		regInfo.setHeadimgraw(imageName);
		// checkSaveHeadImg(regInfo, headimgurl);
		regInfo.setHeadimgurl(headimgurl);
//		checkSaveHeadImg(regInfo, headimgurl);
		regInfo.setSex(sex);
		regInfo.setIdentity(unionid);
		String password = "xsg_" + regInfo.getPf() + "_pw_default_" + regInfo.getFlatId();
		regInfo.setPw(LoginUtil.genPw(password));
		regInfo.setSessCode(LoginUtil.genSessCode(regInfo.getFlatId()));
		JsonWrapper jsonWrapper = new JsonWrapper("");
		jsonWrapper.putString("wx",nickname);
		regInfo.setLoginExtend(jsonWrapper.toString());

		authorization(regInfo, unionid, true);
	}

	public static void authorization(RegInfo regInfo, String unionid, boolean isCreateRole) throws Exception {
//		AuthorizationDaoImpl authorization = AuthorizationDaoImpl.getInstance();
//		WeiXinAuthorization wx = authorization.queryWeiXinAuthorization(unionid);
//
//		if (wx != null) {
//			if (isCreateRole) {
//				if (wx.getAgencyId() > 0) {
//					regInfo.setRegBindId(wx.getAgencyId());
//					regInfo.setPayBindId(wx.getAgencyId());
//					regInfo.setPayBindTime(new Date());
//				}
//			}
//
//			if (wx.getInviterId() > 0) {
//
//				ActivityBean activity = StaticDataManager.getActivityBean(ActivityConstant.activity_fudai);
//				if (activity != null) {
//
//					if (!isCreateRole) {
//						long currTime = TimeUtil.currentTimeMillis();
//						long lastTime = regInfo.getLogTime().getTime();
//						long twentyDay = TimeUtil.DAY_IN_MINILLS * 20;
//						if (currTime - lastTime < twentyDay) {
//							return;
//						}
//					}
//
//					long inviterId = wx.getInviterId();
//					Manager.invitationSuccess(regInfo, inviterId);
//				}
//			}
//		}
	}

}
