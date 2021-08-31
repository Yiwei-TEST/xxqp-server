package com.sy.sanguo.game.redpack;
import java.io.Serializable;

/*
 * 微信红包的请求对象，为了方便构造POST的数据
 * 变量名即是发送的参数名
 */
@SuppressWarnings("serial")
public class WechatRedPackRequest implements Serializable {
    private String nonce_str;       // 随机字符串，不长于32位
    private String sign;            // 签名
    private String mch_billno;      // 商户订单号，必须唯一，组成： mch_id+yyyymmdd+10位一天内不能重复的数字
    private String mch_id;          // 商户号
    private String wxappid;         // 公众账号appid
    private String nick_name;       // 提供方名称
    private String send_name;       // 商户名称
    private String re_openid;       // 用户openid
    private Integer total_amount;   // 付款金额
    private Integer min_value;      // 最小红包金额
    private Integer max_value;      // 最大红包金额
    private Integer total_num;      // 红包发放总人数
    private String wishing;         // 红包祝福语
    private String client_ip;       // Ip地址
    private String act_name;        // 活动名称
    private String remark;          // 备注
    private String logo_imgurl;     // 商户logo的url

    public String getNonce_str() {
        return nonce_str;
    }

    public void setNonce_str(String nonce_str) {
        this.nonce_str = nonce_str;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getMch_billno() {
        return mch_billno;
    }

    public void setMch_billno(String mch_billno) {
        this.mch_billno = mch_billno;
    }

    public String getMch_id() {
        return mch_id;
    }

    public void setMch_id(String mch_id) {
        this.mch_id = mch_id;
    }

    public String getWxappid() {
        return wxappid;
    }

    public void setWxappid(String wxappid) {
        this.wxappid = wxappid;
    }

    public String getNick_name() {
        return nick_name;
    }

    public void setNick_name(String nick_name) {
        this.nick_name = nick_name;
    }

    public String getSend_name() {
        return send_name;
    }

    public void setSend_name(String send_name) {
        this.send_name = send_name;
    }

    public String getRe_openid() {
        return re_openid;
    }

    public void setRe_openid(String re_openid) {
        this.re_openid = re_openid;
    }

    public Integer getTotal_amount() {
        return total_amount;
    }

    public void setTotal_amount(Integer total_amount) {
        this.total_amount = total_amount;
    }

    public Integer getMin_value() {
        return min_value;
    }

    public void setMin_value(Integer min_value) {
        this.min_value = min_value;
    }

    public Integer getMax_value() {
        return max_value;
    }

    public void setMax_value(Integer max_value) {
        this.max_value = max_value;
    }

    public Integer getTotal_num() {
        return total_num;
    }

    public void setTotal_num(Integer total_num) {
        this.total_num = total_num;
    }

    public String getWishing() {
        return wishing;
    }

    public void setWishing(String wishing) {
        this.wishing = wishing;
    }

    public String getClient_ip() {
        return client_ip;
    }

    public void setClient_ip(String client_ip) {
        this.client_ip = client_ip;
    }

    public String getAct_name() {
        return act_name;
    }

    public void setAct_name(String act_name) {
        this.act_name = act_name;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getLogo_imgurl() {
        return logo_imgurl;
    }

    public void setLogo_imgurl(String logo_imgurl) {
        this.logo_imgurl = logo_imgurl;
    }
}