package com.sy.sanguo.common.util;

import com.alibaba.fastjson.JSON;
import com.sy.sanguo.game.utils.HttpDataUtil;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class UrlParamUtil {
    public static final String DEFAULT_CHARSET = "UTF-8";

    /**
     * 获取url参数(UTF-8)
     *
     * @param request
     * @return
     * @throws UnsupportedEncodingException
     */
    public static final Map<String, String>  getParameters(HttpServletRequest request) throws UnsupportedEncodingException {
        return getParameters(request, DEFAULT_CHARSET);
    }

    /**
     * 获取url参数
     *
     * @param request
     * @param charset
     * @return
     * @throws UnsupportedEncodingException
     */
    public static final Map<String, String> getParameters(HttpServletRequest request, String charset) throws UnsupportedEncodingException {
        Map<String, String> paramMap = new HashMap<>();
        Enumeration<String> paramNames = request.getParameterNames();
        String key;
        String paramName;
        String paramValue;
        while (paramNames.hasMoreElements()) {
            paramName = paramNames.nextElement();
            paramValue = request.getParameter(paramName);
            if (paramValue == null) {
                paramValue = "";
            }
            String httpDataAESId = HttpDataUtil.getHttpDataAESId();
            if (HttpDataUtil.isSwitchOn() && StringUtils.isNotBlank(httpDataAESId) && paramName.endsWith(httpDataAESId)) {
                key = paramName.substring(0, paramName.length() - httpDataAESId.length());
                Map<String, Object> data = (Map<String, Object>) JSON.parse(HttpDataUtil.aesDecryptHttpData(key));
                if (data != null) {
                    for (Map.Entry<String, Object> entry : data.entrySet()) {
                        paramMap.put(entry.getKey(), entry.getValue() == null ? "" : entry.getValue().toString());
                    }
                }
            } else {
                paramMap.put(CoderUtil.decode(paramName), CoderUtil.decode(paramValue, charset));
            }
        }
        return paramMap;
    }
}
