<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page import="java.util.Scanner"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Map"%>
<%@ page import="java.util.Map.Entry"%>
<%@ page import="java.util.LinkedHashMap"%>
<%@ page import="java.io.File"%>
<%@ page import="java.io.FileReader"%>
<%@ page import="com.google.gson.Gson"%>
<%@ page import="gro.*"%>

<html>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>Insert title here</title>
</head>

<body>
	<style>
b: {
	color: red;
}
</style>
	<%
		FileReader reader = new FileReader(
				"C:\\Users\\Tassadar\\Desktop\\Course\\weibo\\temp\\sampleResult.json");
		Gson gson = new Gson();
		String s = "";
		JsonWrapper obj = gson.fromJson(reader, JsonWrapper.class);
		int count=1;
		for (JsonObject j : obj.cargo) {
			
			//out.println("<p>" + j.content + "</p>");
			s = count+". "+j.content;
			Map<String, Double> map = j.keywords;
			for (Map.Entry<String, Double> e : map.entrySet()) {
				//for each keyword, find the place in the string(content) and insert <b>
				s = s.replaceAll(e.getKey(), "<b style=\"color:red\">" + "/"+e.getKey() +"\\"+ "</b>");
			}
			
			out.println("<p>" + s + "</p>");
			count++;
		}
	%>
</body>
</html>