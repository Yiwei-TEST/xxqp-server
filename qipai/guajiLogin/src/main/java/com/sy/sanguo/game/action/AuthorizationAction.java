package com.sy.sanguo.game.action;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.sy.mainland.util.HttpUtil;
import com.sy.mainland.util.OutputUtil;
import com.sy.sanguo.common.struts.StringResultType;

import com.sy.sanguo.common.util.*;
import org.apache.commons.lang3.StringUtils;

import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.server.GameServerManager;
import com.sy.sanguo.common.struts.GameStrutsAction;
import com.sy.sanguo.game.bean.PfSdkConfig;
import com.sy.sanguo.game.bean.RegInfo;
import com.sy.sanguo.game.bean.RoomCard;
import com.sy.sanguo.game.bean.WeiXinAuthorization;
import com.sy.sanguo.game.dao.AuthorizationDaoImpl;
import com.sy.sanguo.game.dao.RoomCardDaoImpl;
import com.sy.sanguo.game.dao.UserDaoImpl;
import com.sy.sanguo.game.staticdata.PfCommonStaticData;
import com.sy599.sanguo.util.TimeUtil;

public class AuthorizationAction extends GameStrutsAction {
	private static final long serialVersionUID = -3371095610052231066L;

	private RoomCardDaoImpl roomCardDao;
	private UserDaoImpl userDao;
	private String result;

	public String execute() throws Exception {
		return null;
	}

	public String getUserInfoById() {
		String flatId = this.getString("flatId");
		String paySign = this.getString("paySign");
		long payTime = this.getLong("payTime");
		long userId = this.getLong("userId");
		long viewId = this.getLong("viewId");
		long now = TimeUtil.currentTimeMillis();
		if (now - payTime > 30 * 60 * 1000) {
			return StringResultType.RETURN_ATTRIBUTE_NAME;
		}
		String md5 = MD5Util.getStringMD5(payTime + flatId + userId + "7HGO4K61M8N2D9LARSPU");
		if (!md5.equals(paySign)) {
			GameBackLogger.SYS_LOG.info("getUserInfoById-->" + JacksonUtil.writeValueAsString(getRequest().getParameterMap()));
			return StringResultType.RETURN_ATTRIBUTE_NAME;

		}

		Map<String, Object> result = new HashMap<String, Object>();
		try {
			RegInfo regInfo = userDao.getUser(viewId);
			if (regInfo == null) {
				result.put("code", -1);
				this.result = JacksonUtil.writeValueAsString(result);
				return StringResultType.RETURN_ATTRIBUTE_NAME;
			}
			result.put("code", 0);
			String k = MD5Util.getStringMD5(regInfo.getFlatId() + "1" + "weixin" + "1" + "mwFLeKLzNoL46dDn0vE2");
			result.put("k", k);
			result.put("headimgurl", regInfo.getHeadimgurl());
			result.put("flatId", regInfo.getFlatId());
			result.put("name", regInfo.getName());
			result.put("userId", regInfo.getUserId());
			result.put("payBindId", regInfo.getPayBindId());

		} catch (Exception e) {
			GameBackLogger.SYS_LOG.error("getUserInfoById error::", e);

		}

		this.result = JacksonUtil.writeValueAsString(result);
		GameBackLogger.SYS_LOG.info("getUserInfo-->" + this.result);
		return StringResultType.RETURN_ATTRIBUTE_NAME;
	}

	public void getUserOpenid() throws Exception {
		String code = getRequest().getParameter("code");
		String payType = getRequest().getParameter("payType");
		PfSdkConfig config;
		if (StringUtils.isNotBlank(code)&&StringUtils.isNotBlank(payType)&&(config = PfCommonStaticData.getConfig(payType))!=null) {
			String appid = config.getAppId();
			String secret = config.getAppKey();
			String grant_type = "authorization_code";

			String accessTokenUrl = "https://api.weixin.qq.com/sns/oauth2/access_token";// ?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code";

			Map<String, String> paraMap = new HashMap<String, String>();
			paraMap.put("appid", appid);
			paraMap.put("secret", secret);
			paraMap.put("code", code);
			paraMap.put("grant_type", grant_type);

			String msg = HttpUtil.getUrlReturnValue(accessTokenUrl,"UTF-8","GET",paraMap,3);
			if (StringUtils.isNotBlank(msg)){
				JSONObject json = JSONObject.parseObject(msg);
				String openid = json.getString("openid");
				if (StringUtils.isNotBlank(openid)){
					OutputUtil.output(0,openid,request,response,false);
				}else {
					OutputUtil.output(3, "没有获取到openid", request, response, false);
				}
			}else{
				OutputUtil.output(2,"微信没有返回信息",request,response,false);
			}
		}else{
			OutputUtil.output(1, LangMsg.getMsg(LangMsg.code_3),request,response,false);
		}
	}

	public String getUserInfo() throws Exception {
		String code = getRequest().getParameter("code");
		String gzhPf = this.getString("gzhPf", "xyGZH");
		Map<String, Object> result = new HashMap<String, Object>();
		if (code != null) {
			// 测试
			// String appid = "wxeca7f242934af7d4";
			// String secret = "297c9981ff2acd14559f53468a2c0e0e";
			// 正式

			PfSdkConfig config = PfCommonStaticData.getGzhConfig(gzhPf);
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
			Http http = null;
			JSONObject retStatus = null;
			try {
				http = new Http(accessTokenUrl, false);
				msg = http.post(paraMap);
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
					http = new Http(userInfoUrl, false);
					msg = http.post(paraMap);
				} catch (Exception e) {
					GameBackLogger.SYS_LOG.error("WXAuthorization err", e);
				}
				// {"openid":"okb-RwdJs-MA1xqTy-20qgRJe4jE","nickname":"Steven","sex":1,"language":"zh_CN","city":"长沙","province":"湖南","country":"中国","headimgurl":"http:\/\/wx.qlogo.cn\/mmopen\/PWC6E16cB97qB4XyZEicClg8SPPibh1R8JIQia0yVpMmGic7EdZHEIN1Q0oFeWODVjukAfahTeMYVibsUicKUUDMib1cn1mtx3f1Uef\/0","privilege":[],"unionid":"omkQws8jIq1uqG_IBQYWHJGJqZw8"}
				// omkQws8jIq1uqG_IBQYWHJGJqZw8
				// omkQws8jIq1uqG_IBQYWHJGJqZw8
				retStatus = JSONObject.parseObject(msg);
				if (msg.contains("unionid")) {
					String unionid = retStatus.getString("unionid");
					if (unionid != null) {
						long time = TimeUtil.currentTimeMillis();
						result.put("code", 0);
						result.put("openid", openid);
						result.put("payItems", GameServerManager.getInstance().getIosPayItemMsg());
						RegInfo regInfo = userDao.getUserByUnionid(unionid);
						if (regInfo == null) {
							result.put("userId", 0);
							this.result = JacksonUtil.writeValueAsString(result);
							return StringResultType.RETURN_ATTRIBUTE_NAME;
						} else {
							String md5 = MD5Util.getStringMD5(time + regInfo.getFlatId() + regInfo.getUserId() + "7HGO4K61M8N2D9LARSPU");
							String k = MD5Util.getStringMD5(regInfo.getFlatId() + "1" + "weixin" + "1" + "mwFLeKLzNoL46dDn0vE2");
							result.put("headimgurl", regInfo.getHeadimgurl());
							result.put("paySign", md5);
							result.put("payTime", time);
							result.put("flatId", regInfo.getFlatId());
							result.put("k", k);
							result.put("name", regInfo.getName());
							result.put("userId", regInfo.getUserId());
							result.put("payBindId", regInfo.getPayBindId());
						}

					}
				} else {
					result.put("code", -2);
					result.put("msg", "获取unionid失败");
					// + retStatus.getString("errcode") + "，错误消息：" +
					// retStatus.getString("errmsg")
					GameBackLogger.SYS_LOG.error("WXAuthorization error:: 获取unionid失败。错误码：" + retStatus.getString("errcode") + "，错误消息：" + retStatus.getString("errmsg"));
				}
			} else {

				result.put("code", -3);
				result.put("msg", "获取access_token失败。错误码：" + retStatus.getString("errcode") + "，错误消息：" + retStatus.getString("errmsg"));
				GameBackLogger.SYS_LOG.error("WXAuthorization error::获取access_token失败。错误码：" + retStatus.getString("errcode") + "，错误消息：" + retStatus.getString("errmsg"));
			}
		}

		this.result = JacksonUtil.writeValueAsString(result);
		GameBackLogger.SYS_LOG.info("getUserInfo-->" + this.result);
		return StringResultType.RETURN_ATTRIBUTE_NAME;
	}

	// 微信邀请
	public String weiXinInvite() throws Exception {

		Map<String, Object> result = new HashMap<String, Object>();
		String code = getRequest().getParameter("code");
		String userIdStr = getRequest().getParameter("state");

		if (!StringUtils.isBlank(code) && (!StringUtils.isBlank(userIdStr))) {

			String gzhPf = "xyGZH";
			long userId = Long.parseLong(userIdStr);
			RegInfo regInfo = userDao.getUser(userId);

			if (regInfo == null) {
				result.put("code", -4);
				result.put("msg", "没有取到邀请人userId=" + userId + "玩家的数据");
				GameBackLogger.SYS_LOG.error("weiXinInvite error::没有取到邀请人userId=" + userId + "玩家的数据");
			} else {

				PfSdkConfig config = PfCommonStaticData.getGzhConfig(gzhPf);
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
				Http http = null;
				JSONObject retStatus = null;
				try {
					http = new Http(accessTokenUrl, false);
					msg = http.post(paraMap);
				} catch (Exception e) {
					GameBackLogger.SYS_LOG.error("weiXinInvite err:", e);
				}

				retStatus = JSONObject.parseObject(msg);
				if (msg.contains("access_token")) {
					String access_token = retStatus.getString("access_token");
					String openid = retStatus.getString("openid");

					paraMap = new HashMap<String, String>();
					paraMap.put("access_token", access_token);
					paraMap.put("openid", openid);
					paraMap.put("lang", "zh_CN");

					try {
						http = new Http(userInfoUrl, false);
						msg = http.post(paraMap);
					} catch (Exception e) {
						GameBackLogger.SYS_LOG.error("weiXinInvite err:", e);
					}

					retStatus = JSONObject.parseObject(msg);
					if (msg.contains("unionid")) {
						AuthorizationDaoImpl authorizationDaoImpl = AuthorizationDaoImpl.getInstance();
						String unionid = retStatus.getString("unionid");
						// 入库
						WeiXinAuthorization wx = authorizationDaoImpl.queryWeiXinAuthorization(unionid);

						if (wx == null) {
							authorizationDaoImpl.addWeiXinInviter(unionid, userId);

							result.put("code", 0);
							result.put("msg", "被邀请成功");
						} else {

							if (wx.getInviterId() <= 0) {
								Map<String, Object> sqlMap = new HashMap<>();
								sqlMap.put("unionId", unionid);
								sqlMap.put("inviterId", userId);
								sqlMap.put("inviterTime", new Date());
								int count = authorizationDaoImpl.updateAuthorization(sqlMap);
								if (count > 0) {
									result.put("code", 0);
									result.put("msg", "被邀请成功");
								}
							} else {
								result.put("code", 1);
								result.put("msg", "您存在邀请人userId：" + userId);
								GameBackLogger.SYS_LOG.error("weiXinInvite error::" + "您存在邀请人userId：" + userId);
							}
						}
					} else {

						result.put("code", -2);
						result.put("msg", "获取unionid失败。错误码：" + retStatus.getString("errcode") + "，错误消息：" + retStatus.getString("errmsg"));
						GameBackLogger.SYS_LOG.error("weiXinInvite error:: 获取unionid失败。错误码：" + retStatus.getString("errcode") + "，错误消息：" + retStatus.getString("errmsg"));
					}
				} else {

					result.put("code", -3);
					result.put("msg", "获取access_token失败。错误码：" + retStatus.getString("errcode") + "，错误消息：" + retStatus.getString("errmsg"));
					GameBackLogger.SYS_LOG.error("weiXinInvite error::获取access_token失败。错误码：" + msg + "code:" + code);
				}
			}

		} else {

			result.put("code", -1);
			result.put("msg", "code或者state为空");
			GameBackLogger.SYS_LOG.error("weiXinInvite error:: code或者state为空");
		}

		this.result = JacksonUtil.writeValueAsString(result);
		return StringResultType.RETURN_ATTRIBUTE_NAME;

	}

	public String WXAuthorization() throws Exception {
		String code = getRequest().getParameter("code");
		String state = getRequest().getParameter("state");

		Map<String, Object> result = new HashMap<String, Object>();

		if (!StringUtils.isBlank(code) && (!StringUtils.isBlank(state))) {
			String[] values = state.split("_");
			int userId = StringUtil.getIntValue(values, 0);
			// int agencyId = StringUtil.getIntValue(values, 0);
			String gzhPf = StringUtil.getValue(values, 1, "xyGZH");

			RoomCard cardInfo = roomCardDao.queryAgencyInfoByUserId(userId);
			// RoomCard cardInfo = roomCardDao.queryAgencyInfo(agencyId);
			if (cardInfo == null) {
				result.put("code", -4);
				result.put("msg", "没有下载ID为" + userId + "代理商");
				GameBackLogger.SYS_LOG.error("WXAuthorization error::" + "没有下载ID为" + userId + "代理商");
			} else if (cardInfo.getAgencyId() <= 100000) {
				result.put("code", -6);
				result.put("msg", "暂不能绑定预留ID");
				GameBackLogger.SYS_LOG.error("WXAuthorization error::" + "暂不能绑定预留ID");
			} else if (1 == cardInfo.getPartAdmin()) {
				result.put("code", -5);
				result.put("msg", "错误的邀请码");
				GameBackLogger.SYS_LOG.error("WXAuthorization error::" + "该邀请码对应分admin帐号，暂不支持绑定用户");
			} else {

				PfSdkConfig config = PfCommonStaticData.getGzhConfig(gzhPf);
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
				Http http = null;
				JSONObject retStatus = null;
				try {
					http = new Http(accessTokenUrl, false);
					msg = http.post(paraMap);
				} catch (Exception e) {
					GameBackLogger.SYS_LOG.error("WXAuthorization err", e);
				}

				retStatus = JSONObject.parseObject(msg);
				if (msg.contains("access_token")) {
					String access_token = retStatus.getString("access_token");
					String openid = retStatus.getString("openid");
					// String expires_in = retStatus.getString("expires_in");
					// String refresh_token
					// =retStatus.getString("refresh_token");
					// String scope = retStatus.getString("scope");

					paraMap = new HashMap<String, String>();
					paraMap.put("access_token", access_token);
					paraMap.put("openid", openid);
					paraMap.put("lang", "zh_CN");

					try {
						http = new Http(userInfoUrl, false);
						msg = http.post(paraMap);
					} catch (Exception e) {
						GameBackLogger.SYS_LOG.error("WXAuthorization err", e);
					}

					retStatus = JSONObject.parseObject(msg);
					if (msg.contains("unionid")) {
						AuthorizationDaoImpl authorizationDaoImpl = AuthorizationDaoImpl.getInstance();
						String unionid = retStatus.getString("unionid");
						// 入库
						WeiXinAuthorization wx = authorizationDaoImpl.queryWeiXinAuthorization(unionid);

						if (wx == null) {
							authorizationDaoImpl.addWeiXinAuthorization(unionid, cardInfo.getAgencyId());

							result.put("code", 0);
							result.put("msg", "授权成功");
						} else {

							if (wx.getAgencyId() <= 0) {
								Map<String, Object> sqlMap = new HashMap<>();
								sqlMap.put("unionId", unionid);
								sqlMap.put("agencyId", cardInfo.getAgencyId());
								sqlMap.put("createTime", new Date());
								int count = authorizationDaoImpl.updateAuthorization(sqlMap);
								if (count > 0) {
									result.put("code", 0);
									result.put("msg", "授权成功");
								}
							} else {
								result.put("code", 1);
								result.put("msg", "您存在绑定的代理商ID：" + wx.getAgencyId());
								GameBackLogger.SYS_LOG.error("WXAuthorization error::" + "您存在绑定的代理商ID：" + wx.getAgencyId());
							}
						}
					} else {

						result.put("code", -2);
						result.put("msg", "获取unionid失败。错误码：" + retStatus.getString("errcode") + "，错误消息：" + retStatus.getString("errmsg"));
						GameBackLogger.SYS_LOG.error("WXAuthorization error:: 获取unionid失败。错误码：" + retStatus.getString("errcode") + "，错误消息：" + retStatus.getString("errmsg"));
					}
				} else {

					result.put("code", -3);
					result.put("msg", "获取access_token失败。错误码：" + retStatus.getString("errcode") + "，错误消息：" + retStatus.getString("errmsg"));
					GameBackLogger.SYS_LOG.error("WXAuthorization error::获取access_token失败。错误码：" + retStatus.getString("errcode") + "，错误消息：" + retStatus.getString("errmsg"));
				}
			}

		} else {

			result.put("code", -1);
			result.put("msg", "code或者state为空");
			GameBackLogger.SYS_LOG.error("WXAuthorization error:: code或者state为空");
		}

		this.result = JacksonUtil.writeValueAsString(result);
		return StringResultType.RETURN_ATTRIBUTE_NAME;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public RoomCardDaoImpl getRoomCardDao() {
		return roomCardDao;
	}

	public void setRoomCardDao(RoomCardDaoImpl roomCardDao) {
		this.roomCardDao = roomCardDao;
	}

	public UserDaoImpl getUserDao() {
		return userDao;
	}

	public void setUserDao(UserDaoImpl userDao) {
		this.userDao = userDao;
	}
}
