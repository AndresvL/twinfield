package servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;

import DAO.ObjectDAO;
import DAO.TokenDAO;
import DBUtil.DBConnection;
import controller.WorkOrderHandler;
import controller.twinfield.SoapHandler;
import controller.twinfield.TwinfieldHandler;
import controller.wefact.WeFactHandler;
import object.Settings;
import object.Token;

public class SynchServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String errorMessage;
	private String redirect = System.getenv("CALLBACK");
	private String clientToken = null;
	private String[] messageArray = null;
	private String checkUpdate = "false";
	private String errorDetails = "";

	protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
		errorMessage = "";
		String softwareToken = req.getParameter("token");
		String softwareName = req.getParameter("softwareName");
		ArrayList<Token> allTokens = null;
		clientToken = (String) req.getSession().getAttribute("clientToken");
		// Sync data from current user
		if (softwareToken != null) {
			try {
				Token t = TokenDAO.getToken(softwareToken, softwareName);
				softwareName = t.getSoftwareName();
				this.setSyncMethods(t, req, true);
				if (redirect != null) {
					resp.sendRedirect(redirect + "OAuth.do?token=" + softwareToken + "&softwareName=" + softwareName);
				} else {
					resp.sendRedirect("http://localhost:8080/connect/OAuth.do?token=" + softwareToken + "&softwareName="
							+ softwareName);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			// Get all users from database to Sync their data all at once
		} else {
			try {
				allTokens = TokenDAO.getSoftwareTokens();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			String token = null;
			for (Token t : allTokens) {
				softwareName = t.getSoftwareName();
				token = t.getSoftwareToken();
				System.out.println(allTokens.size() + " Users found in database");
				if (WorkOrderHandler.checkWorkOrderToken(token, softwareName) == 200) {
					try {
						this.setSyncMethods(t, null, false);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public String getDate(String date) {
		String timestamp;
		LocalDateTime a = LocalDateTime.now();
		ZoneId zone = ZoneId.of("Europe/Paris");
		ZonedDateTime za = ZonedDateTime.of(a, zone).plusHours(1);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		if (date != null) {
			timestamp = date;
		} else {
			timestamp = za.format(formatter);
		}
		return timestamp;
	}

	public void setSyncMethods(Token t, HttpServletRequest req, boolean loggedIn) throws Exception {
		String date = null;
		String sessionID = null;
		String cluster = null;
		// if(req != null){
		// sessionID = (String)req.getSession().getAttribute("session");
		// cluster = (String)req.getSession().getAttribute("cluster");
		// }
		// sessionID from session is old when sync.do has been called!!!
		if (!loggedIn) {
			date = TokenDAO.getModifiedDate(t.getSoftwareToken());
		}
		try {
			switch (t.getSoftwareName()) {
			case "Twinfield":
				String[] array = SoapHandler.getSession(t);
				if (array != null) {
					sessionID = array[0];
					cluster = array[1];
					twinfieldImport(sessionID, cluster, t.getSoftwareToken(), t.getSoftwareName(), date);
				}
				break;
			case "WeFact":
				weFactImport(t.getSoftwareToken(), clientToken, date);
				break;
			default:
				break;
			}
		} catch (ServletException | IOException | JSONException e) {
			e.printStackTrace();
		}

	}

	public void setErrorMessage(String[] messageArray) {
		if (messageArray != null) {
			errorMessage += messageArray[0];
			if (messageArray[1].equals("true")) {
				checkUpdate = "true";
			}
		}
	}

	public void setErrorMessageDetails(String[] messageArray) {
		System.out.println("errorMessage = " + messageArray[0]);
		System.out.println("errorDetails = " + messageArray[1]);
		if (messageArray != null) {
			errorMessage = messageArray[0];
			if (messageArray[1] != null) {
				errorDetails = messageArray[1];
			}
		}
	}

	public void twinfieldImport(String session, String cluster, String token, String softwareName, String date)
			throws Exception {
		TwinfieldHandler twinfield = new TwinfieldHandler();
		Settings set = ObjectDAO.getSettings(token);
		Connection con = DBConnection.createDatabaseConnection();
		if (set != null) {
			ArrayList<String> importTypes = set.getImportObjects();
			// Import section
			for (String type : importTypes) {
				switch (type) {
				case "employees":
					messageArray = twinfield.getEmployees(set.getImportOffice(), session, cluster, token, softwareName,
							date, con);
					setErrorMessage(messageArray);
					break;
				case "projects":
					messageArray = twinfield.getProjects(set.getImportOffice(), session, cluster, token, softwareName,
							date, con);
					setErrorMessage(messageArray);
					break;
				case "materials":
					messageArray = twinfield.getMaterials(set.getImportOffice(), session, cluster, token, softwareName,
							date, con);
					setErrorMessage(messageArray);
					break;
				case "relations":
					messageArray = twinfield.getRelations(set.getImportOffice(), session, cluster, token, softwareName,
							date, con);
					setErrorMessage(messageArray);
					break;
				case "hourtypes":
					messageArray = twinfield.getHourTypes(set.getImportOffice(), session, cluster, token, softwareName,
							date, con);
					setErrorMessage(messageArray);
					break;
				}
			}
			// Export section
			String[] exportMessageArray = twinfield.getWorkOrders(set.getExportOffice(), session, cluster, token,
					set.getFactuurType(), softwareName, set.getUser());
			setErrorMessageDetails(exportMessageArray);
			if (checkUpdate.equals("true")) {
				TokenDAO.saveModifiedDate(getDate(null), token);
			}
			if (!errorMessage.equals("")) {
				ObjectDAO.saveLog(errorMessage, errorDetails, token, con);
			} else {
				ObjectDAO.saveLog("Niks te importeren", errorDetails, token, con);
			}
			con.close();
		}
	}

	public void weFactImport(String token, String clientToken, String date) throws Exception {
		WeFactHandler wefact = new WeFactHandler();
		Settings set = ObjectDAO.getSettings(token);
		if (set != null) {
			Connection con = DBConnection.createDatabaseConnection();
			ArrayList<String> importTypes = set.getImportObjects();
			// Import section
			for (String type : importTypes) {
				switch (type) {
				case "materials":
					errorMessage += wefact.getMaterials(clientToken, token, date, con);
					break;
				case "relations":
					errorMessage += wefact.getRelations(clientToken, token, date, con);
					break;
				case "hourtypes":
					errorMessage += "hourtype";
					break;
				}
			}
			if (errorMessage.startsWith("Success")) {
				TokenDAO.saveModifiedDate(getDate(null), token);
			}
			// Export section
			// wefact.getWorkOrders(set.getExportOffice(), session, token,
			// set.getFactuurType());
			ObjectDAO.saveLog(errorMessage, null, token, con);
		}
	}
}
