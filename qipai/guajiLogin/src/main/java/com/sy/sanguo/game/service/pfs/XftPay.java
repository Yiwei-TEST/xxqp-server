package com.sy.sanguo.game.service.pfs;

import com.alibaba.fastjson.JSONObject;
import com.sy.mainland.util.*;
import com.sy.mainland.util.Base64Util;
import com.sy.mainland.util.MD5Util;
import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.server.GameServerManager;
import com.sy.sanguo.common.server.PayBean;
import com.sy.sanguo.common.util.*;
import com.sy.sanguo.common.util.UrlParamUtil;
import com.sy.sanguo.game.bean.OrderValidate;
import com.sy.sanguo.game.bean.OvaliMsg;
import com.sy.sanguo.game.bean.PfSdkConfig;
import com.sy.sanguo.game.bean.RegInfo;
import com.sy.sanguo.game.pdkuai.util.LogUtil;
import com.sy.sanguo.game.service.BaseSdk;
import com.sy.sanguo.game.staticdata.PfCommonStaticData;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 星富通 小程序支付
 */
public class XftPay extends BaseSdk {

    @Override
    public String payExecute() {
        String result = "fail";
        try {
            Map<String, String> params = UrlParamUtil.getParameters(request);
            if (!params.containsKey("out_trade_no")){
                String str = NetTool.receivePost(getRequest());
                LogUtil.i("body:" + str);
                JSONObject json = JSONObject.parseObject(str);
                for (Map.Entry<String,Object> kv : json.entrySet()){
                    if (kv.getValue() != null && StringUtils.isNotBlank(kv.getValue().toString().trim())) {
                        params.put(kv.getKey(), kv.getValue().toString());
                    }
                }
            }
            LogUtil.i("params:" + params);

            request.setCharacterEncoding("utf-8");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-type", "text/html;charset=UTF-8");

            String state = params.get("state");

//            String customerid=params.get("merchant_code");
            String sd51no = params.get("third_trade_no");
            String sdcustomno = params.get("out_trade_no");
//            String signType=params.get("sign_type");
            String orderMoney = params.get("amount");//单位 分

            OrderValidate ov = orderValiDao.getOne(sdcustomno);
            if (ov == null || ov.getStatus() != 0) {
                GameBackLogger.SYS_LOG.error(pf + " payExecute orderVali is null");
                return "fail";
            }

            if (ov.getAmount() * 100 != new BigDecimal(orderMoney).intValue()) {
                GameBackLogger.SYS_LOG.error(pf + " payExecute total_fee is err " + orderMoney + "ov.getAmount() = " + ov.getAmount());
                return "fail";
            }

            String pf = ov.getPf();
            String[] payChannel = ov.getPay_channel().split(",");
            String channel = StringUtil.getValue(payChannel, 0);

            PfSdkConfig config = PfCommonStaticData.getConfig(payChannel.length == 2 ? payChannel[1] : payChannel[0]);

            if (config != null) {
                String key = config.getPayKey();
                String[] keys = params.keySet().toArray(new String[0]);
                Arrays.sort(keys);
                StringBuilder strBuilder = new StringBuilder();
                for (String tempK : keys) {
                    if (!"sign".equals(tempK)  && StringUtils.isNotBlank(params.get(tempK))) {
                        strBuilder.append("&").append(tempK).append("=").append(params.get(tempK));
                    }
                }
                strBuilder.append("&").append("key").append("=").append(key);

                String sign = MD5Util.getMD5String(strBuilder.substring(1));

                if (sign.equalsIgnoreCase(params.get("sign"))) {

                    if ("00".equals(state)) {

                        if (!StringUtils.isBlank(channel) && !channel.equals("null")) {
                            pf = pf + channel;
                        }

                        PayBean bean = GameServerManager.getInstance().getPayBean(ov.getItem_id());
                        RegInfo userInfo;
                        int code;
                        if (ov.getAgencyUserId() > 0) {
                            RegInfo user = userDao.getUser(ov.getAgencyUserId());
                            RegInfo user0;
                            if (ov.getUserId() != null && ov.getUserId().longValue() > 0L) {
                                user0 = userDao.getUser(ov.getUserId());
                            } else {
                                user0 = userDao.getUser(ov.getFlat_id(), ov.getPf());
                            }

                            code = payCards(user, ov, sd51no, bean, String.valueOf(user0.getUserId()));
                            userInfo = user;
                        } else {
                            if (ov.getUserId() != null && ov.getUserId().longValue() > 0L) {
                                userInfo = userDao.getUser(ov.getUserId());
                            } else {
                                userInfo = userDao.getUser(ov.getFlat_id(), ov.getPf());
                            }
                            code = payCards(userInfo, ov, sd51no, bean);
                        }

                        switch (code) {
                            case 1:
                                result = "SUCCESS";
                                break;
                            case 2:
                                result = "FAIL";
                                break;
                            case 3:
                                result = "FAIL";
                                break;
                            case 0:// 充值成功
                                result = "SUCCESS";
                                break;
                            case -1:
                                result = "FAIL";
                                break;
                        }
                        if (code == 0) {
                            insertRoomCard(ov, bean, sd51no, userInfo);
                        }

                        GameBackLogger.SYS_LOG.info(pf + " pay orderid:" + sd51no + ",code:" + code);
                    }
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

        String payForbidPfs = PropertiesCacheUtil.getValue("pay_forbid_pfs", Constants.GAME_FILE);
        if (StringUtils.contains(payForbidPfs, "|" + payType + "|")) {
            result.put("code", "111");
            result.put("msg", "发起订单失败");
            GameBackLogger.SYS_LOG.warn("pay_forbid_pfs:" + payForbidPfs + ",payType=" + payType);
            return JacksonUtil.writeValueAsString(result);
        }

        OvaliMsg msg = ovaliComMsg();
        if (msg.getCode() == 0) {
            try {
                PayBean bean = msg.getPayItem();
                if (bean == null) {
                    result.put("code", -4);
                    return JacksonUtil.writeValueAsString(result);
                }
                PfSdkConfig config = PfCommonStaticData.getConfig(payType);
                if (config != null) {
                    JSONObject jsonObject = JSONObject.parseObject(config.getExtStr());
                    Map<String, Object> urlMap = new HashMap<>();
                    String path = jsonObject.getString("path");
                    if (path == null) {
                        path = "";
                    }
                    if (path.contains("?")) {
                        path += "&amount=" + (bean.getAmount() * 100);
                    } else {
                        path += "?amount=" + (bean.getAmount() * 100);
                    }
                    path += "&merchant_code=" + config.getMch_id();

                    Map<String, String> extMap = new LinkedHashMap<>();
                    extMap.put("merchant_user_id", MD5Util.getMD5String(msg.getOv().getFlat_id()));
                    extMap.put("app_id", jsonObject.getString("userName"));
                    extMap.put("out_trade_no", msg.getOv().getOrder_id());

                    path += "&extra=" + Base64Util.encodeUTF8(JSONObject.toJSONString(extMap));

                    urlMap.put("pay_info", path);
                    urlMap.put("appId", jsonObject.getString("appId"));
                    urlMap.put("userName", jsonObject.getString("userName"));
                    result.put("url", urlMap);

                    LogUtil.i("create order:payType=" + payType + ",url=" + urlMap);

                    result.put("payType", payType);
                    result.put("code", 0);
                } else {
                    result.put("code", -1);
                }
            } catch (Exception e) {
                GameBackLogger.SYS_LOG.error(pf + "ovali err:" + e.getMessage(), e);
            }
        } else {
            result.put("code", msg.getCode());
            result.put("msg", msg.getMsg());
        }
        return JacksonUtil.writeValueAsString(result);
    }

    @Override
    public String loginExecute() {
        return null;
    }

}
