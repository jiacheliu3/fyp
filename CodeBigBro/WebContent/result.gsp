<%@ page contentType="text/html;charset=UTF-8"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<meta name="layout" content="main" />
<title>Insert title here</title>
</head>
<body>
	<div class="body">
	<%
		out.println(request.getAttribute("option"));
		out.println(request.getAttribute("result"));
	%>
	</div>
</body>
</html>