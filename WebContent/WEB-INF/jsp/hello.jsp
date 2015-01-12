<%@taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html>
<head>
<title>Spring 3.0 MVC Series</title>
</head>
<body>
	<table border="1" id="hor-zebra">
		<tr>
			<th><c:out value="domain name" /></th>
			<th><c:out value="alexa rank" /></th>
		</tr>

		<c:forEach items="${message}" var="item" varStatus="row">
			<tr>
				<td><c:out value="${item.nakedDNS}" /></td>
				<td><c:out value="${item.alexaRank}" /></td>
			</tr>
		</c:forEach>
	</table>

</body>
</html>