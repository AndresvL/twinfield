package servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import controller.eaccouting.OAuthEAccounting;
import controller.moloni.MoloniHandler;
import controller.moloni.OAuthMoloni;
import controller.twinfield.OAuthTwinfield;
import controller.twinfield.SoapHandler;
import object.Token;
import object.twinfield.Search;

public class VerifyServlet extends HttpServlet {
	
	private String redirect = System.getenv("REDIRECT");
	private static final long serialVersionUID = 1L;
	
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String softwareName = (String) req.getSession().getAttribute("softwareName");
		switch (softwareName) {
		case "Twinfield":
			String temporaryToken = req.getParameter("oauth_token");
			String temporaryVerifier = req.getParameter("oauth_verifier");
			OAuthTwinfield oauth = new OAuthTwinfield();
			
			Token token = oauth.getAccessToken(temporaryToken, temporaryVerifier, softwareName);
			String sessionID = null;
			String cluster = null;
			String[] array = SoapHandler.getSession(token);
			sessionID = array[0];
			cluster = array[1];
			@SuppressWarnings("unchecked")
			ArrayList<String> offices = (ArrayList<String>) SoapHandler.createSOAPXML(sessionID, cluster,
					"<list><type>offices</type></list>", "office");
			// get all users
			ArrayList<Map<String, String>> users = new ArrayList<Map<String, String>>();
			Search searchObject = new Search("USR", "*", 0, 1, 100, null);
			ArrayList<String> responseArray = SoapHandler.createSOAPFinder(sessionID, cluster, searchObject);
			for (String s : responseArray) {
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
				resp.sendRedirect(
						redirect + "OAuth.do?token=" + token.getSoftwareToken() + "&softwareName=" + softwareName);
			} else {
				resp.sendRedirect("https://localhost:8080/connect/OAuth.do?token=" + token.getSoftwareToken()
						+ "&softwareName=" + softwareName);
			}
			break;
		case "eAccounting":
			String softwareToken = (String) req.getSession().getAttribute("softwareToken");
			String code = req.getParameter("code");
			String error = req.getParameter("error");
			Token t = null;
			if (code != null) {
				System.out.println("CODE " + code);
				t = OAuthEAccounting.getAccessToken(code, null, softwareName, softwareToken);
			} else if (error != null) {
				System.out.println("Error EAccounting authentication: " + error);
				break;
			}
			System.out.println("accessToken " + t.getAccessToken());
			System.out.println("refreshToken(secret) " + t.getAccessSecret());
			System.out.println("clientID " + t.getConsumerToken());
			System.out.println("clientSecret " + t.getConsumerSecret());
			System.out.println("softwareToken " + t.getSoftwareToken());
			System.out.println("softwareName " + t.getSoftwareName());
			
			resp.sendRedirect("https://localhost:8080/connect/OAuth.do?token=" + t.getSoftwareToken() + "&softwareName="
					+ softwareName);
			
			req.getSession().setAttribute("errorMessage", "true");
			break;
		case "Moloni":
			softwareToken = (String) req.getSession().getAttribute("softwareToken");
			code = req.getParameter("code");
			System.out.println("CODE " + code);
			error = req.getParameter("error");
			t = null;
			if (code != null) {
				t = OAuthMoloni.getAccessToken(code, null, softwareName, softwareToken);
			} else if (error != null) {
				System.out.println("Error Moloni authentication: " + error);
				break;
			}
			ArrayList<Map<String, String>> offices1 = (ArrayList<Map<String, String>>) MoloniHandler.getOffices(t);
			req.getSession().setAttribute("offices", offices1);
			System.out.println("accessToken " + t.getAccessToken());
			System.out.println("refreshToken(secret) " + t.getAccessSecret());
			System.out.println("clientID " + t.getConsumerToken());
			System.out.println("clientSecret " + t.getConsumerSecret());
			System.out.println("softwareToken " + t.getSoftwareToken());
			System.out.println("softwareName " + t.getSoftwareName());
			if (redirect != null) {
				resp.sendRedirect(
						redirect + "OAuth.do?token=" + t.getSoftwareToken() + "&softwareName=" + softwareName);
			} else {
				resp.sendRedirect("https://www.localhost:8080/connect/OAuth.do?token=" + t.getSoftwareToken()
						+ "&softwareName=" + softwareName);
			}
			req.getSession().setAttribute("errorMessage", "true");
			break;
		}
	}
}
