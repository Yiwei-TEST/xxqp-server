var params={
		currentPayType:1,
};

$(function(){
	$("#loading").css({"display":"none"});
	$("#product_name").html(decodeURI(decodeURI(getUrlParam("productName"))));
	$("#product_amont").html(getUrlParam("amount")+"元");
	$("#product_amont").html(getUrlParam("amount")+"元");
	$("#iconImg").attr("src","./resource/img/pay"+getUrlParam("icon")+".png");
	$("#zhifubao").click(function(){
		$("#zhifubao_yuan").css({"background-image":"url(\"./resource/img/choose_select.png\")"});
		$("#yinlianka_yuan").css({"background-image":"url(\"./resource/img/choose_normal.png\")"});
		params.currentPayType=1;
	});
	$("#yinlianka").click(function(){
		$("#zhifubao_yuan").css({"background-image":"url(\"./resource/img/choose_normal.png\")"});
		$("#yinlianka_yuan").css({"background-image":"url(\"./resource/img/choose_select.png\")"});
		params.currentPayType=2;
	});
	$("#payBtn").click(function(){
		startPay();
	});
	$("#backBtn").click(function(){
		window.opener=null;
		window.close();
	});
	
	
});
function getUrlParam(name)
{
	var reg = new RegExp("(^|&)"+ name +"=([^&]*)(&|$)"); //构造一个含有目标参数的正则表达式对象
	var r = window.location.search.substr(1).match(reg);  //匹配目标参数
	if (r!=null) return unescape(r[2]); return null; //返回参数值
};
function startPay(){
	if(params.currentPayType==1){//支付宝
		
		var url="./zhifubaoh5/alipayapi.jsp?WIDout_trade_no="+getUrlParam("orderId")+"&WIDsubject="+decodeURI(decodeURI(getUrlParam("productName")))+"&WIDtotal_fee="+getUrlParam("amount");
		window.location.href=url;
	}else{//财付通
		var url="../support!yinlianh5Ovali.guajilogin?uid="+getUrlParam("flatId")+"&server_id="+getUrlParam("serverId")+"&pf=syh5"+"&itemId="+getUrlParam("productId")+"&c="+getUrlParam("channelId");
		window.location.href=url;
		
	}
}