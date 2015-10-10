package jav;

import gro.Facade;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class PagerankServlet extends HttpServlet{

	private static final long serialVersionUID = 1L;

	public PagerankServlet() {
		super();

	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		HttpSession session = request.getSession();
		
		Map<String, Object> model = new HashMap<String, Object>();
		// model.put("message", "Hello World");
		session.setAttribute("model", model);

		// get result from Facade. Facade will act as the communicator with
		// groovy package
		//request.removeAttribute("result");
		session.setAttribute("result", Facade.getResult("3"));
		// Forward to JSP file to display message
		RequestDispatcher dispatcher = request
				.getRequestDispatcher("/pagerank.jsp");
		dispatcher.forward(request, response);

	}
}
