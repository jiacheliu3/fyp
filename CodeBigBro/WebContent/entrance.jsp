<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Entrance</title>
</head>
<body>
<p>Please choose your option:</p>
<form id="choice" method="post" action="display">
<input type="radio" name="option" value="readData" /> Read Original Data and Categorize by User
<br />
<input type="radio" name="option" value="scanAllFile" /> Read all user files
<br />
<input type="radio" name="option" value="keywordStudy" /> Keyword Study
<br />
<input type="radio" name="option" value="visualize" /> Visualize
<br />
<input type="radio" name="option" value="userStudy" /> Dig User Information
<input type="submit" value="Go" />
</form>

</body>
</html>