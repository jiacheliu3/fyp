<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Map"%>
<%@ page import="java.util.LinkedHashMap"%>
<%@ page import="java.util.Set"%>
<%@ page import="java.util.HashSet"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="java.util.Map.Entry"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Page Rank</title>
</head>
<body>

	<%
		Map<String, Object> result = (Map<String, Object>) request
				.getAttribute("result");
		LinkedHashMap<String, Double> pagerank = (LinkedHashMap<String, Double>) result
				.get("pagerank");
		//out.println(pagerank.size());
		
	%>
	

	<center>
		<h2>Page Rank Table</h2>
		<table width="100%" border="1" align="center">
			<tr bgcolor="#949494">
				<th>Keyword</th>
				<th>Pagerank</th>
			</tr>
			<%
				Iterator entries = pagerank.entrySet().iterator();
				while (entries.hasNext()) {
					Entry thisEntry = (Entry) entries.next();
					String key = (String) thisEntry.getKey();
					out.print("<tr><td>" + key + "</td>\n");
					Double value = (Double) thisEntry.getValue();
					out.println("<td> " + value + "</td></tr>\n");

				}
			%>
		</table>
	</center>
</body>
</html>