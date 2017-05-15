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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import DAO.ObjectDAO;
import DAO.TokenDAO;
import controller.Authenticate;
import controller.WorkOrderHandler;
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
			WeFactHandler we = new WeFactHandler();
			if (!req.getParameterMap().containsKey("clientToken")) {
				rd = req.getRequestDispatcher("weFact.jsp");
				req.getSession().setAttribute("clientToken", null);
				System.out.println("DBTOKEN " + dbToken);
			// If clientToken is filled
			} else {
				String clientToken = req.getParameter("clientToken");
				Object obj = we.checkClientToken(clientToken);
				//Check if object is boolean; if true object is always true
				if (obj instanceof Boolean && (boolean)obj) {
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
					System.out.println("Session clientToken " + clientToken);
					req.getSession().setAttribute("errorMessage", "true");
					//Set workstatusses once in WorkOrdeApp
					JSONArray JSONArray = new JSONArray();
					JSONObject JSONObject = null;
					try {
						JSONObject = new JSONObject();
						JSONObject.put("sta_code", "0");
						JSONObject.put("sta_name", "Concept factuur");
						JSONArray.put(JSONObject);
						JSONObject = new JSONObject();
						JSONObject.put("sta_code", "2");
						JSONObject.put("sta_name", "Verzonden");
						JSONArray.put(JSONObject);
						JSONObject = new JSONObject();
						JSONObject.put("sta_code", "3");
						JSONObject.put("sta_name", "Deels betaald");
						JSONArray.put(JSONObject);
						JSONObject = new JSONObject();
						JSONObject.put("sta_code", "4");
						JSONObject.put("sta_name", "Betaald");
						JSONArray.put(JSONObject);
						JSONObject = new JSONObject();
						JSONObject.put("sta_code", "8");
						JSONObject.put("sta_name", "Creditfactuur");
						JSONArray.put(JSONObject);
						JSONObject = new JSONObject();
						JSONObject.put("sta_code", "9");
						JSONObject.put("sta_name", "Betaald");
						JSONArray.put(JSONObject);
						WorkOrderHandler.addData(softwareToken, JSONArray, "workstatusses", softwareName, clientToken);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				} else {
					// Login page
					rd = req.getRequestDispatcher("weFact.jsp");
					req.getSession().setAttribute("errorMessage", obj);
					req.getSession().setAttribute("clientToken", null);
				}
			}
		} else if (dbToken.getAccessSecret().equals("invalid")) {
			rd = req.getRequestDispatcher("weFact.jsp");
			req.getSession().setAttribute("clientToken", null);
			req.getSession().setAttribute("errorMessage", "softwareToken is al in gebruik door " + dbToken.getSoftwareName());
		} else {
			req.getSession().setAttribute("clientToken", dbToken.getAccessToken());
			req.getSession().setAttribute("errorMessage", "");
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
				
				req.getSession().setAttribute("savedDate", set.getSyncDate());
				req.getSession().setAttribute("checkboxes", allImports);
				req.getSession().setAttribute("exportWerkbonType", exportWerkbonType);
				req.getSession().setAttribute("roundedHours", set.getRoundedHours());
				req.getSession().setAttribute("factuur", set.getFactuurType());
			}
			rd = req.getRequestDispatcher("weFact.jsp");
		}
		rd.forward(req, resp);

	}

}
