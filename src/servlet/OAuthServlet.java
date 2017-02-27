package servlet;

import java.io.IOException;
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
//		String softwareToken = (String) req.getSession().getAttribute("softwareToken");
//		String softwareName = (String) req.getSession().getAttribute("softwareName");
		String softwareToken = req.getParameter("token");
		String softwareName = req.getParameter("softwareName");
//		// Token from WorkOrderApp
//		if (softwareToken == null) {
//			
//		}
//		if (softwareName == null) {
//			
//		}

		// Set session with software name and token
		if (WorkOrderHandler.checkWorkOrderToken(softwareToken) == 200) {
			req.getSession().setAttribute("softwareToken", softwareToken);
			req.getSession().setAttribute("softwareName", softwareName);
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
		}
	}

}
