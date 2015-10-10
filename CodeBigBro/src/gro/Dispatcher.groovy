package gro

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//import groovy.lang.GroovyClassLoader;
//import groovy.lang.GroovyObject;
import groovy.servlet.GroovyServlet

class Dispatcher extends GroovyServlet {
	private static final long serialVersionUID = 1L;
	public Dispatcher(){
		super();
	}

	
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
			println request;
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		System.out.println(request.getParameter("option"));

		request.setAttribute("option", request.getParameter("option"));


//		final GroovyClassLoader classLoader = new GroovyClassLoader();
//		Class groovy = classLoader.parseClass(new File("gro/Bootstrap.groovy"));
//		System.out.println(groovy);
//		GroovyObject groovyObj = (GroovyObject) groovy.newInstance();

		Map<String,Object> result=new HashMap<>();
//		switch(request.getParameter("option")){
//			case "readData":
//				result=(Map<String, Object>) Bootstrap.start("1");
//				break;
//			case "scanAllFile":
//				result=(Map<String, Object>) Bootstrap.start("2");
//				break;
//			case "keywordStudy":
//				result=(Map<String, Object>) Bootstrap.start("3");
//				break;
//			case "visualize":
//				result=(Map<String, Object>) Bootstrap.start("4");
//				break;
//			case "userStudy":
//				result=(Map<String, Object>) Bootstrap.start("5");
//				break;
//		}

		request.setAttribute("result", result);
		println request;
		// Forward to GSP file to display message
		RequestDispatcher dispatcher = request
				.getRequestDispatcher("/result.gsp");
		dispatcher.forward(request, response);

	}
}
