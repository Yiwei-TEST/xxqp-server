package com.sy.sanguo.common.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sy.mainland.util.PropertiesCacheUtil;
import com.sy.sanguo.common.init.InitData;
import com.sy.sanguo.game.utils.HttpDataUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * 输出数据
 * 
 * @author Administrator
 *
 */
public class OutputUtil {

	public final static String DEFAULT_CHARSET = "UTF-8";

	private static final Logger logger = LoggerFactory.getLogger(OutputUtil.class);

	public final static void setResponseCharacterEncoding(HttpServletResponse response, String charset) {
		response.setContentType(new StringBuilder(30).append("text/html;charset=").append(charset).toString());
		response.setCharacterEncoding(charset);
	}

	/**
	 * 输出数据
	 * 
	 * @param object
	 * @param request
	 * @param response
	 * @param crossdomain
	 * @param isWeb
	 *            web平台默认拼接jsoncallback
	 * @throws Exception
	 */
	public final static void output(Object object, HttpServletRequest request, HttpServletResponse response,
                                    String crossdomain, boolean isWeb) {
		String value = null;
		try {
			setResponseCharacterEncoding(response, DEFAULT_CHARSET);

			if (crossdomain == null) {
				crossdomain = request.getParameter("crossdomain");
			}

			if (object == null) {
				object = "";
			}

			
			if (isWeb) {
				if ("false".equalsIgnoreCase(crossdomain)) {
					value = object.toString();
				} else {
					value = getCallBack(object, request.getParameter("jsoncallback"));
				}
			} else {
				if ("true".equalsIgnoreCase(crossdomain)) {
					value = getCallBack(object, request.getParameter("jsoncallback"));
				} else {
					value = object.toString();
				}
			}

			String temp = LoginCacheContext.getTipWithCard();
			if ("1".equals(temp)){
				if (value.contains("钻石")) {
					value = value.replace("钻石","房卡");
				}
				if (value.contains("俱乐部")) {
					if(InitData.groupToQinYouQuan == 1)
						value = value.replace("俱乐部","亲友圈");
					else
						value = value.replace("俱乐部","军团");
				}
			}else{
				if (value.contains("房卡")) {
					value = value.replace("房卡","钻石");
				}
				if (value.contains("军团")) {
					if(InitData.groupToQinYouQuan == 1)
						value = value.replace("军团","亲友圈");
					else
						value = value.replace("军团","俱乐部");
				}
			}
			if (HttpDataUtil.isSwitchOn() && !isWeb && !"true".equalsIgnoreCase(crossdomain)) {
				value = HttpDataUtil.aesEncryptHttpData(value);
			}

			PrintWriter pw = response.getWriter();
			pw.write(value);
			pw.flush();
			pw.close();
			
		} catch (Exception e) {
			logger.error("Exception Message:" + e.getMessage(), e);
		} finally {
			if ("1".equals(PropertiesCacheUtil.getValue("print_output_log",Constants.GAME_FILE))){
				logger.info(value);
			}
		}

	}

	/**
	 * 输出数据(默认有序输出)
	 * 
	 * @param code
	 * @param message
	 * @param request
	 * @param response
	 * @param isWeb
	 *            web平台默认拼接jsoncallback
	 */
	public final static void output(Object code, Object message, HttpServletRequest request,
                                    HttpServletResponse response, boolean isWeb) {
		output(true, code, message, request, response, isWeb);
	}

	public final static void outputJson(int code, JSONObject json, HttpServletRequest request, HttpServletResponse response, boolean isWeb) {
		json.put("code",code);
		output(json, request, response, null, isWeb);
	}

	public final static void outputJson(JSONObject json, HttpServletRequest request, HttpServletResponse response, boolean isWeb) {
		output(json, request, response, null, isWeb);
	}

	public final static void outputJsonArray(JSONArray json, HttpServletRequest request, HttpServletResponse response, boolean isWeb) {
		output(json, request, response, null, isWeb);
	}

	/**
	 * 输出数据
	 * 
	 * @param ordered
	 * @param code
	 * @param message
	 * @param request
	 * @param response
	 * @param isWeb
	 *            web平台默认拼接jsoncallback
	 */
	public final static void output(boolean ordered, Object code, Object message, HttpServletRequest request,
                                    HttpServletResponse response, boolean isWeb) {
		output(MessageBuilder.newInstance(ordered).builderCodeMessage(code, message), request, response, null, isWeb);
	}

	/**
	 * 处理要输出的数据
	 * 
	 * @param object
	 * @param jsoncallback
	 * @return
	 */
	private final static String getCallBack(Object object, String jsoncallback) {
		return new StringBuilder(100).append(jsoncallback).append("([").append(object.toString()).append("])")
				.toString();
	}
	
	public enum Result{
		SUCCESS,WARN,FAILURE,ERROR,EXCEPTION
	}
}
