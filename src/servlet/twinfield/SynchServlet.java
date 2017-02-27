package servlet.twinfield;

import java.io.IOException;
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
import controller.WorkOrderHandler;
import controller.twinfield.SoapHandler;
import controller.twinfield.TwinfieldHandler;
import controller.wefact.WeFactHandler;
import object.Settings;
import object.Token;

public class SynchServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String errorMessage = "";
	private String redirect = System.getenv("CALLBACK");
	private String softwareName = null;
	private String clientToken = null;

	protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
		errorMessage = "";
		String softwareToken = req.getParameter("token");
		ArrayList<Token> allTokens = null;
		clientToken = (String) req.getSession().getAttribute("clientToken");
		// Sync data from current user
		if (softwareToken != null) {
			try {
				Token t = TokenDAO.getToken(softwareToken);
				softwareName = t.getSoftwareName();
				System.out.println("2. softwareName " + softwareName);
				this.setSyncMethods(t, req);
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
				if (WorkOrderHandler.checkWorkOrderToken(token) == 200) {
					try {
						this.setSyncMethods(t, req);
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

	public void setSyncMethods(Token t, HttpServletRequest req) throws Exception {
		try {
			switch (t.getSoftwareName()) {
			case "Twinfield":
				System.out.println("2. token " + t.getSoftwareToken());
				String sessionID = SoapHandler.getSession(t);
				System.out.println("2. Session " + sessionID);
				twinfieldImport(sessionID, t.getSoftwareToken());
				break;
			case "WeFact":
				String date = TokenDAO.getModifiedDate(t.getSoftwareToken());
				weFactImport(t.getSoftwareToken(), clientToken, date);
				break;
			default:
				break;
			}
		} catch (ServletException | IOException | JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void twinfieldImport(String session, String token) throws ServletException, IOException {
		TwinfieldHandler twinfield = new TwinfieldHandler();
		Settings set = ObjectDAO.getSettings(token);
		if (set != null) {
			ArrayList<String> importTypes = set.getImportObjects();
			// Import section
			for (String type : importTypes) {
				switch (type) {
				case "employees":
					errorMessage += twinfield.getEmployees(set.getImportOffice(), session, token);
					break;
				case "projects":
					errorMessage += twinfield.getProjects(set.getImportOffice(), session, token);
					break;
				case "materials":
					errorMessage += twinfield.getMaterials(set.getImportOffice(), session, token);
					break;
				case "relations":
					errorMessage += twinfield.getRelations(set.getImportOffice(), session, token);
					break;
				case "hourtypes":
					errorMessage += twinfield.getHourTypes(set.getImportOffice(), session, token);
					break;
				}
			}
			// Export section
			errorMessage = twinfield.getWorkOrders(set.getExportOffice(), session, token, set.getFactuurType());
			ObjectDAO.saveLog(errorMessage, token);
		}
	}

	public void weFactImport(String token, String clientToken, String date) throws Exception {
		WeFactHandler wefact = new WeFactHandler();
		Settings set = ObjectDAO.getSettings(token);
		if (set != null) {
			ArrayList<String> importTypes = set.getImportObjects();
			// Import section
			for (String type : importTypes) {
				switch (type) {
				case "materials":

					System.out.println("test");
					errorMessage += wefact.getMaterials(clientToken, token, date);
					break;
				case "relations":
					errorMessage += wefact.getRelations(clientToken, token, date);
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
//			System.out.println("errorMessage " + errorMessage);
			ObjectDAO.saveLog(errorMessage, token);
		}
	}
}
