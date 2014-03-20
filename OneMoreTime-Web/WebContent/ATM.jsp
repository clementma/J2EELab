<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>MyCentennial Bank ATM</title>
</head>
<body>

<h1>MyCentennial Bank ATM</h1>

<!-- 
<p>${bean.getBalance()}</p>
 -->
 
<form action="ATM" method="post">

<table cellspacing="5" border="0">
	<tr>
		<td align="right">Amount:</td>
		<td><input type="text" name="amount"></td>
		<td><td>
	</tr>
	<tr>
		<td><input type="submit" name="Deposit" value="Deposit"></td>
		<td><input type="submit" name="Withdraw" value="Withdraw"></td>
		<td><input type="submit" name="Balance" value="Balance"></td>
		<td><input type="submit" name="Logout" value="Logout"></td>
	</tr>
	
</table>
</form>


</body>
</html>