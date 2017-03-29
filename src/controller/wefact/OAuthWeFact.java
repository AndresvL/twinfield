package controller.wefact;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.ClientProtocolException;

import DAO.ObjectDAO;
import DAO.TokenDAO;
import controller.Authenticate;
import object.Settings;
import object.Token;

public class OAuthWeFact extends Authenticate {
	private Token tokenObject;

	@Override
	public void authenticate(String softwareToken, HttpServletRequest req, HttpServletResponse resp)
			throws ClientProtocolException, IOException, ServletException {
		RequestDispatcher rd = null;
		String softwareName = (String) req.getSession().getAttribute("softwareName");
		Token dbToken = null;
		// Get token from database
		try {
			dbToken = TokenDAO.getToken(softwareToken, softwareName);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (dbToken == null) {
			if (!req.getParameterMap().containsKey("clientToken")) {
				// Login page
				rd = req.getRequestDispatcher("weFact.jsp");
				req.getSession().setAttribute("clientToken", null);
				// If clientToken is filled in
			} else {
				// Settings page
				String clientToken = req.getParameter("clientToken");
				WeFactHandler we = new WeFactHandler();
				if (we.checkClientToken(clientToken)) {
					tokenObject = new Token();
					tokenObject.setAccessToken(clientToken);
					tokenObject.setSoftwareName(softwareName);
					tokenObject.setSoftwareToken(softwareToken);
					// Save token to database
					try {
						TokenDAO.saveToken(tokenObject);
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					rd = req.getRequestDispatcher("weFact.jsp");
					req.getSession().setAttribute("clientToken", clientToken);
					req.getSession().setAttribute("errorMessage", "You are logged in");
				} else {
					// Login page
					rd = req.getRequestDispatcher("weFact.jsp");
					req.getSession().setAttribute("errorMessage", "Security code invalid!");
					req.getSession().setAttribute("clientToken", null);
				}
			}
		} else if (dbToken.getAccessSecret().equals("invalid")) {
			rd = req.getRequestDispatcher("weFact.jsp");
			req.getSession().setAttribute("errorMessage", "softwareToken is al in gebruik door " + dbToken.getSoftwareName());
		} else {
			req.getSession().setAttribute("clientToken", dbToken.getAccessToken());
			ArrayList<Map<String, String>> allLogs = ObjectDAO.getLogs(softwareToken);
			if (!allLogs.isEmpty() || allLogs != null) {
				req.getSession().setAttribute("logs", null);
				req.getSession().setAttribute("logs", allLogs);
			}
			Settings set = ObjectDAO.getSettings(softwareToken);
			if (set != null) {
				Map<String, String> allImports = new HashMap<String, String>();
				for(String s : set.getImportObjects()){
					allImports.put(s, "selected");
				}
				Map<String, String> exportWerkbonType = new HashMap<String, String>();
				exportWerkbonType.put(set.getExportWerkbontype(), "selected");
				
				req.getSession().setAttribute("checkboxes", allImports);
				req.getSession().setAttribute("exportWerkbonType", exportWerkbonType);
				req.getSession().setAttribute("factuur", set.getFactuurType());
			}
			rd = req.getRequestDispatcher("weFact.jsp");
		}
		rd.forward(req, resp);

	}

}
