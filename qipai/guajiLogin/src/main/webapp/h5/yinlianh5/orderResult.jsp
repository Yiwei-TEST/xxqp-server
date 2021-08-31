<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>order</title>
<script type="text/javascript">
function onSubmit(){
	var form = document.getElementById("ifr");
	//alert(form.getAttribute("action"));
	//alert(document.getElementById("ExtData").value);
	//alert(form.getAttribute("action") + "?" + document.getElementById("ExtData").value);
	form.setAttribute("action",form.getAttribute("action") + "?" + document.getElementById("ExtData").value);
	//alert(form.getAttribute("action"));
	return true;
}
</script>
</head>
<body>
	<form id="ifr" method="post" action="Notify.do" onSubmit="return onSubmit();">
		<table>
			<tr>
				<th>测试订单结果通知</th>
			</tr>

      <tr>
        <td>成功通知参数串:</td>
        <td><textarea rows="10" cols="100" id="ExtData">Version=2.0.0&MerchantId=302020000058&MerchOrderId=1407893794150&Amount=1.00&ExtData=5rWL6K+V&OrderId=302014081300038222&Status=02&PayTime=20140814111645&SettleDate=20140909&Sign=iDQ6gBAebnh1kzSb4XN0PP3bTIXTkwG9iE8PDnNZBEiTWpBknH4XoBAotC5G/RF4E+HUa7f9esJWEI1mKw84EMDt+gBY2KABe7fejIdzqS8AH5niJEJkWAKwm4qYQTkT4Ate9lshcOZDfcyZ7eqblXXHUYOFBsYtslANOsb+/IA=</textarea></td>
      </tr>

			<tr>
				<td><input type="submit" value="提交"></td>
				<td><input type="reset" value="重置"></td>
			</tr>
		</table>


	</form>
</body>
</html>