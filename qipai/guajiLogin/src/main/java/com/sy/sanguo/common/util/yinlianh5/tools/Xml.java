package com.sy.sanguo.common.util.yinlianh5.tools;

import java.io.Serializable;

/**
 * 描述： 返回报文的数据元素基础类，只要接口调用有返回报文，就会把报文元素分解到该类中
 * 1、若交易通讯返回失败（RetCode!="0000"），该类只有XmlData、RetCode、RetMsg元素；
 * 2、若报文正常返回：该类的元素请参考接口报文返回数据的元素订单；
 */
public class Xml implements Serializable{
    
	private static final long serialVersionUID = -5543552124157971414L;
	
	private String XmlData="";	    //响应XML数据
	private String RetCode="";	    //响应码
	private String RetMsg="";	    //响应描述
	private String TradeCode="";	//交易码
	private String Version="";		//通讯协议版本号
	private String MerchantId="";	//商户代码
	private String MerchOrderId=""; //商户订单号
	private String Amount="";		//商户订单金额
	private String TradeTime="";	//商户订单提交时间
	private String OrderId="";		//易联订单号
	private String ExtData="";		//商户保留信息
	private String Status="";		//订单状态
	private String PayTime="";		//支付成功时间
	private String VerifyTime="";	//针对配置了防钓鱼的商户返回该参数，采用易联内部算法进行加密处理；验证时间戳30秒有效
	private String SettleDate="";	//清算日期
	private String Sign="";			//签名
	
	public Xml(){}
	
	/**
	 * @return : 响应XML数据
	 */
	public String getXmlData() {
		return XmlData;
	}

	/**
	 * @param retCode : 响应XML数据
	 */
	public void setXmlData(String xmlData) {
		XmlData = xmlData;
	}
	
	/**
	 * @return : 响应码
	 */
	public String getRetCode() {
		return RetCode;
	}

	/**
	 * @param retCode : 响应码
	 */
	public void setRetCode(String retCode) {
		RetCode = retCode;
	}
	
	/**
	 * @return : 响应描述
	 */
	public String getRetMsg() {
		return RetMsg;
	}

	/**
	 * @param tradeCode : 响应描述
	 */
	public void setRetMsg(String retMsg) {
		RetMsg = retMsg;
	}	
	/**
	 * @return : 交易码
	 */
	public String getTradeCode() {
		return TradeCode;
	}

	/**
	 * @param tradeCode : 交易码
	 */
	public void setTradeCode(String tradeCode) {
		TradeCode = tradeCode;
	}

	/**
	 * @return : 通讯协议版本号
	 */
	public String getVersion() {
		return Version;
	}

	/**
	 * @param version : 通讯协议版本号
	 */
	public void setVersion(String version) {
		Version = version;
	}

	/**
	 * @return : 商户代码
	 */
	public String getMerchantId() {
		return MerchantId;
	}

	/**
	 * @param merchantId : 商户代码
	 */
	public void setMerchantId(String merchantId) {
		MerchantId = merchantId;
	}

	/**
	 * @return : 商户订单号
	 */
	public String getMerchOrderId() {
		return MerchOrderId;
	}

	/**
	 * @param merchOrderId : 商户订单号
	 */
	public void setMerchOrderId(String merchOrderId) {
		MerchOrderId = merchOrderId;
	}

	/**
	 * @return : 商户订单金额
	 */
	public String getAmount() {
		return Amount;
	}

	/**
	 * @param amount : 商户订单金额
	 */
	public void setAmount(String amount) {
		Amount = amount;
	}

	/**
	 * @return : 商户订单提交时间
	 */
	public String getTradeTime() {
		return TradeTime;
	}

	/**
	 * @param tradeTime : 商户订单提交时间
	 */
	public void setTradeTime(String tradeTime) {
		TradeTime = tradeTime;
	}

	/**
	 * @return : 易联订单号
	 */
	public String getOrderId() {
		return OrderId;
	}

	/**
	 * @param orderId : 易联订单号
	 */
	public void setOrderId(String orderId) {
		OrderId = orderId;
	}

	/**
	 * @return : 商户保留信息
	 */
	public String getExtData() {
		return ExtData;
	}

	/**
	 * @param extData : 商户保留信息
	 */
	public void setExtData(String extData) {
		ExtData = extData;
	}	

	/**
	 * @return : 订单状态
	 */
	public String getStatus() {
		return Status;
	}

	/**
	 * @param status : 订单状态
	 */
	public void setStatus(String status) {
		Status = status;
	}	
	
	/**
	 * @return : 支付成功时间
	 */
	public String getPayTime() {
		return PayTime;
	}

	/**
	 * @param payTime : 支付成功时间
	 */
	public void setPayTime(String payTime) {
		PayTime = payTime;
	}	

	/**
	 * @return 防钓鱼验证时间戳
	 */
	public String getVerifyTime() {
	    return VerifyTime;
	}
	
	/**
	 * @param verifyTime 防钓鱼验证时间戳
	 */
	public void setVerifyTime(String verifyTime) {
	    VerifyTime = verifyTime;
	}

	/**
	 * @return : 清算日期
	 */
	public String getSettleDate() {
		return SettleDate;
	}


    /**
	 * @param settleDate : 清算日期
	 */
	public void setSettleDate(String settleDate) {
		SettleDate = settleDate;
	}	

	/**
	 * @return : 签名
	 */
	public String getSign() {
		return Sign;
	}

	/**
	 * @param settleDate : 签名
	 */
	public void setSign(String sign) {
		Sign = sign;
	}	
	
}
