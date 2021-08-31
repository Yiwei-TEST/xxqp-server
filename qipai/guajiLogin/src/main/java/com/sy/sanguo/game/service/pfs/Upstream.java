package com.sy.sanguo.game.service.pfs;

import com.sy.sanguo.common.server.PayBean;
import com.sy.sanguo.game.pdkuai.util.LogUtil;
import org.apache.commons.lang3.StringUtils;

import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.util.MD5Util;
import com.sy.sanguo.game.service.BaseSdk;
import com.sy.sanguo.game.service.SysInfManager;
import com.sy.sanguo.game.service.channel.aibei.Aibei;
import com.sy.sanguo.game.service.pfs.apple.Apple;
import com.sy.sanguo.game.service.pfs.weixin.Weixin;

/**
 * 上游账号登录第三方支付
 *
 * @author lc
 */
public class Upstream extends BaseSdk {
    @Override
    public String loginExecute() {
        String uid = getRequest().getParameter("u");
        String pf = getRequest().getParameter("p");
        String time = getRequest().getParameter("t");
        String selfsign = getRequest().getParameter("k");
        String secret = "mwFLeKLzNoL46dDn0vE2";

        StringBuilder md5 = new StringBuilder();
        md5.append(uid);
        md5.append(pf);
        md5.append(time);
        md5.append(secret);

        if (!StringUtils.isBlank(uid) && !StringUtils.isBlank(pf) && !StringUtils.isBlank(time) && !StringUtils.isBlank(selfsign) && MD5Util.getStringMD5(md5.toString()).equals(selfsign)) {
            // 验证通过
            return uid;
        } else {
            GameBackLogger.SYS_LOG.info("pf: " + getPf() + " param verify error!");
        }
        return "";

    }

    @Override
    public String ovali() {
        PayBean item=loadItem();
        String payType;
        if (item != null && StringUtils.isNotBlank(item.getPayType())){
            payType = item.getPayType();
            LogUtil.i("load pay type force:payType="+payType+",itemId="+getRequest().getParameter("itemid")+",total_fee="+this.getString("total_fee"));
        }else{
            payType = this.getString("payType");
        }
        BaseSdk inst = getPayType(payType);
        if (inst==null){
            LogUtil.i("load pay error:payType="+this.getString("payType")+",itemId="+getRequest().getParameter("itemid")+",total_fee="+this.getString("total_fee"));
            return null;
        }else{
            return inst.ovali();
        }
    }

    /**
     * 支付回调处理
     *
     * @param c 渠道
     * @return
     */
    public String payExecute(String c) {
        BaseSdk inst = getPayType(c);
        return inst.payExecute();
    }

    public BaseSdk getPayType(String payType) {
        BaseSdk inst = null;
        if ("aibei".equals(payType)) {
            inst = new Aibei();
        } else if ("apple".equals(payType)) {
            inst = new Apple();
        } else if ("tonglian".equals(payType)) {
            inst = new Tonglian();
        } else if (payType.startsWith("weixin")) {
            inst = new Weixin();
        } else if (payType.startsWith("futong")) {
            inst = new WeiFuTong();
        } else if (payType.startsWith("webfutong")) {
            inst = new WebWeiFuTong();
        } else if (payType.startsWith("webchanyou")) {
            inst = new WebChanYou();
        } else if (payType.startsWith("webzyf")) {
            inst = new WebZyf();
        } else if (payType.startsWith("webunpay")) {
            inst = new WebUnPay();
        } else if (payType.startsWith("webwmpay")) {
            inst = new WebWmPay();
        } else if (payType.startsWith("webhaiwai")) {
            inst = new WebHaiWai();
        } else if (payType.startsWith("xftpay")) {
            inst = new XftPay();
        } else if(payType.startsWith("h5zyf")) {
            inst = new H5Zyf();
        }
        
        
        

        if (inst == null) {
            GameBackLogger.SYS_LOG.info("Upstream: " + payType + " getChannel error!");
            return null;
        }
        inst.setRequest(request);
        inst.setOrderDao(orderDao);
        inst.setPf(pf + payType);
        inst.setPayType(payType);
        inst.setOrderValiDao(orderValiDao);
        inst.setUserDao(userDao);
        inst.setRoomCardDao(roomCardDao);
        inst.setSdkConfig(SysInfManager.getInstance().getSdkConfig(inst.getPf()));
        inst.setPfConfig();
        return inst;
    }

    @Override
    public String payExecute() {
        return null;
    }

}
