package com.sy.sanguo.common.struts;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;
import com.sy.sanguo.common.util.OutputUtil;
import com.sy.sanguo.game.utils.BjdUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.struts2.StrutsStatics;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.apache.struts2.interceptor.SessionAware;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;
import com.sy.sanguo.common.exception.GameException;
import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.util.MD5Util;
import com.sy599.game.util.Md5CheckUtil;

public class GameStrutsAction extends ActionSupport implements SessionAware, ServletRequestAware, ServletResponseAware {

	private static final long serialVersionUID = 1L;
	/**
	 * struts2 session
	 */
	private Map<String, Object> att;

	/**
	 * struts2 request
	 */
	protected HttpServletRequest request;

	/**
	 * struts2 response
	 */
	protected HttpServletResponse response;

	/**
	 * 封装前台参数（json对象）
	 */
	private JSONObject jsonParameter;

	protected void sendXmlToClient(String s) throws GameException {
		HttpServletResponse response = (HttpServletResponse) ActionContext.getContext().get(StrutsStatics.HTTP_RESPONSE);
		response.setContentType("text/xml;charset=utf-8");
		PrintWriter out = null;
		try {
			out = response.getWriter();
		} catch (IOException e) {
			GameBackLogger.SYS_LOG.error("err", e);
			throw new GameException(2, e);
		}
		out.write(s);
		out.flush();
		out.close();

	}

	public Map<String, Object> getAtt() {
		return att;
	}

	public void setAtt(Map<String, Object> att) {
		this.att = att;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	public void setSession(Map<String, Object> att) {
		this.att = att;
	}

	public void setServletRequest(HttpServletRequest request) {
		this.request = request;

	}

	public void setServletResponse(HttpServletResponse response) {
		this.response = response;
	}

	protected String getString(String param, String def) {
		String val = getString(param);
		if (StringUtils.isBlank(val)) {
			return def;
		}
		return val;
	}

	protected String getString(String param) {
		return getRequest().getParameter(param);
	}

	protected Integer getInt(String param) {
		return Integer.parseInt(getString(param));
	}

	protected Integer getInt(String param, int def) {
		String val = getString(param);
		if (StringUtils.isBlank(val)) {
			return def;
		}
		return Integer.parseInt(val);
	}

	protected Long getLong(String param) {
		return Long.parseLong(getString(param));
	}

	protected Long getLong(String param, long def) {
		String val = getString(param);
		if (StringUtils.isBlank(val)) {
			return def;
		}
		return Long.parseLong(val);
	}

	/**
	 * 易玩渠道号
	 * 
	 * @param openId
	 * @return
	 */
	protected String getYiwanChannelId(String openId) {
		if (StringUtils.isBlank(openId) || openId.length() < 4) {
			return null;
		}
		String result = "";
		String code = openId.substring(0, 4);
		Pattern pattern = Pattern.compile("[0-9]*");
		if (!pattern.matcher(code).matches()) {// userId
			return null;
		}
		switch (Integer.parseInt(code)) {
		case 1038:
			result = "m360";
			break;
		case 1029:
			result = "uc";
			break;
		case 1033:
			result = "xiaomi";
			break;
		case 1007:
			result = "baidu";
			break;
		case 1031:
			result = "oppo";
			break;
		case 1045:
			result = "vivo";
			break;
		case 1043:
			result = "kupai";
			break;
		case 1014:
			result = "lenovo";
			break;
		case 1037:
			result = "m37wan";
			break;
		case 1002:
			result = "wandoujia";
			break;
		case 1006:
			result = "yinyonghui";
			break;
		case 1001:
			result = "dangle";
			break;
		case 1028:
			result = "anzhi";
			break;
		case 1005:
			result = "pps";
			break;
		case 1003:
			result = "jifen";
			break;
		case 1046:
			result = "muzhiwan";
			break;
		case 1079:
			result = "huawei";
			break;
		default:
			result = "com";
			break;
		}
		return result;
	}

	public boolean checkPdkSign() {
		String sytime = this.getString("sytime");
		String sign = this.getString("sysign");
//		String md5 = MD5Util.getStringMD5(sytime + "7HGO4K61M8N2D9LARSPU");
		String s1 = sytime + "7HGO4K61M8N2D9LARSPU";
		String s2 = sytime + BjdUtil.sign_key_new;
		if (BjdUtil.useNewSignKey()) {
			return MD5Util.getStringMD5(s2).equals(sign);
		} else {
			if (MD5Util.getStringMD5(s1).equals(sign) || MD5Util.getStringMD5(s2).equals(sign)) {
				return true;
			} else {
				if (Md5CheckUtil.checkHttpMd5(request)) {
					return true;
				}
				return false;
			}
		}

	}

	public boolean checkPdkSign(Map<String, String> map) {
		String sytime = map.get("sytime");
		String sign = map.get("sysign");
//		String md5 = MD5Util.getStringMD5(sytime + "7HGO4K61M8N2D9LARSPU");
		String s1 = sytime + "7HGO4K61M8N2D9LARSPU";
		String s2 = sytime + BjdUtil.sign_key_new;
		if (BjdUtil.useNewSignKey()) {
			return MD5Util.getStringMD5(s2).equals(sign);
		} else {
			if (MD5Util.getStringMD5(s1).equals(sign) || MD5Util.getStringMD5(s2).equals(sign)) {
				return true;
			} else {
				if (Md5CheckUtil.checkHttpMd5(request)) {
					return true;
				}
				return false;
			}
		}
	}

	public static boolean checkHttpMd5(Map<String, String> map) {
		String sytime = map.get("sytime");
		String sign = map.get("sysign");
		String md5 = com.sy599.game.util.MD5Util.getStringMD5(sytime + "FD32RFCAFDSDASDF32", "utf-8");
		return md5.equals(sign);
	}

	/**
	 * 签名验证
	 *
	 * @param map
	 * @return
	 */
	protected static boolean checkSign(Map<String, String> map) {
		String sign = map.remove("sign");
		String timestamp = map.get("timestamp");

		if (org.apache.commons.lang3.StringUtils.isBlank(sign) || !NumberUtils.isDigits(timestamp) /**|| Math.abs(System.currentTimeMillis() - Long.parseLong(timestamp)) > 5 * 60 * 1000**/) {
			return false;
		}

		String[] objs = map.keySet().toArray(new String[0]);
		Arrays.sort(objs);

		StringBuilder sb = new StringBuilder();
		for (String obj : objs) {
			sb.append("&").append(obj).append("=").append(map.get(obj));
		}
		String s1 = sb.toString() + "&key=" + loadSignKey();
		String s2 = sb.toString() + "&key=" + BjdUtil.sign_key_new;
		if (BjdUtil.useNewSignKey()) {
			return sign.equalsIgnoreCase(com.sy.sanguo.common.util.request.MD5Util.getMD5String(s2));
		} else {
			return sign.equalsIgnoreCase(com.sy.sanguo.common.util.request.MD5Util.getMD5String(s1))
					|| sign.equalsIgnoreCase(com.sy.sanguo.common.util.request.MD5Util.getMD5String(s2));
		}
	}

	protected static String loadSignKey(){
		return "043a528a8fc7195876fc4d4c3eaa7d2e";
	}

	public void interfaceDeprecated(){
        OutputUtil.output(-2, "客户端版本过低，请更新最新版本", getRequest(), getResponse(), false);
        return;
    }

    public void response(Object obj) {
        OutputUtil.output(obj, getRequest(), getResponse(), null, false);
    }

    public void response(Object code, Object message) {
        OutputUtil.output(code, message, getRequest(), getResponse(), false);
    }

}
