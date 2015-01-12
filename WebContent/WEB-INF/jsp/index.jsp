<%@taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<html>
<head>
<title>Spring 3.0 MVC</title>
</head>
<body>
	<a href="hello">Say Hello</a>
	<form:form method="POST" action="/hello">
		<table>
			<tr>
<%-- 				<td><form:label path="name">Name </form:label></td>
				<td><form:input path="name" /></td> --%>
			</tr>
		</table>
		<input type="submit" value="submit" />
	</form:form>
</body>
</html>