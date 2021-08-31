package com.sy.sanguo.game.service.pfs;

import com.sy.mainland.util.PropertiesCacheUtil;
import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.server.GameServerManager;
import com.sy.sanguo.common.server.PayBean;
import com.sy.sanguo.common.util.Constants;
import com.sy.sanguo.common.util.JacksonUtil;
import com.sy.sanguo.common.util.StringUtil;
import com.sy.sanguo.common.util.UrlParamUtil;
import com.sy.sanguo.common.util.request.CoderUtil;
import com.sy.sanguo.game.bean.OrderValidate;
import com.sy.sanguo.game.bean.OvaliMsg;
import com.sy.sanguo.game.bean.PfSdkConfig;
import com.sy.sanguo.game.bean.RegInfo;
import com.sy.sanguo.game.pdkuai.util.LogUtil;
import com.sy.sanguo.game.service.BaseSdk;
import com.sy.sanguo.game.staticdata.PfCommonStaticData;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedInputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * h5掌宜付支付
 */
public class H5Zyf extends BaseSdk {

    @Override
    public String payExecute() {
        String result = "fail";
        try {
            Map<String, String> params = UrlParamUtil.getParameters(request);
            LogUtil.i("params:" + params);

            request.setCharacterEncoding("utf-8");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-type", "text/html;charset=UTF-8");

            String retSign = params.remove("sign");
            String status = params.get("code");
            if ("0".equals(status)) {
                String transaction_id = params.get("invoice_no");
                String out_trade_no = params.get("out_trade_no");
                String total_fee = params.get("money");
                OrderValidate ov = orderValiDao.getOne(out_trade_no);
                if (ov == null || ov.getStatus() != 0) {
                    GameBackLogger.SYS_LOG.error(pf + " payExecute orderVali is null");
                    return "fail";
                }

                if (ov.getAmount() * 100 != Integer.parseInt(total_fee)) {
                    GameBackLogger.SYS_LOG.error(pf + " payExecute total_fee is err" + total_fee);
                    return "fail";
                }

                String pf = ov.getPf();
                String[] payChannel = ov.getPay_channel().split(",");
                String channel = StringUtil.getValue(payChannel, 0);

                PfSdkConfig config = PfCommonStaticData.getConfig(payChannel.length == 2 ? payChannel[1] : payChannel[0]);

                String sign0 = loadSign(params, config);

                if (sign0.equalsIgnoreCase(retSign)) {
                    if (!StringUtils.isBlank(channel) && !channel.equals("null")) {
                        pf = pf + channel;
                    }

                    PayBean bean = GameServerManager.getInstance().getPayBean(ov.getItem_id());
                    RegInfo userInfo;
                    int code;
                    if (ov.getAgencyUserId() > 0) {
                        RegInfo user = userDao.getUser(ov.getAgencyUserId());
                        RegInfo user0 ;
                        if (ov.getUserId()!=null&&ov.getUserId().longValue()>0L){
                            user0 = userDao.getUser(ov.getUserId());
                        }else{
                            user0 = userDao.getUser(ov.getFlat_id(), ov.getPf());
                        }

                        code = payCards(user, ov, transaction_id, bean, String.valueOf(user0.getUserId()));
                        userInfo = user;
                    } else {
                        if (ov.getUserId()!=null&&ov.getUserId().longValue()>0L){
                            userInfo = userDao.getUser(ov.getUserId());
                        }else{
                            userInfo = userDao.getUser(ov.getFlat_id(), ov.getPf());
                        }
                        code = payCards(userInfo, ov, transaction_id, bean);
                    }

                    switch (code) {
                        case 1:
                            result = "0";
                            break;
                        case 2:
                            result = "FAIL";
                            break;
                        case 3:
                            result = "FAIL";
                            break;
                        case 0:// 充值成功
                            result = "0";
                            break;
                        case -1:
                            result = "FAIL";
                            break;
                    }
                    if (code == 0) {
                        insertRoomCard(ov, bean, transaction_id, userInfo);
                    }

                    GameBackLogger.SYS_LOG.info(pf + " pay orderid:" + transaction_id + ",code:" + code);
                }
            }
        } catch (Exception e) {
            GameBackLogger.SYS_LOG.error(pf + " pay err:", e);
            return "fail";
        }
        return result;
    }

    @Override
    public String ovali() {
        Map<String, Object> result = new HashMap<String, Object>();

        String payForbidPfs = PropertiesCacheUtil.getValue("pay_forbid_pfs",Constants.GAME_FILE);
        if (StringUtils.contains(payForbidPfs,"|"+payType+"|")){
            result.put("code", "111");
            result.put("msg", "发起订单失败");
            GameBackLogger.SYS_LOG.warn("pay_forbid_pfs:"+payForbidPfs+",payType="+payType);
            return JacksonUtil.writeValueAsString(result);
        }

        OvaliMsg msg = ovaliComMsg();
        if (msg.getCode() == 0) {
            try {
                String total_fee = this.getString("total_fee");

                //pay!webzyf

                PayBean bean = msg.getPayItem();
                if (bean == null) {
                    result.put("code", -4);
                    return JacksonUtil.writeValueAsString(result);
                }
                PfSdkConfig config = PfCommonStaticData.getConfig(payType);
                if (config != null) {
                    Map<String, String> map = new HashMap<>();
                    map.put("money", total_fee);
                    map.put("subject", bean.getName());
                    map.put("orderId", msg.getOv().getOrder_id());

                    String payChannel=msg.getOv().getPay_channel();
                    if (StringUtils.contains(payChannel,",")){
                        payChannel = payChannel.split(",")[0];
                    }
                    if ("null".equals(payChannel)){
                        payChannel="";
                    }
                    map.put("payChannel",payChannel);

                    String ip = getIpAddr();
                    map.put("client_ip",ip);
                    map.put("return_url",loadRootUrl(getRequest()));
                    map.put("wxa_jump","1");
                    if(config.getPf().endsWith("ipa")) {
                    	map.put("is_h5","1");                    	
                    }

                    Map<String, Object> urlMap = new HashMap<>();
                    String url=loadUrl(map, config);

                    Map<String,String> headersMap=new HashMap<>();

                    headersMap.put("X-Real-IP",ip);
                    headersMap.put("X-Forwarded-For",ip);

                    String ret=getUrlReturnValue(url,"UTF-8","GET",null,null,headersMap,2);
//                    if (StringUtils.isNotBlank(ret)){
//                        url=ret;
//                    }
                    LogUtil.i("create order url:"+url+",result="+ret);
                    HashMap<String, String> awardMap = JacksonUtil.readValue(ret, HashMap.class);
                    String path = awardMap.get("reqPath").toString()+"?orderNo="+awardMap.get("orderNo").toString()+"&money="+awardMap.get("money").toString()+"&body="+awardMap.get("body").toString();
                    urlMap.put("pay_info", path);
                    urlMap.put("userName",awardMap.get("reqUserName").toString());
                    urlMap.put("openAppId",awardMap.get("openAppId").toString());
                    result.put("url", urlMap);
                    result.put("code", 0);
                } else {
                    result.put("code", -1);
                }
            } catch (Exception e) {
                GameBackLogger.SYS_LOG.error(pf + "ovali err:", e);
            }

        } else {
            result.put("code", msg.getCode());
            result.put("msg", msg.getMsg());
        }
        return JacksonUtil.writeValueAsString(result);
    }

    @Override
    protected void buildOrderVali(OrderValidate ov) {
//        String total_fee = this.getString("total_fee");
//        PayBean bean = GameServerManager.getInstance().getPayBeanByAmount(Integer.parseInt(total_fee) / 100);
//        if (bean == null) {
//            return;
//        }
//        ov.setItem_id(bean.getId());
//        ov.setAmount(bean.getAmount());
    }

    /**
     * 获取返回给前端的url
     *
     * @param params
     * @param config
     * @return
     * @throws Exception
     */
    private static final String loadUrl(Map<String, String> params, PfSdkConfig config) throws Exception {
        StringBuilder strBuilder = new StringBuilder("http://pay.cxsh2017.cn:8000/createOrder.e?");

        String payChannel = params.get("payChannel");
        Map<String, String> map = new TreeMap<>();
        map.put("partner_id", config.getMch_id());
        map.put("app_id", config.getAppId());
        map.put("wap_type", "alipay".equalsIgnoreCase(payChannel)||config.getPf().contains("alipay")?"2":"12");
        map.put("money", params.get("money"));
        map.put("subject", params.get("subject"));
        map.put("qn", config.getPf());
        map.put("out_trade_no", params.get("orderId"));
        map.put("client_ip",params.get("client_ip"));
        map.put("wxa_jump",params.get("wxa_jump"));
        map.put("is_h5",params.get("is_h5"));
        String return_url=params.get("return_url");
        if (return_url!=null){
            map.put("return_url",return_url);
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, String> kv : map.entrySet()) {
            if (StringUtils.isNotBlank(kv.getValue())) {
                stringBuilder.append(kv.getKey()).append("=").append(URLEncoder.encode(kv.getValue(), "UTF-8")).append("&");
            }
        }
        strBuilder.append(stringBuilder.toString());
        stringBuilder.append("key=").append(config.getPayKey());

        String sign = com.sy.sanguo.common.util.request.MD5Util.getMD5String(stringBuilder);
        strBuilder.append("sign=").append(sign);
        return strBuilder.toString();
    }

    /**
     * 签名验证
     *
     * @param params
     * @param config
     * @return
     * @throws Exception
     */
    private static final String loadSign(Map<String, String> params, PfSdkConfig config) throws Exception {
        Map<String, String> map = new TreeMap<>();
        map.putAll(params);

        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, String> kv : map.entrySet()) {
            if (StringUtils.isNotBlank(kv.getValue())) {
                stringBuilder.append(kv.getKey()).append("=").append(URLEncoder.encode(kv.getValue(), "UTF-8")).append("&");
            }
        }

        stringBuilder.append("key=").append(config.getPayKey());

        return com.sy.sanguo.common.util.request.MD5Util.getMD5String(stringBuilder);
    }

    @Override
    public String loginExecute() {
        return null;
    }

    public static void main(String[] args){
        try {
            Map<String, String> map = new HashMap<>();
            map.put("money", "1");
            map.put("subject", "2");
            map.put("orderId", System.currentTimeMillis() + "");
            map.put("client_ip","127.0.0.1");

            Map<String, Object> urlMap = new HashMap<>();
            PfSdkConfig config = new PfSdkConfig();
            config.setPf("webzyfdtz");
            config.setMch_id("1000100020001303");
            config.setAppId("3864");
            config.setPayKey("9D8CF6A9A6F248859AB12C1D7AF953CD");
            String url = loadUrl(map, config);
            String ret = getUrlReturnValue(url, "UTF-8", "GET", null, null, null, 2);
            System.out.println(ret);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static final String getUrlReturnValue(String urlAddress, String charset, String method,
                                                 Map<String, String> paramsMap, List<String> paramsKeyList, Map<String, String> requestPropertiesMap, int seconds) {
        if (urlAddress == null) {
            throw new IllegalArgumentException("param 'urlAddress' is required");
        }

        StringBuilder paramBuilder = null;

        boolean isSource = false;
        if (paramsMap != null && paramsMap.size() == 1 && paramsMap.get("$") != null) {
            isSource = true;
            paramBuilder = new StringBuilder(paramsMap.get("$"));
        }
        HttpURLConnection conn = null;
        int tempCode = -1;
        try {
            URL url;

            if (!isSource) {
                int index = urlAddress.indexOf("?");
                if (index != -1 && urlAddress.length() > index + 1) {
                    paramBuilder = new StringBuilder("&");
                    if (urlAddress.indexOf("=") == -1) {
                        String paramStr = CoderUtil.decode(urlAddress.substring(index + 1), charset);
                        String[] params = StringUtils.isNotEmpty(paramStr)
                                ? paramStr.split("&(?=[a-zA-Z_]{1}[\\w]*\\=[\\s\\S]*(?=&|$))") : new String[0];
                        for (String param : params) {
                            int idx = param.indexOf("=");
                            if (idx != -1)
                                paramBuilder.append("&").append(param.substring(0, idx)).append("=")
                                        .append(CoderUtil.encode(param.substring(idx + 1), charset));
                        }
                        if (paramBuilder.length() > 1) {
                            paramBuilder.deleteCharAt(0);
                        }
                    } else {
                        paramBuilder.append(urlAddress.substring(index + 1));
                    }
                    urlAddress = urlAddress.substring(0, index);
                }
                if (paramsMap != null && paramsMap.size() > 0) {
                    if (paramBuilder == null) {
                        paramBuilder = new StringBuilder();
                    }
                    if (paramsKeyList != null && paramsKeyList.size() == paramsMap.size()) {
                        for (String paramKey : paramsKeyList) {
                            String tempValue = paramsMap.get(paramKey);
                            if (tempValue == null) {
                                tempValue = "";
                            }
                            paramBuilder.append("&").append(paramKey).append("=")
                                    .append(CoderUtil.encode(tempValue, charset));
                        }
                    } else {
                        for (Map.Entry<String, String> keyValue : paramsMap.entrySet()) {
                            String tempValue = keyValue.getValue();
                            if (tempValue == null) {
                                tempValue = "";
                            }
                            paramBuilder.append("&").append(keyValue.getKey()).append("=")
                                    .append(CoderUtil.encode(tempValue, charset));
                        }
                    }
                }
            }

            final int millis = seconds * 1000;
            if ("POST".equalsIgnoreCase(method)) {
                url = new URL(urlAddress);
                conn = (HttpURLConnection) url.openConnection();
                if (requestPropertiesMap != null) {
                    for (Map.Entry<String, String> keyValue : requestPropertiesMap.entrySet()) {
                        String tempValue = keyValue.getValue();
                        if (tempValue == null) {
                            tempValue = "";
                        }
                        conn.addRequestProperty(keyValue.getKey(), tempValue);
                    }
                }
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setConnectTimeout(millis);// 设置连接超时
                // 如果在建立连接之前超时期满，则会引发一个
                // java.net.SocketTimeoutException。超时时间为零表示无穷大超时。
                conn.setReadTimeout(millis);// 设置读取超时
                conn.setRequestMethod("POST");
                conn.setInstanceFollowRedirects(false);
                if (paramBuilder != null) {
                    if (!isSource)
                        while (paramBuilder.indexOf("&") == 0) {
                            paramBuilder.deleteCharAt(0);
                        }
                    String temp = paramBuilder.toString();
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);
                    conn.connect();
                    OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream(), charset);
                    osw.write(temp);
                    osw.flush();
                    osw.close();
                } else {
                    conn.connect();
                }
            } else {
                if (paramBuilder != null) {
                    while (paramBuilder.indexOf("&") == 0) {
                        paramBuilder.deleteCharAt(0);
                    }
                    paramBuilder.insert(0, "?");
                    paramBuilder.insert(0, urlAddress);
                    urlAddress = paramBuilder.toString();
                }

                url = new URL(urlAddress);
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(millis);// 设置连接超时
                // 如果在建立连接之前超时期满，则会引发一个
                // java.net.SocketTimeoutException。超时时间为零表示无穷大超时。
                conn.setReadTimeout(millis);// 设置读取超时
                conn.setRequestMethod("GET");
                conn.setInstanceFollowRedirects(false);
                if (requestPropertiesMap != null) {
                    for (Map.Entry<String, String> keyValue : requestPropertiesMap.entrySet()) {
                        String tempValue = keyValue.getValue();
                        if (tempValue == null) {
                            tempValue = "";
                        }
                        conn.addRequestProperty(keyValue.getKey(), tempValue);
                    }
                }
                conn.connect();
            }

            tempCode = conn.getResponseCode();

            String location=conn.getHeaderField("Location");
            if (StringUtils.isNotBlank(location)&&tempCode==302){
                return location;
            }

            BufferedInputStream bis;
            if (tempCode == 200) {
                bis = new BufferedInputStream(conn.getInputStream(), 1024);
            } else {
                bis = new BufferedInputStream(conn.getErrorStream(), 1024);
            }

            int length = -1;
            StringBuilder result = new StringBuilder();
            byte[] buf = new byte[1024];
            while ((length = bis.read(buf)) != -1) {
                result.append(new String(buf, 0, length, charset));
            }

            bis.close();
            bis = null;
            buf = null;

            if (tempCode == 200) {
                return result.toString();
            } else {

            }
        } catch (Exception e) {
            LogUtil.e("Exception:"+e.getMessage(),e);
        } finally {
            if (conn != null) {
                try {
                    conn.disconnect();
                } catch (Exception e) {
                }
                conn = null;
            }
        }

        return null;
    }

}
