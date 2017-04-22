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
	private String[] messageArray = null;
	private String checkUpdate = "false";
	private String errorDetails = "";

	protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
		errorMessage = "";
		String softwareToken = req.getParameter("token");
		String softwareName = req.getParameter("softwareName");
		ArrayList<Token> allTokens = null;
		// Sync data from current user
		if (softwareToken != null) {
			try {
				Token t = TokenDAO.getToken(softwareToken, softwareName);
				if (t != null && !t.getAccessSecret().equals("invalid")) {
					softwareName = t.getSoftwareName();
					this.setSyncMethods(t, req, true);
					if (redirect != null) {
						resp.sendRedirect(
								redirect + "OAuth.do?token=" + softwareToken + "&softwareName=" + softwareName);
					} else {
						resp.sendRedirect("http://localhost:8080/connect/OAuth.do?token=" + softwareToken
								+ "&softwareName=" + softwareName);
					}
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
				if (WorkOrderHandler.checkWorkOrderToken(token, softwareName) == 200) {
					try {
						this.setSyncMethods(t, null, false);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			System.out.println(allTokens.size() + " Users found in database");
		}
	}

	public String getDate(String date) {
		String timestamp;
		ZonedDateTime za = ZonedDateTime.now(ZoneId.of("Europe/Paris"));
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
				DBConnection.createDatabaseConnection().close();
				break;
			case "WeFact":
				weFactImport(t.getSoftwareToken(), t.getAccessToken(), date);
				DBConnection.createDatabaseConnection().close();
				break;
			default:
				break;
			}
		} catch (ServletException | IOException | JSONException e) {
			e.printStackTrace();
		}

	}
	//Twinfield
	public void setErrorMessage(String[] messageArray) {
		if (messageArray != null) {
			errorMessage += messageArray[0];
			if (messageArray[1].equals("true")) {
				checkUpdate = "true";
			}
		}
	}
	//Twinfield
	public void setErrorMessageDetails(String[] messageArray) {
		if (messageArray != null) {
			errorMessage += messageArray[0];
			if (messageArray[1] != null) {
				errorDetails = messageArray[1];
			}
		}
	}
	//WeFact
	public void setErrorMessageWeFact(String[] messageArray) {
		if (messageArray != null) {
			errorMessage += messageArray[0];
			if (messageArray[1].equals("true")) {
				checkUpdate = "true";
			}
		}
	}
	
	//WeFact
	public void setErrorMessageDetailsWeFact(String[] messageArray) {
		if (messageArray != null) {
			errorMessage += messageArray[0];
			if (messageArray[1] != null) {
				errorDetails = messageArray[1];
			}
		}
	}

	public void twinfieldImport(String session, String cluster, String token, String softwareName, String date)
			throws Exception {
		TwinfieldHandler twinfield = new TwinfieldHandler();
		Settings set = ObjectDAO.getSettings(token);
		if (set != null) {
			ArrayList<String> importTypes = set.getImportObjects();
			// Import section
			for (String type : importTypes) {
				switch (type) {
				case "employees":
					messageArray = twinfield.getEmployees(set.getImportOffice(), session, cluster, token, softwareName,
							date);
					setErrorMessage(messageArray);
					break;
				case "projects":
					messageArray = twinfield.getProjects(set.getImportOffice(), session, cluster, token, softwareName,
							date);
					setErrorMessage(messageArray);
					break;
				case "materials":
					messageArray = twinfield.getMaterials(set.getImportOffice(), session, cluster, token, softwareName,
							date);
					setErrorMessage(messageArray);
					break;
				case "relations":
					messageArray = twinfield.getRelations(set.getImportOffice(), session, cluster, token, softwareName,
							date);
					setErrorMessage(messageArray);
					break;
				case "hourtypes":
					messageArray = twinfield.getHourTypes(set.getImportOffice(), session, cluster, token, softwareName,
							date);
					setErrorMessage(messageArray);
					break;
					
				}
			}
			// Export section
			String[] exportMessageArray = twinfield.setWorkOrders(set.getExportOffice(), session, cluster, token,
					set.getFactuurType(), softwareName, set.getUser());
			setErrorMessageDetails(exportMessageArray);
			if (checkUpdate.equals("true")) {
				TokenDAO.saveModifiedDate(getDate(null), token);
			}
			if (!errorMessage.equals("")) {
				ObjectDAO.saveLog(errorMessage, errorDetails, token);
			} else {
				ObjectDAO.saveLog("Niks te importeren", errorDetails, token);
			}
		}
	}

	public void weFactImport(String token, String clientToken, String date) throws Exception {
		errorMessage = "";
		WeFactHandler wefact = new WeFactHandler();
		Settings set = ObjectDAO.getSettings(token);
		if (set != null) {
			ArrayList<String> importTypes = set.getImportObjects();
			// Import section
			for (String type : importTypes) {
				switch (type) {
				case "materials":
					messageArray = wefact.getMaterials(clientToken, token, date);
					setErrorMessageWeFact(messageArray);
					break;
				case "relations":
					messageArray = wefact.getRelations(clientToken, token, date);
					setErrorMessageWeFact(messageArray);
					break;
				case "hourtypes":
					messageArray = wefact.getHourTypes(clientToken, token, date);
					setErrorMessageWeFact(messageArray);
					break;
				case "offertes":
					messageArray = wefact.getOffertes(clientToken, token, date);
					setErrorMessageWeFact(messageArray);
					break;
				}
			}
			// Export section
			String[] exportMessageArray = null;
			// Type is factuur
			if (set.getExportWerkbontype().equals("factuur")) {
				exportMessageArray = wefact.setFactuur(clientToken, token, set.getFactuurType(), set.getRoundedHours());
			// Type is offerte
			} else {
				exportMessageArray = wefact.setOfferte(clientToken, token, set.getFactuurType(), set.getRoundedHours());
			}
			setErrorMessageDetailsWeFact(exportMessageArray);
			if (checkUpdate.equals("true")) {
				TokenDAO.saveModifiedDate(getDate(null), token);
			}
			if (!errorMessage.equals("")) {
				ObjectDAO.saveLog(errorMessage, errorDetails, token);
			} else {
				ObjectDAO.saveLog("Niks te importeren", errorDetails, token);
			}
		}
	}
}
