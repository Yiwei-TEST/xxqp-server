package com.sy.sanguo.game.bean;


public class UcPayCallbackResponse{
	String sign = "";
//	String ver="";
//	{"sign":"c272df82a9a75ce73c78feef442cd327",
//	"data":{"failedDesc":"","amount":"10.00","callbackInfo":"897005950,5,2","ucid":"897005950","gameId":"550482","payWay":"999","serverId":"0","orderStatus":"S","orderId":"201412231539153799814"}}

	PayCallbackResponseData data;
	
	public String getSign(){
		return this.sign;
	}
	public void setSign(String sign){
		this.sign =sign;
	}
//	public String getVer() {
//		return ver;
//	}
//	public void setVer(String ver) {
//		this.ver = ver;
//	}
	public PayCallbackResponseData getData(){
		return this.data;
	}
	public void setData(PayCallbackResponseData data){
		this.data = data;
	}
	
	public class PayCallbackResponseData{
		private String orderId;
		private int gameId;
		private int serverId;
		private String ucid;
		private int payWay;
		private String amount;
		private String callbackInfo;
		private String orderStatus;
		private String failedDesc="";
		private String cpOrderId;
		
		public String getOrderId(){
			return this.orderId;
		}
		public void setOrderId(String orderId){
			this.orderId = orderId;
		}
		public int getGameId(){
			return this.gameId;
		}
		
		public void setGameId(int gameId){
			this.gameId = gameId;
		}
		public int getServerId(){
			return this.serverId;
		}
		
		public void setServerId(int serverId){
			this.serverId = serverId;
		}
		
		public String getUcid() {
			return ucid;
		}
		public void setUcid(String ucid) {
			this.ucid = ucid;
		}
		public int getPayWay(){
			return this.payWay;
		}
		
		public void setPayWay(int payWay){
			this.payWay = payWay;
		}
		public String getAmount(){
			return this.amount;
		}
		
		public void setAmount(String amount){
			this.amount = amount;
		}
		
		public String getCallbackInfo(){
			return this.callbackInfo;
		}
		public void setCallbackInfo(String callbackInfo){
			this.callbackInfo = callbackInfo;
		}
		public String getOrderStatus(){
			return this.orderStatus;
		}
		public void setOrderStatus(String orderStatus){
			this.orderStatus = orderStatus;
		}
		public String getFailedDesc(){
			return this.failedDesc;
		}
		public void setFailedDesc(String failedDesc){
			this.failedDesc = failedDesc;
		}
		public String getCpOrderId() {
			return cpOrderId;
		}
		public void setCpOrderId(String cpOrderId) {
			this.cpOrderId = cpOrderId;
		}
		
	}

	
}
