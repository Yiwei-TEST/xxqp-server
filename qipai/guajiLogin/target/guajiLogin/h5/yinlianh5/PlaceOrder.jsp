<%@ page language="java" contentType="text/html; charset=utf-8"	pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>order</title>
</head>
<body>
	<form method="post" action="order.do">
		<table>
			<tr>
				<th>订单下单</th>
			</tr>
			<tr>
				<td>商户订单金额:</td>
				<td><input type="text" name="Amount" size="40" /></td>
			</tr>
			<tr>
				<td>商户订单描述:</td>
				<td><textarea rows="2" cols="80" name="OrderDesc"></textarea></td>
			</tr>
			<tr>
				<td><input type="submit" value="模拟H5下单"></td>
			</tr>
		</table>
	</form>
</body>
</html>