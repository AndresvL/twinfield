package servlet;

import controller.WorkOrderHandler;
import controller.eaccouting.OAuthEAccounting;
import controller.twinfield.OAuthTwinfield;
import controller.wefact.OAuthWeFact;
import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



public class OAuthServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String softwareToken = req.getParameter("token");
		String softwareName = req.getParameter("softwareName");
		//Get checkSaved from session every time oauth is called		
		String checkSaved = (String) req.getSession().getAttribute("checkSaved");
		if(checkSaved != null){
			req.getSession().setAttribute("saved", checkSaved);
			req.getSession().setAttribute("checkSaved", null);
		}else{
			req.getSession().setAttribute("saved", "");
		}
		
		RequestDispatcher rd = null;
		// Set session with software name and token
		int code = WorkOrderHandler.checkWorkOrderToken(softwareToken, softwareName);
		System.out.println("WEB code " + code);
		if (code == 200) {
			req.getSession().setAttribute("softwareToken", softwareToken);
			req.getSession().setAttribute("softwareName", softwareName);
			req.getSession().setAttribute("checkboxes", null);
			req.getSession().setAttribute("logs", null);

			switch (softwareName) {
			case "Twinfield":
				OAuthTwinfield oauth = new OAuthTwinfield();
				oauth.authenticate(softwareToken, req, resp);	
				req.getSession().setAttribute("offices", null);
				req.getSession().setAttribute("users", null);
				break;
			case "WeFact":
				OAuthWeFact oauth2 = new OAuthWeFact();
				oauth2.authenticate(softwareToken, req, resp);
				break;
			case "EAccounting":
				//typeofwork
				req.getSession().setAttribute("types", null);
				//paymentmethod
				req.getSession().setAttribute("paymentmethod", null);
				OAuthEAccounting oauth3 = new OAuthEAccounting();
				oauth3.authenticate(softwareToken, req, resp);
				break;
			}
		}else{
			switch (softwareName) {
			case "Twinfield":
				rd = req.getRequestDispatcher("twinfield.jsp");
				req.getSession().setAttribute("session", null);
				req.getSession().setAttribute("logs", null);
				req.getSession().setAttribute("error", "Token is invalid");
				break;
			case "WeFact":
				req.getSession().setAttribute("softwareToken", softwareToken);			
				req.getSession().setAttribute("logs", null);
				req.getSession().setAttribute("errorMessage", "Error " +  code + ": Token is invalid");
				rd = req.getRequestDispatcher("weFact.jsp");
				break;
			case "EAccounting":
				req.getSession().setAttribute("softwareToken", softwareToken);			
				req.getSession().setAttribute("logs", null);
				req.getSession().setAttribute("errorMessage", "Error " +  code + ": Token is invalid");
				rd = req.getRequestDispatcher("eAccounting.jsp");
				break;
			default:
				break;
			}
			rd.forward(req, resp);
		}
		
	}
}
