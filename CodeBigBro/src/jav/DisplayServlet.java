package jav;

import gro.Bootstrap;
import gro.Facade;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class DisplayServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public DisplayServlet() {
		super();

	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		HttpSession session = request.getSession();
		ServletContext app = getServletConfig().getServletContext();
		
		
		Map<String,Object> result=(Map<String,Object>)request.getAttribute("result"); 
		
		if(result==null||!result.containsKey("users")){
		// get result from Facade. Facade will act as the communicator with
		// groovy package
		//request.removeAttribute("result");
		
		request.setAttribute("result", Facade.getResult("2"));
		}
		// Forward to JSP file to display message
		RequestDispatcher dispatcher = request
				.getRequestDispatcher("/scanResult.jsp");
		dispatcher.forward(request, response);

	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		String option = request.getParameter("option");
		
		Map<String, Object> result;

		switch (option) {
		case "readData":
			result = (Map<String, Object>) Facade.getResult(1);
			request.setAttribute("result", result);
			request.getRequestDispatcher("/readData.jsp").forward(request, response);
			break;
		case "scanAllFile":
			result = (Map<String, Object>) Facade.getResult(2);
			request.setAttribute("result", result);
			request.getRequestDispatcher("/scanResult.jsp").forward(request, response);
			break;
		case "keywordStudy":
			result = (Map<String, Object>) Facade.getResult(3);
			request.setAttribute("result", result);
			request.getRequestDispatcher("/pagerank.jsp").forward(request, response);
			break;
		case "visualize":
			result = (Map<String, Object>) Facade.getResult(4);
			break;
		case "userStudy":
			result = (Map<String, Object>) Facade.getResult(5);
			break;
		}
	}

}
