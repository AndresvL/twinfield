package servlet.twinfield;

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
		String token = "818784741B7543C7AE95CE5BFB783DF2";
		String secret = "F441FB65B6AA42C995F9FAF3662E8A10";
		String softwareToken = (String) req.getSession().getAttribute("softwareToken");
		String softwareName = (String) req.getSession().getAttribute("softwareName");
		// Token from WorkOrderApp
		if (softwareToken == null) {
			softwareToken = req.getParameter("token");
		}
		if (softwareName == null) {
			softwareName = req.getParameter("softwareName");
		}

		// Set session with software name and token
		if (WorkOrderHandler.checkToken(softwareToken) == 200) {
			req.getSession().setAttribute("softwareToken", softwareToken);
			req.getSession().setAttribute("softwareName", softwareName);
			switch (softwareName) {
			case "Twinfield":
				OAuthTwinfield oauth = new OAuthTwinfield();
				oauth.authenticate(token, secret, softwareToken, req, resp);
				break;
			case "WeFact":
				OAuthWeFact oauth2 = new OAuthWeFact();
				oauth2.authenticate(token, secret, softwareToken, req, resp);
				break;

			default:
				break;
			}
		}
	}

}
