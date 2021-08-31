
package com.sy.sanguo.common.util.huawei;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

public abstract class CommonUtil
{
    public static Map<String, Object> getValue(HttpServletRequest request) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            String key = "";
            String value = "";
            Iterator<String> it = request.getParameterMap().keySet().iterator();
            while (it.hasNext()) {
                key = it.next();
                value = ((Object[]) (request.getParameterMap().get(key)))[0]
                        .toString();
                map.put(key, value);
            }

        } catch (Exception e) {
            return null;
        }

        return map;
    }
	
    public static boolean rsaDoCheck(Map<String, Object> params, String sign, String publicKey)
    {
        //获取待签名字符串
        String content = RSA.getSignData(params);
        //验签
        return RSA.doCheck(content, sign, publicKey);
    }
    
}
