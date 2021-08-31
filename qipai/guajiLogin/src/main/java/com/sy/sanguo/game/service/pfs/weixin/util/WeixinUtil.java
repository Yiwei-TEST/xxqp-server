package com.sy.sanguo.game.service.pfs.weixin.util;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.sy.sanguo.common.util.asyn.AsynUtil;

import org.apache.commons.lang.StringUtils;

import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.server.GameServerManager;
//import com.sy.sanguo.common.util.Http;
//import com.sy.sanguo.common.util.HttpRequester;
//import com.sy.sanguo.common.util.HttpRespons;
//import com.sy.sanguo.common.util.HttpUtil;
import com.sy.sanguo.common.util.JsonWrapper;
import com.sy.sanguo.common.util.LoginCacheContext;
import com.sy.sanguo.common.util.MD5Util;
import com.sy.sanguo.common.util.NetTool;
import com.sy.sanguo.common.util.RegUtil;
import com.sy.sanguo.common.util.StringUtil;
import com.sy.sanguo.game.bean.PfSdkConfig;
import com.sy.sanguo.game.bean.RegInfo;
import com.sy.sanguo.game.bean.WeiXinAuthorization;
import com.sy.sanguo.game.dao.AuthorizationDaoImpl;
import com.sy.sanguo.game.dao.UserDao;
import com.sy.sanguo.game.pdkuai.constants.ActivityConstant;
import com.sy.sanguo.game.pdkuai.staticdata.StaticDataManager;
import com.sy.sanguo.game.pdkuai.staticdata.bean.ActivityBean;
import com.sy.sanguo.game.pdkuai.user.Manager;
import com.sy.sanguo.game.service.SysInfManager;
import com.sy.sanguo.game.staticdata.PfCommonStaticData;
import com.sy599.sanguo.util.TimeUtil;

public final class WeixinUtil {

	public static JsonWrapper refreshAccessToken(String appId, String refreshToken) {
		String url = "https://api.weixin.qq.com/sns/oauth2/refresh_token";
		String grant_type = "refresh_token";
		HashMap<String, String> params = new HashMap<>();
		try {
			params.put("appid", appId);
			params.put("refresh_token", refreshToken);
			params.put("grant_type", grant_type);

			String resp = AsynUtil.submit(url,"UTF-8","GET",params,null,1);
			if (!StringUtils.isBlank(resp)) {
				return new JsonWrapper(resp);
			}
		} catch (Exception e) {
			GameBackLogger.SYS_LOG.error("weixin refreshAccessToken error", e);
		}
		return null;
	}

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

			// 获取
//			HttpRespons res = HttpRequester.sendPost(url, params);
//			String resp = res.getContent();
			String resp = AsynUtil.submit(url,"UTF-8","GET",params,null,1);
			if (!StringUtils.isBlank(resp)) {
				// {"scope":"snsapi_userinfo","unionid":"omkQws_l0hwNIa5qJzIuAXZqTlYc","openid":"oHTS3v_xExapmQq3IyOVxEQ14iRE","expires_in":7200,"refresh_token":"gOMLrv7ISf-wlxrbHc1EDiz5VB8BTnZRZZY7Ag75zBLsIJXnoji-k__7Zp3qADMfte2MJNEbk1KxgqEJu8RapVDkgNCVWjaaD5_o9Rp3NGo","access_token":"O6UvHGuK7Qyvmhr9nh2P2t5Ml48b1RhpzOtqmxSiL4HSvM9dB8qlhhVT1yVS4HqounFKjkFhDvEF9aSJAcD0ustP3EdwMl0ejrfspYZUMvo"}
				return new JsonWrapper(resp);
			}
		} catch (Exception e) {
			GameBackLogger.SYS_LOG.error("weixin getAccessToken error", e);
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
			GameBackLogger.SYS_LOG.error("weixin jscode2session error:"+e.getMessage(), e);
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

			// 获取
//			HttpUtil http = new HttpUtil(url);
//			String resp = http.post(params);
			String resp = AsynUtil.submit(url,"UTF-8","GET",params,null,1);
			if (!StringUtils.isBlank(resp)) {
				return new JsonWrapper(resp);
			}
		} catch (Exception e) {
			GameBackLogger.SYS_LOG.error("weixin getUserinfo error", e);
		}
		return null;
		// "openid":"OPENID",
		// "nickname":"NICKNAME",
		// "sex":1,
		// "province":"PROVINCE",
		// "city":"CITY",
		// "country":"COUNTRY",
		// "headimgurl":
		// "http://wx.qlogo.cn/mmopen/g3MonUZtNHkdmzicIlibx6iaFqAc56vxLSUfpb6n5WKSYVY0ChQKkiaJSgQ1dZuTOgvLLrhJbERQQ4eMsv84eavHiaiceqxibJxCfHe/0",
		// "privilege":[
		// "PRIVILEGE1",
		// "PRIVILEGE2"
		// ],
		// "unionid": "o6_bmasdasdsad6_2sgVt7hMZOPfL"
		//
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
		}
		regInfo.setFlatId(openid);
		regInfo.setName(nickname);
		String imageName = MD5Util.getStringMD5(headimgurl);
		regInfo.setHeadimgraw(imageName);
		// checkSaveHeadImg(regInfo, headimgurl);
		regInfo.setHeadimgurl(headimgurl);
//		checkSaveHeadImg(regInfo, headimgurl);
		regInfo.setSex(sex);
		regInfo.setIdentity(unionid);
		String password = "xsg_" + regInfo.getPf() + "_pw_default_" + regInfo.getFlatId();
		regInfo.setPw(RegUtil.genPw(password));
		regInfo.setSessCode(RegUtil.genSessCode(regInfo.getFlatId()));
		JsonWrapper jsonWrapper = new JsonWrapper("");
		jsonWrapper.putString("wx",nickname);
		regInfo.setLoginExtend(jsonWrapper.toString());
		// AuthorizationDaoImpl authorization =
		// AuthorizationDaoImpl.getInstance();
		// WeiXinAuthorization wx =
		// authorization.queryWeiXinAuthorization(unionid);
		//
		// if (wx != null) {
		// if (wx.getAgencyId() > 0) {
		// regInfo.setRegBindId(wx.getAgencyId());
		// }
		//
		// if (wx.getInviterId() > 0) {
		// ActivityBean activity =
		// StaticDataManager.getActivityBean(ActivityConstant.activity_fudai);
		// if (activity != null) {
		// long inviterId = wx.getInviterId();
		// Manager.invitationSuccess(regInfo, inviterId);
		// }
		// }
		// }

		authorization(regInfo, unionid, true);
	}

	public static void authorization(RegInfo regInfo, String unionid, boolean isCreateRole) throws Exception {
		AuthorizationDaoImpl authorization = AuthorizationDaoImpl.getInstance();
		WeiXinAuthorization wx = authorization.queryWeiXinAuthorization(unionid);

		if (wx != null) {
			if (isCreateRole) {
				if (wx.getAgencyId() > 0) {
					regInfo.setRegBindId(wx.getAgencyId());
					regInfo.setPayBindId(wx.getAgencyId());
					regInfo.setPayBindTime(new Date());
				}
			}

			if (wx.getInviterId() > 0) {

				ActivityBean activity = StaticDataManager.getActivityBean(ActivityConstant.activity_fudai);
				if (activity != null) {

					if (!isCreateRole) {
						long currTime = TimeUtil.currentTimeMillis();
						long lastTime = regInfo.getLogTime().getTime();
						long twentyDay = TimeUtil.DAY_IN_MINILLS * 20;
						if (currTime - lastTime < twentyDay) {
							return;
						}
					}

					long inviterId = wx.getInviterId();
					Manager.invitationSuccess(regInfo, inviterId);
				}
			}
		}
	}

	private static boolean deleteDir(File dir) {
		// 递归删除目录中的子目录下
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		// 目录此时为空，可以删除
		return dir.delete();
	}

	public static boolean checkSaveHeadImg(RegInfo regInfo, String headimgurl) {
		if (!StringUtils.isBlank(headimgurl)) {
			headimgurl= headimgurl.substring(0, headimgurl.length() - 1) + "132";
			if (!StringUtils.isBlank(regInfo.getHeadimgraw()) && !StringUtils.isBlank(regInfo.getHeadimgurl())) {
				if (regInfo.getHeadimgraw().equals(MD5Util.getStringMD5(headimgurl))) {
					return false;
				}
			}
			
			if (StringUtils.isBlank(LoginCacheContext.headImgUrl)) {
				if (!regInfo.getHeadimgurl().equals(headimgurl)) {
					regInfo.setHeadimgurl(headimgurl);
					return true;
				}else{
					return false;
				}
			}
			
			int toDay = TimeUtil.getSimpleToDay();
			String baseDir = SysInfManager.baseDir;
			String imagePath = "/headimage/" + toDay + "/" + regInfo.getUserId() % 5 + "/" + regInfo.getUserId() + "/";
			String imageName = MD5Util.getStringMD5(headimgurl);
			try {
				File fileDir = new File(baseDir + imagePath);
				if (fileDir.exists()) {
					deleteDir(fileDir);
				}
//				String getHeadUrl = headimgurl.substring(0, headimgurl.length() - 1) + "132";// 取132像素的图片
				boolean isGet = NetTool.getImage(headimgurl, 0, 0, baseDir + imagePath, imageName + "");
				if (isGet) {
					String baseUrl = LoginCacheContext.headImgUrl;
					if (StringUtils.isBlank(baseUrl)) {
						baseUrl = SysInfManager.baseUrl;
					}
					regInfo.setHeadimgraw(imageName);
					regInfo.setHeadimgurl(baseUrl + imagePath + imageName + ".jpg");
				}
				return isGet;
			} catch (Exception e) {
				GameBackLogger.SYS_LOG.error(" checkSaveHeadImg err", e);
			}

		}
		return false;
	}

	public static RegInfo getInfoByCode(String code) {
		return getInfoByCode(code, null);
	}

	public static RegInfo getInfoByCode(String code, String pf) {
		Map<String, Object> result = new HashMap<String, Object>();
		if (code != null) {
			// 测试
			// String appid = "wx70eb4fc2ecce43c6";
			// String secret = "6f449778e9aaa8e5ff58f87d5b6dfd42";
			// 正式
			PfSdkConfig config = PfCommonStaticData.getGzhConfig(pf);
			String appid = config.getAppId();
			String secret = config.getAppKey();
			String grant_type = "authorization_code";

			String accessTokenUrl = "https://api.weixin.qq.com/sns/oauth2/access_token";// ?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code";
			String userInfoUrl = "https://api.weixin.qq.com/sns/userinfo";// ?access_token=ACCESS_TOKEN&openid=OPENID&lang=zh_CN";

			Map<String, String> paraMap = new HashMap<String, String>();
			paraMap.put("appid", appid);
			paraMap.put("secret", secret);
			paraMap.put("code", code);
			paraMap.put("grant_type", grant_type);

			String msg = null;
//			Http http = null;
			JSONObject retStatus = null;
			try {
//				http = new Http(accessTokenUrl, false);
//				msg = http.post(paraMap);
				msg = AsynUtil.submit(accessTokenUrl,"UTF-8","GET",paraMap,null,1);
			} catch (Exception e) {
				GameBackLogger.SYS_LOG.error("WXAuthorization err", e);
			}
			// {"access_token":"BpcADkdRSevlgx3MWQ8wJl_0rFCdvUdOabR60Qxr7xdsVtSs3wWRoffBmcyx-us9z00aytzrbdKeAkOdcXUbmhIKUCYol2tfo5Qa_VlLRTo","expires_in":7200,"refresh_token":"_H8rQrKi-xIT3E28bWThHeX2SCn5HlSTaOwJxZUa-XzgRZCcAZCWR5ob6uZtj9o5HDr6l-Enyi5sl7-ouI_HLUPTuy-JVnWUNRujRKlNFV4","openid":"okb-RwdJs-MA1xqTy-20qgRJe4jE","scope":"snsapi_userinfo","unionid":"omkQws8jIq1uqG_IBQYWHJGJqZw8"}
			retStatus = JSONObject.parseObject(msg);
			if (msg.contains("access_token")) {
				String access_token = retStatus.getString("access_token");
				String openid = retStatus.getString("openid");
				paraMap = new HashMap<String, String>();
				paraMap.put("access_token", access_token);
				paraMap.put("openid", openid);
				paraMap.put("lang", "zh_CN");

				try {
//					http = new Http(userInfoUrl, false);
//					msg = http.post(paraMap);
					msg = AsynUtil.submit(userInfoUrl,"UTF-8","GET",paraMap,null,1);
				} catch (Exception e) {
					GameBackLogger.SYS_LOG.error("getInfoByCode err", e);
				}

				retStatus = JSONObject.parseObject(msg);
				if (msg.contains("unionid")) {
					String unionid = retStatus.getString("unionid");
					if (unionid != null) {
						result.put("code", 0);
						result.put("openid", openid);
						result.put("payItems", GameServerManager.getInstance().getIosPayItemMsg());
						RegInfo regInfo = UserDao.getInstance().getUserByUnionid(unionid);
						return regInfo;

					}
				} else {
					result.put("code", -2);
					result.put("msg", "获取unionid失败");
					GameBackLogger.SYS_LOG.error("getInfoByCode error:: 获取unionid失败。错误码：" + retStatus.toString());
				}
			} else {

				result.put("code", -3);
				result.put("msg", "获取access_token失败。错误码：" + retStatus.getString("errcode") + "，错误消息：" + retStatus.getString("errmsg"));
				GameBackLogger.SYS_LOG.error("getInfoByCode error::获取access_token失败。错误码：" + retStatus.toString());
			}
		}
		return null;
	}
}
