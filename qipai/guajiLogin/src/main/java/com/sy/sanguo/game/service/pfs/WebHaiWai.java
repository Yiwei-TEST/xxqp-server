package com.sy.sanguo.game.service.pfs;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sy.mainland.util.HttpUtil;
import com.sy.mainland.util.MD5Util;
import com.sy.mainland.util.PropertiesCacheUtil;
import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.server.GameServerManager;
import com.sy.sanguo.common.server.PayBean;
import com.sy.sanguo.common.util.Constants;
import com.sy.sanguo.common.util.JacksonUtil;
import com.sy.sanguo.common.util.MathUtil;
import com.sy.sanguo.common.util.UrlParamUtil;
import com.sy.sanguo.game.bean.OrderValidate;
import com.sy.sanguo.game.bean.OvaliMsg;
import com.sy.sanguo.game.bean.PfSdkConfig;
import com.sy.sanguo.game.bean.RegInfo;
import com.sy.sanguo.game.pdkuai.util.LogUtil;
import com.sy.sanguo.game.service.BaseSdk;
import com.sy.sanguo.game.staticdata.PfCommonStaticData;
import com.sy599.sanguo.util.TimeUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.StringReader;
import java.security.MessageDigest;
import java.util.*;
import java.text.SimpleDateFormat;

public class WebHaiWai extends BaseSdk {
    @Override
    public String payExecute() {
        String result = "fail";

        return result;
    }

    /**
     * 下单验证通用消息处理
     *
     * @return
     */
    protected OvaliMsg ovaliComMsg() {
        OvaliMsg msg = new OvaliMsg();
        try {
            Map<String,String> params=UrlParamUtil.getParameters(getRequest());

            LogUtil.i("create order params:"+params);

            String flat_id = getRequest().getParameter("flat_id");
            String server_id = getRequest().getParameter("server_id");
            String pf = getRequest().getParameter("p");
            int itemid = Integer.parseInt(getRequest().getParameter("itemid"));
            int amount = Integer.parseInt(getRequest().getParameter("amount"));
            String sign = getRequest().getParameter("k");
            String secret = "mwFLeKLzNoL46dDn0vE2";
            String c = getRequest().getParameter("c");

            // 获得被代充人的id
            String agencyUserId = getString("agencyUserId");
            String userId = getString("userId");//玩家Id
            String total_fee = this.getString("total_fee");

            StringBuilder md5 = new StringBuilder();
            md5.append(flat_id);
            md5.append(server_id);
            md5.append(pf);
            md5.append(itemid);
            md5.append(amount);
            md5.append(secret);

            if (StringUtils.isBlank(flat_id)) {
                msg.setCode(1);
                msg.setMsg("param flat_id error");
                return msg;
            }
            if (amount < 10 || amount > 2999) {
                msg.setCode(1);
                msg.setMsg("param amount error");
                return msg;
            }
            if (StringUtils.isBlank(pf)) {
                msg.setCode(1);
                msg.setMsg("param pf error");
                return msg;
            }
            if (StringUtils.isBlank(sign)) {
                msg.setCode(1);
                msg.setMsg("param sign error");
                return msg;
            }
            if (!com.sy.sanguo.common.util.MD5Util.getStringMD5(md5.toString()).equals(sign)) {
                msg.setCode(1);
                msg.setMsg("param md5 err");
                return msg;
            }
            if (StringUtils.isBlank(total_fee)) {
                msg.setCode(1);
                msg.setMsg("param total_fee err");
                return msg;
            }
            long now = TimeUtil.currentTimeMillis();
            long time = now / 1000 + MathUtil.mt_rand(100, 999);
            String order_id = now + flat_id + server_id + pf + MathUtil.mt_rand(1000, 9999);
            order_id = com.sy.sanguo.common.util.MD5Util.getStringMD5(order_id).substring(8, 24);
            order_id += Long.toHexString(time);

            OrderValidate ov = new OrderValidate();
            long tempUserId;

            if (NumberUtils.isDigits(userId) && (tempUserId = Long.parseLong(userId)) > 0) {
                ov.setUserId(tempUserId);
                RegInfo regInfo = userDao.getUser(tempUserId);
                if (regInfo == null) {
                    msg.setCode(1);
                    msg.setMsg("ID错误");
                    return msg;
                }
            } else {
                RegInfo userInfo = userDao.getUser(flat_id, pf);
                if (userInfo != null) {
                    ov.setUserId(userInfo.getUserId());
                } else {
                    msg.setCode(1);
                    msg.setMsg("ID错误");
                    return msg;
                }
            }
            ov.setFlat_id(flat_id);
            ov.setOrder_id(order_id);
            ov.setServer_id(server_id);
            ov.setPf(pf);
            ov.setItem_id(1);
            ov.setAmount(amount);
            if (!StringUtils.isBlank(c)) {
                ov.setPay_channel(c);
            }
            if (!StringUtils.isBlank(payType)) {
                ov.setPay_channel(ov.getPay_channel() + "," + payType);
            }

            // 添加被代充人的id信息
            if (!StringUtils.isEmpty(agencyUserId)) {
                long auid = Long.parseLong(agencyUserId);
                if (auid > 0L && userDao.getUser(auid) == null) {
                    msg.setCode(1);
                    msg.setMsg("ID错误1");
                    return msg;
                }
                ov.setAgencyUserId(auid);
            }
            buildOrderVali(ov);
            orderValiDao.insert(ov);
            msg.setOv(ov);

        } catch (Exception e) {
            msg.setCode(999);
            msg.setMsg(e.getMessage());
            GameBackLogger.SYS_LOG.error(pf + ".exception", e);
        }
        return msg;
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
                PfSdkConfig config = PfCommonStaticData.getConfig(payType);
                if (config != null) {
//                    应用ID(app_id)  4401
//                    Appkey	e9e722a38c154224a4bd8c2c368ec726
//                    代付密钥   f5c40b88a82f4f62b1914c55a18c112f
                    String app_id = config.getAppId();
                    String pay_type = "2";//1=PC扫码支付，2=手机WAP支付,3=手机公众号支付，4=QQ钱包
                    String order_id = msg.getOv().getOrder_id();//String.valueOf(System.currentTimeMillis());
                    String order_amt = String.valueOf(msg.getOv().getAmount());
                    String notify_url = loadPayUrl(getRequest(),"support!ovali_com","pay!webhaiwai");
                    String return_url = loadRootUrl(getRequest());
                    String goods_name = order_amt+"金币";
                    String time_stamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                    String key = config.getAppKey();

                    StringBuilder orgSign = new StringBuilder();

                    orgSign.append("app_id").append("=").append(app_id).append("&");
                    orgSign.append("pay_type").append("=").append(pay_type).append("&");
                    orgSign.append("order_id").append("=").append(order_id).append("&");
                    orgSign.append("order_amt").append("=").append(order_amt).append("&");
                    orgSign.append("notify_url").append("=").append(notify_url).append("&");
                    orgSign.append("return_url").append("=").append(return_url).append("&");
                    orgSign.append("time_stamp").append("=").append(time_stamp).append("&");
                    orgSign.append("key").append("=").append(MD5(key));
                    String sign = MD5(orgSign.toString());
                    Map<String, String> map = new HashMap<>();
                    map.put("app_id", app_id);
                    map.put("pay_type", pay_type);
                    map.put("order_id", order_id);
                    map.put("order_amt", order_amt);
                    map.put("notify_url", java.net.URLEncoder.encode(notify_url, "utf-8"));
                    map.put("return_url", java.net.URLEncoder.encode(return_url, "utf-8"));
                    map.put("goods_name", java.net.URLEncoder.encode(goods_name, "utf-8"));
                    map.put("time_stamp", time_stamp);
                    map.put("sign", sign);

                    String url="http://bpayment.maiduopay.com/Pay/GateWayAliPay.aspx";
                    String ret=HttpUtil.getUrlReturnValue(url, "UTF-8","POST", map, 5);
                    LogUtil.i("create order url:"+url+",result="+ret);
                    JSONObject jsonMsg;
                    try {
                        jsonMsg = JSON.parseObject(ret);
                        if (jsonMsg==null||jsonMsg.size()==0){
                            result.put("code", -2);
                        } else {
                            if (jsonMsg.getIntValue("status_code") == 0) {
                                String pay_info = jsonMsg.getString("pay_url");
                                Map<String, Object> urlMap = new HashMap<>();
                                urlMap.put("pay_info", pay_info);
                                result.put("url", urlMap);
                                result.put("code", 0);
                            }else{
                                result.put("code", -1);
                            }
                        }
                    }catch (Exception e){
                        result.put("code", -3);
                    }
                } else {
                    result.put("code", -1);
                }
            } catch (Exception e) {
                GameBackLogger.SYS_LOG.error(pf + "ovali err:"+e.getMessage(), e);
            }
        } else {
            result.put("code", msg.getCode());
            result.put("msg", msg.getMsg());
        }
        return JacksonUtil.writeValueAsString(result);
    }

    public static String MD5(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(s.getBytes("utf-8"));
            return toHex(bytes).toLowerCase();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String toHex(byte[] bytes) {

        final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();
        StringBuilder ret = new StringBuilder(bytes.length * 2);
        for (int i=0; i<bytes.length; i++) {
            ret.append(HEX_DIGITS[(bytes[i] >> 4) & 0x0f]);
            ret.append(HEX_DIGITS[bytes[i] & 0x0f]);
        }
        return ret.toString();
    }

    @Override
    public String loginExecute() {
        return null;
    }
}
