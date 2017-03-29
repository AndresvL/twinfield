package servlet;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import DAO.ObjectDAO;
import DAO.TokenDAO;
import controller.WorkOrderHandler;
import controller.twinfield.OAuthTwinfield;
import controller.wefact.OAuthWeFact;

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
		if (WorkOrderHandler.checkWorkOrderToken(softwareToken, softwareName) == 200 ) {
			req.getSession().setAttribute("softwareToken", softwareToken);
			req.getSession().setAttribute("softwareName", softwareName);
			req.getSession().setAttribute("checkboxes", null);
			req.getSession().setAttribute("logs", null);
			req.getSession().setAttribute("offices", null);
			req.getSession().setAttribute("users", null);
			switch (softwareName) {
			case "Twinfield":
				OAuthTwinfield oauth = new OAuthTwinfield();
				oauth.authenticate(softwareToken, req, resp);				
				break;
			case "WeFact":
				OAuthWeFact oauth2 = new OAuthWeFact();
				oauth2.authenticate(softwareToken, req, resp);
				break;
			default:
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
				rd = req.getRequestDispatcher("weFact.jsp");
//				req.getSession().setAttribute("logs", null);
				req.getSession().setAttribute("error", "Token is invalid");
				break;
			default:
				break;
			}
			rd.forward(req, resp);
		}
	}
}
