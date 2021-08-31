<%
/* *
 功能：支付宝页面跳转同步通知页面
 版本：3.2
 日期：2011-03-17
 说明：
 以下代码只是为了方便商户测试而提供的样例代码，商户可以根据自己网站的需要，按照技术文档编写,并非一定要使用该代码。
 该代码仅供学习和研究支付宝接口使用，只是提供一个参考。

 //***********页面功能说明***********
 该页面可在本机电脑测试
 可放入HTML等美化页面的代码、商户业务逻辑程序代码
 TRADE_FINISHED(表示交易已经成功结束，并不能再对该交易做后续操作);
 TRADE_SUCCESS(表示交易已经成功结束，可以对该交易做后续操作，如：分润、退款等);
 //********************************
 * */
%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*"%>
<%@ page import="java.util.Map"%>
<%@ page import="com.sy.sanguo.common.util.yinlianh5.ConstantsH5"%>
<%@ page import="com.sy.sanguo.common.util.yinlianh5.client.*"%>
<%@ page import="java.io.PrintWriter"%>
<html>
  <head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>银联支付同步通知页面</title>
		<style type="text/css" >
			body,div,hr,p{
				margin:0px;
				padding:0px;
				background-color:#eeeeee;
				
			}
			hr{
				color:#cccccc;
				height:1px;
			}
			#footer{
				margin:57px auto;
				text-align:center;
			}
			#payBtn{
				width:405px;
				height:47px;
				background-image:url("../resource/img/btn_red_bg.png");
				border:1px solid #99080d;
				margin:0 auto;
				padding-top:13px;
			}
			#top{
				height:80px;
			}
			#backBtn{
				width:175px;
				height:82px;
				padding-left:20px;
				padding-top:20px;
			}
			#info{
				height:230px;
				background-color:#ffffff;
				overflow:hidden;

			}
			#orderResult{
				margin-top:10px;
				background-color:#ffffff;
				text-align:center;
				font-size:25px;
			}
			#orderNum{
				margin-top:20px;
				background-color:#ffffff;
				text-align:center;
				font-size:20px;
			}
		</style>
		<script src="../resource/js/jquery-1.11.2.min.js"></script>
		<script type="text/javascript">
			$(function(){
				$("#payBtn").click(function(){
					window.opener=null;
					window.close();
				});
				$("#backBtn").click(function(){
					window.opener=null;
					window.close();
				});
			
			
			
			});
			
		</script>
  </head>
  <body>
  	<div id="top">
			<div id="backBtn" ><img src="../resource/img/back.png" alt="pay"/></div>
	</div>
	<hr />
	<div id="info"><p id="orderNum">
<%
	// 结果通知参数，易联异步通知采用GET提交
        String version = request.getParameter("Version");
        String merchantId = request.getParameter("MerchantId");
        String merchOrderId = request.getParameter("MerchOrderId");
        String amount = request.getParameter("Amount");
        String extData = request.getParameter("ExtData");
        String orderId = request.getParameter("OrderId");
        String status = request.getParameter("Status");
        String payTime = request.getParameter("PayTime");
        String settleDate = request.getParameter("SettleDate");
        String sign = request.getParameter("Sign");

        // 需要对必要输入的参数进行检查，本处省略...
		out.println("订单号:"+merchOrderId+"</p>");
		out.println("<p id=\"orderResult\">");
        // 订单结果逻辑处理
        String retMsgJson = "";
        try {
            //验证订单结果通知的签名
            boolean b = TransactionH5Client.bCheckNotifySign(version, merchantId, merchOrderId, 
                    amount, extData, orderId, status, payTime, settleDate, sign, 
                    ConstantsH5.PAYECO_RSA_PUBLIC_KEY);
            if (!b) {
                //retMsgJson = "{\"RetCode\":\"E101\",\"RetMsg\":\"验证签名失败!\"}";
                out.println("验证签名失败!");
            }else{
                // 签名验证成功后，需要对订单进行后续处理
                if ("02".equals(status)) { // 订单已支付
                    // 1、检查Amount和商户系统的订单金额是否一致
                    // 2、订单支付成功的业务逻辑处理请在本处增加（订单通知可能存在多次通知的情况，需要做多次通知的兼容处理）；
                    // 3、返回响应内容
                    //retMsgJson = "{\"RetCode\":\"0000\",\"RetMsg\":\"订单已支付\"}";
                    out.println("订单已支付!");
                } else {
                    // 1、订单支付失败的业务逻辑处理请在本处增加（订单通知可能存在多次通知的情况，需要做多次通知的兼容处理，避免成功后又修改为失败）；
                    // 2、返回响应内容
                    //retMsgJson = "{\"RetCode\":\"E102\",\"RetMsg\":\"订单支付失败+"+status+"\"}";
                    out.println("订单支付失败!status="+status);
                }
            }
        } catch (Exception e) {
            //retMsgJson = "{\"RetCode\":\"E103\",\"RetMsg\":\"处理通知结果异常\"}";
           	out.println("处理通知结果异常!e="+e.getMessage());
        }
        //System.out.println("-----同步通知完成----");
        //返回数据
       

%>
		</p>
		</div>
		<hr />
		<div id="footer">
			<div id="payBtn"><img src="../resource/img/backgame.png" alt="pay"/></div>
		</div>
  </body>
</html>