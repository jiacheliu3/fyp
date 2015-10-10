<%@ page contentType="text/html;charset=UTF-8"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<meta name="layout" content="main" />
<title>Insert title here</title>
</head>
<body>
	<div class="body">
		<p>Please choose your option:</p>
		<form id="choice" method="post" action="dispatch">
			<input type="radio" name="option" value="readData" /> Read Original
			Data and Categorize by User <br /> 
			<input type="radio" name="option"	value="scanAllFile" /> Read all user files <br /> 
			<input type="radio" name="option" value="keywordStudy" /> Keyword Study <br />
			<input type="radio" name="option" value="visualize" /> Visualize <br />
			<input type="radio" name="option" value="userStudy" /> Dig User
			Information 
			<input type="submit" value="Go" />
		</form>
	</div>
</body>
</html>