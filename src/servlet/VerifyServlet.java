package servlet;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import controller.OAuthTwinfield;
import controller.SoapHandler;
import object.Token;

public class VerifyServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String temporaryToken = req.getParameter("oauth_token");
		String temporaryVerifier = req.getParameter("oauth_verifier");
		OAuthTwinfield oauth = new OAuthTwinfield();
		Token token = oauth.getAccessToken(temporaryToken, temporaryVerifier);
		String sessionID = SoapHandler.getSession(token);
		RequestDispatcher rd = null;
		@SuppressWarnings("unchecked")
		ArrayList<String> offices = (ArrayList<String>) SoapHandler.createSOAPXML(sessionID,
				"<list><type>offices</type></list>", "office");
		rd = req.getRequestDispatcher("adapter.jsp");
		req.getSession().setAttribute("offices", offices);
		req.getSession().setAttribute("softwareToken", token.getSoftwareToken());
		req.getSession().setAttribute("session", sessionID);
		rd.forward(req, resp);
	}
}
