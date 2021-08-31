package com.sy.sanguo.game.service.pfs;

import com.alibaba.fastjson.JSONObject;
import com.sy.mainland.util.PropertiesCacheUtil;
import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.server.GameServerManager;
import com.sy.sanguo.common.server.PayBean;
import com.sy.sanguo.common.util.*;
import com.sy.sanguo.game.bean.OrderValidate;
import com.sy.sanguo.game.bean.OvaliMsg;
import com.sy.sanguo.game.bean.PfSdkConfig;
import com.sy.sanguo.game.bean.RegInfo;
import com.sy.sanguo.game.pdkuai.util.LogUtil;
import com.sy.sanguo.game.service.BaseSdk;
import com.sy.sanguo.game.service.pfs.webchanyou.HttpUtil;
import com.sy.sanguo.game.service.pfs.webchanyou.Util;
import com.sy.sanguo.game.staticdata.PfCommonStaticData;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class WebChanYou extends BaseSdk {

    @Override
    public String payExecute() {
        String result = "fail";
        Map<String, String> params = null;
        try {
            params = UrlParamUtil.getParameters(request);
            String goodsdesc = request.getParameter("goodsdesc");
            boolean bl=false;
            if (goodsdesc==null){
                goodsdesc="";
            }else{
                bl=goodsdesc.contains("?");
                if (bl){
                    goodsdesc=new String(goodsdesc.getBytes("ISO-8859-1"), "utf-8");
                    if (goodsdesc.contains("?")){
                        bl=false;
                        goodsdesc=params.get("goodsdesc");
                    }
                }
            }

            String respMsg = request.getParameter("respMsg");

            if (respMsg==null){
                respMsg="";
            }else{
                if (bl){
                    respMsg= new String(respMsg.getBytes("ISO-8859-1"), "utf-8");
                }else{
                    respMsg=params.get("respMsg");
                }
            }

            if (bl){
                params.put("goodsdesc", goodsdesc);
                params.put("respMsg", respMsg);
            }

            String payType = params.get("attach");

            request.setCharacterEncoding("utf-8");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-type", "text/html;charset=UTF-8");

            String retSign = params.get("sign");
            params.remove("sign");
            String status = params.get("respCode");
            if ("200".equals(status)) {

                String transaction_id = params.get("transId");
                String out_trade_no = params.get("mertransId");
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

                String signString = Util.getUrlData(params) + "&key=" + config.getPayKey();
                String sign0 = MD5Util.getStringMD5(signString);

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
                    } else {
                        userInfo = userDao.getUser(ov.getFlat_id(), ov.getPf());
                        }
                        code = payCards(userInfo, ov, transaction_id, bean);
                    }

                    switch (code) {
                        case 1:
                            result = "success";
                            break;
                        case 2:
                            result = "FAIL";
                            break;
                        case 3:
                            result = "FAIL";
                            break;
                        case 0:// 充值成功
                            result = "success";
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
        } finally {
            LogUtil.i("payExecute params:" + params);
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
                String url = "http://sdk.ksyouxi.com/pay/v1/getPayInfo?";
//				String nonce_str = RandomStringUtils.randomAlphabetic(10);
//                String requesturl = getRequest().getRequestURL().toString();
//                String notify_url = requesturl.replace("support!ovali_com", "pay!webchanyou");
                String notify_url =loadPayUrl(getRequest(),"support!ovali_com", "pay!webchanyou");
                String mch_create_ip = getIpAddr(request);

                // /////////////////////////////////////////////////////

//                PayBean bean = GameServerManager.getInstance().getPayBeanByAmount(Integer.parseInt(total_fee) / 100);
                PayBean bean = msg.getPayItem();
                if (bean == null) {
                    result.put("code", -4);
                    return JacksonUtil.writeValueAsString(result);
                }
                PfSdkConfig config = PfCommonStaticData.getConfig(payType);
                if (notify_url.contains("?")) {
                    notify_url = notify_url.substring(0, notify_url.indexOf("?"));
                }

                JSONObject item = new JSONObject();
                item.put("signtype", "MD5");
                item.put("txnType", "01");
                item.put("txnSubType", "010102");
                item.put("money", total_fee);
                item.put("merId", config.getMch_id());
                item.put("termIp", mch_create_ip);
                item.put("mertransId", msg.getOv().getOrder_id());
                item.put("mertransTime", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
                item.put("goodsdesc", bean.getName());
                item.put("backurl", "http://sdk.ksyouxi.com?out_trade_no=" + msg.getOv().getOrder_id());
                item.put("notifyurl", notify_url);
                item.put("attach", config.getPf());

                String signString = Util.getUrlData(item) + "&key=" + config.getPayKey();
                item.put("sign", MD5Util.getStringMD5(signString).toUpperCase());
                url += Util.getUrlData(item);

                String ret = HttpUtil.doGet(url, null);

                LogUtil.i("create order:url=" + url + ",result=" + ret);

                JSONObject res = JSONObject.parseObject(ret);

                if (res.containsKey("payInfo") && res.containsKey("sign")) {
                    String retSign = res.getString("sign");
                    res.remove("sign");
                    signString = Util.getUrlData(res) + "&key=" + config.getPayKey();
                    String sign0 = MD5Util.getStringMD5(signString);

                    if (sign0.equalsIgnoreCase(retSign)) {
                        String pay_info = res.get("payInfo").toString();

                        Map<String, Object> urlMap = new HashMap<>();
                        urlMap.put("pay_info", pay_info);
                        result.put("url", urlMap);
                        result.put("code", 0);

                    } else {
                        result.put("code", -1);
                    }
                } else {
                    result.put("code", -3);
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

    @Override
    public String loginExecute() {
        return null;
    }

}
