package servlet.twinfield;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import controller.twinfield.OAuthTwinfield;
import controller.twinfield.SoapHandler;
import object.Token;
import object.twinfield.Search;

public class VerifyServlet extends HttpServlet {

	private String redirect = System.getenv("CALLBACK");
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String temporaryToken = req.getParameter("oauth_token");
		String temporaryVerifier = req.getParameter("oauth_verifier");
		OAuthTwinfield oauth = new OAuthTwinfield();
		String softwareName = (String) req.getSession().getAttribute("softwareName");
		Token token = oauth.getAccessToken(temporaryToken, temporaryVerifier, softwareName);
		String sessionID = null;
		String cluster = null;
		String[] array = SoapHandler.getSession(token);
		sessionID = array[0];
		cluster = array[1];
		@SuppressWarnings("unchecked")
		ArrayList<String> offices = (ArrayList<String>) SoapHandler.createSOAPXML(sessionID, cluster,
				"<list><type>offices</type></list>", "office");
		//get all users
		ArrayList<Map<String, String>> users = new ArrayList<Map<String, String>>();
		Search searchObject = new Search("USR", "*", 0, 1, 100, null);
		ArrayList<String> responseArray = SoapHandler.createSOAPFinder(sessionID, cluster, searchObject);
		for(String s : responseArray){
			Map<String, String> allUsers = new HashMap<String, String>();
			String[] split = s.split(",");
			allUsers.put("code", split[0]);
			allUsers.put("name", split[1]);
			users.add(allUsers);
		}
		
		req.getSession().setAttribute("offices", offices);
		req.getSession().setAttribute("users", users);
		req.getSession().setAttribute("softwareToken", token.getSoftwareToken());
		req.getSession().setAttribute("session", sessionID);
		if (redirect != null) {
//			 + "&checkbox=" + oldImport
			resp.sendRedirect(redirect + "OAuth.do?token=" +  token.getSoftwareToken() + "&softwareName=" + softwareName);
		} else {
			resp.sendRedirect("http://localhost:8080/connect/OAuth.do?token=" + token.getSoftwareToken() + "&softwareName=" + softwareName);
		}
	}
}
