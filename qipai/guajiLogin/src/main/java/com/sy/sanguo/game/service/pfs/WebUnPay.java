package com.sy.sanguo.game.service.pfs;

import com.sy.mainland.util.HttpUtil;
import com.sy.mainland.util.MD5Util;
import com.sy.mainland.util.PropertiesCacheUtil;
import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.server.GameServerManager;
import com.sy.sanguo.common.server.PayBean;
import com.sy.sanguo.common.util.Constants;
import com.sy.sanguo.common.util.JacksonUtil;
import com.sy.sanguo.common.util.StringUtil;
import com.sy.sanguo.common.util.UrlParamUtil;
import com.sy.sanguo.game.bean.OrderValidate;
import com.sy.sanguo.game.bean.OvaliMsg;
import com.sy.sanguo.game.bean.PfSdkConfig;
import com.sy.sanguo.game.bean.RegInfo;
import com.sy.sanguo.game.pdkuai.util.LogUtil;
import com.sy.sanguo.game.service.BaseSdk;
import com.sy.sanguo.game.staticdata.PfCommonStaticData;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.StringReader;
import java.util.*;

/**
 * h5 unpay
 */
public class WebUnPay extends BaseSdk {

    @Override
    public String payExecute() {
        String result = "fail";
        try {
            Map<String, String> params = UrlParamUtil.getParameters(request);
            LogUtil.i("params:" + params);

            request.setCharacterEncoding("utf-8");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-type", "text/html;charset=UTF-8");

            String state=params.get("state");

            String customerid=params.get("customerid");
            String sd51no=params.get("sd51no");
            String sdcustomno=params.get("sdcustomno");
            String mark=params.get("mark");
            String ordermoney=params.get("ordermoney");

            OrderValidate ov = orderValiDao.getOne(sdcustomno);
            if (ov == null || ov.getStatus() != 0) {
                GameBackLogger.SYS_LOG.error(pf + " payExecute orderVali is null");
                return "fail";
            }

            if (ov.getAmount() * 100 != Math.round(Float.parseFloat(ordermoney)*100)) {
                GameBackLogger.SYS_LOG.error(pf + " payExecute total_fee is err " + ordermoney);
                return "fail";
            }

            String pf = ov.getPf();
            String[] payChannel = ov.getPay_channel().split(",");
            String channel = StringUtil.getValue(payChannel, 0);

            PfSdkConfig config = PfCommonStaticData.getConfig(payChannel.length == 2 ? payChannel[1] : payChannel[0]);

            if (config!=null) {
                String key=config.getPayKey();
                String sign = MD5Util.getMD5String("customerid="+customerid+"&sd51no="+sd51no+"&sdcustomno="+sdcustomno+"&mark="+mark+"&key="+key);

                if (sign.equals(params.get("sign"))) {

                    sign = MD5Util.getMD5String("sign=" + sign + "&customerid=" + customerid + "&ordermoney=" + ordermoney + "&sd51no=" + sd51no + "&state=" + state + "&key=" + key);
                    if (sign.equals(params.get("resign"))) {
                        if ("1".equals(state)) {

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
                                    result = "<result>1</result>";
                                    break;
                                case 2:
                                    result = "FAIL";
                                    break;
                                case 3:
                                    result = "FAIL";
                                    break;
                                case 0:// 充值成功
                                    result = "<result>1</result>";
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
                PayBean bean = msg.getPayItem();
                if (bean == null) {
                    result.put("code", -4);
                    return JacksonUtil.writeValueAsString(result);
                }
                PfSdkConfig config = PfCommonStaticData.getConfig(payType);
                if (config != null) {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("customerid", config.getMch_id());
                    map.put("sdcustomno", msg.getOv().getOrder_id());
                    map.put("orderAmount", msg.getOv().getAmount()*100);
                    map.put("cardno", "41");
                    map.put("noticeurl", loadPayUrl(getRequest(),"support!ovali_com","pay!webunpay"));
                    map.put("backurl", loadRootUrl(getRequest()));

                    StringBuilder stringBuilder = new StringBuilder();
                    for (Map.Entry<String, Object> kv : map.entrySet()) {
                        stringBuilder.append("&").append(kv.getKey()).append("=").append(kv.getValue());
                    }
                    stringBuilder.append(config.getPayKey());

                    map.put("sign", MD5Util.getMD5String(stringBuilder.substring(1)));
                    map.put("mark", payType);
                    map.put("remarks", com.sy.mainland.util.CoderUtil.encode(msg.getPayItem().getName()));
                    map.put("zftype", "2");

                    stringBuilder.setLength(0);
                    for (Map.Entry<String, Object> kv : map.entrySet()) {
                        stringBuilder.append("&").append(kv.getKey()).append("=").append(kv.getValue());
                    }
                    String url="http://api.unpay.com/PayMegerHandler.ashx?"+stringBuilder.substring(1);
                    String ret=HttpUtil.getUrlReturnValue(url);
                    Map<String,String> resMap=new HashMap<>();

                    Element elements = new SAXReader().read(new StringReader(ret)).getDocument().getRootElement();
                    Iterator<Element> it=((Element)elements.elements().iterator().next()).elements().iterator();
                    while (it.hasNext()){
                        Element e=it.next();
                        if ("item".equals(e.getName())){
                            resMap.put(e.attributeValue("name"),e.attributeValue("value"));
                        }
                    }

                    LogUtil.i("create order url:"+url+",result="+ret);
                    String status =resMap.get("errcode");
                    if ("1111".equals(status)) {
                        String pay_info = resMap.get("url");
                        Map<String, Object> urlMap = new HashMap<>();
                        urlMap.put("pay_info", pay_info);
                        result.put("url", urlMap);
                        result.put("code", 0);
                    }else{
                        result.put("code", -1);
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

    @Override
    public String loginExecute() {
        return null;
    }

}
