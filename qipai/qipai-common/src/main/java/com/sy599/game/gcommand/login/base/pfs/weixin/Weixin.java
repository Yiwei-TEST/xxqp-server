package com.sy599.game.gcommand.login.base.pfs.weixin;

import com.alibaba.fastjson.JSONObject;
import com.sy.mainland.util.Base64Util;
import com.sy.mainland.util.HttpUtil;
import com.sy599.game.db.bean.*;
import com.sy599.game.gcommand.login.base.BaseSdk;
import com.sy599.game.gcommand.login.base.pfs.configs.PfSdkConfig;
import com.sy599.game.util.JsonWrapper;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.MD5Util;
import com.sy599.game.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.HashMap;
import java.util.Map;

public class Weixin extends BaseSdk {

	private String code;
	private JsonWrapper sessionJson;
	private JsonWrapper userInfo;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public JsonWrapper getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(JsonWrapper userInfo) {
		this.userInfo = userInfo;
	}

	public JsonWrapper getSessionJson() {
		return sessionJson;
	}

	public void setSessionJson(JsonWrapper sessionJson) {
		this.sessionJson = sessionJson;
	}

	@Override
	public String loginExecute() {
		String result = "";
		String access_token;
		String openid;

		if (sessionJson == null) {
			String refresh_token = params.getString("refresh_token");

			if (getOpt() != null && getOpt().startsWith("auth:") && getExt() != null && getExt().startsWith("openid:")) {
				access_token = getOpt().substring(5);
				openid = getExt().substring(7);
			} else if ("refresh".equals(getOpt()) && StringUtils.isNotBlank(refresh_token) && !"undefined".equalsIgnoreCase(refresh_token)) {
				PfSdkConfig pfSdkConfig = getPfSdkConfig();
				if (pfSdkConfig == null) {
					return "";
				} else {
					access_token = null;
					openid = null;
					Map<String, String> map = new HashMap<>();
					map.put("appid", pfSdkConfig.getAppId());
					map.put("grant_type", "refresh_token");
					map.put("refresh_token", refresh_token);

					String ret = HttpUtil.getUrlReturnValue("https://api.weixin.qq.com/sns/oauth2/refresh_token", "UTF-8", "GET", map, 2);
					if (StringUtils.isNotBlank(ret)) {
						JSONObject jsonObject = JSONObject.parseObject(ret);
						openid = jsonObject.getString("openid");
						access_token = jsonObject.getString("access_token");
						if (StringUtils.isBlank(openid) || StringUtils.isBlank(access_token)) {
							return "";
						}
					}
				}
			} else {
				access_token = params.getString("access_token");
				openid = params.getString("openid");
			}

			long time1 = System.currentTimeMillis();
			JsonWrapper json = WeixinUtil.getUserinfo(access_token, openid);

			LogUtil.msgLog.info("weixin login time(ms):" + (System.currentTimeMillis() - time1));

			if (json != null) {
				String openid0 = json.getString("openid");
				if (StringUtils.isNotBlank(openid0)) {
					setSdkId(openid0);
					json.putString("access_token", access_token);
					result = json.toString();
				} else {
					LogUtil.msgLog.error(pf + " loginExecute err:" + json.toString());
				}
			}
		}else{
			//微信小程序、小游戏登陆
			String openid0 = sessionJson.getString("openid");
			String unionId = sessionJson.getString("unionid");
			String session_key = sessionJson.getString("session_key");

			String miniProgram = params.getString("miniProgram");
			JSONObject json = JSONObject.parseObject(miniProgram);

			String encryptedData = json.getString("encryptedData");
//			String rawData = json.getString("rawData");
//			String signature = json.getString("signature");
			String iv = json.getString("iv");
//			String userInfo = json.getString("userInfo");

			try {
				SecretKeySpec skeySpec = new SecretKeySpec(Base64Util.decode(session_key.getBytes("utf-8")), "AES");
				Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
				IvParameterSpec ips = new IvParameterSpec(Base64Util.decode(iv.getBytes("utf-8")));
				cipher.init(Cipher.DECRYPT_MODE, skeySpec, ips);
				byte[] original = cipher.doFinal(Base64Util.decode(encryptedData.getBytes("utf-8")));
				encryptedData = new String(original, "UTF-8");

				JSONObject jsonObject = JSONObject.parseObject(encryptedData);

				if (openid0.equals(jsonObject.getString("openId"))&&getPfSdkConfig().getAppId().equals(jsonObject.getJSONObject("watermark").getString("appid"))){
					JSONObject json0 = new JSONObject();
					if (StringUtils.isNotBlank(unionId)){
						json0.put("unionid",unionId);
					}else if (jsonObject.containsKey("unionId")){
						json0.put("unionid",jsonObject.getString("unionId"));
					}
					json0.put("openid",openid0);
					json0.put("nickname",jsonObject.getString("nickName"));
					String image = jsonObject.getString("avatarUrl");
					json0.put("headimgurl",image!=null&&image.startsWith("https://")? image.replace("https://","http://") : image);
					json0.put("sex",jsonObject.getString("gender"));

					setSdkId(openid0);
					result = json0.toString();
					userInfo = new JsonWrapper(json0);
				}
			}catch (Exception e){
				LogUtil.errorLog.error("Exception:"+e.getMessage(),e);
			}
		}

		return result;
	}

	@Override
	public void createRole(RegInfo regInfo, String info) throws Exception {
		JsonWrapper wrapper = new JsonWrapper(info);
		WeixinUtil.createRole(wrapper, regInfo);

	}

	@Override
	public Map<String, Object> refreshRole(RegInfo regInfo, String info) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		JsonWrapper wrapper = new JsonWrapper(info);
		String nickname = wrapper.getString("nickname");
		String headimgurl = wrapper.getString("headimgurl");
		String unionid = wrapper.getString("unionid");

		if (headimgurl==null){
			headimgurl="";
		}else if(headimgurl.startsWith("https")){
		    headimgurl = headimgurl.replaceFirst("https","http");
        }

		int sex = wrapper.getInt("sex", 1);
		if (sex == 0) {
			sex = 1;
		}

		if (StringUtils.isBlank(regInfo.getIdentity())) {
			map.put("identity", unionid);
			regInfo.setIdentity(unionid);
		}

		nickname = StringUtil.filterEmoji(nickname);
		if (!regInfo.getName().equals(nickname)) {
			map.put("name", nickname);
			regInfo.setName(nickname);
		}

        JsonWrapper jsonWrapper = new JsonWrapper(regInfo.getLoginExtend());
        if (!org.apache.commons.lang3.StringUtils.equals(nickname,jsonWrapper.getString("wx"))){
            jsonWrapper.putString("wx",nickname);
            regInfo.setLoginExtend(jsonWrapper.toString());
            map.put("loginExtend", regInfo.getLoginExtend());
        }

		if (regInfo.getSex() != sex) {
			map.put("sex", sex);
			regInfo.setSex(sex);
		}

		if (!headimgurl.equals(regInfo.getHeadimgurl())){
			map.put("headimgurl", headimgurl);
			map.put("headimgraw", MD5Util.getMD5String(headimgurl));
			regInfo.setHeadimgurl(headimgurl);
		}

//		if (check) {
//			map.put("headimgurl", regInfo.getHeadimgurl());
//			map.put("headimgraw", MD5Util.getStringMD5(headimgurl));
//		}

		WeixinUtil.authorization(regInfo, unionid, false);
		return map;
	}

}
