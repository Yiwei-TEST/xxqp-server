<%/* *
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
			 * */%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*"%>
<%@ page import="java.util.Map"%>
<%@ page import="com.sy.sanguo.common.util.pipah5.*"%>
<html>
  <head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>同步通知页面</title>
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
		//获取支付宝GET过来反馈信息
		Map<String, String> params = new HashMap<String, String>();
		Map requestParams = request.getParameterMap();
		for (Iterator iter = requestParams.keySet().iterator(); iter
				.hasNext();) {
			String name = (String) iter.next();
			String[] values = (String[]) requestParams.get(name);
			String valueStr = "";
			for (int i = 0; i < values.length; i++) {
				valueStr = (i == values.length - 1) ? valueStr + values[i]
						: valueStr + values[i] + ",";
			}
			//乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
			valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
			params.put(name, valueStr);
		}

		String out_trade_no = new String(request.getParameter(
				"orderno").getBytes("ISO-8859-1"), "UTF-8");
	

		//交易状态
		String result = new String(request.getParameter("success").getBytes(
				"ISO-8859-1"), "UTF-8");

		
		boolean verify_result = PayUtil.verifyCallback(params);

		out.println("订单号:" + out_trade_no + "</p>");
		out.println("<p id=\"orderResult\">");
		if (verify_result) {//验证成功
			if ("1".equalsIgnoreCase(result)) {
				out.println("支付成功");
			} else {
				out.println("支付失败");
			}
		} else {
			out.println("验证失败");
		}
	%>
		</p>
		</div>
		<hr />
		<div id="footer">
			<div id="payBtn"><img src="../resource/img/backgame.png" alt="pay"/></div>
		</div>
  </body>
</html>