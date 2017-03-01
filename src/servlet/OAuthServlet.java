package servlet;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import controller.WorkOrderHandler;
import controller.twinfield.OAuthTwinfield;
import controller.wefact.OAuthWeFact;

public class OAuthServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String softwareToken = req.getParameter("token");
		String softwareName = req.getParameter("softwareName");
//		String oldImport = req.getParameter("checkbox");

		RequestDispatcher rd = null;
		// Set session with software name and token
		if (WorkOrderHandler.checkWorkOrderToken(softwareToken, softwareName) == 200) {
			req.getSession().setAttribute("softwareToken", softwareToken);
			req.getSession().setAttribute("softwareName", softwareName);
			req.getSession().setAttribute("checkboxes", null);
			req.getSession().setAttribute("logs", null);
			switch (softwareName) {
			case "Twinfield":
//				req.getSession().setAttribute("oldCheckboxes", oldImport);
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
				req.getSession().setAttribute("error", "Token is invalid");
				break;
			case "WeFact":
				break;
			default:
				break;
			}
			rd.forward(req, resp);
		}
	}
}
