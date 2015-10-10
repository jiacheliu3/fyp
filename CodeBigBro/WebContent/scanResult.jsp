<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="gro.User" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>User information display</title>
<h3><% 
		Map<String,Object> result=(Map<String,Object>)request.getAttribute("result"); 
		HashSet<User> users=(HashSet<User>)result.get("users");
		out.println("User count: "+users.size());
		
	%> </h3>
</head>
<body>

	
	<%
	int count=0;
	
	for(User u:users) {
		//User u=(User)o;
		if(u==null)
			continue;
		%>
	
	<p>User<% out.println(++count); %></p>
	<p>Name:	<%out.println(u.getName()); %></p>
	<p>Keywords:
		<%out.println(u.getAllKeywords()); %>
	</p>
	
	<%
	} 
	%>
</body>
</html>